package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNPCManageRecipes
extends GuiContainerNPCInterface2
implements IGuiData, ICustomScrollListener, ISubGuiListener {
	
	private ContainerManageRecipes container;
	private List<String> dataGroup;
	private List<String> dataRecipe;
	private GuiCustomScroll groups;
	private GuiCustomScroll recipes;
	private ResourceLocation slot;
	private boolean wait;

	public GuiNPCManageRecipes(EntityNPCInterface npc, ContainerManageRecipes container) {
		super(npc, container);
		this.dataGroup = new ArrayList<String>();
		this.dataRecipe = new ArrayList<String>();
		if (ClientProxy.recipeGroup == null) {
			ClientProxy.recipeGroup = "";
		}
		if (ClientProxy.recipeName == null) {
			ClientProxy.recipeName = "";
		}
		this.container = container;
		this.drawDefaultBackground = false;
		this.setBackground("inventorymenu.png");
		this.slot = this.getResource("slot.png");
		this.ySize = 200;
		this.wait = true;
		Client.sendData(EnumPacketServer.RecipesGet, container.width, ClientProxy.recipeGroup, ClientProxy.recipeName);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 0: {
			this.save();
			ClientProxy.recipeGroup = "";
			ClientProxy.recipeName = "";
			NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, this.container.width == 3 ? 4 : 3, 0, 0);
			break;
		}
		case 1: { // Add Group
			this.setSubGui(new SubGuiEditText(0, new String[] { ClientProxy.recipeGroup }));
			break;
		}
		case 2: { // Del Group
			Client.sendData(EnumPacketServer.RecipeRemoveGroup, this.container.width, ClientProxy.recipeGroup);
			ClientProxy.recipeGroup = "";
			ClientProxy.recipeName = "";
			this.wait = true;
			break;
		}
		case 3: { // Add Recipe
			this.setSubGui(new SubGuiEditText(1, new String[] { ClientProxy.recipeName }));
			break;
		}
		case 4: { // Del Recipe
			Client.sendData(EnumPacketServer.RecipeRemove, this.container.width, ClientProxy.recipeGroup,
					ClientProxy.recipeName);
			ClientProxy.recipeName = "";
			this.wait = true;
			break;
		}
		case 5: { // ignore Meta
			this.container.recipe.setIgnoreDamage(button.getValue() == 1);
			this.initGui();
			break;
		}
		case 6: { // ignore NBT
			this.container.recipe.setIgnoreNBT(button.getValue() == 1);
			this.initGui();
			break;
		}
		case 7: { // know
			this.container.recipe.setKnown(button.getValue() == 1);
			this.initGui();
			break;
		}
		case 8: { // availability
			this.setSubGui(new SubGuiNpcAvailability((Availability) this.container.recipe.getAvailability()));
			this.initGui();
			break;
		}
		case 9: { // shaped
			INpcRecipe rec;
			if (this.container.recipe.isShaped()) {
				NpcShapedRecipes recipe = (NpcShapedRecipes) this.container.recipe;
				rec = new NpcShapelessRecipes(recipe.group, recipe.name, recipe.recipeItems, recipe.getRecipeOutput());
				((NpcShapelessRecipes) rec).known = recipe.known;
				((NpcShapelessRecipes) rec).availability = recipe.availability;
				((NpcShapelessRecipes) rec).global = recipe.global;
				((NpcShapelessRecipes) rec).ignoreDamage = recipe.ignoreDamage;
				((NpcShapelessRecipes) rec).ignoreNBT = recipe.ignoreNBT;
				((NpcShapelessRecipes) rec).savesRecipe = recipe.savesRecipe;
			} else {
				NpcShapelessRecipes recipe = (NpcShapelessRecipes) this.container.recipe;
				rec = new NpcShapedRecipes(recipe.group, recipe.name, recipe.global ? 3 : 4, recipe.global ? 3 : 4,
						recipe.recipeItems, recipe.getRecipeOutput());
				((NpcShapedRecipes) rec).known = recipe.known;
				((NpcShapedRecipes) rec).availability = recipe.availability;
				((NpcShapedRecipes) rec).global = recipe.global;
				((NpcShapedRecipes) rec).ignoreDamage = recipe.ignoreDamage;
				((NpcShapedRecipes) rec).ignoreNBT = recipe.ignoreNBT;
				((NpcShapedRecipes) rec).savesRecipe = recipe.savesRecipe;
			}
			this.container.recipe = rec;
			this.initGui();
			break;
		}
		default: {
			return;
		}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.slot);
		for (int i = 0; i < this.container.width; ++i) {
			for (int j = 0; j < this.container.width; ++j) {
				this.drawTexturedModalRect(this.guiLeft + i * 18 + 7, this.guiTop + j * 18 + 34, 0, 0, 18, 18);
			}
		}
		this.drawTexturedModalRect(this.guiLeft + 86, this.guiTop + 60, 0, 0, 18, 18);
		if (ClientProxy.recipeName.isEmpty()) {
			GlStateManager.color(0.75f, 0.0f, 0.75f, 0.5f);
			Gui.drawRect(this.guiLeft + 8, this.guiTop + 35, this.guiLeft + this.container.width * 18 + 6, this.guiTop + this.container.width * 18 + 33, 0x30C000C0);
			Gui.drawRect(this.guiLeft + 87, this.guiTop + 61, this.guiLeft + 103, this.guiTop + 77, 0x30C000C0);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.wait) {
			this.drawWait(i, j, f);
			return;
		}
		super.drawScreen(i, j, f);
		if (this.subgui != null) {
			return;
		}
		if (!CustomNpcs.showDescriptions) { return; }
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
			this.setHoverText(new TextComponentTranslation("availabitily.hover").getFormattedText());
		}
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		CustomNPCsScheduler.runTack(() -> {
			if (slotId >= 0) {
				this.initGui();
			}
		}, 200);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.wait = false;
		this.addLabel(new GuiNpcLabel(0, "gui.recipe.groups", this.guiLeft + 172, this.guiTop + 8));
		this.addLabel(new GuiNpcLabel(1, "gui.recipe.crafts", this.guiLeft + 294, this.guiTop + 8));
		if (ClientProxy.recipeGroup == null) {
			ClientProxy.recipeGroup = "";
		}
		if (ClientProxy.recipeName == null) {
			ClientProxy.recipeName = "";
		}
		// New
		if (this.groups == null) {
			this.groups = new GuiCustomScroll(this, 0);
		}
		if (this.recipes == null) {
			this.recipes = new GuiCustomScroll(this, 1);
		}

		this.groups.setSize(120, 168);
		this.groups.guiLeft = this.guiLeft + 172;
		this.groups.guiTop = this.guiTop + 20;
		this.addScroll(this.groups);

		this.recipes.setSize(120, 168);
		this.recipes.guiLeft = this.guiLeft + 294;
		this.recipes.guiTop = this.guiTop + 20;
		this.addScroll(this.recipes);

		int y = this.guiTop + 191;
		GuiButtonBiDirectional type = new GuiButtonBiDirectional(0, this.guiLeft + 6, y, 120, 20,
				new String[] { "menu.global", "tile.npccarpentybench.name" }, this.container.width == 3 ? 0 : 1);
		type.layerColor = this.container.width == 3 ? 0x4000FF00 : 0x400000FF;
		this.addButton(type);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 172, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 234, y, 59, 20, "gui.remove"));
		this.getButton(2).setEnabled(!ClientProxy.recipeGroup.isEmpty());
		this.addButton(new GuiNpcButton(3, this.guiLeft + 294, y, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 356, y, 59, 20, "gui.remove"));
		this.getButton(4).setEnabled(!ClientProxy.recipeName.isEmpty());

		this.addButton(new GuiNpcButton(5, this.guiLeft + 114, this.guiTop + 68, 50, 20,
				new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" },
				this.container.recipe.getIgnoreDamage() ? 1 : 0)); // Changed
		this.addButton(new GuiNpcButton(6, this.guiLeft + 114, this.guiTop + 90, 50, 20,
				new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, this.container.recipe.getIgnoreNBT() ? 1 : 0)); // Changed
		this.addButton(new GuiNpcButton(7, this.guiLeft + 114, this.guiTop + 46, 50, 20,
				new String[] { "gui.known.0", "gui.known.1" }, this.container.recipe.isKnown() ? 1 : 0)); // New
		this.addLabel(new GuiNpcLabel(2, "availability.options", this.guiLeft + 6, this.guiTop + 9)); // New
		this.addButton(new GuiNpcButton(8, this.guiLeft + 114, this.guiTop + 4, 50, 20, "selectServer.edit")); // New
		this.addButton(new GuiNpcButton(9, this.guiLeft + 114, this.guiTop + 25, 50, 20,
				new String[] { "gui.shaped.0", "gui.shaped.1" }, this.container.recipe.isShaped() ? 1 : 0)); // New
		this.getButton(5).setVisible(!ClientProxy.recipeGroup.isEmpty());
		this.getButton(6).setVisible(!ClientProxy.recipeGroup.isEmpty());
		this.getButton(7).setVisible(!ClientProxy.recipeGroup.isEmpty());
		this.getLabel(2).enabled = !ClientProxy.recipeGroup.isEmpty();
		this.getButton(8).setVisible(!ClientProxy.recipeGroup.isEmpty());
		this.getButton(9).setVisible(!ClientProxy.recipeGroup.isEmpty());
		boolean hasItem = !ClientProxy.recipeGroup.isEmpty();
		if (hasItem && this.container != null && this.container.width > 0 && this.container.getSlot(0).getHasStack()) {
			hasItem = false;
			for (int i = 1; i <= this.container.width * this.container.width; i++) {
				if (this.container.getSlot(i).getHasStack()) {
					hasItem = true;
					break;
				}
			}
		} else {
			hasItem = false;
		}
		this.getButton(3).setEnabled(hasItem);
		this.getButton(3).layerColor = hasItem ? 0xFF70F070 : 0;
		this.getButton(5).setEnabled(hasItem);
		this.getButton(5).layerColor = hasItem ? this.container.recipe.getIgnoreDamage() ? 0xFF70F070 : 0xFFF07070 : 0;
		this.getButton(6).setEnabled(hasItem);
		this.getButton(6).layerColor = hasItem ? this.container.recipe.getIgnoreNBT() ? 0xFF70F070 : 0xFFF07070 : 0;
		this.getButton(7).setEnabled(hasItem);
		this.getButton(7).layerColor = hasItem ? this.container.recipe.isKnown() ? 0xFF70F070 : 0xFFF07070 : 0;
		this.getButton(8).setEnabled(hasItem);
		this.getButton(9).setEnabled(hasItem);
		this.getButton(9).layerColor = hasItem ? this.container.recipe.isShaped() ? 0xFF70F070 : 0xFF7070FF : 0;
		
		if (this.container.width == 3) { // New
			GuiNpcLabel label = new GuiNpcLabel(3, new TextComponentTranslation("gui.recipe.hover.cursor.name").getFormattedText(), this.guiLeft + 9, this.guiTop + 94);
			label.backColor = 0x40FF0000;
			label.borderColor = 0x80808080;
			label.color = 0xFF000000;
			label.hoverText = new String[] { new TextComponentTranslation("gui.recipe.hover.cursor.info").getFormattedText() };
			this.addLabel(label);
		}
	}

	@Override
	public void save() {
		if (ClientProxy.recipeGroup != null && !ClientProxy.recipeGroup.isEmpty() && ClientProxy.recipeName != null
				&& !ClientProxy.recipeName.isEmpty()) {
			this.container.saveRecipe(ClientProxy.recipeGroup, ClientProxy.recipeName,
					this.getButton(9) != null ? this.getButton(9).getValue() == 1 : true);
			NBTTagCompound compound = this.container.recipe.isShaped()
					? ((NpcShapedRecipes) this.container.recipe).getNbt().getMCNBT()
					: ((NpcShapelessRecipes) this.container.recipe).getNbt().getMCNBT();
			Client.sendData(EnumPacketServer.RecipeSave, compound);
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		switch (scroll.id) {
			case 0: { // Group
				if (ClientProxy.recipeGroup.equals(this.groups.getSelected())) {
					return;
				}
				this.save();
				ClientProxy.recipeGroup = this.groups.getSelected();
				ClientProxy.recipeName = "";
				this.container.craftingMatrix.clear();
				this.recipes.setSelected(null);
				Client.sendData(EnumPacketServer.RecipesGet, this.container.width, ClientProxy.recipeGroup,
						ClientProxy.recipeName);
				this.wait = true;
				break;
			}
			case 1: { // Recipe
				if (ClientProxy.recipeName.equals(this.recipes.getSelected())) {
					return;
				}
				this.save();
				ClientProxy.recipeName = this.recipes.getSelected();
				Client.sendData(EnumPacketServer.RecipeGet, this.container.width, ClientProxy.recipeGroup,
						ClientProxy.recipeName);
				this.wait = true;
				break;
			}
		}
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
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Groups", 9) && compound.hasKey("Recipes", 9)) {
			List<String> gs = Lists.newArrayList();
			for (int i = 0; i < compound.getTagList("Groups", 8).tagCount(); i++) {
				gs.add(compound.getTagList("Groups", 8).getStringTagAt(i));
			}
			this.dataGroup = gs;
			this.groups.setList(this.dataGroup);

			List<String> rs = Lists.newArrayList();
			for (int i = 0; i < compound.getTagList("Recipes", 8).tagCount(); i++) {
				rs.add(compound.getTagList("Recipes", 8).getStringTagAt(i));
			}
			this.dataRecipe = rs;
			this.recipes.setList(this.dataRecipe);

			boolean newGui = false;
			if (ClientProxy.recipeGroup != null && !ClientProxy.recipeGroup.isEmpty()) {
				if (!this.dataGroup.contains(ClientProxy.recipeGroup)) {
					ClientProxy.recipeGroup = "";
					newGui = true;
				} else {
					this.groups.setSelected(ClientProxy.recipeGroup);
				}
			}
			if (ClientProxy.recipeName != null && !ClientProxy.recipeName.isEmpty()) {
				if (!this.dataRecipe.contains(ClientProxy.recipeName)) {
					ClientProxy.recipeName = "";
					newGui = true;
				} else {
					this.recipes.setSelected(ClientProxy.recipeName);
				}
			}
			if (newGui) {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, this.container.size, 0, 0);
			} else {
				this.initGui();
			}
		}
		if (compound.hasKey("SelectRecipe", 9)) {
			NBTTagCompound nbtRecipe = compound.getCompoundTag("SelectRecipe");
			INpcRecipe recipe;
			if (nbtRecipe.getBoolean("IsShaped")) {
				recipe = NpcShapedRecipes.read(nbtRecipe);
			} else {
				recipe = NpcShapelessRecipes.read(nbtRecipe);
			}
			this.container.setRecipe(recipe);
		}
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcAvailability) {
			this.save();
		} else if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) {
				return;
			}
			if (((SubGuiEditText) subgui).id == 0) { // Add new Group
				ClientProxy.recipeGroup = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesAddGroup, this.container.width, ClientProxy.recipeGroup);
				this.wait = true;
			} else if (((SubGuiEditText) subgui).id == 1) { // Add new Recipe
				ClientProxy.recipeName = ((SubGuiEditText) subgui).text[0];
				this.save();
				Client.sendData(EnumPacketServer.RecipeGet, this.container.width, ClientProxy.recipeGroup,
						ClientProxy.recipeName);
				this.wait = true;
			} else if (((SubGuiEditText) subgui).id == 2) { // Rename Group
				String old = ClientProxy.recipeGroup;
				ClientProxy.recipeGroup = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRenameGroup, this.container.width, old, ClientProxy.recipeGroup,
						ClientProxy.recipeName);
				this.wait = true;
			} else if (((SubGuiEditText) subgui).id == 3) { // Rename Recipe
				String old = ClientProxy.recipeName;
				ClientProxy.recipeName = ((SubGuiEditText) subgui).text[0];
				Client.sendData(EnumPacketServer.RecipesRename, this.container.width, old, ClientProxy.recipeGroup,
						ClientProxy.recipeName);
				this.wait = true;
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
		}
		super.keyTyped(c, i);
	}

}
