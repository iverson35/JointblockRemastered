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
import jp.mc.ancientred.jointblock.item.model.SubIconItemModelLoader;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJBEntityBatcherRenderer;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJointerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
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

    private JBClientSetup() {
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

        event.registerBlockEntityRenderer(JBBlockEntityTypes.JOINT, TileEntityJointerRenderer::new);
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
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            JBMenuTypes.registerScreens();
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
