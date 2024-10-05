package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;

public class ReTickerModule extends ModuleBlock implements QuarryModule{
    @Override
    public ResourceLocation moduleName() {
        return Constant.RE_TICKER.moduleName();
    }
}
