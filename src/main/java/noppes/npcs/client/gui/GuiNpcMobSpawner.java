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
	private List<String> list;
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
	public void drawScreen(int i, int j, float f) {
		if (this.subgui == null) {
			GlStateManager.pushMatrix();
			if (this.selectNpc != null) {
				this.drawNpc(this.selectNpc, 210, 130, 1.0f, (int) (3 * this.player.world.getTotalWorldTime() % 360), 0,
						0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(this.guiLeft + 179, this.guiTop + 54, this.guiLeft + 242, this.guiTop + 142, 0xFF808080);
			Gui.drawRect(this.guiLeft + 180, this.guiTop + 55, this.guiLeft + 241, this.guiTop + 141, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(i, j, f);
	}

	private NBTTagCompound getCompound() { // Client Clones or Vanilla Mobs
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
		this.guiTop += 10;
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 188);
		} else {
			this.scroll.clear();
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 26;
		this.addScroll(this.scroll);
		this.scroll.selected = this.sel;
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 20, GuiNpcMobSpawner.search));
		GuiMenuTopButton button;
		this.addTopButton(button = new GuiMenuTopButton(3, this.guiLeft + 4, this.guiTop - 17, "spawner.clones"));
		button.active = (GuiNpcMobSpawner.showingClones == 0);
		this.addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = (GuiNpcMobSpawner.showingClones == 1);
		this.addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = (GuiNpcMobSpawner.showingClones == 2);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 170, this.guiTop + 6, 82, 20, "spawner.spawn"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + 146, 82, 20, "spawner.mobspawner"));
		if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			GuiMenuSideButton sideButton;
			for (int id = 1; id < 10; id++) {
				sideButton = new GuiMenuSideButton(20 + id, x, y + (id - 1) * 21, "Tab " + id);
				this.addSideButton(sideButton);
			}
			this.addButton(new GuiNpcButton(6, this.guiLeft + 170, this.guiTop + 30, 82, 20, "gui.remove"));
			this.getSideButton(20 + this.activeTab).active = true;
			this.showClones();
		} else {
			this.showEntities();
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (!GuiNpcMobSpawner.search.equals(this.getTextField(1).getText())) {
			GuiNpcMobSpawner.search = this.getTextField(1).getText().toLowerCase();
			this.scroll.setList(this.getSearchList());
			this.scroll.selected = this.sel;
		}
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode()
				|| i == ClientProxy.backButton.getKeyCode()) {
			this.resetEntity();
		}
	}

	private void resetEntity() {
		if (GuiNpcMobSpawner.showingClones == 0) { // client
			NBTTagCompound npcNbt = ClientCloneController.Instance.getCloneData(this.player, this.scroll.getSelected(),
					this.activeTab);
			if (npcNbt == null) {
				return;
			}
			Entity entity = EntityList.createEntityFromNBT(npcNbt, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				this.selectNpc = (EntityLivingBase) entity;
			}
		} else if (GuiNpcMobSpawner.showingClones == 1) { // mob
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				if (ent.getName().equals(this.scroll.getSelected())) {
					Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
					if (entity instanceof EntityLivingBase) {
						this.selectNpc = (EntityLivingBase) entity;
					}
					return;
				}
			}
		} else { // server
			Client.sendData(EnumPacketServer.GetClone, false, this.scroll.getSelected(), this.activeTab);
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.resetEntity();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) {
			this.selectNpc = (EntityNPCInterface) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"),
					this.player.world);
			return;
		}
		NBTTagList nbtlist = compound.getTagList("List", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nbtlist.tagCount(); ++i) {
			list.add(nbtlist.getStringTagAt(i));
		}
		this.list = list;
		this.scroll.setList(this.getSearchList());
		this.scroll.selected = this.sel;
	}

	private void showClones() {
		if (GuiNpcMobSpawner.showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, this.activeTab);
			return;
		}
		this.list = ClientCloneController.Instance.getClones(this.activeTab);
		this.scroll.setList(this.getSearchList());
		this.scroll.selected = this.sel;
		this.resetEntity();
	}

	private void showEntities() {
		ArrayList<String> list = new ArrayList<>();
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
		this.list = list;
		this.scroll.setList(this.getSearchList());
		this.scroll.selected = this.sel;
	}

}
