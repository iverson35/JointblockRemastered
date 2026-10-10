package jp.mc.ancientred.jointblock.gui.widget;

import java.lang.ref.WeakReference;
import jp.mc.ancientred.jointblock.gui.GuiJointCorePalette;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiColorChooser}: the saturation/brightness field of
 * {@link GuiJointCorePalette}.
 * <p>1.20.1 substitutions: every {@code func_73734_a} pixel/rect of the 1.12 body is
 * {@link GuiGraphics#fill(int, int, int, int, int)} with the same coordinates and colours (one quad
 * per field pixel, exactly as the 1.12 immediate-mode loop); the drag update
 * {@code func_146119_b} was moved to {@link #mouseDragged} (same clamping, same call order) and is not
 * repeated per frame.  The RGB maths feeding the three text fields is unchanged.
 */
@OnlyIn(Dist.CLIENT)
public class GuiColorChooser extends GuiButtonJB {
   private static final int BUTTON_WIDTH = 60;
   private static final int BUTTON_HEIGHT = 60;

   private boolean dragging;
   private int hueR;
   private int hueG;
   private int hueB;
   private GuiTextFieldEx setTargetRed;
   private GuiTextFieldEx setTargetGreen;
   private GuiTextFieldEx setTargetBlue;
   private int nowPointX;
   private int nowPointY;
   private WeakReference<GuiJointCorePalette> parentWeak;

   public GuiColorChooser(int id, int xPos, int yPos, GuiJointCorePalette parent) {
      super(id, xPos, yPos, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty());
      this.parentWeak = new WeakReference<>(parent);
      this.nowPointX = xPos;
      this.nowPointY = yPos;
      this.hueR = 255;
      this.hueG = 0;
      this.hueB = 0;
   }

   public void setTargetTextField(GuiTextFieldEx colorTextFieldRed, GuiTextFieldEx colorTextFieldGreen, GuiTextFieldEx colorTextFieldBlue) {
      this.setTargetRed = colorTextFieldRed;
      this.setTargetGreen = colorTextFieldGreen;
      this.setTargetBlue = colorTextFieldBlue;
   }

   public void setHue(int r, int g, int b) {
      this.hueR = r;
      this.hueG = g;
      this.hueB = b;
      this.updateTarget(this.nowPointX, this.nowPointY);
   }

   public void updateTarget(int mouseX, int mouseY) {
      int x = mouseX - this.getX();
      int y = mouseY - this.getY();
      float rateX = 1.0F - (float)x / (float)this.width;
      int rx = this.hueR + (int)((float)(255 - this.hueR) * rateX);
      int gx = this.hueG + (int)((float)(255 - this.hueG) * rateX);
      int bx = this.hueB + (int)((float)(255 - this.hueB) * rateX);
      float rateY = (float)y / (float)this.height;
      int r = (int)((float)rx * (1.0F - rateY));
      int g = (int)((float)gx * (1.0F - rateY));
      int b = (int)((float)bx * (1.0F - rateY));
      this.setTargetRed.setValue(String.format("%1$d", r));
      this.setTargetGreen.setValue(String.format("%1$d", g));
      this.setTargetBlue.setValue(String.format("%1$d", b));
      GuiJointCorePalette parent = this.parentWeak.get();
      if (parent != null) {
         parent.setColorToActive();
      }

      this.nowPointX = mouseX;
      this.nowPointY = mouseY;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.visible && this.isMousePointInArea((int)mouseX, (int)mouseY)) {
         this.dragging = true;
         this.updateTarget((int)mouseX, (int)mouseY);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.visible && this.dragging) {
         int mouseXIn = (int)mouseX;
         int mouseYIn = (int)mouseY;
         if (mouseXIn < this.getX()) {
            mouseXIn = this.getX();
         }

         if (mouseYIn < this.getY()) {
            mouseYIn = this.getY();
         }

         if (mouseXIn > this.getX() + this.width) {
            mouseXIn = this.getX() + this.width;
         }

         if (mouseYIn > this.getY() + this.height) {
            mouseYIn = this.getY() + this.height;
         }

         this.updateTarget(mouseXIn, mouseYIn);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = this.isMousePointInArea(mouseX, mouseY);

      for (int rRev = 0; rRev < this.width; ++rRev) {
         float rateX = 1.0F - (float)rRev / (float)this.width;
         int rx = this.hueR + (int)((float)(255 - this.hueR) * rateX);
         int gx = this.hueG + (int)((float)(255 - this.hueG) * rateX);
         int bx = this.hueB + (int)((float)(255 - this.hueB) * rateX);

         for (int gRev = 0; gRev < this.height; ++gRev) {
            float rateY = (float)gRev / (float)this.height;
            int r = (int)((float)rx * (1.0F - rateY));
            int g = (int)((float)gx * (1.0F - rateY));
            int b = (int)((float)bx * (1.0F - rateY));
            int posX = this.getX() + rRev;
            int posY = this.getY() + gRev;
            guiGraphics.fill(posX, posY, posX + 1, posY + 1, -16777216 | r << 16 | g << 8 | b);
         }
      }

      float rateX = 1.0F - (float)(this.nowPointX - this.getX()) / (float)this.width;
      int rx = this.hueR + (int)((float)(255 - this.hueR) * rateX);
      int gx = this.hueG + (int)((float)(255 - this.hueG) * rateX);
      int bx = this.hueB + (int)((float)(255 - this.hueB) * rateX);
      float rateY = (float)(this.nowPointY - this.getY()) / (float)this.height;
      int r = (int)((float)rx * (1.0F - rateY));
      int g = (int)((float)gx * (1.0F - rateY));
      int b = (int)((float)bx * (1.0F - rateY));
      int rRev = 255 - r;
      int gRev = 255 - g;
      int bRev = 255 - b;
      int diff = Math.abs(rRev - r) + Math.abs(gRev - g) + Math.abs(bRev - b);
      if (diff < 40) {
         rRev += 40 + diff;
         gRev += 40 + diff;
         bRev += 40 + diff;
         if (rRev > 255) {
            rRev = 255;
         }

         if (gRev > 255) {
            gRev = 255;
         }

         if (bRev > 255) {
            bRev = 255;
         }
      }

      int crossHairColor = -16777216 | rRev << 16 | gRev << 8 | bRev;
      guiGraphics.fill(this.nowPointX - 1, this.nowPointY, this.nowPointX + 2, this.nowPointY + 1, crossHairColor);
      guiGraphics.fill(this.nowPointX, this.nowPointY - 1, this.nowPointX + 1, this.nowPointY + 2, crossHairColor);
   }

   public boolean isMousePointInArea(int mouseX, int mouseY) {
      return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
   }

   public int getButtonWidth() {
      return this.width;
   }

   public int getButtonHeight() {
      return this.height;
   }
}
