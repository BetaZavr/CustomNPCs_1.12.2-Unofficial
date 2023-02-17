package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.item.ICustomItem;
import noppes.npcs.util.ObfuscationHelper;

public class CustomFood
extends ItemFood
implements ICustomItem {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	
	public CustomFood(int amount, float saturation, boolean isWolfFood, NBTTagCompound nbtItem) {
		super(amount, saturation, isWolfFood);
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_"+nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_"+nbtItem.getString("RegistryName"));
		
		if (nbtItem.hasKey("UseDuration", 3)) { ObfuscationHelper.setValue(ItemFood.class, this, nbtItem.getInteger("UseDuration"), 0); }
		if (nbtItem.hasKey("PotionEffect", 10)) {
			NBTTagCompound potionEffect = nbtItem.getCompoundTag("PotionEffect");
			Potion potion = Potion.getPotionFromResourceLocation(potionEffect.getString("Potion"));
			if (potion!=null) {
				PotionEffect effect = new PotionEffect(potion,
						potionEffect.getInteger("DurationTicks"),
						potionEffect.getInteger("Amplifier"),
						potionEffect.getBoolean("Ambient"),
						potionEffect.getBoolean("ShowParticles"));
				this.setPotionEffect(effect, potionEffect.getFloat("Probability"));
			}
		}
		if (nbtItem.hasKey("AlwaysEdible", 1) && nbtItem.getBoolean("AlwaysEdible")) { this.setAlwaysEdible(); }
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) { this.setFull3D(); }
		this.setCreativeTab((CreativeTabs) CustomItems.tabItems);
	}
	
	public int getMaxItemUseDuration(ItemStack stack) {
        return ObfuscationHelper.getValue(ItemFood.class, this, 0);
    }

	@Override
	public NBTTagCompound getData() { return this.nbtData; }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
