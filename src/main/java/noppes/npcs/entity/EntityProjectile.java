package noppes.npcs.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.ParticleType;
import noppes.npcs.api.constants.PotionEffectType;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.entity.data.DataRanged;

public class EntityProjectile extends EntityThrowable {

	public interface IProjectileCallback {
		boolean onImpact(EntityProjectile p0, BlockPos p1, Entity p2);
	}

	private static DataParameter<Boolean> Arrow = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Boolean> Glows = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Boolean> Gravity = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Boolean> Is3d = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<ItemStack> ItemStackThrown = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.ITEM_STACK);
	private static DataParameter<Integer> Particle = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.VARINT);
	private static DataParameter<Boolean> Rotating = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Integer> Size = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.VARINT);
	private static DataParameter<Boolean> Sticks = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Integer> Velocity = EntityDataManager.createKey(EntityProjectile.class,
			DataSerializers.VARINT);
	public boolean accelerate;
	private double accelerationX;
	private double accelerationY;
	private double accelerationZ;
	public int accuracy;
	public int amplify;
	public int arrowShake;
	public IProjectileCallback callback;
	public boolean canBePickedUp;
	public float damage;
	public boolean destroyedOnEntityHit;
	public int duration;
	public int effect;
	public boolean explosiveDamage;
	public int explosiveRadius;
	private int inData;
	protected boolean inGround;
	private Block inTile;
	private EntityNPCInterface npc;
	public int punch;
	public List<ScriptContainer> scripts;
	public int throwableShake;
	private EntityLivingBase thrower;
	private String throwerName;
	public int ticksInAir;
	private int ticksInGround;

	private BlockPos tilePos;

	public EntityProjectile(World par1World) {
		super(par1World);
		this.tilePos = BlockPos.ORIGIN;
		this.inGround = false;
		this.inData = 0;
		this.throwableShake = 0;
		this.arrowShake = 0;
		this.canBePickedUp = false;
		this.destroyedOnEntityHit = true;
		this.throwerName = null;
		this.ticksInAir = 0;
		this.damage = 5.0f;
		this.punch = 0;
		this.accelerate = false;
		this.explosiveDamage = true;
		this.explosiveRadius = 0;
		this.effect = 0;
		this.duration = 5;
		this.amplify = 0;
		this.accuracy = 60;
		this.scripts = new ArrayList<ScriptContainer>();
		this.setSize(0.25f, 0.25f);
	}

	public EntityProjectile(World par1World, EntityLivingBase par2EntityLiving, ItemStack item, boolean isNPC) {
		super(par1World);
		this.tilePos = BlockPos.ORIGIN;
		this.inGround = false;
		this.inData = 0;
		this.throwableShake = 0;
		this.arrowShake = 0;
		this.canBePickedUp = false;
		this.destroyedOnEntityHit = true;
		this.throwerName = null;
		this.ticksInAir = 0;
		this.damage = 5.0f;
		this.punch = 0;
		this.accelerate = false;
		this.explosiveDamage = true;
		this.explosiveRadius = 0;
		this.effect = 0;
		this.duration = 5;
		this.amplify = 0;
		this.accuracy = 60;
		this.scripts = new ArrayList<ScriptContainer>();
		this.thrower = par2EntityLiving;
		if (this.thrower != null) {
			this.throwerName = this.thrower.getUniqueID().toString();
		}
		this.setThrownItem(item);
		this.dataManager.set(EntityProjectile.Arrow, (this.getItem() == Items.ARROW));
		this.setSize(this.getSize() / 10.0f, this.getSize() / 10.0f);
		this.setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + par2EntityLiving.getEyeHeight(),
				par2EntityLiving.posZ, par2EntityLiving.rotationYaw, par2EntityLiving.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0f * 3.1415927f) * 0.1f;
		this.posY -= 0.10000000149011612;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0f * 3.1415927f) * 0.1f;
		this.setPosition(this.posX, this.posY, this.posZ);
		if (isNPC) {
			this.npc = (EntityNPCInterface) this.thrower;
			this.getStatProperties(this.npc.stats.ranged);
		}
	}

	protected boolean canTriggerWalking() {
		return false;
	}

	protected void entityInit() {
		this.dataManager.register(EntityProjectile.ItemStackThrown, ItemStack.EMPTY);
		this.dataManager.register(EntityProjectile.Velocity, 10);
		this.dataManager.register(EntityProjectile.Size, 10);
		this.dataManager.register(EntityProjectile.Particle, 0);
		this.dataManager.register(EntityProjectile.Gravity, false);
		this.dataManager.register(EntityProjectile.Glows, false);
		this.dataManager.register(EntityProjectile.Arrow, false);
		this.dataManager.register(EntityProjectile.Is3d, false);
		this.dataManager.register(EntityProjectile.Rotating, false);
		this.dataManager.register(EntityProjectile.Sticks, false);
	}

	public float getAngleForXYZ(double varX, double varY, double varZ, double horiDist, boolean arc) {
		float g = this.getGravityVelocity();
		float var1 = this.getSpeed() * this.getSpeed();
		double var2 = g * horiDist;
		double var3 = g * horiDist * horiDist + 2.0 * varY * var1;
		double var4 = var1 * var1 - g * var3;
		if (var4 < 0.0) {
			return 30.0f;
		}
		float var5 = arc ? (var1 + MathHelper.sqrt(var4)) : (var1 - MathHelper.sqrt(var4));
		float var6 = (float) Math.atan2(var5, var2) * 180.0f / 3.141592653589793f;
		return var6;
	}

	public float getBrightness() {
		return this.dataManager.get(EntityProjectile.Glows) ? 1.0f : super.getBrightness();
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return this.dataManager.get(EntityProjectile.Glows) ? 15728880 : super.getBrightnessForRender();
	}

	public ITextComponent getDisplayName() {
		if (!this.getItemDisplay().isEmpty()) {
			return new TextComponentTranslation(this.getItemDisplay().getDisplayName(), new Object[0]);
		}
		return super.getDisplayName();
	}

	private Item getItem() {
		ItemStack item = this.getItemDisplay();
		if (item.isEmpty()) {
			return Items.AIR;
		}
		return item.getItem();
	}

	public ItemStack getItemDisplay() {
		try {
			return this.dataManager.get(EntityProjectile.ItemStackThrown);
		} catch (Exception ex) {
			return ItemStack.EMPTY;
		}
	}

	protected float getMotionFactor() {
		return this.accelerate ? 0.95f : 1.0f;
	}

	private int getPotionColor(int p) {
		switch (p) {
		case 2: {
			return 32660;
		}
		case 3: {
			return 32660;
		}
		case 4: {
			return 32696;
		}
		case 5: {
			return 32698;
		}
		case 6: {
			return 32732;
		}
		case 7: {
			return 15;
		}
		case 8: {
			return 32732;
		}
		default: {
			return 0;
		}
		}
	}

	public int getSize() {
		return this.dataManager.get(EntityProjectile.Size);
	}

	public float getSpeed() {
		return this.dataManager.get(EntityProjectile.Velocity) / 10.0f;
	}

	public void getStatProperties(DataRanged stats) {
		this.damage = stats.getStrength();
		this.punch = stats.getKnockback();
		this.accelerate = stats.getAccelerate();
		this.explosiveRadius = stats.getExplodeSize();
		this.effect = stats.getEffectType();
		this.duration = stats.getEffectTime();
		this.amplify = stats.getEffectStrength();
		this.setParticleEffect(stats.getParticle());
		this.dataManager.set(EntityProjectile.Size, stats.getSize());
		this.dataManager.set(EntityProjectile.Glows, stats.getGlows());
		this.setSpeed(stats.getSpeed());
		this.setHasGravity(stats.getHasGravity());
		this.setIs3D(stats.getRender3D());
		this.setRotating(stats.getSpins());
		this.setStickInWall(stats.getSticks());
	}

	public EntityLivingBase getThrower() {
		if (this.throwerName == null || this.throwerName.isEmpty()) {
			return null;
		}
		try {
			UUID uuid = UUID.fromString(this.throwerName);
			if (this.thrower == null && uuid != null) {
				this.thrower = this.world.getPlayerEntityByUUID(uuid);
			}
		} catch (IllegalArgumentException ex) {
		}
		return this.thrower;
	}

	public boolean glows() {
		return (boolean) this.dataManager.get(EntityProjectile.Glows);
	}

	public boolean hasGravity() {
		return (boolean) this.dataManager.get(EntityProjectile.Gravity);
	}

	public boolean is3D() {
		return (boolean) this.dataManager.get(EntityProjectile.Is3d) || this.isBlock();
	}

	public boolean isArrow() {
		return (boolean) this.dataManager.get(EntityProjectile.Arrow);
	}

	public boolean isBlock() {
		ItemStack item = this.getItemDisplay();
		return !item.isEmpty() && item.getItem() instanceof ItemBlock;
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double par1) {
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0;
		d1 *= 64.0;
		return par1 < d1 * d1;
	}

	public boolean isRotating() {
		return (boolean) this.dataManager.get(EntityProjectile.Rotating);
	}

	public void onCollideWithPlayer(EntityPlayer par1EntityPlayer) {
		if (this.world.isRemote || !this.canBePickedUp || !this.inGround || this.arrowShake > 0) {
			return;
		}
		if (par1EntityPlayer.inventory.addItemStackToInventory(this.getItemDisplay())) {
			this.inGround = false;
			this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2f,
					((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7f + 1.0f) * 2.0f);
			par1EntityPlayer.onItemPickup(this, 1);
			this.setDead();
		}
	}

	protected void onImpact(RayTraceResult movingobjectposition) {
		if (!this.world.isRemote) {
			BlockPos pos = null;
			ProjectileEvent.ImpactEvent event;
			if (movingobjectposition.entityHit != null) {
				pos = movingobjectposition.entityHit.getPosition();
				event = new ProjectileEvent.ImpactEvent((IProjectile<?>) NpcAPI.Instance().getIEntity(this), 0,
						movingobjectposition.entityHit);
			} else {
				pos = movingobjectposition.getBlockPos();
				event = new ProjectileEvent.ImpactEvent((IProjectile<?>) NpcAPI.Instance().getIEntity(this), 1,
						NpcAPI.Instance().getIBlock(this.world, pos));
			}
			if (pos == BlockPos.ORIGIN) {
				pos = new BlockPos(movingobjectposition.hitVec);
			}
			if (this.callback != null && this.callback.onImpact(this, pos, movingobjectposition.entityHit)) {
				return;
			}
			EventHooks.onProjectileImpact(this, event);
		}
		if (movingobjectposition.entityHit != null) {
			float damage = this.damage;
			if (damage == 0.0f) {
				damage = 0.001f;
			}
			if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()),
					damage)) {
				if (movingobjectposition.entityHit instanceof EntityLivingBase
						&& (this.isArrow() || this.sticksToWalls())) {
					EntityLivingBase entityliving = (EntityLivingBase) movingobjectposition.entityHit;
					if (!this.world.isRemote) {
						entityliving.setArrowCountInEntity(entityliving.getArrowCountInEntity() + 1);
					}
					if (this.destroyedOnEntityHit && !(movingobjectposition.entityHit instanceof EntityEnderman)) {
						this.setDead();
					}
				}
				if (this.isBlock()) {
					this.world.playEvent((EntityPlayer) null, 2001, movingobjectposition.entityHit.getPosition(),
							Item.getIdFromItem(this.getItem()));
				} else if (!this.isArrow() && !this.sticksToWalls()) {
					int[] intArr = { Item.getIdFromItem(this.getItem()) };
					if (this.getItem().getHasSubtypes()) {
						intArr = new int[] { Item.getIdFromItem(this.getItem()), this.getItemDisplay().getMetadata() };
					}
					for (int i = 0; i < 8; ++i) {
						this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ,
								this.rand.nextGaussian() * 0.15, this.rand.nextGaussian() * 0.2,
								this.rand.nextGaussian() * 0.15, intArr);
					}
				}
				if (this.punch > 0) {
					float f3 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
					if (f3 > 0.0f) {
						movingobjectposition.entityHit.addVelocity(this.motionX * this.punch * 0.6000000238418579 / f3,
								0.1, this.motionZ * this.punch * 0.6000000238418579 / f3);
					}
				}
				if (this.effect != 0 && movingobjectposition.entityHit instanceof EntityLivingBase) {
					if (this.effect != 1) {
						Potion p = PotionEffectType.getMCType(this.effect);
						((EntityLivingBase) movingobjectposition.entityHit)
								.addPotionEffect(new PotionEffect(p, this.duration * 20, this.amplify));
					} else {
						movingobjectposition.entityHit.setFire(this.duration);
					}
				}
			} else if (this.hasGravity() && (this.isArrow() || this.sticksToWalls())) {
				this.motionX *= -0.10000000149011612;
				this.motionY *= -0.10000000149011612;
				this.motionZ *= -0.10000000149011612;
				this.rotationYaw += 180.0f;
				this.prevRotationYaw += 180.0f;
				this.ticksInAir = 0;
			}
		} else if (this.isArrow() || this.sticksToWalls()) {
			this.tilePos = movingobjectposition.getBlockPos();
			IBlockState state = this.world.getBlockState(this.tilePos);
			this.inTile = state.getBlock();
			this.inData = this.inTile.getMetaFromState(state);
			this.motionX = (movingobjectposition.hitVec.x - this.posX);
			this.motionY = (movingobjectposition.hitVec.y - this.posY);
			this.motionZ = (movingobjectposition.hitVec.z - this.posZ);
			float f4 = MathHelper
					.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			this.posX -= this.motionX / f4 * 0.05000000074505806;
			this.posY -= this.motionY / f4 * 0.05000000074505806;
			this.posZ -= this.motionZ / f4 * 0.05000000074505806;
			this.inGround = true;
			this.arrowShake = 7;
			if (!this.hasGravity()) {
				this.dataManager.set(EntityProjectile.Gravity, true);
			}
			if (this.inTile != null) {
				this.inTile.onEntityCollidedWithBlock(this.world, this.tilePos, state, this);
			}
		} else if (this.isBlock()) {
			this.world.playEvent((EntityPlayer) null, 2001, this.getPosition(), Item.getIdFromItem(this.getItem()));
		} else {
			int[] intArr2 = { Item.getIdFromItem(this.getItem()) };
			if (this.getItem().getHasSubtypes()) {
				intArr2 = new int[] { Item.getIdFromItem(this.getItem()), this.getItemDisplay().getMetadata() };
			}
			for (int j = 0; j < 8; ++j) {
				this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ,
						this.rand.nextGaussian() * 0.15, this.rand.nextGaussian() * 0.2,
						this.rand.nextGaussian() * 0.15, intArr2);
			}
		}
		if (this.explosiveRadius > 0) {
			boolean terraindamage = this.world.getGameRules().getBoolean("mobGriefing") && this.explosiveDamage;
			this.world.newExplosion(((this.getThrower() == null) ? this : this.getThrower()), this.posX, this.posY,
					this.posZ, this.explosiveRadius, this.effect == 1, terraindamage);
			if (this.effect != 0) {
				AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().grow((this.explosiveRadius * 2),
						(this.explosiveRadius * 2), (this.explosiveRadius * 2));
				List<EntityLivingBase> list1 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
				for (EntityLivingBase entity : list1) {
					if (this.effect != 1) {
						Potion p2 = PotionEffectType.getMCType(this.effect);
						if (p2 == null) {
							continue;
						}
						entity.addPotionEffect(new PotionEffect(p2, this.duration * 20, this.amplify));
					} else {
						entity.setFire(this.duration);
					}
				}
				this.world.playEvent((EntityPlayer) null, 2002, this.getPosition(), this.getPotionColor(this.effect));
			}
			this.setDead();
		}
		if (!this.world.isRemote && !this.isArrow() && !this.sticksToWalls()) {
			this.setDead();
		}
	}

	public void onUpdate() {
		super.onEntityUpdate();
		if (++this.ticksExisted % 10 == 0) {
			EventHooks.onProjectileTick(this);
		}
		if (this.effect == 1 && !this.inGround) {
			this.setFire(1);
		}
		IBlockState state = this.world.getBlockState(this.tilePos);
		Block block = state.getBlock();
		if ((this.isArrow() || this.sticksToWalls()) && this.tilePos != BlockPos.ORIGIN) {
			AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox((IBlockAccess) this.world, this.tilePos);
			if (axisalignedbb != null && axisalignedbb.contains(new Vec3d(this.posX, this.posY, this.posZ))) {
				this.inGround = true;
			}
		}
		if (this.arrowShake > 0) {
			--this.arrowShake;
		}
		if (this.inGround) {
			int j = block.getMetaFromState(state);
			if (block == this.inTile && j == this.inData) {
				++this.ticksInGround;
				if (this.ticksInGround == 1200) {
					this.setDead();
				}
			} else {
				this.inGround = false;
				this.motionX *= this.rand.nextFloat() * 0.2f;
				this.motionY *= this.rand.nextFloat() * 0.2f;
				this.motionZ *= this.rand.nextFloat() * 0.2f;
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		} else {
			++this.ticksInAir;
			if (this.ticksInAir == 1200) {
				this.setDead();
			}
			Vec3d vec3 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec4 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3, vec4, false, true, false);
			vec3 = new Vec3d(this.posX, this.posY, this.posZ);
			vec4 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			if (movingobjectposition != null) {
				vec4 = new Vec3d(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y,
						movingobjectposition.hitVec.z);
			}
			if (!this.world.isRemote) {
				Entity entity = null;
				List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this,
						this.getEntityBoundingBox().grow(this.motionX, this.motionY, this.motionZ).grow(1.0, 1.0, 1.0));
				double d0 = 0.0;
				for (int k = 0; k < list.size(); ++k) {
					Entity entity2 = list.get(k);
					if (entity2.canBeCollidedWith()
							&& (!entity2.isEntityEqual(this.thrower) || this.ticksInAir >= 25)) {
						float f = 0.3f;
						AxisAlignedBB axisalignedbb2 = entity2.getEntityBoundingBox().grow(f, f, f);
						RayTraceResult movingobjectposition2 = axisalignedbb2.calculateIntercept(vec3, vec4);
						if (movingobjectposition2 != null) {
							double d2 = vec3.distanceTo(movingobjectposition2.hitVec);
							if (d2 < d0 || d0 == 0.0) {
								entity = entity2;
								d0 = d2;
							}
						}
					}
				}
				if (entity != null) {
					movingobjectposition = new RayTraceResult(entity);
				}
				if (movingobjectposition != null && movingobjectposition.entityHit != null) {
					if (this.npc != null && movingobjectposition.entityHit instanceof EntityLivingBase
							&& this.npc.isOnSameTeam(movingobjectposition.entityHit)) {
						movingobjectposition = null;
					} else if (movingobjectposition.entityHit instanceof EntityPlayer) {
						EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;
						if (entityplayer.capabilities.disableDamage || (this.thrower instanceof EntityPlayer
								&& !((EntityPlayer) this.thrower).canAttackPlayer(entityplayer))) {
							movingobjectposition = null;
						}
					}
				}
			}
			if (movingobjectposition != null) {
				if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK
						&& this.world.getBlockState(movingobjectposition.getBlockPos()).getBlock() == Blocks.PORTAL) {
					this.setPortal(movingobjectposition.getBlockPos());
				} else {
					this.dataManager.set(EntityProjectile.Rotating, false);
					this.onImpact(movingobjectposition);
				}
			}
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) Math.atan2(this.motionX, this.motionZ) * 180.0f / 3.141592653589793f;
			this.rotationPitch = (float) Math.atan2(this.motionY, f2) * 180.0f / 3.141592653589793f;
			while (this.rotationPitch - this.prevRotationPitch < -180.0f) {
				this.prevRotationPitch -= 360.0f;
			}
			while (this.rotationPitch - this.prevRotationPitch >= 180.0f) {
				this.prevRotationPitch += 360.0f;
			}
			while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
				this.prevRotationYaw -= 360.0f;
			}
			while (this.rotationYaw - this.prevRotationYaw >= 180.0f) {
				this.prevRotationYaw += 360.0f;
			}
			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch);
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw);
			if (this.isRotating()) {
				int spin = this.isBlock() ? 10 : 20;
				this.rotationPitch -= this.ticksInAir % 15 * spin * this.getSpeed();
			}
			float f3 = this.getMotionFactor();
			float f4 = this.getGravityVelocity();
			if (this.isInWater()) {
				if (this.world.isRemote) {
					for (int i = 0; i < 4; ++i) {
						float f5 = 0.25f;
						this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f5,
								this.posY - this.motionY * f5, this.posZ - this.motionZ * f5, this.motionX,
								this.motionY, this.motionZ, new int[0]);
					}
				}
				f3 = 0.8f;
			}
			this.motionX *= f3;
			this.motionY *= f3;
			this.motionZ *= f3;
			if (this.hasGravity()) {
				this.motionY -= f4;
			}
			if (this.accelerate) {
				this.motionX += this.accelerationX;
				this.motionY += this.accelerationY;
				this.motionZ += this.accelerationZ;
			}
			if (this.world.isRemote && this.dataManager.get(EntityProjectile.Particle) > 0) {
				this.world.spawnParticle(ParticleType.getMCType(this.dataManager.get(EntityProjectile.Particle)),
						this.posX, this.posY, this.posZ, 0.0, 0.0, 0.0, new int[0]);
			}
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		this.tilePos = new BlockPos(compound.getShort("xTile"), compound.getShort("yTile"), compound.getShort("zTile"));
		this.inTile = Block.getBlockById(compound.getByte("inTile") & 0xFF);
		this.inData = (compound.getByte("inData") & 0xFF);
		this.throwableShake = (compound.getByte("shake") & 0xFF);
		this.inGround = (compound.getByte("inGround") == 1);
		this.dataManager.set(EntityProjectile.Arrow, compound.getBoolean("isArrow"));
		this.throwerName = compound.getString("ownerName");
		this.canBePickedUp = compound.getBoolean("canBePickedUp");
		this.damage = compound.getFloat("damagev2");
		this.punch = compound.getInteger("punch");
		this.explosiveRadius = compound.getInteger("explosiveRadius");
		this.duration = compound.getInteger("effectDuration");
		this.accelerate = compound.getBoolean("accelerate");
		this.effect = compound.getInteger("PotionEffect");
		this.accuracy = compound.getInteger("accuracy");
		this.dataManager.set(EntityProjectile.Particle, compound.getInteger("trailenum"));
		this.dataManager.set(EntityProjectile.Size, compound.getInteger("size"));
		this.dataManager.set(EntityProjectile.Glows, compound.getBoolean("glows"));
		this.dataManager.set(EntityProjectile.Velocity, compound.getInteger("velocity"));
		this.dataManager.set(EntityProjectile.Gravity, compound.getBoolean("gravity"));
		this.dataManager.set(EntityProjectile.Is3d, compound.getBoolean("Render3D"));
		this.dataManager.set(EntityProjectile.Rotating, compound.getBoolean("Spins"));
		this.dataManager.set(EntityProjectile.Sticks, compound.getBoolean("Sticks"));
		if (this.throwerName != null && this.throwerName.length() == 0) {
			this.throwerName = null;
		}
		if (compound.hasKey("direction")) {
			NBTTagList nbttaglist = compound.getTagList("direction", 6);
			this.motionX = nbttaglist.getDoubleAt(0);
			this.motionY = nbttaglist.getDoubleAt(1);
			this.motionZ = nbttaglist.getDoubleAt(2);
		}
		NBTTagCompound var2 = compound.getCompoundTag("Item");
		ItemStack item = new ItemStack(var2);
		if (item.isEmpty()) {
			this.setDead();
		} else {
			this.dataManager.set(EntityProjectile.ItemStackThrown, item);
		}
	}

	public void setHasGravity(boolean bo) {
		this.dataManager.set(EntityProjectile.Gravity, bo);
	}

	public void setIs3D(boolean bo) {
		this.dataManager.set(EntityProjectile.Is3d, bo);
	}

	public void setParticleEffect(int type) {
		this.dataManager.set(EntityProjectile.Particle, type);
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double par1, double par3, double par5, float par7, float par8, int par9,
			boolean bo) {
		if (this.world.isRemote && this.inGround) {
			return;
		}
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	public void setRotating(boolean bo) {
		this.dataManager.set(EntityProjectile.Rotating, bo);
	}

	public void setSpeed(int speed) {
		this.dataManager.set(EntityProjectile.Velocity, speed);
	}

	public void setStickInWall(boolean bo) {
		this.dataManager.set(EntityProjectile.Sticks, bo);
	}

	public void setThrownItem(ItemStack item) {
		this.dataManager.set(EntityProjectile.ItemStackThrown, item);
	}

	public void shoot(double par1, double par3, double par5, float par7, float par8) {
		float f2 = MathHelper.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
		float f3 = MathHelper.sqrt(par1 * par1 + par5 * par5);
		float yaw = (float) Math.atan2(par1, par5) * 180.0f / 3.141592653589793f;
		float pitch = this.hasGravity() ? par7 : (float) Math.atan2(par3, f3) * 180.0f / 3.141592653589793f;
		float n = yaw;
		this.rotationYaw = n;
		this.prevRotationYaw = n;
		float n2 = pitch;
		this.rotationPitch = n2;
		this.prevRotationPitch = n2;
		this.motionX = MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f);
		this.motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f);
		this.motionY = MathHelper.sin((pitch + 1.0f) / 180.0f * 3.1415927f);
		this.motionX += this.rand.nextGaussian() * 0.007499999832361937 * par8;
		this.motionZ += this.rand.nextGaussian() * 0.007499999832361937 * par8;
		this.motionY += this.rand.nextGaussian() * 0.007499999832361937 * par8;
		this.motionX *= this.getSpeed();
		this.motionZ *= this.getSpeed();
		this.motionY *= this.getSpeed();
		this.accelerationX = par1 / f2 * 0.1;
		this.accelerationY = par3 / f2 * 0.1;
		this.accelerationZ = par5 / f2 * 0.1;
		this.ticksInGround = 0;
	}

	public void shoot(float speed) {
		double varX = -MathHelper.sin(this.rotationYaw / 180.0f * 3.1415927f)
				* MathHelper.cos(this.rotationPitch / 180.0f * 3.1415927f);
		double varZ = MathHelper.cos(this.rotationYaw / 180.0f * 3.1415927f)
				* MathHelper.cos(this.rotationPitch / 180.0f * 3.1415927f);
		double varY = -MathHelper.sin(this.rotationPitch / 180.0f * 3.1415927f);
		this.shoot(varX, varY, varZ, -this.rotationPitch, speed);
	}

	public boolean sticksToWalls() {
		return this.is3D() && (boolean) this.dataManager.get(EntityProjectile.Sticks);
	}

	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		par1NBTTagCompound.setShort("xTile", (short) this.tilePos.getX());
		par1NBTTagCompound.setShort("yTile", (short) this.tilePos.getY());
		par1NBTTagCompound.setShort("zTile", (short) this.tilePos.getZ());
		par1NBTTagCompound.setByte("inTile", (byte) Block.getIdFromBlock(this.inTile));
		par1NBTTagCompound.setByte("inData", (byte) this.inData);
		par1NBTTagCompound.setByte("shake", (byte) this.throwableShake);
		par1NBTTagCompound.setBoolean("inGround", this.inGround);
		par1NBTTagCompound.setBoolean("isArrow", this.isArrow());
		par1NBTTagCompound.setTag("direction",
				this.newDoubleNBTList(new double[] { this.motionX, this.motionY, this.motionZ }));
		par1NBTTagCompound.setBoolean("canBePickedUp", this.canBePickedUp);
		if ((this.throwerName == null || this.throwerName.length() == 0) && this.thrower != null
				&& this.thrower instanceof EntityPlayer) {
			this.throwerName = this.thrower.getUniqueID().toString();
		}
		par1NBTTagCompound.setString("ownerName", (this.throwerName == null) ? "" : this.throwerName);
		par1NBTTagCompound.setTag("Item", this.getItemDisplay().writeToNBT(new NBTTagCompound()));
		par1NBTTagCompound.setFloat("damagev2", this.damage);
		par1NBTTagCompound.setInteger("punch", this.punch);
		par1NBTTagCompound.setInteger("size", this.dataManager.get(EntityProjectile.Size));
		par1NBTTagCompound.setInteger("velocity", this.dataManager.get(EntityProjectile.Velocity));
		par1NBTTagCompound.setInteger("explosiveRadius", this.explosiveRadius);
		par1NBTTagCompound.setInteger("effectDuration", this.duration);
		par1NBTTagCompound.setBoolean("gravity", this.hasGravity());
		par1NBTTagCompound.setBoolean("accelerate", this.accelerate);
		par1NBTTagCompound.setBoolean("glows", (boolean) this.dataManager.get(EntityProjectile.Glows));
		par1NBTTagCompound.setInteger("PotionEffect", this.effect);
		par1NBTTagCompound.setInteger("trailenum", this.dataManager.get(EntityProjectile.Particle));
		par1NBTTagCompound.setBoolean("Render3D", (boolean) this.dataManager.get(EntityProjectile.Is3d));
		par1NBTTagCompound.setBoolean("Spins", (boolean) this.dataManager.get(EntityProjectile.Rotating));
		par1NBTTagCompound.setBoolean("Sticks", (boolean) this.dataManager.get(EntityProjectile.Sticks));
		par1NBTTagCompound.setInteger("accuracy", this.accuracy);
	}
}
