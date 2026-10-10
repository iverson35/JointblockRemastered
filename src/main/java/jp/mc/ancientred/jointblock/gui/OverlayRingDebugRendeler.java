package jp.mc.ancientred.jointblock.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.text.DecimalFormat;
import java.util.ArrayList;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.assemble.ai.JBPathEntity;
import jp.mc.ancientred.jointblock.assemble.ai.JBPathPoint;
import jp.mc.ancientred.jointblock.assemble.ring.AbstractRealTimeStateNode;
import jp.mc.ancientred.jointblock.assemble.ring.AbstractRingNode;
import jp.mc.ancientred.jointblock.assemble.ring.RingNodeSTLink;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgram;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgramPage;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * 1.12.2 OverlayRingDebugRendeler ported to Forge 1.20.1.
 *
 * <p>The ring debug window traversal/layout bookkeeping (page grid, node ring positions, activation
 * decay, {@link #ringNodeWork}/{@link #ringPageWork} population), the node value readout (including
 * {@link RingNodeSTLink} resolution) and the path debug traversal are ported verbatim. Drawing
 * substitutions:
 * <ul>
 *   <li>The ring/debug window entry is {@link #renderRingDebugWindow(GuiGraphics, Minecraft, int, int)}
 *       (the 1.20.1 {@code IGuiOverlay} contract): the window size that 1.12 read from the
 *       {@code RenderGameOverlayEvent} resolution now comes from the overlay's screen width/height.
 *       The window sprites/font are drawn through {@link GuiGraphics}; the ring body and the nodes
 *       are delegated to {@link GuiRingProgram#drawFilledRing} / {@link GuiRingProgram#drawNode}
 *       (the 1.20.1 signatures take the {@link GuiGraphics} first).</li>
 *   <li>The 1.12 named-texture binds ({@code mc.getTextureManager().bindTexture(...)}) and the
 *       {@code Gui.field_110324_m} restore are dropped: each {@link GuiGraphics} draw binds its own
 *       render type/texture, so there is no global texture bind to set or restore.</li>
 *   <li>The 1.12 global colour ({@code GlStateManager.color}) is replaced by the per-vertex/per-draw
 *       colours of {@link GuiGraphics} and the delegated {@link GuiRingProgram} helpers.</li>
 *   <li>The error readout keeps the 1.12 order (string first, then the translucent backdrop rect over
 *       it): {@link GuiGraphics#drawString} + {@link GuiGraphics#fill}.</li>
 *   <li>{@link #renderPathDebug} draws the navigator path boxes in world space. 1.12 used
 *       {@code RenderWorldLastEvent} + immediate mode; 1.20.1 has no {@code RenderWorldLastEvent}, so
 *       the entry is {@link #renderPathDebug(float, PoseStack, MultiBufferSource)} and the boxes are
 *       emitted as {@link RenderType#debugLineStrip(double)} batches over the supplied world-pass
 *       {@link PoseStack} (camera-relative coordinates, as 1.12 did). The 1.12
 *       {@code enableBlend}/{@code blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)}/colour
 *       {@code (0,0,0,0.4)}/{@code glLineWidth(2)}/{@code depthMask(false)} state is carried by the
 *       vertex colours and the line-strip width; {@code NO_TRANSPARENCY} on {@code debugLineStrip}
 *       means the 0.4 alpha is not blended (the closest available POSITION_COLOR line render type) -
 *       recorded as a substitution. The caller is the {@code RenderLevelStageEvent} handler
 *       {@code ClientEventhandler#onRenderLevelStage}.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class OverlayRingDebugRendeler {
   public static ArrayList<AbstractRingNode> ringNodeWork = new ArrayList<AbstractRingNode>();
   public static ArrayList<RingProgramPage> ringPageWork = new ArrayList<RingProgramPage>();
   private static DecimalFormat dFormat = new DecimalFormat("#.###");
   /** Stands in for the 1.12 constant {@code Block.field_185505_j} (unit block box, 1.20.1 has no such constant). */
   private static final AABB FULL_BLOCK_AABB = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

   public static void renderRingDebugWindow(GuiGraphics guiGraphics, Minecraft mc, int screenWidth, int screenHeight) {
      if (JBConfig.enableDebugRing) {
         EntityJointed ej = getRingDebugEntityJointedFromServer(mc);
         if (ej != null && ej.ringPrograms != null) {
            if (JBConfig.debugRingTargetNum >= 0 && JBConfig.debugRingTargetNum < ej.ringPrograms.length) {
               RingProgram program = ej.ringPrograms[JBConfig.debugRingTargetNum];
               if (program != null && program.getEntityJointed() != null) {
                  int rowCnt = 1;
                  int colCnt = 2;

                  int pageCnt;
                  for(pageCnt = program.pageList.size(); rowCnt * colCnt < pageCnt; ++colCnt) {
                     ++rowCnt;
                  }

                  int mergin = 30;
                  int cellMergin = 5;
                  int left = mergin;
                  int top = mergin;
                  int screenWidthM = screenWidth - mergin * 2;
                  int screenHeightM = screenHeight - mergin * 2;
                  int rowWidth = screenHeightM / rowCnt;
                  int colWidth = screenWidthM / colCnt;
                  int ringRadius;
                  if (rowWidth < colWidth) {
                     ringRadius = (rowWidth - cellMergin * 2) / 2;
                     ringRadius = (int)((double)ringRadius * 0.8);
                  } else {
                     ringRadius = (colWidth - cellMergin * 2) / 2;
                     ringRadius = (int)((double)ringRadius * 0.8);
                  }

                  float activeExpand = (float)ringRadius / 7.0F;
                  int showIconSize = (int)(24.0F * (float)ringRadius / 80.0F);
                  int ringWidth = (int)(15.0F * (float)ringRadius / 80.0F);
                  ringNodeWork.clear();
                  ringPageWork.clear();

                  for(int row = 0; row < rowCnt; ++row) {
                     for(int col = 0; col < colCnt; ++col) {
                        int pageIdx = col + row * colCnt;
                        if (pageIdx < pageCnt) {
                           float ringCenterX = (float)(left + col * colWidth + colWidth / 2);
                           float ringCenterY = (float)(top + row * rowWidth + rowWidth / 2);
                           GuiRingProgram.drawFilledRing(guiGraphics, ringCenterX, ringCenterY, (float)ringRadius, 20, (double)ringWidth, 5916487);
                           RingProgramPage page = program.pageList.get(pageIdx);
                           boolean isErrorPage = program.hadError && (program.lastPageIndex == page.pageIndex || program.lastLastPageIndex == page.pageIndex);
                           int nodeCnt = page.nodeList.size();

                           for(int v = 0; v < nodeCnt; ++v) {
                              AbstractRingNode node = page.nodeList.get(v);
                              boolean isActive = false;
                              float radAdd = 0.0F;
                              if (node.gui == null) {
                                 node.withGui();
                              }

                              if (node.gui.dbgActivated > 0) {
                                 isActive = true;
                                 radAdd = activeExpand * ((float)node.gui.dbgActivated / 10.0F);
                                 --node.gui.dbgActivated;
                              }

                              double angleRad = (double)((float)(1.5707963267948966 - 6.283185307179586 / (double)nodeCnt * (double)v));
                              float nodeCenterX = ringCenterX + (float)Math.cos(angleRad) * ((float)ringRadius + radAdd);
                              float nodeCenterY = ringCenterY - (float)Math.sin(angleRad) * ((float)ringRadius + radAdd);
                              boolean isErrorNode = isErrorPage && (program.lastNodeIndex == node.nodeIndex || program.lastLastNodeIndex == node.nodeIndex);
                              if (!isErrorNode) {
                                 GuiRingProgram.drawNode(guiGraphics, node, nodeCenterX, nodeCenterY, showIconSize, true, isActive, 8355635, 127, false);
                              } else {
                                 GuiRingProgram.drawNode(guiGraphics, node, nodeCenterX, nodeCenterY, showIconSize, true, true, 13120307, 255, false);
                              }

                              nodeCenterX = ringCenterX + (float)Math.cos(angleRad) * (float)ringRadius * 0.8F;
                              nodeCenterY = ringCenterY - (float)Math.sin(angleRad) * (float)ringRadius * 0.8F;
                              node.setGuiDrawPos(nodeCenterX, nodeCenterY);
                              ringNodeWork.add(node);
                              ringPageWork.add(page);
                           }
                        }
                     }
                  }

                  for(int i = 0; i < ringNodeWork.size(); ++i) {
                     RingProgram pgm = program;
                     RingProgramPage page = ringPageWork.get(i);
                     AbstractRingNode node = ringNodeWork.get(i);
                     float nodeX = node.gui.drawX;
                     float nodeY = node.gui.drawY;
                     if (node instanceof RingNodeSTLink) {
                        RingNodeSTLink linkNode = (RingNodeSTLink)node;
                        node = linkNode.linkTargetNode;
                        page = linkNode.linkTargetPage;
                        pgm = linkNode.linkTargetProgram;
                        if (node == null || page == null || pgm == null || node instanceof RingNodeSTLink) {
                           drawVar(guiGraphics, mc, nodeX, nodeY, Double.NaN, 0);
                           continue;
                        }
                     }

                     if (node.hasVectorOutput(pgm, page)) {
                        for(int v = 0; v < 4; ++v) {
                           double viewValue;
                           if (node instanceof AbstractRealTimeStateNode) {
                              double[] vars = ((AbstractRealTimeStateNode)node).arrayValues;
                              if (vars != null) {
                                 viewValue = vars[v];
                              } else {
                                 viewValue = ((AbstractRealTimeStateNode)node).returnValue;
                              }
                           } else {
                              viewValue = node.getArrayValue(pgm, page, v);
                           }

                           drawVar(guiGraphics, mc, nodeX, nodeY, viewValue, v - 2);
                        }
                     } else {
                        double viewValue;
                        if (node instanceof AbstractRealTimeStateNode) {
                           viewValue = ((AbstractRealTimeStateNode)node).returnValue;
                        } else {
                           viewValue = node.getReturnValue(pgm, page);
                        }

                        drawVar(guiGraphics, mc, nodeX, nodeY, viewValue, 0);
                     }
                  }

                  if (program.hadError && program.hadErrorType != null) {
                     int showErrorCX = screenWidth / 2;
                     int showErrorCY = screenHeight - 40;
                     if (program.errorTypeStr == null) {
                        program.errorTypeStr = ChatFormatting.BOLD + "Error:" + CompatHelper.formatLang(program.hadErrorType.getErrorMessageKey());
                     }

                     int strW = mc.font.width(program.errorTypeStr);
                     guiGraphics.drawString(mc.font, program.errorTypeStr, showErrorCX - strW / 2, showErrorCY - mc.font.lineHeight / 2, 12255232);
                     int backW = 100;
                     int backH = 15;
                     guiGraphics.fill(showErrorCX - backW, showErrorCY - backH, showErrorCX + backW, showErrorCY + backH, 1690748636);
                  }

                  ringNodeWork.clear();
                  ringPageWork.clear();
               }
            }
         }
      }
   }

   private static EntityJointed getRingDebugEntityJointedFromServer(Minecraft mc) {
      if (mc.player == null) {
         return null;
      } else {
         Entity ridingEntity = mc.player.getVehicle();
         if (ridingEntity instanceof EntitySeatPart) {
            EntitySeatPart seatPart = (EntitySeatPart)ridingEntity;
            if (seatPart.entityJointed == null) {
               return null;
            }

            MinecraftServer sv = JBModContainer.proxy.getIntegratedServer();
            if (sv == null) {
               return null;
            }

            ServerLevel serverLevel = sv.overworld();
            if (serverLevel == null) {
               return null;
            }

            Entity entity = serverLevel.getEntity(seatPart.entityJointed.getId());
            if (entity instanceof EntityJointed) {
               return (EntityJointed)entity;
            }
         } else {
            for(InteractionHand hand : InteractionHand.values()) {
               ItemStack curItemStack = mc.player.getItemInHand(hand);
               if (ItemJBController.isItemStackJBController(curItemStack)) {
                  MinecraftServer sv = JBModContainer.proxy.getIntegratedServer();
                  if (sv != null && sv.overworld() != null) {
                     EntityJointed ejClient = ItemJBController.getRelatedEntityJointedServer(curItemStack, sv.overworld(), mc.player);
                     return ejClient;
                  }

                  return null;
               }
            }
         }

         return null;
      }
   }

   protected static void drawVar(GuiGraphics guiGraphics, Minecraft mc, float nodeX, float nodeY, double viewValue, int row) {
      String str;
      if (viewValue != viewValue) {
         str = "err";
      } else {
         str = dFormat.format(viewValue);
      }

      int strW = mc.font.width(str);
      int rowPad = mc.font.lineHeight * row;
      int strL = (int)(nodeX - (float)strW / 2.0F);
      int strT = (int)(nodeY - (float)mc.font.lineHeight / 2.0F);
      guiGraphics.drawString(mc.font, str, strL, strT + rowPad, 16777215);
   }

   /**
    * 1.12 {@code renderPathDebug(float)}: the navigator path boxes. 1.20.1 removed
    * {@code RenderWorldLastEvent}, so the caller ({@code ClientEventhandler#onRenderLevelStage},
    * the counterpart of the 1.12 {@code handleRenderLast}) supplies the world-pass
    * {@link PoseStack} and the geometry {@link MultiBufferSource}; positions are camera-relative as
    * in 1.12 and each box is flushed through {@link #drawOutlinedBoundingBox}.
    */
   public static void renderPathDebug(float partialTick, PoseStack pose, MultiBufferSource bufferSource) {
      if (JBConfig.enableDebugRing) {
         Minecraft mc = Minecraft.getInstance();
         EntityJointed ej = getRingDebugEntityJointedFromServer(mc);
         if (ej != null) {
            JBPathEntity path = ej.getJBNavigator().currentPath;
            Entity rvEntity = Minecraft.getInstance().getCameraEntity();
            Level theWorld = rvEntity.level();
            if (path != null && rvEntity != null && theWorld != null) {
               float f1 = 0.002F;

               for(int i = 0; i <= path.getCurrentPathLength(); ++i) {
                  JBPathPoint point;
                  double d0;
                  double d1;
                  double d2;
                  if (i == path.getCurrentPathLength()) {
                     try {
                        point = path.getPathPointFromIndex(path.getCurrentPathIndex());
                        d0 = rvEntity.xo + (rvEntity.getX() - rvEntity.xo) * (double)partialTick;
                        d1 = rvEntity.yo + (rvEntity.getY() - rvEntity.yo) * (double)partialTick;
                        d2 = rvEntity.zo + (rvEntity.getZ() - rvEntity.zo) * (double)partialTick;
                        AABB debugBox = FULL_BLOCK_AABB.move((double)point.posX, (double)point.posY, (double)point.posZ).deflate(0.3).move(-d0, -d1, -d2);
                        drawOutlinedBoundingBox(pose, bufferSource, debugBox, 0.0F, 0.0F, 0.0F, 0.4F);
                     } catch (Exception ex) {
                     }
                  } else {
                     point = path.getPathPointFromIndex(i);
                     d0 = rvEntity.xo + (rvEntity.getX() - rvEntity.xo) * (double)partialTick;
                     d1 = rvEntity.yo + (rvEntity.getY() - rvEntity.yo) * (double)partialTick;
                     d2 = rvEntity.zo + (rvEntity.getZ() - rvEntity.zo) * (double)partialTick;
                     AABB debugBox = FULL_BLOCK_AABB.move((double)point.posX, (double)point.posY, (double)point.posZ).inflate((double)f1, (double)f1, (double)f1).move(-d0, -d1, -d2);
                     drawOutlinedBoundingBox(pose, bufferSource, debugBox, 0.0F, 0.0F, 0.0F, 0.4F);
                  }
               }
            }
         }
      }
   }

   /**
    * 1.12 {@code drawOutlinedBoundingBox(AxisAlignedBB)}: two horizontal {@code GL_LINE_STRIP} loops
    * plus four vertical {@code GL_LINES} edges. Reproduced with
    * {@link RenderType#debugLineStrip(double)} (width 2.0, the 1.12 {@code glLineWidth}) batches; the
    * four disjoint vertical edges are four separate two-vertex strips because a line strip cannot be
    * restarted mid-batch. Colour {@code (r,g,b,a)} was the 1.12 global colour state.
    */
   public static void drawOutlinedBoundingBox(PoseStack pose, MultiBufferSource bufferSource, AABB box, float r, float g, float b, float a) {
      double lineWidth = 2.0;
      Matrix4f mat = pose.last().pose();
      VertexConsumer vc = bufferSource.getBuffer(RenderType.debugLineStrip(lineWidth));
      lineVertex(vc, mat, box.minX, box.minY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.minY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.minY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.minY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.minY, box.minZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
      lineVertex(vc, mat, box.minX, box.maxY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.maxY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.maxY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.maxY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.maxY, box.minZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
      lineVertex(vc, mat, box.minX, box.minY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.maxY, box.minZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
      lineVertex(vc, mat, box.maxX, box.minY, box.minZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.maxY, box.minZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
      lineVertex(vc, mat, box.maxX, box.minY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.maxX, box.maxY, box.maxZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
      lineVertex(vc, mat, box.minX, box.minY, box.maxZ, r, g, b, a);
      lineVertex(vc, mat, box.minX, box.maxY, box.maxZ, r, g, b, a);
      flushBuffer(bufferSource, lineWidth);
   }

   private static void lineVertex(VertexConsumer vc, Matrix4f mat, double x, double y, double z, float r, float g, float b, float a) {
      vc.vertex(mat, (float)x, (float)y, (float)z).color(r, g, b, a).endVertex();
   }

   private static void flushBuffer(MultiBufferSource bufferSource, double lineWidth) {
      if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
         bs.endBatch(RenderType.debugLineStrip(lineWidth));
      }
   }
}
