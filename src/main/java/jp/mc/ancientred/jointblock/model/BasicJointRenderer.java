package jp.mc.ancientred.jointblock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import jp.mc.ancientred.jointblock.model.render.LoadedTextures;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Central joint model renderer, ported from the 1.12.2 fixed-function/display-list version.
 *
 * <p>Only the geometry selection and the model-state selection survived verbatim; the draw calls
 * were rewritten around {@link MultiBufferSource}/{@link VertexConsumer}:
 * <ul>
 *   <li>Texture binding ({@code GlStateManager.bindTexture(texId)}) became one
 *       {@link net.minecraft.client.renderer.RenderType} per model, see
 *       {@link #textureFor(int)}/{@link JBRenderTypes#jointModel(ResourceLocation)}.</li>
 *   <li>{@code renderNormalBlock} no longer needs {@code DummyIBlockAccess} plus a hand-rolled
 *       normal VBO: {@code BlockRenderDispatcher.renderSingleBlock} emits correct per-face normals
 *       itself, which is what the 1.12 {@code calculateNormalQuads}/{@code JBVertexBufferUploader}
 *       pair existed to emulate for the shader-mod path.</li>
 *   <li>{@code renderBeam}/{@code renderAbstractConnection} are untextured
 *       {@link JBRenderTypes#solidColor()} quads instead of a white-texture + disabled-lighting
 *       draw. The 1.12 {@code GL_TRIANGLES} cap is emitted as a degenerate quad so that a single
 *       QUADS buffer can carry it.</li>
 * </ul>
 *
 * <p>{@code renderSeatAs} and {@code renderCoreAsSlave} stay as ambient state because the 1.12
 * call order is {@code setAdditonalRenderState(...)} immediately followed by
 * {@code renderBasicModel(...)}; {@link #textureFor(int)} reads it.
 */
@OnlyIn(Dist.CLIENT)
public class BasicJointRenderer {

   public static final int SEAT_AS_SEAT = 0;
   public static final int SEAT_AS_INTERFACE = 1;
   public static final int SEAT_AS_CAMERA_TE = 2;
   public static final int SEAT_AS_CAMERA_JT = 9;

   public static boolean renderCoreAsSlave = false;
   private static int renderSeatAs = SEAT_AS_SEAT;

   private static final ModelJointCore modelCore = new ModelJointCore();
   private static final ModelJointSeat modelSeat = new ModelJointSeat();
   private static final ModelJointScaler modelScaler = new ModelJointScaler();
   private static final ModelJointSlider modelSlider = new ModelJointSlider();
   private static final ModelJointExtender modelExtender = new ModelJointExtender();
   private static final ModelJointAttracter modelAttracter = new ModelJointAttracter();
   private static final ModelInterface modelInterface = new ModelInterface();
   private static final ModelCamera modelCamera = new ModelCamera();
   private static final ModelRenderError modelErr = new ModelRenderError(0);
   private static final ModelRenderError modelPenalty = new ModelRenderError(32);
   private static final ModelSimpleJB modelSimpleJBU = new ModelSimpleJB(0, 0);
   private static final ModelSimpleJB modelSimpleJBD = new ModelSimpleJB(0, 32);

   /** The texture {@link #renderBasicModel} will pick for {@code jointType} in the current state. */
   public static ResourceLocation textureFor(int jointType) {
      switch (jointType) {
         case 0:
            return renderCoreAsSlave ? LoadedTextures.coreSlaveRes : LoadedTextures.coreNormalRes;
         case 2:
            if (renderSeatAs == SEAT_AS_INTERFACE) {
               return LoadedTextures.attracter_interfaceRes;
            }
            if (renderSeatAs == SEAT_AS_CAMERA_TE) {
               return LoadedTextures.cameraRes;
            }
            return LoadedTextures.seatRes;
         case 8:
            return LoadedTextures.simpleJBRes;
         case 10:
            return LoadedTextures.extenderRes;
         case 12:
            return LoadedTextures.scalerRes;
         case 14:
            return LoadedTextures.sliderRes;
         case 15:
            return LoadedTextures.attracter_interfaceRes;
         default:
            return LoadedTextures.simpleJBRes;
      }
   }

   public static void renderBasicModel(PoseStack pose, MultiBufferSource bufferSource, int packedLight,
                                       int packedOverlay, int jointType, boolean renderConnection) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(textureFor(jointType)));
      switch (jointType) {
         case 0:
            modelCore.renderAll(pose, consumer, packedLight, packedOverlay);
            break;
         case 2:
            if (renderSeatAs == SEAT_AS_SEAT) {
               modelSeat.renderAll(pose, consumer, packedLight, packedOverlay);
            } else if (renderSeatAs == SEAT_AS_INTERFACE) {
               modelInterface.renderAll(pose, consumer, packedLight, packedOverlay);
            } else if (renderSeatAs == SEAT_AS_CAMERA_TE) {
               modelCamera.renderAll(pose, consumer, packedLight, packedOverlay);
            }
            break;
         case 8:
            modelSimpleJBD.renderAll(pose, consumer, packedLight, packedOverlay, renderConnection);
            break;
         case 10:
            modelExtender.renderAll(pose, consumer, packedLight, packedOverlay);
            break;
         case 12:
            modelScaler.renderAll(pose, consumer, packedLight, packedOverlay);
            break;
         case 14:
            modelSlider.renderAll(pose, consumer, packedLight, packedOverlay);
            break;
         case 15:
            modelAttracter.renderAll(pose, consumer, packedLight, packedOverlay);
            break;
         default:
            modelSimpleJBU.renderAll(pose, consumer, packedLight, packedOverlay, renderConnection);
            break;
      }
   }

   public static void renderErrorModel(PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(LoadedTextures.errRes));
      modelErr.renderAll(pose, consumer, packedLight, packedOverlay);
   }

   public static void renderPenaltyModel(PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(LoadedTextures.errRes));
      modelPenalty.renderAll(pose, consumer, packedLight, packedOverlay);
   }

   /**
    * Draws the block model the joint is configured with. 1.12.2 rendered it through a
    * {@code DummyIBlockAccess} plus a normal-fixing VBO; {@code renderSingleBlock} already emits
    * per-face normals, so the whole workaround is gone.
    */
   public static void renderNormalBlock(BlockState state, PoseStack pose, MultiBufferSource bufferSource,
                                        int packedLight, int packedOverlay) {
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, bufferSource, packedLight, packedOverlay);
   }

   public static void renderIJBModel(IJBModelItem.IJBModel ijbModel, PoseStack pose, MultiBufferSource bufferSource,
                                     int packedLight, int packedOverlay, int modelMeta, int pass) {
      ijbModel.renderAll(modelMeta, pose, bufferSource, packedLight, packedOverlay, pass);
   }

   /** 1.12.2 {@code renderAll(modelMeta, 0.0625F, pass)} for the debug error model of an addon. */
   public static void renderErrorModel(IJBModelItem.IJBModel ijbModel, PoseStack pose, MultiBufferSource bufferSource,
                                       int packedLight, int packedOverlay, int modelMeta, int pass) {
      ijbModel.renderAll(modelMeta, pose, bufferSource, packedLight, packedOverlay, pass);
   }

   public static void addAdditonalSideRotatoin(PoseStack pose, int renderSide) {
      addAdditonalSideRotatoin(pose, renderSide, false);
   }

   public static void addAdditonalSideRotatoin(PoseStack pose, int renderSide, boolean negateZ) {
      float[] rotXZ = JBRotationUtil.rotXZ_matrix[renderSide];
      pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F * rotXZ[0]));
      pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0F * (negateZ ? -rotXZ[1] : rotXZ[1])));
   }

   public static void addAdditonalSideRotatoinOwnMatrix(int renderSide, ModelViewMatrix matrix) {
      addAdditonalSideRotatoinOwnMatrix(renderSide, false, matrix);
   }

   public static void addAdditonalSideRotatoinOwnMatrix(int renderSide, boolean negateZ, ModelViewMatrix matrix) {
      float[] rotXZ = JBRotationUtil.rotXZ_matrix[renderSide];
      matrix.rotateXX(rotXZ[0] * 3.1415927F);
      if (negateZ) {
         matrix.rotateZZ(-rotXZ[1] * 3.1415927F);
      } else {
         matrix.rotateZZ(rotXZ[1] * 3.1415927F);
      }
   }

   public static void addAdditonalFacingRotatoin(PoseStack pose, int facing) {
      addAdditonalFacingRotatoin(pose, facing, true);
   }

   public static void addAdditonalFacingRotatoin(PoseStack pose, int facing, boolean negateY) {
      float rotY = JBRotationUtil.roatYawEachSide[facing];
      pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(negateY ? -rotY : rotY));
   }

   public static void setAdditonalRenderState(TileEntityJoint joint, float partial) {
      if (joint.jointType == 2) {
         if (joint.initValues[0][2] == 1) {
            renderSeatAs = SEAT_AS_INTERFACE;
         } else if (joint.initValues[0][2] == 2) {
            renderSeatAs = SEAT_AS_CAMERA_TE;
         } else {
            renderSeatAs = SEAT_AS_SEAT;
            modelSeat.setModelState(joint);
         }
      }

      if (joint.jointType == 0) {
         renderCoreAsSlave = joint instanceof TileEntityJointCore && ((TileEntityJointCore)joint).isSlaveMode();
         modelCore.setAdditionalRenderState(joint, partial);
      }

      if (joint.jointType == 10) {
         modelExtender.setModelState(joint, partial);
      }
   }

   public static void setAdditonalRenderState(EntityJointed entityJointed, JointParts joint, float partial) {
      if (joint.jointType == 2) {
         if (joint.initValues[0][2] == 1) {
            renderSeatAs = SEAT_AS_INTERFACE;
         } else if (joint.initValues[0][2] == 2) {
            renderSeatAs = SEAT_AS_CAMERA_JT;
         } else {
            renderSeatAs = SEAT_AS_SEAT;
            modelSeat.setModelState(joint);
         }
      }

      if (joint.jointType == 0) {
         renderCoreAsSlave = entityJointed.isCoreSubColor();
         modelCore.setAdditionalRenderState(joint, partial);
      }

      if (joint.jointType == 10) {
         modelExtender.setModelState(joint, partial);
      }
   }

   /**
    * The vertical connecting beam of a slave unit, drawn from the unit origin upward.
    *
    * <p>1.12.2 drew it as {@code floor(2 * slaveOffsetDiv)} stacked boxes with a white texture and
    * lighting disabled.
    */
   public static void renderBeam(PoseStack pose, MultiBufferSource bufferSource, double slaveOffsetDiv) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.solidColor());
      Matrix4f matrix = pose.last().pose();
      double widthHalf = 0.4;
      double heightHalf = 0.15;
      double basePosY = 1.0 + heightHalf;
      int height = (int)Math.floor(2.0 * slaveOffsetDiv);

      for (int i = 0; i < height; ++i) {
         double minX = -widthHalf;
         double maxX = widthHalf;
         double minY = basePosY - heightHalf;
         double maxY = basePosY + heightHalf;
         double minZ = -widthHalf;
         double maxZ = widthHalf;
         quad(consumer, matrix, 1.0F, 1.0F, 1.0F, 1.0F,
            minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
         quad(consumer, matrix, 1.0F, 1.0F, 1.0F, 1.0F,
            minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
         quad(consumer, matrix, 1.0F, 1.0F, 1.0F, 1.0F,
            minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ);
         quad(consumer, matrix, 1.0F, 1.0F, 1.0F, 1.0F,
            maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ);
         basePosY -= 0.5;
      }
   }

   /**
    * The blue/grey/yellow/red master-slave connection bars.
    *
    * <p>1.12.2 issued three batches: two quad batches coloured
    * {@code (0,0,0.8)}/{@code (0.3,0.3,0.3)} then {@code (0.8,0.8,0.2)}/{@code (0.7,0.7,0.7)}, and
    * a final GL_TRIANGLES cap coloured {@code (0.8,0,0)}. The cap is two flat triangles; emitted
    * here as a quad with the third vertex repeated, which renders identically in a QUADS buffer.
    */
   public static void renderAbstractConnection(PoseStack pose, MultiBufferSource bufferSource, boolean dullColor) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.solidColor());
      Matrix4f matrix = pose.last().pose();
      double widthXZ = 0.05;
      double widthY = 0.1;
      double minX = -widthXZ;
      double maxX = widthXZ;
      double minY = 1.0 - widthY;
      double maxY = 1.0 + widthY;
      double minZ = -widthXZ;
      double maxZ = widthXZ;

      float r = dullColor ? 0.3F : 0.0F;
      float g = dullColor ? 0.3F : 0.0F;
      float b = dullColor ? 0.3F : 0.8F;
      quad(consumer, matrix, r, g, b, 1.0F, minX, maxY, 0.0, maxX, maxY, 0.0, maxX, minY, 0.0, minX, minY, 0.0);
      quad(consumer, matrix, r, g, b, 1.0F, 0.0, minY, minZ, 0.0, maxY, minZ, 0.0, maxY, maxZ, 0.0, minY, maxZ);
      quad(consumer, matrix, r, g, b, 1.0F, maxX, 1.0, maxZ, maxX, 1.0, minZ, minX, 1.0, minZ, minX, 1.0, maxZ);

      minY = maxY;
      maxY = 2.0 - widthY * 4.0;
      r = dullColor ? 0.7F : 0.8F;
      g = dullColor ? 0.7F : 0.8F;
      b = dullColor ? 0.7F : 0.2F;
      quad(consumer, matrix, r, g, b, 1.0F, minX, maxY, 0.0, maxX, maxY, 0.0, maxX, minY, 0.0, minX, minY, 0.0);
      quad(consumer, matrix, r, g, b, 1.0F, 0.0, minY, minZ, 0.0, maxY, minZ, 0.0, maxY, maxZ, 0.0, minY, maxZ);

      minY = maxY;
      maxY = 2.0 - widthY * 2.0;
      quad(consumer, matrix, 0.8F, 0.0F, 0.0F, 1.0F,
         minX, minY, 0.0, maxX, minY, 0.0, 0.0, maxY, 0.0, 0.0, maxY, 0.0);
      quad(consumer, matrix, 0.8F, 0.0F, 0.0F, 1.0F,
         0.0, minY, maxZ, 0.0, minY, minZ, 0.0, maxY, 0.0, 0.0, maxY, 0.0);
   }

   /** Emits one quad in the ambient, unlit pipeline: only position and colour reach the buffer. */
   private static void quad(VertexConsumer consumer, Matrix4f matrix,
                            float r, float g, float b, float a,
                            double x0, double y0, double z0, double x1, double y1, double z1,
                            double x2, double y2, double z2, double x3, double y3, double z3) {
      consumer.vertex(matrix, (float)x0, (float)y0, (float)z0).color(r, g, b, a).endVertex();
      consumer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a).endVertex();
      consumer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a).endVertex();
      consumer.vertex(matrix, (float)x3, (float)y3, (float)z3).color(r, g, b, a).endVertex();
   }

   /** Full-bright helper for the untextured overlays, which are not lit in 1.12.2 either. */
   public static int unlitLight() {
      return LightTexture.FULL_BRIGHT;
   }
}
