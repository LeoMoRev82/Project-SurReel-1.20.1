package com.leomorev.surreel.block.mineshaft_builder;

import com.leomorev.surreel.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MineshaftBuilderMenu extends AbstractContainerMenu{
	
	public MineshaftBuilderBlockEntity blockEntity;
	private final ContainerData data;
	public Level level;

    public MineshaftBuilderMenu(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (MineshaftBuilderBlockEntity) inv.player.level().getBlockEntity(data.readBlockPos()), new SimpleContainerData(4));
    }

    //public MineshaftBuilderMenu(int id, Inventory inv, Player player, MineshaftBuilderBlockEntity entity){
    //   super(SurReelMain.ObjectRegistry.MINESHAFT_BUILDER_MENU.get(), id);

    //


	
	public MineshaftBuilderMenu(int id, Inventory inv, MineshaftBuilderBlockEntity entity, ContainerData data) {
		super(Objects.MINESHAFT_BUILDER_MENU.get(), id);
		checkContainerSize(inv, 19);
		
		this.blockEntity = entity;
		this.level = inv.player.level();
		this.data = data;
		
		addPlayerInventory(inv);
		addPlayerHotbar(inv);
		
		this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Fuel Slot
            this.addSlot(new ResultSlot(handler, 18, 26, 37));

            // Tool Slots

            //Storage Slots
            this.addSlot(new SlotItemHandler(handler, 17, 62, 19));
            this.addSlot(new SlotItemHandler(handler, 16, 80, 19));
            this.addSlot(new SlotItemHandler(handler, 15, 98, 19));
            this.addSlot(new SlotItemHandler(handler, 14, 116, 19));
            this.addSlot(new SlotItemHandler(handler, 13, 134, 19));
            this.addSlot(new SlotItemHandler(handler, 12, 152, 19));
            
            this.addSlot(new SlotItemHandler(handler, 11, 62, 37));
            this.addSlot(new SlotItemHandler(handler, 10, 80, 37));
            this.addSlot(new SlotItemHandler(handler, 9, 98, 37));
            this.addSlot(new SlotItemHandler(handler, 8, 116, 37));
            this.addSlot(new SlotItemHandler(handler, 7, 134, 37));
            this.addSlot(new SlotItemHandler(handler, 6, 152, 37));
            
            this.addSlot(new SlotItemHandler(handler, 5, 62, 55));
            this.addSlot(new SlotItemHandler(handler, 4, 80, 55));
            this.addSlot(new SlotItemHandler(handler, 3, 98, 55));
            this.addSlot(new SlotItemHandler(handler, 2, 116, 55));
            this.addSlot(new SlotItemHandler(handler, 1, 134, 55));
            this.addSlot(new SlotItemHandler(handler, 0, 152, 55));
        });
		this.addDataSlots(data);
	}

    public boolean isLit() {
        return data.get(0) > 0;
    }
    
    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
           i = 200;
        }
        return this.data.get(0) * 13 / i;
    }

    public float getCompletionPercent(){
        return this.data.get(3);
    }
    
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
    	Slot slot = slots.get(index);
  	  	ItemStack copyItem = ItemStack.EMPTY;
  	  	ItemStack item = slot.getItem();
	      
  	  	if(slot.hasItem()) {
  	  		copyItem = item.copy();
	        if (index < 36) {
	        	if (!moveItemStackTo(item, 36, 36 + 19, false)) {return ItemStack.EMPTY;}
	        }	
	        else if (!moveItemStackTo(item, 0, 36, false)) {return ItemStack.EMPTY;}

	        if (item.isEmpty()) {slot.set(ItemStack.EMPTY);}
	        else {slot.setChanged();}
	    }

	    slot.onTake(player, item);
	    return copyItem;
	}
	

	@Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, Objects.MINESHAFT_BUILDER.get());
    }
	
	private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }
    
    public static class ResultSlot extends SlotItemHandler {
        public ResultSlot(IItemHandler itemHandler, int index, int x, int y) {
            super(itemHandler, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return isFuel(stack);
        }
    }

    private static boolean isFuel(ItemStack itemStack) {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack, null) > 0;
    }

}
