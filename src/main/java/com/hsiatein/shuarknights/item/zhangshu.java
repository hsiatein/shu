package com.hsiatein.shuarknights.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class zhangshu extends SwordItem {
    public static final int MAX_DRAW_DURATION = 20;

    public zhangshu(Properties properties) {
        super(Tiers.NETHERITE, 5, -2.4F, properties.durability(0));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        if (!world.isClientSide) {

            // 恢复生命值
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            // 提升抗性
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1)); // 持续 10 秒，抗性等级 1
            player.swing(hand);
        } else {
            player.swing(hand);

        }
        //player.releaseUsingItem();
        return super.use(world, player, hand);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    public int getUseDuration(@NotNull ItemStack p_40680_) {
        return 1200;
    }

    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level world, @NotNull LivingEntity player, int remainTime) {
        int useDuration = this.getUseDuration(itemStack) - remainTime;

        if (!world.isClientSide && useDuration >= 100) {
            // player.sendSystemMessage(Component.nullToEmpty(String.valueOf(useDuration)));
            Random random = new Random();
            // 判断蓄力时间
            if (world.isRaining()) {
                // 如果是雨天，变为晴天
                ((ServerLevel) world).setWeatherParameters(4800, 0, false, false);

                // player.sendSystemMessage(Component.nullToEmpty("雨->晴"));
            } else {
                // 如果是晴天，变为雨天
                ((ServerLevel) world).setWeatherParameters(0, 4800, true, random.nextInt(100) < 20);
                // player.sendSystemMessage(Component.nullToEmpty("晴->雨"));
            }
        }

    }

    public boolean isBarVisible(@NotNull ItemStack p_150899_) {
        return false;
    }

    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack itemStack, int remainTime) {
//        int useDuration=this.getUseDuration(itemStack)-remainTime;
//        if (!world.isClientSide) {
//            // player.sendSystemMessage(Component.nullToEmpty(String.valueOf(useDuration)));
//        }
    }

}