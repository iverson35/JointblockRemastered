package jp.mc.ancientred.jbrobot.gui;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * 1.12.2 {@code SlotJBMerchantResult}: the single output slot of the model catalog.  It adds no
 * behaviour over {@link Slot} beyond its position (the original class did not either); the buy
 * logic lives in {@link ContainerModelCatalog}.
 */
public class SlotJBMerchantResult extends Slot {
   public SlotJBMerchantResult(Container inventory, int slotIndex, int xPosition, int yPosition) {
      super(inventory, slotIndex, xPosition, yPosition);
   }
}
