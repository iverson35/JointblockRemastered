package jp.mc.ancientred.jointblock.block;

import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockJointNormal extends BlockJointBase {
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJoint(4, new int[]{0, 0, 0, 0}, pos, state);
   }
}
