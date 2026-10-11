package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.ItemMotionBinder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemItemBindedMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final ItemMotionBinder itemMotionBinder = new ItemMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.itemMotionBinder;
   }

   @Override
   public Component getName(ItemStack stack) {
      // 1.12.2 appended ".name" to the unlocalized name ("item.itemresmotionitem" + ".name");
      // in 1.20.1 the description id already is the lang key (item.jointblock.itemresmotionitem).
      return Component.translatable(this.getDescriptionId()).append(Component.translatable(ItemMotionBinder.seatNames[getSubtype(stack)]));
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 10062079;
   }
}
