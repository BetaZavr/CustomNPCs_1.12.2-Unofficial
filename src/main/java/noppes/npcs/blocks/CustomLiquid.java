package noppes.npcs.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;

public class CustomLiquid extends BlockFluidClassic implements ICustomElement {

	public NBTTagCompound nbtData = new NBTTagCompound();

	public CustomLiquid(Fluid fluid, Material material, NBTTagCompound nbtBlock) {
		super(fluid, material);
		this.nbtData = nbtBlock;
		String name = "custom_fluid_" + nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return (int) this.nbtData.getByte("BlockType"); }
		return 1;
	}

}
