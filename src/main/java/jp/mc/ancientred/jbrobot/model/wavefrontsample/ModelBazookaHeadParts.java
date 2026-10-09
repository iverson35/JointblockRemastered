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
 * Bazooka head model backed by a wavefront {@code .obj} mesh.
 *
 * <p>Same 1.12.2 -&gt; 1.20.1 substitutions as {@link ModelBazookaGripParts}: the bound texture travels
 * with {@link JBRenderTypes#jointModel(ResourceLocation)}, {@code scale * 24} becomes a fixed
 * {@code 0.0625F * 24.0F} pose translation and the display-list cache is replaced by a per-frame mesh
 * emission.
 */
public class ModelBazookaHeadParts extends AbstractJBObjModel {
   private static final ResourceLocation objRes = new ResourceLocation("jbrobot:model/bazookahead.obj");
   private static final ResourceLocation res = new ResourceLocation("jbrobot:textures/entity/bazookatexture.png");
   /** The scale every 1.12.2 addon render call site passed to {@code renderAll}. */
   private static final float SCALE = 0.0625F;
   public static ModelBazookaHeadParts instance = new ModelBazookaHeadParts();

   public ModelBazookaHeadParts() {
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
      // 1.12.2 ModelBazookaHeadParts#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
