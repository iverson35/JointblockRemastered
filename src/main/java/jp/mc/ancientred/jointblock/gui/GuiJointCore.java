package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBBase64Util;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldJP;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointCore}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code drawTexturedModalRect} -> {@code GuiGraphics.blit};
 * {@code drawRect} -> {@code GuiGraphics.fill}; {@code RenderHelper.enableStandardItemLighting/
 * disableStandardItemLighting} around the manual unit-name field draw is dropped (1.20.1 widget
 * rendering is unlit).
 *
 * <p>{@code super.renderLabels} is intentionally not called: the 1.12 base
 * {@code GuiContainer.drawGuiContainerForegroundLayer} was empty, whereas the 1.20.1 base also draws
 * the ({@code Component.empty()}) screen title and the player-inventory label, which this 1.12 GUI
 * never had.
 *
 * <p>Dropped with no 1.20.1 counterpart: {@code Keyboard.enableRepeatEvents} and the manual
 * {@code handleKeyboardInput} input pump; {@code handleMouseWheelInput} was a no-op in 1.12
 * (it computed local values and discarded them), so it has no port.
 */
@OnlyIn(Dist.CLIENT)
public class GuiJointCore extends GuiContainerWithTip<ContainerJointCore> {
   private static final int MAXRESULTROWCOUNT = 4;
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   public static final int ButtonBindNumPrevID = 2;
   public static final int ButtonBindNumNextID = 4;
   public static final int ButtonDoAssembleID = 6;
   public static final int ButtonUnitSearchID = 8;
   public static final int CheckBoxDeplyUnitPartsID = 10;
   public static final int CheckBoxNoMoveID = 12;
   public static final int CheckBoxAutoBoardID = 14;
   public static final int CheckBoxSlaveModeID = 16;
   public static final int ButtonSlaveSlotIncID = 20;
   public static final int ButtonSlaveSlotDecID = 21;
   public static final int TexJPID = 97;
   private static final int ROW_HIGHT = 25;
   private GuiTabSetJoint tabSetJoint;
   private GuiButtonJB ButtonBindNumPrev;
   private GuiButtonJB ButtonBindNumNext;
   private GuiButtonJB ButtonDoAssemble;
   private GuiButtonJB ButtonUnitSearch;
   private GuiCheckBoxNoShadow CheckBoxNoMove;
   private GuiCheckBoxNoShadow CheckBoxAutoBoard;
   private GuiCheckBoxNoShadow CheckBoxSlaveMode;
   private GuiTextFieldJP TextFieldUnitName;
   private GuiButtonJB ButtonSlaveSlotInc;
   private GuiButtonJB ButtonSlaveSlotDec;
   private Player player;
   TileEntityJointCore te;
   private String savedUnitName = "";
   private ItemStack itemStackForIconSteve;
   private ItemStack itemStackForIconCreep;
   private boolean isAssembleButtonSended;

   public GuiJointCore(ContainerJointCore menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.teCore;
      this.player = menu.entityplayer;
      this.imageHeight = 222;
      this.itemStackForIconSteve = new ItemStack(Items.PLAYER_HEAD);
      this.itemStackForIconCreep = new ItemStack(Items.CREEPER_HEAD);
   }

