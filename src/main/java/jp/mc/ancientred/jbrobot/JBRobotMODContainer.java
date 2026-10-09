package jp.mc.ancientred.jbrobot;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jbrobot.item.external.ItemImportedAddon;
import jp.mc.ancientred.jbrobot.item.external.ItemImportedModel;
import jp.mc.ancientred.jbrobot.item.external.ItemImportedSound;
import jp.mc.ancientred.jbrobot.item.merchant.DefaultModelMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBBlueprintFactory;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.api.IJBSoundManager;
import jp.mc.ancientred.jointblock.export.JBBlueprintFactory;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackageLoader;
import jp.mc.ancientred.jointblock.modelpack.ModelPackResources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The addon's static context: the loaded external packs and the items that hand them out.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.JBRobotMODContainer} was a second {@code @Mod}
 * ({@code modj_jbrobot}) that scanned {@code <config>/JointBlock/*.zip} during preInit, registered
 * one item per pack, and synced the pack list to clients for the catalog GUI. The port keeps the
 * same class and static members but runs from the single {@code jointblock} mod: {@link #init} is
 * called from the mod constructor (items must exist before the registry events fire), and
 * {@link #onCommonSetup} resolves the registered items back into the packs.
 *
 * <p>The 1.12.2 {@code CommonProxy*}/{@code JBRPacketHandler*}/{@code CommonForgeEventHandler}
 * classes are not ported one to one: their render helpers are obsolete in 1.20.1 (textures and
 * language come from {@link ModelPackResources}), their GUI handler is the port's
 * {@code JBGuiOpener} (id {@link #guiModelCatalogId}), and their network channel is
 * {@code jp.mc.ancientred.jbrobot.packet.JBRNetwork}.
 */
public final class JBRobotMODContainer {
    public static final String MODID_LC = "modj_jbrobot";

    /** Registry namespace of the addon's items; its assets and lang keys live under it too. */
    public static final String ASSET_NAMESPACE = ModelPackResources.ITEM_NAMESPACE;

    /** {@code <config dir>/JointBlock}, the folder the 1.12.2 addon scanned for pack zips. */
    public static final String PACK_FOLDER_NAME = "JointBlock";

    /** 1.12.2 {@code JBRobotMODContainer.guiModelCatalogId}. */
    public static final int guiModelCatalogId = 5596;

    public static ArrayList<ExtraPackage> importedExPackageList = new ArrayList<>();
    public static Hashtable<String, ExtraPackage> importedExPackageMap = new Hashtable<>();
    public static ArrayList<IJBRMerchantPackage> syncedExPackageListServer = new ArrayList<>();
    public static ArrayList<IJBRMerchantPackage> syncedExPackageListClient = new ArrayList<>();

    public static IJBBlueprintFactory blueprintFactory = new JBBlueprintFactory();
    public static IJBSoundManager soundManager;

    /** One item per pack, in the 1.12.2 names ({@code m_}/{@code a_}/{@code s_} + package id). */
    public static final DeferredRegister<Item> PACK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ASSET_NAMESPACE);

    private static final Map<ExtraPackage, RegistryObject<Item>> MODEL_ITEMS = new LinkedHashMap<>();
    private static final Map<ExtraPackage, RegistryObject<Item>> ADDON_ITEMS = new LinkedHashMap<>();
    private static final Map<ExtraPackage, RegistryObject<Item>> SOUND_ITEMS = new LinkedHashMap<>();

    private JBRobotMODContainer() {
    }

    /** Scans the pack folder and registers the per-pack items; call from the mod constructor. */
    public static void init(File configDir) {
        importedExPackageList = new ArrayList<>(ExtraPackageLoader.loadExtraModels(new File(configDir, PACK_FOLDER_NAME)));
        importedExPackageMap = new Hashtable<>();
        for (ExtraPackage pack : importedExPackageList) {
            importedExPackageMap.put(pack.packageId, pack);
            MODEL_ITEMS.put(pack, PACK_ITEMS.register("m_" + pack.packageId, () -> new ItemImportedModel(pack)));
            ADDON_ITEMS.put(pack, PACK_ITEMS.register("a_" + pack.packageId, () -> new ItemImportedAddon(pack)));
            SOUND_ITEMS.put(pack, PACK_ITEMS.register("s_" + pack.packageId, () -> new ItemImportedSound(pack)));
        }
    }

    public static void register(IEventBus modBus) {
        PACK_ITEMS.register(modBus);
    }

    public static Item getModelItem(ExtraPackage pack) {
        RegistryObject<Item> item = MODEL_ITEMS.get(pack);
        return item != null ? item.get() : null;
    }

    public static Item getAddonItem(ExtraPackage pack) {
        RegistryObject<Item> item = ADDON_ITEMS.get(pack);
        return item != null ? item.get() : null;
    }

    public static Item getSoundItem(ExtraPackage pack) {
        RegistryObject<Item> item = SOUND_ITEMS.get(pack);
        return item != null ? item.get() : null;
    }

    /**
     * 1.12.2 postInit/serverLoad: resolve the registered items into the packs, price the catalog
     * entries and collect what has to be synced to clients. 1.20.1 runs it from common setup,
     * because the items only exist after the registry events.
     */
    public static void onCommonSetup() {
        soundManager = JBModContainer.soundManager;
        Map<String, IJBAddon.IJBAddonLimitKey> limitKeyMap = JBModContainer.limitKeyMap;

        for (ExtraPackage pack : importedExPackageList) {
            pack.modelItem = getModelItem(pack);
            pack.addonItem = getAddonItem(pack);
            pack.soundItem = getSoundItem(pack);

            for (jp.mc.ancientred.jbrobot.item.external.ExJBAddonLimitKeyInfo limitKeyInfo : pack.limitKeyListCommited) {
                IJBAddon.IJBAddonLimitKey existing = limitKeyMap.get(limitKeyInfo.limitKeyName);
                if (existing == null || existing.getVersion() < limitKeyInfo.getVersion()) {
                    limitKeyMap.put(limitKeyInfo.limitKeyName, limitKeyInfo);
                }
            }

            pack.setupMerchant();
            pack.setupAddons(limitKeyMap);
            pack.setupModelAction(JBModContainer.actionFactory);
            pack.setupBlueprint(blueprintFactory);

            if (soundManager != null) {
                for (Map.Entry<String, jp.mc.ancientred.jbrobot.item.external.ExJBSoundInfo> sound : pack.soundMap.entrySet()) {
                    IJBSound value = sound.getValue();
                    soundManager.registerSound(pack.packageId + ":" + sound.getKey(), value);
                }
            }
        }

        syncedExPackageListServer.clear();
        syncedExPackageListServer.add(DefaultModelMerchantPackage.getInstanceUnSafe());
        syncedExPackageListServer.addAll(importedExPackageList);

        DefaultModelMerchantPackage.getInstanceUnSafe().setupBlueprint(blueprintFactory);
        // 1.12.2 JBRobotMODContainer:188 - the dummy stacks the catalog GUI renders.
        JBRef.setUpReference();
    }

    /** Subtype stacks of one pack item, in pack order (the catalog and creative tab use these). */
    public static ArrayList<ItemStack> createSubtypeStacks(Item item, int[] ids) {
        ArrayList<ItemStack> stacks = new ArrayList<>(ids.length);
        for (int id : ids) {
            ItemStack stack = new ItemStack(item);
            stack.setDamageValue(id);
            stacks.add(stack);
        }

        return stacks;
    }
}
