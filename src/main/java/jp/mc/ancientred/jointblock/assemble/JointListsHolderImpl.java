package jp.mc.ancientred.jointblock.assemble;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JointListsHolderImpl implements IJointListsHolder {
   public int[] serialized = null;
   public CompoundTag coreSubTag = null;
   public JointParts[] refLists;
   public int minY = 0;
   public int maxY = 0;

   public void setReferenceLists(JointParts[] referenceAry, JointParts[] motionReferenceAry) {
      this.refLists = referenceAry;
   }

   public void setMinMaxY(int minY, int maxY) {
      this.minY = minY;
      this.maxY = maxY;
   }

   public void createBlocksFromRefLists(IJointListsHolder.BlockArranger arranger, int facing, int posX, int posY, int posZ) {
      int len = this.refLists.length;

      for(int i = 0; i < len; ++i) {
         if (this.refLists[i] != null) {
            this.refLists[i].turnIntoBlock(arranger, (EntityJointed)null, facing, posX, posY, posZ);
         }
      }

   }

   public void createCoreBlockFromRefLists(IJointListsHolder.BlockArranger arranger, int facing, int posX, int posY, int posZ) {
      if (this.refLists.length != 0) {
         this.refLists[0].turnIntoBlock(arranger, (EntityJointed)null, facing, 0, 0, 0);
      }
   }

   public static class DefaultBlockArranger implements IJointListsHolder.BlockArranger {
      private final Level world;

      public DefaultBlockArranger(Level world) {
         this.world = world;
      }

      public void placeBlockAt(JointParts parts, EntityJointed entityJointedMayBeNull, int posX, int posY, int posZ, int coreFacing, int facingSide) {
         Block block = parts.getJBBlockType();
         if (block != null) {
            if (WorldHelper.setBlock(this.world, posX, posY, posZ, block, 2)) {
               TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(this.world, posX, posY, posZ);
               te.readFromJointParts(entityJointedMayBeNull, parts, facingSide, coreFacing);
               if (parts.jointType == 15 && entityJointedMayBeNull != null) {
                  parts.reAttatchSlaveToTileEntity(te, entityJointedMayBeNull);
               }
            }

         }
      }
   }
}
