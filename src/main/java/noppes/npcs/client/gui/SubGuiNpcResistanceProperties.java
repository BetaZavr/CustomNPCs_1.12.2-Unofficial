package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
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
	private final Map<String, String> data = new HashMap<>();
	private GuiCustomScroll scroll;
	private String select = "";

	public SubGuiNpcResistanceProperties(Resistances resist) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		resistances = resist;
		Client.sendData(EnumPacketServer.GetResistances);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> names = new ArrayList<>();
		List<String> notList = new ArrayList<>();
		Map<String, String> mapSfx = new HashMap<>();
		for (String name : data.keySet()) {
			if (resistances.data.containsKey(data.get(name))) {
				names.add(name);
				float v = (2.0f - resistances.data.get(data.get(name)));
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
		if (select.isEmpty() && !names.isEmpty()) { select = Util.instance.deleteColor(names.get(0)); }
		List<String> suffixes = new ArrayList<>();
		for (String key : names) { suffixes.add(mapSfx.get(key)); }
		
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(248, 176); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 4;
		scroll.setSelected(npc.linkedName);
		scroll.setListNotSorted(names);
		scroll.setSuffixes(suffixes);
		scroll.setSelected(select);
		addScroll(scroll);
		
		int y = guiTop + ySize - 34;
		if (!select.isEmpty()) {
			float v = (2.0f - resistances.get(data.get(select)));
			int t = (int) (v * -100.0f + 100.0f);
			GuiNpcSlider slider = new GuiNpcSlider(this, 0, guiLeft + 4, y, (t == 0 ? "" : (((char) 167) + (t < 0 ? "c" : "a+"))) + String.valueOf(t).replace(".", ",") + "%", (float) t * 0.001667f + 0.833333f);
			slider.height = 14;
			slider.width = 248;

			ITextComponent mes = new TextComponentTranslation("stats.hover.resist", Util.instance.deleteColor(select));
			String damageType = data.get(select);
			v = Math.round(resistances.get(damageType) * 120.0f - 140.0f);
			if (v == 0.0f) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.0")); }
			else if (v < 0.0f) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.1", "" + (v * -1.0f))); }
			else { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.2", "" + v)); }
			slider.setHoverText(mes.getFormattedText());
			addSlider(slider);
			
			GuiNpcTextField textField = new GuiNpcTextField(0, this, guiLeft + 4, y + 16, 60, 14, "" + t);
			textField.setMinMaxDefault(-500, 100, t);
			textField.setHoverText(mes.getFormattedText());
			addTextField(textField);
		}
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 190, y + 16, 60, 14, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
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
		if (!data.containsKey(select)) { return; }
		setValue(data.get(select), (int) (slider.sliderValue * 600.0f - 500.0f));
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
		data.clear();
		for (String name : list) {
			String trName = Util.instance.deleteColor(new TextComponentTranslation("resistance." + name.toLowerCase()).getFormattedText());
			if (trName.equals("resistance." + name.toLowerCase())) { trName = name; }
			data.put(trName, name);
		}
		initGui();
	}

	@Override
	public void setSelected(String select) { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!scroll.hasSelected() || !data.containsKey(Util.instance.deleteColor(scroll.getSelected()))) { return; }
		select = Util.instance.deleteColor(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (!data.containsKey(select)) { return; }
		setValue(data.get(select), textField.getInteger());
	}

	private void setValue(String damageType, int value) {
		if (value == 0 && !damageType.equals("arrow") && !damageType.equals("thrown") &&
				!damageType.equals("player") && !damageType.equals("mob") && 
				!damageType.equals("explosion") && !damageType.equals("explosion.player") &&
				!damageType.equals("knockback")) {
			resistances.data.remove(damageType);
		} else {
			resistances.data.put(damageType, value * 0.01f + 1.0f);
		}
		initGui();
	}

}
