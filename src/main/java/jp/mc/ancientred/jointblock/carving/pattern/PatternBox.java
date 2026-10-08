package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PatternBox extends JBCarvingPattern {
   public int getPatternSideFace(int patternValue) {
      return 0;
   }

   public int getPatternSide(int valuePattern) {
      return 0;
   }

   public int getPatternFace(int valuePattern) {
      return 0;
   }

   public int getPatternFaceRaw(int valuePattern) {
      return 0;
   }

   public int getPatternArgs(int valuePattern) {
      return 0;
   }

   public int clearPatternArgs(int valuePattern) {
      return 0;
   }

   public int clearPatternSideFace(int valuePattern) {
      return 0;
   }

   protected int setNewArgs(int valuePattern, int args) {
      return 0;
   }

   protected int setNewSideFace(int valuePattern, int side, int face) {
      return 0;
   }

   protected int setNewArgsSideFace(int valuePattern, int args, int side, int face) {
      return 0;
   }

   public String getPatternShortName() {
      return "PCube";
   }

   public void setUpRender(FixedDoubleArrayPool aryPool) {
   }

   public void prebuildPattern(Prebuilder prebuilder, double minX, double minY, double minZ, double unit, int rotSideFace, int patternArgs) {
      double maxX = minX + unit;
      double maxY = minY + unit;
      double maxZ = minZ + unit;
      prebuilder.addVertex(minX, minY, maxZ);
      prebuilder.addVertex(minX, minY, minZ);
      prebuilder.addVertex(maxX, minY, minZ);
      prebuilder.addVertex(maxX, minY, minZ);
      prebuilder.addVertex(maxX, minY, maxZ);
      prebuilder.addVertex(minX, minY, maxZ);
      prebuilder.addVertex(maxX, maxY, maxZ);
      prebuilder.addVertex(maxX, maxY, minZ);
      prebuilder.addVertex(minX, maxY, minZ);
      prebuilder.addVertex(minX, maxY, minZ);
      prebuilder.addVertex(minX, maxY, maxZ);
      prebuilder.addVertex(maxX, maxY, maxZ);
      prebuilder.addVertex(minX, maxY, minZ);
      prebuilder.addVertex(maxX, maxY, minZ);
      prebuilder.addVertex(maxX, minY, minZ);
      prebuilder.addVertex(maxX, minY, minZ);
      prebuilder.addVertex(minX, minY, minZ);
      prebuilder.addVertex(minX, maxY, minZ);
      prebuilder.addVertex(minX, maxY, maxZ);
      prebuilder.addVertex(minX, minY, maxZ);
      prebuilder.addVertex(maxX, minY, maxZ);
      prebuilder.addVertex(maxX, minY, maxZ);
      prebuilder.addVertex(maxX, maxY, maxZ);
      prebuilder.addVertex(minX, maxY, maxZ);
      prebuilder.addVertex(minX, maxY, maxZ);
      prebuilder.addVertex(minX, maxY, minZ);
      prebuilder.addVertex(minX, minY, minZ);
      prebuilder.addVertex(minX, minY, minZ);
      prebuilder.addVertex(minX, minY, maxZ);
      prebuilder.addVertex(minX, maxY, maxZ);
      prebuilder.addVertex(maxX, minY, maxZ);
      prebuilder.addVertex(maxX, minY, minZ);
      prebuilder.addVertex(maxX, maxY, minZ);
      prebuilder.addVertex(maxX, maxY, minZ);
      prebuilder.addVertex(maxX, maxY, maxZ);
      prebuilder.addVertex(maxX, minY, maxZ);
   }

   public void rayTraceSubBounds(CarvingRayTraceResult result, Vec3 vecPos, Vec3 velLookRay, int hitType, double unit, int rotSideFace, AABB aabbWork) {
   }
}
