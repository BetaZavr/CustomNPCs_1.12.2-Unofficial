package noppes.npcs.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerGameData;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.AdditionalMethods;

public class ClientEventHandler {
	
	private int displayList;
	private boolean inGame;

	public ClientEventHandler() {
		this.displayList = -1;
	}

	public void drawSelectionBox(BlockPos pos) {
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		AxisAlignedBB bb = new AxisAlignedBB(BlockPos.ORIGIN, pos);
		RenderGlobal.drawSelectionBoundingBox(bb, 1.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
	}
	
	// New
	@SubscribeEvent
	public void onOpenGUIEvent(GuiOpenEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		CustomNpcs.debugData.startDebug("Client", mc.player, "ClientEventHandler_onOpenGUIEvent");
		LogWriter.debug(((event.getGui() == null ? "Cloce GUI " : "Open GUI - " + event.getGui().getClass())
				+ "; OLD - " + (mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName())));
		if (event.getGui() instanceof GuiNpcCarpentryBench || event.getGui() instanceof GuiCrafting) {
			AdditionalMethods.resetRecipes(mc.player, (GuiContainer) event.getGui());
		} else if (event.getGui() instanceof GuiInventory && !mc.player.capabilities.isCreativeMode) {
			AdditionalMethods.resetRecipes(mc.player, (GuiContainer) event.getGui());
		}
		if (event.getGui() instanceof GuiOptions && mc.currentScreen instanceof GuiLanguage) {
			ClientProxy.checkLocalization();
		}
		ScaledResolution scaleW = new ScaledResolution(mc);
		PlayerGameData data = ClientProxy.playerData.game;
		if (data.windowSize[0] != scaleW.getScaledWidth_double()
				|| data.windowSize[1] != scaleW.getScaledHeight_double()) {
			data.windowSize[0] = scaleW.getScaledWidth_double();
			data.windowSize[1] = scaleW.getScaledHeight_double();
			if (mc.player != null) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTTagList list = new NBTTagList();
				list.appendTag(new NBTTagDouble(data.windowSize[0]));
				list.appendTag(new NBTTagDouble(data.windowSize[1]));
				compound.setTag("WindowSize", list);
				NoppesUtilPlayer.sendData(EnumPlayerPacket.WindowSize, compound);
			}
		}
		if (event.getGui() instanceof GuiIngameMenu) {
			if (!this.inGame) { // Login
				this.inGame = true;
			}
		} else if (event.getGui() instanceof GuiMainMenu) {
			if (this.inGame) { // Logout
				this.inGame = false;
				CustomNpcs.showDebugs();
			}
		}
		CustomNpcs.debugData.endDebug("Client", mc.player, "ClientEventHandler_onOpenGUIEvent");
	}

	@SubscribeEvent
	public void onRenderTick(RenderWorldLastEvent event) {
		EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().player;
		if (TileBuilder.DrawPos == null || TileBuilder.DrawPos.distanceSq((Vec3i) player.getPosition()) > 1000000.0) {
			return;
		}
		TileEntity te = player.world.getTileEntity(TileBuilder.DrawPos);
		if (te == null || !(te instanceof TileBuilder)) {
			return;
		}
		TileBuilder tile = (TileBuilder) te;
		SchematicWrapper schem = tile.getSchematic();
		if (schem == null) {
			return;
		}
		CustomNpcs.debugData.startDebug("Client", player, "ClientEventHandler_onRenderTick");
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(TileBuilder.DrawPos.getX() - TileEntityRendererDispatcher.staticPlayerX,
				TileBuilder.DrawPos.getY() - TileEntityRendererDispatcher.staticPlayerY + 0.01,
				TileBuilder.DrawPos.getZ() - TileEntityRendererDispatcher.staticPlayerZ);
		GlStateManager.translate(1.0f, tile.yOffest, 1.0f);
		if (tile.rotation % 2 == 0) {
			this.drawSelectionBox(
					new BlockPos(schem.schema.getWidth(), schem.schema.getHeight(), schem.schema.getLength()));
		} else {
			this.drawSelectionBox(
					new BlockPos(schem.schema.getLength(), schem.schema.getHeight(), schem.schema.getWidth()));
		}

		if (TileBuilder.Compiled) {
			GlStateManager.callList(this.displayList);
		} else {
			BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			if (this.displayList >= 0) {
				GLAllocation.deleteDisplayLists(this.displayList);
			}
			GL11.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
			try {
				for (int i = 0; i < schem.size && i < 25000; ++i) {
					int posX = i % schem.schema.getWidth();
					int posZ = (i - posX) / schem.schema.getWidth() % schem.schema.getLength();
					int posY = ((i - posX) / schem.schema.getWidth() - posZ) / schem.schema.getLength();
					IBlockState state = schem.schema.getBlockState(posX, posY, posZ);
					if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
						BlockPos pos = schem.rotatePos(posX, posY, posZ, tile.rotation);
						GlStateManager.pushMatrix();
						GlStateManager.pushAttrib();
						GlStateManager.enableRescaleNormal();
						GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
						Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
						GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
						state = schem.rotationState(state, tile.rotation);
						try {
							dispatcher.renderBlockBrightness(state, 1.0f);
							if (GL11.glGetError() != 0) {
								break;
							}
						} catch (Exception ex) {
						} finally {
							GlStateManager.popAttrib();
							GlStateManager.disableRescaleNormal();
							GlStateManager.popMatrix();
						}
					}
				}
			} catch (Exception e) {
				LogWriter.error("Error preview builder block", e);
			} finally {
				GL11.glEndList();
				if (GL11.glGetError() == 0) {
					TileBuilder.Compiled = true;
				}
			}
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
		
		CustomNpcs.debugData.endDebug("Client", player, "ClientEventHandler_onRenderTick");
	}

	@SubscribeEvent
	public void post(RenderLivingEvent.Post<EntityLivingBase> event) {
		CustomNpcs.debugData.startDebug("Client", event.getEntity(), "ClientEventHandler_postRenderLivingEvent");
		MarkData data = MarkData.get(event.getEntity());
		for (MarkData.Mark m : data.marks) {
			if (m.getType() != 0 && m.availability.isAvailable(Minecraft.getMinecraft().player)) {
				MarkRenderer.render(event.getEntity(), event.getX(), event.getY(), event.getZ(), m);
				break;
			}
		}
		CustomNpcs.debugData.endDebug("Client", event.getEntity(), "ClientEventHandler_postRenderLivingEvent");
	}

}
