package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeCar extends AbstractUnitBase {
   public String getUnitTypeName() {
      return "jbunit.automobile.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 0.3));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 0.6));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 200;
      unitAbility.moveStrafingSpeedRate = 0;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 1;
   }
}
