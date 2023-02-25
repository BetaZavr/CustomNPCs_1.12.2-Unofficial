package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;

public class ItemNpcInterface extends Item {
	private boolean damageAble;

	public ItemNpcInterface() {
		this.damageAble = true;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}

	public ItemNpcInterface(int par1) {
		this();
	}

	public boolean consumeItem(EntityPlayer player, Item item) {
		ItemStack itemstack = this.getItemStack(player, item);
		if (itemstack == null) {
			return false;
		}
		itemstack.shrink(1);
		if (itemstack.getCount() == 0) {
			player.inventory.deleteStack(itemstack);
		}
		return true;
	}

	public int getItemEnchantability() {
		return super.getItemEnchantability();
	}

	private ItemStack getItemStack(EntityPlayer player, Item item) {
		if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() == item) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		}
		if (player.getHeldItem(EnumHand.MAIN_HAND) != null
				&& player.getHeldItem(EnumHand.MAIN_HAND).getItem() == item) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		}
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack itemstack = player.inventory.getStackInSlot(i);
			if (itemstack != null && itemstack.getItem() == item) {
				return itemstack;
			}
		}
		return null;
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		subItems.add(new ItemStack((Item) this, 1, 0));
	}

	public boolean hasItem(EntityPlayer player, Item item) {
		return this.getItemStack(player, item) != null;
	}

	public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving,
			EntityLivingBase par3EntityLiving) {
		if (par2EntityLiving.getHealth() <= 0.0f) {
			return false;
		}
		if (this.damageAble) {
			par1ItemStack.damageItem(1, par3EntityLiving);
		}
		return true;
	}

	public void playSound(EntityLivingBase entity, SoundEvent sound, float volume, float pitch) {
		entity.world.playSound((EntityPlayer) null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.NEUTRAL,
				volume, pitch);
	}

	public void setUnDamageable() {
		this.damageAble = false;
	}

	public Item setUnlocalizedName(String name) {
		super.setUnlocalizedName(name);
		this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, name));
		return this;
	}
	
}
