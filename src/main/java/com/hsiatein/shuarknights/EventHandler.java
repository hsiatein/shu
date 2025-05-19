package com.hsiatein.shuarknights;

import com.hsiatein.shuarknights.hud.verdant_wisdom;
import com.hsiatein.shuarknights.hud.bountiful_harvest;
import com.hsiatein.shuarknights.hud.samsara;
import com.hsiatein.shuarknights.item.yucong;
import com.hsiatein.shuarknights.network.ModMessages;
import com.hsiatein.shuarknights.network.SwingMsg;
import com.hsiatein.shuarknights.runtime.bountiful_harvest_runtime;
import com.hsiatein.shuarknights.runtime.samsara_runtime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.HashSet;

import static org.apache.commons.lang3.math.NumberUtils.min;

@Mod.EventBusSubscriber(modid = shuarknights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

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
        if(samsara.DURATION>=samsara.MAX_DURATION){
            samsara_runtime.clear();
            samsara.refineTimes=0;
            return;
        }
        if(samsara.world==null || samsara.startPos==null) return;

        if(samsara.DURATION%40== 0){
            samsara.refineTimes++;
            utils.performOnEveryPos(samsara.world,samsara.startPos);
            samsara_runtime.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2*samsara.MAX_DURATION- samsara.DURATION, 0));
            samsara_runtime.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2*samsara.MAX_DURATION- samsara.DURATION, 0));
            samsara_runtime.addExploredTimes(samsara.startPos);
        }
        int i = 0;

        while (i<samsara.MAX_EXPAND_TIMES && !samsara_runtime.isEmpty()){
            BlockPos u= samsara_runtime.pop();
            if(utils.discard(samsara.refineTimes, samsara_runtime.exploredTimes(u))) continue;
            ArrayDeque<BlockPos> neighbors = samsara_runtime.getNeighbors(samsara.world, u);
            for(BlockPos v:neighbors){
                utils.performOnEveryPos(samsara.world,v);
            }
            i++;
        }
        samsara_runtime.transmitEnemy();
        samsara.DURATION++;
    }

    @SubscribeEvent
    public static void bountiful_harvest(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.CLIENT) return;
        if(event.phase == TickEvent.Phase.END) return;
        if(bountiful_harvest.DURATION>=bountiful_harvest.MAX_DURATION){
            bountiful_harvest_runtime.clear();
            return;
        }
        if(bountiful_harvest.world==null || bountiful_harvest.startPos==null) return;

        if(bountiful_harvest.DURATION== 0){
            bountiful_harvest_runtime.push(bountiful_harvest.startPos);
        }

        int i = 0;
        while (i<bountiful_harvest.MAX_EXPAND_TIMES && !bountiful_harvest_runtime.isEmpty()){
            BlockPos u= bountiful_harvest_runtime.pop();
            ArrayDeque<BlockPos> neighbors = bountiful_harvest_runtime.getNeighbors(bountiful_harvest.world, u);
            for(BlockPos v:neighbors){
                if(bountiful_harvest_runtime.exploredOperate(v)!=bountiful_harvest_runtime.operate.no) continue;
                bountiful_harvest_runtime.expandPos(bountiful_harvest.world,v);
                bountiful_harvest_runtime.push(v);
            }
            i++;
        }
        if(bountiful_harvest.DURATION%5==0){
            var blocks = new HashSet<>(bountiful_harvest_runtime.getExploredSet());
            for(var block:blocks){
                if(bountiful_harvest_runtime.exploredOperate(block)==bountiful_harvest_runtime.operate.explored
                        || bountiful_harvest_runtime.exploredOperate(block)==bountiful_harvest_runtime.operate.operated){
                    bountiful_harvest_runtime.markOperated(block);
                    continue;
                }
                if(bountiful_harvest_runtime.exploredOperate(block)==bountiful_harvest_runtime.operate.wood){
                    if(utils.isFarmlandBound(bountiful_harvest.world,block)) bountiful_harvest.world.setBlock(block, Blocks.OAK_LOG.defaultBlockState(),3);
                    bountiful_harvest_runtime.markOperated(block);
                    continue;
                }
                if(bountiful_harvest_runtime.exploredOperate(block)==bountiful_harvest_runtime.operate.water){
                    bountiful_harvest.world.setBlock(block, Blocks.WATER.defaultBlockState(),3);
                    bountiful_harvest_runtime.markOperated(block);
                    continue;
                }
                if(bountiful_harvest_runtime.exploredOperate(block)==bountiful_harvest_runtime.operate.farmland){
                    var aboveState=bountiful_harvest.world.getBlockState(block.above());
                    if(!bountiful_harvest.world.getBlockState(block).is(Blocks.FARMLAND)) bountiful_harvest.world.setBlock(block, Blocks.FARMLAND.defaultBlockState(),3);
                    else if (!aboveState.is(BlockTags.CROPS)) bountiful_harvest.world.setBlock(block.above(), Blocks.WHEAT.defaultBlockState(),3);
                    else {
                        if (aboveState.getValue(net.minecraft.world.level.block.CropBlock.AGE)<7) utils.applyBoneMealEffect(bountiful_harvest.world,block.above());
                        if (utils.isCropMature(bountiful_harvest.world,block.above())) bountiful_harvest_runtime.markOperated(block);
                    }
                }
            }
        }
        bountiful_harvest.DURATION++;
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
        samsara.DURATION= samsara.MAX_DURATION;
        bountiful_harvest.SP= bountiful_harvest.INITIAL_SP;
        bountiful_harvest.DURATION= bountiful_harvest.MAX_DURATION;
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

    @SubscribeEvent
    public void onEntityUpdate(LivingDeathEvent event) {
        if(event.getEntity() instanceof Player player){
            for(var itemstack:player.getInventory().items){
                if(itemstack.is(shuarknights.YU_CONG.get())){
                    break;
                }
            }
        }

    }

}
