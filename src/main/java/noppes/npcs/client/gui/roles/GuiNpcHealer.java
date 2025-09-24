package noppes.npcs.client.gui.roles;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.data.HealerSettings;

import javax.annotation.Nonnull;

public class GuiNpcHealer extends GuiNPCInterface2 implements ITextfieldListener, ICustomScrollListener {

	protected final Map<String, String> displays_0 = new TreeMap<>();
	protected final Map<String, String> displays_1 = new TreeMap<>(); // [display name, registry name] (0-options, 1-configured)
	protected final Map<String, Integer> potions = new TreeMap<>(); // [registry name, registry ID]
	protected GuiCustomScroll configured;
	protected GuiCustomScroll options;
	protected final JobHealer job;
	protected int range = 8;
	protected int speed = 10;
	protected int amplifier = 0;
	protected byte type = (byte) 2;

	public GuiNpcHealer(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		job = (JobHealer) npc.advanced.jobInterface;
		for (Potion p : Potion.REGISTRY) { potions.put(p.getName(), Potion.getIdFromPotion(p)); }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (!configured.hasSelected()) { return; }
				int id = potions.get(displays_1.get(configured.getSelected()));
				if (!job.effects.containsKey(id)) { return; }
				setSubGui(new SubGuiNpcJobHealerSettings(0, job.effects.get(id)));
				break;
			} // edit HealerSettings
			case 1: type = (byte) button.getValue(); break; // faction type
			case 11: {
				if (!options.hasSelected()) { return; }
				GuiNpcTextField.unfocus();
				int id = potions.get(displays_0.get(options.getSelected()));
				HealerSettings hs = new HealerSettings(id, range, speed, amplifier, type);
				job.effects.put(id, hs);
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			} // >
			case 12: {
				if (!configured.hasSelected()) { return; }
				job.effects.remove(potions.get(displays_1.get(configured.getSelected())));
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			} // <
			case 13: {
				GuiNpcTextField.unfocus();
				job.effects.clear();
				for (Potion p : Potion.REGISTRY) {
					int id = Potion.getIdFromPotion(p);
					HealerSettings hs = new HealerSettings(id, range, speed, amplifier, type);
					job.effects.put(id, hs);
				}
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			} // >>
			case 14: {
				job.effects.clear();
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			} // <<
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 14;
		if (options == null) { options = new GuiCustomScroll(this, 0).setSize(172, 154); }
		options.guiLeft = guiLeft + 4;
		options.guiTop = y;
		addScroll(options);
		addLabel(new GuiNpcLabel(11, "beacon.availableEffects", guiLeft + 4, y - 10));
		if (configured == null) { configured = new GuiCustomScroll(this, 1).setSize(172, 154); }
		configured.guiLeft = guiLeft + 238;
		configured.guiTop = y;
		addScroll(configured);
		addLabel(new GuiNpcLabel(12, "beacon.currentEffects", guiLeft + 235, y - 10));
		displays_0.clear();
		displays_1.clear();
		LinkedHashMap<Integer, List<String>> htsO = new LinkedHashMap<>();
		LinkedHashMap<Integer, List<String>> htsC = new LinkedHashMap<>();
		ITextComponent r = new TextComponentTranslation("gui.range");
		ITextComponent s = new TextComponentTranslation("gui.repeatable");
		ITextComponent b = new TextComponentTranslation("gui.blocks");
		ITextComponent c = new TextComponentTranslation("gui.sec");
		ITextComponent t = new TextComponentTranslation("gui.time");
		ITextComponent p = new TextComponentTranslation("beacon.amplifier");
		ITextComponent l = new TextComponentTranslation("parameter.level");
		ITextComponent j = new TextComponentTranslation("gui.type");
		ITextComponent u = new TextComponentTranslation("script.target");
		r.getStyle().setColor(TextFormatting.GRAY);
		s.getStyle().setColor(TextFormatting.GRAY);
		c.getStyle().setColor(TextFormatting.GRAY);
		b.getStyle().setColor(TextFormatting.GRAY);
		t.getStyle().setColor(TextFormatting.GRAY);
		p.getStyle().setColor(TextFormatting.GRAY);
		l.getStyle().setColor(TextFormatting.GRAY);
		j.getStyle().setColor(TextFormatting.GRAY);
		u.getStyle().setColor(TextFormatting.GRAY);
		for (String pointName : potions.keySet()) {
			int id = potions.get(pointName);
			Potion potion = Potion.getPotionById(id);
			String name = ((char) 167) + (potion == null ? "5" : potion.isBadEffect() ? "c" : "a") + new TextComponentTranslation(pointName).getFormattedText();
			if (!job.effects.containsKey(id)) { // has potion ID
				displays_0.put(name, pointName);
				htsO.put(htsO.size(), Collections.singletonList("ID: " + ((char) 167) + "6" + id));
			} else { // to setts
				HealerSettings hs = job.effects.get(id);
				String lv = "enchantment.level." + hs.amplifier;
				if (!new TextComponentTranslation(lv).getFormattedText().equals(lv)) { lv = new TextComponentTranslation(lv).getFormattedText(); }
				else { lv = "" + (hs.amplifier + 1); }
				displays_1.put(name + " " + lv, pointName);
				ITextComponent f = new TextComponentTranslation(hs.type == (byte) 0 ? "faction.friendly" : hs.type == (byte) 1 ? "faction.unfriendly" : "spawner.all");
				f.getStyle().setColor(hs.type == (byte) 0 ? TextFormatting.GREEN : TextFormatting.DARK_AQUA);
				ITextComponent h = new TextComponentTranslation(hs.isMassive ? "beacon.massive" : "beacon.not.massive");
				h.getStyle().setColor(hs.isMassive ? TextFormatting.DARK_PURPLE : TextFormatting.YELLOW);
				List<String> hovers = new ArrayList<>();
				hovers.add("ID: " + ((char) 167) + "6" + id);
				hovers.add(r.getFormattedText() + ((char) 167) + "7: "
						+ ((char) 167) + "e" + hs.range + " " + b.getFormattedText());
				hovers.add(s.getFormattedText()
						+ ((char) 167) + "7: " + ((char) 167) + "b" + (Math.round((double) hs.speed / 2.0d) / 10.0d)
						+ " " + c.getFormattedText());
				hovers.add(t.getFormattedText() + ((char) 167) + "7: "
						+ ((char) 167) + "a" + (Math.round((double) hs.time / 2.0d) / 10.0d) + " "
						+ c.getFormattedText());
				hovers.add(p.getFormattedText() + ((char) 167) + "7: " + ((char) 167)
						+ "c" + (hs.amplifier + 1) + " " + l.getFormattedText());
				hovers.add(j.getFormattedText() + ((char) 167) + "7: " + f.getFormattedText());
				hovers.add(u.getFormattedText() + ((char) 167) + "7: " + h.getFormattedText());
				htsC.put(htsC.size(), hovers);
			}
		}
		options.setUnsortedList(new ArrayList<>(displays_0.keySet())).setHoverTexts(htsO);
		configured.setUnsortedList(new ArrayList<>(displays_1.keySet())).setHoverTexts(htsC);
		ITextComponent toall = new TextComponentTranslation("beacon.hover.toall");
		addLabel(new GuiNpcLabel(1, "beacon.range", guiLeft + 10, (y += 156) + 5));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 80, y, 40, 20, range + "")
				.setMinMaxDefault(1, 64, 16)
				.setHoverText(new TextComponentTranslation("beacon.hover.dist").appendSibling(toall).getFormattedText()));
		addLabel(new GuiNpcLabel(2, "stats.speed", guiLeft + 140, y + 5));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 220, y, 40, 20, speed + "")
				.setMinMaxDefault(10, 72000, 20)
				.setHoverText(new TextComponentTranslation("beacon.hover.speed").appendSibling(toall).getFormattedText()));
		addLabel(new GuiNpcLabel(3, "beacon.affect", guiLeft + 10, (y += 22) + 5));
		addButton(new GuiNpcButton(1, guiLeft + 56, y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, type)
				.setHoverText(new TextComponentTranslation("beacon.hover.type").appendSibling(toall).getFormattedText()));
		addLabel(new GuiNpcLabel(4, "beacon.amplifier", guiLeft + 140, y + 5));
		String lv = "enchantment.level." + amplifier;
		if (!new TextComponentTranslation(lv).getFormattedText().equals(lv)) { lv = new TextComponentTranslation(lv).getFormattedText(); }
		else { lv = "" + (amplifier + 1); }
		addTextField(new GuiNpcTextField(3, this, guiLeft + 220, y, 40, 20, (amplifier + 1) + "")
				.setMinMaxDefault(1, 4, 1)
				.setHoverText(new TextComponentTranslation("beacon.hover.power", lv).appendSibling(toall).getFormattedText()));
		addButton(new GuiNpcButton(11, guiLeft + 177, (y -= 165), 61, 20, ">")
				.setIsEnable(options.hasSelected())
				.setHoverText("beacon.hover.add.element"));
		addButton(new GuiNpcButton(12, guiLeft + 177, (y += 22), 61, 20, "<")
				.setIsEnable(configured.hasSelected())
				.setHoverText("beacon.hover.del.element"));
		addButton(new GuiNpcButton(13, guiLeft + 177, (y += 44), 61, 20, ">>")
				.setIsEnable(!displays_0.isEmpty())
				.setHoverText("beacon.hover.add.all.element"));
		addButton(new GuiNpcButton(14, guiLeft + 177, (y += 22), 61, 20, "<<")
				.setIsEnable(!displays_1.isEmpty())
				.setHoverText("hover.del.all.element"));
		addButton(new GuiNpcButton(0, guiLeft + 177, y + 33, 61, 20, "gui.edit")
				.setIsEnable(configured.hasSelected())
				.setHoverText("beacon.hover.edit"));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.JobSave, job.save(new NBTTagCompound())); }

	// New from Unofficial (BetaZavr)
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNpcJobHealerSettings) || !configured.hasSelected()) { return; }
		int id = potions.get(displays_1.get(configured.getSelected()));
		job.effects.put(id, ((SubGuiNpcJobHealerSettings) subgui).healerSettings);
		initGui();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			if (!options.hasSelected()) { return; }
			GuiNpcTextField.unfocus();
			int id = potions.get(displays_0.get(options.getSelected()));
			HealerSettings hs = new HealerSettings(id, range, speed, amplifier, type);
			job.effects.put(id, hs);
			options.setSelect(-1);
			configured.setSelect(-1);
		} // >
		else if (scroll.getID() == 1) {
			if (!configured.hasSelected()) { return; }
			job.effects.remove(potions.get(displays_1.get(configured.getSelected())));
			options.setSelect(-1);
			configured.setSelect(-1);
		} // <
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			if (!options.hasSelected()) { return; }
			GuiNpcTextField.unfocus();
			int id = potions.get(displays_0.get(options.getSelected()));
			HealerSettings hs = new HealerSettings(id, range, speed, amplifier, type);
			job.effects.put(id, hs);
			options.setSelect(-1);
			configured.setSelect(-1);
			initGui();
		} else {
			if (!configured.hasSelected()) { return; }
			int id = potions.get(displays_1.get(configured.getSelected()));
			if (!job.effects.containsKey(id)) { return; }
			setSubGui(new SubGuiNpcJobHealerSettings(0, job.effects.get(id)));
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: range = textField.getInteger(); break;
			case 2: speed = textField.getInteger(); break;
			case 3: amplifier = textField.getInteger() - 1; break;
		}
	}

}
