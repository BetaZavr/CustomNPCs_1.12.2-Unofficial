package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;

import javax.annotation.Nonnull;

public class TileEntityCustomBanner extends TileEntityBanner {

	public int factionId = -1;

	public ResourceLocation getFactionFlag() {
		Faction f = FactionController.instance.factions.get(factionId);
		return f == null ? null : f.flag;
	}

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("FactionID", 3)) {
			factionId = compound.getInteger("FactionID");
		}
	}

	@Override
	public void setItemValues(@Nonnull ItemStack stack, boolean bo) {
		super.setItemValues(stack, bo);
		factionId = -1;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey("BlockEntityTag", 10)
				&& nbt.getCompoundTag("BlockEntityTag").hasKey("FactionID", 3)) {
			factionId = nbt.getCompoundTag("BlockEntityTag").getInteger("FactionID");
		}
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("FactionID", factionId);
		return compound;
	}

}
