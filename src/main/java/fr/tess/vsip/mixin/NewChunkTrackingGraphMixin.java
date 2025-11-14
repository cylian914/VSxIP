package fr.tess.vsip.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.apigame.world.IPlayer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.MinecraftPlayer;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph.PlayerWatchRecord;
import qouteall.q_misc_util.MiscHelper;

@Pseudo
@Mixin(value = NewChunkTrackingGraph.class, remap = false)
public abstract class NewChunkTrackingGraphMixin {

    @Shadow
    private static Long2ObjectOpenHashMap<Object2ObjectOpenHashMap<ServerPlayer, PlayerWatchRecord>> getDimChunkWatchRecords(
        ResourceKey<Level> dimension) {
        return null;
    }

    @Inject(
        method = "updateForPlayer",
        at = @At("TAIL")
    )
    private static void addShipPlayerWatch(ServerPlayer player, CallbackInfo ci) {
        Long2ObjectOpenHashMap<Object2ObjectOpenHashMap<ServerPlayer, PlayerWatchRecord>>
            chunkRecordMap = getDimChunkWatchRecords(player.level().dimension()); //type: <ChunkPos, <player, PlayerWatch>>



    }
}
