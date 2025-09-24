package noppes.npcs.ability;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class AbstractAbility implements IAbility {

	private long cooldown = 0L;
	public float maxHP = 1.0f;
	public float minHP = 1.0f;
	protected EntityNPCInterface npc;

	public AbstractAbility(EntityNPCInterface npc) {
		this.npc = npc;
	}

	@Override
	public boolean canRun(EntityLivingBase target) {
		if (onCooldown()) {
			return false;
		}
		float f = npc.getHealth() / npc.getMaxHealth();
		return f >= minHP && f <= maxHP
				&& (getRNG() <= 1 || npc.getRNG().nextInt(getRNG()) == 0)
				&& npc.canSee(target);
	}

	@Override
	public void endAbility() {
		cooldown = System.currentTimeMillis() + npc.ais.getMaxHurtResistantTime() * 1000L;
	}

	@Override
	public int getRNG() {
		return 0;
	}

	public abstract boolean isType(EnumAbilityType type);

	private boolean onCooldown() {
		return System.currentTimeMillis() < cooldown;
	}

	@Override
	public void startCombat() {
		cooldown = System.currentTimeMillis() + npc.ais.getMaxHurtResistantTime() * 1000L;
	}
}
