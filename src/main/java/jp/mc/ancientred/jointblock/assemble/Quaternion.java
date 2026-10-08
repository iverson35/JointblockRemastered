package jp.mc.ancientred.jointblock.assemble;

import net.minecraft.util.Mth;

public class Quaternion {
   public float x;
   public float y;
   public float z;
   public float w;

   public Quaternion() {
      this.setIdentity();
   }

   public void setIdentity() {
      this.x = 0.0F;
      this.y = 0.0F;
      this.z = 0.0F;
      this.w = 1.0F;
   }

   public Quaternion setFromAxisAngle(double angleRad, double x, double y, double z) {
      this.x = (float)x;
      this.y = (float)y;
      this.z = (float)z;
      this.w = (float)Math.cos(0.5 * angleRad);
      float n = (float)Math.sqrt(x * x + y * y + z * z);
      if (n != 0.0F) {
         float s = (float)(Math.sin(0.5 * angleRad) / (double)n);
         this.x *= s;
         this.y *= s;
         this.z *= s;
      }

      return this;
   }

   public void setFrom(Quaternion quaternion) {
      this.x = quaternion.x;
      this.y = quaternion.y;
      this.z = quaternion.z;
      this.w = quaternion.w;
   }

   public void set(float x, float y, float z, float w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         Quaternion that = (Quaternion)obj;
         return this.x == that.x && this.y == that.y && this.z == that.z && this.w == that.w;
      } else {
         return false;
      }
   }

   public float getLength() {
      return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w));
   }

   public Quaternion normalise() {
      float length = this.getLength();
      if (length != 0.0F) {
         this.x /= length;
         this.y /= length;
         this.z /= length;
         this.w /= length;
      }

      return this;
   }

   public final float dot(Quaternion quaternion) {
      return this.x * quaternion.x + this.y * quaternion.y + this.z * quaternion.z + this.w * quaternion.w;
   }

   public static Quaternion mul(Quaternion left, Quaternion right, Quaternion dest) {
      dest.set(left.x * right.w + left.w * right.x + left.y * right.z - left.z * right.y, left.y * right.w + left.w * right.y + left.z * right.x - left.x * right.z, left.z * right.w + left.w * right.z + left.x * right.y - left.y * right.x, left.w * right.w - left.x * right.x - left.y * right.y - left.z * right.z);
      return dest;
   }

   public static Quaternion mulInverse(Quaternion left, Quaternion right, Quaternion dest) {
      float n = right.x * right.x + right.y * right.y + right.z * right.z + right.w * right.w;
      n = n == 0.0F ? n : 1.0F / n;
      dest.set((left.x * right.w - left.w * right.x - left.y * right.z + left.z * right.y) * n, (left.y * right.w - left.w * right.y - left.z * right.x + left.x * right.z) * n, (left.z * right.w - left.w * right.z - left.x * right.y + left.y * right.x) * n, (left.w * right.w + left.x * right.x + left.y * right.y + left.z * right.z) * n);
      return dest;
   }

   public float getAngleBetween(Quaternion other, Quaternion work) {
      work = mulInverse(other, this, work);
      float angle = (float)Math.toDegrees(Math.acos((double)Mth.clamp(work.w, -1.0F, -1.0F)) * 2.0);
      if (angle < -180.0F) {
         return angle + 360.0F;
      } else {
         return angle > 180.0F ? angle - 360.0F : angle;
      }
   }

   public float getRotationYAxis() {
      double sqw = (double)(this.w * this.w);
      double sqx = (double)(this.x * this.x);
      double sqy = (double)(this.y * this.y);
      double sqz = (double)(this.z * this.z);
      double sq = sqx + sqy + sqz + sqw;
      if (sq == 0.0) {
         return 0.0F;
      } else {
         double invs = 1.0 / sq;
         double m22 = (-sqx - sqy + sqz + sqw) * invs;
         double m02 = 2.0 * (double)(this.x * this.z + this.y * this.w) * invs;
         return (float)(-Math.toDegrees(Math.atan2(m02, m22)));
      }
   }

   public static Quaternion slerp(float lerp, Quaternion value1, Quaternion value2, Quaternion result) {
      return slerp(lerp, value1, value2, result, false);
   }

   public static Quaternion slerp(float lerp, Quaternion value1, Quaternion value2, Quaternion result, boolean allowExceed) {
      if (lerp < 0.0F) {
         return value1;
      } else if (!allowExceed && lerp > 1.0F) {
         return value2;
      } else {
         float dot = value1.dot(value2);
         float x2;
         float y2;
         float z2;
         float w2;
         if (dot < 0.0F) {
            dot = 0.0F - dot;
            x2 = 0.0F - value2.x;
            y2 = 0.0F - value2.y;
            z2 = 0.0F - value2.z;
            w2 = 0.0F - value2.w;
         } else {
            x2 = value2.x;
            y2 = value2.y;
            z2 = value2.z;
            w2 = value2.w;
         }

         float EPSILON = 1.0E-4F;
         float t1;
         float t2;
         if (1.0F - dot > 1.0E-4F) {
            float angle = (float)Math.acos((double)Mth.clamp(dot, -1.0F, 1.0F));
            float sinAngle = (float)Math.sin((double)angle);
            t1 = (float)Math.sin((double)((1.0F - lerp) * angle)) / sinAngle;
            t2 = (float)Math.sin((double)(lerp * angle)) / sinAngle;
         } else {
            t1 = 1.0F - lerp;
            t2 = lerp;
         }

         result.x = value1.x * t1 + x2 * t2;
         result.y = value1.y * t1 + y2 * t2;
         result.z = value1.z * t1 + z2 * t2;
         result.w = value1.w * t1 + w2 * t2;
         return result;
      }
   }
}
