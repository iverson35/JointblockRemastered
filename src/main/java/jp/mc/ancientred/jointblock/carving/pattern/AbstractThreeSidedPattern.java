package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.common.JBRotationUtil;

public abstract class AbstractThreeSidedPattern extends AbstractSidedPattern {
   public int getPatternArgs(int valuePattern) {
      return valuePattern & 1023;
   }

   public int getPatternSideFace(int valuePattern) {
      return valuePattern >> 10 & 15;
   }

   public int getPatternSide(int valuePattern) {
      return this.getPatternSideRaw(valuePattern) * 2;
   }

   public int getPatternSideRaw(int valuePattern) {
      int sideRaw = this.getPatternSideFace(valuePattern) / 4;
      return sideRaw >= 3 ? 0 : sideRaw;
   }

   public int getPatternFace(int valuePattern) {
      return this.getPatternFaceRaw(valuePattern) + 2;
   }

   public int getPatternFaceRaw(int valuePattern) {
      return this.getPatternSideFace(valuePattern) % 4;
   }

   public int clearPatternArgs(int valuePattern) {
      return valuePattern & -1024;
   }

   public int clearPatternSideFace(int valuePattern) {
      return valuePattern & 115711;
   }

   public int turnPatternSide(int valuePattern, int toggleDir) {
      int newSideRaw = this.getPatternSideRaw(valuePattern) + toggleDir;
      if (newSideRaw < 0) {
         newSideRaw = 2;
      }

      if (newSideRaw >= 3) {
         newSideRaw = 0;
      }

      return this.setNewSideFace(valuePattern, newSideRaw * 2, this.getPatternFace(valuePattern));
   }

   public int turnPatternFace(int valuePattern, int toggleDir) {
      int newFace = JBRotationUtil.rotateFace(this.getPatternFace(valuePattern), toggleDir > 0);
      return this.setNewSideFace(valuePattern, this.getPatternSide(valuePattern), newFace);
   }

   protected int setNewArgs(int valuePattern, int args) {
      return this.clearPatternArgs(valuePattern) | args & 1023;
   }

   protected int setNewSideFace(int valuePattern, int side, int face) {
      int newSideFace = side / 2 * 4 + (face - 2);
      return this.clearPatternSideFace(valuePattern) | newSideFace << 10;
   }

   protected int setNewArgsSideFace(int valuePattern, int args, int side, int face) {
      int newSideFace = side / 2 * 4 + (face - 2);
      return this.clearExceptId(valuePattern) | newSideFace << 10 | args & 1023;
   }

   public int flipCopyValueX(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
         case 2:
            face = convertMutual(face, 3, 5, 2, 4);
            args = this.swapZX(args);
         default:
            return this.setNewArgsSideFace(valuePattern, args, side, face);
      }
   }

   public int flipCopyValueY(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 2:
            face = convertMutual(face, 3, 4, 2, 5);
            args = this.swapZX(args);
            break;
         case 4:
            face = convertMutual(face, 3, 5, 2, 4);
            args = this.swapZX(args);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int flipCopyValueZ(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
         case 4:
            face = convertMutual(face, 3, 4, 2, 5);
            args = this.swapZX(args);
         default:
            return this.setNewArgsSideFace(valuePattern, args, side, face);
      }
   }

   public int flipCopyValueYofXYZReversed(int valuePattern) {
      return this.flipCopyValueX(this.flipCopyValueZ(this.flipCopyValueY(valuePattern)));
   }

   public int rotCopyValue3to4(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            face = JBRotationUtil.rotateFace(face, true);
         case 1:
         case 3:
         default:
            break;
         case 2:
            side = 4;
            face = convertMutual(face, 3, 2);
            args = this.swapZX(args);
            break;
         case 4:
            side = 2;
            face = JBRotationUtil.rotateFace(face, true);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to2(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            side = 2;
         case 1:
         case 3:
         default:
            break;
         case 2:
            side = 0;
            args = this.swapZX(args);
            switch (face) {
               case 2:
                  face = 5;
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 3:
                  face = 4;
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 4:
                  face = 3;
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 5:
                  face = 2;
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               default:
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
            }
         case 4:
            face = JBRotationUtil.rotateFace(face, false);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to3(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            side = 2;
            args = this.swapZX(args);
            switch (face) {
               case 2:
                  face = 5;
                  break;
               case 3:
                  face = 4;
                  break;
               case 4:
                  face = 3;
                  break;
               case 5:
                  face = 2;
            }
         case 1:
         case 3:
         default:
            break;
         case 2:
            side = 0;
            break;
         case 4:
            face = JBRotationUtil.rotateFace(face, true);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to4(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            side = 4;
         case 1:
         case 3:
         default:
            break;
         case 2:
            face = JBRotationUtil.rotateFace(face, true);
            break;
         case 4:
            side = 0;
            args = this.swapZX(args);
            switch (face) {
               case 2:
                  face = 4;
                  break;
               case 3:
                  face = 5;
                  break;
               case 4:
                  face = 2;
                  break;
               case 5:
                  face = 3;
            }
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to5(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            side = 4;
            args = this.swapZX(args);
            switch (face) {
               case 2:
                  face = 4;
                  break;
               case 3:
                  face = 5;
                  break;
               case 4:
                  face = 2;
                  break;
               case 5:
                  face = 3;
            }
         case 1:
         case 3:
         default:
            break;
         case 2:
            face = JBRotationUtil.rotateFace(face, false);
            break;
         case 4:
            side = 0;
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   protected abstract int swapZX(int var1);
}
