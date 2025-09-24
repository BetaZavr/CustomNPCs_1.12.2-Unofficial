package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileWaypoint;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiNpcWaypoint extends GuiNPCInterface implements IGuiData {

	protected final TileWaypoint tile;

	public GuiNpcWaypoint(int x, int y, int z) {
		super();
		xSize = 265;

		tile = (TileWaypoint) player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() == 66) { onClosed(); }
	}

	@Override
	public void initGui() {
		super.initGui();
		if (tile == null) { onClosed(); return; }
		int color = new Color(0xFFFFFF).getRGB();
		// name
		addLabel(new GuiNpcLabel(0, "gui.name", guiLeft + 1, guiTop + 76, color));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 60, guiTop + 71, 200, 20, tile.name));
		// range
		addLabel(new GuiNpcLabel(1, "gui.range", guiLeft + 1, guiTop + 97, color));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 60, guiTop + 92, 200, 20, tile.range + "")
				.setMinMaxDefault(2, 60, 10));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 40, guiTop + 190, 120, 20, "gui.done")
				.setHoverText("hover.exit"));
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
