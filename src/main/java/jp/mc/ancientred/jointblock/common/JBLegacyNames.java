package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.modelpack.ModelPackResources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * Registry-name translation for stored 1.12.2 names.
 *
 * <p>The port registers everything under the mod ids {@code jointblock} and {@code jbrobot}, but the
 * 1.12.2 mods were {@code modj_jointblock} and {@code modj_jbrobot}. Names that were persisted by
 * 1.12.2 - blueprint payloads ({@code sttbl}/{@code idtbl} id tables, effect-slot item names) - still
 * carry the old namespaces, so every name read back out of an imported payload has to be normalised
 * before it is looked up in the registries. Without this, a lookup misses, the caller treats the entry
 * as "item no longer present" and the imported machine silently loses those parts (model items, MCTRL
 * motion items).
 *
 * <p>1.12.2 additionally stored addon items with the namespace stripped, as a bare {@code ":name"}
 * ({@code EffectInventory} export/import pair), which is expanded to the current addon namespace.
 */
public final class JBLegacyNames {
   /** 1.12.2 mod id of the main mod (now {@link JointBlockRemastered#MOD_ID}). */
   public static final String LEGACY_MAIN_NAMESPACE = "modj_jointblock";
   /** 1.12.2 mod id of the model-pack addon (now {@link ModelPackResources#ITEM_NAMESPACE}). */
   public static final String LEGACY_ADDON_NAMESPACE = "modj_jbrobot";

   private JBLegacyNames() {
   }

   /**
    * Rewrites a stored name to the namespace this port registers it under. Unknown namespaces are
    * passed through (lower-cased, as the 1.12.2 lookups did).
    */
   public static String toCurrentName(String storedName) {
      if (storedName == null || storedName.isEmpty()) {
         return storedName;
      }

      String lower = storedName.toLowerCase(Locale.ROOT);
      if (lower.startsWith(LEGACY_MAIN_NAMESPACE + ":")) {
         return JointBlockRemastered.MOD_ID + lower.substring(LEGACY_MAIN_NAMESPACE.length());
      }

      if (lower.startsWith(LEGACY_ADDON_NAMESPACE + ":")) {
         return ModelPackResources.ITEM_NAMESPACE + lower.substring(LEGACY_ADDON_NAMESPACE.length());
      }

      if (lower.charAt(0) == ':') {
         return ModelPackResources.ITEM_NAMESPACE + lower;
      }

      return lower;
   }

   /** {@link #toCurrentName} as a {@link ResourceLocation}, or {@code null} when it is malformed. */
   @Nullable
   public static ResourceLocation toCurrentLocation(String storedName) {
      String current = toCurrentName(storedName);
      if (current == null || current.isEmpty()) {
         return null;
      }

      try {
         return new ResourceLocation(current);
      } catch (Exception e) {
         return null;
      }
   }

   /** Resolves a stored name against the item registry, {@code null} when it no longer exists. */
   @Nullable
   public static Item itemByName(String storedName) {
      ResourceLocation location = toCurrentLocation(storedName);
      return location == null ? null : BuiltInRegistries.ITEM.getOptional(location).orElse(null);
   }

   /** Resolves a stored name against the block registry, {@code null} when it no longer exists. */
   @Nullable
   public static Block blockByName(String storedName) {
      ResourceLocation location = toCurrentLocation(storedName);
      return location == null ? null : BuiltInRegistries.BLOCK.getOptional(location).orElse(null);
   }
}
