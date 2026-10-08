package jp.mc.ancientred.jointblock.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IJBBlueprintFactory {

    ItemStack createBlueprintItemFromBase64(String base64, String name, boolean flag);

    CompoundTag createBlueprintTagFromBase64(String base64, String name);
}
