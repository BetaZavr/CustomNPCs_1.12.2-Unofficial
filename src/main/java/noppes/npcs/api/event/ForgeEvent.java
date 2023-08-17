package noppes.npcs.api.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.BlockFluidContainerWrapper;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.api.wrapper.BlockScriptedWrapper;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {
	
	public static class InitEvent extends ForgeEvent {
		public InitEvent() {
			super(null);
		}
	}
	
	public static class SoundTickEvent extends ForgeEvent {
		
		public int tick, ticks;
		public String name, resource;
		public IPos pos;
		public float volume, pitch;
		public ISound music;

		public SoundTickEvent(ISound music, int tick) {
			super(null);
			this.tick = tick;
			this.music = music;
			if (music!=null) {
				this.ticks = 0;
				this.name = music.getSoundLocation().toString();
				this.volume = music.getVolume();
				this.pitch = music.getPitch();
				this.pos = NpcAPI.Instance().getIPos(music.getXPosF(), music.getYPosF(), music.getZPosF());
				if (music.getSound()!=null) { this.resource = music.getSound().getSoundLocation().toString(); }
			} else {
				this.ticks = 0;
				this.name = "null";
				this.volume = 1.0f;
				this.pitch = 1.0f;
				this.pos = NpcAPI.Instance().getIPos(0, 0, 0);
			}
		}
	}
	
	public NpcAPI API;
	public Event event;
	public IEntity<?> entity;
	public IPlayer<?> player;
	public ICustomNpc<?> npc;
	public IWorld world;
	public IPos pos;
	public IBlock block;
	public IItemStack stack;

	public ForgeEvent(Event event) {
		this.API = NpcAPI.Instance();
		this.event = event;
		if (event==null || CustomNpcs.simplifiedForgeEvents) { return; }
		// Common
		Block bl = null;
		IBlockState st = null;
		Class<?> sp = event.getClass();
		while(this.entity==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
			for (Field f : sp.getDeclaredFields()) {
				if (this.entity==null && f.getType().getSimpleName().indexOf("EntityPlayer")==-1 && (f.getType() == Entity.class || f.getType() == EntityLiving.class || f.getType() == EntityLivingBase.class)) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.entity = NpcAPI.Instance().getIEntity((Entity) f.get(event));
					} catch (Exception e) { }
				}
				if (this.player==null && f.getType().getSimpleName().indexOf("EntityPlayer")!=-1) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((Entity) f.get(event));
					} catch (Exception e) { }
				}
				if (this.npc==null && f.getType() == EntityNPCInterface.class) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((EntityNPCInterface) f.get(event));
					} catch (Exception e) { }
				}
				if (this.world == null && (f.getType().getSimpleName().equals("WorldClient") || f.getType() == WorldServer.class || f.getType() == World.class)) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.world = NpcAPI.Instance().getIWorld((World) f.get(event));
					} catch (Exception e) { }
				}
				if (this.pos == null && f.getType() == BlockPos.class) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.pos = NpcAPI.Instance().getIPos((BlockPos) f.get(event));
					} catch (Exception e) { }
				}
				if (this.stack == null && f.getType() == ItemStack.class) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						this.stack = NpcAPI.Instance().getIItemStack((ItemStack) f.get(event));
					} catch (Exception e) { }
				}
				if ((bl==null || st==null) && (f.getType() == Block.class || f.getType() == IBlockState.class)) {
					try {
						if (!f.isAccessible()) { f.setAccessible(true); }
						if (f.getType() == Block.class) { bl = (Block) f.get(event); }
						else { st = (IBlockState) f.get(event); }
					} catch (Exception e) { }
				}
			}
			for (Method m : sp.getDeclaredMethods()) {
				if (m.getParameterCount()!=0) { continue; }
				if (this.entity==null && m.getReturnType().getName().indexOf(".EntityPlayer")==-1 && (m.getReturnType() == Entity.class || m.getReturnType() == EntityLiving.class || m.getReturnType() == EntityLivingBase.class)) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.entity = NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { }
				}

				if (this.player==null && m.getReturnType().getName().indexOf(".EntityPlayer")!=-1) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { }
				}
				if (this.npc==null && m.getReturnType() == EntityNPCInterface.class) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((EntityNPCInterface) m.invoke(event));
					} catch (Exception e) { }
				}
				if (this.world == null && (m.getReturnType().getSimpleName().equals("WorldClient") || m.getReturnType() == WorldServer.class || m.getReturnType() == World.class)) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.world = NpcAPI.Instance().getIWorld((World) m.invoke(event));
					} catch (Exception e) { }
				}
				if (this.pos == null && m.getReturnType() == BlockPos.class) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.pos = NpcAPI.Instance().getIPos((BlockPos) m.invoke(event));
					} catch (Exception e) { }
				}
				if (this.stack == null && m.getReturnType() == ItemStack.class) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						this.stack = NpcAPI.Instance().getIItemStack((ItemStack) m.invoke(event));
					} catch (Exception e) { }
				}
				if ((bl==null || st==null) && (m.getReturnType() == Block.class || m.getReturnType() == IBlockState.class)) {
					try {
						if (!m.isAccessible()) { m.setAccessible(true); }
						if (m.getReturnType() == Block.class) { bl = (Block) m.invoke(event); }
						else { st = (IBlockState) m.invoke(event); }
					} catch (Exception e) { }
				}
			}
			sp = sp.getSuperclass();
		}
		
		// player
		if (this.player==null && this.entity!=null && this.entity.getMCEntity() instanceof EntityPlayer) { this.player = (IPlayer<?>) this.entity; }
		if (this.player==null && CustomNpcs.proxy.getPlayer()!=null) { this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity(CustomNpcs.proxy.getPlayer()); }
		
		// NPC
		if (this.npc==null && this.entity!=null && this.entity.getMCEntity() instanceof EntityNPCInterface) { this.npc = (ICustomNpc<?>) this.entity; }
		if (this.entity==null && this.player!=null) { this.entity = this.player; }
		if (this.entity==null && this.npc!=null) { this.entity = this.npc; }
		
		// world
		if (this.world==null && this.entity!=null) { this.world = this.entity.getWorld(); }
		if (this.world==null && this.player!=null) { this.world = this.player.getWorld(); }
		if (this.world==null && this.npc!=null) { this.world = this.npc.getWorld(); }
		
		// pos
		if (this.pos==null && this.entity!=null) { this.pos = this.entity.getPos(); }
		if (this.pos==null && this.player!=null) { this.pos = this.player.getPos(); }
		if (this.pos==null && this.npc!=null) { this.pos = this.npc.getPos(); }
		
		// block
		this.getBlock(bl, st);
	}

	private void getBlock(Block block, IBlockState state) {
		if ((block == null && state==null) || this.pos == null || this.pos.getMCBlockPos() == null || this.world==null || this.world.getMCWorld()==null) { return; }
		String key;
		BlockPos p = this.pos.getMCBlockPos();
		World w = this.world.getMCWorld();
		if (state != null) {
			block = state.getBlock();
			key = state.toString() + p.toString();
		} else {
			key = block.getDefaultState().toString() + p.toString();
		}
		if (!BlockWrapper.blockCache.containsKey(key)) {
			if (block instanceof BlockScripted) { this.block = new BlockScriptedWrapper(w, block, p); }
			else if (block instanceof BlockScriptedDoor) { this.block = new BlockScriptedDoorWrapper(w, block, p); }
			else if (block instanceof BlockFluidBase) { this.block = new BlockFluidContainerWrapper(w, block, p); }
			else { this.block = new BlockWrapper(this.world.getMCWorld(), block, p); }
			BlockWrapper.blockCache.put(key, (BlockWrapper) this.block);
		}
		if (this.block != null) { ((BlockWrapper) this.block).setTile(w.getTileEntity(p)); }
	}
	
}
