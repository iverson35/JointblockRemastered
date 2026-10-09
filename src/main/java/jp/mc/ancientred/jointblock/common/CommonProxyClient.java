package jp.mc.ancientred.jointblock.common;

import java.io.IOException;
import java.util.ArrayList;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.effect.AbstractEffectAttributes;
import jp.mc.ancientred.jointblock.effect.EffectHelper;
import jp.mc.ancientred.jointblock.effect.PositionRelatedSound;
import jp.mc.ancientred.jointblock.effect.SoundHandlerHook;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityJBClientCamera;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.export.JBImportExportDataUtil;
import jp.mc.ancientred.jointblock.gui.GuiJointCorePalette;
import jp.mc.ancientred.jointblock.gui.GuiJointMirror;
import jp.mc.ancientred.jointblock.gui.GuiParamCopy;
import jp.mc.ancientred.jointblock.gui.GuiRingProgram;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJBEntityBatcher;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJBEntityBatcherRenderer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client side proxy.
 *
 * <p>Deferred to Phase 4 (render rewrite): TESR/TEISR + model registration
 * ({@code registerPreRenderHelper}/{@code registerRenderHelper}), the frustum based sticky-TE AABB
 * trick ({@code updateStickeyTeAABB}), the sticky render list ({@code addToStickyRenderList}),
 * GL display-list release ({@code freeCarvingCompiledListNum}), particle spawning
 * ({@code playPartsParticleEvent}), the {@code JointPartsRenderable}-backed parts sound
 * ({@code playPartsSoundEvent}), shader-mod detection ({@code detectShaderModInstalled}) and the
 * whole {@code ClientEventhandler} inner class (camera / render-tick / fog / hand / debug-overlay
 * events; it depends on the Phase 4 gui tuning classes and render subsystem).
 */
public class CommonProxyClient extends CommonProxy {
   public static int renderedStickeyCnt = 0;
   public static int renderedEntitysCnt = 0;
   public static int frustumHitType = 0;
   public static boolean renderFrustumCheck = false;
   public static float tickPartialSaved;
   public static ArrayList<BlockEntity> dummyTEList = new ArrayList<>();

   /**
    * 1.12.2 {@code CommonProxyClient#getClientGuiElement}: the four screens whose {@code IGuiHandler}
    * element existed only on the client ({@code getServerGuiElement} returned {@code null} for them,
    * so the server never opened a container).  They are opened directly here, which is exactly what
    * the 1.12 client half of {@code EntityPlayer#openGui} did.
    */
   @Override
   protected void openGui(int guiId, Player player, Level level, BlockPos pos, EntityJointed entityJointed) {
      super.openGui(guiId, player, level, pos, entityJointed);
      if (level == null || !level.isClientSide) {
         return;
      }

      if (guiId == JBGuiIds.guiJointCorePaleteId) {
         TileEntityJointCore teCore = BlockJointCore.getTileEntityJointerCore(level, pos);
         if (teCore != null) {
            Minecraft.getInstance().setScreen(new GuiJointCorePalette(player, teCore));
         }
      } else if (guiId == JBGuiIds.guiMirrorBuildId) {
         TileEntityJoint teJoint = BlockJointBase.getTileEntityJointerBase(level, pos);
         if (teJoint != null) {
            Minecraft.getInstance().setScreen(new GuiJointMirror(player, teJoint));
         }
      } else if (guiId == JBGuiIds.guiRingProgramId) {
         ItemStack held = CompatHelper.getCurrentEquippedItem(player);
         if (ItemRingProgram.isRingProgramItem(held)) {
            Minecraft.getInstance().setScreen(new GuiRingProgram(player, held, player.getInventory().selected));
         }
      } else if (guiId == JBGuiIds.guiJointParamCpyId) {
         TileEntityJoint teJoint = BlockJointBase.getTileEntityJointerBase(level, pos);
         ItemStack held = CompatHelper.getCurrentEquippedItem(player);
         if (teJoint != null && ItemJBCopyTool.isParamCpyTool(held)) {
            Minecraft.getInstance().setScreen(new GuiParamCopy(player, teJoint, held));
         }
      }
   }

