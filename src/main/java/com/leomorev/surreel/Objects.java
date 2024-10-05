package com.leomorev.surreel;

import com.leomorev.surreel.block.trash_can.ItemTrashCan;
import com.leomorev.surreel.block.trash_can.TrashCanBlockEntity;
import com.leomorev.surreel.block.entity.OilStoneBlockEntity;
import com.leomorev.surreel.block.quarry_marker.QuarryMarker;
import com.leomorev.surreel.block.quarry_marker.QuarryMarkerBlockEntity;
import com.leomorev.surreel.block.mineshaft_builder.MineShaftBuilder;
import com.leomorev.surreel.block.mineshaft_builder.MineshaftBuilderBlockEntity;
import com.leomorev.surreel.block.mineshaft_builder.MineshaftBuilderMenu;
import com.leomorev.surreel.block.oil_machinery.OilStone;
import com.leomorev.surreel.block.pump.Pump;
import com.leomorev.surreel.block.pump.PumpBlockEntity;
import com.leomorev.surreel.block.transport_pipe.TransportPipe;
import com.leomorev.surreel.block.quarry.module.*;
import com.leomorev.surreel.block.quarry.QuarryBlock;
import com.leomorev.surreel.block.quarry.QuarryBlockEntity;
import com.leomorev.surreel.block.transport_pipe.TransportPipeBlockEntity;
import com.leomorev.surreel.block.vein_miner.VeinMiner;
import com.mojang.datafixers.DSL;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Objects{
    //Registry lists to be called by IEventBus
    public static final DeferredRegister<Item>                  ITEM_REG 			= DeferredRegister.create(Registries.ITEM, 					SurReelMain.MODID);
    public static final DeferredRegister<Block> 				BLOCK_REG 			= DeferredRegister.create(Registries.BLOCK, 				SurReelMain.MODID);
    public static final DeferredRegister<BlockEntityType<?>> 	BLOCK_ENTITY_REG 	= DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, 	SurReelMain.MODID);
    public static final DeferredRegister<MenuType<?>> 			MENU_REG 			= DeferredRegister.create(Registries.MENU, 					SurReelMain.MODID);

    public static void register(IEventBus bus){
        ITEM_REG.register(bus);
        BLOCK_REG.register(bus);
        BLOCK_ENTITY_REG.register(bus);
        MENU_REG.register(bus);
        bus.register(CreativeTab.class);
    }

    //Suppliers - Basic item/block properties; looks cleaner
    private static final Supplier<Item>     basicItem   = () -> new Item(new Item.Properties());
    private static final Supplier<Block>    basicBlock  = () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
    private static final Supplier<Block>    basicGlass  = () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS));

    //Items
    public static final RegistryObject<Item> TEST1                      = registerItem("test",                    basicItem);

    //Blocks
    public static final RegistryObject<Block> ASHED_STONE  		        = registerBlock("ashed_stone",            basicBlock);
    public static final RegistryObject<Block> DAMMING_BLOCK             = registerBlock("damming_block",          basicGlass);
    public static final RegistryObject<Block> ASHED_COBBLESTONE         = registerBlock("ashed_cobblestone",      basicBlock);
    public static final RegistryObject<Block> ASHED_STONE_BRICK         = registerBlock("ashed_stonebricks",      basicBlock);
    public static final RegistryObject<Block> OIL_SOAKED_STONE          = registerBlock("oil_soaked_stone",       OilStone::new);

    //Quarry Modules
    public static final RegistryObject<Block> SILK_TOUCH_MODULE         = registerBlock("silk_touch_module",      SilkTouchModule::new);
    public static final RegistryObject<Block> EFFICIENCY_I_MODULE       = registerBlock("efficiency_i_module",    EfficiencyIModule::new);
    public static final RegistryObject<Block> EFFICIENCY_II_MODULE      = registerBlock("efficiency_ii_module",   EfficiencyIIModule::new);
    public static final RegistryObject<Block> EFFICIENCY_III_MODULE     = registerBlock("efficiency_iii_module",  EfficiencyIIIModule::new);
    public static final RegistryObject<Block> EFFICIENCY_IV_MODULE      = registerBlock("efficiency_iv_module",   EfficiencyIVModule::new);
    public static final RegistryObject<Block> EFFICIENCY_V_MODULE       = registerBlock("efficiency_v_module",    EfficiencyVModule::new);
    public static final RegistryObject<Block> FORTUNE_I_MODULE          = registerBlock("fortune_i_module",       FortuneIModule::new);
    public static final RegistryObject<Block> FORTUNE_II_MODULE         = registerBlock("fortune_ii_module",      FortuneIIModule::new);
    public static final RegistryObject<Block> FORTUNE_III_MODULE        = registerBlock("fortune_iii_module",     FortuneIIIModule::new);
    public static final RegistryObject<Block> FREEZER_MODULE            = registerBlock("freezer_module",         FreezerModule::new);
    public static final RegistryObject<Block> RE_TICKER_MODULE          = registerBlock("re_ticker_module",       ReTickerModule::new);

    //Testing
    public static final RegistryObject<Block> TRASH_CAN                 = registerBlock("trash_can",              ItemTrashCan::new);

    //Machines
    public static final RegistryObject<Block> MINESHAFT_BUILDER         = registerBlock("mineshaft_builder",      MineShaftBuilder::new);
    public static final RegistryObject<Block> VEIN_MAINER               = registerBlock("vein_miner",             VeinMiner::new);
    public static final RegistryObject<Block> QUARRY                    = registerBlock("quarry",                 QuarryBlock::new);
    public static final RegistryObject<Block> TRANSPORT_PIPE            = registerBlock("transport_pipe",         TransportPipe::new);
    public static final RegistryObject<Block> QUARRY_MARKER             = registerBlock("quarry_marker",          QuarryMarker::new);
    public static final RegistryObject<Block> PUMP                      = registerBlock("pump",                   Pump::new);

    public static final RegistryObject<Block> OIL_REFINERY              = registerBlock("oil_refinery",           basicBlock);
    public static final RegistryObject<Block> RECYCLER                  = registerBlock("recycler",               basicBlock);
    public static final RegistryObject<Block> BLOCK_BREAKER             = registerBlock("block_breaker",          basicBlock);
    public static final RegistryObject<Block> BLOCK_PLACER              = registerBlock("block_placer",           basicBlock);
    public static final RegistryObject<Block> WASTE_TREATMENT           = registerBlock("waste_treatment",        basicBlock);

    public static final RegistryObject<Block> CROP_FARM                 = registerBlock("crop_farm",              basicBlock);
    public static final RegistryObject<Block> TREE_FARM                 = registerBlock("tree_farm",              basicBlock);

    public static final RegistryObject<Block> SOLAR_PANEL               = registerBlock("solar_panel",            basicBlock);
    public static final RegistryObject<Block> BURNER_GENERATOR          = registerBlock("burner_generator",       basicBlock);
    public static final RegistryObject<Block> GEOTHERMAL_GENERATOR      = registerBlock("geothermal_generator",   basicBlock);
    public static final RegistryObject<Block> GAS_GENERATOR             = registerBlock("gas_generator",          basicBlock);

    //Block Entities
    public static final RegistryObject<BlockEntityType<MineshaftBuilderBlockEntity>>        MINESHAFT_BUILDER_BLOCK_ENTITY  = registerBlockEntityType(MINESHAFT_BUILDER, MineshaftBuilderBlockEntity::new);
    public static final RegistryObject<BlockEntityType<OilStoneBlockEntity>>                OIL_SOAKED_STONE_BLOCK_ENTITY   = registerBlockEntityType(OIL_SOAKED_STONE, OilStoneBlockEntity::new);
    public static final RegistryObject<BlockEntityType<QuarryBlockEntity>>                  QUARRY_BLOCK_ENTITY             = registerBlockEntityType(QUARRY, QuarryBlockEntity::new);
    public static final RegistryObject<BlockEntityType<TransportPipeBlockEntity>>           TRANSPORT_PIPE_BLOCK_ENTITY     = registerBlockEntityType(TRANSPORT_PIPE, TransportPipeBlockEntity::new);
    public static final RegistryObject<BlockEntityType<QuarryMarkerBlockEntity>>            QUARRY_MARKER_BLOCK_ENTITY      = registerBlockEntityType(QUARRY_MARKER, QuarryMarkerBlockEntity::new);
    public static final RegistryObject<BlockEntityType<PumpBlockEntity>>                    PUMP_BLOCK_ENTITY               = registerBlockEntityType(PUMP, PumpBlockEntity::new);
    public static final RegistryObject<BlockEntityType<FreezerModuleBlockEntity>>           FREEZER_MODULE_BLOCK_ENTITY     = registerBlockEntityType(FREEZER_MODULE, FreezerModuleBlockEntity::new);

    public static final RegistryObject<BlockEntityType<TrashCanBlockEntity>>                TRASH_CAN_BLOCK_ENTITY          = registerBlockEntityType(TRASH_CAN, TrashCanBlockEntity::new);

    //Menus
    public static final RegistryObject<MenuType<MineshaftBuilderMenu>>                      MINESHAFT_BUILDER_MENU          = registerMenuType(MINESHAFT_BUILDER, MineshaftBuilderMenu::new);


    //Register a new menu
    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(RegistryObject<Block> block, IContainerFactory<T> factory){
        assert block.getKey() != null;
        return MENU_REG.register(block.getId().getPath() + "_menu", () -> IForgeMenuType.create(factory));
    }

    //Register a new block entity
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntityType(RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<T> blockEntity){
        assert block.getKey() != null;
        return BLOCK_ENTITY_REG.register(block.getId().getPath() + "_block_entity", () -> BlockEntityType.Builder.of(blockEntity, block.get()).build(DSL.emptyPartType()));
    }

    //Register a new item
    private static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> item){
        return ITEM_REG.register(name, item);
    }

    //Registers a new block with itemBlock
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        var returns = BLOCK_REG.register(name, block);
        ITEM_REG.register(name, () -> new BlockItem(returns.get(), new Item.Properties()));
        return returns;
    }
}
