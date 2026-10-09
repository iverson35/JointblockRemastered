package jp.mc.ancientred.jbrobot.item.external;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An addon item of a model pack: the inventory item that hands out one {@link ExJBAddonInfo} per
 * item damage value (the pack's {@link #addonItemId}), and the catalog entry for it.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBAddonItemInfo}. {@link #getAddon()}
 * reads the addon straight off the produced item stack (the package's addon item implements
 * {@link IJBAddon.IJBAddonHolder}), exactly as in 1.12.2; a 1.20.1 {@link ItemStack#getDamageValue()}
 * replaces {@code getItemDamage()} and {@code getItem()} replaces {@code getItem()}.
 */
public class ExJBAddonItemInfo implements IJBRMerchantEntry {
    public final JsonObject properties;
    public final ExtraPackage parent;
    public final int addonItemId;
    public final String addonName;
    public String addonItemName;
    public String descriptionText;
    public ItemStack merchantCost;
    public ExJBAddonInfo addon;
    public ItemStack addonItemStack;

    @OnlyIn(Dist.CLIENT)
    public String iconLoadInFileEntryName;
    @OnlyIn(Dist.CLIENT)
    ModelMerchantItem merchantModel;

    public ExJBAddonItemInfo(ExtraPackage parent, int argAddonItemId, String argAddonName, JsonObject argProperties) {
        this.parent = parent;
        this.addonItemId = argAddonItemId;
        this.addonName = argAddonName;
        this.properties = argProperties;
    }

    @Override
    public String getEntryName() {
        return CompatHelper.formatLang(this.addonItemName);
    }

    @Override
    public ItemStack getMerchantCost() {
        return this.merchantCost;
    }

    @Override
    public ItemStack getMerchantResult(Level worldObj) {
        return this.addonItemStack;
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public IJBAddon getAddon() {
        return this.addonItemStack.getItem() instanceof IJBAddon.IJBAddonHolder
                ? ((IJBAddon.IJBAddonHolder) this.addonItemStack.getItem()).getIJBAddon(this.addonItemStack.getDamageValue())
                : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getEntryItemStack() {
        return this.addonItemStack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IJBModelItem.IJBModel getModel() {
        if (this.merchantModel == null) {
            this.merchantModel = new ModelMerchantItem(this);
        }

        return this.merchantModel;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getModelMeta() {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getCatalogScale() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getEntryDesc() {
        return this.descriptionText;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getActionName() {
        return "";
    }
}
