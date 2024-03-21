package noppes.npcs.client.gui.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.constants.EnumPlayerPacket;

@SideOnly(Side.CLIENT)
public class GuiNpcRecipeBook
extends GuiRecipeBook {
	
	protected static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
	private InventoryCrafting craftingSlots;
	private GuiNpcButtonRecipeTab currentTab;
	private final NPCGhostRecipe ghostRecipe = new NPCGhostRecipe();
	private int height;
	// New
	public boolean isGlobal = true;
	private String lastSearch = "";
	private Minecraft mc;
	public RecipeBook recipeBook;
	private NpcRecipeBookPage recipeBookPage;
	private List<GuiNpcButtonRecipeTab> recipeTabs;
	private GuiTextField searchBar;
	private RecipeItemHelper stackedContents = new RecipeItemHelper();
	private int timesInventoryChanged;
	private GuiButtonToggle toggleRecipesBtn;
	private int width;
	private int xOffset;

	public GuiNpcRecipeBook(boolean isGlobal) {
		this.isGlobal = isGlobal;
		this.recipeBookPage = new NpcRecipeBookPage(isGlobal);
		this.recipeTabs = Lists.newArrayList(new GuiNpcButtonRecipeTab(0, CreativeTabs.SEARCH, isGlobal),
				new GuiNpcButtonRecipeTab(0, CreativeTabs.TOOLS, isGlobal),
				new GuiNpcButtonRecipeTab(0, CreativeTabs.BUILDING_BLOCKS, isGlobal),
				new GuiNpcButtonRecipeTab(0, CreativeTabs.MISC, isGlobal),
				new GuiNpcButtonRecipeTab(0, CreativeTabs.REDSTONE, isGlobal));
	}

	public void func_194303_a(int width, int height, Minecraft mc, boolean widthTooNarrow, InventoryCrafting inv) {
		this.mc = mc;
		this.width = width;
		this.height = height;
		this.craftingSlots = inv;
		this.recipeBook = mc.player.getRecipeBook();
		this.timesInventoryChanged = mc.player.inventory.getTimesChanged();
		this.currentTab = this.recipeTabs.get(0);
		this.currentTab.setStateTriggered(true);
		if (this.isVisible()) { this.initVisuals(widthTooNarrow, inv); }
		Keyboard.enableRepeatEvents(true);
	}

	public boolean hasClickedOutside(int p_193955_1_, int p_193955_2_, int p_193955_3_, int p_193955_4_,
			int p_193955_5_, int p_193955_6_) {
		if (!this.isVisible()) {
			return true;
		} else {
			boolean flag = p_193955_1_ < p_193955_3_ || p_193955_2_ < p_193955_4_
					|| p_193955_1_ >= p_193955_3_ + p_193955_5_ || p_193955_2_ >= p_193955_4_ + p_193955_6_;
			boolean flag1 = p_193955_3_ - 147 < p_193955_1_ && p_193955_1_ < p_193955_3_ && p_193955_4_ < p_193955_2_
					&& p_193955_2_ < p_193955_4_ + p_193955_6_;
			return flag && !flag1 && !this.currentTab.mousePressed(this.mc, p_193955_1_, p_193955_2_);
		}
	}

	public void initVisuals(boolean widthTooNarrow, InventoryCrafting inv) {
		this.xOffset = widthTooNarrow ? 0 : 86;
		int i = (this.width - 147) / 2 - this.xOffset;
		int j = (this.height - 166) / 2;
		this.stackedContents.clear();
		this.mc.player.inventory.fillStackedContents(this.stackedContents, false);
		inv.fillStackedContents(this.stackedContents);
		this.searchBar = new GuiTextField(0, this.mc.fontRenderer, i + 25, j + 14, 80, this.mc.fontRenderer.FONT_HEIGHT + 5);
		this.searchBar.setMaxStringLength(50);
		this.searchBar.setEnableBackgroundDrawing(false);
		this.searchBar.setVisible(true);
		this.searchBar.setTextColor(16777215);
		this.recipeBookPage.init(this.mc, i, j);
		this.recipeBookPage.addListener(this);
		this.toggleRecipesBtn = new GuiButtonToggle(0, i + 110, j + 12, 26, 16, this.recipeBook.isFilteringCraftable());
		this.toggleRecipesBtn.initTextureValues(152, 41, 28, 18, RECIPE_BOOK);
		this.updateCollections(false);
		this.updateTabs();
	}

	private boolean isOffsetNextToMainGUI() {
		return this.xOffset == 86;
	}

	public boolean isVisible() {
		return this.recipeBook.isGuiOpen();
	}

	public boolean keyPressed(char typedChar, int keycode) {
		if (this.isVisible() && !this.mc.player.isSpectator()) {
			if (keycode == 1 && !this.isOffsetNextToMainGUI()) {
				this.setVisible(false);
				return true;
			} else {
				if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat) && !this.searchBar.isFocused()) {
					this.searchBar.setFocused(true);
				} else if (this.searchBar.textboxKeyTyped(typedChar, keycode)) {
					String s1 = this.searchBar.getText().toLowerCase(Locale.ROOT);
					this.pirateRecipe(s1);
					if (!s1.equals(this.lastSearch)) {
						this.updateCollections(false);
						this.lastSearch = s1;
					}
					return true;
				}
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!this.isVisible() || this.mc.player.isSpectator()) {
			return false;
		}
		if (this.recipeBookPage.mouseClicked(mouseX, mouseY, mouseButton, (this.width - 147) / 2 - this.xOffset,
				(this.height - 166) / 2, 147, 166)) {
			IRecipe irecipe = this.recipeBookPage.getLastClickedRecipe();
			RecipeList recipelist = this.recipeBookPage.getLastClickedRecipeList();
			if (irecipe != null && recipelist != null) {
				if (!recipelist.isCraftable(irecipe) && this.ghostRecipe.getRecipe() == irecipe) {
					return false;
				}
				this.ghostRecipe.clear();
				if (this.isGlobal) {
					this.mc.playerController.func_194338_a(this.mc.player.openContainer.windowId, irecipe,
							GuiScreen.isShiftKeyDown(), this.mc.player);
				} else {
					NoppesUtilPlayer.sendData(EnumPlayerPacket.GetGhostRecipe, this.mc.player.openContainer.windowId,
							((INpcRecipe) irecipe).getId(), GuiScreen.isShiftKeyDown());
				}
				if (!this.isOffsetNextToMainGUI() && mouseButton == 0) {
					this.setVisible(false);
				}
			}
			return true;
		} else if (mouseButton != 0) {
			return false;
		} else if (this.searchBar.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		} else if (this.toggleRecipesBtn.mousePressed(this.mc, mouseX, mouseY)) {
			boolean flag = !this.recipeBook.isFilteringCraftable();
			this.recipeBook.setFilteringCraftable(flag);
			this.toggleRecipesBtn.setStateTriggered(flag);
			this.toggleRecipesBtn.playPressSound(this.mc.getSoundHandler());
			this.sendUpdateSettings();
			this.updateCollections(false);
			return true;
		} else {
			for (GuiNpcButtonRecipeTab GuiNpcButtonRecipeTab : this.recipeTabs) {
				if (GuiNpcButtonRecipeTab.mousePressed(this.mc, mouseX, mouseY)) {
					if (this.currentTab != GuiNpcButtonRecipeTab) {
						GuiNpcButtonRecipeTab.playPressSound(this.mc.getSoundHandler());
						this.currentTab.setStateTriggered(false);
						this.currentTab = GuiNpcButtonRecipeTab;
						this.currentTab.setStateTriggered(true);
						this.updateCollections(true);
					}
					return true;
				}
			}
			return false;
		}
	}

	private void pirateRecipe(String text) {
		if ("excitedze".equals(text)) {
			LanguageManager languagemanager = this.mc.getLanguageManager();
			Language language = languagemanager.getLanguage("en_pt");
			if (languagemanager.getCurrentLanguage().compareTo(language) == 0) {
				return;
			}
			languagemanager.setCurrentLanguage(language);
			this.mc.gameSettings.language = language.getLanguageCode();
			net.minecraftforge.fml.client.FMLClientHandler.instance()
					.refreshResources(net.minecraftforge.client.resource.VanillaResourceType.LANGUAGES);
			this.mc.fontRenderer.setUnicodeFlag(
					this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.mc.gameSettings.forceUnicodeFont);
			this.mc.fontRenderer.setBidiFlag(languagemanager.isCurrentLanguageBidirectional());
			this.mc.gameSettings.saveOptions();
		}
	}

	public void recipesShown(List<IRecipe> recipes) {
		for (IRecipe irecipe : recipes) {
			this.mc.player.removeRecipeHighlight(irecipe);
		}
	}

	public void recipesUpdated() {
		this.updateTabs();
		if (this.isVisible()) {
			this.updateCollections(false);
		}
	}

	public void removed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void render(int mouseX, int mouseY, float partialTicks) {
		if (!this.isVisible() || this.searchBar==null) {
			return;
		}
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 100.0F);
		this.mc.renderEngine.bindTexture(RECIPE_BOOK);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (this.width - 147) / 2 - this.xOffset;
		int j = (this.height - 166) / 2;
		this.drawTexturedModalRect(i, j, 1, 1, 147, 166);
		this.searchBar.drawTextBox();
		RenderHelper.disableStandardItemLighting();
		for (GuiNpcButtonRecipeTab GuiNpcButtonRecipeTab : this.recipeTabs) {
			GuiNpcButtonRecipeTab.drawButton(this.mc, mouseX, mouseY, partialTicks);
		}
		this.toggleRecipesBtn.drawButton(this.mc, mouseX, mouseY, partialTicks);
		this.recipeBookPage.render(i, j, mouseX, mouseY, partialTicks);
		GlStateManager.popMatrix();
	}

	public void renderGhostRecipe(int guiLeft, int guiTop, boolean select, float partialTicks) {
		this.ghostRecipe.render(this.mc, guiLeft, guiTop, select, partialTicks);
	}

	private void renderGhostRecipeTooltip(int mouseX, int mouseY, int mouseButton, int p_193015_4_) {
		ItemStack itemstack = null;
		for (int i = 0; i < this.ghostRecipe.size(); ++i) {
			GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
			int j = ghostrecipe$ghostingredient.getX() + mouseX;
			int k = ghostrecipe$ghostingredient.getY() + mouseY;
			if (mouseButton >= j && p_193015_4_ >= k && mouseButton < j + 16 && p_193015_4_ < k + 16) {
				itemstack = ghostrecipe$ghostingredient.getItem();
			}
		}
		if (itemstack != null && this.mc.currentScreen != null) {
			this.mc.currentScreen.drawHoveringText(this.mc.currentScreen.getItemToolTip(itemstack), mouseButton,
					p_193015_4_);
		}
	}

	public void renderTooltip(int mouseX, int mouseY, int mouseButton, int p_191876_4_) {
		if (this.isVisible() && this.recipeBookPage != null && this.toggleRecipesBtn != null) {
			this.recipeBookPage.renderTooltip(mouseButton, p_191876_4_);
			if (this.toggleRecipesBtn.isMouseOver()) {
				String s1 = I18n
						.format(this.toggleRecipesBtn.isStateTriggered() ? "gui.recipebook.toggleRecipes.craftable"
								: "gui.recipebook.toggleRecipes.all");
				if (this.mc.currentScreen != null) {
					this.mc.currentScreen.drawHoveringText(s1, mouseButton, p_191876_4_);
				}
			}
			this.renderGhostRecipeTooltip(mouseX, mouseY, mouseButton, p_191876_4_);
		}
	}

	private void sendUpdateSettings() {
		if (this.mc.getConnection() != null) {
			this.mc.getConnection()
					.sendPacket(new CPacketRecipeInfo(this.isVisible(), this.recipeBook.isFilteringCraftable()));
		}
	}

	public void setupGhostRecipe(IRecipe recipe, List<Slot> slots) {
		ItemStack itemstack = recipe.getRecipeOutput();
		this.ghostRecipe.setRecipe(recipe);
		this.ghostRecipe.addIngredient(Ingredient.fromStacks(itemstack),
				(slots.get(0)).xPos + (this.isGlobal ? 0 : -77), (slots.get(0)).yPos);
		int i = this.craftingSlots.getWidth();
		int j = this.craftingSlots.getHeight();
		int k = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : i;
		int l = 1;
		Iterator<Ingredient> iterator = recipe.getIngredients().iterator();
		for (int i1 = 0; i1 < j; ++i1) {
			for (int j1 = 0; j1 < k; ++j1) {
				if (!iterator.hasNext()) {
					return;
				}
				Ingredient ingredient = iterator.next();
				if (ingredient != Ingredient.EMPTY) {
					Slot slot = slots.get(l);
					this.ghostRecipe.addIngredient(ingredient, slot.xPos + (this.isGlobal ? 0 : -77), slot.yPos);
				}
				++l;
			}
			if (k < i) {
				l += i - k;
			}
		}
	}

	private void setVisible(boolean visible) {
		this.recipeBook.setGuiOpen(visible);
		if (!visible) {
			this.recipeBookPage.setInvisible();
		}
		this.sendUpdateSettings();
	}

	public void slotClicked(@Nullable Slot slotIn) {
		if (slotIn != null && slotIn.slotNumber <= (this.isGlobal ? 9 : 16)) {
			this.ghostRecipe.clear();
			if (this.isVisible()) {
				this.updateStackedContents();
			}
		}
	}

	public void tick() {
		if (!this.isVisible()) {
			return;
		}
		if (this.timesInventoryChanged != this.mc.player.inventory.getTimesChanged()) {
			this.updateStackedContents();
			this.timesInventoryChanged = this.mc.player.inventory.getTimesChanged();
		}
	}

	public void toggleVisibility() {
		this.setVisible(!this.isVisible());
	}

	private void updateCollections(boolean bo) {
		if (this.searchBar==null) { return; }
		List<RecipeList> recipes = RecipeBookClient.RECIPES_BY_TAB.get(this.currentTab.getCategory());
		if (!this.isGlobal) {
			recipes = ClientProxy.MOD_RECIPES_BY_TAB.get(this.currentTab.getCategory());
		}
		recipes.forEach((recipeList) -> {
			recipeList.canCraft(this.stackedContents, this.craftingSlots.getWidth(), this.craftingSlots.getHeight(), this.recipeBook);
		});
		List<RecipeList> list = Lists.newArrayList(recipes);
		list.removeIf((recipeList) -> {
			return !recipeList.isNotEmpty();
		});
		list.removeIf((recipeList) -> {
			return !recipeList.containsValidRecipes();
		});
		String s = this.searchBar.getText();
		if (!s.isEmpty()) {
			ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<RecipeList>( this.mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)));
			list.removeIf((recipeList) -> {
				return !objectset.contains(recipeList);
			});
		}
		if (this.recipeBook.isFilteringCraftable()) {
			list.removeIf((recipeList) -> {
				return !recipeList.containsCraftableRecipes();
			});
		}
		this.recipeBookPage.updateLists(list, bo);
	}

	public int updateScreenPosition(boolean p_193011_1_, int p_193011_2_, int p_193011_3_) {
		int i;
		if (this.isVisible() && !p_193011_1_) {
			i = 177 + (p_193011_2_ - p_193011_3_ - 200) / 2;
		} else {
			i = (p_193011_2_ - p_193011_3_) / 2;
		}
		return i;
	}

	private void updateStackedContents() {
		this.stackedContents.clear();
		this.mc.player.inventory.fillStackedContents(this.stackedContents, false);
		this.craftingSlots.fillStackedContents(this.stackedContents);
		this.updateCollections(false);
	}

	private void updateTabs() {
		int i = (this.width - 147) / 2 - this.xOffset - 30;
		int j = (this.height - 166) / 2 + 3;
		int k = 27;
		int l = 0;
		for (GuiNpcButtonRecipeTab GuiNpcButtonRecipeTab : this.recipeTabs) {
			CreativeTabs creativetabs = GuiNpcButtonRecipeTab.getCategory();
			if (creativetabs == CreativeTabs.SEARCH) {
				GuiNpcButtonRecipeTab.visible = true;
				GuiNpcButtonRecipeTab.setPosition(i, j + k * l++);
			} else if (GuiNpcButtonRecipeTab.updateVisibility()) {
				GuiNpcButtonRecipeTab.setPosition(i, j + k * l++);
				GuiNpcButtonRecipeTab.startAnimation(this.mc);
			}
		}
	}

	public List<GuiNpcButtonRecipeTab> getRecipeTabs() { return this.recipeTabs; }

}
