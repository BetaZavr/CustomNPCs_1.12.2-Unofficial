package noppes.npcs.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import noppes.npcs.CustomNpcs;

public class CustomFluid extends Fluid {

	private int mapColor;

	public NBTTagCompound nbtData = new NBTTagCompound();

	public CustomFluid(NBTTagCompound nbtBlock) {
		super("custom_fluid_" + nbtBlock.getString("RegistryName"),
				new ResourceLocation(CustomNpcs.MODID,
						"fluids/custom_fluid_" + nbtBlock.getString("RegistryName") + "_still"),
				new ResourceLocation(CustomNpcs.MODID,
						"fluids/custom_fluid_" + nbtBlock.getString("RegistryName") + "_flow"),
				new ResourceLocation(CustomNpcs.MODID,
						"fluids/custom_fluid_" + nbtBlock.getString("RegistryName") + "_overlay"));
		this.nbtData = nbtBlock;
		this.mapColor = nbtBlock.hasKey("Color", 3) ? nbtBlock.getInteger("Color") : 0xFFFFFFFF;
		this.setDensity(nbtBlock.hasKey("Density", 3) ? nbtBlock.getInteger("Density") : 1100);
		this.setGaseous(nbtBlock.hasKey("IsGaseous", 1) ? nbtBlock.getBoolean("IsGaseous") : false);
		this.setLuminosity(nbtBlock.hasKey("Luminosity", 3) ? nbtBlock.getInteger("Luminosity") : 5);
		this.setViscosity(nbtBlock.hasKey("Viscosity", 3) ? nbtBlock.getInteger("Viscosity") : 900);
		this.setTemperature(nbtBlock.hasKey("Temperature", 3) ? nbtBlock.getInteger("Temperature") : 300);
		this.setUnlocalizedName("custom_fluid_" + nbtBlock.getString("RegistryName"));
	}

	@Override
	public int getColor() {
		return this.mapColor;
	}

	public Fluid setColor(int parColor) {
		this.mapColor = parColor;
		return this;
	}

}
