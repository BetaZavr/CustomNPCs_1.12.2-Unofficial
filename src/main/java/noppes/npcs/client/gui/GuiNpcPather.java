package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;

public class GuiNpcPather
extends GuiNPCInterface
implements IGuiData {
	
	private DataAI ai;
	private GuiCustomScroll scroll;

	public GuiNpcPather(EntityNPCInterface npc) {
		this.drawDefaultBackground = false;
		this.xSize = 176;
		this.title = "Npc Pather";
		this.setBackground("smallbg.png");
		this.ai = npc.ais;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (this.scroll.selected < 0) {
			return;
		}
		int id = guibutton.id;
		if (id == 0) {
			List<int[]> list = this.ai.getMovingPath();
			int selected = this.scroll.selected;
			if (list.size() <= selected + 1) {
				return;
			}
			int[] a = list.get(selected);
			int[] b = list.get(selected + 1);
			list.set(selected, b);
			list.set(selected + 1, a);
			this.ai.setMovingPath(list);
			this.initGui();
			this.scroll.selected = selected + 1;
		}
		if (id == 1) {
			if (this.scroll.selected - 1 < 0) {
				return;
			}
			List<int[]> list = this.ai.getMovingPath();
			int selected = this.scroll.selected;
			int[] a = list.get(selected);
			int[] b = list.get(selected - 1);
			list.set(selected, b);
			list.set(selected - 1, a);
			this.ai.setMovingPath(list);
			this.initGui();
			this.scroll.selected = selected - 1;
		}
		if (id == 2) {
			List<int[]> list = this.ai.getMovingPath();
			if (list.size() <= 1) {
				return;
			}
			list.remove(this.scroll.selected);
			this.ai.setMovingPath(list);
			this.initGui();
		}
	}

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
	}

	@Override
	public void initGui() {
		super.initGui();
		(this.scroll = new GuiCustomScroll(this, 0)).setSize(160, 164);
		List<String> list = new ArrayList<String>();
		for (int[] arr : this.ai.getMovingPath()) {
			list.add("x:" + arr[0] + " y:" + arr[1] + " z:" + arr[2]);
		}
		this.scroll.setUnsortedList(list);
		this.scroll.guiLeft = this.guiLeft + 7;
		this.scroll.guiTop = this.guiTop + 12;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + 178, 52, 20, "gui.down"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 62, this.guiTop + 178, 52, 20, "gui.up"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 118, this.guiTop + 178, 52, 20, "selectWorld.deleteButton"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.MovingPathGet, new Object[0]);
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
	public void save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(this.ai.getMovingPath()));
		Client.sendData(EnumPacketServer.MovingPathSave, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.ai.readToNBT(compound);
		this.initGui();
	}
}
