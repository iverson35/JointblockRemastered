package jp.mc.ancientred.jointblock.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jp.mc.ancientred.jointblock.entity.EntityDisassembleBall;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code Render} -> 1.20.1 {@link EntityRenderer}&lt;{@link EntityDisassembleBall}&gt;.
 *
 * <p>GL substitutions:
 * <ul>
 *   <li>{@code GlStateManager.pushMatrix/popMatrix/translate/scale} -> {@link PoseStack};
 *       {@code enableRescaleNormal}/{@code disableRescaleNormal} have no 1.20.1 counterpart and are
 *       dropped.</li>
 *   <li>The 1.12.2 billboard rotations {@code rotate(-renderManager.playerViewY, Y)} +
 *       {@code rotate(renderManager.playerViewX, X)} are the same net orientation as the vanilla
 *       1.20.1 {@code entityRenderDispatcher.cameraOrientation()} plus
 *       {@code Axis.YP.rotationDegrees(180)}, which is what vanilla's own thrown-item renderer
 *       uses.</li>
 *   <li>{@code bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)} + {@code RenderItem#renderItem(...,
 *       TransformType.GROUND)} -> {@link ItemRenderer#renderStatic} with
 *       {@link ItemDisplayContext#GROUND}; the texture is implied by the item model and
 *       {@link #getTextureLocation} keeps returning the block atlas.</li>
 *   <li>1.12.2 damage-value subtypes ({@code ItemStack(item, 1, meta)}) are the port's
 *       {@code subtype} NBT tag ({@code createSubtypedStack}).</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityDisassembleBall extends EntityRenderer<EntityDisassembleBall> {

   private final ItemRenderer renderItem;
   private ItemStack stackForFenderDBall;
   private ItemStack stackForFenderMount;
   private ItemStack stackForFenderCapsule;

   public RenderEntityDisassembleBall(EntityRendererProvider.Context context) {
      super(context);
      this.renderItem = context.getItemRenderer();
      this.shadowRadius = 0.0F;
   }

   /** 1.12.2 {@code func_76986_a}. */
   @Override
   public void render(EntityDisassembleBall entity, float entityYaw, float partialTick, PoseStack pose,
                      MultiBufferSource bufferSource, int packedLight) {
      ItemStack stackForRender;
      switch (entity.type) {
         case mount:
            if (this.stackForFenderMount == null) {
               this.stackForFenderMount = JBItems.DisassembleBallItem.createSubtypedStack(1);
            }

            stackForRender = this.stackForFenderMount;
            break;
         case capsule:
            if (this.stackForFenderCapsule == null) {
               this.stackForFenderCapsule = JBItems.BluePrintItem.createSubtypedStack(3);
            }

            stackForRender = this.stackForFenderCapsule;
            break;
         case disassemble:
         default:
            if (this.stackForFenderDBall == null) {
               this.stackForFenderDBall = JBItems.DisassembleBallItem.createSubtypedStack(0);
            }

            stackForRender = this.stackForFenderDBall;
      }

      if (this.renderItem == null || stackForRender == null) {
         return;
      }

      pose.pushPose();
      pose.scale(0.5F, 0.5F, 0.5F);
      pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      this.renderItem.renderStatic(stackForRender, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
         pose, bufferSource, entity.level(), entity.getId());
      pose.popPose();
   }

   /** 1.12.2 {@code func_110775_a} returned the block atlas. */
   @Override
   public ResourceLocation getTextureLocation(EntityDisassembleBall entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
