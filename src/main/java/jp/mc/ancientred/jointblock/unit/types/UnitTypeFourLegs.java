package jp.mc.ancientred.jointblock.unit.types;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

public class UnitTypeFourLegs extends AbstractUnitBase {
   private static final float LARGEWEIGHT_YARDSTICK = 5.0F;

   public String getUnitTypeName() {
      return "jbunit.fourleg.name";
   }

   public void setUnitHealthStatesAtServer(EntityJointed ej) {
      ej.maxJBHealth = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitHealth * 1.4));
      ej.maxJBEnergy = (short)((int)Math.min(32767.0, (double)JBConfig.baseUnitEnergy * 1.2));
   }

   public void setUnitMovementAbility(UnitAbility unitAbility) {
      unitAbility.setMovable();
      unitAbility.setJumpable();
   }

   public void setUnitMovementStates(UnitAbility unitAbility) {
      unitAbility.moveForwardSpeedRate = 220;
      unitAbility.moveStrafingSpeedRate = 70;
      unitAbility.stepHight = 6;
   }
}
