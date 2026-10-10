package jp.mc.ancientred.jointblock.effect;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.world.entity.Entity;

public class SoundHandlerHook {
   public static final String EXTRA_PACKAGE_ROOT_DOMAIN = "jbrextrapackages";

   public static boolean validatePlay(Entity entity, AbstractEffectAttributes soundSlot) {
      if (!JBConfig.enableJointBlockSounds) {
         return false;
      } else {
         return !soundSlot.isDriverrOnly() || !(entity instanceof EntityJointed) || ((EntityJointed)entity).isRidingThisEntity(Minecraft.getInstance().player);
      }
   }

   public static WeighedSoundEvents getSoundEventAccessor(AbstractEffectAttributes soundSlot) {
      if (soundSlot.sound != null) {
         return soundSlot.sound.getSoundAccessor();
      } else {
         return soundSlot.soundNameLoc != null ? Minecraft.getInstance().getSoundManager().getSoundEvent(soundSlot.soundNameLoc) : null;
      }
   }

   public static void playJBSound(AbstractEffectAttributes soundSlot, WeighedSoundEvents accessor, EffectHelper effectHelper) {
      if (effectHelper.soundPlaying != null && !effectHelper.soundPlaying.isSoundSlotObjEquals(soundSlot)) {
         effectHelper.soundPlaying = null;
      }

      Minecraft mc = Minecraft.getInstance();
      if (soundSlot.isRepeat()) {
         if (effectHelper.soundPlaying != null && !effectHelper.soundPlaying.isStopped()) {
            if (effectHelper.soundPlaying instanceof PositionRelatedSoundRepeat) {
               ((PositionRelatedSoundRepeat)effectHelper.soundPlaying).extendAliveTick();
            }
         } else {
            effectHelper.soundPlaying = new PositionRelatedSoundRepeat(effectHelper, accessor, soundSlot);
            mc.getSoundManager().play(effectHelper.soundPlaying);
         }
      } else {
         if (mc.getSoundManager().isActive(effectHelper.soundPlaying)) {
            return;
         }

         effectHelper.soundPlaying = new PositionRelatedSound(effectHelper, accessor, soundSlot);
         mc.getSoundManager().play(effectHelper.soundPlaying);
      }

   }

   // OMITTED (deferred, needs SoundEngine rewrite for 1.20.1 OpenAL pipeline):
   //   handlePlaySoundHook(SoundHandler, ISound) - 1.12 paulscode SoundSystem injection
   //     (SoundSystem.newStreamingSource/newSource/setPitch/setVolume/play +
   //      PlaySoundSourceEvent/PlayStreamingSourceEvent, all removed in 1.20.1)
   //   getURLForSoundResource(ResourceLocation) - mcsounddomain: URLStreamHandler hook
   //     feeding paulscode from the resource manager.
}
