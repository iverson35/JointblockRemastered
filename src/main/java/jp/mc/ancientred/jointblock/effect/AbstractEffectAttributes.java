package jp.mc.ancientred.jointblock.effect;

import jp.mc.ancientred.jointblock.api.IJBSound;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractEffectAttributes {
   public static final String VANILLA_PREFIX = "minecraft:";
   public static final byte KEEP_DURATION_DEFAULT = 10;
   public static final byte KEEP_DURATION_MIN = 1;
   public static final byte DURATION_TICK_MAX = 100;
   public byte soundFlags = 1;
   public static final int FLG_AS_DEFAULT = 1;
   public static final int FLG_REPEAT = 2;
   public static final int FLG_DRIVER_ONLY = 4;
   public static final int FLG_IS_PARTICLE = 8;
   public static final int FLG_IS_UNMANAGED = 16;
   public byte volume = 10;
   public byte pitch = 10;
   public byte repeatDelay = 0;
   public byte keepDuration = 10;
   public byte fadeDuration = 0;
   public byte fadeBackDuration = 0;
   public String effectNameStr = "";
   public IJBSound sound;
   public ResourceLocation soundNameLoc;

   public boolean isAsDefault() {
      return (this.soundFlags & 1) != 0;
   }

   public void setAsDefault(boolean value) {
      this.soundFlags &= -2;
      if (value) {
         this.soundFlags = (byte)(this.soundFlags | 1);
      }

   }

   public boolean isRepeat() {
      return (this.soundFlags & 2) != 0;
   }

   public void setRepeat(boolean value) {
      this.soundFlags &= -3;
      if (value) {
         this.soundFlags = (byte)(this.soundFlags | 2);
      }

   }

   public boolean isDriverrOnly() {
      return (this.soundFlags & 4) != 0;
   }

   public void setDriverrOnly(boolean value) {
      this.soundFlags &= -5;
      if (value) {
         this.soundFlags = (byte)(this.soundFlags | 4);
      }

   }

   public boolean isParticle() {
      return (this.soundFlags & 8) != 0;
   }

   public void setParticle(boolean value) {
      this.soundFlags &= -9;
      if (value) {
         this.soundFlags = (byte)(this.soundFlags | 8);
      }

   }

   public boolean isUnManaged() {
      return (this.soundFlags & 16) != 0;
   }

   public void setUnManaged(boolean value) {
      this.soundFlags &= -17;
      if (value) {
         this.soundFlags = (byte)(this.soundFlags | 16);
      }

   }
}
