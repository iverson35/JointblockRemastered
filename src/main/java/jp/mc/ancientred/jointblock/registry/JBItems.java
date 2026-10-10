package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.item.motionitem.ItemBeastLowerMotion;
import jp.mc.ancientred.jointblock.item.ItemCeramicChisel;
import jp.mc.ancientred.jointblock.item.ItemCeramicPutty;
import jp.mc.ancientred.jointblock.item.ItemDisassembleBall;
import jp.mc.ancientred.jointblock.item.ItemFacingJointBlock;
import jp.mc.ancientred.jointblock.item.motionitem.ItemHumanLowerMotion;
import jp.mc.ancientred.jointblock.item.motionitem.ItemHumanUpperMotion;
import jp.mc.ancientred.jointblock.item.motionitem.ItemInsectsMotion;
import jp.mc.ancientred.jointblock.item.motionitem.ItemItemBindedMotion;
import jp.mc.ancientred.jointblock.item.ItemJBAssemblyTuner;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemJBEffect;
import jp.mc.ancientred.jointblock.item.ItemJBIFFTag;
import jp.mc.ancientred.jointblock.item.ItemJBMirrorTool;
import jp.mc.ancientred.jointblock.item.ItemJBMouse;
import jp.mc.ancientred.jointblock.item.ItemJBTargetSight;
import jp.mc.ancientred.jointblock.item.motionitem.ItemKeyBindedMotion;
import jp.mc.ancientred.jointblock.item.motionitem.ItemMachineMotion;
import jp.mc.ancientred.jointblock.item.ItemPaintGun;
import jp.mc.ancientred.jointblock.item.ItemPatternCutter;
import jp.mc.ancientred.jointblock.item.motionitem.ItemRingChannelMotion;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.item.motionitem.ItemSensorMotion;
import jp.mc.ancientred.jointblock.item.ItemTurnWrench;
import jp.mc.ancientred.jointblock.item.ItemUnitParts;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Items. Field names match the 1.12.2 {@code JBModContainer} statics; registry and
 * unlocalised names are the originals (e.g. "unitparts", "keyresmotionitem") so the
 * converted {@code assets/jointblock/lang/*.json} keys keep resolving.
 *
 * <p>The eight joint blocks share {@link ItemFacingJointBlock} (which overrides
 * {@code useOn} to place the block facing the clicked side); the blueprint builder
 * uses a plain {@link BlockItem}.
 */
public final class JBItems {

    private JBItems() {
    }

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, JointBlockRemastered.MOD_ID);

    // ------------------------------------------------------------------
    // Block items
    // ------------------------------------------------------------------
    public static ItemFacingJointBlock JointCoreItem;
    public static ItemFacingJointBlock JointSeatItem;
    public static ItemFacingJointBlock JointNormalItem;
    public static ItemFacingJointBlock JointRotatorItem;
    public static ItemFacingJointBlock JointExtenderItem;
    public static ItemFacingJointBlock JointScalerItem;
    public static ItemFacingJointBlock JointSliderItem;
    public static ItemFacingJointBlock JointAttracterItem;
    public static BlockItem BlueprintBuilderItem;

    // ------------------------------------------------------------------
    // Tools and components
    // ------------------------------------------------------------------
    public static ItemJBController JBControllerItem;
    public static ItemJBMouse JBMouseItem;
    public static ItemDisassembleBall DisassembleBallItem;
    public static ItemJBAssemblyTuner AssemblyTunerItem;
    public static ItemTurnWrench TurnWrenchItem;
    public static ItemUnitParts UnitPartsItem;
    public static ItemCeramicPutty CeramicPuttyItem;
    public static ItemCeramicChisel CeramicChiselItem;
    public static ItemPaintGun PaintGunItem;
    public static ItemPatternCutter PatternCutterItem;
    public static ItemJBCopyTool CopyToolItem;
    public static ItemJBIFFTag JBIFFTagItem;
    public static ItemJBMirrorTool JBMirrorToolItem;
    public static ItemJBBlueprint BluePrintItem;
    public static ItemRingProgram RingProgramItem;
    public static ItemJBTargetSight TargetSightItem;
    public static ItemJBEffect JBEffectItem;

    // ------------------------------------------------------------------
    // Motion binders (one per motion-item kind)
    // ------------------------------------------------------------------
    public static ItemKeyBindedMotion KeyBindedMotionItem;
    public static ItemItemBindedMotion ItemBindedMotionItem;
    public static ItemHumanUpperMotion HumanUpperMotionItem;
    public static ItemHumanLowerMotion HumanLowerMotionItem;
    public static ItemBeastLowerMotion BeastLowerMotionItem;
    public static ItemInsectsMotion InsectsMotionItem;
    public static ItemMachineMotion MachineMotionItem;
    public static ItemRingChannelMotion RingMotionItem;
    public static ItemSensorMotion SensorMotionItem;

    public static void register(IEventBus modBus) {
        ITEMS.register("joint_core", () -> JointCoreItem = new ItemFacingJointBlock(JBBlocks.JointCoreBlock));
        ITEMS.register("joint_seat", () -> JointSeatItem = new ItemFacingJointBlock(JBBlocks.JointSeatBlock));
        ITEMS.register("joint_normal", () -> JointNormalItem = new ItemFacingJointBlock(JBBlocks.JointNormalBlock));
        ITEMS.register("joint_rotator", () -> JointRotatorItem = new ItemFacingJointBlock(JBBlocks.JointRotatorBlock));
        ITEMS.register("joint_extender", () -> JointExtenderItem = new ItemFacingJointBlock(JBBlocks.JointExtenderBlock));
        ITEMS.register("joint_scaler", () -> JointScalerItem = new ItemFacingJointBlock(JBBlocks.JointScalerBlock));
        ITEMS.register("joint_slider", () -> JointSliderItem = new ItemFacingJointBlock(JBBlocks.JointSliderBlock));
        ITEMS.register("joint_attracter", () -> JointAttracterItem = new ItemFacingJointBlock(JBBlocks.JointAttracterBlock));
        ITEMS.register("blueprintbuilder", () -> BlueprintBuilderItem = new BlockItem(JBBlocks.BlueprintBuilderBlock, new Item.Properties()));

        ITEMS.register("jbcontroller", () -> JBControllerItem = new ItemJBController());
        ITEMS.register("jbmouse", () -> JBMouseItem = new ItemJBMouse());
        ITEMS.register("disassembleball", () -> DisassembleBallItem = new ItemDisassembleBall());
        ITEMS.register("assemblytuner", () -> AssemblyTunerItem = new ItemJBAssemblyTuner());
        ITEMS.register("turnwrench", () -> TurnWrenchItem = new ItemTurnWrench());
        ITEMS.register("unitparts", () -> UnitPartsItem = new ItemUnitParts());
        ITEMS.register("ceramicputty", () -> CeramicPuttyItem = new ItemCeramicPutty());
        ITEMS.register("ceramicchisel", () -> CeramicChiselItem = new ItemCeramicChisel());
        ITEMS.register("paintgun", () -> PaintGunItem = new ItemPaintGun());
        ITEMS.register("patterncutter", () -> PatternCutterItem = new ItemPatternCutter());
        ITEMS.register("jbcopytool", () -> CopyToolItem = new ItemJBCopyTool());
        ITEMS.register("jbifftag", () -> JBIFFTagItem = new ItemJBIFFTag());
        ITEMS.register("jbmirrortool", () -> JBMirrorToolItem = new ItemJBMirrorTool());
        ITEMS.register("jbblueprint", () -> BluePrintItem = new ItemJBBlueprint());
        ITEMS.register("ringprogram", () -> RingProgramItem = new ItemRingProgram());
        ITEMS.register("targetsightitem", () -> TargetSightItem = new ItemJBTargetSight());
        ITEMS.register("jbeffect", () -> JBEffectItem = new ItemJBEffect());

        ITEMS.register("keyresmotionitem", () -> KeyBindedMotionItem = new ItemKeyBindedMotion());
        ITEMS.register("itemresmotionitem", () -> ItemBindedMotionItem = new ItemItemBindedMotion());
        ITEMS.register("humanupmotionitem", () -> HumanUpperMotionItem = new ItemHumanUpperMotion());
        ITEMS.register("humanlwmotionitem", () -> HumanLowerMotionItem = new ItemHumanLowerMotion());
        ITEMS.register("beastlwmotionitem", () -> BeastLowerMotionItem = new ItemBeastLowerMotion());
        ITEMS.register("insectsmotionitem", () -> InsectsMotionItem = new ItemInsectsMotion());
        ITEMS.register("machinemotionitem", () -> MachineMotionItem = new ItemMachineMotion());
        ITEMS.register("ringchmotionitem", () -> RingMotionItem = new ItemRingChannelMotion());
        ITEMS.register("sensormotionitem", () -> SensorMotionItem = new ItemSensorMotion());

        ITEMS.register(modBus);
    }
}
