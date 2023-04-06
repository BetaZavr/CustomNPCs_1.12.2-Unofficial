package noppes.npcs.containers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.gui.mainmenu.GuiDropEdit;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DropSet;

public class ContainerNPCDropSetup
extends Container {
	
	public DropSet inventoryDS;

	public ContainerNPCDropSetup(EntityNPCInterface npc, int pos, EntityPlayer player) {
		this.inventoryDS = null;
		if (npc.inventory.drops.containsKey(pos)) {
			this.inventoryDS = npc.inventory.drops.get(pos);
		}
		if (this.inventoryDS == null) { this.inventoryDS = new DropSet(npc.inventory); }
		this.addSlotToContainer(new Slot(this.inventoryDS, 0, 202, 135));
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l2 = 0; l2 < 9; ++l2) {
				this.addSlotToContainer(new Slot(player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 135 + i1 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(player.inventory, j1, j1 * 18 + 8, 193));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		GuiScreen cmg = Minecraft.getMinecraft().currentScreen;
		if (slotId == 0 && cmg instanceof GuiDropEdit) {
			((GuiDropEdit) cmg).reset();
		}
		return super.slotClick(slotId, dragType, clickType, player);
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}
}
