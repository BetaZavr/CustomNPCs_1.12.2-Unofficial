package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.SchematicWrapper;

public class GuiBlockBuilder
extends GuiNPCInterface
implements IGuiData, ICustomScrollListener, IScrollData, GuiYesNoCallback {

	private GuiCustomScroll scroll;
	private ISchematic selected;
	private final TileBuilder tile;
	private final int x;
	private final int y;
	private final int z;

	public GuiBlockBuilder(int xPos, int yPos, int zPos) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		selected = null;
		x = xPos;
		y = yPos;
		z = zPos;
		tile = (TileBuilder) player.world.getTileEntity(new BlockPos(x, y, z));
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getId() == 3) {
			if (((GuiNpcButtonYesNo) button).getBoolean()) {
				TileBuilder.SetDrawPos(new BlockPos(x, y, z));
				tile.setDrawSchematic(new SchematicWrapper(selected));
			} else {
				TileBuilder.SetDrawPos(null);
				tile.setDrawSchematic(null);
			}
		}
		if (button.getId() == 4) {
			tile.enabled = ((GuiNpcButtonYesNo) button).getBoolean();
		}
		if (button.getId() == 5) {
			tile.rotation = button.getValue();
		}
		if (button.getId() == 6) {
			setSubGui(new SubGuiNpcAvailability(tile.availability, this));
		}
		if (button.getId() == 7) {
			tile.finished = ((GuiNpcButtonYesNo) button).getBoolean();
			Client.sendData(EnumPacketServer.SchematicsSet, x, y, z, scroll.getSelected());
		}
		if (button.getId() == 8) {
			tile.started = ((GuiNpcButtonYesNo) button).getBoolean();
		}
		if (button.getId() == 10) {
			save();
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("schematic.instantBuildText").getFormattedText(), 0);
			displayGuiScreen(guiyesno);
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.SchematicsBuild, x, y, z);
			close();
			selected = null;
		} else {
			NoppesUtil.openGUI(player, this);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(125, 208); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 4;
		addScroll(scroll);
		if (selected != null) {
			int xL = guiLeft + 130;
			int xB = guiLeft + 200;
			int y = guiTop + 4;
			addLabel(new GuiNpcLabel(3, "schematic.preview", xL, y + 5));
			addButton(new GuiNpcButtonYesNo(3, xB, y, TileBuilder.has(tile.getPos())));
			y += 21;
			addLabel(new GuiNpcLabel(0, new TextComponentTranslation("schematic.width").getFormattedText() + ": " + selected.getWidth(), xL, y));
			y += 11;
			addLabel(new GuiNpcLabel(1, new TextComponentTranslation("schematic.length").getFormattedText() + ": " + selected.getLength(), xL, y));
			y += 11;
			addLabel(new GuiNpcLabel(2, new TextComponentTranslation("schematic.height").getFormattedText() + ": " + selected.getHeight(), xL, y));
			y += 14;
			addButton(new GuiNpcButtonYesNo(4, xB, y, tile.enabled));
			addLabel(new GuiNpcLabel(4, new TextComponentTranslation("gui.enabled").getFormattedText(), xL, y + 5));
			y += 22;
			addButton(new GuiNpcButtonYesNo(7, xB, y, tile.finished));
			addLabel(new GuiNpcLabel(7, new TextComponentTranslation("gui.finished").getFormattedText(), xL, y + 5));
			y += 22;
			addButton(new GuiNpcButtonYesNo(8, xB, y, tile.started));
			addLabel(new GuiNpcLabel(8, new TextComponentTranslation("gui.started").getFormattedText(), xL, y + 5));
			y += 22;
			addLabel(new GuiNpcLabel(9, new TextComponentTranslation("gui.yoffset").getFormattedText(), xL, y + 5));
			GuiNpcTextField textField = new GuiNpcTextField(9, this, xB, y, 50, 20, tile.yOffset + "");
			textField.setMinMaxDefault(-10, 10, 0);
			addTextField(textField);
			y += 22;
			addLabel(new GuiNpcLabel(5, new TextComponentTranslation("movement.rotation").getFormattedText(), xL, y + 5));
			addButton(new GuiNpcButton(5, xB, y, 50, 20, new String[] { "0", "90", "180", "270" }, tile.rotation));
			y += 22;
			addButton(new GuiNpcButton(6, xL, y, 120, 20, "availability.options"));
			y += 22;
			addButton(new GuiNpcButton(10, xL, y, 120, 20, "schematic.instantBuild"));
		}
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.SchematicsTile, x, y, z);
	}

	@Override
	public void save() {
		if (getTextField(9) != null) { tile.yOffset = getTextField(9).getInteger(); }
		Client.sendData(EnumPacketServer.SchematicsTileSave, x, y, z, tile.writePartNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (!scroll.hasSelected()) { return; }
		if (selected != null) { getButton(3).setDisplay(0); }
		TileBuilder.SetDrawPos(null);
		tile.setDrawSchematic(null);
		Client.sendData(EnumPacketServer.SchematicsSet, x, y, z, scroll.getSelected());
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		scroll.setList(list);
		if (selected != null) { scroll.setSelected(selected.getName()); }
		initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Width")) {
			List<IBlockState> states = new ArrayList<>();
			NBTTagList list = compound.getTagList("Data", 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				states.add(NBTUtil.readBlockState(list.getCompoundTagAt(i)));
			}
			selected = new ISchematic() {
				@Override
				public IBlockState getBlockState(int i) {
					return states.get(i);
				}

				@Override
				public IBlockState getBlockState(int x, int y, int z) {
					return getBlockState((y * getLength() + z) * getWidth() + x);
				}

				@Override
				public NBTTagList getEntitys() {
					return new NBTTagList();
				}

				@Override
				public short getHeight() {
					return compound.getShort("Height");
				}

				@Override
				public short getLength() {
					return compound.getShort("Length");
				}

				@Override
				public String getName() {
					return compound.getString("SchematicName");
				}

				@Override
				public NBTTagCompound getNBT() {
					return null;
				}

				@Override
				public BlockPos getOffset() {
					return BlockPos.ORIGIN;
				}

				@Override
				public NBTTagCompound getTileEntity(int i) {
					return null;
				}

				@Override
				public int getTileEntitySize() {
					return 0;
				}

				@Override
				public short getWidth() {
					return compound.getShort("Width");
				}

				@Override
				public boolean hasEntitys() {
					return false;
				}
			};
			if (TileBuilder.has(tile.getPos())) {
				SchematicWrapper wrapper = new SchematicWrapper(selected);
				wrapper.rotation = tile.rotation;
				tile.setDrawSchematic(wrapper);
			}
			scroll.setSelected(selected.getName());
			scroll.scrollTo(selected.getName());
		} else {
			tile.readPartNBT(compound);
		}
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

}
