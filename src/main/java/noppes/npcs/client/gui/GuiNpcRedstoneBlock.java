package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

import java.awt.*;

public class GuiNpcRedstoneBlock
extends GuiNPCInterface
implements IGuiData {

	private static final int minRange = 0;
	private static final int maxRange = 50;
	private final TileRedstoneBlock tile;

	public GuiNpcRedstoneBlock(int x, int y, int z) {
		super();
		tile = (TileRedstoneBlock) player.world.getTileEntity(new BlockPos(x, y, z));
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: close(); break;
			case 1: {
				tile.isDetailed = button.getValue() == 1;
				initGui();
				break;
			}
			case 4: {
				save();
				setSubGui(new SubGuiNpcAvailability(tile.availability, this));
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int color = new Color(0xFFFFFF).getRGB();
		// options
		GuiNpcButton button = new GuiNpcButton(4, guiLeft + 40, guiTop + 20, 120, 20, "availability.options");
		addButton(button);
		// detailed
		addLabel(new GuiNpcLabel(11, "gui.detailed", guiLeft + 40, guiTop + 47, color));
		button = new GuiNpcButton(1, guiLeft + 110, guiTop + 42, 50, 20, new String[] { "gui.no", "gui.yes" }, (tile.isDetailed ? 1 : 0));
		addButton(button);
		// data
		GuiNpcTextField textField;
		if (tile.isDetailed) {
			// x on
			addLabel(new GuiNpcLabel(0, new TextComponentTranslation("bard.ondistance").getFormattedText() + " X:", guiLeft + 1, guiTop + 76, color));
			textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 80, guiTop + 71, 30, 20, tile.onRangeX + "");
			textField.setMinMaxDefault(minRange, maxRange, 6);
			addTextField(textField);
			// y on
			addLabel(new GuiNpcLabel(1, "Y:", guiLeft + 113, guiTop + 76, color));
			textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 122, guiTop + 71, 30, 20, tile.onRangeY + "");
			textField.setMinMaxDefault(minRange, maxRange, 6);
			addTextField(textField);
			// z on
			addLabel(new GuiNpcLabel(2, "Z:", guiLeft + 155, guiTop + 76, color));
			textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 164, guiTop + 71, 30, 20, tile.onRangeZ + "");
			textField.setMinMaxDefault(minRange, maxRange, 6);
			addTextField(textField);
			// x off
			addLabel(new GuiNpcLabel(3, new TextComponentTranslation("bard.offdistance").getFormattedText() + " X:", guiLeft - 3, guiTop + 99, color));
			textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 80, guiTop + 94, 30, 20, tile.offRangeX + "");
			textField.setMinMaxDefault(minRange, maxRange, 10);
			addTextField(textField);
			// y off
			addLabel(new GuiNpcLabel(4, "Y:", guiLeft + 113, guiTop + 99, color));
			textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 122, guiTop + 94, 30, 20, tile.offRangeY + "");
			textField.setMinMaxDefault(minRange, maxRange, 10);
			addTextField(textField);
			// z off
			addLabel(new GuiNpcLabel(5, "Z:", guiLeft + 155, guiTop + 99, color));
			textField = new GuiNpcTextField(5, this, fontRenderer, guiLeft + 164, guiTop + 94, 30, 20, tile.offRangeZ + "");
        }
		else {
			// range on
			addLabel(new GuiNpcLabel(0, "bard.ondistance", guiLeft + 1, guiTop + 76, color));
			textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 80, guiTop + 71, 30, 20, tile.onRange + "");
			textField.setMinMaxDefault(minRange, maxRange, 6);
			addTextField(textField);
			// range off
			addLabel(new GuiNpcLabel(3, "bard.offdistance", guiLeft - 3, guiTop + 99, color));
			textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 80, guiTop + 94, 30, 20, tile.offRange + "");
        }
        textField.setMinMaxDefault(minRange, maxRange, 10);
        addTextField(textField);
        button = new GuiNpcButton(0, guiLeft + 40, guiTop + 190, 120, 20, "gui.done");
		button.setHoverText("hover.exit");
		addButton(button);
	}

	@Override
	public void save() {
		if (tile == null) {
			return;
		}
		if (tile.isDetailed) {
			tile.onRangeX = getTextField(0).getInteger();
			tile.onRangeY = getTextField(1).getInteger();
			tile.onRangeZ = getTextField(2).getInteger();
			tile.offRangeX = getTextField(3).getInteger();
			tile.offRangeY = getTextField(4).getInteger();
			tile.offRangeZ = getTextField(5).getInteger();
			if (tile.onRangeX > tile.offRangeX) { tile.offRangeX = tile.onRangeX; }
			if (tile.onRangeY > tile.offRangeY) { tile.offRangeY = tile.onRangeY; }
			if (tile.onRangeZ > tile.offRangeZ) { tile.offRangeZ = tile.onRangeZ; }
		} else {
			tile.onRange = getTextField(0).getInteger();
			tile.offRange = getTextField(3).getInteger();
			if (tile.onRange > tile.offRange) { tile.offRange = tile.onRange; }
		}
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		compound.removeTag("BlockActivated");
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		tile.readFromNBT(compound);
		initGui();
	}

}
