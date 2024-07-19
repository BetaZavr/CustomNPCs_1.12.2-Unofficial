package noppes.npcs.client.model;

import java.util.Map;

import noppes.npcs.LogWriter;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.model.animation.AnimationStack;
import noppes.npcs.client.model.part.head.ModelHeadwear;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.client.util.aw.CustomSkinModelRenderHelper;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;

import javax.annotation.Nonnull;

/* Used to create simple models
 * like a player. Texture type 64x32
 * Example - layers of armor, Classic Player, Golem, etc.
 */
public class ModelBipedAlt extends ModelNpcAlt {

	private final boolean isArmorModel;
	public final AnimationStack rightStackData = new AnimationStack();
	public final AnimationStack leftStackData = new AnimationStack();
	private EntityEquipmentSlot slot;

	public ModelBipedAlt(float modelSize, boolean isArmorModel, boolean smallArmsIn, boolean isClassicPlayer) {
		super(modelSize, smallArmsIn, isClassicPlayer);
		this.isArmorModel = isArmorModel;
		this.textureWidth = 64;
		this.textureHeight = 32;
		this.init(modelSize);
	}

	@Override
	protected void init(float modelSize) {
		float handWidth = this.smallArmsIn ? 3.0f : 4.0f;
		this.bipedHead = new ModelRendererAlt(this, EnumParts.HEAD, 0, 0, true);
		((ModelRendererAlt) this.bipedHead).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHeadwear = new ModelRendererAlt(this, EnumParts.HEAD, 32, 0, true);
		((ModelRendererAlt) this.bipedHeadwear).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize + 0.5f);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHeadwear_64 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 64);
		this.bipedHeadwear_128 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 128);
		this.bipedHeadwear_256 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 256);
		this.bipedHeadwear_512 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 512);
		this.bipedHeadwear_1024 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 1024);
		this.bipedHeadwear_2048 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 2048);
		this.bipedHeadwear_4096 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 4096);

		this.bipedBody = new ModelRendererAlt(this, EnumParts.BODY, 16, 16, false);
		((ModelRendererAlt) this.bipedBody).setBox(-4.0F, 0.0F, -2.0F, 8, 5.5f, 4.0f, 2.5f, 4, modelSize);
		this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);

		this.bipedRightArm = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 16, false);
		((ModelRendererAlt) this.bipedRightArm).setBox(this.smallArmsIn ? -2.0F : -3.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		((ModelRendererAlt) this.bipedRightArm).smallArms = this.smallArmsIn;
		((ModelRendererAlt) this.bipedRightArm).isArmor = true;
		this.bipedRightLeg = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 16, false);
		((ModelRendererAlt) this.bipedRightLeg).setBox(-2.0F, 0.0F, -2.1F, 4.0f, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		((ModelRendererAlt) this.bipedRightLeg).isArmor = true;

		this.bipedLeftArm = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 40, 16, false);
		this.bipedLeftArm.mirror = true;
		((ModelRendererAlt) this.bipedLeftArm).setBox(-1.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		((ModelRendererAlt) this.bipedLeftArm).smallArms = this.smallArmsIn;
		((ModelRendererAlt) this.bipedLeftArm).isArmor = true;
		this.bipedLeftLeg = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 0, 16, false);
		this.bipedLeftLeg.mirror = true;
		((ModelRendererAlt) this.bipedLeftLeg).setBox(-2.2F, 0.0F, -2.1F, 4.0f, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		((ModelRendererAlt) this.bipedLeftLeg).isArmor = true;
	}

	@Override
	public void render(@Nonnull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entityIn.equals(ModelNpcAlt.editAnimDataSelect.displayNpc) && !ModelNpcAlt.editAnimDataSelect.showArmor) { return; }
		if (entityIn.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		Map<EnumParts, Boolean> ba = Maps.newHashMap();
		Map<EnumParts, Boolean> baArmor = Maps.newHashMap();

		float r = 1.0f, g = 1.0f, b = 1.0f;
		if (entityIn instanceof EntityPlayer) {
			PlayerData data = PlayerData.get((EntityPlayer) entityIn);
			if (data != null) {
				ba.putAll(data.animation.showParts);
				baArmor.putAll(data.animation.showArmorParts);
			}
		}
		else if (entityIn instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entityIn;
			ba.putAll(npc.animation.showParts);
			baArmor.putAll(npc.animation.showArmorParts);
			if (!this.isArmorModel && npc.display.getTint() != 0xFFFFFF) {
				r = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
				g = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
				b = (float)(npc.display.getTint() & 255) / 255.0F;
			}
		}

		ItemStack stack;
		ArmourersWorkshopUtil awu = null;
		IWardrobeCap wardrobe = null;
		CustomSkinModelRenderHelper modelRenderer = null;
		boolean isDistance = false;
		double distance = 0.0d;
		if (ArmourersWorkshopApi.isAvailable()) {
			awu = ArmourersWorkshopUtil.getInstance();
			wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(entityIn);
			modelRenderer = CustomSkinModelRenderHelper.getInstance();
			double d = 0.0d;
			distance = Minecraft.getMinecraft().player.getDistance(entityIn.posX, entityIn.posY, entityIn.posZ);
			try { d = (int) awu.renderDistanceSkin.get(awu.configHandlerClient); }
			catch (Exception e) { LogWriter.error("Error:", e); }
			isDistance = distance <= d;
		}

		int entitySkinTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		this.bipedHead.showModel = ba.get(EnumParts.HEAD) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.HEAD);
		if (this.bipedHead.showModel && entityIn instanceof EntityLivingBase) {
			((ModelRendererAlt) this.bipedHead).checkBacklightColor(r, g, b);
			if (this.isChild) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
				boolean showArmorHead = this.renderHead((EntityLivingBase) entityIn, scale, baArmor);
				if (showArmorHead && ((ModelRendererAlt) this.bipedHead).notOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					this.renderHeadWear(scale);
				}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			} else {
				if (entityIn.isSneaking()) {
					boolean chest = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemArmor;
					boolean legs = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemArmor;
					boolean feets = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemArmor;
					GlStateManager.translate(0.0F, -0.2F - (legs ? 0.2F : 0.0F) - (feets ? 0.2F : 0.0F) - (chest ? 0.2F : 0.0F), 0.0F);
				}
				boolean showArmorHead = this.renderHead((EntityLivingBase) entityIn, scale, baArmor);
				if (showArmorHead && ((ModelRendererAlt) this.bipedHead).notOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					this.renderHeadWear(scale);
				}
			}
		}

		boolean bodyArmorRendered = false;
		this.bipedRightArm.showModel = ba.get(EnumParts.ARM_RIGHT) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.CHEST);
		this.bipedLeftArm.showModel = ba.get(EnumParts.ARM_LEFT) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.CHEST);
		if (this.bipedRightArm.showModel || this.bipedLeftArm.showModel) {
			if (this.isArmorModel && this.slot == EntityEquipmentSlot.CHEST) {
				stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				this.bipedRightArm.showModel = stack.getItem() instanceof ItemArmor && baArmor.get(EnumParts.ARM_RIGHT);
				this.bipedLeftArm.showModel = stack.getItem() instanceof ItemArmor && baArmor.get(EnumParts.ARM_LEFT);
				if (ArmourersWorkshopApi.isAvailable() && entityIn instanceof EntityNPCInterface) {
					ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
					if (skinDescriptor != null && awu != null && isDistance) {
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								bodyArmorRendered = modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) entityIn, this, scale, baArmor);
								this.bipedRightArm.showModel = !bodyArmorRendered;
								this.bipedLeftArm.showModel = !bodyArmorRendered;
							}
						}
						catch (Exception e) { LogWriter.error("Error:", e); }
					}
				}
				if (!bodyArmorRendered) {
					((ModelRendererAlt) this.bipedRightArm).clearOBJ();
					((ModelRendererAlt) this.bipedLeftArm).clearOBJ();
					if (entityIn instanceof EntityCustomNpc) {
						String m = ((EntityCustomNpc) entityIn).display.getModel();
						boolean smallArms = m != null && m.contains("customnpcalex");
						((ModelRendererAlt) this.bipedLeftArm).smallArms = smallArms;
						((ModelRendererAlt) this.bipedRightArm).smallArms = smallArms;
					}
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						((ModelRendererAlt) this.bipedRightArm).setOBJModel(stack, null);
						((ModelRendererAlt) this.bipedLeftArm).setOBJModel(stack, null);
					}
				}
			}
			if (!bodyArmorRendered) {
				GlStateManager.pushMatrix();
				if (entityIn.isSneaking()) { GlStateManager.translate(0.0F, -0.2F, 0.0F); }
				if (this.bipedRightArm.showModel) {

					((ModelRendererAlt) this.bipedRightArm).checkBacklightColor(r, g, b);
					if (((ModelRendererAlt) this.bipedRightArm).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedRightArm.render(scale);
				}
				if (this.bipedLeftArm.showModel) {
					((ModelRendererAlt) this.bipedLeftArm).checkBacklightColor(r, g, b);
					if (((ModelRendererAlt) this.bipedLeftArm).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedLeftArm.render(scale);
				}
				GlStateManager.popMatrix();
			}
		}

		this.bipedRightLeg.showModel = ba.get(EnumParts.LEG_RIGHT) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.LEGS || this.slot == EntityEquipmentSlot.FEET);
		this.bipedLeftLeg.showModel = ba.get(EnumParts.LEG_LEFT) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.LEGS || this.slot == EntityEquipmentSlot.FEET);
		if ((ba.get(EnumParts.LEG_RIGHT) && this.bipedRightLeg.showModel) || (ba.get(EnumParts.LEG_LEFT) && this.bipedLeftLeg.showModel)) {
			boolean legsRender = true;
			if (this.isArmorModel && (this.slot == EntityEquipmentSlot.LEGS || this.slot == EntityEquipmentSlot.FEET)) {
				if (this.slot == EntityEquipmentSlot.LEGS) {
					this.bipedRightLeg.showModel = baArmor.get(EnumParts.LEG_RIGHT);
					this.bipedLeftLeg.showModel = baArmor.get(EnumParts.LEG_LEFT);
				} else {
					this.bipedRightLeg.showModel = baArmor.get(EnumParts.FEET_RIGHT);
					this.bipedLeftLeg.showModel = baArmor.get(EnumParts.FEET_LEFT);
					baArmor.put(EnumParts.LEG_RIGHT, this.bipedRightLeg.showModel);
					baArmor.put(EnumParts.LEG_LEFT, this.bipedLeftLeg.showModel);
				}
				legsRender = ((EntityLivingBase) entityIn).getItemStackFromSlot(this.slot).getItem() instanceof ItemArmor;
				if (legsRender && ArmourersWorkshopApi.isAvailable() && entityIn instanceof EntityNPCInterface) {
					stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(this.slot);
					ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
					if (skinDescriptor != null && awu != null && isDistance) {
						try {
							ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
							if (skin != null) {
								ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
								Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, DefaultPlayerSkin.getDefaultSkinLegacy());
								modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) entityIn, this, scale, baArmor);
								this.bipedRightLeg.showModel = false;
								this.bipedLeftLeg.showModel = false;
							}
						}
						catch (Exception e) { LogWriter.error("Error:", e); }
						legsRender = false;
					}
				}
				((ModelRendererAlt) this.bipedBody).clearOBJ();
				((ModelRendererAlt) this.bipedRightLeg).clearOBJ();
				((ModelRendererAlt) this.bipedLeftLeg).clearOBJ();
				if (this.slot == EntityEquipmentSlot.FEET && legsRender) {
					stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.FEET);
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						if (this.bipedRightLeg.showModel) {
							((ModelRendererAlt) this.bipedRightLeg).checkBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedRightLeg).setOBJModel(stack, EnumParts.FEET_RIGHT);
							this.bipedRightLeg.render(scale);
						}
						if (this.bipedLeftLeg.showModel) {
							((ModelRendererAlt) this.bipedLeftLeg).checkBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedLeftLeg).setOBJModel(stack, EnumParts.FEET_LEFT);
							this.bipedLeftLeg.render(scale);
						}
					}
				}
				if (this.slot == EntityEquipmentSlot.LEGS && legsRender) {
					stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.LEGS);
					if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
						if (this.bipedRightLeg.showModel) { ((ModelRendererAlt) this.bipedRightLeg).setOBJModel(stack, null); }
						if (this.bipedLeftLeg.showModel) { ((ModelRendererAlt) this.bipedLeftLeg).setOBJModel(stack, null); }
						if (ba.get(EnumParts.BODY) && this.bipedBody.showModel) {
							((ModelRendererAlt) this.bipedBody).checkBacklightColor(r, g, b);
							((ModelRendererAlt) this.bipedBody).setOBJModel(stack, EnumParts.BELT);
							this.bipedBody.render(scale);
						}
					}
				}
			}
			if (legsRender) {
				GlStateManager.pushMatrix();
				if (entityIn.isSneaking()) {
					GlStateManager.translate(0.0F, -0.4F, 0.0F);
					if (this.isArmorModel && this.slot == EntityEquipmentSlot.FEET) { GlStateManager.translate(0.0F, -0.2F, 0.0F); }
				}
				if (this.bipedRightLeg.showModel) {
					((ModelRendererAlt) this.bipedRightLeg).checkBacklightColor(r, g, b);
					if (((ModelRendererAlt) this.bipedRightLeg).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedRightLeg.render(scale);
				}
				if (this.bipedLeftLeg.showModel) {
					((ModelRendererAlt) this.bipedLeftLeg).checkBacklightColor(r, g, b);
					if (((ModelRendererAlt) this.bipedLeftLeg).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
					this.bipedLeftLeg.render(scale);
				}
				GlStateManager.popMatrix();
			}
		}
		if (!bodyArmorRendered) { bodyArmorRendered = !baArmor.get(EnumParts.BODY); }

		this.bipedBody.showModel = !bodyArmorRendered && ba.get(EnumParts.BODY) && (!this.isArmorModel || this.slot == EntityEquipmentSlot.CHEST);
		if (this.bipedBody.showModel) {
			if (this.isArmorModel) {
				((ModelRendererAlt) this.bipedBody).clearOBJ();
				stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
					((ModelRendererAlt) this.bipedBody).setOBJModel(stack, null);
				}
			}
			GlStateManager.pushMatrix();
			if (entityIn.isSneaking()) { GlStateManager.translate(0.0F, -0.2F, 0.0F); }
			((ModelRendererAlt) this.bipedBody).checkBacklightColor(r, g, b);
			if (((ModelRendererAlt) this.bipedBody).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedBody.render(scale);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	private boolean renderHead(EntityLivingBase living, float scale, Map<EnumParts, Boolean> baArmor) {
		boolean showArmorHead = true;
		ItemStack stack = living.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (this.isArmorModel) {
			showArmorHead = baArmor.get(EnumParts.HEAD);
			if (showArmorHead && ArmourersWorkshopApi.isAvailable() && living instanceof EntityNPCInterface) {
				ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
				if (skinDescriptor != null) {
					ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
					double distance = Minecraft.getMinecraft().player.getDistance(living.posX, living.posY, living.posZ);
					int d;
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
								modelRenderer.renderEquipmentPart(skin, renderData, (EntityNPCInterface) living, this, scale, baArmor);
								return false;
							}
						}
						catch (Exception e) { LogWriter.error("Error:", e); }
					}
				}
			}
			if (!(stack.getItem() instanceof ItemArmor)) {
				this.bipedHead.showModel = false;
			}
		}
		if (showArmorHead) {
			((ModelRendererAlt) this.bipedHead).clearOBJ();
			if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
				((ModelRendererAlt) this.bipedHead).setOBJModel(stack, null);
			}
			this.bipedHead.render(scale);
		}
		return showArmorHead;
	}

	public void setSlot(EntityEquipmentSlot slotIn) { this.slot = slotIn; }

}
