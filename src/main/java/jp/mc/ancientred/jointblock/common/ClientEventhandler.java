package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.TargetSelect;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import jp.mc.ancientred.jointblock.carving.prebuild.PrebuildWriter;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.config.JBKeyHanlder;
import jp.mc.ancientred.jointblock.entity.EntityJBClientCamera;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.entity.IIgnoreTarget;
import jp.mc.ancientred.jointblock.gui.OverlayRingDebugRendeler;
import jp.mc.ancientred.jointblock.item.ICeramicModBoundItem;
import jp.mc.ancientred.jointblock.item.ItemJBTargetSight;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.render.TileEntityJBEntityBatcherRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only 1.20.1 port of the 1.12.2 inner class
 * {@code CommonProxyClient.ClientEventhandler} (reference: CommonProxyClient.java:1326-1608).
 *
 * <p>1.12.2 registered the inner class as an instance on {@code MinecraftForge.EVENT_BUS}; in 1.20.1
 * the class is a top level, Forge-bus subscriber with {@code static} handlers and the 1.12.2 mutable
 * instance fields ({@code languagePrev}, {@code cancelRenderHand}, {@code needCameraRollReset},
 * {@code checkShadermodTickCount}, {@code enableFakeTransform}) turned into static fields with the
 * same role.
 *
 * <p>Event mapping (1.12.2 -&gt; 1.20.1):
 * <table>
 * <caption>event map</caption>
 * <tr><td>{@code GuiScreenEvent.InitGuiEvent.Post}</td><td>{@link ScreenEvent.Init.Post}</td></tr>
 * <tr><td>{@code TickEvent.RenderTickEvent}</td><td>{@link TickEvent.RenderTickEvent} (unchanged)</td></tr>
 * <tr><td>{@code MouseEvent} (dwheel)</td><td>{@link InputEvent.MouseScrollingEvent}</td></tr>
 * <tr><td>{@code EntityViewRenderEvent.CameraSetup}</td><td>{@link ViewportEvent.ComputeCameraAngles}</td></tr>
 * <tr><td>{@code TickEvent.ClientTickEvent}</td><td>{@link TickEvent.ClientTickEvent} (unchanged)</td></tr>
 * <tr><td>{@code RenderGameOverlayEvent.Text}</td><td>{@link CustomizeGuiOverlayEvent.DebugText}</td></tr>
 * <tr><td>{@code RenderHandEvent}</td><td>{@link RenderHandEvent} (unchanged)</td></tr>
 * <tr><td>{@code RenderPlayerEvent.Pre}</td><td>{@link RenderPlayerEvent.Pre} (unchanged)</td></tr>
 * <tr><td>{@code EntityViewRenderEvent.FogDensity}</td><td>{@link ViewportEvent.RenderFog}</td></tr>
 * <tr><td>{@code EntityViewRenderEvent.FogColors}</td><td>{@link ViewportEvent.ComputeFogColor}</td></tr>
 * </table>
 *
 * <p>1.12.2 code that has no 1.20.1 counterpart:
 * <ul>
 *   <li>{@code renderGlobal.updateTileEntities(Collections.emptyList(), dummyTEList)} in the render
 *       tick: 1.12.2 {@code RenderGlobal} kept its own global tile-entity map (a TE created outside
 *       the world was re-registered into it every frame so its TESR ran). 1.20.1
 *       {@code LevelRenderer} has no such list - block entities are collected per chunk section
 *       ({@code javap LevelRenderer}: only {@code setSectionDirty*}/{@code setBlockDirty}/
 *       {@code allChanged}), so a detached TE cannot be injected. {@code
 *       CommonProxyClient.dummyTEList} therefore has no consumer in the port, and {@link
 *       TileEntityJBEntityBatcherRenderer#renderList} is driven from the renderer callbacks instead.</li>
 *   <li>{@code ClientRenderHelper.fakeTransformCamera} (the {@code GlStateManager} immediate-mode
 *       modelview hack that pulled the 1.12.2 third-person view back by the far distance): 1.20.1 has
 *       no {@code GlStateManager} matrix stack - the view matrix is built from {@code Camera} inside
 *       {@code GameRenderer}. Forge only exposes camera <em>angles</em> ({@link
 *       ViewportEvent.ComputeCameraAngles}); there is no camera-position event and
 *       {@code Camera.move/setPosition} are {@code protected} (not widened by the project AT, which
 *       covers only {@code Vec3}/{@code AABB}/{@code Entity} fields). The closest real equivalent is
 *       the mod's own client camera entity path, {@link
 *       ClientRenderHelper#setUpThirdPersonFarViewCamera} - see {@link #onClientRenderTick}.</li>
 *   <li>{@code GlStateManager.setFog(FogMode.EXP)} in the water fog override: 1.20.1 shader fog is
 *       linear (near/far planes) and exposes no fog mode; {@link ViewportEvent.RenderFog} is the only
 *       hook, so the density change is expressed as a far-plane scale.</li>
 *   <li>{@code JBReflectionUtilClient.clearMouseWheel} (reflected into LWJGL2's {@code
 *       Mouse.event_dwheel} to stop the wheel being consumed): in 1.20.1 the equivalent is
 *       canceling {@link InputEvent.MouseScrollingEvent} - {@code MouseHandler.onScroll} returns
 *       before the hotbar/spectator handling when {@code ForgeHooksClient.onMouseScroll} is
 *       canceled. The helper is gone from the port.</li>
 * </ul>
 *
 * <p>Unreachable-but-kept 1.12.2 branch: the {@code else if (event.phase == Phase.END)} tail of
 * {@link #onClientRenderTick} - a render tick's {@code type} is always {@code RENDER}, so 1.12.2
 * never reached it either ({@code TickEvent.Type.RENDER} still exists in 1.20.1, so it is ported
 * verbatim).
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventhandler {
   /** 1.12.2 {@code languagePrev}: detected language changes re-init the entry formats. */
   private static String languagePrev = "";
   /** 1.12.2 {@code cancelRenderHand}: set while the seat camera is active, consumed by {@link #onRenderHand}. */
   private static boolean cancelRenderHand = false;
   /** 1.12.2 {@code needCameraRollReset}: raised while a JB camera is active, cleared when it is not. */
   private static boolean needCameraRollReset = false;
   /** 1.12.2 {@code checkShadermodTickCount}: the render tick re-probes the shader mod every 16 frames. */
   private static int checkShadermodTickCount = 0;
   /**
    * 1.12.2 {@code enableFakeTransform}: selects how the far third-person view of a boarded unit is
    * produced. 1.12.2's {@code true} path was the GL immediate-mode fake transform, which cannot
    * exist in 1.20.1 (see the class javadoc), so the only representable mechanism - the client
    * camera entity of {@link ClientRenderHelper#setUpThirdPersonFarViewCamera} - runs under this
    * flag instead. The flag defaults to {@code true} and is never written anywhere in 1.12.2 either,
    * so the far view is active by default.
    */
   private static boolean enableFakeTransform = true;

   /** 1.12.2 {@code handleGuiSettingsChanged}: any screen init checks for a language change. */
   @SubscribeEvent
   public static void handleGuiSettingsChanged(ScreenEvent.Init.Post event) {
      String language = Minecraft.getInstance().options.languageCode;
      if (!languagePrev.equals(language)) {
         languagePrev = language;
         EntryFormats.initEntryFormats();
      }
   }

   @SubscribeEvent
   public static void onClientRenderTick(TickEvent.RenderTickEvent event) {
      Minecraft mc;
      if (event.type == TickEvent.Type.RENDER) {
         if (event.phase == TickEvent.Phase.START) {
            CommonProxyClient.renderedStickeyCnt = 0;
            CommonProxyClient.renderedEntitysCnt = 0;
            if ((++checkShadermodTickCount & 15) == 0) {
               JBModContainer.proxy.detectShaderModInstalled();
            }

            mc = Minecraft.getInstance();
            CommonProxyClient.tickPartialSaved = event.renderTickTime;
            cancelRenderHand = false;
            boolean isCamEnabled = false;
            Player player = mc.player;
            if (player != null) {
               Entity playerRidingEntity = player.getVehicle();
               if (playerRidingEntity != null && player.level() == playerRidingEntity.level() && playerRidingEntity instanceof EntitySeatPart) {
                  EntitySeatPart seat = (EntitySeatPart)playerRidingEntity;
                  if (seat.seatEnabled) {
                     EntityJointed entityJointed = seat.entityJointed;
                     if (seat.cameraModeSeatNum >= 0 && entityJointed != null) {
                        Entity eCamera = entityJointed.getCameraForNum(seat.cameraModeSeatNum, false);
                        if (eCamera instanceof EntityJBClientCamera) {
                           EntityJBClientCamera camEntity = (EntityJBClientCamera)eCamera;
                           camEntity.calculateRotationForEveryRender(event.renderTickTime);
                           mc.setCameraEntity(camEntity);
                           isCamEnabled = true;
                           cancelRenderHand = true;
                           needCameraRollReset = true;
                        }
                     } else if (enableFakeTransform && (mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK || mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) && entityJointed != null && entityJointed.isChangeThirdPersonViewDirtanceFar() && ClientRenderHelper.setUpThirdPersonFarViewCamera(mc, player, entityJointed, event.renderTickTime)) {
                        isCamEnabled = true;
                        cancelRenderHand = true;
                        needCameraRollReset = true;
                     }
                  }
               }
            }

            if (!isCamEnabled) {
               if (needCameraRollReset) {
                  needCameraRollReset = false;
               }

               if (mc.getCameraEntity() instanceof EntityJBClientCamera) {
                  mc.setCameraEntity(mc.player);
               }
            }
         }
      } else if (event.phase == TickEvent.Phase.END) {
         mc = Minecraft.getInstance();
         if (mc.getCameraEntity() instanceof EntityJBClientCamera) {
            mc.setCameraEntity(mc.player);
         }
      }

   }

   /**
    * 1.12.2 {@code onClientMouseEvent} ({@code MouseEvent}): tracks the wheel for the seat-camera
    * zoom and, when {@code JBConfig.cutoffMouseScrollWithCtrl} is set, keeps control+scroll from
    * reaching the hotbar. 1.20.1 replaced the event with {@link InputEvent.MouseScrollingEvent},
    * which only fires with no screen open ({@code MouseHandler.onScroll} posts it in the
    * {@code screen == null} branch) - exactly the state the seat camera is used in.
    */
   @SubscribeEvent
   public static void onClientMouseEvent(InputEvent.MouseScrollingEvent event) {
      double dwheel = event.getScrollDelta();
      if (dwheel > 0.0) {
         JBKeyHanlder.instance.mouseWheelState = 1;
      } else if (dwheel < 0.0) {
         JBKeyHanlder.instance.mouseWheelState = -1;
      }

      if (dwheel != 0.0 && JBConfig.cutoffMouseScrollWithCtrl && Screen.hasControlDown()) {
         event.setCanceled(true);
      }

   }

   /**
    * 1.12.2's server handled an attack packet naming an entity it considered invalid in two ways:
    * a self attack became a 0-damage hit ({@code NetHandlerPlayServer.processUseEntity}:
    * {@code if (entityIn == this.playerEntity) { attackEntityFrom(causePlayerDamage(...), 0.0F); }})
    * and anything else it rejected only warned. 1.20.1's {@code ServerGamePacketListenerImpl$1.onAttack}
    * disconnects instead ({@code multiplayer.disconnect.invalid_entity_attacked}, shown as
    * "试图攻击无效实体") when the packet names an {@code ItemEntity}, an {@code ExperienceOrb}, an
    * {@code AbstractArrow} or the player itself.
    *
    * <p>While the player rides a unit, the seat camera swaps {@code Minecraft}'s camera entity for
    * {@link EntityJBClientCamera} (see {@link #onClientRenderTick}). {@code GameRenderer.pick} casts
    * its ray from - and excludes - the camera entity rather than the player, so the player's own
    * bounding box is no longer skipped: the ray starts inside it, {@code AABB.clip} hits at distance
    * zero and {@code Minecraft.startAttack} sends the player's own id as the attack target. Cancel
    * that interaction (and any that targets a {@code PartEntity} or an {@link IIgnoreTarget}, whose
    * entity ids exist on the client only) before {@code MultiPlayerGameMode.attack} sends the packet,
    * and keep the hand swing so the server still runs {@code ItemJBMouse.onEntitySwing} and records
    * the click on the seat ({@code isRiddenbyUsedItemList}) exactly as 1.12.2 did.
    */
   @SubscribeEvent
   public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
      if (!event.isAttack()) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }

      Entity hitEntity = mc.hitResult instanceof EntityHitResult hitResult ? hitResult.getEntity() : null;
      if (hitEntity == null) {
         return;
      }

      if (hitEntity != mc.player && !(hitEntity instanceof PartEntity<?>) && !(hitEntity instanceof IIgnoreTarget)) {
         return;
      }

      event.setCanceled(true);
      event.setSwingHand(true);
   }

   /**
    * 1.12.2 {@code onCameraSetup}. The camera roll of a {@link EntityJBClientCamera} is applied here;
    * the 1.12.2 {@code enableFakeTransform} GL-matrix branch has no 1.20.1 equivalent (class javadoc)
    * - the far view is set up in {@link #onClientRenderTick} through that very camera entity.
    */
   @SubscribeEvent
   public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
      if (event.getCamera().getEntity() instanceof EntityJBClientCamera) {
         EntityJBClientCamera camEntity = (EntityJBClientCamera)event.getCamera().getEntity();
         event.setRoll(camEntity.prevCameraRoll + (camEntity.prevCameraRoll - camEntity.cameraRoll) * (float)event.getPartialTick());
      }

   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      JBKeyHanlder.instance.mouseWheelState = 0;
      Minecraft mc;
      if (event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.START) {
         mc = Minecraft.getInstance();
         if (!mc.isPaused()) {
            TileEntityJBEntityBatcherRenderer.renderList.clearDangerous();
         }

         if (StaticClientTuningStates.checkClientPlayerHoldingTuneItem()) {
            StaticClientTuningStates.isClientPlayerHoldingTuneItem = true;
            StaticClientTuningStates.target.copyFrom(StaticClientTuningStates.targetPri);
            StaticClientTuningStates.targetPri.clearWork();
            StaticClientTuningStates.targetSelected.checkSelectionEnabled();
            if (StaticClientTuningStates.targetSelected.isSetectionEnabled) {
               if (JBKeyHanlder.detectKeyOnDown(JBKeyHanlder.KEY_INDEX_DW)) {
                  StaticClientTuningStates.shiftSelectedToChild();
               }

               if (JBKeyHanlder.detectKeyOnDown(JBKeyHanlder.KEY_INDEX_UP)) {
                  StaticClientTuningStates.shiftSelectedToParent();
               }

               if (JBKeyHanlder.detectKeyOnDown(JBKeyHanlder.KEY_INDEX_LF)) {
                  StaticClientTuningStates.shiftSelectedWhithinBrothers(true);
               }

               if (JBKeyHanlder.detectKeyOnDown(JBKeyHanlder.KEY_INDEX_RI)) {
                  StaticClientTuningStates.shiftSelectedWhithinBrothers(false);
               }
            }
         } else {
            StaticClientTuningStates.isClientPlayerHoldingTuneItem = false;
            StaticClientTuningStates.target.clearWork();
            StaticClientTuningStates.targetPri.clearWork();
            StaticClientTuningStates.targetSelected.clearWork();
         }
      }

      if (event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.END) {
         mc = Minecraft.getInstance();
         CommonProxy.updateDelayedList(CommonProxy.delayedListClient);
         if (mc.player != null && mc.player.getVehicle() != null && mc.player.getVehicle() instanceof EntitySeatPart) {
            boolean wasTargetSelectBtnPressed = TargetSelect.isSelectBtnPressed;
            if (TargetSelect.isSelectBtnPressed && TargetSelect.storeSelectedFromCandidates()) {
               mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP, 2.0F));
            }

            TargetSelect.isSelectBtnPressed = false;
            if (ItemJBTargetSight.isPlayerHoldingTargetSightItem(mc.player)) {
               if (Screen.hasControlDown() && System.nanoTime() - TargetSelect.selectModeLastToggled > 250000000L) {
                  ++TargetSelect.curerntSelectMode;
                  TargetSelect.selectModeLastToggled = System.nanoTime();
               }

               if (TargetSelect.isPointSelectMode()) {
                  Entity renderViewEntity = mc.getCameraEntity();
                  double depth = 30.0;
                  Vec3 vecVp = renderViewEntity.getEyePosition(1.0F);
                  Vec3 vecLp = renderViewEntity.getViewVector(1.0F);
                  Vec3 vecRay = vecVp.add(vecLp.x * depth, vecLp.y * depth, vecLp.z * depth);
                  // 1.12.2 world.rayTraceBlocks(from, to, stopOnLiquid = true, ignoreBlockWithoutBoundingBox = false,
                  // returnLastUncollidableBlock = false) == 1.20.1 ClipContext(Block.COLLIDER, Fluid.ANY): blocks are hit
                  // through their collision shape (1.12.2 skipped empty collision boxes) and fluids through their
                  // fluid shape.
                  TargetSelect.objectMouseOver = renderViewEntity.level().clip(new ClipContext(vecVp, vecRay, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, renderViewEntity));
               } else {
                  TargetSelect.collectCandidates(mc.player, TargetSelect.isEntityLivingSelectMode());
               }

               // 1.12.2 Mouse.isButtonDown(1) (raw LWJGL2 state) -> raw GLFW state, not the key bind.
               if (GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
                  TargetSelect.isSelectBtnPressed = true;
               } else if (wasTargetSelectBtnPressed) {
                  TargetSelect.commitTargetSelection(TargetSelect.isPointSelectMode());
               }
            } else {
               TargetSelect.candidatesList.clear();
               TargetSelect.clearTargetSelection();
            }
         }

         if (JBConfig.showJBFrame > 0) {
            --JBConfig.showJBFrame;
         }

         if (JBConfig.showJBBounds > 0) {
            --JBConfig.showJBBounds;
         }

         JBConfig.doShowJBConnection = false;
         if (JBConfig.showJBConnection > 0) {
            --JBConfig.showJBConnection;
            JBConfig.doShowJBConnection = StaticClientTuningStates.isClientPlayerHoldingTuneItem;
         }

         if (PrebuildWriter.kickWriteQueue != null) {
            try {
               PrebuildWriter.runWorkThread();
            } catch (Exception var22) {
            } finally {
               PrebuildWriter.kickWriteQueue = null;
            }
         }

         if (PrebuildWriter.doneWriteQueue != null) {
            try {
               PrebuildWriter.doneWriteQueue.processPlayerMessage();
            } catch (Exception var20) {
            } finally {
               PrebuildWriter.doneWriteQueue = null;
            }
         }
      }

   }

   @SubscribeEvent
   public static void onDebugInfoOverlay(CustomizeGuiOverlayEvent.DebugText event) {
      if (Minecraft.getInstance().options.renderDebug) {
         event.getRight().add(String.format("[JB]StickeyState:%b, %b, %d, %d, %d", JBConfig.bypathEJRenderByStickyTileEntity, CommonProxyClient.renderFrustumCheck, CommonProxyClient.frustumHitType, CommonProxyClient.renderedStickeyCnt, CommonProxyClient.renderedEntitysCnt));
      }

   }

   @SubscribeEvent
   public static void onRenderHand(RenderHandEvent event) {
      if (cancelRenderHand) {
         event.setCanceled(true);
      }

   }

   /**
    * 1.12.2 {@code CommonForgeEventHandler#onDrawBlockHighlight} ->
    * {@code CommonProxyClient#processJBSelectionBoundingBox}: while the player holds a carving tool
    * and looks at a carveable joint block, draw the mod's own carved hit box instead of the vanilla
    * block outline. The port had {@code JBCarvingClientUtil#drawJBCarvedBoundingBox} but never wired
    * the event, so the carved (green) box never appeared.
    */
   @SubscribeEvent
   public static void onDrawBlockHighlight(RenderHighlightEvent.Block event) {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }

      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack) || !(stack.getItem() instanceof ICeramicModBoundItem)) {
         return;
      }

      BlockPos pos = event.getTarget().getBlockPos();
      Level level = player.level();
      if (!(level.getBlockState(pos).getBlock() instanceof BlockJointBase)) {
         return;
      }

      TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(level, pos.getX(), pos.getY(), pos.getZ());
      if (te != null && te.isValidCarveable()) {
         JBCarvingClientUtil.drawJBCarvedBoundingBox(event, te);
         event.setCanceled(true);
      }
   }

   /**
    * 1.12.2 {@code CommonForgeEventHandler#handleRenderLast} ->
    * {@code OverlayRingDebugRendeler#renderPathDebug}: the {@code /jbringdebug} navigator path
    * boxes. 1.20.1 removed {@code RenderWorldLastEvent}; the counterpart is
    * {@link RenderLevelStageEvent} at {@link RenderLevelStageEvent.Stage#AFTER_ENTITIES} (the
    * camera-relative world pose the 1.12 world-last pass also used). Without this the boxes were
    * never drawn - {@code renderPathDebug} had no caller.
    */
   @SubscribeEvent
   public static void onRenderLevelStage(RenderLevelStageEvent event) {
      if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
         OverlayRingDebugRendeler.renderPathDebug(event.getPartialTick(), event.getPoseStack(),
                 Minecraft.getInstance().renderBuffers().bufferSource());
      }
   }

   @SubscribeEvent
   public static void onPlayerRender(RenderPlayerEvent.Pre event) {
      Player entityPlayer = event.getEntity();
      if (entityPlayer != null) {
         Entity playerRidingEntity = entityPlayer.getVehicle();
         if (playerRidingEntity != null && playerRidingEntity instanceof EntitySeatPart) {
            EntitySeatPart seat = (EntitySeatPart)playerRidingEntity;
            if (seat.seatEnabled && seat.hidePlayer) {
               event.setCanceled(true);
            }
         }
      }

   }

   /**
    * 1.12.2 {@code onFogDensity}: {@code event.getState().getMaterial() == Material.WATER} became
    * {@code Camera.getFluidInCamera() == FogType.WATER} (the water FogType is exactly the water
    * material check of 1.12.2). 1.12.2 replaced the vanilla water density (0.1F, EXP) with 0.01F and
    * set {@code FogMode.EXP}; 1.20.1 shader fog is linear and has no mode/density, so the same
    * ten-times-longer visibility is expressed by scaling the vanilla water far plane (vanilla
    * 1.20.1 water: -8..96) by 10 and canceling the event (canceling makes
    * {@code ForgeHooksClient.onFogRender} push the event's near/far/shape into the shader fog).
    */
   @SubscribeEvent
   public static void onFogDensity(ViewportEvent.RenderFog event) {
      EntityJointed entityJointed = EntityJointed.getRidingEntityJB(event.getCamera().getEntity());
      if (entityJointed != null && entityJointed.unitAbility.isSubmarine() && event.getType() == FogType.WATER) {
         event.scaleFarPlaneDistance(10.0F);
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onFogColor(ViewportEvent.ComputeFogColor event) {
      EntityJointed entityJointed = EntityJointed.getRidingEntityJB(event.getCamera().getEntity());
      if (entityJointed != null && entityJointed.unitAbility.isSubmarine() && event.getCamera().getFluidInCamera() == FogType.WATER) {
         event.setBlue(0.0F);
         event.setRed(0.0F);
         event.setGreen(0.0F);
      }

   }
}
