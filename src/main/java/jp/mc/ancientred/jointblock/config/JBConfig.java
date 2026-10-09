package jp.mc.ancientred.jointblock.config;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Port of the 1.12.2 {@code JBConfig} (Forge {@code Configuration}) to Forge 1.20.1
 * {@link ForgeConfigSpec}.
 *
 * <p>Field names, defaults and ranges are kept identical to the original so later
 * module ports can reference them unchanged. Split into SERVER (gameplay rules,
 * synced semantics preserved where the original had "Local" mirrors) and CLIENT
 * (rendering / UI) specs.
 */
public final class JBConfig {

    private JBConfig() {
    }

    // ------------------------------------------------------------------
    // Server-side (gameplay) values
    // ------------------------------------------------------------------
    public static int maxAssembleCount = 32767;
    public static int maxAssembleMotionCount = 255;
    public static int maxAssembleXRange = 64;
    public static int maxAssembleYRange = 64;
    public static int maxAssembleZRange = 64;
    public static int maxInitLayers = 5;
    public static int baseUnitHealth = 100;
    public static int baseUnitEnergy = 100;
    public static boolean enableCarving = true;
    public static int maxCarvingDivide = 4;
    public static int maxImportablePartsCount = 32767;
    public static boolean enableOverpowerNode = false;
    public static boolean enableDataImport = true;
    public static boolean enableDataExport = true;
    public static boolean disableDisassemble = false;
    public static boolean disableMasterConnectedDisassemble = true;
    public static boolean fluctuateUnitHeight = false;
    public static float fluctuateUnitHeightThreshold = 1.0F;
    public static float slaveModeEnergyHealMult = 0.25F;
    public static int entityJointedTrackingRange = 64;
    public static int entityJointedTrackingRangePow2 = 64 * 64;
    public static float unitFuelEfficiencyMult = 0.01F;
    public static double worldMaxEntityRadiusModifier = 2.0;
    public static double worldMaxEntityRadiusModifierLocal = 2.0;
    public static boolean bypathPlayerDamageToUnit = true;
    public static boolean bypathPlayerDamageToUnitLocal = true;
    public static boolean allowAnyBlockForModel = false;
    public static List<? extends String> allowedModelBlocks = List.of();

    // ------------------------------------------------------------------
    // Client-side values
    // ------------------------------------------------------------------
    public static boolean enableJointBlockParticle = true;
    public static boolean enableJointBlockSounds = true;
    public static boolean priorReceiveJBPacket = true;
    public static boolean disableCarvingGLCompile = false;
    public static int unitStatusBarOffsetX = 0;
    public static int unitStatusBarOffsetY = 0;
    public static boolean enableIndicatorWindowFromStart = true;
    public static boolean indicatorWindowEnabled = true;
    public static int indicatorWindowPosition = 2; // I_INDICATOR_POS_RIGT
    /**
     * 1.12.2 default was {@code true}: jointed units, bullets and worker effects were drawn by the
     * sticky batcher tile-entity renderer instead of the entity pass.
     *
     * <p>That path cannot be reached in 1.20.1: its trigger was
     * {@code RenderGlobal.renderEntities(Collections.emptyList(), CommonProxyClient.dummyTEList)},
     * a per-frame forced TESR pass that no longer exists, and {@code TileEntityJBEntityBatcher} is
     * registered without an owning block, so no batcher instance can ever sit in the world. The
     * batcher renderer is kept as the faithful 1.12.2 artefact, but the default is now the entity
     * renderer path ({@code RenderEntityJointed}/{@code RenderEntityBullet}), which is the complete
     * 1.20.1 port of the same drawing code.
     */
    public static boolean bypathEJRenderByStickyTileEntity = false;
    public static boolean cutoffMouseScrollWithCtrl = false;

