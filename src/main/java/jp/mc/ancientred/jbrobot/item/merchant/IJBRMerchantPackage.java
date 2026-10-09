package jp.mc.ancientred.jbrobot.item.merchant;

import net.minecraft.world.item.ItemStack;

/**
 * A catalog source: the built-in default package or one loaded model pack. Ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage}.
 */
public interface IJBRMerchantPackage {
   String getPackageName();

   int getEntryNums(JBMerchantType type);

   IJBRMerchantEntry getMerchantEntry(JBMerchantType type, int index);

   ItemStack getEntryIconStack();
}
