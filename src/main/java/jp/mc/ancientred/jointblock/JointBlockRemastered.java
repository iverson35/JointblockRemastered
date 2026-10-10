package jp.mc.ancientred.jointblock;

import com.mojang.logging.LogUtils;
import jp.mc.ancientred.jointblock.action.JBActionFactory;
import jp.mc.ancientred.jointblock.block.ModelBlockRegister;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingUtil;
import jp.mc.ancientred.jointblock.command.JBCommands;
import jp.mc.ancientred.jointblock.common.CapabilityEntityCommonStorage;
import jp.mc.ancientred.jointblock.common.CommonProxyClient;
import jp.mc.ancientred.jointblock.common.CommonProxyServer;
import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jbrobot.packet.JBRNetwork;
import jp.mc.ancientred.jbrobot.registry.JBRCreativeTab;
import jp.mc.ancientred.jbrobot.registry.JBRItems;
import jp.mc.ancientred.jbrobot.registry.JBRMenuTypes;
import jp.mc.ancientred.jointblock.common.CommonForgeEventHandler;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.JBSoundManager;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.export.JBBlueprintFactory;
import jp.mc.ancientred.jointblock.gui.JBMenuTypes;
import jp.mc.ancientred.jointblock.motion.BasicMotionBinder;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.registry.JBAttributes;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import jp.mc.ancientred.jointblock.registry.JBBlocks;
import jp.mc.ancientred.jointblock.registry.JBCreativeTab;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import jp.mc.ancientred.jointblock.registry.JBItems;
import jp.mc.ancientred.jointblock.registry.JBRecipes;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import net.minecraftforge.fml.loading.FMLEnvironment;
import jp.mc.ancientred.jointblock.registry.JBClientEntityTypes;

/**
 * JointBlock Remastered - Forge 1.20.1 port of JointBlock 0.8.4 (MC 1.12.2, by ARUBE).
 *
 * <p>See README.md for attribution and MIGRATION_PLAN.md for the porting status.
 */
@Mod(JointBlockRemastered.MOD_ID)
public class JointBlockRemastered {

    public static final String MOD_ID = "jointblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JointBlockRemastered() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1.12.2 JBModContainer.preInit: the mod-wide singletons every ported call site reads
        // through JBModContainer (proxy, factories, sound manager, config dir).
        JBModContainer.instance = this;
        // DistExecutor.safeRunForDist validates BOTH referents in a dev environment (it calls the
        // suppliers), which loads the client proxy - and through it LocalPlayer - on the dedicated
        // server. unsafeRunForDist only invokes the matching side's supplier, so the client class
        // is never resolved on the server.
        JBModContainer.proxy = DistExecutor.unsafeRunForDist(() -> CommonProxyClient::new, () -> CommonProxyServer::new);
        JBModContainer.actionFactory = new JBActionFactory();
        JBModContainer.blueprintFactory = new JBBlueprintFactory();
        JBModContainer.soundManager = new JBSoundManager();
        JBModContainer.configDir = FMLPaths.CONFIGDIR.get().toFile();
        JBModContainer.workerEntityModDomainedName = "modJ_JointBlock.EntityTempWorker";

        JBConfig.register();
        modBus.addListener(this::onConfigLoaded);

        JBBlocks.register(modBus);
        JBItems.register(modBus);
        JBRecipes.register(modBus);
        JBBlockEntityTypes.register(modBus);
        JBEntityTypes.register(modBus);
        if (FMLEnvironment.dist.isClient()) {
            // EntityJBClientCamera is @OnlyIn(Dist.CLIENT); its type may only be registered on the client.
            JBClientEntityTypes.register(modBus);
        }
        JBAttributes.register(modBus);
        JBCreativeTab.register(modBus);
        JBMenuTypes.register(modBus);

        // Addon half (1.12.2 mod modj_jbrobot): scan <config>/JointBlock for model packs and register
        // one item per pack. 1.12.2 did this in the addon's preInit; 1.20.1 items have to exist before
        // the registry events fire, so the scan runs in the constructor.
        JBRobotMODContainer.init(JBModContainer.configDir);
        JBRobotMODContainer.register(modBus);
        JBRItems.register(modBus);
        JBRCreativeTab.register(modBus);
        JBRMenuTypes.register(modBus);
        JBRNetwork.register();

        modBus.addListener(this::onEntityAttributeCreation);
        modBus.addListener(this::onRegisterCapabilities);
        modBus.addListener(this::onCommonSetup);

        JBNetwork.register();
        JBCommands.register();

        // 1.12.2 JBModContainer.preInit registered a CommonForgeEventHandler instance on the Forge bus
        // (login sync, capability attach, mount/item-toss/attack hooks). The ported class kept every
        // handler but was never wired, so those hooks never ran.
        MinecraftForge.EVENT_BUS.register(new CommonForgeEventHandler());
    }

    /**
     * 1.12.2 {@code JBModContainer.init}/{@code postInit}, the common half: pattern table, the
     * allowed-model-block table, the default effect item list and the motion binder singleton.
     * Deferred to {@code enqueueWork} because 1.20.1 runs common setup in parallel.
     */
    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            JBPatternCarvingUtil.setUpPatterns();
            ModelBlockRegister.registerAllowedModelBlocks();
            JBModContainer.soundManager.setupDefaultItemList();
            BasicMotionBinder.instance = new BasicMotionBinder();
            // 1.12.2 addon postInit/serverLoad: resolve the pack items, price the catalog and build
            // the blueprint stacks; needs the registry events to have run, hence common setup.
            JBRobotMODContainer.onCommonSetup();
        });
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        MasterSlot.CapabilityMasterSlotHolder.register(event);
        event.register(CapabilityEntityCommonStorage.IEntitCommonStorage.class);
    }

    /**
     * Every {@code LivingEntity} subtype needs a supplier here; without one
     * {@code LivingEntity#getMaxHealth} reads an empty attribute map and assembled units die on
     * the first damage tick. {@code EntityJointed} is the only living entity in the mod
     * ({@code EntityTempWorker} extends {@code Entity}).
     */
    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(JBEntityTypes.JOINTED, EntityJointed.createAttributes().build());
    }

    private void onConfigLoaded(ModConfigEvent event) {
        ModConfig cfg = event.getConfig();
        if (cfg.getModId().equals(MOD_ID)) {
            JBConfig.bake();
        }
    }
}