    // ------------------------------------------------------------------
    // Runtime-only statics (never persisted; the 1.12.2 config loader did not read
    // them either — they are set at runtime by the packet sync and the debug commands)
    // ------------------------------------------------------------------
    /** Client mirror of {@link #maxInitLayers}, filled by {@code PACKET_TYPE_SETTINGS_SYNC}. */
    public static int maxInitLayersLocal = 127;
    /** Toggled by {@code /jbdebugglist}: dumps the GL display-list state overlay. */
    public static boolean debugGLListState = false;
    /** {@code /jbshowframe <n>}: 0 = off, >0 renders joint frames. */
    public static int showJBFrame = 0;
    /** {@code /jbshowbounds <n>}: 0 = off, >0 renders joint bounds. */
    public static int showJBBounds = 0;
    /** {@code /jbshowconnection <n>}: 0 = off, >0 renders joint connections. */
    public static int showJBConnection = 0;
    public static boolean doShowJBConnection = false;
    /** {@code /jbringdebug [off|on|0-4]}: ring-program path debug overlay. */
    public static boolean enableDebugRing = false;
    public static int debugRingTargetNum = 0;
    /** Indicator window "detail" mode, enabled by {@code /jbtoggleindicator don}. */
    public static boolean enableDebugIndicates = false;

    // ------------------------------------------------------------------
    // Constants kept from the original
    // ------------------------------------------------------------------
    public static final int I_INDICATOR_POS_LEFT = 0;
    public static final int I_INDICATOR_POS_CENT = 1;
    public static final int I_INDICATOR_POS_RIGT = 2;

    /** Third-person-view far multipliers used by the seat camera. */
    public static final float THISMODS_TPVF_VALUE_L1 = 4.0374002F;
    public static final float THISMODS_TPVF_VALUE_L2 = 8.0374F;
    public static final float THISMODS_TPVF_VALUE_L3 = 12.037399F;
    public static final float THISMODS_TPVF_VALUE_L4 = 16.0374F;
    public static final String[] TPV_FAR_STR_ARY = {"x2", "x3", "x4", "x5"};
    public static final float[] TPV_FAR_ARY = {
        THISMODS_TPVF_VALUE_L1, THISMODS_TPVF_VALUE_L2, THISMODS_TPVF_VALUE_L3, THISMODS_TPVF_VALUE_L4
    };

    private static ForgeConfigSpec serverSpec;
    private static ForgeConfigSpec clientSpec;

    // Server spec values
    private static ForgeConfigSpec.IntValue sMaxAssembleCount;
    private static ForgeConfigSpec.IntValue sMaxAssembleMotionCount;
    private static ForgeConfigSpec.IntValue sMaxAssembleXRange;
    private static ForgeConfigSpec.IntValue sMaxAssembleYRange;
    private static ForgeConfigSpec.IntValue sMaxAssembleZRange;
    private static ForgeConfigSpec.IntValue sMaxInitLayers;
    private static ForgeConfigSpec.IntValue sBaseUnitHealth;
    private static ForgeConfigSpec.IntValue sBaseUnitEnergy;
    private static ForgeConfigSpec.BooleanValue sEnableCarving;
    private static ForgeConfigSpec.IntValue sMaxCarvingDivide;
    private static ForgeConfigSpec.IntValue sMaxImportablePartsCount;
    private static ForgeConfigSpec.BooleanValue sEnableOverpowerNode;
    private static ForgeConfigSpec.BooleanValue sEnableDataImport;
    private static ForgeConfigSpec.BooleanValue sEnableDataExport;
    private static ForgeConfigSpec.BooleanValue sDisableDisassemble;
    private static ForgeConfigSpec.BooleanValue sDisableMasterConnectedDisassemble;
    private static ForgeConfigSpec.BooleanValue sFluctuateUnitHeight;
    private static ForgeConfigSpec.DoubleValue sFluctuateUnitHeightThreshold;
    private static ForgeConfigSpec.DoubleValue sSlaveModeEnergyHealMult;
    private static ForgeConfigSpec.IntValue sEntityJointedTrackingRange;
    private static ForgeConfigSpec.DoubleValue sUnitFuelEfficiencyMult;
    private static ForgeConfigSpec.DoubleValue sWorldMaxEntityRadiusModifier;
    private static ForgeConfigSpec.BooleanValue sBypathPlayerDamageToUnit;
    private static ForgeConfigSpec.BooleanValue sAllowAnyBlockForModel;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> sAllowedModelBlocks;

