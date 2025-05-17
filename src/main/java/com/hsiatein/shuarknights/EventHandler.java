package com.hsiatein.shuarknights;

import com.hsiatein.shuarknights.hud.shu_skill_one;
import com.hsiatein.shuarknights.item.yucong;
import com.hsiatein.shuarknights.network.ModMessages;
import com.hsiatein.shuarknights.network.SwingMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shuarknights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void shu_skill_one(TickEvent.PlayerTickEvent event) {
        Player entity = event.player;
        if (entity.getHealth() > entity.getMaxHealth() * 0.5 || !entity.isAlive()) return;
        // 确保是玩家
        ServerPlayer player= (ServerPlayer) entity;
        ItemStack heldItem = player.getMainHandItem();
        // 检查是否持有该物品
        if (heldItem.getItem() instanceof yucong) {
            // 检查血量是否低于50%
            ModMessages.sendToPlayer(new SwingMsg(), player);
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        }
//        if (entity instanceof ServerPlayer player) {
//
//        }
    }

    @SubscribeEvent
    public static void coolDown(TickEvent.PlayerTickEvent event) {

    }

}
