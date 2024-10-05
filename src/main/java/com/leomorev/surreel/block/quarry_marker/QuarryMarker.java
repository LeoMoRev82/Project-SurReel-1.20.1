package com.leomorev.surreel.block.quarry_marker;

import com.leomorev.surreel.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class QuarryMarker extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty PARENT = BooleanProperty.create("parent");
    public static final ParticleOptions particle = DustParticleOptions.REDSTONE;

    static double min = 6D / 16.0D;
    static double max = 10D / 16.0D;                    //minX, minY, minZ, maxX, maxY, maxZ
    private static final VoxelShape UP       = Shapes.box(min, 0, min, max, max, max);
    private static final VoxelShape DOWN     = Shapes.box(min, min, min, max, 1, max);
    private static final VoxelShape NORTH    = Shapes.box(min, min, min, max, max, 1);
    private static final VoxelShape SOUTH    = Shapes.box(min, min, 0, max, max, max);
    private static final VoxelShape WEST     = Shapes.box(min, min, min, 1, max, max);
    private static final VoxelShape EAST     = Shapes.box(0, min, min, max, max, max);

    public QuarryMarker(){
        super(Properties.of().lightLevel(light -> 5).noCollission().sound(SoundType.METAL));
        this.registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false)
                .setValue(LIT, false)
                .setValue(PARENT, false)
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if(!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof QuarryMarkerBlockEntity marker) {
                if(marker.isConnected() && state.getValue(LIT)){
                    Collection<BlockPos> markers = marker.getMarkerPositions();
                    markers.stream().flatMap(e -> level.getBlockEntity(e, Objects.QUARRY_MARKER_BLOCK_ENTITY.get()).stream())
                            .forEach(d -> d.disconnectMarkers(d));
                    marker.setRemoved();}
                if(!marker.isConnected() && !state.getValue(LIT)){marker.setRemoved();}
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    public void onPlace(@NotNull BlockState state1, Level level, @NotNull BlockPos pos, @NotNull BlockState state2, boolean bool) {
        level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource rng) {
        if (level.getBlockEntity(pos) instanceof QuarryMarkerBlockEntity marker) {
            if(marker.isConnected() && !state.getValue(LIT)){
                level.setBlock(pos, state.setValue(LIT, true).setValue(PARENT, marker.isParent()), 3);
            }
            if(!marker.isConnected() && state.getValue(LIT)){
                level.setBlock(pos, state.setValue(LIT, false).setValue(PARENT, marker.isParent()), 3);
            }
        }
        level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 1);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QuarryMarkerBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
        builder.add(LIT);
        builder.add(PARENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, fluid.is(Fluids.WATER))
                .setValue(LIT, false)
                .setValue(PARENT, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState nState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos nPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource rng) {
        if (state.getValue(LIT)) {
            double d0 = (double)pos.getX() + 0.5D + (rng.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double)pos.getY() + 0.7D + (rng.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double)pos.getZ() + 0.5D + (rng.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(particle, d0, d1, d2, 0,0, 0);
            level.addParticle(particle, d0 - 0.1D, d1 - 0.3D, d2 - 0.1D, 0,0, 0);
        }

        if(state.getValue(PARENT) && state.getValue(LIT)){
            double d0 = (double)pos.getX() + 0.5D + (rng.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double)pos.getY() + 0.7D + (rng.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double)pos.getZ() + 0.5D + (rng.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(particle, pos.getX() + .5D, pos.getY() + 1, pos.getZ() + .5D, 0,0, 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP     -> UP;
            case DOWN   -> DOWN;
            case EAST   -> EAST;
            case WEST   -> WEST;
            case NORTH  -> NORTH;
            case SOUTH  -> SOUTH;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof QuarryMarkerBlockEntity marker) {
                if(!marker.isConnected()) {marker.tryConnect(true, pos);}
                if(marker.getArea() != null){
                    player.displayClientMessage(Component.literal("%sCreated Area%s: %s"
                            .formatted(ChatFormatting.DARK_GREEN, ChatFormatting.RESET, marker.getArea())), false);
                }
                else{
                    player.displayClientMessage(Component.literal("%sCould Not Create Area...%s"
                            .formatted(ChatFormatting.DARK_RED, ChatFormatting.RESET)), false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
