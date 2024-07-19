package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

import javax.annotation.Nonnull;

public class ContainerNPCTrader extends ContainerNpcInterface {

	public Marcet marcet;

	public ContainerNPCTrader(EntityNPCInterface npc, EntityPlayer player) {
		super(player);
		this.marcet = (Marcet) ((RoleTrader) npc.advanced.roleInterface).getMarket();
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot(player.inventory, l1 + i2 * 9 + 9, 32 + l1 * 18, 140 + i2 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, 32 + j1 * 18, 198));
		}
	}

	public ContainerNPCTrader(int marcetId, EntityPlayer player) {
		super(player);
		this.marcet = (Marcet) MarcetController.getInstance().getMarcet(marcetId);
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(new Slot(player.inventory, l1 + i2 * 9 + 9, 32 + l1 * 18, 140 + i2 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, 32 + j1 * 18, 198));
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (playerIn instanceof EntityPlayerMP) {
			this.marcet.removeListener(playerIn, true);
		}
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}
}
