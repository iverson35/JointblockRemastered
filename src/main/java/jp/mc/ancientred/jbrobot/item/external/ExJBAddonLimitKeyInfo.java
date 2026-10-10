package jp.mc.ancientred.jbrobot.item.external;

import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jointblock.api.IJBAddon;

/**
 * One {@code addon/} limit key of a model pack.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBAddonLimitKeyInfo}, ported verbatim:
 * the class only carries the parsed values (the loader fills {@link #version}, {@link #costLimit},
 * {@link #ignoreModelOnExceed} and {@link #limitKeyDisplayName}) and hands them to
 * {@link IJBAddon.IJBAddonLimitKey} consumers.
 */
public class ExJBAddonLimitKeyInfo implements IJBAddon.IJBAddonLimitKey {
    public final JsonObject properties;
    public final ExtraPackage parent;
    public final String limitKeyName;
    public int version;
    public int costLimit;
    public boolean ignoreModelOnExceed;
    public String limitKeyDisplayName;

    public ExJBAddonLimitKeyInfo(ExtraPackage parent, String argLimitKeyName, JsonObject argProperties) {
        this.parent = parent;
        this.limitKeyName = argLimitKeyName;
        this.properties = argProperties;
    }

    @Override
    public String getApplyKeyName() {
        return this.limitKeyDisplayName;
    }

    @Override
    public String getApplyKey() {
        return this.limitKeyName;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public int getApplyCostLimit() {
        return this.costLimit;
    }

    @Override
    public boolean ignoreModelOnExceed() {
        return this.ignoreModelOnExceed;
    }
}
