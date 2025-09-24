package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCAdvancedLinkedNpc extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {

	protected List<String> data = new ArrayList<>();
	protected GuiCustomScroll scroll;

	public GuiNPCAdvancedLinkedNpc(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && button.getID() == 1) { Client.sendData(EnumPacketServer.LinkedSet, ""); }
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.clear"));
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(143, 208); }
		scroll.guiLeft = guiLeft + 137;
		scroll.guiTop = guiTop + 4;
		scroll.setSelected(npc.linkedName);
		scroll.setList(data);
		addScroll(scroll);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.LinkedGetAll);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { Client.sendData(EnumPacketServer.LinkedSet, scroll.getSelected()); }

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data = new ArrayList<>(dataList);
		initGui();
	}

	@Override
	public void setSelected(String selected) { scroll.setSelected(selected); }

}
