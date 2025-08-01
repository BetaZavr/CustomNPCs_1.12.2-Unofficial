package noppes.npcs.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.*;
import noppes.npcs.api.mixin.client.audio.ISoundHandlerMixin;
import noppes.npcs.api.mixin.client.audio.ISoundManagerMixin;
import noppes.npcs.api.mixin.client.gui.IGuiYesNoMixin;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.constants.*;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.client.util.MusicData;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import noppes.npcs.util.TempFile;
import org.lwjgl.input.Mouse;

public class ClientTickHandler {

	public static boolean checkMails = false;
	public static boolean inGame = false;
	public static Map<ISound, MusicData> musics = new HashMap<>();
	
	public static void loadFiles() {
		if (ClientProxy.loadFiles.isEmpty()) {
			return;
		}
		String isDel = "";
		for (String key : ClientProxy.loadFiles.keySet()) {
			TempFile file = ClientProxy.loadFiles.get(key);
			if (file.lastLoad == 0) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.GetFilePart, file.getNextPart(), key);
				file.lastLoad = System.currentTimeMillis();
			} else if (file.lastLoad + 12000L < System.currentTimeMillis()) {
				file.tryLoads++;
				if (file.tryLoads > 9) {
					LogWriter.error("Failed to load file after 10 attempts: \"" + key + "\"");
					isDel = key;
				} else {
					NoppesUtilPlayer.sendData(EnumPlayerPacket.GetFilePart, file.getNextPart(), key);
					file.lastLoad = System.currentTimeMillis();
				}
			}
			break;
		}
		if (!isDel.isEmpty()) {
			ClientProxy.loadFiles.remove(isDel);
			ClientTickHandler.loadFiles();
		}
	}
	private boolean otherContainer = false;
	private World prevWorld;
	public final Map<String, ISound> nowPlayingSounds = new HashMap<>();
	public static long ticks = 0L;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void npcClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) { return; }
		CustomNpcs.debugData.start(null);
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && ClientProxy.playerData.getPlayer() == null) {
			ClientProxy.playerData.setPlayer(mc.player);
		}
		if (mc.player != null && mc.player.openContainer instanceof ContainerPlayer) {
			if (this.otherContainer) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
				this.otherContainer = false;
			}
		}
		else { this.otherContainer = true; }
		if (mc.player == null) {
			if (ClientTickHandler.inGame) {
				LogWriter.debug("Client Player: Exit game");
				ClientTickHandler.inGame = false;
				ScriptController.Instance.clientScripts.saveDefaultScripts();
				EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.LOGOUT, new PlayerEvent.LogoutEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(mc.player)));
			} else if (!ScriptController.Instance.clientScripts.loadDefault) {
				ScriptController.Instance.clientScripts.loadDefaultScripts();
			}
		}
		
		char c0 = Keyboard.getEventCharacter();
        if (Keyboard.getEventKey() == 0 && c0 >= ' ' || Keyboard.getEventKeyState()) {
        	if (mc.currentScreen instanceof GuiYesNo) {
        		if (Keyboard.getEventKey() == 1) { // ESC
					GuiYesNoCallback parentScreen = ((IGuiYesNoMixin) mc.currentScreen).npcs$getParentScreen();
        			parentScreen.confirmClicked(false, ((IGuiYesNoMixin) mc.currentScreen).npcs$getParentButtonClickedId());
        		}
        		if (Keyboard.getEventKey() == 28) { // Enter
					GuiYesNoCallback parentScreen = ((IGuiYesNoMixin) mc.currentScreen).npcs$getParentScreen();
					parentScreen.confirmClicked(true, ((IGuiYesNoMixin) mc.currentScreen).npcs$getParentButtonClickedId());
        		}
        	}
        }
		++ticks;
		++RenderNPCInterface.LastTextureTick;
		if (this.prevWorld != mc.world) {
			this.prevWorld = mc.world;
			MusicController.Instance.stopSounds();
		}
		SoundManager sm = ((ISoundHandlerMixin) mc.getSoundHandler()).npcs$getSndManager();
		Map<String, ISound> playingSounds = ((ISoundManagerMixin) sm).npcs$getPlayingSounds();
		List<String> del = new ArrayList<>();
        if (playingSounds != null) {
			for (String uuid : playingSounds.keySet()) { // is played
				try {
					if (!this.nowPlayingSounds.containsKey(uuid) || !this.nowPlayingSounds.containsValue(playingSounds.get(uuid))) {
						ISound sound = playingSounds.get(uuid);
						if (sound.getCategory() == SoundCategory.MUSIC && !ClientTickHandler.musics.containsKey(sound)) {
							ClientTickHandler.musics.put(sound, new MusicData(sound, uuid, sm));
						}
						this.nowPlayingSounds.put(uuid, playingSounds.get(uuid));
						NoppesUtilPlayer.sendData(EnumPlayerPacket.PlaySound, sound.getSound().getSoundLocation(),
								sound.getSoundLocation(), sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(),
								sound.getZPosF(), sound.getVolume(), sound.getPitch());
						EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.SOUND_PLAY,
								new PlayerEvent.PlayerSound((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(mc.player),
										sound.getSound().getSoundLocation().toString(), sound.getSoundLocation().toString(),
										sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF(),
										sound.getVolume(), sound.getPitch()));
					}
				} catch (Exception e) {
					LogWriter.error("Error set played sound: " + e);
					del.add(uuid);
				}
			}
			for (String uuid : this.nowPlayingSounds.keySet()) { // is stopped
				try {
					if (!playingSounds.containsKey(uuid) || !playingSounds.containsValue(this.nowPlayingSounds.get(uuid))) {
						ISound sound = this.nowPlayingSounds.get(uuid);
						ClientTickHandler.musics.remove(sound);
						NoppesUtilPlayer.sendData(EnumPlayerPacket.StopSound, sound.getSound().getSoundLocation(),
								sound.getSoundLocation(), sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(),
								sound.getZPosF(), sound.getVolume(), sound.getPitch());
						EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.SOUND_STOP,
								new PlayerEvent.PlayerSound((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(mc.player),
										sound.getSound().getSoundLocation().toString(), sound.getSoundLocation().toString(),
										sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF(),
										sound.getVolume(), sound.getPitch()));
						del.add(uuid);
					}
				} catch (Exception e) {
					LogWriter.error("Error stop played sound: " + e);
					del.add(uuid);
				}
			}
		}
		for (String uuid : del) {
			this.nowPlayingSounds.remove(uuid);
		}
		for (MusicData md : ClientTickHandler.musics.values()) {
			if (md.sound != null && md.source != null && !md.source.paused()) {
				EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.SOUND_TICK_EVENT,
						md.createEvent(CustomNpcs.proxy.getPlayer()));
			}
		}
		if (ticks % 10 == 0) {
			MarcetController.getInstance().updateTime();
			ClientTickHandler.loadFiles();
			if (mc.playerController != null) {
				double d0 = mc.playerController.getBlockReachDistance();
				if (mc.playerController.extendedReach()) { d0 = 6.0; }
				if (ClientProxy.playerData.game.blockReachDistance != d0) {
					ClientProxy.playerData.game.blockReachDistance = d0;
					ClientProxy.playerData.game.updateClient = true;
				}
				double d1 = mc.gameSettings.getOptionFloatValue(GameSettings.Options.RENDER_DISTANCE) * 16.0;
				if (ClientProxy.playerData.game.renderDistance != d1) {
					ClientProxy.playerData.game.renderDistance = d1;
					ClientProxy.playerData.game.updateClient = true;
				}
			} else {
				ClientProxy.playerData.game.blockReachDistance = 0.0d;
				ClientProxy.playerData.game.renderDistance = 0.0d;
				ClientProxy.playerData.game.updateClient = true;
			}
			if (mc.player != null) {
				MusicController.Instance.checkBards(mc.player);
				if (ClientProxy.playerData.game.updateClient) {
					NoppesUtilPlayer.sendData(EnumPlayerPacket.SendSyncData, EnumSync.GameData, ClientProxy.playerData.game.saveNBTData(new NBTTagCompound()));
					ClientProxy.playerData.game.updateClient = false;
				}
			}
		}
		if (ticks % 60 == 0) { BlockWrapper.checkClearCache(); }
		if (ScriptController.Instance.clientScripts.isEnabled()) {
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.TICK, new PlayerEvent.UpdateEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(mc.player)));
		}
		if (ClientTickHandler.checkMails || CustomNpcs.MailWindow != -1 && ticks % 100 == 0) {
			boolean hasNewMail = false;
			long time = System.currentTimeMillis();
			for (PlayerMail mail : ClientProxy.playerData.mailData.playerMails) {
				if (!mail.beenRead && time - mail.timeWhenReceived >= mail.timeWillCome) {
					hasNewMail = true;
					break;
				}
			}
			if (hasNewMail != ClientGuiEventHandler.hasNewMail) {
				ClientGuiEventHandler.hasNewMail = hasNewMail;
				if (hasNewMail) {
					ClientGuiEventHandler.showNewMail = 0L;
					ClientGuiEventHandler.startMail = 0L;
					mc.player.sendMessage(new TextComponentTranslation("mailbox.new.letters.received"));
				}
			}
			ClientTickHandler.checkMails = false;
		}
		if (mc.currentScreen != null) {
			if (!ClientProxy.playerData.hud.keyPress.isEmpty()) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, -1);
				ClientProxy.playerData.hud.keyPress.clear();
			}
		}
		if (mc.currentScreen instanceof GuiNPCInterface || mc.currentScreen instanceof GuiContainerNPCInterface) {
			SubGuiInterface subGui;
			if (mc.currentScreen instanceof GuiNPCInterface) {
				subGui = ((GuiNPCInterface) mc.currentScreen).getSubGui();
			} else {
				subGui = ((GuiContainerNPCInterface) mc.currentScreen).getSubGui();
			}
			if (subGui != null && subGui.getSubGui() != null) {
				while (subGui.getSubGui() != null) {
					subGui = subGui.getSubGui();
				}
			}
			if (ClientEventHandler.subgui != subGui) {
				LogWriter.debug(
						((subGui == null ? "Close SubGUI " : "Open SubGUI - " + subGui.getClass()) + "; SubOLD - "
								+ (ClientEventHandler.subgui == null ? "null"
										: ClientEventHandler.subgui.getClass().getSimpleName()))
								+ "; in GUI "
								+ (mc.currentScreen != null ? mc.currentScreen.getClass().getSimpleName() : "NULL"));
				ClientEventHandler.subgui = subGui;
			}
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcKeyInputEvent(InputEvent.KeyInputEvent event) {
		CustomNpcs.debugData.start(null);
		if (CustomNpcs.SceneButtonsEnabled) {
			if (ClientProxy.Scene1.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 1);
			}
			if (ClientProxy.Scene2.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 2);
			}
			if (ClientProxy.Scene3.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 3);
			}
			if (ClientProxy.SceneReset.isPressed()) {
				Client.sendData(EnumPacketServer.SceneReset);
			}
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (ClientProxy.QuestLog.isPressed()) {
			if (mc.currentScreen == null) {
				CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.QuestLog, mc.player);
			} else if (mc.currentScreen instanceof GuiLog) {
				mc.setIngameFocus();
			}
		}
		if (mc.currentScreen == null) {
			boolean isCtrlPressed = ClientProxy.playerData.hud.hasOrKeysPressed(157, 29);
			boolean isShiftPressed = ClientProxy.playerData.hud.hasOrKeysPressed(54, 42);
			boolean isAltPressed = ClientProxy.playerData.hud.hasOrKeysPressed(184, 56);
			boolean isMetaPressed = ClientProxy.playerData.hud.hasOrKeysPressed(220, 219);
			boolean isDown = Keyboard.getEventKeyState();
			int key = Keyboard.getEventKey();
			if (isDown) {
				ClientProxy.playerData.hud.keyPress.add(key);
				ClientProxy.pressed(key);
				if (key == 29 || key == 157) { isCtrlPressed = true; }
				if (key == 42 || key == 54) { isShiftPressed = true; }
				if (key == 56 || key == 184) { isAltPressed = true; }
				if (key == 219 || key == 220) { isMetaPressed = true; }
			} else {
				if (ClientProxy.playerData.hud.hasOrKeysPressed(key)) {
					ClientProxy.playerData.hud.keyPress.remove((Integer) key);
				}
				if (key == 29 || key == 157) { isCtrlPressed = false; }
				if (key == 42 || key == 54) { isShiftPressed = false; }
				if (key == 56 || key == 184) { isAltPressed = false; }
				if (key == 219 || key == 220) { isMetaPressed = false; }
			}
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, key, isDown, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
		} else if (!ClientProxy.playerData.hud.keyPress.isEmpty()) {
			ClientProxy.playerData.hud.keyPress.clear();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, -1);
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		CustomNpcs.debugData.start(null);
		if (event.getHand() != EnumHand.MAIN_HAND) { return; }
		NoppesUtilPlayer.sendData(EnumPlayerPacket.LeftClick);
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcLivingUpdate(LivingUpdateEvent event) {
		if (!event.getEntity().world.isRemote || !(event.getEntity() instanceof EntityNPCInterface)) { return; }
		CustomNpcs.debugData.start(null);
		int dimID = Minecraft.getMinecraft().world.provider.getDimension();
		if (ClientProxy.notVisibleNPC.containsKey(dimID)
				&& ClientProxy.notVisibleNPC.get(dimID).contains(event.getEntity().getUniqueID())) {
			Minecraft.getMinecraft().world.removeEntity(event.getEntity());
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcLoadAllOBJTextures(TextureStitchEvent.Pre event) {
		CustomNpcs.debugData.start(null);
		File assets = new File(CustomNpcs.Dir, "assets/customnpcs");
		if (!assets.exists()) {
			CustomNpcs.debugData.end(null);
			return;
		}
		List<ResourceLocation> objTextures = new ArrayList<>();
		for (File file : Util.instance.getFiles(assets, ".mtl")) {
			try {
				for (String line : Files.readAllLines(file.toPath())) {
					if (!line.contains("map_Kd")) {
						continue;
					}
					int endIndex = line.indexOf("" + ((char) 10), line.indexOf("map_Kd"));
					if (endIndex == -1) {
						endIndex = line.length();
					}
					String txtr = line.substring(line.indexOf(" ", line.indexOf("map_Kd")) + 1, endIndex);
					String domain = "", path;
					if (!txtr.contains(":")) {
						path = txtr;
					} else {
						domain = txtr.substring(0, txtr.indexOf(":"));
						path = txtr.substring(txtr.indexOf(":") + 1);
					}
					objTextures.add(new ResourceLocation(domain, path));
				}
			} catch (IOException e) { LogWriter.error(e); }
		}
		for (ResourceLocation res : objTextures) {
			if (event.getMap().getTextureExtry(res.toString()) != null) {
				continue;
			}
			event.getMap().registerSprite(res);
		}
		CustomNpcs.debugData.end(null);
	}

	@SubscribeEvent
	public void npcGuiScreenMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
		npcMouseInput(Mouse.getEventButton(), Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventDX(), Mouse.getEventDY(), Mouse.getEventDWheel(), Mouse.getEventButtonState());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void npcMouseInput(MouseEvent event) {
		npcMouseInput(event.getButton(), event.getX(), event.getY(), event.getDx(), event.getDy(), event.getDwheel(), event.isButtonstate());
	}

	public void npcMouseInput(int key, int x, int y, int dx, int dy, int dWheel, boolean isDown) {
		CustomNpcs.debugData.start(null);
		boolean isCtrlPressed = GuiScreen.isCtrlKeyDown();
		boolean isShiftPressed = GuiScreen.isShiftKeyDown();
		boolean isAltPressed = GuiScreen.isAltKeyDown();
		boolean isMetaPressed = Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220); // Windows and Command
		IPlayer<?> iPlayer = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(Minecraft.getMinecraft().player);
		if (key < 0) {
			Event event = new PlayerEvent.MouseMoveEvent(iPlayer, x, y, dx, dy, dWheel, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.MOUSE_MOVE, event);
			CustomNpcs.debugData.end(null);
			return;
		}
		Event event = new PlayerEvent.KeyPressedEvent(iPlayer, key, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed);
		EventHooks.onEvent(ScriptController.Instance.clientScripts, isDown ? EnumScriptType.MOUSE_DOWN : EnumScriptType.MOUSE_UP, event);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, key, isDown, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed, dWheel);
		if (isDown) {
			ClientProxy.playerData.hud.mousePress.add(key);
		} else {
			if (ClientProxy.playerData.hud.hasMousePress(key)) {
				ClientProxy.playerData.hud.mousePress.remove((Integer) key);
			}
		}
		CustomNpcs.debugData.end(null);
	}

}
