package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.ItemMotionBinder;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemItemBindedMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private final ItemMotionBinder itemMotionBinder = new ItemMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.itemMotionBinder;
   }

   /**
    * 1.12 damage-value subtype (seat index), now stored as int NBT tag "subtype"
    * (flattening to separate items is a Phase 6 decision).
    */
   public static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt("subtype") : 0;
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      stack.getOrCreateTag().putInt("subtype", subtype);
      return stack;
   }

   @Override
   public Component getName(ItemStack stack) {
      return Component.translatable(this.getDescriptionId() + ".name").append(Component.translatable(ItemMotionBinder.seatNames[getSubtype(stack)]));
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 10062079;
   }
}
