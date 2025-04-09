package noppes.npcs.client.gui.select;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCSelection
extends SubGuiInterface
implements IGuiData, ICustomScrollListener {

	public EntityNPCInterface selectEntity;
	public EntityNPCInterface main;
	private final HashMap<String, Integer> dataIDs = new HashMap<>();
	private GuiCustomScroll scroll;
	private final DecimalFormat df = new DecimalFormat("#.#");

	public GuiNPCSelection(EntityNPCInterface completer) {
		xSize = 256;
		setBackground("menubg.png");

		selectEntity = completer;
		main = completer;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			GlStateManager.pushMatrix();
			if (selectEntity != null) {
				drawNpc(selectEntity, 221, 162, 1.0f, (int) (3 * player.world.getTotalWorldTime() % 360), 0, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(guiLeft + 191, guiTop + 85, guiLeft + 252, guiTop + 171, new Color(0xFF808080).getRGB());
			Gui.drawRect(guiLeft + 192, guiTop + 86, guiLeft + 251, guiTop + 170, new Color(0xFF000000).getRGB());
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(165, 191); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 21;
		addScroll(scroll);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.RemoteNpcsGet, false);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || isInventoryKey(i)) { close(); }
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == mc.gameSettings.keyBindForward.getKeyCode() || i == mc.gameSettings.keyBindBack.getKeyCode()) {
			resetEntity();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		scroll.mouseClicked(i, j, k);
	}

	private void resetEntity() {
		selectEntity = null;
		if (dataIDs.containsKey(scroll.getSelected())) {
			Entity entity = mc.world.getEntityByID(dataIDs.get(scroll.getSelected()));
			if (!(entity instanceof EntityNPCInterface)) {
				return;
			}
			selectEntity = (EntityNPCInterface) entity;
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		resetEntity();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		close();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtList = compound.getTagList("Data", 10);
		List<String> list = new ArrayList<>();
		dataIDs.clear();
		String mainKey = ((char) 167) + "aID:-1 " + ((char) 167) + "r" + main.getName() + " " + ((char) 167) + "7" + df.format(-1.0f);
		dataIDs.put(mainKey, -1);
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			NBTTagCompound nbt = nbtList.getCompoundTagAt(i);
			Entity entity = mc.world.getEntityByID(nbt.getInteger("Id"));
			if (entity == null) { continue; }
			if (main != null && entity.getName().equals(main.getName())) { dataIDs.remove(mainKey); }
			float distance = player.getDistance(entity);
			hts.put(i, Collections.singletonList(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(distance)));
			String key = ((char) 167) + "aID:" + nbt.getInteger("Id") + " " + ((char) 167) + "r" + entity.getName() + " " + ((char) 167) + "7" + df.format(distance);
			list.add(key);
			dataIDs.put(key, nbt.getInteger("Id"));
		}
		scroll.setListNotSorted(list);
		scroll.setHoverTexts(hts);
		resetEntity();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (mc.world.getTotalWorldTime() % 40 != 0) {
			return;
		}
		for (int id : dataIDs.values()) {
			Entity entity = mc.world.getEntityByID(id);
			if (entity != null) {
				for (int i = 0; i < scroll.getList().size(); i++) {
					if (scroll.getList().get(i).contains("ID:" + id + " ")) {
						List<String> l = new ArrayList<>();
						l.add(((char) 167) + "7Name: " + ((char) 167) + "r" + new TextComponentTranslation(entity.getName()).getFormattedText());
						l.add(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(player.getDistance(entity)));
						l.add(((char) 167) + "7Class Type: " + ((char) 167) + "f" + entity.getClass().getSimpleName());
						scroll.getHoversTexts().put(i, l);
						break;
					}
				}
			}
		}
	}

}
