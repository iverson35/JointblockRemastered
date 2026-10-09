package jp.mc.ancientred.jointblock.packet;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Network layer of the 1.12.2 {@code JBModContainer.channel} ({@code FMLEventChannel}
 * + {@code NetworkRegistry.INSTANCE.newEventDrivenChannel("JointBlock")}).
 *
 * <p>Forge 1.20.1 has neither {@code FMLEventChannel} nor {@code FMLProxyPacket}, so the
 * same "one channel, first payload byte = packet type" protocol is carried by a
 * {@link SimpleChannel} with a single {@link Payload} message holding the verbatim
 * 1.12.2 payload bytes. The type/subtype byte constants are declared (verbatim) in
 * {@link JBModContainer} and re-exposed here for the packet layer.
 *
 * <p>{@link #register()} is called from the mod constructor; it registers the message
 * and publishes the channel as {@code JBModContainer.channel}. Dispatch keeps the
 * 1.12.2 threading: the handler runs on the network thread and the existing
 * server/client switch methods marshal to the game thread themselves.
 */
public final class JBNetwork {

    private JBNetwork() {
    }

    // ------------------------------------------------------------------
    // Packet type / sub type bytes (values are the 1.12.2 JBModContainer ones)
    // ------------------------------------------------------------------
    public static final byte PACKET_TYPE_GUIACT = JBModContainer.PACKET_TYPE_GUIACT;
    public static final byte PACKET_TYPE_SEAT_OP = JBModContainer.PACKET_TYPE_SEAT_OP;
    public static final byte PACKET_TYPE_JOINTUPDATE = JBModContainer.PACKET_TYPE_JOINTUPDATE;
    public static final byte PACKET_TYPE_JOINTDISASSEMBLE = JBModContainer.PACKET_TYPE_JOINTDISASSEMBLE;
    public static final byte PACKET_TYPE_STRINGCHNGED = JBModContainer.PACKET_TYPE_STRINGCHNGED;
    public static final byte PACKET_TYPE_JOINTREINIT = JBModContainer.PACKET_TYPE_JOINTREINIT;
    public static final byte PACKET_TYPE_SETTINGS_SYNC = JBModContainer.PACKET_TYPE_SETTINGS_SYNC;
    public static final byte PACKET_TYPE_CARVING_ACT = JBModContainer.PACKET_TYPE_CARVING_ACT;
    public static final byte PACKET_TYPE_PALETTE_GUIACT = JBModContainer.PACKET_TYPE_PALETTE_GUIACT;
    public static final byte PACKET_TYPE_IMPORT_DATA = JBModContainer.PACKET_TYPE_IMPORT_DATA;
    public static final byte PACKET_TYPE_MIRROR_GUIACT = JBModContainer.PACKET_TYPE_MIRROR_GUIACT;
    public static final byte PACKET_TYPE_TUNING_ACT = JBModContainer.PACKET_TYPE_TUNING_ACT;
    public static final byte PACKET_TYPE_DUMMY_EXPLODE = JBModContainer.PACKET_TYPE_DUMMY_EXPLODE;
    public static final byte PACKET_TYPE_EXPORT_REQUESTED = JBModContainer.PACKET_TYPE_EXPORT_REQUESTED;
    public static final byte PACKET_TYPE_CORE_SYNC = JBModContainer.PACKET_TYPE_CORE_SYNC;
    public static final byte PACKET_TYPE_RING_DATA = JBModContainer.PACKET_TYPE_RING_DATA;
    public static final byte PACKET_TYPE_PCOPY_GUIACT = JBModContainer.PACKET_TYPE_PCOPY_GUIACT;
    public static final byte PACKET_TYPE_TARGET_SELECT = JBModContainer.PACKET_TYPE_TARGET_SELECT;
    public static final byte PACKET_TYPE_SOUND_GUIACT = JBModContainer.PACKET_TYPE_SOUND_GUIACT;
    public static final byte PACKET_TYPE_SOUND_ENTITYEVENT = JBModContainer.PACKET_TYPE_SOUND_ENTITYEVENT;
    public static final byte PACKET_TYPE_SEAT_RIDE_SYNC = JBModContainer.PACKET_TYPE_SEAT_RIDE_SYNC;

    public static final byte PACKET_SUBTYPE_IMPORT_CARVING_DATA = JBModContainer.PACKET_SUBTYPE_IMPORT_CARVING_DATA;
    public static final byte PACKET_SUBTYPE_IMPORT_PALETTE_DATA = JBModContainer.PACKET_SUBTYPE_IMPORT_PALETTE_DATA;
    public static final byte PACKET_SUBTYPE_IMPORT_ASSEMBLE_DATA = JBModContainer.PACKET_SUBTYPE_IMPORT_ASSEMBLE_DATA;
    public static final byte PACKET_SUBTYPE_IMPORT_RING_DATA = JBModContainer.PACKET_SUBTYPE_IMPORT_RING_DATA;
    public static final byte PACKET_SUBTYPE_IMPORT_EXPORT_REQUEST = JBModContainer.PACKET_SUBTYPE_IMPORT_EXPORT_REQUEST;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_UNITNAME = JBModContainer.PACKET_SUBTYPE_STRINGCHNGED_UNITNAME;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR = JBModContainer.PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR;
    public static final byte PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME = JBModContainer.PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME;

    /** 1.12.2 used the plain channel name "JointBlock"; the SimpleChannel needs a namespaced name. */
    public static final ResourceLocation CHANNEL_NAME =
        new ResourceLocation(JBModContainer.MODID_LC, "main");

    /** Packet payloads kept the whole 1.12.2 body, so a single message type is enough. */
    private static final int MSG_PAYLOAD = 0;

    /** Version of the 1.12.2 protocol (unchanged: the payload layout is identical). */
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        CHANNEL_NAME,
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals);

    private static boolean registered;

    /** Called from the mod constructor. */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(Payload.class, MSG_PAYLOAD)
            .encoder(Payload::encode)
            .decoder(Payload::decode)
            .consumerNetworkThread(JBNetwork::handle)
            .add();
        JBModContainer.channel = CHANNEL;
    }

    private static void handle(Payload message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        if (ctx.getDirection().getReceptionSide().isServer()) {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                JBPacketHandlerServer.handleServerPacket(message, player);
            }
        } else {
            JBClientPacketInboudHandler.handleInbound(message);
        }
        ctx.setPacketHandled(true);
    }

    // ------------------------------------------------------------------
    // Sending
    // ------------------------------------------------------------------
    public static void sendToServer(Payload message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendTo(ServerPlayer player, Payload message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * Port of {@code JBPacketSender.sendToNetworksAroundPoint}: the 1.12.2 code walked the
     * player list and kept players of the same dimension whose squared distance was below
     * {@code JBConfig.entityJointedTrackingRangePow2}.
     */
    public static void sendToPlayersAround(ResourceKey<Level> dimension, double posX, double posY, double posZ, Payload message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        double rangePow2 = (double)JBConfig.entityJointedTrackingRangePow2;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension() != dimension) {
                continue;
            }
            double d4 = posX - player.getX();
            double d5 = posY - player.getY();
            double d6 = posZ - player.getZ();
            if (d4 * d4 + d5 * d5 + d6 * d6 < rangePow2) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
            }
        }
    }

    /**
     * The 1.12.2 packets were raw {@code PacketBuffer} payloads (channel name carried by
     * {@code FMLProxyPacket}). This message type carries exactly those bytes.
     */
    public static final class Payload {

        private final byte[] data;

        public Payload(FriendlyByteBuf buffer) {
            this.data = new byte[buffer.readableBytes()];
            buffer.readBytes(this.data);
        }

        private Payload(byte[] data) {
            this.data = data;
        }

        /** A fresh buffer over the payload, positioned at index 0 (1.12.2 {@code payload()}). */
        public FriendlyByteBuf payload() {
            return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.data));
        }

        private void encode(FriendlyByteBuf target) {
            target.writeByteArray(this.data);
        }

        private static Payload decode(FriendlyByteBuf source) {
            return new Payload(source.readByteArray());
        }
    }
}
