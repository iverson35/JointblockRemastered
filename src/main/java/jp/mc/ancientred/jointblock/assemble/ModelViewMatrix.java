package jp.mc.ancientred.jointblock.assemble;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ModelViewMatrix extends MatrixBase implements IJBVectorTransformer {
   public static ThreadLocal<ModelViewMatrix> tHreadMvMatrix = new ThreadLocal<ModelViewMatrix>() {
      protected ModelViewMatrix initialValue() {
         return new ModelViewMatrix();
      }
   };
   public float tm00;
   public float tm01;
   public float tm02;
   public float tm03;
   public float tm10;
   public float tm11;
   public float tm12;
   public float tm13;
   public float tm20;
   public float tm21;
   public float tm22;
   public float tm23;
   public float tm30;
   public float tm31;
   public float tm32;
   public float tm33;
   public MatrixBase invMatrix = new MatrixBase();
   public boolean isRemote;
   public boolean rotTwdUpdateY;
   public boolean rotTwdUpdateX;
   public int index = 0;
   private int size = 300;
   private float[] list;
   public Vec3 cmnVec;
   public Vec3 cmnSubVec;
   public Vec3 camWorkVecA;
   public Vec3 camWorkVecB;
   public Vec3 min;
   public Vec3 max;
   public double baseEntityPosX;
   public double baseEntityPosY;
   public double baseEntityPosZ;
   public float savedBaseM00;
   public float savedBaseM10;
   public float savedBaseM20;
   public float savedBaseM01;
   public float savedBaseM11;
   public float savedBaseM21;
   public float savedBaseM02;
   public float savedBaseM12;
   public float savedBaseM22;
   public int renderPass;
   public int rayHitSide;
   public static Vec3 rayHitVecWork;
   public Vec3 rayBecSt;
   public Vec3 rayBecEd;
   public Vec3 rayBecStCnv;
   public Vec3 rayBecEdCnv;
   public Vec3 vecRayWorkYZ2MinX;
   public Vec3 vecRayWorkYZ2MaxX;
   public Vec3 vecRayWorkXZ2MinY;
   public Vec3 vecRayWorkXZ2MaxY;
   public Vec3 vecRayWorkXY2MinZ;
   public Vec3 vecRayWorkXY2MaxZ;
   public AABB selectBoxBB;
   private FloatBuffer buf;

   public ModelViewMatrix() {
      this.list = new float[this.size];
      this.cmnVec = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.cmnSubVec = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.camWorkVecA = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.camWorkVecB = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.min = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.max = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.rayBecSt = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.rayBecEd = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.rayBecStCnv = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.rayBecEdCnv = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkYZ2MinX = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkYZ2MaxX = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkXZ2MinY = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkXZ2MaxY = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkXY2MinZ = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecRayWorkXY2MaxZ = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.selectBoxBB = JBCommonUtil.getBoundingBox(-0.55, 0.45, -0.55, 0.55, 1.55, 0.55);
      this.buf = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
   }

   public void saveBaseRotation() {
      this.savedBaseM00 = this.m00;
      this.savedBaseM01 = this.m01;
      this.savedBaseM02 = this.m02;
      this.savedBaseM10 = this.m10;
      this.savedBaseM11 = this.m11;
      this.savedBaseM12 = this.m12;
      this.savedBaseM20 = this.m20;
      this.savedBaseM21 = this.m21;
      this.savedBaseM22 = this.m22;
   }

   public void setBasePos(EntityJointed entity, boolean addCoreHeight) {
      this.min.x = this.min.y = this.min.z = 1000.0;
      this.max.x = this.max.y = this.max.z = -1000.0;
      this.baseEntityPosX = entity.getX();
      this.baseEntityPosY = entity.getY();
      this.baseEntityPosZ = entity.getZ();
      if (addCoreHeight) {
         this.cmnVec.x = 0.0;
         this.cmnVec.y = (double)(-entity.coreHeight);
         this.cmnVec.z = 0.0;
         this.transformOnlyRotation(this.cmnVec);
         this.baseEntityPosX += this.cmnVec.x;
         this.baseEntityPosY += this.cmnVec.y;
         this.baseEntityPosZ += this.cmnVec.z;
      }

   }

   public void reset() {
      this.setIdentity();
      this.index = 0;
   }

   public void updateMinMax(int jointType, int connectSide) {
      double val = this.cmnVec.x;
      if (this.max.x < val) {
         this.max.x = val;
      }

      if (this.min.x > val) {
         this.min.x = val;
      }

      val = this.cmnVec.y;
      if (this.max.y < val) {
         this.max.y = val;
      }

      if (this.min.y > val - 1.5) {
         this.setDownTwardsSubVec(jointType, connectSide);
         val += this.cmnSubVec.y;
         if (this.min.y > val) {
            this.min.y = val;
         }
      }

      val = this.cmnVec.z;
      if (this.max.z < val) {
         this.max.z = val;
      }

      if (this.min.z > val) {
         this.min.z = val;
      }

   }

   public final void updateMinMaxDirect(int jointType, int connectSide) {
      double val = (double)(this.m01 + this.m03);
      if (this.max.x < val) {
         this.max.x = val;
      }

      if (this.min.x > val) {
         this.min.x = val;
      }

      val = (double)(this.m11 + this.m13);
      if (this.max.y < val) {
         this.max.y = val;
      }

      if (this.min.y > val - 1.5) {
         this.setDownTwardsSubVec(jointType, connectSide);
         val += this.cmnSubVec.y;
         if (this.min.y > val) {
            this.min.y = val;
         }
      }

      val = (double)(this.m21 + this.m23);
      if (this.max.z < val) {
         this.max.z = val;
      }

      if (this.min.z > val) {
         this.min.z = val;
      }

   }

   public int pushMatrix() {
      this.ensureList();
      this.list[++this.index] = this.m00;
      this.list[++this.index] = this.m01;
      this.list[++this.index] = this.m02;
      this.list[++this.index] = this.m03;
      this.list[++this.index] = this.m10;
      this.list[++this.index] = this.m11;
      this.list[++this.index] = this.m12;
      this.list[++this.index] = this.m13;
      this.list[++this.index] = this.m20;
      this.list[++this.index] = this.m21;
      this.list[++this.index] = this.m22;
      this.list[++this.index] = this.m23;
      this.list[++this.index] = this.m30;
      this.list[++this.index] = this.m31;
      this.list[++this.index] = this.m32;
      this.list[++this.index] = this.m33;
      return this.index;
   }

   public void tempPushMatrix() {
      this.tm00 = this.m00;
      this.tm10 = this.m10;
      this.tm20 = this.m20;
      this.tm30 = this.m30;
      this.tm01 = this.m01;
      this.tm11 = this.m11;
      this.tm21 = this.m21;
      this.tm31 = this.m31;
      this.tm02 = this.m02;
      this.tm12 = this.m12;
      this.tm22 = this.m22;
      this.tm32 = this.m32;
      this.tm03 = this.m03;
      this.tm13 = this.m13;
      this.tm23 = this.m23;
      this.tm33 = this.m33;
   }

   public void tempPopMatrix() {
      this.m00 = this.tm00;
      this.m10 = this.tm10;
      this.m20 = this.tm20;
      this.m30 = this.tm30;
      this.m01 = this.tm01;
      this.m11 = this.tm11;
      this.m21 = this.tm21;
      this.m31 = this.tm31;
      this.m02 = this.tm02;
      this.m12 = this.tm12;
      this.m22 = this.tm22;
      this.m32 = this.tm32;
      this.m03 = this.tm03;
      this.m13 = this.tm13;
      this.m23 = this.tm23;
      this.m33 = this.tm33;
   }

   private void ensureList() {
      while(this.index >= this.list.length || this.index + 16 >= this.list.length) {
         this.list = Arrays.copyOf(this.list, this.size *= 2);
      }

   }

   public void popBeforeIndexAndPushMatrix(int matrixIndex) {
      if (matrixIndex != 0) {
         this.index = matrixIndex;
         this.popMatrix();
         this.index = matrixIndex;
      }

   }

   public void popMatrix() {
      this.m33 = this.list[this.index--];
      this.m32 = this.list[this.index--];
      this.m31 = this.list[this.index--];
      this.m30 = this.list[this.index--];
      this.m23 = this.list[this.index--];
      this.m22 = this.list[this.index--];
      this.m21 = this.list[this.index--];
      this.m20 = this.list[this.index--];
      this.m13 = this.list[this.index--];
      this.m12 = this.list[this.index--];
      this.m11 = this.list[this.index--];
      this.m10 = this.list[this.index--];
      this.m03 = this.list[this.index--];
      this.m02 = this.list[this.index--];
      this.m01 = this.list[this.index--];
      this.m00 = this.list[this.index--];
   }

   public Vec3 transformRelative(Vec3 vec) {
      return this.transform(vec);
   }

   public Vec3 transformAbsolute(Vec3 vec) {
      vec = this.transform(vec);
      vec.x += this.baseEntityPosX;
      vec.y += this.baseEntityPosY;
      vec.z += this.baseEntityPosZ;
      return vec;
   }

   public void resetRotate() {
      float scaleX = this.getScaleX();
      float scaleY = this.getScaleX();
      float scaleZ = this.getScaleX();
      this.m00 = this.savedBaseM00;
      this.m01 = this.savedBaseM01;
      this.m02 = this.savedBaseM02;
      this.m10 = this.savedBaseM10;
      this.m11 = this.savedBaseM11;
      this.m12 = this.savedBaseM12;
      this.m20 = this.savedBaseM20;
      this.m21 = this.savedBaseM21;
      this.m22 = this.savedBaseM22;
      this.scale(scaleX, scaleY, scaleZ);
   }

   public void setRotationToMasterAttatcher(MasterSlot holder) {
      float scaleX = this.getScaleX();
      float scaleY = this.getScaleX();
      float scaleZ = this.getScaleX();
      holder.m00 = this.m00;
      holder.m01 = this.m01;
      holder.m02 = this.m02;
      holder.m10 = this.m10;
      holder.m11 = this.m11;
      holder.m12 = this.m12;
      holder.m20 = this.m20;
      holder.m21 = this.m21;
      holder.m22 = this.m22;
      holder.m00 *= 1.0F / scaleX;
      holder.m01 *= 1.0F / scaleY;
      holder.m02 *= 1.0F / scaleZ;
      holder.m10 *= 1.0F / scaleX;
      holder.m11 *= 1.0F / scaleY;
      holder.m12 *= 1.0F / scaleZ;
      holder.m20 *= 1.0F / scaleX;
      holder.m21 *= 1.0F / scaleY;
      holder.m22 *= 1.0F / scaleZ;
   }

   public void resumeRotationFromMasterAttatcher(MasterSlot holder) {
      this.m00 = holder.m00;
      this.m01 = holder.m01;
      this.m02 = holder.m02;
      this.m10 = holder.m10;
      this.m11 = holder.m11;
      this.m12 = holder.m12;
      this.m20 = holder.m20;
      this.m21 = holder.m21;
      this.m22 = holder.m22;
   }

   public void setBaseRotationToMasterAttatcher(MasterSlot holder) {
      holder.savedBaseM00 = this.savedBaseM00;
      holder.savedBaseM01 = this.savedBaseM01;
      holder.savedBaseM02 = this.savedBaseM02;
      holder.savedBaseM10 = this.savedBaseM10;
      holder.savedBaseM11 = this.savedBaseM11;
      holder.savedBaseM12 = this.savedBaseM12;
      holder.savedBaseM20 = this.savedBaseM20;
      holder.savedBaseM21 = this.savedBaseM21;
      holder.savedBaseM22 = this.savedBaseM22;
   }

   public void setBaseRotationFromMasterAttatcher(MasterSlot holder) {
      this.savedBaseM00 = holder.savedBaseM00;
      this.savedBaseM01 = holder.savedBaseM01;
      this.savedBaseM02 = holder.savedBaseM02;
      this.savedBaseM10 = holder.savedBaseM10;
      this.savedBaseM11 = holder.savedBaseM11;
      this.savedBaseM12 = holder.savedBaseM12;
      this.savedBaseM20 = holder.savedBaseM20;
      this.savedBaseM21 = holder.savedBaseM21;
      this.savedBaseM22 = holder.savedBaseM22;
   }

   public FloatBuffer getMatrixTransferredFBuf() {
      int indexNow = this.index;
      this.buf.rewind();
      this.pushInListWithGLOrder();
      this.buf.put(this.list, indexNow + 1, 16);
      this.buf.flip();
      this.popFromListWithGLOrder();
      return this.buf;
   }

   public void pushInListWithGLOrder() {
      this.ensureList();
      this.list[++this.index] = this.m00;
      this.list[++this.index] = this.m10;
      this.list[++this.index] = this.m20;
      this.list[++this.index] = this.m30;
      this.list[++this.index] = this.m01;
      this.list[++this.index] = this.m11;
      this.list[++this.index] = this.m21;
      this.list[++this.index] = this.m31;
      this.list[++this.index] = this.m02;
      this.list[++this.index] = this.m12;
      this.list[++this.index] = this.m22;
      this.list[++this.index] = this.m32;
      this.list[++this.index] = this.m03;
      this.list[++this.index] = this.m13;
      this.list[++this.index] = this.m23;
      this.list[++this.index] = this.m33;
   }

   public void popFromListWithGLOrder() {
      this.m33 = this.list[this.index--];
      this.m23 = this.list[this.index--];
      this.m13 = this.list[this.index--];
      this.m03 = this.list[this.index--];
      this.m32 = this.list[this.index--];
      this.m22 = this.list[this.index--];
      this.m12 = this.list[this.index--];
      this.m02 = this.list[this.index--];
      this.m31 = this.list[this.index--];
      this.m21 = this.list[this.index--];
      this.m11 = this.list[this.index--];
      this.m01 = this.list[this.index--];
      this.m30 = this.list[this.index--];
      this.m20 = this.list[this.index--];
      this.m10 = this.list[this.index--];
      this.m00 = this.list[this.index--];
   }

   public static void forceReleasDirect(ByteBuffer del) {
      try {
         Method cleanerMethod = del.getClass().getMethod("cleaner");
         cleanerMethod.setAccessible(true);
         Object cleaner = cleanerMethod.invoke(del);
         Method cleanMethod = cleaner.getClass().getMethod("clean");
         cleanMethod.setAccessible(true);
         cleanMethod.invoke(cleaner);
      } catch (Exception var4) {
      }

   }

   public void setDownTwardsSubVec(int jointType, int connectSide) {
      int lowestSide;
      if (jointType == 10) {
         lowestSide = EnumDirectionOld.OPPOSITES[connectSide];
         this.cmnSubVec.x = JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][0];
         this.cmnSubVec.y = -JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][1];
         this.cmnSubVec.z = -JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][2];
         this.transformOnlyRotation(this.cmnSubVec);
      } else {
         lowestSide = this.detectLowestSide();
         this.cmnSubVec.x = JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][0];
         this.cmnSubVec.y = JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][1];
         this.cmnSubVec.z = JBRotationUtil.DOUBLE_SIDE_HALF_OFFSET[lowestSide][2];
         this.transformOnlyRotation(this.cmnSubVec);
      }

   }

   public int detectLowestSide() {
      double yFromX = (double)this.m10;
      double yFromY = (double)this.m11;
      double yFromZ = (double)this.m12;
      int retX = 4;
      if (yFromX < 0.0) {
         yFromX = 0.0 - yFromX;
         retX = 5;
      }

      int retY = 0;
      if (yFromY < 0.0) {
         yFromY = 0.0 - yFromY;
         retY = 1;
      }

      int retZ = 2;
      if (yFromZ < 0.0) {
         yFromZ = 0.0 - yFromZ;
         retZ = 3;
      }

      if (yFromY >= yFromX && yFromY >= yFromZ) {
         return retY;
      } else {
         return yFromX >= yFromZ ? retX : retZ;
      }
   }

   public Vec3 getCamWorkVecAWithValue(double x, double y, double z) {
      this.camWorkVecA.x = x;
      this.camWorkVecA.y = y;
      this.camWorkVecA.z = z;
      return this.camWorkVecA;
   }

   public Vec3 crossProductToCamWorkVecB(Vec3 vecA, Vec3 vecB) {
      this.camWorkVecB.x = vecA.y * vecB.z - vecA.z * vecB.y;
      this.camWorkVecB.y = vecA.z * vecB.x - vecA.x * vecB.z;
      this.camWorkVecB.z = vecA.x * vecB.y - vecA.y * vecB.x;
      return this.camWorkVecB;
   }

   public void setupRayVec() {
      this.rayBecSt.x -= this.baseEntityPosX;
      this.rayBecSt.y -= this.baseEntityPosY;
      this.rayBecSt.z -= this.baseEntityPosZ;
      this.rayBecEd.x -= this.baseEntityPosX;
      this.rayBecEd.y -= this.baseEntityPosY;
      this.rayBecEd.z -= this.baseEntityPosZ;
   }

   public void setToQuat(Quaternion quat) {
      float tr = this.m00 + this.m11 + this.m22;
      float s;
      if (tr >= 0.0F) {
         s = (float)Math.sqrt((double)(tr + 1.0F));
         quat.w = s * 0.5F;
         s = 0.5F / s;
         quat.x = (this.m21 - this.m12) * s;
         quat.y = (this.m02 - this.m20) * s;
         quat.z = (this.m10 - this.m01) * s;
      } else {
         float max = Math.max(Math.max(this.m00, this.m11), this.m22);
         if (max == this.m00) {
            s = (float)Math.sqrt((double)(this.m00 - (this.m11 + this.m22) + 1.0F));
            quat.x = s * 0.5F;
            s = 0.5F / s;
            quat.y = (this.m01 + this.m10) * s;
            quat.z = (this.m20 + this.m02) * s;
            quat.w = (this.m21 - this.m12) * s;
         } else if (max == this.m11) {
            s = (float)Math.sqrt((double)(this.m11 - (this.m22 + this.m00) + 1.0F));
            quat.y = s * 0.5F;
            s = 0.5F / s;
            quat.z = (this.m12 + this.m21) * s;
            quat.x = (this.m01 + this.m10) * s;
            quat.w = (this.m02 - this.m20) * s;
         } else {
            s = (float)Math.sqrt((double)(this.m22 - (this.m00 + this.m11) + 1.0F));
            quat.z = s * 0.5F;
            s = 0.5F / s;
            quat.x = (this.m20 + this.m02) * s;
            quat.y = (this.m12 + this.m21) * s;
            quat.w = (this.m10 - this.m01) * s;
         }
      }

   }

   public void setRotation(Quaternion quat) {
      float sqw = quat.w * quat.w;
      float sqx = quat.x * quat.x;
      float sqy = quat.y * quat.y;
      float sqz = quat.z * quat.z;
      float invs = 1.0F / (sqx + sqy + sqz + sqw);
      this.m00 = (sqx - sqy - sqz + sqw) * invs;
      this.m11 = (-sqx + sqy - sqz + sqw) * invs;
      this.m22 = (-sqx - sqy + sqz + sqw) * invs;
      float mulA = quat.x * quat.y;
      float mulB = quat.z * quat.w;
      this.m10 = 2.0F * (mulA + mulB) * invs;
      this.m01 = 2.0F * (mulA - mulB) * invs;
      mulA = quat.x * quat.z;
      mulB = quat.y * quat.w;
      this.m20 = 2.0F * (mulA - mulB) * invs;
      this.m02 = 2.0F * (mulA + mulB) * invs;
      mulA = quat.y * quat.z;
      mulB = quat.x * quat.w;
      this.m21 = 2.0F * (mulA + mulB) * invs;
      this.m12 = 2.0F * (mulA - mulB) * invs;
   }

   public void rotate(float angle, Vec3 axis) {
      double c = Math.cos((double)angle);
      double s = Math.sin((double)angle);
      double oneminusc = 1.0 - c;
      double xy = axis.x * axis.y;
      double yz = axis.y * axis.z;
      double xz = axis.x * axis.z;
      double xs = axis.x * s;
      double ys = axis.y * s;
      double zs = axis.z * s;
      float f00 = (float)(axis.x * axis.x * oneminusc + c);
      float f01 = (float)(xy * oneminusc + zs);
      float f02 = (float)(xz * oneminusc - ys);
      float f10 = (float)(xy * oneminusc - zs);
      float f11 = (float)(axis.y * axis.y * oneminusc + c);
      float f12 = (float)(yz * oneminusc + xs);
      float f20 = (float)(xz * oneminusc + ys);
      float f21 = (float)(yz * oneminusc - xs);
      float f22 = (float)(axis.z * axis.z * oneminusc + c);
      float t00 = this.m00 * f00 + this.m10 * f01 + this.m20 * f02;
      float t01 = this.m01 * f00 + this.m11 * f01 + this.m21 * f02;
      float t02 = this.m02 * f00 + this.m12 * f01 + this.m22 * f02;
      float t03 = this.m03 * f00 + this.m13 * f01 + this.m23 * f02;
      float t10 = this.m00 * f10 + this.m10 * f11 + this.m20 * f12;
      float t11 = this.m01 * f10 + this.m11 * f11 + this.m21 * f12;
      float t12 = this.m02 * f10 + this.m12 * f11 + this.m22 * f12;
      float t13 = this.m03 * f10 + this.m13 * f11 + this.m23 * f12;
      this.m20 = this.m00 * f20 + this.m10 * f21 + this.m20 * f22;
      this.m21 = this.m01 * f20 + this.m11 * f21 + this.m21 * f22;
      this.m22 = this.m02 * f20 + this.m12 * f21 + this.m22 * f22;
      this.m23 = this.m03 * f20 + this.m13 * f21 + this.m23 * f22;
      this.m00 = t00;
      this.m01 = t01;
      this.m02 = t02;
      this.m03 = t03;
      this.m10 = t10;
      this.m11 = t11;
      this.m12 = t12;
      this.m13 = t13;
   }

   public double getYawFromMatrix() {
      if ((double)this.m12 < 0.99998) {
         return (double)this.m12 > -0.99998 ? Math.atan2((double)this.m02, (double)this.m22) : Math.PI - Math.atan2((double)(-this.m01), (double)this.m00);
      } else {
         return Math.PI + Math.atan2((double)(-this.m01), (double)this.m00);
      }
   }

   public void updateSavedBaseFromYawRad(double yawRad) {
      float cos = (float)Math.cos(yawRad);
      float sin = (float)Math.sin(yawRad);
      this.savedBaseM00 = cos;
      this.savedBaseM01 = 0.0F;
      this.savedBaseM02 = -sin * -1.0F;
      this.savedBaseM10 = 0.0F;
      this.savedBaseM11 = -1.0F;
      this.savedBaseM12 = 0.0F;
      this.savedBaseM20 = sin;
      this.savedBaseM21 = 0.0F;
      this.savedBaseM22 = cos * -1.0F;
   }
}
