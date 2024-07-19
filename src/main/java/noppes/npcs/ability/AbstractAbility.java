package noppes.npcs.ability;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class AbstractAbility implements IAbility {

	private long cooldown;
	private final int cooldownTime;
	public float maxHP;
	public float minHP;
	protected EntityNPCInterface npc;
	private final int startCooldownTime;

	public AbstractAbility(EntityNPCInterface npc) {
		this.cooldown = 0L;
		this.cooldownTime = 10;
		this.startCooldownTime = 10;
		this.maxHP = 1.0f;
		this.minHP = 0.0f;
		this.npc = npc;
	}

	@Override
	public boolean canRun(EntityLivingBase target) {
		if (this.onCooldown()) {
			return false;
		}
		float f = this.npc.getHealth() / this.npc.getMaxHealth();
		return f >= this.minHP && f <= this.maxHP
				&& (this.getRNG() <= 1 || this.npc.getRNG().nextInt(this.getRNG()) == 0)
				&& this.npc.canEntityBeSeen(target);
	}

	@Override
	public void endAbility() {
		this.cooldown = System.currentTimeMillis() + this.cooldownTime * 1000L;
	}

	@Override
	public int getRNG() {
		return 0;
	}

	public abstract boolean isType(EnumAbilityType type);

	private boolean onCooldown() {
		return System.currentTimeMillis() < this.cooldown;
	}

	@Override
	public void startCombat() {
		this.cooldown = System.currentTimeMillis() + this.startCooldownTime * 1000L;
	}
}
