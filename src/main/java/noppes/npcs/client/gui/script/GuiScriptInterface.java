package noppes.npcs.client.gui.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.api.handler.data.IScriptData;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.ScriptData;

//Changed
public class GuiScriptInterface
extends GuiNPCInterface
implements IGuiData, ITextChangeListener, ICustomScrollListener {
	
	private int activeTab;
	Map<String, Class<?>> baseFuncNames = new HashMap<String, Class<?>>();
	private final Map<String, Map<Integer, ScriptData>> data = Maps.newHashMap(); // In Global // Local List
	private String error = "";
	public IScriptHandler handler;
	public Map<String, List<String>> languages;
	// New
	private final Map<String, String[]> map = Maps.newHashMap(); // In Help Scroll
	private final List<String> newFunc = new ArrayList<String>();
	private int startPos;
	private GuiNpcLabel helper;
	private GuiCustomScroll scrollHelp;
	private GuiCustomScroll scrollVariables;
	private int var = 0;
	private Map<String, ScriptData> dataVar = Maps.newHashMap();
	private ScaledResolution scaleW;
	List<String> regexFunction = Lists.<String>newArrayList("if","else","switch","with","for","while","in","var","const","let","throw","then","function","continue","break","foreach","return","try","catch","finally","do","this","typeof","instanceof","new");
	private String select, preSelect;

	public GuiScriptInterface() {
		this.activeTab = 0;
		this.languages = new HashMap<String, List<String>>();
		this.drawDefaultBackground = true;
		this.closeOnEsc = true;
		this.xSize = 427;
		this.setBackground("menubg.png");
		this.scaleW = new ScaledResolution(Minecraft.getMinecraft());
	}

	@Override
	public void initGui() {
		super.initGui();
		try {
			this.xSize = (int) (this.width * 0.88);
			this.ySize = (int) (this.xSize * 0.56);
			if (this.ySize > this.height * 0.95) {
				this.ySize = (int) (this.height * 0.95);
				this.xSize = (int) (this.ySize / 0.56);
			}
			this.bgScale = this.xSize / 400.0f;
			super.initGui();
			this.guiTop += 10;
			

			this.bgScale = this.xSize / 400.0f;
			
			int yoffset = (int) (this.ySize * 0.02);
			GuiMenuTopButton top;
			this.addTopButton(top = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.settings"));
			for (int i = 0; i < this.handler.getScripts().size(); ++i) {
				this.addTopButton(top = new GuiMenuTopButton(i + 1, top, i + 1 + ""));
			}
			if (!(this instanceof GuiScriptClient) && this.handler.getScripts().size() < 16) {
				this.addTopButton(top = new GuiMenuTopButton(12, top, "+"));
			}
			top = this.getTopButton(this.activeTab);
			if (top == null) {
				if (this.activeTab > 0 && this instanceof GuiScriptClient) {
					this.activeTab = 1;
					top = this.getTopButton(12);
				} else {
					this.activeTab = 0;
					top = this.getTopButton(0);
				}
			}
			top.active = true;
			if (this.activeTab > 0) {
				ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
				GuiTextArea ta = new GuiTextArea(2, this.guiLeft + 1 + yoffset, this.guiTop + yoffset,
						this.xSize - 108 - yoffset, (int) ((this.ySize * 0.96) - yoffset * 2),
						(container == null) ? "" : container.script);
				ta.enableCodeHighlighting();
				ta.setListener(this);
				this.add(ta);
				int left = this.guiLeft + this.xSize - 105;
				this.addButton(new GuiNpcButton(102, left, this.guiTop + yoffset, 60, 20, "gui.clear"));
				this.addButton(new GuiNpcButton(101, left + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
				this.addButton(new GuiNpcButton(100, left, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
				this.addButton(new GuiNpcButton(105, left + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));
				this.addButton(new GuiNpcButton(107, left, this.guiTop + 42 + yoffset, 80, 20, "script.loadscript"));
				GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setUnselectable();
				scroll.setSize(100, (int) ((this.ySize * 0.54) - yoffset * 2));
				scroll.guiLeft = left;
				scroll.guiTop = this.guiTop + 88 + yoffset;
				if (container != null) {
					scroll.setList(container.scripts);
				}
				this.addScroll(scroll);
	
				// New
				if (!CustomNpcs.useScriptHelper) {
					return;
				}
				this.addButton(new GuiButtonBiDirectional(120, left, this.guiTop + 63 + yoffset, 120, 20,
						new String[] { "global", "local" }, this.var));
	
				this.scrollHelp = new GuiCustomScroll(this, 0);
				this.scrollHelp.visible = false; // this.activeTab>0
				this.scrollHelp.colorBack = 0xFF808080;
				this.scrollHelp.setSize(119, this.ySize - 102);
				this.addScroll(this.scrollHelp);
	
				this.scrollVariables = new GuiCustomScroll(this, 1);
				this.scrollVariables.guiLeft = this.guiLeft + this.xSize - 105;
				this.scrollVariables.guiTop = this.guiTop + 84 + yoffset;
				this.scrollVariables.setSize(119, ta.height-84);
				this.scrollVariables.visible = this.activeTab > 0;
				this.addScroll(this.scrollVariables);
	
				this.helper = new GuiNpcLabel(0, "Test", 0, 0);
				this.helper.backColor = 0xFFFFFFE1;
				this.helper.borderColor = 0xFF646464;
	
				this.cheakVariables(50, null);
			} else {
				GuiTextArea ta2 = new GuiTextArea(2, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset,
						this.xSize - 160 - yoffset, (int) ((this.ySize * 0.92f) - yoffset * 2), this.getConsoleText());
				ta2.enabled = false;
				this.add(ta2);
				int left2 = this.guiLeft + this.xSize - 150;
				this.addButton(new GuiNpcButton(100, left2, this.guiTop + 125, 60, 20, "gui.copy"));
				this.addButton(new GuiNpcButton(102, left2, this.guiTop + 146, 60, 20, "gui.clear"));
				this.addLabel(new GuiNpcLabel(1, "script.language", left2, this.guiTop + 15));
				this.addButton(new GuiNpcButton(103, left2 + 60, this.guiTop + 10, 80, 20,
						this.languages.keySet().toArray(new String[this.languages.keySet().size()]),
						this.getScriptIndex()));
				this.getButton(103).enabled = (this.languages.size() > 0);
				this.addLabel(new GuiNpcLabel(2, "gui.enabled", left2, this.guiTop + 36));
				this.addButton(new GuiNpcButton(104, left2 + 60, this.guiTop + 31, 50, 20,
						new String[] { "gui.no", "gui.yes" }, (this.handler.getEnabled() ? 1 : 0)));
				if (this.player.getServer() != null) {
					this.addButton(new GuiNpcButton(106, left2, this.guiTop + 55, 150, 20, "script.openfolder"));
				}
				this.addButton(new GuiNpcButton(109, left2, this.guiTop + 78, 80, 20, "gui.website"));
				this.addButton(new GuiNpcButton(112, left2 + 81, this.guiTop + 78, 80, 20, "gui.forum"));
				this.addButton(new GuiNpcButton(110, left2, this.guiTop + 99, 80, 20, "script.apidoc"));
				this.addButton(new GuiNpcButton(111, left2 + 81, this.guiTop + 99, 80, 20, "script.apisrc"));
			}
			this.xSize = 420;
			this.ySize = 256;
		} catch (Exception e) {
			this.displayGuiScreen(null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id >= 0 && guibutton.id < 12) {
			this.setScript();
			this.activeTab = guibutton.id;
			this.initGui();
		}
		if (guibutton.id == 12) {
			if (this instanceof GuiScriptClient) {
				this.activeTab = 1;
			} else {
				this.handler.getScripts().add(new ScriptContainer(this.handler));
				this.activeTab = this.handler.getScripts().size();
			}
			this.initGui();
		}
		if (guibutton.id == 109) {
			this.displayGuiScreen((GuiScreen) new GuiConfirmOpenLink((GuiYesNoCallback) this,
					"http://www.kodevelopment.nl/minecraft/customnpcs/scripting", 0, true));
		}
		if (guibutton.id == 110) {
			this.displayGuiScreen((GuiScreen) new GuiConfirmOpenLink((GuiYesNoCallback) this,
					"http://www.kodevelopment.nl/customnpcs/api/", 1, true));
		}
		if (guibutton.id == 111) {
			this.displayGuiScreen((GuiScreen) new GuiConfirmOpenLink((GuiYesNoCallback) this,
					"https://github.com/Noppes/CustomNPCsAPI", 2, true));
		}
		if (guibutton.id == 112) {
			this.displayGuiScreen((GuiScreen) new GuiConfirmOpenLink((GuiYesNoCallback) this,
					"http://www.minecraftforge.net/forum/index.php/board,122.0.html", 3, true));
		}
		if (guibutton.id == 100) {
			NoppesStringUtils.setClipboardContents(((GuiTextArea) this.get(2)).getText());
		}
		if (guibutton.id == 101) {
			((GuiTextArea) this.get(2)).setText(NoppesStringUtils.getClipboardContents());
		}
		if (guibutton.id == 102) {
			if (this.activeTab > 0) {
				ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
				container.script = "";
			} else {
				this.handler.clearConsole();
			}
			this.initGui();
		}
		if (guibutton.id == 103) {
			this.handler.setLanguage(((GuiNpcButton) guibutton).displayString);
		}
		if (guibutton.id == 104) {
			this.handler.setEnabled(((GuiNpcButton) guibutton).getValue() == 1);
		}
		if (guibutton.id == 105) {
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, "",
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 10);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
		if (guibutton.id == 106) {
			NoppesUtil.openFolder(ScriptController.Instance.dir);
		}
		if (guibutton.id == 107) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container == null) {
				this.handler.getScripts().add(container = new ScriptContainer(this.handler));
			}
			this.setSubGui(new GuiScriptList(this.languages.get(AdditionalMethods.deleteColor(this.handler.getLanguage())), container));
		}
		if (guibutton.id == 108) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container != null) {
				this.setScript();
			}
		}
		if (guibutton.id == 120) {
			this.var = ((GuiNpcButton) guibutton).getValue();
			cheakVariables(50, null);
		}
	}

	private void cheakPath(GuiTextArea area) {
		if (this.activeTab == 0 || !CustomNpcs.useScriptHelper || this.subgui!=null) { return; }
		CustomNPCsScheduler.runTack(() -> {
			int pos = area.getCursorPosition();
			String text = area.getText();
			this.scrollHelp.visible = false;
			/** Create new data */
			if (text.length()==0) {
				this.map.put("var", null);
				this.map.put("function", null);
				this.scrollHelp.setListNotSorted(Lists.newArrayList("var", "function"));
				this.scrollHelp.selected = -1;
				this.scrollHelp.visible = true;
				this.scrollHelp.hoversTexts = null;
				resetHelpPos(pos, area);
				return;
			}
			/** Try found parametrs */
			this.startPos = 0; // start pos
			this.select = "";
			this.preSelect = "";
			char tab = 9;
			char enter = 10;
			String filter = (""+tab)+" ;"+(""+enter);
			if (pos!= text.length()) {
				while (pos>0 && (text.charAt(pos)==' ' || text.charAt(pos)==tab)) { pos--; }
			}
			for (int i = 0; i<filter.length(); i++) {
				char c = filter.charAt(i);
				int p = text.lastIndexOf(c, pos);
				if (p>-1) {
					this.select = AdditionalMethods.match(text, p + 1+(pos==(p + 1) ? 1: 0), filter, ".([{"+filter);
					this.startPos = text.lastIndexOf(this.select, p+1);
					if (this.startPos>0 && (c==' ' || c==tab)) {
						int e = -1;
						p = -1;
						for (int j = this.startPos-1; j>=0; j--) {
							c = text.charAt(j);
							if (e==-1) {
								if (c==' ' || c==tab) { e = j; }
								continue;
							}
							if (c!=' ' && c!=tab) {
								this.preSelect = AdditionalMethods.match(text, j, filter, GuiTextArea.filter);
								break;
							}
						}
					}
					break;
				}
			}
			if (this.startPos==0 && pos>0) { this.select = AdditionalMethods.match(text, 0, GuiTextArea.filter, GuiTextArea.filter); }
			while (this.select.indexOf(tab)!=-1) { this.select = this.select.replace(""+tab, ""); }
			while (this.preSelect.indexOf(tab)!=-1) { this.preSelect = this.preSelect.replace(""+tab, ""); }
			if (this.preSelect.equals("function") || (this.preSelect.isEmpty() && this.select.equals("function"))) {
				if (this.preSelect.isEmpty() && this.select.equals("function")) { this.select = ""; }
				this.map.clear();
				for (String func : this.baseFuncNames.keySet()) {
					if (!this.select.isEmpty() && func.indexOf(this.select)==-1) { continue; }
					this.map.put(func + "(event)", new String[0]);
				}
				if (this.map.size() > 0) {
					this.scrollHelp.setListNotSorted(Lists.newArrayList(this.map.keySet()));
					this.scrollHelp.selected = -1;
					this.scrollHelp.visible = true;
					this.scrollHelp.hoversTexts = null;
					resetHelpPos(pos, area);
				}
				return;
			}
			//if (this.select.isEmpty()) { return; }
			String part = text.substring(this.startPos, pos);
			if (part.lastIndexOf(" ", pos)!=-1) { part = part.substring(part.lastIndexOf(" ", pos)+1); }
			if (part.lastIndexOf(""+((char) 9), pos)!=-1) { part = part.substring(part.lastIndexOf(""+((char) 9), pos)+1); }
			String path = part;
			if (part.lastIndexOf('.', pos-this.startPos)!=-1) { path = path.substring(0, path.lastIndexOf('.', pos-this.startPos)); }
			//language
			String language = this.handler.getLanguage();
			if (language.isEmpty() && this.getScriptIndex() < this.languages.size()) {
				int i = 0, j = this.getScriptIndex();
				for (String l : this.languages.keySet()) {
					if (i == j) {
						language = l;
						break;
					}
				}
			}
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container == null) { return; }
			if (!text.isEmpty()) { text += "\n"; }
			for (String loc : container.scripts) {
				String code = ScriptController.Instance.scripts.get(loc);
				if (code != null && !code.isEmpty()) {
					text += code + "\n";
				}
			}
			text = text.replace(area.getLineText(), "");
			if (!text.isEmpty()) {
				String newText = text;
				newText = newText.replace("\r\n", "\n");
				newText = text.replace("\r", "\n");
				container.script = newText;
			} else {
				this.setScript();
			}
			this.setData(AdditionalMethods.getScriptData(language, container, this.baseFuncNames));
			Map<String, Class<?>> parametrs = Maps.newHashMap();
			String fName = this.getFuncName();
			boolean foundFunc = false;
			
			for (String key : this.data.keySet()) {
				if (this.data.get(key).size()==1) {
					ScriptData sd = this.data.get(key).get(0);
					if (sd.getType()==12) {
						if (!fName.isEmpty() && key.equals(fName)) {
							ScriptData func = this.data.get(fName).get(0);
							Map<String, Class<?>> prs = func.getVariables(this.data);
							for (String k : prs.keySet()) { parametrs.put(k, prs.get(k)); }
							foundFunc = true;
						}
					}
					if ((!fName.isEmpty() && key.equals(fName)) || (foundFunc && parametrs.containsKey(key))) { continue; }
					parametrs.put(key, AdditionalMethods.getScriptClass(sd.getObject()));
				}
			}
			List<String> keys = Lists.<String>newArrayList();
			while (path.indexOf('.')!=-1) {
				keys.add(path.substring(0, path.indexOf('.')));
				path = path.substring(path.indexOf('.')+1);
			}
			keys.add(path);
			Map<String, Map<String, String[]>> mdata = AdditionalMethods.getObjectVarAndMetods(keys.toArray(new String[keys.size()]), parametrs);
			if (mdata.size() > 0) {
				part = this.select;
				this.select = AdditionalMethods.match(part, pos-this.startPos, ".", ".([{"+filter);
				if (this.select.indexOf('.')==0) { this.select = this.select.substring(1); }
				this.startPos += part.lastIndexOf(this.select, pos-this.startPos);
				this.map.clear();
				List<String> vars = Lists.newArrayList();
				for (int j = 0; j < 3; j++) {
					String group = "classes";
					if (j == 1) {
						group = "fields";
					} else if (j == 2) {
						group = "methods";
					}
					if (mdata.containsKey(group)) {
						List<String> list = Lists.newArrayList(mdata.get(group).keySet());
						Collections.sort(list);
						for (String str : list) {
							if (this.select.isEmpty() || str.toLowerCase().indexOf(this.select.toLowerCase()) != -1) {
								this.map.put(str, mdata.get(group).get(str));
								vars.add(str);
							}
						}
					}
				}
				if (vars.size() == 1 && vars.get(0).equals(this.select)) {
					this.map.clear();
					this.scrollHelp.visible = false;
					return;
				}
				String[][] hts = new String[vars.size()][];
				int i = 0;
				for (String str : vars) {
					if (CustomNpcs.scriptHelperObfuscations) {
						boolean found = false;
						if (AdditionalMethods.obfuscations.containsValue(str)) {
							found = true;
							String[] parent = this.map.get(str);
							hts[i] = new String[parent.length + 1];
							for (int g = 0; g < parent.length; g++) {
								hts[i][g] = parent[g];
							}
							hts[i][parent.length] = Character.toChars(0x00A7)[0] + "8Deobfuscation name: "
									+ Character.toChars(0x00A7)[0] + "r" + AdditionalMethods.obfuscations.get(str);
							break;
						}
						if (!found) {
							hts[i] = this.map.get(str);
						}
					} else {
						hts[i] = this.map.get(str);
					}
					i++;
				}
				this.scrollHelp.setListNotSorted(vars);
				this.scrollHelp.hoversTexts = hts;
				this.scrollHelp.selected = -1;
				this.scrollHelp.visible = true;
				this.resetHelpPos(this.startPos, area);
				return;
			} else {
				this.map.clear();
				this.scrollHelp.visible = false;
			}
		}, 50);
	}

	private void cheakVariables(int time, String text) {
		if (this.activeTab == 0 || !CustomNpcs.useScriptHelper || this.subgui!=null) {
			return;
		}
		CustomNPCsScheduler.runTack(() -> {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (text != null && !text.isEmpty()) {
				if (container == null) {
					this.handler.getScripts().add(container = new ScriptContainer(this.handler));
				}
				String newText = text;
				newText = newText.replace("\r\n", "\n");
				newText = text.replace("\r", "\n");
				container.script = newText;
			} else {
				this.setScript();
			}
			/** language */
			String language = this.handler.getLanguage();
			if (language.isEmpty() && this.getScriptIndex() < this.languages.size()) {
				int i = 0, j = this.getScriptIndex();
				for (String l : this.languages.keySet()) {
					if (i == j) {
						language = l;
						break;
					}
				}
			}
			this.setData(AdditionalMethods.getScriptData(language, container, this.baseFuncNames));
		}, time);
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			if (i == 0) {
				this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
			}
			if (i == 1) {
				this.openLink("http://www.kodevelopment.nl/customnpcs/api/");
			}
			if (i == 2) {
				this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
			}
			if (i == 3) {
				this.openLink("http://www.minecraftforge.net/forum/index.php/board,122.0.html");
			}
			if (i == 10) {
				this.handler.getScripts().remove(this.activeTab - 1);
				this.activeTab = 0;
			}
		}
		this.displayGuiScreen(this);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.activeTab == 0 || this.scrollVariables == null || this.getButton(120) == null) {
			return;
		}
		if (this.subgui!=null) {
			if (this.helper!=null) { this.helper.enabled = false; }
			if (this.scrollVariables!=null) {
				this.scrollHelp.selected = -1;
				this.scrollHelp.visible = false;
				this.scrollHelp.hoversTexts = null;
			}
			return;
		}
		this.getButton(120).layerColor = this.error.isEmpty() ? 0 : 0xA0FF2020;
		if (!this.error.isEmpty() && this.getButton(120).isMouseOver()) {
			this.hoverText = new String[] { this.error };
		}
		if (!CustomNpcs.useScriptHelper || this.helper == null || this.get(2) == null) {
			return;
		}
		GuiTextArea area = ((GuiTextArea) this.get(2));
		Object[] select = area.getSelectionText(mouseX, mouseY);
		this.helper.enabled = false;

		if ((int) select[0] != -1 && !((String) select[1]).isEmpty()) {
			if (this.data.containsKey(select[1])) {
				ScriptData sd = this.data.get(select[1]).get(0);
				this.helper.setLabel(sd.getGUIDescription());
				this.helper.enabled = true;
			}
			if (this.data.containsKey(select[1]) && this.data.get(select[1]).containsKey(select[0])) {
				ScriptData sd = this.data.get(select[1]).get(select[0]);
				if (((String) select[1]).equalsIgnoreCase("var") || ((String) select[1]).equalsIgnoreCase("function")) {
					this.helper.setLabel(sd.getValue());
				}
				else {
					this.helper.setLabel(sd.getGUIDescription());
				}
				this.helper.enabled = true;
			} else {
				Integer i = null;
				Double d = null;
				try { d = Double.parseDouble((String) select[1]); } catch (Exception e) {  }
				try { i = Integer.parseInt((String) select[1]); } catch (Exception e) {  }
				if (d!=null || i!=null) {
					if (d!=null && i==null) { this.helper.setLabel("double = " + d); }
					else { this.helper.setLabel("int = " + i); }
					this.helper.enabled = true;
				}
			}
		} else {
			return;
		}
		if (mouseX + this.helper.width < this.scaleW.getScaledWidth_double()) { this.helper.x = mouseX; } else { this.helper.x = mouseX - this.helper.width; }
		if (this.helper.x<2) { this.helper.x = 2; }
		if (mouseY + this.helper.height + 10 < this.scaleW.getScaledHeight_double()) { this.helper.y = mouseY + 10; } else { this.helper.y = mouseY - this.helper.height - 6; }
		this.helper.drawLabel(this, this.fontRenderer, mouseX, mouseY, partialTicks);
	}

	private String getConsoleText() {
		Map<Long, String> map = this.handler.getConsoleText();
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Long, String> entry : map.entrySet()) {
			builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n");
		}
		return builder.toString();
	}

	private String getFuncName() {
		if (!CustomNpcs.useScriptHelper || !(this.get(2) instanceof GuiTextArea)) {
			return "";
		}
		GuiTextArea area = (GuiTextArea) this.get(2);
		if (area == null) {
			return "";
		}
		String text = area.getText();
		int st = text.lastIndexOf("function", area.getCursorPosition());
		if (st<0) {
			return "";
		}
		int end = text.indexOf("(", st+8);
		String funcName = end>(st+8) ? text.substring(st + 8, end) : text.substring(st + 8);
		while (funcName.indexOf(" ") != -1) {
			funcName = funcName.replace(" ", "");
		}
		return funcName;
	}

	private int getScriptIndex() {
		int i = 0;
		for (String language : this.languages.keySet()) {
			if (language.equalsIgnoreCase(this.handler.getLanguage())) {
				return i;
			}
			++i;
		}
		return 0;
	}

		@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && CustomNpcs.useScriptHelper && this.scrollHelp != null && this.scrollHelp.visible) {
			this.scrollHelp.visible = false;
			return;
		}
		super.keyTyped(c, i);
		if (!CustomNpcs.useScriptHelper) {
			return;
		}
		if (i == 15 && this.scrollHelp != null) { // Tab
			if (this.scrollHelp.visible) {
				this.scrollHelp.selected = 0;
				this.scrollClicked(0, 0, 0, this.scrollHelp);
				return;
			}
		}
		if (c == '.' && this.scrollHelp != null) {
			this.scrollHelp.visible = false;
			this.map.clear();
			this.startPos = 0;
		}
		GuiTextArea area = (GuiTextArea) this.get(2);
		if (area == null) {
			return;
		}
		cheakPath(area);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (!CustomNpcs.useScriptHelper || !(this.get(2) instanceof GuiTextArea)) { return; }
		GuiTextArea area = (GuiTextArea) this.get(2);
		if (!area.hovered) { return; }
		if (this.scrollHelp != null && this.scrollHelp.visible && !this.scrollHelp.hovered) {
			this.scrollHelp.visible = false;
			return;
		}
		if (area.getCursorPosition() != -1) { cheakPath(area); }
	}

	private void resetHelpPos(int pos, GuiTextArea area) {
		if (!CustomNpcs.useScriptHelper) { return; }
		if (area == null || pos < 0 || this.scrollHelp == null || !this.scrollHelp.visible) { return; }
		int[] xy = area.getXYPosition(pos);
		this.scrollHelp.scrollY = 0;
		this.scrollHelp.guiLeft = area.x;
		this.scrollHelp.guiTop = xy[1] + 1;
		int h = this.scrollHelp.getList().size() * 14 + 4;
		int w = 0;
		for (String str : this.scrollHelp.getList()) {
			int t = str.length() * 5 + 12;
			if (w == 0 || w < t) {
				w = t;
			}
		}
		if (w > area.width) { w = area.width; }
		if (h > 74) { h = 74; }
		this.scrollHelp.setSize(w, h);
		ScaledResolution sw = new ScaledResolution(this.mc);
		if (xy[1] + h > sw.getScaledHeight()) {
			this.scrollHelp.guiTop -= (h + 14);
		}
	}

	@Override
	public void save() {
		this.setScript();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!CustomNpcs.useScriptHelper || scroll==null) { return; }
		GuiTextArea area = (GuiTextArea) this.get(2);
		if (area == null || scroll.getSelected()==null) { return; }
		String text = area.getText(), add = "";
		if (scroll.id == 1) { // variebels
			String select = "" + AdditionalMethods.deleteColor(scroll.getSelected());
			scroll.selected = -1;
			ScriptData d = this.dataVar.get(select);
			if (d==null) { return; }
			String keys = "";
			if (d.getType()==12) {
				for (String k : d.getParametrs()) { keys += k + ", "; }
				keys = "("+keys.substring(0, keys.length()-2)+")";
			}
			add = select + keys;
			this.startPos = area.getCursorPosition();
			this.preSelect = "";
			this.select = "";
		}
		else if (scroll.id==0 && !scroll.getSelected().isEmpty()) { // helper hover
			add = AdditionalMethods.deleteColor(scroll.getSelected());
		}
		if (text==null || add==null || add.isEmpty() || this.startPos<0 || (text.length()!=0 && this.startPos>=text.length())) { return; }
		int newpos = this.startPos + add.length()-1;
		if (text.length()==0) { text = add+" "; }
		else {
			if (this.preSelect.equals("function")) {
				if (text.indexOf('{')!=-1) {
					add += " {";
					this.select = text.substring(this.startPos, text.indexOf('{')+1);
					newpos += 2;
				} else {
					add += " {"+(""+((char) 10))+"  "+(""+((char) 10))+"}";
					newpos += 5;
				}
			}
			if (!this.select.isEmpty()) {
				text = text.substring(0, this.startPos)+(this.startPos+this.select.length()<text.length() ? text.substring(this.startPos+this.select.length()) : "");
			}
			text = (this.startPos > 0 ? text.substring(0, this.startPos) : "") + add + (this.startPos < text.length() - 1 ? text.substring(this.startPos) : "");
		}
		area.setText(text);
		area.setCursorPosition(newpos);
		this.scrollHelp.visible = false;
		this.map.clear();
		this.startPos = 0;
		this.keyTyped(((char) 0), 205); // rigth key
		cheakPath(area);
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@SuppressWarnings({ "unchecked" })
	public void setData(Object[] objects) { // New
		String func = this.getFuncName();
		ScriptData funcSD = null;
		this.newFunc.clear();
		this.data.clear();
		this.data.put("var", Maps.newHashMap());
		this.data.get("var").put(0, new ScriptData("var", "Variable declaration", this.handler.getLanguage()));
		this.data.put("function", Maps.newHashMap());
		this.data.get("function").put(0, new ScriptData("function", "Function declaration", this.handler.getLanguage()));
		List<IScriptData> variables = (List<IScriptData>) objects[0];
		this.error = (String) objects[1];
		List<String> conss = new ArrayList<String>();
		List<String> arrs = new ArrayList<String>();
		List<String> vars = new ArrayList<String>();
		List<String> funcs = new ArrayList<String>();
		Map<String, String> names = Maps.newHashMap();
		Map<String, String[]> ht = Maps.newHashMap();

		for (IScriptData isd : variables) {
			ScriptData sd = (ScriptData) isd;
			this.data.put(sd.getName(), Maps.newHashMap());
			this.data.get(sd.getName()).put(0, sd);
			String key = sd.getName();
			if (sd.isConstant) {
				conss.add(key);
			} else if (sd.getType() == 11) {
				arrs.add(key);
			} else if (sd.getType() == 12) {
				funcs.add(key);
				if (!func.isEmpty() && sd.getName().equals(func)) {
					funcSD = sd;
				}
			} else {
				vars.add(key);
			}
			ht.put(key, sd.getGUIDescription());
			names.put(key, sd.getGUIName());
			this.dataVar.put(key, sd);
		}
		String[][] htl = null;
		int i = 0;
		List<String> toScroll = new ArrayList<String>();
		if (this.var == 1) {
			Map<String, String[]> dsf = funcSD.getFuncVariables();
			htl = new String[dsf.size()][];
			for (String name : dsf.keySet()) {
				toScroll.add(name);
				htl[i] = dsf.get(name);
				i++;
			}
		} else {
			Collections.sort(conss);
			Collections.sort(arrs);
			Collections.sort(vars);
			Collections.sort(funcs);
			htl = new String[conss.size() + arrs.size() + vars.size() + funcs.size()][];
			for (String key : vars) {
				toScroll.add(names.get(key));
				htl[i] = ht.get(key);
				i++;
			}
			for (String key : arrs) {
				toScroll.add(names.get(key));
				htl[i] = ht.get(key);
				i++;
			}
			for (String key : funcs) {
				toScroll.add(names.get(key));
				htl[i] = ht.get(key);
				i++;
			}
			for (String key : conss) {
				toScroll.add(names.get(key));
				htl[i] = ht.get(key);
				i++;
			}
		}
		this.scrollVariables.setListNotSorted(toScroll);
		this.scrollVariables.hoversTexts = htl;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList data = compound.getTagList("Languages", 10);
		Map<String, List<String>> languages = new HashMap<String, List<String>>();
		for (int i = 0; i < data.tagCount(); ++i) {
			NBTTagCompound comp = data.getCompoundTagAt(i);
			List<String> scripts = new ArrayList<String>();
			NBTTagList list = comp.getTagList("Scripts", 8);
			for (int j = 0; j < list.tagCount(); ++j) {
				scripts.add(list.getStringTagAt(j));
			}
			languages.put(comp.getString("Language"), scripts);
		}
		this.languages = languages;
		this.initGui();
	}

	private void setScript() {
		if (this.activeTab > 0) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container == null) {
				this.handler.getScripts().add(container = new ScriptContainer(this.handler));
			}
			String text = ((GuiTextArea) this.get(2)).getText();
			text = text.replace("\r\n", "\n");
			text = text.replace("\r", "\n");
			container.script = text;
		}
	}

	@Override
	public void textUpdate(String text) {
		ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
		if (container != null) {
			container.script = text;
			this.cheakVariables(50, text);
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (!CustomNpcs.useScriptHelper || this.scrollHelp == null || this.get(2) == null) {
			return;
		}
		GuiTextArea area = ((GuiTextArea) this.get(2));
		area.freeze = this.scrollHelp.visible;
		if (this.error.indexOf("line number") != -1) {
			int line = -1;
			try {
				int st = this.error.indexOf("line number ") + 12;
				int end = this.error.indexOf(" ", st);
				if (end == -1) {
					end = this.error.length();
				}
				line = Integer.parseInt(this.error.substring(st, end));
			} catch (Exception e) {
				return;
			}
			area.errorLine = line - 1;
		} else {
			area.errorLine = -1;
		}
	}

}
