package com.leomorev.surreel.block.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public record QuarryArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){

    public QuarryArea(BlockPos pos1, BlockPos pos2) {
        this(
                Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    public QuarryArea inflate(int x, int y, int z){
        int x1 = minX - x;
        int x2 = maxX + x;
        int y1 = minY - y;
        int y2 = maxY + y;
        int z1 = minZ - z;
        int z2 = maxZ + z;
        return new QuarryArea(
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public QuarryArea shrink(int x, int y, int z) {
        int x1 = minX + x;
        int x2 = maxX - x;
        int y1 = minY + y;
        int y2 = maxY - y;
        int z1 = minZ + z;
        int z2 = maxZ - z;
        return new QuarryArea(
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public QuarryArea moveYAxis(int y) {
        int y1 = minY + y;
        int y2 = maxY + y;
        return new QuarryArea(
                Math.min(minX, maxX), Math.min(y1, y2), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(y1, y2), Math.max(minZ, maxZ));
    }

    public QuarryArea clampMinToMaxY() {
        return new QuarryArea(
                Math.min(minX, maxX), maxY, Math.min(minZ, maxZ),
                Math.max(minX, maxX), maxY, Math.max(minZ, maxZ));
    }

    public QuarryArea modMinY(int y) {
        int y1 = minY + y;
        return new QuarryArea(
                Math.min(minX, maxX), Math.min(y1, maxY), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(y1, maxY), Math.max(minZ, maxZ));
    }

    public QuarryArea modMaxY(int y) {
        int y1 = maxY + y;
        return new QuarryArea(
                Math.min(minX, maxX), Math.min(minY, y1), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(minY, y1), Math.max(minZ, maxZ));
    }

    public CompoundTag toNBT() {
        var tag = new CompoundTag();
        tag.putInt("minX", this.minX);
        tag.putInt("minY", this.minY);
        tag.putInt("minZ", this.minZ);
        tag.putInt("maxX", this.maxX);
        tag.putInt("maxY", this.maxY);
        tag.putInt("maxZ", this.maxZ);
        return tag;
    }

    public static Optional<QuarryArea> fromNBT(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new QuarryArea(
                    tag.getInt("minX"),
                    tag.getInt("minY"),
                    tag.getInt("minZ"),
                    tag.getInt("maxX"),
                    tag.getInt("maxY"),
                    tag.getInt("maxZ")
            ));
        }
    }
}
