package noppes.npcs.roles;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.role.IRoleCompanion;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.companion.CompanionFarmer;
import noppes.npcs.roles.companion.CompanionFoodStats;
import noppes.npcs.roles.companion.CompanionGuard;
import noppes.npcs.roles.companion.CompanionJobInterface;
import noppes.npcs.roles.companion.CompanionTrader;

public class RoleCompanion extends RoleInterface implements IRoleCompanion {

	public boolean canAge, defendOwner, hasInv;
	public int companionID, currentExp, eatingDelay, eatingTicks;
	private IItemStack eating;
	public CompanionFoodStats foodstats;
	public NpcMiscInventory inventory;
	public EnumCompanionJobs job;
	public CompanionJobInterface jobInterface;
	public EntityPlayer owner;
	public EnumCompanionStage stage;
	public Map<EnumCompanionTalent, Integer> talents;
	public long ticksActive;
	public String ownerName, uuid;

	public RoleCompanion(EntityNPCInterface npc) {
		super(npc);
		this.uuid = "";
		this.ownerName = "";
		this.talents = new TreeMap<>();
		this.canAge = true;
		this.ticksActive = 0L;
		this.stage = EnumCompanionStage.FULL_GROWN;
		this.owner = null;
		this.job = EnumCompanionJobs.NONE;
		this.jobInterface = null;
		this.hasInv = true;
		this.defendOwner = true;
		this.foodstats = new CompanionFoodStats();
		this.eatingTicks = 20;
		this.eating = null;
		this.eatingDelay = 0;
		this.currentExp = 0;
		this.inventory = new NpcMiscInventory(12);
		this.type = RoleType.COMPANION;
	}

	public void addExp(int exp) {
		if (this.canAddExp(exp)) {
			this.currentExp += exp;
		}
	}

	public void addMovementStat(double x, double y, double z) {
		int i = Math.round(MathHelper.sqrt(x * x + y * y + z * z) * 100.0f);
		if (this.npc.isAttacking()) {
			this.foodstats.addExhaustion(0.04f * i * 0.01f);
		} else {
			this.foodstats.addExhaustion(0.02f * i * 0.01f);
		}
	}

	public void addTalentExp(EnumCompanionTalent talent, int exp) {
		if (this.talents.containsKey(talent)) {
			exp += this.talents.get(talent);
		}
		this.talents.put(talent, exp);
	}

	@Override
	public boolean aiShouldExecute() {
		EntityPlayer prev = this.owner;
		this.owner = this.getOwner();
		if (this.jobInterface != null && this.jobInterface.isSelfSufficient()) {
			return true;
		}
		if (this.owner == null && !this.uuid.isEmpty()) {
			this.npc.isDead = true;
		} else if (prev != this.owner && this.owner != null) {
			this.ownerName = this.owner.getDisplayNameString();
			PlayerData data = PlayerData.get(this.owner);
			if (data.companionID != this.companionID) {
				this.npc.isDead = true;
			}
		}
		return this.owner != null;
	}

	@Override
	public void aiUpdateTask() {
		if (this.owner != null && (this.jobInterface == null || !this.jobInterface.isSelfSufficient())) {
			this.foodstats.onUpdate(this.npc);
		}
		if (this.foodstats.getFoodLevel() >= 18) {
			this.npc.stats.healthRegen = 0;
			this.npc.stats.combatRegen = 0;
		}
		if (this.foodstats.needFood() && this.isSitting()) {
			if (this.eatingDelay > 0) {
				--this.eatingDelay;
				return;
			}
			IItemStack prev = this.eating;
			this.eating = this.getFood();
			if (prev != null && this.eating == null) {
				this.npc.setRoleData("");
			}
			if (prev == null && this.eating != null) {
				this.npc.setRoleData("eating");
				this.eatingTicks = 20;
			}
			if (this.isEating()) {
				this.doEating();
			}
		} else if (this.eating != null && !this.isSitting()) {
			this.eating = null;
			this.eatingDelay = 20;
			this.npc.setRoleData("");
		}
		++this.ticksActive;
		if (this.canAge && this.stage != EnumCompanionStage.FULL_GROWN) {
			if (this.stage == EnumCompanionStage.BABY && this.ticksActive > EnumCompanionStage.CHILD.matureAge) {
				this.matureTo(EnumCompanionStage.CHILD);
			} else if (this.stage == EnumCompanionStage.CHILD && this.ticksActive > EnumCompanionStage.TEEN.matureAge) {
				this.matureTo(EnumCompanionStage.TEEN);
			} else if (this.stage == EnumCompanionStage.TEEN && this.ticksActive > EnumCompanionStage.ADULT.matureAge) {
				this.matureTo(EnumCompanionStage.ADULT);
			} else if (this.stage == EnumCompanionStage.ADULT
					&& this.ticksActive > EnumCompanionStage.FULL_GROWN.matureAge) {
				this.matureTo(EnumCompanionStage.FULL_GROWN);
			}
		}
	}

