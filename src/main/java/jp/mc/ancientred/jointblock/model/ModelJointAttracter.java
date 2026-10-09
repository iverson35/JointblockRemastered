package jp.mc.ancientred.jointblock.model;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelJointAttracter {
   JBModelRenderer Bottom;
   JBModelRenderer Ant1;
   JBModelRenderer Ant2;

   public ModelJointAttracter() {
      this.Bottom = new JBModelRenderer(0, 0);
      this.Bottom.addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
      this.Bottom.setRotationPoint(-5.0F, 22.0F, -5.0F);
      this.Bottom.setTextureSize(64, 32);
      this.Bottom.mirror = true;
      this.setRotation(this.Bottom, 0.0F, 0.0F, 0.0F);
      this.Ant1 = new JBModelRenderer(0, 0);
      this.Ant1.addBox(0.0F, -1.0F, 0.0F, 1, 3, 1);
      this.Ant1.setRotationPoint(3.0F, 20.0F, 3.0F);
      this.Ant1.setTextureSize(64, 32);
      this.Ant1.mirror = true;
      this.setRotation(this.Ant1, 0.0F, 0.0F, 0.0F);
      this.Ant2 = new JBModelRenderer(0, 0);
      this.Ant2.addBox(0.0F, -1.0F, 0.0F, 1, 3, 1);
      this.Ant2.setRotationPoint(-4.0F, 20.0F, 3.0F);
      this.Ant2.setTextureSize(64, 32);
      this.Ant2.mirror = true;
      this.setRotation(this.Ant2, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Bottom.render(pose, consumer, packedLight, packedOverlay);
      this.Ant1.render(pose, consumer, packedLight, packedOverlay);
      this.Ant2.render(pose, consumer, packedLight, packedOverlay);
   }

   public void setModelState(IJBJointParameterHolder joint) {
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
