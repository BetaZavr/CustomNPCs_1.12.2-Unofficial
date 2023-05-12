package noppes.npcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.oredict.OreDictionary;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.QuestEvent.QuestTurnedInEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerBankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;

public class NoppesUtilPlayer {

	private static Map<Object, Long> delaySendMap = new HashMap<Object, Long>();

	public static void bankUnlock(EntityPlayerMP player, EntityNPCInterface npc) {
		if (npc.advanced.roleInterface.getEnumType() != EnumNpcRole.BANK) {
			return;
		}
		Container con = player.openContainer;
		if (con == null || !(con instanceof ContainerNPCBankInterface)) {
			return;
		}
		ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
		Bank bank = BankController.getInstance().getBank(container.bankid);
		ItemStack item = bank.currencyInventory.getStackInSlot(container.slot);
		if (item == null || item.isEmpty()) {
			return;
		}
		int price = item.getCount();
		ItemStack currency = container.currencyMatrix.getStackInSlot(0);
		if (currency == null || currency.isEmpty() || price > currency.getCount()) {
			return;
		}
		if (currency.getCount() - price == 0) {
			container.currencyMatrix.setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			currency = currency.splitStack(price);
		}
		player.closeContainer();
		PlayerBankData data = PlayerDataController.instance.getBankData(player, bank.id);
		BankData bankData = data.getBank(bank.id);
		if (bankData.unlockedSlots + 1 <= bank.maxSlots) {
			BankData bankData2 = bankData;
			++bankData2.unlockedSlots;
		}
		RoleEvent.BankUnlockedEvent event = new RoleEvent.BankUnlockedEvent(player, npc.wrappedNPC, container.slot);
		EventHooks.onNPCRole(npc, event);
		bankData.openBankGui(player, npc, bank.id, container.slot);
	}

	public static void bankUpgrade(EntityPlayerMP player, EntityNPCInterface npc) {
		if (npc.advanced.roleInterface.getEnumType() != EnumNpcRole.BANK) {
			return;
		}
		Container con = player.openContainer;
		if (con == null || !(con instanceof ContainerNPCBankInterface)) {
			return;
		}
		ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
		Bank bank = BankController.getInstance().getBank(container.bankid);
		ItemStack item = bank.upgradeInventory.getStackInSlot(container.slot);
		if (item == null || item.isEmpty()) {
			return;
		}
		int price = item.getCount();
		ItemStack currency = container.currencyMatrix.getStackInSlot(0);
		if (currency == null || currency.isEmpty() || price > currency.getCount()) {
			return;
		}
		if (currency.getCount() - price == 0) {
			container.currencyMatrix.setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			currency = currency.splitStack(price);
		}
		player.closeContainer();
		PlayerBankData data = PlayerDataController.instance.getBankData(player, bank.id);
		BankData bankData = data.getBank(bank.id);
		bankData.upgradedSlots.put(container.slot, true);
		RoleEvent.BankUpgradedEvent event = new RoleEvent.BankUpgradedEvent(player, npc.wrappedNPC, container.slot);
		EventHooks.onNPCRole(npc, event);
		bankData.openBankGui(player, npc, bank.id, container.slot);
	}

