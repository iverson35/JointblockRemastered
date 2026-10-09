package jp.mc.ancientred.jointblock.tileentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.assemble.BatchRenderHelper;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.model.BasicJointRenderer;
import jp.mc.ancientred.jointblock.model.render.CarvingRenderer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.slf4j.Logger;

/**
 * Renders a joint block: its carved shape, its IJB add-on model, the block model it carries, or the
 * plain joint model, plus the debug frame / attracter field overlays.
 *
 * <p>1.12.2 was a {@code TileEntitySpecialRenderer.func_192841_a(TileEntity, double x, double y,
 * double z, float partial, int destroyStage, float alpha)} and owned its own GL state machine. The
 * 1.20.1 port keeps the whole state-selection logic and call order but:
 * <ul>
 *   <li>The {@code x}/{@code y}/{@code z} arguments (and every {@code translate(x, y, z)} built from
 *       them) are gone: a 1.20.1 {@code BlockEntityRenderer} already receives a {@link PoseStack}
 *       positioned at the block (camera-relative), so translating by {@code blockPos - camera}
 *       again would double the offset. The remaining {@code GlStateManager.translate} sequences are
 *       reproduced verbatim - they are all right-multiplications, so their order is preserved.</li>
 *   <li>{@code MinecraftForgeClient.getRenderPass()} does not exist. Everything renders in one pass,
 *       so the 1.12.2 {@code renderPass == 0}/{@code == 1} split, the {@code enableBlend}/
 *       {@code blendFunc} toggles, {@code enableRescaleNormal}/{@code disableRescaleNormal} and the
 *       {@code RenderHelper.enableStandardItemLighting}/{@code disableStandardItemLighting} pairs
 *       are dropped - the {@link RenderType} carries blend/light/cull now. {@link #RENDER_PASS} is
 *       handed to the IJB add-on model as the single pass index.</li>
 *   <li>{@code renderNormalBlock}'s {@code getRenderLayer()} filter ({@code BlockRenderLayer
 *       .TRANSLUCENT} in pass 1, everything else in pass 0) is dropped for the same reason:
 *       {@code renderSingleBlock} picks the {@code RenderType} for the state itself, so the block is
 *       drawn exactly once instead of once per pass.</li>
 *   <li>Texture binds ({@code LoadedTextures.whiteResTexId}) are gone; the per-model
 *       {@code RenderType} selects the texture.</li>
 *   <li>The 1.12.2 {@code Tessellator.draw()} flush inside the failure handler is gone (there is no
 *       open batch to flush).</li>
 * </ul>
 *
 * <p>Two visual details cannot survive the port's frozen renderer API: the 1.12.2
 * {@code GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F)} that dimmed a not-connected basic model
 * (colour is per-vertex now and {@code BasicJointRenderer} has no multiplier) and the yellow/red
 * alpha-0.3 attracter beam colour, because {@code BasicJointRenderer.renderBeam} emits white.
 */
@OnlyIn(Dist.CLIENT)
public class TileEntityJointerRenderer implements BlockEntityRenderer<TileEntityJoint> {

   private static final Logger LOGGER = LogUtils.getLogger();

   /** Single 1.20.1 render pass; replaces {@code MinecraftForgeClient.getRenderPass()}. */
   private static final int RENDER_PASS = 0;

   public static final BlockEntityRendererProvider<TileEntityJoint> PROVIDER = TileEntityJointerRenderer::new;

