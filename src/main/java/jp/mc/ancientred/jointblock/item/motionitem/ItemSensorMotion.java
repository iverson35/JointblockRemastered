package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.SensorMotionBinder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSensorMotion extends AbstractMotionItem implements IJBMotionBinderItem {
   private final SensorMotionBinder sensorBinder = new SensorMotionBinder();

   @Override
   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      return this.sensorBinder;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      return pass == 0 ? DEFAULT.getColorFromItemStack(itemStack, pass) : 16776960;
   }
}
