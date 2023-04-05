package noppes.npcs.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class CustomBlock
extends BlockInterface
implements IPermission, ICustomElement {

	public NBTTagCompound nbtData = new NBTTagCompound();
	public AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	private EnumBlockRenderType renderType = EnumBlockRenderType.MODEL;
	private IProperty<?> property;

	public CustomBlock(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		this.setName("custom_"+nbtBlock.getString("RegistryName"));
		
		this.enableStats = true;
		this.blockSoundType = SoundType.STONE;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		
		if (nbtBlock.hasKey("Hardness", 5)) { this.setHardness(nbtBlock.getFloat("Hardness")); }
		if (nbtBlock.hasKey("Resistance", 5)) { this.setResistance(nbtBlock.getFloat("Resistance")); }
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }
		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setAABB(nbtBlock.getTagList("AABB", 6));
		this.renderType = CustomBlock.getNbtRenderType(nbtBlock.getString("BlockRenderType"));
		this.setCreativeTab((CreativeTabs) CustomItems.tabBlocks);
	}

	public static EnumBlockRenderType getNbtRenderType(String string) {
		switch(string.toLowerCase()) {
			case "invisible": return EnumBlockRenderType.INVISIBLE;
			case "liquid": return EnumBlockRenderType.LIQUID;
			case "entityblock_animated": return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
			default: return EnumBlockRenderType.MODEL;
		}
	}

	private void setAABB(NBTTagList tagList) {
		double[] v = new double[6];
		for (int i=0; i<6; i++) {
			double s = i<3 ? 0.0d : 1.0d;
			if (i < tagList.tagCount()) { s = tagList.getDoubleAt(i); }
			v[i] = s;
		}
		this.FULL_BLOCK_AABB = new AxisAlignedBB(v[0], v[1], v[2], v[3], v[4], v[5]);
	}

	public static SoundType getNbtSoundType(String soundName) {
		switch(soundName.toLowerCase()) {
			case "wood": return SoundType.WOOD;
			case "ground": return SoundType.GROUND;
			case "plant": return SoundType.PLANT;
			case "metal": return SoundType.METAL;
			case "glass": return SoundType.GLASS;
			case "cloth": return SoundType.CLOTH;
			case "sand": return SoundType.SAND;
			case "snow": return SoundType.SNOW;
			case "ladder": return SoundType.LADDER;
			case "anvil": return SoundType.ANVIL;
			case "slime": return SoundType.SLIME;
			default: return SoundType.STONE;
		}
	}

	public boolean hasTileEntity() {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}
	
	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return true;
	}

	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return this.nbtData.getBoolean("IsLadder");
	}
	
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		return this.nbtData.getBoolean("IsPassable");
	}
	
	public boolean isOpaqueCube(IBlockState state) {
		return this.nbtData==null ||
				!this.nbtData.hasKey("IsOpaqueCube") ?
						false : this.nbtData.getBoolean("IsOpaqueCube"); }
	
	public boolean isFullCube(IBlockState state) { return this.nbtData==null || this.nbtData.hasKey("IsFullCube") ? false : this.nbtData.getBoolean("IsFullCube"); }
	
	public EnumBlockRenderType getRenderType(IBlockState state) { return this.renderType; }

	public boolean hasProperty() {
		return this.nbtData==null || this.nbtData.hasKey("Property", 10) && this.getProperty()!=null;
	}

	public IProperty<?> getProperty() {
		if (this.property!=null) { return this.property; }
		else if (this.nbtData==null || this.nbtData.hasKey("Property", 10)) {
			NBTTagCompound nbtProperty = this.nbtData.getCompoundTag("Property");
			switch(nbtProperty.getByte("Type")) {
				case (byte) 3:
					this.property = PropertyInteger.create(nbtProperty.getString("Name"), nbtProperty.getInteger("Min"), nbtProperty.getInteger("Max"));
					return this.property;
				case (byte) 4:
					this.property = PropertyDirection.create(nbtProperty.getString("Name"), EnumFacing.Plane.HORIZONTAL);
					return this.property;
				default:
					
			}
		}
		return null;
	}

	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
