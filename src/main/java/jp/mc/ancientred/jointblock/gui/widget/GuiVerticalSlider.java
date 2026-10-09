package jp.mc.ancientred.jointblock.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiVerticalSlider} (Forge {@code GuiButtonExt}): a vertical value slider.
 * <p>1.20.1 substitutions: Forge's {@code GuiUtils.drawContinuousTexturedBox} track and knob become
 * {@link GuiGraphics#blitNineSliced} of the same {@code textures/gui/widgets.png} sprite rectangles
 * (track at 0,46 and knob at 0,66), matching what {@code AbstractSliderButton} does in 1.20.1; the
 * {@code func_146114_a} override that pinned the track row to 0 is inlined as the constant 0, and the
 * drag update half of {@code func_146119_b} lives in {@link #mouseDragged} instead of running per
 * frame.  The knob height the 1.12 render computed is computed by
 * {@link #updateSliderButtonHeight()} in both the render and the mouse handlers so the drag maths stays
 * identical.
 */
@OnlyIn(Dist.CLIENT)
public class GuiVerticalSlider extends GuiButtonJB {
   private static final int SLIDERBTN_MIN_HIGHT = 10;
   public static final double EPSILON = 1.0E-5;
   public int sliderButtonHight = 20;
   public double sliderValue = 1.0;
   public boolean dragging = false;
   public double minValue = 0.0;
   public double maxValue = 5.0;

   public GuiVerticalSlider(int id, int xPos, int yPos, int width, int height, double minVal, double maxVal, double currentVal) {
      this(id, xPos, yPos, width, height, minVal, maxVal, currentVal, null);
   }

   public GuiVerticalSlider(int id, int xPos, int yPos, int width, int height, double minVal, double maxVal, double currentVal, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, Component.empty(), onPress);
      this.minValue = minVal;
      this.maxValue = maxVal;
      this.sliderValue = (currentVal - this.minValue) / (this.maxValue - this.minValue);
   }

   private void updateSliderButtonHeight() {
      if (this.maxValue == 0.0) {
         this.sliderButtonHight = this.height;
      } else if ((int)((double)this.height / (this.maxValue + 1.0)) < SLIDERBTN_MIN_HIGHT) {
         this.sliderButtonHight = SLIDERBTN_MIN_HIGHT;
      } else {
         this.sliderButtonHight = (int)((double)this.height / (this.maxValue + 1.0));
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      this.updateSliderButtonHeight();
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
      int k = 0;
      guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.width, this.height, 20, 4, 200, 20, 0, 46 + k * 20);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blitNineSliced(
         WIDGETS_LOCATION,
         this.getX() + 1,
         this.getY() + (int)(this.sliderValue * (double)((float)(this.height - this.sliderButtonHight))),
         8,
         this.sliderButtonHight,
         20,
         4,
         200,
         20,
         0,
         66
      );
   }

   private void updateSliderValueFromMouseY(int mouseY) {
      if (this.height - this.sliderButtonHight == 0) {
         this.sliderValue = 0.0;
      } else {
         this.sliderValue = (double)((float)(mouseY - this.getY() - this.sliderButtonHight / 2) / (float)(this.height - this.sliderButtonHight));
      }

      this.updateSlider();
   }

   @Override
   public void playDownSound(SoundManager soundManager) {
      // 1.12 GuiVerticalSlider.func_146113_a was deliberately empty: the vertical slider is silent.
   }

   @Override
   public void onClick(double mouseX, double mouseY) {
      this.updateSliderButtonHeight();
      this.updateSliderValueFromMouseY((int)mouseY);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         this.dragging = true;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.visible && this.dragging) {
         this.updateSliderValueFromMouseY((int)mouseY);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean wasDragging = this.dragging;
      this.dragging = false;
      if (wasDragging && this.onPressCallback != null) {
         this.onPressCallback.accept(this);
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public void updateSlider() {
      if (this.sliderValue < 0.0) {
         this.sliderValue = 0.0;
      }

      if (this.sliderValue > 1.0) {
         this.sliderValue = 1.0;
      }
   }

   public double getValue() {
      return this.sliderValue * (this.maxValue - this.minValue) + this.minValue;
   }

   public void setValue(double d) {
      this.sliderValue = (d - this.minValue) / (this.maxValue - this.minValue);
   }

   public void handleSliderWheelScroll(int dWheel, double rowCount) {
      if (dWheel < 0) {
         int nTypeStartRow = (int)(this.getValue() * rowCount);
         double newValue = ((double)(nTypeStartRow + 1) + EPSILON) / rowCount;
         if (newValue <= this.maxValue) {
            this.setValue(newValue);
         } else {
            this.setValue(this.maxValue);
         }
      } else if (dWheel > 0) {
         int nTypeStartRow = (int)(this.getValue() * rowCount);
         double newValue = ((double)(nTypeStartRow - 1) + EPSILON) / rowCount;
         if (newValue >= this.minValue) {
            this.setValue(newValue);
         } else {
            this.setValue(0.0);
         }
      }
   }
}
