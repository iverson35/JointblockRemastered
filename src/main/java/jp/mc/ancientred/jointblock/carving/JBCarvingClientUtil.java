package jp.mc.ancientred.jointblock.carving;

import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.item.ItemPaintGun;
import jp.mc.ancientred.jointblock.item.ItemPatternCutter;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JBCarvingClientUtil {
   private static final int DATA_BITLEN = 4;
   private static long lastSentTimeMillis;
   private static final long EDIT_SEND_INTERVAL = 100L;

   public static void editCarveDataOnClient(Player player, TileEntityJoint te, boolean leftClick) {
      long now = System.currentTimeMillis();
      if (now - lastSentTimeMillis > 100L) {
         Level level = player.level();
         Vec3 vecPos = player.getEyePosition(1.0F);
         Vec3 vecLook = player.getViewVector(1.0F);
         double reach = 6.0;
         Vec3 velLookRay = vecPos.add(vecLook.x * reach, vecLook.y * reach, vecLook.z * reach);
         BlockPos tePos = te.getBlockPos();
         int teX = tePos.getX();
         int teY = tePos.getY();
         int teZ = tePos.getZ();
         int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
         switch (carvingType) {
            case 2:
            case 4:
            default:
               CarvingRayTraceResult res = collisionRayTrace(te.getJointFacing(), te.carvingData, level, teX, teY, teZ, vecPos, velLookRay, detectSubBoundHitType(player));
               if (res != null) {
                  JBPacketSender.sendCarvingActionToServer(teX, teY, teZ, res.boxIndex, res.getSubBoundsArg(), res.hitSide, leftClick);
                  lastSentTimeMillis = now;
               }
         }
      }

   }

   private static int detectSubBoundHitType(Player player) {
      int hitType = -1;
      ItemStack currentStack;
      if (player != null && (currentStack = CompatHelper.getCurrentEquippedItem(player)) != null) {
         if (ItemPatternCutter.isPatternArgsCutter(currentStack)) {
            hitType = 0;
         } else if (ItemPatternCutter.isPatternSideFaceCutter(currentStack)) {
            hitType = 1;
         }

         return hitType;
      } else {
         return hitType;
      }
   }

   private static ItemStack getPlayerHoldingPaintGunStack(Player player) {
      ItemStack currentStack;
      if (player != null && (currentStack = CompatHelper.getCurrentEquippedItem(player)) != null) {
         return currentStack.getItem() instanceof ItemPaintGun ? currentStack : null;
      } else {
         return null;
      }
   }

   private static CarvingRayTraceResult collisionRayTrace(int facing, int[] carvingData, Level level, int posX, int posY, int posZ, Vec3 vecPos, Vec3 velLookRay, int hitType) {
      int type = JBCarvingDataUtil.getCarvingType(carvingData);
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return null;
      } else {
         int divCountPow = divideCount * divideCount;
         AABB aabbWork = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
         double unitd = 1.0 / (double)divideCount;
         vecPos = vecPos.add((double)(-posX), (double)(-posY), (double)(-posZ));
         velLookRay = velLookRay.add((double)(-posX), (double)(-posY), (double)(-posZ));
         double rotateCenterX = 0.5;
         double rotateCenterY = 0.5;
         double rotateCenterZ = 0.5;
         switch (facing) {
            case 2:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
            case 3:
            default:
               break;
            case 4:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               break;
            case 5:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
         }

         CarvingRayTraceResult result = null;
         double disanceMin = Double.MAX_VALUE;
         int hitValue = 0;

         int y;
         for(y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;
            aabbWork.minY = (double)y * unitd;
            aabbWork.maxY = aabbWork.minY + unitd;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;
               aabbWork.minZ = (double)z * unitd;
               aabbWork.maxZ = aabbWork.minZ + unitd;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndexCnt = x + yPzCalc;
                  aabbWork.minX = (double)x * unitd;
                  aabbWork.maxX = aabbWork.minX + unitd;
                  boolean doRayTraceSubBounds = false;
                  int mod;
                  int dat;
                  switch (type) {
                     case 2:
                        mod = (boxIndexCnt & 3) << 3;
                        dat = carvingData[boxIndexCnt >> 2] >> mod & 240;
                        if (dat == 0) {
                           continue;
                        }

                        hitValue = carvingData[boxIndexCnt >> 2] >> mod & 15;
                        doRayTraceSubBounds = true;
                        break;
                     case 4:
                        mod = (boxIndexCnt & 3) << 3;
                        dat = carvingData[boxIndexCnt >> 2] >> mod & 32;
                        if (dat != 0) {
                           continue;
                        }

                        hitValue = carvingData[boxIndexCnt >> 2] >> mod & 31;
                        break;
                     default:
                        mod = (boxIndexCnt & 7) << 2;
                        dat = carvingData[boxIndexCnt >> 3] >>> mod & 15;
                        if (dat == 0) {
                           continue;
                        }
                  }

                  Vec3 vec32 = vecPos.getIntermediateWithXValue(velLookRay, aabbWork.minX);
                  Vec3 vec33 = vecPos.getIntermediateWithXValue(velLookRay, aabbWork.maxX);
                  Vec3 vec34 = vecPos.getIntermediateWithYValue(velLookRay, aabbWork.minY);
                  Vec3 vec35 = vecPos.getIntermediateWithYValue(velLookRay, aabbWork.maxY);
                  Vec3 vec36 = vecPos.getIntermediateWithZValue(velLookRay, aabbWork.minZ);
                  Vec3 vec37 = vecPos.getIntermediateWithZValue(velLookRay, aabbWork.maxZ);
                  if (!isVecInsideYZBounds(aabbWork, vec32)) {
                     vec32 = null;
                  }

                  if (!isVecInsideYZBounds(aabbWork, vec33)) {
                     vec33 = null;
                  }

                  if (!isVecInsideXZBounds(aabbWork, vec34)) {
                     vec34 = null;
                  }

                  if (!isVecInsideXZBounds(aabbWork, vec35)) {
                     vec35 = null;
                  }

                  if (!isVecInsideXYBounds(aabbWork, vec36)) {
                     vec36 = null;
                  }

                  if (!isVecInsideXYBounds(aabbWork, vec37)) {
                     vec37 = null;
                  }

                  Vec3 vec38 = null;
                  if (vec32 != null && (vec38 == null || vecPos.squareDistanceTo(vec32) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec32;
                  }

                  if (vec33 != null && (vec38 == null || vecPos.squareDistanceTo(vec33) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec33;
                  }

                  if (vec34 != null && (vec38 == null || vecPos.squareDistanceTo(vec34) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec34;
                  }

                  if (vec35 != null && (vec38 == null || vecPos.squareDistanceTo(vec35) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec35;
                  }

                  if (vec36 != null && (vec38 == null || vecPos.squareDistanceTo(vec36) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec36;
                  }

                  if (vec37 != null && (vec38 == null || vecPos.squareDistanceTo(vec37) < vecPos.squareDistanceTo(vec38))) {
                     vec38 = vec37;
                  }

                  if (vec38 != null) {
                     byte b0 = -1;
                     if (vec38 == vec32) {
                        b0 = 4;
                     }

                     if (vec38 == vec33) {
                        b0 = 5;
                     }

                     if (vec38 == vec34) {
                        b0 = 0;
                     }

                     if (vec38 == vec35) {
                        b0 = 1;
                     }

                     if (vec38 == vec36) {
                        b0 = 2;
                     }

                     if (vec38 == vec37) {
                        b0 = 3;
                     }

                     double distance = vec38.distanceTo(vecPos);
                     if (distance < disanceMin) {
                        disanceMin = distance;
                        if (result == null) {
                           result = new CarvingRayTraceResult();
                        }

                        JBCommonUtil.copyBBTo(aabbWork, result.aabb);
                        result.boxIndex = boxIndexCnt;
                        result.hitSide = b0;
                        result.commonHitValue = hitValue;
                        result.x = x;
                        result.y = y;
                        result.z = z;
                        result.rayTraceSubBounds = doRayTraceSubBounds;
                     }
                  }
               }
            }
         }

         if (result != null && result.rayTraceSubBounds) {
            JBPatternCarvingClientUtil.rayTracePatternSubBounds(carvingData, result, vecPos, velLookRay, hitType, aabbWork);
         }

         if (result != null) {
            if (result.aabb != null) {
               switch (facing) {
                  case 2:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
                  case 3:
                  default:
                     break;
                  case 4:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
                     break;
                  case 5:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               }
            }

            if (result.aabbSubBound != null && result.aabbSubBound.bbAry != null) {
               for(y = 0; y < result.aabbSubBound.bbAry.length; ++y) {
                  switch (facing) {
                     case 2:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
                     case 3:
                     default:
                        break;
                     case 4:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
                        break;
                     case 5:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
                  }
               }
            }
         }

         return result;
      }
   }

   public static boolean isVecInsideYZBounds(AABB aabb, Vec3 p_149654_1_) {
      return p_149654_1_ == null ? false : p_149654_1_.y >= aabb.minY && p_149654_1_.y <= aabb.maxY && p_149654_1_.z >= aabb.minZ && p_149654_1_.z <= aabb.maxZ;
   }

   public static boolean isVecInsideXZBounds(AABB aabb, Vec3 p_149687_1_) {
      return p_149687_1_ == null ? false : p_149687_1_.x >= aabb.minX && p_149687_1_.x <= aabb.maxX && p_149687_1_.z >= aabb.minZ && p_149687_1_.z <= aabb.maxZ;
   }

   public static boolean isVecInsideXYBounds(AABB aabb, Vec3 p_149661_1_) {
      return p_149661_1_ == null ? false : p_149661_1_.x >= aabb.minX && p_149661_1_.x <= aabb.maxX && p_149661_1_.y >= aabb.minY && p_149661_1_.y <= aabb.maxY;
   }

   static {
      lastSentTimeMillis = 0L;
   }
}
