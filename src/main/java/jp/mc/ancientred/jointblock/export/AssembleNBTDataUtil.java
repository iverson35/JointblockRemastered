package jp.mc.ancientred.jointblock.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import jp.mc.ancientred.jointblock.assemble.JointDataSerializer;
import jp.mc.ancientred.jointblock.assemble.JointListsHolderImpl;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgram;
import jp.mc.ancientred.jointblock.common.JBLegacyNames;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Blueprint/clipboard payload validation and repair, ported from the 1.12.2
 * {@code AssembleNBTDataUtil}.
 *
 * <p>1.20.1 semantics: 1.12 resolved model/motion blocks+items through {@code GameData}'s
 * {@code RegistryNamespaced} raw numeric ids ({@code GameData.getWrapper(Item.class)} /
 * {@code Block.class}); 1.20.1 has no numeric raw ids, so {@link BuiltInRegistries#ITEM} /
 * {@link BuiltInRegistries#BLOCK} session ids are used instead (blocks still stored negated).
 * The serialized format itself ({@code ser}/{@code tnm}/{@code idtbl}/{@code sttbl} plus Java-UTF
 * id/name tables) is a persisted blueprint format and is unchanged; lookups that could miss use
 * {@link Registry#getOptional(ResourceLocation)} to keep the 1.12 null-means-invalid behaviour.
 * Stored names still carry the 1.12.2 namespaces, so they go through {@link JBLegacyNames} first.
 */
public class AssembleNBTDataUtil {
   private static final String INVALID_DATA = "invalid data";

   public static boolean validateAndFixAssembleDataTag(CompoundTag dataTag, boolean checkAtClient) {
      int[] serialized = dataTag.getIntArray("ser");
      int partsCount = dataTag.getInt("tnm");
      if (serialized.length != 0 && partsCount != 0) {
         Registry<Item> registeredItemsList = BuiltInRegistries.ITEM;
         Registry<Block> registeredBlockList = BuiltInRegistries.BLOCK;
         Hashtable<Short, String> idTable = null;
         if (checkAtClient) {
            byte[] objAry;
            if (dataTag.contains("sttbl")) {
               objAry = dataTag.getByteArray("sttbl");
               idTable = new Hashtable<>();
               readToMapFromStraightTable(idTable, objAry);
            } else if (dataTag.contains("idtbl")) {
               objAry = dataTag.getByteArray("idtbl");
               idTable = new Hashtable<>();
               readInMapFromSerialized(idTable, objAry);
            }
         }

         if (dataTag.contains("idtbl")) {
            dataTag.remove("idtbl");
         }

         if (dataTag.contains("sttbl")) {
            dataTag.remove("sttbl");
         }

         JointListsHolderImpl listHolder = new JointListsHolderImpl();
         JointDataSerializer.rebuildAll(serialized, partsCount, listHolder);
         int coreCnt = 0;
         int seatCnt = 0;
         int normalCnt = 0;
         int rotatorCnt = 0;
         int extenderCnt = 0;
         int scalerCnt = 0;
         int sliderCnt = 0;
         int attracterCnt = 0;
         JointParts[] refList = listHolder.refLists;
         int len = refList.length;

         for (int i = 0; i < len; ++i) {
            JointParts parts = refList[i];
            if (parts == null) {
               return false;
            }

            switch (parts.jointType) {
               case 0:
                  ++coreCnt;
                  if (coreCnt >= 2) {
                     return false;
                  }
                  break;
               case 1:
               case 3:
               case 5:
               case 6:
               case 7:
               case 9:
               case 11:
               case 13:
               default:
                  return false;
               case 2:
                  ++seatCnt;
                  break;
               case 4:
                  ++normalCnt;
                  break;
               case 8:
                  ++rotatorCnt;
                  break;
               case 10:
                  ++extenderCnt;
                  break;
               case 12:
                  ++scalerCnt;
                  break;
               case 14:
                  ++sliderCnt;
                  break;
               case 15:
                  ++attracterCnt;
            }

            if (checkAtClient) {
               String namespaced;
               short fixedId;
               Item objItem;
               if (parts.modelItemId > 0) {
                  namespaced = idTable.get(parts.modelItemId);
                  if (namespaced == null) {
                     return false;
                  }

                  objItem = JBLegacyNames.itemByName(namespaced);
                  fixedId = (short)(objItem != null ? registeredItemsList.getId(objItem) : -1);
                  if (fixedId == -1) {
                     parts.modelItemId = 0;
                  } else {
                     parts.modelItemId = fixedId;
                  }
               } else if (parts.modelItemId < 0) {
                  namespaced = idTable.get(parts.modelItemId);
                  if (namespaced == null) {
                     return false;
                  }

                  Block objBlock = JBLegacyNames.blockByName(namespaced);
                  fixedId = (short)(objBlock != null ? registeredBlockList.getId(objBlock) : -1);
                  if (fixedId == -1) {
                     parts.modelItemId = 0;
                  } else {
                     parts.modelItemId = (short)(-fixedId);
                  }
               }

               if (parts.motionItemId != 0) {
                  namespaced = idTable.get(parts.motionItemId);
                  if (namespaced == null) {
                     return false;
                  }

                  objItem = JBLegacyNames.itemByName(namespaced);
                  fixedId = (short)(objItem != null ? registeredItemsList.getId(objItem) : -1);
                  if (fixedId == -1) {
                     parts.motionItemId = 0;
                  } else {
                     parts.motionItemId = fixedId;
                  }
               }
            }
         }

         if (dataTag.getInt("cnm") != coreCnt) {
            return false;
         } else if (dataTag.getInt("snm") != seatCnt) {
            return false;
         } else if (dataTag.getInt("nnm") != normalCnt) {
            return false;
         } else if (dataTag.getInt("rnm") != rotatorCnt) {
            return false;
         } else if (dataTag.getInt("enm") != extenderCnt) {
            return false;
         } else if (dataTag.getInt("scl") != scalerCnt) {
            return false;
         } else if (dataTag.getInt("sld") != sliderCnt) {
            return false;
         } else if (dataTag.getInt("atr") != attracterCnt) {
            return false;
         } else if (coreCnt + seatCnt + normalCnt + rotatorCnt + extenderCnt + scalerCnt + sliderCnt + attracterCnt != partsCount) {
            return false;
         } else {
            serialized = JointDataSerializer.serializeAll(refList, partsCount);
            dataTag.putIntArray("ser", serialized);
            return true;
         }
      } else {
         return false;
      }
   }

   private static void readInMapFromSerialized(Hashtable<Short, String> map, byte[] objAry) {
      ByteArrayInputStream bin = new ByteArrayInputStream(objAry);
      DataInputStream input = null;

      try {
         input = new DataInputStream(bin);
         if (input.readUnsignedByte() != 172) {
            return;
         }

         if (input.readUnsignedByte() != 237) {
            return;
         }

         if (input.readByte() != 0) {
            return;
         }

         input.readByte();
         if (input.readByte() != 115) {
            return;
         }

         if (input.readByte() != 114) {
            return;
         }

         String strCheck = readUTFSafe(input);
         if (!"java.util.Hashtable".equals(strCheck)) {
            return;
         }

         if (input.readLong() != 1421746759512286392L) {
            return;
         }

         input.readByte();
         if (input.readShort() != 2) {
            return;
         }

         if (input.readByte() != 70) {
            return;
         }

         strCheck = readUTFSafe(input);
         if (!"loadFactor".equals(strCheck)) {
            return;
         }

         if (input.readByte() != 73) {
            return;
         }

         strCheck = readUTFSafe(input);
         if (!"threshold".equals(strCheck)) {
            return;
         }

         if (input.readByte() != 120) {
            return;
         }

         if (input.readByte() == 112) {
            input.readFloat();
            input.readInt();
            if (input.readByte() != 119) {
               return;
            }

            input.readByte();
            int tableLength = input.readInt();
            int entryCounts = input.readInt();
            short key = 0;
            strCheck = "";

            for (int i = 0; i < entryCounts * 2; ++i) {
               int tc_code = input.readByte();
               if (tc_code == 115) {
                  key = readObjShort(input);
               } else {
                  if (tc_code != 116) {
                     throw new Exception("invalid object serialization");
                  }

                  strCheck = readUTFSafe(input);
                  if (strCheck.length() > 150) {
                     throw new Exception("that is heck large String. something is wrong!");
                  }

                  if (key != 0 && !"".equals(strCheck)) {
                     map.put(key, strCheck);
                  }
               }
            }

            if (input.readByte() != 120) {
               return;
            }

            return;
         }
      } catch (Exception var34) {
         var34.printStackTrace();
         return;
      } finally {
         try {
            if (input != null) {
               input.close();
            }
         } catch (Exception var33) {
         }
      }
   }

   private static short readObjShort(DataInputStream input) throws Exception {
      byte second = input.readByte();
      if (second == 114) {
         if (!"java.lang.Short".equals(readUTFSafe(input))) {
            throw new Exception(INVALID_DATA);
         }

         if (7515723908773894738L != input.readLong()) {
            throw new Exception(INVALID_DATA);
         }

         input.readByte();
         if (1 != input.readShort()) {
            throw new Exception(INVALID_DATA);
         }

         if (83 != input.readByte()) {
            throw new Exception(INVALID_DATA);
         }

         if (!"value".equals(readUTFSafe(input))) {
            throw new Exception(INVALID_DATA);
         }

         if (120 != input.readByte()) {
            throw new Exception(INVALID_DATA);
         }

         if (114 != input.readByte()) {
            throw new Exception(INVALID_DATA);
         }

         if (!"java.lang.Number".equals(readUTFSafe(input))) {
            throw new Exception(INVALID_DATA);
         }

         if (-8742448824652078965L != input.readLong()) {
            throw new Exception(INVALID_DATA);
         }

         input.readByte();
         if (0 != input.readShort()) {
            throw new Exception(INVALID_DATA);
         }

         if (120 != input.readByte()) {
            throw new Exception(INVALID_DATA);
         }

         if (112 != input.readByte()) {
            throw new Exception(INVALID_DATA);
         }
      } else if (second == 113) {
         int var2 = input.readInt();
      }

      return input.readShort();
   }

   private static <T> void addIfNotInTable(Registry<T> registeredIdsList, Hashtable<Short, String> idTable, int id, T target) {
      if (idTable.get((short)id) == null) {
         ResourceLocation namespacedLoc = registeredIdsList.getKey(target);
         if (namespacedLoc != null) {
            idTable.put((short)id, namespacedLoc.toString());
         }
      }
   }

   public static void readToMapFromStraightTable(Hashtable<Short, String> map, byte[] bary) {
      if (bary != null) {
         ByteArrayInputStream bin = new ByteArrayInputStream(bary);
         DataInputStream din = null;

         try {
            din = new DataInputStream(bin);
            int count = din.readInt();
            if (count > 2048) {
               return;
            }

            for (int i = 0; i < count; ++i) {
               map.put(din.readShort(), readUTFSafe(din));
            }
         } catch (Exception var6) {
         }
      }
   }

   public static boolean buildStraightIdTable(CompoundTag dataTag) {
      int[] serialized = dataTag.getIntArray("ser");
      int partsCount = dataTag.getInt("tnm");
      if (serialized.length != 0 && partsCount != 0) {
         Registry<Item> registeredItemsList = BuiltInRegistries.ITEM;
         Registry<Block> registeredBlockList = BuiltInRegistries.BLOCK;
         Hashtable<Short, String> idTable = new Hashtable<>();
         JointListsHolderImpl listHolder = new JointListsHolderImpl();
         JointDataSerializer.rebuildAll(serialized, partsCount, listHolder);
         JointParts[] refList = listHolder.refLists;
         int len = refList.length;

         for (int i = 0; i < len; ++i) {
            JointParts parts = refList[i];
            if (parts == null) {
               return false;
            }

            int modelItemId = parts.getModelItemId();
            if (modelItemId > 0) {
               addIfNotInTable(registeredItemsList, idTable, modelItemId, BuiltInRegistries.ITEM.byId(modelItemId));
            } else if (modelItemId < 0) {
               addIfNotInTable(registeredBlockList, idTable, modelItemId, BuiltInRegistries.BLOCK.byId(-modelItemId));
            }

            int motoinItemId = parts.getMotionItemId();
            if (motoinItemId != 0) {
               addIfNotInTable(registeredItemsList, idTable, motoinItemId, BuiltInRegistries.ITEM.byId(motoinItemId));
            }
         }

         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         DataOutputStream dout = new DataOutputStream(bout);

         try {
            Set<Map.Entry<Short, String>> eset = idTable.entrySet();
            dout.writeInt(eset.size());
            Iterator<Map.Entry<Short, String>> it = eset.iterator();

            while (it.hasNext()) {
               Map.Entry<Short, String> each = it.next();
               dout.writeShort(each.getKey());
               dout.writeUTF(each.getValue());
            }
         } catch (IOException var13) {
            return false;
         }

         dataTag.putByteArray("sttbl", bout.toByteArray());
         return true;
      } else {
         return false;
      }
   }

   private static final String readUTFSafe(DataInputStream input) throws Exception {
      input.mark(0);
      int utflen = input.readUnsignedShort();
      if (utflen > 200) {
         throw new Exception("That is heck large String. something is wrong!");
      } else {
         input.reset();
         return input.readUTF();
      }
   }

   public static boolean validateRINGDataTag(CompoundTag dataTag, boolean getSetName) {
      if (dataTag != null && dataTag.contains("jb:ringp", 7)) {
         byte[] ringDataAry = dataTag.getByteArray("jb:ringp");
         if (ringDataAry.length == 0) {
            return false;
         } else {
            try {
               RingProgram pgm = RingProgram.readFromByteArray(ringDataAry);
               if (!pgm.hadError) {
                  String pageZeroName = pgm.getRingZeroName();
                  if (pageZeroName != null && pageZeroName.length() > 0) {
                     dataTag.putString("jb:ringnm", pageZeroName);
                  }

                  return true;
               }
            } catch (Exception var5) {
            }

            return false;
         }
      } else {
         return false;
      }
   }
}
