package com.leomorev.surreel.block.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MachineBuffer {
    public LazyOptional<IItemHandler> itemHandler;
    private final Predicate<ItemEntity> ITEM_ENTITY_PREDICATE = entity -> true;
    private final SimplerItemHandler BUFFER = new SimplerItemHandler(10);

    public SimplerItemHandler get(){
        return this.BUFFER;
    }

    public MachineBuffer() {
        setHandler();
    }

    public ItemStack add(int slot, ItemStack stack){
        return this.BUFFER.selfsert(slot, stack, false);
    }

    public ItemStack remove(int slot, int amount){
        return this.BUFFER.extractItem(slot, amount, false);
    }

    public void drops(Level level, BlockPos pos){
        SimpleContainer inventory = new SimpleContainer(this.getSlots());
        for(int i = 0; i < this.getSlots(); i++){
            inventory.setItem(i, this.getStackInSlot(i));
        }
        assert level != null;
        Containers.dropContents(level, pos, inventory);
    }

    public void tryEjectBuffer(Level level, BlockPos pos, int maxItemsPerTransfer) {
        BlockEntity blockEntity = level.getBlockEntity(pos.above());
        if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).isPresent()){
            ItemStack stack = ItemStack.EMPTY;
            int slot = -1;
            for(int i = 0; i < this.getSlots(); i++){
                if(this.getStackInSlot(i) != ItemStack.EMPTY){
                    stack = this.getStackInSlot(i).copy();
                    slot = i;
                    break;
                }
            }
            if(!stack.isEmpty() && slot != -1){
                if(maxItemsPerTransfer >= 64 || maxItemsPerTransfer <= 0){maxItemsPerTransfer = 4;}
                while (stack.getCount() < maxItemsPerTransfer) {
                    maxItemsPerTransfer--;
                }
                stack.setCount(maxItemsPerTransfer);
                int before = stack.getCount();
                stack = MachineBuffer.inject(stack, level.getBlockEntity(pos.relative(Direction.UP)), Direction.UP.getOpposite());
                int after = stack.getCount();
                this.remove(slot, (before - after));
            }
        }
    }

    public void collectAndStore(Level level, BlockPos storeTarget, BlockPos collectTarget) {
        AABB aabb = new AABB(collectTarget, collectTarget.offset(1, 2, 1));
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, aabb, ITEM_ENTITY_PREDICATE);
        ArrayList<ItemStack> itemArray = new ArrayList<>();
        for (ItemEntity item : itemEntities) {
            if (item.getItem().getCount() <= 0) continue;
            itemArray.add(item.getItem().copy());
            item.setItem(ItemStack.EMPTY);
            item.kill();
        }
        while(!itemArray.isEmpty()) {
            if(itemArray.get(0).isEmpty()) {
                itemArray.remove(0);
                continue;
            }
            ItemStack sendItem = itemArray.get(0);
            itemArray.remove(0);
            sendItem = inject(sendItem, level.getBlockEntity(storeTarget.relative(Direction.UP)), Direction.UP.getOpposite());
            for(int i = 0; i < this.getSlots() && !sendItem.isEmpty() && this.bufferPercentage() <= 50; i++){
                sendItem = this.add(i, sendItem);
            }
            if(!sendItem.isEmpty()) {
                RandomSource rand;
                rand = level.random;
                ItemEntity ei = new ItemEntity(level, (double) storeTarget.getX() + 0.5, storeTarget.getY() + 1, (double) storeTarget.getZ() + 0.5, sendItem.copy());
                ei.setDeltaMovement((rand.nextDouble() - rand.nextDouble()) * 0.045, 0.25, (rand.nextDouble() - rand.nextDouble()) * 0.045);
                level.addFreshEntity(ei);
            }
        }
    }

    public int bufferPercentage(){
        int slotsUsed = 0;
        int maxSlots = this.BUFFER.getSlots();
        for(int i = 0; i < maxSlots; i++){
            ItemStack stack = this.BUFFER.getStackInSlot(i);
            if(!stack.isEmpty()){
                slotsUsed++;
            }
        }
        return (int)(((float)slotsUsed / (float)maxSlots) * 100);
    }

    public ItemStack getStackInSlot(int slot){
        return this.BUFFER.getStackInSlot(slot);
    }

    public int getSlots(){
        return this.BUFFER.getSlots();
    }

    public void setHandler() {
        itemHandler = LazyOptional.of(() -> BUFFER);
    }

    public static ItemStack inject(ItemStack itemStack, BlockEntity tile, Direction direction) {
        return tile != null ? tile.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).map(n -> inject(itemStack, n)).orElse(itemStack) : itemStack;
    }

    public static ItemStack inject(ItemStack itemStack, IItemHandler itemHandler) {
        for(int i = 0; itemHandler != null && i < itemHandler.getSlots() && !itemStack.isEmpty(); ++i)
            itemStack = itemHandler.insertItem(i, itemStack, false);
        return itemStack;
    }

    public interface HasStorage {
        MachineBuffer getStorage();
    }
}
