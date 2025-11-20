package hs.elementmod.recipes;

import hs.elementmod.ElementMod;
import hs.elementmod.items.*;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeManager {
    private static final Map<Identifier, ShapedRecipe> recipes = new HashMap<>();

    public static void registerRecipes() {
        registerUpgrader1Recipe();
        registerUpgrader2Recipe();
        registerRerollerRecipe();
        registerAdvancedRerollerRecipe();
        registerLifeCoreRecipe();
        registerDeathCoreRecipe();

        ElementMod.LOGGER.info("Registered {} custom recipes", recipes.size());
    }

    private static void registerUpgrader1Recipe() {
        Identifier id = Identifier.of(ElementMod.MOD_ID, "upgrader_1");

        DefaultedList<Optional<Ingredient>> inputs = DefaultedList.ofSize(9, Optional.empty());
        inputs.set(0, Optional.of(Ingredient.ofItems(Items.GOLD_BLOCK)));
        inputs.set(1, Optional.of(Ingredient.ofItems(Items.FIRE_CHARGE)));
        inputs.set(2, Optional.of(Ingredient.ofItems(Items.GOLD_BLOCK)));
        inputs.set(3, Optional.of(Ingredient.ofItems(Items.WATER_BUCKET)));
        inputs.set(4, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(5, Optional.of(Ingredient.ofItems(Items.GRASS_BLOCK)));
        inputs.set(6, Optional.of(Ingredient.ofItems(Items.GOLD_BLOCK)));
        inputs.set(7, Optional.of(Ingredient.ofItems(Items.FEATHER)));
        inputs.set(8, Optional.of(Ingredient.ofItems(Items.GOLD_BLOCK)));

        ShapedRecipe recipe = new ShapedRecipe(
                "",
                CraftingRecipeCategory.MISC,
                new RawShapedRecipe(3, 3, inputs, Optional.empty()),
                Upgrader1Item.create()
        );

        recipes.put(id, recipe);
    }

    private static void registerUpgrader2Recipe() {
        Identifier id = Identifier.of(ElementMod.MOD_ID, "upgrader_2");

        DefaultedList<Optional<Ingredient>> inputs = DefaultedList.ofSize(9, Optional.empty());
        inputs.set(0, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(1, Optional.of(Ingredient.ofItems(Items.FIRE_CHARGE)));
        inputs.set(2, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(3, Optional.of(Ingredient.ofItems(Items.WATER_BUCKET)));
        inputs.set(4, Optional.of(Ingredient.ofItems(Items.NETHERITE_INGOT)));
        inputs.set(5, Optional.of(Ingredient.ofItems(Items.GRASS_BLOCK)));
        inputs.set(6, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(7, Optional.of(Ingredient.ofItems(Items.FEATHER)));
        inputs.set(8, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));

        ShapedRecipe recipe = new ShapedRecipe(
                "",
                CraftingRecipeCategory.MISC,
                new RawShapedRecipe(3, 3, inputs, Optional.empty()),
                Upgrader2Item.create()
        );

        recipes.put(id, recipe);
    }

    private static void registerRerollerRecipe() {
        Identifier id = Identifier.of(ElementMod.MOD_ID, "element_reroller");

        DefaultedList<Optional<Ingredient>> inputs = DefaultedList.ofSize(9, Optional.empty());
        inputs.set(0, Optional.of(Ingredient.ofItems(Items.IRON_BLOCK)));
        inputs.set(1, Optional.of(Ingredient.ofItems(Items.NETHERITE_SCRAP)));
        inputs.set(2, Optional.of(Ingredient.ofItems(Items.GOLD_BLOCK)));
        inputs.set(3, Optional.of(Ingredient.ofItems(Items.NETHERITE_SCRAP)));
        inputs.set(4, Optional.of(Ingredient.ofItems(Items.TOTEM_OF_UNDYING)));
        inputs.set(5, Optional.of(Ingredient.ofItems(Items.NETHERITE_SCRAP)));
        inputs.set(6, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(7, Optional.of(Ingredient.ofItems(Items.NETHERITE_SCRAP)));
        inputs.set(8, Optional.of(Ingredient.ofItems(Items.EMERALD_BLOCK)));

        ShapedRecipe recipe = new ShapedRecipe(
                "",
                CraftingRecipeCategory.MISC,
                new RawShapedRecipe(3, 3, inputs, Optional.empty()),
                RerollerItem.create()
        );

        recipes.put(id, recipe);
    }

    private static void registerAdvancedRerollerRecipe() {
        Identifier id = Identifier.of(ElementMod.MOD_ID, "advanced_reroller");

        DefaultedList<Optional<Ingredient>> inputs = DefaultedList.ofSize(9, Optional.empty());
        inputs.set(0, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(1, Optional.of(Ingredient.ofItems(Items.NETHERITE_INGOT)));
        inputs.set(2, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(3, Optional.of(Ingredient.ofItems(Items.NETHERITE_INGOT)));
        inputs.set(4, Optional.of(Ingredient.ofItems(Items.TOTEM_OF_UNDYING)));
        inputs.set(5, Optional.of(Ingredient.ofItems(Items.NETHERITE_INGOT)));
        inputs.set(6, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));
        inputs.set(7, Optional.of(Ingredient.ofItems(Items.NETHERITE_INGOT)));
        inputs.set(8, Optional.of(Ingredient.ofItems(Items.DIAMOND_BLOCK)));

        ShapedRecipe recipe = new ShapedRecipe(
                "",
                CraftingRecipeCategory.MISC,
                new RawShapedRecipe(3, 3, inputs, Optional.empty()),
                AdvancedRerollerItem.create()
        );

        recipes.put(id, recipe);
    }

    private static void registerLifeCoreRecipe() {
        ElementMod.LOGGER.info("Life Core recipe registered (placeholder)");
    }

    private static void registerDeathCoreRecipe() {
        ElementMod.LOGGER.info("Death Core recipe registered (placeholder)");
    }

    public static Map<Identifier, ShapedRecipe> getRecipes() {
        return new HashMap<>(recipes);
    }
}
