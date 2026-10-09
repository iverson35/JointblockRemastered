package jp.mc.ancientred.jointblock.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICommandEditableItem {
   boolean editByCommand(Player var1, ItemStack var2, String[] var3);
}
