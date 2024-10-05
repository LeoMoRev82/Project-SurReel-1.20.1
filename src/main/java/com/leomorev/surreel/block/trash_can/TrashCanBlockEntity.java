package com.leomorev.surreel.block.trash_can;

import com.leomorev.surreel.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class TrashCanBlockEntity extends BlockEntity{
    public TrashCanBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        super(Objects.TRASH_CAN_BLOCK_ENTITY.get(), pos, state);
    }

    public LazyOptional<IItemHandler> trashHandler;
    private final TrashHandler TRASH = new TrashHandler();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir){
        if(cap == ForgeCapabilities.ITEM_HANDLER){return this.trashHandler.cast();}
        return super.getCapability(cap, dir);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.trashHandler.invalidate();
    }

    @Override
    public void onLoad(){
        super.onLoad();
        this.trashHandler = LazyOptional.of(() -> TRASH);
    }

    private static class TrashHandler extends ItemStackHandler{
        public TrashHandler() {
            stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate){
            return ItemStack.EMPTY;
        }
    }
}
