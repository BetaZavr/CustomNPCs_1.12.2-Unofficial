package noppes.npcs.entity.data;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.INPCMelee;
import noppes.npcs.api.entity.data.INPCRanged;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.constants.EnumCreatureRarity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataStats
implements INPCStats {
	
	public int aggroRange;
	public boolean burnInSun;
	public boolean calmdown;
	public boolean canDrown;
	public int combatRegen;
	public EnumCreatureAttribute creatureType;
	public int healthRegen;
	public boolean hideKilledBody;
	public boolean ignoreCobweb;
	public boolean immuneToFire;
	// New
	private int level = 1;
	private String rarityTitle;
	private EnumCreatureRarity rarity;
	
	public double maxHealth;
	public DataMelee melee;
	public boolean noFallDamage;
	private EntityNPCInterface npc;
	public boolean potionImmune;
	public DataRanged ranged;
	public Resistances resistances;
	public int respawnTime;
	public int spawnCycle;

	public DataStats(EntityNPCInterface npc) {
		this.aggroRange = 16;
		this.maxHealth = 20.0d;
		this.respawnTime = 20;
		this.spawnCycle = 0;
		this.hideKilledBody = false;
		this.resistances = new Resistances();
		this.immuneToFire = false;
		this.potionImmune = false;
		this.canDrown = true;
		this.burnInSun = false;
		this.noFallDamage = false;
		this.ignoreCobweb = false;
		this.healthRegen = 1;
		this.combatRegen = 0;
		this.creatureType = EnumCreatureAttribute.UNDEFINED;
		this.npc = npc;
		this.melee = new DataMelee(npc);
		this.ranged = new DataRanged(npc);
		// New
		this.level = 1;
		this.rarity = EnumCreatureRarity.NORMAL;
		this.rarityTitle = ((char) 167) + "flv." + ((char) 167) + "21";
		this.calmdown = true;
	}

	@Override
	public int getAggroRange() {
		return this.aggroRange;
	}

	@Override
	public int getCombatRegen() {
		return this.combatRegen;
	}

	@Override
	public int getCreatureType() {
		return this.creatureType.ordinal();
	}

	@Override
	public int getHealthRegen() {
		return this.healthRegen;
	}

	@Override
	public boolean getHideDeadBody() {
		return this.hideKilledBody;
	}

	public double getHP() {
		int[] corr = CustomNpcs.HealthNormal;
		if (this.rarity == EnumCreatureRarity.ELITE) {
			corr = CustomNpcs.HealthElite;
		} else if (this.rarity == EnumCreatureRarity.BOSS) {
			corr = CustomNpcs.HealthBoss;
		}
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[0] - a;
		double hp = Math.round(a * Math.pow(this.level, 2) + b);
		if (hp <= 1.0d) {
			hp = 1.0d;
		}
		if (hp > 10000) {
			hp = Math.ceil(hp / 100.0d) * 100.0d;
		} else if (hp > 1000) {
			hp = Math.ceil(hp / 25.0d) * 25.0d;
		} else if (hp > 100) {
			hp = Math.ceil(hp / 10.0d) * 10.0d;
		} else if (hp > 50) {
			hp = Math.ceil(hp / 5.0d) * 5.0d;
		} else {
			hp = Math.ceil(hp);
		}
		if (hp > (double) corr[1]) {
			hp = (double) corr[1];
		}
		return hp;
	}

	@Override
	public boolean getImmune(int type) {
		switch(type) {
			case 0: return this.potionImmune;
			case 1: return this.noFallDamage;
			case 2: return this.burnInSun;
			case 3: return this.immuneToFire;
			case 4: return this.canDrown;
			case 5: return this.ignoreCobweb;
		}
		throw new CustomNPCsException("Unknown immune type: " + type, new Object[0]);
	}

	// new
	@Override
	public int getLevel() {
		if (this.level < 1) {
			this.level = 1;
		} else if (this.level > CustomNpcs.MaxLv) {
			this.level = CustomNpcs.MaxLv;
		}
		return this.level;
	}

	@Override
	public double getMaxHealth() {
		return this.maxHealth;
	}

	@Override
	public INPCMelee getMelee() {
		return this.melee;
	}

	public int getMellePower() {
		int[] corr = CustomNpcs.DamageNormal;
		if (this.rarity == EnumCreatureRarity.ELITE) {
			corr = CustomNpcs.DamageElite;
		} else if (this.rarity == EnumCreatureRarity.BOSS) {
			corr = CustomNpcs.DamageBoss;
		}
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[0] - a;
		return (int) Math.round(a * Math.pow(this.level, 2) + b);
	}

	@Override
	public INPCRanged getRanged() {
		return this.ranged;
	}

	public int getRangePower() {
		int[] corr = CustomNpcs.DamageNormal;
		if (this.rarity == EnumCreatureRarity.ELITE) {
			corr = CustomNpcs.DamageElite;
		} else if (this.rarity == EnumCreatureRarity.BOSS) {
			corr = CustomNpcs.DamageBoss;
		}
		double a = ((double) corr[2] - (double) corr[3]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[2] - a;
		return (int) Math.round(a * Math.pow(this.level, 2) + b);
	}

	@Override
	public int getRarity() {
		return this.rarity.ordinal();
	}

	@Override
	public String getRarityTitle() {
		return this.rarityTitle;
	}

	@Override
	public float getResistance(int type) {
		switch(type) {
			case 0: return this.resistances.melee;
			case 1: return this.resistances.arrow;
			case 2: return this.resistances.explosion;
			case 3: return this.resistances.knockback;
		}
		return 1.0f;
	}

	@Override
	public int getRespawnTime() {
		return this.respawnTime;
	}

	@Override
	public int getRespawnType() {
		return this.spawnCycle;
	}

	public int[] getXP() {
		float[] corr = new float[] { (float) CustomNpcs.Experience[0], (float) CustomNpcs.Experience[1],
				(float) CustomNpcs.Experience[2], (float) CustomNpcs.Experience[3] };
		if (this.rarity == EnumCreatureRarity.ELITE) {
			corr[0] *= 1.75f;
			corr[1] *= 1.75f;
			corr[2] *= 1.75f;
			corr[3] *= 1.75f;
		} else if (this.rarity == EnumCreatureRarity.BOSS) {
			corr[0] *= 4.75f;
			corr[1] *= 4.75f;
			corr[2] *= 4.75f;
			corr[3] *= 4.75f;
		}
		int subMinLv = CustomNpcs.MaxLv / 3;
		int subMaxLv = CustomNpcs.MaxLv * 2 / 3;
		float subMinXP = corr[1] / 3.0f;
		float subMaxXP = corr[1] * 2.0f / 3.0f;
		float subMinXPM = corr[3] / 3.0f;
		float subMaxXPM = corr[3] * 2.0f / 3.0f;
		double a = ((subMaxXP - corr[1]) * (1 - subMinLv) - (corr[0] - subMinXP) * (subMaxLv - CustomNpcs.MaxLv))
				/ ((subMaxLv - CustomNpcs.MaxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.MaxLv, 2) - Math.pow(subMaxLv, 2)));
		double b = (corr[0] - subMinXP + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		double c = corr[0] - a - b;
		int min = (int) (Math.pow(this.level, 2) * a + this.level * b + c);
		a = ((subMaxXPM - corr[3]) * (1 - subMinLv) - (corr[2] - subMinXPM) * (subMaxLv - CustomNpcs.MaxLv))
				/ ((subMaxLv - CustomNpcs.MaxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.MaxLv, 2) - Math.pow(subMaxLv, 2)));
		b = (corr[2] - subMinXPM + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		c = corr[2] - a - b;
		int max = (int) (Math.pow(this.level, 2) * a + this.level * b + c);
		return new int[] { min, max };
	}

	@Override
	public boolean isCalmdown() {
		return this.calmdown;
	}

	public void readToNBT(NBTTagCompound compound) {
		this.resistances.readToNBT(compound.getCompoundTag("Resistances"));
		if (compound.hasKey("MaxHealth", 3)) { // Old
			this.setMaxHealth(compound.getInteger("MaxHealth"));
		}
		else { this.setMaxHealth(compound.getDouble("MaxHealth")); }
		this.hideKilledBody = compound.getBoolean("HideBodyWhenKilled");
		this.aggroRange = compound.getInteger("AggroRange");
		this.respawnTime = compound.getInteger("RespawnTime");
		this.spawnCycle = compound.getInteger("SpawnCycle");
		this.creatureType = EnumCreatureAttribute.values()[compound.getInteger("CreatureType")];
		this.healthRegen = compound.getInteger("HealthRegen");
		this.combatRegen = compound.getInteger("CombatRegen");
		this.immuneToFire = compound.getBoolean("ImmuneToFire");
		this.potionImmune = compound.getBoolean("PotionImmune");
		this.canDrown = compound.getBoolean("CanDrown");
		this.burnInSun = compound.getBoolean("BurnInSun");
		this.noFallDamage = compound.getBoolean("NoFallDamage");
		this.npc.setImmuneToFire(this.immuneToFire);
		this.ignoreCobweb = compound.getBoolean("IgnoreCobweb");
		this.melee.readFromNBT(compound);
		this.ranged.readFromNBT(compound);
		// New
		this.level = compound.getInteger("NPCLevel");
		this.rarity = EnumCreatureRarity.values()[compound.getInteger("NPCRarity")];
		this.rarityTitle = compound.getString("RarityTitle");
		this.calmdown = compound.getBoolean("CalmdownRange");
		if (this.aggroRange < 1) { this.aggroRange = 1; }
		IAttributeInstance follow_range = this.npc.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
		if (follow_range!=null) { follow_range.setBaseValue(this.aggroRange); }
	}
	
	@Override
	public void setAggroRange(int range) {
		this.aggroRange = range;
	}

	@Override
	public void setCalmdown(boolean range) {
		this.calmdown = range;
	}

	@Override
	public void setCombatRegen(int regen) {
		this.combatRegen = regen;
	}

	@Override
	public void setCreatureType(int type) {
		this.creatureType = EnumCreatureAttribute.values()[type];
	}

	@Override
	public void setHealthRegen(int regen) {
		this.healthRegen = regen;
	}

	@Override
	public void setHideDeadBody(boolean hide) {
		this.hideKilledBody = hide;
		this.npc.updateClient = true;
	}

	@Override
	public void setImmune(int type, boolean bo) {
		if (type == 0) {
			this.potionImmune = bo;
		} else if (type == 1) {
			this.noFallDamage = !bo;
		} else if (type == 2) {
			this.burnInSun = bo;
		} else if (type == 3) {
			this.npc.setImmuneToFire(bo);
		} else if (type == 4) {
			this.canDrown = !bo;
		} else {
			if (type != 5) {
				throw new CustomNPCsException("Unknown immune type: " + type, new Object[0]);
			}
			this.ignoreCobweb = bo;
		}
	}

	@Override
	public void setLevel(int level) {
		if (level < 1) {
			level = 1;
		} else if (level > CustomNpcs.MaxLv) {
			level = CustomNpcs.MaxLv;
		}
		this.level = level;
	}

	@Override
	public void setMaxHealth(double maxHealth) {
		if (maxHealth == this.maxHealth) { return; }
		this.maxHealth = maxHealth;
		this.npc.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
		this.npc.updateClient = true;
	}

	@Override
	public void setRarity(int rarity) {
		if (rarity < 0) {
			rarity = 0;
		} else if (rarity > EnumCreatureRarity.values().length) {
			rarity = EnumCreatureRarity.values().length;
		}
		this.rarity = EnumCreatureRarity.values()[rarity];
	}

	@Override
	public void setRarityTitle(String rarity) {
		if (this.rarityTitle.equals(rarity)) {
			return;
		}
		this.rarityTitle = rarity;
		this.npc.updateClient = true;
	}

	@Override
	public void setResistance(int type, float value) {
		value = ValueUtil.correctFloat(value, 0.0f, 2.0f);
		if (type == 0) {
			this.resistances.melee = value;
		} else if (type == 1) {
			this.resistances.arrow = value;
		} else if (type == 2) {
			this.resistances.explosion = value;
		} else if (type == 3) {
			this.resistances.knockback = value;
		}
	}

	@Override
	public void setRespawnTime(int seconds) {
		this.respawnTime = seconds;
	}

	@Override
	public void setRespawnType(int type) {
		this.spawnCycle = type;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Resistances", this.resistances.writeToNBT());
		compound.setDouble("MaxHealth", this.maxHealth);
		compound.setInteger("AggroRange", this.aggroRange);
		compound.setBoolean("HideBodyWhenKilled", this.hideKilledBody);
		compound.setInteger("RespawnTime", this.respawnTime);
		compound.setInteger("SpawnCycle", this.spawnCycle);
		compound.setInteger("CreatureType", this.creatureType.ordinal());
		compound.setInteger("HealthRegen", this.healthRegen);
		compound.setInteger("CombatRegen", this.combatRegen);
		compound.setBoolean("ImmuneToFire", this.immuneToFire);
		compound.setBoolean("PotionImmune", this.potionImmune);
		compound.setBoolean("CanDrown", this.canDrown);
		compound.setBoolean("BurnInSun", this.burnInSun);
		compound.setBoolean("NoFallDamage", this.noFallDamage);
		compound.setBoolean("IgnoreCobweb", this.ignoreCobweb);
		this.melee.writeToNBT(compound);
		this.ranged.writeToNBT(compound);
		// New
		compound.setInteger("NPCLevel", this.level);
		compound.setInteger("NPCRarity", this.rarity.ordinal());
		compound.setString("RarityTitle", this.rarityTitle);
		compound.setBoolean("CalmdownRange", this.calmdown);
		return compound;
	}
	
}
