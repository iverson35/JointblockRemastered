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

public class SlavePurgeKeyItemComplexHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   protected static final int KEY_ITEM_1_PARAM_NUM = 0;
   protected static final int KEY_ITEM_2_PARAM_NUM = 1;
   protected int keyOrItem;
   private int seatNum;

   public SlavePurgeKeyItemComplexHandler(int seatNum, int keyOrItem) {
      this.seatNum = seatNum;
      this.keyOrItem = keyOrItem;
   }

   public int getBindNum() {
      return this.keyOrItem == 0 ? 43 : 43;
   }

   public String getMotionHandlerName() {
      return this.keyOrItem == 0 ? "mhndl.keyresponse.complex.name" : "mhndl.itemresponse.complex.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.keyenable.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.actenable.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(1, (String)"keymot.keydisable.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      } else {
         guiSettings.setParamEntryState(1, (String)"itemmot.actdisable.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
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
            boolean keyOnePressed = false;
            boolean keyTwoPressed = false;
            if (this.keyOrItem == 0) {
               if (entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  keyOnePressed = true;
               }

               if (entitySeats.isRiddenbyPressingExKey(joint.getParam(1))) {
                  keyTwoPressed = true;
               }
            } else {
               if (entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
                  keyOnePressed = true;
               }

               if (entitySeats.isRiddenbyUsedItem(joint.getParam(1))) {
                  keyTwoPressed = true;
               }
            }

            int slotNum = jp.initValues[0][1];
            if (keyOnePressed && !entityJointed.getMasterSlotEnableStateFlag(slotNum)) {
               entityJointed.setMasterSlotEnableStateFlag(slotNum, true, true);
            }

            if (keyTwoPressed && entityJointed.getMasterSlotEnableStateFlag(slotNum)) {
               entityJointed.setMasterSlotEnableStateFlag(slotNum, false, true);
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
