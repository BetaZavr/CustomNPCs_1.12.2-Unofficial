package noppes.npcs.client.model;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.model.animation.AnimationStack;
import noppes.npcs.client.model.part.head.ModelHeadwear;
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
	private float armorColorR = 1.0f;
	private float armorColorG = 1.0f;
	private float armorColorB = 1.0f;
	public final AnimationStack rightStackData = new AnimationStack(7);
	public final AnimationStack leftStackData = new AnimationStack(6);
	protected EntityEquipmentSlot slot;

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

		bipedHeadwear_64 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 64, false);
		bipedHeadwear_128 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 128, false);
		bipedHeadwear_256 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 256, false);
		bipedHeadwear_512 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 512, false);
		bipedHeadwear_1024 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 1024, false);
		bipedHeadwear_2048 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 2048, false);
		bipedHeadwear_4096 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 4096, false);

		bipedHeadwear_64_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 64, true);
		bipedHeadwear_128_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 128, true);
		bipedHeadwear_256_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 256, true);
		bipedHeadwear_512_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 512, true);
		bipedHeadwear_1024_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 1024, true);
		bipedHeadwear_2048_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 2048, true);
		bipedHeadwear_4096_old = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 4096, true);

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
		Map<EnumParts, Boolean> ba = new HashMap<>();
		Map<EnumParts, Boolean> baArmor = new HashMap<>();

		float r = 1.0f, g = 1.0f, b = 1.0f;
		if (isArmorModel) {
			r = armorColorR;
			g = armorColorG;
			b = armorColorB;
		}
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
			if (!isArmorModel && npc.display.getTint() != 0xFFFFFF) {
				r = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
				g = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
				b = (float)(npc.display.getTint() & 255) / 255.0F;
			}
		}

		ItemStack stack;
		int entitySkinTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();

		bipedHead.showModel = ba.get(EnumParts.HEAD) && (!isArmorModel || (baArmor.get(EnumParts.HEAD) && slot == EntityEquipmentSlot.HEAD));
		if (this.bipedHead.showModel && entityIn instanceof EntityLivingBase) {
			((ModelRendererAlt) this.bipedHead).checkBacklightColor(r, g, b);
			if (this.isChild) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
				if (renderHead((EntityLivingBase) entityIn, scale)) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					renderHeadWear(scale);
				}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			} else {
				if (entityIn.isSneaking()) {
					boolean chest = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemArmor;
					boolean legs = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemArmor;
					boolean feet = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemArmor;
					GlStateManager.translate(0.0F, -0.2F - (legs ? 0.2F : 0.0F) - (feet ? 0.2F : 0.0F) - (chest ? 0.2F : 0.0F), 0.0F);
				}
				if (renderHead((EntityLivingBase) entityIn, scale)) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					renderHeadWear(scale);
				}
			}
		}

		bipedRightArm.showModel = ba.get(EnumParts.ARM_RIGHT) && (!isArmorModel || (baArmor.get(EnumParts.ARM_RIGHT) && slot == EntityEquipmentSlot.CHEST));
		bipedLeftArm.showModel = ba.get(EnumParts.ARM_LEFT) && (!isArmorModel || (baArmor.get(EnumParts.ARM_LEFT) && slot == EntityEquipmentSlot.CHEST));
		if (this.bipedRightArm.showModel || this.bipedLeftArm.showModel) {
			if (this.isArmorModel && this.slot == EntityEquipmentSlot.CHEST) {
				stack = ((EntityLivingBase) entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				this.bipedRightArm.showModel = stack.getItem() instanceof ItemArmor && baArmor.get(EnumParts.ARM_RIGHT);
				this.bipedLeftArm.showModel = stack.getItem() instanceof ItemArmor && baArmor.get(EnumParts.ARM_LEFT);
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

		bipedLeftArm.showModel = ba.get(EnumParts.LEG_RIGHT) && (!isArmorModel || (baArmor.get(EnumParts.LEG_RIGHT) && (slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET)));
		bipedLeftLeg.showModel = ba.get(EnumParts.LEG_LEFT) && (!isArmorModel || (baArmor.get(EnumParts.LEG_LEFT) && (slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET)));
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

		bipedBody.showModel = ba.get(EnumParts.BODY) && (!isArmorModel || (baArmor.get(EnumParts.BODY) && slot == EntityEquipmentSlot.CHEST));
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

	private boolean renderHead(EntityLivingBase living, float scale) {
		ItemStack stack = living.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		((ModelRendererAlt) bipedHead).clearOBJ();
		if (stack.getItem() instanceof CustomArmor && ((CustomArmor) stack.getItem()).objModel != null) {
			((ModelRendererAlt) bipedHead).setOBJModel(stack, null);
		}
		bipedHead.render(scale);
		return ((ModelRendererAlt) this.bipedHead).notOBJModel();
	}

	public void setSlot(EntityEquipmentSlot slotIn) { this.slot = slotIn; }

	public void setArmorColor(float r, float g, float b) {
		armorColorR = r;
		armorColorG = g;
		armorColorB = b;
	}

}
