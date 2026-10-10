package jp.mc.ancientred.jbrobot.proxy;

import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jbrobot.item.external.MultiLayeredModel;
import jp.mc.ancientred.jbrobot.model.technesample.ModelRenderError;
import jp.mc.ancientred.jbrobot.registry.JBRItems;
import jp.mc.ancientred.jbrobot.registry.JBRMenuTypes;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.item.IColorHanldlerItem;
import jp.mc.ancientred.jointblock.modelpack.ExJBModelInfo;
import jp.mc.ancientred.jointblock.modelpack.ExJBModelLayer;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jointblock.modelpack.JBRModelUtils;
import jp.mc.ancientred.jointblock.modelpack.ModelPackResources;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client wiring of the addon.
 *
 * <p>1.12.2 did this from {@code CommonProxyClient}: {@code registerResourcePack()} installed
 * {@code JBRFileResourcePack}, {@code registerPreRenderHelper()} registered the fake
 * {@code jbrobotexmodel} model loader and mesh definitions, and
 * {@code setupModelResoureceForClient}/{@code setupAddonItemResoureceForClient}/
 * {@code setupSoundItemResoureceForClient} parsed the {@code .obj}s, uploaded GL textures and read
 * the per-entry render flags.
 *
 * <p>1.20.1 needs none of the mesh/GL half: pack textures, language files and {@code sounds.json}
 * are served by {@link ModelPackResources} as an always-active resource pack, so only two things
 * remain - the pack registration itself and the per-model render models/render flags the joint block
 * renderer and the catalog GUI read.
 */
@Mod.EventBusSubscriber(modid = JointBlockRemastered.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JBRClientSetup {

    private JBRClientSetup() {
    }

    /**
     * Predicate of the three fixed items whose 1.12.2 model was chosen in code
     * ({@code CommonProxyClient.registerRenderHelper}: {@code ItemModelMesher.register(item, meta, model)});
     * the matching {@code overrides} live in {@code assets/jbrobot/models/item/<name>.json}.
     */
    private static final ResourceLocation META_PREDICATE = new ResourceLocation(JBRobotMODContainer.ASSET_NAMESPACE, "meta");

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || JBRobotMODContainer.importedExPackageList.isEmpty()) {
            return;
        }

        event.addRepositorySource(consumer -> {
            ModelPackResources resources = new ModelPackResources(JBRobotMODContainer.importedExPackageList);
            Pack pack = Pack.readMetaAndCreate(ModelPackResources.PACK_ID, Component.literal("JointBlock model packs"),
                    true, id -> resources, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.DEFAULT);
            if (pack != null) {
                consumer.accept(pack);
            }
        });

        registerPackEntryPredicates();
        registerFixedItemPredicates();
    }

    private static void registerFixedItemPredicates() {
        ItemProperties.register(JBRItems.ModelHandItem.get(), META_PREDICATE,
                (stack, level, entity, seed) -> stack.getDamageValue());
        ItemProperties.register(JBRItems.ModelBazookaHeadPartsItem.get(), META_PREDICATE,
                (stack, level, entity, seed) -> stack.getDamageValue());
        ItemProperties.register(JBRItems.ModelBladeTopItem.get(), META_PREDICATE,
                (stack, level, entity, seed) -> stack.getDamageValue());
    }

    /**
     * 1.12.2 {@code CommonProxyClient.registerRenderHelper}: {@code ItemColors.register} for
     * {@code BasicAddonItem} (its tint-0/tint-1 tables live in {@code ItemBasicAddon}).
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> IColorHanldlerItem.safeCast(stack.getItem()).getColorFromItemStack(stack, tintIndex),
                JBRItems.BasicAddonItem.get());
    }

    /**
     * The synthesized item models select a pack entry with the {@code jbrobot:pack_entry} predicate
     * (the 1.12.2 {@code ItemOverrideList} did the same job by item damage).
     */
    private static void registerPackEntryPredicates() {
        for (ExtraPackage pack : JBRobotMODContainer.importedExPackageList) {
            Item[] items = {
                    JBRobotMODContainer.getModelItem(pack),
                    JBRobotMODContainer.getAddonItem(pack),
                    JBRobotMODContainer.getSoundItem(pack),
            };
            for (Item item : items) {
                if (item != null) {
                    ItemProperties.register(item, ModelPackResources.PACK_ENTRY_PREDICATE,
                            (stack, level, entity, seed) -> stack.getDamageValue());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 1.12.2 built the screen from the container instance in the addon's client IGuiHandler.
        JBRMenuTypes.registerScreens();
        event.enqueueWork(JBRClientSetup::readEntryRenderFlags);
    }

    /**
     * 1.12.2 {@code CommonProxyClient#setupModelResoureceForClient}: built each model's render model
     * and read the catalog render flags. The addon/sound item halves of that pass only copied
     * {@code itemIconFile} into the (1.20.1 unused) icon field, which {@link ModelPackResources}
     * now reads from the entry properties itself.
     */
    private static void readEntryRenderFlags() {
        for (ExtraPackage pack : JBRobotMODContainer.importedExPackageList) {
            for (int i = 0; i < pack.modelNums; ++i) {
                ExJBModelInfo info = pack.modelsLoaded[pack.modelsIndexCompressed[i]];
                if (info == null) {
                    continue;
                }

                info.model = buildClientModel(info);
                info.renderType = 0;
                info.renderExpand = 0.0;
                info.catalogScale = 1.0F;
                if (info.properties != null) {
                    if (info.properties.has("renderType")) {
                        int renderType = JBRModelUtils.getIntegerFromJson(info.properties, "renderType");
                        if (renderType >= 0 && renderType < 3) {
                            info.renderType = renderType;
                        }
                    }

                    if (info.properties.has("renderExpand")) {
                        int renderExpand = JBRModelUtils.getIntegerFromJson(info.properties, "renderExpand");
                        if (renderExpand > 0) {
                            info.renderExpand = renderExpand;
                        }
                    }

                    if (info.properties.has("catalogScale")) {
                        float catalogScale = JBRModelUtils.getFloatFromJson(info.properties, "catalogScale");
                        if (catalogScale > 0.0F) {
                            info.catalogScale = catalogScale;
                        }
                    }
                }
            }
        }
    }

    /**
     * 1.12.2 resolved every layer's {@code .obj} and texture zip entry up front and fell back to
     * {@code ModelRenderError.instanceError} when one of them was missing. 1.20.1 parses the
     * {@code .obj} lazily ({@code MultiLayeredModel#ensureLayerLoaded}), so only the entries are
     * resolved here - the same check 1.12.2 made before reading them.
     */
    private static IJBModelItem.IJBModel buildClientModel(ExJBModelInfo info) {
        ExtraPackage pack = info.parent;
        ExJBModelLayer[] layers = info.modelLayers;
        if (pack == null || pack.fileLoader == null || layers == null || layers.length == 0) {
            return ModelRenderError.instanceError;
        }

        for (ExJBModelLayer layer : layers) {
            if (JBRModelUtils.isInvalidFileLoaderEntiry(pack.getEntryIgnoreCase(layer.modelFileName))
                    || JBRModelUtils.isInvalidFileLoaderEntiry(pack.getEntryIgnoreCase(layer.textureFileName))) {
                return ModelRenderError.instanceError;
            }
        }

        return new MultiLayeredModel(info);
    }
}
