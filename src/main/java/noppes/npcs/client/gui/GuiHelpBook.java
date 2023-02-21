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
	int activeLeftTab = 10;
	Map<Integer, String> map;
	private String[] arr = new String [] { "config", "blocks", "items", "potions" };
	
	public GuiHelpBook() {
		this.setBackground("bgfilled.png");
		this.xSize = 300;
		this.ySize = 174;
		this.closeOnEsc = true;
		this.map = Maps.<Integer, String>newHashMap();
		char chr = Character.toChars(0x000A)[0];
		for (int i=0; i< this.arr.length; i++) {
			String text = new TextComponentTranslation("help.info."+arr[i]).getFormattedText();
			while(text.indexOf("<br>")!=-1) { text = text.replaceAll("<br>", ""+chr); }
			this.map.put(10+i, AdditionalMethods.deleteColor(text));
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int maxW = 0, w = 0;
		if (this.activeTopTab==0) {
			w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.config").getFormattedText());
			maxW = w;
			w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.blocks").getFormattedText());
			if (maxW<w) { maxW = w; }
			w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.items").getFormattedText());
			if (maxW<w) { maxW = w; }
			w = this.mc.fontRenderer.getStringWidth(new TextComponentTranslation("gui.help.potions").getFormattedText());
			if (maxW<w) { maxW = w; }
		}
		ScaledResolution sr = new ScaledResolution(this.mc);
		this.xSize = (int) (sr.getScaledWidth_double() - maxW - 40);
		this.ySize = (int) (sr.getScaledHeight_double() - 60);
		this.width = this.xSize;
		this.height = this.ySize;
		this.guiLeft = 20 + maxW;
		this.guiTop = 30;
		
		GuiMenuTopButton general = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.help.general");
		//GuiMenuTopButton npcEdit = new GuiMenuTopButton(1, general.x + general.getWidth(), guiTop - 17, "gui.help.npc");
		general.active = general.id==this.activeTopTab;
		//npcEdit.active = npcEdit.id==this.activeTopTab;
		this.addTopButton(general);
		//this.addTopButton(npcEdit);
		
		if (this.activeTopTab==0) {
			if (this.activeLeftTab<10 || this.activeLeftTab>13) { this.activeLeftTab = 10; }
			GuiMenuLeftButton conf = new GuiMenuLeftButton(10, this.guiLeft, this.guiTop + 4, "gui.help.config");
			GuiMenuLeftButton blocks = new GuiMenuLeftButton(11, this.guiLeft, conf.y+conf.getHeight(), "gui.help.blocks");
			GuiMenuLeftButton items = new GuiMenuLeftButton(12, this.guiLeft, blocks.y+blocks.getHeight(), "gui.help.items");
			GuiMenuLeftButton potions = new GuiMenuLeftButton(13, this.guiLeft, items.y+items.getHeight(), "gui.help.potions");
			conf.active = conf.id==this.activeLeftTab;
			blocks.active = blocks.id==this.activeLeftTab;
			items.active = items.id==this.activeLeftTab;
			potions.active = potions.id==this.activeLeftTab;
			this.addLeftButton(conf);
			this.addLeftButton(blocks);
			this.addLeftButton(items);
			this.addLeftButton(potions);
		} else {
			if (this.activeLeftTab<14 || this.activeLeftTab>15) { this.activeLeftTab = 14; }
		}
		
		String text = this.map.get(this.activeLeftTab);
		if (text==null) { text = ""; }
		GuiTextArea ta = new GuiTextArea(0, this.guiLeft + 4, this.guiTop + 4,
				this.xSize - 8, this.ySize -8, text );
		ta.enableCodeHighlighting();
		ta.onlyReading = true;
		this.add(ta);
	}


	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id<10) { this.activeTopTab = button.id; }
		else { this.activeLeftTab = button.id; }
		this.initGui();
	}
	
	@Override
	public void save() { }

}
