package jp.mc.ancientred.jbrobot.item.addon;

import jp.mc.ancientred.jointblock.api.IJBAddon;

/**
 * The "subtype" addon limit key, ported verbatim from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.addon.LimitKeySubType}. Its version is
 * {@code Integer.MAX_VALUE} so it always wins the duplicate-key comparison.
 */
public class LimitKeySubType implements IJBAddon.IJBAddonLimitKey {
   public static IJBAddon.IJBAddonLimitKey instance = new LimitKeySubType();

   @Override
   public String getApplyKeyName() {
      return "JBSUBTYPE";
   }

   @Override
   public String getApplyKey() {
      return "JBSUBTYPE";
   }

   @Override
   public int getVersion() {
      return Integer.MAX_VALUE;
   }

   @Override
   public int getApplyCostLimit() {
      return 1;
   }

   @Override
   public boolean ignoreModelOnExceed() {
      return false;
   }
}
