package com.leomorev.surreel.block.quarry;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.quarry_marker.QuarryMarkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class QuarryBlock extends BaseEntityBlock{
	public QuarryBlock() {
		super(Properties.of().sound(SoundType.METAL));
	}
	
	// ROTATION/FACING
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING);
	}

	// BLOCK ENTITY

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if(!level.isClientSide) {
			if (state.getBlock() != newState.getBlock()) {
				if (level.getBlockEntity(pos) instanceof QuarryBlockEntity be) {
					be.getStorage().drops(level, pos);
					be.deactivateModules();
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if(level.isClientSide){return;}
		if(level.getBlockEntity(pos) instanceof QuarryBlockEntity quarry){
			quarry.setSize(findSize(level, pos, state.getValue(FACING).getOpposite()));
		}

	}

	//Searches for valid quarry markers if none are valid or don't exist then return default quarry size
	static QuarryArea findSize(Level level, BlockPos pos, Direction behind) {
		List<Direction> directions = Arrays.asList(behind, behind.getCounterClockWise(), behind.getClockWise());
		for (Direction direction : directions) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof QuarryMarkerBlockEntity marker && marker.isConnected()) {
				return marker.getArea();}}
		return new QuarryArea(pos.relative(behind).relative(behind.getCounterClockWise(), 5),
				pos.relative(behind, 11).relative(behind.getClockWise(), 5).relative(Direction.UP, 4));
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new QuarryBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> entity) {
		return createTickerHelper(entity, Objects.QUARRY_BLOCK_ENTITY.get(),
				QuarryBlockEntity::tick);
	}
	
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}

}
