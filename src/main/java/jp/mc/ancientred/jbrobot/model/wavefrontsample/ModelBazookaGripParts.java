package jp.mc.ancientred.jbrobot.model.wavefrontsample;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.model.obj.AbstractJBObjModel;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Bazooka grip model backed by a wavefront {@code .obj} mesh.
 *
 * <p>1.12.2 bound {@code modj_jbrobot:textures/entity/bazookatexture.png}, applied
 * {@code GL11.glTranslatef(0, scale * 24, 0)} and drew the cached display list of the baked model.
 * The 1.12.2 addon call site always passed {@code scale = 0.0625F}, so the port applies the same
 * {@code 0.0625F * 24.0F} block-unit translation to the {@link PoseStack} and re-emits the mesh every
 * frame through {@link JBRenderTypes#jointModel(ResourceLocation)} (1.20.1 has no display lists).
 */
public class ModelBazookaGripParts extends AbstractJBObjModel {
   private static final ResourceLocation objRes = new ResourceLocation("jbrobot:model/bazookagrip.obj");
   private static final ResourceLocation res = new ResourceLocation("jbrobot:textures/entity/bazookatexture.png");
   /** The scale every 1.12.2 addon render call site passed to {@code renderAll}. */
   private static final float SCALE = 0.0625F;
   public static ModelBazookaGripParts instance = new ModelBazookaGripParts();

   public ModelBazookaGripParts() {
      this.customModel = this.getBakedObjModelFromRes(objRes);
   }

   @Override
   public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(res));
      pose.translate(0.0F, SCALE * 24.0F, 0.0F);
      this.renderModel(this.customModel, pose, consumer, packedLight, packedOverlay);
   }

   @Override
   public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
      // 1.12.2 ModelBazookaGripParts#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
