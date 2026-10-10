package jp.mc.ancientred.jointblock.block;

import java.util.Random;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.item.ICeramicModItem;
import jp.mc.ancientred.jointblock.item.ItemJBIFFTag;
import jp.mc.ancientred.jointblock.item.ItemJBMirrorTool;
import jp.mc.ancientred.jointblock.item.ItemTurnWrench;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BlockJointBase extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   protected final Random rand = new Random();

   protected BlockJointBase() {
      super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(1.2F, 5.0F).pushReaction(PushReaction.IGNORE).noOcclusion());
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   /**
    * Joint type this block builds when placed (0 core, 2 seat, 4 normal, 8 rotator, 10 extender,
    * 12 scaler, 14 slider, 15 attracter). Also selects the joint model the block item renders.
    */
   public abstract int getJointType();

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rot) {
      return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())) {
         TileEntityJoint te = getTileEntityJointerBase(level, pos);
         if (te != null) {
            if (!te.breakByAssemble) {
               Containers.dropContents(level, pos, te);
            }

            level.updateNeighbourForOutputSignal(pos, this);
            te.notiyfyCarvingNeighbour();
         }

         super.onRemove(state, level, pos, newState, movedByPiston);
      }
   }

   protected boolean isPlayerHoldingJointBlock(Player entityPlayer) {
      ItemStack handHeldItemStack = CompatHelper.getCurrentEquippedItem(entityPlayer);
      return !CompatHelper.isStackEmptyOrNull(handHeldItemStack) && Block.byItem(handHeldItemStack.getItem()) instanceof BlockJointBase;
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos bPos, Player entityPlayer, InteractionHand hand, BlockHitResult hit) {
      if (this.isPlayerHoldingJointBlock(entityPlayer)) {
         return InteractionResult.PASS;
      } else if (this.isPlayerHoldingInteractiveItem(level, bPos, state, entityPlayer, hit.getDirection())) {
         return InteractionResult.SUCCESS;
      } else {
         if (!level.isClientSide) {
            openJointGui(entityPlayer, level, bPos);
         }

         return InteractionResult.SUCCESS;
      }
   }

   protected static void openJointGui(Player entityPlayer, Level level, BlockPos bPos) {
      openJointGui(entityPlayer, level, bPos, JBGuiIds.guiJointBlockId);
   }

   /** 1.12.2 {@code EntityPlayer#openGui(mod, intGuiId, world, x, y, z)}; see {@link JBGuiOpener}. */
   protected static void openJointGui(Player entityPlayer, Level level, BlockPos bPos, int guiId) {
      JBModContainer.proxy.openGui(guiId, entityPlayer, level, bPos);
   }

   protected boolean isPlayerHoldingInteractiveItem(Level world, BlockPos bPos, BlockState state, Player entityPlayer, Direction side) {
      ItemStack handHeldItemStack = CompatHelper.getCurrentEquippedItem(entityPlayer);
      if (!CompatHelper.isStackEmptyOrNull(handHeldItemStack)) {
         Item item = handHeldItemStack.getItem();
         TileEntityJoint teJoint = getTileEntityJointerBase(world, bPos);
         int sideOld;
         if (ItemTurnWrench.isFaceRotateWrench(handHeldItemStack) || item == Items.STICK) {
            if (teJoint.isValidCarveable()) {
               JBCarvingDataUtil.rotateCarvingDataClockwize(teJoint);
               teJoint.markUpdateForCarvingDataMod(entityPlayer);
            } else {
               sideOld = teJoint.getJointFacing();
               teJoint.setJointFacing(JBRotationUtil.rotateFace(sideOld, true));
               teJoint.markUpdateThisWithMirrorProcess(entityPlayer);
            }

            return true;
         }

         if (teJoint == null) {
            return true;
         }

         if (ItemTurnWrench.isSideRotateWrench(handHeldItemStack) || item == Blocks.TORCH.asItem()) {
            if (teJoint.isValidCarveable()) {
               JBCarvingDataUtil.rotateCarvingData(teJoint, entityPlayer);
               return true;
            }

            if (JBCommonUtil.doSideRotateOnRender(teJoint.getJointType())) {
               sideOld = teJoint.renderSide;
               int sideNew = JBRotationUtil.rotateSide(sideOld);
               teJoint.renderSide = sideNew;
               teJoint.markUpdateThisWithMirrorProcess(entityPlayer);
               return true;
            }
         }

         if (ItemTurnWrench.isMagnetWrench(handHeldItemStack)) {
            if (entityPlayer.isShiftKeyDown()) {
               teJoint.makeEverySideBlockConnectThis();
            } else {
               teJoint.turnToNewConnectSide();
               teJoint.markUpdateThisWithMirrorProcess(entityPlayer);
            }

            return true;
         }

         if (ItemTurnWrench.isHammer(handHeldItemStack)) {
            side = Direction.from3DDataValue(JBRotationUtil.getPlayerLookingSide(entityPlayer));
            if (entityPlayer.isShiftKeyDown()) {
               teJoint.conductHammerWorks(side.getOpposite());
            } else {
               teJoint.conductHammerWorks(side);
            }

            return true;
         }

         if (item instanceof IJBModelItem && entityPlayer.isShiftKeyDown() && teJoint.setJBItemStack(handHeldItemStack)) {
            handHeldItemStack.shrink(1);
            teJoint.markUpdateThisWithMirrorProcess(entityPlayer);
            return true;
         }

         if (item instanceof ItemJBIFFTag && teJoint instanceof TileEntityJointCore) {
            ItemJBIFFTag.setIFFTagToCore(handHeldItemStack, (TileEntityJointCore)teJoint, entityPlayer);
            return true;
         }

         if (item instanceof ICeramicModItem) {
            ((ICeramicModItem)item).onItemUsedToJointBlock(handHeldItemStack, entityPlayer, teJoint);
            return true;
         }

         if (item instanceof ItemJBMirrorTool) {
            JBModContainer.proxy.openGui(JBGuiIds.guiMirrorBuildId, entityPlayer, world, bPos);
            return true;
         }
      }

      return false;
   }

   public static TileEntityJoint getTileEntityJointerBase(BlockGetter world, int posX, int posY, int posZ) {
      BlockEntity te = WorldHelper.getTileEntity(world, posX, posY, posZ);
      return te instanceof TileEntityJoint ? (TileEntityJoint)te : null;
   }

   public static TileEntityJoint getTileEntityJointerBase(BlockGetter world, BlockPos pos) {
      BlockEntity te = WorldHelper.getTileEntity(world, pos);
      return te instanceof TileEntityJoint ? (TileEntityJoint)te : null;
   }

   public static final Block convertItemToBlock(Item item) {
      return item instanceof BlockItem ? ((BlockItem)item).getBlock() : null;
   }

   public static final BlockState convertItemMetaToBlockState(Item item, int meta) {
      return item instanceof BlockItem ? ((BlockItem)item).getBlock().defaultBlockState() : null;
   }

   @Override
   public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
      return false;
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return (lvl, pos, st, be) -> {
         if (be instanceof TileEntityJoint teJoint) {
            teJoint.tick();
         }
      };
   }
}
