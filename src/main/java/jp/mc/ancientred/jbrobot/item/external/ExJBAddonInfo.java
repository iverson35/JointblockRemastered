package jp.mc.ancientred.jbrobot.item.external;

import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jointblock.api.IJBAddon;

/**
 * A single addon of a model pack (an {@code addon/*.json} entry).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBAddonInfo}, ported verbatim. The
 * loader fills {@link #bonusValues}, {@link #limitKeyCost} and {@link #limitKeyName}; the limit key
 * object itself is bound later by
 * {@link ExtraPackage#setupAddons(java.util.Map)}.
 */
public class ExJBAddonInfo implements IJBAddon {
    public final JsonObject properties;
    public final ExtraPackage parent;
    public final String addonName;
    public int limitKeyCost;
    public String limitKeyName;
    public IJBAddon.IJBAddonLimitKey limitKey;
    public float[] bonusValues = new float[IJBAddon.BonusType.values().length];

    public ExJBAddonInfo(ExtraPackage parent, String argAddonName, JsonObject argProperties) {
        this.parent = parent;
        this.addonName = argAddonName;
        this.properties = argProperties;
    }

    @Override
    public float getBonusValue(IJBAddon.BonusType bonusType) {
        return this.bonusValues[bonusType.ordinal()];
    }

    @Override
    public int getCost() {
        return this.limitKeyCost;
    }

    @Override
    public IJBAddon.IJBAddonLimitKey getLimitKey() {
        return this.limitKey;
    }
}
