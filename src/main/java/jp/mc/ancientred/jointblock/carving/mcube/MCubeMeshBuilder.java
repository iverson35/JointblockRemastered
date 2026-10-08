package jp.mc.ancientred.jointblock.carving.mcube;

import java.util.Arrays;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class MCubeMeshBuilder {
   public static final MCubeMeshBuilder instance = new MCubeMeshBuilder();
   public static final short DENSITY_THRESHOLD = 16;
   public static final float DENSITY_THRESHOLDF = 16.0F;
   public static final int VERTICES_BUF_COUNT = 2058;
   public static int[][] dimenLens;
   public static JBMCubeTable.VertEdgeCache[][][] vCacheDim;
   public static JBMCubeTable.VertEdgeCache[][][] vCacheRefByEdgeIndexDim;
   private static int[][][][][][] gatherTableDim;
   private static TileEntityJoint[] teAround;
   private static int[] teAroundStack;
   private static TileEntityJoint[] teAroundEmpty;
   private static JointParts[] jointAround;
   private static int[] jointAroundStack;
   private static JointParts[] jointAroundEmpty;
   private static int[][] idx2XYZ;
   private static int TMB_TOP = 0;
   private static int TMB_MID = 1;
   private static int TMB_BOT = 2;
   private static final int NCELL_UNDRAW = 0;
   private static final int NCELL_X = 1;
   private static final int NCELL_Z = 2;
   private static final int NCELL_SRC_C1 = 3;
   private static final int NCELL_SRC_C2 = 4;
   private static final int NCELL_DST_C1 = 5;
   private static final int NCELL_DST_C2 = 6;
   private static final int NCELL_Y = 7;
   private static final int EX1_NCELL_LEN = 7;
   private static final int EX2_NCELL_LEN = 8;
   private static boolean[][][] CORNERS_BY_SIDE = new boolean[][][]{{{false, false}, {true, true}, {true, false}, {false, true}}, {{false, true}, {true, false}, {false, false}, {true, true}}, {{true, false}, {false, true}, {true, true}, {false, false}}, {{true, true}, {false, false}, {false, true}, {true, false}}};
   private static int[][] CORNER_NEIGHBOUR = new int[][]{{1, -1, -1, 3, 0, 0}, {0, -1, 1, 2, 0, 1}, {0, 1, -1, 1, 0, 2}, {0, 1, 1, 0, 0, 3}};
   private static int[][] SIDE_NEIGHBOUR = new int[][]{{0, 0, 0, 0, 3, 0, 3, -1}, {0, 0, 0, 0, 3, 0, 3, 1}, {1, 0, -1, 1, 3, 0, 2}, {0, 0, 1, 2, 0, 3, 1}, {1, -1, 0, 3, 2, 1, 0}, {0, 1, 0, 0, 1, 2, 3}};
   private static int[][] NEIGHBOURS;
   public int addedvertexcount;
   public int addedindexcount;
   public int vCacheGeneration;
   public MeshVertex[] vertices = new MeshVertex[2058];
   public int[] indices = new int[2058];
   public float[][] normals = new float[2058][];

   public static void setUpHelper() {
      dimenLens = new int[6][];
      vCacheDim = new JBMCubeTable.VertEdgeCache[6][][];
      vCacheRefByEdgeIndexDim = new JBMCubeTable.VertEdgeCache[6][][];

      for(int dim = 2; dim <= 5; ++dim) {
         int len = dim + 2;
         int size = len * len * len;
         dimenLens[dim] = new int[3];
         vCacheDim[dim] = new JBMCubeTable.VertEdgeCache[size][];
         vCacheRefByEdgeIndexDim[dim] = new JBMCubeTable.VertEdgeCache[size][];
         int xDimen = dimenLens[dim][0] = 1;
         int zDimen = dimenLens[dim][2] = len;
         int yDimen = dimenLens[dim][1] = len * len;

         int y;
         int x;
         int z;
         JBMCubeTable.VertEdgeCache[] vECaches;
         for(y = 0; y < len; ++y) {
            for(x = 0; x < len; ++x) {
               for(z = 0; z < len; ++z) {
                  vECaches = new JBMCubeTable.VertEdgeCache[]{new JBMCubeTable.VertEdgeCache(), new JBMCubeTable.VertEdgeCache(), new JBMCubeTable.VertEdgeCache()};
                  vCacheDim[dim][x * xDimen + y * yDimen + z * zDimen] = vECaches;
               }
            }
         }

         for(y = 0; y <= dim; ++y) {
            for(x = 0; x <= dim; ++x) {
               for(z = 0; z <= dim; ++z) {
                  vECaches = new JBMCubeTable.VertEdgeCache[12];

                  for(int edge = 0; edge < 12; ++edge) {
                     int nx = x + JBMCubeTable.EDGE_REF[edge][0];
                     int ny = y + JBMCubeTable.EDGE_REF[edge][1];
                     int nz = z + JBMCubeTable.EDGE_REF[edge][2];
                     int rIndex = JBMCubeTable.EDGE_REF[edge][3];
                     vECaches[edge] = vCacheDim[dim][nx * xDimen + ny * yDimen + nz * zDimen][rIndex];
                  }

                  vCacheRefByEdgeIndexDim[dim][x * xDimen + y * yDimen + z * zDimen] = vECaches;
               }
            }
         }
      }

      setUpGatherHelperTable();
   }

   private static void setUpGatherHelperTable() {
      int[] workArray = new int[50];
      gatherTableDim = new int[6][][][][][];

      int dim;
      int tmb;
      int x;
      for(dim = 2; dim <= 5; ++dim) {
         int[][][][][] gatherTableTmb = gatherTableDim[dim] = new int[3][][][][];
         tmb = 0;

         for(x = 1; tmb < 3; ++tmb) {
            int[][][][] gatherTableNeighb = gatherTableTmb[tmb] = new int[NEIGHBOURS.length][][][];

            for(int i = 0; i < NEIGHBOURS.length; ++i) {
               int[] nCell = NEIGHBOURS[i];
               if (nCell.length != 8 || nCell[7] == x) {
                  int[][][] gatherTableSrcF = gatherTableNeighb[i] = new int[4][][];

                  for(int srcFacing = 0; srcFacing < 4; ++srcFacing) {
                     int[][] gatherTableDstF = gatherTableSrcF[srcFacing] = new int[4][];

                     for(int dstFacing = 0; dstFacing < 4; ++dstFacing) {
                        int arrayLen = createGatherTable(workArray, nCell, dim, srcFacing, dstFacing, tmb);
                        gatherTableDstF[dstFacing] = Arrays.copyOf(workArray, arrayLen);
                     }
                  }
               }
            }

            --x;
         }
      }

      teAround = new TileEntityJoint[27];
      teAroundEmpty = new TileEntityJoint[teAround.length];
      teAroundStack = new int[teAround.length];
      jointAround = new JointParts[27];
      jointAroundEmpty = new JointParts[jointAround.length];
      jointAroundStack = new int[jointAround.length];
      idx2XYZ = new int[jointAround.length][];
      dim = 0;

      for(int z = 0; z < 3; ++z) {
         for(tmb = 0; tmb < 3; ++tmb) {
            for(x = 0; x < 3; ++x) {
               int[] xyz = new int[]{x, tmb, z};
               idx2XYZ[dim++] = xyz;
            }
         }
      }

   }

   private static int createGatherTable(int[] workArray, int[] nCell, int dim, int srcFacing, int dstFacing, int tmb) {
      int usedArrayCount = 0;
      srcFacing += 2;
      dstFacing += 2;
      int xDstDimen = dimenLens[dim][0];
      int yDstDimen = dimenLens[dim][1];
      int zDstDimen = dimenLens[dim][2];
      int xSrcDimen = 1;
      int ySrcDimen = dim * dim;
      int zSrcDimen = dim;
      int srcDimEnd = dim - 1;
      int destDimEnd = dim + 2 - 1;
      int srcXa;
      int srcZa;
      int srcXb;
      int srcZb;
      int srcXDir;
      if (nCell.length >= 7) {
         srcXa = CORNERS_BY_SIDE[nCell[3]][srcFacing - 2][0] ? 0 : srcDimEnd;
         srcZa = CORNERS_BY_SIDE[nCell[3]][srcFacing - 2][1] ? 0 : srcDimEnd;
         srcXb = CORNERS_BY_SIDE[nCell[4]][srcFacing - 2][0] ? 0 : srcDimEnd;
         srcZb = CORNERS_BY_SIDE[nCell[4]][srcFacing - 2][1] ? 0 : srcDimEnd;
         srcXDir = srcXa > srcXb ? -1 : 1;
         int srcZDir = srcZa > srcZb ? -1 : 1;
         boolean isDoubleMove = true;
         boolean da = CORNERS_BY_SIDE[nCell[5]][dstFacing - 2][0];
         boolean db = CORNERS_BY_SIDE[nCell[6]][dstFacing - 2][0];
         boolean var10000;
         int dstXa;
         int dstXDir;
         int var39;
         if (da != db) {
            dstXa = da ? 1 : destDimEnd - 1;
            if (db) {
               var10000 = true;
            } else {
               var39 = destDimEnd - 1;
            }

            dstXDir = da ? 1 : -1;
         } else {
            dstXa = da ? 0 : destDimEnd;
            var39 = db ? 0 : destDimEnd;
            dstXDir = 0;
            isDoubleMove = false;
         }

         da = CORNERS_BY_SIDE[nCell[5]][dstFacing - 2][1];
         db = CORNERS_BY_SIDE[nCell[6]][dstFacing - 2][1];
         int dstZa;
         int dstZDir;
         if (da != db) {
            dstZa = da ? 1 : destDimEnd - 1;
            if (db) {
               var10000 = true;
            } else {
               var39 = destDimEnd - 1;
            }

            dstZDir = da ? 1 : -1;
         } else {
            dstZa = da ? 0 : destDimEnd;
            var39 = db ? 0 : destDimEnd;
            dstZDir = 0;
            isDoubleMove = false;
         }

         boolean dstXFirst = false;
         boolean dstZFirst = false;
         if (isDoubleMove) {
            boolean isSrcGroup23 = srcFacing == 2 || srcFacing == 3;
            boolean isDstGroup23 = dstFacing == 2 || dstFacing == 3;
            dstXFirst = !(dstZFirst = isSrcGroup23 == isDstGroup23);
         }

         int dx = dstXa;
         int dz = dstZa;
         int x = srcXa;

         while(true) {
            int z = srcZa;

            while(true) {
               if (tmb == TMB_TOP) {
                  workArray[usedArrayCount++] = x * xSrcDimen + 0 * ySrcDimen + z * zSrcDimen;
                  workArray[usedArrayCount++] = dx * xDstDimen + destDimEnd * yDstDimen + dz * zDstDimen;
               } else {
                  int y;
                  if (tmb == TMB_BOT) {
                     y = workArray[usedArrayCount++] = x * xSrcDimen + srcDimEnd * ySrcDimen + z * zSrcDimen;
                     workArray[usedArrayCount++] = dx * xDstDimen + 0 * yDstDimen + dz * zDstDimen;
                  } else {
                     for(y = 0; y < dim; ++y) {
                        workArray[usedArrayCount++] = x * xSrcDimen + y * ySrcDimen + z * zSrcDimen;
                        workArray[usedArrayCount++] = dx * xDstDimen + (y + 1) * yDstDimen + dz * zDstDimen;
                     }
                  }
               }

               if (!dstZFirst) {
                  dx += dstXDir;
               }

               if (!dstXFirst) {
                  dz += dstZDir;
               }

               if (z == srcZb) {
                  if (dstZFirst) {
                     dx += dstXDir;
                     dz = dstZa;
                  }

                  if (dstXFirst) {
                     dx = dstXa;
                     dz += dstZDir;
                  }

                  if (x == srcXb) {
                     return usedArrayCount;
                  }

                  x += srcXDir;
                  break;
               }

               z += srcZDir;
            }
         }
      } else {
         srcXa = CORNERS_BY_SIDE[nCell[3]][srcFacing - 2][0] ? 0 : srcDimEnd;
         srcZa = CORNERS_BY_SIDE[nCell[3]][srcFacing - 2][1] ? 0 : srcDimEnd;
         srcXb = CORNERS_BY_SIDE[nCell[5]][dstFacing - 2][0] ? 0 : destDimEnd;
         srcZb = CORNERS_BY_SIDE[nCell[5]][dstFacing - 2][1] ? 0 : destDimEnd;
         if (tmb == TMB_TOP) {
            workArray[usedArrayCount++] = srcXa * xSrcDimen + 0 * ySrcDimen + srcZa * dim;
            workArray[usedArrayCount++] = srcXb * xDstDimen + destDimEnd * yDstDimen + srcZb * zDstDimen;
         } else if (tmb == TMB_BOT) {
            workArray[usedArrayCount++] = srcXa * xSrcDimen + srcDimEnd * ySrcDimen + srcZa * dim;
            workArray[usedArrayCount++] = srcXb * xDstDimen + 0 * yDstDimen + srcZb * zDstDimen;
         } else {
            for(srcXDir = 0; srcXDir < dim; ++srcXDir) {
               workArray[usedArrayCount++] = srcXa * xSrcDimen + srcXDir * ySrcDimen + srcZa * zSrcDimen;
               workArray[usedArrayCount++] = srcXb * xDstDimen + (srcXDir + 1) * yDstDimen + srcZb * zDstDimen;
            }
         }
      }

      return usedArrayCount;
   }

   public static MCubeGathered gatherMCCarvingDataTileEntity(MCubeGathered mcGatheredData, TileEntityJoint te) {
      Level level = te.getLevel();
      int dim = JBCarvingDataUtil.getCarvingDivide(te.carvingData);
      int pltIndex = JBCarvingDataUtil.getPaletteIndex(te.carvingData);
      if (level != null && dim <= 5) {
         if (mcGatheredData == null || mcGatheredData.dim != dim) {
            mcGatheredData = new MCubeGathered(dim);
         }

         byte[] densityMatrix = mcGatheredData.gatheredDensity;
         copyToGatherFromMain(densityMatrix, te.carvingData, dim, dimenLens[dim][0], dimenLens[dim][1], dimenLens[dim][2]);
         int dstFacing = te.getJointFacing();
         if (dstFacing <= 1) {
            return mcGatheredData;
         } else {
            int tmb;
            int yMod;
            int i;
            if (te.jointType == 4) {
               System.arraycopy(teAroundEmpty, 0, teAround, 0, teAround.length);
               int stackCnt = gatherNeighbourTe(level, te, dim, pltIndex, 0, 1, 1, 1);
               tmb = 0;

               for(yMod = 0; yMod < 3; ++yMod) {
                  for(int len = stackCnt; tmb < len; ++tmb) {
                     i = teAroundStack[tmb];
                     TileEntityJoint base = teAround[i];
                     stackCnt = gatherNeighbourTe(level, base, dim, pltIndex, stackCnt, idx2XYZ[i][0], idx2XYZ[i][1], idx2XYZ[i][2]);
                  }
               }
            }

            tmb = 0;

            for(yMod = 1; tmb < 3; ++tmb) {
               boolean yUnDraw = yMod == 1;

               for(i = 0; i < NEIGHBOURS.length; ++i) {
                  int[] nCell = NEIGHBOURS[i];
                  if (nCell.length != 8 || nCell[7] == yMod) {
                     int relX = nCell[1] + 1;
                     int relY = yMod + 1;
                     int relZ = nCell[2] + 1;
                     int idx = relX + relY * 3 + relZ * 9;
                     TileEntityJoint teNeighbour = teAround[idx];
                     teAround[idx] = null;
                     int srcFacing;
                     if (teNeighbour != null && (srcFacing = teNeighbour.getJointFacing()) > 1) {
                        int[] neighbourSrcData = teNeighbour.carvingData;
                        int[] gatherTableFor = gatherTableDim[dim][tmb][i][srcFacing - 2][dstFacing - 2];
                        int g = 0;

                        for(int len = gatherTableFor.length; g < len; g += 2) {
                           int srcCnt = gatherTableFor[g + 0];
                           int dstDataIndex = gatherTableFor[g + 1];
                           densityMatrix[dstDataIndex] = (byte)(neighbourSrcData[srcCnt >> 2] >> ((srcCnt & 3) << 3) & 31);
                        }
                     }
                  }
               }

               --yMod;
            }

            return mcGatheredData;
         }
      } else {
         throw new RuntimeException("Something's wrong!");
      }
   }

   private static int gatherNeighbourTe(Level level, TileEntityJoint base, int dim, int palleteIndex, int stackCnt, int baseX, int baseY, int baseZ) {
      for(int side = 0; side < 6; ++side) {
         int nX = baseX + EnumDirectionOld.VALID_DIRECTIONS[side].offsetX;
         if (nX >= 0 && nX <= 2) {
            int nY = baseY + EnumDirectionOld.VALID_DIRECTIONS[side].offsetY;
            if (nY >= 0 && nY <= 2) {
               int nZ = baseZ + EnumDirectionOld.VALID_DIRECTIONS[side].offsetZ;
               if (nZ >= 0 && nZ <= 2) {
                  int idx = nX + nY * 3 + nZ * 9;
                  if (idx != 13 && teAround[idx] == null) {
                     TileEntityJoint teNeighbour = tryGetSide(level, base, side);
                     if (teNeighbour != null && teNeighbour.isValidCarveable() && teNeighbour.jointType == 4 && JBCarvingDataUtil.getCarvingType(teNeighbour.carvingData) == 4 && JBCarvingDataUtil.getPaletteIndex(teNeighbour.carvingData) == palleteIndex && JBCarvingDataUtil.getCarvingDivide(teNeighbour.carvingData) == dim) {
                        teAround[idx] = teNeighbour;
                        teAroundStack[stackCnt++] = idx;
                     }
                  }
               }
            }
         }
      }

      return stackCnt;
   }

   private static TileEntityJoint tryGetSide(Level level, TileEntityJoint te, int side) {
      if (te.jointType != 0 && te.connectSide == side) {
         return te.getConnectedSideTileEntityJointed();
      } else {
         BlockPos pos = te.getBlockPos();
         int sideX = pos.getX() + JBRotationUtil.side_matrix[side][0];
         int sideY = pos.getY() + JBRotationUtil.side_matrix[side][1];
         int sideZ = pos.getZ() + JBRotationUtil.side_matrix[side][2];
         TileEntityJoint teNeighb = BlockJointBase.getTileEntityJointerBase(level, sideX, sideY, sideZ);
         return teNeighb != null && teNeighb.connectSide == EnumDirectionOld.OPPOSITES[side] ? teNeighb : null;
      }
   }

   public static MCubeGathered gatherMCCarvingDataJointParts(MCubeGathered mcGatheredData, JointParts joint) {
      int dim = JBCarvingDataUtil.getCarvingDivide(joint.carvingData);
      int pltIndex = JBCarvingDataUtil.getPaletteIndex(joint.carvingData);
      if (dim > 5) {
         throw new RuntimeException("Something's wrong!");
      } else {
         if (mcGatheredData == null || mcGatheredData.dim != dim) {
            mcGatheredData = new MCubeGathered(dim);
         }

         byte[] densityMatrix = mcGatheredData.gatheredDensity;
         copyToGatherFromMain(densityMatrix, joint.carvingData, dim, dimenLens[dim][0], dimenLens[dim][1], dimenLens[dim][2]);
         int dstFacing = joint.facing;
         if (dstFacing <= 1) {
            return mcGatheredData;
         } else {
            int tmb;
            int yMod;
            int i;
            if (joint.jointType == 4) {
               System.arraycopy(jointAroundEmpty, 0, jointAround, 0, jointAround.length);
               tmb = gatherNeighbourJoint(joint, dim, pltIndex, 0, 1, 1, 1);
               yMod = 0;

               for(int searchCnt = 0; searchCnt < 3; ++searchCnt) {
                  for(i = tmb; yMod < i; ++yMod) {
                     int idx = jointAroundStack[yMod];
                     JointParts base = jointAround[idx];
                     tmb = gatherNeighbourJoint(base, dim, pltIndex, tmb, idx2XYZ[idx][0], idx2XYZ[idx][1], idx2XYZ[idx][2]);
                  }
               }
            }

            tmb = 0;

            for(yMod = 1; tmb < 3; ++tmb) {
               boolean yUnDraw = yMod == 1;

               for(i = 0; i < NEIGHBOURS.length; ++i) {
                  int[] nCell = NEIGHBOURS[i];
                  if (nCell.length != 8 || nCell[7] == yMod) {
                     int relX = nCell[1] + 1;
                     int relY = yMod + 1;
                     int relZ = nCell[2] + 1;
                     int idx = relX + relY * 3 + relZ * 9;
                     JointParts jointNeighbour = jointAround[idx];
                     jointAround[idx] = null;
                     byte srcFacing;
                     if (jointNeighbour != null && (srcFacing = jointNeighbour.facing) > 1) {
                        int[] neighbourSrcData = jointNeighbour.carvingData;
                        int[] gatherTableFor = gatherTableDim[dim][tmb][i][srcFacing - 2][dstFacing - 2];
                        int g = 0;

                        for(int len = gatherTableFor.length; g < len; g += 2) {
                           int srcCnt = gatherTableFor[g + 0];
                           int dstDataIndex = gatherTableFor[g + 1];
                           densityMatrix[dstDataIndex] = (byte)(neighbourSrcData[srcCnt >> 2] >> ((srcCnt & 3) << 3) & 31);
                        }
                     }
                  }
               }

               --yMod;
            }

            return mcGatheredData;
         }
      }
   }

   private static int gatherNeighbourJoint(JointParts base, int dim, int palleteIndex, int stackCnt, int baseX, int baseY, int baseZ) {
      for(int side = 0; side < 6; ++side) {
         int nX = baseX + EnumDirectionOld.VALID_DIRECTIONS[side].offsetX;
         if (nX >= 0 && nX <= 2) {
            int nY = baseY + EnumDirectionOld.VALID_DIRECTIONS[side].offsetY;
            if (nY >= 0 && nY <= 2) {
               int nZ = baseZ + EnumDirectionOld.VALID_DIRECTIONS[side].offsetZ;
               if (nZ >= 0 && nZ <= 2) {
                  int idx = nX + nY * 3 + nZ * 9;
                  if (idx != 13 && jointAround[idx] == null) {
                     JointParts jointNeighbour = base.tryGetSide(side);
                     if (jointNeighbour != null && jointNeighbour.carvingData != null && jointNeighbour.jointType == 4 && JBCarvingDataUtil.getCarvingType(jointNeighbour.carvingData) == 4 && JBCarvingDataUtil.getPaletteIndex(jointNeighbour.carvingData) == palleteIndex && JBCarvingDataUtil.getCarvingDivide(jointNeighbour.carvingData) == dim) {
                        jointAround[idx] = jointNeighbour;
                        jointAroundStack[stackCnt++] = idx;
                     }
                  }
               }
            }
         }
      }

      return stackCnt;
   }

   private static void copyToGatherFromMain(byte[] gathered, int[] carvingData, int dim, int xDimen, int yDimen, int zDimen) {
      int srcCnt = 0;
      int len = dim + 2 - 1;

      for(int y = 1; y < len; ++y) {
         for(int z = 1; z < len; ++z) {
            for(int x = 1; x < len; ++x) {
               int dstDataIndex = x * xDimen + y * yDimen + z * zDimen;
               gathered[dstDataIndex] = (byte)(carvingData[srcCnt >> 2] >> ((srcCnt & 3) << 3) & 31);
               ++srcCnt;
            }
         }
      }

   }

   private MCubeMeshBuilder() {
      for(int i = 0; i < 2058; ++i) {
         this.vertices[i] = new MeshVertex();
      }

   }

   public void buildMesh(int dim, MCubeGathered mcGatheredData) {
      byte[] densityMatrix = mcGatheredData.gatheredDensity;
      int vIdx = this.addedvertexcount = 0;
      int triangleIdx = this.addedindexcount = 0;
      if (dim >= 2 && dim <= 5) {
         if (densityMatrix != null) {
            int cacheGen = ++this.vCacheGeneration;
            JBMCubeTable.VertEdgeCache[][] var10000 = vCacheDim[dim];
            JBMCubeTable.VertEdgeCache[][] vCacheRefByEdgeIndex = vCacheRefByEdgeIndexDim[dim];
            int xDimen = dimenLens[dim][0];
            int yDimen = dimenLens[dim][1];
            int zDimen = dimenLens[dim][2];
            int type = 0;
            int len = dim + 2 - 1;

            for(int y = 0; y < len; ++y) {
               for(int z = 0; z < len; ++z) {
                  for(int x = 0; x < len; ++x) {
                     int dataIndex = x * xDimen + y * yDimen + z * zDimen;
                     if (x == 0) {
                        type = 0;
                        if (densityMatrix[dataIndex] >= 16) {
                           type |= 8;
                        }

                        if (densityMatrix[dataIndex + zDimen] >= 16) {
                           type |= 4;
                        }

                        if (densityMatrix[dataIndex + yDimen] >= 16) {
                           type |= 2;
                        }

                        if (densityMatrix[dataIndex + yDimen + zDimen] >= 16) {
                           type |= 1;
                        }
                     } else {
                        type >>= 4;
                     }

                     if (densityMatrix[dataIndex + xDimen] >= 16) {
                        type |= 128;
                     }

                     if (densityMatrix[dataIndex + xDimen + zDimen] >= 16) {
                        type |= 64;
                     }

                     if (densityMatrix[dataIndex + xDimen + yDimen] >= 16) {
                        type |= 32;
                     }

                     if (densityMatrix[dataIndex + xDimen + yDimen + zDimen] >= 16) {
                        type |= 16;
                     }

                     if (type != 0) {
                        JBMCubeTable.VertEdgeCache[] vCacheRef = vCacheRefByEdgeIndex[dataIndex];
                        int triNums = JBMCubeTable.typeDataTable[type].trianglesNum;
                        int[][] edgeVertices = JBMCubeTable.typeDataTable[type].edgeVertices;

                        for(int tris = 0; tris < triNums; ++tris) {
                           int[] edgesAryOfFace = edgeVertices[tris];

                           float v1X;
                           float v1Y;
                           float v1Z;
                           float v2X;
                           float v2Y;
                           float v2Z;
                           MeshVertex vertRefB;
                           for(int v = 0; v < 3; ++v) {
                              int edge = edgesAryOfFace[v];
                              if (vCacheRef[edge].generation == cacheGen) {
                                 this.indices[triangleIdx++] = vCacheRef[edge].vertIndex;
                              } else {
                                 vCacheRef[edge].vertIndex = vIdx;
                                 vCacheRef[edge].generation = cacheGen;
                                 JBMCubeTable.EdgeIndices edgeIndicesOfEdge = JBMCubeTable.EDGE_INDICES[edge];
                                 JBMCubeTable.VertIndices vertIndicesOfEdgeV = JBMCubeTable.VERT_INDICES[edgeIndicesOfEdge.e1];
                                 v1X = vertIndicesOfEdgeV.fx;
                                 v1Y = vertIndicesOfEdgeV.fy;
                                 v1Z = vertIndicesOfEdgeV.fz;
                                 int d1Idx = dataIndex + vertIndicesOfEdgeV.x * xDimen + vertIndicesOfEdgeV.y * yDimen + vertIndicesOfEdgeV.z * zDimen;
                                 float d1_lerp = (float)densityMatrix[d1Idx];
                                 vertIndicesOfEdgeV = JBMCubeTable.VERT_INDICES[edgeIndicesOfEdge.e2];
                                 v2X = vertIndicesOfEdgeV.fx;
                                 v2Y = vertIndicesOfEdgeV.fy;
                                 v2Z = vertIndicesOfEdgeV.fz;
                                 int d2Idx = dataIndex + vertIndicesOfEdgeV.x * xDimen + vertIndicesOfEdgeV.y * yDimen + vertIndicesOfEdgeV.z * zDimen;
                                 vertRefB = this.vertices[vIdx];
                                 d1_lerp = (d1_lerp - 16.0F) / (d1_lerp - (float)densityMatrix[d2Idx]);
                                 vertRefB.vertexX = v1X + (v2X - v1X) * d1_lerp + (float)x;
                                 vertRefB.vertexY = v1Y + (v2Y - v1Y) * d1_lerp + (float)y;
                                 vertRefB.vertexZ = v1Z + (v2Z - v1Z) * d1_lerp + (float)z;
                                 this.indices[triangleIdx++] = vIdx++;
                              }
                           }

                           MeshVertex vertRefC = this.vertices[this.indices[triangleIdx - 3]];
                           vertRefB = this.vertices[this.indices[triangleIdx - 2]];
                           MeshVertex vertRefA = this.vertices[this.indices[triangleIdx - 1]];
                           v1X = vertRefA.vertexX - vertRefB.vertexX;
                           v1Y = vertRefA.vertexY - vertRefB.vertexY;
                           v1Z = vertRefA.vertexZ - vertRefB.vertexZ;
                           v2X = vertRefC.vertexX - vertRefB.vertexX;
                           v2Y = vertRefC.vertexY - vertRefB.vertexY;
                           v2Z = vertRefC.vertexZ - vertRefB.vertexZ;
                           float xt = v1Y * v2Z - v1Z * v2Y;
                           float yt = v1Z * v2X - v1X * v2Z;
                           float zt = v1X * v2Y - v1Y * v2X;
                           float mgn = Mth.sqrt(xt * xt + yt * yt + zt * zt);
                           if (mgn == 0.0F) {
                              triangleIdx -= 3;
                           } else {
                              vertRefA.normalX = vertRefB.normalX = vertRefC.normalX = xt / mgn;
                              vertRefA.normalY = vertRefB.normalY = vertRefC.normalY = yt / mgn;
                              vertRefA.normalZ = vertRefB.normalZ = vertRefC.normalZ = zt / mgn;
                              int nIdx = triangleIdx - 3;
                              if (this.normals[nIdx] == null) {
                                 this.normals[nIdx] = new float[3];
                              }

                              this.normals[nIdx][0] = xt / mgn;
                              this.normals[nIdx][1] = yt / mgn;
                              this.normals[nIdx][2] = zt / mgn;
                           }
                        }
                     }
                  }
               }
            }

            this.addedvertexcount = vIdx;
            this.addedindexcount = triangleIdx;
         }
      }
   }

   static {
      NEIGHBOURS = new int[][]{CORNER_NEIGHBOUR[0], CORNER_NEIGHBOUR[1], CORNER_NEIGHBOUR[2], CORNER_NEIGHBOUR[3], SIDE_NEIGHBOUR[0], SIDE_NEIGHBOUR[1], SIDE_NEIGHBOUR[2], SIDE_NEIGHBOUR[3], SIDE_NEIGHBOUR[4], SIDE_NEIGHBOUR[5]};
   }

   public static class MeshVertex {
      public float vertexX;
      public float vertexY;
      public float vertexZ;
      public float normalX;
      public float normalY;
      public float normalZ;
   }
}
