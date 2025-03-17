package noppes.npcs.api.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import noppes.npcs.LogWriter;
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
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {

	public static class EnterToRegion extends CustomNPCsEvent {

		public Entity entity;
		public Zone3D region;

		public EnterToRegion(Entity entityIn, Zone3D zone) {
			super();
			entity = entityIn;
			region = zone;
		}

	}

	public static class LeaveRegion extends CustomNPCsEvent {

		public Entity entity;
		public Zone3D region;

		public LeaveRegion(Entity entityIn, Zone3D zone) {
			super();
			entity = entityIn;
			region = zone;
		}

	}


	public static class InitEvent extends ForgeEvent {

		public InitEvent() {
			super(null);
		}

	}

	@Cancelable
	public static class SoundTickEvent extends ForgeEvent {

		public float milliSeconds, totalSecond;
		public String name, resource;
		public float volume, pitch;

		public SoundTickEvent(IPlayer<?> player, String name, String resource, IPos pos, float volume, float pitch,
				float milliSeconds, float totalSecond) {
			super(null);
			this.milliSeconds = milliSeconds;
			this.totalSecond = totalSecond;
			this.name = name;
			this.resource = resource;
			this.volume = volume;
			this.pitch = pitch;

			this.pos = pos;
			this.player = player;
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
		if (event == null || CustomNpcs.SimplifiedForgeEvents) {
			return;
		}
		createData();
	}

	public void createData() {
		// Common
		Block bl = null;
		IBlockState st = null;
		Class<?> sp = event.getClass();
		while (this.entity == null && sp.getSuperclass() != null && sp.getSuperclass() != Event.class) {
			for (Field f : sp.getDeclaredFields()) {
				if (this.entity == null && !f.getType().getSimpleName().contains("EntityPlayer")
						&& (f.getType() == Entity.class || f.getType() == EntityLiving.class
						|| f.getType() == EntityLivingBase.class)) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.entity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity((Entity) f.get(event));
					}
					catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.player == null && f.getType().getSimpleName().contains("EntityPlayer")) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity((Entity) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.npc == null && f.getType() == EntityNPCInterface.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.npc = (ICustomNpc<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity((EntityNPCInterface) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.world == null && (f.getType().getSimpleName().equals("WorldClient")
						|| f.getType() == WorldServer.class || f.getType() == World.class)) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.world = Objects.requireNonNull(NpcAPI.Instance()).getIWorld((World) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.pos == null && f.getType() == BlockPos.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos((BlockPos) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.stack == null && f.getType() == ItemStack.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						this.stack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack((ItemStack) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if ((bl == null || st == null) && (f.getType() == Block.class || f.getType() == IBlockState.class)) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						if (f.getType() == Block.class) {
							bl = (Block) f.get(event);
						} else {
							st = (IBlockState) f.get(event);
						}
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
			}
			for (Method m : sp.getDeclaredMethods()) {
				if (m.getParameterCount() != 0) {
					continue;
				}
				if (this.entity == null && !m.getReturnType().getName().contains(".EntityPlayer")
						&& (m.getReturnType() == Entity.class || m.getReturnType() == EntityLiving.class
						|| m.getReturnType() == EntityLivingBase.class)) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.entity = NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}

				if (this.player == null && m.getReturnType().getName().contains(".EntityPlayer")) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.npc == null && m.getReturnType() == EntityNPCInterface.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.npc = (ICustomNpc<?>) NpcAPI.Instance().getIEntity((EntityNPCInterface) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.world == null && (m.getReturnType().getSimpleName().equals("WorldClient")
						|| m.getReturnType() == WorldServer.class || m.getReturnType() == World.class)) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.world = NpcAPI.Instance().getIWorld((World) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.pos == null && m.getReturnType() == BlockPos.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.pos = NpcAPI.Instance().getIPos((BlockPos) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (this.stack == null && m.getReturnType() == ItemStack.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						this.stack = NpcAPI.Instance().getIItemStack((ItemStack) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if ((bl == null || st == null)
						&& (m.getReturnType() == Block.class || m.getReturnType() == IBlockState.class)) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						if (m.getReturnType() == Block.class) {
							bl = (Block) m.invoke(event);
						} else {
							st = (IBlockState) m.invoke(event);
						}
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
			}
			sp = sp.getSuperclass();
		}

		// player
		if (this.player == null && this.entity != null && this.entity.getMCEntity() instanceof EntityPlayer) {
			this.player = (IPlayer<?>) this.entity;
		}
		if (this.player == null && CustomNpcs.proxy.getPlayer() != null) {
			this.player = (IPlayer<?>) NpcAPI.Instance().getIEntity(CustomNpcs.proxy.getPlayer());
		}

		// NPC
		if (this.npc == null && this.entity != null && this.entity.getMCEntity() instanceof EntityNPCInterface) {
			this.npc = (ICustomNpc<?>) this.entity;
		}
		if (this.entity == null && this.player != null) {
			this.entity = this.player;
		}
		if (this.entity == null && this.npc != null) {
			this.entity = this.npc;
		}

		// world
		if (this.world == null && this.entity != null) {
			this.world = this.entity.getWorld();
		}
		if (this.world == null && this.player != null) {
			this.world = this.player.getWorld();
		}
		if (this.world == null && this.npc != null) {
			this.world = this.npc.getWorld();
		}

		// pos
		if (this.pos == null && this.entity != null) {
			this.pos = this.entity.getPos();
		}
		if (this.pos == null && this.player != null) {
			this.pos = this.player.getPos();
		}
		if (this.pos == null && this.npc != null) {
			this.pos = this.npc.getPos();
		}

		// block
		this.getBlock(bl, st);
	}

	private void getBlock(Block block, IBlockState state) {
		if ((block == null && state == null) || this.pos == null || this.pos.getMCBlockPos() == null
				|| this.world == null || this.world.getMCWorld() == null) {
			return;
		}
		String key;
		BlockPos p = this.pos.getMCBlockPos();
		World w = this.world.getMCWorld();
		if (state != null) {
			block = state.getBlock();
			key = state + p.toString();
		} else {
			key = block.getDefaultState() + p.toString();
		}
		if (!BlockWrapper.blockCache.containsKey(key)) {
			if (block instanceof BlockScripted) {
				this.block = new BlockScriptedWrapper(w, block, p);
			} else if (block instanceof BlockScriptedDoor) {
				this.block = new BlockScriptedDoorWrapper(w, block, p);
			} else if (block instanceof BlockFluidBase) {
				this.block = new BlockFluidContainerWrapper(w, block, p);
			} else {
				this.block = new BlockWrapper(this.world.getMCWorld(), block, p);
			}
			BlockWrapper.blockCache.put(key, (BlockWrapper) this.block);
		}
		if (this.block != null) {
			((BlockWrapper) this.block).setTile(w.getTileEntity(p));
		}
	}

}
