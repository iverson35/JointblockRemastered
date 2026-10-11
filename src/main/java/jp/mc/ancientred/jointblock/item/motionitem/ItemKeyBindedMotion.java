package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.KeyMotionBinder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemKeyBindedMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final KeyMotionBinder keyMotionBinder = new KeyMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.keyMotionBinder;
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
