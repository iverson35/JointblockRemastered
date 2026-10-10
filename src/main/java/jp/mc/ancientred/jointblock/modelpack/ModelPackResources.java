package jp.mc.ancientred.jointblock.modelpack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonItemInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSound;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundItemInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * The always-active client resource pack that exposes every loaded pack zip plus the JSONs 1.20.1
 * needs but the 1.12.2 format never had.
 *
 * <p>1.12.2 shipped {@code jp.mc.ancientred.jbrobot.resouce.JBRFileResourcePack}: an
 * {@code IResourcePack} serving zip entries under {@code jbrextrapackages:<packageId>:<path>}, with
 * GL textures and language maps built by hand. 1.20.1 resolves textures, language files and
 * {@code sounds.json} through the {@code ResourceManager} instead, so this pack serves:
 * <ul>
 *   <li>{@code jbrextrapackages:<packageId>/<zip entry>} - raw zip passthrough (textures, .obj, ...);</li>
 *   <li>{@code jbrextrapackages:textures/item/<packageId>/<icon>.png} - the pack's
 *       {@code itemIconFile}s again, under the blocks atlas' {@code item/} source, so item models
 *       can address them as sprites ({@link #ICON_ALIAS_PREFIX});</li>
 *   <li>{@code jbrextrapackages:lang/<code>.json} - the packs' {@code lang/*.lang|json}, merged;</li>
 *   <li>{@code jbrextrapackages:models/<packageId>/model_<id>.json} - one flat item model per pack
 *       model, so the pack's model item shows its own icon in inventories and in the catalog;</li>
 *   <li>{@code jbrextrapackages:sounds.json} - the packs' sound entries;</li>
 *   <li>{@code jointblock:models/item/<m_|a_|s_><packageId>.json} - the item models of the pack's
 *       model/addon/sound items, with one {@code jointblock:pack_entry} override per entry id.</li>
 * </ul>
 *
 * <p>The pack's zips stay open for the lifetime of the game: pack models are parsed lazily while
 * rendering, so {@link #close()} deliberately does not close them (1.12.2 did not either).
 */
public class ModelPackResources extends AbstractPackResources {
    public static final String PACK_ID = "jointblock_model_packs";

    /** 1.20.1 resource pack format. */
    private static final int PACK_FORMAT = 15;

    /** Namespace the addon registers its items in; its assets and lang keys live under it. */
    public static final String ITEM_NAMESPACE = "jbrobot";

    /** Item predicate the synthesized item models use to select a pack entry by item damage. */
    public static final ResourceLocation PACK_ENTRY_PREDICATE =
            new ResourceLocation(ITEM_NAMESPACE, "pack_entry");

    private static final String NS = ExtraPackage.EXTRA_PACKAGE_ROOT_DOMAIN;
    private static final String ITEM_PREFIX = "models/item/";

    /**
     * The pack's {@code itemIconFile}s are served a second time under this path: 1.20.1 item models
     * can only address sprites of the blocks atlas, and the only directory sources of that atlas a
     * pack can join are {@code textures/block} and {@code textures/item} (the vanilla source list
     * stitches {@code textures/item/**} of every namespace under the {@code item/} prefix).
     */
    private static final String ICON_ALIAS_PREFIX = "textures/item/";

    private static final Gson GSON = new Gson();

    private final List<ExtraPackage> packs;
    private final Map<String, ExtraPackage> packById = new LinkedHashMap<>();
    private final byte[] packMeta;

    public ModelPackResources(List<ExtraPackage> packs) {
        super(PACK_ID, false);
        this.packs = packs;
        for (ExtraPackage pack : packs) {
            this.packById.put(pack.packageId.toLowerCase(Locale.ROOT), pack);
        }

        this.packMeta = buildPackMeta(packs);
    }

    public List<ExtraPackage> getPacks() {
        return this.packs;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return () -> new ByteArrayInputStream(this.packMeta);
        }

        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != PackType.CLIENT_RESOURCES) {
            return null;
        }

        byte[] generated = generate(location);
        if (generated != null) {
            return () -> new ByteArrayInputStream(generated);
        }

        ExtraPackage pack = zipPackFor(location);
        if (pack == null) {
            return null;
        }

        String entryName = zipEntryFor(pack, location);
        FileLoader.FileEntry entry = entryName == null ? null : pack.getEntryIgnoreCase(entryName);
        if (entry == null) {
            return null;
        }

        return () -> pack.fileLoader.getInputStream(entry);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
        if (packType != PackType.CLIENT_RESOURCES || !NS.equals(namespace)) {
            return;
        }

        for (ExtraPackage pack : this.packs) {
            String id = pack.packageId.toLowerCase(Locale.ROOT);
            String prefix = id + "/";
            if (path.startsWith(prefix) && !path.startsWith("sounds/" + prefix)) {
                String sub = path.substring(prefix.length());
                for (java.util.Enumeration<FileLoader.FileEntry> e = pack.fileLoader.entries(); e.hasMoreElements(); ) {
                    FileLoader.FileEntry entry = e.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }

                    String lower = entry.getName().toLowerCase(Locale.ROOT);
                    if (lower.startsWith(sub)) {
                        ResourceLocation location = new ResourceLocation(NS, prefix + lower);
                        output.accept(location, () -> pack.fileLoader.getInputStream(entry));
                    }
                }
            }

            for (ResourceLocation generated : generatedLocations(pack)) {
                String generatedPath = generated.getPath();
                if (generatedPath.startsWith(path)) {
                    output.accept(generated, () -> new ByteArrayInputStream(generate(generated)));
                }
            }

            for (String iconFile : iconFileNames(pack)) {
                ResourceLocation alias = iconAliasLocation(pack, iconFile);
                if (!alias.getPath().startsWith(path)) {
                    continue;
                }

                String entryName = zipEntryFor(pack, alias);
                FileLoader.FileEntry entry = entryName == null ? null : pack.getEntryIgnoreCase(entryName);
                if (entry != null) {
                    output.accept(alias, () -> pack.fileLoader.getInputStream(entry));
                }
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        Set<String> namespaces = new LinkedHashSet<>();
        namespaces.add(NS);
        namespaces.add(ITEM_NAMESPACE);
        return namespaces;
    }

    @Override
    public void close() {
        // Pack models are parsed from the zip streams lazily while rendering; the 1.12.2 loader also
        // kept its FileLoaders open for the whole game.
    }

    /** 1.20.1 asks for {@code pack.mcmeta} up front; the packs' language codes are listed here. */
    private static byte[] buildPackMeta(List<ExtraPackage> packs) {
        JsonObject language = new JsonObject();
        Set<String> codes = new LinkedHashSet<>();
        codes.add("en_us");
        for (ExtraPackage pack : packs) {
            if (pack.langMap != null) {
                codes.addAll(pack.langMap.keySet());
            }
        }

        for (String code : codes) {
            JsonObject info = new JsonObject();
            info.addProperty("name", code);
            // The vanilla language serializer rejects an empty region, so packs without one
            // still need a placeholder.
            info.addProperty("region", "US");
            info.addProperty("bidirectional", false);
            language.add(code, info);
        }

        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", PACK_FORMAT);
        pack.addProperty("description", "JointBlock external model packs");

        JsonObject root = new JsonObject();
        root.add("pack", pack);
        root.add("language", language);
        return GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
    }

    /** Every JSON this pack invents for a package (used by {@link #listResources}). */
    private List<ResourceLocation> generatedLocations(ExtraPackage pack) {
        List<ResourceLocation> locations = new ArrayList<>();
        String id = pack.packageId.toLowerCase(Locale.ROOT);
        locations.add(new ResourceLocation(NS, "sounds.json"));
        if (pack.langMap != null) {
            for (String code : pack.langMap.keySet()) {
                locations.add(new ResourceLocation(NS, "lang/" + code + ".json"));
            }
        }

        for (int i = 0; i < pack.modelNums; ++i) {
            locations.add(new ResourceLocation(NS, "models/" + id + "/m_" + pack.modelsIndexCompressed[i] + ".json"));
        }

        for (int i = 0; i < pack.addonItemNums; ++i) {
            locations.add(new ResourceLocation(NS, "models/" + id + "/a_" + pack.addonItemsIndexCompressed[i] + ".json"));
        }

        for (int i = 0; i < pack.soundItemNums; ++i) {
            locations.add(new ResourceLocation(NS, "models/" + id + "/s_" + pack.soundItemsIndexCompressed[i] + ".json"));
        }

        locations.add(new ResourceLocation(ITEM_NAMESPACE, ITEM_PREFIX + "m_" + id + ".json"));
        locations.add(new ResourceLocation(ITEM_NAMESPACE, ITEM_PREFIX + "a_" + id + ".json"));
        locations.add(new ResourceLocation(ITEM_NAMESPACE, ITEM_PREFIX + "s_" + id + ".json"));
        return locations;
    }

    /** The JSONs that exist only in 1.20.1; {@code null} when the location is a plain zip entry. */
    private byte[] generate(ResourceLocation location) {
        if (NS.equals(location.getNamespace())) {
            String path = location.getPath();
            if (path.startsWith("lang/") && path.endsWith(".json")) {
                return generateLang(path.substring("lang/".length(), path.length() - ".json".length()));
            }

            if ("sounds.json".equals(path)) {
                return generateSounds();
            }

            if (path.startsWith("models/") && path.endsWith(".json")) {
                return generateEntryModel(path.substring("models/".length(), path.length() - ".json".length()));
            }

            return null;
        }

        if (ITEM_NAMESPACE.equals(location.getNamespace()) && location.getPath().startsWith(ITEM_PREFIX)
                && location.getPath().endsWith(".json")) {
            String itemName = location.getPath().substring(ITEM_PREFIX.length(), location.getPath().length() - ".json".length());
            return generateItemModel(itemName);
        }

        return null;
    }

    /** {@code lang/<code>.json}: every pack's map for that code, later packs winning. */
    private byte[] generateLang(String code) {
        JsonObject merged = new JsonObject();
        for (ExtraPackage pack : this.packs) {
            if (pack.langMap == null) {
                continue;
            }

            Map<String, String> map = pack.langMap.get(code.toLowerCase(Locale.ROOT));
            if (map == null) {
                continue;
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                merged.addProperty(entry.getKey(), entry.getValue());
            }
        }

        return GSON.toJson(merged).getBytes(StandardCharsets.UTF_8);
    }

    /** {@code models/<packageId>/<m_|a_|s_><id>.json}: a flat icon model for one pack entry. */
    private byte[] generateEntryModel(String path) {
        int slash = path.indexOf('/');
        if (slash <= 0) {
            return null;
        }

        ExtraPackage pack = this.packById.get(path.substring(0, slash));
        if (pack == null) {
            return null;
        }

        String name = path.substring(slash + 1);
        if (name.length() < 3 || name.charAt(1) != '_') {
            return null;
        }

        int entryId;
        try {
            entryId = Integer.parseInt(name.substring(2));
        } catch (NumberFormatException e) {
            return null;
        }

        String icon = null;
        switch (name.charAt(0)) {
            case 'm':
                ExJBModelInfo modelInfo = pack.getLoadedModelInfoFromItemDamage(entryId);
                icon = modelInfo != null ? iconOf(pack, modelInfo.properties) : null;
                break;
            case 'a':
                jp.mc.ancientred.jbrobot.item.external.ExJBAddonItemInfo addonInfo = pack.getLoadedAddonItemInfoFromItemDamage(entryId);
                icon = addonInfo != null ? iconOf(pack, addonInfo.properties) : null;
                break;
            case 's':
                jp.mc.ancientred.jbrobot.item.external.ExJBSoundItemInfo soundInfo = pack.getLoadedSoundItemInfoFromItemDamage(entryId);
                icon = soundInfo != null ? iconOf(pack, soundInfo.properties) : null;
                break;
            default:
                return null;
        }

        return GSON.toJson(iconModel(icon != null ? icon : packageIcon(pack))).getBytes(StandardCharsets.UTF_8);
    }

    /** {@code models/item/<name>.json}: the pack item's icon plus one override per entry id. */
    private byte[] generateItemModel(String itemName) {
        for (ExtraPackage pack : this.packs) {
            String id = pack.packageId.toLowerCase(Locale.ROOT);
            char kind;
            if (itemName.equals("m_" + id)) {
                kind = 'm';
            } else if (itemName.equals("a_" + id)) {
                kind = 'a';
            } else if (itemName.equals("s_" + id)) {
                kind = 's';
            } else {
                continue;
            }

            JsonObject root = iconModel(packageIcon(pack));
            JsonArray overrides = new JsonArray();
            int entries = kind == 'm' ? pack.modelNums : kind == 'a' ? pack.addonItemNums : pack.soundItemNums;
            int[] ids = kind == 'm' ? pack.modelsIndexCompressed
                    : kind == 'a' ? pack.addonItemsIndexCompressed : pack.soundItemsIndexCompressed;
            for (int i = 0; i < entries; ++i) {
                overrides.add(override(ids[i], "jbrextrapackages:" + id + "/" + kind + "_" + ids[i]));
            }

            if (overrides.size() > 0) {
                root.add("overrides", overrides);
            }

            return GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
        }

        return null;
    }

    /**
     * {@code sounds.json}: 1.12.2 registered a {@code SoundEventAccessor} per pack sound by hand;
     * 1.20.1 plays pack sounds through {@code SoundEvent.createVariableRangeEvent} of the same
     * names, so the pack only has to publish the file list here.
     */
    private byte[] generateSounds() {
        JsonObject root = new JsonObject();
        for (ExtraPackage pack : this.packs) {
            String id = pack.packageId.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, ExJBSoundInfo> entry : pack.soundMap.entrySet()) {
                ExJBSoundInfo info = entry.getValue();
                JsonArray sounds = new JsonArray();
                for (ExJBSound sound : info.soundFileList) {
                    JsonObject one = new JsonObject();
                    // A name without a namespace would resolve to minecraft:sounds/... - the pack
                    // serves its sound files under its own namespace (see zipEntryFor).
                    one.addProperty("name", NS + ":" + id + "/"
                            + stripExtension(sound.soundlFileName).toLowerCase(Locale.ROOT));
                    one.addProperty("stream", false);
                    one.addProperty("volume", sound.volume);
                    one.addProperty("pitch", sound.pitch);
                    one.addProperty("weight", sound.weight);
                    sounds.add(one);
                }

                JsonObject event = new JsonObject();
                event.add("sounds", sounds);
                root.add(id + "/" + entry.getKey().toLowerCase(Locale.ROOT), event);
            }
        }

        return GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject override(int entryId, String model) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty(PACK_ENTRY_PREDICATE.toString(), entryId);

        JsonObject override = new JsonObject();
        override.add("predicate", predicate);
        override.addProperty("model", model);
        return override;
    }

    private static JsonObject iconModel(String iconLocation) {
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", iconLocation != null ? iconLocation : "minecraft:missingno");

        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        root.add("textures", textures);
        return root;
    }

    /** The icon of one entry: its own {@code itemIconFile}, else the package icon. */
    private static String iconOf(ExtraPackage pack, JsonObject properties) {
        if (properties != null && properties.has("itemIconFile")) {
            String icon = properties.get("itemIconFile").getAsString();
            if (icon != null && !icon.isEmpty()) {
                return iconSprite(pack, icon);
            }
        }

        return packageIcon(pack);
    }

    private static String packageIcon(ExtraPackage pack) {
        return pack.packageIconFileName != null && !pack.packageIconFileName.isEmpty()
                ? iconSprite(pack, pack.packageIconFileName)
                : null;
    }

    /** Every icon file a pack's item models can reference, in pack order and without duplicates. */
    private static Set<String> iconFileNames(ExtraPackage pack) {
        Set<String> icons = new LinkedHashSet<>();
        for (int i = 0; i < pack.modelNums; ++i) {
            ExJBModelInfo info = pack.modelsLoaded[pack.modelsIndexCompressed[i]];
            addIconFileName(icons, info != null ? info.properties : null);
        }

        for (int i = 0; i < pack.addonItemNums; ++i) {
            ExJBAddonItemInfo info = pack.addonItemsLoaded[pack.addonItemsIndexCompressed[i]];
            addIconFileName(icons, info != null ? info.properties : null);
        }

        for (int i = 0; i < pack.soundItemNums; ++i) {
            ExJBSoundItemInfo info = pack.soundItemsLoaded[pack.soundItemsIndexCompressed[i]];
            addIconFileName(icons, info != null ? info.properties : null);
        }

        if (pack.packageIconFileName != null && !pack.packageIconFileName.isEmpty()) {
            icons.add(pack.packageIconFileName);
        }

        return icons;
    }

    private static void addIconFileName(Set<String> icons, JsonObject properties) {
        if (properties != null && properties.has("itemIconFile")) {
            String icon = properties.get("itemIconFile").getAsString();
            if (icon != null && !icon.isEmpty()) {
                icons.add(icon);
            }
        }
    }

    /** The blocks atlas sprite name of a pack icon; {@link #iconAliasLocation} is the file behind it. */
    private static String iconSprite(ExtraPackage pack, String iconFileName) {
        return new ResourceLocation(NS, "item/" + iconAliasPath(pack, iconFileName)).toString();
    }

    private static ResourceLocation iconAliasLocation(ExtraPackage pack, String iconFileName) {
        return new ResourceLocation(NS, ICON_ALIAS_PREFIX + iconAliasPath(pack, iconFileName) + ".png");
    }

    /** {@code <packageId>/<icon file without its extension>}, both lower cased as resource paths are. */
    private static String iconAliasPath(ExtraPackage pack, String iconFileName) {
        return pack.packageId.toLowerCase(Locale.ROOT) + "/" + stripExtension(iconFileName).toLowerCase(Locale.ROOT);
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private ExtraPackage zipPackFor(ResourceLocation location) {
        if (!NS.equals(location.getNamespace())) {
            return null;
        }

        String path = location.getPath();
        if (path.startsWith(ICON_ALIAS_PREFIX)) {
            path = path.substring(ICON_ALIAS_PREFIX.length());
        }

        if (path.startsWith("sounds/")) {
            path = path.substring("sounds/".length());
        }

        int slash = path.indexOf('/');
        return slash <= 0 ? null : this.packById.get(path.substring(0, slash));
    }

    /**
     * Maps a location back to a zip entry: {@code <packageId>/<entry>} for ordinary resources,
     * {@code sounds/<packageId>/<entry>.ogg} for sounds (1.20.1 only reads sound files from
     * {@code sounds/}) and {@code textures/item/<packageId>/<entry>.png} for the icon alias.
     */
    private String zipEntryFor(ExtraPackage pack, ResourceLocation location) {
        String id = pack.packageId.toLowerCase(Locale.ROOT);
        String path = location.getPath();
        if (path.startsWith(ICON_ALIAS_PREFIX)) {
            path = path.substring(ICON_ALIAS_PREFIX.length());
        }

        if (path.startsWith("sounds/")) {
            String prefix = "sounds/" + id + "/";
            return path.startsWith(prefix) ? path.substring(prefix.length()) : null;
        }

        String prefix = id + "/";
        return path.startsWith(prefix) ? path.substring(prefix.length()) : null;
    }
}
