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
import noppes.npcs.api.item.ICustomItem;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class CustomBlock
extends BlockInterface
implements IPermission, ICustomItem {

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
		
		this.setSoundType(nbtBlock.getString("SoundType"));
		this.setAABB(nbtBlock.getTagList("AABB", 6));
		this.setRenderType(nbtBlock.getString("BlockRenderType"));
		
		this.setCreativeTab((CreativeTabs) CustomItems.tabBlocks);
	}

	private void setRenderType(String string) {
		switch(string.toLowerCase()) {
			case "invisible":
				this.renderType = EnumBlockRenderType.INVISIBLE;
				break;
			case "liquid":
				this.renderType = EnumBlockRenderType.LIQUID;
				break;
			case "entityblock_animated":
				this.renderType = EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
				break;
			default:
				this.renderType = EnumBlockRenderType.MODEL;
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

	private void setSoundType(String soundName) {
		SoundType type;
		switch(soundName.toLowerCase()) {
			case "wood":
				type = SoundType.WOOD;
				break;
			case "ground":
				type = SoundType.GROUND;
				break;
			case "plant":
				type = SoundType.PLANT;
				break;
			case "metal":
				type = SoundType.METAL;
				break;
			case "glass":
				type = SoundType.GLASS;
				break;
			case "cloth":
				type = SoundType.CLOTH;
				break;
			case "sand":
				type = SoundType.SAND;
				break;
			case "snow":
				type = SoundType.SNOW;
				break;
			case "ladder":
				type = SoundType.LADDER;
				break;
			case "anvil":
				type = SoundType.ANVIL;
				break;
			case "slime":
				type = SoundType.SLIME;
				break;
			default:
				type = SoundType.STONE;
		}
		this.setSoundType(type);
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
	public NBTTagCompound getData() { return this.nbtData; }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
}