   @Override
   public void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      List<AbstractWidget> tabButtonList = new ArrayList<>();
      this.tabSetJoint = new GuiTabSetJoint(tabButtonList, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.core, 0, false);
      for (AbstractWidget widget : tabButtonList) {
         this.addRenderableWidget(widget);
      }
      int buttonWidth = this.imageWidth - 14;
      int buttonHeight = 20;
      this.addRenderableWidget(this.ButtonDoAssemble = new GuiButtonRed(6, xLeft + 7, yTop + 176, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.assemble"), this::actionPerformed));
      buttonWidth = 26;
      buttonHeight = 20;
      int buttonShiftH = buttonWidth + 2;
      this.addRenderableWidget(this.ButtonBindNumPrev = new GuiButtonJB(2, xLeft + 112, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindprev"), this::actionPerformed));
      this.addRenderableWidget(this.ButtonBindNumNext = new GuiButtonJB(4, this.ButtonBindNumPrev.getX() + buttonShiftH, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindnext"), this::actionPerformed));
      buttonWidth = 96;
      buttonHeight = 20;
      this.addRenderableWidget(this.ButtonUnitSearch = new GuiButtonJB(8, xLeft + 9, yTop + 47, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.requirement"), this::actionPerformed));
      this.TextFieldUnitName = new GuiTextFieldJP(999, this.font, xLeft + 8, yTop + 95, 160, 18);
      this.TextFieldUnitName.setTextColor(-1);
      this.TextFieldUnitName.setTextColorUneditable(-1);
      this.TextFieldUnitName.setBordered(true);
      this.TextFieldUnitName.setMaxLength(15);
      if (this.te.unitName != null && this.te.unitName.length() >= 0) {
         this.TextFieldUnitName.setValue(this.te.unitName);
      } else {
         this.TextFieldUnitName.setValue("");
      }

      this.addRenderableWidget(this.CheckBoxAutoBoard = new GuiCheckBoxNoShadow(14, xLeft + 9, yTop + 130, CompatHelper.formatLang("jointGui.info.unitautoboard"), AssembleFlagsUtil.noMove(this.te.assembleFlags), this::actionPerformed));
      this.addRenderableWidget(this.CheckBoxSlaveMode = new GuiCheckBoxNoShadow(16, xLeft + 9, yTop + 145, CompatHelper.formatLang("jointGui.info.slavemode"), AssembleFlagsUtil.isSlaveMode(this.te.assembleFlags), this::actionPerformed));
      this.addRenderableWidget(this.CheckBoxNoMove = new GuiCheckBoxNoShadow(12, xLeft + 9, yTop + 160, CompatHelper.formatLang("jointGui.info.unitnomove"), AssembleFlagsUtil.boardImmidiate(this.te.assembleFlags), this::actionPerformed));
      this.CheckBoxNoMove.packedFGColour = 0;
      this.CheckBoxAutoBoard.packedFGColour = 0;
      this.addRenderableWidget(this.ButtonSlaveSlotInc = new GuiButtonJB(20, xLeft + 149, yTop + 150 - buttonHeight, 20, buttonHeight, "+", this::actionPerformed));
      this.addRenderableWidget(this.ButtonSlaveSlotDec = new GuiButtonJB(21, xLeft + 149, yTop + 150 + 1, 20, buttonHeight, "-", this::actionPerformed));
      this.updateInputStates();
   }

   private ItemStack doesPlayerHaveMirrorItem(Player player2) {
      return null;
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      this.ButtonBindNumPrev.active = true;
      this.ButtonBindNumNext.active = true;
      this.ButtonDoAssemble.active = true;
      this.CheckBoxNoMove.setIsChecked(AssembleFlagsUtil.noMove(this.te.assembleFlags));
      this.CheckBoxSlaveMode.setIsChecked(AssembleFlagsUtil.isSlaveMode(this.te.assembleFlags));
      this.CheckBoxAutoBoard.setIsChecked(AssembleFlagsUtil.boardImmidiate(this.te.assembleFlags));
      if (this.te.guiPage == 0) {
         this.ButtonUnitSearch.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.requirement")));
         this.TextFieldUnitName.setEnabled(true);
         this.TextFieldUnitName.setVisible(true);
         this.TextFieldUnitName.setTextColor(-1);
         this.TextFieldUnitName.setTextColorUneditable(-1);
         this.CheckBoxAutoBoard.visible = true;
         this.CheckBoxSlaveMode.visible = true;
         this.ButtonSlaveSlotDec.visible = this.CheckBoxSlaveMode.isChecked();
         this.ButtonSlaveSlotInc.visible = this.CheckBoxSlaveMode.isChecked();
         this.CheckBoxNoMove.visible = true;
         if (!this.TextFieldUnitName.isFocused() && !this.savedUnitName.equals(this.te.unitName)) {
            this.TextFieldUnitName.setValue(this.te.unitName);
            this.savedUnitName = this.te.unitName;
         }
      } else {
         this.ButtonUnitSearch.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.closeresult")));
         this.TextFieldUnitName.setEnabled(false);
         this.TextFieldUnitName.setVisible(false);
         this.CheckBoxAutoBoard.visible = false;
         this.CheckBoxSlaveMode.visible = false;
         this.ButtonSlaveSlotDec.visible = false;
         this.ButtonSlaveSlotInc.visible = false;
         this.CheckBoxNoMove.visible = false;
      }
   }

   protected void actionPerformed(IJBGuiWidget widget) {
      if (this.player != null) {
         int flags = 0;
         flags |= this.CheckBoxNoMove.isChecked() ? 2 : 0;
         flags |= this.CheckBoxAutoBoard.isChecked() ? 4 : 0;
         flags |= this.CheckBoxSlaveMode.isChecked() ? 8 : 0;
         String unitName = null;
         if (widget.getId() == 6) {
            unitName = this.TextFieldUnitName.getValue();
            this.isAssembleButtonSended = true;
         }

         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(widget.getId(), 0, flags, unitName));
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int eventButton) {
      boolean result = super.mouseClicked(mouseX, mouseY, eventButton);
      boolean isTextFocused = this.TextFieldUnitName.isFocused();
      this.TextFieldUnitName.mouseClicked(mouseX, mouseY, eventButton);
      if (isTextFocused && !this.TextFieldUnitName.isFocused()) {
         String name = this.TextFieldUnitName.getValue();
         if (!name.equals(this.te.unitName) && !this.isAssembleButtonSended) {
            JBPacketSender.sendJointGuiStringChanged(name, (byte)10, 0);
         }
      }

      return result;
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      return this.TextFieldUnitName.textboxKeyTyped(typedChar, 0) || super.charTyped(typedChar, modifiers);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.TextFieldUnitName.textboxKeyTyped((char)0, keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang(this.te.getTypeName()), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.setmodel"), 29, 26, 4210752, false);
      if (this.CheckBoxSlaveMode.visible && this.CheckBoxSlaveMode.isChecked()) {
         String slotNumStr = CompatHelper.formatLang("jointGui.info.slaveslot") + "[" + this.te.slaveSlotNum + "]";
         int slotNumStrWidth = this.font.width(slotNumStr);
         guiGraphics.drawString(this.font, slotNumStr, this.imageWidth - 30 - slotNumStrWidth, 147, 0, false);
         if (this.te.slaveSlotNum == 0) {
            slotNumStr = CompatHelper.formatLang("jointGui.info.slotforplayer");
            slotNumStrWidth = this.font.width(slotNumStr);
            guiGraphics.drawString(this.font, slotNumStr, this.imageWidth - 30 - slotNumStrWidth, 155, 0, false);
         }
      }

      AbstractUnitBase unit = AbstractUnitBase.getUnitBaseById(this.te.unitId);
      if (unit != null) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang(unit.getUnitTypeName()), 70, 26, 3355630, false);
      } else {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jbunit.nounittype"), 70, 26, 3355630, false);
      }

      if (this.te.guiPage == 0) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.unitnametitle"), 8, 83, 4210752, false);
         String iffTagStr = CompatHelper.formatLang("jointGui.info.iff") + ":" + (this.te.iffTagId == 0L ? "---" : JBBase64Util.longToBasd64(this.te.iffTagId));
         guiGraphics.drawString(this.font, iffTagStr, 12, 115, -11184811, false);
         String msbTagStr = CompatHelper.formatLang("jointGui.info.msb") + ":" + (this.te.msbTagId == 0L ? "---" : JBBase64Util.longToBasd64(this.te.msbTagId));
         guiGraphics.drawString(this.font, msbTagStr, 80, 115, -11184811, false);
      } else if (this.te.unitCheckResult != null && this.te.unitCheckResult.length >= 16) {
         int[] resultIntArray = this.te.unitCheckResult;
         int rowY = this.imageHeight - 151;
         int rowB = rowY + 12;
         int rowC = rowB + 11;
         int rowD = rowC + 12;
         int newleftA = 14;
         int newleftB = newleftA + 39;
         int newleftC = newleftB + 39;
         int newleftD = newleftC + 39;
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.testresult.titleformat"), 12, rowY, 15658734, false);
         guiGraphics.drawString(this.font, "CO:" + resultIntArray[8], newleftA, rowB, 15658734, false);
         guiGraphics.drawString(this.font, "SE:" + resultIntArray[9], newleftB, rowB, 15658734, false);
         guiGraphics.drawString(this.font, "NM:" + resultIntArray[10], newleftC, rowB, 15658734, false);
         guiGraphics.drawString(this.font, "RO:" + resultIntArray[11], newleftD, rowB, 15658734, false);
         guiGraphics.drawString(this.font, "EX:" + resultIntArray[12], newleftA, rowC, 15658734, false);
         guiGraphics.drawString(this.font, "SC:" + resultIntArray[13], newleftB, rowC, 15658734, false);
         guiGraphics.drawString(this.font, "SL:" + resultIntArray[14], newleftC, rowC, 15658734, false);
         guiGraphics.drawString(this.font, "AT:" + resultIntArray[15], newleftD, rowC, 15658734, false);
         String all = String.format("Block:%d MBlock:%d", resultIntArray[0], resultIntArray[1]);
         int w = this.font.width(all);
         guiGraphics.drawString(this.font, all, 155 - w, rowD, 15658734, false);
         int rowRange = rowD + 25;
         guiGraphics.drawString(this.font, String.format("X-Range min:%d, max;%d", resultIntArray[2], resultIntArray[3]), newleftA, rowRange, 15658734, false);
         rowRange += 11;
         guiGraphics.drawString(this.font, String.format("Y-Range min:%d, max;%d", resultIntArray[4], resultIntArray[5]), newleftA, rowRange, 15658734, false);
         rowRange += 11;
         guiGraphics.drawString(this.font, String.format("Z-Range min:%d, max;%d", resultIntArray[6], resultIntArray[7]), newleftA, rowRange, 15658734, false);
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture, the unit icon frame, the
    * binder-name frame and (page 0 only) the requirement-panel line + filled rect.
    * {@code bindTexture(resource)} + {@code drawTexturedModalRect} become {@code blit(resource, ...)};
    * {@code drawRect} becomes {@code fill}.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resource, k, l, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.blit(resource, k + 148, l + 21, 176, 0, 20, 20);
      guiGraphics.blit(resource, k + 106, l + 41, 176, 20, 63, 28);
      if (this.te.guiPage == 0) {
         int border = 77;
         guiGraphics.blit(resource, k + 7, l + border - 1, 7, 175, 162, 1);
         guiGraphics.fill(k + 7, l + border, k + 169, l + 176, -3750202);
      }
   }

   /**
    * 1.12.2 {@code drawScreen} only added the manual {@code drawTextBox()} of the unit-name field
    * (page 0 only) after the standard container draw (the field is a plain widget here, not an
    * {@code addRenderableWidget()} entry, so it is not drawn automatically).
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      if (this.te.guiPage == 0) {
         this.TextFieldUnitName.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
      }
   }
}
