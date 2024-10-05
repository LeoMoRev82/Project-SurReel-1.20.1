package com.leomorev.surreel.block.entity;

import com.leomorev.surreel.block.quarry.QuarryArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MinerBuilderBlockEntity extends BlockEntity {
    public MinerBuilderBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public static void mineBlock(Level level, BlockPos pos){
        level.destroyBlock(pos, true);
        level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
    }

    public static boolean mineBlock(Level level, BlockPos pos, FakePlayer fakePlayer){
        return mineBlock(level, pos, fakePlayer, Items.NETHERITE_PICKAXE.getDefaultInstance(), false);
    }

    public static boolean mineBlock(Level level, BlockPos pos, FakePlayer fakePlayer, ItemStack tool, boolean requireTool){
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, tool);
        BlockState state = level.getBlockState(pos);
        var sound = state.getSoundType();
        var breakEvent = new BlockEvent.BreakEvent(level, pos, state, fakePlayer);
        var drops = Block.getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos), fakePlayer, tool);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if(breakEvent.isCanceled()){return false;}
        if(!isCorrectTool(state, tool) && requireTool){return false;}

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);
        state.getBlock().playerWillDestroy(level, pos, state, fakePlayer);

        for(int i = 0; i < drops.size(); i++){
            RandomSource rand;
            rand = level.random;
            ItemEntity ei = new ItemEntity(level, (double) pos.getX() + 0.5, pos.getY() + 1, (double) pos.getZ() + 0.5, drops.get(0));
            ei.setDeltaMovement((rand.nextDouble() - rand.nextDouble()) * 0.045, 0.25, (rand.nextDouble() - rand.nextDouble()) * 0.045);
            level.addFreshEntity(ei);
            drops.remove(0);
        }

        return true;
    }

    private static boolean isCorrectTool(BlockState state, ItemStack tool) {
        return tool.isCorrectToolForDrops(state);
    }

    public static void replaceBlock(Level level, BlockPos pos, BlockState state) {
        replaceBlock(level, pos, state, false);
    }

    public static void replaceBlock(Level level, BlockPos pos, BlockState state, boolean replaceFluidsOnly){
        SoundType soundType = state.getSoundType();
        if(isFluid(level, pos, false) && replaceFluidsOnly) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.gameEvent(null, GameEvent.BLOCK_PLACE, pos);
            level.playSound((Player)null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);}
        if(!replaceFluidsOnly) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.gameEvent(null, GameEvent.BLOCK_PLACE, pos);
            level.playSound((Player)null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);}
    }

    public static boolean canPlace(Level level, BlockPos pos){
        return canPlace(level, pos, false, false);
    }

    public static boolean canPlace(Level level, BlockPos pos, boolean includeFluids, boolean sourceOnly){
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState caveAir = Blocks.CAVE_AIR.defaultBlockState();
        BlockState voidAir = Blocks.VOID_AIR.defaultBlockState();

        if(includeFluids && isFluid(level, pos, sourceOnly))
            return true;
        if(level.getBlockState(pos) == air || level.getBlockState(pos) == caveAir || level.getBlockState(pos) == voidAir)
            return true;

        return false;
    }

    public static boolean isFluid(@NotNull Level level, BlockPos pos, boolean sourceOnly){
        if(sourceOnly){
            return level.getFluidState(pos).isSource();
        }
        else{
            return !level.getFluidState(pos).isEmpty();
        }
    }

    public List<BlockPos> scanBlocks(QuarryArea area) {
        if(this.level == null || this.level.isClientSide){return Collections.emptyList();}
        BlockState air     = Blocks.AIR.defaultBlockState();
        BlockState caveAir = Blocks.CAVE_AIR.defaultBlockState();
        BlockState voidAir = Blocks.VOID_AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        List<BlockPos> list = new ArrayList<>();
        for(int y = area.maxY(); y >= area.minY(); y--) {
            for(int x = area.minX(); x <= area.maxX(); x++) {
                for(int z = area.minZ(); z <= area.maxZ(); z++) {
                    BlockPos newPos = new BlockPos(x, y, z);
                    BlockState check = this.level.getBlockState(newPos);
                    if(!(isFluid(this.level, newPos, false) || check == air || check == caveAir || check == voidAir || check == bedrock)) {
                        list.add(newPos);
                    }
                }
            }
        }
        return list;
    }

    public List<BlockPos> scanFluids(QuarryArea area) {
        if(this.level == null || this.level.isClientSide){return Collections.emptyList();}
        var newArea = area.modMaxY(1);
        List<BlockPos> list = new ArrayList<>();
        for(int y = newArea.maxY(); y >= newArea.minY(); y--) {
            for(int x = newArea.minX(); x <= newArea.maxX(); x++) {
                for(int z = newArea.minZ(); z <= newArea.maxZ(); z++) {
                    var pos = new BlockPos(x, y, z);
                    if(isFluid(this.level, pos, true)) {
                        list.add(pos);
                    }
                }
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    public List<BlockPos> scanFluidsInWalls(QuarryArea area){
        if(this.level == null || this.level.isClientSide){return Collections.emptyList();}
        var newArea = area.inflate(1,0,1).modMaxY(1);
        List<BlockPos> list = new ArrayList<>();
        for(int y = newArea.minY(); y <= newArea.maxY(); y++){
            for(int x = newArea.minX(); x <= newArea.maxX(); x++){
                var minZ = new BlockPos(x, y, newArea.minZ());
                var maxZ = new BlockPos(x, y, newArea.maxZ());
                if(isFluid(this.level, minZ, false)){
                    list.add(minZ);}
                if(isFluid(this.level, maxZ, false)){
                    list.add(maxZ);}
            }
            for(int z = newArea.minZ(); z <= newArea.maxZ(); z++){
                var minX = new BlockPos(newArea.minX(), y, z);
                var maxX = new BlockPos(newArea.maxX(), y, z);
                if(isFluid(this.level, minX, false)){
                    list.add(minX);}
                if(isFluid(this.level, maxX, false)){
                    list.add(maxX);}
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    public static int scanMineCompletion(Level level, BlockPos pos, int offX, int offZ, int sizeX, int sizeZ) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState caveAir = Blocks.CAVE_AIR.defaultBlockState();
        BlockState voidAir = Blocks.VOID_AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        float countedBlocks 	= 0;
        float validBlocks 		= 0;
        float validPercent;
        for(int y = 0; y < pos.getY() - (-65); y++) {
            for(int x = 0; x < sizeX ; x++) {
                for(int z = 0; z < sizeZ ; z++) {
                    BlockState check = level.getBlockState(pos.offset(x + offX, -(y), z + offZ));
                    if(!(isFluid(level, pos.offset(x + offX, -(y), z + offZ), false) || (check != air && check != caveAir && check != voidAir && check != bedrock))) {
                        validBlocks++;}
                    countedBlocks++;
                }
            }
        }
        validPercent = (validBlocks * 100) / countedBlocks;
        validPercent = validPercent * 100;
        return (int)validPercent;
    }

    public static BlockState blockStateFromItem(Item item){
        return ((BlockItem) item).getBlock().defaultBlockState();
    }

    public static Block blockFromItem(Item item){
        return ((BlockItem) item).getBlock();
    }

    public static int getValidSlot(ItemStackHandler handler, int slotMod, TagKey<Block> targetBlock){
        for(int s = 0; s < handler.getSlots() + slotMod; s++){
            Item item = handler.getStackInSlot(s).getItem();
            if(item instanceof BlockItem && ((BlockItem) item).getBlock().defaultBlockState().is(targetBlock)) {
                return s;}
        }
        return 0;
    }

    public static boolean hasValidBlocks(ItemStackHandler handler, int slotMod, TagKey<Block> targetBlock) {
        for(int s = 0; s < handler.getSlots() + slotMod; s++){
            Item item = handler.getStackInSlot(s).getItem();
            if(item instanceof BlockItem && ((BlockItem) item).getBlock().defaultBlockState().is(targetBlock)) {
                return true;}
        }
        return false;
    }



    public static void collectAndEject(Level level, BlockPos pos, AABB aabb, Predicate<ItemEntity> itemEntityPredicate){
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, aabb, itemEntityPredicate);
        ArrayList<ItemStack> itemArray = new ArrayList<>();
        for (ItemEntity item : itemEntities) {
            if (item.getItem().getCount() <= 0) continue;
            itemArray.add(item.getItem().copy());
            item.setItem(ItemStack.EMPTY);
            item.kill();
        }
        System.out.println(itemArray);
        //while(!itemArray.isEmpty()) {
        //    ItemStack sendItem = itemArray.get(0);
        //    if(itemArray.get(0).isEmpty()) {itemArray.remove(0);continue;}
        //    //for(Direction face : Direction.values()) {
        //    //    sendItem = inject(sendItem, level.getBlockEntity(pos.relative(face)), face.getOpposite());
        //    //    if(sendItem.isEmpty()) {break;}
        //    //}
        //    System.out.println(sendItem);
        //    //Inject picked up items into an ItemCapability container
        //    sendItem = inject(sendItem, level.getBlockEntity(pos.relative(Direction.UP)), Direction.UP.getOpposite());
        //    if(sendItem.isEmpty()) {break;}
        //    //If above container is full self inject and fill internal buffer
        //    sendItem = inject(sendItem, level.getBlockEntity(pos), Direction.UP);
        //    if(sendItem.isEmpty()) {break;}
        //    //If internal buffer is full spill picked up items onto the ground
        //    if(!sendItem.isEmpty()) {
        //        RandomSource rand;
        //        rand = level.random;
        //        ItemEntity ei = new ItemEntity(level, (double) pos.getX() + 0.5, pos.getY() + 1, (double) pos.getZ() + 0.5, sendItem.copy());
        //        ei.setDeltaMovement((rand.nextDouble() - rand.nextDouble()) * 0.045, 0.25, (rand.nextDouble() - rand.nextDouble()) * 0.045);
        //        level.addFreshEntity(ei);
        //    }
        //}
        while(!itemArray.isEmpty()) {
            if(itemArray.get(0).isEmpty()) {
                itemArray.remove(0);
                continue;
            }
            ItemStack sendItem = itemArray.get(0);
            itemArray.remove(0);
            System.out.println(sendItem);
            //Inject picked up items into an ItemCapability container
            //sendItem = inject(sendItem, level.getBlockEntity(pos.relative(Direction.UP)), Direction.UP.getOpposite());
            //If above container is full self inject and fill internal buffer
            //sendItem = inject(sendItem, level.getBlockEntity(pos), Direction.UP);

            //If internal buffer is full spill picked up items onto the ground
            if(!sendItem.isEmpty()) {
                RandomSource rand;
                rand = level.random;
                ItemEntity ei = new ItemEntity(level, (double) pos.getX() + 0.5, pos.getY() + 1, (double) pos.getZ() + 0.5, sendItem.copy());
                ei.setDeltaMovement((rand.nextDouble() - rand.nextDouble()) * 0.045, 0.25, (rand.nextDouble() - rand.nextDouble()) * 0.045);
                level.addFreshEntity(ei);
            }
        }
    }

    public static class BlockStateProperty{
        private final BlockPos blockPos;
        private final BlockState blockState;

        public BlockStateProperty(BlockPos pos, BlockState state){
            blockPos = pos;
            blockState = state;
        }

        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        public BlockState getBlockState(){return this.blockState;}

        public Block getBlock(){return this.blockState.getBlock();}
    }
}


