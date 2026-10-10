package jp.mc.ancientred.jointblock.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemJBTargetSight extends Item {
   public ItemJBTargetSight() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (world.isClientSide) {
         player.startUsingItem(hand);
         return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
      } else {
         return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
      }
   }

   @Override
   public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
      super.releaseUsing(stack, worldIn, entityLiving, timeLeft);
   }

   @Override
   public int getUseDuration(ItemStack stack) {
      return 72000;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.NONE;
   }

   public static boolean isPlayerHoldingTargetSightItem(Player player) {
      if (player != null && player.getInventory() != null) {
         ItemStack current = player.getInventory().getSelected();
         return current != null && current.getItem().getClass() == ItemJBTargetSight.class;
      } else {
         return false;
      }
   }
}
