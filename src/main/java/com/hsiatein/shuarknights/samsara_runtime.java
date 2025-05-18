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

public class samsara_runtime {
    private static final ArrayDeque<BlockPos> openList = new ArrayDeque<>();
    private static final HashMap<BlockPos, Integer> explored = new HashMap<>();
    private static final HashMap<LivingEntity, Integer> creatureMap = new HashMap<>();
    private static final HashMap<LivingEntity, Vec3> enemyMap = new HashMap<>();
    // private static final ArrayDeque<BlockPos> neighborsList = new ArrayDeque<>();
    public static void push(BlockPos pos){
        openList.addLast(pos);
    }
    public static void pushCreature(Level world, BlockPos pos){
        var entities=utils.getAllCreatures(world,pos);
        for(var e:entities){
            if(!creatureMap.containsKey(e)) creatureMap.put(e,0);
        }
    }
    public static void pushEnemy(Level world, BlockPos pos){
        var entities=utils.getAllEnemy(world,pos);
        for(var e:entities){
            if(!enemyMap.containsKey(e)) enemyMap.put(e,e.position());
        }
    }
    public static void transmitEnemy(){
        var enemies=new HashSet<>(enemyMap.keySet());
        for(var e:enemies){
            if(!e.isAlive()){
                enemyMap.remove(e);
                continue;
            }
            double manhattan = Math.abs(e.position().x-enemyMap.get(e).x)+Math.abs(e.position().z-enemyMap.get(e).z);
            if(manhattan > 3){
                e.setPos(enemyMap.get(e));
            }
        }
    }
    public static void addEffect(MobEffectInstance mobEffectInstance){
        var entities=new HashSet<>(creatureMap.keySet());
        for(LivingEntity e:entities){
            if(!e.isAlive()){
                creatureMap.remove(e);
                continue;
            }
            e.addEffect(mobEffectInstance);
            creatureMap.put(e,creatureMap.get(e)+1);
        }
    }
    public static BlockPos pop(){
        return openList.pollFirst();
    }
    public static void clear(){
        openList.clear();
        explored.clear();
        creatureMap.clear();
        enemyMap.clear();
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
    public static ArrayDeque<BlockPos> getNeighbors(@NotNull Level world, BlockPos pos){
        ArrayDeque<BlockPos> result = new ArrayDeque<>();
        ArrayDeque<BlockPos> neighbors = utils.getNeighbors26(pos);
        for(BlockPos neighbor:neighbors){
            if(utils.isValidSuccessor(world,neighbor) && samsara_runtime.exploredTimes(neighbor)< samsara_runtime.exploredTimes(pos)){
                result.addLast(neighbor);
                samsara_runtime.addExploredTimes(neighbor);
            }
        }
        return result;
    }
}
