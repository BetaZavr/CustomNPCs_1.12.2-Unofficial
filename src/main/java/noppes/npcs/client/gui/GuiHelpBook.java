package noppes.npcs.client.gui;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.util.EventData;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.constants.EnumEventData;
import noppes.npcs.constants.EnumInterfaceData;
import noppes.npcs.util.ObfuscationHelper;

public class GuiHelpBook
extends GuiNPCInterface
implements ICustomScrollListener, ITextChangeListener {
	
	public static int activeTab = 0,  activeButton = 0;
	private static final Map<Integer, String> map = Maps.<Integer, String>newHashMap();
	private final String[] arr = new String [] { "config", "blocks", "items", "potions", "", "", "", "", "", "",
			"npc.display", "npc.stats", "npc.ais", "npc.inventory", "npc.advanced", "", "", "", "", "", 
			"script.main", "", "", "", "", "", "", "", "" };
	private GuiCustomScroll scroll;
	private String curentLang = "";
	private Map<String, MetodData> data = Maps.<String, MetodData>newHashMap();
	private char chr = ((char) 167);
	
	public GuiHelpBook() {
		this.drawDefaultBackground = true;
		this.closeOnEsc = true;
		this.xSize = 427;
		this.setBackground("menubg.png");
		
		String currentLanguage = ObfuscationHelper.getValue(LanguageManager.class, Minecraft.getMinecraft().getLanguageManager(), String.class);
		if (GuiHelpBook.map.isEmpty() || !this.curentLang.equals(currentLanguage)) {
			String wip = new TextComponentTranslation("gui.wip").getFormattedText();
			this.curentLang = currentLanguage;
			GuiHelpBook.map.clear();
			for (int i=0; i< this.arr.length; i++) {
				if (this.arr[i].isEmpty()) {
					GuiHelpBook.map.put(i, wip);
					continue;
				}
				String text = new TextComponentTranslation("help.info."+this.arr[i]).getFormattedText();
				if (text.isEmpty()) {
					GuiHelpBook.map.put(i, wip);
					continue;
				}
				while(text.indexOf("<br>")!=-1) { text = text.replaceAll("<br>", ""+((char) 10)); }
				GuiHelpBook.map.put(i, text);
			}
		}
	}
	
	@Override
	public void initGui() {
		this.xSize = (int) (this.width * 0.88d);
		this.ySize = (int) (this.xSize * 0.56);
		if (this.ySize > this.height * 0.95) {
			this.ySize = (int) (this.height * 0.95);
			this.xSize = (int) (this.ySize / 0.56);
		}
		this.bgScale = this.xSize / 400.0f;
		super.initGui();
		this.guiTop += 10;
		int yoffset = (int) (this.ySize * 0.02);
		
		GuiTextArea ta = new GuiTextArea(0, this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 108 - yoffset, (int) ((this.ySize * 0.96) - yoffset * 2), "");
		ta.enableCodeHighlighting();
		ta.setIsCode(false);
		ta.setListener(this);
		ta.onlyReading = true;
		this.add(ta);
		
		GuiMenuTopButton topButton;
		int pre = 0;
		
		int u = this.guiLeft + 4, v = this.guiTop - 17;
		for (int i = 0; i < 3 ; i++) {
			topButton = new GuiMenuTopButton(i, u + pre, v, "gui.help."+(i==0 ? "general" : i==1 ? "npc" : "scripts"));
			pre += topButton.getWidth();
			topButton.active = i == GuiHelpBook.activeTab;
			this.addTopButton(topButton);
		}
		u = this.guiLeft + ta.width + 8;
		v = this.guiTop - 12;
		int wb = this.xSize - ta.width + 6;
		switch(GuiHelpBook.activeTab) {
			case 1: { // npcEdit
				if (GuiHelpBook.activeButton<10 || GuiHelpBook.activeButton>19) { GuiHelpBook.activeButton = 10; }
				for (int i = 0; i < 5 ; i++) {
					String name;
					switch(i) {
						case 1: name = "menu.stats"; break;
						case 2: name = "menu.ai"; break;
						case 3: name = "menu.inventory"; break;
						case 4: name = "menu.advanced"; break;
						default: name = "menu.display"; break;
					}
					this.addButton(new GuiNpcButton(i + 10, u, v += 17, wb, 15, name));
				}
				break;
			}
			case 2: { // scripts
				if (GuiHelpBook.activeButton<20 || GuiHelpBook.activeButton>29) { GuiHelpBook.activeButton = 20; }
				for (int i = 0; i < 3 ; i++) {
					String name;
					switch(i) {
						case 1: name = "gui.help.api"; break;
						case 2: name = "gui.help.events"; break;
						default: name = "gui.help.general"; break;
					}
					this.addButton(new GuiNpcButton(i + 20, u, v += 17, wb, 15, name));
				}
				//scroll
				if (this.scroll==null) { this.scroll = new GuiCustomScroll(this, 0); }
				Map<String, String[]> m = Maps.<String, String[]>newTreeMap();
				if (GuiHelpBook.activeButton==21) {
					for (EnumInterfaceData enumID : EnumInterfaceData.values()) {
						List<String> com = enumID.it.getComment();
						m.put(enumID.name(), com.toArray(new String[com.size()]));
					}
				}
				else if (GuiHelpBook.activeButton==22) {
					for (EnumEventData enumED : EnumEventData.values()) {
						List<String> com = enumED.ed.getComment();
						m.put(enumED.name(), com.toArray(new String[com.size()]));
					}
				}
				this.scroll.setList(Lists.newArrayList(m.keySet()));
				this.scroll.hoversTexts = new String[m.size()][];
				int i = 0;
				for (String[] com : m.values()) {
					this.scroll.hoversTexts[i] = com;
					i++;
				}
				this.scroll.guiLeft = u;
				this.scroll.guiTop = (v += 17);
				
				this.scroll.setSize(wb, this.ySize - this.scroll.guiTop + this.guiTop - 15);
				if (this.scroll.selected<0) { this.scroll.selected = 0; }
				this.addScroll(this.scroll);
				break;
			}
			default: { // general
				if (GuiHelpBook.activeButton<0 || GuiHelpBook.activeButton>9) { GuiHelpBook.activeButton = 0; }
				for (int i = 0; i < 5 ; i++) {
					String name;
					switch(i) {
						case 1: name = "gui.help.blocks"; break;
						case 2: name = "gui.help.items"; break;
						case 3: name = "gui.help.potions"; break;
						default: name = "gui.help.config"; break;
					}
					this.addButton(new GuiNpcButton(i, u, v += 17, wb, 15, name));
				}
				break;	
			}
		}
		this.xSize = 420;
		this.ySize = 256;
		this.resetText();
	}

	private void resetText() {
		if (!(this.get(0) instanceof GuiTextArea)) { return; }
		if (GuiHelpBook.activeButton!=21 && GuiHelpBook.activeButton!=22) {
			String text = GuiHelpBook.map.get(GuiHelpBook.activeButton);
			if (text==null) { text = ""; }
			((GuiTextArea) this.get(0)).setText(text);
			((GuiTextArea) this.get(0)).scrolledLine = 0;
			return;
		}
		String text = "";
		if (GuiHelpBook.activeButton == 21) { // API
			if (this.scroll==null || EnumInterfaceData.get(this.scroll.getSelected())==null) {
				((GuiTextArea) this.get(0)).setText("");
				((GuiTextArea) this.get(0)).scrolledLine = 0;
				return;
			}
			InterfaseData intf = EnumInterfaceData.get(this.scroll.getSelected());
			List<MetodData> list = intf.getAllMetods(Lists.<MetodData>newArrayList());
			TreeMap<String, MetodData> m = Maps.<String, MetodData>newTreeMap();
			for (MetodData md : list) {
				String name = md.name;
				while (m.containsKey(name)) { name += "_"; }
				m.put(name, md);
			}
			this.data.clear();
			InterfaseData intfEx = intf;
			List<Class<?>> lc = Lists.newArrayList();
			while(true) {
				lc.add(0, intfEx.interF);
				if (intfEx.extend!=null) {
					intfEx = EnumInterfaceData.get(intfEx.extend.getSimpleName());
					if (intfEx==null) { break; }
				}
				else { break; }
			}
			text = new TextComponentTranslation("gui.interfase", ":").getFormattedText() + (""+((char) 10));
			for (int c = 0; c < lc.size(); c++) {
				int ofs = 1 + c;
				while(ofs>0) { text += "  "; ofs--; }
				text += chr + ((c == lc.size()-1) ? "6" : "7") + lc.get(c).getName() + (""+((char) 10));
			}
			if (intf.wraper!=null && intf.wraper.length>0) {
				text += (""+((char) 10)) + new TextComponentTranslation("interfase.classes").getFormattedText() + (""+((char) 10));
				for (Class<?> c : intf.wraper) {
					text += "  " + chr + "e" + c.getName() + (""+((char) 10));
				}
			}
			text += (""+((char) 10)) + new TextComponentTranslation("interfase.methods").getFormattedText() + (""+((char) 10));
			for (String name : m.keySet()) {
				String key = m.get(name).getText();
				text += key+(""+((char) 10));
				this.data.put(key, m.get(name));
			}
		}
		else if (GuiHelpBook.activeButton==22) { // Events
			if (this.scroll==null || EnumEventData.get(this.scroll.getSelected())==null) {
				((GuiTextArea) this.get(0)).setText("");
				((GuiTextArea) this.get(0)).scrolledLine = 0;
				return;
			}
			EventData evd = EnumEventData.get(this.scroll.getSelected());
			List<MetodData> list = evd.getAllMetods(Lists.<MetodData>newArrayList());
			TreeMap<String, MetodData> m = Maps.<String, MetodData>newTreeMap();
			for (MetodData md : list) {
				String name = md.name;
				while (m.containsKey(name)) { name += "_"; }
				m.put(name, md);
			}
			this.data.clear();
			EventData evdEx = evd;
			List<String> lc = Lists.<String>newArrayList();
			Class<?> sub = evdEx.event;
			while(sub.getSuperclass()!=null && sub.getSuperclass()!=Event.class) {
				lc.add(0, sub.getName().replace("$", "."));
				sub = sub.getSuperclass();
			}
			text = new TextComponentTranslation("gui.event", ":").getFormattedText() + (""+((char) 10));
			int i = 1;
			for (String name : lc) {
				int ofs = 1 + i;
				while(ofs>0) { text += "  "; ofs--; }
				text += chr + ((i == lc.size()) ? "6" : "7") + name + (""+((char) 10));
				i++;
			}
			String comm = ""; 
			for (String str : evd.getComment()) {
				if (!comm.isEmpty()) { comm = ""+((char) 10); }
				comm += str;
			}
			text += (""+((char) 10)) + comm + (""+((char) 10)) + 
					(""+((char) 10)) + new TextComponentTranslation("event.fields").getFormattedText() + (""+((char) 10));
			for (String name : m.keySet()) {
				MetodData md = m.get(name);
				String ret = md.returnType.getName();
				if (ret.indexOf("[L")==0) { ret = ret.replace("[L", "").replace(";", "") + "[]"; }
				else if (ret.indexOf("[B")==0) { ret = ret.replace("[B", "").replace(";", "") + "[]"; }
				String key = chr + "7 event." + chr + "6" + md.name + chr + "7;   // " + ret;
				text += key+(""+((char) 10)); 
				this.data.put(key, m.get(name));
			}
			if (evd.event.getMethods().length>0) {
				text += (""+((char) 10)) + new TextComponentTranslation("event.methods").getFormattedText() + (""+((char) 10));
				TreeMap<String, MetodData> md = Maps.<String, MetodData>newTreeMap();
				for (Method mtd : evd.event.getMethods()) {
					MetodData mdata = new MetodData(mtd.getReturnType(), mtd.getName(), "method.event."+mtd.getName().toLowerCase());
					if (mtd.getParameterCount()>0) {
						int g = 0;
						for (Parameter p : mtd.getParameters()) {
							mdata.parameters.add(new ParameterData(p.getType(), p.getType().getSimpleName().toLowerCase() + g, "parameter.event."+mtd.getName().toLowerCase()+"."+p.getType().getName().toLowerCase() + g));
							g++;
						}
					}
					mdata.isVarielble = true;
					md.put(mtd.getName(), mdata);
				}
				for (String name : md.keySet()) {
					String key = md.get(name).getText();
					text += key+(""+((char) 10));
					this.data.put(key, md.get(name));
				}
			}
		}
		((GuiTextArea) this.get(0)).setText(text);
		((GuiTextArea) this.get(0)).scrolledLine = 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.getButton(GuiHelpBook.activeButton)!=null) {
			this.getButton(GuiHelpBook.activeButton).layerColor = 0xFF00FF00;
		}
		if (this.scroll!=null && this.get(0) instanceof GuiTextArea) { 
			GuiTextArea area = (GuiTextArea) this.get(0);
			if (area == null || !area.hovered) { return; }
			int pos = area.getSelectionPos(mouseX, mouseY);
			Object[] sel = area.getSelectionText(mouseX, mouseY);
			if (GuiHelpBook.activeButton==21) { // API
				if (sel==null) { return; }
				if (sel!=null && !((String) sel[1]).isEmpty() && !((String) sel[1]).equals(this.scroll.getSelected())) {
					InterfaseData intf = EnumInterfaceData.get((String) sel[1]);
					if (intf!=null) {
						this.drawHoveringText(Lists.<String>newArrayList(new TextComponentTranslation("interfase.next", chr + "9" + chr + "l" + chr + "o" +(String) sel[1]).getFormattedText()), mouseX, mouseY, this.fontRenderer);
						this.hoverText = null;
						return;
					}
				}
				if (pos<0) { return; }
				String text = area.getText(), ent = (""+((char) 10));
				int start = 1+(pos==0 ? 0 : text.lastIndexOf(ent, pos));
				int end = pos==text.length()||text.indexOf(ent, pos)<0 ? text.length() : text.indexOf(ent, pos);
				if (start<0) { start = 0; }
				if (end<0) { end = text.length(); }
				if (start>=end) { return; }
				String metodText = text.substring(start, end);
				if (this.data.containsKey(metodText)) {
					this.drawHoveringText(this.data.get(metodText).getComment(), mouseX, mouseY, this.fontRenderer);
					this.hoverText = null;
				}
			}
			else if (GuiHelpBook.activeButton==22) { // Events
				if (pos<0) { return; }
				String text = area.getText(), ent = (""+((char) 10));
				int start = 1+(pos==0 ? 0 : text.lastIndexOf(ent, pos));
				int end = pos==text.length()||text.indexOf(ent, pos)<0 ? text.length() : text.indexOf(ent, pos);
				if (start<0) { start = 0; }
				if (end<0) { end = text.length(); }
				if (start>=end) { return; }
				String metodText = text.substring(start, end);
				if (this.data.containsKey(metodText)) {
					this.drawHoveringText(this.data.get(metodText).getComment(), mouseX, mouseY, this.fontRenderer);
					this.hoverText = null;
				}
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button instanceof GuiMenuTopButton) { GuiHelpBook.activeTab = button.id; }
		else { GuiHelpBook.activeButton = button.id; }
		this.initGui();
	}
	
	@Override
	public void save() { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.resetText();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.scroll==null || !(this.get(0) instanceof GuiTextArea)) { return; }
		GuiTextArea area = (GuiTextArea) this.get(0);
		if (!area.hovered) { return; }
		try {
			Object[] select = area.getSelectionText(this.mouseX, this.mouseY);
			if (GuiHelpBook.activeButton == 21) {
				if (!this.scroll.getSelected().equals(select[1]) && EnumInterfaceData.get((String) select[1])!=null) {
					this.scroll.setSelected((String) select[1]);
					this.resetText();
					return;
				}
			}
			else if (GuiHelpBook.activeButton == 22) {
				
			}
		}
		catch (Exception e) { }
	}

	@Override
	public void textUpdate(String text) { }
	
}
