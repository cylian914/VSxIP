package fr.tess.vsip.mixin;

import fr.tess.vsip.ShipChunkLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.mod.common.util.MinecraftPlayer;
import org.valkyrienskies.mod.common.world.ChunkManagement;

import java.util.HashMap;

@Mixin(ChunkManagement.class)
public class ChunkManagmentMixin {

    private static HashMap<ServerShip, ShipChunkLoader> shipChunkLoader = new HashMap<>();


    /**
     * @author me :3
     * @reason too lazy to mixin in kotlin
     */
    @Overwrite
    public static final void tickChunkLoading(@NotNull ServerShipWorldCore shipWorld, @NotNull MinecraftServer server) {
        var list = shipWorld.getChunkWatchTasks();
        list.getWatchTasks().forEach((chunkWatchTask) -> {
            var chunkLoader = shipChunkLoader.getOrDefault(chunkWatchTask.getShip(), new ShipChunkLoader(chunkWatchTask.getShip()));
            shipChunkLoader.put(chunkWatchTask.getShip(), chunkLoader);
            chunkLoader.addChunk(new ChunkPos(chunkWatchTask.getChunkX(), chunkWatchTask.getChunkZ()).toLong());
            chunkWatchTask.getPlayersNeedWatching().forEach((p) -> {
                var minecraftPlayer = (MinecraftPlayer)p;
                var serverPlayer = (ServerPlayer)minecraftPlayer.getPlayerEntityReference().get();
                if (serverPlayer != null) {
                    chunkLoader.addPlayer(serverPlayer);
                }
            });
        });
        list.getUnwatchTasks().forEach((chunkUnwatchTask) -> {
            var chunkLoader = shipChunkLoader.getOrDefault(chunkUnwatchTask.getShip(), new ShipChunkLoader(chunkUnwatchTask.getShip()));
            shipChunkLoader.put(chunkUnwatchTask.getShip(), chunkLoader);
            chunkLoader.removeChunk(new ChunkPos(chunkUnwatchTask.getChunkX(), chunkUnwatchTask.getChunkZ()).toLong());
            chunkUnwatchTask.getPlayersNeedUnwatching().forEach((p) -> {
                var minecraftPlayer = (MinecraftPlayer)p;
                var serverPlayer = (ServerPlayer)minecraftPlayer.getPlayerEntityReference().get();
                if (serverPlayer != null) {
                    chunkLoader.removePlayer(serverPlayer);
                }
            });
        });
        shipWorld.setExecutedChunkWatchTasks(list.getWatchTasks(), list.getUnwatchTasks());
    }
}
