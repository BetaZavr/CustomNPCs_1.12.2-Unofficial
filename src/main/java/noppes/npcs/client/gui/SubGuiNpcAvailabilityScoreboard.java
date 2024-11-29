package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityScoreboard;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityScoreboardData;

public class SubGuiNpcAvailabilityScoreboard extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

	private final Availability availability;
	private final String chr = "" + ((char) 167);
	private final Map<String, String> dataNames = new HashMap<>();
	private final Map<String, AvailabilityScoreboardData> dataSets = new HashMap<>();
	// New
	private GuiCustomScroll scroll;
	private String select;

	public SubGuiNpcAvailabilityScoreboard(Availability availability) {
		this.availability = availability;
		this.setBackground("menubg.png");
		this.xSize = 316;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.select = "";
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			if (this.select.isEmpty()) {
				return;
			}
			String obj = this.dataNames.get(this.select);
			AvailabilityScoreboardData asd = this.availability.scoreboards.get(obj);
			asd.scoreboardType = EnumAvailabilityScoreboard.values()[button.getValue()];
			this.availability.scoreboards.put(obj, asd);
			this.select = obj + " - " + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase())
							.getFormattedText()
					+ chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
			this.initGui();
		} else if (button.id == 2) {
			this.availability.scoreboards.remove(this.dataNames.get(this.select));
			this.select = "";
			this.initGui();
		} else if (button.id == 3) { // More
			this.save();
			this.initGui();
		} else if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("availability.hover.enum.type").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 59, this.guiTop + this.ySize - 46, 189, 20)) {
			this.setHoverText(new TextComponentTranslation("availability.hover.scoreboard.name").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 252, this.guiTop + this.ySize - 46, 36, 20)) {
			this.setHoverText(new TextComponentTranslation("availability.hover.scoreboard.value").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20)) {
			this.setHoverText(new TextComponentTranslation("availability.hover.remove").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("availability.hover.more").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, this.guiTop + 192, 70, 20, "gui.done"));
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(this.xSize - 12, this.ySize - 66);
		}
		this.dataNames.clear();
		this.dataSets.clear();
		for (String obj : this.availability.scoreboards.keySet()) {
			AvailabilityScoreboardData asd = this.availability.scoreboards.get(obj);
			String key = obj + " - " + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase())
							.getFormattedText()
					+ chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
			this.dataNames.put(key, obj);
			this.dataSets.put(key, asd);
		}
		if (!this.select.isEmpty() && !this.dataNames.containsKey(this.select)) {
			this.select = "";
		}
		this.scroll.setList(new ArrayList<>(dataNames.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!this.select.isEmpty()) {
			this.scroll.setSelected(this.select);
		}
		this.addScroll(this.scroll);
		int p = 0;
		if (!this.select.isEmpty()) {
			p = this.dataSets.get(this.select).scoreboardType.ordinal();
		}

		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20,
				new String[] { "availability.smaller", "availability.equals", "availability.bigger" }, p));
		this.getButton(0).setEnabled(!this.select.isEmpty());
		this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 59, this.guiTop + this.ySize - 46, 189, 20,
				!this.select.isEmpty() ? this.dataNames.get(this.select) : ""));
		this.addTextField(new GuiNpcTextField(1, this, this.guiLeft + 252, this.guiTop + this.ySize - 46, 36, 20,
				!this.select.isEmpty() ? "" + this.dataSets.get(this.select).scoreboardValue : ""));
		this.getTextField(1).setNumbersOnly();
		this.addButton(new GuiNpcButton(2, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20, "X"));
		this.getButton(2).setEnabled(!this.select.isEmpty());

		this.addButton(
				new GuiNpcButton(3, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20, "availability.more"));
		this.getButton(3).setEnabled(!this.select.isEmpty());
	}

	@Override
	public void keyTyped(char c, int key) {
		if (key == 28 && this.getTextField(0).isFocused()) { // Enter
			this.getTextField(0).unFocused();
		}
		super.keyTyped(c, key);
	}

	@Override
	public void save() {
		if (this.select.isEmpty()) {
			return;
		}
		EnumAvailabilityScoreboard eas = EnumAvailabilityScoreboard.values()[this.getButton(0).getValue()];
		int value = NoppesStringUtils.parseInt(this.getTextField(1).getText(), 0);
		String obj = this.dataNames.get(this.select);
		this.availability.scoreboards.put(obj, new AvailabilityScoreboardData(eas, value));
		this.select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.isEmpty()) {
			return;
		}
		String obj = "";
		AvailabilityScoreboardData asd = null;
		int value = NoppesStringUtils.parseInt(this.getTextField(1).getText(), 0);
		if (!this.select.isEmpty()) {
			obj = this.dataNames.get(this.select);
			asd = this.availability.scoreboards.get(obj);
		}
		if (textfield.getId() == 0) {
			if (obj == null || obj.isEmpty() || asd == null) {
				obj = textfield.getText();
				asd = new AvailabilityScoreboardData(EnumAvailabilityScoreboard.SMALLER, value);
			} else {
				if (obj.equals(textfield.getText())) {
					return;
				}
				obj = textfield.getText();
				this.availability.scoreboards.remove(this.dataNames.get(this.select));
			}
		} else if (textfield.getId() == 1) {
			if (asd == null || asd.scoreboardValue == value) {
				return;
			}
			asd.scoreboardValue = value;
		}
		if (asd != null) {
			this.availability.scoreboards.put(obj, asd);
			this.select = obj + " - " + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase()).getFormattedText()
					+ chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
		}
		this.initGui();
	}

}
