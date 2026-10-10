package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.LivingEntity;

public abstract class CommonWaistRotatorHandler extends AbstractCommonMotionHandler {
   protected static final int LIMIT_ROT_PARAM_NUM = 0;
   protected static final int LIMIT_ROT_RANGE_PARAM_NUM = 1;
   protected static final int LIMIT_ROT_FIX_PARAM_NUM = 2;

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 1;
      params[1] = 110;
      params[2] = 1;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"cmnmot.limitrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(1, (String)"cmnmot.limitrotrange.title", (String)"cmnmot.limitrotrange.valueformat", 0, 170);
      guiSettings.setParamEntryState(2, (String)"cmnmot.limitfix.title", (Format)EntryFormats.limitRotFixValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
         return true;
      } else if (joint.getParam(0) == 1 && joint.getParam(1) == 0) {
         return false;
      } else {
         double d0 = entityLivingBase.getX() - entityLivingBase.xo;
         double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
         float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1);
         return (double)distMoved > 0.1;
      }
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
         ((JointParts)joint).setToIninitalValue();
      } else {
         double d0 = entityLivingBase.getX() - entityLivingBase.xo;
         double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
         double yaw = -90.0 + Math.atan2(d1, d0) * 180.0 / Math.PI;
         float yRot = (float)yaw - entityLivingBase.getYRot() % 360.0F;
         if (yRot < -180.0F) {
            yRot += 360.0F;
         }

         if (yRot > 180.0F) {
            yRot -= 360.0F;
         }

         if (joint.getParam(0) == 1) {
            int limit = joint.getParam(1);
            if (yRot < (float)(-limit)) {
               yRot = joint.getParam(2) == 0 ? (float)(-limit) : 0.0F;
            }

            if (yRot > (float)limit) {
               yRot = joint.getParam(2) == 0 ? (float)limit : 0.0F;
            }
         }

         joint.setNewRotationY(joint.getInitValue(1) + (int)yRot);
      }
   }
}
