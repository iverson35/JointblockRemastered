package jp.mc.ancientred.jointblock.model;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelCamera {
   JBModelRenderer Base;
   JBModelRenderer Shaft;
   JBModelRenderer Camera;

   public ModelCamera() {
      this.Base = new JBModelRenderer(0, 25);
      this.Base.addBox(0.0F, 0.0F, 0.0F, 6, 1, 6);
      this.Base.setRotationPoint(-3.0F, 23.0F, -3.0F);
      this.Base.setTextureSize(32, 32);
      this.Base.mirror = true;
      this.setRotation(this.Base, 0.0F, 0.0F, 0.0F);
      this.Shaft = new JBModelRenderer(0, 0);
      this.Shaft.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2);
      this.Shaft.setRotationPoint(-1.0F, 21.0F, 0.0F);
      this.Shaft.setTextureSize(32, 32);
      this.Shaft.mirror = true;
      this.setRotation(this.Shaft, 0.0F, 0.0F, 0.0F);
      this.Camera = new JBModelRenderer(0, 10);
      this.Camera.addBox(0.0F, 0.0F, 0.0F, 4, 4, 6);
      this.Camera.setRotationPoint(-2.0F, 17.0F, -3.0F);
      this.Camera.setTextureSize(32, 32);
      this.Camera.mirror = true;
      this.setRotation(this.Camera, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Base.render(pose, consumer, packedLight, packedOverlay);
      this.Shaft.render(pose, consumer, packedLight, packedOverlay);
      this.Camera.render(pose, consumer, packedLight, packedOverlay);
   }

   public void setModelState(IJBJointParameterHolder joint) {
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
