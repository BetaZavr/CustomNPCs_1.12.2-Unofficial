package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.NBTTags;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcPather
extends GuiNPCInterface
implements IGuiData {

	private List<int[]> path;
	private GuiCustomScroll scroll;

	public GuiNpcPather(EntityNPCInterface npc) {
		super(npc);
		drawDefaultBackground = false;
		xSize = 176;
		title = "Npc Pather";
		setBackground("smallbg.png");

		path = npc.ais.getMovingPath();
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (scroll.getSelect() < 0) {
			return;
		}
		if (button.getId() == 0) { // down
			List<int[]> list = new ArrayList<>(path);
			int selected = scroll.getSelect();
			if (list.size() <= selected + 1) {
				return;
			}
			int[] a = list.get(selected);
			int[] b = list.get(selected + 1);
			list.set(selected, b);
			list.set(selected + 1, a);
			path = list;
			initGui();
			scroll.setSelect(selected + 1);
		}
		if (button.getId() == 1) { // up
			if (scroll.getSelect() - 1 < 0) {
				return;
			}
			List<int[]> list = new ArrayList<>(path);
			int selected = scroll.getSelect();
			int[] a = list.get(selected);
			int[] b = list.get(selected - 1);
			list.set(selected, b);
			list.set(selected - 1, a);
			path = list;
			initGui();
			scroll.setSelect(selected - 1);
		}
		if (button.getId() == 2) { // remove
			List<int[]> list = new ArrayList<>(path);
			if (list.size() <= 1) {
				return;
			}
			list.remove(scroll.getSelect());
            scroll.setSelect(scroll.getSelect() - 1);
			path = list;
			initGui();
		}
		npc.ais.setMovingPath(path);
	}

	protected void drawGuiContainerBackgroundLayer(float ignoredF, int ignoredI, int ignoredJ) {
	}

	@Override
	public void initGui() {
		int sel;
		if (scroll != null) {
			sel = scroll.getSelect();
		} else {
			sel = 0;
			Vec3d vec3d = player.getPositionEyes(1.0f);
			Vec3d vec3d2 = player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 6.0d, vec3d2.y * 6.0d, vec3d2.z * 6.0d);
			RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, true);
			if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                int x = result.getBlockPos().getX();
                int y = result.getBlockPos().getY();
                int z = result.getBlockPos().getZ();
                int i = 0;
                for (int[] arr : path) {
                    if (arr[0] == x && y == arr[1] && z == arr[2]) {
                        sel = i;
                        break;
                    }
                    i++;
                }
            }
		}
		super.initGui();
		(scroll = new GuiCustomScroll(this, 0)).setSize(160, 164);
		List<String> list = new ArrayList<>();
		for (int[] arr : path) {
			list.add("x:" + arr[0] + " y:" + arr[1] + " z:" + arr[2]);
		}
		scroll.setListNotSorted(list);
		scroll.guiLeft = guiLeft + 7;
		scroll.guiTop = guiTop + 12;
		scroll.setSelect(sel);
		addScroll(scroll);
		addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + 178, 52, 20, "gui.down"));
		addButton(new GuiNpcButton(1, guiLeft + 62, guiTop + 178, 52, 20, "gui.up"));
		addButton(new GuiNpcButton(2, guiLeft + 118, guiTop + 178, 52, 20, "selectWorld.deleteButton"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.MovingPathGet);
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
	public void save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(path));
		Client.sendData(EnumPacketServer.MovingPathSave, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		path = NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew", 10));
		npc.ais.setMovingPath(path);
		initGui();
	}
}
