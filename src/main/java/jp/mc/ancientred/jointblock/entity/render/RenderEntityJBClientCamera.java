package jp.mc.ancientred.jointblock.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.entity.EntityJBClientCamera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 bound its single untyped {@code RenderEntityJointed} to {@code EntityJBClientCamera}, whose
 * {@code doRender} had no branch for it - the camera entity is an invisible seat-camera carrier and
 * drew nothing. 1.20.1 requires every entity type to have a renderer, so this is the explicit
 * no-op: the camera positions the view, it is never seen.
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityJBClientCamera extends EntityRenderer<EntityJBClientCamera> {

   public RenderEntityJBClientCamera(EntityRendererProvider.Context context) {
      super(context);
      this.shadowRadius = 0.0F;
   }

   @Override
   public void render(EntityJBClientCamera entity, float entityYaw, float partialTick, PoseStack pose,
                      MultiBufferSource bufferSource, int packedLight) {
      // Invisible by design (1.12.2 rendered nothing for this entity).
   }

   @Override
   public ResourceLocation getTextureLocation(EntityJBClientCamera entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
