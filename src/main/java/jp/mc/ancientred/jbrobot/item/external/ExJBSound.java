package jp.mc.ancientred.jbrobot.item.external;

import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One sound file line of an {@link ExJBSoundInfo} (an entry of the pack's {@code soundFileList}).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBSound}. The 1.12.2 client cached a
 * {@code net.minecraft.client.audio.Sound} built from the raw GL-independent values; 1.20.1 keeps
 * the same fields and builds a {@link Sound} on demand.
 *
 * <p>1.12.2 {@code JBRModelUtils.removeExtension} is not part of the ported {@code JBRModelUtils}
 * (nothing else needs it), so the identical trailing-extension strip is a private helper here.
 */
public class ExJBSound {
    /** Default attenuation distance of 1.20.1 sound definitions; the 1.12.2 ctor had no such field. */
    private static final int DEFAULT_ATTENUATION_DISTANCE = 16;

    public String soundlFileName;
    public int weight = 1;
    public float volume = 1.0F;
    public float pitch = 1.0F;
    public String domainedSoundPath;

    @OnlyIn(Dist.CLIENT)
    public Sound sound;

    public ExJBSound(ExtraPackage parent, String argSoundFileName, int argWeight, float argVolume, float argPitch) {
        this.soundlFileName = argSoundFileName;
        this.weight = argWeight;
        this.volume = argVolume;
        this.pitch = argPitch;
        // 1.12.2 addressed the file as jbrextrapackages:<packageId>:<path>; see ExtraPackage#packResource.
        this.domainedSoundPath = removeExtension(parent.packResource(argSoundFileName).toString());
    }

    /**
     * 1.12.2 {@code new Sound(path, volume, pitch, weight, Type.FILE, false)}. The 1.20.1
     * {@link Sound} takes {@link net.minecraft.util.valueproviders.SampledFloat} volume/pitch plus
     * the {@code preload}/{@code attenuationDistance} values the 1.12.2 ctor lacked; the streaming
     * flag stays {@code false}.
     */
    @OnlyIn(Dist.CLIENT)
    public Sound getSound() {
        if (this.sound == null) {
            this.sound = new Sound(this.domainedSoundPath, ConstantFloat.of(this.volume), ConstantFloat.of(this.pitch),
                    this.weight, Sound.Type.FILE, false, false, DEFAULT_ATTENUATION_DISTANCE);
        }

        return this.sound;
    }

    private static String removeExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int charAt = fileName.lastIndexOf(46);
        return charAt > 0 ? fileName.substring(0, charAt) : fileName;
    }
}
