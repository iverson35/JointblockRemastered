package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.fx.EntityWhiteSmokeFx;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionThruster extends AbstractSoundAction {
   private static final int EXVALNUM_WORKER_ID = 0;
   public double moveSpeed = 0.05;
   public double speedLimit = 1.0;
   protected int shootSide = 0;
   public float useEnergyMultA = 0.006F;
   public float useEnergyMultB = 0.003F;
   public float useEnergyMultC = 0.012F;

   public String getActionName() {
      return "jbaction.thruster.name";
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (world.isClientSide) {
         Entity target = ((IJBEntityState)entityLivingBase).getMasterEntityIfConnected();
         if (target instanceof Player) {
            target.fallDistance = 0.0F;
            this.addMotionThruster(matrixUtil, entityLivingBase, target);
         }

         this.spawnSmokeEffectAtPosition(world, joint, matrixUtil);
         if (this.sound != null || this.soundNameLoc != null) {
            Vec3 soundPos = matrixUtil.transformAbsolute(JBCommonUtil.createVectorHelper(0.0, 1.0, 0.0));
            ((JointParts)joint).updateEffectPosition(soundPos);
            JBModContainer.proxy.playPartsSoundEvent((JointParts)joint, (EntityJointed)entityLivingBase, this);
         }

      } else {
         if (!joint.hasInitExtraActionVars()) {
            joint.initExtraActionVars(0, 0, -1, Integer.MAX_VALUE);
            joint.setExtraActionVars(0, -1);
         }

         IJBEntityState entityState = (IJBEntityState)entityLivingBase;
         float energyToUse;
         if (entityState.isMasterEntityConnected()) {
            energyToUse = entityState.getEntityMaxEnergy() * this.useEnergyMultC;
         } else if (entityState.getUnitId() == 0) {
            energyToUse = entityState.getEntityMaxEnergy() * this.useEnergyMultB;
         } else {
            energyToUse = entityState.getEntityMaxEnergy() * this.useEnergyMultA;
         }

         if (((IJBEntityState)entityLivingBase).useEntityEnergy(energyToUse)) {
            Entity target = ((IJBEntityState)entityLivingBase).getMasterEntityIfConnected();
            if (target instanceof Player) {
               target.fallDistance = 0.0F;
            } else {
               this.addMotionThruster(matrixUtil, entityLivingBase, target);
            }

            ((IJBEntityState)entityLivingBase).notifyClientAction(joint);
         }
      }
   }

   private void addMotionThruster(IJBVectorTransformer matrixUtil, LivingEntity entityLivingBase, Entity target) {
      EnumDirectionOld dir = EnumDirectionOld.VALID_DIRECTIONS[this.shootSide];
      int xDirection = dir.offsetX;
      int yDirection = dir.offsetY;
      int zDirection = dir.offsetZ;
      double bonusValue = 1.0;
      if (entityLivingBase instanceof EntityJointed) {
         bonusValue = (double)((EntityJointed)entityLivingBase).unitAbility.thrusterPower;
      }

      Vec3 dirOut = JBCommonUtil.createVectorHelper((double)xDirection, (double)(-yDirection), (double)(-zDirection));
      dirOut = matrixUtil.transformOnlyRotation(dirOut);
      dirOut = JBCommonUtil.normalizeVec3Subtle(dirOut);
      double moveSpeedPlus = this.moveSpeed * bonusValue;
      double vectorX = dirOut.x * moveSpeedPlus;
      double vectorY = dirOut.y * moveSpeedPlus;
      double vectorZ = dirOut.z * moveSpeedPlus;
      Vec3 targetDelta = target.getDeltaMovement();
      double addedMotX = targetDelta.x + vectorX;
      double addedMotY = targetDelta.y + vectorY;
      double addedMotZ = targetDelta.z + vectorZ;
      double addedSpeed = Math.sqrt(addedMotX * addedMotX + addedMotY * addedMotY + addedMotZ * addedMotZ);
      if (!Double.isInfinite(addedSpeed) && !Double.isNaN(addedSpeed)) {
         double div = 1.0;
         if (addedSpeed > this.speedLimit) {
            div += (addedSpeed - this.speedLimit) * 3.0;
         }

         target.setDeltaMovement(targetDelta.x + vectorX / div, targetDelta.y + vectorY / div, targetDelta.z + vectorZ / div);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnSmokeEffectAtPosition(Level world, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      EnumDirectionOld dir = EnumDirectionOld.VALID_DIRECTIONS[EnumDirectionOld.OPPOSITES[this.shootSide]];
      Vec3 posOffTop = JBCommonUtil.createVectorHelper(0.0 + (double)dir.offsetX * 0.5, 1.0 + (double)dir.offsetY * -0.5, 0.0 + (double)dir.offsetZ * -0.5);
      posOffTop = matrixUtil.transformAbsolute(posOffTop);

      for(int i = 0; i < 4; ++i) {
         Minecraft.getInstance().particleEngine.add(new EntityWhiteSmokeFx(world, posOffTop.x, posOffTop.y, posOffTop.z));
      }

   }
}
