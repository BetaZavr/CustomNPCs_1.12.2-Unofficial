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
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.data.SpawnNPCData;

import javax.annotation.Nonnull;

public class GuiNpcSpawner extends GuiNPCInterface2 implements IGuiData, ICustomScrollListener, ITextfieldListener {

	protected GuiCustomScroll deadScroll;
	protected GuiCustomScroll aliveScroll;
	protected EntityLivingBase selectNpc;
	protected final JobSpawner job;
	protected int slot = -1;
	protected boolean isDead = false;

	public GuiNpcSpawner(EntityNPCInterface npc) {
		super(npc);
		parentGui = EnumGuiType.MainMenuAdvanced;

		job = (JobSpawner) npc.advanced.jobInterface;
		Client.sendData(EnumPacketServer.JobGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				isDead = false;
				slot = -1;
				setSubGui(getSelector());
				break;
			} // add alive
			case 2: {
				if (isDead) { return; }
				Client.sendData(EnumPacketServer.JobSpawnerRemove, slot, false);
				initGui();
				break;
			} // del alive
			case 3: {
				if (isDead) { return; }
				setSubGui(getSelector());
				break;
			} // change alive
			case 4: {
				if (isDead || slot < 1) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, true, false);
				slot--;
				initGui();
				break;
			} // up alive
			case 5: {
				if (isDead || slot >= job.size(false)) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, false, isDead);
				slot++;
				initGui();
				break;
			} // down alive
			case 6: Client.sendData(EnumPacketServer.JobClear, false); break; // clear alive
			case 7: job.setDespawnOnTargetLost(false, ((GuiNpcCheckBox) button).isSelected()); break; // targetLost alive
			case 8: job.setSpawnType(false, button.getValue()); break; // type alive
			case 9: {
				isDead = true;
				slot = -1;
				setSubGui(getSelector());
				break;
			} // add Dead
			case 10: {
				if (!isDead) { return; }
				Client.sendData(EnumPacketServer.JobSpawnerRemove, slot, true);
				initGui();
				break;
			} // del Dead
			case 11: {
				if (!isDead) { return; }
				setSubGui(getSelector());
				break;
			} // change Dead
			case 12: {
				if (!isDead || slot < 1) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, true, true);
				slot--;
				initGui();
				break;
			} // up Dead
			case 13: {
				if (!isDead || slot >= job.size(true)) { return; }
				Client.sendData(EnumPacketServer.SpawnerNpcMove, slot, false, isDead);
				slot++;
				initGui();
				break;
			} // down Dead
			case 14: Client.sendData(EnumPacketServer.JobClear, true); break; // clear Dead
			case 15: job.setDespawnOnTargetLost(true, ((GuiNpcCheckBox) button).isSelected()); break; // targetLost Dead
			case 16: job.setSpawnType(true, button.getValue()); break; // type Dead
			case 17: job.exact = ((GuiNpcCheckBox) button).isSelected(); break; // exact
			case 18: job.resetUpdate = ((GuiNpcCheckBox) button).isSelected(); break; // resetUpdate
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface gui) {
		SubGuiNpcMobSpawnerSelector selector = (SubGuiNpcMobSpawnerSelector) gui;
		if (slot < 0) { slot = (isDead ? deadScroll : aliveScroll).getList().size(); }
		if (selector.showingClones == 2) {
			String selected = selector.getSelected();
			if (selected != null) {
				Client.sendData(EnumPacketServer.JobSpawnerAdd, true, isDead, slot, selected, selector.activeTab);
				Client.sendData(EnumPacketServer.GetClone, true, slot, isDead);
			}
		}
		else {
			NBTTagCompound nbtNpc = selector.getCompound();
			if (nbtNpc == null) { return; }
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

	private SubGuiNpcMobSpawnerSelector getSelector() {
		SubGuiNpcMobSpawnerSelector guiMSS = new SubGuiNpcMobSpawnerSelector();
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd == null) { sd = new SpawnNPCData(npc.world); }
		guiMSS.spawnData = sd;
		guiMSS.showingClones = sd.typeClones;
		if ((sd.typeClones == 0 || sd.typeClones == 2) && sd.compound != null && sd.compound.hasKey("ClonedTab", 3)) { guiMSS.activeTab = sd.compound.getInteger("ClonedTab"); }
		return guiMSS;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (aliveScroll == null) { aliveScroll = new GuiCustomScroll(this, 1).setSize(172, 101); }
		aliveScroll.guiLeft = guiLeft + 5;
		aliveScroll.guiTop = guiTop + 14;
		if (!isDead) {
			if (slot >= 0 && slot < aliveScroll.getList().size()) { aliveScroll.setSelect(slot); }
			else {
				slot = -1;
				aliveScroll.setSelect(-1);
				selectNpc = null;
			}
		}
		else { aliveScroll.setSelect(-1); }
		addScroll(aliveScroll);
		if (deadScroll == null) { deadScroll = new GuiCustomScroll(this, 0).setSize(172, 101); }
		deadScroll.guiLeft = guiLeft + 180;
		deadScroll.guiTop = guiTop + 14;
		if (isDead) {
			if (slot >= 0 && slot < deadScroll.getList().size()) { deadScroll.setSelect(slot); }
			else {
				slot = -1;
				deadScroll.setSelect(-1);
				selectNpc = null;
			}
		} else { deadScroll.setSelect(-1); }
		addScroll(deadScroll);
		addLabel(new GuiNpcLabel(1, "spawner.list.0", guiLeft + 6, guiTop + 4)
				.setHoverText(new TextComponentTranslation("spawner.hover.list.0")
						.appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText()));
		addLabel(new GuiNpcLabel(2, "spawner.list.1", guiLeft + 182, guiTop + 4)
				.setHoverText(new TextComponentTranslation("spawner.hover.list.1")
						.appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText()));
			// Alive
		ITextComponent strAlive = new TextComponentTranslation("spawner.hover.sp.0");
		// add
		addButton(new GuiNpcButton(1, guiLeft + 5, guiTop + 116, 56, 20, "gui.add")
				.setHoverText(new TextComponentTranslation("spawner.hover.add").appendSibling(strAlive).getFormattedText()));
		// del
		addButton(new GuiNpcButton(2, guiLeft + 63, guiTop + 116, 56, 20, "gui.remove")
				.setIsEnable(!isDead && slot >= 0)
				.setHoverText(new TextComponentTranslation("spawner.hover.del").appendSibling(strAlive).getFormattedText()));
		// edit mode
		addButton(new GuiNpcButton(3, guiLeft + 121, guiTop + 116, 56, 20, "advanced.editingmode")
				.setIsEnable(!isDead && slot >= 0)
				.setHoverText(new TextComponentTranslation("spawner.hover.change").appendSibling(strAlive).getFormattedText()));
		// up
		addButton(new GuiNpcButton(4, guiLeft + 5, guiTop + 138, 56, 20, "type.up")
				.setIsEnable(!isDead && slot >= 0 && slot >= 1)
				.setHoverText(new TextComponentTranslation("spawner.hover.up").appendSibling(strAlive).getFormattedText()));
		// down
		addButton(new GuiNpcButton(5, guiLeft + 63, guiTop + 138, 56, 20, "type.down")
				.setIsEnable(!isDead && slot >= 0 && slot < (aliveScroll.getList().size() - 1))
				.setHoverText(new TextComponentTranslation("spawner.hover.down").appendSibling(strAlive).getFormattedText()));
		// clear
		addButton(new GuiNpcButton(6, guiLeft + 121, guiTop + 138, 56, 20, "gui.clear")
				.setIsEnable(!aliveScroll.getList().isEmpty())
				.setHoverText(new TextComponentTranslation("spawner.hover.clear").appendSibling(strAlive).getFormattedText()));
		addLabel(new GuiNpcLabel(4, "type.offset", guiLeft + 6, guiTop + 166));
		addLabel(new GuiNpcLabel(5, "X:", guiLeft + 44, guiTop + 166));
		int[] set = job.getOffset(false);
		if (set == null) { set = new int[] { 0, 0, 0 }; }
		if (set.length != 3) {
			int[] ns = new int[] { 0, 0, 0 };
            System.arraycopy(set, 0, ns, 0, set.length);
			set = ns;
		}
		addTextField(new GuiNpcTextField(0, this, guiLeft + 52, guiTop + 161, 35, 15, "" + set[0])
				.setMinMaxDefault(0, 5, set[0])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "X").appendSibling(strAlive).getFormattedText()));
		addLabel(new GuiNpcLabel(6, "Y:", guiLeft + 89, guiTop + 166));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 97, guiTop + 161, 35, 15, "" + set[1])
				.setMinMaxDefault(0, 5, set[1])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Y").appendSibling(strAlive).getFormattedText()));
		addLabel(new GuiNpcLabel(7, "Z:", guiLeft + 134, guiTop + 166));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 142, guiTop + 161, 35, 15, "" + set[2])
				.setMinMaxDefault(0, 5, set[2])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Z").appendSibling(strAlive).getFormattedText()));
		addButton(new GuiNpcCheckBox(7, guiLeft + 5, guiTop + 176, 170, 14, "spawner.despawn", null, job.getDespawnOnTargetLost(false))
				.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost").appendSibling(strAlive).getFormattedText()));
		addLabel(new GuiNpcLabel(8, "spawner.type", guiLeft + 5, guiTop + 195));
		addButton(new GuiNpcButton(8, guiLeft + 63, guiTop + 190, 55, 20, new String[] { "spawner.one", "spawner.all", "spawner.random" }, job.getSpawnType(false))
				.setHoverText(new TextComponentTranslation("spawner.hover.type.0."+job.getSpawnType(false)).appendSibling(strAlive).getFormattedText()));
			// Dead
		ITextComponent strDead = new TextComponentTranslation("spawner.hover.sp.1");
		// add
		addButton(new GuiNpcButton(9, guiLeft + 180, guiTop + 116, 56, 20, "gui.add")
				.setHoverText(new TextComponentTranslation("spawner.hover.add").appendSibling(strDead).getFormattedText()));
		// del
		addButton(new GuiNpcButton(10, guiLeft + 238, guiTop + 116, 56, 20, "gui.remove")
				.setIsEnable(slot >= 0)
				.setHoverText(new TextComponentTranslation("spawner.hover.del").appendSibling(strDead).getFormattedText()));
		// edit mode
		addButton(new GuiNpcButton(11, guiLeft + 296, guiTop + 116, 56, 20, "advanced.editingmode")
				.setIsEnable(isDead && slot >= 0)
				.setHoverText(new TextComponentTranslation("spawner.hover.change").appendSibling(strDead).getFormattedText()));
		// up
		addButton(new GuiNpcButton(12, guiLeft + 180, guiTop + 138, 56, 20, "type.up")
				.setIsEnable(isDead && slot >= 0 && slot >= 1)
				.setHoverText(new TextComponentTranslation("spawner.hover.up").appendSibling(strDead).getFormattedText()));
		// down
		addButton(new GuiNpcButton(13, guiLeft + 238, guiTop + 138, 56, 20, "type.down")
				.setIsEnable(isDead && slot >= 0 && slot < (deadScroll.getList().size() - 1))
				.setHoverText(new TextComponentTranslation("spawner.hover.down").appendSibling(strDead).getFormattedText()));
		// clear
		addButton(new GuiNpcButton(14, guiLeft + 296, guiTop + 138, 56, 20, "gui.clear")
				.setIsEnable(!deadScroll.getList().isEmpty())
				.setHoverText(new TextComponentTranslation("spawner.hover.clear").appendSibling(strDead).getFormattedText()));
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
		addTextField(new GuiNpcTextField(3, this, guiLeft + 227, guiTop + 161, 35, 15, "" + set[0])
				.setMinMaxDefault(0, 5, set[0])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "X").appendSibling(strDead).getFormattedText()));
		addLabel(new GuiNpcLabel(11, "Y:", guiLeft + 264, guiTop + 166));
		addTextField(new GuiNpcTextField(4, this, guiLeft + 272, guiTop + 161, 35, 15, "" + set[1])
				.setMinMaxDefault(0, 5, set[1])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Y").appendSibling(strDead).getFormattedText()));
		addLabel(new GuiNpcLabel(12, "Z:", guiLeft + 309, guiTop + 166));
		addTextField(new GuiNpcTextField(5, this, guiLeft + 317, guiTop + 161, 35, 15, "" + set[2])
				.setMinMaxDefault(0, 5, set[2])
				.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset", "Z").appendSibling(strDead).getFormattedText()));
		addButton(new GuiNpcCheckBox(15, guiLeft + 180, guiTop + 176, 170, 14, "spawner.despawn", null, job.getDespawnOnTargetLost(true))
				.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost").appendSibling(strAlive).getFormattedText()));
		addLabel(new GuiNpcLabel(13, "spawner.type", guiLeft + 180, guiTop + 195));
		addButton(new GuiNpcButton(16, guiLeft + 238, guiTop + 190, 55, 20, new String[] { "spawner.one", "spawner.all", "spawner.random" }, job.getSpawnType(true))
				.setHoverText(new TextComponentTranslation("spawner.hover.type.1."+job.getSpawnType(true)).appendSibling(strAlive).getFormattedText()));
			// Both
		ITextComponent strBoth = new TextComponentTranslation("spawner.hover.sp.2");
		addButton(new GuiNpcCheckBox(17, guiLeft + 357, guiTop + 161, 98, 14, "type.exact", null, job.exact)
				.setHoverText(new TextComponentTranslation("spawner.hover.exact").appendSibling(strBoth).getFormattedText()));
		addButton(new GuiNpcCheckBox(18, guiLeft + 357, guiTop + 176, 98, 14, "script.update", null, job.resetUpdate)
				.setHoverText(new TextComponentTranslation("spawner.hover.reset").appendSibling(strBoth).getFormattedText()));
		// cooldown
		addLabel(new GuiNpcLabel(14, "spawner.cooldown", guiLeft + 358, guiTop + 132)
				.setIsEnable(!aliveScroll.getList().isEmpty()));
		addTextField(new GuiNpcTextField(6, this, guiLeft + 357, guiTop + 144, 55, 15, "" + job.getCooldown() / 50L)
				.setMinMaxDefault(0, 6000, (int) (job.getCooldown() / 50L))
				.setIsVisible(!aliveScroll.getList().isEmpty())
				.setHoverText("spawner.hover.cooldown"));
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (keyCode == 200 ||
				keyCode == 208 ||
				keyCode == mc.gameSettings.keyBindForward.getKeyCode() ||
				keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
			resetEntity();
		}
		return bo;
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
		if (sd.typeClones == 2) { Client.sendData(EnumPacketServer.GetClone, false, sel, sd.compound.getInteger("Tab")); }
		else {
			Entity entity = EntityList.createEntityFromNBT(sd.compound, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				selectNpc = (EntityLivingBase) entity;
			}
		}
	}

	@Override
	public void save() {
		NBTTagCompound compound = job.save(new NBTTagCompound());
		job.removeCompound(compound);
		Client.sendData(EnumPacketServer.JobSave, compound);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		slot = scroll.getSelect();
		isDead = scroll.getID() == 0;
		(isDead ? aliveScroll : deadScroll).setSelect(-1);
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd != null && sd.compound != null) { Client.sendData(EnumPacketServer.GetClone, true, slot, isDead); }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { setSubGui(getSelector()); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) { // Entity set
			selectNpc = (EntityLivingBase) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), player.world);
			return;
		}
		// job data
		if (compound.hasKey("SpawnerWhenAlive", 3)) { job.load(compound); }
		// Setts
		char chr = ((char) 167);
		for (int j = 0; j < 2; j++) {
			boolean type = j == 0;
			List<String> list = new ArrayList<>();
			for (int i = 0; i < job.size(type); i++) {
				SpawnNPCData sd = job.get(i, type);
				list.add(chr + "7" + (i + 1) + ": " + chr + "r" + sd.getTitle() + chr + (sd.typeClones == 0 ? "a (Client)" : sd.typeClones == 1 ? "c (Mob)" : "b (Server)"));
			}
			(type ? deadScroll : aliveScroll).setUnsortedList(list);
		}
		// Data
		if (compound.hasKey("SetPos", 3)) { slot = compound.getInteger("SetPos"); }
		if (compound.hasKey("SetDead", 1)) {
			isDead = compound.getBoolean("SetDead");
			(isDead ? deadScroll : aliveScroll).setSelect(-1);
		}
		SpawnNPCData sd = job.get(slot, isDead);
		if (sd != null && sd.compound != null) { Client.sendData(EnumPacketServer.GetClone, true, slot, isDead); }
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 0: job.getOffset(false)[0] = textField.getInteger(); break; // X alive
			case 1: job.getOffset(false)[1] = textField.getInteger(); break; // Y alive
			case 2: job.getOffset(false)[2] = textField.getInteger(); break; // Z alive
			case 3: job.getOffset(true)[0] = textField.getInteger(); break; // X dead
			case 4: job.getOffset(true)[1] = textField.getInteger(); break; // Y dead
			case 5: job.getOffset(true)[2] = textField.getInteger(); break; // Z dead
			case 6: job.setCooldown(textField.getInteger()); break; // cooldown
		}
	}

}
