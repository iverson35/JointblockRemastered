package jp.mc.ancientred.jointblock.export;

import net.minecraft.nbt.CompoundTag;

/**
 * Process-wide holder for blueprint payloads that are referenced by a heap key instead of being
 * carried inside the item stack, ported verbatim from the 1.12.2
 * {@code MemorySavedBlueprintDataCollections}. Only {@code NBTTagCompound} became {@link CompoundTag}.
 */
public class MemorySavedBlueprintDataCollections {
   public static MemorySavedBlueprintDataCollections instance = new MemorySavedBlueprintDataCollections();
   public CompoundTag blueprintDataNBTCollection = new CompoundTag();
   private int sequence = 0;

   public int getUniqueSequenceId() {
      return this.sequence++;
   }
}
