package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemDoor;

public class ItemNpcBlockDoor extends ItemDoor {

	public ItemNpcBlockDoor(Block block) {
		super(block);
		String name = block.getUnlocalizedName().substring(5);
		this.setRegistryName(name);
		this.setUnlocalizedName(name);
		this.setCreativeTab(null);
	}

}
