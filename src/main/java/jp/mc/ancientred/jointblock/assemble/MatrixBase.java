package jp.mc.ancientred.jointblock.assemble;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MatrixBase {
   public float m00;
   public float m01;
   public float m02;
   public float m03;
   public float m10;
   public float m11;
   public float m12;
   public float m13;
   public float m20;
   public float m21;
   public float m22;
   public float m23;
   public float m30;
   public float m31;
   public float m32;
   public float m33;

   public String toString() {
      return "m00:" + this.m00 + "m01:" + this.m01 + "m02:" + this.m02 + "m03:" + this.m03 + "m10:" + this.m10 + "m11:" + this.m11 + "m12:" + this.m12 + "m13:" + this.m13 + "m20:" + this.m20 + "m21:" + this.m21 + "m22:" + this.m22 + "m23:" + this.m23 + "m30:" + this.m30 + "m31:" + this.m31 + "m32:" + this.m32 + "m33:" + this.m33;
   }

   public final void setIdentity() {
      this.m00 = 1.0F;
      this.m01 = 0.0F;
      this.m02 = 0.0F;
      this.m03 = 0.0F;
      this.m10 = 0.0F;
      this.m11 = 1.0F;
      this.m12 = 0.0F;
      this.m13 = 0.0F;
      this.m20 = 0.0F;
      this.m21 = 0.0F;
      this.m22 = 1.0F;
      this.m23 = 0.0F;
      this.m30 = 0.0F;
      this.m31 = 0.0F;
      this.m32 = 0.0F;
      this.m33 = 1.0F;
   }

   public void rotate(float angle, float vX, float vY, float vZ) {
      float c = Mth.cos(angle);
      float s = Mth.sin(angle);
      float t = 1.0F - c;
      this.apply(t * vX * vX + c, t * vX * vY - s * vZ, t * vX * vZ + s * vY, 0.0F, t * vX * vY + s * vZ, t * vY * vY + c, t * vY * vZ - s * vX, 0.0F, t * vX * vZ - s * vY, t * vY * vZ + s * vX, t * vZ * vZ + c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void rotateX(float angle) {
      float c = Mth.cos(angle);
      float s = Mth.sin(angle);
      float t = 1.0F - c;
      this.apply(t + c, 0.0F, 0.0F, 0.0F, 0.0F, c, -s, 0.0F, 0.0F, s, c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void rotateXX(float angle) {
      float cs = Mth.cos(angle);
      float sp = Mth.sin(angle);
      float sn = -sp;
      float a = this.m01 * cs + this.m02 * sp;
      float b = this.m01 * sn + this.m02 * cs;
      this.m01 = a;
      this.m02 = b;
      a = this.m11 * cs + this.m12 * sp;
      b = this.m11 * sn + this.m12 * cs;
      this.m11 = a;
      this.m12 = b;
      a = this.m21 * cs + this.m22 * sp;
      b = this.m21 * sn + this.m22 * cs;
      this.m21 = a;
      this.m22 = b;
      a = this.m31 * cs + this.m32 * sp;
      b = this.m31 * sn + this.m32 * cs;
      this.m31 = a;
      this.m32 = b;
   }

   public void rotateY(float angle) {
      float c = Mth.cos(angle);
      float s = Mth.sin(angle);
      float t = 1.0F - c;
      this.apply(c, 0.0F, s, 0.0F, 0.0F, t + c, 0.0F, 0.0F, -s, 0.0F, c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void rotateYY(float angle) {
      float cs = Mth.cos(angle);
      float sp = Mth.sin(angle);
      float sn = -sp;
      float a = this.m00 * cs + this.m02 * sn;
      float b = this.m00 * sp + this.m02 * cs;
      this.m00 = a;
      this.m02 = b;
      a = this.m10 * cs + this.m12 * sn;
      b = this.m10 * sp + this.m12 * cs;
      this.m10 = a;
      this.m12 = b;
      a = this.m20 * cs + this.m22 * sn;
      b = this.m20 * sp + this.m22 * cs;
      this.m20 = a;
      this.m22 = b;
      a = this.m30 * cs + this.m32 * sn;
      b = this.m30 * sp + this.m32 * cs;
      this.m30 = a;
      this.m32 = b;
   }

   public void rotateZ(float angle) {
      float c = Mth.cos(angle);
      float s = Mth.sin(angle);
      float t = 1.0F - c;
      this.apply(c, -s, 0.0F, 0.0F, s, c, 0.0F, 0.0F, 0.0F, 0.0F, t + c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void rotateZZ(float angle) {
      float cs = Mth.cos(angle);
      float sp = Mth.sin(angle);
      float sn = -sp;
      float a = this.m00 * cs + this.m01 * sp;
      float b = this.m00 * sn + this.m01 * cs;
      this.m00 = a;
      this.m01 = b;
      a = this.m10 * cs + this.m11 * sp;
      b = this.m10 * sn + this.m11 * cs;
      this.m10 = a;
      this.m11 = b;
      a = this.m20 * cs + this.m21 * sp;
      b = this.m20 * sn + this.m21 * cs;
      this.m20 = a;
      this.m21 = b;
      a = this.m30 * cs + this.m31 * sp;
      b = this.m30 * sn + this.m31 * cs;
      this.m30 = a;
      this.m31 = b;
   }

   public void apply(float n00, float n01, float n02, float n03, float n10, float n11, float n12, float n13, float n20, float n21, float n22, float n23, float n30, float n31, float n32, float n33) {
      float r00 = this.m00 * n00 + this.m01 * n10 + this.m02 * n20 + this.m03 * n30;
      float r01 = this.m00 * n01 + this.m01 * n11 + this.m02 * n21 + this.m03 * n31;
      float r02 = this.m00 * n02 + this.m01 * n12 + this.m02 * n22 + this.m03 * n32;
      float r03 = this.m00 * n03 + this.m01 * n13 + this.m02 * n23 + this.m03 * n33;
      float r10 = this.m10 * n00 + this.m11 * n10 + this.m12 * n20 + this.m13 * n30;
      float r11 = this.m10 * n01 + this.m11 * n11 + this.m12 * n21 + this.m13 * n31;
      float r12 = this.m10 * n02 + this.m11 * n12 + this.m12 * n22 + this.m13 * n32;
      float r13 = this.m10 * n03 + this.m11 * n13 + this.m12 * n23 + this.m13 * n33;
      float r20 = this.m20 * n00 + this.m21 * n10 + this.m22 * n20 + this.m23 * n30;
      float r21 = this.m20 * n01 + this.m21 * n11 + this.m22 * n21 + this.m23 * n31;
      float r22 = this.m20 * n02 + this.m21 * n12 + this.m22 * n22 + this.m23 * n32;
      float r23 = this.m20 * n03 + this.m21 * n13 + this.m22 * n23 + this.m23 * n33;
      float r30 = this.m30 * n00 + this.m31 * n10 + this.m32 * n20 + this.m33 * n30;
      float r31 = this.m30 * n01 + this.m31 * n11 + this.m32 * n21 + this.m33 * n31;
      float r32 = this.m30 * n02 + this.m31 * n12 + this.m32 * n22 + this.m33 * n32;
      float r33 = this.m30 * n03 + this.m31 * n13 + this.m32 * n23 + this.m33 * n33;
      this.m00 = r00;
      this.m01 = r01;
      this.m02 = r02;
      this.m03 = r03;
      this.m10 = r10;
      this.m11 = r11;
      this.m12 = r12;
      this.m13 = r13;
      this.m20 = r20;
      this.m21 = r21;
      this.m22 = r22;
      this.m23 = r23;
      this.m30 = r30;
      this.m31 = r31;
      this.m32 = r32;
      this.m33 = r33;
   }

   public void translate(float tx, float ty, float tz) {
      this.m03 += tx * this.m00 + ty * this.m01 + tz * this.m02;
      this.m13 += tx * this.m10 + ty * this.m11 + tz * this.m12;
      this.m23 += tx * this.m20 + ty * this.m21 + tz * this.m22;
      this.m33 += tx * this.m30 + ty * this.m31 + tz * this.m32;
   }

   public float getScaleX() {
      return Mth.sqrt(this.m00 * this.m00 + this.m01 * this.m01 + this.m02 * this.m02);
   }

   public float getScaleY() {
      return Mth.sqrt(this.m10 * this.m10 + this.m11 * this.m11 + this.m12 * this.m12);
   }

   public float getScaleZ() {
      return Mth.sqrt(this.m20 * this.m20 + this.m21 * this.m21 + this.m22 * this.m22);
   }

   public void scale(float x, float y, float z) {
      this.m00 *= x;
      this.m01 *= y;
      this.m02 *= z;
      this.m10 *= x;
      this.m11 *= y;
      this.m12 *= z;
      this.m20 *= x;
      this.m21 *= y;
      this.m22 *= z;
      this.m30 *= x;
      this.m31 *= y;
      this.m32 *= z;
   }

   public final Vec3 transform(Vec3 vec) {
      double x = (double)this.m00 * vec.x + (double)this.m01 * vec.y + (double)this.m02 * vec.z + (double)this.m03;
      double y = (double)this.m10 * vec.x + (double)this.m11 * vec.y + (double)this.m12 * vec.z + (double)this.m13;
      double z = (double)this.m20 * vec.x + (double)this.m21 * vec.y + (double)this.m22 * vec.z + (double)this.m23;
      vec.x = x;
      vec.y = y;
      vec.z = z;
      return vec;
   }

   public final Vec3 transform010(Vec3 vec) {
      vec.x = (double)(this.m01 + this.m03);
      vec.y = (double)(this.m11 + this.m13);
      vec.z = (double)(this.m21 + this.m23);
      return vec;
   }

   public Vec3 transformOnlyRotation(Vec3 vec) {
      double x = (double)this.m00 * vec.x + (double)this.m01 * vec.y + (double)this.m02 * vec.z;
      double y = (double)this.m10 * vec.x + (double)this.m11 * vec.y + (double)this.m12 * vec.z;
      double z = (double)this.m20 * vec.x + (double)this.m21 * vec.y + (double)this.m22 * vec.z;
      vec.x = x;
      vec.y = y;
      vec.z = z;
      return vec;
   }

   public float determinant() {
      float f = this.m00 * (this.m11 * this.m22 * this.m33 + this.m12 * this.m23 * this.m31 + this.m13 * this.m21 * this.m32 - this.m13 * this.m22 * this.m31 - this.m11 * this.m23 * this.m32 - this.m12 * this.m21 * this.m33);
      f -= this.m01 * (this.m10 * this.m22 * this.m33 + this.m12 * this.m23 * this.m30 + this.m13 * this.m20 * this.m32 - this.m13 * this.m22 * this.m30 - this.m10 * this.m23 * this.m32 - this.m12 * this.m20 * this.m33);
      f += this.m02 * (this.m10 * this.m21 * this.m33 + this.m11 * this.m23 * this.m30 + this.m13 * this.m20 * this.m31 - this.m13 * this.m21 * this.m30 - this.m10 * this.m23 * this.m31 - this.m11 * this.m20 * this.m33);
      f -= this.m03 * (this.m10 * this.m21 * this.m32 + this.m11 * this.m22 * this.m30 + this.m12 * this.m20 * this.m31 - this.m12 * this.m21 * this.m30 - this.m10 * this.m22 * this.m31 - this.m11 * this.m20 * this.m32);
      return f;
   }

   public MatrixBase createInvert(MatrixBase dst) {
      float determinant = this.determinant();
      if (determinant == 0.0F) {
         return null;
      } else {
         float determinant_inv = 1.0F / determinant;
         float t00 = this.m11 * (this.m22 * this.m33 - this.m23 * this.m32) + this.m12 * (this.m23 * this.m31 - this.m21 * this.m33) + this.m13 * (this.m21 * this.m32 - this.m22 * this.m31);
         float t01 = -(this.m10 * (this.m22 * this.m33 - this.m23 * this.m32) + this.m12 * (this.m23 * this.m30 - this.m20 * this.m33) + this.m13 * (this.m20 * this.m32 - this.m22 * this.m30));
         float t02 = this.m10 * (this.m21 * this.m33 - this.m23 * this.m31) + this.m11 * (this.m23 * this.m30 - this.m20 * this.m33) + this.m13 * (this.m20 * this.m31 - this.m21 * this.m30);
         float t03 = -(this.m10 * (this.m21 * this.m32 - this.m22 * this.m31) + this.m11 * (this.m22 * this.m30 - this.m20 * this.m32) + this.m12 * (this.m20 * this.m31 - this.m21 * this.m30));
         float t10 = -(this.m01 * (this.m22 * this.m33 - this.m23 * this.m32) + this.m02 * (this.m23 * this.m31 - this.m21 * this.m33) + this.m03 * (this.m21 * this.m32 - this.m22 * this.m31));
         float t11 = this.m00 * (this.m22 * this.m33 - this.m23 * this.m32) + this.m02 * (this.m23 * this.m30 - this.m20 * this.m33) + this.m03 * (this.m20 * this.m32 - this.m22 * this.m30);
         float t12 = -(this.m00 * (this.m21 * this.m33 - this.m23 * this.m31) + this.m01 * (this.m23 * this.m30 - this.m20 * this.m33) + this.m03 * (this.m20 * this.m31 - this.m21 * this.m30));
         float t13 = this.m00 * (this.m21 * this.m32 - this.m22 * this.m31) + this.m01 * (this.m22 * this.m30 - this.m20 * this.m32) + this.m02 * (this.m20 * this.m31 - this.m21 * this.m30);
         float t20 = this.m01 * (this.m12 * this.m33 - this.m13 * this.m32) + this.m02 * (this.m13 * this.m31 - this.m11 * this.m33) + this.m03 * (this.m11 * this.m32 - this.m12 * this.m31);
         float t21 = -(this.m00 * (this.m12 * this.m33 - this.m13 * this.m32) + this.m02 * (this.m13 * this.m30 - this.m10 * this.m33) + this.m03 * (this.m10 * this.m32 - this.m12 * this.m30));
         float t22 = this.m00 * (this.m11 * this.m33 - this.m13 * this.m31) + this.m01 * (this.m13 * this.m30 - this.m10 * this.m33) + this.m03 * (this.m10 * this.m31 - this.m11 * this.m30);
         float t23 = -(this.m00 * (this.m11 * this.m32 - this.m12 * this.m31) + this.m01 * (this.m12 * this.m30 - this.m10 * this.m32) + this.m02 * (this.m10 * this.m31 - this.m11 * this.m30));
         float t30 = -(this.m01 * (this.m12 * this.m23 - this.m13 * this.m22) + this.m02 * (this.m13 * this.m21 - this.m11 * this.m23) + this.m03 * (this.m11 * this.m22 - this.m12 * this.m21));
         float t31 = this.m00 * (this.m12 * this.m23 - this.m13 * this.m22) + this.m02 * (this.m13 * this.m20 - this.m10 * this.m23) + this.m03 * (this.m10 * this.m22 - this.m12 * this.m20);
         float t32 = -(this.m00 * (this.m11 * this.m23 - this.m13 * this.m21) + this.m01 * (this.m13 * this.m20 - this.m10 * this.m23) + this.m03 * (this.m10 * this.m21 - this.m11 * this.m20));
         float t33 = this.m00 * (this.m11 * this.m22 - this.m12 * this.m21) + this.m01 * (this.m12 * this.m20 - this.m10 * this.m22) + this.m02 * (this.m10 * this.m21 - this.m11 * this.m20);
         dst.m00 = t00 * determinant_inv;
         dst.m11 = t11 * determinant_inv;
         dst.m22 = t22 * determinant_inv;
         dst.m33 = t33 * determinant_inv;
         dst.m01 = t10 * determinant_inv;
         dst.m10 = t01 * determinant_inv;
         dst.m20 = t02 * determinant_inv;
         dst.m02 = t20 * determinant_inv;
         dst.m12 = t21 * determinant_inv;
         dst.m21 = t12 * determinant_inv;
         dst.m03 = t30 * determinant_inv;
         dst.m30 = t03 * determinant_inv;
         dst.m13 = t31 * determinant_inv;
         dst.m31 = t13 * determinant_inv;
         dst.m32 = t23 * determinant_inv;
         dst.m23 = t32 * determinant_inv;
         return dst;
      }
   }

   public void copyTo(MatrixBase dst) {
      dst.m00 = this.m00;
      dst.m01 = this.m01;
      dst.m02 = this.m02;
      dst.m03 = this.m03;
      dst.m10 = this.m10;
      dst.m11 = this.m11;
      dst.m12 = this.m12;
      dst.m13 = this.m13;
      dst.m20 = this.m20;
      dst.m21 = this.m21;
      dst.m22 = this.m22;
      dst.m23 = this.m23;
      dst.m30 = this.m30;
      dst.m31 = this.m31;
      dst.m32 = this.m32;
      dst.m33 = this.m33;
   }

   public void shift(float tx, float ty, float tz) {
      this.m00 += this.m30 * tx;
      this.m01 += this.m31 * tx;
      this.m02 += this.m32 * tx;
      this.m03 += this.m33 * tx;
      this.m10 += this.m30 * ty;
      this.m11 += this.m31 * ty;
      this.m12 += this.m32 * ty;
      this.m13 += this.m33 * ty;
      this.m20 += this.m30 * tz;
      this.m21 += this.m31 * tz;
      this.m22 += this.m32 * tz;
      this.m23 += this.m33 * tz;
   }

   public static MatrixBase mul(MatrixBase left, MatrixBase right, MatrixBase dest) {
      float m00 = left.m00 * 1.0F + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
      float m01 = left.m01 * 1.0F + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
      float m02 = left.m02 * 1.0F + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
      float m03 = left.m03 * 1.0F + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
      float m10 = left.m00 * right.m10 + left.m10 * 1.0F + left.m20 * right.m12 + left.m30 * right.m13;
      float m11 = left.m01 * right.m10 + left.m11 * 1.0F + left.m21 * right.m12 + left.m31 * right.m13;
      float m12 = left.m02 * right.m10 + left.m12 * 1.0F + left.m22 * right.m12 + left.m32 * right.m13;
      float m13 = left.m03 * right.m10 + left.m13 * 1.0F + left.m23 * right.m12 + left.m33 * right.m13;
      float m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * 1.0F + left.m30 * right.m23;
      float m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * 1.0F + left.m31 * right.m23;
      float m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * 1.0F + left.m32 * right.m23;
      float m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * 1.0F + left.m33 * right.m23;
      float m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * 1.0F;
      float m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * 1.0F;
      float m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * 1.0F;
      float m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * 1.0F;
      dest.m00 = m00;
      dest.m01 = m01;
      dest.m02 = m02;
      dest.m03 = m03;
      dest.m10 = m10;
      dest.m11 = m11;
      dest.m12 = m12;
      dest.m13 = m13;
      dest.m20 = m20;
      dest.m21 = m21;
      dest.m22 = m22;
      dest.m23 = m23;
      dest.m30 = m30;
      dest.m31 = m31;
      dest.m32 = m32;
      dest.m33 = m33;
      return dest;
   }
}
