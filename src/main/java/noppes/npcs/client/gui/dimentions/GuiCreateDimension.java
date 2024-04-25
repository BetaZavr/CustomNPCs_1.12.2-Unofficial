package noppes.npcs.client.gui.dimentions;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.entity.EntityNPCInterface;

@SideOnly(Side.CLIENT)
public class GuiCreateDimension extends GuiScreen {

	private static final String[] disallowedFilenames = new String[] { "CON", "COM", "PRN", "AUX", "CLOCK$", "NUL",
			"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4",
			"LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };
	public static String func_146317_a(ISaveFormat p_146317_0_, String p_146317_1_) {
		p_146317_1_ = p_146317_1_.replaceAll("[\\./\"]", "_");
		String[] astring = disallowedFilenames;
		int i = astring.length;
		for (int j = 0; j < i; ++j) {
			String s1 = astring[j];
			if (p_146317_1_.equalsIgnoreCase(s1)) {
				p_146317_1_ = "_" + p_146317_1_ + "_";
			}
		}
		while (p_146317_0_.getWorldInfo(p_146317_1_) != null) {
			p_146317_1_ = p_146317_1_ + "-";
		}
		return p_146317_1_;
	}
	private GuiTextField dimensionNameTextField;
	private GuiTextField seedTextField;
	private String field_146336_i;
	private String gameType = "survival";
	private String field_175300_s;
	private boolean generateStructures = true;
	private boolean allowCheats;
	private boolean bonusChest;
	private boolean hardcore;
	private boolean alreadyGenerated;
	private boolean userInMoreOptions;
	private GuiButton btnMoreOptions;
	private GuiButton btnStructures;
	private GuiButton btnDimensionType;
	private GuiButton btnCustomizeType;
	public String gameMode1;
	public String gameMode2;
	private String seedID;
	private String dimentionName;
	private int selectedIndex;
	public String chunkProviderSettingsJson = "";

	private int dimentionId = 0;

	public GuiCreateDimension(Gui p_i46320_1_) {
		this.seedID = "";
		this.dimentionName = "custom_dimention";
		this.dimentionId = 0;
	}

	public GuiCreateDimension(int dimentionId) {
		this.seedID = "";
		this.dimentionName = "custom_dimention";
		this.dimentionId = dimentionId;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		if (button.id == 1) {
			CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions);
		} else if (button.id == 0) {
			CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions);
			if (this.alreadyGenerated) {
				return;
			}
			this.alreadyGenerated = true;
			long i = (new Random()).nextLong();
			String s = this.seedTextField.getText();
			if (!StringUtils.isEmpty(s)) {
				try {
					long j = Long.parseLong(s);
					if (j != 0L) {
						i = j;
					}
				} catch (NumberFormatException numberformatexception) {
					i = s.hashCode();
				}
			}
			WorldType.WORLD_TYPES[this.selectedIndex].onGUICreateWorldPress();
			GameType gametype = GameType.getByName(this.gameType);
			WorldSettings worldsettings = new WorldSettings(i, gametype, this.generateStructures, this.hardcore,
					WorldType.WORLD_TYPES[this.selectedIndex]);
			worldsettings.setGeneratorOptions(this.chunkProviderSettingsJson);
			if (this.bonusChest && !this.hardcore) {
				worldsettings.enableBonusChest();
			}
			if (this.allowCheats && !this.hardcore) {
				worldsettings.enableCommands();
			}
			WorldInfo worldInfo = new CustomWorldInfo(worldsettings, this.dimensionNameTextField.getText().trim());
			Client.sendData(EnumPacketServer.DimensionSettings, this.dimentionId, worldInfo);
		} else if (button.id == 3) {
			this.func_146315_i();
		} else if (button.id == 4) {
			this.generateStructures = !this.generateStructures;
			this.updateDisplayState();
		} else if (button.id == 5) {
			++this.selectedIndex;
			if (this.selectedIndex >= WorldType.WORLD_TYPES.length) {
				this.selectedIndex = 0;
			}
			while (!this.func_175299_g()) {
				++this.selectedIndex;
				if (this.selectedIndex >= WorldType.WORLD_TYPES.length) {
					this.selectedIndex = 0;
				}
			}
			this.chunkProviderSettingsJson = "";
			this.updateDisplayState();
			this.showMoreWorldOptions(this.userInMoreOptions);
		} else if (button.id == 8) {
			if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.FLAT) {
				this.mc.displayGuiScreen(new GuiCreateFlatDimension(this, this.chunkProviderSettingsJson));
			} else if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.CUSTOMIZED) {
				this.mc.displayGuiScreen(new GuiCustomizeDimension(this, this.chunkProviderSettingsJson));
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, new TextComponentTranslation("dimensions.create").getFormattedText(),
				this.width / 2, 20, -1);
		if (this.userInMoreOptions) {
			this.drawString(this.fontRenderer, new TextComponentTranslation("selectWorld.enterSeed").getFormattedText(),
					this.width / 2 - 100, 47, -6250336);
			this.drawString(this.fontRenderer, new TextComponentTranslation("selectWorld.seedInfo").getFormattedText(),
					this.width / 2 - 100, 85, -6250336);
			if (this.btnStructures.visible) {
				this.drawString(this.fontRenderer,
						new TextComponentTranslation("selectWorld.mapFeatures.info").getFormattedText(),
						this.width / 2 - 150, 122, -6250336);
			}
			this.seedTextField.drawTextBox();
			if (WorldType.WORLD_TYPES[this.selectedIndex].hasInfoNotice()) {
				this.fontRenderer.drawSplitString(
						new TextComponentTranslation(WorldType.WORLD_TYPES[this.selectedIndex].getInfoTranslationKey())
								.getFormattedText(),
						this.btnDimensionType.x + 2, this.btnDimensionType.y + 22,
						this.btnDimensionType.getButtonWidth(), 10526880);
			}
		} else {
			this.drawString(this.fontRenderer, new TextComponentTranslation("dimensions.enter.name").getFormattedText(),
					this.width / 2 - 100, 47, -6250336);
			this.drawString(this.fontRenderer,
					new TextComponentTranslation("selectWorld.resultFolder").getFormattedText() + " "
							+ this.field_146336_i,
					this.width / 2 - 100, 85, -6250336);
			this.dimensionNameTextField.drawTextBox();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void func_146314_g() {
		this.field_146336_i = this.dimensionNameTextField.getText().toLowerCase().trim();
		while (this.field_146336_i.indexOf(" ") != -1) {
			this.field_146336_i = this.field_146336_i.replace(" ", "_");
		}
		char[] achar = ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS;
		int i = achar.length;
		for (int j = 0; j < i; ++j) {
			char c0 = achar[j];
			this.field_146336_i = this.field_146336_i.replace(c0, '_');
		}
		if (StringUtils.isEmpty(this.field_146336_i)) {
			this.field_146336_i = "World";
		}
		this.field_146336_i = func_146317_a(this.mc.getSaveLoader(), this.field_146336_i);
	}

	private void func_146315_i() {
		this.showMoreWorldOptions(!this.userInMoreOptions);
	}

	public void func_146318_a(WorldInfo p_146318_1_) {
		this.dimentionName = new TextComponentTranslation("selectWorld.newWorld.copyOf",
				new Object[] { p_146318_1_.getWorldName() }).getFormattedText();
		this.seedID = p_146318_1_.getSeed() + "";
		this.selectedIndex = p_146318_1_.getTerrainType().getId();
		this.chunkProviderSettingsJson = p_146318_1_.getGeneratorOptions();
		this.generateStructures = p_146318_1_.isMapFeaturesEnabled();
		this.allowCheats = p_146318_1_.areCommandsAllowed();

		if (p_146318_1_.isHardcoreModeEnabled()) {
			this.gameType = "hardcore";
		} else if (p_146318_1_.getGameType().isSurvivalOrAdventure()) {
			this.gameType = "survival";
		} else if (p_146318_1_.getGameType().isCreative()) {
			this.gameType = "creative";
		}
	}

	private boolean func_175299_g() {
		WorldType worldtype = WorldType.WORLD_TYPES[this.selectedIndex];
		return worldtype != null && worldtype.canBeCreated()
				? (worldtype == WorldType.DEBUG_ALL_BLOCK_STATES ? isShiftKeyDown() : true)
				: false;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20,
				new TextComponentTranslation("dimensions.create").getFormattedText()));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20,
				new TextComponentTranslation("gui.cancel").getFormattedText()));
		this.buttonList.add(this.btnMoreOptions = new GuiButton(3, this.width / 2 - 75, 187, 150, 20,
				new TextComponentTranslation("dimensions.more.dimension.options").getFormattedText()));
		this.buttonList.add(this.btnStructures = new GuiButton(4, this.width / 2 - 155, 100, 150, 20,
				new TextComponentTranslation("selectWorld.mapFeatures").getFormattedText()));
		this.btnStructures.visible = false;
		this.buttonList.add(this.btnDimensionType = new GuiButton(5, this.width / 2 + 5, 100, 150, 20,
				new TextComponentTranslation("selectWorld.mapType").getFormattedText()));
		this.btnDimensionType.visible = false;
		this.buttonList.add(this.btnCustomizeType = new GuiButton(8, this.width / 2 + 5, 120, 150, 20,
				new TextComponentTranslation("selectWorld.customizeType").getFormattedText()));
		this.btnCustomizeType.visible = false;
		this.dimensionNameTextField = new GuiTextField(9, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
		this.dimensionNameTextField.setFocused(true);
		this.dimensionNameTextField.setText(this.dimentionName);
		this.seedTextField = new GuiTextField(10, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
		this.seedTextField.setText(this.seedID);
		this.showMoreWorldOptions(this.userInMoreOptions);
		this.func_146314_g();
		this.updateDisplayState();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (this.dimensionNameTextField.isFocused() && !this.userInMoreOptions) {
			this.dimensionNameTextField.textboxKeyTyped(typedChar, keyCode);
			this.dimentionName = this.dimensionNameTextField.getText().toLowerCase();
			while (this.dimentionName.indexOf(" ") != -1) {
				this.dimentionName = this.dimentionName.replace(" ", "_");
			}
		} else if (this.seedTextField.isFocused() && this.userInMoreOptions) {
			this.seedTextField.textboxKeyTyped(typedChar, keyCode);
			this.seedID = this.seedTextField.getText();
		}
		if (keyCode == 28 || keyCode == 156) {
			this.actionPerformed((GuiButton) this.buttonList.get(0));
		}
		((GuiButton) this.buttonList.get(0)).enabled = this.dimensionNameTextField.getText().length() > 0;
		this.func_146314_g();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (this.userInMoreOptions) {
			this.seedTextField.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			this.dimensionNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	private void showMoreWorldOptions(boolean toggle) {
		this.userInMoreOptions = toggle;
		if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.DEBUG_ALL_BLOCK_STATES) {
			if (this.field_175300_s == null) {
				this.field_175300_s = this.gameType;
			}
			this.gameType = "spectator";
			this.btnStructures.visible = false;
			this.btnDimensionType.visible = this.userInMoreOptions;
			this.btnCustomizeType.visible = false;
		} else {
			if (this.field_175300_s != null) {
				this.gameType = this.field_175300_s;
				this.field_175300_s = null;
			}
			this.btnStructures.visible = this.userInMoreOptions
					&& WorldType.WORLD_TYPES[this.selectedIndex] != WorldType.CUSTOMIZED;
			this.btnDimensionType.visible = this.userInMoreOptions;
			this.btnCustomizeType.visible = this.userInMoreOptions
					&& WorldType.WORLD_TYPES[this.selectedIndex].isCustomizable();
		}
		this.updateDisplayState();
		if (this.userInMoreOptions) {
			this.btnMoreOptions.displayString = new TextComponentTranslation("gui.done").getFormattedText();
		} else {
			this.btnMoreOptions.displayString = new TextComponentTranslation("dimensions.more.dimension.options")
					.getFormattedText();
		}
	}

	private void updateDisplayState() {
		this.gameMode1 = new TextComponentTranslation("selectWorld.gameMode." + this.gameType + ".line1")
				.getFormattedText();
		this.gameMode2 = new TextComponentTranslation("selectWorld.gameMode." + this.gameType + ".line2")
				.getFormattedText();
		this.btnStructures.displayString = new TextComponentTranslation("selectWorld.mapFeatures").getFormattedText()
				+ " ";
		if (this.generateStructures) {
			this.btnStructures.displayString = this.btnStructures.displayString
					+ new TextComponentTranslation("options.on").getFormattedText();
		} else {
			this.btnStructures.displayString = this.btnStructures.displayString
					+ new TextComponentTranslation("options.off").getFormattedText();
		}
		this.btnDimensionType.displayString = new TextComponentTranslation("selectWorld.mapType").getFormattedText()
				+ " " + new TextComponentTranslation(WorldType.WORLD_TYPES[this.selectedIndex].getTranslationKey())
						.getFormattedText();
	}

	@Override
	public void updateScreen() {
		this.dimensionNameTextField.updateCursorCounter();
		this.seedTextField.updateCursorCounter();
	}

}