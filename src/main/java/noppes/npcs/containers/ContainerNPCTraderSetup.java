package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;

import javax.annotation.Nonnull;

public class ContainerNPCTraderSetup extends Container {

	public Deal deal;
	public Marcet marcet;

	public ContainerNPCTraderSetup(Marcet marcetIn, Deal dealIn, EntityPlayer player) {
		marcet = marcetIn;
		deal = (dealIn == null || (player instanceof EntityPlayerMP) ? dealIn : dealIn.copy());
		if (deal != null) {
			addSlotToContainer(new Slot(deal.getMCInventoryProduct(), 0, 26, 17)); // 215
			for (int v = 0; v < 3; ++v) {
				for (int u = 0; u < 3; ++u) { addSlotToContainer(new Slot(deal.getMCInventoryCurrency(), u + v * 3, 8 + u * 18, 54 + v * 18)); }
			}
		}
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				addSlotToContainer(new Slot(player.inventory, l1 + i2 * 9 + 9, 8 + l1 * 18, 135 + i2 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 193));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

	@SideOnly(Side.CLIENT)
	public void setSlotPos(int slotID, int[] newPos) {
		if (newPos == null || newPos.length < 2 || slotID < 0 || slotID > 9) { return; }
		inventorySlots.set(slotID, new Slot(deal.getMCInventoryProduct(), 0, newPos[0], newPos[1]));
	}

}
