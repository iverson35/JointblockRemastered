package jp.mc.ancientred.jointblock.export;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * THROWAWAY diagnostic harness (delete after use). Server half: assemble the CFA blueprint,
 * then board the connected client onto seat 0 and log the authoritative state each tick.
 */
@EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public final class RideProbe {
   private static final Path BP = Path.of("C:/Workplace/WorkProjects/Java/JointblockRemastered/referance/jbblueprint/CFA.txt");
   public static EntityJointed machine;
   private static EntitySeatPart seat0;
   private static int ticks;
   private static boolean boarded;
   private static boolean vantage;
   private static boolean clickTest;
   private static boolean highlightTest;

   private RideProbe() {
   }

   @SubscribeEvent
   public static void onServerStarted(ServerStartedEvent event) {
      MinecraftServer server = event.getServer();
      ServerLevel level = server.overworld();
      try {
         String text = new String(Files.readAllBytes(BP), StandardCharsets.UTF_8).trim();
         JBBlueprintFactory factory = new JBBlueprintFactory();
         CompoundTag tag = factory.createBlueprintTagFromBase64("CFA.txt", text);
         ItemStack item = factory.createBlueprintItemFromBase64("CFA.txt", text, false);
         if (tag == null || item.isEmpty()) {
            JointBlockRemastered.LOGGER.error("[RIDEPROBE] decode failed");
            return;
         }

         int x = 40;
         int z = 40;
         level.getChunk(x >> 4, z >> 4);
         int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 3;
         for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
               level.setBlockAndUpdate(new BlockPos(x + dx, y - 1, z + dz), Blocks.STONE.defaultBlockState());
            }
         }

         // In-world joint block rendering: one of each joint block in a row, and one dropped item
         // per joint block item, so a screenshot can show both the block entity renderer and the
         // item model renderer.
         net.minecraft.world.level.block.Block[] blocks = {
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointCoreBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointSeatBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointNormalBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointRotatorBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointExtenderBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointScalerBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointSliderBlock,
            jp.mc.ancientred.jointblock.registry.JBBlocks.JointAttracterBlock};
         net.minecraft.world.item.Item[] blockItems = {
            jp.mc.ancientred.jointblock.registry.JBItems.JointCoreItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointSeatItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointNormalItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointRotatorItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointExtenderItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointScalerItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointSliderItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointAttracterItem};
         for (int i = 0; i < blocks.length; i++) {
            level.setBlockAndUpdate(new BlockPos(x + 4, y - 1, z - 4 + i), blocks[i].defaultBlockState());
            net.minecraft.world.entity.item.ItemEntity drop =
               new net.minecraft.world.entity.item.ItemEntity(level, x + 3.5, y + 0.3, z - 3.5 + i * 1.0,
                  new ItemStack(blockItems[i]));
            drop.setPickUpDelay(32767);
            drop.setNoGravity(true);
            level.addFreshEntity(drop);
         }

         JointBlockRemastered.LOGGER.info("[RIDEPROBE] block row placed at x={} y={} z={}..{}", x + 4, y - 1, z - 3, z + 4);

         Entity entity = JBEntityJBFactory.creaaEntityJBFromDataTag(tag, level, null, x, y, z, 3);
         if (entity == null) {
            JointBlockRemastered.LOGGER.error("[RIDEPROBE] assemble failed");
            return;
         }

         level.addFreshEntity(entity);
         machine = (EntityJointed)entity;
         seat0 = machine.entitySeatPartArray[0];
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] assembled uuid={} pos={} exFlyable={} flyable={}",
            machine.getUUID(), machine.position(),
            machine.unitAbility != null && machine.unitAbility.isExFlyable(),
            machine.unitAbility != null && machine.unitAbility.isFlyable());
      } catch (Throwable t) {
         JointBlockRemastered.LOGGER.error("[RIDEPROBE] threw", t);
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != TickEvent.Phase.END || machine == null || machine.isRemoved()) {
         return;
      }

      ticks++;
      MinecraftServer server = event.getServer();
      if (server.getPlayerList().getPlayers().isEmpty()) {
         return;
      }

      if (ticks % 5 == 0) {
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] ground tick={} y={} dy={} onGround={} fallDistance={} vColl={} hColl={}",
            ticks, machine.getY(), machine.getDeltaMovement().y, machine.onGround(), machine.fallDistance,
            machine.verticalCollision, machine.horizontalCollision);
      }

      ServerPlayer player = server.getPlayerList().getPlayers().get(0);
      seat0 = machine.entitySeatPartArray[0];

      if (!boarded && ticks > 60 && machine.hasInitEnd && seat0.seatEnabled) {
         player.teleportTo(server.overworld(), machine.getX(), machine.getY() + 1.0, machine.getZ(), 0.0F, 0.0F);
         boolean ok = player.startRiding(seat0, true);
         JBPacketSender.sendSeatRideSyncToPlayers(machine, seat0, player, true);
         boarded = true;
         for (var item : new net.minecraft.world.item.Item[]{
            jp.mc.ancientred.jointblock.registry.JBItems.JointCoreItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointSeatItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointNormalItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointRotatorItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointExtenderItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointScalerItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointSliderItem,
            jp.mc.ancientred.jointblock.registry.JBItems.JointAttracterItem,
            jp.mc.ancientred.jointblock.registry.JBItems.BlueprintBuilderItem}) {
            player.getInventory().add(new ItemStack(item));
         }

         JointBlockRemastered.LOGGER.info("[RIDEPROBE] boarded {} ok={} vehicle={} seatRider={}",
            player.getName().getString(), ok, player.getVehicle(), CompatHelper.getRiddenByEntity(seat0));
         ticks = 0;
      }

      if (boarded && ticks % 10 == 0) {
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] srv tick={} playerYaw={} machineYaw={} shift={} vehicle={} sneakPress={} sneakDbl={} canDetach={} yawChange={} yawValue={} spinning={}",
            ticks, player.getYRot(), machine.getYRot(), player.isShiftKeyDown(), player.getVehicle() != null,
            seat0.isRiddenbyPressingSneakKey, seat0.isRiddenbyDoublePressedSneakKey, seat0.canDetach,
            seat0.yawChange, seat0.yawValue, machine.spinningRate);
      }

      // Rider input -> movement: hold the forward key for a while and watch the machine travel.
      if (boarded && ticks >= 100 && ticks <= 260 && ticks % 10 == 0) {
         seat0.receivedPlayerOP(0, 1);
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] drive tick={} pos={} yaw={} seatFwd={} hasPlayerRider={} cutOP={} vehicle={} ridden={}",
            ticks, machine.position(), machine.getYRot(), seat0.getMoveForwardDR(), seat0.hasPlayerRider(), seat0.cutDefaultOP,
            player.getVehicle(), CompatHelper.getRiddenByEntity(seat0));
      }

      // Direct movement check: feed a forward input and drive one travel step, so the movement
      // machinery is exercised regardless of whether the client's own (idle) input packet wins.
      if (boarded && ticks == 200) {
         seat0.receivedPlayerOP(0, 1);
         net.minecraft.world.phys.Vec3 before = machine.position();
         machine.setDeltaMovement(0.0, 0.0, 0.0);
         machine.travel(new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0));
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] directTravel before={} after={} dm={}",
            before, machine.position(), machine.getDeltaMovement());
      }

      // After the dismount, park the player in front of the block row so the client can screenshot
      // the in-world joint block models and the dropped joint block items.
      if (!vantage && ticks > 300) {
         vantage = true;
         player.teleportTo(server.overworld(), machine.getX() + 4.5, machine.getY(), machine.getZ() + 4.5, 180.0F, 15.0F);
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] vantage at {} {} {}", player.getX(), player.getY(), player.getZ());
      }

      // Carved selection box check: stand right in front of a joint block holding the carving
      // chisel, so the client's RenderHighlightEvent path resolves it.
      if (!highlightTest && ticks > 400) {
         highlightTest = true;
         ServerLevel level = server.overworld();
         BlockPos target = new BlockPos(machine.getBlockX() + 4, machine.getBlockY() - 1, machine.getBlockZ() - 4);
         player.getInventory().setItem(player.getInventory().selected,
            new ItemStack(jp.mc.ancientred.jointblock.registry.JBItems.CeramicChiselItem));
         player.teleportTo(level, target.getX() + 2.5, target.getY(), target.getZ() + 0.5, 90.0F, 0.0F);
         jp.mc.ancientred.jointblock.tileentity.TileEntityJoint te =
            jp.mc.ancientred.jointblock.block.BlockJointBase.getTileEntityJointerBase(level, target.getX(), target.getY(), target.getZ());
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] highlight vantage pos={} block={} te={} carveable={}",
            player.position(), level.getBlockState(target).getBlock(), te, te != null && te.isValidCarveable());
      }

      // Right-click test on the placed core block: an empty hand must open the core GUI, a held
      // joint block item must return PASS (the 1.12.2 onBlockActivated did the same).
      if (!clickTest && ticks > 340) {
         clickTest = true;
         ServerLevel level = server.overworld();
         BlockPos corePos = new BlockPos(machine.getBlockX() + 4, machine.getBlockY() - 1, machine.getBlockZ() - 4);
         net.minecraft.world.level.block.state.BlockState state = level.getBlockState(corePos);
         net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
            net.minecraft.world.phys.Vec3.atCenterOf(corePos), net.minecraft.core.Direction.UP, corePos, false);
         ItemStack saved = player.getInventory().getSelected().copy();

         player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
         net.minecraft.world.InteractionResult emptyResult = state.use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] click core={} at {} emptyHand -> {}",
            state.getBlock(), corePos, emptyResult);

         player.getInventory().setItem(player.getInventory().selected, new ItemStack(jp.mc.ancientred.jointblock.registry.JBItems.JointCoreItem));
         net.minecraft.world.InteractionResult heldResult = state.use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
         JointBlockRemastered.LOGGER.info("[RIDEPROBE] click core={} heldJointBlock -> {}",
            state.getBlock(), heldResult);

         player.getInventory().setItem(player.getInventory().selected, saved);
      }
   }
}
