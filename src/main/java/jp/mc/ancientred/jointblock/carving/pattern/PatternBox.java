package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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

   public void drawPattern(VertexConsumer consumer, Matrix4f matrix, double minX, double minY, double minZ, double unit, int rotSideFace, int patternArgs, int color) {
      double maxX = minX + unit;
      double maxY = minY + unit;
      double maxZ = minZ + unit;
      // 1.12.2 rendered these quads with GL lighting on, so each face carries the light-model shade.
      int cDown = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(0.0F, -1.0F, 0.0F));
      int cUp = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(0.0F, 1.0F, 0.0F));
      int cN = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(0.0F, 0.0F, -1.0F));
      int cS = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(0.0F, 0.0F, 1.0F));
      int cW = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(-1.0F, 0.0F, 0.0F));
      int cE = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(1.0F, 0.0F, 0.0F));
      quad(consumer, matrix, cDown, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, 0.0F, -1.0F, 0.0F);
      quad(consumer, matrix, cUp, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, 0.0F, 1.0F, 0.0F);
      quad(consumer, matrix, cN, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, 0.0F, 0.0F, -1.0F);
      quad(consumer, matrix, cS, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, 0.0F, 0.0F, 1.0F);
      quad(consumer, matrix, cW, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, -1.0F, 0.0F, 0.0F);
      quad(consumer, matrix, cE, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, 1.0F, 0.0F, 0.0F);
   }

   /** Two triangles per face, matching the 1.12.2 {@code GL_TRIANGLES} emit exactly. */
   private static void quad(VertexConsumer consumer, Matrix4f matrix, int color,
         double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3,
         float nx, float ny, float nz) {
      int cb = color >> 16 & 255;
      int cg = color >> 8 & 255;
      int cr = color & 255;
      consumer.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x3, (float)y3, (float)z3).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
   }

   public void rayTraceSubBounds(CarvingRayTraceResult result, Vec3 vecPos, Vec3 velLookRay, int hitType, double unit, int rotSideFace, AABB aabbWork) {
   }
}
