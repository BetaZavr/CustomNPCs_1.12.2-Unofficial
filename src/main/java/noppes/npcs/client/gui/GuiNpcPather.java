package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.NBTTags;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcPather
extends GuiNPCInterface
implements IGuiData {
	
	private List<int[]> path;
	private GuiCustomScroll scroll;

	public GuiNpcPather(EntityNPCInterface npc) {
		super(npc);
		this.drawDefaultBackground = false;
		this.xSize = 176;
		this.title = "Npc Pather";
		this.setBackground("smallbg.png");
		this.path = npc.ais.getMovingPath();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (this.scroll.selected < 0) { return; }
		if (button.id == 0) { // down
			List<int[]> list = Lists.newArrayList(this.path);
			int selected = this.scroll.selected;
			if (list.size() <= selected + 1) { return; }
			int[] a = list.get(selected);
			int[] b = list.get(selected + 1);
			list.set(selected, b);
			list.set(selected + 1, a);
			this.path = list;
			this.initGui();
			this.scroll.selected = selected + 1;
		}
		if (button.id == 1) { // up
			if (this.scroll.selected - 1 < 0) { return; }
			List<int[]> list = Lists.newArrayList(this.path);
			int selected = this.scroll.selected;
			int[] a = list.get(selected);
			int[] b = list.get(selected - 1);
			list.set(selected, b);
			list.set(selected - 1, a);
			this.path = list;
			this.initGui();
			this.scroll.selected = selected - 1;
		}
		if (button.id == 2) { // remove
			List<int[]> list = Lists.newArrayList(this.path);
			if (list.size() <= 1) { return; }
			list.remove(this.scroll.selected);
			int selected = this.scroll.selected - 1;
			if (selected == -1 && list.isEmpty()) { selected = 0; }
			this.scroll.selected = selected;
			this.path = list;
			this.initGui();
		}
		this.npc.ais.setMovingPath(this.path);
	}

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
	}

	@Override
	public void initGui() {
		int sel;
		if (this.scroll!=null) { sel = this.scroll.selected; }
		else {
			sel = 0;
			Vec3d vec3d = this.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = this.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 6.0d, vec3d2.y * 6.0d, vec3d2.z * 6.0d);
			RayTraceResult result = this.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, true);
			if (result!=null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos()!=null) {
				int x = result.getBlockPos().getX();
				int y = result.getBlockPos().getY();
				int z = result.getBlockPos().getZ();
				int i = 0;
				for (int[] arr : this.path) {
					if (arr[0] == x && y == arr[1] && z == arr[2]) {
						sel = i;
						break;
					}
					i++;
				}
			}
		}
		super.initGui();
		(this.scroll = new GuiCustomScroll(this, 0)).setSize(160, 164);
		List<String> list = new ArrayList<String>();
		for (int[] arr : this.path) {
			list.add("x:" + arr[0] + " y:" + arr[1] + " z:" + arr[2]);
		}
		this.scroll.setListNotSorted(list);
		this.scroll.guiLeft = this.guiLeft + 7;
		this.scroll.guiTop = this.guiTop + 12;
		this.scroll.selected = sel;
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
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(this.path));
		Client.sendData(EnumPacketServer.MovingPathSave, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.path = NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew", 10));
		this.npc.ais.setMovingPath(this.path);
		this.initGui();
	}
}
