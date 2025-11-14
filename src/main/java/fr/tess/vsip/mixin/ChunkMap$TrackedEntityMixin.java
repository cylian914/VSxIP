package fr.tess.vsip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(value = ChunkMap.TrackedEntity.class, priority = 1100)
public class ChunkMap$TrackedEntityMixin {

    @Shadow
    @Final
    Entity entity;

    @Unique
    private Ship inCallShip = null;

    // Changes entity position for tracking into world space if needed
    @Dynamic
    @ModifyExpressionValue(method = "updateEntityTrackingStatus", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    ChunkPos includeShips(final ChunkPos original) {
        final Ship ship = inCallShip = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) this.entity.level(), original);
        if (ship != null) {
            Vec3 chunkPosVec = VectorConversionsMCKt.toMinecraft(ship.getShipToWorld().transformPosition(
                VectorConversionsMCKt.toJOMLD(original.getBlockAt(0, 0, 0))));
            return new ChunkPos(SectionPos.blockToSectionCoord(chunkPosVec.x),SectionPos.blockToSectionCoord(chunkPosVec.z));
        } else {
            return original;
        }
    }

    @Dynamic
    @WrapOperation(method = "updateEntityTrackingStatus", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;broadcastToPlayer(Lnet/minecraft/server/level/ServerPlayer;)Z"))
    boolean skipWierdCheck(final Entity instance, final ServerPlayer serverPlayer,
        final Operation<Boolean> broadcastToPlayer) {
        return inCallShip != null || broadcastToPlayer.call(instance, serverPlayer);
    }
}
