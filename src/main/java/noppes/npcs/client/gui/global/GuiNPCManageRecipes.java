package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.wrapper.WrapperRecipe;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditIngredients;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiNPCManageRecipes
extends GuiContainerNPCInterface2
implements ICustomScrollListener, ISubGuiListener {

	private static boolean onlyMod = true;
	private static final WrapperRecipe recipe = new WrapperRecipe();
	private static final int green = new Color(0xFF70F070).getRGB();
	private static final int red = new Color(0xFFF07070).getRGB();

    private final Map<Boolean, Map<String, List<WrapperRecipe>>> data = new TreeMap<>(); // <isGlobal, <Group, recipe data>>
	private GuiCustomScroll groups;
	private GuiCustomScroll recipes;
	private boolean wait = false;

	public GuiNPCManageRecipes(EntityNPCInterface npc, ContainerManageRecipes container) {
		super(npc, container);
        drawDefaultBackground = false;
		setBackground("inventorymenu.png");
		recipe.domen = CustomNpcs.MODID;
		ySize = 200;
	}

	@Override
	public void buttonEvent(@Nonnull IGuiNpcButton button, int mouseButton) {
		if (mouseButton == 1) {
			int id = button.getID();
			ItemStack heldStack = player.inventory.getItemStack();
			if (id >= 10 && id < 27) {
				if (id == 10) {
					if (heldStack.isEmpty()) {
						recipe.product.setCount(Math.max(1, recipe.product.getCount() - 1));
					}
					else if (recipe.product.isEmpty()) {
						ItemStack stack = null;
						if (recipe.main) { stack = heldStack.copy(); }
						else {
							for (WrapperRecipe wr: data.get(recipe.global).get(recipe.group)) {
								if (wr.main) {
									stack = wr.product.copy();
									break;
								}
							}
						}
						if (stack != null) {
							stack.setCount(1);
							recipe.product = stack;
						}
					}
					else if (!recipe.main || NoppesUtilPlayer.compareItems(recipe.product, heldStack, false, false)) { // +N
						recipe.product.setCount(Math.min(recipe.product.getMaxStackSize(), recipe.product.getCount() + 1));
					}
					if (recipe.product.isEmpty()) { button.setLayerColor(red); }
				}
				else {
					int pos = id - 11;
					ItemStack[] array = recipe.recipeItems.get(pos);
					if (heldStack.isEmpty() && array != null && array.length > 0) {
						int p = button.getCurrentStackID();
						int count = Math.max(0, array[p].getCount() - 1);
						if (count > 0) { array[p].setCount(count); }
						else {
							List<ItemStack> list = new ArrayList<>();
							for (int i = 0; i < array.length; i++) {
								if (i == p) { continue; }
								list.add(array[i]);
							}
							array = list.toArray(new ItemStack[0]);
						}
						button.setStacks(array);
						button.setCurrentStackPos(p);
						recipe.recipeItems.put(pos, array);
					}
					else if ((array == null || array.length == 0) && !heldStack.isEmpty()) {
						ItemStack stack = heldStack.copy();
						stack.setCount(1);
						array = new ItemStack[] { stack };
						button.setStacks(array);
						recipe.recipeItems.put(pos, array);
					}
					else if (array != null) {
						for (int i = 0; i < array.length; i++) {
							if (!array[i].isEmpty() && NoppesUtilPlayer.compareItems(array[i], heldStack, false, false)) {
								array[i].setCount(Math.min(array[i].getMaxStackSize(), array[i].getCount() + 1));
								button.setStacks(array);
								button.setCurrentStackPos(i);
								recipe.recipeItems.put(pos, array);
								break;
							}
						}
					}
					if (recipe.domen.equals(CustomNpcs.MODID)) {
						button.setLayerColor(recipe.isValid() ? array != null && array.length > 0 ? 0 : green : red);
					}
				}
			}
		}
		if (mouseButton == 2) {
			ItemStack heldStack = player.inventory.getItemStack();
			if (heldStack.isEmpty()) {
				ItemStack stack = button.getCurrentStack().copy();
				stack.setCount(stack.getMaxStackSize());
				player.inventory.setItemStack(stack);
				Client.sendData(EnumPacketServer.SetItem, stack.writeToNBT(new NBTTagCompound()));
			}
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		int id = button.getID();
		if (id >= 10 && id < 27) {
			if (!recipe.domen.equals(CustomNpcs.MODID)) { return; }
			// show list of ingredients
			if (id != 10 && isShiftKeyDown()) {
				if (recipe.recipeItems.get(id - 11).length > 0) {
					this.setSubGui(new SubGuiEditIngredients(id - 11, recipe.recipeItems.get(id - 11)));
				}
				return;
			}
			ItemStack heldStack = player.inventory.getItemStack();
			// product
			if (id == 10) {
				if (isAltKeyDown()) { recipe.product.setCount(1); }
				else if (recipe.product.isEmpty()) {
					ItemStack stack = null;
					if (recipe.main && !heldStack.isEmpty()) { stack = heldStack.copy(); }
					else {
						for (WrapperRecipe wr: data.get(recipe.global).get(recipe.group)) {
							if (wr.main) {
								stack = wr.product.copy();
								stack.setCount(heldStack.getCount());
								break;
							}
						}
					}
					if (stack != null) { recipe.product = stack; }
				}
				else {
					if (heldStack.isEmpty()) { recipe.product.setCount(Math.max(1, recipe.product.getCount() - 1)); } // -1
					else if (!recipe.main || NoppesUtilPlayer.compareItems(recipe.product, heldStack, false, false)) { // +N
						recipe.product.setCount(Math.min(recipe.product.getMaxStackSize(), recipe.product.getCount() + heldStack.getCount()));
					}
					else if (recipe.main) { recipe.product = heldStack.copy(); } // replace
					button.setStacks(recipe.product);
				}
				if (recipe.product.isEmpty()) { button.setLayerColor(red); }
			}
			// ingredient
			else {
				int pos = id - 11;
				ItemStack[] array = recipe.recipeItems.get(pos);
				if (isCtrlKeyDown()) { // try to add new
					if (heldStack.isEmpty() || array.length >= 16) { return; }
					if (array.length == 0) {
						array = new ItemStack[] { heldStack.copy() };
						array[0].setCount(1);
						button.setStacks(array);
						recipe.recipeItems.put(pos, array);
					} else {
						boolean found = false;
						for (ItemStack stack : array) {
							if (!stack.isEmpty() && NoppesUtilPlayer.compareItems(stack, heldStack, false, false)) {
								found = true;
								break;
							}
						}
						if (!found) {
							array = Arrays.copyOf(array, array.length + 1);
							array[array.length - 1] = heldStack.copy();
							button.setStacks(array);
							recipe.recipeItems.put(pos, array);
						}
					}
				}
				else if (isAltKeyDown()) { // set count == 1
					if (button.getCurrentStackID() < array.length) {
						array[button.getCurrentStackID()].setCount(1);
						button.setStacks(array);
						recipe.recipeItems.put(pos, array);
					} else if (array.length == 0 && !heldStack.isEmpty()) {
						array = new ItemStack[] { heldStack.copy() };
						array[0].setCount(1);
						button.setStacks(array);
						recipe.recipeItems.put(pos, array);
					}
				}
				else if (array == null || array.length == 0) { // install at least something
					if (!heldStack.isEmpty()) {
						array = new ItemStack[]{ heldStack.copy() };
						button.setStacks(array);
						recipe.recipeItems.put(pos, array);
					}
				}
				else { // +/- count? and set display found stack
					if (heldStack.isEmpty()) { // -1
						int p = button.getCurrentStackID();
						int count = Math.max(0, array[p].getCount() - 1);
						if (count > 0) { array[p].setCount(count); }
						else {
							List<ItemStack> list = new ArrayList<>();
							for (int i = 0; i < array.length; i++) {
								if (i == p) { continue; }
								list.add(array[i]);
							}
							array = list.toArray(new ItemStack[0]);
						}
						button.setStacks(array);
						button.setCurrentStackPos(p);
						recipe.recipeItems.put(pos, array);
					} else {
						boolean found = false;
						for (int i = 0; i < array.length; i++) {
							if (!array[i].isEmpty() && NoppesUtilPlayer.compareItems(array[i], heldStack, false, false)) {
								// +N
								found = true;
								array[i].setCount(Math.min(array[i].getMaxStackSize(), array[i].getCount() + heldStack.getCount()));
								button.setStacks(array);
								button.setCurrentStackPos(i);
								break;
							}
						}
						if (!found) {
							array[button.getCurrentStackID()] = heldStack.copy();
							button.setStacks(array);
							button.setCurrentStackPos(button.getCurrentStackID());
						}
					}
					recipe.recipeItems.put(pos, array);
				}
				if (recipe.domen.equals(CustomNpcs.MODID)) {
					button.setLayerColor(recipe.isValid() ? array != null && array.length > 0 ? 0 : green : red);
				}
			}
			return;
		}
		switch (id) {
			case 0: { // global type
				this.save();
				recipe.clear();
				recipe.global = button.getValue() == 0;
				initGui();
				break;
			}
			case 1: { // Add Group
				SubGuiEditText subGui = new SubGuiEditText(0, new String[]{ Util.instance.getResourceName(recipe.group) });
				subGui.latinAlphabetOnly = true;
				subGui.allowUppercase = false;
				this.setSubGui(subGui);
				break;
			}
			case 2: { // Del Group
				Client.sendData(EnumPacketServer.RecipeRemoveGroup, recipe.global, recipe.group);
				recipe.clear();
				wait = true;
				break;
			}
			case 3: { // Add Recipe
				int i;
				String[] text;
				String[] hovers;
				String label;
				if (recipe.domen.equals(CustomNpcs.MODID)) {
					i = 1;
					text = new String[] { Util.instance.getResourceName(recipe.name) };
					label = new TextComponentTranslation("gui.name").getFormattedText()+":";
					hovers = new String[] { new TextComponentTranslation("recipe.hover.recipe.named").getFormattedText() + ". " + new TextComponentTranslation("hover.latin.alphabet.only").getFormattedText() };
				}
				else {
					i = 4;
					text = new String[] { recipe.group, recipe.name };
					label = new TextComponentTranslation("gui.group").getFormattedText()+" / "+new TextComponentTranslation("gui.name").getFormattedText()+":";
					hovers = new String[] {
							new TextComponentTranslation("recipe.hover.group.named").getFormattedText() + ". " + new TextComponentTranslation("hover.latin.alphabet.only").getFormattedText(),
							new TextComponentTranslation("recipe.hover.recipe.named").getFormattedText() + ". " + new TextComponentTranslation("hover.latin.alphabet.only").getFormattedText()
					};
				}
				SubGuiEditText subGui = new SubGuiEditText(i, text);
				subGui.label = label;
				subGui.hovers = hovers;
				subGui.latinAlphabetOnly = true;
				subGui.allowUppercase = false;
				this.setSubGui(subGui);
				break;
			}
			case 4: { // Del Recipe
				Client.sendData(EnumPacketServer.RecipeRemove, recipe.global, recipe.group, recipe.name);
				recipe.name = "";
				wait = true;
				break;
			}
			case 5: { // ignore Meta
				recipe.ignoreDamage = !recipe.ignoreDamage;
				save();
				initGui();
				break;
			}
			case 6: { // ignore NBT
				recipe.ignoreNBT = !recipe.ignoreNBT;
				save();
				initGui();
				break;
			}
			case 7: { // know
				recipe.known = !recipe.known;
				save();
				initGui();
				break;
			}
			case 8: { // availability
				setSubGui(new SubGuiNpcAvailability(recipe.availability, this));
				break;
			}
			case 9: { // replace shaped <-> shapeless
				recipe.isShaped = !recipe.isShaped;
				save();
				break;
			}
			case 28: {
				ItemStack heldStack = player.inventory.getItemStack();
				if (!heldStack.isEmpty()) {
					player.inventory.setItemStack(ItemStack.EMPTY);
					Client.sendData(EnumPacketServer.SetItem, ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
				}
				break;
			}
			case 30: {
				onlyMod = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				break;
			}
			default: {
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (wait) {
			drawWait();
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		for (int i = 11; i < 27; i++) {
			if (this.getButton(i) != null && this.getButton(i).isVisible() && this.getButton(i).isHovered()) {
				if (getButton(i).getCurrentStack().isEmpty()) { continue; }
				ITextComponent hover = new TextComponentTranslation("recipe.hover.ingredients", "" + (i - 11));
				if (recipe.domen.equals(CustomNpcs.MODID)) {
					hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.0"));
					hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.1"));
					hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
				}
				hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.3"));
				if (getButton(i).getCurrentStack() != null) {
					hover.appendSibling(new TextComponentString("<br>"));
					List<String> list = getButton(i).getCurrentStack().getTooltip(player, player.capabilities.isCreativeMode ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
					for (String str : list) {
						hover.appendSibling(new TextComponentString("<br>" + str));
					}
				}
				drawHoverText(hover.getFormattedText());
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		wait = false;
		data.clear();
		if (onlyMod && !recipe.domen.equals(CustomNpcs.MODID)) { recipe.clear(); }
		for (ResourceLocation loc : CraftingManager.REGISTRY.getKeys()) {
			IRecipe r = CraftingManager.REGISTRY.getObject(loc);
			if (r instanceof INpcRecipe || r instanceof IShapedRecipe || r instanceof ShapelessRecipes) {
				if (onlyMod && !(r instanceof INpcRecipe)) { continue; }
				WrapperRecipe wrapper = new WrapperRecipe();
				wrapper.copyFrom(r, CraftingManager.REGISTRY.getIDForObject(r));
				if (!data.containsKey(wrapper.global)) {
					data.put(wrapper.global, new TreeMap<>());
				}
				if (!data.get(wrapper.global).containsKey(wrapper.group)) {
					data.get(wrapper.global).put(wrapper.group, new ArrayList<>());
				}
				data.get(wrapper.global).get(wrapper.group).add(wrapper);
			}
		}
		data.forEach((k0, v0) -> v0.forEach((k1, v1) -> v1.sort(Comparator.comparing(WrapperRecipe::getName))));
		if (recipe.group.isEmpty() && !data.get(recipe.global).isEmpty()) {
			recipe.clear();
			recipe.group = data.get(recipe.global).values().iterator().next().get(0).group;
		}
		if (!recipe.name.isEmpty()) {
			boolean found = false;
			if (data.get(recipe.global).containsKey(recipe.group) && !data.get(recipe.global).get(recipe.group).isEmpty()) {
				for (WrapperRecipe wr : data.get(recipe.global).get(recipe.group)) {
					if (wr.name.equals(recipe.name)) {
						found = true;
						recipe.copyFrom(wr);
						break;
					}
				}
			}
			if (!found) {
				recipe.name = "";
			}
		}
		if (recipe.name.isEmpty() && data.get(recipe.global).containsKey(recipe.group) && !data.get(recipe.global).get(recipe.group).isEmpty()) { recipe.copyFrom(data.get(recipe.global).get(recipe.group).get(0)); }

		GuiNpcLabel label = new GuiNpcLabel(0, "gui.recipe.groups", guiLeft + 172, guiTop + 8);
		label.setHoverText("recipe.hover.info.groups");
		addLabel(label);
		label = new GuiNpcLabel(1, "gui.recipe.crafts", guiLeft + 294, guiTop + 8);
		label.setHoverText("recipe.hover.info.crafts");
		addLabel(label);
		if (groups == null) { groups = new GuiCustomScroll(this, 0); }
		if (recipes == null) { recipes = new GuiCustomScroll(this, 1); }
		List<String> recipesList = new ArrayList<>();
		List<String> groupsList = new ArrayList<>(data.get(recipe.global).keySet());
		LinkedHashMap<Integer, List<String>> htsG = new LinkedHashMap<>();
		int i = 0;
		for (String groupName : groupsList) {
			String domen = CustomNpcs.MODID;
			String name = "Empty";
			if (!data.get(recipe.global).get(groupName).isEmpty()) {
				domen = data.get(recipe.global).get(groupName).get(0).domen;
				ItemStack stack = data.get(recipe.global).get(groupName).get(0).product;
				name = Objects.requireNonNull(stack.getItem().getRegistryName()).toString() +
						((char) 167) + "7; count: " + ((char) 167) + "6" + stack.getCount() +
						((char) 167) + "7; meta: " + ((char) 167) + "e" + stack.getItemDamage();
				if (stack.hasTagCompound()) {
					name += ((char) 167) + "7; ("+((char) 167) + "dhas NBT" + ((char) 167) + "7)";
				}
			}
			List<String> ht = new ArrayList<>();
			ht.add(((char) 167) + "7Group: " + ((char) 167) + "f" + Util.instance.deleteColor(groupName));
			ht.add(((char) 167) + "7Item: " + ((char) 167) + "f" + name);
			ht.add(((char) 167) + "7Mod: " + ((char) 167) + "b" + domen);
			ht.add(((char) 167) + "7Is global group: " + ((char) 167) + (recipe.global ? "atrue" : "dfalse"));
			htsG.put(i++, ht);
		}
		LinkedHashMap<Integer, List<String>> htsR = new LinkedHashMap<>();
		if (data.get(recipe.global).containsKey(recipe.group)) {
			for (WrapperRecipe wrapper : data.get(recipe.global).get(recipe.group)) {
				recipesList.add(wrapper.name);
				List<String> ht = new ArrayList<>();
				ht.add(((char) 167) + "7Group: " + ((char) 167) + "f" + Util.instance.deleteColor(wrapper.group));
				ht.add(((char) 167) + "7Name: " + ((char) 167) + "f" + Util.instance.deleteColor(wrapper.name));
				ht.add(((char) 167) + "7ID: " + ((char) 167) + "6" + wrapper.id);
				ht.add(((char) 167) + "7Mod: " + ((char) 167) + "b" + wrapper.domen);
				ht.add(((char) 167) + "7Is main product: " + ((char) 167) + (wrapper.main ? "atrue" : "dfalse"));
				ht.add(((char) 167) + "7Is global recipe: " + ((char) 167) + (wrapper.global ? "atrue" : "dfalse"));
				ht.add(((char) 167) + "7Is shaped: " + ((char) 167) + (wrapper.isShaped ? "atrue" : "dfalse"));
				ht.add(((char) 167) + "7Always known: " + ((char) 167) + (wrapper.known ? "atrue" : "dfalse"));
				htsR.put(i++, ht);
			}
		}
		groups.setListNotSorted(groupsList);
		groups.setHoverTexts(htsG);
		groups.setSize(120, 168);
		groups.guiLeft = guiLeft + 172;
		groups.guiTop = guiTop + 20;
		addScroll(groups);
		if (!recipe.group.isEmpty()) { groups.setSelected(recipe.group); }
		recipes.setListNotSorted(recipesList);
		recipes.setHoverTexts(htsR);
		recipes.setSize(120, 168);
		recipes.guiLeft = guiLeft + 294;
		recipes.guiTop = guiTop + 20;
		addScroll(recipes);
		if (!recipe.name.isEmpty()) { recipes.setSelected(recipe.name); }
		int x = guiLeft + 118;
		int y = guiTop + 191;
		boolean hasItem = recipe.isValid() && recipe.domen.equals(CustomNpcs.MODID);
		// Global type
		GuiNpcButton button = new GuiButtonBiDirectional(0, this.guiLeft + 6, y, 163, 20, new String[] { "menu.global", "tile.npccarpentybench.name" }, recipe.global ? 0 : 1);
		button.layerColor = recipe.global ?
				new Color(0x4000FF00).getRGB() :
				new Color(0x400000FF).getRGB();
		button.setHoverText("recipe.hover.type");
		addButton(button);
		// Only mod list
		if (recipe.global) { addButton(new GuiNpcCheckBox(30, guiLeft + 7, guiTop + 97, 95, 12, "gui.recipe.type.true", "gui.recipe.type.false", onlyMod)); }
		// Groups
		button = new GuiNpcButton(1, this.guiLeft + 172, y, 59, 20, "gui.add");
		button.setHoverText("recipe.hover.add.group");
		addButton(button);
		button = new GuiNpcButton(2, this.guiLeft + 234, y, 59, 20, "gui.remove");
		button.setEnabled(groups.hasSelected() && recipe.domen.equals(CustomNpcs.MODID));
		button.setHoverText("recipe.hover.del.group");
		addButton(button);
		// Recipes
		button = new GuiNpcButton(3, this.guiLeft + 294, y, 59, 20, "gui.copy");
		button.setEnabled(!recipe.domen.equals(CustomNpcs.MODID) || recipes.getList().size() < 16);
		button.setHoverText("recipe.hover.add.recipe");
		addButton(button);
		button = new GuiNpcButton(4, this.guiLeft + 356, y, 59, 20, "gui.remove");
		button.setEnabled(recipes.hasSelected() && recipe.domen.equals(CustomNpcs.MODID));
		button.setHoverText("recipe.hover.del.recipe");
		addButton(button);
		// Recipe settings
		if (recipe.domen.equals(CustomNpcs.MODID)) {
			y = guiTop + 4;
			addLabel(new GuiNpcLabel(2, "availability.options", guiLeft + 6, y + 5));

			button = new GuiNpcButton(8, x, y, 50, 20, "selectServer.edit");
			button.setEnabled(hasItem);
			button.setHoverText("availability.hover");
			addButton(button);

			button = new GuiNpcButton(9, x, y += 21, 50, 20, new String[] { "gui.shaped.0", "gui.shaped.1" }, recipe.isShaped ? 1 : 0);
			button.setEnabled(hasItem);
			button.layerColor = hasItem ? recipe.isShaped ? green :
					new Color(0xFF7070FF).getRGB() :
					new Color(0x0).getRGB();
			button.setHoverText("recipe.hover.shared");
			addButton(button);

			button = new GuiNpcButton(7, x, y += 21, 50, 20, new String[] { "gui.known.0", "gui.known.1" }, recipe.known ? 1 : 0);
			button.setEnabled(hasItem);
			button.layerColor = hasItem ? recipe.known ? green : red : 0;
			button.setHoverText("recipe.hover.known");
			addButton(button);

			button = new GuiNpcButton(5, x, y += 21, 50, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, recipe.ignoreDamage ? 0 : 1);
			button.layerColor = hasItem ? recipe.ignoreDamage ? green : red : 0;
			button.setHoverText("recipe.hover.damage");
			addButton(button);

			button =new GuiNpcButton(6, x, y + 21, 50, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, recipe.ignoreNBT ? 0 : 1);
			button.layerColor = hasItem ? recipe.ignoreNBT ? green : red : 0;
			button.setHoverText("recipe.hover.nbt");
			addButton(button);
		}
		// Product
		int craftOffset = recipe.global ? 9 : 0;
		button = new GuiNpcButton(10, guiLeft + 7 + craftOffset + (recipe.global ? 61 : 76), guiTop + 14 + craftOffset + (int) ((recipe.global ? 1.0 : 1.5) * 19.0), 30, 30, "");
		button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 220;
		button.txrY = 96;
		button.txrW = 36;
		button.txrH = 36;
		if (recipe.product.isEmpty()) { button.layerColor = red; }
		button.setEnabled(recipe.domen.equals(CustomNpcs.MODID) && recipe.isValid());
		if (!recipe.main) { button.layerColor = new Color(0xFFA0A0A0).getRGB(); }
		button.setStacks(recipe.product);
		ITextComponent hover = new TextComponentTranslation("recipe.hover.product");
		if (recipe.domen.equals(CustomNpcs.MODID)) {
			if (!recipe.main) { hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.4")); }
			hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.1"));
			hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
		}
		hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.3"));
		if (recipe.product != null) {
			hover.appendSibling(new TextComponentString("<br>"));
			List<String> list = recipe.product.getTooltip(player, player.capabilities.isCreativeMode ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
			for (String str : list) { hover.appendSibling(new TextComponentString("<br>" + str)); }
		}
		button.setHoverText(hover.getFormattedText());
		addButton(button);
		// Craft grid
		// set buttons
		int s = recipe.global ? 3 : 4;
		for (int h = 0; h < s; ++h) {
			for (int w = 0; w < s; ++w) {
				int id = 11 + w + h * s;
				button = new GuiNpcButton(id, guiLeft + craftOffset + w * 19 + 7, guiTop + craftOffset + h * 19 + 20, 18, 18, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.isAnim = true;
				button.txrX = 220;
				button.txrY = 96;
				button.txrW = 36;
				button.txrH = 36;
				button.setEnabled(recipe.domen.equals(CustomNpcs.MODID) && recipe.isValid());
				if (recipe.domen.equals(CustomNpcs.MODID)) { button.layerColor = recipe.isValid() ? green : red; }
				addButton(button);
			}
		}
		// set recipe
		for (int w = 0; w < recipe.width; ++w) {
			for (int h = 0; h < recipe.height; ++h) {
				int id = 11 + h * recipe.height + w;
				int slotID = h * recipe.width + w;
				ItemStack[] stacks = recipe.recipeItems.get(slotID);
				button = (GuiNpcButton) getButton(id);
				button.setStacks(stacks);
				if (recipe.domen.equals(CustomNpcs.MODID)) { button.layerColor = recipe.isValid() ? (stacks != null && stacks.length > 0) ? 0 : green : red; }
			}
		}
		// Clear
		button = new GuiNpcButton(28, guiLeft + 92, guiTop + 77, 18, 18, "");
		button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		if (!recipe.isValid() || !(recipe.parent instanceof INpcRecipe) || !recipe.domen.equals(CustomNpcs.MODID)) { return; }
		Client.sendData(EnumPacketServer.RecipeSave, recipe.getNbt());
		wait = true;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) { // Group
			if (recipe.group.equals(groups.getSelected()) && !data.get(recipe.global).containsKey(groups.getSelected())) { return; }
			this.save();
			recipe.clear();
			recipe.group = groups.getSelected();
        } else { // Recipe
			if (recipe.name.equals(recipes.getSelected()) && !data.get(recipe.global).containsKey(recipe.group)) { return; }
			for (WrapperRecipe wrapper : data.get(recipe.global).get(recipe.group)) {
				if (wrapper.name.equals(recipes.getSelected())) {
					this.save();
					recipe.copyFrom(wrapper);
					break;
				}
			}
        }
        initGui();
    }

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: { // rename Group
				this.setSubGui(new SubGuiEditText(2, new String[] { selection }));
				break;
			}
			case 1: { // rename Recipe
				this.setSubGui(new SubGuiEditText(3, new String[] { selection }));
				break;
			}
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcAvailability) {
			this.save();
		}
		else if (subgui instanceof SubGuiEditIngredients) {
			ItemStack[] stacks = new ItemStack[0];
			if (((SubGuiEditIngredients) subgui).stacks != null) {
				List<ItemStack> list = new ArrayList<>();
				for (ItemStack stack : ((SubGuiEditIngredients) subgui).stacks) {
					if (stack.isEmpty()) { continue; }
					list.add(stack);
				}
				if (!list.isEmpty()) { stacks = list.toArray(stacks); }
			}
			if (getButton(11 + subgui.getId()) != null) {
				GuiNpcButton button = (GuiNpcButton) getButton(11 + subgui.getId());
				button.setStacks(stacks);
				button.setCurrentStackPos(0);
				if (stacks.length == 0) {
					button.layerColor = recipe.isValid() ?
							new Color(0xFF70F070).getRGB() :
							new Color(0xFFF07070).getRGB();
				}
			}
			recipe.recipeItems.put(subgui.getId(), stacks);
		}
		else if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) {
				return;
			}
			if (subgui.getId() == 0) { // Add new Group
				this.save();
				recipe.clear();
				recipe.group = Util.instance.getResourceName(((SubGuiEditText) subgui).text[0]);
				recipe.name = "default";
				Client.sendData(EnumPacketServer.RecipesAddGroup, recipe.global, recipe.group);
			} else if (subgui.getId() == 1) { // Add new Recipe
				this.save();
				String name = ((SubGuiEditText) subgui).text[0];
				while (true) {
					boolean found = false;
					for (WrapperRecipe wr : data.get(recipe.global).get(recipe.group)) {
						if (wr.name.equals(name)) {
							name = name+ "_";
							found = true;
							break;
						}
					}
					if (!found) { break; }
				}
				recipe.name = name;
				Client.sendData(EnumPacketServer.RecipeAdd, recipe.getNbt());
			} else if (subgui.getId() == 2) { // Rename Group
				String old = recipe.group;
				recipe.group = Util.instance.getResourceName(((SubGuiEditText) subgui).text[0]);
				Client.sendData(EnumPacketServer.RecipesRenameGroup, recipe.global, old, recipe.group);
			} else if (subgui.getId() == 3) { // Rename Recipe
				String old = recipe.name;
				recipe.name = Util.instance.getResourceName(((SubGuiEditText) subgui).text[0]);
				Client.sendData(EnumPacketServer.RecipesRename, recipe.global, old, recipe.group, recipe.name);
			} else if (subgui.getId() == 4) { // Copy vanilla Recipe
				String group = ((SubGuiEditText) subgui).text[0];
				if (data.get(recipe.global).containsKey(group) && data.get(recipe.global).get(group).size() >= 16) { return; }
				recipe.group = Util.instance.getResourceName(group);
				String name = Util.instance.getResourceName(((SubGuiEditText) subgui).text[1]);
				while (data.get(recipe.global).containsKey(recipe.group)) {
					boolean found = false;
					for (WrapperRecipe wr : data.get(recipe.global).get(recipe.group)) {
						if (wr.name.equals(name)) {
							name = name+ "_";
							found = true;
							break;
						}
					}
					if (!found) { break; }
				}
				recipe.name = name;
				Client.sendData(EnumPacketServer.RecipeAdd, recipe.getNbt());
			} else { return; }
			wait = true;
		}
	}

}