    // Client spec values
    private static ForgeConfigSpec.BooleanValue cEnableJointBlockParticle;
    private static ForgeConfigSpec.BooleanValue cEnableJointBlockSounds;
    private static ForgeConfigSpec.BooleanValue cPriorReceiveJBPacket;
    private static ForgeConfigSpec.BooleanValue cDisableCarvingGLCompile;
    private static ForgeConfigSpec.IntValue cUnitStatusBarOffsetX;
    private static ForgeConfigSpec.IntValue cUnitStatusBarOffsetY;
    private static ForgeConfigSpec.BooleanValue cEnableIndicatorWindowFromStart;
    private static ForgeConfigSpec.IntValue cIndicatorWindowPosition;
    private static ForgeConfigSpec.BooleanValue cBypathEJRenderByStickyTileEntity;
    private static ForgeConfigSpec.BooleanValue cCutoffMouseScrollWithCtrl;

    public static void register() {
        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
        server.push("server_side_config");
        sMaxAssembleCount = server.defineInRange("maxAssembleCount", 32767, 1, 32767);
        sMaxAssembleMotionCount = server.defineInRange("maxAssembleMotionCount", 255, 1, 255);
        sMaxAssembleXRange = server.defineInRange("maxAssembleXRange", 64, 0, 64);
        sMaxAssembleYRange = server.defineInRange("maxAssembleYRange", 64, 0, 64);
        sMaxAssembleZRange = server.defineInRange("maxAssembleZRange", 64, 0, 64);
        sMaxInitLayers = server.defineInRange("maxInitLayers", 5, 1, 127);
        sBaseUnitHealth = server.defineInRange("baseUnitHealth", 100, 50, 32767);
        sBaseUnitEnergy = server.defineInRange("baseUnitEnergy", 100, 50, 32767);
        sEnableCarving = server.define("enableCarving", true);
        sMaxCarvingDivide = server.defineInRange("maxCarvingDivide", 4, 2, 16);
        sMaxImportablePartsCount = server.defineInRange("maxImportablePartsCount", 32767, 0, 32767);
        sEnableOverpowerNode = server.define("enableOverpowerNode", false);
        sEnableDataImport = server.define("enableDataImport", true);
        sEnableDataExport = server.define("enableDataExport", true);
        sDisableDisassemble = server.define("disableDisassemble", false);
        sDisableMasterConnectedDisassemble = server.define("disableMasterConnectedDisassemble", true);
        sFluctuateUnitHeight = server.define("fluctuateUnitHeight", false);
        sFluctuateUnitHeightThreshold = server.defineInRange("fluctuateUnitHeightThreshold", 1.0, 0.0, 256.0);
        sSlaveModeEnergyHealMult = server.defineInRange("slaveModeEnergyHealMult", 0.25, 0.01, 1000.0);
        sEntityJointedTrackingRange = server.defineInRange("entityJointedTrackingRange", 64, 64, 256);
        sUnitFuelEfficiencyMult = server.defineInRange("unitFuelEfficiencyMult", 0.01, 0.001, 1000.0);
        sWorldMaxEntityRadiusModifier = server.defineInRange("worldMaxEntityRadiusModifier", 2.0, 2.0, 256.0);
        sBypathPlayerDamageToUnit = server.define("bypathPlayerDamageToUnit", true);
        sAllowAnyBlockForModel = server.define("allowAnyBlockForModel", false);
        sAllowedModelBlocks = server.defineList("allowedModelBlocks", List.of(), o -> o instanceof String);
        server.pop();
        serverSpec = server.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("client_side_config");
        cEnableJointBlockParticle = client.define("enableJointBlockParticle", true);
        cEnableJointBlockSounds = client.define("enableJointBlockSounds", true);
        cPriorReceiveJBPacket = client.define("priorReceiveJBPacket", true);
        cDisableCarvingGLCompile = client.define("disableCarvingGLCompile", false);
        cUnitStatusBarOffsetX = client.defineInRange("unitStatusBarOffsetX", 0, 0, 32767);
        cUnitStatusBarOffsetY = client.defineInRange("unitStatusBarOffsetY", 0, 0, 32767);
        cEnableIndicatorWindowFromStart = client.define("enableIndicatorWindowFromStart", true);
        cIndicatorWindowPosition = client.defineInRange("indicatorWindowPosition", 2, 0, 2);
        cBypathEJRenderByStickyTileEntity = client.define("bypathEJRenderByStickyTileEntity", false);
        cCutoffMouseScrollWithCtrl = client.define("cutoffMouseScrollWithCtrl", false);
        client.pop();
        clientSpec = client.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }

