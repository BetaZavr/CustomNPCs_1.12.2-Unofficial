package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityStoredData;

public class SubGuiNpcAvailabilityStoredData
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener {

	private Availability availabitily;
	private final Map<String, AvailabilityStoredData> data;
	private GuiCustomScroll scroll;
	private AvailabilityStoredData select;
	private int keyError;
	
	public SubGuiNpcAvailabilityStoredData(Availability availabitily) {
		this.availabitily = availabitily;
		this.setBackground("menubg.png");
		this.xSize = 316;
		this.ySize = 217;
		this.closeOnEsc = true;
		
		this.data = Maps.<String, AvailabilityStoredData>newTreeMap();
		this.select = null;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		int y = this.guiTop + this.ySize - 46;
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, y + 22, 70, 20, "gui.done"));
		this.data.clear();
		String selKey = "";
		for (AvailabilityStoredData sd : this.availabitily.storeddata) {
			String key = ((char) 167) + (sd.has ? "a+" : "c-") + ((char) 167) + "6\"" + ((char) 167) + "r" + sd.key + ((char) 167) + "6\"";
			if (!sd.value.isEmpty()) {
				key += ((char) 167) + "r == " + ((char) 167) + "b\"" + ((char) 167) + "r" + sd.value + ((char) 167) + "b\"";
			}
			this.data.put(key , sd);
			if (this.select != null && this.select.key.equals(sd.key)) {
				this.select = sd;
				selKey = key;
			}
		}
		if (this.select != null && selKey.isEmpty()) { this.select = null; }
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(this.xSize - 12, this.ySize - 64); }
		this.scroll.setList(Lists.newArrayList(this.data.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!selKey.isEmpty()) { this.scroll.setSelected(selKey); }
		else { this.scroll.selected = -1; }
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, y, 50, 20, new String[] { "availability.only", "availability.except" }, this.select == null || this.select.has ? 0 : 1));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 290, y, 20, 20, "X"));
		this.getButton(2).setEnabled(this.select != null);
		int x = this.guiLeft + 58;
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x, y + 1, 112, 18, this.select != null ? this.select.key : "");
		textField.setMaxStringLength(120);
		this.addTextField(textField);
		textField = new GuiNpcTextField(1, this, x + 116, y + 1, 112, 18, this.select != null ? this.select.value : "");
		textField.setMaxStringLength(120);
		this.addTextField(textField);
		this.addButton(new GuiNpcButton(3, this.guiLeft + this.xSize - 76, y + 22, 70, 20, "availability.more"));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				if (this.select == null) { return; }
				this.select.has = button.getValue() == 0;
				this.initGui();
				break;
			}
			case 2: { // remove
				if (this.select == null) { return; }
				this.availabitily.storeddata.remove(this.select);
				this.select = null;
				this.initGui();
				break;
			}
			case 3: { // more
				if (this.getTextField(0) == null || this.getTextField(1) == null || this.getButton(0) == null) { return; }
				if (this.select != null) {
					String key = this.getTextField(0).getText();
					int i = 0;
					while(true) {
						if (i >= this.availabitily.storeddata.size()) { break; }
						AvailabilityStoredData asd = this.availabitily.storeddata.get(i);
						i++;
						if (asd == this.select) {
							continue;
						}
						if (asd.key.equals(key)) {
							key += "_";
							i = 0;
						}
					}
					this.select.key = key;
					this.select.value = this.getTextField(1).getText();
					this.select.has = this.getButton(0).getValue() == 0;
					this.select = null;
				}
				else {
					String key = this.getTextField(0).getText();
					int i = 0;
					while(true) {
						if (i >= this.availabitily.storeddata.size()) { break; }
						AvailabilityStoredData asd = this.availabitily.storeddata.get(i);
						i++;
						if (asd.key.equals(key)) {
							key += "_";
							i = 0;
						}
					}
					this.availabitily.storeddata.add(new AvailabilityStoredData(key, this.getTextField(1).getText(), this.getButton(0).getValue() == 0));
				}
				this.initGui();
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.keyError > 0) {
			this.keyError--;
			if (this.getTextField(0) != null) {
				GuiNpcTextField textField = this.getTextField(0);
				if (this.keyError != 0) {
					textField.setTextColor(0xFFFF0000);
					textField.setDisabledTextColour(0xFFFF0000);
				} else {
					textField.setTextColor(0xFFFFFFFF);
					textField.setDisabledTextColour(0xFFFFFFFF);
				}
			}
		}
		if (this.getButton(3) != null && this.getTextField(0) != null) {
			this.getButton(3).setEnabled(!this.getTextField(0).getText().isEmpty());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.sd.key").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.sd.value").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.sdtype."+this.getButton(0).getValue()).getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.remove").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.more").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void save() {
	}
	
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) { return; }
		this.select = this.data.get(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId()==0) {
			if (textfield.isEmpty()) { return; }
			String key = textfield.getText();
			int i = 0;
			while(true) {
				if (i >= this.availabitily.storeddata.size()) { break; }
				AvailabilityStoredData asd = this.availabitily.storeddata.get(i);
				i++;
				if (asd == this.select) {
					continue;
				}
				if (asd.key.equals(key)) {
					key += "_";
					i = 0;
				}
			}
			if (!textfield.getText().equals(key)) {
				textfield.setText(key);
				this.keyError = 60;
			}
			if (this.select != null) {
				this.select.key = key;
				this.initGui();
			}
		} else if (this.select != null) {
			this.select.value = textfield.getText();
			this.initGui();
		}
	}
}
