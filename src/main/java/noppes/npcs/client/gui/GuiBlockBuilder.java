package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.SchematicWrapper;

public class GuiBlockBuilder extends GuiNPCInterface
		implements IGuiData, ICustomScrollListener, IScrollData, GuiYesNoCallback {
	private GuiCustomScroll scroll;
	private ISchematic selected;
	private TileBuilder tile;
	private int x;
	private int y;
	private int z;

	public GuiBlockBuilder(int x, int y, int z) {
		this.selected = null;
		this.x = x;
		this.y = y;
		this.z = z;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
		this.tile = (TileBuilder) this.player.world.getTileEntity(new BlockPos(x, y, z));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 3) {
			GuiNpcButtonYesNo button = (GuiNpcButtonYesNo) guibutton;
			if (button.getBoolean()) {
				TileBuilder.SetDrawPos(new BlockPos(this.x, this.y, this.z));
				this.tile.setDrawSchematic(new SchematicWrapper(this.selected));
			} else {
				TileBuilder.SetDrawPos(null);
				this.tile.setDrawSchematic(null);
			}
		}
		if (guibutton.id == 4) {
			this.tile.enabled = ((GuiNpcButtonYesNo) guibutton).getBoolean();
		}
		if (guibutton.id == 5) {
			this.tile.rotation = ((GuiNpcButton) guibutton).getValue();
		}
		if (guibutton.id == 6) {
			this.setSubGui(new SubGuiNpcAvailability(this.tile.availability));
		}
		if (guibutton.id == 7) {
			this.tile.finished = ((GuiNpcButtonYesNo) guibutton).getBoolean();
			Client.sendData(EnumPacketServer.SchematicsSet, this.x, this.y, this.z, this.scroll.getSelected());
		}
		if (guibutton.id == 8) {
			this.tile.started = ((GuiNpcButtonYesNo) guibutton).getBoolean();
		}
		if (guibutton.id == 10) {
			this.save();
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, "",
					new TextComponentTranslation("schematic.instantBuildText").getFormattedText(), 0);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.SchematicsBuild, this.x, this.y, this.z);
			this.close();
			this.selected = null;
		} else {
			NoppesUtil.openGUI((EntityPlayer) this.player, this);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(125, 208);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 4;
		this.addScroll(this.scroll);
		if (this.selected != null) {
			int y = this.guiTop + 4;
			// int size = this.selected.getWidth() * this.selected.getHeight() *
			// this.selected.getLength();
			this.addButton(new GuiNpcButtonYesNo(3, this.guiLeft + 200, y, TileBuilder.has(this.tile.getPos())));
			this.addLabel(new GuiNpcLabel(3, "schematic.preview", this.guiLeft + 130, y + 5));
			int id = 0;
			String string = new TextComponentTranslation("schematic.width").getFormattedText() + ": "
					+ this.selected.getWidth();
			int x = this.guiLeft + 130;
			y += 21;
			this.addLabel(new GuiNpcLabel(id, string, x, y));
			int id2 = 1;
			String string2 = new TextComponentTranslation("schematic.length").getFormattedText() + ": "
					+ this.selected.getLength();
			int x2 = this.guiLeft + 130;
			y += 11;
			this.addLabel(new GuiNpcLabel(id2, string2, x2, y));
			int id3 = 2;
			String string3 = new TextComponentTranslation("schematic.height").getFormattedText() + ": "
					+ this.selected.getHeight();
			int x3 = this.guiLeft + 130;
			y += 11;
			this.addLabel(new GuiNpcLabel(id3, string3, x3, y));
			int id4 = 4;
			int x4 = this.guiLeft + 200;
			y += 14;
			this.addButton(new GuiNpcButtonYesNo(id4, x4, y, this.tile.enabled));
			this.addLabel(new GuiNpcLabel(4, new TextComponentTranslation("gui.enabled").getFormattedText(),
					this.guiLeft + 130, y + 5));
			int id5 = 7;
			int x5 = this.guiLeft + 200;
			y += 22;
			this.addButton(new GuiNpcButtonYesNo(id5, x5, y, this.tile.finished));
			this.addLabel(new GuiNpcLabel(7, new TextComponentTranslation("gui.finished").getFormattedText(),
					this.guiLeft + 130, y + 5));
			int id6 = 8;
			int x6 = this.guiLeft + 200;
			y += 22;
			this.addButton(new GuiNpcButtonYesNo(id6, x6, y, this.tile.started));
			this.addLabel(new GuiNpcLabel(8, new TextComponentTranslation("gui.started").getFormattedText(),
					this.guiLeft + 130, y + 5));
			int id7 = 9;
			int i = this.guiLeft + 200;
			y += 22;
			this.addTextField(new GuiNpcTextField(id7, this, i, y, 50, 20, this.tile.yOffest + ""));
			this.addLabel(new GuiNpcLabel(9, new TextComponentTranslation("gui.yoffset").getFormattedText(),
					this.guiLeft + 130, y + 5));
			this.getTextField(9).numbersOnly = true;
			this.getTextField(9).setMinMaxDefault(-10, 10, 0);
			int j = 5;
			int k = this.guiLeft + 200;
			y += 22;
			this.addButton(
					new GuiNpcButton(j, k, y, 50, 20, new String[] { "0", "90", "180", "270" }, this.tile.rotation));
			this.addLabel(new GuiNpcLabel(5, new TextComponentTranslation("movement.rotation").getFormattedText(),
					this.guiLeft + 130, y + 5));
			int l = 6;
			int m = this.guiLeft + 130;
			y += 22;
			this.addButton(new GuiNpcButton(l, m, y, 120, 20, "availability.options"));
			int i2 = 10;
			int j2 = this.guiLeft + 130;
			y += 22;
			this.addButton(new GuiNpcButton(i2, j2, y, 120, 20, "schematic.instantBuild"));
		}
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.SchematicsTile, this.x, this.y, this.z);
	}

	@Override
	public void save() {
		if (this.getTextField(9) != null) {
			this.tile.yOffest = this.getTextField(9).getInteger();
		}
		Client.sendData(EnumPacketServer.SchematicsTileSave, this.x, this.y, this.z,
				this.tile.writePartNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) {
			return;
		}
		if (this.selected != null) {
			this.getButton(3).setDisplay(0);
		}
		TileBuilder.SetDrawPos(null);
		this.tile.setDrawSchematic(null);
		Client.sendData(EnumPacketServer.SchematicsSet, this.x, this.y, this.z, scroll.getSelected());
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.scroll.setList(list);
		if (this.selected != null) {
			this.scroll.setSelected(this.selected.getName());
		}
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Width")) {
			List<IBlockState> states = new ArrayList<IBlockState>();
			NBTTagList list = compound.getTagList("Data", 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				states.add(NBTUtil.readBlockState(list.getCompoundTagAt(i)));
			}
			this.selected = new ISchematic() {
				@Override
				public IBlockState getBlockState(int i) {
					return states.get(i);
				}

				@Override
				public IBlockState getBlockState(int x, int y, int z) {
					return this.getBlockState((y * this.getLength() + z) * this.getWidth() + x);
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

				@Override
				public NBTTagList getEntitys() {
					return new NBTTagList();
				}

				@Override
				public BlockPos getOffset() {
					return BlockPos.ORIGIN;
				}
			};
			if (TileBuilder.has(this.tile.getPos())) {
				SchematicWrapper wrapper = new SchematicWrapper(this.selected);
				wrapper.rotation = this.tile.rotation;
				this.tile.setDrawSchematic(wrapper);
			}
			this.scroll.setSelected(this.selected.getName());
			this.scroll.scrollTo(this.selected.getName());
		} else {
			this.tile.readPartNBT(compound);
		}
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
	}
}
