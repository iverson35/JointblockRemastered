package jp.mc.ancientred.jointblock.export;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBBase64Util;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the 1.12.2 {@code JBImportExportDataUtil}: clipboard (Base64) import/export of
 * carving, palette, blueprint and ring-program data.
 *
 * <p>1.12.2 hooked the client commands through Forge's {@code CommandEvent}
 * ({@code importJBData(CommandEvent)} / {@code exportJBData(CommandEvent)}). 1.20.1 has no
 * such event, so both entry points now take the invoking {@link Player} plus the command
 * arguments (same argument semantics: {@code "s"}/{@code "seal"} for import, {@code "c"}
 * (deflate) and {@code "b"} (break lines) for export) and are called by the Brigadier
 * client commands. Chat feedback is a {@link net.minecraft.network.chat.Component}.
 *
 * <p>{@code importAssembleData(EntityPlayer, boolean)} could not be decompiled; it was
 * reconstructed from the class file bytecode — see the method comment.
 *
 * <p>The clipboard is read/written through the game's own clipboard
 * ({@code Minecraft#keyboardHandler}, i.e. GLFW) instead of 1.12.2's {@code java.awt.Toolkit}:
 * the 1.20.1 client JVM runs headless ({@code java.awt.headless=true}), so every AWT clipboard
 * call threw {@code HeadlessException} and {@code /jbid} answered "no data on the clipboard"
 * while {@code /jbed} silently wrote nothing. GLFW's clipboard is main-thread only, so the
 * single-player shortcut (where the integrated server used to read the clipboard itself) now
 * receives the already parsed tag — see {@link #takeImportedAssembleDataForSingle()}.
 */
public class JBImportExportDataUtil {
   public static final byte FORMAT_CARVING_NORMAL = 1;
   public static final byte FORMAT_CARVING_DEFLATE = 2;
   public static final byte FORMAT_PALETTE_NORMAL = 3;
   public static final byte FORMAT_PALETTE_DEFLATE = 4;
   public static final byte FORMAT_ASSEMBLE_DEFLATE = 5;
   public static final byte FORMAT_RING_DEFLATE = 7;
   public static final int SEND_SUCCESS = 0;
   public static final int SEND_EXCEPTION = 1;
   public static final int SEND_TOO_LARGE = 2;
   public static Long s_requestToken = null;
   /**
    * Single-player hand-off: the tag parsed from the client clipboard by {@link #importAssembleData},
    * consumed by the integrated server thread through {@code CommonProxy#getNBTImportDataAsSingle}
    * (it cannot read the GLFW clipboard itself).
    */
   private static CompoundTag s_importedAssembleDataForSingle = null;

   public static void importJBData(Player player, String[] params) {
      if (!player.level().isClientSide) {
         return;
      }

      ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
      if (!CompatHelper.isStackEmptyOrNull(currentItemStack)) {
         if (ItemJBCopyTool.isCopyMoldTool(currentItemStack)) {
            importCarvingData(player);
            return;
         }

         if (ItemJBCopyTool.isPaletteTool(currentItemStack)) {
            importPaletteData(player);
            return;
         }

         if (ItemJBBlueprint.isBlueprint(currentItemStack)) {
            if (ItemJBBlueprint.isSealed(currentItemStack)) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.bpsealed"), false);
               return;
            }

            importAssembleData(player, commandHasSeal(params));
            return;
         }

         if (ItemRingProgram.isRingProgramItem(currentItemStack)) {
            importRINGData(player);
            return;
         }
      }

      player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.wrongitem"), false);
   }

   private static boolean commandHasSeal(String[] params) {
      return params != null && params.length >= 1 && ("s".equals(params[0]) || "seal".equals(params[0]));
   }

   public static void exportJBData(Player player, String[] params) {
      if (!player.level().isClientSide) {
         return;
      }

      boolean deflate = false;
      boolean breakString = false;
      if (params != null) {
         for(int i = 0; i < params.length; ++i) {
            if ("c".equals(params[0])) {
               deflate = true;
            }

            if ("b".equals(params[0])) {
               breakString = true;
            }
         }
      }

      ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
      if (!CompatHelper.isStackEmptyOrNull(currentItemStack)) {
         if (ItemJBCopyTool.isCopyMoldTool(currentItemStack)) {
            exportCarvingData(player, deflate, breakString);
            return;
         }

         if (ItemJBCopyTool.isPaletteTool(currentItemStack)) {
            exportPaletteData(player, deflate, breakString);
            return;
         }

         if (ItemJBBlueprint.isBlueprint(currentItemStack)) {
            s_requestToken = player.level().random.nextLong();
            JBPacketSender.sendBlueprintExportRequestToServer(s_requestToken, deflate, breakString);
            return;
         }

         if (ItemRingProgram.isRingProgramItem(currentItemStack)) {
            exportRINGData(player, breakString);
            return;
         }
      }

      player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.wrongitem"), false);
   }

   private static void importCarvingData(Player player) {
      try {
         byte[] byteAry = getValidClipboardData(player);
         if (byteAry != null) {
            byte format = byteAry[0];
            byte[] carvingByteData;
            int len;
            if (format == FORMAT_CARVING_NORMAL) {
               carvingByteData = new byte[byteAry.length - 1];
               System.arraycopy(byteAry, 1, carvingByteData, 0, carvingByteData.length);
            } else {
               if (format != FORMAT_CARVING_DEFLATE) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
                  return;
               }

               Inflater inflater = new Inflater();
               inflater.setInput(byteAry, 1, byteAry.length - 1);
               byte[] outBuffer = new byte[5000];
               len = inflater.inflate(outBuffer);
               if (!inflater.finished()) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.toolarge"), false);
                  return;
               }

               carvingByteData = new byte[len];
               System.arraycopy(outBuffer, 0, carvingByteData, 0, len);
            }

            if ((carvingByteData.length & 3) != 0) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            } else {
               int carvingDataLen = carvingByteData.length >> 2;
               if (carvingDataLen < 2) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
               } else {
                  int[] carvingData = new int[carvingDataLen];
                  len = carvingData.length;
                  int index = 0;

                  for(int i = 0; i < len; ++i) {
                     int data = 0;
                     data |= (carvingByteData[index++] & 255) << 24;
                     data |= (carvingByteData[index++] & 255) << 16;
                     data |= (carvingByteData[index++] & 255) << 8;
                     data |= carvingByteData[index++] & 255;
                     carvingData[i] = data;
                  }

                  if (!JBCarvingDataUtil.validateCarvingData(carvingData)) {
                     player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
                  } else {
                     JBPacketSender.sendImportedCarvingData(carvingData);
                  }
               }
            }
         }
      } catch (Exception var10) {
         handleUnknownImportError(player, var10);
      }
   }

   private static void exportCarvingData(Player player, boolean deflate, boolean breakString) {
      try {
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (currentItemStack.hasTag() && currentItemStack.getTag().contains("jb:cdata")) {
            CompoundTag tag = currentItemStack.getTag();
            int[] carvingData = tag.getIntArray("jb:cdata");
            int carvingDiv = JBCarvingDataUtil.getCarvingDivide(carvingData);
            if (carvingDiv < 1) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbed.error.nodata"), false);
            }

            byte[] byteAry = new byte[carvingData.length * 4];
            int len = carvingData.length;
            int index = 0;

            int compressedDataLength;
            for(int i = 0; i < len; ++i) {
               compressedDataLength = carvingData[i];
               byteAry[index++] = (byte)(compressedDataLength >> 24 & 255);
               byteAry[index++] = (byte)(compressedDataLength >> 16 & 255);
               byteAry[index++] = (byte)(compressedDataLength >> 8 & 255);
               byteAry[index++] = (byte)(compressedDataLength & 255);
            }

            byte[] exportDataAry;
            if (!deflate) {
               exportDataAry = new byte[byteAry.length + 1];
               System.arraycopy(byteAry, 0, exportDataAry, 1, byteAry.length);
               exportDataAry[0] = FORMAT_CARVING_NORMAL;
            } else {
               Deflater deflater = new Deflater(9, false);
               deflater.setInput(byteAry);
               deflater.finish();
               byte[] outputAry = new byte[byteAry.length + 200];
               compressedDataLength = deflater.deflate(outputAry);
               exportDataAry = new byte[compressedDataLength + 1];
               System.arraycopy(outputAry, 0, exportDataAry, 1, compressedDataLength);
               exportDataAry[0] = FORMAT_CARVING_DEFLATE;
            }

            String base64string;
            if (breakString) {
               base64string = JBBase64Util.toBase64WrappedChunkString(exportDataAry);
            } else {
               base64string = JBBase64Util.toBase64FlatChunkString(exportDataAry);
            }

            setClipboardString(base64string);
            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbed.succeed"), false);
         } else {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbed.error.nodata"), false);
         }
      } catch (Exception var12) {
         handleUnknownExportError(player, var12);
      }
   }

   private static void importPaletteData(Player player) {
      try {
         byte[] byteAry = getValidClipboardData(player);
         if (byteAry != null) {
            byte format = byteAry[0];
            byte[] paletteByteData;
            int i;
            if (format == FORMAT_PALETTE_NORMAL) {
               paletteByteData = new byte[byteAry.length - 1];
               System.arraycopy(byteAry, 1, paletteByteData, 0, paletteByteData.length);
            } else {
               if (format != FORMAT_PALETTE_DEFLATE) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
                  return;
               }

               Inflater inflater = new Inflater();
               inflater.setInput(byteAry, 1, byteAry.length - 1);
               byte[] outBuffer = new byte[5000];
               i = inflater.inflate(outBuffer);
               if (!inflater.finished()) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.toolarge"), false);
                  return;
               }

               paletteByteData = new byte[i];
               System.arraycopy(outBuffer, 0, paletteByteData, 0, i);
            }

            int[][] paletteDataList = new int[6][];
            ByteBuf bbuf = Unpooled.buffer();
            bbuf.writeBytes(paletteByteData);
            JBCarvingDataUtil.readPaletteDataFromBBuf(paletteDataList, bbuf);

            for(i = 0; i < paletteDataList.length; ++i) {
               if (paletteDataList[i] != null && paletteDataList[i].length != 8) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
                  return;
               }
            }

            JBPacketSender.sendImportedPaletteData(paletteDataList);
         }
      } catch (Exception var7) {
         handleUnknownImportError(player, var7);
      }
   }

   private static void exportPaletteData(Player player, boolean deflate, boolean breakString) {
      try {
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         if (currentItemStack.hasTag() && currentItemStack.getTag().contains("jb:pdata")) {
            CompoundTag tag = currentItemStack.getTag();
            int[][] paletteDataList = new int[6][];
            JBCarvingDataUtil.getPaletteDataFromNBT(paletteDataList, "jb:pdata", tag);

            for(int i = 0; i < paletteDataList.length; ++i) {
               if (paletteDataList[i] != null && paletteDataList[i].length != 8) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
                  return;
               }
            }

            ByteBuf bbuf = Unpooled.buffer();
            JBCarvingDataUtil.writePaletteDataToBBuf(paletteDataList, bbuf);
            byte[] byteAry = Arrays.copyOf(bbuf.array(), bbuf.writerIndex());
            byte[] exportDataAry;
            if (!deflate) {
               exportDataAry = new byte[byteAry.length + 1];
               System.arraycopy(byteAry, 0, exportDataAry, 1, byteAry.length);
               exportDataAry[0] = FORMAT_PALETTE_NORMAL;
            } else {
               Deflater deflater = new Deflater(9, false);
               deflater.setInput(byteAry);
               deflater.finish();
               byte[] outputAry = new byte[byteAry.length + 500];
               int compressedDataLength = deflater.deflate(outputAry);
               exportDataAry = new byte[compressedDataLength + 1];
               System.arraycopy(outputAry, 0, exportDataAry, 1, compressedDataLength);
               exportDataAry[0] = FORMAT_PALETTE_DEFLATE;
            }

            String base64string;
            if (breakString) {
               base64string = JBBase64Util.toBase64WrappedChunkString(exportDataAry);
            } else {
               base64string = JBBase64Util.toBase64FlatChunkString(exportDataAry);
            }

            setClipboardString(base64string);
            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbed.succeed"), false);
         } else {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbed.error.nodata"), false);
         }
      } catch (Exception var12) {
         handleUnknownExportError(player, var12);
      }
   }

   private static void importRINGData(Player player) {
      try {
         byte[] byteAry = getValidClipboardData(player);
         if (byteAry != null) {
            byte format = byteAry[0];
            if (format != FORMAT_RING_DEFLATE) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            } else {
               byte[] importDataAry = new byte[byteAry.length - 1];
               System.arraycopy(byteAry, 1, importDataAry, 0, importDataAry.length);
               CompoundTag dataTag = JBCompressToolHelper.readDecompress(importDataAry, new NbtAccounter(262144L));
               if (!AssembleNBTDataUtil.validateRINGDataTag(dataTag, false)) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
               } else {
                  JBPacketSender.sendSavedRingDataToServer(dataTag, player.getInventory().selected, false);
               }
            }
         }
      } catch (Exception var5) {
         handleUnknownImportError(player, var5);
      }
   }

   private static void exportRINGData(Player player, boolean breakString) {
      try {
         ItemStack currentItemStack = CompatHelper.getCurrentEquippedItem(player);
         CompoundTag fromItemTag = ItemRingProgram.getRingProgramTag(currentItemStack);
         if (fromItemTag == null) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbed.error.nodata"), false);
         } else if (!AssembleNBTDataUtil.validateRINGDataTag(fromItemTag, false)) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
         } else {
            CompoundTag dataTag = new CompoundTag();
            dataTag.putByteArray("jb:ringp", fromItemTag.getByteArray("jb:ringp"));
            byte[] byteAry = JBCompressToolHelper.compress(dataTag);
            byte[] exportDataAry = new byte[byteAry.length + 1];
            System.arraycopy(byteAry, 0, exportDataAry, 1, byteAry.length);
            exportDataAry[0] = FORMAT_RING_DEFLATE;
            String base64string;
            if (breakString) {
               base64string = JBBase64Util.toBase64WrappedChunkString(exportDataAry);
            } else {
               base64string = JBBase64Util.toBase64FlatChunkString(exportDataAry);
            }

            setClipboardString(base64string);
            player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbed.succeed"), false);
         }
      } catch (Exception var8) {
         handleUnknownExportError(player, var8);
      }
   }

   /**
    * Reconstructed: Fernflower failed on this method ("$FF: Couldn't be decompiled"), so the
    * body was read back from the compiled class
    * ({@code javap -c jp/mc/ancientred/jointblock/export/JBImportExportDataUtil.class}).
    * Control flow of the original bytecode:
    * <pre>
    * MinecraftServer is = JBModContainer.proxy.getIntegratedServer();
    * if (is != null &amp;&amp; is.func_71278_l() &amp;&amp; !is.func_71241_aa() &amp;&amp; is.func_71264_H()) {
    *    JBPacketSender.sendImportedAssembleData(null, seal, true);   // result popped
    *    return;                                                      // goto end
    * }
    * try {   // NB: the whole try/catch starts *after* the integrated-server branch
    *    tag = getBase64edNBTFromClientClipboard(player);
    *    if (tag == null) return;
    *    switch (JBPacketSender.sendImportedAssembleData(tag, seal, false)) {
    *       case 1: handleUnknownImportError(player, new Exception("Network Error")); break;
    *       case 2: player.func_145747_a(makeErrorChat("commands.jbeid.error.datatoolarge")); break;
    *    }
    * } catch (Exception e) { handleUnknownImportError(player, e); }
    * </pre>
    * The SRG checks map to {@code isRunning() / !isStopped() / isSingleplayer()}: on an
    * integrated server the blueprint tag is not shipped over the loopback channel at all,
    * the (in-process) server reads the client clipboard itself via
    * {@code CommonProxy.getNBTImportDataAsSingle} — which is exactly the
    * {@code processForSingleIS} branch of {@code JBPacketSender.sendImportedAssembleData} /
    * {@code JBPacketHandlerServer.receivePlayerImportedAssembleData}.
    */
   private static void importAssembleData(Player player, boolean seal) {
      MinecraftServer integratedServer = JBModContainer.proxy.getIntegratedServer();
      if (integratedServer != null && integratedServer.isRunning() && !integratedServer.isStopped() && integratedServer.isSingleplayer()) {
         CompoundTag dataTag;
         try {
            dataTag = getBase64edNBTFromClientClipboard(player);
         } catch (IOException e) {
            handleUnknownImportError(player, e);
            return;
         }

         if (dataTag == null) {
            return;
         }

         s_importedAssembleDataForSingle = dataTag;
         JBPacketSender.sendImportedAssembleData(null, seal, true);
         return;
      }

      try {
         CompoundTag dataTag = getBase64edNBTFromClientClipboard(player);
         if (dataTag == null) {
            return;
         }

         switch (JBPacketSender.sendImportedAssembleData(dataTag, seal, false)) {
            case SEND_EXCEPTION:
               handleUnknownImportError(player, new Exception("Network Error"));
               break;
            case SEND_TOO_LARGE:
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.datatoolarge"), false);
               break;
            default:
         }
      } catch (Exception var4) {
         handleUnknownImportError(player, var4);
      }
   }

   public static CompoundTag getBase64edNBTFromClientClipboard(Player player) throws IOException {
      byte[] byteAry = getValidClipboardData(player);
      if (byteAry == null) {
         return null;
      } else {
         byte format = byteAry[0];
         if (format != FORMAT_ASSEMBLE_DEFLATE) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            return null;
         } else {
            byte[] importDataAry = new byte[byteAry.length - 1];
            System.arraycopy(byteAry, 1, importDataAry, 0, importDataAry.length);
            CompoundTag dataTag = JBCompressToolHelper.readDecompress(importDataAry, new NbtAccounter(2097152L));
            if (!AssembleNBTDataUtil.validateAndFixAssembleDataTag(dataTag, true)) {
               player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
               return null;
            } else {
               dataTag.remove("wsav");
               dataTag.remove("msav");
               return dataTag;
            }
         }
      }
   }

   public static void exportAssembleData(Player player, long requestToken, CompoundTag dataTag, boolean deflate, boolean breakString) {
      if (s_requestToken != null && requestToken == s_requestToken) {
         if (dataTag != null) {
            try {
               dataTag.remove("wsav");
               dataTag.remove("msav");
               if (!AssembleNBTDataUtil.buildStraightIdTable(dataTag)) {
                  player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
               } else {
                  byte[] byteAry = JBCompressToolHelper.compress(dataTag);
                  byte[] exportDataAry = new byte[byteAry.length + 1];
                  System.arraycopy(byteAry, 0, exportDataAry, 1, byteAry.length);
                  exportDataAry[0] = FORMAT_ASSEMBLE_DEFLATE;
                  String base64string;
                  if (breakString) {
                     base64string = JBBase64Util.toBase64WrappedChunkString(exportDataAry);
                  } else {
                     base64string = JBBase64Util.toBase64FlatChunkString(exportDataAry);
                  }

                  setClipboardString(base64string);
                  player.displayClientMessage(JBCommonUtil.makeNormalChat("commands.jbed.succeed"), false);
                  if (dataTag.contains("idtbl")) {
                     dataTag.remove("idtbl");
                  }

                  if (dataTag.contains("sttbl")) {
                     dataTag.remove("sttbl");
                  }
               }
            } catch (Exception var9) {
               handleUnknownExportError(player, var9);
            }
         }
      }
   }

   private static byte[] getValidClipboardData(Player player) {
      boolean hasClipboardData = false;
      String clipboardData = getClipboardString();
      if (clipboardData != null) {
         clipboardData = clipboardData.trim();
         if (clipboardData.length() != 0) {
            hasClipboardData = true;
         }
      }

      if (!hasClipboardData) {
         player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.noclipboard"), false);
         return null;
      } else if (!JBBase64Util.checkBase64String(clipboardData)) {
         player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbid.error.notbase64"), false);
         return null;
      } else {
         byte[] byteAry = JBBase64Util.base64StringToByteArray(clipboardData);
         if (byteAry.length < 2) {
            player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.invaliddata"), false);
            return null;
         } else {
            return byteAry;
         }
      }
   }

   /** Takes the tag handed over by {@link #importAssembleData}; called on the integrated server thread. */
   @OnlyIn(Dist.CLIENT)
   public static CompoundTag takeImportedAssembleDataForSingle() {
      CompoundTag tag = s_importedAssembleDataForSingle;
      s_importedAssembleDataForSingle = null;
      return tag;
   }

   @OnlyIn(Dist.CLIENT)
   private static String getClipboardString() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? "" : mc.keyboardHandler.getClipboard();
   }

   @OnlyIn(Dist.CLIENT)
   private static void setClipboardString(String string) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.keyboardHandler.setClipboard(string);
      }
   }

   private static void handleUnknownImportError(Player player, Exception ex) {
      JointBlockRemastered.LOGGER.debug("Error during data import.", ex);
      player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.unknownerror"), false);
   }

   private static void handleUnknownExportError(Player player, Exception ex) {
      JointBlockRemastered.LOGGER.debug("Error during data export.", ex);
      player.displayClientMessage(JBCommonUtil.makeErrorChat("commands.jbeid.error.unknownerror"), false);
   }
}
