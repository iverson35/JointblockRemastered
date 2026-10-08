package jp.mc.ancientred.jointblock.carving.pattern;

import java.util.ArrayList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternSlope extends AbstractThreeSidedPattern {
   private static int[] TARGET_SIDES = new int[]{0, 2, 4};
   private static final int[][] LEFTS = new int[][]{{3, 0}, {2, 0}, {1, 0}, {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}};
   private static final int[][] FRNTS = new int[][]{{4, 1}, {4, 2}, {4, 3}, {4, 4}, {3, 4}, {2, 4}, {1, 4}, {0, 4}};

   public String getPatternShortName() {
      return "PSlope";
   }

   public int getPatternArgs(int valuePattern) {
      return valuePattern & 63;
   }

   public int getDefaultPatternValue(int faceTo) {
      int side = 4;
      int face = 3;
      switch (faceTo) {
         case 2:
            side = 4;
            face = 4;
            break;
         case 3:
            side = 4;
            face = 3;
            break;
         case 4:
            side = 2;
            face = 4;
            break;
         case 5:
            side = 2;
            face = 2;
      }

      int leftIdx = 3;
      int frontIdx = 3;
      int args = leftIdx << 3 | frontIdx;
      return this.setNewArgsSideFace(0, args, side, face);
   }

   public int modArg0(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int leftIdx = args >> 3 & 7;
      int frontIdx = args & 7;
      leftIdx += toggleDir;
      if (leftIdx >= 8) {
         leftIdx = 7;
      }

      if (leftIdx < 0) {
         leftIdx = 0;
      }

      args = leftIdx << 3 | frontIdx;
      return this.setNewArgs(valuePattern, args);
   }

   public int modArg1(int valuePattern, int toggleDir) {
      int args = this.getPatternArgs(valuePattern);
      int leftIdx = args >> 3 & 7;
      int frontIdx = args & 7;
      frontIdx += toggleDir;
      if (frontIdx >= 8) {
         frontIdx = 7;
      }

      if (frontIdx < 0) {
         frontIdx = 0;
      }

      args = leftIdx << 3 | frontIdx;
      return this.setNewArgs(valuePattern, args);
   }

   protected int swapZX(int args) {
      int leftIdx = args >> 3 & 7;
      int frontIdx = args & 7;
      return frontIdx << 3 | leftIdx;
   }

   @OnlyIn(Dist.CLIENT)
   protected int[] getTargetSides() {
      return TARGET_SIDES;
   }

   @OnlyIn(Dist.CLIENT)
   public void setUpRender(FixedDoubleArrayPool aryPool) {
      this.maxPatternArgs = 64;
      this.sideFaceTrianglesOfArgs = new JBPatternTriangle[this.maxPatternArgs][][];

      for(int leftIdx = 0; leftIdx < 8; ++leftIdx) {
         for(int frontIdx = 0; frontIdx < 8; ++frontIdx) {
            int args = leftIdx << 3 | frontIdx;
            this.createPatternForSideFace(args, this.sideFaceTrianglesOfArgs, aryPool);
         }
      }

      this.sideFaceSubBounds = this.createBoundForSideFace(aryPool);
   }

   @OnlyIn(Dist.CLIENT)
   protected void addBaseS0F3Pattern(ArrayList<JBPatternTriangle> coordBaseS0F3, int args, FixedDoubleArrayPool aryPool) {
      int max = 4;
      int min = 0;
      aryPool.initOrReset();
      int leftIdx = args >> 3 & 7;
      int frontIdx = args & 7;
      int leftX = LEFTS[leftIdx][0];
      int leftZ = LEFTS[leftIdx][1];
      int frntX = FRNTS[frontIdx][0];
      int frntZ = FRNTS[frontIdx][1];
      double[] cornrTop = aryPool.get((double)max, (double)max, (double)min);
      double[] cornrBot = aryPool.get((double)max, (double)min, (double)min);
      double[] leftCTop = aryPool.get((double)leftX, (double)max, (double)min);
      double[] leftCBot = aryPool.get((double)leftX, (double)min, (double)min);
      double[] frntCTop = aryPool.get((double)max, (double)max, (double)frntZ);
      double[] frntCBot = aryPool.get((double)max, (double)min, (double)frntZ);
      double[] leftZTop = aryPool.get((double)leftX, (double)max, (double)leftZ);
      double[] leftZBot = aryPool.get((double)leftX, (double)min, (double)leftZ);
      double[] frntXTop = aryPool.get((double)frntX, (double)max, (double)frntZ);
      double[] frntXBot = aryPool.get((double)frntX, (double)min, (double)frntZ);
      this.addTris(coordBaseS0F3, leftZTop, frntXTop, cornrTop);
      this.addTris(coordBaseS0F3, frntXBot, leftZBot, cornrBot);
      this.addQuads(coordBaseS0F3, leftZTop, leftZBot, frntXBot, frntXTop);
      this.addQuads(coordBaseS0F3, cornrTop, cornrBot, leftCBot, leftCTop);
      this.addQuads(coordBaseS0F3, frntCTop, frntCBot, cornrBot, cornrTop);
      this.addQuads(coordBaseS0F3, leftCTop, leftCBot, leftZBot, leftZTop);
      this.addTris(coordBaseS0F3, leftCTop, leftZTop, cornrTop);
      this.addTris(coordBaseS0F3, leftZBot, leftCBot, cornrBot);
      this.addQuads(coordBaseS0F3, frntXTop, frntXBot, frntCBot, frntCTop);
      this.addTris(coordBaseS0F3, frntXTop, frntCTop, cornrTop);
      this.addTris(coordBaseS0F3, frntCBot, frntXBot, cornrBot);
   }

   @OnlyIn(Dist.CLIENT)
   protected void addSOF3HidBounds(ArrayList<PatternArgsBBSet> boudsBaseS0F3, FixedDoubleArrayPool aryPool) {
      PatternArgsBB aabbA = this.getBBFixed(-16776961, 0.0, 0.0, 0.0, 2.0, 4.0, 0.5);
      PatternArgsBB aabbB = this.getBBFixed(-11184811, 0.0, 0.0, 0.0, 0.5, 4.0, 2.0);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 0, new PatternArgsBB[]{aabbA, aabbB}));
      aabbA = this.getBBFixed(-16776961, 4.0, 0.0, 4.0, -0.5, 4.0, -2.0);
      aabbB = this.getBBFixed(-11184811, 4.0, 0.0, 4.0, -2.0, 4.0, -0.5);
      boudsBaseS0F3.add(new PatternArgsBBSet(0, 1, new PatternArgsBB[]{aabbA, aabbB}));
      this.addSideFaceBounds(boudsBaseS0F3);
   }
}
