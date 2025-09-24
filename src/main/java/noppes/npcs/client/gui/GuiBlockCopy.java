package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import javax.annotation.Nonnull;

public class GuiBlockCopy extends GuiNPCInterface implements IGuiData, ITextfieldListener {

	protected final TileCopy tile;
	protected int x;
	protected int y;
	protected int z;

	public GuiBlockCopy(int xPos, int yPos, int zPos) {
		super();
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 216;

		x = xPos;
		y = yPos;
		z = zPos;
		tile = (TileCopy) player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				NBTTagCompound compound = new NBTTagCompound();
				tile.writeToNBT(compound);
				Client.sendData(EnumPacketServer.SchematicStore, getTextField(5).getText(), getButton(6).getValue(), compound);
				onClosed();
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int xL = guiLeft + 5;
		int xTF = guiLeft + 104;
		int y = guiTop + 4;
		addLabel(new GuiNpcLabel(0, "schematic.height", xL, y + 5));
		addTextField(new GuiNpcTextField(0, this, xTF, y, 50, 20, tile.height + "")
				.setMinMaxDefault(0, 1000, 10));
		y += 23;
		addLabel(new GuiNpcLabel(1, "schematic.width", xL, y + 5));
		addTextField(new GuiNpcTextField(1, this, xTF, y, 50, 20, tile.width + "")
				.setMinMaxDefault(0, 1000, 10));
		y += 23;
		addLabel(new GuiNpcLabel(2, "schematic.length", xL, y + 5));
		addTextField(new GuiNpcTextField(2, this, xTF, y, 50, 20, tile.length + "")
				.setMinMaxDefault(0, 1000, 10));
		y += 23;
		addLabel(new GuiNpcLabel(5, "gui.name", xL, y + 5));
		addTextField(new GuiNpcTextField(5, this, xTF, y, 100, 20, ""));
		y += 23;
		addButton(new GuiNpcButton(6, xL, y, 200, 20, 0, "copy.schematic", "copy.blueprint"));
		y += 30;
		addButton(new GuiNpcButton(0, xL, y, 60, 20, "gui.save"));
		addButton(new GuiNpcButton(66, guiLeft + 67, y, 60, 20, "gui.cancel"));
	}

    @Override
	public void save() {
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		tile.readFromNBT(compound);
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 0: tile.height = (short) textfield.getInteger(); break;
			case 1: tile.width = (short) textfield.getInteger(); break;
			case 2: tile.length = (short) textfield.getInteger(); break;
		}
	}

}
