package noppes.npcs.client;

import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
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
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class ClientTickHandler {
	
	private boolean otherContainer;
	private World prevWorld;
	private Map<String, ISound> nowPlayingSounds;

	public ClientTickHandler() {
		this.otherContainer = false;
		this.nowPlayingSounds = Maps.<String, ISound>newHashMap();
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
		int key = event.getButton();
		if (key == -1) { return; }
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
		if (Minecraft.getMinecraft().currentScreen==null) {
			boolean isCtrlPressed = ClientProxy.playerData.hud.hasOrKeysPressed(157, 29);
			boolean isShiftPressed = ClientProxy.playerData.hud.hasOrKeysPressed(54, 42);
			boolean isAltPressed = ClientProxy.playerData.hud.hasOrKeysPressed(184, 56);
			boolean isMetaPressed = ClientProxy.playerData.hud.hasOrKeysPressed(220, 219);
			boolean isDown = event.isButtonstate();
			if (isDown) { ClientProxy.playerData.hud.mousePress.add(key); }
			else {
				if (ClientProxy.playerData.hud.hasMousePress(key)) { ClientProxy.playerData.hud.mousePress.remove((Integer)key); }
			}
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, key, isDown, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
		} else if (ClientProxy.playerData.hud.mousePress.size()>0) {
			ClientProxy.playerData.hud.mousePress.clear();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, -1);
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
	}

	@SubscribeEvent
	public void npcOnLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getHand() != EnumHand.MAIN_HAND) { return; }
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
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
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
		SoundManager sm = ObfuscationHelper.getValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), SoundManager.class);
		Map<String, ISound> playingSounds = ObfuscationHelper.getValue(SoundManager.class, sm, 8);
		for (String uuid : playingSounds.keySet()) { // is played
			if (!this.nowPlayingSounds.containsKey(uuid) || !this.nowPlayingSounds.containsValue(playingSounds.get(uuid))) {
				ISound sound = playingSounds.get(uuid);
				this.nowPlayingSounds.put(uuid, playingSounds.get(uuid));
				Client.sendData(EnumPacketServer.PlaySound, sound.getSound().getSoundLocation(), sound.getSoundLocation(), sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getVolume(), sound.getPitch());
			}
		}
		List<String> del = Lists.newArrayList();
		for (String uuid : this.nowPlayingSounds.keySet()) { // is stoped
			if (!playingSounds.containsKey(uuid) || !playingSounds.containsValue(this.nowPlayingSounds.get(uuid))) {
				ISound sound = this.nowPlayingSounds.get(uuid);
				Client.sendData(EnumPacketServer.StopSound, sound.getSound().getSoundLocation(), sound.getSoundLocation(), sound.getCategory().getName(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getVolume(), sound.getPitch());
				del.add(uuid);
			}
		}
		for (String uuid : del) { this.nowPlayingSounds.remove(uuid); }
		
		if (CustomNpcs.ticks % 10 == 0) {
			MarcetController.getInstance().updateTime();
		}
		if (mc.currentScreen!=null) {
			if (ClientProxy.playerData.hud.keyPress.size()>0) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, -1);
				ClientProxy.playerData.hud.keyPress.clear();
			}
			if (ClientProxy.playerData.hud.mousePress.size()>0) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, -1);
				ClientProxy.playerData.hud.mousePress.clear();
			}
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
		if (mc.currentScreen==null) {
			boolean isCtrlPressed = ClientProxy.playerData.hud.hasOrKeysPressed(157, 29);
			boolean isShiftPressed = ClientProxy.playerData.hud.hasOrKeysPressed(54, 42);
			boolean isAltPressed = ClientProxy.playerData.hud.hasOrKeysPressed(184, 56);
			boolean isMetaPressed = ClientProxy.playerData.hud.hasOrKeysPressed(220, 219);
			boolean isDown = Keyboard.getEventKeyState();
			int key = Keyboard.getEventKey();
			if (isDown) { ClientProxy.playerData.hud.keyPress.add(key); }
			else {
				if (ClientProxy.playerData.hud.hasOrKeysPressed(key)) { ClientProxy.playerData.hud.keyPress.remove((Integer)key); }
			}
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, key, isDown, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.IsMoved, ClientProxy.playerData.hud.hasOrKeysPressed(ClientProxy.frontButton.getKeyCode(), ClientProxy.backButton.getKeyCode(), ClientProxy.leftButton.getKeyCode(), ClientProxy.rightButton.getKeyCode()));
		} else if (ClientProxy.playerData.hud.keyPress.size()>0) {
			ClientProxy.playerData.hud.keyPress.clear();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, -1);
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_npcOnKey");
	}

	@SubscribeEvent
	public void testingCode(LivingEvent.LivingJumpEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (!(entity instanceof EntityPlayerMP) || !CustomNpcs.VerboseDebug) { return; }
		//TempClass.run((EntityPlayerSP) entity);
		
		/*try {
			Class<?> cl = Class.forName("net.minecraft.util.EnumParticleTypes");
			System.out.println("Enum class: "+cl);
			for (Field f : cl.getDeclaredFields()) {
				if (!f.getType().isInterface()) { continue; }
				System.out.println("Field name: "+f.getName()+"; type: "+f.getType());
				try {
					if (!f.isAccessible()) { f.setAccessible(true); }
					Map<?, ?> map = (Map<?, ?>) f.get(cl);
					for (Entry<?, ?> entry : map.entrySet()) {
						System.out.println("Field map Key: "+entry.getKey().getClass());
						break;
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch (ClassNotFoundException e) { e.printStackTrace(); }*/
		
		/*// Placer
		int sx = 4952, sy = 160, sz = 4952, cx = 0, cz = 0, i = 0;
		long t = System.currentTimeMillis();
		System.out.println("size: "+(97*97));
		Block block = Block.REGISTRY.getObject(new ResourceLocation(CustomNpcs.MODID, "custom_block_9"));
		System.out.println("block_9: "+block);
		if (block==null) { return; }
		while(cz<97) {
			while(cx<97) {
				IBlockState place = block.getDefaultState();
				//if (cz>2 && cz<95 && cx>2 && cx<95) { place = Blocks.AIR.getDefaultState(); }
				entity.world.setBlockState(new BlockPos(sx+cx, sy, sz+cz), Blocks.AIR.getDefaultState());
				i++;
				cx ++;
			}
			cz ++;
			cx = 0;
			System.out.println("z["+cz+"]: "+(sz + cz));
		}
		System.out.println("total["+i+"]: "+AdditionalMethods.ticksToElapsedTime(t-System.currentTimeMillis(), true, false, false));*/
	}

}
