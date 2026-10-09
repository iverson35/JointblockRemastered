package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ActionPlant implements IJBModelItem.IJBActionHandler {
   protected float useEnergy = 0.5F;

   public String getActionName() {
      return "jbaction.plant.name";
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
            Block blockHit = WorldHelper.getBlock(world, targetX, targetY, targetZ);
            if (entityConducted instanceof Player) {
               Player entityPlayer = (Player)entityConducted;
               if (!entityPlayer.mayBuild()) {
                  return;
               }
            }

            if (blockHit == Blocks.AIR) {
               Block blockBelow = WorldHelper.getBlock(world, targetX, targetY - 1, targetZ);
               if ((blockBelow == Blocks.DIRT || blockBelow == Blocks.GRASS_BLOCK) && ((IJBEntityState)entityLivingBase).useEntityEnergy(this.useEnergy)) {
                  WorldHelper.setBlock(world, targetX, targetY - 1, targetZ, Blocks.FARMLAND);
                  blockBelow = Blocks.FARMLAND;
               }

               if (blockBelow == Blocks.FARMLAND && ((IJBEntityState)entityLivingBase).getEntityEnergy() >= this.useEnergy) {
                  Block blockToPlant = this.getPlantableFromInventory(entityState);
                  if (blockToPlant != null && ((IJBEntityState)entityLivingBase).useEntityEnergy(this.useEnergy)) {
                     WorldHelper.setBlock(world, targetX, targetY, targetZ, blockToPlant);
                  }
               }
            }

         }
      }
   }

   private Block getPlantableFromInventory(IJBEntityState entityState) {
      Container targetInventory = entityState.getInnerInventory();
      if (targetInventory == null) {
         return null;
      } else {
         for(int i = 0; i < targetInventory.getContainerSize() && i < 18; ++i) {
            ItemStack itemStackInSlot = targetInventory.getItem(i);
            if (itemStackInSlot != null && itemStackInSlot.getCount() > 0) {
               Block itemFoundToBlock = null;
               if (itemStackInSlot.getItem() == Items.WHEAT_SEEDS) {
                  itemFoundToBlock = Blocks.WHEAT;
               }

               if (itemStackInSlot.getItem() == Items.PUMPKIN_SEEDS) {
                  itemFoundToBlock = Blocks.PUMPKIN_STEM;
               }

               if (itemStackInSlot.getItem() == Items.MELON_SEEDS) {
                  itemFoundToBlock = Blocks.MELON_STEM;
               }

               if (itemStackInSlot.getItem() == Items.CARROT) {
                  itemFoundToBlock = Blocks.CARROTS;
               }

               if (itemStackInSlot.getItem() == Items.POTATO) {
                  itemFoundToBlock = Blocks.POTATOES;
               }

               if (itemFoundToBlock != null) {
                  itemStackInSlot.shrink(1);
                  targetInventory.setChanged();
                  return itemFoundToBlock;
               }
            }
         }

         return null;
      }
   }
}
