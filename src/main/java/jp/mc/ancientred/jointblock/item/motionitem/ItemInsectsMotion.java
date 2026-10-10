package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.InsectsMotionBinder;
import net.minecraft.world.item.ItemStack;

public class ItemInsectsMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final InsectsMotionBinder insectsMotionBinder = new InsectsMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.insectsMotionBinder;
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 11206570;
   }
}
