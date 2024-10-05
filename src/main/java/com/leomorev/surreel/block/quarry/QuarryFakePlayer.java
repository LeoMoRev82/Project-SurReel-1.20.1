package com.leomorev.surreel.block.quarry;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class QuarryFakePlayer {
    private static final GameProfile profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "THE_VEIN_MAN");
    public static FakePlayer get(ServerLevel serverLevel) {
        return FakePlayerFactory.get(serverLevel, profile);
    }
}
