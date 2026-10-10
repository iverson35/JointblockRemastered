package jp.mc.ancientred.jointblock.carving.prebuild;

import java.util.ArrayList;
import java.util.LinkedList;
import net.minecraft.util.Mth;

public class PbCarvingCell {
   public static final float FLOAT_EPSILON = 1.0E-4F;
   private static final float[][][] FACES_MATCH = new float[][][]{{{0.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 1.0F}, {0.0F, 0.0F, 1.0F}}, {{0.0F, 1.0F, 0.0F}, {1.0F, 1.0F, 0.0F}, {1.0F, 1.0F, 1.0F}, {0.0F, 1.0F, 1.0F}}, {{0.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {1.0F, 1.0F, 0.0F}, {1.0F, 0.0F, 0.0F}}, {{0.0F, 0.0F, 1.0F}, {0.0F, 1.0F, 1.0F}, {1.0F, 1.0F, 1.0F}, {1.0F, 0.0F, 1.0F}}, {{0.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 1.0F}, {0.0F, 0.0F, 1.0F}}, {{1.0F, 0.0F, 0.0F}, {1.0F, 1.0F, 0.0F}, {1.0F, 1.0F, 1.0F}, {1.0F, 0.0F, 1.0F}}};
   private static final int[] DIAGONAL = new int[]{2, 3, 0, 1};
   private static final int EMPTY = -1;
   private static final int FILLED = -2;
   public byte collectingVertCount = 0;
   public PbCellVert collectingVertFirst;
   public PbCellVert collectingVertEnd;
   public PbCellFace faceFirst;
   public PbCellFace faceEnd;
   public byte filledStates = 0;
   public int colorIdx;
   public byte x;
   public byte y;
   public byte z;

   public PbCarvingCell(int argX, int argY, int argZ) {
      this.x = (byte)argX;
      this.y = (byte)argY;
      this.z = (byte)argZ;
   }

   public void addNewCollectingVert(PbCellVert newVert) {
      newVert.nextVertRef = null;
      if (this.collectingVertFirst == null) {
         this.collectingVertFirst = newVert;
      }

      if (this.collectingVertEnd != null) {
         this.collectingVertEnd.nextVertRef = newVert;
      }

      this.collectingVertEnd = newVert;
      ++this.collectingVertCount;
   }

   public void commitNewFace(Prebuilder prebuilder, PbCellFace newFace) {
      newFace.nextFaceRef = null;
      if (this.faceFirst == null) {
         this.faceFirst = newFace;
      }

      if (this.faceEnd != null) {
         this.faceEnd.nextFaceRef = newFace;
      }

      this.faceEnd = newFace;

      PbCellVert nextVert;
      for(PbCellVert targetVert = this.collectingVertFirst; targetVert != null; targetVert = nextVert) {
         nextVert = targetVert.nextVertRef;
         targetVert.nextVertRef = null;
         newFace.addVert(targetVert, true);
      }

      this.collectingVertFirst = null;
      this.collectingVertEnd = null;
      this.collectingVertCount = 0;
   }

   public void setAllFilled() {
      this.filledStates = 63;
   }

   public void calclulateFilled(Prebuilder prebuilder, double dDivCountUnit, double dMinX, double dMinY, double dMinZ) {
      float divCountUnit = (float)dDivCountUnit;
      float minX = (float)dMinX;
      float minY = (float)dMinY;
      float minZ = (float)dMinZ;
      int[] fillMatchWork = prebuilder.fillMatchWork;

      for(int f = 0; f < 6; ++f) {
         fillMatchWork[f] = -1;
      }

      for(PbCellFace targetFace = this.faceFirst; targetFace != null; targetFace = targetFace.nextFaceRef) {
         int sideTurned;
         PbCellVert targetVert;
         for(int side = 0; side < 6; ++side) {
            int matchCnt = 0;
            int unmatchedVertIdx = -1;

            for(sideTurned = 0; sideTurned < 4; ++sideTurned) {
               boolean hasMatch = false;

               for(int fv = 0; fv < targetFace.vertIdxCnt; ++fv) {
                  targetVert = targetFace.vertices[fv];
                  if (Prebuilder.nEquals(targetVert.inBoxX, minX + FACES_MATCH[side][sideTurned][0] * divCountUnit) && Prebuilder.nEquals(targetVert.inBoxY, minY + FACES_MATCH[side][sideTurned][1] * divCountUnit) && Prebuilder.nEquals(targetVert.inBoxZ, minZ + FACES_MATCH[side][sideTurned][2] * divCountUnit)) {
                     ++matchCnt;
                     hasMatch = true;
                  }
               }

               if (!hasMatch) {
                  unmatchedVertIdx = sideTurned;
               }
            }

            if (matchCnt == 3) {
               sideTurned = prebuilder.turnSideByPartsFace(side);
               targetFace.sideIdForDelim = sideTurned;
               if (fillMatchWork[sideTurned] == -1) {
                  fillMatchWork[sideTurned] = unmatchedVertIdx;
               } else if (fillMatchWork[sideTurned] != -2 && DIAGONAL[fillMatchWork[sideTurned]] == unmatchedVertIdx) {
                  fillMatchWork[sideTurned] = -2;
               }
               break;
            }
         }

         if (targetFace.sideIdForDelim == -1) {
            boolean xSame = true;
            boolean ySame = true;
            boolean zSame = true;
            sideTurned = 0;

            for(int len = targetFace.vertIdxCnt - 1; sideTurned < len; ++sideTurned) {
               PbCellVert vA = targetFace.vertices[sideTurned];
               targetVert = targetFace.vertices[sideTurned + 1];
               xSame &= vA.inBoxX == targetVert.inBoxX;
               ySame &= vA.inBoxY == targetVert.inBoxY;
               zSame &= vA.inBoxZ == targetVert.inBoxZ;
            }

            PbCellVert vSamp = targetFace.vertices[0];
            if (xSame) {
               if (Prebuilder.nEquals(vSamp.inBoxX, minX)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(4);
               } else if (Prebuilder.nEquals(vSamp.inBoxX, minX + divCountUnit)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(5);
               }
            } else if (ySame) {
               if (Prebuilder.nEquals(vSamp.inBoxY, minY)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(0);
               } else if (Prebuilder.nEquals(vSamp.inBoxY, minY + divCountUnit)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(1);
               }
            } else if (zSame) {
               if (Prebuilder.nEquals(vSamp.inBoxZ, minZ)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(2);
               } else if (Prebuilder.nEquals(vSamp.inBoxZ, minZ + divCountUnit)) {
                  targetFace.sideIdForDelim = prebuilder.turnSideByPartsFace(3);
               }
            }
         }
      }

      if (fillMatchWork[0] == -2) {
         this.setYNegFilled();
      }

      if (fillMatchWork[1] == -2) {
         this.setYPosFilled();
      }

      if (fillMatchWork[2] == -2) {
         this.setZNegFilled();
      }

      if (fillMatchWork[3] == -2) {
         this.setZPosFilled();
      }

      if (fillMatchWork[4] == -2) {
         this.setXNegFilled();
      }

      if (fillMatchWork[5] == -2) {
         this.setXPosFilled();
      }

   }

   public boolean isYNegFilled() {
      return (this.filledStates & 1) != 0;
   }

   public boolean isYPosFilled() {
      return (this.filledStates & 2) != 0;
   }

   public boolean isZNegFilled() {
      return (this.filledStates & 4) != 0;
   }

   public boolean isZPosFilled() {
      return (this.filledStates & 8) != 0;
   }

   public boolean isXNegFilled() {
      return (this.filledStates & 16) != 0;
   }

   public boolean isXPosFilled() {
      return (this.filledStates & 32) != 0;
   }

   public void setYNegFilled() {
      this.filledStates = (byte)(this.filledStates | 1);
   }

   public void setYPosFilled() {
      this.filledStates = (byte)(this.filledStates | 2);
   }

   public void setZNegFilled() {
      this.filledStates = (byte)(this.filledStates | 4);
   }

   public void setZPosFilled() {
      this.filledStates = (byte)(this.filledStates | 8);
   }

   public void setXNegFilled() {
      this.filledStates = (byte)(this.filledStates | 16);
   }

   public void setXPosFilled() {
      this.filledStates = (byte)(this.filledStates | 32);
   }

   public void decimateYNeg() {
      this.decimateMatchedFace(0);
   }

   public void decimateYPos() {
      this.decimateMatchedFace(1);
   }

   public void decimateZNeg() {
      this.decimateMatchedFace(2);
   }

   public void decimateZPos() {
      this.decimateMatchedFace(3);
   }

   public void decimateXNeg() {
      this.decimateMatchedFace(4);
   }

   public void decimateXPos() {
      this.decimateMatchedFace(5);
   }

   private void decimateMatchedFace(int faceIdx) {
      for(PbCellFace targetFace = this.faceFirst; targetFace != null; targetFace = targetFace.nextFaceRef) {
         targetFace.isDecimated |= targetFace.sideIdForDelim == faceIdx;
      }

   }

   public static class PbBoxMatrix {
      public byte divCount;
      public byte palletIdx;
      public PbCarvingCell[] cellMatrix;
      public PbBoxMatrix[] neighBourBoxMatrixes;

      public PbCarvingCell[] createMatrix(int len) {
         return this.cellMatrix = new PbCarvingCell[len];
      }
   }

   public static class PbWFObjGroup {
      int matIdx;
      int palIdx;
      int colIdx;
      public PbCellFace faceFirst;
      public PbCellFace faceEnd;

      public PbWFObjGroup(int argMatIdx, int argPalIdx, int argColIdx, int argFaceNum) {
         this.matIdx = argMatIdx;
         this.palIdx = argPalIdx;
         this.colIdx = argColIdx;
      }

      public void addFace(PbCellFace newFace) {
         newFace.nextFaceRef = null;
         if (this.faceFirst == null) {
            this.faceFirst = newFace;
         }

         if (this.faceEnd != null) {
            this.faceEnd.nextFaceRef = newFace;
         }

         this.faceEnd = newFace;
      }

      public void clearFaceLink() {
         this.faceFirst = null;
         this.faceEnd = null;
      }
   }

   public static class PbWFMaterial {
      int matIdx;
      int palIdx;
      int colIdx;
      int colorR;
      int colorG;
      int colorB;
      int colorA;

      public PbWFMaterial(int argMatIdx, int argPalIdx, int argColIdx) {
         this.matIdx = argMatIdx;
         this.palIdx = argPalIdx;
         this.colIdx = argColIdx;
      }
   }

   public static class PbCellFaceWithSubs extends PbCellFace {
      public PbCellFace holeLinkRefFirst;
      public PbCellFace holeLinkRefEnd;

      public PbCellFaceWithSubs() {
         super(0);
      }

      public PbCellFace getActualFaceFirst() {
         return this.holeLinkRefFirst;
      }

      public void addSubFace(PbCellFace sub) {
         sub.isSubloop = true;
         sub.nextSubLinkRef = null;
         if (this.holeLinkRefFirst == null) {
            this.holeLinkRefFirst = sub;
         }

         if (this.holeLinkRefEnd != null) {
            this.holeLinkRefEnd.nextSubLinkRef = sub;
         }

         this.holeLinkRefEnd = sub;
      }
   }

   public static class PbCellFace {
      public static final int CONVEX = 0;
      public static final int CONCAVE = 1;
      public static final int COLLINEAR = 2;
      public boolean isDecimated;
      public boolean isSubloop;
      public int groupNum;
      public int vertIdxCnt;
      public PbCellVert[] vertices;
      public PbCellEdge[] edges;
      public int[] vertJointIdx;
      public int sideIdForDelim;
      public float nX;
      public float nY;
      public float nZ;
      public PbCellFace nextFaceRef;
      public PbCellFace nextSubLinkRef;
      public boolean hadError;

      public PbCellFace(int argVertCnt) {
         this(argVertCnt, true);
      }

      public PbCellFace(int argVertCnt, boolean genEdgeList) {
         this.isDecimated = false;
         this.isSubloop = false;
         this.groupNum = 0;
         this.vertIdxCnt = 0;
         this.sideIdForDelim = -1;
         if (argVertCnt != 0) {
            this.vertices = new PbCellVert[argVertCnt];
            if (genEdgeList) {
               this.edges = new PbCellEdge[argVertCnt];
            }
         }

      }

      public PbCellFace getActualFaceFirst() {
         return this;
      }

      public void addEdge(int refferedVertIdx, PbCellEdge newEdge, PbCellFace faceBody) {
         this.edges[refferedVertIdx] = newEdge;
         newEdge.holderFaces.add(faceBody);
      }

      public void addVert(PbCellVert newVert, boolean addFaceRef) {
         this.vertices[this.vertIdxCnt++] = newVert;
         if (addFaceRef) {
            newVert.holderFaces.addLast(this);
         }

      }

      public PbCellFace calcNormalNewell() {
         float x = 0.0F;
         float y = 0.0F;
         float z = 0.0F;

         for(int i = 0; i < this.vertIdxCnt; ++i) {
            PbCellVert v = this.vertices[i];
            PbCellVert vN = this.vertices[i == this.vertIdxCnt - 1 ? 0 : i + 1];
            if (v != vN) {
               x += (v.ty - vN.ty) * (v.tz + vN.tz);
               y += (v.tz - vN.tz) * (v.tx + vN.tx);
               z += (v.tx - vN.tx) * (v.ty + vN.ty);
            }
         }

         float mag = Mth.sqrt(x * x + y * y + z * z);
         if (mag > 1.0E-4F) {
            this.nX = x / mag;
            this.nY = y / mag;
            this.nZ = z / mag;
         } else {
            this.nX = this.nY = this.nZ = 0.0F;
         }

         return this;
      }

      public static boolean checkNormalDot(PbCellFace faceA, PbCellFace faceB, boolean isOppoSite) {
         float dot = faceA.nX * faceB.nX + faceA.nY * faceB.nY + faceA.nZ * faceB.nZ;
         if (isOppoSite) {
            return dot < -0.9999F;
         } else {
            return dot > 0.9999F;
         }
      }

      public int getVertIndexOf(PbCellVert targetVert) {
         if (targetVert == null) {
            return -1;
         } else {
            for(int i = 0; i < this.vertIdxCnt; ++i) {
               if (this.vertices[i] == targetVert) {
                  return i;
               }
            }

            return -1;
         }
      }

      public int[] setAndClearEdgeFlagWork(int[] work, boolean setVertType) {
         this.vertJointIdx = work;
         if (this.vertJointIdx.length < this.vertIdxCnt) {
            this.vertJointIdx = new int[this.vertIdxCnt * 2];
         }

         for(int i = 0; i < this.vertIdxCnt; ++i) {
            if (setVertType) {
               this.vertJointIdx[i] = this.calculateVertType(i);
            } else {
               int nextIndex = i + 1;
               if (nextIndex >= this.vertIdxCnt) {
                  nextIndex -= this.vertIdxCnt;
               }

               this.vertJointIdx[i] = nextIndex;
            }
         }

         return this.vertJointIdx;
      }

      public void setConnectSelfIdx(int index, int nextIdx) {
         this.vertJointIdx[index] = nextIdx;
      }

      public int getNextSelfIdx(int index) {
         return this.vertJointIdx[index];
      }

      public void setBypathOther(int index, int idxToOther, int othersLen) {
         if (idxToOther >= othersLen) {
            idxToOther -= othersLen;
         }

         this.vertJointIdx[index] = -(idxToOther + 1);
      }

      public boolean isBypathOther(int index) {
         return this.vertJointIdx[index] < 0;
      }

      public int getBypathIndex(int index) {
         return -(this.vertJointIdx[index] + 1);
      }

      public int searchUnMergedIdx(PbCellEdge ignoreEdge) {
         for(int i = 0; i < this.vertIdxCnt; ++i) {
            if (this.edges[i] != null && this.edges[i] != ignoreEdge) {
               return i;
            }
         }

         return -1;
      }

      public int searchUnMergedDupeVertIdx(int myIdx) {
         PbCellVert me = this.vertices[myIdx];
         int searchHit = -1;

         for(int oe = 1; oe < this.vertIdxCnt; ++oe) {
            int searchValid = oe + myIdx;
            if (searchValid >= this.vertIdxCnt) {
               searchValid -= this.vertIdxCnt;
            }

            if (this.vertices[searchValid] == me) {
               searchHit = searchValid;
               break;
            }
         }

         return searchHit;
      }

      public int searchUnMergedIdxFromNext(int bypathIdx) {
         int searchHit = -1;

         for(int oe = 0; oe < this.vertIdxCnt; ++oe) {
            int searchValid = oe + bypathIdx;
            if (searchValid >= this.vertIdxCnt) {
               searchValid -= this.vertIdxCnt;
            }

            if (this.edges[searchValid] != null) {
               searchHit = searchValid;
               break;
            }
         }

         return searchHit;
      }

      public int removeIndexVert(int targetEarIdx) {
         int lenSub1 = this.vertIdxCnt - 1;

         for(int i = targetEarIdx; i < lenSub1; ++i) {
            this.vertices[i] = this.vertices[i + 1];
            if (this.edges != null) {
               this.edges[i] = this.edges[i + 1];
            }

            if (this.vertJointIdx != null) {
               this.vertJointIdx[i] = this.vertJointIdx[i + 1];
            }
         }

         this.vertices[lenSub1] = null;
         return --this.vertIdxCnt;
      }

      public int calculateVertType(int idx) {
         int lenSub1 = this.vertIdxCnt - 1;
         PbCellVert eV1 = this.vertices[idx == 0 ? lenSub1 : idx - 1];
         PbCellVert eV2 = this.vertices[idx];
         PbCellVert eV3 = this.vertices[idx == lenSub1 ? 0 : idx + 1];
         int res = this.calculateVertType(eV1, eV2, eV3);
         return res == 2 ? 1 : res;
      }

      public int calculateVertType(PbCellVert eV1, PbCellVert eV2, PbCellVert eV3) {
         float v1X = eV3.tx - eV2.tx;
         float v1Y = eV3.ty - eV2.ty;
         float v1Z = eV3.tz - eV2.tz;
         float v2X = eV1.tx - eV2.tx;
         float v2Y = eV1.ty - eV2.ty;
         float v2Z = eV1.tz - eV2.tz;
         float xt = v1Y * v2Z - v1Z * v2Y;
         float yt = v1Z * v2X - v1X * v2Z;
         float zt = v1X * v2Y - v1Y * v2X;
         float mgn = Mth.sqrt(xt * xt + yt * yt + zt * zt);
         if (mgn < 1.0E-4F) {
            return 2;
         } else {
            xt = this.nX - xt / mgn;
            yt = this.nY - yt / mgn;
            zt = this.nZ - zt / mgn;
            return xt * xt + yt * yt + zt * zt < 1.0E-4F ? 0 : 1;
         }
      }
   }

   public static class PbCellEdge {
      public int preCalcHash;
      public PbCellVert vert1;
      public PbCellVert vert2;
      public LinkedList<PbCellFace> holderFaces = new LinkedList();

      public boolean setVertPairSort(PbCellVert vertA, PbCellVert vertB) {
         if (vertA.tx > vertB.tx) {
            this.vert1 = vertA;
            this.vert2 = vertB;
         } else if (vertA.tx < vertB.tx) {
            this.vert1 = vertB;
            this.vert2 = vertA;
         } else if (vertA.ty > vertB.ty) {
            this.vert1 = vertA;
            this.vert2 = vertB;
         } else if (vertA.ty < vertB.ty) {
            this.vert1 = vertB;
            this.vert2 = vertA;
         } else if (vertA.tz > vertB.tz) {
            this.vert1 = vertA;
            this.vert2 = vertB;
         } else {
            if (!(vertA.tz < vertB.tz)) {
               return false;
            }

            this.vert1 = vertB;
            this.vert2 = vertA;
         }

         this.preCalcHash = this.calcHash();
         return true;
      }

      public int calcHash() {
         int result = 37;
         result = 31 * result + Float.floatToIntBits(this.vert1.tx);
         result = 31 * result + Float.floatToIntBits(this.vert1.ty);
         result = 31 * result + Float.floatToIntBits(this.vert1.tz);
         result = 31 * result + Float.floatToIntBits(this.vert2.tx);
         result = 31 * result + Float.floatToIntBits(this.vert2.ty);
         result = 31 * result + Float.floatToIntBits(this.vert2.tz);
         return result;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            PbCellEdge otherObj = (PbCellEdge)obj;
            return this.vert1 == otherObj.vert1 && this.vert2 == otherObj.vert2;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.preCalcHash;
      }
   }

   public static class PbCellVertWrapper extends PbCellVert {
      public PbCellVert ref;
      public boolean isTurningPoint;
      public boolean twardsLeft;
      public Prebuilder.SplittedEdge connectFront;
      public Prebuilder.SplittedEdge connectFromBack;

      public PbCellVertWrapper(float argTx, float argTy, float argTz) {
         super(argTx, argTy, argTz);
      }

      public PbCellVertWrapper(PbCellVert argRef) {
         super(argRef.tx, argRef.ty, argRef.tz);
         this.ts = argRef.ts;
         this.ref = argRef;
      }
   }

   public static class PbCellVert {
      public float tx;
      public float ty;
      public float tz;
      public float ts;
      float inBoxX;
      float inBoxY;
      float inBoxZ;
      public int vertIdx_and_flg;
      public PbCellVert nextVertRef;
      public LinkedList<PbCellFace> holderFaces;

      public PbCellVert(float argTx, float argTy, float argTz) {
         this.vertIdx_and_flg = 0;
         this.holderFaces = new LinkedList();
         this.tx = argTx;
         this.ty = argTy;
         this.tz = argTz;
      }

      public PbCellVert(float argTx, float argTy, float argTz, float argTs, float rawX, float rawY, float rawZ) {
         this(argTx, argTy, argTz);
         this.ts = argTs;
         this.inBoxX = rawX;
         this.inBoxY = rawY;
         this.inBoxZ = rawZ;
      }

      public float distanceTo(PbCellVert other) {
         float d0 = other.tx - this.tx;
         float d1 = other.ty - this.ty;
         float d2 = other.tz - this.tz;
         return Mth.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      }

      public float distanceToSq(PbCellVert other) {
         float d0 = other.tx - this.tx;
         float d1 = other.ty - this.ty;
         float d2 = other.tz - this.tz;
         return d0 * d0 + d1 * d1 + d2 * d2;
      }

      public PbCellVert cloneForDuped(ArrayList<PbCellVert> vertListToAppend) {
         PbCellVert cloned = new PbCellVert(this.tx, this.ty, this.tz, this.ts, this.inBoxX, this.inBoxY, this.inBoxZ);
         vertListToAppend.add(cloned);
         return cloned;
      }

      public PbCellVert normalize() {
         float len = Mth.sqrt(this.tx * this.tx + this.ty * this.ty + this.tz * this.tz);
         if (len == 0.0F) {
            this.tx = this.ty = this.tz = 0.0F;
         } else {
            this.tx /= len;
            this.ty /= len;
            this.tz /= len;
         }

         return this;
      }

      public static float dot(PbCellVert a, PbCellVert b) {
         return a.tx * b.tx + a.ty * b.ty + a.tz * b.tz;
      }

      public static PbCellVert setSubtract(PbCellVert a, PbCellVert b, PbCellVert toSet) {
         toSet.tx = a.tx - b.tx;
         toSet.ty = a.ty - b.ty;
         toSet.tz = a.tz - b.tz;
         return toSet;
      }

      public String toString() {
         return "{" + String.valueOf(this.tx) + "," + this.ty + "," + this.tz + "}";
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            PbCellVert otherObj = (PbCellVert)obj;
            return this.tx == otherObj.tx && this.ty == otherObj.ty && this.tz == otherObj.tz;
         } else {
            return false;
         }
      }
   }
}
