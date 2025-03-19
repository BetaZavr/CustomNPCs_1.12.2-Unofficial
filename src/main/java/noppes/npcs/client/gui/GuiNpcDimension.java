package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcDimension
extends GuiNPCInterface
implements IScrollData, ICustomScrollListener {

	private final HashMap<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scroll;

	public GuiNpcDimension() {
		xSize = 256;
		setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: { // settings
				player.sendMessage(new TextComponentTranslation("gui.wip"));
				if (!data.containsKey(scroll.getSelected())) {
					return;
				}
				int id = data.get(scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) {
					return;
				}
				CustomNpcs.proxy.openGui(null, EnumGuiType.DimensionSetting, id, 0, 0);
				break;
			}
			case 2: { // add
				CustomNpcs.proxy.openGui(null, EnumGuiType.DimensionSetting, 0, 0, 0);
				break;
			}
			case 3: { // remove
				if (!data.containsKey(scroll.getSelected())) {
					return;
				}
				int id = data.get(scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) {
					return;
				}
				Client.sendData(EnumPacketServer.DimensionDelete, id);
				break;
			}
			case 4: {
				tp();
				break;
			}
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, data.get(scroll.getSelected()));
		}
		NoppesUtil.openGUI(player, this);
	}

	@Override
	public void initGui() {
		super.initGui();
		int id = 0;
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(186, 199); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		addScroll(scroll);
		if (scroll.getSelect() == -1) {
			for (String key : data.keySet()) {
				if (data.get(key) == mc.player.world.provider.getDimension()) {
					scroll.setSelected(key);
				}
			}
		}
		if (data.containsKey(scroll.getSelected())) { id = data.get(scroll.getSelected()); }
		// title
		GuiNpcLabel label = new GuiNpcLabel(0, "gui.dimensions", guiLeft, guiTop + 4);
		label.setCenter(xSize);
		addLabel(label);
		// settings
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 192, guiTop + 36, 60, 20, "gui.settings");
		button.setEnabled(scroll.getSelect() >= 0 && ClientHandler.getInstance().has(id));
		button.setHoverText("dimensions.hover.settings");
		addButton(button);
		// add
		button = new GuiNpcButton(2, guiLeft + 192, guiTop + 80, 60, 20, "gui.add");
		button.setHoverText("dimensions.hover.add");
		addButton(button);
		// del
		button = new GuiNpcButton(3, guiLeft + 192, guiTop + 102, 60, 20, "gui.remove");
		button.setEnabled(scroll.getSelect() >= 0 && ClientHandler.getInstance().has(id));
		button.setHoverText("dimensions.hover.del");
		addButton(button);
		// pt
		button = new GuiNpcButton(4, guiLeft + 192, guiTop + 14, 60, 20, "TP");
		button.setEnabled(scroll.getSelect() >= 0);
		button.setHoverText("dimensions.hover.tp");
		addButton(button);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.DimensionsGet);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || isInventoryKey(i)) {
			close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		scroll.mouseClicked(i, j, k);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		tp();
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
		data.clear();
		TreeMap<Integer, String> m = new TreeMap<>();
		// reverse K V
		for (String key : dataMap.keySet()) { m.put(dataMap.get(key), key); }
		List<String> l = new ArrayList<>();
		List<String> s = new ArrayList<>();
		String c = "" + ((char) 167);
		for (int id : m.keySet()) {
			String[] t = m.get(id).split("&");
			String r = t[0].equals("delete") ? "8" : "7";
			String str = c + r + "ID:" + (t[0].equals("delete") ? c + "7" : c + "6") + id + c + r + " - \"" + (t[0].equals("delete") ? c + "7" : c + "r") + new TextComponentTranslation(t[1]).getFormattedText() + c + r + "\"" + (t.length >= 3 && !t[2].isEmpty() ? " [" + t[2] + "]" : "");
			l.add(str);
			String p = c + (id > 99 ? "6NPC" : "bMC") + c + r + ".";
			s.add(p + (t[0].equals("delete") ? c + "7delete" : t[0].equals("true") ? c + "aloaded" : c + "cunloaded"));
			data.put(str, id);
		}
		scroll.setListNotSorted(l);
		scroll.setSuffixes(s);
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		getButton(3).setDisplayText(selected);
	}

	private void tp() {
		if (!data.containsKey(scroll.getSelected())) {
			return;
		}
		Client.sendData(EnumPacketServer.DimensionTeleport, data.get(scroll.getSelected()));
		close();
	}

}
