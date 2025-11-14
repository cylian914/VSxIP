package fr.tess.vsip;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Overwrite;
import org.valkyrienskies.core.api.ships.Ship;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.chunk_loading.ChunkLoader;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import qouteall.imm_ptl.core.chunk_loading.LenientChunkRegion;
import qouteall.q_misc_util.MiscHelper;

import java.util.ArrayList;
import java.util.HashSet;


public class ShipChunkLoader extends ChunkLoader {

    public ShipChunkLoader(Ship ship) {
        super(new DimensionalChunkPos(VSGameUtilsKt.getResourceKey(ship.getChunkClaimDimension()), ship.getChunkClaim().getXMiddle(), ship.getChunkClaim().getZMiddle()), ship.getChunkClaim().getZMiddle() - ship.getChunkClaim().getZStart());
        this.ship = ship;
    }

    private Ship ship;
    private ArrayList<Long> chunks  = new ArrayList<Long>();
    private HashSet<ServerPlayer> playerSet = new HashSet<>();

    @Override
    public int getChunkNum() {
        return chunks.size();
    }

    @Override
    public void foreachChunkPos(ChunkPosConsumer func) {
        for (var c : chunks) {
            var chunkPos = new ChunkPos(c);
            func.consume(
                    VSGameUtilsKt.getResourceKey(ship.getChunkClaimDimension()),
                    chunkPos.x,
                    chunkPos.z,
                    32);
        }
    }

    @Override
    public LenientChunkRegion createChunkRegion() {
        var level = VSGameUtilsKt.getLevelFromDimensionId(MiscHelper.getServer(),ship.getChunkClaimDimension());
        if (level == null)
            return super.createChunkRegion();
        var chunkList = new ArrayList<ChunkAccess>();
        for (var c : chunks) {
            var cPos = new ChunkPos(c);
            chunkList.add(level.getChunk(cPos.x, cPos.z));
        }
        return new LenientChunkRegion(level, chunkList);
    }

    public void addChunk(Long l) {
        chunks.add(l);
    }

    public void removeChunk(Long l) {
        chunks.remove(l);
    }

    public void addPlayer(ServerPlayer serverPlayer ) {
        if (playerSet.contains(serverPlayer))
            return;
        playerSet.add(serverPlayer);
        PortalAPI.addChunkLoaderForPlayer(serverPlayer, this);
    }

    public void removePlayer(ServerPlayer serverPlayer) {
        playerSet.remove(serverPlayer);
    }
}
