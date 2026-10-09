package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.BeastLowerMotionBinder;
import net.minecraft.world.item.ItemStack;

public class ItemBeastLowerMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final BeastLowerMotionBinder beastLowerMotionBinder = new BeastLowerMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.beastLowerMotionBinder;
   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 14509602;
   }
}
