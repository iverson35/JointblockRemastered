package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractComplexKeyItemExtenderScalerHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   public int keyOrItem;
   public int seatNum;

   public String getMotionHandlerName() {
      return this.keyOrItem == 0 ? "mhndl.keyresponse.complex.name" : "mhndl.itemresponse.complex.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
      params[2] = 0;
      params[3] = 0;
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

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
         guiSettings.setParamEntryState(2, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(3, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
         guiSettings.setParamEntryState(2, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(3, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
      }

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
      EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         if (this.keyOrItem == 0) {
            return entitySeats.isRiddenbyPressingExKey(joint.getParam(0)) || entitySeats.isRiddenbyPressingExKey(joint.getParam(2));
         } else {
            return entitySeats.isRiddenbyUsedItem(joint.getParam(0)) || entitySeats.isRiddenbyUsedItem(joint.getParam(2));
         }
      } else {
         return false;
      }
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         int min = joint.getParam(4);
         int max = joint.getParam(5);
         int minmaxDiff = max - min;
         if (minmaxDiff <= 0) {
            joint.setNewExtendRate(min);
         } else {
            int nowValue = joint.getExtendRate();
            nowValue = this.addExtendForKeyItem(0, nowValue, joint, entitySeats, min, max);
            nowValue = this.addExtendForKeyItem(2, nowValue, joint, entitySeats, min, max);
            joint.setNewExtendRate(nowValue);
         }
      }
   }

   protected int addExtendForKeyItem(int keyItemNum, int nowValue, IJBJointParameterHolder.IJBJoint joint, EntitySeatPart entitySeats, int min, int max) {
      int amount;
      if (this.keyOrItem == 0) {
         if (entitySeats.isRiddenbyPressingExKey(joint.getParam(keyItemNum))) {
            amount = joint.getParam(keyItemNum + 1);
            nowValue += amount;
            if (nowValue < min) {
               nowValue = min;
            }

            if (nowValue > max) {
               nowValue = max;
            }
         }

         return nowValue;
      } else {
         if (entitySeats.isRiddenbyUsedItemList[joint.getParam(keyItemNum)]) {
            amount = joint.getParam(keyItemNum + 1);
            nowValue += amount;
            if (nowValue < min) {
               nowValue = min;
            }

            if (nowValue > max) {
               nowValue = max;
            }
         }

         return nowValue;
      }
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
   }

   public boolean isIgnoreRotationPrevFix() {
      return false;
   }
}
