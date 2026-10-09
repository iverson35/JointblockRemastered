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

public class EMountKeyItemBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final String NBT_FORGEDAT_EMOUNT = "jbemnt:n";
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   protected static final int KEY_ITEM_PARAM_NUM = 0;
   protected static final int RANGE_PARAM_NUM = 1;
   protected int keyOrItem;
   private int seatNum;

   public EMountKeyItemBindedHandler(int seatNum, int keyOrItem) {
      this.seatNum = seatNum;
      this.keyOrItem = keyOrItem;
   }

   public int getBindNum() {
      return this.keyOrItem == 0 ? 46 : 46;
   }

   public String getMotionHandlerName() {
      return this.keyOrItem == 0 ? "mhndl.keyresponse.emount.name" : "mhndl.itemresponse.emount.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 8;
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

      guiSettings.setParamEntryState(1, (String)"keymot.seat.searchrange.title", (String)"jbcommon.simplenum.valueformat", 1, 16);
      guiSettings.setParamButtonToggleAry(1, EntryFormats.sliderAmountButtonStates, 0);
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return false;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      EntitySeatPart entitySeatsForOp = entityJointed.getSeatEntity(this.seatNum);
      if (entitySeatsForOp != null && entitySeatsForOp.canKeyItemUsed()) {
         if (joint instanceof JointParts) {
            JointParts jp = (JointParts)joint;
            EntitySeatPart targetSeat = jp.getRelatedSeatPart();
            if (targetSeat != null && !targetSeat.notAsSeat && targetSeat.seatEnabled) {
               if (this.keyOrItem == 0) {
                  if (!entitySeatsForOp.isRiddenbyPressingExKey(joint.getParam(0))) {
                     return;
                  }
               } else if (!entitySeatsForOp.isRiddenbyUsedItem(joint.getParam(0))) {
                  return;
               }

               targetSeat.mountEntityWork(entityJointed, (double)joint.getParam(1), matrixUtil);
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
