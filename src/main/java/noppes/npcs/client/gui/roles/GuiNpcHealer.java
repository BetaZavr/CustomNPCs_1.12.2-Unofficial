package noppes.npcs.client.gui.roles;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcJobHealerSettings;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.data.HealerSettings;

public class GuiNpcHealer
extends GuiNPCInterface2
implements ISubGuiListener, ITextfieldListener, ICustomScrollListener {

	private final Map<String, String> displays_0 = new TreeMap<>();
    private final Map<String, String> displays_1 = new TreeMap<>(); // [display name, registry name] (0-options, 1-configured)
	private final Map<String, Integer> potions = new TreeMap<>(); // [registry name, registry ID]
	private final JobHealer job;
	private int range = 8;
	private int speed = 10;
	private int amplifier = 0;
	private byte type = (byte) 2;
	private GuiCustomScroll options, configured;

	public GuiNpcHealer(EntityNPCInterface npc) {
		super(npc);
		job = (JobHealer) npc.advanced.jobInterface;
		for (Potion p : Potion.REGISTRY) {
			potions.put(p.getName(), Potion.getIdFromPotion(p));
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 0: {
				if (!configured.hasSelected()) { return; }
				int id = potions.get(displays_1.get(configured.getSelected()));
				if (!job.effects.containsKey(id)) { return; }
				setSubGui(new SubGuiNpcJobHealerSettings(0, job.effects.get(id)));
				break;
			}
			case 1: {
				type = (byte) button.getValue();
				break;
			}
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
			}
			case 12: {
				if (!configured.hasSelected()) {
					return;
				}
				job.effects.remove(potions.get(displays_1.get(configured.getSelected())));
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			}
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
			}
			case 14: {
				job.effects.clear();
				options.setSelect(-1);
				configured.setSelect(-1);
				initGui();
				break;
			}
		}
	}

	@Override
	public void elementClicked() {
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 14;
		if (options == null) { (options = new GuiCustomScroll(this, 0)).setSize(172, 154); }
		options.guiLeft = guiLeft + 4;
		options.guiTop = y;
		addScroll(options);

		addLabel(new GuiNpcLabel(11, "beacon.availableEffects", guiLeft + 4, y - 10));
		if (configured == null) { (configured = new GuiCustomScroll(this, 1)).setSize(172, 154); }
		configured.guiLeft = guiLeft + 238;
		configured.guiTop = y;
		addScroll(configured);

		addLabel(new GuiNpcLabel(12, "beacon.currentEffects", guiLeft + 235, y - 10));
		displays_0.clear();
		displays_1.clear();
		List<String> h_0 = new ArrayList<>();
		List<String> h_1 = new ArrayList<>();
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
			String name = ((char) 167) + (potion == null ? "5" : potion.isBadEffect() ? "c" : "a")
					+ new TextComponentTranslation(pointName).getFormattedText();
			if (!job.effects.containsKey(id)) { // has potion ID
				displays_0.put(name, pointName);
				h_0.add("ID: " + ((char) 167) + "6" + id);
			} else { // to setts
				HealerSettings hs = job.effects.get(id);
				displays_1.put(
						name + " "
								+ new TextComponentTranslation("enchantment.level." + hs.amplifier).getFormattedText(),
						pointName);
				ITextComponent f = new TextComponentTranslation(hs.type == (byte) 0 ? "faction.friendly"
						: hs.type == (byte) 1 ? "faction.unfriendly" : "spawner.all");
				f.getStyle().setColor(hs.type == (byte) 0 ? TextFormatting.GREEN
						: TextFormatting.DARK_AQUA);
				ITextComponent h = new TextComponentTranslation(hs.isMassive ? "beacon.massive" : "beacon.not.massive");
				h.getStyle().setColor(hs.isMassive ? TextFormatting.DARK_PURPLE : TextFormatting.YELLOW);
				h_1.add("ID: " + ((char) 167) + "6" + id + "<br>" + r.getFormattedText() + ((char) 167) + "7: "
						+ ((char) 167) + "e" + hs.range + " " + b.getFormattedText() + "<br>" + s.getFormattedText()
						+ ((char) 167) + "7: " + ((char) 167) + "b" + (Math.round((double) hs.speed / 2.0d) / 10.0d)
						+ " " + c.getFormattedText() + "<br>" + t.getFormattedText() + ((char) 167) + "7: "
						+ ((char) 167) + "a" + (Math.round((double) hs.time / 2.0d) / 10.0d) + " "
						+ c.getFormattedText() + "<br>" + p.getFormattedText() + ((char) 167) + "7: " + ((char) 167)
						+ "c" + (hs.amplifier + 1) + " " + l.getFormattedText() + "<br>" + j.getFormattedText()
						+ ((char) 167) + "7: " + ((char) 167) + "c" + f.getFormattedText() + "<br>"
						+ u.getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "c" + h.getFormattedText());
			}
		}
		options.setListNotSorted(new ArrayList<>(displays_0.keySet()));
		LinkedHashMap<Integer, List<String>> htsO = new LinkedHashMap<>();
		int i = 0;
		for (String str : h_0) {
			htsO.put(i, Arrays.asList(str.split("<br>")));
			i++;
		}
		options.setHoverTexts(htsO);

		configured.setListNotSorted(new ArrayList<>(displays_1.keySet()));
		LinkedHashMap<Integer, List<String>> htsC = new LinkedHashMap<>();
		i = 0;
		for (String str : h_1) {
			htsC.put(i, Arrays.asList(str.split("<br>")));
			i++;
		}
		configured.setHoverTexts(htsC);

		ITextComponent toall = new TextComponentTranslation("beacon.hover.toall");
		y += 156;
		addLabel(new GuiNpcLabel(1, "beacon.range", guiLeft + 10, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 80, y, 40, 20, range + "");
		textField.setMinMaxDefault(1, 64, 16);
		textField.setHoverText(new TextComponentTranslation("beacon.hover.dist").appendSibling(toall).getFormattedText());
		addTextField(textField);

		addLabel(new GuiNpcLabel(2, "stats.speed", guiLeft + 140, y + 5));
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 220, y, 40, 20, speed + "");
		textField.setMinMaxDefault(10, 72000, 20);
		textField.setHoverText(new TextComponentTranslation("beacon.hover.speed").appendSibling(toall).getFormattedText());
		addTextField(textField);

		y += 22;
		addLabel(new GuiNpcLabel(3, "beacon.affect", guiLeft + 10, y + 5));
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 56, y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, type);
		button.setHoverText(new TextComponentTranslation("beacon.hover.type").appendSibling(toall).getFormattedText());
		addButton(button);

		addLabel(new GuiNpcLabel(4, "beacon.amplifier", guiLeft + 140, y + 5));
		textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 220, y, 40, 20, (amplifier + 1) + "");
		textField.setMinMaxDefault(1, 4, 1);
		textField.setHoverText(new TextComponentTranslation("beacon.hover.power", new TextComponentTranslation("enchantment.level." + amplifier).getFormattedText()).appendSibling(toall).getFormattedText());
		addTextField(textField);

		y -= 198;
		button = new GuiNpcButton(11, guiLeft + 177, (y += 33), 61, 20, ">");
		button.setEnabled(options.getSelect() != -1);
		button.setHoverText("beacon.hover.add");
		addButton(button);

		button = new GuiNpcButton(12, guiLeft + 177, (y += 22), 61, 20, "<");
		button.setEnabled(configured.getSelect() != -1);
		button.setHoverText("beacon.hover.del");
		addButton(button);

		button = new GuiNpcButton(13, guiLeft + 177, (y += 44), 61, 20, ">>");
		button.setEnabled(!displays_0.isEmpty());
		button.setHoverText("beacon.hover.add.all");
		addButton(button);

		button = new GuiNpcButton(14, guiLeft + 177, (y += 22), 61, 20, "<<");
		button.setEnabled(!displays_1.isEmpty());
		button.setHoverText("beacon.hover.del.all");
		addButton(button);

		button = new GuiNpcButton(0, guiLeft + 177, y + 33, 61, 20, "gui.edit");
		button.setEnabled(configured.getSelect() != -1);
		button.setHoverText("beacon.hover.edit");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		if (scroll.getId() == 0) {
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
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNpcJobHealerSettings) || !configured.hasSelected()) {
			return;
		}
		int id = potions.get(displays_1.get(configured.getSelected()));
		job.effects.put(id, ((SubGuiNpcJobHealerSettings) subgui).hs);
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		switch (textField.getId()) {
			case 1: {
				range = textField.getInteger();
				break;
			}
			case 2: {
				speed = textField.getInteger();
				break;
			}
			case 3: {
				amplifier = textField.getInteger() - 1;
				break;
			}
		}
	}

}
