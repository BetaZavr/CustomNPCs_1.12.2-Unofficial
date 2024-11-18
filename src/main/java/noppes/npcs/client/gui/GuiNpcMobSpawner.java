package noppes.npcs.client.gui;

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
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcMobSpawner extends GuiNPCInterface implements IGuiData, ICustomScrollListener {

	private static String search = "";
	private static int showingClones = 0;
	private int activeTab;
	private final List<String> list = new ArrayList<>();
	private final int posX;
    private final int posY;
    private final int posZ;
    private int sel;
	private GuiCustomScroll scroll;
	public EntityLivingBase selectNpc;

	public GuiNpcMobSpawner(int x, int y, int z) {
		this.activeTab = 1;
		this.xSize = 256;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.sel = -1;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			this.close();
			break;
		}
		case 1: {
			if (GuiNpcMobSpawner.showingClones == 2) { // Server
				String sel = this.scroll.getSelected();
				if (sel == null) {
					return;
				}
				Client.sendData(EnumPacketServer.SpawnMob, true, this.posX, this.posY, this.posZ, sel, this.activeTab);
				this.close();
			} else {
				NBTTagCompound compound = this.getCompound();
				if (compound == null) {
					return;
				}
				Client.sendData(EnumPacketServer.SpawnMob, false, this.posX, this.posY, this.posZ, compound);
				this.close();
			}
			break;
		}
		case 2: {
			if (GuiNpcMobSpawner.showingClones == 2) {
				String sel = this.scroll.getSelected();
				if (sel == null) {
					return;
				}
				Client.sendData(EnumPacketServer.MobSpawner, true, this.posX, this.posY, this.posZ, sel,
						this.activeTab);
				this.close();
			} else {
				NBTTagCompound compound = this.getCompound();
				if (compound == null) {
					return;
				}
				Client.sendData(EnumPacketServer.MobSpawner, false, this.posX, this.posY, this.posZ, compound);
				this.close();
			}
			break;
		}
		case 3: {
			this.selectNpc = null;
			GuiNpcMobSpawner.showingClones = 0;
			this.initGui();
			break;
		}
		case 4: {
			this.selectNpc = null;
			GuiNpcMobSpawner.showingClones = 1;
			this.initGui();
			break;
		}
		case 5: {
			this.selectNpc = null;
			GuiNpcMobSpawner.showingClones = 2;
			this.initGui();
			break;
		}
		case 6: { // delete
			String name = this.scroll.getSelected();
			if (name == null || name.isEmpty()) {
				return;
			}
			this.scroll.selected--;
			if (this.scroll.selected < 0) {
				if (this.scroll.getList() == null || this.scroll.getList().isEmpty()) {
					this.scroll.selected = -1;
				} else {
					this.scroll.selected = 0;
				}
			}
			this.sel = this.scroll.selected;
			this.selectNpc = null;
			if (GuiNpcMobSpawner.showingClones == 2) {
				Client.sendData(EnumPacketServer.CloneRemove, this.activeTab, name);
				return;
			}
			ClientCloneController.Instance.removeClone(name, this.activeTab);
			this.initGui();
			break;
		}
		default: {
			if (button.id > 20) {
				this.activeTab = button.id - 20;
				this.initGui();
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
			Gui.drawRect(guiLeft + 179, guiTop + 54, guiLeft + 242, guiTop + 142, 0xFF808080);
			Gui.drawRect(guiLeft + 180, guiTop + 55, guiLeft + 241, guiTop + 141, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	// Client Clones or Vanilla Mobs
	private NBTTagCompound getCompound() {
		String sel = this.scroll.getSelected();
		if (sel == null) {
			return null;
		}
		if (GuiNpcMobSpawner.showingClones == 0) { // Client Clones
			return ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
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

	private List<String> getSearchList() {
		if (GuiNpcMobSpawner.search.isEmpty()) {
			return new ArrayList<>(this.list);
		}
		List<String> list = new ArrayList<>();
		for (String name : this.list) {
			if (name.toLowerCase().contains(GuiNpcMobSpawner.search)) {
				list.add(name);
			}
		}
		return list;
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
		scroll.selected = sel;
		addScroll(scroll);

		addTextField(new GuiNpcTextField(1, this, fontRenderer, guiLeft + 4, guiTop + 4, 165, 20, GuiNpcMobSpawner.search));

		GuiMenuTopButton button;
		addTopButton(button = new GuiMenuTopButton(3, this.guiLeft + 4, this.guiTop - 17, "spawner.clones"));
		button.active = (GuiNpcMobSpawner.showingClones == 0);
		addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = (GuiNpcMobSpawner.showingClones == 1);
		addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = (GuiNpcMobSpawner.showingClones == 2);

		addButton(new GuiNpcButton(1, this.guiLeft + 170, this.guiTop + 6, 82, 20, "spawner.spawn"));
		addButton(new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + 146, 82, 20, "spawner.mobspawner"));
		if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			GuiMenuSideButton sideButton;
			for (int id = 1; id < 10; id++) {
				sideButton = new GuiMenuSideButton(20 + id, x, y + (id - 1) * 19, "Tab " + id);
				addSideButton(sideButton);
			}
			addButton(new GuiNpcButton(6, guiLeft + 170, guiTop + 30, 82, 20, "gui.remove"));
			getSideButton(20 + activeTab).active = true;
			showClones();
		} else {
			showEntities();
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (!GuiNpcMobSpawner.search.equals(getTextField(1).getText())) {
			GuiNpcMobSpawner.search = getTextField(1).getText().toLowerCase();
			scroll.setList(getSearchList());
			scroll.selected = sel;
		}
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
	public void save() {
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		resetEntity();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) {
			selectNpc = (EntityNPCInterface) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), player.world);
			return;
		}
		NBTTagList nbtList = compound.getTagList("List", 8);
		list.clear();
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			list.add(nbtList.getStringTagAt(i));
		}
		scroll.setList(getSearchList());
		scroll.selected = sel;
	}

	private void showClones() {
		if (GuiNpcMobSpawner.showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
		list.clear();
		list.addAll(ClientCloneController.Instance.getClones(activeTab));
		scroll.setList(getSearchList());
		scroll.selected = sel;
		resetEntity();
	}

	private void showEntities() {
		list.clear();
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
		scroll.setList(getSearchList());
		scroll.selected = sel;
	}

}
