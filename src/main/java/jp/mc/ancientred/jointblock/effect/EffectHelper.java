package jp.mc.ancientred.jointblock.effect;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EffectHelper {
   public double posX;
   public double posY;
   public double posZ;
   public double velX;
   public double velY;
   public double velZ;
   public final Entity entity;
   @OnlyIn(Dist.CLIENT)
   public IJBRelatedSound soundPlaying;

   public EffectHelper() {
      this.entity = null;
   }

   public EffectHelper(Entity entity) {
      this.entity = entity;
   }

   public EffectHelper(EffectHelper other) {
      this.entity = other.entity;
      this.posX = other.posX;
      this.posY = other.posY;
      this.posZ = other.posZ;
      this.velX = other.velX;
      this.velY = other.velY;
      this.velZ = other.velZ;
   }

   public float getPosX() {
      return this.entity != null ? (float)this.entity.getX() : (float)this.posX;
   }

   public float getPosY() {
      return this.entity != null ? (float)this.entity.getY() : (float)this.posY;
   }

   public float getPosZ() {
      return this.entity != null ? (float)this.entity.getZ() : (float)this.posZ;
   }

   @OnlyIn(Dist.CLIENT)
   public void clearPlayingSounds() {
      this.soundPlaying = null;
   }
}
