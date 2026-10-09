package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.block.BlockBlueprintBuilder;
import jp.mc.ancientred.jointblock.block.BlockJointAttracter;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.block.BlockJointExtender;
import jp.mc.ancientred.jointblock.block.BlockJointNormal;
import jp.mc.ancientred.jointblock.block.BlockJointRotator;
import jp.mc.ancientred.jointblock.block.BlockJointScaler;
import jp.mc.ancientred.jointblock.block.BlockJointSeat;
import jp.mc.ancientred.jointblock.block.BlockJointSlider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Blocks. Field names and types match the 1.12.2 {@code JBModContainer} statics so
 * ported call sites (e.g. {@code block = JBBlocks.JointCoreBlock;}) resolve unchanged.
 *
 * <p>Instances are built inside the {@link DeferredRegister} suppliers: 1.20.1 freezes
 * {@code BuiltInRegistries.BLOCK} once vanilla bootstrap is done, and {@code Block}'s
 * constructor writes an intrusive holder into it, so constructing a block during class
 * initialisation throws {@code IllegalStateException: Registry is already frozen}. The
 * fields are therefore assigned by the supplier and are non-null from the
 * {@code RegisterEvent} onwards (blocks are registered before items, whose block items
 * read them).
 *
 * <p>Registry names are the original ones ("joint_core" ... "blueprintbuilder") so
 * the existing {@code assets/jointblock/} paths keep working.
 */
public final class JBBlocks {

    private JBBlocks() {
    }

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, JointBlockRemastered.MOD_ID);

    public static BlockJointCore JointCoreBlock;
    public static BlockJointSeat JointSeatBlock;
    public static BlockJointNormal JointNormalBlock;
    public static BlockJointRotator JointRotatorBlock;
    public static BlockJointExtender JointExtenderBlock;
    public static BlockJointScaler JointScalerBlock;
    public static BlockJointSlider JointSliderBlock;
    public static BlockJointAttracter JointAttracterBlock;
    public static BlockBlueprintBuilder BlueprintBuilderBlock;

    public static void register(IEventBus modBus) {
        BLOCKS.register("joint_core", () -> JointCoreBlock = new BlockJointCore());
        BLOCKS.register("joint_seat", () -> JointSeatBlock = new BlockJointSeat());
        BLOCKS.register("joint_normal", () -> JointNormalBlock = new BlockJointNormal());
        BLOCKS.register("joint_rotator", () -> JointRotatorBlock = new BlockJointRotator());
        BLOCKS.register("joint_extender", () -> JointExtenderBlock = new BlockJointExtender());
        BLOCKS.register("joint_scaler", () -> JointScalerBlock = new BlockJointScaler());
        BLOCKS.register("joint_slider", () -> JointSliderBlock = new BlockJointSlider());
        BLOCKS.register("joint_attracter", () -> JointAttracterBlock = new BlockJointAttracter());
        BLOCKS.register("blueprintbuilder", () -> BlueprintBuilderBlock = new BlockBlueprintBuilder());
        BLOCKS.register(modBus);
    }
}
