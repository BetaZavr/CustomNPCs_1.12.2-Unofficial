package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelShield;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.blocks.tiles.TileEntityCustomBanner;
import noppes.npcs.util.ObfuscationHelper;

import javax.annotation.Nullable;

public class TileEntityItemStackCustomRenderer extends TileEntityItemStackRenderer {

	private final TileEntityCustomBanner banner = new TileEntityCustomBanner();
	private final ModelShield modelShield = new ModelShield();
	private final ModelRenderer customBannerSlate;

	public TileEntityItemStackCustomRenderer() {
		customBannerSlate = new ModelRenderer(modelShield, 0, 0);
		customBannerSlate.addBox(-10.0F, -32.0F, -2.0F, 20, 40, 1, 0.0F);
		ModelBox list = customBannerSlate.cubeList.get(0);
		PositionTextureVertex[] vp = ObfuscationHelper.getValue(ModelBox.class, list, 0);
		if (vp == null) { return; }
		TexturedQuad[] quadList = new TexturedQuad[6];
		quadList[0] = new TexturedQuad(new PositionTextureVertex[] { vp[5], vp[1], vp[2], vp[6] }, 11, 1, 12, 17, 64, 32); // right
		quadList[1] = new TexturedQuad(new PositionTextureVertex[] { vp[0], vp[4], vp[7], vp[3] }, 0, 1, 1, 17, 64, 32); // left
		quadList[2] = new TexturedQuad(new PositionTextureVertex[] { vp[5], vp[4], vp[0], vp[1] }, 1, 0, 11, 0, 64, 32); // top
		quadList[3] = new TexturedQuad(new PositionTextureVertex[] { vp[2], vp[3], vp[7], vp[6] }, 11, 0, 21, 1, 64, 32); // bottom
		quadList[4] = new TexturedQuad(new PositionTextureVertex[] { vp[1], vp[0], vp[3], vp[2] }, 1, 1, 11, 17, 64, 32); // front
		quadList[5] = new TexturedQuad(new PositionTextureVertex[] { vp[4], vp[5], vp[6], vp[7] }, 12, 1, 22, 17, 64, 32); // back
		ObfuscationHelper.setValue(ModelBox.class, list, quadList, 1);
	}

	@Override
	public void renderByItem(@Nullable ItemStack stack, float partialTicks) {
		if (stack == null) { return; }
		Item item = stack.getItem();
		if (item == Items.SHIELD && stack.getSubCompound("BlockEntityTag") != null
				&& stack.getTagCompound() != null
				&& stack.getTagCompound().getCompoundTag("BlockEntityTag").hasKey("FactionID")) {
			banner.setItemValues(stack, true);
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.0F, -1.0F, -1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(BannerTextures.SHIELD_BASE_TEXTURE);
			modelShield.render();
			ResourceLocation loc = banner.getFactionFlag();
			if (loc != null) {
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
				GlStateManager.translate(0.0f, 6.0f / 16.0f, 0.075f / 16.0f);
				GlStateManager.scale(0.5f, 0.5f, 1.05f);
				customBannerSlate.render(0.0625F);
				GlStateManager.popMatrix();
			}
			GlStateManager.popMatrix();
			return;
		}
		super.renderByItem(stack, partialTicks);
	}

}
