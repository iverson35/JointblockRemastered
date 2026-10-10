package jp.mc.ancientred.jointblock.motion;

import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ring.ActiveRingSlots;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.motion.common.AbstractCommonMotionHandler;
import net.minecraft.world.entity.LivingEntity;

public class RingChannelMotionBinder extends AbstractCommonMotionHandler implements IJBMotionBinderItem.IJBMotionBinder, IJBMotionBinderItem.IJBMotionHandler {
   private static final int[] SINGLE_SELF_BIND_LIST = new int[]{0};
   protected static final int KEYNUM_PARAM_NUM = 0;

   public String getBinderName() {
      return "mbinder.ringchannel.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      return this;
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return SINGLE_SELF_BIND_LIST;
   }

   public int getBindNum() {
      return 0;
   }

   public String getMotionHandlerName() {
      return "mhndl.ringchannel.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 0;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"jbring.cmn.channel.title", (String)"jbcommon.simplenum.valueformat", 0, 127);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntityJointed ej = (EntityJointed)entityLivingBase;
      ActiveRingSlots activeRingSlots = ej.activeRingSlots;
      if (activeRingSlots == null) {
         return false;
      } else {
         short slotKey = (short)joint.getParam(0);
         ActiveRingSlots.ActiveSlot slotMot = activeRingSlots.getSlotByType(ActiveRingSlots.SlotType.motion, slotKey);
         return slotMot != null && slotMot.activeNode != null && slotMot.activeNode.remainTick + 1 > 0;
      }
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntityJointed ej = (EntityJointed)entityLivingBase;
      ActiveRingSlots activeRingSlots = ej.activeRingSlots;
      if (activeRingSlots != null) {
         short slotKey = (short)joint.getParam(0);
         ActiveRingSlots.ActiveSlot slot = activeRingSlots.getSlotByType(ActiveRingSlots.SlotType.motion, slotKey);
         if (slot != null && slot.activeNode != null) {
            slot.activeNode.doActiveChannelWork(ej, (JointParts)joint, slot);
         }
      }

   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      EntityJointed ej = (EntityJointed)entityLivingBase;
      ActiveRingSlots activeRingSlots = ej.activeRingSlots;
      if (activeRingSlots != null) {
         short slotKey = (short)joint.getParam(0);
         ActiveRingSlots.ActiveSlot slot = activeRingSlots.getSlotByType(ActiveRingSlots.SlotType.action, slotKey);
         if (slot != null && slot.activeNode != null) {
            slot.activeNode.doActiveChannelWork(ej, (JointParts)joint, slot);
         }
      }

   }
}
