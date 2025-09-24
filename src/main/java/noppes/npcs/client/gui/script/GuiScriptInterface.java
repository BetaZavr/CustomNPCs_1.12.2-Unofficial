package noppes.npcs.client.gui.script;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.*;

import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.reflection.nbt.TagLongArrayReflection;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiScriptInterface extends GuiNPCInterface
		implements IGuiData, ITextChangeListener, ICustomScrollListener {

	private static final String web_site = "http://www.kodevelopment.nl/minecraft/customnpcs/scripting";
	private static final String api_doc_site = "https://github.com/BetaZavr/CustomNPCsAPI-Unofficial";
	private static final String api_site = "https://github.com/BetaZavr/CustomNPCsAPI-Unofficial";
	private static final String dis_site = "https://discord.gg/RGb4JqE6Qz";
	protected int activeTab;
	protected final Map<Integer, Long> dataLog = new HashMap<>();
	protected Long selectLog = 0L;
	public IScriptHandler handler;
	public Map<String, Map<String, Long>> languages = new HashMap<>();
	public String path, ext;

	protected boolean wait = true;

	public GuiScriptInterface() {
		super();
		drawDefaultBackground = true;
		closeOnEsc = true;
		setBackground("menubg.png");
		activeTab = 0;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0 || wait) { return; }
		if (button.getID() >= 0 && button.getID() < CustomNpcs.ScriptMaxTabs) {
			setScript();
			activeTab = button.getID();
			initGui();
			return;
		}
		if (button.getID() == CustomNpcs.ScriptMaxTabs + 1) {
			if (handler.getScripts().size() >= CustomNpcs.ScriptMaxTabs) {
				activeTab = CustomNpcs.ScriptMaxTabs;
				initGui();
				return;
			}
			handler.getScripts().add(new ScriptContainer(handler, true));
			activeTab = handler.getScripts().size();
			initGui();
			return;
		}
		switch (button.getID()) {
			case 100: {
				if (activeTab == 0) { // copy all logs
					Map<Long, String> map = handler.getConsoleText();
					StringBuilder builder = new StringBuilder();
					for (Map.Entry<Long, String> entry : map.entrySet()) { builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n\n"); }
					NoppesStringUtils.setClipboardContents(builder.toString());
				}
				else { NoppesStringUtils.setClipboardContents(((GuiTextArea) get(2, GuiTextArea.class)).getText()); }
				break;
			} // copy code or all logs
			case 101: {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				if (container != null) {
					boolean sr = !(container.script == null || container.script.isEmpty());
					if (sr) {
						String tempScript = container.script.replace(" ", "").replace("" + ((char) 9), "").replace("" + ((char) 10), "");
						sr = !tempScript.isEmpty();
					}
					if (sr) {
						displayGuiScreen(new GuiYesNo(this, "", new TextComponentTranslation("gui.replaceMessage").getFormattedText(), 6));
						return;
					}
				}
				getTextArea(2).setText(NoppesStringUtils.getClipboardContents());
				break;
			}
			case 102: {
				if (activeTab > 0) { displayGuiScreen(new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 5)); }
				else { handler.clearConsole(); }
				initGui();
				break;
			} // clear all logs
			case 103: {
				handler.setLanguage(button.displayString);
				String[] data = getLanguageData(button.displayString);
				button.setHoverText(new TextComponentTranslation("script.hover.info." + data[0]).
						appendSibling(new TextComponentTranslation("script.hover.info.dir", data[1], data[2])).getFormattedText());
				break;
			}
			case 104: handler.setEnabled(button.getValue() == 1); break;
			case 105: displayGuiScreen(new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 10)); break;
			case 106: NoppesUtil.openFolder(ScriptController.Instance.dir); break;
			case 107: {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				if (container == null) { handler.getScripts().add(container = new ScriptContainer(handler, true)); }
				setSubGui(new SubGuiScriptList(languages.get(Util.instance.deleteColor(handler.getLanguage())), container));
				break;
			}
			case 108: {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				if (container != null) { setScript(); }
				break;
			}
			case 109: displayGuiScreen(new GuiConfirmOpenLink(this, web_site, 0, true)); break;
			case 110: displayGuiScreen(new GuiConfirmOpenLink(this, api_doc_site, 1, true)); break;
			case 111: displayGuiScreen(new GuiConfirmOpenLink(this, api_site, 2, true)); break;
			case 112: displayGuiScreen(new GuiConfirmOpenLink(this, dis_site, 3, true)); break;
			case 115: displayGuiScreen(new GuiYesNo(this, new TextComponentTranslation("gui.remove.all").getFormattedText(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 4)); break;
			case 118: {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				if (container != null) { setSubGui(new SubGuiScriptEncrypt(path, ext)); }
				break;
			}
			case 119: {
				if (activeTab > 0 ||
						get(2, GuiTextArea.class) == null ||
						!dataLog.containsKey(button.getValue()) ||
						!handler.getConsoleText().containsKey(dataLog.get(button.getValue()))) {
					return;
				}
				selectLog = dataLog.get(button.getValue());
				((GuiTextArea) get(2, GuiTextArea.class)).setText(new Date(selectLog) + handler.getConsoleText().get(selectLog));
				break;
			}
			case 120: {
				if (activeTab > 0) { return; }
				NoppesStringUtils.setClipboardContents(getTextArea(2).getText());
				break;
			} // copy log
			case 121: {
				if (activeTab > 0) { return; }
				handler.clearConsoleText(selectLog);
				initGui();
				break;
			} // clear log
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (result) {
			switch (id) {
				case 0: openLink(web_site); break;
				case 1: openLink(api_doc_site); break;
				case 2: openLink(api_site); break;
				case 3: openLink(dis_site); break;
				case 4: {
					handler.getScripts().clear();
					activeTab = 0;
					break;
				}
				case 5: {
					ScriptContainer container = handler.getScripts().get(activeTab - 1);
					container.script = "";
					break;
				}
				case 6: getTextArea(2).setText(NoppesStringUtils.getClipboardContents()); break;
				case 10: {
					handler.getScripts().remove(activeTab - 1);
					activeTab = 0;
					break;
				}
			}
		}
		displayGuiScreen(this);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (mc.world.getTotalWorldTime() % 5 == 0) {
			if (activeTab > 0) {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				boolean e = container == null || container.script.isEmpty();
				if (getButton(100) != null) { // copy
					if (getButton(100).enabled && e) { getButton(100).setIsEnable(false); }
					else if (!getButton(100).enabled && !e) { getButton(100).setIsEnable(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).enabled && e) { getButton(102).setIsEnable(false); }
					else if (!getButton(102).enabled && !e) { getButton(102).setIsEnable(true); }
				}
				if (getButton(118) != null) { // encode
					getButton(118).setIsEnable(container != null && container.hasNoEncryptScriptCode());
				}
				
				if (getButton(107) != null) { // files
					Map<String, Long> map = languages.get(Util.instance.deleteColor(handler.getLanguage()));
					e = map != null && !map.isEmpty();
					if (getButton(107).enabled && !e) { getButton(107).setIsEnable(false); }
					else if (!getButton(107).enabled && e) { getButton(107).setIsEnable(true); }
				}
				if (getButton(101) != null) { // paste
					try {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						Transferable contents = clipboard.getContents(null);
						e = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
						if (getButton(101).enabled && !e) { getButton(101).setIsEnable(false); }
						else if (!getButton(101).enabled && e) { getButton(101).setIsEnable(true); }
					}
					catch (Exception ee) { LogWriter.error(ee); }
				}
			} else {
				boolean e = handler == null || handler.getConsoleText().isEmpty();
				if (getButton(100) != null) { // copy
					if (getButton(100).enabled && e) { getButton(100).setIsEnable(false); }
					else if (!getButton(100).enabled && !e) { getButton(100).setIsEnable(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).enabled && e) { getButton(102).setIsEnable(false); }
					else if (!getButton(102).enabled && !e) { getButton(102).setIsEnable(true); }
				}
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private static String[] getLanguageData(String name) {
		String key = "ecmascript";
		String ext = ".js";
		if (name.toLowerCase().startsWith("graaljs")) { key = "graaljs"; }
		else if (name.toLowerCase().startsWith("luaj")) { key = "lua"; ext = ".lua"; }
		else if (name.toLowerCase().startsWith("jython")) { key = "jython"; ext = ".py"; }
		else if (name.toLowerCase().startsWith("jruby")) { key = "jruby"; ext = ".rb"; }
		else if (name.toLowerCase().startsWith("groovy")) { key = "groovy"; ext = ".groovy"; }
		else if (name.toLowerCase().startsWith("kotlin")) { key = "kotlin"; ext = ".kt"; }
		else if (name.toLowerCase().startsWith("rhino")) { key = "rhino"; }
		String dir = "./(world_name)/customnpcs/scripts/" + name.toLowerCase();
		if (CustomNpcs.Server != null) {
			dir = CustomNpcs.getWorldSaveDirectory().getAbsolutePath().replace("\\", "/");
			if (dir.lastIndexOf("/./") != -1) { dir = dir.substring(dir.lastIndexOf("/./") + 1); }
			dir += "/scripts/" + name.toLowerCase();
		}
		return new String[] { key, dir, ext };
	}

	private int getScriptIndex() {
		int i = 0;
		for (String language : languages.keySet()) {
			if (Util.instance.equalsDeleteColor(language, handler.getLanguage(), true)) { return i; }
			++i;
		}
		return 0;
	}

	@Override
	public void initGui() {
		super.initGui();
		wait = false;
		xSize = (int) (width * 0.88);
		ySize = (int) (height * 0.90);
		super.initGui();
		guiTop += 10;
		int y = guiTop + 5;
		GuiMenuTopButton top;
		addTopButton(top = new GuiMenuTopButton(0, guiLeft + 4, guiTop - 17, "gui.settings"));
		for (int i = 0; i < handler.getScripts().size(); ++i) {
			addTopButton(top = new GuiMenuTopButton(i + 1, top, i + 1 + ""));
		}
		if (handler.getScripts().size() < CustomNpcs.ScriptMaxTabs && !(handler instanceof ClientScriptData)) {
			addTopButton(new GuiMenuTopButton(CustomNpcs.ScriptMaxTabs + 1, top, "+"));
		}
		top = getTopButton(activeTab);
		if (top == null) {
			activeTab = 0;
			top = getTopButton(0);
		}
		top.setIsActive(true);
		if (activeTab > 0) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			GuiTextArea ta = new GuiTextArea(2, guiLeft + 5, y, xSize - 132, ySize - 10, (container == null) ? "" : container.script);
			ta.enableCodeHighlighting();
			ta.setListener(this);
			add(ta);
			int x = guiLeft + 7 + ta.width;
			addButton(new GuiNpcButton(102, x, y, 60, 20, "gui.clear"));
			addButton(new GuiNpcButton(101, x + 61, y, 60, 20, "gui.paste"));
			addButton(new GuiNpcButton(100, x, y + 21, 60, 20, "gui.copy"));
			addButton(new GuiNpcButton(105, x + 61, y + 21, 60, 20, "gui.remove"));
			addButton(new GuiNpcButton(107, x, y + 66, 80, 20, "script.loadscript"));
			addButton(new GuiNpcButton(115, x + 30, y + 43, 60, 20, "gui.remove.all"));
			GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setIsEnable(false);
			scroll.setSize(120, ySize - 120);
			scroll.guiLeft = x;
			scroll.guiTop = (y = guiTop + 93);
			if (container != null) { scroll.setList(container.scripts); }
			addScroll(scroll);
			addButton(new GuiNpcButton(118, x, y + 2 + scroll.height, 80, 20, "gui.encrypt")
					.setIsEnable(!(this instanceof GuiScriptClient))
					.setHoverText("" + (!(this instanceof GuiScriptClient))));
		} // scripts
		else {
			NavigableMap<Long, String> map = handler.getConsoleText().descendingMap();
			String log = "";
			if (!map.isEmpty()) {
				if (!map.containsKey(selectLog)) {
					for (long time : map.keySet()) {
						if (selectLog < time) { selectLog = time; }
					}
				}
				log = new Date(selectLog) + map.get(selectLog);
			}
			GuiTextArea ta = new GuiTextArea(2, guiLeft + 5, y, xSize - 175, ySize - 10, log);
			if (!map.isEmpty()) {
				ta.y += 24;
				ta.height -= 24;
			}
			ta.enabled = false;
			add(ta);
			int x = guiLeft + 7 + ta.width;
			addButton(new GuiNpcButton(100, x, guiTop + 125, 60, 20,
					map.size() < 2 ? "gui.copy" :
							new TextComponentTranslation("gui.copy").getFormattedText() + " "
							+ new TextComponentTranslation("gui.all").getFormattedText())
					.setHoverText("script.hover.log.copy.all"));
			addButton(new GuiNpcButton(102, x, guiTop + 146, 60, 20,
					map.size() < 2 ? "gui.clear" :
							new TextComponentTranslation("gui.clear").getFormattedText() + " "
									+ new TextComponentTranslation("gui.all").getFormattedText())
					.setHoverText("script.hover.log.clear.all"));
			if (map.size() > 1) {
				List<String> selects = new ArrayList<>();
				dataLog.clear();
				int i = 0;
				int pos = 0;
				for(Long key : map.keySet()) {
					dataLog.put(i, key);
					if (Objects.equals(key, selectLog)) { pos = i; }
					selects.add((i + 1) + "/" + map.size() + ": "+new Date(key));
					i++;
				}
				addButton(new GuiButtonBiDirectional(119, guiLeft + 4, guiTop + 7, ta.width, 20, selects.toArray(new String[0]), pos));
				if (map.size() > 1) {
					getButton(100).x = x + 62;
					getButton(102).x = x + 62;
					addButton(new GuiNpcButton(120, x, guiTop + 125, 60, 20, "gui.copy")
							.setHoverText("script.hover.log.copy"));
					addButton(new GuiNpcButton(121, x, guiTop + 146, 60, 20, "gui.clear")
							.setHoverText("script.hover.log.clear"));
				}
			}

			addLabel(new GuiNpcLabel(1, "script.language", x + 1, guiTop + 15));
			String[] ls = languages.keySet().toArray(new String[0]);
			if (ls.length < 1) { addButton(new GuiNpcButton(103, x + 60, guiTop + 10, 80, 20, ls, getScriptIndex())); }
			else { addButton(new GuiButtonBiDirectional(103, x + 60, guiTop + 10, 80, 20, ls, getScriptIndex())); }
			getButton(103).setIsEnable(!languages.isEmpty());
			String[] data = getLanguageData(getButton(103).getDisplayString());
			getButton(103).setHoverText(new TextComponentTranslation("script.hover.info." + data[0]).
					appendSibling(new TextComponentTranslation("script.hover.info.dir", data[1], data[2])).getFormattedText());
			addLabel(new GuiNpcLabel(3, "[?]", x + 145, guiTop + 15));
			getLabel(3).setHoverText("script.hover.info");
			addLabel(new GuiNpcLabel(2, "gui.enabled", x + 1, guiTop + 36));
			addButton(new GuiNpcButton(104, x + 60, guiTop + 31, 50, 20, new String[] { "gui.no", "gui.yes" }, (handler.getEnabled() ? 1 : 0)));
			if (player.getServer() != null) { addButton(new GuiNpcButton(106, x, guiTop + 55, 150, 20, "script.openfolder")); }
			addButton(new GuiNpcButton(109, x, guiTop + 78, 80, 20, "gui.website"));
			addButton(new GuiNpcButton(112, x + 81, guiTop + 78, 80, 20, "gui.forum"));
			addButton(new GuiNpcButton(110, x, guiTop + 99, 80, 20, "script.apidoc"));
			addButton(new GuiNpcButton(111, x + 81, guiTop + 99, 80, 20, "script.apisrc"));
		} // info
	}

	@Override
	public void save() { setScript(); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList data = compound.getTagList("Languages", 10);
		languages.clear();
		for (int i = 0; i < data.tagCount(); ++i) {
			NBTTagCompound comp = data.getCompoundTagAt(i);
			Map<String, Long> scripts = new TreeMap<>();
			NBTTagList list = comp.getTagList("Scripts", 8);
			long[] ld = new long[list.tagCount()];
			if (comp.hasKey("sizes", 12)) { ld = TagLongArrayReflection.getData((NBTTagLongArray) comp.getTag("sizes")); }
			if (ld != null) {
				for (int j = 0; j < list.tagCount(); ++j) { scripts.put(list.getStringTagAt(j), ld[j]); }
			}
			languages.put(comp.getString("Language"), scripts);
			if (Util.instance.equalsDeleteColor(comp.getString("Language"), handler.getLanguage(), false)) { ext = comp.getString("FileSfx"); }
		}
		path = compound.getString("DirPath") + "/" + handler.getLanguage().toLowerCase();
		initGui();
	}

	private void setScript() {
		if (activeTab > 0) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container == null) { handler.getScripts().add(container = new ScriptContainer(handler, true)); }
			String text = getTextArea(2).getText();
			text = text.replace("\r\n", "\n");
			text = text.replace("\r", "\n");
			container.script = text;
		}
	}

	@Override
	public void textUpdate(String text) {
		ScriptContainer container = handler.getScripts().get(activeTab - 1);
		if (container != null) { container.script = text; }
	}

	public void addScript(int tab, String codePart) {
		if (tab < 0) { return; }
		List<ScriptContainer> scripts = handler.getScripts();
		while (tab >= scripts.size()) { scripts.add(new ScriptContainer(handler, true)); }
		ScriptContainer container = scripts.get(tab);
		if (container.script == null || codePart.isEmpty()) {
			container.script = "";
			if (codePart.isEmpty()) { return; }
		}
		container.script += codePart;
	}

	public void addConsole(int tab, long time, String consolePart) {
		if (tab < 0) { return; }
		List<ScriptContainer> scripts = handler.getScripts();
		while (tab >= scripts.size()) { scripts.add(new ScriptContainer(handler, true)); }
		ScriptContainer container = scripts.get(tab);
		if (container.console == null) { container.console = new TreeMap<>();}
		if (!container.console.containsKey(time) || consolePart.isEmpty()) { container.console.put(time, ""); }
		String consoleText = container.console.get(time) + consolePart;
		container.console.put(time, consoleText);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
