package com.hsiatein.shuarknights;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.hsiatein.shuarknights.utils.canTransmit;
import static com.hsiatein.shuarknights.utils.isValidSuccessor;

public class bountiful_harvest_runtime {
    private static final ArrayDeque<BlockPos> openList = new ArrayDeque<>();
    private static final HashMap<BlockPos, operate> explored = new HashMap<>();
    // private static final HashMap<BlockPos, BlockPos> unionFindSet = new HashMap<>();
    private static final HashSet<BlockPos> operated = new HashSet<>();

    public static void push(BlockPos pos){
        openList.addLast(pos);
    }

    public static BlockPos pop(){
        return openList.pollFirst();
    }
    public static void clear(){
        openList.clear();
        explored.clear();
        operated.clear();
        // unionFindSet.clear();
    }
    public static boolean isEmpty(){
        return openList.isEmpty();
    }
    public static operate exploredOperate(BlockPos pos){
        if(explored.get(pos)==null) return operate.no;
        if(operated.contains(pos)) return operate.operated;
        else return explored.get(pos);
    }

    public static Set<BlockPos> getExploredSet(){
        return explored.keySet();
    }

    public static void markOperated(BlockPos pos){
        operated.add(pos);
        explored.remove(pos);
    }


    public static ArrayDeque<BlockPos> getNeighbors(@NotNull Level world, BlockPos pos){
        ArrayDeque<BlockPos> result = new ArrayDeque<>();
        ArrayDeque<BlockPos> neighbors = utils.getNeighbors26(pos);
        for(BlockPos neighbor:neighbors){
            if(isValidSuccessor(world,neighbor)) result.addLast(neighbor);
        }
        return result;
    }

    public static void expandPos(@NotNull Level world, BlockPos pos){
        if(!canTransmit(world,pos) || canTransmit(world,pos.above())) explored.put(pos,operate.explored);
        else if(utils.isBound(world,pos)) explored.put(pos,operate.wood);
        else if(utils.isValidFarmland(world,pos)) explored.put(pos,operate.farmland);
        else explored.put(pos,operate.explored);

    }

    public enum operate{
        water,
        farmland,
        wood,
        no,
        explored,
        operated,
    }


}
