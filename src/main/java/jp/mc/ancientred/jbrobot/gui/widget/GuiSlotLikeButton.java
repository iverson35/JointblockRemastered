package jp.mc.ancientred.jbrobot.gui.widget;

import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiSlotLikeButton}: a 16x16 button that draws the merchant entry's / package's item
 * icon and a translucent hover/selection wash instead of the vanilla button body.
 *
 * <p>1.20.1 substitutions: the {@code func_73734_a} selection quad becomes
 * {@link GuiGraphics#fill} (same ARGB values, {@code 0x80FFFF00} selected / {@code 0x80FFFFFF}
 * hovered); the direct {@code GlStateManager}/{@code GL11} colour-mask + lighting toggles around it
 * are unnecessary - {@code fill} blends and the item draw sets up its own state; {@code RenderItem
 * .renderItemAndEffectIntoGUI} -> {@link GuiGraphics#renderItem(ItemStack, int, int)} (1.12.2 bound
 * the block atlas itself, 1.20.1 travels with the render type); the explicit
 * {@code TextureMap} bind is dropped for the same reason.
 */
@OnlyIn(Dist.CLIENT)
public class GuiSlotLikeButton extends GuiButtonJB implements IJBGuiWidget {
   public IJBRMerchantEntry merchantEntry;
   public IJBRMerchantPackage packageEntry;
   public int index;
   public boolean isSelected;

   public GuiSlotLikeButton(int id, int xPos, int yPos) {
      super(id, xPos, yPos, 16, 16, Component.empty());
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
      int k = this.getHoverState(this.isHovered);
      if (k == 2 || this.isSelected) {
         if (this.isSelected) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x80FFFF00);
         } else {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x80FFFFFF);
         }
      }

      ItemStack itemStack;
      if (this.merchantEntry != null) {
         itemStack = this.merchantEntry.getEntryItemStack();
         if (CompatHelper.isStackEmptyOrNull(itemStack)) {
            return;
         }

         guiGraphics.renderItem(itemStack, this.getX(), this.getY());
      } else if (this.packageEntry != null) {
         itemStack = this.packageEntry.getEntryIconStack();
         if (CompatHelper.isStackEmptyOrNull(itemStack)) {
            return;
         }

         guiGraphics.renderItem(itemStack, this.getX(), this.getY());
      }
   }

   /** 1.12.2 {@code GuiButton.func_146114_a}: 0 = disabled, 1 = normal, 2 = highlighted. */
   public int getHoverState(boolean mouseOver) {
      if (!this.active) {
         return 0;
      }

      return mouseOver ? 2 : 1;
   }

   public int getHoverState() {
      return this.getHoverState(this.isHovered);
   }
}
