package jp.mc.ancientred.jointblock.model;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelJointSeat {
   JBModelRenderer Seat;
   JBModelRenderer Lean;
   JBModelRenderer Handle1;
   JBModelRenderer Handle2;

   public ModelJointSeat() {
      this.Seat = new JBModelRenderer(0, 0);
      this.Seat.addBox(0.0F, 0.0F, 0.0F, 10, 2, 13);
      this.Seat.setRotationPoint(-5.0F, 22.0F, -8.0F);
      this.Seat.setTextureSize(64, 32);
      this.Seat.mirror = true;
      this.setRotation(this.Seat, 0.0F, 0.0F, 0.0F);
      this.Lean = new JBModelRenderer(0, 15);
      this.Lean.addBox(0.0F, -10.0F, 0.0F, 10, 10, 2);
      this.Lean.setRotationPoint(-5.0F, 24.0F, 5.0F);
      this.Lean.setTextureSize(64, 32);
      this.Lean.mirror = true;
      this.setRotation(this.Lean, -0.1745329F, 0.0F, 0.0F);
      this.Handle1 = new JBModelRenderer(34, 0);
      this.Handle1.addBox(0.0F, -7.0F, 1.0F, 1, 7, 1);
      this.Handle1.setRotationPoint(-6.0F, 24.0F, -6.0F);
      this.Handle1.setTextureSize(64, 32);
      this.Handle1.mirror = true;
      this.setRotation(this.Handle1, 0.1570796F, 0.0F, 0.0F);
      this.Handle2 = new JBModelRenderer(34, 0);
      this.Handle2.addBox(-1.0F, -7.0F, 0.0F, 1, 7, 1);
      this.Handle2.setRotationPoint(6.0F, 24.0F, -5.0F);
      this.Handle2.setTextureSize(64, 32);
      this.Handle2.mirror = true;
      this.setRotation(this.Handle2, 0.1570796F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Seat.render(pose, consumer, packedLight, packedOverlay);
      this.Lean.render(pose, consumer, packedLight, packedOverlay);
      this.Handle1.render(pose, consumer, packedLight, packedOverlay);
      this.Handle2.render(pose, consumer, packedLight, packedOverlay);
   }

   public void setModelState(IJBJointParameterHolder joint) {
      if (joint.getInitValue(0) == 1) {
         this.Handle1.showModel = true;
         this.Handle2.showModel = true;
      } else {
         this.Handle1.showModel = false;
         this.Handle2.showModel = false;
      }

   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
