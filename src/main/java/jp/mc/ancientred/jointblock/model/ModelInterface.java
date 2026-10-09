package jp.mc.ancientred.jointblock.model;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelInterface {
   JBModelRenderer Bottom;
   JBModelRenderer Ant1;
   JBModelRenderer Ant2;
   JBModelRenderer Ant3;
   JBModelRenderer Ant4;
   JBModelRenderer Pole1;
   JBModelRenderer Pole2;
   JBModelRenderer MonitorBase;
   JBModelRenderer FrameSide1;
   JBModelRenderer FrameSide2;
   JBModelRenderer FrameTop;
   JBModelRenderer FrameBot;
   JBModelRenderer Shape1;

   public ModelInterface() {
      this.Bottom = new JBModelRenderer(0, 21);
      this.Bottom.addBox(0.0F, 0.0F, 0.0F, 10, 1, 10);
      this.Bottom.setRotationPoint(-5.0F, 23.0F, -5.0F);
      this.Bottom.setTextureSize(64, 32);
      this.Bottom.mirror = true;
      this.setRotation(this.Bottom, 0.0F, 0.0F, 0.0F);
      this.Ant1 = new JBModelRenderer(0, 13);
      this.Ant1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.Ant1.setRotationPoint(1.0F, 21.0F, 1.0F);
      this.Ant1.setTextureSize(64, 32);
      this.Ant1.mirror = true;
      this.setRotation(this.Ant1, 0.0F, 0.0F, 0.0F);
      this.Ant2 = new JBModelRenderer(0, 13);
      this.Ant2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.Ant2.setRotationPoint(1.0F, 21.0F, -2.0F);
      this.Ant2.setTextureSize(64, 32);
      this.Ant2.mirror = true;
      this.setRotation(this.Ant2, 0.0F, 0.0F, 0.0F);
      this.Ant3 = new JBModelRenderer(0, 13);
      this.Ant3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.Ant3.setRotationPoint(-2.0F, 21.0F, -2.0F);
      this.Ant3.setTextureSize(64, 32);
      this.Ant3.mirror = true;
      this.setRotation(this.Ant3, 0.0F, 0.0F, 0.0F);
      this.Ant4 = new JBModelRenderer(0, 13);
      this.Ant4.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.Ant4.setRotationPoint(-2.0F, 21.0F, 1.0F);
      this.Ant4.setTextureSize(64, 32);
      this.Ant4.mirror = true;
      this.setRotation(this.Ant4, 0.0F, 0.0F, 0.0F);
      this.Pole1 = new JBModelRenderer(30, 0);
      this.Pole1.addBox(0.0F, 0.0F, 0.0F, 2, 2, 4);
      this.Pole1.setRotationPoint(-1.0F, 25.0F, -1.0F);
      this.Pole1.setTextureSize(64, 32);
      this.Pole1.mirror = true;
      this.setRotation(this.Pole1, 0.0F, 0.0F, 0.0F);
      this.Pole2 = new JBModelRenderer(27, 13);
      this.Pole2.addBox(0.0F, 0.0F, 0.0F, 4, 2, 2);
      this.Pole2.setRotationPoint(-2.0F, 20.0F, -1.0F);
      this.Pole2.setTextureSize(64, 32);
      this.Pole2.mirror = true;
      this.setRotation(this.Pole2, 0.0F, 0.0F, 0.0F);
      this.MonitorBase = new JBModelRenderer(30, 24);
      this.MonitorBase.addBox(0.0F, 0.0F, 0.0F, 8, 5, 2);
      this.MonitorBase.setRotationPoint(-4.0F, 15.0F, -1.0F);
      this.MonitorBase.setTextureSize(64, 32);
      this.MonitorBase.mirror = true;
      this.setRotation(this.MonitorBase, 0.0F, 0.0F, 0.0F);
      this.FrameSide1 = new JBModelRenderer(0, 21);
      this.FrameSide1.addBox(0.0F, 0.0F, 0.0F, 1, 5, 1);
      this.FrameSide1.setRotationPoint(3.0F, 15.0F, -2.0F);
      this.FrameSide1.setTextureSize(64, 32);
      this.FrameSide1.mirror = true;
      this.setRotation(this.FrameSide1, 0.0F, 0.0F, 0.0F);
      this.FrameSide2 = new JBModelRenderer(0, 21);
      this.FrameSide2.addBox(0.0F, 0.0F, 0.0F, 1, 5, 1);
      this.FrameSide2.setRotationPoint(-4.0F, 15.0F, -2.0F);
      this.FrameSide2.setTextureSize(64, 32);
      this.FrameSide2.mirror = true;
      this.setRotation(this.FrameSide2, 0.0F, 0.0F, 0.0F);
      this.FrameTop = new JBModelRenderer(27, 18);
      this.FrameTop.addBox(0.0F, 0.0F, 0.0F, 6, 1, 1);
      this.FrameTop.setRotationPoint(-3.0F, 15.0F, -2.0F);
      this.FrameTop.setTextureSize(64, 32);
      this.FrameTop.mirror = true;
      this.setRotation(this.FrameTop, 0.0F, 0.0F, 0.0F);
      this.FrameBot = new JBModelRenderer(27, 18);
      this.FrameBot.addBox(0.0F, 0.0F, 0.0F, 6, 1, 1);
      this.FrameBot.setRotationPoint(-3.0F, 19.0F, -2.0F);
      this.FrameBot.setTextureSize(64, 32);
      this.FrameBot.mirror = true;
      this.setRotation(this.FrameBot, 0.0F, 0.0F, 0.0F);
      this.Shape1 = new JBModelRenderer(0, 13);
      this.Shape1.addBox(0.0F, 0.0F, 0.0F, 10, 1, 7);
      this.Shape1.setRotationPoint(-5.0F, 22.0F, -2.0F);
      this.Shape1.setTextureSize(64, 32);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Bottom.render(pose, consumer, packedLight, packedOverlay);
      this.Ant1.render(pose, consumer, packedLight, packedOverlay);
      this.Ant2.render(pose, consumer, packedLight, packedOverlay);
      this.Ant3.render(pose, consumer, packedLight, packedOverlay);
      this.Ant4.render(pose, consumer, packedLight, packedOverlay);
      this.Pole1.render(pose, consumer, packedLight, packedOverlay);
      this.Pole2.render(pose, consumer, packedLight, packedOverlay);
      this.MonitorBase.render(pose, consumer, packedLight, packedOverlay);
      this.FrameSide1.render(pose, consumer, packedLight, packedOverlay);
      this.FrameSide2.render(pose, consumer, packedLight, packedOverlay);
      this.FrameTop.render(pose, consumer, packedLight, packedOverlay);
      this.FrameBot.render(pose, consumer, packedLight, packedOverlay);
      this.Shape1.render(pose, consumer, packedLight, packedOverlay);
   }

   public void setModelState(IJBJointParameterHolder joint) {
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
