package jp.mc.ancientred.jointblock.gui;

/**
 * The 14 GUI ids of JointBlock 1.12.2, copied verbatim from {@code JBModContainer}
 * (same names, same numbers).  In 1.20.1 the ids no longer select an
 * {@code IGuiHandler} element; they are still used as the wire id of the
 * {@code PACKET_TYPE_GUIACT} packet and as the id of the menu-button action
 * buttons inside the ported screens, exactly as in 1.12.2.
 */
public final class JBGuiIds {

    private JBGuiIds() {
    }

    public static final int guiJointBlockId = 4596;
    public static final int guiJointCoreId = 4597;
    public static final int guiJointCorePaleteId = 4598;
    public static final int guiBlueprintBuilderId = 4599;
    public static final int guiMirrorBuildId = 4600;
    public static final int guiJointBlockExtraId = 4601;
    public static final int guiJointChestFromEntityId = 4602;
    public static final int guiJointChestFromBlockId = 4603;
    public static final int guiRingProgramId = 4604;
    public static final int guiJointParamCpyId = 4606;
    public static final int guiJointCoreAddonsId = 4607;
    public static final int guiJointCoreSoundsId = 4608;
    public static final int guiJointCoreFuelsFromEntityId = 4609;
    public static final int guiJointCoreFuelsFromBlockId = 4610;

    /**
     * 1.12.2 {@code JBRobotMODContainer.guiModelCatalogId}: the addon's model catalog, opened by the
     * {@code jbrobot:jbrobot_catalog} item. Unlike the 14 ids above it belongs to the addon mod, but
     * it shares the same {@code IGuiHandler} dispatch and the same wire id, so it lives here too.
     */
    public static final int guiModelCatalogId = 5596;
}
