package jp.mc.ancientred.jointblock.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.assemble.AddonBonus;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.GuiVerticalSlider;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointCoreAddons}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code bindTexture}+{@code drawTexturedModalRect} ->
 * {@code GuiGraphics.blit(resMainbg, ...)}; {@code drawRect} -> {@code GuiGraphics.fill}.
 *
 * <p>{@code super.renderLabels} is intentionally not called: the 1.12 base
 * {@code GuiContainer.drawGuiContainerForegroundLayer} was empty, whereas the 1.20.1 base also draws
 * the ({@code Component.empty()}) screen title and the player-inventory label, which this 1.12 GUI
 * never had.
 */
@OnlyIn(Dist.CLIENT)
public class GuiJointCoreAddons extends GuiContainerWithTip<ContainerJointCoreAddons> {
   private static final ResourceLocation resMainbg = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   private static final String STRMINUS = "-";
   private static final String STRPLUS = "+";
   private static DecimalFormat dFormat = new DecimalFormat("#.###");
   private static final int MAXRESULTROWCOUNT = 8;
   private static final int RESULT_TOP_Y = 55;
   private static final int ROW_HIGHT = 12;
   public static final int ButtonAddonSearchID = 10;
   public static final int ButtonAddonListID = 12;
   public static final int ButtonAddonSummaryID = 14;
   public static final int ButtonCloseResultID = 16;
   public static final int SliderTestResultID = 99;
   private GuiTabSetJoint tabSetJoint;
   private GuiButtonJB ButtonAddonSearch;
   private GuiButtonJB ButtonAddonList;
   private GuiButtonJB ButtonAddonSummary;
   private GuiButtonJB ButtonCloseResult;
   private GuiVerticalSlider SliderTestResult;
   TileEntityJointCore te;
   private Player player;
   private boolean showSummary;

   public GuiJointCoreAddons(ContainerJointCoreAddons menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.teCore;
      this.player = playerInv.player;
      this.imageHeight = 222;
   }

   @Override
   public void removed() {
      super.removed();
   }

   @Override
   protected void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      List<AbstractWidget> tabButtons = new ArrayList<>();
      this.tabSetJoint = new GuiTabSetJoint(tabButtons, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.core, 4, false);
      tabButtons.forEach(this::addRenderableWidget);
      int buttonWidth = this.imageWidth - 18;
      int buttonHeight = 20;
      this.ButtonAddonSearch = new GuiButtonJB(ButtonAddonSearchID, xLeft + 9, yTop + 55 + 7, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.searchaddon"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonAddonSearch);
      int closeBtnWidth = 20;
      int btnPad = 2;
      buttonWidth = (this.imageWidth - 18 - closeBtnWidth - btnPad * 2) / 2;
      this.ButtonAddonList = new GuiButtonJB(ButtonAddonListID, xLeft + 9, yTop + 55 + 7, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.addonlist"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonAddonList);
      this.ButtonAddonSummary = new GuiButtonJB(ButtonAddonSummaryID, this.ButtonAddonList.getX() + buttonWidth + btnPad, yTop + 55 + 7, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.addonsummary"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonAddonSummary);
      this.ButtonCloseResult = new GuiButtonJB(ButtonCloseResultID, this.ButtonAddonSummary.getX() + buttonWidth + btnPad, yTop + 55 + 7, closeBtnWidth, buttonHeight, "X", this::actionPerformed);
      this.addRenderableWidget(this.ButtonCloseResult);
      this.SliderTestResult = new GuiVerticalSlider(SliderTestResultID, xLeft + 158, yTop + 55 + 30, 10, 104, 0.0, 0.0, 0.0);
      this.addRenderableWidget(this.SliderTestResult);
      this.updateInputStates();
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      if (this.te.addonBonus == null) {
         this.SliderTestResult.visible = false;
         this.ButtonAddonSearch.visible = true;
         this.ButtonAddonList.visible = false;
         this.ButtonAddonSummary.visible = false;
         this.ButtonCloseResult.visible = false;
      } else {
         this.setUpAddonSearchResultStr();
         ArrayList<Object> list = this.showSummary ? this.te.addonBonus.sResultStrSummary : this.te.addonBonus.sResultStrList;
         if (list != null) {
            int maxRowIndex = list.size() - 1;
            maxRowIndex -= 7;
            if (maxRowIndex < 0) {
               maxRowIndex = 0;
            }

            this.SliderTestResult.maxValue = (double)maxRowIndex / 8.0;
            this.SliderTestResult.minValue = 0.0;
         } else {
            this.SliderTestResult.maxValue = 0.0;
            this.SliderTestResult.minValue = 0.0;
         }

         this.SliderTestResult.visible = true;
         this.ButtonAddonSearch.visible = false;
         this.ButtonAddonList.visible = true;
         this.ButtonAddonSummary.visible = true;
         this.ButtonCloseResult.visible = true;
      }

   }

   private void setUpAddonSearchResultStr() {
      AddonBonus ab = this.te.addonBonus;
      if (ab.sResultStrList == null && ab.addonSearchResultAry != null && ab.addonSearchResultAry.length >= 2 && ab.addonSearchResultAry.length % 2 == 0) {
         this.showSummary = false;
         ab.sResultStrSummary = new ArrayList<>();
         if (ab.maxHealthMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.maxHealth, ab.maxHealthMult));
         }

         if (ab.maxEnergyMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.maxEnergy, ab.maxEnergyMult));
         }

