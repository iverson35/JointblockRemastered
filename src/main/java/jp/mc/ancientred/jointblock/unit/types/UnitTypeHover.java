package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeHover extends AbstractUnitBase {
   public static final int MOVE_FORWARD_SPEED = 250;
   public static final int MOVE_STRAFING_SPEED = 250;

   public String getUnitTypeName() {
      return "jbunit.hover.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 0.8));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 0.7));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
      unitAbility.setHoverable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 250;
      unitAbility.moveStrafingSpeedRate = 250;
      unitAbility.backWardSlowdownRate = 100;
      unitAbility.stepHight = 8;
   }
}
