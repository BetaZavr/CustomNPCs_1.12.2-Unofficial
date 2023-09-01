package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiNpcFactionSelect
extends SubGuiInterface
implements ICustomScrollListener {

	private String name;
	private Map<Integer, String> data;
	private GuiCustomScroll scrollHostileFactions;
	public HashSet<Integer> selectFactions;

	public SubGuiNpcFactionSelect(String name, HashSet<Integer> setFactions, Map<String, Integer> data) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		this.xSize = 171;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.name = name;
		this.selectFactions = setFactions;
		this.data = Maps.<Integer, String>newTreeMap();
		for (String key : data.keySet()) {
			this.data.put(data.get(key), key);
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		ArrayList<String> showList = Lists.<String>newArrayList();
		HashSet<String> set = Sets.<String>newHashSet();
		for (int id : this.data.keySet()) {
			String key = this.data.get(id);
			if (key.equals(this.name)) { continue; }
			showList.add(key);
			if (this.selectFactions.contains(id)) { set.add(key); }
		}
		if (this.scrollHostileFactions == null) {
			(this.scrollHostileFactions = new GuiCustomScroll(this, 1, true)).setSize(163, 185);
		}
		this.scrollHostileFactions.guiLeft = this.guiLeft + 4;
		this.scrollHostileFactions.guiTop = this.guiTop + 28;
		this.scrollHostileFactions.setListNotSorted(showList);
		this.scrollHostileFactions.setSelectedList(set);
		
		this.addScroll(this.scrollHostileFactions);
		this.addLabel(new GuiNpcLabel(0, AdditionalMethods.instance.deleteColor(this.name), this.guiLeft + 4, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "faction.select", this.guiLeft + 4, this.guiTop + 16));
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 123, this.guiTop + 6, 45, 20, "gui.done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		this.close();
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.add").getFormattedText());
		}
	}
	
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (scroll.id == 1) {
			HashSet<Integer> set = Sets.<Integer>newHashSet();
			HashSet<String> list = scroll.getSelectedList();
			HashSet<String> newlist = Sets.<String>newHashSet();
			for (int id : this.data.keySet()) {
				if (!list.contains(this.data.get(id))) { continue; }
				set.add(id);
				newlist.add(this.data.get(id));
			}
			this.selectFactions = set;
			if (list.size()!=newlist.size()) { scroll.setSelectedList(newlist); }
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
