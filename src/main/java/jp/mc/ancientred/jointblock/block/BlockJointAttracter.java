package jp.mc.ancientred.jointblock.block;

import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockJointAttracter extends BlockJointBase {
   @Override
   public int getJointType() {
      return 15;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJointAttracter(pos, state);
   }

   public static TileEntityJointAttracter getTileEntityJointerAttracter(BlockGetter world, int posX, int posY, int posZ) {
      BlockEntity te = WorldHelper.getTileEntity(world, posX, posY, posZ);
      return te instanceof TileEntityJointAttracter ? (TileEntityJointAttracter)te : null;
   }

   public static TileEntityJointAttracter getTileEntityJointerAttracter(BlockGetter world, BlockPos pos) {
      BlockEntity te = WorldHelper.getTileEntity(world, pos);
      return te instanceof TileEntityJointAttracter ? (TileEntityJointAttracter)te : null;
   }

   @Override
   public VoxelShape getShape(BlockState bState, BlockGetter world, BlockPos pos, CollisionContext context) {
      double lw = 0.18125000000000002;
      double hi = 0.81875;
      double tall = 0.86875;
      TileEntityJoint te = getTileEntityJointerBase(world, pos);
      if (te != null && te.getModelItemID() == 0 && te.carvingData == null) {
         int connectedSide = te.getConnectSide();
         int axis = JBRotationUtil.getExtenderAxis(connectedSide);
         switch (axis) {
            case 0:
               return Shapes.box(connectedSide == 5 ? tall : 0.0, lw, lw, 1.0 - (connectedSide == 4 ? tall : 0.0), hi, hi);
            case 1:
               return Shapes.box(lw, connectedSide == 1 ? tall : 0.0, lw, hi, 1.0 - (connectedSide == 0 ? tall : 0.0), hi);
            case 2:
               return Shapes.box(lw, lw, connectedSide == 3 ? tall : 0.0, hi, hi, 1.0 - (connectedSide == 2 ? tall : 0.0));
         }
      }

      return Shapes.block();
   }
}
