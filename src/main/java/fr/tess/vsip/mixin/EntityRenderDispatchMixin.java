package fr.tess.vsip.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import qouteall.imm_ptl.core.render.CrossPortalEntityRenderer;

//TODO find a way for it to not load when IP isn't here
@Mixin(value = EntityRenderDispatcher.class, priority = 900)
public class EntityRenderDispatchMixin {
    @ModifyReturnValue(
        method = "shouldRender",
        at = @At("RETURN")
    )
    private boolean shouldRenderCrossPortalEntityRenderer(final boolean returns, final Entity entity, final Frustum frustum,
        final double camX, final double camY, final double camZ) {
        return CrossPortalEntityRenderer.shouldRenderEntityNow(entity);
    }
}
