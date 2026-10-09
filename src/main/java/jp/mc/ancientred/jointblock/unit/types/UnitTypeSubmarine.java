package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class UnitTypeSubmarine extends AbstractUnitBase {
   public String getUnitTypeName() {
      return "jbunit.submarine.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 0.8));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 0.7));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
      unitAbility.setFloatable();
      unitAbility.setFloatOnly();
      unitAbility.setSubmarine();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 40;
      unitAbility.moveStrafingSpeedRate = 30;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 3;
   }

   public void updatePlayerEffect(EntityJointed entityJointed, Entity ridingEntity) {
      if (ridingEntity instanceof LivingEntity) {
         LivingEntity entityLivingBase = (LivingEntity)ridingEntity;
         if (entityLivingBase.isInWater()) {
            MobEffectInstance effect = entityLivingBase.getEffect(MobEffects.WATER_BREATHING);
            if (effect == null || effect.getDuration() < 400) {
               entityLivingBase.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 450));
            }

            effect = entityLivingBase.getEffect(MobEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() < 400) {
               entityLivingBase.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 450));
            }
         }
      }

   }
}
