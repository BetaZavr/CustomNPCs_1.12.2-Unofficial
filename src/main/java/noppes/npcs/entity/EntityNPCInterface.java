package noppes.npcs.entity;

import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.EventHooks;
import noppes.npcs.IChatMessages;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.Server;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.ai.CombatHandler;
import noppes.npcs.ai.EntityAIAnimation;
import noppes.npcs.ai.EntityAIBustDoor;
import noppes.npcs.ai.EntityAIFindShade;
import noppes.npcs.ai.EntityAIJob;
import noppes.npcs.ai.EntityAILook;
import noppes.npcs.ai.EntityAIMoveIndoors;
import noppes.npcs.ai.EntityAIMovingPath;
import noppes.npcs.ai.EntityAIRole;
import noppes.npcs.ai.EntityAITransform;
import noppes.npcs.ai.EntityAIWander;
import noppes.npcs.ai.EntityAIWorldLines;
import noppes.npcs.ai.FlyingMoveHelper;
import noppes.npcs.ai.attack.EntityAIAvoidTarget;
import noppes.npcs.ai.attack.EntityAICommanderTarget;
import noppes.npcs.ai.attack.EntityAICustom;
import noppes.npcs.ai.attack.EntityAIDodge;
import noppes.npcs.ai.attack.EntityAIHitAndRun;
import noppes.npcs.ai.attack.EntityAINoTactic;
import noppes.npcs.ai.attack.EntityAIOnslaught;
import noppes.npcs.ai.attack.EntityAIPounceTarget;
import noppes.npcs.ai.attack.EntityAIStalkTarget;
import noppes.npcs.ai.attack.EntityAISurround;
import noppes.npcs.ai.movement.EntityAIFollow;
import noppes.npcs.ai.movement.EntityAIReturn;
import noppes.npcs.ai.movement.EntityAISprintToTarget;
import noppes.npcs.ai.movement.EntityAIWaterNav;
import noppes.npcs.ai.selector.NPCAttackSelector;
import noppes.npcs.ai.target.EntityAIClearTarget;
import noppes.npcs.ai.target.EntityAIClosestTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtByTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtTarget;
import noppes.npcs.ai.target.EntityAIWatchClosest;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.*;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.api.mixin.entity.IEntityLivingBaseMixin;
import noppes.npcs.api.mixin.world.IWorldMixin;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumAnimationStages;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.data.DataTransform;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataAbilities;
import noppes.npcs.entity.data.DataAdvanced;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataScript;
import noppes.npcs.entity.data.DataStats;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.items.ItemSoulstoneFilled;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.GameProfileAlt;

