package com.hsiatein.shuarknights;

import com.hsiatein.shuarknights.runtime.samsara_runtime;
import com.hsiatein.shuarknights.runtime.bountiful_harvest_runtime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.lang.Math;
import java.util.stream.Collectors;

import static com.hsiatein.shuarknights.hud.samsara.MAX_EXPAND_TIMES;

public class utils {
    public static Block[] ALL_FLOWERS={};
    public static Block[] ALL_SAPLINGS={};
    public static Random random = new Random();


    public static ArrayDeque<BlockPos> getNeighbors(@NotNull Level world, BlockPos pos){
        ArrayDeque<BlockPos> result = new ArrayDeque<>();
        ArrayDeque<BlockPos> neighbors = utils.getNeighbors26(pos);
        for(BlockPos neighbor:neighbors){
            if(utils.isValidSuccessor(world,neighbor)){
                result.addLast(neighbor);
            }
        }
        return result;
    }

    public static void performOnEveryPos(Level world, BlockPos pos){
        utils.refineBlock(world,pos);
        samsara_runtime.pushCreature(world,pos);
        samsara_runtime.pushEnemy(world,pos);
        samsara_runtime.push(pos);
    }

    public static void refineBlocks(Level world,BlockPos startPos){
        while (!canTransmit(world,startPos) && startPos.getY()>-64){
            startPos=startPos.below();
        }
        ArrayDeque<BlockPos> openList = new ArrayDeque<>();
        refineBlock(world,startPos);
        HashSet<LivingEntity> creatureSet = new HashSet<>(getAllCreatures(world, startPos));
        HashSet<BlockPos> closeList = new HashSet<>();
        openList.addLast(startPos);

        int i = 0;
        while(i<MAX_EXPAND_TIMES && !openList.isEmpty()){
            // Logger.log("expand:"+i);
            BlockPos u= openList.pop();
            closeList.add(u);
            ArrayDeque<BlockPos> neighbors = getNeighbors(world, u);
            Logger.log(String.valueOf(neighbors.size()));
            for(BlockPos v:neighbors){
                if(closeList.contains(v)) continue;
                // Logger.log("refineBlock:");
                refineBlock(world,v);
                // Logger.log("getAllCreatures:");
                var blockCreatures=getAllCreatures(world, v);
                // Logger.log("addCreatures:");
                creatureSet.addAll(blockCreatures);
                // Logger.log("creatureSet:");
                // Logger.log(String.valueOf(creatureSet.size()));
                // Logger.log(creatureSet.toString());
                openList.addLast(v);
                // Logger.log("openList:");
                // Logger.log(String.valueOf(openList.size()));
                // Logger.log(openList.toString());
            }
            i++;
        }

        for(var e:creatureSet){
            if(!e.isAlive()) continue;
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20*10, 0));
            e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*10, 0));
        }

    }
    public static void addEffect(MobEffectInstance mobEffectInstance,Level world,BlockPos pos){
        var entities=utils.getAllCreatures(world,pos);
        for(LivingEntity e:entities){
            if(!e.isAlive()) continue;
            e.addEffect(mobEffectInstance);
        }
    }

    public static List<LivingEntity> getAllCreatures(@NotNull Level world, BlockPos pos){
        // 获取方块上方的坐标
        BlockPos aboveBlockPos = pos.above();
        List<LivingEntity> result =new ArrayList<>();
        if(!world.getBlockState(aboveBlockPos).isAir()) return result;
        result=world.getEntitiesOfClass(LivingEntity.class,
                        new AABB(aboveBlockPos).inflate(1.0D, 2.0D, 1.0D))
                .stream()
                .filter(entity -> entity.getClassification(false) == MobCategory.CREATURE || entity instanceof Player)
                .collect(Collectors.toList());

        return result;
    }

    public static List<LivingEntity> getAllCreatures(@NotNull Level world,Player player, double range) {
        List<LivingEntity> result=world.getEntitiesOfClass(LivingEntity.class,
                        new AABB(player.blockPosition()).inflate(range, range, range))
                .stream()
                .filter(entity -> entity.getClassification(false) == MobCategory.CREATURE || entity instanceof Player)
                .collect(Collectors.toList());

        return result;
    }

    public static List<LivingEntity> getAllEnemy(@NotNull Level world, BlockPos pos){
        // 获取方块上方的坐标
        BlockPos aboveBlockPos = pos.above();
        List<LivingEntity> result =new ArrayList<>();
        if(!world.getBlockState(aboveBlockPos).isAir()) return result;
        result=world.getEntitiesOfClass(LivingEntity.class,
                        new AABB(aboveBlockPos).inflate(1.0D, 2.0D, 1.0D))
                .stream()
                .filter(entity -> entity.getClassification(false) == MobCategory.MONSTER)
                .collect(Collectors.toList());

        return result;
    }

    public static void refineBlock(Level world, BlockPos pos) {
        BlockState currentState = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());

        if (currentState.is(Blocks.STONE) || currentState.is(Blocks.COBBLESTONE)) {
            world.setBlock(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
        } else if (currentState.is(Blocks.MOSSY_COBBLESTONE)) {
            world.setBlock(pos, Blocks.MOSS_BLOCK.defaultBlockState(), 3);
        } else if (currentState.is(Blocks.MOSS_BLOCK)) {
            if(aboveState.canBeReplaced()){
                world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
            else world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        } else if (currentState.is(Blocks.SAND) || currentState.is(Blocks.GRAVEL)) {
            world.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), 3);
        } else if (currentState.is(Blocks.COARSE_DIRT)) {
            world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        } else if (currentState.is(Blocks.DIRT)) {
            if(aboveState.getBlock()==Blocks.WATER){
                if(utils.random.nextInt(100) < 3){
                    world.setBlock(pos.above(), Blocks.SEAGRASS.defaultBlockState(), 3);
                    world.setBlock(pos.above(), Blocks.KELP.defaultBlockState(), 3);
                }
            }else if(aboveState.canBeReplaced()){
                world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }

        } else if (currentState.is(Blocks.GRASS_BLOCK)) {
            if(aboveState.isAir()){
                // 在上面长草或树苗或花
                if(utils.random.nextInt(100) < 1){
                    utils.plantRandomVegetation(world,pos);
                }
            }else if(aboveState.is(Blocks.GRASS) || aboveState.is(BlockTags.SAPLINGS)){
                utils.applyBoneMealEffect(world,pos.above());
            }


        } else if (currentState.is(Blocks.FARMLAND)) {
            if(aboveState.isAir()){
                // 替换为播种后的耕地
                world.setBlock(pos.above(), Blocks.WHEAT.defaultBlockState(), 3);
            }
            else if(aboveState.is(BlockTags.CROPS)){
                utils.applyBoneMealEffect(world,pos.above());
            }

        }
    }

    public static boolean discard(int refineTimes, int exploredTimes){
        if (refineTimes>6) return true;
        if(refineTimes == exploredTimes) return false;
        else if (refineTimes > exploredTimes) {
            double reserveProb=Math.pow(1.8,exploredTimes-refineTimes)*Math.pow(1.4,exploredTimes-1);
            return random.nextDouble()>reserveProb;
        }
        else return true;
    }

    public static ArrayDeque<Block> getBlocksByTag(TagKey<Block> blockTag) {
        ArrayDeque<Block> result = new ArrayDeque<>();
        var blocks=ForgeRegistries.BLOCKS.getValues();
        Logger.log(String.valueOf((blocks.size())));
        for(Block block:blocks){
            if(block.defaultBlockState().is(blockTag)) result.add(block);
        }
        return result;

    }

    public static void applyBoneMealEffect(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        BonemealableBlock bonemealableblock = (BonemealableBlock) blockState.getBlock();
//        if(blockState.getBlock()==Blocks.WHEAT){
//            world.players().get(0).sendSystemMessage(Component.nullToEmpty("Mugi"+pos.toString()));
//        }
        if (bonemealableblock.isValidBonemealTarget(world, pos, blockState, world.isClientSide)) {
            if (bonemealableblock.isBonemealSuccess(world, world.random, pos, blockState)) {
                bonemealableblock.performBonemeal((ServerLevel)world, world.random, pos, blockState);
            }
        }
    }
    public static boolean isCropMature(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // 检查是否是作物块
        if (state.getBlock() == Blocks.WHEAT) {
            return state.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7;
        } else if (state.getBlock() == Blocks.CARROTS) {
            return state.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7;
        } else if (state.getBlock() == Blocks.POTATOES) {
            return state.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7;
        } else if (state.getBlock() == Blocks.BEETROOTS) {
            return state.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 3; // 甜菜最大年龄为 3
        }

        return false; // 不是作物块或尚未成熟
    }
    public static void getAllFlowers() {
        if(ALL_FLOWERS.length>0) return;
        ArrayDeque<Block> allFlowers = getBlocksByTag(BlockTags.FLOWERS);
        ArrayDeque<Block> result = new ArrayDeque<>();
        for(Block flower:allFlowers){
            if(flower.defaultBlockState().is(Blocks.WITHER_ROSE)) continue;
            result.add(flower);
        }
        ALL_FLOWERS=result.toArray(new Block[0]);
    }
    public static void getAllSaplings() {
        if(ALL_SAPLINGS.length>0) return;
        ArrayDeque<Block> allSaplings = getBlocksByTag(BlockTags.SAPLINGS);
        ALL_SAPLINGS=allSaplings.toArray(new Block[0]);
    }

    public static void plantRandomVegetation(Level world, BlockPos pos) {
        BlockPos abovePos = pos.above(); // 获取草方块上方的位置
        Random random = new Random();

        int choice = random.nextInt(100); // 生成 0 到 2 之间的随机数

        if (choice <20) {
            // 种植草
            world.setBlock(abovePos, Blocks.GRASS.defaultBlockState(), 3);
        } else if (choice<50) {
            // 种植树苗（以橡树为例）
            plantRandomSaplings(world,pos);
            // world.setBlock(abovePos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
        } else {
            // 种植花（以红花为例）
            plantRandomFlower(world,pos);
            // world.setBlock(abovePos, Blocks.POPPY.defaultBlockState(), 3);
        }
    }

    public static void plantRandomFlower(Level world, BlockPos pos) {
        BlockPos abovePos = pos.above(); // 获取草方块上方的位置

        int choice = random.nextInt(ALL_FLOWERS.length); // 生成随机数

        world.setBlock(abovePos, ALL_FLOWERS[choice].defaultBlockState(), 3);
    }
    public static void plantRandomSaplings(Level world, BlockPos pos) {
        BlockPos abovePos = pos.above(); // 获取草方块上方的位置

        int choice = random.nextInt(ALL_SAPLINGS.length); // 生成随机数

        world.setBlock(abovePos, ALL_SAPLINGS[choice].defaultBlockState(), 3);
    }

    public static boolean canTransmit(@NotNull Level world,BlockPos pos){
        BlockState state = world.getBlockState(pos);
        return !state.canBeReplaced() && !state.is(BlockTags.CROPS) && !state.is(BlockTags.SAPLINGS);
    }
    public static boolean isValidSuccessor(@NotNull Level world,BlockPos pos){
        if(!canTransmit(world,pos)) return false;
        ArrayDeque<BlockPos> neighbors = getNeighbors6(pos);
        for(BlockPos neighbor:neighbors){
            if(!canTransmit(world,neighbor)) return true;
        }
        return false;
    }
    public static boolean isValidFarmland(@NotNull Level world,BlockPos pos){
        if(!world.getBlockState(pos).is(BlockTags.DIRT)) return false;
        if(world.getBlockState(pos.above()).is(BlockTags.CROPS)) return true;
        return !canTransmit(world, pos.above());
    }

    public static boolean isBound(@NotNull Level world,BlockPos pos){
        if(!canTransmit(world,pos)) throw new RuntimeException("这里是非实体方块");
        ArrayDeque<BlockPos> neighbors = getNeighbors4(pos);
        for(BlockPos neighbor:neighbors){
            if(!canTransmit(world,neighbor)) return true;
        }
        return false;
    }
    public static boolean isFarmlandBound(@NotNull Level world,BlockPos pos){
        if(!isBound(world, pos)) throw new RuntimeException("这里不是边界");
        ArrayDeque<BlockPos> neighbors = getNeighbors8(pos);
        for(BlockPos neighbor:neighbors){
            if(world.getBlockState(neighbor).is(BlockTags.DIRT) && !isBound(world,neighbor) && !canTransmit(world, neighbor.above())) return true;
        }
        return false;
    }

    public static ArrayDeque<BlockPos> getNeighbors6(BlockPos pos){
        ArrayDeque<BlockPos> neighbors = new ArrayDeque<>();
        neighbors.addLast(pos.above());
        neighbors.addLast(pos.below());
        neighbors.addLast(pos.west());
        neighbors.addLast(pos.east());
        neighbors.addLast(pos.north());
        neighbors.addLast(pos.south());
        return neighbors;
    }
    public static ArrayDeque<BlockPos> getNeighbors8(BlockPos pos){
        ArrayDeque<BlockPos> neighbors = new ArrayDeque<>();
        if(pos==null) return neighbors;
        int[] direction={0,-1,1};
        for(int i:direction)
            for(int j:direction)
                neighbors.addLast(new BlockPos(pos.getX()+i,pos.getY(),pos.getZ()+j));
        neighbors.pop();
        return neighbors;
    }

    public static ArrayDeque<BlockPos> getNeighbors4(BlockPos pos){
        ArrayDeque<BlockPos> neighbors = new ArrayDeque<>();
        neighbors.addLast(pos.west());
        neighbors.addLast(pos.east());
        neighbors.addLast(pos.north());
        neighbors.addLast(pos.south());
        return neighbors;
    }

    public static ArrayDeque<BlockPos> getNeighbors26(BlockPos pos){
        ArrayDeque<BlockPos> neighbors = new ArrayDeque<>();
        if(pos==null) return neighbors;
        int[] direction={0,-1,1};
        for(int i:direction)
            for(int j:direction)
                for(int k:direction){
                    neighbors.addLast(new BlockPos(pos.getX()+i,pos.getY()+j,pos.getZ()+k));
                }
        neighbors.pop();
        return neighbors;
    }


    public static class Logger {
        private static final File logFile = new File("logs/shuarknights.log");

        public static void log(String message) {
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile(); // 创建新文件
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(message);
                writer.newLine(); // 换行
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
