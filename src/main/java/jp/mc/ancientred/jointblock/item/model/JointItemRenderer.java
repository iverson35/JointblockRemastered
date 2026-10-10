package jp.mc.ancientred.jointblock.item.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jp.mc.ancientred.jointblock.model.BasicJointRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.20.1 replacement for the 1.12.2 {@code TileEntityItemStackRendererJB} +
 * {@code TileEntityTESRRenderer(jointType)} pair: the joint block items whose model declares
 * {@code builtin/entity} are drawn by this renderer instead of a baked model.
 *
 * <p>The 1.12.2 transform sequence is kept verbatim; the only change is that the item transform is
 * the {@link PoseStack} the item renderer already set up, and that the joint model needs an explicit
 * {@link MultiBufferSource} / light / overlay instead of the fixed-function GL state.
 */
@OnlyIn(Dist.CLIENT)
public class JointItemRenderer extends BlockEntityWithoutLevelRenderer {
   private final int jointType;

   public JointItemRenderer(int jointType) {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
      this.jointType = jointType;
   }

   @Override
   public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      pose.pushPose();
      pose.translate(0.0F, -0.1F, 0.0F);
      pose.translate(0.5F, 1.0F, 0.5F);
      if (this.jointType != 15 && this.jointType != 12) {
         pose.mulPose(Axis.YP.rotationDegrees(90.0F));
      } else {
         pose.mulPose(Axis.XP.rotationDegrees(180.0F));
      }

      pose.translate(-0.5F, -1.0F, -0.5F);
      BasicJointRenderer.renderBasicModel(pose, bufferSource, packedLight, packedOverlay, this.jointType, false);
      pose.popPose();
   }
}
