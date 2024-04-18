package noppes.npcs.client.gui.roles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcJobHealerSettings;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.data.HealerSettings;

public class GuiNpcHealer
extends GuiNPCInterface2
implements ISubGuiListener, ITextfieldListener, ICustomScrollListener {
	
	private Map<String, String> displays_0, displays_1; // [display name, registry name] (0-options, 1-configured)
	private Map<String, Integer> potions; // [registry name, registry ID]
	private JobHealer job;
	private int range, speed, amplifier;
	private byte type;
	private GuiCustomScroll options, configured;

	public GuiNpcHealer(EntityNPCInterface npc) {
		super(npc);
		this.range = 8;
		this.speed = 10;
		this.amplifier = 0;
		this.type = (byte) 2;
		this.job = (JobHealer) npc.advanced.jobInterface;
		this.potions = Maps.<String, Integer>newTreeMap();
		this.displays_0 = Maps.<String, String>newTreeMap();
		this.displays_1 = Maps.<String, String>newTreeMap();
		for (Potion p : Potion.REGISTRY) {
			this.potions.put(p.getName(), Potion.getIdFromPotion(p));
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				if (!this.configured.hasSelected()) { return; }
				int id = this.potions.get(this.displays_1.get(this.configured.getSelected()));
				if (!this.job.effects.containsKey(id)) { return; }
				this.setSubGui(new SubGuiNpcJobHealerSettings(0, this.job.effects.get(id)));
				break;
			}
			case 1: {
				this.type = (byte) button.getValue();
				break;
			}
			case 11: {
				if (!this.options.hasSelected()) { return; }
				GuiNpcTextField.unfocus();
				int id = this.potions.get(this.displays_0.get(this.options.getSelected()));
				HealerSettings hs = new HealerSettings(id, this.range, this.speed, this.amplifier, this.type);
				this.job.effects.put(id, hs);
				this.options.selected = -1;
				this.configured.selected = -1;
				this.initGui();
				break;
			}
			case 12: {
				if (!this.configured.hasSelected()) { return; }
				this.job.effects.remove(this.potions.get(this.displays_1.get(this.configured.getSelected())));
				this.options.selected = -1;
				this.configured.selected = -1;
				this.initGui();
				break;
			}
			case 13: {
				GuiNpcTextField.unfocus();
				this.job.effects.clear();
				for (Potion p : Potion.REGISTRY) {
					int id = Potion.getIdFromPotion(p);
					HealerSettings hs = new HealerSettings(id, this.range, this.speed, this.amplifier, this.type);
					this.job.effects.put(id, hs);
				}
				this.options.selected = -1;
				this.configured.selected = -1;
				this.initGui();
				break;
			}
			case 14: {
				this.job.effects.clear();
				this.options.selected = -1;
				this.configured.selected = -1;
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void elementClicked() { }

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 14;
		if (this.options == null) {
			(this.options = new GuiCustomScroll(this, 0)).setSize(172, 154);
		}
		this.options.guiLeft = this.guiLeft + 4;
		this.options.guiTop = y;
		this.addScroll(this.options);
		
		this.addLabel(new GuiNpcLabel(11, "beacon.availableEffects", this.guiLeft + 4, y - 10));
		if (this.configured == null) {
			(this.configured = new GuiCustomScroll(this, 1)).setSize(172, 154);
		}
		this.configured.guiLeft = this.guiLeft + 238;
		this.configured.guiTop = y;
		this.addScroll(this.configured);
		
		this.addLabel(new GuiNpcLabel(12, "beacon.currentEffects", this.guiLeft + 235, y - 10));
		this.displays_0.clear();
		this.displays_1.clear();
		List<String> h_0 = Lists.<String>newArrayList(), h_1 = Lists.<String>newArrayList();
		ITextComponent r = new TextComponentTranslation("gui.range");
		ITextComponent s = new TextComponentTranslation("gui.repeatable");
		ITextComponent b = new TextComponentTranslation("gui.blocks");
		ITextComponent ñ = new TextComponentTranslation("gui.sec");
		ITextComponent t = new TextComponentTranslation("gui.time");
		ITextComponent p = new TextComponentTranslation("beacon.amplifier");
		ITextComponent l = new TextComponentTranslation("parameter.level");
		ITextComponent j = new TextComponentTranslation("gui.type");
		ITextComponent u = new TextComponentTranslation("script.target");
		r.getStyle().setColor(TextFormatting.GRAY);
		s.getStyle().setColor(TextFormatting.GRAY);
		ñ.getStyle().setColor(TextFormatting.GRAY);
		b.getStyle().setColor(TextFormatting.GRAY);
		t.getStyle().setColor(TextFormatting.GRAY);
		p.getStyle().setColor(TextFormatting.GRAY);
		l.getStyle().setColor(TextFormatting.GRAY);
		j.getStyle().setColor(TextFormatting.GRAY);
		u.getStyle().setColor(TextFormatting.GRAY);
		
		for (String potinName : this.potions.keySet()) {
			int id = this.potions.get(potinName);
			Potion potion = Potion.getPotionById(id);
			String name = ((char) 167) + (potion==null ? "5" : potion.isBadEffect() ? "c" : "a") + new TextComponentTranslation(potinName).getFormattedText();
			if (!this.job.effects.containsKey(id)) { // has potion ID
				this.displays_0.put(name, potinName);
				h_0.add("ID: "+((char) 167)+"6"+id);
			} else { // to setts
				HealerSettings hs = this.job.effects.get(id);
				this.displays_1.put(name + " " + new TextComponentTranslation("enchantment.level." + hs.amplifier).getFormattedText(), potinName);
				ITextComponent f = new TextComponentTranslation(hs.type==(byte) 0 ? "faction.friendly" : hs.type==(byte) 1 ? "faction.unfriendly" : "spawner.all");
				f.getStyle().setColor(hs.type==(byte) 0 ? TextFormatting.GREEN : hs.type==(byte) 0 ? TextFormatting.RED : TextFormatting.DARK_AQUA);
				ITextComponent h = new TextComponentTranslation(hs.isMassive ? "beacon.massive" : "beacon.not.massive");
				h.getStyle().setColor(hs.isMassive ? TextFormatting.DARK_PURPLE : TextFormatting.YELLOW);
				h_1.add("ID: "+((char) 167)+"6"+id+"<br>"+
						r.getFormattedText()+((char) 167)+"7: "+((char) 167)+"e"+hs.range+" "+b.getFormattedText()+"<br>"+
						s.getFormattedText()+((char) 167)+"7: "+((char) 167)+"b"+(Math.round((double) hs.speed / 2.0d)/10.0d)+" "+ñ.getFormattedText()+"<br>"+
						t.getFormattedText()+((char) 167)+"7: "+((char) 167)+"a"+(Math.round((double) hs.time / 2.0d)/10.0d)+" "+ñ.getFormattedText()+"<br>"+
						p.getFormattedText()+((char) 167)+"7: "+((char) 167)+"c"+(hs.amplifier+1)+" "+l.getFormattedText()+"<br>"+
						j.getFormattedText()+((char) 167)+"7: "+((char) 167)+"c"+f.getFormattedText()+"<br>"+
						u.getFormattedText()+((char) 167)+"7: "+((char) 167)+"c"+h.getFormattedText());
			}
		}
		this.options.setListNotSorted(Lists.newArrayList(this.displays_0.keySet()));
		this.options.hoversTexts = new String[h_0.size()][];
		int i = 0;
		for (String str : h_0) { this.options.hoversTexts[i] = str.split("<br>"); i++; }
		this.configured.setListNotSorted(Lists.newArrayList(this.displays_1.keySet()));
		this.configured.hoversTexts = new String[h_1.size()][];
		i = 0;
		for (String str : h_1) { this.configured.hoversTexts[i] = str.split("<br>"); i++; }
		
		y += 156;
		this.addLabel(new GuiNpcLabel(1, "beacon.range", this.guiLeft + 10, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 80, y, 40, 20, this.range + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, 64, 16);
		
		this.addLabel(new GuiNpcLabel(2, "stats.speed", this.guiLeft + 140, y + 5));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 220, y, 40, 20, this.speed + ""));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(10, 72000, 20);
		y += 22;
		this.addLabel(new GuiNpcLabel(3, "beacon.affect", this.guiLeft + 10, y + 5));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 56, y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, this.type));
		
		this.addLabel(new GuiNpcLabel(4, "beacon.amplifier", this.guiLeft + 140, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 220, y, 40, 20, (this.amplifier+1) + ""));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(1, 4, 1);
		y -= 198;
		this.addButton(new GuiNpcButton(11, this.guiLeft + 177, (y += 33), 61, 20, ">"));
		this.getButton(11).enabled = this.options.selected!=-1;
		this.addButton(new GuiNpcButton(12, this.guiLeft + 177, (y += 22), 61, 20, "<"));
		this.getButton(12).enabled = this.configured.selected!=-1;
		this.addButton(new GuiNpcButton(13, this.guiLeft + 177, (y += 44), 61, 20, ">>"));
		this.getButton(13).enabled = !this.displays_0.isEmpty();
		this.addButton(new GuiNpcButton(14, this.guiLeft + 177, (y += 22), 61, 20, "<<"));
		this.getButton(14).enabled = !this.displays_1.isEmpty();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 177, (y += 33), 61, 20, "gui.edit"));
		this.getButton(0).enabled = this.configured.selected!=-1;
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (scroll.id==0) {
			if (!this.options.hasSelected()) { return; }
			GuiNpcTextField.unfocus();
			int id = this.potions.get(this.displays_0.get(this.options.getSelected()));
			HealerSettings hs = new HealerSettings(id, this.range, this.speed, this.amplifier, this.type);
			this.job.effects.put(id, hs);
			this.options.selected = -1;
			this.configured.selected = -1;
			this.initGui();
		}
		else {
			if (!this.configured.hasSelected()) { return; }
			int id = this.potions.get(this.displays_1.get(this.configured.getSelected()));
			if (!this.job.effects.containsKey(id)) { return; }
			this.setSubGui(new SubGuiNpcJobHealerSettings(0, this.job.effects.get(id)));
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getId()) {
			case 1: { this.range = textField.getInteger(); break; }
			case 2: { this.speed = textField.getInteger(); break; }
			case 3: { this.amplifier = textField.getInteger() - 1; break; }
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions && this.subgui == null) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.edit").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.type").appendSibling(new TextComponentTranslation("beacon.hover.toall")).getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.add").getFormattedText());
		} else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.del").getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.add.all").getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.del.all").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.dist").appendSibling(new TextComponentTranslation("beacon.hover.toall")).getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.speed").appendSibling(new TextComponentTranslation("beacon.hover.toall")).getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			int p = this.amplifier;
			if (this.getTextField(3).isInteger()) { p = this.getTextField(3).getInteger() - 1; }
			this.setHoverText(new TextComponentTranslation("beacon.hover.power", new TextComponentTranslation("enchantment.level." + p).getFormattedText()).appendSibling(new TextComponentTranslation("beacon.hover.toall")).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNpcJobHealerSettings) || !this.configured.hasSelected()) { return; }
		int id = this.potions.get(this.displays_1.get(this.configured.getSelected()));
		this.job.effects.put(id, ((SubGuiNpcJobHealerSettings) subgui).hs);
		this.initGui();
	}
	
}
