package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemMounter extends Item implements IPermission {
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
}
