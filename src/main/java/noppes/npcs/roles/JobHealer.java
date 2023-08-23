package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import noppes.npcs.NBTTags;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class JobHealer
extends JobInterface {
	
	private List<EntityLivingBase> affected;
	public HashMap<Integer, Integer> effects;
	private int healTicks;
	public int range;
	public int speed;
	public byte healerType;

	public JobHealer(EntityNPCInterface npc) {
		super(npc);
		this.healTicks = 0;
		this.range = 8;
		this.healerType = (byte) 2;
		this.speed = 20;
		this.effects = new HashMap<Integer, Integer>();
		this.affected = new ArrayList<EntityLivingBase>();
		this.type = JobType.HEALER;
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		++this.healTicks;
		if (this.healTicks < this.speed) {
			return false;
		}
		this.healTicks = 0;
		this.affected = this.npc.world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.npc.getEntityBoundingBox().grow(this.range, this.range / 2.0, this.range));
		return !this.affected.isEmpty();
	}

	@Override
	public void aiStartExecuting() {
		for (EntityLivingBase entity : this.affected) {
			boolean isEnemy = false;
			if (entity instanceof EntityPlayer) {
				isEnemy = this.npc.faction.isAggressiveToPlayer((EntityPlayer) entity);
			} else if (entity instanceof EntityNPCInterface) {
				isEnemy = this.npc.faction.isAggressiveToNpc((EntityNPCInterface) entity);
			} else {
				isEnemy = (entity instanceof EntityMob);
			}
			if (entity != this.npc && (this.healerType != (byte) 0 || !isEnemy)) {
				if (this.healerType == (byte) 1 && !isEnemy) {
					continue;
				}
				for (Integer potionEffect : this.effects.keySet()) {
					Potion p = Potion.getPotionById(potionEffect);
					if (p != null) {
						entity.addPotionEffect(new PotionEffect(p, 100, this.effects.get(potionEffect)));
					}
				}
			}
		}
		this.affected.clear();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.HEALER;
		this.range = compound.getInteger("HealerRange");
		this.healerType = compound.getByte("HealerType");
		this.effects = NBTTags.getIntegerIntegerMap(compound.getTagList("BeaconEffects", 10));
		this.speed = ValueUtil.correctInt(compound.getInteger("HealerSpeed"), 10, Integer.MAX_VALUE);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.HEALER.get());
		compound.setInteger("HealerRange", this.range);
		compound.setByte("HealerType", this.healerType);
		compound.setTag("BeaconEffects", NBTTags.nbtIntegerIntegerMap(this.effects));
		compound.setInteger("HealerSpeed", this.speed);
		return compound;
	}
}
