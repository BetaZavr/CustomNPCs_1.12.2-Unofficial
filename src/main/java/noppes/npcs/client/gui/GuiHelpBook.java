package noppes.npcs.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.GuiMenuLeftButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.util.AdditionalMethods;

public class GuiHelpBook
extends GuiNPCInterface {
	
	int activeTopTab = 0;
	int activeLeftTab = 0;
	private final Map<Integer, String> map;
	private final String[] arr = new String [] { "config", "blocks", "items", "potions", "", "", "", "", "", "",
			"npc.display", "npc.stats", "npc.ais", "npc.inventory", "npc.advanced", "", "", "", "", "", 
			"new.items", "new.blocks", "", "", "", "", "", "", "", "", "", 
			"script.main", "script.old.api", "script.new.api", "", "", "", "", "", "" };
	
	public GuiHelpBook() {
		this.setBackground("bgfilled.png");
		this.xSize = 300;
		this.ySize = 174;
		this.closeOnEsc = true;
		this.map = Maps.<Integer, String>newHashMap();
		char chr = Character.toChars(0x000A)[0];
		for (int i=0; i< this.arr.length; i++) {
			if (this.arr[i].isEmpty()) {
				this.map.put(i, this.arr[i]);
				continue;
			}
			String text = new TextComponentTranslation("help.info."+this.arr[i]).getFormattedText();
			while(text.indexOf("<br>")!=-1) { text = text.replaceAll("<br>", ""+chr); }
			this.map.put(i, AdditionalMethods.deleteColor(text));
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
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.old.api").getFormattedText());
				if (maxW<w) { maxW = w; }
				w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.new.api").getFormattedText());
				if (maxW<w) { maxW = w; }
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
				GuiMenuLeftButton oldapi = new GuiMenuLeftButton(31, this.guiLeft, main.y+main.getHeight(), "gui.help.old.api");
				GuiMenuLeftButton newapi = new GuiMenuLeftButton(32, this.guiLeft, oldapi.y+oldapi.getHeight(), "gui.help.new.api");
				main.active = main.id==this.activeLeftTab;
				oldapi.active = oldapi.id==this.activeLeftTab;
				newapi.active = newapi.id==this.activeLeftTab;
				this.addLeftButton(main);
				this.addLeftButton(oldapi);
				this.addLeftButton(newapi);
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
		String text = this.map.get(this.activeLeftTab);
		if (text==null) { text = ""; }
		GuiTextArea ta = new GuiTextArea(0, this.guiLeft + 4, this.guiTop + 4, this.xSize - 8, this.ySize -8, text );
		ta.enableCodeHighlighting();
		ta.onlyReading = true;
		this.add(ta);
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

}
