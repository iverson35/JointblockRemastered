package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.common.JBRotationUtil;

public abstract class AbstractTwoSidedPattern extends AbstractSidedPattern {
   public int getPatternArgs(int valuePattern) {
      return valuePattern & 2047;
   }

   public int getPatternSideFace(int valuePattern) {
      return valuePattern >> 11 & 7;
   }

   public int getPatternSide(int valuePattern) {
      return this.getPatternSideFace(valuePattern) / 4;
   }

   public int getPatternFace(int valuePattern) {
      return this.getPatternFaceRaw(valuePattern) + 2;
   }

   public int getPatternFaceRaw(int valuePattern) {
      return this.getPatternSideFace(valuePattern) % 4;
   }

   public int clearPatternArgs(int valuePattern) {
      return valuePattern & -2048;
   }

   public int clearPatternSideFace(int valuePattern) {
      return valuePattern & 116735;
   }

   public int turnPatternSide(int valuePattern, int toggleDir) {
      int newSide = this.getPatternSide(valuePattern) + toggleDir;
      if (newSide < 0) {
         newSide = 1;
      }

      if (newSide >= 2) {
         newSide = 0;
      }

      return this.setNewSideFace(valuePattern, newSide, this.getPatternFace(valuePattern));
   }

   public int turnPatternFace(int valuePattern, int toggleDir) {
      int newFace = JBRotationUtil.rotateFace(this.getPatternFace(valuePattern), toggleDir > 0);
      return this.setNewSideFace(valuePattern, this.getPatternSide(valuePattern), newFace);
   }

   protected int setNewArgs(int valuePattern, int args) {
      return this.clearPatternArgs(valuePattern) | args & 2047;
   }

   protected int setNewSideFace(int valuePattern, int side, int face) {
      int newSideFace = side * 4 + (face - 2);
      return this.clearPatternSideFace(valuePattern) | newSideFace << 11;
   }

   protected int setNewArgsSideFace(int valuePattern, int args, int side, int face) {
      int newSideFace = side * 4 + (face - 2);
      return this.clearExceptId(valuePattern) | newSideFace << 11 | args & 2047;
   }

   public int flipCopyValueX(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      face = convertMutual(face, 3, 5, 2, 4);
      args = this.swapZX(args);
      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int flipCopyValueY(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      side = convertMutual(side, 0, 1);
      face = convertMutual(face, 3, 4, 2, 5);
      args = this.swapZX(args);
      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int flipCopyValueZ(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      face = convertMutual(face, 3, 4, 2, 5);
      args = this.swapZX(args);
      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int flipCopyValueYofXYZReversed(int valuePattern) {
      return this.flipCopyValueX(this.flipCopyValueZ(this.flipCopyValueY(valuePattern)));
   }

   public int rotCopyValue3to4(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      face = JBRotationUtil.rotateFace(face, side == 0);
      return this.setNewSideFace(valuePattern, side, face);
   }

   public int rotCopyValue0to2(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (face) {
         case 2:
            face = 5;
            args = this.swapZtoYtoXtoZ(args);
            break;
         case 3:
            side = convertMutual(side, 0, 1);
            face = 4;
            args = this.swapZtoYtoXtoZ(args);
            break;
         case 4:
            face = 3;
            args = this.swapXtoYtoZtoX(args);
            break;
         case 5:
            side = convertMutual(side, 0, 1);
            face = 2;
            args = this.swapXtoYtoZtoX(args);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to3(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (face) {
         case 2:
            side = convertMutual(side, 0, 1);
            face = 5;
            args = this.swapZtoYtoXtoZ(args);
            break;
         case 3:
            face = 4;
            args = this.swapZtoYtoXtoZ(args);
            break;
         case 4:
            side = convertMutual(side, 0, 1);
            face = 3;
            args = this.swapXtoYtoZtoX(args);
            break;
         case 5:
            face = 2;
            args = this.swapXtoYtoZtoX(args);
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   public int rotCopyValue0to4(int valuePattern) {
      int side = this.getPatternSide(valuePattern);
      int face = this.getPatternFace(valuePattern);
      int args = this.getPatternArgs(valuePattern);
      switch (side) {
         case 0:
            switch (face) {
               case 2:
                  side = convertMutual(side, 0, 1);
                  face = 5;
                  args = this.swapXtoYtoZtoX(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 3:
                  face = 5;
                  args = this.swapXtoYtoZtoX(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 4:
                  face = 2;
                  args = this.swapZtoYtoXtoZ(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 5:
                  side = convertMutual(side, 0, 1);
                  face = 2;
                  args = this.swapZtoYtoXtoZ(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               default:
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
            }
         case 1:
            switch (face) {
               case 2:
                  face = 4;
                  args = this.swapXtoYtoZtoX(args);
                  break;
               case 3:
                  side = convertMutual(side, 0, 1);
                  face = 4;
                  args = this.swapXtoYtoZtoX(args);
                  break;
               case 4:
                  side = convertMutual(side, 0, 1);
                  face = 3;
                  args = this.swapZtoYtoXtoZ(args);
                  break;
               case 5:
                  face = 3;
                  args = this.swapZtoYtoXtoZ(args);
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
            switch (face) {
               case 2:
                  face = 4;
                  args = this.swapXtoYtoZtoX(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 3:
                  side = convertMutual(side, 0, 1);
                  face = 4;
                  args = this.swapXtoYtoZtoX(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 4:
                  side = convertMutual(side, 0, 1);
                  face = 3;
                  args = this.swapZtoYtoXtoZ(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               case 5:
                  face = 3;
                  args = this.swapZtoYtoXtoZ(args);
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
               default:
                  return this.setNewArgsSideFace(valuePattern, args, side, face);
            }
         case 1:
            switch (face) {
               case 2:
                  side = convertMutual(side, 0, 1);
                  face = 5;
                  args = this.swapXtoYtoZtoX(args);
                  break;
               case 3:
                  face = 5;
                  args = this.swapXtoYtoZtoX(args);
                  break;
               case 4:
                  face = 2;
                  args = this.swapZtoYtoXtoZ(args);
                  break;
               case 5:
                  side = convertMutual(side, 0, 1);
                  face = 2;
                  args = this.swapZtoYtoXtoZ(args);
            }
      }

      return this.setNewArgsSideFace(valuePattern, args, side, face);
   }

   protected abstract int swapZtoYtoXtoZ(int var1);

   protected abstract int swapXtoYtoZtoX(int var1);

   protected abstract int swapXY(int var1);

   protected abstract int swapYZ(int var1);

   protected abstract int swapZX(int var1);
}
