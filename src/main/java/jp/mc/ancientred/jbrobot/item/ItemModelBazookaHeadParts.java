package jp.mc.ancientred.jbrobot.item;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelBazookaHeadParts;
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
 * Bazooka head model item with the 1.12.2 rocket / mortar damage-value subtypes.
 *
 * <p>1.20.1 deviation: the model meta is the vanilla {@code Damage} tag read through
 * {@link ItemStack#getDamageValue()} (the same convention {@code modelpack.ExtraPackage} and
 * {@code TileEntityJoint#getModelItemDamage} use). The rocket/mortar "bullet" action parameters are
 * copied verbatim.
 */
public class ItemModelBazookaHeadParts extends Item implements IJBModelItem {
   public static final int ITEM_META_ROCKET = 0;
   public static final int ITEM_META_MORTAR = 1;
   private IJBModelItem.IJBActionHandler rocketAction;
   private IJBModelItem.IJBActionHandler mortarAction;

   public ItemModelBazookaHeadParts() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelBazookaHeadParts.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (modelMeta == 1) {
         if (this.mortarAction == null && JBModContainer.actionFactory != null) {
            Map<String, Object> params = new Hashtable<>();
            params.put("bulletType", "mortar");
            params.put("damage", 20.0);
            params.put("speed", 1.5);
            params.put("shootSide", 1.0);
            params.put("useEnergy", 18.0);
            params.put("coolTimeTick", 60.0);
            params.put("explosionSize", 4.0);
            this.mortarAction = JBModContainer.actionFactory.createAction("bullet", params);
         }

         return this.mortarAction;
      } else {
         if (this.rocketAction == null && JBModContainer.actionFactory != null) {
            Map<String, Object> params = new Hashtable<>();
            params.put("bulletType", "rocket");
            params.put("damage", 20.0);
            params.put("speed", 1.5);
            params.put("shootSide", 1.0);
            params.put("useEnergy", 15.0);
            params.put("coolTimeTick", 60.0);
            params.put("explosionSize", 3.0);
            this.rocketAction = JBModContainer.actionFactory.createAction("bullet", params);
         }

         return this.rocketAction;
      }
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 0.0;
   }

   @Override
   public Component getName(ItemStack stack) {
      return super.getName(stack).copy()
            .append(Component.translatable(stack.getDamageValue() == 1 ? "jbrobot.typeb" : "jbrobot.typea"));
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
