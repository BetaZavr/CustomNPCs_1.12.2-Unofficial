package noppes.npcs.client.layer;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.model.ModelBipedAW;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class LayerCustomArmor<T extends ModelBase> extends LayerArmorBase<ModelBiped> {

	private final RenderLivingBase<?> renderer;
	private final boolean skipRenderGlint;
	private final boolean smallArmsIn;
	private final boolean isClassicPlayer;
	protected ModelBipedAW modelAW;

	public LayerCustomArmor(RenderLivingBase<?> rendererIn, boolean skipRenderGlint, boolean smallArmsIn, boolean isClassicPlayer) {
		super(rendererIn);
		this.renderer = rendererIn;
        this.skipRenderGlint = skipRenderGlint;
        this.smallArmsIn = smallArmsIn;
		this.isClassicPlayer = isClassicPlayer;
		this.modelLeggings = new ModelBipedAlt(0.5F, true, this.smallArmsIn, this.isClassicPlayer);
		this.modelArmor = new ModelBipedAlt(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
        this.modelAW = new ModelBipedAW(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
	}

	public void doRenderLayer(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
	}

	@Override
	protected @Nonnull ModelBiped getArmorModelHook(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemStack, @Nonnull EntityEquipmentSlot slot, @Nonnull ModelBiped model) {
		return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
	}

	protected void initArmor() {
		this.modelLeggings = new ModelBipedAlt(0.5F, true, this.smallArmsIn, this.isClassicPlayer);
		this.modelArmor = new ModelBipedAlt(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
		this.modelAW = new ModelBipedAW(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
	}

	@SuppressWarnings("unchecked")
	protected void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
		boolean isAWLoad = ArmourersWorkshopApi.isAvailable();
		if (isAWLoad && entityLivingBaseIn instanceof EntityNPCInterface) {
			modelAW.setSlot(slotIn);
			modelAW.setModelAttributes(renderer.getMainModel());
			modelAW.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
			setModelSlotVisible(modelAW, slotIn);
			modelAW.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
		ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slotIn);
		if (!(itemstack.getItem() instanceof ItemArmor)) { return; }
		if (isAWLoad && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(itemstack)) { return; }
		ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
		if (itemarmor.getEquipmentSlot() != slotIn) { return; }

		T t = (T) this.getModelFromSlot(slotIn);
		t = (T) getArmorModelHook(entityLivingBaseIn, itemstack, slotIn, (ModelBiped) t);
		t.setModelAttributes(this.renderer.getMainModel());
		t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
		this.setModelSlotVisible((ModelBiped) t, slotIn);
		this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, null));
		float alpha = 1.0F;
		float colorR = 1.0F;
		float colorG = 1.0F;
		float colorB = 1.0F;
		if (itemarmor.hasOverlay(itemstack)) { // Allow this for anything, not only cloth
			int i = itemarmor.getColor(itemstack);
			float f = (float) (i >> 16 & 255) / 255.0F;
			float f1 = (float) (i >> 8 & 255) / 255.0F;
			float f2 = (float) (i & 255) / 255.0F;
			GlStateManager.color(colorR * f, colorG * f1, colorB * f2, alpha);
			t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, "overlay"));
		}
		// Non-colored
		GlStateManager.color(colorR, colorG, colorB, alpha);
		if (t instanceof ModelBipedAlt) {
			((ModelBipedAlt) t).setSlot(slotIn);
		}
		t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		// Default
		if (!this.skipRenderGlint && itemstack.hasEffect()) {
			renderEnchantedGlint(this.renderer, entityLivingBaseIn, t, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}

	@SuppressWarnings("incomplete-switch")
	protected void setModelSlotVisible(@Nonnull ModelBiped modelBiped, @Nonnull EntityEquipmentSlot slotIn) {
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
