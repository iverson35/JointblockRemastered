package jp.mc.ancientred.jointblock.assemble.ai;

import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class JBPathNavigate {
   public static final int WIDTH_RANGE = 1;
   public static final float PATH_SEARCH_RANGE = 32.0F;
   private EntityJointed theEntity;
   private JBPathFinder pathFinder;
   public JBPathEntity currentPath;
   public double curdestX;
   public double curdestY;
   public double curdestZ;
   public boolean destinationUpdated;
   public int lastPathFindingETick;
   private int processedCurPathTicks;
   private int ticksAtLastPos;
   private Vec3 vecWork = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private Vec3 vecEntityPos = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private Vec3 lastPosCheck = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);

   public JBPathNavigate(EntityJointed ej) {
      this.theEntity = ej;
      this.pathFinder = JBPathFinder.tHreadPathFinder.get();
      this.lastPathFindingETick = 0;
   }

   public boolean tryMoveToTarget(EjTarget target) {
      if (!target.isTargetEntity) {
         return this.setPath(this.getPathToXYZ((double)target.worldPosX, (double)target.worldPosY, (double)target.worldPosZ));
      } else {
         return target.targetEntity != null ? this.setPath(this.getPathToEntity(target.targetEntity)) : false;
      }
   }

   public boolean tryMoveToXYZ(double dstX, double dstY, double dstZ) {
      return this.setPath(this.getPathToXYZ((double)Mth.floor(dstX), (double)((int)dstY), (double)Mth.floor(dstZ)));
   }

   public boolean tryMoveToEntity(Entity entityDst) {
      return this.setPath(this.getPathToEntity(entityDst));
   }

   public JBPathEntity getPathToXYZ(double dstX, double dstY, double dstZ) {
      if (!this.canSearchNewPath()) {
         return null;
      } else {
         this.lastPathFindingETick = this.theEntity.tickCount;
         return this.pathFinder.createEntityPathTo(this.theEntity, (double)Mth.floor(dstX) + 0.5, (double)((int)dstY) + 0.5, (double)Mth.floor(dstZ) + 0.5, 32.0F);
      }
   }

   public JBPathEntity getPathToEntity(Entity entityTarget) {
      if (!this.canSearchNewPath()) {
         return null;
      } else {
         this.lastPathFindingETick = this.theEntity.tickCount;
         return this.pathFinder.createEntityPathTo(this.theEntity, entityTarget.getX(), entityTarget.getBoundingBox().minY, entityTarget.getZ(), 32.0F);
      }
   }

   private boolean canSearchNewPath() {
      if (this.canSearchPath() && this.pathFinder != null) {
         int tickSpan = this.theEntity.tickCount - this.lastPathFindingETick;
         return tickSpan < 0 || tickSpan > 40;
      } else {
         return false;
      }
   }

   private boolean setPath(JBPathEntity path) {
      this.currentPath = path;
      if (this.currentPath != null && this.currentPath.points.length != 0) {
         this.processedCurPathTicks = 0;
         this.ticksAtLastPos = this.processedCurPathTicks;
         this.lastPosCheck.x = this.theEntity.getX();
         this.lastPosCheck.y = this.theEntity.getY();
         this.lastPosCheck.z = this.theEntity.getZ();
         return true;
      } else {
         this.currentPath = null;
         return false;
      }
   }

   public boolean noPath() {
      return this.currentPath == null || this.currentPath.isFinished();
   }

   public JBPathEntity getPath() {
      return this.currentPath;
   }

   public void onUpdateNavigation() {
      ++this.processedCurPathTicks;
      if (!this.noPath()) {
         this.pathFollow();
         if (!this.noPath()) {
            JBPathPoint pp = this.currentPath.getCurrentPathPoint();
            if (pp != null) {
               this.curdestX = (double)pp.posX + 0.5;
               this.curdestY = (double)pp.posY;
               this.curdestZ = (double)pp.posZ + 0.5;
               this.destinationUpdated = true;
            }
         }
      }
   }

   private void pathFollow() {
      this.vecEntityPos.x = this.theEntity.getX();
      this.vecEntityPos.y = this.theEntity.getY();
      this.vecEntityPos.z = this.theEntity.getZ();
      int entityY = (int)this.vecEntityPos.y;
      boolean isEntityFlyType = false;
      double yRangeAsNear = 1.96;
      int entitYSize = Mth.ceil(this.theEntity.totalHeight) - 1;
      if (this.theEntity.unitAbility.isFlyable()) {
         isEntityFlyType = true;
         yRangeAsNear = 25.0;
         ++entitYSize;
      } else if (this.theEntity.unitAbility.isHoverable()) {
         isEntityFlyType = true;
         yRangeAsNear = 25.0;
         entitYSize += 5;
      }

      int pathCheckLen = this.currentPath.points.length;
      int pIdx;
      int py;
      if (isEntityFlyType) {
         for (pIdx = this.currentPath.currentPathIndex; pIdx < pathCheckLen; ++pIdx) {
            py = this.currentPath.getPathPointFromIndex(pIdx).posY;
            if (py > entityY) {
               pathCheckLen = pIdx;
               break;
            }
         }
      } else {
         for (pIdx = this.currentPath.currentPathIndex; pIdx < pathCheckLen; ++pIdx) {
            py = this.currentPath.getPathPointFromIndex(pIdx).posY;
            if (py != entityY && py != entityY - 1) {
               pathCheckLen = pIdx;
               break;
            }
         }
      }

      double widthHSq = 0.26666666666666666;
      widthHSq *= widthHSq;

      for (int pIdx2 = this.currentPath.currentPathIndex; pIdx2 < pathCheckLen; ++pIdx2) {
         JBPathPoint pp = this.currentPath.getPathPointFromIndex(pIdx2);
         double dX = (double)pp.posX + 0.5 - this.vecEntityPos.x;
         double dZ = (double)pp.posZ + 0.5 - this.vecEntityPos.z;
         double dY = (double)pp.posY - this.vecEntityPos.y;
         if (dX * dX + dZ * dZ < widthHSq && dY * dY < yRangeAsNear) {
            this.currentPath.currentPathIndex = pIdx2 + 1;
         }
      }

      BlockPos.MutableBlockPos mBPos = null;

      for (int pIdx3 = pathCheckLen - 1; pIdx3 >= this.currentPath.currentPathIndex; --pIdx3) {
         JBPathPoint pp = this.currentPath.getPathPointFromIndex(pIdx3);
         this.vecWork.x = (double)pp.posX + 0.5;
         this.vecWork.y = (double)pp.posY;
         this.vecWork.z = (double)pp.posZ + 0.5;
         if (mBPos == null) {
            mBPos = new BlockPos.MutableBlockPos();
         }

         if (isDirectPathBetweenPoints(this.theEntity, this.vecEntityPos, this.vecWork, isEntityFlyType, entitYSize, mBPos)) {
            this.currentPath.currentPathIndex = pIdx3;
            break;
         }
      }

      this.checkPathTickExceed(this.vecEntityPos);
   }

   protected void checkPathTickExceed(Vec3 vEntityPos) {
      if (this.processedCurPathTicks - this.ticksAtLastPos > 100) {
         if (vEntityPos.distanceToSqr(this.lastPosCheck) < 2.25) {
            this.clearPathEntity();
         }

         this.ticksAtLastPos = this.processedCurPathTicks;
         this.lastPosCheck.x = vEntityPos.x;
         this.lastPosCheck.y = vEntityPos.y;
         this.lastPosCheck.z = vEntityPos.z;
      }
   }

   public boolean checkDestinationExceed(EjTarget target) {
      if (this.processedCurPathTicks > 40 && !this.currentPath.isDestinationInTargetRange(target, 3.0)) {
         this.clearPathEntity();
         return false;
      } else {
         return true;
      }
   }

   public void clearPathEntity() {
      this.currentPath = null;
   }

   private boolean canSearchPath() {
      return !this.theEntity.unitAbility.isFlyable() && !this.theEntity.unitAbility.isHoverable() ? this.theEntity.onGround() : true;
   }

   private boolean isInLiquid() {
      return this.theEntity.isInWater() || this.theEntity.isInLava();
   }

   private static boolean isDirectPathBetweenPoints(EntityJointed ej, Vec3 posEntityNow, Vec3 posDst, boolean isEntityFlyType, int entityYSize, BlockPos.MutableBlockPos mBPos) {
      Level world = ej.level();
      int xCurInt = Mth.floor(posEntityNow.x);
      int yCurInt = (int)posEntityNow.y;
      int zCurInt = Mth.floor(posEntityNow.z);
      boolean avoidOverWater = true;
      if (isEntityFlyType) {
         yCurInt = (int)posDst.y;
         avoidOverWater = false;
      }

      int xDstInt = Mth.floor(posDst.x);
      int zDstInt = Mth.floor(posDst.z);
      double distX = posDst.x - posEntityNow.x;
      double distZ = posDst.z - posEntityNow.z;
      double dist = distX * distX + distZ * distZ;
      if (dist < 1.0E-8) {
         return false;
      } else {
         double mgnDiv = 1.0 / Math.sqrt(dist);
         double distXNorm = distX * mgnDiv;
         double distZNorm = distZ * mgnDiv;
         if (!isSafeToStandAt(world, ej, xCurInt, yCurInt, zCurInt, entityYSize, posEntityNow, distXNorm, distZNorm, avoidOverWater, mBPos)) {
            return false;
         } else {
            double d4 = 1.0 / Math.abs(distXNorm);
            double d5 = 1.0 / Math.abs(distZNorm);
            double difX = (double)xCurInt - posEntityNow.x;
            double difZ = (double)zCurInt - posEntityNow.z;
            if (distXNorm >= 0.0) {
               ++difX;
            }

            if (distZNorm >= 0.0) {
               ++difZ;
            }

            difX /= distXNorm;
            difZ /= distZNorm;
            int xWay = distXNorm < 0.0 ? -1 : 1;
            int zWay = distZNorm < 0.0 ? -1 : 1;
            int diffXInt = xDstInt - xCurInt;
            int diffZInt = zDstInt - zCurInt;

            do {
               if (diffXInt * xWay <= 0 && diffZInt * zWay <= 0) {
                  return true;
               }

               if (difX < difZ) {
                  difX += d4;
                  xCurInt += xWay;
                  diffXInt = xDstInt - xCurInt;
               } else {
                  difZ += d5;
                  zCurInt += zWay;
                  diffZInt = zDstInt - zCurInt;
               }
            } while (isSafeToStandAt(world, ej, xCurInt, yCurInt, zCurInt, entityYSize, posEntityNow, distXNorm, distZNorm, avoidOverWater, mBPos));

            return false;
         }
      }
   }

   private static boolean isSafeToStandAt(Level world, EntityJointed ej, int posX, int posY, int posZ, int entitYSize, Vec3 posEntityNow, double distXNorm, double distZNorm, boolean avoidOverWater, BlockPos.MutableBlockPos mBPos) {
      double diffX = (double)posX + 0.5 - posEntityNow.x;
      double diffZ = (double)posZ + 0.5 - posEntityNow.z;
      if (diffX * distXNorm + diffZ * distZNorm >= 0.0) {
         BlockState bState = WorldHelper.getBlockState(world, posX, posY - 1, posZ);
         if (bState.isAir()) {
            return false;
         }

         if (avoidOverWater && bState.getFluidState().is(FluidTags.WATER)) {
            return false;
         }

         if (bState.getFluidState().is(FluidTags.LAVA)) {
            return false;
         }

         int heightMapY = world.getMaxBuildHeight() - 1;

         for (int y = posY, yTo = posY + entitYSize; y <= yTo && heightMapY > y; ++y) {
            BlockState bStateAt = WorldHelper.getBlockState(world, posX, y, posZ);
            if (!bStateAt.isPathfindable(world, mBPos.set(posX, y, posZ), PathComputationType.LAND)) {
               return false;
            }
         }
      }

      return true;
   }
}
