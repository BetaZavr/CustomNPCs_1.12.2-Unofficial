package noppes.npcs.api.wrapper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.MarkData;

@SuppressWarnings("rawtypes")
public class EntityLivingBaseWrapper<T extends EntityLivingBase>
extends EntityWrapper<T>
implements IEntityLivingBase {
	
	public EntityLivingBaseWrapper(T entity) {
		super(entity);
	}

	@Override
	public IMark addMark(int type) {
		MarkData data = MarkData.get(this.entity);
		return data.addMark(type);
	}

	@Override
	public void addPotionEffect(int effect, int duration, int strength, boolean hideParticles) {
		Potion p = Potion.getPotionById(effect);
		if (p == null) {
			return;
		}
		if (strength < 0) {
			strength = 0;
		} else if (strength > 255) {
			strength = 255;
		}
		if (duration < 0) {
			duration = 0;
		} else if (duration > 1000000) {
			duration = 1000000;
		}
		if (!p.isInstant()) {
			duration *= 20;
		}
		if (duration == 0) {
			this.entity.removePotionEffect(p);
		} else {
			this.entity.addPotionEffect(new PotionEffect(p, duration, strength, false, hideParticles));
		}
	}

	@Override
	public boolean canSeeEntity(IEntity entity) {
		return this.entity.canEntityBeSeen(entity.getMCEntity());
	}

	@Override
	public void clearPotionEffects() {
		this.entity.clearActivePotions();
	}

	@Override
	public IItemStack getArmor(int slot) {
		if (slot < 0 || slot > 3) {
			throw new CustomNPCsException("Wrong slot id:" + slot, new Object[0]);
		}
		return NpcAPI.Instance().getIItemStack(this.entity.getItemStackFromSlot(this.getSlot(slot)));
	}

	@Override
	public IEntityLivingBase getAttackTarget() {
		return (IEntityLivingBase) NpcAPI.Instance().getIEntity(this.entity.getRevengeTarget());
	}

	@Override
	public float getHealth() {
		return this.entity.getHealth();
	}

	@Override
	public IEntityLivingBase getLastAttacked() {
		return (IEntityLivingBase) NpcAPI.Instance().getIEntity(this.entity.getLastAttackedEntity());
	}

	@Override
	public int getLastAttackedTime() {
		return this.entity.getLastAttackedEntityTime();
	}

	@Override
	public IItemStack getMainhandItem() {
		return NpcAPI.Instance().getIItemStack(this.entity.getHeldItemMainhand());
	}

	@Override
	public IMark[] getMarks() {
		MarkData data = MarkData.get(this.entity);
		return data.marks.toArray(new IMark[data.marks.size()]);
	}

	@Override
	public float getMaxHealth() {
		return this.entity.getMaxHealth();
	}

	@Override
	public float getMoveForward() {
		return this.entity.moveForward;
	}

	@Override
	public float getMoveStrafing() {
		return this.entity.moveStrafing;
	}

	@Override
	public float getMoveVertical() {
		return this.entity.moveVertical;
	}

	@Override
	public IItemStack getOffhandItem() {
		return NpcAPI.Instance().getIItemStack(this.entity.getHeldItemOffhand());
	}

	@Override
	public int getPotionEffect(int effect) {
		PotionEffect pf = this.entity.getActivePotionEffect(Potion.getPotionById(effect));
		if (pf == null) {
			return -1;
		}
		return pf.getAmplifier();
	}

	@Override
	public float getRotation() {
		return this.entity.renderYawOffset;
	}

	private EntityEquipmentSlot getSlot(int slot) {
		if (slot == 3) {
			return EntityEquipmentSlot.HEAD;
		}
		if (slot == 2) {
			return EntityEquipmentSlot.CHEST;
		}
		if (slot == 1) {
			return EntityEquipmentSlot.LEGS;
		}
		if (slot == 0) {
			return EntityEquipmentSlot.FEET;
		}
		return null;
	}

	@Override
	public int getType() {
		return EntityType.LIVING.get();
	}

	@Override
	public boolean isAttacking() {
		return this.entity.getRevengeTarget() != null;
	}

	@Override
	public boolean isChild() {
		return this.entity.isChild();
	}

	@Override
	public void removeMark(IMark mark) {
		MarkData data = MarkData.get(this.entity);
		data.marks.remove(mark);
		data.syncClients();
	}

	@Override
	public void setArmor(int slot, IItemStack item) {
		if (slot < 0 || slot > 3) {
			throw new CustomNPCsException("Wrong slot id:" + slot, new Object[0]);
		}
		this.entity.setItemStackToSlot(this.getSlot(slot), (item == null) ? ItemStack.EMPTY : item.getMCItemStack());
	}

	@Override
	public void setAttackTarget(IEntityLivingBase living) {
		if (living == null) {
			this.entity.setRevengeTarget(null);
		} else {
			this.entity.setRevengeTarget(living.getMCEntity());
		}
	}

	@Override
	public void setHealth(float health) {
		this.entity.setHealth(health);
	}

	@Override
	public void setMainhandItem(IItemStack item) {
		this.entity.setHeldItem(EnumHand.MAIN_HAND, (item == null) ? ItemStack.EMPTY : item.getMCItemStack());
	}

	@Override
	public void setMaxHealth(float health) {
		if (health < 0.0f) {
			return;
		}
		this.entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
	}

	@Override
	public void setMoveForward(float move) {
		this.entity.moveForward = move;
	}

	@Override
	public void setMoveStrafing(float move) {
		this.entity.moveStrafing = move;
	}

	@Override
	public void setMoveVertical(float move) {
		this.entity.moveVertical = move;
	}

	@Override
	public void setOffhandItem(IItemStack item) {
		this.entity.setHeldItem(EnumHand.OFF_HAND, (item == null) ? ItemStack.EMPTY : item.getMCItemStack());
	}

	@Override
	public void setRotation(float rotation) {
		this.entity.renderYawOffset = rotation;
	}

	@Override
	public void swingMainhand() { this.swim(EnumHand.MAIN_HAND); }

	@Override
	public void swingOffhand() { this.swim(EnumHand.OFF_HAND); }

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.LIVING.get() || super.typeOf(type);
	}
	
	private void swim(EnumHand hand) {
		//this.entity.swingArm(hand);
		ItemStack stack = this.entity.getHeldItem(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().onEntitySwing(this.entity, stack)) { return; }
		}
		if (!this.entity.isSwingInProgress || this.entity.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.entity.swingProgressInt < 0) {
			this.entity.swingProgressInt = -1;
			this.entity.isSwingInProgress = true;
			this.entity.swingingHand = hand;
			if (this.entity.world instanceof WorldServer) {
				((WorldServer)this.entity.world).getEntityTracker().sendToTracking(this.entity, new SPacketAnimation(this.entity, hand == EnumHand.MAIN_HAND ? 0 : 3));
			}
		}
	}
	
	private int getArmSwingAnimationEnd() {
		if (this.entity.isPotionActive(MobEffects.HASTE)) { return 6 - (1 + this.entity.getActivePotionEffect(MobEffects.HASTE).getAmplifier()); }
		else { return this.entity.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + this.entity.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6; }
	}
	
}
