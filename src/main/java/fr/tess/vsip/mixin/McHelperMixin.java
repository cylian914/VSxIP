package fr.tess.vsip.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.McHelper;

@Pseudo
@Mixin(value = McHelper.class, remap = false)
public class McHelperMixin {
    @Redirect(
        method = "traverseEntities",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/commons/lang3/Validate;isTrue(ZLjava/lang/String;[Ljava/lang/Object;)V")
    )
    private static void disableStupid1000ChunkTeleportLimit(boolean expression, String message, Object[] values) {

    }
}
