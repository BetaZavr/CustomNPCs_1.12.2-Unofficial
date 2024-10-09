package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerManageRecipes extends Container {

	public ContainerManageRecipes(EntityPlayer player) {
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot(player.inventory, l1 + i2 * 9 + 9, 8 + l1 * 18, 113 + i2 * 18));
			}
		}
		for (int j2 = 0; j2 < 9; ++j2) {
			this.addSlotToContainer(new Slot(player.inventory, j2, 8 + j2 * 18, 171));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}
