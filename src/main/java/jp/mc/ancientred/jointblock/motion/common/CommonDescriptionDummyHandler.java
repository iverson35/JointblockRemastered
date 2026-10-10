package jp.mc.ancientred.jointblock.motion.common;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import net.minecraft.world.entity.LivingEntity;

public abstract class CommonDescriptionDummyHandler extends AbstractCommonMotionHandler {
   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return false;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
   }
}
