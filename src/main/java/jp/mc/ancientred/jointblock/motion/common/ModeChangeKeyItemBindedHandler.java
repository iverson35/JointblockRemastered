package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public class ModeChangeKeyItemBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   protected static final int KEY_ITEM_PARAM_NUM = 0;
   protected static final int FROM_MODE_PARAM_NUM = 1;
   protected static final int TO_MODE_PARAM_NUM = 2;
   protected static final int TOGGLE_MAIN_SUBTYPE = 3;
   public static final int TARGET_MODE_INIT = 0;
   public static final int TARGET_MODE_SLAVE = 1;
   protected int target;
   protected int keyOrItem;
   private int seatNum;

   public ModeChangeKeyItemBindedHandler(int seatNum, int target, int keyOrItem) {
      this.seatNum = seatNum;
      this.target = target;
      this.keyOrItem = keyOrItem;
   }

   public int getBindNum() {
      if (this.target == 0) {
         return this.keyOrItem == 0 ? 44 : 44;
      } else {
         return this.keyOrItem == 0 ? 45 : 45;
      }
   }

   public String getMotionHandlerName() {
      if (this.target == 0) {
         return this.keyOrItem == 0 ? "mhndl.keyresponse.initmodechange.name" : "mhndl.itemresponse.initmodechange.name";
      } else {
         return this.keyOrItem == 0 ? "mhndl.keyresponse.slavemodechange.name" : "mhndl.itemresponse.slavemodechange.name";
      }
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
      params[2] = 0;
      params[3] = 0;
   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      byte modeMin;
      byte modeMAX;
      if (this.target == 0) {
         modeMin = 0;
         modeMAX = 127;
      } else {
         modeMin = -1;
         modeMAX = 10;
      }

      guiSettings.setParamEntryState(1, (String)"keymot.modefrom.title", (Format)EntryFormats.modeChangeValueFormat, modeMin, modeMAX);
      guiSettings.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      guiSettings.setParamEntryState(2, (String)"keymot.modeto.title", (Format)EntryFormats.modeChangeValueFormat, modeMin, modeMAX);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 1);
      if (this.target == 0) {
         guiSettings.setParamEntryState(3, (String)"keymot.toggletype.title", (Format)EntryFormats.mainSubTypeValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
      }

   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return false;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      EntitySeatPart entitySeats = entityJointed.getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         if (joint instanceof JointParts) {
            JointParts jp = (JointParts)joint;
            if (this.keyOrItem == 0) {
               if (!entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  return;
               }
            } else if (!entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
               return;
            }

            int fromMode = joint.getParam(1);
            int toMode = joint.getParam(2);
            if (fromMode != toMode) {
               if (this.target == 0) {
                  if (entityJointed.isSlaveMode()) {
                     return;
                  }

                  if (entityJointed.getInitLayerMode() != fromMode) {
                     return;
                  }

                  if (entityJointed.getInitLayerMode() == toMode) {
                     return;
                  }

                  entityJointed.starReInit((byte)toMode, joint.getParam(3));
               } else if (fromMode < 0) {
                  if (entityJointed.isSlaveMode()) {
                     return;
                  }

                  entityJointed.setSlaveMode(true, toMode);
               } else if (toMode < 0) {
                  if (!entityJointed.isSlaveMode()) {
                     return;
                  }

                  if (entityJointed.getSlaveSlotNum() != fromMode) {
                     return;
                  }

                  entityJointed.setSlaveMode(false, 0);
               } else {
                  if (!entityJointed.isSlaveMode()) {
                     return;
                  }

                  if (entityJointed.getSlaveSlotNum() != fromMode) {
                     return;
                  }

                  if (entityJointed.getSlaveSlotNum() == toMode) {
                     return;
                  }

                  entityJointed.setSlaveMode(true, toMode);
               }

            }
         }
      }
   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
   }

   public boolean isIgnoreRotationPrevFix() {
      return false;
   }
}
