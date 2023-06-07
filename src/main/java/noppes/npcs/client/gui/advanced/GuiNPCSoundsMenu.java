package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
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
	public void buttonEvent(GuiButton button) {
		if (button.id == 6) {
			this.npc.advanced.disablePitch = (((GuiNpcButton) button).getValue() == 0);
		} else {
			this.selectedField = this.getTextField(button.id);
			this.setSubGui(new GuiSoundSelection(this.selectedField.getText()));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "advanced.idlesound", this.guiLeft + 5, this.guiTop + 20));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 15, 200, 20, this.npc.advanced.getSound(0)));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 290, this.guiTop + 15, 80, 20, "gui.selectSound"));
		this.addLabel(new GuiNpcLabel(2, "advanced.angersound", this.guiLeft + 5, this.guiTop + 45));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 40, 200, 20, this.npc.advanced.getSound(1)));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 290, this.guiTop + 40, 80, 20, "gui.selectSound"));
		this.addLabel(new GuiNpcLabel(3, "advanced.hurtsound", this.guiLeft + 5, this.guiTop + 70));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 65, 200, 20, this.npc.advanced.getSound(2)));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 290, this.guiTop + 65, 80, 20, "gui.selectSound"));
		this.addLabel(new GuiNpcLabel(4, "advanced.deathsound", this.guiLeft + 5, this.guiTop + 95));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 90, 200, 20, this.npc.advanced.getSound(3)));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 290, this.guiTop + 90, 80, 20, "gui.selectSound"));
		this.addLabel(new GuiNpcLabel(5, "advanced.stepsound", this.guiLeft + 5, this.guiTop + 120));
		this.addTextField(new GuiNpcTextField(5, this, this.fontRenderer, this.guiLeft + 80, this.guiTop + 115, 200, 20, this.npc.advanced.getSound(4)));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 290, this.guiTop + 115, 80, 20, "gui.selectSound"));
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
		if (textfield.getId() == 0) {
			this.npc.advanced.setSound(0, textfield.getText());
		}
		if (textfield.getId() == 2) {
			this.npc.advanced.setSound(1, textfield.getText());
		}
		if (textfield.getId() == 3) {
			this.npc.advanced.setSound(2, textfield.getText());
		}
		if (textfield.getId() == 4) {
			this.npc.advanced.setSound(3, textfield.getText());
		}
		if (textfield.getId() == 5) {
			this.npc.advanced.setSound(4, textfield.getText());
		}
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