	public float applyArmorCalculations(DamageSource source, float damage) {
		if (!this.hasInv || this.getTalentLevel(EnumCompanionTalent.ARMOR) <= 0) {
			return damage;
		}
		if (!source.isUnblockable()) {
			this.damageArmor(damage);
			int i = 25 - this.getTotalArmorValue();
			float f1 = damage * i;
			damage = f1 / 25.0f;
		}
		return damage;
	}

	public void attackedEntity() {
		IItemStack weapon = this.npc.inventory.getRightHand();
		this.gainExp((weapon == null) ? 8 : 4);
		if (weapon == null) {
			return;
		}
		weapon.getMCItemStack().damageItem(1, this.npc);
		if (weapon.getMCItemStack().getCount() <= 0) {
			this.npc.inventory.setRightHand(null);
		}
	}

	public boolean canAddExp(int exp) {
		int newExp = this.currentExp + exp;
		return newExp >= 0 && newExp < this.getMaxExp();
	}

	public boolean canWearArmor(ItemStack item) {
		int level = this.getTalentLevel(EnumCompanionTalent.ARMOR);
		if (item == null || !(item.getItem() instanceof ItemArmor) || level <= 0) {
			return false;
		}
		if (level >= 5) {
			return true;
		}
		ItemArmor armor = (ItemArmor) item.getItem();
		int reduction = 0;
		for (Field f : ItemArmor.ArmorMaterial.class.getDeclaredFields()) {
			if (f.getType() == int.class) {
				try {
					f.setAccessible(true);
					reduction = (int) f.get(armor.getArmorMaterial());
				} catch (Exception e) {
					LogWriter.debug(e.toString());
				}
				break;
			}
		}
		return reduction <= 5 || reduction <= 7 && level >= 2 || reduction <= 15 && level >= 3 || reduction <= 33 && level == 4;
	}

	public boolean canWearSword(IItemStack item) {
		int level = this.getTalentLevel(EnumCompanionTalent.SWORD);
		return item != null && item.getMCItemStack().getItem() instanceof ItemSword && level > 0
				&& (level >= 5 || this.getSwordDamage(item) - level < 4.0);
	}

	@Override
	public void clientUpdate() {
		if (this.npc.getRoleData().equals("eating")) {
			this.eating = this.getFood();
			if (this.isEating()) {
				this.doEating();
			}
		} else if (this.eating != null) {
			this.eating = null;
		}
	}

	private void damageArmor(float damage) {
		damage /= 4.0f;
		if (damage < 1.0f) {
			damage = 1.0f;
		}
		boolean hasArmor = false;
		Iterator<Map.Entry<Integer, IItemStack>> ita = this.npc.inventory.armor.entrySet().iterator();
		while (ita.hasNext()) {
			Map.Entry<Integer, IItemStack> entry = ita.next();
			IItemStack item = entry.getValue();
			if (item != null) {
				if (!(item.getMCItemStack().getItem() instanceof ItemArmor)) {
					continue;
				}
				hasArmor = true;
				item.getMCItemStack().damageItem((int) damage, this.npc);
				if (item.getStackSize() > 0) {
					continue;
				}
				ita.remove();
			}
		}
		this.gainExp(hasArmor ? 4 : 8);
	}

	@Override
	public boolean defendOwner() {
		return !this.defendOwner || this.owner == null || this.stage == EnumCompanionStage.BABY
                || (this.jobInterface != null && this.jobInterface.isSelfSufficient());
	}

