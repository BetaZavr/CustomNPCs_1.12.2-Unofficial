package noppes.npcs.client.renderer.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockNpcDoorInterface;
import noppes.npcs.blocks.tiles.TileDoor;

public class BlockDoorRenderer<T extends TileEntity> extends BlockRendererInterface<T> {
	static {
		new Random();
	}

	private boolean overrideModel() {
		ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
		return held != null && (held.getItem() == CustomItems.wand || held.getItem() == CustomItems.scripter
				|| held.getItem() == CustomItems.scriptedDoorTool);
	}

	@SuppressWarnings("deprecation")
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		TileDoor tile = (TileDoor) te;
		IBlockState original = CustomItems.scriptedDoor.getStateFromMeta(tile.getBlockMetadata());
		BlockPos lowerPos = tile.getPos();
		if (original.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			lowerPos = tile.getPos().down();
		}
		BlockPos upperPos = lowerPos.up();
		TileDoor lowerTile = (TileDoor) this.getWorld().getTileEntity(lowerPos);
		TileDoor upperTile = (TileDoor) this.getWorld().getTileEntity(upperPos);
		if (lowerTile == null || upperTile == null) {
			return;
		}
		IBlockState lowerState = CustomItems.scriptedDoor.getStateFromMeta(lowerTile.getBlockMetadata());
		IBlockState upperState = CustomItems.scriptedDoor.getStateFromMeta(upperTile.getBlockMetadata());
		int meta = BlockNpcDoorInterface.combineMetadata((IBlockAccess) this.getWorld(), tile.getPos());
		Block b = lowerTile.blockModel;
		if (this.overrideModel()) {
			b = CustomItems.scriptedDoor;
		}
		IBlockState state = b.getStateFromMeta(meta);
		state = state.withProperty(BlockDoor.HALF, original.getValue(BlockDoor.HALF));
		state = state.withProperty(BlockDoor.FACING, lowerState.getValue(BlockDoor.FACING));
		state = state.withProperty(BlockDoor.OPEN, lowerState.getValue(BlockDoor.OPEN));
		state = state.withProperty(BlockDoor.HINGE, upperState.getValue(BlockDoor.HINGE));
		state = state.withProperty(BlockDoor.POWERED, upperState.getValue(BlockDoor.POWERED));
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
		this.renderBlock(tile, b, state);
		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
	}

	private void renderBlock(TileDoor tile, Block b, IBlockState state) {
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(-0.5f, 0.0f, 0.5f);
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel ibakedmodel = dispatcher.getBlockModelShapes().getModelForState(state);
		if (ibakedmodel == null) {
			dispatcher.renderBlockBrightness(state, 1.0f);
		} else {
			dispatcher.getBlockModelRenderer().renderModelBrightness(ibakedmodel, state, 1.0f, true);
		}
	}
}
