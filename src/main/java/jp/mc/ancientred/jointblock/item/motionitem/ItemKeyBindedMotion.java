package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.KeyMotionBinder;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemKeyBindedMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private final KeyMotionBinder keyMotionBinder = new KeyMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.keyMotionBinder;
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
      // 1.12.2 appended ".name" to the unlocalized name ("item.keyresmotionitem" + ".name");
      // in 1.20.1 the description id already is the lang key (item.jointblock.keyresmotionitem).
      return Component.translatable(this.getDescriptionId()).append(Component.translatable(KeyMotionBinder.seatNames[getSubtype(stack)]));
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return DEFAULT.getColorFromItemStack(itemStack, pass);
   }
}
