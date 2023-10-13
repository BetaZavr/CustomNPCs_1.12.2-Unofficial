package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.items.ItemNpcBlock;

public class CustomDoor
extends BlockDoor
implements ITileEntityProvider, ICustomElement {

	public NBTTagCompound nbtData = new NBTTagCompound();
	
	public CustomDoor(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_"+nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
		this.hasTileEntity = false;
		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setHardness(0.0f);
		this.setResistance(10.0f);
		
		if (nbtBlock.hasKey("Hardness", 5)) { this.setHardness(nbtBlock.getFloat("Hardness")); }
		if (nbtBlock.hasKey("Resistance", 5)) { this.setResistance(nbtBlock.getFloat("Resistance")); }
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		if (this.nbtData!=null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
	}
	
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (this.nbtData!=null && this.nbtData.hasKey("InteractOpen") && !this.nbtData.getBoolean("InteractOpen")){ return false; }
		BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
		IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
		if (iblockstate.getBlock() != this) { return false; }
		state = iblockstate.cycleProperty(OPEN);
		worldIn.setBlockState(blockpos, state, 10);
		worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
		worldIn.playEvent(playerIn, ((Boolean)state.getValue(OPEN)).booleanValue() ?
				(this.blockMaterial == Material.IRON ? 1005 : 1006) :
				(this.blockMaterial == Material.IRON ? 1011 : 1012), pos, 0);
		return true;
	}
	
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) { return null; }

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		Item item = new ItemNpcBlock(this);
		for (Item it : Item.REGISTRY) {
			if (!it.getRegistryName().getResourceDomain().equals(CustomNpcs.MODID)) { continue; }
			if (it.getRegistryName().equals(this.getRegistryName())) {
				item = it;
				break;
			}
		}
		return item == null ? null : new ItemStack(item, 1, 0);
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		Item item = new ItemNpcBlock(this);
		for (Item it : Item.REGISTRY) {
			if (!it.getRegistryName().getResourceDomain().equals(CustomNpcs.MODID)) { continue; }
			if (it.getRegistryName().equals(this.getRegistryName())) {
				item = it;
				break;
			}
		}
		return item == null || state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.AIR : item;
	}

	public boolean hasTileEntity(IBlockState state) { return true; }

	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
