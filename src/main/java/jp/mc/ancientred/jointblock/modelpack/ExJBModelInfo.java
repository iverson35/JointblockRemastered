package jp.mc.ancientred.jointblock.modelpack;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A single model of a pack, addressed by {@code modelId} (the item damage value of the pack's model
 * item).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBModelInfo}. The 1.12.2 inventory
 * baking half ({@code TextureAtlasSprite icon}, {@code IBakedModel bakedItemModel},
 * {@code domainedIconZipFileName}) is gone: 1.20.1 bakes pack item icons from the model JSONs the
 * resource pack synthesizes per model (see {@code ModelPackResources}), so only the icon's
 * {@link ResourceLocation} is kept here.
 */
public class ExJBModelInfo implements IJBRMerchantEntry {
    public JsonObject properties;
    public final ExtraPackage parent;
    public final int modelId;
    public final String modelName;
    public ExJBModelLayer[] modelLayers;
    public String descriptionText;
    public IJBModelItem.IJBActionHandler action;
    public ItemStack merchantCost;
    public ItemStack modelItemStack;
    public String addonName;
    public IJBAddon addon = null;

    @OnlyIn(Dist.CLIENT)
    public IJBModelItem.IJBModel model;
    @OnlyIn(Dist.CLIENT)
    public double renderExpand;
    @OnlyIn(Dist.CLIENT)
    public int renderType;
    @OnlyIn(Dist.CLIENT)
    public float catalogScale;
    @OnlyIn(Dist.CLIENT)
    public String iconLoadInFileEntryName;

    /** Resolved from {@link #iconLoadInFileEntryName} or the package icon by the client pack. */
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation iconLocation;

    public ExJBModelInfo(ExtraPackage parent, int argModelId, String argModelName, ExJBModelLayer[] argModelLayers, JsonObject argProperties) {
        this.parent = parent;
        this.modelId = argModelId;
        this.modelName = argModelName;
        this.modelLayers = argModelLayers;
        this.properties = argProperties;
    }

    public String getEntryName() {
        return CompatHelper.formatLang(this.modelName);
    }

    public ItemStack getMerchantCost() {
        return this.merchantCost;
    }

    public ItemStack getMerchantResult(Level level) {
        return this.modelItemStack;
    }

    public boolean hasAction() {
        return this.action != null;
    }

    public IJBAddon getAddon() {
        return this.modelItemStack != null && this.modelItemStack.getItem() instanceof IJBAddon.IJBAddonHolder
                ? ((IJBAddon.IJBAddonHolder) this.modelItemStack.getItem()).getIJBAddon(this.modelItemStack.getDamageValue())
                : null;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getEntryItemStack() {
        return this.modelItemStack;
    }

    @OnlyIn(Dist.CLIENT)
    public IJBModelItem.IJBModel getModel() {
        return this.model;
    }

    @OnlyIn(Dist.CLIENT)
    public int getModelMeta() {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getCatalogScale() {
        return this.catalogScale;
    }

    @OnlyIn(Dist.CLIENT)
    public String getEntryDesc() {
        return this.descriptionText;
    }

    @OnlyIn(Dist.CLIENT)
    public String getActionName() {
        return this.action != null ? this.action.getActionName() : "";
    }
}
