package jp.mc.ancientred.jointblock.motion.common;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import net.minecraft.world.entity.Entity;

public class PendulumHelper {
   public static final int AXIS_X = 0;
   public static final int AXIS_Y = 1;
   public static final int AXIS_Z = 2;
   public static final int MODIFY_PADDING = 10000;

   public static void handleExtenderMotion(Entity entity, IJBJointParameterHolder.IJBJoint joint, int amount, int min, int max) {
      if (amount != 0) {
         int minmaxDiff = max - min;
         if (minmaxDiff <= 0) {
            joint.setNewExtendRate(min);
         } else {
            int worldRange = minmaxDiff * 2;
            int value = joint.getExtendRate();
            if (value >= min) {
               value -= min;
            } else if (value <= -min) {
               value += min;
            } else {
               value = 0;
            }

            value += minmaxDiff + amount;
            value %= worldRange;
            if (value < 0) {
               value += worldRange;
            }

            if (value >= minmaxDiff) {
               joint.setNewExtendRate(value - minmaxDiff + min);
            } else {
               joint.setNewExtendRate(value - max);
            }

         }
      }
   }

   public static void simplePendulumMotion(IJBJointParameterHolder.IJBJoint joint, int amount, int min, int max, int axis) {
      min += 10000;
      max += 10000;
      if (amount != 0) {
         int minmaxDiff = max - min;
         if (minmaxDiff <= 0) {
            setNewFixedAmount(joint, min, axis);
         } else {
            int worldRange = minmaxDiff * 2;
            int value = getFixedAmount(joint, axis);
            if (value >= min) {
               value -= min;
            } else if (value <= -min) {
               value += min;
            } else {
               value = 0;
            }

            value += minmaxDiff + amount;
            value %= worldRange;
            if (value < 0) {
               value += worldRange;
            }

            if (value >= minmaxDiff) {
               setNewFixedAmount(joint, value - minmaxDiff + min, axis);
            } else {
               setNewFixedAmount(joint, value - max, axis);
            }

         }
      }
   }

   private static int getFixedAmount(IJBJointParameterHolder.IJBJoint joint, int axis) {
      int ret;
      switch (axis) {
         case 0:
            ret = joint.getRotationX();
            break;
         case 1:
            ret = joint.getRotationY();
            break;
         default:
            ret = joint.getRotationZ();
      }

      return (ret + 10000) * (joint.getMotionCommonFlag() ? -1 : 1);
   }

   private static void setNewFixedAmount(IJBJointParameterHolder.IJBJoint joint, int amount, int axis) {
      if (amount > 0) {
         joint.setMotionCommonFlag(false);
      } else {
         amount = -amount;
         joint.setMotionCommonFlag(true);
      }

      switch (axis) {
         case 0:
            joint.setNewRotationX(amount - 10000);
            return;
         case 1:
            joint.setNewRotationY(amount - 10000);
            return;
         default:
            joint.setNewRotationZ(amount - 10000);
      }
   }

   public static void sinePendulumMotion(IJBJointParameterHolder.IJBJoint joint, int amount, int min, int max, int axis) {
      if (amount != 0) {
         int minmaxDiff = max - min;
         if (minmaxDiff <= 0) {
            setNewFixedAmount(joint, min, axis);
         } else {
            int nowExtendRate = joint.getExtendRate();
            int initialValue;
            if (!joint.getMotionCommonFlag()) {
               initialValue = getSimpleAmount(joint, axis);
               if (initialValue > max) {
                  initialValue = max;
               } else if (initialValue < min) {
                  initialValue = min;
               }

               double rad = Math.asin((double)((float)(initialValue - min) / (float)minmaxDiff * 2.0F - 1.0F));
               nowExtendRate = Math.round((float)(rad * 180.0 / Math.PI));
               joint.setMotionCommonFlag(true);
            }

            initialValue = nowExtendRate + amount;
            float sin = (float)Math.sin((double)initialValue * 0.017453292519943295);
            int res = Math.round((sin + 1.0F) / 2.0F * (float)minmaxDiff) + min;
            joint.setNewExtendRate(initialValue % 360);
            setSimpleNewAmount(joint, res, axis);
         }
      }
   }

   private static int getSimpleAmount(IJBJointParameterHolder.IJBJoint joint, int axis) {
      switch (axis) {
         case 0:
            return joint.getRotationX();
         case 1:
            return joint.getRotationY();
         default:
            return joint.getRotationZ();
      }
   }

   private static void setSimpleNewAmount(IJBJointParameterHolder.IJBJoint joint, int amount, int axis) {
      switch (axis) {
         case 0:
            joint.setNewRotationX(amount);
            return;
         case 1:
            joint.setNewRotationY(amount);
            return;
         default:
            joint.setNewRotationZ(amount);
      }
   }

   private static int getSimpleInitValue(IJBJointParameterHolder.IJBJoint joint, int axis) {
      switch (axis) {
         case 0:
            return joint.getInitValue(0);
         case 1:
            return joint.getInitValue(1);
         default:
            return joint.getInitValue(2);
      }
   }
}
