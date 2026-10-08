package jp.mc.ancientred.jointblock.carving.pattern;

import java.util.ArrayList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternCornerB extends PatternCorner {
   public String getPatternShortName() {
      return "PCorner(B)";
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
      double[] center;
      if (topYIdx == leftIdx && leftIdx == frntIdx) {
         center = aryPool.get((topF[0] + topL[0] + rightR[0] + rightU[0] + leftF[0] + leftU[0]) / 6.0, (topF[1] + topL[1] + rightR[1] + rightU[1] + leftF[1] + leftU[1]) / 6.0, (topF[2] + topL[2] + rightR[2] + rightU[2] + leftF[2] + leftU[2]) / 6.0);
      } else if (topYIdx == leftIdx) {
         center = aryPool.get((topF[0] + leftF[0]) / 2.0, (topF[1] + leftF[1]) / 2.0, (topF[2] + leftF[2]) / 2.0);
      } else if (topYIdx == frntIdx) {
         center = aryPool.get((topL[0] + rightR[0]) / 2.0, (topL[1] + rightR[1]) / 2.0, (topL[2] + rightR[2]) / 2.0);
      } else if (leftIdx == frntIdx) {
         center = aryPool.get((rightU[0] + leftU[0]) / 2.0, (rightU[1] + leftU[1]) / 2.0, (rightU[2] + leftU[2]) / 2.0);
      } else {
         center = aryPool.get((topF[0] + topL[0] + rightR[0] + rightU[0] + leftF[0] + leftU[0]) / 6.0, (topF[1] + topL[1] + rightR[1] + rightU[1] + leftF[1] + leftU[1]) / 6.0, (topF[2] + topL[2] + rightR[2] + rightU[2] + leftF[2] + leftU[2]) / 6.0);
      }

      this.addTris(coordBaseS0F3, topL, center, topF);
      this.addTris(coordBaseS0F3, leftF, center, leftU);
      this.addTris(coordBaseS0F3, rightU, center, rightR);
      this.addTris(coordBaseS0F3, topF, center, rightU);
      this.addTris(coordBaseS0F3, leftU, center, topL);
      this.addTris(coordBaseS0F3, rightR, center, leftF);
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
