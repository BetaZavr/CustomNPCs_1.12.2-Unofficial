package noppes.npcs.client.gui.dimentions;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiCreateFlatDimension extends GuiScreen {

	@SideOnly(Side.CLIENT)
	class Details extends GuiSlot {

		public int selectedLayer = -1;

		public Details() {
			super(GuiCreateFlatDimension.this.mc, GuiCreateFlatDimension.this.width, GuiCreateFlatDimension.this.height, 43, GuiCreateFlatDimension.this.height - 60, 24);
		}

		@Override
		protected void drawBackground() { }

		private void drawItem(int x, int z, ItemStack itemToDraw) {
			drawItemBackground(x + 1, z + 1);
			GlStateManager.enableRescaleNormal();
			if (itemToDraw != null) {
                itemToDraw.getItem();
				RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemIntoGUI(itemToDraw, x + 2, z + 2);
                RenderHelper.disableStandardItemLighting();
            }
			GlStateManager.disableRescaleNormal();
		}

		private void drawItemBackground(int x, int y) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(x, y + 18, zLevel).tex(0.0F, 0.140625F).endVertex();
			vertexbuffer.pos(x + 18, y + 18, zLevel).tex(0.140625F, 0.140625F).endVertex();
			vertexbuffer.pos(x + 18, y, zLevel).tex(0.140625F, 0.0F).endVertex();
			vertexbuffer.pos(x, y, zLevel).tex(0.0F, 0.0F).endVertex();
			tessellator.draw();
		}

		@SuppressWarnings("all")
		@Override
		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
			FlatLayerInfo flatlayerinfo = theFlatGeneratorInfo
					.getFlatLayers()
					.get(theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
			IBlockState iblockstate = flatlayerinfo.getLayerMaterial();
			Block block = iblockstate.getBlock();
			Item item = Item.getItemFromBlock(block);
			ItemStack itemstack = block != Blocks.AIR
					? new ItemStack(item, 1, block.getMetaFromState(iblockstate))
					: null;
			String s = itemstack == null ? I18n.format("createWorld.customize.flat.air")
					: item.getItemStackDisplayName(itemstack);
            drawItem(insideLeft, yPos, itemstack);
			fontRenderer.drawString(s, insideLeft + 18 + 5, yPos + 3, 0xFFFFFF);
			String s1;
			if (entryID == 0) { s1 = I18n.format("createWorld.customize.flat.layer.top", flatlayerinfo.getLayerCount()); }
			else if (entryID == theFlatGeneratorInfo.getFlatLayers().size() - 1) { s1 = I18n.format("createWorld.customize.flat.layer.bottom", flatlayerinfo.getLayerCount()); }
			else { s1 = I18n.format("createWorld.customize.flat.layer", flatlayerinfo.getLayerCount()); }
			fontRenderer.drawString(s1, insideLeft + 2 + 213 - fontRenderer.getStringWidth(s1), yPos + 3, 0xFFFFFF);
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedLayer = slotIndex;
			onLayersChanged();
		}

		@Override
		protected int getScrollBarX() { return width - 70; }

		@Override
		protected int getSize() { return theFlatGeneratorInfo.getFlatLayers().size(); }

		@Override
		protected boolean isSelected(int slotIndex) { return slotIndex == selectedLayer; }

	}
	private final GuiCreateDimension createDimensionGui;
	private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
	private String flatWorldTitle;
	private String materialText;
	private String heightText;
	private GuiCreateFlatDimension.Details createFlatWorldListSlotGui;
	private GuiButton addLayerButton;
	private GuiButton editLayerButton;

	private GuiButton removeLayerButton;

	public GuiCreateFlatDimension(GuiCreateDimension createWorldGuiIn, String preset) {
		createDimensionGui = createWorldGuiIn;
		setPreset(preset);
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		int i = theFlatGeneratorInfo.getFlatLayers().size() - createFlatWorldListSlotGui.selectedLayer - 1;
		if (button.id == 1) { mc.displayGuiScreen(createDimensionGui); }
		else if (button.id == 0) {
			createDimensionGui.chunkProviderSettingsJson = getPreset();
			mc.displayGuiScreen(createDimensionGui);
		}
		else if (button.id == 5) { mc.displayGuiScreen(new GuiFlatDimensionPresets(this)); }
		else if (button.id == 4 && hasSelectedLayer()) {
			theFlatGeneratorInfo.getFlatLayers().remove(i);
			createFlatWorldListSlotGui.selectedLayer = Math.min(createFlatWorldListSlotGui.selectedLayer, theFlatGeneratorInfo.getFlatLayers().size() - 1);
		}
		theFlatGeneratorInfo.updateLayers();
		onLayersChanged();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, flatWorldTitle, width / 2, 8, 16777215);
		int i = width / 2 - 92 - 16;
		drawString(fontRenderer, materialText, i, 32, 16777215);
		drawString(fontRenderer, heightText, i + 2 + 213 - fontRenderer.getStringWidth(heightText), 32, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public String getPreset() { return theFlatGeneratorInfo.toString(); }

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		createFlatWorldListSlotGui.handleMouseInput();
	}

	private boolean hasSelectedLayer() {
		return createFlatWorldListSlotGui.selectedLayer > -1 && createFlatWorldListSlotGui.selectedLayer < theFlatGeneratorInfo.getFlatLayers().size();
	}

	@Override
	public void initGui() {
		buttonList.clear();
		flatWorldTitle = I18n.format("createWorld.customize.flat.title");
		materialText = I18n.format("createWorld.customize.flat.tile");
		heightText = I18n.format("createWorld.customize.flat.height");
		createFlatWorldListSlotGui = new GuiCreateFlatDimension.Details();
		buttonList.add(addLayerButton = new GuiButton(2, width / 2 - 154, height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
		buttonList.add(editLayerButton = new GuiButton(3, width / 2 - 50, height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
		buttonList.add(removeLayerButton = new GuiButton(4, width / 2 - 155, height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
		buttonList.add(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		buttonList.add(new GuiButton(5, width / 2 + 5, height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
		buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		addLayerButton.visible = editLayerButton.visible = false;
		theFlatGeneratorInfo.updateLayers();
		onLayersChanged();
	}

	public void onLayersChanged() {
        removeLayerButton.enabled = hasSelectedLayer();
        editLayerButton.enabled = false;
		addLayerButton.enabled = false;
	}

	public void setPreset(String preset) { theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(preset); }

}