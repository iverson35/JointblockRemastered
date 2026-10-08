package jp.mc.ancientred.jointblock.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WorldHelper {
   public static final int getBlockMetadata(Level world, int posX, int posY, int posZ) {
      BlockState bState = world.getChunk(posX >> 4, posZ >> 4).getBlockState(new BlockPos(posX, posY, posZ));
      // 1.12 metadata does not exist in 1.20.1; the only metadata this mod relies on is
      // the joint block FACING direction index (BlockJointBase meta == Direction index).
      return bState.hasProperty(BlockStateProperties.FACING) ? bState.getValue(BlockStateProperties.FACING).get3DDataValue() : 0;
   }

   public static final Block getBlock(Level world, int posX, int posY, int posZ) {
      return world.getChunk(posX >> 4, posZ >> 4).getBlockState(new BlockPos(posX, posY, posZ)).getBlock();
   }

   public static final Block getBlock(Level world, BlockPos pos) {
      return world.getBlockState(pos).getBlock();
   }

   public static final BlockState getBlockState(Level world, int posX, int posY, int posZ) {
      return world.getChunk(posX >> 4, posZ >> 4).getBlockState(new BlockPos(posX, posY, posZ));
   }

   public static final BlockState getBlockState(Level world, BlockPos pos) {
      return world.getBlockState(pos);
   }

   public static final BlockEntity getTileEntity(BlockGetter world, int posX, int posY, int posZ) {
      return world.getBlockEntity(new BlockPos(posX, posY, posZ));
   }

   public static final BlockEntity getTileEntity(BlockGetter world, BlockPos pos) {
      return world.getBlockEntity(pos);
   }

   public static boolean isAirBlock(Level world, int posX, int posY, int posZ) {
      BlockState bState = world.getChunk(posX >> 4, posZ >> 4).getBlockState(new BlockPos(posX, posY, posZ));
      return bState.isAir();
   }

   public static boolean setBlockToAir(Level world, int posX, int posY, int posZ) {
      return world.removeBlock(new BlockPos(posX, posY, posZ), false);
   }

   public static boolean setBlockToAir(Level world, BlockPos pos) {
      return world.removeBlock(pos, false);
   }

   public static boolean setBlock(Level world, int posX, int posY, int posZ, Block block) {
      return world.setBlockAndUpdate(new BlockPos(posX, posY, posZ), block.defaultBlockState());
   }

   public static boolean setBlock(Level world, int posX, int posY, int posZ, Block block, int flags) {
      return world.setBlock(new BlockPos(posX, posY, posZ), block.defaultBlockState(), flags);
   }

   public static boolean setBlock(Level world, BlockPos pos, Block block, int flags) {
      return world.setBlock(pos, block.defaultBlockState(), flags);
   }

   public static void markBlockForUpdate(Level world, BlockPos pos) {
      BlockState state = getBlockState(world, pos);
      world.sendBlockUpdated(pos, state, state, 8);
   }

   public static ChunkAccess getChunkFromBlockCoords(Level world, int posX, int posY, int posZ) {
      return world.getChunk(posX >> 4, posZ >> 4);
   }
}
