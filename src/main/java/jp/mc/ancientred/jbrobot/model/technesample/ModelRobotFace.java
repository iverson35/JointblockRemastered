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
 * Robot face box model, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.model.technesample.ModelRobotFace}.
 *
 * <p>1.12.2 derived from {@code ModelBase}, bound {@code robotface.png} and rendered its
 * {@code ModelRenderer} boxes at {@code scale}. The 1.20.1 port keeps the same box geometry (every
 * box has its own texture offset and mirror flag, which {@link JBModelRenderer} expresses exactly) but
 * draws through {@link JBRenderTypes#jointModel(ResourceLocation)} so the texture travels with the
 * render type.
 */
public class ModelRobotFace implements IJBModelItem.IJBModel {
   public static ModelRobotFace instance = new ModelRobotFace();
   private static final ResourceLocation res = new ResourceLocation("jbrobot:textures/entity/robotface.png");
   JBModelRenderer Shape1;
   JBModelRenderer Shape2;
   JBModelRenderer Shape3;
   JBModelRenderer Shape4;
   JBModelRenderer Shape5;
   JBModelRenderer Shape6;
   JBModelRenderer Shape7;
   JBModelRenderer Shape8;
   JBModelRenderer Shape9;
   JBModelRenderer Shape10;
   JBModelRenderer Shape11;
   JBModelRenderer Shape12;
   JBModelRenderer Shape13;
   JBModelRenderer Shape14;
   JBModelRenderer Shape15;
   JBModelRenderer Shape16;
   JBModelRenderer Shape17;
   JBModelRenderer Shape18;
   JBModelRenderer Shape20;
   JBModelRenderer Shape21;
   JBModelRenderer Shape22;
   JBModelRenderer Shape23;
   JBModelRenderer Shape24;
   JBModelRenderer Shape25;

   public ModelRobotFace() {
      this.Shape1 = new JBModelRenderer(50, 21);
      this.Shape1.addBox(-5.0F, -6.0F, 0.0F, 5, 6, 1);
      this.Shape1.setRotationPoint(5.0F, 23.0F, -5.0F);
      this.Shape1.setTextureSize(64, 32);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, -0.2617994F, 0.0F);
      this.Shape2 = new JBModelRenderer(18, 14);
      this.Shape2.addBox(-1.0F, -16.0F, 0.0F, 1, 15, 3);
      this.Shape2.setRotationPoint(-3.0F, 22.0F, 2.0F);
      this.Shape2.setTextureSize(64, 32);
      this.Shape2.mirror = true;
      this.setRotation(this.Shape2, -0.5235988F, -0.2617994F, -0.4363323F);
      this.Shape3 = new JBModelRenderer(18, 14);
      this.Shape3.addBox(0.0F, -16.0F, 0.0F, 1, 15, 3);
      this.Shape3.setRotationPoint(3.0F, 22.0F, 2.0F);
      this.Shape3.setTextureSize(64, 32);
      this.Shape3.mirror = true;
      this.setRotation(this.Shape3, -0.5235988F, 0.2617994F, 0.4363323F);
      this.Shape4 = new JBModelRenderer(18, 14);
      this.Shape4.addBox(0.0F, -15.0F, 0.0F, 1, 15, 3);
      this.Shape4.setRotationPoint(-6.0F, 22.0F, -2.0F);
      this.Shape4.setTextureSize(64, 32);
      this.Shape4.mirror = true;
      this.setRotation(this.Shape4, -0.3490659F, 0.0F, -0.2443461F);
      this.Shape5 = new JBModelRenderer(18, 14);
      this.Shape5.addBox(-1.0F, -15.0F, 0.0F, 1, 15, 3);
      this.Shape5.setRotationPoint(6.0F, 22.0F, -3.0F);
      this.Shape5.setTextureSize(64, 32);
      this.Shape5.mirror = true;
      this.setRotation(this.Shape5, -0.3490659F, 0.0F, 0.2617994F);
      this.Shape6 = new JBModelRenderer(50, 21);
      this.Shape6.addBox(0.0F, -6.0F, 0.0F, 5, 6, 1);
      this.Shape6.setRotationPoint(-5.0F, 23.0F, -5.0F);
      this.Shape6.setTextureSize(64, 32);
      this.Shape6.mirror = true;
      this.setRotation(this.Shape6, 0.0F, 0.2617994F, 0.0F);
      this.Shape7 = new JBModelRenderer(50, 14);
      this.Shape7.addBox(0.0F, -6.0F, 0.0F, 5, 6, 1);
      this.Shape7.setRotationPoint(-4.0F, 23.0F, -5.0F);
      this.Shape7.setTextureSize(64, 32);
      this.Shape7.mirror = true;
      this.setRotation(this.Shape7, -0.0698132F, -1.745329F, 0.0F);
      this.Shape8 = new JBModelRenderer(50, 14);
      this.Shape8.addBox(-5.0F, -6.0F, 0.0F, 5, 6, 1);
      this.Shape8.setRotationPoint(4.0F, 23.0F, -5.0F);
      this.Shape8.setTextureSize(64, 32);
      this.Shape8.mirror = true;
      this.setRotation(this.Shape8, -0.0698132F, 1.745329F, 0.0F);
      this.Shape9 = new JBModelRenderer(28, 17);
      this.Shape9.addBox(-5.0F, -6.0F, 0.0F, 5, 1, 2);
      this.Shape9.setRotationPoint(5.0F, 23.0F, -6.0F);
      this.Shape9.setTextureSize(64, 32);
      this.Shape9.mirror = true;
      this.setRotation(this.Shape9, 0.0F, -0.2617994F, 0.0F);
      this.Shape10 = new JBModelRenderer(28, 14);
      this.Shape10.addBox(0.0F, -6.0F, 0.0F, 5, 1, 2);
      this.Shape10.setRotationPoint(-5.0F, 23.0F, -6.0F);
      this.Shape10.setTextureSize(64, 32);
      this.Shape10.mirror = true;
      this.setRotation(this.Shape10, 0.0F, 0.2617994F, 0.0F);
      this.Shape11 = new JBModelRenderer(0, 19);
      this.Shape11.addBox(0.0F, -1.0F, 0.0F, 7, 2, 2);
      this.Shape11.setRotationPoint(-7.0F, 16.0F, -6.0F);
      this.Shape11.setTextureSize(64, 32);
      this.Shape11.mirror = true;
      this.setRotation(this.Shape11, 0.0F, 0.2617994F, 0.0F);
      this.Shape12 = new JBModelRenderer(0, 19);
      this.Shape12.addBox(-7.0F, -1.0F, 0.0F, 7, 2, 2);
      this.Shape12.setRotationPoint(7.0F, 16.0F, -6.0F);
      this.Shape12.setTextureSize(64, 32);
      this.Shape12.mirror = true;
      this.setRotation(this.Shape12, 0.0F, -0.2617994F, 0.0F);
      this.Shape13 = new JBModelRenderer(0, 15);
      this.Shape13.addBox(-7.0F, -1.0F, 0.0F, 7, 2, 2);
      this.Shape13.setRotationPoint(7.0F, 14.0F, -5.0F);
      this.Shape13.setTextureSize(64, 32);
      this.Shape13.mirror = true;
      this.setRotation(this.Shape13, 0.0F, -0.2617994F, 0.0F);
      this.Shape14 = new JBModelRenderer(0, 15);
      this.Shape14.addBox(0.0F, -1.0F, 0.0F, 7, 2, 2);
      this.Shape14.setRotationPoint(-7.0F, 14.0F, -5.0F);
      this.Shape14.setTextureSize(64, 32);
      this.Shape14.mirror = true;
      this.setRotation(this.Shape14, 0.0F, 0.2617994F, 0.0F);
      this.Shape15 = new JBModelRenderer(26, 24);
      this.Shape15.addBox(0.0F, -3.0F, 0.0F, 7, 3, 1);
      this.Shape15.setRotationPoint(-5.0F, 17.0F, -5.0F);
      this.Shape15.setTextureSize(64, 32);
      this.Shape15.mirror = true;
      this.setRotation(this.Shape15, 0.1745329F, -1.745329F, 0.0F);
      this.Shape16 = new JBModelRenderer(26, 24);
      this.Shape16.addBox(-7.0F, -3.0F, 0.0F, 7, 3, 1);
      this.Shape16.setRotationPoint(5.0F, 17.0F, -5.0F);
      this.Shape16.setTextureSize(64, 32);
      this.Shape16.mirror = true;
      this.setRotation(this.Shape16, 0.1745329F, 1.745329F, 0.0F);
      this.Shape17 = new JBModelRenderer(44, 0);
      this.Shape17.addBox(-4.0F, 0.0F, 0.0F, 8, 11, 2);
      this.Shape17.setRotationPoint(0.0F, 12.0F, 6.0F);
      this.Shape17.setTextureSize(64, 32);
      this.Shape17.mirror = true;
      this.setRotation(this.Shape17, 0.0F, 0.0F, 0.0F);
      this.Shape18 = new JBModelRenderer(0, 21);
      this.Shape18.addBox(-7.0F, -6.0F, -1.0F, 7, 9, 2);
      this.Shape18.setRotationPoint(5.0F, 20.0F, 6.0F);
      this.Shape18.setTextureSize(64, 32);
      this.Shape18.mirror = true;
      this.setRotation(this.Shape18, 0.0F, -1.570796F, 0.0F);
      this.Shape20 = new JBModelRenderer(0, 21);
      this.Shape20.addBox(-7.0F, -6.0F, -1.0F, 7, 9, 2);
      this.Shape20.setRotationPoint(-5.0F, 20.0F, 6.0F);
      this.Shape20.setTextureSize(64, 32);
      this.Shape20.mirror = true;
      this.setRotation(this.Shape20, 0.0F, -1.570796F, 0.0F);
      this.Shape21 = new JBModelRenderer(0, 9);
      this.Shape21.addBox(-3.0F, 0.0F, 0.0F, 6, 1, 4);
      this.Shape21.setRotationPoint(0.0F, 20.0F, 7.0F);
      this.Shape21.setTextureSize(64, 32);
      this.Shape21.mirror = true;
      this.setRotation(this.Shape21, 0.5934119F, 0.0F, 0.0F);
      this.Shape22 = new JBModelRenderer(34, 0);
      this.Shape22.addBox(-4.0F, 0.0F, 0.0F, 8, 4, 7);
      this.Shape22.setRotationPoint(0.0F, 20.0F, -3.0F);
      this.Shape22.setTextureSize(64, 32);
      this.Shape22.mirror = true;
      this.setRotation(this.Shape22, 0.0F, 0.0F, 0.0F);
      this.Shape23 = new JBModelRenderer(16, 0);
      this.Shape23.addBox(-7.0F, 0.0F, 0.0F, 14, 2, 10);
      this.Shape23.setRotationPoint(0.0F, 12.0F, -3.0F);
      this.Shape23.setTextureSize(64, 32);
      this.Shape23.mirror = true;
      this.setRotation(this.Shape23, 0.0F, 0.0F, 0.0F);
      this.Shape24 = new JBModelRenderer(28, 14);
      this.Shape24.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 14);
      this.Shape24.setRotationPoint(0.0F, 11.0F, -8.0F);
      this.Shape24.setTextureSize(64, 32);
      this.Shape24.mirror = true;
      this.setRotation(this.Shape24, -0.2617994F, -0.1745329F, -0.7853982F);
      this.Shape25 = new JBModelRenderer(0, 9);
      this.Shape25.addBox(-3.0F, 0.0F, 0.0F, 6, 1, 4);
      this.Shape25.setRotationPoint(0.0F, 17.0F, 7.0F);
      this.Shape25.setTextureSize(64, 32);
      this.Shape25.mirror = true;
      this.setRotation(this.Shape25, 0.5934119F, 0.0F, 0.0F);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   @Override
   public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(res));
      this.Shape1.render(pose, consumer, packedLight, packedOverlay);
      this.Shape2.render(pose, consumer, packedLight, packedOverlay);
      this.Shape3.render(pose, consumer, packedLight, packedOverlay);
      this.Shape4.render(pose, consumer, packedLight, packedOverlay);
      this.Shape5.render(pose, consumer, packedLight, packedOverlay);
      this.Shape6.render(pose, consumer, packedLight, packedOverlay);
      this.Shape7.render(pose, consumer, packedLight, packedOverlay);
      this.Shape8.render(pose, consumer, packedLight, packedOverlay);
      this.Shape9.render(pose, consumer, packedLight, packedOverlay);
      this.Shape10.render(pose, consumer, packedLight, packedOverlay);
      this.Shape11.render(pose, consumer, packedLight, packedOverlay);
      this.Shape12.render(pose, consumer, packedLight, packedOverlay);
      this.Shape13.render(pose, consumer, packedLight, packedOverlay);
      this.Shape14.render(pose, consumer, packedLight, packedOverlay);
      this.Shape15.render(pose, consumer, packedLight, packedOverlay);
      this.Shape16.render(pose, consumer, packedLight, packedOverlay);
      this.Shape17.render(pose, consumer, packedLight, packedOverlay);
      this.Shape18.render(pose, consumer, packedLight, packedOverlay);
      this.Shape20.render(pose, consumer, packedLight, packedOverlay);
      this.Shape21.render(pose, consumer, packedLight, packedOverlay);
      this.Shape22.render(pose, consumer, packedLight, packedOverlay);
      this.Shape23.render(pose, consumer, packedLight, packedOverlay);
      this.Shape24.render(pose, consumer, packedLight, packedOverlay);
      this.Shape25.render(pose, consumer, packedLight, packedOverlay);
   }

   @Override
   public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
      // 1.12.2 ModelRobotFace#setRenderState was an empty no-op; kept for interface conformance.
   }

   @Override
   public boolean shouldRenderInPass(int modelMeta, int renderPass) {
      return renderPass == 0;
   }
}
