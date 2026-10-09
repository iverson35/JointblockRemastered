package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelHarvester;
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
 * Harvester model item, ported from the 1.12.2 {@code jp.mc.ancientred.jbrobot.item.ItemModelHarvester}.
 */
public class ItemModelHarvester extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler harvesterAction;

   public ItemModelHarvester() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelHarvester.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.harvesterAction == null && JBModContainer.actionFactory != null) {
         this.harvesterAction = JBModContainer.actionFactory.createAction("harvest", null);
      }

      return this.harvesterAction;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 1.0;
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
