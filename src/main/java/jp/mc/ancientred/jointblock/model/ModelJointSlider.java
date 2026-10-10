package jp.mc.ancientred.jointblock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelJointSlider {
   JBModelRenderer Bar1a;
   JBModelRenderer Bar1c;
   JBModelRenderer Bar1d;
   JBModelRenderer Bar1b;
   JBModelRenderer Center;
   JBModelRenderer Top;
   JBModelRenderer Sidea;
   JBModelRenderer Sideb;

   public ModelJointSlider() {
      this.Bar1a = new JBModelRenderer(0, 0);
      this.Bar1a.addBox(0.0F, 0.0F, 0.0F, 2, 3, 16);
      this.Bar1a.setRotationPoint(-8.0F, 21.0F, 3.0F);
      this.Bar1a.setTextureSize(64, 64);
      this.Bar1a.mirror = true;
      this.setRotation(this.Bar1a, 0.0F, 1.570796F, 0.0F);
      this.Bar1c = new JBModelRenderer(0, 0);
      this.Bar1c.addBox(0.0F, 0.0F, 0.0F, 2, 3, 16);
      this.Bar1c.setRotationPoint(1.0F, 21.0F, -8.0F);
      this.Bar1c.setTextureSize(64, 64);
      this.Bar1c.mirror = true;
      this.setRotation(this.Bar1c, 0.0F, 0.0F, 0.0F);
      this.Bar1d = new JBModelRenderer(0, 0);
      this.Bar1d.addBox(0.0F, 0.0F, 0.0F, 2, 3, 16);
      this.Bar1d.setRotationPoint(-3.0F, 21.0F, -8.0F);
      this.Bar1d.setTextureSize(64, 64);
      this.Bar1d.mirror = true;
      this.setRotation(this.Bar1d, 0.0F, 0.0F, 0.0F);
      this.Bar1b = new JBModelRenderer(0, 0);
      this.Bar1b.addBox(0.0F, 0.0F, 0.0F, 2, 3, 16);
      this.Bar1b.setRotationPoint(-8.0F, 21.0F, -1.0F);
      this.Bar1b.setTextureSize(64, 64);
      this.Bar1b.mirror = true;
      this.setRotation(this.Bar1b, 0.0F, 1.570796F, 0.0F);
      this.Center = new JBModelRenderer(0, 19);
      this.Center.addBox(0.0F, 0.0F, 0.0F, 16, 10, 16);
      this.Center.setRotationPoint(-8.0F, 11.0F, -8.0F);
      this.Center.setTextureSize(64, 64);
      this.Center.mirror = true;
      this.setRotation(this.Center, 0.0F, 0.0F, 0.0F);
      this.Top = new JBModelRenderer(0, 45);
      this.Top.addBox(0.0F, 0.0F, 0.0F, 10, 3, 16);
      this.Top.setRotationPoint(-5.0F, 8.0F, -8.0F);
      this.Top.setTextureSize(64, 64);
      this.Top.mirror = true;
      this.setRotation(this.Top, 0.0F, 0.0F, 0.0F);
      this.Sidea = new JBModelRenderer(38, 48);
      this.Sidea.addBox(0.0F, 0.0F, 0.0F, 3, 3, 10);
      this.Sidea.setRotationPoint(5.0F, 8.0F, -5.0F);
      this.Sidea.setTextureSize(64, 64);
      this.Sidea.mirror = true;
      this.setRotation(this.Sidea, 0.0F, 0.0F, 0.0F);
      this.Sideb = new JBModelRenderer(38, 6);
      this.Sideb.addBox(0.0F, 0.0F, 0.0F, 3, 3, 10);
      this.Sideb.setRotationPoint(-8.0F, 8.0F, -5.0F);
      this.Sideb.setTextureSize(64, 64);
      this.Sideb.mirror = true;
      this.setRotation(this.Sideb, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Bar1a.render(pose, consumer, packedLight, packedOverlay);
      this.Bar1c.render(pose, consumer, packedLight, packedOverlay);
      this.Bar1d.render(pose, consumer, packedLight, packedOverlay);
      this.Bar1b.render(pose, consumer, packedLight, packedOverlay);
      this.Center.render(pose, consumer, packedLight, packedOverlay);
      this.Top.render(pose, consumer, packedLight, packedOverlay);
      this.Sidea.render(pose, consumer, packedLight, packedOverlay);
      this.Sideb.render(pose, consumer, packedLight, packedOverlay);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
