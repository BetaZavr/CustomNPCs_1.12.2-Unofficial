package noppes.npcs.client.gui.dimentions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiFlatDimensionPresets extends GuiScreen {

	@SideOnly(Side.CLIENT)
	static class LayerItem {

		public Item icon;
		public int iconMetadata;
		public String name;
		public String generatorInfo;

		public LayerItem(Item iconIn, int iconMetadataIn, String nameIn, String generatorInfoIn) {
			this.icon = iconIn;
			this.iconMetadata = iconMetadataIn;
			this.name = nameIn;
			this.generatorInfo = generatorInfoIn;
		}

	}
	@SideOnly(Side.CLIENT)
	class ListSlot extends GuiSlot {

		public int field_148175_k = -1;

		public ListSlot() {
			super(GuiFlatDimensionPresets.this.mc, GuiFlatDimensionPresets.this.width,
					GuiFlatDimensionPresets.this.height, 80, GuiFlatDimensionPresets.this.height - 37, 24);
		}

		protected void drawBackground() {
		}

		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn,
				float partialTicks) {
			GuiFlatDimensionPresets.LayerItem guiflatpresets$layeritem = GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.get(entryID);
			this.renderIcon(insideLeft, yPos, guiflatpresets$layeritem.icon, guiflatpresets$layeritem.iconMetadata);
			GuiFlatDimensionPresets.this.fontRenderer.drawString(guiflatpresets$layeritem.name, insideLeft + 18 + 5, yPos + 6, 16777215);
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			this.field_148175_k = slotIndex;
			GuiFlatDimensionPresets.this.func_146426_g();
			GuiFlatDimensionPresets.this.field_146433_u.setText(GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.get(GuiFlatDimensionPresets.this.field_146435_s.field_148175_k).generatorInfo);
		}

		private void func_148171_c(int p_148171_1_, int p_148171_2_) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.renderEngine.bindTexture(Gui.STAT_ICONS);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(p_148171_1_, p_148171_2_ + 18.0F, GuiFlatDimensionPresets.this.zLevel).tex(0, 18.0F * 0.0078125F).endVertex();
			vertexbuffer.pos(p_148171_1_ + 18.0F, p_148171_2_ + 18.0F, GuiFlatDimensionPresets.this.zLevel).tex(0.140625F, 0.140625F).endVertex();
			vertexbuffer.pos(p_148171_1_ + 18.0F, p_148171_2_, GuiFlatDimensionPresets.this.zLevel).tex(0.140625F, 0.0F).endVertex();
			vertexbuffer.pos(p_148171_1_, p_148171_2_, GuiFlatDimensionPresets.this.zLevel).tex(0.0F, 0.0F).endVertex();
			tessellator.draw();
		}

		private void func_148173_e(int p_148173_1_, int p_148173_2_) {
			this.func_148171_c(p_148173_1_, p_148173_2_);
		}

		protected int getSize() {
			return GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.size();
		}

		protected boolean isSelected(int slotIndex) {
			return slotIndex == this.field_148175_k;
		}

		private void renderIcon(int p_178054_1_, int p_178054_2_, Item icon, int iconMetadata) {
			this.func_148173_e(p_178054_1_ + 1, p_178054_2_ + 1);
			GlStateManager.enableRescaleNormal();
			RenderHelper.enableGUIStandardItemLighting();
			GuiFlatDimensionPresets.this.itemRender.renderItemIntoGUI(new ItemStack(icon, 1, iconMetadata),
					p_178054_1_ + 2, p_178054_2_ + 2);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
		}

	}
	private static final List<GuiFlatDimensionPresets.LayerItem> FLAT_WORLD_PRESETS = Lists.newArrayList();
	static {
		registerPreset("Classic Flat", Item.getItemFromBlock(Blocks.GRASS), Biomes.PLAINS,
                Collections.singletonList("village"),
                new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(2, Blocks.DIRT),
                new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("Tunnelers' Dream", Item.getItemFromBlock(Blocks.STONE), Biomes.EXTREME_HILLS,
				Arrays.asList("biome_1", "dungeon", "decoration", "stronghold", "mineshaft"),
                new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(5, Blocks.DIRT),
                new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("Water World", Items.WATER_BUCKET, Biomes.DEEP_OCEAN,
				Arrays.asList("biome_1", "oceanmonument"),
                new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.SAND),
                new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE),
                new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("Overworld", Item.getItemFromBlock(Blocks.TALLGRASS), BlockTallGrass.EnumType.GRASS.getMeta(),
				Biomes.PLAINS,
				Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake"),
                new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT),
                new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("Snowy Kingdom", Item.getItemFromBlock(Blocks.SNOW_LAYER), Biomes.ICE_PLAINS,
				Arrays.asList("village", "biome_1"),
                new FlatLayerInfo(1, Blocks.SNOW_LAYER), new FlatLayerInfo(1, Blocks.GRASS),
                new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE),
                new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("Bottomless Pit", Items.FEATHER, Biomes.PLAINS,
				Arrays.asList("village", "biome_1"),
                new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT),
                new FlatLayerInfo(2, Blocks.COBBLESTONE));
		registerPreset("Desert", Item.getItemFromBlock(Blocks.SAND), Biomes.DESERT,
				Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"),
                new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE),
                new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset(
				new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE),
                new FlatLayerInfo(1, Blocks.BEDROCK));
		registerPreset("The Void", Item.getItemFromBlock(Blocks.BARRIER), Biomes.VOID,
                Collections.singletonList("decoration"),
                new FlatLayerInfo(1, Blocks.AIR));
	}
	private static void registerPreset(FlatLayerInfo... layers) {
		registerPreset("Redstone Ready", Items.REDSTONE, 0, Biomes.DESERT, null, layers);
	}
	private static void registerPreset(String name, Item icon, Biome biome, List<String> features, FlatLayerInfo... layers) {
		registerPreset(name, icon, 0, biome, features, layers);
	}
	private static void registerPreset(String name, Item icon, int iconMetadata, Biome biome, List<String> features, FlatLayerInfo... layers) {
		FlatGeneratorInfo flatgeneratorinfo = new FlatGeneratorInfo();
		for (int i = layers.length - 1; i >= 0; --i) {
			flatgeneratorinfo.getFlatLayers().add(layers[i]);
		}
		flatgeneratorinfo.setBiome(Biome.getIdForBiome(biome));
		flatgeneratorinfo.updateLayers();
		if (features != null) {
			for (String s : features) {
				flatgeneratorinfo.getWorldFeatures().put(s, Maps.newHashMap());
			}
		}
		FLAT_WORLD_PRESETS.add(new GuiFlatDimensionPresets.LayerItem(icon, iconMetadata, name, flatgeneratorinfo.toString()));
	}
	private final GuiCreateFlatDimension parentScreen;

	private String presetsTitle;

	private String presetsShare;

	private String field_146436_r;

	private GuiFlatDimensionPresets.ListSlot field_146435_s;

	private GuiButton field_146434_t;

	private GuiTextField field_146433_u;

	public GuiFlatDimensionPresets(GuiCreateFlatDimension p_i46318_1_) {
		this.parentScreen = p_i46318_1_;
	}

	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		if (button.id == 0 && this.func_146430_p()) {
			this.parentScreen.setPreset(this.field_146433_u.getText());
			this.mc.displayGuiScreen(this.parentScreen);
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(this.parentScreen);
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.field_146435_s.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, this.presetsTitle, this.width / 2, 8, 16777215);
		this.drawString(this.fontRenderer, this.presetsShare, 50, 30, 10526880);
		this.drawString(this.fontRenderer, this.field_146436_r, 50, 70, 10526880);
		this.field_146433_u.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void func_146426_g() {
        this.field_146434_t.enabled = this.func_146430_p();
	}

	private boolean func_146430_p() {
		return this.field_146435_s.field_148175_k > -1 && this.field_146435_s.field_148175_k < FLAT_WORLD_PRESETS.size()
				|| this.field_146433_u.getText().length() > 1;
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.field_146435_s.handleMouseInput();
	}

	public void initGui() {
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		this.presetsTitle = new TextComponentTranslation("createWorld.customize.presets.title").getFormattedText();
		this.presetsShare = new TextComponentTranslation("createWorld.customize.presets.share").getFormattedText();
		this.field_146436_r = new TextComponentTranslation("createWorld.customize.presets.list").getFormattedText();
		this.field_146433_u = new GuiTextField(2, this.fontRenderer, 50, 40, this.width - 100, 20);
		this.field_146435_s = new GuiFlatDimensionPresets.ListSlot();
		this.field_146433_u.setMaxStringLength(1230);
		this.field_146433_u.setText(this.parentScreen.getPreset());
		this.buttonList.add(this.field_146434_t = new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20,
				new TextComponentTranslation("createWorld.customize.presets.select").getFormattedText()));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20,
				new TextComponentTranslation("gui.cancel").getFormattedText()));
		this.func_146426_g();
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.field_146433_u.textboxKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		this.field_146433_u.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void updateScreen() {
		this.field_146433_u.updateCursorCounter();
		super.updateScreen();
	}

}