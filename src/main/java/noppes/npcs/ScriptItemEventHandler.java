package noppes.npcs;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import noppes.npcs.items.ItemScripted;

public class ScriptItemEventHandler {

	@SubscribeEvent
	public void npcItemPickup(EntityItemPickupEvent event) {
		if (event.getEntityPlayer().world.isRemote) { return; }
		CustomNpcs.debugData.start(null);
		EntityItem entity = event.getItem();
		ItemStack stack = entity.getItem();
		if (!stack.isEmpty() && (stack.getItem() == CustomRegisters.scripted_item)) {
			EventHooks.onScriptItemPickedUp(ItemScripted.GetWrapper(stack), event.getEntityPlayer(), entity);
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityItem)) { return; }
		CustomNpcs.debugData.start(null);
		EntityItem entity = (EntityItem) event.getEntity();
		ItemStack stack = entity.getItem();
		if (!stack.isEmpty() && (stack.getItem() == CustomRegisters.scripted_item) && EventHooks.onScriptItemSpawn(ItemScripted.GetWrapper(stack), entity)) {
			event.setCanceled(true);
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcItemToss(ItemTossEvent event) {
		if (event.getPlayer().world.isRemote) { return; }
		CustomNpcs.debugData.start(null);
		EntityItem entity = event.getEntityItem();
		ItemStack stack = entity.getItem();
		if (!stack.isEmpty() && (stack.getItem() == CustomRegisters.scripted_item) && EventHooks.onScriptItemTossed(ItemScripted.GetWrapper(stack), event.getPlayer(), entity)) {
			event.setCanceled(true);
		}
		CustomNpcs.debugData.end(null);
	}
}
