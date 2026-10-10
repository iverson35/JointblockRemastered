package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.assemble.BatchRenderHelper;
import jp.mc.ancientred.jointblock.carving.mcube.JBMCubeTable;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeMeshBuilder;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import jp.mc.ancientred.jointblock.config.JBKeyHanlder;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityBullet;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityDisassembleBall;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityJBClientCamera;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityJointed;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityTempWorker;
import jp.mc.ancientred.jointblock.gui.JBMenuTypes;
import jp.mc.ancientred.jointblock.gui.OverlayRendeler;
import jp.mc.ancientred.jointblock.item.model.SubIconItemModelLoader;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJBEntityBatcherRenderer;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJointerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import jp.mc.ancientred.jointblock.item.AbstractCarvingToolItem;
import jp.mc.ancientred.jointblock.item.IColorHanldlerItem;
import jp.mc.ancientred.jointblock.item.ItemDisassembleBall;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.item.ItemJBIFFTag;
import jp.mc.ancientred.jointblock.item.ItemJBMirrorTool;
import jp.mc.ancientred.jointblock.item.ItemTurnWrench;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only mod-bus setup. Kept out of {@link JointBlockRemastered} so the dedicated server never
 * loads a class that names a client type.
 *
 * <p>This is the 1.20.1 counterpart of the 1.12.2 {@code CommonProxyClient.init}/{@code
 * registerRenderHelper} bodies: entity renderers ({@code RenderingRegistry.registerEntityRenderingHandler}),
 * the block-entity renderers ({@code ClientRegistry.bindTileEntitySpecialRenderer}), the sub-icon
 * model loader ({@code ModelLoaderRegistry.registerLoader}) and the client-side table/pattern setup.
 */