   public static void updateStickeyTeAABB(AABB teAABB) {
      // Phase 4 (rendering): frustum test driven sticky-TE AABB update (ClippingHelper /
      // ClippingHelperImpl + JBCommonUtil.setAABBALL / setInLookRay) deferred to the render rewrite.
   }

   @Override
   public void registerPreRenderHelper() {
      // Phase 4 (rendering): ModelLoaderRegistry / ModelBakery item-variant + SubIconItemModelLoader
      // registration deferred (model layer is rewritten for 1.20.1).
   }

   @Override
   public void registerRenderHelper() {
      // Phase 4 (rendering): ClientRegistry.bindTileEntitySpecialRenderer / entity renderers /
      // item color handlers / TESR + TEISR registration deferred.
   }

   @Override
   public JointParts creteSideBasedJointParts(int partsX, int partsY, int partsZ) {
      // 1.12.2 CommonProxyClient returned the render-side subclass, which carries the carved
      // geometry / effect helper the entity renderer draws. Returning the base JointParts would
      // make RenderEntityJointed / RenderEntityBullet skip every part (they test
      // `jointParts instanceof JointPartsRenderable`).
      return new JointPartsRenderable(partsX, partsY, partsZ);
   }

   @Override
   public void onPlayerAttachedToSeat(Player riddenByEntity, EntityJointed entityJointed) {
      Minecraft mc = Minecraft.getInstance();
      if (riddenByEntity == mc.player) {
         if (entityJointed.isForceFirstPersonViewWhenBoard()) {
            // 1.12 set GameSettings.thirdPersonView = 1, i.e. the third-person-back view.
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         }

         String mountmessage = CompatHelper.formatLang("jbinfo.mount.onboard", mc.options.keyShift.getTranslatedKeyMessage().getString());
         mc.gui.getChat().addMessage(Component.literal(mountmessage));
      }

   }

   @Override
   public void attatchEntityCamera(EntitySeatPart entitySeatPart) {
      Level world = Minecraft.getInstance().level;
      EntityJBClientCamera cam = new EntityJBClientCamera(world);
      cam.parentEntity = entitySeatPart;
      entitySeatPart.entityCamera = cam;
   }

   @Override
   public void setCameraMatrix(Entity entity, double posX, double posY, double posZ, ModelViewMatrix matrix) {
      if (entity instanceof EntityJBClientCamera) {
         EntityJBClientCamera cam = (EntityJBClientCamera)entity;
         cam.updatedPosX = posX;
         cam.updatedPosY = posY;
         cam.updatedPosZ = posZ;
         matrix.setToQuat(cam.updatedRotCamQuat);
      }

   }

   @Override
   public void onEntityJBJoinedWorld(EntityJointed entityJB, Level world) {
      // 1.12 additionally re-registered every EntitySeatPart into the client world
      // (WorldClient.addEntityToWorld). In the port EntitySeatPart is a PartEntity child of the
      // EntityJointed and is no longer a separately tracked entity, so that block is obsolete.
      if (entityJB.isChangeThirdPersonViewDirtanceFar()) {
         EntityJBClientCamera cam = new EntityJBClientCamera(entityJB.level());
         cam.parentEntity = entityJB;
         entityJB.entityCamera = cam;
      }

   }

   @Override
   public void addToStickyRenderList(TypedLinkable entityLinkable) {
      // 1.12.2 kept the most recently drawn entity at the tail and re-added it whenever a new frame
      // started from a different entity. The list is only consumed by the (inert in 1.20.1)
      // sticky batcher renderer, but the bookkeeping is kept verbatim so that path stays coherent.
      if (TileEntityJBEntityBatcherRenderer.renderList.getLast() != entityLinkable) {
         TileEntityJBEntityBatcherRenderer.renderList.addLast(entityLinkable);
      }
   }

