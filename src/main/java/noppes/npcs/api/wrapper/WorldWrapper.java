package noppes.npcs.api.wrapper;

import java.util.*;

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
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
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
import noppes.npcs.api.wrapper.data.Data;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.reflection.world.WorldReflection;
import noppes.npcs.reflection.world.biome.BiomeReflection;
import noppes.npcs.util.Util;

public class WorldWrapper implements IWorld {

	private static final Data tempdata = new Data();
	private static final Data storeddata = new Data();

	@Deprecated
	public static WorldWrapper createNew(World world) {
		return new WorldWrapper(world);
	}
	public IDimension dimension;

	public World world;

	private WorldWrapper(World world) {
		this.world = world;
		this.dimension = new DimensionWrapper(world.provider.getDimension(), world.provider.getDimensionType());
	}

	public static void clearTempdata() { tempdata.clear(); }

	@Override
	public void broadcast(String message) {
		if (this.world.getMinecraftServer() != null) {
			this.world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(message));
		} else if (CustomNpcs.Server != null) {
			CustomNpcs.Server.getPlayerList().sendMessage(new TextComponentString(message));
		} else {
			EntityPlayer player = CustomNpcs.proxy.getPlayer();
			if (player != null) {
				player.sendMessage(new TextComponentString(message));
			}
		}
	}

	@Override
	public IEntity<?> createEntity(String id) {
		ResourceLocation resource = new ResourceLocation(id);
		Entity entity = EntityList.createEntityByIDFromName(resource, this.world);
		if (entity == null) {
			throw new CustomNPCsException("Failed to create an entity from given id: " + id);
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
	}

	@Override
	public IEntity<?> createEntityFromNBT(INbt nbt) {
		Entity entity = EntityList.createEntityFromNBT(nbt.getMCNBT(), this.world);
		if (entity == null) {
			throw new CustomNPCsException("Failed to create an entity from given NBT");
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
	}

	@Override
	public IItemStack createItem(String name, int damage, int size) {
		Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
		if (item == null) {
			throw new CustomNPCsException("Unknown item id: " + name);
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(item, size, damage));
	}

	@Override
	public IItemStack createItemFromNbt(INbt nbt) {
		ItemStack item = new ItemStack(nbt.getMCNBT());
		if (item.isEmpty()) {
			throw new CustomNPCsException("Failed to create an item from given NBT");
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item);
	}

	@Override
	public void explode(double x, double y, double z, float range, boolean fire, boolean grief) {
		this.world.newExplosion(null, x, y, z, range, fire, grief);
	}

	@Override
	public void forcePlaySoundAt(int categoryType, IPos pos, String sound, float volume, float pitch) {
		Server.sendRangedData(this.world, pos.getMCBlockPos(), 16, EnumPacketClient.FORCE_PLAY_SOUND, categoryType, sound, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ(), volume, pitch);
	}

	@Override
	public IEntity<?>[] getAllEntities(int type) {
		@SuppressWarnings("unchecked")
		List<Entity> entities = this.world.getEntities(this.getClassForType(type), EntitySelectors.NOT_SPECTATING);
		List<IEntity<?>> list = new ArrayList<>();
		for (Entity living : entities) {
			list.add(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(living));
		}
		return list.toArray(new IEntity[0]);
	}

	@Override
	public IPlayer<?>[] getAllPlayers() {
		List<EntityPlayerMP> list = Objects.requireNonNull(this.world.getMinecraftServer()).getPlayerList().getPlayers();
		IPlayer<?>[] arr = new IPlayer[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			arr[i] = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(list.get(i));
		}
		return arr;
	}

	@Override
	public String getBiomeName(int x, int z) {
		return BiomeReflection.getBiomeName(world.getBiomeForCoordsBody(new BlockPos(x, 0, z)));
	}

	@Override
	@Deprecated
	public IBlock getBlock(int x, int y, int z) {
		return Objects.requireNonNull(NpcAPI.Instance()).getIBlock(this.world, new BlockPos(x, y, z));
	}

	@Override
	public IBlock getBlock(IPos pos) {
		if (pos == null) {
			return null;
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIBlock(this.world, pos.getMCBlockPos());
	}

	@SuppressWarnings("rawtypes")
	private Class getClassForType(int type) {
		switch (type) {
		case 1:
			return EntityPlayer.class;
		case 2:
			return EntityNPCInterface.class;
		case 3:
			return EntityMob.class;
		case 4:
			return EntityAnimal.class;
		case 5:
			return EntityLivingBase.class;
		case 6:
			return EntityItem.class;
		case 7:
			return EntityProjectile.class;
		case 8:
			return PixelmonHelper.getPixelmonClass();
		case 9:
			return EntityVillager.class;
		case 10:
			return EntityArrow.class;
		case 11:
			return EntityThrowable.class;
		}
		return Entity.class;
	}

	@Override
	@Deprecated
	public IEntity<?> getClone(int tab, String name) {
		return Objects.requireNonNull(NpcAPI.Instance()).getClones().get(tab, name, this);
	}

	@Override
	@Deprecated
	public IEntity<?> getClosestEntity(int x, int y, int z, int range, int type) {
		return this.getClosestEntity(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IEntity<?> getClosestEntity(IPos pos, int range, int type) {
		List<? extends Entity> list = Util.instance.getEntitiesWithinDist(getClassForType(type), world, pos.getX(), pos.getY(), pos.getZ(), range);
		double distance = 0.0d;
		Entity entity = null;
		for (Entity e : list) {
			double r = pos.getMCBlockPos().distanceSq(e.getPosition());
            if (entity != null && r >= distance) {  continue; }
            distance = r;
            entity = e;
        }
		return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
	}

	@Override
	public IDimension getDimension() {
		return this.dimension;
	}

	@Override
	public IEntity<?> getEntity(String uuid) {
		UUID id = null;
		try {
			id = UUID.fromString(uuid);
		} catch (Exception e) { LogWriter.error(e); }
		if (id == null) {
			throw new CustomNPCsException("Given uuid was invalid " + uuid);
		}
		Entity e = null;
		for (Entity entity : this.world.loadedEntityList) {
			if (entity.getUniqueID().equals(id)) {
				e = entity;
				break;
			}
		}
		if (e == null) {
			List<Entity> unloadedEntityList = WorldReflection.getUnloadedEntityList(world);
            if (unloadedEntityList != null) {
				for (Entity entity : unloadedEntityList) {
					if (entity.getUniqueID().equals(id)) {
						e = entity;
						break;
					}
				}
			}
		}
		if (e == null) {
			e = this.world.getPlayerEntityByUUID(id);
		}
		if (e == null) {
			return null;
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(e);
	}

	@Override
	public IEntity<?>[] getEntitys(int type) {
		List<IEntity<?>> list = new ArrayList<>();
		for (Entity living : this.world.loadedEntityList) {
			IEntity<?> ie = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(living);
			if (ie.getType() != type) {
				continue;
			}
			list.add(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(living));
		}
		return list.toArray(new IEntity[0]);
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
	public World getMCWorld() {
		return this.world;
	}

	@Override
	public String getName() {
		return this.world.getWorldInfo().getWorldName();
	}

	@Override
	@Deprecated
	public IEntity<?>[] getNearbyEntities(int x, int y, int z, int range, int type) {
		return this.getNearbyEntities(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IEntity<?>[] getNearbyEntities(IPos pos, int range, int type) {
		List<? extends Entity> entities = Util.instance.getEntitiesWithinDist(getClassForType(type), world, pos.getX(), pos.getY(), pos.getZ(), range);
		List<IEntity<?>> list = new ArrayList<>();
		for (Entity e : entities) {
			list.add(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(e));
		}
		return list.toArray(new IEntity[0]);
	}

	@Override
	public IPlayer<?> getPlayer(String name) {
		EntityPlayer player = this.world.getPlayerEntityByName(name);
		if (player == null) {
			return null;
		}
		return (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player);
	}

	@Override
	public int getRedstonePower(int x, int y, int z) {
		return this.world.getStrongPower(new BlockPos(x, y, z));
	}

	@Override
	public IScoreboard getScoreboard() {
		return this.world.getMinecraftServer() == null ? null : new ScoreboardWrapper(this.world.getMinecraftServer());
	}

	@Override
	public IBlock getSpawnPoint() {
		BlockPos pos = null;
		if (this.world instanceof WorldServer) {
			pos = ((WorldServer) this.world).getSpawnCoordinate();
		}
		if (pos == null) {
			pos = this.world.getSpawnPoint();
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIBlock(this.world, pos);
	}
	@Override
	public IData getStoreddata() { return storeddata; }

	@Override
	public IData getTempdata() { return tempdata; }

	public static IData getTempData() { return tempdata; }

	public static IData getStoredData() { return storeddata; }

	@Override
	public long getTime() {
		return this.world.getWorldTime();
	}

	@Override
	public long getTotalTime() {
		return this.world.getTotalWorldTime();
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
	@Deprecated
	public void removeBlock(int x, int y, int z) {
		this.world.setBlockToAir(new BlockPos(x, y, z));
	}

	@Override
	public void removeBlock(IPos pos) {
		this.world.setBlockToAir(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
	}

	@Override
	@Deprecated
	public void setBlock(int x, int y, int z, String name, int meta) {
		Block block = Block.getBlockFromName(name);
		if (block == null) {
			throw new CustomNPCsException("There is no such block: %s");
		}
		this.world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(meta));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlock(IPos pos, String name, int meta) {
		Block block = Block.getBlockFromName(name);
		if (block == null) {
			throw new CustomNPCsException("There is no such block: %s");
		}
		this.world.setBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), block.getStateFromMeta(meta));
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
	@Deprecated
	public IEntity<?> spawnClone(double x, double y, double z, int tab, String name) {
		return Objects.requireNonNull(NpcAPI.Instance()).getClones().spawn(x, y, z, tab, name, this);
	}

	@Override
	public void spawnEntity(IEntity<?> entity) {
		Entity e = entity.getMCEntity();
		for (Entity el : this.world.loadedEntityList) {
			if (el.getUniqueID().equals(e.getUniqueID())) {
				throw new CustomNPCsException("Entity with this UUID already exists");
			}
		}
		e.setPosition(e.posX, e.posY, e.posZ);
		this.world.spawnEntity(e);
	}

	@Override
	public void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count) {
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
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(particleType, x, y, z, count, dx, dy, dz, speed);
			} else {
				this.world.spawnParticle(particleType, false, x, y, z, dx * speed, dy * speed, dz * speed, count);
			}
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

	@Override
	public String getId() {
		if (world.provider != null) {
            return world.provider.getDimensionType().getName();
        }
		return "nothing";
	}

}
