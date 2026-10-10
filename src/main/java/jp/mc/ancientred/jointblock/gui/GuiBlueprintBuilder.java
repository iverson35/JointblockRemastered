package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBlueprintBuilder extends GuiContainerWithTip<ContainerBlueprintBuilder> {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   public static final int ButtonBuildFromBlueprintID = 2;
   public static final int CheckBoxAlignAsBottomID = 10;
   public static final int CheckBoxAlignAsOriginalID = 11;
   public static final int CheckBoxAlignAsTopID = 12;
   private GuiButtonJB ButtonBuildFromBlueprint;
   private GuiCheckBoxNoShadow CheckBoxAlignAsBottom;
   private GuiCheckBoxNoShadow CheckBoxAlignAsOriginal;
   private GuiCheckBoxNoShadow CheckBoxAlignAsTop;
   TileEntityBlueprintBuilder te;
   private Player player;

   public GuiBlueprintBuilder(ContainerBlueprintBuilder menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.te;
      this.player = playerInv.player;
      this.imageHeight = 222;
   }

   @Override
   protected void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      int buttonHeight = 20;
      this.ButtonBuildFromBlueprint = new GuiButtonJB(ButtonBuildFromBlueprintID, xLeft + 68, yTop + 20, 99, buttonHeight, CompatHelper.formatLang("jointGui.info.dobuild"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonBuildFromBlueprint);
      int checkBoxTop = 97;
      this.CheckBoxAlignAsBottom = new GuiCheckBoxNoShadow(CheckBoxAlignAsBottomID, xLeft + 9, yTop + checkBoxTop, CompatHelper.formatLang("jointGui.info.alignasbottom"), false, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAlignAsBottom);
      checkBoxTop += 14;
      this.CheckBoxAlignAsOriginal = new GuiCheckBoxNoShadow(CheckBoxAlignAsOriginalID, xLeft + 9, yTop + checkBoxTop, CompatHelper.formatLang("jointGui.info.alignasoriginal"), false, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAlignAsOriginal);
      checkBoxTop += 14;
      this.CheckBoxAlignAsTop = new GuiCheckBoxNoShadow(CheckBoxAlignAsTopID, xLeft + 9, yTop + checkBoxTop, CompatHelper.formatLang("jointGui.info.alignastop"), false, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAlignAsTop);
      this.CheckBoxAlignAsBottom.packedFGColour = 0;
      this.CheckBoxAlignAsOriginal.packedFGColour = 0;
      this.CheckBoxAlignAsTop.packedFGColour = 0;
      Level world = this.te.getLevel();
      BlockPos pos = this.te.getBlockPos();
      boolean isBelowAir = world.isEmptyBlock(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
      boolean isAboveAir = world.isEmptyBlock(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
      if (!isBelowAir) {
         this.CheckBoxAlignAsBottom.setIsChecked(true);
      } else if (!isAboveAir) {
         this.CheckBoxAlignAsTop.setIsChecked(true);
      } else {
         this.CheckBoxAlignAsOriginal.setIsChecked(true);
      }

   }

   @Override
   protected void containerTick() {
      super.containerTick();
      CompoundTag dataTag = this.getBlueprintDataFromSlotZero();
      if (dataTag != null && !dataTag.getBoolean("jb:outdate")) {
         this.ButtonBuildFromBlueprint.active = true;
      } else {
         this.ButtonBuildFromBlueprint.active = false;
      }

   }

   private static final String translateCommaSeparated(String bindName) {
      if (bindName == null) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         String[] strAry = bindName.split(",");
         String[] var3 = strAry;
         int var4 = strAry.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String str = var3[var5];
            builder.append(CompatHelper.formatLang(str));
         }

         return builder.toString();
      }
   }

   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      this.toggleRadioCheck(par1GuiButton);
      if (par1GuiButton == this.ButtonBuildFromBlueprint && this.player != null) {
         int checkedId = this.CheckBoxAlignAsBottom.getId();
         if (this.CheckBoxAlignAsBottom.isChecked()) {
            checkedId = this.CheckBoxAlignAsBottom.getId();
         }

         if (this.CheckBoxAlignAsOriginal.isChecked()) {
            checkedId = this.CheckBoxAlignAsOriginal.getId();
         }

         if (this.CheckBoxAlignAsTop.isChecked()) {
            checkedId = this.CheckBoxAlignAsTop.getId();
         }

         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(par1GuiButton.getId(), checkedId, 0, (String)null));
      }

   }

   private void toggleRadioCheck(IJBGuiWidget par1GuiButton) {
      if (par1GuiButton == this.CheckBoxAlignAsBottom) {
         if (this.CheckBoxAlignAsBottom.isChecked()) {
            this.CheckBoxAlignAsOriginal.setIsChecked(false);
            this.CheckBoxAlignAsTop.setIsChecked(false);
         } else {
            this.CheckBoxAlignAsBottom.setIsChecked(true);
         }
      }

      if (par1GuiButton == this.CheckBoxAlignAsOriginal) {
         if (this.CheckBoxAlignAsOriginal.isChecked()) {
            this.CheckBoxAlignAsBottom.setIsChecked(false);
            this.CheckBoxAlignAsTop.setIsChecked(false);
         } else {
            this.CheckBoxAlignAsOriginal.setIsChecked(true);
         }
      }

      if (par1GuiButton == this.CheckBoxAlignAsTop) {
         if (this.CheckBoxAlignAsTop.isChecked()) {
            this.CheckBoxAlignAsBottom.setIsChecked(false);
            this.CheckBoxAlignAsOriginal.setIsChecked(false);
         } else {
            this.CheckBoxAlignAsTop.setIsChecked(true);
         }
      }

   }

   private CompoundTag getBlueprintDataFromSlotZero() {
      Slot slotZero = this.menu.getSlot(0);
      ItemStack stack = slotZero.getItem();
      if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getItem() instanceof ItemJBBlueprint && stack.hasTag()) {
         CompoundTag tag = stack.getTag();
         if (tag != null && tag.contains("jb:blueprint")) {
            return tag.getCompound("jb:blueprint");
         }
      }

      return null;
   }

   /** 1.12.2 {@code func_146976_a} (drawGuiContainerBackgroundLayer). */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.leftPos;
      int l = this.topPos;
      guiGraphics.blit(resource, k, l, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.blit(resource, k + 148, l + 21, 176, 0, 20, 20);
      int border = 94;
      guiGraphics.blit(resource, k + 7, l + border - 1, 7, 175, 162, 1);
      guiGraphics.fill(k + 7, l + border, k + 169, l + 176, -3750202);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int slotStart = 139;
      guiGraphics.blit(resource, k + 7, l + slotStart + 0, 7, 197, 168, 18);
      guiGraphics.blit(resource, k + 7, l + slotStart + 18, 7, 197, 168, 18);
      guiGraphics.blit(resource, k + 7, l + slotStart + 36, 7, 197, 168, 18);
   }

   /** 1.12.2 {@code func_146979_b} (drawGuiContainerForegroundLayer). */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderLabels(guiGraphics, mouseX, mouseY);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.bpbuilder"), 8, 6, 4210752, false);
      CompoundTag dataTag = this.getBlueprintDataFromSlotZero();
      if (dataTag != null) {
         if (dataTag.getBoolean("jb:outdate")) {
            guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.bpoutdated"), 14, 48, 15658734, false);
         } else {
            int coreNum = dataTag.getInt("cnm");
            int seatNum = dataTag.getInt("snm");
            int normalNum = dataTag.getInt("nnm");
            int rotatorNum = dataTag.getInt("rnm");
            int extenderNum = dataTag.getInt("enm");
            int scalerNum = dataTag.getInt("scl");
            int sliderNum = dataTag.getInt("sld");
            int attracterNum = dataTag.getInt("atr");
            int totalNum = dataTag.getInt("tnm");
            int rowA = 48;
            int rowB = rowA + 12;
            int rowC = rowB + 11;
            int rowD = rowC + 11;
            int newleftA = 14;
            int newleftB = newleftA + 39;
            int newleftC = newleftB + 39;
            int newleftD = newleftC + 39;
            if (dataTag.contains("name")) {
               guiGraphics.drawString(this.font, CompatHelper.formatLang(dataTag.getString("name")), newleftA, rowA, 15658734, false);
            }

            guiGraphics.drawString(this.font, "CO:" + coreNum, newleftA, rowB, 15658734, false);
            guiGraphics.drawString(this.font, "SE:" + seatNum, newleftB, rowB, 15658734, false);
            guiGraphics.drawString(this.font, "NM:" + normalNum, newleftC, rowB, 15658734, false);
            guiGraphics.drawString(this.font, "RO:" + rotatorNum, newleftD, rowB, 15658734, false);
            guiGraphics.drawString(this.font, "EX:" + extenderNum, newleftA, rowC, 15658734, false);
            guiGraphics.drawString(this.font, "SC:" + scalerNum, newleftB, rowC, 15658734, false);
            guiGraphics.drawString(this.font, "SL:" + sliderNum, newleftC, rowC, 15658734, false);
            guiGraphics.drawString(this.font, "AT:" + attracterNum, newleftD, rowC, 15658734, false);
            String all = "ALL:" + totalNum;
            int w = this.font.width(all);
            guiGraphics.drawString(this.font, all, 155 - w, rowD, 15658734, false);
         }
      } else {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.setblueprint"), 18, 49, 15658734, false);
      }

   }
}
