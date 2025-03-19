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
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcMobSpawnerMounter
extends GuiNPCInterface
implements IGuiData {

	private static int showingClones = 0;
	private int activeTab = 1;
	private final int posX;
	private final int posY;
	private final int posZ;
	private GuiCustomScroll scroll;

	public GuiNpcMobSpawnerMounter(int x, int y, int z) {
		xSize = 256;
		closeOnEsc = true;
		setBackground("menubg.png");

		posX = x;
		posY = y;
		posZ = z;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0 : close(); break;
			case 1: {
				NBTTagCompound compound = getCompound();
				if (compound != null) {
					compound.setTag("Pos", newDoubleNBTList(posX + 0.5, posY + 1, posZ + 0.5));
					Client.sendData(EnumPacketServer.SpawnRider, compound);
					close();
				}
				break;
			}
			case 2: {
				Client.sendData(EnumPacketServer.PlayerRider);
				close();
				break;
			}
			case 3: {
				GuiNpcMobSpawnerMounter.showingClones = 0;
				initGui();
				break;
			}
			case 4: {
				GuiNpcMobSpawnerMounter.showingClones = 1;
				initGui();
				break;
			}
			case 5: {
				GuiNpcMobSpawnerMounter.showingClones = 2;
				initGui();
				break;
			}
			default: {
				if (button.getID() > 20) {
					activeTab = button.getID() - 20;
					initGui();
				}
				break;
			}
		}
	}

	private NBTTagCompound getCompound() {
		String sel = scroll.getSelected();
		if (sel == null) {
			return null;
		}
		if (GuiNpcMobSpawnerMounter.showingClones == 0) {
			return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
		}
		Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(sel), Minecraft.getMinecraft().world);
		if (entity == null) {
			return null;
		}
		NBTTagCompound compound = new NBTTagCompound();
		entity.writeToNBTAtomically(compound);
		return compound;
	}

	@Override
	public void initGui() {
		super.initGui();
		guiTop += 10;
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(165, 188);
		} else {
			scroll.clear();
		}
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 26;
		addScroll(scroll);
		// clones
		GuiMenuTopButton button = new GuiMenuTopButton(3, guiLeft + 4, guiTop - 17, "spawner.clones");
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 0);
		addTopButton(button);
		// entities
		button = new GuiMenuTopButton(4, button, "spawner.entities");
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 1);
		addTopButton(button);
		// server
		button = new GuiMenuTopButton(5, button, "gui.server");
		button.active = (GuiNpcMobSpawnerMounter.showingClones == 2);
		addTopButton(button);
		// mount
		addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 6, 82, 20, "spawner.mount"));
		// mountplayer
		addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 50, 82, 20, "spawner.mountplayer"));
		if (GuiNpcMobSpawnerMounter.showingClones == 0 || GuiNpcMobSpawnerMounter.showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			GuiMenuSideButton sideButton;
			for (int id = 1; id < 10; id++) {
				sideButton = new GuiMenuSideButton(20 + id, x, y + (id - 1) * 21, "Tab " + id);
				addSideButton(sideButton);
			}
			getSideButton(20 + activeTab).setActive(true);
			showClones();
		} else {
			showEntities();
		}
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
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtList = compound.getTagList("List", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			list.add(nbtList.getStringTagAt(i));
		}
		scroll.setList(list);
	}

	private void showClones() {
		if (GuiNpcMobSpawnerMounter.showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
		scroll.setList(ClientCloneController.Instance.getClones(activeTab));
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
		scroll.setList(list);
	}

}
