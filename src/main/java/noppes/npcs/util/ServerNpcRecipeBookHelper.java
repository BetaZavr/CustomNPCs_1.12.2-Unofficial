package noppes.npcs.util;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketPlaceGhostRecipe;
import net.minecraftforge.common.crafting.IRecipeContainer;
import net.minecraftforge.common.crafting.IShapedRecipe;
import noppes.npcs.Server;
import noppes.npcs.client.util.NpcRecipeItemHelper;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerCarpentryBench;

public class ServerNpcRecipeBookHelper {
	
	private InventoryCrafting craftMatrix;
	private InventoryCraftResult craftResult;
	private boolean isShiftPressed;
	private EntityPlayerMP player;
	private IRecipe recipe;
	private final NpcRecipeItemHelper recipeItemHelper = new NpcRecipeItemHelper();
	private List<Slot> slots;

	private void fillCraftInvWithPlayerStacks() {
		InventoryPlayer inventoryplayer = this.player.inventory;
		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
			ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
			if (itemstack.isEmpty()) {
				continue;
			}
			while (itemstack.getCount() > 0) {
				int j = inventoryplayer.storeItemStack(itemstack);
				if (j == -1) {
					j = inventoryplayer.getFirstEmptyStack();
				}
				ItemStack itemstack1 = itemstack.copy();
				itemstack1.setCount(1);
				inventoryplayer.add(j, itemstack1);
				this.craftMatrix.decrStackSize(i, 1);
			}
		}
		this.craftMatrix.clear();
		this.craftResult.clear();
	}

	private int fillMax(int amount, boolean matches) {
		int i = 1;
		if (this.isShiftPressed) {
			i = amount;
		} else if (matches) {
			i = 64;
			for (int j = 0; j < this.craftMatrix.getSizeInventory(); ++j) {
				ItemStack itemstack = this.craftMatrix.getStackInSlot(j);
				if (!itemstack.isEmpty() && i > itemstack.getCount()) {
					i = itemstack.getCount();
				}
			}
			if (i < 64) {
				++i;
			}
		}

		return i;
	}

	private void fillMaxCraftInv() {
		boolean matches = this.recipe.matches(this.craftMatrix, this.player.world);
		int i = this.recipeItemHelper.getBiggestCraftableStack(this.recipe, (IntList) null);
		if (matches) {
			boolean hasStack = true;
			for (int j = 0; j < this.craftMatrix.getSizeInventory(); ++j) {
				ItemStack itemstack = this.craftMatrix.getStackInSlot(j);
				if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) > itemstack.getCount()) {
					hasStack = false;
				}
			}
			if (hasStack) {
				return;
			}
		}
		int row = this.fillMax(i, matches);
		IntList intlist = new IntArrayList();
		if (!this.recipeItemHelper.canCraft(this.recipe, intlist, row)) {
			return;
		}
		int j1 = row;
		IntListIterator intlistiterator = intlist.iterator();
		while (intlistiterator.hasNext()) {
			int k = ((Integer) intlistiterator.next()).intValue();
			int l = RecipeItemHelper.unpack(k).getMaxStackSize();
			if (l < j1) {
				j1 = l;
			}
		}
		if (this.recipeItemHelper.canCraft(this.recipe, intlist, j1)) {
			this.fillCraftInvWithPlayerStacks();
			this.fillSlot(j1, intlist);
		}
	}

	private void fillSlot(int id, IntList slots) {
		int i = this.craftMatrix.getWidth();
		int j = this.craftMatrix.getHeight();
		if (this.recipe instanceof IShapedRecipe) {
			IShapedRecipe shapedrecipes = (IShapedRecipe) this.recipe;
			i = shapedrecipes.getRecipeWidth();
			j = shapedrecipes.getRecipeHeight();
		}
		int j1 = 1;
		Iterator<Integer> iterator = slots.iterator();
		for (int k = 0; k < this.craftMatrix.getWidth() && j != k; ++k) {
			for (int l = 0; l < this.craftMatrix.getHeight(); ++l) {
				if (i == l || !iterator.hasNext()) {
					j1 += this.craftMatrix.getWidth() - l;
					break;
				}
				Slot slot = this.slots.get(j1);
				ItemStack itemstack = RecipeItemHelper.unpack(((Integer) iterator.next()).intValue());
				if (itemstack.isEmpty()) {
					++j1;
				} else {
					for (int i1 = 0; i1 < id; ++i1) {
						this.hasStack(slot, itemstack);
					}
					++j1;
				}
			}
			if (!iterator.hasNext()) {
				break;
			}
		}
	}

	public void getGhostRecipe(EntityPlayerMP player, @Nullable IRecipe recipe, boolean isShiftPressed) {
		if (recipe == null || !player.getRecipeBook().isUnlocked(recipe)) {
			return;
		}
		this.player = player;
		this.recipe = recipe;
		this.isShiftPressed = isShiftPressed;
		this.slots = player.openContainer.inventorySlots;
		Container container = player.openContainer;
		this.craftResult = null;
		this.craftMatrix = null;

		if (container instanceof ContainerCarpentryBench) {
			this.craftResult = ((ContainerCarpentryBench) container).craftResult;
			this.craftMatrix = ((ContainerCarpentryBench) container).craftMatrix;
		} else if (container instanceof ContainerPlayer) {
			this.craftResult = ((ContainerPlayer) container).craftResult;
			this.craftMatrix = ((ContainerPlayer) container).craftMatrix;
		} else if (container instanceof IRecipeContainer) {
			this.craftResult = ((IRecipeContainer) container).getCraftResult();
			this.craftMatrix = ((IRecipeContainer) container).getCraftMatrix();
		}

		if (this.craftResult == null || this.craftMatrix == null) {
			return;
		}
		if (!this.hasStackInCraftInv() && !player.isCreative()) {
			return;
		}
		this.recipeItemHelper.clear();
		
		player.inventory.fillStackedContents(this.recipeItemHelper, false);
		this.craftMatrix.fillStackedContents(this.recipeItemHelper);
		
		if (this.recipeItemHelper.canCraft(recipe, (IntList) null)) {
			this.fillMaxCraftInv();
		} else {
			this.fillCraftInvWithPlayerStacks();
			Server.sendData(player, EnumPacketClient.SET_GHOST_RECIPE, player.openContainer.windowId,
					CraftingManager.REGISTRY.getIDForObject(recipe));
			player.connection.sendPacket(new SPacketPlaceGhostRecipe(player.openContainer.windowId, recipe));
		}
		player.inventory.markDirty();
	}

	private void hasStack(Slot slot, ItemStack stack) {
		InventoryPlayer inventoryplayer = this.player.inventory;
		int i = inventoryplayer.findSlotMatchingUnusedItem(stack);
		if (i == -1) {
			return;
		}
		ItemStack itemstack = inventoryplayer.getStackInSlot(i).copy();
		if (itemstack.isEmpty()) {
			return;
		}
		if (itemstack.getCount() >= stack.getCount()) {
			inventoryplayer.decrStackSize(i, stack.getCount());
		} else {
			inventoryplayer.removeStackFromSlot(i);
		}
		itemstack.setCount(stack.getCount());
		if (slot.getStack().isEmpty()) {
			slot.putStack(itemstack);
		} else {
			slot.getStack().grow(1);
		}
	}

	private boolean hasStackInCraftInv() {
		InventoryPlayer inventoryplayer = this.player.inventory;
		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
			ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
			if (itemstack.isEmpty()) {
				continue;
			}
			int j = inventoryplayer.storeItemStack(itemstack);
			if (j == -1) {
				j = inventoryplayer.getFirstEmptyStack();
			}
			if (j == -1) {
				return false;
			}
		}
		return true;
	}

}