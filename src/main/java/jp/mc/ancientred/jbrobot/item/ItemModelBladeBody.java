package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelBladeBody;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * Blade body model item, ported from the 1.12.2 {@code jp.mc.ancientred.jbrobot.item.ItemModelBladeBody}.
 *
 * <p>1.20.1 deviations: the 1.12.2 addon had its own {@code JBRobotMODContainer.actionFactory} copied
 * from JointBlock in postInit; the port merges the addon into JointBlock, so the single
 * {@link JBModContainer#actionFactory} is used directly. The hover text key is unchanged.
 */
public class ItemModelBladeBody extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler bladeAction;

   public ItemModelBladeBody() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelBladeBody.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.bladeAction == null && JBModContainer.actionFactory != null) {
         this.bladeAction = JBModContainer.actionFactory.createAction("blade", null);
      }

      return this.bladeAction;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 0.0;
   }

   @Override
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("jbrobot.info.hasaction"));
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }
}
