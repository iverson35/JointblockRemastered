package jp.mc.ancientred.jointblock.api;

import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;

public interface IJBSoundManager {

    void registerSound(String name, IJBSound sound);

    ArrayList<ItemStack> getImportedSoundItemList();
}
