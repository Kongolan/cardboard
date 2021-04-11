package com.javazilla.bukkitfabric.mixin.world;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;

@Mixin(ServerWorldAccess.class)
public interface MixinServerWorldAccess extends WorldAccess {

    default boolean addAllEntities(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        entity.streamSelfAndPassengers().forEach((e) -> this.spawnEntity(e));
        return !entity.isRemoved();
    }

}