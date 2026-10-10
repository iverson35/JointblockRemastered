package jp.mc.ancientred.jbrobot.item.addon;

import jp.mc.ancientred.jointblock.api.IJBAddon;

/**
 * The "head" addon limit key, ported verbatim from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.addon.LimitKeyHead}.
 */
public class LimitKeyHead implements IJBAddon.IJBAddonLimitKey {
   public static IJBAddon.IJBAddonLimitKey instance = new LimitKeyHead();

   @Override
   public String getApplyKeyName() {
      return "JBHEAD";
   }

   @Override
   public String getApplyKey() {
      return "JBHEAD";
   }

   @Override
   public int getVersion() {
      return 0;
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
