package noppes.npcs.api.wrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.data.AttributeWrapper;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.api.mixin.entity.IEntityTrackerMixin;
import noppes.npcs.api.mixin.entity.ai.attributes.IAbstractAttributeMapMixin;

@SuppressWarnings("rawtypes")
public class EntityLivingBaseWrapper<T extends EntityLivingBase> extends EntityWrapper<T> implements IEntityLivingBase {

	public EntityLivingBaseWrapper(T entity) {
		super(entity);
	}

	@Override
	public INpcAttribute addAttribute(INpcAttribute attribute) {
		if (attribute == null || this.hasAttribute(attribute)) {
			return null;
		}
		IAttribute attr = null;
		if (attribute.getMCAttribute() instanceof IAttribute) {
			attr = (IAttribute) attribute.getMCAttribute();
		} else if (attribute.getMCBaseAttribute() != null) {
			attr = attribute.getMCBaseAttribute();
		}
		if (attr == null) {
			return null;
		}
		this.entity.getAttributeMap().registerAttribute(attr);
		INpcAttribute npcAttr = this.getIAttribute(attribute.getName());
		if (npcAttr != null) {
			((AttributeWrapper) npcAttr).setCustom(true);
		}
		return npcAttr;
	}

	@Override
	public INpcAttribute addAttribute(String attributeName, String displayName, double baseValue, double minValue,
			double maxValue) {
		if (attributeName == null || attributeName.isEmpty() || this.hasAttribute(attributeName)) {
			return null;
		}
		return this.addAttribute(
				new AttributeWrapper(this.entity, attributeName, displayName, baseValue, minValue, maxValue));
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
			throw new CustomNPCsException("Wrong slot id:" + slot);
		}
		EntityEquipmentSlot s = this.getSlot(slot);
		if (s == null) { return ItemStackWrapper.AIR;}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.entity.getItemStackFromSlot(s));
	}

	private int getArmSwingAnimationEnd() {
		if (this.entity.isPotionActive(MobEffects.HASTE)) {
			return 6 - (1 + Objects.requireNonNull(this.entity.getActivePotionEffect(MobEffects.HASTE)).getAmplifier());
		} else {
			return this.entity.isPotionActive(MobEffects.MINING_FATIGUE)
					? 6 + (1 + Objects.requireNonNull(this.entity.getActivePotionEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) * 2
					: 6;
		}
	}

	@Override
	public IEntityLivingBase getAttackTarget() {
		return (IEntityLivingBase) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity.getRevengeTarget());
	}

	@Override
	public float getHealth() {
		return this.entity.getHealth();
	}

	@Override
	public INpcAttribute getIAttribute(String attributeName) {
		Map<String, IAttributeInstance> attributesByName = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getAttributesByName();
        if (attributesByName == null) { return null; }
        return Objects.requireNonNull(NpcAPI.Instance()).getIAttribute(attributesByName.get(attributeName));
	}

	@Override
	public String[] getIAttributeNames() {
		Map<String, IAttributeInstance> attributesByName = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getAttributesByName();
		if (attributesByName == null) { return new String[0]; }
        return attributesByName.keySet().toArray(new String[0]);
	}

	@Override
	public INpcAttribute[] getIAttributes() {
		List<INpcAttribute> list = Lists.newArrayList();
		for (IAttributeInstance attr : this.entity.getAttributeMap().getAllAttributes()) {
			list.add(Objects.requireNonNull(NpcAPI.Instance()).getIAttribute(attr));
		}
		return list.toArray(new INpcAttribute[0]);
	}

	@Override
	public IEntityLivingBase getLastAttacked() {
		return (IEntityLivingBase) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity.getLastAttackedEntity());
	}

	@Override
	public int getLastAttackedTime() {
		return this.entity.getLastAttackedEntityTime();
	}

	@Override
	public IItemStack getMainhandItem() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.entity.getHeldItemMainhand());
	}

	@Override
	public IMark[] getMarks() {
		MarkData data = MarkData.get(this.entity);
		return data.marks.toArray(new IMark[0]);
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
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.entity.getHeldItemOffhand());
	}

	@Override
	public int getPotionEffect(int effect) {
		PotionEffect pf = this.entity.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(effect)));
		if (pf == null) {
			return -1;
		}
		return pf.getAmplifier();
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
	public boolean hasAttribute(INpcAttribute attribute) {
		for (IAttributeInstance attr : this.entity.getAttributeMap().getAllAttributes()) {
			if (attr.equals(attribute.getMCAttribute())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		Map<String, IAttributeInstance> attributesByName = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getAttributesByName();
		if (attributesByName == null) { return false; }
		return attributesByName.containsKey(attributeName);
	}

	@Override
	public boolean isAttacking() {
		return this.entity.getRevengeTarget() != null;
	}

	@Override
	public boolean isChild() {
		return this.entity.isChild();
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean removeAttribute(INpcAttribute attribute) {
		if (attribute == null || !attribute.isCustom() || !this.hasAttribute(attribute)) {
			return false;
		}
		Map<IAttribute, IAttributeInstance> attributes = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getAttributes();
		Map<String, IAttributeInstance> attributesByName = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getAttributesByName();
		Multimap<IAttribute, IAttribute> descendantsByParent = ((IAbstractAttributeMapMixin) this.entity.getAttributeMap()).npcs$getDescendantsByParent();
		if (attributes == null || descendantsByParent == null || attributesByName == null) { return false; }
		IAttribute key = null;
		String name = null;
		IAttribute parent = null;
		for (IAttribute k : attributes.keySet()) {
			if (attributes.get(k).equals(attribute.getMCAttribute())) {
				key = k;
				break;
			}
		}
		if (key != null) {
			name = key.getName();
			for (IAttribute p : descendantsByParent.keySet()) {
				if (descendantsByParent.get(p).equals(key)) {
					parent = p;
					break;
				}
			}
		}
		attributes.remove(key);
		attributesByName.remove(name);
		if (parent != null) {
			descendantsByParent.remove(parent, key);
		}
		return true;
	}

	@Override
	public boolean removeAttribute(String attributeName) {
		return this.removeAttribute(this.getIAttribute(attributeName));
	}

	@Override
	public void removeMark(IMark mark) {
		MarkData data = MarkData.get(this.entity);
		data.marks.remove((MarkData.Mark) mark);
		data.syncClients();
	}

	@Override
	public void setArmor(int slot, IItemStack item) {
		if (slot < 0 || slot > 3) {
			throw new CustomNPCsException("Wrong slot id:" + slot);
		}
		EntityEquipmentSlot s = this.getSlot(slot);
		if (s != null) { this.entity.setItemStackToSlot(s, (item == null) ? ItemStack.EMPTY : item.getMCItemStack()); }
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

	private void swim(EnumHand hand) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			this.entity.swingArm(hand);
			return;
		}
		ItemStack stack = this.entity.getHeldItem(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().onEntitySwing(this.entity, stack)) {
				return;
			}
		}
		if (!this.entity.isSwingInProgress || this.entity.swingProgressInt >= this.getArmSwingAnimationEnd() / 2
				|| this.entity.swingProgressInt < 0) {
			this.entity.swingProgressInt = -1;
			this.entity.isSwingInProgress = true;
			this.entity.swingingHand = hand;
			SPacketAnimation pack = new SPacketAnimation(this.entity, hand == EnumHand.MAIN_HAND ? 0 : 3);
			IntHashMap<EntityTrackerEntry> trackedEntityHashTable = ((IEntityTrackerMixin) ((WorldServer) this.entity.world).getEntityTracker()).npcs$getTrackedEntityHashTable();
			EntityTrackerEntry entitytrackerentry = trackedEntityHashTable.lookup(this.entity.getEntityId());
			if (entitytrackerentry != null) {
				for (EntityPlayerMP entityplayermp : entitytrackerentry.trackingPlayers) {
					entityplayermp.connection.sendPacket(pack);
				}
				if (this.entity instanceof EntityPlayerMP && !entitytrackerentry.trackingPlayers.contains((EntityPlayerMP) this.entity)) {
					((EntityPlayerMP) this.entity).connection.sendPacket(pack);
				}
			}
		}
	}

	@Override
	public void swingMainhand() {
		this.swim(EnumHand.MAIN_HAND);
	}

	@Override
	public void swingOffhand() {
		this.swim(EnumHand.OFF_HAND);
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.LIVING.get() || super.typeOf(type);
	}

}
