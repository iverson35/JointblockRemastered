package jp.mc.ancientred.jbrobot;

import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12.2 {@code jp.mc.ancientred.jbrobot.JBRef}: the shared dummy stacks the addon GUI renders when it
 * has no real stack yet.
 *
 * <p>1.12.2 built {@code bluprintItemStackMockDummy} from {@code JBModContainer.BluePrintItem} with
 * damage 2 and an empty {@code jb:blueprint} tag ({@code JBRef:10}), which is what the blueprint
 * item's own {@code getNBTBasedMeta}/{@code getBlueprintInfo} read. 1.20.1 uses
 * {@link ItemStack#getOrCreateTag()} for the same tag.
 */
public final class JBRef {

    private JBRef() {
    }

    public static ItemStack bluprintItemStackMockDummy;

    /** 1.12.2 {@code JBRef.setUpReference()}, called from the addon's common setup. */
    public static void setUpReference() {
        bluprintItemStackMockDummy = new ItemStack(JBItems.BluePrintItem, 1);
        bluprintItemStackMockDummy.setDamageValue(2);
        CompoundTag tag = new CompoundTag();
        tag.put("jb:blueprint", new CompoundTag());
        bluprintItemStackMockDummy.setTag(tag);
    }
}
