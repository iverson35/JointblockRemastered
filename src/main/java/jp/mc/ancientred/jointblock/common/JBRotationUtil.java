package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JBRotationUtil {
   public static final int[][] side_matrix = new int[][]{{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
   public static final float[][] rotXZ_matrix = new float[][]{{0.0F, 0.0F}, {-1.0F, 0.0F}, {0.5F, 0.0F}, {-0.5F, 0.0F}, {0.0F, 0.5F}, {0.0F, -0.5F}};
   public static final double[] matirxRoatUpToDownD_YN = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
   public static final double[] matirxRoatDownTOupD_YP = new double[]{1.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, -1.0};
   public static final double[] matirxRoatEastTOwestD_XN = new double[]{0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
   public static final double[] matirxRoatWestTOeastD_XP = new double[]{0.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
   public static final double[] matirxRoatNorthTOsouthD_ZP = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 1.0, 0.0};
   public static final double[] matirxRoatSouthTOnorthD_ZN = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
   public static final double[][] side_as_top_rotate_matrixD;
   public static final float[] matirxRoatUpToDownF_YN;
   public static final float[] matirxRoatDownTOupF_YP;
   public static final float[] matirxRoatEastTOwestF_XN;
   public static final float[] matirxRoatWestTOeastF_XP;
   public static final float[] matirxRoatNorthTOsouthF_ZP;
   public static final float[] matirxRoatSouthTOnorthF_ZN;
   public static final float[][] side_as_top_rotate_matrixF;
   private static final int[] turnDown;
   private static final int[] turnUP;
   private static final int[] noTurning;
   private static final int[] turnBackwd;
   private static final int[] turnLeft;
   private static final int[] turnRight;
   private static final int[][] roat3tEachSide;
   private static final int[][] roatEachSidet3;
   private static int[][] turnfaceWithCore;
   private static int[][] turnfacetozpositive;
   public static int[][] faceConvertOnflipWE;
   public static int[][] faceConvertOnflipSN;
   public static int[][] faceConvertOnflipUD;
   private static int[][][] sideOnRotatedSideAndFace;
   public static final float[] roatYawEachSide;
   public static final float[] entityYawEachSide;
   public static final float[] entityYawEachSideNagateZ;
   private static final int[] EXTENDER_SIDEtoAXIS;
   public static double[][] DOUBLE_SIDE_HALF_OFFSET;
   public static final double[] matirxRoat3to2;
   public static final double[] matirxRoat3to4;
   public static final double[] matirxRoat3to5;
   public static final double[] matirxRoat0to2;
   public static final double[] matirxRoat0to3;
   public static final double[] matirxRoat0to4;
   public static final double[] matirxRoat0to5;

   public static final int[] rotateFacingToZFront(int facing, int[] retVal, int argX, int argY, int argZ) {
      retVal[0] = argX * roat3tEachSide[facing][0] + argY * roat3tEachSide[facing][3] + argZ * roat3tEachSide[facing][6];
      retVal[1] = argX * roat3tEachSide[facing][1] + argY * roat3tEachSide[facing][4] + argZ * roat3tEachSide[facing][7];
      retVal[2] = argX * roat3tEachSide[facing][2] + argY * roat3tEachSide[facing][5] + argZ * roat3tEachSide[facing][8];
      return retVal;
   }

   public static final int[] rotateZFrontToFacing(int facing, int[] retVal, int argX, int argY, int argZ) {
      retVal[0] = argX * roatEachSidet3[facing][0] + argY * roatEachSidet3[facing][3] + argZ * roatEachSidet3[facing][6];
      retVal[1] = argX * roatEachSidet3[facing][1] + argY * roatEachSidet3[facing][4] + argZ * roatEachSidet3[facing][7];
      retVal[2] = argX * roatEachSidet3[facing][2] + argY * roatEachSidet3[facing][5] + argZ * roatEachSidet3[facing][8];
      return retVal;
   }

   public static short rotateSideOrFacingBasedOnBaseFacing(int baseFacing, int facing) {
      return rotateSideOrFacingBasedOnBaseFacing(baseFacing, facing, 0);
   }

   public static short rotateSideOrFacingBasedOnBaseFacing(int baseFacing, int facing, int connectSide) {
      if (baseFacing >= 0 && baseFacing <= 5) {
         if (facing >= 0 && facing <= 5) {
            return connectSide == 1 ? (short)turnfacetozpositive[baseFacing][facing] : (short)turnfaceWithCore[baseFacing][facing];
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static short convertSideFacingFromFaceBasedToZpositive(int baseFacing, int facing) {
      return convertSideFacingFromFaceBasedToZpositive(baseFacing, facing, 0);
   }

   public static short convertSideFacingFromFaceBasedToZpositive(int baseFacing, int facing, int connectSide) {
      if (baseFacing >= 0 && baseFacing <= 5) {
         if (facing >= 0 && facing <= 5) {
            return connectSide == 1 ? (short)turnfaceWithCore[baseFacing][facing] : (short)turnfacetozpositive[baseFacing][facing];
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static int convertFacingOnFlip(int[][] flipTable, int baseRenderSide, int facing) {
      if (baseRenderSide >= 0 && baseRenderSide <= 5) {
         return facing >= 0 && facing <= 5 ? flipTable[baseRenderSide][facing] : 0;
      } else {
         return 0;
      }
   }

   public static short getSideWhenRotatedSideAndFace(int rotatedSide, int face, int side) {
      return (short)sideOnRotatedSideAndFace[rotatedSide][face - 2][side];
   }

   public static int rotateFace(int face, boolean clockwise) {
      if (clockwise) {
         switch (face) {
            case 2:
               return 5;
            case 3:
               return 4;
            case 4:
               return 2;
            case 5:
               return 3;
            default:
               return 3;
         }
      } else {
         switch (face) {
            case 2:
               return 4;
            case 3:
               return 5;
            case 4:
               return 3;
            case 5:
               return 2;
            default:
               return 3;
         }
      }
   }

   public static int rotateSide(int side) {
      switch (side) {
         case 0:
            return 1;
         case 1:
            return 2;
         case 2:
            return 3;
         case 3:
            return 4;
         case 4:
            return 5;
         case 5:
            return 0;
         default:
            return 3;
      }
   }

   public static final int getExtenderAxis(int rotSide) {
      return rotSide >= 0 && rotSide <= 5 ? EXTENDER_SIDEtoAXIS[rotSide] : 1;
   }

   public static TileEntityJoint getSideNeighbourTe(Level world, int x, int y, int z, int side) {
      int neighbourX = x + side_matrix[side][0];
      int neighbourY = y + side_matrix[side][1];
      int neighbourZ = z + side_matrix[side][2];
      return BlockJointBase.getTileEntityJointerBase(world, neighbourX, neighbourY, neighbourZ);
   }

   public static final Vec3 rotateVec(Vec3 vec3, double centerX, double centerY, double centerZ, double[] rotMatrix) {
      double x = vec3.x - centerX;
      double y = vec3.y - centerY;
      double z = vec3.z - centerZ;
      vec3.x = x * rotMatrix[0] + y * rotMatrix[3] + z * rotMatrix[6] + centerX;
      vec3.y = x * rotMatrix[1] + y * rotMatrix[4] + z * rotMatrix[7] + centerY;
      vec3.z = x * rotMatrix[2] + y * rotMatrix[5] + z * rotMatrix[8] + centerZ;
      return vec3;
   }

   public static final AABB rotateAABBAt(AABB aabb, double roatCenterX, double roatCenterY, double roatCenterZ, double[] rotMatrix) {
      double aabbminX = aabb.minX - roatCenterX;
      double aabbminY = aabb.minY - roatCenterY;
      double aabbminZ = aabb.minZ - roatCenterZ;
      double aabbmaxX = aabb.maxX - roatCenterX;
      double aabbmaxY = aabb.maxY - roatCenterY;
      double aabbmaxZ = aabb.maxZ - roatCenterZ;
      double x1 = aabbminX * rotMatrix[0] + aabbminY * rotMatrix[3] + aabbminZ * rotMatrix[6] + roatCenterX;
      double y1 = aabbminX * rotMatrix[1] + aabbminY * rotMatrix[4] + aabbminZ * rotMatrix[7] + roatCenterY;
      double z1 = aabbminX * rotMatrix[2] + aabbminY * rotMatrix[5] + aabbminZ * rotMatrix[8] + roatCenterZ;
      double x2 = aabbmaxX * rotMatrix[0] + aabbmaxY * rotMatrix[3] + aabbmaxZ * rotMatrix[6] + roatCenterX;
      double y2 = aabbmaxX * rotMatrix[1] + aabbmaxY * rotMatrix[4] + aabbmaxZ * rotMatrix[7] + roatCenterY;
      double z2 = aabbmaxX * rotMatrix[2] + aabbmaxY * rotMatrix[5] + aabbmaxZ * rotMatrix[8] + roatCenterZ;
      aabb.minX = Math.min(x1, x2);
      aabb.minY = Math.min(y1, y2);
      aabb.minZ = Math.min(z1, z2);
      aabb.maxX = Math.max(x1, x2);
      aabb.maxY = Math.max(y1, y2);
      aabb.maxZ = Math.max(z1, z2);
      return aabb;
   }

   public static void fixEntityPrevYaw(Entity target) {
      while(target.getYRot() - target.yRotO < -180.0F) {
         target.yRotO -= 360.0F;
      }

      while(target.getYRot() - target.yRotO >= 180.0F) {
         target.yRotO += 360.0F;
      }

   }

   public static void fixEntityLivingPrevYaw(LivingEntity target) {
      while(target.yBodyRot - target.yBodyRotO < -180.0F) {
         target.yBodyRotO -= 360.0F;
      }

      while(target.yBodyRot - target.yBodyRotO >= 180.0F) {
         target.yBodyRotO += 360.0F;
      }

      while(target.yHeadRot - target.yHeadRotO < -180.0F) {
         target.yHeadRotO -= 360.0F;
      }

      while(target.yHeadRot - target.yHeadRotO >= 180.0F) {
         target.yHeadRotO += 360.0F;
      }

   }

   public static int getPlayerLookingSide(Player player) {
      if (player.getXRot() > 45.0F) {
         return 0;
      } else if (player.getXRot() < -45.0F) {
         return 1;
      } else {
         int i1 = ((Mth.floor((double)(player.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
         int facing = 3;
         if (i1 == 0) {
            facing = 2;
         }

         if (i1 == 1) {
            facing = 5;
         }

         if (i1 == 2) {
            facing = 3;
         }

         if (i1 == 3) {
            facing = 4;
         }

         return facing;
      }
   }

   public static int getPlayerFacingOpposite(Player player) {
      int i1 = ((Mth.floor((double)(player.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
      int facing = 3;
      if (i1 == 1) {
         facing = 4;
      }

      if (i1 == 2) {
         facing = 2;
      }

      if (i1 == 3) {
         facing = 5;
      }

      return facing;
   }

   public static int getFacingOppositeSideFromEntity(Entity entity) {
      int facingSide = 3;
      if (entity != null) {
         int i1 = ((Mth.floor((double)(entity.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
         if (i1 == 0) {
            facingSide = 3;
         }

         if (i1 == 1) {
            facingSide = 4;
         }

         if (i1 == 2) {
            facingSide = 2;
         }

         if (i1 == 3) {
            facingSide = 5;
         }
      }

      return facingSide;
   }

   static {
      side_as_top_rotate_matrixD = new double[][]{matirxRoatDownTOupD_YP, matirxRoatUpToDownD_YN, matirxRoatNorthTOsouthD_ZP, matirxRoatSouthTOnorthD_ZN, matirxRoatWestTOeastD_XP, matirxRoatEastTOwestD_XN};
      matirxRoatUpToDownF_YN = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F};
      matirxRoatDownTOupF_YP = new float[]{1.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -1.0F};
      matirxRoatEastTOwestF_XN = new float[]{0.0F, -1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
      matirxRoatWestTOeastF_XP = new float[]{0.0F, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
      matirxRoatNorthTOsouthF_ZP = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 1.0F, 0.0F};
      matirxRoatSouthTOnorthF_ZN = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, -1.0F, 0.0F};
      side_as_top_rotate_matrixF = new float[][]{matirxRoatDownTOupF_YP, matirxRoatUpToDownF_YN, matirxRoatNorthTOsouthF_ZP, matirxRoatSouthTOnorthF_ZN, matirxRoatWestTOeastF_XP, matirxRoatEastTOwestF_XN};
      turnDown = new int[]{1, 0, 0, 0, 0, 1, 0, -1, 0};
      turnUP = new int[]{1, 0, 0, 0, 0, -1, 0, 1, 0};
      noTurning = new int[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
      turnBackwd = new int[]{-1, 0, 0, 0, 1, 0, 0, 0, -1};
      turnLeft = new int[]{0, 0, -1, 0, 1, 0, 1, 0, 0};
      turnRight = new int[]{0, 0, 1, 0, 1, 0, -1, 0, 0};
      roat3tEachSide = new int[][]{turnUP, turnDown, turnBackwd, noTurning, turnLeft, turnRight};
      roatEachSidet3 = new int[][]{turnDown, turnUP, turnBackwd, noTurning, turnRight, turnLeft};
      turnfaceWithCore = new int[][]{{3, 2, 0, 1, 4, 5}, {2, 3, 1, 0, 4, 5}, {0, 1, 3, 2, 5, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 4, 5, 3, 2}, {0, 1, 5, 4, 2, 3}};
      turnfacetozpositive = new int[][]{{2, 3, 1, 0, 4, 5}, {3, 2, 0, 1, 4, 5}, {0, 1, 3, 2, 5, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 5, 4, 2, 3}, {0, 1, 4, 5, 3, 2}};
      faceConvertOnflipWE = new int[][]{{0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}};
      faceConvertOnflipSN = new int[][]{{0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}};
      faceConvertOnflipUD = new int[][]{{1, 0, 3, 2, 4, 5}, {1, 0, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 4, 5}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 3, 5, 4}};
      sideOnRotatedSideAndFace = new int[][][]{{{0, 1, 3, 2, 5, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 5, 4, 2, 3}, {0, 1, 4, 5, 3, 2}}, {{1, 0, 2, 3, 5, 4}, {1, 0, 3, 2, 4, 5}, {1, 0, 5, 4, 3, 2}, {1, 0, 4, 5, 2, 3}}, {{2, 3, 0, 1, 5, 4}, {2, 3, 1, 0, 4, 5}, {2, 3, 5, 4, 1, 0}, {2, 3, 4, 5, 0, 1}}, {{3, 2, 1, 0, 5, 4}, {3, 2, 0, 1, 4, 5}, {3, 2, 5, 4, 0, 1}, {3, 2, 4, 5, 1, 0}}, {{4, 5, 3, 2, 0, 1}, {4, 5, 2, 3, 1, 0}, {4, 5, 0, 1, 2, 3}, {4, 5, 1, 0, 3, 2}}, {{5, 4, 3, 2, 1, 0}, {5, 4, 2, 3, 0, 1}, {5, 4, 1, 0, 2, 3}, {5, 4, 0, 1, 3, 2}}};
      roatYawEachSide = new float[]{0.0F, 0.0F, 180.0F, 0.0F, -90.0F, 90.0F};
      entityYawEachSide = new float[]{0.0F, 0.0F, 180.0F, 0.0F, 90.0F, -90.0F};
      entityYawEachSideNagateZ = new float[]{0.0F, 0.0F, 0.0F, 180.0F, 90.0F, -90.0F};
      EXTENDER_SIDEtoAXIS = new int[]{1, 1, 2, 2, 0, 0};
      DOUBLE_SIDE_HALF_OFFSET = new double[][]{{0.0, -0.5, 0.0}, {0.0, 0.5, 0.0}, {0.0, 0.0, -0.5}, {0.0, 0.0, 0.5}, {-0.5, 0.0, 0.0}, {0.5, 0.0, 0.0}};
      matirxRoat3to2 = new double[]{-1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, -1.0};
      matirxRoat3to4 = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 0.0};
      matirxRoat3to5 = new double[]{0.0, 0.0, -1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0};
      matirxRoat0to2 = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
      matirxRoat0to3 = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 1.0, 0.0};
      matirxRoat0to4 = new double[]{0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
      matirxRoat0to5 = new double[]{0.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
   }
}
