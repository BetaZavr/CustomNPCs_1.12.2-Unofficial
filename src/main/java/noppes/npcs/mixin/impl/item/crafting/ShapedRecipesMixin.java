package noppes.npcs.mixin.impl.item.crafting;

import net.minecraft.item.crafting.ShapedRecipes;
import noppes.npcs.mixin.item.crafting.IShapedRecipesMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ShapedRecipes.class)
public class ShapedRecipesMixin implements IShapedRecipesMixin {

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
