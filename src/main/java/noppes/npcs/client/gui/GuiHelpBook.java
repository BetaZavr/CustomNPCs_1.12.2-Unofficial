package noppes.npcs.client.gui;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
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
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.constants.ParticleType;
import noppes.npcs.api.constants.PotionEffectType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.constants.SideType;
import noppes.npcs.api.constants.TacticalType;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.util.EventData;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MethodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.constants.EnumEventData;
import noppes.npcs.constants.EnumInterfaceData;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
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
	private Map<String, MethodData> data = Maps.<String, MethodData>newHashMap();
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
		u = this.guiLeft + ta.width + 10;
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
				for (int i = 0; i < 4 ; i++) {
					String name;
					switch(i) {
						case 1: name = "gui.help.api"; break;
						case 2: name = "gui.help.events"; break;
						case 3: name = "gui.help.constants"; break;
						default: name = "gui.help.general"; break;
					}
					this.addButton(new GuiNpcButton(i + 20, u, v += 17, wb, 15, name));
				}
				//scroll
				if (this.scroll==null) { this.scroll = new GuiCustomScroll(this, 0); }
				Map<String, String[]> m = Maps.<String, String[]>newTreeMap();
				List<String> keys = Lists.<String>newArrayList();
				if (GuiHelpBook.activeButton==21) {
					for (EnumInterfaceData enumID : EnumInterfaceData.values()) {
						List<String> com = enumID.it.getComment();
						m.put(enumID.name(), com.toArray(new String[com.size()]));
						keys.add(enumID.name());
					}
				}
				else if (GuiHelpBook.activeButton==22) {
					for (EnumEventData enumED : EnumEventData.values()) {
						List<String> com = enumED.ed.getComment();
						m.put(enumED.name(), com.toArray(new String[com.size()]));
						keys.add(enumED.name());
					}
				}
				else if (GuiHelpBook.activeButton==23) {
					for (String c : ScriptContainer.Data.keySet()) {
						Object value = ScriptContainer.Data.get(c);
						List<String> com = Lists.newArrayList();
						if (value instanceof Boolean || 
								value instanceof Byte || 
								value instanceof Short || 
								value instanceof Integer || 
								value instanceof Float || 
								value instanceof Double || 
								value instanceof Long || 
								value instanceof String || 
								value instanceof Class) { com.add(" = " + com.toString()); }
						else { com.add("Object: " + com.getClass().getName()); }
						m.put(c, new String[] { "" + ScriptContainer.Data.get(c) });
						keys.add(c);
					}
				}
				Collections.sort(keys);
				this.scroll.setListNotSorted(keys);
				this.scroll.hoversTexts = new String[keys.size()][];
				int i = 0;
				for (String key : keys) {
					this.scroll.hoversTexts[i] = m.get(key);
					i++;
				}
				this.scroll.guiLeft = u;
				this.scroll.guiTop = (v += 17);
				
				this.scroll.setSize(wb, this.ySize - this.scroll.guiTop + this.guiTop - 17);
				if (this.scroll.selected<0) { this.scroll.selected = 0; }
				this.addScroll(this.scroll);
				break;
			}
			default: { // general
				if (GuiHelpBook.activeButton<0 || GuiHelpBook.activeButton>9) { GuiHelpBook.activeButton = 0; }
				for (int i = 0; i < 4 ; i++) {
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
		GuiTextArea area = (GuiTextArea) this.get(0);
		if (GuiHelpBook.activeButton!=21 && GuiHelpBook.activeButton!=22 && GuiHelpBook.activeButton!=23) {
			String text = GuiHelpBook.map.get(GuiHelpBook.activeButton);
			if (text==null) { text = new TextComponentTranslation("gui.wip").getFormattedText(); }
			area.setText(text);
			area.setCursorPosition(0);
			area.scrolledLine = 0;
			return;
		}
		String text = "";
		if (GuiHelpBook.activeButton == 21) { // API
			if (this.scroll==null || EnumInterfaceData.get(this.scroll.getSelected())==null) {
				area.setText(text);
				area.setCursorPosition(0);
				area.scrolledLine = 0;
				return;
			}
			InterfaseData intf = EnumInterfaceData.get(this.scroll.getSelected());
			List<MethodData> list = intf.getAllMetods(Lists.<MethodData>newArrayList());
			TreeMap<String, MethodData> m = Maps.<String, MethodData>newTreeMap();
			for (MethodData md : list) {
				String name = md.name;
				while (m.containsKey(name)) { name += "_"; }
				m.put(name, md);
			}
			this.data.clear();
			InterfaseData intfEx = intf;
			List<Class<?>> lc = Lists.newArrayList();
			while(true) {
				lc.add(0, intfEx.interfaseClass);
				if (intfEx.extendClass!=null) {
					intfEx = EnumInterfaceData.get(intfEx.extendClass.getSimpleName());
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
			if (intf.wraperClass!=null && intf.wraperClass.length>0) {
				text += (""+((char) 10)) + new TextComponentTranslation("interfase.classes").getFormattedText() + (""+((char) 10));
				for (Class<?> c : intf.wraperClass) {
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
				area.setText(text);
				area.setCursorPosition(0);
				area.scrolledLine = 0;
				return;
			}
			EventData evd = EnumEventData.get(this.scroll.getSelected());
			List<MethodData> list = evd.getAllMetods(Lists.<MethodData>newArrayList());
			TreeMap<String, MethodData> m = Maps.<String, MethodData>newTreeMap();
			for (MethodData md : list) {
				String name = md.name;
				md.isVarielble = true;
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
			String comm = ""; 
			for (String str : evd.getComment()) {
				if (!comm.isEmpty()) { comm = ""+((char) 10); }
				comm += str;
			}
			text = comm + ":" + (""+((char) 10));
			int i = 1;
			for (String name : lc) {
				int ofs = 1 + i;
				while(ofs>0) { text += "  "; ofs--; }
				text += chr + ((i == lc.size()) ? "6" : "7") + name + (""+((char) 10));
				i++;
			}
			text += (""+((char) 10)) + 
					new TextComponentTranslation("event.example").getFormattedText() + (""+((char) 10)) +
					chr + "9function " + chr + "f" + evdEx.func + chr + "9(" + chr + "eevent" + chr + "9) {" + (""+((char) 10));
			
			for (String name : m.keySet()) {
				MethodData md = m.get(name);
				String ret = md.returnType.getName();
				if (ret.indexOf("[L")==0) { ret = ret.replace("[L", "").replace(";", "") + "[]"; }
				else if (ret.indexOf("[B")==0) { ret = ret.replace("[B", "").replace(";", "") + "[]"; }
				String key = "  "+chr + "eevent" + chr + "9." + chr + "f" + name + chr + "9; "  + chr + "7// " + ret;
				text += key + (""+((char) 10)); 
				this.data.put(key, m.get(name));
			}
			text += chr + "9}" + ((char) 10);
			if (evd.event.getMethods().length>0) {
				text += (""+((char) 10)) + new TextComponentTranslation("event.methods").getFormattedText() + (""+((char) 10));
				TreeMap<String, MethodData> md = Maps.<String, MethodData>newTreeMap();
				for (Method mtd : evd.event.getMethods()) {
					MethodData mdata = new MethodData(mtd.getReturnType(), mtd.getName(), "method.event."+mtd.getName().toLowerCase());
					mdata.parameters = new ParameterData[mtd.getParameterCount()];
					if (mtd.getParameterCount()>0) {
						Parameter[] ps = mtd.getParameters();
						for (int g = 0; g < ps.length; g++) {
							mdata.parameters[g] = new ParameterData(ps[g].getType(), ps[g].getType().getSimpleName().toLowerCase() + g, "parameter.event."+mtd.getName().toLowerCase()+"."+ps[g].getType().getName().toLowerCase());
						}
					}
					//mdata.isVarielble = true;
					md.put(mtd.getName(), mdata);
				}
				for (String name : md.keySet()) {
					String key = md.get(name).getText();
					text += key+(""+((char) 10));
					this.data.put(key, md.get(name));
				}
			}
		}
		else if (GuiHelpBook.activeButton==23 && this.scroll!=null) { // Constants
			String select = this.scroll.getSelected();
			Object value = this.scroll.hoversTexts[this.scroll.selected][0];
			Class<?> c = null;
			boolean isMain = false;
			try {
				if (select.indexOf("_")!=-1) { c = Class.forName("noppes.npcs.api.constants."+ select.substring(0, select.indexOf("_"))); }
				else {
					c = Class.forName("noppes.npcs.api.constants."+ select);
					isMain = true;
				}
			}
			catch (ClassNotFoundException e) { }
			if (c==null || !c.isEnum()) { // Object
				value = ScriptContainer.Data.get(select);
				String constant = "constant.object";
				if (value.toString().indexOf("JavaClassStatics[")==0) {
					value = chr + "e" + value.toString().replace("JavaClassStatics[", "").replace("]", "");
					constant = "constant.static";
				} else if (value.getClass().getName().equals("jdk.nashorn.api.scripting.ScriptObjectMirror")) {
					if (value.toString().indexOf("function")==0) {
						value = chr + "3" + value.toString().substring(0, value.toString().indexOf("{")) + "{ /*...*/ }";
					}
				} else if (value.toString().indexOf("ScriptContainer$")!=-1) {
					String sfx = "(Object obj)";
					if (value.toString().indexOf("$Log")!=-1) { sfx = "(String message)"; }
					value = value.toString().replace("$", ".");
					value = chr + "e" + value.toString().substring(0, value.toString().indexOf("@")) + sfx;
				} else if (value.toString().indexOf("WorldWrapper$")!=-1) {
					String sfx = ".tempdata; // IData";
					if (value.toString().indexOf("$2@")!=-1) { sfx = ".storeddata; // IData"; }
					value = chr + "b" + value.toString().substring(0, value.toString().indexOf("$")) + sfx;
				} else {
					value = chr + "6" + value.toString();
				}
				text += new TextComponentTranslation("constant.custom").getFormattedText() + (""+((char) 10)) + (""+((char) 10)) +
						chr + "9" + select + chr + "7 == " + (""+((char) 10)) + value + (""+((char) 10)) + (""+((char) 10)) +
						new TextComponentTranslation(constant).getFormattedText();
				if (ScriptController.Instance.constants.getCompoundTag("Constants").hasKey(select, 8)) {
					text += (""+((char) 10)) + (""+((char) 10)) + new TextComponentTranslation("constant.written.as", chr + "7" + ScriptController.Instance.constants.getCompoundTag("Constants").getString(select)).getFormattedText();
				}
			} else { // Base Enums
				String example = "";
				if (c==AnimationKind.class) {
					example = chr + "9var " + chr + "fianim " + chr + "9= " + chr + "6INPCAnimation" + chr + "9." + chr + "fgetAnimations" + chr + "9(" + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
							chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
							chr + "9var " + chr + "fianim " + chr + "9= " + chr + "6IAnimationHandler" + chr + "9." + chr + "fcreateNew" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==AnimationType.class) {
					example = chr + "6INPCAi" + chr + "9." + chr + "fsetAnimation" + chr + "9(" + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "6INPCAi" + chr + "9." + chr + "fsetAnimation" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==EntityType.class) {
					example = chr + "6IWorld" + chr + "9." + chr + "fgetClosestEntity" + chr + "9(" + chr + "7IPos" + chr + "9, " + chr + "7range" + chr + "9, " + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "9if (" + chr + "7IEntity" + chr + "9." + chr + "fgetType" + chr + "9() == " + chr + "6" + value + chr + "9) { " + chr + "7 /*...*/" + chr + "9 }";
				}
				else if (c==JobType.class) {
					example = chr + "9if (" + chr + "7INPCJob" + chr + "9." + chr + "fgetType" + chr + "9() == " + chr + "6" + select + chr + "9) { " + chr + "7 /*...*/" + chr + "9 }" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "9if (" + chr + "7INPCJob" + chr + "9." + chr + "fgetType" + chr + "9() == " + chr + "6" + value + chr + "9) { " + chr + "7 /*...*/" + chr + "9 }";
				}
				else if (c==MarkType.class) {
					example = chr + "9var " + chr + "fimark " + chr + "9= " + chr + "6IEntityLivingBase" + chr + "9." + chr + "faddMark" + chr + "9(" + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "9var " + chr + "fimark " + chr + "9= " + chr + "6IEntityLivingBase" + chr + "9." + chr + "faddMark" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==OptionType.class) {
					example = chr + "9var " + chr + "fidialogoption " + chr + "9= " + chr + "6IDialog" + chr + "9." + chr + "fgetOption" + chr + "9(" + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "9var " + chr + "fidialogoption " + chr + "9= " + chr + "6IDialog" + chr + "9." + chr + "fgetOption" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==ParticleType.class) {
					example = chr + "6INPCRanged" + chr + "9." + chr + "fsetParticle" + chr + "9(" + chr + "r" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "6INPCRanged" + chr + "9." + chr + "fsetParticle" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==PotionEffectType.class) {
					example = chr + "6INPCMelee" + chr + "9." + chr + "fsetEffect" + chr + "9(" + chr + "r" + select + chr + "9, " + chr + "rstrength" + chr + "9, " + chr + "rtime" + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "6INPCRanged" + chr + "9." + chr + "fsetEffect" + chr + "9(" + chr + "6" + value + chr + "9, " + chr + "rstrength" + chr + "9, " + chr + "rtime" + chr + "9);";
				}
				else if (c==RoleType.class) {
					example = chr + "9if (" + chr + "7INPCRole" + chr + "9." + chr + "fgetType" + chr + "9() == " + chr + "6" + select + chr + "9) { " + chr + "7 /*...*/" + chr + "9 }" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "9if (" + chr + "7INPCRole" + chr + "9." + chr + "fgetType" + chr + "9() == " + chr + "6" + value + chr + "9) { " + chr + "7 /*...*/" + chr + "9 }";
				}
				else if (c==SideType.class) {
					example = chr + "6IBlock" + chr + "9." + chr + "finteract" + chr + "9(" + chr + "6" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "6IBlock" + chr + "9." + chr + "finteract" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				else if (c==TacticalType.class) {
					example = chr + "6INPCAi" + chr + "9." + chr + "fsetTacticalType" + chr + "9(" + chr + "6" + select + chr + "9);" + (""+((char) 10)) +
					chr + "7// " + new TextComponentTranslation("quest.task.step.2").getFormattedText() + (""+((char) 10)) +
					chr + "6INPCAi" + chr + "9." + chr + "fsetTacticalType" + chr + "9(" + chr + "6" + value + chr + "9);";
				}
				text += new TextComponentTranslation("constant.base").getFormattedText() + (""+((char) 10)) + (""+((char) 10)) +
						chr + "9" + select + chr + "7 == " + (""+((char) 10)) + chr + "6" + value + (""+((char) 10)) + (""+((char) 10)) + 
						(isMain ? new TextComponentTranslation("constant.class").getFormattedText()
								: new TextComponentTranslation("constant."+ c.getSimpleName().toLowerCase()).getFormattedText() +
								(example.isEmpty() ? "" : 
								(""+((char) 10)) + (""+((char) 10)) +
								new TextComponentTranslation("event.example").getFormattedText() + (""+((char) 10)) + example));
			}
		}
		area.setText(text);
		area.setCursorPosition(0);
		area.scrolledLine = 0;
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
		if (this.scroll!=null) { this.scroll.setSelected(null); }
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
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (this.scroll!=null) { this.resetText(); }
	}
	
}
