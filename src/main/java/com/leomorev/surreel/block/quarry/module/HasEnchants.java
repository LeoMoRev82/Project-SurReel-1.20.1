package com.leomorev.surreel.block.quarry.module;

import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface HasEnchants {
    List<EnchantmentLevel> getEnchants();

    default int getFortuneLevel() {
        if(getEnchants().stream().map(EnchantmentLevel::enchantment).toList().contains(Enchantments.BLOCK_FORTUNE)){
            return getEnchants().stream().filter(n -> n.enchantment() == Enchantments.BLOCK_FORTUNE).flatMapToInt(l -> IntStream.of(l.level())).sum();
        }
        return 0;
    }

    default int getEfficiencyLevel() {
        if(getEnchants().stream().map(EnchantmentLevel::enchantment).toList().contains(Enchantments.BLOCK_EFFICIENCY)){
            return getEnchants().stream().filter(n -> n.enchantment() == Enchantments.BLOCK_EFFICIENCY).flatMapToInt(l -> IntStream.of(l.level())).sum();
        }
        return 0;
    }

    default int getSilkTouchLevel() {
        if(getEnchants().stream().map(EnchantmentLevel::enchantment).toList().contains(Enchantments.SILK_TOUCH)){
            return 1;
        }
        return 0;
    }
}
