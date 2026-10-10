package jp.mc.ancientred.jointblock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelSimpleJB {
   JBModelRenderer Shape1;
   JBModelRenderer Shape2;
   JBModelRenderer Shape3;
   JBModelRenderer Shape4;
   JBModelRenderer Shape5;
   JBModelRenderer Shape6;
   JBModelRenderer Shape7;

   public ModelSimpleJB(int tOffsetX, int tOffsetY) {
      this.Shape1 = new JBModelRenderer(tOffsetX, tOffsetY);
      this.Shape1.addBox(0.0F, 0.0F, 0.0F, 10, 16, 16);
      this.Shape1.setRotationPoint(-5.0F, 8.0F, -8.0F);
      this.Shape1.setTextureSize(64, 128);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
      this.Shape2 = new JBModelRenderer(0, 64);
      this.Shape2.addBox(0.0F, 0.0F, 0.0F, 16, 16, 10);
      this.Shape2.setRotationPoint(-8.0F, 8.0F, -5.0F);
      this.Shape2.setTextureSize(64, 128);
      this.Shape2.mirror = true;
      this.setRotation(this.Shape2, 0.0F, 0.0F, 0.0F);
      this.Shape3 = new JBModelRenderer(0, 90);
      this.Shape3.addBox(0.0F, 0.0F, 0.0F, 16, 10, 16);
      this.Shape3.setRotationPoint(-8.0F, 11.0F, -8.0F);
      this.Shape3.setTextureSize(64, 128);
      this.Shape3.mirror = true;
      this.setRotation(this.Shape3, 0.0F, 0.0F, 0.0F);
      this.Shape4 = new JBModelRenderer(60, 0);
      this.Shape4.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1);
      this.Shape4.setRotationPoint(-6.0F, 21.0F, -6.0F);
      this.Shape4.setTextureSize(64, 128);
      this.Shape4.mirror = true;
      this.setRotation(this.Shape4, 0.0F, 0.0F, 0.0F);
      this.Shape5 = new JBModelRenderer(60, 0);
      this.Shape5.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1);
      this.Shape5.setRotationPoint(5.0F, 21.0F, -6.0F);
      this.Shape5.setTextureSize(64, 128);
      this.Shape5.mirror = true;
      this.setRotation(this.Shape5, 0.0F, 0.0F, 0.0F);
      this.Shape6 = new JBModelRenderer(60, 0);
      this.Shape6.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1);
      this.Shape6.setRotationPoint(-6.0F, 21.0F, 5.0F);
      this.Shape6.setTextureSize(64, 128);
      this.Shape6.mirror = true;
      this.setRotation(this.Shape6, 0.0F, 0.0F, 0.0F);
      this.Shape7 = new JBModelRenderer(60, 0);
      this.Shape7.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1);
      this.Shape7.setRotationPoint(5.0F, 21.0F, 5.0F);
      this.Shape7.setTextureSize(64, 128);
      this.Shape7.mirror = true;
      this.setRotation(this.Shape7, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay, boolean renderConnection) {
         this.Shape1.render(pose, consumer, packedLight, packedOverlay);
         this.Shape2.render(pose, consumer, packedLight, packedOverlay);
         this.Shape3.render(pose, consumer, packedLight, packedOverlay);

      if (renderConnection) {
            this.Shape4.render(pose, consumer, packedLight, packedOverlay);
            this.Shape5.render(pose, consumer, packedLight, packedOverlay);
            this.Shape6.render(pose, consumer, packedLight, packedOverlay);
            this.Shape7.render(pose, consumer, packedLight, packedOverlay);

      }

   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
