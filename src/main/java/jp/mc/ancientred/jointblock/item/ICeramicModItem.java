package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICeramicModItem {
   void onItemUsedToJointBlock(ItemStack var1, Player var2, TileEntityJoint var3);
}
