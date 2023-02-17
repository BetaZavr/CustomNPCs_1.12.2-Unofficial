package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class ContainerNPCFollower extends ContainerNpcInterface {
	public InventoryNPC currencyMatrix;
	public RoleFollower role;

	public ContainerNPCFollower(EntityNPCInterface npc, EntityPlayer player) {
		super(player);
		this.role = (RoleFollower) npc.roleInterface;
		this.currencyMatrix = new InventoryNPC("currency", 1, this);
		this.addSlotToContainer(
				(Slot) new SlotNpcMercenaryCurrency(this.role, (IInventory) this.currencyMatrix, 0, 26, 9));
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j1, 8 + j1 * 18, 142));
		}
	}

	public void onContainerClosed(EntityPlayer entityplayer) {
		super.onContainerClosed(entityplayer);
		if (!entityplayer.world.isRemote) {
			ItemStack itemstack = this.currencyMatrix.removeStackFromSlot(0);
			if (!NoppesUtilServer.IsItemStackNull(itemstack) && !entityplayer.world.isRemote) {
				entityplayer.entityDropItem(itemstack, 0.0f);
			}
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}
}
