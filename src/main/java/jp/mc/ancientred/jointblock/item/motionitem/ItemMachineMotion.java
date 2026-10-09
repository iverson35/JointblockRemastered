package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.MachineMotionBinder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMachineMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final MachineMotionBinder machineBinder = new MachineMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.machineBinder;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 11184810;
   }
}
