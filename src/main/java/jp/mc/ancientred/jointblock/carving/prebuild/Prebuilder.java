package jp.mc.ancientred.jointblock.carving.prebuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import org.joml.Matrix3f;
import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;
import jp.mc.ancientred.jointblock.assemble.MatrixBase;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeMeshBuilder;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;

public class Prebuilder extends MatrixBase {
   public float b00;
   public float b01;
   public float b02;
   public float b03;
   public float b10;
   public float b11;
   public float b12;
   public float b13;
   public float b20;
   public float b21;
   public float b22;
   public float b23;
   public float b30;
   public float b31;
   public float b32;
   public float b33;
   public Player player;
   public String outputFolderStr;
   private int errorCount = 0;
   private static final int ERROR_EXCEED_CNT = 5;
   public boolean coreBase;
   private boolean triangulateAtLast;
   public byte palletIdxTemp;
   public int colorIdx;
   public int faceNum;
   private int sideIdForDelimPreset = -1;
   public int partsFacing = 3;
   private int partsChunkGroupNow;
   public int[] fillMatchWork = new int[6];
   public int[] vertJointIdxA = new int[200];
   public int[] vertJointIdxB = new int[200];
   private PbCarvingCell cellNow;
   private PbCarvingCell[][] divMatrix = new PbCarvingCell[16][];
   public PbCarvingCell.PbWFObjGroup[] groupListF3 = new PbCarvingCell.PbWFObjGroup[48];
   public PbCarvingCell.PbWFObjGroup[] groupListF4 = new PbCarvingCell.PbWFObjGroup[48];
   public PbCarvingCell.PbWFMaterial[] matList = new PbCarvingCell.PbWFMaterial[48];
   public ArrayList<PbCarvingCell.PbCellVert> vertices = new ArrayList();
   private ArrayList<PbCarvingCell.PbCellVert> mergeFaceVertWork = new ArrayList();
   private ArrayList<PbCarvingCell.PbCellVert> mergeHoleVertWork = new ArrayList();
   private ArrayList<PbCarvingCell.PbCellFace> mergeFaceFaceWork = new ArrayList();
   private ArrayList<PbCarvingCell.PbCellFace> mergeHoleFaceWork = new ArrayList();
   private ArrayList<SplittedEdge> debugEdgeList = new ArrayList();
   private ArrayList<SplittedEdge> splitWorkLeft = new ArrayList();
   private ArrayList<SplittedEdge> splitWorkRight = new ArrayList();
   private ArrayList<SplittedEdge> splitWorkLeftLocal = new ArrayList();
   private ArrayList<SplittedEdge> splitWorkRightLocal = new ArrayList();
   private PriorityQueue<PbCarvingCell.PbCellVertWrapper> turnPointQueueLeft;
   private PriorityQueue<PbCarvingCell.PbCellVertWrapper> turnPointQueueRight;
   private Matrix3f planerMatrix = new Matrix3f();
   private Matrix3f planerMatrixInv = new Matrix3f();
   private PbCarvingCell.PbCellVert intrisA = new PbCarvingCell.PbCellVert(0.0F, 0.0F, 0.0F);
   private PbCarvingCell.PbCellVert intrisB = new PbCarvingCell.PbCellVert(0.0F, 0.0F, 0.0F);
   private PbCarvingCell.PbCellVert intrisC = new PbCarvingCell.PbCellVert(0.0F, 0.0F, 0.0F);
   private static final PbCarvingCell.PbCellEdge EDGE_COLLECTED = new PbCarvingCell.PbCellEdge();
   private static final PbCarvingCell.PbCellVert VECTILT = new PbCarvingCell.PbCellVert(0.0F, 1.0F, 0.0F);
   private static final PbCarvingCell.PbCellVert VECTILTINV = new PbCarvingCell.PbCellVert(0.0F, -1.0F, 0.0F);
   public BlockedPartsMap blockedPartsMap = new BlockedPartsMap();
   public static final float MERGE_DIST = 1.0E-4F;
   public static final float MERGE_DIST_SQ = 9.999999E-9F;
   public static final float MERGE_DIST_3X = (Mth.sqrt(3.0F) + 5.0E-5F) * 1.0E-4F;
   public static final int MERGE_TGT = -1;
   public static final int MERGE_DEL = -2;
   private String resultMessageStr;
   private boolean isResultMessageError;
   private Object[] resultMessageParams = null;

   public Prebuilder(Player argPlayer, String argOutputGroup, boolean triangulate, boolean coreBase) {
      this.player = argPlayer;
      this.outputFolderStr = argOutputGroup;
      if (this.outputFolderStr == null || JBCommonUtil.isStringEmpty(this.outputFolderStr)) {
         this.outputFolderStr = "out";
      }

      this.coreBase = coreBase;
      this.triangulateAtLast = triangulate;
   }

   public void setupBase() {
      this.b00 = this.m00;
      this.b01 = this.m01;
      this.b02 = this.m02;
      this.b03 = this.m03;
      this.b10 = this.m10;
      this.b11 = this.m11;
      this.b12 = this.m12;
      this.b13 = this.m13;
      this.b20 = this.m20;
      this.b21 = this.m21;
      this.b22 = this.m22;
      this.b23 = this.m23;
      this.b30 = this.m30;
      this.b31 = this.m31;
      this.b32 = this.m32;
      this.b33 = this.m33;
   }

   public void mulMatrixFrom(MatrixBase matrix) {
      this.m00 = this.b00 * matrix.m00 + this.b01 * matrix.m10 + this.b02 * matrix.m20 + this.b03 * matrix.m30;
      this.m01 = this.b00 * matrix.m01 + this.b01 * matrix.m11 + this.b02 * matrix.m21 + this.b03 * matrix.m31;
      this.m02 = this.b00 * matrix.m02 + this.b01 * matrix.m12 + this.b02 * matrix.m22 + this.b03 * matrix.m32;
      this.m03 = this.b00 * matrix.m03 + this.b01 * matrix.m13 + this.b02 * matrix.m23 + this.b03 * matrix.m33;
      this.m10 = this.b10 * matrix.m00 + this.b11 * matrix.m10 + this.b12 * matrix.m20 + this.b13 * matrix.m30;
      this.m11 = this.b10 * matrix.m01 + this.b11 * matrix.m11 + this.b12 * matrix.m21 + this.b13 * matrix.m31;
      this.m12 = this.b10 * matrix.m02 + this.b11 * matrix.m12 + this.b12 * matrix.m22 + this.b13 * matrix.m32;
      this.m13 = this.b10 * matrix.m03 + this.b11 * matrix.m13 + this.b12 * matrix.m23 + this.b13 * matrix.m33;
      this.m20 = this.b20 * matrix.m00 + this.b21 * matrix.m10 + this.b22 * matrix.m20 + this.b23 * matrix.m30;
      this.m21 = this.b20 * matrix.m01 + this.b21 * matrix.m11 + this.b22 * matrix.m21 + this.b23 * matrix.m31;
      this.m22 = this.b20 * matrix.m02 + this.b21 * matrix.m12 + this.b22 * matrix.m22 + this.b23 * matrix.m32;
      this.m23 = this.b20 * matrix.m03 + this.b21 * matrix.m13 + this.b22 * matrix.m23 + this.b23 * matrix.m33;
      this.m30 = this.b30 * matrix.m00 + this.b31 * matrix.m10 + this.b32 * matrix.m20 + this.b33 * matrix.m30;
      this.m31 = this.b30 * matrix.m01 + this.b31 * matrix.m11 + this.b32 * matrix.m21 + this.b33 * matrix.m31;
      this.m32 = this.b30 * matrix.m02 + this.b31 * matrix.m12 + this.b32 * matrix.m22 + this.b33 * matrix.m32;
      this.m33 = this.b30 * matrix.m03 + this.b31 * matrix.m13 + this.b32 * matrix.m23 + this.b33 * matrix.m33;
   }

   public void addVertex(double xCoord, double yCoord, double zCoord) {
      float fXCoord = (float)xCoord;
      float fYCoord = (float)yCoord;
      float fZCoord = (float)zCoord;
      float tx = this.m00 * fXCoord + this.m01 * fYCoord + this.m02 * fZCoord + this.m03;
      float ty = this.m10 * fXCoord + this.m11 * fYCoord + this.m12 * fZCoord + this.m13;
      float tz = this.m20 * fXCoord + this.m21 * fYCoord + this.m22 * fZCoord + this.m23;
      float ts = tx + ty + tz;
      PbCarvingCell.PbCellVert newVert = new PbCarvingCell.PbCellVert(tx, ty, tz, ts, fXCoord, fYCoord, fZCoord);
      this.cellNow.addNewCollectingVert(newVert);
      if (this.cellNow.collectingVertCount > this.faceNum) {
         throw new RuntimeException("cant happen!");
      } else {
         if (this.cellNow.collectingVertCount == this.faceNum) {
            PbCarvingCell.PbCellFace newFace = new PbCarvingCell.PbCellFace(this.faceNum);
            newFace.sideIdForDelim = this.turnSideByPartsFace(this.sideIdForDelimPreset);
            newFace.groupNum = this.partsChunkGroupNow;
            this.cellNow.commitNewFace(this, newFace);
            newFace.calcNormalNewell();
         }

      }
   }

   public int turnSideByPartsFace(int face) {
      if (face >= 0 && face <= 5) {
         return this.partsFacing >= 2 && this.partsFacing <= 5 ? JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(this.partsFacing, face) : face;
      } else {
         return face;
      }
   }

   public void addAdditonalFacingRotatoinOwn(int facing, boolean negateY) {
      float rotY = JBRotationUtil.roatYawEachSide[facing];
      if (negateY) {
         this.rotateYY(-rotY * 0.017453292F);
      } else {
         this.rotateYY(rotY * 0.017453292F);
      }

   }

