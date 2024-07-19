package noppes.npcs.client.gui.roles;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
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

public class GuiNpcSpawner extends GuiNPCInterface2 implements IGuiData, ICustomScrollListener, ITextfieldListener {

	private final JobSpawner job;
	private int slot;
	private GuiCustomScroll deadScroll, aliveScroll;
	public EntityLivingBase selectNpc;
	private boolean isDead = false;

	public GuiNpcSpawner(EntityNPCInterface npc) {
		super(npc);
		this.slot = -1;
		this.job = (JobSpawner) npc.advanced.jobInterface;
		Client.sendData(EnumPacketServer.JobGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 1: { // add alive
			this.isDead = false;
			this.slot = -1;
			this.setSubGui(this.getSelector());
			break;
		}
		case 2: { // del alive
			if (this.isDead) {
				return;
			}
			Client.sendData(EnumPacketServer.JobSpawnerRemove, this.slot, this.isDead);
			this.initGui();
			break;
		}
		case 3: { // change alive
			if (this.isDead) {
				return;
			}
			this.setSubGui(this.getSelector());
			break;
		}
		case 4: { // up alive
			if (this.isDead || this.slot < 1) {
				return;
			}
			Client.sendData(EnumPacketServer.SpawnerNpcMove, this.slot, true, this.isDead);
			this.slot--;
			this.initGui();
			break;
		}
		case 5: { // down alive
			if (this.isDead || this.slot >= this.job.size(this.isDead)) {
				return;
			}
			Client.sendData(EnumPacketServer.SpawnerNpcMove, this.slot, false, this.isDead);
			this.slot++;
			this.initGui();
			break;
		}
		case 6: { // clear alive
			Client.sendData(EnumPacketServer.JobClear, false);
			break;
		}
		case 7: { // targetLost alive
			this.job.setDespawnOnTargetLost(false, ((GuiNpcCheckBox) button).isSelected());
			break;
		}
		case 8: { // type alive
			this.job.setSpawnType(false, button.getValue());
			break;
		}
		case 9: { // add Dead
			this.isDead = true;
			this.slot = -1;
			this.setSubGui(this.getSelector());
			break;
		}
		case 10: { // del Dead
			if (!this.isDead) {
				return;
			}
			Client.sendData(EnumPacketServer.JobSpawnerRemove, this.slot, this.isDead);
			this.initGui();
			break;
		}
		case 11: { // change Dead
			if (!this.isDead) {
				return;
			}
			this.setSubGui(this.getSelector());
			break;
		}
		case 12: { // up Dead
			if (!this.isDead || this.slot < 1) {
				return;
			}
			Client.sendData(EnumPacketServer.SpawnerNpcMove, this.slot, true, this.isDead);
			this.slot--;
			this.initGui();
			break;
		}
		case 13: { // down Dead
			if (!this.isDead || this.slot >= this.job.size(this.isDead)) {
				return;
			}
			Client.sendData(EnumPacketServer.SpawnerNpcMove, this.slot, false, this.isDead);
			this.slot++;
			this.initGui();
			break;
		}
		case 14: { // clear Dead
			Client.sendData(EnumPacketServer.JobClear, true);
			break;
		}
		case 15: { // targetLost Dead
			this.job.setDespawnOnTargetLost(true, ((GuiNpcCheckBox) button).isSelected());
			break;
		}
		case 16: { // type Dead
			this.job.setSpawnType(true, button.getValue());
			break;
		}
		case 17: { // exact
			this.job.exact = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 18: { // resetUpdate
			this.job.resetUpdate = ((GuiNpcCheckBox) button).isSelected();
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
		if (this.slot < 0) {
			this.slot = (this.isDead ? this.deadScroll : this.aliveScroll).getList().size();
		}
		if (selector.showingClones == 2) {
			String selected = selector.getSelected();
			if (selected != null) {
				Client.sendData(EnumPacketServer.JobSpawnerAdd, true, this.isDead, this.slot, selected,
						selector.activeTab);
				Client.sendData(EnumPacketServer.GetClone, true, this.slot, this.isDead);
			}
		} else {
			NBTTagCompound nbtNpc = selector.getCompound();
			if (nbtNpc == null) {
				return;
			}
			SpawnNPCData sd = selector.spawnData;
			if (this.slot < 0) {
				this.slot = this.job.size(this.isDead);
			}
			sd.compound = nbtNpc;
			if (sd.typeClones == 0) {
				sd.compound.setInteger("ClonedTab", selector.activeTab);
			}
			this.job.readJobCompound(this.slot, this.isDead, sd.writeToNBT());
			Client.sendData(EnumPacketServer.JobSpawnerAdd, false, this.isDead, this.slot, sd.writeToNBT());
			Client.sendData(EnumPacketServer.GetClone, true, this.slot, this.isDead);
		}
		this.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.subgui == null) {
			GlStateManager.pushMatrix();
			if (this.selectNpc != null) {
				this.drawNpc(this.selectNpc, 385, 92, 1.0f, (int) (3 * this.player.world.getTotalWorldTime() % 360), 0,
						0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			this.drawVerticalLine(this.guiLeft + 178, this.guiTop + 4, this.guiTop + this.ySize + 12, 0xFF404040);
			this.drawVerticalLine(this.guiLeft + 353, this.guiTop + 4, this.guiTop + this.ySize + 12, 0xFF404040);
			Gui.drawRect(this.guiLeft + 355, this.guiTop + 13, this.guiLeft + 416, this.guiTop + 99, 0xFF808080);
			Gui.drawRect(this.guiLeft + 356, this.guiTop + 14, this.guiLeft + 415, this.guiTop + 98, 0xFF000000);
			GlStateManager.popMatrix();
			super.drawScreen(mouseX, mouseY, partialTicks);
		} else {
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
		}
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.add")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.del")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.change")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.up")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.down")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.clear")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.type.0." + this.getButton(8).getValue())
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.0")).getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.add")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.del")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.change")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.up")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.down")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.clear")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.des.tr.lost")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(16) != null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.type.1." + this.getButton(8).getValue())
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.1")).getFormattedText());
		} else if (this.getButton(17) != null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.exact")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.2")).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.reset")
					.appendSibling(new TextComponentTranslation("spawner.hover.sp.2")).getFormattedText());
		} else if (this.getLabel(1) != null && this.getLabel(1).hovered) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.list.0")
					.appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText());
		} else if (this.getLabel(2) != null && this.getLabel(2).hovered) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.list.1")
					.appendSibling(new TextComponentTranslation("spawner.hover.list.2")).getFormattedText());
		}
		for (int t = 0; t < 6; t++) {
			if (mouseX >= this.guiLeft + (t < 3 ? 52 : 227) + (t % 3 * 45) && mouseY >= this.guiTop + 161
					&& mouseX < this.guiLeft + (t < 3 ? 87 : 262) + (t % 3 * 45) && mouseY < this.guiTop + 176) {
				this.setHoverText(new TextComponentTranslation("spawner.hover.axis.offset",
						(t % 3 == 0 ? "X" : t % 3 == 1 ? "Y" : "Z"))
								.appendSibling(
										new TextComponentTranslation("spawner.hover.sp." + ((int) Math.floor(t / 3.0d))))
								.getFormattedText());
				return;
			}
		}
		if (this.getTextField(6) != null && this.getTextField(6).getVisible() && mouseX >= this.guiLeft + 357
				&& mouseY >= this.guiTop + 144 && mouseX < this.guiLeft + 412 && mouseY < this.guiTop + 159) {
			this.setHoverText(new TextComponentTranslation("spawner.hover.cooldown").getFormattedText());
			return;
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void elementClicked() {
	}

	private GuiNpcMobSpawnerSelector getSelector() {
		GuiNpcMobSpawnerSelector guiMSS = new GuiNpcMobSpawnerSelector();
		SpawnNPCData sd = this.job.get(this.slot, this.isDead);
		if (sd == null) {
			sd = new SpawnNPCData();
		}
		guiMSS.spawnData = sd;
		guiMSS.showingClones = sd.typeClones;
		if ((sd.typeClones == 0 || sd.typeClones == 2) && sd.compound != null && sd.compound.hasKey("ClonedTab", 3)) {
			guiMSS.activeTab = sd.compound.getInteger("ClonedTab");
		}
		return guiMSS;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.aliveScroll == null) {
			(this.aliveScroll = new GuiCustomScroll(this, 1)).setSize(172, 101);
		}
		this.aliveScroll.guiLeft = this.guiLeft + 5;
		this.aliveScroll.guiTop = this.guiTop + 14;
		if (!this.isDead) {
			if (this.slot >= 0 && this.slot < this.aliveScroll.getList().size()) {
				this.aliveScroll.selected = this.slot;
			} else {
				this.slot = -1;
				this.aliveScroll.selected = -1;
				this.selectNpc = null;
			}
		} else {
			this.aliveScroll.selected = -1;
		}
		this.addScroll(this.aliveScroll);

		if (this.deadScroll == null) {
			(this.deadScroll = new GuiCustomScroll(this, 0)).setSize(172, 101);
		}
		this.deadScroll.guiLeft = this.guiLeft + 180;
		this.deadScroll.guiTop = this.guiTop + 14;
		if (this.isDead) {
			if (this.slot >= 0 && this.slot < this.deadScroll.getList().size()) {
				this.deadScroll.selected = this.slot;
			} else {
				this.slot = -1;
				this.deadScroll.selected = -1;
				this.selectNpc = null;
			}
		} else {
			this.deadScroll.selected = -1;
		}
		this.addScroll(this.deadScroll);

		this.addLabel(new GuiNpcLabel(1, "spawner.list.0", this.guiLeft + 6, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(2, "spawner.list.1", this.guiLeft + 182, this.guiTop + 4));

		this.addButton(new GuiNpcButton(1, this.guiLeft + 5, this.guiTop + 116, 56, 20, "gui.add"));
		GuiNpcButton button = new GuiNpcButton(2, this.guiLeft + 63, this.guiTop + 116, 56, 20, "gui.remove");
		button.enabled = !this.isDead && this.slot >= 0;
		this.addButton(button);
		button = new GuiNpcButton(3, this.guiLeft + 121, this.guiTop + 116, 56, 20, "advanced.editingmode");
		button.enabled = !this.isDead && this.slot >= 0;
		this.addButton(button);
		button = new GuiNpcButton(4, this.guiLeft + 5, this.guiTop + 138, 56, 20, "type.up");
		button.enabled = !this.isDead && this.slot >= 0 && this.slot >= 1;
		this.addButton(button);
		button = new GuiNpcButton(5, this.guiLeft + 63, this.guiTop + 138, 56, 20, "type.down");
		button.enabled = !this.isDead && this.slot >= 0 && this.slot < (this.aliveScroll.getList().size() - 1);
		this.addButton(button);
		button = new GuiNpcButton(6, this.guiLeft + 121, this.guiTop + 138, 56, 20, "gui.clear");
		button.enabled = !this.aliveScroll.getList().isEmpty();
		this.addButton(button);

		this.addLabel(new GuiNpcLabel(4, "type.offset", this.guiLeft + 6, this.guiTop + 166));
		this.addLabel(new GuiNpcLabel(5, "X:", this.guiLeft + 44, this.guiTop + 166));
		int[] set = this.job.getOffset(false);
		if (set == null) {
			set = new int[] { 0, 0, 0 };
		}
		if (set.length != 3) {
			int[] ns = new int[] { 0, 0, 0 };
            System.arraycopy(set, 0, ns, 0, set.length);
			set = ns;
		}
		GuiNpcTextField tf = new GuiNpcTextField(0, this, this.guiLeft + 52, this.guiTop + 161, 35, 15, "" + set[0]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[0]);
		this.addTextField(tf);

		this.addLabel(new GuiNpcLabel(6, "Y:", this.guiLeft + 89, this.guiTop + 166));
		tf = new GuiNpcTextField(1, this, this.guiLeft + 97, this.guiTop + 161, 35, 15, "" + set[1]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[1]);
		this.addTextField(tf);

		this.addLabel(new GuiNpcLabel(7, "Z:", this.guiLeft + 134, this.guiTop + 166));
		tf = new GuiNpcTextField(2, this, this.guiLeft + 142, this.guiTop + 161, 35, 15, "" + set[2]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[2]);
		this.addTextField(tf);

		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(7, this.guiLeft + 5, this.guiTop + 176, 170, 14,
				"spawner.despawn");
		checkBox.setSelected(this.job.getDespawnOnTargetLost(false));
		this.addButton(checkBox);

		this.addLabel(new GuiNpcLabel(8, "spawner.type", this.guiLeft + 5, this.guiTop + 195));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 63, this.guiTop + 190, 55, 20,
				new String[] { "spawner.one", "spawner.all", "spawner.random" }, this.job.getSpawnType(false)));

		// Dead
		this.addButton(new GuiNpcButton(9, this.guiLeft + 180, this.guiTop + 116, 56, 20, "gui.add"));
		button = new GuiNpcButton(10, this.guiLeft + 238, this.guiTop + 116, 56, 20, "gui.remove");
		button.enabled = this.slot >= 0;
		this.addButton(button);
		button = new GuiNpcButton(11, this.guiLeft + 296, this.guiTop + 116, 56, 20, "advanced.editingmode");
		button.enabled = this.isDead && this.slot >= 0;
		this.addButton(button);
		button = new GuiNpcButton(12, this.guiLeft + 180, this.guiTop + 138, 56, 20, "type.up");
		button.enabled = this.isDead && this.slot >= 0 && this.slot >= 1;
		this.addButton(button);
		button = new GuiNpcButton(13, this.guiLeft + 238, this.guiTop + 138, 56, 20, "type.down");
		button.enabled = this.isDead && this.slot >= 0 && this.slot < (this.deadScroll.getList().size() - 1);
		this.addButton(button);
		button = new GuiNpcButton(14, this.guiLeft + 296, this.guiTop + 138, 56, 20, "gui.clear");
		button.enabled = !this.deadScroll.getList().isEmpty();
		this.addButton(button);

		this.addLabel(new GuiNpcLabel(9, "type.offset", this.guiLeft + 181, this.guiTop + 166));
		this.addLabel(new GuiNpcLabel(10, "X:", this.guiLeft + 219, this.guiTop + 166));
		set = this.job.getOffset(true);
		if (set == null) {
			set = new int[] { 0, 0, 0 };
		}
		if (set.length != 3) {
			int[] ns = new int[] { 0, 0, 0 };
            System.arraycopy(set, 0, ns, 0, set.length);
			set = ns;
		}
		tf = new GuiNpcTextField(3, this, this.guiLeft + 227, this.guiTop + 161, 35, 15, "" + set[0]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[0]);
		this.addTextField(tf);

		this.addLabel(new GuiNpcLabel(11, "Y:", this.guiLeft + 264, this.guiTop + 166));
		tf = new GuiNpcTextField(4, this, this.guiLeft + 272, this.guiTop + 161, 35, 15, "" + set[1]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[1]);
		this.addTextField(tf);

		this.addLabel(new GuiNpcLabel(12, "Z:", this.guiLeft + 309, this.guiTop + 166));
		tf = new GuiNpcTextField(5, this, this.guiLeft + 317, this.guiTop + 161, 35, 15, "" + set[2]);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 5, set[2]);
		this.addTextField(tf);

		checkBox = new GuiNpcCheckBox(15, this.guiLeft + 180, this.guiTop + 176, 170, 14, "spawner.despawn");
		checkBox.setSelected(this.job.getDespawnOnTargetLost(true));
		this.addButton(checkBox);

		this.addLabel(new GuiNpcLabel(13, "spawner.type", this.guiLeft + 180, this.guiTop + 195));
		this.addButton(new GuiNpcButton(16, this.guiLeft + 238, this.guiTop + 190, 55, 20,
				new String[] { "spawner.one", "spawner.all", "spawner.random" }, this.job.getSpawnType(true)));

		checkBox = new GuiNpcCheckBox(17, this.guiLeft + 357, this.guiTop + 161, 98, 14, "type.exact");
		checkBox.setSelected(this.job.exact);
		this.addButton(checkBox);

		checkBox = new GuiNpcCheckBox(18, this.guiLeft + 357, this.guiTop + 176, 98, 14, "script.update");
		checkBox.setSelected(this.job.resetUpdate);
		this.addButton(checkBox);

		GuiNpcLabel label = new GuiNpcLabel(14, "spawner.cooldown", this.guiLeft + 358, this.guiTop + 132);
		label.enabled = !this.aliveScroll.getList().isEmpty();
		this.addLabel(label);
		tf = new GuiNpcTextField(6, this, this.guiLeft + 357, this.guiTop + 144, 55, 15, "" + this.job.getCooldown() / 50L);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 6000, (int) (this.job.getCooldown() / 50L));
		tf.setVisible(!this.aliveScroll.getList().isEmpty());
		this.addTextField(tf);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode()
				|| i == ClientProxy.backButton.getKeyCode()) {
			this.resetEntity();
		}
	}

	private void resetEntity() {
		String sel = (this.isDead ? this.deadScroll : this.aliveScroll).getSelected();
		if (sel == null) {
			this.selectNpc = null;
			return;
		}
		SpawnNPCData sd = this.job.get(this.slot, isDead);
		if (sd == null) {
			this.selectNpc = null;
			return;
		}
		if (sd.typeClones == 2) {
			Client.sendData(EnumPacketServer.GetClone, false, sel, sd.compound.getInteger("Tab"));
		} else {
			Entity entity = EntityList.createEntityFromNBT(sd.compound, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				this.selectNpc = (EntityLivingBase) entity;
			}
		}
	}

	@Override
	public void save() {
		NBTTagCompound compound = this.job.writeToNBT(new NBTTagCompound());
		this.job.removeCompound(compound);
		Client.sendData(EnumPacketServer.JobSave, compound);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.slot = scroll.selected;
		this.isDead = scroll.id == 0;
		(this.isDead ? this.aliveScroll : this.deadScroll).selected = -1;
		SpawnNPCData sd = this.job.get(this.slot, this.isDead);
		if (sd != null && sd.compound != null) {
			Client.sendData(EnumPacketServer.GetClone, true, this.slot, this.isDead);
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.setSubGui(this.getSelector());
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) { // Entity set
			this.selectNpc = (EntityLivingBase) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"),
					this.player.world);
			return;
		}
		// Setts
		char chr = ((char) 167);
		this.job.readFromNBT(compound);
		for (int j = 0; j < 2; j++) {
			boolean type = j == 0;
			List<String> list = Lists.newArrayList();
			for (int i = 0; i < this.job.size(type); i++) {
				SpawnNPCData sd = this.job.get(i, type);
				list.add(chr + "7" + (i + 1) + ": " + chr + "r" + sd.getTitle() + chr
						+ (sd.typeClones == 0 ? "a (Client)" : sd.typeClones == 1 ? "c (Mob)" : "b (Server)"));
			}
			(type ? this.deadScroll : this.aliveScroll).setListNotSorted(list);
		}
		// Data
		if (compound.hasKey("SetPos", 3)) {
			this.slot = compound.getInteger("SetPos");
		}
		if (compound.hasKey("SetDead", 1)) {
			this.isDead = compound.getBoolean("SetDead");
			(this.isDead ? this.deadScroll : this.aliveScroll).selected = -1;
		}
		SpawnNPCData sd = this.job.get(this.slot, this.isDead);
		if (sd != null && sd.compound != null) {
			Client.sendData(EnumPacketServer.GetClone, true, this.slot, this.isDead);
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
		case 0: { // X alive
			this.job.getOffset(false)[0] = textField.getInteger();
			break;
		}
		case 1: { // Y alive
			this.job.getOffset(false)[1] = textField.getInteger();
			break;
		}
		case 2: { // Z alive
			this.job.getOffset(false)[2] = textField.getInteger();
			break;
		}
		case 3: { // X dead
			this.job.getOffset(true)[0] = textField.getInteger();
			break;
		}
		case 4: { // Y dead
			this.job.getOffset(true)[1] = textField.getInteger();
			break;
		}
		case 5: { // Z dead
			this.job.getOffset(true)[2] = textField.getInteger();
			break;
		}
		case 6: { // cooldown
			this.job.setCooldown(textField.getInteger());
			break;
		}
		default: {

		}
		}
	}
}
