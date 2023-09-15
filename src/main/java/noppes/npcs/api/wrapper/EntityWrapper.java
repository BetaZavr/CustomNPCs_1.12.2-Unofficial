package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.List;
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
import noppes.npcs.api.wrapper.data.StoredData;
import noppes.npcs.api.wrapper.data.TempData;
import noppes.npcs.controllers.ServerCloneController;

@SuppressWarnings("rawtypes")
public class EntityWrapper<T extends Entity>
implements IEntity {
	
	protected T entity;
	private IData storeddata;
	private IData tempdata;
	private IWorld worldWrapper;

	@SuppressWarnings("deprecation")
	public EntityWrapper(T entity) {
		this.entity = entity;
		this.tempdata = new TempData();
		this.storeddata = new StoredData(this);
		
		if (entity.world instanceof WorldServer) {
			this.worldWrapper = NpcAPI.Instance().getIWorld((WorldServer) entity.world);
		} else if (entity.world != null) {
			WorldWrapper w = WrapperNpcAPI.worldCache.get(entity.world.provider.getDimension());
			if (w != null) { w.world = entity.world; }
			else { WrapperNpcAPI.worldCache.put(entity.world.provider.getDimension(), w = WorldWrapper.createNew(entity.world)); }
			this.worldWrapper = w;
		}
	}

	@Override
	public void addRider(IEntity entity) {
		if (entity != null) {
			entity.getMCEntity().startRiding(this.entity, true);
		}
	}

	@Override
	public void addTag(String tag) {
		this.entity.addTag(tag);
	}

	@Override
	public void clearRiders() {
		this.entity.removePassengers();
	}

	@Override
	public void damage(float amount) {
		this.entity.attackEntityFrom(DamageSource.GENERIC, amount);
	}

	@Override
	public void damage(float amount, IEntityDamageSource source) {
		if (!(this.entity instanceof EntityLivingBase)) { return; }
		if (source instanceof EntityDamageSource) {
			this.entity.attackEntityFrom((EntityDamageSource) source, amount);
			if (((EntityDamageSource) source).getTrueSource() instanceof EntityLivingBase) {
				if (this.entity instanceof EntityLiving) {
					((EntityLiving) this.entity).setAttackTarget((EntityLivingBase) ((EntityDamageSource) source).getTrueSource());
				}
				if (this.entity instanceof EntityLivingBase) {
					((EntityLivingBase) this.entity).setRevengeTarget((EntityLivingBase) ((EntityDamageSource) source).getTrueSource());
				}
			}
		}
		else { this.damage(amount); }
	}

	@Override
	public void despawn() {
		this.entity.isDead = true;
	}

	@Override
	public IEntityItem dropItem(IItemStack item) {
		return (IEntityItem) NpcAPI.Instance().getIEntity(this.entity.entityDropItem(item.getMCItemStack(), 0.0f));
	}

	@Override
	public void extinguish() {
		this.entity.extinguish();
	}

	private IEntity[] findEntityOnPath(double distance, Vec3d vec3d, Vec3d vec3d1) {
		List<Entity> list = this.entity.world.getEntitiesWithinAABBExcludingEntity(this.entity, this.entity.getEntityBoundingBox().grow(distance));
		List<IEntity> result = new ArrayList<IEntity>();
		for (Entity entity1 : list) {
			if (entity1.canBeCollidedWith() && entity1 != this.entity) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
				RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
				if (raytraceresult1 == null) {
					continue;
				}
				result.add(NpcAPI.Instance().getIEntity(entity1));
			}
		}
		result.sort((o1, o2) -> {
			double d1 = this.entity.getDistance(o1.getMCEntity());
			double d2 = this.entity.getDistance(o2.getMCEntity());
			if (d1 == d2) {
				return 0;
			} else {
				return (d1 > d2) ? 1 : -1;
			}
		});
		return result.toArray(new IEntity[result.size()]);
	}

	@Override
	public String generateNewUUID() {
		UUID id = UUID.randomUUID();
		this.entity.setUniqueId(id);
		return id.toString();
	}

	@Override
	public long getAge() {
		return this.entity.ticksExisted;
	}

	@Override
	public IEntity[] getAllRiders() {
		List<Entity> list = new ArrayList<Entity>(this.entity.getRecursivePassengers());
		IEntity[] riders = new IEntity[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			riders[i] = NpcAPI.Instance().getIEntity(list.get(i));
		}
		return riders;
	}

	@Override
	public int getBlockX() {
		return MathHelper.floor(this.entity.posX);
	}

	@Override
	public int getBlockY() {
		return MathHelper.floor(this.entity.posY);
	}

	@Override
	public int getBlockZ() {
		return MathHelper.floor(this.entity.posZ);
	}

	@Override
	public String getEntityName() {
		String s = EntityList.getEntityString(this.entity);
		if (s == null) {
			s = "generic";
		}
		return new TextComponentTranslation("entity." + s + ".name").getFormattedText();
	}

	@Override
	public INbt getEntityNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		this.entity.writeToNBT(compound);
		ResourceLocation resourcelocation = EntityList.getKey(this.entity);
		if (this.getType() == 1) {
			resourcelocation = new ResourceLocation("player");
		}
		if (resourcelocation != null) {
			compound.setString("id", resourcelocation.toString());
		}
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public float getEyeHeight() {
		return this.entity.getEyeHeight();
	}

	@Override
	public float getHeight() {
		return this.entity.height;
	}

	@Override
	public T getMCEntity() {
		return this.entity;
	}

	@Override
	public double getMotionX() {
		return this.entity.motionX;
	}

	@Override
	public double getMotionY() {
		return this.entity.motionY;
	}

	@Override
	public double getMotionZ() {
		return this.entity.motionZ;
	}

	@Override
	public IEntity getMount() {
		return NpcAPI.Instance().getIEntity(this.entity.getRidingEntity());
	}

	@Override
	public String getName() {
		return this.entity.getName();
	}

	@Override
	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(this.entity.getEntityData());
	}

	@Override
	public float getPitch() {
		return this.entity.rotationPitch;
	}

	@Override
	public IPos getPos() {
		return new BlockPosWrapper(this.entity.getPosition());
	}

	@Override
	public IEntity[] getRiders() {
		List<Entity> list = (List<Entity>) this.entity.getPassengers();
		IEntity[] riders = new IEntity[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			riders[i] = NpcAPI.Instance().getIEntity(list.get(i));
		}
		return riders;
	}

	@Override
	public float getRotation() {
		return this.entity.rotationYaw;
	}

	@Override
	public IData getStoreddata() {
		return this.storeddata;
	}

	@Override
	public String[] getTags() {
		return this.entity.getTags().toArray(new String[this.entity.getTags().size()]);
	}

	@Override
	public IData getTempdata() {
		return this.tempdata;
	}

	@Override
	public int getType() {
		return EntityType.UNKNOWN.get();
	}

	@Override
	public String getTypeName() {
		return EntityList.getEntityString(this.entity);
	}

	@Override
	public String getUUID() {
		return this.entity.getUniqueID().toString();
	}

	@Override
	public float getWidth() {
		return this.entity.width;
	}

	@Override
	public IWorld getWorld() {
		if (this.entity.world != this.worldWrapper.getMCWorld() && this.entity.world instanceof WorldServer) {
			this.worldWrapper = NpcAPI.Instance().getIWorld((WorldServer) this.entity.world);
		}
		return this.worldWrapper;
	}

	@Override
	public double getX() {
		return this.entity.posX;
	}

	@Override
	public double getY() {
		return this.entity.posY;
	}

	@Override
	public double getZ() {
		return this.entity.posZ;
	}

	@Override
	public boolean hasCustomName() {
		return this.entity.hasCustomName();
	}

	@Override
	public boolean hasTag(String tag) {
		return this.entity.getTags().contains(tag);
	}

	@Override
	public boolean inFire() {
		return this.entity.isInsideOfMaterial(Material.FIRE);
	}

	@Override
	public boolean inLava() {
		return this.entity.isInsideOfMaterial(Material.LAVA);
	}

	@Override
	public boolean inWater() {
		return this.entity.isInsideOfMaterial(Material.WATER);
	}

	@Override
	public boolean isAlive() {
		return this.entity.isEntityAlive();
	}

	@Override
	public boolean isBurning() {
		return this.entity.isBurning();
	}

	@Override
	public boolean isSneaking() {
		return this.entity.isSneaking();
	}

	@Override
	public boolean isSprinting() {
		return this.entity.isSprinting();
	}

	@Override
	public void kill() {
		this.entity.setDead();
	}

	@Override
	public void knockback(int power, float direction) {
		float v = direction * 3.1415927f / 180.0f;
		this.entity.addVelocity((-MathHelper.sin(v) * power), 0.1 + power * 0.04f, (MathHelper.cos(v) * power));
		Entity entity = this.entity;
		entity.motionX *= 0.6;
		Entity entity2 = this.entity;
		entity2.motionZ *= 0.6;
		this.entity.velocityChanged = true;
	}

	@Override
	public void playAnimation(int type) {
		if (!(this.worldWrapper.getMCWorld() instanceof WorldServer)) { return; }
		((WorldServer) this.worldWrapper.getMCWorld()).getEntityTracker().sendToTrackingAndSelf(this.entity, new SPacketAnimation(this.entity, type));
	}

	@Override
	public IRayTrace rayTraceBlock(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		Vec3d vec3d = this.entity.getPositionEyes(1.0f);
		Vec3d vec3d2 = this.entity.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
		RayTraceResult result = this.entity.world.rayTraceBlocks(vec3d, vec3d3, stopOnLiquid, ignoreBlockWithoutBoundingBox, true);
		if (result == null) {
			return null;
		}
		return new RayTraceWrapper(NpcAPI.Instance().getIBlock(this.entity.world, result.getBlockPos()), result.sideHit.getIndex());
	}

	@Override
	public IEntity[] rayTraceEntities(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		Vec3d vec3d = this.entity.getPositionEyes(1.0f);
		Vec3d vec3d2 = this.entity.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
		RayTraceResult result = this.entity.world.rayTraceBlocks(vec3d, vec3d3, stopOnLiquid, ignoreBlockWithoutBoundingBox, false);
		if (result != null) {
			vec3d3 = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
		}
		return this.findEntityOnPath(distance, vec3d, vec3d3);
	}

	@Override
	public void removeTag(String tag) {
		this.entity.removeTag(tag);
	}

	@Override
	public void setBurning(int ticks) {
		this.entity.setFire(ticks);
	}

	@Override
	public void setEntityNbt(INbt nbt) {
		this.entity.readFromNBT(nbt.getMCNBT());
	}

	@Override
	public void setMotionX(double motion) {
		if (this.entity.motionX == motion) {
			return;
		}
		this.entity.motionX = motion;
		this.entity.velocityChanged = true;
	}

	@Override
	public void setMotionY(double motion) {
		if (this.entity.motionY == motion) {
			return;
		}
		this.entity.motionY = motion;
		this.entity.velocityChanged = true;
	}

	@Override
	public void setMotionZ(double motion) {
		if (this.entity.motionZ == motion) {
			return;
		}
		this.entity.motionZ = motion;
		this.entity.velocityChanged = true;
	}

	@Override
	public void setMount(IEntity entity) {
		if (entity == null) {
			this.entity.dismountRidingEntity();
		} else {
			this.entity.startRiding(entity.getMCEntity(), true);
		}
	}

	@Override
	public void setName(String name) {
		this.entity.setCustomNameTag(name);
	}

	@Override
	public void setPitch(float rotation) {
		this.entity.rotationPitch = rotation;
	}

	@Override
	public void setPos(IPos pos) {
		this.entity.setPosition((pos.getX() + 0.5f), pos.getY(), (pos.getZ() + 0.5f));
	}

	@Override
	public void setPosition(double x, double y, double z) {
		if (this.entity instanceof EntityPlayerMP) {
			((EntityPlayerMP) this.entity).setPositionAndRotation(x, y, z, this.entity.rotationYaw, this.entity.rotationPitch);
		} else {
			this.entity.setPosition(x, y, z);
		}
	}

	@Override
	public void setRotation(float rotation) {
		this.entity.rotationYaw = rotation;
	}

	@Override
	public void setX(double x) {
		this.entity.posX = x;
	}

	@Override
	public void setY(double y) {
		this.entity.posY = y;
	}

	@Override
	public void setZ(double z) {
		this.entity.posZ = z;
	}

	@Override
	public void spawn() {
		Entity el = null;
		try {
			for (Entity e : this.worldWrapper.getMCWorld().getLoadedEntityList()) {
				if (e.getUniqueID().equals(this.entity.getUniqueID())) {
					el = e;
					break;
				}
			}
		} catch (Exception e) {}
		if (el != null) {
			throw new CustomNPCsException("Entity is already spawned", new Object[0]);
		}
		this.entity.isDead = false;
		try { this.worldWrapper.getMCWorld().spawnEntity(this.entity); } catch (Exception e) {}
	}

	@Override
	public void storeAsClone(int tab, String name) {
		NBTTagCompound compound = new NBTTagCompound();
		if (!this.entity.writeToNBTAtomically(compound)) {
			throw new CustomNPCsException("Cannot store dead entities", new Object[0]);
		}
		ServerCloneController.Instance.addClone(compound, name, tab);
	}

	@Override
	public boolean typeOf(int type) {
		return type == this.getType();
	}
}
