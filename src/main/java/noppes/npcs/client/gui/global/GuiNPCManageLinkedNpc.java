package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageLinkedNpc
extends GuiNPCInterface2
implements IScrollData, ISubGuiListener {

	public static GuiScreen Instance;
	private List<String> data;
	private GuiCustomScroll scroll;

	public GuiNPCManageLinkedNpc(EntityNPCInterface npc) {
		super(npc);
		this.data = new ArrayList<>();
		GuiNPCManageLinkedNpc.Instance = this;
		Client.sendData(EnumPacketServer.LinkedGetAll);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 1) {
			this.save();
			this.setSubGui(new SubGuiEditText(0, "New"));
		}
		if (button.getID() == 2 && this.scroll.hasSelected()) {
			Client.sendData(EnumPacketServer.LinkedRemove, this.scroll.getSelected());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 358, this.guiTop + 61, 58, 20, "gui.remove"));
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(143, 208);
		}
		this.scroll.guiLeft = this.guiLeft + 214;
		this.scroll.guiTop = this.guiTop + 4;
		this.scroll.setList(this.data);
		this.addScroll(this.scroll);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = new ArrayList<>(list);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!((SubGuiEditText) subgui).cancelled) {
			Client.sendData(EnumPacketServer.LinkedAdd, ((SubGuiEditText) subgui).text[0]);
		}
	}
}
