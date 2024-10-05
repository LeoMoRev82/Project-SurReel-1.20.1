package com.leomorev.surreel.util;

import com.leomorev.surreel.SurReelMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Networking {
    private static SimpleChannel INSTANCE;
    private static int packetID = 0;
    private static int id(){
        return packetID++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(SurReelMain.MODID, "networks")).networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(c -> true).serverAcceptedVersions(s -> true).simpleChannel();
        INSTANCE = net;
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, Level level){
        INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
