package noppes.npcs.ai.target;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
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
	public void resetTask() {}
	
	@Override
	public boolean shouldExecute() {
		if (this.map.isEmpty()) { this.target = null; }
		if (this.delay == 0) { this.delay = 7; }
		this.delay --;
		return this.delay==0 && !this.map.isEmpty();
	}

	@Override
	public void startExecuting() {
		this.target = null;
		Map<EntityLivingBase, Double> tempMap = Maps.<EntityLivingBase, Double>newHashMap();
		List<EntityLivingBase> del = Lists.<EntityLivingBase>newArrayList();
		double minDist = Double.MAX_VALUE, maxDist = Double.MIN_VALUE;
		// collect possible options
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
			if (maxDist == Double.MIN_VALUE || dist > maxDist) { maxDist = dist; }
			if (minDist == Double.MAX_VALUE || dist < minDist) { minDist = dist; }
		}
		for (EntityLivingBase entity : del) { this.map.remove(entity); }
		// choose the best target
		if (!tempMap.isEmpty()) {
			if (minDist < 0.25d) { minDist = 0.25d; }
			if (minDist == maxDist) { maxDist += 0.5d; }
			double a = 0.25d / (minDist - maxDist);
			double b = 1.25d - a * minDist;
			double maxValue = Double.MIN_VALUE;
			for (EntityLivingBase entity : tempMap.keySet()) {
				double dist = tempMap.get(entity);
				double value = this.map.get(entity) * (a * dist + b); // max 1.25 min 1.0
				if (entity.getHeldItemMainhand().getItem() instanceof ItemShield || entity.getHeldItemOffhand().getItem() instanceof ItemShield) {
					value *= 1.5d;
				}
				else if (entity.getHeldItemMainhand().getItem() instanceof ItemBow || entity.getHeldItemOffhand().getItem() instanceof ItemBow) {
					value *= 1.025d;
				}
				if (this.npc.getAttackTarget() != null && this.npc.getAttackTarget().equals(entity)) { value *= 1.25d; }
				if (maxValue == Double.MIN_VALUE || value >= maxValue) {
					maxValue = value;
					this.target = entity;
				}
			}
		}
		// set target
		if (this.target != null && (this.npc.getAttackTarget() == null || !this.npc.getAttackTarget().equals(this.target))) {
			this.npc.setAttackTarget(this.target);
			this.delay = 60;
		}
	}

	public void addDamageFromEntity(EntityLivingBase attackingEntity, double damage) {
		if (attackingEntity == null || (attackingEntity instanceof EntityPlayer && ((EntityPlayer) attackingEntity).capabilities.disableDamage)) { return; }
		if (damage <= 0.25d) { damage = 0.25d; }
		if (attackingEntity instanceof EntityPlayer) {
			if (this.npc.faction.isAggressiveToPlayer((EntityPlayer) attackingEntity)) { damage *= 1.2d; }
		}
		else if (attackingEntity instanceof EntityNPCInterface) {
			if (this.npc.faction.isAggressiveToNpc((EntityNPCInterface) attackingEntity)) { damage *= 1.2d; }
		}
		if (!this.map.containsKey(attackingEntity)) {
			this.map.put(attackingEntity, damage);
			return;
		}
		this.map.put(attackingEntity, this.map.get(attackingEntity) + damage);
	}
	
}