   public TileEntityJointerRenderer(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileEntityJoint te, float partial, PoseStack pose, MultiBufferSource bufferSource,
                      int packedLight, int packedOverlay) {
      IJBModelItem modelItem = te.getModelItemFromStack();
      BlockState blockState = te.getBlockStateFromStack();
      int jointType = te.getJointType();
      if (JBConfig.doShowJBConnection && te.jointType != 0) {
         renderAbstractConnection(te, partial, pose, bufferSource, packedLight, packedOverlay);
      } else {
         pose.pushPose();
         if (!te.failedOnRender) {
            try {
               if (te.carvingData != null) {
                  int paletteIndex = JBCarvingDataUtil.getPaletteIndex(te.carvingData);
                  int[] paletteData = null;
                  if (te.boFixedPaletteDataList != null) {
                     paletteData = te.boFixedPaletteDataList[paletteIndex];
                  }

                  if (paletteData == null) {
                     paletteData = TileEntityJointCore.BOFIXED_DEFAULT_PALETTE_DATA[paletteIndex];
                  }

                  pose.translate(0.5F, 0.5F, 0.5F);
                  int facing = te.getJointFacing();
                  BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing, false);
                  pose.translate(-0.5F, -0.5F, -0.5F);
                  if (JBConfig.disableCarvingGLCompile) {
                     CarvingRenderer.renderTeCarvedModelByType(te, paletteData, pose, bufferSource, packedLight);
                  } else {
                     BatchRenderHelper.renderer.compileOrRenderCarving(te, paletteData, pose, bufferSource, packedLight);
                  }
               } else if (te.isValidRenderModel && modelItem != null) {
                  int modelMeta = te.getModelItemDamage();
                  IJBModelItem.IJBModel ijbModel = modelItem.getIJBModel(modelMeta);
                  if (ijbModel.shouldRenderInPass(modelMeta, RENDER_PASS)) {
                     pose.translate(0.0F, 1.0F, 1.0F);
                     pose.scale(1.0F, -1.0F, -1.0F);
                     pose.translate(0.5F, 0.5F, 0.5F);
                     if (JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
                        BasicJointRenderer.addAdditonalSideRotatoin(pose, te.getRenderSide());
                     }

                     int facing = te.getJointFacing();
                     BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing);
                     ijbModel.setRenderState(pose, te, facing, 1.0F, (LivingEntity)null, RENDER_PASS);
                     pose.translate(-0.5F, -0.5F, -0.5F);
                     pose.translate(0.5F, -0.5F, 0.5F);
                     BasicJointRenderer.renderIJBModel(ijbModel, pose, bufferSource, packedLight, packedOverlay, facing, RENDER_PASS);
                  }
               } else if (te.isValidRenderModel && blockState != null) {
                  Block block = blockState.getBlock();
                  pose.translate(0.5F, 0.5F, 0.5F);
                  if (JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
                     BasicJointRenderer.addAdditonalSideRotatoin(pose, te.getRenderSide(), true);
                  }

                  int facing = te.getJointFacing();
                  BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing, false);
                  pose.translate(-0.5F, -0.5F, -0.5F);
                  boolean renderedTe = false;
                  if (block instanceof EntityBlock) {
                     if (!te.checkdTileEntity) {
                        te.checkdTileEntity = true;
                        BlockEntity teToRender = ((EntityBlock)block).newBlockEntity(te.getBlockPos(), blockState);
                        if (teToRender != null && Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(teToRender) != null) {
                           te.tileEntityToRender = teToRender;
                        }
                     }

                     if (te.tileEntityToRender != null) {
                        Minecraft.getInstance().getBlockEntityRenderDispatcher().render(te.tileEntityToRender, partial, pose, bufferSource);
                        renderedTe = true;
                     }
                  }

                  Level level;
                  if (!renderedTe && (level = te.getLevel()) != null) {
                     BasicJointRenderer.renderNormalBlock(blockState, pose, bufferSource, LevelRenderer.getLightColor(level, te.getBlockPos()), packedOverlay);
                  }
               } else {
                  pose.translate(0.0F, 1.0F, 1.0F);
                  pose.scale(1.0F, -1.0F, -1.0F);
                  pose.translate(0.5F, 0.5F, 0.5F);
                  if (JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
                     BasicJointRenderer.addAdditonalSideRotatoin(pose, te.getConnectSide());
                  }

                  int facing = te.getJointFacing();
                  BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing);
                  BasicJointRenderer.setAdditonalRenderState(te, partial);
                  pose.translate(-0.5F, -0.5F, -0.5F);
                  pose.translate(0.5F, -0.5F, 0.5F);
                  if (te.hasJBAtConnectSide && jointType != 10 && jointType != 0 && jointType != 2) {
                     BasicJointRenderer.renderBasicModel(pose, bufferSource, packedLight, packedOverlay, jointType, true);
                  } else {
                     BasicJointRenderer.renderBasicModel(pose, bufferSource, packedLight, packedOverlay, jointType, false);
                  }
               }
            } catch (Exception e) {
               LOGGER.debug("[JointBlock]Failed at TileEntity render:", e);
               te.failedOnRender = true;
            }
         } else {
            pose.translate(0.0F, 1.0F, 1.0F);
            pose.scale(1.0F, -1.0F, -1.0F);
            pose.translate(0.5F, -0.5F, 0.5F);
            BasicJointRenderer.renderErrorModel(pose, bufferSource, packedLight, packedOverlay);
         }

         pose.popPose();
         if (JBConfig.enableJointBlockParticle) {
            if (jointType == 15 && !te.isExtraFlagsTypeSpecCommonA()) {
               renderAttracterFields(te, pose, bufferSource, packedLight, packedOverlay);
            }

            if (JBConfig.showJBFrame > 0) {
               renderJBFrame(te, pose, bufferSource);
            }
         }
      }
   }

   /**
    * 1.12.2 {@code renderAbstractConnection}: the master/slave connection bars, drawn instead of the
    * model whenever {@code JBConfig.doShowJBConnection} is on for a non-core joint.
    */
   private void renderAbstractConnection(TileEntityJoint te, float partial, PoseStack pose, MultiBufferSource bufferSource,
                                         int packedLight, int packedOverlay) {
      pose.pushPose();

      try {
         pose.translate(0.0F, 1.0F, 1.0F);
         pose.scale(1.0F, -1.0F, -1.0F);
         pose.translate(0.5F, 0.5F, 0.5F);
         if (JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
            BasicJointRenderer.addAdditonalSideRotatoin(pose, te.getConnectSide());
         }

         int facing = te.getJointFacing();
         BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing);
         pose.translate(-0.5F, -0.5F, -0.5F);
         pose.translate(0.5F, -0.5F, 0.5F);
         BasicJointRenderer.renderAbstractConnection(pose, bufferSource, !te.isConnectedToCore);
      } catch (Exception e) {
         // 1.12.2 swallowed this deliberately: a broken connection bar must not kill the block.
      }

      pose.popPose();
   }

   /**
    * 1.12.2 frame overlay ({@code JBConfig.showJBFrame}): the joint radius rings and, for a core, the
    * facing line.
    *
    * <p>The 1.12.2 code emitted camera-relative absolute coordinates after the outer
    * {@code popMatrix()}, when the matrix sat at the camera origin. In 1.20.1 the {@link PoseStack}
    * handed to the renderer is instead positioned at the block, so this pushes a compensating
    * translate of {@code -(blockPos - camera)} first and keeps the reference coordinate math
    * verbatim. The camera position comes from {@code Camera.getPosition()} (the same value the
    * level pose is built from) rather than 1.12.2's interpolated camera-entity position.
    */
   private void renderJBFrame(TileEntityJoint te, PoseStack pose, MultiBufferSource bufferSource) {
      Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      BlockPos tePos = te.getBlockPos();
      VertexConsumer bb = bufferSource.getBuffer(RenderType.lines());
      pose.pushPose();
      pose.translate(-(float)((double)tePos.getX() - camPos.x), -(float)((double)tePos.getY() - camPos.y), -(float)((double)tePos.getZ() - camPos.z));
      Matrix4f matrix = pose.last().pose();
      int circleDiv = 8;
      float scale = 0.85F;
      scale += Mth.cos((float)Math.toRadians((double)te.electricAnimation * 6.0)) * 0.05F;
      float red;
      float green;
      float blue;
      if (te.getJointType() == 0) {
         green = 1.0F;
         blue = 0.0F;
         red = 0.0F;
         int lineDir = 0;
         int meta = te.getJointFacing();
         if (meta == 3) {
            lineDir = 1;
         }

         if (meta == 4) {
            lineDir = 2;
         }

         if (meta == 2) {
            lineDir = 3;
         }

         if (meta == 5) {
            lineDir = 0;
         }

         renderLineXZ(bb, matrix, te, lineDir, scale, camPos.x, camPos.y, camPos.z, red, green, blue);
         red = 1.0F;
         green = 0.2F;
         blue = 0.2F;
         circleDiv = 6;
      } else {
         if (JBCommonUtil.jointTypeHasMotion(te.getJointType())) {
            circleDiv = 12;
         } else {
            circleDiv = 4;
         }

         if (te.isConnectedToCore) {
            red = 0.2F;
            green = 0.2F;
            blue = 1.0F;
         } else {
            red = 0.4F;
            green = 0.4F;
            blue = 0.4F;
         }
      }

      for(int lineDir = 0; lineDir < circleDiv; ++lineDir) {
         renderCycleXZ(bb, matrix, te, lineDir, circleDiv, scale, camPos.x, camPos.y, camPos.z, red, green, blue);
         renderCycleXY(bb, matrix, te, lineDir, circleDiv, scale, camPos.x, camPos.y, camPos.z, red, green, blue);
         renderCycleYZ(bb, matrix, te, lineDir, circleDiv, scale, camPos.x, camPos.y, camPos.z, red, green, blue);
      }

      pose.popPose();
   }

   /** 1.12.2 {@code renderLineXZ}: the single facing line of a core joint. */
   private void renderLineXZ(VertexConsumer bb, Matrix4f matrix, TileEntityJoint te, int num, float scale,
                             double interpPosX, double interpPosY, double interpPosZ, float red, float green, float blue) {
      BlockPos pos = te.getBlockPos();
      int tePosX = pos.getX();
      int tePosY = pos.getY();
      int tePosZ = pos.getZ();
      float fY = (float)((double)tePosY + 0.5 - interpPosY);
      float angle = (float)(num * 360 / 4);
      float cfX = (float)((double)tePosX + 0.5 - interpPosX);
      float cfZ = (float)((double)tePosZ + 0.5 - interpPosZ);
      float afX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale * 2.0 + (double)tePosX + 0.5 - interpPosX);
      float afZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale * 2.0 + (double)tePosZ + 0.5 - interpPosZ);
      line(bb, matrix, cfX, fY, cfZ, afX, fY, afZ, red, green, blue);
   }

   /** 1.12.2 {@code renderCycleXZ}: the XZ ring, plus the half-scale spoke on every other segment. */
   private void renderCycleXZ(VertexConsumer bb, Matrix4f matrix, TileEntityJoint te, int num, int circleDiv, float scale,
                              double interpPosX, double interpPosY, double interpPosZ, float red, float green, float blue) {
      BlockPos pos = te.getBlockPos();
      int tePosX = pos.getX();
      int tePosY = pos.getY();
      int tePosZ = pos.getZ();
      float fY = (float)((double)tePosY + 0.5 - interpPosY);
      float angle = (float)(num * 360 / circleDiv);
      float afX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
      float afZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
      ++num;
      if (num == circleDiv) {
         num = 0;
      }

      angle = (float)(num * 360 / circleDiv);
      float bfX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
      float bfZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
      line(bb, matrix, afX, fY, afZ, bfX, fY, bfZ, red, green, blue);
      if (num % 2 != 0) {
         scale *= 0.7F;
         afX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
         afZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
         line(bb, matrix, bfX, fY, bfZ, afX, fY, afZ, red, green, blue);
      }

   }

   /** 1.12.2 {@code renderCycleXY}. */
   private void renderCycleXY(VertexConsumer bb, Matrix4f matrix, TileEntityJoint te, int num, int circleDiv, float scale,
                              double interpPosX, double interpPosY, double interpPosZ, float red, float green, float blue) {
      BlockPos pos = te.getBlockPos();
      int tePosX = pos.getX();
      int tePosY = pos.getY();
      int tePosZ = pos.getZ();
      float fZ = (float)((double)tePosZ + 0.5 - interpPosZ);
      float angle = (float)(num * 360 / circleDiv);
      float afX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
      float afY = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
      ++num;
      if (num == circleDiv) {
         num = 0;
      }

      angle = (float)(num * 360 / circleDiv);
      float bfX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
      float bfY = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
      line(bb, matrix, afX, afY, fZ, bfX, bfY, fZ, red, green, blue);
      if (num % 2 != 0) {
         scale *= 0.7F;
         afX = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosX + 0.5 - interpPosX);
         afY = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
         line(bb, matrix, bfX, bfY, fZ, afX, afY, fZ, red, green, blue);
      }

   }

   /** 1.12.2 {@code renderCycleYZ}. */
   private void renderCycleYZ(VertexConsumer bb, Matrix4f matrix, TileEntityJoint te, int num, int circleDiv, float scale,
                              double interpPosX, double interpPosY, double interpPosZ, float red, float green, float blue) {
      BlockPos pos = te.getBlockPos();
      int tePosX = pos.getX();
      int tePosY = pos.getY();
      int tePosZ = pos.getZ();
      float fX = (float)((double)tePosX + 0.5 - interpPosX);
      float angle = (float)(num * 360 / circleDiv);
      float afY = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
      float afZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
      ++num;
      if (num == circleDiv) {
         num = 0;
      }

      angle = (float)(num * 360 / circleDiv);
      float bfY = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
      float bfZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
      line(bb, matrix, fX, afY, afZ, fX, bfY, bfZ, red, green, blue);
      if (num % 2 != 0) {
         scale *= 0.7F;
         afY = (float)(Math.cos(Math.toRadians((double)angle)) * (double)scale + (double)tePosY + 0.5 - interpPosY);
         afZ = (float)(Math.sin(Math.toRadians((double)angle)) * (double)scale + (double)tePosZ + 0.5 - interpPosZ);
         line(bb, matrix, fX, bfY, bfZ, fX, afY, afZ, red, green, blue);
      }

   }

   /**
    * One reference-coloured segment of the frame overlay.
    *
    * <p>1.12.2 drew these with {@code GL_LINES} and the ambient {@code color(r, g, b, 0.5)}; here
    * {@link RenderType#lines()} carries the state and the colour goes per vertex. The normal picks
    * the screen-space line axis the way vanilla's {@code renderLineBox} does.
    */
   private static void line(VertexConsumer bb, Matrix4f matrix, float x0, float y0, float z0, float x1, float y1, float z1,
                            float red, float green, float blue) {
      float nx = y0 == y1 && z0 == z1 ? 1.0F : 0.0F;
      float ny = nx == 0.0F && x0 == x1 && z0 == z1 ? 1.0F : 0.0F;
      float nz = nx == 0.0F && ny == 0.0F ? 1.0F : 0.0F;
      bb.vertex(matrix, x0, y0, z0).color(red, green, blue, 0.5F).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, x1, y1, z1).color(red, green, blue, 0.5F).normal(nx, ny, nz).endVertex();
   }

   /**
    * 1.12.2 {@code renderAttracterFields}: the attracter's slave beam, statically called by the
    * joint-block renderer and by the entity renderer while an attracter is guiding a slave.
    *
    * <p>The 1.12.2 {@code color(1.0F, 0.2F/1.0F, ..., 0.3F)} that reddened a temporarily disabled
    * attracter is lost: {@code BasicJointRenderer.renderBeam} emits white, and its signature is
    * frozen for this port.
    */
   public static void renderAttracterFields(TileEntityJoint te, PoseStack pose, MultiBufferSource bufferSource,
                                            int packedLight, int packedOverlay) {
      if (te instanceof TileEntityJointAttracter) {
         TileEntityJointAttracter teAttracter = (TileEntityJointAttracter)te;
         pose.pushPose();
         pose.translate(0.0F, 1.0F, 1.0F);
         pose.scale(1.0F, -1.0F, -1.0F);
         pose.translate(0.5F, 0.5F, 0.5F);
         if (JBCommonUtil.doSideRotateOnRender(te.getJointType())) {
            BasicJointRenderer.addAdditonalSideRotatoin(pose, te.getConnectSide());
         }

         int facing = te.getJointFacing();
         BasicJointRenderer.addAdditonalFacingRotatoin(pose, facing);
         pose.translate(-0.5F, -0.5F, -0.5F);
         pose.translate(0.5F, -0.5F, 0.5F);
         BasicJointRenderer.renderBeam(pose, bufferSource, (double)teAttracter.getSlaveOffset() / 16.0);
         pose.popPose();
      }
   }
}
