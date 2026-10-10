package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeTank extends AbstractUnitBase {
   public String getUnitTypeName() {
      return "jbunit.wheel.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 1.6));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 1.6));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 85;
      unitAbility.moveStrafingSpeedRate = 60;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 1;
   }
}
