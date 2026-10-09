package jp.mc.ancientred.jbrobot.item;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelMissilePod;
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
 * Missile pod model item, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.ItemModelMissilePod}. The "bullet" action parameters (including
 * {@code cutExplosionNockBack}) are copied verbatim.
 */
public class ItemModelMissilePod extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler missileAction;

   public ItemModelMissilePod() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelMissilePod.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.missileAction == null && JBModContainer.actionFactory != null) {
         Map<String, Object> params = new Hashtable<>();
         params.put("bulletType", "missile");
         params.put("damage", 20.0);
         params.put("speed", 0.5);
         params.put("shootSide", 2.0);
         params.put("useEnergy", 10.0);
         params.put("coolTimeTick", 30.0);
         params.put("explosionSize", 1.5);
         params.put("cutExplosionNockBack", true);
         this.missileAction = JBModContainer.actionFactory.createAction("bullet", params);
      }

      return this.missileAction;
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
