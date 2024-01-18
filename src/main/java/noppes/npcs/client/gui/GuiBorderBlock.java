package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiBorderBlock extends GuiNPCInterface implements IGuiData {
	private TileBorder tile;

	public GuiBorderBlock(int x, int y, int z) {
		this.tile = (TileBorder) this.player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.close();
		}
		if (button.id == 4) {
			this.save();
			this.setSubGui(new SubGuiNpcAvailability(this.tile.availability));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(4, this.guiLeft + 40, this.guiTop + 40, 120, 20, "availability.available"));
		this.addLabel(new GuiNpcLabel(0, "Height", this.guiLeft + 1, this.guiTop + 76, 16777215));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 60, this.guiTop + 71, 40, 20, this.tile.height + ""));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, 500, 6);
		this.addLabel(new GuiNpcLabel(1, "Message", this.guiLeft + 1, this.guiTop + 100, 16777215));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 60, this.guiTop + 95, 200, 20,
				this.tile.message));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 40, this.guiTop + 190, 120, 20, "Done"));
	}

	@Override
	public void save() {
		if (this.tile == null) {
			return;
		}
		this.tile.height = this.getTextField(0).getInteger();
		this.tile.message = this.getTextField(1).getText();
		NBTTagCompound compound = new NBTTagCompound();
		this.tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.tile.readFromNBT(compound);
		this.initGui();
	}
}