         if (ab.forwardSpeedMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.forwardSpeed, ab.forwardSpeedMult));
         }

         if (ab.strafingSpeedMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.strafingSpeed, ab.strafingSpeedMult));
         }

         if (ab.energyUseRateMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.energyUseRate, ab.energyUseRateMult));
         }

         if (ab.jumpPowerMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.jumpPower, ab.jumpPowerMult));
         }

         if (ab.thrusterPowerMult != 0.0F) {
            ab.sResultStrSummary.add(this.createSummaryPercentage(IJBAddon.BonusType.thrusterPower, ab.thrusterPowerMult));
         }

         if (ab.stepHeightModify != 0) {
            ab.sResultStrSummary.add(this.createSummaryInteger(IJBAddon.BonusType.stepHeight, ab.stepHeightModify));
         }

         if (ab.energyHealIntervalModify != 0) {
            ab.sResultStrSummary.add(this.createSummaryTick(IJBAddon.BonusType.energyHealInterval, ab.energyHealIntervalModify));
         }

         AddonSearchRes subTypeSumary = this.createSubTypeRes(ab.subTypeUnitId, 0);
         if (subTypeSumary != null) {
            ab.sResultStrSummary.add(subTypeSumary);
         }

         ab.sResultStrList = new ArrayList<>();
         int valHeader = ab.addonSearchResultAry[0];
         ab.foundCount = (short)(valHeader >> 16 & '\uffff');
         ab.arrayLimit = (short)(valHeader & '\uffff');
         valHeader = ab.addonSearchResultAry[1];
         ab.ignoredCount = (short)(valHeader & '\uffff');
         ab.strResultHeader = ChatFormatting.ITALIC + CompatHelper.formatLang("jointGui.testresult.absearchres", ab.foundCount, ab.ignoredCount);
         IJBAddon.BonusType[] btValues = IJBAddon.BonusType.values();

         for(int i = 2; i < ab.addonSearchResultAry.length; i += 2) {
            int itemId = ab.addonSearchResultAry[i];
            int itemMeta = ab.addonSearchResultAry[i + 1];
            Item item = Item.byId(itemId);
            if (item != null && item instanceof IJBAddon.IJBAddonHolder) {
               IJBAddon addon = ((IJBAddon.IJBAddonHolder)item).getIJBAddon(itemMeta);
               if (addon != null) {
                  ItemStack stackWork = new ItemStack(item, 1);
                  stackWork.setDamageValue(itemMeta);
                  String str = stackWork.getHoverName().getString();
                  ab.sResultStrList.add(new AddonSearchRes(str, 16777215, 0, 0));

                  for(int b = 0; b < btValues.length; ++b) {
                     IJBAddon.BonusType bt = btValues[b];
                     float val = addon.getBonusValue(bt);
                     if (val != 0.0F) {
                        if (bt == IJBAddon.BonusType.subTypeUnitId) {
                           AddonSearchRes subTypeRes = this.createSubTypeRes((int)val, 10);
                           if (subTypeRes != null) {
                              ab.sResultStrList.add(subTypeRes);
                           }
                        } else {
                           str = this.formatBonusType(bt, val);
                           if (str != null) {
                              ab.sResultStrList.add(new AddonSearchRes(str, this.valueColor(bt, val), 0, 10));
                           }
                        }
                     }
                  }

                  IJBAddon.IJBAddonLimitKey limitKey = addon.getLimitKey();
                  if (limitKey != null) {
                     String limitKeyName = CompatHelper.formatLang(limitKey.getApplyKeyName());
                     int limitKeyCost = addon.getCost();
                     int limitKeyMax = limitKey.getApplyCostLimit();
                     String limitKeyStr = CompatHelper.formatLang("jointGui.testresult.ablimitkeyformat", limitKeyName, limitKeyCost, limitKeyMax);
                     ab.sResultStrList.add(new AddonSearchRes(limitKeyStr, 13421772, 1, 5));
                  }
               }
            }
         }
      }

   }

   private AddonSearchRes createSubTypeRes(int unitId, int indent) {
      AbstractUnitBase unitBase = AbstractUnitBase.getUnitBaseById(unitId);
      if (unitBase != null) {
         String str = CompatHelper.formatLang("jointGui.testresult.abaddsubtype") + CompatHelper.formatLang(unitBase.getUnitTypeName());
         return new AddonSearchRes(str, 16776994, 0, indent);
      } else {
         return null;
      }
   }

   private AddonSearchRes createSummaryPercentage(IJBAddon.BonusType bt, float value) {
      return new AddonSearchRes(this.formatPercentage(CompatHelper.formatLang(bt.nameKey), value), this.valueColor(bt, value), 0, 0);
   }

   private AddonSearchRes createSummaryInteger(IJBAddon.BonusType bt, int value) {
      return new AddonSearchRes(this.formatInteger(CompatHelper.formatLang(bt.nameKey), (float)value), this.valueColor(bt, (float)value), 0, 0);
   }

   private AddonSearchRes createSummaryTick(IJBAddon.BonusType bt, int value) {
      return new AddonSearchRes(this.formatTick(CompatHelper.formatLang(bt.nameKey), (float)value), this.valueColor(bt, (float)value), 0, 0);
   }

   protected int valueColor(IJBAddon.BonusType bt, float val) {
      switch (bt) {
         case energyHealInterval:
            return val <= 0.0F ? 2293538 : 16720418;
         default:
            return val >= 0.0F ? 2293538 : 16720418;
      }
   }

   private String formatBonusType(IJBAddon.BonusType bt, float value) {
      switch (bt) {
         case energyHealInterval:
            return this.formatTick(CompatHelper.formatLang(bt.nameKey), value);
         case maxEnergy:
         case maxHealth:
         case forwardSpeed:
         case strafingSpeed:
         case jumpPower:
         case thrusterPower:
         case energyUseRate:
            return this.formatPercentage(CompatHelper.formatLang(bt.nameKey), value);
         case stepHeight:
            return this.formatInteger(CompatHelper.formatLang(bt.nameKey), value);
         case subTypeUnitId:
         default:
            return null;
      }
   }

   private String formatPercentage(String title, float value) {
      return title + (value >= 0.0F ? STRPLUS : STRMINUS) + dFormat.format((double)(Math.abs(value) * 100.0F)) + "%";
   }

   private String formatInteger(String title, float value) {
      return title + (value >= 0.0F ? STRPLUS : STRMINUS) + dFormat.format((double)Math.abs(value));
   }

   private String formatTick(String title, float value) {
      return title + (value >= 0.0F ? STRPLUS : STRMINUS) + dFormat.format((double)Math.abs(value)) + "tick";
   }

   @Override
   protected void actionPerformed(IJBGuiWidget widget) {
      if (widget.getId() != SliderTestResultID) {
         if (widget.getId() == ButtonAddonSummaryID) {
            this.showSummary = true;
            this.SliderTestResult.setValue(0.0);
         } else if (widget.getId() == ButtonAddonListID) {
            this.showSummary = false;
            this.SliderTestResult.setValue(0.0);
         } else {
            if (this.player != null) {
               JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(widget.getId(), 0, 0, null));
            }

         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean handled = super.mouseScrolled(mouseX, mouseY, delta);
      this.handleMouseWheelInput(mouseX, mouseY, (int)delta);
      return handled;
   }

   private void handleMouseWheelInput(double mouseX, double mouseY, int dWheel) {
      if (this.SliderTestResult.visible && this.SliderTestResult.maxValue != 0.0) {
         int Lft = (this.width - this.imageWidth) / 2;
         int Top = (this.height - this.imageHeight) / 2;
         if (mouseX >= Lft + 7 && mouseX < Lft + this.imageWidth - 7 && mouseY >= Top + RESULT_TOP_Y + 25 && mouseY < Top + RESULT_TOP_Y + 136) {
            this.SliderTestResult.handleSliderWheelScroll(dWheel, 8.0);
         }
      }

   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.jointcoreaddons"), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.addonslot"), 7, 22, 0, false);
      int rowLeft = 10;
      int rowRight = 169 - this.SliderTestResult.getWidth();
      if (this.te.addonBonus != null) {
         AddonBonus ab = this.te.addonBonus;
         ArrayList<Object> list = this.showSummary ? ab.sResultStrSummary : ab.sResultStrList;
         if (list != null) {
            if (this.showSummary) {
               guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.addonsummary"), rowLeft, 85, 3355630, false);
            } else {
               guiGraphics.drawString(this.font, ab.strResultHeader, rowLeft, 85, 3355630, false);
            }

            int maxRowIndex = list.size() - 1;
            maxRowIndex -= 7;
            if (maxRowIndex < 0) {
               maxRowIndex = 0;
            }

            int strIndex = (int)(this.SliderTestResult.getValue() * 8.0);
            if (strIndex > maxRowIndex) {
               strIndex = maxRowIndex;
            }

            if (strIndex < 0) {
               strIndex = 0;
            }

            int rowY = 95;

            for (int i = 0; strIndex < list.size() && i < 8; ++i) {
               AddonSearchRes resObj = (AddonSearchRes)list.get(strIndex);
               if (resObj != null) {
                  if (resObj.pos == AddonSearchRes.POS_LEFT) {
                     guiGraphics.drawString(this.font, resObj.str, rowLeft + resObj.indent, rowY, resObj.col, false);
                  } else if (resObj.pos == AddonSearchRes.POS_RIGHT) {
                     int strw = this.font.width(resObj.str);
                     guiGraphics.drawString(this.font, resObj.str, rowRight - strw - resObj.indent, rowY, resObj.col, false);
                  }
               }

               rowY += 12;
               ++strIndex;
            }
         }
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture, the inner panel filled
    * rect, the result panel frame, the nine addon-slot frames and (no search result yet) the
    * bottom-panel separator + filled rect.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int Lft = (this.width - this.imageWidth) / 2;
      int Top = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resMainbg, Lft, Top, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.fill(Lft + 7, Top + 10, Lft + 169, Top + 196, -3750202);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blit(resMainbg, Lft + 7, Top + RESULT_TOP_Y + 5, 7, 45, 162, 131);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

      for (int j = 0; j < 9; ++j) {
         guiGraphics.blit(resMainbg, Lft + 7 + j * 18, Top + 35, 176, 48, 18, 18);
      }

      if (this.te.addonBonus == null) {
         int border = 86;
         guiGraphics.blit(resMainbg, Lft + 7, Top + border - 1, 7, 175, 162, 1);
         guiGraphics.fill(Lft + 7, Top + border, Lft + 169, Top + 196, -3750202);
         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   static class AddonSearchRes {
      public static final int POS_LEFT = 0;
      public static final int POS_RIGHT = 1;
      public int col;
      public int pos;
      public int indent;
      public String str;

      public AddonSearchRes(String argStr, int argCol, int argPos, int argIndent) {
         this.str = argStr;
         this.col = argCol;
         this.pos = argPos;
         this.indent = argIndent;
      }
   }
}
