package jp.mc.ancientred.jbrobot.item.merchant;

import net.minecraft.world.item.ItemStack;

/**
 * The error-only catalog package shown when the client and server catalog packages disagree, ported
 * from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.merchant.DummyModelMerchantPackage}.
 *
 * <p>1.20.1 deviation: the icon slot returns {@link ItemStack#EMPTY} instead of {@code null}, matching
 * the port's other packages (a null stack would break the slot rendering).
 */
public class DummyModelMerchantPackage implements IJBRMerchantPackage {

   @Override
   public int getEntryNums(JBMerchantType type) {
      return 0;
   }

   @Override
   public IJBRMerchantEntry getMerchantEntry(JBMerchantType type, int index) {
      return null;
   }

   @Override
   public ItemStack getEntryIconStack() {
      return ItemStack.EMPTY;
   }

   @Override
   public String getPackageName() {
      return "SERVER SYNC ERROR";
   }
}
