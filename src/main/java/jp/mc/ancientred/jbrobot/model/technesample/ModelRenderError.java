package jp.mc.ancientred.jbrobot.model.technesample;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fallback "error"/"penalty" box model, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.model.technesample.ModelRenderError}.
 *
 * <p>1.12.2 derived from {@code ModelBase} and rendered one 16x16x16 {@code ModelRenderer} box trough
 * {@code ModelRenderer#render(scale)} after binding {@code error.png}. The 1.20.1 port uses
 * {@link JBModelRenderer} (the {@code ModelBase}/{@code ModelRenderer} replacement) and draws through
 * {@link JBRenderTypes#jointModel(ResourceLocation)}; the {@code texOffsetV} constructor argument
 * selects the vertical texture offset (0 for {@code instanceError}, 32 for {@code instancePenalty}).
 */
public class ModelRenderError implements IJBModelItem.IJBModel {
   public static final ModelRenderError instanceError = new ModelRenderError(0);
   public static final ModelRenderError instancePenalty = new ModelRenderError(32);
   private static final ResourceLocation errRes = new ResourceLocation("jbrobot:textures/entity/error.png");
   JBModelRenderer Shape1;

   public ModelRenderError(int texOffsetV) {
      this.Shape1 = new JBModelRenderer(0, texOffsetV);
      this.Shape1.addBox(0.0F, 0.0F, 0.0F, 16, 16, 16);
      this.Shape1.setRotationPoint(-8.0F, 8.0F, -8.0F);
      this.Shape1.setTextureSize(64, 64);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   @Override
   public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(errRes));
      this.Shape1.render(pose, consumer, packedLight, packedOverlay);
   }

   @Override
   public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
      // 1.12.2 ModelRenderError#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
