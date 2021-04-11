/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
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
package org.cardboardpowered.impl.world;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.javazilla.bukkitfabric.interfaces.IMixinWorldChunk;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.ChunkRandom;

public class CardboardChunk implements Chunk {
    
    private WeakReference<net.minecraft.world.chunk.WorldChunk> weakChunk;
    private final ServerWorld worldServer;
    private final int x;
    private final int z;
    private static final PalettedContainer<net.minecraft.block.BlockState> emptyBlockIDs = new ChunkSection(0).getContainer();
    private static final byte[] emptyLight = new byte[2048];

    public CardboardChunk(net.minecraft.world.chunk.WorldChunk chunk) {
        this.weakChunk = new WeakReference<>(chunk);

        worldServer = (ServerWorld) getHandle().getWorld();
        x = getHandle().getPos().x;
        z = getHandle().getPos().z;
    }

    @Override
    public WorldImpl getWorld() {
        return ((IMixinWorld)worldServer.toServerWorld()).getWorldImpl();
    }

    public net.minecraft.world.chunk.WorldChunk getHandle() {
        net.minecraft.world.chunk.WorldChunk c = weakChunk.get();

        if (c == null) {
            c = worldServer.getChunk(x, z);
            weakChunk = new WeakReference<>(c);
        }
        return c;
    }

    void breakLink() {
        weakChunk.clear();
    }

    @Override
    public int getX() {
        return getHandle().getPos().x;
    }

    @Override
    public int getZ() {
        return getHandle().getPos().z;
    }

    @Override
    public String toString() {
        return "BukkitChunk{" + "x=" + getX() + "z=" + getZ() + '}';
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new CraftBlock(worldServer, new BlockPos((this.x << 4) | x, y, (this.z << 4) | z));
    }

    @Override
    public Entity[] getEntities() {
        if (!isLoaded()) getWorld().getChunkAt(x, z);
        int count = 0, index = 0;
        ArrayList<Entity> list = new ArrayList<>();
        for (Entity e : getWorld().getEntities()) {
            if (e.getChunk() == this) {
                count++;
                list.add(e);
            }
        }
        return list.toArray(new Entity[list.size()]);

        /*
        net.minecraft.world.chunk.WorldChunk chunk = getHandle();

        for (int i = 0; i < 16; i++)
            count += ((IMixinWorldChunk)(Object)chunk).getEntitySections()[i].size();

        Entity[] entities = new Entity[count];

        for (int i = 0; i < 16; i++) {
            for (Object obj : ((IMixinWorldChunk)(Object)chunk).getEntitySections()[i].toArray()) {
                if (!(obj instanceof net.minecraft.entity.Entity)) continue;
                entities[index++] = ((IMixinEntity)(Object)((net.minecraft.entity.Entity) obj)).getBukkitEntity();
            }
        }
        return entities;*/
    }

    @Override
    public BlockState[] getTileEntities() {
        if (!isLoaded()) getWorld().getChunkAt(x, z);

        int index = 0;
        net.minecraft.world.chunk.WorldChunk chunk = getHandle();

        BlockState[] entities = new BlockState[chunk.getBlockEntities().size()];

        for (Object obj : chunk.getBlockEntities().keySet().toArray()) {
            if (!(obj instanceof BlockPos)) continue;

            BlockPos position = (BlockPos) obj;
            entities[index++] = ((IMixinWorld)(Object)worldServer).getWorldImpl().getBlockAt(position.getX(), position.getY(), position.getZ()).getState();
        }

        return entities;
    }

    @Override
    public boolean isLoaded() {
        return getWorld().isChunkLoaded(this);
    }

    @Override
    public boolean load() {
        return getWorld().loadChunk(getX(), getZ(), true);
    }

    @Override
    public boolean load(boolean generate) {
        return getWorld().loadChunk(getX(), getZ(), generate);
    }

    @Override
    public boolean unload() {
        return getWorld().unloadChunk(getX(), getZ());
    }

    @Override
    public boolean isSlimeChunk() {
        return ChunkRandom.getSlimeRandom(getX(), getZ(), getWorld().getSeed(), 987234911L).nextInt(10) == 0;
    }

    @Override
    public boolean unload(boolean save) {
        return getWorld().unloadChunk(getX(), getZ(), save);
    }

    @Override
    public boolean isForceLoaded() {
        return getWorld().isChunkForceLoaded(getX(), getZ());
    }

    @Override
    public void setForceLoaded(boolean forced) {
        getWorld().setChunkForceLoaded(getX(), getZ(), forced);
    }

