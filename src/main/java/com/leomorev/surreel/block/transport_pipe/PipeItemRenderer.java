package com.leomorev.surreel.block.transport_pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PipeItemRenderer implements BlockEntityRenderer<TransportPipeBlockEntity> {
    public PipeItemRenderer(BlockEntityRendererProvider.Context context){}

    @Override
    public void render(TransportPipeBlockEntity be, float tick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int light, int overlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        List<PipeStack> stacks = be.getRenderStacks();
        float tX = 0.5f; float tY = 0.5f; float tZ = 0.5f;

        //float lerpedValue = 0;
        //for (PipeStack stack : stacks) {
        //    if (stack != PipeStack.EMPTY) {
        //        lerpedValue = stack.getPrevPosition();
        //        stack.test++;
        //        System.out.println(stack.test);
        //        break;
        //    }
        //}

        pose.pushPose();
        for(int i = 0; i < stacks.size(); ++i){
            PipeStack pipeStack = stacks.get(i);
            //float render = pipeStack.getPrevPosition();
            if(pipeStack != PipeStack.EMPTY){
                float partial = tick * 1f/20f;
                float prev = pipeStack.getPrevPosition();
                float curr = pipeStack.getPosition();
                float lerp = prev + (curr - prev) * 0.12f;
                pipeStack.setPrevPosition(lerp);
                //float render = ((pipeStack.getPosition())/ 100F) * partial;
                float render = (lerp / stacks.size() - 0.55f);
                //System.out.println(render + " = " + prev + " = " + curr + " --- " + (prev >= curr));
                //render = (float) i / stacks.size();
                //float render = (pipeStack.getPosition() / stacks.size()) - 0.55f;
                float stretch = 1.0189f;
                switch (be.getBlockState().getValue(BlockStateProperties.FACING)){
                    case NORTH -> tZ = 0.5f + -stretch * render;
                    case SOUTH -> tZ = 0.5f +  stretch * render;
                    case WEST  -> tX = 0.5f + -stretch * render;
                    case EAST  -> tX = 0.5f +  stretch * render;
                    case UP    -> tY = 0.5f +  stretch * render;
                    case DOWN  -> tY = 0.5f + -stretch * render;
                }

                pose.pushPose();
                pose.translate(tX, tY, tZ);
                //pose.translate(0.5, 0.5, 15f / 32f);
                pose.scale(0.4f, 0.4f, 0.4f);
                pose.mulPose(Axis.YP.rotationDegrees(60));
                renderer.renderStatic(pipeStack.getItemStack(),
                        ItemDisplayContext.FIXED,
                        getLightLevel(Objects.requireNonNull(be.getLevel()), be.getBlockPos()),
                        OverlayTexture.NO_OVERLAY, pose,
                        buffer, be.getLevel(), 1);
                pose.popPose();
            }
        }
        pose.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos){
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        return LightTexture.pack(blockLight, skyLight);
    }
}
