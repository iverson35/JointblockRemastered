package jp.mc.ancientred.jointblock.export;

import java.util.HashMap;
import jp.mc.ancientred.jointblock.assemble.IJointListsHolder;
import jp.mc.ancientred.jointblock.assemble.JointDataSerializer;
import jp.mc.ancientred.jointblock.assemble.JointListsHolderImpl;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Rebuilds a joint block structure/entity from an exported blueprint tag, ported from the 1.12.2
 * {@code JBEntityJBFactory}.
 *
 * <p>1.20.1 semantics: the nested {@code EntityJBStructure} implemented 1.12 {@code IBlockAccess},
 * which became {@link BlockGetter} — it now also has to answer {@code getFluidState}/{@code getHeight}/
 * {@code getMinBuildHeight} (1.12 had no height accessor, so the vanilla overworld bounds are used).
 * Joint tile entities are created with {@link EntityBlock#newBlockEntity(BlockPos, BlockState)}
 * instead of 1.12 {@code ITileEntityProvider.createTileEntity(world, meta)}; the 1.20.1 block entity
 * constructor already stores its position, so the explicit {@code setPos} call is gone.
 */
public class JBEntityJBFactory {
   public static Entity creaaEntityJBFromDataTag(CompoundTag dataTag, Level world, Player entityPlayer, int posX, int posY, int posZ, int facing) {
      if (dataTag != null && !dataTag.getBoolean("jb:outdate")) {
         try {
            JointListsHolderImpl listHolder = getSerializedFromDataTag(dataTag);
            if (listHolder.serialized == null) {
               return null;
            } else {
               int partsCount = dataTag.getInt("tnm");
               JointDataSerializer.rebuildAll(listHolder.serialized, partsCount, listHolder);
               EntityJBStructure structure = buildEntityJBStructureFromDataTag(dataTag);
               TileEntityJointCore teCore = structure.getTileEntityJointCore();
               EntityJointed ej = teCore.assemble(world, structure, entityPlayer);
               float yaw = -JBRotationUtil.roatYawEachSide[facing];
               ej.moveTo((double)posX + 0.5, (double)posY + 0.02, (double)posZ + 0.5, yaw, 0.0F);
               return ej;
            }
         } catch (Throwable var13) {
            var13.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   public static JointListsHolderImpl getSerializedFromDataTag(CompoundTag dataTag) {
      JointListsHolderImpl listHolder = new JointListsHolderImpl();
      listHolder.serialized = null;
      listHolder.coreSubTag = null;
      if (dataTag.contains("ser")) {
         listHolder.serialized = dataTag.getIntArray("ser");
         if (dataTag.contains("crinf")) {
            listHolder.coreSubTag = dataTag.getCompound("crinf");
         }
      } else if (dataTag.contains("msav")) {
         CompoundTag blueprintDataNBT = ItemJBBlueprint.tryGetBlueprintNBTFromMemroyHeap(dataTag);
         if (blueprintDataNBT == null) {
            return listHolder;
         }

         listHolder.serialized = blueprintDataNBT.getIntArray("ser");
         if (blueprintDataNBT.contains("crinf")) {
            listHolder.coreSubTag = blueprintDataNBT.getCompound("crinf");
         }
      }

      return listHolder;
   }

   public static EntityJBStructure buildEntityJBStructureFromDataTag(CompoundTag dataTag) {
      JointListsHolderImpl listHolder = getSerializedFromDataTag(dataTag);
      if (listHolder.serialized == null) {
         return null;
      } else {
         int partsCount = dataTag.getInt("tnm");
         JointDataSerializer.rebuildAll(listHolder.serialized, partsCount, listHolder);
         int fixedY = -listHolder.minY;
         final EntityJBStructure structure = new EntityJBStructure();
         listHolder.createBlocksFromRefLists(new IJointListsHolder.BlockArranger() {
            @Override
            public void placeBlockAt(JointParts parts, EntityJointed entityJointedMayBeNull, int posX, int posY, int posZ, int coreFacing, int facingSide) {
               Block block = parts.getJBBlockType();
               if (block != null) {
                  TileEntityJoint te = (TileEntityJoint)((EntityBlock)block).newBlockEntity(new BlockPos(posX, posY, posZ), block.defaultBlockState());
                  structure.putBlockAndTileEntity(posX, posY, posZ, block, te);
                  te.readFromJointParts(entityJointedMayBeNull, parts, facingSide, coreFacing);
                  if (parts.jointType == 15 && entityJointedMayBeNull != null) {
                     parts.reAttatchSlaveToTileEntity(te, entityJointedMayBeNull);
                  }
               }
            }
         }, 3, 0, fixedY, 0);
         TileEntityJointCore teCore = BlockJointCore.getTileEntityJointerCore(structure, 0, fixedY, 0);
         if (teCore != null && listHolder.coreSubTag != null) {
            teCore.readOriginalValuesExcept(listHolder.coreSubTag, true);
            if (teCore.innerChest != null) {
               teCore.innerChest.readRINGDatasFromImportNBT(listHolder.coreSubTag);
            }

            if (teCore.effectChest != null) {
               teCore.effectChest.readEffectSlotDatasFromImportNBT(listHolder.coreSubTag);
            }
         }

         return structure;
      }
   }

   public static class EntityJBStructure implements BlockGetter {
      private TileEntityJointCore teCore;
      HashMap<BlockPos, CoordData> map = new HashMap<>();

      public void putBlockAndTileEntity(int posX, int posY, int posZ, Block block, TileEntityJoint te) {
         BlockPos pos = new BlockPos(posX, posY, posZ);
         this.map.put(pos, new CoordData(block, te));
         if (te instanceof TileEntityJointCore) {
            this.teCore = (TileEntityJointCore)te;
         }
      }

      public TileEntityJointCore getTileEntityJointCore() {
         return this.teCore;
      }

      @Nullable
      @Override
      public BlockEntity getBlockEntity(BlockPos pos) {
         CoordData cd = this.map.get(pos);
         return cd != null ? cd.te : null;
      }

      @Override
      public BlockState getBlockState(BlockPos pos) {
         CoordData cd = this.map.get(pos);
         return cd != null ? cd.block.defaultBlockState() : Blocks.AIR.defaultBlockState();
      }

      @Override
      public FluidState getFluidState(BlockPos pos) {
         CoordData cd = this.map.get(pos);
         return cd != null ? cd.block.defaultBlockState().getFluidState() : Fluids.EMPTY.defaultFluidState();
      }

      /** 1.12 {@code IBlockAccess.isAirBlock} — this structure only knows the blocks it placed. */
      public boolean isEmpty(BlockPos pos) {
         CoordData cd = this.map.get(pos);
         return cd == null;
      }

      /** 1.12 {@code IBlockAccess} had no height accessor; the vanilla overworld bounds are used here. */
      @Override
      public int getHeight() {
         return 384;
      }

      /** 1.12 {@code IBlockAccess} had no height accessor; the vanilla overworld bounds are used here. */
      @Override
      public int getMinBuildHeight() {
         return -64;
      }

      class CoordData {
         TileEntityJoint te;
         Block block;

         public CoordData(Block argBlock, TileEntityJoint argTe) {
            this.block = argBlock;
            this.te = argTe;
         }
      }
   }
}
