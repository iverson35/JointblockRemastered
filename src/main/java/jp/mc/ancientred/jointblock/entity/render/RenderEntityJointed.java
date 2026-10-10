package jp.mc.ancientred.jointblock.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.assemble.Quaternion;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.carving.prebuild.PrebuildWriter;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code Render<Entity>} -> 1.20.1 {@link EntityRenderer}&lt;{@link EntityJointed}&gt;.
 *
 * <p>GL substitutions:
 * <ul>
 *   <li>{@code GlStateManager.pushMatrix/popMatrix} -> the dispatcher owns the {@link PoseStack}
 *       push/pop; {@link #doRenderWithOwnMatrix} only pushes its own {@code (posX,posY,posZ)}
 *       offset for the sticky-batcher call path.</li>
 *   <li>{@code ModelViewMatrix.tHreadMvMatrix.get()} is still built exactly as in 1.12.2 (reset,
 *       ex-fly quaternion or yaw, {@code scale(1,-1,-1)}, master-control core height,
 *       {@code saveBaseRotation}). The 1.12.2 {@code translate(posX,posY,posZ)} on the model matrix
 *       is dropped: the 1.20.1 dispatcher {@code PoseStack} is already translated to the entity, and
 *       {@link JointPartsRenderable} right-multiplies this (entity local) matrix itself.</li>
 *   <li>{@code matrix.renderPass = MinecraftForgeClient.getRenderPass()} plus the pass-1 blend
 *       toggles are dropped - 1.20.1 renders in a single pass and blending is a
 *       {@link RenderType} property.</li>
 *   <li>{@code RenderGlobal.drawBoundingBox} (filled POSITION_COLOR box) ->
 *       {@link LevelRenderer#renderLineBox} (POSITION_COLOR_NORMAL outline); the colour selection
 *       (red / blue when ridden / green when friendly) is unchanged.</li>
 *   <li>{@code ActiveRenderInfo}/{@code RenderHelper} seat marker particle
 *       ({@code renderSeatParticle}) is not portable: {@code EntitySeatPart} is a Forge
 *       {@code PartEntity} in 1.20.1 and is never handed to an {@link EntityRenderer}, so the 1.12.2
 *       call site ({@code func_76986_a} seat branch) does not exist.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityJointed extends EntityRenderer<EntityJointed> {

   public RenderEntityJointed(EntityRendererProvider.Context context) {
      super(context);
      // 1.12.2 field_76989_e = 0.0F.
      this.shadowRadius = 0.0F;
   }

   /**
    * 1.12.2 {@code func_76986_a} jointed branch. The seat-particle branch is gone (see class
    * javadoc) and {@code brightnessTemp} is refreshed from the renderer's packed light, which is
    * the 1.20.1 replacement for the {@code Entity#getBrightnessForRender} hook the 1.12.2 override
    * of the entity provided.
    */
   @Override
   public void render(EntityJointed entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      if (!JBConfig.bypathEJRenderByStickyTileEntity) {
         entity.brightnessTemp = this.getPackedLightCoords(entity, partialTick);
         // The dispatcher PoseStack already sits on the entity, so the model offset is zero.
         doRenderWithOwnMatrix(entity, 0.0, 0.0, 0.0, entityYaw, partialTick, pose, bufferSource, packedLight);
      }
   }

   /**
    * 1.12.2 {@code func_70070_b} sampled the packed light at
    * {@code floor(x), floor(y + (coreHeight < 2.0F ? coreHeight + 1.0F : coreHeight)), floor(z)} and
    * returned 0 outside loaded chunks. {@code EntityRenderer#getPackedLightCoords} is final in
    * 1.20.1, so the two level hooks it packs ({@code block << 4 | sky << 20}) carry that sampling.
    */
   private static int jointPackedLight(EntityJointed entity) {
      float offset = entity.coreHeight;
      if (offset < 2.0F) {
         ++offset;
      }
      BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() + (double)offset, entity.getZ());
      return entity.level().hasChunkAt(pos) ? LevelRenderer.getLightColor(entity.level(), pos) : 0;
   }

   @Override
   protected int getBlockLightLevel(EntityJointed entity, BlockPos pos) {
      return jointPackedLight(entity) >> 4 & 0xFFFF;
   }

   @Override
   protected int getSkyLightLevel(EntityJointed entity, BlockPos pos) {
      return jointPackedLight(entity) >> 20 & 0xFFFF;
   }

   /**
    * 1.12.2 {@code doRenderWithOwnMatrix}. Kept {@code public static} (rather than the private
    * instance method the plain render path would need) because the sticky-batcher TESR draws the
    * batched entity list outside the normal entity pass and calls this directly, as the 1.12.2
    * {@code TileEntityJBEntityBatcherRenderer} did.
    *
    * <p>{@code posX/posY/posZ} are the entity's camera-relative interpolated coordinates and the
    * {@link PoseStack} must originate at the camera. Pass {@code 0,0,0} when the caller already
    * translated the pose onto the entity.
    */
   public static void doRenderWithOwnMatrix(EntityJointed entityJointed, double posX, double posY, double posZ,
                                            float entityYaw, float tickPartial, PoseStack pose,
                                            MultiBufferSource bufferSource, int packedLight) {
      if (entityJointed.jointParts != null && entityJointed.jointParts instanceof JointPartsRenderable) {
         entityJointed.isClientPlayerOnBoard = false;
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer theClientPlayer;
         // 1.12.2 field_71474_y.field_74320_O == 0 -> first person.
         if (mc.options.getCameraType() == CameraType.FIRST_PERSON && (theClientPlayer = mc.player) != null) {
            entityJointed.isClientPlayerOnBoard = entityJointed.isRidingThisEntity(theClientPlayer);
         }

         entityJointed.reverseSelectedWireframe = false;
         if (StaticClientTuningStates.targetSelected.isSetectionEnabled && StaticClientTuningStates.targetSelected.rayTracedHitEntity == entityJointed) {
            entityJointed.reverseSelectedWireframe = true;
         }

         ModelViewMatrix matrix = ModelViewMatrix.tHreadMvMatrix.get();
         matrix.reset();

         pose.pushPose();
         pose.translate((float)posX, (float)posY, (float)posZ);

         float coreHeight;
         if (entityJointed.exFlyAttr != null && entityJointed.unitAbility.isExFlyable()) {
            Quaternion.slerp(tickPartial, entityJointed.exFlyAttr.prevCoreRotQuats, entityJointed.exFlyAttr.coreRotQuats, entityJointed.exFlyAttr.workCoreRotQuats);
            matrix.setRotation(entityJointed.exFlyAttr.workCoreRotQuats);
         } else {
            float yaw = entityJointed.yRotO + (entityJointed.getYRot() - entityJointed.yRotO) * tickPartial;
            matrix.rotateYY(-yaw * 0.017453292F);
         }

         matrix.scale(1.0F, -1.0F, -1.0F);
         if (!entityJointed.disassemble && entityJointed.isUnderMasterControl()) {
            coreHeight = entityJointed.prevCoreHight + (entityJointed.coreHeight - entityJointed.prevCoreHight) * tickPartial;
            matrix.translate(0.0F, -1.5F - coreHeight, 0.0F);
            matrix.saveBaseRotation();
            entityJointed.slaveAttatcher.setAdditionalMatrix(entityJointed, matrix);
         } else {
            coreHeight = entityJointed.prevCoreHight + (entityJointed.coreHeight - entityJointed.prevCoreHight) * tickPartial;
            matrix.translate(0.0F, -1.5F - coreHeight, 0.0F);
            matrix.saveBaseRotation();
         }

         entityJointed.isTunbleEntityOnClient = JBModContainer.proxy.isEntityJointedAllowdTuning(entityJointed);
         if (entityJointed.prebuilder != null && entityJointed.prebuilder instanceof Prebuilder) {
            Prebuilder prebuilder = (Prebuilder)entityJointed.prebuilder;

            try {
               prebuilder.setIdentity();
               float yaw = entityJointed.yRotO + (entityJointed.getYRot() - entityJointed.yRotO) * tickPartial;
               prebuilder.rotateYY(yaw * 0.017453292F);
               // The carved mesh is built in world space; shift it into the entity frame, exactly as
               // the 1.12.2 renderer did with the camera-relative entity position.
               Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
               prebuilder.translate(
                  (float)(-(Mth.lerp(tickPartial, entityJointed.xOld, entityJointed.getX()) - cameraPos.x)),
                  (float)(-(Mth.lerp(tickPartial, entityJointed.yOld, entityJointed.getY()) - cameraPos.y)),
                  (float)(-(Mth.lerp(tickPartial, entityJointed.zOld, entityJointed.getZ()) - cameraPos.z)));
               prebuilder.setupBase();
               JointPartsRenderable.renderAllPartsWithPrebuild(entityJointed, tickPartial, pose, bufferSource, packedLight);
            } finally {
               entityJointed.prebuilder = null;
            }

            if (PrebuildWriter.kickWriteQueue == null) {
               PrebuildWriter.kickWriteQueue = prebuilder;
            }
         } else {
            JointPartsRenderable.renderAllPartsWithOwnMatrix(entityJointed, tickPartial, pose, bufferSource, packedLight);
         }

         // 1.12.2 also required MinecraftForgeClient.getRenderPass() == 0.
         if (JBConfig.showJBBounds > 0 && !entityJointed.disassemble) {
            renderDebugBoundingBox(entityJointed, pose, bufferSource);
         }

         pose.popPose();
      }
   }

   /**
    * 1.12.2 {@code renderDebugBoundingBox}. {@code RenderGlobal.drawBoundingBox} drew a filled box
    * through the white texture with lighting and culling disabled; 1.20.1 has no white texture and
    * the closest API is the vanilla line box ({@link RenderType#lines()}, unlit, unculled). The box
    * is emitted at the entity origin because the {@link PoseStack} already sits on the entity.
    */
   private static void renderDebugBoundingBox(EntityJointed entityJointed, PoseStack pose, MultiBufferSource bufferSource) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         float colR = 1.0F;
         float colG = 0.0F;
         float colB = 0.0F;
         if (entityJointed.isRidingThisEntity(mc.player)) {
            colR = 0.0F;
            colG = 0.0F;
            colB = 1.0F;
         } else if (entityJointed.isFriendlyEntity(mc.player)) {
            colR = 0.0F;
            colG = 1.0F;
            colB = 0.0F;
         }

         float f2 = entityJointed.getBbWidth() / 2.0F;
         AABB bb = new AABB(-f2, 0.0, -f2, f2, entityJointed.getBbHeight(), f2);
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
         LevelRenderer.renderLineBox(pose, consumer, bb, colR, colG, colB, 1.0F);
      }
   }

   /**
    * 1.12.2 {@code func_177071_a} returned {@code true} unconditionally (the assembled unit is far
    * larger than its entity bounding box, so frustum culling must not apply). The 1.20.1 override
    * keeps that and adds the {@code jointParts} gate that mirrors
    * {@link #doRenderWithOwnMatrix}'s early return.
    */
   @Override
   public boolean shouldRender(EntityJointed entity, Frustum frustum, double camX, double camY, double camZ) {
      return entity.jointParts != null && entity.jointParts instanceof JointPartsRenderable;
   }

   /** 1.12.2 {@code func_110775_a} returned {@code null} - the jointed entity binds its own textures. */
   @Override
   public ResourceLocation getTextureLocation(EntityJointed entity) {
      return null;
   }
}
