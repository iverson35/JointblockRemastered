package jp.mc.ancientred.jointblock.export;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * THROWAWAY diagnostic harness (delete after use). Client half: while the local player rides the
 * probe machine, log the view/seat/sneak state, drive a simulated mouse turn and a double sneak
 * press, and grab screenshots.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class RideProbeClient {
   private static int ticks = -1;
   private static boolean turned;
   private static boolean shot1;
   private static boolean shot2;
   private static boolean invShot;
   private static boolean invGrabbed;
   private static boolean worldShot1;
   private static boolean worldShot2;

   private RideProbeClient() {
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase != TickEvent.Phase.END) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null || mc.getConnection() == null) {
         return;
      }

      if (ticks < 0) {
         ticks = 0;
         return;
      }

      ticks++;
      EntityJointed machine = null;
      EntityJointed probe = RideProbe.machine;
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof EntityJointed) {
            if (probe != null && !entity.getUUID().equals(probe.getUUID())) {
               continue;
            }

            machine = (EntityJointed)entity;
            break;
         }
      }

      if (machine == null || machine.entitySeatPartArray == null) {
         if (ticks % 100 == 0) {
            JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] tick={} no machine loaded (player={})", ticks, mc.player.blockPosition());
         }

         return;
      }

      EntitySeatPart seat0 = machine.entitySeatPartArray[0];
      boolean ridingSeat0 = mc.player.getVehicle() == seat0;

      // Simulated mouse look: 20 degrees to the right, once, while riding.
      if (!turned && ridingSeat0 && ticks > 60) {
         turned = true;
         mc.player.turn(20.0, 0.0);
         JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] MOUSETURN +20 yaw={} machineYaw={}", mc.player.getYRot(), machine.getYRot());
      }

      // Simulated double sneak press.
      if (ridingSeat0) {
         if (ticks == 120 || ticks == 126) {
            mc.options.keyShift.setDown(true);
            JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] SNEAKDOWN tick={}", ticks);
         }

         if (ticks == 123 || ticks == 132) {
            mc.options.keyShift.setDown(false);
            JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] SNEAKUP tick={}", ticks);
         }
      }

      if (ticks % 5 == 0) {
         JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] tick={} riding={} playerYaw={} machineYaw={} yRotO={} shiftDown={} inputShift={} rawKey={} seatRider={} sneakPress={} sneakDbl={}",
            ticks, ridingSeat0, mc.player.getYRot(), machine.getYRot(), machine.yRotO,
            mc.player.isShiftKeyDown(), mc.player.input.shiftKeyDown, mc.options.keyShift.isDown(),
            CompatHelper.getRiddenByEntity(seat0) != null,
            seat0.isRiddenbyPressingSneakKey, seat0.isRiddenbyDoublePressedSneakKey);
      }

      if (!shot1 && ridingSeat0 && ticks > 100) {
         shot1 = true;
         Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), name -> JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] shot1 {}", name.getString()));
      }

      if (!shot2 && ridingSeat0 && ticks > 200) {
         shot2 = true;
         Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), name -> JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] shot2 {}", name.getString()));
      }

      if (!invShot && ticks > 240) {
         invShot = true;
         mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
         JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] inventory opened");
      }

      if (invShot && !invGrabbed && ticks > 260) {
         invGrabbed = true;
         Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), name -> JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] invshot {}", name.getString()));
      }

      // Close the inventory and screenshot the in-world joint block row / dropped items once the
      // server has parked the player in front of them.
      if (ticks > 300 && ticks < 380 && mc.screen != null) {
         mc.setScreen(null);
      }

      if (!worldShot1 && ticks > 380) {
         worldShot1 = true;
         Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), name -> JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] world1 {}", name.getString()));
      }

      if (!worldShot2 && ticks > 460) {
         worldShot2 = true;
         Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), name -> JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] world2 {}", name.getString()));
      }

      if (ticks % 40 == 0 && ticks > 300) {
         JointBlockRemastered.LOGGER.info("[RIDEPROBE-C] vantage tick={} pos={} yaw={} pitch={} screen={}",
            ticks, mc.player.position(), mc.player.getYRot(), mc.player.getXRot(),
            mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
      }
   }
}
