package jp.mc.ancientred.jbrobot.proxy;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jbrobot.item.merchant.DefaultModelMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jbrobot.packet.JBRNetwork;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * 1.12.2 {@code jp.mc.ancientred.jbrobot.proxy.JBRPacketSender}, the sending half of the addon's
 * {@code "JBRbt"} channel.
 *
 * <p>1.12.2 built an {@code FMLProxyPacket} per call ({@code sendCatalogSyncPacket} wrote it directly,
 * {@code createGUIActPacket} returned one for the GUI to hand to
 * {@code NetHandlerPlayClient#addToSendQueue}); 1.20.1 sends the equivalent messages of
 * {@link JBRNetwork} through its {@code SimpleChannel}, so {@code createGUIActPacket} became
 * {@link #sendGuiActPacket} (the client-side GUI has nothing to do with the packet object).
 */
public final class JBRPacketSender {

    private JBRPacketSender() {
    }

    /** 1.12.2 {@code sendCatalogSyncPacket}: sent when a player logs in (packet type 12). */
    public static void sendCatalogSyncPacket(ServerPlayer player) {
        List<JBRNetwork.CatalogSyncMessage.Entry> entries = new ArrayList<>();
        for (IJBRMerchantPackage merchantPackage : JBRobotMODContainer.syncedExPackageListServer) {
            if (merchantPackage instanceof ExtraPackage pack) {
                entries.add(new JBRNetwork.CatalogSyncMessage.Entry(pack.modelNums + pack.blueprintNums, pack.packageVersion, pack.packageId));
            } else if (merchantPackage instanceof DefaultModelMerchantPackage) {
                entries.add(new JBRNetwork.CatalogSyncMessage.Entry(-1, -1, "default"));
            }
        }

        JBRNetwork.channel().send(PacketDistributor.PLAYER.with(() -> player), new JBRNetwork.CatalogSyncMessage(entries));
    }

    /**
     * 1.12.2 {@code createGUIActPacket} + {@code NetHandlerPlayClient#addToSendQueue}: one catalog
     * button action (packet type 10), sent from the client screen.
     */
    public static void sendGuiActPacket(int id, int mtype, int packageId, int entryNum) {
        JBRNetwork.channel().sendToServer(new JBRNetwork.GuiActMessage(id, mtype, packageId, entryNum));
    }
}
