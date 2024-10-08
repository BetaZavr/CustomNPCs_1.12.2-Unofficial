package noppes.npcs.mixin.impl.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ServerRecipeBookHelper;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.client.util.NPCRecipePicker;
import noppes.npcs.controllers.data.Availability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(value = ServerRecipeBookHelper.class, remap = false)
public class ServerRecipeBookHelperMixin {

    // (LOGGER) field_194330_a
    @Final
    @Shadow(aliases = "field_194331_b")
    private RecipeItemHelper recipeItemHelper;
    @Shadow(aliases = "field_194332_c")
    private EntityPlayerMP player;
    @Shadow(aliases = "field_194333_d")
    private IRecipe recipe;
    @Shadow(aliases = "field_194334_e")
    private boolean isShiftPressed;
    @Shadow(aliases = "field_194335_f")
    private InventoryCraftResult invCraftResult;
    @Shadow(aliases = "field_194336_g")
    private InventoryCrafting invCrafting;
    @Shadow(aliases = "field_194337_h")
    private List<Slot> slots;

    @Unique
    private boolean npcs$ignoreDamage = false;
    @Unique
    private boolean npcs$ignoreNBT = false;

    /*
     * NetHandlerPlayServer.func_194308_a(CPacketPlaceRecipe cPacket) {} -> here:
     * (processCraftRecipe) func_194327_a(EntityPlayerMP playerMP, IRecipe checkRecipe, boolean shiftPressed) {}
     *
     * checks if the player open container has a crafting grid;
     * if the player has items for crafting, then places them; -> func_194329_b()
     * if there are not enough items, then sends back a ghost recipe; -> player.connection.sendPacket(new SPacketPlaceGhostRecipe())
     */

    // parent: func_194326_a()
    @Unique
    private void npcs$clearInventoryCrafting() {
        InventoryPlayer inventoryplayer = this.player.inventory;
        for (int i = 0; i < this.invCrafting.getSizeInventory(); ++i) {
            ItemStack itemstack = this.invCrafting.getStackInSlot(i);
            if (itemstack.isEmpty()) { continue; }
            // return item to player 1 piece at a time
            while (itemstack.getCount() > 0) {
                int slotID = inventoryplayer.storeItemStack(itemstack); // slotID where this item can be placed
                if (slotID == -1) { slotID = inventoryplayer.getFirstEmptyStack(); } // or Empty
                ItemStack itemstack1 = itemstack.copy();
                itemstack1.setCount(1);
                inventoryplayer.add(slotID, itemstack1);
                this.invCrafting.decrStackSize(i, 1);
            }
        }
        this.invCrafting.clear();
        this.invCraftResult.clear();
    }

    @Inject(method = "func_194329_b", at = @At("HEAD"), cancellable = true)
    public void npcs$placeRecipeInCraftingGrid(CallbackInfo ci) {
        ci.cancel();
        boolean isMatches = recipe.matches(invCrafting, player.world);
        /*
         * int craftableStacks = this.recipeItemHelper.getBiggestCraftableStack(this.recipe, null); // -> RecipePicker(recipe).tryPickAll()
         *
         * this method should return the maximum number of crafts relative to items in the player's inventory,
         * but for some reason it doesn't take into account the amount of ingredient in the slot
         * and can't make a mixin into private class RecipeItemHelper.RecipePicker
         */
        int maxCraftableStacks = (new NPCRecipePicker(recipeItemHelper.itemToCount, recipe)).getBiggestCraftableStack(invCrafting.getInventoryStackLimit());
        if (isMatches) {
            // have all items in crafting grid
            boolean flag = true;
            for (int i = 0; i < this.invCrafting.getSizeInventory(); ++i) {
                ItemStack itemstack = this.invCrafting.getStackInSlot(i);
                if (!itemstack.isEmpty() && Math.min(maxCraftableStacks, itemstack.getMaxStackSize()) > itemstack.getCount()) {
                    flag = false;
                }
            }
            if (flag) { return; }
        }
        int minCraftableStacks = this.npcs$getSmallestCraftableStack(maxCraftableStacks, isMatches);
        IntList listOfItemIDs = new IntArrayList();
        if (!this.recipeItemHelper.canCraft(this.recipe, listOfItemIDs, minCraftableStacks)) { return; }
        int craftableCount = minCraftableStacks;
        for (int itemID : listOfItemIDs) {
            int maxStack = RecipeItemHelper.unpack(itemID).getMaxStackSize();
            if (maxStack < craftableCount) {
                craftableCount = maxStack;
            }
        }
        if (this.recipeItemHelper.canCraft(this.recipe, listOfItemIDs, craftableCount)) {
            this.npcs$clearInventoryCrafting();
            this.npcs$fillCraftingGrid(craftableCount, listOfItemIDs);
        }
    }

    // parent: func_194324_a(int p_194324_1_, boolean p_194324_2_)
    @Unique
    private int npcs$getSmallestCraftableStack(int maximum, boolean isMatches) {
        int amount = 1;
        if (this.isShiftPressed) { return maximum; }
        if (isMatches) {
            amount = 64;
            for (int i = 0; i < this.invCrafting.getSizeInventory(); ++i) {
                ItemStack itemstack = this.invCrafting.getStackInSlot(i);
                if (!itemstack.isEmpty() && amount > itemstack.getCount()) { amount = itemstack.getCount(); }
            }
            if (amount < 64) { ++amount; }
        }
        return amount;
    }

