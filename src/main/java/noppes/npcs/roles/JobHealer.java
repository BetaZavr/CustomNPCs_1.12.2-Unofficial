package noppes.npcs.roles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import noppes.npcs.NBTTags;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobHealer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.data.HealerSettings;
import noppes.npcs.util.ValueUtil;

public class JobHealer
extends JobInterface
implements IJobHealer {
	
	private Map<Integer, List<EntityLivingBase>> affected;
	private Random rnd;
	public Map<Integer, HealerSettings> effects; // [ID, settings]

	public JobHealer(EntityNPCInterface npc) {
		super(npc);
		this.effects = Maps.<Integer, HealerSettings>newHashMap();
		this.affected = Maps.<Integer, List<EntityLivingBase>>newHashMap();
		this.type = JobType.HEALER;
		this.rnd = new Random();
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		boolean canAdd = false;
		this.affected.clear();
		for (Integer id : this.effects.keySet()) {
			if (this.npc.totalTicksAlive % this.effects.get(id).speed < 3) {
				canAdd = true;
				int r = this.effects.get(id).range;
				this.affected.put(id, this.npc.world.getEntitiesWithinAABB(EntityLivingBase.class, this.npc.getEntityBoundingBox().grow(r, r / 2.0d, r)));
				if (!this.effects.get(id).onHimself) { this.affected.get(id).remove(this.npc); }
			}
		}
		return canAdd;
	}

	@Override
	public void aiStartExecuting() {
		boolean activated = false;
		for (Integer id : this.affected.keySet()) {
			Potion potion = Potion.getPotionById(id);
			if (potion == null) { continue; }
			HealerSettings hs = this.effects.get(id);
			if (!hs.isMassive) {
				if (this.affected.get(id).isEmpty()) { continue; }
				EntityLivingBase entity = null;
				try { entity = this.affected.get(id).get(this.rnd.nextInt(this.affected.get(id).size())); } catch (Exception e) {}
				if (entity!=null) {
					boolean isEnemy = this.isEnemy(entity);
					boolean canAdd = true;
					switch(hs.type) {
						case (byte) 0: canAdd = !isEnemy; break;
						case (byte) 1: canAdd = isEnemy; break;
					}
					if (canAdd) {
						entity.addPotionEffect(new PotionEffect(potion, hs.time, hs.amplifier));
						activated = true;
					}
				}
			} else {
				for (EntityLivingBase entity : this.affected.get(id)) {
					if ((entity instanceof EntityMob || entity instanceof EntityAnimal) && !hs.possibleOnMobs) { continue; }
					boolean isEnemy = this.isEnemy(entity);
					boolean next = false;
					switch(hs.type) {
						case (byte) 0: next = isEnemy; break;
						case (byte) 1: next = !isEnemy; break;
					}
					if (next) { continue; }
					entity.addPotionEffect(new PotionEffect(potion, hs.time, hs.amplifier));
					activated = true;
				}
			}
		}
		this.affected.clear();
		if (activated) {
			if (!this.npc.getHeldItemMainhand().isEmpty()) { this.npc.swingArm(EnumHand.MAIN_HAND); }
			else { this.npc.swingArm(EnumHand.OFF_HAND); }
		}
	}

	private boolean isEnemy(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			return this.npc.faction.isAggressiveToPlayer((EntityPlayer) entity);
		} else if (entity instanceof EntityNPCInterface) {
			return this.npc.faction.isAggressiveToNpc((EntityNPCInterface) entity);
		}
		return (entity instanceof EntityMob);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.HEALER;
		this.effects.clear();
		if (compound.hasKey("HealerData", 9)) {
			for (int i = 0; i < compound.getTagList("HealerData", 10).tagCount(); i++) {
				HealerSettings hs = new HealerSettings(compound.getTagList("HealerData", 10).getCompoundTagAt(i));
				this.effects.put(hs.id, hs);
			}
		} else if (compound.hasKey("HealerRange", 3)) { // OLD
			int range = compound.getInteger("HealerRange");
			int speed = ValueUtil.correctInt(compound.getInteger("HealerSpeed"), 10, Integer.MAX_VALUE);
			byte type = compound.getByte("HealerType");
			HashMap<Integer, Integer> oldMap = NBTTags.getIntegerIntegerMap(compound.getTagList("BeaconEffects", 10));
			for (int id : oldMap.keySet()) {
				HealerSettings hs = new HealerSettings(id, range, speed, oldMap.get(id), type);
				this.effects.put(hs.id, hs);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.HEALER.get());
		NBTTagList list = new NBTTagList();
		for (HealerSettings hs : this.effects.values()) {
			list.appendTag(hs.writeNBT());
		}
		compound.setTag("HealerData", list);
		
		return compound;
	}
}
