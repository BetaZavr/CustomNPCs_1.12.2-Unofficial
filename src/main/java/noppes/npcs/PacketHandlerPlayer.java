package noppes.npcs;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.QuestEvent.QuestExtraButtonEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.ISpecBuilder;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.constants.*;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.PlayerSkinController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.controllers.data.PlayerGameData.FollowerSet;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataScript;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.*;

public class PacketHandlerPlayer {

	private static List<EnumPlayerPacket> list;

	static {
		PacketHandlerPlayer.list = new ArrayList<>();
		PacketHandlerPlayer.list.add(EnumPlayerPacket.NpcVisualData);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.IsMoved);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.KeyPressed);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.LeftClick);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.MousesPressed);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.StopNPCAnimation);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.GetTileData);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.GetFilePart);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.MovingPathGet);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.MarketTime);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.NpcData);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.PlaySound);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.StopSound);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.MiniMapData);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.GetBuildData);
		PacketHandlerPlayer.list.add(EnumPlayerPacket.OpenGui);
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		ByteBuf buffer = event.getPacket().payload();
		Objects.requireNonNull(player.getServer()).addScheduledTask(() -> {
			EnumPlayerPacket type = null;
			try {
				type = EnumPlayerPacket.values()[buffer.readInt()];
				CustomNpcs.debugData.start(type.toString());
				if (!PacketHandlerPlayer.list.contains(type)) {
					LogWriter.debug("Received: " + type);
				}
				player(buffer, player, type);
				CustomNpcs.debugData.end(type.toString());
			} catch (Exception e) {
				LogWriter.error("Error with EnumPlayerPacket." + type, e);
			} finally {
				buffer.release();
			}
		});
	}

	@SuppressWarnings("all")
	private void player(ByteBuf buffer, EntityPlayerMP player, EnumPlayerPacket type) throws Exception {
		PlayerData data = PlayerData.get(player);
		if (type == EnumPlayerPacket.MarkData) {
			Entity entity = Objects.requireNonNull(player.getServer()).getEntityFromUuid(Server.readUUID(buffer));
			if (!(entity instanceof EntityLivingBase)) {
				return;
			}
			MarkData.get((EntityLivingBase) entity);
		} else if (type == EnumPlayerPacket.KeyPressed) {
			PlayerOverlayHUD hud = data.hud;
			int key = buffer.readInt();
			if (key < 0) {
				for (int k : hud.keyPress) {
					EventHooks.onPlayerKeyPressed(player, k, false, false, false, false, false);
				}
				hud.keyPress.clear();
				return;
			}
			boolean isDown = buffer.readBoolean();
			if (isDown) {
				hud.keyPress.add(key);
			} else {
				if (hud.hasOrKeysPressed(key)) {
					hud.keyPress.remove((Integer) key);
				}
			}
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				return;
			}
			EventHooks.onPlayerKeyPressed(player, key, isDown, buffer.readBoolean(), buffer.readBoolean(),
					buffer.readBoolean(), buffer.readBoolean());

		} else if (type == EnumPlayerPacket.MousesPressed) {
			PlayerOverlayHUD hud = data.hud;
			int key = buffer.readInt();
			if (key < 0) {
				hud.mousePress.clear();
				return;
			}
			boolean isDown = buffer.readBoolean();
			if (isDown) { hud.mousePress.add(key); }
			else if (hud.hasMousePress(key)) { hud.mousePress.remove((Integer) key); }
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				return;
			}
			EventHooks.onPlayerMousePressed(player, key, isDown, buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
		} else if (type == EnumPlayerPacket.LeftClick) {
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				return;
			}
			ItemStack item = player.getHeldItemMainhand();
			PlayerScriptData handler = data.scriptData;
			PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 0, null);
			EventHooks.onPlayerAttack(handler, ev);
			if (item.getItem() == CustomRegisters.scripted_item) {
				ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
				ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 0, null);
				EventHooks.onScriptItemAttack(isw, eve);
			}
		} else if (type == EnumPlayerPacket.CustomGuiClose) {
			ICustomGui gui = new CustomGuiWrapper(player).fromNBT(Server.readNBT(buffer));
			EventHooks.onCustomGuiClose((PlayerWrapper<EntityPlayerMP>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player), gui);
		} else if (type == EnumPlayerPacket.CustomGuiButton) {
			if (player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiButton((PlayerWrapper<EntityPlayerMP>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player),
						((ContainerCustomGui) player.openContainer).customGui, buffer.readInt());
			}
		} else if (type == EnumPlayerPacket.CustomGuiScrollClick) {
			if (player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiScrollClick((PlayerWrapper<EntityPlayerMP>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player),
						((ContainerCustomGui) player.openContainer).customGui, buffer.readInt(), buffer.readInt(),
						CustomGuiController.readScrollSelection(buffer), buffer.readBoolean());
			}
		} else if (type == EnumPlayerPacket.CloseGui) {
			player.closeContainer();
		} else if (type == EnumPlayerPacket.CompanionTalentExp) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleCompanion) || player != npc.getOwner()) {
				return;
			}
			int id = buffer.readInt();
			int exp = buffer.readInt();
			RoleCompanion role = (RoleCompanion) npc.advanced.roleInterface;
			if (exp <= 0 || !role.canAddExp(-exp) || id < 0 || id >= EnumCompanionTalent.values().length) {
				return;
			}
			EnumCompanionTalent talent = EnumCompanionTalent.values()[id];
			role.addExp(-exp);
			role.addTalentExp(talent, exp);
		} else if (type == EnumPlayerPacket.CompanionOpenInv) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleCompanion) || player != npc.getOwner()) {
				return;
			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionInv, npc);
		} else if (type == EnumPlayerPacket.FollowerHire) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != RoleType.FOLLOWER) { return; }
			NoppesUtilPlayer.hireFollower(player, npc, buffer.readInt());
		} else if (type == EnumPlayerPacket.FollowerExtend) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != RoleType.FOLLOWER) {
				return;
			}
			NoppesUtilPlayer.extendFollower(player, npc, buffer.readInt());
			Server.sendData(player, EnumPacketClient.GUI_DATA,
					npc.advanced.roleInterface.save(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.FollowerState) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != RoleType.FOLLOWER) { return; }
			int t = buffer.readInt();
			if (t == 0) {
				NoppesUtilPlayer.changeFollowerState(player, npc);
				Server.sendData(player, EnumPacketClient.GUI_DATA,
						npc.advanced.roleInterface.save(new NBTTagCompound()));
			} else if (t == 1) {
				RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
				RoleEvent.FollowerFinishedEvent event = new RoleEvent.FollowerFinishedEvent(role.owner, npc.wrappedNPC);
				EventHooks.onNPCRole(npc, event);
				npc.say(role.owner, new Line(new TextComponentTranslation(NoppesStringUtils.formatText(role.dialogFired, role.owner, npc)).getFormattedText()));
				if (data != null) {
					FollowerSet fs = data.game.getFollower(role.npc);
					if (fs != null) {
						data.game.removeFollower(role.npc);
					}
				}
				role.killed();
			}
		} else if (type == EnumPlayerPacket.RoleGet) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				return;
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA,
					npc.advanced.roleInterface.save(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.Transport) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleTransporter)) {
				return;
			}
			((RoleTransporter) npc.advanced.roleInterface).transport(player, buffer.readInt());
		} else if (type == EnumPlayerPacket.BankUpgrade) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				return;
			}
			EntityNPCInterface npc;
			Entity e = player.world.getEntityByID(buffer.readInt());
			if (e instanceof EntityNPCInterface) {
				npc = (EntityNPCInterface) e;
				NoppesUtilServer.setEditingNpc(player, npc);
			}
			else { npc = NoppesUtilServer.getEditingNpc(player); }
			if (npc == null || npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				return;
			}
			NoppesUtilPlayer.bankUpgrade(player, npc, buffer.readBoolean(), buffer.readInt());
		} else if (type == EnumPlayerPacket.BankRegrade) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				return;
			}
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				Entity e = player.world.getEntityByID(buffer.readInt());
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
					NoppesUtilServer.setEditingNpc(player, npc);
				}
			}
			if (!player.capabilities.isCreativeMode || npc == null
					|| npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				return;
			}
			NoppesUtilPlayer.bankRegrade(player, npc);
		} else if (type == EnumPlayerPacket.BankUnlock) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				Entity e = player.world.getEntityByID(buffer.readInt());
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
					NoppesUtilServer.setEditingNpc(player, npc);
				}
			}
			if (!(player.openContainer instanceof ContainerNPCBank) || npc == null || npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
				return;
			}
			NoppesUtilPlayer.bankUnlock(player, npc, buffer.readBoolean());
		} else if (type == EnumPlayerPacket.BankLock) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				return;
			}
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				Entity e = player.world.getEntityByID(buffer.readInt());
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
					NoppesUtilServer.setEditingNpc(player, npc);
				}
			}
			if (!player.capabilities.isCreativeMode || npc == null
					|| npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				return;
			}
			NoppesUtilPlayer.bankLock(player, npc);
		} else if (type == EnumPlayerPacket.BankClearCeil) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				return;
			}
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				Entity e = player.world.getEntityByID(buffer.readInt());
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
					NoppesUtilServer.setEditingNpc(player, npc);
				}
			}
			if (!player.capabilities.isCreativeMode || npc == null
					|| npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				return;
			}
			NoppesUtilPlayer.bankClearCeil(player, npc);
		} else if (type == EnumPlayerPacket.BankResetCeil) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				return;
			}
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if (npc == null) {
				Entity e = player.world.getEntityByID(buffer.readInt());
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
					NoppesUtilServer.setEditingNpc(player, npc);
				}
			}
			if (!player.capabilities.isCreativeMode || npc == null
					|| npc.advanced.roleInterface.getEnumType() != RoleType.BANK) {
				return;
			}
			NoppesUtilPlayer.bankResetCeil(player, npc);
		} else if (type == EnumPlayerPacket.Dialog) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			int dialogId = buffer.readInt();
			int optionId = buffer.readInt();
			data.dialogData.option(dialogId, optionId);
			if (npc == null) { return; }
			NoppesUtilPlayer.dialogSelected(dialogId, optionId, player, npc);
		} else if (type == EnumPlayerPacket.CheckQuestCompletion) {
			int id = buffer.readInt();
			PlayerQuestData playerdata = PlayerData.get(player).questData;
			for (QuestData qd : playerdata.activeQuests.values()) {
				if (id > 0 && qd.quest.id != id) {
					continue;
				}
				playerdata.checkQuestCompletion(player, qd);
			}
		} else if (type == EnumPlayerPacket.QuestCompletion) {
			NoppesUtilPlayer.questCompletion(player, buffer.readInt());
		} else if (type == EnumPlayerPacket.QuestCompletionReward) {
			int id = buffer.readInt();
			ItemStack stack = new ItemStack(Server.readNBT(buffer));
			NoppesUtilPlayer.questCompletion(player, id, stack);
		} else if (type == EnumPlayerPacket.FactionsGet) {
			PlayerFactionData data2 = data.factionData;
			Server.sendData(player, EnumPacketClient.GUI_DATA, data2.getPlayerGuiData());
		} else if (type == EnumPlayerPacket.MailGet) {
			PlayerMailData data3 = data.mailData;
			Server.sendData(player, EnumPacketClient.GUI_DATA, data3.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailReturn) {
			long id = buffer.readLong();
			PlayerMail mail = data.mailData.get(id);
			if (mail == null) {
				return;
			}
			PlayerData plData = PlayerDataController.instance.getDataFromUsername(Objects.requireNonNull(player.world.getMinecraftServer()),
					mail.sender);
			if (plData == null) {
				if (!mail.sender.isEmpty()) {
					player.sendMessage(new TextComponentTranslation("mailbox.error.return.player", mail.sender));
				}
			} else {
				mail.sender += new TextComponentTranslation("mailbox.returned").getFormattedText();
				mail.returned = true;
				mail.ransom = 0;
				mail.beenRead = false;
				plData.mailData.addMail(mail);
				plData.save(false);
			}
			Iterator<PlayerMail> it = data.mailData.playerMails.iterator();
			while (it.hasNext()) {
				PlayerMail m = it.next();
				if (m.timeWhenReceived == id && m.sender.equals(mail.sender)) {
					it.remove();
					break;
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, data.mailData.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailDelete) {
			long time = buffer.readLong();
			PlayerMailData mailData = data.mailData;
			if (time < 0) { // All letters
				List<PlayerMail> del = Lists.newArrayList();
				long serverTime = System.currentTimeMillis();
				for (PlayerMail mail : mailData.playerMails) {
					if (serverTime - mail.timeWhenReceived - mail.timeWillCome < 0L) {
						continue;
					}
					del.add(mail);
				}
				for (PlayerMail mail : del) {
					mailData.playerMails.remove(mail);
				}
				NBTTagCompound compound = new NBTTagCompound();
				mailData.saveNBTData(compound);
				Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData, compound);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
				return;
			} else if (time == 0) { // All read letters
				List<PlayerMail> del = Lists.newArrayList();
				for (PlayerMail mail : mailData.playerMails) {
					if (!mail.beenRead) {
						continue;
					}
					del.add(mail);
				}
				for (PlayerMail mail : del) {
					mailData.playerMails.remove(mail);
				}
				NBTTagCompound compound = new NBTTagCompound();
				mailData.saveNBTData(compound);
				Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData, compound);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
				return;
			}
			String username = Server.readString(buffer);
			Iterator<PlayerMail> it = mailData.playerMails.iterator();
			while (it.hasNext()) {
				PlayerMail mail = it.next();
				if (mail.timeWhenReceived == time && mail.sender.equals(username)) {
					it.remove();
					break;
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, mailData.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailSend) {
			String username = Server.readString(buffer);
			boolean isCreative = player.capabilities.isCreativeMode;
            assert username != null;
            if (username.equalsIgnoreCase(player.getName()) && !(CustomNpcs.MailSendToYourself || isCreative)) {
				NoppesUtilServer.sendGuiError(player, 2);
				return;
			}
			if (PlayerDataController.instance.hasPlayer(username).isEmpty()) {
				NoppesUtilServer.sendGuiError(player, 0);
				return;
			}
			long cost = buffer.readLong();
			if (!isCreative && cost > data.game.getMoney()) {
				NoppesUtilServer.sendGuiError(player, 3);
				return;
			}
			PlayerMail mail = new PlayerMail();
			String s = player.getDisplayNameString();
			if (!s.equals(player.getName())) {
				s = s + "(" + player.getName() + ")";
			}
			mail.readNBT(Server.readNBT(buffer));
			if (mail.title.isEmpty()) {
				NoppesUtilServer.sendGuiError(player, 1);
				return;
			}
			mail.sender = s;
			mail.items = ((ContainerMail) player.openContainer).mail.items;
			((ContainerMail) player.openContainer).sendMail = true;
			NBTTagCompound comp = new NBTTagCompound();
			comp.setString("username", username);
			NoppesUtilServer.sendGuiClose(player, 1, comp);
			EntityNPCInterface npc2 = NoppesUtilServer.getEditingNpc(player);
			if (npc2 != null && EventHooks.onNPCRole(npc2, new RoleEvent.MailmanEvent(player, npc2.wrappedNPC, mail))) {
				return;
			}
			PlayerDataController.instance.addPlayerMessage(player.getServer(), username, mail);
		} else if (type == EnumPlayerPacket.MailboxOpenMail) {
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			int x = buffer.readInt();
			int y = buffer.readInt();
			player.closeContainer();
			if (x == 1 && y == 1) {
				player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailOpen.ordinal(), player.world, 1, 1, 0);
				return;
			}
			PlayerMailData data4 = data.mailData;
			for (PlayerMail mail : data4.playerMails) {
				if (mail.timeWhenReceived == time && mail.sender.equals(username)) {
					ContainerMail.staticmail = mail;
					player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailOpen.ordinal(), player.world, 0, 0, 0);
					break;
				}
			}
		} else if (type == EnumPlayerPacket.MailRead) {
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			PlayerMailData data4 = data.mailData;
			for (PlayerMail mail : data4.playerMails) {
				if (!mail.beenRead && mail.timeWhenReceived == time && mail.sender.equals(username)) {
					if (mail.hasQuest()) {
						PlayerQuestController.addActiveQuest(mail.getQuest(), player, false);
					}
					mail.beenRead = true;
				}
			}
		}
		else if (type == EnumPlayerPacket.QuestRemoveActive) {
			int id = buffer.readInt();
			IQuest quest = QuestController.instance.get(id);
			if (quest == null) {
				return;
			}
			boolean bo = EventHooks.onQuestCanceled(data.scriptData, (Quest) quest);
			if (!bo && PlayerQuestController.getRemoveActiveQuest(player, id)) {
				player.sendMessage(new TextComponentTranslation("quest.removequest", quest.getTitle()));
				Server.sendData(player, EnumPacketClient.GUI_DATA, new NBTTagCompound());
			} else {
				player.sendMessage(new TextComponentTranslation("quest.removequest.not", quest.getTitle()));
			}
		} else if (type == EnumPlayerPacket.QuestChooseReward) {
			int id = buffer.readInt();
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.QuestRewardItem, null, id, 0, 0);
		} else if (type == EnumPlayerPacket.NpcVisualData) {
			int entityID = buffer.readInt();
			Entity entity = player.world.getEntityByID(entityID);
			if (entity instanceof EntityNPCInterface) {
				NBTTagCompound compound = new NBTTagCompound();
				EntityNPCInterface npcData = (EntityNPCInterface) entity;
				compound.setInteger("NPCLevel", npcData.stats.getLevel());
				compound.setInteger("NPCRarity", npcData.stats.getRarity());
				compound.setString("RarityTitle", npcData.stats.getRarityTitle());
				Server.sendData(player, EnumPacketClient.NPC_VISUAL_DATA, entityID, compound);
			}
		} else if (type == EnumPlayerPacket.IsMoved) {
			data.hud.isMoved = buffer.readBoolean();
		} else if (type == EnumPlayerPacket.WindowSize) {
			PlayerOverlayHUD hud = data.hud;
			NBTTagCompound compound = Server.readNBT(buffer);
			hud.setWindowSize(compound.getTagList("WindowSize", 6));
		}
		else if (type == EnumPlayerPacket.TraderMarketBuy) {
			int marcetID = buffer.readInt();
			Marcet marcet = MarcetController.getInstance().getMarcet(marcetID);
			if (marcet == null || marcet.notHasListener(player)) {
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
				return;
			}
			int dealID = buffer.readInt();
			Deal deal = marcet.getDeal(dealID);
			if (deal == null || deal.getType() == 1) {
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
				return;
			}
			int npcID = buffer.readInt();
			int count = buffer.readInt();
			DealMarkup dm = MarcetController.getInstance().getBuyData(marcet, deal, data.game.getMarcetLevel(marcet.getId()), count);
			boolean notGiveItem = !deal.availability.isAvailable(player) ||
					dm.buyMoney < data.game.getMoney() || // has money
					dm.buyDonat < data.game.getDonat() || // has donal
					!Util.instance.canRemoveItems(player.inventory.mainInventory, dm.buyItems, dm.ignoreDamage, dm.ignoreNBT); // has items
			if (marcet.isLimited) {
				boolean notBuy = false;
				Map<ItemStack, Integer> mainItem = new LinkedHashMap<>();
				mainItem.put(dm.main, dm.count);
				if (!deal.isCase() && !Util.instance.canRemoveItems(marcet.inventory, mainItem, dm.ignoreDamage, dm.ignoreNBT)) { notBuy = true; }
				if (notBuy) {
					marcet.detectAndSendChanges();
					Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
					Server.sendData(player, EnumPacketClient.GUI_UPDATE);
					player.sendMessage(new TextComponentTranslation("marcet.message.not.deal"));
					return;
				}
				marcet.money += dm.sellMoney;
				if (!deal.isCase()) { marcet.removeInventoryItems(mainItem); }
			}
			EntityNPCInterface npc = null;
			Entity entity = player.world.getEntityByID(npcID);
			if (entity instanceof EntityNPCInterface) { npc = (EntityNPCInterface) entity; }
			if (!player.isCreative()) {
				if (notGiveItem) {
					if (npc != null) { EventHooks.onNPCRole(npc, new RoleEvent.TradeFailedEvent(player, npc.wrappedNPC, dm.main, dm.buyItems)); }
					Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
					Server.sendData(player, EnumPacketClient.GUI_UPDATE);
					return;
				}
				data.game.addMoney(-1 * dm.buyMoney); // remove money
				data.game.addDonat(-1 * dm.buyDonat); // remove donat
				// Del Items
				for (ItemStack st : dm.buyItems.keySet()) { Util.instance.removeItem(player, st, dm.buyItems.get(st), dm.ignoreDamage, dm.ignoreNBT); }
			}
			boolean bo;
			if (deal.isCase()) {
				List<ItemStack> addedTo = new ArrayList<>();
				double baseChance = 1.0d;
				IAttributeInstance l = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
				if (l != null) {
					double luck = l.getAttributeValue();
					if (luck != 0.0d) {
						if (luck < 0) {
							luck *= -1;
							baseChance -= luck * luck * -0.005555d + luck * 0.255555d; // 1lv = 25%$ 10lv = 200%
						}
						else { baseChance += luck * luck * -0.005555d + luck * 0.255555d; } // 1lv = 25%$ 10lv = 200%
					}
				}
				for (int i = 0; i < count; i++) { addedTo.addAll(deal.createCaseItems(baseChance)); }
				for (ItemStack stack : addedTo) { spawnItem(player, stack); }
				bo = !addedTo.isEmpty();
				if (bo) { Server.sendData(player, EnumPacketClient.GUI_OPEN_CASE, dealID, addedTo); }
			}
			else {
				bo = true;
				ItemStack stack = dm.main.copy();
				if (dm.count > dm.main.getMaxStackSize()) {
					while (dm.count > 0) {
						stack = dm.main.copy();
						stack.setCount(Math.min(dm.count, dm.main.getMaxStackSize()));
						dm.count -= dm.main.getMaxStackSize();
						spawnItem(player, stack);
					}
				}
				else {
					stack.setCount(dm.count);
					spawnItem(player, stack);
				}
			}
			if (bo) {
				if (deal.getMaxCount() != 0) { deal.setAmount(deal.getAmount() - dm.count); }
				if (CustomNpcs.SendMarcetInfo) { player.sendMessage(new TextComponentTranslation("mes.market.buy", dm.main.getDisplayName() + " x" + dm.count)); }
				NoppesUtilServer.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
				Util.instance.updatePlayerInventory(player);
				data.game.addMarkupXP(marcet.getId(), 5);
				if (npc != null) { EventHooks.onNPCRole(npc, new RoleEvent.TraderEvent(player, npc.wrappedNPC, dm.main, dm.buyItems)); }
				marcet.detectAndSendChanges();
			}
			else if (npc != null) { EventHooks.onNPCRole(npc, new RoleEvent.TradeFailedEvent(player, npc.wrappedNPC, dm.main, dm.buyItems)); }
			Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		}
		else if (type == EnumPlayerPacket.TraderMarketSell) {
			int marcetID = buffer.readInt();
			Marcet marcet = MarcetController.getInstance().getMarcet(marcetID);
			if (marcet == null || marcet.notHasListener(player)) {
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
				return;
			}
			int dealID = buffer.readInt();
			Deal deal = marcet.getDeal(dealID);
			if (deal == null || deal.getType() == 0 ||
					((deal.isCase() && deal.getCaseItems().length == 0) || (!deal.isCase() && deal.getProduct().isEmpty()))) {
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
				return;
			}
			int npcID = buffer.readInt();
			int count = buffer.readInt();
			DealMarkup dm = MarcetController.getInstance().getBuyData(marcet, deal, data.game.getMarcetLevel(marcet.getId()), count);
			Entity entity = player.world.getEntityByID(npcID);
			EntityNPCInterface npc = null;
			if (entity instanceof EntityNPCInterface) { npc = (EntityNPCInterface) entity; }
			if (marcet.isLimited) {
				boolean notSell = marcet.money < dm.sellMoney;
				if (!notSell && !dm.sellItems.isEmpty() &&
						!Util.instance.canRemoveItems(marcet.inventory, dm.sellItems, dm.ignoreDamage, dm.ignoreNBT)) { notSell = true; }
				if (notSell) {
					marcet.detectAndSendChanges();
					Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
					Server.sendData(player, EnumPacketClient.GUI_UPDATE);
					player.sendMessage(new TextComponentTranslation("marcet.message.not.deal"));
					return;
				}
				marcet.money -= dm.sellMoney;
				Map<ItemStack, Integer> mainItem = new LinkedHashMap<>();
				mainItem.put(dm.main, dm.count);
				marcet.addInventoryItems(mainItem);
				marcet.removeInventoryItems(dm.sellItems);
			}
			if (player.isCreative() || Util.instance.removeItem(player, dm.main, dm.ignoreDamage, dm.ignoreNBT)) {
				// Add Items
				if (!dm.sellItems.isEmpty()) {
					boolean change = false;
					for (ItemStack st : dm.sellItems.keySet()) {
						int c = dm.sellItems.get(st);
						while (c > 0) {
							ItemStack stc = st.copy();
							stc.setCount(Math.min(c, st.getMaxStackSize()));
							c -= st.getMaxStackSize();
							if (player.inventory.addItemStackToInventory(stc)) { change = true; }
						}
					}
					if (change) {
						NoppesUtilServer.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						Util.instance.updatePlayerInventory(player);
					}
				}
				// Add Money
				if (dm.sellMoney > 0) {
					data.game.addMoney(dm.sellMoney);
					marcet.money -= dm.sellMoney;
				}
				if (deal.getMaxCount() != 0) { deal.setAmount(deal.getAmount() + dm.count); }
				if (CustomNpcs.SendMarcetInfo) { player.sendMessage(new TextComponentTranslation("mes.market.sell", dm.main.getDisplayName() + " x" + dm.count)); }
				data.game.addMarkupXP(marcet.getId(), 1);
				if (npc != null) { EventHooks.onNPCRole(npc, new RoleEvent.TraderEvent(player, npc.wrappedNPC, dm.main, dm.sellItems)); }
				marcet.detectAndSendChanges();
			}
			else if (npc != null) { EventHooks.onNPCRole(npc, new RoleEvent.TradeFailedEvent(player, npc.wrappedNPC, dm.main, dm.sellItems)); }
			Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		}
		else if (type == EnumPlayerPacket.TraderMarketReset) {
			Marcet m = (Marcet) MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.updateNew();
			}
		} else if (type == EnumPlayerPacket.TraderLivePlayer) {
			Marcet m = (Marcet) MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.removeListener(player, true);
			}
		} else if (type == EnumPlayerPacket.MailTakeMoney) {
			long id = buffer.readLong();
			PlayerMail mail = data.mailData.get(id);
			if (mail == null || mail.money <= 0) {
				return;
			}
			data.game.addMoney(mail.money);
			mail.money = 0;
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData,
					data.mailData.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailRansom) {
			long id = buffer.readLong();
			PlayerMail mail = data.mailData.get(id);
			if (mail != null && mail.ransom <= 0) {
				return;
			}
			if (!player.capabilities.isCreativeMode) {
                assert mail != null;
                if (data.game.getMoney() < mail.ransom) {
                    NoppesUtilServer.sendGuiError(player, 3);
                    return;
                }
            }
            assert mail != null;
            data.game.addMoney(mail.ransom * -1L);
			mail.ransom = 0;
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData, data.mailData.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.GetBuildData) {
			ItemStack stack = player.getHeldItemMainhand();
			BuilderData builder = null;
			if (stack.getItem() instanceof ISpecBuilder) {
				builder = ItemBuilder.getBuilder(player.getHeldItemMainhand(), player);
			} else {
				int id = buffer.readInt();
				if (id >= 0) { builder = SyncController.dataBuilder.get(id); }
			}
			if (builder != null) { Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.BuilderData, builder.getNbt()); }
		} else if (type == EnumPlayerPacket.HudTimerEnd) {
			int id = buffer.readInt();
			int orientationType = buffer.readInt();
			EventHooks.onPlayerTimer(data, id);
			data.hud.removeComponent(orientationType, id);
			data.hud.update();
		} else if (type == EnumPlayerPacket.TrackQuest) {
			data.hud.questID = buffer.readInt();
		} else if (type == EnumPlayerPacket.SaveCompassData) {
			data.hud.compassData.load(Server.readNBT(buffer));
		} else if (type == EnumPlayerPacket.GetTileData) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TileEntity tile = player.world.getTileEntity(new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z")));
			if (tile != null) {
				tile.writeToNBT(compound);
				Server.sendData(player, EnumPacketClient.SET_TILE_DATA, compound);
			}
		} else if (type == EnumPlayerPacket.ScriptPackage) {
			EventHooks.onScriptPackage(player, Server.readNBT(buffer));
		} else if (type == EnumPlayerPacket.MovingPathGet) {
			int id = buffer.readInt();
			Entity entity = player.world.getEntityByID(id);
			if (entity instanceof EntityCustomNpc) {
				Server.sendData(player, EnumPacketClient.NPC_MOVING_PATH, id,
						((EntityCustomNpc) entity).ais.writeToNBT(new NBTTagCompound()));
			}
		} else if (type == EnumPlayerPacket.KeyActive) {
			EventHooks.onPlayerKeyActive(player, buffer.readInt());
		} else if (type == EnumPlayerPacket.StopNPCAnimation) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) entity).animation.stopAnimation();
				EventHooks.onNPCStopAnimation((EntityNPCInterface) entity, buffer.readInt(), buffer.readInt());
			} else if (entity instanceof EntityPlayer) {
				DataAnimation animation = PlayerData.get((EntityPlayer) entity).animation;
				if (animation != null) {
					animation.stopAnimation();
				}
			}
		} else if (type == EnumPlayerPacket.OpenGui) {
			EventHooks.onPlayerOpenGui(player, Server.readString(buffer), Server.readString(buffer));
		} else if (type == EnumPlayerPacket.GetFilePart) {
			int part = buffer.readInt();
			String name = Server.readString(buffer);
			if (!CommonProxy.downloadableFiles.containsKey(name)) {
				Server.sendData(player, EnumPacketClient.SEND_FILE_PART, true, name);
				return;
			}
			TempFile file = CommonProxy.downloadableFiles.get(name);
			Server.sendData(player, EnumPacketClient.SEND_FILE_PART, false, part, name,
					String.valueOf(file.data.get(part)));
		} else if (type == EnumPlayerPacket.GetSyncData) {
			SyncController.syncPlayer(player);
		} else if (type == EnumPlayerPacket.TransportCategoriesGet) {
			NoppesUtilServer.sendTransportData(player);
		} else if (type == EnumPlayerPacket.CustomGuiKeyPressed) {
			IPlayer<?> pl = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player);
			if (pl.getCustomGui() == null || ((CustomGuiWrapper) pl.getCustomGui()).getScriptHandler() == null) {
				return;
			}
			EventHooks.onEvent(((CustomGuiWrapper) pl.getCustomGui()).getScriptHandler(), EnumScriptType.KEY_GUI_UP, new CustomGuiEvent.KeyPressedEvent(pl, pl.getCustomGui(), buffer.readInt()));
		} else if (type == EnumPlayerPacket.MarketTime) {
			MarcetController.getInstance().sendTo(player, buffer.readInt());
		} else if (type == EnumPlayerPacket.OpenCeilBank) {
			int bankId = buffer.readInt();
			BankData bd = data.bankData.get(bankId);
			if (bd != null) {
				EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
				int ceilId = buffer.readInt();
				NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
			}
		} else if (type == EnumPlayerPacket.NpcData) {
			int id = buffer.readInt();
			Entity e = player.world.getEntityByID(id);
			if (e instanceof EntityNPCInterface) {
				NBTTagCompound compound = new NBTTagCompound();
				e.writeToNBT(compound);
				compound.setInteger("EntityID", id);
				Server.sendData(player, EnumPacketClient.NPC_DATA, compound);
			}
		} else if (type == EnumPlayerPacket.PlaySound) {
			if (player == null) {
				return;
			}
			EventHooks.onPlayerPlaySound(PlayerData.get(player).scriptData,
					new PlayerEvent.PlayerSound((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player),
							Server.readString(buffer), Server.readString(buffer), Server.readString(buffer),
							buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
							buffer.readFloat()));
		} else if (type == EnumPlayerPacket.StopSound) {
			if (player == null) {
				return;
			}
			EventHooks.onPlayerStopSound(data.scriptData,
					new PlayerEvent.PlayerSound((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player),
							Server.readString(buffer), Server.readString(buffer), Server.readString(buffer),
							buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
							buffer.readFloat()));
		} else if (type == EnumPlayerPacket.QuestExtraButton) {
			EventHooks.onEvent(data.scriptData,
					EnumScriptType.QUEST_LOG_BUTTON,
					new QuestExtraButtonEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player), QuestController.instance.get(buffer.readInt())));
		} else if (type == EnumPlayerPacket.PlayerSkinSet) {
			PlayerSkinController pData = PlayerSkinController.getInstance();
			pData.loadPlayerSkin(Server.readNBT(buffer));
			pData.sendToAll(player);
		} else if (type == EnumPlayerPacket.ScriptEncrypt) {
			NBTTagCompound compound = Server.readNBT(buffer);
			int tab = compound.getInteger("Tab");
			ScriptContainer container;
			IScriptHandler handler = null;
			switch (compound.getByte("Type")) {
				case 0: { // Block or Door
					NBTTagCompound scriptData = compound.getCompoundTag("data");
					TileEntity tile = player.world.getTileEntity(new BlockPos(scriptData.getInteger("x"), scriptData.getInteger("y"), scriptData.getInteger("z")));
					if (tile instanceof TileScripted) {
						((TileScripted) tile).setNBT(compound);
						((TileScripted) tile).lastInited = -1L;
						handler = (TileScripted) tile;
					}
					if (tile instanceof TileScriptedDoor) {
						((TileScriptedDoor) tile).setNBT(compound);
						((TileScriptedDoor) tile).lastInited = -1L;
						handler = (TileScriptedDoor) tile;
					}
					break;
				}
				case 1: { // Forge
					handler = ScriptController.Instance.forgeScripts;
					((ForgeScriptData) handler).readFromNBT(compound);
					((ForgeScriptData) handler).lastInited = -1L;
					break;
				}
				case 2: { // Players
					handler = ScriptController.Instance.playerScripts;
					((PlayerScriptData) handler).readFromNBT(compound);
					ScriptController.Instance.playerScripts.readFromNBT(compound);
					((PlayerScriptData) handler).lastInited = -1L;
					break;
				}
				case 3: { // Item Stack
					handler = (ItemScriptedWrapper) Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(player.getHeldItemMainhand());
					((ItemScriptedWrapper) handler).setScriptNBT(compound);
					((ItemScriptedWrapper) handler).lastInited = -1L;
					break;
				}
				case 4: { // Potion
					handler = ScriptController.Instance.potionScripts;
					((PotionScriptData) handler).readFromNBT(compound);
					((PotionScriptData) handler).lastInited = -1L;
					break;
				}
				case 6: { // NPCs
					handler = ScriptController.Instance.npcsScripts;
					((NpcScriptData) handler).readFromNBT(compound);
					((NpcScriptData) handler).lastInited = -1L;
					break;
				}
				case 7: { // NPC
					Entity e = player.world.getEntityByID(compound.getInteger("EntityID"));
					if (!(e instanceof EntityNPCInterface)) {
						return;
					}
					handler = ((EntityNPCInterface) e).script;
					((DataScript) handler).readFromNBT(compound);
					((DataScript) handler).lastInited = -1L;
					break;
				}
			}
			boolean error = false;
			File file = new File(compound.getString("Path"));
			String filePath = file.getAbsolutePath();
			if (filePath.contains(".\\")) {
				filePath = filePath.substring(filePath.indexOf(".\\"));
			}
			else {
				File tempFile = new File(compound.getString("Path"));
				while (tempFile.getParentFile() != null) {
					tempFile = tempFile.getParentFile();
					if ((new File(tempFile, "config")).exists()) { break; }
				}
				filePath = filePath.replace(tempFile.getParentFile() + "\\", "");
			}
			String handlerType = "";
			if (handler != null) {
				handlerType = " for " + handler.getClass().getSimpleName();
				container = handler.getScripts().get(tab);
				error = container == null;
				if (!error) {
					boolean onlyTab = compound.getBoolean("OnlyTab");
					String code = "";
					if (onlyTab) { code = container.script; } else {
						try {
							Method getTotalCode = container.getClass().getDeclaredMethod("getTotalCode");
							getTotalCode.setAccessible(true);
							code = (String) getTotalCode.invoke(container);
							getTotalCode.setAccessible(false);
						}
						catch (Exception e) { error = true; }
					}
					if (!error) {
						error = !ScriptEncryption.encryptScript(file, compound.getString("Name"), code, onlyTab, container, handler);
						if (!error) {
							compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
							compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
							NBTTagCompound tabNBT = compound.getTagList("Scripts", 10).getCompoundTagAt(tab);
							if (!tabNBT.getKeySet().isEmpty()) {
								tabNBT.setString("Script", "");
								NBTTagList scriptList = tabNBT.getTagList("ScriptList", 10);
								boolean added = true;
								for (int i = 0; i < scriptList.tagCount(); i++) {
									if (scriptList.getCompoundTagAt(i).getString("Line").equals(compound.getString("Name"))) {
										added = false;
										break;
									}
								}
								if (added) {
									NBTTagCompound nbtFile = new NBTTagCompound();
									nbtFile.setString("Line", compound.getString("Name"));
									scriptList.appendTag(nbtFile);
								}
							}
							Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
						}
					}
				}
			}
			player.sendMessage(new TextComponentString(((char) 167) + "2CustomNPCs" + ((char) 167) + (error ? "c: Error encrypt" : "7: Encrypt") + " script to file \"" + filePath + "\"" + handlerType));
		} else if (type == EnumPlayerPacket.MiniMapData) {
			data.minimap.loadNBTData(Server.readNBT(buffer));
		} else if (type == EnumPlayerPacket.InGame) {
			EventHooks.onEvent(data.scriptData, EnumScriptType.IN_GAME, new PlayerEvent.LoginEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player)));
		} else if (type == EnumPlayerPacket.SendSyncData) {
			EnumSync synctype = EnumSync.values()[buffer.readInt()];
			SyncController.update(synctype, Server.readNBT(buffer), buffer, player);
		} else if (type == EnumPlayerPacket.AcceptScripts) {
			ScriptController.Instance.setAgreement(Server.readString(buffer), buffer.readBoolean());
		} else if (type == EnumPlayerPacket.DropsData) {
			if (player.capabilities.isCreativeMode && data.editingNpc != null && !data.editingNpc.isEntityAlive() && data.editingNpc.inventory.deadLoots != null) {
				List<String> list = new ArrayList<>();
				for (EntityLivingBase e : data.editingNpc.inventory.deadLoots.keySet()) {
					String name = e.getName();
					if (!(e instanceof EntityPlayer)) { name = ((char) 167) + "7Mob: " + e.getName(); }
					list.add(name);
				}
				Collections.sort(list);
				Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
			}
		} else if (type == EnumPlayerPacket.DropData) {
			if (player.capabilities.isCreativeMode && data.editingNpc != null && !data.editingNpc.isEntityAlive() && data.editingNpc.inventory.deadLoots != null) {
				DataInventory dataInv = data.editingNpc.inventory;
				String name = Server.readString(buffer);
				if (name != null) {
					int i = 0;
					int size = 9;
					for (EntityLivingBase e : dataInv.deadLoots.keySet()) {
						String n = e.getName();
						if (!(e instanceof EntityPlayer)) { n = "Mob: " + e.getName(); }
						if (n.equals(name)) {
							size = dataInv.deadLoots.get(e).getSizeInventory();
							break;
						}
						i++;
					}
					NoppesUtilServer.sendOpenGui(player, EnumGuiType.DeadInventory, data.editingNpc, size, i, 0);
				}
			}
		}
	}

	private void spawnItem(EntityPlayerMP player, ItemStack stack) {
		EntityItem ie = new EntityItem(player.world, player.posX, player.posY, player.posZ, stack);
		ie.motionX = 0.0d;
		ie.motionY = 0.0d;
		ie.motionZ = 0.0d;
		ie.lifespan = 100;
		player.world.spawnEntity(ie);
	}

}
