package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

public class FortuneIModule extends ModuleBlock implements QuarryModule, ModuleEnchants{

    @Override
    public EnchantmentLevel getEnchantment() {
        return new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 1);
    }

    @Override
    public ResourceLocation moduleName() {
        return Constant.FORTUNE.moduleName();
    }

    @Override
    public List<ResourceLocation> incompatibleWith() {
        return List.of(Constant.SILK_TOUCH.moduleName());
    }
}
