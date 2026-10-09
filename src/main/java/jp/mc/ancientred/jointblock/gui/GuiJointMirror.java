package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiScreenClosable;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiJointMirror extends GuiScreenClosable {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   protected int xSize = 176;
   protected int ySize = 107;
   protected int guiLeft;
   protected int guiTop;
   public static final int ButtonMirrorRightToLeftID = 20;
   public static final int ButtonMirrorLeftToRightID = 10;
   public static final int ButtonMirrorDownToUpID = 30;
   public static final int ButtonMirrorUpToDownID = 40;
   public static final int ButtonMirrorDeleteConfID = 50;
   public static final int ButtonMirrorDeleteExecID = 60;
   private GuiButtonRed ButtonMirrorRightToLeft;
   private GuiButtonRed ButtonMirrorLeftToRight;
   private GuiButtonRed ButtonMirrorDownToUp;
   private GuiButtonRed ButtonMirrorUpToDown;
   private GuiButtonRed ButtonMirrorDeleteConf;
   private GuiButtonRed ButtonMirrorDeleteExec;
   private Player player;
   TileEntityJoint te;
   private int pageMode = 0;
   private static final int MODE_NORMAL = 0;
   private static final int MODE_DELETE = 1;
   private String confButtonMesDel;
   private String confButtonMesBak;

   public GuiJointMirror(Player par1Player, TileEntityJoint te) {
      super();
      this.te = te;
      this.player = par1Player;
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
      int buttonShiftH = buttonWidth + 2;
      this.ButtonMirrorLeftToRight = new GuiButtonRed(ButtonMirrorLeftToRightID, xLeft + 7, yTop + 50, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.mirrorleft2right"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorLeftToRight);
      this.ButtonMirrorRightToLeft = new GuiButtonRed(ButtonMirrorRightToLeftID, this.ButtonMirrorLeftToRight.getX() + buttonShiftH, yTop + 50, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.mirrorright2left"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorRightToLeft);
      this.ButtonMirrorDownToUp = new GuiButtonRed(ButtonMirrorDownToUpID, xLeft + 48, this.ButtonMirrorRightToLeft.getY() - 22, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.mirrordown2up"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorDownToUp);
      this.ButtonMirrorUpToDown = new GuiButtonRed(ButtonMirrorUpToDownID, xLeft + 48, this.ButtonMirrorRightToLeft.getY() + 22, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.mirrorup2down"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorUpToDown);
      this.confButtonMesDel = CompatHelper.formatLang("jointGui.info.delete");
      this.confButtonMesBak = CompatHelper.formatLang("jointGui.info.goback");
      this.ButtonMirrorDeleteConf = new GuiButtonRed(ButtonMirrorDeleteConfID, xLeft + 7, yTop + 80, 40, buttonHeight, this.confButtonMesDel, this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorDeleteConf);
      this.ButtonMirrorDeleteExec = new GuiButtonRed(ButtonMirrorDeleteExecID, xLeft + 7, this.ButtonMirrorDownToUp.getY(), this.xSize - 7 - 7, buttonHeight, CompatHelper.formatLang("jointGui.info.deleteexec"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonMirrorDeleteExec);
      this.ButtonMirrorDeleteExec.active = false;
      this.ButtonMirrorDeleteExec.visible = false;
      this.updateInputStates();
   }

   @Override
   public void tick() {
      super.tick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      if (!this.isScreenTargetStillValid(this.player)) {
         this.player.closeContainer();
      }

      if (this.pageMode == MODE_DELETE) {
         this.ButtonMirrorDeleteExec.active = true;
         this.ButtonMirrorDeleteExec.visible = true;
         this.ButtonMirrorDeleteConf.setMessage(Component.literal(this.confButtonMesBak));
         this.ButtonMirrorLeftToRight.active = false;
         this.ButtonMirrorLeftToRight.visible = false;
         this.ButtonMirrorRightToLeft.active = false;
         this.ButtonMirrorRightToLeft.visible = false;
         this.ButtonMirrorDownToUp.active = false;
         this.ButtonMirrorDownToUp.visible = false;
         this.ButtonMirrorUpToDown.active = false;
         this.ButtonMirrorUpToDown.visible = false;
      } else {
         this.ButtonMirrorDeleteExec.active = false;
         this.ButtonMirrorDeleteExec.visible = false;
         this.ButtonMirrorDeleteConf.setMessage(Component.literal(this.confButtonMesDel));
         this.ButtonMirrorLeftToRight.active = true;
         this.ButtonMirrorLeftToRight.visible = true;
         this.ButtonMirrorRightToLeft.active = true;
         this.ButtonMirrorRightToLeft.visible = true;
         this.ButtonMirrorDownToUp.active = true;
         this.ButtonMirrorDownToUp.visible = true;
         this.ButtonMirrorUpToDown.active = true;
         this.ButtonMirrorUpToDown.visible = true;
      }

   }

   private boolean isScreenTargetStillValid(Player playerRef) {
      return playerRef != null && BlockJointBase.getTileEntityJointerBase(playerRef.level(), this.te.getBlockPos()) != null;
   }

   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      if (par1GuiButton.getId() == ButtonMirrorDeleteConfID) {
         if (this.pageMode == MODE_NORMAL) {
            this.pageMode = MODE_DELETE;
            return;
         }

         if (this.pageMode == MODE_DELETE) {
            this.pageMode = MODE_NORMAL;
            return;
         }
      }

      if (this.player != null && this.isScreenTargetStillValid(this.player)) {
         BlockPos tePos = this.te.getBlockPos();
         JBNetwork.sendToServer(JBPacketSender.createMirrorGUIActPacket(par1GuiButton.getId(), tePos.getX(), tePos.getY(), tePos.getZ()));
      }

      this.player.closeContainer();
   }

   /**
    * 1.12.2 {@code func_73863_a} (drawScreen).  1.12 called {@code func_146276_q_()} (default
    * background) then {@code drawGuiContainerBackgroundLayer}, then {@code super.func_73863_a}
    * (widgets only) and finally {@code drawGuiContainerForegroundLayer} under a
    * {@code translate(guiLeft, guiTop)}.  1.20.1 {@code Screen.render} only draws the widgets, so
    * the default background and the two explicit layers are issued here in the same order.  The
    * GL depth/lighting toggles around the layer calls have no 1.20.1 counterpart and are dropped;
    * {@code renderBackground} additionally applies the 1.20.1 world blur, which 1.12's
    * {@code func_146276_q_()} did not have.
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
      String descStr = this.pageMode == MODE_DELETE
         ? CompatHelper.formatLang("jointGui.info.deleteconf")
         : CompatHelper.formatLang("jointGui.info.mirrortool.desc");
      guiGraphics.drawString(this.font, descStr, this.xSize / 2 - this.font.width(descStr) / 2, 10, -16776961, false);
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
