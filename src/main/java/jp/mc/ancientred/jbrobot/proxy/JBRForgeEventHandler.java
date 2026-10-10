package jp.mc.ancientred.jbrobot.proxy;

import jp.mc.ancientred.jbrobot.packet.JBRNetwork;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 1.12.2 {@code jp.mc.ancientred.jbrobot.proxy.CommonForgeEventHandler}: the addon's Forge-bus hooks.
 *
 * <p>1.12.2 registered one instance from the addon's preInit and sent the catalog package list when a
 * player logged in, so the client's {@code GuiModelCatalog} knows which packages the server has
 * ({@code JBRobotMODContainer.syncedExPackageListClient}). That is the only hook the addon had; the
 * rest of the class ({@code handleClientConnectedToServer}) is the obsolete Netty injection replaced
 * by {@link JBRNetwork}.
 */
@Mod.EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JBRForgeEventHandler {

    private JBRForgeEventHandler() {
    }

    @SubscribeEvent
    public static void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JBRPacketSender.sendCatalogSyncPacket(player);
        }
    }
}