@Mod.EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JBClientSetup {

    /**
     * 1.12.2 {@code CommonProxyClient.registerRenderHelper}: the subtype-based {@code ItemMeshDefinition}
     * registrations ({@code func_178080_a}). 1.20.1 selects the variant from an {@code overrides} entry in
     * the item model JSON; these are the predicates those entries test. The 1.12.2 damage-value subtype now
     * lives in the {@code subtype} NBT tag (the item classes' own convention).
     */
    private static final ResourceLocation SUBTYPE_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "subtype");
    private static final ResourceLocation FLIPWAY_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "flipway");
    /** Combined subtype + {@code jb:posnum} rank: 0 jbiff, 1 jbmsb, 2..5 the jbmsb_* variants. */
    private static final ResourceLocation POSNUM_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "posnum");
    /** {@code jb:pnum} clamped to the values a variant exists for; 0 selects the base model. */
    private static final ResourceLocation PNUM_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "pnum");
    private static final ResourceLocation CONNECTED_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "connected");
    /** Combined blueprint family * 3 + overlay rank (0..17); 0 is the {@code jbblueprint_b_e_n} base. */
    private static final ResourceLocation BLUEPRINT_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "blueprint");
    /** 1.12.2 {@code func_178086_a} registration, where the damage value itself is the model selector. */
    private static final ResourceLocation META_PREDICATE = new ResourceLocation(JointBlockRemastered.MOD_ID, "meta");

    private JBClientSetup() {
    }

    /**
     * The per-stack item model selectors, one rank per variant; the {@code overrides} arrays in
     * {@code assets/jointblock/models/item/*.json} test them with {@code >=} in descending order, so every
     * rank maps onto exactly the model the 1.12.2 {@code ItemMeshDefinition} returned for that stack.
     */
    private static void registerItemPropertyPredicates() {
        ItemProperties.register(JBItems.TurnWrenchItem, SUBTYPE_PREDICATE, (stack, level, entity, seed) -> {
            int subtype = ItemTurnWrench.getSubtype(stack);
            return subtype >= 1 && subtype <= 3 ? subtype : 0;
        });
        ItemProperties.register(JBItems.DisassembleBallItem, SUBTYPE_PREDICATE, (stack, level, entity, seed) ->
                ItemDisassembleBall.getSubtype(stack) == 1 ? 1 : 0);
        ItemProperties.register(JBItems.JBEffectItem, META_PREDICATE, (stack, level, entity, seed) ->
                stack.getDamageValue() == 1 ? 1 : 0);
        ItemProperties.register(JBItems.JBMirrorToolItem, FLIPWAY_PREDICATE, (stack, level, entity, seed) -> {
            int flipWay = stack.getTag() != null ? stack.getTag().getInt(ItemJBMirrorTool.FLIPWAY_NBT_TAG) : ItemJBMirrorTool.FLIPWAY_DISABLED;
            return flipWay == ItemJBMirrorTool.FLIPWAY_LR ? 0 : (flipWay == ItemJBMirrorTool.FLIPWAY_UD ? 1 : 2);
        });
        ItemProperties.register(JBItems.JBIFFTagItem, POSNUM_PREDICATE, (stack, level, entity, seed) -> {
            if (ItemJBIFFTag.getSubtype(stack) != 1) {
                return 0;
            }

            int patternNum = stack.hasTag() ? stack.getTag().getByte(ItemJBIFFTag.NBT_TAGNAME_POSNUM) : 0;
            return patternNum >= 1 && patternNum <= 4 ? patternNum + 1 : 1;
        });
        ItemProperties.register(JBItems.JBControllerItem, CONNECTED_PREDICATE, (stack, level, entity, seed) ->
                stack.hasTag() && stack.getTag().getInt(ItemJBController.TARGET_ENTITY_ID_NBT_TAG) != 0 ? 1 : 0);
        ItemProperties.register(JBItems.CopyToolItem, SUBTYPE_PREDICATE, (stack, level, entity, seed) -> {
            int subtype = ItemJBCopyTool.getSubtype(stack);
            return subtype >= 0 && subtype <= 3 ? subtype : 1;
        });
        ItemProperties.register(JBItems.PaintGunItem, PNUM_PREDICATE, (stack, level, entity, seed) -> {
            int patternNum = stack.hasTag() ? stack.getTag().getByte(AbstractCarvingToolItem.NBT_TAGNAME_PNUM) : 0;
            return patternNum >= 2 && patternNum <= 7 ? patternNum : 0;
        });
        ItemProperties.register(JBItems.CeramicChiselItem, PNUM_PREDICATE, (stack, level, entity, seed) -> {
            int patternNum = stack.hasTag() ? stack.getTag().getByte(AbstractCarvingToolItem.NBT_TAGNAME_PNUM) : 0;
            return patternNum == 2 || patternNum == 4 || patternNum == 8 ? patternNum : 0;
        });
        // 1.12.2 clamps the pattern cutter's jb:pnum to 1..5; anything else selects the plain model.
        ItemProperties.register(JBItems.PatternCutterItem, PNUM_PREDICATE, (stack, level, entity, seed) -> {
            int patternNum = stack.hasTag() ? stack.getTag().getByte(AbstractCarvingToolItem.NBT_TAGNAME_PNUM) : 0;
            return patternNum >= 1 && patternNum <= 5 ? patternNum : 0;
        });
        ItemProperties.register(JBItems.BluePrintItem, BLUEPRINT_PREDICATE, (stack, level, entity, seed) -> {
            int family;
            int subtype = ItemJBBlueprint.getSubtype(stack);
            if (subtype == 2) {
                family = 4;
            } else if (subtype == 3) {
                family = 5;
            } else {
                boolean temporary = ItemJBBlueprint.isTemporaryBlueprint(stack);
                boolean withData = stack.hasTag() && stack.getTag().contains(ItemJBBlueprint.NBT_TAGNAME_BLUEPRINTDATA);
                family = withData ? (temporary ? 3 : 2) : (temporary ? 1 : 0);
            }

            int overlay = 0;
            if (stack.hasTag() && stack.getTag().getCompound(ItemJBBlueprint.NBT_TAGNAME_BLUEPRINTDATA).getBoolean(ItemJBBlueprint.NBT_TAGNAME_OUTDATED)) {
                overlay = 2;
            } else if (ItemJBBlueprint.isSealed(stack)) {
                overlay = 1;
            }

            return family * 3 + overlay;
        });
    }

    /**
     * 1.12.2 {@code CommonProxyClient.init}: {@code RenderingRegistry.registerEntityRenderingHandler}
     * for the six entity classes. {@code EntitySeatPart} is a {@code PartEntity} of
     * {@code EntityJointed} in 1.20.1 (rendered by the jointed renderer), so it is not registered.
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(JBEntityTypes.JOINTED, RenderEntityJointed::new);
        event.registerEntityRenderer(JBEntityTypes.BULLET, RenderEntityBullet::new);
        event.registerEntityRenderer(JBEntityTypes.TEMP_WORKER, RenderEntityTempWorker::new);
        event.registerEntityRenderer(JBEntityTypes.DISASSEMBLE_BALL, RenderEntityDisassembleBall::new);
        event.registerEntityRenderer(jp.mc.ancientred.jointblock.registry.JBClientEntityTypes.CLIENT_CAMERA,
            RenderEntityJBClientCamera::new);

        // Every joint block returns RenderShape.ENTITYBLOCK_ANIMATED, so its in-world model comes
        // from this renderer alone - the three block entity types all extend TileEntityJoint and
        // share it (1.12.2 bound one TileEntitySpecialRenderer to all of them).
        event.registerBlockEntityRenderer(JBBlockEntityTypes.JOINT, TileEntityJointerRenderer::new);
        event.registerBlockEntityRenderer(JBBlockEntityTypes.JOINT_CORE, TileEntityJointerRenderer::new);
        event.registerBlockEntityRenderer(JBBlockEntityTypes.JOINT_ATTRACTER, TileEntityJointerRenderer::new);
        event.registerBlockEntityRenderer(JBBlockEntityTypes.JB_ENTITY_BATCHER, TileEntityJBEntityBatcherRenderer::new);
    }

    /** 1.12.2 {@code ModelLoaderRegistry.registerLoader}: the {@code jbsubiconmodel} geometry loader. */
    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(SubIconItemModelLoader.SUBICON_MODEL_DOMAIN, SubIconItemModelLoader.INSTANCE);
    }

    /**
     * 1.12.2 had no equivalent step: its client {@code IGuiHandler} built the screen directly, and
     * the key handler / pattern tables were built by {@code CommonProxyClient.init}. Forge 1.20.1
     * has no {@code RegisterMenuScreensEvent} in this version, so the screens are registered here
     * (work is deferred to the main thread by {@code enqueueWork}).
     */
    /**
     * 1.12.2 {@code CommonProxyClient.registerRenderHelper} registered one {@code ItemColors} handler
     * for the items that implement {@code IColorHanldlerItem}; without it 1.20.1 renders them with the
     * raw texture and the motion/mouse/putty tint tables are never read.
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> IColorHanldlerItem.safeCast(stack.getItem()).getColorFromItemStack(stack, tintIndex),
                JBItems.KeyBindedMotionItem, JBItems.ItemBindedMotionItem, JBItems.HumanUpperMotionItem,
                JBItems.HumanLowerMotionItem, JBItems.BeastLowerMotionItem, JBItems.InsectsMotionItem,
                JBItems.MachineMotionItem, JBItems.SensorMotionItem, JBItems.RingMotionItem,
                JBItems.CeramicPuttyItem, JBItems.JBMouseItem);
    }

    /**
     * 1.12.2 {@code CommonForgeEventHandler.handleDrawSceen}: {@code RenderGameOverlayEvent.Post} at
     * {@code ElementType.BOSSHEALTH} called {@code OverlayRendeler.renderOverlay(event)}. 1.20.1 has
     * no {@code RenderGameOverlayEvent}, so the same body is registered as an {@link IGuiOverlay}
     * ordered above {@link VanillaGuiOverlay#BOSS_EVENT_PROGRESS} - the vanilla layer the 1.12
     * {@code BOSSHEALTH} element corresponds to. Without this registration the whole HUD
     * (unit status bars, target sights, indicator window and the {@code /jbringdebug} ring window)
     * never renders.
     */
    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "renderer",
                (gui, guiGraphics, partialTick, screenWidth, screenHeight)
                        -> OverlayRendeler.render(guiGraphics, partialTick, screenWidth, screenHeight));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            JBMenuTypes.registerScreens();
            // 1.12.2 CommonProxyClient.registerRenderHelper: the per-stack ItemMeshDefinitions.
            registerItemPropertyPredicates();
            // 1.12.2 CommonProxyClient.init: JBKeyHanlder.instance = new JBKeyHanlder();
            JBKeyHanlder.instance = new JBKeyHanlder();
            // 1.12.2 CommonProxyClient.init: build the client-side pattern tables and the batch renderer.
            JBMCubeTable.buildMCTable();
            MCubeMeshBuilder.setUpHelper();
            JBPatternCarvingClientUtil.setUpPatternsForRender();
            BatchRenderHelper.setup();
        });
    }
}
