package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemJBEffect extends Item {
   public static final int TYPE_SOUND = 0;
   public static final int TYPE_PARTICLE = 1;

   public ItemJBEffect() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public Component getName(ItemStack stack) {
      String soundNameStr = getSoundNameStrFromStackNBT(stack);
      return Component.literal(soundNameStr);
   }

   public static String getSoundNameStrFromStackNBT(ItemStack stack) {
      CompoundTag tag;
      return !CompatHelper.isStackEmptyOrNull(stack) && (tag = stack.getTag()) != null && tag.contains("effectNameStr", 8) ? tag.getString("effectNameStr") : "";
   }
}
