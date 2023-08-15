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
import net.minecraft.client.entity.EntityPlayerSP;
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
			if (isDown) {
				ClientProxy.playerData.hud.keyPress.add(key);
				ClientProxy.pressed(key);
			}
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
		if (!(entity instanceof EntityPlayer)) { return; }
		
		if (entity instanceof EntityPlayerMP) {
			//Server.sendData((EntityPlayerMP) entity, EnumPacketClient.SYNC_END, 10, KeyController.getInstance().getNBT());
			//System.out.println("Server: "+entity);
		}
		if (!(entity instanceof EntityPlayerSP)) { return; }
		
		/*File dir = CustomNpcs.getWorldSaveDirectory();
		if (dir.exists()) {
			dir = dir.getParentFile();
			File f = new File(dir, "level.dat");
			try {
				NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(f));
				NBTTagList list = nbt.getCompoundTag("FML").getCompoundTag("Registries").getCompoundTag("minecraft:blocks").getTagList("ids", 10);
				NBTTagList newList = new NBTTagList();
				for (NBTBase tag : list) {
					if (((NBTTagCompound) tag).getString("K").equals("customnpcs:custom_fasingblockexample") ||
							((NBTTagCompound) tag).getString("K").equals("customnpcs:custom_flat_lamp_it") ||
							((NBTTagCompound) tag).getString("K").equals("customnpcs:custom_switch_0") ||
							((NBTTagCompound) tag).getString("K").equals("customnpcs:custom_mini_free")) {
						System.out.println("found: "+((NBTTagCompound) tag).getString("K"));
						continue;
					}
					newList.appendTag(tag);
				}
				if (list.tagCount()!=newList.tagCount()) {
					nbt.getCompoundTag("FML").getCompoundTag("Registries").getCompoundTag("minecraft:blocks").setTag("ids", newList);
					System.out.println("list: "+list.toString().length()+" // "+newList.toString().length());
					CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(f));
					System.out.println("save:");
				}
			}
			catch (IOException e) { }
		}*/
		
		//System.out.println("Client: "+entity);
		
		//TempClass.run((EntityPlayerSP) entity, 3);
		//System.out.println("Test: ");
		
		/*for (KeyBinding kb : Minecraft.getMinecraft().gameSettings.keyBindings) {
			System.out.println("key: "+kb.getDisplayName()+" _ "+kb.getKeyCategory()+" = "+kb.getKeyCode()+" // "+kb.getKeyModifier()+" /// "+kb.getKeyConflictContext().getClass()+"["+kb.getKeyConflictContext()+"]");
		}*/
		
		/*String v = Integer.toHexString(CustomNpcs.mainColor).toUpperCase();
		int i = (int) Long.parseLong(v, 16);
		System.out.println("D: "+(int) CustomNpcs.mainColor+" to hex: \""+v+"\" next: "+i);*/
		
		/*System.out.println("try found chests:");
		Iterator<Item> iter = Item.REGISTRY.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Item item = iter.next();
			if (item.getRegistryName().toString().toLowerCase().indexOf("chest")!=-1 && item.getRegistryName().toString().toLowerCase().indexOf("chestplate")==-1) {
				System.out.println("found["+i+"] - "+item.getRegistryName());
			}
			i++;
		}
		System.out.println("total["+i+"];");*/
		
		/*ItemStack stack = entity.getHeldItemMainhand();
		Item item = entity.getHeldItemMainhand().getItem();
		System.out.println("stack: "+stack.getTagCompound());
		System.out.println("item: "+item.getClass().getName()+" - "+item.getRegistryName());*/
		//System.out.println("item: "+entity.getHeldItemMainhand().getItem().onItemUse((EntityPlayer) entity, entity.world, new BlockPos(282, 60, 220), EnumHand.MAIN_HAND, EnumFacing.UP, 0.5f, 0.5f, 0.5f));
		
		//((EntityPlayerMP) entity).setPositionAndUpdate(5001.5d, 151.5d, 5001.5d);
		
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
		
		/*// Simple Placer
		int d = 128, sx = -5000 - d, sy = 0, sz = 5000 - d, cx = 0, cy = 0, cz = 0, i = 0;
		int nx = d*2, ny=9, nz = d*2;
		long t = System.currentTimeMillis();
		System.out.println("size: "+(nx*nz));
		//Block block = Block.REGISTRY.getObject(new ResourceLocation(CustomNpcs.MODID, "custom_block_9"));
		//System.out.println("block_9: "+block);
		//if (block==null) { return; }
		while(cy<ny) {
			while(cz<nz) {
				while(cx<nx) {
					//IBlockState place = block.getDefaultState();
					//if (cz>2 && cz<95 && cx>2 && cx<95) { place = Blocks.AIR.getDefaultState(); }
					entity.world.setBlockState(new BlockPos(sx+cx, sy, sz+cz), Blocks.AIR.getDefaultState());
					i++;
					cx ++;
					//if (sx+cx == -5000) { System.out.println("xyz["+cx+", "+cy+", "+cz+"]: ["+(sx+cx)+", "+sy+", "+(sz+cz)+"]"); }
				}
				cz ++;
				cx = 0;
				//System.out.println("y["+cy+"]: "+(sy + cy)+"; z["+cz+"]: "+(sz + cz)+"; ["+sx+"/"+(sx+nx)+"]");
			}
			cy ++;
			cz = 0;
		}
		System.out.println("total["+i+"]: "+AdditionalMethods.ticksToElapsedTime(t-System.currentTimeMillis(), true, false, false));
		*/
		
		/*// Radius Placer
		int radius = 64, sx = -5000 - radius, sy = 3, sz = 5000 - radius, cx = 0 - radius, cz = 0 - radius, i = 0;
		int nx = radius, nz = radius;
		System.out.println("size: "+(4 * radius * radius)+" / "+Math.round(Math.PI * Math.pow(radius, 2.0d)));
		long t = System.currentTimeMillis();
		while(cz<nz) {
			while(cx<nx) {
				double tr = Math.sqrt(Math.pow((double) cx + 0.5d, 2.0d) +Math.pow((double) cz + 0.5d, 2.0d));
				if (tr>radius) { cx ++; continue; }
				int cy = tr>radius/3 ? tr>radius*2/3 ? 2 : 1 : 0;
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy, sz+cz+radius), Blocks.BEDROCK.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+1, sz+cz+radius), Blocks.STONE.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+2, sz+cz+radius), Blocks.STONE.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+3, sz+cz+radius), Blocks.STONE.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+4, sz+cz+radius), Blocks.DIRT.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+5, sz+cz+radius), Blocks.DIRT.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+6, sz+cz+radius), Blocks.DIRT.getDefaultState());
				entity.world.setBlockState(new BlockPos(sx+cx+radius, sy+cy+7, sz+cz+radius), Blocks.GRASS.getDefaultState());
				i++;
				cx ++;
			}
			cz ++;
			cx = 0 - radius;
			System.out.println("z["+cz+"]: "+(sz + cz));
		}
		System.out.println("total["+i+"]: "+AdditionalMethods.ticksToElapsedTime(t-System.currentTimeMillis(), true, false, false));
		*/
	}

}
