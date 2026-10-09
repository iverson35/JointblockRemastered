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

public class SlavePurgeKeyItemBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   protected static final int KEY_ITEM_PARAM_NUM = 0;
   protected static final int FUNCTION_PARAM_NUM = 1;
   protected static final int PURGE_TICK_PARAM_NUM = 2;
   protected static final int TE_SHUT_RANGE_PARAM_NUM = 3;
   protected static final int TE_SHUT_TICK_PARAM_NUM = 4;
   protected static final int FUNCTION_TOGGLE = 0;
   protected static final int FUNCTION_PURGE = 1;
   protected static final int FUNCTION_SHUTDOWN_TE = 2;
   protected static final int FUNCTION_PURGE_AND_SHUTDOWN_TE = 3;
   protected int keyOrItem;
   private int seatNum;

   public SlavePurgeKeyItemBindedHandler(int seatNum, int keyOrItem) {
      this.seatNum = seatNum;
      this.keyOrItem = keyOrItem;
   }

   public int getBindNum() {
      return this.keyOrItem == 0 ? 42 : 42;
   }

   public String getMotionHandlerName() {
      return this.keyOrItem == 0 ? "mhndl.keyresponse.attracter.name" : "mhndl.itemresponse.attracter.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
      params[2] = 40;
      params[3] = 8;
      params[4] = 20;
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

      guiSettings.setParamEntryState(1, (String)"keymot.attr.function.title", (Format)EntryFormats.attracterKeyFunctionTypeFormat, 0, 3);
      guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(2, (String)"keymot.attr.purgetick.title", (String)"jbcommon.simplenum.valueformat", 1, 200);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 0);
      guiSettings.setParamEntryState(3, (String)"keymot.attr.disterange.title", (String)"jbcommon.simplenum.valueformat", 1, 16);
      guiSettings.setParamButtonToggleAry(3, EntryFormats.defaultButtonStates, 0);
      guiSettings.setParamEntryState(4, (String)"keymot.attr.distetick.title", (String)"jbcommon.simplenum.valueformat", 1, 200);
      guiSettings.setParamButtonToggleAry(4, EntryFormats.defaultButtonStates, 0);
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

            int function = joint.getParam(1);
            int range;
            if (function == 0) {
               range = jp.initValues[0][1];
               entityJointed.setMasterSlotEnableStateFlag(range, !entityJointed.getMasterSlotEnableStateFlag(range), true);
            }

            if (function == 1 || function == 3) {
               jp.purgeAttatchedSlave(entityJointed, (short)joint.getParam(2));
            }

            if (function == 2 || function == 3) {
               range = joint.getParam(3);
               short shutTeTick = (short)joint.getParam(4);
               SlavePurgeHelper.disableTeInRange(entityJointed, matrixUtil, range, shutTeTick);
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
