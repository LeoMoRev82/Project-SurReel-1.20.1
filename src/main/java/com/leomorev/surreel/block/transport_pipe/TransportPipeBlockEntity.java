package com.leomorev.surreel.block.transport_pipe;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.entity.MinerBuilderBlockEntity;
import com.leomorev.surreel.block.quarry.MachineBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransportPipeBlockEntity extends MinerBuilderBlockEntity{
    public TransportPipeBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.TRANSPORT_PIPE_BLOCK_ENTITY.get(), pos, state);
    }


    private LazyOptional<IItemHandler> inputHandler = LazyOptional.empty();
    private final ItemStackHandler INPUT = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(!isPipeSectionFull(PipeSection.ONE)){
                return PIPE.insertItem(0, stack, simulate);}
            return stack;}
    };

    private LazyOptional<IItemHandler> sideLoadHandler = LazyOptional.empty();
    private final ItemStackHandler SIDE_LOAD = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(!isPipeSectionFull(PipeSection.THREE)){
                return PIPE.insertItem(32, stack, simulate);}
            if(!isPipeSectionFull(PipeSection.TWO)){
                return PIPE.insertItem(16, stack, simulate);}
            return stack;}
    };

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
    private final PipeStackHandler PIPE = new PipeStackHandler(64) {
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
            assert level != null;
            if(!level.isClientSide){
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);}}
        @Override
        public int getSlotLimit(int slot) {return 1;}
        //@Override
        //public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        //    if(!isPipeSectionFull(PipeSection.ONE)){
        //        return super.insertItem(slot, stack, simulate);}
        //    return stack;}
    };

    public static void tick(Level level, BlockPos pos, BlockState state, TransportPipeBlockEntity be) {
        if(level.isClientSide){return;}
            //Render items based on slot position and input direction
            //if need be may have to increase from 32 to 64 or even 128; Factorio is 256
            //Add splitters, filters, and extraction pipe.
            //Make it eject based the output of the pipe.
        // 6  -  7.5 per second
        // 12 - 15.0 per second
        // 24 - 30.0 per second

        for(int i = 0; i < be.PIPE.getSlots(); i++){
            PipeStack stack = be.PIPE.pipeStacks.get(i);
            stack.setPrevPosition(be.PIPE.getPipeStackInSlot(i).getPosition());
        }

        for(int k = 0; k < 1; k++){
            var direction = be.getBlockState().getValue(BlockStateProperties.FACING);
            var blockEntity = level.getBlockEntity(pos.relative(direction));
            if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).isPresent()){
                //if(blockEntity instanceof TransportPipeBlockEntity pipe){
                //    var sendItem = be.PIPE.getPipeStackInSlot(63);
                //    if(!pipe.isPipeSectionFull(PipeSection.ONE)){
                //        pipe.PIPE.insert(0, sendItem, false);}
                //}
                //else {
                //
                //}

                var sendItem = be.PIPE.getStackInSlot(63);
                int before = sendItem.getCount();
                sendItem = MachineBuffer.inject(sendItem, blockEntity, direction.getOpposite());
                int after = sendItem.getCount();
                be.PIPE.extractItem(63, before - after, false);

            }

            boolean lastSlotAir = false;
            int lastSlot = -1;
            for(int i = be.PIPE.getSlots() - 1; i > -1 ; i--){
                var stack = be.PIPE.getPipeStackInSlot(i);
                if(i >= be.PIPE.getSlots()){break;}
                if(stack == PipeStack.EMPTY){
                    lastSlotAir = true;
                    lastSlot = i;
                    continue;
                }
                if(lastSlotAir && (!be.isLastSectionIndex(i) || !be.isPipeSectionFull(be.getNextPipeSection(be.getPipeSectionFromIndex(i))))){
                    //stack.setPosition((int)((lastSlot + 1f) / be.PIPE.getSlots() * 100));
                    stack.setPosition(lastSlot);
                    be.PIPE.setPipeStackInSlot(i, PipeStack.EMPTY);
                    be.PIPE.setPipeStackInSlot(lastSlot, stack);
                    lastSlot = i;
                }
            }
        }
    }

    public List<PipeStack> getRenderStacks(){
        List<PipeStack> list = new ArrayList<>();
        for(int i = 0; i < this.PIPE.getSlots(); i++){
            list.add(this.PIPE.getPipeStackInSlot(i));
        }
        return list;
    }

    private boolean isLastSectionIndex(int i) {
        boolean isLast = false;
        switch (i){
            case 15, 31, 47, 63 -> isLast = true;}
        return isLast;
    }

    private PipeSection getNextPipeSection(PipeSection sec){
        return switch (sec){
            case INVALID -> PipeSection.INVALID;
            case ONE ->     PipeSection.TWO;
            case TWO ->     PipeSection.THREE;
            case THREE ->   PipeSection.FOUR;
            case FOUR ->    PipeSection.INVALID;
        };
    }

    public boolean isPipeSectionFull(PipeSection sec) {
        var quadList = new ArrayList<>();
        int fromIndex = 0;
        int toIndex = 0;
        switch (sec){
            case ONE ->     {fromIndex = 0; toIndex = 16;}
            case TWO ->     {fromIndex = 16; toIndex = 32;}
            case THREE ->   {fromIndex = 32; toIndex = 48;}
            case FOUR ->    {fromIndex = 48; toIndex = 64;}}
        for(int i = 0; i < this.PIPE.getSlots(); i++){
            quadList.add(i, this.PIPE.getStackInSlot(i));}
        return !quadList.subList(fromIndex, toIndex).stream().allMatch(n -> n.equals(ItemStack.EMPTY));
    }

    private PipeSection getPipeSectionFromIndex(int i) {
        return switch (i / 16){
            case 0 -> PipeSection.ONE;
            case 1 -> PipeSection.TWO;
            case 2 -> PipeSection.THREE;
            case 3 -> PipeSection.FOUR;
            default -> PipeSection.INVALID;
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side){
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            Direction facing = this.getBlockState().getValue(TransportPipe.FACING);
            if(side == facing.getOpposite()){
                return inputHandler.cast();}
            else {
                return sideLoadHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad(){
        super.onLoad();
        itemHandler     = LazyOptional.of(() -> PIPE);
        inputHandler    = LazyOptional.of(() -> INPUT);
        sideLoadHandler = LazyOptional.of(() -> SIDE_LOAD);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        itemHandler.invalidate();
        inputHandler.invalidate();
        sideLoadHandler.invalidate();
    }

    public void drops(Level level, BlockPos pos){
        SimpleContainer inventory = new SimpleContainer(this.PIPE.getSlots());
        for(int i = 0; i < this.PIPE.getSlots(); i++){
            inventory.setItem(i, this.PIPE.getStackInSlot(i));
        }
        assert level != null;
        Containers.dropContents(level, pos, inventory);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag){
        tag.put("inventory", this.PIPE.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag){
        super.load(tag);
        this.PIPE.deserializeNBT(tag.getCompound("inventory"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    public enum PipeSection{
        INVALID,
        ONE,
        TWO,
        THREE,
        FOUR
    }
}
