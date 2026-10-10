package jp.mc.ancientred.jointblock.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiButtonRed}: a button drawn with the reddish button texture.
 * <p>1.20.1 substitutions: the two {@code drawTexturedModalRectReddish} immediate-mode quads of
 * {@code textures/gui/widgets.png} (tinted 1.0/0.4/0.4) become {@link GuiGraphics#blit} calls of the
 * same sprite halves with {@link GuiGraphics#setColor} holding that tint, reset to white at the same
 * point; {@code func_146114_a} is the {@code getHoverState} texture row; {@code func_73732_a} is
 * {@link GuiGraphics#drawCenteredString(Font, Component, int, int, int)} (shadowed, as in 1.12).
 */
@OnlyIn(Dist.CLIENT)
public class GuiButtonRed extends GuiButtonJB {
   public GuiButtonRed(int id, int xPos, int yPos, int width, int height, String str) {
      super(id, xPos, yPos, width, height, str);
   }

   public GuiButtonRed(int id, int xPos, int yPos, int width, int height, Component str) {
      super(id, xPos, yPos, width, height, str);
   }

   public GuiButtonRed(int id, int xPos, int yPos, int width, int height, String str, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, str, onPress);
   }

   public GuiButtonRed(int id, int xPos, int yPos, int width, int height, Component str, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, str, onPress);
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      Font font = Minecraft.getInstance().font;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.getHoverState();
      guiGraphics.setColor(1.0F, 0.4F, 0.4F, 1.0F);
      guiGraphics.blit(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width / 2, this.height);
      guiGraphics.blit(
         WIDGETS_LOCATION,
         this.getX() + this.width / 2,
         this.getY(),
         200 - this.width / 2,
         46 + k * 20,
         this.width / 2,
         this.height
      );
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int l = 14737632;
      if (this.packedFGColour != 0) {
         l = this.packedFGColour;
      } else if (!this.active) {
         l = 10526880;
      } else if (this.isHovered) {
         l = 16777120;
      }

      guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, l);
   }

   /** 1.12.2 {@code GuiButton.func_146114_a}: which of the three widget rows to blit. */
   private int getHoverState() {
      if (!this.active) {
         return 0;
      }

      return this.isHovered ? 2 : 1;
   }
}
