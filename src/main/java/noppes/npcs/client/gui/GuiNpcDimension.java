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
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiNpcDimension extends GuiNPCInterface implements IScrollData, ICustomScrollListener {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scroll;

	public GuiNpcDimension() {
		super();
		setBackground("menubg.png");
		xSize = 256;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				player.sendMessage(new TextComponentTranslation("gui.wip"));
				if (!data.containsKey(scroll.getSelected())) { return; }
				int id = data.get(scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) { return; }
				CustomNpcs.proxy.openGui(null, EnumGuiType.DimensionSetting, id, 0, 0);
				break;
			} // settings
			case 2: CustomNpcs.proxy.openGui(null, EnumGuiType.DimensionSetting, 0, 0, 0); break; // add
			case 3: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				int id = data.get(scroll.getSelected());
				if (!ClientHandler.getInstance().has(id)) { return; }
				Client.sendData(EnumPacketServer.DimensionDelete, id);
				break;
			} // remove
			case 4: tp(); break;
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) { Client.sendData(EnumPacketServer.RemoteDelete, data.get(scroll.getSelected())); }
		NoppesUtil.openGUI(player, this);
	}

	@Override
	public void initGui() {
		super.initGui();
		int id = 0;
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(186, 199); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		addScroll(scroll);
		if (scroll.getSelect() == -1) {
			for (String key : data.keySet()) {
				if (data.get(key) == mc.player.world.provider.getDimension()) { scroll.setSelected(key); }
			}
		}
		if (data.containsKey(scroll.getSelected())) { id = data.get(scroll.getSelected()); }
		// title
		addLabel(new GuiNpcLabel(0, "gui.dimensions", guiLeft, guiTop + 4)
				.setCenter(xSize));
		// settings
		addButton(new GuiNpcButton(1, guiLeft + 192, guiTop + 36, 60, 20, "gui.settings")
				.setIsEnable(scroll.getSelect() >= 0 && ClientHandler.getInstance().has(id))
				.setHoverText("dimensions.hover.settings"));
		// add
		addButton(new GuiNpcButton(2, guiLeft + 192, guiTop + 80, 60, 20, "gui.add")
				.setHoverText("dimensions.hover.add"));
		// del
		addButton(new GuiNpcButton(3, guiLeft + 192, guiTop + 102, 60, 20, "gui.remove")
				.setIsEnable(scroll.getSelect() >= 0 && ClientHandler.getInstance().has(id))
				.setHoverText("dimensions.hover.del"));
		// pt
		addButton(new GuiNpcButton(4, guiLeft + 192, guiTop + 14, 60, 20, "TP")
				.setIsEnable(scroll.getSelect() >= 0)
				.setHoverText("dimensions.hover.tp"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.DimensionsGet);
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui != null) { return subgui.keyCnpcsPressed(typedChar, keyCode); }
		if (keyCode == Keyboard.KEY_ESCAPE || isInventoryKey(keyCode)) {
			onClosed();
			return true;
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { initGui(); }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { tp(); }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
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
		scroll.setUnsortedList(l).setSuffixes(s);
		initGui();
	}

	@Override
	public void setSelected(String selected) { getButton(3).setDisplayText(selected); }

	private void tp() {
		if (!data.containsKey(scroll.getSelected())) {return; }
		Client.sendData(EnumPacketServer.DimensionTeleport, data.get(scroll.getSelected()));
		onClosed();
	}

}
