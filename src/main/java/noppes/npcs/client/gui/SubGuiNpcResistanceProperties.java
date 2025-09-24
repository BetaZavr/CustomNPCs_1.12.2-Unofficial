package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.data.Resistances;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNpcResistanceProperties extends SubGuiInterface
		implements ICustomScrollListener, ISliderListener, IScrollData, ITextfieldListener {

	protected final Resistances resistances;
	protected final Map<String, String> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcResistanceProperties(Resistances resistancesIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		resistances = resistancesIn;
		Client.sendData(EnumPacketServer.GetResistances);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.id == 66) { onClosed(); }
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
		
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(248, 176); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 4;
		scroll.setSelected(npc.linkedName)
				.setUnsortedList(names)
				.setSuffixes(suffixes)
				.setSelected(select);
		addScroll(scroll);

		int y = guiTop + ySize - 34;
		if (!select.isEmpty()) {
			float v = (2.0f - resistances.get(data.get(select)));
			int t = (int) (v * -100.0f + 100.0f);
			ITextComponent mes = new TextComponentTranslation("stats.hover.resist", Util.instance.deleteColor(select));
			if (t == 0) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.0")); }
			else if (t < 0) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.1", "" + t)); }
			else { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.2", "" + t)); }
			addSlider(new GuiNpcSlider(this, 0, guiLeft + 4, y, 248, 14, (t == 0 ? "" : (((char) 167) + (t < 0 ? "c" : "a+"))) + String.valueOf(t).replace(".", ",") + "%", (float) t * 0.001667f + 0.833333f)
					.setHoverText(mes.getFormattedText()));
			addTextField(new GuiNpcTextField(0, this, guiLeft + 4, y + 16, 60, 14, "" + t)
					.setMinMaxDefault(-500, 100, t)
					.setHoverText(mes.getFormattedText()));
		}
		addButton(new GuiNpcButton(66, guiLeft + 190, y + 16, 60, 14, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		float n = 5.0f / 6.0f;
		int t = Math.round(slider.sliderValue * 600.0f - 500.0f);
		slider.displayString = slider.sliderValue == n ? "" : (((char) 167) + (slider.sliderValue < n ? "c" : "a+")) + String.valueOf(t).replace(".", ",") + "%";
		ITextComponent mes = new TextComponentTranslation("stats.hover.resist", Util.instance.deleteColor(select));
		if (t == 0) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.0")); }
		else if (t < 0) { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.1", "" + t)); }
		else { mes.appendSibling(new TextComponentTranslation("stats.hover.resist.2", "" + t)); }
		slider.setHoverText(mes.getFormattedText());
		if (getTextField(0) != null) { getTextField(0).setHoverText(mes.getFormattedText()).setText("" + t); }
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if (!data.containsKey(select)) { return; }
		setValue(data.get(select), (int) (slider.sliderValue * 600.0f - 500.0f));
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		for (String name : dataList) {
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
		}
		else { resistances.data.put(damageType, value * 0.01f + 1.0f); }
		initGui();
	}

}
