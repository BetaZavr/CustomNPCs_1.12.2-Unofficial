package noppes.npcs.client.gui.script;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.ObfuscationHelper;

public class GuiScriptInterface
extends GuiNPCInterface
implements IGuiData, ITextChangeListener
{
	protected int activeTab;
	public IScriptHandler handler;
	public Map<String, Map<String, Long>> languages;
	public String path, ext;
	
	public GuiScriptInterface() {
		this.activeTab = 0;
		this.languages = new HashMap<String, Map<String, Long>>();
		this.drawDefaultBackground = true;
		this.closeOnEsc = true;
		this.xSize = 420;
		this.setBackground("menubg.png");
	}
	
	@Override
	public void initGui() {
		this.xSize = (int) (this.width * 0.88);
		this.ySize = (int) (this.xSize * 0.56);
		if (this.ySize > this.height * 0.95) {
			this.ySize = (int) (this.height * 0.95);
			this.xSize = (int) (this.ySize / 0.56);
		}
		this.bgScale = this.xSize / 400.0f;
		super.initGui();
		this.guiTop += 10;
		int yoffset = (int) (this.ySize * 0.02);
		GuiMenuTopButton top;
		this.addTopButton(top = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.settings"));
		for (int i = 0; i < this.handler.getScripts().size(); ++i) {
			this.addTopButton(top = new GuiMenuTopButton(i + 1, top, i + 1 + ""));
		}
		if (this.handler.getScripts().size() < CustomNpcs.scriptMaxTabs) {
			this.addTopButton(top = new GuiMenuTopButton(CustomNpcs.scriptMaxTabs + 1, top, "+"));
		}
		top = this.getTopButton(this.activeTab);
		if (top == null) {
			this.activeTab = 0;
			top = this.getTopButton(0);
		}
		top.active = true;
		if (this.activeTab > 0) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			GuiTextArea ta = new GuiTextArea(2, this, this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 108 - yoffset, (int) ((this.ySize * 0.96) - yoffset * 2), (container == null) ? "" : container.script);
			ta.enableCodeHighlighting();
			ta.setListener(this);
			this.add(ta);
			int left = this.guiLeft + this.xSize - 104;
			this.addButton(new GuiNpcButton(102, left, this.guiTop + yoffset, 60, 20, "gui.clear"));
			this.addButton(new GuiNpcButton(101, left + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
			this.addButton(new GuiNpcButton(100, left, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
			this.addButton(new GuiNpcButton(105, left + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));
			this.addButton(new GuiNpcButton(107, left, this.guiTop + 66 + yoffset, 80, 20, "script.loadscript"));
			this.addButton(new GuiNpcButton(115, left + 30, this.guiTop + 43 + yoffset, 60, 20, "gui.remove.all"));
			GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setUnselectable();
			scroll.setSize(100, (int) ((this.ySize * 0.54) - yoffset * 2) - 22);
			scroll.guiLeft = left;
			scroll.guiTop = this.guiTop + 88 + yoffset;
			if (container != null) {
				scroll.setList(container.scripts);
			}
			this.addScroll(scroll);
			this.addButton(new GuiNpcButton(118, left, this.guiTop + 90 + yoffset + scroll.height, 80, 20, "gui.encrypt"));
		}
		else {
			GuiTextArea ta2 = new GuiTextArea(2, this, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset, this.xSize - 160 - yoffset, (int) ((this.ySize * 0.92f) - yoffset * 2), this.getConsoleText());
			ta2.enabled = false;
			this.add(ta2);
			int left2 = this.guiLeft + this.xSize - 150;
			this.addButton(new GuiNpcButton(100, left2, this.guiTop + 125, 60, 20, "gui.copy"));
			this.addButton(new GuiNpcButton(102, left2, this.guiTop + 146, 60, 20, "gui.clear"));
			this.addLabel(new GuiNpcLabel(1, "script.language", left2, this.guiTop + 15));
			this.addButton(new GuiNpcButton(103, left2 + 60, this.guiTop + 10, 80, 20, this.languages.keySet().toArray(new String[this.languages.keySet().size()]), this.getScriptIndex()));
			this.getButton(103).enabled = (this.languages.size() > 0);
			this.addLabel(new GuiNpcLabel(2, "gui.enabled", left2, this.guiTop + 36));
			this.addButton(new GuiNpcButton(104, left2 + 60, this.guiTop + 31, 50, 20, new String[] { "gui.no", "gui.yes" }, (this.handler.getEnabled() ? 1 : 0)));
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
	}
	
	private String getConsoleText() {
		Map<Long, String> map = this.handler.getConsoleText();
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Long, String> entry : map.entrySet()) {
			builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n");
		}
		return builder.toString();
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
			if (i == 4) {
				this.handler.getScripts().clear();
				this.activeTab = 0;
			}
			if (i == 10) {
				this.handler.getScripts().remove(this.activeTab - 1);
				this.activeTab = 0;
			}
		}
		this.displayGuiScreen(this);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id >= 0 && guibutton.id < CustomNpcs.scriptMaxTabs) {
			this.setScript();
			this.activeTab = guibutton.id;
			this.initGui();
		}
		if (guibutton.id == CustomNpcs.scriptMaxTabs + 1) {
			if (this.handler.getScripts().size() >= CustomNpcs.scriptMaxTabs) {
				this.activeTab = CustomNpcs.scriptMaxTabs;
				this.initGui();
				return;
			}
			this.handler.getScripts().add(new ScriptContainer(this.handler, true));
			this.activeTab = this.handler.getScripts().size();
			this.initGui();
		}

		if (guibutton.id == 100) {
			NoppesStringUtils.setClipboardContents(((GuiTextArea)this.get(2)).getText());
		}
		if (guibutton.id == 101) {
			((GuiTextArea)this.get(2)).setText(NoppesStringUtils.getClipboardContents());
		}
		if (guibutton.id == 102) {
			if (this.activeTab > 0) {
				ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
				container.script = "";
			}
			else {
				this.handler.clearConsole();
			}
			this.initGui();
		}
		if (guibutton.id == 103) {
			this.handler.setLanguage(((GuiNpcButton)guibutton).displayString);
		}
		if (guibutton.id == 104) {
			this.handler.setEnabled(((GuiNpcButton)guibutton).getValue() == 1);
		}
		if (guibutton.id == 105) {
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 10);
			this.displayGuiScreen(guiyesno);
		}
		if (guibutton.id == 106) {
			NoppesUtil.openFolder(ScriptController.Instance.dir);
		}
		if (guibutton.id == 107) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container == null) {
				this.handler.getScripts().add(container = new ScriptContainer(this.handler, true));
			}
			this.setSubGui(new GuiScriptList(this.languages.get(AdditionalMethods.instance.deleteColor(this.handler.getLanguage())), container));
		}
		if (guibutton.id == 108) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container != null) { this.setScript(); }
		}
		if (guibutton.id == 109) {
			this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.kodevelopment.nl/minecraft/customnpcs/scripting", 0, true));
		}
		if (guibutton.id == 110) {
			this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.kodevelopment.nl/customnpcs/api/", 1, true));
		}
		if (guibutton.id == 111) {
			this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/Noppes/CustomNPCsAPI", 2, true));
		}
		if (guibutton.id == 112) {
			this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.minecraftforge.net/forum/index.php/board,122.0.html", 3, true));
		}
		if (guibutton.id == 115) {
			GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.remove.all").getFormattedText(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 4);
			this.displayGuiScreen(guiyesno);
		}
		if (guibutton.id == 118) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container != null) {
				this.setSubGui(new GuiScriptEncrypt(this.path, this.ext));
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.getButton(118) != null) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			this.getButton(118).setEnabled(container != null && !container.getFullCode().isEmpty());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		}
	}
	
	private void setScript() {
		if (this.activeTab > 0) {
			ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
			if (container == null) {
				this.handler.getScripts().add(container = new ScriptContainer(this.handler, true));
			}
			String text = ((GuiTextArea)this.get(2)).getText();
			text = text.replace("\r\n", "\n");
			text = text.replace("\r", "\n");
			container.script = text;
		}
	}
	
	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList data = compound.getTagList("Languages", 10);
		Map<String, Map<String, Long>> languages = new HashMap<String, Map<String, Long>>();
		for (int i = 0; i < data.tagCount(); ++i) {
			NBTTagCompound comp = data.getCompoundTagAt(i);
			Map<String, Long> scripts = Maps.<String, Long>newTreeMap();
			NBTTagList list = comp.getTagList("Scripts", 8);
			long[] ld = new long[list.tagCount()]; 
			if (comp.hasKey("sizes", 12)) { ld = ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) comp.getTag("sizes"), 0); }
			for (int j = 0; j < list.tagCount(); ++j) {
				scripts.put(list.getStringTagAt(j), ld[j]);
			}
			languages.put(comp.getString("Language"), scripts);
			if (AdditionalMethods.equalsDeleteColor(comp.getString("Language"), this.handler.getLanguage(), false)) { this.ext = comp.getString("FileSfx"); }
		}
		this.path = compound.getString("DirPath")+"/"+this.handler.getLanguage().toLowerCase();
		this.languages = languages;
		this.initGui();
	}
	
	@Override
	public void save() {
		this.setScript();
	}
	
	@Override
	public void textUpdate(String text) {
		ScriptContainer container = this.handler.getScripts().get(this.activeTab - 1);
		if (container != null) {
			container.script = text;
		}
	}
	
}
