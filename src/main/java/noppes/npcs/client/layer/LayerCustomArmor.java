package noppes.npcs.client.layer;

import java.util.Map;

import com.google.common.collect.Maps;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.client.util.aw.CustomSkinModelRenderHelper;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class LayerCustomArmor<T extends ModelBase> extends LayerArmorBase<ModelBiped> {

	private final RenderLivingBase<?> renderer;
	private final boolean skipRenderGlint;
	private final boolean smallArmsIn;
	private final boolean isClassicPlayer;

	public LayerCustomArmor(RenderLivingBase<?> rendererIn, boolean skipRenderGlint, boolean smallArmsIn, boolean isClassicPlayer) {
		super(rendererIn);
		this.renderer = rendererIn;
        this.skipRenderGlint = skipRenderGlint;
        this.smallArmsIn = smallArmsIn;
		this.isClassicPlayer = isClassicPlayer;
		this.modelLeggings = new ModelBipedAlt(0.5F, true, this.smallArmsIn, this.isClassicPlayer);
		this.modelArmor = new ModelBipedAlt(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
	}

	public void doRenderLayer(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
		this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
	}

	@Override
	protected @Nonnull ModelBiped getArmorModelHook(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemStack, @Nonnull EntityEquipmentSlot slot, @Nonnull ModelBiped model) {
		return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
	}

	protected void initArmor() {
		this.modelLeggings = new ModelBipedAlt(0.5F, true, this.smallArmsIn, this.isClassicPlayer);
		this.modelArmor = new ModelBipedAlt(1.0F, true, this.smallArmsIn, this.isClassicPlayer);
	}

	@SuppressWarnings("unchecked")
	protected void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
		if (ArmourersWorkshopApi.isAvailable() && slotIn == EntityEquipmentSlot.LEGS && entityLivingBaseIn instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entityLivingBaseIn;
			Map<EnumParts, Boolean> ba = Maps.newHashMap();
			ba.putAll(npc.animation.showParts);
			
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			double distance = Minecraft.getMinecraft().player.getDistance(npc.posX, npc.posY, npc.posZ);
			int d;
			try { d = (int) awu.renderDistanceSkin.get(awu.configHandlerClient); }
			catch (Exception e) { d = (int) (distance + 1); }
			if (distance <= d) {
				T t = (T) this.getModelFromSlot(slotIn);
				if (t instanceof ModelBipedAlt) {
					CustomSkinModelRenderHelper modelRenderer = CustomSkinModelRenderHelper.getInstance();
					IWardrobeCap wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(npc);
					boolean isRotate = false;
					// SkinWings
					ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(npc.inventory.getStackInSlot(8));
					if (skinDescriptor != null) {
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								t.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, npc);
								isRotate = true;
								modelRenderer.renderEquipmentPart(skin, renderData, npc, (ModelBipedAlt) t, scale, null);
							}
						}
						catch (Exception e) { LogWriter.error("Error:", e); }
					}
					// SkinOutfit
					skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(npc.inventory.getStackInSlot(7));
					if (skinDescriptor != null) {
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								if (!isRotate) { t.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, npc); }
								modelRenderer.renderEquipmentPart(skin, renderData, npc, (ModelBipedAlt) t, scale, ba);
							}
						}
						catch (Exception e) { LogWriter.error("Error:", e); }
					}
				}
			}
		}
		ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slotIn);
		if (!(itemstack.getItem() instanceof ItemArmor)) { return; }
		ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
		if (itemarmor.getEquipmentSlot() != slotIn) {
			return;
		}
		T t = (T) this.getModelFromSlot(slotIn);
		t = (T) getArmorModelHook(entityLivingBaseIn, itemstack, slotIn, (ModelBiped) t);
		boolean isAW = t.getClass().getName().contains("armourers_workshop");
		if (ArmourersWorkshopApi.isAvailable() && isAW) {
			boolean isArmourersWorkshop = ArmourersWorkshopClientApi.getSkinRenderHandler() != null && ArmourersWorkshopApi.getEntitySkinCapability(entityLivingBaseIn) != null;
			if (isArmourersWorkshop) {
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
                            }
						}
					} catch (Exception e) { LogWriter.error("Error:", e); }
				}
			}
		}
		t.setModelAttributes(this.renderer.getMainModel());
		t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
		this.setModelSlotVisible((ModelBiped) t, slotIn);
		this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, null));
		if (isAW) {
			t = (T) this.getModelFromSlot(slotIn);
			t.setModelAttributes(this.renderer.getMainModel());
			t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
			this.setModelSlotVisible((ModelBiped) t, slotIn);
			t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			return;
		}
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
		if (t instanceof ModelBipedAlt) { ((ModelBipedAlt) t).setSlot(slotIn); }
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
