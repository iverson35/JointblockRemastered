package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
