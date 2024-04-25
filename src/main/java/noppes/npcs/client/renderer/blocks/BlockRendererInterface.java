package noppes.npcs.client.renderer.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public abstract class BlockRendererInterface<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

	public static float[][] colorTable = new float[][] { { 1.0f, 1.0f, 1.0f }, { 0.95f, 0.7f, 0.2f },
			{ 0.9f, 0.5f, 0.85f }, { 0.6f, 0.7f, 0.95f }, { 0.9f, 0.9f, 0.2f }, { 0.5f, 0.8f, 0.1f },
			{ 0.95f, 0.7f, 0.8f }, { 0.3f, 0.3f, 0.3f }, { 0.6f, 0.6f, 0.6f }, { 0.3f, 0.6f, 0.7f },
			{ 0.7f, 0.4f, 0.9f }, { 0.2f, 0.4f, 0.8f }, { 0.5f, 0.4f, 0.3f }, { 0.4f, 0.5f, 0.2f },
			{ 0.8f, 0.3f, 0.3f }, { 0.1f, 0.1f, 0.1f } };
	protected static ResourceLocation Diamond = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/diamond_block.png");
	protected static ResourceLocation Gold = new ResourceLocation(CustomNpcs.MODID, "textures/cache/gold_block.png");
	protected static ResourceLocation Iron = new ResourceLocation(CustomNpcs.MODID, "textures/cache/iron_block.png");
	protected static ResourceLocation PlanksAcacia = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_acacia.png");
	protected static ResourceLocation PlanksBigOak = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_big_oak.png");
	protected static ResourceLocation PlanksBirch = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_birch.png");
	protected static ResourceLocation PlanksJungle = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_jungle.png");
	protected static ResourceLocation PlanksOak = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_oak.png");
	protected static ResourceLocation PlanksSpruce = new ResourceLocation(CustomNpcs.MODID,
			"textures/cache/planks_spruce.png");
	protected static ResourceLocation Steel = new ResourceLocation(CustomNpcs.MODID, "textures/models/Steel.png");
	protected static ResourceLocation Stone = new ResourceLocation(CustomNpcs.MODID, "textures/cache/stone.png");

	public static void setMaterialTexture(int meta) {
		TextureManager manager = Minecraft.getMinecraft().getTextureManager();
		if (meta == 1) {
			manager.bindTexture(BlockRendererInterface.Stone);
		} else if (meta == 2) {
			manager.bindTexture(BlockRendererInterface.Iron);
		} else if (meta == 3) {
			manager.bindTexture(BlockRendererInterface.Gold);
		} else if (meta == 4) {
			manager.bindTexture(BlockRendererInterface.Diamond);
		} else {
			manager.bindTexture(BlockRendererInterface.PlanksOak);
		}
	}

	public boolean playerTooFar(TileEntity tile) {
		Minecraft mc = Minecraft.getMinecraft();
		double d6 = mc.getRenderViewEntity().posX - tile.getPos().getX();
		double d7 = mc.getRenderViewEntity().posY - tile.getPos().getY();
		double d8 = mc.getRenderViewEntity().posZ - tile.getPos().getZ();
		return d6 * d6 + d7 * d7 + d8 * d8 > this.specialRenderDistance() * this.specialRenderDistance();
	}

	public void setWoodTexture(int meta) {
		TextureManager manager = Minecraft.getMinecraft().getTextureManager();
		if (meta == 1) {
			manager.bindTexture(BlockRendererInterface.PlanksSpruce);
		} else if (meta == 2) {
			manager.bindTexture(BlockRendererInterface.PlanksBirch);
		} else if (meta == 3) {
			manager.bindTexture(BlockRendererInterface.PlanksJungle);
		} else if (meta == 4) {
			manager.bindTexture(BlockRendererInterface.PlanksAcacia);
		} else if (meta == 5) {
			manager.bindTexture(BlockRendererInterface.PlanksBigOak);
		} else {
			manager.bindTexture(BlockRendererInterface.PlanksOak);
		}
	}

	public int specialRenderDistance() {
		return 20;
	}

}
