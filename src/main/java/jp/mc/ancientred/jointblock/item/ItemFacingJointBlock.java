package jp.mc.ancientred.jointblock.item;

import java.util.function.Consumer;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.item.model.JointItemRenderer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ItemFacingJointBlock extends BlockItem {
   public ItemFacingJointBlock(Block block) {
      super(block, new Item.Properties());
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Direction facing = context.getClickedFace();
      BlockPos clickedPos = context.getClickedPos();
      Vec3 hitVec = context.getClickLocation();
      float hitX = (float)(hitVec.x - (double)clickedPos.getX());
      float hitY = (float)(hitVec.y - (double)clickedPos.getY());
      float hitZ = (float)(hitVec.z - (double)clickedPos.getZ());
      return facing.get3DDataValue() <= 5 && this.tryPlaceJointBlockAt(context.getPlayer(), (ItemStack)null, context.getLevel(), clickedPos, context.getHand(), facing, hitX, hitY, hitZ) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
   }

   public boolean tryPlaceJointBlockAt(Player player, ItemStack itemStack, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      BlockState iblockstate = world.getBlockState(pos);
      if (!iblockstate.canBeReplaced()) {
         pos = pos.relative(facing);
      }

      if (CompatHelper.isStackEmptyOrNull(itemStack) && player != null) {
         itemStack = player.getItemInHand(hand);
      }

      BlockState iblockstate1 = this.getBlock().getStateForPlacement(new BlockPlaceContext(world, player, hand, itemStack, new BlockHitResult(new Vec3((double)pos.getX() + (double)hitX, (double)pos.getY() + (double)hitY, (double)pos.getZ() + (double)hitZ), facing, pos, false)));
      if (!CompatHelper.isStackEmptyOrNull(itemStack) && !itemStack.isEmpty() && player.mayUseItemAt(pos, facing, itemStack) && iblockstate1 != null && world.isUnobstructed(iblockstate1, pos, CollisionContext.of(player))) {
         int side = facing.get3DDataValue();
         int parX = pos.getX();
         int parY = pos.getY();
         int parZ = pos.getZ();
         TileEntityJoint te = JBRotationUtil.getSideNeighbourTe(world, parX, parY, parZ, EnumDirectionOld.OPPOSITES[side]);
         if (te != null && te.getJointType() == 10) {
            if (side != te.getConnectableExtenderSide() && side != EnumDirectionOld.OPPOSITES[te.getConnectableExtenderSide()]) {
               return false;
            }
         } else if (te != null && te.getJointType() == 15 && side != te.connectSide) {
            return false;
         }

         int facingSide = JBRotationUtil.getFacingOppositeSideFromEntity(player);
         if (this.placeBlockAt(itemStack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1, facingSide)) {
            iblockstate1 = world.getBlockState(pos);
            SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            itemStack.shrink(1);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean placeBlockAt(ItemStack stack, Player player, Level world, BlockPos pos, int side, float hitX, float hitY, float hitZ, BlockState newState, int facingSide) {
      if (!world.setBlock(pos, newState, 11)) {
         return false;
      } else {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() == this.getBlock()) {
            TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(world, pos);
            if (te != null) {
               int oppositeSide = EnumDirectionOld.OPPOSITES[side];
               TileEntityJoint teAtSide = JBRotationUtil.getSideNeighbourTe(world, x, y, z, oppositeSide);
               int connectSideSet;
               if (teAtSide != null) {
                  connectSideSet = oppositeSide;
                  te.setConnectSide(oppositeSide);
               } else {
                  te.setConnectSide(connectSideSet = this.searchJointBlockExistSide(world, x, y, z));
               }

               te.setJointFacing(facingSide);
               if (connectSideSet == 1 && JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
                  if (facingSide == 3) {
                     te.setJointFacing(2);
                  } else if (facingSide == 2) {
                     te.setJointFacing(3);
                  }
               }
            }

            TileEntityJoint.setMirrorBlock(world, x, y, z, te.getJointFacing(), player, false);
            this.getBlock().setPlacedBy(world, pos, state, player, stack);
            if (player instanceof ServerPlayer) {
               CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, pos, stack);
            }
         }

         return true;
      }
   }

   private int searchJointBlockExistSide(Level world, int x, int y, int z) {
      for(int side = 0; side < 6; ++side) {
         TileEntityJoint te = JBRotationUtil.getSideNeighbourTe(world, x, y, z, side);
         if (te != null && te.getJointType() != 15 && (te.getJointType() != 10 || side == EnumDirectionOld.OPPOSITES[te.getConnectableExtenderSide()] || side == te.getConnectableExtenderSide())) {
            return side;
         }
      }

      return 0;
   }

   /**
    * The joint items use the {@code builtin/entity} model, which renders through the joint model
    * instead of a baked one (1.12.2 did this with {@code Item.setTileEntityItemStackRenderer}).
    * {@code initializeClient} only runs on the client, so the client-only renderer stays out of the
    * dedicated-server class path.
    */
   @Override
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private BlockEntityWithoutLevelRenderer renderer;

         @Override
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new JointItemRenderer(((BlockJointBase)ItemFacingJointBlock.this.getBlock()).getJointType());
            }

            return this.renderer;
         }
      });
   }
}
