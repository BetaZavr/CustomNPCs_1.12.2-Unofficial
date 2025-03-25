package noppes.npcs.reflection.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class IngredientReflection {

    private static Field matchingStacks;

    public static ItemStack[] getRawMatchingStacks(Ingredient ingredient) {
        if (ingredient == null) { return new ItemStack[0]; }
        if (matchingStacks == null) {
            Exception error = null;
            try { matchingStacks = Ingredient.class.getDeclaredField("field_193371_b"); } catch (Exception e) { error = e; }
            if (matchingStacks == null) {
                try {
                    matchingStacks = Ingredient.class.getDeclaredField("matchingStacks");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"matchingStacks\"", error);
                return new ItemStack[0];
            }
        }
        try {
            matchingStacks.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(matchingStacks, matchingStacks.getModifiers() & ~Modifier.FINAL);

            return (ItemStack[]) matchingStacks.get(ingredient);
        } catch (Exception e) {
            LogWriter.error("Error get \"matchingStacks\" in " + ingredient, e);
        }
        return new ItemStack[0];
    }

}
