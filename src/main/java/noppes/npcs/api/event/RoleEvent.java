package noppes.npcs.api.event;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.ITransportLocation;
import noppes.npcs.api.item.IItemStack;

public class RoleEvent extends CustomNPCsEvent {
	
	public static class BankUnlockedEvent extends RoleEvent {
		public int slot;

		public BankUnlockedEvent(EntityPlayer player, ICustomNpc<?> npc, int slot) {
			super(player, npc);
			this.slot = slot;
		}
	}

	public static class BankUpgradedEvent extends RoleEvent {
		public int slot;

		public BankUpgradedEvent(EntityPlayer player, ICustomNpc<?> npc, int slot) {
			super(player, npc);
			this.slot = slot;
		}
	}

	public static class FollowerFinishedEvent extends RoleEvent {
		public FollowerFinishedEvent(EntityPlayer player, ICustomNpc<?> npc) {
			super(player, npc);
		}
	}

	@Cancelable
	public static class FollowerHireEvent extends RoleEvent {
		public int days;

		public FollowerHireEvent(EntityPlayer player, ICustomNpc<?> npc, int days) {
			super(player, npc);
			this.days = days;
		}
	}

	@Cancelable
	public static class MailmanEvent extends RoleEvent {
		public IPlayerMail mail;

		public MailmanEvent(EntityPlayer player, ICustomNpc<?> npc, IPlayerMail mail) {
			super(player, npc);
			this.mail = mail;
		}
	}

	public static class TradeFailedEvent extends RoleEvent {

		public Map<IItemStack, Integer> currency;
		public IItemStack sold;

		public TradeFailedEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack sold, Map<ItemStack, Integer> items) {
			super(player, npc);
			this.currency = Maps.<IItemStack, Integer>newLinkedHashMap();
			for (ItemStack stack : items.keySet()) {
				if (stack == null || stack.isEmpty()) { continue; }
				this.currency.put(NpcAPI.Instance().getIItemStack(stack), items.get(stack));
			}
			this.sold = NpcAPI.Instance().getIItemStack(sold.copy());
		}
	}

	@Cancelable
	public static class TraderEvent extends RoleEvent {
		
		public Map<IItemStack, Integer> currency;
		public IItemStack sold;

		public TraderEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack sold, Map<ItemStack, Integer> items) {
			super(player, npc);
			this.currency = Maps.<IItemStack, Integer>newLinkedHashMap();
			for (ItemStack stack : items.keySet()) {
				if (stack == null || stack.isEmpty()) { continue; }
				this.currency.put(NpcAPI.Instance().getIItemStack(stack), items.get(stack));
			}
			this.sold = NpcAPI.Instance().getIItemStack(sold);
		}
	}

	@Cancelable
	public static class TransporterUnlockedEvent extends RoleEvent {
		public TransporterUnlockedEvent(EntityPlayer player, ICustomNpc<?> npc) {
			super(player, npc);
		}
	}

	@Cancelable
	public static class TransporterUseEvent extends RoleEvent {
		public ITransportLocation location;

		public TransporterUseEvent(EntityPlayer player, ICustomNpc<?> npc, ITransportLocation location) {
			super(player, npc);
			this.location = location;
		}
	}

	public ICustomNpc<?> npc;

	public IPlayer<?> player;

	public RoleEvent(EntityPlayer player, ICustomNpc<?> npc) {
		this.npc = npc;
		this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
	}
	
}
