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

public class ActionBlade implements IJBModelItem.IJBActionHandler {
   private static final int EXVALNUM_WORKER_ID = 0;
   protected float damage = 45.0F;
   public float useEnergyMultA = 0.0033F;
   public float useEnergyMultB = 0.0016F;
   public float useEnergyHit = 10.0F;
   public int bladeColor = -2130758861;
   public boolean setMobFire = false;
   public int setMobPotionEffect = -1;
   public int potionAmplifier = 1;
   public int effectDuration = 5;
   private Vec3 posCenter = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private Vec3 posTop = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private Vec3 posBottom = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);

   public String getActionName() {
      return "jbaction.blade.name";
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
            this.posCenter = new Vec3(0.0, this.posCenter.y, this.posCenter.z);
            this.posCenter = new Vec3(this.posCenter.x, 1.0, this.posCenter.z);
            this.posCenter = new Vec3(this.posCenter.x, this.posCenter.y, 0.0);
            this.posCenter = matrixUtil.transformAbsolute(this.posCenter);
            this.posBottom = new Vec3(0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[0].offsetX * 0.5, this.posBottom.y, this.posBottom.z);
            this.posBottom = new Vec3(this.posBottom.x, 1.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[0].offsetY * 0.5, this.posBottom.z);
            this.posBottom = new Vec3(this.posBottom.x, this.posBottom.y, 0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[0].offsetZ * 0.5);
            this.posBottom = matrixUtil.transformAbsolute(this.posBottom);
            this.posTop = new Vec3(0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[1].offsetX * 0.5, this.posTop.y, this.posTop.z);
            this.posTop = new Vec3(this.posTop.x, 1.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[1].offsetY * 0.5, this.posTop.z);
            this.posTop = new Vec3(this.posTop.x, this.posTop.y, 0.0 + (double)EnumDirectionOld.VALID_DIRECTIONS[1].offsetZ * 0.5);
            this.posTop = matrixUtil.transformAbsolute(this.posTop);
            int workerId = joint.getExtraActionVars(0);
            Entity entity = world.getEntity(workerId);
            EntityTempWorker worker;
            if (entity == null || ((Entity)entity).isRemoved() || !(entity instanceof EntityTempWorker)) {
               entity = (new EntityTempWorker(world)).setConducter(entityConducted).setBase(entityLivingBase).setWorkerType(EntityTempWorker.WorkerType.blade);
               ((Entity)entity).moveTo(this.posBottom.x, this.posBottom.y, this.posBottom.z, 0.0F, 0.0F);
               worker = (EntityTempWorker)entity;
               worker.setAdditionalPos(this.posTop.x, this.posTop.y, this.posTop.z);
               worker.setUseEnergy(this.useEnergyHit);
               worker.setDamage(this.damage);
               worker.extraFieldValue = this.bladeColor;
               worker.setMobFire = this.setMobFire;
               worker.setMobPotionEffect = this.setMobPotionEffect;
               worker.potionAmplifier = this.potionAmplifier;
               worker.effectDuration = this.effectDuration;
               world.addFreshEntity((Entity)entity);
               joint.setExtraActionVars(0, ((Entity)entity).getId());
            }

            worker = (EntityTempWorker)entity;
            worker.moveTo(this.posBottom.x, this.posBottom.y, this.posBottom.z, 0.0F, 0.0F);
            worker.setAdditionalPos(this.posTop.x, this.posTop.y, this.posTop.z);
            worker.setContinueWork(0);
         }
      }
   }
}
