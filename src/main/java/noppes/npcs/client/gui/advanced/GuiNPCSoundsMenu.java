package noppes.npcs.client.gui.advanced;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCSoundsMenu
extends GuiNPCInterface2
implements ITextfieldListener, ISubGuiListener {
	
	public GuiSoundSelection gui;
	private GuiNpcTextField selectedField;

	public GuiNPCSoundsMenu(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 6) {
			this.npc.advanced.disablePitch = button.getValue() == 0;
		}
		else if (button.id < 10) {
			this.selectedField = this.getTextField(button.id);
			this.setSubGui(new GuiSoundSelection(this.selectedField.getText()));
		} else {
			this.selectedField = this.getTextField(button.id - 10);
			this.selectedField.setText("");
			this.unFocused(this.selectedField);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i<5; i++) {
			String name;
			switch(i) {
				case 1: name = "advanced.angersound"; break;
				case 2: name = "advanced.hurtsound"; break;
				case 3: name = "advanced.deathsound"; break;
				case 4: name = "advanced.stepsound"; break;
				default: name = "advanced.idlesound"; break;
			}
			this.addLabel(new GuiNpcLabel(i, name, this.guiLeft + 5, this.guiTop + 20 + i * 25));
			this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 15 + i * 25, 200, 20, this.npc.advanced.getSound(i)));
			this.addButton(new GuiNpcButton(i, this.guiLeft + 310, this.guiTop + 15 + i * 25, 80, 20, "gui.selectSound"));
			this.addButton(new GuiNpcButton(10 + i, this.guiLeft + 285, this.guiTop + 15 + i * 25, 20, 20, "X"));
		}
		this.addLabel(new GuiNpcLabel(6, "advanced.haspitch", this.guiLeft + 5, this.guiTop + 150));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 120, this.guiTop + 145, 80, 20, new String[] { "gui.no", "gui.yes" }, (this.npc.advanced.disablePitch ? 0 : 1)));
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			this.selectedField.setText(gss.selectedResource.toString());
			this.unFocused(this.selectedField);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		this.npc.advanced.setSound(textfield.getId(), textfield.getText());
		this.initGui();
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}
}
