package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;

public class GuiBlockCopy
extends GuiNPCInterface
implements IGuiData, ITextfieldListener {

	private final TileCopy tile;
	public int x, y, z;

	public GuiBlockCopy(int xPos, int yPos, int zPos) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		x = xPos;
		y = yPos;
		z = zPos;
		tile = (TileCopy) player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			Client.sendData(EnumPacketServer.SchematicStore, getTextField(5).getText(), getButton(6).getValue(), compound);
			close();
		}
		if (button.id == 1) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int xL = guiLeft + 5;
		int xTF = guiLeft + 104;
		int y = guiTop + 4;
		addLabel(new GuiNpcLabel(0, "schematic.height", xL, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, xTF, y, 50, 20, tile.height + "");
		textField.setMinMaxDefault(0, 100, 10);
		addTextField(textField);
		y += 23;
		addLabel(new GuiNpcLabel(1, "schematic.width", xL, y + 5));
		textField = new GuiNpcTextField(1, this, xTF, y, 50, 20, tile.width + "");
		textField.setMinMaxDefault(0, 100, 10);
		addTextField(textField);
		y += 23;
		addLabel(new GuiNpcLabel(2, "schematic.length", xL, y + 5));
		textField = new GuiNpcTextField(2, this, xTF, y, 50, 20, tile.length + "");
		textField.setMinMaxDefault(0, 100, 10);
		addTextField(textField);
		y += 23;
		addLabel(new GuiNpcLabel(5, "gui.name", xL, y + 5));
		addTextField(new GuiNpcTextField(5, this, xTF, y, 100, 20, ""));
		y += 23;
		addButton(new GuiNpcButton(6, xL, y, 200, 20, 0, "copy.schematic", "copy.blueprint"));
		y += 30;
		addButton(new GuiNpcButton(0, xL, y, 60, 20, "gui.save"));
		addButton(new GuiNpcButton(1, guiLeft + 67, y, 60, 20, "gui.cancel"));
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
		switch (textfield.getId()) {
			case 0: {
				tile.height = (short) textfield.getInteger();
				break;
			}
			case 1: {
				tile.width = (short) textfield.getInteger();
				break;
			}
			case 2: {
				tile.length = (short) textfield.getInteger();
				break;
			}
		}
	}
}
