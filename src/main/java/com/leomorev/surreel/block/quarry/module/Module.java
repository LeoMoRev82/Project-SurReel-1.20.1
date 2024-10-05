package com.leomorev.surreel.block.quarry.module;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Module(ResourceLocation name, BlockPos pos) {

    public static Tag toNBTList(List<Module> modules) {
        var tagList = new ListTag();
        for(int i = 0; i < modules.size(); i++){
            var nbt = new CompoundTag();
            nbt.put("module." + i, modules.get(i).toNBT());
            tagList.add(nbt);
        }
        return tagList;
    }

    public static List<Module> fromNBTList(String compound, CompoundTag tag) {
        var tagList = tag.getList(compound, ListTag.TAG_COMPOUND);
        List<Module> list = new ArrayList<>(Collections.emptyList());
        for(int i = 0; i < tagList.size(); i++){
            var nbt = tagList.getCompound(i);
            int finalI = i;
            fromNBT(nbt.getCompound("module." + i)).ifPresent(n -> list.add(finalI, n));
        }
        return list;
    }

    public CompoundTag toNBT() {
        var tag = new CompoundTag();
        tag.putInt("posX", pos.getX());
        tag.putInt("posY", pos.getY());
        tag.putInt("posZ", pos.getZ());
        tag.putString("moduleName", name.toString());
        return tag;
    }

    public static Optional<Module> fromNBT(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        } else {
            //System.out.println(tag.getString("moduleName").replace(SurReelMain.MODID + ":", ""));
            return Optional.of(
                    new Module(
                    new ResourceLocation(
                            tag.getString("moduleName")),
                    new BlockPos(
                            tag.getInt("posX"),
                            tag.getInt("posY"),
                            tag.getInt("posZ"))
                    )
            );
        }
    }
}
