package jp.mc.ancientred.jointblock.tileentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.common.CommonProxyClient;
import jp.mc.ancientred.jointblock.common.TypedLinkable;
import jp.mc.ancientred.jointblock.common.TypedLinkedList;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityBullet;
import jp.mc.ancientred.jointblock.entity.render.RenderEntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJBEntityBatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Draws the sticky-entity batch list: an invisible {@link TileEntityJBEntityBatcher} is parked in
 * front of the camera so that, while {@code JBConfig.bypathEJRenderByStickyTileEntity} is on, the
 * jointed entities / bullets / worker effects on {@link #renderList} are rendered from here instead
 * of through the normal entity pass.
 *
 * <p>1.12.2 was a {@code TileEntitySpecialRenderer} with:
 * <ul>
 *   <li>{@code MinecraftForgeClient.getRenderPass()} plus {@code Entity.shouldRenderInPass(pass)} -
 *       both gone in 1.20.1 (a single render pass). The {@code renderToken}/{\@code checkRendered}
 *       bookkeeping that guarded against an entity being drawn twice is kept verbatim.</li>
 *   <li>{@code TileEntityRendererDispatcher.field_147554_b/c/d} (the dispatcher's camera position) -
 *       replaced by {@code Camera.getPosition()}, the same value the level pose is built from.</li>
 *   <li>{@code OpenGlHelper.setLightmapTextureCoords} - replaced by passing the computed
 *       {@code packedLight} to the entity renderer.</li>
 *   <li>{@code TileEntityJBEntityBatcher.shouldRenderInPass() == true},
 *       {@code getDistanceSq() == 1.0} and {@code getMaxRenderDistanceSquared() == Double.MAX_VALUE}
 *       (the "never cull me, the sticky AABB does the culling" overrides) - replaced by
 *       {@link #shouldRender} returning {@code true} and {@link #getViewDistance()} returning
 *       {@code Integer.MAX_VALUE}.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class TileEntityJBEntityBatcherRenderer implements BlockEntityRenderer<TileEntityJBEntityBatcher> {

   public static final TypedLinkedList<TypedLinkable> renderList = new TypedLinkedList<>();
   public static long renderToken = -1L;

   public static final BlockEntityRendererProvider<TileEntityJBEntityBatcher> PROVIDER = TileEntityJBEntityBatcherRenderer::new;

   public TileEntityJBEntityBatcherRenderer(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileEntityJBEntityBatcher be, float tickPartial, PoseStack pose, MultiBufferSource bufferSource,
                      int packedLight, int packedOverlay) {
      if (!JBConfig.bypathEJRenderByStickyTileEntity) {
         CommonProxyClient.renderedEntitysCnt = -1;
         return;
      }

      ++CommonProxyClient.renderedStickeyCnt;
      Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      BlockPos bePos = be.getBlockPos();
      // The 1.12.2 renderer ran with the camera-relative world coordinates of the batcher's dispatcher
      // pass; the 1.20.1 pose handed to a block entity renderer sits on the batcher block itself, so
      // undo that translation to get back to the camera origin the entity renderers expect.
      pose.pushPose();
      pose.translate((float)(camPos.x - (double)bePos.getX()), (float)(camPos.y - (double)bePos.getY()), (float)(camPos.z - (double)bePos.getZ()));

      TypedLinkable typedEntity = renderList.getFirst();
      ++renderToken;

      for(; typedEntity != null; typedEntity = typedEntity.getNext()) {
         if (typedEntity instanceof Entity) {
            if (!typedEntity.checkRendered(renderToken)) {
               CommonProxyClient.renderedEntitysCnt = -10;
               break;
            }

            Entity entity = (Entity)typedEntity;
            // 1.12.2 also tested Entity#shouldRenderInPass(pass) here; 1.20.1 renders in a single pass.
            double dPosX = entity.xo + (entity.getX() - entity.xo) * (double)tickPartial - camPos.x;
            double dPosY = entity.yo + (entity.getY() - entity.yo) * (double)tickPartial - camPos.y;
            double dPosZ = entity.zo + (entity.getZ() - entity.zo) * (double)tickPartial - camPos.z;
            float fYaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * tickPartial;
            // 1.12.2 Entity#func_70070_b() = EntityRenderDispatcher#getPackedLightCoords in 1.20.1.
            // OpenGlHelper#setLightmapTextureCoords is gone: the value is passed to the renderers instead.
            int br = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(entity, tickPartial);
            if (entity.isOnFire()) {
               br = LightTexture.FULL_BRIGHT;
            }

            if (entity instanceof EntityJointed) {
               RenderEntityJointed.doRenderWithOwnMatrix((EntityJointed)entity, dPosX, dPosY, dPosZ, fYaw, tickPartial, pose, bufferSource, br);
               ++CommonProxyClient.renderedEntitysCnt;
            } else if (entity instanceof EntityBullet) {
               RenderEntityBullet.doRenderBullet((EntityBullet)entity, dPosX, dPosY, dPosZ, fYaw, tickPartial, pose, bufferSource, br);
               ++CommonProxyClient.renderedEntitysCnt;
            } else if (entity instanceof EntityTempWorker && ((EntityTempWorker)entity).renderOn) {
               if (((EntityTempWorker)entity).workerType == EntityTempWorker.WorkerType.blade) {
                  RenderEntityBullet.doRenderBlade((EntityTempWorker)entity, dPosX, dPosY, dPosZ, fYaw, tickPartial, pose, bufferSource, br);
                  ++CommonProxyClient.renderedEntitysCnt;
               } else if (((EntityTempWorker)entity).workerType == EntityTempWorker.WorkerType.flamer) {
                  RenderEntityBullet.doRenderFlame((EntityTempWorker)entity, dPosX, dPosY, dPosZ, fYaw, tickPartial, pose, bufferSource, br);
                  ++CommonProxyClient.renderedEntitysCnt;
               }
            }
         }
      }

      pose.popPose();
   }

   /**
    * 1.12.2 {@code TileEntityJBEntityBatcher.getDistanceSq()} returned {@code 1.0} and
    * {@code shouldRenderInPass} returned {@code true}: the batcher is never distance-culled, because
    * {@code getRenderBoundingBox()} (driven by {@code CommonProxyClient.updateStickeyTeAABB}) is what
    * decides whether the batch list is in view.
    */
   @Override
   public boolean shouldRender(TileEntityJBEntityBatcher be, Vec3 cameraPos) {
      return true;
   }

   /** 1.12.2 {@code TileEntityJBEntityBatcher.getMaxRenderDistanceSquared() == Double.MAX_VALUE}. */
   @Override
   public int getViewDistance() {
      return Integer.MAX_VALUE;
   }
}
