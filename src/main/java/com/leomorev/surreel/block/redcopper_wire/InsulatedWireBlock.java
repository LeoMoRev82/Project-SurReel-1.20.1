package com.leomorev.surreel.block.redcopper_wire;

import net.minecraft.world.item.DyeColor;

public class InsulatedWireBlock extends RedCopperWireBlock {
    private final DyeColor color;

    public InsulatedWireBlock(DyeColor dyeColor){
        this.color = dyeColor;
    }

    public DyeColor getColor(){
        return this.color;
    }
}
