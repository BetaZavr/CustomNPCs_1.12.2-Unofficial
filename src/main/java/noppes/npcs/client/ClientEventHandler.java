package noppes.npcs.client;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

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
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.ObfuscationHelper;

public class ClientEventHandler {
	
	public static Map<ISchematic, Integer> displayMap;
	private boolean inGame;
	public static BlockPos schemaPos;
	public static Schematic schema;
	public static int rotaion;

	public ClientEventHandler() { ClientEventHandler.displayMap = Maps.<ISchematic, Integer>newHashMap(); }

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
		double[] d = ClientProxy.playerData.hud.getWindowSize();
		if (d[0] != scaleW.getScaledWidth_double() || d[1] != scaleW.getScaledHeight_double()) {
			d[0] = scaleW.getScaledWidth_double();
			d[1] = scaleW.getScaledHeight_double();
			if (mc.player != null) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTTagList list = new NBTTagList();
				list.appendTag(new NBTTagDouble(d[0]));
				list.appendTag(new NBTTagDouble(d[1]));
				compound.setTag("WindowSize", list);
				NoppesUtilPlayer.sendData(EnumPlayerPacket.WindowSize, compound);
			}
			ClientProxy.playerData.hud.clearGuiComponents();
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
		CustomNpcs.debugData.startDebug("Client", player, "ClientEventHandler_onRenderTick");
		ClientEventHandler.schema = null;
		ClientEventHandler.schemaPos = null;
		if (!player.getHeldItemMainhand().isEmpty() &&
				player.getHeldItemMainhand().getItem() instanceof ItemBuilder &&
				player.getHeldItemMainhand().hasTagCompound() &&
				CommonProxy.dataBuilder.containsKey(player.getHeldItemMainhand().getTagCompound().getInteger("ID")) &&
				ClientGuiEventHandler.result!=null &&
				ClientGuiEventHandler.result.getBlockPos()!=null) {
			BuilderData bd = CommonProxy.dataBuilder.get(player.getHeldItemMainhand().getTagCompound().getInteger("ID"));
			if (bd.type==3 && !bd.schematicaName.isEmpty()) {
				SchematicWrapper schema = SchematicController.Instance.getSchema(bd.schematicaName+".schematic");
				if (schema!=null) {
					Schematic sc = (Schematic) schema.schema;
					ClientEventHandler.schemaPos = ClientGuiEventHandler.result.getBlockPos();
					ClientEventHandler.rotaion = MathHelper.floor(player.rotationYaw / 90.0f + 0.5f) & 0x3;
					int x = -1, y=0, z = -1;
					boolean has = sc.offset!=null && sc.offset.length>=3 && (sc.offset[0]!=0 || sc.offset[1]!=0 || sc.offset[2]!=0);
					switch(ClientEventHandler.rotaion) {
						case 1: {
							if (has) {
								x = -1*sc.offset[2] - sc.getLength();
								y = sc.offset[1];
								z = sc.offset[0]-1;
							}
							else {
								x = -1*sc.getLength();
								z = (int) (-1 * Math.ceil((double)sc.getWidth()/2.0d));
							}
							break;
						}
						case 2: {
							if (has) {
								x = -1*sc.offset[0]-sc.getWidth();
								y = sc.offset[1];
								z = -1*sc.offset[2]-sc.getLength();
							}
							else {
								x = (int) (-1 * Math.ceil((double)sc.getWidth()/2.0d));
								z = -1 * sc.getLength();
							}
							break;
						}
						case 3: {
							if (has) {
								x = sc.offset[2]-1;
								y = sc.offset[1];
								z = -1*sc.offset[0]-sc.getWidth();
							}
							else {
								x = -1;
								z = (int) (-1 * Math.ceil((double)sc.getWidth()/2.0d));
							}
							break;
						}
						default: {
							if (has) {
								x = sc.offset[0]-1;
								y = sc.offset[1];
								z = sc.offset[2]-1;
							} else {
								x = (int) (-1 * Math.ceil((double)sc.getWidth()/2.0d));
							}
							break;
						}
					}
					ClientEventHandler.schema = sc;
					ClientEventHandler.schemaPos = ClientEventHandler.schemaPos.add(x, y, z);
					this.drawSchematic(ClientEventHandler.schemaPos, schema, 0, ClientEventHandler.rotaion);
				}
			}
		}
		if (TileBuilder.DrawPoses.isEmpty()) { return; }
		for (BlockPos pos : TileBuilder.DrawPoses) {
			if (pos==null || player == null || pos.distanceSq((Vec3i) player.getPosition()) > 10000.0) { continue; }
			TileEntity te = player.world.getTileEntity(pos);
			if (!(te instanceof TileBuilder)) { continue; }
			this.drawSchematic(pos, ((TileBuilder) te).getSchematic(), ((TileBuilder) te).yOffest, ((TileBuilder) te).rotation);
		}
		CustomNpcs.debugData.endDebug("Client", player, "ClientEventHandler_onRenderTick");
	}

	private void drawSchematic(BlockPos pos, SchematicWrapper schem, int yOffest, int rotation) {
		if (pos==null || schem==null) { return; }
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(pos.getX() - TileEntityRendererDispatcher.staticPlayerX, pos.getY() - TileEntityRendererDispatcher.staticPlayerY + 0.01, pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ);
		GlStateManager.translate(1.0f, yOffest, 1.0f);
		// Bound
		if (rotation % 2 == 0) { this.drawSelectionBox(new BlockPos(schem.schema.getWidth(), schem.schema.getHeight(), schem.schema.getLength())); }
		else { this.drawSelectionBox(new BlockPos(schem.schema.getLength(), schem.schema.getHeight(), schem.schema.getWidth())); }
		
		if (ClientEventHandler.displayMap.containsKey(schem.schema)) {
			GlStateManager.rotate(rotation*-90.0f, 0.0f, 1.0f, 0.0f);
			switch(rotation) {
				case 1: { GlStateManager.translate(0, 0, -1*schem.schema.getLength()); break; }
				case 2: { GlStateManager.translate(-1*schem.schema.getWidth(), 0, -1*schem.schema.getLength()); break; }
				case 3: { GlStateManager.translate(-1*schem.schema.getWidth(), 0, 0); break;}
			}
			GlStateManager.callList(ClientEventHandler.displayMap.get(schem.schema));
		} else {
			ClientEventHandler.displayMap.put(schem.schema, GLAllocation.generateDisplayLists(1));
			GL11.glNewList(ClientEventHandler.displayMap.get(schem.schema), 4864);
			try {
				for (int i = 0; i < schem.size && i < 25000; ++i) {
					int posX = i % schem.schema.getWidth();
					int posZ = (i - posX) / schem.schema.getWidth() % schem.schema.getLength();
					int posY = ((i - posX) / schem.schema.getWidth() - posZ) / schem.schema.getLength();
					IBlockState state = schem.schema.getBlockState(posX, posY, posZ);
					if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
						BlockPos p = schem.rotatePos(posX, posY, posZ, 0);
						GlStateManager.pushMatrix();
						GlStateManager.pushAttrib();
						GlStateManager.enableRescaleNormal();
						GlStateManager.translate(p.getX(), p.getY(), p.getZ());
						Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
						GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
						state = schem.rotationState(state, 0);
						try {
							this.renderBlock(state);
							if (GL11.glGetError() != 0) { break; }
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
				if (GL11.glGetError() != 0) { ClientEventHandler.displayMap.remove(schem.schema); }
			}
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
	}

	private void renderBlock(IBlockState state) {
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		dispatcher.renderBlockBrightness(state, 1.0f);
		/*switch (state.getRenderType())  {
			case MODEL:
				IBakedModel ibakedmodel = dispatcher.getModelForState(state);
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				BlockModelRenderer bmr = ObfuscationHelper.getValue(BlockRendererDispatcher.class, dispatcher, BlockModelRenderer.class);
				BlockColors bc = ObfuscationHelper.getValue(BlockModelRenderer.class, bmr, BlockColors.class);
				int color = bc.colorMultiplier(state, (IBlockAccess)null, (BlockPos)null, 0);
				if (EntityRenderer.anaglyphEnable) { color = TextureUtil.anaglyphColor(color); }
				float r = (float)(color >> 16 & 255) / 255.0F;
				float g = (float)(color >> 8 & 255) / 255.0F;
				float b = (float)(color & 255) / 255.0F;
				for (EnumFacing enumfacing : EnumFacing.values()) { this.renderModelBlockQuads(ibakedmodel.getQuads(state, enumfacing, 0L), r, g, b); }
				this.renderModelBlockQuads(ibakedmodel.getQuads(state, (EnumFacing)null, 0L), r, g, b);
				break;
			case ENTITYBLOCK_ANIMATED:
				//this.chestRenderer.renderChestBrightness(state.getBlock(), brightness);
			case LIQUID: break;
			default: break;
		}*/
	}
	
	private void renderModelBlockQuads(List<BakedQuad> quads, float r, float g, float b) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		for (int j = quads.size(), i = 0; i < j; ++i) {
			BakedQuad bakedquad = quads.get(i);
			bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
			bufferbuilder.addVertexData(bakedquad.getVertexData());
			if (bakedquad.hasTintIndex()) { bufferbuilder.putColorRGB_F4(r, g, b); }
			else { bufferbuilder.putColorRGB_F4(1.0f, 1.0f, 1.0f); }
			Vec3i vec3i = bakedquad.getFace().getDirectionVec();
			bufferbuilder.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
			tessellator.draw();
		}
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

	@SubscribeEvent
	public void clientDisconnect(ClientDisconnectionFromServerEvent event) {
		if (!event.getManager().isLocalChannel()) { ClientHandler.getInstance().cleanUp(); }
	}
	
}
