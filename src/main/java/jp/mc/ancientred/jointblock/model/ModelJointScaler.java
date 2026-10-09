package jp.mc.ancientred.jointblock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelJointScaler {
   JBModelRenderer Shape1d;
   JBModelRenderer Shape1a;
   JBModelRenderer Shape1b;
   JBModelRenderer Shape1c;
   JBModelRenderer Bottom;
   JBModelRenderer TopR;
   JBModelRenderer TopF;
   JBModelRenderer TopL;
   JBModelRenderer TolR;

   public ModelJointScaler() {
      this.Shape1d = new JBModelRenderer(0, 0);
      this.Shape1d.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
      this.Shape1d.setRotationPoint(5.0F, 11.0F, 5.0F);
      this.Shape1d.setTextureSize(64, 32);
      this.Shape1d.mirror = true;
      this.setRotation(this.Shape1d, 0.0F, 0.0F, 0.0F);
      this.Shape1a = new JBModelRenderer(0, 0);
      this.Shape1a.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
      this.Shape1a.setRotationPoint(5.0F, 11.0F, -8.0F);
      this.Shape1a.setTextureSize(64, 32);
      this.Shape1a.mirror = true;
      this.setRotation(this.Shape1a, 0.0F, 0.0F, 0.0F);
      this.Shape1b = new JBModelRenderer(0, 0);
      this.Shape1b.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
      this.Shape1b.setRotationPoint(-8.0F, 11.0F, -8.0F);
      this.Shape1b.setTextureSize(64, 32);
      this.Shape1b.mirror = true;
      this.setRotation(this.Shape1b, 0.0F, 0.0F, 0.0F);
      this.Shape1c = new JBModelRenderer(0, 0);
      this.Shape1c.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
      this.Shape1c.setRotationPoint(-8.0F, 11.0F, 5.0F);
      this.Shape1c.setTextureSize(64, 32);
      this.Shape1c.mirror = true;
      this.setRotation(this.Shape1c, 0.0F, 0.0F, 0.0F);
      this.Bottom = new JBModelRenderer(0, 0);
      this.Bottom.addBox(0.0F, 0.0F, 0.0F, 16, 3, 16);
      this.Bottom.setRotationPoint(-8.0F, 21.0F, -8.0F);
      this.Bottom.setTextureSize(64, 32);
      this.Bottom.mirror = true;
      this.setRotation(this.Bottom, 0.0F, 0.0F, 0.0F);
      this.TopR = new JBModelRenderer(0, 19);
      this.TopR.addBox(0.0F, 0.0F, 0.0F, 10, 3, 3);
      this.TopR.setRotationPoint(-5.0F, 8.0F, 5.0F);
      this.TopR.setTextureSize(64, 32);
      this.TopR.mirror = true;
      this.setRotation(this.TopR, 0.0F, 0.0F, 0.0F);
      this.TopF = new JBModelRenderer(0, 19);
      this.TopF.addBox(0.0F, 0.0F, 0.0F, 10, 3, 3);
      this.TopF.setRotationPoint(-5.0F, 8.0F, -8.0F);
      this.TopF.setTextureSize(64, 32);
      this.TopF.mirror = true;
      this.setRotation(this.TopF, 0.0F, 0.0F, 0.0F);
      this.TopL = new JBModelRenderer(38, 19);
      this.TopL.addBox(0.0F, 0.0F, 0.0F, 3, 3, 10);
      this.TopL.setRotationPoint(-8.0F, 8.0F, -5.0F);
      this.TopL.setTextureSize(64, 32);
      this.TopL.mirror = true;
      this.setRotation(this.TopL, 0.0F, 0.0F, 0.0F);
      this.TolR = new JBModelRenderer(38, 19);
      this.TolR.addBox(0.0F, 0.0F, 0.0F, 3, 3, 10);
      this.TolR.setRotationPoint(5.0F, 8.0F, -5.0F);
      this.TolR.setTextureSize(64, 32);
      this.TolR.mirror = true;
      this.setRotation(this.TolR, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Shape1d.render(pose, consumer, packedLight, packedOverlay);
      this.Shape1a.render(pose, consumer, packedLight, packedOverlay);
      this.Shape1b.render(pose, consumer, packedLight, packedOverlay);
      this.Shape1c.render(pose, consumer, packedLight, packedOverlay);
      this.Bottom.render(pose, consumer, packedLight, packedOverlay);
      this.TopR.render(pose, consumer, packedLight, packedOverlay);
      this.TopF.render(pose, consumer, packedLight, packedOverlay);
      this.TopL.render(pose, consumer, packedLight, packedOverlay);
      this.TolR.render(pose, consumer, packedLight, packedOverlay);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
