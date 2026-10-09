package jp.mc.ancientred.jointblock.export;

import jp.mc.ancientred.jointblock.api.IJBBlueprintFactory;
import jp.mc.ancientred.jointblock.common.JBBase64Util;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.item.ItemStack;

/**
 * Default {@link IJBBlueprintFactory}, ported from the 1.12.2 {@code JBBlueprintFactory}.
 *
 * <p>1.20.1 semantics: the 1.12 blueprint item carried its kind in the stack metadata
 * ({@code new ItemStack(item, 1, 3|2)}); 1.20.1 items have no metadata, so the same two kinds are
 * stored via {@link ItemJBBlueprint#setSubtype(ItemStack, int)} with
 * {@link ItemJBBlueprint#TYPE_CAPSULE}/{@link ItemJBBlueprint#TYPE_MERCHANT} (1.12 meta 3/2).
 * {@code NBTSizeTracker} became {@link NbtAccounter}. Export payload format is unchanged.
 */
public class JBBlueprintFactory implements IJBBlueprintFactory {
   @Override
   public ItemStack createBlueprintItemFromBase64(String key, String str, boolean asCapsule) {
      CompoundTag dataTag = this.createBlueprintTagFromBase64(key, str);
      if (dataTag == null) {
         return null;
      } else {
         CompoundTag tagMain = new CompoundTag();
         ItemStack ret = new ItemStack(JBItems.BluePrintItem);
         tagMain.put("jb:blueprint", dataTag);
         tagMain.putBoolean("jb:seal", true);
         ret.setTag(tagMain);
         // 1.12 stored the blueprint kind as item metadata (3 = capsule, 2 = merchant);
         // 1.20.1 keeps it as the blueprint item subtype inside the stack tag.
         ItemJBBlueprint.setSubtype(ret, asCapsule ? ItemJBBlueprint.TYPE_CAPSULE : ItemJBBlueprint.TYPE_MERCHANT);
         return ret;
      }
   }

   @Override
   public CompoundTag createBlueprintTagFromBase64(String key, String str) {
      try {
         byte[] byteAry = JBBase64Util.base64StringToByteArray(str);
         if (byteAry == null) {
            return null;
         } else {
            byte format = byteAry[0];
            if (format != 5) {
               return null;
            } else {
               byte[] importDataAry = new byte[byteAry.length - 1];
               System.arraycopy(byteAry, 1, importDataAry, 0, importDataAry.length);
               CompoundTag dataTag = JBCompressToolHelper.readDecompress(importDataAry, new NbtAccounter(2097152L));
               if (!AssembleNBTDataUtil.validateAndFixAssembleDataTag(dataTag, true)) {
                  return null;
               } else {
                  dataTag.remove("wsav");
                  dataTag.remove("msav");
                  String unitName = ItemJBBlueprint.getNameFromCoreAddtionalInfo(dataTag);
                  if (unitName != null && unitName.length() > 0) {
                     dataTag.putString("name", unitName);
                  }

                  ItemJBBlueprint.saveImportedBpDataToMemroyHeap(key, dataTag);
                  return dataTag;
               }
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         return null;
      }
   }
}
