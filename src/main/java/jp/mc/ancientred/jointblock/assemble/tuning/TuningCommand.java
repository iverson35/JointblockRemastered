package jp.mc.ancientred.jointblock.assemble.tuning;

import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;

public class TuningCommand {
   private static final int AXIS_INDEX_0 = 0;
   private static final int AXIS_INDEX_X = 0;
   private static final int AXIS_INDEX_Y = 1;
   private static final int AXIS_INDEX_Z = 2;
   public boolean isCommandAccepted;
   public short jointRefId;
   public int additionalBarNum;
   public int additionalBarOffset;
   public int additionalBarSide;
   public int[] modifyCombination;
   public byte initLayer;
   public byte initParamIndex;
   public short newValue;
   private static final int[][] EX_SC_MOD_COMB_ARY = new int[][]{{0, 1}, {0, -1}};
   private static final int[][] ROTATOR_MOD_COMB_ARY = new int[][]{{0, 1}, {0, -1}, {1, 1}, {1, -1}, {2, 1}, {2, -1}};
   private static final int[][] SLIDER_MOD_COMB_ARY = new int[][]{{0, 1}, {0, -1}, {1, 1}, {1, -1}, {2, 1}, {2, -1}};

   public boolean createCommandFromTuningInf(EntityJointed entityJointed, short jointRefId, byte additionalBarNum, byte additionalBarOffset, byte additionalBarSide) {
      this.isCommandAccepted = false;
      JointParts joint = entityJointed.getJointPartsFromRefId(jointRefId);
      if (joint != null && !joint.isDestructed && !entityJointed.isRemoved()) {
         this.jointRefId = jointRefId;
         this.additionalBarNum = additionalBarNum;
         this.additionalBarOffset = additionalBarOffset;
         this.additionalBarSide = additionalBarSide;
         short[] initVals = joint.getCurrentInitVals();
         if (initVals == null) {
            return false;
         } else {
            switch (joint.getJointType()) {
               case 8:
                  this.modifyCombination = getModCombForRotator(this.additionalBarNum, this.additionalBarSide);
                  if (this.modifyCombination != null) {
                     this.isCommandAccepted = true;
                  }
               case 9:
               case 11:
               case 13:
               default:
                  break;
               case 10:
               case 12:
                  if (this.additionalBarNum == 0) {
                     this.modifyCombination = EX_SC_MOD_COMB_ARY[0];
                     this.isCommandAccepted = true;
                  }

                  if (this.additionalBarNum == 1) {
                     this.modifyCombination = EX_SC_MOD_COMB_ARY[1];
                     this.isCommandAccepted = true;
                  }
                  break;
               case 14:
                  this.modifyCombination = getModCombForSlider(this.additionalBarNum, this.additionalBarSide);
                  if (this.modifyCombination != null) {
                     this.isCommandAccepted = true;
                  }
            }

            return this.isCommandAccepted;
         }
      } else {
         return false;
      }
   }

   public boolean validaeAndCommitTuning(EntityJointed entityJointed) {
      JointParts joint = entityJointed.getJointPartsFromRefId(this.jointRefId);
      if (joint != null && !joint.isDestructed && !entityJointed.isRemoved()) {
         short valueMin;
         short valueMax;
         switch (joint.getJointType()) {
            case 8:
               valueMin = -359;
               valueMax = 359;
               break;
            case 9:
            case 11:
            case 13:
            default:
               return false;
            case 10:
               valueMin = 0;
               valueMax = 300;
               break;
            case 12:
               valueMin = 0;
               valueMax = 300;
               break;
            case 14:
               valueMin = -128;
               valueMax = 128;
         }

         short[] initVals = joint.getCurrentInitVals();
         if (initVals == null) {
            return false;
         } else if (this.modifyCombination != null && this.modifyCombination[0] < initVals.length) {
            int targetInitParamIndex = this.modifyCombination[0];
            int numberDirection = this.modifyCombination[1];
            int valueOLd = initVals[targetInitParamIndex];
            if (numberDirection < 0 && valueOLd <= valueMin) {
               return false;
            } else if (numberDirection > 0 && valueOLd >= valueMax) {
               return false;
            } else {
               int modValue = (int)Math.pow(10.0, (double)this.additionalBarOffset) * numberDirection;
               int valueNew = valueOLd + modValue;
               if (valueNew < valueMin) {
                  valueNew = valueMin;
               }

               if (valueNew > valueMax) {
                  valueNew = valueMax;
               }

               this.initLayer = entityJointed.getInitLayerMode();
               this.initParamIndex = (byte)targetInitParamIndex;
               this.newValue = (short)valueNew;
               return joint.updateAndForceInitValue(this.initLayer, this.initParamIndex, this.newValue, JBConfig.maxInitLayers - 1);
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static final int[] getModCombForRotator(int additionalBarNum, int additionalBarSide) {
      int[] modComb = null;
      switch (additionalBarNum) {
         case 0:
            if (additionalBarSide == 2) {
               modComb = ROTATOR_MOD_COMB_ARY[1];
            }

            if (additionalBarSide == 3) {
               modComb = ROTATOR_MOD_COMB_ARY[0];
            }
            break;
         case 1:
            if (additionalBarSide == 2) {
               modComb = ROTATOR_MOD_COMB_ARY[0];
            }

            if (additionalBarSide == 3) {
               modComb = ROTATOR_MOD_COMB_ARY[1];
            }
            break;
         case 2:
            if (additionalBarSide == 4) {
               modComb = ROTATOR_MOD_COMB_ARY[2];
            }

            if (additionalBarSide == 5) {
               modComb = ROTATOR_MOD_COMB_ARY[3];
            }
            break;
         case 3:
            if (additionalBarSide == 4) {
               modComb = ROTATOR_MOD_COMB_ARY[3];
            }

            if (additionalBarSide == 5) {
               modComb = ROTATOR_MOD_COMB_ARY[2];
            }
            break;
         case 4:
            if (additionalBarSide == 0) {
               modComb = ROTATOR_MOD_COMB_ARY[5];
            }

            if (additionalBarSide == 1) {
               modComb = ROTATOR_MOD_COMB_ARY[4];
            }
            break;
         case 5:
            if (additionalBarSide == 0) {
               modComb = ROTATOR_MOD_COMB_ARY[4];
            }

            if (additionalBarSide == 1) {
               modComb = ROTATOR_MOD_COMB_ARY[5];
            }
      }

      return modComb;
   }

   public static final int[] getModCombForSlider(int additionalBarNum, int additionalBarSide) {
      int[] modComb = null;
      switch (additionalBarNum) {
         case 0:
            modComb = ROTATOR_MOD_COMB_ARY[2];
            break;
         case 1:
            modComb = ROTATOR_MOD_COMB_ARY[3];
            break;
         case 2:
            modComb = ROTATOR_MOD_COMB_ARY[5];
            break;
         case 3:
            modComb = ROTATOR_MOD_COMB_ARY[4];
            break;
         case 4:
            modComb = ROTATOR_MOD_COMB_ARY[1];
            break;
         case 5:
            modComb = ROTATOR_MOD_COMB_ARY[0];
      }

      return modComb;
   }
}
