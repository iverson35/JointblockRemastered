package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemJBMirrorTool extends Item {
   public static final String FLIPWAY_NBT_TAG = "jb.flipway";
   public static final int FLIPWAY_LR = 0;
   public static final int FLIPWAY_UD = 1;
   public static final int FLIPWAY_DISABLED = 9;

   public ItemJBMirrorTool() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         CompoundTag nbttagcompound = stack.getTag();
         int flipWay;
         if (nbttagcompound == null) {
            nbttagcompound = new CompoundTag();
            stack.setTag(nbttagcompound);
            flipWay = 0;
         } else {
            flipWay = nbttagcompound.getInt("jb.flipway");
            if (flipWay == 0) {
               flipWay = 1;
            } else if (flipWay == 1) {
               flipWay = 9;
            } else if (flipWay == 9) {
               flipWay = 0;
            }
         }

         nbttagcompound.putInt("jb.flipway", flipWay);
         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   public static boolean isMirrorItemLeftRight(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBMirrorTool && getFlipWayFormStack(itemStack) == 0;
   }

   public static boolean isMirrorItemUpDown(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBMirrorTool && getFlipWayFormStack(itemStack) == 1;
   }

   public static boolean isMirrorItemDisabled(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBMirrorTool && getFlipWayFormStack(itemStack) == 9;
   }

   private static int getFlipWayFormStack(ItemStack itemStack) {
      if (itemStack.hasTag()) {
         CompoundTag tag = itemStack.getTag();
         return tag.getInt("jb.flipway");
      } else {
         return 9;
      }
   }
}
