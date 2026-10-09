package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldJP;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointBlockExtra}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code drawTexturedModalRect} -> {@code GuiGraphics.blit};
 * {@code drawRect} -> {@code GuiGraphics.fill}; {@code RenderHelper.enableStandardItemLighting/
 * disableStandardItemLighting} around the manual memo-field draw is dropped (1.20.1 widget
 * rendering is unlit).
 *
 * <p>{@code super.renderLabels} is intentionally not called: the 1.12 base
 * {@code GuiContainer.drawGuiContainerForegroundLayer} was empty, whereas the 1.20.1 base also draws
 * the ({@code Component.empty()}) screen title and the player-inventory label, which this 1.12 GUI
 * never had.
 *
 * <p>Dropped with no 1.20.1 counterpart: {@code Keyboard.enableRepeatEvents} and the manual
 * {@code handleKeyboardInput} input pump.
 */
@OnlyIn(Dist.CLIENT)
public class GuiJointBlockExtra extends GuiContainerWithTip<ContainerJointBlockExtra> {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   public static final int CheckBoxTransparentAlwaysID = 10;
   public static final int CheckBoxTransparentOnBoardID = 12;
   public static final int CheckBoxWireframeOnBoardID = 14;
   public static final int CheckBoxVisibleOnlyOnBoardID = 16;
   public static final int CheckBoxCoreForceTPSID = 20;
   public static final int CheckBoxCoreMakeTPSFarID = 22;
   public static final int CheckBoxCoreTPSFarLV_x2_ID = 24;
   public static final int CheckBoxCoreTPSFarLV_x3_ID = 25;
   public static final int CheckBoxCoreTPSFarLV_x4_ID = 26;
   public static final int CheckBoxCoreTPSFarLV_x5_ID = 27;
   public static final int CheckBoxCoreSlaveMotIndependentID = 30;
   public static final int CheckBoxCoreSlaveOpIndependentID = 32;
   public static final int CheckBoxCoreManualSetLargeWeightID = 34;
   public static final int ButtonCoreAddOneLargeWeightID = 50;
   public static final int ButtonCoreAddTenLargeWeightID = 52;
   public static final int ButtonCoreSubOneLargeWeightID = 54;
   public static final int ButtonCoreSubTenLargeWeightID = 56;
   public static final int CheckBoxTypeSpecCommonAID = 41;
   public static final int CheckBoxTypeSpecCommonBID = 42;
   public static final int CheckBoxTypeSpecCommonCID = 43;
   public static final int TexJPID = 99;
   private GuiTabSetJoint tabSetJoint;
   private GuiCheckBoxNoShadow CheckBoxTransparentAlways;
   private GuiCheckBoxNoShadow CheckBoxTransparentOnBoard;
   private GuiCheckBoxNoShadow CheckBoxWireframeOnBoard;
   private GuiCheckBoxNoShadow CheckBoxVisibleOnlyOnBoard;
   private GuiCheckBoxNoShadow[] relatedListCommon;
   private GuiCheckBoxNoShadow[] checkBoxTpsFarLVNumsList;
   private GuiCheckBoxNoShadow CheckBoxCoreForceTPS;
   private GuiCheckBoxNoShadow CheckBoxCoreMakeTPSFar;
   private GuiCheckBoxNoShadow CheckBoxCoreTPSFarLV_x2;
   private GuiCheckBoxNoShadow CheckBoxCoreTPSFarLV_x3;
   private GuiCheckBoxNoShadow CheckBoxCoreTPSFarLV_x4;
   private GuiCheckBoxNoShadow CheckBoxCoreTPSFarLV_x5;
   private GuiCheckBoxNoShadow CheckBoxCoreSlaveMotIndependent;
   private GuiCheckBoxNoShadow CheckBoxCoreSlaveOpIndependent;
   private GuiCheckBoxNoShadow CheckBoxCoreManualSetLargeWeight;
   private GuiButtonJB ButtonCoreAddOneLargeWeight;
   private GuiButtonJB ButtonCoreAddTenLargeWeight;
   private GuiButtonJB ButtonCoreSubOneLargeWeight;
   private GuiButtonJB ButtonCoreSubTenLargeWeight;
   private GuiCheckBoxNoShadow CheckBoxTypeSpecCommonA;
   private GuiCheckBoxNoShadow CheckBoxTypeSpecCommonB;
   private GuiCheckBoxNoShadow CheckBoxTypeSpecCommonC;
   private GuiTextFieldJP TextFieldMemoStr;
   TileEntityJoint te;
   private Player player;
   private JBGuiSettingsImpl guiEntrySettings;

