package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJBEntityBatcher;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Block entity types.
 *
 * <p>In 1.12.2 there was a single {@code TileEntityJoint} class serving all eight joint blocks with
 * a {@code jointType} discriminator; the port split off {@code TileEntityJointCore} and
 * {@code TileEntityJointAttracter}. Each type therefore lists exactly the blocks whose
 * {@code newBlockEntity} constructs it - a block may only be claimed by one type - and the batcher
 * type is a block-less holder used by the client-side entity-batching render path.
 */
public final class JBBlockEntityTypes {

    private JBBlockEntityTypes() {
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, jp.mc.ancientred.jointblock.JointBlockRemastered.MOD_ID);

    public static BlockEntityType<TileEntityJoint> JOINT;
    public static BlockEntityType<TileEntityJointCore> JOINT_CORE;
    public static BlockEntityType<TileEntityJointAttracter> JOINT_ATTRACTER;
    public static BlockEntityType<TileEntityBlueprintBuilder> BLUEPRINT_BUILDER;
    public static BlockEntityType<TileEntityJBEntityBatcher> JB_ENTITY_BATCHER;

    public static void register(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register("joint", () -> JOINT = BlockEntityType.Builder
            .of(TileEntityJoint::new,
                JBBlocks.JointSeatBlock,
                JBBlocks.JointNormalBlock,
                JBBlocks.JointRotatorBlock,
                JBBlocks.JointExtenderBlock,
                JBBlocks.JointScalerBlock,
                JBBlocks.JointSliderBlock)
            .build(null));
        BLOCK_ENTITY_TYPES.register("joint_core", () -> JOINT_CORE = BlockEntityType.Builder
            .of(TileEntityJointCore::new, JBBlocks.JointCoreBlock)
            .build(null));
        BLOCK_ENTITY_TYPES.register("joint_attracter", () -> JOINT_ATTRACTER = BlockEntityType.Builder
            .of(TileEntityJointAttracter::new, JBBlocks.JointAttracterBlock)
            .build(null));
        BLOCK_ENTITY_TYPES.register("blueprint_builder", () -> BLUEPRINT_BUILDER = BlockEntityType.Builder
            .of(TileEntityBlueprintBuilder::new, JBBlocks.BlueprintBuilderBlock)
            .build(null));
        // Rendering-only holder; no block owns an instance.
        BLOCK_ENTITY_TYPES.register("jb_entity_batcher", () -> JB_ENTITY_BATCHER = BlockEntityType.Builder
            .of(TileEntityJBEntityBatcher::new)
            .build(null));
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
