package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeArthropod extends AbstractUnitBase {
   private static final float LARGEWEIGHT_YARDSTICK = 5.0F;
   int CASCADE_ID_FRONTLEFT = 5;
   int CASCADE_ID_FRONTRIGHT = 6;
   int CASCADE_ID_REARLEFT = 9;
   int CASCADE_ID_REARRIGHT = 10;

   public String getUnitTypeName() {
      return "jbunit.arthropod.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 1.4));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 1.4));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
      unitAbility.setJumpable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 140;
      unitAbility.moveStrafingSpeedRate = 140;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 6;
   }
}
