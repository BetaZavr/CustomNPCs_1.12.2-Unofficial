package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.AdditionalMethods;

public class CustomBlockPortal
extends BlockEndPortal
implements ICustomElement {
	
	public NBTTagCompound nbtData = new NBTTagCompound();

	public CustomBlockPortal(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_"+nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());	
		
		this.enableStats = true;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		
		this.setHardness(-1.0F);
		this.setResistance(6000000.0F);
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }
		
		this.setCreativeTab((CreativeTabs) CustomItems.tabBlocks);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		int id = 0;
		if (this.nbtData.hasKey("DimentionID", 3)) { id = this.nbtData.getInteger("DimentionID"); }
		if (!DimensionManager.isDimensionRegistered(id)) { id = 0; }
        if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss() && entityIn.getEntityBoundingBox().intersects(state.getBoundingBox(worldIn, pos).offset(pos))) {
        	WorldServer world = entityIn.getServer().getWorld(id);
    		BlockPos coords = world.getSpawnCoordinate();
    		double x = 0, y = 70, z = 0;
    		if (coords == null) {
    			coords = world.getSpawnPoint();
    			if (!world.isAirBlock(coords)) {
    				coords = world.getTopSolidOrLiquidBlock(coords);
    			} else {
    				while (world.isAirBlock(coords) && coords.getY() > 0) {
    					coords = coords.down();
    				}
    				if (coords.getY() == 0) {
    					coords = world.getTopSolidOrLiquidBlock(coords);
    				}
    			}
    			x = coords.getX();
    			y = coords.getY();
    			z = coords.getZ();
    		}
        	if (entityIn instanceof EntityPlayerMP) {
        		NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) entityIn, x, y, z, id);
        	} else {
        		entityIn = AdditionalMethods.travelEntity(CustomNpcs.Server, entityIn, id);
        		entityIn.setPosition(x, y, z);
        	}
        }
    }

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) { return false; }

	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (this.nbtData.hasKey("Particle", 8)) {
	        double d0 = (double)((float)pos.getX() + rand.nextFloat());
	        double d1 = (double)((float)pos.getY() + 0.8F);
	        double d2 = (double)((float)pos.getZ() + rand.nextFloat());
	        EnumParticleTypes p = EnumParticleTypes.CRIT;
	        for (EnumParticleTypes ept : EnumParticleTypes.values()) {
	        	if (ept.name().equalsIgnoreCase(this.nbtData.getString("Particle"))) {
	        		p = ept;
	        		break;
	        	}
	        }
	        worldIn.spawnParticle(p, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
    }
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }

}