   @Override
   public void freeCarvingCompiledListNum(int num) {
      // Phase 4 (rendering): GLCompiledListManager display-list release (glGenLists/glCallList).
   }

   @Override
   public boolean setPlayerRayVec(ModelViewMatrix matrix) {
      Minecraft mc = Minecraft.getInstance();
      Player player;
      if (mc != null && (player = mc.player) != null) {
         double rayLength = 5.0;
         Vec3 vecLook = player.getViewVector(1.0F);
         Vec3 vecEyes = player.getEyePosition(1.0F);
         matrix.rayBecSt.x = vecEyes.x;
         matrix.rayBecSt.y = vecEyes.y;
         matrix.rayBecSt.z = vecEyes.z;
         matrix.rayBecEd = matrix.rayBecSt.add(vecLook.x * rayLength, vecLook.y * rayLength, vecLook.z * rayLength);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isEntityJointedAllowdTuning(EntityJointed entityJointed) {
      Minecraft mc = Minecraft.getInstance();
      Player player;
      return mc != null && (player = mc.player) != null ? entityJointed.isPlayerUnitOwner(player) : false;
   }

   @Override
   public CompoundTag getNBTImportDataAsSingle(Player player) {
      try {
         return JBImportExportDataUtil.getBase64edNBTFromClientClipboard(player);
      } catch (IOException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public String formatLang(String translateKey, Object... parameters) {
      return I18n.get(translateKey, parameters);
   }

   @Override
   public void playPartsParticleEvent(JointParts parts, EntityJointed entity, AbstractEffectAttributes effectSlot) {
      // Phase 4 (rendering): needs JointPartsRenderable.getEffectHelper() plus the 1.20.1
      // particle engine (Minecraft.level.addParticle / ParticleTypes) deferred.
   }

   @Override
   public void playPartsSoundEvent(JointParts parts, Entity entity, AbstractEffectAttributes soundSlot) {
      // Phase 4 (rendering): the sound position comes from JointPartsRenderable.getEffectHelper(),
      // which only exists in the render-side subclass - deferred.
   }

   @Override
   public void playEntitySoundEvent(Entity entity, AbstractEffectAttributes soundSlot) {
      if (entity == null || entity.level().isClientSide) {
         if (SoundHandlerHook.validatePlay(entity, soundSlot)) {
            WeighedSoundEvents accessor = SoundHandlerHook.getSoundEventAccessor(soundSlot);
            if (accessor != null) {
               EffectHelper helper = new EffectHelper(entity);
               new PositionRelatedSound(helper, accessor, soundSlot);
               SoundHandlerHook.playJBSound(soundSlot, accessor, helper);
            }
         }
      }

   }

   @Override
   public void playDefaultBulletSound(EntityBullet.BulletType bulletType, double x, double y, double z) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
         if (bulletType == EntityBullet.BulletType.lazerBeam) {
            world.playLocalSound(x, y, z, SoundEvents.SNOW_STEP, SoundSource.HOSTILE, 1.0F, 0.8F, false);
         } else {
            world.playLocalSound(x, y, z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.HOSTILE, 1.0F, 0.8F, false);
         }
      }

   }

   @Override
   public void detectShaderModInstalled() {
      // Phase 4 (rendering): 1.12 probed Tessellator's vertex builder / Optifine's
      // Shaders.shaderPackLoaded and stored the result in JBConfig.hasShaderModInstalled.
   }

   @Override
   public MinecraftServer getIntegratedServer() {
      return Minecraft.getInstance().getSingleplayerServer();
   }

   static {
      dummyTEList.add(new TileEntityJBEntityBatcher(BlockPos.ZERO, Blocks.AIR.defaultBlockState()));
   }
}
