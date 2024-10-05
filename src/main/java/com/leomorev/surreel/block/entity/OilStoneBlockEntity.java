package com.leomorev.surreel.block.entity;

import com.leomorev.surreel.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class OilStoneBlockEntity extends BlockEntity {
    public OilStoneBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.OIL_SOAKED_STONE_BLOCK_ENTITY.get(), pos, state);
    }

    private int oilCap = 10;

    public int getOil(){
        return this.oilCap;
    }

    public void setOil(int setNew){
        this.oilCap = setNew;
    }

    @Override
    protected void saveAdditional(CompoundTag tag){
        tag.putInt("mineshaft_builder.oilCap", this.oilCap);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag){
        super.load(tag);
        this.oilCap = tag.getInt("mineshaft_builder.oilCap");
    }
}
