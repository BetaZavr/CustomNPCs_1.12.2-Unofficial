package noppes.npcs.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.layer.LayerArms;
import noppes.npcs.client.layer.LayerBody;
import noppes.npcs.client.layer.LayerCustomArmor;
import noppes.npcs.client.layer.LayerCustomHeldItem;
import noppes.npcs.client.layer.LayerEyes;
import noppes.npcs.client.layer.LayerHead;
import noppes.npcs.client.layer.LayerLegs;
import noppes.npcs.client.layer.LayerNpcCloak;
import noppes.npcs.client.layer.LayerPreRender;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class RenderCustomNpc<T extends EntityCustomNpc> extends RenderNPCInterface<T> {

	private EntityLivingBase entity;
	public ModelBiped npcmodel;
	private float partialTicks;
	private RenderLivingBase<EntityLivingBase> renderEntity;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RenderCustomNpc(ModelBiped model) {
		super(model, 0.5f);
		this.npcmodel = (ModelBiped) this.mainModel;
		this.layerRenderers.add(new LayerEyes(this));
		this.layerRenderers.add(new LayerHead(this));
		this.layerRenderers.add(new LayerArms(this));
		this.layerRenderers.add(new LayerLegs(this));
		this.layerRenderers.add(new LayerBody(this));
		this.layerRenderers.add(new LayerNpcCloak(this));
		this.addLayer(new LayerCustomHead(this.npcmodel.bipedHead));
		this.addLayer(new LayerCustomHeldItem(this));
		boolean smallArmsIn = model instanceof ModelNpcAlt && ((ModelNpcAlt) model).smallArmsIn;
		boolean isClassicPlayer = model instanceof ModelNpcAlt && ((ModelNpcAlt) model).isClassicPlayer;
		this.addLayer(new LayerCustomArmor(this, false, smallArmsIn, isClassicPlayer));
	}

	@Override
	protected void applyRotations(@Nonnull T npc, float handleRotation, float rotationYaw, float partialTicks) {
		if (this.renderEntity != null && !(this.renderEntity instanceof RenderCustomNpc)) {
			NPCRendererHelper.applyRotations(this.renderEntity, this.entity, handleRotation, rotationYaw, partialTicks);
			return;
		}
		if (npc.isEntityAlive()) {
			super.applyRotations(npc, handleRotation, rotationYaw, partialTicks);
			return;
		}
		GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
		if (npc.deathTime > 0) {
			if (npc.animation.hasAnim(AnimationKind.DIES)) {
				return;
			}
			float f = ((float) npc.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
			f = MathHelper.sqrt(f);
			if (f > 1.0F) {
				f = 1.0F;
			}
			GlStateManager.rotate(f * this.getDeathMaxRotation(npc), 0.0F, 0.0F, 1.0F);
		} else {
			String s = TextFormatting.getTextWithoutFormattingCodes(npc.getName());
			if (("Dinnerbone".equals(s) || "Grumm".equals(s))) {
				GlStateManager.translate(0.0F, npc.height + 0.1F, 0.0F);
				GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doRender(@Nonnull T npc, double d, double d1, double d2, float f, float partialTicks) {
		this.partialTicks = partialTicks;
		this.entity = npc.modelData.getEntity(npc);
		if (this.entity != null) {
			Render<?> render = this.renderManager.getEntityRenderObject(this.entity);
			if (render instanceof RenderLivingBase) {
				this.renderEntity = (RenderLivingBase<EntityLivingBase>) render;
			} else {
				this.renderEntity = null;
				this.entity = null;
			}
		} else {
			this.renderEntity = null;
			List<LayerRenderer<T>> list = this.layerRenderers;
			for (LayerRenderer<T> layer : list) {
				if (layer instanceof LayerPreRender) {
					((LayerPreRender)layer).preRender(npc);
				}
			}
		}
		this.npcmodel.rightArmPose = this.getPose(npc, npc.getHeldItemMainhand());
		this.npcmodel.leftArmPose = this.getPose(npc, npc.getHeldItemOffhand());
		super.doRender(npc, d, d1, d2, f, partialTicks);
	}

	public ModelBiped.ArmPose getPose(T npc, ItemStack item) {
		if (NoppesUtilServer.IsItemStackNull(item)) {
			return ModelBiped.ArmPose.EMPTY;
		}
		if (npc.getItemInUseCount() > 0) {
			EnumAction enumaction = item.getItemUseAction();
			if (enumaction == EnumAction.BLOCK) {
				return ModelBiped.ArmPose.BLOCK;
			}
			if (enumaction == EnumAction.BOW) {
				return ModelBiped.ArmPose.BOW_AND_ARROW;
			}
		}
		return ModelBiped.ArmPose.ITEM;
	}

	@Override
	protected float handleRotationFloat(@Nonnull T par1EntityLivingBase, float partialTicks) {
		if (this.renderEntity != null) {
			return NPCRendererHelper.handleRotationFloat(this.entity, partialTicks, this.renderEntity);
		}
		return super.handleRotationFloat(par1EntityLivingBase, partialTicks);
	}

	@Override
	protected void preRenderCallback(@Nonnull T npc, float f) {
		if (this.renderEntity != null) {
			this.renderColor(npc);
			int size = npc.display.getSize();
			if (this.entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) this.entity).display.setSize(5);
			}
			NPCRendererHelper.preRenderCallback(this.entity, f, this.renderEntity);
			npc.display.setSize(size);
			GlStateManager.scale(0.2f * npc.display.getSize(), 0.2f * npc.display.getSize(), 0.2f * npc.display.getSize());
		} else {
			super.preRenderCallback(npc, f);
		}
	}

	protected void renderLayers(@Nonnull T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
		if (this.entity != null && this.renderEntity != null) {
			NPCRendererHelper.drawLayers(this.entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn, this.renderEntity);
		} else {
			Map<EnumParts, Boolean> sp = entitylivingbaseIn.animation.showParts;
			for (LayerRenderer<T> layerrenderer : this.layerRenderers) {
				if ((layerrenderer instanceof LayerEyes || layerrenderer instanceof LayerHead
						|| layerrenderer.getClass().getSimpleName().equals("LayerCustomHead")) && !sp.get(EnumParts.HEAD)) {
					continue;
				}
				if (ArmourersWorkshopApi.isAvailable() && layerrenderer.getClass().getSimpleName().equals("LayerCustomHead") && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD))) {
					continue;
				}
				if ((layerrenderer instanceof LayerBody || layerrenderer instanceof LayerNpcCloak) && !sp.get(EnumParts.BODY)) {
					continue;
				}
				if (layerrenderer.getClass().getSimpleName().equals("SkinLayerRendererCustomNPC")) {
					continue;
				}
				boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures());
				layerrenderer.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);
				if (flag) {
					this.unsetBrightness();
				}
			}
		}
	}

	@Override
	protected void renderModel(@Nonnull T npc, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (this.renderEntity != null) {
			boolean isInvisible = npc.isInvisible();
			if (npc.display.getVisible() == 1) {
				isInvisible = npc.display.getAvailability().isAvailable(Minecraft.getMinecraft().player);
			} else if (npc.display.getVisible() == 2) {
				isInvisible = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() != CustomRegisters.wand;
			}
			if (isInvisible) {
				GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
			}
			ModelBase model = this.renderEntity.getMainModel();
			if (PixelmonHelper.isPixelmon(this.entity)) {
				ModelBase pixModel = (ModelBase) PixelmonHelper.getModel(this.entity);
				if (pixModel != null) {
					model = pixModel;
					PixelmonHelper.setupModel(this.entity, pixModel);
				}
			}
			model.swingProgress = this.mainModel.swingProgress;
			model.isRiding = (this.entity.isRiding() && this.entity.getRidingEntity() != null && this.entity.getRidingEntity().shouldRiderSit());
			model.setLivingAnimations(this.entity, limbSwing, limbSwingAmount, this.partialTicks);
			model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, this.entity);
			model.isChild = this.entity.isChild();
			NPCRendererHelper.renderModel(this.entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, this.renderEntity, model, Objects.requireNonNull(this.getEntityTexture(npc)));
			if (!npc.display.getOverlayTexture().isEmpty()) {
				GlStateManager.depthFunc(515);
				if (npc.textureGlowLocation == null) {
					npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
				}
				float f1 = 1.0f;
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(1, 1);
				GlStateManager.disableLighting();
                GlStateManager.depthMask(!npc.isInvisible());
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.pushMatrix();
				GlStateManager.scale(1.001f, 1.001f, 1.001f);
				NPCRendererHelper.renderModel(this.entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, this.renderEntity, model, npc.textureGlowLocation);
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				GlStateManager.color(1.0f, 1.0f, 1.0f, f1);
				GlStateManager.depthFunc(515);
				GlStateManager.disableBlend();
			}
			if (isInvisible) {
				GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
			}
		} else {
			super.renderModel(npc, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLightmap(@Nonnull EntityCustomNpc npc) {
		super.setLightmap((T) npc);
	}

}
