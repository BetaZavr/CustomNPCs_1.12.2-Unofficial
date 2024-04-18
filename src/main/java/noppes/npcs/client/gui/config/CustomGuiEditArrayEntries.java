package noppes.npcs.client.gui.config;

import static net.minecraftforge.fml.client.config.GuiUtils.INVALID;
import static net.minecraftforge.fml.client.config.GuiUtils.VALID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ArrayEntry;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLLog;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.config.CustomNpcsConfigGui.CustomGuiEditArray;
import noppes.npcs.util.ObfuscationHelper;

public class CustomGuiEditArrayEntries
extends GuiEditArrayEntries {

	protected GuiEditArray owningGui;
	public IConfigElement configElement;
	public List<IArrayEntry> listEntries;
	public boolean isDefault;
	public boolean isChanged;
	public boolean canAddMoreEntries;
	public final int controlWidth;
	public final Object[] beforeValues;
	public Object[] currentValues;

	public CustomGuiEditArrayEntries(GuiEditArray parent, Minecraft mc, IConfigElement configElement, Object[] beforeValues, Object[] currentValues) {
		super(parent, mc, configElement, currentValues, currentValues);
		owningGui = parent;
		this.configElement = configElement;
		this.beforeValues = beforeValues;
		this.currentValues = currentValues;
		setShowSelectionBox(false);
		isChanged = !Arrays.deepEquals(beforeValues, currentValues);
		isDefault = Arrays.deepEquals(currentValues, configElement.getDefaults());
		canAddMoreEntries = !configElement.isListLengthFixed() && (configElement.getMaxListLength() == -1 || currentValues.length < configElement.getMaxListLength());
		listEntries = new ArrayList<IArrayEntry>();
		controlWidth = (parent.width / 2) - (configElement.isListLengthFixed() ? 0 : 48);
		if (configElement.isList() && configElement.getArrayEntryClass() != null) {
			Class<? extends IArrayEntry> clazz = (Class<? extends IArrayEntry>) configElement.getArrayEntryClass();
			for (Object value : currentValues) {
				try { listEntries.add(clazz.getConstructor(GuiEditArray.class, CustomGuiEditArrayEntries.class, IConfigElement.class, Object.class).newInstance(this.owningGui, this, configElement, value)); }
				catch (Throwable e) { FMLLog.log.error("There was a critical error instantiating the custom IGuiEditListEntry for property {}.", configElement.getName(), e); }
			}
		}
		else if (configElement.isList() && configElement.getType().equals(ConfigGuiType.BOOLEAN)) {
			for (Object value : currentValues) { listEntries.add(new BooleanEntry(this.owningGui, this, configElement, Boolean.valueOf(value.toString()))); }
		}
		else if (configElement.isList() && configElement.getType().equals(ConfigGuiType.INTEGER)) {
			for (Object value : currentValues) { listEntries.add(new IntegerEntry(this.owningGui, this, configElement, Integer.parseInt(value.toString()))); }
		}
		else if (configElement.isList() && configElement.getType().equals(ConfigGuiType.DOUBLE)) {
			for (Object value : currentValues) { listEntries.add(new DoubleEntry(this.owningGui, this, configElement, Double.parseDouble(value.toString()))); }
		}
		else if (configElement.isList() && configElement.getType().equals(ConfigGuiType.COLOR)) {
			for (Object value : currentValues) { listEntries.add(new ColorEntry(this.owningGui, this, configElement, value.toString())); }
		}
		else if (configElement.isList()) {
			for (Object value : currentValues) { listEntries.add(new StringEntry(this.owningGui, this, configElement, value.toString())); }
		}
		if (!configElement.isListLengthFixed()) { listEntries.add(new BaseEntry(this.owningGui, this, configElement)); }
	}

	@Override
	protected int getScrollBarX() { return width - (width / 4); }

	/**
	 * Gets the width of the list
	 */
	@Override
	public int getListWidth() { return owningGui.width; }

	@Override
	public IArrayEntry getListEntry(int index) { return listEntries.get(index); }

	@Override
	protected int getSize() { return listEntries.size(); }

	public boolean isChanged() { return isChanged; }

	public boolean isDefault() { return isDefault; }

	public void recalculateState() {
		isDefault = true;
		isChanged = false;
		int listLength = configElement.isListLengthFixed() ? listEntries.size() : listEntries.size() - 1;
		if (listLength != configElement.getDefaults().length) { isDefault = false; }
		if (listLength != beforeValues.length) { isChanged = true; }
		if (isDefault) {
			for (int i = 0; i < listLength; i++) {
				if (!configElement.getDefaults()[i].equals(listEntries.get(i).getValue())) { isDefault = false; }
			}
		}
		if (!isChanged) {
			for (int i = 0; i < listLength; i++) {
				if (!beforeValues[i].equals(listEntries.get(i).getValue())) { isChanged = true; }
			}
		}
	}

	protected void keyTyped(char eventChar, int eventKey) {
		for (IArrayEntry entry : this.listEntries) { entry.keyTyped(eventChar, eventKey); }
		recalculateState();
	}

	protected void updateScreen() {
		for (IArrayEntry entry : this.listEntries) { entry.updateCursorCounter(); }
	}

	protected void mouseClickedPassThru(int x, int y, int mouseEvent) {
		for (IArrayEntry entry : this.listEntries) { entry.mouseClicked(x, y, mouseEvent); }
	}

	protected boolean isListSavable() {
		for (IArrayEntry entry : this.listEntries) {
			if (!entry.isValueSavable()) { return false; }
		}
		return true;
	}

	protected void saveListChanges() {
		int listLength = configElement.isListLengthFixed() ? listEntries.size() : listEntries.size() - 1;
		int slotIndex = ObfuscationHelper.getValue(GuiEditArray.class, owningGui, int.class);
		GuiScreen parentScreen = ObfuscationHelper.getValue(GuiEditArray.class, owningGui, GuiScreen.class);
		if (slotIndex != -1 && parentScreen != null && parentScreen instanceof GuiConfig && ((GuiConfig) parentScreen).entryList.getListEntry(slotIndex) instanceof ArrayEntry) {
			ArrayEntry entry = (ArrayEntry) ((GuiConfig) parentScreen).entryList.getListEntry(slotIndex);
			Object[] ao = new Object[listLength];
			for (int i = 0; i < listLength; i++) { ao[i] = listEntries.get(i).getValue(); }
			entry.setListFromChildScreen(ao);
		}
		else {
			if (configElement.isList() && configElement.getType() == ConfigGuiType.BOOLEAN) {
				Boolean[] abol = new Boolean[listLength];
				for (int i = 0; i < listLength; i++) { abol[i] = Boolean.valueOf(listEntries.get(i).getValue().toString()); }
				configElement.set(abol);
			}
			else if (configElement.isList() && configElement.getType() == ConfigGuiType.INTEGER) {
				Integer[] ai = new Integer[listLength];
				for (int i = 0; i < listLength; i++) { ai[i] = Integer.valueOf(listEntries.get(i).getValue().toString()); }
				configElement.set(ai);
			}
			else if (configElement.isList() && configElement.getType() == ConfigGuiType.DOUBLE) {
				Double[] ad = new Double[listLength];
				for (int i = 0; i < listLength; i++) { ad[i] = Double.valueOf(listEntries.get(i).getValue().toString()); }
				configElement.set(ad);
			}
			else if (configElement.isList()) {
				String[] as = new String[listLength];
				for (int i = 0; i < listLength; i++) { as[i] = listEntries.get(i).getValue().toString(); }
				configElement.set(as);
			}
		}
	}

	protected void drawScreenPost(int mouseX, int mouseY, float f) {
		for (IArrayEntry entry : this.listEntries) { entry.drawToolTip(mouseX, mouseY); }
	}

	public Minecraft getMC() { return this.mc; }

	/**
	 * IGuiListEntry Inner Classes
	 */
	
	public static class ColorEntry extends BaseEntry {
		
		protected final GuiButtonExt btnValue;
		String value;
		
		public ColorEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement, String string) {
			super(owningScreen, owningEntryList, configElement);
			value = string;
			btnValue = new GuiButtonExt(0, 0, 0, owningEntryList.controlWidth, 18, I18n.format(String.valueOf(value)));
			boolean enabled = ObfuscationHelper.getValue(GuiEditArray.class, owningScreen, boolean.class);
			btnValue.enabled = enabled;
			isValidated = false;
		}
		
		public GuiButtonExt getButon() { return btnValue; }

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			btnValue.x = listWidth / 4;
			btnValue.y = y;
			String trans = I18n.format(String.valueOf(value));
			if (!trans.equals(String.valueOf(value))) { btnValue.displayString = trans; }
			else { btnValue.displayString = String.valueOf(value); }
			btnValue.drawButton(owningEntryList.getMC(), mouseX, mouseY, partial);
			
			int left = btnValue.x + btnValue.width + 2;
			int right = left + 50;
			GuiUtils.drawGradientRect(0, left, y, right, y + 18, 0xFF000000, 0xFF000000);
			int color = (int) Long.parseLong(value, 16);
			float alpha = (float)(color >> 24 & 255) / 255.0F;
			if (alpha == 0.0f) { color += 0xFF000000; }
			GuiUtils.drawGradientRect(0, left + 1, y + 1, right - 1, y + 17, color, color);
		}

		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (btnValue.mousePressed(owningEntryList.getMC(), x, y)) {
				CustomGuiEditArray.subGui = new SubGuiColorSelector((int) Long.parseLong(value, 16));
				CustomGuiEditArray.subGui.width = owningEntryList.width;
				CustomGuiEditArray.subGui.height = owningEntryList.height;
				CustomGuiEditArray.subGui.initGui();
				CustomGuiEditArray.subGui.parent = owningScreen;
				CustomGuiEditArray.subGui.object = this;
				return true;
			}
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		}

		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			this.btnValue.mouseReleased(x, y);
			super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
		}

		@Override
		public Object getValue() { return value; }
		
	}

	public static class DoubleEntry extends StringEntry {
		
		public DoubleEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement, Double value) {
			super(owningScreen, owningEntryList, configElement, value);
			this.isValidated = true;
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			boolean enabled = ObfuscationHelper.getValue(GuiEditArray.class, owningScreen, boolean.class);
			if (enabled || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
					|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = this.textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar)) ||
						(!before.startsWith("-") && this.textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| (!before.contains(".") && eventChar == '.')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
						|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) { this.textFieldValue.textboxKeyTyped((enabled ? eventChar : Keyboard.CHAR_NONE), eventKey); }

				if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
					try {
						double value = Double.parseDouble(textFieldValue.getText().trim());
						if (value < Double.valueOf(configElement.getMinValue().toString()) || value > Double.valueOf(configElement.getMaxValue().toString())) { this.isValidValue = false; }
						else { this.isValidValue = true; }
					}
					catch (Throwable e) { this.isValidValue = false; }
				}
				else { this.isValidValue = false; }
			}
		}

		@Override
		public Double getValue() {
			try { return Double.valueOf(this.textFieldValue.getText().trim()); }
			catch (Throwable e) { return Double.MAX_VALUE; }
		}
	
	}

	public static class IntegerEntry extends StringEntry {
		
		public IntegerEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement, Integer value) {
			super(owningScreen, owningEntryList, configElement, value);
			this.isValidated = true;
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			boolean enabled = ObfuscationHelper.getValue(GuiEditArray.class, owningScreen, boolean.class);
			if (enabled || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
					|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = this.textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar))
						|| (!before.startsWith("-") && this.textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE
						|| eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
					this.textFieldValue.textboxKeyTyped((enabled ? eventChar : Keyboard.CHAR_NONE), eventKey);

				if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-"))
				{
					try
					{
						long value = Long.parseLong(textFieldValue.getText().trim());
						if (value < Integer.valueOf(configElement.getMinValue().toString()) || value > Integer.valueOf(configElement.getMaxValue().toString()))
							this.isValidValue = false;
						else
							this.isValidValue = true;
					}
					catch (Throwable e)
					{
						this.isValidValue = false;
					}
				}
				else
					this.isValidValue = false;
			}
		}

		@Override
		public Integer getValue() {
			try { return Integer.valueOf(this.textFieldValue.getText().trim()); }
			catch (Throwable e) { return Integer.MAX_VALUE; }
		}
		
	}

	public static class StringEntry extends BaseEntry {
		
		protected final GuiTextField textFieldValue;

		public StringEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value) {
			super(owningScreen, owningEntryList, configElement);
			this.textFieldValue = new GuiTextField(0, owningEntryList.getMC().fontRenderer, owningEntryList.width / 4 + 1, 0, owningEntryList.controlWidth - 3, 16);
			this.textFieldValue.setMaxStringLength(10000);
			this.textFieldValue.setText(value.toString());
			this.isValidated = configElement.getValidationPattern() != null;
			if (configElement.getValidationPattern() != null) {
				if (configElement.getValidationPattern().matcher(this.textFieldValue.getText().trim()).matches()) { isValidValue = true; }
				else { isValidValue = false; }
			}
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			if (configElement.isListLengthFixed() || slotIndex != owningEntryList.listEntries.size() - 1) {
				this.textFieldValue.setVisible(true);
				this.textFieldValue.y = y + 1;
				this.textFieldValue.drawTextBox();
			}
			else { this.textFieldValue.setVisible(false); }
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			boolean enabled = ObfuscationHelper.getValue(GuiEditArray.class, owningScreen, boolean.class);
			if (enabled || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
					|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				this.textFieldValue.textboxKeyTyped((enabled ? eventChar : Keyboard.CHAR_NONE), eventKey);
				if (configElement.getValidationPattern() != null) {
					if (configElement.getValidationPattern().matcher(this.textFieldValue.getText().trim()).matches()) { isValidValue = true; }
					else { isValidValue = false; }
				}
			}
		}

		@Override
		public void updateCursorCounter() { this.textFieldValue.updateCursorCounter(); }

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) { this.textFieldValue.mouseClicked(x, y, mouseEvent); }

		@Override
		public Object getValue() { return this.textFieldValue.getText().trim(); }

	}

	public static class BooleanEntry extends BaseEntry {
		
		protected final GuiButtonExt btnValue;
		private boolean value;

		public BooleanEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement, boolean value) {
			super(owningScreen, owningEntryList, configElement);
			this.value = value;
			this.btnValue = new GuiButtonExt(0, 0, 0, owningEntryList.controlWidth, 18, I18n.format(String.valueOf(value)));
			boolean enabled = ObfuscationHelper.getValue(GuiEditArray.class, owningScreen, boolean.class);
			this.btnValue.enabled = enabled;
			this.isValidated = false;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			this.btnValue.x = listWidth / 4;
			this.btnValue.y = y;
			String trans = I18n.format(String.valueOf(value));
			if (!trans.equals(String.valueOf(value))) { this.btnValue.displayString = trans; }
			else { this.btnValue.displayString = String.valueOf(value); }
			btnValue.packedFGColour = value ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
			this.btnValue.drawButton(owningEntryList.getMC(), mouseX, mouseY, partial);
		}

		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (this.btnValue.mousePressed(owningEntryList.getMC(), x, y)) {
				btnValue.playPressSound(owningEntryList.getMC().getSoundHandler());
				value = !value;
				owningEntryList.recalculateState();
				return true;
			}
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		}

		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			this.btnValue.mouseReleased(x, y);
			super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
		}

		@Override
		public Object getValue() { return value; }
		
	}

	public static class BaseEntry implements IArrayEntry {
		
		protected final GuiEditArray owningScreen;
		protected final CustomGuiEditArrayEntries owningEntryList;
		protected final IConfigElement configElement;
		protected boolean isValidValue = true;
		protected boolean isValidated = false;

		public BaseEntry(GuiEditArray owningScreen, CustomGuiEditArrayEntries owningEntryList, IConfigElement configElement) {
			this.owningScreen = owningScreen;
			this.owningEntryList = owningEntryList;
			this.configElement = configElement;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			if (this.getValue() != null && this.isValidated) {
				owningEntryList.getMC().fontRenderer.drawString(
						isValidValue ? TextFormatting.GREEN + VALID : TextFormatting.RED + INVALID,
						listWidth / 4 - owningEntryList.getMC().fontRenderer.getStringWidth(VALID) - 2,
						y + slotHeight / 2 - owningEntryList.getMC().fontRenderer.FONT_HEIGHT / 2,
						16777215);
			}
		}

		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { return false; }

		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

		@Override
		public void keyTyped(char eventChar, int eventKey) {}

		@Override
		public void updateCursorCounter() {}

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) {}

		@Override
		public boolean isValueSavable() { return isValidValue; }

		@Override
		public Object getValue() { return null; }

		@Override
		public void updatePosition(int p_178011_1_, int p_178011_2_, int p_178011_3_, float partial){}

		@Override
		public void drawToolTip(int mouseX, int mouseY) { }
		
	}

}
	