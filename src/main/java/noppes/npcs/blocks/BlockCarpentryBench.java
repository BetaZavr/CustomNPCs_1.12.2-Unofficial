package noppes.npcs.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.blocks.tiles.TileBlockAnvil;
import noppes.npcs.constants.EnumGuiType;

import javax.annotation.Nonnull;

public class BlockCarpentryBench extends BlockInterface {

	public static PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

	public BlockCarpentryBench() {
		super(Material.WOOD);
		this.setName("npccarpentybench");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomRegisters.tab);
		this.setSoundType(SoundType.WOOD);
	}

	protected @Nonnull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockCarpentryBench.ROTATION);
	}

	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return new TileBlockAnvil();
	}

	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(BlockCarpentryBench.ROTATION);
	}

	public @Nonnull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockCarpentryBench.ROTATION, (meta % 4));
	}

	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!par1World.isRemote) {
			player.openGui(CustomNpcs.instance, EnumGuiType.PlayerAnvil.ordinal(), par1World, pos.getX(), pos.getY(),
					pos.getZ());
		}
		return true;
	}

	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase entity, @Nonnull ItemStack stack) {
		int var6 = MathHelper.floor(entity.rotationYaw / 90.0f + 0.5) & 0x3;
		world.setBlockState(pos, state.withProperty(BlockCarpentryBench.ROTATION, var6), 2);
	}

}
