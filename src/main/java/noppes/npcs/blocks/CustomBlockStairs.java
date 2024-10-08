package noppes.npcs.blocks;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CustomBlockStairs extends BlockStairs implements ICustomElement {

	public NBTTagCompound nbtData;

	public CustomBlockStairs(NBTTagCompound nbtBlock) {
		super(Blocks.COBBLESTONE.getDefaultState());
		this.nbtData = nbtBlock;
		String name = "custom_" + nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());

		this.enableStats = true;
		this.blockSoundType = SoundType.STONE;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		this.setHardness(0.0f);
		this.setResistance(10.0f);

		if (nbtBlock.hasKey("Hardness", 5)) {
			this.setHardness(nbtBlock.getFloat("Hardness"));
		}
		if (nbtBlock.hasKey("Resistance", 5)) {
			this.setResistance(nbtBlock.getFloat("Resistance"));
		}
		if (nbtBlock.hasKey("LightLevel", 5)) {
			this.setLightLevel(nbtBlock.getFloat("LightLevel"));
		}

		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	@Override
	public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabBlocks && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative")) {
			return;
		}
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabBlocks) { Util.instance.sort(items); }
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return this.nbtData.getByte("BlockType"); }
		return 3;
	}

}
