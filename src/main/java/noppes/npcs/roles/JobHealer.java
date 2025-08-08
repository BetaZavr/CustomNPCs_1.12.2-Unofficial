package noppes.npcs.roles;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobHealer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.data.HealerSettings;
import noppes.npcs.util.ValueUtil;

public class JobHealer extends JobInterface implements IJobHealer {

	// New from Unofficial (BetaZavr)
	private final Map<Integer, List<EntityLivingBase>> affected = new HashMap<>();
	private final Random rnd = new Random();
	public Map<Integer, HealerSettings> effects = new HashMap<>(); // [ID, settings]

	public JobHealer(EntityNPCInterface npc) {
		super(npc);
		type = JobType.HEALER;
	}

	@Override
	public boolean isWorking() {
		return !affected.isEmpty() && npc.isAttacking();
	}

	@Override
	public boolean aiContinueExecute() { return false; }

	@Override
	public boolean aiShouldExecute() {
		boolean canAdd = false;
		affected.clear();
		for (Integer id : effects.keySet()) {
			if (npc.totalTicksAlive % effects.get(id).speed < 3) {
				canAdd = true;
				int r = effects.get(id).range;
				List<EntityLivingBase> list = new ArrayList<>();
				try {
					list = npc.world.getEntitiesWithinAABB(EntityLivingBase.class, npc.getEntityBoundingBox().grow(r, r / 2.0d, r));
				}
				catch (Exception ignored) { }
				affected.put(id, list);
				if (!effects.get(id).onHimself) {
					affected.get(id).remove(npc);
				}
			}
		}
		return canAdd;
	}

	@Override
	public void aiStartExecuting() {
		boolean activated = false;
		for (Integer id : affected.keySet()) {
			Potion potion = Potion.getPotionById(id);
			if (potion == null) { continue; }
			HealerSettings hs = effects.get(id);
			if (!hs.isMassive) {
				if (affected.get(id).isEmpty()) { continue; }
				EntityLivingBase entity = null;
				try {
					List<EntityLivingBase> filteredEntities = affected.get(id).stream()
							.filter(e -> e.getActivePotionEffects().stream().noneMatch(effect -> effect.getPotion() == potion))
							.collect(Collectors.toList());
					if (!filteredEntities.isEmpty()) { entity = filteredEntities.get(rnd.nextInt(filteredEntities.size())); }
				} catch (Exception e) { LogWriter.error(e); }
				if (entity != null) {
					boolean isEnemy = isEnemy(entity);
					if (hs.type == 2 || (hs.type == 0 && !isEnemy) || (hs.type == 1 && isEnemy)) {
						entity.addPotionEffect(new PotionEffect(potion, hs.time, hs.amplifier));
						activated = true;
						if (entity != npc && (!CustomNpcs.ShowCustomAnimation || !npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES))) {
							npc.getLookHelper().setLookPositionWithEntity(entity, 10.0f, npc.getVerticalFaceSpeed());
						}
					}
				}
			} else {
				for (EntityLivingBase entity : affected.get(id)) {
					if ((entity instanceof EntityMob || entity instanceof EntityAnimal) && !hs.possibleOnMobs) { continue; }
					boolean isEnemy = isEnemy(entity);
					if (hs.type == 2 || (hs.type == 0 && !isEnemy) || (hs.type == 1 && isEnemy)) {
						entity.addPotionEffect(new PotionEffect(potion, hs.time, hs.amplifier));
						activated = true;
					}
				}
			}
		}
		affected.clear();
		if (activated) {
			if (!npc.getHeldItemMainhand().isEmpty()) {
				npc.swingArm(EnumHand.MAIN_HAND);
			} else {
				npc.swingArm(EnumHand.OFF_HAND);
			}
		}
	}

	@Override
	public void load(NBTTagCompound compound) {
		super.load(compound);
		type = JobType.HEALER;
		effects.clear();
		if (compound.hasKey("HealerData", 9)) {
			for (int i = 0; i < compound.getTagList("HealerData", 10).tagCount(); i++) {
				HealerSettings hs = new HealerSettings(compound.getTagList("HealerData", 10).getCompoundTagAt(i));
				effects.put(hs.id, hs);
			}
		} else if (compound.hasKey("HealerRange", 3)) { // OLD
			int range = compound.getInteger("HealerRange");
			int speed = ValueUtil.correctInt(compound.getInteger("HealerSpeed"), 10, Integer.MAX_VALUE);
			byte type = compound.getByte("HealerType");
			HashMap<Integer, Integer> oldMap = NBTTags.getIntegerIntegerMap(compound.getTagList("BeaconEffects", 10));
			for (int id : oldMap.keySet()) {
				HealerSettings hs = new HealerSettings(id, range, speed, oldMap.get(id), type);
				effects.put(hs.id, hs);
			}
		}
	}

	@Override
	public NBTTagCompound save(NBTTagCompound compound) {
		super.save(compound);
		NBTTagList list = new NBTTagList();
		for (HealerSettings hs : effects.values()) { list.appendTag(hs.writeNBT()); }
		compound.setTag("HealerData", list);
		return compound;
	}

	// New from Unofficial (BetaZavr)
	private boolean isEnemy(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) { return npc.faction.isAggressiveToPlayer((EntityPlayer) entity); }
		else if (entity instanceof EntityNPCInterface) { return npc.faction.isAggressiveToNpc((EntityNPCInterface) entity); }
		return (entity instanceof EntityMob);
	}

}
