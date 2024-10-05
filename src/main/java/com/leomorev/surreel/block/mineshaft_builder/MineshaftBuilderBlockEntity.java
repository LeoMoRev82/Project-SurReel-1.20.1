package com.leomorev.surreel.block.mineshaft_builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.entity.MinerBuilderBlockEntity;

import com.leomorev.surreel.block.quarry.QuarryArea;
import com.leomorev.surreel.block.quarry_marker.QuarryMarkerBlockEntity;
import com.leomorev.surreel.util.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MineshaftBuilderBlockEntity extends MinerBuilderBlockEntity implements MenuProvider {

	private final ItemStackHandler itemHandle = new ItemStackHandler(19) {
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};

	public MineshaftBuilderBlockEntity(BlockPos pos, BlockState state) {
		super(Objects.MINESHAFT_BUILDER_BLOCK_ENTITY.get(), pos, state);
		this.data = new ContainerData() {
			public int get(int index) {
				return switch (index) {
					case 0 -> MineshaftBuilderBlockEntity.this.burnTime;
					case 1 -> MineshaftBuilderBlockEntity.this.maxBurnTime;
					default -> 0;
				};
			}

			public void set(int index, int value) {
				switch (index) {
					case 0 -> MineshaftBuilderBlockEntity.this.burnTime = value;
					case 1 -> MineshaftBuilderBlockEntity.this.maxBurnTime = value;
				}
			}

			public int getCount() {
				return 4;
			}
		};
	}

	private static final Predicate<ItemEntity> ITEM_ENTITY_PREDICATE = entity -> true;
	private LazyOptional<IItemHandler> lazyItem = LazyOptional.empty();
	protected final ContainerData data;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
	
	private int burnTime = 0;
	private int maxBurnTime = 20;

	private int miningTick = 0;
	private final int miningSpeed = 3;

	private boolean startUp = true;
	private boolean hasMined = false;
	private boolean stage1 = false;
	private boolean stage2 = false;
	private boolean stage3 = false;
	private boolean stage4 = false;
	private boolean stage5 = false;
	private boolean stage6 = false;
	private boolean stage7 = false;

	private boolean placedBlock = false;

	private final ArrayList<BlockPos> blockPosArray 	= new ArrayList<>();
	private final ArrayList<BlockPos> wallFluidPosArray = new ArrayList<>();
	private final ArrayList<BlockPos> fluidPosArray 	= new ArrayList<>();
	private final ArrayList<BlockPos> supportPosArray 	= new ArrayList<>();
	private int supportPos = 0;
	private final ArrayList<BlockStateProperty> platformPosArray = new ArrayList<>();
	private int platformPos = 0;

	private final QuarryArea quarrySite = new QuarryArea(this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite()).relative(this.getBlockState().getValue(FACING).getOpposite().getCounterClockWise(), 3),
			this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite(), 7).relative(this.getBlockState().getValue(FACING).getOpposite().getClockWise(), 3).relative(Direction.UP, 4));

	private QuarryArea quarryDigArea = quarrySite.shrink(1, 0, 1).moveYAxis(-(quarrySite.maxY() - quarrySite.minY() + 1)).clampMinToMaxY();

	@Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
		return new MineshaftBuilderMenu(id, inventory, this, this.data);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.literal("Mineshaft Builder");
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side){
		if(cap == ForgeCapabilities.ITEM_HANDLER){return lazyItem.cast();}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void onLoad(){
		super.onLoad();
		lazyItem = LazyOptional.of(() -> itemHandle);
	}
	
	@Override
	public void invalidateCaps(){
		super.invalidateCaps();
		lazyItem.invalidate();
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag){
		tag.put("inventory", itemHandle.serializeNBT());
		tag.putInt("mineshaft_builder.burnTime", burnTime);
		tag.putInt("mineshaft_builder.maxBurn", maxBurnTime);
		tag.putInt("mineshaft_builder.supportPos", supportPos);
		tag.putInt("mineshaft_builder.platformPos", platformPos);
		tag.putBoolean("mineshaft_builder.hasMined", hasMined);
		tag.putBoolean("mineshaft_builder.stage1", stage1);
		tag.putBoolean("mineshaft_builder.stage2", stage2);
		tag.putBoolean("mineshaft_builder.stage3", stage3);
		tag.putBoolean("mineshaft_builder.stage4", stage4);
		tag.putBoolean("mineshaft_builder.stage5", stage5);
		tag.putBoolean("mineshaft_builder.stage6", stage6);
		tag.putBoolean("mineshaft_builder.stage7", stage7);
		super.saveAdditional(tag);
	}
	
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		itemHandle.deserializeNBT(tag.getCompound("inventory"));
		burnTime = tag.getInt("mineshaft_builder.burnTime");
		maxBurnTime = tag.getInt("mineshaft_builder.maxBurn");
		supportPos = tag.getInt("mineshaft_builder.supportPos");
		platformPos = tag.getInt("mineshaft_builder.platformPos");
		hasMined = tag.getBoolean("mineshaft_builder.hasMined");
		stage1 = tag.getBoolean("mineshaft_builder.stage1");
		stage2 = tag.getBoolean("mineshaft_builder.stage2");
		stage3 = tag.getBoolean("mineshaft_builder.stage3");
		stage4 = tag.getBoolean("mineshaft_builder.stage4");
		stage5 = tag.getBoolean("mineshaft_builder.stage5");
		stage6 = tag.getBoolean("mineshaft_builder.stage6");
		stage7 = tag.getBoolean("mineshaft_builder.stage7");
	}

	public void drops(){
		SimpleContainer inventory = new SimpleContainer(itemHandle.getSlots());
		for(int i = 0; i < itemHandle.getSlots(); i++){
			inventory.setItem(i, itemHandle.getStackInSlot(i));
		}
		assert this.level != null;
		Containers.dropContents(this.level, this.worldPosition, inventory);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MineshaftBuilderBlockEntity blockEntity) {
		if(level.isClientSide){return;}

		//if(blockEntity.ticks >= 20){
		//	blockEntity.ticks = 0;
		//	System.out.println(blockEntity.quarryDigArea);
		//	level.players().iterator().next().displayClientMessage(Component.literal(
		//			"%s%s%s".formatted(ChatFormatting.DARK_RED, blockEntity.quarryDigArea, ChatFormatting.RESET)
		//	), false);

		//	System.out.println(blockEntity.quarrySite);
		//	level.players().iterator().next().displayClientMessage(Component.literal(
		//			"%s%s%s".formatted(ChatFormatting.DARK_GREEN, blockEntity.quarrySite, ChatFormatting.RESET)
		//	), false);
		//}

		//level.players().iterator().next().sendSystemMessage(Component.literal(String.valueOf(blockEntity.scanPercent / 100)));
		//LevelChunk c = level.getChunkAt(blockEntity.worldPosition);

		//SimpleContainer inventorySlots = new SimpleContainer(blockEntity.itemHandle.getSlots());
		//for (int y = 0; y < blockEntity.itemHandle.getSlots(); y++) {
		//	inventorySlots.setItem(y, blockEntity.itemHandle.getStackInSlot(y));
		//}

		BlockEntity entity = level.getBlockEntity(pos.below());
		if(entity instanceof QuarryMarkerBlockEntity machine){
			if(blockEntity.miningTick == 0){
				QuarryArea quarryArea = machine.getArea();
				BlockPos first = machine.getParentPos();
				List<BlockPos> poss = machine.getMarkerPositions();
				boolean con = machine.isConnected();
				boolean parent = machine.isParent();

				//level.players().iterator().next().displayClientMessage(Component.literal(
				//		"%s%s%s".formatted(ChatFormatting.DARK_GREEN, size, ChatFormatting.RESET) +
				//		" %s%s%s".formatted(ChatFormatting.RED, con, ChatFormatting.RESET) +
				//		" %s%s%s".formatted(ChatFormatting.YELLOW, poss, ChatFormatting.RESET)
				//), false);
				System.out.println(quarryArea + " | " + con + " | " + poss + " | " + parent  + " | " + first);
			}
		}

		if(isFuel(blockEntity.itemHandle.getStackInSlot(18)) || blockEntity.isLit()){
			ItemStack fuel = blockEntity.itemHandle.getStackInSlot(18);
			if (blockEntity.isLit()) {
				--blockEntity.burnTime;
				setChanged(level, pos, state);}
			else{blockEntity.maxBurnTime = getBurnDuration(fuel); burn(fuel, blockEntity);}
		}

		/*
		DIG 1 LAYER AT A TIME
		STAGE 1 = Scan Dig-site and save to an arraylist
		Stage 2 = Scan blocks from starting y to current y and save to an arraylist
				  Scan Walls for Lava or water and save to an arraylist
				  Scan for water or lava in shaft and save in an arraylist
		Stage 3 = Fill in lava and water in the walls one block at a time based on the arraylist, and remove
		Stage 4 = Remove water or lava in shaft one block at a time
		Stage 5 = Mine blocks that are != to air blocks or bedrock one block at a time, and set block to air in arraylist
		Stage 6 = Scan for completion; if not completed then move to Stage 2, if completed move to Stage 7
		Stage 7 = Build Supports and Platforms with Scaffolds and Torches
		*/

		//STARTUP
		if(blockEntity.startUp) {
			blockEntity.startUp = false;
			blockEntity.stage1 = true;
		}

		//STAGE 1
		if(blockEntity.stage1 && blockEntity.isLit()){

			blockEntity.blockPosArray.addAll(blockEntity.scanBlocks(blockEntity.quarrySite));

			blockEntity.stage1 = false;
			blockEntity.stage2 = true;
		}

		//STAGE 2
		if(blockEntity.stage2 && blockEntity.isLit()) {

			blockEntity.blockPosArray.addAll(blockEntity.scanBlocks(blockEntity.quarryDigArea));

			//scanFluids(level, pos, blockEntity.fluidPosArray, blockEntity.quarrySite, blockEntity.workingY);
			//scanWallsForFluids(level, pos, blockEntity.wallFluidPosArray, blockEntity.facingOffsetX - 1, blockEntity.facingOffsetZ - 1, 7, 7, blockEntity.workingY);

			blockEntity.stage2 = false;
			blockEntity.stage3 = true;
		}

		//STAGE 3
		if(blockEntity.stage3 && blockEntity.isLit()) {
			if(blockEntity.wallFluidPosArray.isEmpty()) {
				blockEntity.stage3 = false;
				blockEntity.stage4 = true;
			} else if(blockEntity.miningTick == blockEntity.miningSpeed){
				try {
					if(hasValidBlocks(blockEntity.itemHandle, -1, Tags.MINESHAFT_WALL_FILL)){
						int slotIndex = getValidSlot(blockEntity.itemHandle, -1, Tags.MINESHAFT_WALL_FILL);
						if(canPlace(level, blockEntity.wallFluidPosArray.get(0), true, false)){
							replaceBlock(level, blockEntity.wallFluidPosArray.get(0), ((BlockItem) blockEntity.itemHandle.getStackInSlot(slotIndex).getItem()).getBlock().defaultBlockState());
							blockEntity.itemHandle.getStackInSlot(slotIndex).shrink(1);
						}
						blockEntity.placedBlock = true;
						blockEntity.wallFluidPosArray.remove(0);
					}
				}
				catch(Exception ignored) {}
			}
		}

		//STAGE 4
		if(blockEntity.stage4 && blockEntity.isLit()) {
			if(blockEntity.fluidPosArray.isEmpty()) {
				blockEntity.stage4 = false;
				blockEntity.stage5 = true;
			} else {
				try {
					replaceBlock(level, blockEntity.fluidPosArray.get(0)        , Blocks.AIR.defaultBlockState(), true);
					replaceBlock(level, blockEntity.fluidPosArray.get(0).north(), Blocks.AIR.defaultBlockState(), true);
					replaceBlock(level, blockEntity.fluidPosArray.get(0).east() , Blocks.AIR.defaultBlockState(), true);
					replaceBlock(level, blockEntity.fluidPosArray.get(0).south(), Blocks.AIR.defaultBlockState(), true);
					replaceBlock(level, blockEntity.fluidPosArray.get(0).west() , Blocks.AIR.defaultBlockState(), true);}
				catch(Exception ignored) {}
				blockEntity.fluidPosArray.remove(0);
			}
		}

		//STAGE 5
		if(blockEntity.stage5 && blockEntity.isLit()) {
			if(blockEntity.blockPosArray.isEmpty()) {
				blockEntity.stage5 = false;
				blockEntity.stage6 = true;
			} else if(blockEntity.miningTick == blockEntity.miningSpeed) {
				try {mineBlock(level, blockEntity.blockPosArray.get(0));} catch(Exception ignored) {}
				AABB aabb = new AABB(blockEntity.blockPosArray.get(0), blockEntity.blockPosArray.get(0).offset(1, 2, 1));
				collectAndEject(level, pos, aabb, ITEM_ENTITY_PREDICATE);
				blockEntity.blockPosArray.remove(0);
			}
		}

		//STAGE 6
		if(blockEntity.stage6 && blockEntity.isLit()){

			blockEntity.quarryDigArea = blockEntity.quarryDigArea.modMinY(-1);
			blockEntity.stage6 = false;

			if(blockEntity.quarryDigArea.minY() <= 0){blockEntity.hasMined = true; blockEntity.stage7 = true;}
			else{blockEntity.stage2 = true;}
		}
		//STAGE 7
		if(blockEntity.stage7 && blockEntity.isLit()) {
			//System.out.println("Stage 7");
			if(blockEntity.supportPosArray.size() == blockEntity.supportPos && blockEntity.platformPosArray.size() == blockEntity.platformPos) {
				blockEntity.supportPos = 0;
				blockEntity.supportPosArray.clear();
				blockEntity.platformPos = 0;
				blockEntity.platformPosArray.clear();
				blockEntity.stage7 = false;
			}
			else if(blockEntity.miningTick == blockEntity.miningSpeed) {
				if(hasValidBlocks(blockEntity.itemHandle, -1, Tags.MINESHAFT_SUPPORT) && !(blockEntity.supportPosArray.size() == blockEntity.supportPos)) {
					int slotIndex = getValidSlot(blockEntity.itemHandle, -1, Tags.MINESHAFT_SUPPORT);
					Item item = blockEntity.itemHandle.getStackInSlot(slotIndex).getItem();
					if(canPlace(level, blockEntity.supportPosArray.get(blockEntity.supportPos), true, false)) {
						replaceBlock(level, blockEntity.supportPosArray.get(blockEntity.supportPos), blockStateFromItem(item));
						blockEntity.itemHandle.getStackInSlot(slotIndex).shrink(1);}
					blockEntity.placedBlock = true;
					blockEntity.supportPos++;}

				if(!(blockEntity.platformPosArray.size() == blockEntity.platformPos)){
					ArrayList<Integer> slots = new ArrayList<>();
					for(int s = 0; s < blockEntity.itemHandle.getSlots() - 1; s++){
						Item item = blockEntity.itemHandle.getStackInSlot(s).getItem();
						if(item instanceof BlockItem && (blockFromItem(item).defaultBlockState().is(Tags.MINESHAFT_PLATFORM))) {
							slots.add(s);}}

					for (Integer slot : slots) {
						Item item = blockEntity.itemHandle.getStackInSlot(slot).getItem();
						BlockState blockState = blockEntity.platformPosArray.get(blockEntity.platformPos).getBlockState();
						BlockPos blockpos = blockEntity.platformPosArray.get(blockEntity.platformPos).getBlockPos();
						boolean hasFacing = blockState.getOptionalValue(FACING).isPresent();
						boolean hasHalf = blockState.getOptionalValue(HALF).isPresent();
						boolean hasSlabType = blockState.getOptionalValue(TYPE).isPresent();
						var arrayClass = blockState.getBlock().getClass();
						var itemBlockClass = blockFromItem(item).getClass();
						if (itemBlockClass == arrayClass || arrayClass == FenceBlock.class && (itemBlockClass == WallBlock.class ||
								itemBlockClass == IronBarsBlock.class || itemBlockClass == ChainBlock.class)) {
							if (canPlace(level, blockpos, true, false)) {
								if(hasFacing && hasHalf){
									replaceBlock(level, blockpos, blockStateFromItem(item)
											.trySetValue(FACING, blockState.getValue(FACING))
											.trySetValue(HALF, blockState.getValue(HALF)));}
								else if(hasSlabType){
									replaceBlock(level, blockpos, blockStateFromItem(item)
											.trySetValue(TYPE, blockState.getValue(TYPE)));}
								else{
									replaceBlock(level, blockpos, blockStateFromItem(item));}
								blockEntity.itemHandle.getStackInSlot(slot).shrink(1);}
							blockEntity.placedBlock = true;
							blockEntity.platformPos++;
							break;}
					}
				}
			}
		}

		blockEntity.miningTick++;
		if(blockEntity.miningTick > blockEntity.miningSpeed)
			blockEntity.miningTick = 0;
    }

	private static boolean isFuel(ItemStack itemStack) {
		return net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack, null) > 0;
	}

	private static int getBurnDuration(ItemStack itemStack) {
		if (itemStack.isEmpty()) {return 0;}
		else {return net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack, null);}
	}

	private boolean isLit() {
	      return this.burnTime > 0;
	   }
	
	private static void burn(ItemStack coalSlot, MineshaftBuilderBlockEntity blockEntity){
		coalSlot.shrink(1);
		blockEntity.burnTime = blockEntity.maxBurnTime;
	}

}
