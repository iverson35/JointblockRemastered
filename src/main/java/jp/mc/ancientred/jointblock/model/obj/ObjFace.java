package jp.mc.ancientred.jointblock.model.obj;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.util.Mth;

public class ObjFace {
   public int vertIdxCnt;
   public ObjVertex[] vertices;
   public boolean isFaceNormalInvalid;
   public ObjVec3f faceNormal;
   public Material material = null;
   public static final int CONVEX = 0;
   public static final int CONCAVE = 1;
   public static final int COLLINEAR = 2;

   public ObjFace() {
   }

   public ObjFace(ArrayList<ObjVertex> temporaryVertList) {
      this.setVerticesList(temporaryVertList);
   }

   public void setVerticesList(ArrayList<ObjVertex> temporaryVertList) {
      this.vertices = (ObjVertex[])temporaryVertList.toArray(new ObjVertex[temporaryVertList.size()]);
      this.vertIdxCnt = this.vertices.length;
      this.calculateFaceNormal();
   }

   public boolean calculateFaceNormal() {
      if (this.isFaceNormalInvalid) {
         return false;
      } else if (this.faceNormal != null) {
         return true;
      } else {
         this.faceNormal = new ObjVec3f(0.0F, 0.0F, 0.0F);
         float[] edge1 = new float[3];
         float[] edge2 = new float[3];
         float[] normal = new float[3];
         ObjVec3f v1 = this.vertices[0].v;
         ObjVec3f v2 = this.vertices[1].v;
         ObjVec3f v3 = this.vertices[2].v;
         float[] p1 = new float[]{v1.x, v1.y, v1.z};
         float[] p2 = new float[]{v2.x, v2.y, v2.z};
         float[] p3 = new float[]{v3.x, v3.y, v3.z};
         edge1[0] = p2[0] - p1[0];
         edge1[1] = p2[1] - p1[1];
         edge1[2] = p2[2] - p1[2];
         edge2[0] = p3[0] - p2[0];
         edge2[1] = p3[1] - p2[1];
         edge2[2] = p3[2] - p2[2];
         normal[0] = edge1[1] * edge2[2] - edge1[2] * edge2[1];
         normal[1] = edge1[2] * edge2[0] - edge1[0] * edge2[2];
         normal[2] = edge1[0] * edge2[1] - edge1[1] * edge2[0];
         this.faceNormal.x = normal[0];
         this.faceNormal.y = normal[1];
         this.faceNormal.z = normal[2];
         float d0 = Mth.sqrt(this.faceNormal.x * this.faceNormal.x + this.faceNormal.y * this.faceNormal.y + this.faceNormal.z * this.faceNormal.z);
         if ((double)d0 < 1.0E-4) {
            return false;
         } else {
            this.faceNormal.x /= d0;
            this.faceNormal.y /= d0;
            this.faceNormal.z /= d0;
            return true;
         }
      }
   }

