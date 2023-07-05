package noppes.npcs.api.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {
	
	public static class InitEvent extends ForgeEvent {
		public InitEvent() {
			super(null);
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
		if (event!=null) {
			// entity
			Class<?> sp = event.getClass();
			while(this.entity==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType().getName().indexOf(".EntityPlayer")!=-1 || f.getType() != Entity.class || f.getType() != EntityLiving.class || f.getType() != EntityLivingBase.class) { continue; }
					try {
						f.setAccessible(true);
						this.entity = NpcAPI.Instance().getIEntity((Entity) f.get(event));
					} catch (Exception e) { }
				}
				sp = sp.getSuperclass();
			}
			if (this.entity==null) {
				sp = event.getClass();
				while(this.entity==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType().getName().indexOf(".EntityPlayer")!=-1 || m.getReturnType() != Entity.class || m.getReturnType() != EntityLiving.class || m.getReturnType() != EntityLivingBase.class || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							this.entity = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
						} catch (Exception e) { }
					}
					sp = sp.getSuperclass();
				}
			}
			
			// player
			sp = event.getClass();
			while(this.player==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType().getName().indexOf(".EntityPlayer")==-1) { continue; }
					try {
						f.setAccessible(true);
						this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) f.get(event));
					} catch (Exception e) { }
					if (event instanceof ArrowLooseEvent) { System.out.println("Field: "+f.getName()+" - "+this.player); }
				}
				sp = sp.getSuperclass();
			}
			if (this.player==null) {
				sp = event.getClass();
				while(this.player==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType().getName().indexOf(".EntityPlayer")==-1 || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
						} catch (Exception e) { }
						if (event instanceof ArrowLooseEvent) { System.out.println("Method: "+m.getName()+" - "+this.player); }
					}
					sp = sp.getSuperclass();
				}
			}
			if (this.player==null && this.entity!=null && this.entity.getMCEntity() instanceof EntityPlayer) {
				this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) this.entity.getMCEntity());
			}
			
			// NPC
			sp = event.getClass();
			while(this.npc==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType() != EntityNPCInterface.class) { continue; }
					try {
						f.setAccessible(true);
						this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((EntityNPCInterface) f.get(event));
					} catch (Exception e) { }
				}
				sp = sp.getSuperclass();
			}
			if (this.npc==null) {
				sp = event.getClass();
				while(this.npc==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType() != EntityNPCInterface.class || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((Entity) m.invoke(event));
						} catch (Exception e) { }
					}
					sp = sp.getSuperclass();
				}
			}
			if (this.npc==null && this.entity!=null && this.entity.getMCEntity() instanceof EntityNPCInterface) {
				this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((EntityNPCInterface) this.entity.getMCEntity());
			}
			if (this.entity==null && this.player!=null) { this.entity = this.player; }
			if (this.entity==null && this.npc!=null) { this.entity = this.npc; }
			
			// world
			sp = event.getClass();
			while(this.world==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType() != WorldServer.class || f.getType() != World.class) { continue; }
					try {
						f.setAccessible(true);
						this.world = NpcAPI.Instance().getIWorld((WorldServer) f.get(event));
					} catch (Exception e) { }
				}
				sp = sp.getSuperclass();
			}
			if (this.world==null) {
				sp = event.getClass();
				while(this.world==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType() != WorldServer.class || m.getReturnType() != World.class || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							this.world = NpcAPI.Instance().getIWorld((WorldServer) m.invoke(event));
						} catch (Exception e) { }
					}
					sp = sp.getSuperclass();
				}
			}
			if (this.world==null && this.entity!=null) { this.world = this.entity.getWorld(); }
			if (this.world==null && this.player!=null) { this.world = this.player.getWorld(); }
			if (this.world==null && this.npc!=null) { this.world = this.npc.getWorld(); }
			
			// pos
			sp = event.getClass();
			while(this.pos==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType() != BlockPos.class) { continue; }
					try {
						f.setAccessible(true);
						BlockPos bp = (BlockPos) f.get(event);
						this.pos = NpcAPI.Instance().getIPos(bp.getX(), bp.getY(), bp.getZ());
					} catch (Exception e) { }
				}
				sp = sp.getSuperclass();
			}
			if (this.pos==null) {
				sp = event.getClass();
				while(this.pos==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType() != BlockPos.class || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							BlockPos bp = (BlockPos) m.invoke(event);
							this.pos = NpcAPI.Instance().getIPos(bp.getX(), bp.getY(), bp.getZ());
						} catch (Exception e) { }
					}
					sp = sp.getSuperclass();
				}
			}
			if (this.pos==null && this.entity!=null) { this.pos = this.entity.getPos(); }
			if (this.pos==null && this.player!=null) { this.pos = this.player.getPos(); }
			if (this.pos==null && this.npc!=null) { this.pos = this.npc.getPos(); }
			
			// stack
			sp = event.getClass();
			while(this.stack==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
				for (Field f : sp.getDeclaredFields()) {
					if (f.getType() != ItemStack.class) { continue; }
					try {
						f.setAccessible(true);
						this.stack = NpcAPI.Instance().getIItemStack((ItemStack) f.get(event));
					} catch (Exception e) { }
				}
				sp = sp.getSuperclass();
			}
			if (this.stack==null) {
				sp = event.getClass();
				while(this.stack==null && sp.getSuperclass()!=null && sp.getSuperclass()!=Event.class) {
					for (Method m : sp.getDeclaredMethods()) {
						if (m.getReturnType() != ItemStack.class || m.getParameterCount()!=0) { continue; }
						try {
							m.setAccessible(true);
							this.stack = NpcAPI.Instance().getIItemStack((ItemStack) m.invoke(event));
						} catch (Exception e) { }
					}
					sp = sp.getSuperclass();
				}
			}
			
			// block
			if (this.world!=null && this.pos!=null) { this.block = NpcAPI.Instance().getIBlock(this.world.getMCWorld(), this.pos.getMCBlockPos()); }
		}
	}

}
