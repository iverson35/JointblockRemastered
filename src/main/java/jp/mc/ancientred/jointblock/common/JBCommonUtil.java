package jp.mc.ancientred.jointblock.common;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JBCommonUtil {
   private static final long FNV_64_INIT = -3750763034362895579L;
   private static final long FNV_64_PRIME = 1099511628211L;
   private static final int FNV_32_INIT = -2128831035;
   private static final int FNV_32_PRIME = 16777619;

   public static short sigmoids(short value, short maxValue) {
      return (short)(maxValue * value / (maxValue + Math.abs(value)));
   }

   public static boolean jointNeedsMotionId(int jointType, int motionItemId, int motionItemMeta, int motionBindNum) {
      if (jointType == 0) {
         return true;
      } else if (!jointTypeHasMotion(jointType)) {
         return false;
      } else {
         return motionItemId != 0 || motionBindNum != 0;
      }
   }

   public static boolean jointTypeHasMotion(int jointType) {
      return jointType == 0 || jointType == 8 || jointType == 10 || jointType == 12 || jointType == 14 || jointType == 15;
   }

   public static boolean doSideRotateOnRender(int jointType) {
      return jointType == 4 || jointType == 8 || jointType == 10 || jointType == 12 || jointType == 14 || jointType == 15;
   }

   public static int getUUIDHash(Entity entity) {
      if (entity == null) {
         return 0;
      } else {
         UUID uuid = entity.getUUID();
         return getFNVHashFromUUIDBits(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
      }
   }

   private static int getFNVHashFromUUIDBits(long val1, long val2) {
      int rv = -2128831035;

      int i;
      for(i = 0; i < 8; ++i) {
         rv ^= (byte)((int)(val1 >> 8 * i));
         rv *= 16777619;
      }

      for(i = 0; i < 8; ++i) {
         rv ^= (byte)((int)(val2 >> 8 * i));
         rv *= 16777619;
      }

      return rv;
   }

   public static boolean isArrayEmptyOrNull(int[] array) {
      return array == null || array.length == 0;
   }

   public static MutableComponent makeErrorChat(String str, Object... params) {
      MutableComponent ret = Component.translatable(str, params);
      ret.withStyle(ChatFormatting.RED);
      return ret;
   }

   public static MutableComponent makeErrorChat(String str) {
      MutableComponent ret = Component.translatable(str, new Object[0]);
      ret.withStyle(ChatFormatting.RED);
      return ret;
   }

   public static MutableComponent makeNormalChat(String str, Object... params) {
      MutableComponent ret = Component.translatable(str, params);
      ret.withStyle(ChatFormatting.WHITE);
      return ret;
   }

   public static MutableComponent makeNormalChat(String str) {
      MutableComponent ret = Component.translatable(str, new Object[0]);
      ret.withStyle(ChatFormatting.WHITE);
      return ret;
   }

   public static boolean isDiableMotion(IJBJointParameterHolder.IJBJoint joint) {
      return joint instanceof JointParts ? ((JointParts)joint).isDisableMotion() : false;
   }

   public static final AABB getBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public static AABB copyBB(AABB src) {
      return getBoundingBox(src.minX, src.minY, src.minZ, src.maxX, src.maxY, src.maxZ);
   }

   public static final <T extends AABB> T offsetBB(T target, double x, double y, double z) {
      target.minX += x;
      target.minY += y;
      target.minZ += z;
      target.maxX += x;
      target.maxY += y;
      target.maxZ += z;
      return target;
   }

   public static final <T extends AABB> T multBB(T target, double val) {
      target.minX *= val;
      target.minY *= val;
      target.minZ *= val;
      target.maxX *= val;
      target.maxY *= val;
      target.maxZ *= val;
      return target;
   }

   public static <T extends AABB> T copyBBTo(T src, T dst) {
      dst.minX = src.minX;
      dst.minY = src.minY;
      dst.minZ = src.minZ;
      dst.maxX = src.maxX;
      dst.maxY = src.maxY;
      dst.maxZ = src.maxZ;
      return dst;
   }

   public static Vec3 createVectorHelper(double x, double y, double z) {
      return new Vec3(x, y, z);
   }

   public static Vec3 createVectorHelper(Vec3 arg) {
      return new Vec3(arg.x, arg.y, arg.z);
   }

   public static final boolean calculateFixedBoxIntercept(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, Vec3 startVec, Vec3 endVec, ModelViewMatrix matrix) {
      Vec3 vecRayWorkYZ2MinX = getIntermediateWithXValue(startVec, endVec, matrix.vecRayWorkYZ2MinX, minX);
      Vec3 vecRayWorkYZ2MaxX = getIntermediateWithXValue(startVec, endVec, matrix.vecRayWorkYZ2MaxX, maxX);
      Vec3 vecRayWorkXZ2MinY = getIntermediateWithYValue(startVec, endVec, matrix.vecRayWorkXZ2MinY, minY);
      Vec3 vecRayWorkXZ2MaxY = getIntermediateWithYValue(startVec, endVec, matrix.vecRayWorkXZ2MaxY, maxY);
      Vec3 vecRayWorkXY2MinZ = getIntermediateWithZValue(startVec, endVec, matrix.vecRayWorkXY2MinZ, minZ);
      Vec3 vecRayWorkXY2MaxZ = getIntermediateWithZValue(startVec, endVec, matrix.vecRayWorkXY2MaxZ, maxZ);
      if (vecRayWorkYZ2MinX == null || !(vecRayWorkYZ2MinX.y >= minY) || !(vecRayWorkYZ2MinX.y <= maxY) || !(vecRayWorkYZ2MinX.z >= minZ) || !(vecRayWorkYZ2MinX.z <= maxZ)) {
         vecRayWorkYZ2MinX = null;
      }

      if (vecRayWorkYZ2MaxX == null || !(vecRayWorkYZ2MaxX.y >= minY) || !(vecRayWorkYZ2MaxX.y <= maxY) || !(vecRayWorkYZ2MaxX.z >= minZ) || !(vecRayWorkYZ2MaxX.z <= maxZ)) {
         vecRayWorkYZ2MaxX = null;
      }

      if (vecRayWorkXZ2MinY == null || !(vecRayWorkXZ2MinY.x >= minX) || !(vecRayWorkXZ2MinY.x <= maxX) || !(vecRayWorkXZ2MinY.z >= minZ) || !(vecRayWorkXZ2MinY.z <= maxZ)) {
         vecRayWorkXZ2MinY = null;
      }

      if (vecRayWorkXZ2MaxY == null || !(vecRayWorkXZ2MaxY.x >= minX) || !(vecRayWorkXZ2MaxY.x <= maxX) || !(vecRayWorkXZ2MaxY.z >= minZ) || !(vecRayWorkXZ2MaxY.z <= maxZ)) {
         vecRayWorkXZ2MaxY = null;
      }

      if (vecRayWorkXY2MinZ == null || !(vecRayWorkXY2MinZ.x >= minX) || !(vecRayWorkXY2MinZ.x <= maxX) || !(vecRayWorkXY2MinZ.y >= minY) || !(vecRayWorkXY2MinZ.y <= maxY)) {
         vecRayWorkXY2MinZ = null;
      }

      if (vecRayWorkXY2MaxZ == null || !(vecRayWorkXY2MaxZ.x >= minX) || !(vecRayWorkXY2MaxZ.x <= maxX) || !(vecRayWorkXY2MaxZ.y >= minY) || !(vecRayWorkXY2MaxZ.y <= maxY)) {
         vecRayWorkXY2MaxZ = null;
      }

      Vec3 vecHit = null;
      if (vecRayWorkYZ2MinX != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkYZ2MinX) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkYZ2MinX;
      }

      if (vecRayWorkYZ2MaxX != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkYZ2MaxX) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkYZ2MaxX;
      }

      if (vecRayWorkXZ2MinY != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkXZ2MinY) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkXZ2MinY;
      }

      if (vecRayWorkXZ2MaxY != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkXZ2MaxY) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkXZ2MaxY;
      }

      if (vecRayWorkXY2MinZ != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkXY2MinZ) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkXY2MinZ;
      }

      if (vecRayWorkXY2MaxZ != null && (vecHit == null || startVec.distanceToSqr(vecRayWorkXY2MaxZ) < startVec.distanceToSqr(vecHit))) {
         vecHit = vecRayWorkXY2MaxZ;
      }

      if (vecHit == null) {
         return false;
      } else {
         byte hitSide = -1;
         if (vecHit == vecRayWorkYZ2MinX) {
            hitSide = 4;
         }

         if (vecHit == vecRayWorkYZ2MaxX) {
            hitSide = 5;
         }

         if (vecHit == vecRayWorkXZ2MinY) {
            hitSide = 0;
         }

         if (vecHit == vecRayWorkXZ2MaxY) {
            hitSide = 1;
         }

         if (vecHit == vecRayWorkXY2MinZ) {
            hitSide = 2;
         }

         if (vecHit == vecRayWorkXY2MaxZ) {
            hitSide = 3;
         }

         matrix.rayHitSide = hitSide;
         ModelViewMatrix.rayHitVecWork = vecHit;
         return true;
      }
   }

   public static final Vec3 getIntermediateWithXValue(Vec3 vecSrc, Vec3 vecTgt, Vec3 vecWork, double valX) {
      double xdiff = vecTgt.x - vecSrc.x;
      double ydiff = vecTgt.y - vecSrc.y;
      double zdiff = vecTgt.z - vecSrc.z;
      if (xdiff * xdiff >= 1.0000000116860974E-7) {
         double xmag = (valX - vecSrc.x) / xdiff;
         if (xmag >= 0.0 && xmag <= 1.0) {
            vecWork.x = vecSrc.x + xdiff * xmag;
            vecWork.y = vecSrc.y + ydiff * xmag;
            vecWork.z = vecSrc.z + zdiff * xmag;
            return vecWork;
         }
      }

      return null;
   }

   public static final Vec3 getIntermediateWithYValue(Vec3 vecSrc, Vec3 vecTgt, Vec3 vecWork, double valY) {
      double xdiff = vecTgt.x - vecSrc.x;
      double ydiff = vecTgt.y - vecSrc.y;
      double zdiff = vecTgt.z - vecSrc.z;
      if (ydiff * ydiff >= 1.0000000116860974E-7) {
         double ymag = (valY - vecSrc.y) / ydiff;
         if (ymag >= 0.0 && ymag <= 1.0) {
            vecWork.x = vecSrc.x + xdiff * ymag;
            vecWork.y = vecSrc.y + ydiff * ymag;
            vecWork.z = vecSrc.z + zdiff * ymag;
            return vecWork;
         }
      }

      return null;
   }

   public static final Vec3 getIntermediateWithZValue(Vec3 vecSrc, Vec3 vecTgt, Vec3 vecWork, double valZ) {
      double xdiff = vecTgt.x - vecSrc.x;
      double ydiff = vecTgt.y - vecSrc.y;
      double zdiff = vecTgt.z - vecSrc.z;
      if (zdiff * zdiff >= 1.0000000116860974E-7) {
         double zmag = (valZ - vecSrc.z) / zdiff;
         if (zmag >= 0.0 && zmag <= 1.0) {
            vecWork.x = vecSrc.x + xdiff * zmag;
            vecWork.y = vecSrc.y + ydiff * zmag;
            vecWork.z = vecSrc.z + zdiff * zmag;
            return vecWork;
         }
      }

      return null;
   }

   public static final Vec3 normalizeVec3Subtle(Vec3 vec) {
      double mag = Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
      if (mag < 1.0E-7) {
         vec.x = vec.y = vec.z = 0.0;
      } else {
         vec.x /= mag;
         vec.y /= mag;
         vec.z /= mag;
      }

      return vec;
   }

   public static final Vec3 normalizeVec3(Vec3 vec) {
      double mag = Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
      if (mag < 1.0E-4) {
         vec.x = vec.y = vec.z = 0.0;
      } else {
         vec.x /= mag;
         vec.y /= mag;
         vec.z /= mag;
      }

      return vec;
   }

   public static final Vec3 setAsNormalizeVec3(double x, double y, double z, Vec3 vec) {
      vec.x = x;
      vec.y = y;
      vec.z = z;
      return normalizeVec3(vec);
   }

   public static final Vec3 crossProductToVec(Vec3 vecA, Vec3 vecB, Vec3 toVec) {
      double x = vecA.y * vecB.z - vecA.z * vecB.y;
      double y = vecA.z * vecB.x - vecA.x * vecB.z;
      double z = vecA.x * vecB.y - vecA.y * vecB.x;
      toVec.x = x;
      toVec.y = y;
      toVec.z = z;
      return toVec;
   }

   public static void slerp(Vec3 src, Vec3 target, double alpha, Vec3 dst) {
      double dot = src.dot(target);
      if (!(dot > 0.9995) && !(dot < -0.9995)) {
         double theta0 = Math.acos(dot);
         double theta = theta0 * alpha;
         double st = Math.sin(theta);
         double tx = target.x - src.x * dot;
         double ty = target.y - src.y * dot;
         double tz = target.z - src.z * dot;
         double l2 = tx * tx + ty * ty + tz * tz;
         double dl = st * (l2 < 1.0E-4 ? 1.0 : 1.0 / Math.sqrt(l2));
         double cosTheta = Math.cos(theta);
         dst.x = src.x * cosTheta + tx * dl;
         dst.y = src.y * cosTheta + ty * dl;
         dst.z = src.z * cosTheta + tz * dl;
         nor(dst);
      } else {
         lerp(src, target, alpha, dst);
      }
   }

   public static void nor(Vec3 target) {
      double len2 = target.lengthSqr();
      if (len2 != 0.0 && len2 != 1.0) {
         double d = 1.0 / Math.sqrt(len2);
         target.x *= d;
         target.y *= d;
         target.z *= d;
      }
   }

   public static void lerp(Vec3 src, Vec3 target, double alpha, Vec3 dst) {
      dst.x = src.x + alpha * (target.x - src.x);
      dst.y = src.y + alpha * (target.y - src.y);
      dst.z = src.z + alpha * (target.z - src.z);
   }

   public static final boolean rayTraceWorldBlockHitSimple(Level world, Vec3 vecFrom, Vec3 vecTo) {
      int xTo = Mth.floor(vecTo.x);
      int yTo = Mth.floor(vecTo.y);
      int zTo = Mth.floor(vecTo.z);
      int xFr = Mth.floor(vecFrom.x);
      int yFr = Mth.floor(vecFrom.y);
      int zFr = Mth.floor(vecFrom.z);
      if (WorldHelper.getBlockState(world, xFr, yFr, zFr).canOcclude()) {
         return true;
      } else {
         int count = 200;

         do {
            if (count-- < 0) {
               return false;
            }

            if (vecFrom.x != vecFrom.x || vecFrom.y != vecFrom.y || vecFrom.z != vecFrom.z) {
               return false;
            }

            if (xFr == xTo && yFr == yTo && zFr == zTo) {
               return false;
            }

            boolean xHasDiff = true;
            boolean yHasDiff = true;
            boolean zHasDiff = true;
            double xShift = 999.0;
            double yShift = 999.0;
            double zShift = 999.0;
            if (xTo > xFr) {
               xShift = (double)xFr + 1.0;
            } else if (xTo < xFr) {
               xShift = (double)xFr + 0.0;
            } else {
               xHasDiff = false;
            }

            if (yTo > yFr) {
               yShift = (double)yFr + 1.0;
            } else if (yTo < yFr) {
               yShift = (double)yFr + 0.0;
            } else {
               yHasDiff = false;
            }

            if (zTo > zFr) {
               zShift = (double)zFr + 1.0;
            } else if (zTo < zFr) {
               zShift = (double)zFr + 0.0;
            } else {
               zHasDiff = false;
            }

            double d3 = 999.0;
            double d4 = 999.0;
            double d5 = 999.0;
            double xDiff = vecTo.x - vecFrom.x;
            double yDiff = vecTo.y - vecFrom.y;
            double zDiff = vecTo.z - vecFrom.z;
            if (xHasDiff) {
               d3 = (xShift - vecFrom.x) / xDiff;
            }

            if (yHasDiff) {
               d4 = (yShift - vecFrom.y) / yDiff;
            }

            if (zHasDiff) {
               d5 = (zShift - vecFrom.z) / zDiff;
            }

            byte b0;
            if (d3 < d4 && d3 < d5) {
               if (xTo > xFr) {
                  b0 = 4;
               } else {
                  b0 = 5;
               }

               vecFrom.x = xShift;
               vecFrom.y += yDiff * d3;
               vecFrom.z += zDiff * d3;
            } else if (d4 < d5) {
               if (yTo > yFr) {
                  b0 = 0;
               } else {
                  b0 = 1;
               }

               vecFrom.x += xDiff * d4;
               vecFrom.y = yShift;
               vecFrom.z += zDiff * d4;
            } else {
               if (zTo > zFr) {
                  b0 = 2;
               } else {
                  b0 = 3;
               }

               vecFrom.x += xDiff * d5;
               vecFrom.y += yDiff * d5;
               vecFrom.z = zShift;
            }

            xFr = Mth.floor(vecFrom.x);
            if (b0 == 5) {
               --xFr;
            }

            yFr = Mth.floor(vecFrom.y);
            if (b0 == 1) {
               --yFr;
            }

            zFr = Mth.floor(vecFrom.z);
            if (b0 == 3) {
               --zFr;
            }
         } while(!WorldHelper.getBlockState(world, xFr, yFr, zFr).canOcclude());

         return true;
      }
   }

   public static boolean isAirUp(LivingEntity entityLivingBase) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      if (entityJointed.onGround()) {
         return false;
      } else {
         return entityLivingBase.getY() - entityLivingBase.yo >= 0.0;
      }
   }

   public static boolean isAirDown(LivingEntity entityLivingBase) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      if (entityJointed.onGround()) {
         return false;
      } else {
         return entityLivingBase.getY() - entityLivingBase.yo < 0.0;
      }
   }

   public static boolean isJumpingUp(LivingEntity entityLivingBase) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      if (!entityJointed.isEntityJumping() && !entityJointed.isEntityFlying()) {
         return false;
      } else {
         return entityLivingBase.getY() - entityLivingBase.yo >= 0.0;
      }
   }

   public static boolean isJumpingDown(LivingEntity entityLivingBase) {
      EntityJointed entityJointed = (EntityJointed)entityLivingBase;
      if (!entityJointed.isEntityJumping() && !entityJointed.isEntityFlying()) {
         return false;
      } else {
         return entityLivingBase.getY() - entityLivingBase.yo < 0.0;
      }
   }

   public static void xyzBackToInitialWithAmount(JointParts joint, int backAmount) {
      int nowValue = joint.getRotationX();
      int diff;
      int fixValue;
      if (joint.fixedInitVals[0] != nowValue) {
         diff = joint.fixedInitVals[0] - nowValue;
         if (diff > 0) {
            if (backAmount >= diff) {
               fixValue = diff;
            } else {
               fixValue = backAmount;
            }
         } else if (backAmount >= -diff) {
            fixValue = diff;
         } else {
            fixValue = -backAmount;
         }

         joint.setNewRotationX(nowValue + fixValue);
      }

      nowValue = joint.getRotationY();
      if (joint.fixedInitVals[1] != nowValue) {
         diff = joint.fixedInitVals[1] - nowValue;
         if (diff > 0) {
            if (backAmount >= diff) {
               fixValue = diff;
            } else {
               fixValue = backAmount;
            }
         } else if (backAmount >= -diff) {
            fixValue = diff;
         } else {
            fixValue = -backAmount;
         }

         joint.setNewRotationY(nowValue + fixValue);
      }

      nowValue = joint.getRotationZ();
      if (joint.fixedInitVals[2] != nowValue) {
         diff = joint.fixedInitVals[2] - nowValue;
         if (diff > 0) {
            if (backAmount >= diff) {
               fixValue = diff;
            } else {
               fixValue = backAmount;
            }
         } else if (backAmount >= -diff) {
            fixValue = diff;
         } else {
            fixValue = -backAmount;
         }

         joint.setNewRotationZ(nowValue + fixValue);
      }

   }

   public static final byte int2Byte3(int x) {
      return (byte)(x >> 24);
   }

   public static final byte int2Byte2(int x) {
      return (byte)(x >> 16);
   }

   public static final byte int2Byte1(int x) {
      return (byte)(x >> 8);
   }

   public static final byte int2Byte0(int x) {
      return (byte)x;
   }

   public static final byte short2Byte1(short x) {
      return (byte)(x >> 8);
   }

   public static final byte short2Byte0(short x) {
      return (byte)x;
   }

   public static final short back2Short(byte b1, byte b0) {
      return (short)((b1 & 255) << 8 | b0 & 255);
   }

   public static final int back2Int(byte b3, byte b2, byte b1, byte b0) {
      return b3 << 24 | (b2 & 255) << 16 | (b1 & 255) << 8 | b0 & 255;
   }

   public static boolean isStringEmpty(String str) {
      return str == null || str.trim().length() == 0;
   }

   public static final byte[] createByteAryFromSoundNameString(String strName) {
      if (strName != null && strName.length() != 0) {
         if (strName.length() > 64) {
            strName = strName.substring(0, 64);
         }

         return strName.trim().getBytes(StandardCharsets.UTF_8);
      } else {
         return new byte[0];
      }
   }

   public static final byte[] createByteAryFromNameString(String strName) {
      if (strName != null && strName.length() != 0) {
         if (strName.length() > 15) {
            strName = strName.substring(0, 15);
         }

         return strName.trim().getBytes(StandardCharsets.UTF_8);
      } else {
         return new byte[0];
      }
   }

   public static final byte[] createByteAryFromMemoString(String strName) {
      if (strName != null && strName.length() != 0) {
         if (strName.length() > 5) {
            strName = strName.substring(0, 5);
         }

         return strName.trim().getBytes(StandardCharsets.UTF_8);
      } else {
         return new byte[0];
      }
   }

   public static final void writeLimitedLengthStrToBuf(String str, ByteBuf bbuf, byte limit) {
      byte[] bytes = null;
      if (!StringUtil.isNullOrEmpty(str)) {
         try {
            bytes = str.getBytes(StandardCharsets.UTF_8);
         } catch (Throwable var5) {
         }
      }

      if (bytes != null && bytes.length <= limit) {
         bbuf.writeByte(bytes.length);
         bbuf.writeBytes(bytes);
      } else {
         bbuf.writeByte(0);
      }

   }

   public static final String readLimitedLengthStrFromBuf(ByteBuf bbuf, byte limit, String defaultStr) {
      byte strLen = bbuf.readByte();
      if (strLen > 0 && strLen <= limit) {
         try {
            byte[] bytes = new byte[strLen];
            bbuf.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
         } catch (Exception var5) {
         }
      }

      return defaultStr;
   }

   public static final void writeLimitedLengthStrToBuf(String str, ByteBuffer bbuf, byte limit) {
      byte[] bytes = null;
      if (!StringUtil.isNullOrEmpty(str)) {
         try {
            bytes = str.getBytes(StandardCharsets.UTF_8);
         } catch (Throwable var5) {
         }
      }

      if (bytes != null && bytes.length <= limit) {
         bbuf.put((byte)bytes.length);
         bbuf.put(bytes);
      } else {
         bbuf.put((byte)0);
      }

   }

   public static final String readLimitedLengthStrFromBuf(ByteBuffer bbuf, byte limit, String defaultStr) {
      byte strLen = bbuf.get();
      if (strLen > 0 && strLen <= limit) {
         try {
            byte[] bytes = new byte[strLen];
            bbuf.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
         } catch (Exception var5) {
         }
      }

      return defaultStr;
   }

   public static void setAABBALL(AABB aabb, double value) {
      aabb.minX = value;
      aabb.minY = value;
      aabb.minZ = value;
      aabb.maxX = value;
      aabb.maxY = value;
      aabb.maxZ = value;
   }

   public static void setInLookRay(AABB teAABB, Vec3 pos, Vec3 look, double length) {
      teAABB.minX = pos.x + look.x * length - 0.1;
      teAABB.minY = pos.y + look.y * length - 0.1;
      teAABB.minZ = pos.z + look.z * length - 0.1;
      teAABB.maxX = pos.x + look.x * length + 0.1;
      teAABB.maxY = pos.y + look.y * length + 0.1;
      teAABB.maxZ = pos.z + look.z * length + 0.1;
   }
}
