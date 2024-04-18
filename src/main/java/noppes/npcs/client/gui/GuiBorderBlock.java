package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.AdditionalMethods;

public class GuiBorderBlock extends GuiNPCInterface implements IGuiData {
	private TileBorder tile;

	public GuiBorderBlock(int x, int y, int z) {
		this.tile = (TileBorder) this.player.world.getTileEntity(new BlockPos(x, y, z));
		this.closeOnEsc = true;
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
		if (button.id == 5) {
			this.tile.creative = ((GuiNpcButtonYesNo) button).getBoolean();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 60;
		int xl = this.guiLeft + 1;
		int y = this.guiTop + 40;
		this.addButton(new GuiNpcButton(4, x - 20, y, 120, 20, "availability.available"));
		
		this.addLabel(new GuiNpcLabel(0, "schematic.height", xl, (y += 25) + 5, 0xFFFFFF));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x, y, 40, 20, this.tile.height + ""));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, 500, 6);
		
		this.addLabel(new GuiNpcLabel(1, "gui.message", xl, (y += 24) + 5, 0xFFFFFF));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, x, y, 200, 20, this.tile.message));
		
		this.addLabel(new GuiNpcLabel(2, "gui.creative", xl, (y += 24) + 5, 0xFFFFFF));
		this.addButton(new GuiNpcButtonYesNo(5, x - 1, y, 60, 20, this.tile.creative));
		
		this.addButton(new GuiNpcButton(0, x - 20, this.guiTop + 188, 120, 20, "gui.done"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui!=null || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("border.hover.availability").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("border.hover.creative").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("border.hover.height").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			ITextComponent hover = new TextComponentTranslation("border.hover.message");
			ITextComponent mes = new TextComponentTranslation(this.tile.message);
			if (!AdditionalMethods.instance.deleteColor(this.tile.message).equals(AdditionalMethods.instance.deleteColor(mes.getFormattedText()))) {
				hover.appendSibling(new TextComponentString("<br>"));
				hover.appendSibling(new TextComponentTranslation("gui.translation", mes.getFormattedText()));
			}
			this.setHoverText(hover.getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
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
