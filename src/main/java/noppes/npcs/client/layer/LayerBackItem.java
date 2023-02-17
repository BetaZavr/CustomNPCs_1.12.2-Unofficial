package noppes.npcs.client.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.wrapper.ItemStackWrapper;

@SuppressWarnings("deprecation")
public class LayerBackItem<T extends EntityLivingBase> extends LayerInterface<T> {
	public LayerBackItem(RenderLiving<?> render) {
		super(render);
	}

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		Minecraft minecraft = Minecraft.getMinecraft();
		ItemStack itemstack = ItemStackWrapper.MCItem(this.npc.inventory.getRightHand());
		if (NoppesUtilServer.IsItemStackNull(itemstack) || this.npc.isAttacking()) {
			return;
		}
		Item item = itemstack.getItem();
		if (item instanceof ItemBlock) {
			return;
		}
		this.model.bipedBody.postRender(par7);
		GlStateManager.translate(0.0, 0.36, 0.14);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		if (item instanceof ItemSword) {
			GlStateManager.rotate(180.0f, -1.0f, 0.0f, 0.0f);
		}
		IBakedModel model = minecraft.getRenderItem().getItemModelMesher().getItemModel(itemstack);
		ItemTransformVec3f thirdperson_right = model.getItemCameraTransforms().thirdperson_right;
		GlStateManager.scale(thirdperson_right.scale.x + ItemCameraTransforms.offsetScaleX,
				thirdperson_right.scale.y + ItemCameraTransforms.offsetScaleY,
				thirdperson_right.scale.z + ItemCameraTransforms.offsetScaleZ);
		minecraft.getItemRenderer().renderItem(this.npc, itemstack, ItemCameraTransforms.TransformType.NONE);
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) {
	}
}
