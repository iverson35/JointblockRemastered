package jp.mc.ancientred.jointblock.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiSliderShrink}: a {@code GuiSlider} whose knob is drawn narrower, plus
 * left/right half click stepping ({@code mouseLeftPressed}).
 * <p>1.20.1 substitutions: the 1.12 body blitted the two {@code textures/gui/widgets.png} halves of
 * the track ({@code func_73729_b}) and then drew the 8 px knob with Forge's
 * {@code GuiUtils.drawContinuousTexturedBox} in {@code func_146119_b}.  The halves are
 * {@link GuiGraphics#blit} and the knob is {@link GuiGraphics#blitNineSliced} of the same sprite
 * region (0,46+row*20), which is exactly what {@code AbstractSliderButton} does in 1.20.1; the drag
 * update half of {@code func_146119_b} lives in {@link #mouseDragged} instead of running per frame.
 */
@OnlyIn(Dist.CLIENT)
public class GuiSliderShrink extends GuiButtonJB {
   private float sliderPosition = 1.0F;
   public boolean isMouseDown;
   private final String name;
   private final float min;
   private final float max;
   private final GuiResponder responder;
   private final FormatHelper formatHelper;
   public float leftClickModifyDigit = 1.0F;

   public GuiSliderShrink(GuiResponder guiResponder, int idIn, int x, int y, int width, int height, String nameIn, float minIn, float maxIn, float defaultValue, FormatHelper formatter) {
      super(idIn, x, y, width, height, Component.empty());
      this.name = nameIn;
      this.min = minIn;
      this.max = maxIn;
      this.sliderPosition = (defaultValue - minIn) / (maxIn - minIn);
      this.formatHelper = formatter;
      this.responder = guiResponder;
      this.setMessage(Component.literal(this.getDisplayString()));
   }

   public float getSliderValue() {
      return this.min + (this.max - this.min) * this.sliderPosition;
   }

   public void setSliderValue(float value) {
      value = Mth.clamp(value, this.min, this.max);
      this.sliderPosition = (value - this.min) / (this.max - this.min);
      this.setMessage(Component.literal(this.getDisplayString()));
   }

   public float getSliderPosition() {
      return this.sliderPosition;
   }

   public void setSliderPosition(float position) {
      this.sliderPosition = position;
      this.setMessage(Component.literal(this.getDisplayString()));
   }

   private String getDisplayString() {
      return this.formatHelper == null
         ? I18n.get(this.name) + ": " + this.getSliderValue()
         : this.formatHelper.getText(this.id, I18n.get(this.name), this.getSliderValue());
   }

   protected int getKnobHoverState(boolean mouseOver) {
      if (!this.active) {
         return 0;
      } else {
         return mouseOver ? 2 : 1;
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      Font font = Minecraft.getInstance().font;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
      int i = 0;
      guiGraphics.blit(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + i * 20, this.width / 2, this.height);
      guiGraphics.blit(
         WIDGETS_LOCATION, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height
      );
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int hoverstate = this.getKnobHoverState(this.isHovered);
      guiGraphics.blitNineSliced(
         WIDGETS_LOCATION,
         this.getX() + (int)(this.sliderPosition * (float)(this.width - 8)),
         this.getY(),
         8,
         this.height,
         20,
         4,
         200,
         20,
         0,
         46 + hoverstate * 20
      );
      int j = 14737632;
      if (this.packedFGColour != 0) {
         j = this.packedFGColour;
      } else if (!this.active) {
         j = 10526880;
      } else if (this.isHovered) {
         j = 16777120;
      }

      guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j);
   }

   public boolean mouseLeftPressed(double mouseX, double mouseY) {
      if (!this.clicked(mouseX, mouseY)) {
         return false;
      } else {
         if (mouseX >= (double)this.getX() && mouseX < (double)(this.getX() + this.width / 2)) {
            this.setSliderValue((float)(Math.round(this.getSliderValue() / this.leftClickModifyDigit) - 1) * this.leftClickModifyDigit);
            this.responder.onTick(this.id, this.getSliderValue());
         } else if (mouseX >= (double)(this.getX() + this.width / 2) && mouseX < (double)(this.getX() + this.width)) {
            this.setSliderValue((float)(Math.round(this.getSliderValue() / this.leftClickModifyDigit) + 1) * this.leftClickModifyDigit);
            this.responder.onTick(this.id, this.getSliderValue());
         }

         return true;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         this.sliderPosition = (float)(mouseX - (double)(this.getX() + 4)) / (float)(this.width - 8);
         if (this.sliderPosition < 0.0F) {
            this.sliderPosition = 0.0F;
         }

         if (this.sliderPosition > 1.0F) {
            this.sliderPosition = 1.0F;
         }

         this.setMessage(Component.literal(this.getDisplayString()));
         this.isMouseDown = true;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.visible && this.isMouseDown) {
         this.sliderPosition = (float)(mouseX - (double)(this.getX() + 4)) / (float)(this.width - 8);
         if (this.sliderPosition < 0.0F) {
            this.sliderPosition = 0.0F;
         }

         if (this.sliderPosition > 1.0F) {
            this.sliderPosition = 1.0F;
         }

         this.setMessage(Component.literal(this.getDisplayString()));
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.isMouseDown = false;
      this.responder.onTick(this.id, this.getSliderValue());
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @OnlyIn(Dist.CLIENT)
   public interface GuiResponder {
      void onToggle(int id, boolean value);

      void setEntryValue(int id, String value);

      void onTick(int id, float value);
   }

   @OnlyIn(Dist.CLIENT)
   public interface FormatHelper {
      String getText(int id, String name, float value);
   }
}
