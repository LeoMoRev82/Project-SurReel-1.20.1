package com.leomorev.surreel.block.quarry.module;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.entity.MinerBuilderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FreezerModuleBlockEntity extends MinerBuilderBlockEntity {
    public FreezerModuleBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.FREEZER_MODULE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FreezerModuleBlockEntity be) {
        if(level == null || level.isClientSide) {return;}


    }
}
