package com.hsiatein.shuarknights;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Random;


public class utils {
    public static Block[] ALL_FLOWERS={};
    public static Block[] ALL_SAPLINGS={};
    public static Random random = new Random();

    public static ArrayDeque<Block> getBlocksByTag(TagKey<Block> blockTag) {
        ArrayDeque<Block> result = new ArrayDeque<>();
        var blocks=ForgeRegistries.BLOCKS.getValues();
        Logger.log(String.valueOf((blocks.size())));
        for(Block block:blocks){
//            if(blockTag==BlockTags.FLOWERS){
//                Logger.log(block.toString());
//
//            }
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
    public static void getAllFlowers() {
        if(ALL_FLOWERS.length>0) return;
        ArrayDeque<Block> allFlowers = getBlocksByTag(BlockTags.FLOWERS);
        ArrayDeque<Block> result = new ArrayDeque<>();
        for(Block flower:allFlowers){
            if(flower.defaultBlockState().is(Blocks.WITHER_ROSE)) continue;
            result.add(flower);
        }
        ALL_FLOWERS=allFlowers.toArray(new Block[0]);
    }
    public static void getAllSaplings() {
        if(ALL_SAPLINGS.length>0) return;
        ArrayDeque<Block> allSaplings = getBlocksByTag(BlockTags.SAPLINGS);
        ALL_SAPLINGS=allSaplings.toArray(new Block[0]);
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
