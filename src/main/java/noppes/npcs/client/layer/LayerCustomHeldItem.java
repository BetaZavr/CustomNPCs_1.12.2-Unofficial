package noppes.npcs.client.layer;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.capability.IEntitySkinCapability;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.client.util.aw.CustomSkinModelRenderHelper;
import noppes.npcs.constants.EnumParts;

@SideOnly(Side.CLIENT)
public class LayerCustomHeldItem<T extends EntityLivingBase> extends LayerInterface<T> {

	public LayerCustomHeldItem(RenderLiving<?> livingEntityRendererIn) {
		super(livingEntityRendererIn);
	}

	@Override
	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		boolean flag = npc.getPrimaryHand() == EnumHandSide.RIGHT;
		ItemStack mainhand = flag ? npc.getHeldItemMainhand() : npc.getHeldItemOffhand();
		ItemStack offhand = flag ? npc.getHeldItemOffhand() : npc.getHeldItemMainhand();
		boolean isAWShow = ArmourersWorkshopApi.isAvailable();
		if (isAWShow || !mainhand.isEmpty() || !offhand.isEmpty()) {
			double distance = Minecraft.getMinecraft().player.getDistance(npc.posX, npc.posY, npc.posZ);
			if (isAWShow) {
				try {
					isAWShow = distance <= (double) ArmourersWorkshopUtil.getInstance().renderDistanceSkin.get(ArmourersWorkshopUtil.getInstance().configHandlerClient);
				}
				catch (Exception ignored) { }
			}
			GlStateManager.pushMatrix();
			if (npc.isChild()) {
				GlStateManager.translate(0.0F, 0.75F, 0.0F);
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
			}
			boolean showMain = false, showOff;
			if (npc.animation.showParts.get(EnumParts.ARM_RIGHT)) {
				showMain = true;
				if (model instanceof ModelBipedAlt) { showMain = flag ? ((ModelBipedAlt) this.model).rightStackData.showModel : ((ModelBipedAlt) this.model).leftStackData.showModel; }
				else if (model instanceof ModelNpcAlt) { showMain = flag ? ((ModelNpcAlt) this.model).rightStackData.showModel : ((ModelNpcAlt) this.model).leftStackData.showModel; }
				if (showMain) {
					renderHeldItem(npc, mainhand, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT, scale, distance, isAWShow);
				}
			}
			if (npc.animation.showParts.get(EnumParts.ARM_LEFT)) {
				showOff = true;
				if (model instanceof ModelBipedAlt) { showOff = flag ? ((ModelBipedAlt) this.model).leftStackData.showModel : ((ModelBipedAlt) this.model).rightStackData.showModel; }
				else if (model instanceof ModelNpcAlt) { showOff = flag ? ((ModelNpcAlt) this.model).leftStackData.showModel : ((ModelNpcAlt) this.model).rightStackData.showModel; }
				if (showOff) {
					renderHeldItem(npc, offhand, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT, scale, distance, isAWShow);
				}
			}
			if (ArmourersWorkshopApi.isAvailable() && isAWShow) {
				IEntitySkinCapability skinCapability = ArmourersWorkshopApi.getEntitySkinCapability(npc);
				ISkinType[] skinTypes = skinCapability.getValidSkinTypes();
				if (showMain) {
					for (ISkinType skinType : skinTypes) {
						if (skinType.getName().equals("Sword") || skinType.getName().equals("bow")) {
							IInventory inv = ArmourersWorkshopUtil.getInstance().getSkinTypeInv(skinCapability.getSkinInventoryContainer(), skinType);
							for (int j = 0; j < inv.getSizeInventory(); j++) {
								if (ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(inv.getStackInSlot(j))) {
									renderHeldAWItem(ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(inv.getStackInSlot(j)), npc.isSneaking(), flag ? EnumHandSide.RIGHT : EnumHandSide.LEFT, distance, scale);
								}
							}
						}
					}
				}
				if (showMain) {
					for (ISkinType skinType : skinTypes) {
						if (skinType.getName().equals("Shield")) {
							IInventory inv = ArmourersWorkshopUtil.getInstance().getSkinTypeInv(skinCapability.getSkinInventoryContainer(), skinType);
							for (int j = 0; j < inv.getSizeInventory(); j++) {
								if (ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(inv.getStackInSlot(j))) {
									renderHeldAWItem(ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(inv.getStackInSlot(j)), npc.isSneaking(), flag ? EnumHandSide.LEFT : EnumHandSide.RIGHT, distance, scale);
								}
							}
						}
					}
				}
			}
			GlStateManager.popMatrix();
		}
	}

	private void renderHeldItem(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transform, EnumHandSide handSide, float scale, double distance, boolean isAWShow) {
		if (stack.isEmpty()) { return; }
		GlStateManager.pushMatrix();
		if (isAWShow && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(stack)) {
			renderHeldAWItem(ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack), entity.isSneaking(), handSide, distance, scale);
			return;
		}
		model.postRenderArm(scale, handSide);
		boolean isLeft = handSide == EnumHandSide.LEFT;

		if (entity.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
		GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate((isLeft ? -1.0F : 1.0F) / 16.0F, 0.125f, -0.625f);
		Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack, transform, isLeft);
		GlStateManager.popMatrix();
	}

	private void renderHeldAWItem(ISkinDescriptor skinDescriptor, boolean isSneaking, EnumHandSide handSide, double distance, float scale) {
		ArmourersWorkshopUtil awu  = ArmourersWorkshopUtil.getInstance();
		CustomSkinModelRenderHelper modelRenderer = CustomSkinModelRenderHelper.getInstance();
		IWardrobeCap wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(npc);
		GlStateManager.pushMatrix();
		if (isSneaking) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
		model.postRenderArm(scale, handSide);
		boolean isLeft = handSide == EnumHandSide.LEFT;
		try {
			ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
			if (skin != null) {
				ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
				for (int dyeIndex = 0; dyeIndex < 8; dyeIndex++) {
					if (skinDescriptor.getSkinDye().haveDyeInSlot(dyeIndex)) {
						dye.addDye(dyeIndex, skinDescriptor.getSkinDye().getDyeColour(dyeIndex));
					}
				}
				ResourceLocation texture = DefaultPlayerSkin.getDefaultSkinLegacy();
				Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, texture);
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.translate((isLeft ? 1.0F : -1.0F) / 16.0F, 0.0F, -0.5F);
				if (isLeft) {
					String type = skinDescriptor.getIdentifier().getSkinType().getName();
					if (type.equalsIgnoreCase("shield")) {
						GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
						GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
						GlStateManager.translate(0.0f, 0.0f, -0.125f);
					} else if (type.equalsIgnoreCase("bow")) {
						GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
						GlStateManager.rotate(220.0F, 1.0F, 0.0F, 0.0F);
						GlStateManager.translate(0.0f, 0.1875f, -0.0625f);
					}
				}
				modelRenderer.renderEquipmentPart(skin, renderData, this.npc, (ModelBiped) this.render.getMainModel(), scale, null);
			}
		} catch (Exception e) {
			LogWriter.error("Error:", e);
			GlStateManager.popMatrix();
			return;
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	public void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
	}

}