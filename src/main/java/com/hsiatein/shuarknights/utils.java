package com.hsiatein.shuarknights;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class utils {
    public static ArrayDeque<Block> ALL_FLOWERS;
    public static ArrayDeque<Block> ALL_SAPLINGS;
    public ArrayDeque<Block> getBlocksByTag(TagKey<Block> blockTag) {
        ArrayDeque<Block> result = new ArrayDeque<>();
        for(Block block:ForgeRegistries.BLOCKS){
            if(block.defaultBlockState().is(blockTag)) result.add(block);
        }
        return result;

    }

}
