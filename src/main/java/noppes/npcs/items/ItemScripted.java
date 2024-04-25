package noppes.npcs.items;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemScripted extends Item implements IPermission {

	public static Map<Integer, String> Resources = new HashMap<Integer, String>();

	public static ItemScriptedWrapper GetWrapper(ItemStack stack) {
		return (ItemScriptedWrapper) NpcAPI.Instance().getIItemStack(stack);
	}

	public ItemScripted() {
		this.setRegistryName(CustomNpcs.MODID, "scripted_item");
		this.setUnlocalizedName("scripted_item");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
		this.setHasSubtypes(true);
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if (istack instanceof ItemScriptedWrapper) {
			return 1.0 - ((ItemScriptedWrapper) istack).durabilityValue;
		}
		return super.getDurabilityForDisplay(stack);
	}

	public int getItemStackLimit(ItemStack stack) {
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if (istack instanceof ItemScriptedWrapper) {
			return ((ItemScriptedWrapper) istack).getMaxStackSize();
		}
		return super.getItemStackLimit(stack);
	}

	public int getRGBDurabilityForDisplay(ItemStack stack) {
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if (!(istack instanceof ItemScriptedWrapper)) {
			return super.getRGBDurabilityForDisplay(stack);
		}
		int color = ((ItemScriptedWrapper) istack).durabilityColor;
		if (color >= 0) {
			return color;
		}
		return MathHelper.hsvToRGB((float) (Math.max(0.0f, (1.0 - this.getDurabilityForDisplay(stack))) / 3.0f), 1.0f,
				1.0f);
	}

	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		return true;
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.ScriptItemDataGet || e == EnumPacketServer.ScriptItemDataSave;
	}

	public boolean showDurabilityBar(ItemStack stack) {
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if (istack instanceof ItemScriptedWrapper) {
			return ((ItemScriptedWrapper) istack).durabilityShow;
		}
		return super.showDurabilityBar(stack);
	}

}
