package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class FreezerModule extends ModuleBlock implements QuarryModule{

    @Override
    public ResourceLocation moduleName() {
        return Constant.FREEZER.moduleName();
    }

    @Override
    public List<ResourceLocation> incompatibleWith() {
        return List.of(Constant.PUMP.moduleName());
    }

    @Override
    public boolean isStackable() {
        return false;
    }
}
