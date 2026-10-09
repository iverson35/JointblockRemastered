package jp.mc.ancientred.jointblock.motion.common;

import java.util.Map;
import java.util.Set;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;

public class SlavePurgeHelper {
   public static final int PURGE_TICK_MIN = 1;
   public static final int SHUT_TE_RANGE_MIN = 1;
   public static final int SHUT_TE_TICK_MIN = 1;
   public static final int PURGE_TICK_MAX = 200;
   public static final int SHUT_TE_RANGE_MAX = 16;
   public static final int SHUT_TE_TICK_MAX = 200;

   public static void disableTeInRange(EntityJointed entityJointed, IJBVectorTransformer matrixUtil, int range, short teShutTick) {
      ModelViewMatrix matrix = (ModelViewMatrix)matrixUtil;
      matrix.cmnVec.x = 0.0;
      matrix.cmnVec.y = 1.0;
      matrix.cmnVec.z = 0.0;
      matrix.transformAbsolute(matrix.cmnVec);
      int minX = Mth.floor(matrix.cmnVec.x - (double)range);
      int maxX = Mth.ceil(matrix.cmnVec.x + (double)range);
      int minY = Mth.floor(matrix.cmnVec.y - (double)range);
      int maxY = Mth.ceil(matrix.cmnVec.y + (double)range);
      int minZ = Mth.floor(matrix.cmnVec.z - (double)range);
      int maxZ = Mth.ceil(matrix.cmnVec.z + (double)range);
      int xmn = Mth.floor((double)minX / 16.0);
      int xmx = Mth.floor((double)maxX / 16.0);
      int zmn = Mth.floor((double)minZ / 16.0);
      int zmx = Mth.floor((double)maxZ / 16.0);
      Level world = entityJointed.level();
      ChunkSource chunkProvider = world.getChunkSource();

      for (int cax = xmn; cax <= xmx; ++cax) {
         for (int caz = zmn; caz <= zmx; ++caz) {
            if (chunkProvider.hasChunk(cax, caz)) {
               LevelChunk chunk = world.getChunk(cax, caz);
               if (chunk == null) {
                  continue;
               }

               Set<Map.Entry<BlockPos, BlockEntity>> set = chunk.getBlockEntities().entrySet();
               if (set == null) {
                  continue;
               }

               for (Map.Entry<BlockPos, BlockEntity> entry : set) {
                  BlockEntity objValue = entry.getValue();
                  if (objValue instanceof TileEntityJointAttracter) {
                     TileEntityJointAttracter teAttracertFound = (TileEntityJointAttracter)objValue;
                     BlockPos tePos = teAttracertFound.getBlockPos();
                     if (tePos.getX() >= minX && tePos.getX() <= maxX && tePos.getY() >= minY && tePos.getY() <= maxY && tePos.getZ() >= minZ && tePos.getZ() <= maxZ) {
                        teAttracertFound.setTemporaryDisabledTick(teShutTick);
                     }
                  }
               }
            }
         }
      }

   }
}
