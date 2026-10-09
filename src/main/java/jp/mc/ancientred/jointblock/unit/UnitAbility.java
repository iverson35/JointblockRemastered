package jp.mc.ancientred.jointblock.unit;

public class UnitAbility {
   public static int UNIT_ABILITY_MOVEABLE = 1;
   public static int UNIT_ABILITY_JUMPABLE = 2;
   public static int UNIT_ABILITY_DAHSABLE = 4;
   public static int UNIT_ABILITY_FLYABLE = 8;
   public static int UNIT_ABILITY_HOVERABLE = 16;
   public static int UNIT_ABILITY_FLOATABLE = 32;
   public static int UNIT_ABILITY_FLOATONLY = 64;
   public static int UNIT_ABILITY_EXFLYABLE = 128;
   public static int UNIT_ABILITY_SUBMARINE = 256;
   private int abilityFlag;
   public short moveStrafingSpeedRate;
   public short moveForwardSpeedRate;
   public short backWardSlowdownRate;
   public byte stepHight;
   public float jumpPower;
   public float thrusterPower;
   public float energyUseRate;
   public byte energyHealSpeed;

   public UnitAbility() {
      this.clearAll();
   }

   public void clearAll() {
      this.clearMovementStates();
   }

   public void clearMovementStates() {
      this.abilityFlag = 0;
      this.moveStrafingSpeedRate = 100;
      this.moveForwardSpeedRate = 100;
      this.backWardSlowdownRate = 40;
      this.stepHight = 0;
      this.jumpPower = 1.0F;
      this.thrusterPower = 1.0F;
      this.energyUseRate = 1.0F;
      this.energyHealSpeed = 30;
   }

   public boolean isMovable() {
      return (this.abilityFlag & UNIT_ABILITY_MOVEABLE) != 0;
   }

   public void setMovable() {
      this.abilityFlag |= UNIT_ABILITY_MOVEABLE;
   }

   public boolean isJumpable() {
      return (this.abilityFlag & UNIT_ABILITY_JUMPABLE) != 0;
   }

   public void setJumpable() {
      this.abilityFlag |= UNIT_ABILITY_JUMPABLE;
   }

   public boolean isDashable() {
      return (this.abilityFlag & UNIT_ABILITY_DAHSABLE) != 0;
   }

   public void setDashable() {
      this.abilityFlag |= UNIT_ABILITY_DAHSABLE;
   }

   public boolean isFlyable() {
      return (this.abilityFlag & UNIT_ABILITY_FLYABLE) != 0;
   }

   public void setFlyable() {
      this.abilityFlag |= UNIT_ABILITY_FLYABLE;
   }

   public boolean isExFlyable() {
      return (this.abilityFlag & UNIT_ABILITY_EXFLYABLE) != 0;
   }

   public void setExFlyable() {
      this.abilityFlag |= UNIT_ABILITY_EXFLYABLE;
   }

   public boolean isHoverable() {
      return (this.abilityFlag & UNIT_ABILITY_HOVERABLE) != 0;
   }

   public void setHoverable() {
      this.abilityFlag |= UNIT_ABILITY_HOVERABLE;
   }

   public boolean isFloatble() {
      return (this.abilityFlag & UNIT_ABILITY_FLOATABLE) != 0;
   }

   public void setFloatable() {
      this.abilityFlag |= UNIT_ABILITY_FLOATABLE;
   }

   public boolean isFloatOnly() {
      return (this.abilityFlag & UNIT_ABILITY_FLOATONLY) != 0;
   }

   public void setFloatOnly() {
      this.abilityFlag |= UNIT_ABILITY_FLOATONLY;
   }

   public boolean isSubmarine() {
      return (this.abilityFlag & UNIT_ABILITY_SUBMARINE) != 0;
   }

   public void setSubmarine() {
      this.abilityFlag |= UNIT_ABILITY_SUBMARINE;
   }
}
