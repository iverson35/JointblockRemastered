package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractSpringKeyItemExtenderScalerHandler extends AbstractComplexKeyItemExtenderScalerHandler {
   public String getMotionHandlerName() {
      return this.keyOrItem == 0 ? "mhndl.keyresponse.spring.name" : "mhndl.itemresponse.spring.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
      params[2] = 0;
      params[3] = 10;
      switch (jointType) {
         case 10:
         default:
            params[4] = 100;
            params[5] = 200;
            break;
         case 12:
            params[4] = 0;
            params[5] = 100;
      }

   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
      }

      guiSettings.setParamEntryState(2, (String)"cmnmot.springbacktype.title", (Format)EntryFormats.springBackTypeFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(3, (String)"cmnmot.springbackamount.title", (String)"jbcommon.simplenum.valueformat", 0, 100);
      switch (jointType) {
         case 10:
         default:
            guiSettings.setParamEntryState(4, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
            guiSettings.setParamEntryState(5, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
            break;
         case 12:
            guiSettings.setParamEntryState(4, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
            guiSettings.setParamEntryState(5, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
      }

   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return true;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         if (this.keyOrItem == 0) {
            if (entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
               super.doUpdateJointState(entityLivingBase, joint);
               return;
            }
         } else if (entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
            super.doUpdateJointState(entityLivingBase, joint);
            return;
         }

         JointParts jp = (JointParts)joint;
         int backAmount = jp.getParam(3);
         if (backAmount > 0) {
            int nowValue;
            int diff;
            int fixValue;
            if (jp.getParam(2) == 0) {
               nowValue = jp.getExtendRate();
               if (jp.fixedInitVals[0] != nowValue) {
                  diff = jp.fixedInitVals[0] - nowValue;
                  if (diff > 0) {
                     if (backAmount >= diff) {
                        fixValue = diff;
                     } else {
                        fixValue = backAmount;
                     }
                  } else if (backAmount >= -diff) {
                     fixValue = diff;
                  } else {
                     fixValue = -backAmount;
                  }

                  jp.setNewExtendRate(nowValue + fixValue);
               }
            } else {
               if (backAmount > 100) {
                  backAmount = 100;
               }

               float backRateF = (float)backAmount / 100.0F;
               nowValue = jp.getExtendRate();
               if (jp.fixedInitVals[0] != nowValue) {
                  diff = jp.fixedInitVals[0] - nowValue;
                  fixValue = Math.round((float)diff * backRateF);
                  if (fixValue == 0) {
                     fixValue = diff > 0 ? 1 : -1;
                  }

                  jp.setNewExtendRate(nowValue + fixValue);
               }
            }

         }
      }
   }

   protected int addExtendForKeyItem(int keyActNum, int nowValue, IJBJointParameterHolder.IJBJoint joint, EntitySeatPart entitySeats, int min, int max) {
      return keyActNum != 0 ? nowValue : super.addExtendForKeyItem(keyActNum, nowValue, joint, entitySeats, min, max);
   }
}
