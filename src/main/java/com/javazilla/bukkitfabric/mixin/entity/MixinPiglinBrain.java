/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(PiglinBrain.class)
public class MixinPiglinBrain {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public static void loot(PiglinEntity entitypiglin, ItemEntity entityitem) {
        stopWalking(entitypiglin);
        ItemStack itemstack;

        if (entityitem.getStack().getItem() == Items.GOLD_NUGGET && !BukkitEventFactory.callEntityPickupItemEvent(entitypiglin, entityitem, 0, false).isCancelled()) {
            entitypiglin.sendPickup(entityitem, entityitem.getStack().getCount());
            itemstack = entityitem.getStack();
            entityitem.remove(RemovalReason.DISCARDED);
        } else if (!BukkitEventFactory.callEntityPickupItemEvent(entitypiglin, entityitem, entityitem.getStack().getCount() - 1, false).isCancelled()) {
            entitypiglin.sendPickup(entityitem, 1);
            itemstack = getItemFromStack(entityitem);
        } else return;

        if (isGoldenItem(itemstack)) {
            entitypiglin.getBrain().forget(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            swapItemWithOffHand(entitypiglin, itemstack);
            setAdmiringItem((LivingEntity) entitypiglin);
        } else if (isFood(itemstack) && !hasAteRecently(entitypiglin)) {
            setEatenRecently(entitypiglin);
        } else {
            if (!entitypiglin.tryEquip(itemstack)) barterItem(entitypiglin, itemstack);
        }
    }

    // This class likes static methods
    @Shadow public static boolean isGoldenItem(ItemStack item) {return false;}
    @Shadow public static void setEatenRecently(PiglinEntity entitypiglin) {}
    @Shadow public static void setAdmiringItem(LivingEntity entityliving) {}
    @Shadow public static boolean hasAteRecently(PiglinEntity entitypiglin) {return false;}
    @Shadow public static boolean isFood(ItemStack item) {return false;}
    @Shadow public static ItemStack getItemFromStack(ItemEntity entityitem) {return null;}
    @Shadow public static void swapItemWithOffHand(PiglinEntity entitypiglin, ItemStack itemstack) {}
    @Shadow public static void stopWalking(PiglinEntity entitypiglin) {}
    @Shadow public static void barterItem(PiglinEntity entitypiglin, ItemStack itemstack) {}

}