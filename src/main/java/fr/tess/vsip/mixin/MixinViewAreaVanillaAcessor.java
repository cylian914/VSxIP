package fr.tess.vsip.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ViewArea.class, priority = 1100)
public interface MixinViewAreaVanillaAcessor {
    @Dynamic
    @Accessor(value = "vs$shipRenderChunks", remap = false)
    Long2ObjectMap<ChunkRenderDispatcher.RenderChunk[]> getVs$shipRenderChunks();

    @Dynamic
    @Accessor(value = "vs$chunkBuilder", remap = false)
    ChunkRenderDispatcher getVs$chunkBuilder();

    @Accessor(value = "level", remap = false)
    Level getLevel();

    @Accessor(value = "chunkGridSizeY", remap = false)
    int getChunkGridSizeY();
}
