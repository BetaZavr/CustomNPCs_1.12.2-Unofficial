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
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiNpcMobSpawner extends GuiNPCInterface implements ICustomScrollListener, IGuiData {

	protected static int showingClones = 0;
	protected GuiCustomScroll scroll;
	protected final int posX;
	protected final int posY;
	protected final int posZ;
	protected int activeTab = 1;
	protected int sel = -1;
	public EntityLivingBase selectNpc;

	public GuiNpcMobSpawner(int x, int y, int z) {
		super();
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;

		posX = x;
		posY = y;
		posZ = z;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				if (GuiNpcMobSpawner.showingClones == 2) { // Server
					String sel = scroll.getSelected();
					if (sel == null) { return; }
					Client.sendData(EnumPacketServer.SpawnMob, true, posX, posY, posZ, sel, activeTab);
					onClosed();
				} else {
					NBTTagCompound compound = getCompound();
					if (compound == null) { return; }
					Client.sendData(EnumPacketServer.SpawnMob, false, posX, posY, posZ, compound);
					onClosed();
				}
				break;
			}
			case 2: {
				if (GuiNpcMobSpawner.showingClones == 2) {
					String sel = scroll.getSelected();
					if (sel == null) { return; }
					Client.sendData(EnumPacketServer.MobSpawner, true, posX, posY, posZ, sel, activeTab);
					onClosed();
				} else {
					NBTTagCompound compound = getCompound();
					if (compound == null) { return; }
					Client.sendData(EnumPacketServer.MobSpawner, false, posX, posY, posZ, compound);
					onClosed();
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
				if (name == null || name.isEmpty()) { return; }
				scroll.setSelect(scroll.getSelect() - 1);
				if (scroll.getSelect() < 0) {
					if (scroll.getList() == null || scroll.getList().isEmpty()) { scroll.setSelect(-1); }
					else { scroll.setSelect(0); }
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
			case 66: onClosed(); break;
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
			if (selectNpc != null) { drawNpc(selectNpc, 210, 130, 1.0f, (int) (3 * player.world.getTotalWorldTime() % 360), 0, 0); }
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
		if (sel == null) { return null; }
		if (GuiNpcMobSpawner.showingClones == 0) { return ClientCloneController.Instance.getCloneData(player, sel, activeTab); } // Client Clones
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
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(165, 188); }
		else { scroll.clear(); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 26;
		scroll.setSelect(sel);
		addScroll(scroll);

		addTopButton(new GuiMenuTopButton(3, guiLeft + 4, guiTop - 17, "spawner.clones")
				.setIsActive(GuiNpcMobSpawner.showingClones == 0));
		addTopButton(new GuiMenuTopButton(4, getTopButton(3), "spawner.entities")
				.setIsActive(GuiNpcMobSpawner.showingClones == 1));
		addTopButton(new GuiMenuTopButton(5, getTopButton(4), "gui.server")
				.setIsActive(GuiNpcMobSpawner.showingClones == 2));

		addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 6, 82, 20, "spawner.spawn"));
		addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 146, 82, 20, "spawner.mobspawner"));
		if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			for (int id = 1; id < 10; id++) {
				addSideButton(new GuiMenuSideButton(20 + id, x, y + (id - 1) * 19, "Tab " + id));
			}
			addButton(new GuiNpcButton(6, guiLeft + 170, guiTop + 30, 82, 20, "gui.remove"));
			getSideButton(20 + activeTab).setIsActive(true);
			showClones();
		}
		else { showEntities(); }
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui != null) { return subgui.keyCnpcsPressed(typedChar, keyCode); }
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_UP ||
				keyCode == Keyboard.KEY_DOWN ||
				keyCode == mc.gameSettings.keyBindForward.getKeyCode() ||
				keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
			resetEntity();
		}
		return bo;
	}

	private void resetEntity() {
		if (GuiNpcMobSpawner.showingClones == 0) { // client
			NBTTagCompound npcNbt = ClientCloneController.Instance.getCloneData(player, scroll.getSelected(), activeTab);
			if (npcNbt == null) { return; }
			Entity entity = EntityList.createEntityFromNBT(npcNbt, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) { selectNpc = (EntityLivingBase) entity; }
		} else if (GuiNpcMobSpawner.showingClones == 1) { // mob
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				if (ent.getName().equals(scroll.getSelected())) {
					Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
					if (entity instanceof EntityLivingBase) { selectNpc = (EntityLivingBase) entity; }
					return;
				}
			}
		} else { // server
			Client.sendData(EnumPacketServer.GetClone, false, scroll.getSelected(), activeTab);
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { resetEntity(); }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) {
			selectNpc = (EntityNPCInterface) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), player.world);
			return;
		}
		NBTTagList nbtList = compound.getTagList("List", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nbtList.tagCount(); ++i) { list.add(nbtList.getStringTagAt(i)); }
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
			if (Objects.requireNonNull(ent.getRegistryName()).getResourceDomain().equals(CustomNpcs.MODID)) { continue; }
			Class<? extends Entity> c = ent.getEntityClass();
			String name = ent.getName();
			try {
				if (classes.contains(c) || !EntityLiving.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) { continue; }
				list.add(name);
				classes.add(c);
			} catch (Exception e) { LogWriter.error(e); }
		}
		scroll.setList(list);
		scroll.setSelect(sel);
	}

}
