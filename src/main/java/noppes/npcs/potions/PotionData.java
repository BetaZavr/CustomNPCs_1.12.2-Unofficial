package noppes.npcs.potions;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;

public class PotionData {

	public Potion potion;
	public PotionType potionType;
	public NBTTagCompound nbtData;

	public PotionData(Potion potion, PotionType potionType, NBTTagCompound nbtPotion) {
		this.potion = potion;
		this.potionType = potionType;
		this.nbtData = nbtPotion;
	}

}
