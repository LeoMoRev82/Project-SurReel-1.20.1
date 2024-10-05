package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

public class SilkTouchModule extends ModuleBlock implements QuarryModule, ModuleEnchants{
    @Override
    public EnchantmentLevel getEnchantment() {
        return new EnchantmentLevel(Enchantments.SILK_TOUCH, 1);
    }

    @Override
    public ResourceLocation moduleName() {
        return Constant.SILK_TOUCH.moduleName();
    }

    @Override
    public List<ResourceLocation> incompatibleWith() {
        return List.of(Constant.FORTUNE.moduleName());
    }

    @Override
    public boolean isStackable() {
        return false;
    }
}
