package com.leomorev.surreel.block.quarry.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record EnchantmentLevel(Enchantment enchantment, int level) {

    public EnchantmentLevel(ResourceLocation name, int level) {
        this(ForgeRegistries.ENCHANTMENTS.getValue(name), level);
    }

    @Nullable
    public ResourceLocation getName() {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment());
    }

    public String getNameLevel(){
        String name = getName().toString();
        int level = level();
        if(level > 1){
            return name + " + " + level;
        }
        return name;
    }

    public static List<EnchantmentLevel> breakDownSilkTouch(List<EnchantmentLevel> silk) {
        int count = (int)silk.stream().filter(n -> n.level() == 1).count();
        silk.clear();
        if(count >= 1){return List.of(new EnchantmentLevel(Enchantments.SILK_TOUCH, 1));}
        return Collections.emptyList();
    }

    public static List<EnchantmentLevel> breakDownFortune(List<EnchantmentLevel> fortune) {
        int fLevel1 = (int)fortune.stream().filter(n -> n.level() == 1).count();
        int fLevel2 = (int)fortune.stream().filter(n -> n.level() == 2).count();
        int fLevel3 = (int)fortune.stream().filter(n -> n.level() == 3).count();

        int temp1 = fLevel1 / 2;
        if(fLevel1 >= 2) {
            fLevel2 +=temp1;
            fLevel1 -= 2 * fLevel2;
        }

        int temp2 = fLevel2 / 2;
        if(fLevel2 >= 2) {
            fLevel3 +=temp2;
            fLevel2 -= 2 * fLevel3;
        }

        fortune.clear();
        if(fLevel3 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 3));}
        if(fLevel2 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 2));}
        if(fLevel1 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 1));}
        return Collections.emptyList();
    }

    public static List<EnchantmentLevel> breakDownEfficiency(List<EnchantmentLevel> efficiency) {
        int fLevel1 = (int)efficiency.stream().filter(n -> n.level() == 1).count();
        int fLevel2 = (int)efficiency.stream().filter(n -> n.level() == 2).count();
        int fLevel3 = (int)efficiency.stream().filter(n -> n.level() == 3).count();
        int fLevel4 = (int)efficiency.stream().filter(n -> n.level() == 4).count();
        int fLevel5 = (int)efficiency.stream().filter(n -> n.level() == 5).count();

        if(fLevel1 >= 2) {
            fLevel2 += fLevel1 / 2;
            fLevel1 -= 2 * fLevel2;
        }

        if(fLevel2 >= 2) {
            fLevel3 += fLevel2 / 2;
            fLevel2 -= 2 * fLevel3;
        }

        if(fLevel3 >= 2) {
            fLevel4 += fLevel3 / 2;
            fLevel3 -= 2 * fLevel4;
        }

        if(fLevel4 >= 2) {
            fLevel5 += fLevel4 / 2;
            fLevel4 -= 2 * fLevel5;
        }

        efficiency.clear();
        if(fLevel5 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 5));}
        if(fLevel4 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 4));}
        if(fLevel3 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 3));}
        if(fLevel2 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 2));}
        if(fLevel1 >= 1){return List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1));}
        return Collections.emptyList();
    }
}
