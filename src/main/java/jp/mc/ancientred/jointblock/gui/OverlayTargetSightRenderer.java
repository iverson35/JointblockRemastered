package jp.mc.ancientred.jointblock.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.TargetSelect;
import jp.mc.ancientred.jointblock.common.CapabilityEntityCommonStorage;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.entity.IIgnoreTarget;
import jp.mc.ancientred.jointblock.item.ItemJBTargetSight;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * 1.12.2 OverlayTargetSightRenderer ported to Forge 1.20.1.
 *
 * <p>The candidate scan/culling, the world -&gt; screen projection, the nearest-candidate ordering
 * ({@link #addToNormalDrawList}) and the {@link TargetSelect#selectedEntityList} commit are ported
 * verbatim. The sight geometry of the 1.12 immediate-mode code is reproduced here as
 * {@code POSITION_COLOR} vertex batches obtained from {@link GuiGraphics#bufferSource()}:
 * <ul>
 *   <li>The 1.12 GL line strips ({@code GL_LINE_STRIP}, mode 3) become
 *       {@link RenderType#debugLineStrip(double)} batches; the batch is flushed with
 *       {@link GuiGraphics#flush()} at the same points the 1.12 code called
 *       {@code Tessellator.draw()}.</li>
 *   <li>The 1.12 filled triangle strips ({@code GL_TRIANGLE_STRIP}, mode 5) and the single
 *       {@code GL_TRIANGLES} (mode 4) outbound marker become
 *       {@link RenderType#debugQuads()} batches. Each strip segment {@code (outer[i], inner[i],
 *       outer[i+1], inner[i+1])} is emitted as one quad {@code (outer[i], inner[i], inner[i+1],
 *       outer[i+1])}, which covers exactly the two strip triangles of that segment; the outbound
 *       triangle is emitted as a degenerate quad with a repeated third corner. {@code debugQuads}
 *       carries translucent blending, matching the 1.12 alpha colours.</li>
 *   <li>The 1.12 model transform ({@code glTranslatef/glScalef/glRotatef} and the display-list
 *       rotations) becomes {@link PoseStack#translate}/{@link PoseStack#scale}/
 *       {@link PoseStack#mulPose}({@link Axis#ZP}) in the same order.</li>
 *   <li>The 1.12 global {@code GlStateManager.color(r,g,b,a)} state becomes the per-vertex
 *       {@code VertexConsumer.color(r,g,b,a)} (there is no global fixed-function colour any more).</li>
 *   <li>The 1.12 GL display-list cache ({@code glGenLists/glNewList/glCallList} for
 *       targetDefectRingA/B, targetDefectBoxA, targetPerfectRing1/2) is dropped: display lists do
 *       not exist in the 1.20.1 render pipeline, so the ring geometry is regenerated per frame
 *       (the 1.12 "generate once, call many" cache existed only to amortise immediate-mode
 *       re-emission, which the buffered {@code VertexConsumer} path already does). The
 *       {@code targetDefectRingA/B}, {@code targetDefectBoxA}, {@code targetPerfectRing1/2} fields
 *       are still present to mirror the 1.12 handshake the caller-visible state used.</li>
 * </ul>
 *
 * <p>1.12 obtained the world pass projection/modelview matrices through {@code JBReflectionUtilClient}
 * (the {@code ActiveRenderInfo.PROJECTION}/{@code MODELVIEW} FloatBuffers, which keep the world pass
 * values while the HUD overlay runs) and drew with the 1.12 world Frustum. 1.20.1 exposes the same
 * world pass data through RenderSystem: the inverse view rotation matrix (written only by
 * {@code GameRenderer.renderLevel}, still valid when the overlay runs) plus
 * {@code GameRenderer.getProjectionMatrix(fov)}; the FloatBuffer fields are gone and the frustum is
 * rebuilt from those matrices in the same way {@code LevelRenderer} does it. See
 * {@link #captureRenderMatrices(Minecraft)}.
 */
@OnlyIn(Dist.CLIENT)
public class OverlayTargetSightRenderer {
   private static final float SELECT_SCREEN_RANGESQ = 400.0F;
   private static final Matrix4f pMatrix = new Matrix4f();
   private static final Matrix4f mvMatrix = new Matrix4f();
   private static final Matrix4f mvpMatrix = new Matrix4f();
   private static final Vector4f posVec = new Vector4f();
   /** 1.12 kept a no-arg frustum and rebuilt it in setPosition; 1.20.1 builds it from the frame matrices. */
   private static Frustum frustrum = null;
   private static final int NORMAL_DRAW_MAX = 10;
   private static final Entity[] tgtEntityLst = new Entity[10];
   private static final float[] tgtDistLst = new float[10];
   private static final float[] tgtScreenXLst = new float[10];
   private static final float[] tgtScreenYLst = new float[10];
   private static double RenderPosX = 0.0;
   private static double RenderPosY = 0.0;
   private static double RenderPosZ = 0.0;
   private static final BlockPos.MutableBlockPos posWork = new BlockPos.MutableBlockPos();
   private static long tsRenderCount = 0L;
   private static final float[] bbPointsX = new float[8];
   private static final float[] bbPointsY = new float[8];
   /**
    * 1.12 GL display-list handles of the sight rings. 1.20.1 has no display lists, so the values are
    * only carried as the 1.12 handshake marker and the geometry is regenerated every frame.
    */
   private static int targetDefectRingA;
   private static int targetDefectRingB;
   private static int targetDefectBoxA;
   private static int targetPerfectRing1;
   private static int targetPerfectRing2;
   public static final int BG_SIGHT_COL1 = 10682320;
   public static final int BG_SIGHT_COL2 = 16777020;
   public static final int BG_SIGHT_COL3 = 13762560;
   public static final int BG_SIGHT_COL4 = 3932280;
   public static final double PI_X2 = 6.283185307179586;
   public static final double PI_D2 = 1.5707963267948966;
   public static final int RING_SEGMENTS = 80;
   public static RasterNumRenderer[] rasterNumRendererList = new RasterNumRenderer[10];
   /** 1.12 global {@code GlStateManager.color} of the 7-segment digits (set by {@link #drawTargetSight}). */
   private static float digitColR;
   private static float digitColG;
   private static float digitColB;
   private static float digitColA;

   /**
    * Fills {@link #pMatrix} / {@link #mvMatrix} / {@link #mvpMatrix} with the world camera matrices of
    * the current frame. {@code getInverseViewRotationMatrix()} is written only by
    * {@code GameRenderer.renderLevel} and is not reset by the GUI pass, so it still holds this frame's
    * view rotation here - the counterpart of the 1.12 {@code ActiveRenderInfo.MODELVIEW} capture. Like
    * the 1.12 modelview it carries no translation (the 1.12 code subtracted the camera position
    * itself), which is also how {@code LevelRenderer} builds its own frustum.
    */
   private static void captureRenderMatrices(Minecraft mc) {
      mvMatrix.set(RenderSystem.getInverseViewRotationMatrix()).invert();
      pMatrix.set(mc.gameRenderer.getProjectionMatrix((double)mc.options.fov().get()));
      mvpMatrix.set(pMatrix).mul(mvMatrix);
   }

   /**
    * 1.12 {@code renderTargetSights(RenderGameOverlayEvent.Post, Minecraft)}. Must only be called
    * while the client player rides an {@link EntitySeatPart} (the 1.12 body cast the ridden entity
    * without checking; {@link OverlayRendeler#render} keeps that guard).
    *
    * <p>Called from {@link OverlayRendeler#render(GuiGraphics, float, int, int)}, i.e. from the
    * {@code IGuiOverlay} that Main registers through {@code RegisterGuiOverlaysEvent}.
    */
   public static void renderTargetSights(GuiGraphics guiGraphics, Minecraft mc, float partialTick) {
      EntitySeatPart seatPart = (EntitySeatPart)mc.player.getVehicle();
      EntityJointed ej = seatPart.entityJointed;
      if (ej != null) {
         TargetSelect.selectedEntityList.clear();
         ItemStack currentItemStack = mc.player.getInventory().getSelected();
         boolean showPointSelection = TargetSelect.isPointSelectMode();
         boolean showSelectionCandidates = !showPointSelection && !CompatHelper.isStackEmptyOrNull(currentItemStack) && currentItemStack.getItem() instanceof ItemJBTargetSight;
         boolean showSelection = false;

         for(int i = 0; i < TargetSelect.selectedList.length && !showSelection; ++i) {
            if (TargetSelect.selectedList[i] != null) {
               showSelection = true;
            }
         }

         boolean showCurrent = false;

         for(int i = 1; i < ej.targetList.length && !showCurrent; ++i) {
            if (ej.targetList[i] != null) {
               showCurrent = true;
            }
         }

         if (showPointSelection || showSelectionCandidates || showCurrent || showSelection) {
            ++tsRenderCount;
            Entity rvEntity = mc.getCameraEntity();
            double partiald = (double)partialTick;
            RenderPosX = rvEntity.xo + (rvEntity.getX() - rvEntity.xo) * partiald;
            RenderPosY = rvEntity.yo + (rvEntity.getY() - rvEntity.yo) * partiald;
            RenderPosZ = rvEntity.zo + (rvEntity.getZ() - rvEntity.zo) * partiald;
            double d0 = RenderPosX;
            double d1 = RenderPosY;
            double d2 = RenderPosZ;
            int gameWidth = guiGraphics.guiWidth();
            int gameHeight = guiGraphics.guiHeight();
            captureRenderMatrices(mc);
            int normalDrawCnt = 0;
            if (showPointSelection) {
               if (TargetSelect.objectMouseOver != null) {
                  BlockPos pos = TargetSelect.objectMouseOver.getBlockPos();
                  int blockX = pos.getX();
                  int blockY = pos.getY();
                  int blockZ = pos.getZ();
                  drawThickOutlinedBoundingBox(guiGraphics, (double)blockX, (double)blockY, (double)blockZ, (float)gameWidth, (float)gameHeight, 16777020);
               }
            } else if (showSelectionCandidates) {
               frustrum = new Frustum(mvMatrix, pMatrix);
               frustrum.prepare(d0, d1, d2);
               List<Entity> list = TargetSelect.candidatesList;

               for(int i = 0; i < list.size(); ++i) {
                  Entity entity = (Entity)list.get(i);
                  if (!entity.isRemoved() && !(entity instanceof IIgnoreTarget) && seatPart.entityJointed != entity && !seatPart.entityJointed.isRelatedToThisEntity(entity)) {
                     boolean doRender = entity.shouldRender(d0, d1, d2) && frustrum.isVisible(entity.getBoundingBox());
                     if (doRender && (entity != rvEntity || mc.options.getCameraType() != CameraType.FIRST_PERSON) && mc.level.hasChunkAt(posWork.set(Mth.floor(entity.getX()), 0, Mth.floor(entity.getZ())))) {
                        double wpX = entity.xo + (entity.getX() - entity.xo) * partiald - d0;
                        double wpY = entity.yo + (entity.getY() - entity.yo) * partiald - d1;
                        double wpZ = entity.zo + (entity.getZ() - entity.zo) * partiald - d2;
                        wpY += (double)(entity.getBbHeight() / 2.0F);
                        posVec.set((float)wpX, (float)wpY, (float)wpZ, 1.0F);
                        mvpMatrix.transform(posVec);
                        if (posVec.z != 0.0F) {
                           posVec.x /= posVec.z;
                           posVec.y /= posVec.z;
                           float screenX = (posVec.x + 1.0F) / 2.0F * (float)gameWidth;
                           float screenY = (1.0F - posVec.y) / 2.0F * (float)gameHeight;
                           float difX = screenX - (float)(gameWidth / 2);
                           float difY = screenY - (float)(gameHeight / 2);
                           float dist = difX * difX + difY * difY;
                           normalDrawCnt = addToNormalDrawList(entity, normalDrawCnt, dist, screenX, screenY);
                        }
                     }
                  }
               }

               if (normalDrawCnt > 0 && tgtDistLst[0] < SELECT_SCREEN_RANGESQ) {
                  if (TargetSelect.isSelectBtnPressed) {
                     drawTargetSight(guiGraphics, ej, tgtEntityLst[0], (EjTarget)null, 0, tgtScreenXLst[0], tgtScreenYLst[0], 13762560, false, true, false, (TSTriangleDrawType)null, partialTick);
                     TargetSelect.selectedEntityList.add(tgtEntityLst[0]);
                  } else {
                     drawTargetSight(guiGraphics, ej, tgtEntityLst[0], (EjTarget)null, 0, tgtScreenXLst[0], tgtScreenYLst[0], 16777020, false, true, false, (TSTriangleDrawType)null, partialTick);
                  }
               }
            }

            if (showSelection) {
               drawTSFromList(guiGraphics, ej, TargetSelect.selectedList, true, partialTick);
            }

            if (showCurrent) {
               drawTSFromList(guiGraphics, ej, ej.targetList, false, partialTick);
            }

            if (showSelectionCandidates) {
               for(int i = 0; i < normalDrawCnt; ++i) {
                  drawTargetSight(guiGraphics, ej, tgtEntityLst[i], (EjTarget)null, 0, tgtScreenXLst[i], tgtScreenYLst[i], 10682320, false, false, false, (TSTriangleDrawType)null, partialTick);
                  if (TargetSelect.isSelectBtnPressed && tgtDistLst[i] < SELECT_SCREEN_RANGESQ) {
                     TargetSelect.selectedEntityList.add(tgtEntityLst[i]);
                  }
               }
            }

            for(int i = 0; i < normalDrawCnt; ++i) {
               tgtEntityLst[i] = null;
            }
         }
      }
   }

   /**
    * 1.12 {@code drawThickOutlinedBoundingBox}: projects the 8 block corners into
    * {@link #bbPointsX}/{@link #bbPointsY} (corner index {@code x &lt;&lt; 2 | y &lt;&lt; 1 | z}) and draws
    * the 12 edges as 1.12 did: a bottom line strip, a top line strip and four vertical GL_LINES
    * segments. The strips become {@link RenderType#debugLineStrip(double)} batches flushed after each
    * strip; the four disjoint vertical lines are emitted as four separate two-vertex strips (a line
    * strip cannot be restarted mid-batch), colour {@code col} at alpha 0.49803922F.
    */
   public static void drawThickOutlinedBoundingBox(GuiGraphics guiGraphics, double minX, double minY, double minZ, float gameWidth, float gameHeight, int col) {
      Entity entity = Minecraft.getInstance().getCameraEntity();
      double d0 = RenderPosX;
      double d1 = RenderPosY;
      double d2 = RenderPosZ;
      double wpMinX = minX - d0;
      double wpMinY = minY - d1;
      double wpMinZ = minZ - d2;
      double wpMaxX = minX + 1.0 - d0;
      double wpMaxY = minY + 1.0 - d1;
      double wpMaxZ = minZ + 1.0 - d2;

      for(int x = 0; x < 2; ++x) {
         double dX = x == 0 ? wpMinX : wpMaxX;

         for(int y = 0; y < 2; ++y) {
            double dY = y == 0 ? wpMinY : wpMaxY;

            for(int z = 0; z < 2; ++z) {
               double dZ = z == 0 ? wpMinZ : wpMaxZ;
               posVec.set((float)dX, (float)dY, (float)dZ, 1.0F);
               mvpMatrix.transform(posVec);
               if (posVec.z <= 0.0F) {
                  return;
               }

               posVec.x /= posVec.z;
               posVec.y /= posVec.z;
               float screenX = (posVec.x + 1.0F) / 2.0F * gameWidth;
               float screenY = (1.0F - posVec.y) / 2.0F * gameHeight;
               int idx = x << 2 | y << 1 | z;
               bbPointsX[idx] = screenX;
               bbPointsY[idx] = screenY;
            }
         }
      }

      int min = 0;
      int max = 1;
      float r = (float)(col >> 16 & 255) / 255.0F;
      float g = (float)(col >> 8 & 255) / 255.0F;
      float b = (float)(col & 255) / 255.0F;
      float a = 0.49803922F;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugLineStrip(1.0));
      addVOfIdx(vc, guiGraphics, min, min, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, min, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, min, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, min, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, min, min, r, g, b, a);
      guiGraphics.flush();
      addVOfIdx(vc, guiGraphics, min, max, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, max, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, max, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, max, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, max, min, r, g, b, a);
      guiGraphics.flush();
      addVOfIdx(vc, guiGraphics, min, min, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, max, min, r, g, b, a);
      guiGraphics.flush();
      addVOfIdx(vc, guiGraphics, max, min, min, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, max, min, r, g, b, a);
      guiGraphics.flush();
      addVOfIdx(vc, guiGraphics, max, min, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, max, max, max, r, g, b, a);
      guiGraphics.flush();
      addVOfIdx(vc, guiGraphics, min, min, max, r, g, b, a);
      addVOfIdx(vc, guiGraphics, min, max, max, r, g, b, a);
      guiGraphics.flush();
   }

   private static void addVOfIdx(VertexConsumer vc, GuiGraphics guiGraphics, int x, int y, int z, float r, float g, float b, float a) {
      int idx = x << 2 | y << 1 | z;
      vc.vertex(guiGraphics.pose().last().pose(), bbPointsX[idx], bbPointsY[idx], 0.0F).color(r, g, b, a).endVertex();
   }

   protected static int addToNormalDrawList(Entity entity, int normalDrawCnt, float dist, float screenX, float screenY) {
      int insertIdx;
      if (normalDrawCnt < tgtEntityLst.length) {
         for(insertIdx = 0; insertIdx < normalDrawCnt; ++insertIdx) {
            if (tgtDistLst[insertIdx] > dist) {
               pushRemainsForward(insertIdx, normalDrawCnt);
               putValuesToListAtIdx(insertIdx, entity, dist, screenX, screenY);
               ++normalDrawCnt;
               return normalDrawCnt;
            }
         }

         putValuesToListAtIdx(normalDrawCnt, entity, dist, screenX, screenY);
         ++normalDrawCnt;
         return normalDrawCnt;
      } else {
         insertIdx = -1;

         for(int i = normalDrawCnt - 1; i >= 0 && !(tgtDistLst[i] <= dist); insertIdx = i--) {
         }

         if (insertIdx != -1) {
            pushRemainsForward(insertIdx, normalDrawCnt - 1);
            putValuesToListAtIdx(insertIdx, entity, dist, screenX, screenY);
            return normalDrawCnt;
         } else {
            return normalDrawCnt;
         }
      }
   }

   protected static void pushRemainsForward(int startIdx, int lastIdx) {
      for(int f = lastIdx - 1; f >= startIdx; --f) {
         tgtEntityLst[f + 1] = tgtEntityLst[f];
         tgtDistLst[f + 1] = tgtDistLst[f];
         tgtScreenXLst[f + 1] = tgtScreenXLst[f];
         tgtScreenYLst[f + 1] = tgtScreenYLst[f];
      }

   }

   protected static void putValuesToListAtIdx(int idx, Entity entity, float dist, float screenX, float screenY) {
      tgtEntityLst[idx] = entity;
      tgtDistLst[idx] = dist;
      tgtScreenXLst[idx] = screenX;
      tgtScreenYLst[idx] = screenY;
   }

   protected static void drawTSFromList(GuiGraphics guiGraphics, EntityJointed ej, EjTarget[] list, boolean asCandidates, float partialTick) {
      double pt = (double)partialTick;
      double d0 = RenderPosX;
      double d1 = RenderPosY;
      double d2 = RenderPosZ;
      float gameWidthF = (float)guiGraphics.guiWidth();
      float gameHeightF = (float)guiGraphics.guiHeight();

      for(int i = 0; i < list.length; ++i) {
         if (list[i] != null) {
            Entity entity = null;
            double wpX;
            double wpY;
            double wpZ;
            if (list[i].isTargetEntity) {
               entity = list[i].targetEntity;
               if (entity == null || entity.isRemoved()) {
                  continue;
               }

               wpX = entity.xo + (entity.getX() - entity.xo) * pt - d0;
               wpY = entity.yo + (entity.getY() - entity.yo) * pt - d1;
               wpZ = entity.zo + (entity.getZ() - entity.zo) * pt - d2;
               wpY += (double)(entity.getBbHeight() / 2.0F);
            } else {
               wpX = (double)list[i].worldPosX + 0.5 - d0;
               wpY = (double)list[i].worldPosY + 0.5 - d1;
               wpZ = (double)list[i].worldPosZ + 0.5 - d2;
            }

            posVec.set((float)wpX, (float)wpY, (float)wpZ, 1.0F);
            mvpMatrix.transform(posVec);
            if (posVec.z != 0.0F) {
               posVec.x /= posVec.z;
               posVec.y /= posVec.z;
               float screenX = (posVec.x + 1.0F) / 2.0F * gameWidthF;
               float screenY = (1.0F - posVec.y) / 2.0F * gameHeightF;
               boolean outOfScreen = false;
               TSTriangleDrawType outboundTris = null;
               if (posVec.z < 0.0F) {
                  screenX = gameWidthF - Mth.clamp(screenX, 0.0F, gameWidthF);
                  screenY = gameHeightF;
                  outOfScreen = true;
               } else if (screenX <= 0.0F || screenX >= gameWidthF || screenY <= 0.0F || screenY >= gameHeightF) {
                  screenX = Mth.clamp(screenX, 0.0F, gameWidthF);
                  screenY = Mth.clamp(screenY, 0.0F, gameHeightF);
                  outOfScreen = true;
               }

               if (outOfScreen) {
                  if (screenY == 0.0F) {
                     outboundTris = OverlayTargetSightRenderer.TSTriangleDrawType.up;
                  } else if (screenY == gameHeightF) {
                     outboundTris = OverlayTargetSightRenderer.TSTriangleDrawType.bottom;
                  } else if (screenX == 0.0F) {
                     outboundTris = OverlayTargetSightRenderer.TSTriangleDrawType.left;
                  } else if (screenX == gameWidthF) {
                     outboundTris = OverlayTargetSightRenderer.TSTriangleDrawType.right;
                  }
               }

               if (asCandidates) {
                  drawTargetSight(guiGraphics, ej, entity, list[i], i + 1, screenX, screenY, 3932280, true, false, true, outboundTris, partialTick);
                  if (!list[i].isTargetEntity) {
                     drawThickOutlinedBoundingBox(guiGraphics, (double)list[i].worldPosX, (double)list[i].worldPosY, (double)list[i].worldPosZ, gameWidthF, gameHeightF, 3932280);
                  }
               } else {
                  drawTargetSight(guiGraphics, ej, entity, list[i], i, screenX, screenY, 13762560, false, false, true, outboundTris, partialTick);
                  if (!list[i].isTargetEntity) {
                     drawThickOutlinedBoundingBox(guiGraphics, (double)list[i].worldPosX, (double)list[i].worldPosY, (double)list[i].worldPosZ, gameWidthF, gameHeightF, 13762560);
                  }
               }
            }
         }
      }

   }

   /**
    * 1.12 {@code draw7SegNums}: renders {@code num} as 7-segment digits through
    * {@link #rasterNumRendererList}. {@link GuiGraphics} is threaded through because the digit
    * geometry is a buffered vertex batch now instead of immediate mode.
    */
   public static void draw7SegNums(GuiGraphics guiGraphics, int num, float span, float width, float pad, float posX, float posY) {
      int numFixed = Math.max(Math.min(num, 999), 0);
      num = numFixed;
      if (numFixed == 0) {
         rasterNumRendererList[0].render(guiGraphics, span, width, posX, posY);
      } else {
         int digitCnt;
         for(digitCnt = 0; num > 0; ++digitCnt) {
            num /= 10;
         }

         num = numFixed;
         float unit = span + pad;
         float fullWidth = unit * (float)digitCnt - pad;

         for(digitCnt = 0; num > 0; ++digitCnt) {
            int d = num % 10;
            rasterNumRendererList[d].render(guiGraphics, span, width, posX + (fullWidth - span) / 2.0F - (float)digitCnt * unit, posY);
            num /= 10;
         }

      }
   }

   /**
    * 1.12 {@code drawTargetSight}. The sight tick bookkeeping, the scale/colour computation and the
    * model transform order are kept; the immediate-mode geometry becomes {@code debugQuads} vertex
    * batches (the 7-segment digits and the four rings) and one degenerate quad for the outbound
    * triangle. The 1.12 display-list cache is not reproduced (see the class javadoc): each ring is
    * re-emitted every frame.
    */
   public static void drawTargetSight(GuiGraphics guiGraphics, EntityJointed ej, Entity entity, EjTarget target, int idx, float screenX, float screenY, int sightCol, boolean showOuterRing, boolean showOuterBox, boolean showIdx, TSTriangleDrawType trisType, float partialTick) {
      int ticks;
      if (entity != null) {
         CapabilityEntityCommonStorage.IEntitCommonStorage property = entity.getCapability(CapabilityEntityCommonStorage.CAPABILITY, Direction.UP).orElse(null);
         if (property == null) {
            return;
         }

         if (property.getTargetSightRenderGen() == tsRenderCount) {
            return;
         }

         property.setTargetSightRenderGen(tsRenderCount);
         int curTick = entity.tickCount;
         if (curTick - property.getTargetSightTick() > 10 || property.getTargetSightTickStart() > curTick) {
            property.setTargetSightTickStart(curTick);
         }

         property.setTargetSightTick(curTick);
         ticks = curTick - property.getTargetSightTickStart();
      } else {
         if (target == null) {
            return;
         }

         ticks = ej.tickCount;
      }

      float scaleXf = 1.0F;
      float scaleYf = 1.0F;
      float partial = partialTick;
      float interpolateContinues = ((float)ticks + partial) % 60.0F / 60.0F;
      float interpolateOneShot = Math.min(((float)ticks + partial) / 60.0F, 1.0F);
      float initialFrame = 0.03F;
      scaleXf = 1.0F;
      initialFrame *= 2.0F;
      scaleYf = (1.0F - Math.abs(initialFrame - interpolateOneShot) / initialFrame) * 1.8F;
      if (interpolateOneShot > initialFrame) {
         scaleYf = Math.max(scaleYf, 1.0F);
      }

      if (scaleYf > 1.0F) {
         scaleXf -= scaleYf - 1.0F;
      }

      double radius = 20.0;
      double ringWidth = 0.6;
      float r = (float)(sightCol >> 16 & 255) / 255.0F;
      float g = (float)(sightCol >> 8 & 255) / 255.0F;
      float b = (float)(sightCol & 255) / 255.0F;
      PoseStack pose = guiGraphics.pose();
      pose.pushPose();
      pose.translate(screenX, screenY, 0.0F);
      pose.scale(scaleXf, scaleYf, 1.0F);
      if (trisType != null) {
         pose.pushPose();
         pose.mulPose(Axis.ZP.rotationDegrees(trisType.rot));
         double trisX = radius * 0.5;
         double trisY = trisX * Math.sqrt(3.0);
         double trisZ = 0.0;
         VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());
         float alpha = 0.7058824F;
         localVertex(guiGraphics, vc, 0.0, 0.0, trisZ, r, g, b, alpha);
         localVertex(guiGraphics, vc, trisX, -trisY, trisZ, r, g, b, alpha);
         localVertex(guiGraphics, vc, -trisX, -trisY, trisZ, r, g, b, alpha);
         localVertex(guiGraphics, vc, -trisX, -trisY, trisZ, r, g, b, alpha);
         guiGraphics.flush();
         pose.popPose();
      } else {
         float angledeg;
         if (showIdx) {
            digitColR = r;
            digitColG = g;
            digitColB = b;
            digitColA = 0.74509805F;
            angledeg = (float)radius / 5.0F;
            draw7SegNums(guiGraphics, idx, angledeg, angledeg / 5.0F, angledeg / 5.0F, (float)(-radius) + angledeg, (float)(-radius) + angledeg);
         }

         if (showOuterBox) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(-45.0F));
            radius += ringWidth * 8.0;
            drawDefectBoxA(guiGraphics, radius, ringWidth, r, g, b, 0.6666667F);
            radius -= ringWidth * 8.0;
            pose.popPose();
         }

         if (showOuterRing) {
            pose.pushPose();
            angledeg = interpolateContinues * 360.0F * 2.0F;
            pose.mulPose(Axis.ZP.rotationDegrees(-angledeg));
            radius += ringWidth * 4.0;
            drawDefectRingB(guiGraphics, radius, ringWidth, r, g, b, 0.6666667F);
            radius -= ringWidth * 4.0;
            pose.popPose();
         }

         drawPerfectRing(guiGraphics, radius, ringWidth, r, g, b, 0.49803922F);
         pose.pushPose();
         angledeg = interpolateContinues * 360.0F;
         pose.mulPose(Axis.ZP.rotationDegrees(angledeg));
         radius -= ringWidth * 3.0;
         drawDefectRingA(guiGraphics, radius, ringWidth, r, g, b, 0.2509804F);
         pose.popPose();
         radius -= ringWidth * 2.0;
         drawPerfectRing(guiGraphics, radius, ringWidth, r, g, b, 0.49803922F);
      }

      pose.popPose();
   }

   private static void localVertex(GuiGraphics guiGraphics, VertexConsumer vc, double x, double y, double z, float r, float g, float b, float a) {
      vc.vertex(guiGraphics.pose().last().pose(), (float)x, (float)y, (float)z).color(r, g, b, a).endVertex();
   }

   /**
    * 1.12 {@code drawPerfectRing}: a closed 80-vertex {@code GL_TRIANGLE_STRIP} band, re-emitted as
    * {@link RenderType#debugQuads()} quads (one per strip segment), colour {@code (r,g,b,a)}.
    */
   public static void drawPerfectRing(GuiGraphics guiGraphics, double radius, double ringWidth, float r, float g, float b, float a) {
      double minRadius = radius - ringWidth;
      double maxRadius = radius + ringWidth;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());

      for(int i = 0; i < RING_SEGMENTS - 1; ++i) {
         quadOfStripSegment(guiGraphics, vc, PI_X2 * (double)i / 79.0 + PI_D2, PI_X2 * (double)(i + 1) / 79.0 + PI_D2, minRadius, maxRadius, r, g, b, a);
      }

      guiGraphics.flush();
   }

   /**
    * 1.12 {@code drawDefectRingA}: four 20-vertex-strip arcs, i.e. for each quadrant the sub arc of 14
    * vertices {@code [pad, 20 - pad)}, re-emitted as quads.
    */
   public static void drawDefectRingA(GuiGraphics guiGraphics, double radius, double ringWidth, float r, float g, float b, float a) {
      double minRadius = radius - ringWidth;
      double maxRadius = radius + ringWidth;
      int pad = 3;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());

      for(int p = 0; p < 4; ++p) {
         double start = PI_D2 * (double)p;

         for(int i = pad; i < 20 - pad - 1; ++i) {
            quadOfStripSegment(guiGraphics, vc, PI_X2 * (double)i / 79.0 + start, PI_X2 * (double)(i + 1) / 79.0 + start, minRadius, maxRadius, r, g, b, a);
         }
      }

      guiGraphics.flush();
   }

   /**
    * 1.12 {@code drawDefectRingB}: for each quadrant two 3-vertex-strip arcs ({@code [0, pad)} and
    * {@code [20 - pad, 20)}) separated by a {@code draw()}, re-emitted as quads.
    */
   public static void drawDefectRingB(GuiGraphics guiGraphics, double radius, double ringWidth, float r, float g, float b, float a) {
      double minRadius = radius - ringWidth;
      double maxRadius = radius + ringWidth;
      int pad = 3;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());

      for(int p = 0; p < 4; ++p) {
         double start = PI_D2 * (double)p;

         for(int i = 0; i < pad - 1; ++i) {
            quadOfStripSegment(guiGraphics, vc, PI_X2 * (double)i / 79.0 + start, PI_X2 * (double)(i + 1) / 79.0 + start, minRadius, maxRadius, r, g, b, a);
         }

         guiGraphics.flush();

         for(int i = 20 - pad; i < 20 - 1; ++i) {
            quadOfStripSegment(guiGraphics, vc, PI_X2 * (double)i / 79.0 + start, PI_X2 * (double)(i + 1) / 79.0 + start, minRadius, maxRadius, r, g, b, a);
         }

         guiGraphics.flush();
      }
   }

   /**
    * 1.12 {@code drawDefectBoxA}: one strip per quadrant combining the {@code [0, pad)} and
    * {@code [20 - pad, 20)} arcs (the strip also connects the two arcs across the gap exactly like the
    * 1.12 {@code GL_TRIANGLE_STRIP}), re-emitted as quads.
    */
   public static void drawDefectBoxA(GuiGraphics guiGraphics, double radius, double ringWidth, float r, float g, float b, float a) {
      double minRadius = radius - ringWidth;
      double maxRadius = radius + ringWidth;
      int pad = 3;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());

      for(int p = 0; p < 4; ++p) {
         double start = PI_D2 * (double)p;
         int[] idx = new int[pad * 2];
         int n = 0;

         for(int i = 0; i < pad; ++i) {
            idx[n++] = i;
         }

         for(int i = 20 - pad; i < 20; ++i) {
            idx[n++] = i;
         }

         for(int k = 0; k < n - 1; ++k) {
            quadOfStripSegment(guiGraphics, vc, PI_X2 * (double)idx[k] / 79.0 + start, PI_X2 * (double)idx[k + 1] / 79.0 + start, minRadius, maxRadius, r, g, b, a);
         }
      }

      guiGraphics.flush();
   }

   /**
    * Emits the quad of one {@code GL_TRIANGLE_STRIP} segment. A strip segment emits
    * {@code (outer[i], inner[i], outer[i+1], inner[i+1])} which realises as the quad
    * {@code (outer[i], inner[i], inner[i+1], outer[i+1])} (strip triangles {@code (v0,v1,v2)} and
    * {@code (v2,v1,v3)}). {@code debugQuads} is unculled, so the winding is irrelevant.
    */
   private static void quadOfStripSegment(GuiGraphics guiGraphics, VertexConsumer vc, double angle0, double angle1, double minRadius, double maxRadius, float r, float g, float b, float a) {
      float outer0x = (float)(Math.cos(angle0) * maxRadius);
      float outer0y = (float)(Math.sin(angle0) * maxRadius);
      float inner0x = (float)(Math.cos(angle0) * minRadius);
      float inner0y = (float)(Math.sin(angle0) * minRadius);
      float outer1x = (float)(Math.cos(angle1) * maxRadius);
      float outer1y = (float)(Math.sin(angle1) * maxRadius);
      float inner1x = (float)(Math.cos(angle1) * minRadius);
      float inner1y = (float)(Math.sin(angle1) * minRadius);
      Matrix4f mat = guiGraphics.pose().last().pose();
      vc.vertex(mat, outer0x, outer0y, 0.0F).color(r, g, b, a).endVertex();
      vc.vertex(mat, inner0x, inner0y, 0.0F).color(r, g, b, a).endVertex();
      vc.vertex(mat, inner1x, inner1y, 0.0F).color(r, g, b, a).endVertex();
      vc.vertex(mat, outer1x, outer1y, 0.0F).color(r, g, b, a).endVertex();
   }

   static {
      rasterNumRendererList[0] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderE(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[1] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[2] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderE(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[3] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[4] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[5] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[6] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderE(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[7] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[8] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderE(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
      rasterNumRendererList[9] = new RasterNumRenderer() {
         public void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
            this.renderA(guiGraphics, span, width, posX, posY);
            renderB(guiGraphics, span, width, posX, posY);
            renderC(guiGraphics, span, width, posX, posY);
            renderD(guiGraphics, span, width, posX, posY);
            renderF(guiGraphics, span, width, posX, posY);
            renderG(guiGraphics, span, width, posX, posY);
         }
      };
   }

   /**
    * 1.12 {@code RasterNumRenderer}: the 7-segment digit rasterizer. The 1.12
    * {@code GL_TRIANGLE_STRIP} thick lines become {@link RenderType#debugQuads()} quads; the colour is
    * the {@link OverlayTargetSightRenderer#digitColR}..{@code digitColA} state that
    * {@link #drawTargetSight} sets before {@link #draw7SegNums} (the counterpart of the 1.12 global
    * {@code GlStateManager.color} the digits inherited).
    */
   public abstract static class RasterNumRenderer {
      private static final double ZVAL = 0.0;
      private static final double CHIP = 0.5;
      private static final double TILT = 0.1;

      public abstract void render(GuiGraphics guiGraphics, float span, float width, float posX, float posY);

      public void renderA(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX - span / 2.0F;
         float toX = posX + span / 2.0F;
         float fromY = posY - span;
         float toY = posY - span;
         renderThickLine(guiGraphics, span, posY, width, fromX, fromY, toX, toY, true);
      }

      public static void renderB(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX + span / 2.0F;
         float toX = posX + span / 2.0F;
         float fromY = posY - span;
         renderThickLine(guiGraphics, span, posY, width, fromX, fromY, toX, posY, true);
      }

      public static void renderC(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX + span / 2.0F;
         float toX = posX + span / 2.0F;
         float toY = posY + span;
         renderThickLine(guiGraphics, span, posY, width, fromX, posY, toX, toY, true);
      }

      public static void renderD(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX + span / 2.0F;
         float toX = posX - span / 2.0F;
         float fromY = posY + span;
         float toY = posY + span;
         renderThickLine(guiGraphics, span, posY, width, fromX, fromY, toX, toY, true);
      }

      public static void renderE(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX - span / 2.0F;
         float toX = posX - span / 2.0F;
         float fromY = posY + span;
         renderThickLine(guiGraphics, span, posY, width, fromX, fromY, toX, posY, true);
      }

      public static void renderF(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX - span / 2.0F;
         float toX = posX - span / 2.0F;
         float toY = posY - span;
         renderThickLine(guiGraphics, span, posY, width, fromX, posY, toX, toY, true);
      }

      public static void renderG(GuiGraphics guiGraphics, float span, float width, float posX, float posY) {
         float fromX = posX - span / 2.0F;
         float toX = posX + span / 2.0F;
         renderThickLine(guiGraphics, span, posY, width, fromX, posY, toX, posY, false);
      }

      public static void renderThickLine(GuiGraphics guiGraphics, float span, float posY, float width, float fromX, float fromY, float toX, float toY, boolean chipTop) {
         float pad = width / 5.0F;
         width /= 2.0F;
         float xDiff = toX - fromX;
         float yDiff = toY - fromY;
         float len = Mth.sqrt(xDiff * xDiff + yDiff * yDiff);
         float xDiffNm = xDiff / len;
         float yDiffNm = yDiff / len;
         float fromXA = fromX + yDiffNm * width;
         float fromYA = fromY - xDiffNm * width;
         float fromXB = fromX - yDiffNm * width;
         float fromYB = fromY + xDiffNm * width;
         float toXA = fromXA + xDiff;
         float toYA = fromYA + yDiff;
         float toXB = fromXB + xDiff;
         float toYB = fromYB + yDiff;
         fromXA += xDiffNm * (width + pad);
         fromYA += yDiffNm * (width + pad);
         fromXB += xDiffNm * (width + pad);
         fromYB += yDiffNm * (width + pad);
         toXA -= xDiffNm * (width + pad);
         toYA -= yDiffNm * (width + pad);
         toXB -= xDiffNm * (width + pad);
         toYB -= yDiffNm * (width + pad);
         float fromXC = fromX + xDiffNm * pad;
         float fromYC = fromY + yDiffNm * pad;
         float toXC = toX - xDiffNm * pad;
         float toYC = toY - yDiffNm * pad;
         if (chipTop) {
            fromXA = (float)((double)fromXA + (double)(fromXC - fromXA) * 0.5);
            fromYA = (float)((double)fromYA + (double)(fromYC - fromYA) * 0.5);
            toXA = (float)((double)toXA + (double)(toXC - toXA) * 0.5);
            toYA = (float)((double)toYA + (double)(toYC - toYA) * 0.5);
         }

         fromXA = (float)((double)fromXA + (double)((posY - fromYA) / span * span) * 0.1);
         fromXB = (float)((double)fromXB + (double)((posY - fromYB) / span * span) * 0.1);
         fromXC = (float)((double)fromXC + (double)((posY - fromYC) / span * span) * 0.1);
         toXA = (float)((double)toXA + (double)((posY - toYA) / span * span) * 0.1);
         toXB = (float)((double)toXB + (double)((posY - toYB) / span * span) * 0.1);
         toXC = (float)((double)toXC + (double)((posY - toYC) / span * span) * 0.1);
         VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());
         Matrix4f mat = guiGraphics.pose().last().pose();
         float a = digitColA;
         // strip (fromXB, toXB, fromXC, toXC, fromXA, toXA) -> quads (v0,v1,v3,v2) and (v2,v3,v5,v4)
         vc.vertex(mat, fromXB, fromYB, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, toXB, toYB, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, toXC, toYC, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, fromXC, fromYC, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, fromXC, fromYC, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, toXC, toYC, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, toXA, toYA, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         vc.vertex(mat, fromXA, fromYA, 0.0F).color(digitColR, digitColG, digitColB, a).endVertex();
         guiGraphics.flush();
      }
   }

   private static enum TSTriangleDrawType {
      up(180.0F),
      left(90.0F),
      right(-90.0F),
      bottom(0.0F);

      public float rot;

      private TSTriangleDrawType(float argRot) {
         this.rot = argRot;
      }
   }
}
