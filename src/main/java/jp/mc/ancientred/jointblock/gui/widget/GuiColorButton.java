package jp.mc.ancientred.jointblock.gui.widget;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import jp.mc.ancientred.jointblock.gui.GuiJointCorePalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiColorButton}: one palette entry of {@link GuiJointCorePalette}.
 * <p>1.20.1 substitutions: the 1.12 {@code func_73734_a} rectangles are
 * {@link GuiGraphics#fill(int, int, int, int, int)} of the same coordinates/colours, and the
 * {@code func_73732_a} override (left aligned, no shadow: {@code FontRenderer.func_78276_b}) is
 * {@link GuiGraphics#drawString(Font, String, int, int, int, boolean)} with {@code false}.
 */
@OnlyIn(Dist.CLIENT)
public class GuiColorButton extends GuiButtonJB {
   public final int paletteIndex;
   private WeakReference<GuiJointCorePalette> parentWeak;

   public GuiColorButton(int id, int xPos, int yPos, int width, int height, Component str, int pIndex, GuiJointCorePalette parent, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, str, onPress);
      this.parentWeak = new WeakReference<>(parent);
      this.paletteIndex = pIndex;
   }

   public GuiColorButton(int id, int xPos, int yPos, int width, int height, String str, int pIndex, GuiJointCorePalette parent, Consumer<IJBGuiWidget> onPress) {
      this(id, xPos, yPos, width, height, str == null ? Component.empty() : Component.literal(str), pIndex, parent, onPress);
   }

   public GuiColorButton(int id, int xPos, int yPos, int width, int height, Component str, int pIndex, GuiJointCorePalette parent) {
      this(id, xPos, yPos, width, height, str, pIndex, parent, null);
   }

   public GuiColorButton(int id, int xPos, int yPos, int width, int height, String str, int pIndex, GuiJointCorePalette parent) {
      this(id, xPos, yPos, width, height, str, pIndex, parent, null);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         GuiJointCorePalette parent = this.parentWeak.get();
         if (parent != null) {
            parent.setActiveColorButton(this);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      GuiJointCorePalette parent = this.parentWeak.get();
      if (parent == null) {
         return;
      }

      Font font = Minecraft.getInstance().font;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
      if (parent.activeIndex == this.paletteIndex) {
         guiGraphics.fill(this.getX() - 2, this.getY() - 2, this.getX() + this.width + 2, this.getY() + this.width + 2, -16776961);
      } else {
         guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.width + 1, -1);
      }

      guiGraphics.fill(
         this.getX(),
         this.getY(),
         this.getX() + this.width,
         this.getY() + this.width,
         -16777216 | parent.getCurrentPagePaletteData()[this.paletteIndex]
      );
      String label = this.getMessage().getString();
      guiGraphics.drawString(
         font, label, this.getX() + this.width / 2 - font.width(label) / 2, this.getY() - 10, 4210752, false
      );
   }
}
