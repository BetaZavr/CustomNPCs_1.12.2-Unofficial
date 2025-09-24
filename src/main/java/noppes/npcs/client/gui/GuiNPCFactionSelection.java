package noppes.npcs.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCFactionSelection extends GuiNPCInterface implements IScrollData {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected final GuiScreen parent;
	protected GuiNPCStringSlot slot;
	protected int factionId;
	public GuiSelectionListener listener;

	public GuiNPCFactionSelection(EntityNPCInterface npc, GuiScreen gui, int id) {
		super(npc);
		drawDefaultBackground = false;
		title = "Select Dialog Category";

		parent = gui;
		factionId = id;
		if (parent instanceof GuiSelectionListener) { listener = (GuiSelectionListener) parent; }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 2: onClosed(); NoppesUtil.openGUI(player, parent); break;
			case 4: doubleClicked(); break;
		}
	}

	@Override
	public void doubleClicked() {
		if (slot.selected == null || slot.selected.isEmpty()) { return; }
		factionId = data.get(slot.selected);
		onClosed();
		NoppesUtil.openGUI(player, parent);
	}

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		slot.drawScreen(mouseXIn, mouseYIn, partialTicks);
		super.drawScreen(mouseXIn, mouseYIn, partialTicks);
	}

	public void handleMouseInput() throws IOException {
		slot.handleMouseInput();
		super.handleMouseInput();
	}

	@Override
	public void initGui() {
		super.initGui();
		Vector<String> list = new Vector<>();
		(slot = new GuiNPCStringSlot(list, this, false, 18)).registerScrollButtons(4, 5);
		addButton(new GuiNpcButton(2, width / 2 - 100, height - 41, 98, 20, "gui.back").setHoverText("hover.back"));
		addButton(new GuiNpcButton(4, width / 2 + 2, height - 41, 98, 20, "mco.template.button.select"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void save() {
		if (factionId >= 0 && listener != null) { listener.selected(factionId, slot.selected); }
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		data.putAll(dataMap);
		slot.setList(dataList);
		if (factionId >= 0) {
			for (String name : data.keySet()) {
				if (data.get(name) == factionId) { slot.selected = name; }
			}
		}
	}

	@Override
	public void setSelected(String selected) { }

}
