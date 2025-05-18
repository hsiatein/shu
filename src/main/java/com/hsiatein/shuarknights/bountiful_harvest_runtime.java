package com.hsiatein.shuarknights;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

public class bountiful_harvest_runtime {
    private static final ArrayDeque<BlockPos> openList = new ArrayDeque<>();
    private static final HashMap<BlockPos, Integer> explored = new HashMap<>();
    private static final HashMap<BlockPos, BlockPos> unionFindSet = new HashMap<>();

    public static void push(BlockPos pos){
        openList.addLast(pos);
    }

    public static BlockPos pop(){
        return openList.pollFirst();
    }
    public static void clear(){
        openList.clear();
        explored.clear();
        unionFindSet.clear();
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
