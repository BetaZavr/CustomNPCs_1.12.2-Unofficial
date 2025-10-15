package noppes.npcs.api.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.EventName;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.ITransportLocation;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

public class RoleEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.ROLE)
	public static class BankUnlockedEvent extends RoleEvent {
		public int slot;

		public BankUnlockedEvent(EntityPlayer player, ICustomNpc<?> npc, int slotIn) {
			super(player, npc);
			slot = slotIn;
		}
	}

	@EventName(EnumScriptType.ROLE)
	public static class BankUpgradedEvent extends RoleEvent {
		public int slot;

		public BankUpgradedEvent(EntityPlayer player, ICustomNpc<?> npc, int slotIn) {
			super(player, npc);
			slot = slotIn;
		}
	}

	@EventName(EnumScriptType.ROLE)
	public static class FollowerFinishedEvent extends RoleEvent {
		public FollowerFinishedEvent(EntityPlayer player, ICustomNpc<?> npc) {
			super(player, npc);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ROLE)
	public static class FollowerHireEvent extends RoleEvent {
		public int days;

		public FollowerHireEvent(EntityPlayer player, ICustomNpc<?> npc, int daysIn) {
			super(player, npc);
			days = daysIn;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ROLE)
	public static class MailmanEvent extends RoleEvent {
		public IPlayerMail mail;

		public MailmanEvent(EntityPlayer player, ICustomNpc<?> npc, IPlayerMail mailIn) {
			super(player, npc);
			mail = mailIn;
		}
	}

	@EventName(EnumScriptType.ROLE)
	public static class TradeFailedEvent extends RoleEvent {

		public Map<IItemStack, Integer> currency;
		public IItemStack sold;

		public TradeFailedEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack soldIn, Map<ItemStack, Integer> items) {
			super(player, npc);
			currency = new LinkedHashMap<>();
			for (ItemStack stack : items.keySet()) {
				if (stack == null || stack.isEmpty()) { continue; }
				currency.put(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack), items.get(stack));
			}
			sold = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(soldIn.copy());
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ROLE)
	public static class TraderEvent extends RoleEvent {

		public Map<IItemStack, Integer> currency;
		public IItemStack sold;

		public TraderEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack soldIn, Map<ItemStack, Integer> items) {
			super(player, npc);
			currency = new LinkedHashMap<>();
			for (ItemStack stack : items.keySet()) {
				if (stack == null || stack.isEmpty()) { continue; }
				currency.put(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack), items.get(stack));
			}
			sold = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(soldIn);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ROLE)
	public static class TransporterUnlockedEvent extends RoleEvent {
		public TransporterUnlockedEvent(EntityPlayer player, ICustomNpc<?> npc) {
			super(player, npc);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.ROLE)
	public static class TransporterUseEvent extends RoleEvent {
		public ITransportLocation location;

		public TransporterUseEvent(EntityPlayer player, ICustomNpc<?> npc, ITransportLocation locationIn) {
			super(player, npc);
			location = locationIn;
		}
	}

	public ICustomNpc<?> npc;
	public IPlayer<?> player;

	public RoleEvent(EntityPlayer playerIn, ICustomNpc<?> npcIn) {
		npc = npcIn;
		player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(playerIn);
	}

}
