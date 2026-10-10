package jp.mc.ancientred.jbrobot.item.external;

import com.google.gson.JsonObject;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A blueprint of a model pack (a {@code blueprint/*.json} entry), addressed by {@link #blueprintId}.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBBluePrintInfo}, ported with the
 * original field names (including the {@code bluprintItemStack} misspelling the loader and merchant
 * code reference).
 *
 * <p>1.20.1 deviations: the blueprint item's 1.12 damage value is the NBT tag {@code subtype} in the
 * port, so {@link #createMockDummy()} builds the subtype-2/3 stack through
 * {@link ItemJBBlueprint#setSubtype}; {@code NBTTagCompound} became {@link CompoundTag} and
 * {@code setTag} replaces {@code func_77982_d}.
 */
public class ExJBBluePrintInfo implements IJBRMerchantEntry {
    // 1.12.2 declared this constant without the trailing ':' but keyed the map with the literal
    // "bp-jbr:df:" + blueprintId, so the runtime key keeps the separator.
    public static final String DEFAULT_PACKAGE_BLUEPRINT_MEM_SAVE_KEY_PREF = "bp-jbr:df:";
    public static final String BLUEPRINT_MEM_SAVE_KEY_PREF = "bp-jbr:ex:";
    public static final String NBT_TAGNAME_DUMMEY_BP_KEY = "dummeyBpKey";

    public ExtraPackage parent;
    public int blueprintId;
    public String blueprintName;
    public String descriptionText;
    public ItemStack bluprintItemStack;
    public ItemStack bluprintItemStackMockDummy;
    public JsonObject properties;
    public ItemStack merchantCost;
    public String mapKey;
    public String base64StringData;
    public boolean isCapsule;

    public ExJBBluePrintInfo(ExtraPackage parent, int argBlueprintId, String argBlueprintName, JsonObject argProperties) {
        this.parent = parent;
        this.blueprintId = argBlueprintId;
        this.blueprintName = argBlueprintName;
        this.properties = argProperties;
    }

    public boolean isCapsule() {
        return this.isCapsule;
    }

    @Override
    public String getEntryName() {
        return CompatHelper.formatLang(this.blueprintName);
    }

    @Override
    public ItemStack getMerchantCost() {
        return this.merchantCost;
    }

    @Override
    public ItemStack getMerchantResult(Level worldObj) {
        return worldObj != null && !worldObj.isClientSide ? this.bluprintItemStack : this.bluprintItemStackMockDummy;
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public IJBAddon getAddon() {
        return null;
    }

    public String createMapKey() {
        if (this.mapKey == null) {
            if (this.parent == null) {
                this.mapKey = DEFAULT_PACKAGE_BLUEPRINT_MEM_SAVE_KEY_PREF + this.blueprintId;
            } else {
                this.mapKey = BLUEPRINT_MEM_SAVE_KEY_PREF + this.parent.packageId + ":" + this.blueprintId;
            }
        }

        return this.mapKey;
    }

    public void createMockDummy() {
        ItemStack dummy = new ItemStack(JBItems.BluePrintItem);
        // 1.12.2 used damage 2 (merchant) / 3 (capsule); the port keeps the damage semantics as the
        // ItemJBBlueprint "subtype" NBT tag.
        ItemJBBlueprint.setSubtype(dummy, this.isCapsule() ? ItemJBBlueprint.TYPE_CAPSULE : ItemJBBlueprint.TYPE_MERCHANT);
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_TAGNAME_DUMMEY_BP_KEY, this.createMapKey());
        dummy.setTag(tag);
        this.bluprintItemStackMockDummy = dummy;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getEntryItemStack() {
        return this.bluprintItemStackMockDummy;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IJBModelItem.IJBModel getModel() {
        return ModelBlueprint.instance;
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
