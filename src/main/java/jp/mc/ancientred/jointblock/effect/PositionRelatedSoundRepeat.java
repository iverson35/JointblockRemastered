package jp.mc.ancientred.jointblock.effect;

import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PositionRelatedSoundRepeat implements IJBRelatedSound {
   public int aliveTick;
   public int fadeTick;
   public int ascendTick = 1;
   public boolean isRepeatedRound;
   public float volume;
   public float initVolume;
   public float pitch;
   public int repeatDelay;
   public int keepDuration;
   public int fadeDuration;
   public int fadeBackDuration;
   private float fadeBackAmountPerTick;
   public Minecraft mc;
   public Sound soundSelected;
   public EffectHelper effectHelper;
   public final WeighedSoundEvents accessor;
   public AbstractEffectAttributes soundSlot;
   /** 1.20.1 {@link Sound#getVolume()}/{@link Sound#getPitch()} are
    * {@link net.minecraft.util.valueproviders.SampledFloat} value providers, not the 1.12.2 constant
    * floats; a stable source keeps repeated {@code sample()} calls deterministic. */
   private final RandomSource sampleRandom = SoundInstance.createUnseededRandom();

   public PositionRelatedSoundRepeat(EffectHelper effectHelperIn, WeighedSoundEvents accessorIn, AbstractEffectAttributes soundIn) {
      this.effectHelper = effectHelperIn;
      this.accessor = accessorIn;
      this.soundSlot = soundIn;
      this.soundSelected = accessorIn.getSound(RandomSource.create());
      this.mc = Minecraft.getInstance();
      this.initVolume = this.volume = (float)soundIn.volume * 0.1F;
      this.pitch = (float)soundIn.pitch * 0.1F;
      this.repeatDelay = Math.abs(soundIn.repeatDelay);
      this.keepDuration = Math.abs(soundIn.keepDuration);
      this.fadeDuration = Math.abs(soundIn.fadeDuration);
      this.fadeBackDuration = Math.abs(soundIn.fadeBackDuration);
      this.extendAliveTick();
      this.isRepeatedRound = false;
   }

   public boolean isSoundSlotObjEquals(AbstractEffectAttributes soundSlot) {
      return this.soundSlot == soundSlot;
   }

   public void dispose() {
      if (this.effectHelper.soundPlaying == this) {
         this.effectHelper.soundPlaying = null;
      }

   }

   public Sound getSoundPoolEntry() {
      return this.soundSelected;
   }

   public double getX() {
      return this.effectHelper.getPosX();
   }

   public double getY() {
      return this.effectHelper.getPosY();
   }

   public double getZ() {
      return this.effectHelper.getPosZ();
   }

   public float getVolume() {
      return this.soundSelected != null ? this.volume * this.soundSelected.getVolume().sample(this.sampleRandom) : this.volume;
   }

   public float getPitch() {
      return this.soundSelected != null ? this.pitch * this.soundSelected.getPitch().sample(this.sampleRandom) : this.pitch;
   }

   /** Positions are absolute world coordinates (see {@link #getX()}), so the sound is never listener relative. */
   public boolean isRelative() {
      return false;
   }

   public boolean isLooping() {
      return !this.isStopped();
   }

   public int getDelay() {
      return this.repeatDelay;
   }

   public SoundInstance.Attenuation getAttenuation() {
      return SoundInstance.Attenuation.NONE;
   }

   public ResourceLocation getLocation() {
      return this.soundSelected != null ? this.soundSelected.getLocation() : null;
   }

   public SoundSource getSource() {
      return JBSoundManager.JB_SOUND_CATEGORY;
   }

   public Sound getSound() {
      return this.soundSelected;
   }

   public WeighedSoundEvents resolve(SoundManager handler) {
      this.soundSelected = this.accessor.getSound(RandomSource.create());
      return this.accessor;
   }

   public void tick() {
      if (this.aliveTick > 0) {
         this.aliveTick -= this.ascendTick;
         if (this.volume < this.initVolume) {
            this.volume = Math.min(this.initVolume, this.volume + this.fadeBackAmountPerTick * (float)this.ascendTick);
         }

         this.fadeTick = this.fadeDuration + 1;
      } else {
         if (this.fadeTick > 0) {
            this.fadeTick -= this.ascendTick;
         }

         if (this.fadeDuration == 0) {
            this.volume = 0.0F;
         } else {
            float decrementVolumePerTick = this.initVolume / (float)this.fadeDuration;
            this.volume = Math.max(0.0F, this.volume - decrementVolumePerTick * (float)this.ascendTick);
            this.fadeBackAmountPerTick = (this.initVolume - this.volume) / (float)this.fadeBackDuration;
         }
      }

   }

   public void extendAliveTick() {
      this.aliveTick = this.keepDuration;
   }

   public boolean isStopped() {
      if (!JBConfig.enableJointBlockSounds) {
         return true;
      } else {
         return this.aliveTick <= 0 && this.fadeTick <= 0 || this.effectHelper.soundPlaying != this;
      }
   }
}
