package noppes.npcs.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import noppes.npcs.CustomNpcs;

public abstract class BlockInterface extends BlockContainer {

	protected BlockInterface(Material materialIn) {
		super(materialIn);
	}

	protected void setName(String name) {
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
	}

}
