package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.HumanoidUpperMotionBinder;
import net.minecraft.world.item.ItemStack;

public class ItemHumanUpperMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final HumanoidUpperMotionBinder humanUpperMotionBinder = new HumanoidUpperMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.humanUpperMotionBinder;
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 15645593;
   }
}
