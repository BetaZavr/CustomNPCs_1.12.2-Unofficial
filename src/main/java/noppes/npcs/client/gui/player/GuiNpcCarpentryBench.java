package noppes.npcs.client.gui.player;

import java.io.IOException;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.containers.ContainerCarpentryBench;

import javax.annotation.Nonnull;

public class GuiNpcCarpentryBench
		extends GuiContainerNPCInterface
		implements IRecipeShownListener {

	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(CustomNpcs.MODID, "textures/gui/carpentry.png");
	private final ResourceLocation buttonTexture = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
	private final ContainerCarpentryBench container;
	// from GuiCrafting
	private final GuiRecipeBook recipeBookGui = new GuiRecipeBook();
	private GuiNpcButton recipeButton;
	private boolean widthTooNarrow;

	public GuiNpcCarpentryBench(ContainerCarpentryBench container) {
		super(null, container);
		this.title = "";
		this.allowUserInput = false;
		this.closeOnEsc = true;
		this.ySize = 180;
		this.container = container;
		ScaledResolution scaleW = new ScaledResolution(this.mc);
		this.guiLeft = (scaleW.getScaledWidth() - this.xSize) / 2;
		this.guiTop = (scaleW.getScaledHeight() - this.ySize) / 2;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 10) {
			this.recipeBookGui.initVisuals(this.widthTooNarrow, ((ContainerCarpentryBench) this.inventorySlots).craftMatrix);
			this.recipeBookGui.toggleVisibility();
			this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
			this.recipeButton.x = this.guiLeft + 5;
			this.recipeButton.y = this.height / 2 - 49;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiNpcCarpentryBench.CRAFTING_TABLE_GUI_TEXTURES);
		container.checkPos(recipeBookGui.isVisible());
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		fontRenderer.drawString(new TextComponentTranslation("tile.npccarpentybench.name").getFormattedText(), this.guiLeft + 4, this.guiTop + 4, CustomNpcResourceListener.DefaultTextColor);
		fontRenderer.drawString(new TextComponentTranslation("container.inventory").getFormattedText(), this.guiLeft + 4, this.guiTop + 87, CustomNpcResourceListener.DefaultTextColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			this.recipeBookGui.render(mouseX, mouseY, partialTicks);
		} else {
			this.recipeBookGui.render(mouseX, mouseY, partialTicks);
			super.drawScreen(mouseX, mouseY, partialTicks);
			this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
		}
		this.renderHoveredToolTip(mouseX, mouseY);
		this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
	}

	@Nonnull
	@Override
	public GuiRecipeBook func_194310_f() {
		return this.recipeBookGui;
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_) {
		boolean flag = p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + this.xSize || p_193983_2_ >= p_193983_4_ + this.ySize;
		return this.recipeBookGui.hasClickedOutside(p_193983_1_, p_193983_2_, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookGui.func_194303_a(this.width, this.height, this.mc, this.widthTooNarrow, ((ContainerCarpentryBench) this.inventorySlots).craftMatrix);
		this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
		this.addButton(this.recipeButton = new GuiNpcButton(10, this.guiLeft + 5, this.height / 2 - 49, 20, 19, 0, 168, this.buttonTexture).simple(true));
	}

	@Override
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (!this.recipeBookGui.keyPressed(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
			if (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		this.recipeBookGui.removed();
		super.onGuiClosed();
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookGui.recipesUpdated();
	}

	@Override
	public void save() {
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.recipeBookGui.tick();
	}

}