public abstract class EntityNPCInterface
extends EntityCreature
implements IEntityAdditionalSpawnData, ICommandSender, IRangedAttackMob, IAnimals {

	public static FakePlayer ChatEventPlayer;
	public static FakePlayer CommandPlayer;
	public static FakePlayer GenericPlayer;
	public static GameProfileAlt ChatEventProfile = new GameProfileAlt();
	public static GameProfileAlt CommandProfile = new GameProfileAlt();
	public static GameProfileAlt GenericProfile = new GameProfileAlt();
	protected static DataParameter<Integer> Animation = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.VARINT);
	public static final DataParameter<Boolean> Attacking = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> FactionData = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> Interacting = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IsDead = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.BOOLEAN);
	private static final DataParameter<String> JobData = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.STRING);
	private static final DataParameter<String> RoleData = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.STRING);
	private static final DataParameter<Boolean> Walking = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Float> AimRotationYaw = EntityDataManager.createKey(EntityNPCInterface.class, DataSerializers.FLOAT); // fix bug while aiming
	public DataAbilities abilities;
	public DataDisplay display;
	public DataStats stats;
	public DataAI ais;
	public DataInventory inventory;
	public DataAdvanced advanced;
	public DataScript script;
	public DataAnimation animation;
	public int animationStart = 0;
	public int currentAnimation = 0;
	public float baseHeight = 1.8f;
	public BossInfoServer bossInfo;
	public CombatHandler combatHandler;
	public int[] dialogs = new int[0];
	public Faction faction;
	public double field_20061_w, field_20062_v, field_20063_u, field_20064_t, field_20065_s, field_20066_r;
	public boolean hasDied = false;
	public List<EntityLivingBase> interactingEntities = new ArrayList<>();
	public EntityAICustom aiAttackTarget;
	public EntityAIAnimation animateAi;
	public EntityAILook lookAi;
	public Entity lookAt = null;
	public float[] lookPos = new float[] { -1.0f, -1.0f };
	public boolean updateLook = false;
	public EntityNPCInterface aiOwnerNPC;
	public boolean aiIsSneak;
	public long killedTime = 0L;
	public int lastInteract = 0;
	public int homeDimensionId;
	public LinkedNpcController.LinkedData linkedData;
	public long linkedLast = 0L;
	public String linkedName = "";
	public IChatMessages messages;
	public int npcVersion = VersionCompatibility.ModRev;
	public float scaleX;
	public float scaleY;
	public float scaleZ;
	private double startYPos = -1.0;
	private int taskCount = 1;
	public ResourceLocation textureCloakLocation = null;
	public ResourceLocation textureGlowLocation = null;
	public ResourceLocation textureLocation = null;
	public DataTimers timers;
	public long totalTicksAlive = 0L;
	public DataTransform transform;
	public boolean updateClient = false;
	private boolean wasKilled = false;
	public ICustomNpc<?> wrappedNPC;
	public boolean updateAI = true;
	public Path navigating;
	private long initTime;
	private boolean isOldSneaking;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityNPCInterface(World world) {
		super(world);
		this.homeDimensionId = world.provider.getDimension();
		this.combatHandler = new CombatHandler(this);
		this.bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
		this.wrappedNPC = new NPCWrapper(this);
		if (!CustomNpcs.DefaultInteractLine.isEmpty()) {
			this.advanced.interactLines.lines.put(0, new Line(CustomNpcs.DefaultInteractLine));
		}
		this.experienceValue = 0;
		float f = 0.9375f;
		this.scaleZ = f;
		this.scaleY = f;
		this.scaleX = f;
		this.faction = this.getFaction();
		this.setFaction(this.faction.id);
		this.setSize(1.0f, 1.0f);
		this.bossInfo.setVisible(false);
		this.stepHeight = this.ais.stepheight;
		this.initTime = System.currentTimeMillis();
		if (!isServerWorld()) { CustomNpcs.proxy.checkTexture(this); }
		animation.tryRunAnimation(AnimationKind.INIT);
		this.maxHurtResistantTime = this.ais.getMaxHurtResistantTime();
	}

	public void addInteract(EntityLivingBase entity) {
		if (!this.ais.stopAndInteract || this.isAttacking() || !entity.isEntityAlive() || this.isAIDisabled()) {
			return;
		}
		if (this.ticksExisted - this.lastInteract < 180) {
			this.interactingEntities.clear();
		}
		this.getNavigator().clearPath();
		this.lastInteract = this.ticksExisted;
		if (!this.interactingEntities.contains(entity)) {
			this.interactingEntities.add(entity);
		}
	}

	public void addRegularEntries() {
		this.tasks.addTask(this.taskCount++, new EntityAIReturn(this));
		this.tasks.addTask(this.taskCount++, new EntityAIFollow(this));
		if (this.ais.getStandingType() != 1 && this.ais.getStandingType() != 3) {
			this.tasks.addTask(this.taskCount++, new EntityAIWatchClosest(this, EntityLivingBase.class, 5.0f));
		}
		this.tasks.addTask(this.taskCount++, (this.lookAi = new EntityAILook(this)));
		this.tasks.addTask(this.taskCount++, new EntityAIWorldLines(this));
		if (!this.ais.aiDisabled) {
			this.tasks.addTask(this.taskCount++, new EntityAIJob(this));
			this.tasks.addTask(this.taskCount++, new EntityAIRole(this));
		}
		this.tasks.addTask(this.taskCount++, (this.animateAi = new EntityAIAnimation(this)));
		if (this.transform.isValid()) {
			this.tasks.addTask(this.taskCount++, new EntityAITransform(this));
		}
	}

	public void addTrackingPlayer(@Nonnull EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	public void addVelocity(double d, double d1, double d2) {
		if (this.isWalking() && !this.isKilled()) {
			super.addVelocity(d, d1, d2);
		}
	}

	protected float applyArmorCalculations(@Nonnull DamageSource source, float damage) {
		if (this.advanced.roleInterface instanceof RoleCompanion) {
			damage = ((RoleCompanion) this.advanced.roleInterface).applyArmorCalculations(source, damage);
		}
		return damage;
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.abilities = new DataAbilities(this);
		this.display = new DataDisplay(this);
		this.stats = new DataStats(this);
		this.ais = new DataAI(this);
		this.advanced = new DataAdvanced(this);
		this.inventory = new DataInventory(this);
		this.transform = new DataTransform(this);
		this.script = new DataScript(this);
		this.timers = new DataTimers(this);
		if (animation == null) { animation = new DataAnimation(this); }
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.stats.maxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CustomNpcs.NpcNavRange);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getSpeed());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.stats.melee.getStrength());
		this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue((this.getSpeed() * 2.0f));
	}

	public boolean attackEntityAsMob(@Nonnull Entity entity) { // this NPCs attempt to damage the target <- EntityAICustom
		AnimationConfig anim = animation.tryRunAnimation(AnimationKind.ATTACKING);
		if (anim != null) {
			boolean found = false;
			for (AnimationFrameConfig frame : anim.frames.values()) {
				if (frame.isNowDamage() && frame.damageDelay != 0) {
					final int time = frame.damageDelay * 50;
					CustomNPCsScheduler.runTack(() -> tryAttackEntityAsMob(entity, frame.id), time);
					found = true;
				}
			}
			if (!found) {
				final int time = (anim.totalTicks - 1) * 50;
				CustomNPCsScheduler.runTack(() -> tryAttackEntityAsMob(entity, anim.frames.size() - 1), time);
			}
			return false;
		}
		return this.tryAttackEntityAsMob(entity, 0);
	}

	private boolean tryAttackEntityAsMob(Entity target, int frameID) {
		if (this.ais.aiDisabled || target == null || !target.isEntityAlive()) { return false; }
		Set<Entity> entityList = new HashSet<>();
		entityList.add(target);
		if (CustomNpcs.ShowCustomAnimation && isServerWorld() && animation.isAnimated(AnimationKind.ATTACKING)) {
			List<AxisAlignedBB> aabbs = animation.getAnimation().getDamageHitboxes(this, frameID);
			if (aabbs.isEmpty()) { // only target
				double range = stats.melee.getRange();
				double minRange = (width + target.width) * 0.425d; // (/ 2.0 * 0.85)
				double yaw = Math.abs(Util.instance.getVector3D(posX, posY, posZ, target.posX, target.posY, target.posZ).getYaw());
				if (getDistance(target) - minRange > range || yaw > 60.0d) { return false; }
			}
			else { // custom targets
				for (AxisAlignedBB aabb : aabbs) {
                    entityList.addAll(world.getEntitiesWithinAABB(Entity.class, aabb));
				}
				entityList.remove(this);
			}
		}
		float amount = this.stats.melee.getStrength();
		DamageSource damageSource = new NpcDamageSource("mob", this);
		boolean attackEntity = false;
		for (Entity entity : entityList) {
			if (this.stats.melee.getDelay() < 10) {
				entity.hurtResistantTime = 0;
			}
			if (entity instanceof EntityLivingBase) {
				NpcEvent.MeleeAttackEvent event = new NpcEvent.MeleeAttackEvent(this.wrappedNPC, (EntityLivingBase) entity, amount);
				if (EventHooks.onNPCAttacksMelee(this, event)) { return false; }
				amount = event.damage;
			}
			boolean var4 = entity.attackEntityFrom(damageSource, amount);
			if (var4) {
				if (this.getOwner() instanceof EntityPlayer && entity instanceof EntityLivingBase) {
					EntityUtil.setRecentlyHit((EntityLivingBase) entity);
				}
				if (this.advanced.roleInterface instanceof RoleCompanion) {
					((RoleCompanion) this.advanced.roleInterface).attackedEntity();
				}
			}
			if (this.stats.melee.getEffectType() != 0) {
				if (this.stats.melee.getEffectType() != 1) {
					Potion eff = PotionEffectType.getMCType(this.stats.melee.getEffectType());
					if (eff != null && entity instanceof EntityLivingBase) {
						((EntityLivingBase) entity) .addPotionEffect(new PotionEffect(eff, this.stats.melee.getEffectTime() * 20, this.stats.melee.getEffectStrength()));
					}
				} else {
					entity.setFire(this.stats.melee.getEffectTime());
				}
			}
			if (var4) { attackEntity = true; }
		}
		return attackEntity;
	}

	private boolean canBlockDamageSource(DamageSource damageSourceIn) {
		boolean isBlocked = false;
		int type = damageSourceIn.isUnblockable() || damageSourceIn.getDamageLocation() == null ? -1 : 0;
		if (damageSourceIn.getDamageLocation() != null) {
			type = 0;
			Vec3d vec3d = damageSourceIn.getDamageLocation(); // position from which damage is dealt
			float angle = (float) Util.instance.getAngles3D(this.posX, 0.0d, this.posZ, vec3d.x, 0.0d, vec3d.z).getYaw() - this.rotationYaw;
			Vec3d vec3d1 = this.getLook(1.0F); // which way is looking this NPC
			Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
			vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
			if (vec3d2.dotProduct(vec3d1) < 0.0D) {
				if (angle < 180.0f) { type = 1; } else { type = 2; }
			}
		}
		if (type != -1 && !damageSourceIn.isUnblockable()) {
			float chance = this.stats.getChanceBlockDamage() / 100.0f;
			if (chance > 0.0f && type > 0) { // in front of
				ItemStack stack;
				if (this.inventory.getProjectile() != null) { chance /= 3.0f; }
				else if (type == 1) { // to the right
					stack = this.inventory.getRightHand() != null ? this.inventory.getRightHand().getMCItemStack() : ItemStack.EMPTY;
					if (stack.getItem() instanceof ItemSword) {
						chance *= 1.3333f;
						if (chance < 0.25f) { chance = 0.25f; }
					}
					else if (stack.getItem() instanceof ItemShield) {
						chance *= 2.0f;
						if (chance < 0.75f) { chance = 0.75f; }
					}
				} else { // to the left
					stack = this.inventory.getLeftHand() != null ? this.inventory.getLeftHand().getMCItemStack() : ItemStack.EMPTY;
					if (stack.getItem() instanceof ItemSword) {
						chance *= 1.1667f;
						if (chance < 0.1f) { chance = 0.1f; }
					}
					else if (stack.getItem() instanceof ItemShield) {
						chance *= 1.75f;
						if (chance < 0.5f) { chance = 0.5f; }
					}
				}
				float f = this.rand.nextFloat();
				isBlocked = chance >= f;
			}
		}
		NpcEvent.NeedBlockDamage event = new NpcEvent.NeedBlockDamage(this.wrappedNPC, damageSourceIn, isBlocked, type);
		EventHooks.onNPCNeedBlockDamage(this, event);
		isBlocked = event.isBlocked && !event.isCanceled();
		return isBlocked;
	}

	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource damagesource, float damage) {
		//if (this.animation.isAnimated() && this.animation.getAnimationType() == AnimationKind.INIT) { return false; }
		if (!this.isServerWorld() || CustomNpcs.FreezeNPCs || damagesource.damageType.equals("inWall")) {
			return false;
		}
		if (this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER && this.advanced.roleInterface.isFollowing() && damagesource == DamageSource.FALL) {
			return false;
		}
		if (damagesource.damageType.equals("outOfWorld") && this.isKilled()) { this.reset(); }
		damage = this.stats.resistances.applyResistance(damagesource, damage);
		if (!this.combatHandler.canDamage(damagesource, damage)) { return false; }
		Entity entity = NoppesUtilServer.GetDamageSource(damagesource);
		EntityLivingBase attackingEntity = null;
		if (entity instanceof EntityLivingBase) { attackingEntity = (EntityLivingBase) entity; }
		if (attackingEntity != null && attackingEntity == this.getOwner()) { return false; }
		if (attackingEntity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) attackingEntity;
			if (npc.faction.id == this.faction.id) { return false; }
			if (npc.getOwner() instanceof EntityPlayer) { this.recentlyHit = 100; }
		} else if (attackingEntity instanceof EntityPlayer && this.faction.isFriendlyToPlayer((EntityPlayer) attackingEntity)) {
			ForgeHooks.onLivingAttack(this, damagesource, damage);
			return false;
		}
		NpcEvent.DamagedEvent event = new NpcEvent.DamagedEvent(this.wrappedNPC, entity, damage, damagesource);
		if (EventHooks.onNPCDamaged(this, event)) {
			ForgeHooks.onLivingAttack(this, damagesource, damage);
			return false;
		}
		damage = event.damage;
		if (this.isKilled()) {
			return false;
		}
		if (damagesource.damageType.contains("inFire")) {
			this.setFire(8);
		} // -> onFire
		boolean isHurt = false;
		if (attackingEntity == null) {
			isHurt = customAttackEntityFrom(damagesource, damage);
		} else {
			try {
				boolean check = false;
				if (!(attackingEntity instanceof EntityPlayer) || !((EntityPlayer) attackingEntity).capabilities.disableDamage) {
					if (damage > 0.0f) {
						List<EntityNPCInterface> inRange = this.world.getEntitiesWithinAABB(EntityNPCInterface.class, this.getEntityBoundingBox().grow(32.0, 16.0, 32.0));
						for (EntityNPCInterface npc : inRange) {
							if (npc.equals(this)) { continue; }
							npc.advanced.tryDefendFaction(this.faction.id, this, attackingEntity);
						}
					}
					if (this.isAttacking()) {
						if (this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) > this.getDistance(attackingEntity)) { this.setAttackTarget(attackingEntity); }
						isHurt = customAttackEntityFrom(damagesource, damage);
						check = true;
					}
					else if (damage > 0.0f) { this.setAttackTarget(attackingEntity); }
				}
				if (!check) { isHurt = customAttackEntityFrom(damagesource, damage); }
			} finally {
				if (event.clearTarget) {
					this.setAttackTarget(null);
					this.setRevengeTarget(null);
				}
			}
		}
		if (!isKilled()) {
			if (isHurt && damage > 0.0f) {
				animation.tryRunAnimation(AnimationKind.HIT);
			}
			else  {
				AnimationConfig anim = animation.tryRunAnimation(AnimationKind.BLOCKED);
				if (anim == null && !damagesource.isProjectile() && attackingEntity != null) { blockUsingShield(attackingEntity); }
			}
		}

		return isHurt;
	}

	private boolean customAttackEntityFrom(DamageSource source, float amount) {
		if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, amount)) { return false; }
		if (!this.isServerWorld() || this.isEntityInvulnerable(source)) { return false; }
		this.idleTime = 0;
		if (this.getHealth() <= 0.0F || (source.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE))) { return false; }
		float f = amount;
		if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
			this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
			amount *= 0.75F;
		}
		boolean isBlockedDamage = false;
		if (amount > 0.0F && canBlockDamageSource(source)) {
			this.damageShield(amount);
			amount = 0.0F;
			isBlockedDamage = true;
		}
		this.limbSwingAmount = 1.5F;
		boolean damageCanBeDone = true;
		if (this.ais.getMaxHurtResistantTime() != 0 && (float) this.hurtResistantTime > (float) this.ais.getMaxHurtResistantTime() / 2.0F) {
			if (amount <= this.lastDamage) { return false; }
			this.damageEntity(source, amount - this.lastDamage);
			this.lastDamage = amount;
			damageCanBeDone = false;
		} else {
			this.lastDamage = amount;
			this.hurtResistantTime = this.ais.getMaxHurtResistantTime();
			this.damageEntity(source, amount);
			this.maxHurtTime = 10;
			this.hurtTime = this.maxHurtTime;
		}
		this.attackedAtYaw = 0.0F;
		Entity entity1 = source.getTrueSource();
		if (entity1 != null) {
			if (entity1 instanceof EntityLivingBase) { this.setRevengeTarget((EntityLivingBase) entity1); }
			if (entity1 instanceof EntityPlayer) {
				this.recentlyHit = 100;
				this.attackingPlayer = (EntityPlayer) entity1;
			}
			else if (entity1 instanceof net.minecraft.entity.passive.EntityTameable) {
				net.minecraft.entity.passive.EntityTameable entity_wolf = (net.minecraft.entity.passive.EntityTameable)entity1;
				if (entity_wolf.isTamed()) {
					this.recentlyHit = 100;
					this.attackingPlayer = null;
				}
			}
		}
		if (damageCanBeDone) {
			if (isBlockedDamage) { this.world.setEntityState(this, (byte)29); }
			else if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) { this.world.setEntityState(this, (byte)33); }
			else {
				byte b0;
				if (source == DamageSource.DROWN) { b0 = 36; }
				else if (source.isFireDamage()) { b0 = 37; }
				else{ b0 = 2; }
				this.world.setEntityState(this, b0);
			}
			if (source != DamageSource.DROWN && !isBlockedDamage) { this.markVelocityChanged(); }

			if (entity1 != null) {
				double d1 = entity1.posX - this.posX;
				double d0;
				for (d0 = entity1.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) { d1 = (Math.random() - Math.random()) * 0.01D; }
				this.attackedAtYaw = (float)(MathHelper.atan2(d0, d1) * (180D / Math.PI) - (double)this.rotationYaw);
				this.knockBack(entity1, 0.4F, d1, d0);
			} else { this.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180); }
		}

		if (this.getHealth() <= 0.0F) { this.onDeath(source); }
		else if (damageCanBeDone) { this.playHurtSound(source, isBlockedDamage); }
		boolean isDamaged = !isBlockedDamage;
		if (isDamaged) {
			((IEntityLivingBaseMixin) this).npcs$setLastDamageSource(source);
			((IEntityLivingBaseMixin) this).npcs$setLastDamageStamp(this.world.getTotalWorldTime());
		}
		if (entity1 instanceof EntityPlayerMP) {
			CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((EntityPlayerMP) entity1, this, source, f, amount, isBlockedDamage);
		}
		return isDamaged;
	}

	public void attackEntityWithRangedAttack(@Nonnull EntityLivingBase entity, float distanceFactor) {
		if (this.ais.aiDisabled) { return; }
		ItemStack proj = ItemStackWrapper.MCItem(this.inventory.getProjectile());
		if (proj == null) {
			this.updateAI = true;
			return;
		}
		NpcEvent.RangedLaunchedEvent event = new NpcEvent.RangedLaunchedEvent(this.wrappedNPC, entity, this.stats.ranged.getStrength());
		for (int i = 0; i < this.stats.ranged.getShotCount(); ++i) {
			EntityProjectile projectile = this.shoot(entity, this.stats.ranged.getAccuracy(), proj, distanceFactor == 1.0f);
			projectile.damage = event.damage;
			ItemStack stack = entity.getHeldItemMainhand();
			projectile.callback = ((projectile_0, pos, entity1) -> {
				if (stack.getItem() == CustomRegisters.soulstoneFull) {
					Entity e = ItemSoulstoneFilled.Spawn(null, stack, this.world, pos);
					if (e instanceof EntityLiving && entity1 instanceof EntityLiving) {
						((EntityLiving) e).setRevengeTarget((EntityLiving) entity1);
					} else if (e instanceof EntityLivingBase && entity1 instanceof EntityLivingBase) {
						((EntityLivingBase) e).setRevengeTarget((EntityLivingBase) entity1);
					}
				}
				SoundEvent se = this.stats.ranged.getSoundEvent((entity1 != null) ? 1 : 2);
				String sound = this.stats.ranged.getSound((entity1 != null) ? 1 : 2);
				float pitch = 1.2f / (this.getRNG().nextFloat() * 0.2f + 0.9f);
				if (se != null) {
					projectile_0.playSound(se, 1.0f, pitch);
				} else if (!sound.isEmpty()) {
					BlockPos pos1 = new BlockPos(this.posX, this.posY, this.posZ);
					Server.sendRangedData(this.world, pos1, 64, EnumPacketClient.FORCE_PLAY_SOUND, SoundCategory.NEUTRAL.ordinal(), sound, (float) pos1.getX(), (float) pos1.getY(), (float) pos1.getZ(), 1.0f, pitch);
				}
				return false;
			});
			SoundEvent se = this.stats.ranged.getSoundEvent(0);
			String sound = this.stats.ranged.getSound(0);
			if (se != null) {
				this.playSound(se, 2.0f, 1.0f);
			} else if (!sound.isEmpty()) {
				BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
				Server.sendRangedData(this.world, pos, 64, EnumPacketClient.FORCE_PLAY_SOUND, SoundCategory.NEUTRAL.ordinal(), sound, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ(), 2.0f, 1.0f);
			}
			event.projectiles.add((IProjectile<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(projectile));
		}
		EventHooks.onNPCRangedLaunched(this, event);
	}

	private double calculateStartYPos(BlockPos pos) {
		BlockPos startPos = this.ais.startPos();
		while (pos.getY() > 0) {
			IBlockState state = this.world.getBlockState(pos);
			AxisAlignedBB bb = state.getBoundingBox(this.world, pos).offset(pos);
            if (this.ais.movementType != 2 || startPos.getY() > pos.getY() || state.getMaterial() != Material.WATER) {
                return bb.maxY;
            }
            pos = pos.down();
        }
		return 0.0;
	}

	private BlockPos calculateTopPos(BlockPos pos) {
		for (BlockPos check = pos; check.getY() > 0; check = check.down()) {
			IBlockState state = this.world.getBlockState(check);
			if (state.getBlock().isAir(state, this.world, check)) { return check; }
        }
		return pos;
	}

	public boolean canAttackClass(@Nonnull Class<? extends EntityLivingBase> clazz) {
		return !this.ais.aiDisabled && EntityBat.class != clazz;
	}

	public boolean canBeCollidedWith() {
		return !this.isKilled() && this.display.getHasHitbox();
	}

	public boolean canBeLeashedTo(@Nonnull EntityPlayer player) {
		return false;
	}

	public boolean canBePushed() {
		return super.canBePushed() && this.display.getHasHitbox();
	}

	public boolean canBreatheUnderwater() {
		return this.ais.movementType == 2;
	}

	protected boolean canDespawn() {
		return this.stats.spawnCycle == 4;
	}

	public boolean canFly() {
		return false;
	}

	public boolean canSee(Entity entity) {
		if (entity instanceof EntityLivingBase) {
			return Util.instance.npcCanSeeTarget(this, (EntityLivingBase) entity, false, true);
		}
		return this.getEntitySenses().canSee(entity);
	}

	private void clearTasks(EntityAITasks tasks) {
		List<EntityAITasks.EntityAITaskEntry> list = new ArrayList<>(tasks.taskEntries);
		for (EntityAITasks.EntityAITaskEntry entityaitaskentry : list) {
			try {
				tasks.removeTask(entityaitaskentry.action);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		tasks.taskEntries.clear();
	}

	public void cloakUpdate() {
		this.field_20066_r = this.field_20063_u;
		this.field_20065_s = this.field_20062_v;
		this.field_20064_t = this.field_20061_w;
		double d = this.posX - this.field_20063_u;
		double d2 = this.posY - this.field_20062_v;
		double d3 = this.posZ - this.field_20061_w;
		double d4 = 10.0;
		if (d > d4) {
			double posX = this.posX;
			this.field_20063_u = posX;
			this.field_20066_r = posX;
		}
		if (d3 > d4) {
			double posZ = this.posZ;
			this.field_20061_w = posZ;
			this.field_20064_t = posZ;
		}
		if (d2 > d4) {
			double posY = this.posY;
			this.field_20062_v = posY;
			this.field_20065_s = posY;
		}
		if (d < -d4) {
			double posX2 = this.posX;
			this.field_20063_u = posX2;
			this.field_20066_r = posX2;
		}
		if (d3 < -d4) {
			double posZ2 = this.posZ;
			this.field_20061_w = posZ2;
			this.field_20064_t = posZ2;
		}
		if (d2 < -d4) {
			double posY2 = this.posY;
			this.field_20062_v = posY2;
			this.field_20065_s = posY2;
		}
		this.field_20063_u += d * 0.25;
		this.field_20061_w += d3 * 0.25;
		this.field_20062_v += d2 * 0.25;
	}

	protected void damageEntity(@Nonnull DamageSource damageSrc, float damageAmount) {
		super.damageEntity(damageSrc, damageAmount);
		this.combatHandler.damage(damageSrc, damageAmount);
	}

	protected int decreaseAirSupply(int par1) {
		if (!this.stats.canDrown) {
			return par1;
		}
		return super.decreaseAirSupply(par1);
	}

	public void delete() {
		this.advanced.roleInterface.delete();
		this.advanced.jobInterface.delete();
		super.setDead();
	}

	public void doorInteractType() {
		if (this.canFly()) {
			return;
		}
		EntityAIBase aiDoor = null;
		if (this.ais.doorInteract == 1) {
			this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIOpenDoor(this, true));
		} else if (this.ais.doorInteract == 0) {
			this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIBustDoor(this));
		}
		if (this.getNavigator() instanceof PathNavigateGround) {
			((PathNavigateGround) this.getNavigator()).setBreakDoors(aiDoor != null);
		}
	}

	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
	}

	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
	}

	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(EntityNPCInterface.RoleData, "");
		this.dataManager.register(EntityNPCInterface.JobData, "");
		this.dataManager.register(EntityNPCInterface.FactionData, 0);
		this.dataManager.register(EntityNPCInterface.Animation, 0);
		this.dataManager.register(EntityNPCInterface.Walking, false);
		this.dataManager.register(EntityNPCInterface.Interacting, false);
		this.dataManager.register(EntityNPCInterface.IsDead, false);
		this.dataManager.register(EntityNPCInterface.Attacking, false);
		this.dataManager.register(EntityNPCInterface.AimRotationYaw, 361.0f);
	}

	public void fall(float distance, float modifier) {
		if (!this.stats.noFallDamage || (this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER
				&& this.advanced.roleInterface.isFollowing())) {
			return;
		}
		super.fall(distance, modifier);
	}

	public int followRange() {
		if (this.advanced.scenes.getOwner() != null) {
			return 4;
		}
		if (this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER
				&& this.advanced.roleInterface.isFollowing()) {
			return 6;
		}
		if (this.advanced.roleInterface.getEnumType() == RoleType.COMPANION
				&& this.advanced.roleInterface.isFollowing()) {
			return 4;
		}
		if (this.advanced.jobInterface.getEnumType() == JobType.FOLLOWER && this.advanced.jobInterface.isFollowing()) {
			return 4;
		}
		return 15;
	}

	public boolean getAlwaysRenderNameTagForRender() {
		return true;
	}

	public @Nonnull Iterable<ItemStack> getArmorInventoryList() {
		ArrayList<ItemStack> list = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			list.add(ItemStackWrapper.MCItem(this.inventory.armor.get(3 - i)));
		}
		return list;
	}

	public float getBlockPathWeight(@Nonnull BlockPos pos) {
		if (this.ais.movementType == 2) {
			return (this.world.getBlockState(pos).getMaterial() == Material.WATER) ? 10.0f : 0.0f;
		}
		float weight = this.world.getLightBrightness(pos) - 0.5f;
		if (this.world.getBlockState(pos).isOpaqueCube()) {
			weight += 10.0f;
		}
		return weight;
	}

	public boolean getCanSpawnHere() {
		return this.getBlockPathWeight(new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ)) >= 0.0f
				&& this.world.getBlockState(new BlockPos(this).down()).canEntitySpawn(this);
	}

	public Entity getCommandSenderEntity() {
		if (!this.isServerWorld()) {
			return this;
		}
		EntityUtil.Copy(this, EntityNPCInterface.CommandPlayer);
		EntityNPCInterface.CommandPlayer.setWorld(this.world);
		EntityNPCInterface.CommandPlayer.setPosition(this.posX, this.posY, this.posZ);
		return EntityNPCInterface.CommandPlayer;
	}

	public @Nonnull EnumCreatureAttribute getCreatureAttribute() {
		return (this.stats == null) ? EnumCreatureAttribute.UNDEFINED : this.stats.creatureType;
	}

	public SoundEvent getDeathSound() {
		return null;
	}

	private Dialog getDialog(EntityPlayer player) {
		Set<Integer> newDS = new HashSet<>();
		Dialog dialog = null;
		for (int dialogId : this.dialogs) {
			if (!DialogController.instance.hasDialog(dialogId)) {
				continue;
			}
			newDS.add(dialogId);
			if (dialog != null) {
				continue;
			}
			Dialog d = (Dialog) DialogController.instance.get(dialogId);
			if (d.availability.isAvailable(player)) {
				dialog = d;
			}
		}
		if (newDS.size() != this.dialogs.length) {
			this.dialogs = new int[newDS.size()];
			int i = 0;
			for (int id : newDS) {
				this.dialogs[i] = id;
				i++;
			}
		}
		return dialog;
	}

	public @Nonnull World getEntityWorld() {
		return this.world;
	}

	public Faction getFaction() {
		Faction fac = FactionController.instance.getFaction(this.dataManager.get(EntityNPCInterface.FactionData));
		if (fac == null) {
			return FactionController.instance.getFaction(FactionController.instance.getFirstFactionId());
		}
		return fac;
	}

	public EntityPlayerMP getFakeChatPlayer() {
		if (!this.isServerWorld()) {
			return null;
		}
		EntityUtil.Copy(this, EntityNPCInterface.ChatEventPlayer);
		EntityNPCInterface.ChatEventProfile.npc = this;
		EntityNPCInterface.ChatEventPlayer.refreshDisplayName();
		EntityNPCInterface.ChatEventPlayer.setWorld(this.world);
		EntityNPCInterface.ChatEventPlayer.setPosition(this.posX, this.posY, this.posZ);
		return EntityNPCInterface.ChatEventPlayer;
	}

	public @Nonnull Iterable<ItemStack> getHeldEquipment() {
		List<ItemStack> list = new ArrayList<>();
		list.add(ItemStackWrapper.MCItem(this.inventory.weapons.get(0)));
		list.add(ItemStackWrapper.MCItem(this.inventory.weapons.get(2)));
		return list;
	}

	public @Nonnull ItemStack getHeldItemMainhand() {
		IItemStack item;
		if (this.isAttacking()) {
			item = this.inventory.getRightHand();
		} else if (this.advanced.roleInterface instanceof RoleCompanion) {
			item = ((RoleCompanion) this.advanced.roleInterface).getHeldItem();
		} else if (this.advanced.jobInterface.overrideMainHand) {
			item = this.advanced.jobInterface.getMainhand();
		} else {
			item = this.inventory.getRightHand();
		}
		return ItemStackWrapper.MCItem(item);
	}

	public @Nonnull ItemStack getHeldItemOffhand() {
		IItemStack item;
		if (this.isAttacking()) {
			item = this.inventory.getLeftHand();
		} else if (this.advanced.jobInterface.overrideOffHand) {
			item = this.advanced.jobInterface.getOffhand();
		} else {
			item = this.inventory.getLeftHand();
		}
		return ItemStackWrapper.MCItem(item);
	}

	public @Nonnull ItemStack getItemStackFromSlot(@Nonnull EntityEquipmentSlot slot) {
		if (slot == EntityEquipmentSlot.MAINHAND) {
			return this.getHeldItemMainhand();
		}
		if (slot == EntityEquipmentSlot.OFFHAND) {
			return this.getHeldItemOffhand();
		}
		return ItemStackWrapper.MCItem(this.inventory.getArmor(3 - slot.getIndex()));
	}

	public String getJobData() {
		return this.dataManager.get(EntityNPCInterface.RoleData);
	}

	public boolean getLeashed() {
		return false;
	}

	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public @Nonnull String getName() {
		if (display == null) { return "Display is null!"; }
		return this.display.getName();
	}

	public EntityLivingBase getOwner() {
		if (this.ais.aiDisabled) { return null; }
		if (this.advanced.scenes.getOwner() != null) {
			return this.advanced.scenes.getOwner();
		}
		if (this.advanced.roleInterface instanceof RoleFollower) {
			return ((RoleFollower) this.advanced.roleInterface).owner;
		}
		if (this.advanced.roleInterface instanceof RoleCompanion) {
			return ((RoleCompanion) this.advanced.roleInterface).owner;
		}
		if (this.advanced.jobInterface instanceof JobFollower) {
			return ((JobFollower) this.advanced.jobInterface).following;
		}
		return null;
	}

	public @Nonnull BlockPos getPosition() {
		return new BlockPos(this.posX, this.posY, this.posZ);
	}

	public @Nonnull Vec3d getPositionVector() {
		return new Vec3d(this.posX, this.posY, this.posZ);
	}

	public @Nonnull EnumPushReaction getPushReaction() {
		return this.display.getHasHitbox() ? super.getPushReaction() : EnumPushReaction.IGNORE;
	}

	public String getRoleData() {
		return this.dataManager.get(EntityNPCInterface.RoleData);
	}

	protected float getSoundPitch() {
		if (this.advanced.disablePitch) {
			return 1.0f;
		}
		return super.getSoundPitch();
	}

	public float getSpeed() {
		return this.ais.getWalkingSpeed() / 20.0f;
	}

	public float getStartXPos() {
		return ais.startPos().getX() + ais.bodyOffsetX / 10.0f;
	}

	public double getStartYPos() {
		if (startYPos < 0.0) {
			return calculateStartYPos(ais.startPos());
		}
		return startYPos;
	}

	public float getStartZPos() {
		return ais.startPos().getZ() + ais.bodyOffsetZ / 10.0f;
	}

	public int getTalkInterval() {
		return 160;
	}

	public void givePlayerItem(EntityPlayer player, ItemStack item) {
		if (this.ais.aiDisabled || !this.isServerWorld()) { return; }
		item = item.copy();
		float f = 0.7f;
		double d = this.world.rand.nextFloat() * f + (1.0f - f);
		double d2 = this.world.rand.nextFloat() * f + (1.0f - f);
		double d3 = this.world.rand.nextFloat() * f + (1.0f - f);
		EntityItem entityitem = new EntityItem(this.world, this.posX + d, this.posY + d2, this.posZ + d3, item);
		entityitem.setPickupDelay(2);
		this.world.spawnEntity(entityitem);
		int i = item.getCount();
		if (player.inventory.addItemStackToInventory(item)) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
					SoundCategory.PLAYERS, 0.2f,
					((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7f + 1.0f) * 2.0f);
			player.onItemPickup(entityitem, i);
			if (item.getCount() <= 0) {
				entityitem.setDead();
			}
		}
	}

	public boolean hasOwner() {
		if (this.ais.aiDisabled) { return false; }
		return this.advanced.scenes.getOwner() != null
				|| (this.advanced.roleInterface instanceof RoleFollower
				&& ((RoleFollower) this.advanced.roleInterface).hasOwner())
				|| (this.advanced.roleInterface instanceof RoleCompanion
				&& ((RoleCompanion) this.advanced.roleInterface).hasOwner())
				|| (this.advanced.jobInterface instanceof JobFollower
				&& ((JobFollower) this.advanced.jobInterface).hasOwner());
	}

	public boolean isAttacking() {
		return dataManager.get(EntityNPCInterface.Attacking);
	}

	public boolean isEntityAlive() {
		boolean bo = super.isEntityAlive();
		if (ais != null && ais.aiDisabled) { return bo; }
		return bo && !isKilled();
	}

	public boolean isFollower() {
		if (this.ais.aiDisabled) { return false; }
		return this.advanced.scenes.getOwner() != null || this.advanced.roleInterface.isFollowing()
				|| this.advanced.jobInterface.isFollowing();
	}

	public boolean isFriend(Entity entityTarget) {
		if (!(entityTarget instanceof EntityNPCInterface)) {
			return false;
		}
		EntityNPCInterface npcTarget = (EntityNPCInterface) entityTarget;
        return this.faction.id == npcTarget.faction.id || npcTarget.faction.frendFactions.contains(this.faction.id)
                || npcTarget.advanced.friendFactions.contains(this.faction.id)
                || this.faction.frendFactions.contains(npcTarget.faction.id)
                || this.advanced.friendFactions.contains(npcTarget.faction.id);
    }

	public boolean isInRange(double posX, double posY, double posZ, double range) {
		double y = Math.abs(this.posY - posY);
		if (posY >= 0.0 && y > range) {
			return false;
		}
		double x = Math.abs(this.posX - posX);
		double z = Math.abs(this.posZ - posZ);
		return x <= range && z <= range;
	}

	public boolean isInRange(Entity entity, double range) {
		return this.isInRange(entity.posX, entity.posY, entity.posZ, range);
	}

	public boolean isInteracting() {
		return this.ticksExisted - this.lastInteract < 40
				|| (!this.isServerWorld() && this.dataManager.get(EntityNPCInterface.Interacting))
				|| (this.ais.stopAndInteract && !this.interactingEntities.isEmpty()
				&& this.ticksExisted - this.lastInteract < 180);
	}

	public boolean isInvisible() {
		return this.display.getVisible() == 1;
	}

	public boolean isInvisibleToPlayer(@Nonnull EntityPlayer player) {
		return this.isInvisible() && !(player.getHeldItemMainhand().getItem() instanceof INPCToolItem)
				&& this.display.getAvailability().isAvailable(player);
	}

	public boolean isKilled() {
		return this.isDead || (dataManager != null && dataManager.get(EntityNPCInterface.IsDead));
	}

	public boolean isMoving() {
		double sp = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
		double speed = 0.069d;
		if (sp != 0.0d) { speed = speed * 0.25d / sp; }
		double xz = Math.sqrt(Math.pow(this.motionX, 2.0d) + Math.pow(this.motionZ, 2.0d));
		return xz >= (speed / 2.0d) && (this.motionY <= -speed || this.motionY > 0.0d);
	}

	public boolean isOnSameTeam(@Nonnull Entity entity) {
		if (this.isServerWorld()) {
			if (entity instanceof EntityPlayer && this.getFaction().isFriendlyToPlayer((EntityPlayer) entity)) {
				return true;
			}
			if (entity == this.getOwner()) {
				return true;
			}
			if (entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).faction.id == this.faction.id) {
				return true;
			}
		}
		return super.isOnSameTeam(entity);
	}

	public boolean isPlayerSleeping() {
		return this.getHealth() <= 0.0f || currentAnimation == 2 && !isAttacking()
				&& getAttackingEntity() == null && navigating == null;
	}

	public boolean isPotionApplicable(@Nonnull PotionEffect effect) {
		return !this.stats.potionImmune && (this.getCreatureAttribute() != EnumCreatureAttribute.ARTHROPOD || effect.getPotion() != MobEffects.POISON) && super.isPotionApplicable(effect);
	}

	public boolean isPushedByWater() {
		return this.ais.movementType != 2;
	}

	public boolean isSneaking() {
		return this.currentAnimation == 4 || this.aiIsSneak;
	}

	public boolean isVeryNearAssignedPlace() {
		double xx = this.posX - this.getStartXPos();
		double zz = this.posZ - this.getStartZPos();
		return xx >= -0.2 && xx <= 0.2 && zz >= -0.2 && zz <= 0.2;
	}

	public boolean isWalking() {
		return this.ais.getMovingType() != 0 || this.isAttacking() || this.isFollower() || this.dataManager.get(EntityNPCInterface.Walking);
	}

	public void knockBack(@Nonnull Entity entity, float strength, double ratioX, double ratioZ) {
		super.knockBack(entity, strength * (2.0f - stats.resistances.get("knockback")), ratioX, ratioZ);
	}

	public boolean nearPosition(BlockPos pos) {
		BlockPos npcPos = this.getPosition();
		float x = (npcPos.getX() - pos.getX());
		float z = (npcPos.getZ() - pos.getZ());
		float y = (npcPos.getY() - pos.getY());
		float height = (MathHelper.ceil(this.height + 1.0f) * MathHelper.ceil(this.height + 1.0f));
		return x * x + z * z < 2.5 && y * y < height + 2.5;
	}

	public void onAttack(EntityLivingBase entity) {
		if (this.ais.aiDisabled || entity == null || entity == this || this.isAttacking() || this.ais.onAttack == 3 || entity == this.getOwner()) {
			return;
		}
		super.setAttackTarget(entity);
	}

	protected void collideWithEntity(@Nonnull Entity entityIn) {
		if (this.canBeCollidedWith()) { entityIn.applyEntityCollision(this); }
	}

	public void onCollide() {
		if (this.ais.aiDisabled || !this.isEntityAlive() || this.ticksExisted % 4 != 0 || !this.isServerWorld()) {
			return;
		}
		AxisAlignedBB axisalignedbb;
		if (this.getRidingEntity() != null && this.getRidingEntity().isEntityAlive()) {
			axisalignedbb = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).grow(1.0, 0.0, 1.0);
		} else {
			axisalignedbb = this.getEntityBoundingBox().grow(1.0, 0.5, 1.0);
		}
		List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
        for (Entity entity : list) {
            if (entity != this && entity.isEntityAlive()) {
                EventHooks.onNPCCollide(this, entity);
            }
        }
	}

	public void onDeath(@Nonnull DamageSource damagesource) {
		this.setSprinting(false);
		this.getNavigator().clearPath();
		this.extinguish();
		this.clearActivePotions();
		Entity attackingEntity = NoppesUtilServer.GetDamageSource(damagesource);
		if (this.advanced.roleInterface != null) {
			this.advanced.roleInterface.aiDeathExecute(attackingEntity);
		}
		if (this.advanced.jobInterface != null) {
			this.advanced.jobInterface.aiDeathExecute(attackingEntity);
		}
		if (this.isServerWorld()) {
			advanced.playSound(3, this.getSoundVolume(), this.getSoundPitch());
			NpcEvent.DiedEvent event = new NpcEvent.DiedEvent(this.wrappedNPC, damagesource, attackingEntity, this.combatHandler);
			double baseChance = 1.0d;
			if (!combatHandler.aggressors.isEmpty()) {
				double luck = 0.0d;
				double enchLv = 0.0d;
				int i = 0;
				int j = 0;
				for (EntityLivingBase e : combatHandler.aggressors.keySet()) {
					IAttributeInstance l = e.getEntityAttribute(SharedMonsterAttributes.LUCK);
					if (l != null) {
						luck += l.getAttributeValue();
						i++;
					}
					ItemStack held = !e.getHeldItemMainhand().isEmpty() ? e.getHeldItemMainhand() : e.getHeldItemOffhand();
					if (held.isItemEnchanted()) {
						enchLv += EnchantmentHelper.getLootingModifier(e);
						j++;
					}
				}
				// Luck
				if (i > 0 && luck > 0.0d) {
					luck /= i;
					if (luck < 0) {
						luck *= -1;
						baseChance -= luck * luck * -0.005555d + luck * 0.255555d; // 1lv = 25%$ 10lv = 200%
					} else {
						baseChance += luck * luck * -0.005555d + luck * 0.255555d; // 1lv = 25%$ 10lv = 200%
					}
				}
				// Enchantment
				if (j > 0 && enchLv > 0.0d) {
					enchLv /= j;
					baseChance += enchLv * enchLv * 0.000555d + enchLv * 0.019444d; // 1lv = +2%$ 10lv = +25%
				}
			} // chance
			// drop on ground
			Map<IEntity<?>, List<IItemStack>> mapD = inventory.createDrops(0, baseChance);
			if (mapD.isEmpty()) { event.droppedItems = new IItemStack[0]; }
			else {
				List<IItemStack> list = new ArrayList<>();
				event.droppedItems = new IItemStack[mapD.size()];
				for(IEntity<?> attacking : mapD.keySet()) {
					list.addAll(mapD.get(attacking));
				}
				event.droppedItems = list.toArray(new IItemStack[0]);
			}
			// drop on player
			event.lootedItems = inventory.createDrops(1, baseChance);
			// to inventory from player
			event.inventoryItems = inventory.createDrops(2, baseChance);
			event.expDropped = inventory.getExpRNG();
			event.line = this.advanced.getKilledLine();
			if (advanced.roleInterface instanceof RoleFollower && !((RoleFollower) advanced.roleInterface).inventory.isEmpty()) {
				for (ItemStack stack : ((RoleFollower) this.advanced.roleInterface).inventory.items) {
					if (NoppesUtilServer.IsItemStackNull(stack) || stack.isEmpty()) { continue; }
					this.entityDropItem(stack, 0.0f);
				}
				((RoleFollower) this.advanced.roleInterface).inventory.clear();
			}
			//
			EventHooks.onNPCDied(this, event);
			this.bossInfo.setVisible(false);
			this.inventory.dropStuff(event, damagesource);
			if (event.line != null) {
				saySurrounding(Line.formatTarget((Line) event.line, (attackingEntity instanceof EntityLivingBase) ? (EntityLivingBase) attackingEntity : null));
			}
		}
		AnimationConfig anim = animation.tryRunAnimation(AnimationKind.DIES);
		if (anim != null) {
			motionX = 0.0d;
			motionY = 0.0d;
			motionZ = 0.0d;
		}
		super.onDeath(damagesource);
	}

	public void onDeathUpdate() {
		if (this.stats.spawnCycle == 3 || this.stats.spawnCycle == 4) {
			super.onDeathUpdate();
			return;
		}
		++this.deathTime;
		if (this.world.isRemote) {
			return;
		}
		if (!this.hasDied) {
			this.setDead();
		}
		if (this.killedTime < System.currentTimeMillis() && (this.stats.spawnCycle == 0 || (this.world.isDaytime() && this.stats.spawnCycle == 1) || (!this.world.isDaytime() && this.stats.spawnCycle == 2))) {
			this.reset();
		}
	}

	public void onLivingUpdate() {
		if (CustomNpcs.FreezeNPCs) {
			return;
		}
		CustomNpcs.debugData.startDebug(this.isServerWorld() ? "Server" : "Client", this, "NPCLivingUpdate");
		if (isAIDisabled()) {
			super.onLivingUpdate();
			CustomNpcs.debugData.endDebug(this.isServerWorld() ? "Server" : "Client", this, "NPCLivingUpdate");
			return;
		}
		++this.totalTicksAlive;
		this.updateArmSwingProgress();
		if (this.totalTicksAlive % 20 == 0) {
			this.faction = this.getFaction();
		}
		if (this.isServerWorld()) {
			if (!this.ais.aiDisabled) {
				if (this.aiAttackTarget != null) {
					this.aiAttackTarget.update();
				}
				if (!this.isKilled() && this.totalTicksAlive % 20 == 0) {
					this.advanced.scenes.update();
					if (this.getHealth() < this.getMaxHealth()) {
						if (this.stats.healthRegen > 0 && !this.isAttacking()) {
							this.heal(this.stats.healthRegen);
							((WorldServer) this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + this.height, this.posZ, 1, this.width / 3.0d, 0.05d, this.width / 3.0d, 1.0d);
						}
						if (this.stats.combatRegen > 0 && this.isAttacking()) {
							this.heal(this.stats.combatRegen);
							((WorldServer) this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + this.height, this.posZ, 1, this.width / 3.0d, 0.05d, this.width / 3.0d, 1.0d);
						}
					}
					if (this.faction.getsAttacked && !this.isAttacking()) {
						List<EntityMob> list = this.world.getEntitiesWithinAABB(EntityMob.class, this.getEntityBoundingBox().grow(16.0, 16.0, 16.0));
						for (EntityMob mob : list) {
							if (mob.getAttackTarget() == null && this.canSee(mob)) {
								mob.setAttackTarget(this);
							}
						}
					}
					if (this.linkedData != null && this.linkedData.time > this.linkedLast) {
						LinkedNpcController.Instance.loadNpcData(this);
					}
					if (this.updateClient) {
						this.updateClient();
					}
					if (this.updateAI) {
						this.updateTasks();
						this.updateAI = false;
					}
				}
			}
			if (this.updateLook) {
				Server.sendToAll(this.world.getMinecraftServer(), EnumPacketClient.NPC_LOOK_POS, this.world.provider.getDimension(), this.getEntityId(), this.lookAt == null ? -1 : this.lookAt.getEntityId());
			}
			if (this.getHealth() <= 0.0f && !this.isKilled()) {
				this.clearActivePotions();
				this.dataManager.set(EntityNPCInterface.IsDead, true);
				this.updateTasks();
				this.updateHitbox();
			}
			if (this.display.getBossbar() == 2) {
				this.bossInfo.setVisible(this.getAttackTarget() != null);
			}
			this.dataManager.set(EntityNPCInterface.Walking, !this.getNavigator().noPath());
			this.dataManager.set(EntityNPCInterface.Interacting, this.isInteracting());
			this.combatHandler.update();
			this.onCollide();
		} else {
			isAirBorne = this.canFly() && world.getBlockState(this.getPosition().down()).getMaterial() == Material.AIR;
		}
		if (CustomNpcs.ShowCustomAnimation) {
			// Jump
			if (!animation.getJump() && !isKilled() && getHealth() > 0.0f && world != null && !(isInWater() || isInLava()) && ais.getNavigationType() == 0 && !onGround && motionY > 0.0d) {
				BlockPos posUnderfoot = getPosition().down();
				BlockPos posAhead = getPosition().add(motionX, 0, motionZ).down();
				boolean canJumpHere = !(world.getBlockState(posUnderfoot).getBlock() instanceof BlockStairs);
				boolean canLandThere = !(world.getBlockState(posAhead).getBlock() instanceof BlockStairs);
				if (canJumpHere && canLandThere) {
					animation.setJump(true);
					animation.tryRunAnimation(AnimationKind.JUMP);
				}
			}
			else if (animation.getJump() && onGround && animation.getAnimationStage() != EnumAnimationStages.Started) {
				animation.setJump(false);
				if (animation.isAnimated(AnimationKind.JUMP)) {
					animation.stopAnimation();
				}
			}
			// Swing
			if (!animation.getSwing() && swingProgress > 0.0f) {
				animation.setSwing(true);
				if (!animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.AIM, AnimationKind.SHOOT)) {
					AnimationConfig anim = animation.tryRunAnimation(AnimationKind.SWING);
					if (anim != null) {
						swingProgress = 0.0f;
						swingProgressInt = 0;
						prevSwingProgress = 0.0f;
						isSwingInProgress = false;
					}
				}
			}
			else if (animation.getSwing() && swingProgress == 0.0f) {
				animation.setSwing(false);
			}
			// walking or standing
			animation.resetWalkAndStandAnimations();
		}

		if (this.wasKilled != this.isKilled() && this.wasKilled) {
			this.reset();
		}
		if (this.world.isDaytime() && this.isServerWorld() && this.stats.burnInSun) {
			float f = this.getBrightness();
			if (f > 0.5f && this.rand.nextFloat() * 30.0f < (f - 0.4f) * 2.0f
					&& this.world.canBlockSeeSky(new BlockPos(this))) {
				this.setFire(8);
			}
		}
		super.onLivingUpdate();
		if (dataManager != null && isAttacking() && getAttackTarget() != null) {
			dataManager.set(EntityNPCInterface.AimRotationYaw, rotationYawHead);
		}
		if (!this.isServerWorld()) {
			this.advanced.roleInterface.clientUpdate();
			if (this.textureCloakLocation != null) {
				this.cloakUpdate();
			}
			if (this.currentAnimation != this.dataManager.get(EntityNPCInterface.Animation)) {
				this.currentAnimation = this.dataManager.get(EntityNPCInterface.Animation);
				this.animationStart = this.ticksExisted;
				this.updateHitbox();
			}
			if (!this.ais.aiDisabled && this.advanced.jobInterface instanceof JobBard) {
				((JobBard) this.advanced.jobInterface).onLivingUpdate();
			}
		}
		if (this.display.getBossbar() > 0) {
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
		}
		CustomNpcs.debugData.endDebug(this.isServerWorld() ? "Server" : "Client", this, "NPCLivingUpdate");
	}

	@Override
	public void onUpdate() {
		CustomNpcs.debugData.startDebug(this.isServerWorld() ? "Server" : "Client", this, "NPCUpdate");
		super.onUpdate();
		if (animation != null) { animation.updateTime(); }
		if (!this.ais.aiDisabled && this.ticksExisted % 10 == 0) {
			if (this.initTime != 0L && !this.isServerWorld() && this.initTime < System.currentTimeMillis() - 1000L) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.NpcData, this.getEntityId());
				this.initTime = 0L;
			}
			if (!this.isKilled()) {
				if (this.width <= 1.0E-5f) {
					this.updateHitbox();
				}
			}
			// Path change
			Path path = getNavigator().getPath();
			if (isServerWorld()) {
				if (path != null) {
					PathPoint fp = path.getFinalPathPoint();
					BlockPos pos = getPosition();
					if (fp == null || pos.getX() == fp.x && pos.getY() == fp.y && pos.getZ() == fp.z) {
						navigating = null;
						updateNavClient();
					}
					else if (path != navigating) {
						navigating = path;
						updateNavClient();
					}
				} else if (navigating != null) {
					navigating = null;
					updateNavClient();
				}
			}
			if (this.ais.onAttack == 1) { // Panic
				if ((this.isBurning() || this.getAttackTarget() != null) && (this.getNavigator().noPath() || !this.isMoving())) {
					Vec3d vec = RandomPositionGenerator.findRandomTarget(this, 5, 4);
					if (vec != null && (!CustomNpcs.ShowCustomAnimation || !this.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES))) {
						this.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.3d);
					}
				}
			}
			this.startYPos = this.calculateStartYPos(this.ais.startPos()) + 1.0;
			if ((this.startYPos < 0.0 || this.startYPos > 255.0) && this.isServerWorld()) {
				this.setDead();
			}
			EventHooks.onNPCTick(this);
		}
		if ((this.isSneaking() && !this.isOldSneaking) || (!this.isSneaking() && this.isOldSneaking)) {
			this.updateHitbox();
			this.isOldSneaking = this.isSneaking();
		}
		if (this.deathTime > 0 || (this.getAttackTarget() instanceof EntityPlayer && ((EntityPlayer) this.getAttackTarget()).capabilities.disableDamage)) {
			super.setAttackTarget(null);
		}
		if (!this.ais.aiDisabled) {  this.timers.update(); }
		if (!isServerWorld()) {
			if (wasKilled != isKilled()) {
				deathTime = 0;
				updateHitbox();
			}
			else if (!isAttacking() && getNavigator().noPath() && currentAnimation != ais.animationType) {
				currentAnimation = ais.animationType;
				updateHitbox();
			}
		}
		this.wasKilled = this.isKilled();
		if (this.currentAnimation == 14) {
			this.deathTime = 19;
		}
		CustomNpcs.debugData.endDebug(this.isServerWorld() ? "Server" : "Client", this, "NPCUpdate");
	}

	protected void playHurtSound(DamageSource ignoredSource, boolean isBlocked) {
		this.advanced.playSound(isBlocked ? 5 : 2, this.getSoundVolume(), this.getSoundPitch());
	}

	public void playLivingSound() {
		if (!this.isEntityAlive()) {
			return;
		}
		this.advanced.playSound((this.getAttackTarget() != null) ? 1 : 0, this.getSoundVolume(), this.getSoundPitch());
	}

	protected void playStepSound(@Nonnull BlockPos pos, @Nonnull Block block) {
		if (this.advanced.getSound(4) != null) {
			this.advanced.playSound(4, 0.15f, 1.0f);
		} else {
			super.playStepSound(pos, block);
		}
	}

	public boolean processInteract(@Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
		if (!isServerWorld()) { return !isAttacking(); }
		if (hand != EnumHand.MAIN_HAND) { return true; }
		ItemStack stack = player.getHeldItem(hand);
        Item item = stack.getItem();
        if (item == CustomRegisters.moving) {
            this.setAttackTarget(null);
			if (player.getHeldItemMainhand().getTagCompound() == null || getEntityId() != player.getHeldItemMainhand().getTagCompound().getInteger("NPCID")) {
				player.sendMessage( new TextComponentTranslation("message.pather.reg", this.getName(), stack.getDisplayName()));
			}
            stack.setTagInfo("NPCID", new NBTTagInt(this.getEntityId()));
            Server.sendData((EntityPlayerMP) player, EnumPacketClient.NPC_MOVINGPATH, this.getEntityId(), this.ais.writeToNBT(new NBTTagCompound()));
            return true;
        } else if (item instanceof INPCToolItem) {
            this.setAttackTarget(null);
            this.setRevengeTarget(null);
            return true;
        }
        if (!this.ais.aiDisabled && EventHooks.onNPCInteract(this, player)) { return false; }
		if (this.getFaction().isAggressiveToPlayer(player)) {
			if (!isAttacking()) { setAttackTarget(player); }
			return !isAttacking();
		}
		this.addInteract(player);
		if (!lookAi.fastRotation) {
			AnimationConfig anim = animation.tryRunAnimation(AnimationKind.INTERACT);
			if (anim != null ) {
				lookAi.fastRotation = true;
				CustomNPCsScheduler.runTack(() -> lookAi.fastRotation = false , anim.totalTicks * 50);
			}
		}
		Dialog dialog = this.getDialog(player);
		PlayerData pd = PlayerData.get(player);
		if (!this.faction.getIsHidden() && !pd.factionData.factionData.containsKey(this.faction.id)) {
			PlayerEvent.FactionUpdateEvent event = new PlayerEvent.FactionUpdateEvent((PlayerWrapper<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player), this.faction, this.faction.defaultPoints, true);
			EventHooks.onPlayerFactionChange(pd.scriptData, event);
			pd.factionData.factionData.put(this.faction.id, event.points);
		}
		QuestData data = pd.questData.getQuestCompletion(player, this);
		if (data != null) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.QUEST_COMPLETION, data.quest.id);
		} else if (dialog != null) {
			NoppesUtilServer.openDialog(player, this, dialog);
		} else if (!this.ais.aiDisabled && advanced.roleInterface.getType() > 0) {
			advanced.roleInterface.interact(player);
		} else {
			this.say(player, this.advanced.getInteractLine());
		}
		return true;
	}

	public void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.npcVersion = compound.getInteger("ModRev");
		VersionCompatibility.CheckNpcCompatibility(this, compound);
		this.display.readToNBT(compound);
		this.stats.readToNBT(compound);
		this.ais.readToNBT(compound);
		this.script.readFromNBT(compound);
		this.timers.readFromNBT(compound);
		this.advanced.readToNBT(compound);
		this.animation.load(compound);
		this.inventory.readEntityFromNBT(compound);
		this.transform.readToNBT(compound);
		this.killedTime = compound.getLong("KilledTime");
		this.totalTicksAlive = compound.getLong("TotalTicksAlive");
		this.linkedName = compound.getString("LinkedNpcName");
		if (this.isServerWorld()) {
			LinkedNpcController.Instance.loadNpcData(this);
		}
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CustomNpcs.NpcNavRange);
		this.updateAI = true;
		if (compound.hasKey("HomeDimensionId", 3)) {
			this.homeDimensionId = compound.getInteger("HomeDimensionId");
		}
	}

	public void readSpawnData(ByteBuf buf) {
		try {
			this.readSpawnData(Server.readNBT(buf));
		} catch (IOException e) { LogWriter.error("Error:", e); }
	}

	public void readSpawnData(NBTTagCompound compound) {
		this.display.readToNBT(compound);
		this.animation.load(compound);
		this.stats.setLevel(compound.getInteger("NPCLevel"));
		this.stats.setRarity(compound.getInteger("NPCRarity"));
		this.stats.setRarityTitle(compound.getString("NPCRarityTitle"));
		this.stats.setMaxHealth(compound.getInteger("MaxHealth"));
		this.stats.hideKilledBody = compound.getBoolean("DeadBody");
		this.stats.aggroRange = compound.getInteger("AggroRange");
		if (this.stats.aggroRange < 1) {
			this.stats.aggroRange = 1;
		}
		IAttributeInstance follow_range = this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        follow_range.setBaseValue(this.stats.aggroRange);
        this.ais.setWalkingSpeed(compound.getInteger("Speed"));
		this.ais.setStandingType(compound.getInteger("StandingState"));
		this.ais.setMovingType(compound.getInteger("MovingState"));
		this.ais.orientation = compound.getInteger("Orientation");
		this.ais.bodyOffsetX = compound.getFloat("PositionXOffset");
		this.ais.bodyOffsetY = compound.getFloat("PositionYOffset");
		this.ais.bodyOffsetZ = compound.getFloat("PositionZOffset");
		if (compound.hasKey("MaxHurtResistantTime", 3)) {
			this.ais.setMaxHurtResistantTime(compound.getInteger("MaxHurtResistantTime"));
			this.maxHurtResistantTime = this.ais.getMaxHurtResistantTime();
		}
		this.inventory.armor = NBTTags.getIItemStackMap(compound.getTagList("Armor", 10));
		this.inventory.weapons = NBTTags.getIItemStackMap(compound.getTagList("Weapons", 10));
		if (this.advanced.jobInterface instanceof JobBard) {
			NBTTagCompound bard = compound.getCompoundTag("Bard");
			this.advanced.jobInterface.readFromNBT(bard);
		}
		if (this.advanced.jobInterface instanceof JobFollower) {
			NBTTagCompound follower = compound.getCompoundTag("Companion");
			this.advanced.jobInterface.readFromNBT(follower);
		}
		if (this instanceof EntityCustomNpc) {
			((EntityCustomNpc) this).modelData.readFromNBT(compound.getCompoundTag("ModelData"));
		}
		this.advanced.readToNBT(compound);
		this.dataManager.set(EntityNPCInterface.IsDead, compound.getBoolean("IsDead"));
		this.deathTime = compound.getInteger("DeathTime");
	}

	public void removeTrackingPlayer(@Nonnull EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	public void reset() {
		this.hasDied = false;
		this.isDead = false;
		this.setSprinting(this.wasKilled = false);
		this.aiIsSneak = false;
		this.aiOwnerNPC = null;
		this.updateAiClient();
		this.setHealth(this.getMaxHealth());
		this.dataManager.set(EntityNPCInterface.Animation, 0);
		this.dataManager.set(EntityNPCInterface.Walking, false);
		this.dataManager.set(EntityNPCInterface.IsDead, false);
		this.dataManager.set(EntityNPCInterface.Interacting, false);
		this.interactingEntities.clear();
		this.combatHandler.reset();
		this.setAttackTarget(null);
		this.setRevengeTarget(null);
		this.deathTime = 0;
		this.setFire(0);
		this.lookAt = null;
		if (this.lookAi != null) { this.lookAi.fastRotation = false; }
		updateLook = false;
		if (this.ais.returnToStart && (this.advanced.jobInterface instanceof JobFollower || !this.hasOwner()) && this.isServerWorld() && !this.isRiding()) {
			double x = this.getStartXPos();
			double y = this.getStartYPos();
			double z = this.getStartZPos();
			if (this.world != null) {
				BlockPos pos = new BlockPos(x, y, z);
				IBlockState state = this.world.getBlockState(pos);
				if (state.getBlock().isPassable(this.world, pos)) { // possibly high
					for (int i = (int) y; i >= 0; i--) {
						pos = pos.down();
						state = this.world.getBlockState(pos);
						if (!state.getBlock().isPassable(this.world, pos)) {
							pos = pos.up();
							if (y - pos.getY() < 3) {
								y = pos.getY();
							}
							break;
						}
					}
				}
			}
			this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		}
		this.maxHurtResistantTime = this.ais.getMaxHurtResistantTime();
		this.killedTime = 0L;
		this.extinguish();
		this.clearActivePotions();
		this.travel(0.0f, 0.0f, 0.0f);
		this.distanceWalkedModified = 0.0f;
		this.getNavigator().clearPath();
		this.currentAnimation = 0;
		this.updateHitbox();
		this.updateAI = true;
		this.ais.movingPos = 0;
		if (this.getOwner() != null) {
			this.getOwner().setLastAttackedEntity(Objects.requireNonNull(EntityList.newEntity(EntityPainting.class, this.world)));
		}
		this.bossInfo.setVisible(this.display.getBossbar() == 1);
		this.advanced.jobInterface.reset();
		if (animation.isAnimated()) {  animation.stopAnimation(); }
		animation.tryRunAnimation(AnimationKind.INIT);
		this.updateClient = true;
		if (this.ais.returnToStart && this.homeDimensionId != this.world.provider.getDimension() && !(this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER && this.advanced.roleInterface.isFollowing())) {
			try {
				Util.instance.teleportEntity(this.world.getMinecraftServer(), this, homeDimensionId, this.posX, this.posY, this.posZ);
			} catch (CommandException e) { LogWriter.error("Error:", e); }
		}
		this.stepHeight = this.ais.stepheight;
		EventHooks.onNPCInit(this);
	}

	public void reset(int delay) {
		CustomNPCsScheduler.runTack(this::reset, delay);
	}

	public void say(EntityPlayer player, Line line) {
		if (line == null || !this.getEntitySenses().canSee(player)) { return; }
		if (!line.getSound().isEmpty()) {
			BlockPos pos = this.getPosition();
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.PLAY_SOUND, line.getSound(), pos.getX(),
					pos.getY(), pos.getZ(), this.getSoundVolume(), this.getSoundPitch());
		}
		boolean isEmpty = line.getText().isEmpty();
		if (!isEmpty) {
			isEmpty = true;
			for (int i = 0; i < line.getText().length(); i++) {
				if (line.getText().charAt(i) != ((char) 32) || line.getText().charAt(i) != ((char) 9)) {
					isEmpty = false;
					break;
				}
			}
		}
		if (!isEmpty) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT_BUBBLE, this.getEntityId(), line.getText(),
					line.getShowText());
		}
	}

	public void saySurrounding(Line line) {
		if (line == null) {
			return;
		}
		if (line.getShowText() && !line.getText().isEmpty()) {
			ServerChatEvent event = new ServerChatEvent(this.getFakeChatPlayer(), line.getText(), new TextComponentTranslation(line.getText().replace("%", "%%")));
			if (CustomNpcs.NpcSpeachTriggersChatEvent && (MinecraftForge.EVENT_BUS.post(event) || event.getComponent() == null)) {
				return;
			}
			line.setText(event.getComponent().getUnformattedText().replace("%%", "%"));
		}
		List<EntityPlayer> inRange = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(20.0, 20.0, 20.0));
		for (EntityPlayer player : inRange) {
			this.say(player, line);
		}
	}

	public void seekShelter() {
		if (this.ais.aiDisabled) { return; }
		if (this.ais.findShelter == 0) {
			this.tasks.addTask(this.taskCount++, new EntityAIMoveIndoors(this));
		} else if (this.ais.findShelter == 1) {
			if (!this.canFly()) {
				this.tasks.addTask(this.taskCount++, new EntityAIRestrictSun(this));
			}
			this.tasks.addTask(this.taskCount++, new EntityAIFindShade(this));
		}
	}

	public void setPriorityAttackTarget(EntityLivingBase entityTarget) {
		if (!isEntityAlive() ||
				getAttackTarget() == entityTarget ||
				(entityTarget instanceof EntityPlayer && ((EntityPlayer) entityTarget).capabilities.disableDamage) ||
				(entityTarget != null && entityTarget == getOwner()) ||
				(entityTarget instanceof EntityNPCInterface && isFriend(entityTarget))
		) {
			return;
		}
		super.setAttackTarget(entityTarget);
	}
	/**
	 * dataManager.set(EntityNPCInterface.Attacking, boolean); in to CombatHandler
	 */
    public void setAttackTarget(EntityLivingBase entityTarget) {
		if (!isEntityAlive() ||
				getAttackTarget() == entityTarget ||
				(entityTarget instanceof EntityPlayer && ((EntityPlayer) entityTarget).capabilities.disableDamage) ||
				(entityTarget != null && entityTarget == getOwner()) ||
				(entityTarget instanceof EntityNPCInterface && isFriend(entityTarget))
		) {
			return;
		}
		//LogWriter.debug("Set Attack: "+entityTarget+" // "+getAttackTarget());
		if (entityTarget != null) {
			if (getAttackTarget() != null && combatHandler.priorityTarget != null) {
				return;
			}
			NpcEvent.TargetEvent event = new NpcEvent.TargetEvent(this.wrappedNPC, entityTarget);
			if (EventHooks.onNPCTarget(this, event)) { return; }
			if (event.entity == null) { entityTarget = null; }
			else { entityTarget = event.entity.getMCEntity(); }
			if (getAttackTarget() == entityTarget ||
					(entityTarget instanceof EntityPlayer && ((EntityPlayer) entityTarget).capabilities.disableDamage) ||
					(entityTarget != null && entityTarget == getOwner()) ||
					(entityTarget instanceof EntityNPCInterface && this.isFriend(entityTarget))
			) {
				return;
			}
		}
		else {
			for (EntityAITasks.EntityAITaskEntry en : targetTasks.taskEntries) {
				if (en.using) {
					en.using = false;
					en.action.resetTask();
				}
			}
			if (EventHooks.onNPCTargetLost(this, getAttackTarget())) {
				return;
			}
		}
		if (entityTarget != null && entityTarget != this && this.ais.onAttack != 3 && !this.isAttacking() && this.isServerWorld()) {
			Line line = advanced.getAttackLine();
			if (line != null) {
				saySurrounding(Line.formatTarget(line, entityTarget));
			}
		}
		super.setAttackTarget(entityTarget);
		updateTargetClient();
	}

	public void setCurrentAnimation(int animation) {
		currentAnimation = animation;
		dataManager.set(EntityNPCInterface.Animation, animation);
		if (animation != 4 && this.aiAttackTarget instanceof EntityAICommanderTarget) {
			((EntityAICommanderTarget) this.aiAttackTarget).baseAnimation = animation;
		}
		this.updateAnimationClient();
	}

	public void setDataWatcher(EntityDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void setDead() {
		this.hasDied = true;
		this.removePassengers();
		this.dismountRidingEntity();
		if (!this.isServerWorld() || this.stats.spawnCycle == 3 || this.stats.spawnCycle == 4) {
			this.delete();
		} else {
			this.setHealth(-1.0f);
			this.setSprinting(false);
			this.getNavigator().clearPath();
			this.setCurrentAnimation(2);
			this.updateHitbox();
			if (this.killedTime <= 0L) {
				this.killedTime = this.stats.respawnTime * 1000L + System.currentTimeMillis();
			}
			if (!this.ais.aiDisabled) {
				this.advanced.roleInterface.killed();
				this.advanced.jobInterface.killed();
			}
		}
	}

	public void setFaction(int id) {
		if (id < 0 || !this.isServerWorld()) {
			return;
		}
		this.dataManager.set(EntityNPCInterface.FactionData, id);
	}

	public void setHomePosAndDistance(@Nonnull BlockPos pos, int range) {
		super.setHomePosAndDistance(pos, range);
		ais.setStartPos(pos);
		updateClient = true;
	}

	public void setImmuneToFire(boolean immuneToFire) {
		this.isImmuneToFire = immuneToFire;
		this.stats.immuneToFire = immuneToFire;
	}

	public void setInWeb() {
		if (!this.stats.ignoreCobweb) {
			super.setInWeb();
		}
	}

	public void setItemStackToSlot(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack item) {
		if (slot == EntityEquipmentSlot.MAINHAND) {
			this.inventory.weapons.put(0, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
		} else if (slot == EntityEquipmentSlot.OFFHAND) {
			this.inventory.weapons.put(2, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
		} else {
			this.inventory.armor.put(3 - slot.getIndex(), Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
		}
	}

	public void setJobData(String s) {
		this.dataManager.set(EntityNPCInterface.RoleData, s);
	}

	public void setMoveType() {
		if (this.ais.getMovingType() == 1) {
			this.tasks.addTask(this.taskCount++, new EntityAIWander(this));
		}
		if (this.ais.getMovingType() == 2) {
			this.tasks.addTask(this.taskCount++, new EntityAIMovingPath(this));
		}
	}

	public void setPortal(@Nonnull BlockPos pos) {
	}

	private void setResponse() {
		this.aiAttackTarget = null;
		this.aiIsSneak = false;
		this.aiOwnerNPC = null;
		if (this.ais.aiDisabled) { return; }
		if (this.ais.canSprint) {
			this.tasks.addTask(this.taskCount++, new EntityAISprintToTarget(this));
		}
		if (this.ais.onAttack == 2) { // Avoid
			this.tasks.addTask(this.taskCount++, this.aiAttackTarget = new EntityAIAvoidTarget(this));
		} else if (this.ais.onAttack == 0) { // Attack
			if (this.ais.canLeap) {
				this.tasks.addTask(this.taskCount++, new EntityAIPounceTarget(this));
			} // can Jump
			switch (this.ais.tacticalVariant) {
				case 0: {
					this.tasks.addTask(this.taskCount++, (this.aiAttackTarget = new EntityAIOnslaught(this)));
					break;
				}
				case 1: {
					this.tasks.addTask(this.taskCount++,
							(this.aiAttackTarget = new EntityAIDodge(this)));
					break;
				}
				case 2: {
					this.tasks.addTask(this.taskCount++,
							(this.aiAttackTarget = new EntityAISurround(this)));
					break;
				}
				case 3: {
					this.tasks.addTask(this.taskCount++,
							(this.aiAttackTarget = new EntityAIHitAndRun(this)));
					break;
				}
				case 4: {
					this.tasks.addTask(this.taskCount++, this.aiAttackTarget = new EntityAICommanderTarget(this));
					break;
				}
				case 5: {
					this.tasks.addTask(this.taskCount++, this.aiAttackTarget = new EntityAIStalkTarget(this));
					break;
				}
				default: {
					this.tasks.addTask(this.taskCount++,
							(this.aiAttackTarget = new EntityAINoTactic(this)));
					break;
				}
			}
		}
	}

	public void setRoleData(String s) {
		this.dataManager.set(EntityNPCInterface.RoleData, s);
	}

	public void setSwingingArms(boolean swingingArms) {
	}

	public EntityProjectile shoot(double x, double y, double z, int accuracy, ItemStack proj, boolean indirect) {
		EntityProjectile projectile = new EntityProjectile(this.world, this, proj.copy(), true);
		double varX = x - this.posX;
		double varY = y - (this.posY + this.getEyeHeight());
		double varZ = z - this.posZ;
		float varF = projectile.hasGravity() ? MathHelper.sqrt(varX * varX + varZ * varZ) : 0.0f;
		float angle = projectile.getAngleForXYZ(varY, varF, indirect);
		float acc = 20.0f - MathHelper.floor(accuracy / 5.0f);
		projectile.shoot(varX, varY, varZ, angle, acc);
		this.world.spawnEntity(projectile);
		animation.tryRunAnimation(AnimationKind.SHOOT);
		if (animation.isAnimated(AnimationKind.AIM)) {
			animation.stopAnimation();
		}
		return projectile;
	}

	public EntityProjectile shoot(EntityLivingBase entity, int accuracy, ItemStack proj, boolean indirect) {
		return this.shoot(entity.posX, entity.getEntityBoundingBox().minY + entity.height / 2.0f, entity.posZ, accuracy, proj, indirect);
	}

	public boolean shouldDismountInWater(@Nonnull Entity rider) {
		return false;
	}

	public void tpTo(EntityLivingBase owner) {
		if (owner == null) {
			return;
		}
		EnumFacing facing = owner.getHorizontalFacing().getOpposite();
		BlockPos pos = new BlockPos(owner.posX, owner.getEntityBoundingBox().minY, owner.posZ);
		pos = pos.add(facing.getFrontOffsetX(), 0, facing.getFrontOffsetZ());
		pos = this.calculateTopPos(pos);
		for (int i = -1; i < 2; ++i) {
			for (int j = 0; j < 3; ++j) {
				BlockPos check;
				if (facing.getFrontOffsetX() == 0) {
					check = pos.add(i, 0, j * facing.getFrontOffsetZ());
				} else {
					check = pos.add(j * facing.getFrontOffsetX(), 0, i);
				}
				check = this.calculateTopPos(check);
				if (!this.world.getBlockState(check).isFullBlock()
						&& !this.world.getBlockState(check.up()).isFullBlock()) {
					this.setLocationAndAngles((check.getX() + 0.5f), check.getY(), (check.getZ() + 0.5f),
							this.rotationYaw, this.rotationPitch);
					this.getNavigator().clearPath();
					break;
				}
			}
		}
	}

	public void travel(float f1, float f2, float f3) {
		double d0 = this.posX;
		double d2 = this.posY;
		double d3 = this.posZ;
		super.travel(f1, f2, f3);
		if (this.ais.aiDisabled) { return; }
		if (this.advanced.roleInterface instanceof RoleCompanion && this.isServerWorld()) {
			((RoleCompanion) this.advanced.roleInterface).addMovementStat(this.posX - d0, this.posY - d2,
					this.posZ - d3);
		}
	}

	public void updateAiClient() {
		if (!this.isServerWorld() || this.aiAttackTarget == null) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", this.getEntityId());
		aiAttackTarget.writeToClientNBT(compound);
		if (compound.getKeySet().size() > 1) {
			Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_AI_TARGET, compound);
		}
	}

	public void updateAnimationClient() {
		if (!this.isServerWorld()) { return; }
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, world.provider.getDimension(), getEntityId(), currentAnimation);
	}

	public void updateClient() {
		NBTTagCompound compound = this.writeSpawnData();
		compound.setInteger("EntityId", this.getEntityId());
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC, compound);
		this.updateClient = false;
		this.updateNavClient();
	}

	public void updateHitbox() {
		// collide in
		// EntityRenderer.getMouseOver(0.0f);
		// AABB = this.getEntityBoundingBox == (this.boundingBox);
		// set in setPosition();
		if (((currentAnimation == AnimationType.SLEEP.get() || currentAnimation == AnimationType.CRAWL.get()) && !isAttacking()) || deathTime > 0) {
			width = 0.8f;
			height = 0.4f;
		} else if (this.isRiding()) {
			width = 0.6f;
			height = this.baseHeight * 0.77f;
		} else if (this.isSneaking()) {
			width = 0.6f;
			height = this.baseHeight * 0.775f;
		} else {
			width = 0.6f;
			height = this.baseHeight;
		}
		if (!display.getHasHitbox() || (isKilled() && stats.hideKilledBody)) {
			width = 1.0E-5f;
			height = 0.25f;
		}
		else if (display.getHasHitbox() && display.width != 0.0f && display.height != 0.0f) {
			width = display.width;
			height = display.height;
		}
		if (display.getModel() == null && this instanceof EntityCustomNpc) {
			ModelData modeldata = ((EntityCustomNpc) this).modelData;
			ModelPartConfig model = modeldata.getPartConfig(EnumParts.HEAD);
			float scaleHead = Math.max(model.scale[0], model.scale[2]);
			model = modeldata.getPartConfig(EnumParts.BODY);
			float scaleBody = Math.max(model.scale[0], model.scale[2]);
			width *= Math.max(scaleHead, scaleBody);
			width = width / 5.0f * display.getSize();
			this.height = height / 5.0f * display.getSize();
		}
		double n = width / 2.0f;
		if (n > World.MAX_ENTITY_RADIUS) {
			World.MAX_ENTITY_RADIUS = n;
		}
		if (getHealth() == 0) { return; }
		setPosition(posX, posY, posZ); // set BoundingBox
	}

	public void updateNavClient() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", this.getEntityId());
		compound.setBoolean("IsNavigating", navigating != null);
		if (navigating != null) {
			compound.setTag("Navigating", Server.writePathToNBT(navigating));
		}
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_NAVIGATION, compound);
	}

	public void updateTargetClient() {
		if (!isServerWorld()) { return; }
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_TARGET, getEntityId(), getAttackTarget() != null ? getAttackTarget().getEntityId() : -1);
	}

	private void updateTasks() {
		if (!this.isServerWorld()) {
			return;
		}
		this.clearTasks(this.tasks);
		this.clearTasks(this.targetTasks);
		if (this.isKilled()) {
			return;
		}
		Predicate<EntityLivingBase> attackEntitySelector = new NPCAttackSelector(this);
		this.targetTasks.addTask(0, new EntityAIClearTarget(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAIClosestTarget(this, EntityLivingBase.class, 4, this.ais.directLOS, false, attackEntitySelector));
		this.targetTasks.addTask(3, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(4, new EntityAIOwnerHurtTarget(this));

		PathWorldListener pwl = ((IWorldMixin) this.world).npcs$getPathListener();
		if (pwl != null) { pwl.onEntityRemoved(this); }
		if (this.ais.movementType == 1) {
			this.moveHelper = new FlyingMoveHelper(this);
			this.navigator = new PathNavigateFlying(this, this.world);
		} else if (this.ais.movementType == 2) {
			this.moveHelper = new FlyingMoveHelper(this);
			this.navigator = new PathNavigateSwimmer(this, this.world);
		} else {
			this.moveHelper = new EntityMoveHelper(this);
			this.navigator = new PathNavigateGround(this, this.world);
			this.tasks.addTask(0, new EntityAIWaterNav(this));
		}
		if (pwl != null) { pwl.onEntityAdded(this); }
		this.taskCount = 1;
		this.addRegularEntries();
		this.doorInteractType();
		this.seekShelter();
		this.setResponse();
		this.setMoveType();
	}

	public void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		this.display.writeToNBT(compound);
		this.stats.writeToNBT(compound);
		this.ais.writeToNBT(compound);
		this.script.writeToNBT(compound);
		this.timers.writeToNBT(compound);
		this.advanced.writeToNBT(compound);
		this.inventory.writeEntityToNBT(compound);
		this.transform.writeToNBT(compound);
		this.animation.save(compound);
		compound.setLong("KilledTime", this.killedTime);
		compound.setLong("TotalTicksAlive", this.totalTicksAlive);
		compound.setInteger("ModRev", this.npcVersion);
		compound.setString("LinkedNpcName", this.linkedName);
		compound.setInteger("HomeDimensionId", this.homeDimensionId);
	}

	public NBTTagCompound writeSpawnData() {
		NBTTagCompound compound = new NBTTagCompound();
		this.display.writeToNBT(compound);
		this.advanced.writeToNBT(compound);
		this.animation.save(compound);
		compound.setInteger("NPCLevel", this.stats.getLevel());
		compound.setInteger("NPCRarity", this.stats.getRarity());
		compound.setString("NPCRarityTitle", this.stats.getRarityTitle());
		compound.setDouble("MaxHealth", this.stats.maxHealth);
		compound.setBoolean("DeadBody", this.stats.hideKilledBody);
		compound.setInteger("AggroRange", this.stats.aggroRange);
		compound.setTag("Armor", NBTTags.nbtIItemStackMap(this.inventory.armor));
		compound.setTag("Weapons", NBTTags.nbtIItemStackMap(this.inventory.weapons));
		compound.setInteger("Speed", this.ais.getWalkingSpeed());
		compound.setInteger("CurrentAnimation", this.currentAnimation);
		compound.setInteger("StandingState", this.ais.getStandingType());
		compound.setInteger("MovingState", this.ais.getMovingType());
		compound.setInteger("Orientation", this.ais.orientation);
		compound.setFloat("PositionXOffset", this.ais.bodyOffsetX);
		compound.setFloat("PositionYOffset", this.ais.bodyOffsetY);
		compound.setFloat("PositionZOffset", this.ais.bodyOffsetZ);
		if (this.advanced.jobInterface instanceof JobBard) {
			NBTTagCompound bard = compound.getCompoundTag("Bard");
			this.advanced.jobInterface.writeToNBT(bard);
			compound.setTag("Bard", bard);
		}
		if (this.advanced.jobInterface instanceof JobFollower) {
			NBTTagCompound follower = compound.getCompoundTag("Companion");
			this.advanced.jobInterface.writeToNBT(follower);
			compound.setTag("Companion", follower);
		}
		if (this instanceof EntityCustomNpc) {
			compound.setTag("ModelData", ((EntityCustomNpc) this).modelData.writeToNBT());
		}
		this.isKilled();
		compound.setBoolean("IsDead", this.dataManager.get(EntityNPCInterface.IsDead));
		compound.setInteger("DeathTime", this.deathTime);
		return compound;
	}

	public void writeSpawnData(ByteBuf buffer) {
		try {
			Server.writeNBT(buffer, this.writeSpawnData());
		} catch (IOException e) { LogWriter.error("Error:", e); }
	}

}
