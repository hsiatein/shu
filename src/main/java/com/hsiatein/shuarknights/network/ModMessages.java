package com.hsiatein.shuarknights.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import com.hsiatein.shuarknights.shuarknights;

import java.util.Optional;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id(){
        return packetId++;
    }

    public static void register(){
        INSTANCE = NetworkRegistry.newSimpleChannel(fromNamespaceAndPath(shuarknights.MODID,"messages"), () -> "1.0", s -> true,s -> true);
//        INSTANCE = NetworkRegistry.ChannelBuilder
//                .named(ResourceLocation.fromNamespaceAndPath(shuarknights.MODID,"messages"))
//                .networkProtocolVersion(()->"1.0")
//                .clientAcceptedVersions(s -> true)
//                .serverAcceptedVersions(s -> true)
//                .simpleChannel();
//        INSTANCE.registerMessage(packetId, SwingMsg.class, SwingMsg::encoder, SwingMsg::decoder, SwingMsg::handler, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        packetId++;
        INSTANCE.messageBuilder(SwingMsg.class,id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SwingMsg::decoder)
                .encoder(SwingMsg::encoder)
                .consumerMainThread(SwingMsg::handler)
                .add();
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(()-> player),message);
        // INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}