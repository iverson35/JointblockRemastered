package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.JointInventory;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointCoreFuels}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code bindTexture}+{@code drawTexturedModalRect} ->
 * {@code GuiGraphics.blit(...)}; {@code drawRect} -> {@code GuiGraphics.fill};
 * {@code func_152125_a} (scaled blit of the circuit background) -> the 11-argument
 * {@code GuiGraphics.blit(resource, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight)}.
 * {@code TextFormatting.ITALIC} -> {@code ChatFormatting.ITALIC}; both render as the legacy
 * {@code "§o"} prefix, which {@code GuiGraphics.drawString(Font, String, ...)} still decodes.
 *
 * <p>{@code super.renderLabels} is intentionally not called: the 1.12 base
 * {@code GuiContainer.drawGuiContainerForegroundLayer} was empty, whereas the 1.20.1 base also draws
 * the ({@code Component.empty()}) screen title and the player-inventory label.  The inventory name
 * this screen draws at (8, 128) is the same string the 1.12 code drew there.
 */
@OnlyIn(Dist.CLIENT)
public class GuiJointCoreFuels extends GuiContainerWithTip<ContainerJointCoreFuels> {
   private static final ResourceLocation resMainbg = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   private static final ResourceLocation resCircuit = new ResourceLocation("jointblock:textures/gui/circuitbg_b.png");
   public static final int CheckBoxInfiniteFuelID = 10;
   private GuiCheckBoxNoShadow CheckBoxInfiniteFuel;
   private GuiTabSetJoint tabSetJoint;
   private Player player;
   private JointInventory jointInventory;
   private TileEntityJointCore teMayBeNull;
   private EntityJointed ejMayBeNull;

   public GuiJointCoreFuels(ContainerJointCoreFuels menu, Inventory playerInv) {
      super(menu, playerInv);
      this.player = playerInv.player;
      this.imageHeight = 222;
      this.jointInventory = menu.innerChestInventory;
      this.teMayBeNull = this.jointInventory.getInventoryHolderTileEntityJointCore();
      this.ejMayBeNull = this.jointInventory.getInventoryHolderEntityJointed();
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
      if (this.teMayBeNull != null) {
         List<AbstractWidget> tabButtons = new ArrayList<>();
         this.tabSetJoint = new GuiTabSetJoint(tabButtons, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.core, 3, false);
         tabButtons.forEach(this::addRenderableWidget);
      } else if (this.ejMayBeNull != null) {
         List<AbstractWidget> tabButtons = new ArrayList<>();
         this.tabSetJoint = new GuiTabSetJoint(tabButtons, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.entity, 3, false);
         tabButtons.forEach(this::addRenderableWidget);
      }

      if (this.teMayBeNull != null && this.player != null && this.player.getAbilities().instabuild) {
         String label = CompatHelper.formatLang("jointGui.info.infinitefuel");
         int strW = this.font.width(label);
         this.CheckBoxInfiniteFuel = new GuiCheckBoxNoShadow(CheckBoxInfiniteFuelID, xLeft + 175 - (strW + 16), yTop + 4, label, AssembleFlagsUtil.isInfiniteFuel(this.teMayBeNull.assembleFlags), this::actionPerformed);
         this.addRenderableWidget(this.CheckBoxInfiniteFuel);
      }

   }

   @Override
   protected void containerTick() {
      super.containerTick();
      // 1.12 computed xLeft/yTop here but never used them; there is no state to update.
   }

   @Override
   protected void actionPerformed(IJBGuiWidget widget) {
      if (this.player != null) {
         int amount = 0;
         if (widget.getId() == CheckBoxInfiniteFuelID) {
            amount = this.CheckBoxInfiniteFuel.isChecked() ? 1 : 0;
         }

         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(widget.getId(), 0, amount, null));
      }

   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.jointcoreextra"), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.fuel"), 7, 22, 0, false);
      float fuelValue = this.jointInventory.getFuelValue(false);
      int fuelValueLeft = 97;
      int fuelValueTop = 44 - this.font.lineHeight / 2;
      String fuelEqStr = ChatFormatting.ITALIC + " = ";
      String fuelStr = ChatFormatting.ITALIC + String.format("%.1f", fuelValue);
      if (this.teMayBeNull != null && AssembleFlagsUtil.isInfiniteFuel(this.teMayBeNull.assembleFlags)
         || this.ejMayBeNull != null && AssembleFlagsUtil.isInfiniteFuel(this.ejMayBeNull.assembleFlags)) {
         fuelStr = CompatHelper.formatLang("jointGui.info.infinite");
      }

      guiGraphics.drawString(this.font, ChatFormatting.ITALIC + "total", fuelValueLeft + 10, fuelValueTop - this.font.lineHeight, 0, false);
      guiGraphics.drawString(this.font, fuelEqStr, fuelValueLeft, fuelValueTop, 0, false);
      int fuelCentX = fuelValueLeft + this.font.width(fuelEqStr) + 30 - this.font.width(fuelStr) / 2;
      guiGraphics.drawString(this.font, fuelStr, fuelCentX, fuelValueTop, 0, false);
      guiGraphics.drawString(this.font, ChatFormatting.ITALIC + " EN", fuelValueLeft + 55, fuelValueTop + this.font.lineHeight, 0, false);
      if (this.ejMayBeNull != null) {
         float fuelCache = this.ejMayBeNull.getEFuelPoolCache();
         if (fuelCache != 0.0F) {
            String strCache = ChatFormatting.ITALIC + String.format(" + (%.1f)", fuelCache);
            guiGraphics.drawString(this.font, strCache, fuelValueLeft, fuelValueTop + this.font.lineHeight, 0, false);
         }
      }

      String inventoryName = this.player.getInventory().hasCustomName()
         ? this.player.getInventory().getName().getString()
         : CompatHelper.formatLang(this.player.getInventory().getName().getString());
      guiGraphics.drawString(this.font, inventoryName, 8, 128, 4210752, false);
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture, the inner panel filled
    * rect, the fuel-slot frames, the circuit-background scaled blit, the fuel-gauge ring frames.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int Lft = (this.width - this.imageWidth) / 2;
      int Top = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resMainbg, Lft, Top, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.fill(Lft + 7, Top + 10, Lft + 169, Top + 196, -3750202);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int yPos;

      for (yPos = 0; yPos < 5; ++yPos) {
         guiGraphics.blit(resMainbg, Lft + 7 + yPos * 18, Top + 35, 176, 48, 18, 18);
      }

      yPos = 140;

      for (int i = 0; i < 3; ++i) {
         guiGraphics.blit(resMainbg, Lft + 7 + i * 54, Top + yPos - 1, 176, 48, 54, 54);
      }

      guiGraphics.blit(resCircuit, Lft + 19 - 12, Top + 60, 162, 60, 0.0F, 0.0F, 324, 120, 324, 120);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      yPos = Top + 65;
      int ringCol = 2;

      for (int j = 0; j < 4; ++j) {
         if (j == ringCol) {
            yPos += 19;
         }

         guiGraphics.blit(resMainbg, Lft + 41 + j % ringCol * 19, yPos, 176, 48, 18, 18);
      }
   }
}
