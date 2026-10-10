package jp.mc.ancientred.jointblock.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import jp.mc.ancientred.jointblock.registry.JBBlocks;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiTabSetJoint}: builds the per-page tab buttons and adds them to the screen owned
 * button list.  The 1.12 class has no drawing code of its own (the tab strip is those buttons), so the
 * 1.12 {@code RenderItem} parameter is dropped and each tab's sprite/icon rendering lives in
 * {@link GuiTabButton}.
 */
@OnlyIn(Dist.CLIENT)
public class GuiTabSetJoint implements GuiTabButton.ITabButtonParent {
   private static final int TOP_PADDING = 10;
   private static final int MOVE_PADDING = 182;
   public GuiTabButton ButtonTabBases;
   public GuiTabButton ButtonTabExtra;
   public GuiTabButton ButtonTabCoreExtra;
   public GuiTabButton ButtonTabCoreFuels;
   public GuiTabButton ButtonTabCoreAddons;
   public GuiTabButton ButtonTabCoreSounds;
   public GuiTabButton ButtonTabCoreInv;
   public GuiTabButton ButtonTabToParent;
   public GuiTabButton ButtonTabToForward;
   private int selectedTabForPage;

   public GuiTabSetJoint(List<AbstractWidget> buttonList, Consumer<IJBGuiWidget> onPress, int xPosition, int yPosition, TabSetType type, int selectedIndex, boolean showMoveTab) {
      this.selectedTabForPage = selectedIndex;
      if (type != TabSetType.normal && type != TabSetType.core) {
         if (type == TabSetType.entity) {
            buttonList.add(this.ButtonTabCoreInv = new GuiTabButton(7223, xPosition, TOP_PADDING + yPosition + 0, 22, 27, 2, this, new ItemStack(Blocks.CHEST), onPress));
            buttonList.add(this.ButtonTabCoreFuels = new GuiTabButton(7226, xPosition, TOP_PADDING + yPosition + 27, 22, 27, 3, this, 3, onPress));
         }
      } else {
         buttonList.add(this.ButtonTabBases = new GuiTabButton(7221, xPosition, TOP_PADDING + yPosition + 0, 22, 27, 0, this, new ItemStack(JBBlocks.JointNormalBlock), onPress));
         buttonList.add(this.ButtonTabExtra = new GuiTabButton(7222, xPosition, TOP_PADDING + yPosition + 27, 22, 27, 1, this, 2, onPress));
         if (type == TabSetType.core) {
            buttonList.add(this.ButtonTabCoreFuels = new GuiTabButton(7226, xPosition, TOP_PADDING + yPosition + 54, 22, 27, 3, this, 3, onPress));
            buttonList.add(this.ButtonTabCoreAddons = new GuiTabButton(7227, xPosition, TOP_PADDING + yPosition + 81, 22, 27, 4, this, 4, onPress));
            buttonList.add(this.ButtonTabCoreSounds = new GuiTabButton(7228, xPosition, TOP_PADDING + yPosition + 108, 22, 27, 5, this, 5, onPress));
            buttonList.add(this.ButtonTabCoreInv = new GuiTabButton(7223, xPosition, TOP_PADDING + yPosition + 135, 22, 27, 2, this, new ItemStack(Blocks.CHEST), onPress));
         }
      }

      if (showMoveTab) {
         buttonList.add(this.ButtonTabToParent = new GuiTabButton(7224, xPosition, MOVE_PADDING + yPosition - 0, 22, 27, 99, this, 0, onPress));
         buttonList.add(this.ButtonTabToForward = new GuiTabButton(7225, xPosition, MOVE_PADDING + yPosition - 27, 22, 27, 99, this, 1, onPress));
      }
   }

   @Override
   public int getActiveIndex() {
      return this.selectedTabForPage;
   }

   public void setVisibleMoveToTab(boolean value) {
      if (this.ButtonTabToParent != null) {
         this.ButtonTabToParent.visible = value;
      }

      if (this.ButtonTabToForward != null) {
         this.ButtonTabToForward.visible = value;
      }
   }

   public enum TabSetType {
      normal,
      core,
      entity
   }
}
