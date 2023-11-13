package noppes.npcs.potions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.ValueUtil;

public class CustomPotion
extends Potion
implements ICustomElement {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected ResourceLocation resource;
	protected ItemStack cureItem = ItemStack.EMPTY;
	protected final Map<IAttribute, AttributeModifier> attributeModifierMap = Maps.<IAttribute, AttributeModifier>newHashMap(); // RangedAttribute, AttributeModifier
	
	public CustomPotion(NBTTagCompound nbtPotion) {
		super(nbtPotion.getBoolean("IsBadEffect"), nbtPotion.getInteger("LiquidColor"));
		this.nbtData = nbtPotion;
		String name = nbtPotion.getString("RegistryName").toLowerCase();
		this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, "custom_potion_"+name));
		this.setPotionName("effect."+name);
		if (nbtPotion.getBoolean("IsBeneficial")) { this.setBeneficial(); }
		if (nbtPotion.hasKey("CureItem", 10)) { this.cureItem = new ItemStack(nbtPotion.getCompoundTag("CureItem")); }
		if (nbtPotion.hasKey("Modifiers", 10)) {
			this.attributeModifierMap.clear();
			for (int i = 0; i < nbtPotion.getTagList("Modifiers", 10).tagCount(); i++) {
				NBTTagCompound potionModifier = nbtPotion.getTagList("Modifiers", 10).getCompoundTagAt(i);
				try {
					double d = potionModifier.getDouble("AttributeDefValue");
					double m = potionModifier.getDouble("AttributeMinValue");
					double n = potionModifier.getDouble("AttributeMaxValue");
					UUID uuid;
					try { uuid = UUID.fromString(potionModifier.getString("UUID")); }
					catch (Exception e) { uuid = UUID.randomUUID(); }
					this.attributeModifierMap.put(new RangedAttribute((IAttribute) null, potionModifier.getString("AttributeName"), ValueUtil.correctDouble(d, m, n), ValueUtil.min(m, n), ValueUtil.max(m, n)),
							new AttributeModifier(uuid, this.getName(), potionModifier.getDouble("Ammount"), potionModifier.getInteger("Operation")));
				}
				catch (Exception e) {
					LogWriter.error("Error create or added attribute modifier #"+i+" to custom potion: \""+this.getCustomName()+"\"", e);
				}
			}
		}
		
		this.resource = new ResourceLocation(CustomNpcs.MODID, "textures/potions/"+name+".png");
	}
	
	@Override
	public boolean isReady(int duration, int amplifier) {
		boolean isReady = true;
		if (this.nbtData.hasKey("Duration", 3)) {
			isReady = duration % this.nbtData.getInteger("Duration") == 0;
		}
		if (isReady || duration%10==0) {
			IsReadyEvent event = new IsReadyEvent(this, isReady, duration, amplifier);
			EventHooks.onCustomPotionIsReady(event);
			EventHooks.onEvent(ScriptController.Instance.potionScripts, "customPotionIsReady", event);
			isReady = event.ready;
		}
		return isReady;
	}

	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
		PerformEffect event = new PerformEffect(this, entityLivingBaseIn, amplifier);
		EventHooks.onCustomPotionPerformEffect(event);
		EventHooks.onEvent(ScriptController.Instance.potionScripts, "customPotionPerformEffect", event);
	}

	@Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		AffectEntity event = new AffectEntity(this, source, indirectSource, entityLivingBaseIn, amplifier, health);
		EventHooks.onCustomPotionAffectEntity(event);
		EventHooks.onEvent(ScriptController.Instance.potionScripts, "customPotionAffectEntity", event);
	}
	
	@Override
	public boolean hasStatusIcon() { return false; }
	
	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		if (mc.currentScreen != null) {
			mc.getTextureManager().bindTexture(this.resource);
			Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		mc.getTextureManager().bindTexture(this.resource);
		Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}

	@Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		for (Entry<IAttribute, AttributeModifier> entry : this.attributeModifierMap.entrySet()) {
			IAttributeInstance iattributeinstance = attributeMapIn.getAttributeInstance(entry.getKey());
			if (iattributeinstance != null) {
				AttributeModifier attributemodifier = entry.getValue();
				iattributeinstance.removeModifier(attributemodifier);
				iattributeinstance.applyModifier(new AttributeModifier(attributemodifier.getID(), this.getName() + " " + amplifier, this.getAttributeModifierAmount(amplifier, attributemodifier), attributemodifier.getOperation()));
			}
		}
	}

	@Override
	public Potion registerPotionAttributeModifier(IAttribute attribute, String uniqueId, double ammount, int operation) {
		AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(uniqueId), this.getName(), ammount, operation);
		this.attributeModifierMap.put(attribute, attributemodifier);
		return this;
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		for (Entry<IAttribute, AttributeModifier> entry : this.attributeModifierMap.entrySet()) {
			IAttributeInstance iattributeinstance = attributeMapIn.getAttributeInstance(entry.getKey());
			if (iattributeinstance != null) {
				iattributeinstance.removeModifier(entry.getValue());
			}
		}
		EndEffect event = new EndEffect(this, entityLivingBaseIn, amplifier);
		EventHooks.onCustomPotionEndEffect(event);
		EventHooks.onEvent(ScriptController.Instance.potionScripts, "customPotionEndEffect", event);
	}
	
	@Override
	public java.util.List<net.minecraft.item.ItemStack> getCurativeItems() {
		List<ItemStack> ret = Lists.<ItemStack>newArrayList();
		if (!this.cureItem.isEmpty()) { ret.add(this.cureItem); }
		else { ret.add(new ItemStack(Items.MILK_BUCKET)); }
		return ret;
	}
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }

	@SideOnly(Side.CLIENT)
	public Map<IAttribute, AttributeModifier> getAttributeModifierMap() { return this.attributeModifierMap; }
	
}
