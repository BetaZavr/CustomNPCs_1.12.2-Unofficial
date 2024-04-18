package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityPlayerName;
import noppes.npcs.controllers.data.Availability;

public class SubGuiNpcAvailabilityNames
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener {
	
	private Availability availabitily;
	private final Map<String, EnumAvailabilityPlayerName> data;
	private GuiCustomScroll scroll;
	private String select;
	
	public SubGuiNpcAvailabilityNames(Availability availabitily) {
		this.availabitily = availabitily;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.data = new HashMap<String, EnumAvailabilityPlayerName>();
		this.select = "";
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				if (this.select.isEmpty()) {
					return;
				}
				EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.values()[button.getValue()];
				this.availabitily.playerNames.put(this.select, eapn);
				this.initGui();
				break;
			}
			case 1: {
				SubGuiEditText subGui = new SubGuiEditText(0, this.select);
				subGui.hovers[0] = "availabitily.hover.player.name";
				this.setSubGui(subGui);
				break;
			}
			case 2: {
				this.availabitily.playerNames.remove(this.select);
				this.select = "";
				this.initGui();
				break;
			}
			case 3: {
				this.save();
				this.initGui();
				break;
			}
			case 66: {
				this.close();
				break;
			}
			default: { break; }
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.name."+this.getButton(0).getValue()).getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.player.name").getFormattedText());
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
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, this.guiTop + 192, 70, 20, "gui.done"));

		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(this.xSize - 12, this.ySize - 66);
		}
		this.data.clear();
		for (String name : this.availabitily.playerNames.keySet()) {
			this.data.put(name, this.availabitily.playerNames.get(name));
		}
		if (!this.select.isEmpty() && !this.data.containsKey(this.select)) {
			this.select = "";
		}
		this.scroll.setList(Lists.newArrayList(this.data.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!this.select.isEmpty()) { this.scroll.setSelected(this.select); }
		else { this.scroll.selected = -1; }
		this.addScroll(this.scroll);
		int p = 0;
		if (!this.select.isEmpty()) {
			p = this.data.get(this.select).ordinal();
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20, new String[] { "availability.only", "availability.except" }, p));
		this.addButton( new GuiNpcButton(1, this.guiLeft + 58, this.guiTop + this.ySize - 46, 170, 20, "availability.select"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 230, this.guiTop + this.ySize - 46, 20, 20, "X"));
		this.addButton( new GuiNpcButton(3, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20, "availability.more"));
		this.getButton(3).setEnabled(!this.select.isEmpty());
		if (!this.select.isEmpty()) { this.getButton(1).setDisplayText(this.select); }
		this.getButton(2).setEnabled(!this.select.isEmpty());
	}

	@Override
	public void save() {
		if (this.select.isEmpty()) { return; }
		EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.values()[this.getButton(0).getValue()];
		this.availabitily.playerNames.put(this.select, eapn);
		this.select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		SubGuiEditText subGui = new SubGuiEditText(0, this.select);
		subGui.hovers[0] = "availabitily.hover.player.name";
		this.setSubGui(subGui);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		SubGuiEditText selector = (SubGuiEditText) subgui;
		if (selector.cancelled) { return; }
		EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.Only;
		if (!this.select.isEmpty()) {
			eapn = this.data.get(this.select);
			this.availabitily.playerNames.remove(this.select);
		}
		this.select = selector.text[0];
		this.availabitily.playerNames.put(this.select, eapn);
		this.initGui();
	}
	
}
