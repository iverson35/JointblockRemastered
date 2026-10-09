package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ActionHarvest implements IJBModelItem.IJBActionHandler {
   protected float useEnergy = 0.5F;

   public String getActionName() {
      return "jbaction.harvest.name";
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (entityConducted != null) {
         if (entityLivingBase instanceof IJBEntityState) {
            IJBEntityState entityState = (IJBEntityState)entityLivingBase;
            Vec3 posCenter = JBCommonUtil.createVectorHelper(0.0, 1.0, 0.0);
            posCenter = matrixUtil.transformAbsolute(posCenter);
            int targetX = Mth.floor(posCenter.x);
            int targetY = Mth.floor(posCenter.y);
            int targetZ = Mth.floor(posCenter.z);
            if (entityConducted instanceof Player) {
               Player entityPlayer = (Player)entityConducted;
               if (!entityPlayer.mayBuild()) {
                  return;
               }
            }

            BlockState bState = WorldHelper.getBlockState(world, targetX, targetY, targetZ);
            Block blockHit = bState.getBlock();
            boolean isHarvestTarget = false;
            // 1.12.2 Material check (PLANTS/CACTUS/VINE/GOURD) mapped to 1.20.1 block classes
            if ((blockHit instanceof BushBlock || blockHit instanceof CactusBlock || blockHit instanceof VineBlock || blockHit instanceof StemGrownBlock) && !(blockHit instanceof StemBlock) && !(blockHit instanceof SaplingBlock)) {
               isHarvestTarget = true;
            }

            if (isHarvestTarget && ((IJBEntityState)entityLivingBase).useEntityEnergy(this.useEnergy)) {
               JBHarvestUtil.harvestBlockToInventoryDropItem(world, entityConducted, entityState, targetX, targetY, targetZ, blockHit, 18);
            }

         }
      }
   }
}
