package noppes.npcs.client.gui;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCLinesEdit extends GuiNPCInterface2 implements IGuiData, ISubGuiListener {
	private Lines lines;
	private int selectedId;

	public GuiNPCLinesEdit(EntityNPCInterface npc, Lines lines) {
		super(npc);
		this.selectedId = -1;
		this.lines = lines;
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet, new Object[0]);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		this.selectedId = button.id + 8;
		this.setSubGui(new GuiSoundSelection(this.getTextField(this.selectedId).getText()));
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 8; ++i) {
			String text = "";
			String sound = "";
			if (this.lines.lines.containsKey(i)) {
				Line line = this.lines.lines.get(i);
				text = line.getText();
				sound = line.getSound();
			}
			this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer, this.guiLeft + 4,
					this.guiTop + 4 + i * 24, 200, 20, text));
			this.addTextField(new GuiNpcTextField(i + 8, this, this.fontRenderer, this.guiLeft + 208,
					this.guiTop + 4 + i * 24, 146, 20, sound));
			this.addButton(new GuiNpcButton(i, this.guiLeft + 358, this.guiTop + 4 + i * 24, 60, 20,
					"mco.template.button.select"));
		}
	}

	@Override
	public void save() {
		this.saveLines();
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}

	private void saveLines() {
		HashMap<Integer, Line> lines = new HashMap<Integer, Line>();
		for (int i = 0; i < 8; ++i) {
			GuiNpcTextField tf = this.getTextField(i);
			GuiNpcTextField tf2 = this.getTextField(i + 8);
			if (!tf.isEmpty() || !tf2.isEmpty()) {
				Line line = new Line();
				line.setText(tf.getText());
				line.setSound(tf2.getText());
				lines.put(i, line);
			}
		}
		this.lines.lines = lines;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.npc.advanced.readToNBT(compound);
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			this.getTextField(this.selectedId).setText(gss.selectedResource.toString());
			this.saveLines();
			this.initGui();
		}
	}
}
