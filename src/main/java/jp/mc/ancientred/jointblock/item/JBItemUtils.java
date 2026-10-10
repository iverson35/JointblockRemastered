package jp.mc.ancientred.jointblock.item;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class JBItemUtils {
   private static final float VOLUME = 0.5F;
   protected static Random rand = new Random();

   public static void playDataRegistrationSound(Player player) {
      if (player instanceof ServerPlayer) {
         sentSoundToPlayer((ServerPlayer)player, Holder.direct(SoundEvents.EXPERIENCE_ORB_PICKUP), 1.0F);
      }
   }

   public static void playDataApplySound(Player player) {
      if (player instanceof ServerPlayer) {
         sentSoundToPlayer((ServerPlayer)player, SoundEvents.UI_BUTTON_CLICK, 1.0F);
      }
   }

   private static void sentSoundToPlayer(ServerPlayer player, Holder<SoundEvent> soundName, float pitch) {
      double px = player.getX();
      double py = player.getY();
      double pz = player.getZ();
      player.connection.send(new ClientboundSoundPacket(soundName, SoundSource.PLAYERS, px, py, pz, 0.5F, pitch, player.level().random.nextLong()));
   }
}
