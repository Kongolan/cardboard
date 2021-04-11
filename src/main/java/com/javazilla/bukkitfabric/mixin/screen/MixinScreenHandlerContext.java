package com.javazilla.bukkitfabric.mixin.screen;

import java.util.Optional;
import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandlerContext;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ScreenHandlerContext.class)
public interface MixinScreenHandlerContext extends IMixinScreenHandlerContext {

    @Override
    default World getWorld() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    default BlockPos getPosition() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    default org.bukkit.Location getLocation() {
        return new org.bukkit.Location(((IMixinWorld)getWorld()).getWorldImpl(), getPosition().getX(), getPosition().getY(), getPosition().getZ());
    }

    /**
     * @reason Add new methods
     * @author BukkitFabric
     */
    @Overwrite
    static ScreenHandlerContext create(final World world, final BlockPos blockposition) {
        return new ScreenHandlerContext() {

            @SuppressWarnings("unused")
            public World getWorld() {
                return world;
            }

            @SuppressWarnings("unused")
            public BlockPos getPosition() {
                return blockposition;
            }

            @Override
            public <T> Optional<T> get(BiFunction<World, BlockPos, T> bifunction) {
                return Optional.of(bifunction.apply(world, blockposition));
            }
        };
    }

}