    // parent: func_194323_a(int p_194323_1_, IntList p_194323_2_)
    @Unique
    private void npcs$fillCraftingGrid(int craftableCount, IntList listOfItemIDs) {
        int width = this.invCrafting.getWidth();
        int height = this.invCrafting.getHeight();
        if (this.recipe instanceof IShapedRecipe) {
            IShapedRecipe shapedrecipes = (IShapedRecipe) this.recipe;
            width = shapedrecipes.getRecipeWidth();
            height = shapedrecipes.getRecipeHeight();
        }
        int slotID = 1;
        Iterator<Integer> iterator = listOfItemIDs.iterator();
        for (int k = 0; k < this.invCrafting.getWidth() && height != k; ++k) {
            for (int l = 0; l < this.invCrafting.getHeight(); ++l) {
                if (width == l || !iterator.hasNext()) {
                    slotID += this.invCrafting.getWidth() - l;
                    break;
                }
                Slot slot = this.slots.get(slotID);
                ItemStack itemstack = RecipeItemHelper.unpack(iterator.next());
                if (!itemstack.isEmpty()) {
                    int count = 1;
                    ItemStack[] ingStacks = recipe.getIngredients().get(l + k * this.invCrafting.getWidth()).getMatchingStacks();
                    for (ItemStack st : ingStacks) { // for variants
                        if (st.isEmpty()) { continue; }
                        if (NoppesUtilPlayer.compareItems(st, itemstack, npcs$ignoreDamage, npcs$ignoreNBT)) {
                            count = st.getCount();
                            break;
                        }
                    }
                    for (int i = 0; i < craftableCount; ++i) {
                        this.npcs$decrStack(slot, itemstack, count);
                    }
                }
                ++slotID;
            }
            if (!iterator.hasNext()) { break; }
        }
    }

    // parent: func_194325_a(Slot p_194325_1_, ItemStack p_194325_2_)
    @Unique
    private void npcs$decrStack(Slot slot, ItemStack stack, int count) {
        InventoryPlayer inventoryplayer = this.player.inventory;
        while (count > 0) {
            int slotID = inventoryplayer.findSlotMatchingUnusedItem(stack);
            if (slotID == -1) {
                return;
            }
            ItemStack itemstack = inventoryplayer.getStackInSlot(slotID).copy();
            if (itemstack.isEmpty()) {
                return;
            }
            if (itemstack.getCount() > 1) {
                inventoryplayer.decrStackSize(slotID, 1);
            } else {
                inventoryplayer.removeStackFromSlot(slotID);
            }
            itemstack.setCount(1);
            if (slot.getStack().isEmpty()) {
                slot.putStack(itemstack);
            } else {
                slot.getStack().grow(1);
            }
            count--;
        }
    }

    // (canCraft) func_194328_c()
    @Inject(method = "func_194328_c", at = @At("HEAD"), cancellable = true)
    public void npcs$canCraft(CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        InventoryPlayer inventoryplayer = this.player.inventory;
        if (recipe instanceof INpcRecipe) {
            npcs$ignoreDamage = ((INpcRecipe) recipe).getIgnoreDamage();
            npcs$ignoreNBT = ((INpcRecipe) recipe).getIgnoreNBT();
            Availability npcs$availability = (Availability) ((INpcRecipe) recipe).getAvailability();
            if (!npcs$availability.isAvailable(player)) {
                cir.setReturnValue(false);
                return;
            }
            int stackLimit = inventoryplayer.getInventoryStackLimit();
            for (int i = 0; i < this.invCrafting.getSizeInventory(); ++i) {
                ItemStack craftStack = this.invCrafting.getStackInSlot(i);
                if (craftStack.isEmpty()) { continue; }
                ItemStack playerStack = inventoryplayer.getCurrentItem();
                int slotID = -1;
                if (this.npcs$canMergeStacks(playerStack, craftStack, stackLimit)) { slotID = inventoryplayer.currentItem; }
                if (slotID == -1) {
                    playerStack = inventoryplayer.getStackInSlot(40);
                    if (this.npcs$canMergeStacks(playerStack, craftStack, stackLimit)) { slotID = inventoryplayer.currentItem; }
                }
                if (slotID == -1) {
                    for (int s = 0; s < inventoryplayer.mainInventory.size(); ++s) {
                        playerStack = inventoryplayer.mainInventory.get(i);
                        if (this.npcs$canMergeStacks(playerStack, craftStack, stackLimit)) {
                            slotID = s;
                            break;
                        }
                    }
                }
                if (slotID == -1) {
                    slotID = inventoryplayer.getFirstEmptyStack();
                }
                if (slotID == -1) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        else {
            for (int i = 0; i < this.invCrafting.getSizeInventory(); ++i) {
                ItemStack craftStack = this.invCrafting.getStackInSlot(i);
                if (craftStack.isEmpty()) { continue; }
                int slotID = inventoryplayer.storeItemStack(craftStack);
                if (slotID == -1) { slotID = inventoryplayer.getFirstEmptyStack(); }
                if (slotID == -1) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        cir.setReturnValue(true);
    }

    @Unique
    private boolean npcs$canMergeStacks(ItemStack stack1, ItemStack stack2, int stackLimit) {
        return !stack1.isEmpty() && NoppesUtilPlayer.compareItems(stack1, stack2, npcs$ignoreDamage, npcs$ignoreNBT) && stack1.isStackable() && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < stackLimit;
    }

}
