package fr.tess.vsip.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.handling.VSEntityHandler;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import qouteall.imm_ptl.core.portal.Portal;

@Pseudo
@Mixin(value = Portal.class, remap = false)
public abstract class PortalMixin implements VSEntityHandler {

    @Shadow
    public abstract Level getOriginWorld();

    @Shadow
    public abstract Level getDestWorld();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract void reloadAndSyncToClient();

    @Unique
    private Vec3 cache = new Vec3(0, 0, 0);

    @Inject(
        method = "getDestPos",
        at = @At("RETURN"),
        cancellable = true
    )
    private void fixPortalTeleportingInShipSpace(CallbackInfoReturnable<Vec3> cir) {
        if (getOriginWorld() != getDestWorld())
            return; //TODO support interdimensional teleportation
        final Ship ship = VSGameUtilsKt.getShipManagingPos(getOriginWorld(), cir.getReturnValue());
        if (ship == null)
            return;
        Vec3 newValue = VectorConversionsMCKt.toMinecraft(
            VSGameUtilsKt.toWorldCoordinates(
                ship, cir.getReturnValue().x, cir.getReturnValue().y, cir.getReturnValue().z));
        if (cache.x != newValue.x) {
           // LOGGER.warn("{} -> {}", cir.getReturnValue(), newValue);
            cache = newValue;
            reloadAndSyncToClient(); //TODO find a better solution then spamming client
        }
        cir.setReturnValue(newValue);
    }


}
