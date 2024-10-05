package com.leomorev.surreel.block.quarry.module;

import com.leomorev.surreel.SurReelMain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface QuarryModule {
    ResourceLocation moduleName();

    default List<ResourceLocation> incompatibleWith() {
        return new ArrayList<>();
    }

    default boolean isStackable() {
        return true;
    }

    default void setStatus(Level level, BlockPos pos, BlockState state, boolean set){
        level.setBlock(pos, state.setValue(ModuleBlock.ACTIVE, set), 3);
    }

    enum Constant implements QuarryModule {
        FORTUNE     (new ResourceLocation(SurReelMain.MODID, "fortune_module")),
        EFFICIENCY  (new ResourceLocation(SurReelMain.MODID, "efficiency_module")),
        SILK_TOUCH  (new ResourceLocation(SurReelMain.MODID, "silk_touch_module")),
        PUMP        (new ResourceLocation(SurReelMain.MODID, "pump_module")),
        FREEZER     (new ResourceLocation(SurReelMain.MODID, "freezer_module")),
        RE_TICKER   (new ResourceLocation(SurReelMain.MODID, "re_ticker_module"))
        ;

        private final ResourceLocation moduleName;
        Constant(ResourceLocation moduleName) {
            this.moduleName = moduleName;
        }

        @Override
        public ResourceLocation moduleName() {
            return moduleName;
        }
    }
}
