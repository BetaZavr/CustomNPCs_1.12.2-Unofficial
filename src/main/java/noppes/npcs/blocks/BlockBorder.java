package noppes.npcs.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class BlockBorder extends BlockInterface implements IPermission {
	public static PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

	public BlockBorder() {
		super(Material.ROCK);
		this.setName("npcborder");
		this.setSoundType(SoundType.STONE);
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
		this.setBlockUnbreakable();
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockBorder.ROTATION });
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileBorder();
	}

	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockBorder.ROTATION);
	}

	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockBorder.ROTATION, meta);
	}

	private TileBorder getTile(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileBorder) { return (TileBorder) tile; }
		return null;
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SaveTileEntity;
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (!world.isRemote && currentItem != null && currentItem.getItem() == CustomRegisters.wand) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.Border, null, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity,
			ItemStack stack) {
		int l = MathHelper.floor(entity.rotationYaw * 4.0f / 360.0f + 0.5) & 0x3;
		l %= 4;
		world.setBlockState(pos, state.withProperty(BlockBorder.ROTATION, l));
		TileBorder tile = (TileBorder) world.getTileEntity(pos);
		TileBorder adjacent = this.getTile(world, pos.south());
		if (adjacent == null) {
			for (int i=0; i<26; i++) {
				switch(i) {
					case 0: { adjacent = this.getTile(world, pos.south()); break; }
					case 1: { adjacent = this.getTile(world, pos.north()); break; }
					case 2: { adjacent = this.getTile(world, pos.east()); break; }
					case 3: { adjacent = this.getTile(world, pos.west()); break; }
					case 4: { adjacent = this.getTile(world, pos.up()); break; }
					case 5: { adjacent = this.getTile(world, pos.down()); break; }
					case 6: { adjacent = this.getTile(world, pos.south().east()); break; }
					case 7: { adjacent = this.getTile(world, pos.south().west()); break; }
					case 8: { adjacent = this.getTile(world, pos.north().east()); break; }
					case 9: { adjacent = this.getTile(world, pos.north().west()); break; }
					case 10: { adjacent = this.getTile(world, pos.down().south()); break; }
					case 11: { adjacent = this.getTile(world, pos.down().north()); break; }
					case 12: { adjacent = this.getTile(world, pos.down().east()); break; }
					case 13: { adjacent = this.getTile(world, pos.down().west()); break; }
					case 14: { adjacent = this.getTile(world, pos.down().south().east()); break; }
					case 15: { adjacent = this.getTile(world, pos.down().south().west()); break; }
					case 16: { adjacent = this.getTile(world, pos.down().north().east()); break; }
					case 17: { adjacent = this.getTile(world, pos.down().north().west()); break; }
					case 18: { adjacent = this.getTile(world, pos.up().south()); break; }
					case 19: { adjacent = this.getTile(world, pos.up().north()); break; }
					case 20: { adjacent = this.getTile(world, pos.up().east()); break; }
					case 21: { adjacent = this.getTile(world, pos.up().west()); break; }
					case 22: { adjacent = this.getTile(world, pos.up().south().east()); break; }
					case 23: { adjacent = this.getTile(world, pos.up().south().west()); break; }
					case 24: { adjacent = this.getTile(world, pos.up().north().east()); break; }
					case 25: { adjacent = this.getTile(world, pos.up().north().west()); break; }
				}
				if (adjacent != null) { break; }
			}
		}
		if (adjacent != null) {
			NBTTagCompound compound = new NBTTagCompound();
			adjacent.writeExtraNBT(compound);
			tile.readExtraNBT(compound);
		}
		tile.rotation = l;
		if (entity instanceof EntityPlayer && !world.isRemote) {
			if (adjacent==null) { // New
				NoppesUtilServer.sendOpenGui((EntityPlayer) entity, EnumGuiType.Border, null, pos.getX(), pos.getY(), pos.getZ());
			} else { // Copy
				((EntityPlayer) entity).sendMessage(new TextComponentTranslation("barrier.copied.around"));
			}
		}
	}
	
	public boolean isOpaqueCube(IBlockState state) { return false; }
	
	public boolean isFullCube(IBlockState state) { return false; }

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() { return BlockRenderLayer.CUTOUT; }
	
	public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	

}