   public void prebuildJointCarvedModelByType(JointPartsRenderable jointParts) {
      this.partsFacing = jointParts.facing;
      this.partsChunkGroupNow = jointParts.group;
      PbCarvingCell.PbBoxMatrix boxMatrix = new PbCarvingCell.PbBoxMatrix();
      jointParts.getAdditional().boxMatrix = boxMatrix;
      boxMatrix.palletIdx = this.palletIdxTemp;
      switch (jointParts.carvingType) {
         case 2:
            prebuildPatternCarvedModel(jointParts.carvingData, this, boxMatrix);
            return;
         case 4:
            if (jointParts.mCubeGatheredData == null || jointParts.mCubeGatheredData.dim != JBCarvingDataUtil.getCarvingDivide(jointParts.carvingData)) {
               jointParts.mCubeGatheredData = MCubeMeshBuilder.gatherMCCarvingDataJointParts(jointParts.mCubeGatheredData, jointParts);
               if (jointParts.mCubeGatheredData == null) {
                  return;
               }
            }

            prebuildMCubeCarvedModel(jointParts.carvingData, jointParts.mCubeGatheredData, this, boxMatrix);
            return;
         default:
            prebuildBoxesCarvedModel(jointParts.carvingData, this, boxMatrix);
      }
   }

   public static final void prebuildBoxesCarvedModel(int[] carvingData, Prebuilder prebuilder, PbCarvingCell.PbBoxMatrix boxMatrix) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         double unitd = 1.0 / (double)divideCount;
         int color = 0;
         int boxIndexCnt = -1;
         prebuilder.faceNum = 3;
         prebuilder.cellNow = null;
         boxMatrix.divCount = (byte)divideCount;
         PbCarvingCell[] divMatrixForBlock = boxMatrix.createMatrix(divideCount * divideCount * divideCount);

         for(int y = 0; y < divideCount; ++y) {
            double minY = (double)y * unitd;
            double maxY = minY + unitd;

            for(int z = 0; z < divideCount; ++z) {
               double minZ = (double)z * unitd;
               double maxZ = minZ + unitd;

               for(int x = 0; x < divideCount; ++x) {
                  ++boxIndexCnt;
                  double minX = (double)x * unitd;
                  double maxX = minX + unitd;
                  int mod = (boxIndexCnt & 7) << 2;
                  int dat = carvingData[boxIndexCnt >> 3] >>> mod & 15;
                  prebuilder.cellNow = prebuilder.createCellTurned(divideCount, x, y, z, divMatrixForBlock);
                  if ((dat & 8) != 0) {
                     prebuilder.cellNow.setAllFilled();
                  } else if (dat != 0) {
                     prebuilder.cellNow.colorIdx = dat;
                     prebuilder.sideIdForDelimPreset = 0;
                     prebuilder.addVertex(minX, minY, maxZ);
                     prebuilder.addVertex(minX, minY, minZ);
                     prebuilder.addVertex(maxX, minY, minZ);
                     prebuilder.addVertex(maxX, minY, minZ);
                     prebuilder.addVertex(maxX, minY, maxZ);
                     prebuilder.addVertex(minX, minY, maxZ);
                     prebuilder.sideIdForDelimPreset = 1;
                     prebuilder.addVertex(maxX, maxY, maxZ);
                     prebuilder.addVertex(maxX, maxY, minZ);
                     prebuilder.addVertex(minX, maxY, minZ);
                     prebuilder.addVertex(minX, maxY, minZ);
                     prebuilder.addVertex(minX, maxY, maxZ);
                     prebuilder.addVertex(maxX, maxY, maxZ);
                     prebuilder.sideIdForDelimPreset = 2;
                     prebuilder.addVertex(minX, maxY, minZ);
                     prebuilder.addVertex(maxX, maxY, minZ);
                     prebuilder.addVertex(maxX, minY, minZ);
                     prebuilder.addVertex(maxX, minY, minZ);
                     prebuilder.addVertex(minX, minY, minZ);
                     prebuilder.addVertex(minX, maxY, minZ);
                     prebuilder.sideIdForDelimPreset = 3;
                     prebuilder.addVertex(minX, maxY, maxZ);
                     prebuilder.addVertex(minX, minY, maxZ);
                     prebuilder.addVertex(maxX, minY, maxZ);
                     prebuilder.addVertex(maxX, minY, maxZ);
                     prebuilder.addVertex(maxX, maxY, maxZ);
                     prebuilder.addVertex(minX, maxY, maxZ);
                     prebuilder.sideIdForDelimPreset = 4;
                     prebuilder.addVertex(minX, maxY, maxZ);
                     prebuilder.addVertex(minX, maxY, minZ);
                     prebuilder.addVertex(minX, minY, minZ);
                     prebuilder.addVertex(minX, minY, minZ);
                     prebuilder.addVertex(minX, minY, maxZ);
                     prebuilder.addVertex(minX, maxY, maxZ);
                     prebuilder.sideIdForDelimPreset = 5;
                     prebuilder.addVertex(maxX, minY, maxZ);
                     prebuilder.addVertex(maxX, minY, minZ);
                     prebuilder.addVertex(maxX, maxY, minZ);
                     prebuilder.addVertex(maxX, maxY, minZ);
                     prebuilder.addVertex(maxX, maxY, maxZ);
                     prebuilder.addVertex(maxX, minY, maxZ);
                     prebuilder.cellNow.setAllFilled();
                  }
               }
            }
         }

