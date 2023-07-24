package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;

public interface IWorld {
	
	void broadcast(String message);

	IEntity<?> createEntity(String id);

	IEntity<?> createEntityFromNBT(INbt nbt);

	IItemStack createItem(String name, int damage, int size);

	IItemStack createItemFromNbt(INbt nbt);

	void explode(double x, double y, double z, float range, boolean fire, boolean grief);

	IEntity<?>[] getAllEntities(int type);

	IPlayer<?>[] getAllPlayers();

	String getBiomeName(int x, int z);

	@Deprecated
	IBlock getBlock(int x, int y, int z);
	
	IBlock getBlock(IPos pos);

	@Deprecated
	IEntity<?> getClone(int tab, String name);

	@Deprecated
	IEntity<?> getClosestEntity(int x, int y, int z, int range, int type);

	IEntity<?> getClosestEntity(IPos pos, int range, int type);

	IDimension getDimension();

	IEntity<?> getEntity(String uuid);

	float getLightValue(int x, int y, int z);

	BlockPos getMCBlockPos(int x, int y, int z);

	World getMCWorld();

	String getName();

	@Deprecated
	IEntity<?>[] getNearbyEntities(int x, int y, int z, int range, int type);

	IEntity<?>[] getNearbyEntities(IPos pos, int range, int type);

	IPlayer<?> getPlayer(String name);

	int getRedstonePower(int x, int y, int z);

	IScoreboard getScoreboard();

	IBlock getSpawnPoint();

	IData getStoreddata();

	IData getTempdata();

	long getTime();

	long getTotalTime();

	boolean isDay();

	boolean isRaining();

	void playSoundAt(IPos pos, String sound, float volume, float pitch);

	@Deprecated
	void removeBlock(int x, int y, int z);

	void removeBlock(IPos pos);

	@Deprecated
	void setBlock(int x, int y, int z, String name, int meta);
	
	void setBlock(IPos pos, String name, int meta);

	void setRaining(boolean bo);

	void setSpawnPoint(IBlock block);

	void setTime(long ticks);

	@Deprecated
	IEntity<?> spawnClone(double x, double y, double z, int tab, String name);

	void spawnEntity(IEntity<?> entity);

	void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count);

	void thunderStrike(double x, double y, double z);

	void trigger(int id, Object[] arguments);

	IEntity<?>[] getEntitys(int type);
	
}
