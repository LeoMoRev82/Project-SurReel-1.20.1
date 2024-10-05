package com.leomorev.surreel;

import com.leomorev.surreel.block.mineshaft_builder.MineshaftBuilderScreen;

import com.leomorev.surreel.util.DataGen;
import com.leomorev.surreel.util.Networking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SurReelMain.MODID)
public class SurReelMain {
	public static final String MODID = "surreel";


	public SurReelMain(){
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::clientSetup);

		Objects.register(bus);

		bus.addListener(DataGen::register);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event){
		event.enqueueWork(() -> {
		});
		Networking.register();
	}
	
	private void clientSetup(final FMLClientSetupEvent event){
		MenuScreens.register(Objects.MINESHAFT_BUILDER_MENU.get(), MineshaftBuilderScreen::new);
	}
}
