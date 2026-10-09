package jp.mc.ancientred.jointblock.assemble.ai;

import java.util.Arrays;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.PathComputationType;

public final class JBPathFinder {
   public static ThreadLocal<JBPathFinder> tHreadPathFinder = new ThreadLocal<JBPathFinder>() {
      protected JBPathFinder initialValue() {
         return new JBPathFinder();
      }
   };
   private PriorityPathPointQueue pathPriorityQueue = new PriorityPathPointQueue();
   private ComputedPointMap pointMap = new ComputedPointMap();
   private JBPathPoint[] pathOptions = new JBPathPoint[6];
   private int entityYHeight = 0;
   private int entityYHeightActual = 0;
   private int pPoolPointer = 0;
   private int pPoolSize = 200;
   private JBPathPoint[] pPool = new JBPathPoint[200];
   private BlockPos.MutableBlockPos mBPos = new BlockPos.MutableBlockPos();
   private static final int[] OPT_OFFSET_X = new int[]{0, -1, 1, 0};
   private static final int[] OPT_OFFSET_Z = new int[]{1, 0, 0, -1};
   private static final int COLLIDE_SOLID_BLOCKED = 0;
   private static final int COLLIDE_WAY_CLEARED = 1;
   private static final int COLLIDE_WAY_CLEARED_EX = 2;
   private static final int COLLIDE_LAVA = -2;
   private static final int COLLIDE_SOLID_FENCE = -3;
   private static final int COLLIDE_SOLID_TRAPDOOR = -4;

   protected JBPathEntity createEntityPathTo(EntityJointed entitySelf, double targetX, double targetY, double targetZ, float searchRange) {
      this.pPoolPointer = 0;
      this.pointMap.clearMap();
      LevelChunk[] chunkStoreAry = new LevelChunk[256];
      boolean isFlyType = false;
      this.entityYHeightActual = Mth.ceil(entitySelf.totalHeight);
      this.entityYHeight = this.entityYHeightActual - 1;
      if (entitySelf.unitAbility.isFlyable()) {
         ++this.entityYHeight;
         isFlyType = true;
      } else if (entitySelf.unitAbility.isHoverable()) {
         this.entityYHeight += 5;
         isFlyType = true;
      }

      int tx = Mth.floor(targetX);
      int ty = Mth.floor(targetY);
      int tz = Mth.floor(targetZ);
      JBPathPoint targetPoint = this.openPoint(tx, ty, tz, this.pointMap);
      JBPathPoint startPoint = this.openPoint(Mth.floor(entitySelf.getX()), Mth.floor(entitySelf.getBoundingBox().minY + 0.5), Mth.floor(entitySelf.getZ()), this.pointMap);
      startPoint.totalPathDistFromStart = 0.0F;
      startPoint.distToTargetPoint = startPoint.distanceToSquared(targetPoint);
      startPoint.costDistToTarget = startPoint.distToTargetPoint;
      this.pathPriorityQueue.clearPath();
      this.pathPriorityQueue.addPoint(startPoint);
      JBPathPoint openedPointNearestToTgt = startPoint;
      int searchRangeSq = (int)searchRange * (int)searchRange;

      while (!this.pathPriorityQueue.isPathEmpty()) {
         JBPathPoint processingPoint = this.pathPriorityQueue.dequeue();
         if (processingPoint.equals(targetPoint)) {
            return this.createEntityPath(entitySelf, startPoint, targetPoint);
         }

         if (processingPoint.distanceToSquared(targetPoint) < openedPointNearestToTgt.distanceToSquared(targetPoint)) {
            openedPointNearestToTgt = processingPoint;
         }

         processingPoint.hasSearchedOption = true;
         int optionFoundNum = this.findPathOptions(entitySelf, processingPoint, tx, ty, tz, searchRangeSq, chunkStoreAry, isFlyType);

         for (int i = 0; i < optionFoundNum; ++i) {
            JBPathPoint pathpointOption = this.pathOptions[i];
            float costToOptionPoint = processingPoint.totalPathDistFromStart + processingPoint.distanceToSquared(pathpointOption);
            if (!pathpointOption.isAssigned() || costToOptionPoint < pathpointOption.totalPathDistFromStart) {
               pathpointOption.previous = processingPoint;
               pathpointOption.totalPathDistFromStart = costToOptionPoint;
               pathpointOption.distToTargetPoint = pathpointOption.distanceToSquared(targetPoint);
               if (pathpointOption.isAssigned()) {
                  this.pathPriorityQueue.changeDistance(pathpointOption, pathpointOption.totalPathDistFromStart + pathpointOption.distToTargetPoint);
               } else {
                  pathpointOption.costDistToTarget = pathpointOption.totalPathDistFromStart + pathpointOption.distToTargetPoint;
                  this.pathPriorityQueue.addPoint(pathpointOption);
               }
            }
         }
      }

      if (openedPointNearestToTgt == startPoint) {
         return null;
      } else {
         return this.createEntityPath(entitySelf, startPoint, openedPointNearestToTgt);
      }
   }

