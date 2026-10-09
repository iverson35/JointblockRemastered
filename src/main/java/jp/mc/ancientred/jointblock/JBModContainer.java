package jp.mc.ancientred.jointblock;

import java.io.File;
import java.util.HashMap;
import jp.mc.ancientred.jointblock.action.JBActionFactory;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.common.CommonProxy;
import jp.mc.ancientred.jointblock.effect.JBSoundManager;
import jp.mc.ancientred.jointblock.export.JBBlueprintFactory;
import jp.mc.ancientred.jointblock.registry.JBBlocks;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Mod-wide context holder, ported from the 1.12.2 {@code JBModContainer}.
 *
 * <p>In 1.12.2 this class <em>was</em> the {@code @Mod} entry point; in the 1.20.1 port
 * that role belongs to {@link JointBlockRemastered}. This class is retained (without
 * the {@code @Mod} annotation) because ported call sites reference its statics directly
 * — {@code JBModContainer.proxy}, {@code JBModContainer.guiJointCoreId}, ... .
 * Registry-backed members (items, blocks) live in {@link JBItems}/{@link JBBlocks};
 * the 1.12.2 field-name aliases that used to be copied here are gone.
 */
public final class JBModContainer {

    private JBModContainer() {
    }

    public static final String MODID_LC = "jointblock";
    public static final String networkChannelName = "JointBlock";

    /** The running mod instance (1.12.2: {@code JBModContainer.instance}). */
    public static JointBlockRemastered instance;

    /** Side proxy, assigned during construction. */
    public static CommonProxy proxy;

    public static JBActionFactory actionFactory;
    public static JBBlueprintFactory blueprintFactory;
    public static JBSoundManager soundManager;
    public static HashMap<String, IJBAddon.IJBAddonLimitKey> limitKeyMap = new HashMap<>();

    /** Network channel, assigned when the packet module registers. */
    public static SimpleChannel channel;

    /** Configuration directory handed out by 1.12.2's pre-init event. */
    public static File configDir;

    public static String workerEntityModDomainedName;

    // ------------------------------------------------------------------
    // Packet types (verbatim from the 1.12.2 container)
    // ------------------------------------------------------------------
    public static final byte PACKET_TYPE_GUIACT = 10;
    public static final byte PACKET_TYPE_SEAT_OP = 12;
    public static final byte PACKET_TYPE_JOINTUPDATE = 14;
    public static final byte PACKET_TYPE_JOINTDISASSEMBLE = 18;
    public static final byte PACKET_TYPE_STRINGCHNGED = 20;
    public static final byte PACKET_TYPE_JOINTREINIT = 22;
    public static final byte PACKET_TYPE_SETTINGS_SYNC = 24;
    public static final byte PACKET_TYPE_CARVING_ACT = 26;
    public static final byte PACKET_TYPE_PALETTE_GUIACT = 28;
    public static final byte PACKET_TYPE_IMPORT_DATA = 30;
    public static final byte PACKET_TYPE_MIRROR_GUIACT = 32;
    public static final byte PACKET_TYPE_TUNING_ACT = 34;
    public static final byte PACKET_TYPE_DUMMY_EXPLODE = 36;
    public static final byte PACKET_TYPE_EXPORT_REQUESTED = 37;
    public static final byte PACKET_TYPE_CORE_SYNC = 40;
    public static final byte PACKET_TYPE_RING_DATA = 42;
    public static final byte PACKET_TYPE_PCOPY_GUIACT = 46;
    public static final byte PACKET_TYPE_TARGET_SELECT = 48;
    public static final byte PACKET_TYPE_SOUND_GUIACT = 50;
    public static final byte PACKET_TYPE_SOUND_ENTITYEVENT = 52;

    /**
     * 1.20.1 addition (no 1.12.2 counterpart). The 1.12.2 seats were real world entities, so the
     * vanilla {@code SPacketSetPassengers} carrying a seat id resolved on the client. The ported
     * seats are Forge {@code PartEntity} objects that no level entity lookup contains, so the
     * vanilla passenger packet is dropped client side; this packet carries the parent entity id and
     * the seat index instead, which the client can resolve.
     */
    public static final byte PACKET_TYPE_SEAT_RIDE_SYNC = 54;

    public static final byte PACKET_SUBTYPE_IMPORT_CARVING_DATA = 10;
    public static final byte PACKET_SUBTYPE_IMPORT_PALETTE_DATA = 20;
    public static final byte PACKET_SUBTYPE_IMPORT_ASSEMBLE_DATA = 30;
    public static final byte PACKET_SUBTYPE_IMPORT_RING_DATA = 35;
    public static final byte PACKET_SUBTYPE_IMPORT_EXPORT_REQUEST = 40;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_UNITNAME = 10;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR = 20;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME = 30;

    // ------------------------------------------------------------------
    // GUI ids (verbatim; 1.20.1 MenuTypes replace them, but ported switch
    // tables and packet payloads still carry these numbers)
    // ------------------------------------------------------------------
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

}
