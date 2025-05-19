package com.hsiatein.shuarknights.item;

import com.hsiatein.shuarknights.hud.samsara;
import com.hsiatein.shuarknights.shuarknights;
import com.hsiatein.shuarknights.hud.bountiful_harvest;
import com.hsiatein.shuarknights.utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.lang.Math;

import static org.apache.commons.lang3.math.NumberUtils.min;

public class yucong extends SwordItem {
    public static final int MAX_CHARGE_DURATION = 100;
    private static final int MAX_EXPAND_TIMES = 200;
    private static final int START_TICK = 10;


    public yucong(Properties properties) {
        super(Tiers.NETHERITE, 5, -2.4F, properties.durability(0).fireResistant());
    }



    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack itemStack = new ItemStack(this);
        CompoundTag tag = new CompoundTag();
        tag.putInt("useDuration", 0); // 设置默认 useDuration
        tag.putInt("refineTimes", 0);
        tag.putInt("coolDown", 0);
        itemStack.setTag(tag); // 应用标签到 ItemStack
        return itemStack;
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!world.isClientSide) {
            player.startUsingItem(hand);
            // 恢复生命值
            // player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            // 提升抗性
        }
        //player.releaseUsingItem();
        return super.use(world, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
    {
        if(samsara.DURATION<samsara.MAX_DURATION){
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
        }
        if(bountiful_harvest.DURATION<bountiful_harvest.MAX_DURATION){
            if(entity instanceof Mob e){
//                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 255));
//                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 255));
                e.setNoAi(true);
                Vec3 newPos=new Vec3(e.position().x,e.position().y-e.getEyeHeight(),e.position().z);
                e.setPos(newPos);
                player.playSound(shuarknights.PLANT_IN_SOIL_SOUND.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level world, @NotNull LivingEntity player, int remainTime) {
        int useDuration = this.getUseDuration(itemStack) - remainTime;
        // player.sendSystemMessage(Component.nullToEmpty(player.getLookAngle().toString()));
        if (!world.isClientSide && useDuration >= 100 && player.getLookAngle().y>0.75 && !player.isShiftKeyDown()) {
            // player.sendSystemMessage(Component.nullToEmpty(String.valueOf(useDuration)));

            // 判断蓄力时间
            if (world.isRaining()) {
                // 如果是雨天，变为晴天
                ((ServerLevel) world).setWeatherParameters(12000, 0, false, false);

                // player.sendSystemMessage(Component.nullToEmpty("雨->晴"));
            } else {
                // 如果是晴天，变为雨天
                ((ServerLevel) world).setWeatherParameters(0, 12000, true, utils.random.nextInt(100) < 20);
                // player.sendSystemMessage(Component.nullToEmpty("晴->雨"));
            }
        }

    }
    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count)
    {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.putInt("useDuration", 0);
            tag.putInt("refineTimes", 0);
        }
        // RunTime.clear();
    }
    @Override
    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack itemStack, int remainTime) {
        int useDuration=this.getUseDuration(itemStack)-remainTime;
        if((useDuration<MAX_CHARGE_DURATION && useDuration%8==0)||useDuration==MAX_CHARGE_DURATION){
            CompoundTag tag = itemStack.getTag();
            if (tag != null) {
                tag.putInt("useDuration", useDuration);
            }
        }

        if(!world.isClientSide && player.isShiftKeyDown()){
            if(useDuration< START_TICK){
                // player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
                return;
            }
            if(samsara.CHARGES<=0) return;
            samsara.startPos=player.blockPosition();
            while (!utils.canTransmit(world,samsara.startPos) && samsara.startPos.getY()>-64){
                samsara.startPos=samsara.startPos.below();
            }
            samsara.world=world;
            samsara.CHARGES--;
            samsara.DURATION=0;
            player.playSound(shuarknights.SAMSARA_SOUND.get());
        }
        if(!world.isClientSide && !player.isShiftKeyDown()){
            if(useDuration< START_TICK){
                // player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
                return;
            }
            if(bountiful_harvest.CHARGES<=0) return;
            bountiful_harvest.startPos=player.blockPosition();
            while (!utils.canTransmit(world,bountiful_harvest.startPos) && bountiful_harvest.startPos.getY()>-64){
                bountiful_harvest.startPos=bountiful_harvest.startPos.below();
            }
            bountiful_harvest.world=world;
            bountiful_harvest.CHARGES--;
            bountiful_harvest.DURATION=0;
        }
    }


//    @Override
//    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack itemStack, int remainTime) {
//        int useDuration=this.getUseDuration(itemStack)-remainTime;
//        if((useDuration<MAX_CHARGE_DURATION && useDuration%8==0)||useDuration==MAX_CHARGE_DURATION){
//            CompoundTag tag = itemStack.getTag();
//            if (tag != null) {
//                tag.putInt("useDuration", useDuration);
//            }
//        }
//
//        if(!world.isClientSide && player.isShiftKeyDown()){
//            if(useDuration< START_TICK){
//                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
//                if(useDuration== START_TICK -1) utils.getAllFlowers();
//                else if(useDuration== START_TICK -2) utils.getAllSaplings();
//                return;
//            }
//            int refineTimes=0;
//            CompoundTag tag = itemStack.getTag();
//            if(useDuration%40== START_TICK){
//                if (tag != null) {
//                    refineTimes = tag.getInt("refineTimes");
//                    tag.putInt("refineTimes",refineTimes+1);
//                }
//                BlockPos startPos = player.blockPosition();
//                while(!utils.canTransmit(world,startPos)){
//                    startPos=startPos.below();
//                }
//                utils.performOnEveryPos(world,startPos);
//                // RunTime.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
//                RunTime.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100*(refineTimes+1), min(refineTimes,4)-1));
//                RunTime.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100*(refineTimes+1), min(refineTimes,4)-1));
//                RunTime.addExploredTimes(startPos);
//            }
//            int i = 0;
//            if (tag != null) refineTimes = tag.getInt("refineTimes");
//
//            while (i<MAX_EXPAND_TIMES && !RunTime.isEmpty()){
//                BlockPos u= RunTime.pop();
//                if(utils.discard(refineTimes,RunTime.exploredTimes(u))) continue;
//                ArrayDeque<BlockPos> neighbors = utils.getNeighbors(world, u);
//                for(BlockPos v:neighbors){
//                    utils.performOnEveryPos(world,v);
//                }
//                i++;
//            }
//            RunTime.transmitEnemy();
//        }
//    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            return tag.getInt("useDuration") > 0; // 检查 customData 是否大于 0
        }
        return false;
    }
    @Override
    public int getBarWidth(@NotNull ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            int useDuration=tag.getInt("useDuration");
            return Math.round(13.0F*min(MAX_CHARGE_DURATION,useDuration)/MAX_CHARGE_DURATION);
        }
        return 0;
    }
    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        if(this.getBarWidth(itemStack)==13) return Mth.hsvToRgb(0.5464F, 0.4575F, 0.8314F);
        return Mth.hsvToRgb(0.2023F, 0.5628F, 0.7176F);
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        int useDuration=0;
        if (tag != null) {
            useDuration = tag.getInt("useDuration");
        }
        if(useDuration< START_TICK) return UseAnim.BLOCK;
        else return UseAnim.BOW;
    }
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();// 如果其他部分不同，则返回 true
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        if(Screen.hasShiftDown()){
            components.add(Component.literal("短暂使用可以恢复生命值, 长时间使用可以把周围环境改造成更适合作物生长的样子, 甚至拥有呼风唤雨的能力.").withStyle(ChatFormatting.AQUA));
        }else{
            components.add(Component.literal("黍的玉琮, 在黍的手上可以发挥出神奇的效果.").withStyle(ChatFormatting.AQUA));
            components.add(Component.literal("按 SHIFT 获得更多信息").withStyle(ChatFormatting.YELLOW));

        }
        super.appendHoverText(itemStack, level, components, tooltipFlag);
    }
    @Override
    public int getUseDuration(@NotNull ItemStack p_40680_) {
        return 1200;
    }



}