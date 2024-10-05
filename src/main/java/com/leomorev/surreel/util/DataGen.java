package com.leomorev.surreel.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGen {
    public static void register(GatherDataEvent dataEvent){
        DataGenerator gen = dataEvent.getGenerator();
        PackOutput pack = gen.getPackOutput();
        ExistingFileHelper helper = dataEvent.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> cf = dataEvent.getLookupProvider();

        gen.addProvider(true, new Tags(pack, cf, helper));
    }
}
