package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ActionSound extends AbstractSoundAction {
   private static final int EXTRA_VALUE_NUM_COOLTIME = 0;
   protected int coolTimeTick = 3;

   public String getActionName() {
      return "jbaction.sound.name";
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (!world.isClientSide) {
         if (!joint.hasInitExtraActionVars()) {
            joint.initExtraActionVars(0, -1, 0, Integer.MAX_VALUE);
         }

         int extraValue = joint.getExtraActionVars(0);
         if (extraValue <= 0) {
            ((IJBEntityState)entityLivingBase).notifyClientAction(joint);
         }
      } else {
         if (this.sound != null || this.soundNameLoc != null) {
            Vec3 soundPos = matrixUtil.transformAbsolute(JBCommonUtil.createVectorHelper(0.0, 1.0, 0.0));
            ((JointParts)joint).updateEffectPosition(soundPos);
            JBModContainer.proxy.playPartsSoundEvent((JointParts)joint, (EntityJointed)entityLivingBase, this);
         }

      }
   }
}