   private JBPathEntity createEntityPath(EntityJointed entitySelf, JBPathPoint startPoint, JBPathPoint endPoint) {
      int i = 0;

      JBPathPoint pp;
      for (pp = endPoint; pp.previous != null; pp = pp.previous) {
         ++i;
      }

      JBPathPoint[] pathPointArray = new JBPathPoint[i + 1];
      pp = endPoint;

      for (pathPointArray[i] = endPoint; pp.previous != null; pathPointArray[i] = pp) {
         pp = pp.previous;
         --i;
      }

      return new JBPathEntity(pathPointArray, entitySelf.unitAbility.isHoverable());
   }

   private final int findPathOptions(EntityJointed entitySelf, JBPathPoint processingPoint, int tx, int ty, int tz, int searchRangeSq, LevelChunk[] chunkStoreAry, boolean isFlyType) {
      int tryCountMax = 3;
      boolean treatLiquidSolid = false;
      int startFromAboveOffset;
      if (isFlyType) {
         startFromAboveOffset = 5;
         tryCountMax = 5;
         treatLiquidSolid = true;
      } else {
         startFromAboveOffset = Mth.floor((float)entitySelf.unitAbility.stepHight);
      }

      Level level = entitySelf.level();
      int minBuildY = level.getMinBuildHeight();
      int heightMapY = level.getMaxBuildHeight() - 1;
      int idxPathOpt = 0;

      label99:
      for (int i = 0; i < 4; ++i) {
         int posX = processingPoint.posX + OPT_OFFSET_X[i];
         int posY = processingPoint.posY;
         int posZ = processingPoint.posZ + OPT_OFFSET_Z[i];
         int chunkX = posX >> 4;
         int chunkZ = posZ >> 4;
         int cStoreIdx = (chunkX + 60000000 & 15) * 16 + (chunkZ + 60000000 & 15);
         LevelChunk chunk;
         if (chunkStoreAry[cStoreIdx] != null) {
            chunk = chunkStoreAry[cStoreIdx];
         } else {
            chunk = chunkStoreAry[cStoreIdx] = level.getChunk(chunkX, chunkZ);
         }

         if (chunk != null) {
            int pathPointX = 0;
            int pathPointY = 0;
            int pathPointZ = 0;
            boolean pathPointFound = false;
            JBPathPoint openedCheck;
            int checkMax;
            int clearCnt;
            int collideResultUp;
            if ((openedCheck = this.pointMap.lookup(JBPathPoint.makeHash(posX, posY, posZ))) != null) {
               if (!openedCheck.hasSearchedOption) {
                  checkMax = tx - posX;
                  clearCnt = ty - posY;
                  collideResultUp = tz - posZ;
                  if (checkMax * checkMax + clearCnt * clearCnt + collideResultUp * collideResultUp < searchRangeSq) {
                     this.pathOptions[idxPathOpt++] = openedCheck;
                  }
               }
            } else {
               int collideResultAt = checkSizeAreaCollide(entitySelf, posX, posY, posZ, chunk, heightMapY, this.entityYHeight, treatLiquidSolid, this.mBPos);
               if (collideResultAt == 1) {
                  pathPointFound = true;
                  pathPointX = posX;
                  pathPointY = posY;
                  pathPointZ = posZ;
                  checkMax = 0;
                  clearCnt = 0;

                  while (posY > minBuildY) {
                     if (heightMapY >= posY) {
                        clearCnt = checkSizeAreaCollide(entitySelf, posX, posY - 1, posZ, chunk, heightMapY, 0, treatLiquidSolid, this.mBPos);
                        if (clearCnt != 1) {
                           break;
                        }
                     }

                     if (checkMax++ >= tryCountMax) {
                        continue label99;
                     }

                     --posY;
                     if (posY > minBuildY) {
                        pathPointFound = true;
                        pathPointX = posX;
                        pathPointY = posY;
                        pathPointZ = posZ;
                     }
                  }

                  if (clearCnt == -2) {
                     continue;
                  }
               } else if (startFromAboveOffset > 0 && collideResultAt == 0) {
                  checkMax = posY + startFromAboveOffset;
                  int clearCntUp = 0;

                  while (true) {
                     ++posY;
                     if (posY > checkMax) {
                        break;
                     }

                     if (heightMapY < posY) {
                        continue label99;
                     }

                     collideResultUp = checkSizeAreaCollide(entitySelf, posX, posY, posZ, chunk, heightMapY, 0, treatLiquidSolid, this.mBPos);
                     if (collideResultUp == 1) {
                        collideResultUp = checkSizeAreaCollide(entitySelf, posX, posY + 1, posZ, chunk, heightMapY, this.entityYHeight - 1, treatLiquidSolid, this.mBPos);
                        if (collideResultUp == 1) {
                           pathPointFound = true;
                           pathPointX = posX;
                           pathPointY = posY;
                           pathPointZ = posZ;
                           break;
                        }

                        clearCntUp = clearCntUp + 1;
                        if (clearCntUp >= this.entityYHeightActual - 1) {
                           break;
                        }
                     } else if (collideResultUp != 0) {
                        break;
                     }

                     clearCntUp = 0;
                  }
               }

               if (pathPointFound) {
                  JBPathPoint pathpointRet = this.openPoint(pathPointX, pathPointY, pathPointZ, this.pointMap);
                  if (!pathpointRet.hasSearchedOption) {
                     clearCnt = tx - posX;
                     collideResultUp = ty - posY;
                     int zDiff = tz - posZ;
                     if (clearCnt * clearCnt + collideResultUp * collideResultUp + zDiff * zDiff < searchRangeSq) {
                        this.pathOptions[idxPathOpt++] = pathpointRet;
                     }
                  }
               }
            }
         }
      }

      return idxPathOpt;
   }

