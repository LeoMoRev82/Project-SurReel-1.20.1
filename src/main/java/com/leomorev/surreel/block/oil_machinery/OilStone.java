package com.leomorev.surreel.block.oil_machinery;

import com.leomorev.surreel.block.entity.OilStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OilStone extends BaseEntityBlock {
    public OilStone() {
        super(Properties.of().sound(SoundType.METAL));
    }



    // BLOCK ENTITY AND MENUS

    //public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand){}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilStoneBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.MODEL;
    }
}
