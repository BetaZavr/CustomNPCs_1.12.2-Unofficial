package noppes.npcs.ai.target;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAITargetController
extends EntityAIBase {
	
	private EntityNPCInterface npc;
	private int delay;
	protected EntityLivingBase target;
	public final Map<EntityLivingBase, Double> map;

	public EntityAITargetController(EntityNPCInterface npc) {
		this.npc = npc;
		this.map = Maps.<EntityLivingBase, Double>newHashMap();
	}
	
	@Override
	public void resetTask() {
		this.map.clear();
		this.delay = 0;
	}
	
	@Override
	public boolean shouldExecute() {
		if (this.map.isEmpty()) { this.target = null; }
		if (this.delay > 0) { this.delay --; }
		return this.delay==0 && !this.map.isEmpty() && this.npc.ticksExisted % 20 == 0;
	}

	@Override
	public void startExecuting() {
		this.target = null;
		Map<EntityLivingBase, Double> tempMap = Maps.<EntityLivingBase, Double>newHashMap();
		List<EntityLivingBase> del = Lists.<EntityLivingBase>newArrayList();
		double minDist = -1.0d, maxDist = -1.0d;
		for (EntityLivingBase entity : this.map.keySet()) {
			if (entity == null || !entity.isEntityAlive() || entity.world == null || entity.world.provider.getDimension() != this.npc.world.provider.getDimension()) {
				del.add(entity);
				continue;
			}
			double dist = this.npc.getDistance(entity.posX, entity.posY, entity.posZ);
			if (dist > 2.0d * CustomNpcs.NpcNavRange) {
				del.add(entity);
				continue;
			}
			tempMap.put(entity, dist);
			if (maxDist < 0 || dist > maxDist) { maxDist = dist; }
			if (minDist < 0 || dist < minDist) { minDist = dist; }
		}
		for (EntityLivingBase entity : del) { this.map.remove(entity); }
		if (!tempMap.isEmpty()) {
			if (minDist < 0.25d) { minDist = 0.25d; }
			double a = 0.25d / (minDist - maxDist);
			double b = 1.25d - a * minDist;
			double maxValue = -1.0d;
			for (EntityLivingBase entity : tempMap.keySet()) {
				double dist = tempMap.get(entity);
				double value = this.map.get(entity) * (a * dist + b); // max 1.25 min 1.0
				if (entity.getHeldItemMainhand().getItem() instanceof ItemShield || entity.getHeldItemOffhand().getItem() instanceof ItemShield) {
					value *= 1.5d;
				}
				if (entity.getHeldItemMainhand().getItem() instanceof ItemBow) {
					value *= 1.025d;
				}
				if (maxValue <0 || value >= maxValue) {
					maxValue = value;
					this.target = entity;
				}
			}
		}
		if (this.target != null && !this.npc.getAttackTarget().equals(this.target)) {
			this.npc.setAttackTarget(this.target);
			this.delay = 60;
		}
	}

	public void addDamageFromEntity(EntityLivingBase attackingEntity, double damage) {
		if (!this.map.containsKey(attackingEntity)) {
			this.map.put(attackingEntity, damage);
			return;
		}
		this.map.put(attackingEntity, this.map.get(attackingEntity) + damage);
	}
	
}
