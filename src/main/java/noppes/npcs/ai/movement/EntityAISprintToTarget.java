package noppes.npcs.ai.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import noppes.npcs.ai.attack.EntityAIHitAndRun;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.mixin.entity.IEntityMixin;

import java.util.Objects;

public class EntityAISprintToTarget extends EntityAIBase {

	private final EntityNPCInterface npc;

    public EntityAISprintToTarget(EntityNPCInterface npc) {
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	public void resetTask() {
		this.npc.setSprinting(false);
	}

	public boolean shouldContinueExecuting() {
		return this.npc.isEntityAlive() && this.npc.ais.canSprint;
	}

	public boolean shouldExecute() {
        EntityLivingBase target = this.npc.getAttackTarget();
		if (target != null && target.isEntityAlive() && this.npc.hurtTime <= 0 && !this.npc.getNavigator().noPath()) {
			this.startExecuting();
		} else {
			EntityDataManager dataManager = ((IEntityMixin) this.npc).npcs$getDataManager();
			DataParameter<Byte> FLAGS = ((IEntityMixin) this.npc).npcs$getFLAGS();
			if (dataManager != null && FLAGS != null && (dataManager.get(FLAGS) & 1 << 3) != 0) {
				this.npc.setSprinting(false);
			}
		}
		return false;
	}

	public void startExecuting() {
		boolean isSprint = this.npc.aiAttackTarget instanceof EntityAIHitAndRun;
		if (!isSprint) {
			switch (this.npc.ais.onAttack) {
				case 0:
					isSprint = !this.npc.isInRange(Objects.requireNonNull(this.npc.getAttackTarget()), (double) this.npc.stats.aggroRange / 3.0d);
					break; // Attack
				case 1:
					isSprint = true;
					break; // Panic
				case 2:
					isSprint = this.npc.isInRange(Objects.requireNonNull(this.npc.getAttackTarget()), this.npc.stats.aggroRange);
					break; // Avoid
				default:
					break;
			}
		}
		this.npc.setSprinting(isSprint);
	}
}
