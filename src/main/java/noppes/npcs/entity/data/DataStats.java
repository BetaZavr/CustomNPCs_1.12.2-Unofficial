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

public class DataStats implements INPCStats {

	public boolean burnInSun = false;
	public boolean calmdown = true;
	public boolean canDrown = true;
	public boolean hideKilledBody = false;
	public boolean ignoreCobweb = false;
	public boolean immuneToFire = false;
	public boolean noFallDamage = false;
	public boolean potionImmune = false;
	public int aggroRange = 16;
	public int combatRegen = 0;
	public int healthRegen = 1;
	private int level = 1;
	public int respawnTime = 20;
	public int spawnCycle = 0;
	private float chanceBlockDamage = 2.0f;
	public double maxHealth = 20.0d;

	public EnumCreatureAttribute creatureType = EnumCreatureAttribute.UNDEFINED;
	private String rarityTitle = ((char) 167) + "flv." + ((char) 167) + "21";
	private EnumCreatureRarity rarity = EnumCreatureRarity.NORMAL;

	public DataMelee melee;
	private final EntityNPCInterface npc;
	public DataRanged ranged;
	public Resistances resistances = new Resistances();

	public DataStats(EntityNPCInterface npc) {
		this.npc = npc;
		this.melee = new DataMelee(npc);
		this.ranged = new DataRanged(npc);
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
			hp = corr[1];
		}
		return hp;
	}

	@Override
	public boolean getImmune(int type) {
		switch (type) {
		case 0:
			return this.potionImmune;
		case 1:
			return this.noFallDamage;
		case 2:
			return this.burnInSun;
		case 3:
			return this.immuneToFire;
		case 4:
			return this.canDrown;
		case 5:
			return this.ignoreCobweb;
		}
		throw new CustomNPCsException("Unknown immune type: " + type);
	}

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

	@Override
	public INPCRanged getRanged() {
		return this.ranged;
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
	public String[] getResistanceKeys() {
		return this.resistances.data.keySet().toArray(new String[0]);
	}
	
	@Override
	public float getResistance(String damageName) {
		return this.resistances.get(damageName);
	}

	@Override
	public int getRespawnTime() {
		return this.respawnTime;
	}

	@Override
	public int getRespawnType() {
		return this.spawnCycle;
	}

	@Override
	public boolean isCalmdown() {
		return this.calmdown;
	}

	public void readToNBT(NBTTagCompound compound) {
		if (compound.hasKey("Resistances", 9)) { this.resistances.readToNBT(compound.getTagList("Resistances", 10)); }
		else { this.resistances.oldReadToNBT(compound.getCompoundTag("Resistances")); }
		if (compound.hasKey("MaxHealth", 3)) { // Old
			this.setMaxHealth(compound.getInteger("MaxHealth"));
		} else {
			this.setMaxHealth(compound.getDouble("MaxHealth"));
		}
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
		this.level = compound.getInteger("NPCLevel");
		this.rarity = EnumCreatureRarity.values()[compound.getInteger("NPCRarity")];
		this.rarityTitle = compound.getString("RarityTitle");
		this.calmdown = compound.getBoolean("CalmdownRange");
		if (this.aggroRange < 1) {
			this.aggroRange = 1;
		}
		IAttributeInstance follow_range = this.npc.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        follow_range.setBaseValue(this.aggroRange);

		if (compound.hasKey("ChanceBlockDamage", 5)) { this.setChanceBlockDamage(compound.getFloat("ChanceBlockDamage")); }
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
				throw new CustomNPCsException("Unknown immune type: " + type);
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
		if (maxHealth == this.maxHealth) {
			return;
		}
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
	public void setResistance(String damageName, float value) {
		this.resistances.data.put(damageName, ValueUtil.correctFloat(value, 0.0f, 2.0f));
	}

	@Override
	public void setRespawnTime(int seconds) {
		this.respawnTime = seconds;
	}

	@Override
	public void setRespawnType(int type) {
		this.spawnCycle = type;
	}

	@Override
	public float getChanceBlockDamage() { return this.chanceBlockDamage; }

	@Override
	public void setChanceBlockDamage(float chance) {
		if (chance < 0.0f) { chance *= -1.0f; }
		if (chance > 100.0f) { chance = 100.0f; }
		this.chanceBlockDamage = chance;
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
		compound.setInteger("NPCLevel", this.level);
		compound.setInteger("NPCRarity", this.rarity.ordinal());
		compound.setString("RarityTitle", this.rarityTitle);
		compound.setBoolean("CalmdownRange", this.calmdown);
		compound.setFloat("ChanceBlockDamage", this.chanceBlockDamage);
		return compound;
	}

}
