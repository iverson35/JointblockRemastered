package jp.mc.ancientred.jointblock.packet;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.export.AssembleNBTDataUtil;
import jp.mc.ancientred.jointblock.export.JBCompressToolHelper;
import jp.mc.ancientred.jointblock.gui.ContainerJointBlockExtra;
import jp.mc.ancientred.jointblock.gui.ContainerJointCore;
import jp.mc.ancientred.jointblock.gui.ContainerJointCoreSounds;
import jp.mc.ancientred.jointblock.gui.IContainerActionReceiver;
import jp.mc.ancientred.jointblock.item.AbstractCarvingToolItem;
import jp.mc.ancientred.jointblock.item.ItemJBAssemblyTuner;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemJBMirrorTool;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the 1.12.2 {@code JBPacketHandlerServer}.
 *
 * <p>{@code FMLNetworkEvent.ServerCustomPacketEvent} does not exist in 1.20.1: the raw
 * payload arrives from {@link JBNetwork} through a {@code consumerMainThread} handler,
 * so {@link #handleServerPacket} already runs on the server thread - 1.12.2 reached the
 * same state with {@code WorldServer.isCallingFromMinecraftThread()} /
 * {@code addScheduledTask()}.
 */
public class JBPacketHandlerServer {

   protected static TileEntityJoint getTileEntityJointedSafe(ServerPlayer player, int posX, int posY, int posZ) {
      BlockPos pos = new BlockPos(posX, posY, posZ);
      return player.level().hasChunkAt(pos) ? BlockJointBase.getTileEntityJointerBase(player.level(), pos) : null;
   }

   /** 1.12.2 {@code onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent)}. */
   public static void handleServerPacket(JBNetwork.Payload message, ServerPlayer thePlayer) {
      if (thePlayer == null) {
         return;
      }
      switchByPacketType(message.payload(), thePlayer);
   }

   public static final void switchByPacketType(FriendlyByteBuf packet, ServerPlayer thePlayer) {
      byte packetType = -1;

      try {
         packetType = packet.readByte();
         if (packetType == JBNetwork.PACKET_TYPE_SEAT_OP) {
            if (thePlayer != null) {
               receiveRidingPlayerInput(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_GUIACT) {
            if (thePlayer != null && thePlayer.containerMenu instanceof IContainerActionReceiver) {
               IContainerActionReceiver container = (IContainerActionReceiver)thePlayer.containerMenu;
               container.receiveButtonAction(thePlayer, packet.readInt(), packet.readInt(), packet.readInt());
               return;
            }

            if (thePlayer != null && thePlayer.containerMenu instanceof ContainerJointCore) {
               ContainerJointCore container = (ContainerJointCore)thePlayer.containerMenu;
               container.receiveButtonAction(packet.readInt(), packet.readInt(), packet.readInt(), getStringFromPacket(packet));
               return;
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_STRINGCHNGED) {
            if (thePlayer != null) {
               byte subType = packet.readByte();
               if (subType == JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_UNITNAME && thePlayer.containerMenu instanceof ContainerJointCore) {
                  ContainerJointCore container = (ContainerJointCore)thePlayer.containerMenu;
                  container.receiveUnitNameUpdate(getStringFromPacket(packet));
               } else if (subType == JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR && thePlayer.containerMenu instanceof ContainerJointBlockExtra) {
                  ContainerJointBlockExtra container = (ContainerJointBlockExtra)thePlayer.containerMenu;
                  container.receiveMemoStrUpdate(getStringFromPacket(packet));
               } else if (subType == JBNetwork.PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME && thePlayer.containerMenu instanceof ContainerJointCoreSounds) {
                  ContainerJointCoreSounds container = (ContainerJointCoreSounds)thePlayer.containerMenu;
                  int actualSlotNum = packet.readByte();
                  container.receiveSoundNameStrUpdate(actualSlotNum, getStringFromPacket(packet));
               }
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_TUNING_ACT) {
            if (thePlayer != null) {
               receiveAssemblyTuningAction(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_PALETTE_GUIACT) {
            if (thePlayer != null) {
               receivePaletteGUIUpdate(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_SOUND_GUIACT) {
            if (thePlayer != null) {
               receiveSoundGUIUpdate(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_TARGET_SELECT) {
            if (thePlayer != null) {
               receiveTargetSelectUpdate(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_MIRROR_GUIACT) {
            if (thePlayer != null) {
               receiveMirrorGUIUpdate(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_PCOPY_GUIACT) {
            if (thePlayer != null) {
               receiveParamCopyGUIUpdate(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_CARVING_ACT) {
            if (thePlayer != null) {
               receivePlayerCarveAction(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_RING_DATA) {
            if (thePlayer != null) {
               receiveSavedRingData(packet, thePlayer);
            }

            return;
         }

         if (packetType == JBNetwork.PACKET_TYPE_IMPORT_DATA) {
            if (thePlayer != null) {
               handleImportPacket(packet, thePlayer);
            }

            return;
         }
      } catch (Exception var8) {
         JointBlockRemastered.LOGGER.debug("Error handling JB SERVER-CLIENT Packet :{}", packetType, var8);
      }

   }

   private static void handleImportPacket(FriendlyByteBuf packet, ServerPlayer player) {
      byte packetSubType = packet.readByte();
      if (packetSubType == JBNetwork.PACKET_SUBTYPE_IMPORT_CARVING_DATA) {
         if (!JBConfig.enableDataImport) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.notallowed"), false);
         } else {
            receivePlayerImportedCarvingData(packet, player);
         }
      } else if (packetSubType == JBNetwork.PACKET_SUBTYPE_IMPORT_PALETTE_DATA) {
         if (!JBConfig.enableDataImport) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.notallowed"), false);
         } else {
            receivePlayerImportedPaletteData(packet, player);
         }
      } else if (packetSubType == JBNetwork.PACKET_SUBTYPE_IMPORT_ASSEMBLE_DATA) {
         if (!JBConfig.enableDataImport) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.notallowed"), false);
         } else {
            receivePlayerImportedAssembleData(packet, player);
         }
      } else if (packetSubType == JBNetwork.PACKET_SUBTYPE_IMPORT_EXPORT_REQUEST) {
         if (!JBConfig.enableDataExport) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbed.error.notallowed"), false);
         } else {
            receivePlayerExportRequest(packet, player);
         }
      }
   }

   private static String getStringFromPacket(ByteBuf payload) {
      try {
         byte len = payload.readByte();
         if (len == -1) {
            return null;
         } else if (len == 0) {
            return "";
         } else {
            byte[] data = new byte[len];
            payload.readBytes(data, 0, len);
            return new String(data, StandardCharsets.UTF_8);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         return "";
      }
   }

   private static void receiveRidingPlayerInput(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int flagsA = data.readInt();
         int flagsB = data.readInt();
         Entity ridingEntity = player.getVehicle();
         if (ridingEntity instanceof EntitySeatPart) {
            EntitySeatPart seatPart = (EntitySeatPart)ridingEntity;
            if (!seatPart.isRemoved() && seatPart.hasPassenger(player)) {
               seatPart.receivedPlayerOP(flagsA, flagsB);
            }
         } else {
            InteractionHand[] var6 = InteractionHand.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               InteractionHand hand = var6[var8];
               ItemStack current = player.getItemInHand(hand);
               if (ItemJBController.isItemStackJBController(current)) {
                  EntityJointed ej = ItemJBController.getRelatedEntityJointedServer(current, player.level(), player);
                  if (ej == null || ej.isRemoved() || !ej.isPlayerUnitOwner(player)) {
                     return;
                  }

                  EntitySeatPart seatPart = ej.entitySeatPart0;
                  if (seatPart != null && !seatPart.isRemoved() && CompatHelper.getRiddenByEntity(seatPart) == null && seatPart.isAsInteractableJoint) {
                     seatPart.receivedPlayerOP(flagsA, flagsB);
                     seatPart.keyConductor = player;
                  }
               }
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   private static void receivePlayerCarveAction(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int posX = data.readInt();
         int posY = data.readInt();
         int posZ = data.readInt();
         int boxIndex = data.readInt();
         int subHitValue = data.readInt();
         int hitSide = data.readInt();
         boolean leftClick = data.readBoolean();
         TileEntityJoint te = getTileEntityJointedSafe(player, posX, posY, posZ);
         if (te != null && te.isValidCarveable()) {
            ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
            if (currentItem != null && currentItem.getItem() instanceof AbstractCarvingToolItem) {
               AbstractCarvingToolItem item = (AbstractCarvingToolItem)currentItem.getItem();
               item.processEditOnServer(currentItem, player, te, boxIndex, subHitValue, hitSide, leftClick);
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   private static void receivePlayerImportedCarvingData(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int dataLen = data.readInt();
         int maxDataLen = Math.max(JBCarvingDataUtil.getCVDataLenFromDiv(0, 16), JBCarvingDataUtil.getCVDataLenFromDiv(2, 5) + 8);
         if (dataLen < 2 || dataLen > maxDataLen) {
            return;
         }

         int[] carvingData = new int[dataLen];

         int carvingType;
         for(carvingType = 0; carvingType < dataLen; ++carvingType) {
            carvingData[carvingType] = data.readInt();
         }

         if (!JBCarvingDataUtil.validateCarvingData(carvingData)) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            return;
         }

         carvingType = JBCarvingDataUtil.getCarvingType(carvingData);
         int carvingDivide = JBCarvingDataUtil.getCarvingDivide(carvingData);
         boolean isDivideTooLarge;
         switch (carvingType) {
            case 2:
               isDivideTooLarge = carvingDivide > Math.min(JBConfig.maxCarvingDivide, 5);
               break;
            case 4:
               isDivideTooLarge = carvingDivide > Math.min(JBConfig.maxCarvingDivide, 5);
               break;
            default:
               isDivideTooLarge = carvingDivide > Math.min(JBConfig.maxCarvingDivide, 16);
         }

         if (isDivideTooLarge) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.exceededlimit"), false);
            return;
         }

         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (!CompatHelper.isStackEmptyOrNull(currentItemStack) && ItemJBCopyTool.isCopyMoldTool(currentItemStack)) {
            currentItemStack.getOrCreateTag().putIntArray("jb:cdata", carvingData);
            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbid.succeed"), false);
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   private static void receivePlayerImportedPaletteData(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int[][] pdataList = new int[6][];
         JBCarvingDataUtil.readPaletteDataFromBBuf(pdataList, data);
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (!CompatHelper.isStackEmptyOrNull(currentItemStack) && ItemJBCopyTool.isPaletteTool(currentItemStack)) {
            JBCarvingDataUtil.setPaletteDataToNBT(pdataList, "jb:pdata", currentItemStack.getOrCreateTag());
            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbid.succeed"), false);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private static void receivePlayerImportedAssembleData(FriendlyByteBuf packet, ServerPlayer player) {
      try {
         boolean seal = packet.readBoolean();
         boolean processForSingleIS = packet.readBoolean();
         CompoundTag dataTag;
         if (processForSingleIS) {
            packet.readShort();
            dataTag = JBModContainer.proxy.getNBTImportDataAsSingle(player);
            if (dataTag == null) {
               return;
            }
         } else {
            dataTag = JBCompressToolHelper.readNBTTagCompoundFromBuffer(packet);
         }

         int partsCount = dataTag.getInt("tnm");
         if (partsCount > JBConfig.maxImportablePartsCount) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.exceededlimit"), false);
            return;
         }

         if (!AssembleNBTDataUtil.validateAndFixAssembleDataTag(dataTag, false)) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            return;
         }

         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (!CompatHelper.isStackEmptyOrNull(currentItemStack) && ItemJBBlueprint.isBlueprint(currentItemStack)) {
            if (ItemJBBlueprint.isSealed(currentItemStack)) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.bpsealed"), false);
               return;
            }

            CompoundTag stackTag = currentItemStack.getOrCreateTag();
            String unitName = ItemJBBlueprint.getNameFromCoreAddtionalInfo(dataTag);
            if (unitName != null && unitName.length() > 0) {
               dataTag.putString("name", unitName);
            }

            if (ItemJBBlueprint.isTemporaryBlueprint(currentItemStack)) {
               ItemJBBlueprint.saveImportedBpDataToMemroyHeapWithNewId(dataTag);
            }

            stackTag.put("jb:blueprint", dataTag);
            if (seal) {
               stackTag.putBoolean("jb:seal", true);
            }

            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbid.succeed"), false);
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   private static void receivePlayerExportRequest(FriendlyByteBuf packet, ServerPlayer player) {
      try {
         long requestToken = packet.readLong();
         boolean deflate = packet.readBoolean();
         boolean breakStr = packet.readBoolean();
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (!CompatHelper.isStackEmptyOrNull(currentItemStack) && ItemJBBlueprint.isBlueprint(currentItemStack) && currentItemStack.hasTag()) {
            if (ItemJBBlueprint.isSealed(currentItemStack)) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.bpsealed"), false);
            } else {
               CompoundTag tag = currentItemStack.getTag();
               if (tag != null && tag.contains("jb:blueprint")) {
                  CompoundTag dataTag = tag.getCompound("jb:blueprint");
                  int[] serialized = null;
                  CompoundTag coreSubTag = null;
                  if (dataTag.contains("ser")) {
                     serialized = dataTag.getIntArray("ser");
                     if (dataTag.contains("crinf")) {
                        coreSubTag = dataTag.getCompound("crinf");
                     }
                  } else if (dataTag.contains("msav")) {
                     CompoundTag blueprintDataNBT = ItemJBBlueprint.tryGetBlueprintNBTFromMemroyHeap(dataTag);
                     if (blueprintDataNBT == null) {
                        player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.cantfinddata"), false);
                        return;
                     }

                     serialized = blueprintDataNBT.getIntArray("ser");
                     if (blueprintDataNBT.contains("crinf")) {
                        coreSubTag = blueprintDataNBT.getCompound("crinf");
                     }
                  }

                  if (serialized == null) {
                     player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.cantfinddata"), false);
                  } else {
                     dataTag.putIntArray("ser", serialized);
                     if (coreSubTag != null) {
                        dataTag.put("crinf", coreSubTag);
                     }

                     if (JBPacketSender.sendBlueprintExportRequestedToClient(player, requestToken, deflate, breakStr, dataTag) == 2) {
                        player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.datatoolarge"), false);
                     }
                  }
               } else {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.wrongitem"), false);
               }
            }
         } else {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.wrongitem"), false);
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void receiveAssemblyTuningAction(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int entityId = data.readInt();
         short jointRefId = data.readShort();
         byte additionalBarNum = data.readByte();
         byte additionalBarOffset = data.readByte();
         byte additionalBarSide = data.readByte();
         ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
         if (!player.isRemoved() && currentItem != null && currentItem.getItem() instanceof ItemJBAssemblyTuner) {
            Entity entity = player.level().getEntity(entityId);
            if (entity != null && !entity.isRemoved() && entity instanceof EntityJointed) {
               EntityJointed entityJointed = (EntityJointed)entity;
               if (entityJointed.isPlayerUnitOwner(player)) {
                  entityJointed.receiveTuningAction(player, jointRefId, additionalBarNum, additionalBarOffset, additionalBarSide);
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   public static void receivePaletteGUIUpdate(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int id = data.readInt();
         int posX = data.readInt();
         int posY = data.readInt();
         int posZ = data.readInt();
         int[][] pdataList = new int[6][];
         JBCarvingDataUtil.readPaletteDataFromBBuf(pdataList, data);
         ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
         if (ItemJBCopyTool.isPaletteTool(currentItem)) {
            TileEntityJoint te = getTileEntityJointedSafe(player, posX, posY, posZ);
            if (te instanceof TileEntityJointCore) {
               TileEntityJointCore teCore = (TileEntityJointCore)te;
               if (Vec3.atCenterOf(teCore.getBlockPos()).distanceToSqr(player.getX(), player.getY(), player.getZ()) < 512.0) {
                  teCore.paletteDataList = pdataList;
                  teCore.markUpdateThis();
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   public static void receiveSoundGUIUpdate(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int wid = data.readInt();
         int slotIndex = data.readInt();
         EffectSlot slot = new EffectSlot();
         slot.readFromBuffer(data, false);
         if (player.containerMenu instanceof ContainerJointCoreSounds && player.containerMenu.containerId == wid) {
            ((ContainerJointCoreSounds)player.containerMenu).updateSlotFromPacket(slotIndex, slot);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public static void receiveTargetSelectUpdate(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int cnt = data.readInt();
         Entity ridingEntity = player.getVehicle();
         if (ridingEntity instanceof EntitySeatPart) {
            EntitySeatPart seatPart = (EntitySeatPart)ridingEntity;
            if (!seatPart.isRemoved() && seatPart.entityJointed != null && !seatPart.entityJointed.isRemoved() && seatPart.hasPassenger(player)) {
               EntityJointed ej = seatPart.entityJointed;
               cnt = Math.min(cnt, 5);

               for(int i = 1; i < ej.targetList.length; ++i) {
                  --cnt;
                  if (cnt >= 0) {
                     ej.targetList[i] = EjTarget.readFromBuffer(ej.level(), data);
                  } else {
                     ej.targetList[i] = null;
                  }
               }

               ej.needToSendTargetUpdate = true;
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public static void receiveParamCopyGUIUpdate(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int flags = data.readInt();
         int posX = data.readInt();
         int posY = data.readInt();
         int posZ = data.readInt();
         ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
         if (ItemJBCopyTool.isParamCpyTool(currentItem)) {
            TileEntityJoint te = getTileEntityJointedSafe(player, posX, posY, posZ);
            if (te != null && Vec3.atCenterOf(te.getBlockPos()).distanceToSqr(player.getX(), player.getY(), player.getZ()) < 512.0) {
               te.copyValueFromParamCpy(player, currentItem, flags);
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public static void receiveMirrorGUIUpdate(FriendlyByteBuf data, ServerPlayer player) {
      try {
         int id = data.readInt();
         int posX = data.readInt();
         int posY = data.readInt();
         int posZ = data.readInt();
         ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
         if (currentItem != null && currentItem.getItem() instanceof ItemJBMirrorTool) {
            TileEntityJoint te = getTileEntityJointedSafe(player, posX, posY, posZ);
            te.conductMirrorCopyAllChildes(id);
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   private static void receiveSavedRingData(FriendlyByteBuf packet, ServerPlayer player) {
      try {
         boolean forGUI = packet.readBoolean();
         int itemIndex = packet.readByte();
         CompoundTag dataTag = JBCompressToolHelper.readNBTTagCompoundFromBuffer(packet);
         if (!AssembleNBTDataUtil.validateRINGDataTag(dataTag, true)) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            return;
         }

         int currentItemIndex = player.getInventory().selected;
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (currentItemIndex == itemIndex && ItemRingProgram.isRingProgramItem(currentItemStack)) {
            ItemRingProgram.setRingProgramFromTag(currentItemStack, dataTag);
            if (!forGUI) {
               player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbid.succeed"), false);
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }
}
