package noppes.npcs.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCFactionSelection
extends GuiNPCInterface
implements IScrollData {

	private final HashMap<String, Integer> data = new HashMap<>();
	private int factionId;
	public GuiSelectionListener listener;
	private final GuiScreen parent;
	private GuiNPCStringSlot slot;

	public GuiNPCFactionSelection(EntityNPCInterface npc, GuiScreen gui, int id) {
		super(npc);
		drawDefaultBackground = false;

		title = "Select Dialog Category";
		parent = gui;
		factionId = id;
		if (parent instanceof GuiSelectionListener) {
			listener = (GuiSelectionListener) parent;
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 2) {
			close();
			NoppesUtil.openGUI(player, parent);
		}
		if (button.id == 4) {
			doubleClicked();
		}
	}

	@Override
	public void doubleClicked() {
		if (slot.selected == null || slot.selected.isEmpty()) {
			return;
		}
		factionId = data.get(slot.selected);
		close();
		NoppesUtil.openGUI(player, parent);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		slot.drawScreen(i, j, f);
		super.drawScreen(i, j, f);
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
		addButton(new GuiNpcButton(2, width / 2 - 100, height - 41, 98, 20, "gui.back"));
		addButton(new GuiNpcButton(4, width / 2 + 2, height - 41, 98, 20, "mco.template.button.select"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void save() {
		if (factionId >= 0 && listener != null) {
			listener.selected(factionId, slot.selected);
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
		data.clear();
		data.putAll(dataMap);
		slot.setList(list);
		if (factionId >= 0) {
			for (String name : data.keySet()) {
				if (data.get(name) == factionId) {
					slot.selected = name;
				}
			}
		}
	}

	@Override
	public void setSelected(String selected) {
	}
}
