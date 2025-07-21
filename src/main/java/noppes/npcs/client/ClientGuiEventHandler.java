package noppes.npcs.client;

import java.awt.Point;
import java.util.*;

import net.minecraft.client.gui.*;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.reflection.pathfinding.PathReflection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.player.GuiMailmanWrite;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.client.util.CrashesData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemNpcMovingPath;
import noppes.npcs.particles.CustomParticle;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.ValueUtil;

@SideOnly(Side.CLIENT)
public class ClientGuiEventHandler extends Gui {

	private static final ResourceLocation[] BORDER;

	public static final ResourceLocation COIN_NPC = new ResourceLocation(CustomNpcs.MODID, "textures/items/coin_gold.png");
	public static final ResourceLocation RESOURCE_COMPASS = new ResourceLocation(CustomNpcs.MODID + ":models/util/compass.obj");
	public static final CrashesData crashes = new CrashesData();
	public static final List<double[]> movingPath = new ArrayList<>();
	public static RayTraceResult result;
	public static List<CustomParticle> customParticle = new ArrayList<>();
	public static boolean hasNewMail = false;
	public static long showNewMail = 0L, startMail = 0L;

	static {
		BORDER = new ResourceLocation[16];
		for (int i = 0; i < 16; i++) {
			BORDER[i] = new ResourceLocation(CustomNpcs.MODID, "textures/util/border/" + (i < 10 ? "0" + i : i) + ".png");
		}
	}
	private Minecraft mc;
	private ScaledResolution sw;
    private double dx, dy, dz;
	private int qt = 0;

	private final List<Entity> tempEntity = new ArrayList<>();

