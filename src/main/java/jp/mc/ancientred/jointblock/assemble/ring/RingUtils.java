package jp.mc.ancientred.jointblock.assemble.ring;

public class RingUtils {
   public static short moduloRotatorValue(int value) {
      return (short)(value % 360);
   }

   public static short range180YawValue(int yawValue) {
      yawValue %= 360;
      if (yawValue > 180) {
         yawValue -= 360;
      }

      if (yawValue < -180) {
         yawValue += 360;
      }

      return (short)yawValue;
   }

   public static float range180YawValue(float yawValue) {
      yawValue %= 360.0F;
      if (yawValue > 180.0F) {
         yawValue -= 360.0F;
      }

      if (yawValue < -180.0F) {
         yawValue += 360.0F;
      }

      return yawValue;
   }
}
