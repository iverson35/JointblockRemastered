package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.EntrySetting;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldEx;
import jp.mc.ancientred.jointblock.gui.widget.GuiVerticalSlider;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointBlock}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code drawTexturedModalRect} -> {@code GuiGraphics.blit};
 * {@code drawRect} -> {@code GuiGraphics.fill}; {@code drawHoveringText} (end of
 * {@code drawGuiContainerForegroundLayer}, kept here in {@link #renderLabels}) ->
 * {@code GuiGraphics.renderComponentTooltip} with the same gui-relative position;
 * {@code RenderHelper.enableStandardItemLighting/disableStandardItemLighting} around the manual
 * text-field draw is dropped (1.20.1 widget rendering is unlit).
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
public class GuiJointBlock extends GuiContainerWithTip<ContainerJointBlock> {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   public static final int ButtonBindNumPrevID = 2;
   public static final int ButtonBindNumNextID = 4;
   public static final int ButtonMotionValueID = 6;
   public static final int ButtonInitialValueID = 8;
   public static final int ButtonR1ModAddID = 10;
   public static final int ButtonR1ModSubID = 14;
   public static final int ButtonR1TglID = 16;
   public static final int ButtonR1EditID = 18;
   public static final int ButtonR2ModAddID = 20;
   public static final int ButtonR2ModSubID = 24;
   public static final int ButtonR2TglID = 26;
   public static final int ButtonR2EditID = 28;
   public static final int ButtonR3ModAddID = 30;
   public static final int ButtonR3ModSubID = 34;
   public static final int ButtonR3TglID = 36;
   public static final int ButtonR3EditID = 38;
   public static final int ButtonR4ModAddID = 40;
   public static final int ButtonR4ModSubID = 44;
   public static final int ButtonR4TglID = 46;
   public static final int ButtonR4EditID = 48;
   public static final int ButtonR1SendEditID = 81;
   public static final int ButtonR2SendEditID = 82;
   public static final int ButtonR3SendEditID = 83;
   public static final int ButtonR4SendEditID = 84;
   public static final int ButtonDelInitValID = 96;
   public static final int ButtonCopyInitValID = 97;
   public static final int ButtonPasteInitValID = 98;
   public static final int SliderID = 99;
   public static final int TexEx1ID = 200;
   public static final int TexEx2ID = 201;
   public static final int TexEx3ID = 202;
   public static final int TexEx4ID = 203;
   private static final int ROW_HIGHT = 25;
   private static int[] ROWIDX_TO_SENDID_CONV = new int[]{81, 82, 83, 84};
   private static int SEND_LARGE_AMOUNT = 100;
   private static final int KEY_ENTER = 257;
   private GuiTabSetJoint tabSetJoint;
   private GuiButtonJB ButtonBindNumPrev;
   private GuiButtonJB ButtonBindNumNext;
   private GuiButtonJB ButtonMotionValue;
   private GuiButtonJB ButtonInitialValue;
   private GuiButtonJB ButtonR1ModAdd;
   private GuiButtonJB ButtonR1ModSub;
   private GuiButtonJB ButtonR1TglCur;
   private GuiButtonJB ButtonR1Edit;
   private GuiButtonJB ButtonR2ModAdd;
   private GuiButtonJB ButtonR2ModSub;
   private GuiButtonJB ButtonR2TglCur;
   private GuiButtonJB ButtonR2Edit;
   private GuiButtonJB ButtonR3ModAdd;
   private GuiButtonJB ButtonR3ModSub;
   private GuiButtonJB ButtonR3TglCur;
   private GuiButtonJB ButtonR3Edit;
   private GuiButtonJB ButtonR4ModAdd;
   private GuiButtonJB ButtonR4ModSub;
   private GuiButtonJB ButtonR4TglCur;
   private GuiButtonJB ButtonR4Edit;
   private GuiTextFieldEx textFieldR1;
   private GuiTextFieldEx textFieldR2;
   private GuiTextFieldEx textFieldR3;
   private GuiTextFieldEx textFieldR4;
   private GuiButtonJB ButtonDelInitVal;
   private GuiButtonJB ButtonCopyInitVal;
   private GuiButtonJB ButtonPasteInitVal;
   private GuiVerticalSlider Slider;
   private GuiButtonJB[][] buttonAry;
   private GuiTextFieldEx[] textFiledAry;
   private boolean[] showTextFieldFlgAry;
   private boolean[] bypathTextFieldClick;
   TileEntityJoint te;
   boolean initlayerAddUnLock = false;
   ArrayList<String> hoverTipWorkList = new ArrayList<>();
   int prevGuiPage = -1;
   int prevMotionItemId;
   int prevBindNum;
   int prevMotionItemDamage;
   private Player player;
   private JBGuiSettingsImpl guiEntrySettings;
   private boolean isCtrlPressed = false;

   public GuiJointBlock(ContainerJointBlock menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.te;
      this.player = menu.entityplayer;
      this.imageHeight = 222;
      this.guiEntrySettings = new JBGuiSettingsImpl();
   }

   @Override
   public void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      int buttonHeight = 20;
      List<AbstractWidget> tabButtonList = new ArrayList<>();
      this.tabSetJoint = new GuiTabSetJoint(tabButtonList, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.normal, 0, true);
      for (AbstractWidget widget : tabButtonList) {
         this.addRenderableWidget(widget);
      }
      this.tabSetJoint.setVisibleMoveToTab(false);
      int buttonWidth = (this.imageWidth - 7 - 7 - 2) / 2;
      int buttonShiftH = buttonWidth + 2;
      this.addRenderableWidget(this.ButtonMotionValue = new GuiButtonJB(6, xLeft + 7, yTop + 176, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.movetosetmotion"), this::actionPerformed));
      this.addRenderableWidget(this.ButtonInitialValue = new GuiButtonJB(8, this.ButtonMotionValue.getX() + buttonShiftH, yTop + 176, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.movetosetinit"), this::actionPerformed));
      buttonWidth = 26;
      buttonShiftH = buttonWidth + 2;
      this.addRenderableWidget(this.ButtonBindNumPrev = new GuiButtonJB(2, xLeft + 112, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindprev"), this::actionPerformed));
      this.addRenderableWidget(this.ButtonBindNumNext = new GuiButtonJB(4, this.ButtonBindNumPrev.getX() + buttonShiftH, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindnext"), this::actionPerformed));
      this.addRenderableWidget(this.ButtonDelInitVal = new GuiButtonRed(96, xLeft + 10, yTop + 47, 24, buttonHeight, "Del", this::actionPerformed));
      this.addRenderableWidget(this.ButtonCopyInitVal = new GuiButtonJB(97, this.ButtonDelInitVal.getX() + this.ButtonDelInitVal.getWidth() + 2, yTop + 47, 30, buttonHeight, "Copy", this::actionPerformed));
      this.addRenderableWidget(this.ButtonPasteInitVal = new GuiButtonJB(98, this.ButtonCopyInitVal.getX() + this.ButtonCopyInitVal.getWidth() + 2, yTop + 47, 36, buttonHeight, "Paste", this::actionPerformed));
      this.ButtonPasteInitVal.active = false;
      this.addRenderableWidget(this.Slider = new GuiVerticalSlider(99, xLeft + 158, yTop + 70, 10, 104, 0.0, 0.0, 0.0, this::actionPerformed));
      buttonWidth = 20;
      buttonHeight = 20;
      buttonShiftH = buttonWidth + 1;
      int rowPadding = 5;
      int buttonXLeft = xLeft + 95;
      int buttonBndRowY = yTop + 48;
      int button1stRowY = buttonBndRowY + buttonHeight + rowPadding;
      int button2ndRowY = button1stRowY + buttonHeight + rowPadding;
      int button3rdRowY = button2ndRowY + buttonHeight + rowPadding;
      int button4thRowY = button3rdRowY + buttonHeight + rowPadding;
      String modeStr = "";
      String addStr = "";
      String subStr = "";
      this.addRenderableWidget(this.ButtonR1ModSub = new GuiButtonJB(14, buttonXLeft + buttonShiftH * 0, button1stRowY, buttonWidth, buttonHeight, subStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR1TglCur = new GuiButtonJB(16, buttonXLeft + buttonShiftH * 1, button1stRowY - 2, buttonWidth, buttonHeight - 9, modeStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR1Edit = new GuiButtonJB(18, buttonXLeft + buttonShiftH * 1, button1stRowY + 10, buttonWidth, buttonHeight - 9, "Edit", this::actionPerformed));
      this.addRenderableWidget(this.ButtonR1ModAdd = new GuiButtonJB(10, buttonXLeft + buttonShiftH * 2, button1stRowY, buttonWidth, buttonHeight, addStr, this::actionPerformed));
      this.textFieldR1 = new GuiTextFieldEx(200, this.font, buttonXLeft + buttonShiftH * 0, button1stRowY, 40, 18);
      this.addRenderableWidget(this.ButtonR2ModSub = new GuiButtonJB(24, buttonXLeft + buttonShiftH * 0, button2ndRowY, buttonWidth, buttonHeight, subStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR2TglCur = new GuiButtonJB(26, buttonXLeft + buttonShiftH * 1, button2ndRowY - 2, buttonWidth, buttonHeight - 9, modeStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR2Edit = new GuiButtonJB(28, buttonXLeft + buttonShiftH * 1, button2ndRowY + 10, buttonWidth, buttonHeight - 9, "Edit", this::actionPerformed));
      this.addRenderableWidget(this.ButtonR2ModAdd = new GuiButtonJB(20, buttonXLeft + buttonShiftH * 2, button2ndRowY, buttonWidth, buttonHeight, addStr, this::actionPerformed));
      this.textFieldR2 = new GuiTextFieldEx(201, this.font, buttonXLeft + buttonShiftH * 0, button2ndRowY, 40, 18);
      this.addRenderableWidget(this.ButtonR3ModSub = new GuiButtonJB(34, buttonXLeft + buttonShiftH * 0, button3rdRowY, buttonWidth, buttonHeight, subStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR3TglCur = new GuiButtonJB(36, buttonXLeft + buttonShiftH * 1, button3rdRowY - 2, buttonWidth, buttonHeight - 9, modeStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR3Edit = new GuiButtonJB(38, buttonXLeft + buttonShiftH * 1, button3rdRowY + 10, buttonWidth, buttonHeight - 9, "Edit", this::actionPerformed));
      this.addRenderableWidget(this.ButtonR3ModAdd = new GuiButtonJB(30, buttonXLeft + buttonShiftH * 2, button3rdRowY, buttonWidth, buttonHeight, addStr, this::actionPerformed));
      this.textFieldR3 = new GuiTextFieldEx(202, this.font, buttonXLeft + buttonShiftH * 0, button3rdRowY, 40, 18);
      this.addRenderableWidget(this.ButtonR4ModSub = new GuiButtonJB(44, buttonXLeft + buttonShiftH * 0, button4thRowY, buttonWidth, buttonHeight, subStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR4TglCur = new GuiButtonJB(46, buttonXLeft + buttonShiftH * 1, button4thRowY - 2, buttonWidth, buttonHeight - 9, modeStr, this::actionPerformed));
      this.addRenderableWidget(this.ButtonR4Edit = new GuiButtonJB(48, buttonXLeft + buttonShiftH * 1, button4thRowY + 10, buttonWidth, buttonHeight - 9, "Edit", this::actionPerformed));
      this.addRenderableWidget(this.ButtonR4ModAdd = new GuiButtonJB(40, buttonXLeft + buttonShiftH * 2, button4thRowY, buttonWidth, buttonHeight, addStr, this::actionPerformed));
      this.textFieldR4 = new GuiTextFieldEx(203, this.font, buttonXLeft + buttonShiftH * 0, button4thRowY, 40, 18);
      this.buttonAry = new GuiButtonJB[][]{{this.ButtonR1ModSub, this.ButtonR1ModAdd, this.ButtonR1TglCur, this.ButtonR1Edit}, {this.ButtonR2ModSub, this.ButtonR2ModAdd, this.ButtonR2TglCur, this.ButtonR2Edit}, {this.ButtonR3ModSub, this.ButtonR3ModAdd, this.ButtonR3TglCur, this.ButtonR3Edit}, {this.ButtonR4ModSub, this.ButtonR4ModAdd, this.ButtonR4TglCur, this.ButtonR4Edit}};
      this.textFiledAry = new GuiTextFieldEx[]{this.textFieldR1, this.textFieldR2, this.textFieldR3, this.textFieldR4};
      this.showTextFieldFlgAry = new boolean[this.textFiledAry.length];
      this.bypathTextFieldClick = new boolean[this.textFiledAry.length];

      for (int i = 0; i < this.textFiledAry.length; ++i) {
         this.textFiledAry[i].setTextColor(-1);
         this.textFiledAry[i].setTextColorUneditable(-1);
         this.textFiledAry[i].setBordered(true);
         this.textFiledAry[i].setMaxLength(4);
      }

      this.prevMotionItemId = this.te.getMotionBinderItemId();
      this.prevMotionItemDamage = this.te.getMotionItemDamage();
      this.prevBindNum = this.te.bindNum;
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
      boolean isCtrlDown = Screen.hasControlDown();
      this.tabSetJoint.setVisibleMoveToTab(isCtrlDown);
      this.guiEntrySettings.resetAll();
      this.guiEntrySettings.text = null;
      boolean hasPageChanged = false;
      if (this.prevGuiPage != this.te.guiPage) {
         this.prevGuiPage = this.te.guiPage;
         hasPageChanged = true;
      }

      boolean bindReset = false;
      int nowMotionBinderItemId = this.te.getMotionBinderItemId();
      if (this.prevMotionItemId != nowMotionBinderItemId) {
         this.prevMotionItemId = nowMotionBinderItemId;
         bindReset = true;
         hasPageChanged = true;
      }

      int nowMotionItemDamage = this.te.getMotionItemDamage();
      if (this.prevMotionItemDamage != nowMotionItemDamage) {
         this.prevMotionItemDamage = nowMotionItemDamage;
         bindReset = true;
         hasPageChanged = true;
      }

      if (bindReset) {
         this.te.bindNum = 0;
      }

      IJBMotionBinderItem.IJBMotionHandler actHndl = this.te.getBindOrFixBindStatus();
      if (this.prevBindNum != this.te.bindNum) {
         this.prevBindNum = this.te.bindNum;
         hasPageChanged = true;
      }

      if (this.te.guiPage == 0) {
         this.ButtonMotionValue.active = true;
         this.ButtonInitialValue.active = false;
         if (JBCommonUtil.jointTypeHasMotion(this.te.getJointType()) && this.te.jointType != 15) {
            this.ButtonBindNumPrev.active = this.te.initValuesLayer > 0;
            this.ButtonBindNumNext.active = this.te.initValuesLayer < JBConfig.maxInitLayers - 1;
            this.ButtonBindNumPrev.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindprev")));
            if (this.te.initValuesLayer + 1 >= this.te.initValues.length) {
               if (this.initlayerAddUnLock) {
                  this.ButtonBindNumPrev.active = true;
                  this.ButtonBindNumPrev.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.addLayerConfNo")));
                  this.ButtonBindNumNext.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.addLayerConfOk")));
               } else {
                  this.ButtonBindNumNext.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.addLayer")));
               }
            } else {
               this.initlayerAddUnLock = false;
               this.ButtonBindNumNext.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindnext")));
            }

            this.ButtonPasteInitVal.visible = this.ButtonCopyInitVal.visible = this.ButtonDelInitVal.visible = isCtrlDown;
            this.ButtonDelInitVal.active = this.te.initValues.length > 1 && this.te.initValuesLayer == this.te.initValues.length - 1;
         } else {
            this.ButtonBindNumPrev.active = false;
            this.ButtonBindNumNext.active = false;
            this.ButtonBindNumPrev.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindprev")));
            this.ButtonBindNumNext.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindnext")));
            this.ButtonPasteInitVal.visible = this.ButtonCopyInitVal.visible = this.ButtonDelInitVal.visible = false;
         }

         EntryFormats.setDefaultJointTypeButtonState(this.guiEntrySettings, true, this.te.getJointType());
      } else {
         this.initlayerAddUnLock = false;
         this.ButtonMotionValue.active = false;
         this.ButtonInitialValue.active = true;
         this.ButtonBindNumPrev.active = this.te.hasMultipleBind;
         this.ButtonBindNumNext.active = this.te.hasMultipleBind;
         this.ButtonBindNumPrev.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindprev")));
         this.ButtonBindNumNext.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.bindnext")));
         actHndl.setGuiEntryState(this.te.getJointType(), this.guiEntrySettings);
         this.ButtonPasteInitVal.visible = this.ButtonCopyInitVal.visible = this.ButtonDelInitVal.visible = false;
      }

      int maxEnabledRow = 0;
      int startIndex;
      for (startIndex = 0; startIndex < this.guiEntrySettings.entrySettings.length; ++startIndex) {
         EntrySetting setting = this.guiEntrySettings.entrySettings[startIndex];
         if (setting.visible) {
            maxEnabledRow = startIndex;
         }
      }

      maxEnabledRow -= 3;
      if (maxEnabledRow < 0) {
         maxEnabledRow = 0;
      }

      this.Slider.maxValue = (double)maxEnabledRow / 4.0;
      this.Slider.minValue = 0.0;
      if (hasPageChanged) {
         this.Slider.sliderValue = 0.0;
         this.disableAllTextField();
      }

      startIndex = (int)(this.Slider.getValue() * 4.0);
      int k = 0;

      for (int j = startIndex; k < this.buttonAry.length; ++j) {
         EntrySetting setting = this.guiEntrySettings.entrySettings[j];
         int i;
         if (this.showTextFieldFlgAry[k]) {
            for (i = 0; i < this.buttonAry[k].length; ++i) {
               this.buttonAry[k][i].visible = false;
               this.buttonAry[k][i].active = false;
            }

            this.buttonAry[k][1].visible = setting.visible;
            this.buttonAry[k][1].active = setting.visible;
            this.textFiledAry[k].setVisible(setting.visible);
            this.textFiledAry[k].setEditable(setting.enabled);
         } else {
            for (i = 0; i < this.buttonAry[k].length; ++i) {
               this.buttonAry[k][i].visible = setting.visible;
               this.buttonAry[k][i].active = setting.enabled;
            }

            this.textFiledAry[k].setVisible(false);
            this.textFiledAry[k].setEditable(false);
         }

         if (hasPageChanged) {
            setting.buttonStatesIndex = setting.buttonStatesInitIndex;
         }

         if (this.showTextFieldFlgAry[k]) {
            this.buttonAry[k][1].setMessage(Component.literal("Set"));
         } else {
            if (setting.buttonStatesIndex >= setting.buttonStates.length) {
               setting.buttonStatesIndex = 0;
            }

            this.buttonAry[k][0].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].left));
            this.buttonAry[k][1].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].right));
            if (setting.buttonStates[setting.buttonStatesIndex].center != null) {
               this.buttonAry[k][2].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].center));
            } else {
               this.buttonAry[k][2].visible = false;
               this.buttonAry[k][2].active = false;
               this.buttonAry[k][3].visible = false;
               this.buttonAry[k][3].active = false;
            }
         }

         ++k;
      }
   }

   private static final String translateCommaSeparated(String bindName) {
      if (bindName == null) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         String[] strAry = bindName.split(",");

         for (String str : strAry) {
            builder.append(CompatHelper.formatLang(str));
         }

         return builder.toString();
      }
   }

   protected void actionPerformed(IJBGuiWidget widget) {
      if (!this.contrilInitLayerAddUnlock(widget)) {
         this.initlayerAddUnLock = false;
         if (widget.getId() == 99) {
            this.disableAllTextField();
         } else {
            int startIndex = (int)(this.Slider.getValue() * 4.0);
            if (startIndex >= 0 && startIndex <= 2) {
               int amount = 0;
               int sendId = widget.getId();
               switch (widget.getId()) {
                  case 10:
                     if (this.showTextFieldFlgAry[0]) {
                        sendId = 81;
                        amount = this.getValueFromTextField(0);
                     } else {
                        amount = this.getButtonRightValue(startIndex + 0);
                     }
                     break;
                  case 14:
                     amount = this.getButtonLeftValue(startIndex + 0);
                     break;
                  case 16:
                     this.setNextButtonStateIndex(this.guiEntrySettings.entrySettings[startIndex + 0]);
                     return;
                  case 18:
                     this.enableTargetTextField(startIndex, 0);
                     return;
                  case 20:
                     if (this.showTextFieldFlgAry[1]) {
                        sendId = 82;
                        amount = this.getValueFromTextField(1);
                     } else {
                        amount = this.getButtonRightValue(startIndex + 1);
                     }
                     break;
                  case 24:
                     amount = this.getButtonLeftValue(startIndex + 1);
                     break;
                  case 26:
                     this.setNextButtonStateIndex(this.guiEntrySettings.entrySettings[startIndex + 1]);
                     return;
                  case 28:
                     this.enableTargetTextField(startIndex, 1);
                     return;
                  case 30:
                     if (this.showTextFieldFlgAry[2]) {
                        sendId = 83;
                        amount = this.getValueFromTextField(2);
                     } else {
                        amount = this.getButtonRightValue(startIndex + 2);
                     }
                     break;
                  case 34:
                     amount = this.getButtonLeftValue(startIndex + 2);
                     break;
                  case 36:
                     this.setNextButtonStateIndex(this.guiEntrySettings.entrySettings[startIndex + 2]);
                     return;
                  case 38:
                     this.enableTargetTextField(startIndex, 2);
                     return;
                  case 40:
                     if (this.showTextFieldFlgAry[3]) {
                        sendId = 84;
                        amount = this.getValueFromTextField(3);
                     } else {
                        amount = this.getButtonRightValue(startIndex + 3);
                     }
                     break;
                  case 44:
                     amount = this.getButtonLeftValue(startIndex + 3);
                     break;
                  case 46:
                     this.setNextButtonStateIndex(this.guiEntrySettings.entrySettings[startIndex + 3]);
                     return;
                  case 48:
                     this.enableTargetTextField(startIndex, 3);
                     return;
                  case 97:
                     this.ButtonPasteInitVal.active = true;
               }

               this.sendAction(startIndex, amount, sendId);
            }
         }
      }
   }

   private boolean contrilInitLayerAddUnlock(IJBGuiWidget widget) {
      if (this.te.guiPage == 0 && widget.getId() == 4) {
         if (this.te.initValuesLayer + 1 >= this.te.initValues.length && !this.initlayerAddUnLock) {
            this.initlayerAddUnLock = true;
            return true;
         }

         this.te.setInitValuesLayerNext(this.player);
      }

      if (this.te.guiPage == 0 && this.te.initValuesLayer + 1 >= this.te.initValues.length && widget.getId() == 2 && this.initlayerAddUnLock) {
         this.initlayerAddUnLock = false;
         return true;
      } else {
         return false;
      }
   }

   private void actionPerformedMouseRightClick(GuiButtonJB widget) {
      this.initlayerAddUnLock = false;
      int startIndex = (int)(this.Slider.getValue() * 4.0);
      if (startIndex >= 0 && startIndex <= 2) {
         int amount = 0;
         int sendId = widget.getId();
         switch (widget.getId()) {
            case 10:
               amount = SEND_LARGE_AMOUNT;
            case 11:
            case 12:
            case 13:
            case 15:
            case 17:
            case 19:
            case 21:
            case 22:
            case 23:
            case 25:
            case 27:
            case 29:
            case 31:
            case 32:
            case 33:
            case 35:
            case 37:
            case 39:
            case 41:
            case 42:
            case 43:
            case 45:
            case 47:
            default:
               break;
            case 14:
               amount = -SEND_LARGE_AMOUNT;
               break;
            case 16:
            case 18:
               amount = 0;
               sendId = 81;
               break;
            case 20:
               amount = SEND_LARGE_AMOUNT;
               break;
            case 24:
               amount = -SEND_LARGE_AMOUNT;
               break;
            case 26:
            case 28:
               amount = 0;
               sendId = 82;
               break;
            case 30:
               amount = SEND_LARGE_AMOUNT;
               break;
            case 34:
               amount = -SEND_LARGE_AMOUNT;
               break;
            case 36:
            case 38:
               amount = 0;
               sendId = 83;
               break;
            case 40:
               amount = SEND_LARGE_AMOUNT;
               break;
            case 44:
               amount = -SEND_LARGE_AMOUNT;
               break;
            case 46:
            case 48:
               amount = 0;
               sendId = 84;
         }

         this.sendAction(startIndex, amount, sendId);
         widget.playDownSound(Minecraft.getInstance().getSoundManager());
      }
   }

   private void sendAction(int startIndex, int amount, int sendId) {
      if (this.player != null) {
         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(sendId, startIndex, amount, (String)null));
      }
   }

   private void setValueFormCurrrentValueAry(int startIndex, int idx) {
      if (idx >= 0 && idx < this.textFiledAry.length) {
         int[] values;
         if (this.te.guiPage == 0) {
            values = this.te.initValues[this.te.initValuesLayer];
         } else {
            values = this.te.params;
         }

         int valueIdx = startIndex + idx;
         if (valueIdx >= 0 && valueIdx < values.length) {
            this.textFiledAry[idx].setValue(String.valueOf(values[valueIdx]));
         } else {
            this.textFiledAry[idx].setValue("");
         }
      }
   }

   private int getButtonLeftValue(int rowNum) {
      int index = this.guiEntrySettings.entrySettings[rowNum].buttonStatesIndex;
      return (int)this.guiEntrySettings.entrySettings[rowNum].buttonStates[index].amountLeft;
   }

   private int getButtonRightValue(int rowNum) {
      int index = this.guiEntrySettings.entrySettings[rowNum].buttonStatesIndex;
      return (int)this.guiEntrySettings.entrySettings[rowNum].buttonStates[index].amountRight;
   }

   private void setNextButtonStateIndex(EntrySetting entrySetting) {
      if (++entrySetting.buttonStatesIndex >= entrySetting.buttonStates.length) {
         entrySetting.buttonStatesIndex = 0;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean result = super.mouseScrolled(mouseX, mouseY, delta);
      this.handleMouseWheelInput(mouseX, mouseY, delta);
      return result;
   }

   private void handleMouseWheelInput(double mouseX, double mouseY, double delta) {
      int x = (int)mouseX;
      int y = (int)mouseY;
      int dWheel = delta > 0.0 ? 120 : (delta < 0.0 ? -120 : 0);
      if (dWheel != 0) {
         this.disableAllTextField();
      }

      if (this.Slider.visible && this.Slider.maxValue != 0.0) {
         int Lft = (this.width - this.imageWidth) / 2;
         int Top = (this.height - this.imageHeight) / 2;
         if (x >= Lft + 7 && x < Lft + this.imageWidth - 7 && y >= Top + 70 && y < Top + this.imageHeight - 45) {
            this.Slider.handleSliderWheelScroll(dWheel, 4.0);
         }
      }
   }

   private void enableTargetTextField(int startIndex, int idx) {
      this.disableAllTextField();
      this.setValueFormCurrrentValueAry(startIndex, idx);
      this.showTextFieldFlgAry[idx] = true;
      this.textFiledAry[idx].setFocusedIgnoreVisible(true);
      this.bypathTextFieldClick[idx] = true;
   }

   public boolean isButtonMousePressed(GuiButtonJB button, int mouseX, int mouseY) {
      return button.active && button.visible && mouseX >= button.getX() && mouseY >= button.getY() && mouseX < button.getX() + button.getWidth() && mouseY < button.getY() + button.getHeight();
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int eventButton) {
      int x = (int)mouseX;
      int y = (int)mouseY;
      int i;
      if (eventButton == 1) {
         for (i = 0; i < this.buttonAry.length; ++i) {
            if (!this.showTextFieldFlgAry[i]) {
               if (this.isButtonMousePressed(this.buttonAry[i][0], x, y)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][0]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][1], x, y)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][1]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][2], x, y)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][2]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][3], x, y)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][3]);
               }
            }
         }
      }

      boolean result = super.mouseClicked(mouseX, mouseY, eventButton);

      for (i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         if (!this.bypathTextFieldClick[i]) {
            this.textFiledAry[i].mouseClicked(mouseX, mouseY, eventButton);
            if (!this.textFiledAry[i].isFocused()) {
               this.showTextFieldFlgAry[i] = false;
            }
         } else {
            String str = this.textFiledAry[i].getValue();
            if (!JBGuiStringHelper.isEmptyOrNull(str)) {
               this.textFiledAry[i].moveCursorToEnd();
               this.textFiledAry[i].setHighlightPos(str.length());
            }
         }

         this.bypathTextFieldClick[i] = false;
      }

      return result;
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      String[] oldStrs = new String[this.textFiledAry.length];
      boolean hasKeyTyped = false;

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         oldStrs[i] = this.textFiledAry[i].getValue();
         hasKeyTyped |= this.textFiledAry[i].charTyped(typedChar, modifiers);
      }

      boolean result = hasKeyTyped;
      if (!hasKeyTyped) {
         result = super.charTyped(typedChar, modifiers);
      }

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         JBGuiStringHelper.fixToValidNumString(this.textFiledAry[i], oldStrs[i]);
      }

      return result;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      String[] oldStrs = new String[this.textFiledAry.length];
      boolean hasKeyTyped = false;

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         oldStrs[i] = this.textFiledAry[i].getValue();
         hasKeyTyped |= this.textFiledAry[i].keyPressed(keyCode, scanCode, modifiers);
         if (this.showTextFieldFlgAry[i] && keyCode == KEY_ENTER) {
            hasKeyTyped = true;
            this.sendKeyEnterNumberCommit(i);
         }
      }

      boolean result = hasKeyTyped;
      if (!hasKeyTyped) {
         result = super.keyPressed(keyCode, scanCode, modifiers);
      }

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         JBGuiStringHelper.fixToValidNumString(this.textFiledAry[i], oldStrs[i]);
      }

      return result;
   }

   private void sendKeyEnterNumberCommit(int idx) {
      int amount = this.getValueFromTextField(idx);
      int startIndex = (int)(this.Slider.getValue() * 4.0);
      if (startIndex >= 0 && startIndex <= 2) {
         if (idx < ROWIDX_TO_SENDID_CONV.length) {
            this.sendAction(startIndex, amount, ROWIDX_TO_SENDID_CONV[idx]);
            this.disableAllTextField();
         }
      }
   }

   private void disableAllTextField() {
      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         this.showTextFieldFlgAry[i] = false;
      }
   }

   private int getValueFromTextField(int i) {
      return i >= 0 && i <= this.textFiledAry.length ? JBGuiStringHelper.fixToValidNumString(this.textFiledAry[i], "0") : 0;
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang(this.te.getTypeName()), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.setmodel"), 29, 26, 4210752, false);
      IJBMotionBinderItem.IJBMotionHandler actHndl = this.te.getBindOrFixBindStatus();
      guiGraphics.drawString(this.font, CompatHelper.formatLang(this.te.getMotionBinderName()), 70, 26, 4210752, false);
      int rowY = this.imageHeight - 170;
      int[] values;
      String str;
      if (this.te.guiPage == 0) {
         if (!this.ButtonDelInitVal.visible) {
            str = CompatHelper.formatLang("jointGui.info.inittitle");
            guiGraphics.drawString(this.font, str, 10.0F, (float)rowY, 16777113, true);
            int initValuesCount = this.te.initValues.length - 1;
            String modeTitle;
            if (this.te.initValuesLayer == 0 && initValuesCount > 0) {
               modeTitle = "(+" + initValuesCount + ")";
               int modeTitleWidth = this.font.width(modeTitle);
               guiGraphics.drawString(this.font, modeTitle, (float)(106 - modeTitleWidth), (float)rowY, 16777113, true);
            } else if (this.te.initValuesLayer > 0) {
               modeTitle = "(M" + this.te.initValuesLayer + ")";
               int modeTitleWidth = this.font.width(modeTitle);
               guiGraphics.drawString(this.font, modeTitle, (float)(106 - modeTitleWidth), (float)rowY, 16777113, true);
            }
         }

         values = this.te.initValues[this.te.initValuesLayer];
      } else {
         guiGraphics.drawString(this.font, translateCommaSeparated(actHndl.getMotionHandlerName()), 10.0F, (float)rowY, 16777113, true);
         values = this.te.params;
      }

      rowY += 25;
      if (this.guiEntrySettings.text != null) {
         rowY += this.guiEntrySettings.textFromRow * 25;
         int rowFix = -5;
         List<FormattedCharSequence> list = this.font.split(Component.literal(this.guiEntrySettings.localized), 150);
         int lineCount = list.size();

         for (int i = 0; i < lineCount && i < 8; ++i) {
            guiGraphics.drawString(this.font, list.get(i), 10, rowY + rowFix, 15658734, false);
            if ((i & 1) == 1) {
               rowY += 25;
               rowFix = -5;
            } else {
               rowFix = 6;
            }
         }
      } else {
         int startIndex = (int)(this.Slider.getValue() * 4.0);
         int rowIndex = 0;

         for (int j = startIndex; rowIndex < this.buttonAry.length; ++j) {
            if (this.guiEntrySettings.entrySettings[j].visible) {
               if (this.guiEntrySettings.entrySettings[j].title != null) {
                  str = this.guiEntrySettings.entrySettings[j].title.format((long)values[j]);
               } else {
                  str = CompatHelper.formatLang(this.guiEntrySettings.entrySettings[j].titleStr);
               }

               guiGraphics.drawString(this.font, str, 10, rowY - 5, 15658734, false);
               if (this.guiEntrySettings.entrySettings[j].format != null) {
                  str = this.guiEntrySettings.entrySettings[j].format.format(new Object[]{values[j]});
               } else {
                  str = CompatHelper.formatLang(this.guiEntrySettings.entrySettings[j].formatStr, values[j]);
               }

               int righten = 93 - this.font.width(str);
               guiGraphics.drawString(this.font, str, righten, rowY + 5, 15658734, false);
            }

            rowY += 25;
            ++rowIndex;
         }
      }

      if (this.initlayerAddUnLock) {
         this.hoverTipWorkList.clear();
         String confirmTip = CompatHelper.formatLang("jointGui.info.addLayerConfTip");
         this.hoverTipWorkList.add(confirmTip);
         int tipX = this.ButtonBindNumNext.getX() + this.ButtonBindNumNext.getWidth() / 2 - this.font.width(confirmTip) / 2 - 12;
         int tipY = this.ButtonBindNumNext.getY() - 5;
         List<Component> tips = new ArrayList<>();

         for (String tip : this.hoverTipWorkList) {
            tips.add(Component.literal(tip));
         }

         guiGraphics.renderComponentTooltip(this.font, tips, tipX - this.leftPos, tipY - this.topPos);
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture plus the binder-name
    * frame.  {@code bindTexture(resource)} + {@code drawTexturedModalRect} become
    * {@code blit(resource, ...)}.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resource, k, l, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.blit(resource, k + 106, l + 41, 176, 20, 63, 28);
   }

   /**
    * 1.12.2 {@code drawScreen} only added the manual {@code drawTextBox()} of every visible
    * {@code GuiTextFieldEx} after the standard container draw (the fields are plain widgets here,
    * not {@code addRenderableWidget()} entries, so they are not drawn automatically).
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         if (this.showTextFieldFlgAry[i]) {
            this.textFiledAry[i].renderWidget(guiGraphics, mouseX, mouseY, partialTick);
         }
      }
   }
}
