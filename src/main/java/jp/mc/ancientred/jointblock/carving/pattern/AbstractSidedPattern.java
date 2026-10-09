package jp.mc.ancientred.jointblock.carving.pattern;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public abstract class AbstractSidedPattern extends JBCarvingPattern {
   protected static final double BOX_SIZE = 2.0;
   protected static final double SMALLBOX_SIZE = 0.5;
   protected static final double STICKBOX_LNGSIZE = 2.0;
   protected static final double STICKBOX_SRTSIZE = 0.5;
   private static final int[] FACE_ORDER = new int[]{4, 2, 5, 3};
   private static final ArrayList<JBPatternTriangle> coordBaseS0F3Work = new ArrayList();
   private static final ArrayList<PatternArgsBBSet> boudsBaseS0F3Work = new ArrayList();
   @OnlyIn(Dist.CLIENT)
   protected int maxPatternArgs;
   @OnlyIn(Dist.CLIENT)
   protected JBPatternTriangle[][][] sideFaceTrianglesOfArgs;
   @OnlyIn(Dist.CLIENT)
   protected PatternArgsBBSet[][] sideFaceSubBounds;

   @OnlyIn(Dist.CLIENT)
   public void createPatternForSideFace(int args, JBPatternTriangle[][][] sideFaceTrianglesOfArgs, FixedDoubleArrayPool aryPool) {
      coordBaseS0F3Work.clear();
      this.addBaseS0F3Pattern(coordBaseS0F3Work, args, aryPool);
      JBPatternTriangle[] coordsSideBaseS0F3 = (JBPatternTriangle[])coordBaseS0F3Work.toArray(new JBPatternTriangle[0]);
      int[] sidesAry = this.getTargetSides();
      JBPatternTriangle[][] sideFaceTriangles = new JBPatternTriangle[sidesAry.length * 4][];

      for(int sideIdx = 0; sideIdx < sidesAry.length; ++sideIdx) {
         int side = sidesAry[sideIdx];
         JBPatternTriangle[] turnBaseCoords = coordsSideBaseS0F3;

         int faceIndex;
         int faceFixed;
         JBPatternTriangle[] turnedCoords;
         int i;
         for(faceIndex = 0; faceIndex < FACE_ORDER.length; ++faceIndex) {
            faceFixed = FACE_ORDER[faceIndex] - 2;
            turnedCoords = new JBPatternTriangle[turnBaseCoords.length];

            for(i = 0; i < turnBaseCoords.length; ++i) {
               turnedCoords[i] = turnBaseCoords[i].cloneTurn3to4This();
            }

            sideFaceTriangles[sideIdx * 4 + faceFixed] = turnedCoords;
            turnBaseCoords = turnedCoords;
         }

         for(faceIndex = 0; faceIndex < FACE_ORDER.length; ++faceIndex) {
            faceFixed = FACE_ORDER[faceIndex] - 2;
            turnedCoords = sideFaceTriangles[sideIdx * 4 + faceFixed];

            for(i = 0; i < turnBaseCoords.length; ++i) {
               switch (side) {
                  case 0:
                  default:
                     break;
                  case 1:
                     turnedCoords[i].turn0to3();
                     turnedCoords[i].turn0to3();
                     break;
                  case 2:
                     turnedCoords[i].turn0to2();
                     break;
                  case 3:
                     turnedCoords[i].turn0to3();
                     break;
                  case 4:
                     turnedCoords[i].turn0to4();
                     break;
                  case 5:
                     turnedCoords[i].turn0to5();
               }
            }
         }
      }

      sideFaceTrianglesOfArgs[args] = sideFaceTriangles;
   }

   @OnlyIn(Dist.CLIENT)
   public PatternArgsBBSet[][] createBoundForSideFace(FixedDoubleArrayPool aryPool) {
      boudsBaseS0F3Work.clear();
      this.addSOF3HidBounds(boudsBaseS0F3Work, aryPool);
      PatternArgsBBSet[] boundsBaseS0F3 = (PatternArgsBBSet[])boudsBaseS0F3Work.toArray(new PatternArgsBBSet[0]);
      int[] sidesAry = this.getTargetSides();
      PatternArgsBBSet[][] sideFaceBounds = new PatternArgsBBSet[sidesAry.length * 4][];
      double rotBBCenterX = 2.0;
      double rotBBCenterY = 2.0;
      double rotBBCenterZ = 2.0;

      for(int sideIdx = 0; sideIdx < sidesAry.length; ++sideIdx) {
         int side = sidesAry[sideIdx];
         PatternArgsBBSet[] turnBaseBBs = boundsBaseS0F3;

         int faceIndex;
         int faceFixed;
         PatternArgsBBSet[] turnedBBs;
         int i;
         int j;
         for(faceIndex = 0; faceIndex < FACE_ORDER.length; ++faceIndex) {
            faceFixed = FACE_ORDER[faceIndex] - 2;
            turnedBBs = new PatternArgsBBSet[turnBaseBBs.length];

            for(i = 0; i < turnBaseBBs.length; ++i) {
               turnedBBs[i] = turnBaseBBs[i].copy();

               for(j = 0; j < turnedBBs[i].bbAry.length; ++j) {
                  turnedBBs[i].bbAry[j] = (PatternArgsBB)JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat3to4);
               }
            }

            sideFaceBounds[sideIdx * 4 + faceFixed] = turnedBBs;
            turnBaseBBs = turnedBBs;
         }

         for(faceIndex = 0; faceIndex < FACE_ORDER.length; ++faceIndex) {
            faceFixed = FACE_ORDER[faceIndex] - 2;
            turnedBBs = sideFaceBounds[sideIdx * 4 + faceFixed];

            for(i = 0; i < turnedBBs.length; ++i) {
               for(j = 0; j < turnedBBs[i].bbAry.length; ++j) {
                  switch (side) {
                     case 0:
                     default:
                        break;
                     case 1:
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to3);
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to3);
                        break;
                     case 2:
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to2);
                        break;
                     case 3:
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to3);
                        break;
                     case 4:
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to4);
                        break;
                     case 5:
                        JBRotationUtil.rotateAABBAt(turnedBBs[i].bbAry[j], rotBBCenterX, rotBBCenterY, rotBBCenterZ, JBRotationUtil.matirxRoat0to5);
                  }
               }
            }
         }
      }

      return sideFaceBounds;
   }

   protected PatternArgsBB getBBFixed(int color, double minX, double minY, double minZ, double xLen, double yLen, double zLen) {
      double fixedXMin;
      double fixedXMax;
      if (xLen < 0.0) {
         fixedXMax = minX;
         fixedXMin = minX + xLen;
      } else {
         fixedXMax = minX + xLen;
         fixedXMin = minX;
      }

      double fixedYMin;
      double fixedYMax;
      if (yLen < 0.0) {
         fixedYMax = minY;
         fixedYMin = minY + yLen;
      } else {
         fixedYMax = minY + yLen;
         fixedYMin = minY;
      }

      double fixedZMin;
      double fixedZMax;
      if (zLen < 0.0) {
         fixedZMax = minZ;
         fixedZMin = minZ + zLen;
      } else {
         fixedZMax = minZ + zLen;
         fixedZMin = minZ;
      }

      return new PatternArgsBB(color, fixedXMin, fixedYMin, fixedZMin, fixedXMax, fixedYMax, fixedZMax);
   }

   @OnlyIn(Dist.CLIENT)
   protected void addSideFaceBounds(ArrayList<PatternArgsBBSet> boudsBaseS0F3) {
      PatternArgsBB aabbA = this.getBBFixed(-16776961, 1.75, 0.5, 2.0, 0.5, 3.5, 2.0);
      PatternArgsBB aabbB = this.getBBFixed(-65536, 0.0, 0.0, 0.0, 4.0, 0.5, 4.0);
      PatternArgsBB aabbC = this.getBBFixed(-1, 0.0, 0.0, 0.0, 4.0, 4.0, 4.0);
      boudsBaseS0F3.add(new PatternArgsBBSet(1, -1, new PatternArgsBB[]{aabbA, aabbB, aabbC}));
   }

   @OnlyIn(Dist.CLIENT)
   protected abstract void addBaseS0F3Pattern(ArrayList<JBPatternTriangle> var1, int var2, FixedDoubleArrayPool var3);

   @OnlyIn(Dist.CLIENT)
   protected abstract void addSOF3HidBounds(ArrayList<PatternArgsBBSet> var1, FixedDoubleArrayPool var2);

   @OnlyIn(Dist.CLIENT)
   protected abstract int[] getTargetSides();

   @OnlyIn(Dist.CLIENT)
   public void prebuildPattern(Prebuilder prebuilder, double minX, double minY, double minZ, double unit, int rotSideFace, int patternArgs) {
      if (patternArgs <= this.maxPatternArgs) {
         double unitDiv = unit / 4.0;
         JBPatternTriangle[] coords = this.sideFaceTrianglesOfArgs[patternArgs][rotSideFace];
         int i = 0;

         for(int len = coords.length; i < len; ++i) {
            JBPatternTriangle tris = coords[i];
            if (tris.enabled) {
               prebuilder.addVertex(minX + tris.vertA.x * unitDiv, minY + tris.vertA.y * unitDiv, minZ + tris.vertA.z * unitDiv);
               prebuilder.addVertex(minX + tris.vertB.x * unitDiv, minY + tris.vertB.y * unitDiv, minZ + tris.vertB.z * unitDiv);
               prebuilder.addVertex(minX + tris.vertC.x * unitDiv, minY + tris.vertC.y * unitDiv, minZ + tris.vertC.z * unitDiv);
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public void drawPattern(VertexConsumer consumer, Matrix4f matrix, double minX, double minY, double minZ, double unit, int rotSideFace, int patternArgs, int color) {
      if (patternArgs <= this.maxPatternArgs) {
         double unitDiv = unit / 4.0;
         JBPatternTriangle[] coords = this.sideFaceTrianglesOfArgs[patternArgs][rotSideFace];
         int i = 0;

         for(int len = coords.length; i < len; ++i) {
            JBPatternTriangle tris = coords[i];
            if (tris.enabled) {
               // 1.12.2 drew the pattern triangles with GL lighting on: shade by their own normal.
               int shaded = JBCarvingClientUtil.shade(color, JBCarvingClientUtil.faceLight(tris.normalX, tris.normalY, tris.normalZ));
               int cb = shaded >> 16 & 255;
               int cg = shaded >> 8 & 255;
               int cr = shaded & 255;
               consumer.vertex(matrix, (float)(minX + tris.vertA.x * unitDiv), (float)(minY + tris.vertA.y * unitDiv), (float)(minZ + tris.vertA.z * unitDiv)).color(cr, cg, cb, 255).normal(tris.normalX, tris.normalY, tris.normalZ).endVertex();
               consumer.vertex(matrix, (float)(minX + tris.vertB.x * unitDiv), (float)(minY + tris.vertB.y * unitDiv), (float)(minZ + tris.vertB.z * unitDiv)).color(cr, cg, cb, 255).normal(tris.normalX, tris.normalY, tris.normalZ).endVertex();
               consumer.vertex(matrix, (float)(minX + tris.vertC.x * unitDiv), (float)(minY + tris.vertC.y * unitDiv), (float)(minZ + tris.vertC.z * unitDiv)).color(cr, cg, cb, 255).normal(tris.normalX, tris.normalY, tris.normalZ).endVertex();
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public void rayTraceSubBounds(CarvingRayTraceResult baseTraceResult, Vec3 vecPos, Vec3 velLookRay, int hitType, double unit, int rotSideFace, AABB aabbWork) {
      double unitDiv = unit / 4.0;
      PatternArgsBBSet[] subBoundsSetsAry = this.sideFaceSubBounds[rotSideFace];
      double minX = baseTraceResult.aabb.minX;
      double minY = baseTraceResult.aabb.minY;
      double minZ = baseTraceResult.aabb.minZ;
      double disanceMin = Double.MAX_VALUE;

      int i;
      for(i = 0; i < subBoundsSetsAry.length; ++i) {
         PatternArgsBBSet subBoundSets = subBoundsSetsAry[i];
         if (subBoundSets != null && subBoundSets.bbAry != null && subBoundSets.type == hitType) {
            for(int j = 0; j < subBoundSets.bbAry.length; ++j) {
               AABB subBound = subBoundSets.bbAry[j];
               JBCommonUtil.copyBBTo(subBound, aabbWork);
               JBCommonUtil.multBB(aabbWork, unitDiv);
               JBCommonUtil.offsetBB(aabbWork, minX, minY, minZ);
               Vec3 vec32 = JBCarvingClientUtil.getIntermediateWithXValue(vecPos, velLookRay, aabbWork.minX);
               Vec3 vec33 = JBCarvingClientUtil.getIntermediateWithXValue(vecPos, velLookRay, aabbWork.maxX);
               Vec3 vec34 = JBCarvingClientUtil.getIntermediateWithYValue(vecPos, velLookRay, aabbWork.minY);
               Vec3 vec35 = JBCarvingClientUtil.getIntermediateWithYValue(vecPos, velLookRay, aabbWork.maxY);
               Vec3 vec36 = JBCarvingClientUtil.getIntermediateWithZValue(vecPos, velLookRay, aabbWork.minZ);
               Vec3 vec37 = JBCarvingClientUtil.getIntermediateWithZValue(vecPos, velLookRay, aabbWork.maxZ);
               if (!JBCarvingClientUtil.isVecInsideYZBounds(aabbWork, vec32)) {
                  vec32 = null;
               }

               if (!JBCarvingClientUtil.isVecInsideYZBounds(aabbWork, vec33)) {
                  vec33 = null;
               }

               if (!JBCarvingClientUtil.isVecInsideXZBounds(aabbWork, vec34)) {
                  vec34 = null;
               }

               if (!JBCarvingClientUtil.isVecInsideXZBounds(aabbWork, vec35)) {
                  vec35 = null;
               }

               if (!JBCarvingClientUtil.isVecInsideXYBounds(aabbWork, vec36)) {
                  vec36 = null;
               }

               if (!JBCarvingClientUtil.isVecInsideXYBounds(aabbWork, vec37)) {
                  vec37 = null;
               }

               Vec3 vec38 = null;
               if (vec32 != null && (vec38 == null || vecPos.distanceToSqr(vec32) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec32;
               }

               if (vec33 != null && (vec38 == null || vecPos.distanceToSqr(vec33) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec33;
               }

               if (vec34 != null && (vec38 == null || vecPos.distanceToSqr(vec34) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec34;
               }

               if (vec35 != null && (vec38 == null || vecPos.distanceToSqr(vec35) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec35;
               }

               if (vec36 != null && (vec38 == null || vecPos.distanceToSqr(vec36) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec36;
               }

               if (vec37 != null && (vec38 == null || vecPos.distanceToSqr(vec37) < vecPos.distanceToSqr(vec38))) {
                  vec38 = vec37;
               }

               if (vec38 != null) {
                  double distance = vec38.distanceTo(vecPos);
                  if (distance < disanceMin) {
                     disanceMin = distance;
                     baseTraceResult.aabbSubBound = subBoundSets;
                  }
               }
            }
         }
      }

      if (baseTraceResult != null && baseTraceResult.aabbSubBound != null) {
         baseTraceResult.aabbSubBound = baseTraceResult.aabbSubBound.copy();
         if (baseTraceResult.aabbSubBound.bbAry != null) {
            for(i = 0; i < baseTraceResult.aabbSubBound.bbAry.length; ++i) {
               AABB aabbRes = baseTraceResult.aabbSubBound.bbAry[i];
               aabbRes = JBCommonUtil.multBB(aabbRes, unitDiv);
               JBCommonUtil.offsetBB(aabbRes, minX, minY, minZ);
            }
         }
      }

   }
}
