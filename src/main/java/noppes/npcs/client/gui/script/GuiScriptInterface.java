package noppes.npcs.client.gui.script;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.api.mixin.nbt.INBTTagLongArrayMixin;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiScriptInterface
extends GuiNPCInterface
implements IGuiData, ITextChangeListener {

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


	public GuiScriptInterface() {
		super();
		drawDefaultBackground = true;
		closeOnEsc = true;
		xSize = 420;
		setBackground("menubg.png");

		activeTab = 0;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) {
		if (button.id >= 0 && button.id < CustomNpcs.ScriptMaxTabs) {
			setScript();
			activeTab = button.id;
			initGui();
		}
		if (button.id == CustomNpcs.ScriptMaxTabs + 1) {
			if (handler.getScripts().size() >= CustomNpcs.ScriptMaxTabs) {
				activeTab = CustomNpcs.ScriptMaxTabs;
				initGui();
				return;
			}
			handler.getScripts().add(new ScriptContainer(handler, true));
			activeTab = handler.getScripts().size();
			initGui();
		}
		if (button.id == 100) { // copy all logs
			Map<Long, String> map = handler.getConsoleText();
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<Long, String> entry : map.entrySet()) {
				builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n\n");
			}
			NoppesStringUtils.setClipboardContents(builder.toString());
		}
		if (button.id == 101) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container != null) {
				boolean sr = !(container.script == null || container.script.isEmpty());
				if (sr) {
					String tempScript = container.script.replace(" ", "").replace("" + ((char) 9), "").replace("" + ((char) 10), "");
					sr = !tempScript.isEmpty();
				}
				if (sr) {
					GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.replaceMessage").getFormattedText(), 6);
					displayGuiScreen(guiyesno);
					return;
				}
			}
			getTextArea(2).setText(NoppesStringUtils.getClipboardContents());
		}
		if (button.id == 102) { // clear all logs
			if (activeTab > 0) {
				GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 5);
				displayGuiScreen(guiyesno);
			} else {
				handler.clearConsole();
			}
			initGui();
		}
		if (button.id == 103) {
			handler.setLanguage(button.displayString);
			String[] data = getLanguageData(button.displayString);
			((GuiNpcButton) button).setHoverText(new TextComponentTranslation("script.hover.info." + data[0]).
					appendSibling(new TextComponentTranslation("script.hover.info.dir", data[1], data[2])).getFormattedText());
		}
		if (button.id == 104) {
			handler.setEnabled(((GuiNpcButton) button).getValue() == 1);
		}
		if (button.id == 105) {
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 10);
			displayGuiScreen(guiyesno);
		}
		if (button.id == 106) {
			NoppesUtil.openFolder(ScriptController.Instance.dir);
		}
		if (button.id == 107) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container == null) { handler.getScripts().add(container = new ScriptContainer(handler, true)); }
			setSubGui(new GuiScriptList(languages.get(Util.instance.deleteColor(handler.getLanguage())), container));
		}
		if (button.id == 108) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container != null) { setScript(); }
		}
		if (button.id == 109) {
			displayGuiScreen(new GuiConfirmOpenLink(this, web_site , 0, true));
		}
		if (button.id == 110) {
			displayGuiScreen(new GuiConfirmOpenLink(this, api_doc_site, 1, true));
		}
		if (button.id == 111) {
			displayGuiScreen(new GuiConfirmOpenLink(this, api_site, 2, true));
		}
		if (button.id == 112) {
			displayGuiScreen(new GuiConfirmOpenLink(this, dis_site, 3, true));
		}
		if (button.id == 115) {
			GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.remove.all").getFormattedText(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 4);
			displayGuiScreen(guiyesno);
		}
		if (button.id == 118) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container != null) {
				setSubGui(new GuiScriptEncrypt(path, ext));
			}
		}
		if (button.id == 119) {
			if (activeTab > 0 ||
					!(get(2) instanceof GuiTextArea) ||
					!dataLog.containsKey(((GuiNpcButton) button).getValue()) ||
					!handler.getConsoleText().containsKey(dataLog.get(((GuiNpcButton) button).getValue()))) { return; }
			selectLog = dataLog.get(((GuiNpcButton) button).getValue());
			((GuiTextArea) get(2)).setText(new Date(selectLog) + handler.getConsoleText().get(selectLog));
		}
		if (button.id == 120) { // copy log
			if (activeTab > 0) { return; }
			NoppesStringUtils.setClipboardContents(getTextArea(2).getText());
		}
		if (button.id == 121) { // clear log
			if (activeTab > 0) { return; }
			handler.clearConsoleText(selectLog);
			initGui();
		}
	}
	
	@Override
	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			if (i == 0) { openLink(web_site); }
			else if (i == 1) { openLink(api_doc_site); }
			else if (i == 2) { openLink(api_site); }
			else if (i == 3) { openLink(dis_site); }
			else if (i == 4) {
				handler.getScripts().clear();
				activeTab = 0;
			}
			else if (i == 5) {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				container.script = "";
			}
			else if (i == 6) {
				getTextArea(2).setText(NoppesStringUtils.getClipboardContents());
			}
			else if (i == 10) {
				handler.getScripts().remove(activeTab - 1);
				activeTab = 0;
			}
		}
		displayGuiScreen(this);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (mc.world.getTotalWorldTime()%5 == 0) {
			if (activeTab > 0) {
				ScriptContainer container = handler.getScripts().get(activeTab - 1);
				boolean e = container == null || container.script.isEmpty();
				if (getButton(100) != null) { // copy
					if (getButton(100).isEnabled() && e) { getButton(100).setEnabled(false); }
					else if (!getButton(100).isEnabled() && !e) { getButton(100).setEnabled(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).isEnabled() && e) { getButton(102).setEnabled(false); }
					else if (!getButton(102).isEnabled() && !e) { getButton(102).setEnabled(true); }
				}
				if (getButton(118) != null) { // encode
					getButton(118).setEnabled(container != null && container.hasNoEncryptScriptCode());
				}
				
				if (getButton(107) != null) { // files
					Map<String, Long> map = languages.get(Util.instance.deleteColor(handler.getLanguage()));
					e = map != null && !map.isEmpty();
					if (getButton(107).isEnabled() && !e) { getButton(107).setEnabled(false); }
					else if (!getButton(107).isEnabled() && e) { getButton(107).setEnabled(true); }
				}
				if (getButton(101) != null) { // paste
					try {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						Transferable contents = clipboard.getContents(null);
						e = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
						if (getButton(101).isEnabled() && !e) { getButton(101).setEnabled(false); }
						else if (!getButton(101).isEnabled() && e) { getButton(101).setEnabled(true); }
					}
					catch (Exception ee) { LogWriter.error("Error:", ee); }
				}
			} else {
				boolean e = handler == null || handler.getConsoleText().isEmpty();
				if (getButton(100) != null) { // copy
					if (getButton(100).isEnabled() && e) { getButton(100).setEnabled(false); }
					else if (!getButton(100).isEnabled() && !e) { getButton(100).setEnabled(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).isEnabled() && e) { getButton(102).setEnabled(false); }
					else if (!getButton(102).isEnabled() && !e) { getButton(102).setEnabled(true); }
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
		xSize = (int) (width * 0.88);
		ySize = (int) (xSize * 0.56);
		if (ySize > height * 0.95) {
			ySize = (int) (height * 0.95);
			xSize = (int) (ySize / 0.56);
		}
		bgScale = xSize / 400.0f;
		super.initGui();
		guiTop += 10;
		int yoffset = (int) (ySize * 0.02);
		GuiNpcButton button;
		GuiMenuTopButton top;
		addTopButton(top = new GuiMenuTopButton(0, guiLeft + 4, guiTop - 17, "gui.settings"));
		for (int i = 0; i < handler.getScripts().size(); ++i) {
			addTopButton(top = new GuiMenuTopButton(i + 1, top, i + 1 + ""));
		}
		if (handler.getScripts().size() < CustomNpcs.ScriptMaxTabs && !(handler instanceof ClientScriptData)) {
			addTopButton(new GuiMenuTopButton(CustomNpcs.ScriptMaxTabs + 1, top, "+"));
		}
		top = (GuiMenuTopButton) getTopButton(activeTab);
		if (top == null) {
			activeTab = 0;
			top = (GuiMenuTopButton) getTopButton(0);
		}
		top.active = true;
		if (activeTab > 0) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			GuiTextArea ta = new GuiTextArea(2, guiLeft + 1 + yoffset, guiTop + 1 + yoffset, xSize - 108 - yoffset, (int) ((ySize * 0.96) - yoffset * 2), (container == null) ? "" : container.script);
			ta.enableCodeHighlighting();
			ta.setListener(this);
			add(ta);
			int left = guiLeft + xSize - 104;
			addButton(new GuiNpcButton(102, left, guiTop + yoffset, 60, 20, "gui.clear"));
			addButton(new GuiNpcButton(101, left + 61, guiTop + yoffset, 60, 20, "gui.paste"));
			addButton(new GuiNpcButton(100, left, guiTop + 21 + yoffset, 60, 20, "gui.copy"));
			addButton(new GuiNpcButton(105, left + 61, guiTop + 21 + yoffset, 60, 20, "gui.remove"));
			addButton(new GuiNpcButton(107, left, guiTop + 66 + yoffset, 80, 20, "script.loadscript"));
			addButton(new GuiNpcButton(115, left + 30, guiTop + 43 + yoffset, 60, 20, "gui.remove.all"));
			GuiCustomScroll scroll = (GuiCustomScroll) (new GuiCustomScroll(this, 0)).setUnSelectable();
			scroll.setSize(100, (int) ((ySize * 0.54) - yoffset * 2) - 22);
			scroll.guiLeft = left;
			scroll.guiTop = guiTop + 88 + yoffset;
			if (container != null) {
				scroll.setList(container.scripts);
			}
			addScroll(scroll);
			addButton(button = new GuiNpcButton(118, left, guiTop + 90 + yoffset + scroll.height, 80, 20, "gui.encrypt"));
			button.setEnabled(!(this instanceof GuiScriptClient));
			button.setHoverText("" + button.enabled);
		} // script
		else { // info
			TreeMap<Long, String> map = handler.getConsoleText();
			String log = "";
			if (!map.isEmpty()) {
				if (!map.containsKey(selectLog)) { selectLog = map.firstKey(); }
				log = new Date(selectLog) + map.get(selectLog);
			}
			GuiTextArea ta = new GuiTextArea(2, guiLeft + 4, guiTop + 7 + yoffset + (map.size() > 1 ? 22 : 0), xSize - 160, (int) ((ySize * 0.92f) - yoffset * 2) - (map.size() > 1 ? 22 : 0), log);
			ta.enabled = false;
			add(ta);
			int left = guiLeft + xSize - 150;
			addButton(button = new GuiNpcButton(100, left, guiTop + 125, 60, 20,
					map.size() < 2 ? "gui.copy" :
							new TextComponentTranslation("gui.copy").getFormattedText() + " "
							+ new TextComponentTranslation("gui.all").getFormattedText()));
			button.setHoverText("script.hover.log.copy.all");

			addButton(button = new GuiNpcButton(102, left, guiTop + 146, 60, 20,
					map.size() < 2 ? "gui.clear" :
							new TextComponentTranslation("gui.clear").getFormattedText() + " "
									+ new TextComponentTranslation("gui.all").getFormattedText()));
			button.setHoverText("script.hover.log.clear.all");

			if (map.size() > 1) {
				List<String> selects = new ArrayList<>();
				dataLog.clear();
				int i = 0;
				int pos = 0;
				for(Long key : map.keySet()) {
					dataLog.put(i, key);
					if (Objects.equals(key, selectLog)) { pos = i; }
					selects.add((i + 1) + ": "+new Date(key));
					i++;
				}
				addButton(new GuiButtonBiDirectional(119, guiLeft + 4, guiTop + 7 + yoffset, xSize - 160, 20, selects.toArray(new String[0]), pos));
				if (map.size() > 1) {
					getButton(100).setLeft(left + 62);
					getButton(102).setLeft(left + 62);
					addButton(button = new GuiNpcButton(120, left, guiTop + 125, 60, 20, "gui.copy"));
					button.setHoverText("script.hover.log.copy");
					addButton(button = new GuiNpcButton(121, left, guiTop + 146, 60, 20, "gui.clear"));
					button.setHoverText("script.hover.log.clear");
				}
			}

			addLabel(new GuiNpcLabel(1, "script.language", left, guiTop + 15));
			String[] ls = languages.keySet().toArray(new String[0]);
			if (ls.length < 1) { addButton(new GuiNpcButton(103, left + 60, guiTop + 10, 80, 20, ls, getScriptIndex())); }
			else { addButton(new GuiButtonBiDirectional(103, left + 60, guiTop + 10, 80, 20, ls, getScriptIndex())); }
			getButton(103).setEnabled(!languages.isEmpty());

			String[] data = getLanguageData(getButton(103).getDisplayString());
			getButton(103).setHoverText(new TextComponentTranslation("script.hover.info." + data[0]).
					appendSibling(new TextComponentTranslation("script.hover.info.dir", data[1], data[2])).getFormattedText());

			addLabel(new GuiNpcLabel(3, "[?]", left + 145, guiTop + 15));
			getLabel(3).setHoverText("script.hover.info");

			addLabel(new GuiNpcLabel(2, "gui.enabled", left, guiTop + 36));
			addButton(new GuiNpcButton(104, left + 60, guiTop + 31, 50, 20, new String[] { "gui.no", "gui.yes" }, (handler.getEnabled() ? 1 : 0)));
			if (player.getServer() != null) {
				addButton(new GuiNpcButton(106, left, guiTop + 55, 150, 20, "script.openfolder"));
			}
			addButton(new GuiNpcButton(109, left, guiTop + 78, 80, 20, "gui.website"));
			addButton(new GuiNpcButton(112, left + 81, guiTop + 78, 80, 20, "gui.forum"));
			addButton(new GuiNpcButton(110, left, guiTop + 99, 80, 20, "script.apidoc"));
			addButton(new GuiNpcButton(111, left + 81, guiTop + 99, 80, 20, "script.apisrc"));
		}
		xSize = 420;
		ySize = 256;
		if (getButton(1) != null && getButton(1).isMouseOver()) { setHoverText("animation.hover.anim.del"); }
	}

	@Override
	public void save() {
		setScript();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList data = compound.getTagList("Languages", 10);
		languages.clear();
		for (int i = 0; i < data.tagCount(); ++i) {
			NBTTagCompound comp = data.getCompoundTagAt(i);
			Map<String, Long> scripts = new TreeMap<>();
			NBTTagList list = comp.getTagList("Scripts", 8);
			long[] ld = new long[list.tagCount()];
			if (comp.hasKey("sizes", 12)) { ld = ((INBTTagLongArrayMixin) comp.getTag("sizes")).npcs$getData(); }
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

}
