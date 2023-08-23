package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.data.role.IJobSpawner;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.data.SpawnNPCData;

public class JobSpawner
extends JobInterface
implements IJobSpawner {
	
	private String id;
	// New
	// 0/false=alive | 1/true=dead
	private SpawnNPCData[][] dataEntitys;
	public int[][] offset;
	private int[] spawnType; // 0 =one, 1 = all, 2 = random
	private int[] number; // curent pos
	private long cooldownSet; // setting time cooldown
	private long cooldown; // cooldown time if alive
	private boolean[] desTargetLost; // despawnOnTargetLost
	private Map<Boolean, List<EntityLivingBase>> spawnedEntitys;
	private EntityLivingBase target;
	public boolean exact;
	public boolean resetUpdate;

	public JobSpawner(EntityNPCInterface npc) {
		super(npc);
		this.id = RandomStringUtils.random(8, true, true);
		// new
		this.dataEntitys = new SpawnNPCData[2][];
		this.dataEntitys[0] = new SpawnNPCData[0];
		this.dataEntitys[1] = new SpawnNPCData[0];
		
		this.offset = new int[2][];
		this.offset[0] = new int[] { 0, 0, 0 };
		this.offset[1] = new int[] { 0, 0, 0 };
		
		this.spawnType = new int [] { 0, 0 };
		this.number = new int[] { 0, 0 };
		this.cooldownSet = 3000L;
		this.desTargetLost = new boolean[] { true, true };

		this.spawnedEntitys = Maps.<Boolean, List<EntityLivingBase>>newHashMap();
		this.spawnedEntitys.put(false, Lists.<EntityLivingBase>newArrayList());
		this.spawnedEntitys.put(true, Lists.<EntityLivingBase>newArrayList());
		this.cooldown = System.currentTimeMillis() + this.cooldownSet;
		
		this.exact = false;
		this.resetUpdate = true;
		this.type = JobType.SPAWNER;
	}

	@Override
	public boolean aiShouldExecute() { // make work
		boolean isDead = this.npc.getHealth()<=0;
		if (this.isEmpty(isDead) || this.npc.isKilled()) { return false; }
		this.target = this.getTarget();
		if (!this.spawnedEntitys.isEmpty()) { this.checkSpawns(); }
		return this.target!=null;
	}

	@Override
	public void aiStartExecuting() { // after reset NPC
		this.number[0] = 0;
		this.number[1] = 0;
		for (int i=0; i<2; i++) {
			for (EntityLivingBase entity : this.spawnedEntitys.get(i==0)) {
				int slot = entity.getEntityData().getInteger("NpcSpawnerSlot");
				if (slot > this.number[i]) { this.number[i] = slot; }
				this.setTarget(entity, this.getTarget());
			}
		}
	}
	
	@Override
	public void aiDeathExecute(Entity attackingEntity) { // whent death
		if (attackingEntity instanceof EntityLivingBase) { this.target = (EntityLivingBase) attackingEntity; }
		this.aiUpdateTask();
	}

	@Override
	public void aiUpdateTask() { // after start any 20 ticks
		boolean isDead = this.npc.getHealth()<=0;
		if (!this.spawnedEntitys.get(isDead).isEmpty()) { // Has Spawned
			if (this.npc.world.getTotalWorldTime()%20==0) {
				this.cooldown = System.currentTimeMillis() + (long) ((double) this.cooldownSet * (this.npc.getRNG().nextFloat()<0.5f ? 1.1d : 0.9d));
			}
			this.checkSpawns();
			return;
		}
		if (this.getTarget()==null) { return; }
		if (!isDead && this.isOnCooldown(isDead)) { return; } // is Alive and or Cooldown
		int type = isDead ? 1 : 0;
		switch(this.spawnType[type]) {
			case 0: { // one to one
				this.spawnEntity(this.number[type], isDead);
				this.number[type]++;
				if (this.number[type]>this.dataEntitys[type].length) { this.number[type] = 0; }
				break;
			}
			case 1: { // all
				Map<Integer, SpawnNPCData> map = Maps.newHashMap();
				for (int i=0; i<this.dataEntitys[type].length; i++) {
					map.put(i, this.dataEntitys[type][i]);
				}
				while(map.size()>7) {
					map.remove(this.npc.getRNG().nextInt(map.size()));
				}
				for (int slot : map.keySet()) {
					this.number[type] = slot;
					this.spawnEntity(slot, isDead);
				}
				break;
			}
			default: { // random
				this.number[type] = this.npc.getRNG().nextInt(this.dataEntitys[type].length);
				this.spawnEntity(this.number[type], isDead);
			}
		}
	}

	public void checkSpawns() {
		for (int i=0; i<2; i++) {
			List<EntityLivingBase> toDespawn = Lists.<EntityLivingBase>newArrayList();
			for (EntityLivingBase spawn : this.spawnedEntitys.get(i==0)) {
				if (this.shouldDelete(spawn)) {
					spawn.isDead = true;
					toDespawn.add(spawn);
				}
				else { this.checkTarget(spawn); }
			}
			for (EntityLivingBase entity : toDespawn) { this.spawnedEntitys.get(i==0).remove(entity); }
		}
	}

	public void checkTarget(EntityLivingBase entity) {
		if (entity instanceof EntityLiving) {
			EntityLiving liv = (EntityLiving) entity;
			if (liv.getAttackTarget() == null || this.npc.getRNG().nextInt(100) == 1) {
				liv.setAttackTarget(this.target);
			}
		} else if (entity.getRevengeTarget() == null || this.npc.getRNG().nextInt(100) == 1) {
			entity.setRevengeTarget(this.target);
		}
	}

	public void cleanCompound(NBTTagCompound compound) {
		for (int i=0; i<2; i++) {
			String key = "DataEntitysWhen"+(i==0 ? "Alive" : "Dead");
			for (int j =0 ; j<compound.getTagList(key, 10).tagCount(); j++) {
				NBTTagCompound sdNbt = compound.getTagList(key, 10).getCompoundTagAt(j).getCompoundTag("EntityNBT");
				String name = "type.empty";
				if (sdNbt!= null) {
					sdNbt = sdNbt.copy();
					if (sdNbt.hasKey("ClonedName", 8)) {
						name =  sdNbt.getString("ClonedName");
					} else if (sdNbt.hasKey("Name", 8)) {
						name = sdNbt.getString("Name");
					}
					else if (sdNbt.hasKey("id", 8)) {
						Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(sdNbt.getString("id")), Minecraft.getMinecraft().world);
						if (entity!=null) { name = entity.getName(); }
					}
				}
				compound.getTagList(key, 10).getCompoundTagAt(j).removeTag("EntityNBT");
				compound.getTagList(key, 10).getCompoundTagAt(j).setString("Name", name);
				if (sdNbt.hasKey("ClonedName", 8)) { compound.getTagList(key, 10).getCompoundTagAt(j).setString("ClonedName", sdNbt.getString("ClonedName")); }
				if (sdNbt.hasKey("ClonedTab", 3)) { compound.getTagList(key, 10).getCompoundTagAt(j).setInteger("ClonedTab", sdNbt.getInteger("ClonedTab")); }
			}
		}
	}
	
	public void removeCompound(NBTTagCompound compound) {
		for (int i=0; i<2; i++) {
			String key = "DataEntitysWhen"+(i==0 ? "Alive" : "Dead");
			for (int j =0 ; j<compound.getTagList(key, 10).tagCount(); j++) {
				compound.getTagList(key, 10).getCompoundTagAt(j).removeTag("EntityNBT");
			}
		}
	}

	private EntityLivingBase getTarget() {
		EntityLivingBase target = this.getTarget(this.npc);
		if (target!=null) { return target; }
		for (int i=0; i<2; i++) {
			for (EntityLivingBase entity : this.spawnedEntitys.get(i==0)) {
				target = this.getTarget(entity);
				if (target != null) { return target; }
			}
		}
		return this.target;
	}

	private EntityLivingBase getTarget(EntityLivingBase entity) {
		if (entity==null || (entity==this.npc && (entity.isDead || entity.getHealth()<=0.0))) {
			return this.target;
		}
		if (entity instanceof EntityLiving) {
			this.target = ((EntityLiving) entity).getAttackTarget();
			if (this.target != null && !this.target.isDead && this.target.getHealth() > 0.0f) {
				return this.target;
			}
		}
		this.target = entity.getRevengeTarget();
		if (this.target != null && !this.target.isDead && this.target.getHealth() > 0.0f) {
			if (entity.getDistance(this.target)>40.0f) { return null; }
			return this.target;
		}
		return null;
	}

	public boolean hasPixelmon() {
		for (int i=0; i<2; i++) {
			for (SpawnNPCData sd : this.dataEntitys[i]) {
				if (sd.compound!=null && sd.compound.getString("id").equals("pixelmontainer")) { return true; }
			}
		}
		return false;
	}

	private boolean isEmpty(boolean isDead) {
		for (SpawnNPCData sd : this.dataEntitys[isDead ? 1 : 0]) {
			if (sd.compound!=null && sd.compound.hasKey("id")) { return false; }
		}
		return true;
	}

	public boolean isOnCooldown(boolean isDead) {
		return System.currentTimeMillis() < this.cooldown;
	}

	@Override
	public void killed() { this.reset(); }

	@Override
	public void removeAllSpawned() {
		for (int i=0; i<2; i++) {
			for (EntityLivingBase entity : this.spawnedEntitys.get(i==0)) { entity.isDead = true; }
			this.spawnedEntitys.get(i==0).clear();
		}
	}

	@Override
	public void reset() {
		for (int i=0; i<2; i++) {
			this.number[i] = 0;
			if (this.spawnedEntitys.get(i==0).isEmpty()) {
				this.spawnedEntitys.put(i==0, this.getNearbySpawned(i==0));
			}
		}
		this.target = null;
		this.cooldown = 0L;
		this.checkSpawns();
	}
	
	private List<EntityLivingBase> getNearbySpawned(boolean isDead) {
		List<EntityLivingBase> spawnList = new ArrayList<EntityLivingBase>();
		List<EntityLivingBase> list = this.npc.world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.npc.getEntityBoundingBox().grow(60.0, 60.0, 60.0));
		for (EntityLivingBase entity : list) {
			if (!entity.isDead && entity.getEntityData().getString("NpcSpawnerId").equals(this.id) && entity.getEntityData().getBoolean("NpcSpawnerDead")==isDead) { spawnList.add(entity); }
		}
		return spawnList;
	}

	@Override
	public void resetTask() { this.reset(); }

	public SpawnNPCData readJobCompound(int slot, boolean isDead, NBTTagCompound spawnNBT) {
		int type = isDead ? 1 : 0;
		if (slot>=0 && slot<this.dataEntitys[type].length) {
			SpawnNPCData sd = this.dataEntitys[type][slot];
			sd.readFromNBT(spawnNBT);
			return sd;
		}
		return this.add(new SpawnNPCData(spawnNBT), isDead);
	}

	private void setTarget(EntityLivingBase base, EntityLivingBase target) {
		if (base instanceof EntityLiving) {
			((EntityLiving) base).setAttackTarget(target);
		} else {
			base.setRevengeTarget(target);
		}
		if (this.npc==base) { this.target = target; }
	}

	public boolean shouldDelete(EntityLivingBase entity) {
		SpawnNPCData sp = null;
		boolean sets = false;
		boolean isDead = this.npc.getHealth()<=0;
		// EntityData
		if (entity.getEntityData().hasKey("NpcSpawnerEntityId", 3) &&
				entity.getEntityData().hasKey("NpcSpawnerSlot", 3) &&
				entity.getEntityData().hasKey("NpcSpawnerId", 8) &&
				entity.getEntityData().hasKey("NpcSpawnerDead", 1)) {
			
			if (this.resetUpdate && isDead!=entity.getEntityData().getBoolean("NpcSpawnerDead")) { return true; }
			sets = entity.getEntityData().getString("NpcSpawnerId").equals(this.id) &&
					entity.getEntityData().getInteger("NpcSpawnerEntityId")==this.npc.getEntityId();
			sp = this.get(entity.getEntityData().getInteger("NpcSpawnerSlot"), entity.getEntityData().getBoolean("NpcSpawnerDead"));
		}
		if (!sets || sp==null) { return true; }
		// Destination or Dead
		if (entity.isDead|| entity.getHealth() <= 0.0f) { return true; }
		if (!this.npc.isInRange(entity, 40.0)) {
			entity.setRevengeTarget(null);
			entity.setPosition(this.npc.posX, this.npc.posY, this.npc.posZ);
			return false;
		}
		// Target
		if (!this.desTargetLost[isDead ? 1 : 0]) { return false; }
		if (entity.getAttackingEntity()==null) { this.setTarget(entity, this.getTarget()); } // try set
		if (entity.getAttackingEntity()==null) { entity.setRevengeTarget(this.getTarget()); }
		return entity.getAttackingEntity()==null;
	}

	@Override
	public IEntityLivingBase<?> spawnEntity(int slot, boolean isDead) {
		SpawnNPCData spawn = this.get(slot, isDead);
		if (spawn==null) { return null; }
		EntityLivingBase base = this.spawnEntity(spawn, isDead, null);
		if (base == null) { return null; }
		return (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(base);
	}

	public EntityLivingBase spawnEntity(SpawnNPCData sd, boolean isDead, EntityLivingBase base) {
		if (this.getTarget()==null || sd.compound == null || !sd.compound.hasKey("id")) {
			return null;
		}
		if (!isDead) {
			if (this.npc.getDistance(this.getTarget())>this.npc.stats.aggroRange) { return null; }
		}
		int type = isDead ? 1 : 0;
		EntityLivingBase trueEntity = base!=null ? base : this.npc;
		int add = !this.exact && this.spawnType[type]==1 ? 2 : 0;
		double x = trueEntity.posX + (add + this.offset[type][0]) * (this.exact ? 1 : trueEntity.getRNG().nextFloat() * (trueEntity.getRNG().nextFloat()<0.5f ? -1 : 1)) - 0.5 + trueEntity.getRNG().nextFloat();
		double y = trueEntity.posY + (add + this.offset[type][1]) * (this.exact ? 1 : trueEntity.getRNG().nextFloat() * (trueEntity.getRNG().nextFloat()<0.5f ? -1 : 1));
		double z = trueEntity.posZ + (add + this.offset[type][2]) * (this.exact ? 1 : trueEntity.getRNG().nextFloat() * (trueEntity.getRNG().nextFloat()<0.5f ? -1 : 1)) - 0.5 + trueEntity.getRNG().nextFloat();
		this.npc.getNavigator().tryMoveToXYZ(x, y, z, 1);
		Path path = this.npc.getNavigator().getPath();
		this.npc.getNavigator().tryMoveToXYZ(this.npc.posX, this.npc.posY, this.npc.posZ, 1);
		if (path!=null && path.getFinalPathPoint()!=null) { // Corrector
			x = path.getFinalPathPoint().x;
			y = path.getFinalPathPoint().y;
			z = path.getFinalPathPoint().z;
		}
		NBTTagCompound compound = sd.compound;
		if (sd.typeClones==2) {
			String name = sd.compound.getString("ClonedName");
			int tab = sd.compound.getInteger("ClonedTab");
			compound = ServerCloneController.Instance.getCloneData(null, name, tab);
			if (compound==null) { compound = sd.compound; }
			else {
				sd.compound = compound;
				sd.compound.getString("ClonedName");
				sd.compound.getInteger("ClonedTab");
			}
		}
		ServerCloneController.Instance.cleanTags(compound);
		compound.setTag("Pos", NBTTags.nbtDoubleList(x, y, z));
		Entity entity = EntityList.createEntityFromNBT(compound, trueEntity.world);
		if (entity == null || (trueEntity.world.getDifficulty() == EnumDifficulty.PEACEFUL && entity instanceof EntityMob)) {
			return null;
		}
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.ais.setStartPos(new BlockPos(npc));
		}
		trueEntity.world.spawnEntity(entity);
		EntityLivingBase living = (EntityLivingBase) entity;
		living.getEntityData().setInteger("NpcSpawnerEntityId", this.npc.getEntityId());
		living.getEntityData().setString("NpcSpawnerId", this.id);
		living.getEntityData().setInteger("NpcSpawnerSlot", this.number[type]);
		living.getEntityData().setBoolean("NpcSpawnerDead", isDead);
		this.setTarget(living, this.npc.getAttackTarget()!=null ? this.npc.getAttackTarget() : this.target);
		living.setPosition(x, y, z);
		if (living instanceof EntityNPCInterface) {
			EntityNPCInterface sSnpc = (EntityNPCInterface) living;
			sSnpc.advanced.spawner = this.npc;
			sSnpc.stats.spawnCycle = 4;
			sSnpc.stats.respawnTime = 0;
			sSnpc.ais.returnToStart = false;
			sSnpc.ais.onAttack = 0;
		}
		this.spawnedEntitys.get(isDead).add(living);
		return living;
	}

	public SpawnNPCData get(int slot, boolean isDead) {
		if (slot<0 || slot>=this.dataEntitys[isDead ? 1 : 0].length) { return null; }
		return this.dataEntitys[isDead ? 1 : 0][slot];
	}

	public boolean removeSpawned(int slot, boolean isDead) {
		int type = isDead ? 1 : 0;
		if (slot<0 || slot>=this.dataEntitys[type].length) { return false; }
		SpawnNPCData[] newSData = new SpawnNPCData[this.dataEntitys[type].length-1];
		for (int i=0, j=0; i<this.dataEntitys[type].length; i++) {
			if (i==slot) { continue;}
			newSData[j] = this.dataEntitys[type][i];
			j++;
		}
		this.dataEntitys[type] = newSData;
		return true;
	}

	public SpawnNPCData add(SpawnNPCData sd, boolean isDead) {
		int type = isDead ? 1 : 0;
		SpawnNPCData[] newSData = new SpawnNPCData[this.dataEntitys[type].length+1];
		for (int i=0 ; i<this.dataEntitys[type].length; i++) {
			newSData[i] = this.dataEntitys[type][i];
		}
		newSData[this.dataEntitys[type].length] = sd;
		this.dataEntitys[type] = newSData;
		return sd;
	}

	public int size(boolean isDead) { return this.dataEntitys[isDead ? 1 : 0].length; }

	public String getId() { return this.id; }
	
	public void clear(boolean isDead) {
		this.dataEntitys[isDead ? 1 : 0] = new SpawnNPCData[0];
	}

	public int getSpawnType(boolean isDead) {
		return this.spawnType[isDead ? 1 : 0];
	}
	
	public void setSpawnType(boolean isDead, int readInt) {
		if (readInt<0) { readInt *= -1; }
		if (readInt>2) { readInt = readInt%3; }
		this.spawnType[isDead ? 1 : 0] = readInt;
	}
	
	public boolean getDespawnOnTargetLost(boolean isDead) { return this.desTargetLost[isDead ? 1 : 0]; }
	
	public void setDespawnOnTargetLost(boolean isDead, boolean isLost) { this.desTargetLost[isDead ? 1 : 0] = isLost; }

	public int[] getOffset(boolean isDead) { return this.offset[isDead ? 1 : 0]; }

	public void setOffset(boolean isDead, int[] offset) {
		for (int i=0; i<4; i++) { this.offset[isDead ? 1 : 0][i] = offset[i]; }
	}
	
	public long getCooldown() { return this.cooldownSet; }
	
	public void setCooldown(int ticks) {
		if (ticks<0) { ticks *=-1; }
		if (ticks>6000) { ticks = 6000; }
		this.cooldownSet = ticks * 50L;
	}
	
	public void setCooldown(long ticks) {
		if (ticks<0L) { ticks *=-1; }
		if (ticks>300000L) { ticks = 300000L; }
		this.cooldownSet = ticks;
	}


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.SPAWNER;
		this.id = compound.getString("SpawnerId");
		this.dataEntitys = new SpawnNPCData[2][];
		this.offset = new int[2][];
		if (compound.hasKey("SpawnerDoesntDie", 1) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD
			this.cooldownSet = 3000L;
			this.exact = false;
			this.resetUpdate = true;
			
			int[] offset = new int[] { compound.getInteger("SpawnerXOffset"), compound.getInteger("SpawnerYOffset"), compound.getInteger("SpawnerZOffset") };
			List<SpawnNPCData> sDs = Lists.newArrayList();
			for(int i=1; i<7; i++) {
				if (!compound.hasKey("SpawnerNBT"+i, 10)) { continue; }
				SpawnNPCData sd = new SpawnNPCData();
				sd.compound = compound.getCompoundTag("SpawnerNBT"+i);
				sDs.add(sd);
			}
			int i = 0;
			if (compound.getBoolean("SpawnerDoesntDie")) { // dosent Dead
				this.spawnType[0] = 0;
				this.spawnType[1] = compound.getInteger("SpawnerType");
				this.offset[0] = new int[] { 0, 0, 0 };
				this.offset[1] = offset;
				this.desTargetLost[0] = true;
				this.desTargetLost[1] = compound.getBoolean("DespawnOnTargetLost");
				this.dataEntitys[0] = new SpawnNPCData[0];
				this.dataEntitys[1] = new SpawnNPCData[sDs.size()];
				for (SpawnNPCData sd : sDs) {
					this.dataEntitys[1][i] = sd;
					i++;
				}
			}
			else { // Alive
				this.spawnType[0] = compound.getInteger("SpawnerType");
				this.spawnType[1] = 0;
				this.offset[0] = offset;
				this.offset[1] = new int[] { 0, 0, 0 };
				this.desTargetLost[0] = compound.getBoolean("DespawnOnTargetLost");
				this.desTargetLost[1] = true;
				this.dataEntitys[0] = new SpawnNPCData[sDs.size()];
				this.dataEntitys[1] = new SpawnNPCData[0];
				for (SpawnNPCData sd : sDs) {
					this.dataEntitys[0][i] = sd;
					i++;
				}
			}
			return;
		}
		this.spawnType[0] = compound.getInteger("SpawnerWhenAlive");
		this.spawnType[1] = compound.getInteger("SpawnerWhenDead");
		this.cooldownSet = compound.getLong("SpawnerCooldownSetting");
		this.offset[0] = compound.getIntArray("OffsetWhenAlive");
		this.offset[1] = compound.getIntArray("OffsetWhenDead");
		this.desTargetLost[0] = compound.getBoolean("DespawnOnTargetLostWhenAlive");
		this.desTargetLost[1] = compound.getBoolean("DespawnOnTargetLostWhenDead");
		this.exact = compound.getBoolean("IsExactOffsetSpawn");
		this.resetUpdate = compound.getBoolean("DespawnInReset");
		for (int i=0; i<2; i++) {
			NBTTagList nbt = compound.getTagList("DataEntitysWhen"+(i==0 ? "Alive" : "Dead"), 10);
			this.dataEntitys[i] = new SpawnNPCData[nbt.tagCount()];
			for (int slot =0 ; slot<nbt.tagCount(); slot++) {
				this.dataEntitys[i][slot] = new SpawnNPCData(nbt.getCompoundTagAt(slot));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.SPAWNER.get());
		compound.setString("SpawnerId", this.id);
		compound.setInteger("SpawnerWhenAlive", this.spawnType[0]);
		compound.setInteger("SpawnerWhenDead", this.spawnType[1]);
		compound.setLong("SpawnerCooldownSetting", this.cooldownSet);
		compound.setIntArray("OffsetWhenAlive", this.offset[0]);
		compound.setIntArray("OffsetWhenDead", this.offset[1]);
		compound.setBoolean("DespawnOnTargetLostWhenAlive", this.desTargetLost[0]);
		compound.setBoolean("DespawnOnTargetLostWhenDead", this.desTargetLost[1]);
		compound.setBoolean("IsExactOffsetSpawn", this.exact);
		compound.setBoolean("DespawnInReset", this.resetUpdate);
		for (int i=0; i<2; i++) {
			NBTTagList list = new NBTTagList();
			for (SpawnNPCData sd : this.dataEntitys[i]) { list.appendTag(sd.writeToNBT()); }
			compound.setTag("DataEntitysWhen"+(i==0 ? "Alive" : "Dead"), list);
		}
		return compound;
	}
}
