package noppes.npcs.client.gui.select;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCSelection extends SubGuiInterface implements IGuiData, ICustomScrollListener {

	public EntityNPCInterface selectEntity, main;
	private final HashMap<String, Integer> dataIDs;
	private GuiCustomScroll scroll;
	private String search = "";
	private List<String> list;
	private final DecimalFormat df = new DecimalFormat("#.#");
	private final char chr = ((char) 167);

	public GuiNPCSelection(EntityNPCInterface completer) {
		this.selectEntity = completer;
		this.main = completer;
		this.dataIDs = new HashMap<>();
		this.xSize = 256;
		this.setBackground("menubg.png");
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.subgui == null) {
			GlStateManager.pushMatrix();
			if (this.selectEntity != null) {
				this.drawNpc(this.selectEntity, 221, 162, 1.0f, (int) (3 * this.player.world.getTotalWorldTime() % 360), 0, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(this.guiLeft + 191, this.guiTop + 85, this.guiLeft + 252, this.guiTop + 171, 0xFF808080);
			Gui.drawRect(this.guiLeft + 192, this.guiTop + 86, this.guiLeft + 251, this.guiTop + 170, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.edit").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private List<String> getSearchList() {
		if (this.search.isEmpty()) {
			return new ArrayList<>(this.list);
		}
		List<String> list = new ArrayList<>();
		for (String name : this.list) {
			if (name.toLowerCase().contains(this.search)) {
				list.add(name);
			}
		}
		return list;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 191);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 21;
		this.addScroll(this.scroll);

		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 15,
				this.search));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.RemoteNpcsGet, false);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode()
				|| i == ClientProxy.backButton.getKeyCode()) {
			this.resetEntity();
		}
		if (this.search.equals(this.getTextField(1).getText())) {
			return;
		}
		this.search = this.getTextField(1).getText().toLowerCase();
		this.scroll.setList(this.getSearchList());
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

	private void resetEntity() {
		this.selectEntity = null;
		if (this.dataIDs.containsKey(this.scroll.getSelected())) {
			Entity entity = this.mc.world.getEntityByID(this.dataIDs.get(this.scroll.getSelected()));
			if (!(entity instanceof EntityNPCInterface)) {
				return;
			}
			this.selectEntity = (EntityNPCInterface) entity;
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.resetEntity();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.close();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtlist = compound.getTagList("Data", 10);
		List<String> list = new ArrayList<>();
		String[][] hs = new String[nbtlist.tagCount()][];
		this.dataIDs.clear();
		String mainKey = chr + "aID:-1 " + chr + "r" + this.main.getName() + " " + chr + "7" + this.df.format(-1.0f);
		this.dataIDs.put(mainKey, -1);
		for (int i = 0; i < nbtlist.tagCount(); ++i) {
			NBTTagCompound nbt = nbtlist.getCompoundTagAt(i);
			Entity entity = this.mc.world.getEntityByID(nbt.getInteger("Id"));
			if (entity == null) {
				continue;
			}
			if (this.main != null && entity.getName().equals(this.main.getName())) {
				this.dataIDs.remove(mainKey);
			}
			float distance = this.player.getDistance(entity);
			hs[i] = new String[] { chr + "7Distance Of: " + chr + "6" + this.df.format(distance) };
			String key = chr + "aID:" + nbt.getInteger("Id") + " " + chr + "r" + entity.getName() + " " + chr + "7"
					+ this.df.format(distance);
			list.add(key);
			this.dataIDs.put(key, nbt.getInteger("Id"));
		}
		this.list = list;
		this.scroll.setListNotSorted(this.getSearchList());
		this.scroll.hoversTexts = hs;
		this.resetEntity();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (this.mc.world.getTotalWorldTime() % 40 != 0) {
			return;
		}
		for (int id : this.dataIDs.values()) {
			Entity entity = this.mc.world.getEntityByID(id);
			if (entity != null) {
				float distance = this.player.getDistance(entity);
				for (int i = 0; i < this.scroll.getList().size(); i++) {
					if (this.scroll.getList().get(i).contains("ID:" + id + " ")) {
						this.scroll.hoversTexts[i] = new String[] {
								chr + "7Name: " + chr + "r"
										+ new TextComponentTranslation(entity.getName()).getFormattedText(),
								chr + "7Distance Of: " + chr + "6" + this.df.format(distance),
								chr + "7Class Type: " + chr + "f" + entity.getClass().getSimpleName() };
						break;
					}
				}
			}
		}
	}

}
