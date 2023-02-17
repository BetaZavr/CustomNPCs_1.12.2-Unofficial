package noppes.npcs.ai;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.entity.EntityNPCInterface;

public class CombatHandler {
	private Map<EntityLivingBase, Float> aggressors;
	private int combatResetTimer;
	private EntityNPCInterface npc;

	public CombatHandler(EntityNPCInterface npc) {
		this.aggressors = new HashMap<EntityLivingBase, Float>();
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
		Entity e = NoppesUtilServer.GetDamageSourcee(source);
		if (e instanceof EntityLivingBase) {
			EntityLivingBase el = (EntityLivingBase) e;
			Float f = this.aggressors.get(el);
			if (f == null) {
				f = 0.0f;
			}
			this.aggressors.put(el, f + damageAmount);
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
		this.npc.getDataManager().set(EntityNPCInterface.Attacking, false);
	}

	private boolean shouldCombatContinue() {
		return this.npc.getAttackTarget() != null && this.isValidTarget(this.npc.getAttackTarget());
	}

	public void start() {
		this.combatResetTimer = 0;
		this.npc.world.getWorldInfo().getWorldTotalTime();
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
}
