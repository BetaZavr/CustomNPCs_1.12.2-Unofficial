package noppes.npcs.containers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;

import javax.annotation.Nonnull;

public class ContainerNpcQuestRewardItem extends Container {

	public NpcMiscInventory inv;

    public ContainerNpcQuestRewardItem(int questId) {
        Quest quest = (Quest) QuestController.instance.get(questId);
		List<ItemStack> its = new ArrayList<>();
		for (ItemStack item : quest.rewardItems.items) {
			if (item.isEmpty()) {
				continue;
			}
			its.add(item.copy());
		}
		this.inv = new NpcMiscInventory(its.size());
		for (ItemStack item : its) {
			this.inv.addItemStack(item);
		}

		int slotNow = 0, x = -10 + (9 * 9) - its.size() * 9;
		for (int u = 0; u < 3 && slotNow < its.size(); ++u) {
			for (int v = 0; v < 3 && slotNow < its.size(); ++v) {
				this.addSlotToContainer(new Slot(this.inv, slotNow++, x + slotNow * 18, 20));
			}
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public @Nonnull ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
		return ItemStack.EMPTY;
	}

	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		return ItemStack.EMPTY;
	}

}