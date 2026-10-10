package jp.mc.ancientred.jbrobot.item.external;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A sound of a model pack (a {@code sound/*.json} entry), addressed by {@link #soundName}.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBSoundInfo}. The 1.12.2 client accessor
 * was a {@code SoundEventAccessor}; 1.20.1 renamed that class to {@link WeighedSoundEvents}, whose
 * constructor takes {@code (ResourceLocation, String subtitle)} (subtitles do not exist in the pack
 * format, so {@code null}) and which receives each {@link ExJBSound} through {@code addSound}.
 */
public class ExJBSoundInfo implements IJBSound {
    public final JsonObject properties;
    public final ExtraPackage parent;
    public final String soundName;
    public ExJBSound[] soundFileList;
    public ResourceLocation resLoc;

    @OnlyIn(Dist.CLIENT)
    public WeighedSoundEvents accesor;

    public ExJBSoundInfo(ExtraPackage parent, String argSoundName, ExJBSound[] argSoundFileList, JsonObject argProperties) {
        this.parent = parent;
        this.soundName = argSoundName;
        this.soundFileList = argSoundFileList;
        this.properties = argProperties;
        this.resLoc = this.parent.packResource(this.soundName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public synchronized WeighedSoundEvents getSoundAccessor() {
        if (this.accesor == null) {
            this.accesor = new WeighedSoundEvents(this.resLoc, (String) null);
            for (ExJBSound each : this.soundFileList) {
                this.accesor.addSound(each.getSound());
            }
        }

        return this.accesor;
    }
}
