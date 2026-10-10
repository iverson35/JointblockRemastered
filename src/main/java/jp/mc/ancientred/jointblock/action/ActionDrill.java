package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ActionDrill implements IJBModelItem.IJBActionHandler {
   private static final int EXVALNUM_WORKER_ID = 0;
   protected float useEnergy = 0.1F;

   public String getActionName() {
      return "jbaction.drill.name";
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (entityConducted != null) {
         if (!joint.hasInitExtraActionVars()) {
            joint.initExtraActionVars(0, 0, -1, Integer.MAX_VALUE);
            joint.setExtraActionVars(0, -1);
         }

         int rotateUpdateX = Math.abs(joint.getPrevRotationX() - joint.getRotationX());
         int rotateUpdateY = Math.abs(joint.getPrevRotationY() - joint.getRotationY());
         int rotateUpdateZ = Math.abs(joint.getPrevRotationZ() - joint.getRotationZ());
         int largestUpdate = Math.max(rotateUpdateZ, Math.max(rotateUpdateX, rotateUpdateY));
         if (largestUpdate < 20) {
            joint.setExtraActionVars(0, -1);
         } else {
            Vec3 posTop = JBCommonUtil.createVectorHelper(0.0, 0.4, 0.0);
            posTop = matrixUtil.transformAbsolute(posTop);
            int workerId = joint.getExtraActionVars(0);
            Entity entity = world.getEntity(workerId);
            if (entity == null || ((Entity)entity).isRemoved() || !(entity instanceof EntityTempWorker)) {
               entity = (new EntityTempWorker(world)).setConducter(entityConducted).setBase(entityLivingBase).setWorkerType(EntityTempWorker.WorkerType.drill);
               ((Entity)entity).moveTo(posTop.x, posTop.y, posTop.z, 0.0F, 0.0F);
               ((EntityTempWorker)entity).setUseEnergy(this.useEnergy);
               world.addFreshEntity((Entity)entity);
               joint.setExtraActionVars(0, ((Entity)entity).getId());
            }

            EntityTempWorker worker = (EntityTempWorker)entity;
            worker.moveTo(posTop.x, posTop.y, posTop.z, 0.0F, 0.0F);
            worker.setContinueWork(largestUpdate / 50);
         }
      }
   }
}
