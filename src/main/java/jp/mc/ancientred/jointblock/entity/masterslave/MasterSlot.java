package jp.mc.ancientred.jointblock.entity.masterslave;

import java.lang.ref.WeakReference;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MasterSlot {
   public double posX;
   public double posY;
   public double posZ;
   public float m00;
   public float m01;
   public float m02;
   public float m10;
   public float m11;
   public float m12;
   public float m20;
   public float m21;
   public float m22;
   public float savedBaseM00;
   public float savedBaseM10;
   public float savedBaseM20;
   public float savedBaseM01;
   public float savedBaseM11;
   public float savedBaseM21;
   public float savedBaseM02;
   public float savedBaseM12;
   public float savedBaseM22;
   public boolean isSlotValueSet;
   public boolean isConnectionAlive;
   public int connectedSlaveEntityHash;
   public static final int KEEP_CONNECTION_ALIVE_TICK = 15;
   public short connectionEJAliveTick = 15;
   public WeakReference<AbstractSlaveAttatcher> slaveAttatcherWeak;
   public WeakReference<EntityJointed> entityJointedWeak;
   public int updatedTicksExisted;

   public static MasterSlot getFromEntity(Entity entity, int slotNum) {
      return getOrCreateFromEntity(entity, slotNum, true);
   }

   private static MasterSlot getOrCreateFromEntity(Entity entity, int slotNum, boolean createIfNot) {
      if (entity instanceof Player && slotNum == 0) {
         IMasterSlotHolder property = entity.getCapability(CapabilityMasterSlotHolder.CAPABILITY, Direction.UP).orElse(null);
         if (property == null) {
            return null;
         } else {
            MasterSlotHolderImpl epms = (MasterSlotHolderImpl)property;
            if (epms.masterSlot == null && createIfNot) {
               epms.masterSlot = new MasterSlot();
            }

            return epms.masterSlot;
         }
      } else if (entity instanceof EntityJointed) {
         EntityJointed entityJointed = (EntityJointed)entity;
         if (entityJointed.masterSlotHolderArray == null) {
            if (!createIfNot) {
               return null;
            }

            entityJointed.masterSlotHolderArray = new MasterSlot[11];
         }

         if (slotNum >= 0 && entityJointed.masterSlotHolderArray.length > slotNum) {
            if (createIfNot && entityJointed.masterSlotHolderArray[slotNum] == null) {
               entityJointed.masterSlotHolderArray[slotNum] = new MasterSlot();
            }

            return entityJointed.masterSlotHolderArray[slotNum];
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void saveToNBT(EntityJointed entityJointed, CompoundTag tagParent) {
      if (entityJointed.masterSlotHolderArray != null) {
         ListTag nbttaglist = new ListTag();

         for (int i = 0; i < entityJointed.masterSlotHolderArray.length; ++i) {
            MasterSlot masterSlot = entityJointed.masterSlotHolderArray[i];
            if (masterSlot != null) {
               CompoundTag nbttagcompound = new CompoundTag();
               nbttagcompound.putByte("Slot", (byte)i);
               masterSlot.saveNBTData(nbttagcompound);
               nbttaglist.add(nbttagcompound);
            }
         }

         tagParent.put("masterSlotHolderArray", nbttaglist);
      }
   }

   public static void loadFromNBT(EntityJointed entityJointed, CompoundTag tagParent) {
      if (tagParent.contains("masterSlotHolderArray")) {
         ListTag nbttaglist = tagParent.getList("masterSlotHolderArray", Tag.TAG_COMPOUND);
         if (nbttaglist != null && nbttaglist.size() != 0) {
            entityJointed.masterSlotHolderArray = new MasterSlot[11];

            for (int i = 0; i < nbttaglist.size(); ++i) {
               CompoundTag nbttagcompound = nbttaglist.getCompound(i);
               int j = nbttagcompound.getByte("Slot") & 255;
               MasterSlot masterSlot = new MasterSlot();
               masterSlot.loadNBTData(nbttagcompound);
               if (j >= 0 && j < entityJointed.masterSlotHolderArray.length) {
                  entityJointed.masterSlotHolderArray[j] = masterSlot;
               }
            }

         }
      }
   }

   public boolean canBeConnectedWith(EntityJointed entityJointedSlave) {
      if (this.isConnectionAlive) {
         return this.connectedSlaveEntityHash == JBCommonUtil.getUUIDHash(entityJointedSlave);
      } else {
         return true;
      }
   }

   public boolean isConnectionAliveWith(EntityJointed entityJointedSlave) {
      return this.isConnectionAlive && this.connectedSlaveEntityHash == JBCommonUtil.getUUIDHash(entityJointedSlave);
   }

   public void createConnectionWith(EntityJointed entityJointedSlave, AbstractSlaveAttatcher slaveAttatcher) {
      this.slaveAttatcherWeak = new WeakReference(slaveAttatcher);
      slaveAttatcher.connectedMasterSlotWeak = new WeakReference(this);
      this.entityJointedWeak = new WeakReference(entityJointedSlave);
      this.connectedSlaveEntityHash = JBCommonUtil.getUUIDHash(entityJointedSlave);
      this.isConnectionAlive = true;
      this.keepConnectionAlive();
   }

   public void updateConnection() {
      if (this.isConnectionAlive) {
         if (this.connectionEJAliveTick > 0) {
            --this.connectionEJAliveTick;
         } else {
            this.releaseConnection();
         }
      }

   }

   public void keepConnectionAlive() {
      this.connectionEJAliveTick = 15;
   }

   public void releaseConnection() {
      this.isConnectionAlive = false;
      this.connectedSlaveEntityHash = 0;
      this.slaveAttatcherWeak = null;
      this.entityJointedWeak = null;
   }

   public boolean isConnectionAlive() {
      return this.isConnectionAlive;
   }

   public AbstractSlaveAttatcher getConnectedSlaveAtattcher() {
      return this.slaveAttatcherWeak != null ? this.slaveAttatcherWeak.get() : null;
   }

   public EntityJointed getConnectedSlaveEntity() {
      return this.entityJointedWeak != null ? this.entityJointedWeak.get() : null;
   }

   public void saveNBTData(CompoundTag tag) {
      tag.putBoolean("isConnectionAlive", this.isConnectionAlive);
      tag.putInt("connectedSlaveEntityHash", this.connectedSlaveEntityHash);
   }

   public void loadNBTData(CompoundTag tag) {
      this.isConnectionAlive = tag.getBoolean("isConnectionAlive");
      this.connectedSlaveEntityHash = tag.getInt("connectedSlaveEntityHash");
   }

   public static class MasterSlotHolderImpl implements IMasterSlotHolder, ICapabilitySerializable<CompoundTag> {
      public MasterSlot masterSlot = new MasterSlot();
      private final LazyOptional<IMasterSlotHolder> holder = LazyOptional.of(() -> this);

      public void saveNBTData(CompoundTag tag) {
         this.masterSlot.saveNBTData(tag);
      }

      public CompoundTag serializeNBT() {
         CompoundTag tag = new CompoundTag();
         this.saveNBTData(tag);
         return tag;
      }

      public void loadNBTData(CompoundTag tag) {
         this.masterSlot.loadNBTData(tag);
      }

      public void deserializeNBT(CompoundTag nbt) {
         this.loadNBTData(nbt);
      }

      @NotNull
      public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
         return capability == CapabilityMasterSlotHolder.CAPABILITY ? this.holder.cast() : LazyOptional.empty();
      }
   }

   public static class CapabilityMasterSlotHolder {
      // 1.20.1: @CapabilityInject and Capability.IStorage are gone. The token below resolves the
      // capability instance; register the implementation through RegisterCapabilitiesEvent#register.
      public static final Capability<IMasterSlotHolder> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
      });
      public static final ResourceLocation EXTENDED_PROPERTY_MSLOT_HOLDER = new ResourceLocation("jointblock:capability_mshld");

      public static void register(RegisterCapabilitiesEvent event) {
         event.register(IMasterSlotHolder.class);
      }
   }

   public interface IMasterSlotHolder {
      void saveNBTData(CompoundTag var1);

      void loadNBTData(CompoundTag var1);
   }
}
