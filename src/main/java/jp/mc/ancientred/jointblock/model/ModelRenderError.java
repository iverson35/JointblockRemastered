package jp.mc.ancientred.jointblock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.model.render.JBModelRenderer;

public class ModelRenderError {
   JBModelRenderer Shape1;

   public ModelRenderError(int texOffsetV) {
      this.Shape1 = new JBModelRenderer(0, texOffsetV);
      this.Shape1.addBox(0.0F, 0.0F, 0.0F, 16, 16, 16);
      this.Shape1.setRotationPoint(-8.0F, 8.0F, -8.0F);
      this.Shape1.setTextureSize(64, 64);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.Shape1.render(pose, consumer, packedLight, packedOverlay);
   }

   private void setRotation(JBModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
