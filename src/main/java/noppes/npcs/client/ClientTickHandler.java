package noppes.npcs.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.PlayerGameData;
import noppes.npcs.entity.EntityNPCInterface;

public class ClientTickHandler {
	
	private int buttonPressed;
	private long buttonTime;
	private int[] ignoreKeys;
	private boolean otherContainer;
	private World prevWorld;

	public ClientTickHandler() {
		this.otherContainer = false;
		this.buttonPressed = -1;
		this.buttonTime = 0L;
		this.ignoreKeys = new int[] { 157, 29, 54, 42, 184, 56, 220, 219 };
	}

	private boolean isIgnoredKey(int key) {
		for (int i : this.ignoreKeys) {
			if (i == key) {
				return true;
			}
		}
		return false;
	}


	@SubscribeEvent
	public void LivingUpdate(LivingUpdateEvent event) {
		if (!event.getEntity().world.isRemote || !(event.getEntity() instanceof EntityNPCInterface)) { return; }
		int dimID = Minecraft.getMinecraft().world.provider.getDimension();
		if (ClientProxy.notVisibleNPC.containsKey(dimID) && ClientProxy.notVisibleNPC.get(dimID).contains(event.getEntity().getUniqueID())) {
			Minecraft.getMinecraft().world.removeEntity(event.getEntity());
		}
	}
	
	@SubscribeEvent
	public void cnpcMouseInput(MouseEvent event) {
		if (event.getButton() == -1) {
			return;
		}
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
		PlayerGameData data = ClientProxy.playerData.game;
		boolean isDown = event.isButtonstate();
		boolean send = false;
		NBTTagList list = new NBTTagList();
		for (NBTBase k : data.mousePress) {
			if (event.getButton() == ((NBTTagInt) k).getInt()) {
				if (event.isButtonstate()) {
					list.appendTag(k);
				} else {
					send = true;
				}
				isDown = false;
			} else {
				list.appendTag(k);
			}
		}
		if (isDown) {
			list.appendTag(new NBTTagInt(event.getButton()));
			send = true;
		}
		if (send) {
			data.mousePress = list;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("keys", data.mousePress);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, compound);
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
	}

	@SubscribeEvent
	public void npcOnLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getHand() != EnumHand.MAIN_HAND) {
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.LeftClick, new Object[0]);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void npcOnClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_npcOnClientTick");
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && mc.player.openContainer instanceof ContainerPlayer) {
			if (this.otherContainer) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, new Object[0]);
				this.otherContainer = false;
			}
		} else {
			this.otherContainer = true;
		}
		++CustomNpcs.ticks;
		++RenderNPCInterface.LastTextureTick;
		if (this.prevWorld != mc.world) {
			this.prevWorld = mc.world;
			MusicController.Instance.stopMusic();
		}
		if (CustomNpcs.ticks % 10 == 0) {
			MarcetController.getInstance().updateTime();
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_npcOnClientTick");
	}

	@SubscribeEvent
	public void npcOnKey(InputEvent.KeyInputEvent event) {
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_npcOnKey");
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
				Client.sendData(EnumPacketServer.SceneReset, new Object[0]);
			}
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (ClientProxy.QuestLog.isPressed()) {
			if (mc.currentScreen == null) {
				NoppesUtil.openGUI((EntityPlayer) mc.player, new GuiQuestLog((EntityPlayer) mc.player));
			} else if (mc.currentScreen instanceof GuiQuestLog) {
				mc.setIngameFocus();
			}
		}
		int key = Keyboard.getEventKey();
		long time = Keyboard.getEventNanoseconds();
		boolean isDown = Keyboard.getEventKeyState();
		if (isDown) {
			if (!this.isIgnoredKey(key)) {
				this.buttonTime = time;
				this.buttonPressed = key;
				if (mc.currentScreen == null) {
					boolean isCtrlPressed = Keyboard.isKeyDown(157) || Keyboard.isKeyDown(29);
					boolean isShiftPressed = Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42);
					boolean isAltPressed = Keyboard.isKeyDown(184) || Keyboard.isKeyDown(56);
					boolean isMetaPressed = Keyboard.isKeyDown(220) || Keyboard.isKeyDown(219);
					NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyDown, key, isCtrlPressed, isShiftPressed, isAltPressed,
							isMetaPressed);
				}
			}
		} else {
			if (key == this.buttonPressed && time - this.buttonTime < 500000000L && mc.currentScreen == null) {
				boolean isCtrlPressed = Keyboard.isKeyDown(157) || Keyboard.isKeyDown(29);
				boolean isShiftPressed = Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42);
				boolean isAltPressed = Keyboard.isKeyDown(184) || Keyboard.isKeyDown(56);
				boolean isMetaPressed = Keyboard.isKeyDown(220) || Keyboard.isKeyDown(219);
				NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyUp, key, isCtrlPressed, isShiftPressed, isAltPressed,
						isMetaPressed);
			}
			this.buttonPressed = -1;
			this.buttonTime = 0L;
		}
		// New
		PlayerGameData data = ClientProxy.playerData.game;
		boolean send = false;
		NBTTagList list = new NBTTagList();
		for (NBTBase k : data.keyPress) {
			if (Keyboard.getEventKey() == ((NBTTagInt) k).getInt()) {
				if (Keyboard.getEventKeyState()) {
					list.appendTag(k);
				} else {
					send = true;
				}
				isDown = false;
			} else {
				list.appendTag(k);
			}
		}
		if (isDown) {
			list.appendTag(new NBTTagInt(Keyboard.getEventKey()));
			send = true;
		}
		if (send) {
			data.keyPress = list;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("keys", data.keyPress);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeysPressed, compound);
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.IsMoved, data.hasOrKeysPressed(
				new int[] { ClientProxy.frontButton.getKeyCode(),
						ClientProxy.backButton.getKeyCode(),
						ClientProxy.leftButton.getKeyCode(),
						ClientProxy.rightButton.getKeyCode() }
		));
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_npcOnKey");
	}

	@SubscribeEvent
	public void testingCode(LivingEvent.LivingJumpEvent event) {
		/*EntityLivingBase entity = event.getEntityLiving();
		if (!(entity instanceof EntityPlayerMP) || !CustomNpcs.VerboseDebug) { return; }*/
	}
	
}
