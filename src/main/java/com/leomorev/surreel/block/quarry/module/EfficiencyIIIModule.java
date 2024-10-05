package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;

public class EfficiencyIIIModule extends ModuleBlock implements QuarryModule, ModuleEnchants{
    @Override
    public EnchantmentLevel getEnchantment() {
        return new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 3);
    }

    @Override
    public ResourceLocation moduleName() {
        return Constant.EFFICIENCY.moduleName();
    }
}
