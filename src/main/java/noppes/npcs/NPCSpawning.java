package noppes.npcs;

import java.util.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.reflection.world.biome.BiomeReflection;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class NPCSpawning {

	private static final Set<ChunkPos> eligibleChunksForSpawning = new HashSet<>();
	private static boolean pWGS = false;
	private static boolean fCFS = false;

	// Is called when the world has the ability to summon an entity
	public static void performWorldGenSpawning(WorldServer world, int x, int z, Random rand) {
		CustomNPCsScheduler.runTack(() -> {
			if (pWGS || world == null) { return;}
			CustomNpcs.debugData.start("Mod", NPCSpawning.class, "performWorldGenSpawning");
			pWGS = true;
			Biome biome = world.getBiomeForCoordsBody(new BlockPos(x + 8, 0, z + 8));
			SpawnData data = SpawnController.instance.getRandomSpawnData(BiomeReflection.getBiomeName(biome));
			if (data == null || data.group <= 0 || rand.nextFloat() > (float) data.itemWeight / 100.0f) {
				pWGS = false;
				CustomNpcs.debugData.end("Mod", NPCSpawning.class, "performWorldGenSpawning");
				return;
			}
			// is living
			Entity entity = null;
			try { entity = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception e) { LogWriter.error("Error:", e); }
			if (!(entity instanceof EntityLiving)) {
				pWGS = false;
				CustomNpcs.debugData.end("Mod", NPCSpawning.class, "performWorldGenSpawning");
				return;
			}
			Entity finalEntity = entity;
			trySummonToPos(3, data, world, world.getTopSolidOrLiquidBlock(new BlockPos(x + rand.nextInt(16), 0, z + rand.nextInt(16))), (EntityLiving) finalEntity);
			pWGS = false;
			CustomNpcs.debugData.end("Mod", NPCSpawning.class, "performWorldGenSpawning");
		});
	}

	// Called every tick
	public static void findChunksForSpawning(WorldServer world) {
		CustomNPCsScheduler.runTack(() -> {
			if (fCFS || SpawnController.instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % 100L != 0L) { return; }
			CustomNpcs.debugData.start("Mod", NPCSpawning.class, "findChunksForSpawning");
			fCFS = true;
			try {
				NPCSpawning.eligibleChunksForSpawning.clear();
				for (int i = 0; i < world.playerEntities.size(); ++i) {
					EntityPlayer entityplayer = world.playerEntities.get(i);
					if (!entityplayer.isSpectator()) {
						int j = MathHelper.floor(entityplayer.posX / 16.0);
						int k = MathHelper.floor(entityplayer.posZ / 16.0);
						byte size = 7;
						for (int x = -size; x <= size; ++x) {
							for (int z = -size; z <= size; ++z) {
								ChunkPos chunkcoordintpair = new ChunkPos(x + j, z + k);
								if (!NPCSpawning.eligibleChunksForSpawning.contains(chunkcoordintpair) && world.getWorldBorder().contains(chunkcoordintpair)) {
									PlayerChunkMapEntry playerinstance = world.getPlayerChunkMap().getEntry(chunkcoordintpair.x, chunkcoordintpair.z);
									if (playerinstance != null && playerinstance.isSentToPlayers()) {
										NPCSpawning.eligibleChunksForSpawning.add(chunkcoordintpair);
									}
								}
							}
						}
					}
				}
				ArrayList<ChunkPos> tmp = new ArrayList<>(NPCSpawning.eligibleChunksForSpawning);
				Collections.shuffle(tmp);
				for (ChunkPos chunkcoordintpair2 : tmp) {
					BlockPos chunkposition = getChunk(world, chunkcoordintpair2.x, chunkcoordintpair2.z);
					byte range = 6;
					int posX = chunkposition.getX() + world.rand.nextInt(range) - world.rand.nextInt(range);
					int posZ = chunkposition.getZ() + world.rand.nextInt(range) - world.rand.nextInt(range);
					BlockPos randomPos = new BlockPos(posX, chunkposition.getY(), posZ);
					String name = BiomeReflection.getBiomeName(world.getBiomeForCoordsBody(randomPos));
					SpawnData data = SpawnController.instance.getRandomSpawnData(name);
					if (data == null || data.group <= 0 || world.rand.nextFloat() > (float) data.itemWeight / 100.0f) { continue; }
					// is living
					Entity entity = null;
					try { entity = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception e) { LogWriter.error("Error:", e); }
					if (!(entity instanceof EntityLiving)) { continue; }
					trySummonToPos(1, data, world, randomPos, (EntityLiving) entity);
				}
			}
			catch (Exception ignored) {}
            fCFS = false;
			CustomNpcs.debugData.end("Mod", NPCSpawning.class, "findChunksForSpawning");
		});
	}

	private static void trySummonToPos(int maxTries, @Nonnull SpawnData data, @Nonnull WorldServer world, @Nonnull BlockPos startPos, @Nonnull  EntityLiving entity) {
		// total sizes:
		int[] sizes = getEntitySizes(world);
		if (entity instanceof EntityNPCInterface) { if (sizes[0] > 70) { return; } }
		else if (entity instanceof EntityAnimal) { if (sizes[1] > 10) { return; } }
		else if (entity instanceof EntityMob) { if (sizes[2] > 70) { return; } }
		else { if (sizes[3] > 50) { return; } }
		for (int summonTry = 0; summonTry < maxTries; ++summonTry) {
			BlockPos pos = getSpawnLocation(data, entity, world, startPos);
			if (pos != null) {
				boolean inSpawned = false;
				for (int i = 0; i < data.group; i++) {
					Entity e;
					try { e = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception ignored) { continue; }
					if (!(e instanceof EntityLiving)) { continue; }
					if (checkEntitySize(world, e, pos, data)) { spawnData((EntityLiving) e, world, pos); inSpawned = true; }
				}
				if (inSpawned) { break; }
			}
		}
	}

	private static BlockPos getSpawnLocation(SpawnData data, EntityLiving entity, WorldServer world, BlockPos startPos) {
		if (data == null || world == null) { return null; }
		int radius = 5;
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				if (y + startPos.getY() < 3 || y + startPos.getY() > 250) { continue; }
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = startPos.add(x, y, z);
					if (!world.getChunkFromBlockCoords(checkPos).isLoaded()) { continue; }
					if ((data.type == 1 && world.getLight(checkPos) > 8) || (data.type == 2 && world.getLight(checkPos) <= 8)) { continue; }

					entity.posX = checkPos.getX() + 0.5d;
					entity.posZ = checkPos.getZ() + 0.5d;
					entity.posY = checkPos.getY();

					boolean isSpawnPos = false;
					BlockPos posDown = checkPos.down();
					BlockPos posUp = checkPos.up((int) Math.floor(entity.getEyeHeight()));
					IBlockState state = world.getBlockState(checkPos);
					IBlockState stateDown = world.getBlockState(posDown);
					IBlockState stateUp = world.getBlockState(posUp);
					// in water
					if (data.liquid) {
						isSpawnPos = state.getMaterial().isLiquid() &&
								stateDown.getMaterial().isLiquid() &&
								stateUp.getMaterial().isLiquid() || !stateUp.isNormalCube();
					}
					// in air
					else if (entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).ais.getNavigationType() == 1) {
						isSpawnPos = (!state.isNormalCube() || state.getBlock().isAir(state, world, checkPos)) &&
								(!stateDown.isNormalCube() || stateDown.getBlock().isAir(stateDown, world, posDown)) &&
								(!stateUp.isNormalCube() || stateUp.getBlock().isAir(stateUp, world, posUp));
					}
					// on ground
					else {
						if (stateDown.getBlock() != Blocks.BEDROCK && stateDown.getBlock() != Blocks.BARRIER) {
							isSpawnPos = !state.isNormalCube() && !state.getMaterial().isLiquid() &&
									stateDown.getBlock().canCreatureSpawn(stateDown, world, posDown, EntityLiving.SpawnPlacementType.ON_GROUND) &&
									(!stateUp.isNormalCube() || stateUp.getBlock().isAir(stateUp, world, posUp));
						}
					}
					if (isSpawnPos) {
						// near players
						for (EntityPlayer player : world.playerEntities) {
							if (player.isSpectator()) {
								isSpawnPos = false;
								break;
							}
                            double dist = player.getDistance(entity);
							if (dist > PlayerData.get(player).game.renderDistance + 16.0d) {
								isSpawnPos = false;
								break;
							}
							// too close
							if (dist < 12) {
								isSpawnPos = false;
								break;
							}
							// can see summon
							if (data.canSeeSummon != Util.instance.npcCanSeeTarget(player, entity, false, true)) {
								isSpawnPos = false;
								break;
							}
							// range
							AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(checkPos).grow(144.0d);
							List<? extends EntityLiving> list = new ArrayList<>();
							try {
								list = world.getEntitiesWithinAABB(entity.getClass(), aabb);
							}
							catch (Exception ignored) { }
							int count = list.size();
							if (entity instanceof EntityNPCInterface) {
								count = 0;
								for (Entity e : list) { if (((EntityNPCInterface) e).stats.spawnCycle == 4) { count++; } }
							}
							if (count >= data.maxNearPlayer) {
								isSpawnPos = false;
								break;
							}
						}
					}
					if (!isSpawnPos) { continue; }
					@SuppressWarnings("deprecation")
					Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ);
					if (canSpawn == Event.Result.DENY || (canSpawn == Event.Result.DEFAULT && !entity.getCanSpawnHere())) { continue; }
					return checkPos;
				}
			}
		}
		return null;
	}

	private static boolean checkEntitySize(WorldServer world, Entity entity, BlockPos pos, @Nonnull SpawnData data) {
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos);
		// Range
		List<Entity> list = new ArrayList<>();
		try {
			list = world.getEntitiesWithinAABB(entity.getClass(), aabb.grow(data.range));
		}
		catch (Exception ignored) { }
		int count = list.size();
		if (entity instanceof EntityNPCInterface) {
			count = 0;
			for (Entity e : list) {
				if (((EntityNPCInterface) e).stats.spawnCycle == 4) { count++; }
			}
		}
        return count <= data.group;
    }

	private static int[] getEntitySizes(World world) {
		int[] sizes = new int[4]; // npc, animals, mobs
		for (Entity e : world.loadedEntityList) {
			if (!e.isEntityAlive()) { continue; }
			if (e instanceof EntityNPCInterface) {
				if (((EntityNPCInterface) e).stats.spawnCycle == 4) { sizes[0]++; }
			}
			else if (e instanceof EntityAnimal) { sizes[1]++; }
			else if (e instanceof EntityMob) { sizes[2]++; }
			else { sizes[3]++; }
		}
		return sizes;
	}

	protected static BlockPos getChunk(World world, int x, int z) {
		Chunk chunk = world.getChunkFromChunkCoords(x, z);
		int posX = x * 16 + world.rand.nextInt(16);
		int posZ = z * 16 + world.rand.nextInt(16);
		int y = MathHelper.roundUp(chunk.getHeight(new BlockPos(posX, 0, posZ)) + 1, 16);
		int posY = world.rand.nextInt((y > 0) ? y : (chunk.getTopFilledSegment() + 16 - 1));
		return new BlockPos(posX, posY, posZ);
	}

	private static void spawnData(EntityLiving entity, World world, BlockPos pos) {
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.stats.spawnCycle = 4;
			npc.stats.respawnTime = 0;
			npc.ais.returnToStart = false;
			npc.ais.setStartPos(pos);
		}
		entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.rand.nextFloat() * 360.0f, 0.0f);
		world.spawnEntity(entity);
	}

}
