package fr.tess.vsip.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import qouteall.imm_ptl.core.render.CrossPortalEntityRenderer;

@Pseudo
@Mixin(CrossPortalEntityRenderer.class)
public class CrossPortalEntityRendererMixin {

    @Unique
    private static boolean shouldCancel = false;

    @WrapMethod(
        method = "shouldRenderEntityNow"
    )
    private static boolean shouldRenderEntity(Entity entity, Operation<Boolean> original) {
        return true;
       /* shouldCancel = !shouldCancel;
        return shouldCancel || original.call(entity);*/
    }

}
