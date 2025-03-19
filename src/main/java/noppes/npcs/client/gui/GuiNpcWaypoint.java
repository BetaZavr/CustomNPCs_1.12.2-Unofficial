package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileWaypoint;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import java.awt.*;

public class GuiNpcWaypoint
extends GuiNPCInterface
implements IGuiData {

	private final TileWaypoint tile;

	public GuiNpcWaypoint(int x, int y, int z) {
		super();
		xSize = 265;

		tile = (TileWaypoint) player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (tile == null) {
			close();
			return;
		}
		int color = new Color(0xFFFFFF).getRGB();
		// name
		addLabel(new GuiNpcLabel(0, "gui.name", guiLeft + 1, guiTop + 76, color));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 60, guiTop + 71, 200, 20, tile.name);
		addTextField(textField);
		// range
		addLabel(new GuiNpcLabel(1, "gui.range", guiLeft + 1, guiTop + 97, color));
		textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 60, guiTop + 92, 200, 20, tile.range + "");
		textField.setMinMaxDefault(2, 60, 10);
		addTextField(textField);
		// exit
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 40, guiTop + 190, 120, 20, "gui.done");
		button.setHoverText("hover.exit");
		addButton(button);
	}

	@Override
	public void save() {
		tile.name = getTextField(0).getText();
		tile.range = getTextField(1).getInteger();
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		tile.readFromNBT(compound);
		initGui();
	}
}
