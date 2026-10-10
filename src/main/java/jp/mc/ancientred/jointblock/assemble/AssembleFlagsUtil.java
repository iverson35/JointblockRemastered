package jp.mc.ancientred.jointblock.assemble;

public class AssembleFlagsUtil {
   public static final int FLG_AUTO_DEPLOY_UNITPARTS = 1;
   public static final int FLG_UNIT_NO_MOVE = 2;
   public static final int FLG_UNIT_BOARDIMMIDIATE = 4;
   public static final int FLG_UNIT_SLAVEMODE = 8;
   public static final int FLG_UNIT_SLAVEMOTINDIPENDENT = 16;
   public static final int FLG_UNIT_SLAVEOPINDIPENDENT = 32;
   public static final int FLG_UNIT_MANUAL_SET_LARGEWEIGHT = 64;
   public static final int FLG_UNIT_INFINITE_FUEL = 128;

   public static final boolean autoDeployUnitParts(int flags) {
      return (flags & 1) != 0;
   }

   public static final boolean noMove(int flags) {
      return (flags & 2) != 0;
   }

   public static final boolean boardImmidiate(int flags) {
      return (flags & 4) != 0;
   }

   public static final boolean isSlaveMode(int flags) {
      return (flags & 8) != 0;
   }

   public static final boolean isSlaveMotIndependent(int flags) {
      return (flags & 16) != 0;
   }

   public static final boolean isSlaveOpIndependent(int flags) {
      return (flags & 32) != 0;
   }

   public static final boolean isManualSetLargeWeight(int flags) {
      return (flags & 64) != 0;
   }

   public static final boolean isInfiniteFuel(int flags) {
      return (flags & 128) != 0;
   }

   public static final int setSlaveMode(int flags, boolean value) {
      return value ? flags | 8 : flags & -9;
   }

   public static int setSlaveMotIndependent(int flags, boolean value) {
      return value ? flags | 16 : flags & -17;
   }

   public static int setSlaveOpIndependent(int flags, boolean value) {
      return value ? flags | 32 : flags & -33;
   }

   public static int setManualSetLargeWeight(int flags, boolean value) {
      return value ? flags | 64 : flags & -65;
   }

   public static int setInfiniteFuel(int flags, boolean value) {
      return value ? flags | 128 : flags & -129;
   }
}
