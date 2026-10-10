package jp.mc.ancientred.jbrobot.item;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelLazerGun;
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
 * Lazer gun model item, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.ItemModelLazerGun}. {@code getRenderExpand} is 2.0.
 */
public class ItemModelLazerGun extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler lGunAction;

   public ItemModelLazerGun() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelLazerGun.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.lGunAction == null && JBModContainer.actionFactory != null) {
         Map<String, Object> params = new Hashtable<>();
         params.put("bulletType", "lazer");
         params.put("damage", 18.0);
         params.put("speed", 3.5);
         params.put("shootSide", 3.0);
         params.put("useEnergy", 4.0);
         params.put("coolTimeTick", 8.0);
         params.put("lazerLength", 48.0);
         params.put("lazerWidth", 4.0);
         this.lGunAction = JBModContainer.actionFactory.createAction("bullet", params);
      }

      return this.lGunAction;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 2.0;
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
