package com.leomorev.surreel;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

public class CreativeTab {
    @SubscribeEvent
    public static void registerCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, CreativeTab::createTab);
    }

    public static void createTab(RegisterEvent.RegisterHelper<CreativeModeTab> event){
        event.register(new ResourceLocation(SurReelMain.MODID, "surreel_tab"), buildTab());
    }

    public static CreativeModeTab buildTab() {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(Objects.QUARRY.get()))
                .title(Component.literal("Project SurReel"))
                .displayItems((displayContext, e) -> {

                    //e.accept(Objects.OIL_REFINERY.get());
                    //e.accept(Objects.RECYCLER.get());
                    //e.accept(Objects.BLOCK_BREAKER.get());
                    //e.accept(Objects.BLOCK_PLACER.get());

                    //e.accept(Objects.CROP_FARM.get());
                    //e.accept(Objects.TREE_FARM.get());
                    //e.accept(Objects.WASTE_TREATMENT.get());

                    //e.accept(Objects.SOLAR_PANEL.get());
                    //e.accept(Objects.BURNER_GENERATOR.get());
                    //e.accept(Objects.GEOTHERMAL_GENERATOR.get());
                    //e.accept(Objects.GAS_GENERATOR.get());

                    //e.accept(Objects.ASHED_STONE.get());
                    //e.accept(Objects.ASHED_STONE_BRICK.get());
                    //e.accept(Objects.ASHED_COBBLESTONE.get());

                    //TEST
                    e.accept(Objects.TRASH_CAN.get());
                    e.accept(Objects.DAMMING_BLOCK.get());

                    //Item Transportation
                    e.accept(Objects.TRANSPORT_PIPE.get());

                    //Quarry and Its Modules
                    e.accept(Objects.QUARRY.get());
                    e.accept(Objects.PUMP.get());
                    e.accept(Objects.QUARRY_MARKER.get());
                    e.accept(Objects.SILK_TOUCH_MODULE.get());
                    e.accept(Objects.FORTUNE_I_MODULE.get());
                    e.accept(Objects.FORTUNE_II_MODULE.get());
                    e.accept(Objects.FORTUNE_III_MODULE.get());
                    e.accept(Objects.EFFICIENCY_I_MODULE.get());
                    e.accept(Objects.EFFICIENCY_II_MODULE.get());
                    e.accept(Objects.EFFICIENCY_III_MODULE.get());
                    e.accept(Objects.EFFICIENCY_IV_MODULE.get());
                    e.accept(Objects.EFFICIENCY_V_MODULE.get());

                })
                .build();
    }
}
