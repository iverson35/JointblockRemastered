package jp.mc.ancientred.jointblock.packet;

import jp.mc.ancientred.jointblock.JBModContainer;

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
 * consulted). The class is kept as the client-side ingress that {@link JBNetwork}
 * dispatches every JointBlock payload through, preserving the original
 * "handle this channel's payload, nothing to forward" contract.
 */
public class JBClientPacketInboudHandler {

   private final String cn;

   public JBClientPacketInboudHandler(String channelName) {
      this.cn = channelName;
   }

   /** 1.12.2 {@code channelRead0(ChannelHandlerContext, Packet)}. */
   public void channelRead(JBNetwork.Payload msg) {
      if (this.cn.equals(JBModContainer.networkChannelName)) {
         JBPacketHandlerClient.switchByPacketType(msg, true);
      }
   }

   /** 1.20.1 ingress used by {@link JBNetwork}: every payload already belongs to the channel. */
   public static void handleInbound(JBNetwork.Payload msg) {
      JBPacketHandlerClient.switchByPacketType(msg, true);
   }
}
