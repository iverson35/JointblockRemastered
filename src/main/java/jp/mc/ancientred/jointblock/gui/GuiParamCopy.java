package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiScreenClosable;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiParamCopy extends GuiScreenClosable {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   protected int xSize = 176;
   protected int ySize = 107;
   protected int guiLeft;
   protected int guiTop;
   public static final int CheckBoxCopyMotionID = 2;
   public static final int CheckBoxCopyInitValID = 4;
   public static final int CheckBoxCopyExtrasID = 5;
   public static final int ButtonExecuteCopyID = 3;
   private GuiCheckBoxNoShadow CheckBoxCopyMotion;
   private GuiCheckBoxNoShadow CheckBoxCopyInitVal;
   private GuiCheckBoxNoShadow CheckBoxCopyExtras;
   private GuiButtonRed ButtonExecuteCopy;
   private Player player;
   private ItemStack itemStack;
   TileEntityJoint te;

   public GuiParamCopy(Player par1Player, TileEntityJoint te, ItemStack itemStack) {
      super();
      this.te = te;
      this.player = par1Player;
      this.itemStack = itemStack;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      int xLeft = this.guiLeft = (this.width - this.xSize) / 2;
      int yTop = this.guiTop = (this.height - this.ySize) / 2;
      int buttonHeight = 20;
      int buttonWidth = (this.xSize - 7 - 7 - 2) / 2;
      boolean isTypeMatched = ItemJBCopyTool.checkDataAndJointType(this.itemStack, this.te);
      boolean checkCpyMot = false;
      boolean checkCpyInit = false;
      boolean checkCpyExt = false;
      CompoundTag tag = ItemJBCopyTool.getCopyValueTag(this.itemStack);
      if (tag != null) {
         if (isTypeMatched) {
            checkCpyMot = tag.getBoolean("docmtn");
            checkCpyInit = tag.getBoolean("docint");
         }

         checkCpyExt = tag.getBoolean("docext");
      }

      int checkBoxLeft = 48;
      this.CheckBoxCopyMotion = new GuiCheckBoxNoShadow(CheckBoxCopyMotionID, xLeft + checkBoxLeft, yTop + 25, CompatHelper.formatLang("jointGui.info.copymotion"), checkCpyMot, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxCopyMotion);
      this.CheckBoxCopyMotion.packedFGColour = 0;
      this.CheckBoxCopyInitVal = new GuiCheckBoxNoShadow(CheckBoxCopyInitValID, xLeft + checkBoxLeft, yTop + 40, CompatHelper.formatLang("jointGui.info.copyinitvals"), checkCpyInit, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxCopyInitVal);
      this.CheckBoxCopyInitVal.packedFGColour = 0;
      this.CheckBoxCopyExtras = new GuiCheckBoxNoShadow(CheckBoxCopyExtrasID, xLeft + checkBoxLeft, yTop + 55, CompatHelper.formatLang("jointGui.info.copyextras"), checkCpyExt, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxCopyExtras);
      this.CheckBoxCopyExtras.packedFGColour = 0;
      if (!isTypeMatched) {
         this.CheckBoxCopyMotion.active = false;
         this.CheckBoxCopyInitVal.active = false;
      }

      this.ButtonExecuteCopy = new GuiButtonRed(ButtonExecuteCopyID, xLeft + 48, yTop + 70, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.executecopy"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonExecuteCopy);
      if (tag == null) {
         this.CheckBoxCopyMotion.active = false;
         this.CheckBoxCopyInitVal.active = false;
         this.CheckBoxCopyExtras.active = false;
      }

   }

   @Override
   public void tick() {
      super.tick();
      if (!this.isScreenTargetStillValid(this.player)) {
         this.player.closeContainer();
      }

   }

   private boolean isScreenTargetStillValid(Player playerRef) {
      return playerRef != null && BlockJointBase.getTileEntityJointerBase(playerRef.level(), this.te.getBlockPos()) != null;
   }

   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      if (par1GuiButton.getId() == ButtonExecuteCopyID) {
         if (this.player != null && this.isScreenTargetStillValid(this.player)) {
            int flags = 0;
            flags |= this.CheckBoxCopyMotion.isChecked() ? 1 : 0;
            flags |= this.CheckBoxCopyInitVal.isChecked() ? 2 : 0;
            flags |= this.CheckBoxCopyExtras.isChecked() ? 4 : 0;
            BlockPos pos = this.te.getBlockPos();
            JBNetwork.sendToServer(JBPacketSender.createParamCpyGUIActPacket(flags, pos.getX(), pos.getY(), pos.getZ()));
         }

         this.player.closeContainer();
      }

   }

   /**
    * 1.12.2 {@code func_73863_a} (drawScreen).  1.12 called {@code func_146276_q_()} (default
    * background) then {@code drawGuiContainerBackgroundLayer}, then {@code super.func_73863_a}
    * (widgets only) and finally {@code drawGuiContainerForegroundLayer} under a
    * {@code translate(guiLeft, guiTop)}.  1.20.1 {@code Screen.render} only draws the widgets, so
    * the default background and the two explicit layers are issued here in the same order.  The
    * {@code disableDepth/enableDepth} + item-lighting GL state around the layer calls has no
    * 1.20.1 counterpart and is dropped; {@code renderBackground} additionally applies the 1.20.1
    * world blur, which 1.12's {@code func_146276_q_()} did not have.
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
      String title = CompatHelper.formatLang("jointGui.info.paramcpy");
      guiGraphics.drawString(this.font, title, this.xSize / 2 - this.font.width(title) / 2, 10, -16776961, false);
   }

   /** 1.12.2 {@code drawGuiContainerBackgroundLayer}. */
   protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int l = (this.width - this.xSize) / 2;
      int t = (this.height - this.ySize) / 2;
      guiGraphics.blit(resource, l, t + 0, 0, 0, this.xSize, 100);
      guiGraphics.blit(resource, l, t + 100, 0, 215, this.xSize, 7);
      guiGraphics.fill(l + 7, t + 10, l + 169, t + 100, -3750202);
   }
}
