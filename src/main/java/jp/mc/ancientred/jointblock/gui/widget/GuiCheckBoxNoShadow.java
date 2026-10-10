package jp.mc.ancientred.jointblock.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiCheckBoxNoShadow} (Forge {@code GuiCheckBox}): a bare 11x11 box, exactly the
 * size the 1.12 widget had - {@code GuiCheckBox} passed {@code boxWidth}/{@code boxHeight} to
 * {@code GuiButton}, not the 200x20 of the plain {@code GuiButton(id, x, y, text)} constructor.
 * The label is drawn next to the box and is not part of the hit box, which is what the 1.12
 * layouts assume: the mod places check boxes 15px apart vertically and 30px apart horizontally.
 * <p>Using 200x20 made every check box swallow its neighbours (and the blank space around them),
 * so a click landed on whichever widget had been added last.
 * <p>1.20.1 substitutions: Forge's {@code GuiUtils.drawContinuousTexturedBox} 11x11 box becomes
 * {@link GuiGraphics#blitNineSliced} of the same {@code textures/gui/widgets.png} sprite (0,46);
 * the 1.12 {@code func_73731_b} override drew the label without a shadow
 * ({@link GuiGraphics#drawString(Font, String, int, int, int, boolean)} with {@code false}) and the
 * {@code func_73732_a} override drew the checked "x" shadowed through
 * {@link GuiGraphics#drawCenteredString(Font, String, int, int, int)} only while enabled.
 */
@OnlyIn(Dist.CLIENT)
public class GuiCheckBoxNoShadow extends AbstractButton implements IJBGuiWidget {
   /** 1.12 Forge {@code GuiCheckBox.boxWidth} / box height - also the widget's hit box. */
   private static final int BOX_WIDTH = 11;
   private static final int BOX_HEIGHT = 11;
   public static final int DEFAULT_FG_COLOUR = 14737632;

   public final int id;
   public int packedFGColour = DEFAULT_FG_COLOUR;
   private final Consumer<IJBGuiWidget> onPressCallback;
   private boolean isChecked;

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, Component displayString, boolean isChecked) {
      this(id, xPos, yPos, displayString, isChecked, DEFAULT_FG_COLOUR, null);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, String displayString, boolean isChecked) {
      this(id, xPos, yPos, displayString, isChecked, DEFAULT_FG_COLOUR, null);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, Component displayString, boolean isChecked, int packedFCol) {
      this(id, xPos, yPos, displayString, isChecked, packedFCol, null);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, String displayString, boolean isChecked, int packedFCol) {
      this(id, xPos, yPos, displayString, isChecked, packedFCol, null);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, Component displayString, boolean isChecked, Consumer<IJBGuiWidget> onPress) {
      this(id, xPos, yPos, displayString, isChecked, DEFAULT_FG_COLOUR, onPress);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, String displayString, boolean isChecked, Consumer<IJBGuiWidget> onPress) {
      this(id, xPos, yPos, displayString, isChecked, DEFAULT_FG_COLOUR, onPress);
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, Component displayString, boolean isChecked, int packedFCol, Consumer<IJBGuiWidget> onPress) {
      super(xPos, yPos, BOX_WIDTH, BOX_HEIGHT, displayString);
      this.id = id;
      this.isChecked = isChecked;
      this.packedFGColour = packedFCol;
      this.onPressCallback = onPress;
   }

   public GuiCheckBoxNoShadow(int id, int xPos, int yPos, String displayString, boolean isChecked, int packedFCol, Consumer<IJBGuiWidget> onPress) {
      this(id, xPos, yPos, displayString == null ? Component.empty() : Component.literal(displayString), isChecked, packedFCol, onPress);
   }

   @Override
   public int getId() {
      return this.id;
   }

   public boolean isChecked() {
      return this.isChecked;
   }

   public void setIsChecked(boolean checked) {
      this.isChecked = checked;
   }

   @Override
   public void onPress() {
      this.isChecked = !this.isChecked;
      if (this.onPressCallback != null) {
         this.onPressCallback.accept(this);
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      Font font = Minecraft.getInstance().font;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), BOX_WIDTH, BOX_HEIGHT, 20, 4, 200, 20, 0, 46);
      int color = 14737632;
      if (this.packedFGColour != 0) {
         color = this.packedFGColour;
      } else if (!this.active) {
         color = 10526880;
      }

      if (this.isChecked && this.active) {
         guiGraphics.drawCenteredString(font, "x", this.getX() + BOX_WIDTH / 2 + 1, this.getY() + 1, 14737632);
      }

      guiGraphics.drawString(font, this.getMessage().getString(), this.getX() + BOX_WIDTH + 2, this.getY() + 2, color, false);
   }

   @Override
   protected void updateWidgetNarration(NarrationElementOutput narration) {
      narration.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            narration.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
         } else {
            narration.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
         }
      }
   }
}
