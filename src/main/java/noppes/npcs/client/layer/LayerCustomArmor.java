package noppes.npcs.client.layer;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import noppes.npcs.client.model.ModelBipedAlt;

public class LayerCustomArmor<T extends EntityLivingBase>
extends LayerInterface<T> {

	protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	protected ModelBipedAlt modelLeggings;
	protected ModelBipedAlt modelArmor;
	private boolean skipRenderGlint;
	private static final Map<String, ResourceLocation> ARMOR_TEXTURE_RES_MAP = Maps.<String, ResourceLocation>newHashMap();
	
	public LayerCustomArmor(RenderLiving<?> render) {
		super(render);
		this.modelLeggings = new ModelBipedAlt(0.5F);
		this.modelArmor = new ModelBipedAlt(1.0F);
	}
	
	protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slot) {
		this.setModelVisible(model);
		switch (slot) {
			case HEAD:
				model.bipedHead.showModel = true;
				model.bipedHeadwear.showModel = true;
				break;
			case CHEST:
				model.bipedBody.showModel = true;
				model.bipedRightArm.showModel = true;
				model.bipedLeftArm.showModel = true;
				break;
			case LEGS:
				model.bipedBody.showModel = true;
				model.bipedRightLeg.showModel = true;
				model.bipedLeftLeg.showModel = true;
				break;
			case FEET:
				model.bipedRightLeg.showModel = true;
				model.bipedLeftLeg.showModel = true;
			default: break;
		}
	}

	protected void setModelVisible(ModelBiped model) { model.setVisible(false); }

	protected ModelBiped getArmorModelHook(EntityLivingBase entity, ItemStack itemStack, EntityEquipmentSlot slot, ModelBiped model) { return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model); }

	public boolean shouldCombineTextures() { return false; }

	private void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slot) {
		ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slot);
		if (!(itemstack.getItem() instanceof ItemArmor)) { return; }
		ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
		if (itemarmor.getEquipmentSlot() != slot) { return; }
		
		ModelBiped t = getArmorModelHook(entityLivingBaseIn, itemstack, slot, this.getModelFromSlot(slot));
		t.setModelAttributes(this.render.getMainModel());
		t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
		this.setModelSlotVisible(t, slot);
		this.render.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slot, null));
		{
			if (itemarmor.hasOverlay(itemstack)) {
				int i = itemarmor.getColor(itemstack);
				float f = (float)(i >> 16 & 255) / 255.0F;
				float f1 = (float)(i >> 8 & 255) / 255.0F;
				float f2 = (float)(i & 255) / 255.0F;
				GlStateManager.color(f, f1, f2, 1.0F);
				this.renderPart(t, slot, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				this.render.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slot, "overlay"));
			}
			{
				this.renderPart(t, slot, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
			if (!this.skipRenderGlint && itemstack.hasEffect()) { this.renderEnchantedGlint(this.render, entityLivingBaseIn, t, slot, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale); }
		}
	}

	private void renderPart(ModelBiped model, EntityEquipmentSlot slot, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, this.npc);
		GlStateManager.pushMatrix();

		if (this.npc.isChild()) {
			if (slot == EntityEquipmentSlot.HEAD) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
			} else {
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			}
		} else {
			if (this.npc.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
		}
		switch (slot) {
			case HEAD:
				if (this.model.bipedHead.showModel) {
					model.bipedHead.render(scale);
					model.bipedHeadwear.render(scale);
				}
				break;
			case CHEST:
				if (this.model.bipedBody.showModel) { model.bipedBody.render(scale); }
				if (this.model.bipedRightArm.showModel) { model.bipedRightArm.render(scale); }
				if (this.model.bipedLeftArm.showModel) { model.bipedLeftArm.render(scale); }
				break;
			case LEGS:
				if (this.model.bipedBody.showModel) { model.bipedBody.render(scale); }
				if (this.model.bipedRightLeg.showModel) { model.bipedRightLeg.render(scale); }
				if (this.model.bipedLeftLeg.showModel) { model.bipedLeftLeg.render(scale); }
				break;
			case FEET:
				if (this.model.bipedRightLeg.showModel) { model.bipedRightLeg.render(scale); }
				if (this.model.bipedLeftLeg.showModel) { model.bipedLeftLeg.render(scale); }
			default: break;
		}
		GlStateManager.popMatrix();
	}

	public ModelBipedAlt getModelFromSlot(EntityEquipmentSlot slotIn) { return this.isLegSlot(slotIn) ? this.modelLeggings : this.modelArmor; }

	private boolean isLegSlot(EntityEquipmentSlot slotIn) { return slotIn == EntityEquipmentSlot.LEGS; }

	public void renderEnchantedGlint(RenderLivingBase<?> render, EntityLivingBase entity, ModelBiped model, EntityEquipmentSlot slot, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		float f = (float) entity.ticksExisted + partialTicks;
		render.bindTexture(ENCHANTED_ITEM_GLINT_RES);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(false);
		GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
			GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
			GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			this.renderPart(model, slot, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			//model.render(entity, p_188364_3_, p_188364_4_, p_188364_6_, p_188364_7_, p_188364_8_, scale);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
	}

	@Deprecated
	public ResourceLocation getArmorResource(ItemArmor armor, boolean p_177181_2_) { return this.getArmorResource(armor, p_177181_2_, (String)null); }

	@Deprecated
	public ResourceLocation getArmorResource(ItemArmor armor, boolean p_177178_2_, String p_177178_3_) {
		String s = String.format("textures/models/armor/%s_layer_%d%s.png", armor.getArmorMaterial().getName(), p_177178_2_ ? 2 : 1, p_177178_3_ == null ? "" : String.format("_%s", p_177178_3_));
		ResourceLocation resourcelocation = ARMOR_TEXTURE_RES_MAP.get(s);
		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s);
			ARMOR_TEXTURE_RES_MAP.put(s, resourcelocation);
		}
		return resourcelocation;
	}

	public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EntityEquipmentSlot slot, String type) {
		ItemArmor item = (ItemArmor)stack.getItem();
		String texture = item.getArmorMaterial().getName();
		String domain = "minecraft";
		int idx = texture.indexOf(':');
		if (idx != -1) {
			domain = texture.substring(0, idx);
			texture = texture.substring(idx + 1);
		}
		String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (isLegSlot(slot) ? 2 : 1), type == null ? "" : String.format("_%s", type));
		s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
		ResourceLocation resourcelocation = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(s1);
		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s1);
			ARMOR_TEXTURE_RES_MAP.put(s1, resourcelocation);
		}
		return resourcelocation;
	}

	@Override
	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) { }
	
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.pushMatrix();
		this.renderArmorLayer(this.npc, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		this.renderArmorLayer(this.npc, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		this.renderArmorLayer(this.npc, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		this.renderArmorLayer(this.npc, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
		GlStateManager.popMatrix();
	}

	@Override
	public void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) { }
	
}
