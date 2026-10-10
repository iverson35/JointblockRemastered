package jp.mc.ancientred.jointblock.block;

import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockJointSeat extends BlockJointBase {
   private static VoxelShape AABB_SEAT = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
   private static VoxelShape AABB_ELSE = Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 0.5625, 0.8125);

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJoint(this.getJointType(), new int[]{1, 2, 0, 0}, pos, state);
   }

   @Override
   public VoxelShape getShape(BlockState bState, BlockGetter world, BlockPos pos, CollisionContext context) {
      TileEntityJoint te = getTileEntityJointerBase(world, pos);
      if (te != null && te.getModelItemID() == 0 && te.carvingData == null) {
         return this.isSeat(te) ? AABB_SEAT : AABB_ELSE;
      } else {
         // 1.12.2 returned Block.NULL_AABB (an empty box) here, not the full cube: a joint block that
         // has a model or carving applied is drawn by its own model and stays intangible, otherwise it
         // swallows the selection ray of whatever it is attached to.
         return Shapes.empty();
      }
   }

   /** 1.12.2 {@code func_185496_a} returned {@code func_180646_a}, so collision and selection match. */
   @Override
   public VoxelShape getCollisionShape(BlockState bState, BlockGetter world, BlockPos pos, CollisionContext context) {
      return this.getShape(bState, world, pos, context);
   }

   private boolean isSeat(TileEntityJoint te) {
      return te.initValues == null || te.initValues[0] == null || te.initValues[0].length < 3 || te.initValues[0][2] == 0;
   }

   @Override
   public int getJointType() {
      return 2;
   }
}
