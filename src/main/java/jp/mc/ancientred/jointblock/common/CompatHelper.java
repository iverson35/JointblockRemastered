package jp.mc.ancientred.jointblock.common;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompatHelper {
   public static ItemStack getCurrentEquippedItem(Player player) {
      return player == null ? null : player.getMainHandItem();
   }

   public static boolean isStackPlayerCurrentEquipedMainOrSub(Entity entity, ItemStack itemStack) {
      if (entity instanceof Player) {
         Player player = (Player)entity;
         if (areItemsEqual(player.getMainHandItem(), itemStack)) {
            return true;
         }

         if (areItemsEqual(player.getOffhandItem(), itemStack)) {
            return true;
         }
      }

      return false;
   }

   private static boolean areItemsEqual(ItemStack stackA, ItemStack stackB) {
      return stackA.getItem() == stackB.getItem() && stackA.getDamageValue() == stackB.getDamageValue();
   }

   public static Entity getRiddenByEntity(Entity entity) {
      List<Entity> riddenByEntities = entity.getPassengers();
      return riddenByEntities.isEmpty() ? null : (Entity)riddenByEntities.get(0);
   }

   public static boolean isStackEmptyOrNull(ItemStack target) {
      return target == null || target.isEmpty();
   }
}
