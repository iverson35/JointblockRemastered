package jp.mc.ancientred.jointblock.block;

import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockJointExtender extends BlockJointBase {
   private static float LW = 0.36875F;
   private static float HI = 0.63125F;
   private static VoxelShape AABB_X;
   private static VoxelShape AABB_Y;
   private static VoxelShape AABB_Z;

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJoint(this.getJointType(), new int[]{100, 100, 100, 0}, pos, state);
   }

   @Override
   public VoxelShape getShape(BlockState bState, BlockGetter world, BlockPos pos, CollisionContext context) {
      TileEntityJoint te = getTileEntityJointerBase(world, pos);
      if (te != null && te.getModelItemID() == 0 && te.carvingData == null) {
         int connectedSide = te.getConnectSide();
         int axis = JBRotationUtil.getExtenderAxis(connectedSide);
         switch (axis) {
            case 0:
               return AABB_X;
            case 1:
               return AABB_Y;
            case 2:
               return AABB_Z;
         }
      }

      // 1.12.2 returned Block.FULL_BLOCK_AABB here: with a model or carving applied the joint block is a full cube.
      return Shapes.block();
   }

   /** 1.12.2 {@code func_185496_a} returned {@code func_180646_a}, so collision and selection match. */
   @Override
   public VoxelShape getCollisionShape(BlockState bState, BlockGetter world, BlockPos pos, CollisionContext context) {
      return this.getShape(bState, world, pos, context);
   }

   static {
      AABB_X = Shapes.box(0.0, (double)LW, (double)LW, 1.0, (double)HI, (double)HI);
      AABB_Y = Shapes.box((double)LW, 0.0, (double)LW, (double)HI, 1.0, (double)HI);
      AABB_Z = Shapes.box((double)LW, (double)LW, 0.0, (double)HI, (double)HI, 1.0);
   }

   @Override
   public int getJointType() {
      return 10;
   }
}
