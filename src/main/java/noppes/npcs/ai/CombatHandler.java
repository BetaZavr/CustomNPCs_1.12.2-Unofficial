package noppes.npcs.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.util.DamageSource;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.api.mixin.entity.IEntityLivingBaseMixin;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class CombatHandler {

	public final Map<EntityLivingBase, Long> lastDamages = new HashMap<>();
	public final Map<EntityLivingBase, Double> aggressors = new HashMap<>();
	private int combatResetTimer = 0;
	private int delay = 10;
	private final EntityNPCInterface npc;
	public boolean onlyPlayers = false;

	public EntityLivingBase priorityTarget = null;

	public CombatHandler(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean checkTarget() {
		if (aggressors.isEmpty() || npc.ticksExisted % 10 != 0) { return false; }
		EntityLivingBase target = npc.getAttackTarget();
		Double current = 0.0d;
		if (isValidTarget(target)) {
			current = aggressors.get(target);
			if (current == null) { current = 0.0d; }
		} else {
			target = null;
		}
		for (Map.Entry<EntityLivingBase, Double> entry : aggressors.entrySet()) {
			if (entry.getValue() > current && isValidTarget(entry.getKey())) {
				current = entry.getValue();
				target = entry.getKey();
			}
		}
		return target == null;
	}

	public void damage(DamageSource source, double damageAmount) {
		combatResetTimer = 0;
		Entity e = NoppesUtilServer.GetDamageSource(source);
		if (!(e instanceof EntityLivingBase)) { return; }
		EntityLivingBase attackingEntity = (EntityLivingBase) e;
		if (attackingEntity instanceof EntityPlayer) {
			if (((EntityPlayer) attackingEntity).capabilities.isCreativeMode) { return; }
			onlyPlayers = true;
		}
		// Minimum
		if (damageAmount <= 0.25d) { damageAmount = 0.25d; }
		// Distance
		double dist = npc.getDistance(attackingEntity.posX, attackingEntity.posY, attackingEntity.posZ);
		// Value
		double newValue = damageAmount;
		// further target, greater anger [5_block +0%; 32_blocks +25%]
		if (dist > 5 && dist < 32) { newValue *= 0.009259d * dist + 0.953704d; }
		// is player
		if (attackingEntity instanceof EntityPlayer) { newValue *= 1.1d; }
		// target is tank
		if (attackingEntity.getHeldItemMainhand().getItem() instanceof ItemShield || attackingEntity.getHeldItemOffhand().getItem() instanceof ItemShield) {
			newValue *= 1.2d;
		}
		// or target is a damage dealer
		else if (source.isProjectile() || source.isMagicDamage()) { newValue *= 1.025d; }
		// is current target
		if (npc.getAttackTarget() != null && npc.getAttackTarget().equals(attackingEntity)) { newValue *= 1.05d; }
		// add
		Double oldValue = aggressors.get(attackingEntity);
		if (oldValue == null) { oldValue = 0.0d; }
		aggressors.put(attackingEntity, oldValue + newValue);
		lastDamages.put(attackingEntity, npc.world.getTotalWorldTime());
		if (priorityTarget == null) { priorityTarget = attackingEntity; }
	}

	public boolean isValidTarget(EntityLivingBase target) {
		return target != null && target.isEntityAlive() &&
				(!(target instanceof EntityPlayer) || !((EntityPlayer) target).capabilities.disableDamage) &&
				npc.isInRange(target, npc.stats.aggroRange) &&
				npc.world.provider.getDimension() == target.world.provider.getDimension();
	}

	public void reset() {
		combatResetTimer = 0;
		delay = 10;
		onlyPlayers = false;
		aggressors.clear();
		lastDamages.clear();
		priorityTarget = null;
		npc.getDataManager().set(EntityNPCInterface.Attacking, false);
	}

	private boolean shouldCombatContinue() {
		return npc.getAttackTarget() != null && isValidTarget(npc.getAttackTarget());
	}

	public void start() {
		combatResetTimer = 0;
		npc.getDataManager().set(EntityNPCInterface.Attacking, true);
		for (AbstractAbility ab : npc.abilities.abilities) {
			ab.startCombat();
		}
	}

	public void update() {
		if (npc.isKilled()) {
			if (npc.isAttacking()) { reset(); }
			return;
		}
		if (npc.getAttackTarget() != null && !npc.isAttacking()) { start(); }
		if (!shouldCombatContinue()) {
			if (combatResetTimer++ > 40) { reset(); }
			return;
		}
		combatResetTimer = 0;
		if (aggressors.isEmpty()) {
			delay = 10;
			return;
		}
		delay--;
		if (delay > 0) { return; }
		delay = 10;
		List<EntityLivingBase> del = new ArrayList<>();
		double maxValue = Double.MIN_VALUE;
		priorityTarget = null;
		double maxDist = npc.stats.aggroRange * 2.0d;
		for (EntityLivingBase entity : aggressors.keySet()) {
			if (!isValidTarget(entity)) {
				del.add(entity);
				continue;
			}
			if (!Util.instance.canMoveEntityToEntity(npc, entity)) { continue; }
			double d = npc.getDistance(entity.posX, entity.posY, entity.posZ);
			if (d > maxDist) { del.add(entity); }
			if (maxValue == Double.MIN_VALUE || aggressors.get(entity) >= maxValue) {
				maxValue = aggressors.get(entity);
				priorityTarget = entity;
			}
		}
		for (EntityLivingBase entity : del) { aggressors.remove(entity); }
		// set priority target
		if (priorityTarget != null && (npc.getAttackTarget() == null || !npc.getAttackTarget().equals(priorityTarget))) {
			npc.setPriorityAttackTarget(priorityTarget);
			npc.getNavigator().tryMoveToEntityLiving(priorityTarget, 1.5);
			delay = 60;
		}
	}

	public boolean canDamage(DamageSource damagesource, float amount) {
		Entity entity = NoppesUtilServer.GetDamageSource(damagesource);
		if (!(entity instanceof EntityLivingBase)) {
			if (npc.ais.getMaxHurtResistantTime() != 0 && npc.hurtResistantTime > npc.ais.getMaxHurtResistantTime() / 2.0F) {
                return amount > ((IEntityLivingBaseMixin) npc).npcs$getLastDamage();
			}
			return true;
		}
		if (!lastDamages.containsKey(entity) || npc.ais.getMaxHurtResistantTime() == 0 || (lastDamages.get(entity) + npc.ais.getMaxHurtResistantTime() / 2) < npc.world.getTotalWorldTime()) {
			lastDamages.put((EntityLivingBase) entity, npc.world.getTotalWorldTime());
			return true;
		}
		return false;
	}

}
