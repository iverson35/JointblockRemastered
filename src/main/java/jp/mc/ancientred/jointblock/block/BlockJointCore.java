package jp.mc.ancientred.jointblock.block;

import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockJointCore extends BlockJointBase {
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJointCore(pos, state);
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos bPos, Player entityPlayer, InteractionHand hand, BlockHitResult hit) {
      if (this.isPlayerHoldingJointBlock(entityPlayer)) {
         return InteractionResult.PASS;
      } else if (this.isPlayerHoldingInteractiveItem(world, bPos, state, entityPlayer, hit.getDirection())) {
         return InteractionResult.SUCCESS;
      } else {
         if (!world.isClientSide) {
            openJointGui(entityPlayer, world, bPos,
                ItemRingProgram.isCurrentItemRing(entityPlayer) ? JBGuiIds.guiJointCoreFuelsFromBlockId : JBGuiIds.guiJointCoreId);
         }

         return InteractionResult.SUCCESS;
      }
   }

   public static TileEntityJointCore getTileEntityJointerCore(BlockGetter world, BlockPos pos) {
      BlockEntity te = WorldHelper.getTileEntity(world, pos);
      return te instanceof TileEntityJointCore ? (TileEntityJointCore)te : null;
   }

   public static TileEntityJointCore getTileEntityJointerCore(BlockGetter world, int posX, int posY, int posZ) {
      BlockEntity te = WorldHelper.getTileEntity(world, posX, posY, posZ);
      return te instanceof TileEntityJointCore ? (TileEntityJointCore)te : null;
   }

   @Override
   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())) {
         TileEntityJointCore jointCore = getTileEntityJointerCore(world, pos);
         if (jointCore != null && !jointCore.breakByAssemble && jointCore.innerChest != null) {
            Containers.dropContents(world, pos, jointCore.innerChest);
         }
      }

      super.onRemove(state, world, pos, newState, movedByPiston);
   }
}
