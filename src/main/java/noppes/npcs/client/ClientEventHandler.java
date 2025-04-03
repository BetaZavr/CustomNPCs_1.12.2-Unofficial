package noppes.npcs.client;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.client.gui.*;
import noppes.npcs.reflection.client.renderer.BlockModelRendererReflection;
import noppes.npcs.reflection.client.renderer.BlockRendererDispatcherReflection;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ChestRenderer;
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
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.item.ISpecBuilder;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.gui.GuiNbtBook;
import noppes.npcs.client.gui.GuiNpcPather;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MiniMapData;
import noppes.npcs.controllers.data.PlayerMiniMapData;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.CustomNPCsScheduler;

public class ClientEventHandler {

	public static final Map<EntityPlayer, RenderChatMessages> chatMessages = new HashMap<>();

	public static SubGuiInterface subgui;
	public static Map<ISchematic, Integer> displayMap = new HashMap<>();
	public static BlockPos schemaPos;
	public static Schematic schema;
	public static int rotation;
	public static long secs;
	private boolean miniMapLoaded;

	@SubscribeEvent
	public void cnpcOpenGUIEvent(GuiOpenEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientEventHandler_onOpenGUIEvent");
		ClientEventHandler.subgui = null;
		LogWriter.debug(((event.getGui() == null ? "Close GUI " : "Open GUI - " + event.getGui().getClass()) + "; OLD - " + (mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName())));

		String newGUI = event.getGui() == null ? "GuiIngame" : event.getGui().getClass().getSimpleName();
		if (event.getGui() instanceof GuiLog) {
			switch(((GuiLog) event.getGui()).type) {
				case 0: newGUI = "GuiFactionLog"; break;
				case 1: newGUI = "GuiQuestLog"; break;
				case 2: newGUI = "GuiCompassLog"; break;
				default: newGUI = "GuiInventoryLog"; break;
			}
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.OpenGui, newGUI, mc.currentScreen == null ? "GuiIngame" : mc.currentScreen.getClass().getSimpleName());
		if (mc.currentScreen instanceof GuiNpcPather) {
			ClientGuiEventHandler.movingPath.clear();
		}
		else if (event.getGui() instanceof GuiInventory) {
			if (mc.player.getHeldItemMainhand().getItem() instanceof ISpecBuilder) {
				event.setCanceled(true);
				ISpecBuilder item = (ISpecBuilder) mc.player.getHeldItemMainhand().getItem();
				BuilderData builder = ItemBuilder.getBuilder(mc.player.getHeldItemMainhand(), mc.player);
				int id = builder == null ? -1 : builder.getID();
				int type = builder == null ? item.getType() : builder.getType();
				if (id > -1) { NoppesUtilPlayer.sendData(EnumPlayerPacket.GetBuildData, id, type);
                    CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.Gui, item.getGUIType(), id, type, 0), 100);
                } else {
                    CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.Gui, item.getGUIType(), id, type, 0), 100);
                }
                return;
			}
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
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientEventHandler_onOpenGUIEvent");
	}

	@SubscribeEvent
	public void cnpcPostLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event) {
		CustomNpcs.debugData.startDebug("Client", event.getEntity(), "ClientEventHandler_postRenderLivingEvent");
		MarkData data = MarkData.get(event.getEntity());
		for (MarkData.Mark m : data.marks) {
			if (m.getType() != 0 && m.availability.isAvailable(Minecraft.getMinecraft().player)) {
				MarkRenderer.render(event.getEntity(), event.getX(), event.getY(), event.getZ(), m);
				break;
			}
		}
		if (event.getEntity() instanceof EntityPlayer && ClientEventHandler.chatMessages.containsKey((EntityPlayer) event.getEntity())) {
			EntityPlayer player = (EntityPlayer) event.getEntity();
			float height = event.getEntity().height + 0.9f;
			ClientEventHandler.chatMessages.get(player).renderPlayerMessages(event.getX(), event.getY() + height, event.getZ(), 0.666667f * height, this.isInRange(Minecraft.getMinecraft().player, event.getX(), event.getY() + 1.2d, event.getZ()));
		}
		CustomNpcs.debugData.endDebug("Client", event.getEntity(), "ClientEventHandler_postRenderLivingEvent");
	}

	@SubscribeEvent
	public void cnpcRenderTick(RenderWorldLastEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		CustomNpcs.debugData.startDebug("Client", player, "ClientEventHandler_onRenderTick");
		ClientEventHandler.schema = null;
		ClientEventHandler.schemaPos = null;
		if (!ClientTickHandler.inGame) {
			ClientTickHandler.inGame = true;
			this.miniMapLoaded = false;
			this.updateMiniMaps(true);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.InGame);
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.LOGIN, new PlayerEvent.LoginEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player)));
			LogWriter.debug("Client Player: Start game");
		}
		BuilderData bd = ItemBuilder.getBuilder(player.getHeldItemMainhand(), player);
		if (bd != null && ClientGuiEventHandler.result != null) {
            if (bd.getType() == 3 && !bd.schematicName.isEmpty()) {
                SchematicWrapper schema = SchematicController.Instance.getSchema(bd.schematicName + ".schematic");
                if (schema != null) {
                    Schematic sc = (Schematic) schema.schema;
                    ClientEventHandler.schemaPos = ClientGuiEventHandler.result.getBlockPos();
                    ClientEventHandler.rotation = MathHelper.floor(player.rotationYaw / 90.0f + 0.5f) & 0x3;
                    int x, y = 0, z = -1;
                    boolean has = sc.offset != null  && (sc.offset.getX() != 0 || sc.offset.getY() != 0 || sc.offset.getZ() != 0);
                    switch (ClientEventHandler.rotation) {
                        case 1: {
                            if (has) {
                                x = -1 * sc.offset.getZ() - sc.getLength();
                                y = sc.offset.getY();
                                z = sc.offset.getX() - 1;
                            } else {
                                x = -1 * sc.getLength();
                                z = (int) (-1 * Math.ceil((double) sc.getWidth() / 2.0d));
                            }
                            break;
                        }
                        case 2: {
                            if (has) {
                                x = -1 * sc.offset.getX() - sc.getWidth();
                                y = sc.offset.getY();
                                z = -1 * sc.offset.getZ() - sc.getLength();
                            } else {
                                x = (int) (-1 * Math.ceil((double) sc.getWidth() / 2.0d));
                                z = -1 * sc.getLength();
                            }
                            break;
                        }
                        case 3: {
                            if (has) {
                                x = sc.offset.getZ() - 1;
                                y = sc.offset.getY();
                                z = -1 * sc.offset.getX() - sc.getWidth();
                            } else {
                                x = -1;
                                z = (int) (-1 * Math.ceil((double) sc.getWidth() / 2.0d));
                            }
                            break;
                        }
                        default: {
                            if (has) {
                                x = sc.offset.getX() - 1;
                                y = sc.offset.getY();
                                z = sc.offset.getZ() - 1;
                            } else {
                                x = (int) (-1 * Math.ceil((double) sc.getWidth() / 2.0d));
                            }
                            break;
                        }
                    }
                    ClientEventHandler.schema = sc;
                    ClientEventHandler.schemaPos = ClientEventHandler.schemaPos.add(x, y, z);
                    this.drawSchematic(ClientEventHandler.schemaPos, schema, 0, ClientEventHandler.rotation);
                }
            }
        }
		if (player.world.getTotalWorldTime() % 100 == 0 && secs != System.currentTimeMillis() / 1000) {
			secs = System.currentTimeMillis() / 1000;
			this.updateMiniMaps(false);
		}
		if (TileBuilder.DrawPoses.isEmpty()) {
			CustomNpcs.debugData.endDebug("Client", player, "ClientEventHandler_onRenderTick");
			return;
		}
		for (BlockPos pos : TileBuilder.DrawPoses) {
			if (pos == null || pos.distanceSq(player.getPosition()) > 10000.0) {
				continue;
			}
			TileEntity te = player.world.getTileEntity(pos);
			if (!(te instanceof TileBuilder)) {
				continue;
			}
			this.drawSchematic(pos, ((TileBuilder) te).getSchematic(), ((TileBuilder) te).yOffset,
					((TileBuilder) te).rotation);
		}
		CustomNpcs.debugData.endDebug("Client", player, "ClientEventHandler_onRenderTick");
	}

	private void drawSchematic(BlockPos pos, SchematicWrapper schem, int yOffset, int rotation) {
		if (pos == null || schem == null) {
			return;
		}
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(pos.getX() - TileEntityRendererDispatcher.staticPlayerX,
				pos.getY() - TileEntityRendererDispatcher.staticPlayerY + 0.01,
				pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ);
		GlStateManager.translate(1.0f, yOffset, 1.0f);
		// Bound
		if (rotation % 2 == 0) {
			this.drawSelectionBox(
					new BlockPos(schem.schema.getWidth(), schem.schema.getHeight(), schem.schema.getLength()));
		} else {
			this.drawSelectionBox(
					new BlockPos(schem.schema.getLength(), schem.schema.getHeight(), schem.schema.getWidth()));
		}
		if (!ClientEventHandler.displayMap.containsKey(schem.schema)) {
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
						state = SchematicWrapper.rotationState(state, 0);
						try {
							this.renderBlock(state);
							if (GL11.glGetError() != 0) {
								break;
							}
						} catch (Exception e) {
							LogWriter.error("Error:", e);
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
				if (GL11.glGetError() != 0) {
					ClientEventHandler.displayMap.remove(schem.schema);
				}
			}
		}
		if (ClientEventHandler.displayMap.containsKey(schem.schema)) {
			GlStateManager.rotate(rotation * -90.0f, 0.0f, 1.0f, 0.0f);
			switch (rotation) {
			case 1: {
				GlStateManager.translate(0, 0, -1 * schem.schema.getLength());
				break;
			}
			case 2: {
				GlStateManager.translate(-1 * schem.schema.getWidth(), 0, -1 * schem.schema.getLength());
				break;
			}
			case 3: {
				GlStateManager.translate(-1 * schem.schema.getWidth(), 0, 0);
				break;
			}
			}
			GlStateManager.callList(ClientEventHandler.displayMap.get(schem.schema));
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
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

	private boolean isInRange(EntityPlayer player, double posX, double posY, double posZ) {
		double y = Math.abs(player.posY - posY);
		if (posY >= 0.0 && y > 16.0) {
			return false;
		}
		double x = Math.abs(player.posX - posX);
		double z = Math.abs(player.posZ - posZ);
		return x <= 16.0 && z <= 16.0;
	}

	@SubscribeEvent
	public void npcPlayerLoginEvent(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		if (!event.player.world.isRemote) {
			return;
		}
		ClientProxy.playerData.hud.clear();
	}

	private void renderBlock(IBlockState state) {
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		// dispatcher.renderBlockBrightness(state, 0.1f);
		switch (state.getRenderType()) {
		case MODEL:
			IBakedModel ibakedmodel = dispatcher.getModelForState(state);
			GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
			BlockModelRenderer bmr = BlockRendererDispatcherReflection.getBlockModelRenderer(dispatcher);
			BlockColors bc = BlockModelRendererReflection.getBlockColors(bmr);
			int color = bc.colorMultiplier(state, null, null, 0);
			if (EntityRenderer.anaglyphEnable) {
				color = TextureUtil.anaglyphColor(color);
			}
			float r = (float) (color >> 16 & 255) / 255.0F;
			float g = (float) (color >> 8 & 255) / 255.0F;
			float b = (float) (color & 255) / 255.0F;
			for (EnumFacing enumfacing : EnumFacing.values()) {
				this.renderModelBlockQuads(ibakedmodel.getQuads(state, enumfacing, 0L), r, g, b);
			}
			this.renderModelBlockQuads(ibakedmodel.getQuads(state, null, 0L), r, g, b);
			break;
		case ENTITYBLOCK_ANIMATED:
			ChestRenderer chestRenderer = BlockRendererDispatcherReflection.getChestRenderer(dispatcher);
			chestRenderer.renderChestBrightness(state.getBlock(), 1.0f);
            default:
			break;
		}
	}

	private void renderModelBlockQuads(List<BakedQuad> quads, float r, float g, float b) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		for (int j = quads.size(), i = 0; i < j; ++i) {
			BakedQuad bakedquad = quads.get(i);
			bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
			bufferbuilder.addVertexData(bakedquad.getVertexData());
			if (bakedquad.hasTintIndex()) {
				bufferbuilder.putColorRGB_F4(r, g, b);
			} else {
				bufferbuilder.putColorRGB_F4(1.0f, 1.0f, 1.0f);
			}
			Vec3i vec3i = bakedquad.getFace().getDirectionVec();
			bufferbuilder.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());

			tessellator.draw();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateMiniMaps(boolean update) {
		PlayerMiniMapData mm = CustomNpcs.proxy.getPlayerData(Minecraft.getMinecraft().player).minimap;
		// Found Mods:
		boolean hasJourneyMap = false;
		boolean hasXaeroMap = false;
		boolean hasVoxelMap = false;
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getModId().equals("journeymap")) {
				hasJourneyMap = true;
			}
			if (mod.getModId().equals("xaerominimap")) {
				hasXaeroMap = true;
			}
			if (mod.getModId().equals("voxelmap")) {
				hasVoxelMap = true;
			}
		}
		// If name is changed:
		if (!hasJourneyMap) {
			Class<?> jm = null;
			try {
				jm = Class.forName("journeymap.client.model.Waypoint");
				hasJourneyMap = true;
			} catch (Exception e) { /*LogWriter.debug("JourneyMap is missing: "+jm);*/ }
		}
		if (!hasXaeroMap) {
			Class<?> xm = null;
			try {
				xm = Class.forName("xaero.common.minimap.waypoints.Waypoint");
				hasXaeroMap = true;
			} catch (Exception e) { /*LogWriter.debug("XaeroMap is missing: "+xm);*/ }
		}
		if (!hasVoxelMap) {
			Class<?> vm = null;
			try {
				vm = Class.forName("com.mamiyaotaru.voxelmap.VoxelMap");
				hasVoxelMap = true;
			} catch (Exception e) { /*LogWriter.debug("VoxelMap is missing: "+vm);*/ }
		}
		// Check save client Points:
		List<MiniMapData> points = new ArrayList<>();
		if (hasJourneyMap) {
			mm.addData.clear();
			if (!mm.modName.equals("journeymap")) {
				mm.modName = "journeymap";
				update = true;
			}
			try {
				Class<?> ws = Class.forName("journeymap.client.waypoint.WaypointStore");
				miniMapLoaded = (boolean) ws.getDeclaredMethod("hasLoaded").invoke(ws.getEnumConstants()[0]);
				if (!miniMapLoaded) {
					CustomNPCsScheduler.runTack(() -> this.updateMiniMaps(true), 50);
					return;
				}
				Collection<Object> waypoints = (Collection<Object>) ws.getDeclaredMethod("getAll")
						.invoke(ws.getEnumConstants()[0]); // Collection<Object> waypoints =
															// WaypointStore.INSTANCE.getAll();
				for (Object waypoint : waypoints) {
					Class<?> wc = waypoint.getClass();
					MiniMapData mmd = new MiniMapData();
					mmd.name = (String) wc.getDeclaredMethod("getName").invoke(waypoint);
					mmd.type = wc.getDeclaredMethod("getType").invoke(waypoint).toString();
					mmd.icon = (String) wc.getDeclaredMethod("getIcon").invoke(waypoint);
					mmd.pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos((BlockPos) wc.getDeclaredMethod("getBlockPos").invoke(waypoint));
					mmd.color = new Color((int) wc.getDeclaredMethod("getR").invoke(waypoint),
							(int) wc.getDeclaredMethod("getG").invoke(waypoint),
							(int) wc.getDeclaredMethod("getB").invoke(waypoint)).getRGB();
					mmd.isEnable = (boolean) wc.getDeclaredMethod("isEnable").invoke(waypoint);
					Collection<Integer> dimensions = (Collection<Integer>) wc.getDeclaredMethod("getDimensions")
							.invoke(waypoint);
					mmd.dimIDs = new int[dimensions.size()];
					int i = 0;
					for (int dim : dimensions) {
						mmd.dimIDs[i] = dim;
						i++;
					}

					mmd.id = points.size();
					points.add(mmd);
					
					MiniMapData mmp = mm.get(mmd);
					if (mmp != null) { mmd.setQuest(mmp); } else { update = true; }
				}
			} catch (Exception e) { LogWriter.debug("JourneyMap tried to collect its points"); }
		}
		else if (hasXaeroMap) {
			if (!mm.modName.equals("xaerominimap")) {
				mm.modName = "xaerominimap";
				update = true;
			}
			try {
				Class<?> xms = Class.forName("xaero.common.XaeroMinimapSession");
				Object minimapSession = xms.getDeclaredMethod("getCurrentSession").invoke(xms); // XaeroMinimapSession
				if (minimapSession != null) {
					Field fl = xms.getDeclaredField("usable");
					fl.setAccessible(true);
					miniMapLoaded = fl.getBoolean(minimapSession);
				} else {
					miniMapLoaded = false;
				}
				if (!miniMapLoaded) {
					CustomNPCsScheduler.runTack(() -> this.updateMiniMaps(true), 50);
					return;
				}

				Object waypointsManager = xms.getDeclaredMethod("getWaypointsManager").invoke(minimapSession); // WaypointsManager

				Method getWaypointMap = waypointsManager.getClass().getDeclaredMethod("getWaypointMap");
				HashMap<String, Object> waypointMap = (HashMap<String, Object>) getWaypointMap.invoke(waypointsManager);

				String mainContainerID = (String) waypointsManager.getClass()
						.getDeclaredMethod("getAutoRootContainerID").invoke(waypointsManager);
				Object wwrc = waypointMap.get(mainContainerID);// WaypointWorldRootContainer
				if (wwrc == null) {
					if (!mm.points.isEmpty()) {
						mm.points.clear();
						update = true;
					}
				} else {
					mm.addData.clear();
					Gson gson = new Gson();
					Field subContainers = wwrc.getClass().getField("subContainers");
					Field worlds = wwrc.getClass().getField("worlds");
					HashMap<String, Object> dimMap = (HashMap<String, Object>) subContainers.get(wwrc);
					for (String k : dimMap.keySet()) {
						if (!mm.addData.containsKey("xaero_world_name")) {
							String world_name = (String) dimMap.get(k).getClass().getDeclaredMethod("getKey")
									.invoke(dimMap.get(k));
							while (world_name.lastIndexOf("/") != -1) {
								world_name = world_name.substring(0, world_name.lastIndexOf("/"));
							}
							mm.addData.put("xaero_world_name", world_name);
						}
						HashMap<String, Object> worldMap = (HashMap<String, Object>) worlds.get(dimMap.get(k));
						for (String k1 : worldMap.keySet()) {
							if (!k1.equals("waypoints")) {
								continue;
							}
							Object waypointWorld = worldMap.get(k1); // WaypointWorld
							int dimId = (int) waypointWorld.getClass().getDeclaredMethod("getDimId") .invoke(waypointWorld);
							HashMap<String, Object> sets = (HashMap<String, Object>) waypointWorld.getClass().getDeclaredMethod("getSets").invoke(waypointWorld);
							for (String ks : sets.keySet()) {
								Object waypointSet = sets.get(ks); // WaypointSet
								List<Object> list = (List<Object>) waypointSet.getClass().getDeclaredMethod("getList")
										.invoke(waypointSet);
								for (Object waypoint : list) {
									Class<?> wc = waypoint.getClass();
									MiniMapData mmd = new MiniMapData();
									mmd.name = (String) wc.getDeclaredMethod("getName").invoke(waypoint);
									mmd.type = wc.getDeclaredMethod("getWaypointType").invoke(waypoint).toString();
									mmd.icon = (String) wc.getDeclaredMethod("getSymbol").invoke(waypoint);
									int x = (int) wc.getDeclaredMethod("getX").invoke(waypoint);
									int y = (int) wc.getDeclaredMethod("getY").invoke(waypoint);
									int z = (int) wc.getDeclaredMethod("getZ").invoke(waypoint);
									mmd.pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(new BlockPos(x, y, z));
									mmd.color = (int) wc.getDeclaredMethod("getColor").invoke(waypoint);
									mmd.isEnable = !((boolean) wc.getDeclaredMethod("isDisabled").invoke(waypoint));
									mmd.dimIDs = new int[] { dimId };
									mmd.gsonData.put("temporary",
											gson.toJson(wc.getDeclaredMethod("isTemporary").invoke(waypoint)));

									mmd.id = points.size();
									points.add(mmd);
									
									MiniMapData mmp = mm.get(mmd);
									if (mmp != null) { mmd.setQuest(mmp); } else { update = true; }
								}
							}
						}
					}
				}
			} catch (Exception e) {LogWriter.debug("XaeroMap tried to collect its points");}
		}
		else if (hasVoxelMap) {
			mm.addData.clear();
			if (!mm.modName.equals("voxelmap")) {
				mm.modName = "voxelmap";
				update = true;
			}
			try {
				Class<?> vm = Class.forName("com.mamiyaotaru.voxelmap.VoxelMap");
				Object instance = vm.getMethod("getInstance").invoke(vm);
				Object waypointManager = vm.getMethod("getWaypointManager").invoke(instance);

				Field fl = waypointManager.getClass().getDeclaredField("loaded");
				fl.setAccessible(true);
				miniMapLoaded = fl.getBoolean(waypointManager);
				if (!miniMapLoaded) {
					CustomNPCsScheduler.runTack(() -> this.updateMiniMaps(true), 50);
					return;
				}
				List<Object> waypoints = (List<Object>) waypointManager.getClass().getMethod("getWaypoints").invoke(waypointManager);
				for (Object waypoint : waypoints) {
					Class<?> wc = waypoint.getClass();
					MiniMapData mmd = new MiniMapData();
					mmd.name = (String) wc.getDeclaredField("name").get(waypoint);
					mmd.gsonData.put("voxel_world_name", (String) wc.getDeclaredField("world").get(waypoint));
					mmd.icon = (String) wc.getDeclaredField("imageSuffix").get(waypoint);
					int x = (int) wc.getDeclaredField("x").get(waypoint);
					int y = (int) wc.getDeclaredField("y").get(waypoint);
					int z = (int) wc.getDeclaredField("z").get(waypoint);
					mmd.pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, y, z);
					mmd.color = new Color((float) wc.getDeclaredField("red").get(waypoint),
							(float) wc.getDeclaredField("green").get(waypoint),
							(float) wc.getDeclaredField("blue").get(waypoint)).getRGB();
					mmd.isEnable = (boolean) wc.getDeclaredField("enabled").get(waypoint);
					TreeSet<Integer> dimensions = (TreeSet<Integer>) wc.getDeclaredField("dimensions").get(waypoint);
					mmd.dimIDs = new int[dimensions.size()];
					int i = 0;
					for (int dim : dimensions) {
						mmd.dimIDs[i] = dim;
						i++;
					}
					mmd.id = points.size();
					points.add(mmd);
					
					MiniMapData mmp = mm.get(mmd);
					if (mmp != null) { mmd.setQuest(mmp); } else { update = true; }
				}
			} catch (Exception e) {LogWriter.debug("VoxelMap tried to collect its points");}
		} else {
			mm.addData.clear();
			if (!mm.modName.equals("non")) {
				mm.modName = "non";
				update = true;
			}
		}
		if (!update && points.size() != mm.points.size()) { update = true; }
		// Send
		if (update) {
			mm.points.clear();
			mm.points.addAll(points);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MiniMapData, mm.saveNBTData(new NBTTagCompound()));
		}
	}

	@SideOnly(Side.CLIENT)
	public static void entityClientEvent(EntityInteract event) {
		CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.NbtBook, event.getEntityPlayer());
		CustomNPCsScheduler.runTack(() -> {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen instanceof GuiNbtBook) {
				((GuiNbtBook) mc.currentScreen).entityId = event.getTarget().getEntityId();
				((GuiNbtBook) mc.currentScreen).entity = event.getTarget();
				NBTTagCompound compound = new NBTTagCompound();
				((GuiNbtBook) mc.currentScreen).originalCompound = event.getTarget().writeToNBT(compound);
				((GuiNbtBook) mc.currentScreen).compound = ((GuiNbtBook) mc.currentScreen).originalCompound;
				mc.currentScreen.initGui();
			}
		}, 250);
		
	}
	
}
