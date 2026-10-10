package jp.mc.ancientred.jbrobot.item.external;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;

/**
 * A sound item of a model pack: the inventory item that hands out one {@link ExJBSoundInfo} per
 * item damage value (the pack's {@link ExJBSoundItemInfo#soundItemId}).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBSoundItemInfo}. The 1.12.2 client
 * fields cached an atlas sprite, the icon's entry name and a baked override model; 1.20.1 bakes
 * pack icons from the model JSONs {@code ModelPackResources} serves, which read
 * {@code itemIconFile} from {@link #properties} themselves.
 */
public class ExJBSoundItemInfo {
    public final JsonObject properties;
    public final ExtraPackage parent;
    public final int soundItemId;
    public final String soundName;
    public String soundItemName;
    public String descriptionText;
    public ExJBSoundInfo sound;

    public ExJBSoundItemInfo(ExtraPackage parent, int argAddonItemId, String argAddonName, JsonObject argProperties) {
        this.parent = parent;
        this.soundItemId = argAddonItemId;
        this.soundName = argAddonName;
        this.properties = argProperties;
    }
}
