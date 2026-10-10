package jp.mc.ancientred.jointblock.gui.widget;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiColorHueChooser}: hue strip selecting the base hue of a
 * {@link GuiColorChooser}.
 * <p>1.20.1 substitutions: every {@code func_73734_a} hue row and cursor arm of the 1.12 body is
 * {@link GuiGraphics#fill(int, int, int, int, int)} with the same coordinates/colours; the drag update
 * {@code func_146119_b} was moved to {@link #mouseDragged} (same clamping) and is not repeated per
 * frame.  The hue lookup maths is unchanged.
 */
@OnlyIn(Dist.CLIENT)
public class GuiColorHueChooser extends GuiButtonJB {
   private static final int WIDTH = 10;
   private static final int PIXELROWHIGHT = 1;

   private boolean dragging;
   private int nowPointY;
   private GuiColorChooser setTarget;
   public static float[][] hueList = new float[0][];

   public GuiColorHueChooser(int id, int xPos, int yPos) {
      super(id, xPos, yPos, WIDTH, hueList.length * PIXELROWHIGHT, Component.empty());
      this.nowPointY = yPos;
   }

   public void setTargetColoerChooser(GuiColorChooser colorChooser) {
      this.setTarget = colorChooser;
   }

   public void updateTarget(int mouseX, int mouseY) {
      int inPointY = mouseY - this.getY();
      int hueIndex = inPointY / PIXELROWHIGHT;
      if (hueIndex >= 0 && hueIndex < hueList.length) {
         int r = (int)(255.0F * hueList[hueIndex][0]);
         int g = (int)(255.0F * hueList[hueIndex][1]);
         int b = (int)(255.0F * hueList[hueIndex][2]);
         this.setTarget.setHue(r, g, b);
      }

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
      int x = this.getX();

      for (int i = 0; i < hueList.length; ++i) {
         int r = (int)(255.0F * hueList[i][0]);
         int g = (int)(255.0F * hueList[i][1]);
         int b = (int)(255.0F * hueList[i][2]);
         int posY = this.getY() + i * PIXELROWHIGHT;
         guiGraphics.fill(x, posY, x + WIDTH, posY + PIXELROWHIGHT, -16777216 | r << 16 | g << 8 | b);
      }

      int cursorColor = -12566464;
      guiGraphics.fill(x + WIDTH + 2, this.nowPointY - 1, x + WIDTH + 3, this.nowPointY - 1 + 1, cursorColor);
      guiGraphics.fill(x + WIDTH + 1, this.nowPointY, x + WIDTH + 6, this.nowPointY + 1, cursorColor);
      guiGraphics.fill(x + WIDTH + 2, this.nowPointY + 1, x + WIDTH + 3, this.nowPointY + 1 + 1, cursorColor);
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

   static {
      ArrayList<float[]> lstA = new ArrayList<>();
      ArrayList<float[]> lstB = new ArrayList<>();
      ArrayList<float[]> lstC = new ArrayList<>();
      ArrayList<float[]> lstD = new ArrayList<>();
      ArrayList<float[]> lstE = new ArrayList<>();
      ArrayList<float[]> lstF = new ArrayList<>();

      for (int i = 0; i < 10; ++i) {
         float _IAf = (float)i / 10.0F;
         float _ISf = (float)(10 - i) / 10.0F;
         lstA.add(new float[]{1.0F, _IAf, 0.0F});
         lstB.add(new float[]{_ISf, 1.0F, 0.0F});
         lstC.add(new float[]{0.0F, 1.0F, _IAf});
         lstD.add(new float[]{0.0F, _ISf, 1.0F});
         lstE.add(new float[]{_IAf, 0.0F, 1.0F});
         lstF.add(new float[]{1.0F, 0.0F, _ISf});
      }

      lstA.addAll(lstB);
      lstA.addAll(lstC);
      lstA.addAll(lstD);
      lstA.addAll(lstE);
      lstA.addAll(lstF);
      hueList = lstA.toArray(hueList);
   }
}
