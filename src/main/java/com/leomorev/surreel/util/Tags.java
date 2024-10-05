package com.leomorev.surreel.util;

import com.leomorev.surreel.SurReelMain;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class Tags extends BlockTagsProvider {
    public Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SurReelMain.MODID, existingFileHelper);
    }

    public static final TagKey<Block> MINESHAFT_WALL_FILL   = BlockTags.create(new ResourceLocation(SurReelMain.MODID, "mineshaft_builder/mineshaft_wall_fill"));
    public static final TagKey<Block> MINESHAFT_SUPPORT     = BlockTags.create(new ResourceLocation(SurReelMain.MODID, "mineshaft_builder/mineshaft_support"));
    public static final TagKey<Block> MINESHAFT_PLATFORM    = BlockTags.create(new ResourceLocation(SurReelMain.MODID, "mineshaft_builder/mineshaft_platform"));

    @Override
    protected void addTags(HolderLookup.@NotNull Provider p_256380_) {
        this.tag(MINESHAFT_WALL_FILL)
                .addTag(BlockTags.WOOL).addTag(BlockTags.PLANKS).addTag(BlockTags.LOGS).addTag(BlockTags.DIRT).addTag(BlockTags.TERRACOTTA).addTag(BlockTags.STONE_BRICKS)
                .addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES).addTag(BlockTags.BASE_STONE_NETHER).addTag(BlockTags.NETHER_CARVER_REPLACEABLES)
                .add(Blocks.GLASS).add(Blocks.TINTED_GLASS).add(Blocks.WHITE_STAINED_GLASS).add(Blocks.LIGHT_GRAY_STAINED_GLASS).add(Blocks.GRAY_STAINED_GLASS).add(Blocks.BLACK_STAINED_GLASS).add(Blocks.BROWN_STAINED_GLASS).add(Blocks.RED_STAINED_GLASS).add(Blocks.ORANGE_STAINED_GLASS).add(Blocks.YELLOW_STAINED_GLASS).add(Blocks.LIME_STAINED_GLASS).add(Blocks.GREEN_STAINED_GLASS).add(Blocks.CYAN_STAINED_GLASS).add(Blocks.LIGHT_BLUE_STAINED_GLASS).add(Blocks.BLUE_STAINED_GLASS).add(Blocks.PURPLE_STAINED_GLASS).add(Blocks.MAGENTA_STAINED_GLASS).add(Blocks.PINK_STAINED_GLASS)
                .add(Blocks.WHITE_CONCRETE).add(Blocks.LIGHT_GRAY_CONCRETE).add(Blocks.GRAY_CONCRETE).add(Blocks.BLACK_CONCRETE).add(Blocks.BROWN_CONCRETE).add(Blocks.RED_CONCRETE).add(Blocks.ORANGE_CONCRETE).add(Blocks.YELLOW_CONCRETE).add(Blocks.LIME_CONCRETE).add(Blocks.GREEN_CONCRETE).add(Blocks.CYAN_CONCRETE).add(Blocks.LIGHT_BLUE_CONCRETE).add(Blocks.BLUE_CONCRETE).add(Blocks.PURPLE_CONCRETE).add(Blocks.MAGENTA_CONCRETE).add(Blocks.PINK_CONCRETE)
                .add(Blocks.GLOWSTONE).add(Blocks.SEA_LANTERN).add(Blocks.SHROOMLIGHT).add(Blocks.JACK_O_LANTERN).add(Blocks.OCHRE_FROGLIGHT).add(Blocks.PEARLESCENT_FROGLIGHT).add(Blocks.VERDANT_FROGLIGHT).add(Blocks.MAGMA_BLOCK)
                .add(Blocks.SLIME_BLOCK).add(Blocks.HONEY_BLOCK).add(Blocks.HONEYCOMB_BLOCK).add(Blocks.GLOWSTONE).add(Blocks.SPONGE).add(Blocks.HAY_BLOCK).add(Blocks.MELON).add(Blocks.PUMPKIN).add(Blocks.CARVED_PUMPKIN)
                .add(Blocks.COBBLESTONE).add(Blocks.MOSSY_COBBLESTONE).add(Blocks.SMOOTH_STONE).add(Blocks.OBSIDIAN).add(Blocks.PRISMARINE).add(Blocks.PRISMARINE_BRICKS).add(Blocks.DARK_PRISMARINE).add(Blocks.COBBLED_DEEPSLATE).add(Blocks.DEEPSLATE_BRICKS).add(Blocks.CRACKED_DEEPSLATE_BRICKS).add(Blocks.DEEPSLATE_TILES).add(Blocks.CRACKED_DEEPSLATE_TILES).add(Blocks.POLISHED_DEEPSLATE).add(Blocks.CHISELED_DEEPSLATE)
                .add(Blocks.SMOOTH_SANDSTONE).add(Blocks.CUT_SANDSTONE).add(Blocks.CHISELED_SANDSTONE).add(Blocks.SMOOTH_RED_SANDSTONE).add(Blocks.CUT_RED_SANDSTONE).add(Blocks.CHISELED_RED_SANDSTONE)
                .add(Blocks.NETHER_BRICKS).add(Blocks.CRACKED_NETHER_BRICKS).add(Blocks.RED_NETHER_BRICKS).add(Blocks.SMOOTH_BASALT).add(Blocks.POLISHED_BASALT)
                .add(Blocks.CLAY).add(Blocks.BRICKS).add(Blocks.PACKED_MUD).add(Blocks.MUD_BRICKS).add(Blocks.DRIPSTONE_BLOCK).add(Blocks.BONE_BLOCK).add(Blocks.BLUE_ICE).add(Blocks.PURPUR_BLOCK).add(Blocks.PURPUR_PILLAR)
                .add(Blocks.POLISHED_GRANITE).add(Blocks.POLISHED_ANDESITE).add(Blocks.POLISHED_DIORITE)
                .add(Blocks.CHISELED_POLISHED_BLACKSTONE).add(Blocks.POLISHED_BLACKSTONE).add(Blocks.POLISHED_BLACKSTONE_BRICKS).add(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        this.tag(MINESHAFT_SUPPORT)
                .addTag(MINESHAFT_WALL_FILL);
        this.tag(MINESHAFT_PLATFORM)
                .addTag(BlockTags.STAIRS).addTag(BlockTags.SLABS).addTag(BlockTags.FENCES).addTag(BlockTags.WALLS)
                .add(Blocks.IRON_BARS).add(Blocks.CHAIN).add(Blocks.TORCH).add(Blocks.SOUL_TORCH).add(Blocks.SCAFFOLDING)
                .add(Blocks.GLASS_PANE).add(Blocks.WHITE_STAINED_GLASS_PANE).add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE).add(Blocks.GRAY_STAINED_GLASS_PANE).add(Blocks.BLACK_STAINED_GLASS_PANE).add(Blocks.BROWN_STAINED_GLASS_PANE).add(Blocks.RED_STAINED_GLASS_PANE).add(Blocks.ORANGE_STAINED_GLASS_PANE).add(Blocks.YELLOW_STAINED_GLASS_PANE).add(Blocks.LIME_STAINED_GLASS_PANE).add(Blocks.GREEN_STAINED_GLASS_PANE).add(Blocks.CYAN_STAINED_GLASS_PANE).add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE).add(Blocks.BLUE_STAINED_GLASS_PANE).add(Blocks.PURPLE_STAINED_GLASS_PANE).add(Blocks.MAGENTA_STAINED_GLASS_PANE).add(Blocks.PINK_STAINED_GLASS_PANE);


    }
}
