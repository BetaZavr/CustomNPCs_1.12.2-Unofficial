package noppes.npcs.mixin.inv;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.data.Availability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// methods are mixed, as there are now recipes with more than 1 count ingredient
@Mixin(value = SlotCrafting.class)
public class SlotCraftingMixin {

    @Final
    @Shadow
    private InventoryCrafting craftMatrix;
    @Final
    @Shadow
    private EntityPlayer player;
    @Shadow
    private int amountCrafted;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void npcs$onTake(EntityPlayer thePlayer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        cir.cancel();

        InventoryCraftResult inventorycraftresult = (InventoryCraftResult) ((Slot) (Object) this).inventory;
        IRecipe recipe = inventorycraftresult.getRecipeUsed();
        if (recipe == null) {
            recipe = CraftingManager.findMatchingRecipe(craftMatrix, thePlayer.world);
            if (recipe != null) { inventorycraftresult.setRecipeUsed(recipe); }
        }

        boolean ignoreDamage = false;
        boolean ignoreNBT = false;
        // Availability
        if (recipe instanceof INpcRecipe) {
            if (!((Availability) ((INpcRecipe) recipe).getAvailability()).isAvailable(player)) {
                if (!player.world.isRemote) {
                    player.sendMessage(new TextComponentTranslation("item.craft.not.availability"));
                    player.inventory.setItemStack(ItemStack.EMPTY);
                    player.openContainer.detectAndSendChanges();
                }
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            ignoreDamage = ((INpcRecipe) recipe).getIgnoreDamage();
            ignoreNBT = ((INpcRecipe) recipe).getIgnoreNBT();
        }

        npcs$onCrafting(stack);
        ForgeHooks.setCraftingPlayer(thePlayer);
        NonNullList<ItemStack> nonnulllist = CraftingManager.getRemainingItems(craftMatrix, thePlayer.world);
        ForgeHooks.setCraftingPlayer(null);
System.out.println("CNPCs: craftMatrix: "+craftMatrix);
System.out.println("CNPCs: recipe: "+(recipe instanceof ShapedRecipes)+"; "+(recipe instanceof ShapelessRecipes)+"; "+recipe);

        Map<Integer, Integer> slotToCount = new HashMap<>();
        if (recipe instanceof ShapedRecipes) {
            slotToCount = npcs$processShapedRecipe((ShapedRecipes) recipe, ignoreDamage, ignoreNBT);
        } else if (recipe instanceof ShapelessRecipes ) {
            slotToCount = npcs$processShapelessRecipe((ShapelessRecipes) recipe, ignoreDamage, ignoreNBT);
        }
System.out.println("CNPCs: slotToCount: "+slotToCount);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack craftStack = this.craftMatrix.getStackInSlot(i);
            ItemStack extraStack = nonnulllist.get(i);
            int count = 1;
            if (slotToCount.containsKey(i)) {
                count = slotToCount.get(i);
            }
            if (!craftStack.isEmpty()) {
                this.craftMatrix.decrStackSize(i, count);
                craftStack = this.craftMatrix.getStackInSlot(i);
            }
            if (!extraStack.isEmpty()) {
                if (craftStack.isEmpty()) { this.craftMatrix.setInventorySlotContents(i, extraStack); }
                else if (ItemStack.areItemsEqual(craftStack, extraStack) && ItemStack.areItemStackTagsEqual(craftStack, extraStack)) {
                    extraStack.grow(craftStack.getCount());
                    this.craftMatrix.setInventorySlotContents(i, extraStack);
                }
                else if (!this.player.inventory.addItemStackToInventory(extraStack)) { this.player.dropItem(extraStack, false); }
            }
        }
        thePlayer.openContainer.detectAndSendChanges();
        cir.setReturnValue(stack);
    }

    @Unique
    protected void npcs$onCrafting(ItemStack stack) {
        if (amountCrafted > 0) {
            stack.onCrafting(player.world, player, amountCrafted);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, craftMatrix);
        }
        amountCrafted = 0;
        InventoryCraftResult inventorycraftresult = (InventoryCraftResult) ((Slot) (Object) this).inventory;
        IRecipe irecipe = inventorycraftresult.getRecipeUsed();
        if (irecipe != null && !irecipe.isDynamic()) {
            player.unlockRecipes(Lists.newArrayList(irecipe));
            inventorycraftresult.setRecipeUsed(null);
        }
    }

    @Unique
    public Map<Integer, Integer> npcs$processShapedRecipe(ShapedRecipes recipe, boolean ignoreDamage, boolean ignoreNBT) {
        Map<Integer, Integer> slotToCount = new HashMap<>();
        int width = recipe.getWidth();
        int height = recipe.getHeight();
        // First non-empty slot in the crafting grid
        int startX = -1, startY = -1;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            if (!craftMatrix.getStackInSlot(i).isEmpty()) {
                startX = i % width;
                startY = i / height;
                break;
            }
        }
        if (startX >= 0 && startY >= 0) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if (index >= ingredients.size()) {
                        break;
                    }
                    int slotIndex = (y + startY) * width + (x + startX);
                    ItemStack craftStack = craftMatrix.getStackInSlot(slotIndex);
                    ItemStack ingStack = null;
                    for (ItemStack stack : ingredients.get(index).getMatchingStacks()) {
                        if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
                        if (NoppesUtilPlayer.compareItems(craftStack, stack, ignoreDamage, ignoreNBT)) {
                            ingStack = stack;
                            break;
                        }
                    }
                    if (ingStack != null) {
                        slotToCount.put(slotIndex, ingStack.getCount());
                    }
                }
            }
        }
        return slotToCount;
    }

    @Unique
    public Map<Integer, Integer> npcs$processShapelessRecipe(ShapelessRecipes recipe, boolean ignoreDamage, boolean ignoreNBT) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        Map<Integer, Integer> slotToCount = new HashMap<>();
        Map<ItemStack, Integer> recipeStacks = new HashMap<>();
        List<Integer> founds = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            for (ItemStack ingredientStack : ingredient.getMatchingStacks()) {
                for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
                    if (founds.contains(i)) { continue; }
                    ItemStack itemInSlot = craftMatrix.getStackInSlot(i);
                    if (!NoppesUtilServer.IsItemStackNull(itemInSlot) && NoppesUtilPlayer.compareItems(itemInSlot, ingredientStack, ignoreDamage, ignoreNBT)) {
                        boolean added = true;
                        for (ItemStack recipeStack : recipeStacks.keySet()) {
                            if (NoppesUtilPlayer.compareItems(recipeStack, itemInSlot, ignoreDamage, ignoreNBT)) {
                                added = false;
                                recipeStacks.put(recipeStack, recipeStacks.get(recipeStack) + ingredientStack.getCount());
                                break;
                            }
                        }
                        if (added) {
                            recipeStacks.put(ingredientStack, ingredientStack.getCount());
                        }
                        founds.add(i);
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack itemInSlot = craftMatrix.getStackInSlot(i);
            for (ItemStack recipeStack : recipeStacks.keySet()) {
                int count = recipeStacks.get(recipeStack);
                if (count <= 0) { continue; }
                if (NoppesUtilPlayer.compareItems(recipeStack, itemInSlot, ignoreDamage, ignoreNBT)) {
                    if (itemInSlot.getCount() >= count) {
                        slotToCount.put(i, count);
                        recipeStacks.put(recipeStack, 0);
                    } else {
                        slotToCount.put(i, itemInSlot.getCount());
                        recipeStacks.put(recipeStack, count - itemInSlot.getCount());
                    }
                    break;
                }
            }
        }
        return slotToCount;
    }

}
