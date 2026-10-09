package jp.mc.ancientred.jointblock.model.tuning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.model.obj.AbstractJBObjModel;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tuning blade item model backed by a wavefront {@code .obj} mesh.
 *
 * <p>1.12.2 {@code renderAll(int, float, int)} bound the blade texture with
 * {@code TextureManager.bindTexture}, applied {@code GL11.glTranslatef(0, scale * 24, 0)} and then
 * drew the cached display list of the baked model. The 1.12.2 tuning call site always passed
 * {@code scale = 0.0625F}, so the port applies the identical {@code 0.0625F * 24.0F = 1.5} block unit
 * translation to the {@link PoseStack} and re-emits the mesh every frame through the render type
 * carrying the texture (the display list cache of 1.12.2 has no 1.20.1 equivalent). No blend state
 * was enabled, so {@link JBRenderTypes#jointModel(ResourceLocation)} (entity cutout, no cull) stands
 * in for the bound GL texture.
 *
 * <p>{@code setRenderState} was an empty no-op in 1.12.2 - the blade carries no per-model state and
 * its transform lived inside {@code renderAll} - and stays a no-op here for interface conformance.
 * {@code shouldRenderInPass} keeps the 1.12.2 {@code renderPass == 0} rule.
 */
public class ModelTuningBladeSlideX extends AbstractJBObjModel {
   private static final ResourceLocation objRes = new ResourceLocation("jointblock:model/sliderx.obj");
   private static final ResourceLocation res = new ResourceLocation("jointblock:textures/entity/axis_x.png");
   /** The scale the 1.12.2 tuning call site always passed to {@code renderAll}. */
   private static final float TUNING_BLADE_SCALE = 0.0625F;
   public static ModelTuningBladeSlideX instance = new ModelTuningBladeSlideX();

   public ModelTuningBladeSlideX() {
      this.customModel = this.getBakedObjModelFromRes(objRes);
   }

   @Override
   public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(res));
      pose.translate(0.0F, TUNING_BLADE_SCALE * 24.0F, 0.0F);
      this.renderModel(this.customModel, pose, consumer, packedLight, packedOverlay);
   }

   @Override
   public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
      // 1.12.2 ModelTuningBladeSlideX#setRenderState was an empty no-op: the blade holds no per-model state and its
      // transform is applied by renderAll above. Kept empty for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
