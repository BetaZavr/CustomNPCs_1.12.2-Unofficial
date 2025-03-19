package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.Util;

import java.awt.*;

public class GuiBorderBlock
extends GuiNPCInterface
implements IGuiData {

	private final TileBorder tile;

	public GuiBorderBlock(int x, int y, int z) {
		tile = (TileBorder) player.world.getTileEntity(new BlockPos(x, y, z));
		closeOnEsc = true;
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 4: {
				save();
				setSubGui(new SubGuiNpcAvailability(tile.availability, this));
				break;
			}
			case 5: {
				tile.creative = ((GuiNpcButtonYesNo) button).getBoolean();
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 60;
		int xl = guiLeft + 1;
		int y = guiTop + 40;
		// availability
		GuiNpcButton button = new GuiNpcButton(4, x - 20, y, 120, 20, "availability.available");
		button.setHoverText("border.hover.availability");
		addButton(button);
		int color = new Color(0xFFFFFF).getRGB();
		// height
		addLabel(new GuiNpcLabel(0, "schematic.height", xl, (y += 25) + 5, color));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x, y, 40, 20, tile.height + "");
		textField.setMinMaxDefault(0, 500, 6);
		addTextField(textField);
		// message
		addLabel(new GuiNpcLabel(1, "gui.message", xl, (y += 24) + 5, color));
		textField = new GuiNpcTextField(1, this, fontRenderer, x, y, 200, 20, tile.message);
		ITextComponent hover = new TextComponentTranslation("border.hover.message");
		ITextComponent mes = new TextComponentTranslation(tile.message);
		if (!Util.instance.deleteColor(tile.message).equals(Util.instance.deleteColor(mes.getFormattedText()))) {
			hover.appendSibling(new TextComponentString("<br>"));
			hover.appendSibling(new TextComponentTranslation("gui.translation", mes.getFormattedText()));
		}
		textField.setHoverText(hover.getFormattedText());
		addTextField(textField);
		// gm type
		addLabel(new GuiNpcLabel(2, "gui.creative", xl, (y += 24) + 5, color));
		button = new GuiNpcButtonYesNo(5, x - 1, y, 60, 20, tile.creative);
		button.setHoverText("border.hover.creative");
		addButton(button);
		// exit
		addButton(new GuiNpcButton(66, x - 20, guiTop + 188, 120, 20, "gui.done"));
		button.setHoverText("hover.exit");
		addButton(button);
	}

	@Override
	public void save() {
		if (tile == null) {
			return;
		}
		tile.height = getTextField(0).getInteger();
		tile.message = getTextField(1).getText();
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
