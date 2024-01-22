package noppes.npcs.client.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.model.part.head.ModelHeadwear;
import noppes.npcs.entity.EntityCustomNpc;

public class LayerHeadwear<T extends EntityLivingBase>
extends LayerInterface<T>
implements LayerPreRender {
	
	private ModelHeadwear headwear;
	public EntityCustomNpc npc;

	public LayerHeadwear(RenderLiving<?> render) {
		super(render);
		this.headwear = new ModelHeadwear(this.model);
	}

	@Override
	public void preRender(EntityCustomNpc player) {
		this.model.bipedHeadwear.isHidden = (CustomNpcs.HeadWearType == 1);
		if (player!=null) { this.npc  = player; }
	}

	@Override
	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (CustomNpcs.HeadWearType != 1 || this.npc==null) { return; }
		if (this.npc.hurtTime <= 0 && this.npc.deathTime <= 0) {
			int color = this.npc.display.getTint();
			float red = (color >> 16 & 0xFF) / 255.0f;
			float green = (color >> 8 & 0xFF) / 255.0f;
			float blue = (color & 0xFF) / 255.0f;
			GlStateManager.color(red, green, blue, 1.0f);
		}
		ClientProxy.bindTexture(this.npc.textureLocation);
		this.model.bipedHead.postRender(scale);
		GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);

		boolean isInvisible = false;
		if (this.npc.display.getVisible() == 1) { isInvisible = this.npc.display.getAvailability().isAvailable(Minecraft.getMinecraft().player); }
		else if (this.npc.display.getVisible() == 2) { isInvisible = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() != CustomRegisters.wand; }
		if (isInvisible) {
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.alphaFunc(516, 0.003921569f);
		}
		this.headwear.render(scale);
		if (isInvisible) {
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(516, 0.1f);
			GlStateManager.popMatrix();
			GlStateManager.depthMask(true);
		}
		GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	}

	@Override
	public void rotate(float par2, float par3, float par4, float par5, float par6, float par7) { }
	
}
