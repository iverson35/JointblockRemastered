package jp.mc.ancientred.jbrobot.item;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelLazerGatling;
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
 * Lazer gatling model item, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.ItemModelLazerGatling}. The "bullet" action parameters are
 * copied verbatim.
 */
public class ItemModelLazerGatling extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler lGatlingAction;

   public ItemModelLazerGatling() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelLazerGatling.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.lGatlingAction == null && JBModContainer.actionFactory != null) {
         Map<String, Object> params = new Hashtable<>();
         params.put("bulletType", "lazer");
         params.put("damage", 6.0);
         params.put("speed", 3.5);
         params.put("shootSide", 1.0);
         params.put("useEnergy", 1.0);
         params.put("coolTimeTick", 3.0);
         params.put("lazerLength", 12.0);
         params.put("lazerWidth", 4.0);
         this.lGatlingAction = JBModContainer.actionFactory.createAction("bullet", params);
      }

      return this.lGatlingAction;
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
