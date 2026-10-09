package jp.mc.ancientred.jbrobot.gui.widget;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiTabButton}: a tab of {@link GuiTabSetCatalog}.  The tab sprite lives in the
 * JointBlock gui sheet (the 1.12.2 class referenced {@code modj_jointblock:textures/gui/
 * GuiJointBlock.png}).
 *
 * <p>1.20.1 substitutions: the {@code func_73729_b} sprite blits are {@link GuiGraphics#blit} with
 * the same uv rectangles; the 1.12 {@code RenderItem} parameter (plus its GUI lighting,
 * {@code zLevel} and {@code GL11} state around {@code renderItemIntoGUI}) is replaced by
 * {@link GuiGraphics#renderItem(ItemStack, int, int)}, which performs that setup itself; the
 * 1.12.2 public {@code itemRender} field is therefore dropped.
 */
@OnlyIn(Dist.CLIENT)
public class GuiTabButton extends GuiButtonJB implements IJBGuiWidget {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   public final int paletteIndex;
   public ItemStack itemStackForIcon;
   private int fixedIconIdx = 0;
   private final WeakReference<ITabButtonParent> parentWeak;

   public GuiTabButton(int id, int xPos, int yPos, int width, int height, int pIndex, ITabButtonParent parent, ItemStack stackItemForIcon, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, Component.empty(), onPress);
      this.parentWeak = new WeakReference<>(parent);
      this.paletteIndex = pIndex;
      this.itemStackForIcon = stackItemForIcon;
   }

   public GuiTabButton(int id, int xPos, int yPos, int width, int height, int pIndex, ITabButtonParent parent, ItemStack stackItemForIcon) {
      this(id, xPos, yPos, width, height, pIndex, parent, stackItemForIcon, (Consumer<IJBGuiWidget>)null);
   }

   public GuiTabButton(int id, int xPos, int yPos, int width, int height, int pIndex, ITabButtonParent parent, int fixedIconIdx, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, Component.empty(), onPress);
      this.parentWeak = new WeakReference<>(parent);
      this.paletteIndex = pIndex;
      this.itemStackForIcon = ItemStack.EMPTY;
      this.fixedIconIdx = fixedIconIdx;
   }

   public GuiTabButton(int id, int xPos, int yPos, int width, int height, int pIndex, ITabButtonParent parent, int fixedIconIdx) {
      this(id, xPos, yPos, width, height, pIndex, parent, fixedIconIdx, (Consumer<IJBGuiWidget>)null);
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      ITabButtonParent parent = this.parentWeak.get();
      if (parent == null) {
         return;
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
      int k = this.getHoverState(this.isHovered);
      if (k == 2) {
         guiGraphics.blit(resource, this.getX(), this.getY(), 220, 102, 22, 27);
      } else if (parent.getActiveIndex() == this.paletteIndex) {
         guiGraphics.blit(resource, this.getX(), this.getY(), 176, 102, 22, 27);
      } else {
         guiGraphics.blit(resource, this.getX(), this.getY(), 198, 102, 22, 27);
      }

      if (CompatHelper.isStackEmptyOrNull(this.itemStackForIcon)) {
         guiGraphics.blit(resource, this.getX() + 5, this.getY() + 5, 240, this.fixedIconIdx * 16, 16, 16);
      } else {
         guiGraphics.renderItem(this.itemStackForIcon, this.getX() + 5, this.getY() + 6);
      }
   }

   /** 1.12.2 {@code GuiButton.func_146114_a}: 0 = disabled, 1 = normal, 2 = highlighted. */
   public int getHoverState(boolean mouseOver) {
      if (!this.active) {
         return 0;
      }

      return mouseOver ? 2 : 1;
   }

   public interface ITabButtonParent {
      int getActiveIndex();
   }
}
