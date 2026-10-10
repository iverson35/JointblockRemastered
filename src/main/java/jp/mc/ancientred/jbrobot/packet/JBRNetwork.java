package jp.mc.ancientred.jbrobot.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jbrobot.gui.ContainerModelCatalog;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jbrobot.item.merchant.DefaultModelMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.DummyModelMerchantPackage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The addon's network channel, 1.12.2 {@code "JBRbt"}.
 *
 * <p>1.12.2 used an {@code FMLEventChannel} with a leading packet-type byte; the port uses one
 * {@code SimpleChannel} with one message class per 1.12.2 packet type, keeping the original type
 * numbers as the message ids so the wire layout of the catalog stays recognisable.
 */
public final class JBRNetwork {
    /** 1.12.2 {@code JBRobotMODContainer.PACKET_TYPE_GUIACT}: a catalog button action. */
    public static final byte PACKET_TYPE_GUIACT = 10;

    /** 1.12.2 {@code JBRobotMODContainer.PACKET_TYPE_PLIST_SYNC}: the server's package list. */
    public static final byte PACKET_TYPE_PLIST_SYNC = 12;

    private static final String PROTOCOL_VERSION = "1";
    // 1.12.2 used "JBRbt"; 1.20.1 resource locations reject upper case, so the port lowercases it.
    private static final String CHANNEL_NAME = "jbrbt";

    private static SimpleChannel channel;

    private JBRNetwork() {
    }

    /** The 1.12.2 {@code JBRobotMODContainer.channel} equivalent, used by {@code JBRPacketSender}. */
    public static SimpleChannel channel() {
        return channel;
    }

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(JBRobotMODContainer.ASSET_NAMESPACE, CHANNEL_NAME),
                () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        channel.messageBuilder(CatalogSyncMessage.class, PACKET_TYPE_PLIST_SYNC, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CatalogSyncMessage::encode)
                .decoder(CatalogSyncMessage::new)
                .consumerMainThread(CatalogSyncMessage::handle)
                .add();

        channel.messageBuilder(GuiActMessage.class, PACKET_TYPE_GUIACT, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GuiActMessage::encode)
                .decoder(GuiActMessage::new)
                .consumerMainThread(GuiActMessage::handle)
                .add();
    }

    /** Server -> client: which packages the server has, so the catalog can grey out missing ones. */
    public static class CatalogSyncMessage {
        private final List<Entry> entries;

        public CatalogSyncMessage(List<Entry> entries) {
            this.entries = entries;
        }

        public CatalogSyncMessage(FriendlyByteBuf buf) {
            int size = buf.readShort();
            this.entries = new ArrayList<>(Math.max(0, size));
            for (int i = 0; i < size; ++i) {
                this.entries.add(new Entry(buf.readShort(), buf.readShort(), buf.readUtf()));
            }
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeShort(this.entries.size());
            for (Entry entry : this.entries) {
                buf.writeShort(entry.entryNums);
                buf.writeShort(entry.version);
                buf.writeUtf(entry.packageId);
            }
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> receiveCatalogSync(this.entries));
            context.get().setPacketHandled(true);
        }

        /** 1.12.2 {@code JBRPacketHandlerClient.receiveCatalogSyncPacket}. */
        @OnlyIn(Dist.CLIENT)
        private static void receiveCatalogSync(List<Entry> entries) {
            JBRobotMODContainer.syncedExPackageListClient.clear();
            List<String> lackList = new ArrayList<>();

            for (Entry entry : entries) {
                if (entry.entryNums == -1 && entry.version == -1 && "default".equals(entry.packageId)) {
                    JBRobotMODContainer.syncedExPackageListClient.add(DefaultModelMerchantPackage.getInstanceUnSafe());
                    continue;
                }

                ExtraPackage pack = JBRobotMODContainer.importedExPackageMap.get(entry.packageId);
                if (pack != null && pack.modelNums + pack.blueprintNums == entry.entryNums && pack.packageVersion == entry.version) {
                    JBRobotMODContainer.syncedExPackageListClient.add(pack);
                } else {
                    JBRobotMODContainer.syncedExPackageListClient.add(new DummyModelMerchantPackage());
                    lackList.add(entry.packageId);
                }
            }

            if (!lackList.isEmpty()) {
                notifyErrorToPlayer("jbrinfo.error.packagesync.a");
                notifyErrorToPlayer("jbrinfo.error.packagesync.b", lackList.toString());
                notifyErrorToPlayer("jbrinfo.error.packagesync.c");
                notifyErrorToPlayer("jbrinfo.error.packagesync.d");
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void notifyErrorToPlayer(String key, Object... params) {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                        Component.translatable(key, params).withStyle(net.minecraft.ChatFormatting.RED), false);
            }
        }

        /** One package line of the sync: entry count, version, package id. */
        public record Entry(int entryNums, int version, String packageId) {
        }
    }

    /** Client -> server: one catalog button action (1.12.2 packet type 10). */
    public static class GuiActMessage {
        private final int id;
        private final int mtype;
        private final int packageId;
        private final int entryNum;

        public GuiActMessage(int id, int mtype, int packageId, int entryNum) {
            this.id = id;
            this.mtype = mtype;
            this.packageId = packageId;
            this.entryNum = entryNum;
        }

        public GuiActMessage(FriendlyByteBuf buf) {
            this.id = buf.readInt();
            this.mtype = buf.readInt();
            this.packageId = buf.readInt();
            this.entryNum = buf.readInt();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(this.id);
            buf.writeInt(this.mtype);
            buf.writeInt(this.packageId);
            buf.writeInt(this.entryNum);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null && player.containerMenu instanceof ContainerModelCatalog catalog) {
                    catalog.receiveButtonAction(player, this.id, this.mtype, this.packageId, this.entryNum);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
