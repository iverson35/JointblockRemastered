package jp.mc.ancientred.jointblock.gui;

import java.util.Arrays;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBUnicodeUtil;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiColorButton;
import jp.mc.ancientred.jointblock.gui.widget.GuiColorChooser;
import jp.mc.ancientred.jointblock.gui.widget.GuiColorHueChooser;
import jp.mc.ancientred.jointblock.gui.widget.GuiScreenClosable;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldEx;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiJointCorePalette extends GuiScreenClosable {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   protected int xSize = 176;
   protected int ySize = 222;
   protected int guiLeft;
   protected int guiTop;
   public static final int ButtonNumPrevID = 1;
   public static final int ButtonNumNextID = 2;
   public static final int ButtonSavePaletteID = 3;
   public static final int ButtonResetPaletteID = 4;
   public static final int ButtonCopyFromPaletteID = 5;
   public static final int ButtonCopyPaletteNumID = 6;
   public static final int ButtonPastePaletteNumID = 7;
   public static final int ColorChooserID = 10;
   public static final int ColorHueChooserID = 12;
   public static final int ButtonColor_1_ID = 20;
   public static final int ButtonColor_2_ID = 21;
   public static final int ButtonColor_3_ID = 22;
   public static final int ButtonColor_4_ID = 23;
   public static final int ButtonColor_5_ID = 24;
   public static final int ButtonColor_6_ID = 25;
   public static final int ButtonColor_B_ID = 26;
   public static final int TexExRID = 200;
   public static final int TexExGID = 201;
   public static final int TexExBID = 202;
   private GuiButtonJB ButtonNumPrev;
   private GuiButtonJB ButtonNumNext;
   private GuiButtonRed ButtonSavePalette;
   private GuiButtonJB ButtonResetPalette;
   private GuiButtonJB ButtonCopyFromPalette;
   private GuiButtonJB ButtonCopyPaletteNum;
   private GuiButtonJB ButtonPastePaletteNum;
   private GuiColorChooser ColorChooser;
   private GuiColorHueChooser ColorHueChooser;
   private GuiTextFieldEx textFieldRed;
   private GuiTextFieldEx textFieldGreen;
   private GuiTextFieldEx textFieldBlue;
   private GuiColorButton ButtonColor1;
   private GuiColorButton ButtonColor2;
   private GuiColorButton ButtonColor3;
   private GuiColorButton ButtonColor4;
   private GuiColorButton ButtonColor5;
   private GuiColorButton ButtonColor6;
   private GuiColorButton ButtonColorB;
   private Player player;
   TileEntityJointCore te;
   private int[][] tempPaletteDataList = new int[6][];
   private static int lastEdditPage = 0;
   private static int lastActiveIndex = 0;
   private static BlockPos lastAccessPos = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   public int edditingPage = 0;
   public int activeIndex = 0;
   private static int[] copyedPnumData;

   public GuiJointCorePalette(Player par1Player, TileEntityJointCore teCore) {
      this.te = teCore;
      this.player = par1Player;
      this.resetPalettte(teCore);
      if (teCore.getBlockPos().equals(lastAccessPos)) {
         this.edditingPage = lastEdditPage;
         this.activeIndex = lastActiveIndex;
      } else {
         this.edditingPage = 0;
         this.activeIndex = 7;
      }

      if (this.edditingPage < 0 || this.edditingPage >= 6) {
         this.edditingPage = 0;
      }

      if (this.activeIndex < 1 || this.activeIndex >= 8) {
         this.activeIndex = 7;
      }

   }

   @Override
   public void removed() {
      // 1.12 called Keyboard.enableRepeatEvents(false) here; that API is gone in 1.20.1 (key repeat
      // is always delivered to screens).
      lastEdditPage = this.edditingPage;
      lastActiveIndex = this.activeIndex;
      lastAccessPos = this.te.getBlockPos();
      super.removed();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      super.init();
      int xLeft = this.guiLeft = (this.width - this.xSize) / 2;
      int yTop = this.guiTop = (this.height - this.ySize) / 2;
      int buttonHeight = 20;
      this.ButtonNumPrev = new GuiButtonJB(ButtonNumPrevID, xLeft + 7, yTop + 30, 26, buttonHeight, CompatHelper.formatLang("jointGui.info.bindprev"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonNumPrev);
      this.ButtonNumNext = new GuiButtonJB(ButtonNumNextID, xLeft + this.xSize - 26 - 7, yTop + 30, 26, buttonHeight, CompatHelper.formatLang("jointGui.info.bindnext"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonNumNext);
      int buttonWidth = (this.xSize - 7 - 7 - 2) / 2;
      int buttonShiftH = buttonWidth + 2;
      this.ButtonCopyFromPalette = new GuiButtonJB(ButtonCopyFromPaletteID, xLeft + 7, yTop + 172, this.xSize - 14, buttonHeight, CompatHelper.formatLang("jointGui.info.copyfromitem"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonCopyFromPalette);
      this.ButtonResetPalette = new GuiButtonJB(ButtonResetPaletteID, xLeft + 7, yTop + 196, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.resetallpalette"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonResetPalette);
      this.ButtonSavePalette = new GuiButtonRed(ButtonSavePaletteID, this.ButtonResetPalette.getX() + buttonShiftH, yTop + 196, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.savepalette"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonSavePalette);
      this.ButtonCopyPaletteNum = new GuiButtonJB(ButtonCopyPaletteNumID, xLeft + 7, yTop + 172, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.copypalettenum"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonCopyPaletteNum);
      this.ButtonPastePaletteNum = new GuiButtonJB(ButtonPastePaletteNumID, this.ButtonCopyFromPalette.getX() + buttonShiftH, yTop + 172, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.pastepalettenum"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonPastePaletteNum);
      this.ButtonCopyPaletteNum.visible = false;
      this.ButtonPastePaletteNum.visible = false;
      this.updateCopyFromPaletteBtnEnabled();
      int cbtnWidth = 20;
      int cbtnHeight = 30;
      int c = xLeft + this.xSize / 2 - cbtnWidth / 2;
      int padding = 10;
      int side = cbtnWidth + padding;
      int sideA = (cbtnWidth + padding) / 2;
      int sideB = sideA + cbtnWidth + padding;
      int rowTop = yTop + 32;
      this.ButtonColor1 = new GuiColorButton(ButtonColor_1_ID, c - side, rowTop, cbtnWidth, cbtnHeight, "1", 1, this);
      this.addRenderableWidget(this.ButtonColor1);
      this.ButtonColor2 = new GuiColorButton(ButtonColor_2_ID, c, rowTop, cbtnWidth, cbtnHeight, "2", 2, this);
      this.addRenderableWidget(this.ButtonColor2);
      this.ButtonColor3 = new GuiColorButton(ButtonColor_3_ID, c + side, rowTop, cbtnWidth, cbtnHeight, "3", 3, this);
      this.addRenderableWidget(this.ButtonColor3);
      rowTop += 35;
      this.ButtonColor4 = new GuiColorButton(ButtonColor_4_ID, c - sideB, rowTop, cbtnWidth, cbtnHeight, "4", 4, this);
      this.addRenderableWidget(this.ButtonColor4);
      this.ButtonColor5 = new GuiColorButton(ButtonColor_5_ID, c - sideA, rowTop, cbtnWidth, cbtnHeight, "5", 5, this);
      this.addRenderableWidget(this.ButtonColor5);
      this.ButtonColor6 = new GuiColorButton(ButtonColor_6_ID, c + sideA, rowTop, cbtnWidth, cbtnHeight, "6", 6, this);
      this.addRenderableWidget(this.ButtonColor6);
      // 1.12 used the label "base" instead of "Base" when JBConfig.hasShaderModInstalled was set.
      // The shader-mod detection (CommonProxyClient.detectShaderModInstalled) was not ported and
      // JBConfig has no such field, so the default label is always used.
      this.ButtonColorB = new GuiColorButton(ButtonColor_B_ID, c + sideB, rowTop, cbtnWidth, cbtnHeight, "Base", 7, this);
      this.addRenderableWidget(this.ButtonColorB);
      int colorZoneTop = 100;
      int textLeft = 30;
      this.textFieldRed = new GuiTextFieldEx(TexExRID, this.font, xLeft + textLeft, yTop + colorZoneTop, 40, 18);
      this.textFieldGreen = new GuiTextFieldEx(TexExGID, this.font, xLeft + textLeft, this.textFieldRed.getY() + this.textFieldRed.getHeight() + 3, 40, 18);
      this.textFieldBlue = new GuiTextFieldEx(TexExBID, this.font, xLeft + textLeft, this.textFieldGreen.getY() + this.textFieldGreen.getHeight() + 3, 40, 18);
      this.textFieldRed.setTextColor(-1);
      this.textFieldRed.setTextColorUneditable(-1);
      this.textFieldRed.setBordered(true);
      this.textFieldRed.setMaxLength(3);
      this.textFieldGreen.setTextColor(-1);
      this.textFieldGreen.setTextColorUneditable(-1);
      this.textFieldGreen.setBordered(true);
      this.textFieldGreen.setMaxLength(3);
      this.textFieldBlue.setTextColor(-1);
      this.textFieldBlue.setTextColorUneditable(-1);
      this.textFieldBlue.setBordered(true);
      this.textFieldBlue.setMaxLength(3);
      this.addRenderableWidget(this.textFieldRed);
      this.addRenderableWidget(this.textFieldGreen);
      this.addRenderableWidget(this.textFieldBlue);
      this.ColorChooser = new GuiColorChooser(ColorChooserID, xLeft + 80, yTop + colorZoneTop, this);
      this.addRenderableWidget(this.ColorChooser);
      this.ColorHueChooser = new GuiColorHueChooser(ColorHueChooserID, this.ColorChooser.getX() + this.ColorChooser.getWidth() + 2, this.ColorChooser.getY());
      this.addRenderableWidget(this.ColorHueChooser);
      this.ColorChooser.setTargetTextField(this.textFieldRed, this.textFieldGreen, this.textFieldBlue);
      this.ColorHueChooser.setTargetColoerChooser(this.ColorChooser);
      this.setActiveButtonColorToTextfield();
      this.updateInputStates();
   }

   public void setActiveColorButton(GuiColorButton guiColorButton) {
      this.activeIndex = guiColorButton.paletteIndex;
      this.setActiveButtonColorToTextfield();
   }

   public int[] getCurrentPagePaletteData() {
      return this.tempPaletteDataList[this.edditingPage];
   }

   private void setActiveButtonColorToTextfield() {
      int color = this.getCurrentPagePaletteData()[this.activeIndex];
      int r = color >> 16 & 255;
      int g = color >> 8 & 255;
      int b = color & 255;
      this.textFieldRed.setValue(String.format("%1$d", r));
      this.textFieldGreen.setValue(String.format("%1$d", g));
      this.textFieldBlue.setValue(String.format("%1$d", b));
   }

   private void resetPalettte(TileEntityJointCore teCore) {
      for(int i = 0; i < teCore.paletteDataList.length; ++i) {
         if (teCore.paletteDataList[i] != null && teCore.paletteDataList[i].length == 8) {
            this.tempPaletteDataList[i] = Arrays.copyOf(teCore.paletteDataList[i], 8);
         } else {
            this.tempPaletteDataList[i] = Arrays.copyOf(TileEntityJointCore.DEFAULT_PALLETE_DATA[i], 8);
         }
      }

   }

   @Override
   public void tick() {
      super.tick();
      // 1.12 advanced the text field cursor counters inside drawTextBox(); 1.20.1 moves that to
      // EditBox.tick().
      this.textFieldRed.tick();
      this.textFieldGreen.tick();
      this.textFieldBlue.tick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      if (!this.isScreenTargetStillValid(this.player)) {
         this.player.closeContainer();
      }

      boolean isCtrlDown = hasControlDown();
      if (isCtrlDown) {
         this.ButtonCopyFromPalette.visible = false;
         this.ButtonCopyPaletteNum.visible = true;
         this.ButtonPastePaletteNum.visible = true;
         this.ButtonPastePaletteNum.active = copyedPnumData != null;
      } else {
         this.ButtonCopyPaletteNum.visible = false;
         this.ButtonPastePaletteNum.visible = false;
         this.ButtonCopyFromPalette.visible = true;
         this.updateCopyFromPaletteBtnEnabled();
      }

   }

   protected void updateCopyFromPaletteBtnEnabled() {
      this.ButtonCopyFromPalette.active = false;
      if (this.player != null) {
         ItemStack stack = CompatHelper.getCurrentEquippedItem(this.player);
         if (ItemJBCopyTool.isPaletteTool(stack) && ItemJBCopyTool.doesStackHavePaletteData(stack)) {
            this.ButtonCopyFromPalette.active = true;
         }
      }

   }

   private boolean isScreenTargetStillValid(Player playerRef) {
      return playerRef != null && BlockJointBase.getTileEntityJointerBase(playerRef.level(), this.te.getBlockPos()) != null;
   }

   @Override
   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      if (par1GuiButton == this.ButtonSavePalette) {
         if (this.isScreenTargetStillValid(this.player)) {
            int[][] sendData = new int[6][];

            for(int i = 0; i < this.tempPaletteDataList.length; ++i) {
               if (!Arrays.equals(this.tempPaletteDataList[i], TileEntityJointCore.DEFAULT_PALLETE_DATA[i])) {
                  sendData[i] = Arrays.copyOf(this.tempPaletteDataList[i], 8);
               }
            }

            BlockPos pos = this.te.getBlockPos();
            JBNetwork.sendToServer(JBPacketSender.createPaletteGUIActPacket(par1GuiButton.getId(), pos.getX(), pos.getY(), pos.getZ(), sendData));
         }

         this.player.closeContainer();
      } else if (par1GuiButton == this.ButtonNumPrev) {
         if (--this.edditingPage < 0) {
            this.edditingPage = 5;
         }

         this.setActiveButtonColorToTextfield();
      } else if (par1GuiButton == this.ButtonNumNext) {
         if (++this.edditingPage >= 6) {
            this.edditingPage = 0;
         }

         this.setActiveButtonColorToTextfield();
      } else if (par1GuiButton == this.ButtonResetPalette) {
         this.resetPalettte(this.te);
         this.setActiveButtonColorToTextfield();
      } else if (par1GuiButton == this.ButtonCopyFromPalette) {
         if (this.player != null) {
            ItemStack stack = CompatHelper.getCurrentEquippedItem(this.player);
            if (ItemJBCopyTool.isPaletteTool(stack)) {
               int[][] pDatafromItem = ItemJBCopyTool.getPaletteData(stack);
               if (pDatafromItem != null) {
                  for(int i = 0; i < pDatafromItem.length; ++i) {
                     if (pDatafromItem[i] != null && pDatafromItem[i].length == 8) {
                        this.tempPaletteDataList[i] = Arrays.copyOf(pDatafromItem[i], 8);
                     } else {
                        this.tempPaletteDataList[i] = Arrays.copyOf(TileEntityJointCore.DEFAULT_PALLETE_DATA[i], 8);
                     }
                  }
               }
            }
         }
      } else if (par1GuiButton == this.ButtonCopyPaletteNum) {
         if (this.edditingPage >= 0 && this.edditingPage < 6) {
            copyedPnumData = Arrays.copyOf(this.tempPaletteDataList[this.edditingPage], 8);
         }
      } else if (par1GuiButton == this.ButtonPastePaletteNum && this.edditingPage >= 0 && this.edditingPage < 6 && copyedPnumData != null && copyedPnumData.length == 8) {
         for(int i = 0; i < 8; ++i) {
            this.tempPaletteDataList[this.edditingPage][i] = copyedPnumData[i];
         }

         this.setActiveButtonColorToTextfield();
      }

   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int eventButton) {
      boolean wasRedFocused = this.textFieldRed.isFocused();
      boolean wasGreenFocused = this.textFieldGreen.isFocused();
      boolean wasBlueFocused = this.textFieldBlue.isFocused();
      boolean handled = super.mouseClicked(mouseX, mouseY, eventButton);
      boolean hasChanged = false;
      hasChanged |= this.fixNumStringOnFocusChange(this.textFieldRed, wasRedFocused);
      hasChanged |= this.fixNumStringOnFocusChange(this.textFieldGreen, wasGreenFocused);
      hasChanged |= this.fixNumStringOnFocusChange(this.textFieldBlue, wasBlueFocused);
      if (hasChanged) {
         this.setColorToActive();
      }

      return handled;
   }

   protected boolean fixNumStringOnFocusChange(GuiTextFieldEx checkTargetField, boolean wasTextFocused) {
      if (wasTextFocused && !checkTargetField.isFocused()) {
         String colorStr = checkTargetField.getValue();
         if (this.isEmptyOrNull(colorStr)) {
            checkTargetField.setValue("0");
            return true;
         }

         checkTargetField.moveCursorToEnd();
      } else if (!wasTextFocused && checkTargetField.isFocused()) {
         String colorStr = checkTargetField.getValue();
         if (!this.isEmptyOrNull(colorStr)) {
            checkTargetField.moveCursorToStart();
            checkTargetField.setHighlightPos(colorStr.length());
         }
      }

      return false;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      String oldRed = this.textFieldRed.getValue();
      String oldGreen = this.textFieldGreen.getValue();
      String oldBlue = this.textFieldBlue.getValue();
      boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
      this.commitTextFieldsToActiveColor(oldRed, oldGreen, oldBlue);
      return handled;
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      String oldRed = this.textFieldRed.getValue();
      String oldGreen = this.textFieldGreen.getValue();
      String oldBlue = this.textFieldBlue.getValue();
      boolean handled = super.charTyped(typedChar, modifiers);
      this.commitTextFieldsToActiveColor(oldRed, oldGreen, oldBlue);
      return handled;
   }

   private void commitTextFieldsToActiveColor(String oldRed, String oldGreen, String oldBlue) {
      int r = this.fixToValidNumString(this.textFieldRed, oldRed) & 255;
      int g = this.fixToValidNumString(this.textFieldGreen, oldGreen) & 255;
      int b = this.fixToValidNumString(this.textFieldBlue, oldBlue) & 255;
      this.getCurrentPagePaletteData()[this.activeIndex] = r << 16 | g << 8 | b;
   }

   public void setColorToActive() {
      int r = this.fixToValidNumString(this.textFieldRed, (String)null) & 255;
      int g = this.fixToValidNumString(this.textFieldGreen, (String)null) & 255;
      int b = this.fixToValidNumString(this.textFieldBlue, (String)null) & 255;
      this.getCurrentPagePaletteData()[this.activeIndex] = r << 16 | g << 8 | b;
   }

   private int fixToValidNumString(GuiTextFieldEx textField, String oldStr) {
      String str = textField.getValue();
      if (this.isEmptyOrNull(str)) {
         textField.setValue("");
         return 0;
      } else {
         if (!this.isValidColorNumber(str)) {
            if (this.isEmptyOrNull(oldStr) || !this.isValidColorNumber(oldStr)) {
               textField.setValue("");
               return 0;
            }

            textField.setValue(oldStr);
         }

         try {
            return Integer.parseInt(str);
         } catch (NumberFormatException var5) {
            textField.setValue("");
            return 0;
         }
      }
   }

   private boolean isEmptyOrNull(String str) {
      if (str == null) {
         return true;
      } else {
         return str.trim().length() == 0;
      }
   }

   private boolean isValidColorNumber(String str) {
      try {
         int num;
         for(num = 0; num < str.length(); ++num) {
            if (!JBUnicodeUtil.isNumChar(str.charAt(0))) {
               return false;
            }
         }

         num = Integer.parseInt(str);
         return num >= 0 && num <= 255;
      } catch (NumberFormatException var3) {
         return false;
      }
   }

   /**
    * 1.12.2 {@code func_73863_a} (drawScreen).  1.12 called {@code func_146276_q_()} (default
    * background) then {@code drawGuiContainerBackgroundLayer}, then {@code super.func_73863_a}
    * (widgets only) and finally {@code drawGuiContainerForegroundLayer} under a
    * {@code translate(guiLeft, guiTop)}.  1.20.1 {@code Screen.render} only draws the widgets, so
    * the default background and the two explicit layers are issued here in the same order.
    *
    * <p>Substitution: the three colour edit boxes were drawn manually with {@code drawTextBox()}
    * after the foreground layer in 1.12; here they are registered widgets and are drawn by
    * {@code super.render} (before the foreground layer).  They do not overlap the foreground text,
    * so the result is visually identical.  The GL depth/lighting toggles and the lightmap-coord
    * call around the layers have no 1.20.1 counterpart and are dropped; {@code renderBackground}
    * additionally applies the 1.20.1 world blur, which 1.12's {@code func_146276_q_()} did not have.
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      this.drawGuiContainerBackgroundLayer(guiGraphics, mouseX, mouseY);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate((float)this.guiLeft, (float)this.guiTop, 0.0F);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
      guiGraphics.pose().popPose();
   }

   /** 1.12.2 {@code drawGuiContainerForegroundLayer}. */
   public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.editpalette"), 8, 6, 4210752, false);
      String pageStr = CompatHelper.formatLang("jointGui.info.palette") + (this.edditingPage + 1);
      int pageStrWidth = this.font.width(pageStr);
      guiGraphics.drawString(this.font, pageStr, this.xSize - 9 - pageStrWidth, 6, -11184641, false);
      guiGraphics.drawString(this.font, "R", 20, 102, 4210752, false);
      guiGraphics.drawString(this.font, "G", 20, 123, 4210752, false);
      guiGraphics.drawString(this.font, "B", 20, 145, 4210752, false);
   }

   /** 1.12.2 {@code drawGuiContainerBackgroundLayer}. */
   protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int l = (this.width - this.xSize) / 2;
      int t = (this.height - this.ySize) / 2;
      guiGraphics.blit(resource, l, t, 0, 0, this.xSize, this.ySize);
      guiGraphics.fill(l + 7, t + 10, l + 169, t + 218, -3750202);
   }
}
