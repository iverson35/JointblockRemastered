package jp.mc.ancientred.jointblock.entity.masterslave;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.block.BlockJointAttracter;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.item.ItemJBIFFTag;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractSlaveAttatcher {
   public static final int ATTATCHER_TYPE_ID_NONE = 0;
   public static final int ATTATCHER_TYPE_ID_PLAYER = 4;
   public static final int ATTATCHER_TYPE_ID_JOINT = 6;
   public static final int ATTATCHER_TYPE_ID_ATTRACTER = 8;
   public static final int ATTATCHER_TYPE_ID_TEMPORARY = 10;
   public static final int INVALID_STATE_EXPIRED_THRESHOLD = 200;
   public final int typeId;
   public int tickCounter;
   public int cannotFindMasterTick;
   public boolean isEnabled;
   public WeakReference<MasterSlot> connectedMasterSlotWeak;
   protected AABB aabbWork = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   public Vec3 savedPrevPos = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   public Vec3 savedLastTickPos = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);

   public static void setTemporaryAtattcher(EntityJointed slaveJointed, short aliveTick) {
      slaveJointed.slaveAttatcher = new TemporarySlaveAttatcher(aliveTick);
   }

   public static AbstractSlaveAttatcher createFromTypeId(int attatcherTypeId) {
      switch (attatcherTypeId) {
         case 4:
            return new PlayerSlaveAttatcher();
         case 5:
         case 7:
         case 9:
         default:
            throw new RuntimeException("arube:should not be in here!!");
         case 6:
            return new JointSlaveAttatcher();
         case 8:
            return new TileEntitySlaveAttatcher();
         case 10:
            return new TemporarySlaveAttatcher();
      }
   }

   public static void saveToNBT(AbstractSlaveAttatcher attatcher, CompoundTag tag) {
      if (attatcher != null) {
         CompoundTag sub = new CompoundTag();
         sub.putInt("typeId", attatcher.typeId);
         attatcher.writeToNBT(sub);
         tag.put("jbMasterAttatcher", sub);
      }
   }

   public static AbstractSlaveAttatcher createFromNBT(CompoundTag tag) {
      try {
         if (tag != null && tag.contains("jbMasterAttatcher")) {
            CompoundTag sub = tag.getCompound("jbMasterAttatcher");
            int typeId = sub.getByte("typeId");
            AbstractSlaveAttatcher instance = createFromTypeId(typeId);
            if (instance != null) {
               instance.readFromNBT(sub);
            }

            return instance;
         } else {
            return null;
         }
      } catch (Exception var4) {
         return null;
      }
   }

   protected AbstractSlaveAttatcher(int argTypeId) {
      this.typeId = argTypeId;
   }

   public boolean isUnderMasterControl() {
      return this.isEnabled;
   }

   public abstract boolean hasMasterReferenceServer(EntityJointed var1);

   public abstract boolean hasMasterReferenceClient(EntityJointed var1);

   public abstract void searchAndSetupMasterReference(EntityJointed var1, boolean var2);

   public abstract void writeSpawnData(FriendlyByteBuf var1);

   public abstract void readSpawnData(FriendlyByteBuf var1);

   protected abstract void writeToNBT(CompoundTag var1);

   protected abstract void readFromNBT(CompoundTag var1);

   public abstract boolean isConenctionValid(EntityJointed var1);

   public abstract void setPositionFromMaster(EntityJointed var1);

   public void setAdditionalMatrix(EntityJointed entityJointed, ModelViewMatrix matrix) {
   }

   public void keepConnectionAlive() {
      if (this.connectedMasterSlotWeak != null) {
         MasterSlot connectedMasterSlot = this.connectedMasterSlotWeak.get();
         if (connectedMasterSlot != null) {
            connectedMasterSlot.keepConnectionAlive();
         }
      }

   }

   public void breakConnection(EntityJointed slaveJointed) {
      if (this.connectedMasterSlotWeak != null) {
         MasterSlot connectedMasterSlot = this.connectedMasterSlotWeak.get();
         if (connectedMasterSlot != null && connectedMasterSlot.isConnectionAliveWith(slaveJointed)) {
            connectedMasterSlot.releaseConnection();
         }
      }

   }

   protected float clampYaw(float orig) {
      return orig %= 360.0F;
   }

   public void savePrevValues(EntityJointed ej) {
      this.savedPrevPos.x = ej.xo;
      this.savedPrevPos.y = ej.yo;
      this.savedPrevPos.z = ej.zo;
      this.savedLastTickPos.x = ej.xOld;
      this.savedLastTickPos.y = ej.yOld;
      this.savedLastTickPos.z = ej.zOld;
   }

   public void resumePrevValues(EntityJointed ej) {
      ej.xo = this.savedPrevPos.x;
      ej.yo = this.savedPrevPos.y;
      ej.zo = this.savedPrevPos.z;
      ej.xOld = this.savedLastTickPos.x;
      ej.yOld = this.savedLastTickPos.y;
      ej.zOld = this.savedLastTickPos.z;
   }

   public static class TemporarySlaveAttatcher extends AbstractSlaveAttatcher {
      private short aliveTick;

      public TemporarySlaveAttatcher() {
         this((short)0);
      }

      public TemporarySlaveAttatcher(short argAliveTick) {
         super(10);
         this.aliveTick = argAliveTick;
      }

      public boolean isUnderMasterControl() {
         return false;
      }

      public void searchAndSetupMasterReference(EntityJointed entityJointedSlave, boolean forClient) {
      }

      public boolean hasMasterReferenceServer(EntityJointed entityJointedSlave) {
         return true;
      }

      public boolean hasMasterReferenceClient(EntityJointed entityJointedSlave) {
         return true;
      }

      public boolean isConenctionValid(EntityJointed slaveJointed) {
         return this.aliveTick > 0;
      }

      public void setPositionFromMaster(EntityJointed entityJointedSlave) {
         --this.aliveTick;
      }

      protected void writeToNBT(CompoundTag tag) {
         tag.putShort("aliveTick", this.aliveTick);
      }

      protected void readFromNBT(CompoundTag tag) {
         this.aliveTick = tag.getShort("aliveTick");
      }

      public void writeSpawnData(FriendlyByteBuf buff) {
      }

      public void readSpawnData(FriendlyByteBuf buff) {
      }
   }

   public static class TileEntitySlaveAttatcher extends AbstractSlaveAttatcher {
      private int xCoord;
      private int yCoord;
      private int zCoord;
      public TileEntityJointAttracter teAttracter;

      public TileEntitySlaveAttatcher() {
         super(8);
      }

      public TileEntitySlaveAttatcher(TileEntityJointAttracter teAttracterFound) {
         this();
         BlockPos pos = teAttracterFound.getBlockPos();
         this.xCoord = pos.getX();
         this.yCoord = pos.getY();
         this.zCoord = pos.getZ();
         this.teAttracter = teAttracterFound;
      }

      public void searchAndSetupMasterReference(EntityJointed entityJointedSlave, boolean forClient) {
         this.teAttracter = BlockJointAttracter.getTileEntityJointerAttracter(entityJointedSlave.level(), this.xCoord, this.yCoord, this.zCoord);
      }

      public boolean hasMasterReferenceServer(EntityJointed entityJointedSlave) {
         return this.teAttracter != null;
      }

      public boolean hasMasterReferenceClient(EntityJointed entityJointedSlave) {
         if (this.teAttracter == null) {
            return false;
         } else {
            BlockPos pos = this.teAttracter.getBlockPos();
            return pos.getX() == this.xCoord && pos.getY() == this.yCoord && pos.getZ() == this.zCoord;
         }
      }

      public boolean isConenctionValid(EntityJointed slaveJointed) {
         if (this.teAttracter != null && !this.teAttracter.isRemoved() && this.teAttracter.getLevel() == slaveJointed.level()) {
            BlockPos pos = this.teAttracter.getBlockPos();
            return pos.getX() == this.xCoord
               && pos.getY() == this.yCoord
               && pos.getZ() == this.zCoord
               && this.teAttracter.isSlaveAttractable(slaveJointed)
               && this.teAttracter.masterSlot != null
               && this.teAttracter.masterSlot.isConnectionAliveWith(slaveJointed);
         } else {
            return false;
         }
      }

      public void setPositionFromMaster(EntityJointed entityJointedSlave) {
         if (this.teAttracter != null) {
            this.teAttracter.setPositionAndRotationOfSlave(entityJointedSlave);
            entityJointedSlave.yRotO = entityJointedSlave.yRot;
         }

      }

      protected void writeToNBT(CompoundTag tag) {
         tag.putInt("xCoord", this.xCoord);
         tag.putInt("yCoord", this.yCoord);
         tag.putInt("zCoord", this.zCoord);
      }

      protected void readFromNBT(CompoundTag tag) {
         this.xCoord = tag.getInt("xCoord");
         this.yCoord = tag.getInt("yCoord");
         this.zCoord = tag.getInt("zCoord");
      }

      public void writeSpawnData(FriendlyByteBuf buff) {
         buff.writeInt(this.xCoord);
         buff.writeInt(this.yCoord);
         buff.writeInt(this.zCoord);
      }

      public void readSpawnData(FriendlyByteBuf buff) {
         this.xCoord = buff.readInt();
         this.yCoord = buff.readInt();
         this.zCoord = buff.readInt();
      }
   }

   public static class JointSlaveAttatcher extends AbstarctEntitySlaveAttatcher {
      public EntityJointed entityMaster;

      protected JointSlaveAttatcher() {
         super(6);
      }

      public boolean hasMasterReferenceServer(EntityJointed slaveJointed) {
         return this.entityMaster != null;
      }

      public boolean hasMasterReferenceClient(EntityJointed slaveJointed) {
         return this.entityMaster != null && this.masterEntityIdForClient == this.entityMaster.getId();
      }

      public void searchAndSetupMasterReference(EntityJointed slaveJointed, boolean forClient) {
         this.entityMaster = (EntityJointed)super.getEntityReference(slaveJointed, EntityJointed.class, forClient);
      }

      public void writeSpawnData(FriendlyByteBuf buff) {
         buff.writeInt(this.entityMaster.getId());
      }

      public void readSpawnData(FriendlyByteBuf buff) {
         this.masterEntityIdForClient = buff.readInt();
      }

      public boolean isConenctionValid(EntityJointed slaveJointed) {
         if (!MasterSlaveControl.isValidTargetEntityCommon(this.entityMaster, slaveJointed)) {
            return false;
         } else {
            int slaveSlotNum = slaveJointed.getSlaveSlotNum();
            if (!this.entityMaster.getMasterSlotEnableStateFlag(slaveSlotNum)) {
               return false;
            } else {
               MasterSlot masterSlot = MasterSlot.getFromEntity(this.entityMaster, slaveSlotNum);
               return masterSlot.isConnectionAliveWith(slaveJointed);
            }
         }
      }

      public void setPositionFromMaster(EntityJointed slaveJointed) {
         EntityJointed entityJointedMaster = EntityJointed.asEntityJointed(this.entityMaster);
         if (entityJointedMaster != null) {
            MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointedMaster, slaveJointed.getSlaveSlotNum());
            if (masterSlot != null && masterSlot.isSlotValueSet) {
               slaveJointed.setPos(masterSlot.posX, masterSlot.posY - 0.5 - (double)slaveJointed.coreHeight, masterSlot.posZ);
               slaveJointed.calculateLimbSwing();
            }

         }
      }

      public void setAdditionalMatrix(EntityJointed slaveJointed, ModelViewMatrix matrix) {
         EntityJointed entityJointedMaster = EntityJointed.asEntityJointed(this.entityMaster);
         if (entityJointedMaster != null) {
            MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointedMaster, slaveJointed.getSlaveSlotNum());
            if (masterSlot != null && masterSlot.isSlotValueSet) {
               matrix.setBaseRotationFromMasterAttatcher(masterSlot);
               matrix.translate(0.0F, 1.0F, 0.0F);
               matrix.resumeRotationFromMasterAttatcher(masterSlot);
               float prev = slaveJointed.yRot;
               double rawRad = matrix.getYawFromMatrix();
               slaveJointed.yRot = 180.0F - (float)Math.toDegrees(rawRad);
               if (Float.isNaN(slaveJointed.yRot) || Float.isInfinite(slaveJointed.yRot)) {
                  slaveJointed.yRot = prev;
                  rawRad = 0.0;
               }

               if (slaveJointed.isSlaveOpIndependent()) {
                  matrix.updateSavedBaseFromYawRad(Math.PI - rawRad);
               }

               matrix.translate(-0.0F, -1.0F, -0.0F);
            }

         }
      }
   }

   public static class PlayerSlaveAttatcher extends AbstarctEntitySlaveAttatcher {
      public Player entityMaster;

      protected PlayerSlaveAttatcher() {
         super(4);
      }

      public boolean hasMasterReferenceServer(EntityJointed slaveJointed) {
         return this.entityMaster != null;
      }

      public boolean hasMasterReferenceClient(EntityJointed slaveJointed) {
         return this.entityMaster != null && this.masterEntityIdForClient == this.entityMaster.getId();
      }

      public void searchAndSetupMasterReference(EntityJointed slaveJointed, boolean forClient) {
         this.entityMaster = (Player)super.getEntityReference(slaveJointed, Player.class, forClient);
      }

      public void writeSpawnData(FriendlyByteBuf buff) {
         buff.writeInt(this.entityMaster.getId());
      }

      public void readSpawnData(FriendlyByteBuf buff) {
         this.masterEntityIdForClient = buff.readInt();
      }

      public boolean isConenctionValid(EntityJointed slaveJointed) {
         if (!MasterSlaveControl.isValidTargetEntityCommon(this.entityMaster, slaveJointed)) {
            return false;
         } else if (slaveJointed.getSlaveSlotNum() != 0) {
            return false;
         } else {
            MasterSlot masterSlot = MasterSlot.getFromEntity(this.entityMaster, 0);
            return masterSlot.isConnectionAliveWith(slaveJointed);
         }
      }

      public void setPositionFromMaster(EntityJointed slaveJointed) {
         if (this.entityMaster != null) {
            double z = (double)Mth.cos(-this.entityMaster.yRot * 0.017453292F - 3.1415927F);
            double x = (double)Mth.sin(-this.entityMaster.yRot * 0.017453292F - 3.1415927F);
            int num = ItemJBIFFTag.getPlayerMSBSlavePositionNum(this.entityMaster, slaveJointed);
            double fixX = 0.0;
            double fixZ = 0.0;
            switch (num) {
               case 1:
                  fixX = x;
                  fixZ = z;
                  break;
               case 2:
                  fixX = x * 2.200000047683716;
                  fixZ = z * 2.200000047683716;
                  break;
               case 3:
                  fixX = -x;
                  fixZ = -z;
                  break;
               case 4:
                  fixX = -x * 2.200000047683716;
                  fixZ = -z * 2.200000047683716;
            }

            AABB masterAABB = this.entityMaster.getBoundingBox();
            slaveJointed.setPos((masterAABB.maxX + masterAABB.minX) / 2.0 + fixX, masterAABB.minY, (masterAABB.maxZ + masterAABB.minZ) / 2.0 + fixZ);
            slaveJointed.yRot = this.clampYaw(this.entityMaster.yRot);
            slaveJointed.calculateLimbSwing();
         }

      }
   }

   public abstract static class AbstarctEntitySlaveAttatcher extends AbstractSlaveAttatcher {
      protected UUID masterUUID;
      protected int masterEntityIdForClient = -1;

      protected AbstarctEntitySlaveAttatcher(int argTypeId) {
         super(argTypeId);
      }

      public Entity getEntityReference(EntityJointed slaveJointed, Class clazz, boolean forClient) {
         if (!forClient && this.masterUUID == null) {
            return null;
         } else if (forClient && this.masterEntityIdForClient == -1) {
            return null;
         } else {
            Level world = slaveJointed.level();
            this.aabbWork.maxX = slaveJointed.getX() + 16.0;
            this.aabbWork.minX = slaveJointed.getX() - 16.0;
            this.aabbWork.maxY = slaveJointed.getY() + 16.0;
            this.aabbWork.minY = slaveJointed.getY() - 16.0;
            this.aabbWork.maxZ = slaveJointed.getZ() + 16.0;
            this.aabbWork.minZ = slaveJointed.getZ() - 16.0;
            List<Entity> entityList = world.getEntitiesOfClass((Class<Entity>)clazz, this.aabbWork);

            for (Entity entityFound : entityList) {
               if (!slaveJointed.equals(entityFound)) {
                  MasterSlot masterSlot;
                  if (forClient) {
                     if (entityFound.getId() == this.masterEntityIdForClient) {
                        masterSlot = MasterSlot.getFromEntity(entityFound, slaveJointed.getSlaveSlotNum());
                        if (masterSlot.canBeConnectedWith(slaveJointed)) {
                           masterSlot.createConnectionWith(slaveJointed, this);
                           return entityFound;
                        }
                     }
                  } else if (this.masterUUID.equals(entityFound.getUUID())) {
                     masterSlot = MasterSlot.getFromEntity(entityFound, slaveJointed.getSlaveSlotNum());
                     if (masterSlot.canBeConnectedWith(slaveJointed)) {
                        masterSlot.createConnectionWith(slaveJointed, this);
                        return entityFound;
                     }
                  }
               }
            }

            return null;
         }
      }

      protected void writeToNBT(CompoundTag tag) {
         tag.putLong("UUIDMost", this.masterUUID.getMostSignificantBits());
         tag.putLong("UUIDLeast", this.masterUUID.getLeastSignificantBits());
      }

      protected void readFromNBT(CompoundTag tag) {
         if (this.masterUUID == null || !this.masterUUID.equals(this.masterUUID)) {
            this.masterUUID = new UUID(tag.getLong("UUIDMost"), tag.getLong("UUIDLeast"));
         }

      }
   }
}