   public GuiJointBlockExtra(ContainerJointBlockExtra menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.te;
      this.player = menu.entityplayer;
      this.imageHeight = 222;
      this.guiEntrySettings = new JBGuiSettingsImpl();
   }

   private boolean isCoreSetting() {
      return this.te instanceof TileEntityJointCore;
   }

   private boolean isAttracterSetting() {
      return this.te instanceof TileEntityJointAttracter;
   }

   private boolean isSeatSetting() {
      return this.te.jointType == 2;
   }

   @Override
   public void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      List<AbstractWidget> tabButtonList = new ArrayList<>();
      this.tabSetJoint = new GuiTabSetJoint(tabButtonList, this::actionPerformed, xLeft - 22, yTop, this.te instanceof TileEntityJointCore ? GuiTabSetJoint.TabSetType.core : GuiTabSetJoint.TabSetType.normal, 1, false);
      for (AbstractWidget widget : tabButtonList) {
         this.addRenderableWidget(widget);
      }
      int buttonWidth = this.imageWidth - 14;
      int buttonShiftH = buttonWidth + 2;
      int checkBoxLeft = 15;
      this.addRenderableWidget(this.CheckBoxTransparentAlways = new GuiCheckBoxNoShadow(10, xLeft + checkBoxLeft, yTop + 33, CompatHelper.formatLang("jointGui.info.transparentalways"), this.te.isExtraFlagsTransparentAlways(), this::actionPerformed));
      this.addRenderableWidget(this.CheckBoxTransparentOnBoard = new GuiCheckBoxNoShadow(12, xLeft + checkBoxLeft, yTop + 48, CompatHelper.formatLang("jointGui.info.transparentonboard"), this.te.isExtraFlagsTransparentOnBoard(), this::actionPerformed));
      this.addRenderableWidget(this.CheckBoxVisibleOnlyOnBoard = new GuiCheckBoxNoShadow(16, xLeft + checkBoxLeft, yTop + 63, CompatHelper.formatLang("jointGui.info.visibleonlyonboard"), this.te.isExtraFlagsVisibleOnlyOnBoard(), this::actionPerformed));
      this.addRenderableWidget(this.CheckBoxWireframeOnBoard = new GuiCheckBoxNoShadow(14, xLeft + checkBoxLeft, yTop + 78, CompatHelper.formatLang("jointGui.info.wireframeonboard"), this.te.isExtraFlagsWireframeOnBoard(), this::actionPerformed));
      this.CheckBoxTransparentAlways.packedFGColour = 0;
      this.CheckBoxWireframeOnBoard.packedFGColour = 0;
      this.relatedListCommon = new GuiCheckBoxNoShadow[]{this.CheckBoxTransparentAlways, this.CheckBoxTransparentOnBoard, this.CheckBoxVisibleOnlyOnBoard, this.CheckBoxWireframeOnBoard};
      this.TextFieldMemoStr = new GuiTextFieldJP(99, this.font, xLeft + 110, yTop + 5, 60, 18);
      this.TextFieldMemoStr.setTextColor(-1);
      this.TextFieldMemoStr.setTextColorUneditable(-1);
      this.TextFieldMemoStr.setBordered(true);
      this.TextFieldMemoStr.setMaxLength(5);
      if (!JBCommonUtil.isStringEmpty(this.te.memoStr)) {
         this.TextFieldMemoStr.setValue(this.te.memoStr);
      } else {
         this.TextFieldMemoStr.setValue("");
      }

