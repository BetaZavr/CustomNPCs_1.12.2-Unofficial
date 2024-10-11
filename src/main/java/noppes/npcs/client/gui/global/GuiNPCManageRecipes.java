package noppes.npcs.client.gui.global;

import java.util.*;

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
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCManageRecipes
extends GuiContainerNPCInterface2
implements ICustomScrollListener, ISubGuiListener {

	private static boolean onlyMod = true;
	private static final WrapperRecipe recipe = new WrapperRecipe();

    private final Map<Boolean, Map<String, List<WrapperRecipe>>> data = new HashMap<>(); // <isGlobal, <Group, recipe data>>
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
	protected void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 2) {
			ItemStack heldStack = player.inventory.getItemStack();
			if (heldStack.isEmpty()) {
				ItemStack stack = button.currentStack.copy();
				stack.setCount(stack.getMaxStackSize());
				player.inventory.setItemStack(stack);
				Client.sendData(EnumPacketServer.SetItem, stack.writeToNBT(new NBTTagCompound()));
			}
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id >= 10 && button.id < 27) {
			if (!recipe.domen.equals(CustomNpcs.MODID)) { return; }
			// show list of ingredients
			if (button.id != 10 && isShiftKeyDown()) {
				if (recipe.recipeItems.get(button.id - 11).length > 0) {
					this.setSubGui(new SubGuiEditIngredients(button.id - 11, recipe.recipeItems.get(button.id - 11)));
				}
				return;
			}
			ItemStack heldStack = player.inventory.getItemStack();
			// product
			if (button.id == 10) {
				if (isAltKeyDown()) { recipe.product.setCount(1); }
				else if (recipe.product.isEmpty()) {
					if (!heldStack.isEmpty()) {
						recipe.product = heldStack.copy();
					}
				}
				else {
					if (heldStack.isEmpty()) { recipe.product.setCount(Math.max(1, recipe.product.getCount() - 1)); } // -1
					else if (NoppesUtilPlayer.compareItems(recipe.product, heldStack.copy(), false, false)) { // +N
						recipe.product.setCount(Math.min(recipe.product.getMaxStackSize(), recipe.product.getCount() + heldStack.getCount()));
					}
					else { recipe.product = heldStack.copy(); } // replace
				}
			}
			// ingredient
			else {
				int pos = button.id - 11;
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
					if (button.currentStackID < array.length) {
						array[button.currentStackID].setCount(1);
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
						int p = button.currentStackID;
						int count = Math.max(0, array[p].getCount() - 1);
						if (count > 0) { array[p].setCount(count); }
						else { array = new ItemStack[0]; }
						button.setStacks(array);
						button.setCurrentStackPos(p);
					} else {
						for (int i = 0; i < array.length; i++) {
							System.out.println("CNPCs: " + button.id + "/" + array.length);
							if (!array[i].isEmpty() && NoppesUtilPlayer.compareItems(array[i], heldStack, false, false)) {
								// +N
								array[i].setCount(Math.min(array[i].getMaxStackSize(), array[i].getCount() + heldStack.getCount()));
								button.setStacks(array);
								button.setCurrentStackPos(i);
								break;
							}
						}
					}
					recipe.recipeItems.put(pos, array);
				}
			}
			initGui();
			return;
		}
		switch (button.id) {
			case 0: { // global type
				this.save();
				recipe.clear();
				recipe.global = button.getValue() == 0;
				int size = recipe.global ? 3 : 4;
				recipe.width = size;
				recipe.height = size;
				initGui();
				break;
			}
			case 1: { // Add Group
				this.setSubGui(new SubGuiEditText(0, new String[] { recipe.group }));
				break;
			}
			case 2: { // Del Group
				Client.sendData(EnumPacketServer.RecipeRemoveGroup, recipe.global, recipe.group);
				recipe.clear();
				wait = true;
				break;
			}
			case 3: { // Add Recipe
				this.setSubGui(new SubGuiEditText(1, new String[] { recipe.name }));
				break;
			}
			case 4: { // Del Recipe
				Client.sendData(EnumPacketServer.RecipeRemove, recipe.global, recipe.group, recipe.name);
				recipe.name = "";
				wait = true;
				break;
			}
			case 5: { // ignore Meta
				recipe.ignoreDamage = button.getValue() == 1;
				initGui();
				break;
			}
			case 6: { // ignore NBT
				recipe.ignoreNBT = button.getValue() == 1;
				initGui();
				break;
			}
			case 7: { // know
				recipe.known = button.getValue() == 1;
				initGui();
				break;
			}
			case 8: { // availability
				setSubGui(new SubGuiNpcAvailability(recipe.availability));
				break;
			}
			case 9: { // replace shaped <-> shapeless
				recipe.isShaped = !recipe.isShaped;
				initGui();
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
				onlyMod = !onlyMod;
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
		if (this.subgui != null) {
			return;
		}
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (getLabel(0) != null && getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.info.groups").getFormattedText());
		} else if (getLabel(1) != null && getLabel(1).hovered) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.info.crafts").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.type").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).visible && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.add.group").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).visible && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.del.group").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).visible && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.add.recipe").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).visible && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.del.recipe").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).visible && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.damage").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).visible && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.nbt").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).visible && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.known").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).visible && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).visible && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.shared").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).visible && this.getButton(10).isMouseOver()) {
			ITextComponent hover = new TextComponentTranslation("recipe.hover.product");
			if (recipe.domen.equals(CustomNpcs.MODID)) {
				hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.1"));
				hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
			}
			hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.3"));
			if (getButton(10).currentStack != null) {
				hover.appendSibling(new TextComponentString("<br>"));
				List<String> list = getButton(10).currentStack.getTooltip(player, player.capabilities.isCreativeMode ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
				for (String str : list) {
					hover.appendSibling(new TextComponentString("<br>" + str));
				}
			}
			this.setHoverText(hover.getFormattedText());
		} else if (this.getButton(30) != null && this.getButton(30).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.only.mod").getFormattedText());
		} else {
			for (int i = 11; i < 27; i++) {
				if (this.getButton(i) != null && this.getButton(i).visible && this.getButton(i).isMouseOver()) {
					ITextComponent hover = new TextComponentTranslation("recipe.hover.ingredients", "" + (i - 11));
					if (recipe.domen.equals(CustomNpcs.MODID)) {
						hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.0"));
						hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.1"));
						hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
					}
					hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.3"));
					if (getButton(i).currentStack != null) {
						hover.appendSibling(new TextComponentString("<br>"));
						List<String> list = getButton(i).currentStack.getTooltip(player, player.capabilities.isCreativeMode ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
						for (String str : list) {
							hover.appendSibling(new TextComponentString("<br>" + str));
						}
					}
					this.setHoverText(hover.getFormattedText());
					break;
				}
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		wait = false;

		data.clear();
		if (onlyMod && !recipe.domen.equals(CustomNpcs.MODID)) {
			boolean isGlobal = recipe.global;
			recipe.clear();
			recipe.global = isGlobal;
		}
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
				if (recipe.global == wrapper.global) {
					if (recipe.group.isEmpty()) {
						recipe.copyFrom(wrapper);
					} else if (recipe.group.equals(wrapper.group) && recipe.name.isEmpty()) {
						recipe.copyFrom(wrapper);
					}
				}
			}
		}

		this.addLabel(new GuiNpcLabel(0, "gui.recipe.groups", guiLeft + 172, guiTop + 8));
		this.addLabel(new GuiNpcLabel(1, "gui.recipe.crafts", guiLeft + 294, guiTop + 8));
		if (groups == null) { groups = new GuiCustomScroll(this, 0); }
		if (recipes == null) { recipes = new GuiCustomScroll(this, 1); }

		List<String> recipesList = new ArrayList<>();
		List<String> groupsList = new ArrayList<>(data.get(recipe.global).keySet());
		List<String[]> groupsHoverList = new ArrayList<>();
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
			String[] hover = new String[] {
					((char) 167) + "7Group: " + ((char) 167) + "f" + groupName,
					((char) 167) + "7Item: " + ((char) 167) + "f" + name,
					((char) 167) + "7Mod: " + ((char) 167) + "b" + domen,
					((char) 167) + "7Is global group: " + ((char) 167) + (recipe.global ? "atrue" : "dfalse")
			};
			groupsHoverList.add(hover);
		}

		List<String[]> recipesHoverList = new ArrayList<>();
		if (data.get(recipe.global).containsKey(recipe.group)) {
			for (WrapperRecipe wrapper : data.get(recipe.global).get(recipe.group)) {
				recipesList.add(wrapper.name);
				String[] hover = new String[] {
						((char) 167) + "7Group: " + ((char) 167) + "f" + wrapper.group,
						((char) 167) + "7Name: " + ((char) 167) + "f" + wrapper.name,
						((char) 167) + "7ID: " + ((char) 167) + "6" + wrapper.id,
						((char) 167) + "7Mod: " + ((char) 167) + "b" + wrapper.domen,
						((char) 167) + "7Is global recipe: " + ((char) 167) + (wrapper.global ? "atrue" : "dfalse"),
						((char) 167) + "7Is shaped: " + ((char) 167) + (wrapper.isShaped ? "atrue" : "dfalse"),
						((char) 167) + "7Always known: " + ((char) 167) + (wrapper.known ? "atrue" : "dfalse")
				};
				recipesHoverList.add(hover);
			}
		}

		groups.setListNotSorted(groupsList);
		groups.setSize(120, 168);
		groups.guiLeft = guiLeft + 172;
		groups.guiTop = guiTop + 20;
		addScroll(groups);
		if (!recipe.group.isEmpty()) { groups.setSelected(recipe.group); }
		if (groupsHoverList.isEmpty()) { groups.hoversTexts = null; }
		else {
			groups.hoversTexts = new String[groupsHoverList.size()][];
			for (int i = 0; i < groupsHoverList.size(); i++) {
				groups.hoversTexts[i] = groupsHoverList.get(i);
			}
		}

		recipes.setListNotSorted(recipesList);
		recipes.setSize(120, 168);
		recipes.guiLeft = guiLeft + 294;
		recipes.guiTop = guiTop + 20;
		addScroll(recipes);
		if (!recipe.name.isEmpty()) { recipes.setSelected(recipe.name); }
		if (recipesHoverList.isEmpty()) { recipes.hoversTexts = null; }
		else {
			recipes.hoversTexts = new String[recipesHoverList.size()][];
			for (int i = 0; i < recipesHoverList.size(); i++) {
				recipes.hoversTexts[i] = recipesHoverList.get(i);
			}
		}

		int y = guiTop + 191;
		GuiButtonBiDirectional type = new GuiButtonBiDirectional(0, this.guiLeft + 6, y, 163, 20, new String[] { "menu.global", "tile.npccarpentybench.name" }, recipe.global ? 0 : 1);
		type.layerColor = recipe.global ? 0x4000FF00 : 0x400000FF;
		this.addButton(type);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 172, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 234, y, 59, 20, "gui.remove"));
		this.getButton(2).setEnabled(groups.hasSelected());

		this.addButton(new GuiNpcButton(3, this.guiLeft + 294, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 356, y, 59, 20, "gui.remove"));
		this.getButton(4).setEnabled(recipes.hasSelected());

		int x = guiLeft + 118;
		y = guiTop + 4;
		this.addLabel(new GuiNpcLabel(2, "availability.options", guiLeft + 6, y + 5));
		this.addButton(new GuiNpcButton(8, x, y, 50, 20, "selectServer.edit"));
		this.addButton(new GuiNpcButton(9, x, y += 21, 50, 20, new String[] { "gui.shaped.0", "gui.shaped.1" }, recipe.isShaped ? 1 : 0));
		this.addButton(new GuiNpcButton(7, x, y += 21, 50, 20, new String[] { "gui.known.0", "gui.known.1" }, recipe.known ? 1 : 0));
		this.addButton(new GuiNpcButton(5, x, y += 21, 50, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, recipe.ignoreDamage ? 1 : 0));
		this.addButton(new GuiNpcButton(6, x, y + 21, 50, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, recipe.ignoreNBT ? 1 : 0));

		this.getButton(5).setVisible(recipe.isValid());
		this.getButton(6).setVisible(recipe.isValid());
		this.getButton(7).setVisible(recipe.isValid());
		this.getLabel(2).enabled = recipe.isValid();
		this.getButton(8).setVisible(recipe.isValid());
		this.getButton(9).setVisible(recipe.isValid());
		boolean hasItem = recipe.isValid() && recipe.domen.equals(CustomNpcs.MODID);
		int green = 0xFF70F070;
		int red = 0xFFF07070;

		this.getButton(3).setEnabled(hasItem);
		this.getButton(3).layerColor = hasItem ? green : 0;
		this.getButton(5).setEnabled(hasItem);
		this.getButton(5).layerColor = hasItem ? recipe.ignoreDamage ? green : red : 0;
		this.getButton(6).setEnabled(hasItem);
		this.getButton(6).layerColor = hasItem ? recipe.ignoreNBT ? green : red : 0;
		this.getButton(7).setEnabled(hasItem);
		this.getButton(7).layerColor = hasItem ? recipe.known ? green : red : 0;
		this.getButton(8).setEnabled(hasItem);
		this.getButton(9).setEnabled(hasItem);
		this.getButton(9).layerColor = hasItem ? recipe.isShaped ? green : 0xFF7070FF : 0;

		int craftOffset = recipe.global ? 9 : 0;
		GuiNpcButton button = new GuiNpcButton(10, guiLeft + 7 + craftOffset + (recipe.global ? 61 : 76), guiTop + 14 + craftOffset + (int) ((recipe.global ? 1.0 : 1.5) * 19.0), 30, 30, "");
		button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.txrX = 220;
		button.txrY = 96;
		button.txrW = 36;
		button.txrH = 36;
		if (recipe.isValid() && recipe.product.isEmpty()) { button.layerColor = red; }
		button.setEnabled(recipe.domen.equals(CustomNpcs.MODID) && recipe.isValid());
		button.setStacks(recipe.product);
		addButton(button);

		int s = recipe.global ? 3 : 4;
		for (int i = 0; i < s; ++i) {
			for (int j = 0; j < s; ++j) {
				int id = 11 + j + i * s;
				button = new GuiNpcButton(id, guiLeft + craftOffset + j * 19 + 7, guiTop + craftOffset + i * 19 + 20, 18, 18, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.txrX = 220;
				button.txrY = 96;
				button.txrW = 36;
				button.txrH = 36;
				button.setEnabled(recipe.domen.equals(CustomNpcs.MODID) && recipe.isValid());
				id -= 11;
				button.setStacks(recipe.recipeItems.get(id));
				if (recipe.domen.equals(CustomNpcs.MODID)) {
					if (!recipe.recipeItems.containsKey(id) || recipe.recipeItems.get(id).length ==0) {
						button.layerColor = recipe.isValid() ? green : red;
					}
				}
				addButton(button);
			}
		}
		button = new GuiNpcButton(28, guiLeft + 92, guiTop + 77, 18, 18, "");
		button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);

		addButton(new GuiNpcCheckBox(30, guiLeft + 7, guiTop + 97, 95, 12, "gui.recipe.type." + onlyMod, onlyMod));
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
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.id == 0) { // Group
			if (recipe.group.equals(groups.getSelected()) && !data.get(recipe.global).containsKey(groups.getSelected())) { return; }
			this.save();
			boolean isGlobal = recipe.global;
			recipe.clear();
			recipe.group = groups.getSelected();
			recipe.global = isGlobal;
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
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		switch (scroll.id) {
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
	public void subGuiClosed(SubGuiInterface subgui) {
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
			if (getButton(11 + subgui.id) != null) {
				GuiNpcButton button = getButton(11 + subgui.id);
				button.setStacks(stacks);
				button.setCurrentStackPos(0);
				if (stacks.length == 0) {
					button.layerColor = recipe.isValid() ? 0xFF70F070 : 0xFFF07070;
				}
			}
			recipe.recipeItems.put(subgui.id, stacks);
		}
		else if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) {
				return;
			}
			if (subgui.id == 0) { // Add new Group
				this.save();
				recipe.clear();
				recipe.group = ((SubGuiEditText) subgui).text[0];
				recipe.name = "default";
				Client.sendData(EnumPacketServer.RecipesAddGroup, recipe.global, recipe.group);
			} else if (subgui.id == 1) { // Add new Recipe
				this.save();
				String baseGroup = recipe.group;
				recipe.clear();
				recipe.group = baseGroup;
				recipe.name = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipeAdd, recipe.global, recipe.group, recipe.name);
			} else if (subgui.id == 2) { // Rename Group
				String old = recipe.group;
				recipe.group = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRenameGroup, recipe.global, old, recipe.group);
			} else if (subgui.id == 3) { // Rename Recipe
				String old = recipe.name;
				recipe.name = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRename, recipe.global, old, recipe.group, recipe.name);
			} else { return; }
			wait = true;
		}
	}

}
