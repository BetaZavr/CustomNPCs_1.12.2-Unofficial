package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.data.Resistances;
import noppes.npcs.util.Util;

public class SubGuiNpcResistanceProperties
extends SubGuiInterface
implements ICustomScrollListener, ISliderListener, IScrollData, ITextfieldListener {

	private final Resistances resistances;
	private final Map<String, String> data = Maps.newHashMap();
	private GuiCustomScroll scroll;
	private String select = "";

	public SubGuiNpcResistanceProperties(Resistances resistances) {
		this.resistances = resistances;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
		Client.sendData(EnumPacketServer.GetResistances);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions || this.scroll == null) { return; }
		if ((this.getSlider(0) != null && this.getSlider(0).isMouseOver()) ||
				(this.getTextField(0) != null && this.getTextField(0).isMouseOver())) {
			ITextComponent mes = new TextComponentTranslation("stats.hover.resist", Util.instance.deleteColor(this.select));
			String damageType = this.data.get(this.select);
			float v = Math.round(this.resistances.get(damageType) * 120.0f - 140.0f);
			if (v == 0.0f) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.0")); }
			else if (v < 0.0f) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.1", "" + (v * -1.0f))); }
			else { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.2", "" + v)); }
			this.setHoverText(mes.getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
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
		List<String> names = Lists.newArrayList();
		List<String> notList = Lists.newArrayList();
		Map<String, String> mapSfx = Maps.newHashMap();
		for (String name : this.data.keySet()) {
			if (this.resistances.data.containsKey(this.data.get(name))) {
				names.add(name);
				float v = (2.0f - this.resistances.data.get(this.data.get(name)));
				int t = (int) (v * -100.0f + 100.0f);
				mapSfx.put(name, (t == 0 ? "" : (((char) 167) + (t < 0 ? "c" : "a+"))) + t + "%");
			}
			else {
				String key = ((char) 167) + "7" + name;
				notList.add(key);
				mapSfx.put(key, ((char) 167) + "70%");
			}
		}
		Collections.sort(names);
		Collections.sort(notList);
		names.addAll(notList);
		if (this.select.isEmpty() && !names.isEmpty()) { this.select = Util.instance.deleteColor(names.get(0)); }
		
		List<String> suffixs = Lists.newArrayList();
		for (String key : names) { suffixs.add(mapSfx.get(key)); }
		
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(248, 176); }
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 4;
		this.scroll.setSelected(this.npc.linkedName);
		this.scroll.setListNotSorted(names);
		this.scroll.setSuffixes(suffixs);
		this.scroll.setSelected(this.select);
		
		this.addScroll(this.scroll);
		
		int y = this.guiTop + this.ySize - 34;
		
		if (!this.select.isEmpty()) {
			float v = (2.0f - this.resistances.get(this.data.get(this.select)));
			int t = (int) (v * -100.0f + 100.0f);
			GuiNpcSlider slider = new GuiNpcSlider(this, 0, this.guiLeft + 4, y, (t == 0 ? "" : (((char) 167) + (t < 0 ? "c" : "a+"))) + String.valueOf(t).replace(".", ",") + "%", (float) t * 0.001667f + 0.833333f);
			slider.height = 14;
			slider.width = 248;
			this.addSlider(slider);
			
			GuiNpcTextField textField = new GuiNpcTextField(0, this, this.guiLeft + 4, y + 16, 60, 14, "" + t);
			textField.setNumbersOnly();
			textField.setMinMaxDefault(-500, 100, t);
			this.addTextField(textField);
		}
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 190, y + 16, 60, 14, "gui.done"));
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		float n = 5.0f / 6.0f;
		slider.displayString = (slider.sliderValue == n ? "" : (((char) 167) + (slider.sliderValue < n ? "c" : "a+"))) + String.valueOf(Math.round(slider.sliderValue * 600.0f - 500.0f)).replace(".", ",") + "%";
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if (!this.data.containsKey(this.select)) { return; }
		this.setValue(this.data.get(this.select), (int) (slider.sliderValue * 600.0f - 500.0f));
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		for (String name : list) {
			String trName = Util.instance.deleteColor(new TextComponentTranslation("resistance." + name.toLowerCase()).getFormattedText());
			if (trName.equals("resistance." + name.toLowerCase())) { trName = name; }
			this.data.put(trName, name);
		}
		this.initGui();
	}

	@Override
	public void setSelected(String select) { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!scroll.hasSelected() || !this.data.containsKey(Util.instance.deleteColor(scroll.getSelected()))) { return; }
		this.select = Util.instance.deleteColor(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (!this.data.containsKey(this.select)) { return; }
		this.setValue(this.data.get(this.select), textField.getInteger());
	}

	private void setValue(String damageType, int value) {
		if (value == 0 && !damageType.equals("arrow") && !damageType.equals("thrown") &&
				!damageType.equals("player") && !damageType.equals("mob") && 
				!damageType.equals("explosion") && !damageType.equals("explosion.player") &&
				!damageType.equals("knockback")) {
			this.resistances.data.remove(damageType);
		} else {
			this.resistances.data.put(damageType, value * 0.01f + 1.0f);
		}
		this.initGui();
	}

}