   public boolean breakDownToTris(TrisCalculationHelper tch) {
      tch.triangleList.clear();
      if (!tch.calcNormalNewell(this)) {
         JointBlockRemastered.LOGGER.info("Obj face triangulation failed. invalid face normal.");
         return false;
      } else {
         int remainVCnt = this.vertIdxCnt;
         if (tch.vertTypes == null || tch.vertTypes.length < this.vertIdxCnt) {
            tch.vertTypes = new int[this.vertIdxCnt];
         }

         for(int i = 0; i < remainVCnt; ++i) {
            tch.vertTypes[i] = this.calculateVertType(this, i, tch);
         }

         while(remainVCnt > 3) {
            int lenSub1 = remainVCnt - 1;
            int earIdx = -1;
            float maxAngleDiff = -Float.MAX_VALUE;

            for(int v = 0; v < remainVCnt; ++v) {
               if (tch.vertTypes[v] != CONCAVE) {
                  int earPrevIdx = v == 0 ? lenSub1 : v - 1;
                  int earNextIdx = v == lenSub1 ? 0 : v + 1;
                  ObjVec3f eV1 = this.vertices[earPrevIdx].v;
                  ObjVec3f eV2 = this.vertices[v].v;
                  ObjVec3f eV3 = this.vertices[earNextIdx].v;
                  float minAngleDiffWithOther = Float.MAX_VALUE;

                  for(int vt = 2; vt < lenSub1; ++vt) {
                     int vtCurrIdx = vt + v;
                     if (vtCurrIdx >= remainVCnt) {
                        vtCurrIdx -= remainVCnt;
                     }

                     float angleDiff = this.angleVertInTris(eV1, eV2, eV3, this.vertices[vtCurrIdx].v, tch);
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
               JointBlockRemastered.LOGGER.info("Obj face triangulation failed. no ear.");
               return false;
            }

            int earPrevIdx = earIdx == 0 ? lenSub1 : earIdx - 1;
            int earNextIdx = earIdx == lenSub1 ? 0 : earIdx + 1;
            tch.triangleList.add(this.createNewTriangle(this, earPrevIdx, earIdx, earNextIdx));

            for(int v = earIdx; v < lenSub1; ++v) {
               this.vertices[v] = this.vertices[v + 1];
               tch.vertTypes[v] = tch.vertTypes[v + 1];
            }

            this.vertices[lenSub1] = null;
            remainVCnt = --this.vertIdxCnt;
            int v = earIdx == 0 ? remainVCnt - 1 : earIdx - 1;
            int fixedNext = earIdx == remainVCnt ? 0 : earIdx;
            tch.vertTypes[v] = this.calculateVertType(this, v, tch);
            tch.vertTypes[fixedNext] = this.calculateVertType(this, fixedNext, tch);
         }

         if (remainVCnt == 3) {
            tch.triangleList.add(this.createNewTriangle(this, 0, 1, 2));
         }

         return true;
      }
   }

   private int calculateVertType(ObjFace face, int idx, TrisCalculationHelper tch) {
      int lenSub1 = this.vertIdxCnt - 1;
      ObjVec3f eV1 = face.vertices[idx == 0 ? lenSub1 : idx - 1].v;
      ObjVec3f eV2 = face.vertices[idx].v;
      ObjVec3f eV3 = face.vertices[idx == lenSub1 ? 0 : idx + 1].v;
      int res = this.calculateVertType(eV1, eV2, eV3, tch);
      return res == COLLINEAR ? CONCAVE : res;
   }

   public int calculateVertType(ObjVec3f eV1, ObjVec3f eV2, ObjVec3f eV3, TrisCalculationHelper tch) {
      float v1X = eV3.x - eV2.x;
      float v1Y = eV3.y - eV2.y;
      float v1Z = eV3.z - eV2.z;
      float v2X = eV1.x - eV2.x;
      float v2Y = eV1.y - eV2.y;
      float v2Z = eV1.z - eV2.z;
      float xt = v1Y * v2Z - v1Z * v2Y;
      float yt = v1Z * v2X - v1X * v2Z;
      float zt = v1X * v2Y - v1Y * v2X;
      float mgn = Mth.sqrt(xt * xt + yt * yt + zt * zt);
      if (mgn < 1.0E-5F) {
         return COLLINEAR;
      } else {
         xt = tch.nX - xt / mgn;
         yt = tch.nY - yt / mgn;
         zt = tch.nZ - zt / mgn;
         return xt * xt + yt * yt + zt * zt < 0.001F ? CONVEX : CONCAVE;
      }
   }

   public float angleVertInTris(ObjVec3f p1, ObjVec3f p2, ObjVec3f p3, ObjVec3f targetPoint, TrisCalculationHelper tch) {
      tch.intrisA = ObjFace.ObjVec3f.setSubtract(targetPoint, p1, tch.intrisA).normalize();
      tch.intrisB = ObjFace.ObjVec3f.setSubtract(targetPoint, p2, tch.intrisB).normalize();
      tch.intrisC = ObjFace.ObjVec3f.setSubtract(targetPoint, p3, tch.intrisC).normalize();
      double angles = 0.0;
      angles += Math.acos((double)Mth.clamp(ObjFace.ObjVec3f.dot(tch.intrisA, tch.intrisB), -1.0F, 1.0F));
      angles += Math.acos((double)Mth.clamp(ObjFace.ObjVec3f.dot(tch.intrisB, tch.intrisC), -1.0F, 1.0F));
      angles += Math.acos((double)Mth.clamp(ObjFace.ObjVec3f.dot(tch.intrisC, tch.intrisA), -1.0F, 1.0F));
      return (float)Math.abs(angles - 6.283185307179586);
   }

   private ObjFace createNewTriangle(ObjFace face, int prevIdx, int centIdx, int nextIdx) {
      ObjFace tris = new ObjFace();
      tris.vertices = new ObjVertex[3];
      tris.vertIdxCnt = 3;
      tris.vertices[0] = face.vertices[prevIdx];
      tris.vertices[1] = face.vertices[centIdx];
      tris.vertices[2] = face.vertices[nextIdx];
      tris.material = face.material;
      tris.calculateFaceNormal();
      return tris;
   }

   public static class TrisCalculationHelper {
      public int[] vertTypes;
      public ArrayList<ObjFace> triangleList = new ArrayList<>();
      public ObjVec3f intrisA = new ObjVec3f(0.0F, 0.0F, 0.0F);
      public ObjVec3f intrisB = new ObjVec3f(0.0F, 0.0F, 0.0F);
      public ObjVec3f intrisC = new ObjVec3f(0.0F, 0.0F, 0.0F);
      public float nX;
      public float nY;
      public float nZ;

      public boolean calcNormalNewell(ObjFace face) {
         float x = 0.0F;
         float y = 0.0F;
         float z = 0.0F;

         for(int i = 0; i < face.vertIdxCnt; ++i) {
            ObjVec3f v = face.vertices[i].v;
            ObjVec3f vN = face.vertices[i == face.vertIdxCnt - 1 ? 0 : i + 1].v;
            if (v != vN) {
               x += (v.y - vN.y) * (v.z + vN.z);
               y += (v.z - vN.z) * (v.x + vN.x);
               z += (v.x - vN.x) * (v.y + vN.y);
            }
         }

         float mag = Mth.sqrt(x * x + y * y + z * z);
         if (mag != 0.0F) {
            this.nX = x / mag;
            this.nY = y / mag;
            this.nZ = z / mag;
            return true;
         } else {
            this.nX = this.nY = this.nZ = 0.0F;
            return false;
         }
      }
   }

   public static class Material {
      public String name;
      public Reflection ka = new Reflection();
      public Reflection kd = new Reflection();
      public Reflection ks = new Reflection();
      public Reflection tf = new Reflection();
      public double dFactor = 1.0;

      public Material(String name) {
         this.name = name;
      }
   }

   public static class Reflection {
      public boolean isRGB = false;
      public boolean isXYZ = false;
      public double rx;
      public double gy;
      public double bz;
   }

   public static class TextureUV {
      public float u = 0.0F;
      public float v = 0.0F;

      public TextureUV(float u, float v) {
         this.u = u;
         this.v = v;
      }
   }

   public static class ObjVec3f {
      public float x = 0.0F;
      public float y = 0.0F;
      public float z = 0.0F;

      public ObjVec3f(float x, float y, float z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public ObjVec3f normalize() {
         float len = Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
         if (len == 0.0F) {
            this.x = this.y = this.z = 0.0F;
         } else {
            this.x /= len;
            this.y /= len;
            this.z /= len;
         }

         return this;
      }

      public static float dot(ObjVec3f a, ObjVec3f b) {
         return a.x * b.x + a.y * b.y + a.z * b.z;
      }

      public static ObjVec3f setSubtract(ObjVec3f a, ObjVec3f b, ObjVec3f toSet) {
         toSet.x = a.x - b.x;
         toSet.y = a.y - b.y;
         toSet.z = a.z - b.z;
         return toSet;
      }
   }

   public static class ObjVertex {
      public ObjVec3f v = null;
      public ObjVec3f n = null;
      public TextureUV t = null;

      public ObjVertex(ObjVec3f vertexCoord) {
         this.v = vertexCoord;
      }
   }
}
