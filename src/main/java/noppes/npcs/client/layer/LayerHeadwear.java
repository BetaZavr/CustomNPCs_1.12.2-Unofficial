package noppes.npcs.client.layer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.model.part.head.ModelHeadwear;
import noppes.npcs.entity.EntityCustomNpc;

public class LayerHeadwear<T extends EntityLivingBase>
extends LayerInterface<T>
implements LayerPreRender {
	
	private ModelHeadwear headwear;
	private ResourceLocation textureLocation;
	public EntityCustomNpc npc;

	public LayerHeadwear(RenderLiving<?> render) {
		super(render);
		this.headwear = new ModelHeadwear((ModelBase) this.model);
	}

	@Override
	public void preRender(EntityCustomNpc player) {
		this.model.bipedHeadwear.isHidden = (CustomNpcs.HeadWearType == 1);
		if (player!=null) {
			this.npc  = player;
			this.textureLocation = player.textureLocation;
		}
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
		if (this.textureLocation!=null) { ClientProxy.bindTexture(this.textureLocation); }
		else { ClientProxy.bindTexture(this.npc.textureLocation); }
		this.model.bipedHead.postRender(scale);
		GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
		this.headwear.render(scale);
		GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	}

	@Override
	public void rotate(float par2, float par3, float par4, float par5, float par6, float par7) { }
	
}
