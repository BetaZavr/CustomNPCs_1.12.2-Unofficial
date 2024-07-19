package noppes.npcs.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

import javax.annotation.Nonnull;

public class BlockBuilder extends BlockInterface implements IPermission {

	public static PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

	public BlockBuilder() {
		super(Material.ROCK);
		this.setName("npcbuilderblock");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomRegisters.tab);
		this.setSoundType(SoundType.STONE);
	}

	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		if (TileBuilder.has(pos)) {
			TileBuilder.DrawPoses.remove(pos);
		}
	}

	protected @Nonnull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockBuilder.ROTATION);
	}

	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return new TileBuilder();
	}

	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(BlockBuilder.ROTATION);
	}

	public @Nonnull EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public @Nonnull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockBuilder.ROTATION, meta);
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SchematicsSet || e == EnumPacketServer.SchematicsTile
				|| e == EnumPacketServer.SchematicsTileSave || e == EnumPacketServer.SchematicsBuild;
	}

	public boolean onBlockActivated(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (par1World.isRemote) {
			return true;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem.getItem() == CustomRegisters.wand
				|| currentItem.getItem() == Item.getItemFromBlock(CustomRegisters.builder)) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BuilderBlock, null, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase entity, @Nonnull ItemStack stack) {
		int var6 = MathHelper.floor(entity.rotationYaw / 90.0f + 0.5) & 0x3;
		world.setBlockState(pos, state.withProperty(BlockBuilder.ROTATION, var6), 2);
		if (entity instanceof EntityPlayer && !world.isRemote) {
			NoppesUtilServer.sendOpenGui((EntityPlayer) entity, EnumGuiType.BuilderBlock, null, pos.getX(), pos.getY(),
					pos.getZ());
		}
	}

}
