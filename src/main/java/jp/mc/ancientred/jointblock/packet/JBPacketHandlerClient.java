package jp.mc.ancientred.jointblock.packet;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.entity.ExplosionEx;
import jp.mc.ancientred.jointblock.entity.masterslave.AbstractSlaveAttatcher;
import jp.mc.ancientred.jointblock.export.JBCompressToolHelper;
import jp.mc.ancientred.jointblock.export.JBImportExportDataUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the 1.12.2 {@code JBPacketHandlerClient}.
 *
 * <p>{@code FMLNetworkEvent.ClientCustomPacketEvent} does not exist in 1.20.1; the raw
 * payload is handed over by {@link JBClientPacketInboudHandler} on the network thread.
 * The 1.12.2 thread contract is kept: the first five packet types are handled on the
 * calling (network) thread, everything else is re-dispatched onto the client thread
 * ({@code Minecraft.isCallingFromMinecraftThread()} → {@code Minecraft.isSameThread()},
 * {@code Minecraft.addScheduledTask()} → {@code Minecraft.execute(Runnable)}).
 */
public class JBPacketHandlerClient {

   /** 1.12.2 {@code onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent)}. */
   public static void handleClientPacket(JBNetwork.Payload message) {
      switchByPacketType(message, false);
   }

   public static boolean switchByPacketType(JBNetwork.Payload packet, boolean intercepted) {
      FriendlyByteBuf data = packet.payload();
      byte packetType = -1;

      try {
         packetType = data.readByte();
         if (packetType == JBNetwork.PACKET_TYPE_SETTINGS_SYNC) {
            syncSettingOnClient(data);
            return true;
         }

         if (packetType == JBNetwork.PACKET_TYPE_JOINTUPDATE) {
            updateEntityJointOnClient(data);
            return true;
         }

         if (packetType == JBNetwork.PACKET_TYPE_JOINTDISASSEMBLE) {
            updateDisassembleJointOnClient(data);
            return true;
         }

         if (packetType == JBNetwork.PACKET_TYPE_JOINTREINIT) {
            startJointReInitOnClient(data);
            return true;
         }

         if (packetType == JBNetwork.PACKET_TYPE_DUMMY_EXPLODE) {
            receiveDummyExplosion(data);
            return true;
         }

         Minecraft client = Minecraft.getInstance();
         if (!client.isSameThread()) {
            client.execute(() -> {
               // 1.12.2 reset the reader index of the shared payload buffer here; the
               // Payload wrapper hands out a fresh buffer positioned at 0, so the
               // re-dispatch reads the packet type again on its own.
               switchByPacketType(packet, false);
            });
            return true;
         }

         if (packetType == JBNetwork.PACKET_TYPE_CORE_SYNC) {
            updateAttracterSycnOnClient(data);
            return false;
         }

         if (packetType == JBNetwork.PACKET_TYPE_EXPORT_REQUESTED) {
            receiveRequestedBlueprintExportData(data);
            return false;
         }

         if (packetType == JBNetwork.PACKET_TYPE_TUNING_ACT) {
            receiveAssemblyTuningActionAtClient(data);
            return false;
         }

         if (packetType == JBNetwork.PACKET_TYPE_TARGET_SELECT) {
            receiveTargetSelectUpdate(data);
            return false;
         }

         if (packetType == JBNetwork.PACKET_TYPE_SOUND_ENTITYEVENT) {
            playEntityJBSoundOnClient(data);
            return false;
         }

         if (packetType == JBNetwork.PACKET_TYPE_SEAT_RIDE_SYNC) {
            receiveSeatRideSync(data);
            return false;
         }
      } catch (Exception var4) {
         JointBlockRemastered.LOGGER.debug("Error handling JB SERVER-CLIENT Packet :{}", packetType, var4);
      }

      return false;
   }

