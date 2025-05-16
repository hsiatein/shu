package com.hsiatein.shuarknights.network;

import com.hsiatein.shuarknights.utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwingMsg {
    public SwingMsg(){

    }

    public static SwingMsg decoder(FriendlyByteBuf buf){
        return new SwingMsg();
    }

    public static void encoder(SwingMsg swingMsg, FriendlyByteBuf buf){

    }

    public static void handler(SwingMsg swingMsg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> handleSwingMsg(swingMsg,supplier)));

        context.setPacketHandled(true);
    }


    public static void handleSwingMsg(SwingMsg message, Supplier<NetworkEvent.Context> supplier) {
        if (supplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            // utils.Logger.log("handle");
            Player player = Minecraft.getInstance().player;
            // player.sendSystemMessage(Component.nullToEmpty("handle"));
            if (player != null) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

}

