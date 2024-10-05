package com.leomorev.surreel.block.transport_pipe;

import com.leomorev.surreel.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TransportPipe extends BaseEntityBlock{
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	static double minX = 5D / 16.0D;
	static double maxX = 11D / 16.0D;
	static double minY = 0;
	static double maxY = 1;
	static double minZ = 5D / 16.0D;
	static double maxZ = 11D / 16.0D;                      //minX, minY, minZ, maxX, maxY, maxZ
	private static final VoxelShape VOX_UP 		= Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
	private static final VoxelShape VOX_DOWN 	= Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
	private static final VoxelShape VOX_NORTH 	= Shapes.box(minX, minZ, minY, maxX, maxZ, maxY);
	private static final VoxelShape VOX_SOUTH 	= Shapes.box(minX, minZ, minY, maxX, maxZ, maxY);
	private static final VoxelShape VOX_WEST 	= Shapes.box(minY, minX, minZ, maxY, maxX, maxZ);
	private static final VoxelShape VOX_EAST 	= Shapes.box(minY, minX, minZ, maxY, maxX, maxZ);

	public static final EnumProperty<ConnectorType> CON_NORTH 	= EnumProperty.create("north", 	ConnectorType.class);
	public static final EnumProperty<ConnectorType> CON_SOUTH 	= EnumProperty.create("south", 	ConnectorType.class);
	public static final EnumProperty<ConnectorType> CON_WEST 	= EnumProperty.create("west", 	ConnectorType.class);
	public static final EnumProperty<ConnectorType> CON_EAST 	= EnumProperty.create("east", 	ConnectorType.class);
	public static final EnumProperty<ConnectorType> CON_UP 		= EnumProperty.create("up", 	ConnectorType.class);
	public static final EnumProperty<ConnectorType> CON_DOWN 	= EnumProperty.create("down", 	ConnectorType.class);

	public TransportPipe() {
		super(Properties.of().sound(SoundType.METAL).noOcclusion());
	}

	// ROTATION/FACING
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		return calculateState(world, pos, defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()));
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, CON_NORTH, CON_SOUTH, CON_UP, CON_DOWN, CON_EAST, CON_WEST);
	}

	public static BlockState calculateState(LevelAccessor world, BlockPos pos, BlockState state) {
		ConnectorType north 	= getConnectorType(world, pos, Direction.NORTH);
		ConnectorType south 	= getConnectorType(world, pos, Direction.SOUTH);
		ConnectorType west 		= getConnectorType(world, pos, Direction.WEST);
		ConnectorType east 		= getConnectorType(world, pos, Direction.EAST);
		ConnectorType up 		= getConnectorType(world, pos, Direction.UP);
		ConnectorType down 		= getConnectorType(world, pos, Direction.DOWN);
		return state
				.setValue(CON_NORTH, north).setValue(CON_SOUTH, south)
				.setValue(CON_WEST, west).setValue(CON_EAST, east)
				.setValue(CON_UP, up).setValue(CON_DOWN, down);
	}
	    
	// BLOCK ENTITY AND MENUS

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new TransportPipeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> entity) {
		return createTickerHelper(entity, Objects.TRANSPORT_PIPE_BLOCK_ENTITY.get(),
				TransportPipeBlockEntity::tick);
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case UP     -> VOX_UP;
			case DOWN   -> VOX_DOWN;
			case EAST   -> VOX_EAST;
			case WEST   -> VOX_WEST;
			case NORTH  -> VOX_NORTH;
			case SOUTH  -> VOX_SOUTH;
		};
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighbourState, @Nonnull LevelAccessor world, @Nonnull BlockPos current, @Nonnull BlockPos offset) {
		return calculateState(world, current, state);
	}

	@Override
	public void setPlacedBy(@Nonnull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		BlockState blockState = calculateState(level, pos, state);
		if (state != blockState) {
			level.setBlockAndUpdate(pos, blockState);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if(!level.isClientSide) {
			if (state.getBlock() != newState.getBlock()) {
				if (level.getBlockEntity(pos) instanceof TransportPipeBlockEntity be) {
					be.drops(level, pos);
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	private static ConnectorType getConnectorType(BlockGetter world, BlockPos pipePos, Direction facing) {
		var pos = pipePos.relative(facing);
		var state = world.getBlockState(pos);
		var thisPipe= world.getBlockState(pipePos);
		var block = state.getBlock();

		if(thisPipe.getProperties().contains(FACING) && thisPipe.getValue(FACING) == facing){
			return ConnectorType.OUTPUT;
		}
		//else if (isConnectable(world, pipePos, facing)){
		//	if(block instanceof TransportPipe){
		//		if(state.getProperties().contains(FACING) && state.getValue(FACING).getOpposite() == facing){
		//			return ConnectorType.INPUT;
		//		}
		//	}
		//	//else if(thisPipe.getProperties().contains(FACING) && thisPipe.getValue(FACING) == facing){
		//	//	return ConnectorType.SIDE_LOAD;
		//	//}
		//}



		if (block instanceof TransportPipe) {
			if(state.getProperties().contains(FACING) && state.getValue(FACING).getOpposite() == facing){
				return ConnectorType.INPUT;
			}
		}
		else if (isConnectable(world, pipePos, facing)) {
			if(state.getProperties().contains(FACING) && state.getValue(FACING).getOpposite() == facing){
				return ConnectorType.INPUT;
			}
			return ConnectorType.INPUT;
		}
		return ConnectorType.NONE;
	}

	public static boolean isConnectable(BlockGetter world, BlockPos connectorPos, Direction facing) {
		BlockPos pos = connectorPos.relative(facing);
		BlockState state = world.getBlockState(pos);
		if (state.isAir()) {
			return false;
		}
		BlockEntity te = world.getBlockEntity(pos);
		if (te == null) {
			return false;
		}
		return te.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
	}

	public enum ConnectorType implements StringRepresentable {
		NONE,
		INPUT,
		SIDE_LOAD,
		OUTPUT;

		public static final ConnectorType[] VALUES = values();
		@Override
		@NotNull
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}
}
