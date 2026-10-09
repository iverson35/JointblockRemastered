package jp.mc.ancientred.jointblock.packet;

import io.netty.buffer.Unpooled;
import java.util.Iterator;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.tuning.TuningCommand;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.export.JBCompressToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the 1.12.2 {@code JBPacketSender}.
 *
 * <p>{@code FMLProxyPacket} + {@code FMLEventChannel} are gone in 1.20.1: the payload is
 * written into a {@link FriendlyByteBuf} exactly as before and handed to
 * {@link JBNetwork}, which wraps it in a single {@link JBNetwork.Payload}. The original
 * "first byte = packet type" wire layout is preserved verbatim, and so are all method
 * signatures (only the argument types changed: {@code EntityPlayerMP}→{@link ServerPlayer},
 * {@code NBTTagCompound}→{@link CompoundTag}, {@code Vec3d}→{@link Vec3},
 * {@code BlockPos} list stays a list of positions).
 */
public class JBPacketSender {

   private static FriendlyByteBuf newBuffer() {
      return new FriendlyByteBuf(Unpooled.buffer());
   }

   public static void sendSettingSyncPacket(ServerPlayer player) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_SETTINGS_SYNC);
         buffef.writeByte(JBConfig.maxInitLayers);
         buffef.writeBoolean(JBConfig.bypathPlayerDamageToUnit);
         buffef.writeDouble(JBConfig.worldMaxEntityRadiusModifier);
         JBNetwork.sendTo(player, new JBNetwork.Payload(buffef));
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public static void sendRidingPlayerInputToServer(int keyFlagsA, int keyFlagsB) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_SEAT_OP);
         buffef.writeInt(keyFlagsA);
         buffef.writeInt(keyFlagsB);
         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static void sendCarvingActionToServer(int xCoord, int yCoord, int zCoord, int boxIndex, int subHitValue, int hitSide, boolean leftClick) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_CARVING_ACT);
         buffef.writeInt(xCoord);
         buffef.writeInt(yCoord);
         buffef.writeInt(zCoord);
         buffef.writeInt(boxIndex);
         buffef.writeInt(subHitValue);
         buffef.writeInt(hitSide);
         buffef.writeBoolean(leftClick);
         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void sendAssemblyTuningActionToServer(int entityId, int jointRefId, int additionalBarNum, int additionalBarOffset, int additionalBarSide) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_TUNING_ACT);
         buffef.writeInt(entityId);
         buffef.writeShort(jointRefId);
         buffef.writeByte(additionalBarNum);
         buffef.writeByte(additionalBarOffset);
         buffef.writeByte(additionalBarSide);
         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public static void sendAssemblyTuningActionToPlayers(EntityJointed entityJointed, TuningCommand tuningCommand) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_TUNING_ACT);
         buffef.writeInt(entityJointed.getId());
         buffef.writeShort(tuningCommand.jointRefId);
         buffef.writeByte(tuningCommand.initLayer);
         buffef.writeByte(tuningCommand.initParamIndex);
         buffef.writeShort(tuningCommand.newValue);
         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public static void sendImportedCarvingData(int[] carvingData) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_IMPORT_DATA);
         buffef.writeByte(JBNetwork.PACKET_SUBTYPE_IMPORT_CARVING_DATA);
         buffef.writeInt(carvingData.length);
         int len = carvingData.length;

         for(int i = 0; i < len; ++i) {
            buffef.writeInt(carvingData[i]);
         }

         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static void sendImportedPaletteData(int[][] paletteDataList) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_IMPORT_DATA);
         buffef.writeByte(JBNetwork.PACKET_SUBTYPE_IMPORT_PALETTE_DATA);
         JBCarvingDataUtil.writePaletteDataToBBuf(paletteDataList, buffef);
         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public static int sendBlueprintExportRequestToServer(long requestToken, boolean deflate, boolean breakStr) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_IMPORT_DATA);
         buffef.writeByte(JBNetwork.PACKET_SUBTYPE_IMPORT_EXPORT_REQUEST);
         buffef.writeLong(requestToken);
         buffef.writeBoolean(deflate);
         buffef.writeBoolean(breakStr);
         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
         return 0;
      } catch (Exception var7) {
         var7.printStackTrace();
         return 1;
      }

   }

   public static int sendBlueprintExportRequestedToClient(ServerPlayer player, long requestToken, boolean deflate, boolean breakStr, CompoundTag tag) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_EXPORT_REQUESTED);
         buffef.writeLong(requestToken);
         buffef.writeBoolean(deflate);
         buffef.writeBoolean(breakStr);
         if (tag == null) {
            buffef.writeInt(-1);
         } else {
            byte[] abyte = JBCompressToolHelper.compress(tag);
            if (abyte.length > 2096026) {
               return 2;
            }

            buffef.writeInt(abyte.length);
            buffef.writeBytes(abyte);
         }

         JBNetwork.sendTo(player, new JBNetwork.Payload(buffef));
         return 0;
      } catch (Exception var9) {
         var9.printStackTrace();
         return 1;
      }

   }

   public static int sendImportedAssembleData(CompoundTag tag, boolean seal, boolean processForSingleIS) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_IMPORT_DATA);
         buffef.writeByte(JBNetwork.PACKET_SUBTYPE_IMPORT_ASSEMBLE_DATA);
         buffef.writeBoolean(seal);
         buffef.writeBoolean(processForSingleIS);
         if (tag != null && !processForSingleIS) {
            byte[] abyte = JBCompressToolHelper.compress(tag);
            if (abyte.length > 31743) {
               return 2;
            }

            buffef.writeShort((short)abyte.length);
            buffef.writeBytes(abyte);
         } else {
            buffef.writeShort(-1);
         }

         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
         return 0;
      } catch (Exception var6) {
         var6.printStackTrace();
         return 1;
      }

   }

   public static boolean sendSavedRingDataToServer(CompoundTag tag, int itemIndex, boolean forGUI) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_RING_DATA);
         buffef.writeBoolean(forGUI);
         buffef.writeByte(itemIndex);
         if (tag == null) {
            buffef.writeShort(-1);
         } else {
            byte[] abyte = JBCompressToolHelper.compress(tag);
            if (abyte.length > 31743) {
               return false;
            }

            buffef.writeShort((short)abyte.length);
            buffef.writeBytes(abyte);
         }

         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
         return true;
      } catch (Exception var6) {
         var6.printStackTrace();
         return false;
      }

   }

   public static boolean sendTargetSelectPacketToServer(int sendCount, EjTarget[] targetList) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         sendCount = Math.min(sendCount, 5);
         buffef.writeByte(JBNetwork.PACKET_TYPE_TARGET_SELECT);
         buffef.writeInt(sendCount);

         for(int i = 0; i < targetList.length; ++i) {
            if (targetList[i] != null) {
               --sendCount;
               if (sendCount < 0) {
                  break;
               }

               targetList[i].writeToBuffer(buffef);
            }
         }

         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
         return true;
      } catch (Exception var5) {
         var5.printStackTrace();
         return false;
      }

   }

   public static void sendTargetSelectPacketToClient(EntityJointed entityJointed, int sendCount, EjTarget[] targetList) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         sendCount = Math.min(sendCount, 5);
         buffef.writeByte(JBNetwork.PACKET_TYPE_TARGET_SELECT);
         buffef.writeInt(entityJointed.getId());
         buffef.writeInt(sendCount);

         for(int i = 1; i < targetList.length; ++i) {
            if (targetList[i] != null) {
               --sendCount;
               if (sendCount < 0) {
                  break;
               }

               buffef.writeByte((byte)i);
               targetList[i].writeToBuffer2(buffef);
            }
         }

         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void sendCoreSyncToPlayers(EntityJointed entityJointed) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_CORE_SYNC);
         buffef.writeInt(entityJointed.getId());
         buffef.writeFloat(entityJointed.coreHeight);
         buffef.writeFloat(entityJointed.totalHeight);
         buffef.writeFloat(entityJointed.largeUnitWeight);
         buffef.writeByte(entityJointed.seatFlags);
         if (entityJointed.slaveAttatcher != null && entityJointed.slaveAttatcher.hasMasterReferenceServer(entityJointed)) {
            buffef.writeInt(entityJointed.slaveAttatcher.typeId);
            entityJointed.slaveAttatcher.writeSpawnData(buffef);
         } else {
            buffef.writeInt(0);
         }

         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public static void sendJointsDisassembleToPlayers(EntityJointed entityJointed, int disassembleFacing) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_JOINTDISASSEMBLE);
         buffef.writeInt(entityJointed.getId());
         buffef.writeInt(disassembleFacing);
         buffef.writeDouble(entityJointed.dissassemblePosX);
         buffef.writeDouble(entityJointed.dissassemblePosY);
         buffef.writeDouble(entityJointed.dissassemblePosZ);
         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public static void sendJointsReInitToPlayers(EntityJointed entityJointed, byte initLayerMode, byte mainSubToggleFlg) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_JOINTREINIT);
         buffef.writeInt(entityJointed.getId());
         buffef.writeByte(initLayerMode);
         buffef.writeByte(mainSubToggleFlg);
         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static JBNetwork.Payload createGUIActPacket(int id, int startIndex, int amount, String unitName) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_GUIACT);
         buffef.writeInt(id);
         buffef.writeInt(startIndex);
         buffef.writeInt(amount);
         if (unitName == null) {
            buffef.writeByte(-1);
         } else {
            byte[] bytes = JBCommonUtil.createByteAryFromNameString(unitName);
            if (bytes.length == 0) {
               buffef.writeByte(0);
            } else {
               buffef.writeByte(bytes.length);
               buffef.writeBytes(bytes, 0, bytes.length);
            }
         }

         return new JBNetwork.Payload(buffef);
      } catch (Exception var12) {
         var12.printStackTrace();
         return null;
      }

   }

   public static JBNetwork.Payload createPaletteGUIActPacket(int id, int posX, int posY, int posZ, int[][] paletteDataList) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_PALETTE_GUIACT);
         buffef.writeInt(id);
         buffef.writeInt(posX);
         buffef.writeInt(posY);
         buffef.writeInt(posZ);
         JBCarvingDataUtil.writePaletteDataToBBuf(paletteDataList, buffef);
         return new JBNetwork.Payload(buffef);
      } catch (Exception var8) {
         var8.printStackTrace();
         return null;
      }

   }

   public static JBNetwork.Payload createSoundGUIActPacket(int wid, int id, int slotIndex, EffectSlot effectSlot) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_SOUND_GUIACT);
         buffef.writeInt(wid);
         buffef.writeInt(slotIndex);
         effectSlot.writeToBuffer(buffef, false);
         return new JBNetwork.Payload(buffef);
      } catch (Exception var7) {
         var7.printStackTrace();
         return null;
      }

   }

   public static JBNetwork.Payload createMirrorGUIActPacket(int id, int posX, int posY, int posZ) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_MIRROR_GUIACT);
         buffef.writeInt(id);
         buffef.writeInt(posX);
         buffef.writeInt(posY);
         buffef.writeInt(posZ);
         return new JBNetwork.Payload(buffef);
      } catch (Exception var7) {
         var7.printStackTrace();
         return null;
      }

   }

   public static JBNetwork.Payload createParamCpyGUIActPacket(int flags, int posX, int posY, int posZ) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_PCOPY_GUIACT);
         buffef.writeInt(flags);
         buffef.writeInt(posX);
         buffef.writeInt(posY);
         buffef.writeInt(posZ);
         return new JBNetwork.Payload(buffef);
      } catch (Exception var7) {
         var7.printStackTrace();
         return null;
      }

   }

   public static void sendJointGuiStringChanged(String strToSend, byte strType, int extra) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_STRINGCHNGED);
         buffef.writeByte(strType);
         byte[] bytes;
         switch (strType) {
            case JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_UNITNAME:
               bytes = JBCommonUtil.createByteAryFromNameString(strToSend);
               break;
            case JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME:
               buffef.writeByte(extra);
               bytes = JBCommonUtil.createByteAryFromSoundNameString(strToSend);
               break;
            case JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR:
            default:
               bytes = JBCommonUtil.createByteAryFromMemoString(strToSend);
               break;
         }

         if (bytes.length == 0) {
            buffef.writeByte(0);
         } else {
            buffef.writeByte(bytes.length);
            buffef.writeBytes(bytes, 0, bytes.length);
         }

         JBNetwork.sendToServer(new JBNetwork.Payload(buffef));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void sendDummyExplosionToPlayer(ServerPlayer playerMP, double posX, double posY, double posZ, float explosionSize, boolean affectsBlocks, boolean affectsEntity, List<BlockPos> ccList, Vec3 playerMot) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_DUMMY_EXPLODE);
         buffef.writeDouble(posX);
         buffef.writeDouble(posY);
         buffef.writeDouble(posZ);
         buffef.writeFloat(explosionSize);
         buffef.writeBoolean(affectsBlocks);
         buffef.writeBoolean(affectsEntity);
         buffef.writeInt(ccList.size());
         Iterator<BlockPos> iterator = ccList.iterator();

         while(iterator.hasNext()) {
            BlockPos chunkposition = iterator.next();
            int cpX = chunkposition.getX() - (int)posX;
            int ppY = chunkposition.getY() - (int)posY;
            int cpZ = chunkposition.getZ() - (int)posZ;
            buffef.writeByte(cpX);
            buffef.writeByte(ppY);
            buffef.writeByte(cpZ);
         }

         if (playerMot != null) {
            buffef.writeFloat((float)playerMot.x);
            buffef.writeFloat((float)playerMot.y);
            buffef.writeFloat((float)playerMot.z);
         } else {
            buffef.writeFloat(0.0F);
            buffef.writeFloat(0.0F);
            buffef.writeFloat(0.0F);
         }

         JBNetwork.sendTo(playerMP, new JBNetwork.Payload(buffef));
      } catch (Exception var22) {
         var22.printStackTrace();
      }

   }

   public static void sendJointsUpdateToPlayers(EntityJointed entityJointed) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_JOINTUPDATE);
         buffef.writeInt(entityJointed.getId());
         buffef.writeFloat(entityJointed.walkAnimation.position());
         buffef.writeFloat(entityJointed.getYRot());
         buffef.writeShort(entityJointed.updateList.index);
         buffef.writeBytes(entityJointed.updateList.list, 0, entityJointed.updateList.index);
         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public static void sendEntityJBSoundToPlayers(EntityJointed entityJointed, int soundSlotNum) {
      FriendlyByteBuf buffef = newBuffer();

      try {
         buffef.writeByte(JBNetwork.PACKET_TYPE_SOUND_ENTITYEVENT);
         buffef.writeInt(entityJointed.getId());
         buffef.writeByte(soundSlotNum);
         JBNetwork.sendToPlayersAround(entityJointed.level().dimension(), entityJointed.getX(), entityJointed.getY(), entityJointed.getZ(), new JBNetwork.Payload(buffef));
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }
}
