package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IWorld {

	void broadcast(@ParamName("message") String message);

	IEntity<?> createEntity(@ParamName("id") String id);

	IEntity<?> createEntityFromNBT(@ParamName("nbt") INbt nbt);

	IItemStack createItem(@ParamName("name") String name, @ParamName("damage") int damage, @ParamName("size") int size);

	IItemStack createItemFromNbt(@ParamName("texture") INbt nbt);

	void explode(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
				 @ParamName("range") float range, @ParamName("fire") boolean fire, @ParamName("grief") boolean grief);

	void forcePlaySoundAt(@ParamName("categoryType") int categoryType, @ParamName("pos") IPos pos,
						  @ParamName("sound") String sound, @ParamName("volume") float volume, @ParamName("pitch") float pitch);

	IEntity<?>[] getAllEntities(@ParamName("type") int type);

	IPlayer<?>[] getAllPlayers();

	String getBiomeName(@ParamName("x") int x, @ParamName("z") int z);

	@Deprecated
	IBlock getBlock(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	IBlock getBlock(@ParamName("pos") IPos pos);

	@Deprecated
	IEntity<?> getClone(@ParamName("tab") int tab, @ParamName("name") String name);

	@Deprecated
	IEntity<?> getClosestEntity(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z,
								@ParamName("range") int range, @ParamName("type") int type);

	IEntity<?> getClosestEntity(@ParamName("pos") IPos pos, @ParamName("range") int range, @ParamName("type") int type);

	IDimension getDimension();

	IEntity<?> getEntity(@ParamName("uuid") String uuid);

	IEntity<?>[] getEntitys(@ParamName("type") int type);

	float getLightValue(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	BlockPos getMCBlockPos(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	World getMCWorld();

	String getName();

	@Deprecated
	IEntity<?>[] getNearbyEntities(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z,
								   @ParamName("range") int range, @ParamName("type") int type);

	IEntity<?>[] getNearbyEntities(@ParamName("pos") IPos pos, @ParamName("range") int range, @ParamName("type") int type);

	IPlayer<?> getPlayer(@ParamName("name") String name);

	int getRedstonePower(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	IScoreboard getScoreboard();

	IBlock getSpawnPoint();

	IData getStoreddata();

	IData getTempdata();

	long getTime();

	long getTotalTime();

	boolean isDay();

	boolean isRaining();

	void playSoundAt(@ParamName("pos") IPos pos, @ParamName("sound") String sound, @ParamName("volume") float volume, @ParamName("pitch") float pitch);

	@Deprecated
	void removeBlock(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void removeBlock(@ParamName("pos") IPos pos);

	@Deprecated
	void setBlock(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z,
				  @ParamName("name") String name, @ParamName("name") int meta);

	void setBlock(@ParamName("pos") IPos pos, @ParamName("name") String name, @ParamName("meta") int meta);

	void setRaining(@ParamName("bo") boolean bo);

	void setSpawnPoint(@ParamName("block") IBlock block);

	void setTime(@ParamName("ticks") long ticks);

	@Deprecated
	IEntity<?> spawnClone(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
						  @ParamName("tab") int tab, @ParamName("name") String name);

	void spawnEntity(@ParamName("entity") IEntity<?> entity);

	void spawnParticle(@ParamName("particle") String particle, @ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
					   @ParamName("dx") double dx, @ParamName("dy") double dy, @ParamName("dz") double dz,
					   @ParamName("speed") double speed, @ParamName("count") int count);

	void thunderStrike(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	void trigger(@ParamName("id") int id, @ParamName("arguments") Object... arguments);

	String getId();

}
