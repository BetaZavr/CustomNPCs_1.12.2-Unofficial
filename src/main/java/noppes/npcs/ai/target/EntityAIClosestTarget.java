package noppes.npcs.ai.target;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIClosestTarget extends EntityAITarget {

	public EntityNPCInterface npc;
	private int targetChance;
	private Class<EntityLivingBase> targetClass;
	private EntityLivingBase targetEntity;
	private Predicate<EntityLivingBase> targetEntitySelector;
	private EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;

	public EntityAIClosestTarget(EntityNPCInterface npc, Class<EntityLivingBase> targetClass, int targetChance, boolean directLOS, boolean onlyNearby, Predicate<EntityLivingBase> attackEntitySelector) {
		super((EntityCreature) npc, directLOS, onlyNearby);
		this.targetClass = targetClass;
		this.targetChance = targetChance;
		this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(npc);
		this.setMutexBits(1);
		this.targetEntitySelector = attackEntitySelector;
		this.npc = npc;
	}

	public boolean shouldExecute() {
		if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
			return false;
		}
		try {
			double dist = this.getTargetDistance();
			List<EntityLivingBase> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass,
					this.taskOwner.getEntityBoundingBox().grow(dist, MathHelper.ceil(dist / 2.0), dist),
					this.targetEntitySelector);
			Collections.sort(list, this.theNearestAttackableTargetSorter);
			if (list.isEmpty()) {
				return false;
			}
			this.targetEntity = list.get(0);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.targetEntity);
		if (this.targetEntity instanceof EntityMob && ((EntityMob) this.targetEntity).getAttackTarget() == null) {
			((EntityMob) this.targetEntity).setAttackTarget(this.taskOwner);
		}
		super.startExecuting();
	}
}
