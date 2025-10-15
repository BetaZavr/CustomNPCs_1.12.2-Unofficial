package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScenes;

import javax.annotation.Nonnull;

public class GuiNPCScenes extends GuiNPCInterface2 {

	protected final DataScenes scenes;
	protected DataScenes.SceneContainer scene;

	public GuiNPCScenes(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		scenes = npc.advanced.scenes;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() < 60) {
			DataScenes.SceneContainer sceneIn = scenes.scenes.get(button.getID() / 10);
			if (button.getID() % 10 == 1) {
				sceneIn.enabled = button.getValue() == 1;
			}
			if (button.getID() % 10 == 2) {
				scene = sceneIn;
				setSubGui(new SubGuiNpcTextArea(0, sceneIn.lines));
			}
			if (button.getID() % 10 == 3) {
				scenes.scenes.remove(sceneIn);
				initGui();
			}
			if (button.getID() % 10 == 4) {
				scene.btn = button.getValue();
				initGui();
			}
		}
		if (button.getID() == 101) {
			scenes.addScene(getTextField(101).getText());
			initGui();
		}
	}

	@Override
	public void subGuiClosed(GuiScreen gui) {
		if (gui instanceof SubGuiNpcTextArea) {
			scene.lines = ((SubGuiNpcTextArea) gui).text;
			scene = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(102, "gui.button", guiLeft + 236, guiTop + 4));
		int y = guiTop + 14;
		for (int i = 0; i < scenes.scenes.size(); ++i) {
			DataScenes.SceneContainer scene = scenes.scenes.get(i);
			addLabel(new GuiNpcLabel(i * 10, scene.name, guiLeft + 10, y + 5));
			addButton(new GuiNpcButton(1 + i * 10, guiLeft + 120, y, 60, 20, new String[] { "gui.disabled", "gui.enabled" }, (scene.enabled ? 1 : 0)));
			addButton(new GuiNpcButton(2 + i * 10, guiLeft + 181, y, 50, 20, "selectServer.edit"));
			addButton(new GuiNpcButton(3 + i * 10, guiLeft + 293, y, 50, 20, "X"));
			if (CustomNpcs.SceneButtonsEnabled) {
				addButton(new GuiNpcButton(4 + i * 10, guiLeft + 232, y, 60, 20,
						new String[] { "gui.none", GameSettings.getKeyDisplayString(ClientProxy.Scene1.getKeyCode()),
								GameSettings.getKeyDisplayString(ClientProxy.Scene2.getKeyCode()),
								GameSettings.getKeyDisplayString(ClientProxy.Scene3.getKeyCode()) },
						scene.btn));
			}
			y += 22;
		}
		if (scenes.scenes.size() < 6) {
			addTextField(new GuiNpcTextField(101, this, guiLeft + 4, y + 10, 190, 20, "Scene" + (scenes.scenes.size() + 1)));
			addButton(new GuiNpcButton(101, guiLeft + 204, y + 10, 60, 20, "gui.add"));
		}
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.save(new NBTTagCompound())); }

}
