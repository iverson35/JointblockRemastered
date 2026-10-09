package jp.mc.ancientred.jointblock.item.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

/**
 * 1.20.1 port of the 1.12.2 {@code jbsubiconmodel} custom model loader.
 *
 * <p>In 1.12.2 this class implemented Forge's {@code ICustomModelLoader} and intercepted the fake
 * {@code jbsubiconmodel} resource domain: {@code ModelLoaderRegistry} asked {@code accepts()}, and
 * for {@code jbsubiconmodel:subiconmodel/<name>} {@code loadModel()} returned
 * {@code new SubIconItemModel(new ResourceLocation("jointblock:item/<name>"))}. Those classes
 * ({@code ICustomModelLoader}, {@code ModelLoaderRegistry.registerLoader},
 * {@code IModel}/{@code IModelState}) no longer exist in 1.20.1.
 *
 * <p><b>1.20.1 registration path.</b> This is an
 * {@link IGeometryLoader} referenced from a model JSON by the {@code "loader"} key. The client
 * wiring (a client-side mod-event handler, not this class) must register it on the
 * <em>mod</em> event bus:
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
 *     event.register(SubIconItemModelLoader.SUBICON_MODEL_DOMAIN, SubIconItemModelLoader.INSTANCE);
 * }
 * // Forge's RegisterGeometryLoaders.register(String path, IGeometryLoader<?>) prefixes the active
 * // mod namespace, so the registered id is "jointblock:jbsubiconmodel".
 * }</pre>
 *
 * <p><b>Model JSON.</b> A sub icon model is a normal item model JSON that names its loader plus the
 * model whose quads become the small inset icon; the base (pattern cutter) model is fixed by
 * {@link SubIconItemModel#MODEL_BASE}. All three of these are equivalent, the first two being the
 * idiomatic 1.20.1 spellings:
 * <pre>{@code
 * // assets/jointblock/models/item/subiconmodel/jbmold.json
 * { "loader": "jointblock:jbsubiconmodel", "parent": "jointblock:item/jbmold" }
 * // or, avoiding the vanilla parent chain entirely:
 * { "loader": "jointblock:jbsubiconmodel", "submodel": "jointblock:item/jbmold" }
 * // or, exactly like 1.12.2, where the fake path named the icon model:
 * //   jbsubiconmodel:subiconmodel/jbmold -> modj_jointblock:item/jbmold
 * { "loader": "jointblock:jbsubiconmodel" }
 * }</pre>
 * The third form is resolved from this model's own location by
 * {@link #getSubModelLocation(ResourceLocation)}: any model whose id starts with
 * {@code item/subiconmodel/} takes its icon from {@code <same namespace>:item/<rest>}.
 *
 * <p>Note when using the {@code "parent"} form: the parent must be the icon model, never this file
 * itself - a self-reference makes {@code ModelBaker} fail on its circular dependency check.
 *
 * <p>What could not be carried over: {@code IResourceManager}/{@code onResourceManagerReload}
 * (1.12.2's {@code func_110549_a}) - 1.20.1 model bakers are recreated per resource reload, so the
 * loader itself is stateless.
 */
public class SubIconItemModelLoader implements IGeometryLoader<SubIconItemModel> {
   /** The 1.12.2 fake resource domain this loader intercepted; also the {@code "loader"} path to register. */
   public static final String SUBICON_MODEL_DOMAIN = "jbsubiconmodel";

   /**
    * The 1.20.1 model-id equivalent of the 1.12.2 {@code models/item/subiconmodel/} resource path:
    * models under it take their sub icon from {@code <same namespace>:item/<rest>}.
    */
   public static final String SUBICON_MODEL_PATH = "item/subiconmodel/";

   /** Stateless; the client wiring registers this single instance. */
   public static final SubIconItemModelLoader INSTANCE = new SubIconItemModelLoader();

   /**
    * Reads the sub icon model location out of the model JSON: {@code "submodel"} first, then the
    * vanilla {@code "parent"}. Null means "derive it from this model's own location" (1.12.2 path
    * rule); {@link SubIconItemModel#bake} then falls back to the missing model if even that yields
    * nothing.
    */
   @Override
   public SubIconItemModel read(JsonObject json, JsonDeserializationContext context) {
      ResourceLocation subModelLoc = null;
      if (json.has("submodel")) {
         subModelLoc = new ResourceLocation(GsonHelper.getAsString(json, "submodel"));
      } else if (json.has("parent")) {
         subModelLoc = new ResourceLocation(GsonHelper.getAsString(json, "parent"));
      }

      return new SubIconItemModel(subModelLoc);
   }

   /** 1.12.2 {@code accepts}: is this model location handled by the {@code jbsubiconmodel} loader? */
   public static boolean accepts(ResourceLocation modelLocation) {
      return modelLocation.getPath().startsWith(SUBICON_MODEL_PATH);
   }

   /**
    * 1.12.2 {@code loadModel}: strips the {@code models/item/subiconmodel/} prefix and returns the
    * actual item model that provides the sub icon. 1.12.2 hard-coded the {@code modj_jointblock}
    * namespace because the source location lived in the fake {@code jbsubiconmodel} domain; the
    * 1.20.1 port keeps the namespace of the source location instead.
    *
    * @return the sub icon model location, or null if the location is not handled by this loader.
    */
   public static ResourceLocation getSubModelLocation(ResourceLocation modelLocation) {
      if (!accepts(modelLocation)) {
         return null;
      }

      String modelName = modelLocation.getPath().substring(SUBICON_MODEL_PATH.length());
      if (modelName.isEmpty()) {
         return null;
      }

      return new ResourceLocation(modelLocation.getNamespace(), "item/" + modelName);
   }
}
