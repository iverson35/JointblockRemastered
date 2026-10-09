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
 * Port of the 1.12.2 shapeless subtype recipes of {@code JBModContainer.addRecipie()} - the
 * {@code r_mousecol*}, {@code r_wrenchmode*}, {@code r_disasm2mount}, {@code r_putty_*},
 * {@code r_*motseatnm*}, {@code r_jbconreset} and the five {@code r_*_nbtreset} recipes: one item of
 * damage value N goes in, one item of damage value M comes out.
 *
 * <p>They cannot be datapack {@code crafting_shapeless} entries: 1.20.1 ingredients ignore NBT, so a
 * plain item ingredient would match every subtype and make the cyclic conversions (mouse 0-&gt;1-&gt;2-&gt;3-&gt;0)
 * ambiguous, while {@code forge:nbt} would refuse the untagged stacks that 1.12.2 players carry. This
 * recipe matches the subtype in code instead, reading it with the same default-0 semantics as the item
 * classes, and returns a canonical stack - which also makes the {@code *_nbtreset} recipes (N == M)
 * strip the data the input carried, exactly like the original did.
 */
public class SubtypeConversionRecipe extends CustomRecipe {

   private final Item inputItem;
   private final int inputSubtype;
   private final ItemStack result;

   public SubtypeConversionRecipe(ResourceLocation id, CraftingBookCategory category, Item inputItem, int inputSubtype,
         Item resultItem, int resultSubtype) {
      super(id, category);
      this.inputItem = inputItem;
      this.inputSubtype = inputSubtype;
      this.result = SubtypeStacks.create(resultItem, resultSubtype);
   }

   @Override
   public boolean matches(CraftingContainer craftMatrix, Level level) {
      boolean found = false;
      for (int i = 0; i < craftMatrix.getContainerSize(); ++i) {
         ItemStack stack = craftMatrix.getItem(i);
         if (stack.isEmpty()) {
            continue;
         }
         // the 1.12.2 recipes were shapeless and took exactly one item
         if (found || stack.getItem() != this.inputItem || SubtypeStacks.getSubtype(stack) != this.inputSubtype) {
            return false;
         }
         found = true;
      }
      return found;
   }

   @Override
   public ItemStack assemble(CraftingContainer craftMatrix, RegistryAccess registryAccess) {
      return this.result.copy();
   }

   @Override
   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.result.copy();
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 1;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return JBRecipes.SUBTYPE_CONVERSION.get();
   }

   /** {@code {"input":{"item":..,"subtype":N},"result":{"item":..,"subtype":M}}}. */
   public static class Serializer implements RecipeSerializer<SubtypeConversionRecipe> {

      @Override
      public SubtypeConversionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
         CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
               GsonHelper.getAsString(json, "category", CraftingBookCategory.MISC.getSerializedName()), CraftingBookCategory.MISC);
         JsonObject input = GsonHelper.getAsJsonObject(json, "input");
         JsonObject result = GsonHelper.getAsJsonObject(json, "result");
         return new SubtypeConversionRecipe(recipeId, category,
               item(GsonHelper.getAsString(input, "item")), GsonHelper.getAsInt(input, "subtype", 0),
               item(GsonHelper.getAsString(result, "item")), GsonHelper.getAsInt(result, "subtype", 0));
      }

      @Override
      public SubtypeConversionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
         CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
         Item inputItem = item(buffer.readResourceLocation());
         int inputSubtype = buffer.readVarInt();
         Item resultItem = item(buffer.readResourceLocation());
         int resultSubtype = buffer.readVarInt();
         return new SubtypeConversionRecipe(recipeId, category, inputItem, inputSubtype, resultItem, resultSubtype);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buffer, SubtypeConversionRecipe recipe) {
         buffer.writeEnum(recipe.category());
         buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(recipe.inputItem));
         buffer.writeVarInt(recipe.inputSubtype);
         buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(recipe.result.getItem()));
         buffer.writeVarInt(SubtypeStacks.getSubtype(recipe.result));
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
