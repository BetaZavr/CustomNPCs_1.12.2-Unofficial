package noppes.npcs.client.gui;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuLeftButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.constants.EnumInterfaceData;
import noppes.npcs.util.AdditionalMethods;

public class GuiHelpBook
extends GuiNPCInterface
implements ICustomScrollListener {
	
	int activeTopTab = 0;
	int activeLeftTab = 0;
	private static final Map<Integer, String> map = Maps.<Integer, String>newHashMap();
	private final String[] arr = new String [] { "config", "blocks", "items", "potions", "", "", "", "", "", "",
			"npc.display", "npc.stats", "npc.ais", "npc.inventory", "npc.advanced", "", "", "", "", "", 
			"new.items", "new.blocks", "", "", "", "", "", "", "", "", "", 
			"script.main", "", "", "", "", "", "", "", "" };
	private static final Map<String, EnumInterfaceData> apis = Maps.<String, EnumInterfaceData>newTreeMap();
	private GuiCustomScroll scroll;
	private int curentLine = -1;
	private String curentLang = "";
	
	static {
		GuiHelpBook.apis.put("IBlock", EnumInterfaceData.IBlock);
		GuiHelpBook.apis.put("IBlockFluidContainer", EnumInterfaceData.IBlockFluidContainer);
		GuiHelpBook.apis.put("IBlockScripted", EnumInterfaceData.IBlockScripted);
		GuiHelpBook.apis.put("IBlockScriptedDoor", EnumInterfaceData.IBlockScriptedDoor);
		GuiHelpBook.apis.put("ICustmBlock", EnumInterfaceData.ICustmBlock);
		GuiHelpBook.apis.put("ITextPlane", EnumInterfaceData.ITextPlane);
		GuiHelpBook.apis.put("IContainer", EnumInterfaceData.IContainer);
		GuiHelpBook.apis.put("IContainerCustomChest", EnumInterfaceData.IContainerCustomChest);
		GuiHelpBook.apis.put("IDamageSource", EnumInterfaceData.IDamageSource);
		GuiHelpBook.apis.put("IDimension", EnumInterfaceData.IDimension);
		GuiHelpBook.apis.put("IEntityDamageSource", EnumInterfaceData.IEntityDamageSource);
		GuiHelpBook.apis.put("INbt", EnumInterfaceData.INbt);
		GuiHelpBook.apis.put("IPos", EnumInterfaceData.IPos);
		GuiHelpBook.apis.put("IPotion", EnumInterfaceData.IPotion);
		GuiHelpBook.apis.put("IRayTrace", EnumInterfaceData.IRayTrace);
		GuiHelpBook.apis.put("IScoreboard", EnumInterfaceData.IScoreboard);
		GuiHelpBook.apis.put("IScoreboardObjective", EnumInterfaceData.IScoreboardObjective);
		GuiHelpBook.apis.put("IScoreboardScore", EnumInterfaceData.IScoreboardScore);
		GuiHelpBook.apis.put("IScoreboardTeam", EnumInterfaceData.IScoreboardTeam);
		GuiHelpBook.apis.put("ITimers", EnumInterfaceData.ITimers);
		GuiHelpBook.apis.put("IWorld", EnumInterfaceData.IWorld);
		GuiHelpBook.apis.put("NpcAPI", EnumInterfaceData.NpcAPI);
	}
	
	public GuiHelpBook() {
		this.xSize = 300;
		this.ySize = 174;
		this.closeOnEsc = true;
		if (GuiHelpBook.map.isEmpty() || !this.curentLang.equals(ClientProxy.playerData.hud.getCurrentLanguage())) {
			this.curentLang = ClientProxy.playerData.hud.getCurrentLanguage();
			char chr = Character.toChars(0x000A)[0];
			GuiHelpBook.map.clear();
			for (int i=0; i< this.arr.length; i++) {
				if (this.arr[i].isEmpty()) {
					GuiHelpBook.map.put(i, this.arr[i]);
					continue;
				}
				String text = new TextComponentTranslation("help.info."+this.arr[i]).getFormattedText();
				while(text.indexOf("<br>")!=-1) { text = text.replaceAll("<br>", ""+chr); }
				GuiHelpBook.map.put(i, AdditionalMethods.deleteColor(text));
			}
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int maxW = 0, w = 0;
		switch(this.activeTopTab) {
			case 1: { // npcEdit
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("menu.display").getFormattedText());
				maxW = w;
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("menu.stats").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("menu.ai").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("menu.inventory").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("menu.advanced").getFormattedText());
				if (maxW<w) { maxW = w; }
				break;
			}
			case 2: { // news
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("inv.drops").getFormattedText());
				maxW = w;
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.blocks").getFormattedText());
				if (maxW<w) { maxW = w; }
				break;
			}
			case 3: { // scripts
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.general").getFormattedText());
				maxW = w;
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.api").getFormattedText());
				if (maxW<w) { maxW = w; }
				if (this.activeLeftTab==31 && maxW<110) { maxW = 110; }
				break;
			}
			default: { // general
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.config").getFormattedText());
				maxW = w;
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.blocks").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.items").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.potions").getFormattedText());
				if (maxW<w) { maxW = w; }
			}
		}
		ScaledResolution sr = new ScaledResolution(this.mc);
		this.xSize = (int) (sr.getScaledWidth_double() - maxW - 40);
		this.ySize = (int) (sr.getScaledHeight_double() - 60);
		this.width = this.xSize;
		this.height = this.ySize;
		this.guiLeft = 20 + maxW;
		this.guiTop = 30;
		
		GuiMenuTopButton general = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.help.general");
		GuiMenuTopButton npcEdit = new GuiMenuTopButton(1, general.x + general.getWidth(), guiTop - 17, "gui.help.npc");
		GuiMenuTopButton news = new GuiMenuTopButton(2, npcEdit.x + npcEdit.getWidth(), guiTop - 17, "gui.help.new");
		GuiMenuTopButton scripts = new GuiMenuTopButton(3, news.x + news.getWidth(), guiTop - 17, "gui.help.scripts");
		general.active = general.id==this.activeTopTab;
		npcEdit.active = npcEdit.id==this.activeTopTab;
		news.active = news.id==this.activeTopTab;
		scripts.active = scripts.id==this.activeTopTab;
		this.addTopButton(general);
		this.addTopButton(npcEdit);
		this.addTopButton(news);
		this.addTopButton(scripts);
		
		switch(this.activeTopTab) {
			case 1: { // npcEdit
				if (this.activeLeftTab<10 || this.activeLeftTab>19) { this.activeLeftTab = 10; }
				GuiMenuLeftButton display = new GuiMenuLeftButton(10, this.guiLeft, this.guiTop + 4, "menu.display");
				GuiMenuLeftButton stats = new GuiMenuLeftButton(11, this.guiLeft, display.y+display.getHeight(), "menu.stats");
				GuiMenuLeftButton ais = new GuiMenuLeftButton(12, this.guiLeft, stats.y+stats.getHeight(), "menu.ai");
				GuiMenuLeftButton inventory = new GuiMenuLeftButton(13, this.guiLeft, ais.y+ais.getHeight(), "menu.inventory");
				GuiMenuLeftButton advanced = new GuiMenuLeftButton(14, this.guiLeft, inventory.y+inventory.getHeight(), "menu.advanced");
				display.active = display.id==this.activeLeftTab;
				stats.active = stats.id==this.activeLeftTab;
				ais.active = ais.id==this.activeLeftTab;
				inventory.active = inventory.id==this.activeLeftTab;
				advanced.active = advanced.id==this.activeLeftTab;
				this.addLeftButton(display);
				this.addLeftButton(stats);
				this.addLeftButton(ais);
				this.addLeftButton(inventory);
				this.addLeftButton(advanced);
				break;
			}
			case 2: { // news
				if (this.activeLeftTab<20 || this.activeLeftTab>29) { this.activeLeftTab = 20; }
				GuiMenuLeftButton items = new GuiMenuLeftButton(20, this.guiLeft, this.guiTop + 4, "inv.drops");
				GuiMenuLeftButton blocks = new GuiMenuLeftButton(21, this.guiLeft, items.y+items.getHeight(), "gui.help.blocks");
				items.active = items.id==this.activeLeftTab;
				blocks.active = blocks.id==this.activeLeftTab;
				this.addLeftButton(items);
				this.addLeftButton(blocks);
				break;
			}
			case 3: { // scripts
				if (this.activeLeftTab<30 || this.activeLeftTab>39) { this.activeLeftTab = 30; }
				GuiMenuLeftButton main = new GuiMenuLeftButton(30, this.guiLeft, this.guiTop + 4, "gui.help.general");
				GuiMenuLeftButton api = new GuiMenuLeftButton(31, this.guiLeft, main.y+main.getHeight(), "gui.help.api");
				//scroll
				if (this.scroll==null) {
					this.scroll = new GuiCustomScroll(this, 0);
					this.scroll.setList(Lists.newArrayList(GuiHelpBook.apis.keySet()));
					this.scroll.hoversTexts = new String[GuiHelpBook.apis.size()][];
					this.scroll.setSize(100, this.ySize-50);
					int i = 0;
					for (EnumInterfaceData enumIT : GuiHelpBook.apis.values()) {
						List<String> com = enumIT.it.getComment();
						this.scroll.hoversTexts[i] = com.toArray(new String[com.size()]);
						i++;
					}
				}
				this.scroll.guiLeft = this.guiLeft - 101;
				this.scroll.guiTop = this.guiTop + 47;
				if (this.scroll.selected<0) { this.scroll.selected = 0; }
				this.addScroll(this.scroll);
				
				main.active = main.id==this.activeLeftTab;
				api.active = api.id==this.activeLeftTab;
				this.addLeftButton(main);
				this.addLeftButton(api);
				break;
			}
			default: { // general
				if (this.activeLeftTab<0 || this.activeLeftTab>9) { this.activeLeftTab = 0; }
				GuiMenuLeftButton conf = new GuiMenuLeftButton(0, this.guiLeft, this.guiTop + 4, "gui.help.config");
				GuiMenuLeftButton blocks = new GuiMenuLeftButton(1, this.guiLeft, conf.y+conf.getHeight(), "gui.help.blocks");
				GuiMenuLeftButton items = new GuiMenuLeftButton(2, this.guiLeft, blocks.y+blocks.getHeight(), "gui.help.items");
				GuiMenuLeftButton potions = new GuiMenuLeftButton(3, this.guiLeft, items.y+items.getHeight(), "gui.help.potions");
				conf.active = conf.id==this.activeLeftTab;
				blocks.active = blocks.id==this.activeLeftTab;
				items.active = items.id==this.activeLeftTab;
				potions.active = potions.id==this.activeLeftTab;
				this.addLeftButton(conf);
				this.addLeftButton(blocks);
				this.addLeftButton(items);
				this.addLeftButton(potions);
			}
		}
		GuiTextArea ta = new GuiTextArea(0, this.guiLeft + 4, this.guiTop + 4, this.xSize - 7, this.ySize -7, "" );
		ta.enableCodeHighlighting();
		ta.setIsCode(false);
		this.add(ta);
		this.resetText();
	}

	private void resetText() {
		if (!(this.get(0) instanceof GuiTextArea)) { return; }
		if (this.activeLeftTab!=31) {
			String text = GuiHelpBook.map.get(this.activeLeftTab);
			if (text==null) { text = ""; }
			((GuiTextArea) this.get(0)).setText(text);
			return;
		}
		if (this.scroll==null || !GuiHelpBook.apis.containsKey(this.scroll.getSelected())) {
			((GuiTextArea) this.get(0)).setText("");
			return;
		}
		((GuiTextArea) this.get(0)).setText(GuiHelpBook.apis.get(this.scroll.getSelected()).it.getText());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.mc.renderEngine!=null) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			Gui.drawRect(3, 3, this.xSize-2, this.ySize-2, 0xFFC6C6C6);
			this.drawHorizontalLine(2, this.xSize-2, 0, 0xFF000000);
			this.drawHorizontalLine(1, 1, 1, 0xFF000000);
			this.drawHorizontalLine(this.xSize-1, this.xSize-1, 1, 0xFF000000);
			this.drawVerticalLine(0, 1, this.ySize-1, 0xFF000000);
			this.drawVerticalLine(this.xSize, 1, this.ySize-1, 0xFF000000);
			this.drawHorizontalLine(1, 1, this.ySize-1, 0xFF000000);
			this.drawHorizontalLine(2, this.xSize-2, this.ySize, 0xFF000000);
			this.drawHorizontalLine(this.xSize-1, this.xSize-1, this.ySize-1, 0xFF000000);
			this.drawHorizontalLine(2, this.xSize-2, 1, 0xFFFFFFFF);
			this.drawHorizontalLine(1, this.xSize-2, 2, 0xFFFFFFFF);
			this.drawVerticalLine(1, 2, this.ySize-1, 0xFFFFFFFF);
			this.drawVerticalLine(2, 2, this.ySize-1, 0xFFFFFFFF);
			this.drawHorizontalLine(2, this.xSize-2, this.ySize-1, 0xFF555555);
			this.drawHorizontalLine(3, this.xSize-1, this.ySize-2, 0xFF555555);
			this.drawVerticalLine(this.xSize-1, 1, this.ySize-1, 0xFF555555);
			this.drawVerticalLine(this.xSize-2, 2, this.ySize-1, 0xFF555555);
			GlStateManager.popMatrix();
		}
		if (this.scroll!=null) {
			this.scroll.visible = this.activeLeftTab==31;
			if (this.scroll.visible) {
				GlStateManager.pushMatrix();
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.translate(this.guiLeft-105, this.guiTop+43, 0.0f);
				int x = 105;
				int y = this.scroll.height+7;
				Gui.drawRect(3, 3, x, y-2, 0xFFC6C6C6);
				this.drawHorizontalLine(2, x-1, 0, 0xFF000000);
				this.drawHorizontalLine(1, 1, 1, 0xFF000000);
				this.drawVerticalLine(0, 1, y-1, 0xFF000000);
				this.drawHorizontalLine(1, 1, y-1, 0xFF000000);
				this.drawHorizontalLine(2, x+1, y, 0xFF000000);
				this.drawHorizontalLine(2, x-1, 1, 0xFFFFFFFF);
				this.drawHorizontalLine(1, x-1, 2, 0xFFFFFFFF);
				this.drawVerticalLine(1, 2, y-1, 0xFFFFFFFF);
				this.drawVerticalLine(2, 2, y-1, 0xFFFFFFFF);
				this.drawHorizontalLine(2, x, y-1, 0xFF555555);
				this.drawHorizontalLine(3, x-1, y-2, 0xFF555555);
				GlStateManager.popMatrix();
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.curentLine = -1;
		if (this.activeLeftTab==31 && this.scroll!=null && this.get(0) instanceof GuiTextArea) { 
			GuiTextArea area = (GuiTextArea) this.get(0);
			if (area != null && area.hovered) {
				this.curentLine = (mouseY - area.y + 1)/area.container.lineHeight + area.scrolledLine;
				if (this.curentLine<0) { this.curentLine = -1; }
			}
		}
		if (this.curentLine!=-1) {
			InterfaseData it = GuiHelpBook.apis.get(this.scroll.getSelected()).it;
			if (it==null) { return; }
			MetodData m = it.metods.get(this.curentLine);
			if (m==null) { return; }
			this.drawHoveringText(m.getComment(), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button instanceof GuiMenuTopButton) { this.activeTopTab = button.id; }
		else { this.activeLeftTab = button.id; }
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
		if (this.activeLeftTab!=31 || this.scroll==null || !(this.get(0) instanceof GuiTextArea)) { return; }
		GuiTextArea area = (GuiTextArea) this.get(0);
		if (!area.hovered) { return; }
		Object[] select = area.getSelectionText(this.mouseX, this.mouseY);
		System.out.println("line: "+this.curentLine+" - "+select[1]);
		/*
		if (GuiHelpBook.apis.containsKey(select[1])) {
			this.scroll.setSelected(this.curentElement);
			this.resetText();
			return;
		}*/
	}
	
}
