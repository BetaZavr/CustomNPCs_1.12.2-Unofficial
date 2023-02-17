package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;

public class GuiNpcHealer extends GuiNPCInterface2 {
	private HashMap<String, String> displays;
	private JobHealer job;
	private int potency;
	private HashMap<String, Integer> potions;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;

	public GuiNpcHealer(EntityNPCInterface npc) {
		super(npc);
		this.potency = 0;
		this.job = (JobHealer) npc.jobInterface;
		this.potions = new HashMap<String, Integer>();
		this.displays = new HashMap<String, String>();
		for (Potion p : Potion.REGISTRY) {
			this.potions.put(p.getName(), Potion.getIdFromPotion(p));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 3) {
			this.job.type = (byte) button.getValue();
		}
		if (button.id == 11 && this.scroll1.hasSelected()) {
			this.job.effects.put(this.potions.get(this.scroll1.getSelected()), this.getTextField(2).getInteger());
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 12 && this.scroll2.hasSelected()) {
			this.job.effects.remove(this.potions.get(this.displays.remove(this.scroll2.getSelected())));
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 13) {
			this.job.effects.clear();
			for (Potion p : Potion.REGISTRY) {
				this.job.effects.put(Potion.getIdFromPotion(p), this.potency);
			}
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 14) {
			this.job.effects.clear();
			this.displays.clear();
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
	}

	@Override
	public void elementClicked() {
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "beacon.range", this.guiLeft + 10, this.guiTop + 9));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 4, 40, 20,
				this.job.range + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(1, 64, 16);
		this.addLabel(new GuiNpcLabel(4, "stats.speed", this.guiLeft + 140, this.guiTop + 9));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 220, this.guiTop + 4, 40, 20,
				this.potency + ""));
		this.getTextField(4).numbersOnly = true;
		this.getTextField(4).setMinMaxDefault(10, Integer.MAX_VALUE, 20);
		this.addLabel(new GuiNpcLabel(3, "beacon.affect", this.guiLeft + 10, this.guiTop + 31));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 56, this.guiTop + 26, 80, 20,
				new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, this.job.type));
		this.addLabel(new GuiNpcLabel(2, "beacon.potency", this.guiLeft + 140, this.guiTop + 31));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 220, this.guiTop + 26, 40, 20,
				this.potency + ""));
		this.getTextField(2).numbersOnly = true;
		this.getTextField(2).setMinMaxDefault(0, 3, 0);
		if (this.scroll1 == null) {
			(this.scroll1 = new GuiCustomScroll(this, 0)).setSize(175, 154);
		}
		this.scroll1.guiLeft = this.guiLeft + 4;
		this.scroll1.guiTop = this.guiTop + 58;
		this.addScroll(this.scroll1);
		this.addLabel(new GuiNpcLabel(11, "beacon.availableEffects", this.guiLeft + 4, this.guiTop + 48));
		if (this.scroll2 == null) {
			(this.scroll2 = new GuiCustomScroll(this, 1)).setSize(175, 154);
		}
		this.scroll2.guiLeft = this.guiLeft + 235;
		this.scroll2.guiTop = this.guiTop + 58;
		this.addScroll(this.scroll2);
		this.addLabel(new GuiNpcLabel(12, "beacon.currentEffects", this.guiLeft + 235, this.guiTop + 48));
		List<String> all = new ArrayList<String>();
		for (String names : this.potions.keySet()) {
			if (!this.job.effects.containsKey(this.potions.get(names))) {
				all.add(names);
			} else {
				this.displays.put(I18n.format(names, new Object[0]) + " " + I18n.format(
						"enchantment.level." + (this.job.effects.get(this.potions.get(names)) + 1), new Object[0]),
						names);
			}
		}
		this.scroll1.setList(all);
		List<String> applied = new ArrayList<String>(this.displays.keySet());
		this.scroll2.setList(applied);
		this.addButton(new GuiNpcButton(11, this.guiLeft + 180, this.guiTop + 80, 55, 20, ">"));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 180, this.guiTop + 102, 55, 20, "<"));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 180, this.guiTop + 130, 55, 20, ">>"));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 180, this.guiTop + 152, 55, 20, "<<"));
	}

	@Override
	public void save() {
		this.job.range = this.getTextField(1).getInteger();
		this.potency = this.getTextField(2).getInteger();
		this.job.speed = this.getTextField(4).getInteger();
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}
}
