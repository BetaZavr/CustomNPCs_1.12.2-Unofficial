package noppes.npcs.client.gui.dimentions;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

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

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiCreateDimension extends GuiScreen {

	private static final String[] disallowedFilenames = new String[] { "CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };
	public static String func_146317_a(ISaveFormat p_146317_0_, String p_146317_1_) {
		p_146317_1_ = p_146317_1_.replaceAll("[\\\\./\"]", "_");
        StringBuilder p_146317_1_Builder = new StringBuilder(p_146317_1_);
        for (String s1 : disallowedFilenames) {
            if (p_146317_1_Builder.toString().equalsIgnoreCase(s1)) {
                p_146317_1_Builder = new StringBuilder("_" + p_146317_1_Builder + "_");
            }
        }
        p_146317_1_ = p_146317_1_Builder.toString();
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
    private boolean alreadyGenerated;
	private boolean userInMoreOptions;
	private GuiButton btnMoreOptions;
	private GuiButton btnStructures;
	private GuiButton btnDimensionType;
	private GuiButton btnCustomizeType;
	public String gameMode1;
	public String gameMode2;
	private String seedID;
	private String dimensionName;
	private int selectedIndex;
	public String chunkProviderSettingsJson = "";

	private final int dimensionId;

	public GuiCreateDimension(int dimensionIdIn) {
		seedID = "";
		dimensionName = "custom_dimension";
		dimensionId = dimensionIdIn;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		if (!button.enabled) { return; }
		if (button.id == 1) { CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions); }
		else if (button.id == 0) {
			CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions);
			if (alreadyGenerated) { return; }
			alreadyGenerated = true;
			long i = (new Random()).nextLong();
			String s = seedTextField.getText();
			if (!StringUtils.isEmpty(s)) {
				try {
					long j = Long.parseLong(s);
					if (j != 0L) { i = j; }
				}
				catch (NumberFormatException numberformatexception) { i = s.hashCode(); }
			}
			WorldType.WORLD_TYPES[selectedIndex].onGUICreateWorldPress();
			final WorldInfo worldInfo = getWorldInfo(i);
			Client.sendData(EnumPacketServer.DimensionSettings, dimensionId, worldInfo);
		}
		else if (button.id == 3) { func_146315_i(); }
		else if (button.id == 4) {
			generateStructures = !generateStructures;
			updateDisplayState();
		}
		else if (button.id == 5) {
            do {
                ++selectedIndex;
                if (selectedIndex >= WorldType.WORLD_TYPES.length) { selectedIndex = 0; }
            } while (!func_175299_g());
			chunkProviderSettingsJson = "";
			updateDisplayState();
			showMoreWorldOptions(userInMoreOptions);
		} else if (button.id == 8) {
			if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.FLAT) { mc.displayGuiScreen(new GuiCreateFlatDimension(this, chunkProviderSettingsJson)); }
			else if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.CUSTOMIZED) { mc.displayGuiScreen(new GuiCustomizeDimension(this, chunkProviderSettingsJson)); }
		}
	}

	private WorldInfo getWorldInfo(long i) {
		GameType gametype = GameType.getByName(gameType);
		boolean hardcore = false;
		WorldSettings worldsettings = new WorldSettings(i, gametype, generateStructures, hardcore, WorldType.WORLD_TYPES[selectedIndex]);
		worldsettings.setGeneratorOptions(chunkProviderSettingsJson);
        if (allowCheats && !hardcore) { worldsettings.enableCommands(); }
        return new CustomWorldInfo(worldsettings, dimensionNameTextField.getText().trim());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, new TextComponentTranslation("dimensions.create").getFormattedText(), width / 2, 20, -1);
		if (userInMoreOptions) {
			drawString(fontRenderer, new TextComponentTranslation("selectWorld.enterSeed").getFormattedText(), width / 2 - 100, 47, 0xFFA0A0A0);
			drawString(fontRenderer, new TextComponentTranslation("selectWorld.seedInfo").getFormattedText(), width / 2 - 100, 85, 0xFFA0A0A0);
			if (btnStructures.visible) {
				drawString(fontRenderer,
						new TextComponentTranslation("selectWorld.mapFeatures.info").getFormattedText(),
						width / 2 - 150, 122, 0xFFA0A0A0);
			}
			seedTextField.drawTextBox();
			if (WorldType.WORLD_TYPES[selectedIndex].hasInfoNotice()) {
				fontRenderer.drawSplitString(
						new TextComponentTranslation(WorldType.WORLD_TYPES[selectedIndex].getInfoTranslationKey()).getFormattedText(),
						btnDimensionType.x + 2, btnDimensionType.y + 22,
						btnDimensionType.getButtonWidth(), 10526880);
			}
		} else {
			drawString(fontRenderer, new TextComponentTranslation("dimensions.enter.name").getFormattedText(), width / 2 - 100, 47, 0xFFA0A0A0);
			drawString(fontRenderer,
					new TextComponentTranslation("selectWorld.resultFolder").getFormattedText() + " " + field_146336_i,
					width / 2 - 100, 85, 0xFFA0A0A0);
			dimensionNameTextField.drawTextBox();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void func_146314_g() {
		field_146336_i = dimensionNameTextField.getText().toLowerCase().trim();
		while (field_146336_i.contains(" ")) { field_146336_i = field_146336_i.replace(" ", "_"); }
		char[] aChar = ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS;
        for (char c0 : aChar) { field_146336_i = field_146336_i.replace(c0, '_'); }
		if (StringUtils.isEmpty(field_146336_i)) { field_146336_i = "World"; }
		field_146336_i = func_146317_a(mc.getSaveLoader(), field_146336_i);
	}

	private void func_146315_i() { showMoreWorldOptions(!userInMoreOptions); }

	public void func_146318_a(WorldInfo p_146318_1_) {
		dimensionName = new TextComponentTranslation("selectWorld.newWorld.copyOf", p_146318_1_.getWorldName()).getFormattedText();
		seedID = p_146318_1_.getSeed() + "";
		selectedIndex = p_146318_1_.getTerrainType().getId();
		chunkProviderSettingsJson = p_146318_1_.getGeneratorOptions();
		generateStructures = p_146318_1_.isMapFeaturesEnabled();
		allowCheats = p_146318_1_.areCommandsAllowed();
		if (p_146318_1_.isHardcoreModeEnabled()) { gameType = "hardcore"; }
		else if (p_146318_1_.getGameType().isSurvivalOrAdventure()) { gameType = "survival"; }
		else if (p_146318_1_.getGameType().isCreative()) { gameType = "creative"; }
	}

	private boolean func_175299_g() {
		WorldType worldtype = WorldType.WORLD_TYPES[selectedIndex];
		return worldtype != null && worldtype.canBeCreated() && (worldtype != WorldType.DEBUG_ALL_BLOCK_STATES || isShiftKeyDown());
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, new TextComponentTranslation("dimensions.create").getFormattedText()));
		buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, new TextComponentTranslation("gui.cancel").getFormattedText()));
		buttonList.add(btnMoreOptions = new GuiButton(3, width / 2 - 75, 187, 150, 20, new TextComponentTranslation("dimensions.more.dimension.options").getFormattedText()));
		buttonList.add(btnStructures = new GuiButton(4, width / 2 - 155, 100, 150, 20, new TextComponentTranslation("selectWorld.mapFeatures").getFormattedText()));
		btnStructures.visible = false;
		buttonList.add(btnDimensionType = new GuiButton(5, width / 2 + 5, 100, 150, 20, new TextComponentTranslation("selectWorld.mapType").getFormattedText()));
		btnDimensionType.visible = false;
		buttonList.add(btnCustomizeType = new GuiButton(8, width / 2 + 5, 120, 150, 20, new TextComponentTranslation("selectWorld.customizeType").getFormattedText()));
		btnCustomizeType.visible = false;
		dimensionNameTextField = new GuiTextField(9, fontRenderer, width / 2 - 100, 60, 200, 20);
		dimensionNameTextField.setFocused(true);
		dimensionNameTextField.setText(dimensionName);
		seedTextField = new GuiTextField(10, fontRenderer, width / 2 - 100, 60, 200, 20);
		seedTextField.setText(seedID);
		showMoreWorldOptions(userInMoreOptions);
		func_146314_g();
		updateDisplayState();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (dimensionNameTextField.isFocused() && !userInMoreOptions) {
			dimensionNameTextField.textboxKeyTyped(typedChar, keyCode);
			dimensionName = dimensionNameTextField.getText().toLowerCase();
			while (dimensionName.contains(" ")) { dimensionName = dimensionName.replace(" ", "_"); }
		} else if (seedTextField.isFocused() && userInMoreOptions) {
			seedTextField.textboxKeyTyped(typedChar, keyCode);
			seedID = seedTextField.getText();
		}
		if (keyCode == 28 || keyCode == 156) { actionPerformed(buttonList.get(0)); }
		buttonList.get(0).enabled = !dimensionNameTextField.getText().isEmpty();
		func_146314_g();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (userInMoreOptions) { seedTextField.mouseClicked(mouseX, mouseY, mouseButton); }
		else { dimensionNameTextField.mouseClicked(mouseX, mouseY, mouseButton); }
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	private void showMoreWorldOptions(boolean toggle) {
		userInMoreOptions = toggle;
		if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.DEBUG_ALL_BLOCK_STATES) {
			if (field_175300_s == null) { field_175300_s = gameType; }
			gameType = "spectator";
			btnStructures.visible = false;
			btnDimensionType.visible = userInMoreOptions;
			btnCustomizeType.visible = false;
		} else {
			if (field_175300_s != null) {
				gameType = field_175300_s;
				field_175300_s = null;
			}
			btnStructures.visible = userInMoreOptions && WorldType.WORLD_TYPES[selectedIndex] != WorldType.CUSTOMIZED;
			btnDimensionType.visible = userInMoreOptions;
			btnCustomizeType.visible = userInMoreOptions && WorldType.WORLD_TYPES[selectedIndex].isCustomizable();
		}
		updateDisplayState();
		if (userInMoreOptions) { btnMoreOptions.displayString = new TextComponentTranslation("gui.done").getFormattedText(); }
		else { btnMoreOptions.displayString = new TextComponentTranslation("dimensions.more.dimension.options").getFormattedText(); }
	}

	private void updateDisplayState() {
		gameMode1 = new TextComponentTranslation("selectWorld.gameMode." + gameType + ".line1").getFormattedText();
		gameMode2 = new TextComponentTranslation("selectWorld.gameMode." + gameType + ".line2").getFormattedText();
		btnStructures.displayString = new TextComponentTranslation("selectWorld.mapFeatures").getFormattedText() + " ";
		if (generateStructures) {
			btnStructures.displayString = btnStructures.displayString + new TextComponentTranslation("options.on").getFormattedText();
		} else {
			btnStructures.displayString = btnStructures.displayString + new TextComponentTranslation("options.off").getFormattedText();
		}
		btnDimensionType.displayString = new TextComponentTranslation("selectWorld.mapType").getFormattedText() +
				" " + new TextComponentTranslation(WorldType.WORLD_TYPES[selectedIndex].getTranslationKey()).getFormattedText();
	}

	@Override
	public void updateScreen() {
		dimensionNameTextField.updateCursorCounter();
		seedTextField.updateCursorCounter();
	}

}