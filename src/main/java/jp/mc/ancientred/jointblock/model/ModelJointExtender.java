package jp.mc.ancientred.jointblock.model;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelJointExtender {
   JBModelRenderer StickA;
   JBModelRenderer Center;
   JBModelRenderer StickB;
   JBModelRenderer StickC;
   JBModelRenderer StickD;
   JBModelRenderer BottomBox;
   JBModelRenderer TopBox;
   JBModelRenderer MoveBox;

   public ModelJointExtender() {
      this.StickA = new JBModelRenderer(0, 0);
      this.StickA.addBox(0.0F, 0.0F, 0.0F, 1, 12, 1);
      this.StickA.setRotationPoint(-2.0F, 10.0F, 1.0F);
      this.StickA.setTextureSize(32, 16);
      this.StickA.mirror = true;
      this.Center = new JBModelRenderer(20, 0);
      this.Center.addBox(0.0F, 0.0F, 0.0F, 1, 14, 1);
      this.Center.setRotationPoint(-0.5F, 9.0F, -0.5F);
      this.Center.setTextureSize(32, 16);
      this.Center.mirror = true;
      this.StickB = new JBModelRenderer(0, 0);
      this.StickB.addBox(0.0F, 0.0F, 0.0F, 1, 12, 1);
      this.StickB.setRotationPoint(-2.0F, 10.0F, -2.0F);
      this.StickB.setTextureSize(32, 16);
      this.StickB.mirror = true;
      this.StickC = new JBModelRenderer(0, 0);
      this.StickC.addBox(0.0F, 0.0F, 0.0F, 1, 12, 1);
      this.StickC.setRotationPoint(1.0F, 10.0F, 1.0F);
      this.StickC.setTextureSize(32, 16);
      this.StickC.mirror = true;
      this.StickD = new JBModelRenderer(0, 0);
      this.StickD.addBox(0.0F, 0.0F, 0.0F, 1, 12, 1);
      this.StickD.setRotationPoint(1.0F, 10.0F, -2.0F);
      this.StickD.setTextureSize(32, 16);
      this.StickD.mirror = true;
      this.BottomBox = new JBModelRenderer(4, 6);
      this.BottomBox.addBox(0.0F, 0.0F, 0.0F, 4, 2, 4);
      this.BottomBox.setRotationPoint(-2.0F, 22.0F, -2.0F);
      this.BottomBox.setTextureSize(32, 16);
      this.BottomBox.mirror = true;
      this.TopBox = new JBModelRenderer(4, 0);
      this.TopBox.addBox(0.0F, 0.0F, 0.0F, 4, 2, 4);
      this.TopBox.setRotationPoint(-2.0F, 8.0F, -2.0F);
      this.TopBox.setTextureSize(32, 16);
      this.TopBox.mirror = true;
      this.MoveBox = new JBModelRenderer(4, 12);
      this.MoveBox.addBox(0.0F, 0.0F, 0.0F, 3, 1, 3);
      this.MoveBox.setRotationPoint(-1.5F, 21.0F, -1.5F);
      this.MoveBox.setTextureSize(32, 16);
      this.MoveBox.mirror = true;
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.StickA.render(pose, consumer, packedLight, packedOverlay);
      this.Center.render(pose, consumer, packedLight, packedOverlay);
      this.StickB.render(pose, consumer, packedLight, packedOverlay);
      this.StickC.render(pose, consumer, packedLight, packedOverlay);
      this.StickD.render(pose, consumer, packedLight, packedOverlay);
      this.BottomBox.render(pose, consumer, packedLight, packedOverlay);
      this.TopBox.render(pose, consumer, packedLight, packedOverlay);
      this.MoveBox.render(pose, consumer, packedLight, packedOverlay);
   }

   public void setModelState(IJBJointParameterHolder joint, float partial) {
      if (!(joint instanceof TileEntityJoint)) {
         this.MoveBox.isHidden = true;
      } else {
         TileEntityJoint te = (TileEntityJoint)joint;
         int mod = te.electricAnimation % 90;
         if (mod > 30) {
            this.MoveBox.isHidden = true;
         } else {
            this.MoveBox.isHidden = false;
            this.MoveBox.offsetY = -0.75F * ((float)mod - 1.0F + 1.0F * partial) / 30.0F;
         }
      }
   }
}
