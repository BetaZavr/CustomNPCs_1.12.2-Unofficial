package noppes.npcs.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
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
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.EventHooks;
import noppes.npcs.IChatMessages;
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
import noppes.npcs.ai.movement.EntityAITargetCannotBeReached;
import noppes.npcs.ai.movement.EntityAIWaterNav;
import noppes.npcs.ai.selector.NPCAttackSelector;
import noppes.npcs.ai.target.EntityAIClearTarget;
import noppes.npcs.ai.target.EntityAIClosestTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtByTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtTarget;
import noppes.npcs.ai.target.EntityAITargetController;
import noppes.npcs.ai.target.EntityAIWatchClosest;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.PotionEffectType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.model.part.ModelData;
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
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.GameProfileAlt;
import noppes.npcs.util.ObfuscationHelper;

public abstract class EntityNPCInterface extends EntityCreature
		implements IEntityAdditionalSpawnData, ICommandSender, IRangedAttackMob, IAnimals {

	public static FakePlayer ChatEventPlayer;
	public static FakePlayer CommandPlayer;
	public static FakePlayer GenericPlayer;
	public static GameProfileAlt ChatEventProfile = new GameProfileAlt();
	public static GameProfileAlt CommandProfile = new GameProfileAlt();
	public static GameProfileAlt GenericProfile = new GameProfileAlt();
	protected static DataParameter<Integer> Animation = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.VARINT);
	public static DataParameter<Boolean> Attacking = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Integer> FactionData = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.VARINT);
	private static DataParameter<Boolean> Interacting = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<Boolean> IsDead = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.BOOLEAN);
	private static DataParameter<String> JobData = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.STRING);
	private static DataParameter<String> RoleData = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.STRING);
	private static DataParameter<Boolean> Walking = EntityDataManager.createKey(EntityNPCInterface.class,
			DataSerializers.BOOLEAN);
	public DataAbilities abilities;
	public DataDisplay display;
	public DataStats stats;
	public DataAI ais;
	public DataInventory inventory;
	public DataAdvanced advanced;
	public DataScript script;
	public int animationStart, currentAnimation;
	public float baseHeight;
	public BossInfoServer bossInfo;
	public CombatHandler combatHandler;
	public int[] dialogs; // Changed
	public Faction faction;
	public double field_20061_w, field_20062_v, field_20063_u, field_20064_t, field_20065_s, field_20066_r;
	public boolean hasDied;
	public List<EntityLivingBase> interactingEntities;

	// AIs
	public EntityAICustom aiAttackTarget;
	public final EntityAITargetController aiTargetAnalysis;
	public EntityAIAnimation animateAi;
	public EntityAILook lookAi;
	public EntityNPCInterface aiOwnerNPC;
	public boolean aiIsSneak;

	// New
	public long killedtime;
	public int lastInteract, homeDimensionId;
	public LinkedNpcController.LinkedData linkedData;
	public long linkedLast;
	public String linkedName;
	public IChatMessages messages;
	public int npcVersion;
	public float scaleX;
	public float scaleY;
	public float scaleZ;
	private double startYPos;
	private int taskCount;
	public ResourceLocation textureCloakLocation;
	public ResourceLocation textureGlowLocation;
	public ResourceLocation textureLocation;
	public DataTimers timers;
	public long totalTicksAlive;
	public DataTransform transform;
	public boolean updateClient;
	private boolean wasKilled;
	public ICustomNpc<?> wrappedNPC;
	public boolean updateAI;
	public DataAnimation animation;
	public Path navigating;
	private long initTime;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityNPCInterface(World world) {
		super(world);
		this.homeDimensionId = world.provider.getDimension();
		this.combatHandler = new CombatHandler(this);
		this.linkedName = "";
		this.linkedLast = 0L;
		this.baseHeight = 1.8f;
		this.wasKilled = false;
		this.hasDied = false;
		this.killedtime = 0L;
		this.totalTicksAlive = 0L;
		this.taskCount = 1;
		this.lastInteract = 0;
		this.interactingEntities = new ArrayList<EntityLivingBase>();
		this.textureLocation = null;
		this.textureGlowLocation = null;
		this.textureCloakLocation = null;
		this.currentAnimation = 0;
		this.animationStart = 0;
		this.npcVersion = VersionCompatibility.ModRev;
		this.updateClient = false;
		this.bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
		this.startYPos = -1.0;
		this.wrappedNPC = new NPCWrapper(this);
		this.dialogs = new int[0];
		if (!CustomNpcs.DefaultInteractLine.isEmpty()) {
			this.advanced.interactLines.lines.put(0, new Line(CustomNpcs.DefaultInteractLine));
		}
		this.experienceValue = 0;
		float scaleX = 0.9375f;
		this.scaleZ = scaleX;
		this.scaleY = scaleX;
		this.scaleX = scaleX;
		this.faction = this.getFaction();
		this.setFaction(this.faction.id);
		this.setSize(1.0f, 1.0f);
		this.updateAI = true;
		this.bossInfo.setVisible(false);
		this.stepHeight = this.ais.stepheight;
		// New
		this.getNavigator();
		if (this.world.isRemote) {
			this.initTime = System.currentTimeMillis();
		}
		this.aiTargetAnalysis = new EntityAITargetController(this);
		if (world.isRemote) {
			CustomNpcs.proxy.checkTexture(this);
		}
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
		this.tasks.addTask(this.taskCount++, new EntityAIJob(this));
		this.tasks.addTask(this.taskCount++, new EntityAIRole(this));
		this.tasks.addTask(this.taskCount++, (this.animateAi = new EntityAIAnimation(this)));
		if (this.transform.isValid()) {
			this.tasks.addTask(this.taskCount++, new EntityAITransform(this));
		}
	}

	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	public void addVelocity(double d, double d1, double d2) {
		if (this.isWalking() && !this.isKilled()) {
			super.addVelocity(d, d1, d2);
		}
	}

	protected float applyArmorCalculations(DamageSource source, float damage) {
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
		this.animation = new DataAnimation(this);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.stats.maxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CustomNpcs.NpcNavRange);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getSpeed());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.stats.melee.getStrength());
		this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue((this.getSpeed() * 2.0f));
	}

	public boolean attackEntityAsMob(Entity par1Entity) {
		float f = this.stats.melee.getStrength();
		if (this.stats.melee.getDelay() < 10) {
			par1Entity.hurtResistantTime = 0;
		}
		if (par1Entity instanceof EntityLivingBase) {
			NpcEvent.MeleeAttackEvent event = new NpcEvent.MeleeAttackEvent(this.wrappedNPC,
					(EntityLivingBase) par1Entity, f);
			if (EventHooks.onNPCAttacksMelee(this, event)) {
				return false;
			}
			f = event.damage;
		}
		boolean var4 = par1Entity.attackEntityFrom((DamageSource) new NpcDamageSource("mob", this), f);
		if (var4) {
			if (this.getOwner() instanceof EntityPlayer) {
				EntityUtil.setRecentlyHit((EntityLivingBase) par1Entity);
			}
			if (this.stats.melee.getKnockback() > 0) {
				par1Entity.addVelocity(
						(-MathHelper.sin(this.rotationYaw * 3.1415927f / 180.0f) * this.stats.melee.getKnockback()
								* 0.5f),
						0.1, (MathHelper.cos(this.rotationYaw * 3.1415927f / 180.0f) * this.stats.melee.getKnockback()
								* 0.5f));
				this.motionX *= 0.6;
				this.motionZ *= 0.6;
			}
			if (this.advanced.roleInterface instanceof RoleCompanion) {
				((RoleCompanion) this.advanced.roleInterface).attackedEntity(par1Entity);
			}
		}
		if (this.stats.melee.getEffectType() != 0) {
			if (this.stats.melee.getEffectType() != 1) {
				((EntityLivingBase) par1Entity)
						.addPotionEffect(new PotionEffect(PotionEffectType.getMCType(this.stats.melee.getEffectType()),
								this.stats.melee.getEffectTime() * 20, this.stats.melee.getEffectStrength()));
			} else {
				par1Entity.setFire(this.stats.melee.getEffectTime());
			}
		}
		return var4;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float damage) {
		if (this.world.isRemote || CustomNpcs.FreezeNPCs || damagesource.damageType.equals("inWall")) {
			return false;
		}
		if (this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER && this.advanced.roleInterface.isFollowing()
				&& damagesource == DamageSource.FALL) {
			return false;
		}
		if (damagesource.damageType.equals("outOfWorld") && this.isKilled()) {
			this.reset();
		}
		damage = this.stats.resistances.applyResistance(damagesource, damage);
		if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0f && damage <= this.lastDamage) {
			return false;
		}
		Entity entity = NoppesUtilServer.GetDamageSourcee(damagesource);
		EntityLivingBase attackingEntity = null;
		if (entity instanceof EntityLivingBase) {
			attackingEntity = (EntityLivingBase) entity;
		}
		if (attackingEntity != null && attackingEntity == this.getOwner()) {
			return false;
		}
		if (attackingEntity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) attackingEntity;
			if (npc.faction.id == this.faction.id) {
				return false;
			}
			if (npc.getOwner() instanceof EntityPlayer) {
				this.recentlyHit = 100;
			}
		} else if (attackingEntity instanceof EntityPlayer
				&& this.faction.isFriendlyToPlayer((EntityPlayer) attackingEntity)) {
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
		if (damagesource.damageType.indexOf("inFire") != -1) {
			this.setFire(8);
		} // -> onFire

		this.aiTargetAnalysis.addDamageFromEntity(attackingEntity, damage);
		if (attackingEntity == null) {
			return super.attackEntityFrom(damagesource, damage);
		}
		try {
			if (!(attackingEntity instanceof EntityPlayer)
					|| !((EntityPlayer) attackingEntity).capabilities.disableDamage) {
				if (damage > 0.0f) {
					List<EntityNPCInterface> inRange = this.world.getEntitiesWithinAABB(EntityNPCInterface.class,
							this.getEntityBoundingBox().grow(32.0, 16.0, 32.0));
					for (EntityNPCInterface npc : inRange) {
						if (npc.equals(this)) {
							continue;
						}
						npc.advanced.tryDefendFaction(this.faction.id, this, attackingEntity);
					}
				}
				if (this.isAttacking()) {
					if (this.getAttackTarget() != null && attackingEntity != null
							&& this.getDistance(this.getAttackTarget()) > this.getDistance(attackingEntity)) {
						this.setAttackTarget(attackingEntity);
					}
					return super.attackEntityFrom(damagesource, damage);
				}
				if (damage > 0.0f) {
					this.setAttackTarget(attackingEntity);
				}
			}
			return super.attackEntityFrom(damagesource, damage);
		} finally {
			if (event.clearTarget) {
				this.setAttackTarget(null);
				this.setRevengeTarget(null);
			}
		}
	}

	public void attackEntityWithRangedAttack(EntityLivingBase entity, float distanceFactor) {
		ItemStack proj = ItemStackWrapper.MCItem(this.inventory.getProjectile());
		if (proj == null) {
			this.updateAI = true;
			return;
		}
		NpcEvent.RangedLaunchedEvent event = new NpcEvent.RangedLaunchedEvent(this.wrappedNPC, entity,
				this.stats.ranged.getStrength());
		for (int i = 0; i < this.stats.ranged.getShotCount(); ++i) {
			EntityProjectile projectile = this.shoot(entity, this.stats.ranged.getAccuracy(), proj,
					distanceFactor == 1.0f);
			projectile.damage = event.damage;
			ItemStack stack = entity.getHeldItemMainhand();
			projectile.callback = ((projectile_0, pos, entity1) -> {
				if (stack.getItem() == CustomRegisters.soulstoneFull) {
					Entity e = ItemSoulstoneFilled.Spawn(null, stack, this.world, pos);
					if (e instanceof EntityLivingBase && entity1 instanceof EntityLivingBase) {
						((EntityLivingBase) e).setRevengeTarget((EntityLivingBase) entity1);
					} else if (e instanceof EntityLiving && entity1 instanceof EntityLiving) {
						((EntityLiving) e).setRevengeTarget((EntityLiving) entity1);
					}
				}
				SoundEvent se = this.stats.ranged.getSoundEvent((entity1 != null) ? 1 : 2);
				String sound = this.stats.ranged.getSound((entity1 != null) ? 1 : 2);
				float pitch = 1.2f / (this.getRNG().nextFloat() * 0.2f + 0.9f);
				if (se != null) {
					projectile_0.playSound(se, 1.0f, pitch);
				} else if (!sound.isEmpty()) {
					BlockPos pos1 = new BlockPos(this.posX, this.posY, this.posZ);
					Server.sendRangedData(this.world, pos1, 64, EnumPacketClient.FORCE_PLAY_SOUND,
							SoundCategory.NEUTRAL.ordinal(), sound, pos1.getX(), pos1.getY(), pos1.getZ(), 1.0f, pitch);
				}
				return false;
			});
			SoundEvent se = this.stats.ranged.getSoundEvent(0);
			String sound = this.stats.ranged.getSound(0);
			if (se != null) {
				this.playSound(se, 2.0f, 1.0f);
			} else if (!sound.isEmpty()) {
				BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
				Server.sendRangedData(this.world, pos, 64, EnumPacketClient.FORCE_PLAY_SOUND,
						SoundCategory.NEUTRAL.ordinal(), sound, pos.getX(), pos.getY(), pos.getZ(), 2.0f, 1.0f);
			}
			event.projectiles.add((IProjectile<?>) NpcAPI.Instance().getIEntity(projectile));
		}
		EventHooks.onNPCRangedLaunched(this, event);
	}

	private double calculateStartYPos(BlockPos pos) {
		BlockPos startPos = this.ais.startPos();
		while (pos.getY() > 0) {
			IBlockState state = this.world.getBlockState(pos);
			AxisAlignedBB bb = state.getBoundingBox((IBlockAccess) this.world, pos).offset(pos);
			if (bb != null) {
				if (this.ais.movementType != 2 || startPos.getY() > pos.getY()
						|| state.getMaterial() != Material.WATER) {
					return bb.maxY;
				}
				pos = pos.down();
			} else {
				pos = pos.down();
			}
		}
		return 0.0;
	}

	private BlockPos calculateTopPos(BlockPos pos) {
		for (BlockPos check = pos; check.getY() > 0; check = check.down()) {
			IBlockState state = this.world.getBlockState(pos);
			AxisAlignedBB bb = state.getBoundingBox((IBlockAccess) this.world, pos).offset(pos);
			if (bb != null) {
				return check;
			}
		}
		return pos;
	}

	@SuppressWarnings("rawtypes")
	public boolean canAttackClass(Class par1Class) {
		return EntityBat.class != par1Class;
	}

	public boolean canBeCollidedWith() {
		return !this.isKilled() && this.display.getHasHitbox();
	}

	public boolean canBeLeashedTo(EntityPlayer player) {
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
			return AdditionalMethods.npcCanSeeTarget(this, (EntityLivingBase) entity, false, true);
		}
		return this.getEntitySenses().canSee(entity);
	}

	private void clearTasks(EntityAITasks tasks) {
		List<EntityAITasks.EntityAITaskEntry> list = new ArrayList<EntityAITasks.EntityAITaskEntry>(tasks.taskEntries);
		for (EntityAITasks.EntityAITaskEntry entityaitaskentry : list) {
			try {
				tasks.removeTask(entityaitaskentry.action);
			} catch (Throwable t) {
			}
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

	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
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
		this.dataManager.register(EntityNPCInterface.RoleData, String.valueOf(""));
		this.dataManager.register(EntityNPCInterface.JobData, String.valueOf(""));
		this.dataManager.register(EntityNPCInterface.FactionData, 0);
		this.dataManager.register(EntityNPCInterface.Animation, 0);
		this.dataManager.register(EntityNPCInterface.Walking, false);
		this.dataManager.register(EntityNPCInterface.Interacting, false);
		this.dataManager.register(EntityNPCInterface.IsDead, false);
		this.dataManager.register(EntityNPCInterface.Attacking, false);
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

	public Iterable<ItemStack> getArmorInventoryList() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for (int i = 0; i < 4; ++i) {
			list.add(ItemStackWrapper.MCItem(this.inventory.armor.get(3 - i)));
		}
		return list;
	}

	public float getBlockPathWeight(BlockPos pos) {
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
		if (this.world.isRemote) {
			return this;
		}
		EntityUtil.Copy(this, EntityNPCInterface.CommandPlayer);
		EntityNPCInterface.CommandPlayer.setWorld(this.world);
		EntityNPCInterface.CommandPlayer.setPosition(this.posX, this.posY, this.posZ);
		return EntityNPCInterface.CommandPlayer;
	}

	public EnumCreatureAttribute getCreatureAttribute() {
		return (this.stats == null) ? null : this.stats.creatureType;
	}

	public SoundEvent getDeathSound() {
		return null;
	}

	private Dialog getDialog(EntityPlayer player) {
		Set<Integer> newDS = Sets.<Integer>newHashSet();
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

	public World getEntityWorld() {
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
		if (this.world.isRemote) {
			return null;
		}
		EntityUtil.Copy(this, EntityNPCInterface.ChatEventPlayer);
		EntityNPCInterface.ChatEventProfile.npc = this;
		EntityNPCInterface.ChatEventPlayer.refreshDisplayName();
		EntityNPCInterface.ChatEventPlayer.setWorld(this.world);
		EntityNPCInterface.ChatEventPlayer.setPosition(this.posX, this.posY, this.posZ);
		return (EntityPlayerMP) EntityNPCInterface.ChatEventPlayer;
	}

	public Iterable<ItemStack> getHeldEquipment() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(ItemStackWrapper.MCItem(this.inventory.weapons.get(0)));
		list.add(ItemStackWrapper.MCItem(this.inventory.weapons.get(2)));
		return (Iterable<ItemStack>) list;
	}

	public ItemStack getHeldItemMainhand() {
		IItemStack item = null;
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

	public ItemStack getHeldItemOffhand() {
		IItemStack item = null;
		if (this.isAttacking()) {
			item = this.inventory.getLeftHand();
		} else if (this.advanced.jobInterface.overrideOffHand) {
			item = this.advanced.jobInterface.getOffhand();
		} else {
			item = this.inventory.getLeftHand();
		}
		return ItemStackWrapper.MCItem(item);
	}

	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot) {
		if (slot == EntityEquipmentSlot.MAINHAND) {
			return this.getHeldItemMainhand();
		}
		if (slot == EntityEquipmentSlot.OFFHAND) {
			return this.getHeldItemOffhand();
		}
		return ItemStackWrapper.MCItem(this.inventory.getArmor(3 - slot.getIndex()));
	}

	public String getJobData() {
		return (String) this.dataManager.get(EntityNPCInterface.RoleData);
	}

	public boolean getLeashed() {
		return false;
	}

	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public String getName() {
		if (display == null) {
			return "Display is null!";
		}
		return this.display.getName();
	}

	public EntityLivingBase getOwner() {
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

	public BlockPos getPosition() {
		return new BlockPos(this.posX, this.posY, this.posZ);
	}

	public Vec3d getPositionVector() {
		return new Vec3d(this.posX, this.posY, this.posZ);
	}

	public EnumPushReaction getPushReaction() {
		return this.display.getHasHitbox() ? super.getPushReaction() : EnumPushReaction.IGNORE;
	}

	public String getRoleData() {
		return (String) this.dataManager.get(EntityNPCInterface.RoleData);
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
		return this.ais.startPos().getX() + this.ais.bodyOffsetX / 10.0f;
	}

	public double getStartYPos() {
		if (this.startYPos < 0.0) {
			return this.calculateStartYPos(this.ais.startPos());
		}
		return this.startYPos;
	}

	public float getStartZPos() {
		return this.ais.startPos().getZ() + this.ais.bodyOffsetZ / 10.0f;
	}

	public int getTalkInterval() {
		return 160;
	}

	public void givePlayerItem(EntityPlayer player, ItemStack item) {
		if (this.world.isRemote) {
			return;
		}
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
			this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
					SoundCategory.PLAYERS, 0.2f,
					((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7f + 1.0f) * 2.0f);
			player.onItemPickup(entityitem, i);
			if (item.getCount() <= 0) {
				entityitem.setDead();
			}
		}
	}

	public boolean hasOwner() {
		return this.advanced.scenes.getOwner() != null
				|| (this.advanced.roleInterface instanceof RoleFollower
						&& ((RoleFollower) this.advanced.roleInterface).hasOwner())
				|| (this.advanced.roleInterface instanceof RoleCompanion
						&& ((RoleCompanion) this.advanced.roleInterface).hasOwner())
				|| (this.advanced.jobInterface instanceof JobFollower
						&& ((JobFollower) this.advanced.jobInterface).hasOwner());
	}

	public boolean isAttacking() {
		return (boolean) this.dataManager.get(EntityNPCInterface.Attacking);
	}

	public boolean isEntityAlive() {
		return super.isEntityAlive() && !this.isKilled();
	}

	public boolean isFollower() {
		return this.advanced.scenes.getOwner() != null || this.advanced.roleInterface.isFollowing()
				|| this.advanced.jobInterface.isFollowing();
	}

	public boolean isFrend(Entity entityTarget) {
		if (!(entityTarget instanceof EntityNPCInterface)) {
			return false;
		}
		EntityNPCInterface npcTarget = (EntityNPCInterface) entityTarget;
		if (this.faction.id == npcTarget.faction.id || npcTarget.faction.frendFactions.contains(this.faction.id)
				|| npcTarget.advanced.frendFactions.contains(this.faction.id)
				|| this.faction.frendFactions.contains(npcTarget.faction.id)
				|| this.advanced.frendFactions.contains(npcTarget.faction.id)) {
			return true;
		}
		return false;
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
				|| (this.isRemote() && (boolean) this.dataManager.get(EntityNPCInterface.Interacting))
				|| (this.ais.stopAndInteract && !this.interactingEntities.isEmpty()
						&& this.ticksExisted - this.lastInteract < 180);
	}

	public boolean isInvisible() {
		return this.display.getVisible() == 1;
	}

	public boolean isInvisibleToPlayer(EntityPlayer player) {
		return this.isInvisible() && player.getHeldItemMainhand().getItem() != CustomRegisters.wand
				&& this.display.getAvailability().isAvailable(player);
	}

	public boolean isKilled() {
		return this.isDead || (boolean) this.dataManager.get(EntityNPCInterface.IsDead);
	}

	public boolean isMoving() {
		return this.motionX != 0.0d || this.motionZ != 0.0d || this.motionY <= -0.085d || this.motionY > 0.0d;
	}

	public boolean isOnSameTeam(Entity entity) {
		if (!this.isRemote()) {
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
		return this.getHealth() <= 0.0f || this.currentAnimation == 2 && !this.isAttacking()
				&& this.getAttackingEntity() == null && this.navigating == null;
	}

	public boolean isPotionApplicable(PotionEffect effect) {
		return !this.stats.potionImmune && (this.getCreatureAttribute() != EnumCreatureAttribute.ARTHROPOD
				|| effect.getPotion() != MobEffects.POISON) && super.isPotionApplicable(effect);
	}

	public boolean isPushedByWater() {
		return this.ais.movementType != 2;
	}

	public boolean isRemote() {
		return this.world == null || this.world.isRemote;
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
		return this.ais.getMovingType() != 0 || this.isAttacking() || this.isFollower()
				|| (boolean) this.dataManager.get(EntityNPCInterface.Walking);
	}

	public void knockBack(Entity par1Entity, float strength, double ratioX, double ratioZ) {
		super.knockBack(par1Entity, strength * (2.0f - this.stats.resistances.knockback), ratioX, ratioZ);
	}

	public boolean nearPosition(BlockPos pos) {
		BlockPos npcpos = this.getPosition();
		float x = (npcpos.getX() - pos.getX());
		float z = (npcpos.getZ() - pos.getZ());
		float y = (npcpos.getY() - pos.getY());
		float height = (MathHelper.ceil(this.height + 1.0f) * MathHelper.ceil(this.height + 1.0f));
		return x * x + z * z < 2.5 && y * y < height + 2.5;
	}

	public void onAttack(EntityLivingBase entity) {
		if (entity == null || entity == this || this.isAttacking() || this.ais.onAttack == 3
				|| entity == this.getOwner()) {
			return;
		}
		super.setAttackTarget(entity);
	}

	public void onCollide() {
		if (!this.isEntityAlive() || this.ticksExisted % 4 != 0 || this.world.isRemote) {
			return;
		}
		AxisAlignedBB axisalignedbb = null;
		if (this.getRidingEntity() != null && this.getRidingEntity().isEntityAlive()) {
			axisalignedbb = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).grow(1.0,
					0.0, 1.0);
		} else {
			axisalignedbb = this.getEntityBoundingBox().grow(1.0, 0.5, 1.0);
		}
		List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = list.get(i);
			if (entity != this && entity.isEntityAlive()) {
				EventHooks.onNPCCollide(this, entity);
			}
		}
	}

	public void onDeath(DamageSource damagesource) {
		this.setSprinting(false);
		this.getNavigator().clearPath();
		this.extinguish();
		this.clearActivePotions();
		Entity attackingEntity = NoppesUtilServer.GetDamageSourcee(damagesource);
		if (this.advanced.roleInterface != null) {
			this.advanced.roleInterface.aiDeathExecute(attackingEntity);
		}
		if (this.advanced.jobInterface != null) {
			this.advanced.jobInterface.aiDeathExecute(attackingEntity);
		}
		if (!this.isRemote()) {
			this.advanced.playSound(3, this.getSoundVolume(), this.getSoundPitch());
			NpcEvent.DiedEvent event = new NpcEvent.DiedEvent(this.wrappedNPC, damagesource, attackingEntity);
			event.droppedItems = this.inventory.getItemsRNG(
					(attackingEntity instanceof EntityLivingBase) ? (EntityLivingBase) attackingEntity : null);
			event.lootedItems = this.inventory.getItemsRNGL(
					(attackingEntity instanceof EntityLivingBase) ? (EntityLivingBase) attackingEntity : null);
			event.expDropped = this.inventory.getExpRNG();
			event.line = this.advanced.getKilledLine();
			if (this.advanced.roleInterface instanceof RoleFollower
					&& !((RoleFollower) this.advanced.roleInterface).inventory.isEmpty()) {
				for (ItemStack stack : ((RoleFollower) this.advanced.roleInterface).inventory.items) {
					if (NoppesUtilServer.IsItemStackNull(stack) || stack.isEmpty()) {
						continue;
					}
					this.entityDropItem(stack, 0.0f);
				}
				((RoleFollower) this.advanced.roleInterface).inventory.clear();
			}
			EventHooks.onNPCDied(this, event);
			this.bossInfo.setVisible(false);
			this.inventory.dropStuff(event, attackingEntity, damagesource);
			if (event.line != null) {
				this.saySurrounding(Line.formatTarget((Line) event.line,
						(attackingEntity instanceof EntityLivingBase) ? (EntityLivingBase) attackingEntity : null));
			}
		}
		if (this.animation.getActiveAnimation(AnimationKind.DIES) != null) {
			this.motionX = 0.0d;
			this.motionY = 0.0d;
			this.motionZ = 0.0d;
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
		if (this.killedtime < System.currentTimeMillis()
				&& (this.stats.spawnCycle == 0 || (this.world.isDaytime() && this.stats.spawnCycle == 1)
						|| (!this.world.isDaytime() && this.stats.spawnCycle == 2))) {
			this.reset();
		}
	}

	public void onLivingUpdate() {
		if (CustomNpcs.FreezeNPCs) {
			return;
		}
		CustomNpcs.debugData.startDebug(!this.world.isRemote ? "Server" : "Client", this, "NPCLivingUpdate");
		if (this.isAIDisabled()) {
			super.onLivingUpdate();
			CustomNpcs.debugData.endDebug(!this.world.isRemote ? "Server" : "Client", this, "NPCLivingUpdate");
			return;
		}
		++this.totalTicksAlive;
		this.updateArmSwingProgress();
		if (this.totalTicksAlive % 20 == 0) {
			this.faction = this.getFaction();
		}
		if (!this.world.isRemote) {
			if (this.aiAttackTarget != null) {
				this.aiAttackTarget.update();
			}
			if (!this.isKilled() && this.totalTicksAlive % 20 == 0) {
				this.advanced.scenes.update();
				if (this.getHealth() < this.getMaxHealth()) {
					if (this.stats.healthRegen > 0 && !this.isAttacking()) {
						this.heal(this.stats.healthRegen);
					}
					if (this.stats.combatRegen > 0 && this.isAttacking()) {
						this.heal(this.stats.combatRegen);
					}
				}
				if (this.faction.getsAttacked && !this.isAttacking()) {
					List<EntityMob> list = this.world.getEntitiesWithinAABB(EntityMob.class,
							this.getEntityBoundingBox().grow(16.0, 16.0, 16.0));
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
		if (this.wasKilled != this.isKilled() && this.wasKilled) {
			this.reset();
		}
		if (this.world.isDaytime() && !this.world.isRemote && this.stats.burnInSun) {
			float f = this.getBrightness();
			if (f > 0.5f && this.rand.nextFloat() * 30.0f < (f - 0.4f) * 2.0f
					&& this.world.canBlockSeeSky(new BlockPos(this))) {
				this.setFire(8);
			}
		}
		super.onLivingUpdate();
		if (this.world.isRemote) {
			this.advanced.roleInterface.clientUpdate();
			if (this.textureCloakLocation != null) {
				this.cloakUpdate();
			}
			if (this.currentAnimation != this.dataManager.get(EntityNPCInterface.Animation)) {
				this.currentAnimation = this.dataManager.get(EntityNPCInterface.Animation);
				this.animationStart = this.ticksExisted;
				this.updateHitbox();
			}
			if (this.advanced.jobInterface instanceof JobBard) {
				((JobBard) this.advanced.jobInterface).onLivingUpdate();
			}
		}
		if (this.display.getBossbar() > 0) {
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
		}
		CustomNpcs.debugData.endDebug(!this.world.isRemote ? "Server" : "Client", this, "NPCLivingUpdate");
	}

	public void onUpdate() {
		CustomNpcs.debugData.startDebug(!this.world.isRemote ? "Server" : "Client", this, "NPCUpdate");
		super.onUpdate();
		if (this.ticksExisted % 10 == 0) {
			if (this.initTime != 0L && this.world.isRemote && this.initTime < System.currentTimeMillis() - 1000L) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.NpcData, this.getEntityId());
				this.initTime = 0L;
			}
			if (this.isKilled()) {
				this.aiTargetAnalysis.map.clear();
			} else {
				if (this.width <= 1.0E-5f) {
					this.updateHitbox();
				}
			}
			// Path change
			Path path = this.getNavigator().getPath();
			if (this.isServerWorld()) {
				if (path != null) {
					if (path != this.navigating) {
						this.navigating = path;
						this.updateNavClient();
					}
				} else if (this.navigating != null) {
					this.navigating = null;
					this.updateNavClient();
				}
			}
			if (this.ais.onAttack == 1) { // Panic
				if ((this.isBurning() || this.getAttackTarget() != null)
						&& (this.getNavigator().noPath() || !this.isMoving())) {
					Vec3d vec = RandomPositionGenerator.findRandomTarget(this, 5, 4);
					if (vec != null) {
						this.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.3d);
					}
				}
			}
			this.startYPos = this.calculateStartYPos(this.ais.startPos()) + 1.0;
			if ((this.startYPos < 0.0 || this.startYPos > 255.0) && !this.isRemote()) {
				this.setDead();
			}
			EventHooks.onNPCTick(this);
		}
		if (this.deathTime > 0 || (this.getAttackTarget() instanceof EntityPlayer
				&& ((EntityPlayer) this.getAttackTarget()).capabilities.disableDamage)) {
			super.setAttackTarget(null);
		}
		this.timers.update();
		if (this.world.isRemote && this.wasKilled != this.isKilled()) {
			this.deathTime = 0;
			this.updateHitbox();
		}
		this.wasKilled = this.isKilled();
		if (this.currentAnimation == 14) {
			this.deathTime = 19;
		}
		CustomNpcs.debugData.endDebug(!this.world.isRemote ? "Server" : "Client", this, "NPCUpdate");
	}

	protected void playHurtSound(DamageSource source) {
		this.advanced.playSound(2, this.getSoundVolume(), this.getSoundPitch());
	}

	public void playLivingSound() {
		if (!this.isEntityAlive()) {
			return;
		}
		this.advanced.playSound((this.getAttackTarget() != null) ? 1 : 0, this.getSoundVolume(), this.getSoundPitch());
	}

	protected void playStepSound(BlockPos pos, Block block) {
		if (this.advanced.getSound(4) != null) {
			this.advanced.playSound(4, 0.15f, 1.0f);
		} else {
			super.playStepSound(pos, block);
		}
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.world.isRemote) {
			return !this.isAttacking();
		}
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		ItemStack stack = player.getHeldItem(hand);
		if (stack != null) {
			Item item = stack.getItem();
			if (item == CustomRegisters.cloner || item == CustomRegisters.wand || item == CustomRegisters.mount
					|| item == CustomRegisters.scripter) {
				this.setAttackTarget(null);
				this.setRevengeTarget(null);
				return true;
			}
			if (item == CustomRegisters.moving) {
				this.setAttackTarget(null);
				stack.setTagInfo("NPCID", new NBTTagInt(this.getEntityId()));
				player.sendMessage(
						new TextComponentTranslation("message.pather.reg", this.getName(), stack.getDisplayName()));
				Server.sendData((EntityPlayerMP) player, EnumPacketClient.NPC_MOVINGPATH, this.getEntityId(),
						this.ais.writeToNBT(new NBTTagCompound()));
				return true;
			}
		}
		if (EventHooks.onNPCInteract(this, player)) {
			return false;
		}
		if (this.getFaction().isAggressiveToPlayer(player)) {
			return !this.isAttacking();
		}
		this.addInteract(player);
		Dialog dialog = this.getDialog(player);
		PlayerData pd = PlayerData.get(player);
		if (!this.faction.getIsHidden() && !pd.factionData.factionData.containsKey(this.faction.id)) {
			PlayerEvent.FactionUpdateEvent event = new PlayerEvent.FactionUpdateEvent(
					(PlayerWrapper<?>) NpcAPI.Instance().getIEntity(player), this.faction, this.faction.defaultPoints,
					true);
			EventHooks.OnPlayerFactionChange(PlayerData.get(player).scriptData, event);
			pd.factionData.factionData.put(this.faction.id, event.points);
		}
		QuestData data = pd.questData.getQuestCompletion(player, this);
		if (data != null) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.QUEST_COMPLETION, data.quest.id);
		} else if (dialog != null) {
			NoppesUtilServer.openDialog(player, this, dialog);
		} else if (this.advanced.roleInterface.getType() > 0) {
			this.advanced.roleInterface.interact(player);
		} else {
			this.say(player, this.advanced.getInteractLine());
		}
		return true;
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
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
		this.killedtime = compound.getLong("KilledTime");
		this.totalTicksAlive = compound.getLong("TotalTicksAlive");
		this.linkedName = compound.getString("LinkedNpcName");
		if (!this.isRemote()) {
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
		} catch (IOException ex) {
		}
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
		if (follow_range != null) {
			follow_range.setBaseValue(this.stats.aggroRange);
		}
		this.ais.setWalkingSpeed(compound.getInteger("Speed"));
		this.ais.setStandingType(compound.getInteger("StandingState"));
		this.ais.setMovingType(compound.getInteger("MovingState"));
		this.ais.orientation = compound.getInteger("Orientation");
		this.ais.bodyOffsetX = compound.getFloat("PositionXOffset");
		this.ais.bodyOffsetY = compound.getFloat("PositionYOffset");
		this.ais.bodyOffsetZ = compound.getFloat("PositionZOffset");
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

	public void removeTrackingPlayer(EntityPlayerMP player) {
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
		this.aiTargetAnalysis.map.clear();
		if (this.ais.returnToStart && (this.advanced.jobInterface instanceof JobFollower || !this.hasOwner())
				&& !this.isRemote() && !this.isRiding()) {
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
								y = (double) pos.getY();
							}
							break;
						}
					}
				}
			}
			this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		}
		this.killedtime = 0L;
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
			this.getOwner().setLastAttackedEntity(null);
		}
		this.bossInfo.setVisible(this.display.getBossbar() == 1);
		this.advanced.jobInterface.reset();
		this.animation.reset();
		if (this.isRemote()) {
			this.animation.startAnimation(AnimationKind.INIT.get());
		}
		this.updateClient = true;
		if (this.ais.returnToStart && this.homeDimensionId != this.world.provider.getDimension()
				&& !(this.advanced.roleInterface.getEnumType() == RoleType.FOLLOWER
						&& this.advanced.roleInterface.isFollowing())) {
			try {
				AdditionalMethods.teleportEntity(this.world.getMinecraftServer(), this, homeDimensionId, this.posX,
						this.posY, this.posZ);
			} catch (CommandException e) {
				e.printStackTrace();
			}
		}
		this.stepHeight = this.ais.stepheight;
		EventHooks.onNPCInit(this);
	}

	public void reset(int delay) {
		CustomNPCsScheduler.runTack(() -> {
			this.reset();
		}, delay);
	}

	public void say(EntityPlayer player, Line line) {
		if (line == null || !this.getEntitySenses().canSee(player)) {
			return;
		}
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
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHATBUBBLE, this.getEntityId(), line.getText(),
					line.getShowText());
		}
	}

	public void saySurrounding(Line line) {
		if (line == null) {
			return;
		}
		if (line.getShowText() && !line.getText().isEmpty()) {
			ServerChatEvent event = new ServerChatEvent(this.getFakeChatPlayer(), line.getText(),
					new TextComponentTranslation(line.getText().replace("%", "%%"), new Object[0]));
			if (CustomNpcs.NpcSpeachTriggersChatEvent
					&& (MinecraftForge.EVENT_BUS.post((Event) event) || event.getComponent() == null)) {
				return;
			}
			line.setText(event.getComponent().getUnformattedText().replace("%%", "%"));
		}
		List<EntityPlayer> inRange = this.world.getEntitiesWithinAABB(EntityPlayer.class,
				this.getEntityBoundingBox().grow(20.0, 20.0, 20.0));
		for (EntityPlayer player : inRange) {
			this.say(player, line);
		}
	}

	public void seekShelter() {
		if (this.ais.findShelter == 0) {
			this.tasks.addTask(this.taskCount++, new EntityAIMoveIndoors(this));
		} else if (this.ais.findShelter == 1) {
			if (!this.canFly()) {
				this.tasks.addTask(this.taskCount++, new EntityAIRestrictSun((EntityCreature) this));
			}
			this.tasks.addTask(this.taskCount++, new EntityAIFindShade(this));
		}
	}

	public void sendMessage(ITextComponent var1) {
	}

	public void setAttackTarget(EntityLivingBase entityTarget) {
		if (this.getAttackTarget() == entityTarget) {
			return;
		}
		// Next sets
		if (entityTarget instanceof EntityPlayer && ((EntityPlayer) entityTarget).capabilities.disableDamage) {
			return;
		}
		if (entityTarget != null && entityTarget == this.getOwner()) {
			return;
		}
		if (entityTarget instanceof EntityNPCInterface && this.isFrend((EntityNPCInterface) entityTarget)) {
			return;
		}
		if (entityTarget != null) {
			NpcEvent.TargetEvent event = new NpcEvent.TargetEvent(this.wrappedNPC, entityTarget);
			if (EventHooks.onNPCTarget(this, event)) {
				return;
			}
			if (event.entity == null) {
				entityTarget = null;
			} else {
				entityTarget = event.entity.getMCEntity();
			}
		} else {
			for (EntityAITasks.EntityAITaskEntry en : this.targetTasks.taskEntries) {
				if (en.using) {
					en.using = false;
					en.action.resetTask();
				}
			}
			if (EventHooks.onNPCTargetLost(this, this.getAttackTarget())) {
				return;
			}
		}
		if (entityTarget != null && entityTarget != this && this.ais.onAttack != 3 && !this.isAttacking()
				&& !this.isRemote()) {
			Line line = this.advanced.getAttackLine();
			if (line != null) {
				this.saySurrounding(Line.formatTarget(line, entityTarget));
			}
		}
		if (entityTarget == null) {
			this.aiTargetAnalysis.map.clear();
		}
		super.setAttackTarget(entityTarget);
		this.updateTargetClient();
	}

	public void setCurrentAnimation(int animation) {
		this.currentAnimation = animation;
		this.dataManager.set(EntityNPCInterface.Animation, animation);
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
		if (this.world.isRemote || this.stats.spawnCycle == 3 || this.stats.spawnCycle == 4) {
			this.delete();
		} else {
			this.setHealth(-1.0f);
			this.setSprinting(false);
			this.getNavigator().clearPath();
			this.setCurrentAnimation(2);
			this.updateHitbox();
			if (this.killedtime <= 0L) {
				this.killedtime = this.stats.respawnTime * 1000 + System.currentTimeMillis();
			}
			this.advanced.roleInterface.killed();
			this.advanced.jobInterface.killed();
		}
	}

	public void setFaction(int id) {
		if (id < 0 || this.isRemote()) {
			return;
		}
		this.dataManager.set(EntityNPCInterface.FactionData, id);
	}

	public void setHomePosAndDistance(BlockPos pos, int range) {
		super.setHomePosAndDistance(pos, range);
		this.ais.setStartPos(pos);
		this.updateClient = true;
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

	public void setItemStackToSlot(EntityEquipmentSlot slot, ItemStack item) {
		if (slot == EntityEquipmentSlot.MAINHAND) {
			this.inventory.weapons.put(0, NpcAPI.Instance().getIItemStack(item));
		} else if (slot == EntityEquipmentSlot.OFFHAND) {
			this.inventory.weapons.put(2, NpcAPI.Instance().getIItemStack(item));
		} else {
			this.inventory.armor.put(3 - slot.getIndex(), NpcAPI.Instance().getIItemStack(item));
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

	public void setPortal(BlockPos pos) {
	}

	private void setResponse() {
		this.aiAttackTarget = null;
		this.aiIsSneak = false;
		this.aiOwnerNPC = null;
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
				this.tasks.addTask(this.taskCount++,
						(this.aiAttackTarget = new EntityAIOnslaught((IRangedAttackMob) this)));
				break;
			}
			case 1: {
				this.tasks.addTask(this.taskCount++,
						(this.aiAttackTarget = new EntityAIDodge((IRangedAttackMob) this)));
				break;
			}
			case 2: {
				this.tasks.addTask(this.taskCount++,
						(this.aiAttackTarget = new EntityAISurround((IRangedAttackMob) this)));
				break;
			}
			case 3: {
				this.tasks.addTask(this.taskCount++,
						(this.aiAttackTarget = new EntityAIHitAndRun((IRangedAttackMob) this)));
				break;
			}
			case 4: {
				this.tasks.addTask(this.taskCount++,
						(EntityAIBase) (this.aiAttackTarget = new EntityAICommanderTarget((IRangedAttackMob) this)));
				break;
			}
			case 5: {
				this.tasks.addTask(this.taskCount++,
						(EntityAIBase) (this.aiAttackTarget = new EntityAIStalkTarget((IRangedAttackMob) this)));
				break;
			}
			default: {
				this.tasks.addTask(this.taskCount++,
						(this.aiAttackTarget = new EntityAINoTactic((IRangedAttackMob) this)));
				break;
			}
			}
			this.tasks.addTask(this.taskCount++, new EntityAITargetCannotBeReached(this));
		}
		this.aiTargetAnalysis.map.clear();
		this.tasks.addTask(this.taskCount++, this.aiTargetAnalysis); // Reset Target from Damage
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
		float angle = projectile.getAngleForXYZ(varX, varY, varZ, varF, indirect);
		float acc = 20.0f - MathHelper.floor(accuracy / 5.0f);
		projectile.shoot(varX, varY, varZ, angle, acc);
		this.world.spawnEntity(projectile);
		return projectile;
	}

	public EntityProjectile shoot(EntityLivingBase entity, int accuracy, ItemStack proj, boolean indirect) {
		return this.shoot(entity.posX, entity.getEntityBoundingBox().minY + entity.height / 2.0f, entity.posZ, accuracy,
				proj, indirect);
	}

	public boolean shouldDismountInWater(Entity rider) {
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
		if (this.advanced.roleInterface instanceof RoleCompanion && !this.isRemote()) {
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
		this.aiAttackTarget.writeToClientNBT(compound);
		if (compound.getKeySet().size() > 1) {
			Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_AI_TARGET, compound);
		}
	}

	public void updateAnimationClient() {
		if (!this.isServerWorld()) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", this.getEntityId());
		compound.setInteger("baseanim", this.currentAnimation);
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_ANIMATION, 4, compound);
	}

	public void updateClient() {
		NBTTagCompound compound = this.writeSpawnData();
		compound.setInteger("EntityId", this.getEntityId());
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC, compound);
		this.updateClient = false;
		this.updateNavClient();
	}

	public void updateHitbox() {
		if (this.currentAnimation == 2 || this.currentAnimation == 7 || this.deathTime > 0) {
			this.width = 0.8f;
			this.height = 0.4f;
		} else if (this.isRiding()) {
			this.width = 0.6f;
			this.height = this.baseHeight * 0.77f;
		} else {
			this.width = 0.6f;
			this.height = this.baseHeight;
		}
		if (this.display.getModel() == null && this instanceof EntityCustomNpc) {
			ModelData modeldata = ((EntityCustomNpc) this).modelData;
			ModelPartConfig model = modeldata.getPartConfig(EnumParts.HEAD);
			float scaleHead = model.scaleBase[0] > model.scaleBase[2] ? model.scaleBase[0] : model.scaleBase[2];
			model = modeldata.getPartConfig(EnumParts.BODY);
			float scaleBody = model.scaleBase[0] > model.scaleBase[2] ? model.scaleBase[0] : model.scaleBase[2];
			this.width *= scaleHead > scaleBody ? scaleHead : scaleBody;
			this.width = this.width / 5.0f * this.display.getSize();
			this.height = this.height / 5.0f * this.display.getSize();
		}
		if (!this.display.getHasHitbox() || (this.isKilled() && this.stats.hideKilledBody)) {
			this.width = 1.0E-5f;
		}
		double n = this.width / 2.0f;
		if (n > World.MAX_ENTITY_RADIUS) {
			World.MAX_ENTITY_RADIUS = n;
		}
		if (this.getHealth() == 0) {
			return;
		}
		this.setPosition(this.posX, this.posY, this.posZ);
	}

	public void updateNavClient() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", this.getEntityId());
		compound.setBoolean("IsNavigating", this.navigating != null);
		if (this.navigating != null) {
			compound.setTag("Navigating", Server.writePathToNBT(this.navigating));
		}
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_NAVIGATION, compound);
	}

	public void updateTargetClient() {
		if (!this.isServerWorld()) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", this.getEntityId());
		if (this.getAttackTarget() != null) {
			compound.setInteger("target", this.getAttackTarget().getEntityId());
		}
		Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC_TARGET, compound);
	}

	private void updateTasks() {
		if (this.world == null || this.world.isRemote) {
			return;
		}
		this.clearTasks(this.tasks);
		this.clearTasks(this.targetTasks);
		if (this.isKilled()) {
			return;
		}
		Predicate<EntityLivingBase> attackEntitySelector = new NPCAttackSelector(this);
		this.targetTasks.addTask(0, new EntityAIClearTarget(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) this, false, new Class[0]));
		this.targetTasks.addTask(2, new EntityAIClosestTarget(this, EntityLivingBase.class, 4, this.ais.directLOS,
				false, attackEntitySelector));
		this.targetTasks.addTask(3, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(4, new EntityAIOwnerHurtTarget(this));
		PathWorldListener pwl = ObfuscationHelper.getValue(World.class, this.world, 23);
		pwl.onEntityRemoved(this);
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
		/*
		 * PathFinder pathFinder = ObfuscationHelper.getValue(PathNavigate.class,
		 * this.navigator, PathFinder.class); NodeProcessor nodeProcessor =
		 * ObfuscationHelper.getValue(PathFinder.class, pathFinder,
		 * NodeProcessor.class); if (nodeProcessor instanceof FlyingNodeProcessor) {
		 * ObfuscationHelper.setValue(PathFinder.class, pathFinder, new
		 * CustomFlyingNodeProcessor(this), NodeProcessor.class); } else if
		 * (nodeProcessor instanceof SwimNodeProcessor) {
		 * ObfuscationHelper.setValue(PathFinder.class, pathFinder, new
		 * CustomSwimNodeProcessor(this), NodeProcessor.class); } else if (nodeProcessor
		 * instanceof WalkNodeProcessor) { ObfuscationHelper.setValue(PathFinder.class,
		 * pathFinder, new CustomWalkNodeProcessor(this), NodeProcessor.class); }
		 */
		pwl.onEntityAdded(this);
		this.taskCount = 1;
		this.addRegularEntries();
		this.doorInteractType();
		this.seekShelter();
		this.setResponse();
		this.setMoveType();
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
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
		compound.setLong("KilledTime", this.killedtime);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