	public static void changeFollowerState(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) {
			return;
		}
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		EntityPlayer owner = role.owner;
		if (owner == null || !owner.getName().equals(player.getName())) {
			return;
		}
		role.isFollowing = !role.isFollowing;
	}

	public static void closeDialog(EntityPlayerMP player, EntityNPCInterface npc, boolean notifyClient) {
		PlayerData data = PlayerData.get(player);
		Dialog dialog = DialogController.instance.dialogs.get(data.dialogId);
		EventHooks.onNPCDialogClose(npc, player, dialog);
		if (notifyClient) {
			Server.sendData(player, EnumPacketClient.GUI_CLOSE, -1, new NBTTagCompound());
		}
		data.dialogId = -1;
	}

	private static boolean compareItemDetails(ItemStack item, ItemStack item2, boolean ignoreDamage,
			boolean ignoreNBT) {
		return item.getItem() == item2.getItem()
				&& (ignoreDamage || item.getItemDamage() == -1 || item.getItemDamage() == item2.getItemDamage())
				&& (ignoreNBT || item.getTagCompound() == null
						|| (item2.getTagCompound() != null && item.getTagCompound().equals(item2.getTagCompound())))
				&& (ignoreNBT || item2.getTagCompound() == null || item.getTagCompound() != null);
	}

	public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		int size = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(is) && compareItems(item, is, ignoreDamage, ignoreNBT)) {
				size += is.getCount();
			}
		}
		return size >= item.getCount();
	}

	// New
	public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT, int amount) {
		int size = 0;
		if (player==null) { return false; }
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(is) && compareItems(item, is, ignoreDamage, ignoreNBT)) {
				size += is.getCount();
			}
		}
		return size >= amount;
	}

	public static boolean compareItems(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT) {
		if (NoppesUtilServer.IsItemStackNull(item) || NoppesUtilServer.IsItemStackNull(item2)) {
			return false;
		}
		OreDictionary.itemMatches(item, item2, false);
		int[] ids = OreDictionary.getOreIDs(item);
		if (ids.length > 0) {
			for (int id : ids) {
				boolean match1 = false;
				boolean match2 = false;
				for (ItemStack is : OreDictionary.getOres(OreDictionary.getOreName(id))) {
					if (compareItemDetails(item, is, ignoreDamage, ignoreNBT)) {
						match1 = true;
					}
					if (compareItemDetails(item2, is, ignoreDamage, ignoreNBT)) {
						match2 = true;
					}
				}
				if (match1 && match2) {
					return true;
				}
			}
		}
		return compareItemDetails(item, item2, ignoreDamage, ignoreNBT);
	}

	public static void consumeItem(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		if (NoppesUtilServer.IsItemStackNull(item)) {
			return;
		}
		int size = item.getCount();
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(is)) {
				if (compareItems(item, is, ignoreDamage, ignoreNBT)) {
					if (size < is.getCount()) {
						is.splitStack(size);
						break;
					}
					size -= is.getCount();
					player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				}
			}
		}
	}

	public static List<ItemStack> countStacks(IInventory inv, boolean ignoreDamage, boolean ignoreNBT) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack item = inv.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(item)) {
				boolean found = false;
				for (ItemStack is : list) {
					if (compareItems(item, is, ignoreDamage, ignoreNBT)) {
						is.setCount(is.getCount() + item.getCount());
						found = true;
						break;
					}
				}
				if (!found) {
					list.add(item.copy());
				}
			}
		}
		return list;
	}

	public static void dialogSelected(int diaId, int optionId, EntityPlayerMP player, EntityNPCInterface npc) {
		PlayerData data = PlayerData.get(player);
		if (data.dialogId != diaId) {
			return;
		}
		if (data.dialogId < 0 && npc.advanced.roleInterface instanceof RoleDialog) {
			String text = ((RoleDialog) npc.advanced.roleInterface).optionsTexts.get(optionId);
			if (text != null && !text.isEmpty()) {
				Dialog d = new Dialog(null);
				d.text = text;
				NoppesUtilServer.openDialog(player, npc, d);
			}
			return;
		}
		Dialog dialog = DialogController.instance.dialogs.get(data.dialogId);
		if (dialog == null) {
			return;
		}
		if (!dialog.hasDialogs(player) && !dialog.hasOtherOptions()) {
			closeDialog(player, npc, true);
			return;
		}
		DialogOption option = dialog.options.get(optionId);
		if (option == null || EventHooks.onNPCDialogOption(npc, player, dialog, option)
				|| (option.optionType == 1 && (!option.isAvailable(player) || !option.hasDialog()))
				|| option.optionType == 2 || option.optionType == 0) {
			closeDialog(player, npc, true);
			return;
		}
		if (option.optionType == 3) {
			closeDialog(player, npc, true);
			if (npc.advanced.roleInterface != null) {
				if (npc.advanced.roleInterface instanceof RoleCompanion) {
					((RoleCompanion) npc.advanced.roleInterface).interact(player, true);
				} else {
					npc.advanced.roleInterface.interact(player);
				}
			}
		} else if (option.optionType == 1) {
			closeDialog(player, npc, false);
			NoppesUtilServer.openDialog(player, npc, option.getDialog());
		} else if (option.optionType == 4) {
			closeDialog(player, npc, true);
			NoppesUtilServer.runCommand(npc, npc.getName(), option.command, player);
		} else {
			closeDialog(player, npc, true);
		}
	}

	public static void extendFollower(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) { return; }
		Container con = player.openContainer;
		if (con == null || !(con instanceof ContainerNPCFollower)) {
			return;
		}
		ContainerNPCFollower container = (ContainerNPCFollower) con;
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		followerBuy(role, (IInventory) container.currencyMatrix, player, npc);
	}

	private static void followerBuy(RoleFollower role, IInventory currencyInv, EntityPlayerMP player,
			EntityNPCInterface npc) {
		ItemStack currency = currencyInv.getStackInSlot(0);
		if (currency == null || currency.isEmpty()) {
			return;
		}
		HashMap<ItemStack, Integer> cd = new HashMap<ItemStack, Integer>();
		for (int slot = 0; slot < role.inventory.items.size(); ++slot) {
			ItemStack is = role.inventory.items.get(slot);
			if (!is.isEmpty() && is.getItem() == currency.getItem()) {
				if (!is.getHasSubtypes() || is.getItemDamage() == currency.getItemDamage()) {
					int days = 1;
					if (role.rates.containsKey(slot)) {
						days = role.rates.get(slot);
					}
					cd.put(is, days);
				}
			}
		}
		if (cd.size() == 0) {
			return;
		}
		int stackSize = currency.getCount();
		int days2 = 0;
		int possibleDays = 0;
		int possibleSize = stackSize;
		while (true) {
			for (ItemStack item : cd.keySet()) {
				int rDays = cd.get(item);
				int rValue = item.getCount();
				if (rValue > stackSize) {
					continue;
				}
				int newStackSize = stackSize % rValue;
				int size = stackSize - newStackSize;
				int posDays = size / rValue * rDays;
				if (possibleDays > posDays) {
					continue;
				}
				possibleDays = posDays;
				possibleSize = newStackSize;
			}
			if (stackSize == possibleSize) {
				break;
			}
			stackSize = possibleSize;
			days2 += possibleDays;
			possibleDays = 0;
		}
		RoleEvent.FollowerHireEvent event = new RoleEvent.FollowerHireEvent(player, npc.wrappedNPC, days2);
		if (EventHooks.onNPCRole(npc, event)) {
			return;
		}
		if (event.days == 0) {
			return;
		}
		if (stackSize <= 0) {
			currencyInv.setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			currencyInv.setInventorySlotContents(0, currency.splitStack(stackSize));
		}
		npc.say(player,
				new Line(NoppesStringUtils.formatText(role.dialogHire.replace("{days}", days2 + ""), player, npc)));
		role.setOwner(player);
		role.addDays(days2);
	}

	public static void hireFollower(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) { return; }
		Container con = player.openContainer;
		if (con == null || !(con instanceof ContainerNPCFollowerHire)) {
			return;
		}
		ContainerNPCFollowerHire container = (ContainerNPCFollowerHire) con;
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		followerBuy(role, (IInventory) container.currencyMatrix, player, npc);
	}
	
	/* Вначале на клиент кидается пакет с текстом при завершении,
	 * потом с клиента приходит ответ, что всё норм
	 * и вызывается этот метод:
	 */
	public static void questCompletion(EntityPlayerMP player, int questId) {
		NoppesUtilPlayer.questCompletion(player, questId, ItemStack.EMPTY);
	}
	
	public static void questCompletion(EntityPlayerMP player, int questId, ItemStack stack) {
		
		
		PlayerData data = PlayerData.get(player);
		PlayerQuestData playerdata = data.questData;
		QuestData activeData = playerdata.activeQuests.get(questId);
		if (activeData == null) {
			return;
		}
		Quest quest = activeData.quest;
		if (!quest.questInterface.isCompleted(player) && !activeData.isCompleted) {
			return;
		}
		QuestTurnedInEvent event = new QuestTurnedInEvent(data.scriptData.getPlayer(), (IQuest) quest);
		event.expReward = quest.rewardExp;
		List<IItemStack> rewardList = new ArrayList<IItemStack>();
		for (ItemStack item : quest.rewardItems.items) {
			if (item!=null && !item.isEmpty()) {
				rewardList.add(NpcAPI.Instance().getIItemStack(item));
			}
		}
		if (!rewardList.isEmpty()) {
			switch(quest.rewardType) {
				case RANDOM: {
					event.itemRewards = new IItemStack[] { rewardList.get(player.getRNG().nextInt(rewardList.size())) };
					break;
				}
				case ONE: {
					if (stack==null) { stack = ItemStack.EMPTY; }
					event.itemRewards = new IItemStack[] { NpcAPI.Instance().getIItemStack(stack) };
					break;
				}
				default: { // ALL
					event.itemRewards = new IItemStack[rewardList.size()];
					int i = 0;
					for (IItemStack item : rewardList) {
						event.itemRewards[i] = item;
						i++;
					}
				}
			}
		}
		event.factionOptions = quest.factionOptions;
		EventHooks.onQuestTurnedIn(data.scriptData, event);
		
		quest.questInterface.handleComplete(player); // отнять предметы по задачам квеста
		// выдать награды:
		if (event.expReward > 0) {
			NoppesUtilServer.playSound(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f,
					0.5f * ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7f + 1.8f));
			player.addExperience(event.expReward);
		}
		
		event.factionOptions.addPoints(player);
		
		if (event.mail.isValid()) {
			PlayerDataController.instance.addPlayerMessage(player.getServer(), player.getName(), event.mail);
		}
		
		if (!event.command.isEmpty()) {
			FakePlayer cplayer = EntityNPCInterface.CommandPlayer;
			cplayer.setWorld(player.world);
			cplayer.setPosition(player.posX, player.posY, player.posZ);
			NoppesUtilServer.runCommand(cplayer, "QuestCompletion", event.command, player);
		}
		
		if (event.itemRewards.length>0) {
			for (IItemStack stackRew : event.itemRewards) {
				NoppesUtilServer.GivePlayerItem(player, player, stackRew.getMCItemStack());
			}
		}
		
		PlayerQuestController.setQuestFinished(quest, player);
		
		Quest nextQuest = (QuestController.instance == null) ? null : QuestController.instance.quests.get(event.nextQuestId);
		if (nextQuest!=null) {
			PlayerQuestController.addActiveQuest(nextQuest, player);
		}
	}

	public static void sendData(EnumPlayerPacket enu, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!Server.fillBuffer((ByteBuf) buffer, enu, obs)) {
				return;
			}
			CustomNpcs.ChannelPlayer.sendToServer(new FMLProxyPacket(buffer, "CustomNPCsPlayer"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendDataCheakDelay(EnumPlayerPacket enu, Object key, long time, Object... obs) {
		if (NoppesUtilPlayer.delaySendMap.containsKey(key)
				&& NoppesUtilPlayer.delaySendMap.get(key) > System.currentTimeMillis()) {
			return;
		}
		List<Object> del = new ArrayList<Object>();
		for (Object k : NoppesUtilPlayer.delaySendMap.keySet()) {
			if (NoppesUtilPlayer.delaySendMap.get(k) <= System.currentTimeMillis()) {
				del.add(k);
			}
		}
		for (Object k : del) {
			NoppesUtilPlayer.delaySendMap.remove(k);
		}
		NoppesUtilPlayer.delaySendMap.put(key, time + System.currentTimeMillis());
		NoppesUtilPlayer.sendData(enu, obs);
	}

	public static void teleportPlayer(EntityPlayerMP player, double x, double y, double z, int dimension) {
		if (player.dimension != dimension) {
			MinecraftServer server = player.getServer();
			WorldServer wor = server.getWorld(dimension);
			if (wor == null) {
				player.sendMessage(new TextComponentString("Broken transporter. Dimenion does not exist"));
				return;
			}
			player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
			server.getPlayerList().transferPlayerToDimension(player, dimension, (Teleporter) new CustomTeleporter(wor));
			player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
			if (!wor.playerEntities.contains(player)) {
				wor.spawnEntity(player);
			}
		} else {
			player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
		}
		player.world.updateEntityWithOptionalForce(player, false);
	}

}
