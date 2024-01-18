package noppes.npcs.client.gui.global;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcFactionPoints;
import noppes.npcs.client.gui.SubGuiNpcFactionSelect;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCManageFactions
extends GuiNPCInterface2
implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, ISubGuiListener {
	
	private HashMap<String, Integer> base;
	private Map<String, Integer> data;
	private Faction faction;
	private GuiCustomScroll scrollFactions;
	public static boolean isName = true;

	public GuiNPCManageFactions(EntityNPCInterface npc) {
		super(npc);
		this.base = Maps.<String, Integer>newHashMap();
		this.data = Maps.<String, Integer>newLinkedHashMap();
		this.faction = new Faction();
		Client.sendData(EnumPacketServer.FactionsGet, new Object[0]);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				this.save();
				String name;
				for (name = new TextComponentTranslation("gui.new").getFormattedText(); this.data.containsKey(name); name += "_") { }
				Faction faction = new Faction(-1, name, 65280, 1000);
				NBTTagCompound compound = new NBTTagCompound();
				faction.writeNBT(compound);
				Client.sendData(EnumPacketServer.FactionSave, compound);
				break;
			}
			case 1: {
				if (!this.data.containsKey(this.scrollFactions.getSelected())) { return; }
				Client.sendData(EnumPacketServer.FactionRemove, this.data.get(this.scrollFactions.getSelected()));
				this.scrollFactions.clear();
				this.faction = new Faction();
				this.initGui();
				break;
			}
			case 2: {
				this.setSubGui(new SubGuiNpcFactionPoints(this.faction));
				break;
			}
			case 3: {
				this.faction.hideFaction = (button.getValue() == 1);
				break;
			}
			case 4: {
				this.faction.getsAttacked = (button.getValue() == 1);
				break;
			}
			case 5: {
				this.setSubGui(new SubGuiNpcFactionOptions(this.faction.factions));
				break;
			}
			case 6: {
				if (this.scrollFactions.getSelected() == null) { return; }
				HashMap<String, Integer> corData = Maps.<String, Integer>newHashMap();
				for (String name : this.base.keySet()) {
					int id = this.base.get(name);
					if (this.faction.id==id || this.faction.frendFactions.contains(id)) { continue; }
					corData.put(name, id);
				}
				this.setSubGui(new SubGuiNpcFactionSelect(6, this.scrollFactions.getSelected(), this.faction.attackFactions, corData));
				break;
			}
			case 7: {
				if (this.scrollFactions.getSelected() == null) { return; }
				HashMap<String, Integer> corData = Maps.<String, Integer>newHashMap();
				for (String name : this.base.keySet()) {
					int id = this.base.get(name);
					if (this.faction.id==id || this.faction.attackFactions.contains(id)) { continue; }
					corData.put(name, id);
				}
				this.setSubGui(new SubGuiNpcFactionSelect(7, this.scrollFactions.getSelected(), this.faction.frendFactions, corData));
				break;
			}
			case 10: {
				this.setSubGui(new SubGuiColorSelector(this.faction.color));
				break;
			}
			case 14: {
				GuiNPCManageFactions.isName = ((GuiNpcCheckBox) button).isSelected();
				((GuiNpcCheckBox) button).setText(GuiNPCManageFactions.isName ? "gui.name" : "ID");
				this.setData(new Vector<String>(this.base.keySet()), this.base);
				break;
			}
			default: { break; }
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 368, this.guiTop + 8, 45, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 368, this.guiTop + 32, 45, 20, "gui.remove"));
		if (this.scrollFactions == null) { (this.scrollFactions = new GuiCustomScroll(this, 0)).setSize(143, 208); }
		this.scrollFactions.guiLeft = this.guiLeft + 220;
		this.scrollFactions.guiTop = this.guiTop + 4;
		this.addScroll(this.scrollFactions);

		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(14, this.guiLeft + 368, this.guiTop + 56, 45, 12, GuiNPCManageFactions.isName ? "gui.name" : "ID");
		checkBox.setSelected(GuiNPCManageFactions.isName);
		this.addButton(checkBox);
		if (this.faction.id == -1) { return; }
		
		this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 40, this.guiTop + 4, 136, 20, AdditionalMethods.instance.deleteColor(this.faction.name)));
		this.getTextField(0).setMaxStringLength(50);
		
		this.addLabel(new GuiNpcLabel(0, "gui.name", this.guiLeft + 8, this.guiTop + 9));
		this.addLabel(new GuiNpcLabel(10, "ID", this.guiLeft + 178, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(11, this.faction.id + "", this.guiLeft + 178, this.guiTop + 14));
		String color;
		for (color = Integer.toHexString(this.faction.color); color.length() < 6; color = "0" + color) { }
		this.addButton(new GuiNpcButton(10, this.guiLeft + 40, this.guiTop + 26, 60, 20, color));
		
		this.addLabel(new GuiNpcLabel(1, "gui.color", this.guiLeft + 8, this.guiTop + 31));
		this.getButton(10).setTextColor(this.faction.color);
		int y = 48;
		this.addLabel(new GuiNpcLabel(2, "faction.points", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + y, 45, 20, "selectServer.edit"));
		y += 22;
		this.addLabel(new GuiNpcLabel(3, "faction.hidden", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 170, this.guiTop + y, 45, 20, new String[] { "gui.no", "gui.yes" }, (this.faction.hideFaction ? 1 : 0)));
		y += 22;
		this.addLabel(new GuiNpcLabel(4, "faction.attacked", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 170, this.guiTop + y, 45, 20, new String[] { "gui.no", "gui.yes" }, (this.faction.getsAttacked ? 1 : 0)));
		y += 22;
		this.addLabel(new GuiNpcLabel(5, "faction.ondeath", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 170, this.guiTop + y, 45, 20, "faction.points"));
		y += 32;
		this.addLabel(new GuiNpcLabel(6, "faction.hostiles", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 170, this.guiTop + y, 45, 20, "selectServer.edit"));
		y += 22;
		this.addLabel(new GuiNpcLabel(7, "faction.friends", this.guiLeft + 8, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 170, this.guiTop + y, 45, 20, "selectServer.edit"));
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) { // add new
			this.setHoverText(new TextComponentTranslation("faction.hover.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.del").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).visible && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.points").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).visible && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.hide").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).visible && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.mobs").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).visible && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.dead.points").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).visible && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.hostiles").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).visible && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.addfrends").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.color").getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.sort", new TextComponentTranslation("global.factions").getFormattedText(), ((GuiNpcCheckBox) this.getButton(14)).getText()).getFormattedText());
		}
	}
	
	@Override
	public void save() {
		if (this.scrollFactions==null ||
				this.scrollFactions.getSelected()==null ||
				!this.data.containsKey(this.scrollFactions.getSelected()) ||
				this.faction == null || this.faction.id == -1)
		{ return; }
		NBTTagCompound compound = new NBTTagCompound();
		this.faction.writeNBT(compound);
		Client.sendData(EnumPacketServer.FactionSave, compound);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			if (!this.data.containsKey(this.scrollFactions.getSelected())) { return; }
			this.save();
			Client.sendData(EnumPacketServer.FactionGet, this.data.get(this.scrollFactions.getSelected()));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.base = data;
		String select = this.scrollFactions==null || this.scrollFactions.getSelected() == null ? "" : this.scrollFactions.getSelected();
		this.data.clear();
		List<Entry<String, Integer>> newList = Lists.newArrayList(data.entrySet());
		Collections.sort(newList, new Comparator<Entry<String, Integer>>() {
	        public int compare(Entry<String, Integer> f_0, Entry<String, Integer> f_1) {
	        	if (GuiNPCManageFactions.isName) { return f_0.getKey().compareTo(f_1.getKey()); }
	        	else { return f_0.getValue().compareTo(f_1.getValue()); }
	        }
	    });
        for (Entry<String, Integer> entry : newList) {
        	int id = entry.getValue();
			String name = AdditionalMethods.instance.deleteColor(entry.getKey());
			if (name.indexOf("ID:"+id+" ")>=0) { name = name.substring(name.indexOf(" ")+3); }
			String key = ((char) 167)+"7ID:"+id+" "+((char) 167)+"r"+name;
        	this.data.put(key, id);
			if (select!=null && select.equals(key)) { select = key; }
        }
		this.scrollFactions.setListNotSorted(Lists.<String>newArrayList(this.data.keySet()));
		if (select!=null && !select.isEmpty()) { this.scrollFactions.setSelected(select); }
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		(this.faction = new Faction()).readNBT(compound);
		this.setSelected("ID:"+this.faction.id+" "+this.faction.name);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
		for (String key : this.scrollFactions.getList()) {
			if (AdditionalMethods.instance.deleteColor(key+"").equals(selected) && this.data.containsKey(key)) {
				this.scrollFactions.setSelected(key);
				return;
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector) {
			this.faction.color = ((SubGuiColorSelector) subgui).color;
			this.initGui();
		}
		else if (subgui instanceof SubGuiNpcFactionSelect) {
			SubGuiNpcFactionSelect gui = (SubGuiNpcFactionSelect) subgui;
			if (subgui.id==6) {
				this.faction.attackFactions.clear();
				for (int id : gui.selectFactions) {
					this.faction.frendFactions.remove(id);
					this.faction.attackFactions.add(id);
				}
			} else if (gui.id==7) {
				this.faction.frendFactions.clear();
				for (int id : gui.selectFactions) {
					this.faction.attackFactions.remove(id);
					this.faction.frendFactions.add(id);
				}
			}
			this.save();
			this.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if (this.faction.id == -1) {
			return;
		}
		if (guiNpcTextField.getId() == 0) {
			String name = guiNpcTextField.getText();
			if (!name.isEmpty() && !this.data.containsKey(name)) {
				String old = "" + this.scrollFactions.getSelected();
				this.data.remove(this.faction.name);
				this.base.remove(this.faction.name);
				this.faction.name = name;
				String str = ((char) 167)+"7ID:"+this.faction.id+" "+((char) 167)+"r"+name;
				this.data.put(str, this.faction.id);
				this.base.put(name, this.faction.id);
				this.scrollFactions.replace(old, str);
			}
			this.initGui();
		} else if (guiNpcTextField.getId() == 1) {
			int color = 0;
			try {
				color = Integer.parseInt(guiNpcTextField.getText(), 16);
			} catch (NumberFormatException e) {
				color = 0;
			}
			guiNpcTextField.setTextColor(this.faction.color = color);
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

}
