package jp.mc.ancientred.jointblock.model.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Wavefront OBJ backed item model.
 *
 * <p>Ported: resource loading + the wavefront parse pipeline ({@link WavefrontBuildedObj}) and the
 * geometry draw of the loaded model ({@link #customModel}).
 *
 * <p>1.12.2 drew the baked faces through a {@code Tessellator}/{@code BufferBuilder} whose vertex
 * format varied per method ({@code POSITION_TEX_NORMAL}, {@code POSITION_COL_NORMAL},
 * {@code POSITION_COL}) and then {@code Tessellator.draw()}ed them with {@code GlStateManager.color}
 * as the current colour. In 1.20.1 the vertex format travels with the {@code RenderType}, so the
 * three variants now emit into one {@link VertexConsumer}:
 * <ul>
 *   <li>{@link #renderModel} - 1.12.2 {@code POSITION_TEX_NORMAL}; position, uv, face normal and a
 *       white vertex colour (1.12.2 carried no colour and relied on the caller's current GL colour,
 *       which the joint renderers reset to white).</li>
 *   <li>{@link #renderModelWithMtl} - 1.12.2 {@code POSITION_COL_NORMAL}; adds the per-face
 *       {@code material.kd} colour.</li>
 *   <li>{@link #renderModelWithMtlWithoutNormal} - 1.12.2 {@code POSITION_COL}. That format had no
 *       normal at all; the 1.20.1 entity vertex format requires one, so a constant {@code (0,0,1)}
 *       normal is written instead (recorded GL->API substitution).</li>
 * </ul>
 *
 * <p>Two further 1.12.2 -> 1.20.1 substitutions are recorded here:
 * <ul>
 *   <li>{@code facesTris} were submitted as {@code GL_TRIANGLES} and {@code facesQuads} as
 *       {@code GL_QUADS} in two separate {@code Tessellator.draw()} batches. A 1.20.1
 *       {@link VertexConsumer} is bound to a single {@code VertexFormat.Mode} (the joint
 *       {@code RenderType} is QUADS), so the triangles are emitted as degenerate quads (last vertex
 *       repeated), which rasterises to the identical triangle. The 1.12.2 tris-then-quads submission
 *       order is preserved.</li>
 *   <li>a missing {@code ObjVertex.t} (1.12.2 would NPE) falls back to {@code (0,0)}; all shipped
 *       {@code .obj} assets carry a {@code vt} for every vertex so this is unreachable in practice.</li>
 * </ul>
 *
 * <p>The display-list cache ({@code compiled}/{@code displayList}/{@code GLAllocation}) of 1.12.2 is
 * gone: 1.20.1 re-emits the vertices every frame through the buffer source.
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractJBObjModel implements IJBModelItem.IJBModel {
   protected boolean modelLoadError;
   protected WavefrontBuildedObj customModel;
   protected TextureManager tMan = Minecraft.getInstance().getTextureManager();

   public WavefrontBuildedObj getBakedObjModelFromRes(ResourceLocation modelLocation) {
      InputStream is = null;

      try {
         Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(modelLocation);
         is = resource.open();
         return new WavefrontBuildedObj(modelLocation.toString(), is);
      } catch (Exception var15) {
         var15.printStackTrace();
         this.modelLoadError = true;
      } finally {
         try {
            if (is != null) {
               is.close();
            }
         } catch (Exception var14) {
         }

      }

      return null;
   }

   /** 1.12.2 {@code renderAll()}: draw the baked model into the caller supplied consumer. */
   protected void renderAll(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      if (this.customModel != null) {
         this.renderModel(this.customModel, pose, consumer, packedLight, packedOverlay);
      }
   }

   /** 1.12.2 {@code renderModel(WavefrontBuildedObj)} - pos + uv + normal, white colour. */
   protected void renderModel(WavefrontBuildedObj model, PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      if (model == null) {
         return;
      }

      renderFaceList(model.facesTris, pose, consumer, packedLight, packedOverlay, false, true);
      renderFaceList(model.facesQuads, pose, consumer, packedLight, packedOverlay, false, true);
   }

   /** 1.12.2 {@code renderModelWithMtl(WavefrontBuildedObj)} - pos + per-face {@code material.kd} colour + normal. */
   protected void renderModelWithMtl(WavefrontBuildedObj model, PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      if (model == null) {
         return;
      }

      renderFaceList(model.facesTris, pose, consumer, packedLight, packedOverlay, true, true);
      renderFaceList(model.facesQuads, pose, consumer, packedLight, packedOverlay, true, true);
   }

   /** 1.12.2 {@code renderModelWithMtlWithoutNormal(WavefrontBuildedObj)} - pos + per-face colour, no normal. */
   protected void renderModelWithMtlWithoutNormal(WavefrontBuildedObj model, PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      if (model == null) {
         return;
      }

      renderFaceList(model.facesTris, pose, consumer, packedLight, packedOverlay, true, false);
      renderFaceList(model.facesQuads, pose, consumer, packedLight, packedOverlay, true, false);
   }

   /**
    * Emits one 1.12.2 face list. Faces whose {@link ObjFace#calculateFaceNormal()} fails are skipped,
    * exactly as the 1.12.2 render loop did.
    */
   private static void renderFaceList(List<ObjFace> faces, PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay, boolean useMtlColor, boolean useNormal) {
      if (faces == null || faces.isEmpty()) {
         return;
      }

      Matrix4f matrix = pose.last().pose();
      Matrix3f normalMatrix = pose.last().normal();

      for (ObjFace face : faces) {
         if (!face.calculateFaceNormal()) {
            continue;
         }

         float nx = useNormal ? face.faceNormal.x : 0.0F;
         float ny = useNormal ? face.faceNormal.y : 0.0F;
         float nz = useNormal ? face.faceNormal.z : 1.0F;
         float r = 1.0F;
         float g = 1.0F;
         float b = 1.0F;
         if (useMtlColor && face.material != null) {
            r = (float)face.material.kd.rx;
            g = (float)face.material.kd.gy;
            b = (float)face.material.kd.bz;
         }

         int count = face.vertIdxCnt;
         for (int i = 0; i < count; ++i) {
            emitVertex(consumer, matrix, normalMatrix, face.vertices[i], nx, ny, nz, r, g, b, packedLight, packedOverlay);
         }

         if (count == 3) {
            // GL_TRIANGLES has no RenderType in this port; close the triangle as a degenerate quad.
            emitVertex(consumer, matrix, normalMatrix, face.vertices[2], nx, ny, nz, r, g, b, packedLight, packedOverlay);
         }
      }
   }

   private static void emitVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix, ObjFace.ObjVertex fv, float nx, float ny, float nz, float r, float g, float b, int packedLight, int packedOverlay) {
      float u = fv.t != null ? fv.t.u : 0.0F;
      float v = fv.t != null ? fv.t.v : 0.0F;
      consumer.vertex(matrix, fv.v.x, fv.v.y, fv.v.z)
              .color(r, g, b, 1.0F)
              .uv(u, v)
              .overlayCoords(packedOverlay)
              .uv2(packedLight)
              .normal(normalMatrix, nx, ny, nz)
              .endVertex();
   }
}