   private final JBPathPoint openPoint(int posX, int posY, int posZ, ComputedPointMap pointMap) {
      int hash = JBPathPoint.makeHash(posX, posY, posZ);
      JBPathPoint pathpoint = pointMap.lookup(hash);
      if (pathpoint == null) {
         if (this.pPool[this.pPoolPointer] == null) {
            pathpoint = new JBPathPoint(posX, posY, posZ, hash);
            this.pPool[this.pPoolPointer] = pathpoint;
         } else {
            pathpoint = this.pPool[this.pPoolPointer].reUse(posX, posY, posZ, hash);
         }

         if (++this.pPoolPointer >= this.pPoolSize) {
            int newCapacity = this.pPoolSize + (this.pPoolSize >> 1);
            this.pPool = Arrays.copyOf(this.pPool, newCapacity);
            this.pPoolSize = newCapacity;
         }

         pointMap.addKey(hash, pathpoint);
      }

      return pathpoint;
   }

   private static final int checkSizeAreaCollide(EntityJointed entitySelf, int posX, int posY, int posZ, LevelChunk chunk, int heightMapY, int entityYSize, boolean treatLiquidSolid, BlockPos.MutableBlockPos bp) {
      Level world = entitySelf.level();
      if (posY < world.getMinBuildHeight()) {
         return 1;
      } else {
         for (int y = posY, yTo = posY + entityYSize; y <= yTo && heightMapY > y; ++y) {
            BlockState bState = chunk.getBlockState(bp.set(posX, y, posZ));
            Block block = bState.getBlock();
            if (!bState.isAir()) {
               if (treatLiquidSolid && !bState.getFluidState().isEmpty()) {
                  return 0;
               }

               if (!bState.isPathfindable(world, bp, PathComputationType.LAND)) {
                  if (block instanceof FenceBlock || block instanceof WallBlock || block instanceof FenceGateBlock) {
                     return -3;
                  }

                  if (block instanceof TrapDoorBlock) {
                     return -4;
                  }

                  if (!bState.getFluidState().is(FluidTags.LAVA)) {
                     return 0;
                  }

                  if (!entitySelf.isInLava) {
                     return -2;
                  }
               }
            }
         }

         return 1;
      }
   }
}
