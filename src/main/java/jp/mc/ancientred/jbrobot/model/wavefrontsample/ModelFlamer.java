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
 * Flamer model backed by a wavefront {@code .obj} mesh.
 *
 * <p>Same 1.12.2 -&gt; 1.20.1 substitutions as {@link ModelBazookaGripParts}. The flamer shares the
 * bazooka texture, exactly as in 1.12.2.
 */
public class ModelFlamer extends AbstractJBObjModel {
   private static final ResourceLocation objRes = new ResourceLocation("jbrobot:model/flamer.obj");
   private static final ResourceLocation res = new ResourceLocation("jbrobot:textures/entity/bazookatexture.png");
   /** The scale every 1.12.2 addon render call site passed to {@code renderAll}. */
   private static final float SCALE = 0.0625F;
   public static ModelFlamer instance = new ModelFlamer();

   public ModelFlamer() {
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
      // 1.12.2 ModelFlamer#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
