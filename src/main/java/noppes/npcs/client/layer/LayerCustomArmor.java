package noppes.npcs.client.layer;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.entity.EntityCustomNpc;

public class LayerCustomArmor<T extends ModelBase> extends LayerArmorBase<ModelBiped> {

	private final RenderCustomNpc<EntityCustomNpc> renderer;
	private float alpha = 1.0F;
	private float colorR = 1.0F;
	private float colorG = 1.0F;
	private float colorB = 1.0F;
	private boolean skipRenderGlint;

	public LayerCustomArmor(RenderCustomNpc<EntityCustomNpc> rendererIn) {
		super(rendererIn);
		renderer = rendererIn;
		this.modelLeggings = new ModelBipedAlt(0.5F);
		this.modelArmor = new ModelBipedAlt(1.0F);
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
				headPitch, scale, EntityEquipmentSlot.CHEST);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
				headPitch, scale, EntityEquipmentSlot.LEGS);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
				headPitch, scale, EntityEquipmentSlot.FEET);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
				headPitch, scale, EntityEquipmentSlot.HEAD);
	}

	@Override
	protected ModelBiped getArmorModelHook(net.minecraft.entity.EntityLivingBase entity,
			net.minecraft.item.ItemStack itemStack, EntityEquipmentSlot slot, ModelBiped model) {
		return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
	}

	protected void initArmor() {
		this.modelLeggings = new ModelBipedAlt(0.5F);
		this.modelArmor = new ModelBipedAlt(1.0F);
	}

	protected boolean isLegSlot(EntityEquipmentSlot slotIn) {
		return slotIn == EntityEquipmentSlot.LEGS;
	}

	@SuppressWarnings("unchecked")
	protected void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale,
			EntityEquipmentSlot slotIn) {
		ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slotIn);
		if (!(itemstack.getItem() instanceof ItemArmor)) {
			return;
		}
		ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
		if (itemarmor.getEquipmentSlot() != slotIn) {
			return;
		}
		T t = (T) this.getModelFromSlot(slotIn);
		t = (T) getArmorModelHook(entityLivingBaseIn, itemstack, slotIn, (ModelBiped) t);
		boolean isAW = t.getClass().getName().indexOf("armourers_workshop") > -1;
		boolean isArmourersWorkshop = ArmourersWorkshopClientApi.getSkinRenderHandler() != null
				&& ArmourersWorkshopApi.getEntitySkinCapability(entityLivingBaseIn) != null;
		if (isArmourersWorkshop && !isAW) {
			if (ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(itemstack)) {
				ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
				ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(itemstack);
				try {
					ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor); // Skin
					if (skin != null) {
						Object targetModel = awu.getTypeHelperForModel.invoke(awu.skinModelRenderHelper,
								awu.MODEL_BIPED, skin.getSkinType());
						if (targetModel instanceof ModelBase) {
							awu.npcSkinData.set(targetModel, skin);
							awu.npcDyeData.set(targetModel, skinDescriptor.getSkinDye());
							t = (T) targetModel;
							isAW = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (isAW) {
			return;
		}
		t.setModelAttributes(this.renderer.getMainModel());
		t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
		this.setModelSlotVisible((ModelBiped) t, slotIn);
		this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, null));
		{
			if (itemarmor.hasOverlay(itemstack)) { // Allow this for anything, not only cloth
				int i = itemarmor.getColor(itemstack);
				float f = (float) (i >> 16 & 255) / 255.0F;
				float f1 = (float) (i >> 8 & 255) / 255.0F;
				float f2 = (float) (i & 255) / 255.0F;
				GlStateManager.color(this.colorR * f, this.colorG * f1, this.colorB * f2, this.alpha);
				t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, "overlay"));
			}
			// Non-colored
			GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
			t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			// Default
			if (!this.skipRenderGlint && itemstack.hasEffect()) {
				renderEnchantedGlint(this.renderer, entityLivingBaseIn, t, limbSwing, limbSwingAmount, partialTicks,
						ageInTicks, netHeadYaw, headPitch, scale);
			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	protected void setModelSlotVisible(ModelBiped modelBiped, EntityEquipmentSlot slotIn) {
		this.setModelVisible(modelBiped);
		switch (slotIn) {
		case HEAD:
			modelBiped.bipedHead.showModel = true;
			modelBiped.bipedHeadwear.showModel = true;
			break;
		case CHEST:
			modelBiped.bipedBody.showModel = true;
			modelBiped.bipedRightArm.showModel = true;
			modelBiped.bipedLeftArm.showModel = true;
			break;
		case LEGS:
			modelBiped.bipedBody.showModel = true;
			modelBiped.bipedRightLeg.showModel = true;
			modelBiped.bipedLeftLeg.showModel = true;
			break;
		case FEET:
			modelBiped.bipedRightLeg.showModel = true;
			modelBiped.bipedLeftLeg.showModel = true;
		}
	}

	protected void setModelVisible(ModelBiped model) {
		model.setVisible(false);
	}

}
