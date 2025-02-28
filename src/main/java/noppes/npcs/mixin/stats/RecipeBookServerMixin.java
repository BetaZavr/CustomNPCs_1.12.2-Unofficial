package noppes.npcs.mixin.stats;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBookServer;
import noppes.npcs.api.mixin.stats.IRecipeBookMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeBookServer.class)
public class RecipeBookServerMixin {

    @Inject(method = "getRecipes", at = @At("HEAD"))
    private void npcs$getRecipes(CallbackInfoReturnable<NBTTagCompound> cir) {
        ((IRecipeBookMixin) this).npcs$checkRecipes();
    }

}
