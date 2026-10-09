package jp.mc.ancientred.jointblock.motion;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.motion.common.AbstractCommonMotionHandler;
import net.minecraft.world.entity.LivingEntity;

public class MachineMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int BIND_TYPE_WHEEL = 10;
   public static final int BIND_TYPE_ROTOR = 20;
   public static final int BIND_TYPE_HOVER = 30;
   public static final int BIND_TYPE_STABILIZER = 40;
   public static final int BIND_TYPE_SCREW = 50;
   private static final int[] machineMotionBindList = new int[]{10, 20, 30, 40, 50};
   private static final AbstractCommonMotionHandler wheelMotionHandler = new AbstractCommonMotionHandler() {
      protected static final int DO_YROT_PARAM_NUM = 0;
      protected static final int LIMIT_ROT_PARAM_NUM = 1;
      protected static final int LIMIT_ROT_RANGE_PARAM_NUM = 2;
      protected static final int CANCEL_PARENT_ROT_PARAM_NUM = 3;

      public int getBindNum() {
         return 10;
      }

      public String getMotionHandlerName() {
         return "mhndl.machine.wheel.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 1;
         params[1] = 1;
         params[2] = 45;
         params[3] = 1;
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (initValues[3] != 1 && joint.getParam(3) == 1) {
            joint.setResetParentRotation();
         }

      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"mechmot.rotytoway.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"cmnmot.limitrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"cmnmot.limitrotrange.title", (String)"cmnmot.limitrotrange.valueformat", 0, 90);
         guiSettings.setParamEntryState(3, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            return true;
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            double d2 = entityLivingBase.getY() - entityLivingBase.yo;
            float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            return (double)distMoved > 0.1;
         }
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            ((JointParts)joint).setToIninitalValue();
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            float distMovedXZ = (float)Math.sqrt(d0 * d0 + d1 * d1);
            double yaw = -90.0 + Math.atan2(d1, d0) * 180.0 / Math.PI;
            float yRot = (float)yaw - entityLivingBase.getYRot() % 360.0F;
            if (yRot < -180.0F) {
               yRot += 360.0F;
            }

            if (yRot > 180.0F) {
               yRot -= 360.0F;
            }

            float spin = distMovedXZ * 90.0F;
            if (yRot < -90.0F || yRot > 90.0F) {
               spin = -spin;
            }

            joint.setNewRotationX(joint.getRotationX() + (int)spin);
            if (joint.getParam(0) != 0) {
               if (yRot < -90.0F) {
                  yRot += 180.0F;
               } else if (yRot > 90.0F) {
                  yRot -= 180.0F;
               }

               if (joint.getParam(1) == 1) {
                  int limit = joint.getParam(2);
                  if (yRot < (float)(-limit)) {
                     yRot = (float)(-limit);
                  }

                  if (yRot > (float)limit) {
                     yRot = (float)limit;
                  }
               }

               joint.setNewRotationY(joint.getInitValue(1) + (int)yRot);
            }
         }
      }
   };
   private static final AbstractCommonMotionHandler rotorMotionHandler = new AbstractCommonMotionHandler() {
      protected static final int REVROT_PARAM_NUM = 0;
      protected static final int ROTAXIS_PARAM_NUM = 1;
      protected static final int CANCEL_PARENT_ROT_PARAM_NUM = 2;

      public int getBindNum() {
         return 20;
      }

      public String getMotionHandlerName() {
         return "mhndl.machine.rotor.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 1;
         params[1] = 1;
         params[2] = 1;
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (initValues[3] != 1 && joint.getParam(2) == 1) {
            joint.setResetParentRotation();
         }

      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"mechmot.reverserot.title", (Format)EntryFormats.commonYRotValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"mechmot.rotaxis.title", (Format)EntryFormats.commonRotAxisValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         return (double)((EntityJointed)entityLivingBase).spinningRate > 0.0;
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         float spinRate = ((EntityJointed)entityLivingBase).spinningRate * 60.0F;
         if (joint.getParam(0) == 1) {
            spinRate *= -1.0F;
         }

         int rotAxis = joint.getParam(1);
         if (rotAxis == 0) {
            joint.setNewRotationX(joint.getInitValue(0) + joint.getRotationX() + (int)spinRate);
         } else if (rotAxis == 1) {
            joint.setNewRotationY(joint.getInitValue(1) + joint.getRotationY() + (int)spinRate);
         } else {
            joint.setNewRotationZ(joint.getInitValue(2) + joint.getRotationZ() + (int)spinRate);
         }

      }
   };
   private static final AbstractCommonMotionHandler hoverMotionHandler = new AbstractCommonMotionHandler() {
      protected static final int DO_YROT_PARAM_NUM = 0;
      protected static final int CANCEL_PARENT_ROT_PARAM_NUM = 1;

      public int getBindNum() {
         return 30;
      }

      public String getMotionHandlerName() {
         return "mhndl.machine.hover.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 1;
         params[1] = 1;
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"mechmot.rotytoway.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         joint.setRotatePoint(1);
         if (initValues[3] != 1 && joint.getParam(1) == 1) {
            joint.setResetParentRotation();
         }

      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         return true;
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            ((JointParts)joint).setToIninitalValue();
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            float distMovedXZ = (float)Math.sqrt(d0 * d0 + d1 * d1);
            double yaw = -90.0 + Math.atan2(d1, d0) * 180.0 / Math.PI;
            float yRot = (float)yaw - entityLivingBase.getYRot() % 360.0F;
            if (yRot < -180.0F) {
               yRot += 360.0F;
            }

            if (yRot > 180.0F) {
               yRot -= 360.0F;
            }

            joint.setNewRotationX((int)(60.0F * entityLivingBase.walkAnimation.speed()));
            if (joint.getParam(0) != 0) {
               if (distMovedXZ > 0.1F) {
                  joint.setNewRotationY(joint.getInitValue(1) + (int)yRot);
               }

            }
         }
      }
   };
   private static final AbstractCommonMotionHandler stabilizerMotionHandler = new AbstractCommonMotionHandler() {
      protected static final int CANCEL_PARENT_ROT_PARAM_NUM = 0;

      public int getBindNum() {
         return 40;
      }

      public String getMotionHandlerName() {
         return "mhndl.machine.stabilizer.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 1;
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (initValues[3] != 1 && joint.getParam(0) == 1) {
            joint.setResetParentRotation();
         }

      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         return false;
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      }
   };
   private static final AbstractCommonMotionHandler screwMotionHandler = new AbstractCommonMotionHandler() {
      protected static final int SCREWROT_AMOUNT_PARAM_NUM = 0;
      protected static final int ROTAXIS_PARAM_NUM = 1;
      protected static final int CANCEL_PARENT_ROT_PARAM_NUM = 2;

      public int getBindNum() {
         return 20;
      }

      public String getMotionHandlerName() {
         return "mhndl.machine.screw.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 60;
         params[1] = 2;
         params[2] = 1;
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (initValues[3] != 1 && joint.getParam(2) == 1) {
            joint.setResetParentRotation();
         }

      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"mechmot.rotamount.title", (String)"jbcommon.angledegreese.format", -359, 359);
         guiSettings.setParamEntryState(1, (String)"mechmot.rotaxis.title", (Format)EntryFormats.commonRotAxisValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            return true;
         } else if (!entityLivingBase.isInWater()) {
            return false;
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            double d2 = entityLivingBase.getY() - entityLivingBase.yo;
            float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            return (double)distMoved > 0.1;
         }
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            ((JointParts)joint).setToIninitalValue();
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            double d2 = entityLivingBase.getY() - entityLivingBase.yo;
            float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            float spinRate = distMoved / 1.0F;
            if (spinRate > 1.0F) {
               spinRate = 1.0F;
            }

            int rotAxis = joint.getParam(1);
            int rotAmount = joint.getParam(0);
            if (rotAxis == 0) {
               joint.setNewRotationX(joint.getRotationX() + (int)((float)rotAmount * spinRate));
            } else if (rotAxis == 1) {
               joint.setNewRotationY(joint.getRotationY() + (int)((float)rotAmount * spinRate));
            } else {
               joint.setNewRotationZ(joint.getRotationZ() + (int)((float)rotAmount * spinRate));
            }

         }
      }
   };

   public String getBinderName() {
      return "mbinder.machine.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 10:
            return wheelMotionHandler;
         case 20:
            return rotorMotionHandler;
         case 30:
            return hoverMotionHandler;
         case 40:
            return stabilizerMotionHandler;
         case 50:
            return screwMotionHandler;
         default:
            return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return jointType == 8 ? machineMotionBindList : null;
   }
}
