package noppes.npcs.client.gui.dimentions;

import java.io.IOException;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiCustomizeDimension extends GuiScreen
		implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder {

	private final GuiCreateDimension field_175343_i;
	protected String field_175341_a = "Customize Dimension Settings";
	protected String field_175333_f = "Page 1 of 3";
	protected String field_175335_g = "Basic Settings";
	protected String[] field_175342_h = new String[4];
	private GuiPageButtonList field_175349_r;
	private GuiButton field_175348_s;
	private GuiButton field_175347_t;
	private GuiButton field_175346_u;
	private GuiButton field_175345_v;
	private GuiButton field_175344_w;
	private GuiButton field_175352_x;
	private GuiButton field_175351_y;
	private GuiButton field_175350_z;
	private boolean field_175338_A = false;
	private int field_175339_B = 0;
	private boolean field_175340_C = false;
	private final Predicate<String> field_175332_D = new Predicate<String>() {

		@Override
		public boolean apply(String p_apply_1_) { return tryParseValidFloat(p_apply_1_); }

		public boolean tryParseValidFloat(String p_178956_1_) {
			Float f = null;
			try { f = Float.parseFloat(p_178956_1_); }
			catch (Exception e) { LogWriter.error(e); }
			return p_178956_1_.isEmpty() || f != null && Floats.isFinite(f) && f >= 0.0F;
		}

	};
	private final ChunkGeneratorSettings.Factory field_175334_E = new ChunkGeneratorSettings.Factory();
	private ChunkGeneratorSettings.Factory field_175336_F;
	/** A Random instance for this world customization */
	private final Random random = new Random();

	public GuiCustomizeDimension(GuiScreen p_i45521_1_, String p_i45521_2_) {
		field_175343_i = (GuiCreateDimension) p_i45521_1_;
		func_175324_a(p_i45521_2_);
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		if (button.enabled) {
			switch (button.id) {
			case 300:
				field_175343_i.chunkProviderSettingsJson = field_175336_F.toString();
				mc.displayGuiScreen(field_175343_i);
				break;
			case 301:
				for (int i = 0; i < field_175349_r.getSize(); ++i) {
					GuiPageButtonList.GuiEntry guientry = field_175349_r.getListEntry(i);
					Gui gui = guientry.getComponent1();
					if (gui instanceof GuiButton) {
						GuiButton guibutton = (GuiButton) gui;
						if (guibutton instanceof GuiSlider) {
							float f = ((GuiSlider) guibutton).getSliderPosition()
									* (0.75F + random.nextFloat() * 0.5F)
									+ (random.nextFloat() * 0.1F - 0.05F);
							((GuiSlider) guibutton).setSliderPosition(MathHelper.clamp(f, 0.0F, 1.0F));
						}
						else if (guibutton instanceof GuiListButton) { ((GuiListButton) guibutton).setValue(random.nextBoolean()); }
					}
					Gui gui1 = guientry.getComponent2();
					if (gui1 instanceof GuiButton) {
						GuiButton guibutton = (GuiButton) gui1;
						if (guibutton instanceof GuiSlider) {
							float f1 = ((GuiSlider) guibutton).getSliderPosition()
									* (0.75F + random.nextFloat() * 0.5F)
									+ (random.nextFloat() * 0.1F - 0.05F);
							((GuiSlider) guibutton).setSliderPosition(MathHelper.clamp(f1, 0.0F, 1.0F));
						}
						else if (guibutton instanceof GuiListButton) { ((GuiListButton) guibutton).setValue(random.nextBoolean()); }
					}
				}

				return;
			case 302:
				field_175349_r.previousPage();
				func_175328_i();
				break;
			case 303:
				field_175349_r.nextPage();
				func_175328_i();
				break;
			case 304:
				if (field_175338_A) { func_175322_b(); }
				break;
			case 305:
				mc.displayGuiScreen(new GuiScreenCustomizeDimensionPresets(this));
				break;
			case 306:
				func_175331_h();
				break;
			case 307:
				field_175339_B = 0;
				func_175331_h();
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		field_175349_r.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, field_175341_a, width / 2, 2, 0xFFFFFF);
		drawCenteredString(fontRenderer, field_175333_f, width / 2, 12, 0xFFFFFF);
		drawCenteredString(fontRenderer, field_175335_g, width / 2, 22, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (field_175339_B != 0) {
			drawRect(0, 0, width, height, Integer.MIN_VALUE);
			drawHorizontalLine(width / 2 - 91, width / 2 + 90, 99, 0xFFE0E0E0);
			drawHorizontalLine(width / 2 - 91, width / 2 + 90, 185, 0xFFA0A0A0);
			drawVerticalLine(width / 2 - 91, 99, 185, 0xFFE0E0E0);
			drawVerticalLine(width / 2 + 90, 99, 185, 0xFFA0A0A0);
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexBuffer = tessellator.getBuffer();
			mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(width / 2.0D - 90.0D, 185.0D, 0.0D).tex(0.0D, 2.65625D).color(64, 64, 64, 64).endVertex();
			vertexBuffer.pos(width / 2.0D + 90.0D, 185.0D, 0.0D).tex(5.625D, 2.65625D).color(64, 64, 64, 64).endVertex();
			vertexBuffer.pos(width / 2.0D + 90.0D, 100.0D, 0.0D).tex(5.625D, 0.0D).color(64, 64, 64, 64).endVertex();
			vertexBuffer.pos(width / 2.0D - 90.0D, 100.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 64).endVertex();
			tessellator.draw();
			drawCenteredString(fontRenderer,
					new TextComponentTranslation("createWorld.customize.custom.confirmTitle").getFormattedText(),
					width / 2, 105, 0xFFFFFF);
			drawCenteredString(fontRenderer,
					new TextComponentTranslation("createWorld.customize.custom.confirm1").getFormattedText(),
					width / 2, 125, 0xFFFFFF);
			drawCenteredString(fontRenderer,
					new TextComponentTranslation("createWorld.customize.custom.confirm2").getFormattedText(),
					width / 2, 135, 0xFFFFFF);
			field_175352_x.drawButton(mc, mouseX, mouseY, partialTicks);
			field_175351_y.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}

	private void func_175322_b() {
		field_175339_B = 304;
		func_175329_a(true);
	}

	public String func_175323_a() {
		return field_175336_F.toString().replace("\n", "");
	}

	public void func_175324_a(String p_175324_1_) {
		if (p_175324_1_ != null && !p_175324_1_.isEmpty()) { field_175336_F = ChunkGeneratorSettings.Factory.jsonToFactory(p_175324_1_); }
		else { field_175336_F = new ChunkGeneratorSettings.Factory(); }
	}

	private void func_175325_f() {
		GuiPageButtonList.GuiListEntry[] guiListentry = new GuiPageButtonList.GuiListEntry[] {
				new GuiPageButtonList.GuiSlideEntry(160,
						new TextComponentTranslation("createWorld.customize.custom.seaLevel").getFormattedText(), true,
						this, 1.0F, 255.0F, field_175336_F.seaLevel),
				new GuiPageButtonList.GuiButtonEntry(148,
						new TextComponentTranslation("createWorld.customize.custom.useCaves").getFormattedText(), true,
						field_175336_F.useCaves),
				new GuiPageButtonList.GuiButtonEntry(150,
						new TextComponentTranslation("createWorld.customize.custom.useStrongholds").getFormattedText(),
						true, field_175336_F.useStrongholds),
				new GuiPageButtonList.GuiButtonEntry(151,
						new TextComponentTranslation("createWorld.customize.custom.useVillages").getFormattedText(),
						true, field_175336_F.useVillages),
				new GuiPageButtonList.GuiButtonEntry(152,
						new TextComponentTranslation("createWorld.customize.custom.useMineShafts").getFormattedText(),
						true, field_175336_F.useMineShafts),
				new GuiPageButtonList.GuiButtonEntry(153,
						new TextComponentTranslation("createWorld.customize.custom.useTemples").getFormattedText(),
						true, field_175336_F.useTemples),
				new GuiPageButtonList.GuiButtonEntry(210,
						new TextComponentTranslation("createWorld.customize.custom.useMonuments").getFormattedText(),
						true, field_175336_F.useMonuments),
				new GuiPageButtonList.GuiButtonEntry(154,
						new TextComponentTranslation("createWorld.customize.custom.useRavines").getFormattedText(),
						true, field_175336_F.useRavines),
				new GuiPageButtonList.GuiButtonEntry(149,
						new TextComponentTranslation("createWorld.customize.custom.useDungeons").getFormattedText(),
						true, field_175336_F.useDungeons),
				new GuiPageButtonList.GuiSlideEntry(157,
						new TextComponentTranslation("createWorld.customize.custom.dungeonChance").getFormattedText(),
						true, this, 1.0F, 100.0F, field_175336_F.dungeonChance),
				new GuiPageButtonList.GuiButtonEntry(155,
						new TextComponentTranslation("createWorld.customize.custom.useWaterLakes").getFormattedText(),
						true, field_175336_F.useWaterLakes),
				new GuiPageButtonList.GuiSlideEntry(158,
						new TextComponentTranslation("createWorld.customize.custom.waterLakeChance").getFormattedText(),
						true, this, 1.0F, 100.0F, field_175336_F.waterLakeChance),
				new GuiPageButtonList.GuiButtonEntry(156,
						new TextComponentTranslation("createWorld.customize.custom.useLavaLakes").getFormattedText(),
						true, field_175336_F.useLavaLakes),
				new GuiPageButtonList.GuiSlideEntry(159,
						new TextComponentTranslation("createWorld.customize.custom.lavaLakeChance").getFormattedText(),
						true, this, 10.0F, 100.0F, field_175336_F.lavaLakeChance),
				new GuiPageButtonList.GuiButtonEntry(161,
						new TextComponentTranslation("createWorld.customize.custom.useLavaOceans").getFormattedText(),
						true, field_175336_F.useLavaOceans),
				new GuiPageButtonList.GuiSlideEntry(162,
						new TextComponentTranslation("createWorld.customize.custom.fixedBiome").getFormattedText(),
						true, this, -1.0F, 37.0F, field_175336_F.fixedBiome),
				new GuiPageButtonList.GuiSlideEntry(163,
						new TextComponentTranslation("createWorld.customize.custom.biomeSize").getFormattedText(), true,
						this, 1.0F, 8.0F, field_175336_F.biomeSize),
				new GuiPageButtonList.GuiSlideEntry(164,
						new TextComponentTranslation("createWorld.customize.custom.riverSize").getFormattedText(), true,
						this, 1.0F, 5.0F, field_175336_F.riverSize) };
		GuiPageButtonList.GuiListEntry[] guiListentry_2 = new GuiPageButtonList.GuiListEntry[] {
				new GuiPageButtonList.GuiLabelEntry(416,
						new TextComponentTranslation("tile.dirt.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(165,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.dirtSize),
				new GuiPageButtonList.GuiSlideEntry(166,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.dirtCount),
				new GuiPageButtonList.GuiSlideEntry(167,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.dirtMinHeight),
				new GuiPageButtonList.GuiSlideEntry(168,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.dirtMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(417,
						new TextComponentTranslation("tile.gravel.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(169,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.gravelSize),
				new GuiPageButtonList.GuiSlideEntry(170,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.gravelCount),
				new GuiPageButtonList.GuiSlideEntry(171,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.gravelMinHeight),
				new GuiPageButtonList.GuiSlideEntry(172,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.gravelMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(418,
						new TextComponentTranslation("tile.stone.granite.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(173,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.graniteSize),
				new GuiPageButtonList.GuiSlideEntry(174,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.graniteCount),
				new GuiPageButtonList.GuiSlideEntry(175,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.graniteMinHeight),
				new GuiPageButtonList.GuiSlideEntry(176,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.graniteMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(419, new TextComponentTranslation("tile.stone.diorite.name").getFormattedText(), false), null,
				new GuiPageButtonList.GuiSlideEntry(177,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.dioriteSize),
				new GuiPageButtonList.GuiSlideEntry(178,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.dioriteCount),
				new GuiPageButtonList.GuiSlideEntry(179,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.dioriteMinHeight),
				new GuiPageButtonList.GuiSlideEntry(180,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.dioriteMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(420,
						new TextComponentTranslation("tile.stone.andesite.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(181,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.andesiteSize),
				new GuiPageButtonList.GuiSlideEntry(182,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.andesiteCount),
				new GuiPageButtonList.GuiSlideEntry(183,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.andesiteMinHeight),
				new GuiPageButtonList.GuiSlideEntry(184,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.andesiteMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(421,
						new TextComponentTranslation("tile.oreCoal.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(185,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.coalSize),
				new GuiPageButtonList.GuiSlideEntry(186,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.coalCount),
				new GuiPageButtonList.GuiSlideEntry(187,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.coalMinHeight),
				new GuiPageButtonList.GuiSlideEntry(189,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.coalMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(422,
						new TextComponentTranslation("tile.oreIron.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(190,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.ironSize),
				new GuiPageButtonList.GuiSlideEntry(191,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.ironCount),
				new GuiPageButtonList.GuiSlideEntry(192,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.ironMinHeight),
				new GuiPageButtonList.GuiSlideEntry(193,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.ironMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(423,
						new TextComponentTranslation("tile.oreGold.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(194,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.goldSize),
				new GuiPageButtonList.GuiSlideEntry(195,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.goldCount),
				new GuiPageButtonList.GuiSlideEntry(196,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.goldMinHeight),
				new GuiPageButtonList.GuiSlideEntry(197,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.goldMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(424,
						new TextComponentTranslation("tile.oreRedstone.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(198,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.redstoneSize),
				new GuiPageButtonList.GuiSlideEntry(199,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.redstoneCount),
				new GuiPageButtonList.GuiSlideEntry(200,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.redstoneMinHeight),
				new GuiPageButtonList.GuiSlideEntry(201,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.redstoneMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(425,
						new TextComponentTranslation("tile.oreDiamond.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(202,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.diamondSize),
				new GuiPageButtonList.GuiSlideEntry(203,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.diamondCount),
				new GuiPageButtonList.GuiSlideEntry(204,
						new TextComponentTranslation("createWorld.customize.custom.minHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.diamondMinHeight),
				new GuiPageButtonList.GuiSlideEntry(205,
						new TextComponentTranslation("createWorld.customize.custom.maxHeight").getFormattedText(),
						false, this, 0.0F, 255.0F, field_175336_F.diamondMaxHeight),
				new GuiPageButtonList.GuiLabelEntry(426,
						new TextComponentTranslation("tile.oreLapis.name").getFormattedText(), false),
				null,
				new GuiPageButtonList.GuiSlideEntry(206,
						new TextComponentTranslation("createWorld.customize.custom.size").getFormattedText(), false,
						this, 1.0F, 50.0F, field_175336_F.lapisSize),
				new GuiPageButtonList.GuiSlideEntry(207,
						new TextComponentTranslation("createWorld.customize.custom.count").getFormattedText(), false,
						this, 0.0F, 40.0F, field_175336_F.lapisCount),
				new GuiPageButtonList.GuiSlideEntry(208,
						new TextComponentTranslation("createWorld.customize.custom.center").getFormattedText(), false,
						this, 0.0F, 255.0F, field_175336_F.lapisCenterHeight),
				new GuiPageButtonList.GuiSlideEntry(209,
						new TextComponentTranslation("createWorld.customize.custom.spread").getFormattedText(), false,
						this, 0.0F, 255.0F, field_175336_F.lapisSpread) };
		GuiPageButtonList.GuiListEntry[] guiListentry_3 = new GuiPageButtonList.GuiListEntry[]{
				new GuiPageButtonList.GuiSlideEntry(100,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleX").getFormattedText(),
						false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleX),
				new GuiPageButtonList.GuiSlideEntry(101,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleY").getFormattedText(),
						false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleY),
				new GuiPageButtonList.GuiSlideEntry(102,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleZ").getFormattedText(),
						false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleZ),
				new GuiPageButtonList.GuiSlideEntry(103,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleX")
								.getFormattedText(),
						false, this, 1.0F, 2000.0F, field_175336_F.depthNoiseScaleX),
				new GuiPageButtonList.GuiSlideEntry(104,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleZ")
								.getFormattedText(),
						false, this, 1.0F, 2000.0F, field_175336_F.depthNoiseScaleZ),
				new GuiPageButtonList.GuiSlideEntry(105,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleExponent")
								.getFormattedText(),
						false, this, 0.01F, 20.0F, field_175336_F.depthNoiseScaleExponent),
				new GuiPageButtonList.GuiSlideEntry(106,
						new TextComponentTranslation("createWorld.customize.custom.baseSize").getFormattedText(), false,
						this, 1.0F, 25.0F, field_175336_F.baseSize),
				new GuiPageButtonList.GuiSlideEntry(107,
						new TextComponentTranslation("createWorld.customize.custom.coordinateScale").getFormattedText(),
						false, this, 1.0F, 6000.0F, field_175336_F.coordinateScale),
				new GuiPageButtonList.GuiSlideEntry(108,
						new TextComponentTranslation("createWorld.customize.custom.heightScale").getFormattedText(),
						false, this, 1.0F, 6000.0F, field_175336_F.heightScale),
				new GuiPageButtonList.GuiSlideEntry(109,
						new TextComponentTranslation("createWorld.customize.custom.stretchY").getFormattedText(), false,
						this, 0.01F, 50.0F, field_175336_F.stretchY),
				new GuiPageButtonList.GuiSlideEntry(110,
						new TextComponentTranslation("createWorld.customize.custom.upperLimitScale").getFormattedText(),
						false, this, 1.0F, 5000.0F, field_175336_F.upperLimitScale),
				new GuiPageButtonList.GuiSlideEntry(111,
						new TextComponentTranslation("createWorld.customize.custom.lowerLimitScale").getFormattedText(),
						false, this, 1.0F, 5000.0F, field_175336_F.lowerLimitScale),
				new GuiPageButtonList.GuiSlideEntry(112,
						new TextComponentTranslation("createWorld.customize.custom.biomeDepthWeight")
								.getFormattedText(),
						false, this, 1.0F, 20.0F, field_175336_F.biomeDepthWeight),
				new GuiPageButtonList.GuiSlideEntry(113,
						new TextComponentTranslation("createWorld.customize.custom.biomeDepthOffset")
								.getFormattedText(),
						false, this, 0.0F, 20.0F, field_175336_F.biomeDepthOffset),
				new GuiPageButtonList.GuiSlideEntry(114,
						new TextComponentTranslation("createWorld.customize.custom.biomeScaleWeight")
								.getFormattedText(),
						false, this, 1.0F, 20.0F, field_175336_F.biomeScaleWeight),
				new GuiPageButtonList.GuiSlideEntry(115,
						new TextComponentTranslation("createWorld.customize.custom.biomeScaleOffset")
								.getFormattedText(),
						false, this, 0.0F, 20.0F, field_175336_F.biomeScaleOffset)};
		GuiPageButtonList.GuiListEntry[] guiListentry_4 = new GuiPageButtonList.GuiListEntry[] {
				new GuiPageButtonList.GuiLabelEntry(400,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleX").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(132,
						String.format("%5.3f", field_175336_F.mainNoiseScaleX),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(401,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleY").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(133,
						String.format("%5.3f", field_175336_F.mainNoiseScaleY),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(402,
						new TextComponentTranslation("createWorld.customize.custom.mainNoiseScaleZ").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(134,
						String.format("%5.3f", field_175336_F.mainNoiseScaleZ),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(403,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleX").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(135,
						String.format("%5.3f", field_175336_F.depthNoiseScaleX),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(404,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleZ").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(136,
						String.format("%5.3f", field_175336_F.depthNoiseScaleZ),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(405,
						new TextComponentTranslation("createWorld.customize.custom.depthNoiseScaleExponent")
								.getFormattedText() + ":",
						false),
				new GuiPageButtonList.EditBoxEntry(137,
						String.format("%2.3f", field_175336_F.depthNoiseScaleExponent),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(406,
						new TextComponentTranslation("createWorld.customize.custom.baseSize").getFormattedText() + ":",
						false),
				new GuiPageButtonList.EditBoxEntry(138,
						String.format("%2.3f", field_175336_F.baseSize), false,
						field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(407,
						new TextComponentTranslation("createWorld.customize.custom.coordinateScale").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(139,
						String.format("%5.3f", field_175336_F.coordinateScale),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(408,
						new TextComponentTranslation("createWorld.customize.custom.heightScale").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(140,
						String.format("%5.3f", field_175336_F.heightScale), false,
						field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(409,
						new TextComponentTranslation("createWorld.customize.custom.stretchY").getFormattedText() + ":",
						false),
				new GuiPageButtonList.EditBoxEntry(141,
						String.format("%2.3f", field_175336_F.stretchY), false,
						field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(410,
						new TextComponentTranslation("createWorld.customize.custom.upperLimitScale").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(142,
						String.format("%5.3f", field_175336_F.upperLimitScale),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(411,
						new TextComponentTranslation("createWorld.customize.custom.lowerLimitScale").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(143,
						String.format("%5.3f", field_175336_F.lowerLimitScale),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(412,
						new TextComponentTranslation("createWorld.customize.custom.biomeDepthWeight").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(144,
						String.format("%2.3f", field_175336_F.biomeDepthWeight),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(413,
						new TextComponentTranslation("createWorld.customize.custom.biomeDepthOffset").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(145,
						String.format("%2.3f", field_175336_F.biomeDepthOffset),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(414,
						new TextComponentTranslation("createWorld.customize.custom.biomeScaleWeight").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(146,
						String.format("%2.3f", field_175336_F.biomeScaleWeight),
						false, field_175332_D),
				new GuiPageButtonList.GuiLabelEntry(415,
						new TextComponentTranslation("createWorld.customize.custom.biomeScaleOffset").getFormattedText()
								+ ":",
						false),
				new GuiPageButtonList.EditBoxEntry(147,
						String.format("%2.3f", field_175336_F.biomeScaleOffset),
						false, field_175332_D) };
		field_175349_r = new GuiPageButtonList(mc, width, height, 32, height - 32, 25, this,
				guiListentry, guiListentry_2, guiListentry_3,
				guiListentry_4);
		for (int i = 0; i < 4; ++i) {
			field_175342_h[i] = new TextComponentTranslation("createWorld.customize.custom.page" + i)
					.getFormattedText();
		}
		func_175328_i();
	}

	private void func_175326_g() {
		field_175336_F.setDefaults();
		func_175325_f();
	}

	private void func_175327_a(float p_175327_1_) {
		Gui gui = field_175349_r.getFocusedControl();
		if (gui instanceof GuiTextField) {
			float f1 = p_175327_1_;
			if (GuiScreen.isShiftKeyDown()) {
				f1 = p_175327_1_ * 0.1F;
				if (GuiScreen.isCtrlKeyDown()) {
					f1 *= 0.1F;
				}
			} else if (GuiScreen.isCtrlKeyDown()) {
				f1 = p_175327_1_ * 10.0F;
				if (GuiScreen.isAltKeyDown()) {
					f1 *= 10.0F;
				}
			}
			GuiTextField guitextfield = (GuiTextField) gui;
			Float f2 = null;
			try { f2 = Float.parseFloat(guitextfield.getText()); } catch (Exception e) { LogWriter.error(e); }
			if (f2 != null) {
				f2 = f2 + f1;
				int i = guitextfield.getId();
				String s = func_175330_b(guitextfield.getId(), f2);
				guitextfield.setText(s);
				setEntryValue(i, s);
			}
		}
	}

	private void func_175328_i() {
		field_175345_v.enabled = field_175349_r.getPage() != 0;
		field_175344_w.enabled = field_175349_r.getPage() != field_175349_r.getPageCount() - 1;
		field_175333_f = new TextComponentTranslation("book.pageIndicator", field_175349_r.getPage() + 1, field_175349_r.getPageCount()).getFormattedText();
		field_175335_g = field_175342_h[field_175349_r.getPage()];
		field_175347_t.enabled = field_175349_r.getPage() != field_175349_r.getPageCount() - 1;
	}

	private void func_175329_a(boolean p_175329_1_) {
		field_175352_x.visible = p_175329_1_;
		field_175351_y.visible = p_175329_1_;
		field_175347_t.enabled = !p_175329_1_;
		field_175348_s.enabled = !p_175329_1_;
		field_175345_v.enabled = !p_175329_1_;
		field_175344_w.enabled = !p_175329_1_;
		field_175346_u.enabled = !p_175329_1_;
		field_175350_z.enabled = !p_175329_1_;
	}

	private String func_175330_b(int p_175330_1_, float p_175330_2_) {
		switch (p_175330_1_) {
			case 100:
			case 101:
			case 102:
			case 103:
			case 104:
			case 107:
			case 108:
			case 110:
			case 111:
			case 132:
			case 133:
			case 134:
			case 135:
			case 136:
			case 139:
			case 140:
			case 142:
			case 143:
				return String.format("%5.3f", p_175330_2_);
			case 105:
			case 106:
			case 109:
			case 112:
			case 113:
			case 114:
			case 115:
			case 137:
			case 138:
			case 141:
			case 144:
			case 145:
			case 146:
			case 147:
				return String.format("%2.3f", p_175330_2_);
            case 162:
				if (p_175330_2_ < 0.0F) {
					return new TextComponentTranslation("gui.all").getFormattedText();
				} else {
					Biome biome_gen_base;
					if ((int) p_175330_2_ >= Biome.getIdForBiome(Biomes.HELL)) {
						biome_gen_base = Biome.getBiome((int) p_175330_2_ + 2);
					} else {
						biome_gen_base = Biome.getBiome((int) p_175330_2_);
					}
					return biome_gen_base != null ? biome_gen_base.getBiomeName() : "?";
				}
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            default:
				return String.format("%d", (int) p_175330_2_);
		}
	}

	private void func_175331_h() throws IOException {
		switch (field_175339_B) {
		case 300:
			actionPerformed((GuiListButton) field_175349_r.getComponent(300));
			break;
		case 304:
			func_175326_g();
		}
		field_175339_B = 0;
		field_175340_C = true;
		func_175329_a(false);
	}

	@Nonnull
	@Override
	public String getText(int p_175318_1_, @Nonnull String p_175318_2_, float p_175318_3_) {
		return p_175318_2_ + ": " + func_175330_b(p_175318_1_, p_175318_3_);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		field_175349_r.handleMouseInput();
	}

	@Override
	public void initGui() {
		field_175341_a = new TextComponentTranslation("dimensions.customize.title").getFormattedText();
		buttonList.clear();
		buttonList.add(field_175345_v = new GuiButton(302, 20, 5, 80, 20,
				new TextComponentTranslation("createWorld.customize.custom.prev").getFormattedText()));
		buttonList.add(field_175344_w = new GuiButton(303, width - 100, 5, 80, 20,
				new TextComponentTranslation("createWorld.customize.custom.next").getFormattedText()));
		buttonList.add(field_175346_u = new GuiButton(304, width / 2 - 187, height - 27, 90, 20,
				new TextComponentTranslation("createWorld.customize.custom.defaults").getFormattedText()));
		buttonList.add(field_175347_t = new GuiButton(301, width / 2 - 92, height - 27, 90, 20,
				new TextComponentTranslation("createWorld.customize.custom.randomize").getFormattedText()));
		buttonList.add(field_175350_z = new GuiButton(305, width / 2 + 3, height - 27, 90, 20,
				new TextComponentTranslation("createWorld.customize.custom.presets").getFormattedText()));
		buttonList.add(field_175348_s = new GuiButton(300, width / 2 + 98, height - 27, 90, 20,
				new TextComponentTranslation("gui.done").getFormattedText()));
		field_175352_x = new GuiButton(306, width / 2 - 55, 160, 50, 20,
				new TextComponentTranslation("gui.yes").getFormattedText());
		field_175352_x.visible = false;
		buttonList.add(field_175352_x);
		field_175351_y = new GuiButton(307, width / 2 + 5, 160, 50, 20,
				new TextComponentTranslation("gui.no").getFormattedText());
		field_175351_y.visible = false;
		buttonList.add(field_175351_y);
		func_175325_f();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (field_175339_B == 0) {
			switch (keyCode) {
				case 200: func_175327_a(1.0F);break;
				case 208: func_175327_a(-1.0F);break;
				default: field_175349_r.onKeyPressed(typedChar, keyCode);
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (field_175339_B == 0 && !field_175340_C) { field_175349_r.mouseClicked(mouseX, mouseY, mouseButton); }
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (field_175340_C) {
			field_175340_C = false;
		} else if (field_175339_B == 0) {
			field_175349_r.mouseReleased(mouseX, mouseY, state);
		}
	}

	@Override
	public void setEntryValue(int p_175321_1_, boolean p_175321_2_) {
		switch (p_175321_1_) {
			case 148: field_175336_F.useCaves = p_175321_2_; break;
			case 149: field_175336_F.useDungeons = p_175321_2_; break;
			case 150: field_175336_F.useStrongholds = p_175321_2_; break;
			case 151: field_175336_F.useVillages = p_175321_2_; break;
			case 152: field_175336_F.useMineShafts = p_175321_2_; break;
			case 153: field_175336_F.useTemples = p_175321_2_; break;
			case 154: field_175336_F.useRavines = p_175321_2_; break;
			case 155: field_175336_F.useWaterLakes = p_175321_2_; break;
			case 156: field_175336_F.useLavaLakes = p_175321_2_; break;
			case 161: field_175336_F.useLavaOceans = p_175321_2_; break;
			case 210: field_175336_F.useMonuments = p_175321_2_;
		}
		if (!field_175336_F.equals(field_175334_E)) {
			field_175338_A = true;
		}
	}

	@Override
	public void setEntryValue(int p_175320_1_, float p_175320_2_) {
		switch (p_175320_1_) {
			case 100: field_175336_F.mainNoiseScaleX = p_175320_2_; break;
			case 101: field_175336_F.mainNoiseScaleY = p_175320_2_; break;
			case 102: field_175336_F.mainNoiseScaleZ = p_175320_2_; break;
			case 103: field_175336_F.depthNoiseScaleX = p_175320_2_; break;
			case 104: field_175336_F.depthNoiseScaleZ = p_175320_2_; break;
			case 105: field_175336_F.depthNoiseScaleExponent = p_175320_2_; break;
			case 106: field_175336_F.baseSize = p_175320_2_; break;
			case 107: field_175336_F.coordinateScale = p_175320_2_; break;
			case 108: field_175336_F.heightScale = p_175320_2_; break;
			case 109: field_175336_F.stretchY = p_175320_2_; break;
			case 110: field_175336_F.upperLimitScale = p_175320_2_; break;
			case 111: field_175336_F.lowerLimitScale = p_175320_2_; break;
			case 112: field_175336_F.biomeDepthWeight = p_175320_2_; break;
			case 113: field_175336_F.biomeDepthOffset = p_175320_2_; break;
			case 114: field_175336_F.biomeScaleWeight = p_175320_2_; break;
			case 115: field_175336_F.biomeScaleOffset = p_175320_2_;
            case 157: field_175336_F.dungeonChance = (int) p_175320_2_; break;
			case 158: field_175336_F.waterLakeChance = (int) p_175320_2_; break;
			case 159: field_175336_F.lavaLakeChance = (int) p_175320_2_; break;
			case 160: field_175336_F.seaLevel = (int) p_175320_2_; break;
			case 162: field_175336_F.fixedBiome = (int) p_175320_2_; break;
			case 163: field_175336_F.biomeSize = (int) p_175320_2_; break;
			case 164: field_175336_F.riverSize = (int) p_175320_2_; break;
			case 165: field_175336_F.dirtSize = (int) p_175320_2_; break;
			case 166: field_175336_F.dirtCount = (int) p_175320_2_; break;
			case 167: field_175336_F.dirtMinHeight = (int) p_175320_2_; break;
			case 168: field_175336_F.dirtMaxHeight = (int) p_175320_2_; break;
			case 169: field_175336_F.gravelSize = (int) p_175320_2_; break;
			case 170: field_175336_F.gravelCount = (int) p_175320_2_; break;
			case 171: field_175336_F.gravelMinHeight = (int) p_175320_2_; break;
			case 172: field_175336_F.gravelMaxHeight = (int) p_175320_2_; break;
			case 173: field_175336_F.graniteSize = (int) p_175320_2_; break;
			case 174: field_175336_F.graniteCount = (int) p_175320_2_; break;
			case 175: field_175336_F.graniteMinHeight = (int) p_175320_2_; break;
			case 176: field_175336_F.graniteMaxHeight = (int) p_175320_2_; break;
			case 177: field_175336_F.dioriteSize = (int) p_175320_2_; break;
			case 178: field_175336_F.dioriteCount = (int) p_175320_2_; break;
			case 179: field_175336_F.dioriteMinHeight = (int) p_175320_2_; break;
			case 180: field_175336_F.dioriteMaxHeight = (int) p_175320_2_; break;
			case 181: field_175336_F.andesiteSize = (int) p_175320_2_; break;
			case 182: field_175336_F.andesiteCount = (int) p_175320_2_; break;
			case 183: field_175336_F.andesiteMinHeight = (int) p_175320_2_; break;
			case 184: field_175336_F.andesiteMaxHeight = (int) p_175320_2_; break;
			case 185: field_175336_F.coalSize = (int) p_175320_2_; break;
			case 186: field_175336_F.coalCount = (int) p_175320_2_; break;
			case 187: field_175336_F.coalMinHeight = (int) p_175320_2_; break;
			case 189: field_175336_F.coalMaxHeight = (int) p_175320_2_; break;
			case 190: field_175336_F.ironSize = (int) p_175320_2_; break;
			case 191: field_175336_F.ironCount = (int) p_175320_2_; break;
			case 192: field_175336_F.ironMinHeight = (int) p_175320_2_; break;
			case 193: field_175336_F.ironMaxHeight = (int) p_175320_2_; break;
			case 194: field_175336_F.goldSize = (int) p_175320_2_; break;
			case 195: field_175336_F.goldCount = (int) p_175320_2_; break;
			case 196: field_175336_F.goldMinHeight = (int) p_175320_2_; break;
			case 197: field_175336_F.goldMaxHeight = (int) p_175320_2_; break;
			case 198: field_175336_F.redstoneSize = (int) p_175320_2_; break;
			case 199: field_175336_F.redstoneCount = (int) p_175320_2_; break;
			case 200: field_175336_F.redstoneMinHeight = (int) p_175320_2_; break;
			case 201: field_175336_F.redstoneMaxHeight = (int) p_175320_2_; break;
			case 202: field_175336_F.diamondSize = (int) p_175320_2_; break;
			case 203: field_175336_F.diamondCount = (int) p_175320_2_; break;
			case 204: field_175336_F.diamondMinHeight = (int) p_175320_2_; break;
			case 205: field_175336_F.diamondMaxHeight = (int) p_175320_2_; break;
			case 206: field_175336_F.lapisSize = (int) p_175320_2_; break;
			case 207: field_175336_F.lapisCount = (int) p_175320_2_; break;
			case 208: field_175336_F.lapisCenterHeight = (int) p_175320_2_; break;
			case 209: field_175336_F.lapisSpread = (int) p_175320_2_;
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 161:
            case 188:
            default: break;
		}
		if (p_175320_1_ >= 100 && p_175320_1_ < 116) {
			Gui gui = field_175349_r.getComponent(p_175320_1_ - 100 + 132);
            ((GuiTextField) gui).setText(func_175330_b(p_175320_1_, p_175320_2_));
        }
		if (!field_175336_F.equals(field_175334_E)) { field_175338_A = true; }
	}

	@Override
	public void setEntryValue(int p_175319_1_, @Nonnull String p_175319_2_) {
		float f = 0.0F;
		try { f = Float.parseFloat(p_175319_2_); } catch (NumberFormatException e) { LogWriter.error(e); }
		float f1 = 0.0F;
		switch (p_175319_1_) {
			case 132: f1 = field_175336_F.mainNoiseScaleX = MathHelper.clamp(f, 1.0F, 5000.0F); break;
			case 133: f1 = field_175336_F.mainNoiseScaleY = MathHelper.clamp(f, 1.0F, 5000.0F); break;
			case 134: f1 = field_175336_F.mainNoiseScaleZ = MathHelper.clamp(f, 1.0F, 5000.0F); break;
			case 135: f1 = field_175336_F.depthNoiseScaleX = MathHelper.clamp(f, 1.0F, 2000.0F); break;
			case 136: f1 = field_175336_F.depthNoiseScaleZ = MathHelper.clamp(f, 1.0F, 2000.0F); break;
			case 137: f1 = field_175336_F.depthNoiseScaleExponent = MathHelper.clamp(f, 0.01F, 20.0F); break;
			case 138: f1 = field_175336_F.baseSize = MathHelper.clamp(f, 1.0F, 25.0F); break;
			case 139: f1 = field_175336_F.coordinateScale = MathHelper.clamp(f, 1.0F, 6000.0F); break;
			case 140: f1 = field_175336_F.heightScale = MathHelper.clamp(f, 1.0F, 6000.0F); break;
			case 141: f1 = field_175336_F.stretchY = MathHelper.clamp(f, 0.01F, 50.0F); break;
			case 142: f1 = field_175336_F.upperLimitScale = MathHelper.clamp(f, 1.0F, 5000.0F); break;
			case 143: f1 = field_175336_F.lowerLimitScale = MathHelper.clamp(f, 1.0F, 5000.0F); break;
			case 144: f1 = field_175336_F.biomeDepthWeight = MathHelper.clamp(f, 1.0F, 20.0F); break;
			case 145: f1 = field_175336_F.biomeDepthOffset = MathHelper.clamp(f, 0.0F, 20.0F); break;
			case 146: f1 = field_175336_F.biomeScaleWeight = MathHelper.clamp(f, 1.0F, 20.0F); break;
			case 147: f1 = field_175336_F.biomeScaleOffset = MathHelper.clamp(f, 0.0F, 20.0F);
		}
		if (f1 != f && f != 0.0F) { ((GuiTextField) field_175349_r.getComponent(p_175319_1_)).setText(func_175330_b(p_175319_1_, f1)); }
		((GuiSlider) field_175349_r.getComponent(p_175319_1_ - 132 + 100)).setSliderValue(f1, false);
		if (!field_175336_F.equals(field_175334_E)) { field_175338_A = true; }
	}

}