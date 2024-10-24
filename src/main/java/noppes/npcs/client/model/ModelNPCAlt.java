package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.capability.IEntitySkinCapability;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.model.animation.*;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.mixin.client.model.IModelPlayerMixin;
import noppes.npcs.mixin.entity.IEntityLivingBaseMixin;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.model.part.AnimData;
import noppes.npcs.client.model.part.head.ModelHeadwear;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;
import noppes.npcs.entity.data.DataAnimation;

import javax.annotation.Nonnull;

/* Used as an NPC model */
public class ModelNpcAlt extends ModelPlayer {

    public static final AnimData editAnimDataSelect = new AnimData();

    public static void copyModelAngles(ModelRendererAlt source, ModelRendererAlt dest) {
        dest.copyModelAngles(source);
    }

    protected ModelHeadwear bipedHeadwear_64;
    protected ModelHeadwear bipedHeadwear_128;
    protected ModelHeadwear bipedHeadwear_256;
    protected ModelHeadwear bipedHeadwear_512;
    protected ModelHeadwear bipedHeadwear_1024;
    protected ModelHeadwear bipedHeadwear_2048;
    protected ModelHeadwear bipedHeadwear_4096;

    protected ModelHeadwear bipedHeadwear_64_old;
    protected ModelHeadwear bipedHeadwear_128_old;
    protected ModelHeadwear bipedHeadwear_256_old;
    protected ModelHeadwear bipedHeadwear_512_old;
    protected ModelHeadwear bipedHeadwear_1024_old;
    protected ModelHeadwear bipedHeadwear_2048_old;
    protected ModelHeadwear bipedHeadwear_4096_old;
    protected ModelRendererAlt bipedCape;
    public AnimationStack rightStackData = new AnimationStack();
    public AnimationStack leftStackData = new AnimationStack();
    public boolean smallArmsIn;
    public boolean isClassicPlayer;

    public ModelNpcAlt(float modelSize, boolean smallArmsIn, boolean isClassicPlayer) {
        super(modelSize, smallArmsIn);
        this.smallArmsIn = smallArmsIn;
        this.isClassicPlayer = isClassicPlayer;
        init(modelSize);
    }

    protected void init(float modelSize) {
        float wear = 0.25f;
        this.bipedHead = new ModelRendererAlt(this, EnumParts.HEAD, 0, 0, true);
        ((ModelRendererAlt) this.bipedHead).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedHeadwear = new ModelRendererAlt(this, EnumParts.HEAD, 32, 0, true);
        ((ModelRendererAlt) this.bipedHeadwear).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize + 2.0f * wear);
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
        this.bipedBodyWear = new ModelRendererAlt(this, EnumParts.BODY, 16, 32, false);
        ((ModelRendererAlt) this.bipedBodyWear).setBox(-4.0F, 0.0F, -2.0F, 8, 5.5f, 4.0f, 2.5f, 4, modelSize + wear);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedCape = new ModelRendererAlt(this, EnumParts.BODY, 0, 0, true);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.setBox(-5.0F, 0.0F, -1.0F, 10, 9, 5, 2, 1, modelSize);
        this.bipedCape.setRotationPoint(0.0F, 0.0F, 0.0F);
        ((IModelPlayerMixin) this).npcs$setBipedCape(this.bipedCape);

