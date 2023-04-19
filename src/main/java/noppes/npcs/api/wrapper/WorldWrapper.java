package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import noppes.npcs.EventHooks;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.ObfuscationHelper;

public class WorldWrapper
implements IWorld {
	
	public static Map<String, Object> tempData = new HashMap<String, Object>();

	@Deprecated
	public static WorldWrapper createNew(WorldServer world) {
		return new WorldWrapper(world);
	}

	public IDimension dimension;
	private IData storeddata;
	private IData tempdata;

	public WorldServer world;

	private WorldWrapper(World world) {
		this.tempdata = new IData() {
			@Override
			public void clear() {
				WorldWrapper.tempData.clear();
			}

			@Override
			public Object get(String key) {
				return WorldWrapper.tempData.get(key);
			}

			@Override
			public String[] getKeys() {
				return WorldWrapper.tempData.keySet().toArray(new String[WorldWrapper.tempData.size()]);
			}

			@Override
			public boolean has(String key) {
				return WorldWrapper.tempData.containsKey(key);
			}

			@Override
			public void put(String key, Object value) {
				WorldWrapper.tempData.put(key, value);
			}

			@Override
			public void remove(String key) {
				WorldWrapper.tempData.remove(key);
			}
		};
		this.storeddata = new IData() {
			@Override
			public void clear() {
				ScriptController.Instance.compound = new NBTTagCompound();
				ScriptController.Instance.shouldSave = true;
			}

			@Override
			public Object get(String key) {
				NBTTagCompound compound = ScriptController.Instance.compound;
				if (!compound.hasKey(key)) {
					return null;
				}
				NBTBase base = compound.getTag(key);
				if (base instanceof NBTPrimitive) {
					return ((NBTPrimitive) base).getDouble();
				}
				return ((NBTTagString) base).getString();
			}

			@Override
			public String[] getKeys() {
				return ScriptController.Instance.compound.getKeySet()
						.toArray(new String[ScriptController.Instance.compound.getKeySet().size()]);
			}

			@Override
			public boolean has(String key) {
				return ScriptController.Instance.compound.hasKey(key);
			}

			@Override
			public void put(String key, Object value) {
				NBTTagCompound compound = ScriptController.Instance.compound;
				if (value instanceof Number) {
					compound.setDouble(key, ((Number) value).doubleValue());
				} else if (value instanceof String) {
					compound.setString(key, (String) value);
				}
				ScriptController.Instance.shouldSave = true;
			}

			@Override
			public void remove(String key) {
				ScriptController.Instance.compound.removeTag(key);
				ScriptController.Instance.shouldSave = true;
			}
		};
		this.world = (WorldServer) world;
		this.dimension = new DimensionWrapper(world.provider.getDimension(), world.provider.getDimensionType());
	}

	@Override
	public void broadcast(String message) {
		this.world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(message));
	}

	@Override
	public IEntity<?> createEntity(String id) {
		ResourceLocation resource = new ResourceLocation(id);
		Entity entity = EntityList.createEntityByIDFromName(resource, this.world);
		if (entity == null) {
			throw new CustomNPCsException("Failed to create an entity from given id: " + id, new Object[0]);
		}
		return NpcAPI.Instance().getIEntity(entity);
	}

	@Override
	public IEntity<?> createEntityFromNBT(INbt nbt) {
		Entity entity = EntityList.createEntityFromNBT(nbt.getMCNBT(), this.world);
		if (entity == null) {
			throw new CustomNPCsException("Failed to create an entity from given NBT", new Object[0]);
		}
		return NpcAPI.Instance().getIEntity(entity);
	}

	@Override
	public IItemStack createItem(String name, int damage, int size) {
		Item item = (Item) Item.REGISTRY.getObject(new ResourceLocation(name));
		if (item == null) {
			throw new CustomNPCsException("Unknown item id: " + name, new Object[0]);
		}
		return NpcAPI.Instance().getIItemStack(new ItemStack(item, size, damage));
	}

	@Override
	public IItemStack createItemFromNbt(INbt nbt) {
		ItemStack item = new ItemStack(nbt.getMCNBT());
		if (item.isEmpty()) {
			throw new CustomNPCsException("Failed to create an item from given NBT", new Object[0]);
		}
		return NpcAPI.Instance().getIItemStack(item);
	}

	@Override
	public void explode(double x, double y, double z, float range, boolean fire, boolean grief) {
		this.world.newExplosion(null, x, y, z, range, fire, grief);
	}

	@Override
	public IEntity<?>[] getAllEntities(int type) {
		@SuppressWarnings("unchecked")
		List<Entity> entities = this.world.getEntities(this.getClassForType(type), EntitySelectors.NOT_SPECTATING);
		List<IEntity<?>> list = new ArrayList<IEntity<?>>();
		for (Entity living : entities) {
			list.add(NpcAPI.Instance().getIEntity(living));
		}
		return list.toArray(new IEntity[list.size()]);
	}

	@Override
	public IPlayer<?>[] getAllPlayers() {
		List<EntityPlayerMP> list = (List<EntityPlayerMP>) this.world.getMinecraftServer().getPlayerList().getPlayers();
		IPlayer<?>[] arr = new IPlayer[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			arr[i] = (IPlayer<?>) NpcAPI.Instance().getIEntity(list.get(i));
		}
		return arr;
	}

	@Override
	public String getBiomeName(int x, int z) {
		return ObfuscationHelper.getValue(Biome.class, this.world.getBiomeForCoordsBody(new BlockPos(x, 0, z)), 17);
	}

	@Override
	public IBlock getBlock(int x, int y, int z) {
		return NpcAPI.Instance().getIBlock(this.world, new BlockPos(x, y, z));
	}

	@SuppressWarnings("rawtypes")
	private Class getClassForType(int type) {
		switch(type) {
			case 1: return EntityPlayer.class;
			case 2: return EntityNPCInterface.class;
			case 3: return EntityMob.class;
			case 4: return EntityAnimal.class;
			case 5: return EntityLivingBase.class;
			case 6: return EntityItem.class;
			case 7: return EntityProjectile.class;
			case 8: return PixelmonHelper.getPixelmonClass();
			case 9: return EntityVillager.class;
			case 10: return EntityArrow.class;
			case 11: return EntityThrowable.class;
		}
		return Entity.class;
	}

	@Override
	public IEntity<?> getClone(int tab, String name) {
		return NpcAPI.Instance().getClones().get(tab, name, this);
	}

	@Override
	public IEntity<?> getClosestEntity(int x, int y, int z, int range, int type) {
		return this.getClosestEntity(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
	}

	@Override
	public IEntity<?> getClosestEntity(IPos pos, int range, int type) {
		AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(pos.getMCBlockPos()).grow(range,
				range, range);
		@SuppressWarnings("unchecked")
		List<Entity> entities = this.world.getEntitiesWithinAABB(this.getClassForType(type), bb);
		double distance = range * range * range;
		Entity entity = null;
		for (Entity e : entities) {
			double r = pos.getMCBlockPos().distanceSq((Vec3i) e.getPosition());
			if (entity == null) {
				distance = r;
				entity = e;
			} else {
				if (r >= distance) {
					continue;
				}
				distance = r;
				entity = e;
			}
		}
		return NpcAPI.Instance().getIEntity(entity);
	}

	@Override
	public IDimension getDimension() {
		return this.dimension;
	}

	@Override
	public IEntity<?> getEntity(String uuid) {
		try {
			UUID id = UUID.fromString(uuid);
			Entity e = this.world.getEntityFromUuid(id);
			if (e == null) {
				e = this.world.getPlayerEntityByUUID(id);
			}
			if (e == null) {
				return null;
			}
			return NpcAPI.Instance().getIEntity(e);
		} catch (Exception e2) {
			throw new CustomNPCsException("Given uuid was invalid " + uuid, new Object[0]);
		}
	}
	
	@Override
	public IEntity<?>[] getEntitys(int type) {
		List<IEntity<?>> list = Lists.<IEntity<?>>newArrayList();
		for (Entity living : this.world.loadedEntityList) {
			IEntity<?> ie = NpcAPI.Instance().getIEntity(living);
			if (ie.getType() != type) { continue; }
			list.add(NpcAPI.Instance().getIEntity(living));
		}
		return list.toArray(new IEntity[list.size()]);
	}

	@Override
	public float getLightValue(int x, int y, int z) {
		return this.world.getLight(new BlockPos(x, y, z)) / 16.0f;
	}

	@Override
	public BlockPos getMCBlockPos(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	@Override
	public WorldServer getMCWorld() {
		return this.world;
	}

	@Override
	public String getName() {
		return this.world.getWorldInfo().getWorldName();
	}

	@Override
	public IEntity<?>[] getNearbyEntities(int x, int y, int z, int range, int type) {
		return this.getNearbyEntities(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
	}

	@Override
	public IEntity<?>[] getNearbyEntities(IPos pos, int range, int type) {
		AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(pos.getMCBlockPos()).grow(range,
				range, range);
		@SuppressWarnings("unchecked")
		List<Entity> entities = this.world.getEntitiesWithinAABB(this.getClassForType(type), bb);
		List<IEntity<?>> list = new ArrayList<IEntity<?>>();
		for (Entity living : entities) {
			list.add(NpcAPI.Instance().getIEntity(living));
		}
		return list.toArray(new IEntity[list.size()]);
	}

	@Override
	public IPlayer<?> getPlayer(String name) {
		EntityPlayer player = this.world.getPlayerEntityByName(name);
		if (player == null) {
			return null;
		}
		return (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
	}

	@Override
	public int getRedstonePower(int x, int y, int z) {
		return this.world.getStrongPower(new BlockPos(x, y, z));
	}

	@Override
	public IScoreboard getScoreboard() {
		return new ScoreboardWrapper(this.world.getMinecraftServer());
	}

	@Override
	public IBlock getSpawnPoint() {
		BlockPos pos = this.world.getSpawnCoordinate();
		if (pos == null) {
			pos = this.world.getSpawnPoint();
		}
		return NpcAPI.Instance().getIBlock(this.world, pos);
	}

	@Override
	public IData getStoreddata() {
		return this.storeddata;
	}

	@Override
	public IData getTempdata() {
		return this.tempdata;
	}

	@Override
	public long getTime() {
		return this.world.getWorldTime();
	}

	@Override
	public long getTotalTime() {
		return this.world.getTotalWorldTime();
	}

	public boolean isChunkLoaded(int x, int z) {
		return this.world.getChunkProvider().chunkExists(x >> 4, z >> 4);
	}

	@Override
	public boolean isDay() {
		return this.world.getWorldTime() % 24000L < 12000L;
	}

	@Override
	public boolean isRaining() {
		return this.world.getWorldInfo().isRaining();
	}

	@Override
	public void playSoundAt(IPos pos, String sound, float volume, float pitch) {
		Server.sendRangedData(this.world, pos.getMCBlockPos(), 16, EnumPacketClient.PLAY_SOUND, sound, pos.getX(),
				pos.getY(), pos.getZ(), volume, pitch);
	}

	@Override
	public void removeBlock(int x, int y, int z) {
		this.world.setBlockToAir(new BlockPos(x, y, z));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlock(int x, int y, int z, String name, int meta) {
		Block block = Block.getBlockFromName(name);
		if (block == null) {
			throw new CustomNPCsException("There is no such block: %s", new Object[0]);
		}
		this.world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(meta));
	}

	@Override
	public void setRaining(boolean bo) {
		this.world.getWorldInfo().setRaining(bo);
	}

	@Override
	public void setSpawnPoint(IBlock block) {
		this.world.setSpawnPoint(new BlockPos(block.getX(), block.getY(), block.getZ()));
	}

	@Override
	public void setTime(long time) {
		this.world.setWorldTime(time);
	}

	@Override
	public IEntity<?> spawnClone(double x, double y, double z, int tab, String name) {
		return NpcAPI.Instance().getClones().spawn(x, y, z, tab, name, this);
	}

	@Override
	public void spawnEntity(IEntity<?> entity) {
		Entity e = entity.getMCEntity();
		if (this.world.getEntityFromUuid(e.getUniqueID()) != null) {
			throw new CustomNPCsException("Entity with this UUID already exists", new Object[0]);
		}
		e.setPosition(e.posX, e.posY, e.posZ);
		this.world.spawnEntity(e);
	}

	@Override
	public void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz,
			double speed, int count) {
		EnumParticleTypes particleType = null;
		for (EnumParticleTypes enumParticle : EnumParticleTypes.values()) {
			if (enumParticle.getArgumentCount() > 0) {
				if (particle.startsWith(enumParticle.getParticleName())) {
					particleType = enumParticle;
					break;
				}
			} else if (particle.equals(enumParticle.getParticleName())) {
				particleType = enumParticle;
				break;
			}
		}
		if (particleType != null) {
			this.world.spawnParticle(particleType, x, y, z, count, dx, dy, dz, speed, new int[0]);
		}
	}

	@Override
	public void thunderStrike(double x, double y, double z) {
		this.world.addWeatherEffect(new EntityLightningBolt(this.world, x, y, z, false));
	}
	
	@Override
	public void trigger(int id, Object... arguments) {
		EventHooks.onScriptTriggerEvent(ScriptController.Instance.forgeScripts, id, this, this.getBlock(0, 0, 0).getPos(), null, arguments);
	}

}
