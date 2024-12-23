package noppes.npcs.client.gui.script;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextChangeListener;
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
	
	protected int activeTab;
	public IScriptHandler handler;
	public Map<String, Map<String, Long>> languages = new HashMap<>();
	public String path, ext;
	private static final String web_site = "http://www.kodevelopment.nl/minecraft/customnpcs/scripting";
	private static final String api_doc_site = "https://github.com/BetaZavr/CustomNPCsAPI-Unofficial";
	private static final String api_site = "https://github.com/BetaZavr/CustomNPCsAPI-Unofficial";
	private static final String dis_site = "https://discord.gg/RGb4JqE6Qz";

	public GuiScriptInterface() {
		super();
		drawDefaultBackground = true;
		closeOnEsc = true;
		xSize = 420;
		setBackground("menubg.png");

		activeTab = 0;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (guibutton.id >= 0 && guibutton.id < CustomNpcs.ScriptMaxTabs) {
			setScript();
			activeTab = guibutton.id;
			initGui();
		}
		if (guibutton.id == CustomNpcs.ScriptMaxTabs + 1) {
			if (handler.getScripts().size() >= CustomNpcs.ScriptMaxTabs) {
				activeTab = CustomNpcs.ScriptMaxTabs;
				initGui();
				return;
			}
			handler.getScripts().add(new ScriptContainer(handler, true));
			activeTab = handler.getScripts().size();
			initGui();
		}

		if (guibutton.id == 100) {
			NoppesStringUtils.setClipboardContents(getTextArea(2).getText());
		}
		if (guibutton.id == 101) {
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
		if (guibutton.id == 102) { // clear text
			if (activeTab > 0) {
				GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 5);
				displayGuiScreen(guiyesno);
			} else {
				handler.clearConsole();
			}
			initGui();
		}
		if (guibutton.id == 103) {
			handler.setLanguage(guibutton.displayString);
		}
		if (guibutton.id == 104) {
			handler.setEnabled(((GuiNpcButton) guibutton).getValue() == 1);
		}
		if (guibutton.id == 105) {
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 10);
			displayGuiScreen(guiyesno);
		}
		if (guibutton.id == 106) {
			NoppesUtil.openFolder(ScriptController.Instance.dir);
		}
		if (guibutton.id == 107) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container == null) {
				handler.getScripts().add(container = new ScriptContainer(handler, true));
			}
			setSubGui(new GuiScriptList(languages.get(Util.instance.deleteColor(handler.getLanguage())), container));
		}
		if (guibutton.id == 108) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container != null) {
				setScript();
			}
		}
		if (guibutton.id == 109) {
			displayGuiScreen(new GuiConfirmOpenLink(this, web_site , 0, true));
		}
		if (guibutton.id == 110) {
			displayGuiScreen(new GuiConfirmOpenLink(this, api_doc_site, 1, true));
		}
		if (guibutton.id == 111) {
			displayGuiScreen(new GuiConfirmOpenLink(this, api_site, 2, true));
		}
		if (guibutton.id == 112) {
			displayGuiScreen(new GuiConfirmOpenLink(this, dis_site, 3, true));
		}
		if (guibutton.id == 115) {
			GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.remove.all").getFormattedText(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 4);
			displayGuiScreen(guiyesno);
		}
		if (guibutton.id == 118) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			if (container != null) {
				setSubGui(new GuiScriptEncrypt(path, ext));
			}
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
					if (getButton(100).enabled && e) { getButton(100).setEnabled(false); }
					else if (!getButton(100).enabled && !e) { getButton(100).setEnabled(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).enabled && e) { getButton(102).setEnabled(false); }
					else if (!getButton(102).enabled && !e) { getButton(102).setEnabled(true); }
				}
				if (getButton(118) != null) { // encode
					getButton(118).setEnabled(container != null && container.hasNoEncryptScriptCode());
				}
				
				if (getButton(107) != null) { // files
					Map<String, Long> map = languages.get(Util.instance.deleteColor(handler.getLanguage()));
					e = map != null && !map.isEmpty();
					if (getButton(107).enabled && !e) { getButton(107).setEnabled(false); }
					else if (!getButton(107).enabled && e) { getButton(107).setEnabled(true); }
				}
				if (getButton(101) != null) { // paste
					try {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						Transferable contents = clipboard.getContents(null);
						e = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
						if (getButton(101).enabled && !e) { getButton(101).setEnabled(false); }
						else if (!getButton(101).enabled && e) { getButton(101).setEnabled(true); }
					}
					catch (Exception ee) { LogWriter.error("Error:", ee); }
				}
			} else {
				boolean e = handler == null || handler.getConsoleText().isEmpty();
				if (getButton(100) != null) { // copy
					if (getButton(100).enabled && e) { getButton(100).setEnabled(false); }
					else if (!getButton(100).enabled && !e) { getButton(100).setEnabled(true); }
				}
				if (getButton(102) != null) { // clear
					if (getButton(102).enabled && e) { getButton(102).setEnabled(false); }
					else if (!getButton(102).enabled && !e) { getButton(102).setEnabled(true); }
				}
			}
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		}
	}

	private String getConsoleText() {
		Map<Long, String> map = handler.getConsoleText();
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Long, String> entry : map.entrySet()) {
			builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n");
		}
		return builder.toString();
	}

	private int getScriptIndex() {
		int i = 0;
		for (String language : languages.keySet()) {
			if (language.equalsIgnoreCase(handler.getLanguage())) {
				return i;
			}
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
		top.active = true;
		if (activeTab > 0) {
			ScriptContainer container = handler.getScripts().get(activeTab - 1);
			GuiTextArea ta = new GuiTextArea(2, guiLeft + 1 + yoffset, guiTop + yoffset, xSize - 108 - yoffset, (int) ((ySize * 0.96) - yoffset * 2), (container == null) ? "" : container.script);
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
			GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setUnSelectable();
			scroll.setSize(100, (int) ((ySize * 0.54) - yoffset * 2) - 22);
			scroll.guiLeft = left;
			scroll.guiTop = guiTop + 88 + yoffset;
			if (container != null) {
				scroll.setList(container.scripts);
			}
			addScroll(scroll);
			GuiNpcButton button = new GuiNpcButton(118, left, guiTop + 90 + yoffset + scroll.height, 80, 20, "gui.encrypt");
			button.setEnabled(!(this instanceof GuiScriptClient));
			button.setHoverText("" + button.enabled);
			addButton(button);
		} else {
			GuiTextArea ta2 = new GuiTextArea(2, guiLeft + 4 + yoffset, guiTop + 6 + yoffset, xSize - 160 - yoffset, (int) ((ySize * 0.92f) - yoffset * 2), getConsoleText());
			ta2.enabled = false;
			add(ta2);
			int left2 = guiLeft + xSize - 150;
			addButton(new GuiNpcButton(100, left2, guiTop + 125, 60, 20, "gui.copy"));
			addButton(new GuiNpcButton(102, left2, guiTop + 146, 60, 20, "gui.clear"));
			addLabel(new GuiNpcLabel(1, "script.language", left2, guiTop + 15));
			addButton(new GuiNpcButton(103, left2 + 60, guiTop + 10, 80, 20, languages.keySet().toArray(new String[0]), getScriptIndex()));
			getButton(103).enabled = (!languages.isEmpty());
			addLabel(new GuiNpcLabel(2, "gui.enabled", left2, guiTop + 36));
			addButton(new GuiNpcButton(104, left2 + 60, guiTop + 31, 50, 20, new String[] { "gui.no", "gui.yes" }, (handler.getEnabled() ? 1 : 0)));
			if (player.getServer() != null) {
				addButton(new GuiNpcButton(106, left2, guiTop + 55, 150, 20, "script.openfolder"));
			}
			addButton(new GuiNpcButton(109, left2, guiTop + 78, 80, 20, "gui.website"));
			addButton(new GuiNpcButton(112, left2 + 81, guiTop + 78, 80, 20, "gui.forum"));
			addButton(new GuiNpcButton(110, left2, guiTop + 99, 80, 20, "script.apidoc"));
			addButton(new GuiNpcButton(111, left2 + 81, guiTop + 99, 80, 20, "script.apisrc"));
		}
		xSize = 420;
		ySize = 256;
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
