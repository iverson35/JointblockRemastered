package jp.mc.ancientred.jointblock.entity.masterslave;

import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class MasterSlaveControl {
   public static final int SLOT_FOR_PLAYER = 0;
   public static final int MAX_SLAVE_SLOT_INDEX = 10;
   public static final int SLAVE_SLOT_NUM = 11;
   public static final double SEARCH_EXPAND = 16.0;

   public static boolean isValidTargetEntityCommon(Entity entityMaster, EntityJointed slaveJointed) {
      return entityMaster != null
         && !entityMaster.isRemoved()
         && entityMaster.getVehicle() == null
         && entityMaster.level() == slaveJointed.level()
         && (!(entityMaster instanceof EntityJointed) || !((EntityJointed)entityMaster).isSlaveMode())
         && slaveJointed.hasSameMBSHash(entityMaster);
   }

   public static void searchMasterNearBy(EntityJointed entityJointedSlave) {
      Level world = entityJointedSlave.level();
      if (!world.isClientSide) {
         AABB searchAABB = entityJointedSlave.getBoundingBox().inflate(SEARCH_EXPAND, SEARCH_EXPAND, SEARCH_EXPAND);
         List<Entity> list = world.getEntities(entityJointedSlave, searchAABB);
         Entity entityNearest = null;
         double distSqMin = Double.MAX_VALUE;
         int slaveSlotNum = entityJointedSlave.getSlaveSlotNum();
         boolean isPlayerTarget = slaveSlotNum == 0;
         MasterSlot masterSlotNearest = null;

         for (Entity entityFound : list) {
            boolean isSatisfied = false;
            MasterSlot masterSlotFound = null;
            if (isValidTargetEntityCommon(entityFound, entityJointedSlave)) {
               if (isPlayerTarget) {
                  if (entityFound instanceof Player) {
                     Player foundPlayer = (Player)entityFound;
                     masterSlotFound = MasterSlot.getFromEntity(foundPlayer, 0);
                     if (masterSlotFound != null && masterSlotFound.canBeConnectedWith(entityJointedSlave)) {
                        isSatisfied = true;
                     }
                  }
               } else if (entityFound instanceof EntityJointed) {
                  EntityJointed foundEntityJointed = (EntityJointed)entityFound;
                  if (foundEntityJointed.getMasterSlotEnableStateFlag(entityJointedSlave.getSlaveSlotNum())) {
                     masterSlotFound = MasterSlot.getFromEntity(foundEntityJointed, slaveSlotNum);
                     if (masterSlotFound != null && masterSlotFound.canBeConnectedWith(entityJointedSlave)) {
                        isSatisfied = true;
                     }
                  }
               }

               if (isSatisfied) {
                  double distSq = entityJointedSlave.distanceToSqr(entityFound);
                  if (distSq < distSqMin) {
                     distSqMin = distSq;
                     entityNearest = entityFound;
                     masterSlotNearest = masterSlotFound;
                  }
               }
            }
         }

         if (entityNearest != null) {
            if (entityNearest instanceof Player) {
               Player nearestPlayer = (Player)entityNearest;
               AbstractSlaveAttatcher.PlayerSlaveAttatcher playerSlaveAttatcher = new AbstractSlaveAttatcher.PlayerSlaveAttatcher();
               playerSlaveAttatcher.masterUUID = nearestPlayer.getUUID();
               playerSlaveAttatcher.entityMaster = nearestPlayer;
               masterSlotNearest.createConnectionWith(entityJointedSlave, playerSlaveAttatcher);
               entityJointedSlave.slaveAttatcher = playerSlaveAttatcher;
            } else if (entityNearest instanceof EntityJointed && masterSlotNearest != null) {
               EntityJointed nearestEntityJointed = (EntityJointed)entityNearest;
               AbstractSlaveAttatcher.JointSlaveAttatcher jointSlaveAttatcher = new AbstractSlaveAttatcher.JointSlaveAttatcher();
               jointSlaveAttatcher.masterUUID = nearestEntityJointed.getUUID();
               jointSlaveAttatcher.entityMaster = nearestEntityJointed;
               masterSlotNearest.createConnectionWith(entityJointedSlave, jointSlaveAttatcher);
               entityJointedSlave.slaveAttatcher = jointSlaveAttatcher;
            }
         } else {
            int xmn = Mth.floor(searchAABB.minX / 16.0);
            int xmx = Mth.floor(searchAABB.maxX / 16.0);
            int zmn = Mth.floor(searchAABB.minZ / 16.0);
            int zmx = Mth.floor(searchAABB.maxZ / 16.0);

            for (int cax = xmn; cax <= xmx; ++cax) {
               for (int caz = zmn; caz <= zmx; ++caz) {
                  LevelChunk chunk = world.getChunkSource().hasChunk(cax, caz) ? world.getChunk(cax, caz) : null;
                  if (chunk != null) {
                     Map<BlockPos, BlockEntity> blockEntityMap = chunk.getBlockEntities();
                     Set<Map.Entry<BlockPos, BlockEntity>> set = blockEntityMap != null ? blockEntityMap.entrySet() : null;
                     if (set != null) {
                        for (Map.Entry<BlockPos, BlockEntity> entry : set) {
                           BlockEntity objValue = entry.getValue();
                           if (objValue instanceof TileEntityJointAttracter) {
                              TileEntityJointAttracter teAttracertFound = (TileEntityJointAttracter)objValue;
                              MasterSlot masterSlot = teAttracertFound.masterSlot;
                              if (masterSlot.canBeConnectedWith(entityJointedSlave) && teAttracertFound.isSlaveAttractable(entityJointedSlave)) {
                                 entityJointedSlave.slaveAttatcher = new AbstractSlaveAttatcher.TileEntitySlaveAttatcher(teAttracertFound);
                                 masterSlot.createConnectionWith(entityJointedSlave, entityJointedSlave.slaveAttatcher);
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public static void setupTileEntityAttatcher(EntityJointed entityJointedSlave, TileEntityJointAttracter teAttracter) {
      MasterSlot masterSlot = teAttracter.masterSlot;
      if (masterSlot.canBeConnectedWith(entityJointedSlave) && teAttracter.isSlaveAttractable(entityJointedSlave)) {
         entityJointedSlave.slaveAttatcher = new AbstractSlaveAttatcher.TileEntitySlaveAttatcher(teAttracter);
         masterSlot.createConnectionWith(entityJointedSlave, entityJointedSlave.slaveAttatcher);
      }

   }
}
