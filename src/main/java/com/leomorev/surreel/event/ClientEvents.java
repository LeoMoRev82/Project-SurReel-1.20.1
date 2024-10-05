package com.leomorev.surreel.event;

import com.leomorev.surreel.Objects;
import com.leomorev.surreel.SurReelMain;
import com.leomorev.surreel.block.transport_pipe.PipeItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SurReelMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(Objects.TRANSPORT_PIPE_BLOCK_ENTITY.get(), PipeItemRenderer::new);
    }
}
