package jp.mc.ancientred.jointblock.unit;

import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeAircraft;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeArthropod;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeBike;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeCar;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeFloat;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeFourLegs;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeHeli;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeHover;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeSubmarine;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeTank;
import jp.mc.ancientred.jointblock.unit.types.UnitTypeTwoLegs;
import net.minecraft.world.entity.Entity;

public abstract class AbstractUnitBase {
   protected static final int CASCADE_ID_LEFT = 1;
   protected static final int CASCADE_ID_RIGHT = 2;
   protected static final int CASCADE_ID_FRONT = 4;
   protected static final int CASCADE_ID_REAR = 8;
   protected static final int CASCADE_ID_FRONTLEFT = 5;
   protected static final int CASCADE_ID_FRONTRIGHT = 6;
   protected static final int CASCADE_ID_REARLEFT = 9;
   protected static final int CASCADE_ID_REARRIGHT = 10;
   public static int[] commonJointTypesANY_TYPE = new int[]{8, 10, 4, 2, 12, 14, 15};
   public static int[] commonJointTypesENScSlAt = new int[]{10, 4, 12, 14, 15};
   public static int[] commonJointTypesEN = new int[]{10, 4};
   public static int[] commonJointTypesN = new int[]{4};
   private static int UNITBASE_MAXID = 7;
   private static AbstractUnitBase[] UNIT_TYPES = new AbstractUnitBase[]{null, new UnitTypeTwoLegs(), new UnitTypeTank(), new UnitTypeHeli(), new UnitTypeHover(), new UnitTypeFourLegs(), new UnitTypeArthropod(), new UnitTypeBike(), new UnitTypeCar(), new UnitTypeFloat(), new UnitTypeAircraft(), new UnitTypeSubmarine()};

   public static AbstractUnitBase getUnitBaseById(int unitBaseId) {
      return unitBaseId >= 0 && unitBaseId < UNIT_TYPES.length ? UNIT_TYPES[unitBaseId] : null;
   }

   public static int getPrevUnitBaseId(int unitBaseId) {
      --unitBaseId;
      return unitBaseId < 0 ? UNIT_TYPES.length - 1 : unitBaseId;
   }

   public static int getNextUnitBaseId(int unitBaseId) {
      ++unitBaseId;
      return unitBaseId > UNIT_TYPES.length - 1 ? 0 : unitBaseId;
   }

   public abstract String getUnitTypeName();

   public abstract void setUnitHealthStatesAtServer(EntityJointed var1);

   public abstract void setUnitMovementAbility(UnitAbility var1);

   public abstract void setUnitMovementStates(UnitAbility var1);

   public void updatePlayerEffect(EntityJointed entityJointed, Entity ridingEntity) {
   }

   public String getExceptionalResultString(int partsId, int dataA, int dataB) {
      return "";
   }

   public String getExceptionalResultValueString(int partsId, int dataA, int dataB) {
      return "";
   }
}
