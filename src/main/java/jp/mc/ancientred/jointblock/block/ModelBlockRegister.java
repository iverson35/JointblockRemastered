package jp.mc.ancientred.jointblock.block;

import java.util.HashMap;
import java.util.Locale;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModelBlockRegister {
   public static int BLOCK_ID_MAX = 4095;
   public static boolean[] ALLOWED_BLOCKLIST;
   public static final HashMap<Block, Block> allowedBlockMap;

   public static void registerAllowedModelBlocks() {
      Registry<Block> registeredBlockList = BuiltInRegistries.BLOCK;
      java.util.List<? extends String> allowedBlocks = JBConfig.allowedModelBlocks;
      if (allowedBlocks != null) {
         for(String namespacedBlockName : allowedBlocks) {
            if (namespacedBlockName != null && namespacedBlockName.length() != 0) {
               if (namespacedBlockName.endsWith(":*")) {
                  String namespace = namespacedBlockName.substring(0, namespacedBlockName.length() - 2);

                  for(ResourceLocation searchName : registeredBlockList.keySet()) {
                     if (namespace.equalsIgnoreCase(searchName.getNamespace())) {
                        Block block = registeredBlockList.get(searchName);
                        allowedBlockMap.put(block, block);
                     }
                  }
               } else {
                  ResourceLocation searchName = ResourceLocation.tryParse(namespacedBlockName.toLowerCase(Locale.ROOT));
                  Block block = searchName == null ? null : registeredBlockList.get(searchName);
                  allowedBlockMap.put(block, block);
               }
            }
         }

      }
   }

   public static boolean isAllowedModelBlock(Block block) {
      if (block instanceof BlockJointBase) {
         return false;
      } else if (JBConfig.allowAnyBlockForModel) {
         return true;
      } else {
         return block.getClass().getName().startsWith("net.minecraft") ? true : allowedBlockMap.containsKey(block);
      }
   }

   static {
      ALLOWED_BLOCKLIST = new boolean[BLOCK_ID_MAX];
      allowedBlockMap = new HashMap<>();
   }
}
