package noppes.npcs.client.gui.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ArrayEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ButtonEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.StringEntry;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.IConfigElement;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.util.Util;

public class CustomNpcsConfigGui extends GuiConfig implements ISubGuiListener {

	public static class ColorEntry extends ButtonEntry {

		protected final String beforeValue;
		public String currentValue;

		public ColorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement);
			String value = configElement.get().toString();
			if (value.length() > 6) {
				value = value.substring(0, 6);
			}
			beforeValue = value;
			currentValue = value;
			btnValue.enabled = enabled();
			btnValue.width = 120;
			updateValueButtonText();
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
				boolean isSelected, float partial) {
			btnValue.visible = false;
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);

			btnValue.width = (int) ((float) (owningEntryList.controlWidth - 2) * 0.75f);
			btnValue.x = owningScreen.entryList.controlX;
			btnValue.y = y;
			btnValue.enabled = enabled();
			btnValue.visible = true;
			btnValue.drawButton(this.mc, mouseX, mouseY, partial);

			int left = btnValue.x + btnValue.width + 2;
			int right = left + (owningEntryList.controlWidth - btnValue.width) - 2;
			GuiUtils.drawGradientRect(0, left, y, right, y + 18, 0xFF000000, 0xFF000000);
			int color = (int) Long.parseLong(currentValue, 16);
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			if (alpha == 0.0f) {
				color += 0xFF000000;
			}
			GuiUtils.drawGradientRect(0, left + 1, y + 1, right - 1, y + 17, color, color);
		}

		public GuiButtonExt getButton() {
			return btnValue;
		}

		@Override
		public Object getCurrentValue() {
			return this.currentValue;
		}

		@Override
		public Object[] getCurrentValues() {
			return new Object[] { getCurrentValue() };
		}

		@Override
		public boolean isChanged() {
			return beforeValue != null ? !beforeValue.equals(currentValue) : currentValue.trim().isEmpty();
		}

		@Override
		public boolean isDefault() {
			return configElement.getDefault() != null ? configElement.getDefault().toString().equals(currentValue)
					: currentValue.trim().isEmpty();
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled()) {
				if (isChanged() && this.isValidValue) {
					this.configElement.set(this.currentValue);
					return configElement.requiresMcRestart();
				} else if (isChanged() && !this.isValidValue) {
					this.configElement.setToDefault();
					return configElement.requiresMcRestart() && beforeValue != null
							? beforeValue.equals(configElement.getDefault())
							: configElement.getDefault() == null;
				}
			}
			return false;
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				currentValue = configElement.getDefault().toString();
				keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_HOME);
				btnValue.displayString = currentValue;
			}
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				currentValue = beforeValue;
				btnValue.displayString = currentValue;
			}
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(String.valueOf(currentValue));
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			if (enabled()) {
				CustomNpcsConfigGui.subGui = new SubGuiColorSelector((int) Long.parseLong(currentValue, 16));
				CustomNpcsConfigGui.subGui.width = owningEntryList.width;
				CustomNpcsConfigGui.subGui.height = owningEntryList.height;
				CustomNpcsConfigGui.subGui.initGui();
				CustomNpcsConfigGui.subGui.parent = owningScreen;
				CustomNpcsConfigGui.subGui.object = this;
				CustomNpcsConfigGui.subGui.background = null;
			}
		}

	}

	public static class CustomArrayEntry extends ArrayEntry {

		public CustomArrayEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
				IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement);
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			mc.displayGuiScreen(new CustomNpcsConfigGui.CustomGuiEditArray(owningScreen, configElement, slotIndex,
					currentValues, enabled()));
		}
	}

	public static class CustomGuiEditArray extends GuiEditArray implements ISubGuiListener {

		public static SubGuiInterface subGui;

		public CustomGuiEditArray(GuiScreen parentScreen, IConfigElement configElement, int slotIndex,
				Object[] currentValues, boolean enabled) {
			super(parentScreen, configElement, slotIndex, currentValues, enabled);
		}

		@Override
		protected GuiEditArrayEntries createEditArrayEntries() {
			return new CustomGuiEditArrayEntries(this, this.mc, this.configElement, this.beforeValues,
					this.currentValues);
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			if (subGui != null) {
				this.drawDefaultBackground();
				subGui.drawScreen(mouseX, mouseY, partialTicks);
				this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 8, 16777215);
				if (this.titleLine2 != null) {
					this.drawCenteredString(this.fontRenderer, this.titleLine2, this.width / 2, 18, 16777215);
				}
				if (this.titleLine3 != null) {
					this.drawCenteredString(this.fontRenderer, this.titleLine3, this.width / 2, 28, 16777215);
				}
				return;
			}
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public void initGui() {
			super.initGui();
			if (subGui != null) {
				subGui.width = this.width;
				subGui.height = this.height;
				subGui.initGui();
			}
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (subGui != null) {
				subGui.keyTyped(eventChar, eventKey);
				return;
			}
			super.keyTyped(eventChar, eventKey);

		}

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) throws IOException {
			if (subGui != null) {
				subGui.mouseClicked(x, y, mouseEvent);
				return;
			}
			super.mouseClicked(x, y, mouseEvent);
		}

		@Override
		public void mouseReleased(int x, int y, int mouseEvent) {
			if (subGui != null) {
				subGui.mouseReleased(x, y, mouseEvent);
				return;
			}
			super.mouseReleased(x, y, mouseEvent);
		}

		@Override
		public void subGuiClosed(SubGuiInterface subgui) {
			String color = ((SubGuiColorSelector) subgui).getColor().toUpperCase();
			if (subgui.object instanceof CustomGuiEditArrayEntries.ColorEntry) {
				ConfigElement element = (ConfigElement) ((CustomGuiEditArrayEntries.ColorEntry) subgui.object).configElement;
				((CustomGuiEditArrayEntries.ColorEntry) subgui.object).getButton().displayString = color;
				((CustomGuiEditArrayEntries.ColorEntry) subgui.object).value = color;
				element.set(color);
			}
			subGui = null;
		}

	}

	public static SubGuiInterface subGui;

	public CustomNpcsConfigGui(GuiScreen parentScreen) {
		super(parentScreen, CustomNpcs.Config.getChildElements(), CustomNpcs.MODNAME, false, false, CustomNpcs.MODNAME);
		subGui = null;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subGui != null) {
			drawDefaultBackground();
			subGui.drawScreen(mouseX, mouseY, partialTicks);
			drawCenteredString(this.fontRenderer, this.title, this.width / 2, 8, 0xFFFFFFFF);
			if (subGui.object instanceof ColorEntry) {
				drawCenteredString(this.fontRenderer, ((ColorEntry) subGui.object).getName(), this.width / 2, 18,
						0xFFA0A0A0);
			}
			String title2 = this.titleLine2;
			if (title2 != null) {
				int strWidth = mc.fontRenderer.getStringWidth(title2);
				int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
				if (strWidth > width - 6 && strWidth > ellipsisWidth) {
					title2 = mc.fontRenderer.trimStringToWidth(title2, width - 6 - ellipsisWidth).trim() + "...";
				}
				this.drawCenteredString(this.fontRenderer, title2, this.width / 2, 18, 16777215);
			}

			this.btnUndoAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked())
					&& this.entryList.hasChangedEntry(this.chkApplyGlobally.isChecked());
			this.btnDefaultAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked())
					&& !this.entryList.areAllEntriesDefault(this.chkApplyGlobally.isChecked());

			if (undoHoverChecker.checkHover(mouseX, mouseY)) {
				drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.undoAll").split("\n")), mouseX, mouseY);
			}
			if (resetHoverChecker.checkHover(mouseX, mouseY)) {
				drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.resetAll").split("\n")), mouseX, mouseY);
			}
			if (checkBoxHoverChecker.checkHover(mouseX, mouseY)) {
				drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.applyGlobally").split("\n")), mouseX,
						mouseY);
			}
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawToolTip(List<String> stringList, int x, int y) {
		List<String> newToolTip = Lists.newArrayList();
		String name = "";
		for (String str : stringList) {
			if (name != null) {
				if (name.isEmpty()) {
					name = str.toLowerCase();
				} else {
					newToolTip.add(((char) 167) + "e"
							+ new TextComponentTranslation(
									"property." + Util.instance.deleteColor(name) + ".hover")
											.getFormattedText());
					name = null;
					continue;
				}
			}
			newToolTip.add(str);
		}
		GuiUtils.drawHoveringText(newToolTip, x, y, width, height, 300, fontRenderer);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (subGui != null) {
			subGui.width = this.width;
			subGui.height = this.height;
			subGui.initGui();
		}
		int i = -1;
		List<IConfigEntry> list = Lists.newArrayList(entryList.listEntries);
		for (IConfigEntry entry : list) {
			i++;
			if (entry.getConfigElement().getType() != ConfigGuiType.COLOR) {
				continue;
			}
			if (entry.getClass() == StringEntry.class) {
				entryList.listEntries.remove(entry);
				entryList.listEntries.add(i,
						new CustomNpcsConfigGui.ColorEntry(this, entryList, entry.getConfigElement()));
			}
			if (entry.getClass() == ArrayEntry.class) {
				entryList.listEntries.remove(entry);
				entryList.listEntries.add(i,
						new CustomNpcsConfigGui.CustomArrayEntry(this, entryList, entry.getConfigElement()));
			}
		}
	}

	@Override
	public void keyTyped(char eventChar, int eventKey) {
		if (subGui != null) {
			subGui.keyTyped(eventChar, eventKey);
			return;
		}
		super.keyTyped(eventChar, eventKey);

	}

	@Override
	public void mouseClicked(int x, int y, int mouseEvent) throws IOException {
		if (subGui != null) {
			subGui.mouseClicked(x, y, mouseEvent);
			return;
		}
		super.mouseClicked(x, y, mouseEvent);
	}

	@Override
	public void mouseReleased(int x, int y, int mouseEvent) {
		if (subGui != null) {
			subGui.mouseReleased(x, y, mouseEvent);
			return;
		}
		super.mouseReleased(x, y, mouseEvent);
	}

	@Override
	public void onGuiClosed() {
		CustomNpcs.Config.resetData();
		CustomNpcs.Config.config.save();
		super.onGuiClosed();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		String color = ((SubGuiColorSelector) subgui).getColor().toUpperCase();
		if (subgui.object instanceof ColorEntry) {
			ConfigElement element = (ConfigElement) ((ColorEntry) subgui.object).getConfigElement();
			((ColorEntry) subgui.object).getButton().displayString = color;
			((ColorEntry) subgui.object).currentValue = color;
			element.set(color);
		}
		subGui = null;
	}

}
