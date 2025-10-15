package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiNPCLinesEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNpcMarketSettings extends SubGuiInterface
		implements ICustomScrollListener, ITextfieldListener, GuiYesNoCallback {

	protected final Map<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	public Marcet marcet;
	public int level = 0;

	public SubGuiNpcMarketSettings(Marcet marketIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		marcet = marketIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				marcet.limitedType = button.getValue();
				button.setHoverText("market.hover.only.currency." + marcet.limitedType);
				break;
			}
			case 1: setSubGui(new SubGuiNPCLinesEdit(0, npc, marcet.lines, null)); break; // message
			case 2: marcet.isLimited = ((GuiNpcCheckBox) button).isSelected(); break; // is limited
			case 3: marcet.showXP = ((GuiNpcCheckBox) button).isSelected(); break; // show xp
			case 4: {
				level = button.getValue();
				if (!marcet.markup.containsKey(0)) { marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000)); }
				if (!marcet.markup.containsKey(level)) { level = 0; }
				initGui();
				break;
			} // level
			case 5: {
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			} // add section
			case 6: {
				if (!scroll.hasSelected()) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("gui.sections").getFormattedText() + ": " + scroll.getSelected(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // del section
			case 66: onClosed(); break;
		}
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result || scroll.getSelect() < 0 || !data.containsKey(scroll.getSelected()) || marcet.sections.size() < 2) { return; }
		marcet.sections.remove(data.get(scroll.getSelected()));
		scroll.setSelect(scroll.getSelect() - 1);
		initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (hasSubGui()) { return; }
		if (getButton(4) != null) {
			int color = new Color(0x80000000).getRGB();
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 4, getButton(4).x - 3, color);
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 4, getButton(4).y + 44, color);
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
		addTextField(new GuiNpcTextField(0, this, x + 80, y, 167, 18, marcet.name)
				.setHoverText("market.hover.set.name", new TextComponentTranslation(marcet.name).getFormattedText()));
		// time
		y += 22;
		addLabel(new GuiNpcLabel(lID++, "market.uptime", x + 2, y + 5));
		addTextField(new GuiNpcTextField(1, this, x + 80, y, 60, 18, "" + marcet.updateTime)
				.setMinMaxDefault(0, 360, marcet.updateTime)
				.setHoverText("market.hover.set.update",
						Util.instance.ticksToElapsedTime(marcet.updateTime * 1200L, false, false, false)));
		if (marcet.updateTime >= 5) {
			y += 22;
			addButton(new GuiNpcButton(0, x, y, 170, 20, new String[] { "market.limited.0", "market.limited.1", "market.limited.2" }, marcet.limitedType)
					.setHoverText("market.hover.only.currency." + marcet.limitedType));
		}
		// tabs
		addLabel(new GuiNpcLabel(lID++, "gui.sections", x + 176, y - 17));
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(72, 60); }
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
		scroll.setUnsortedList(list)
				.setHoverTexts(hts);
		addScroll(scroll);
		// update message
		y += 22;
		addButton(new GuiNpcButton(1, x, y, 170, 20, "lines.title")
				.setHoverText("market.hover.message"));
		// isLimited
		y += 20;
		addButton(new GuiNpcCheckBox(2, x, y, 170, 18, "market.select.limited.true", "market.select.limited.false", marcet.isLimited)
				.setHoverText("market.hover.limited"));
		// show XP
		y += 20;
		addButton(new GuiNpcCheckBox(3, x, y, 170, 18, "market.select.show.xp.true", "market.select.show.xp.false", marcet.showXP)
				.setHoverText("market.hover.show.xp"));
		// add new section
		addButton(new GuiNpcButton(5, x + 175, y, 37, 20, "type.add")
				.setHoverText("market.hover.section.add"));
		// del section
		addButton(new GuiNpcButton(6, x + 213, y, 35, 20, "type.del")
				.setIsEnable(marcet.sections.size() > 1 && scroll.getSelect() > 0)
				.setHoverText("market.hover.section.del"));
		// levels
		y += 25;
		String[] values = new String[marcet.markup.size()];
		i = 0;
		for (int level : marcet.markup.keySet()) {
			values[i] = (new TextComponentTranslation("type.level")).getFormattedText() + " " + level;
			i++;
		}
		addLabel(new GuiNpcLabel(lID++, "gui.type", x + 2, y + 5));
		addButton(new GuiNpcButton(4, x + 22, y, 50, 20, values, level)
				.setHoverText("market.hover.extra.slot"));
		// extra markup
		MarkupData md = marcet.markup.get(level);
		if (md == null) {
			level = 0;
			if (!marcet.markup.containsKey(0)) { marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000)); }
			md = marcet.markup.get(level);
		}
		// buy
		addLabel(new GuiNpcLabel(lID++, "market.extra.markup", x + 76, y + 5));
		addLabel(new GuiNpcLabel(lID++, "%", x + 174, y + 5));
		addTextField(new GuiNpcTextField(2, this, x + 120, y, 50, 20, "" + (int) (md.buy * 100.0f))
				.setMinMaxDefault(-100, 500, (int) (md.buy * 100.0f))
				.setHoverText("market.hover.extra.buy"));
		// sell
		addLabel(new GuiNpcLabel(lID++, "%", x + 238, y + 5));
		addTextField(new GuiNpcTextField(3, this, x + 184, y, 50, 20, "" + (int) (md.sell * 100.0f))
				.setMinMaxDoubleDefault(-500, 100, (int) (md.sell * 100.0f))
				.setHoverText("market.hover.extra.sell"));
		// xp
		y += 22;
		addLabel(new GuiNpcLabel(lID, "quest.exp", x + 76, y + 5));
		addTextField(new GuiNpcTextField(4, this, x + 120, y, 50, 20, "" + md.xp)
				.setMinMaxDefault(0, Integer.MAX_VALUE, md.xp)
				.setHoverText("market.hover.xp"));
		// exit
		addButton(new GuiNpcButton(66, x, guiTop + ySize - 24, 60, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (getButton(6) != null) { getButton(6).setIsEnable(marcet.sections.size() > 1 && scroll.getSelect() > 0); }
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) { return; }
		setSubGui(new SubGuiEditText(2, scroll.getSelected()));
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) { return; }
			if (((SubGuiEditText) subgui).getId() == 1) {
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
					if (has) { name += "_"; }
				}
				MarcetSection ms = new MarcetSection(marcet.sections.size());
				ms.name = name;
				marcet.sections.put(ms.getId(), ms);
			} else if (((SubGuiEditText) subgui).getId() == 2) {
				if (!data.containsKey(scroll.getSelected())) { return; }
				String name = ((SubGuiEditText) subgui).text[0];
				int idSel = data.get(scroll.getSelected());
				boolean next = true;
				while (next) {
					next = false;
					for (int id : marcet.sections.keySet()) {
						if (id == idSel) { continue; }
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
	public void unFocused(GuiNpcTextField textField) {
		if (hasSubGui()) { return; }
		String text = textField.getText();
		MarkupData md = marcet.markup.get(level);
		switch (textField.getID()) {
			case 0: {
				if (text.equals(marcet.name)) { return; }
				marcet.name = text;
				initGui();
				break;
			}
			case 1: {
				int time = textField.getInteger();
				if (time < 5) { time = 0; }
				if (time > 360) { time = 360; }
				marcet.updateTime = time;
				initGui();
				break;
			}
			case 2: {
				if (md == null) { return; }
				md.buy = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				initGui();
				break;
			}
			case 3: {
				if (md == null) { return; }
				md.sell = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				initGui();
				break;
			}
			case 4: {
				if (md == null) { return; }
				md.xp = textField.getInteger();
				break;
			}
		}
	}

}
