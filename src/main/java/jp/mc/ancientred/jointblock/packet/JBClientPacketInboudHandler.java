package jp.mc.ancientred.jointblock.packet;

/**
 * Port of the 1.12.2 {@code JBClientPacketInboudHandler}.
 *
 * <p>The 1.12.2 class was a Netty {@code SimpleChannelInboundHandler<Packet>} that
 * {@code CommonForgeEventHandler.handleClientConnectedToServer} inserted after
 * {@code fml:packet_handler}, so that JointBlock payloads were consumed by the mod
 * before the vanilla handler could reject them ("priorReceiveJBPacket").
 *
 * <p>Forge 1.20.1 delivers packets through a {@code SimpleChannel}, which already
 * filters by channel and never hands the mod payload to the vanilla handler, so the
 * pipeline insertion is obsolete ({@code JBConfig.priorReceiveJBPacket} is no longer
 * consulted). What is left of the class is the client-side ingress that {@link JBNetwork}
 * dispatches every JointBlock payload through, preserving the original
 * "handle this channel's payload, nothing to forward" contract. It runs on the client
 * thread ({@code consumerMainThread}).
 */
public class JBClientPacketInboudHandler {

   /** 1.12.2 {@code channelRead0(ChannelHandlerContext, Packet)}. */
   public static void handleInbound(JBNetwork.Payload msg) {
      JBPacketHandlerClient.switchByPacketType(msg);
   }
}