	private void doEating() {
		if (this.eating == null || this.eating.isEmpty()) {
			return;
		}
		ItemStack eating = this.eating.getMCItemStack();
		if (this.npc.world.isRemote) {
			Random rand = this.npc.getRNG();
			for (int j = 0; j < 2; ++j) {
				Vec3d vec3 = new Vec3d((rand.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
				vec3.rotateYaw(-this.npc.rotationPitch * 3.1415927f / 180.0f);
				vec3.rotatePitch(-this.npc.renderYawOffset * 3.1415927f / 180.0f);
				Vec3d vec4 = new Vec3d((rand.nextFloat() - 0.5) * 0.3, -rand.nextFloat() * 0.6 - 0.3,
						this.npc.width / 2.0f + 0.1);
				vec4.rotateYaw(-this.npc.rotationPitch * 3.1415927f / 180.0f);
				vec4.rotatePitch(-this.npc.renderYawOffset * 3.1415927f / 180.0f);
				vec4 = vec4.addVector(this.npc.posX, this.npc.posY + this.npc.height + 0.1, this.npc.posZ);
				if (eating.getHasSubtypes()) {
					this.npc.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec4.x, vec4.y, vec4.z, vec3.x, vec3.y + 0.05, vec3.z, Item.getIdFromItem(eating.getItem()), eating.getMetadata());
				} else {
					this.npc.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec4.x, vec4.y, vec4.z, vec3.x, vec3.y + 0.05, vec3.z, Item.getIdFromItem(eating.getItem()));
				}
			}
		} else {
			--this.eatingTicks;
			if (this.eatingTicks <= 0) {
				if (this.inventory.decrStackSize(eating, 1)) {
					ItemFood food = (ItemFood) eating.getItem();
					this.foodstats.onFoodEaten(food, eating);
					this.npc.playSound(SoundEvents.ENTITY_PLAYER_BURP, 0.5f,
							this.npc.getRNG().nextFloat() * 0.1f + 0.9f);
				}
				this.eatingDelay = 20;
				this.npc.setRoleData("");
            } else if (this.eatingTicks > 3 && this.eatingTicks % 2 == 0) {
				Random rand = this.npc.getRNG();
				this.npc.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5f + 0.5f * rand.nextInt(2), (rand.nextFloat() - rand.nextFloat()) * 0.2f + 1.0f);
			}
		}
	}

	public void gainExp(int chance) {
		if (this.npc.getRNG().nextInt(chance) == 0) {
			this.addExp(1);
		}
	}

	public int getExp(EnumCompanionTalent talent) {
		if (this.talents.containsKey(talent)) {
			return this.talents.get(talent);
		}
		return -1;
	}

	private IItemStack getFood() {
		List<ItemStack> food = new ArrayList<>(this.inventory.items);
		Iterator<ItemStack> ite = food.iterator();
		int i = -1;
		while (ite.hasNext()) {
			ItemStack is = ite.next();
			if (is.isEmpty() || !(is.getItem() instanceof ItemFood)) {
				ite.remove();
			} else {
				int amount = is.getItem().getDamage(is);
				if (i != -1 && amount >= i) {
					continue;
				}
				i = amount;
			}
		}
		for (ItemStack is2 : food) {
			if (is2.getItem().getDamage(is2) == i) {
				return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(is2);
			}
		}
		return null;
	}

	public IItemStack getHeldItem() {
		if (this.eating != null && !this.eating.isEmpty()) {
			return this.eating;
		}
		return this.npc.inventory.getRightHand();
	}

	public int getMaxExp() {
		return 500 + this.getTotalLevel() * 200;
	}

	public Integer getNextLevel(EnumCompanionTalent talent) {
		if (!this.talents.containsKey(talent)) {
			return 0;
		}
		int exp = this.talents.get(talent);
		if (exp < 400) {
			return 400;
		}
		if (exp < 1000) {
			return 700;
		}
		if (exp < 1700) {
			return 1700;
		}
		if (exp < 3000) {
			return 3000;
		}
		return 5000;
	}

	public EntityPlayer getOwner() {
		if (this.uuid == null || this.uuid.isEmpty()) {
			return null;
		}
		try {
			UUID id = UUID.fromString(this.uuid);
            return NoppesUtilServer.getPlayer(Objects.requireNonNull(this.npc.getServer()), id);
        } catch (Exception e) { LogWriter.error("Error:", e); }
		return null;
	}

	private double getSwordDamage(IItemStack item) {
		if (item == null) {
			return 0.0;
		}
		return item.getAttackDamage();
	}

	public int getTalentLevel(EnumCompanionTalent talent) {
		if (!this.talents.containsKey(talent)) {
			return 0;
		}
		int exp = this.talents.get(talent);
		if (exp >= 5000) {
			return 5;
		}
		if (exp >= 3000) {
			return 4;
		}
		if (exp >= 1700) {
			return 3;
		}
		if (exp >= 1000) {
			return 2;
		}
		if (exp >= 400) {
			return 1;
		}
		return 0;
	}

	public int getTotalArmorValue() {
		int armorValue = 0;
		for (IItemStack armor : this.npc.inventory.armor.values()) {
			if (armor != null && armor.getMCItemStack().getItem() instanceof ItemArmor) {
				armorValue += ((ItemArmor) armor.getMCItemStack().getItem()).damageReduceAmount;
			}
		}
		return armorValue;
	}

	public int getTotalLevel() {
		int level = 0;
		for (EnumCompanionTalent talent : this.talents.keySet()) {
			level += this.getTalentLevel(talent);
		}
		return level;
	}

	public boolean hasInv() {
		return this.hasInv && (this.hasTalent(EnumCompanionTalent.INVENTORY)
				|| this.hasTalent(EnumCompanionTalent.ARMOR) || this.hasTalent(EnumCompanionTalent.SWORD));
	}

	@SuppressWarnings("all")
	public boolean hasOwner() {
		return !this.uuid.isEmpty();
	}

	public boolean hasTalent(EnumCompanionTalent talent) {
		return this.getTalentLevel(talent) > 0;
	}

	@Override
	public void interact(EntityPlayer player) {
		this.interact(player, false);
	}

	public void interact(EntityPlayer player, boolean openGui) {
		if (player != null && this.job == EnumCompanionJobs.SHOP) {
			((CompanionTrader) this.jobInterface).interact(player);
		}
		if (player != this.owner || !this.npc.isEntityAlive() || this.npc.isAttacking()) {
			return;
		}
        assert player != null;
        if (player.isSneaking() || openGui) {
			this.openGui(player);
		} else {
			this.setSitting(!this.isSitting());
		}
	}

	public boolean isEating() {
		return this.eating != null && !this.eating.isEmpty();
	}

	@Override
	public boolean isFollowing() {
		return (this.jobInterface == null || !this.jobInterface.isSelfSufficient()) && this.owner != null
				&& !this.isSitting();
	}

	public boolean isSitting() {
		return this.npc.ais.animationType == 1;
	}

	public void matureTo(EnumCompanionStage stage) {
		this.stage = stage;
		EntityCustomNpc npc = (EntityCustomNpc) this.npc;
		npc.ais.animationType = stage.animation;
		if (stage == EnumCompanionStage.BABY) {
			npc.modelData.getPartConfig(EnumParts.ARM_LEFT).setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.getPartConfig(EnumParts.LEG_LEFT).setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.getPartConfig(EnumParts.BODY).setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.getPartConfig(EnumParts.HEAD).setScale(0.7f, 0.7f, 0.7f);
			npc.ais.onAttack = 1;
			npc.ais.setWalkingSpeed(3);
			if (!this.talents.containsKey(EnumCompanionTalent.INVENTORY)) {
				this.talents.put(EnumCompanionTalent.INVENTORY, 0);
			}
		}
		if (stage == EnumCompanionStage.CHILD) {
			npc.modelData.getPartConfig(EnumParts.ARM_LEFT).setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.getPartConfig(EnumParts.LEG_LEFT).setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.getPartConfig(EnumParts.BODY).setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.getPartConfig(EnumParts.HEAD).setScale(0.8f, 0.8f, 0.8f);
			npc.ais.onAttack = 0;
			npc.ais.setWalkingSpeed(4);
			if (!this.talents.containsKey(EnumCompanionTalent.SWORD)) {
				this.talents.put(EnumCompanionTalent.SWORD, 0);
			}
		}
		if (stage == EnumCompanionStage.TEEN) {
			npc.modelData.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.getPartConfig(EnumParts.LEG_LEFT).setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.getPartConfig(EnumParts.BODY).setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.getPartConfig(EnumParts.HEAD).setScale(0.9f, 0.9f, 0.9f);
			npc.ais.onAttack = 0;
			npc.ais.setWalkingSpeed(5);
			if (!this.talents.containsKey(EnumCompanionTalent.ARMOR)) {
				this.talents.put(EnumCompanionTalent.ARMOR, 0);
			}
		}
		if (stage == EnumCompanionStage.ADULT || stage == EnumCompanionStage.FULL_GROWN) {
			npc.modelData.getPartConfig(EnumParts.ARM_LEFT).setScale(1.0f, 1.0f, 1.0f);
			npc.modelData.getPartConfig(EnumParts.LEG_LEFT).setScale(1.0f, 1.0f, 1.0f);
			npc.modelData.getPartConfig(EnumParts.BODY).setScale(1.0f, 1.0f, 1.0f);
			npc.modelData.getPartConfig(EnumParts.HEAD).setScale(1.0f, 1.0f, 1.0f);
			npc.ais.onAttack = 0;
			npc.ais.setWalkingSpeed(5);
		}
	}

	private void openGui(EntityPlayer player) {
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.Companion, this.npc);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.COMPANION;
		this.inventory.load(compound.getCompoundTag("CompanionInventory"));
		this.uuid = compound.getString("CompanionOwner");
		this.ownerName = compound.getString("CompanionOwnerName");
		this.companionID = compound.getInteger("CompanionID");
		this.stage = EnumCompanionStage.values()[compound.getInteger("CompanionStage")];
		this.currentExp = compound.getInteger("CompanionExp");
		this.canAge = compound.getBoolean("CompanionCanAge");
		this.ticksActive = compound.getLong("CompanionAge");
		this.hasInv = compound.getBoolean("CompanionHasInv");
		this.defendOwner = compound.getBoolean("CompanionDefendOwner");
		this.foodstats.readNBT(compound);
		NBTTagList list = compound.getTagList("CompanionTalents", 10);
		Map<EnumCompanionTalent, Integer> talents = new TreeMap<>();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound c = list.getCompoundTagAt(i);
			EnumCompanionTalent talent = EnumCompanionTalent.values()[c.getInteger("Talent")];
			talents.put(talent, c.getInteger("Exp"));
		}
		this.talents = talents;
		this.setJob(compound.getInteger("CompanionJob"));
		if (this.jobInterface != null) {
			this.jobInterface.setNBT(compound.getCompoundTag("CompanionJobData"));
		}
		this.setStats();
	}

	public void setExp(EnumCompanionTalent talent, int exp) {
		this.talents.put(talent, exp);
	}

	private void setJob(int i) {
		this.job = EnumCompanionJobs.values()[i];
		if (this.job == EnumCompanionJobs.SHOP) {
			this.jobInterface = new CompanionTrader();
		} else if (this.job == EnumCompanionJobs.FARMER) {
			this.jobInterface = new CompanionFarmer();
		} else if (this.job == EnumCompanionJobs.GUARD) {
			this.jobInterface = new CompanionGuard();
		} else {
			this.jobInterface = null;
		}
		if (this.jobInterface != null) {
			this.jobInterface.npc = this.npc;
		}
	}

	public void setOwner(EntityPlayer player) {
		this.uuid = player.getUniqueID().toString();
	}

	public void setSitting(boolean sit) {
		if (sit) {
			this.npc.ais.animationType = 1;
			this.npc.ais.onAttack = 3;
			this.npc.ais.setStartPos(new BlockPos(this.npc));
			this.npc.getNavigator().clearPath();
			this.npc.setPositionAndUpdate(this.npc.getStartXPos(), this.npc.posY, this.npc.getStartZPos());
		} else {
			this.npc.ais.animationType = this.stage.animation;
			this.npc.ais.onAttack = 0;
		}
		this.npc.updateAI = true;
	}

	public void setStats() {
		IItemStack weapon = this.npc.inventory.getRightHand();
		this.npc.stats.melee.setStrength((int) (1.0d + this.getSwordDamage(weapon)));
		this.npc.stats.healthRegen = 0;
		this.npc.stats.combatRegen = 0;
		int ranged = this.getTalentLevel(EnumCompanionTalent.RANGED);
		if (ranged > 0 && weapon != null) {
			Item item = weapon.getMCItemStack().getItem();
			if (item == Item.getItemFromBlock(Blocks.COBBLESTONE)) {
				this.npc.inventory.setProjectile(weapon);
			}
			if (item instanceof ItemBow) {
				this.npc.inventory.setProjectile(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(Items.ARROW)));
			}
		}
		this.inventory.setSize(2 + this.getTalentLevel(EnumCompanionTalent.INVENTORY) * 2);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.COMPANION.get());
		compound.setTag("CompanionInventory", this.inventory.save());
		compound.setString("CompanionOwner", this.uuid);
		compound.setString("CompanionOwnerName", this.ownerName);
		compound.setInteger("CompanionID", this.companionID);
		compound.setInteger("CompanionStage", this.stage.ordinal());
		compound.setInteger("CompanionExp", this.currentExp);
		compound.setBoolean("CompanionCanAge", this.canAge);
		compound.setLong("CompanionAge", this.ticksActive);
		compound.setBoolean("CompanionHasInv", this.hasInv);
		compound.setBoolean("CompanionDefendOwner", this.defendOwner);
		this.foodstats.writeNBT(compound);
		compound.setInteger("CompanionJob", this.job.ordinal());
		if (this.jobInterface != null) {
			compound.setTag("CompanionJobData", this.jobInterface.getNBT());
		}
		NBTTagList list = new NBTTagList();
		for (EnumCompanionTalent talent : this.talents.keySet()) {
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("Talent", talent.ordinal());
			c.setInteger("Exp", this.talents.get(talent));
			list.appendTag(c);
		}
		compound.setTag("CompanionTalents", list);
		return compound;
	}
}
