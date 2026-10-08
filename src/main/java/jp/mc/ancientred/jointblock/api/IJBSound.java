package jp.mc.ancientred.jointblock.api;

import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IJBSound {

    /**
     * 1.12.2 returned {@code net.minecraft.client.audio.SoundEventAccessor};
     * the 1.20.1 mojmap equivalent is
     * {@link net.minecraft.client.sounds.WeighedSoundEvents}.
     */
    @OnlyIn(Dist.CLIENT)
    WeighedSoundEvents getSoundAccessor();

    interface IJBSoundHolder extends IJBConsts {
        IJBSound getIJBSound(int index);
    }
}
