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
public class GuiCreateFlatDimension
extends GuiScreen {

	@SideOnly(Side.CLIENT)
	class Details extends GuiSlot {

		public int selectedLayer = -1;

		public Details() {
			super(GuiCreateFlatDimension.this.mc, GuiCreateFlatDimension.this.width, GuiCreateFlatDimension.this.height,
					43, GuiCreateFlatDimension.this.height - 60, 24);
		}

		protected void drawBackground() {
		}

		private void drawItem(int x, int z, ItemStack itemToDraw) {
			this.drawItemBackground(x + 1, z + 1);
			GlStateManager.enableRescaleNormal();
			if (itemToDraw != null) {
                itemToDraw.getItem();
                RenderHelper.enableGUIStandardItemLighting();
                GuiCreateFlatDimension.this.itemRender.renderItemIntoGUI(itemToDraw, x + 2, z + 2);
                RenderHelper.disableStandardItemLighting();
            }
			GlStateManager.disableRescaleNormal();
		}

		private void drawItemBackground(int x, int y) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(x, y + 18, GuiCreateFlatDimension.this.zLevel).tex(0.0F, 0.140625F).endVertex();
			vertexbuffer.pos(x + 18, y + 18, GuiCreateFlatDimension.this.zLevel).tex(0.140625F, 0.140625F).endVertex();
			vertexbuffer.pos(x + 18, y, GuiCreateFlatDimension.this.zLevel).tex(0.140625F, 0.0F).endVertex();
			vertexbuffer.pos(x, y, GuiCreateFlatDimension.this.zLevel).tex(0.0F, 0.0F).endVertex();
			tessellator.draw();
		}

		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
			FlatLayerInfo flatlayerinfo = GuiCreateFlatDimension.this.theFlatGeneratorInfo
					.getFlatLayers()
					.get(GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
			IBlockState iblockstate = flatlayerinfo.getLayerMaterial();
			Block block = iblockstate.getBlock();
			Item item = Item.getItemFromBlock(block);
			ItemStack itemstack = block != Blocks.AIR
					? new ItemStack(item, 1, block.getMetaFromState(iblockstate))
					: null;
			String s = itemstack == null ? I18n.format("createWorld.customize.flat.air")
					: item.getItemStackDisplayName(itemstack);
            this.drawItem(insideLeft, yPos, itemstack);
			GuiCreateFlatDimension.this.fontRenderer.drawString(s, insideLeft + 18 + 5, yPos + 3, 16777215);
			String s1;
			if (entryID == 0) {
				s1 = I18n.format("createWorld.customize.flat.layer.top", flatlayerinfo.getLayerCount());
			} else if (entryID == GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size() - 1) {
				s1 = I18n.format("createWorld.customize.flat.layer.bottom", flatlayerinfo.getLayerCount());
			} else {
				s1 = I18n.format("createWorld.customize.flat.layer", flatlayerinfo.getLayerCount());
			}
			GuiCreateFlatDimension.this.fontRenderer.drawString(s1,
					insideLeft + 2 + 213 - GuiCreateFlatDimension.this.fontRenderer.getStringWidth(s1), yPos + 3,
					16777215);
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			this.selectedLayer = slotIndex;
			GuiCreateFlatDimension.this.onLayersChanged();
		}

		protected int getScrollBarX() {
			return this.width - 70;
		}

		protected int getSize() {
			return GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size();
		}

		protected boolean isSelected(int slotIndex) {
			return slotIndex == this.selectedLayer;
		}
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
		this.createDimensionGui = createWorldGuiIn;
		this.setPreset(preset);
	}

	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		int i = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.selectedLayer - 1;
		if (button.id == 1) {
			this.mc.displayGuiScreen(this.createDimensionGui);
		} else if (button.id == 0) {
			this.createDimensionGui.chunkProviderSettingsJson = this.getPreset();
			this.mc.displayGuiScreen(this.createDimensionGui);
		} else if (button.id == 5) {
			this.mc.displayGuiScreen(new GuiFlatDimensionPresets(this));
		} else if (button.id == 4 && this.hasSelectedLayer()) {
			this.theFlatGeneratorInfo.getFlatLayers().remove(i);
			this.createFlatWorldListSlotGui.selectedLayer = Math.min(this.createFlatWorldListSlotGui.selectedLayer,
					this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
		}
		this.theFlatGeneratorInfo.updateLayers();
		this.onLayersChanged();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, this.flatWorldTitle, this.width / 2, 8, 16777215);
		int i = this.width / 2 - 92 - 16;
		this.drawString(this.fontRenderer, this.materialText, i, 32, 16777215);
		this.drawString(this.fontRenderer, this.heightText,
				i + 2 + 213 - this.fontRenderer.getStringWidth(this.heightText), 32, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public String getPreset() {
		return this.theFlatGeneratorInfo.toString();
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.createFlatWorldListSlotGui.handleMouseInput();
	}

	private boolean hasSelectedLayer() {
		return this.createFlatWorldListSlotGui.selectedLayer > -1
				&& this.createFlatWorldListSlotGui.selectedLayer < this.theFlatGeneratorInfo.getFlatLayers().size();
	}

	public void initGui() {
		this.buttonList.clear();
		this.flatWorldTitle = I18n.format("createWorld.customize.flat.title");
		this.materialText = I18n.format("createWorld.customize.flat.tile");
		this.heightText = I18n.format("createWorld.customize.flat.height");
		this.createFlatWorldListSlotGui = new GuiCreateFlatDimension.Details();
		this.buttonList.add(this.addLayerButton = new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
		this.buttonList.add(this.editLayerButton = new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
		this.buttonList.add(this.removeLayerButton = new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
		this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
		this.addLayerButton.visible = this.editLayerButton.visible = false;
		this.theFlatGeneratorInfo.updateLayers();
		this.onLayersChanged();
	}

	public void onLayersChanged() {
        this.removeLayerButton.enabled = this.hasSelectedLayer();
        this.editLayerButton.enabled = false;
		this.addLayerButton.enabled = false;
	}

	public void setPreset(String preset) {
		this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(preset);
	}

}