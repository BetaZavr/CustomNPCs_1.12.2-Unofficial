package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCSoundsMenu extends GuiNPCInterface2 implements ITextfieldListener {

	protected GuiNpcTextField selectedField;

	public GuiNPCSoundsMenu(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() == 6) { npc.advanced.disablePitch = button.getValue() == 0; }
		else if (button.getID() < 10) { setSubGui(new SubGuiSoundSelection((selectedField = getTextField(button.getID())).getText())); }
		else {
			selectedField = getTextField(button.getID() - 10);
			selectedField.setText("");
			unFocused(selectedField);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 5; i++) {
			String name;
			switch (i) {
				case 1: name = "advanced.angersound"; break;
				case 2: name = "advanced.hurtsound"; break;
				case 3: name = "advanced.deathsound"; break;
				case 4: name = "advanced.stepsound"; break;
				default: name = "advanced.idlesound"; break;
			}
			addLabel(new GuiNpcLabel(i, name, guiLeft + 5, guiTop + 20 + i * 25));
			addTextField(new GuiNpcTextField(i, this, guiLeft + 80, guiTop + 15 + i * 25, 200, 20, npc.advanced.getSound(i)));
			addButton(new GuiNpcButton(i, guiLeft + 310, guiTop + 15 + i * 25, 80, 20, "gui.selectSound"));
			addButton(new GuiNpcButton(10 + i, guiLeft + 285, guiTop + 15 + i * 25, 20, 20, "X"));
		}
		addLabel(new GuiNpcLabel(6, "advanced.haspitch", guiLeft + 5, guiTop + 150));
		addButton(new GuiNpcButton(6, guiLeft + 120, guiTop + 145, 80, 20, new String[] { "gui.no", "gui.yes" }, (npc.advanced.disablePitch ? 0 : 1)));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.save(new NBTTagCompound())); }

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		SubGuiSoundSelection gss = (SubGuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			selectedField.setText(gss.selectedResource.toString());
			unFocused(selectedField);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		npc.advanced.setSound(textfield.getID(), textfield.getText());
		initGui();
	}

}
