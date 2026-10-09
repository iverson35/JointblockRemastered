package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectInventory;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.item.AbstractCarvingToolItem;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Main forge event handler (registered on {@code MinecraftForge.EVENT_BUS}).
 *
 * <p>Not ported (Phase 4 / obsolete in 1.20.1):
 * <ul>
 *   <li>{@code handleClientConnectedToServer} - 1.12 injected {@code JBClientPacketInboudHandler}
 *       into the Netty pipeline via {@code FMLNetworkEvent.ClientConnectedToServerEvent}; the
 *       1.20.1 SimpleChannel ingress (JBNetwork) replaces it.</li>
 *   <li>{@code handleOnClientCommand} - {@code net.minecraftforge.event.CommandEvent} is
 *       server-only in 1.20.1; all 17 commands are Brigadier-registered from JBCommands
 *       (see PortPacketCmdExport).</li>
 *   <li>{@code handleDrawSceen} / {@code onDrawBlockHighlight} / {@code handleRenderLast} -
 *       render events (OverlayRendeler, JBCarvingClientUtil, OverlayRingDebugRendeler).</li>
 *   <li>{@code handlePlaySound} - needs SoundHandlerHook.handlePlaySoundHook, omitted with the
 *       paulscode SoundEngine rewrite.</li>
 * </ul>
 */
public class CommonForgeEventHandler {
   @SubscribeEvent
   public void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      try {
         if (event.getEntity() instanceof ServerPlayer) {
            JBPacketSender.sendSettingSyncPacket((ServerPlayer)event.getEntity());
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   @SubscribeEvent
   public void registerExtraCapability(AttachCapabilitiesEvent<Entity> event) {
      Entity entity = event.getObject();
      if (entity instanceof Player && entity.level() != null) {
         event.addCapability(MasterSlot.CapabilityMasterSlotHolder.EXTENDED_PROPERTY_MSLOT_HOLDER, new MasterSlot.MasterSlotHolderImpl());
      }

      event.addCapability(CapabilityEntityCommonStorage.EXTENDED_PROPERTY_CSTOREAGE_HOLDER, new CapabilityEntityCommonStorage.EntitCommonStorageImpl());
   }

   @SubscribeEvent
   public void handleOnAttack(LivingAttackEvent event) {
      if (JBConfig.bypathPlayerDamageToUnitLocal) {
         LivingEntity entityTarget = event.getEntity();
         if (entityTarget != null && entityTarget.getVehicle() instanceof EntitySeatPart) {
            EntitySeatPart seatPart = (EntitySeatPart)entityTarget.getVehicle();
            seatPart.hurt(event.getSource(), event.getAmount());
            event.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public void handleOnBlockBreakEvent(BlockEvent.BreakEvent event) {
      if (event.getState().getBlock() instanceof BlockJointBase) {
         Player player = event.getPlayer();
         ItemStack currentItem = CompatHelper.getCurrentEquippedItem(player);
         if (currentItem != null && currentItem.getItem() instanceof AbstractCarvingToolItem) {
            TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            if (te != null && te.isValidCarveable()) {
               return;
            }
         }

         if (event.getLevel() instanceof Level) {
            TileEntityJoint.setMirrorBlock((Level)event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 0, event.getPlayer(), true);
         }
      }

   }

   @SubscribeEvent
   public void handleCanMountEntity(EntityMountEvent event) {
      if (!event.isDismounting()) {
         Entity entity = event.getEntityMounting();
         Entity toRide = event.getEntityBeingMounted();
         if (entity != null && toRide instanceof EntityJointed) {
            EntityJointed ej = (EntityJointed)toRide;
            if (ej.seatReMountArray != null && ej.seatReMountArray.size() > 0) {
               byte seatNum = ej.seatReMountArray.removeFirst();
               if (seatNum >= 0 && seatNum < ej.entitySeatPartArray.length && ej.entitySeatPartArray[seatNum] != null) {
                  EntitySeatPart sp = ej.entitySeatPartArray[seatNum];
                  entity.startRiding(sp);
                  event.setCanceled(true);
               }
            }
         }

      }
   }

   @SubscribeEvent
   public void handleItemTossEvent(ItemTossEvent event) {
      ItemEntity eItem = event.getEntity();
      if (eItem != null && EffectInventory.isEffectItem(eItem.getItem())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void handleOnServerWorldTick(TickEvent.LevelTickEvent event) {
      if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && event.level != null && !event.level.isClientSide) {
         CommonProxy.updateDelayedList(CommonProxy.delayedListServer);
      }

   }
}
