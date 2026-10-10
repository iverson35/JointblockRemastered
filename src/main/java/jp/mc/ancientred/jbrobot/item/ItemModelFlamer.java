package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelFlamer;
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
 * Flamer model item, ported from the 1.12.2 {@code jp.mc.ancientred.jbrobot.item.ItemModelFlamer}.
 */
public class ItemModelFlamer extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler flamerAction;

   public ItemModelFlamer() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelFlamer.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.flamerAction == null && JBModContainer.actionFactory != null) {
         this.flamerAction = JBModContainer.actionFactory.createAction("flamer", null);
      }

      return this.flamerAction;
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
