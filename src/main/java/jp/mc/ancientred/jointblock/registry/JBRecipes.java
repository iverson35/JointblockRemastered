package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.item.recipe.CopyDataRecipe;
import jp.mc.ancientred.jointblock.item.recipe.SubtypeConversionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Recipe serializers for the two recipe kinds that 1.20.1 datapack recipes cannot express, because they
 * key on the 1.12.2 damage value (now the NBT {@code subtype} tag) and, for the copy recipes, move NBT
 * from the input to the output.
 *
 * <p>1.12.2 registered its recipes in code ({@code RegistryEvent.Register<IRecipe>}); 1.20.1 has no such
 * event, so the recipes themselves live in {@code data/jointblock/recipes/} and only the serializers are
 * registered here.
 */
public final class JBRecipes {

    private JBRecipes() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, JointBlockRemastered.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SubtypeConversionRecipe>> SUBTYPE_CONVERSION =
        RECIPE_SERIALIZERS.register("subtype_conversion", SubtypeConversionRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CopyDataRecipe>> COPY_DATA =
        RECIPE_SERIALIZERS.register("copy_data", CopyDataRecipe.Serializer::new);

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
