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
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
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
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.data.SpawnNPCData;
import noppes.npcs.util.Util;

public class GuiNpcMobSpawnerSelector
extends SubGuiInterface
implements IGuiData, ICustomScrollListener, ITextfieldListener {

	public int activeTab = 1;
	public int showingClones = 0;
	private GuiCustomScroll scroll;
	public EntityLivingBase selectNpc;
	public SpawnNPCData spawnData;

	public GuiNpcMobSpawnerSelector() {
		xSize = 256;
		closeOnEsc = true;
		setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id > 20) {
			activeTab = button.id - 20;
			initGui();
			return;
		}
		switch (button.id) {
			case 0: {
				close();
				break;
			}
			case 1: {
				scroll.clear();
				close();
				break;
			}
			case 3: {
				selectNpc = null;
				showingClones = 0;
				initGui();
				break;
			}
			case 4: {
				selectNpc = null;
				showingClones = 1;
				initGui();
				break;
			}
			case 5: {
				selectNpc = null;
				showingClones = 2;
				initGui();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (selectNpc != null) {
			drawNpc(selectNpc, 210, 80, 1.0f, (int) (3 * player.world.getTotalWorldTime() % 360), 0, 0);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(guiLeft + 181, guiTop + 4, guiLeft + 242, guiTop + 90, new Color(0xFF808080).getRGB());
		Gui.drawRect(guiLeft + 182, guiTop + 5, guiLeft + 241, guiTop + 89, new Color(0xFF000000).getRGB());
		GlStateManager.popMatrix();
	}

	public NBTTagCompound getCompound() {
		String sel = scroll.getSelected();
		if (sel == null) { return null; }
		NBTTagCompound nbtEntity = null;
		if (showingClones == 0) {
			nbtEntity = ClientCloneController.Instance.getCloneData(player, sel, activeTab);
		} else if (showingClones == 1) {
			if (spawnData != null) { nbtEntity = spawnData.compound; }
			else {
				for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
					if (ent.getName().equals(sel)) {
						Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
						if (entity instanceof EntityLivingBase) {
							nbtEntity = entity.writeToNBT(new NBTTagCompound());
							nbtEntity.setString("id", ent.getRegistryName().toString());
						}
					}
				}
			}
		}
		return nbtEntity;
	}

	public String getSelected() {
		return scroll.getSelected();
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
		GuiMenuTopButton button;
		addTopButton(button = new GuiMenuTopButton(3, guiLeft + 4, guiTop - 17, "spawner.clones"));
		button.active = showingClones == 0;
		addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = showingClones == 1;
		addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = showingClones == 2;
		if (showingClones == 0 || showingClones == 2) {
			int x = guiLeft;
			int y = guiTop + 4;
			GuiMenuSideButton sideButton;
			for (int id = 1; id < 10; id++) {
				sideButton = new GuiMenuSideButton(20 + id, x, y + (id - 1) * 21, "Tab " + id);
				addSideButton(sideButton);
			}
			getSideButton(20 + activeTab).active = true;
			showClones();
		} else {
			showEntities();
		}
		addButton(new GuiNpcButton(0, guiLeft + 171, guiTop + 170, 80, 20, "gui.done"));
		addButton(new GuiNpcButton(1, guiLeft + 171, guiTop + 192, 80, 20, "gui.cancel"));

		if (spawnData == null) {
			return;
		}

		addLabel(new GuiNpcLabel(5, new TextComponentTranslation("type.count").getFormattedText() + ":", guiLeft + 170, guiTop + 153));
		GuiNpcTextField tf = new GuiNpcTextField(2, this, guiLeft + 216, guiTop + 148, 35, 20, "" + spawnData.count);
		tf.setMinMaxDefault(0, 7, spawnData.count);
		addTextField(tf);
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode() || i == ClientProxy.backButton.getKeyCode()) {
			resetEntity();
		}
	}

	private void resetEntity() {
		String sel = scroll.getSelected();
		if (showingClones == 0) { // client
			NBTTagCompound npcNbt = ClientCloneController.Instance.getCloneData(player, sel, activeTab);
			if (npcNbt == null) {
				return;
			}
			Entity entity = EntityList.createEntityFromNBT(npcNbt, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				if (spawnData != null) {
					spawnData.typeClones = 0;
					spawnData.compound = npcNbt;
				}
				selectNpc = (EntityLivingBase) entity;
			}
		} else if (showingClones == 1) { // mob
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				if (ent.getName().equals(sel)) {
					Entity entity = EntityList.createEntityByIDFromName(Objects.requireNonNull(ent.getRegistryName()), Minecraft.getMinecraft().world);
					if (entity instanceof EntityLivingBase) {
						selectNpc = (EntityLivingBase) entity;
						if (spawnData != null) {
							spawnData.typeClones = 1;
							spawnData.compound = entity.writeToNBT(new NBTTagCompound());
							spawnData.compound.setString("id", ent.getRegistryName().toString());
						}
					}
					return;
				}
			}
		} else { // server
			if (spawnData != null) {
				spawnData.typeClones = 2;
				spawnData.compound = new NBTTagCompound();
				spawnData.compound.setString("Name", sel);
			}
			Client.sendData(EnumPacketServer.GetClone, false, sel, activeTab);
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		String sel = scroll.getSelected();
		if (sel == null) { return; }
		resetEntity();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		close();
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
		if (spawnData != null) {
			scroll.setSelected(Util.instance.deleteColor(spawnData.getTitle()));
			if (selectNpc == null) {
				String name = new TextComponentTranslation("type.empty").getFormattedText();
				if (spawnData.compound != null) {
					if (spawnData.compound.hasKey("ClonedName")) {
						name = new TextComponentTranslation(spawnData.compound.getString("ClonedName"))
								.getFormattedText();
					} else if (spawnData.compound.hasKey("Name")) {
						name = new TextComponentTranslation(spawnData.compound.getString("Name")).getFormattedText();
					}
				}
				Client.sendData(EnumPacketServer.GetClone, false, name, activeTab);
			}
		}
	}

	private void showClones() {
		if (showingClones == 2) {
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
		scroll.setList(new ArrayList<>(ClientCloneController.Instance.getClones(activeTab)));
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
				Entity entity = EntityList.createEntityByIDFromName(ent.getRegistryName(),
						Minecraft.getMinecraft().world);
				if (!(entity instanceof EntityMob)) {
					continue;
				}
				list.add(name);
				classes.add(c);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		scroll.setList(list);
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (spawnData == null || textField.getId() != 2) {
			return;
		}
		spawnData.count = textField.getInteger();
	}

}
