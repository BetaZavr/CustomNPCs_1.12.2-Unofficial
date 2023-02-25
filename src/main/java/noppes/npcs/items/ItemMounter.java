package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemMounter
extends Item
implements IPermission {
	
	public ItemMounter() {
		this.setRegistryName(CustomNpcs.MODID, "npcmounter");
		this.setUnlocalizedName("npcmounter");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SpawnRider || e == EnumPacketServer.PlayerRider || e == EnumPacketServer.CloneList;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.mounter").getFormattedText());
		list.add(new TextComponentTranslation("info.item.mounter.0").getFormattedText());
	}
	
}
