package jp.mc.ancientred.jointblock.block;

import java.util.Random;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 1.12.2 {@code BlockBlueprintBuilder} port: the block of
 * {@link TileEntityBlueprintBuilder}, a one-slot blueprint holder that turns a
 * blueprint into an assembled joint structure.
 *
 * <p>Block class: 1.12.2 {@code Block implements ITileEntityProvider} -> 1.20.1
 * {@link BaseEntityBlock} (the {@code ITileEntityProvider} replacement is
 * {@code EntityBlock#newBlockEntity}). 1.12.2 did <em>not</em> extend
 * {@code BlockContainer}; it was a plain {@code Block}, so no container/block-entity
 * plumbing beyond {@code newBlockEntity} is required.
 *
 * <p>Properties: 1.12.2 {@code Material.field_151594_q} = {@code Material.CIRCUITS},
 * i.e. {@code new MaterialLogic(MapColor.AIR).setNoPushMobility()}. Mapped to
 * {@link BlockBehaviour.Properties}:
 * <ul>
 *   <li>{@code MapColor.AIR} -> {@link MapColor#NONE};</li>
 *   <li>{@code MaterialLogic} has {@code blocksLight() == false} /
 *       {@code blocksMovement() == false}, but the 1.12.2 {@code Block} defaults
 *       ({@code isOpaqueCube() == true} => full cube, {@code lightOpacity == 255},
 *       full-cube collision/{@code getBoundingBox} = {@code FULL_BLOCK_AABB}) were
 *       <em>not</em> overridden by this block, so 1.20.1 keeps the default occluding
 *       full-cube behaviour (no {@code noOcclusion()}, no shape override);</li>
 *   <li>{@code Material#setNoPushMobility()} -> {@link PushReaction#DESTROY};</li>
 *   <li>{@code Material#isToolNotRequired()} stays {@code true} -> no
 *       {@code requiresCorrectToolForDrops()};</li>
 *   <li>1.12.2 {@code Block} defaults left untouched ({@code blockHardness == 0.0F},
 *       {@code blockResistance == 0.0F}, sound {@code SoundType.STONE}) ->
 *       {@code instabreak()} + {@code sound(SoundType.STONE)} (explosion resistance
 *       stays at the {@code Properties} default 0.0F, matching {@code blockResistance / 5.0F}).</li>
 * </ul>
 *
 * <p>Substitutions for the removed 1.12.2 hooks:
 * <ul>
 *   <li>{@code onBlockPlacedBy}'s meta computation ({@code ((floor(yaw * 4 / 360 + 0.5) & 3) + 2) % 4}
 *       with {@code 0->meta 3}, {@code 1->4}, {@code 2->2}, {@code 3->5}, i.e.
 *       SOUTH/WEST/NORTH/EAST) -> {@link #getStateForPlacement(BlockPlaceContext)}, the 1.20.1
 *       placement hook; final state is identical.</li>
 *   <li>{@code getMetaFromState}/{@code getStateFromMeta} -> dropped: 1.20.1 stores
 *       {@link #FACING} directly in the block state.</li>
 *   <li>{@code breakBlock}'s {@code InventoryHelper.dropInventoryItems} +
 *       {@code World#updateComparatorOutputLevel} -> {@link #onRemove} with
 *       {@code Containers.dropContents} + {@code Level#updateNeighbourForOutputSignal}
 *       (same shape as 1.20.1 {@code ChestBlock}).
 *       {@code breakByBuild} still suppresses the drop.</li>
 *   <li>{@code onBlockActivated}'s {@code EntityPlayer#openGui} ->
 *       {@link #use} calling {@code JBModContainer.proxy.openGui(guiBlueprintBuilderId, ...)}
 *       behind the original {@code !world.isRemote} guard, returning
 *       {@link InteractionResult#SUCCESS} on both sides as 1.12.2 returned {@code true}.</li>
 *   <li>{@code EnumBlockRenderType.MODEL} -> {@link RenderShape#MODEL}.</li>
 * </ul>
 */
public class BlockBlueprintBuilder extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private final Random rand = new Random();

   public BlockBlueprintBuilder() {
      super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .instabreak()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.DESTROY));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   /**
    * 1.12.2 {@code Block#onBlockPlacedBy}: {@code ((floor(yaw * 4.0F / 360.0F + 0.5D) & 3) + 2) % 4}
    * mapped to the meta table {@code 0 -> SOUTH(3), 1 -> WEST(4), 2 -> NORTH(2), 3 -> EAST(5)}.
    */
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      int i1 = ((Mth.floor((double)(context.getRotation() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
      Direction facing;
      switch (i1) {
         case 0:
            facing = Direction.SOUTH;
            break;
         case 1:
            facing = Direction.WEST;
            break;
         case 2:
            facing = Direction.NORTH;
            break;
         default:
            facing = Direction.EAST;
            break;
      }
      return this.defaultBlockState().setValue(FACING, facing);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityBlueprintBuilder(pos, state);
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player entityPlayer, InteractionHand hand, BlockHitResult hit) {
      if (!level.isClientSide) {
         JBModContainer.proxy.openGui(JBModContainer.guiBlueprintBuilderId, entityPlayer, level, pos);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())) {
         BlockEntity teRaw = level.getBlockEntity(pos);
         if (teRaw instanceof TileEntityBlueprintBuilder) {
            TileEntityBlueprintBuilder te = (TileEntityBlueprintBuilder)teRaw;
            if (!te.breakByBuild) {
               Containers.dropContents(level, pos, te);
               level.updateNeighbourForOutputSignal(pos, this);
            }
         }

         super.onRemove(state, level, pos, newState, movedByPiston);
      }
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
