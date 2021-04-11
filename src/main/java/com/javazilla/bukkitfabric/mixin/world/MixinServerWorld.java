package com.javazilla.bukkitfabric.mixin.world;

import java.util.List;
import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.MapInitializeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;

import net.minecraft.entity.Entity;
import net.minecraft.item.map.MapState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public class MixinServerWorld extends MixinWorld {

   // @Shadow
   // public boolean inEntityTick;

    @SuppressWarnings("rawtypes")
    @Inject(at = @At("TAIL"), method = "<init>")
    public void addToBukkit(MinecraftServer server, Executor a, LevelStorage.Session b, ServerWorldProperties c,
            RegistryKey d, DimensionType f, WorldGenerationProgressListener g, ChunkGenerator h, boolean bl, long l, List<Spawner> list, boolean bl2, CallbackInfo ci){
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getWorldImpl());
    }

    @Inject(at = @At("HEAD"), method = "save")
    public void doWorldSaveEvent(ProgressListener aa, boolean bb, boolean cc, CallbackInfo ci) {
        if (!cc) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(getWorldImpl())); // WorldSaveEvent
        }
    }

    /**
     * @reason MapInitalizeEvent
     * @author BukkitFabricMod
     */
    //@Overwrite
   // public MapState getMapState(String s) {
        // TODO 1.17ify
       // return null; return (MapState) CraftServer.INSTANCE.getServer().getOverworld().getPersistentStateManager().get(() -> {
           /*MapState newMap = new MapState(s);
            MapInitializeEvent event = new MapInitializeEvent(((IMixinMapState)newMap).getMapViewBF());
            Bukkit.getServer().getPluginManager().callEvent(event);
            return newMap;
        }, s);*/
  //  }

    // TODO 1.17ify
   /* @Inject(at = @At("TAIL"), method = "unloadEntity")
    public void unvalidateEntityBF(Entity entity, CallbackInfo ci) {
        ((IMixinEntity)entity).setValid(false);
    } 

    @Inject(at = @At("TAIL"), method = "loadEntityUnchecked")
    public void validateEntityBF(Entity entity, CallbackInfo ci) {
        //if (!this.inEntityTick) {
            IMixinEntity bf = (IMixinEntity) entity;
            bf.setValid(true);
            if (null == bf.getOriginBF() && null != bf.getBukkitEntity()) {
                // Paper's Entity Origin API
                bf.setOriginBF(bf.getBukkitEntity().getLocation());
            }
       // }
    }*/ 

}