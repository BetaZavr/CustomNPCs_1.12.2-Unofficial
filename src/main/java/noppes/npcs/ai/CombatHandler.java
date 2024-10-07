package noppes.npcs.ai;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.entity.IEntityLivingBaseMixin;

public class CombatHandler {

	public final Map<EntityLivingBase, Long> lastDamages = Maps.newHashMap();
	public final Map<EntityLivingBase, Float> aggressors = Maps.newHashMap();
	private int combatResetTimer;
	private final EntityNPCInterface npc;

	public CombatHandler(EntityNPCInterface npc) {
		this.combatResetTimer = 0;
		this.npc = npc;
	}

	public boolean checkTarget() {
		if (this.aggressors.isEmpty() || this.npc.ticksExisted % 10 != 0) {
			return false;
		}
		EntityLivingBase target = this.npc.getAttackTarget();
		Float current = 0.0f;

		if (this.isValidTarget(target)) {
			current = this.aggressors.get(target);
			if (current == null) {
				current = 0.0f;
			}
		} else {
			target = null;
		}

		for (Map.Entry<EntityLivingBase, Float> entry : this.aggressors.entrySet()) {
			if (entry.getValue() > current && this.isValidTarget(entry.getKey())) {
				current = entry.getValue();
				target = entry.getKey();
			}
		}
		return target == null;
	}

	public void damage(DamageSource source, float damageAmount) {
		this.combatResetTimer = 0;
		Entity e = NoppesUtilServer.GetDamageSource(source);
		if (e instanceof EntityLivingBase) {
			EntityLivingBase el = (EntityLivingBase) e;
			Float f = this.aggressors.get(el);
			if (f == null) { f = 0.0f; }
			this.aggressors.put(el, f + damageAmount);
			this.lastDamages.put(el, this.npc.world.getTotalWorldTime());
		}
	}

	public boolean isValidTarget(EntityLivingBase target) {
		return target != null && target.isEntityAlive()
				&& (!(target instanceof EntityPlayer) || !((EntityPlayer) target).capabilities.disableDamage)
				&& this.npc.isInRange(target, this.npc.stats.aggroRange);
	}

	public void reset() {
		this.combatResetTimer = 0;
		this.aggressors.clear();
		this.lastDamages.clear();
		this.npc.getDataManager().set(EntityNPCInterface.Attacking, false);
	}

	private boolean shouldCombatContinue() {
		return this.npc.getAttackTarget() != null && this.isValidTarget(this.npc.getAttackTarget());
	}

	public void start() {
		this.combatResetTimer = 0;
		this.npc.getDataManager().set(EntityNPCInterface.Attacking, true);
		for (AbstractAbility ab : this.npc.abilities.abilities) {
			ab.startCombat();
		}
	}

	public void update() {
		if (this.npc.isKilled()) {
			if (this.npc.isAttacking()) {
				this.reset();
			}
			return;
		}
		if (this.npc.getAttackTarget() != null && !this.npc.isAttacking()) {
			this.start();
		}
		if (!this.shouldCombatContinue()) {
			if (this.combatResetTimer++ > 40) {
				this.reset();
			}
			return;
		}
		this.combatResetTimer = 0;
	}

	public boolean canDamage(DamageSource damagesource, float amount) {
		Entity entity = NoppesUtilServer.GetDamageSource(damagesource);
		if (!(entity instanceof EntityLivingBase)) {
			if (this.npc.ais.getMaxHurtResistantTime() != 0 && this.npc.hurtResistantTime > this.npc.ais.getMaxHurtResistantTime() / 2.0F) {
                return amount > ((IEntityLivingBaseMixin) this.npc).npcs$getLastDamage();
			}
			return true;
		}
		if (!this.lastDamages.containsKey(entity) || this.npc.ais.getMaxHurtResistantTime() == 0 || (this.lastDamages.get(entity) + this.npc.ais.getMaxHurtResistantTime() / 2) < this.npc.world.getTotalWorldTime()) {
			this.lastDamages.put((EntityLivingBase) entity, this.npc.world.getTotalWorldTime());
			return true;
		}
		return false;
	}

}
