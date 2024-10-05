package com.leomorev.surreel.block.quarry.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public interface HasModules {
    List<Module> getModules();

    private boolean get(ResourceLocation name){
        return getModules().stream().map(Module::name).collect(Collectors.toList()).contains(name);
    }

    //void updateModules() {
    //    if(this.level == null){return;}
    //    for (Module value : this.modules) {
    //        BlockPos modulePos = value.pos();
    //        BlockState moduleBlock = this.level.getBlockState(modulePos);
    //        if (moduleBlock.getBlock() instanceof QuarryModule module) {
    //            module.setStatus(this.level, modulePos, moduleBlock, false);
    //        }
    //    }
    //    this.modules.clear();
    //    scanForModules(this.worldPosition, this.getBlockState());
    //    for(int i = 0; i < this.modules.size(); i++){
    //        if(this.modules.size() >= 5){
    //            return;
    //        }
    //        scanForModules(this.modules.get(i).pos(), this.level.getBlockState(this.modules.get(i).pos()));
    //    }
    //}
    //void scanForModules(BlockPos pos, BlockState state) {
    //    if(this.level == null){return;}
    //    Direction facing = state.getValue(FACING);
    //    List<Direction> directions = Arrays.asList(facing.getCounterClockWise(), facing.getClockWise());
    //    for(Direction direction : directions){
    //        BlockPos modulePos = pos.relative(direction);
    //        BlockState moduleBlock = this.level.getBlockState(modulePos);
    //        if(moduleBlock.getBlock() instanceof QuarryModule module){
    //            if(!this.modules.contains(new Module(module.moduleName(), modulePos)) && !moduleBlock.getValue(ModuleBlock.ACTIVE)){
    //                this.modules.add(new Module(module.moduleName(), modulePos));
    //                module.setStatus(this.level, modulePos, moduleBlock, true);
    //            }
    //        }
    //    }
    //}

    default boolean hasFortuneModule()      {return get(QuarryModule.Constant.FORTUNE.moduleName());}
    default boolean hasSilkTouchModule()    {return get(QuarryModule.Constant.SILK_TOUCH.moduleName());}
    default boolean hasEfficiencyModule()   {return get(QuarryModule.Constant.EFFICIENCY.moduleName());}
    default boolean hasPumpModule()         {return get(QuarryModule.Constant.PUMP.moduleName());}
    default boolean hasFreezerModule()      {return get(QuarryModule.Constant.FREEZER.moduleName());}
}
