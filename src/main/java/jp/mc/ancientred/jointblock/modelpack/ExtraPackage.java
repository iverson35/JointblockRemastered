package jp.mc.ancientred.jointblock.modelpack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonItemInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonLimitKeyInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBBluePrintInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundItemInfo;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.JBMerchantType;
import jp.mc.ancientred.jbrobot.model.technesample.ModelRenderError;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBBlueprintFactory;
import jp.mc.ancientred.jointblock.api.IJBActionFactory;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * One loaded extension pack: the data of a {@code <config>/JointBlock/*.zip}.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExtraPackage}. The id arrays and the
 * "compressed index" ordering are kept because they define the pack's item damage values (an entry
 * with id N is item damage N) and the order entries appear in the catalog.
 *
 * <p>1.20.1 notes: {@code new ItemStack(item, count, meta)} is gone, so every subtyped stack is
 * built with {@link #subtypedStack(Item, int)}; resource ids lost their second colon (see
 * {@link #packResource(String)}).
 */
public class ExtraPackage implements IJBRMerchantPackage {
    /** 1.12.2 prefix for pack resource ids: {@code jbrextrapackages:<packageId>:<path>}. */
    public static final String EXTRA_PACKAGE_ROOT_DOMAIN = "jbrextrapackages";

    private static final Logger LOGGER = LogManager.getLogger("JBModel");

    public String packageId;
    public String packageName;
    public short packageVersion;
    public String packageIconFileName;
    public Item modelItem;
    public Item addonItem;
    public Item soundItem;
    public int modelNums;
    public ExJBModelInfo[] modelsLoaded = new ExJBModelInfo[256];
    public int[] modelsIndexCompressed = new int[256];
    public int blueprintNums;
    public ExJBBluePrintInfo[] blueprintsLoaded = new ExJBBluePrintInfo[256];
    public int[] blueprintsIndexCompressed = new int[256];
    public HashMap<String, ExJBSoundInfo> soundMap = new HashMap<>();
    public int soundItemNums;
    public ExJBSoundItemInfo[] soundItemsLoaded = new ExJBSoundItemInfo[256];
    public int[] soundItemsIndexCompressed = new int[256];
    public HashMap<String, ExJBAddonInfo> addonMap = new HashMap<>();
    public ArrayList<ExJBAddonLimitKeyInfo> limitKeyListCommited = new ArrayList<>();
    public int addonItemNums;
    public ExJBAddonItemInfo[] addonItemsLoaded = new ExJBAddonItemInfo[256];
    public int[] addonItemsIndexCompressed = new int[256];
    public Hashtable<String, Map<String, String>> langMap;

    /** 1.12.2 kept a lazily built stack here; the merchant icon of the whole package. */
    private ItemStack iconStack;

    /** Kept open for the client, which reads models and textures straight out of the zip. */
    public FileLoader fileLoader;

    /** Per-pack parse caches (parsed {@code .obj}s and texture locations shared by the layers). */
    public ExtraPackageLoader.ResourceDupeCheckeHolder resources;

    public ExtraPackage(String packageId, String packageName, short packageVersion) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
    }

    /** 1.20.1 {@link ItemStack} has no damage ctor any more; every id-addressable stack uses this. */
    public static ItemStack subtypedStack(Item item, int subtype) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(subtype);
        return stack;
    }

    public String getPackageName() {
        return CompatHelper.formatLang(this.packageName);
    }

    public ExJBModelInfo getLoadedModelInfoFromItemDamage(int modelMeta) {
        return modelMeta >= 0 && modelMeta < this.modelsLoaded.length ? this.modelsLoaded[modelMeta] : null;
    }

    public ExJBAddonItemInfo getLoadedAddonItemInfoFromItemDamage(int modelMeta) {
        return modelMeta >= 0 && modelMeta < this.addonItemsLoaded.length ? this.addonItemsLoaded[modelMeta] : null;
    }

    public ExJBSoundItemInfo getLoadedSoundItemInfoFromItemDamage(int modelMeta) {
        return modelMeta >= 0 && modelMeta < this.soundItemsLoaded.length ? this.soundItemsLoaded[modelMeta] : null;
    }

    public void setModelListCommited(ArrayList<ExJBModelInfo> modelListCommited) {
        for (ExJBModelInfo info : modelListCommited) {
            if (info != null && info.modelId >= 0 && info.modelId < 256) {
                if (this.modelsLoaded[info.modelId] != null) {
                    LOGGER.info("[JBModel] ----------Model id duplicated in package! [" + info.modelId + "]. Ignored.");
                } else {
                    this.modelsLoaded[info.modelId] = info;
                }
            }
        }

        int compressedIndex = 0;
        for (int id = 0; id < this.modelsLoaded.length; ++id) {
            if (this.modelsLoaded[id] != null) {
                this.modelsIndexCompressed[compressedIndex++] = id;
            }
        }

        this.modelNums = compressedIndex;
    }

    public void setBlueprintListCommited(ArrayList<ExJBBluePrintInfo> blueprintListCommited) {
        for (ExJBBluePrintInfo info : blueprintListCommited) {
            if (info != null && info.blueprintId >= 0 && info.blueprintId < 256) {
                if (this.blueprintsLoaded[info.blueprintId] != null) {
                    LOGGER.info("[JBModel] ----------Blueprint id duplicated in package! [" + info.blueprintId + "]. Ignored.");
                } else {
                    this.blueprintsLoaded[info.blueprintId] = info;
                }
            }
        }

        int compressedIndex = 0;
        for (int id = 0; id < this.blueprintsLoaded.length; ++id) {
            if (this.blueprintsLoaded[id] != null) {
                this.blueprintsIndexCompressed[compressedIndex++] = id;
            }
        }

        this.blueprintNums = compressedIndex;
    }

    public void setAddonItemListCommited(ArrayList<ExJBAddonItemInfo> addonItemListCommited) {
        for (ExJBAddonItemInfo info : addonItemListCommited) {
            if (info != null && info.addonItemId >= 0 && info.addonItemId < 256) {
                if (this.addonItemsLoaded[info.addonItemId] != null) {
                    LOGGER.info("[JBModel] ----------Addon item id duplicated in package! [" + info.addonItemId + "]. Ignored.");
                } else {
                    this.addonItemsLoaded[info.addonItemId] = info;
                }
            }
        }

        int compressedIndex = 0;
        for (int id = 0; id < this.addonItemsLoaded.length; ++id) {
            if (this.addonItemsLoaded[id] != null) {
                this.addonItemsIndexCompressed[compressedIndex++] = id;
            }
        }

        this.addonItemNums = compressedIndex;
    }

    public void setSoundItemListCommited(ArrayList<ExJBSoundItemInfo> soundItemListCommited) {
        for (ExJBSoundItemInfo info : soundItemListCommited) {
            if (info != null && info.soundItemId >= 0 && info.soundItemId < 256) {
                if (this.soundItemsLoaded[info.soundItemId] != null) {
                    LOGGER.info("[JBModel] ----------Sound item id duplicated in package! [" + info.soundItemId + "]. Ignored.");
                } else {
                    this.soundItemsLoaded[info.soundItemId] = info;
                }
            }
        }

        int compressedIndex = 0;
        for (int id = 0; id < this.soundItemsLoaded.length; ++id) {
            if (this.soundItemsLoaded[id] != null) {
                this.soundItemsIndexCompressed[compressedIndex++] = id;
            }
        }

        this.soundItemNums = compressedIndex;
    }

    public String getModelNameFromId(int modelMeta) {
        ExJBModelInfo inf = this.getLoadedModelInfoFromItemDamage(modelMeta);
        return inf != null ? inf.modelName : "name not set";
    }

    public String getAddonItemNameFromId(int itemMeta) {
        ExJBAddonItemInfo inf = this.getLoadedAddonItemInfoFromItemDamage(itemMeta);
        return inf != null ? inf.addonItemName : "name not set";
    }

    public String getSoundItemNameFromId(int itemMeta) {
        ExJBSoundItemInfo inf = this.getLoadedSoundItemInfoFromItemDamage(itemMeta);
        return inf != null ? inf.soundItemName : "name not set";
    }

    public void setupAddons(Map<String, IJBAddon.IJBAddonLimitKey> limitKeyMap) {
        for (ExJBAddonInfo addonInf : this.addonMap.values()) {
            if (addonInf.limitKeyName != null) {
                addonInf.limitKey = limitKeyMap.get(addonInf.limitKeyName);
            }
        }
    }

    public void setupMerchant() {
        for (int i = 0; i < this.modelNums; ++i) {
            ExJBModelInfo modelInf = this.modelsLoaded[this.modelsIndexCompressed[i]];
            if (modelInf != null && modelInf.properties != null) {
                JsonObject jObj = modelInf.properties;
                modelInf.merchantCost = jObj.has("merchant") ? this.getMerchantCostStackFormJson(jObj) : new ItemStack(Items.EMERALD);
                if (modelInf.parent.modelItem != null) {
                    modelInf.modelItemStack = subtypedStack(modelInf.parent.modelItem, modelInf.modelId);
                }
            }
        }

        for (int i = 0; i < this.addonItemNums; ++i) {
            ExJBAddonItemInfo addonItemInf = this.addonItemsLoaded[this.addonItemsIndexCompressed[i]];
            if (addonItemInf != null && addonItemInf.properties != null) {
                JsonObject jObj = addonItemInf.properties;
                addonItemInf.merchantCost = jObj.has("merchant") ? this.getMerchantCostStackFormJson(jObj) : new ItemStack(Items.EMERALD);
                if (addonItemInf.parent.addonItem != null) {
                    addonItemInf.addonItemStack = subtypedStack(addonItemInf.parent.addonItem, addonItemInf.addonItemId);
                }
            }
        }

        for (int i = 0; i < this.blueprintNums; ++i) {
            ExJBBluePrintInfo blueprintInf = this.blueprintsLoaded[this.blueprintsIndexCompressed[i]];
            if (blueprintInf != null && blueprintInf.properties != null) {
                JsonObject jObj = blueprintInf.properties;
                blueprintInf.merchantCost = jObj.has("merchant") ? this.getMerchantCostStackFormJson(jObj) : new ItemStack(Items.EMERALD);
                blueprintInf.isCapsule = jObj.has("capsule") && jObj.get("capsule").getAsBoolean();
                blueprintInf.createMockDummy();
            }
        }
    }

    private ItemStack getMerchantCostStackFormJson(JsonObject jObj) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(jObj.get("merchant"), type);
        Item item = Items.EMERALD;
        if (JBRModelUtils.containsString(map, "item")) {
            String itemName = JBRModelUtils.getAsString(map, "item");
            item = JBRModelUtils.tryGetRegisteredItem(itemName);
        }

        int meta = 0;
        if (JBRModelUtils.containsNumber(map, "meta")) {
            int metaGet = (int) JBRModelUtils.getAsNumber(map, "meta");
            if (metaGet >= 0) {
                meta = metaGet;
            }
        }

        int cost = 1;
        if (JBRModelUtils.containsNumber(map, "cost")) {
            int costGet = (int) JBRModelUtils.getAsNumber(map, "cost");
            if (costGet >= 0) {
                cost = costGet;
            }
        }

        return cost != 0 ? subtypedStack(item, meta) : null;
    }

    public void setupModelAction(IJBActionFactory actionFactory) {
        for (int i = 0; i < this.modelNums; ++i) {
            ExJBModelInfo modelInf = this.modelsLoaded[this.modelsIndexCompressed[i]];
            if (modelInf != null && modelInf.properties != null) {
                JsonObject jObj = modelInf.properties;
                if (jObj.has("action")) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<Map<String, Object>>() {}.getType();
                    Map<String, Object> map = gson.fromJson(jObj.get("action"), type);
                    if (JBRModelUtils.containsString(map, "actionType")) {
                        String actionType = JBRModelUtils.getAsString(map, "actionType");
                        if (JBRModelUtils.containsString(map, "soundName")) {
                            String soundNameRaw = JBRModelUtils.getAsString(map, "soundName");
                            if (soundNameRaw.indexOf(':') < 0) {
                                map.put("soundName", this.packageId + ":" + soundNameRaw);
                            }
                        }

                        modelInf.action = actionFactory.createAction(actionType, map);
                    }
                }
            }
        }
    }

    public void setupBlueprint(IJBBlueprintFactory blueprintFactory) {
        for (int i = 0; i < this.blueprintNums; ++i) {
            ExJBBluePrintInfo blueprintInf = this.blueprintsLoaded[this.blueprintsIndexCompressed[i]];
            if (blueprintInf != null && blueprintInf.properties != null) {
                try {
                    JsonObject jObj = blueprintInf.properties;
                    boolean useCoreName = jObj.has("useCoreName") && JBRModelUtils.getBooleanFromJson(jObj, "useCoreName");
                    String strBPData = JBRModelUtils.getStringFromJson(jObj, "blueprintData");
                    if (!JBRModelUtils.isStringEmpty(strBPData)) {
                        blueprintInf.bluprintItemStack = blueprintFactory.createBlueprintItemFromBase64(blueprintInf.createMapKey(), strBPData, blueprintInf.isCapsule());
                        if (!CompatHelper.isStackEmptyOrNull(blueprintInf.bluprintItemStack) && blueprintInf.bluprintItemStack.hasTag()) {
                            CompoundTag nbtTag = blueprintInf.bluprintItemStack.getTag();
                            if (nbtTag != null && nbtTag.contains("jb:blueprint")) {
                                CompoundTag dataTag = nbtTag.getCompound("jb:blueprint");
                                if (dataTag != null) {
                                    if (useCoreName) {
                                        String str = dataTag.getString("name");
                                        if (!JBRModelUtils.isStringEmpty(str)) {
                                            blueprintInf.blueprintName = str;
                                        }
                                    } else {
                                        dataTag.putString("name", blueprintInf.blueprintName);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getEntryNums(JBMerchantType type) {
        if (type == JBMerchantType.model) {
            return this.modelNums;
        } else if (type == JBMerchantType.blueprint) {
            return this.blueprintNums;
        } else {
            return type == JBMerchantType.addon ? this.addonItemNums : 0;
        }
    }

    public IJBRMerchantEntry getMerchantEntry(JBMerchantType type, int index) {
        if (type == JBMerchantType.model) {
            if (index >= 0 && index < this.modelsIndexCompressed.length) {
                return this.modelsLoaded[this.modelsIndexCompressed[index]];
            }
        } else if (type == JBMerchantType.blueprint) {
            if (index >= 0 && index < this.blueprintsIndexCompressed.length) {
                return this.blueprintsLoaded[this.blueprintsIndexCompressed[index]];
            }
        } else if (type == JBMerchantType.addon && index >= 0 && index < this.addonItemsIndexCompressed.length) {
            return this.addonItemsLoaded[this.addonItemsIndexCompressed[index]];
        }

        return null;
    }

    public IJBModelItem.IJBActionHandler getActionFromItemDamage(int modelMeta) {
        ExJBModelInfo inf = this.getLoadedModelInfoFromItemDamage(modelMeta);
        return inf != null ? inf.action : null;
    }

    public IJBAddon getModelAddonFromItemDamage(int modelMeta) {
        ExJBModelInfo inf = this.getLoadedModelInfoFromItemDamage(modelMeta);
        return inf != null ? inf.addon : null;
    }

    public IJBAddon getAddonFromAddonItemDamage(int itemMeta) {
        ExJBAddonItemInfo inf = this.getLoadedAddonItemInfoFromItemDamage(itemMeta);
        return inf != null ? inf.addon : null;
    }

    public IJBSound getSoundFromSoundItemDamage(int itemMeta) {
        ExJBSoundItemInfo inf = this.getLoadedSoundItemInfoFromItemDamage(itemMeta);
        return inf != null ? inf.sound : null;
    }

    public ItemStack getEntryIconStack() {
        if (this.iconStack == null && this.modelItem != null) {
            this.iconStack = subtypedStack(this.modelItem, 999);
        }

        return this.iconStack != null ? this.iconStack : ItemStack.EMPTY;
    }

    /** Item damage values of every model this pack defines, in pack order. */
    public int[] getModelMetas() {
        int[] metas = new int[this.modelNums];
        System.arraycopy(this.modelsIndexCompressed, 0, metas, 0, this.modelNums);
        return metas;
    }

    @OnlyIn(Dist.CLIENT)
    public IJBModelItem.IJBModel getModelFromItemDamage(int modelMeta) {
        ExJBModelInfo inf = this.getLoadedModelInfoFromItemDamage(modelMeta);
        return inf != null ? inf.model : ModelRenderError.instanceError;
    }

    @OnlyIn(Dist.CLIENT)
    public double getRenderExpandFromModelMeta(int modelMeta) {
        ExJBModelInfo inf = this.getLoadedModelInfoFromItemDamage(modelMeta);
        return inf != null ? inf.renderExpand : 0.0;
    }

    /**
     * 1.12.2 addressed pack resources as {@code jbrextrapackages:<packageId>:<path>}. A 1.20.1
     * {@link ResourceLocation} has exactly one colon, so the package id moved into the path:
     * {@code jbrextrapackages:<packageId>/<path>}. Both halves are lower cased because 1.20.1
     * resource paths only accept {@code [a-z0-9/._-]}.
     */
    public ResourceLocation packResource(String resourcePath) {
        return new ResourceLocation(EXTRA_PACKAGE_ROOT_DOMAIN,
                (this.packageId + "/" + resourcePath).toLowerCase(Locale.ROOT));
    }

    /** True when {@code location} addresses a resource of this pack; used by the client pack. */
    public boolean owns(ResourceLocation location) {
        return EXTRA_PACKAGE_ROOT_DOMAIN.equals(location.getNamespace())
                && location.getPath().startsWith(this.packageId.toLowerCase(Locale.ROOT) + "/");
    }

    /** The zip entry name behind a {@link #packResource(String)} location. */
    public String entryNameOf(ResourceLocation location) {
        return location.getPath().substring(this.packageId.length() + 1);
    }

    /** {@link FileLoader#getEntry(String)} is case sensitive; pack zips in the wild are not. */
    public FileLoader.FileEntry getEntryIgnoreCase(String entryName) {
        FileLoader.FileEntry direct = this.fileLoader.getEntry(entryName);
        if (direct != null) {
            return direct;
        }

        for (Enumeration<FileLoader.FileEntry> e = this.fileLoader.entries(); e.hasMoreElements(); ) {
            FileLoader.FileEntry entry = e.nextElement();
            if (entry.getName().equalsIgnoreCase(entryName)) {
                return entry;
            }
        }

        return null;
    }
}