    @Override
    public boolean addPluginChunkTicket(Plugin plugin) {
        return getWorld().addPluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public boolean removePluginChunkTicket(Plugin plugin) {
        return getWorld().removePluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets() {
        return getWorld().getPluginChunkTickets(getX(), getZ());
    }

    @Override
    public long getInhabitedTime() {
        return getHandle().getInhabitedTime();
    }

    @Override
    public void setInhabitedTime(long ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        getHandle().setInhabitedTime(ticks);
    }

    @Override
    public boolean contains(BlockData block) {
        Preconditions.checkArgument(block != null, "Block cannot be null");

        Predicate<net.minecraft.block.BlockState> nms = Predicates.equalTo(((CraftBlockData) block).getState());
        for (ChunkSection section : getHandle().getSectionArray())
            if (section != null && section.getContainer().hasAny(nms)) return true;
        return false;
    }

    @Override
    public ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.world.chunk.WorldChunk chunk = getHandle();

        ChunkSection[] cs = chunk.getSectionArray();
        PalettedContainer[] sectionBlockIDs = new PalettedContainer[cs.length];
        byte[][] sectionSkyLights = new byte[cs.length][];
        byte[][] sectionEmitLights = new byte[cs.length][];
        boolean[] sectionEmpty = new boolean[cs.length];

        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == null) { // Section is empty?
                sectionBlockIDs[i] = emptyBlockIDs;
                sectionSkyLights[i] = emptyLight;
                sectionEmitLights[i] = emptyLight;
                sectionEmpty[i] = true;
            } else { // Not empty
                NbtCompound data = new NbtCompound();
                cs[i].getContainer().write(data, "Palette", "BlockStates");

                PalettedContainer<net.minecraft.block.BlockState> blockids = new PalettedContainer<>(ChunkSection.PALETTE, net.minecraft.block.Block.STATE_IDS, NbtHelper::toBlockState, NbtHelper::fromBlockState, Blocks.AIR.getDefaultState()); // TODO: snapshot whole ChunkSection
                blockids.read(data.getList("Palette", CraftMagicNumbers.NBT.TAG_COMPOUND), data.getLongArray("BlockStates"));

                sectionBlockIDs[i] = blockids;

                LightingProvider lightengine = chunk.world.getChunkManager().getLightingProvider();
                ChunkNibbleArray skyLightArray = lightengine.get(LightType.SKY).getLightSection(ChunkSectionPos.from(x, i, z));
                if (skyLightArray == null)
                    sectionSkyLights[i] = emptyLight;
                else {
                    sectionSkyLights[i] = new byte[2048];
                    System.arraycopy(skyLightArray.asByteArray(), 0, sectionSkyLights[i], 0, 2048);
                }
                ChunkNibbleArray emitLightArray = lightengine.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(x, i, z));
                if (emitLightArray == null)
                    sectionEmitLights[i] = emptyLight;
                else {
                    sectionEmitLights[i] = new byte[2048];
                    System.arraycopy(emitLightArray.asByteArray(), 0, sectionEmitLights[i], 0, 2048);
                }
            }
        }

        Heightmap hmap = null;

        if (includeMaxBlockY) {
            hmap = new Heightmap(null, Heightmap.Type.MOTION_BLOCKING);
            hmap.setTo(chunk.heightmaps.get(Heightmap.Type.MOTION_BLOCKING).asLongArray());
        }

        BiomeArray biome = null;
        if (includeBiome || includeBiomeTempRain)
            biome = chunk.getBiomeArray();

        World world = getWorld();
        return new CardboardChunkSnapshot(getX(), getZ(), world.getName(), world.getFullTime(), sectionBlockIDs, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, biome);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ChunkSnapshot getEmptyChunkSnapshot(int x, int z, WorldImpl world, boolean includeBiome, boolean includeBiomeTempRain) {
        BiomeArray biome = null;

        if (includeBiome || includeBiomeTempRain)
            biome = new BiomeArray(((ServerWorld)world.getHandle()).getRegistryManager().get(Registry.BIOME_KEY), world.getHandle(), new ChunkPos(x, z), ((ServerWorld)world.getHandle()).getChunkManager().getChunkGenerator().getBiomeSource());

        // Fill with empty data
        int hSection = world.getMaxHeight() >> 4;
        PalettedContainer[] blockIDs = new PalettedContainer[hSection];
        byte[][] skyLight = new byte[hSection][];
        byte[][] emitLight = new byte[hSection][];
        boolean[] empty = new boolean[hSection];

        for (int i = 0; i < hSection; i++) {
            blockIDs[i] = emptyBlockIDs;
            skyLight[i] = emptyLight;
            emitLight[i] = emptyLight;
            empty[i] = true;
        }

        return new CardboardChunkSnapshot(x, z, world.getName(), world.getFullTime(), blockIDs, skyLight, emitLight, empty, new Heightmap(null, Heightmap.Type.MOTION_BLOCKING), biome);
    }

    static void validateChunkCoordinates(int x, int y, int z) {
        Preconditions.checkArgument(0 <= x && x <= 15, "x out of range (expected 0-15, got %s)", x);
        Preconditions.checkArgument(0 <= y && y <= 255, "y out of range (expected 0-255, got %s)", y);
        Preconditions.checkArgument(0 <= z && z <= 15, "z out of range (expected 0-15, got %s)", z);
    }

    public PersistentDataContainer getPersistentDataContainer() {
        // Added in Bukkit 1.16.3 API (Spigot Pull #672)
        return null;
    }

    static {
        Arrays.fill(emptyLight, (byte) 0xFF);
    }

    @Override
    public BlockState[] getTileEntities(boolean arg0) {
        Map<BlockPos,BlockEntity> map = getHandle().getBlockEntities();
        BlockState[] bk = new BlockState[map.size()];
        int i = 0;
        for (BlockEntity e : map.values()) {
            bk[i] = CraftBlockState.getBlockState(this.worldServer, e.getPos());
            i++;
        }
        return null;
    }

    @Override
    public Collection<BlockState> getTileEntities(Predicate<Block> arg0, boolean arg1) {
        return null;
    }

}