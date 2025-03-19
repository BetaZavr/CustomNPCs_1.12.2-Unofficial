package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCAdvancedLinkedNpc
extends GuiNPCInterface2
implements IScrollData, ICustomScrollListener {

	public static GuiScreen Instance;
	private List<String> data;
	private GuiCustomScroll scroll;

	public GuiNPCAdvancedLinkedNpc(EntityNPCInterface npc) {
		super(npc);
		this.data = new ArrayList<>();
		GuiNPCAdvancedLinkedNpc.Instance = this;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 1) {
			Client.sendData(EnumPacketServer.LinkedSet, "");
		}
	}

	@Override
	public void close() {
		this.save();
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.clear"));
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(143, 208);
		}
		this.scroll.guiLeft = this.guiLeft + 137;
		this.scroll.guiTop = this.guiTop + 4;
		this.scroll.setSelected(this.npc.linkedName);
		this.scroll.setList(this.data);
		this.addScroll(this.scroll);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.LinkedGetAll);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		Client.sendData(EnumPacketServer.LinkedSet, scroll.getSelected());
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = new ArrayList<>(list);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
		this.scroll.setSelected(selected);
	}

}
