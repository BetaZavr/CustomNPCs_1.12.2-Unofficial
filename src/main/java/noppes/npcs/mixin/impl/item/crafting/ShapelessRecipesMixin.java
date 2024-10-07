package noppes.npcs.mixin.impl.item.crafting;

import net.minecraft.item.crafting.ShapelessRecipes;
import noppes.npcs.mixin.item.crafting.IShapelessRecipesMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ShapelessRecipes.class)
public class ShapelessRecipesMixin implements IShapelessRecipesMixin {

    @Mutable
    @Final
    @Shadow
    private String group;

    @Override
    public void npcs$setGroup(String newGroupName) {
        if (newGroupName == null) { newGroupName = ""; }
        group = newGroupName;
    }

}
