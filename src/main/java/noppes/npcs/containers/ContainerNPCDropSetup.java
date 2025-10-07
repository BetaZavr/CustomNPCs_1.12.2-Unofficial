package noppes.npcs.containers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DropSet;

import javax.annotation.Nonnull;

public class ContainerNPCDropSetup extends Container {

	public final DropSet inventoryDS;
	public final int dataType;

	public int dropType;
	public int groupId;
	public int marcetID;
	public int dealID;

	public ContainerNPCDropSetup(EntityPlayer player, NBTTagCompound compound) {
		DropSet inv = null;
		dataType = compound.getInteger("InventoryType");
		if (dataType == 0) {
			EntityNPCInterface npc = NoppesUtil.getLastNpc();
			if (npc == null && compound.hasKey("EntityId", 3)) {
				Entity e = player.world.getEntityByID(compound.getInteger("EntityId"));
				if (e instanceof EntityNPCInterface) { npc = (EntityNPCInterface) e; }
			}
			if (npc != null) {
				dropType = compound.getInteger("DropType");
				groupId = compound.getInteger("GroupId");
				int pos = compound.getInteger("Pos");
				if (dropType == 1) {
					DropsTemplate template = DropController.getInstance().templates.get(npc.inventory.saveDropsName);
					if (template != null && template.groups.containsKey(groupId)
							&& template.groups.get(groupId).containsKey(pos)) {
						inv = template.groups.get(groupId).get(pos);
					}
				} else {
					if (npc.inventory.drops.containsKey(pos)) { inv = npc.inventory.drops.get(pos); }
				}
			}
		}
		else if (dataType == 1) {
			marcetID = compound.getInteger("Marcet");
			dealID = compound.getInteger("Deal");
			Deal deal = MarcetController.getInstance().deals.get(dealID);
			if (deal != null) {
				int pos = compound.getInteger("DropSet");
				ICustomDrop[] drops = deal.getCaseItems();
				if (pos >= 0 && pos < drops.length) { inv = (DropSet) drops[pos]; }
				if (inv == null) {
					inv = new DropSet(null, deal);
					inv.pos = pos;
				}
			}
		} // Marcet deal
		inventoryDS = inv;
		if (inventoryDS != null) {
			addSlotToContainer(new Slot(inventoryDS, 0, 202, 135));
		}
		for (int i1 = 0; i1 < 3; ++i1) {
			for (int l2 = 0; l2 < 9; ++l2) { addSlotToContainer(new Slot(player.inventory, l2 + i1 * 9 + 9, l2 * 18 + 8, 135 + i1 * 18)); }
		}
		for (int j1 = 0; j1 < 9; ++j1) { addSlotToContainer(new Slot(player.inventory, j1, j1 * 18 + 8, 193)); }
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}
