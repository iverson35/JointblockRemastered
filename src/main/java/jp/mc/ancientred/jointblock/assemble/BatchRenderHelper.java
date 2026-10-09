package jp.mc.ancientred.jointblock.assemble;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.model.render.CarvingRenderer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Render-time dispatch for carved joint models.
 *
 * <p>1.12.2 compiled each carved TE / joint part into a GL display list
 * ({@code GL11.glNewList(...)} / {@code GLCompiledListManager}) and replayed it with
 * {@code GL11.glCallList} until the carve data changed. That whole mechanism has no 1.20.1
 * equivalent: there is no display list API, {@code carvingsCompiled} state is not needed and the
 * vanilla renderer re-emits every frame. The {@code isCarvingCompiled}/{@code carvingCompiledListNum}
 * fields are still carried by {@link TileEntityJoint} and the joint parts (the mirror/sync code
 * resets them) but nothing consumes them here.
 *
 * <p>What remains is the reference's public shape: {@link #renderer} plus
 * {@link IBatchRenderer#compileOrRenderCarving}, which now defers to
 * {@link CarvingRenderer} on every call. {@code glBindVertexArray} (and its
 * {@code CommonProxyClient.cc.OpenGL30} / {@code ARBVertexArrayObject} probing) was VAO plumbing
 * for the display lists and is gone as well; the {@code OpenGL30} branch of {@code setup()} only
 * ever produced the same {@code ListBatchRenderer} in both arms.
 */
@OnlyIn(Dist.CLIENT)
public class BatchRenderHelper {

   /**
    * Never null: the 1.12.2 {@code BatchRenderHelper.setup()} init call is no longer required, but
    * a caller may still invoke it (see {@link #setup()}).
    */
   public static IBatchRenderer renderer = new ListBatchRenderer();

   /** Kept for the 1.12.2 client-init call site; the renderer is usable without it. */
   public static void setup() {
      renderer = new ListBatchRenderer();
   }

   public static class ListBatchRenderer implements IBatchRenderer {

      @Override
      public void compileOrRenderCarving(JointPartsRenderable parts, int[] paletteData, PoseStack pose,
                                         MultiBufferSource bufferSource, int packedLight) {
         CarvingRenderer.renderJointCarvedModelByType(parts, paletteData, pose, bufferSource, packedLight);
      }

      @Override
      public void compileOrRenderCarving(TileEntityJoint te, int[] paletteData, PoseStack pose,
                                         MultiBufferSource bufferSource, int packedLight) {
         CarvingRenderer.renderTeCarvedModelByType(te, paletteData, pose, bufferSource, packedLight);
      }
   }

   public interface IBatchRenderer {

      void compileOrRenderCarving(JointPartsRenderable parts, int[] paletteData, PoseStack pose,
                                  MultiBufferSource bufferSource, int packedLight);

      void compileOrRenderCarving(TileEntityJoint te, int[] paletteData, PoseStack pose,
                                  MultiBufferSource bufferSource, int packedLight);
   }
}
