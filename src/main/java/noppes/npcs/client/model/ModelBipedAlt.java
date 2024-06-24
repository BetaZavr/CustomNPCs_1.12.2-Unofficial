package noppes.npcs.client.model;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationStack;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.client.util.aw.CustomSkinModelRenderHelper;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.util.ObfuscationHelper;

/* Используется для создания простых моделей
 * как у игрока. Текстура типа 64х32
 * Пример - слои брони, Classic Player, Голем и т.д.
 */
public class ModelBipedAlt
extends ModelBiped {
	
	private boolean isArmorModel = false;
	public AnimationStack rightStackData = new AnimationStack();
	public AnimationStack leftStackData = new AnimationStack();
	
	public ModelBipedAlt(float modelSize, boolean isArmorModel) {
		super(modelSize);
		this.isArmorModel = isArmorModel;

		this.bipedHead = new ModelRendererAlt(this, EnumParts.HEAD, 0, 0, true);
		((ModelRendererAlt) this.bipedHead).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHeadwear = new ModelRendererAlt(this, EnumParts.HEAD, 32, 0, true);
		((ModelRendererAlt) this.bipedHeadwear).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize + 0.5f);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
		
		this.bipedBody = new ModelRendererAlt(this, EnumParts.BODY, 16, 16, true);
		((ModelRendererAlt) this.bipedBody).setBox(-4.0F, 0.0F, -2.0F, 8, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		
		this.bipedRightArm = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 16, false);
		((ModelRendererAlt) this.bipedRightArm).setBox(-3.0F, -2.0F, -2.0F, 4.0f, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		((ModelRendererAlt) this.bipedRightArm).isArmor = true;
		this.bipedRightLeg = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 16, false);
		((ModelRendererAlt) this.bipedRightLeg).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		((ModelRendererAlt) this.bipedRightLeg).isArmor = true;
		
		this.bipedLeftArm = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 40, 16, false);
		this.bipedLeftArm.mirror = true;
		((ModelRendererAlt) this.bipedLeftArm).setBox(-1.0F, -2.0F, -2.0F, 4.0f, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		((ModelRendererAlt) this.bipedLeftArm).isArmor = true;
		this.bipedLeftLeg = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 0, 16, false);
		this.bipedLeftLeg.mirror = true;
		((ModelRendererAlt) this.bipedLeftLeg).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		((ModelRendererAlt) this.bipedLeftLeg).isArmor = true;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isSwingInProgress) {
			return super.getMainHand(entityIn);
		}
		EntityLivingBase living = (EntityLivingBase) entityIn;
		if (living.swingingHand == EnumHand.MAIN_HAND) {
			return EnumHandSide.RIGHT;
		}
		return EnumHandSide.LEFT;
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		EntityLiving living = (EntityLiving) entityIn;
		if (living.equals(ModelNpcAlt.editAnimDataSelect.displayNpc) && !ModelNpcAlt.editAnimDataSelect.showArmor) { return; }
		if (living.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, living);
		Map<EnumParts, Boolean> ba = Maps.<EnumParts, Boolean>newHashMap();

		float r = 1.0f, g = 1.0f, b = 1.0f;
		if (living instanceof EntityCustomNpc) {
			EntityCustomNpc npc = (EntityCustomNpc) living;
			ba.putAll(npc.animation.showParts);
			if (!this.isArmorModel && npc.display.getTint() != 0xFFFFFF) {
				r = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
				g = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
				b = (float)(npc.display.getTint() & 255) / 255.0F;
			}
		}

		ItemStack stack = null;
		ArmourersWorkshopUtil awu = null;
		IWardrobeCap wardrobe = null;
		CustomSkinModelRenderHelper modelRenderer = null;
		boolean isDistance = false;
		double distance = 0.0d;
		if (ArmourersWorkshopApi.isAvailable()) {
			awu = ArmourersWorkshopUtil.getInstance();
			wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(living);
			modelRenderer = CustomSkinModelRenderHelper.getInstance();
			double d = 0.0d;
			distance = Minecraft.getMinecraft().player.getDistance(living.posX, living.posY, living.posZ);
			try { d = (int) awu.renderDistanceSkin.get(awu.configHandlerClient); }
			catch (Exception e) { e.printStackTrace(); }
			isDistance = distance <= d;
		}

		int entitySkinTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		if (ba.get(EnumParts.HEAD) && this.bipedHead.showModel && living instanceof EntityLiving) {
			((ModelRendererAlt) this.bipedHead).chechBacklightColor(r, g, b);
			if (this.isChild) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
				this.renderHead((EntityLiving) living, scale, ba);
				if (!((ModelRendererAlt) this.bipedHead).isOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					this.bipedHeadwear.render(scale);
				}
				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			} else {
				if (living.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
				this.renderHead((EntityLiving) living, scale, ba);
				if (!((ModelRendererAlt) this.bipedHead).isOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					this.bipedHeadwear.render(scale);
				}
			}
		}
		boolean bodyRendered = false;
		if ((ba.get(EnumParts.ARM_RIGHT) && this.bipedRightArm.showModel) || (ba.get(EnumParts.ARM_LEFT) && this.bipedLeftArm.showModel)) {
			if (this.isArmorModel) {
				stack = living.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				if (ArmourersWorkshopApi.isAvailable() && living instanceof EntityNPCInterface) {
					ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
					if (skinDescriptor != null) {
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null && isDistance) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								bodyRendered = modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) living, this, scale, ba);
							}
						}
						catch (Exception e) { e.printStackTrace(); }
					}
				}
				if (!bodyRendered) {
					((ModelRendererAlt) this.bipedRightArm).clearOBJ();
					((ModelRendererAlt) this.bipedLeftArm).clearOBJ();
					if (living instanceof EntityCustomNpc) {
						String m = ((EntityCustomNpc) living).display.getModel();
						boolean smallArms = m != null && m.indexOf("customnpcalex") != -1;
						((ModelRendererAlt) this.bipedLeftArm).smallArms = smallArms;
						((ModelRendererAlt) this.bipedRightArm).smallArms = smallArms;
					}
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						((ModelRendererAlt) this.bipedRightArm).setOBJModel(stack, null);
						((ModelRendererAlt) this.bipedLeftArm).setOBJModel(stack, null);
					}
				}
			}
			if (!bodyRendered) {
				if (ba.get(EnumParts.ARM_RIGHT) && this.bipedRightArm.showModel) {
					((ModelRendererAlt) this.bipedRightArm).chechBacklightColor(r, g, b);
					if (!((ModelRendererAlt) this.bipedRightArm).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedRightArm.render(scale);
				}
				if (ba.get(EnumParts.ARM_LEFT) && this.bipedLeftArm.showModel) {
					((ModelRendererAlt) this.bipedLeftArm).chechBacklightColor(r, g, b);
					if (!((ModelRendererAlt) this.bipedLeftArm).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedLeftArm.render(scale);
				}
			}
		}
		
		if ((ba.get(EnumParts.LEG_RIGHT) && this.bipedRightLeg.showModel) || (ba.get(EnumParts.LEG_LEFT) && this.bipedLeftLeg.showModel)) {
			boolean lr = ba.get(EnumParts.LEG_RIGHT) && this.bipedRightLeg.showModel;
			boolean ll = ba.get(EnumParts.LEG_LEFT) && this.bipedLeftLeg.showModel;
			boolean legsRendered = false;
			boolean feetRendered = false;
			if (this.isArmorModel) {
				if (ArmourersWorkshopApi.isAvailable() && living instanceof EntityNPCInterface) {
					for (int i = 0; i < 2; i++) {
						stack = living.getItemStackFromSlot(i == 0 ? EntityEquipmentSlot.FEET : EntityEquipmentSlot.LEGS);
						ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
						if (skinDescriptor != null) {
							try {
								ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
								if (skin != null && isDistance) {
									ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
									Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
									modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) living, this, scale, ba);
								}
							}
							catch (Exception e) { e.printStackTrace(); }
							if (i == 0) { feetRendered = true; } else { legsRendered = true; }
						}
					}
				}
				((ModelRendererAlt) this.bipedBody).clearOBJ();
				((ModelRendererAlt) this.bipedRightLeg).clearOBJ();
				((ModelRendererAlt) this.bipedLeftLeg).clearOBJ();
				if (!feetRendered) {
					stack = living.getItemStackFromSlot(EntityEquipmentSlot.FEET);
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						if (lr) {
							((ModelRendererAlt) this.bipedRightLeg).chechBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedRightLeg).setOBJModel(stack, EnumParts.FEET_RIGHT);
							this.bipedRightLeg.render(scale);
						}
						if (ll) {
							((ModelRendererAlt) this.bipedLeftLeg).chechBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedLeftLeg).setOBJModel(stack, EnumParts.FEET_LEFT);
							this.bipedLeftLeg.render(scale);
						}
					}
				}
				if (!legsRendered) {
					stack = living.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						if (lr) { ((ModelRendererAlt) this.bipedRightLeg).setOBJModel(stack, null); }
						if (ll) { ((ModelRendererAlt) this.bipedLeftLeg).setOBJModel(stack, null); }
						if (ba.get(EnumParts.BODY) && this.bipedBody.showModel) {
							((ModelRendererAlt) this.bipedBody).chechBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedBody).setOBJModel(stack, EnumParts.BELT);
							this.bipedBody.render(scale);
						}
					}
					if (!(stack.getItem() instanceof ItemArmor)) {
						GlStateManager.popMatrix();
						return;
					}
				}
			}
			if (!legsRendered) {
				if (lr) {
					((ModelRendererAlt) this.bipedRightLeg).chechBacklightColor(r, g, b);
					if (!((ModelRendererAlt) this.bipedRightLeg).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedRightLeg.render(scale);
				}
				if (ll) {
					((ModelRendererAlt) this.bipedLeftLeg).chechBacklightColor(r, g, b);
					if (!((ModelRendererAlt) this.bipedLeftLeg).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedLeftLeg.render(scale);
				}
			}
		}
		if (!bodyRendered && ba.get(EnumParts.BODY) && this.bipedBody.showModel) {
			if (this.isArmorModel) {
				((ModelRendererAlt) this.bipedBody).clearOBJ();
				stack = living.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
					((ModelRendererAlt) this.bipedBody).setOBJModel(stack, null);
				}
			}
			((ModelRendererAlt) this.bipedBody).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedBody).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedBody.render(scale);
		}
		GlStateManager.popMatrix();
	}

	private void renderHead(EntityLiving living, float scale, Map<EnumParts, Boolean> ba) {
		ItemStack stack = living.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (this.isArmorModel) {
			if (ArmourersWorkshopApi.isAvailable() && living instanceof EntityNPCInterface) {
				ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
				if (skinDescriptor != null) {
					ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
					double distance = Minecraft.getMinecraft().player.getDistance(living.posX, living.posY, living.posZ);
					int d = 0;
					try { d = (int) awu.renderDistanceSkin.get(awu.configHandlerClient); }
					catch (Exception e) { d = (int) (distance + 1); }
					if (distance <= d) {
						CustomSkinModelRenderHelper modelRenderer = CustomSkinModelRenderHelper.getInstance();
						IWardrobeCap wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(living);
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) living, this, scale, ba);
								return;
							}
						}
						catch (Exception e) { e.printStackTrace(); }
					}
				}
			}
			if (!(stack.getItem() instanceof ItemArmor)) {
				this.bipedHead.showModel = false;
			}
		}
		((ModelRendererAlt) this.bipedHead).clearOBJ();
		if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
			((ModelRendererAlt) this.bipedHead).setOBJModel(stack, null);
		}
		this.bipedHead.render(scale);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (entityIn instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entityIn;
			if (npc.navigating != null && (netHeadYaw < -2.0f || netHeadYaw > 2.0f)) {
				npc.turn(netHeadYaw / 3.0f, headPitch / 3.0f);
				ObfuscationHelper.setValue(EntityLivingBase.class, npc, npc.rotationYaw, 58);
				ObfuscationHelper.setValue(EntityLivingBase.class, npc, npc.rotationPitch, 59);
			}
			if (!this.isRiding) { this.isRiding = npc.currentAnimation == 1; }
			if (this.isSneak && npc.isPlayerSleeping()) { this.isSneak = false; }
			if (npc.currentAnimation == 6 || (npc.inventory.getProjectile() != null && npc.isAttacking() && npc.stats.ranged.getHasAimAnimation())) {
				this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
			}
		}
		this.isSneak = entityIn.isSneaking();
		
		// Standart Rotation
		this.clearAllRotations();
		if (entityIn instanceof EntityCustomNpc) {
			if (this.bipedHead.showModel) { ((ModelRendererAlt) this.bipedHead).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.HEAD)); }
			if (this.bipedBody.showModel) { ((ModelRendererAlt) this.bipedBody).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.BODY)); }
			if (this.bipedLeftArm.showModel) { ((ModelRendererAlt) this.bipedLeftArm).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.ARM_LEFT)); }
			if (this.bipedRightArm.showModel) { ((ModelRendererAlt) this.bipedRightArm).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.ARM_RIGHT)); }
			if (this.bipedLeftLeg.showModel) { ((ModelRendererAlt) this.bipedLeftLeg).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.LEG_LEFT)); }
			if (this.bipedRightLeg.showModel) { ((ModelRendererAlt) this.bipedRightLeg).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.LEG_RIGHT)); }
		}
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		if (!(entityIn instanceof EntityNPCInterface)) { return; }
		EntityNPCInterface npc = (EntityNPCInterface) entityIn;
		if (!npc.animation.hasAnim()) {
			if (npc.getAttackTarget() == null) {
				if (npc.isPlayerSleeping()) {
					if (this.bipedHead.rotateAngleX < 0.0f) {
						this.bipedHead.rotateAngleX = 0.0f;
						this.bipedHeadwear.rotateAngleX = 0.0f;
					}
				} else if (npc.currentAnimation != 0) {
					if (npc.currentAnimation == 3) {
						AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 5) {
						AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 7) {
						AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 8) {
						AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 9) {
						float n6 = 0.7f;
						this.bipedHead.rotateAngleX = n6;
						this.bipedHeadwear.rotateAngleX = n6;
					} else if (npc.currentAnimation == 10) {
						AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 11) {
						AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 12) {
						AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 13) {
						AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					}
				}
			}
			else if (npc.ais.getStandingType() == 4 && npc.lookat != null) {
				double d0 = npc.posX - npc.lookat.posX;
				double d1 = (npc.posY + (double) npc.getEyeHeight()) - (npc.lookat.posY + (double) npc.lookat.getEyeHeight());
				double d2 = npc.posZ - npc.lookat.posZ;
				double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
				float yaw = MathHelper.wrapDegrees(npc.rotationYawHead - (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F);;
				float pitch = MathHelper.wrapDegrees(npc.rotationPitch + (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI))));
				if (yaw < -45.0f) { yaw = -45.0f; }
				else if (yaw > 45.0f) { yaw = 45.0f; }
				this.bipedHead.rotateAngleY = (float) ((-yaw * Math.PI) / 180D);
				if (pitch < -45.0f) { pitch = -45.0f; }
				else if (pitch > 45.0f) { pitch = 45.0f; }
				this.bipedHead.rotateAngleX = (float) ((-pitch * Math.PI) / 180D);
			}
		} else {
			float partialTicks = 0.0f;
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen == null || mc.currentScreen.isFocused()) { partialTicks = mc.getRenderPartialTicks(); }
			npc.animation.setRotationAngles(this.swingProgress, partialTicks);
			if (npc.animation.showParts.get(EnumParts.HEAD)) { ((ModelRendererAlt) this.bipedHead).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.BODY)) { ((ModelRendererAlt) this.bipedBody).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.ARM_RIGHT)) {
				((ModelRendererAlt) this.bipedRightArm).setAnimation(npc.animation);
				this.rightStackData.setAnimation(npc.animation, EnumParts.RIGHT_STACK.patterns);
			}
			if (npc.animation.showParts.get(EnumParts.ARM_LEFT)) {
				((ModelRendererAlt) this.bipedLeftArm).setAnimation(npc.animation);
				this.leftStackData.setAnimation(npc.animation, EnumParts.LEFT_STACK.patterns);
			}
			if (npc.animation.showParts.get(EnumParts.LEG_RIGHT)) { ((ModelRendererAlt) this.bipedRightLeg).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.LEG_LEFT)) { ((ModelRendererAlt) this.bipedLeftLeg).setAnimation(npc.animation); }
		}
		ModelNpcAlt.copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear);
	}

	@Override
	public void postRenderArm(float scale, EnumHandSide side) { // for ItemStacks
		super.postRenderArm(scale, side);
		ModelRendererAlt modelRenderer = (ModelRendererAlt) this.bipedRightArm;
		AnimationStack hundData = this.rightStackData;
		if (side == EnumHandSide.LEFT) {
			modelRenderer = (ModelRendererAlt) this.bipedLeftArm;
			hundData = this.leftStackData;
		}
		if (!hundData.showModel) { return; }
		if (!modelRenderer.isNormal) {
			if (modelRenderer.rotateAngleX1 != 0.0f) {
				float ofsY = modelRenderer.dy2 - modelRenderer.dy0;
				GlStateManager.translate(0.0f, 0.625f, 0.0f);
				float ofsZ = modelRenderer.rotateAngleX1 * (modelRenderer.dz / -2.0f) / (float) -Math.PI;
				GlStateManager.translate(0.0f, ofsY * -0.0625f, ofsZ * 0.0625f);
				GlStateManager.rotate(modelRenderer.rotateAngleX1 * 180.0f / (float) Math.PI, 1.0f, 0.0f, 0.0f);
				GlStateManager.translate(0.0f, ofsY * 0.0625f, ofsZ * -0.0625f);
				GlStateManager.translate(0.0f, -0.625f, 0.0f);
			}
			if (modelRenderer.rotateAngleY1 != 0.0f) {
				float ofs = (side == EnumHandSide.RIGHT ? -1.0f : 1.0f) * 0.0625f;
				GlStateManager.translate(ofs, 0.0f, 0.0f);
				GlStateManager.rotate(modelRenderer.rotateAngleY1 * -180.0f / (float) Math.PI, 0.0f, 1.0f, 0.0f);
				GlStateManager.translate(-ofs, 0.0f, 0.0f);
			}
		}
		if (hundData.partSets[3] != 0.0f || hundData.partSets[4] != 0.0f || hundData.partSets[5] != 0.0f) {
			GlStateManager.translate(hundData.partSets[3], hundData.partSets[4], hundData.partSets[5]);
		}
		if (hundData.partSets[2] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
		}
		if (hundData.partSets[1] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
		}
		if (hundData.partSets[0] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
		}
		if (hundData.partSets[6] != 1.0f || hundData.partSets[7] != 1.0f || hundData.partSets[8] != 1.0f) {
			GlStateManager.scale(hundData.partSets[6], hundData.partSets[7], hundData.partSets[7]);
		}
	}
	
	private void clearAllRotations() {
		// Head
		if (this.bipedHead.showModel) { 
			((ModelRendererAlt) this.bipedHead).clearRotations();
			this.bipedHeadwear.rotateAngleX = 0.0f;
			this.bipedHeadwear.rotateAngleZ = 0.0f;
		}
		// Body
		if (this.bipedBody.showModel) { ((ModelRendererAlt) this.bipedBody).clearRotations(); }
		// Arm left
		if (this.bipedLeftArm.showModel) { ((ModelRendererAlt) this.bipedLeftArm).clearRotations(); }
		// Arm right
		if (this.bipedRightArm.showModel) { ((ModelRendererAlt) this.bipedRightArm).clearRotations(); }
		// Leg left
		if (this.bipedLeftLeg.showModel) { 
			((ModelRendererAlt) this.bipedLeftLeg).clearRotations();
			this.bipedLeftLeg.rotationPointX = 2.0f;
			this.bipedLeftLeg.rotationPointZ = 0.0f;
		}
		// Leg right
		if (this.bipedRightLeg.showModel) { 
			((ModelRendererAlt) this.bipedRightLeg).clearRotations();
			this.bipedRightLeg.rotationPointX = -2.0f;
			this.bipedRightLeg.rotationPointZ = 0.0f;
		}
	}
	
}
