package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class CommonNeckRotatorHandler extends AbstractCommonMotionHandler {
   private static final int SEAT_PARAM_NUM = 0;
   private static final int CANCEL_PARENT_ROT_PARAM_NUM = 1;

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 1;
      params[1] = 1;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"basicmot.seat.seatnum.title", (Format)EntryFormats.seatNumValueFormat, 1, 4);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.oneOnlyButtonStates, 0);
      guiSettings.setParamEntryState(1, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
      if (initValues[3] != 1 && joint.getParam(1) == 1) {
         joint.setResetParentRotation();
      }

   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntityJointed targetEJ = (EntityJointed)entityLivingBase;
      Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
      if (targetEJOrMaster instanceof EntityJointed) {
         targetEJ = (EntityJointed)targetEJOrMaster;
         Player entityPlayer = targetEJ.getRidingPlayer(joint.getParam(0) - 1);
         if (entityPlayer == null) {
            return false;
         }
      } else if (targetEJOrMaster instanceof Player) {
         return true;
      }

      return true;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntityJointed targetEJ = (EntityJointed)entityLivingBase;
      Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
      Player entityPlayer;
      if (targetEJOrMaster instanceof EntityJointed) {
         targetEJ = (EntityJointed)targetEJOrMaster;
         entityPlayer = targetEJ.getRidingPlayer(joint.getParam(0) - 1);
         if (entityPlayer == null) {
            return;
         }

         if (!targetEJ.isMasterTeConnected() && !targetEJ.isMasterConnected() && joint.getParam(0) - 1 == 0) {
            joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
            joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot));
         } else {
            joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
            joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.yHeadRot - targetEJ.getYRot()));
         }
      } else if (targetEJOrMaster instanceof Player) {
         entityPlayer = (Player)targetEJOrMaster;
         joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
         joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot));
      }

   }
}
