package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
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
			if (isShiftKeyDown()) {
				this.setSubGui(new SubGuiEditText(button.id, new String[] { recipe.group }));
				return;
			}
			ItemStack heldStack = player.inventory.getItemStack();
			if (button.id == 10) {
				if (recipe.product.isEmpty()) { recipe.product = heldStack.copy(); }
				else {
					if (heldStack.isEmpty()) {
						recipe.product.setCount(Math.max(1, recipe.product.getCount() - 1));
					}
					else if (NoppesUtilPlayer.compareItems(recipe.product, heldStack.copy(), false, false)) {
						recipe.product.setCount(Math.min(recipe.product.getMaxStackSize(), recipe.product.getCount() + heldStack.getCount()));
					}
					else { recipe.product = heldStack.copy(); }
				}
			}
			else {
				int pos = button.id - 11;
				ItemStack[] array = recipe.recipeItems.get(pos);
				if (array == null || array.length == 0) {
					recipe.recipeItems.put(pos, new ItemStack[] { heldStack.copy() });
				} else {
					boolean found = false;
					for (ItemStack stack : array) {
						if (stack != null && NoppesUtilPlayer.compareItems(stack, heldStack, false, false)) {
							found = true;
							break;
						}
					}
					if (found) {
						// show stacks list
					} else {
						List<ItemStack> list = Arrays.asList(array);
						list.add(heldStack.copy());
						recipe.recipeItems.put(pos, list.toArray(new ItemStack[0]));
					}
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
			}
			hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
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
					}
					hover.appendSibling(new TextComponentTranslation("recipe.hover.ingredient.2"));
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
		if (data.get(recipe.global).containsKey(recipe.group)) {
			for (WrapperRecipe wrapper : data.get(recipe.global).get(recipe.group)) {
				recipesList.add(wrapper.name);
			}
		}

		groups.setListNotSorted(groupsList);
		groups.setSize(120, 168);
		groups.guiLeft = guiLeft + 172;
		groups.guiTop = guiTop + 20;
		addScroll(groups);
		if (!recipe.group.isEmpty()) { groups.setSelected(recipe.group); }

		recipes.setListNotSorted(recipesList);
		recipes.setSize(120, 168);
		recipes.guiLeft = guiLeft + 294;
		recipes.guiTop = guiTop + 20;
		addScroll(recipes);
		if (!recipe.name.isEmpty()) { recipes.setSelected(recipe.name); }

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

		this.addButton(new GuiNpcButton(5, this.guiLeft + 114, this.guiTop + 68, 50, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, recipe.ignoreDamage ? 1 : 0));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 114, this.guiTop + 90, 50, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, recipe.ignoreNBT ? 1 : 0));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 114, this.guiTop + 46, 50, 20, new String[] { "gui.known.0", "gui.known.1" }, recipe.known ? 1 : 0));
		this.addLabel(new GuiNpcLabel(2, "availability.options", this.guiLeft + 6, this.guiTop + 9));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 114, this.guiTop + 4, 50, 20, "selectServer.edit"));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 114, this.guiTop + 25, 50, 20, new String[] { "gui.shaped.0", "gui.shaped.1" }, recipe.isShaped ? 1 : 0));

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
		} else if (subgui instanceof SubGuiEditText) {
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
