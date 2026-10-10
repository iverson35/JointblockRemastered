package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.JointInventory;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code GuiJointChest}.
 *
 * <p>GL substitutions with no direct 1.20.1 counterpart: {@code GlStateManager.color(1,1,1,1)} ->
 * {@code GuiGraphics.setColor}; {@code drawTexturedModalRect} -> {@code GuiGraphics.blit};
 * {@code drawRect} -> {@code GuiGraphics.fill}.
 *
 * <p>{@code super.renderLabels} is intentionally not called: the 1.12 base
 * {@code GuiContainer.drawGuiContainerForegroundLayer} was empty, whereas the 1.20.1 base also draws
 * the ({@code Component.empty()}) screen title and the player-inventory label.  The inventory name
 * this screen draws at (8, 128) is the same string the 1.12 code drew there.
 */
@OnlyIn(Dist.CLIENT)
public class GuiJointChest extends GuiContainerWithTip<ContainerJointChest> {
   private static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   private Inventory upperChestInventory;
   private Inventory lowerChestInventory;
   private Player player;
   private GuiTabSetJoint tabSetJoint;
   private JointInventory jointInventory;
   private TileEntityJointCore teMayBeNull;
   private EntityJointed ejMayBeNull;

   public GuiJointChest(ContainerJointChest menu, Inventory playerInv) {
      super(menu, playerInv);
      this.player = playerInv.player;
      this.upperChestInventory = playerInv;
      this.imageHeight = 222;
      this.jointInventory = menu.innerChestInventory;
      this.teMayBeNull = menu.innerChestInventory.getInventoryHolderTileEntityJointCore();
      this.ejMayBeNull = menu.innerChestInventory.getInventoryHolderEntityJointed();
   }

   @Override
   public void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      if (this.teMayBeNull != null) {
         List<AbstractWidget> tabButtonList = new ArrayList<>();
         this.tabSetJoint = new GuiTabSetJoint(tabButtonList, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.core, 2, false);
         for (AbstractWidget widget : tabButtonList) {
            this.addRenderableWidget(widget);
         }
      } else if (this.ejMayBeNull != null) {
         List<AbstractWidget> tabButtonList = new ArrayList<>();
         this.tabSetJoint = new GuiTabSetJoint(tabButtonList, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.entity, 2, false);
         for (AbstractWidget widget : tabButtonList) {
            this.addRenderableWidget(widget);
         }
      }
   }

   protected void actionPerformed(IJBGuiWidget widget) {
      if (this.player != null) {
         JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(widget.getId(), 0, 0, (String)null));
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer}.  See the class javadoc: the empty 1.12 base
    * foreground layer is not reproduced through {@code super.renderLabels} on purpose.
    */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.unitinventory.interact"), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.unitinventory.fixed"), 8, 57, 4210752, false);
      String inventoryName = this.upperChestInventory.hasCustomName()
         ? this.upperChestInventory.getName().getString()
         : CompatHelper.formatLang(this.upperChestInventory.getName().getString());
      guiGraphics.drawString(this.font, inventoryName, 8, 128, 4210752, false);
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the main GUI texture, the inner panel filled
    * rect and the three slot-group frames.  {@code bindTexture(resource)} +
    * {@code drawTexturedModalRect} become {@code blit(resource, ...)}; {@code drawRect} becomes
    * {@code fill}.
    */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(resource, k, l, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.fill(k + 7, l + 10, k + 169, l + 215, -3750202);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int yPos = 18;

      for (int i = 0; i < 3; ++i) {
         guiGraphics.blit(resource, k + 7 + i * 54, l + yPos - 1, 176, 48, 54, 36);
      }

      yPos = 69;

      for (int i = 0; i < 3; ++i) {
         guiGraphics.blit(resource, k + 7 + i * 54, l + yPos - 1, 176, 48, 54, 54);
      }

      yPos = 140;

      for (int i = 0; i < 3; ++i) {
         guiGraphics.blit(resource, k + 7 + i * 54, l + yPos - 1, 176, 48, 54, 54);
      }

      yPos = 198;

      for (int i = 0; i < 3; ++i) {
         guiGraphics.blit(resource, k + 7 + i * 54, l + yPos - 1, 176, 48, 54, 18);
      }
   }
}
