package noppes.npcs.particles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;

import java.util.Objects;

public class CustomParticleSettings implements ICustomElement {

	public NBTTagCompound nbtData;
	public int id, argumentCount = 0;
	public boolean shouldIgnoreRange = false;
	public String enumName;
	public String name;

	public CustomParticleSettings(NBTTagCompound nbtParticle, int id) {
		this.nbtData = nbtParticle;
		this.id = id;
		this.enumName = nbtParticle.getString("RegistryName").toUpperCase();
		while (this.enumName.contains(" ")) {
			this.enumName = this.enumName.replace(" ", "_");
		}
		nbtParticle.setString("RegistryName", this.enumName);
		this.name = this.enumName.toLowerCase();
		this.enumName = "CUSTOM_" + this.enumName;
		if (nbtParticle.hasKey("ShouldIgnoreRange", 1)) {
			this.shouldIgnoreRange = nbtParticle.getBoolean("ShouldIgnoreRange");
		}
		if (nbtParticle.hasKey("ArgumentCount", 3)) {
			this.argumentCount = nbtParticle.getInteger("ArgumentCount");
		}
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName").toLowerCase();
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	@Override
	public int getType() { return this.nbtData.hasKey("OBJModel", 8) && !this.nbtData.getString("OBJModel").isEmpty() ? 1 : 0; }

}
