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
import noppes.npcs.api.wrapper.BlockWrapper;
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

		public SoundTickEvent(IPlayer<?> playerIn, String nameIn, String resourceIn, IPos posIn, float volumeIn, float pitchIn, float milliSecondsIn, float totalSecondIn) {
			super(null);
			milliSeconds = milliSecondsIn;
			totalSecond = totalSecondIn;
			name = nameIn;
			resource = resourceIn;
			volume = volumeIn;
			pitch = pitchIn;

			pos = posIn;
			player = playerIn;
		}
	}

	public Event event;
	public IEntity<?> entity;
	public IPlayer<?> player;
	public ICustomNpc<?> npc;
	public IWorld world;
	public IPos pos;
	public IBlock block;
	public IItemStack stack;

	public ForgeEvent(Event eventIn) {
		super();
		event = eventIn;
		if (!CustomNpcs.SimplifiedForgeEvents) { createData(); }
	}

	public void createData() {
		if (event == null) { return; }
		if (API == null) { return; }
		// Common
		Block bl = null;
		IBlockState st = null;
		Class<?> sp = event.getClass();
		while (entity == null && sp.getSuperclass() != null && sp.getSuperclass() != Event.class) {
			for (Field f : sp.getDeclaredFields()) {
				if (entity == null && !f.getType().getSimpleName().contains("EntityPlayer")
						&& (f.getType() == Entity.class || f.getType() == EntityLiving.class
						|| f.getType() == EntityLivingBase.class)) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						entity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity((Entity) f.get(event));
					}
					catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (player == null && f.getType().getSimpleName().contains("EntityPlayer")) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity((Entity) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (npc == null && f.getType() == EntityNPCInterface.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						npc = (ICustomNpc<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity((EntityNPCInterface) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (world == null && (f.getType().getSimpleName().equals("WorldClient")
						|| f.getType() == WorldServer.class || f.getType() == World.class)) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						world = Objects.requireNonNull(NpcAPI.Instance()).getIWorld((World) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (pos == null && f.getType() == BlockPos.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos((BlockPos) f.get(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (stack == null && f.getType() == ItemStack.class) {
					try {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						stack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack((ItemStack) f.get(event));
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
				if (entity == null && !m.getReturnType().getName().contains(".EntityPlayer")
						&& (m.getReturnType() == Entity.class || m.getReturnType() == EntityLiving.class
						|| m.getReturnType() == EntityLivingBase.class)) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						entity = API.getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}

				if (player == null && m.getReturnType().getName().contains(".EntityPlayer")) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						player = (IPlayer<?>) API.getIEntity((EntityPlayer) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (npc == null && m.getReturnType() == EntityNPCInterface.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						npc = (ICustomNpc<?>) API.getIEntity((EntityNPCInterface) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (world == null && (m.getReturnType().getSimpleName().equals("WorldClient")
						|| m.getReturnType() == WorldServer.class || m.getReturnType() == World.class)) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						world = API.getIWorld((World) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (pos == null && m.getReturnType() == BlockPos.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						pos = API.getIPos((BlockPos) m.invoke(event));
					} catch (Exception e) { LogWriter.debug("Forge event error:" + e); }
				}
				if (stack == null && m.getReturnType() == ItemStack.class) {
					try {
						if (!m.isAccessible()) {
							m.setAccessible(true);
						}
						stack = API.getIItemStack((ItemStack) m.invoke(event));
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
		if (player == null && entity != null && entity.getMCEntity() instanceof EntityPlayer) {
			player = (IPlayer<?>) entity;
		}
		if (player == null && CustomNpcs.proxy.getPlayer() != null) {
			player = (IPlayer<?>) API.getIEntity(CustomNpcs.proxy.getPlayer());
		}

		// NPC
		if (npc == null && entity != null && entity.getMCEntity() instanceof EntityNPCInterface) {
			npc = (ICustomNpc<?>) entity;
		}
		if (entity == null && player != null) {
			entity = player;
		}
		if (entity == null && npc != null) {
			entity = npc;
		}

		// world
		if (world == null && entity != null) {
			world = entity.getWorld();
		}
		if (world == null && player != null) {
			world = player.getWorld();
		}
		if (world == null && npc != null) {
			world = npc.getWorld();
		}

		// pos
		if (pos == null && entity != null) {
			pos = entity.getPos();
		}
		if (pos == null && player != null) {
			pos = player.getPos();
		}
		if (pos == null && npc != null) {
			pos = npc.getPos();
		}

		// block
		getBlock(bl, st);
	}

	private void getBlock(Block block, IBlockState state) {
		if ((block == null && state == null) || pos == null || pos.getMCBlockPos() == null || world == null || world.getMCWorld() == null) {
			return;
		}
		if (state != null) {
			this.block = BlockWrapper.createNew(world.getMCWorld(), pos.getMCBlockPos(), state);
		}
	}

}
