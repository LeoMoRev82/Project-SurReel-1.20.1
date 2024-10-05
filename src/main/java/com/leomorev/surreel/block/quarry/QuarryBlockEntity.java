package com.leomorev.surreel.block.quarry;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.block.entity.MinerBuilderBlockEntity;
import com.leomorev.surreel.block.pump.PumpBlockEntity;
import com.leomorev.surreel.block.quarry.module.EnchantmentLevel;
import com.leomorev.surreel.block.quarry.module.*;
import com.leomorev.surreel.block.quarry.module.Module;
import com.leomorev.surreel.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;


public class QuarryBlockEntity extends MinerBuilderBlockEntity implements HasModules, HasEnchants, MachineBuffer.HasStorage {

    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(Objects.QUARRY_BLOCK_ENTITY.get(), pos, state);
    }

    private List<EnchantmentLevel> enchantments = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();;

    private boolean stage1 = true;
    private boolean stage2 = false;
    private boolean stage3 = false;
    private boolean stage4 = false;
    private boolean finished = false;
    private boolean halted = false;

    private int maxDepth = -64;
    private final int maxItemsPerTransfer = 16;
    private int speed = 0;
    private int progress = 0;
    private int ticks;

    private List<BlockPos> blockBreakQueue = new ArrayList<>();

    private QuarryArea quarryArea;
    private QuarryArea miningArea;
    private final MachineBuffer machineBuffer = new MachineBuffer();

    private LazyOptional<IEnergyStorage> energyLazyOptional = LazyOptional.empty();
    private final Energy ENERGY_STORAGE = new Energy(1000000, 50000){
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
        if(level.isClientSide){return;}
        if(level.dimension() == Level.NETHER){be.maxDepth = 0;}
        if(pos.getY() <= be.maxDepth + 6 || (pos.getY() >= 110 && level.dimension() == Level.NETHER)){return;}

        if(!be.hasFinished()){
            be.updateSpeed();
            be.updateModules();
            be.updateEnchants();
        }

        be.ticks++;
        if(be.ticks > 10){
            be.ticks = 0;
            if(be.hasPumpModule()){
                BlockPos pumpPos = be.modules.stream().filter(n -> n.name() == QuarryModule.Constant.PUMP.moduleName()).map(Module::pos).findFirst().orElse(null);
                if(pumpPos != null && level.getBlockEntity(pumpPos) instanceof PumpBlockEntity pbe){
                    int received = pbe.receiveEnergy(2000, true);
                    if(be.hasEnoughEnergy(received)){
                        pbe.receiveEnergy(received, false);
                        pbe.setQuarryModuleArea(be.miningArea);
                        be.extractEnergy(received);
                    }
                }
            }
            be.getStorage().tryEjectBuffer(level, pos, be.maxItemsPerTransfer);
            be.setChanged();
        }

        //STAGE 1
        if(be.stage1 && be.hasEnoughEnergy(200)){
            be.blockBreakQueue = be.scanBlocks(be.quarryArea);
            be.extractEnergy(200);
            be.setChanged();
            be.stage1 = false;
            be.stage2 = true;
        }

        //STAGE 2
        if(be.stage2 && !be.halted && be.hasEnoughEnergy(200)){
            be.blockBreakQueue.addAll(be.scanBlocks(be.miningArea));
            be.extractEnergy(200);
            be.setChanged();
            be.stage2 = false;
            be.stage3 = true;
        }

        //STAGE 3
        if(be.stage3 && !be.halted){
            be.progress++;
            if(be.progress > be.speed && !be.blockBreakQueue.isEmpty()) {
                ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
                for(int i = 0; i < be.enchantments.size(); i++){
                    tool.enchant(be.enchantments.get(i).enchantment(), be.enchantments.get(i).level());
                }

                BlockPos targetPos = be.blockBreakQueue.get(0);
                float hardness = level.getBlockState(targetPos).getDestroySpeed(level, targetPos);
                int energyUse = getBreakEnergy(40, be, hardness);
                 if(be.hasEnoughEnergy(energyUse)){
                     mineBlock(level, targetPos, QuarryFakePlayer.get((ServerLevel) level), tool, false);
                     be.getStorage().collectAndStore(level, pos, targetPos);
                     be.blockBreakQueue.remove(0);
                     be.extractEnergy(energyUse);
                     be.setChanged();
                 }
                be.resetProgress();
            }
            if(be.blockBreakQueue.isEmpty()) {
                be.resetProgress();
                be.stage3 = false;
                be.stage4 = true;
            }
        }

        //STAGE 4
        if(be.stage4){
            be.miningArea = be.miningArea.modMinY(-1);
            be.stage4 = false;
            if(be.miningArea.minY() <= be.maxDepth){
                be.finished = true;
                be.setChanged();
            }
            else{
                be.stage2 = true;
            }
        }
    }

    void updateEnchants() {
        if(this.level == null){return;}
        this.enchantments.clear();
        List<EnchantmentLevel> silk         = new ArrayList<>();
        List<EnchantmentLevel> fortune      = new ArrayList<>();
        List<EnchantmentLevel> efficiency   = new ArrayList<>();
        for (Module module : this.modules) {
            if (this.level.getBlockState(module.pos()).getBlock() instanceof ModuleEnchants enchants) {
                if(enchants.getEnchantment().enchantment() == Enchantments.SILK_TOUCH){
                    silk.add(enchants.getEnchantment());
                }
                if(enchants.getEnchantment().enchantment() == Enchantments.BLOCK_FORTUNE){
                    fortune.add(enchants.getEnchantment());
                }
                if(enchants.getEnchantment().enchantment() == Enchantments.BLOCK_EFFICIENCY){
                    efficiency.add(enchants.getEnchantment());
                }
            }
        }
        fortune.addAll(EnchantmentLevel.breakDownFortune(fortune));
        efficiency.addAll(EnchantmentLevel.breakDownEfficiency(efficiency));
        silk.addAll(EnchantmentLevel.breakDownSilkTouch(silk));
        this.enchantments = Stream.of(fortune, efficiency, silk).flatMap(Collection::stream).collect(Collectors.toList());
    }

    //Add all available modules into a temporary list.
    //For each element test for incompatibility and is stackable with an existing module.
    //If is not compatible with another module already exists in list, don't add it and continue;
    //If is not stackable and same module already exists in list, don't add it and continue;
    //Copy elements into final list

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    void updateModules() {
        //long before = System.nanoTime();
        //long after;
        if(this.level == null){return;}
        this.deactivateModules();

        this.modules.clear();
        for (Module tempModule : scanForModules()) {
            if (this.level.getBlockState(tempModule.pos()).getBlock() instanceof QuarryModule module) {
                var incompatible = module.incompatibleWith();
                var stackable = module.isStackable();
                var curList = this.modules.stream().map(Module::name).toList();
                if (!incompatible.isEmpty()) {
                    var flag1 = false;
                    for (ResourceLocation name : incompatible) {
                        if (curList.contains(name)) {
                            flag1 = true;
                            break;
                        }
                    }
                    if (flag1) {
                        continue;
                    }
                }
                if (!stackable && curList.contains(tempModule.name())) {
                    continue;
                }
                this.modules.add(tempModule);
            }
        }

        //this.modules = scanForModules();
        for (Module value : this.modules) {
            BlockPos modulePos = value.pos();
            BlockState moduleBlock = this.level.getBlockState(modulePos);
            if (moduleBlock.getBlock() instanceof QuarryModule module) {
                module.setStatus(this.level, modulePos, moduleBlock, true);
            }
        }

        //after = System.nanoTime();
        //1000 - ns - micro
        //1000000 - micro to ms
        //System.out.println((after - before) / 1000);
    }

    List<Module> scanForModules() {
        if(this.level == null){return new ArrayList<>();}
        List<Module> list = new ArrayList<>();
        Direction facing = this.getBlockState().getValue(FACING);
        List<Direction> directions = Arrays.asList(facing.getCounterClockWise(), facing.getClockWise());
        for(Direction direction : directions){
            BlockPos modulePos = this.worldPosition.relative(direction);
            BlockState moduleBlock = this.level.getBlockState(modulePos);
            if(moduleBlock.getBlock() instanceof QuarryModule module){
                if(!list.contains(new Module(module.moduleName(), modulePos)) && !moduleBlock.getValue(ModuleBlock.ACTIVE)){
                    list.add(new Module(module.moduleName(), modulePos));
                }
            }
        }
        for(int i = 0; i < list.size(); i++){
            if(list.size() >= 5){
                return list;}
            for(Direction direction : directions){
                BlockPos modulePos = list.get(i).pos().relative(direction);
                BlockState moduleBlock = this.level.getBlockState(modulePos);
                if(moduleBlock.getBlock() instanceof QuarryModule module){
                    if(!list.contains(new Module(module.moduleName(), modulePos)) && !moduleBlock.getValue(ModuleBlock.ACTIVE)){
                        list.add(new Module(module.moduleName(), modulePos));
                    }
                }
            }
        }
        return list;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <T extends HasEnchants> int getBreakEnergy(int baseAmount, T enchantments, float hardness) {
        int fortuneLevel = enchantments.getFortuneLevel();
        int efficiencyLevel = enchantments.getEfficiencyLevel();
        int silkTouch = enchantments.getSilkTouchLevel();

        if (hardness < 0 || Float.isInfinite(hardness)) return 200 * baseAmount * (1 + efficiencyLevel);
        long base = (long) (baseAmount * Math.pow(1.3474010519681996, fortuneLevel) * Math.pow(2, silkTouch));
        double coefficient = ((double) hardness) * Math.pow(1.279929661461215, efficiencyLevel);

        return (int)((long) (coefficient * base));
    }

    public boolean hasFinished(){
        return this.finished;
    }

    public void setSize(QuarryArea quarryArea){
        this.quarryArea = quarryArea;
        this.miningArea = quarryArea.shrink(1, 0, 1).moveYAxis(-(quarryArea.maxY() - quarryArea.minY() + 1)).clampMinToMaxY();
    }

    private void resetProgress() {
        this.progress = 0;
    }

    public void setSpeed(float seconds){
        this.speed = (int)(seconds / 0.05);
    }

    private void updateSpeed() {
        if(this.getStorage().bufferPercentage() <= 50){
            this.halted = false;
            if(this.hasEfficiencyModule()){
                switch (this.getEfficiencyLevel()) {
                    case 1 -> this.setSpeed(0.3f);
                    case 2 -> this.setSpeed(0.2f);
                    case 3 -> this.setSpeed(0.1f);
                    case 4 -> this.setSpeed(0.05f);
                    case 5 -> this.setSpeed(0f);}
            }
            else {this.setSpeed(0.5f);}
        }
        else{this.halted = true;}
    }

    private void extractEnergy(int amount) {
        this.ENERGY_STORAGE.extractEnergy(amount, false);
    }

    private boolean hasEnoughEnergy(int requiredAmount) {
        return this.ENERGY_STORAGE.getEnergyStored() >= requiredAmount;
    }

    @Override
    public MachineBuffer getStorage() {
        return this.machineBuffer;
    }

    public void deactivateModules(){
        for (Module value : this.modules) {
            BlockPos modulePos = value.pos();
            assert this.level != null;
            BlockState moduleBlock = this.level.getBlockState(modulePos);
            if (moduleBlock.getBlock() instanceof QuarryModule module) {
                module.setStatus(this.level, modulePos, moduleBlock, false);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir){
        if(cap == ForgeCapabilities.ENERGY){return energyLazyOptional.cast();}
        if(cap == ForgeCapabilities.ITEM_HANDLER && (dir == null || dir == Direction.UP)) {
            return machineBuffer.itemHandler.cast();}
        return super.getCapability(cap, dir);
    }

    @Override
    public void onLoad(){
        super.onLoad();
        this.energyLazyOptional = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.energyLazyOptional.invalidate();
        this.getStorage().itemHandler.invalidate();
    }

    @Override
    public List<Module> getModules() {
        return this.modules;
    }

    @Override
    public List<EnchantmentLevel> getEnchants() {
        return this.enchantments;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag){
        tag.putInt("simple_quarry.energy", this.ENERGY_STORAGE.getEnergyStored());
        tag.put("buffer", this.machineBuffer.get().serializeNBT());
        tag.put("modules", Module.toNBTList(this.modules));
        tag.putInt("progress",      this.progress);
        tag.putBoolean("stage2",    this.stage2);
        tag.putBoolean("stage3",    this.stage3);
        tag.putBoolean("stage4",    this.stage4);
        tag.putBoolean("halted",    this.halted);
        tag.putBoolean("finished",  this.finished);
        if(this.quarryArea != null) {tag.put("quarryArea", this.quarryArea.toNBT());}
        if(this.miningArea != null) {tag.put("miningArea", this.miningArea.toNBT());}
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag){
        super.load(tag);
        this.ENERGY_STORAGE.setEnergy(tag.getInt("simple_quarry.energy"));
        this.machineBuffer.get().deserializeNBT(tag.getCompound("buffer"));
        this.modules = Module.fromNBTList("modules", tag);
        this.progress =     tag.getInt("progress");
        this.stage2 =       tag.getBoolean("stage2");
        this.stage3 =       tag.getBoolean("stage3");
        this.stage4 =       tag.getBoolean("stage4");
        this.halted =       tag.getBoolean("halted");
        this.finished =     tag.getBoolean("finished");
        this.quarryArea = QuarryArea.fromNBT(tag.getCompound("quarryArea")).orElse(null);
        this.miningArea = QuarryArea.fromNBT(tag.getCompound("miningArea")).orElse(null);
    }
}
