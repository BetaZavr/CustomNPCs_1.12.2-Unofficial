package noppes.npcs.client.gui.dimentions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiScreenCustomizeDimensionPresets extends GuiScreen {

	@SideOnly(Side.CLIENT)
	static class Info {

		public String field_178955_a;
		public ResourceLocation field_178953_b;
		public ChunkGeneratorSettings.Factory field_178954_c;

		public Info(String p_i45523_1_, ResourceLocation p_i45523_2_, ChunkGeneratorSettings.Factory p_i45523_3_) {
			field_178955_a = p_i45523_1_;
			field_178953_b = p_i45523_2_;
			field_178954_c = p_i45523_3_;
		}

	}
	@SideOnly(Side.CLIENT)
	class ListPreset extends GuiSlot {

		public int field_178053_u = -1;

		public ListPreset() {
			super(GuiScreenCustomizeDimensionPresets.this.mc, GuiScreenCustomizeDimensionPresets.this.width,
					GuiScreenCustomizeDimensionPresets.this.height, 80, GuiScreenCustomizeDimensionPresets.this.height - 32, 38);
		}

		@Override
		protected void drawBackground() { }

		@Override
		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
			GuiScreenCustomizeDimensionPresets.Info guiscreencustomizepresets$info = GuiScreenCustomizeDimensionPresets.field_175310_f.get(entryID);
			func_178051_a(insideLeft, yPos, guiscreencustomizepresets$info.field_178953_b);
			fontRenderer.drawString(guiscreencustomizepresets$info.field_178955_a, insideLeft + 32 + 10, yPos + 14, 16777215);
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			field_178053_u = slotIndex;
			func_175304_a();
			field_175317_i.setText(GuiScreenCustomizeDimensionPresets.field_175310_f.get(field_175311_g.field_178053_u).field_178954_c.toString());
		}

		private void func_178051_a(int p_178051_1_, int p_178051_2_, ResourceLocation p_178051_3_) {
			int i = p_178051_1_ + 5;
			drawHorizontalLine(i - 1, i + 32, p_178051_2_ - 1, 0xFE0E0E0);
			drawHorizontalLine(i - 1, i + 32, p_178051_2_ + 32, 0xFFA0A0A0);
			drawVerticalLine(i - 1, p_178051_2_ - 1, p_178051_2_ + 32, 0xFE0E0E0);
			drawVerticalLine(i + 32, p_178051_2_ - 1, p_178051_2_ + 32, 0xFFA0A0A0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(p_178051_3_);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(i, p_178051_2_ + 32, 0.0D).tex(0.0D, 1.0D).endVertex();
			vertexbuffer.pos(i + 32, p_178051_2_ + 32, 0.0D).tex(1.0D, 1.0D).endVertex();
			vertexbuffer.pos(i + 32, p_178051_2_, 0.0D).tex(1.0D, 0.0D).endVertex();
			vertexbuffer.pos(i, p_178051_2_, 0.0D).tex(0.0D, 0.0D).endVertex();
			tessellator.draw();
		}

		@Override
		protected int getSize() {
			return GuiScreenCustomizeDimensionPresets.field_175310_f.size();
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == field_178053_u;
		}

	}
	private static final List<GuiScreenCustomizeDimensionPresets.Info> field_175310_f = new ArrayList<>();
	static {
		ChunkGeneratorSettings.Factory ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{ \"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":8.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":0.5, \"biomeScaleWeight\":2.0, \"biomeScaleOffset\":0.375, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":255 }");
		ResourceLocation resourcelocation = new ResourceLocation("textures/gui/presets/water.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.waterWorld").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":3000.0, \"heightScale\":6000.0, \"upperLimitScale\":250.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/isles.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.isleLand").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":5.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":1.0, \"biomeScaleWeight\":4.0, \"biomeScaleOffset\":1.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/delight.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.caveDelight").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":738.41864, \"heightScale\":157.69133, \"upperLimitScale\":801.4267, \"lowerLimitScale\":1254.1643, \"depthNoiseScaleX\":374.93652, \"depthNoiseScaleZ\":288.65228, \"depthNoiseScaleExponent\":1.2092624, \"mainNoiseScaleX\":1355.9908, \"mainNoiseScaleY\":745.5343, \"mainNoiseScaleZ\":1183.464, \"baseSize\":1.8758626, \"stretchY\":1.7137525, \"biomeDepthWeight\":1.7553768, \"biomeDepthOffset\":3.4701107, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":2.535211, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/madness.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.mountains").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":1000.0, \"mainNoiseScaleY\":3000.0, \"mainNoiseScaleZ\":1000.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":20 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/drought.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.drought").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":2.0, \"lowerLimitScale\":64.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":6 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/chaos.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.caveChaos").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
		ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":true, \"seaLevel\":40 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/luck.png");
		field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(new TextComponentTranslation("createWorld.customize.custom.preset.goodLuck").getFormattedText(), resourcelocation, ChunkGeneratorSettings$factory));
	}
	private GuiScreenCustomizeDimensionPresets.ListPreset field_175311_g;
	private GuiButton field_175316_h;
	private GuiTextField field_175317_i;
	private final GuiCustomizeDimension customizeDimensionGui;

	protected String field_175315_a = "Customize World Presets";

	private String field_175313_s;

	private String field_175312_t;

	public GuiScreenCustomizeDimensionPresets(GuiCustomizeDimension customizeDimensionGuiIn) {
		customizeDimensionGui = customizeDimensionGuiIn;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			customizeDimensionGui.func_175324_a(field_175317_i.getText());
			mc.displayGuiScreen(customizeDimensionGui);
			break;
		case 1:
			mc.displayGuiScreen(customizeDimensionGui);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		field_175311_g.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, field_175315_a, width / 2, 8, 16777215);
		drawString(fontRenderer, field_175313_s, 50, 30, 10526880);
		drawString(fontRenderer, field_175312_t, 50, 70, 10526880);
		field_175317_i.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void func_175304_a() { field_175316_h.enabled = func_175305_g(); }

	private boolean func_175305_g() {
		return field_175311_g.field_178053_u > -1 && field_175311_g.field_178053_u < field_175310_f.size() || field_175317_i.getText().length() > 1;
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		field_175311_g.handleMouseInput();
	}

	@Override
	public void initGui() {
		buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		field_175315_a = new TextComponentTranslation("createWorld.customize.custom.presets.title").getFormattedText();
		field_175313_s = new TextComponentTranslation("createWorld.customize.presets.share").getFormattedText();
		field_175312_t = new TextComponentTranslation("createWorld.customize.presets.list").getFormattedText();
		field_175317_i = new GuiTextField(2, fontRenderer, 50, 40, width - 100, 20);
		field_175311_g = new GuiScreenCustomizeDimensionPresets.ListPreset();
		field_175317_i.setMaxStringLength(2000);
		field_175317_i.setText(customizeDimensionGui.func_175323_a());
		buttonList.add(field_175316_h = new GuiButton(0, width / 2 - 102, height - 27, 100, 20, new TextComponentTranslation("createWorld.customize.presets.select").getFormattedText()));
		buttonList.add(new GuiButton(1, width / 2 + 3, height - 27, 100, 20, new TextComponentTranslation("gui.cancel").getFormattedText()));
		func_175304_a();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!field_175317_i.textboxKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		field_175317_i.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		field_175317_i.updateCursorCounter();
		super.updateScreen();
	}

}