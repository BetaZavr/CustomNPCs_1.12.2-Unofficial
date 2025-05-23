package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.reflection.item.ItemFoodReflection;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CustomFood extends ItemFood implements ICustomElement {

	protected NBTTagCompound nbtData;

	public CustomFood(int amount, float saturation, boolean isWolfFood, NBTTagCompound nbtItem) {
		super(amount, saturation, isWolfFood);
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_" + nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_" + nbtItem.getString("RegistryName"));

		if (nbtItem.hasKey("UseDuration", 3)) {
			ItemFoodReflection.setItemUseDuration(this, nbtItem.getInteger("UseDuration"));
		}
		if (nbtItem.hasKey("PotionEffect", 10)) {
			NBTTagCompound potionEffect = nbtItem.getCompoundTag("PotionEffect");
			Potion potion = Potion.getPotionFromResourceLocation(potionEffect.getString("Potion"));
			if (potion != null) {
				PotionEffect effect = new PotionEffect(potion, potionEffect.getInteger("DurationTicks"),
						potionEffect.getInteger("Amplifier"), potionEffect.getBoolean("Ambient"),
						potionEffect.getBoolean("ShowParticles"));
				this.setPotionEffect(effect, potionEffect.getFloat("Probability"));
			}
		}
		if (nbtItem.hasKey("AlwaysEdible", 1) && nbtItem.getBoolean("AlwaysEdible")) {
			this.setAlwaysEdible();
		}
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) {
			this.setFull3D();
		}
		this.setCreativeTab(CustomRegisters.tabItems);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
		return this.itemUseDuration;
	}

	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabItems) { Util.instance.sort(items); }
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("ItemType", 1)) { return this.nbtData.getByte("ItemType"); }
		return 6;
	}

}