      int othersYtop = yTop + 90 + 15;
      if (this.isCoreSetting()) {
         TileEntityJointCore teCore = (TileEntityJointCore)this.te;
         this.addRenderableWidget(this.CheckBoxCoreForceTPS = new GuiCheckBoxNoShadow(20, xLeft + checkBoxLeft, othersYtop, CompatHelper.formatLang("jointGui.info.forcetpv"), teCore.isForceFirstPersonViewWhenBoard(), this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreMakeTPSFar = new GuiCheckBoxNoShadow(22, xLeft + checkBoxLeft, othersYtop + 15, CompatHelper.formatLang("jointGui.info.maketpvfar"), teCore.isChangeThirdPersonViewDirtanceFar(), this::actionPerformed));
         int yPos = othersYtop + 30;
         int leftBase = xLeft + 50;
         int span = 30;
         this.addRenderableWidget(this.CheckBoxCoreTPSFarLV_x2 = new GuiCheckBoxNoShadow(24, leftBase + 0 * span, yPos, "x2", teCore.getThirdPersonViewFarLevel() == 0, this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreTPSFarLV_x3 = new GuiCheckBoxNoShadow(25, leftBase + 1 * span, yPos, "x3", teCore.getThirdPersonViewFarLevel() == 1, this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreTPSFarLV_x4 = new GuiCheckBoxNoShadow(26, leftBase + 2 * span, yPos, "x4", teCore.getThirdPersonViewFarLevel() == 2, this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreTPSFarLV_x5 = new GuiCheckBoxNoShadow(27, leftBase + 3 * span, yPos, "x5", teCore.getThirdPersonViewFarLevel() == 3, this::actionPerformed));
         this.CheckBoxCoreForceTPS.packedFGColour = 0;
         this.CheckBoxCoreMakeTPSFar.packedFGColour = 0;
         span = 60;
         this.addRenderableWidget(this.CheckBoxCoreSlaveMotIndependent = new GuiCheckBoxNoShadow(30, leftBase + 0 * span, othersYtop + 96, CompatHelper.formatLang("jointGui.info.slaveindependent.mot"), AssembleFlagsUtil.isSlaveMotIndependent(teCore.assembleFlags), this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreSlaveOpIndependent = new GuiCheckBoxNoShadow(32, leftBase + 1 * span, othersYtop + 96, CompatHelper.formatLang("jointGui.info.slaveindependent.op"), AssembleFlagsUtil.isSlaveOpIndependent(teCore.assembleFlags), this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxCoreManualSetLargeWeight = new GuiCheckBoxNoShadow(34, xLeft + checkBoxLeft, othersYtop + 44, CompatHelper.formatLang("jointGui.info.manualsetlargeweight"), AssembleFlagsUtil.isManualSetLargeWeight(teCore.assembleFlags), this::actionPerformed));
         int center = xLeft + this.imageWidth / 2;
         int buttonW = 20;
         int buttonH = 20;
         int buttonPad = 2;
         int buttonRowY = othersYtop + 59;
         int numAreaWH = 11;
         this.addRenderableWidget(this.ButtonCoreSubTenLargeWeight = new GuiButtonJB(56, center - numAreaWH - buttonW * 2 - buttonPad, buttonRowY, buttonW, buttonH, "-10", this::actionPerformed));
         this.addRenderableWidget(this.ButtonCoreSubOneLargeWeight = new GuiButtonJB(54, center - numAreaWH - buttonW * 1, buttonRowY, buttonW, buttonH, "-1", this::actionPerformed));
         this.addRenderableWidget(this.ButtonCoreAddOneLargeWeight = new GuiButtonJB(50, center + numAreaWH + buttonW * 0, buttonRowY, buttonW, buttonH, "+1", this::actionPerformed));
         this.addRenderableWidget(this.ButtonCoreAddTenLargeWeight = new GuiButtonJB(52, center + numAreaWH + buttonW * 1 + buttonPad, buttonRowY, buttonW, buttonH, "+10", this::actionPerformed));
         this.checkBoxTpsFarLVNumsList = new GuiCheckBoxNoShadow[]{this.CheckBoxCoreTPSFarLV_x2, this.CheckBoxCoreTPSFarLV_x3, this.CheckBoxCoreTPSFarLV_x4, this.CheckBoxCoreTPSFarLV_x5};
      } else if (this.isAttracterSetting()) {
         this.addRenderableWidget(this.CheckBoxTypeSpecCommonA = new GuiCheckBoxNoShadow(41, xLeft + checkBoxLeft, othersYtop, CompatHelper.formatLang("jointGui.info.hideforcefield"), this.te.isExtraFlagsTypeSpecCommonA(), this::actionPerformed));
      } else if (this.isSeatSetting()) {
         this.addRenderableWidget(this.CheckBoxTypeSpecCommonA = new GuiCheckBoxNoShadow(41, xLeft + checkBoxLeft, othersYtop, CompatHelper.formatLang("jointGui.info.hideplayer"), this.te.isExtraFlagsTypeSpecCommonA(), this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxTypeSpecCommonB = new GuiCheckBoxNoShadow(42, xLeft + checkBoxLeft, othersYtop + 15, CompatHelper.formatLang("jointGui.info.autodetectcam"), this.te.isExtraFlagsTypeSpecCommonB(), this::actionPerformed));
         this.addRenderableWidget(this.CheckBoxTypeSpecCommonC = new GuiCheckBoxNoShadow(43, xLeft + checkBoxLeft, othersYtop + 30, CompatHelper.formatLang("jointGui.info.syncridersyaw"), this.te.isExtraFlagsTypeSpecCommonC(), this::actionPerformed));
      }

      this.updateInputStates();
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      if (this.isCoreSetting()) {
         boolean isParentChecked = this.CheckBoxCoreMakeTPSFar.isChecked();

         for (int i = 0; i < this.checkBoxTpsFarLVNumsList.length; ++i) {
            this.checkBoxTpsFarLVNumsList[i].active = isParentChecked;
         }

         boolean manualSetLW = this.CheckBoxCoreManualSetLargeWeight.isChecked();
         this.ButtonCoreAddOneLargeWeight.active = manualSetLW;
         this.ButtonCoreAddTenLargeWeight.active = manualSetLW;
         this.ButtonCoreSubOneLargeWeight.active = manualSetLW;
         this.ButtonCoreSubTenLargeWeight.active = manualSetLW;
      }
   }

   private void toggleRelatedCheckbox(IJBGuiWidget widget, GuiCheckBoxNoShadow[] relatedList, boolean alwaysCheckOne) {
      for (int i = 0; i < relatedList.length; ++i) {
         if (relatedList[i] == widget) {
            if (relatedList[i].isChecked()) {
               for (int x = 0; x < relatedList.length; ++x) {
                  if (relatedList[x] != widget) {
                     relatedList[x].setIsChecked(false);
                  }
               }

               return;
            } else {
               if (alwaysCheckOne) {
                  relatedList[i].setIsChecked(true);
               }
               break;
            }
         }
      }
   }

   protected void actionPerformed(IJBGuiWidget widget) {
      if (this.player != null) {
         this.toggleRelatedCheckbox(widget, this.relatedListCommon, false);
         if (this.isCoreSetting()) {
            this.toggleRelatedCheckbox(widget, this.checkBoxTpsFarLVNumsList, true);
         }

         int flags_or_amount = 0;
         label131:
         switch (widget.getId()) {
            case 10:
            case 12:
            case 14:
            case 16:
               flags_or_amount |= this.CheckBoxTransparentAlways.isChecked() ? Integer.MIN_VALUE : 0;
               flags_or_amount |= this.CheckBoxTransparentOnBoard.isChecked() ? 1073741824 : 0;
               flags_or_amount |= this.CheckBoxWireframeOnBoard.isChecked() ? 536870912 : 0;
               flags_or_amount |= this.CheckBoxVisibleOnlyOnBoard.isChecked() ? 33554432 : 0;
            case 11:
            case 13:
            case 15:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
            case 28:
            case 29:
            case 31:
            case 33:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 53:
            case 55:
            default:
               break;
            case 20:
            case 22:
               if (this.isCoreSetting() && widget instanceof GuiCheckBoxNoShadow) {
                  flags_or_amount = ((GuiCheckBoxNoShadow)widget).isChecked() ? 1 : 0;
                  break;
               }

               return;
            case 24:
            case 25:
            case 26:
            case 27:
               if (!this.isCoreSetting() || !(widget instanceof GuiCheckBoxNoShadow)) {
                  return;
               }

               int i = 0;

               while (true) {
                  if (i >= this.checkBoxTpsFarLVNumsList.length) {
                     break label131;
                  }

                  if (this.checkBoxTpsFarLVNumsList[i] == widget) {
                     flags_or_amount = this.checkBoxTpsFarLVNumsList[i].isChecked() ? 1 : 0;
                     break label131;
                  }

                  ++i;
               }
            case 30:
               flags_or_amount |= this.CheckBoxCoreSlaveMotIndependent.isChecked() ? 1 : 0;
               break;
            case 32:
               flags_or_amount |= this.CheckBoxCoreSlaveOpIndependent.isChecked() ? 1 : 0;
               break;
            case 34:
               flags_or_amount |= this.CheckBoxCoreManualSetLargeWeight.isChecked() ? 1 : 0;
               break;
            case 41:
               flags_or_amount = this.CheckBoxTypeSpecCommonA.isChecked() ? 1 : 0;
               break;
            case 42:
               flags_or_amount = this.CheckBoxTypeSpecCommonB.isChecked() ? 1 : 0;
               break;
            case 43:
               flags_or_amount = this.CheckBoxTypeSpecCommonC.isChecked() ? 1 : 0;
               break;
            case 50:
               if (!(this.te instanceof TileEntityJointCore)) {
                  return;
               }

               flags_or_amount = Mth.clamp(((TileEntityJointCore)this.te).largeUnitWeightFixed + 1, 1, 300);
               break;
            case 52:
               if (!(this.te instanceof TileEntityJointCore)) {
                  return;
               }

               flags_or_amount = Mth.clamp(((TileEntityJointCore)this.te).largeUnitWeightFixed + 10, 1, 300);
               break;
            case 54:
               if (!(this.te instanceof TileEntityJointCore)) {
                  return;
               }

               flags_or_amount = Mth.clamp(((TileEntityJointCore)this.te).largeUnitWeightFixed - 1, 1, 300);
               break;
            case 56:
               if (!(this.te instanceof TileEntityJointCore)) {
                  return;
               }

               flags_or_amount = Mth.clamp(((TileEntityJointCore)this.te).largeUnitWeightFixed - 10, 1, 300);
         }

         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(widget.getId(), 0, flags_or_amount, (String)null));
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int eventButton) {
      boolean result = super.mouseClicked(mouseX, mouseY, eventButton);
      boolean isTextFocused = this.TextFieldMemoStr.isFocused();
      this.TextFieldMemoStr.mouseClicked(mouseX, mouseY, eventButton);
      if (isTextFocused && !this.TextFieldMemoStr.isFocused()) {
         String memoStr = this.TextFieldMemoStr.getValue();
         if (!memoStr.equals(this.te.memoStr)) {
            JBPacketSender.sendJointGuiStringChanged(memoStr, (byte)20, 0);
         }
      }

      return result;
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      return this.TextFieldMemoStr.textboxKeyTyped(typedChar, 0) || super.charTyped(typedChar, modifiers);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.TextFieldMemoStr.textboxKeyTyped((char)0, keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.extrasetting.title"), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.extrasetting.common"), 8, 20, 0, false);
      String memoTitle = CompatHelper.formatLang("jointGui.info.memostr");
      int w = this.font.width(memoTitle);
      guiGraphics.drawString(this.font, memoTitle, 110 - w, 10, 0, false);
      int othersYtop = 94;
      if (this.isCoreSetting()) {
         TileEntityJointCore teCore = (TileEntityJointCore)this.te;
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.extrasetting.core"), 8, othersYtop, 0, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.slaveindependent"), 15, othersYtop + 99, 0, false);
         int lwTextCol = this.CheckBoxCoreManualSetLargeWeight.isChecked() ? 0 : 10066329;
         int center = this.imageWidth / 2;
         String str = "" + teCore.largeUnitWeightFixed;
         int strW = this.font.width(str);
         guiGraphics.drawString(this.font, str, center - strW / 2, othersYtop + 79, lwTextCol, false);
         str = CompatHelper.formatLang("jointGui.info.slow");
         strW = this.font.width(str);
         guiGraphics.drawString(this.font, str, 34 - strW, othersYtop + 79, lwTextCol, false);
         str = CompatHelper.formatLang("jointGui.info.fast");
         guiGraphics.drawString(this.font, str, this.imageWidth - 34, othersYtop + 79, lwTextCol, false);
      } else if (this.isAttracterSetting()) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.extrasetting.attracter"), 8, othersYtop, 0, false);
      } else if (this.isSeatSetting()) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.extrasetting.seat"), 8, othersYtop, 0, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.syncridersyaw.desc"), 30, othersYtop + 15 + 45, 0, false);
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture plus the inner panel
    * filled rect.  {@code bindTexture(resource)} + {@code drawTexturedModalRect} become
    * {@code blit(resource, ...)}; {@code drawRect} becomes {@code fill}.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int L = (this.width - this.imageWidth) / 2;
      int T = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resource, L, T, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.fill(L + 7, T + 10, L + 169, T + 218, -3750202);
   }

   /**
    * 1.12.2 {@code drawScreen} only added the manual {@code drawTextBox()} of the memo field after
    * the standard container draw (the field is a plain widget here, not an
    * {@code addRenderableWidget()} entry, so it is not drawn automatically).
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.TextFieldMemoStr.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
   }
}
