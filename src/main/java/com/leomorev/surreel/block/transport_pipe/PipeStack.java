package com.leomorev.surreel.block.transport_pipe;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class PipeStack {
    public static final PipeStack EMPTY = new PipeStack(ItemStack.EMPTY);
    private ItemStack itemStack = ItemStack.EMPTY;
    private Direction direction;
    private float position = 0;
    private float prevPosition = 0;

    public float test;

    //public float curPosX = 0;
    //public float curPosY = 0;
    //public float curPosZ = 0;
    //public float prePosX = 0;
    //public float prePosY = 0;
    //public float prePosZ = 0;

    public PipeStack(ItemStack stack) {
        this.itemStack = stack;
    }

    public PipeStack(ItemStack stack, Direction direction, float position) {

    }

    public void setItemStack(ItemStack stack){
        this.itemStack = stack;
    }

    public ItemStack getItemStack(){
        return this.itemStack;
    }

    public void setPosition(float position){
        this.position = position;
    }

    public float getPosition(){
        return this.position;
    }

    public void setPrevPosition(float prevPosition){
        this.prevPosition = prevPosition;
    }

    public float getPrevPosition(){
        return this.prevPosition;
    }

    public void setDirection(Direction direction){
        this.direction = direction;
    }

    public Direction getDirection(){
        return this.direction;
    }
}
