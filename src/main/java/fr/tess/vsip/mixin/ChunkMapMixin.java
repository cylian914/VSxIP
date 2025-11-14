package fr.tess.vsip.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(value = ChunkMap.class, priority = 1100)
public class ChunkMapMixin {
    @Unique
    private boolean pass = false;

    @Dynamic
    @Redirect(
        method = "ip_updateEntityTrackersAfterSendingChunkPacket", remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;chunkPosition()Lnet/minecraft/world/level/ChunkPos;")
    )
    private ChunkPos logChunkSend(Entity instance, LevelChunk chunk, ServerPlayer player) {
        if (VSGameUtilsKt.getShipManagingPos(instance.level(), instance.blockPosition()) != null && VSGameUtilsKt.isChunkInShipyard(chunk.getLevel(), chunk.getPos().x, chunk.getPos().z)) {
            pass = true;
        }
        return instance.chunkPosition();
    }

    @Dynamic
    @Redirect(
        method = "ip_updateEntityTrackersAfterSendingChunkPacket", remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;equals(Ljava/lang/Object;)Z")
    )
    private boolean compareEntityChunkPos(ChunkPos instance, Object pos) {
        if (pass) {
            pass = false;
            return true;
        }
        return instance.equals(pos);
    }
}
