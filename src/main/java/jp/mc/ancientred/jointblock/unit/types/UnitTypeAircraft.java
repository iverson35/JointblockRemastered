package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeAircraft extends AbstractUnitBase {
   public static final int MOVE_FORWARD_SPEED = 300;
   public static final int MOVE_STRAFING_SPEED = 100;

   public String getUnitTypeName() {
      return "jbunit.aircraft.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 0.4));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 0.8));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
      unitAbility.setExFlyable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 300;
      unitAbility.moveStrafingSpeedRate = 100;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 1;
   }
}
