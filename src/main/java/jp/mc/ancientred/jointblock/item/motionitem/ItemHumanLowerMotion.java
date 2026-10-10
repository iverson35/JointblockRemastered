package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.HumanoidLowerMotionBinder;
import net.minecraft.world.item.ItemStack;

public class ItemHumanLowerMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final HumanoidLowerMotionBinder humanLowerMotionBinder = new HumanoidLowerMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.humanLowerMotionBinder;
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 15645593;
   }
}
