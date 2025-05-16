package com.hsiatein.shuarknights.item;

import com.mojang.serialization.Codec;
import com.hsiatein.shuarknights.utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;
import static org.apache.commons.lang3.math.NumberUtils.min;

public class yucong extends SwordItem {
    public static final int MAX_CHARGE_DURATION = 100;
    private static ArrayDeque<BlockPos> closeList = new ArrayDeque<>();
    private static final int MAX_EXPAND_TIMES = 200;


    public yucong(Properties properties) {
        super(Tiers.NETHERITE, 5, -2.4F, properties.durability(0));
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack itemStack = new ItemStack(this);
        CompoundTag tag = new CompoundTag();
        tag.putInt("useDuration", 0); // 设置默认 useDuration
        tag.putInt("refineTimes", 0);
        itemStack.setTag(tag); // 应用标签到 ItemStack
        return itemStack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        if (!world.isClientSide) {

            // 恢复生命值
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            // 提升抗性
            // player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1)); // 持续 10 秒，抗性等级 1
        }
        //player.releaseUsingItem();
        return super.use(world, player, hand);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level world, @NotNull LivingEntity player, int remainTime) {
        int useDuration = this.getUseDuration(itemStack) - remainTime;

        if (!world.isClientSide && useDuration >= 100) {
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
        RunTime.clear();
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

//        if (!world.isClientSide && useDuration==9){
//            utils.getAllFlowers();
//            // player.sendSystemMessage(Component.nullToEmpty(String.valueOf(utils.ALL_FLOWERS.length)));
//        }else if (!world.isClientSide && useDuration==8){
//            utils.getAllSaplings();
//            // player.sendSystemMessage(Component.nullToEmpty(String.valueOf(utils.ALL_SAPLINGS.length)));
//        }
        int startTick=10;
        if(!world.isClientSide){
            if(useDuration<startTick){
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
                if(useDuration==startTick-1) utils.getAllFlowers();
                else if(useDuration==startTick-2) utils.getAllSaplings();
                return;
            }
            int refineTimes=0;
            CompoundTag tag = itemStack.getTag();
            if(useDuration%40==startTick){
                if (tag != null) {
                    refineTimes = tag.getInt("refineTimes");
                    tag.putInt("refineTimes",refineTimes+1);
                }
                BlockPos startPos = player.blockPosition();
                while(!utils.canTransmit(world,startPos)){
                    startPos=startPos.below();
                }
                performOnEveryPos(world,startPos);
                // RunTime.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
                RunTime.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100*(refineTimes+1), min(refineTimes,4)-1));
                RunTime.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100*(refineTimes+1), min(refineTimes,4)-1));
                RunTime.addExploredTimes(startPos);
            }
            int i = 0;
            if (tag != null) refineTimes = tag.getInt("refineTimes");

            while (i<MAX_EXPAND_TIMES && !RunTime.isEmpty()){
                BlockPos u= RunTime.pop();
                if(utils.discard(refineTimes,RunTime.exploredTimes(u))) continue;
                ArrayDeque<BlockPos> neighbors = getNeighbors(world, u);
                for(BlockPos v:neighbors){
                    performOnEveryPos(world,v);
                }
                i++;
            }
        }
//        if (!world.isClientSide && useDuration>=startTick) {
//
//
//            //player.sendSystemMessage(Component.nullToEmpty(playerPos.toString()));
//
//        }
    }

    private ArrayDeque<BlockPos> getNeighbors(@NotNull Level world, BlockPos pos){
        ArrayDeque<BlockPos> result = new ArrayDeque<>();
        ArrayDeque<BlockPos> neighbors = utils.getNeighbors26(pos);
        for(BlockPos neighbor:neighbors){
            if(utils.isValidSuccessor(world,neighbor) && yucong.RunTime.exploredTimes(neighbor)< yucong.RunTime.exploredTimes(pos)){
                result.addLast(neighbor);
                yucong.RunTime.addExploredTimes(neighbor);
            }
        }
        return result;
    }

    private void performOnEveryPos(Level world, BlockPos pos){
        utils.refineBlock(world,pos);
        RunTime.pushCreature(world,pos);
        RunTime.push(pos);
    }




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
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack p_41452_) {
        return UseAnim.BOW;
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


    static class RunTime{
        private static final ArrayDeque<BlockPos> openList = new ArrayDeque<>();
        private static final HashMap<BlockPos, Integer> explored = new HashMap<>();
        private static final ArrayDeque<LivingEntity> creatureList = new ArrayDeque<>();
        // private static final ArrayDeque<BlockPos> neighborsList = new ArrayDeque<>();
        public static void push(BlockPos pos){
            openList.addLast(pos);
        }
        public static void pushCreature(Level world, BlockPos pos){
            creatureList.addAll(utils.getAllCreatures(world,pos));
        }
        public static void addEffect(MobEffectInstance mobEffectInstance){
            for(LivingEntity e:creatureList){
                e.addEffect(mobEffectInstance);
            }
        }
        public static void pushAll(ArrayDeque<BlockPos> allPos){
            openList.addAll(allPos);
        }
        public static BlockPos pop(){
            return openList.pollFirst();
        }
        public static void clear(){
            openList.clear();
            explored.clear();
            creatureList.clear();
        }
        public static boolean isEmpty(){
            return openList.isEmpty();
        }
        public static int exploredTimes(BlockPos pos){
            if(explored.get(pos)==null) return 0;
            else return explored.get(pos);
        }
        public static void addExploredTimes(BlockPos pos){
            explored.put(pos,exploredTimes(pos)+1);
        }

    }
}