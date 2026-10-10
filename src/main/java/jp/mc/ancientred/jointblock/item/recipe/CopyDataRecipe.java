package jp.mc.ancientred.jointblock.item.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jp.mc.ancientred.jointblock.registry.JBRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Port of the three custom 1.12.2 recipes the mod registered through {@code GameData.register_impl}:
 * {@code RecipeCopyTool} ({@code r_cpy_palette}, {@code r_cpy_mold}), {@code RecipeIFFTagCopy}
 * ({@code r_cpy_iff}, {@code r_cpy_msb}) and {@code RecipeRINGTagCopy} ({@code r_cpy_ring}).
 *
 * <p>Two tools of the same subtype are crafted back into two tools, carrying the carving/palette or
 * IFF/ring-program data of whichever input had it; if neither input carries data the result is a fresh
 * tool of that subtype. The original filtered by damage value inside {@code getCraftingResult}, which
 * 1.20.1 ingredients cannot do, so both the subtype check and the data copy happen here.
 */
public class CopyDataRecipe extends CustomRecipe {

   /** Every 1.12.2 copy recipe returned two items. */
   public static final int RESULT_COUNT = 2;

   private final Item toolItem;
   private final int toolSubtype;
   private final String dataTag;
   private final ItemStack fallbackResult;

   public CopyDataRecipe(ResourceLocation id, CraftingBookCategory category, Item toolItem, int toolSubtype, String dataTag) {
      super(id, category);
      this.toolItem = toolItem;
      this.toolSubtype = toolSubtype;
      this.dataTag = dataTag;
      this.fallbackResult = SubtypeStacks.create(toolItem, toolSubtype);
   }

   @Override
   public boolean matches(CraftingContainer craftMatrix, Level level) {
      int count = 0;
      for (int i = 0; i < craftMatrix.getContainerSize(); ++i) {
         ItemStack stack = craftMatrix.getItem(i);
         if (stack.isEmpty()) {
            continue;
         }
         if (++count > RESULT_COUNT || stack.getItem() != this.toolItem
               || SubtypeStacks.getSubtype(stack) != this.toolSubtype) {
            return false;
         }
      }
      return count == RESULT_COUNT;
   }

   @Override
   public ItemStack assemble(CraftingContainer craftMatrix, RegistryAccess registryAccess) {
      ItemStack source = ItemStack.EMPTY;
      for (int i = 0; i < craftMatrix.getContainerSize() && source.isEmpty(); ++i) {
         ItemStack stack = craftMatrix.getItem(i);
         if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains(this.dataTag)) {
            source = stack;
         }
      }

      ItemStack result = source.isEmpty() ? this.fallbackResult.copy() : source.copy();
      result.setCount(RESULT_COUNT);
      return result;
   }

   @Override
   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.fallbackResult.copy();
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= RESULT_COUNT;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return JBRecipes.COPY_DATA.get();
   }

   /** {@code {"tool":{"item":..,"subtype":N},"data":"<nbt key>"}}. */
   public static class Serializer implements RecipeSerializer<CopyDataRecipe> {

      @Override
      public CopyDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
         CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
               GsonHelper.getAsString(json, "category", CraftingBookCategory.MISC.getSerializedName()), CraftingBookCategory.MISC);
         JsonObject tool = GsonHelper.getAsJsonObject(json, "tool");
         return new CopyDataRecipe(recipeId, category, item(GsonHelper.getAsString(tool, "item")),
               GsonHelper.getAsInt(tool, "subtype", 0), GsonHelper.getAsString(json, "data"));
      }

      @Override
      public CopyDataRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
         CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
         Item toolItem = item(buffer.readResourceLocation());
         int toolSubtype = buffer.readVarInt();
         String dataTag = buffer.readUtf();
         return new CopyDataRecipe(recipeId, category, toolItem, toolSubtype, dataTag);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buffer, CopyDataRecipe recipe) {
         buffer.writeEnum(recipe.category());
         buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(recipe.toolItem));
         buffer.writeVarInt(recipe.toolSubtype);
         buffer.writeUtf(recipe.dataTag);
      }

      private static Item item(ResourceLocation name) {
         Item item = ForgeRegistries.ITEMS.getValue(name);
         if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + name + "'");
         }
         return item;
      }

      private static Item item(String name) {
         return item(new ResourceLocation(name));
      }
   }
}
