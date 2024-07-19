package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemDoor;
import noppes.npcs.api.item.INPCToolItem;

public class ItemNpcBlockDoor extends ItemDoor implements INPCToolItem {

	public ItemNpcBlockDoor(Block block) {
		super(block);
		String name = block.getUnlocalizedName().substring(5);
		this.setRegistryName(name);
		this.setUnlocalizedName(name);
	}

}