	private void drawAddSegment(Point[] pns, Point p1, double minY, double maxY) {
		if (pns == null || pns.length != 2 || p1 == null) {
			return;
		}
		Point p0 = pns[0], p2 = pns[1];
		if (p0 == null || p2 == null) {
			return;
		}
		float wallAlpha = 0.11f;
		// Walls
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p0.x + 0.5d, maxY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p0.x + 0.5d, maxY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();

		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p2.x + 0.5d, maxY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p2.x + 0.5d, minY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p2.x + 0.5d, minY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p2.x + 0.5d, maxY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, wallAlpha).endVertex();

		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		// Lines
		buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p2.x + 0.5d, minY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		if (maxY - minY > 1) {
			for (double i = 1.0d; i < maxY - minY; i++) {
				buffer.pos(p0.x + 0.5d, minY + i, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
				buffer.pos(p1.x + 0.5d, minY + i, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
				buffer.pos(p1.x + 0.5d, minY + i, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
				buffer.pos(p2.x + 0.5d, minY + i, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
			}
		}
		buffer.pos(p0.x + 0.5d, maxY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p2.x + 0.5d, maxY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p0.x + 0.5d, maxY, p0.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p2.x + 0.5d, minY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		buffer.pos(p2.x + 0.5d, maxY, p2.y + 0.5d).color((float) 0.75, (float) 0.75, (float) 0.75, (float) 1.0).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawNpc(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase)) {
			return;
		}
		EntityLivingBase entity = (EntityLivingBase) entityIn;
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) {
            NBTTagCompound compound = new NBTTagCompound();
			entity.writeToNBTOptional(compound);
			Entity e = EntityList.createEntityFromNBT(compound, entity.world);
            npc = (EntityNPCInterface) e;
			if (e != null) {
				e.setUniqueId(UUID.randomUUID());
				e.setEntityId(-1);
				if (npc.display.getVisible() == 1) {
					npc.display.setVisible(2);
				}
				npc.display.setShowName(0);
				entity = npc;
			}
        }
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();

		GlStateManager.translate(-12, 10, 50.0f);
		float scale = 1.0f;
		if (entity.height > 2.4) {
			scale = 2.0f / entity.height;
		}
		GlStateManager.scale(-30.0f * scale * (float) 0.75, 30.0f * scale * (float) 0.75, 30.0f * scale * (float) 0.75);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		RenderHelper.enableStandardItemLighting();
		float f2 = entity.renderYawOffset;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity.rotationYawHead;
		float f6 = 0.0f;
		float f7 = 0.0f;
		int orientation = 0;
		if (npc != null) {
			orientation = npc.ais.orientation;
			npc.ais.orientation = 0;
		}
		GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
		entity.renderYawOffset = 0;
		entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + 0);
		entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		entity.rotationYawHead = entity.rotationYaw;
		mc.getRenderManager().playerViewY = 180.0f;
        mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
        entity.renderYawOffset = f2;
		entity.prevRenderYawOffset = f2;
        entity.rotationYaw = f3;
		entity.prevRotationYaw = f3;
        entity.rotationPitch = f4;
		entity.prevRotationPitch = f4;
        entity.rotationYawHead = f5;
		entity.prevRotationYawHead = f5;
		if (npc != null) {
			npc.ais.orientation = orientation;
		}
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
	}

	private void drawNpcMovingPath(EntityCustomNpc npc) {
		NoppesUtilPlayer.sendDataCheckDelay(EnumPlayerPacket.MovingPathGet, npc, 5000, npc.getEntityId());
		List<int[]> list = npc.ais.getMovingPath();
		if (list.isEmpty()) {
			ClientGuiEventHandler.movingPath.clear();
			return;
		}
		boolean type = npc.ais.getMovingPathType() == 0;
		// create path
		if (npc.ais.getMovingType() == 2 && (ClientGuiEventHandler.movingPath.isEmpty() || mc.world.getTotalWorldTime() % 100L == 0L)) {
			NBTTagCompound npcNbt = new NBTTagCompound();
			npc.writeToNBTAtomically(npcNbt);
			Entity entity = EntityList.createEntityFromNBT(npcNbt, mc.world);
			if (entity != null) {
				entity.setUniqueId(UUID.randomUUID());
				if (entity instanceof EntityLiving) {
					ClientGuiEventHandler.movingPath.clear();
					EntityCustomNpc newNpc = (EntityCustomNpc) entity;
					int[] pos = list.get(0);
					double yo = 0.0d;
					IBlockState state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
					if (state.isFullBlock() || state.isFullCube()) {
						yo = 1.0d;
					}
					newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
					ClientGuiEventHandler.movingPath.add(new double[]{pos[0] + 0.5d, pos[1] + yo + 0.4d, pos[2] + 0.5d});
					newNpc.display.setVisible(1);
					newNpc.display.setSize(1);
					newNpc.display.setShowName(1);
					mc.world.spawnEntity(newNpc);
					PathNavigate nv = newNpc.getNavigator();
					for (int i = 1; i < list.size(); i++) {
						pos = list.get(i);
						nv.clearPath();
						newNpc.motionX = 0.0d;
						newNpc.motionY = 0.0d;
						newNpc.motionZ = 0.0d;
						Path path = nv.getPathToXYZ(pos[0], pos[1], pos[2]);
						if (path == null) {
							ClientGuiEventHandler.movingPath.add(new double[0]);
							yo = 0.0d;
							state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
							if (state.isFullBlock() || state.isFullCube()) {
								yo = 1.0d;
							}
							newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
							continue;
						}
						for (int p = 0; p < path.getCurrentPathLength(); p++) {
							PathPoint pp = path.getPathPointFromIndex(p);
							ClientGuiEventHandler.movingPath.add(new double[]{pp.x + 0.5d, pp.y + 0.4d, pp.z + 0.5d});
						}
						yo = 0.0d;
						state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
						if (state.isFullBlock() || state.isFullCube()) {
							yo = 1.0d;
						}
						newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
					}
					if (type) {
						nv.clearPath();
						pos = list.get(list.size() - 1);
						yo = 0.0d;
						state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
						if (state.isFullBlock() || state.isFullCube()) {
							yo = 1.0d;
						}
						newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
						newNpc.motionX = 0.0d;
						newNpc.motionY = 0.0d;
						newNpc.motionZ = 0.0d;
						pos = list.get(0);
						Path path = nv.getPathToXYZ(pos[0], pos[1], pos[2]);
						if (path != null) {
							for (int p = 0; p < path.getCurrentPathLength(); p++) {
								PathPoint pp = path.getPathPointFromIndex(p);
								ClientGuiEventHandler.movingPath
										.add(new double[]{pp.x + 0.5d, pp.y + 0.4d, pp.z + 0.5d});
							}
						}
					}
				}
				entity.isDead = true;
				mc.world.removeEntity(entity);
				tempEntity.add(entity);
			}
		}

		double[] pre;
		float r = 0.75f, g = 0.75f, b = 0.75f, ag = 15.0f;

		// HitBox
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(1);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(npc.posX - dx, npc.posY - dy, npc.posZ - dz);
		double w = npc.width / 2;
		RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(w * -1.0d, 0.0d, w * -1.0d, w, npc.height, w)), r, g, b, 1.0f);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		// Eyes + Head rotation
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		IRayTraceVec pHh = Util.instance.getPosition(npc.posX, npc.posY + npc.getEyeHeight(), npc.posZ, npc.rotationYawHead, 0.0d, npc.width / 2.0d);
		IRayTraceVec pEr = Util.instance.getPosition(pHh.getX(), pHh.getY(), pHh.getZ(), npc.rotationYawHead, npc.rotationPitch * -1.0d, 0.7d / 5.0d * npc.display.getSize());
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		r = 0.25f;
		g = 0.5f;
        buffer.pos(pHh.getX(), pHh.getY(), pHh.getZ()).color(r, g, b, 1.0f).endVertex();
		buffer.pos(pEr.getX(), pEr.getY(), pEr.getZ()).color(r, g, b, 1.0f).endVertex();
		if (npc.ais.directLOS) {
			IRayTraceVec mr = Util.instance.getPosition(pHh.getX(), pHh.getY(), pHh.getZ(), npc.rotationYawHead + 60.0d, 0.0d, 0.7d / 5.0d * npc.display.getSize());
			IRayTraceVec nr = Util.instance.getPosition(pHh.getX(), pHh.getY(), pHh.getZ(), npc.rotationYawHead - 60.0d, 0.0d, 0.7d / 5.0d * npc.display.getSize());
			IRayTraceVec mp = Util.instance.getPosition(pHh.getX(), pHh.getY(), pHh.getZ(), npc.rotationYaw, ValueUtil.correctDouble(npc.rotationPitch * -1.0d + 60.0d, -90.0d, 90.0d), 1.4d / 5.0d * npc.display.getSize());
			IRayTraceVec np = Util.instance.getPosition(pHh.getX(), pHh.getY(), pHh.getZ(), npc.rotationYaw, ValueUtil.correctDouble(npc.rotationPitch * -1.0d - 60.0d, -90.0d, 90.0d), 1.4d / 5.0d * npc.display.getSize());
			r = 0.525f;
			g = 0.725f;
			b = 0.125f;
			buffer.pos(pHh.getX(), pHh.getY(), pHh.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), mp.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(pHh.getX(), pHh.getY(), pHh.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), np.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), mp.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), np.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(pHh.getX(), pHh.getY(), pHh.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), mp.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(pHh.getX(), pHh.getY(), pHh.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), np.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), mp.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), np.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), mp.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), mp.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(mr.getX(), np.getY(), mr.getZ()).color(r, g, b, 1.0f).endVertex();
			buffer.pos(nr.getX(), np.getY(), nr.getZ()).color(r, g, b, 1.0f).endVertex();
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		// Target
		if (npc.getAttackTarget() != null) {
			EntityLivingBase target = npc.getAttackTarget();
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-dx, -dy, -dz);
			tessellator = Tessellator.getInstance();
			buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

			r = 0.8f;
			g = 0.0f;
			b = 0.8f;
			buffer.pos(target.posX, target.posY + target.getEyeHeight(), target.posZ).color(r, g, b, 1.0f).endVertex();
			buffer.pos(npc.posX, npc.posY + npc.getEyeHeight(), npc.posZ).color(r, g, b, 1.0f).endVertex();

			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		// Now way
		Path path = npc.getNavigator().getPath();
		if (path != null) {
			PathPoint[] points = PathReflection.getPoints(path);
			if (points != null) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0f);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(-dx, -dy, -dz);
				tessellator = Tessellator.getInstance();
				buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

				r = 0.156862f;
				g = 0.705882f;
				b = 0.352941f;
				pre = new double[]{npc.posX, npc.posY + (double) npc.getEyeHeight(), npc.posZ};
				int currentPath = points.length - 1;
				double md = -1.0d;
				for (int i = 0; i < points.length; i++) {
					double d = npc.getDistance((double) points[i].x + 0.5d,
							(double) points[i].y + (double) npc.getEyeHeight() / 2.0d, (double) points[i].z + 0.5d);
					if (md == -1.0d || d <= md) {
						md = d;
						currentPath = i;
					}
				}
				for (int i = currentPath; i < points.length; i++) {
					double[] pos = new double[]{(double) points[i].x + 0.5d, (double) points[i].y + (double) npc.getEyeHeight() / 2.0d, (double) points[i].z + 0.5d};
                    double[] newPre = new double[]{pos[0], pos[1], pos[2]};
                    buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
                    buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
                    pre = newPre;
				}
				tessellator.draw();
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}

		// Can Way
		if (ClientGuiEventHandler.movingPath.size() > 1) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-dx, -dy, -dz);
			tessellator = Tessellator.getInstance();
			buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			r = 0.8f;
			g = 0.8f;
			b = 0.8f;
			pre = null;
			for (int i = 0; i < ClientGuiEventHandler.movingPath.size(); i++) {
				double[] pos = ClientGuiEventHandler.movingPath.get(i);
				if (pos.length == 0) {
					pre = null;
					continue;
				}
				double[] newPre = new double[] { pos[0], pos[1], pos[2] };
				if (pre != null) {
					buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
					buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
				}
				pre = newPre;
			}
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		// Way
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		tessellator = Tessellator.getInstance();
		buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		r = 1.0f;
		g = 0.0f;
		b = 0.0f;
		pre = null;
		for (int i = 0; i < list.size(); i++) {
			int[] pos = list.get(i);
			double yo = 0.0d;
			IBlockState state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
			if (state.isFullBlock() || state.isFullCube()) {
				yo = 1.0d;
			}
			double[] newPre = new double[] { pos[0] + 0.5d, pos[1] + 0.5d + yo, pos[2] + 0.5d };
			if (pre != null) {
				buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
				buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
                IRayTraceRotate d = Util.instance.getAngles3D(pre[0], pre[1], pre[2], newPre[0], newPre[1], newPre[2]);
                for (int h = 0; h < 4; h++) {
                    IRayTraceVec p = Util.instance.getPosition(newPre[0], newPre[1], newPre[2], 360.0d - d.getYaw() + (h == 0 ? ag : h == 1 ? -1.0d * ag : 0.0d), 0.0 - d.getPitch() + (h == 2 ? ag : h == 3 ? -1.0d * ag : 0.0d), 0.5d);
                    buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
                    buffer.pos(p.getX(), p.getY(), p.getZ()).color(r, g, b, 1.0f).endVertex();
                }
                if (!type) {
                    d = Util.instance.getAngles3D(newPre[0], newPre[1], newPre[2], pre[0], pre[1], pre[2]);
                    for (int h = 0; h < 4; h++) {
						IRayTraceVec p = Util.instance.getPosition(pre[0], pre[1], pre[2], 360.0d - d.getYaw() + (h == 0 ? ag : h == 1 ? -1.0d * ag : 0.0d), 0.0 - d.getPitch() + (h == 2 ? ag : h == 3 ? -1.0d * ag : 0.0d), 0.5d);
                        buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
                        buffer.pos(p.getX(), p.getY(), p.getZ()).color(0.0f, 0.0f, 1.0f, 1.0f).endVertex();
                    }
                }
            }
			pre = newPre;
			if (type && i == list.size() - 1 && list.size() > 1) {
				pos = list.get(0);
				newPre = new double[] { pos[0] + 0.5d, pos[1] + 0.5d + yo, pos[2] + 0.5d };
				buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
				buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
				IRayTraceRotate d = Util.instance.getAngles3D(pre[0], pre[1], pre[2], newPre[0], newPre[1], newPre[2]);
				for (int h = 0; h < 4; h++) {
					IRayTraceVec p = Util.instance.getPosition(newPre[0], newPre[1], newPre[2], 360.0d - d.getYaw() + (h == 0 ? ag : h == 1 ? -1.0d * ag : 0.0d), 0.0 - d.getPitch() + (h == 2 ? ag : h == 3 ? -1.0d * ag : 0.0d), 0.5d);
					buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
					buffer.pos(p.getX(), p.getY(), p.getZ()).color(r, g, b, 1.0f).endVertex();
				}
			}
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		// Block Poses
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				r = 0.8f;
				g = 0.8f;
				b = 0.8f;
			} else {
                b = 0.0f;
			}
			int[] pos = list.get(i);
			double yo = 0.0d;
			IBlockState state = mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
			if (state.isFullBlock() || state.isFullCube()) {
				yo = 1.0d;
			}
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(i == 0 ? 3.0f : 2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos[0] - dx + 0.5d, pos[1] - dy + 0.5d + yo, pos[2] - dz + 0.5d);
			double m = i == 0 ? -0.125d : -0.075d;
			double n = i == 0 ? 0.125d : 0.075d;
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(m, m, m, n, n, n)), r, g, b, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	private void drawRegion(Zone3D reg, int editID) {
		if (reg == null || reg.size() == 0) {
			return;
		}
		float red = (float) (reg.color >> 16 & 255) / 255.0f;
		float green = (float) (reg.color >> 8 & 255) / 255.0f;
		float blue = (float) (reg.color & 255) / 255.0f;

		// polygon texture size
		int xm = reg.getMinX(), xs = reg.getMaxX() - reg.getMinX();
		int zm = reg.getMinZ(), zs = reg.getMaxZ() - zm;
		double size = (double) (Math.max(xs, zs)) / 4.0D;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		if (reg.size() > 1) {
			// Walls
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.depthMask(false);
			GlStateManager.translate(-dx, -dy, -dz);

			// textured
			GlStateManager.color(red, green, blue, 1.0f);
			mc.getTextureManager().bindTexture(BORDER[(int) (mc.world.getTotalWorldTime() % 16L)]);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); // textured
			double startU = 0.0d;
			double distH = reg.getHeight() + 1.0d;
			for (int pos : reg.points.keySet()) {
				Point p0 = reg.points.get(pos);
				boolean isEnd = pos == reg.points.size() - 1;
				Point p1 = isEnd ? reg.points.get(0) : reg.points.get(pos + 1);

				// seamless texture connection between walls
				double distW = p0.distance(p1);
				if (isEnd) { distW = Math.round(distW + startU); }

				// textured
				buffer.pos(p0.x + 0.5d, reg.y[1] + 1.0d, p0.y + 0.5d).tex(startU, distH).endVertex();
				buffer.pos(p1.x + 0.5d, reg.y[1] + 1.0d, p1.y + 0.5d).tex(startU + distW, distH).endVertex();
				buffer.pos(p1.x + 0.5d, reg.y[0], p1.y + 0.5d).tex(startU + distW, 0.0D).endVertex();
				buffer.pos(p0.x + 0.5d, reg.y[0], p0.y + 0.5d).tex(0.0D, 0.0D).endVertex();

				buffer.pos(p0.x + 0.5d, reg.y[0], p0.y + 0.5d).tex(startU, 0.0D).endVertex();
				buffer.pos(p1.x + 0.5d, reg.y[0], p1.y + 0.5d).tex(startU + distW, 0.0D).endVertex();
				buffer.pos(p1.x + 0.5d, reg.y[1] + 1.0d, p1.y + 0.5d).tex(startU + distW, distH).endVertex();
				buffer.pos(p0.x + 0.5d, reg.y[1] + 1.0d, p0.y + 0.5d).tex(startU, distH).endVertex();

				startU += distW % 1.0d;
			}
			GlStateManager.scale(1.0F, 1.0F, 1.0F);
			tessellator.draw();

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();

			// Lines
			buffer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-dx, -dy, -dz);
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			double x = 0.0d, z = 0.0d;
			for (Point v : reg.points.values()) {
				x += v.x;
				z += v.y;
			}
			if (!reg.points.isEmpty()) {
				x /= reg.points.size();
				z /= reg.points.size();
			}
			x += 0.5d;
			z += 0.5d;
			float alpha = 0.5f;
			for (int pos : reg.points.keySet()) {
				Point p0 = reg.points.get(pos);
				int minY = reg.y[0], maxY = reg.y[1];
				Point p1 = reg.points.get(0);
				if (reg.points.containsKey(pos + 1)) {
					p1 = reg.points.get(pos + 1);
				}

				buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color(red, green, blue, alpha).endVertex();
				buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color(red, green, blue, alpha).endVertex();
				buffer.pos(p0.x + 0.5d, maxY + 1.0D, p0.y + 0.5d).color(red, green, blue, alpha).endVertex();
				buffer.pos(p1.x + 0.5d, maxY + 1.0D, p1.y + 0.5d).color(red, green, blue, alpha).endVertex();

				if (reg.getId() == editID) {
					buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color(red, green, blue, alpha).endVertex();
					buffer.pos(x, minY, z).color(red, green, blue, alpha).endVertex();
					buffer.pos(p0.x + 0.5d, 1.0d + maxY, p0.y + 0.5d).color(red, green, blue, alpha).endVertex();
					buffer.pos(x, 1.0d + maxY, z).color(red, green, blue, alpha).endVertex();

					if (maxY - minY > 1) {
						for (int i = 1; i <= maxY - minY; i++) {
							buffer.pos(p0.x + 0.5d, minY + (double) i, p0.y + 0.5d).color(red, green, blue, alpha)
									.endVertex();
							buffer.pos(p1.x + 0.5d, minY + (double) i, p1.y + 0.5d).color(red, green, blue, alpha)
									.endVertex();
						}
					}
				}
			}
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		if (reg.size() > 2) { // Polygons up and down
			for (int i = 0; i < 2; i++) {
				double y = i == 0 ? (double) reg.y[1] + 0.98d : (double) reg.y[0] + 0.02;
				buffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				GlStateManager.depthMask(false);
				GlStateManager.translate(-dx, -dy, -dz);

				// simple colored
				// GlStateManager.disableTexture2D();
				// buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

				// textured
				GlStateManager.color(red, green, blue, 1.0f);
				mc.getTextureManager().bindTexture(BORDER[(int) (mc.world.getTotalWorldTime() % 16L)]);
				buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

				for (Point p : reg.points.values()) {
					// simple colored
					// buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue,
					// wallAlpha).endVertex();

					// textured
					double texU = 2.0d * size * (double) (p.x - xm) / (double) xs;
					double texV = 2.0d * size * (double) (p.y - zm) / (double) zs;
					buffer.pos(p.x + 0.5d, y, p.y + 0.5d).tex(texU, texV).endVertex();
				}
				for (int pos = reg.points.size() - 1; pos >= 0; pos--) {
					Point p = reg.points.get(pos);
					// simple colored
					// buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue,
					// wallAlpha).endVertex();

					// textured
					double texU = 2.0d * size * (double) (p.x - xm) / (double) xs;
					double texV = 2.0d * size * (double) (p.y - zm) / (double) zs;
					buffer.pos(p.x + 0.5d, y, p.y + 0.5d).tex(texU, texV).endVertex();
				}
				tessellator.draw();
				GlStateManager.depthMask(true);
				// GlStateManager.enableTexture2D(); // simple colored
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}

		if (reg.getId() == editID) {
			for (int i = 0; i < reg.points.size(); i++) {
				Point p0 = reg.points.get(i);
				Point p1 = reg.points.get(i >= reg.points.size() - 1 ? 0 : (i + 1));
				// vertex as * down and up
				if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					drawVertex((double) p0.x + 0.5d, reg.y[0], (double) p0.y + 0.5d, red, green, blue);
					drawVertex((double) p0.x + 0.5d, (double) reg.y[1] + 1.0d, (double) p0.y + 0.5d, red, green, blue);
				}
				// Bound
				drawSegment(p0, p1, reg.y[0], (double) reg.y[1] + 1.0d, red, green, blue);
			}
		}
	}

	private void drawSegment(Point p0, Point p1, double minY, double maxY, float red, float green, float blue) {
		if (p0 == null || p1 == null) {
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth((float) 2.0);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(p0.x + 0.5d, minY, p0.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex(); // _
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex();
		buffer.pos(p0.x + 0.5d, maxY, p0.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex(); // -
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex();
		buffer.pos(p1.x + 0.5d, minY, p1.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex(); // |
		buffer.pos(p1.x + 0.5d, maxY, p1.y + 0.5d).color(red, green, blue, (float) 0.5).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawVertex(double x, double y, double z, float red, float green, float blue) {
		double sizeS = 0.15D;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-dx, -dy, -dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x - sizeS, y - sizeS, z - sizeS).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x + sizeS, y + sizeS, z + sizeS).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x - sizeS, y - sizeS, z + sizeS).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x + sizeS, y + sizeS, z - sizeS).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x + sizeS, y - sizeS, z + sizeS).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x - sizeS, y + sizeS, z - sizeS).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x + sizeS, y - sizeS, z - sizeS).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x - sizeS, y + sizeS, z + sizeS).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x - sizeS, y, z).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x + sizeS, y, z).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x, y - sizeS, z).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x, y + sizeS, z).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(x, y, z - sizeS).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x, y, z + sizeS).color(red, green, blue, 1.0f).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawZone(BuilderData builder, BlockPos pos) {
		if (builder.getType() < 3) {
			int[] s = new int[] { 0, 0, 0 };
			int[] e = new int[] { 1, 1, 1 };
			float r = 1.0f, g = 0.0f, b = 0.0f;
            int[] m = builder.getDirections(mc.player);
            for (int j = 0; j < 3; j++) {
                s[j] = m[j];
                e[j] = m[j + 3];
            }
            if (builder.getType() == 1) {
                r = 0.0f;
                g = 1.0f;
                b = 1.0f;
            } else if (builder.getType() == 2) {
                g = 0.0f;
                b = 1.0f;
            }
            GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX() + s[0] - dx, pos.getY() + s[1] - dy,
					pos.getZ() + s[2] - dz);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, e[0], e[1], e[2])), r, g, b, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX() - dx + 0.5d, pos.getY() - dy, pos.getZ() - dz + 0.5d);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d, 0.0d, -0.5d, 0.5d, 1.0d, 0.5d)), 1.0f, 1.0f,
					1.0f, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		} else if (builder.getType() == 4) {
			if (!builder.schMap.containsKey(0)) {
				return;
			}
			pos = builder.schMap.get(0);
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1, 1, 1)), 1, 1, 1, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
			if (builder.schMap.containsKey(1)) {
				pos = builder.schMap.get(1);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1, 1, 1)), 0, 1, 0, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			if (builder.schMap.containsKey(2)) {
				pos = builder.schMap.get(2);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1, 1, 1)), 0, 0, 1, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			if (builder.schMap.containsKey(1) && builder.schMap.containsKey(2)) {
				AxisAlignedBB aabb = new AxisAlignedBB(builder.schMap.get(1), builder.schMap.get(2));
				pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, aabb.maxX - aabb.minX + 1,
						aabb.maxY - aabb.minY + 1, aabb.maxZ - aabb.minZ + 1)), 1, 0, 0, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	private int[] getOffset(int type) {
		int[] offsets = new int[] { 0, 0 };
		switch (type) {
			case 1: { // left down
				offsets[1] = (int) sw.getScaledHeight_double();
				break;
			}
			case 2: { // right up
				offsets[0] = (int) sw.getScaledWidth_double();
				break;
			}
			case 3: { // right down
				offsets[0] = (int) sw.getScaledWidth_double();
				offsets[1] = (int) sw.getScaledHeight_double();
				break;
			}
			default: { // left up
			}
		}
		return offsets;
	}

	@SubscribeEvent
	public void npcCameraSetupEvent(EntityViewRenderEvent.CameraSetup event) {
		if (event.getEntity() instanceof EntityLivingBase && ClientGuiEventHandler.crashes.isActive) { // camera shaking
			CustomNpcs.debugData.start("Players", this, "npcCameraSetupEvent");
			float amplitude = ClientGuiEventHandler.crashes
					.get(ClientGuiEventHandler.crashes.endTime - event.getEntity().world.getTotalWorldTime());
			if (amplitude != 0.0f) {
				switch (ClientGuiEventHandler.crashes.type) {
				case 0: { // vertical only
					event.setPitch(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getPitch());
					break;
				}
				case 1: { // horizontal only
					event.setYaw(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getYaw());
					break;
				}
				case 2: { // arc only
					event.setRoll(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude);
					break;
				}
				case 3: { // vertical and horizontal
					event.setPitch(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getPitch());
					event.setYaw(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getYaw());
					break;
				}
				case 4: { // vertical and arc
					event.setRoll(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude);
					event.setPitch(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getPitch());
					break;
				}
				default: { // all
					event.setRoll(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude * -1.0f);
					event.setPitch(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getPitch());
					event.setYaw(Minecraft.getMinecraft().getRenderPartialTicks() * amplitude + event.getYaw());
					break;
				}
				}
			}
			CustomNpcs.debugData.end("Players", this, "npcCameraSetupEvent");
		}
	}

	/** HUD Bar Interface */
	@SuppressWarnings("all")
	@SubscribeEvent
	public void npcRenderOverlay(RenderGameOverlayEvent.Text event) {
		CustomNpcs.debugData.start("Players", this, "npcRenderOverlay");
		mc = Minecraft.getMinecraft();
		sw = new ScaledResolution(mc);

		if (!tempEntity.isEmpty()) {
			for (Entity entity : tempEntity) {
				entity.world.removeEntity(entity);
				entity.world.removeEntityDangerously(entity);
				Chunk chunk = entity.world.getChunkFromChunkCoords(entity.chunkCoordX,
						entity.chunkCoordZ);
				if (entity.addedToChunk && chunk.isLoaded()) {
					chunk.removeEntity(entity);
					entity.addedToChunk = false;
				}
			}
			tempEntity.clear();
		}
		PlayerOverlayHUD hud = ClientProxy.playerData.hud;

		boolean isMoved = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) ||
				Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) ||
				Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()) ||
				Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
		if (hud.isMoved != isMoved) {
			hud.isMoved = isMoved;
			NoppesUtilPlayer.sendData(EnumPlayerPacket.IsMoved, isMoved);
		}

		if ((hasNewMail || startMail > 0L) && CustomNpcs.MailWindow != -1) { // Mail
			CustomNpcs.MailWindow = 1;
			int[] offsets = new int[2];
			float sr = -45.0f, su = 12.0f, sv = -32.0f; // sr = 45.0f, su = 12.0f, sv = 32.0f;
			offsets[1] = (int) hud.getWindowSize()[1] - 32;
            GlStateManager.pushMatrix();
			GlStateManager.translate(offsets[0] + 16, offsets[1] + 16, 0);
			if (startMail == 0L) {
				startMail = System.currentTimeMillis();
			}
			long time = System.currentTimeMillis() - startMail;
			// animation
			if (showNewMail == 0L || (time - showNewMail > -500L && time - showNewMail < 0L)) { // start
				if (showNewMail == 0L) {
					showNewMail = time + 500L;
				}
				time -= showNewMail;
				GlStateManager.rotate(sr * (float) time / 500.0f, 0.0f, 0.0f, 1.0f);
				GlStateManager.translate(su * (float) time / 500.0f, sv * (float) time / 500.0f, 0);
				if (time >= 0L) {
					startMail = 0L;
				}
			}
			if (!hasNewMail) { // end
				if (time > 0L) {
					startMail = System.currentTimeMillis() + 500L;
					time = System.currentTimeMillis() - startMail;
				}
				time += 500L;
				time *= -1L;
				GlStateManager.rotate(sr * (float) time / 500.0f, 0.0f, 0.0f, 1.0f);
				GlStateManager.translate(su * (float) time / 500.0f, sv * (float) time / 500.0f, 0);
				if (time < -480L) {
					startMail = 0L;
				}
			} else if (time % 31500 < 1750) { // living
				time = time % 1750;
				if (time < 500) {
					GlStateManager.rotate(30.0f * (float) time / 500.0f, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.0f * (float) time / 500.0f, -5.0f * (float) time / 500.0f, 0);
				} else if (time < 1250) {
					GlStateManager.rotate(30.0f - 420.0f * (float) (time -= 500L) / 750.0f, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.0f + (float) time / 750.0f, -5.0f + 5.0f * (float) time / 750.0f,
							0);
				} else {
					GlStateManager.rotate(-30.0f + 30.0f * (float) (time - 1250L) / 500.0f, 0.0f, 0.0f, 1.0f);
				}
			}
			time = System.currentTimeMillis() % 3000;
			if (time < 1500) {
				GlStateManager.color(0.85f, 0.85f, 0.85f, 0.5f + 0.45f * (float) time / 1500.f);
			} else {
				GlStateManager.color(0.85f, 0.85f, 0.85f, 0.5f + 0.45f * (3000.0f - (float) time) / 1500.f);
			}
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(GuiMailmanWrite.icons);
			drawTexturedModalRect(-16, -16, 0, 0, 32, 32);
			GlStateManager.popMatrix();
		}
		if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiLog)) {
			CustomNpcs.debugData.end("Players", this, "npcRenderOverlay");
			return;
		}
		// Custom HUD window
		TreeMap<Integer, TreeMap<Integer, IGuiComponent>> mapC = hud.getGuiComponents();
		if (!mapC.isEmpty()) {
			GuiCustom gui = new GuiCustom(new ContainerCustomGui(Minecraft.getMinecraft().player.inventory));
			GlStateManager.pushMatrix();
			for (int type : mapC.keySet()) {
				for (IGuiComponent component : mapC.get(type).values()) {
					component.offSet(type, hud.getWindowSize());
					component.setParent(gui);
					component.onRender(mc, -1, -1, 0, 0);
				}
			}
			GlStateManager.popMatrix();
		}
		// Custom HUD slots
		TreeMap<Integer, TreeMap<Integer, IItemSlot>> mapS = hud.getGuiSlots();
		for (int type : mapS.keySet()) {
			int[] os = getOffset(type);
			for (int id : mapS.get(type).keySet()) {
				IItemSlot slot = mapS.get(type).get(id);
				GlStateManager.pushMatrix();
				int x = os[0] == 0 ? slot.getPosX() : os[0] - slot.getPosX() - 18;
				int y = os[1] == 0 ? slot.getPosY() : os[1] - slot.getPosY() - 18;
				GlStateManager.translate(x, y, id);
				mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				drawTexturedModalRect(0, 0, 0, 0, 18, 18);
				if (!slot.getStack().isEmpty()) {
					ItemStack stack = slot.getStack().getMCItemStack();
					GlStateManager.translate(1, 1, 0);
					RenderHelper.enableStandardItemLighting();
					mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					drawString(mc.fontRenderer, "" + stack.getCount(), 12 - (stack.getCount() > 9 ? 9 : 0), 9, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
				}
				GlStateManager.popMatrix();
			}
		}
		// Quest Compass
		if (CustomNpcs.ShowQuestCompass) {
			String name = "", title = "";
			double[] p = null;
			int type = 0, range = 5;
			String n = "";
			if (hud.compassData.show) {
				p = new double[] { hud.compassData.pos.getX() - 0.5d, hud.compassData.pos.getY() + 0.5d,
						hud.compassData.pos.getZ() + 0.5d };
				name = hud.compassData.name;
				title = hud.compassData.title;
				type = hud.compassData.getType();
				if (mc.world.provider.getDimension() != hud.compassData.getDimensionID()) {
					type = 7;
				}
				range = hud.compassData.getRange();
				if (hud.compassData.getNPCName().isEmpty()) {
					n = new TextComponentTranslation("entity." + hud.compassData.getNPCName() + ".name")
							.getFormattedText();
					n = n.substring(0, n.length() - 2);
					if (n.equals("entity." + hud.compassData.getNPCName() + ".name")) {
						n = hud.compassData.getNPCName();
					}
				}
			} else {
				if (!ClientProxy.playerData.questData.activeQuests.containsKey(hud.questID) || hud.questID <= 0) {
					for (int id : ClientProxy.playerData.questData.activeQuests.keySet()) {
						if (ClientProxy.playerData.questData.activeQuests.get(id).quest.hasCompassSettings()
								&& id != hud.questID && id > 0) {
							hud.questID = id;
							break;
						}
					}
				}
				QuestData qData = ClientProxy.playerData.questData.activeQuests.get(hud.questID);
				if (qData != null) {
					double minD = Double.MAX_VALUE;
					QuestObjective select = null;
					for (QuestObjective io : qData.quest.questInterface.getObjectives(mc.player)) {
						if (io.isCompleted()) {
							continue;
						}
						if (qData.quest.step != 1) {
							if (io.rangeCompass == 0 && select == null) {
								select = io;
							} else if (io.rangeCompass != 0) {
								double d = Util.instance.distanceTo(io.pos.getX() + 0.5d, io.pos.getY(), io.pos.getZ() + 0.5d, mc.player.posX, mc.player.posY + mc.player.eyeHeight, mc.player.posZ);
								if (d <= minD) {
									minD = d;
									select = io;
								}
							}
							continue;
						}
						select = io;
						break;
					}
					if (select != null) {
						name = qData.quest.getTitle();
						type = select.getType();
						if (!select.getOrientationEntityName().isEmpty()) {
							n = new TextComponentTranslation("entity." + select.getOrientationEntityName() + ".name")
									.getFormattedText();
							n = n.substring(0, n.length() - 2);
							if (n.equals("entity." + select.getOrientationEntityName() + ".name")) {
								n = select.getOrientationEntityName();
							}
						}
						if (mc.world.provider.getDimension() != select.dimensionID) {
							type = 7;
						}
						if (type != EnumQuestTask.KILL.ordinal() && type != EnumQuestTask.AREAKILL.ordinal()) {
							range = 1;
						}
						if (select.rangeCompass > 0) {
							range = select.rangeCompass;
							EnumQuestTask t = EnumQuestTask.values()[select.getType()];
							p = new double[] { select.pos.getX() - 0.5d, select.pos.getY() + 0.5d,
									select.pos.getZ() + 0.5d };
							if (t == EnumQuestTask.ITEM) {
								title = new TextComponentTranslation("gui.get").getFormattedText() + ": "
										+ select.getItem().getDisplayName() + ": " + select.getProgress() + "/"
										+ select.getMaxProgress();
							} else if (t == EnumQuestTask.CRAFT) {
								title = new TextComponentTranslation("gui.get").getFormattedText() + ": "
										+ select.getItem().getDisplayName() + ": " + select.getProgress() + "/"
										+ select.getMaxProgress();
							} else if (t == EnumQuestTask.DIALOG) {
								title = new TextComponentTranslation("gui.read").getFormattedText() + ": ";
								Dialog dialog = DialogController.instance.dialogs.get(select.getTargetID());
								if (dialog != null) {
									title += new TextComponentTranslation(dialog.title).getFormattedText();
								} else {
									title = "Dialog";
								}
							} else if (t == EnumQuestTask.LOCATION) {
								title = new TextComponentTranslation("gui.found").getFormattedText() + ": "
										+ select.getTargetName();
							} else if (EnumQuestTask.values()[select.getType()] == EnumQuestTask.MANUAL) {
								title = select.getTargetName();
							}
							if (t == EnumQuestTask.KILL || t == EnumQuestTask.AREAKILL) {
								n = new TextComponentTranslation("entity." + select.getTargetName() + ".name")
										.getFormattedText();
								n = n.substring(0, n.length() - 2);
								if (n.equals("entity." + select.getTargetName() + ".name")) {
									n = select.getTargetName();
								}
								title = new TextComponentTranslation("gui.kill").getFormattedText() + ": " + n + ": "
										+ select.getProgress() + "/" + select.getMaxProgress();
							}
						}
					} else if (qData.isCompleted && qData.quest.completion == EnumQuestCompletion.Npc
							&& qData.quest.getCompleterNpc() != null) {
						p = new double[] { qData.quest.completerPos[0] - 0.5d, qData.quest.completerPos[1] + 0.5d,
								qData.quest.completerPos[2] + 0.5d };
						type = EnumQuestTask.DIALOG.ordinal();
						if (mc.world.provider.getDimension() != qData.quest.completerPos[3]) {
							type = 7;
						} else {
							AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(64.0d, 128.0d, 64.0d);
							List<EntityLivingBase> ents = new ArrayList<>();
							try {
								ents = mc.world.getEntitiesWithinAABB(EntityNPCInterface.class, bb);
							}
							catch (Exception ignored) { }
							final EntityLivingBase et = getEntityLivingBase(p, ents, qData);
							if (et != null) {
								p[0] = et.posX;
								p[1] = et.posY;
								p[2] = et.posZ;
								range = 1;
							}
						}
					}
				}
			}
			if (!n.isEmpty() && p != null) {
				EntityLivingBase e = null;
				AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(range,
						1.5d, range);
				List<EntityLivingBase> ents = new ArrayList<>();
				try {
					ents = mc.world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
				}
				catch (Exception ignored) { }
				if (n.equals("Player")) {
					EntityPlayer pl = mc.world.getClosestPlayerToEntity(mc.player, 32.0d);
					if (pl != null && pl.getActivePotionEffect(  Objects.requireNonNull(Potion.getPotionFromResourceLocation("minecraft:invisibility"))) == null) {
						e = pl;
						range = 1;
					}
				}
				if (e == null) {
					double d = range * range * range;
					EntityLivingBase et = null;
					Vec3i v = new Vec3i(p[0], p[1], p[2]);
					for (EntityLivingBase el : ents) {
						if (!el.getName().equals(n)) {
							continue;
						}
						double r = v.distanceSq(el.getPosition());
                        if (et != null) {
                            if (r >= d) {
                                continue;
                            }
                        }
                        d = r;
                        et = el;
                    }
					if (et == null) {
						bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(range, range, range);
						ents.clear();
						try {
							ents = mc.world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
						}
						catch (Exception ignored) { }
						d = range * range * range;
						for (EntityLivingBase el : ents) {
							if (!el.getName().equals(n)) {
								continue;
							}
							double r = v.distanceSq(el.getPosition());
                            if (et != null) {
                                if (r >= d) {
                                    continue;
                                }
                            }
                            d = r;
                            et = el;
                        }
					}
					e = et;
					range = 1;
				}
				if (e != null) {
					p[0] = e.posX;
					p[1] = e.posY;
					p[2] = e.posZ;
				}
			}

			if (p != null) {
				IRayTraceRotate angles = Util.instance.getAngles3D(mc.player.posX, mc.player.posY + mc.player.eyeHeight, mc.player.posZ, p[0], p[1], p[2]);
				float scale = -30.0f * hud.compassData.scale;
				float incline = -45.0f + hud.compassData.incline;
				double[] uvPos = new double[] { sw.getScaledWidth_double() * hud.compassData.screenPos[0],
						sw.getScaledHeight_double() * hud.compassData.screenPos[1] };

				GlStateManager.pushMatrix();

				if (qt < 40) {

					qt++;
				}

                GlStateManager.translate(uvPos[0], uvPos[1], 0.0d);

				// Named
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.0d, 33.0f, 0.0d);
				int i = 0;
				if (hud.compassData.showQuestName) {
					drawCenteredString(mc.fontRenderer, name, 0, 0, 0xFFFFFFFF);
					i = 12;
				}
				if (hud.compassData.showTaskProgress) {
					drawCenteredString(mc.fontRenderer, title, 0, i, 0xFFFFFFFF);
				}
				GlStateManager.popMatrix();

				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				GlStateManager.translate(0.0f, -31.42857f * hud.compassData.scale + 30.71429f, 0.0f);
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.rotate(incline, 1.0f, 0.0f, 0.0f);
				if (hud.compassData.rot != 0.0f) {
					GlStateManager.rotate(hud.compassData.rot, 0.0f, 1.0f, 0.0f);
				}
				GlStateManager.enableDepth();
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.enableRescaleNormal();
				GlStateManager.enableLighting();
				RenderHelper.enableStandardItemLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);

				// Body
				GlStateManager.pushMatrix();
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("body"), null));
				GlStateManager.popMatrix();

				// Dial
				GlStateManager.pushMatrix();
				GlStateManager.rotate(-1.0f * mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("dial"), null));
				GlStateManager.popMatrix();

				// Arrow_0
				GlStateManager.pushMatrix();
				if (angles != null && (range == 1 || angles.getDistance() > range)) {
					float yaw = mc.player.rotationYaw % 360.0f;
					if (yaw < 0) {
						yaw += 360.0f;
					}
					GlStateManager.rotate(180.0f + yaw - (float) angles.getYaw(), 0.0f, 1.0f, 0.0f);
					GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_0"), null));
				} else {
					double t = System.currentTimeMillis() % 4000.0d;
					double f0 = t < 2000.0d ? -0.00033d * t + 1.0d : 0.00033 * t - 0.30033d;
					GlStateManager.scale(f0, f0, f0);
					GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_3"), null));
				}
				GlStateManager.popMatrix();

				// Arrow_1 upper
				double yP;
                yP = -0.25d * (mc.player.posY - p[1]) / (double) range;
                GlStateManager.pushMatrix();
                if (yP >= -0.25d && yP <= 0.25d) {
                    GlStateManager.translate(0.0d, yP, 0.0d);
                } else {
                    if (yP > 0.25d) {
                        GlStateManager.translate(0.0d, 0.275d, 0.0d);
                    } else if (yP < -0.25d) {
                        GlStateManager.translate(0.0d, -0.275d, 0.0d);
                    }
                    double t = System.currentTimeMillis() % 1000.0d;
                    double f0 = t < 500.0d ? -0.025d + 0.05d * (t % 500.0d) / 500.0d
                            : 0.025d - 0.05d * (t % 500.0d) / 500.0d;
                    GlStateManager.translate(0.0d, f0, 0.0d);
                }
                GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_1"), null));
                GlStateManager.popMatrix();

                // Arrow_2
                GlStateManager.pushMatrix();
                if (yP > 0.25d) {
                    GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_21"), null));
                } else if (yP < -0.25d) {
                    GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_22"), null));
                } else {
                    GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_20"), null));
                }
                GlStateManager.popMatrix();

                if (type >= 0 && type <= EnumQuestTask.values().length) {
					Map<String, String> m = new HashMap<>();
					m.put("customnpcs:util/task_0", "customnpcs:util/task_" + type);
					GlStateManager.pushMatrix();
					GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("fase"), m));
					GlStateManager.popMatrix();
				}

				GlStateManager.disableRescaleNormal();
				GlStateManager.disableLighting();
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.enableBlend();
				GlStateManager.disableDepth();

				GlStateManager.popMatrix();
			}
		}
		// Information from the NBT Book
		String rayName, rayTitle = "";
		if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() instanceof ItemNbtBook || mc.player.getHeldItemOffhand().getItem() instanceof ItemNbtBook)) {
			double distance = ClientProxy.playerData.game.renderDistance;
			Vec3d vec3d = mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
			RayTraceResult result = mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, true);
			if (result != null) {
				BlockPos blockPos = result.getBlockPos();
				Entity entity = Util.instance.getLookEntity(mc.player, distance, false);
				ItemStack st = null;
				IBlockState state = null;
				double dist;
				if (entity != null) {
					dist = Math.round(mc.player.getDistance(entity) * 10.0d) / 10.0d;
					ResourceLocation res = EntityList.getKey(entity);
					rayName = ((char) 167) + "7 [" + entity.getClass().getSimpleName() + "]" + " " + ((char) 167) + "r"
							+ entity.getName() + ((char) 167) + "2 " + dist;
					rayTitle = (res != null ? ((char) 167) + "e" + res : "") + ((char) 167) + "b [X:"
							+ ((char) 167) + "6" + Math.round(entity.posX * 10.0d) / 10.0d + ((char) 167) + "b, Y:"
							+ ((char) 167) + "6" + Math.round(entity.posY * 10.0d) / 10.0d + ((char) 167) + "b, Z:"
							+ ((char) 167) + "6" + Math.round(entity.posZ * 10.0d) / 10.0d + ((char) 167) + "b]";
				} else {
					float f = (float) (mc.player.posX - blockPos.getX() + 0.5d);
					float f1 = (float) (mc.player.posY - blockPos.getY() + 0.5d);
					float f2 = (float) (mc.player.posZ - blockPos.getZ() + 0.5d);
					dist = Math.round(MathHelper.sqrt(f * f + f1 * f1 + f2 * f2) * 10.0d) / 10.0d;
					if (dist > 6.0d && !mc.player.getHeldItemOffhand().isEmpty()
							&& !(mc.player.getHeldItemOffhand().getItem() instanceof ItemNbtBook)) {
						st = mc.player.getHeldItemOffhand();
						rayName = ((char) 167) + "r" + st.getDisplayName();
					} else {
						state = mc.world.getBlockState(blockPos);
						if (dist > 6.0d) {
							result = mc.player.world.rayTraceBlocks(vec3d, vec3d3, true, false, true);
							if (result != null) {
								IBlockState tempState = mc.world.getBlockState(result.getBlockPos());
								if (!(tempState.getBlock() instanceof BlockAir)) {
									state = tempState;
								}
							}
						}
						rayName = ((char) 167) + "7ID:" + Block.REGISTRY.getIDForObject(state.getBlock()) + " "
								+ ((char) 167) + "r" + state.getBlock().getLocalizedName() + ((char) 167) + "2 " + dist;
						rayTitle = ((char) 167) + "7[" + ((char) 167) + "r"
								+ state.getBlock().getClass().getSimpleName() + ((char) 167) + "7" + "; meta:"
								+ ((char) 167) + "e" + state.getBlock().getMetaFromState(state) + ((char) 167) + "7]"
								+ ((char) 167) + "a [X:" + ((char) 167) + "6" + blockPos.getX() + ((char) 167) + "a, Y:"
								+ ((char) 167) + "6" + blockPos.getY() + ((char) 167) + "a, Z:" + ((char) 167) + "6"
								+ blockPos.getZ() + ((char) 167) + "a]";
						if (state.getBlock() instanceof ITileEntityProvider) {
							rayTitle += ((char) 167) + "7 [" + ((char) 167) + "3hasTile" + ((char) 167) + "7]";
						}
					}
				}
				GlStateManager.pushMatrix();
				GlStateManager.translate(
						(hud.getWindowSize()[0] - (double) mc.fontRenderer.getStringWidth(rayName)) / 2.0d,
						hud.getWindowSize()[1] - 65.0d + (st != null ? 10.0d : 0.0d), 0.0d);
				GlStateManager.scale(1.005f, 1.005f, 1.005f);
				if (entity != null) {
					GlStateManager.pushMatrix();
					drawNpc(entity);
					GlStateManager.popMatrix();
				} else if (state != null) {
					st = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1,
							state.getBlock().damageDropped(state));
				}
				if (st != null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-18.0f, -4.0f, 0.0f);
					RenderHelper.enableGUIStandardItemLighting();
					RenderItem itemRender = mc.getRenderItem();
					itemRender.renderItemAndEffectIntoGUI(st, 0, 0);
					itemRender.renderItemOverlays(mc.fontRenderer, st, 0, 0);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
				}
				drawString(mc.fontRenderer, rayName, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.translate(
						(hud.getWindowSize()[0] - (double) mc.fontRenderer.getStringWidth(rayTitle)) / 2.0d,
						hud.getWindowSize()[1] - 55.0d, 0.0d);
				GlStateManager.scale(1.005f, 1.005f, 1.005f);
				drawString(mc.fontRenderer, rayTitle, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();
			}

		}
		CustomNpcs.debugData.end("Players", this, "npcRenderOverlay");
	}

	private static EntityLivingBase getEntityLivingBase(double[] p, List<EntityLivingBase> ents, QuestData qData) {
		double d = 65535.0d;
		Vec3i v = new Vec3i(p[0], p[1], p[2]);
		EntityLivingBase et = null;
		for (EntityLivingBase el : ents) {
			if (!el.getName().equals(qData.quest.getCompleterNpc().getName())) {
				continue;
			}
			double r = v.distanceSq(el.getPosition());
			if (et != null && r >= d) { continue; }
			d = r;
			et = el;
		}
		return et;
	}

	/** Any Regions */
	@SubscribeEvent
	public void npcRenderWorldLastEvent(RenderWorldLastEvent event) {
		CustomNpcs.debugData.start("Players", this, "npcRenderWorldLastEvent");
		if (mc == null) { mc = Minecraft.getMinecraft(); }
		if (sw == null) { sw = new ScaledResolution(mc); }
        BorderController bData = BorderController.getInstance();
		if (mc.player == null || mc.player.world == null) {
			CustomNpcs.debugData.end("Players", this, "npcRenderWorldLastEvent");
			return;
		}
		// position
		dx = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double) event.getPartialTicks();
		dy = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) event.getPartialTicks();
		dz = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double) event.getPartialTicks();
		// Show OBJ Particles
		if (!ClientGuiEventHandler.customParticle.isEmpty()) {
			List<CustomParticle> del = new ArrayList<>();
			for (CustomParticle cp : ClientGuiEventHandler.customParticle) {
				if (!cp.isAlive() || cp.obj == null) {
					del.add(cp);
					continue;
				}
				GlStateManager.pushMatrix();
				if (cp.objList != -1) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					GlStateManager.translate(cp.posX() - dx, cp.posY() - dy, cp.posZ() - dz);
					if (cp.getScale() != 0.0f) {
						GlStateManager.scale(cp.getScale(), cp.getScale(), cp.getScale());
					}
					if (cp.getRotationX() != 0.0f) {
						GlStateManager.rotate(cp.getRotationX(), 1.0f, 0.0f, 0.0f);
					}
					if (cp.getRotationY() != 0.0f) {
						GlStateManager.rotate(cp.getRotationY(), 0.0f, 1.0f, 0.0f);
					}
					if (cp.getRotationZ() != 0.0f) {
						GlStateManager.rotate(cp.getRotationZ(), 0.0f, 0.0f, 1.0f);
					}

					GlStateManager.enableDepth();
					GlStateManager.color(cp.getRedColorF(), cp.getGreenColorF(), cp.getBlueColorF(), cp.getAlphaF());
					GlStateManager.enableRescaleNormal();
					GlStateManager.enableLighting();
					RenderHelper.enableStandardItemLighting();
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);

					GlStateManager.callList(cp.objList);

					GlStateManager.disableBlend();
				} else {
					cp.objList = ModelBuffer.getDisplayList(cp.obj, null, null);
				}
				GlStateManager.popMatrix();
			}
			for (CustomParticle cp : del) {
				ClientGuiEventHandler.customParticle.remove(cp);
			}
		}
		// Show builder data
		BuilderData builder = ItemBuilder.getBuilder(mc.player.getHeldItemMainhand(), mc.player);
		if (builder != null && builder.getID() > -1) {
			if (builder.getType() == 4) {
				drawZone(builder, null);
				CustomNpcs.debugData.end("Players", this, "npcRenderWorldLastEvent");
				return;
			}
			Vec3d vec3d = mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
			ClientGuiEventHandler.result = mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			if (ClientGuiEventHandler.result != null) {
                drawZone(builder, ClientGuiEventHandler.result.getBlockPos());
            }
		}
		// Show block tool hitboxes
		NBTTagCompound nbtMP = null;
		ItemStack mainStack = mc.player.getHeldItemMainhand();
		ItemStack offStack = mc.player.getHeldItemOffhand();
		if (CustomNpcs.ShowHitboxWhenHoldTools && mainStack.getItem() instanceof INPCToolItem || offStack.getItem() instanceof INPCToolItem) {
			AxisAlignedBB aabb = new AxisAlignedBB(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0).offset(mc.player.getPosition());
			List<Entity> list = new ArrayList<>();
			try {
				list = mc.player.world.getEntitiesWithinAABB(Entity.class, aabb);
			}
			catch (Exception ignored) { }
			list.remove(mc.player);
			Entity rayTrE;
			if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) {
				rayTrE = Util.instance.getLookEntity(mc.player, (mainStack.getItem() instanceof ItemNbtBook ? ClientProxy.playerData.game.renderDistance : null), false);
			} else { rayTrE = mc.objectMouseOver.entityHit; }
			if (rayTrE != null && !list.contains(rayTrE)) { list.add(rayTrE); }
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);

			GlStateManager.translate(-dx, -dy, -dz);
			for (Entity e : list) {
				float w = e.width / 2;
				if (e.getDistance(mc.player) - w > 2.9) { continue; }
				AxisAlignedBB col= e.getCollisionBoundingBox();
				if (col == null) { col = new AxisAlignedBB(-w, 0.0, -w, w, e.height, w); }
				GlStateManager.pushMatrix();
				GlStateManager.translate(e.posX, e.posY,  e.posZ);
				RenderGlobal.drawSelectionBoundingBox(col,  0.8f, 0.8f, 0.8f, 0.8f);
				if (e.equals(rayTrE)) { // hover entity
					GlStateManager.glLineWidth(3.0F);
					RenderGlobal.drawSelectionBoundingBox(col.grow(e.width / 20.0),  0.8f, 0.3f, 0.6f, 1.0f);
				}
				GlStateManager.popMatrix();
			}
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		// Show NPC moving path
		if (mainStack.getItem() instanceof ItemNpcMovingPath) { nbtMP = mainStack.getTagCompound(); }
		else if (offStack.getItem() instanceof ItemNpcMovingPath) { nbtMP = offStack.getTagCompound(); }
		if (nbtMP != null && nbtMP.hasKey("NPCID", 3)) {
			Entity entity = mc.player.world.getEntityByID(nbtMP.getInteger("NPCID"));
			if (entity instanceof EntityCustomNpc) {
				drawNpcMovingPath((EntityCustomNpc) entity);
			} else {
				ClientGuiEventHandler.movingPath.clear();
			}
		}
		int id = -1;
		// Show rayTrace point
		if (mainStack.getItem() instanceof ItemBoundary) {
			if (mainStack.hasTagCompound() && Objects.requireNonNull(mainStack.getTagCompound()).hasKey("RegionID", 3)) {
				id = mainStack.getTagCompound().getInteger("RegionID");
			}
			Vec3d vec3d = mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
			ClientGuiEventHandler.result = mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			Zone3D reg = (Zone3D) bData.getRegion(id);
			boolean isShiftPressed = ClientProxy.playerData.hud.hasOrKeysPressed(54, 42);
			if ((isShiftPressed || reg == null) && ClientGuiEventHandler.result != null) {
				final BlockPos pos = getPos();
				GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(3.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.translate(pos.getX() - dx + 0.5d, pos.getY() - dy,  pos.getZ() - dz + 0.5d);
                GlStateManager.rotate((float) ((System.currentTimeMillis() / 7) % 360), 0.0f, 1.0f, 0.0f);
                RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.35d, 0.15d, -0.35d, 0.35d, 0.85d, 0.35d)),  1.0f, 0.50f, 1.0f, 1.0f);
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
		}
		// Show Regions
		for (Zone3D reg : bData.getRegionsInWorld(mc.player.world.provider.getDimension())) {
			if (reg == null || reg.dimensionID != mc.player.world.provider.getDimension() || reg.distanceTo(mc.player) > 250.0d) {
				continue;
			}
			if (mc.player.capabilities.isCreativeMode) { renderRegion(reg, id); }
			else if (reg.showInClient) { drawRegion(reg, -1); }
		}
		CustomNpcs.debugData.end("Players", this, "npcRenderWorldLastEvent");
	}

	private static BlockPos getPos() {
		int x = ClientGuiEventHandler.result.getBlockPos().getX();
		int y = ClientGuiEventHandler.result.getBlockPos().getY();
		int z = ClientGuiEventHandler.result.getBlockPos().getZ();
		try {
			switch (ClientGuiEventHandler.result.sideHit) {
				case UP: {
					y += 1;
					break;
				}
				case NORTH: {
					z -= 1;
					break;
				}
				case SOUTH: {
					z += 1;
					break;
				}
				case WEST: {
					x -= 1;
					break;
				}
				case EAST: {
					x += 1;
					break;
				}
				default: {
					y -= 1;
					break;
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
        return new BlockPos(x, y, z);
	}

	/** HUD Bar Interface Canceled */
	@SubscribeEvent
	public void npcScreenRenderPre(RenderGameOverlayEvent.Pre event) {
		CustomNpcs.debugData.start("Players", this, "npcScreenRenderPre");
		if (!ClientProxy.playerData.hud.isShowElementType(event.getType().ordinal())) { event.setCanceled(true); }
		CustomNpcs.debugData.end("Players", this, "npcScreenRenderPre");
	}

	/** Regions Edit -> Draw */
	private void renderRegion(Zone3D reg, int editID) {
		if (reg.size() == 0) {
			return;
		}
		double distMin = Double.MAX_VALUE;
		boolean start = true;
		Point playerPoint = new Point(mc.player.getPosition().getX(), mc.player.getPosition().getZ());
		Point nearestPoint = null;
		double[] nt = new double[] { 0.0d, 255.0d };
		// Draw Vertex/Bound and get nearest Point
		drawRegion(reg, editID);
		if (reg.getId() != editID) {
			return;
		}
		for (Point p : reg.points.values()) {
			if (start || distMin > p.distance(playerPoint)) {
				start = false;
				distMin = p.distance(playerPoint);
				nearestPoint = p;
				nt[0] = (double) reg.y[0] - 0.175d;
				nt[1] = (double) reg.y[1] + 1.175d;
			}
		}
		if (ClientGuiEventHandler.result != null && ClientGuiEventHandler.result.sideHit != null) {
            BlockPos p = ClientGuiEventHandler.result.getBlockPos();
            double min = p.getY() < reg.y[0] ? (double) p.getY() : (double) reg.y[0];
            double max = (p.getY() > reg.y[1] ? (double) p.getY() : (double) reg.y[1]) + 1.0d;
            Point pb = new Point(p.getX(), p.getZ());
            double px = p.getX(), x = px + 0.5d;
            double py = p.getY(), y = py + 0.5d;
            double pz = p.getZ(), z = pz + 0.5d;
            switch (ClientGuiEventHandler.result.sideHit) {
                case UP: {
                    y += 0.55d;
                    break;
                }
                case NORTH: {
                    z -= 0.55d;
                    break;
                }
                case SOUTH: {
                    z += 0.55d;
                    break;
                }
                case WEST: {
                    x -= 0.55d;
                    break;
                }
                case EAST: {
                    x += 0.55d;
                    break;
                }
                default: {
                    y -= 0.55d;
                    break;
                }
            }
            drawVertex(x, y, z, 1.0f, 1.0f, 0.0f);
            // Bound
            Point[] pns = reg.getClosestPoints(pb, Objects.requireNonNull(NpcAPI.Instance()).getIPos(mc.player.posX, mc.player.posY, mc.player.posZ));
            drawAddSegment(pns, pb, min, max);
        }

		if (nearestPoint != null) { // nearest vertex
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			RenderGlobal.drawSelectionBoundingBox(
					(new AxisAlignedBB(nearestPoint.x + 0.35d, nt[0], nearestPoint.y + 0.35d, nearestPoint.x + 0.65d,
							nt[1], nearestPoint.y + 0.65d)).offset(-dx, -dy, -dz),
					1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

}