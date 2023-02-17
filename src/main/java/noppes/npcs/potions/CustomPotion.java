package noppes.npcs.potions;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
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
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPotion;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;

public class CustomPotion
extends Potion
implements IPotion {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected ResourceLocation resource;
	protected ItemStack cureItem = ItemStack.EMPTY;
	
	public CustomPotion(NBTTagCompound nbtPotion) {
		super(nbtPotion.getBoolean("IsBadEffect"), nbtPotion.getInteger("LiquidColor"));
		this.nbtData = nbtPotion;
		String name = nbtPotion.getString("RegistryName").toLowerCase();
		this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, "custom_potion_"+name));
		this.setPotionName("effect."+name);
		if (nbtPotion.getBoolean("IsBeneficial")) { this.setBeneficial(); }
		if (nbtPotion.hasKey("CureItem", 10)) { this.cureItem = new ItemStack(nbtPotion.getCompoundTag("CureItem")); }
		
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
			isReady = event.ready;
		}
		return isReady;
	}

	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
		EventHooks.onCustomPotionPerformEffect(new PerformEffect(this, entityLivingBaseIn, amplifier));
	}

	@Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		EventHooks.onCustomPotionAffectEntity(new AffectEntity(this, source, indirectSource, entityLivingBaseIn, amplifier, health));
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
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }

	@Override
	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
		EventHooks.onCustomPotionEndEffect(new EndEffect(this, entityLivingBaseIn, amplifier));
	}

	@Override
	public java.util.List<net.minecraft.item.ItemStack> getCurativeItems() {
		List<ItemStack> ret = Lists.<ItemStack>newArrayList();
		if (!this.cureItem.isEmpty()) { ret.add(this.cureItem); }
		else { ret.add(new ItemStack(Items.MILK_BUCKET)); }
        return ret;
    }
}
