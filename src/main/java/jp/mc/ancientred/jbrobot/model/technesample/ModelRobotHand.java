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
 * Robot hand box model, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.model.technesample.ModelRobotHand}.
 *
 * <p>Each finger is one {@code ModelRenderer} carrying two boxes, and in 1.12.2 every box resolved its
 * own texture offset from the {@code PieceX.FingerY}/{@code PieceX.FingertopY} name table
 * ({@code ModelBase#setTextureOffset} + {@code ModelRenderer#addBox(String, ...)}). The 1.20.1
 * {@link JBModelRenderer} binds a single texture offset per renderer, so the finger box uses the
 * renderer's own offset and the finger tip is attached as an unnamed child renderer at the same local
 * origin - geometrically identical.
 *
 * <p>1.12.2 wrapped the draw in {@code GL11.glDisable(GL_CULL_FACE)}/{@code glEnable(GL_CULL_FACE)};
 * 1.20.1 carries the cull state on the {@link net.minecraft.client.renderer.RenderType}, and
 * {@link JBRenderTypes#jointModel(ResourceLocation)} is already unculled, so the pair has no port
 * equivalent. The {@code modelMeta == 1} mirror ({@code GL11.glScalef(-1, 1, 1)}) becomes a pose scale.
 */
public class ModelRobotHand implements IJBModelItem.IJBModel {
   public static ModelRobotHand instanceLeft = new ModelRobotHand();
   public static ModelRobotHand instanceRight = new ModelRobotHand();
   private static final ResourceLocation res = new ResourceLocation("jbrobot:textures/entity/robothand.png");
   JBModelRenderer Palm;
   JBModelRenderer Wrist;
   JBModelRenderer Thumb;
   JBModelRenderer Piece1;
   JBModelRenderer Piece2;
   JBModelRenderer Piece3;
   JBModelRenderer Piece4;

   public ModelRobotHand() {
      this.Palm = new JBModelRenderer(34, 5);
      this.Palm.addBox(0.0F, 0.0F, 0.0F, 3, 13, 12);
      this.Palm.setRotationPoint(-7.0F, 9.0F, -6.0F);
      this.Palm.setTextureSize(64, 32);
      this.Palm.mirror = true;
      this.setRotation(this.Palm, 0.0F, 0.0F, 0.0F);
      this.Wrist = new JBModelRenderer(10, 20);
      this.Wrist.addBox(0.0F, 0.0F, 0.0F, 6, 2, 10);
      this.Wrist.setRotationPoint(-7.0F, 22.0F, -5.0F);
      this.Wrist.setTextureSize(64, 32);
      this.Wrist.mirror = true;
      this.setRotation(this.Wrist, 0.0F, 0.0F, 0.0F);
      this.Thumb = new JBModelRenderer(0, 20);
      this.Thumb.addBox(0.0F, 0.0F, 0.0F, 6, 3, 2);
      this.Thumb.setRotationPoint(-5.0F, 19.0F, -8.0F);
      this.Thumb.setTextureSize(64, 32);
      this.Thumb.mirror = true;
      this.setRotation(this.Thumb, 0.0F, 0.0F, 0.0F);

      // 1.12.2 Piece1: "Piece1.Finger1" texOff (0,0), "Piece1.Fingertop1" texOff (34,0).
      this.Piece1 = new JBModelRenderer(0, 0);
      this.Piece1.setRotationPoint(-4.0F, 9.0F, -7.0F);
      this.Piece1.setTextureSize(64, 32);
      this.setRotation(this.Piece1, 0.0F, 0.0F, 0.0F);
      this.Piece1.addBox(0.0F, 0.0F, 0.0F, 8, 3, 3);
      JBModelRenderer fingerTop1 = new JBModelRenderer(34, 0);
      fingerTop1.setTextureSize(64, 32);
      fingerTop1.addBox(8.0F, 1.0F, 0.0F, 3, 11, 3);
      this.Piece1.addChild(fingerTop1);

      // 1.12.2 Piece2: "Piece2.Finger2" texOff (0,12), "Piece2.Fingertop2" texOff (52,0).
      this.Piece2 = new JBModelRenderer(0, 12);
      this.Piece2.setRotationPoint(-4.0F, 9.0F, -3.0F);
      this.Piece2.setTextureSize(64, 32);
      this.setRotation(this.Piece2, 0.0F, 0.0F, 0.0F);
      this.Piece2.addBox(0.0F, 0.0F, 0.0F, 8, 3, 3);
      JBModelRenderer fingerTop2 = new JBModelRenderer(52, 0);
      fingerTop2.setTextureSize(64, 32);
      fingerTop2.addBox(8.0F, 1.0F, 0.0F, 3, 9, 3);
      this.Piece2.addChild(fingerTop2);

      // 1.12.2 Piece3: "Piece3.Finger2" texOff (0,6), "Piece3.Fingertop3" texOff (52,0).
      this.Piece3 = new JBModelRenderer(0, 6);
      this.Piece3.setRotationPoint(-4.0F, 9.0F, 1.0F);
      this.Piece3.setTextureSize(64, 32);
      this.setRotation(this.Piece3, 0.0F, 0.0F, 0.0F);
      this.Piece3.addBox(0.0F, 0.0F, 0.0F, 8, 3, 3);
      JBModelRenderer fingerTop3 = new JBModelRenderer(52, 0);
      fingerTop3.setTextureSize(64, 32);
      fingerTop3.addBox(8.0F, 1.0F, 0.0F, 3, 9, 3);
      this.Piece3.addChild(fingerTop3);

      // 1.12.2 Piece4: "Piece4.Finger4" texOff (0,25), "Piece4.Fingertop4" texOff (22,0).
      this.Piece4 = new JBModelRenderer(0, 25);
      this.Piece4.setRotationPoint(-4.0F, 9.0F, 5.0F);
      this.Piece4.setTextureSize(64, 32);
      this.setRotation(this.Piece4, 0.0F, 0.0F, 0.0F);
      this.Piece4.addBox(0.0F, 0.0F, 0.0F, 8, 3, 2);
      JBModelRenderer fingerTop4 = new JBModelRenderer(22, 0);
      fingerTop4.setTextureSize(64, 32);
      fingerTop4.addBox(8.0F, 1.0F, 0.0F, 2, 7, 2);
      this.Piece4.addChild(fingerTop4);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   @Override
   public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(res));
      if (modelMeta == 1) {
         pose.scale(-1.0F, 1.0F, 1.0F);
      }

      this.Palm.render(pose, consumer, packedLight, packedOverlay);
      this.Piece1.render(pose, consumer, packedLight, packedOverlay);
      this.Piece2.render(pose, consumer, packedLight, packedOverlay);
      this.Wrist.render(pose, consumer, packedLight, packedOverlay);
      this.Thumb.render(pose, consumer, packedLight, packedOverlay);
      this.Piece3.render(pose, consumer, packedLight, packedOverlay);
      this.Piece4.render(pose, consumer, packedLight, packedOverlay);
   }

   @Override
   public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
      // 1.12.2 ModelRobotHand#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
