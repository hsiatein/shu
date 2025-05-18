package com.hsiatein.shuarknights;

import com.hsiatein.shuarknights.hud.verdant_wisdom;
import com.hsiatein.shuarknights.hud.bountiful_harvest;
import com.hsiatein.shuarknights.hud.samsara;
import com.hsiatein.shuarknights.item.yucong;
import com.hsiatein.shuarknights.network.ModMessages;
import com.hsiatein.shuarknights.network.SwingMsg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;

import static com.hsiatein.shuarknights.hud.samsara.*;
import static org.apache.commons.lang3.math.NumberUtils.min;

@Mod.EventBusSubscriber(modid = shuarknights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void verdant_wisdom(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.CLIENT) return;
        if(event.phase == TickEvent.Phase.START) return;
        Player entity = event.player;
        if (verdant_wisdom.CHARGES<1 || entity.getHealth() > entity.getMaxHealth() * 0.5 || !entity.isAlive()) return;
        // 确保是玩家
        ServerPlayer player= (ServerPlayer) entity;
        ItemStack heldItem = player.getMainHandItem();
        // 检查是否持有该物品
        if (heldItem.getItem() instanceof yucong) {
            // 检查血量是否低于50%
            ModMessages.sendToPlayer(new SwingMsg(), player);
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            utils.refineBlocks(player.level(),player.blockPosition());
            verdant_wisdom.CHARGES--;
        }
    }
    @SubscribeEvent
    public static void samsara(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.CLIENT) return;
        if(event.phase == TickEvent.Phase.END) return;
        if(DURATION>=MAX_DURATION){
            samsara_runtime.clear();
            refineTimes=0;
            return;
        }
        if(world==null || startPos==null) return;

        if(DURATION%40== 0){
            refineTimes++;
            utils.performOnEveryPos(world,startPos);
            samsara_runtime.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2*MAX_DURATION- DURATION, 0));
            samsara_runtime.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2*MAX_DURATION- DURATION, 0));
            samsara_runtime.addExploredTimes(startPos);
        }
        int i = 0;

        while (i<MAX_EXPAND_TIMES && !samsara_runtime.isEmpty()){
            BlockPos u= samsara_runtime.pop();
            if(utils.discard(refineTimes, samsara_runtime.exploredTimes(u))) continue;
            ArrayDeque<BlockPos> neighbors = utils.getNeighbors(world, u);
            for(BlockPos v:neighbors){
                utils.performOnEveryPos(world,v);
            }
            i++;
        }
        samsara_runtime.transmitEnemy();
        DURATION++;
    }

    @SubscribeEvent
    public static void coolDown(TickEvent.PlayerTickEvent event) {
//        log(event.player.toString());
//        log(event.side.toString());
//        log(event.type.toString());
//        log(event.phase.toString());
//        log("");

        if(event.side == LogicalSide.CLIENT) return;
        if(event.phase == TickEvent.Phase.START) return;
        ItemStack heldItem = event.player.getMainHandItem();
        verdant_wisdom.recoverSP(heldItem);
        bountiful_harvest.recoverSP(heldItem);
        samsara.recoverSP(heldItem);
    }

    @SubscribeEvent
    public static void getAllNeededBlock(LevelEvent.Load event) {
        utils.getAllFlowers();
        utils.getAllSaplings();
        samsara.SP= samsara.INITIAL_SP;
        DURATION= MAX_DURATION;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        // 检查玩家是否首次进入世界
        if (!player.getPersistentData().getBoolean("hasReceivedYucong")) {
            // 给玩家一个玉虫（示例使用了金苹果）
            ItemStack yucong = shuarknights.YU_CONG.get().getDefaultInstance(); // 替换为玉虫的物品

            player.addItem(yucong);
            player.getPersistentData().putBoolean("hasReceivedYucong", true); // 标记为已给予
        }
    }

}
