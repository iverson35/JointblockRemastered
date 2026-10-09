package jp.mc.ancientred.jointblock.carving.pattern;

import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class JBPatternCarvingClientUtil {
   public static final PatternHolder[] patternRenderWorkTable = new PatternHolder[16];

   public static void setUpPatternsForRender() {
      FixedDoubleArrayPool aryPool = new FixedDoubleArrayPool();

      int i;
      for(i = 0; i < 16; ++i) {
         patternRenderWorkTable[i] = new PatternHolder();
      }

      for(i = 0; i < JBPatternCarvingUtil.patternTable.length; ++i) {
         if (JBPatternCarvingUtil.patternTable[i] != null) {
            JBPatternCarvingUtil.patternTable[i].setUpRender(aryPool);
         }
      }

   }

   public static void renderPatternCarvedModel(VertexConsumer consumer, Matrix4f matrix, int[] carvingData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         int actualLen = JBCarvingDataUtil.getCVDataLenFromDiv(2, divideCount) - 1;
         int lastIdx = 0;

         int i;
         int boxIndexCnt;
         for(i = actualLen; i > 0 && i < carvingData.length - 1; ++i) {
            boxIndexCnt = carvingData[i];
            int firstTableIdx = i - actualLen << 1;
            int first = boxIndexCnt & '\uffff' | (carvingData[actualLen] >> firstTableIdx & 1) << 16;
            int secondTabelIdx = (i - actualLen << 1) + 1;
            int second = boxIndexCnt >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
            setPatternHolderState(patternRenderWorkTable[firstTableIdx], first);
            lastIdx = secondTabelIdx;
            setPatternHolderState(patternRenderWorkTable[secondTabelIdx], second);
         }

         for(i = lastIdx + 1; i < 16; ++i) {
            patternRenderWorkTable[i].pp = null;
         }

         boxIndexCnt = -1;
         double unitd = 1.0 / (double)divideCount;

         for(int y = 0; y < divideCount; ++y) {
            double minY = (double)y * unitd;

            for(int z = 0; z < divideCount; ++z) {
               double minZ = (double)z * unitd;

               for(int x = 0; x < divideCount; ++x) {
                  ++boxIndexCnt;
                  double minX = (double)x * unitd;
                  int mod = (boxIndexCnt & 3) << 3;
                  int dat = carvingData[boxIndexCnt >> 2] >>> mod & 255;
                  int cIndex = dat >> 4;
                  if (cIndex != 0 && (cIndex & 8) == 0) {
                     int pIndex = dat & 15;
                     PatternHolder pth = patternRenderWorkTable[pIndex];
                     if (pth.pp != null) {
                        pth.pp.drawPattern(consumer, matrix, minX, minY, minZ, unitd, pth.rotSideFace, pth.args, palletData[cIndex]);
                     }
                  }
               }
            }
         }

      }
   }

   public static void setPatternHolderState(PatternHolder patternHolder, int valuePattern) {
      patternHolder.pp = JBPatternCarvingUtil.patternTable[valuePattern >> 14 & 7];
      if (patternHolder.pp != null) {
         patternHolder.rotSideFace = patternHolder.pp.getPatternSideFace(valuePattern);
         patternHolder.args = patternHolder.pp.getPatternArgs(valuePattern);
      }

   }

   public static void rayTracePatternSubBounds(int[] carvingData, CarvingRayTraceResult result, Vec3 vecPos, Vec3 velLookRay, int hitType, AABB aabbWork) {
      int valuePattern = JBCarvingDataUtil.getPatternValueFromTable(carvingData, result.commonHitValue);
      if (valuePattern != -1) {
         JBCarvingPattern pattern = JBPatternCarvingUtil.patternTable[valuePattern >> 14 & 7];
         if (pattern != null) {
            int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
            if (divideCount >= 1) {
               double unitd = 1.0 / (double)divideCount;
               pattern.rayTraceSubBounds(result, vecPos, velLookRay, hitType, unitd, pattern.getPatternSideFace(valuePattern), aabbWork);
            }
         }
      }
   }

   public static class PatternHolder {
      public JBCarvingPattern pp;
      public int rotSideFace;
      public int args;
   }
}
