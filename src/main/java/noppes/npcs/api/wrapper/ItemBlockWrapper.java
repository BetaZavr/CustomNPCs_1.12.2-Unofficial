package noppes.npcs.api.wrapper;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.item.IItemBlock;

public class ItemBlockWrapper extends ItemStackWrapper implements IItemBlock {

	protected String blockName;

	protected ItemBlockWrapper(ItemStack item) {
		super(item);
		this.blockName = Block.REGISTRY.getNameForObject(Block.getBlockFromItem(item.getItem())) + "";
	}

	@Override
	public String getBlockName() {
		return this.blockName;
	}

	@Override
	public int getType() {
		return ItemType.BLOCK.get();
	}

}