        float handWidth = this.smallArmsIn ? 3.0f : 4.0f;
        this.bipedRightArm = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 16, false);
        ((ModelRendererAlt) this.bipedRightArm).setBox(this.smallArmsIn ? -2.0F : -3.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 32, false);
        ((ModelRendererAlt) this.bipedRightArmwear).setBox(this.smallArmsIn ? -2.0F : -3.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
        this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

        this.bipedRightLeg = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 16, false);
        ((ModelRendererAlt) this.bipedRightLeg).setBox(-2.0F, 0.0F, -2.1F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
        this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 32, false);
        ((ModelRendererAlt) this.bipedRightLegwear).setBox(-2.0F, 0.0F, -2.1F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
        this.bipedRightLegwear.setRotationPoint(-2.0F, 12.0F, 0.0F);

        this.bipedLeftArm = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 32, 48, false);
        ((ModelRendererAlt) this.bipedLeftArm).setBox(-1.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArmwear = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 48, 48, false);
        ((ModelRendererAlt) this.bipedLeftArmwear).setBox(-1.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
        this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);

        this.bipedLeftLeg = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 16, 48, false);
        ((ModelRendererAlt) this.bipedLeftLeg).setBox(-2.2F, 0.0F, -2.1F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
        this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 0, 48, false);
        ((ModelRendererAlt) this.bipedLeftLegwear).setBox(-2.2F, 0.0F, -2.1F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
        this.bipedLeftLegwear.setRotationPoint(2.0F, 12.0F, 0.0F);
    }

    protected @Nonnull EnumHandSide getMainHand(@Nonnull Entity entityIn) {
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
    public void render(@Nonnull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (ModelNpcAlt.editAnimDataSelect.part != null && Minecraft.getMinecraft().currentScreen == null) { ModelNpcAlt.editAnimDataSelect.part = null; }
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        Map<EnumParts, Boolean> ba = new HashMap<>();
        Map<EnumParts, Boolean> bAW = new HashMap<>();

        ModelNpcAlt.editAnimDataSelect.isNPC = entityIn.equals(ModelNpcAlt.editAnimDataSelect.displayNpc);
        float r = 1.0f, g = 1.0f, b = 1.0f;
        if (entityIn instanceof EntityPlayer) {
            PlayerData data = PlayerData.get((EntityPlayer) entityIn);
            if (data != null) { ba.putAll(data.animation.showParts); }
        }
        else if (entityIn instanceof EntityCustomNpc) {
            EntityCustomNpc npc = (EntityCustomNpc) entityIn;
            ba.putAll(npc.animation.showParts);
            // possible disabling of body parts rendering from the AW mod:
            if (ArmourersWorkshopApi.isAvailable()) {
                npc.animation.resetShowAWParts();
                Map<EnumParts, List<ISkinDescriptor>> map = new HashMap<>();
                map.put(EnumParts.HEAD, new ArrayList<>());
                map.put(EnumParts.BODY, new ArrayList<>());
                map.put(EnumParts.ARM_LEFT, new ArrayList<>());
                map.put(EnumParts.ARM_RIGHT, new ArrayList<>());
                map.put(EnumParts.LEG_LEFT, new ArrayList<>());
                map.put(EnumParts.LEG_RIGHT, new ArrayList<>());
                for (int i = 0; i < 4; i++) {
                    if (npc.inventory.getArmor(i) == null) { continue; }
                    ItemStack stack = npc.inventory.getArmor(i).getMCItemStack();
                    if (stack != null && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(stack)) {
                        ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
                        if (i == 0) {
                            map.get(EnumParts.HEAD).add(skinDescriptor);
                        } else if (i == 1) {
                            map.get(EnumParts.BODY).add(skinDescriptor);
                            map.get(EnumParts.ARM_RIGHT).add(skinDescriptor);
                            map.get(EnumParts.ARM_LEFT).add(skinDescriptor);
                        } else {
                            map.get(EnumParts.LEG_RIGHT).add(skinDescriptor);
                            map.get(EnumParts.LEG_LEFT).add(skinDescriptor);
                        }
                    }
                }
                if (npc.inventory.awItems.get(0) != null && ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(npc.inventory.awItems.get(0).getMCItemStack())) {
                    ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(npc.inventory.awItems.get(0).getMCItemStack());
                    map.get(EnumParts.HEAD).add(skinDescriptor);
                    map.get(EnumParts.BODY).add(skinDescriptor);
                    map.get(EnumParts.ARM_RIGHT).add(skinDescriptor);
                    map.get(EnumParts.ARM_LEFT).add(skinDescriptor);
                    map.get(EnumParts.LEG_RIGHT).add(skinDescriptor);
                    map.get(EnumParts.LEG_LEFT).add(skinDescriptor);
                }
                IEntitySkinCapability skinCapability = ArmourersWorkshopApi.getEntitySkinCapability(entityIn);
                ISkinType[] skinTypes = skinCapability.getValidSkinTypes();
                for (ISkinType skinType : skinTypes) {
                    if (skinType.getName().equals("Outfit") ||
                            skinType.getName().equals("Head") ||
                            skinType.getName().equals("Chest") ||
                            skinType.getName().equals("Legs") ||
                            skinType.getName().equals("Feet")) {
                        IInventory inv = ArmourersWorkshopUtil.getInstance().getSkinTypeInv(skinCapability.getSkinInventoryContainer(), skinType);
                        for (int j = 0; j < inv.getSizeInventory(); j++) {
                            if (ArmourersWorkshopApi.getSkinNBTUtils().hasSkinDescriptor(inv.getStackInSlot(j))) {
                                ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(inv.getStackInSlot(j));
                                switch (skinType.getName()) {
                                    case "Head": {
                                        map.get(EnumParts.HEAD).add(skinDescriptor);
                                        break;
                                    }
                                    case "Chest": {
                                        map.get(EnumParts.BODY).add(skinDescriptor);
                                        map.get(EnumParts.ARM_RIGHT).add(skinDescriptor);
                                        map.get(EnumParts.ARM_LEFT).add(skinDescriptor);
                                        break;
                                    }
                                    case "Outfit": {
                                        map.get(EnumParts.HEAD).add(skinDescriptor);
                                        map.get(EnumParts.BODY).add(skinDescriptor);
                                        map.get(EnumParts.ARM_RIGHT).add(skinDescriptor);
                                        map.get(EnumParts.ARM_LEFT).add(skinDescriptor);
                                        map.get(EnumParts.LEG_RIGHT).add(skinDescriptor);
                                        map.get(EnumParts.LEG_LEFT).add(skinDescriptor);
                                        break;
                                    }
                                    default: {
                                        map.get(EnumParts.LEG_RIGHT).add(skinDescriptor);
                                        map.get(EnumParts.LEG_LEFT).add(skinDescriptor);
                                    }
                                }
                            }
                        }
                    }
                }
                ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
                for (EnumParts slot : map.keySet()) {
                    for (ISkinDescriptor skinDescriptor : map.get(slot)) {
                        try {
                            ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
                            if (skin != null) { awu.setShowPartFromArmor(slot, skin, npc); }
                        }
                        catch (Exception ignore) { }
                    }
                }
            }
            bAW.putAll(npc.animation.showAWParts);
            if (((EntityCustomNpc) entityIn).display.getTint() != 0xFFFFFF) {
                r = (float)(((EntityCustomNpc) entityIn).display.getTint() >> 16 & 255) / 255.0F;
                g = (float)(((EntityCustomNpc) entityIn).display.getTint() >> 8 & 255) / 255.0F;
                b = (float)(((EntityCustomNpc) entityIn).display.getTint() & 255) / 255.0F;
            }
        }
        int entitySkinTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        this.bipedHead.showModel = ba.get(EnumParts.HEAD) && bAW.get(EnumParts.HEAD);
        if (this.bipedHead.showModel) {
            ((ModelRendererAlt) this.bipedHead).checkBacklightColor(r, g, b);
            if (this.isChild) {
                GlStateManager.scale(0.75F, 0.75F, 0.75F);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);

                this.bipedHead.render(scale);
                if (((ModelRendererAlt) this.bipedHead).notOBJModel()) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                    this.renderHeadWear(scale);
                }
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            } else {
                if (entityIn.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
                this.bipedHead.render(scale);
                if (((ModelRendererAlt) this.bipedHead).notOBJModel()) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                    this.renderHeadWear(scale);
                }
            }
        }
        if (ba.get(EnumParts.BODY) && bAW.get(EnumParts.BODY) && this.bipedBody.showModel) {
            ((ModelRendererAlt) this.bipedBody).checkBacklightColor(r, g, b);
            if (((ModelRendererAlt) this.bipedBody).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
            this.bipedBody.render(scale);
            if (this.bipedBodyWear != null && ((ModelRendererAlt) this.bipedBody).notOBJModel()) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                this.bipedBodyWear.render(scale);
            }
        }
        if (ba.get(EnumParts.ARM_RIGHT) && bAW.get(EnumParts.ARM_RIGHT) && this.bipedRightArm.showModel) {
            ((ModelRendererAlt) this.bipedRightArm).checkBacklightColor(r, g, b);
            if (((ModelRendererAlt) this.bipedRightArm).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
            this.bipedRightArm.render(scale);
            if (this.bipedRightArmwear != null && ((ModelRendererAlt) this.bipedLeftArm).notOBJModel()) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                this.bipedRightArmwear.render(scale);
            }
        }
        if (ba.get(EnumParts.ARM_LEFT) && bAW.get(EnumParts.ARM_LEFT) && this.bipedLeftArm.showModel) {
            ((ModelRendererAlt) this.bipedLeftArm).checkBacklightColor(r, g, b);
            if (((ModelRendererAlt) this.bipedLeftArm).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
            this.bipedLeftArm.render(scale);
            if (this.bipedLeftArmwear != null && ((ModelRendererAlt) this.bipedLeftArm).notOBJModel()) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                this.bipedLeftArmwear.render(scale);
            }
        }
        if (ba.get(EnumParts.LEG_RIGHT) && bAW.get(EnumParts.LEG_RIGHT) && this.bipedRightLeg.showModel) {
            ((ModelRendererAlt) this.bipedRightLeg).checkBacklightColor(r, g, b);
            if (((ModelRendererAlt) this.bipedRightLeg).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
            this.bipedRightLeg.render(scale);
            if (this.bipedRightLegwear != null) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                this.bipedRightLegwear.render(scale);
            }
        }
        if (ba.get(EnumParts.LEG_LEFT) && bAW.get(EnumParts.LEG_LEFT) && this.bipedLeftLeg.showModel) {
            ((ModelRendererAlt) this.bipedLeftLeg).checkBacklightColor(r, g, b);
            if (((ModelRendererAlt) this.bipedLeftLeg).notOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
            this.bipedLeftLeg.render(scale);
            if (this.bipedLeftLegwear != null) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
                this.bipedLeftLegwear.render(scale);
            }
        }
    }

    protected void renderHeadWear(float scale) {
        if (CustomNpcs.HeadWearType == 2) { return; }
        ModelRenderer bipedHead = bipedHeadwear;
        if (CustomNpcs.HeadWearType == 1) {
            int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            boolean isOld = w > h;
            if (w >= 4096) {
                if (isOld && bipedHeadwear_4096_old != null) {
                    bipedHead = bipedHeadwear_4096_old;
                }
                else if (!isOld && bipedHeadwear_4096 != null) {
                    bipedHead = bipedHeadwear_4096;
                }
            }
            else if (w >= 2048) {
                if (isOld && bipedHeadwear_2048_old != null) {
                    bipedHead = bipedHeadwear_2048_old;
                }
                else if (!isOld && bipedHeadwear_2048 != null) {
                    bipedHead = bipedHeadwear_2048;
                }
            }
            else if (w >= 1024) {
                if (isOld && bipedHeadwear_1024_old != null) {
                    bipedHead = bipedHeadwear_1024_old;
                }
                else if (!isOld && bipedHeadwear_1024 != null) {
                    bipedHead = bipedHeadwear_1024;
                }
            }
            else if (w >= 512) {
                if (isOld && bipedHeadwear_512_old != null) {
                    bipedHead = bipedHeadwear_512_old;
                }
                else if (!isOld && bipedHeadwear_512 != null) {
                    bipedHead = bipedHeadwear_512;
                }
            }
            else if (w >= 256) {
                if (isOld && bipedHeadwear_256_old != null) {
                    bipedHead = bipedHeadwear_256_old;
                }
                else if (!isOld && bipedHeadwear_256 != null) {
                    bipedHead = bipedHeadwear_256;
                }
            }
            else if (w >= 128) {
                if (isOld && bipedHeadwear_128_old != null) {
                    bipedHead = bipedHeadwear_128_old;
                }
                else if (!isOld && bipedHeadwear_128 != null) {
                    bipedHead = bipedHeadwear_128;
                }
            }
            else {
                if (isOld && bipedHeadwear_64_old != null) {
                    bipedHead = bipedHeadwear_64_old;
                } else if (!isOld && bipedHeadwear_64 != null) {
                    bipedHead = bipedHeadwear_64;
                }
            }
        }
        bipedHead.render(scale);
    }

    @Override
    public void renderCape(float scale) {
        this.bipedCape.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, @Nonnull Entity entityIn) {
        DataAnimation animation = null;
        if (entityIn instanceof EntityPlayer) {
            PlayerData data = PlayerData.get((EntityPlayer) entityIn);
            animation = data.animation;
            this.isSneak = entityIn.isSneaking();
            if (this.isSneak && ((EntityPlayer) entityIn).isPlayerSleeping()) { this.isSneak = false; }
        }
        else if (entityIn instanceof EntityCustomNpc) {
            if (entityIn instanceof EntityNPC64x32 || entityIn instanceof EntityNpcAlex || entityIn instanceof EntityNpcClassicPlayer) {
                for (Entity e : entityIn.world.getLoadedEntityList()) {
                    if (e.posX == entityIn.posX && e.posY == entityIn.posY && e.posZ == entityIn.posZ && e.getName().equals(entityIn.getName())) {
                        entityIn = e;
                        break;
                    }
                }
            }
            animation = ((EntityNPCInterface) entityIn).animation;
            if (((EntityCustomNpc) entityIn).navigating != null && (netHeadYaw < -2.0f || netHeadYaw > 2.0f)) {
                entityIn.turn(netHeadYaw / 3.0f, headPitch / 3.0f);
                ((IModelPlayerMixin) this).npcs$setBipedCape(this.bipedCape);
                ((IEntityLivingBaseMixin) entityIn).npcs$setInterpTargetYaw(entityIn.rotationYaw);
                ((IEntityLivingBaseMixin) entityIn).npcs$setInterpTargetPitch(entityIn.rotationPitch);
            }
            if (!this.isRiding) { this.isRiding = ((EntityCustomNpc) entityIn).currentAnimation == 1; }
            if (((EntityCustomNpc) entityIn).currentAnimation == 6 || (((EntityCustomNpc) entityIn).inventory.getProjectile() != null && ((EntityCustomNpc) entityIn).isAttacking() && ((EntityCustomNpc) entityIn).stats.ranged.getHasAimAnimation())) {
                this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            this.isSneak = entityIn.isSneaking();
            if (this.isSneak && ((EntityCustomNpc) entityIn).isPlayerSleeping()) { this.isSneak = false; }

            // Standard Rotation
            if (((EntityCustomNpc) entityIn).hurtTime != 0 && animation.isAnimated(AnimationKind.HIT)) {
                limbSwingAmount = 0.0f;
            }
            else if (((EntityCustomNpc) entityIn).isSwingInProgress && animation.isAnimated(AnimationKind.SWING, AnimationKind.ATTACKING)) {
                this.swingProgress = 0.0f;
                ((EntityCustomNpc) entityIn).swingProgress = 0.0f;
                ((EntityCustomNpc) entityIn).swingProgressInt = 0;
                ((EntityCustomNpc) entityIn).prevSwingProgress = 0.0f;
                ((EntityCustomNpc) entityIn).isSwingInProgress = false;
            }
        }
        this.clearAllRotations();
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        if (isClassicPlayer) {
            AniClassicPlayer.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
        }
        // Parts scales
        if (entityIn instanceof EntityCustomNpc) {
            animation.resetShowParts();
            bipedHead.showModel = true;
            bipedBody.showModel = true;
            bipedLeftArm.showModel = true;
            bipedRightArm.showModel = true;
            bipedLeftLeg.showModel = true;
            bipedRightLeg.showModel = true;
            ((ModelRendererAlt) bipedHead).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.HEAD));
            ((ModelRendererAlt) bipedBody).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.BODY));
            ((ModelRendererAlt) bipedLeftArm).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.ARM_LEFT));
            ((ModelRendererAlt) bipedRightArm).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.ARM_RIGHT));
            ((ModelRendererAlt) bipedLeftLeg).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.LEG_LEFT));
            ((ModelRendererAlt) bipedRightLeg).setBaseData(((EntityCustomNpc) entityIn).modelData.getPartConfig(EnumParts.LEG_RIGHT));
            // Mod Animation
            if (((EntityNPCInterface) entityIn).getAttackTarget() == null) {
                if (((EntityNPCInterface) entityIn).isPlayerSleeping()) {
                    if (this.bipedHead.rotateAngleX < 0.0f) {
                        this.bipedHead.rotateAngleX = 0.0f;
                    }
                } else if (this.isSneak) {
                    if (this.bipedCape != null) { this.bipedCape.offsetAnimY = -0.475f; }
                    if (this.bipedCape != null) { this.bipedCape.offsetAnimZ = -0.235f; }
                    rightStackData.partSets[4] = -0.475f;
                    leftStackData.partSets[4] = -0.475f;
                } else if (((EntityNPCInterface) entityIn).currentAnimation != 0) {
                    int animType = ((EntityNPCInterface) entityIn).currentAnimation;
                    if (animType == 3) {
                        AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 5) {
                        AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 7) {
                        AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 8) {
                        AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 9) {
                        this.bipedHead.rotateAngleX = 0.7f;
                    } else if (animType == 10) {
                        AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 11) {
                        AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 12) {
                        AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    } else if (animType == 13) {
                        AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, this);
                    }
                }
            }
            else if (((EntityNPCInterface) entityIn).ais.getStandingType() == 4 && ((EntityNPCInterface) entityIn).lookat != null) {
                double d0 = entityIn.posX - ((EntityNPCInterface) entityIn).lookat.posX;
                double d1 = (entityIn.posY + (double) entityIn.getEyeHeight()) - (((EntityNPCInterface) entityIn).lookat.posY + (double) ((EntityNPCInterface) entityIn).lookat.getEyeHeight());
                double d2 = entityIn.posZ - ((EntityNPCInterface) entityIn).lookat.posZ;
                double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
                float yaw = MathHelper.wrapDegrees(((EntityNPCInterface) entityIn).rotationYawHead - (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F);
                float pitch = MathHelper.wrapDegrees(entityIn.rotationPitch + (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI))));
                if (yaw < -45.0f) { yaw = -45.0f; }
                else if (yaw > 45.0f) { yaw = 45.0f; }
                this.bipedHead.rotateAngleY = (float) ((-yaw * Math.PI) / 180D);
                if (pitch < -45.0f) { pitch = -45.0f; }
                else if (pitch > 45.0f) { pitch = 45.0f; }
                this.bipedHead.rotateAngleX = (float) ((-pitch * Math.PI) / 180D);
            }
        } else {
            if (this.isSneak) {
                this.bipedCape.offsetAnimY = -0.475f;
                this.bipedCape.offsetAnimZ = -0.235f;
                rightStackData.partSets[4] = -0.475f;
                leftStackData.partSets[4] = -0.475f;
            }
        }
        if (CustomNpcs.ShowCustomAnimation && animation != null) {
            if (animation.isAnimated()) {
                // Custom Animation
                float partialTicks = 0.0f;
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen == null || mc.currentScreen.isFocused()) { partialTicks = mc.getRenderPartialTicks(); }
                animation.calculationAnimationBeforeRendering(partialTicks);
                if (entityIn.equals(ModelNpcAlt.editAnimDataSelect.displayNpc)) { this.bipedHead.rotateAngleY = 0.0f; }

                if (animation.showParts.get(EnumParts.HEAD)) { ((ModelRendererAlt) this.bipedHead).setAnimation(animation); }
                if (animation.showParts.get(EnumParts.BODY)) { ((ModelRendererAlt) this.bipedBody).setAnimation(animation); }
                if (animation.showParts.get(EnumParts.ARM_RIGHT)) {
                    ((ModelRendererAlt) this.bipedRightArm).setAnimation(animation);
                    this.rightStackData.setAnimation(animation, EnumParts.RIGHT_STACK.patterns);
                }
                if (animation.showParts.get(EnumParts.ARM_LEFT)) {
                    ((ModelRendererAlt) this.bipedLeftArm).setAnimation(animation);
                    this.leftStackData.setAnimation(animation, EnumParts.LEFT_STACK.patterns);
                }
                if (animation.showParts.get(EnumParts.LEG_RIGHT)) { ((ModelRendererAlt) this.bipedRightLeg).setAnimation(animation); }
                if (animation.showParts.get(EnumParts.LEG_LEFT)) { ((ModelRendererAlt) this.bipedLeftLeg).setAnimation(animation); }
            }
            // remember current state to smoothly start another animation
            if (animation.hasAnimations(-1)) {  animation.preFrame.setRotationAngles(this); }
        }
        if (CustomNpcs.HeadWearType != 2) {
            copyModelAngles((ModelRendererAlt) bipedHead, (ModelRendererAlt) bipedHeadwear);
            if (bipedHeadwear_64 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_64); }
            if (bipedHeadwear_128 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_128); }
            if (bipedHeadwear_256 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_256); }
            if (bipedHeadwear_512 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_512); }
            if (bipedHeadwear_1024 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_1024); }
            if (bipedHeadwear_2048 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_2048); }
            if (bipedHeadwear_4096 != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_4096); }
            if (bipedHeadwear_64_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_64_old); }
            if (bipedHeadwear_128_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_128_old); }
            if (bipedHeadwear_256_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_256_old); }
            if (bipedHeadwear_512_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_512_old); }
            if (bipedHeadwear_1024_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_1024_old); }
            if (bipedHeadwear_2048_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_2048_old); }
            if (bipedHeadwear_4096_old != null) { copyModelAngles((ModelRendererAlt) bipedHead, bipedHeadwear_4096_old); }
        }
        if (this.bipedBody != null && this.bipedBodyWear != null)  {
            if (this.bipedBody instanceof ModelRendererAlt && this.bipedBodyWear instanceof ModelRendererAlt) { copyModelAngles((ModelRendererAlt) this.bipedBody, (ModelRendererAlt) this.bipedBodyWear); }
            else { copyModelAngles(this.bipedBody, this.bipedBodyWear); }
        }
        if (this.bipedRightArm != null && this.bipedRightArmwear != null)  {
            if (this.bipedRightArm instanceof ModelRendererAlt && this.bipedRightArmwear instanceof ModelRendererAlt) { copyModelAngles((ModelRendererAlt) this.bipedRightArm, (ModelRendererAlt) this.bipedRightArmwear); }
            else { copyModelAngles(this.bipedRightArm, this.bipedRightArmwear); }
        }
        if (this.bipedLeftArm != null && this.bipedLeftArmwear != null)  {
            if (this.bipedLeftArm instanceof ModelRendererAlt && this.bipedLeftArmwear instanceof ModelRendererAlt) { copyModelAngles((ModelRendererAlt) this.bipedLeftArm, (ModelRendererAlt) this.bipedLeftArmwear); }
            else { copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear); }
        }
        if (this.bipedRightLeg != null && this.bipedRightLegwear != null)  {
            if (this.bipedRightLeg instanceof ModelRendererAlt && this.bipedRightLegwear instanceof ModelRendererAlt) { copyModelAngles((ModelRendererAlt) this.bipedRightLeg, (ModelRendererAlt) this.bipedRightLegwear); }
            else { copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear); }
        }
        if (this.bipedLeftLeg != null && this.bipedLeftLegwear != null)  {
            if (this.bipedLeftLeg instanceof ModelRendererAlt && this.bipedLeftLegwear instanceof ModelRendererAlt) { copyModelAngles((ModelRendererAlt) this.bipedLeftLeg, (ModelRendererAlt) this.bipedLeftLegwear); }
            else { copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear); }
        }
    }

    @Override
    public void postRenderArm(float scale, @Nonnull EnumHandSide side) { // for ItemStacks
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
        if (hundData.partSets == null) { return; }
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
        if (this.bipedCape != null && this.bipedCape.showModel) { this.bipedCape.clearRotations(); }
        // Arm left
        if (this.bipedLeftArm.showModel) { ((ModelRendererAlt) this.bipedLeftArm).clearRotations(); }
        leftStackData.clear();
        // Arm right
        if (this.bipedRightArm.showModel) { ((ModelRendererAlt) this.bipedRightArm).clearRotations(); }
        rightStackData.clear();
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
