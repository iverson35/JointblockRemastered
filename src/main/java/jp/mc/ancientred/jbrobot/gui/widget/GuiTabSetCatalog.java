package jp.mc.ancientred.jbrobot.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import jp.mc.ancientred.jbrobot.JBRef;
import jp.mc.ancientred.jbrobot.registry.JBRItems;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiTabSetCatalog}: builds the three catalog tab buttons and adds them to the
 * screen-owned widget list.  The 1.12 class has no drawing code of its own (the tab strip is those
 * buttons), so the 1.12 {@code RenderItem} parameter is dropped and each tab's sprite/icon rendering
 * lives in {@link GuiTabButton}.
 *
 * <p>The 1.12.2 source assigned {@code ButtonTabExtra} twice (the blueprint and the addon tab) and
 * never kept the first reference; that is preserved verbatim.
 */
@OnlyIn(Dist.CLIENT)
public class GuiTabSetCatalog implements GuiTabButton.ITabButtonParent {
   private static final int TOP_PADDING = 10;
   public GuiTabButton ButtonTabBases;
   public GuiTabButton ButtonTabExtra;
   private int selectedTabForPage;

   public GuiTabSetCatalog(List<AbstractWidget> buttonList, Consumer<IJBGuiWidget> onPress, int xPosition, int yPosition, int selectedIndex) {
      this.selectedTabForPage = selectedIndex;
      buttonList.add(this.ButtonTabBases = new GuiTabButton(500, xPosition, TOP_PADDING + yPosition + 0, 22, 27, 0, this, new ItemStack(JBRItems.ModelFaceItem.get()), onPress));
      buttonList.add(this.ButtonTabExtra = new GuiTabButton(600, xPosition, TOP_PADDING + yPosition + 27, 22, 27, 1, this, JBRef.bluprintItemStackMockDummy, onPress));
      buttonList.add(this.ButtonTabExtra = new GuiTabButton(700, xPosition, TOP_PADDING + yPosition + 54, 22, 27, 2, this, new ItemStack(JBRItems.BasicAddonItem.get()), onPress));
   }

   @Override
   public int getActiveIndex() {
      return this.selectedTabForPage;
   }

   public void setActiveIndex(int index) {
      this.selectedTabForPage = index;
   }
}
