package noppes.npcs.api.event;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
		
		public IItemStack[] currency;
		public IItemStack receiving;
		public IItemStack sold;

		public TradeFailedEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack sold, NonNullList<ItemStack> items) {
			super(player, npc);
			List<IItemStack> list = Lists.<IItemStack>newArrayList();
			for (ItemStack stack : items) {
				if (stack == null || stack.isEmpty()) { continue; }
				list.add(NpcAPI.Instance().getIItemStack(stack.copy()));
			}
			this.currency = list.toArray(new IItemStack[list.size()]);
			this.sold = NpcAPI.Instance().getIItemStack(sold.copy());
		}
	}

	@Cancelable
	public static class TraderEvent extends RoleEvent {
		
		public IItemStack[] currency;
		public IItemStack sold;

		public TraderEvent(EntityPlayer player, ICustomNpc<?> npc, ItemStack sold, NonNullList<ItemStack> items) {
			super(player, npc);
			List<IItemStack> list = Lists.<IItemStack>newArrayList();
			for (ItemStack stack : items) {
				if (stack == null || stack.isEmpty()) { continue; }
				list.add(NpcAPI.Instance().getIItemStack(stack.copy()));
			}
			this.currency = list.toArray(new IItemStack[list.size()]);
			this.sold = NpcAPI.Instance().getIItemStack(sold.copy());
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
