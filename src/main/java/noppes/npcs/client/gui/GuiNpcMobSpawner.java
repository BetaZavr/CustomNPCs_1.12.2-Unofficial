package noppes.npcs.client.gui;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcMobSpawner
extends GuiNPCInterface
implements IGuiData, ICustomScrollListener {

	private static int showingClones = 0;
	private int activeTab = 1;
	private final int posX;
    private final int posY;
    private final int posZ;
    private int sel = -1;
	private GuiCustomScroll scroll;
	public EntityLivingBase selectNpc;

	public GuiNpcMobSpawner(int x, int y, int z) {
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
			case 0: {
				close();
				break;
			}
			case 1: {
				if (GuiNpcMobSpawner.showingClones == 2) { // Server
					String sel = scroll.getSelected();
					if (sel == null) {
						return;
					}
					Client.sendData(EnumPacketServer.SpawnMob, true, posX, posY, posZ, sel, activeTab);
					close();
				} else {
					NBTTagCompound compound = getCompound();
					if (compound == null) {
						return;
					}
					Client.sendData(EnumPacketServer.SpawnMob, false, posX, posY, posZ, compound);
					close();
				}
				break;
			}
			case 2: {
				if (GuiNpcMobSpawner.showingClones == 2) {
					String sel = scroll.getSelected();
					if (sel == null) {
						return;
					}
					Client.sendData(EnumPacketServer.MobSpawner, true, posX, posY, posZ, sel, activeTab);
					close();
				} else {
					NBTTagCompound compound = getCompound();
					if (compound == null) {
						return;
					}
					Client.sendData(EnumPacketServer.MobSpawner, false, posX, posY, posZ, compound);
					close();
				}
				break;
			}
			case 3: {
				selectNpc = null;
				GuiNpcMobSpawner.showingClones = 0;
				initGui();
				break;
			}
			case 4: {
				selectNpc = null;
				GuiNpcMobSpawner.showingClones = 1;
				initGui();
				break;
			}
			case 5: {
				selectNpc = null;
				GuiNpcMobSpawner.showingClones = 2;
				initGui();
				break;
			}
			case 6: { // delete
				String name = scroll.getSelected();
				if (name == null || name.isEmpty()) {
					return;
				}
				scroll.setSelect(scroll.getSelect() - 1);
				if (scroll.getSelect() < 0) {
					if (scroll.getList() == null || scroll.getList().isEmpty()) {
						scroll.setSelect(-1);
					} else {
						scroll.setSelect(0);
					}
				}
				sel = scroll.getSelect();
				selectNpc = null;
				if (GuiNpcMobSpawner.showingClones == 2) {
					Client.sendData(EnumPacketServer.CloneRemove, activeTab, name);
					return;
				}
				ClientCloneController.Instance.removeClone(name, activeTab);
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

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null && player != null) {
			GlStateManager.pushMatrix();
			if (selectNpc != null) {
				drawNpc(selectNpc, 210, 130, 1.0f, (int) (3 * player.world.getTotalWorldTime() % 360), 0, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(guiLeft + 179, guiTop + 54, guiLeft + 242, guiTop + 142, new Color(0xFF808080).getRGB());
			Gui.drawRect(guiLeft + 180, guiTop + 55, guiLeft + 241, guiTop + 141, new Color(0xFF000000).getRGB());
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	// Client Clones or Vanilla Mobs
	private NBTTagCompound getCompound() {
		String sel = scroll.getSelected();
		if (sel == null) {
			return null;
		}
		if (GuiNpcMobSpawner.showingClones == 0) { // Client Clones
			return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
		}
		// Vanilla Mobs
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			if (ent.getName().equals(scroll.getSelected())) {
				Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
				if (entity instanceof EntityLivingBase) {
					NBTTagCompound compound = new NBTTagCompound();
					entity.writeToNBTAtomically(compound);
					return compound;
				}
				break;
			}
		}
		return null;
	}

	@Override
	public void initGui() {
		super.initGui();
		guiTop += 10;
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(165, 188);
		}
		else { scroll.clear(); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 26;
		scroll.setSelect(sel);
		addScroll(scroll);

		GuiMenuTopButton button;
		addTopButton(button = new GuiMenuTopButton(3, guiLeft + 4, guiTop - 17, "spawner.clones"));
		button.active = (GuiNpcMobSpawner.showingClones == 0);
		addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = (GuiNpcMobSpawner.showingClones == 1);
		addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = (GuiNpcMobSpawner.showingClones == 2);

		addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 6, 82, 20, "spawner.spawn"));
		addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 146, 82, 20, "spawner.mobspawner"));
		if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			for (int id = 1; id < 10; id++) {
				addSideButton(new GuiMenuSideButton(20 + id, x, y + (id - 1) * 19, "Tab " + id));
			}
			addButton((IGuiNpcButton) new GuiNpcButton(6, guiLeft + 170, guiTop + 30, 82, 20, "gui.remove"));
			getSideButton(20 + activeTab).setActive(true);
			showClones();
		} else {
			showEntities();
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode() || i == ClientProxy.backButton.getKeyCode()) {
			resetEntity();
		}
	}

	private void resetEntity() {
		if (GuiNpcMobSpawner.showingClones == 0) { // client
			NBTTagCompound npcNbt = ClientCloneController.Instance.getCloneData(player, scroll.getSelected(), activeTab);
			if (npcNbt == null) {
				return;
			}
			Entity entity = EntityList.createEntityFromNBT(npcNbt, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				selectNpc = (EntityLivingBase) entity;
			}
		} else if (GuiNpcMobSpawner.showingClones == 1) { // mob
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				if (ent.getName().equals(scroll.getSelected())) {
					Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
					if (entity instanceof EntityLivingBase) {
						selectNpc = (EntityLivingBase) entity;
					}
					return;
				}
			}
		} else { // server
			Client.sendData(EnumPacketServer.GetClone, false, scroll.getSelected(), activeTab);
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		resetEntity();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) {
			selectNpc = (EntityNPCInterface) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), player.world);
			return;
		}
		NBTTagList nbtList = compound.getTagList("List", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			list.add(nbtList.getStringTagAt(i));
		}
		scroll.setList(list);
		scroll.setSelect(sel);
	}

	private void showClones() {
		if (GuiNpcMobSpawner.showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
		scroll.setList(ClientCloneController.Instance.getClones(activeTab));
		scroll.setSelect(sel);
		resetEntity();
	}

	private void showEntities() {
		List<String> list = new ArrayList<>();
		List<Class<? extends Entity>> classes = new ArrayList<>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			if (Objects.requireNonNull(ent.getRegistryName()).getResourceDomain().equals(CustomNpcs.MODID)) {
				continue;
			}
			Class<? extends Entity> c = ent.getEntityClass();
			String name = ent.getName();
			try {
				if (classes.contains(c) || !EntityLiving.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) {
					continue;
				}
				list.add(name);
				classes.add(c);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		scroll.setList(list);
		scroll.setSelect(sel);
	}

}
