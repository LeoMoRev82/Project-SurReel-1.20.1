package com.leomorev.surreel.block.transport_pipe;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PipeStackHandler extends ItemStackHandler implements INBTSerializable<CompoundTag> {
    protected NonNullList<PipeStack> pipeStacks;

    public PipeStackHandler(int slot){
        this.pipeStacks = NonNullList.withSize(slot, PipeStack.EMPTY);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        CompoundTag nbt = new CompoundTag();
        for (int i = 0; i < pipeStacks.size(); i++) {
            if (!pipeStacks.get(i).getItemStack().isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("slot", i);
                itemTag.putFloat("position", pipeStacks.get(i).getPosition());
                itemTag.putFloat("prevPosition", pipeStacks.get(i).getPrevPosition());
                pipeStacks.get(i).getItemStack().save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        nbt.put("items", nbtTagList);
        nbt.putInt("size", pipeStacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("size", Tag.TAG_INT) ? nbt.getInt("size") : pipeStacks.size());
        ListTag tagList = nbt.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("slot");
            float position = itemTags.getFloat("position");
            float prevPosition = itemTags.getFloat("prevPosition");
            if (slot >= 0 && slot < pipeStacks.size()) {
                PipeStack pipestack = new PipeStack(ItemStack.of(itemTags));
                pipestack.setPosition(position);
                pipestack.setPrevPosition(prevPosition);
                pipeStacks.set(slot, pipestack);
            }
        }
        onLoad();
    }

    public void setPipeStackInSlot(int slot, PipeStack pipeStack){
        this.pipeStacks.set(slot, pipeStack);
        onContentsChanged(slot);
    }

    public PipeStack getPipeStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.pipeStacks.get(slot);
    }

    @Override
    public void setSize(int size) {
        pipeStacks = NonNullList.withSize(size, PipeStack.EMPTY);
    }

    @Override
    public int getSlots() {
        return pipeStacks.size();
    }

    public PipeStack insert(int slot, PipeStack pipeStack, boolean simulate){
        var itemStack= pipeStack.getItemStack();
        itemStack = this.insertItem(slot, itemStack, simulate);
        pipeStack.setItemStack(itemStack);
        return pipeStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!isItemValid(slot, stack))
            return stack;
        validateSlotIndex(slot);
        ItemStack existing = this.pipeStacks.get(slot).getItemStack();
        int limit = getStackLimit(slot, stack);
        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0)
            return stack;
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                PipeStack pipeStack = new PipeStack(reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                this.pipeStacks.set(slot, pipeStack);
            }
            else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;
        validateSlotIndex(slot);
        ItemStack existing = this.pipeStacks.get(slot).getItemStack();
        if (existing.isEmpty())
            return ItemStack.EMPTY;
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.pipeStacks.set(slot, PipeStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            else {
                return existing.copy();
            }
        }
        else {
            if (!simulate) {
                this.pipeStacks.get(slot).setItemStack(ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.pipeStacks.get(slot).getItemStack();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        pipeStacks.set(slot, new PipeStack(stack));
        onContentsChanged(slot);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= pipeStacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + pipeStacks.size() + ")");
    }
}
