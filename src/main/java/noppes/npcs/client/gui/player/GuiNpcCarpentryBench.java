package noppes.npcs.client.gui.player;

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
import noppes.npcs.client.gui.util.IGuiNpcButton;
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

	public GuiNpcCarpentryBench(ContainerCarpentryBench cont) {
		super(null, cont);
		title = "";
		allowUserInput = false;
		closeOnEsc = true;
		ySize = 180;

		container = cont;
		ScaledResolution scaleW = new ScaledResolution(mc);
		guiLeft = (scaleW.getScaledWidth() - xSize) / 2;
		guiTop = (scaleW.getScaledHeight() - ySize) / 2;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 10) {
			recipeBookGui.initVisuals(widthTooNarrow, ((ContainerCarpentryBench) inventorySlots).craftMatrix);
			recipeBookGui.toggleVisibility();
			guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
			recipeButton.x = guiLeft + 5;
			recipeButton.y = height / 2 - 49;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiNpcCarpentryBench.CRAFTING_TABLE_GUI_TEXTURES);
		container.checkPos(recipeBookGui.isVisible());
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		fontRenderer.drawString(new TextComponentTranslation("tile.npccarpentybench.name").getFormattedText(), guiLeft + 4, guiTop + 4, CustomNpcResourceListener.DefaultTextColor);
		fontRenderer.drawString(new TextComponentTranslation("container.inventory").getFormattedText(), guiLeft + 4, guiTop + 87, CustomNpcResourceListener.DefaultTextColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (recipeBookGui.isVisible() && widthTooNarrow) {
			drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			recipeBookGui.render(mouseX, mouseY, partialTicks);
		} else {
			recipeBookGui.render(mouseX, mouseY, partialTicks);
			super.drawScreen(mouseX, mouseY, partialTicks);
			recipeBookGui.renderGhostRecipe(guiLeft, guiTop, true, partialTicks);
		}
		renderHoveredToolTip(mouseX, mouseY);
		recipeBookGui.renderTooltip(guiLeft, guiTop, mouseX, mouseY);
	}

	@Nonnull
	@Override
	public GuiRecipeBook func_194310_f() {
		return recipeBookGui;
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		recipeBookGui.slotClicked(slotIn);
	}

	protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_) {
		boolean flag = p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + xSize || p_193983_2_ >= p_193983_4_ + ySize;
		return recipeBookGui.hasClickedOutside(p_193983_1_, p_193983_2_, guiLeft, guiTop, xSize, ySize) && flag;
	}

	@Override
	public void initGui() {
		super.initGui();
		widthTooNarrow = width < 379;
		recipeBookGui.func_194303_a(width, height, mc, widthTooNarrow, ((ContainerCarpentryBench) inventorySlots).craftMatrix);
		guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
		recipeButton = new GuiNpcButton(10, guiLeft + 5, height / 2 - 49, 20, 19, 0, 168, buttonTexture).simple(true);
		addButton((IGuiNpcButton) recipeButton);
	}

	@Override
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
		return (!widthTooNarrow || !recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (!recipeBookGui.keyPressed(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
			if (!widthTooNarrow || !recipeBookGui.isVisible()) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		recipeBookGui.removed();
		super.onGuiClosed();
	}

	@Override
	public void recipesUpdated() {
		recipeBookGui.recipesUpdated();
	}

    @Override
	public void updateScreen() {
		super.updateScreen();
		recipeBookGui.tick();
	}

}
