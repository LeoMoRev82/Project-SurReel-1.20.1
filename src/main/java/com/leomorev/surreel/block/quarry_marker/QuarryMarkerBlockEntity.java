package com.leomorev.surreel.block.quarry_marker;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.quarry.QuarryArea;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class QuarryMarkerBlockEntity extends BlockEntity {

    public QuarryMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.QUARRY_MARKER_BLOCK_ENTITY.get(), pos, state);
    }

    private BlockPos fPos;
    private BlockPos iPos;
    private BlockPos xPos;
    private BlockPos zPos;
    private QuarryArea quarryArea;
    private boolean isConnected;
    private boolean isParent;
    public boolean isConnected() {return this.isConnected;}
    public void setConnected(boolean connect) {this.isConnected = connect;}
    public QuarryArea getArea(){return this.quarryArea;}
    public BlockPos getParentPos(){return this.fPos;}
    public boolean isParent(){return this.isParent;}
    public void setParentPos(BlockPos pos){this.fPos = pos;}
    public void setParent(boolean isParent){this.isParent = isParent;}
    public void setArea(QuarryArea quarryArea){this.quarryArea = quarryArea;}
    public void setMarkerPositions(BlockPos iMarker, BlockPos zMarker, BlockPos xMarker){
        this.iPos = iMarker; this.zPos = zMarker; this.xPos = xMarker;}
    public List<BlockPos> getMarkerPositions(){
        return Arrays.asList(this.iPos, this.xPos, this.zPos);}

    private static final int maxSize = 128;
    private static final int minSize = 3;

    void tryConnect(boolean first, BlockPos firstPos) {
        assert getLevel() != null;
        Optional<QuarryMarkerBlockEntity> zMarker = IntStream.range(1 + minSize, maxSize + 1)
                .flatMap (i -> IntStream.of(i, -i))
                .mapToObj(i -> getBlockPos().relative(Direction.NORTH, i))
                .flatMap (l -> getLevel().getBlockEntity(l, Objects.QUARRY_MARKER_BLOCK_ENTITY.get()).stream())
                .findFirst();
        Optional<QuarryMarkerBlockEntity> xMarker = IntStream.range(1 + minSize, maxSize + 1)
                .flatMap (i -> IntStream.of(i, -i))
                .mapToObj(i -> getBlockPos().relative(Direction.EAST, i))
                .flatMap (l -> getLevel().getBlockEntity(l, Objects.QUARRY_MARKER_BLOCK_ENTITY.get()).stream())
                .findFirst();
        if(first && zMarker.isPresent() && !zMarker.get().isConnected()){zMarker.get().tryConnect(false, firstPos);}
        if(first && xMarker.isPresent() && !xMarker.get().isConnected()){xMarker.get().tryConnect(false, firstPos);}
        trySetArea(this, zMarker.orElse(null), xMarker.orElse(null), firstPos);
    }

    private void trySetArea(QuarryMarkerBlockEntity iMarker, QuarryMarkerBlockEntity zMarker, QuarryMarkerBlockEntity xMarker, BlockPos firstPos) {
        if(zMarker != null && xMarker != null && !zMarker.isConnected()  && !xMarker.isConnected() && !iMarker.isConnected()){
            assert level != null;
            QuarryArea quarryArea = new QuarryArea(xMarker.getBlockPos(), zMarker.getBlockPos().above(4));
            BlockPos iPos = iMarker.getBlockPos();
            BlockPos xPos = xMarker.getBlockPos();
            BlockPos zPos = zMarker.getBlockPos();
            BlockPos fPos = firstPos.immutable();

            List<QuarryMarkerBlockEntity> tileList = Arrays.asList(iMarker, zMarker, xMarker);
            tileList.forEach(t -> t.setArea(quarryArea));
            tileList.forEach(t -> t.setMarkerPositions(iPos, zPos, xPos));
            tileList.forEach(t -> t.setConnected(true));
            tileList.forEach(t -> t.setParentPos(fPos));
            tileList.forEach(t -> t.setParent(isFirst(t, fPos)));
        }
    }

    private boolean isFirst(QuarryMarkerBlockEntity tile, BlockPos first){
        return tile.getBlockPos().getX() == first.getX()
                && tile.getBlockPos().getY() == first.getY()
                && tile.getBlockPos().getZ() == first.getZ();
    }

    void disconnectMarkers(QuarryMarkerBlockEntity marker) {
        marker.setArea(null);
        marker.setParent(false);
        marker.setParentPos(null);
        marker.setConnected(false);
        marker.setMarkerPositions(null, null, null);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag){
        if(quarryArea != null) {tag.put("area", quarryArea.toNBT());}
        tag.putBoolean("isConnected", isConnected);
        tag.putBoolean("isParent", isParent);
        if(fPos != null){tag.putIntArray("fPos"   , Arrays.asList(fPos.getX(), fPos.getY(), fPos.getZ()));}
        if(iPos != null){tag.putIntArray("iMarker", Arrays.asList(iPos.getX(), iPos.getY(), iPos.getZ()));}
        if(xPos != null){tag.putIntArray("xMarker", Arrays.asList(xPos.getX(), xPos.getY(), xPos.getZ()));}
        if(zPos != null){tag.putIntArray("zMarker", Arrays.asList(zPos.getX(), zPos.getY(), zPos.getZ()));}
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag){
        super.load(tag);
        quarryArea = QuarryArea.fromNBT(tag.getCompound("area")).orElse(null);
        isConnected = tag.getBoolean("isConnected");
        isParent = tag.getBoolean("isParent");
        int[] ixz0 = tag.getIntArray("fPos"   );
        int[] ixz1 = tag.getIntArray("iMarker");
        int[] ixz2 = tag.getIntArray("xMarker");
        int[] ixz3 = tag.getIntArray("zMarker");
        if(ixz0.length == 3){fPos = new BlockPos(ixz0[0], ixz0[1], ixz0[2]);}
        if(ixz1.length == 3){iPos = new BlockPos(ixz1[0], ixz1[1], ixz1[2]);}
        if(ixz2.length == 3){xPos = new BlockPos(ixz2[0], ixz2[1], ixz2[2]);}
        if(ixz3.length == 3){zPos = new BlockPos(ixz3[0], ixz3[1], ixz3[2]);}
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}
