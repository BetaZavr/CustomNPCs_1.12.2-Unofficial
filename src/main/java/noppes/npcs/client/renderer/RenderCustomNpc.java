package noppes.npcs.client.renderer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.layer.LayerArms;
import noppes.npcs.client.layer.LayerBody;
import noppes.npcs.client.layer.LayerCustomHeldItem;
import noppes.npcs.client.layer.LayerEyes;
import noppes.npcs.client.layer.LayerHead;
import noppes.npcs.client.layer.LayerHeadwear;
import noppes.npcs.client.layer.LayerLegs;
import noppes.npcs.client.layer.LayerNpcCloak;
import noppes.npcs.client.layer.LayerPreRender;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class RenderCustomNpc<T extends EntityCustomNpc>
extends RenderNPCInterface<T> {
	
	private EntityLivingBase entity;
	public ModelBiped npcmodel;
	private float partialTicks;
	private RenderLivingBase<EntityLivingBase> renderEntity;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RenderCustomNpc(ModelBiped model) {
		super((ModelBase) model, 0.5f);
		this.npcmodel = (ModelBiped) this.mainModel;
		this.layerRenderers.add(new LayerEyes(this));
		this.layerRenderers.add(new LayerHeadwear(this));
		this.layerRenderers.add(new LayerHead(this));
		this.layerRenderers.add(new LayerArms(this));
		this.layerRenderers.add(new LayerLegs(this));
		this.layerRenderers.add(new LayerBody(this));
		this.layerRenderers.add(new LayerNpcCloak(this));
		this.addLayer(new LayerCustomHeldItem(this));
		this.addLayer(new LayerCustomHead(this.npcmodel.bipedHead));
		//this.addLayer(new LayerCustomArmor(this));
		LayerBipedArmor armor = new LayerBipedArmor(this);
		ObfuscationHelper.setValue(LayerArmorBase.class, armor, new ModelBipedAlt(0.5f), 1);
		ObfuscationHelper.setValue(LayerArmorBase.class, armor, new ModelBipedAlt(1.0f), 2);
		this.addLayer(armor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doRender(T npc, double d, double d1, double d2, float f, float partialTicks) {
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
			List<LayerRenderer<T>> list = (List<LayerRenderer<T>>) this.layerRenderers;
			for (LayerRenderer<T> layer : list) {
				if (layer instanceof LayerPreRender) {
					((LayerPreRender) layer).preRender(npc);
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
	protected float handleRotationFloat(T par1EntityLivingBase, float par2) {
		if (this.renderEntity != null) {
			return NPCRendererHelper.handleRotationFloat(this.entity, par2, this.renderEntity);
		}
		return super.handleRotationFloat(par1EntityLivingBase, par2);
	}

	@Override
	protected void preRenderCallback(T npc, float f) {
		if (this.renderEntity != null) {
			this.renderColor(npc);
			int size = npc.display.getSize();
			if (this.entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) this.entity).display.setSize(5);
			}
			NPCRendererHelper.preRenderCallback(this.entity, f, this.renderEntity);
			npc.display.setSize(size);
			GlStateManager.scale(0.2f * npc.display.getSize(), 0.2f * npc.display.getSize(),
					0.2f * npc.display.getSize());
		} else {
			super.preRenderCallback(npc, f);
		}
	}

	protected void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
		if (this.entity != null && this.renderEntity != null) {
			NPCRendererHelper.drawLayers(this.entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
					headPitch, scaleIn, this.renderEntity);
		} else {
			super.renderLayers(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
					headPitch, scaleIn);
		}
	}

	@Override
	protected void renderModel(T npc, float par2, float par3, float par4, float par5, float par6, float par7) {
		if (this.renderEntity != null) {
			boolean flag = !npc.isInvisible();
			boolean flag2 = !flag && !npc.isInvisibleToPlayer((EntityPlayer) Minecraft.getMinecraft().player);
			if (!flag && !flag2) {
				return;
			}
			if (flag2) {
				GlStateManager.pushMatrix();
				GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
				GlStateManager.depthMask(false);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(770, 771);
				GlStateManager.alphaFunc(516, 0.003921569f);
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
			model.isRiding = (this.entity.isRiding() && this.entity.getRidingEntity() != null
					&& this.entity.getRidingEntity().shouldRiderSit());
			model.setLivingAnimations(this.entity, par2, par3, this.partialTicks);
			model.setRotationAngles(par2, par3, par4, par5, par6, par7, this.entity);
			model.isChild = this.entity.isChild();
			NPCRendererHelper.renderModel(this.entity, par2, par3, par4, par5, par6, par7, this.renderEntity, model,
					this.getEntityTexture(npc));
			if (!npc.display.getOverlayTexture().isEmpty()) {
				GlStateManager.depthFunc(515);
				if (npc.textureGlowLocation == null) {
					npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
				}
				float f1 = 1.0f;
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(1, 1);
				GlStateManager.disableLighting();
				if (npc.isInvisible()) {
					GlStateManager.depthMask(false);
				} else {
					GlStateManager.depthMask(true);
				}
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.pushMatrix();
				GlStateManager.scale(1.001f, 1.001f, 1.001f);
				NPCRendererHelper.renderModel(this.entity, par2, par3, par4, par5, par6, par7, this.renderEntity, model,
						npc.textureGlowLocation);
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				GlStateManager.color(1.0f, 1.0f, 1.0f, f1);
				GlStateManager.depthFunc(515);
				GlStateManager.disableBlend();
			}
			if (flag2) {
				GlStateManager.disableBlend();
				GlStateManager.alphaFunc(516, 0.1f);
				GlStateManager.popMatrix();
				GlStateManager.depthMask(true);
			}
		} else {
			super.renderModel(npc, par2, par3, par4, par5, par6, par7);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLightmap(EntityCustomNpc npc) {
		super.setLightmap((T) npc);
	}

}
