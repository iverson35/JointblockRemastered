package jp.mc.ancientred.jbrobot.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jbrobot.gui.widget.GuiSlotLikeButton;
import jp.mc.ancientred.jbrobot.gui.widget.GuiTabSetCatalog;
import jp.mc.ancientred.jbrobot.gui.widget.GuiVerticalSlider;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.JBMerchantType;
import jp.mc.ancientred.jbrobot.proxy.JBRPacketSender;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.GuiContainerWithTip;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiModelCatalog}: the model/blueprint/addon merchant catalog.
 *
 * <p>1.20.1 substitutions (beyond the {@link GuiContainerWithTip} base):
 * <ul>
 *   <li>{@code GuiContainer} -> {@link GuiContainerWithTip} with a {@link ContainerModelCatalog}
 *       menu; the constructor takes the menu/inventory instead of the player.</li>
 *   <li>{@code buttonList.add} -> {@code addRenderableWidget}; 1.12 {@code GuiButton}/
 *       {@code GuiButtonExt} -> {@code GuiButtonJB} with an {@code actionPerformed} callback.</li>
 *   <li>GL11 immediate mode for the 3D preview ({@code glTranslatef/glScalef/glRotatef},
 *       {@code RenderHelper.enableStandardItemLighting}) -> {@link PoseStack} transforms plus the
 *       frame's {@link MultiBufferSource} and the full-bright lightmap; the 1.12 vertex
 *       {@code scale} argument of {@code renderAll} no longer exists (the joint model renderers
 *       apply their own 1/16 convention), so only the pose transforms are kept.</li>
 *   <li>{@code Mouse.getDWheel} -> {@link #mouseScrolled}; {@code ItemRenderer} z-level juggling ->
 *       {@link GuiGraphics#renderItem}/{@link GuiGraphics#renderItemDecorations}.</li>
 *   <li>custom slot tooltips are drawn after {@code super.render} (the 1.12 code drew them from the
 *       foreground layer, which was translated by the GUI origin).</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class GuiModelCatalog extends GuiContainerWithTip<ContainerModelCatalog> {
   private static final ResourceLocation resource = new ResourceLocation("jbrobot:textures/gui/guimodelcatalog.png");
   private static final int PACKAGE_SLOT_NUMS = 5;
   private static final int MODEL_SLOT_ROWS = 5;
   private static final int MODEL_SLOT_COLS = 2;
   public static final int ButtonPackagePrevID = 2;
   public static final int ButtonPackageNextID = 4;
   public static final int ButtonBuyModelID = 6;
   public static final int ButtonBackColorID = 8;
   public static final int ButtonModelDescID = 10;
   public static final int ButtonAddonID = 12;
   public static final int SliderID = 99;
   public static final int ButtonSlotPackageStartID = 100;
   public static final int ButtonSlotModelStartID = 200;
   private GuiButtonJB ButtonPackagePrev;
   private GuiButtonJB ButtonPackageNext;
   private GuiButtonJB ButtonBuyModel;
   private GuiButtonJB ButtonBackColor;
   private GuiButtonJB ButtonModelDesc;
   private GuiButtonJB ButtonAddon;
   private GuiVerticalSlider Slider;
   private final GuiSlotLikeButton[] slotButtonsPackages = new GuiSlotLikeButton[5];
   private final GuiSlotLikeButton[] slotButtonsModels = new GuiSlotLikeButton[10];
   private GuiTabSetCatalog tabSetCatalog;
   private final List<Component> tipWorkList = new ArrayList<>();
   public ArrayList<IJBRMerchantPackage> packageList = new ArrayList<>();
   public static int packageListPage;
   public static int selectedPackageIndex;
   public static int selectedEntryIndex;
   public static double selectedSliderValue;
   public static JBMerchantType typeNow;
   private static int rotateTick;
   private boolean totallyDisabled = false;
   private boolean showDesc = false;
   private boolean showAddon = false;
   private boolean backWhite = false;
   private boolean triedRestoreSliderValues = false;
   private static final String STRMINUS = "-";
   private static final String STRPLUS = "+";
   private static final DecimalFormat dFormat;

   public GuiModelCatalog(ContainerModelCatalog menu, Inventory playerInv) {
      super(menu, playerInv);
      this.imageHeight = 222;

      for (int i = 0; i < JBRobotMODContainer.syncedExPackageListClient.size(); ++i) {
         this.packageList.add(JBRobotMODContainer.syncedExPackageListClient.get(i));
      }

      if (this.packageList.size() == 0) {
         this.totallyDisabled = true;
      }

      if (packageListPage > this.getMaxPageIndex()) {
         packageListPage = this.getMaxPageIndex();
      }

      if (selectedPackageIndex >= 0 && selectedPackageIndex < this.packageList.size()) {
         IJBRMerchantPackage pkg = this.packageList.get(selectedPackageIndex);
         if (pkg == null) {
            selectedPackageIndex = 0;
            selectedEntryIndex = 0;
         } else if (selectedEntryIndex < 0 || selectedEntryIndex >= pkg.getEntryNums(typeNow)) {
            selectedEntryIndex = 0;
         }
      } else {
         selectedPackageIndex = 0;
         selectedEntryIndex = 0;
      }
   }

   @Override
   public void removed() {
      selectedSliderValue = this.Slider.getValue();
      super.removed();
   }

   public int getMaxPageIndex() {
      return (this.packageList.size() - 1) / 5;
   }

   @Override
   protected void init() {
      super.init();
      int xLeft = this.leftPos;
      int yTop = this.topPos;
      int buttonHeight = 20;
      List<AbstractWidget> tabButtons = new ArrayList<>();
      this.tabSetCatalog = new GuiTabSetCatalog(tabButtons, this::actionPerformed, xLeft - 22, yTop, 0);
      tabButtons.forEach(this::addRenderableWidget);
      int buttonWidth = 30;
      this.addRenderableWidget(this.ButtonPackagePrev = new GuiButtonJB(ButtonPackagePrevID, xLeft + 10, yTop + 22, buttonWidth, buttonHeight, "<<", this::actionPerformed));
      this.addRenderableWidget(this.ButtonPackageNext = new GuiButtonJB(ButtonPackageNextID, xLeft + 136, yTop + 22, buttonWidth, buttonHeight, ">>", this::actionPerformed));
      this.addRenderableWidget(this.ButtonBuyModel = new GuiButtonJB(ButtonBuyModelID, xLeft + 29, yTop + 163, 30, buttonHeight, "<<BUY", this::actionPerformed));
      this.addRenderableWidget(this.ButtonBackColor = new GuiButtonJB(ButtonBackColorID, xLeft + 136, yTop + 117, 32, 9, "col", this::actionPerformed));
      this.addRenderableWidget(this.ButtonModelDesc = new GuiButtonJB(ButtonModelDescID, xLeft + 136, yTop + 176, 32, 9, "desc", this::actionPerformed));
      this.addRenderableWidget(this.ButtonAddon = new GuiButtonJB(ButtonAddonID, xLeft + 136 - 36 - 2, yTop + 176, 36, 9, "addon", this::actionPerformed));
      this.addRenderableWidget(this.Slider = new GuiVerticalSlider(SliderID, xLeft + 48, yTop + 62, 10, 91, 0.0, 0.0, 0.0));
      int packageSlotLeft = xLeft + 45 - 1;
      int packageSlotTop = yTop + 24;
      int count = 0;

      for (int i = 0; i < PACKAGE_SLOT_NUMS; ++i) {
         this.addRenderableWidget(this.slotButtonsPackages[count] = new GuiSlotLikeButton(ButtonSlotPackageStartID + count, packageSlotLeft + i * 18, packageSlotTop));
         ++count;
      }

      int modelSlotLeft = xLeft + 11;
      int modelSlotTop = yTop + 64;
      count = 0;

      for (int r = 0; r < MODEL_SLOT_ROWS; ++r) {
         for (int c = 0; c < MODEL_SLOT_COLS; ++c) {
            this.addRenderableWidget(this.slotButtonsModels[count] = new GuiSlotLikeButton(ButtonSlotModelStartID + count, modelSlotLeft + c * 18, modelSlotTop + r * 18));
            ++count;
         }
      }

      if (!this.totallyDisabled) {
         this.setupSlotButtonStates();
      }
   }

   public ContainerModelCatalog getContainer() {
      return this.menu;
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (!this.totallyDisabled) {
         this.setupSlotButtonStates();
      }
   }

   private void setupSlotButtonStates() {
      ArrayList<IJBRMerchantPackage> merchantPackageList = this.packageList;
      if (merchantPackageList != null && merchantPackageList.size() != 0) {
         if (typeNow == JBMerchantType.model) {
            this.tabSetCatalog.setActiveIndex(0);
         } else if (typeNow == JBMerchantType.blueprint) {
            this.tabSetCatalog.setActiveIndex(1);
         } else {
            this.tabSetCatalog.setActiveIndex(2);
         }

         if (!this.showDesc && !this.showAddon) {
            this.ButtonModelDesc.setMessage(Component.literal("desc"));
         } else {
            this.ButtonModelDesc.setMessage(Component.literal("info"));
         }

         this.ButtonBackColor.visible = !this.showAddon && !this.showDesc;
         this.ButtonAddon.visible = !this.showAddon;
         if (selectedPackageIndex < 0 || selectedPackageIndex >= merchantPackageList.size()) {
            selectedPackageIndex = 0;
         }

         IJBRMerchantPackage selectedMPackage = merchantPackageList.get(selectedPackageIndex);
         if (selectedMPackage == null) {
            selectedPackageIndex = 0;
            selectedMPackage = merchantPackageList.get(0);
         }

         this.ButtonPackagePrev.active = packageListPage - 1 >= 0;
         this.ButtonPackageNext.active = packageListPage + 1 <= this.getMaxPageIndex();
         this.ButtonBuyModel.active = true;
         IJBRMerchantEntry selectedEntry = this.getSelectedEntry();
         if (selectedEntry != null) {
            ContainerModelCatalog ct = this.getContainer();
            ItemStack costStack = selectedEntry.getMerchantCost();
            ItemStack resultStack = selectedEntry.getMerchantResult(this.getContainer().entityplayer.level());
            if (CompatHelper.isStackEmptyOrNull(costStack) || CompatHelper.isStackEmptyOrNull(resultStack) || !ct.checkPlayerHasMerchantCost(costStack) || !ct.isResultSlotReadyForBuy(resultStack)) {
               this.ButtonBuyModel.active = false;
            }
         } else {
            this.ButtonBuyModel.active = false;
         }

         int packageShowStartIndex = packageListPage * 5;
         int count = 0;

         int entryTotalNums;
         for (entryTotalNums = 0; entryTotalNums < 5; ++entryTotalNums) {
            this.slotButtonsPackages[count].packageEntry = null;
            this.slotButtonsPackages[count].isSelected = false;
            int maxRowIndex = packageShowStartIndex + entryTotalNums;
            if (maxRowIndex < merchantPackageList.size()) {
               this.slotButtonsPackages[count].packageEntry = merchantPackageList.get(maxRowIndex);
               this.slotButtonsPackages[count].index = maxRowIndex;
               if (this.slotButtonsPackages[count].packageEntry == selectedMPackage) {
                  this.slotButtonsPackages[count].isSelected = true;
               }
            }

            ++count;
         }

         entryTotalNums = selectedMPackage.getEntryNums(typeNow);
         int maxRowIndex = (entryTotalNums - 1) / 2;
         maxRowIndex -= 4;
         if (maxRowIndex < 0) {
            this.Slider.maxValue = 0.0;
            this.Slider.active = false;
         } else {
            this.Slider.active = true;
            this.Slider.maxValue = (double)maxRowIndex / 5.0;
            this.Slider.minValue = 0.0;
         }

         if (!this.triedRestoreSliderValues) {
            this.triedRestoreSliderValues = true;
            if (selectedSliderValue <= this.Slider.maxValue) {
               this.Slider.setValue(selectedSliderValue);
            }
         }

         int modelStartIdx = (int)(this.Slider.getValue() * 5.0) * 2;
         count = 0;

         for (int r = 0; r < 5; ++r) {
            for (int c = 0; c < 2; ++c) {
               this.slotButtonsModels[count].merchantEntry = null;
               this.slotButtonsModels[count].isSelected = false;
               int modelIdx = modelStartIdx + r * 2 + c;
               if (modelIdx < entryTotalNums) {
                  this.slotButtonsModels[count].merchantEntry = selectedMPackage.getMerchantEntry(typeNow, modelIdx);
                  this.slotButtonsModels[count].index = modelIdx;
                  if (this.slotButtonsModels[count].merchantEntry == selectedEntry) {
                     this.slotButtonsModels[count].isSelected = true;
                  }
               }

               ++count;
            }
         }
      } else {
         this.minecraft.player.closeContainer();
      }
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      if (!this.totallyDisabled) {
         this.drawSlotTips(guiGraphics, mouseX, mouseY, this.slotButtonsModels, 10);
         this.drawSlotTips(guiGraphics, mouseX, mouseY, this.slotButtonsPackages, 5);
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.
    *
    * <p>The 1.12 base foreground layer was empty, so unlike other ported screens {@code
    * super.renderLabels} is not called (it would draw the empty screen title and the player-inventory
    * label this GUI never had).
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (typeNow == JBMerchantType.model) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jbrinfo.gui.mcatalogtitle"), 8, 6, 4210752, false);
      } else if (typeNow == JBMerchantType.addon) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jbrinfo.gui.acatalogtitle"), 8, 6, 4210752, false);
      } else {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jbrinfo.gui.bcatalogtitle"), 8, 6, 4210752, false);
      }

      if (!this.totallyDisabled) {
         IJBRMerchantPackage pkg = this.getSelectedPackage();
         if (pkg != null) {
            String str = pkg.getPackageName();
            int strWidth = this.font.width(str);
            guiGraphics.drawString(this.font, str, this.imageWidth / 2 - strWidth / 2, 47, 4210752, false);
         }

         IJBRMerchantEntry selectedEntry = this.getSelectedEntry();
         if (this.showAddon) {
            if (selectedEntry != null) {
               IJBAddon addon = selectedEntry.getAddon();
               if (addon != null) {
                  int addonRowTopY = 67;
                  int row = 0;
                  int rowHeight = 12;
                  IJBAddon.BonusType[] btValues = IJBAddon.BonusType.values();

                  for (int b = 0; b < btValues.length; ++b) {
                     IJBAddon.BonusType bt = btValues[b];
                     float val = addon.getBonusValue(bt);
                     if (val != 0.0F) {
                        if (row >= 8) {
                           String str = CompatHelper.formatLang("jbrinfo.gui.omitted");
                           guiGraphics.drawString(this.font, str, 167 - this.font.width(str), addonRowTopY + (row - 1) * rowHeight, 8947848, false);
                           break;
                        }

                        if (bt == IJBAddon.BonusType.subTypeUnitId) {
                           AbstractUnitBase unitBase = AbstractUnitBase.getUnitBaseById((int)val);
                           if (unitBase != null) {
                              String str = "ST:" + CompatHelper.formatLang(unitBase.getUnitTypeName());
                              guiGraphics.drawString(this.font, str, 67, addonRowTopY + row * rowHeight, 16776994, false);
                              ++row;
                           }
                        } else {
                           String str = this.formatBonusType(bt, val);
                           if (str != null) {
                              guiGraphics.drawString(this.font, str, 67, addonRowTopY + row * rowHeight, this.valueColor(bt, val), false);
                              ++row;
                           }
                        }
                     }
                  }

                  IJBAddon.IJBAddonLimitKey limitKey = addon.getLimitKey();
                  if (limitKey != null) {
                     String limitKeyName = CompatHelper.formatLang(limitKey.getApplyKeyName());
                     int limitKeyCost = addon.getCost();
                     int limitKeyMax = limitKey.getApplyCostLimit();
                     guiGraphics.drawString(this.font, "LK:" + limitKeyName, 67, 163, 13421772, false);
                     guiGraphics.drawString(this.font, "LKCOST:" + limitKeyCost + "/" + limitKeyMax, 67, 175, 13421772, false);
                  }
               }
            }
         } else if (this.showDesc) {
            if (selectedEntry != null) {
               int descRowY = 140;
               String str = CompatHelper.formatLang(selectedEntry.getEntryDesc());
               if (!isStringEmpty(str)) {
                  List<FormattedCharSequence> list = this.font.split(Component.literal(str), 100);
                  int rowHeight = list.size();

                  for (int i = 0; i < rowHeight && i < 8; ++i) {
                     guiGraphics.drawString(this.font, list.get(i), 67, descRowY, 15658734);
                     descRowY += 12;
                  }
               }
            }
         } else if (selectedEntry != null) {
            String str = selectedEntry.getEntryName();
            int strWidth = this.font.width(str);
            guiGraphics.drawString(this.font, str, 115 - strWidth / 2, 135, 15658734, false);
            if (selectedEntry.hasAction()) {
               str = CompatHelper.formatLang(selectedEntry.getActionName());
               strWidth = this.font.width(str);
               guiGraphics.drawString(this.font, str, 138 - strWidth / 2, 162, 15658734, false);
            }
         }
      }
   }

   private String formatBonusType(IJBAddon.BonusType bt, float value) {
      switch (bt) {
         case maxEnergy:
         case maxHealth:
         case forwardSpeed:
         case strafingSpeed:
         case jumpPower:
         case thrusterPower:
         case energyUseRate:
            return this.formatPercentage(CompatHelper.formatLang(bt.nameKeyShort), value);
         case energyHealInterval:
            return this.formatTick(CompatHelper.formatLang(bt.nameKeyShort), value);
         case stepHeight:
            return this.formatInteger(CompatHelper.formatLang(bt.nameKeyShort), value);
         case subTypeUnitId:
         default:
            return null;
      }
   }

   protected int valueColor(IJBAddon.BonusType bt, float val) {
      switch (bt) {
         case energyHealInterval:
            return val <= 0.0F ? 2293538 : 16720418;
         default:
            return val >= 0.0F ? 2293538 : 16720418;
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

   /** 1.12.2 used {@code JBRModelUtils.isStringEmpty}'s null/empty test inline. */
   private static boolean isStringEmpty(String str) {
      return str == null || str.isEmpty();
   }

   private void drawSlotTips(GuiGraphics guiGraphics, int mouseX, int mouseY, GuiSlotLikeButton[] targetList, int count) {
      this.tipWorkList.clear();

      for (int i = 0; i < count; ++i) {
         GuiSlotLikeButton slotBtn = targetList[i];
         boolean hover = mouseX >= slotBtn.getX() && mouseY >= slotBtn.getY() && mouseX < slotBtn.getX() + slotBtn.getWidth() && mouseY < slotBtn.getY() + slotBtn.getHeight();
         if (slotBtn.getHoverState(hover) == 2) {
            if (slotBtn.merchantEntry != null) {
               this.tipWorkList.add(Component.literal(slotBtn.merchantEntry.getEntryName()));
               if (slotBtn.merchantEntry.hasAction()) {
                  this.tipWorkList.add(Component.literal(CompatHelper.formatLang("jbrobot.info.hasaction")));
               }

               if (slotBtn.merchantEntry.getAddon() != null) {
                  this.tipWorkList.add(Component.literal(CompatHelper.formatLang("jbrobot.info.hasaddon")));
               }
            } else if (slotBtn.packageEntry != null) {
               this.tipWorkList.add(Component.literal(slotBtn.packageEntry.getPackageName()));
            }
         }

         if (this.tipWorkList.size() > 0) {
            guiGraphics.renderTooltip(this.font, this.tipWorkList, java.util.Optional.empty(), mouseX, mouseY);
            break;
         }
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the catalog texture, the detail-panel
    * decorations and (when a merchant entry is selected) the rotating 3D preview and the cost item.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.leftPos;
      int l = this.topPos;
      guiGraphics.blit(resource, k, l, 0, 0, this.imageWidth, this.imageHeight);
      if (!this.totallyDisabled) {
         if (this.showAddon) {
            guiGraphics.fill(k + 63, l + 64, k + 168, l + 185, 0xFF000000);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(resource, k + 62, l + 127, 62, 131, 107, 5);
         } else {
            if (this.backWhite) {
               guiGraphics.blit(resource, k + 63, l + 64, 176, 129, 63, 63);
               guiGraphics.blit(resource, k + 63 + 63, l + 64, 176, 129, 42, 63);
            }

            IJBRMerchantEntry selectedEntry = this.getSelectedEntry();
            if (this.showDesc) {
               guiGraphics.blit(resource, k + 63, l + 131, 176, 192, 55, 55);
               guiGraphics.blit(resource, k + 63 + 55, l + 131, 190, 192, 50, 55);
            } else if (typeNow == JBMerchantType.blueprint) {
               guiGraphics.fill(k + 112, l + 151, k + 112 + 50, l + 151 + 7, 0xFF000000);
               guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               guiGraphics.blit(resource, k + 118, l + 151, 203, 0, 36, 7);
            } else if (typeNow == JBMerchantType.addon) {
               guiGraphics.fill(k + 112, l + 151, k + 112 + 50, l + 151 + 7, 0xFF000000);
               guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               guiGraphics.blit(resource, k + 118, l + 151, 203, 7, 36, 7);
            } else if (selectedEntry == null || !selectedEntry.hasAction()) {
               guiGraphics.blit(resource, k + 148, l + 151, 196, 0, 7, 7);
            }

            if (selectedEntry == null) {
               return;
            }

            int drawPosX = 115;
            int drawPosY = 120;
            this.drawModels(guiGraphics, selectedEntry, k + drawPosX, l + drawPosY, 30.0F);
            if (!this.showDesc) {
               this.drawSelectedMerchantCost(guiGraphics, selectedEntry, k + 64, l + 167);
            }
         }

         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   /**
    * 1.12.2 {@code drawModels}: spins the selected entry's model in the detail panel.
    *
    * <p>1.20.1 has no {@code renderAll(modelMeta, scale, pass)} vertex scale, so the 1.12.2
    * {@code 0.0625F * (1 / scaleEx)} argument is dropped; the pose transforms (including the
    * {@code scaleEx} expand) are preserved. The geometry is emitted into the frame's buffer source
    * and flushed by pass, lit with the full-bright lightmap in place of
    * {@code RenderHelper.enableStandardItemLighting}.
    */
   public void drawModels(GuiGraphics guiGraphics, IJBRMerchantEntry entry, int xPosition, int yPosition, float scale) {
      if (entry != null) {
         IJBModelItem.IJBModel model = entry.getModel();
         if (model != null) {
            float scaleEx = entry.getCatalogScale();
            if (scaleEx == 0.0F || Float.isNaN(scaleEx) || Float.isInfinite(scaleEx)) {
               scaleEx = 1.0F;
            }

            MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
            PoseStack pose = guiGraphics.pose();
            this.minecraft.gameRenderer.lightTexture().turnOnLightLayer();

            for (int pass = 0; pass < 2; ++pass) {
               model.setRenderState(pose, null, 0, 0.0F, null, pass);
               pose.pushPose();
               pose.translate((double)xPosition, (double)yPosition, 50.0D);
               pose.scale(-scale, -scale, -scale);
               pose.translate(0.0D, 1.0D, 0.0D);
               pose.scale(scaleEx, scaleEx, scaleEx);
               pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
               pose.mulPose(Axis.XP.rotationDegrees(10.0F));
               pose.mulPose(Axis.YP.rotationDegrees(-0.5F * (float)rotateTick));
               pose.translate(0.0D, -1.0D * (1.0D / (double)scaleEx), 0.0D);
               model.renderAll(entry.getModelMeta(), pose, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pass);
               pose.popPose();
               bufferSource.endBatch();
            }

            this.minecraft.gameRenderer.lightTexture().turnOffLightLayer();
            ++rotateTick;
         }
      }
   }

   private void drawSelectedMerchantCost(GuiGraphics guiGraphics, IJBRMerchantEntry entry, int xPosition, int yPosition) {
      if (entry != null) {
         ItemStack itemStack = entry.getMerchantCost();
         if (!CompatHelper.isStackEmptyOrNull(itemStack)) {
            String s = ChatFormatting.WHITE + "" + itemStack.getCount();
            guiGraphics.renderItem(itemStack, xPosition, yPosition);
            guiGraphics.renderItemDecorations(this.font, itemStack, xPosition, yPosition, s);
         }
      }
   }

   private IJBRMerchantEntry getSelectedEntry() {
      IJBRMerchantPackage selectedMPackage = this.getSelectedPackage();
      return selectedMPackage != null ? selectedMPackage.getMerchantEntry(typeNow, selectedEntryIndex) : null;
   }

   private IJBRMerchantPackage getSelectedPackage() {
      return selectedPackageIndex >= 0 && selectedPackageIndex < this.packageList.size() ? this.packageList.get(selectedPackageIndex) : null;
   }

   @Override
   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      if (this.totallyDisabled) {
         return;
      }

      int id = par1GuiButton.getId();
      if (id == ButtonAddonID) {
         this.showDesc = false;
         this.showAddon = true;
      }

      if (id != ButtonModelDescID) {
         if (id == ButtonBackColorID) {
            this.backWhite = !this.backWhite;
         } else if (id == ContainerModelCatalog.TAB_MODEL_ID) {
            typeNow = JBMerchantType.model;
            this.tabSetCatalog.setActiveIndex(0);
            this.Slider.setValue(0.0);
            selectedEntryIndex = 0;
         } else if (id == ContainerModelCatalog.TAB_BLUEPRINT_ID) {
            typeNow = JBMerchantType.blueprint;
            this.tabSetCatalog.setActiveIndex(1);
            this.Slider.setValue(0.0);
            selectedEntryIndex = 0;
         } else if (id == ContainerModelCatalog.TAB_ADDONS_ID) {
            typeNow = JBMerchantType.addon;
            this.tabSetCatalog.setActiveIndex(2);
            this.Slider.setValue(0.0);
            selectedEntryIndex = 0;
         } else if (id != SliderID) {
            if (par1GuiButton instanceof GuiSlotLikeButton) {
               GuiSlotLikeButton slotButton = (GuiSlotLikeButton)par1GuiButton;
               if (slotButton.packageEntry != null) {
                  this.Slider.setValue(0.0);
                  selectedPackageIndex = slotButton.index;
                  selectedEntryIndex = 0;
                  rotateTick = 0;
               } else if (slotButton.merchantEntry != null) {
                  selectedEntryIndex = slotButton.index;
                  rotateTick = 0;
               }
            } else if (par1GuiButton == this.ButtonPackagePrev) {
               if (packageListPage - 1 >= 0) {
                  --packageListPage;
               }
            } else if (par1GuiButton == this.ButtonPackageNext) {
               if (packageListPage + 1 <= this.getMaxPageIndex()) {
                  ++packageListPage;
               }
            } else if (par1GuiButton == this.ButtonBuyModel) {
               IJBRMerchantEntry entry = this.getSelectedEntry();
               Player player = this.getContainer().entityplayer;
               if (entry != null && player != null) {
                  JBRPacketSender.sendGuiActPacket(par1GuiButton.getId(), typeNow.ordinal(), selectedPackageIndex, selectedEntryIndex);
               }
            }
         }
      } else if (!this.showAddon && !this.showDesc) {
         this.showDesc = true;
         this.showAddon = false;
      } else {
         this.showDesc = false;
         this.showAddon = false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean handled = super.mouseScrolled(mouseX, mouseY, delta);
      this.handleMouseWheelInput(mouseX, mouseY, (int)delta);
      return handled;
   }

   private void handleMouseWheelInput(double mouseX, double mouseY, int dWheel) {
      if (this.Slider.visible && this.Slider.maxValue != 0.0) {
         int Lft = this.leftPos;
         int Top = this.topPos;
         if (mouseX >= Lft + 7 && mouseX < Lft + 60 && mouseY >= Top + 61 && mouseY < Top + 155) {
            this.Slider.handleSliderWheelScroll(dWheel, 5.0);
         }
      }
   }

   static {
      typeNow = JBMerchantType.blueprint;
      rotateTick = 0;
      dFormat = new DecimalFormat("#.###");
   }
}
