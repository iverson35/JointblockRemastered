package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public class EffectKeyItemBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   protected static final int KEY_ITEM_PARAM_NUM = 0;
   protected static final int EFFECTSLOT_PARAM_NUM = 1;
   protected int keyOrItem;
   private int seatNum;

   public EffectKeyItemBindedHandler(int seatNum, int keyOrItem) {
      this.seatNum = seatNum;
      this.keyOrItem = keyOrItem;
   }

   public int getBindNum() {
      return this.keyOrItem == 0 ? 47 : 47;
   }

   public String getMotionHandlerName() {
      return "mhndl.keyresponse.effect.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
      params[1] = 0;
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

      guiSettings.setParamEntryState(1, (String)"keymot.effectslot.title", (Format)EntryFormats.simpleDecimalFormat, 0, 15);
      guiSettings.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
   }

   public boolean isIgnoreRotationPrevFix() {
      return false;
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return false;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      if (joint instanceof JointParts) {
         JointParts jp = (JointParts)joint;
         if (!entityLivingBase.level().isClientSide) {
            this.doServerWorks(jp, entityLivingBase);
         }

      }
   }

   private void doServerWorks(JointParts joint, LivingEntity entityLivingBase) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      EntitySeatPart entitySeats = entityJointed.getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         if (this.keyOrItem == 0) {
            if (!entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
               return;
            }
         } else if (!entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
            return;
         }

         int effectSlotNum = joint.getParam(1);
         if (effectSlotNum >= 0 && effectSlotNum < entityJointed.effectSlots.length) {
            EffectSlot soundSlot = entityJointed.effectSlots[effectSlotNum];
            if (soundSlot != null) {
               entityJointed.notifyClientEffect(joint, (byte)effectSlotNum);
            }
         }

      }
   }
}
