package jp.mc.ancientred.jointblock.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;
import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeMeshBuilder;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws the carved (palette-coloured) shape of a joint block or joint part.
 *
 * <p>1.12.2 wrote directly into a {@code Tessellator}/{@code BufferBuilder}, one batch per carve
 * type: {@code GL_QUADS} + {@code DefaultVertexFormats.POSITION_COLOR_NORMAL} for box/M-cube,
 * {@code GL_TRIANGLES} + {@code POSITION_COLOR_NORMAL} for patterns, always after
 * {@code GlStateManager.bindTexture(whiteResTexId)} (the MTL-less models carry their colour in the
 * vertex colour, so the bound texture only had to be white). In 1.20.1 the texture and light state
 * come from the {@link net.minecraft.client.renderer.RenderType}, so the white-texture bind (and
 * the whole {@code hasShaderModInstalled} fallback) is gone and the geometry is written into a
 * {@link VertexConsumer} taken from the caller's {@link MultiBufferSource}. All three carve types
 * therefore share {@link JBRenderTypes#solidColorTriangles()} - untextured POSITION_COLOR triangles,
 * unlit, unculled (a QUADS buffer would halve every six-vertex face).
 *
 * <p>The type dispatch and the per-vertex geometry/colour are the 1.12.2 code verbatim, including
 * the M-cube lazy gather ({@code MCubeMeshBuilder.gatherMCCarvingDataTileEntity} /
 * {@code gatherMCCarvingDataJointParts}).
 */
@OnlyIn(Dist.CLIENT)
public class CarvingRenderer {

   /** 1.12.2 {@code renderTeCarvedModelByType(TileEntityJoint, int[])}. */
   public static void renderTeCarvedModelByType(TileEntityJoint te, int[] paletteData, PoseStack pose,
                                                MultiBufferSource bufferSource, int packedLight) {
      switch (JBCarvingDataUtil.getCarvingType(te.carvingData)) {
         case 2:
            renderPatternCarvedModel(te.carvingData, paletteData, pose, bufferSource, packedLight);
            return;
         case 4:
            if (te.mCubeGatheredData == null || te.mCubeGatheredData.dim != JBCarvingDataUtil.getCarvingDivide(te.carvingData)) {
               te.mCubeGatheredData = MCubeMeshBuilder.gatherMCCarvingDataTileEntity(te.mCubeGatheredData, te);
               if (te.mCubeGatheredData == null) {
                  return;
               }
            }
            renderMCubeCarvedModel(te.carvingData, te.mCubeGatheredData, paletteData, pose, bufferSource, packedLight);
            return;
         default:
            renderBoxCarvedModel(te.carvingData, paletteData, pose, bufferSource, packedLight);
      }
   }

   /** 1.12.2 {@code renderJointCarvedModelByType(JointPartsRenderable, int[])}. */
   public static void renderJointCarvedModelByType(JointPartsRenderable joint, int[] paletteData, PoseStack pose,
                                                   MultiBufferSource bufferSource, int packedLight) {
      switch (joint.carvingType) {
         case 2:
            renderPatternCarvedModel(joint.carvingData, paletteData, pose, bufferSource, packedLight);
            return;
         case 4:
            if (joint.mCubeGatheredData == null || joint.mCubeGatheredData.dim != JBCarvingDataUtil.getCarvingDivide(joint.carvingData)) {
               joint.mCubeGatheredData = MCubeMeshBuilder.gatherMCCarvingDataJointParts(joint.mCubeGatheredData, joint);
               if (joint.mCubeGatheredData == null) {
                  return;
               }
            }
            renderMCubeCarvedModel(joint.carvingData, joint.mCubeGatheredData, paletteData, pose, bufferSource, packedLight);
            return;
         default:
            renderBoxCarvedModel(joint.carvingData, paletteData, pose, bufferSource, packedLight);
      }
   }

   /**
    * 1.12.2 {@code renderTeCarvedModelByType} on the compiled path: the carved mesh of the TE is
    * built once and replayed until {@code TileEntityJoint#isCarvingCompiled} is cleared (which every
    * carve-data write already does, as in 1.12.2) or the palette contents change.
    */
   public static void renderTeCarvedModelCached(TileEntityJoint te, int[] paletteData, PoseStack pose,
                                                MultiBufferSource bufferSource, int packedLight) {
      int type = JBCarvingDataUtil.getCarvingType(te.carvingData);
      MCubeGathered gathered = null;

      if (type == 4) {
         if (te.mCubeGatheredData == null || te.mCubeGatheredData.dim != JBCarvingDataUtil.getCarvingDivide(te.carvingData)) {
            te.mCubeGatheredData = MCubeMeshBuilder.gatherMCCarvingDataTileEntity(te.mCubeGatheredData, te);
            if (te.mCubeGatheredData == null) {
               return;
            }
         }

         gathered = te.mCubeGatheredData;
      }

      CarvedMeshCache cache = cacheFor(te);

      if (!te.isCarvingCompiled) {
         cache.invalidate();
      }

      VertexConsumer bb = bufferSource.getBuffer(JBRenderTypes.solidColorTriangles());
      cache.render(bb, pose.last().pose(), te.carvingData, type, paletteData, gathered);
      te.isCarvingCompiled = true;
   }

   /**
    * 1.12.2 {@code renderJointCarvedModelByType} on the compiled path: the carved mesh of the part
    * is built once and replayed while the part keeps the same carving data (assembly data updates
    * always replace the part's {@code carvingData} array) and the same palette contents.
    */
   public static void renderJointCarvedModelCached(JointPartsRenderable joint, int[] paletteData, PoseStack pose,
                                                   MultiBufferSource bufferSource, int packedLight) {
      int type = joint.carvingType;
      MCubeGathered gathered = null;

      if (type == 4) {
         if (joint.mCubeGatheredData == null || joint.mCubeGatheredData.dim != JBCarvingDataUtil.getCarvingDivide(joint.carvingData)) {
            joint.mCubeGatheredData = MCubeMeshBuilder.gatherMCCarvingDataJointParts(joint.mCubeGatheredData, joint);
            if (joint.mCubeGatheredData == null) {
               return;
            }
         }

         gathered = joint.mCubeGatheredData;
      }

      VertexConsumer bb = bufferSource.getBuffer(JBRenderTypes.solidColorTriangles());
      joint.carvedMeshCache.render(bb, pose.last().pose(), joint.carvingData, type, paletteData, gathered);
   }

   /** Per-TE caches; weak keys so an unloaded joint core does not keep its mesh alive. */
   private static final Map<TileEntityJoint, CarvedMeshCache> TE_CACHES = new WeakHashMap<>();

   private static CarvedMeshCache cacheFor(TileEntityJoint te) {
      CarvedMeshCache cache = TE_CACHES.get(te);

      if (cache == null) {
         cache = new CarvedMeshCache();
         TE_CACHES.put(te, cache);
      }

      return cache;
   }

   /** 1.12.2 box carve ({@code renderBoxesCarvedModel}). */
   public static void renderBoxCarvedModel(int[] carvingData, int[] paletteData, PoseStack pose,
                                           MultiBufferSource bufferSource, int packedLight) {
      VertexConsumer bb = bufferSource.getBuffer(JBRenderTypes.solidColorTriangles());
      JBCarvingClientUtil.renderBoxesCarvedModel(bb, pose.last().pose(), carvingData, paletteData);
   }

   /** 1.12.2 pattern carve ({@code GL_TRIANGLES}). */
   public static void renderPatternCarvedModel(int[] carvingData, int[] paletteData, PoseStack pose,
                                               MultiBufferSource bufferSource, int packedLight) {
      VertexConsumer bb = bufferSource.getBuffer(JBRenderTypes.solidColorTriangles());
      JBPatternCarvingClientUtil.renderPatternCarvedModel(bb, pose.last().pose(), carvingData, paletteData);
   }

   /** 1.12.2 M-cube carve ({@code renderMCubesCarvedModel}). */
   public static void renderMCubeCarvedModel(int[] carvingData, MCubeGathered mCubeGatheredData, int[] paletteData,
                                             PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      VertexConsumer bb = bufferSource.getBuffer(JBRenderTypes.solidColorTriangles());
      JBCarvingClientUtil.renderMCubesCarvedModel(bb, pose.last().pose(), carvingData, mCubeGatheredData, paletteData);
   }
}
