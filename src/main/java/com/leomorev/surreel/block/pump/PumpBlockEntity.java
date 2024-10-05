package com.leomorev.surreel.block.pump;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.entity.MinerBuilderBlockEntity;
import com.leomorev.surreel.block.quarry.QuarryArea;
import com.leomorev.surreel.util.Energy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PumpBlockEntity extends MinerBuilderBlockEntity {
    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.PUMP_BLOCK_ENTITY.get(), pos, state);
    }

    private LazyOptional<IEnergyStorage> energyLazyOptional = LazyOptional.empty();
    private final Energy ENERGY_STORAGE = new Energy(100000, 2000){
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private LazyOptional<IFluidHandler> fluidLazyOptional = LazyOptional.empty();
    private final PumpTank FLUID_TANK = new PumpTank(8000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return true;
        }
    };


    private boolean quarryModuleMode = false;
    private QuarryArea quarryModuleArea = null;

    private List<BlockPos> fluidRemoveQueue      = new ArrayList<>();
    private List<BlockPos> wallFluidRemoveQueue  = new ArrayList<>();

    private int speed = 0;
    private int progress = 0;
    private int ticks;

    private boolean pStage1 = true;
    private boolean pStage2 = false;
    private boolean pStage3 = false;

    public static void tick(Level level, BlockPos pos, BlockState state, PumpBlockEntity be) {
        if(level == null || level.isClientSide){return;}

        be.ticks++;
        if(be.ticks > 10) {
            be.ticks = 0;

        }

        Direction facing = state.getValue(FACING);
        List<Direction> directions = Arrays.asList(facing.getOpposite(), facing.getCounterClockWise(), facing.getClockWise());
        for(Direction direction : directions){
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
            IFluidHandler handler = null;
            if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent()){
                handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).orElse(null);
            }
            if(blockEntity != null && handler != null){
                FluidStack sendFluid = be.FLUID_TANK.getFluid().copy();
                if(!sendFluid.isEmpty() && handler.isFluidValid(1, sendFluid)){
                    int maxItemsPerTransfer = 1000;
                    while (sendFluid.getAmount() < maxItemsPerTransfer) {
                        maxItemsPerTransfer--;}
                    sendFluid.setAmount(maxItemsPerTransfer);
                    int before = sendFluid.getAmount();
                    int after = maxItemsPerTransfer - handler.fill(sendFluid, IFluidHandler.FluidAction.EXECUTE);
                    be.FLUID_TANK.drain((before - after), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        if(be.isQuarryMode()){
            //PUMP MODULE STAGE 1
            if(be.pStage1 && be.hasEnoughEnergy(1000)){
                be.wallFluidRemoveQueue = be.scanFluidsInWalls(be.quarryModuleArea);
                be.fluidRemoveQueue = be.scanFluids(be.quarryModuleArea);
                if(!be.wallFluidRemoveQueue.isEmpty()){
                    be.extractEnergy(500);}
                if(!be.fluidRemoveQueue.isEmpty()){
                    be.extractEnergy(500);}
                be.pStage1 = false;
                be.pStage2 = true;
            }

            //PUMP MODULE STAGE 2
            if(be.pStage2){
                //Collect fluids in wall then fill in with damming blocks
                //If fluid can't be injected into pump module, loop until it does
                be.progress++;
                if(be.progress > be.speed && !be.wallFluidRemoveQueue.isEmpty()) {
                    if(be.hasEnoughEnergy(200)){
                        if(canPlace(level, be.wallFluidRemoveQueue.get(0), true, false)){
                            replaceBlock(level, be.wallFluidRemoveQueue.get(0), Objects.DAMMING_BLOCK.get().defaultBlockState());
                        }
                        be.wallFluidRemoveQueue.remove(0);
                        be.extractEnergy(200);
                        be.setChanged();
                    }
                    be.resetProgress();
                }
                if(be.wallFluidRemoveQueue.isEmpty()){
                    be.resetProgress();
                    be.pStage2 = false;
                    be.pStage3 = true;
                }
            }

            //PUMP MODULE STAGE 3
            if(be.pStage3){
                //Collect fluids in main digArea
                //If fluid buffer is full, halt progress
                be.progress++;
                if(be.progress > be.speed && !be.fluidRemoveQueue.isEmpty()) {
                    if(be.hasEnoughEnergy(200)){
                        boolean filledSuccess = false;
                        var targetPos = be.fluidRemoveQueue.get(0);
                        FluidState fluidState = level.getFluidState(targetPos);
                        FluidStack fluidStack = FluidStack.EMPTY;
                        if(!fluidState.isEmpty() && fluidState.isSource()){
                            fluidStack = new FluidStack(fluidState.getType(), 1000);
                        }
                        if(be.FLUID_TANK.getSpace() >= 1000 && be.FLUID_TANK.isFluidValid(fluidStack)){
                            be.FLUID_TANK.internalFill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                            filledSuccess = true;
                        }
                        if(filledSuccess){
                            replaceBlock(level, targetPos, Blocks.AIR.defaultBlockState(), true);
                            be.fluidRemoveQueue.remove(0);
                            be.extractEnergy(200);
                        }
                        else{
                            be.resetProgress();
                            be.pStage3 = false;
                            be.pStage1 = true;
                        }
                        be.setChanged();
                    }
                    be.resetProgress();
                }
                if(be.fluidRemoveQueue.isEmpty()){
                    be.resetProgress();
                    be.pStage3 = false;
                    be.pStage1 = true;
                }
            }
        }
    }

    public void setSpeed(float seconds){
        this.speed = (int)(seconds / 0.05);
    }

    private void resetProgress() {
        this.progress = 0;
    }

    public void setQuarryModuleArea(QuarryArea area){
        this.quarryModuleArea = area;
        this.quarryModuleMode = area != null;
        this.setChanged();

    }

    public boolean isQuarryMode(){
        return this.quarryModuleMode;
    }

    public void extractEnergy(int amount) {
        this.ENERGY_STORAGE.extractEnergy(amount, false);
    }

    public int receiveEnergy(int amount, boolean sim) {
        return this.ENERGY_STORAGE.receiveEnergy(amount, sim);
    }

    public boolean hasEnoughEnergy(int requiredAmount) {
        return this.ENERGY_STORAGE.getEnergyStored() >= requiredAmount;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir){
        if(cap == ForgeCapabilities.ENERGY && !this.isQuarryMode()){
            switch (dir){
                case EAST, WEST, NORTH, SOUTH -> {
                    return energyLazyOptional.cast();
                }
            }
        }
        if(cap == ForgeCapabilities.FLUID_HANDLER){
            return fluidLazyOptional.cast();
        }

        return super.getCapability(cap, dir);
    }

    @Override
    public void onLoad(){
        super.onLoad();
        this.energyLazyOptional = LazyOptional.of(() -> ENERGY_STORAGE);
        this.fluidLazyOptional  = LazyOptional.of(() -> FLUID_TANK);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.energyLazyOptional.invalidate();
        this.fluidLazyOptional.invalidate();
    }

    public class PumpTank extends FluidTank{
        public PumpTank(int capacity) {
            super(capacity);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        public int internalFill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(resource)) {return 0;}
            if (action.simulate()) {
                if (fluid.isEmpty()) {return Math.min(capacity, resource.getAmount());}
                if (!fluid.isFluidEqual(resource)) {return 0;}
                return Math.min(capacity - fluid.getAmount(), resource.getAmount());
            }
            if (fluid.isEmpty()) {
                fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
                onContentsChanged();
                return fluid.getAmount();
            }
            if (!fluid.isFluidEqual(resource)) {return 0;}
            int filled = capacity - fluid.getAmount();
            if (resource.getAmount() < filled) {
                fluid.grow(resource.getAmount());
                filled = resource.getAmount();
            }
            else {fluid.setAmount(capacity);}
            if (filled > 0)
                onContentsChanged();
            return filled;
        }
    }
}
