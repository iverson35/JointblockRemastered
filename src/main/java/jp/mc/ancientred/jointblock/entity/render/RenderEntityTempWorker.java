package jp.mc.ancientred.jointblock.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 registered {@code RenderEntityBullet} (an untyped {@code Render}) for
 * {@link EntityTempWorker} as well; 1.20.1 entity renderers are typed, so the temp-worker branch
 * lives in {@link RenderEntityBullet#renderTempWorker} and this class only supplies the type.
 *
 * <p>The worker has no texture of its own - the 1.12.2 renderer drew the blade trail / flamer flame
 * with untextured or block-atlas geometry - so {@link #getTextureLocation} returns the block atlas,
 * matching {@code RenderEntityDisassembleBall}.
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityTempWorker extends EntityRenderer<EntityTempWorker> {

   public RenderEntityTempWorker(EntityRendererProvider.Context context) {
      super(context);
      // 1.12.2 func_76979_b (doRenderShadowAndFire) was an empty override.
      this.shadowRadius = 0.0F;
   }

   @Override
   public void render(EntityTempWorker entity, float entityYaw, float partialTick, PoseStack pose,
                      MultiBufferSource bufferSource, int packedLight) {
      RenderEntityBullet.renderTempWorker(entity, partialTick, pose, bufferSource, packedLight);
   }

   @Override
   public ResourceLocation getTextureLocation(EntityTempWorker entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
