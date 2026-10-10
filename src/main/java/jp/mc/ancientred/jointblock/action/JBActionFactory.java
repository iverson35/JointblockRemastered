package jp.mc.ancientred.jointblock.action;

import java.util.Map;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBActionFactory;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JBActionFactory implements IJBActionFactory {
   public static ActionBlade defaultBlade = new ActionBlade();
   public static ActionFlamer defaultFlamer = new ActionFlamer();
   public static ActionDrill defaultDrill = new ActionDrill();
   public static ActionPlant defaultPlant = new ActionPlant();
   public static ActionHarvest defaultHarvest = new ActionHarvest();
   public static ActionThruster defaultThruster = new ActionThruster();
   public static ActionShootBullet defaultBullet = new ActionShootBullet();
   public static ActionSound defaultSound = new ActionSound();

   public IJBModelItem.IJBActionHandler createAction(String actionTypeName, Map<String, Object> properties) {
      if (actionTypeName != null && actionTypeName.length() != 0) {
         try {
            if ("blade".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultBlade;
               }

               return this.createActionBlade(properties);
            }

            if ("flamer".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultFlamer;
               }

               return this.createActionFlamer(properties);
            }

            if ("drill".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultDrill;
               }

               return this.createActionDrill(properties);
            }

            if ("plant".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultPlant;
               }

               return this.createActionPlant(properties);
            }

            if ("harvest".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultHarvest;
               }

               return this.createActionHarvest(properties);
            }

            if ("thruster".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultThruster;
               }

               return this.createActionThruster(properties);
            }

            if ("bullet".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultBullet;
               }

               return this.createActionBullet(properties);
            }

            if ("sound".equals(actionTypeName)) {
               if (properties == null) {
                  return defaultSound;
               }

               return this.createActionSound(properties);
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         return null;
      } else {
         return null;
      }
   }

   private IJBModelItem.IJBActionHandler createActionBlade(Map<String, Object> properties) {
      ActionBlade blade = new ActionBlade();
      if (this.containsNumber(properties, "damage")) {
         blade.damage = (float)this.getAsNumber(properties, "damage");
      }

      if (this.containsNumber(properties, "useEnergyMultA")) {
         blade.useEnergyMultA = (float)this.getAsNumber(properties, "useEnergyMultA");
      }

      if (this.containsNumber(properties, "useEnergyMultB")) {
         blade.useEnergyMultB = (float)this.getAsNumber(properties, "useEnergyMultB");
      }

      if (this.containsNumber(properties, "useEnergyHit")) {
         blade.useEnergyHit = (float)this.getAsNumber(properties, "useEnergyHit");
      }

      if (this.containsNumber(properties, "bladeColor")) {
         blade.bladeColor = this.getAsInteger(properties, "bladeColor");
      }

      if (this.containsBoolean(properties, "setMobFire")) {
         blade.setMobFire = this.getAsBoolean(properties, "setMobFire");
      }

      if (this.containsNumber(properties, "setMobPotionEffect")) {
         blade.setMobPotionEffect = this.getAsInteger(properties, "setMobPotionEffect");
      }

      if (this.containsNumber(properties, "potionAmplifier")) {
         blade.potionAmplifier = this.getAsInteger(properties, "potionAmplifier");
      }

      if (this.containsNumber(properties, "effectDuration")) {
         blade.effectDuration = this.getAsInteger(properties, "effectDuration");
      }

      return blade;
   }

   private IJBModelItem.IJBActionHandler createActionFlamer(Map<String, Object> properties) {
      ActionFlamer flamer = new ActionFlamer();
      if (this.containsNumber(properties, "damage")) {
         flamer.damage = (float)this.getAsNumber(properties, "damage");
      }

      if (this.containsNumber(properties, "useEnergyMultA")) {
         flamer.useEnergyMultA = (float)this.getAsNumber(properties, "useEnergyMultA");
      }

      if (this.containsNumber(properties, "useEnergyMultB")) {
         flamer.useEnergyMultB = (float)this.getAsNumber(properties, "useEnergyMultB");
      }

      if (this.containsNumber(properties, "useEnergyHit")) {
         flamer.useEnergyHit = (float)this.getAsNumber(properties, "useEnergyHit");
      }

      if (this.containsNumber(properties, "flameColor")) {
         flamer.effectColor = this.getAsInteger(properties, "flameColor");
      }

      if (this.containsBoolean(properties, "setBlockFire")) {
         flamer.setBlockFire = this.getAsBoolean(properties, "setBlockFire");
      }

      if (this.containsBoolean(properties, "setMobFire")) {
         flamer.setMobFire = this.getAsBoolean(properties, "setMobFire");
      }

      if (this.containsNumber(properties, "setMobPotionEffect")) {
         flamer.setMobPotionEffect = this.getAsInteger(properties, "setMobPotionEffect");
      }

      if (this.containsNumber(properties, "potionAmplifier")) {
         flamer.potionAmplifier = this.getAsInteger(properties, "potionAmplifier");
      }

      if (this.containsNumber(properties, "effectDuration")) {
         flamer.effectDuration = this.getAsInteger(properties, "effectDuration");
      }

      return flamer;
   }

   private IJBModelItem.IJBActionHandler createActionDrill(Map<String, Object> properties) {
      ActionDrill drill = new ActionDrill();
      if (this.containsNumber(properties, "useEnergy")) {
         drill.useEnergy = (float)this.getAsNumber(properties, "useEnergy");
      }

      return drill;
   }

   private IJBModelItem.IJBActionHandler createActionPlant(Map<String, Object> properties) {
      ActionPlant plant = new ActionPlant();
      if (this.containsNumber(properties, "useEnergy")) {
         plant.useEnergy = (float)this.getAsNumber(properties, "useEnergy");
      }

      return plant;
   }

   private IJBModelItem.IJBActionHandler createActionHarvest(Map<String, Object> properties) {
      ActionHarvest harvest = new ActionHarvest();
      if (this.containsNumber(properties, "useEnergy")) {
         harvest.useEnergy = (float)this.getAsNumber(properties, "useEnergy");
      }

      return harvest;
   }

   private IJBModelItem.IJBActionHandler createActionThruster(Map<String, Object> properties) {
      ActionThruster thruster = new ActionThruster();
      if (this.containsNumber(properties, "moveSpeed")) {
         thruster.moveSpeed = this.getAsNumber(properties, "moveSpeed");
      }

      if (this.containsNumber(properties, "speedLimit")) {
         thruster.speedLimit = this.getAsNumber(properties, "speedLimit");
      }

      if (this.containsNumber(properties, "shootSide")) {
         thruster.shootSide = (int)this.getAsNumber(properties, "shootSide");
         if (thruster.shootSide < 0 || thruster.shootSide > 5) {
            thruster.shootSide = 0;
         }
      }

      if (this.containsNumber(properties, "useEnergyMultA")) {
         thruster.useEnergyMultA = (float)this.getAsNumber(properties, "useEnergyMultA");
      }

      if (this.containsNumber(properties, "useEnergyMultB")) {
         thruster.useEnergyMultB = (float)this.getAsNumber(properties, "useEnergyMultB");
      }

      if (this.containsNumber(properties, "useEnergyMultC")) {
         thruster.useEnergyMultC = (float)this.getAsNumber(properties, "useEnergyMultC");
      }

      this.setupSound(thruster, properties);
      return thruster;
   }

   private IJBModelItem.IJBActionHandler createActionBullet(Map<String, Object> properties) {
      ActionShootBullet bullet = new ActionShootBullet();
      if (this.containsString(properties, "bulletType")) {
         String type = this.getAsString(properties, "bulletType");
         if ("lazer".equals(type)) {
            bullet.bulletType = EntityBullet.BulletType.lazerBeam;
         } else if ("rocket".equals(type)) {
            bullet.bulletType = EntityBullet.BulletType.rokect;
         } else if ("missile".equals(type)) {
            bullet.bulletType = EntityBullet.BulletType.missile;
         } else if ("mortar".equals(type)) {
            bullet.bulletType = EntityBullet.BulletType.mortar;
         }
      }

      if (this.containsNumber(properties, "damage")) {
         bullet.damage = (float)this.getAsNumber(properties, "damage");
      }

      if (this.containsNumber(properties, "speed")) {
         bullet.speed = (double)((float)this.getAsNumber(properties, "speed"));
      }

      if (this.containsNumber(properties, "shootSide")) {
         bullet.shootSide = (int)this.getAsNumber(properties, "shootSide");
         if (bullet.shootSide < 0 || bullet.shootSide > 5) {
            bullet.shootSide = 3;
         }
      }

      if (this.containsNumber(properties, "useEnergy")) {
         bullet.useEnergy = (float)this.getAsNumber(properties, "useEnergy");
      }

      if (this.containsNumber(properties, "coolTimeTick")) {
         bullet.coolTimeTick = (int)this.getAsNumber(properties, "coolTimeTick");
      }

      if (this.containsNumber(properties, "lazerLength")) {
         bullet.lazerLength = this.getAsNumber(properties, "lazerLength");
      }

      if (this.containsNumber(properties, "lazerWidth")) {
         bullet.lazerWidth = this.getAsNumber(properties, "lazerWidth");
      }

      if (this.containsNumber(properties, "lazerColor")) {
         bullet.lazerColor = this.getAsInteger(properties, "lazerColor");
      }

      if (this.containsNumber(properties, "accuracy")) {
         bullet.accuracy = (float)this.getAsNumber(properties, "accuracy");
      }

      if (this.containsBoolean(properties, "dummyExplode")) {
         bullet.dummyExplode = this.getAsBoolean(properties, "dummyExplode");
      }

      if (this.containsBoolean(properties, "explodeAffectsBlocks")) {
         bullet.explodeAffectsBlocks = this.getAsBoolean(properties, "explodeAffectsBlocks");
      }

      if (this.containsBoolean(properties, "explodeAffectsEntity")) {
         bullet.explodeAffectsEntity = this.getAsBoolean(properties, "explodeAffectsEntity");
      }

      if (this.containsNumber(properties, "explosionSize")) {
         bullet.explosionSize = (float)this.getAsNumber(properties, "explosionSize");
      }

      if (this.containsBoolean(properties, "ignoreDamageCooldown")) {
         bullet.ignoreDamageCooldown = this.getAsBoolean(properties, "ignoreDamageCooldown");
      }

      if (this.containsBoolean(properties, "cutExplosionNockBack")) {
         bullet.cutExplosionNockBack = this.getAsBoolean(properties, "cutExplosionNockBack");
      }

      if (this.containsBoolean(properties, "disableVT")) {
         bullet.disableVT = this.getAsBoolean(properties, "disableVT");
      }

      if (this.containsBoolean(properties, "setMobFire")) {
         bullet.setMobFire = this.getAsBoolean(properties, "setMobFire");
      }

      if (this.containsNumber(properties, "setMobPotionEffect")) {
         bullet.setMobPotionEffect = this.getAsInteger(properties, "setMobPotionEffect");
      }

      if (this.containsNumber(properties, "potionAmplifier")) {
         bullet.potionAmplifier = this.getAsInteger(properties, "potionAmplifier");
      }

      if (this.containsNumber(properties, "effectDuration")) {
         bullet.effectDuration = this.getAsInteger(properties, "effectDuration");
      }

      this.setupSound(bullet, properties);
      return bullet;
   }

   private IJBModelItem.IJBActionHandler createActionSound(Map<String, Object> properties) {
      ActionSound sound = new ActionSound();
      if (this.containsNumber(properties, "coolTimeTick")) {
         sound.coolTimeTick = (int)this.getAsNumber(properties, "coolTimeTick");
      }

      this.setupSound(sound, properties);
      return sound;
   }

   private void setupSound(AbstractSoundAction soundAct, Map<String, Object> properties) {
      if (properties.containsKey("soundName")) {
         String soundName = properties.get("soundName").toString().trim();
         if (soundName.startsWith("minecraft:")) {
            soundAct.sound = null;
            soundAct.soundNameLoc = new ResourceLocation(soundName.substring("minecraft:".length(), soundName.length()));
         } else {
            soundAct.sound = (IJBSound)JBModContainer.soundManager.soundMap.get(soundName);
            soundAct.soundNameLoc = null;
         }
      }

      if (this.containsNumber(properties, "soundVolume")) {
         soundAct.volume = (byte)Mth.clamp(Math.round((float)this.getAsNumber(properties, "soundVolume") * 10.0F), 0, 20);
      }

      if (this.containsNumber(properties, "soundPitch")) {
         soundAct.pitch = (byte)Mth.clamp(Math.round((float)this.getAsNumber(properties, "soundPitch") * 10.0F), 0, 20);
      }

      if (this.containsBoolean(properties, "soundRepeat")) {
         soundAct.setRepeat(this.getAsBoolean(properties, "soundRepeat"));
      }

      if (this.containsNumber(properties, "soundRepeatDelay")) {
         soundAct.repeatDelay = (byte)Mth.clamp(this.getAsInteger(properties, "soundRepeatDelay"), 0, 100);
      }

      if (this.containsNumber(properties, "soundKeepDuration")) {
         soundAct.keepDuration = (byte)Mth.clamp(this.getAsInteger(properties, "soundKeepDuration"), 0, 100);
      }

      if (this.containsNumber(properties, "soundFadeDuration")) {
         soundAct.fadeDuration = (byte)Mth.clamp(this.getAsInteger(properties, "soundFadeDuration"), 0, 100);
      }

      if (this.containsNumber(properties, "soundFadeBackDuration")) {
         soundAct.fadeBackDuration = (byte)Mth.clamp(this.getAsInteger(properties, "soundFadeBackDuration"), 0, 100);
      }

      if (this.containsBoolean(properties, "soundDriverOnly")) {
         soundAct.setDriverrOnly(this.getAsBoolean(properties, "soundDriverOnly"));
      }

      if (!soundAct.isRepeat() && this.containsBoolean(properties, "soundUnmanaged")) {
         soundAct.setUnManaged(this.getAsBoolean(properties, "soundUnmanaged"));
      }

   }

   private boolean containsBoolean(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         return obj instanceof Boolean;
      } else {
         return false;
      }
   }

   private boolean getAsBoolean(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         if (obj instanceof Boolean) {
            return (Boolean)obj;
         }
      }

      return false;
   }

   private boolean containsNumber(Map<String, Object> properties, String key) {
      if (!properties.containsKey(key)) {
         return false;
      } else {
         Object obj = properties.get(key);
         return obj instanceof Double || obj instanceof Integer || obj instanceof Float;
      }
   }

   private double getAsNumber(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         if (obj instanceof Double) {
            return (Double)obj;
         }

         if (obj instanceof Float) {
            return (double)(Float)obj;
         }

         if (obj instanceof Integer) {
            return (double)(Integer)obj;
         }
      }

      return 0.0;
   }

   private int getAsInteger(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         if (obj instanceof Double) {
            return ((Double)obj).intValue();
         }

         if (obj instanceof Float) {
            return ((Float)obj).intValue();
         }

         if (obj instanceof Integer) {
            return (Integer)obj;
         }
      }

      return 0;
   }

   private boolean containsString(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         return obj instanceof String;
      } else {
         return false;
      }
   }

   private String getAsString(Map<String, Object> properties, String key) {
      if (properties.containsKey(key)) {
         Object obj = properties.get(key);
         return (String)obj;
      } else {
         return "";
      }
   }
}
