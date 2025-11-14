package fr.tess.vsip.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixin.mod_compat.vanilla_renderer.MixinViewAreaVanilla;
import qouteall.imm_ptl.core.render.MyBuiltChunkStorage;

@Pseudo
@Mixin(value = MyBuiltChunkStorage.class, remap = false)
public class MyBuiltChunkStorageMixin {
    @Unique
    public ChunkRenderDispatcher.RenderChunk getShipChunkRender(int chunkX, int chunkY, int chunkZ) {
        return getShipChunkRender(ChunkPos.asLong(chunkX, chunkZ), chunkY);
    }

    @Unique
    public ChunkRenderDispatcher.RenderChunk getShipChunkRender(long chunkPosAsLong, int chunkY) {
        final ChunkRenderDispatcher.RenderChunk[] renderChunksArray = ((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().get(chunkPosAsLong);
        if (renderChunksArray == null) {
            return null;
        }
        return renderChunksArray[chunkY];
    }

    /**
     * This mixin creates render chunks for ship chunks.
     */
    @Inject(method = "setDirty", at = @At("HEAD"), cancellable = true)
    private void preScheduleRebuild(final int x, final int y, final int z, final boolean important,
        final CallbackInfo callbackInfo) {

        final int yIndex = y - ((MixinViewAreaVanillaAcessor)this).getLevel().getMinSection();

        if (yIndex < 0 || yIndex >=  ((MixinViewAreaVanillaAcessor)this).getChunkGridSizeY()) {
            return; // Weird, but just ignore it
        }

        if (VSGameUtilsKt.isChunkInShipyard(((MixinViewAreaVanillaAcessor)this).getLevel(), x, z)) {
            final long chunkPosAsLong = ChunkPos.asLong(x, z);
            final ChunkRenderDispatcher.RenderChunk[] renderChunksArray =
               ((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().computeIfAbsent(chunkPosAsLong,
                    k -> new ChunkRenderDispatcher.RenderChunk[ ((MixinViewAreaVanillaAcessor)this).getChunkGridSizeY()]);

            if (renderChunksArray[yIndex] == null) {
                final ChunkRenderDispatcher.RenderChunk builtChunk =
                    ((MixinViewAreaVanillaAcessor)this).getVs$chunkBuilder().new RenderChunk(0, x << 4, y << 4, z << 4);
                renderChunksArray[yIndex] = builtChunk;
            }

            renderChunksArray[yIndex].setDirty(important);

            callbackInfo.cancel();
        }
    }

    /**
     * This mixin allows {@link ViewArea} to return the render chunks for ships.
     */
    @Inject(method = "getRenderChunkAt", at = @At("HEAD"), cancellable = true)
    private void preGetRenderedChunk(final BlockPos pos,
        final CallbackInfoReturnable<RenderChunk> callbackInfoReturnable) {
        final int chunkX = Mth.floorDiv(pos.getX(), 16);
        final int chunkY = Mth.floorDiv(pos.getY() - ((MixinViewAreaVanillaAcessor)this).getLevel().getMinBuildHeight(), 16);
        final int chunkZ = Mth.floorDiv(pos.getZ(), 16);

        if (chunkY < 0 || chunkY >=  ((MixinViewAreaVanillaAcessor)this).getChunkGridSizeY()) {
            return; // Weird, but ignore it
        }

        if (VSGameUtilsKt.isChunkInShipyard(((MixinViewAreaVanillaAcessor)this).getLevel(), chunkX, chunkZ)) {
            callbackInfoReturnable.setReturnValue(getShipChunkRender(chunkX, chunkY, chunkZ));
        }
    }

    /**
     * Clear VS ship render chunks so that we don't leak memory
     */
    @Inject(method = "releaseAllBuffers", at = @At("HEAD"))
    private void postReleaseAllBuffers(final CallbackInfo ci) {
        for (final Entry<RenderChunk[]> entry : ((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().long2ObjectEntrySet()) {
            for (final ChunkRenderDispatcher.RenderChunk renderChunk : entry.getValue()) {
                if (renderChunk != null) {
                    renderChunk.releaseBuffers();
                }
            }
        }
        ((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().clear();
    }

    @Redirect(
        method = "isRegionActive",
        at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;containsKey(J)Z")
    )
    private boolean setRegionActive(Long2ObjectOpenHashMap instance, long k) {
        if (((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().containsKey(k))
            return true;
        return instance.containsKey(k);
    }

    @Inject(
        method = "onChunkUnload",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onChunkUnloaded(int chunkX, int chunkZ, CallbackInfo ci) {
        if (VSGameUtilsKt.isChunkInShipyard(((MixinViewAreaVanillaAcessor)this).getLevel(), chunkX, chunkZ)) {
            final ChunkRenderDispatcher.RenderChunk[] chunks =
                    ((MixinViewAreaVanillaAcessor)this).getVs$shipRenderChunks().remove(ChunkPos.asLong(chunkX, chunkZ));
            if (chunks != null) {
                for (final ChunkRenderDispatcher.RenderChunk chunk : chunks) {
                    if (chunk != null) {
                        chunk.releaseBuffers();
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(
        method = "rawFetch",
        at = @At("HEAD"),
        cancellable = true
    )
    private void returnVSChunkFetch(int chunkX, int chunkY, int chunkZ, long timeMark, CallbackInfoReturnable<RenderChunk> cir) {
        if (chunkY < 0 || chunkY >=  ((MixinViewAreaVanillaAcessor)this).getChunkGridSizeY()) {
            return; // Weird, but ignore it
        }
        if (VSGameUtilsKt.isChunkInShipyard(((MixinViewAreaVanillaAcessor)this).getLevel(), chunkX, chunkZ)) {
            cir.setReturnValue(getShipChunkRender(chunkX, chunkY, chunkZ));
        }
    }

    @Inject(
        method = "rawGet",
        at = @At("HEAD"),
        cancellable = true
    )
    private void returnVSChunkGet(int chunkX, int chunkY, int chunkZ, CallbackInfoReturnable<RenderChunk> cir) {
        if (chunkY < 0 || chunkY >=  ((MixinViewAreaVanillaAcessor)this).getChunkGridSizeY()) {
            return; // Weird, but ignore it
        }
        if (VSGameUtilsKt.isChunkInShipyard(((MixinViewAreaVanillaAcessor)this).getLevel(), chunkX, chunkZ)) {
            cir.setReturnValue(getShipChunkRender(chunkX, chunkY, chunkZ));
        }
    }
}
