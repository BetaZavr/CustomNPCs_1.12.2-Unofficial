package noppes.npcs.client;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.EntitySpawnMessageHelper;
import noppes.npcs.*;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMixin;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.GuiAchievement;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.client.gui.GuiNpcRemoteEditor;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCManageMarcets;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.player.*;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IEditNPC;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.*;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MiniMapData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMiniMapData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.Util;
import noppes.npcs.util.TempFile;

public class PacketHandlerClient extends PacketHandlerServer {

	private static List<EnumPacketClient> list;

	static {
		PacketHandlerClient.list = new ArrayList<>();
		PacketHandlerClient.list.add(EnumPacketClient.EYE_BLINK);
		PacketHandlerClient.list.add(EnumPacketClient.NPC_VISUAL_DATA);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_NPC);
		PacketHandlerClient.list.add(EnumPacketClient.SET_TILE_DATA);
		PacketHandlerClient.list.add(EnumPacketClient.SEND_FILE_LIST);
		PacketHandlerClient.list.add(EnumPacketClient.SEND_FILE_PART);
		PacketHandlerClient.list.add(EnumPacketClient.PLAY_SOUND);
		PacketHandlerClient.list.add(EnumPacketClient.NPC_MOVINGPATH);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_NPC_ANIMATION);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_NPC_NAVIGATION);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_NPC_AI_TARGET);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_NPC_TARGET);
		PacketHandlerClient.list.add(EnumPacketClient.CHAT_BUBBLE);
		PacketHandlerClient.list.add(EnumPacketClient.SYNC_ADD);
		PacketHandlerClient.list.add(EnumPacketClient.SYNC_END);
		PacketHandlerClient.list.add(EnumPacketClient.SYNC_UPDATE);
		PacketHandlerClient.list.add(EnumPacketClient.BORDER_DATA);
		PacketHandlerClient.list.add(EnumPacketClient.MARCET_DATA);
		PacketHandlerClient.list.add(EnumPacketClient.VISIBLE_TRUE);
		PacketHandlerClient.list.add(EnumPacketClient.VISIBLE_FALSE);
		PacketHandlerClient.list.add(EnumPacketClient.NPC_DATA);
		PacketHandlerClient.list.add(EnumPacketClient.FORCE_PLAY_SOUND);
		PacketHandlerClient.list.add(EnumPacketClient.PLAYER_SKIN_ADD);
		PacketHandlerClient.list.add(EnumPacketClient.NPC_LOOK_POS);
		PacketHandlerClient.list.add(EnumPacketClient.UPDATE_HUD);
		PacketHandlerClient.list.add(EnumPacketClient.ANIMATION_DATA_SET);
		PacketHandlerClient.list.add(EnumPacketClient.ANIMATION_DATA_BASE_ANIMATIONS);
		PacketHandlerClient.list.add(EnumPacketClient.ANIMATION_DATA_RUN_ANIMATION);
		PacketHandlerClient.list.add(EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION);
		PacketHandlerClient.list.add(EnumPacketClient.ANIMATION_DATA_STOP_EMOTION);
	}

	@SuppressWarnings("unchecked")
	private void client(ByteBuf buffer, EntityPlayer player, EnumPacketClient type) throws Exception {
		CustomNpcs.debugData.startDebug("Client", type.toString(), "PacketHandlerClient_Received");
		Minecraft mc = Minecraft.getMinecraft();
		PlayerData data = CustomNpcs.proxy.getPlayerData(mc.player);
		if (type == EnumPacketClient.CHAT_BUBBLE) {
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityNPCInterface || entity instanceof EntityPlayer)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (entity instanceof EntityNPCInterface) {
				EntityNPCInterface npc = (EntityNPCInterface) entity;
				if (npc.messages == null) {
					npc.messages = new RenderChatMessages();
				}
				String text = NoppesStringUtils.formatText(Server.readString(buffer), player, npc);
				npc.messages.addMessage(text, npc);
				if (buffer.readBoolean()) {
					player.sendMessage(new TextComponentString(npc.getName() + ": " + text));
				}
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (!CustomNpcs.EnableChatBubbles || !CustomNpcs.EnablePlayerChatBubbles) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			EntityPlayer pl = (EntityPlayer) entity;
			if (!ClientEventHandler.chatMessages.containsKey(pl)) {
				ClientEventHandler.chatMessages.put(pl, new RenderChatMessages());
			}
			ClientEventHandler.chatMessages.get(pl).addMessage(Server.readString(buffer), pl);
		}
		else if (type == EnumPacketClient.CHAT) {
			StringBuilder message = new StringBuilder();
			String str;
			while ((str = Server.readString(buffer)) != null && !str.isEmpty()) {
				message.append(new TextComponentTranslation(str).getFormattedText());
			}
			player.sendMessage(new TextComponentTranslation(message.toString()));
		}
		else if (type == EnumPacketClient.EYE_BLINK) {
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			ModelData model = ((EntityCustomNpc) entity).modelData;
			model.eyes.blinkStart = System.currentTimeMillis();
		}
		else if (type == EnumPacketClient.MESSAGE) {
			TextComponentTranslation title = new TextComponentTranslation(Objects.requireNonNull(Server.readString(buffer)));
			TextComponentTranslation message = new TextComponentTranslation(Objects.requireNonNull(Server.readString(buffer)));
			mc.getToastGui().add(new GuiAchievement(title, message, buffer.readInt() % 3));
		}
		else if (type == EnumPacketClient.UPDATE_ITEM) {
			int id = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			ItemStack stack = player.inventory.getStackInSlot(id);
			if (!stack.isEmpty()) {
				((ItemStackWrapper) Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack)).setMCNbt(compound);
			}
		}
		else if (type == EnumPacketClient.VISIBLE_FALSE) {
			WorldClient w = (WorldClient) player.world;
			UUID uuid = Server.readUUID(buffer);
			int id = buffer.readInt();
			List<EntityNPCInterface> npcInterfaces = w.getEntities(EntityNPCInterface.class,
					entity -> entity.getUniqueID().equals(uuid));
			if (npcInterfaces.isEmpty()) {
				npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getEntityId() == id);
			}
			for (EntityNPCInterface npc : npcInterfaces) {
				if (npc == null) {
					continue;
				}
				w.removeEntity(npc);
			}
			if (!ClientProxy.notVisibleNPC.containsKey(player.world.provider.getDimension())) {
				ClientProxy.notVisibleNPC.put(player.world.provider.getDimension(), new ArrayList<>());
			}
			ClientProxy.notVisibleNPC.get(player.world.provider.getDimension()).add(uuid);
		}
		else if (type == EnumPacketClient.VISIBLE_TRUE) {
			WorldClient w = (WorldClient) player.world;
			UUID uuid = Server.readUUID(buffer);
			int id = buffer.readInt();
			List<EntityNPCInterface> npcInterfaces = w.getEntities(EntityNPCInterface.class,
					entity -> entity.getUniqueID().equals(uuid));
			if (npcInterfaces.isEmpty()) {
				npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getEntityId() == id);
			}
			if (npcInterfaces.isEmpty()) {
				LogWriter.debug("Tries to visible summon an entity into the client world.");
				EntitySpawnMessageHelper.spawn(buffer);
			}
			if (!npcInterfaces.isEmpty()) {
				for (EntityNPCInterface npc : npcInterfaces) {
					if (npc == null) {
						LogWriter.debug("Tries to visible summon an NPC into the client world.");
						EntitySpawnMessageHelper.spawn(buffer);
					}
				}
			}
			if (!ClientProxy.notVisibleNPC.containsKey(player.world.provider.getDimension())) {
				ClientProxy.notVisibleNPC.put(player.world.provider.getDimension(), new ArrayList<>());
			}
			for (UUID uID : ClientProxy.notVisibleNPC.get(player.world.provider.getDimension())) {
				if (uuid.equals(uID)) {
					ClientProxy.notVisibleNPC.get(player.world.provider.getDimension()).remove(uID);
					CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
					return;
				}
			}
		} else if (type == EnumPacketClient.SYNC_ADD || type == EnumPacketClient.SYNC_END) {
			EnumSync synctype = EnumSync.values()[buffer.readInt()];
			LogWriter.debug(type.toString().replace("SYNC_", "") + " data type: " + synctype);
			NBTTagCompound compound = Server.readNBT(buffer);
			SyncController.add(synctype, compound, type == EnumPacketClient.SYNC_END, player);
			if (synctype == EnumSync.QuestCategoriesData) { // Quest
				if (mc.currentScreen instanceof GuiNPCManageQuest) {
					mc.currentScreen.initGui();
				}
			} else if (synctype == EnumSync.DialogCategoriesData) { // Dialogs
				if (mc.currentScreen instanceof GuiNPCManageDialogs) {
					mc.currentScreen.initGui();
				}
			} else if (synctype == EnumSync.PlayerData) {
				data.setNBT(compound);
			} else if (synctype == EnumSync.ItemScriptedModels) {
				if (player.getServer() == null) {
					ItemScripted.Resources = NBTTags.getIntegerStringMap(compound.getTagList("List", 10));
				}
				CustomNpcs.proxy.reloadItemTextures();
			} else if (synctype == EnumSync.PlayerQuestData) {
				data.setNBT(compound);
			}
		} else if (type == EnumPacketClient.SYNC_UPDATE) {
			EnumSync synctype = EnumSync.values()[buffer.readInt()];
			SyncController.update(synctype, Server.readNBT(buffer), buffer, player);
			if (synctype == EnumSync.MailData) {
				GuiScreen screen = mc.currentScreen;
				if (screen instanceof GuiMailbox) {
					screen.initGui();
				} else if (screen instanceof GuiMailmanWrite) {
					if (GuiMailmanWrite.mail != null) {
						PlayerMail mail = data.mailData.get(GuiMailmanWrite.mail);
						if ((GuiMailmanWrite.mail.money > 0 && mail.money <= 0)
								|| (GuiMailmanWrite.mail.ransom > 0 && mail.ransom <= 0)) {
							MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS,
									CustomNpcs.MODID + ":mail.money", (float) player.posX, (float) player.posY,
									(float) player.posZ, 1.0f, 0.9f + 0.2f * (float) Math.random());
						}
						if (mail != null) {
							GuiMailmanWrite.mail = mail;
						}
					}
					screen.initGui();
				}
			}
		} else if (type == EnumPacketClient.CHEST_NAME) {
			GuiScreen screen = mc.currentScreen;
			if (screen instanceof GuiCustomChest) {
				((GuiCustomChest) screen).title = new TextComponentTranslation(Objects.requireNonNull(Server.readString(buffer)))
						.getFormattedText();
			}
		} else if (type == EnumPacketClient.SYNC_REMOVE) {
			EnumSync synctype = EnumSync.values()[buffer.readInt()];
			LogWriter.debug("Remove data type: " + synctype);
			SyncController.remove(synctype, buffer.readInt(), buffer);
		} else if (type == EnumPacketClient.MARK_DATA) {
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			MarkData mark = MarkData.get((EntityLivingBase) entity);
			mark.setNBT(Server.readNBT(buffer));
			MarkRenderer.needReload = true;
		} else if (type == EnumPacketClient.DIALOG) {
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			NoppesUtil.openDialog(dialog, (EntityNPCInterface) entity, player);
		} else if (type == EnumPacketClient.DIALOG_DUMMY) {
			EntityDialogNpc npc = new EntityDialogNpc(player.world);
			npc.display.setName(Server.readString(buffer));
			EntityUtil.Copy(player, npc);
			Dialog dialog = new Dialog(null);
			dialog.readNBT(Server.readNBT(buffer));
			NoppesUtil.openDialog(dialog, npc, player);
		} else if (type == EnumPacketClient.QUEST_COMPLETION) {
			int id = buffer.readInt();
			Quest quest = (Quest) QuestController.instance.get(id);
			if (!quest.completeText.isEmpty()) {
				NoppesUtil.openGUI(player, new GuiQuestCompletion(quest));
			} else if (quest.rewardType == EnumRewardType.ONE_SELECT && !quest.rewardItems.isEmpty()) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestChooseReward, quest.id);
			} else {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, id);
			}
		} else if (type == EnumPacketClient.EDIT_NPC) {
			int entityID = buffer.readInt();
			Entity entity = mc.world.getEntityByID(entityID);
			if (entityID == -1 || !(entity instanceof EntityNPCInterface)) {
				NoppesUtil.setLastNpc(null);
			} else {
				NoppesUtil.setLastNpc((EntityNPCInterface) entity);
			}
		} else if (type == EnumPacketClient.STOP_SOUND) {
			String soundRes = Server.readString(buffer);
			int categoryType = buffer.readInt();
			if (categoryType >= 0 && categoryType < SoundCategory.values().length) {
				MusicController.Instance.stopSound(soundRes, SoundCategory.values()[categoryType]);
			} else {
				MusicController.Instance.stopSounds();
			}
		}
		else if (type == EnumPacketClient.PLAY_SOUND) {
			MusicController.Instance.playSound(SoundCategory.PLAYERS, Server.readString(buffer), buffer.readInt(),
					buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readFloat());
		}
		else if (type == EnumPacketClient.FORCE_PLAY_SOUND) {
			int categoryType = buffer.readInt();
			SoundCategory cat = SoundCategory.PLAYERS;
			if (categoryType >= 0 && categoryType < SoundCategory.values().length) {
				cat = SoundCategory.values()[categoryType];
			}
			MusicController.Instance.forcePlaySound(cat, Server.readString(buffer), buffer.readInt(), buffer.readInt(),
					buffer.readInt(), buffer.readFloat(), buffer.readFloat());
		}
		else if (type == EnumPacketClient.UPDATE_NPC) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = mc.world.getEntityByID(compound.getInteger("EntityId"));
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (mc.currentScreen instanceof IEditNPC && entity.equals(((IEditNPC) mc.currentScreen).getNPC())) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			((EntityNPCInterface) entity).readSpawnData(compound);
		}
		else if (type == EnumPacketClient.ROLE) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = mc.world.getEntityByID(compound.getInteger("EntityId"));
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			((EntityNPCInterface) entity).advanced.setRole(compound.getInteger("Type"));
			((EntityNPCInterface) entity).advanced.roleInterface.readFromNBT(compound);
			NoppesUtil.setLastNpc((EntityNPCInterface) entity);
		}
		else if (type == EnumPacketClient.GUI) {
			EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
			CustomNpcs.proxy.openGui(NoppesUtil.getLastNpc(), gui, buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
		else if (type == EnumPacketClient.PARTICLE) {
			NoppesUtil.spawnParticle(buffer);
		}
		else if (type == EnumPacketClient.DELETE_ENTITY) {
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) entity).delete();
			} else {
				entity.setDead();
			}
		}
		else if (type == EnumPacketClient.SCROLL_LIST) {
			NoppesUtil.setScrollList(buffer);
		}
		else if (type == EnumPacketClient.SCROLL_DATA) {
			NoppesUtil.setScrollData(buffer);
		}
		else if (type == EnumPacketClient.SCROLL_DATA_PART) {
			NoppesUtil.addScrollData(buffer);
		}
		else if (type == EnumPacketClient.SCROLL_SELECTED) {
			GuiScreen gui = mc.currentScreen;
			String selected = Server.readString(buffer);
            assert selected != null;
            if (selected.equals("Unfreeze Npcs") || selected.equals("Freeze Npcs")) {
				if (CustomNpcs.Server != null && CustomNpcs.Server.isDedicatedServer()) {
					CustomNpcs.FreezeNPCs = selected.equals("Freeze Npcs");
				}
				if (gui instanceof GuiNpcRemoteEditor) {
					gui.initGui();
				}
			}
			if (!(gui instanceof IScrollData)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			((IScrollData) gui).setSelected(selected);
		}
		else if (type == EnumPacketClient.CLONE) {
			NBTTagCompound compound = Server.readNBT(buffer);
			NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(compound));
		}
		else if (type == EnumPacketClient.GUI_DATA) {
			GuiScreen gui = mc.currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
				gui = ((GuiNPCInterface) gui).getSubGui();
			} else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
				gui = ((GuiContainerNPCInterface) gui).getSubGui();
			}
			if (gui instanceof IGuiData) {
				NBTTagCompound compound = Server.readNBT(buffer);
				((IGuiData) gui).setGuiData(compound);
			}
		}
		else if (type == EnumPacketClient.GUI_UPDATE) {
			GuiScreen gui = mc.currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			gui.initGui();
		}
		else if (type == EnumPacketClient.GUI_ERROR) {
			GuiScreen gui = mc.currentScreen;
			if (!(gui instanceof IGuiError)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			int i = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			((IGuiError) gui).setError(i, compound);
		}
		else if (type == EnumPacketClient.GUI_CLOSE) {
			GuiScreen gui = mc.currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (gui instanceof IGuiClose) {
				int i = buffer.readInt();
				NBTTagCompound compound = Server.readNBT(buffer);
				((IGuiClose) gui).setClose(i, compound);
				if (gui instanceof GuiMailmanWrite) {
					return;
				}
			}
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
		else if (type == EnumPacketClient.VILLAGER_LIST) {
			MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(new PacketBuffer(buffer));
			ServerEventsHandler.Merchant.setRecipes(merchantrecipelist);
		}
		else if (type == EnumPacketClient.CONFIG_FONT) {
			int config = buffer.readInt();
			if (config == 0) {
				String font = Server.readString(buffer);
				int size = buffer.readInt();
				Runnable run = () -> {
                    assert font != null;
                    if (!font.isEmpty()) {
						CustomNpcs.FontType = font;
						CustomNpcs.FontSize = size;
						ClientProxy.Font.clear();
						ClientProxy.Font = new ClientProxy.FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
						CustomNpcs.Config.resetConfig();
						player.sendMessage(new TextComponentTranslation("Font set to %s", ClientProxy.Font.getName()));
					} else {
						player.sendMessage(new TextComponentTranslation("Current font is %s", ClientProxy.Font.getName()));
					}
					CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				};
				mc.addScheduledTask(run);
			}
		}
		else if (type == EnumPacketClient.MESSAGE_DATA) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TextComponentTranslation title = new TextComponentTranslation(compound.getString("Title"));
			TextComponentTranslation message = new TextComponentTranslation(compound.getString("Message"));
			if (compound.hasKey("QuestID", 3)) {
				IQuest quest = QuestController.instance.get(compound.getInteger("QuestID"));
				if (quest == null) {
					CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
					return;
				}
				title = new TextComponentTranslation("quest.name");
				title.appendSibling(new TextComponentString(": "));
				title.appendSibling(new TextComponentString(quest.getTitle()));
				int[] pr = compound.getIntArray("Progress");
				if (compound.getString("Type").equalsIgnoreCase("craft")) {
					ItemStack item = new ItemStack(compound.getCompoundTag("Item"));
					message = new TextComponentTranslation(item.getDisplayName());
				} else {
					message = new TextComponentTranslation(compound.getString("TargetName"));
				}
				if (pr[0] >= pr[1]) { // is complete
					message.appendSibling(new TextComponentString(" -"));
					message.appendSibling(
							new TextComponentTranslation("quest.task." + compound.getString("Type") + ".0"));
				} else {
					message.appendSibling(new TextComponentString(" = " + pr[0] + "/" + pr[1]));
				}
			}
			Object[] visible = null; // GuiToast.ToastInstance<?>[]
			for (Field f : GuiToast.class.getDeclaredFields()) {
				if (f.getType().getName().contains("ToastInstance")) {
					try {
						f.setAccessible(true);
						visible = (Object[]) f.get(mc.getToastGui());
					} catch (Exception e) {
						LogWriter.debug(e.toString());
					}
				}
			}
			if (visible == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			boolean found = false;
            for (Object obj : visible) {
				if (obj == null) {
					continue;
				}
				Field toast = obj.getClass().getDeclaredFields()[0];
				toast.setAccessible(true);
				if (!(toast.get(obj) instanceof GuiAchievement)) { continue; }

				GuiAchievement achn = (GuiAchievement) toast.get(obj);
				Field titleF = GuiAchievement.class.getDeclaredFields()[3];
				Field typeF = GuiAchievement.class.getDeclaredFields()[4];
				titleF.setAccessible(true);
				typeF.setAccessible(true);

				String titleD = Util.instance.deleteColor((String) titleF.get(achn));
				int typeD = (int) typeF.get(achn);
				if (!titleD.equals(Util.instance.deleteColor(title.getFormattedText()))
						|| compound.getInteger("MessageType") != typeD) {
					continue;
				}
				achn.setDisplayedText(title, message);
				found = true;
			}
			if (!found) {
				mc.getToastGui().add(new GuiAchievement(title, message, compound.getInteger("MessageType")));
			}
		}
		else if (type == EnumPacketClient.NPC_VISUAL_DATA) {
			int entityID = buffer.readInt();
			Entity entity = player.world.getEntityByID(entityID);
			if (entity instanceof EntityNPCInterface) {
				NBTTagCompound compound = Server.readNBT(buffer);
				EntityNPCInterface npcData = (EntityNPCInterface) entity;
				npcData.stats.setLevel(compound.getInteger("NPCLevel"));
				npcData.stats.setRarity(compound.getInteger("NPCRarity"));
				npcData.stats.setRarityTitle(compound.getString("RarityTitle"));
			}
		}
		else if (type == EnumPacketClient.MARCET_CLOSE) {
			Marcet m = (Marcet) MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.removeListener(player, false);
			}
			GuiScreen gui = mc.currentScreen;
			if (gui instanceof GuiNPCTrader) {
				gui.onGuiClosed();
			}
		}
		else if (type == EnumPacketClient.MARCET_DATA) {
			boolean updateGui = false;
			int t = buffer.readInt();
			switch (t) {
			case 0: {
				MarcetController.getInstance().markets.clear();
				MarcetController.getInstance().deals.clear();
				break;
			}
			case 1: {
				MarcetController.getInstance().loadMarcet(Server.readNBT(buffer));
				break;
			}
			case 2: {
				updateGui = true;
				break;
			}
			case 3: {
				MarcetController.getInstance().loadDeal(Server.readNBT(buffer));
				break;
			}
			case 4: {
				MarcetController.getInstance().removeMarcet(buffer.readInt());
				updateGui = true;
				break;
			}
			}
			if (!updateGui) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			GuiScreen gui = mc.currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (gui instanceof GuiNPCTrader) {
				((GuiNPCTrader) gui).setGuiData(new NBTTagCompound());
			} else if (gui instanceof GuiNPCManageMarcets) {
				((GuiNPCManageMarcets) gui).setGuiData(new NBTTagCompound());
			}
		}
		else if (type == EnumPacketClient.DETECT_HELD_ITEM) {
			try {
				player.inventory.setInventorySlotContents(buffer.readInt(), new ItemStack(Server.readNBT(buffer)));
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			} catch (Exception e) {
				LogWriter.error("Error:", e);
			}
			ItemStack held = new ItemStack(Server.readNBT(buffer));
			player.inventory.setItemStack(held);
		}
		else if (type == EnumPacketClient.BORDER_DATA) {
			int id = buffer.readInt();
			if (id == -1) {
				BorderController.getInstance().regions.clear();
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			} else if (id == -2) {
				GuiScreen gui = mc.currentScreen;
				if (gui instanceof GuiNPCInterface) {
					gui.initGui();
				}
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			BorderController.getInstance().loadRegion(Server.readNBT(buffer));
		}
		else if (type == EnumPacketClient.SAVE_SCHEMATIC) {
			NBTTagCompound compound = Server.readNBT(buffer);
			String name = compound.getString("Name") + ".schematic";
			Schematic schema = new Schematic(name);
			schema.load(compound);
			schema.save(player);
			SchematicController.Instance.map.put(name.toLowerCase(), new SchematicWrapper(schema));
		}
		else if (type == EnumPacketClient.GET_SCHEMATIC) {
			if (ClientEventHandler.schemaPos == null || ClientEventHandler.schema == null) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			int x = ClientEventHandler.schemaPos.getX();
			int y = ClientEventHandler.schemaPos.getY();
			int z = ClientEventHandler.schemaPos.getZ();
			Client.sendData(EnumPacketServer.SchematicsBuild, x, y, z, ClientEventHandler.rotation, ClientEventHandler.schema.getNBT());
		}
		else if (type == EnumPacketClient.UPDATE_HUD) {
			NBTTagCompound compound = Server.readNBT(buffer);
			data.hud.loadNBTData(compound);
		}
		else if (type == EnumPacketClient.DIMENSION_IDS) {
			ClientHandler.getInstance().sync(Server.readIntArray(buffer));
		}
		else if (type == EnumPacketClient.DROP_GROUP_DATA) {
			NBTTagCompound nbtTemplate = Server.readNBT(buffer);
			if (nbtTemplate.getKeySet().isEmpty()) {
				DropController.getInstance().templates.clear();
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			if (!nbtTemplate.hasKey("Name", 8)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			DropController.getInstance().templates.put(nbtTemplate.getString("Name"),
					new DropsTemplate(nbtTemplate.getCompoundTag("Groups")));
		}
		else if (type == EnumPacketClient.SET_TILE_DATA) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TileEntity tile = player.world.getTileEntity(
					new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z")));
			if (tile != null) {
				tile.readFromNBT(compound);
			}
		}
		else if (type == EnumPacketClient.ANIMATION_DATA_SET) {
			if (buffer.readBoolean()) { // is NPC
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					Entity entity = mc.world.getEntityByID(buffer.readInt());
					if (!(entity instanceof EntityNPCInterface)) {
						CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
						return;
					}
					((EntityNPCInterface) entity).animation.load(Server.readNBT(buffer));
				}
			}
			else { // is Player
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					IEntityPlayerMixin pl = (IEntityPlayerMixin) mc.world.getPlayerEntityByUUID(Server.readUUID(buffer));
					if (pl != null) {
						pl.npcs$getAnimation().load(Server.readNBT(buffer));
					}
				}
			}
		}
		else if (type == EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION) {
			if (buffer.readBoolean()) { // is NPC
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					Entity entity = mc.world.getEntityByID(buffer.readInt());
					if (!(entity instanceof EntityNPCInterface)) {
						CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
						return;
					}
					((EntityNPCInterface) entity).animation.stopAnimation();
				}
			}
			else { // is Player
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					IEntityPlayerMixin pl = (IEntityPlayerMixin) mc.world.getPlayerEntityByUUID(Server.readUUID(buffer));
					if (pl != null) { pl.npcs$getAnimation().stopAnimation(); }
				}
			}
		}
		else if (type == EnumPacketClient.ANIMATION_DATA_STOP_EMOTION) {
			if (buffer.readBoolean()) { // is NPC
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					Entity entity = mc.world.getEntityByID(buffer.readInt());
					if (!(entity instanceof EntityNPCInterface)) {
						CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
						return;
					}
					((EntityNPCInterface) entity).animation.stopEmotion();
				}
			}
			else { // is Player
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					IEntityPlayerMixin pl = (IEntityPlayerMixin) mc.world.getPlayerEntityByUUID(Server.readUUID(buffer));
					if (pl != null) { pl.npcs$getAnimation().stopEmotion(); }
				}
			}
		}
		else if (type == EnumPacketClient.ANIMATION_DATA_RUN_ANIMATION) {
			DataAnimation animation = null;
			if (buffer.readBoolean()) { // is NPC
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					Entity entity = mc.world.getEntityByID(buffer.readInt());
					if (!(entity instanceof EntityNPCInterface)) {
						CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
						return;
					}
					animation = ((EntityNPCInterface) entity).animation;
				}
			}
			else { // is Player
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					IEntityPlayerMixin pl = (IEntityPlayerMixin) mc.world.getPlayerEntityByUUID(Server.readUUID(buffer));
					if (pl != null) { animation = pl.npcs$getAnimation(); }
				}
			}
			if (animation != null) {
				AnimationConfig ac = AnimationController.getInstance().animations.get(buffer.readInt());
				if (ac != null) { animation.tryRunAnimation(ac, AnimationKind.get(buffer.readInt())); }
			}
		}
		else if (type == EnumPacketClient.ANIMATION_DATA_BASE_ANIMATIONS) {
			if (buffer.readBoolean()) { // is NPC
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					Entity entity = mc.world.getEntityByID(buffer.readInt());
					if (!(entity instanceof EntityNPCInterface)) {
						CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
						return;
					}
					((EntityNPCInterface) entity).animation.loadBaseAnimations(Server.readMap(buffer));;
				}
			}
			else { // is Player
				if (mc.world.provider.getDimension() == buffer.readInt()) {
					IEntityPlayerMixin pl = (IEntityPlayerMixin) mc.world.getPlayerEntityByUUID(Server.readUUID(buffer));
					if (pl != null) { pl.npcs$getAnimation().loadBaseAnimations(Server.readMap(buffer)); }
				}
			}
		}
		else if (type == EnumPacketClient.UPDATE_NPC_ANIMATION) {
			if (mc.world.provider.getDimension() != buffer.readInt()) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			Entity entity = mc.world.getEntityByID(buffer.readInt());
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			((EntityNPCInterface) entity).currentAnimation = buffer.readInt();
		}
		else if (type == EnumPacketClient.UPDATE_NPC_NAVIGATION) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = mc.world.getEntityByID(compound.getInteger("EntityId"));
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if (compound.hasKey("Navigating", 10)) {
				Path path = Server.readPathToNBT(compound.getCompoundTag("Navigating"));
				npc.navigating = path;
				npc.getNavigator().setPath(path, 1.0d);
			} else {
				npc.navigating = null;
				npc.getNavigator().setPath(null, 1.0d);
			}
		}
		else if (type == EnumPacketClient.UPDATE_NPC_AI_TARGET) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = mc.world.getEntityByID(compound.getInteger("EntityId"));
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if (compound.hasKey("aiIsSneak", 1)) {
				npc.aiIsSneak = compound.getBoolean("aiIsSneak");
				npc.setSneaking(npc.aiIsSneak);
			}

		}
		else if (type == EnumPacketClient.UPDATE_NPC_TARGET) {
			int entityId = buffer.readInt();
			Entity entity = mc.world.getEntityByID(entityId);
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			int targetId = buffer.readInt();
			if (targetId > -1) {
				Entity target = npc.world.getEntityByID(targetId);
				if (target instanceof EntityLivingBase) { ((EntityLiving) npc).setAttackTarget((EntityLivingBase) target); }
				else { ((EntityLiving) npc).setAttackTarget(null); }
			}
			else { ((EntityLiving) npc).setAttackTarget(null); }
		}
		else if (type == EnumPacketClient.SCRIPT_PACKAGE) {
			EventHooks.onScriptPackage(player, Server.readNBT(buffer));
		}
		else if (type == EnumPacketClient.SCRIPT_CLIENT) {
			ScriptController.HasStart = true;
			NBTTagCompound compound = Server.readNBT(buffer);
			ScriptController.Instance.setClientScripts(compound);
		}
		else if (type == EnumPacketClient.SEND_FILE_LIST) {
			NBTTagCompound compound = Server.readNBT(buffer);
			for (int i = 0; i < compound.getTagList("FileList", 10).tagCount(); i++) {
				NBTTagCompound tempFile = compound.getTagList("FileList", 10).getCompoundTagAt(i);
				String name = tempFile.getString("name");
				if (!ClientProxy.loadFiles.containsKey(name)) {
					ClientProxy.loadFiles.put(name, new TempFile());
				}
				TempFile file = ClientProxy.loadFiles.get(name);
				file.setTitle(tempFile);
			}
			ClientTickHandler.loadFiles();
		}
		else if (type == EnumPacketClient.NPC_MOVINGPATH) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity instanceof EntityCustomNpc) {
				((EntityCustomNpc) entity).ais.readToNBT(Server.readNBT(buffer));
			}
		}
		else if (type == EnumPacketClient.SEND_FILE_PART) {
			if (buffer.readBoolean()) {
				ClientProxy.loadFiles.remove(Server.readString(buffer));
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			int part = buffer.readInt();
			String name = Server.readString(buffer);
			if (!ClientProxy.loadFiles.containsKey(name)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			TempFile file = ClientProxy.loadFiles.get(name);
			file.data.put(part, Server.readString(buffer));
			file.lastLoad = System.currentTimeMillis() - 15000L;
			file.tryLoads = 0;
			if (file.isLoad()) {
				if (file.saveType == 1) {
					LogWriter.info("Script Client file was received from the Server: \"" + name + "\"");
					if (player.capabilities.isCreativeMode || data.game.op) {
						char c = ((char) 167);
						String s = "" + file.size;
						if (file.size > 999) {
							s = Util.instance.getTextReducedNumber(file.size, false, false, false);
						}
						ITextComponent message = new TextComponentString(c + "7[" + c + "2CustomNpcs" + c + "7]: Received client script: \"" + c + "f" + name + c + "7\" (" + s + c + "7b)");
						player.sendMessage(message);
					}
					// Put to session
					ScriptController.Instance.clients.put(name, file.getDataText());
					ScriptController.Instance.clientSizes.put(name, file.size);
					// save on client
				} else {
					file.save();
				}
				ClientProxy.loadFiles.remove(name);
			}
			ClientTickHandler.loadFiles();
		}
		else if (type == EnumPacketClient.PLAY_CAMERA_SHAKING) {
			ClientGuiEventHandler.crashes.set(buffer.readInt(), buffer.readInt(), buffer.readInt(),
					buffer.readBoolean());
		}
		else if (type == EnumPacketClient.STOP_CAMERA_SHAKING) {
			ClientGuiEventHandler.crashes.isActive = false;
		}
		else if (type == EnumPacketClient.SHOW_BANK_PLAYER) {
			ContainerNPCBank.editPlayerBankData = Server.readString(buffer);
            if (ContainerNPCBank.editPlayerBankData != null && ContainerNPCBank.editPlayerBankData.isEmpty()) {
				ContainerNPCBank.editPlayerBankData = null;
			}
		}
		else if (type == EnumPacketClient.BANK_CEIL_OPEN) {
			if (!(player.openContainer instanceof ContainerNPCBank)) {
				CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
				return;
			}
			((ContainerNPCBank) player.openContainer).dataCeil = buffer.readInt();
		}
		else if (type == EnumPacketClient.NPC_DATA) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity e = player.world.getEntityByID(compound.getInteger("EntityID"));
			if (e instanceof EntityNPCInterface) {
				e.readFromNBT(compound);
			}
		}
		else if (type == EnumPacketClient.PLAYER_SKIN_ADD) {
			NBTTagCompound compound = Server.readNBT(buffer);
			UUID uuid = PlayerSkinController.getInstance().loadPlayerSkin(compound);
			ClientProxy.resetSkin(uuid);
		}
		else if (type == EnumPacketClient.PLAYER_SKIN_GET) {
			ClientProxy.sendSkin(player.getUniqueID());
		}
		else if (type == EnumPacketClient.MINIMAP_DATA) {
			PlayerMiniMapData mm = data.minimap;
			String modName = mm.modName;
			mm.loadNBTData(Server.readNBT(buffer));
			int isChanged = 0;
			if (modName.endsWith("journeymap")) {
				try {
					Class<?> ws = Class.forName("journeymap.client.waypoint.WaypointStore");
					Class<?> wp = Class.forName("journeymap.client.model.Waypoint");
					Constructor<?> wc = null;
					for (Constructor<?> c : wp.getDeclaredConstructors()) {
						if (c.getParameterCount() == 12) {
							Parameter[] ps = c.getParameters();
							if (ps[0].getType() == String.class && ps[1].getType() == int.class
									&& ps[2].getType() == int.class && ps[3].getType() == int.class
									&& ps[4].getType() == boolean.class && ps[5].getType() == int.class
									&& ps[6].getType() == int.class && ps[7].getType() == int.class
									&& ps[8].getType().getSimpleName().equals("Type") && ps[9].getType() == String.class
									&& ps[10].getType() == Integer.class && ps[11].getType() == Collection.class) {
								wc = c;
								break;
							}
						}
					}
					Field cacheField = ws.getDeclaredField("cache");
					Field groupCacheField = ws.getDeclaredField("groupCache");
					Field dimensionsField = ws.getDeclaredField("dimensions");
					Method load = null, remove = null;
					for (Method m : ws.getDeclaredMethods()) {
						if (m.getName().equals("load") && m.getParameterCount() == 2
								&& m.getParameters()[0].getType() == Collection.class
								&& m.getParameters()[1].getType() == boolean.class) {
							load = m;
						}
						if (m.getName().equals("remove") && m.getParameterCount() == 1
								&& m.getParameters()[0].getType().getSimpleName().equals("Waypoint")) {
							remove = m;
						}
					}

					if (!cacheField.isAccessible()) {
						cacheField.setAccessible(true);
					}
					if (!groupCacheField.isAccessible()) {
						groupCacheField.setAccessible(true);
					}
					if (!dimensionsField.isAccessible()) {
						dimensionsField.setAccessible(true);
					}
					Object waypointStore = ws.getEnumConstants()[0];

					// Clear OLD
					Set<Integer> dimensions = (Set<Integer>) dimensionsField.get(waypointStore);
					dimensions.clear();

					Cache<Long, Object> groupCache = (Cache<Long, Object>) groupCacheField.get(waypointStore);
					groupCache.invalidateAll();

					Cache<String, Object> cache = (Cache<String, Object>) cacheField.get(waypointStore);
					Map<String, Object> map = cache.asMap();
					for (String name : map.keySet()) {
                        assert remove != null;
                        remove.invoke(waypointStore, map.get(name));
					}
					cache.invalidateAll();

					// Create and Add new
					isChanged = 1;
					List<Object> waypoints = new ArrayList<>();
					for (MiniMapData mmd : mm.points) {
						int dimID = mmd.dimIDs[0];
						Object t = null;
						for (Object enumType : wp.getClasses()[0].getEnumConstants()) {
							if (t == null) {
								t = enumType;
							}
							if (enumType.toString().equalsIgnoreCase(mmd.type)) {
								t = enumType;
								break;
							}
						}
						if (t == null) {
							continue;
						}
						int x = (int) mmd.pos.getX();
						int y = (int) mmd.pos.getY();
						int z = (int) mmd.pos.getZ();
						Color color = new Color(mmd.color);
						List<Integer> dim = new ArrayList<>();
						for (int dId : mmd.dimIDs) {
							dim.add(dId);
						}
                        assert wc != null;
                        Object waypoint = wc.newInstance(mmd.name, x, y, z, mmd.isEnable, color.getRed(), color.getGreen(), color.getBlue(), t, "journeymap", dimID, dim);
						wp.getDeclaredMethod("setIcon", String.class).invoke(waypoint, mmd.icon);
						waypoints.add(waypoint);
					}
                    assert load != null;
                    load.invoke(waypointStore, waypoints, true);
				} catch (Exception e) {
					isChanged = 2;
				}
			} else if (modName.endsWith("xaerominimap")) {
				try {
					Class<?> xms = Class.forName("xaero.common.XaeroMinimapSession");
					Object minimapSession = xms.getDeclaredMethod("getCurrentSession").invoke(xms); // XaeroMinimapSession
					Object waypointsManager = xms.getDeclaredMethod("getWaypointsManager").invoke(minimapSession); // WaypointsManager

					Method getWaypointMap = waypointsManager.getClass().getDeclaredMethod("getWaypointMap");
					HashMap<String, Object> waypointMap = (HashMap<String, Object>) getWaypointMap
							.invoke(waypointsManager);

					String mainContainerID = (String) waypointsManager.getClass()
							.getDeclaredMethod("getAutoRootContainerID").invoke(waypointsManager);
					Object wwrc = waypointMap.get(mainContainerID);// WaypointWorldRootContainer
					Field fwwrc = wwrc.getClass().getDeclaredField("dimensionTypes");
					fwwrc.setAccessible(true);

					Int2ObjectMap<Object> dimensionTypes = (Int2ObjectMap<Object>) fwwrc.get(wwrc);
					boolean saveConfig = false;
					for (int dim : DimensionManager.getStaticDimensionIDs()) {
						if (!dimensionTypes.containsKey(dim)) {
							World ret = DimensionManager.getWorld(dim, true);
							if (ret == null) {
								DimensionManager.initDimension(dim);
								ret = DimensionManager.getWorld(dim);
							}
							dimensionTypes.put(dim, wwrc.getClass()
									.getDeclaredMethod("createDimensionType", World.class).invoke(wwrc, ret)); // WaypointDimensionTypeInfo
							saveConfig = true;
						}
					}
					if (saveConfig) {
						wwrc.getClass().getDeclaredMethod("saveConfig").invoke(wwrc);
					}
					Class<?> xm = Class.forName("xaero.minimap.XaeroMinimap");
					Object instance = xm.getField("instance").get(xm);
					File parentFile = (File) xm.getDeclaredMethod("getWaypointsFolder").invoke(instance);

					String world_name = (String) mm.addData.get("xaero_world_name");
					if (world_name == null || world_name.isEmpty()) {
						HashMap<String, Object> dimMap = (HashMap<String, Object>) wwrc.getClass()
								.getField("subContainers").get(wwrc);
						for (String k : dimMap.keySet()) {
							world_name = (String) dimMap.get(k).getClass().getDeclaredMethod("getKey")
									.invoke(dimMap.get(k));
						}
					}
                    assert world_name != null;
                    File worldDir = new File(parentFile, world_name);
					Gson gson = new Gson();

					Map<File, TempWaypointText> map = new HashMap<>();
					for (MiniMapData mmd : mm.points) {
						if (mmd.gsonData.containsKey("temporary") && gson.fromJson(mmd.gsonData.get("temporary"), boolean.class)) { continue; }
						int dimID = mmd.dimIDs[0];
						File dimDir = new File(worldDir, "dim%" + dimID);
						if (dimDir.exists() || dimDir.mkdirs()) {
							File dimFile = new File(dimDir, "/waypoints.txt");
							StringBuilder text = new StringBuilder();
                            StringBuilder endText = new StringBuilder();
                            if (!map.containsKey(dimFile)) {
								if (!dimFile.exists()) {
									text.append("#" + ((char) 10));
									text.append("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination" + ((char) 10));
									text.append("#" + ((char) 10));
								} else {
									FileInputStream inputStream = new FileInputStream(dimFile);
                                    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8))) {
                                        boolean end = false;
                                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                                            if (!end && line.indexOf("#") != 0) {
                                                end = true;
                                                continue;
                                            }
                                            if (line.indexOf("waypoint:") == 0) {
                                                continue;
                                            }
                                            if (end) {
                                                endText.append(line).append((char) 10);
                                            } else {
                                                text.append(line).append((char) 10);
                                            }
                                        }
                                    }
								}
								map.put(dimFile, new TempWaypointText(text.toString(), endText.toString()));
							}
						}
					}
					for (File dimFile : map.keySet()) {
						Util.instance.saveFile(dimFile, map.get(dimFile).getString());
					}
					
					Object settings = xm.getDeclaredMethod("getSettings").invoke(instance); // ModSettings
					Method loadWaypointsFromAllSources = null;
					for (Method m : settings.getClass().getDeclaredMethods()) {
						if (m.getName().equals("loadWaypointsFromAllSources") && m.getParameterCount() == 1) {
							loadWaypointsFromAllSources = m;
							break;
						}
					}
                    assert loadWaypointsFromAllSources != null;
                    loadWaypointsFromAllSources.invoke(settings, waypointsManager);
				} catch (Exception e) {
					LogWriter.error("Error:", e);
					isChanged = 2;
				}
			} else if (modName.endsWith("voxelmap")) {
				try {
					Class<?> vm = Class.forName("com.mamiyaotaru.voxelmap.VoxelMap");
					Object instance = vm.getMethod("getInstance").invoke(vm);
					Object waypointManager = vm.getMethod("getWaypointManager").invoke(instance);
					List<Object> waypoints = (List<Object>) waypointManager.getClass().getMethod("getWaypoints").invoke(waypointManager);
					// Clear OLD
					waypoints.clear();

					// Create and Add new
					isChanged = 1;
					Class<?> wc = Class.forName("com.mamiyaotaru.voxelmap.util.Waypoint");
					Constructor<?> cw = wc.getConstructor(String.class, int.class, int.class, int.class, boolean.class,
							float.class, float.class, float.class, String.class, String.class, TreeSet.class);
					for (MiniMapData mmd : mm.points) {
						int dimID = mmd.dimIDs[0];
						int x = (int) mmd.pos.getX();
						int y = (int) mmd.pos.getY();
						int z = (int) mmd.pos.getZ();
						Color color = new Color(mmd.color);
						TreeSet<Integer> dim = new TreeSet<>();
						for (int dId : mmd.dimIDs) {
							dim.add(dId);
						}
						String worldName = mmd.gsonData.getOrDefault("voxel_world_name", "");
						if (worldName.isEmpty()) {
							World ret = DimensionManager.getWorld(dimID, true);
							if (ret == null) {
								DimensionManager.initDimension(dimID);
								ret = DimensionManager.getWorld(dimID);
							}
							worldName = ret.getProviderName();
						}
						Object waypoint = cw.newInstance(mmd.name, x, z, y, mmd.isEnable(), color.getRed() / 255.0f,
								color.getGreen() / 255.0f, color.getBlue() / 255.0f, mmd.icon, worldName, dim);
						waypoints.add(waypoint);
					}
					waypointManager.getClass().getMethod("saveWaypoints").invoke(waypointManager);
					Method loadWaypoints = waypointManager.getClass().getDeclaredMethod("loadWaypoints");
					loadWaypoints.setAccessible(true);
					loadWaypoints.invoke(waypointManager);
					// remove any dimension points;
					Method med = waypointManager.getClass().getDeclaredMethod("enteredDimension", int.class);
					med.setAccessible(true);
					med.invoke(waypointManager, player.world.provider.getDimension());
				} catch (Exception e) {
					LogWriter.error("Error:", e);
					isChanged = 2;
				}
			}
			if (isChanged != 0) {
				player.sendMessage(new TextComponentTranslation("minimap.set.points." + isChanged,
						((char) 167) + "7" + modName));
			}
		}
		else if (type == EnumPacketClient.NPC_LOOK_POS) {
			if (player.world.provider.getDimension() == buffer.readInt()) {
				Entity npc = player.world.getEntityByID(buffer.readInt());
				if (npc instanceof EntityNPCInterface) {
					int lookId = buffer.readInt();
					if (lookId < 0) { ((EntityNPCInterface) npc).lookAt = null; }
					else { ((EntityNPCInterface) npc).lookAt = player.world.getEntityByID(lookId); }
				}
			}
		}
		else if (type == EnumPacketClient.EVENT_NAMES) {
			String names = Server.readString(buffer);
			if (names == null) { return; }
			byte t = names.indexOf(((char) 167) + "6Client") == 0 ? (byte) 0 : names.indexOf(((char) 167) + "6Server") == 0 ? (byte)1 : (byte)2;
			List<String> list;
			if (t == 2) {
				list = new ArrayList<>();
				for (EnumScriptType est : EnumScriptType.values()) {
					list.add(est.function);
				}
			} else {
				list = new ArrayList<>(t == 0 ? CustomNpcs.forgeClientEventNames.values() : CustomNpcs.forgeEventNames.values());
			}
			names = names.substring(names.indexOf("" + ((char) 10)) + 2);
			names = names.substring(0, names.indexOf(";"));
			for (String key : names.split(", ")) {
				if (list.contains(key)) { continue; }
				list.add(key);
			}
			Collections.sort(list);
			String pre = "";
			StringBuilder text = new StringBuilder();
			for (String name : list) {
				if (pre.isEmpty()) { pre = "" + name.charAt(0); }
				else if (!pre.equals("" + name.charAt(0))) {
					text.append(System.lineSeparator());
					pre = "" + name.charAt(0);
				}
				text.append(name);
				text.append(System.lineSeparator());
			}
			File file = new File(CustomNpcs.Dir.getParentFile().getParentFile().getParentFile(), "all "+(t ==0 ? "client" : t ==1 ? "forge" : "api" )+ " event names.txt");
			Util.instance.saveFile(file, text.toString());
			player.sendMessage(new TextComponentString(((char) 167) + "e[" + ((char) 167) + "2CustomNpcs" + ((char) 167) + "e]" + ((char) 167) + "r: " + ((char) 167) + "7Save event names to file: " + ((char) 167) + "r" + file));
		}
		CustomNpcs.debugData.endDebug("Client", type.toString(), "PacketHandlerClient_Received");
	}

	@SubscribeEvent
	public void onPacketData(FMLNetworkEvent.ClientCustomPacketEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null) {
			return;
		}
		ByteBuf buffer = event.getPacket().payload();
		Minecraft.getMinecraft().addScheduledTask(() -> {
			EnumPacketClient type = null;
			try {
				type = EnumPacketClient.values()[buffer.readInt()];
				if (!PacketHandlerClient.list.contains(type)) {
					LogWriter.debug("Received: " + type);
				}
				this.client(buffer, player, type);
			} catch (Exception e) {
				LogWriter.error("Error with EnumPacketClient." + type, e);
			} finally {
				buffer.release();
			}
		});
	}
	
	public static class TempWaypointText{
		
		public String text;
		String endText;

		public TempWaypointText(String t, String e) {
			text = t;
			endText = e;
		}

		public String getString() { return text + endText; }
		
	}
	
}
