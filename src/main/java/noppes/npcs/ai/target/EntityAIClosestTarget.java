package noppes.npcs.ai.target;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class EntityAIClosestTarget extends EntityAITarget {

	private final int targetChance;
	private EntityLivingBase targetEntity;
	private final Predicate<EntityLivingBase> targetEntitySelector;
	private final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;

	public EntityAIClosestTarget(EntityNPCInterface npcIn, int targetChanceIn, boolean directLOS, boolean onlyNearby, Predicate<EntityLivingBase> attackEntitySelector) {
		super(npcIn, directLOS, onlyNearby);
		targetChance = targetChanceIn;
		theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(npcIn);
        setMutexBits(1);
		targetEntitySelector = attackEntitySelector;
	}

	public boolean shouldExecute() {
		if ((targetChance > 0 && taskOwner.getRNG().nextInt(targetChance) != 0)) { return false; }
		CustomNPCsScheduler.runTack(() -> {
			CustomNpcs.debugData.start(taskOwner);
			try {
				double dist = getTargetDistance();

				List<EntityLivingBase> list = new ArrayList<>();
				for (Entity entity : new ArrayList<>(taskOwner.world.loadedEntityList)) {
					if (!(entity instanceof EntityLivingBase) ||
							!entity.isEntityAlive() ||
							taskOwner.getDistance(entity) > dist ||
							!targetEntitySelector.apply((EntityLivingBase) entity))
					{ continue; }
					list.add((EntityLivingBase) entity);
				}

				if (!list.isEmpty()) {
					list.sort(theNearestAttackableTargetSorter);
					targetEntity = list.get(0);
					taskOwner.setAttackTarget(targetEntity);
					if (targetEntity instanceof EntityMob && ((EntityMob) targetEntity).getAttackTarget() == null) {
						((EntityMob) targetEntity).setAttackTarget(taskOwner);
					}
					super.startExecuting();
				}
			} catch (Exception e) { LogWriter.error(e); }
			CustomNpcs.debugData.end(taskOwner);
		});
		return false;
	}

}
