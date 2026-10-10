package jp.mc.ancientred.jointblock.tileentity;

import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityJointAttracter extends TileEntityJoint {
   public static final int DEFAULT_SLAVE_OFFSET = 16;
   public static final int SLAVE_OFFSET_DIV_I = 16;
   public static final double SLAVE_OFFSET_DIV_D = 16.0;
   public static final float SLAVE_OFFSET_DIV_F = 16.0F;
   public short temporaryDisabledTick;
   public MasterSlot masterSlot = new MasterSlot();

   public TileEntityJointAttracter(BlockPos pos, BlockState state) {
      super(JBBlockEntityTypes.JOINT_ATTRACTER, 15, new int[]{1, 1, 16, 0}, pos, state);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level.isClientSide && this.masterSlot != null) {
         this.masterSlot.updateConnection();
      }

      if (this.temporaryDisabledTick > 0) {
         --this.temporaryDisabledTick;
      }

   }

   @Override
   protected void writeOriginalValues(CompoundTag par1nbtTagCompound) {
      super.writeOriginalValues(par1nbtTagCompound);
      this.masterSlot.saveNBTData(par1nbtTagCompound);
      par1nbtTagCompound.putShort("temporaryDisabledTick", this.temporaryDisabledTick);
   }

   @Override
   protected void readOriginalValues(CompoundTag par1nbtTagCompound) {
      super.readOriginalValues(par1nbtTagCompound);
      if (par1nbtTagCompound.contains("masterSlot")) {
         CompoundTag sub = par1nbtTagCompound.getCompound("masterSlot");
         if (sub != null) {
            this.masterSlot.loadNBTData(sub);
         }
      }

      this.temporaryDisabledTick = par1nbtTagCompound.getShort("temporaryDisabledTick");
   }

   public boolean isEnabledAsMaster() {
      if (this.initValues != null && this.initValues[0] != null) {
         return this.initValues[0][0] == 1;
      } else {
         return false;
      }
   }

   public int getMasterSlotNum() {
      return this.initValues != null && this.initValues[0] != null ? this.initValues[0][1] : -1;
   }

   public void setPositionAndRotationOfSlave(EntityJointed entityJointedSlave) {
      int face = this.getJointFacing();
      if (this.connectSide <= 5 && face <= 5) {
         double offset = (double)this.getSlaveOffset() / 16.0;
         int sideOppositeIdx = EnumDirectionOld.OPPOSITES[this.connectSide];
         EnumDirectionOld sideOpposite = EnumDirectionOld.VALID_DIRECTIONS[sideOppositeIdx];
         entityJointedSlave.setPos((double)this.worldPosition.getX() + 0.5 + (double)sideOpposite.offsetX * offset, (double)this.worldPosition.getY() + 0.5 + (double)sideOpposite.offsetY * offset - 0.5 - (double)entityJointedSlave.coreHeight, (double)this.worldPosition.getZ() + 0.5 + (double)sideOpposite.offsetZ * offset);
         if (this.connectSide == 0) {
            entityJointedSlave.setYRot(JBRotationUtil.entityYawEachSide[face]);
         } else if (this.connectSide == 1) {
            entityJointedSlave.setYRot(JBRotationUtil.entityYawEachSideNagateZ[face]);
         } else {
            entityJointedSlave.setYRot(JBRotationUtil.entityYawEachSide[sideOppositeIdx]);
         }

      }
   }

   public int getSlaveOffset() {
      return this.initValues != null && this.initValues[0] != null ? this.initValues[0][2] : 16;
   }

   public boolean isSlaveAttractable(EntityJointed entityJointed) {
      return this.temporaryDisabledTick <= 0 && this.isEnabledAsMaster() && this.getMasterSlotNum() == entityJointed.getSlaveSlotNum();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public AABB getRenderBoundingBox() {
      AABB bb = super.getRenderBoundingBox();
      int face = this.getJointFacing();
      if (this.connectSide <= 5 && face <= 5) {
         double offset = (double)this.getSlaveOffset() / 16.0;
         EnumDirectionOld sideOpposite = EnumDirectionOld.VALID_DIRECTIONS[EnumDirectionOld.OPPOSITES[this.connectSide]];
         double offsetX = (double)sideOpposite.offsetX * offset;
         double offsetY = (double)sideOpposite.offsetY * offset;
         double offsetZ = (double)sideOpposite.offsetZ * offset;
         if (offsetX < 0.0) {
            bb.minX += offsetX;
         }

         if (offsetX > 0.0) {
            bb.maxX += offsetX;
         }

         if (offsetY < 0.0) {
            bb.minY += offsetY;
         }

         if (offsetY > 0.0) {
            bb.maxY += offsetY;
         }

         if (offsetZ < 0.0) {
            bb.minZ += offsetZ;
         }

         if (offsetZ > 0.0) {
            bb.maxZ += offsetZ;
         }

         return bb;
      } else {
         return bb;
      }
   }

   public void setTemporaryDisabledTick(short disableTick) {
      this.temporaryDisabledTick = disableTick;
      this.markUpdateThis();
   }
}
