package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.block.BlockBlueprintBuilder;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.ContainerBlueprintBuilder;
import jp.mc.ancientred.jointblock.gui.ContainerJointBlock;
import jp.mc.ancientred.jointblock.gui.ContainerJointBlockExtra;
import jp.mc.ancientred.jointblock.gui.ContainerJointChest;
import jp.mc.ancientred.jointblock.gui.ContainerJointCore;
import jp.mc.ancientred.jointblock.gui.ContainerJointCoreAddons;
import jp.mc.ancientred.jointblock.gui.ContainerJointCoreFuels;
import jp.mc.ancientred.jointblock.gui.ContainerJointCoreSounds;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.gui.JBMenuTypes;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

/**
 * Server half of the 1.12.2 {@code IGuiHandler}/{@code EntityPlayer#openGui} pair.
 *
 * <p>1.12.2 opened a GUI with {@code player.openGui(mod, intGuiId, world, x, y, z)}; the id was
 * resolved by {@code CommonProxy#getServerGuiElement} (container) and
 * {@code CommonProxyClient#getClientGuiElement} (screen). 1.20.1 has one object per GUI - a
 * {@link net.minecraft.world.inventory.AbstractContainerMenu} on both sides plus a screen registered
 * by {@link net.minecraft.client.gui.screens.MenuScreens#register} - so this class keeps the 1.12 id
 * dispatch and translates it into {@code NetworkHooks.openScreen} with the payload the matching
 * {@link JBMenuTypes} factory reads (a {@link BlockPos} for block GUIs, a var-int entity id for
 * entity GUIs). The ids are unchanged, so packet/command code that still speaks 1.12 ids keeps
 * working.
 *
 * <p>The client-only screens (guiJointCorePaleteId, guiMirrorBuildId, guiRingProgramId,
 * guiJointParamCpyId) had no {@code getServerGuiElement} counterpart in 1.12 - they were opened by
 * the client half of {@code openGui}. They are handled by
 * {@link CommonProxyClient#openClientGui}, not here.
 *
 * <p>Menu titles are {@link Component#empty()}: 1.12.2's containers had no title (each screen drew
 * its own labels in {@code drawGuiContainerForegroundLayer}), and the 1.20.1 {@code MenuProvider}
 * contract still needs a component.
 */
public final class JBGuiOpener {

    private JBGuiOpener() {
    }

    private static final Component NO_TITLE = Component.empty();

    /** {@code true} if {@code guiId} is one of the four screens that exist only on the client. */
    public static boolean isClientOnlyGui(int guiId) {
        return guiId == JBGuiIds.guiJointCorePaleteId
            || guiId == JBGuiIds.guiMirrorBuildId
            || guiId == JBGuiIds.guiRingProgramId
            || guiId == JBGuiIds.guiJointParamCpyId;
    }

    /**
     * 1.12.2 {@code EntityPlayer#openGui} for a block based GUI. Does nothing when the block entity
     * the id needs is not present, which is what the 1.12 {@code IGuiHandler} returning {@code null}
     * did.
     */
    public static void openBlockGui(int guiId, ServerPlayer player, Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }

        MenuProvider provider = blockProvider(guiId, level, pos);
        if (provider != null) {
            NetworkHooks.openScreen(player, provider, pos);
        }
    }

    /** 1.12.2 {@code EntityPlayer#openGui} for the two entity based GUIs (chest / fuels). */
    public static void openEntityGui(int guiId, ServerPlayer player, EntityJointed entityJointed) {
        if (entityJointed == null) {
            return;
        }

        MenuProvider provider = entityProvider(guiId, entityJointed);
        if (provider != null) {
            NetworkHooks.openScreen(player, provider, buf -> buf.writeVarInt(entityJointed.getId()));
        }
    }

    /**
     * 1.12.2 {@code JBRobotMODContainer.guiModelCatalogId}: the addon's model catalog. It is opened by
     * an item ({@code jbrobot:jbrobot_catalog}) and carries nothing beyond the player, so no payload is
     * written to the open-screen buffer (see {@code jbrobot.registry.JBRMenuTypes}).
     */
    public static void openCatalogGui(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (containerId, inv, p) -> new jp.mc.ancientred.jbrobot.gui.ContainerModelCatalog(containerId, inv), NO_TITLE));
    }

    private static MenuProvider blockProvider(int guiId, Level level, BlockPos pos) {
        if (guiId == JBGuiIds.guiJointBlockId) {
            TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointBlock(containerId, inv, te), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointBlockExtraId) {
            TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointBlockExtra(containerId, inv, te), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointChestFromBlockId) {
            TileEntityJointCore te = BlockJointCore.getTileEntityJointerCore(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointChest(JBMenuTypes.JOINT_CHEST_FROM_BLOCK, containerId, inv, te.innerChest), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointCoreId) {
            TileEntityJointCore te = BlockJointCore.getTileEntityJointerCore(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointCore(containerId, inv, te), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointCoreAddonsId) {
            TileEntityJointCore te = BlockJointCore.getTileEntityJointerCore(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointCoreAddons(containerId, inv, te), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointCoreSoundsId) {
            TileEntityJointCore te = BlockJointCore.getTileEntityJointerCore(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointCoreSounds(containerId, inv, te), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointCoreFuelsFromBlockId) {
            TileEntityJointCore te = BlockJointCore.getTileEntityJointerCore(level, pos.getX(), pos.getY(), pos.getZ());
            return te == null ? null : new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointCoreFuels(JBMenuTypes.JOINT_CORE_FUELS_FROM_BLOCK, containerId, inv, te.innerChest), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiBlueprintBuilderId) {
            BlockEntity te = level.getBlockEntity(pos);
            return te instanceof TileEntityBlueprintBuilder ? new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerBlueprintBuilder(containerId, inv, (TileEntityBlueprintBuilder)te), NO_TITLE) : null;
        }

        return null;
    }

    private static MenuProvider entityProvider(int guiId, EntityJointed entityJointed) {
        if (guiId == JBGuiIds.guiJointChestFromEntityId) {
            return new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointChest(JBMenuTypes.JOINT_CHEST_FROM_ENTITY, containerId, inv, entityJointed.innerChest), NO_TITLE);
        }

        if (guiId == JBGuiIds.guiJointCoreFuelsFromEntityId) {
            return new SimpleMenuProvider(
                (containerId, inv, player) -> new ContainerJointCoreFuels(JBMenuTypes.JOINT_CORE_FUELS_FROM_ENTITY, containerId, inv, entityJointed.innerChest), NO_TITLE);
        }

        return null;
    }
}