   private static void syncSettingOnClient(FriendlyByteBuf data) {
      try {
         JBConfig.maxInitLayersLocal = data.readByte();
         JBConfig.bypathPlayerDamageToUnitLocal = data.readBoolean();
         JBConfig.worldMaxEntityRadiusModifierLocal = data.readDouble();
         // 1.12.2 called JBConfig.updateWorldMaxEntityRadiusModifer() here to enlarge
         // World.MAX_ENTITY_RADIUS. 1.17+ removed that global (section based entity
         // storage), so the synced value has no world-wide side effect to apply.
      } catch (NullPointerException var4) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:syncSettingOnClient:Will be fixed in future");
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private static void updateEntityJointOnClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         float limbSwing = data.readFloat();
         float yaw = data.readFloat();
         short dataLen = data.readShort();
         byte[] updata = new byte[dataLen];
         data.readBytes(updata);
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            ((EntityJointed)entity).receiveJointStateUpdate(limbSwing, yaw, updata);
         }
      } catch (NullPointerException var10) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateEntityJointHeightOnClient:Will be fixed in future");
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   /**
    * Client half of {@link JBPacketSender#sendSeatRideSyncToPlayers}.
    *
    * <p>The seat cannot be resolved through {@code level.getEntity(int)} (a Forge
    * {@code PartEntity} is in no level entity storage), so the packet carries the parent entity id
    * and the seat index and the seat is taken from the parent's part array - the same objects the
    * client picks and renders. This is the sync leg the 1.12.2 port got for free from the vanilla
    * passenger packet, because the 1.12.2 seats were real world entities.
    */
   private static void receiveSeatRideSync(FriendlyByteBuf data) {
      try {
         int parentEntityId = data.readInt();
         int seatNum = data.readByte();
         boolean riding = data.readBoolean();
         int passengerEntityId = data.readInt();
         ClientLevel worldClient = Minecraft.getInstance().level;
         if (worldClient == null) {
            return;
         }

         Entity parent = worldClient.getEntity(parentEntityId);
         if (!(parent instanceof EntityJointed)) {
            return;
         }

         EntityJointed entityJointed = (EntityJointed)parent;
         if (seatNum < 0 || seatNum >= entityJointed.entitySeatPartArray.length) {
            return;
         }

         EntitySeatPart seatPart = entityJointed.entitySeatPartArray[seatNum];
         Entity passenger = passengerEntityId < 0 ? CompatHelper.getRiddenByEntity(seatPart) : worldClient.getEntity(passengerEntityId);
         if (riding) {
            if (passenger != null && passenger.getVehicle() != seatPart) {
               passenger.startRiding(seatPart, true);
            }
         } else {
            if (passenger != null && passenger.getVehicle() == seatPart) {
               passenger.stopRiding();
            }

            seatPart.ejectPassengers();
         }
      } catch (NullPointerException var8) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:receiveSeatRideSync:Will be fixed in future");
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   private static void playEntityJBSoundOnClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         byte soundSlotNum = data.readByte();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            EntityJointed ejb = (EntityJointed)entity;
            if (ejb.effectSlots != null && soundSlotNum >= 0 && soundSlotNum < ejb.effectSlots.length) {
               EffectSlot soundSlot = ejb.effectSlots[soundSlotNum];
               if (soundSlot != null && soundSlot.checkEnabled()) {
                  JBModContainer.proxy.playEntitySoundEvent(ejb, soundSlot);
               }
            }
         }
      } catch (NullPointerException var9) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateEntityJointOnClient:Will be fixed in future");
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   private static void updateAttracterSycnOnClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         float updCoreHeight = data.readFloat();
         float updTotalHeight = data.readFloat();
         float updLargeUnitW = data.readFloat();
         byte updSeatFlg = data.readByte();
         int slaveAttatcherType = data.readInt();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            EntityJointed ej = (EntityJointed)entity;
            ej.setCoreHeightAndTotalHeight(updCoreHeight, updTotalHeight, updLargeUnitW, updSeatFlg);
            ej.hasInitEnd = true;
            if (slaveAttatcherType != 0) {
               if (ej.slaveAttatcher == null || slaveAttatcherType != ej.slaveAttatcher.typeId) {
                  ej.slaveAttatcher = AbstractSlaveAttatcher.createFromTypeId(slaveAttatcherType);
               }

               ej.slaveAttatcher.readSpawnData(data);
            } else {
               ej.slaveAttatcher = null;
            }
         }
      } catch (NullPointerException var12) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateDisassembleJointOnClient:Will be fixed in future");
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   private static void updateDisassembleJointOnClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         int disassembleFacing = data.readInt();
         double posX = data.readDouble();
         double posY = data.readDouble();
         double posZ = data.readDouble();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            ((EntityJointed)entity).receiveJointDisassemble(disassembleFacing, posX, posY, posZ);
         }
      } catch (NullPointerException var13) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateDisassembleJointOnClient:Will be fixed in future");
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void receiveAssemblyTuningActionAtClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         short jointRefId = data.readShort();
         byte initLayer = data.readByte();
         byte initParamIndex = data.readByte();
         short newValue = data.readShort();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            ((EntityJointed)entity).receiveTuningActionAtClient(jointRefId, initLayer, initParamIndex, newValue);
         }
      } catch (NullPointerException var10) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateDisassembleJointOnClient:Will be fixed in future");
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   private static void startJointReInitOnClient(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         byte initLayerMode = data.readByte();
         byte mainSubToggleFlg = data.readByte();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            ((EntityJointed)entity).receiveReInitOnClient(initLayerMode, mainSubToggleFlg);
         }
      } catch (NullPointerException var8) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateDisassembleJointOnClient:Will be fixed in future");
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   private static void receiveDummyExplosion(FriendlyByteBuf data) {
      try {
         double posX = data.readDouble();
         double posY = data.readDouble();
         double posZ = data.readDouble();
         float explosionSize = data.readFloat();
         boolean affectsBlocks = data.readBoolean();
         boolean affectsEntity = data.readBoolean();
         int cclistSize = data.readInt();
         ArrayList<BlockPos> affectCCList = new ArrayList<BlockPos>(cclistSize);

         for(int i1 = 0; i1 < cclistSize; ++i1) {
            int j1 = data.readByte() + (int)posX;
            int k1 = data.readByte() + (int)posY;
            int l1 = data.readByte() + (int)posZ;
            affectCCList.add(new BlockPos(j1, k1, l1));
         }

         float motX = data.readFloat();
         float motY = data.readFloat();
         float motZ = data.readFloat();
         ClientLevel worldClient = Minecraft.getInstance().level;
         ExplosionEx.ExplosionExtraAttribute extra = new ExplosionEx.ExplosionExtraAttribute(new Explosion(worldClient, (Entity)null, posX, posY, posZ, explosionSize, affectCCList), affectsBlocks, affectsEntity, true, true);
         ExplosionEx.doExplosionB(extra, worldClient, (Entity)null, posX, posY, posZ, explosionSize, affectCCList, true);
         Player thePlayer = Minecraft.getInstance().player;
         if (thePlayer != null) {
            Vec3 motion = thePlayer.getDeltaMovement();
            thePlayer.setDeltaMovement(motion.x + (double)motX, motion.y + (double)motY, motion.z + (double)motZ);
         }
      } catch (NullPointerException var20) {
         JointBlockRemastered.LOGGER.debug("[JointBlock]Expected Error In:updateEntityJointOnClient:Will be fixed in future");
      } catch (Exception var21) {
         var21.printStackTrace();
      }

   }

   private static void receiveRequestedBlueprintExportData(FriendlyByteBuf data) {
      try {
         long requestToken = data.readLong();
         boolean deflate = data.readBoolean();
         boolean breakStr = data.readBoolean();
         int dataLen = data.readInt();
         if (dataLen < 0) {
            return;
         }

         byte[] abyte = new byte[dataLen];
         data.readBytes(abyte);
         CompoundTag dataTag = JBCompressToolHelper.readDecompress(abyte, new NbtAccounter(2097152L));
         JBImportExportDataUtil.exportAssembleData(Minecraft.getInstance().player, requestToken, dataTag, deflate, breakStr);
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void receiveTargetSelectUpdate(FriendlyByteBuf data) {
      try {
         int entityId = data.readInt();
         int cnt = data.readInt();
         ClientLevel worldClient = Minecraft.getInstance().level;
         Entity entity = worldClient.getEntity(entityId);
         if (entity != null && entity instanceof EntityJointed) {
            EntityJointed ej = (EntityJointed)entity;
            ej.clearTargetList();
            cnt = Math.min(cnt, 5);
            if (cnt <= 0) {
               return;
            }

            for(int i = 0; i < cnt; ++i) {
               byte idx = data.readByte();
               EjTarget t = EjTarget.readFromBuffer(ej.level(), data);
               if (idx >= 1 && idx < ej.targetList.length) {
                  ej.targetList[idx] = t;
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }
}
