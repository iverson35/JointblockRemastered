package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ActionFlamer implements IJBModelItem.IJBActionHandler {
   private static final int EXVALNUM_WORKER_ID = 0;
   private static final double FLAME_LENGTH = 8.0;
   protected float damage = 5.0F;
   public float useEnergyMultA = 0.0025F;
   public float useEnergyMultB = 0.0012F;
   public float useEnergyHit = 3.0F;
   public boolean setBlockFire = true;
   public int effectColor = -2130758861;
   public boolean setMobFire = true;
   public int setMobPotionEffect = -1;
   public int potionAmplifier = 1;
   public int effectDuration = 5;
   private Vec3 posCenter = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private Vec3 posOut = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);

   public String getActionName() {
      return "jbaction.flamer.name";
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (entityConducted != null) {
         if (!joint.hasInitExtraActionVars()) {
            joint.initExtraActionVars(0, 0, -1, Integer.MAX_VALUE);
            joint.setExtraActionVars(0, -1);
         }

         int unitType = ((IJBEntityState)entityLivingBase).getUnitId();
         float energyToUse;
         if (unitType != 1 && unitType != 5) {
            energyToUse = ((IJBEntityState)entityLivingBase).getEntityMaxEnergy() * this.useEnergyMultA;
         } else {
            energyToUse = ((IJBEntityState)entityLivingBase).getEntityMaxEnergy() * this.useEnergyMultB;
         }

         if (((IJBEntityState)entityLivingBase).useEntityEnergy(energyToUse)) {
            this.posCenter = new Vec3(0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[1].offsetX * 0.5, this.posCenter.y, this.posCenter.z);
            this.posCenter = new Vec3(this.posCenter.x, 1.0 + (double)(-EnumDirectionOld.VALID_DIRECTIONS[1].offsetY) * 0.5, this.posCenter.z);
            this.posCenter = new Vec3(this.posCenter.x, this.posCenter.y, 0.0 + (double)(-EnumDirectionOld.VALID_DIRECTIONS[1].offsetZ) * 0.5);
            this.posCenter = matrixUtil.transformAbsolute(this.posCenter);
            this.posOut = new Vec3(0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[1].offsetX * 8.0, this.posOut.y, this.posOut.z);
            this.posOut = new Vec3(this.posOut.x, 1.0 + (double)(-EnumDirectionOld.VALID_DIRECTIONS[1].offsetY) * 8.0, this.posOut.z);
            this.posOut = new Vec3(this.posOut.x, this.posOut.y, 0.0 + (double)(-EnumDirectionOld.VALID_DIRECTIONS[1].offsetZ) * 8.0);
            this.posOut = matrixUtil.transformAbsolute(this.posOut);
            int workerId = joint.getExtraActionVars(0);
            Entity entity = world.getEntity(workerId);
            EntityTempWorker worker;
            if (entity == null || ((Entity)entity).isRemoved() || !(entity instanceof EntityTempWorker)) {
               entity = (new EntityTempWorker(world)).setConducter(entityConducted).setBase(entityLivingBase).setWorkerType(EntityTempWorker.WorkerType.flamer);
               ((Entity)entity).moveTo(this.posCenter.x, this.posCenter.y, this.posCenter.z, 0.0F, 0.0F);
               worker = (EntityTempWorker)entity;
               worker.setAdditionalPos(this.posOut.x, this.posOut.y, this.posOut.z);
               worker.setUseEnergy(this.useEnergyHit);
               worker.setDamage(this.damage);
               worker.extraFieldValue = this.effectColor;
               worker.setBlockFire = this.setBlockFire;
               worker.setMobFire = this.setMobFire;
               worker.setMobPotionEffect = this.setMobPotionEffect;
               worker.potionAmplifier = this.potionAmplifier;
               worker.effectDuration = this.effectDuration;
               world.addFreshEntity((Entity)entity);
               joint.setExtraActionVars(0, ((Entity)entity).getId());
            }

            worker = (EntityTempWorker)entity;
            worker.moveTo(this.posCenter.x, this.posCenter.y, this.posCenter.z, 0.0F, 0.0F);
            worker.setAdditionalPos(this.posOut.x, this.posOut.y, this.posOut.z);
            worker.setContinueWork(0);
         }
      }
   }
}
