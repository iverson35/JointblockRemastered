package jp.mc.ancientred.jbrobot.item.addon;

import jp.mc.ancientred.jointblock.api.IJBAddon;

/**
 * The "hands" addon limit key, ported verbatim from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.addon.LimitKeyHands}.
 */
public class LimitKeyHands implements IJBAddon.IJBAddonLimitKey {
   public static IJBAddon.IJBAddonLimitKey instance = new LimitKeyHands();

   @Override
   public String getApplyKeyName() {
      return "JBHANDS";
   }

   @Override
   public String getApplyKey() {
      return "JBHANDS";
   }

   @Override
   public int getVersion() {
      return 0;
   }

   @Override
   public int getApplyCostLimit() {
      return 2;
   }

   @Override
   public boolean ignoreModelOnExceed() {
      return false;
   }
}
