package noppes.npcs.blocks;

import java.util.ArrayList;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileMailbox;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;

import javax.annotation.Nonnull;

public class BlockMailbox extends BlockInterface {

	public static PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);
	public static PropertyInteger TYPE = PropertyInteger.create("type", 0, 2);

	public BlockMailbox() {
		super(Material.IRON);
		this.setName("npcmailbox");
		this.setSoundType(SoundType.METAL);
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomRegisters.tab);
	}

	protected @Nonnull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockMailbox.TYPE, BlockMailbox.ROTATION);
	}

	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return new TileMailbox();
	}

	public int damageDropped(@Nonnull IBlockState state) {
		return state.getValue(BlockMailbox.TYPE);
	}

	public @Nonnull ArrayList<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		int damage = state.getValue(BlockMailbox.TYPE);
		ret.add(new ItemStack(this, 1, damage));
		return ret;
	}

	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(BlockMailbox.ROTATION) | state.getValue(BlockMailbox.TYPE) << 2;
	}

	public @Nonnull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockMailbox.TYPE, ((meta >> 2) % 3))
				.withProperty(BlockMailbox.ROTATION, ((meta | 0x4) % 4));
	}

	public void getSubBlocks(@Nonnull CreativeTabs par2CreativeTabs, @Nonnull NonNullList<ItemStack> par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
		par3List.add(new ItemStack(this, 1, 2));
	}

	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!par1World.isRemote) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI, EnumGuiType.PlayerMailbox, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase entity, @Nonnull ItemStack stack) {
		int l = MathHelper.floor(entity.rotationYaw * 4.0f / 360.0f + 0.5) & 0x3;
		world.setBlockState(pos, state.withProperty(BlockMailbox.TYPE, stack.getItemDamage()).withProperty(BlockMailbox.ROTATION, (l % 4)), 2);
	}

}