         prebuilder.cellNow = null;
      }
   }

   public static final void prebuildMCubeCarvedModel(int[] carvingData, MCubeGathered mCubeGatheredData, Prebuilder prebuilder, PbCarvingCell.PbBoxMatrix boxMatrix) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         MCubeMeshBuilder builder = MCubeMeshBuilder.instance;
         builder.buildMesh(divideCount, mCubeGatheredData);
         if (builder.addedvertexcount != 0) {
            float scale = 1.0F / (float)divideCount;
            float baseX = 0.0F - scale / 2.0F;
            float baseY = 0.0F - scale / 2.0F;
            float baseZ = 0.0F - scale / 2.0F;
            prebuilder.faceNum = 3;
            prebuilder.sideIdForDelimPreset = -1;
            prebuilder.cellNow = new PbCarvingCell(0, 0, 0);
            prebuilder.cellNow.colorIdx = 7;
            boxMatrix.divCount = (byte)divideCount;
            boxMatrix.createMatrix(1)[0] = prebuilder.cellNow;
            int len = builder.addedindexcount;
            int[] idxAry = builder.indices;

            for(int i = 0; i < len; i += 3) {
               MCubeMeshBuilder.MeshVertex vertA = builder.vertices[idxAry[i + 0]];
               MCubeMeshBuilder.MeshVertex vertB = builder.vertices[idxAry[i + 1]];
               MCubeMeshBuilder.MeshVertex vertC = builder.vertices[idxAry[i + 2]];
               prebuilder.addVertex((double)(baseX + vertA.vertexX * scale), (double)(baseY + vertA.vertexY * scale), (double)(baseZ + vertA.vertexZ * scale));
               prebuilder.addVertex((double)(baseX + vertB.vertexX * scale), (double)(baseY + vertB.vertexY * scale), (double)(baseZ + vertB.vertexZ * scale));
               prebuilder.addVertex((double)(baseX + vertC.vertexX * scale), (double)(baseY + vertC.vertexY * scale), (double)(baseZ + vertC.vertexZ * scale));
            }

            prebuilder.cellNow = null;
         }
      }
   }

   public static final void prebuildPatternCarvedModel(int[] carvingData, Prebuilder prebuilder, PbCarvingCell.PbBoxMatrix boxMatrix) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         int actualLen = JBCarvingDataUtil.getCVDataLenFromDiv(2, divideCount) - 1;
         int lastIdx = 0;

         int i;
         int boxIndexCnt;
         for(i = actualLen; i > 0 && i < carvingData.length - 1; ++i) {
            int combined = carvingData[i];
            boxIndexCnt = i - actualLen << 1;
            int first = combined & '\uffff' | (carvingData[actualLen] >> boxIndexCnt & 1) << 16;
            int secondTabelIdx = (i - actualLen << 1) + 1;
            int second = combined >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
            JBPatternCarvingClientUtil.setPatternHolderState(JBPatternCarvingClientUtil.patternRenderWorkTable[boxIndexCnt], first);
            lastIdx = secondTabelIdx;
            JBPatternCarvingClientUtil.setPatternHolderState(JBPatternCarvingClientUtil.patternRenderWorkTable[secondTabelIdx], second);
         }

         for(i = lastIdx + 1; i < 16; ++i) {
            JBPatternCarvingClientUtil.patternRenderWorkTable[i].pp = null;
         }

         prebuilder.faceNum = 3;
         prebuilder.sideIdForDelimPreset = -1;
         prebuilder.cellNow = null;
         boxMatrix.divCount = (byte)divideCount;
         PbCarvingCell[] divMatrixForBlock = boxMatrix.createMatrix(divideCount * divideCount * divideCount);
         int color = 0;
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
                  prebuilder.cellNow = prebuilder.createCellTurned(divideCount, x, y, z, divMatrixForBlock);
                  if ((cIndex & 8) != 0) {
                     prebuilder.cellNow.setAllFilled();
                  } else if (cIndex != 0) {
                     int pIndex = dat & 15;
                     JBPatternCarvingClientUtil.PatternHolder pth = JBPatternCarvingClientUtil.patternRenderWorkTable[pIndex];
                     if (pth.pp != null) {
                        prebuilder.cellNow.colorIdx = cIndex;
                        pth.pp.prebuildPattern(prebuilder, minX, minY, minZ, unitd, pth.rotSideFace, pth.args);
                     }

                     prebuilder.cellNow.calclulateFilled(prebuilder, unitd, minX, minY, minZ);
                  }
               }
            }
         }

         prebuilder.cellNow = null;
      }
   }

   private PbCarvingCell createCellTurned(int divCount, int x, int y, int z, PbCarvingCell[] divMatrixForBlock) {
      int divCntSub1 = divCount - 1;
      int divCntPow = divCount * divCount;
      int fixedX = x;
      int fixedZ = z;
      switch (this.partsFacing) {
         case 2:
            fixedX = divCntSub1 - x;
            fixedZ = divCntSub1 - z;
         case 3:
         default:
            break;
         case 4:
            fixedX = divCntSub1 - z;
            fixedZ = x;
            break;
         case 5:
            fixedX = z;
            fixedZ = divCntSub1 - x;
      }

      PbCarvingCell cellNew = new PbCarvingCell(fixedX, y, fixedZ);
      int boxIndexCnt = y * divCntPow + fixedZ * divCount + fixedX;
      return divMatrixForBlock[boxIndexCnt] = cellNew;
   }

   public void processBlockPolys(JointPartsRenderable targetParts) {
      if (targetParts.additional != null) {
         PbCarvingCell.PbBoxMatrix boxMatrix = targetParts.getAdditional().boxMatrix;
         if (boxMatrix != null) {
            int side;
            if (this.isBoxOrPatternCarving(targetParts)) {
               boxMatrix.neighBourBoxMatrixes = new PbCarvingCell.PbBoxMatrix[6];

               for(side = 0; side < 6; ++side) {
                  JointPartsRenderable jointNeighbour = this.tryGetSide(targetParts, side);
                  if (jointNeighbour != null && jointNeighbour.additional != null && targetParts.group == jointNeighbour.group && this.isBoxOrPatternCarving(jointNeighbour)) {
                     PbCarvingCell.PbBoxMatrix boxMatrixNeighbour = jointNeighbour.additional.boxMatrix;
                     boxMatrix.neighBourBoxMatrixes[side] = boxMatrixNeighbour;
                  }
               }

               this.decimateCellFaces(boxMatrix);
            }

            side = 0;

            for(int len = boxMatrix.cellMatrix.length; side < len; ++side) {
               PbCarvingCell cell = boxMatrix.cellMatrix[side];
               this.addAllFaces(cell, boxMatrix.palletIdx);
            }

         }
      }
   }

   private JointPartsRenderable tryGetSide(JointPartsRenderable targetParts, int side) {
      int nX = targetParts.xCoord + EnumDirectionOld.VALID_DIRECTIONS[side].offsetX;
      int nY = targetParts.yCoord + EnumDirectionOld.VALID_DIRECTIONS[side].offsetY;
      int nZ = targetParts.zCoord + EnumDirectionOld.VALID_DIRECTIONS[side].offsetZ;
      return this.blockedPartsMap.get(nX, nY, nZ);
   }

   private boolean isBoxOrPatternCarving(JointPartsRenderable jp) {
      return jp.carvingData != null && (jp.carvingType == 0 || jp.carvingType == 2);
   }

   public void decimateCellFaces(PbCarvingCell.PbBoxMatrix boxMatrix) {
      PbCarvingCell[] cellMatrixForBlock = boxMatrix.cellMatrix;
      PbCarvingCell.PbBoxMatrix[] neighM = boxMatrix.neighBourBoxMatrixes;
      int divideCount = boxMatrix.divCount;
      int divCntSub1 = divideCount - 1;
      int divCntPow = divideCount * divideCount;
      int i = 0;

      for(int len = cellMatrixForBlock.length; i < len; ++i) {
         PbCarvingCell cell = cellMatrixForBlock[i];
         int x = cell.x;
         int y = cell.y;
         int z = cell.z;
         PbCarvingCell cellNeighXN = null;
         PbCarvingCell.PbBoxMatrix neighbBox;
         int nidx;
         if (x > 0) {
            nidx = y * divCntPow + z * divideCount + (x - 1);
            cellNeighXN = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[4]) != null && neighbBox.divCount == divideCount) {
            nidx = y * divCntPow + z * divideCount + divCntSub1;
            cellNeighXN = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighXN != null && cellNeighXN.isXPosFilled()) {
            cell.decimateXNeg();
         }

         PbCarvingCell cellNeighXP = null;
         if (x < divCntSub1) {
            nidx = y * divCntPow + z * divideCount + x + 1;
            cellNeighXP = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[5]) != null && neighbBox.divCount == divideCount) {
            nidx = y * divCntPow + z * divideCount + 0;
            cellNeighXP = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighXP != null && cellNeighXP.isXNegFilled()) {
            cell.decimateXPos();
         }

         PbCarvingCell cellNeighYN = null;
         if (y > 0) {
            nidx = (y - 1) * divCntPow + z * divideCount + x;
            cellNeighYN = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[0]) != null && neighbBox.divCount == divideCount) {
            nidx = divCntSub1 * divCntPow + z * divideCount + x;
            cellNeighYN = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighYN != null && cellNeighYN.isYPosFilled()) {
            cell.decimateYNeg();
         }

         PbCarvingCell cellNeighYP = null;
         if (y < divCntSub1) {
            nidx = (y + 1) * divCntPow + z * divideCount + x;
            cellNeighYP = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[1]) != null && neighbBox.divCount == divideCount) {
            nidx = 0 * divCntPow + z * divideCount + x;
            cellNeighYP = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighYP != null && cellNeighYP.isYNegFilled()) {
            cell.decimateYPos();
         }

         PbCarvingCell cellNeighZN = null;
         if (z > 0) {
            nidx = y * divCntPow + (z - 1) * divideCount + x;
            cellNeighZN = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[2]) != null && neighbBox.divCount == divideCount) {
            nidx = y * divCntPow + divCntSub1 * divideCount + x;
            cellNeighZN = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighZN != null && cellNeighZN.isZPosFilled()) {
            cell.decimateZNeg();
         }

         PbCarvingCell cellNeighZP = null;
         if (z < divCntSub1) {
            nidx = y * divCntPow + (z + 1) * divideCount + x;
            cellNeighZP = cellMatrixForBlock[nidx];
         } else if ((neighbBox = neighM[3]) != null && neighbBox.divCount == divideCount) {
            nidx = y * divCntPow + 0 * divideCount + x;
            cellNeighZP = neighbBox.cellMatrix[nidx];
         }

         if (cellNeighZP != null && cellNeighZP.isZNegFilled()) {
            cell.decimateZPos();
         }
      }

   }

   public void addAllFaces(PbCarvingCell cell, byte palletIdx) {
      PbCarvingCell.PbCellFace nextFaceTemp;
      for(PbCarvingCell.PbCellFace targetFace = cell.faceFirst; targetFace != null; targetFace = nextFaceTemp) {
         nextFaceTemp = targetFace.nextFaceRef;
         if (!targetFace.isDecimated) {
            int targetMatIdx = palletIdx * 8 + cell.colorIdx;
            PbCarvingCell.PbWFObjGroup[] groupListLoc = this.faceNum == 4 ? this.groupListF4 : this.groupListF3;
            PbCarvingCell.PbWFObjGroup group = groupListLoc[targetMatIdx];
            if (group == null) {
               group = groupListLoc[targetMatIdx] = new PbCarvingCell.PbWFObjGroup(targetMatIdx, palletIdx, cell.colorIdx, this.faceNum);
            }

            group.addFace(targetFace);

            for(int fv = 0; fv < targetFace.vertIdxCnt; ++fv) {
               PbCarvingCell.PbCellVert targetVert = targetFace.vertices[fv];
               this.vertices.add(targetVert);
            }

            if (this.matList[targetMatIdx] == null) {
               this.matList[targetMatIdx] = new PbCarvingCell.PbWFMaterial(targetMatIdx, palletIdx, cell.colorIdx);
            }
         }
      }

   }

   public void collectPallete(EntityJointed entityJointed) {
      try {
         for(int m = 0; m < this.matList.length; ++m) {
            PbCarvingCell.PbWFMaterial material = this.matList[m];
            if (material != null) {
               int[] paletteData = null;
               if (entityJointed.paletteDataList != null) {
                  paletteData = entityJointed.paletteDataList[material.palIdx];
               }

               if (paletteData == null) {
                  paletteData = TileEntityJointCore.DEFAULT_PALLETE_DATA[material.palIdx];
               }

               int color = paletteData[material.colIdx];
               material.colorR = color >> 16 & 255;
               material.colorG = color >> 8 & 255;
               material.colorB = color & 255;
            }
         }
      } catch (Exception var6) {
      }

   }

   public void mergeDupeVertices() {
      Collections.sort(this.vertices, new Comparator<PbCarvingCell.PbCellVert>() {
         public int compare(PbCarvingCell.PbCellVert vt1, PbCarvingCell.PbCellVert vt2) {
            return Float.compare(vt1.ts, vt2.ts);
         }
      });
      int v = 0;
      int vertCnt = this.vertices.size();

      PbCarvingCell.PbCellVert vert;
      int v2;
      for(v = 0; v < vertCnt; ++v) {
         if ((vert = (PbCarvingCell.PbCellVert)this.vertices.get(v)).vertIdx_and_flg >= 0) {
            label134:
            for(v2 = v + 1; v2 < vertCnt; ++v2) {
               PbCarvingCell.PbCellVert vertOther;
               if ((vertOther = (PbCarvingCell.PbCellVert)this.vertices.get(v2)).vertIdx_and_flg >= 0) {
                  if (vertOther.ts - vert.ts > MERGE_DIST_3X) {
                     break;
                  }

                  if (vert.distanceToSq(vertOther) < 9.999999E-9F) {
                     vert.vertIdx_and_flg = -1;
                     vertOther.vertIdx_and_flg = -2;
                     Iterator var6 = vertOther.holderFaces.iterator();

                     while(true) {
                        while(true) {
                           if (!var6.hasNext()) {
                              continue label134;
                           }

                           PbCarvingCell.PbCellFace vh = (PbCarvingCell.PbCellFace)var6.next();

                           for(int fv = 0; fv < vh.vertIdxCnt; ++fv) {
                              if (vh.vertices[fv] == vertOther) {
                                 vh.vertices[fv] = vert;
                                 vert.holderFaces.add(vh);
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      ArrayList<PbCarvingCell.PbCellVert> droppedList = new ArrayList(this.vertices.size() / 2);
      v = 0;

      for(v2 = this.vertices.size(); v < v2; ++v) {
         if ((vert = (PbCarvingCell.PbCellVert)this.vertices.get(v)).vertIdx_and_flg != -2) {
            int decimatedFaceCnt = 0;
            Iterator var16 = vert.holderFaces.iterator();

            label101:
            while(var16.hasNext()) {
               PbCarvingCell.PbCellFace vFaceA1 = (PbCarvingCell.PbCellFace)var16.next();
               Iterator var9 = vert.holderFaces.iterator();

               while(true) {
                  PbCarvingCell.PbCellFace vFaceA2;
                  do {
                     do {
                        if (!var9.hasNext()) {
                           if (vFaceA1.isDecimated) {
                              ++decimatedFaceCnt;
                           }
                           continue label101;
                        }

                        vFaceA2 = (PbCarvingCell.PbCellFace)var9.next();
                     } while(vFaceA1 == vFaceA2);
                  } while(vFaceA1.isDecimated && vFaceA2.isDecimated);

                  int matchedCnt = 0;

                  for(int fvA1 = 0; fvA1 < vFaceA1.vertIdxCnt; ++fvA1) {
                     for(int fvA2 = 0; fvA2 < vFaceA2.vertIdxCnt; ++fvA2) {
                        if (vFaceA1.vertices[fvA1] == vFaceA2.vertices[fvA2]) {
                           ++matchedCnt;
                           break;
                        }
                     }
                  }

                  if (matchedCnt == vFaceA1.vertIdxCnt && PbCarvingCell.PbCellFace.checkNormalDot(vFaceA1, vFaceA2, true)) {
                     vFaceA1.isDecimated = true;
                     vFaceA2.isDecimated = true;
                  }
               }
            }

            if (decimatedFaceCnt == vert.holderFaces.size()) {
               vert.vertIdx_and_flg = -2;
            }

            if (vert.vertIdx_and_flg >= -1) {
               droppedList.add(vert);
               vert.nextVertRef = null;
            }
         }
      }

      JointBlockRemastered.LOGGER.debug("[JointBlock:ObjOut]dropedVerts:" + (this.vertices.size() - droppedList.size()));
      JointBlockRemastered.LOGGER.debug("[JointBlock:ObjOut]total_Verts:" + droppedList.size());
      this.vertices = droppedList;
   }

   public void mergeEdgeByEdgeFaces() {
      PbCarvingCell.PbCellEdge edgeTemp = new PbCarvingCell.PbCellEdge();

      int cnt;
      PbCarvingCell.PbWFObjGroup[] groupList;
      int i;
      int f;
      int nf;
      for(cnt = 0; cnt < 2; ++cnt) {
         groupList = cnt == 0 ? this.groupListF3 : this.groupListF4;

         label315:
         for(i = 0; i < groupList.length; ++i) {
            if (groupList[i] != null) {
               HashMap<PbCarvingCell.PbCellEdge, PbCarvingCell.PbCellEdge> edgeSet = new HashMap();

               PbCarvingCell.PbCellVert vertNow;
               PbCarvingCell.PbCellVert vertOth;
               for(PbCarvingCell.PbCellFace targetFace = groupList[i].faceFirst; targetFace != null; targetFace = targetFace.nextFaceRef) {
                  if (!targetFace.isDecimated) {
                     f = 0;

                     for(nf = targetFace.vertIdxCnt; f < nf; ++f) {
                        vertNow = targetFace.vertices[f];
                        vertOth = f == nf - 1 ? targetFace.vertices[0] : targetFace.vertices[f + 1];
                        if (!edgeTemp.setVertPairSort(vertNow, vertOth)) {
                           targetFace.isDecimated = true;
                           break;
                        }

                        PbCarvingCell.PbCellEdge edgeContains = (PbCarvingCell.PbCellEdge)edgeSet.get(edgeTemp);
                        if (edgeContains == null) {
                           edgeSet.put(edgeTemp, edgeTemp);
                           edgeContains = edgeTemp;
                           edgeTemp = new PbCarvingCell.PbCellEdge();
                        }

                        targetFace.addEdge(f, edgeContains, targetFace);
                     }
                  }
               }

               ArrayList<PbCarvingCell.PbCellEdge> edgeListForGroup = new ArrayList(edgeSet.keySet());
               JointBlockRemastered.LOGGER.debug("[JointBlock:ObjOut]group_edgecount:" + edgeListForGroup.size());
               nf = 0;
               Queue<PbCarvingCell.PbCellFace[]> edgeQueue = new LinkedList();
               int e = 0;
               int edgeCnt = edgeListForGroup.size();
               for(e = 0; e < edgeCnt; ++e) {
                  PbCarvingCell.PbCellEdge edge = (PbCarvingCell.PbCellEdge)edgeListForGroup.get(e);
                  Iterator var15 = edge.holderFaces.iterator();

                  while(var15.hasNext()) {
                     PbCarvingCell.PbCellFace eFaceA = (PbCarvingCell.PbCellFace)var15.next();
                     if (!eFaceA.isDecimated) {
                        this.checkForNewFaceQueue(edge, edgeQueue, eFaceA, false);
                     }
                  }
               }

               while(true) {
                  PbCarvingCell.PbCellFace newFace;
                  do {
                     PbCarvingCell.PbCellFace[] facePair;
                     do {
                        do {
                           do {
                              do {
                                 if (edgeQueue.isEmpty()) {
                                    JointBlockRemastered.LOGGER.debug("[JointBlock:ObjOut]group_mergedFaceCnt:" + nf);
                                    edgeListForGroup.clear();
                                    edgeListForGroup.addAll(edgeSet.keySet());
                                    edgeQueue.clear();
                                    e = 0;

                                    for(int len = edgeListForGroup.size(); e < len; ++e) {
                                       PbCarvingCell.PbCellEdge edge = (PbCarvingCell.PbCellEdge)edgeListForGroup.get(e);
                                       Iterator var36 = edge.holderFaces.iterator();

                                       while(var36.hasNext()) {
                                          PbCarvingCell.PbCellFace eFaceA = (PbCarvingCell.PbCellFace)var36.next();
                                          if (!(eFaceA instanceof PbCarvingCell.PbCellFaceWithSubs)) {
                                             this.checkForNewFaceQueue(edge, edgeQueue, eFaceA, true);
                                          }
                                       }
                                    }

                                    while(true) {
                                       do {
                                          if (edgeQueue.isEmpty()) {
                                             continue label315;
                                          }

                                          facePair = (PbCarvingCell.PbCellFace[])edgeQueue.remove();
                                       } while(facePair[0].isDecimated && facePair[1].isDecimated);

                                       try {
                                          this.annihilationOppositeFaces(facePair);
                                       } catch (Exception var19) {
                                          if (++this.errorCount < 5) {
                                             var19.printStackTrace();
                                          }
                                       }
                                    }
                                 }

                                 facePair = (PbCarvingCell.PbCellFace[])edgeQueue.remove();
                              } while(facePair[0].isDecimated);
                           } while(facePair[1].isDecimated);
                        } while(facePair[0].isSubloop);
                     } while(facePair[1].isSubloop);

                     newFace = null;

                     try {
                        newFace = this.mergeFaceByEdgeEx(facePair);
                     } catch (Exception var20) {
                        if (++this.errorCount < 5) {
                           var20.printStackTrace();
                        }
                     }
                  } while(newFace == null);

                  ++nf;
                  groupList[i].addFace(newFace);

                  for(PbCarvingCell.PbCellFace localFaces = newFace.getActualFaceFirst(); localFaces != null; localFaces = localFaces.nextSubLinkRef) {
                     int fv = 0;

                     for(int len = localFaces.vertIdxCnt; fv < len; ++fv) {
                        vertNow = localFaces.vertices[fv];
                        vertOth = localFaces.vertices[fv == len - 1 ? 0 : fv + 1];
                        if (!edgeTemp.setVertPairSort(vertNow, vertOth)) {
                           throw new RuntimeException("cant happen");
                        }

                        PbCarvingCell.PbCellEdge edgeContains = (PbCarvingCell.PbCellEdge)edgeSet.get(edgeTemp);
                        if (edgeContains == null) {
                           edgeSet.put(edgeTemp, edgeTemp);
                           edgeContains = edgeTemp;
                           edgeTemp = new PbCarvingCell.PbCellEdge();
                        }

                        localFaces.addEdge(fv, edgeContains, newFace);
                        this.checkForNewFaceQueue(edgeContains, edgeQueue, newFace, false);
                     }
                  }
               }
            }
         }
      }

      cnt = 0;

      for(int lenPv = this.vertices.size(); cnt < lenPv; ++cnt) {
         ((PbCarvingCell.PbCellVert)this.vertices.get(cnt)).vertIdx_and_flg = -2;
      }

      for(cnt = 0; cnt < 2; ++cnt) {
         groupList = cnt == 0 ? this.groupListF3 : this.groupListF4;

         for(i = 0; i < groupList.length; ++i) {
            if (groupList[i] != null) {
               PbCarvingCell.PbCellFace targetFace = groupList[i].faceFirst;
               ArrayList<PbCarvingCell.PbCellFace> splittedFaceWork = this.mergeHoleFaceWork;
               splittedFaceWork.clear();

               PbCarvingCell.PbCellFace actualFace;
               for(; targetFace != null; targetFace = targetFace.nextFaceRef) {
                  if (!targetFace.isDecimated) {
                     for(actualFace = targetFace.getActualFaceFirst(); actualFace != null; actualFace = actualFace.nextSubLinkRef) {
                        this.removeFaceStraightLines(actualFace);
                     }
                  }

                  if (!targetFace.isDecimated && targetFace instanceof PbCarvingCell.PbCellFaceWithSubs) {
                     try {
                        this.splitHoledFace((PbCarvingCell.PbCellFaceWithSubs)targetFace, splittedFaceWork);
                        targetFace.isDecimated = true;
                     } catch (Exception var18) {
                        targetFace.hadError = true;

                        for(PbCarvingCell.PbCellFace af = targetFace.getActualFaceFirst(); af != null; af = af.nextSubLinkRef) {
                           this.resumeFromWrapper(af, (Matrix3f)null);
                        }

                        if (++this.errorCount < 5) {
                           var18.printStackTrace();
                        }
                     }
                  }
               }

               f = 0;

               for(nf = splittedFaceWork.size(); f < nf; ++f) {
                  groupList[i].addFace((PbCarvingCell.PbCellFace)splittedFaceWork.get(f));
               }

               targetFace = groupList[i].faceFirst;
               splittedFaceWork.clear();

               for(; targetFace != null; targetFace = targetFace.nextFaceRef) {
                  if (!targetFace.isDecimated) {
                     actualFace = targetFace.getActualFaceFirst();
                     this.splitDupedVertFace(actualFace);
                     this.removeFaceStraightLines(actualFace);
                     nf = 0;

                     for(int len2 = actualFace.vertIdxCnt; nf < len2; ++nf) {
                        actualFace.vertices[nf].vertIdx_and_flg = -1;
                     }

                     if (this.triangulateAtLast) {
                        this.triangulateSingleFace(actualFace, splittedFaceWork);
                     }
                  }
               }

               if (this.triangulateAtLast) {
                  groupList[i].clearFaceLink();
                  f = 0;

                  for(nf = splittedFaceWork.size(); f < nf; ++f) {
                     groupList[i].addFace((PbCarvingCell.PbCellFace)splittedFaceWork.get(f));
                  }
               }
            }
         }
      }

   }

   private void removeFaceStraightLines(PbCarvingCell.PbCellFace face) {
      this.mergeFaceVertWork.clear();
      int nf = 0;
      int len2 = face.vertIdxCnt;

      for(int lenSub1 = len2 - 1; nf < len2; ++nf) {
         PbCarvingCell.PbCellVert vertPrev = face.vertices[nf == 0 ? lenSub1 : nf - 1];
         PbCarvingCell.PbCellVert vertCent = face.vertices[nf];
         PbCarvingCell.PbCellVert vertNext = face.vertices[nf == lenSub1 ? 0 : nf + 1];
         if (!this.isStraightOrLapelLine(vertPrev, vertCent, vertNext)) {
            this.mergeFaceVertWork.add(vertCent);
         }
      }

      if (this.mergeFaceVertWork.size() < 3) {
         face.isDecimated = true;
      } else {
         face.vertIdxCnt = this.mergeFaceVertWork.size();
         nf = 0;

         for(len2 = this.mergeFaceVertWork.size(); nf < len2; ++nf) {
            face.vertices[nf] = (PbCarvingCell.PbCellVert)this.mergeFaceVertWork.get(nf);
         }

         face.edges = null;
      }

   }

   private void splitDupedVertFace(PbCarvingCell.PbCellFace targetFace) {
      boolean hadDupe = false;

      label30:
      do {
         hadDupe = false;
         int v1 = 0;

         for(int len = targetFace.vertIdxCnt; v1 < len; ++v1) {
            PbCarvingCell.PbCellVert vert1 = targetFace.vertices[v1];

            for(int v2 = v1 + 1; v2 < len; ++v2) {
               PbCarvingCell.PbCellVert vert2 = targetFace.vertices[v2];
               if (vert1 == vert2) {
                  targetFace.vertices[v2] = vert1.cloneForDuped(this.vertices);
                  hadDupe = true;
                  continue label30;
               }
            }
         }
      } while(hadDupe);

   }

   private void checkForNewFaceQueue(PbCarvingCell.PbCellEdge edge, Queue<PbCarvingCell.PbCellFace[]> faceMergeQueue, PbCarvingCell.PbCellFace faceCheckBase, boolean matchOpposite) {
      Iterator var5 = edge.holderFaces.iterator();

      while(true) {
         PbCarvingCell.PbCellFace eFaceToCheckWith;
         while(true) {
            do {
               if (!var5.hasNext()) {
                  return;
               }

               eFaceToCheckWith = (PbCarvingCell.PbCellFace)var5.next();
            } while(eFaceToCheckWith.isDecimated);

            if (matchOpposite) {
               if (eFaceToCheckWith instanceof PbCarvingCell.PbCellFaceWithSubs) {
                  continue;
               }
            } else if (faceCheckBase.groupNum != eFaceToCheckWith.groupNum) {
               continue;
            }
            break;
         }

         if (faceCheckBase != eFaceToCheckWith && PbCarvingCell.PbCellFace.checkNormalDot(faceCheckBase, eFaceToCheckWith, matchOpposite)) {
            faceMergeQueue.add(new PbCarvingCell.PbCellFace[]{faceCheckBase, eFaceToCheckWith});
         }
      }
   }

   private PbCarvingCell.PbCellFace mergeFaceByEdgeEx(PbCarvingCell.PbCellFace[] pair) {
      int FACENUM = 1;
      ArrayList<PbCarvingCell.PbCellVert> vertWork = this.mergeFaceVertWork;
      ArrayList<PbCarvingCell.PbCellFace> faceWork = this.mergeFaceFaceWork;
      vertWork.clear();
      faceWork.clear();
      PbCarvingCell.PbCellFace mergeFaceBaseA = pair[0];
      PbCarvingCell.PbCellFace mergeFaceBaseB = pair[1];
      boolean isFaceAWithSub = mergeFaceBaseA instanceof PbCarvingCell.PbCellFaceWithSubs;
      boolean isFaceBWithSub = mergeFaceBaseB instanceof PbCarvingCell.PbCellFaceWithSubs;
      boolean compareMainLoopOnly = false;
      PbCarvingCell.PbCellFace mergeFaceMain;
      PbCarvingCell.PbCellFace mergeFaceOthe;
      if (!isFaceAWithSub && isFaceBWithSub) {
         mergeFaceMain = mergeFaceBaseB;
         mergeFaceOthe = mergeFaceBaseA;
      } else {
         compareMainLoopOnly = isFaceAWithSub == isFaceBWithSub;
         mergeFaceMain = mergeFaceBaseA;
         mergeFaceOthe = mergeFaceBaseB;
      }

      PbCarvingCell.PbCellFace localFacesOthe = mergeFaceOthe.getActualFaceFirst();
      PbCarvingCell.PbCellFace localFacesMain = mergeFaceMain.getActualFaceFirst();

      int i;
      int eIdx;
      int indexNow;
      while(localFacesMain != null) {
         this.vertJointIdxA = localFacesMain.setAndClearEdgeFlagWork(this.vertJointIdxA, false);
         this.vertJointIdxB = localFacesOthe.setAndClearEdgeFlagWork(this.vertJointIdxB, false);
         int remainEdgeCntA = localFacesMain.vertIdxCnt;
         int remainEdgeCntB = localFacesOthe.vertIdxCnt;

         PbCarvingCell.PbCellEdge edge1;
         PbCarvingCell.PbCellEdge edge2;
         for(i = 0; i < localFacesMain.vertIdxCnt; ++i) {
            edge1 = localFacesMain.edges[i];

            for(eIdx = 0; eIdx < localFacesOthe.vertIdxCnt; ++eIdx) {
               edge2 = localFacesOthe.edges[eIdx];
               if (edge1 == edge2 && localFacesMain.vertices[i] == localFacesOthe.vertices[eIdx]) {
                  return null;
               }
            }
         }

         for(i = 0; i < localFacesMain.vertIdxCnt; ++i) {
            edge1 = localFacesMain.edges[i];

            for(eIdx = 0; eIdx < localFacesOthe.vertIdxCnt; ++eIdx) {
               edge2 = localFacesOthe.edges[eIdx];
               if (edge2 != null && edge1 == edge2) {
                  edge1.holderFaces.remove(mergeFaceBaseA);
                  edge1.holderFaces.remove(mergeFaceBaseB);
                  localFacesMain.setBypathOther(i, eIdx + 1, localFacesOthe.vertIdxCnt);
                  localFacesOthe.setBypathOther(eIdx, i + 1, localFacesMain.vertIdxCnt);
                  localFacesMain.edges[i] = null;
                  localFacesOthe.edges[eIdx] = null;
                  --remainEdgeCntA;
                  --remainEdgeCntB;
               }
            }
         }

         if (remainEdgeCntA == localFacesMain.vertIdxCnt) {
            if (compareMainLoopOnly) {
               throw new RuntimeException("can't happen!! no match of edges!!");
            }

            localFacesMain = localFacesMain.nextSubLinkRef;
         } else {
            localFacesMain.isDecimated = true;
            localFacesOthe.isDecimated = true;
            if (remainEdgeCntA == 0 && remainEdgeCntB == 0) {
               break;
            }

            i = remainEdgeCntA + remainEdgeCntB;
            PbCarvingCell.PbCellFace current = localFacesMain;
            PbCarvingCell.PbCellFace connectee = localFacesOthe;

            for(indexNow = 0; indexNow < 2; ++indexNow) {
               for(eIdx = 0; eIdx < current.vertIdxCnt; ++eIdx) {
                  if (current.isBypathOther(eIdx) && connectee.edges[current.getBypathIndex(eIdx)] == null) {
                     int searchHit = current.searchUnMergedDupeVertIdx(eIdx);
                     if (searchHit == -1) {
                        searchHit = current.searchUnMergedIdxFromNext(eIdx + 1);
                        if (searchHit == -1) {
                           if (remainEdgeCntA != 0 && remainEdgeCntB != 0) {
                              throw new RuntimeException("cant't be happended!! no connection!!");
                           }

                           searchHit = eIdx + 1;
                        }
                     }

                     current.setConnectSelfIdx(eIdx, searchHit);
                  }
               }

               current = localFacesOthe;
               connectee = localFacesMain;
            }

            indexNow = -1;
            current = localFacesMain;
            connectee = localFacesOthe;

            while(i > 0) {
               PbCarvingCell.PbCellFace temp;
               if (indexNow == -1) {
                  if ((indexNow = current.searchUnMergedIdx(EDGE_COLLECTED)) < 0) {
                     temp = current;
                     current = connectee;
                     connectee = temp;
                     indexNow = current.searchUnMergedIdx(EDGE_COLLECTED);
                  }

                  if (indexNow < 0) {
                     throw new RuntimeException("can't happen!! still has remain cnt !");
                  }

                  if (vertWork.size() > 0) {
                     faceWork.add(this.createNewSingleFace(vertWork, true));
                     vertWork.clear();
                  }
               }

               if (current.isBypathOther(indexNow)) {
                  indexNow = current.getBypathIndex(indexNow);
                  temp = current;
                  current = connectee;
                  connectee = temp;
               } else if (current.edges[indexNow] == EDGE_COLLECTED) {
                  indexNow = -1;
               } else {
                  if (current.edges[indexNow] != null && current.edges[indexNow] != EDGE_COLLECTED) {
                     vertWork.add(current.vertices[indexNow]);
                     --i;
                  }

                  current.edges[indexNow] = EDGE_COLLECTED;
                  indexNow = current.getNextSelfIdx(indexNow);
               }
            }

            if (vertWork.size() > 0) {
               faceWork.add(this.createNewSingleFace(vertWork, true));
               vertWork.clear();
            }

            if (compareMainLoopOnly) {
               break;
            }

            localFacesMain = localFacesMain.nextSubLinkRef;
         }
      }

      mergeFaceBaseA.isDecimated = true;
      mergeFaceBaseB.isDecimated = true;

      for(localFacesMain = mergeFaceMain.getActualFaceFirst(); localFacesMain != null; localFacesMain = localFacesMain.nextSubLinkRef) {
         if (!localFacesMain.isDecimated) {
            faceWork.add(localFacesMain);
         }
      }

      for(localFacesOthe = mergeFaceOthe.getActualFaceFirst(); localFacesOthe != null; localFacesOthe = localFacesOthe.nextSubLinkRef) {
         if (!localFacesOthe.isDecimated) {
            faceWork.add(localFacesOthe);
         }
      }

      if (faceWork.size() == 0) {
         return null;
      } else {
         Object newFace;
         if (faceWork.size() == 1) {
            newFace = (PbCarvingCell.PbCellFace)faceWork.get(0);
            ((PbCarvingCell.PbCellFace)newFace).isSubloop = false;
         } else {
            boolean topHasCorrectNormal = false;
            i = 0;

            for(indexNow = faceWork.size(); i < indexNow; ++i) {
               PbCarvingCell.PbCellFace topTest = (PbCarvingCell.PbCellFace)faceWork.get(i);
               if (PbCarvingCell.PbCellFace.checkNormalDot(mergeFaceBaseA, topTest.calcNormalNewell(), false)) {
                  topHasCorrectNormal = true;
                  if (i == 0) {
                     break;
                  }

                  faceWork.set(i, faceWork.get(0));
                  faceWork.set(0, topTest);
               }
            }

            if (!topHasCorrectNormal) {
               throw new RuntimeException("can't happen normal is invalid!!");
            }

            PbCarvingCell.PbCellFaceWithSubs newFaceWithSubs = new PbCarvingCell.PbCellFaceWithSubs();
            indexNow = 0;

            for(eIdx = faceWork.size(); indexNow < eIdx; ++indexNow) {
               newFaceWithSubs.addSubFace((PbCarvingCell.PbCellFace)faceWork.get(indexNow));
            }

            newFace = newFaceWithSubs;
         }

         ((PbCarvingCell.PbCellFace)newFace).nX = mergeFaceBaseA.nX;
         ((PbCarvingCell.PbCellFace)newFace).nY = mergeFaceBaseA.nY;
         ((PbCarvingCell.PbCellFace)newFace).nZ = mergeFaceBaseA.nZ;
         ((PbCarvingCell.PbCellFace)newFace).groupNum = mergeFaceBaseA.groupNum;
         return (PbCarvingCell.PbCellFace)newFace;
      }
   }

   private PbCarvingCell.PbCellFace createNewSingleFace(ArrayList<PbCarvingCell.PbCellVert> verList, boolean genEdgeList) {
      PbCarvingCell.PbCellFace newFace = new PbCarvingCell.PbCellFace(verList.size(), genEdgeList);
      int i = 0;
      int len = verList.size();

      for(int lenSub1 = verList.size() - 1; i < len; ++i) {
         PbCarvingCell.PbCellVert vertPrev = (PbCarvingCell.PbCellVert)verList.get(i == 0 ? lenSub1 : i - 1);
         PbCarvingCell.PbCellVert vert = (PbCarvingCell.PbCellVert)verList.get(i);
         PbCarvingCell.PbCellVert vertNext = (PbCarvingCell.PbCellVert)verList.get(i == lenSub1 ? 0 : i + 1);
         if (vertPrev == vert || vert == vertNext || vertPrev == vertNext) {
            throw new RuntimeException("can't happen!! vert duped at face loop !");
         }

         newFace.addVert(vert, false);
      }

      return newFace;
   }

   private void annihilationOppositeFaces(PbCarvingCell.PbCellFace[] pair) {
      PbCarvingCell.PbCellFace annihFaceA = pair[0];
      PbCarvingCell.PbCellFace annihFaceB = pair[1];
      if (annihFaceA.vertIdxCnt == annihFaceB.vertIdxCnt) {
         int matchCnt = 0;

         for(int a = 0; a < annihFaceA.vertIdxCnt; ++a) {
            PbCarvingCell.PbCellEdge edgeA = annihFaceA.edges[a];

            for(int b = 0; b < annihFaceB.vertIdxCnt; ++b) {
               PbCarvingCell.PbCellEdge edgeB = annihFaceB.edges[b];
               if (edgeB != null && edgeA == edgeB) {
                  if (annihFaceA.vertices[a] == annihFaceB.vertices[b]) {
                     return;
                  }

                  ++matchCnt;
               }
            }
         }

         if (annihFaceA.vertIdxCnt == matchCnt) {
            annihFaceA.isDecimated = true;
            annihFaceB.isDecimated = true;
         }
      }

   }

   private void splitHoledFace(PbCarvingCell.PbCellFaceWithSubs faceToSplit, ArrayList<PbCarvingCell.PbCellFace> splittedHoleFaceWork) {
      ArrayList<PbCarvingCell.PbCellVert> vertWork = this.mergeFaceVertWork;
      ArrayList<PbCarvingCell.PbCellFace> faceWork = this.mergeFaceFaceWork;
      ArrayList<SplittedEdge> leftFaceEdgeList = this.splitWorkLeft;
      ArrayList<SplittedEdge> rightFaceEdgeList = this.splitWorkRight;
      ArrayList<SplittedEdge> leftFaceEdgeListLocal = this.splitWorkLeftLocal;
      ArrayList<SplittedEdge> rightFaceEdgeListLocal = this.splitWorkRightLocal;
      Matrix3f pm = this.planerMatrix;
      Matrix3f pmInv = this.planerMatrixInv;
      vertWork.clear();
      faceWork.clear();
      if (this.turnPointQueueLeft == null) {
         this.turnPointQueueLeft = new PriorityQueue(100, new Comparator<PbCarvingCell.PbCellVertWrapper>() {
            public int compare(PbCarvingCell.PbCellVertWrapper o1, PbCarvingCell.PbCellVertWrapper o2) {
               return Float.compare(o2.tz, o1.tz);
            }
         });
      }

      PriorityQueue<PbCarvingCell.PbCellVertWrapper> leftTurnPointQueue = this.turnPointQueueLeft;
      leftTurnPointQueue.clear();
      if (this.turnPointQueueRight == null) {
         this.turnPointQueueRight = new PriorityQueue(100, new Comparator<PbCarvingCell.PbCellVertWrapper>() {
            public int compare(PbCarvingCell.PbCellVertWrapper o1, PbCarvingCell.PbCellVertWrapper o2) {
               return Float.compare(o1.tz, o2.tz);
            }
         });
      }

      PriorityQueue<PbCarvingCell.PbCellVertWrapper> rightTurnPointQueue = this.turnPointQueueRight;
      rightTurnPointQueue.clear();
      PbCarvingCell.PbCellFace outerFace = faceToSplit.getActualFaceFirst();
      if (outerFace != null && !outerFace.isDecimated) {
         this.setUpPlanerMatrix(pm, pmInv, outerFace);

         for(PbCarvingCell.PbCellFace holeFaces = outerFace; holeFaces != null; holeFaces = holeFaces.nextSubLinkRef) {
            if (!holeFaces.isDecimated) {
               faceWork.add(holeFaces);
            }
         }

         if (faceWork.size() == 1) {
            outerFace.nX = faceToSplit.nX;
            outerFace.nY = faceToSplit.nY;
            outerFace.nZ = faceToSplit.nZ;
            outerFace.groupNum = faceToSplit.groupNum;
            splittedHoleFaceWork.add(outerFace);
         } else {
            int holeF;
            for(holeF = 0; holeF < faceWork.size(); ++holeF) {
               faceWork.set(holeF, this.wrapVertices(pm, (PbCarvingCell.PbCellFace)faceWork.get(holeF)));
            }

            PbCarvingCell.PbCellFace holeFace;
            for(holeF = 1; holeF < faceWork.size(); ++holeF) {
               holeFace = (PbCarvingCell.PbCellFace)faceWork.get(holeF);
               if (!holeFace.isDecimated && holeFace.isSubloop) {
                  float xMin = Float.MAX_VALUE;
                  float zMin = Float.MAX_VALUE;
                  float xMax = -3.4028235E38F;
                  float zMax = -1.4E-45F;

                  for(int fv = 0; fv < holeFace.vertIdxCnt; ++fv) {
                     PbCarvingCell.PbCellVert vert = holeFace.vertices[fv];
                     xMin = Math.min(xMin, vert.tx);
                     zMin = Math.min(zMin, vert.tz);
                     xMax = Math.max(xMax, vert.tx);
                     zMax = Math.max(zMax, vert.tz);
                  }

                  if (xMin != xMax && zMin != zMax) {
                     float xCenter = (xMax + xMin) / 2.0F;
                     leftFaceEdgeList.clear();
                     rightFaceEdgeList.clear();

                     int fv;
                     int f;
                     label188:
                     for(f = 0; f < faceWork.size(); ++f) {
                        PbCarvingCell.PbCellFace face = (PbCarvingCell.PbCellFace)faceWork.get(f);
                        if (!face.isDecimated) {
                           leftFaceEdgeListLocal.clear();
                           rightFaceEdgeListLocal.clear();
                           boolean oneOfEdgeJoinedLeft = false;
                           boolean oneOfEdgeJoinedRight = false;
                           int e = 0;

                           for(fv = face.vertIdxCnt; e < fv; ++e) {
                              PbCarvingCell.PbCellVertWrapper vertNow = (PbCarvingCell.PbCellVertWrapper)face.vertices[e];
                              PbCarvingCell.PbCellVertWrapper vertNext = (PbCarvingCell.PbCellVertWrapper)face.vertices[e == fv - 1 ? 0 : e + 1];
                              PbCarvingCell.PbCellVertWrapper leftVert;
                              PbCarvingCell.PbCellVertWrapper rightVert;
                              if (vertNow.tx < vertNext.tx) {
                                 leftVert = vertNow;
                                 rightVert = vertNext;
                              } else {
                                 leftVert = vertNext;
                                 rightVert = vertNow;
                              }

                              boolean isLeftNearEqualLine = nEquals(leftVert.tx, xCenter);
                              boolean isRightNearEqualLine = nEquals(rightVert.tx, xCenter);
                              if (isLeftNearEqualLine && isRightNearEqualLine) {
                                 if (nEquals(vertNow.tz, vertNext.tz)) {
                                    continue label188;
                                 }

                                 boolean isLineUpToDown = vertNow.tz > vertNext.tz;
                                 if (f != 0) {
                                    if (isLineUpToDown) {
                                       oneOfEdgeJoinedLeft = true;
                                    } else {
                                       oneOfEdgeJoinedRight = true;
                                    }
                                 } else if (isLineUpToDown) {
                                    oneOfEdgeJoinedLeft = true;
                                 } else {
                                    oneOfEdgeJoinedRight = true;
                                 }
                              } else {
                                 if (isLeftNearEqualLine) {
                                    PbCarvingCell.PbCellVertWrapper vertCP = new PbCarvingCell.PbCellVertWrapper(leftVert);
                                    if (leftVert == vertNow) {
                                       rightFaceEdgeListLocal.add(vertCP.connectFromBack = new SplittedEdge(vertCP, rightVert));
                                       vertCP.twardsLeft = false;
                                    } else {
                                       rightFaceEdgeListLocal.add(new SplittedEdge(rightVert, vertCP));
                                       vertCP.twardsLeft = true;
                                    }

                                    vertCP.isTurningPoint = true;
                                 } else if (isRightNearEqualLine) {
                                    PbCarvingCell.PbCellVertWrapper vertCP = new PbCarvingCell.PbCellVertWrapper(rightVert);
                                    if (rightVert == vertNow) {
                                       leftFaceEdgeListLocal.add(vertCP.connectFromBack = new SplittedEdge(vertCP, leftVert));
                                       vertCP.twardsLeft = true;
                                    } else {
                                       leftFaceEdgeListLocal.add(new SplittedEdge(leftVert, vertCP));
                                       vertCP.twardsLeft = false;
                                    }

                                    vertCP.isTurningPoint = true;
                                 } else if (leftVert.tx < xCenter) {
                                    if (rightVert.tx > xCenter) {
                                       float crossPointZ = leftVert.tz + (rightVert.tz - leftVert.tz) * ((xCenter - leftVert.tx) / (rightVert.tx - leftVert.tx));
                                       PbCarvingCell.PbCellVertWrapper vertCP = new PbCarvingCell.PbCellVertWrapper(xCenter, vertNow.ty, crossPointZ);
                                       if (leftVert == vertNow) {
                                          leftFaceEdgeListLocal.add(new SplittedEdge(leftVert, vertCP));
                                          rightFaceEdgeListLocal.add(vertCP.connectFromBack = new SplittedEdge(vertCP, rightVert));
                                          vertCP.twardsLeft = false;
                                       } else {
                                          leftFaceEdgeListLocal.add(vertCP.connectFromBack = new SplittedEdge(vertCP, leftVert));
                                          rightFaceEdgeListLocal.add(new SplittedEdge(rightVert, vertCP));
                                          vertCP.twardsLeft = true;
                                       }

                                       vertCP.isTurningPoint = true;
                                    } else {
                                       leftFaceEdgeListLocal.add(new SplittedEdge(vertNow, vertNext));
                                    }
                                 } else {
                                    rightFaceEdgeListLocal.add(new SplittedEdge(vertNow, vertNext));
                                 }
                              }
                           }

                           oneOfEdgeJoinedLeft |= leftFaceEdgeListLocal.size() != 0;
                           oneOfEdgeJoinedRight |= rightFaceEdgeListLocal.size() != 0;
                           if (!oneOfEdgeJoinedLeft && !oneOfEdgeJoinedRight) {
                              throw new RuntimeException("can't happen! no edge joined to new face!");
                           }

                           if (oneOfEdgeJoinedRight && oneOfEdgeJoinedLeft) {
                              face.isDecimated = true;
                              this.transferEachLocalEdge(leftFaceEdgeList, leftFaceEdgeListLocal, leftTurnPointQueue);
                              this.transferEachLocalEdge(rightFaceEdgeList, rightFaceEdgeListLocal, rightTurnPointQueue);
                           }
                        }
                     }

                     f = leftFaceEdgeList.size();
                     int rightSideEdgeCount = rightFaceEdgeList.size();
                     if (f == 0 || rightSideEdgeCount == 0) {
                        throw new RuntimeException("can't happen! must have splitted two face!!!");
                     }

                     this.createTurnPointEdges(vertWork, leftFaceEdgeListLocal, leftTurnPointQueue);
                     this.createTurnPointEdges(vertWork, rightFaceEdgeListLocal, rightTurnPointQueue);
                     f += leftFaceEdgeListLocal.size();
                     rightSideEdgeCount += rightFaceEdgeListLocal.size();
                     int savedIndex = faceWork.size();
                     this.createLeftRightSplittedFace(f, leftFaceEdgeList, faceWork);
                     this.createLeftRightSplittedFace(rightSideEdgeCount, rightFaceEdgeList, faceWork);

                     for(int ffix = savedIndex; ffix < faceWork.size(); ++ffix) {
                        PbCarvingCell.PbCellFace faceFix = (PbCarvingCell.PbCellFace)faceWork.get(ffix);

                        for(fv = 0; fv < faceFix.vertIdxCnt; ++fv) {
                           ((PbCarvingCell.PbCellVertWrapper)faceFix.vertices[fv]).isTurningPoint = false;
                        }
                     }
                  } else {
                     holeFace.isDecimated = true;
                  }
               }
            }

            for(holeF = 0; holeF < faceWork.size(); ++holeF) {
               holeFace = (PbCarvingCell.PbCellFace)faceWork.get(holeF);
               this.resumeFromWrapper(holeFace, pmInv);
               if (!holeFace.isDecimated && !holeFace.isSubloop) {
                  holeFace.nX = faceToSplit.nX;
                  holeFace.nY = faceToSplit.nY;
                  holeFace.nZ = faceToSplit.nZ;
                  holeFace.groupNum = faceToSplit.groupNum;
                  splittedHoleFaceWork.add(holeFace);
               }
            }

         }
      }
   }

   private PbCarvingCell.PbCellFace wrapVertices(Matrix3f pm, PbCarvingCell.PbCellFace face) {
      for(int fv = 0; fv < face.vertIdxCnt; ++fv) {
         PbCarvingCell.PbCellVertWrapper vWrap = new PbCarvingCell.PbCellVertWrapper(face.vertices[fv]);
         this.mulVertWithMatrix(pm, vWrap);
         face.vertices[fv] = vWrap;
      }

      return face;
   }

   private void resumeFromWrapper(PbCarvingCell.PbCellFace resultFace, Matrix3f pmInv) {
      for(int fv = 0; fv < resultFace.vertIdxCnt; ++fv) {
         if (resultFace.vertices[fv] instanceof PbCarvingCell.PbCellVertWrapper) {
            PbCarvingCell.PbCellVertWrapper vWrap = (PbCarvingCell.PbCellVertWrapper)resultFace.vertices[fv];
            PbCarvingCell.PbCellVert refRef;
            if (vWrap.ref instanceof PbCarvingCell.PbCellVertWrapper) {
               for(refRef = vWrap.ref; refRef != null && refRef instanceof PbCarvingCell.PbCellVertWrapper; refRef = ((PbCarvingCell.PbCellVertWrapper)refRef).ref) {
               }

               vWrap.ref = refRef;
            }

            if (vWrap.ref == null && pmInv != null) {
               refRef = new PbCarvingCell.PbCellVert(vWrap.tx, vWrap.ty, vWrap.tz);
               this.mulVertWithMatrix(pmInv, refRef);
               this.vertices.add(refRef);
               vWrap.ref = refRef;
            }

            resultFace.vertices[fv] = vWrap.ref;
         }
      }

   }

   private void createTurnPointEdges(ArrayList<PbCarvingCell.PbCellVert> vertWork, ArrayList<SplittedEdge> sideEdgeListLocal, PriorityQueue<PbCarvingCell.PbCellVertWrapper> sideTurnPointQueue) {
      vertWork.clear();

      while(!sideTurnPointQueue.isEmpty()) {
         vertWork.add(sideTurnPointQueue.poll());
      }

      if ((vertWork.size() & 1) != 0) {
         throw new RuntimeException("can't happen! turn point must not be odd number!!");
      } else {
         sideEdgeListLocal.clear();
         int cv = 0;
         int len = vertWork.size();

         for(int lenSub1 = len - 1; cv < len; cv += 2) {
            PbCarvingCell.PbCellVertWrapper vwNow = (PbCarvingCell.PbCellVertWrapper)vertWork.get(cv);
            PbCarvingCell.PbCellVertWrapper vwNxt = (PbCarvingCell.PbCellVertWrapper)vertWork.get(cv + 1);
            if (vwNow.twardsLeft == vwNxt.twardsLeft) {
               throw new RuntimeException("can't happen! turn point is invalid!!");
            }

            SplittedEdge tpEdge = new SplittedEdge(vwNow, vwNxt);
            vwNow.connectFront = tpEdge;
            tpEdge.setNextEdge(vwNxt.connectFromBack);
            sideEdgeListLocal.add(tpEdge);
         }

      }
   }

   private void createLeftRightSplittedFace(int edgeCnt, ArrayList<SplittedEdge> sideEdgeList, ArrayList<PbCarvingCell.PbCellFace> holeFaceWork) {
      ArrayList<PbCarvingCell.PbCellVert> holeVertWork = this.mergeHoleVertWork;
      holeVertWork.clear();
      int sideListSearchIndex = 0;
      int sideEdgeListLen = sideEdgeList.size();
      SplittedEdge edge = null;
      ArrayList<SplittedEdge> debugEdgeList = this.debugEdgeList;
      debugEdgeList.clear();

      while(true) {
         while(edgeCnt > 0) {
            if (edge == null) {
               for(int i = sideListSearchIndex; i < sideEdgeListLen; ++i) {
                  SplittedEdge edgeTemp = (SplittedEdge)sideEdgeList.get(i);
                  sideListSearchIndex = i;
                  if (!edgeTemp.isVisited) {
                     edge = (SplittedEdge)sideEdgeList.get(i);
                     break;
                  }
               }

               if (edge == null) {
                  throw new RuntimeException("can't happen! turn point index exceed!!");
               }
            }

            if (edge.isVisited) {
               if (holeVertWork.size() > 0) {
                  holeFaceWork.add(this.createNewSingleFace(holeVertWork, false));
                  holeVertWork.clear();
               }

               edge = null;
            } else {
               holeVertWork.add(edge.vertFrom);
               debugEdgeList.add(edge);
               edge.isVisited = true;
               --edgeCnt;
               if (!edge.vertFrom.isTurningPoint && edge.vertTo.isTurningPoint) {
                  edge = edge.vertTo.connectFront;
               } else {
                  edge = edge.nextEdge;
               }

               if (edge == null) {
                  throw new RuntimeException("can't happen! new edge is not connected!!");
               }
            }
         }

         if (holeVertWork.size() > 0) {
            holeFaceWork.add(this.createNewSingleFace(holeVertWork, false));
         }

         return;
      }
   }

   private void transferEachLocalEdge(ArrayList<SplittedEdge> sideEdgeList, ArrayList<SplittedEdge> sideEdgeListLocal, PriorityQueue<PbCarvingCell.PbCellVertWrapper> sideTurnPointQueue) {
      int v = 0;
      int len = sideEdgeListLocal.size();

      for(int lenSub1 = len - 1; v < len; ++v) {
         SplittedEdge seNow = (SplittedEdge)sideEdgeListLocal.get(v);
         SplittedEdge seNxt = (SplittedEdge)sideEdgeListLocal.get(v == lenSub1 ? 0 : v + 1);
         if (!seNow.vertTo.isTurningPoint) {
            seNow.setNextEdge(seNxt);
         }

         sideEdgeList.add(seNow);
         if (seNow.vertFrom.isTurningPoint) {
            sideTurnPointQueue.add(seNow.vertFrom);
         } else if (seNow.vertTo.isTurningPoint) {
            sideTurnPointQueue.add(seNow.vertTo);
         }
      }

   }

   private void mulVertWithMatrix(Matrix3f pm, PbCarvingCell.PbCellVert vWrap) {
      float x = pm.m00() * vWrap.tx + pm.m01() * vWrap.ty + pm.m02() * vWrap.tz;
      float y = pm.m10() * vWrap.tx + pm.m11() * vWrap.ty + pm.m12() * vWrap.tz;
      float z = pm.m20() * vWrap.tx + pm.m21() * vWrap.ty + pm.m22() * vWrap.tz;
      vWrap.tx = x;
      vWrap.ty = y;
      vWrap.tz = z;
   }

   private void setUpPlanerMatrix(Matrix3f pm, Matrix3f pmInv, PbCarvingCell.PbCellFace outerFace) {
      float vecNP_x = outerFace.nX;
      float vecNP_y = outerFace.nY;
      float vecNP_z = outerFace.nZ;
      if (this.vecDistanceTo(VECTILT, vecNP_x, vecNP_y, vecNP_z) < 1.0E-4F) {
         pm.identity();
      } else if (this.vecDistanceTo(VECTILTINV, vecNP_x, vecNP_y, vecNP_z) < 1.0E-4F) {
         pm.identity();
         pm.rotateX(3.1415927F);
      } else {
         pm.m10(vecNP_x);
         pm.m11(vecNP_y);
         pm.m12(vecNP_z);
         float vecCP_x = vecNP_y * VECTILT.tz - vecNP_z * VECTILT.ty;
         float vecCP_y = vecNP_z * VECTILT.tx - vecNP_x * VECTILT.tz;
         float vecCP_z = vecNP_x * VECTILT.ty - vecNP_y * VECTILT.tx;
         pm.m00(vecCP_x);
         pm.m01(vecCP_y);
         pm.m02(vecCP_z);
         pm.m20(vecCP_y * vecNP_z - vecCP_z * vecNP_y);
         pm.m21(vecCP_z * vecNP_x - vecCP_x * vecNP_z);
         pm.m22(vecCP_x * vecNP_y - vecCP_y * vecNP_x);
      }

      pm.invert(pmInv);
   }

   public float vecDistanceTo(PbCarvingCell.PbCellVert vec, float bX, float bY, float bZ) {
      float d0 = bX - vec.tx;
      float d1 = bY - vec.ty;
      float d2 = bZ - vec.tz;
      return Mth.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   private boolean isStraightOrLapelLine(PbCarvingCell.PbCellVert vertPrev, PbCarvingCell.PbCellVert vert, PbCarvingCell.PbCellVert vertNext) {
      float aXt = vertPrev.tx - vert.tx;
      float aYt = vertPrev.ty - vert.ty;
      float aZt = vertPrev.tz - vert.tz;
      float mgn = Mth.sqrt(aXt * aXt + aYt * aYt + aZt * aZt);
      if (mgn == 0.0F) {
         return true;
      } else {
         aXt /= mgn;
         aYt /= mgn;
         aZt /= mgn;
         float bXt = vertNext.tx - vert.tx;
         float bYt = vertNext.ty - vert.ty;
         float bZt = vertNext.tz - vert.tz;
         mgn = Mth.sqrt(bXt * bXt + bYt * bYt + bZt * bZt);
         if (mgn < 1.0E-4F) {
            return true;
         } else {
            bXt /= mgn;
            bYt /= mgn;
            bZt /= mgn;
            float dot = aXt * bXt + aYt * bYt + aZt * bZt;
            return dot < -0.9999F || dot > 0.9999F;
         }
      }
   }

   private void triangulateSingleFace(PbCarvingCell.PbCellFace face, ArrayList<PbCarvingCell.PbCellFace> triangleFaceWork) {
      int remainVCnt = face.vertIdxCnt;
      this.vertJointIdxA = face.setAndClearEdgeFlagWork(this.vertJointIdxA, true);

      while(remainVCnt > 3) {
         int lenSub1 = remainVCnt - 1;
         int earIdx = -1;
         float maxAngleDiff = -3.4028235E38F;

         int v;
         int earPrevIdx;
         int earNextIdx;
         for(v = 0; v < remainVCnt; ++v) {
            if (face.vertJointIdx[v] != 1) {
               earPrevIdx = v == 0 ? lenSub1 : v - 1;
               earNextIdx = v == lenSub1 ? 0 : v + 1;
               PbCarvingCell.PbCellVert eV1 = face.vertices[earPrevIdx];
               PbCarvingCell.PbCellVert eV2 = face.vertices[v];
               PbCarvingCell.PbCellVert eV3 = face.vertices[earNextIdx];
               float minAngleDiffWithOther = Float.MAX_VALUE;

               for(int vt = 2; vt < lenSub1; ++vt) {
                  int vtCurrIdx = vt + v;
                  if (vtCurrIdx >= remainVCnt) {
                     vtCurrIdx -= remainVCnt;
                  }

                  float angleDiff = this.angleVertInTris(eV1, eV2, eV3, face.vertices[vtCurrIdx]);
                  if (angleDiff < minAngleDiffWithOther) {
                     minAngleDiffWithOther = angleDiff;
                  }
               }

               if (minAngleDiffWithOther > maxAngleDiff) {
                  maxAngleDiff = minAngleDiffWithOther;
                  earIdx = v;
               }
            }
         }

         if (earIdx == -1) {
            this.removeFaceStraightLines(face);
            if (remainVCnt == face.vertIdxCnt) {
               JointBlockRemastered.LOGGER.debug("[JointBlock:ObjOut]triangulation failed. no ear.");
               return;
            }

            remainVCnt = face.vertIdxCnt;
         } else {
            earPrevIdx = earIdx == 0 ? lenSub1 : earIdx - 1;
            earNextIdx = earIdx == lenSub1 ? 0 : earIdx + 1;
            triangleFaceWork.add(this.createNewTriangle(face, earPrevIdx, earIdx, earNextIdx));
            remainVCnt = face.removeIndexVert(earIdx);
            v = earIdx == 0 ? remainVCnt - 1 : earIdx - 1;
            int fixedNext = earIdx == remainVCnt ? 0 : earIdx;
            face.vertJointIdx[v] = face.calculateVertType(v);
            face.vertJointIdx[fixedNext] = face.calculateVertType(fixedNext);
         }
      }

      if (remainVCnt == 3) {
         triangleFaceWork.add(this.createNewTriangle(face, 0, 1, 2));
      }

   }

   public float angleVertInTris(PbCarvingCell.PbCellVert p1, PbCarvingCell.PbCellVert p2, PbCarvingCell.PbCellVert p3, PbCarvingCell.PbCellVert targetPoint) {
      this.intrisA = PbCarvingCell.PbCellVert.setSubtract(targetPoint, p1, this.intrisA).normalize();
      this.intrisB = PbCarvingCell.PbCellVert.setSubtract(targetPoint, p2, this.intrisB).normalize();
      this.intrisC = PbCarvingCell.PbCellVert.setSubtract(targetPoint, p3, this.intrisC).normalize();
      float angles = 0.0F;
      angles = (float)((double)angles + Math.acos(Mth.clamp((double)PbCarvingCell.PbCellVert.dot(this.intrisA, this.intrisB), -1.0, 1.0)));
      angles = (float)((double)angles + Math.acos(Mth.clamp((double)PbCarvingCell.PbCellVert.dot(this.intrisB, this.intrisC), -1.0, 1.0)));
      angles = (float)((double)angles + Math.acos(Mth.clamp((double)PbCarvingCell.PbCellVert.dot(this.intrisC, this.intrisA), -1.0, 1.0)));
      return Mth.abs(angles - 6.2831855F);
   }

   private PbCarvingCell.PbCellFace createNewTriangle(PbCarvingCell.PbCellFace face, int prevIdx, int centIdx, int nextIdx) {
      PbCarvingCell.PbCellFace tris = new PbCarvingCell.PbCellFace(3, false);
      tris.vertIdxCnt = 3;
      tris.vertices[0] = face.vertices[prevIdx];
      tris.vertices[1] = face.vertices[centIdx];
      tris.vertices[2] = face.vertices[nextIdx];
      return tris;
   }

   public void addMessage(String message, boolean isError, Object... params) {
      this.resultMessageStr = message;
      this.isResultMessageError = isError;
      if (params != null && params.length > 0) {
         this.resultMessageParams = params;
      } else {
         this.resultMessageParams = null;
      }

   }

   public void processPlayerMessage() {
      if (this.resultMessageParams != null) {
         if (this.isResultMessageError) {
            this.player.displayClientMessage(JBCommonUtil.makeErrorChat(this.resultMessageStr, this.resultMessageParams), false);
         } else {
            this.player.displayClientMessage(JBCommonUtil.makeNormalChat(this.resultMessageStr, this.resultMessageParams), false);
         }
      } else if (this.isResultMessageError) {
         this.player.displayClientMessage(JBCommonUtil.makeErrorChat(this.resultMessageStr), false);
      } else {
         this.player.displayClientMessage(JBCommonUtil.makeNormalChat(this.resultMessageStr), false);
      }

   }

   public static boolean nEquals(float a, float b) {
      return Math.abs(a - b) < 1.0E-4F;
   }

   public static class SplittedEdge {
      public PbCarvingCell.PbCellVertWrapper vertFrom;
      public PbCarvingCell.PbCellVertWrapper vertTo;
      public SplittedEdge nextEdge;
      public boolean isVisited = false;

      public SplittedEdge(PbCarvingCell.PbCellVertWrapper argFrom, PbCarvingCell.PbCellVertWrapper argTo) {
         this.vertFrom = argFrom;
         this.vertTo = argTo;
      }

      public void setNextEdge(SplittedEdge argNext) {
         this.nextEdge = argNext;
      }
   }
}
