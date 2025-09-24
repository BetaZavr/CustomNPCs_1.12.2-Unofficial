package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import noppes.npcs.LogWriter;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.mixin.entity.IEntityMixin;
import noppes.npcs.api.wrapper.data.Data;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.util.Util;

@SuppressWarnings("rawtypes")
public class EntityWrapper<T extends Entity> implements IEntity {

	public static List<Entity> findEntityOnPath(Entity entity, double distance, Vec3d vec3d, Vec3d vec3d1) {
		List<Entity> result = new ArrayList<>();
		for (Entity entity1 : Util.instance.getEntitiesWithinDist(Entity.class, entity.world, entity, distance)) {
			if (entity1.canBeCollidedWith() && entity1 != entity) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d1);
				if (raytraceresult == null) { continue; }
				result.add(entity1);
			}
		}
		result.sort((o1, o2) -> {
			double d1 = entity.getDistance(o1);
			double d2 = entity.getDistance(o2);
			if (d1 == d2) { return 0; }
			else { return (d1 > d2) ? 1 : -1; }
		});
		return result;
	}

	public static IEntity[] findIEntityOnPath(Entity entity, double distance, Vec3d vec3d, Vec3d vec3d1) {
		List<IEntity<?>> result = new ArrayList<>();
		for (Entity e : findEntityOnPath(entity, distance, vec3d, vec3d1)) { result.add(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(e)); }
		return result.toArray(new IEntity[0]);
	}

	protected T entity;
	protected final Data storeddata;
	protected final Data tempdata = new Data();

	private IWorld worldWrapper;

	public EntityWrapper(T entityIn) {
		entity = entityIn;
		storeddata = ((IEntityMixin) entityIn).npcs$getStoredData();
		resetWorld();
	}

	@SuppressWarnings("all")
	private void resetWorld() {
		if (entity.world instanceof WorldServer) {
			worldWrapper = Objects.requireNonNull(NpcAPI.Instance()).getIWorld(entity.world);
		}
		else if (entity.world != null) {
			WorldWrapper w = WrapperNpcAPI.worldCache.get(entity.world.provider.getDimension());
			if (w != null) {
				if (w.world == null) { w.world = entity.world; }
			}
			else { WrapperNpcAPI.worldCache.put(entity.world.provider.getDimension(), w = WorldWrapper.createNew(entity.world)); }
			worldWrapper = w;
		}
	}

	@Override
	public void addRider(IEntity entityIn) {
		if (entityIn != null) { entityIn.getMCEntity().startRiding(entity, true); }
	}

	@Override
	public void addTag(String tag) { entity.addTag(tag); }

	@Override
	public void clearRiders() { entity.removePassengers(); }

	@Override
	public void damage(float amount) { entity.attackEntityFrom(DamageSource.GENERIC, amount); }

	@Override
	public void damage(float amount, IEntityDamageSource source) {
		if (!(entity instanceof EntityLivingBase)) { return; }
		if (source instanceof EntityDamageSource) {
			entity.attackEntityFrom((DamageSource) source, amount);
			if (((EntityDamageSource) source).getTrueSource() instanceof EntityLivingBase) {
				if (entity instanceof EntityLiving) {
					((EntityLiving) entity).setAttackTarget((EntityLivingBase) ((EntityDamageSource) source).getTrueSource());
				}
				if (entity instanceof EntityLivingBase) {
					((EntityLivingBase) entity).setRevengeTarget((EntityLivingBase) ((EntityDamageSource) source).getTrueSource());
				}
			}
		}
		else { damage(amount); }
	}

	@Override
	public void despawn() { entity.isDead = true; }

	@Override
	public IEntityItem dropItem(IItemStack item) { return (IEntityItem) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity.entityDropItem(item.getMCItemStack(), 0.0f)); }

	@Override
	public void extinguish() { entity.extinguish(); }

	@Override
	public String generateNewUUID() {
		UUID id = UUID.randomUUID();
		entity.setUniqueId(id);
		return id.toString();
	}

	@Override
	public long getAge() { return entity.ticksExisted; }

	@Override
	public IEntity[] getAllRiders() {
		List<Entity> list = new ArrayList<>(entity.getRecursivePassengers());
		IEntity[] riders = new IEntity[list.size()];
		for (int i = 0; i < list.size(); ++i) { riders[i] = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(list.get(i)); }
		return riders;
	}

	@Override
	public int getBlockX() { return MathHelper.floor(entity.posX); }

	@Override
	public int getBlockY() { return MathHelper.floor(entity.posY); }

	@Override
	public int getBlockZ() { return MathHelper.floor(entity.posZ); }

	@Override
	public String getEntityName() {
		String s = EntityList.getEntityString(entity);
		if (s == null) { s = "generic"; }
		return new TextComponentTranslation("entity." + s + ".name").getFormattedText();
	}

	@Override
	public INbt getEntityNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		entity.writeToNBT(compound);
		ResourceLocation resourcelocation = EntityList.getKey(entity);
		if (getType() == 1) { resourcelocation = new ResourceLocation("player"); }
		if (resourcelocation != null) { compound.setString("id", resourcelocation.toString()); }
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public float getEyeHeight() { return entity.getEyeHeight(); }

	@Override
	public float getHeight() { return entity.height; }

	@Override
	public T getMCEntity() { return entity; }

	@Override
	public double getMotionX() { return entity.motionX; }

	@Override
	public double getMotionY() { return entity.motionY; }

	@Override
	public double getMotionZ() { return entity.motionZ; }

	@Override
	public IEntity getMount() { return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity.getRidingEntity()); }

	@Override
	public String getName() { return entity.getName(); }

	@Override
	public INbt getNbt() { return Objects.requireNonNull(NpcAPI.Instance()).getINbt(entity.getEntityData()); }

	@Override
	public float getPitch() { return entity.rotationPitch; }

	@Override
	public IPos getPos() { return new BlockPosWrapper(entity.posX, entity.posY, entity.posZ); }

	@Override
	public IEntity[] getRiders() {
		List<Entity> list = entity.getPassengers();
		IEntity[] riders = new IEntity[list.size()];
		for (int i = 0; i < list.size(); ++i) { riders[i] = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(list.get(i)); }
		return riders;
	}

	@Override
	public float getRotation() { return entity.rotationYaw; }

	@Override
	public IData getStoreddata() { return storeddata; }

	@Override
	public String[] getTags() { return entity.getTags().toArray(new String[0]); }

	@Override
	public IData getTempdata() { return tempdata; }

	@Override
	public int getType() { return EntityType.UNKNOWN.get(); }

	@Override
	public String getTypeName() { return EntityList.getEntityString(entity); }

	@Override
	public String getUUID() {
		return entity.getUniqueID().toString();
	}

	@Override
	public float getWidth() { return entity.width; }

	@Override
	public IWorld getWorld() {
		if (worldWrapper == null || entity.world != worldWrapper.getMCWorld()) { resetWorld(); }
		return worldWrapper;
	}

	@Override
	public double getX() { return entity.posX; }

	@Override
	public double getY() { return entity.posY; }

	@Override
	public double getZ() {return entity.posZ; }

	@Override
	public boolean hasCustomName() { return entity.hasCustomName(); }

	@Override
	public boolean hasTag(String tag) { return entity.getTags().contains(tag); }

	@Override
	public boolean inFire() { return entity.isInsideOfMaterial(Material.FIRE); }

	@Override
	public boolean inLava() { return entity.isInsideOfMaterial(Material.LAVA); }

	@Override
	public boolean inWater() { return entity.isInsideOfMaterial(Material.WATER); }

	@Override
	public boolean isAlive() { return entity.isEntityAlive(); }

	@Override
	public boolean isBurning() { return entity.isBurning(); }

	@Override
	public boolean isSneaking() { return entity.isSneaking(); }

	@Override
	public boolean isSprinting() { return entity.isSprinting(); }

	@Override
	public void kill() { entity.setDead(); }

	@Override
	public void knockback(int power, float direction) {
		float v = direction * 3.1415927f / 180.0f;
		entity.addVelocity((-MathHelper.sin(v) * power), 0.1 + power * 0.04f, (MathHelper.cos(v) * power));
		entity.motionX *= 0.6;
		entity.motionZ *= 0.6;
		entity.velocityChanged = true;
	}

	@Override
	public void playAnimation(int type) {
		if (!(worldWrapper.getMCWorld() instanceof WorldServer)) { return; }
		((WorldServer) worldWrapper.getMCWorld()).getEntityTracker().sendToTrackingAndSelf(entity, new SPacketAnimation(entity, type));
	}

	@Override
	public IRayTrace rayTraceBlock(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		Vec3d vec3d = entity.getPositionEyes(1.0f);
		Vec3d vec3d2 = entity.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
		RayTraceResult result = entity.world.rayTraceBlocks(vec3d, vec3d3, stopOnLiquid, ignoreBlockWithoutBoundingBox, true);
		if (result == null) { return null; }
		return new RayTraceWrapper(Objects.requireNonNull(NpcAPI.Instance()).getIBlock(entity.world, result.getBlockPos()), result.sideHit.getIndex());
	}

	@Override
	public IEntity[] rayTraceEntities(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		Vec3d vec3d = entity.getPositionEyes(1.0f);
		Vec3d vec3d2 = entity.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
		RayTraceResult result = entity.world.rayTraceBlocks(vec3d, vec3d3, stopOnLiquid, ignoreBlockWithoutBoundingBox, false);
		if (result != null) { vec3d3 = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z); }
		return EntityWrapper.findIEntityOnPath(entity, distance, vec3d, vec3d3);
	}

	@Override
	public void removeTag(String tag) { entity.removeTag(tag); }

	@Override
	public void setBurning(int ticks) { entity.setFire(ticks); }

	@Override
	public void setEntityNbt(INbt nbt) { entity.readFromNBT(nbt.getMCNBT()); }

	@Override
	public void setMotionX(double motion) {
		if (entity.motionX == motion) { return; }
		entity.motionX = motion;
		entity.velocityChanged = true;
	}

	@Override
	public void setMotionY(double motion) {
		if (entity.motionY == motion) { return; }
		entity.motionY = motion;
		entity.velocityChanged = true;
	}

	@Override
	public void setMotionZ(double motion) {
		if (entity.motionZ == motion) { return; }
		entity.motionZ = motion;
		entity.velocityChanged = true;
	}

	@Override
	public void setMount(IEntity entityIn) {
		if (entityIn == null) { entity.dismountRidingEntity(); }
		else { entity.startRiding(entityIn.getMCEntity(), true); }
	}

	@Override
	public void setName(String name) { entity.setCustomNameTag(name); }

	@Override
	public void setPitch(float rotation) { entity.rotationPitch = rotation; }

	@Override
	public void setPos(IPos pos) { entity.setPosition((pos.getX() + 0.5f), pos.getY(), (pos.getZ() + 0.5f)); }

	@Override
	public void setPosition(double x, double y, double z) {
		if (entity instanceof EntityPlayerMP) { entity.setPositionAndRotation(x, y, z, entity.rotationYaw, entity.rotationPitch); }
		else { entity.setPosition(x, y, z); }
	}

	@Override
	public void setRotation(float rotation) { entity.rotationYaw = rotation % 360.0f; }

	@Override
	public void setX(double x) { entity.posX = x; }

	@Override
	public void setY(double y) { entity.posY = y; }

	@Override
	public void setZ(double z) { entity.posZ = z; }

	@Override
	public void spawn() {
		if (worldWrapper.getMCWorld().isRemote) { return; }
		LogWriter.debug("Try summoning 0: " + entity.getName() + "; UUID: " + entity.getUniqueID());
		Entity el = null;
		try {
			for (Entity e : worldWrapper.getMCWorld().loadedEntityList) {
				if (e.getUniqueID().equals(entity.getUniqueID())) {
					el = e;
					break;
				}
			}
		} catch (Exception e) { LogWriter.error(e); }
		if (el != null) {
			LogWriter.debug("Error summoning: " + entity.getName());
			throw new CustomNPCsException("Entity is already spawned");
		}
		entity.isDead = false;
		LogWriter.debug("Try summoning 1: " + entity.getName());
		try {
			boolean bo = worldWrapper.getMCWorld().spawnEntity(entity);
			LogWriter.debug("Is summoning: " + bo + "; World: " + entity.world.getClass().getSimpleName());
		}
		catch (Exception e) { LogWriter.error(e); }
	}

	@Override
	public void storeAsClone(int tab, String name) {
		NBTTagCompound compound = new NBTTagCompound();
		if (!entity.writeToNBTAtomically(compound)) { throw new CustomNPCsException("Cannot store dead entities"); }
		ServerCloneController.Instance.addClone(compound, name, tab);
	}

	@Override
	public boolean typeOf(int type) { return type == getType(); }

}
