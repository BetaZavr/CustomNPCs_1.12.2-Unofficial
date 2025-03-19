package noppes.npcs.client.gui;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.util.Util;

public class SubGuiNpcMarketSettings
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener, ISubGuiListener, GuiYesNoCallback {

	public Marcet marcet;
	public int level = 0;
	private final Map<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scroll;

	public SubGuiNpcMarketSettings(Marcet market) {
		super();
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		marcet = market;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				marcet.limitedType = button.getValue();
				button.setHoverText("market.hover.only.currency." + marcet.limitedType);
				break;
			}
			case 1: { // message
				setSubGui(new SubGuiNPCLinesEdit(0, npc, marcet.lines, null));
				break;
			}
			case 2: { // is limited
				marcet.isLimited = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 3: { // show xp
				marcet.showXP = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 4: { // level
				level = button.getValue();
				if (!marcet.markup.containsKey(0)) {
					marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000));
				}
				if (!marcet.markup.containsKey(level)) {
					level = 0;
				}
				initGui();
				break;
			}
			case 5: { // add section
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 6: { // del section
				if (scroll.getSelect() < 0) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("gui.sections").getFormattedText() + ": " + scroll.getSelected(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 66: { // exit
				close();
				break;
			}
		}
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result || scroll.getSelect() < 0 || !data.containsKey(scroll.getSelected())
				|| marcet.sections.size() < 2) {
			return;
		}
		marcet.sections.remove(data.get(scroll.getSelected()));
		if (scroll.getSelect() > 0) {
			scroll.setSelect(scroll.getSelect() - 1);
		}
		initGui();
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (subgui != null) { return; }
		if (getButton(4) != null) {
			int color = new Color(0x80000000).getRGB();
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 4, getButton(4).getLeft() - 3, color);
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 4, getButton(4).getTop() + 44, color);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lID = 0;
		int x = guiLeft + 4;
		int y = guiTop + 5;
		// name
		addLabel(new GuiNpcLabel(lID++, "role.trader", x + 2, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x + 80, y, 167, 18, marcet.name);
		textField.setHoverText("market.hover.set.name", new TextComponentTranslation(marcet.name).getFormattedText());
		addTextField(textField);
		// time
		y += 22;
		addLabel(new GuiNpcLabel(lID++, "market.uptime", x + 2, y + 5));
		textField = new GuiNpcTextField(1, this, x + 80, y, 60, 18, "" + marcet.updateTime);
		textField.setMinMaxDefault(0, 360, marcet.updateTime);
		textField.setHoverText("market.hover.set.update", Util.instance.ticksToElapsedTime(marcet.updateTime * 1200L, false, false, false));
		addTextField(textField);
		GuiNpcButton button;
		if (marcet.updateTime >= 5) {
			y += 22;
			button = new GuiNpcButton(0, x, y, 170, 20, new String[] { "market.limited.0", "market.limited.1", "market.limited.2" }, marcet.limitedType);
			button.setHoverText("market.hover.only.currency." + marcet.limitedType);
			addButton(button);
		}
		// tabs
		addLabel(new GuiNpcLabel(lID++, "gui.sections", x + 176, y - 17));
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(72, 60);
		}
		scroll.guiLeft = x + 175;
		scroll.guiTop = y;
		List<String> list = new ArrayList<>();
		data.clear();
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 0;
		for (int id : marcet.sections.keySet()) {
			List<String> l = new ArrayList<>();
			l.add("ID: " + id);
			l.add(new TextComponentTranslation("gui.name").getFormattedText() + ": " + marcet.sections.get(id));
			hts.put(i, l);
			String key = marcet.sections.get(id).name;
			data.put(key, id);
			list.add(key);
			i++;
		}
		scroll.setListNotSorted(list);
		scroll.setHoverTexts(hts);
		addScroll(scroll);
		// update message
		y += 22;
		button = new GuiNpcButton(1, x, y, 170, 20, "lines.title");
		button.setHoverText("market.hover.message");
		addButton(button);
		// isLimited
		y += 20;
		button = new GuiNpcCheckBox(2, x, y, 170, 18, "market.select.limited.true", "market.select.limited.false", marcet.isLimited);
		button.setHoverText("market.hover.limited");
		addButton(button);
		// show XP
		y += 20;
		button = new GuiNpcCheckBox(3, x, y, 170, 18, "market.select.show.xp.true", "market.select.show.xp.false", marcet.showXP);
		button.setHoverText("market.hover.show.xp");
		addButton(button);
		// add new section
		button = new GuiNpcButton(5, x + 175, y, 37, 20, "type.add");
		button.setHoverText("market.hover.section.add");
		addButton(button);
		// del section
		button = new GuiNpcButton(6, x + 213, y, 35, 20, "type.del");
		button.setEnabled(marcet.sections.size() > 1 && scroll.getSelect() > 0);
		button.setHoverText("market.hover.section.del");
		addButton(button);
		// levels
		y += 25;
		String[] values = new String[marcet.markup.size()];
		i = 0;
		for (int level : marcet.markup.keySet()) {
			values[i] = (new TextComponentTranslation("type.level")).getFormattedText() + " " + level;
			i++;
		}
		addLabel(new GuiNpcLabel(lID++, "gui.type", x + 2, y + 5));
		button = new GuiNpcButton(4, x + 22, y, 50, 20, values, level);
		button.setHoverText("market.hover.extra.slot");
		addButton(button);
		// extra markup
		MarkupData md = marcet.markup.get(level);
		if (md == null) {
			level = 0;
			if (!marcet.markup.containsKey(0)) {
				marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000));
			}
			md = marcet.markup.get(level);
		}
		// buy
		addLabel(new GuiNpcLabel(lID++, "market.extra.markup", x + 76, y + 5));
		addLabel(new GuiNpcLabel(lID++, "%", x + 174, y + 5));
		textField = new GuiNpcTextField(2, this, x + 120, y, 50, 20, "" + (int) (md.buy * 100.0f));
		textField.setMinMaxDefault(-100, 500, (int) (md.buy * 100.0f));
		textField.setHoverText("market.hover.extra.buy");
		addTextField(textField);
		// sell
		addLabel(new GuiNpcLabel(lID++, "%", x + 238, y + 5));
		textField = new GuiNpcTextField(3, this, x + 184, y, 50, 20, "" + (int) (md.sell * 100.0f));
		textField.setMinMaxDoubleDefault(-500, 100, (int) (md.sell * 100.0f));
		textField.setHoverText("market.hover.extra.sell");
		addTextField(textField);
		// xp
		y += 22;
		addLabel(new GuiNpcLabel(lID, "quest.exp", x + 76, y + 5));
		textField = new GuiNpcTextField(4, this, x + 120, y, 50, 20, "" + md.xp);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, md.xp);
		textField.setHoverText("market.hover.xp");
		addTextField(textField);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize - 24, 60, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (getButton(6) != null) {
			getButton(6).setEnabled(marcet.sections.size() > 1 && scroll.getSelect() > 0);
		}
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		setSubGui(new SubGuiEditText(2, scroll.getSelected()));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) {
				return;
			}
			if (subgui.getId() == 1) {
				String name = ((SubGuiEditText) subgui).text[0];
				boolean has = true;
				while (has) {
					has = false;
					for (MarcetSection s : marcet.sections.values()) {
						if (s.name.equals(name)) {
							has = true;
							break;
						}
					}
					if (has) {
						name += "_";
					}
				}
				MarcetSection ms = new MarcetSection(marcet.sections.size());
				ms.name = name;
				marcet.sections.put(ms.getId(), ms);
			} else if (subgui.getId() == 2) {
				if (!data.containsKey(scroll.getSelected())) {
					return;
				}
				String name = ((SubGuiEditText) subgui).text[0];
				int idSel = data.get(scroll.getSelected());
				boolean next = true;
				while (next) {
					next = false;
					for (int id : marcet.sections.keySet()) {
						if (id == idSel) {
							continue;
						}
						if (marcet.sections.get(id).name.equals(name)) {
							name += "_";
							next = true;
							break;
						}
					}
				}
				MarcetSection ms = new MarcetSection(idSel);
				ms.name = name;
				marcet.sections.put(ms.getId(), ms);
			}
			initGui();
		} else if (subgui instanceof SubGuiNPCLinesEdit) {
			SubGuiNPCLinesEdit sub = (SubGuiNPCLinesEdit) subgui;
			sub.lines.correctLines();
			marcet.lines = sub.lines;
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (hasSubGui()) {
			return;
		}
		String text = textField.getText();
		MarkupData md = marcet.markup.get(level);
		switch (textField.getID()) {
			case 0: {
				if (text.equals(marcet.name)) {
					return;
				}
				marcet.name = text;
				initGui();
				break;
			}
			case 1: {
				int time = textField.getInteger();
				if (time < 5) {
					time = 0;
				}
				if (time > 360) {
					time = 360;
				}
				marcet.updateTime = time;
				initGui();
				break;
			}
			case 2: {
				if (md == null) {
					return;
				}
				md.buy = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				initGui();
				break;
			}
			case 3: {
				if (md == null) {
					return;
				}
				md.sell = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				initGui();
				break;
			}
			case 4: {
				if (md == null) {
					return;
				}
				md.xp = textField.getInteger();
				break;
			}
		}
	}

}
