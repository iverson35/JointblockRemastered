package jp.mc.ancientred.jointblock.common;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.effect.AbstractEffectAttributes;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Side proxy base. 1.12.2 implemented Forge's {@code IGuiHandler} here
 * ({@code getServerGuiElement}/{@code getClientGuiElement}); 1.20.1 replaces that pair with
 * {@code NetworkHooks.openScreen} + {@code MenuType} + a registered screen, driven by the unchanged
 * 1.12 gui ids through {@link #openGui} (see {@link JBGuiOpener} and
 * {@code jp.mc.ancientred.jointblock.gui.JBMenuTypes}).
 */
public class CommonProxy {
   public static ArrayList<Entity> delayedListClient = new ArrayList<>();
   public static ArrayList<Entity> delayedListServer = new ArrayList<>();

   public static void updateDelayedList(ArrayList<Entity> delayedList) {
      int i = 0;

      for(int len = delayedList.size(); i < len; ++i) {
         Entity each = delayedList.get(i);
         if (each instanceof EntityJointed) {
            EntityJointed ej = (EntityJointed)each;

            try {
               ej.isDelayedUpdate = true;
               Level level = each.level();
               if (!level.isClientSide) {
                  ((ServerLevel)level).tickNonPassenger(ej);
               } else {
                  ((ClientLevel)level).tickNonPassenger(ej);
               }
               if (ej.slaveAttatcher != null) {
                  ej.slaveAttatcher.resumePrevValues(ej);
               }

               for(int seatId = 0; seatId < ej.entitySeatPartArray.length; ++seatId) {
                  EntitySeatPart seatPart = ej.entitySeatPartArray[seatId];
                  Entity riddenBy = CompatHelper.getRiddenByEntity(seatPart);
                  if (seatPart != null) {
                     boolean savedURY = seatPart.updateRidersYaw;
                     seatPart.updateRidersYaw = false;
                     seatPart.positionRider(riddenBy);
                     seatPart.updateRidersYaw = savedURY;
                  }
               }
            } catch (Exception var12) {
               var12.printStackTrace();
            } finally {
               ej.isDelayedUpdate = false;
            }
         }
      }

      delayedList.clear();
   }

   public void registerPreRenderHelper() {
   }

   public void registerRenderHelper() {
   }

   public JointParts creteSideBasedJointParts(int partsX, int partsY, int partsZ) {
      return null;
   }

   public void onPlayerAttachedToSeat(Player riddenByEntity, EntityJointed entityJointed) {
   }

   public void freeCarvingCompiledListNum(int carvingCompiledListNum) {
   }

   public boolean setPlayerRayVec(ModelViewMatrix matrix) {
      return false;
   }

   public boolean isEntityJointedAllowdTuning(EntityJointed entityJointed) {
      return false;
   }

   public CompoundTag getNBTImportDataAsSingle(Player player) {
      return null;
   }

   public void attatchEntityCamera(EntitySeatPart entitySeatPart) {
   }

   public void setCameraMatrix(Entity entity, double posX, double posY, double posZ, ModelViewMatrix matrix) {
   }

   public String formatLang(String translateKey, Object... parameters) {
      return "";
   }

   public void onEntityJBJoinedWorld(EntityJointed entityJB, Level world) {
   }

   public void addToStickyRenderList(TypedLinkable entityLinkable) {
   }

   public void playPartsParticleEvent(JointParts parts, EntityJointed entity, AbstractEffectAttributes soundSlot) {
   }

   public void playPartsSoundEvent(JointParts parts, Entity entity, AbstractEffectAttributes soundSlot) {
   }

   public void playEntitySoundEvent(Entity entity, AbstractEffectAttributes soundSlot) {
   }

   public void playDefaultBulletSound(EntityBullet.BulletType bulletType, double x, double y, double z) {
   }

   /**
    * 1.12.2 {@code EntityPlayer#openGui(mod, intGuiId, world, x, y, z)}. The ids are the unchanged
    * {@code JBModContainer} ids (see {@link jp.mc.ancientred.jointblock.gui.JBGuiIds}); the server
    * half opens the container menu, and {@link CommonProxyClient#openClientGui} additionally opens
    * the four screens that exist only on the client.
    */
   public void openGui(int guiId, Player player, Level level, BlockPos pos) {
      openGui(guiId, player, level, pos, null);
   }

   /** Entity variant of {@link #openGui(int, Player, Level, BlockPos)} (chest / fuels from entity). */
   public void openGui(int guiId, Player player, EntityJointed entityJointed) {
      openGui(guiId, player, null, null, entityJointed);
   }

   protected void openGui(int guiId, Player player, Level level, BlockPos pos, EntityJointed entityJointed) {
      if (player instanceof ServerPlayer serverPlayer) {
         if (entityJointed != null) {
            JBGuiOpener.openEntityGui(guiId, serverPlayer, entityJointed);
         } else {
            JBGuiOpener.openBlockGui(guiId, serverPlayer, level, pos);
         }
      }
   }

   public void detectShaderModInstalled() {
   }

   public MinecraftServer getIntegratedServer() {
      return null;
   }
}
