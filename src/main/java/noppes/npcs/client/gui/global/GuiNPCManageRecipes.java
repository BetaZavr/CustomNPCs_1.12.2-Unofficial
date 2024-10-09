package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;

public class GuiNPCManageRecipes
extends GuiContainerNPCInterface2
implements ICustomScrollListener, ISubGuiListener {

    private final List<String> dataGroup = new ArrayList<>();
	private final List<String> dataRecipe = new ArrayList<>();
	private GuiCustomScroll groups;
	private GuiCustomScroll recipes;
	private INpcRecipe recipe = null;
	private String selGroup = "default_group";
	private String selName = "default_name";
	private boolean isGlobal = true;
	private boolean wait = false;

	public GuiNPCManageRecipes(EntityNPCInterface npc, ContainerManageRecipes container) {
		super(npc, container);
        drawDefaultBackground = false;
		setBackground("inventorymenu.png");
		ySize = 200;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id >= 10) {
			ItemStack holdStack = player.inventory.getCurrentItem();
			List<ItemStack> list = button.getStacks();
			System.out.println("CNPCs: "+holdStack);
			System.out.println("CNPCs: "+list);
			return;
		}
		switch (button.id) {
			case 0: { // global type
				this.save();
				isGlobal = button.getValue() == 0;
				selGroup = "default_group";
				selName = "default_name";
				initGui();
				break;
			}
			case 1: { // Add Group
				this.setSubGui(new SubGuiEditText(0, new String[] { selGroup }));
				break;
			}
			case 2: { // Del Group
				Client.sendData(EnumPacketServer.RecipeRemoveGroup, isGlobal, selGroup);
				selGroup = "default_group";
				selName = "default_name";
				wait = true;
				break;
			}
			case 3: { // Add Recipe
				this.setSubGui(new SubGuiEditText(1, new String[] { selName }));
				break;
			}
			case 4: { // Del Recipe
				Client.sendData(EnumPacketServer.RecipeRemove, isGlobal, selGroup, selName);
				selName = "default_name";
				wait = true;
				break;
			}
			case 5: { // ignore Meta
				recipe.setIgnoreDamage(button.getValue() == 1);
				initGui();
				break;
			}
			case 6: { // ignore NBT
				recipe.setIgnoreNBT(button.getValue() == 1);
				initGui();
				break;
			}
			case 7: { // know
				recipe.setKnown(button.getValue() == 1);
				initGui();
				break;
			}
			case 8: { // availability
				setSubGui(new SubGuiNpcAvailability((Availability) recipe.getAvailability()));
				break;
			}
			case 9: { // replace shaped <-> shapeless
				INpcRecipe rec;
				if (recipe.isShaped()) {
					NpcShapedRecipes recipe = (NpcShapedRecipes) this.recipe;
					rec = new NpcShapelessRecipes(recipe.getNpcGroup(), recipe.name, recipe.isGlobal(), recipe.recipeItems, recipe.getRecipeOutput());
					((NpcShapelessRecipes) rec).known = recipe.known;
					((NpcShapelessRecipes) rec).availability = recipe.availability;
					((NpcShapelessRecipes) rec).ignoreDamage = recipe.ignoreDamage;
					((NpcShapelessRecipes) rec).ignoreNBT = recipe.ignoreNBT;
					((NpcShapelessRecipes) rec).savesRecipe = recipe.savesRecipe;
				} else {
					NpcShapelessRecipes recipe = (NpcShapelessRecipes) this.recipe;
					rec = new NpcShapedRecipes(recipe.getNpcGroup(), recipe.name, recipe.isGlobal(), recipe.recipeItems, recipe.getRecipeOutput());
					((NpcShapedRecipes) rec).known = recipe.known;
					((NpcShapedRecipes) rec).availability = recipe.availability;
					((NpcShapedRecipes) rec).ignoreDamage = recipe.ignoreDamage;
					((NpcShapedRecipes) rec).ignoreNBT = recipe.ignoreNBT;
					((NpcShapedRecipes) rec).savesRecipe = recipe.savesRecipe;
				}
				this.recipe = rec;
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
	public void drawScreen(int i, int j, float f) {
		if (wait) {
			drawWait();
			return;
		}
		super.drawScreen(i, j, f);
		if (this.subgui != null) {
			return;
		}
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (isMouseHover(i, j, this.guiLeft + 172, this.guiTop + 8, 120, 10)) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.info.groups").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 172, this.guiTop + 8, 120, 10)) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.info.crafts").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 191, 120, 20)) {
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
		if (recipe != null && recipe.isGlobal() != isGlobal) { recipe = null; }

		RecipeController rData = RecipeController.getInstance();
		dataGroup.clear();
		dataRecipe.clear();

		Map<String, List<INpcRecipe>> data = rData.getRecipes(isGlobal);
        dataGroup.addAll(data.keySet());
		if (selGroup.isEmpty() || !data.containsKey(selGroup)) {
			for (String group : data.keySet()) {
				selGroup = group;
				selName = "default_name";
				break;
			}
		}
		if (!data.containsKey(selGroup)) { selName = "default_name"; }
		else {
			for (INpcRecipe rec : data.get(selGroup)) {
				if (rec == null) { continue; }
				dataRecipe.add(rec.getName());
				break;
			}
			if (!selName.isEmpty()) {
				boolean found = false;
				for (INpcRecipe rec : data.get(selGroup)) {
					if (rec == null || !rec.getName().equals(selName)) { continue; }
					found = true;
					break;
				}
				if (!found) { selName = "default_name"; }
			}
		}
		if (data.containsKey(selGroup) && selName.isEmpty()) {
			for (INpcRecipe rec : data.get(selGroup)) {
				if (rec == null) { continue; }
				selName = rec.getName();
				break;
			}
		}
		recipe = null;
		if (dataGroup.contains(selGroup) && dataRecipe.contains(selName)) {
			for (INpcRecipe rec : data.get(selGroup)) {
				if (rec == null || !rec.getName().equals(selName)) { continue; }
				recipe = rec;
				break;
			}
		}

		this.addLabel(new GuiNpcLabel(0, "gui.recipe.groups", guiLeft + 172, guiTop + 8));
		this.addLabel(new GuiNpcLabel(1, "gui.recipe.crafts", guiLeft + 294, guiTop + 8));
		if (groups == null) { groups = new GuiCustomScroll(this, 0); }
		if (recipes == null) { recipes = new GuiCustomScroll(this, 1); }
		groups.setListNotSorted(dataGroup);
		groups.setSize(120, 168);
		groups.guiLeft = guiLeft + 172;
		groups.guiTop = guiTop + 20;
		addScroll(groups);
		if (!selGroup.isEmpty()) { groups.setSelected(selGroup); }

		recipes.setListNotSorted(dataRecipe);
		recipes.setSize(120, 168);
		recipes.guiLeft = guiLeft + 294;
		recipes.guiTop = guiTop + 20;
		addScroll(recipes);
		if (!selName.isEmpty()) { recipes.setSelected(selName); }

		int y = guiTop + 191;
		GuiButtonBiDirectional type = new GuiButtonBiDirectional(0, this.guiLeft + 6, y, 163, 20, new String[] { "menu.global", "tile.npccarpentybench.name" }, isGlobal ? 0 : 1);
		type.layerColor = isGlobal ? 0x4000FF00 : 0x400000FF;
		this.addButton(type);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 172, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 234, y, 59, 20, "gui.remove"));
		this.getButton(2).setEnabled(groups.hasSelected());

		this.addButton(new GuiNpcButton(3, this.guiLeft + 294, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 356, y, 59, 20, "gui.remove"));
		this.getButton(4).setEnabled(recipes.hasSelected());

		this.addButton(new GuiNpcButton(5, this.guiLeft + 114, this.guiTop + 68, 50, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, recipe != null && recipe.getIgnoreDamage() ? 1 : 0));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 114, this.guiTop + 90, 50, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, recipe != null && recipe.getIgnoreNBT() ? 1 : 0));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 114, this.guiTop + 46, 50, 20, new String[] { "gui.known.0", "gui.known.1" }, recipe != null && recipe.isKnown() ? 1 : 0));
		this.addLabel(new GuiNpcLabel(2, "availability.options", this.guiLeft + 6, this.guiTop + 9));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 114, this.guiTop + 4, 50, 20, "selectServer.edit"));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 114, this.guiTop + 25, 50, 20, new String[] { "gui.shaped.0", "gui.shaped.1" }, recipe != null && recipe.isShaped() ? 1 : 0));
		this.getButton(5).setVisible(recipe != null);
		this.getButton(6).setVisible(recipe != null);
		this.getButton(7).setVisible(recipe != null);
		this.getLabel(2).enabled = recipe != null;
		this.getButton(8).setVisible(recipe != null);
		this.getButton(9).setVisible(recipe != null);
		boolean hasItem = recipe != null;
		int green = 0xFF70F070;
		int red = 0xFFF07070;

		this.getButton(3).setEnabled(hasItem);
		this.getButton(3).layerColor = hasItem ? green : 0;
		this.getButton(5).setEnabled(hasItem);
		this.getButton(5).layerColor = hasItem ? recipe != null && recipe.getIgnoreDamage() ? green : red : 0;
		this.getButton(6).setEnabled(hasItem);
		this.getButton(6).layerColor = hasItem ? recipe != null && recipe.getIgnoreNBT() ? green : red : 0;
		this.getButton(7).setEnabled(hasItem);
		this.getButton(7).layerColor = hasItem ? recipe != null && recipe.isKnown() ? green : red : 0;
		this.getButton(8).setEnabled(hasItem);
		this.getButton(9).setEnabled(hasItem);
		this.getButton(9).layerColor = hasItem ? recipe != null && recipe.isShaped() ? green : 0xFF7070FF : 0;


		if (isGlobal) {
			GuiNpcLabel label = new GuiNpcLabel(3, new TextComponentTranslation("gui.recipe.hover.cursor.name").getFormattedText(), this.guiLeft + 9, this.guiTop + 94);
			label.backColor = 0x40FF0000;
			label.borderColor = 0x80808080;
			label.color = 0xFF000000;
			label.hoverText = new String[] { new TextComponentTranslation("gui.recipe.hover.cursor.info").getFormattedText() };
			this.addLabel(label);
		}

		int size = (isGlobal ? 3 : 4);
		Map<Integer, List<ItemStack>> map = new TreeMap<>();
		if (recipe != null) {
			IItemStack[][] stacks = recipe.getRecipe();
			for (int id = 0; id < stacks.length; ++id) {
				if (!map.containsKey(id)) { map.put(id, new ArrayList<>()); }
				for (IItemStack iStack : stacks[id]) { map.get(id).add(iStack.getMCItemStack()); }
			}
		}

		GuiNpcButton button = new GuiNpcButton(10, guiLeft + 7 + (int) ((isGlobal ? 3.35 : 4.35) * 19.0), guiTop + 26, 18, 18, "");
		button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.txrX = 220;
		button.txrY = 96;
		button.txrW = 36;
		button.txrH = 36;
		if (recipe == null || recipe.getProduct().isEmpty()) { button.layerColor = red; }
		addButton(button);

		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				int id = 11 + j + i * size;
				button = new GuiNpcButton(id, guiLeft + i * 19 + 7, guiTop + j * 19 + 26, 18, 18, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.txrX = 220;
				button.txrY = 96;
				button.txrW = 36;
				button.txrH = 36;
				if (recipe == null || !map.containsKey(id)) { button.layerColor = red; }
				if (map.containsKey(id)) {
					if (map.get(id).isEmpty()) { button.layerColor = green; }
					button.setStacks(map.get(id));
				}
				addButton(button);
			}
		}
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
		if (recipe != null) {
			Client.sendData(EnumPacketServer.RecipeSave, recipe.getNbt().getMCNBT());
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.id == 0) { // Group
			if (selGroup.equals(groups.getSelected())) { return; }
			this.save();
			selGroup = groups.getSelected();
			selName = "default_name";
        } else { // Recipe
			if (selName.equals(groups.getSelected())) { return; }
			this.save();
			selName = groups.getSelected();
        }
		recipe = null;
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
				selGroup = ((SubGuiEditText) subgui).text[0];
				recipe = new NpcShapedRecipes(selGroup, "default", isGlobal, NonNullList.create(), ItemStack.EMPTY);
				RecipeController.getInstance().register(recipe);
				Client.sendData(EnumPacketServer.RecipesAddGroup, isGlobal, selGroup);
			} else if (subgui.id == 1) { // Add new Recipe
				this.save();
				selName = ((SubGuiEditText) subgui).text[0];
				recipe = new NpcShapedRecipes(selGroup, selName, isGlobal, NonNullList.create(), ItemStack.EMPTY);
				Client.sendData(EnumPacketServer.RecipeAdd, isGlobal, selGroup, selName);
			} else if (subgui.id == 2) { // Rename Group
				String old = selGroup;
				selGroup = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRenameGroup, isGlobal, old, selGroup);
			} else if (subgui.id == 3) { // Rename Recipe
				String old = selName;
				selName = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRename, isGlobal, old, selName, selName);
			}
			wait = true;
		}
	}

}
