package jp.mc.ancientred.jointblock.item.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.slf4j.Logger;

/**
 * 1.20.1 port of the 1.12.2 {@code SubIconItemModel}, the unbaked model behind the
 * {@code jbsubiconmodel} loader.
 *
 * <p>The 1.12.2 class implemented Forge's {@code IModel}. 1.20.1 replaces that interface with
 * {@link IUnbakedGeometry} + {@code IGeometryLoader}: the model JSON declares
 * {@code "loader": "jointblock:jbsubiconmodel"} and Forge hands the deserialized geometry to
 * {@link SubIconItemModelLoader#INSTANCE} (see that class for the exact registration call).
 * {@code IModel.getDependencies()}/{@code getTextures()}/{@code getDefaultState()} have no
 * 1.20.1 counterpart: dependencies are resolved by
 * {@link IUnbakedGeometry#resolveParents} (default no-op here - both models are ordinary
 * top-level models that {@link ModelBaker} can bake on demand), textures come from each model
 * itself, and the model state is passed straight through from the caller instead of being
 * spawned from {@code TRSRTransformation.identity()}.
 *
 * <p>Behaviour is the 1.12.2 behaviour: bake {@link #MODEL_BASE} (the pattern cutter item model,
 * {@code modj_jointblock:item/patterncutter} in 1.12.2) plus the sub icon model, rebake the sub
 * icon quads into the upper-right quadrant by {@code 0.5 * v + 0.5} on x/y and {@code v + 0.001}
 * on z, and wrap both quad sets in a {@link BakedSubIconItemModel}. The sub icon model location is
 * taken from, in order: the {@code "submodel"} JSON field, the {@code "parent"} JSON field, or -
 * when neither is present - derived from this model's own location through the 1.12.2
 * {@code models/item/subiconmodel/<name> -> modj_jointblock:item/<name>} path rule
 * ({@link SubIconItemModelLoader#getSubModelLocation}). If nothing resolves, or if any bake throws,
 * the 1.12.2 code fell back to the missing model; this port does the same and logs the failure.
 *
 * <p>Vertex rewriting note: 1.12.2 used {@code quad.getFormat().func_181719_f()} as the vertex
 * stride. 1.20.1 {@link BakedQuad} is always {@link DefaultVertexFormat#BLOCK} (POSITION, COLOR,
 * UV0, UV2, NORMAL, PADDING = 8 ints / 32 bytes per vertex), whose first three ints are still the
 * position floats, so {@code DefaultVertexFormat.BLOCK.getIntegerSize()} is used as the stride.
 */
public class SubIconItemModel implements IUnbakedGeometry<SubIconItemModel> {
   private static final Logger LOGGER = LogUtils.getLogger();

   /** 1.12.2 {@code MODEL_BASE}: the item model whose quads the sub icon is merged into. */
   public static final ResourceLocation MODEL_BASE = new ResourceLocation("jointblock", "item/patterncutter");

   /** Explicit sub icon model location, or null to derive it from this model's own location. */
   public final ResourceLocation subModelLoc;

   public SubIconItemModel(ResourceLocation argSubModelLoc) {
      this.subModelLoc = argSubModelLoc;
   }

   @Override
   public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
      ResourceLocation subLoc = this.subModelLoc != null ? this.subModelLoc : SubIconItemModelLoader.getSubModelLocation(modelLocation);
      if (subLoc == null) {
         LOGGER.error("Sub icon item model {} has no sub icon model; using the missing model", modelLocation);
         return baker.bake(ModelBakery.MISSING_MODEL_LOCATION, modelState);
      }

      try {
         BakedModel bakedModelBase = baker.bake(MODEL_BASE, modelState);
         BakedModel bakedModelSub = baker.bake(subLoc, modelState);
         List<BakedQuad> subBqListOrg = bakedModelSub.getQuads(null, null, RandomSource.create(0L));
         List<BakedQuad> subBqListNew = new ArrayList<>(subBqListOrg.size());

         for (BakedQuad bq : subBqListOrg) {
            int[] transformed = this.transformQuads(bq.getVertices());
            subBqListNew.add(new BakedQuad(transformed, bq.getTintIndex(), bq.getDirection(), bq.getSprite(), bq.isShade(), bq.hasAmbientOcclusion()));
         }

         return new BakedSubIconItemModel(bakedModelBase, subBqListNew);
      } catch (Exception e) {
         LOGGER.error("Failed to bake sub icon item model {} (sub icon {}); using the missing model", modelLocation, subLoc, e);
         return baker.bake(ModelBakery.MISSING_MODEL_LOCATION, modelState);
      }
   }

   /**
    * 1.12.2 {@code transformQuads}: scale the sub icon vertex by 0.5 and shift it by 0.5 in x/y
    * (into the upper-right quadrant of the item space) and lift z by 0.001 so the icon never
    * z-fights with the base model. The vertex element order is not touched, so tint, UVs, lighting
    * and normal of every vertex are preserved.
    */
   private int[] transformQuads(int[] vertexDataOrg) {
      int vertexSize = DefaultVertexFormat.BLOCK.getIntegerSize();
      int[] vertexDataNew = new int[vertexDataOrg.length];
      System.arraycopy(vertexDataOrg, 0, vertexDataNew, 0, vertexDataOrg.length);

      for (int i = 0; i < 4; ++i) {
         int baseIdx = i * vertexSize;
         vertexDataNew[baseIdx + 0] = Float.floatToIntBits(Float.intBitsToFloat(vertexDataNew[baseIdx + 0]) * 0.5F + 0.5F);
         vertexDataNew[baseIdx + 1] = Float.floatToIntBits(Float.intBitsToFloat(vertexDataNew[baseIdx + 1]) * 0.5F + 0.5F);
         vertexDataNew[baseIdx + 2] = Float.floatToIntBits(Float.intBitsToFloat(vertexDataNew[baseIdx + 2]) + 0.001F);
      }

      return vertexDataNew;
   }
}
