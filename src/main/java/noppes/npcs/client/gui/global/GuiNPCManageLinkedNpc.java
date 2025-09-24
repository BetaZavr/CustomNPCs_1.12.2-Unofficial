package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCManageLinkedNpc extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {

	protected final List<String> data = new ArrayList<>();
	protected GuiCustomScroll scroll;

	public GuiNPCManageLinkedNpc(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuGlobal;

		Client.sendData(EnumPacketServer.LinkedGetAll);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: save(); setSubGui(new SubGuiEditText(0, "New")); break;
			case 2: {
				if (scroll.hasSelected()) { Client.sendData(EnumPacketServer.LinkedRemove, scroll.getSelected()); }
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
		addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(143, 208); }
		scroll.guiLeft = guiLeft + 214;
		scroll.guiTop = guiTop + 4;
		scroll.setList(data);
		addScroll(scroll);
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		data.addAll(dataList);
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!((SubGuiEditText) subgui).cancelled) {
			Client.sendData(EnumPacketServer.LinkedAdd, ((SubGuiEditText) subgui).text[0]);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
