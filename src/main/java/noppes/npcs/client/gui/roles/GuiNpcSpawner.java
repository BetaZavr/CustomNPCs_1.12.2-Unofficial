package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.GuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.data.SpawnNPCData;

public class GuiNpcSpawner
extends GuiNPCInterface2
implements IGuiData, ICustomScrollListener, ITextfieldListener {

	private final JobSpawner job;
	private int slot = -1;
	private GuiCustomScroll deadScroll, aliveScroll;
	public EntityLivingBase selectNpc;
	private boolean isDead = false;

	public GuiNpcSpawner(EntityNPCInterface npc) {
		super(npc);
		job = (JobSpawner) npc.advanced.jobInterface;
		Client.sendData(EnumPacketServer.JobGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 1: { // add alive
				isDead = false;
				slot = -1;
				setSubGui(getSelector());
				break;
			}
			case 2: { // del alive
				if (isDead) { return; }
				Client.sendData(EnumPacketServer.JobSpawnerRemove, slot, false);
				initGui();
				break;
			}
			case 3: { // change alive
				if (isDead) {
					return;
				}
				setSubGui(getSelector());
				break;
			}
			case 4: { // up alive
				if (isDead || slot < 1) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, true, false);
				slot--;
				initGui();
				break;
			}
			case 5: { // down alive
				if (isDead || slot >= job.size(false)) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, false, isDead);
				slot++;
				initGui();
				break;
			}
			case 6: { // clear alive
				Client.sendData(EnumPacketServer.JobClear, false);
				break;
			}
			case 7: { // targetLost alive
				job.setDespawnOnTargetLost(false, ((GuiNpcCheckBox) button).isSelected());
				break;
			}
			case 8: { // type alive
				job.setSpawnType(false, button.getValue());
				break;
			}
			case 9: { // add Dead
				isDead = true;
				slot = -1;
				setSubGui(getSelector());
				break;
			}
			case 10: { // del Dead
				if (!isDead) {
					return;
				}
				Client.sendData(EnumPacketServer.JobSpawnerRemove, slot, true);
				initGui();
				break;
			}
			case 11: { // change Dead
				if (!isDead) {
					return;
				}
				setSubGui(getSelector());
				break;
			}
			case 12: { // up Dead
				if (!isDead || slot < 1) {
					return;
				}
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, true, true);
				slot--;
				initGui();
				break;
			}
			case 13: { // down Dead
				if (!isDead || slot >= job.size(true)) {
					return;
				}
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, false, isDead);
				slot++;
				initGui();
				break;
			}
			case 14: { // clear Dead
				Client.sendData(EnumPacketServer.JobClear, true);
				break;
			}
			case 15: { // targetLost Dead
				job.setDespawnOnTargetLost(true, ((GuiNpcCheckBox) button).isSelected());
				break;
			}
			case 16: { // type Dead
				job.setSpawnType(true, button.getValue());
				break;
			}
			case 17: { // exact
				job.exact = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 18: { // resetUpdate
				job.resetUpdate = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			default: {

			}
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		GuiNpcMobSpawnerSelector selector = (GuiNpcMobSpawnerSelector) gui;
		if (slot < 0) { slot = (isDead ? deadScroll : aliveScroll).getList().size(); }
		if (selector.showingClones == 2) {
			String selected = selector.getSelected();
			if (selected != null) {
				Client.sendData(EnumPacketServer.JobSpawnerAdd, true, isDead, slot, selected, selector.activeTab);
				Client.sendData(EnumPacketServer.GetClone, true, slot, isDead);
			}
		} else {
			NBTTagCompound nbtNpc = selector.getCompound();
			if (nbtNpc == null) {
				return;
			}
			SpawnNPCData sd = selector.spawnData;
			if (slot < 0) { slot = job.size(isDead); }
			sd.compound = nbtNpc;
			if (sd.typeClones == 0) { sd.compound.setInteger("ClonedTab", selector.activeTab); }
			job.readJobCompound(slot, isDead, sd.writeToNBT());
			Client.sendData(EnumPacketServer.JobSpawnerAdd, false, isDead, slot, sd.writeToNBT());
			Client.sendData(EnumPacketServer.GetClone, true, slot, isDead);
		}
		initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			GlStateManager.pushMatrix();
			if (selectNpc != null) { drawNpc(selectNpc, 385, 92, 1.0f, (int) (3 * player.world.getTotalWorldTime() % 360), 0, 0); }
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			drawVerticalLine(guiLeft + 178, guiTop + 4, guiTop + ySize + 12, 0xFF404040);
			drawVerticalLine(guiLeft + 353, guiTop + 4, guiTop + ySize + 12, 0xFF404040);
			Gui.drawRect(guiLeft + 355, guiTop + 13, guiLeft + 416, guiTop + 99, 0xFF808080);
			Gui.drawRect(guiLeft + 356, guiTop + 14, guiLeft + 415, guiTop + 98, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void elementClicked() { }

	private GuiNpcMobSpawnerSelector getSelector() {
		GuiNpcMobSpawnerSelector guiMSS = new GuiNpcMobSpawnerSelector();
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd == null) { sd = new SpawnNPCData(); }
		guiMSS.spawnData = sd;
		guiMSS.showingClones = sd.typeClones;
		if ((sd.typeClones == 0 || sd.typeClones == 2) && sd.compound != null && sd.compound.hasKey("ClonedTab", 3)) { guiMSS.activeTab = sd.compound.getInteger("ClonedTab"); }
		return guiMSS;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (aliveScroll == null) { (aliveScroll = new GuiCustomScroll(this, 1)).setSize(172, 101); }
		aliveScroll.guiLeft = guiLeft + 5;
		aliveScroll.guiTop = guiTop + 14;
		if (!isDead) {
			if (slot >= 0 && slot < aliveScroll.getList().size()) {
				aliveScroll.selected = slot;
			} else {
				slot = -1;
				aliveScroll.selected = -1;
				selectNpc = null;
			}
		} else { aliveScroll.selected = -1; }
		addScroll(aliveScroll);

		if (deadScroll == null) { (deadScroll = new GuiCustomScroll(this, 0)).setSize(172, 101); }
		deadScroll.guiLeft = guiLeft + 180;
		deadScroll.guiTop = guiTop + 14;
		if (isDead) {
			if (slot >= 0 && slot < deadScroll.getList().size()) {
				deadScroll.selected = slot;
			} else {
				slot = -1;
				deadScroll.selected = -1;
				selectNpc = null;
			}
		} else { deadScroll.selected = -1; }
		addScroll(deadScroll);

		GuiNpcLabel label = new GuiNpcLabel(1, "spawner.list.0", guiLeft + 6, guiTop + 4);
		label.setHoverText(new TextComponentTranslation("spawner.hover.list.0").appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText());
		addLabel(label);
		label = new GuiNpcLabel(2, "spawner.list.1", guiLeft + 182, guiTop + 4);
		label.setHoverText(new TextComponentTranslation("spawner.hover.list.1").appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText());
		addLabel(label);
			// Alive
		ITextComponent strAlive = new TextComponentTranslation("spawner.hover.sp.0");
		// add
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 5, guiTop + 116, 56, 20, "gui.add");
		button.setHoverText(new TextComponentTranslation("spawner.hover.add").appendSibling(strAlive).getFormattedText());
		addButton(button);
		// del
		button = new GuiNpcButton(2, guiLeft + 63, guiTop + 116, 56, 20, "gui.remove");
		button.setEnabled(!isDead && slot >= 0);
		button.setHoverText(new TextComponentTranslation("spawner.hover.del").appendSibling(strAlive).getFormattedText());
		addButton(button);
		// edit mode
		button = new GuiNpcButton(3, guiLeft + 121, guiTop + 116, 56, 20, "advanced.editingmode");
		button.setEnabled(!isDead && slot >= 0);
		button.setHoverText(new TextComponentTranslation("spawner.hover.change").appendSibling(strAlive).getFormattedText());
		addButton(button);
		// up
		button = new GuiNpcButton(4, guiLeft + 5, guiTop + 138, 56, 20, "type.up");
		button.setEnabled(!isDead && slot >= 0 && slot >= 1);
		button.setHoverText(new TextComponentTranslation("spawner.hover.up").appendSibling(strAlive).getFormattedText());
		addButton(button);
		// down
		button = new GuiNpcButton(5, guiLeft + 63, guiTop + 138, 56, 20, "type.down");
		button.setEnabled(!isDead && slot >= 0 && slot < (aliveScroll.getList().size() - 1));
		button.setHoverText(new TextComponentTranslation("spawner.hover.down").appendSibling(strAlive).getFormattedText());
		addButton(button);
		// clear
		button = new GuiNpcButton(6, guiLeft + 121, guiTop + 138, 56, 20, "gui.clear");
		button.setEnabled(!aliveScroll.getList().isEmpty());
		button.setHoverText(new TextComponentTranslation("spawner.hover.clear").appendSibling(strAlive).getFormattedText());
		addButton(button);

		addLabel(new GuiNpcLabel(4, "type.offset", guiLeft + 6, guiTop + 166));
		addLabel(new GuiNpcLabel(5, "X:", guiLeft + 44, guiTop + 166));
		int[] set = job.getOffset(false);
		if (set == null) { set = new int[] { 0, 0, 0 }; }
		if (set.length != 3) {
			int[] ns = new int[] { 0, 0, 0 };
            System.arraycopy(set, 0, ns, 0, set.length);
			set = ns;
		}
		GuiNpcTextField textField = new GuiNpcTextField(0, this, guiLeft + 52, guiTop + 161, 35, 15, "" + set[0]);
		textField.setMinMaxDefault(0, 5, set[0]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "X").appendSibling(strAlive).getFormattedText());
		addTextField(textField);

		addLabel(new GuiNpcLabel(6, "Y:", guiLeft + 89, guiTop + 166));
		textField = new GuiNpcTextField(1, this, guiLeft + 97, guiTop + 161, 35, 15, "" + set[1]);
		textField.setMinMaxDefault(0, 5, set[1]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Y").appendSibling(strAlive).getFormattedText());
		addTextField(textField);

		addLabel(new GuiNpcLabel(7, "Z:", guiLeft + 134, guiTop + 166));
		textField = new GuiNpcTextField(2, this, guiLeft + 142, guiTop + 161, 35, 15, "" + set[2]);
		textField.setMinMaxDefault(0, 5, set[2]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Z").appendSibling(strAlive).getFormattedText());
		addTextField(textField);

		button = new GuiNpcCheckBox(7, guiLeft + 5, guiTop + 176, 170, 14, "spawner.despawn", null, job.getDespawnOnTargetLost(false));
		button.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost").appendSibling(strAlive).getFormattedText());
		addButton(button);

		addLabel(new GuiNpcLabel(8, "spawner.type", guiLeft + 5, guiTop + 195));
		button = new GuiNpcButton(8, guiLeft + 63, guiTop + 190, 55, 20, new String[] { "spawner.one", "spawner.all", "spawner.random" }, job.getSpawnType(false));
		button.setHoverText(new TextComponentTranslation("spawner.hover.type.0."+job.getSpawnType(false)).appendSibling(strAlive).getFormattedText());
		addButton(button);

			// Dead
		ITextComponent strDead = new TextComponentTranslation("spawner.hover.sp.1");
		// add
		button = new GuiNpcButton(9, guiLeft + 180, guiTop + 116, 56, 20, "gui.add");
		button.setHoverText(new TextComponentTranslation("spawner.hover.add").appendSibling(strDead).getFormattedText());
		addButton(button);
		// del
		button = new GuiNpcButton(10, guiLeft + 238, guiTop + 116, 56, 20, "gui.remove");
		button.setEnabled(slot >= 0);
		button.setHoverText(new TextComponentTranslation("spawner.hover.del").appendSibling(strDead).getFormattedText());
		addButton(button);
		// edit mode
		button = new GuiNpcButton(11, guiLeft + 296, guiTop + 116, 56, 20, "advanced.editingmode");
		button.setEnabled(isDead && slot >= 0);
		button.setHoverText(new TextComponentTranslation("spawner.hover.change").appendSibling(strDead).getFormattedText());
		addButton(button);
		// up
		button = new GuiNpcButton(12, guiLeft + 180, guiTop + 138, 56, 20, "type.up");
		button.setEnabled(isDead && slot >= 0 && slot >= 1);
		button.setHoverText(new TextComponentTranslation("spawner.hover.up").appendSibling(strDead).getFormattedText());
		addButton(button);
		// down
		button = new GuiNpcButton(13, guiLeft + 238, guiTop + 138, 56, 20, "type.down");
		button.setEnabled(isDead && slot >= 0 && slot < (deadScroll.getList().size() - 1));
		button.setHoverText(new TextComponentTranslation("spawner.hover.down").appendSibling(strDead).getFormattedText());
		addButton(button);
		// clear
		button = new GuiNpcButton(14, guiLeft + 296, guiTop + 138, 56, 20, "gui.clear");
		button.setEnabled(!deadScroll.getList().isEmpty());
		button.setHoverText(new TextComponentTranslation("spawner.hover.clear").appendSibling(strDead).getFormattedText());
		addButton(button);

		addLabel(new GuiNpcLabel(9, "type.offset", guiLeft + 181, guiTop + 166));
		addLabel(new GuiNpcLabel(10, "X:", guiLeft + 219, guiTop + 166));
		set = job.getOffset(true);
		if (set == null) {
			set = new int[] { 0, 0, 0 };
		}
		if (set.length != 3) {
			int[] ns = new int[] { 0, 0, 0 };
            System.arraycopy(set, 0, ns, 0, set.length);
			set = ns;
		}
		textField = new GuiNpcTextField(3, this, guiLeft + 227, guiTop + 161, 35, 15, "" + set[0]);
		textField.setMinMaxDefault(0, 5, set[0]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "X").appendSibling(strDead).getFormattedText());
		addTextField(textField);

		addLabel(new GuiNpcLabel(11, "Y:", guiLeft + 264, guiTop + 166));
		textField = new GuiNpcTextField(4, this, guiLeft + 272, guiTop + 161, 35, 15, "" + set[1]);
		textField.setMinMaxDefault(0, 5, set[1]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Y").appendSibling(strDead).getFormattedText());
		addTextField(textField);

		addLabel(new GuiNpcLabel(12, "Z:", guiLeft + 309, guiTop + 166));
		textField = new GuiNpcTextField(5, this, guiLeft + 317, guiTop + 161, 35, 15, "" + set[2]);
		textField.setMinMaxDefault(0, 5, set[2]);
		textField.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Z").appendSibling(strDead).getFormattedText());
		addTextField(textField);

		button = new GuiNpcCheckBox(15, guiLeft + 180, guiTop + 176, 170, 14, "spawner.despawn", null, job.getDespawnOnTargetLost(true));
		button.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost").appendSibling(strAlive).getFormattedText());
		addButton(button);

		addLabel(new GuiNpcLabel(13, "spawner.type", guiLeft + 180, guiTop + 195));
		button = new GuiNpcButton(16, guiLeft + 238, guiTop + 190, 55, 20, new String[] { "spawner.one", "spawner.all", "spawner.random" }, job.getSpawnType(true));
		button.setHoverText(new TextComponentTranslation("spawner.hover.type.1."+job.getSpawnType(true)).appendSibling(strAlive).getFormattedText());
		addButton(button);

			// Both
		ITextComponent strBoth = new TextComponentTranslation("spawner.hover.sp.2");
		button = new GuiNpcCheckBox(17, guiLeft + 357, guiTop + 161, 98, 14, "type.exact", null, job.exact);
		button.setHoverText(new TextComponentTranslation("spawner.hover.exact").appendSibling(strBoth).getFormattedText());
		addButton(button);

		button = new GuiNpcCheckBox(18, guiLeft + 357, guiTop + 176, 98, 14, "script.update", null, job.resetUpdate);
		button.setHoverText(new TextComponentTranslation("spawner.hover.reset").appendSibling(strBoth).getFormattedText());
		addButton(button);

		// cooldown
		label = new GuiNpcLabel(14, "spawner.cooldown", guiLeft + 358, guiTop + 132);
		label.enabled = !aliveScroll.getList().isEmpty();
		addLabel(label);
		textField = new GuiNpcTextField(6, this, guiLeft + 357, guiTop + 144, 55, 15, "" + job.getCooldown() / 50L);
		textField.setMinMaxDefault(0, 6000, (int) (job.getCooldown() / 50L));
		textField.setVisible(!aliveScroll.getList().isEmpty());
		textField.setHoverText("spawner.hover.cooldown");
		addTextField(textField);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode() || i == ClientProxy.backButton.getKeyCode()) {
			resetEntity();
		}
	}

	private void resetEntity() {
		String sel = (isDead ? deadScroll : aliveScroll).getSelected();
		if (sel == null) {
			selectNpc = null;
			return;
		}
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd == null) {
			selectNpc = null;
			return;
		}
		if (sd.typeClones == 2) {
			Client.sendData(EnumPacketServer.GetClone, false, sel, sd.compound.getInteger("Tab"));
		} else {
			Entity entity = EntityList.createEntityFromNBT(sd.compound, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				selectNpc = (EntityLivingBase) entity;
			}
		}
	}

	@Override
	public void save() {
		NBTTagCompound compound = job.writeToNBT(new NBTTagCompound());
		job.removeCompound(compound);
		Client.sendData(EnumPacketServer.JobSave, compound);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		slot = scroll.selected;
		isDead = scroll.id == 0;
		(isDead ? aliveScroll : deadScroll).selected = -1;
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd != null && sd.compound != null) { Client.sendData(EnumPacketServer.GetClone, true, slot, isDead); }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		setSubGui(getSelector());
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) { // Entity set
			selectNpc = (EntityLivingBase) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), player.world);
			return;
		}
		// Setts
		char chr = ((char) 167);
		job.readFromNBT(compound);
		for (int j = 0; j < 2; j++) {
			boolean type = j == 0;
			List<String> list = new ArrayList<>();
			for (int i = 0; i < job.size(type); i++) {
				SpawnNPCData sd = job.get(i, type);
				list.add(chr + "7" + (i + 1) + ": " + chr + "r" + sd.getTitle() + chr + (sd.typeClones == 0 ? "a (Client)" : sd.typeClones == 1 ? "c (Mob)" : "b (Server)"));
			}
			(type ? deadScroll : aliveScroll).setListNotSorted(list);
		}
		// Data
		if (compound.hasKey("SetPos", 3)) {
			slot = compound.getInteger("SetPos");
		}
		if (compound.hasKey("SetDead", 1)) {
			isDead = compound.getBoolean("SetDead");
			(isDead ? deadScroll : aliveScroll).selected = -1;
		}
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd != null && sd.compound != null) {
			Client.sendData(EnumPacketServer.GetClone, true, slot, isDead);
		}
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
			case 0: { // X alive
				job.getOffset(false)[0] = textField.getInteger();
				break;
			}
			case 1: { // Y alive
				job.getOffset(false)[1] = textField.getInteger();
				break;
			}
			case 2: { // Z alive
				job.getOffset(false)[2] = textField.getInteger();
				break;
			}
			case 3: { // X dead
				job.getOffset(true)[0] = textField.getInteger();
				break;
			}
			case 4: { // Y dead
				job.getOffset(true)[1] = textField.getInteger();
				break;
			}
			case 5: { // Z dead
				job.getOffset(true)[2] = textField.getInteger();
				break;
			}
			case 6: { // cooldown
				job.setCooldown(textField.getInteger());
				break;
			}
			default: {

			}
		}
	}
}
