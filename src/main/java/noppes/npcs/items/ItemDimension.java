package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;

public class ItemDimension
extends Item {

	public ItemDimension() {
		this.setRegistryName(CustomNpcs.MODID, "npcdimension");
		this.setUnlocalizedName("npcdimension");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(Character.toChars(0x00A7)[0]+"cWIP");
	}
	
}