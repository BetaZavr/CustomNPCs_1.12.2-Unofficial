package noppes.npcs;

import java.util.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
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
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.reflection.world.biome.BiomeReflection;
import noppes.npcs.util.CustomNPCsScheduler;

public class NPCSpawning {

	private static final int maxTries = 5;
	private static final Set<ChunkPos> eligibleChunksForSpawning = new HashSet<>();

	// Is called when the world has the ability to summon an entity
	public static void performWorldGenSpawning(World world, int x, int z, Random rand) {
		CustomNpcs.debugData.startDebug(!world.isRemote ? "Server" : "Client", "Mod", "NPCSpawning_performWorldGenSpawning");
		Biome biome = world.getBiomeForCoordsBody(new BlockPos(x + 8, 0, z + 8));
		SpawnData data = SpawnController.instance.getRandomSpawnData(BiomeReflection.getBiomeName(biome));
		if (data == null || data.group <= 0 || rand.nextFloat() > (float) data.itemWeight / 100.0f) { return; }
		CustomNPCsScheduler.runTack(() -> {
			// is living
			Entity entity = null;
			try { entity = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception e) { LogWriter.error("Error:", e); }
			if (!(entity instanceof EntityLiving)) { return; }

			// total sizes:
			int[] sizes = getEntitySizes(world);
			if (entity instanceof EntityNPCInterface) {
				if (sizes[0] > CustomNpcs.MaxSpawnEntities) { return; }
			}
			else if (entity instanceof EntityAnimal) {
				if (sizes[1] > CustomNpcs.MaxSpawnEntities) { return; }
			}
			else if (entity instanceof EntityCreature) {
				if (sizes[2] > CustomNpcs.MaxSpawnEntities) { return; }
			}
			for (int summonTry = 0; summonTry < maxTries; ++summonTry) {
				BlockPos pos = getSpawnLocation(data, (EntityLiving) entity, world,
						world.getTopSolidOrLiquidBlock(new BlockPos(x + rand.nextInt(16), 0, z + rand.nextInt(16))), 12);
				if (pos != null) {
					for (int i = 0; i < data.group; i++) {
						Entity e;
						try { e = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception ignored) { continue; }
						if (!(e instanceof EntityLiving)) { continue; }
						if (getEntitySize(world, e, pos, data.range) < data.group) {
							spawnData((EntityLiving) e, world, pos);
						}
					}
					break;
				}
			}
		});
		CustomNpcs.debugData.endDebug(!world.isRemote ? "Server" : "Client", "Mod", "NPCSpawning_performWorldGenSpawning");
	}

	// Called every tick
	public static void findChunksForSpawning(WorldServer world) {
		if (SpawnController.instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % 100L != 0L) { return; }
		CustomNpcs.debugData.startDebug("Server", "Mod", "NPCSpawning_findChunksForSpawning");
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
		CustomNPCsScheduler.runTack(() -> {
			int[] sizes = getEntitySizes(world);
			int tries = 0;
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

				// total sizes:
				if (entity instanceof EntityNPCInterface) {
					if (sizes[0] > CustomNpcs.MaxSpawnEntities) { continue; }
				}
				else if (entity instanceof EntityAnimal) {
					if (sizes[1] > CustomNpcs.MaxSpawnEntities) { continue; }
				}
				else if (entity instanceof EntityCreature) {
					if (sizes[2] > CustomNpcs.MaxSpawnEntities) { continue; }
				}

				BlockPos pos = getSpawnLocation(data, (EntityLiving) entity, world, randomPos, 24);
//System.out.println("CNPCs: "+pos);
				if (pos != null) {
					//boolean isSummon = false;
					for (int i = 0; i < data.group; i++) {
						Entity e;
						try { e = EntityList.createEntityFromNBT(data.compoundEntity, world); } catch (Exception ignored) { continue; }
						if (!(e instanceof EntityLiving)) { continue; }
						if (getEntitySize(world, e, pos, data.range) < data.group) {
							spawnData((EntityLiving) e, world, pos);
							//isSummon = true;
						}
					}
					//if (isSummon) { tries++; }
				}
				//if (tries > maxTries) { break; }
			}
		});
		CustomNpcs.debugData.endDebug("Server", "Mod", "NPCSpawning_findChunksForSpawning");
	}

	private static BlockPos getSpawnLocation(SpawnData data, EntityLiving entity, World world, BlockPos startPos, int distanceToPlayer) {
		if (data == null || world == null) { return null; }
		int radius = 8;
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos checkPos = startPos.add(x, y, z);
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
						EntityPlayer player = world.getClosestPlayer(checkPos.getX(), checkPos.getY(), checkPos.getZ(), 128.0, false);
						if (player != null && player.getDistance(checkPos.getX(), checkPos.getY(), checkPos.getZ()) < distanceToPlayer) { continue; }
						if ((data.type == 1 && world.getLight(checkPos) > 8) || (data.type == 2 && world.getLight(checkPos) <= 8)) { continue; }
						return checkPos;
					}
				}
			}
		}
		return null;
	}

	private static int getEntitySize(World world, Entity entity, BlockPos pos, int range) {
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos).grow(range);
		List<? extends Entity> list = world.getEntitiesWithinAABB(entity.getClass(), aabb);
		if (entity instanceof EntityNPCInterface) {
			int count = 0;
			for (Entity e : list) {
				if (((EntityNPCInterface) e).stats.spawnCycle == 4) { count++; }
			}
			return count;
		}
		return list.size();
	}

	private static int[] getEntitySizes(World world) {
		int[] sizes = new int[3]; // npc, animals, mobs
		for (Entity e : world.loadedEntityList) {
			if (!e.isEntityAlive()) { continue; }
			if (e instanceof EntityNPCInterface) {
				if (((EntityNPCInterface) e).stats.spawnCycle == 4) { sizes[0]++; }
			}
			else if (e instanceof EntityAnimal) { sizes[1]++; }
			else if (e instanceof EntityCreature) { sizes[2]++; }
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
		@SuppressWarnings("deprecation")
		Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ);
		if (canSpawn == Event.Result.DENY || (canSpawn == Event.Result.DEFAULT && !entity.getCanSpawnHere())) {
			return;
		}
		world.spawnEntity(entity);
	}

}
