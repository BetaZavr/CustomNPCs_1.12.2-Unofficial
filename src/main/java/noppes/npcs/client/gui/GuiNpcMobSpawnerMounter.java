package noppes.npcs.client.gui;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcMobSpawnerMounter extends GuiNPCInterface implements IGuiData {
	private static String search = "";
	private static int showingClones = 0;
	private int activeTab;
	private List<String> list;
	private final int posX;
	private final int posY;
	private final int posZ;
	private GuiCustomScroll scroll;

	public GuiNpcMobSpawnerMounter(int i, int j, int k) {
		this.activeTab = 1;
		this.xSize = 256;
		this.posX = i;
		this.posY = j;
		this.posZ = k;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.close();
		}
		if (button.id == 1) {
			NBTTagCompound compound = this.getCompound();
			if (compound != null) {
				compound.setTag("Pos", this.newDoubleNBTList(this.posX + 0.5, this.posY + 1, this.posZ + 0.5));
				Client.sendData(EnumPacketServer.SpawnRider, compound);
				this.close();
			}
		}
		if (button.id == 2) {
			Client.sendData(EnumPacketServer.PlayerRider);
			this.close();
		}
		if (button.id == 3) {
			GuiNpcMobSpawnerMounter.showingClones = 0;
			this.initGui();
		}
		if (button.id == 4) {
			GuiNpcMobSpawnerMounter.showingClones = 1;
			this.initGui();
		}
		if (button.id == 5) {
			GuiNpcMobSpawnerMounter.showingClones = 2;
			this.initGui();
		}
		if (button.id > 20) {
			this.activeTab = button.id - 20;
			this.initGui();
		}
	}

	private NBTTagCompound getCompound() {
		String sel = this.scroll.getSelected();
		if (sel == null) {
			return null;
		}
		if (GuiNpcMobSpawnerMounter.showingClones == 0) {
			return ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
		}
		Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(sel), Minecraft.getMinecraft().world);
		if (entity == null) {
			return null;
		}
		NBTTagCompound compound = new NBTTagCompound();
		entity.writeToNBTAtomically(compound);
		return compound;
	}

	private List<String> getSearchList() {
		if (GuiNpcMobSpawnerMounter.search.isEmpty()) {
			return new ArrayList<>(this.list);
		}
		List<String> list = new ArrayList<>();
		for (String name : this.list) {
			if (name.toLowerCase().contains(GuiNpcMobSpawnerMounter.search)) {
				list.add(name);
			}
		}
		return list;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiTop += 10;
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 188);
		} else {
			this.scroll.clear();
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 26;
		this.addScroll(this.scroll);
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 20,
				GuiNpcMobSpawnerMounter.search));
		GuiMenuTopButton button;
		this.addTopButton(button = new GuiMenuTopButton(3, this.guiLeft + 4, this.guiTop - 17, "spawner.clones"));
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 0);
		this.addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 1);
		this.addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 2);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 170, this.guiTop + 6, 82, 20, "spawner.mount"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + 50, 82, 20, "spawner.mountplayer"));
		if (GuiNpcMobSpawnerMounter.showingClones == 0 || GuiNpcMobSpawnerMounter.showingClones == 2) {
			this.addSideButton(new GuiMenuSideButton(21, this.guiLeft - 69, this.guiTop + 2, 70, 22, "Tab 1"));
			this.addSideButton(new GuiMenuSideButton(22, this.guiLeft - 69, this.guiTop + 23, 70, 22, "Tab 2"));
			this.addSideButton(new GuiMenuSideButton(23, this.guiLeft - 69, this.guiTop + 44, 70, 22, "Tab 3"));
			this.addSideButton(new GuiMenuSideButton(24, this.guiLeft - 69, this.guiTop + 65, 70, 22, "Tab 4"));
			this.addSideButton(new GuiMenuSideButton(25, this.guiLeft - 69, this.guiTop + 86, 70, 22, "Tab 5"));
			this.addSideButton(new GuiMenuSideButton(26, this.guiLeft - 69, this.guiTop + 107, 70, 22, "Tab 6"));
			this.addSideButton(new GuiMenuSideButton(27, this.guiLeft - 69, this.guiTop + 128, 70, 22, "Tab 7"));
			this.addSideButton(new GuiMenuSideButton(28, this.guiLeft - 69, this.guiTop + 149, 70, 22, "Tab 8"));
			this.addSideButton(new GuiMenuSideButton(29, this.guiLeft - 69, this.guiTop + 170, 70, 22, "Tab 9"));
			this.getSideButton(20 + this.activeTab).active = true;
			this.showClones();
		} else {
			this.showEntities();
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (GuiNpcMobSpawnerMounter.search.equals(this.getTextField(1).getText())) {
			return;
		}
		GuiNpcMobSpawnerMounter.search = this.getTextField(1).getText().toLowerCase();
		this.scroll.setList(this.getSearchList());
	}

	protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
		NBTTagList nbttaglist = new NBTTagList();
        for (int i = par1ArrayOfDouble.length, j = 0; j < i; ++j) {
			double d1 = par1ArrayOfDouble[j];
			nbttaglist.appendTag(new NBTTagDouble(d1));
		}
		return nbttaglist;
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtlist = compound.getTagList("List", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nbtlist.tagCount(); ++i) {
			list.add(nbtlist.getStringTagAt(i));
		}
		this.list = list;
		this.scroll.setList(this.getSearchList());
	}

	private void showClones() {
		if (GuiNpcMobSpawnerMounter.showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, this.activeTab);
			return;
		}
		this.list = ClientCloneController.Instance.getClones(this.activeTab);
		this.scroll.setList(this.getSearchList());
	}

	private void showEntities() {
		ArrayList<String> list = new ArrayList<>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = ent.getEntityClass();
			String name = ent.getName();
			try {
				if (!EntityLiving.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) {
					continue;
				}
				list.add(name);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		this.list = list;
		this.scroll.setList(this.getSearchList());
	}

}
