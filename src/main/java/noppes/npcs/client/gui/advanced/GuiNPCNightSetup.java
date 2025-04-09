package noppes.npcs.client.gui.advanced;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.DataTransform;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCNightSetup
extends GuiNPCInterface2
implements IGuiData {

	private final DataTransform data;

	public GuiNPCNightSetup(EntityNPCInterface npc) {
		super(npc);
		data = npc.transform;
		Client.sendData(EnumPacketServer.TransformGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0:
				data.hasDisplay = button.getValue() == 1;
				break;
			case 1:
				data.hasStats = button.getValue() == 1;
				break;
			case 2:
				data.hasAi = button.getValue() == 1;
				break;
			case 3:
				data.hasInv = button.getValue() == 1;
				break;
			case 4:
				data.hasAdvanced = button.getValue() == 1;
				break;
			case 5:
				data.hasRole = button.getValue() == 1;
				break;
			case 6:
				data.hasJob = button.getValue() == 1;
				break;
			case 10: {
				data.editingModus = button.getValue() == 1;
				save();
				initGui();
				break;
			}
			case 11:
				Client.sendData(EnumPacketServer.TransformLoad, false);
				break;
			case 12:
				Client.sendData(EnumPacketServer.TransformLoad, true);
				break;
		}
	}

	@Override
	public void close() {
		save();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void initGui() {
		super.initGui();
		int xL = guiLeft + 4;
		int xB = guiLeft + 74;
		int y = guiTop + 5;
		GuiNpcButton button;
		addLabel(new GuiNpcLabel(10, "advanced.editingmode", xL, y + 5));
		addButton(button = new GuiNpcButton(10, xB, y, 80, 20, new String[] { "gui.no", "gui.yes" }, (data.editingModus ? 1 : 0)));
		button.setHoverText("transform.hover.edit");

		addLabel(new GuiNpcLabel(0, "menu.display", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(0, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasDisplay ? 1 : 0)));
		button.setHoverText("transform.hover.tab", new TextComponentTranslation("menu.display").getFormattedText());

		addLabel(new GuiNpcLabel(1, "menu.stats", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(1, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasStats ? 1 : 0)));
		button.setHoverText("transform.hover.tab", new TextComponentTranslation("menu.stats").getFormattedText());

		if (data.editingModus) {
			addButton(button = new GuiNpcButton(11, guiLeft + 170, y, 120, 20, "advanced.loadday"));
			button.setHoverText(new TextComponentTranslation("transform.hover.loadday")
					.appendSibling(new TextComponentTranslation("transform.hover.state")).getFormattedText());
		}

		addLabel(new GuiNpcLabel(2, "menu.ai", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(2, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasAi ? 1 : 0)));
		button.setHoverText("transform.hover.tab", new TextComponentTranslation("menu.ai").getFormattedText());

		if (data.editingModus) {
			addButton(button = new GuiNpcButton(12, guiLeft + 170, y, 120, 20, "advanced.loadnight"));
			button.setHoverText(new TextComponentTranslation("transform.hover.loadnight")
					.appendSibling(new TextComponentTranslation("transform.hover.state")).getFormattedText());
		}

		addLabel(new GuiNpcLabel(3, "menu.inventory", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(3, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasInv ? 1 : 0)));
		button.setHoverText("transform.hover.tab", new TextComponentTranslation("menu.inventory").getFormattedText());

		addLabel(new GuiNpcLabel(4, "menu.advanced", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(4, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasAdvanced ? 1 : 0)));
		button.setHoverText("transform.hover.tab", new TextComponentTranslation("menu.advanced").getFormattedText());

		addLabel(new GuiNpcLabel(5, "role.name", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(5, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasRole ? 1 : 0)));
		button.setHoverText("transform.hover.role", new TextComponentTranslation("menu.advanced").getFormattedText());

		addLabel(new GuiNpcLabel(6, "job.name", xL, (y += 22) + 5));
		addButton(button = new GuiNpcButton(6, xB, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (data.hasJob ? 1 : 0)));
		button.setHoverText("transform.hover.job", new TextComponentTranslation("menu.advanced").getFormattedText());
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.TransformSave, data.writeOptions(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		data.readOptions(compound);
		initGui();
	}
}
