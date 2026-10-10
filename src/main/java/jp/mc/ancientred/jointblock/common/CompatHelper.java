package jp.mc.ancientred.jointblock.common;

import java.util.List;
import jp.mc.ancientred.jointblock.JBModContainer;
import net.minecraft.util.StringUtil;
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
      // 1.12.2 used ItemStack.func_77970_a (item + meta + NBT); isSameItemSameTags compares item + tag (damage lives in the tag).
      return ItemStack.isSameItemSameTags(stackA, stackB);
   }

   public static Entity getRiddenByEntity(Entity entity) {
      List<Entity> riddenByEntities = entity.getPassengers();
      return riddenByEntities.isEmpty() ? null : (Entity)riddenByEntities.get(0);
   }

   public static boolean isStackEmptyOrNull(ItemStack target) {
      return target == null || target.isEmpty();
   }

   public static String formatLang(String translateKey, Object... parameters) {
      return StringUtil.isNullOrEmpty(translateKey) ? "" : JBModContainer.proxy.formatLang(translateKey, parameters);
   }
}
