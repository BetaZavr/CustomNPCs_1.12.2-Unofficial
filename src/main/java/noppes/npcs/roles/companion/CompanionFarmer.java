package noppes.npcs.roles.companion;

import net.minecraft.nbt.NBTTagCompound;

public class CompanionFarmer extends CompanionJobInterface {
	public boolean isStanding;

	public CompanionFarmer() {
		this.isStanding = false;
	}

	@Override
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("CompanionFarmerStanding", this.isStanding);
		return compound;
	}

	@Override
	public boolean isSelfSufficient() {
		return this.isStanding;
	}

    @Override
	public void setNBT(NBTTagCompound compound) {
		this.isStanding = compound.getBoolean("CompanionFarmerStanding");
	}
}
