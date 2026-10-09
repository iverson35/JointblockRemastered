package jp.mc.ancientred.jbrobot.item.merchant;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import jp.mc.ancientred.jbrobot.item.external.ExJBBluePrintInfo;
import jp.mc.ancientred.jbrobot.item.external.ModelMerchantItem;
import jp.mc.ancientred.jbrobot.registry.JBRItems;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBBlueprintFactory;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jointblock.modelpack.JBRModelUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The built-in {@code default} catalog package: the 21 model entries, the 12 basic-addon entries and
 * the 6 built-in blueprints, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.merchant.DefaultModelMerchantPackage}.
 *
 * <p>1.20.1 deviations:
 * <ul>
 *   <li>the 1.12.2 {@code new ItemStack(item, 1, damage)} subtype stacks are built through
 *       {@link #subtypedStack(Item, int)}, which writes the vanilla {@code Damage} tag - the same
 *       convention {@code jp.mc.ancientred.jointblock.modelpack.ExtraPackage} uses, so a catalog model
 *       meta is the item damage value.</li>
 *   <li>the item statics of the 1.12.2 addon container became {@link JBRItems}
 *       {@code RegistryObject}s.</li>
 *   <li>{@code NBTTagCompound} became {@link CompoundTag}; {@code World} became {@link Level}.</li>
 *   <li>the blueprint base64 payloads are kept byte-for-byte (they are the original serialized
 *       JointBlock blueprints).</li>
 * </ul>
 */
public class DefaultModelMerchantPackage implements IJBRMerchantPackage {
   public static final String ITENTIFIER = "default";
   private static DefaultModelMerchantPackage instance;
   private ArrayList<IJBRMerchantEntry> merchantModelsEntryList = new ArrayList<>();
   private ArrayList<ExJBBluePrintInfo> merchantBlueprintEntryList = new ArrayList<>();
   private ArrayList<IJBRMerchantEntry> merchantAddonEntryList = new ArrayList<>();
   @OnlyIn(Dist.CLIENT)
   private static ItemStack iconStack;

   public static DefaultModelMerchantPackage getInstanceUnSafe() {
      if (instance == null) {
         instance = new DefaultModelMerchantPackage();
      }

      return instance;
   }

   private DefaultModelMerchantPackage() {
      this.registerDefaultModels();
      this.registerDefaultBlueprints();
   }

   /** 1.12.2 {@code new ItemStack(item, 1, damage)}; 1.20.1 has no damage ctor. */
   private static ItemStack subtypedStack(Item item, int subtype) {
      ItemStack stack = new ItemStack(item);
      stack.setDamageValue(subtype);
      return stack;
   }

   private void registerDefaultModels() {
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBazookaGripPartsItem.get(), 0), 0.5F));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBazookaHeadPartsItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBazookaHeadPartsItem.get(), 1)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBazookaTailPartsItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBazookaTubePartsItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelDrillItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelFaceItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelHandItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelHandItem.get(), 1)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelLazerGatlingItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelLazerGunItem.get(), 0), 0.5F));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelWheelItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelThrusterItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelMissilePodItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBladeTopItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBladeTopItem.get(), 1)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBladeBodyItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelBladeGripItem.get(), 0), 0.5F));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelFlamerItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelPlanterItem.get(), 0)));
      this.merchantModelsEntryList.add(new DefaultModelMerchantEntry(subtypedStack(JBRItems.ModelHarvesterItem.get(), 0)));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 0), 5));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 4), 8));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 2), 5));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 6), 8));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 8), 5));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 10), 5));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 12), 2));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 14), 5));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 16), 8));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 30), 8));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 32), 8));
      this.merchantAddonEntryList.add(new DefaultCommonMerchantEntry(subtypedStack(JBRItems.BasicAddonItem.get(), 34), 8));
   }

   private void registerDefaultBlueprints() {
      this.addBlueprint(5, "default.blueprint.bipedal", 18, "BR+LCAAAAAAAAABtks9PwjAUx99bGSDijxgP3sSbXj0aTpiQwIHEQLyabi242HZkLaJ/vM63ronAeIf2u+Tbfr7vrT0ABkwaDeBV4VXnFJiVBamXkgpCXTBaBBwthBK6tN9eXd6Q5x7Q4KElLoNn7j2PgAIHwz1PhOXv7j3DygNfex6G5c+uZ0yeiNY9VgsD6zmwoMGKEQ9Y0GC1EQ9Y0GD57qNj88GoDgxsqR2Ja1LWTxhJmXrqPYjTIjNLhDNurdSJkmPFV5aw3Y3J3IxrCf1RtpaCq8Hiey0RzpOcF+I1k9vayqBvFf+Uc5W72Sb8zHZ1eiL8NKCrbbLgq8l/PEqQ1lk6EFvnEkX6ofqmTu90LqZv0zwzbqTy9OPpfaO5UVuduyyna6Wm49xVT4RytkwjY9WpEoFjUxWU88QT+AMiZARLeAIAAA==");
      this.addBlueprint(10, "default.blueprint.tank", 20, "BR+LCAAAAAAAAACFUj1PwzAQvYuTElKgCLExAGJiYGNiRAKpHboQsSIndktUf6DYBfHrw9nJQKVCbrDeOz2/d76kAGDApNEAEbURTabAnGwJlR0VQl8zRoeAvYXQQRo0Zz1l0HWR30eeZOSTE7gsTy/IowDEO8AdiyTFQfP0pwYT6DD63kSe9b09s8Vur6FsHMnOKBvHsskHRnyQfOA/H9qxizumDbGV9oTOCZnYYwVkdduYFcIxd07qSslnxdeOLPOtafySawmHJTebq/L7QyKcVJa34rWRX72OwZFT/FO+KOuX2+GrTsLVuYgrhly7quTrufg9Ut2PdACZ875ShG8DpydcaysWbwvbGP+obL150Lx+b4zU1jeWbKWm69yHf4WGTM3ugOG1SgwhPoZMQ69WMfkHfujd534CAAA=");
      this.addBlueprint(15, "default.blueprint.heli", 14, "BR+LCAAAAAAAAAB1Uj1PwzAQPccxhPA5dGMABgZWxMSIBKJF6tKKFTmJWyz8gWIXxJ8nnG0CFDU3RO/53t17tlICUKDCaADIEbURkV2gTrSIHjosAqkOKX4aGCgC2SYNyX4gtro8aC4jxQZJnPXt7C9HSLsCwQk7OsadIyCEwHoxzPxZhpmrf75N0zOW4eqoOR/WsI52UTMb1uTkW3O7WYNZXHo/RAvtEY0QmXhGS2B1K82CwD53TuhKiTvFlw4KKFZG+inXAnbuhZKn849XQeCgsrxtHqV4TzoKe07xNzFT1k9XOjluhdFxsKc5FNpVc74c/2ZD+zpF2gbmvK8U4ovA8UnPtG0mTxMrjb9Rtn651rx+lkZo66XFtULjOPfhP8CQuVkPGG6r+nv7aFKGs1pF5y8h2Tn7WgIAAA==");
      this.addBlueprint(20, "default.blueprint.hover", 12, "BR+LCAAAAAAAAAB1UU1LAzEQnW22dqkfldKb4AeevIlHjx7E9tCLxatks9kazIckacU/b5zMLgiyziG898ibeZNMARgwaQ0AIU9odAgsSI/oNmFBXzOGRwODVUCCMt+Zd7SAb6gQXMxPz9BznpW/FgYpkeeOOENPIs/if88IUkGea+LjThvIRirtFGgnnMZaExEtEFnS2BTGwivbFnDMQ5Cm1vJR823A6NXOqrjmRsLsye2lF5638XLz9SELOKkd982Lkp/dbQZHQfO9fNYurnf9Wx7kBsscqSyhMqHe8O3yNyGGEF2wCYxDjLVGfJM5Ln9lXLN6XTll44N24v3ecPGmrDQuKodtpUE7j/mHMGpph2LmzXXTj4o0apI1oWn+D11MQVL6AQAA");
      this.addBlueprint(25, "default.blueprint.quadruped", 36, "BR+LCAAAAAAAAABtkz1P5DAQhmfyAcuyLN8IKjghClpKtNVJtxIUCMSKggY5a4OicxIUe+HuD92vhDPjSQqHjYtoXuv1+PUTewgQQ6zKAoCrmqvROsRG1VT9czSgHZsxfST0DgQHiffsNDICh6xPWUfNXE8Pnm08qWvXzFgnkXNR0CNF9z8aUHG8t3VEPS4AZXwy6cRYQfcZeibeA386nlV0H6Fnyp5pxxMl2Gb5xXotwjbLGeshYptlp80CS1nWET9Dz8R7vmUZIX6Enil7ulmQuEDAZRzTP2F9wXqTuDjuscs9bnu4bBGX0PPYw2WbuIQe2cMFiQsEXHZj7GTZIy7dLMtc9olLN8sylwPi0s3yjQvdVcN3Fal6Lqzfm6qS54ZDSOd1Xj4jbAhjVJFpNdXixcAABosytzeiUDC+WwhZL16VPJn9fVUI46wStXzI1XtjjmFktHhT97qyN4v2iaz49Vf+BqcJDAqTzcTLlQxzzZtcq5AaazNN9bnXdI4fRSWvn66rvLQ/dTX/fZkpYax+LyqbV9RWFbRcWP/wKGlS9qT059ay3cnyTod+bq55+y9GkA9R0AMAAA==");
      this.addBlueprint(30, "default.blueprint.insects", 32, "BR+LCAAAAAAAAACFkz1PwzAQhs91QkO/KbCwAGJirZhQJ6QitUOFRMTAgvLhVhF2gmIXxJ/iH0I426mIaCA3pHen13evH7kdAAqUpQIAPMxym3WBSpZj9lFgQBlDip8YaoNAAY7WjG1JoShMfWXqlotzPExO/dEJzrgDogel1RF4BKzGMZpHrRnhTyUc9wuKjp7r//ISx9vKdT8bNS2HlH5mpR/Y8dPWlzIat/QDO348p1Xumv25a9+hjRqCY4jhdWHqru3VMDddq0GmpMJ0UsO0j0xJhem0hukAmZIGXkNk2qTpIlNSYTqpYXqATEmF6bSG6RiZkgZeh8j0Xw2+ZWneMr5EuhIKsyPMUtOjHXCjPElXBPqBlEyEnN3yYC3BA2+TJmoZCAa9eSpZpOSZ//7CCAzCLMjjh4S9WSmFnuTBK7vnmVpuhF26p0/PtYM9BzwhQz9Yz3/soYPIumqDK5UKOeaXukYa5yKLF0+LLEnVDc+i5+vE7heZSjIcywQeD5T+W6JPJ93xqO/Mt7dXZs+x7kXcLP8GErO36ewDAAA=");
   }

   private void addBlueprint(int id, String name, int cost, String blueprintBase64Str) {
      ExJBBluePrintInfo blueprintInfo = new ExJBBluePrintInfo(null, this.merchantModelsEntryList.size(), name, (JsonObject)null);
      blueprintInfo.blueprintId = id;
      blueprintInfo.base64StringData = blueprintBase64Str;
      blueprintInfo.merchantCost = new ItemStack(Items.EMERALD, cost);
      blueprintInfo.createMockDummy();
      this.merchantBlueprintEntryList.add(blueprintInfo);
   }

   public void setupBlueprint(IJBBlueprintFactory blueprintFactory) {
      for (int i = 0; i < this.merchantBlueprintEntryList.size(); ++i) {
         ExJBBluePrintInfo blueprintInf = this.merchantBlueprintEntryList.get(i);
         if (blueprintInf != null && blueprintInf.base64StringData != null) {
            try {
               if (!JBRModelUtils.isStringEmpty(blueprintInf.base64StringData)) {
                  blueprintInf.bluprintItemStack = blueprintFactory.createBlueprintItemFromBase64(blueprintInf.createMapKey(), blueprintInf.base64StringData, blueprintInf.isCapsule());
                  if (!CompatHelper.isStackEmptyOrNull(blueprintInf.bluprintItemStack) && blueprintInf.bluprintItemStack.hasTag()) {
                     CompoundTag nbtTag = blueprintInf.bluprintItemStack.getTag();
                     if (nbtTag != null && nbtTag.contains("jb:blueprint")) {
                        CompoundTag dataTag = nbtTag.getCompound("jb:blueprint");
                        if (dataTag != null) {
                           dataTag.putString("name", blueprintInf.blueprintName);
                        }
                     }
                  }
               }
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }
   }

   @Override
   public String getPackageName() {
      return CompatHelper.formatLang("jbrinfo.gui.defaultpackage");
   }

   @Override
   public int getEntryNums(JBMerchantType type) {
      if (type == JBMerchantType.model) {
         return this.merchantModelsEntryList.size();
      } else if (type == JBMerchantType.blueprint) {
         return this.merchantBlueprintEntryList.size();
      } else {
         return type == JBMerchantType.addon ? this.merchantAddonEntryList.size() : 0;
      }
   }

   @Override
   public IJBRMerchantEntry getMerchantEntry(JBMerchantType type, int index) {
      if (type == JBMerchantType.model) {
         if (index >= 0 && index < this.merchantModelsEntryList.size()) {
            return this.merchantModelsEntryList.get(index);
         }
      } else if (type == JBMerchantType.blueprint) {
         if (index >= 0 && index < this.merchantBlueprintEntryList.size()) {
            return this.merchantBlueprintEntryList.get(index);
         }
      } else if (type == JBMerchantType.addon && index >= 0 && index < this.merchantAddonEntryList.size()) {
         return this.merchantAddonEntryList.get(index);
      }

      return null;
   }

   @Override
   public ItemStack getEntryIconStack() {
      if (iconStack == null) {
         iconStack = new ItemStack(JBRItems.ModelFaceItem.get());
      }

      return iconStack;
   }

   public static class DefaultCommonMerchantEntry implements IJBRMerchantEntry {
      private ItemStack itemStack;
      private ItemStack merchantCost;
      private float catalogScale;
      @OnlyIn(Dist.CLIENT)
      ModelMerchantItem merchantModel;

      public DefaultCommonMerchantEntry(ItemStack itemStack, int cost) {
         this(itemStack, 1.0F, cost);
      }

      public DefaultCommonMerchantEntry(ItemStack itemStack, float catalogScale, int cost) {
         this.catalogScale = 1.0F;
         this.itemStack = itemStack;
         this.merchantCost = new ItemStack(Items.EMERALD, cost);
         this.catalogScale = catalogScale;
      }

      @Override
      public String getEntryName() {
         return this.itemStack.getHoverName().getString();
      }

      @Override
      public ItemStack getMerchantCost() {
         return this.merchantCost;
      }

      @Override
      public ItemStack getMerchantResult(Level worldObj) {
         return this.itemStack;
      }

      @Override
      public boolean hasAction() {
         return false;
      }

      @Override
      public IJBAddon getAddon() {
         return this.itemStack.getItem() instanceof IJBAddon.IJBAddonHolder
               ? ((IJBAddon.IJBAddonHolder)this.itemStack.getItem()).getIJBAddon(this.itemStack.getDamageValue()) : null;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public ItemStack getEntryItemStack() {
         return this.itemStack;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public IJBModelItem.IJBModel getModel() {
         if (this.merchantModel == null) {
            this.merchantModel = new ModelMerchantItem(this.itemStack);
         }

         return this.merchantModel;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public int getModelMeta() {
         return this.itemStack.getDamageValue();
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public float getCatalogScale() {
         return this.catalogScale;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public String getEntryDesc() {
         return "default.addon.desc";
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public String getActionName() {
         return "";
      }
   }

   public static class DefaultModelMerchantEntry implements IJBRMerchantEntry {
      private ItemStack itemStack;
      private ItemStack merchantCost;
      private float catalogScale;

      public DefaultModelMerchantEntry(ItemStack itemStack) {
         this(itemStack, 1.0F);
      }

      public DefaultModelMerchantEntry(ItemStack itemStack, float catalogScale) {
         this.catalogScale = 1.0F;
         this.itemStack = itemStack;
         int costEmerald = 2;
         int modelMeta = itemStack.getDamageValue();
         if (itemStack.getItem() instanceof IJBModelItem && ((IJBModelItem)itemStack.getItem()).getIJBActionHandler(modelMeta) != null) {
            ++costEmerald;
         }

         if (itemStack.getItem() instanceof IJBAddon.IJBAddonHolder && ((IJBAddon.IJBAddonHolder)itemStack.getItem()).getIJBAddon(modelMeta) != null) {
            ++costEmerald;
         }

         this.merchantCost = new ItemStack(Items.EMERALD, costEmerald);
         this.catalogScale = catalogScale;
      }

      @Override
      public String getEntryName() {
         return this.itemStack.getHoverName().getString();
      }

      @Override
      public ItemStack getMerchantCost() {
         return this.merchantCost;
      }

      @Override
      public ItemStack getMerchantResult(Level worldObj) {
         return this.itemStack;
      }

      @Override
      public boolean hasAction() {
         if (this.itemStack.getItem() instanceof IJBModelItem) {
            return ((IJBModelItem)this.itemStack.getItem()).getIJBActionHandler(this.itemStack.getDamageValue()) != null;
         } else {
            return false;
         }
      }

      @Override
      public IJBAddon getAddon() {
         return this.itemStack.getItem() instanceof IJBAddon.IJBAddonHolder
               ? ((IJBAddon.IJBAddonHolder)this.itemStack.getItem()).getIJBAddon(this.itemStack.getDamageValue()) : null;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public ItemStack getEntryItemStack() {
         return this.itemStack;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public IJBModelItem.IJBModel getModel() {
         return this.itemStack.getItem() instanceof IJBModelItem
               ? ((IJBModelItem)this.itemStack.getItem()).getIJBModel(this.itemStack.getDamageValue()) : null;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public int getModelMeta() {
         return this.itemStack.getDamageValue();
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public float getCatalogScale() {
         return this.catalogScale;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public String getEntryDesc() {
         return "default.model.desc";
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public String getActionName() {
         return ((IJBModelItem)this.itemStack.getItem()).getIJBActionHandler(this.itemStack.getDamageValue()) != null
               ? ((IJBModelItem)this.itemStack.getItem()).getIJBActionHandler(this.itemStack.getDamageValue()).getActionName() : "";
      }
   }
}
