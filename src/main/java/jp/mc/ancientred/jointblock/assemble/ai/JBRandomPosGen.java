package jp.mc.ancientred.jointblock.assemble.ai;

import jp.mc.ancientred.jointblock.assemble.EjTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class JBRandomPosGen {
   public static EjTarget findRandomTarget(Level world, double centerX, double centerY, double centerZ, int rangeXZ, int rangeY) {
      return findRandomTargetBlock(world, centerX, centerY, centerZ, rangeXZ, rangeY, false, 0.0, 0.0, 0.0);
   }

   public static EjTarget findRandomTarget(Entity entity, int rangeXZ, int rangeY) {
      return findRandomTargetBlock(entity.level(), entity.getX(), entity.getY(), entity.getZ(), rangeXZ, rangeY, false, 0.0, 0.0, 0.0);
   }

   public static EjTarget findRandomTargetBlockTowards(Entity entity, int rangeXZ, int rangeY, double xCoord, double yCoord, double zCoord) {
      xCoord -= entity.getX();
      yCoord -= entity.getY();
      zCoord -= entity.getZ();
      return findRandomTargetBlock(entity.level(), entity.getX(), entity.getY(), entity.getZ(), rangeXZ, rangeY, true, xCoord, yCoord, zCoord);
   }

   public static EjTarget findRandomTargetBlockAwayFrom(Entity entity, int rangeXZ, int rangeY, double xCoord, double yCoord, double zCoord) {
      xCoord = entity.getX() - xCoord;
      yCoord = entity.getY() - yCoord;
      zCoord = entity.getZ() - zCoord;
      return findRandomTargetBlock(entity.level(), entity.getX(), entity.getY(), entity.getZ(), rangeXZ, rangeY, true, xCoord, yCoord, zCoord);
   }

   private static EjTarget findRandomTargetBlock(Level world, double centerX, double centerY, double centerZ, int rangeXZ, int rangeY, boolean hasV, double xCoord, double yCoord, double zCoord) {
      RandomSource random = world.random;
      boolean foundPoint = false;
      int foundX = 0;
      int foundY = 0;
      int foundZ = 0;
      float likeMax = -99999.0F;
      BlockPos.MutableBlockPos mBlockPos = new BlockPos.MutableBlockPos();

      for (int cnt = 0; cnt < 10; ++cnt) {
         int testX = random.nextInt(2 * rangeXZ) - rangeXZ;
         int testY = random.nextInt(2 * rangeY) - rangeY;
         int testZ = random.nextInt(2 * rangeXZ) - rangeXZ;
         if (!hasV || (double)testX * xCoord + (double)testZ * zCoord >= 0.1) {
            testX += Mth.floor(centerX);
            testY += Mth.floor(centerY);
            testZ += Mth.floor(centerZ);
            float like = (float)world.getMaxLocalRawBrightness(mBlockPos.set(testX, testY, testZ)) - 0.5F;
            if (like > likeMax) {
               likeMax = like;
               foundX = testX;
               foundY = testY;
               foundZ = testZ;
               foundPoint = true;
            }
         }
      }

      if (foundPoint) {
         return new EjTarget(foundX, foundY, foundZ);
      } else {
         return null;
      }
   }
}
