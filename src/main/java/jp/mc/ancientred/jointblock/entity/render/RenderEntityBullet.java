package jp.mc.ancientred.jointblock.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 1.12.2 {@code Render} -> 1.20.1 {@link EntityRenderer}&lt;{@link EntityBullet}&gt; for the beam
 * projectiles. The 1.12.2 class was also the registered renderer for {@link EntityTempWorker}
 * (blade trail / flamer flame); because 1.20.1 entity renderers are typed, the temp-worker entry
 * points are kept here as static methods ({@link #doRenderBlade}, {@link #doRenderFlame},
 * {@link #renderTempWorker}) for the temp-worker renderer to delegate to.
 *
 * <p>GL substitutions:
 * <ul>
 *   <li>{@code GlStateManager.pushMatrix/popMatrix/translate/rotate/scale} ->
 *       {@link PoseStack} (same right-multiply order). {@code enableRescaleNormal} /
 *       {@code disableRescaleNormal} have no 1.20.1 counterpart (the normal matrix is derived from
 *       the pose) and are dropped.</li>
 *   <li>{@code bindTexture(whiteResTexId)} + {@code POSITION_COLOR} -> {@link JBRenderTypes#solidColor()}
 *       ({@code RenderType.debugQuads()}: POSITION_COLOR, unlit, unculled). 1.12.2 drew the laser
 *       with lighting on, so it was tinted by the lightmap; the 1.20.1 type is unlit, which is the
 *       closest available state set.</li>
 *   <li>{@code bindTexture(rocketTextures)} + {@code POSITION_TEX} -> {@link JBRenderTypes#jointModel}
 *       ({@code entityCutoutNoCull}); the 1.12.2 draw had culling on, the cached joint type does
 *       not, which only adds the back faces of the octagonal tube.</li>
 *   <li>The blade trail ({@code POSITION_COLOR}, unlit, unculled) -> {@link JBRenderTypes#solidColor()}.
 *       The 1.12.2 body subtracted the interpolated camera entity position because the sticky
 *       batcher rendered at that entity's block; here the vertices are expressed relative to the
 *       worker entity that owns the pose.</li>
 *   <li>The flamer flame ({@code POSITION_TEX_COLOR}, unlit, unculled, block atlas) ->
 *       {@link JBRenderTypes#jointModel}({@link InventoryMenu#BLOCK_ATLAS}) with
 *       {@link LightTexture#FULL_BRIGHT} as the lightmap, which reproduces the unlit textured quad.
 *       The 1.12.2 atlas sprites {@code minecraft:blocks/fire_layer_0/1} are the 1.20.1
 *       {@code minecraft:block/fire_0/1}.</li>
 *   <li>{@code Minecraft.func_175606_aa()} -> {@code Minecraft.getInstance().getCameraEntity()}.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityBullet extends EntityRenderer<EntityBullet> {

   private static final ResourceLocation rocketTextures = new ResourceLocation("jointblock:textures/entity/rocket.png");

   public RenderEntityBullet(EntityRendererProvider.Context context) {
      super(context);
      // 1.12.2 func_76979_b (doRenderShadowAndFire) was an empty override.
      this.shadowRadius = 0.0F;
   }

   /** 1.12.2 {@code func_76986_a} bullet branch. */
   @Override
   public void render(EntityBullet entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      if (!JBConfig.bypathEJRenderByStickyTileEntity) {
         doRenderBullet(entity, 0.0, 0.0, 0.0, entityYaw, partialTick, pose, bufferSource, packedLight);
      }
   }

   /**
    * 1.12.2 {@code doRenderBullet}. {@code renderPosX/Y/Z} are camera-relative and the pose must
    * originate at the camera; pass {@code 0,0,0} when the caller already translated the pose onto
    * the entity (the normal entity pass).
    */
   public static void doRenderBullet(EntityBullet entityBullet, double renderPosX, double renderPosY, double renderPosZ,
                                     float entityYaw, float tickPartial, PoseStack pose,
                                     MultiBufferSource bufferSource, int packedLight) {
      pose.pushPose();
      pose.translate((float)renderPosX, (float)renderPosY, (float)renderPosZ);
      pose.mulPose(Axis.YP.rotationDegrees(entityYaw - 90.0F));
      pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(tickPartial, entityBullet.xRotO, entityBullet.getXRot())));

      if (entityBullet.bulletType != EntityBullet.BulletType.rokect
            && entityBullet.bulletType != EntityBullet.BulletType.missile
            && entityBullet.bulletType != EntityBullet.BulletType.mortar) {
         float beamScale = 0.05625F;
         pose.mulPose(Axis.XP.rotationDegrees(45.0F));
         pose.scale(beamScale, beamScale, beamScale);
         pose.translate(-4.0F, 0.0F, 0.0F);
         float lazerLength = (float)(entityBullet.lazerLength / 2.0);
         float lazerWidth = (float)(entityBullet.lazerWidth / 2.0);
         int color = entityBullet.extraFieldValue;
         float a = (float)(color >> 24 & 255) / 255.0F;
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;

         VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.solidColor());
         for (int i = 0; i < 4; ++i) {
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            Matrix4f matrix = pose.last().pose();
            consumer.vertex(matrix, -lazerLength, -lazerWidth, 0.0F).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, lazerLength, -lazerWidth, 0.0F).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, lazerLength, lazerWidth, 0.0F).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, -lazerLength, lazerWidth, 0.0F).color(r, g, b, a).endVertex();
         }
      } else {
         float uMin;
         float uMax;
         float vMin;
         float vMax;
         float bulletLength;
         float bulletWidth;
         if (entityBullet.bulletType == EntityBullet.BulletType.missile) {
            bulletLength = 3.0F;
            bulletWidth = 0.8F;
            uMin = 0.0F;
            uMax = 0.5F;
            vMin = 0.5F;
            vMax = 0.8125F;
            pose.scale(0.12F, 0.12F, 0.12F);
         } else if (entityBullet.bulletType == EntityBullet.BulletType.mortar) {
            bulletLength = 3.0F;
            bulletWidth = 0.8F;
            uMin = 0.5F;
            uMax = 1.0F;
            vMin = 0.5F;
            vMax = 0.8125F;
            pose.scale(0.24F, 0.24F, 0.12F);
         } else {
            bulletLength = 8.0F;
            bulletWidth = 2.0F;
            uMin = 0.0F;
            uMax = 1.0F;
            vMin = 0.0F;
            vMax = 0.3125F;
            pose.scale(0.12F, 0.12F, 0.12F);
         }

         VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(rocketTextures));
         for (int i = 0; i < 8; ++i) {
            pose.mulPose(Axis.XP.rotationDegrees(45.0F));
            Matrix4f matrix = pose.last().pose();
            Matrix3f normal = pose.last().normal();
            consumer.vertex(matrix, -bulletLength, -bulletWidth, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).uv(uMin, vMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, bulletLength, -bulletWidth, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).uv(uMax, vMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, bulletLength, bulletWidth, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).uv(uMax, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, -bulletLength, bulletWidth, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).uv(uMin, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
         }
      }

      pose.popPose();
   }

   /** 1.12.2 {@code func_76986_a} temp-worker branch: blade trail or flamer flame. */
   public static void renderTempWorker(EntityTempWorker tempWorker, float partialTick, PoseStack pose,
                                       MultiBufferSource bufferSource, int packedLight) {
      if (!JBConfig.bypathEJRenderByStickyTileEntity && tempWorker.renderOn) {
         if (tempWorker.workerType == EntityTempWorker.WorkerType.blade) {
            doRenderBlade(tempWorker, 0.0, 0.0, 0.0, 0.0F, partialTick, pose, bufferSource, packedLight);
         } else if (tempWorker.workerType == EntityTempWorker.WorkerType.flamer) {
            doRenderFlame(tempWorker, 0.0, 0.0, 0.0, 0.0F, partialTick, pose, bufferSource, packedLight);
         }
      }
   }

   /** 1.12.2 {@code doRenderBlade} - the blade trail ribbon between the two path arrays. */
   public static void doRenderBlade(EntityTempWorker tempWorker, double renderPosX, double renderPosY, double renderPosZ,
                                    float entityYaw, float partial, PoseStack pose,
                                    MultiBufferSource bufferSource, int packedLight) {
      Vec3[] pathVecsTop = tempWorker.pathVecsTop;
      Vec3[] pathVecsBot = tempWorker.pathVecsBot;
      if (pathVecsBot != null && pathVecsTop != null) {
         pose.pushPose();
         pose.translate((float)renderPosX, (float)renderPosY, (float)renderPosZ);

         double interpPosX = Mth.lerp(partial, tempWorker.xOld, tempWorker.getX());
         double interpPosY = Mth.lerp(partial, tempWorker.yOld, tempWorker.getY());
         double interpPosZ = Mth.lerp(partial, tempWorker.zOld, tempWorker.getZ());

         int color = tempWorker.extraFieldValue;
         float a = (float)(color >> 24 & 255) / 255.0F;
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;

         float prevTopFX = (float)(pathVecsTop[0].x - interpPosX);
         float prevTopFY = (float)(pathVecsTop[0].y - interpPosY);
         float prevTopFZ = (float)(pathVecsTop[0].z - interpPosZ);
         float prevBotFX = (float)(pathVecsBot[0].x - interpPosX);
         float prevBotFY = (float)(pathVecsBot[0].y - interpPosY);
         float prevBotFZ = (float)(pathVecsBot[0].z - interpPosZ);

         VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.solidColor());
         for (int i = 1; i < pathVecsBot.length - 1; ++i) {
            Vec3 vecTop = pathVecsTop[i];
            Vec3 vecBot = pathVecsBot[i];
            float topFX = (float)(vecTop.x - interpPosX);
            float topFY = (float)(vecTop.y - interpPosY);
            float topFZ = (float)(vecTop.z - interpPosZ);
            float botFX = (float)(vecBot.x - interpPosX);
            float botFY = (float)(vecBot.y - interpPosY);
            float botFZ = (float)(vecBot.z - interpPosZ);
            Matrix4f matrix = pose.last().pose();
            consumer.vertex(matrix, topFX, topFY, topFZ).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, botFX, botFY, botFZ).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, prevBotFX, prevBotFY, prevBotFZ).color(r, g, b, a).endVertex();
            consumer.vertex(matrix, prevTopFX, prevTopFY, prevTopFZ).color(r, g, b, a).endVertex();
            prevTopFX = topFX;
            prevTopFY = topFY;
            prevTopFZ = topFZ;
            prevBotFX = botFX;
            prevBotFY = botFY;
            prevBotFZ = botFZ;
         }

         pose.popPose();
      }
   }

   /** 1.12.2 {@code doRenderFlame} - the flamer flame quads along the path, from the block atlas. */
   public static void doRenderFlame(EntityTempWorker tempWorker, double renderPosX, double renderPosY, double renderPosZ,
                                    float entityYaw, float partial, PoseStack pose,
                                    MultiBufferSource bufferSource, int packedLight) {
      Vec3[] pathVecsTop = tempWorker.pathVecsTop;
      if (pathVecsTop != null) {
         pose.pushPose();
         pose.translate((float)renderPosX, (float)renderPosY, (float)renderPosZ);

         double interpPosX = Mth.lerp(partial, tempWorker.xOld, tempWorker.getX());
         double interpPosY = Mth.lerp(partial, tempWorker.yOld, tempWorker.getY());
         double interpPosZ = Mth.lerp(partial, tempWorker.zOld, tempWorker.getZ());

         int color = tempWorker.extraFieldValue;
         float a = (float)(color >> 24 & 255) / 255.0F;
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;

         Minecraft mc = Minecraft.getInstance();
         TextureAtlasSprite iconA = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft:block/fire_0"));
         TextureAtlasSprite iconB = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft:block/fire_1"));

         VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(InventoryMenu.BLOCK_ATLAS));
         for (int i = 0; i < pathVecsTop.length; ++i) {
            float fX = (float)(pathVecsTop[i].x - interpPosX);
            float fY = (float)(pathVecsTop[i].y - interpPosY);
            float fZ = (float)(pathVecsTop[i].z - interpPosZ);

            for (int ps = 0; ps < 4; ++ps) {
               pose.pushPose();
               pose.translate(fX, fY, fZ);
               float s = 1.0F + (float)i * 0.16F;
               pose.scale(s, s, s);
               TextureAtlasSprite icon = (ps & 1) == 0 ? iconA : iconB;
               float f3 = icon.getU0();
               float f4 = icon.getU1();
               float f5 = icon.getV0();
               float f6 = icon.getV1();
               float f7 = 1.0F;
               float f8 = 0.5F;
               float f9 = 0.25F;
               if (ps == 1) {
                  pose.mulPose(Axis.YP.rotationDegrees(90.0F));
               }

               if (ps == 2) {
                  pose.mulPose(Axis.XP.rotationDegrees(90.0F));
               }

               if (ps == 3) {
                  pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
               }

               Matrix4f matrix = pose.last().pose();
               Matrix3f normal = pose.last().normal();
               consumer.vertex(matrix, 0.0F - f8, 0.0F - f9, 0.0F).color(r, g, b, a).uv(f3, f6).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
               consumer.vertex(matrix, f7 - f8, 0.0F - f9, 0.0F).color(r, g, b, a).uv(f4, f6).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
               consumer.vertex(matrix, f7 - f8, 1.0F - f9, 0.0F).color(r, g, b, a).uv(f4, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
               consumer.vertex(matrix, 0.0F - f8, 1.0F - f9, 0.0F).color(r, g, b, a).uv(f3, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
               pose.popPose();
            }
         }

         pose.popPose();
      }
   }

   /** 1.12.2 {@code func_110775_a}. */
   @Override
   public ResourceLocation getTextureLocation(EntityBullet entity) {
      return rocketTextures;
   }
}
