package com.hsiatein.shuarknights;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class utils {
    public static ArrayDeque<Block> ALL_FLOWERS;
    public static ArrayDeque<Block> ALL_SAPLINGS;
    public static ArrayDeque<Block> getBlocksByTag(TagKey<Block> blockTag) {
        ArrayDeque<Block> result = new ArrayDeque<>();
        for(Block block:ForgeRegistries.BLOCKS){
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
}
