package jp.mc.ancientred.jbrobot.item;

import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelBazookaTubeParts;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/**
 * Bazooka tube model item, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.ItemModelBazookaTubeParts}. No action handler and no hover text
 * in 1.12.2, so none here.
 */
public class ItemModelBazookaTubeParts extends Item implements IJBModelItem {

   public ItemModelBazookaTubeParts() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelBazookaTubeParts.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      return null;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 0.0;
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }
}