    /** Pulls loaded spec values into the static fields. Call on config load/reload. */
    public static void bake() {
        if (serverSpec.isLoaded()) {
            maxAssembleCount = sMaxAssembleCount.get();
            maxAssembleMotionCount = sMaxAssembleMotionCount.get();
            maxAssembleXRange = sMaxAssembleXRange.get();
            maxAssembleYRange = sMaxAssembleYRange.get();
            maxAssembleZRange = sMaxAssembleZRange.get();
            maxInitLayers = sMaxInitLayers.get();
            baseUnitHealth = sBaseUnitHealth.get();
            baseUnitEnergy = sBaseUnitEnergy.get();
            enableCarving = sEnableCarving.get();
            maxCarvingDivide = sMaxCarvingDivide.get();
            maxImportablePartsCount = sMaxImportablePartsCount.get();
            enableOverpowerNode = sEnableOverpowerNode.get();
            enableDataImport = sEnableDataImport.get();
            enableDataExport = sEnableDataExport.get();
            disableDisassemble = sDisableDisassemble.get();
            disableMasterConnectedDisassemble = sDisableMasterConnectedDisassemble.get();
            fluctuateUnitHeight = sFluctuateUnitHeight.get();
            fluctuateUnitHeightThreshold = sFluctuateUnitHeightThreshold.get().floatValue();
            slaveModeEnergyHealMult = sSlaveModeEnergyHealMult.get().floatValue();
            entityJointedTrackingRange = sEntityJointedTrackingRange.get();
            entityJointedTrackingRangePow2 = entityJointedTrackingRange * entityJointedTrackingRange;
            unitFuelEfficiencyMult = sUnitFuelEfficiencyMult.get().floatValue();
            worldMaxEntityRadiusModifier = sWorldMaxEntityRadiusModifier.get();
            worldMaxEntityRadiusModifierLocal = worldMaxEntityRadiusModifier;
            bypathPlayerDamageToUnit = sBypathPlayerDamageToUnit.get();
            bypathPlayerDamageToUnitLocal = bypathPlayerDamageToUnit;
            allowAnyBlockForModel = sAllowAnyBlockForModel.get();
            allowedModelBlocks = sAllowedModelBlocks.get();
            // NOTE: 1.12 inflated World.MAX_ENTITY_RADIUS here; the field was removed in
            // 1.17+ (section-based entity storage), so oversized units need no global hook.
        }
        if (clientSpec.isLoaded()) {
            enableJointBlockParticle = cEnableJointBlockParticle.get();
            enableJointBlockSounds = cEnableJointBlockSounds.get();
            priorReceiveJBPacket = cPriorReceiveJBPacket.get();
            disableCarvingGLCompile = cDisableCarvingGLCompile.get();
            unitStatusBarOffsetX = cUnitStatusBarOffsetX.get();
            unitStatusBarOffsetY = cUnitStatusBarOffsetY.get();
            enableIndicatorWindowFromStart = cEnableIndicatorWindowFromStart.get();
            indicatorWindowEnabled = enableIndicatorWindowFromStart;
            indicatorWindowPosition = cIndicatorWindowPosition.get();
            bypathEJRenderByStickyTileEntity = cBypathEJRenderByStickyTileEntity.get();
            cutoffMouseScrollWithCtrl = cCutoffMouseScrollWithCtrl.get();
        }
    }

}
