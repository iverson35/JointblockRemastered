package jp.mc.ancientred.jointblock.carving.pattern;

import java.util.ArrayList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternCorner extends AbstractTwoSidedPattern {
   protected static final int JOINCORN_XZ = 1;
   protected static final int JOINCORN_YX = 2;
   protected static final int JOINCORN_ZY = 3;
   private static int[] TARGET_SIDES = new int[]{0, 1};

   public String getPatternShortName() {
      return "PCorner(A)";
   }

   public int getPatternArgs(int valuePattern) {
      return valuePattern & 2047;
   }

   public int getDefaultPatternValue(int faceTo) {
      int side = 0;
      int leftIdx = 3;
      int frntIdx = 3;
      int topYIdx = 3;
      int joinCrn = 0;
      int args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topYIdx;
      return this.setNewArgsSideFace(0, args, side, faceTo);
   }

   public int modArg0(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      leftIdx += toggleDir;
      if (leftIdx >= 8) {
         leftIdx = 7;
      }

      if (leftIdx < 0) {
         leftIdx = 0;
      }

      args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topYIdx;
      return this.clearPatternArgs(valuePattern) | args;
   }

   public int modArg1(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      frntIdx += toggleDir;
      if (frntIdx >= 8) {
         frntIdx = 7;
      }

      if (frntIdx < 0) {
         frntIdx = 0;
      }

      args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topYIdx;
      return this.clearPatternArgs(valuePattern) | args;
   }

   public int modArg2(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      topYIdx += toggleDir;
      if (topYIdx >= 8) {
         topYIdx = 7;
      }

      if (topYIdx < 0) {
         topYIdx = 0;
      }

      args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topYIdx;
      return this.clearPatternArgs(valuePattern) | args;
   }

   public int modArg3(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      joinCrn += toggleDir;
      int patternIndex = JBPatternCarvingUtil.getPatternIndexFor(valuePattern);
      if (patternIndex != 2 || joinCrn < 4 && joinCrn >= 0) {
         if (patternIndex == 3) {
            valuePattern = JBPatternCarvingUtil.modifyPatternIndex(valuePattern, 2);
            if (toggleDir > 0) {
               joinCrn = 0;
            } else {
               joinCrn = 3;
            }
         } else {
            if (joinCrn >= 4) {
               joinCrn = 0;
            }

            if (joinCrn < 0) {
               joinCrn = 3;
            }
         }
      } else {
         valuePattern = JBPatternCarvingUtil.modifyPatternIndex(valuePattern, 3);
         joinCrn = 0;
      }

      args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topYIdx;
      return this.clearPatternArgs(valuePattern) | args;
   }

   protected int swapXY(int args) {
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      joinCrn = convertMutual(joinCrn, 3, 1);
      return joinCrn << 9 | topYIdx << 6 | frntIdx << 3 | leftIdx;
   }

   protected int swapZX(int args) {
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      joinCrn = convertMutual(joinCrn, 2, 3);
      return joinCrn << 9 | frntIdx << 6 | leftIdx << 3 | topYIdx;
   }

   protected int swapYZ(int args) {
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      joinCrn = convertMutual(joinCrn, 1, 2);
      return joinCrn << 9 | leftIdx << 6 | topYIdx << 3 | frntIdx;
   }

   protected int swapZtoYtoXtoZ(int args) {
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      if (joinCrn == 2) {
         joinCrn = 1;
      } else if (joinCrn == 1) {
         joinCrn = 3;
      } else if (joinCrn == 3) {
         joinCrn = 2;
      }

      return joinCrn << 9 | topYIdx << 6 | leftIdx << 3 | frntIdx;
   }

   protected int swapXtoYtoZtoX(int args) {
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      if (joinCrn == 3) {
         joinCrn = 1;
      } else if (joinCrn == 1) {
         joinCrn = 2;
      } else if (joinCrn == 2) {
         joinCrn = 3;
      }

      return joinCrn << 9 | frntIdx << 6 | topYIdx << 3 | leftIdx;
   }

   @OnlyIn(Dist.CLIENT)
   protected int[] getTargetSides() {
      return TARGET_SIDES;
   }

   @OnlyIn(Dist.CLIENT)
   public void setUpRender(FixedDoubleArrayPool aryPool) {
      this.maxPatternArgs = 2048;
      this.sideFaceTrianglesOfArgs = new JBPatternTriangle[this.maxPatternArgs][][];

      for(int joinCrn = 0; joinCrn < 4; ++joinCrn) {
         for(int leftIdx = 0; leftIdx < 8; ++leftIdx) {
            for(int frntIdx = 0; frntIdx < 8; ++frntIdx) {
               for(int topUIdx = 0; topUIdx < 8; ++topUIdx) {
                  int args = joinCrn << 9 | leftIdx << 6 | frntIdx << 3 | topUIdx;
                  this.createPatternForSideFace(args, this.sideFaceTrianglesOfArgs, aryPool);
               }
            }
         }
      }

      this.sideFaceSubBounds = this.createBoundForSideFace(aryPool);
   }

   @OnlyIn(Dist.CLIENT)
   protected void addBaseS0F3Pattern(ArrayList<JBPatternTriangle> coordBaseS0F3, int args, FixedDoubleArrayPool aryPool) {
      int max = 4;
      int min = 0;
      aryPool.initOrReset();
      double cornrX = (double)max;
      double cornrY = (double)min;
      double cornrZ = (double)min;
      double[] corner = new double[]{cornrX, cornrY, cornrZ};
      int joinCrn = args >> 9 & 3;
      int leftIdx = args >> 6 & 7;
      int frntIdx = args >> 3 & 7;
      int topYIdx = args & 7;
      double[] topL;
      double[] topF;
      double[] topC;
      int topLx;
      if (topYIdx > 3) {
         topLx = max - (topYIdx - 3);
         int topRz = topYIdx - 3;
         topL = aryPool.get((double)topLx, (double)max, (double)min);
         topF = aryPool.get((double)max, (double)max, (double)topRz);
         topC = aryPool.get((double)max, (double)max, (double)min);
      } else {
         topLx = topYIdx + 1;
         topL = aryPool.get((double)max, (double)topLx, (double)min);
         topF = aryPool.get((double)max, (double)topLx, (double)min);
         topC = aryPool.get((double)max, (double)topLx, (double)min);
      }

      double[] leftC;
      int leftUy;
      double[] leftU;
      double[] leftF;
      if (leftIdx > 3) {
         leftUy = leftIdx - 3;
         int leftFz = leftIdx - 3;
         leftU = aryPool.get((double)min, (double)leftUy, (double)min);
         leftF = aryPool.get((double)min, (double)min, (double)leftFz);
         leftC = aryPool.get((double)min, (double)min, (double)min);
      } else {
         leftUy = max - (leftIdx + 1);
         leftU = aryPool.get((double)leftUy, (double)min, (double)min);
         leftF = aryPool.get((double)leftUy, (double)min, (double)min);
         leftC = aryPool.get((double)leftUy, (double)min, (double)min);
      }

      double[] rightC;
      int rightUy;
      double[] rightU;
      double[] rightR;
      if (frntIdx > 3) {
         rightUy = frntIdx - 3;
         int rightRx = max - (frntIdx - 3);
         rightU = aryPool.get((double)max, (double)rightUy, (double)max);
         rightR = aryPool.get((double)rightRx, (double)min, (double)max);
         rightC = aryPool.get((double)max, (double)min, (double)max);
      } else {
         rightUy = frntIdx + 1;
         rightU = aryPool.get((double)max, (double)min, (double)rightUy);
         rightR = aryPool.get((double)max, (double)min, (double)rightUy);
         rightC = aryPool.get((double)max, (double)min, (double)rightUy);
      }

      switch (joinCrn) {
         case 1:
            leftF[2] = frntIdx > 3 ? (double)max : (double)(frntIdx + 1);
            rightR[0] = leftIdx > 3 ? (double)min : (double)(max - (leftIdx + 1));
            break;
         case 2:
            topL[0] = leftIdx > 3 ? (double)min : (double)(max - (leftIdx + 1));
            leftU[1] = topYIdx > 3 ? (double)max : (double)(topYIdx + 1);
            break;
         case 3:
            topF[2] = frntIdx > 3 ? (double)max : (double)(frntIdx + 1);
            rightU[1] = topYIdx > 3 ? (double)max : (double)(topYIdx + 1);
      }

      double[] topC2 = aryPool.get(topL[0], topC[1], topF[2]);
      double[] leftC2 = aryPool.get(leftC[0], leftU[1], leftF[2]);
      double[] rightC2 = aryPool.get(rightR[0], rightU[1], rightC[2]);
      switch (joinCrn) {
         case 1:
            leftC2[1] = rightC2[1] = Math.min(leftC2[1], rightC2[1]);
            break;
         case 2:
            topC2[2] = leftC2[2] = Math.min(topC2[2], leftC2[2]);
            break;
         case 3:
            topC2[0] = rightC2[0] = Math.max(topC2[0], rightC2[0]);
      }

      this.addTris(coordBaseS0F3, topL, topF, topC);
      this.addTris(coordBaseS0F3, topL, topC, corner);
      this.addTris(coordBaseS0F3, topF, corner, topC);
      this.addTris(coordBaseS0F3, leftU, leftC, leftF);
      this.addTris(coordBaseS0F3, leftU, corner, leftC);
      this.addTris(coordBaseS0F3, leftF, leftC, corner);
      this.addTris(coordBaseS0F3, rightR, rightC, rightU);
      this.addTris(coordBaseS0F3, rightR, corner, rightC);
      this.addTris(coordBaseS0F3, rightU, rightC, corner);
      this.addTris(coordBaseS0F3, topL, corner, leftU);
      this.addTris(coordBaseS0F3, rightU, corner, topF);
      this.addTris(coordBaseS0F3, leftF, corner, rightR);
      this.addTris(coordBaseS0F3, topL, topC2, topF);
      this.addTris(coordBaseS0F3, leftF, leftC2, leftU);
      this.addTris(coordBaseS0F3, rightU, rightC2, rightR);
      switch (joinCrn) {
         case 1:
            this.addQuads(coordBaseS0F3, topC2, topL, leftU, leftC2);
            this.addQuads(coordBaseS0F3, topC2, rightC2, rightU, topF);
            break;
         case 2:
            this.addQuads(coordBaseS0F3, rightC2, rightU, topF, topC2);
            this.addQuads(coordBaseS0F3, rightC2, leftC2, leftF, rightR);
            break;
         case 3:
            this.addQuads(coordBaseS0F3, leftC2, topC2, topL, leftU);
            this.addQuads(coordBaseS0F3, leftC2, leftF, rightR, rightC2);
            break;
         default:
            this.addQuads(coordBaseS0F3, topC2, topL, leftU, leftC2);
            this.addQuads(coordBaseS0F3, rightC2, rightU, topF, topC2);
            this.addQuads(coordBaseS0F3, leftC2, leftF, rightR, rightC2);
      }

      this.addTris(coordBaseS0F3, topC2, leftC2, rightC2);
   }

   @OnlyIn(Dist.CLIENT)
   protected void addSOF3HidBounds(ArrayList<PatternArgsBBSet> boudsBaseS0F3, FixedDoubleArrayPool aryPool) {
      PatternArgsBB aabbA = this.getBBFixed(-16776961, 0.0, 0.0, 0.0, 2.0, 0.5, 0.5);
      PatternArgsBB aabbB = this.getBBFixed(-11184811, 0.0, 0.0, 0.0, 0.5, 2.0, 0.5);
      PatternArgsBB aabbC = this.getBBFixed(-11184811, 0.0, 0.0, 0.0, 0.5, 0.5, 2.0);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 0, new PatternArgsBB[]{aabbA, aabbB, aabbC}));
      aabbA = this.getBBFixed(-11184811, 4.0, 0.0, 4.0, -2.0, 0.5, -0.5);
      aabbB = this.getBBFixed(-11184811, 4.0, 0.0, 4.0, -0.5, 2.0, -0.5);
      aabbC = this.getBBFixed(-16776961, 4.0, 0.0, 4.0, -0.5, 0.5, -2.0);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 1, new PatternArgsBB[]{aabbA, aabbB, aabbC}));
      aabbA = this.getBBFixed(-11184811, 4.0, 4.0, 0.0, -2.0, -0.5, 0.5);
      aabbB = this.getBBFixed(-16776961, 4.0, 4.0, 0.0, -0.5, -2.0, 0.5);
      aabbC = this.getBBFixed(-11184811, 4.0, 4.0, 0.0, -0.5, -0.5, 2.0);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 2, new PatternArgsBB[]{aabbA, aabbB, aabbC}));
      aabbA = this.getBBFixed(-65536, 0.0, 4.0, 4.0, 0.5, -0.5, -0.5);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 3, new PatternArgsBB[]{aabbA}));
      this.addSideFaceBounds(boudsBaseS0F3);
   }
}
