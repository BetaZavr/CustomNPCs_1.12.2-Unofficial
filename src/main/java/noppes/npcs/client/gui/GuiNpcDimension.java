package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcDimension
extends GuiNPCInterface
implements IScrollData, ICustomScrollListener {
	
	private HashMap<String, Integer> data;
	private GuiCustomScroll scroll;

	public GuiNpcDimension() {
		this.data = new HashMap<String, Integer>();
		this.xSize = 256;
		this.setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 1: { // settings
				this.player.sendMessage(new TextComponentTranslation("gui.wip"));
				/*if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				int id = this.data.get(this.scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) { return; }
				CustomNpcs.proxy.openGui(null, EnumGuiType.DimentionSetting, id, 0, 0);*/
				break;
			}
			case 2: { // add
				CustomNpcs.proxy.openGui(null, EnumGuiType.DimentionSetting, 0, 0, 0);
				break;
			}
			case 3: { // remove
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				int id = this.data.get(this.scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) { return; }
				Client.sendData(EnumPacketServer.DimensionDelete, id);
				break;
			}
			case 4: {
				this.tp();
				break;
			}
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, this.data.get(this.scroll.getSelected()));
		}
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
	}

	@Override
	public void initGui() {
		super.initGui();
		int id = 0;
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(186, 199);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		if (this.scroll.selected==-1) {
			for (String key : this.data.keySet()) {
				if (this.data.get(key)==this.mc.player.world.provider.getDimension()) {
					this.scroll.setSelected(key);
				}
			}
		}
		if (this.data.containsKey(this.scroll.getSelected())) {
			id = this.data.get(this.scroll.getSelected());
		}
		
		String title = new TextComponentTranslation("gui.dimensions").getFormattedText();
		int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop + 4));
		
		GuiNpcButton button = new GuiNpcButton(1, this.guiLeft + 192, this.guiTop + 36, 60, 20, "gui.settings");
		button.enabled = this.scroll.selected>=0 && ClientHandler.getInstance().has(id);
		this.addButton(button);

		button = new GuiNpcButton(2, this.guiLeft + 192, this.guiTop + 80, 60, 20, "gui.add");
		this.addButton(button);
		
		button = new GuiNpcButton(3, this.guiLeft + 192, this.guiTop + 102, 60, 20, "gui.remove");
		button.enabled = this.scroll.selected>=0 && ClientHandler.getInstance().has(id);
		this.addButton(button);
		
		button = new GuiNpcButton(4, this.guiLeft + 192, this.guiTop + 14, 60, 20, "TP");
		button.enabled = this.scroll.selected>=0;
		this.addButton(button);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.DimensionsGet, new Object[0]);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText("dimensions.hover.settings");
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText("dimensions.hover.add");
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText("dimensions.hover.del");
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText("dimensions.hover.tp");
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

	@Override
	public void save() { }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		TreeMap<Integer, String> m = Maps.<Integer, String>newTreeMap();
		for (String key : data.keySet()) { m.put(data.get(key), key); }
		List<String> l = Lists.<String>newArrayList();
		List<String> s = Lists.<String>newArrayList();
		String c = ""+((char) 167);
		for (int id : m.keySet()) {
			String[] t = m.get(id).split("&");
			String r = t[0].equals("delete") ? "8" : "7";
			String str = c+r+"ID:"+(t[0].equals("delete") ? c+"7" : c+"6")+id+c+r+" - \""+(t[0].equals("delete") ? c+"7" : c+"r")+new TextComponentTranslation(t[1]).getFormattedText()+c+r+"\""+(t.length>=3 && !t[2].isEmpty() ? " ["+t[2]+"]" : "");
			l.add(str);
			String p = c+(ClientHandler.getInstance().has(id) ? "dC" : "bM")+c+r+".";
			s.add(p + (t[0].equals("delete") ? c+"7delete" : t[0].equals("true") ? c+"aloaded" : c+"cunloaded"));
			this.data.put(str, id);
		}
		this.scroll.setListNotSorted(l);
		this.scroll.setSuffixs(s);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
		this.getButton(3).setDisplayText(selected);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.tp();
	}

	private void tp() {
		if (!this.data.containsKey(this.scroll.getSelected())) { return; }
		Client.sendData(EnumPacketServer.DimensionTeleport, this.data.get(this.scroll.getSelected()));
		this.close();
	}
	
}
