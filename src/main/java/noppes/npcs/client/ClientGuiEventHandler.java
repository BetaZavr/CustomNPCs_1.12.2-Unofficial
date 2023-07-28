package noppes.npcs.client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.gui.GuiCompassSetings;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestTask;
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
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;

@SideOnly(Side.CLIENT)
public class ClientGuiEventHandler
extends Gui
{

	private static final ResourceLocation COIN_NPC = new ResourceLocation(CustomNpcs.MODID, "textures/items/coin_gold.png");
	private static final ResourceLocation RESOURCE_SLOT = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private static final ResourceLocation CREATIVE_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static final ResourceLocation[] BORDER;
	public static final ResourceLocation RESOURCE_COMPASS = new ResourceLocation(CustomNpcs.MODID+":models/util/compass.obj");
	
	static {
		BORDER = new ResourceLocation[16];
		for (int i=0; i<16; i++) { BORDER[i] = new ResourceLocation(CustomNpcs.MODID, "textures/util/border/"+(i<10 ? "0"+i : i)+".png"); }
	}
	
	private Minecraft mc;
	private ScaledResolution sw;
	private BorderController bData;
	private double dx, dy, dz;
	private int qt=0;
	private List<int[]> listMovingPath;
	private List<double[]> listPath = Lists.<double[]>newArrayList();
	
	
	public static RayTraceResult result;

	/** HUD Bar Interfase Canceled */
	@SubscribeEvent
	public void npcScreenRenderPre(RenderGameOverlayEvent.Pre event) {
		if (!ClientProxy.playerData.hud.isShowElementType(event.getType().ordinal())) { event.setCanceled(true); }
	}
	
	/** HUD Bar Interfase */
	@SuppressWarnings("unused")
	@SubscribeEvent
	public void npcRenderOverlay(RenderGameOverlayEvent.Text event) {
		this.mc = Minecraft.getMinecraft();
		this.sw = new ScaledResolution(this.mc);
		if (this.mc.currentScreen!=null && !(this.mc.currentScreen instanceof GuiChat) && !(this.mc.currentScreen instanceof GuiCompassSetings)) { return; }
		
		PlayerOverlayHUD hud = ClientProxy.playerData.hud;
		TreeMap<Integer, TreeMap<Integer, IGuiComponent>> mapC = hud.getGuiComponents();
		GuiCustom gui = new GuiCustom(null);
		for (int type : mapC.keySet()) {
			for (IGuiComponent component : mapC.get(type).values()) {
				component.offSet(type, hud.getWindowSize());
				component.setParent(gui);
				component.onRender(this.mc, -1, -1, 0, 0);
			}
		}
		TreeMap<Integer, TreeMap<Integer, IItemSlot>> mapS = hud.getGuiSlots();
		for (int type : mapS.keySet()) {
			int[] os = this.getOffset(type);
			for (int id : mapS.get(type).keySet()) {
				IItemSlot slot = mapS.get(type).get(id);
				GlStateManager.pushMatrix();
				int x = os[0] == 0 ? slot.getPosX() : os[0] - slot.getPosX() - 18;
				int y = os[1] == 0 ? slot.getPosY() : os[1] - slot.getPosY() - 18;
				GlStateManager.translate(x, y, id);
				this.mc.renderEngine.bindTexture(ClientGuiEventHandler.RESOURCE_SLOT);
				this.drawTexturedModalRect(0, 0, 0, 0, 18, 18);
				if (!slot.getStack().isEmpty()) {
					ItemStack stack = slot.getStack().getMCItemStack();
					GlStateManager.translate(1, 1, 0);
					RenderHelper.enableStandardItemLighting();
					this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					this.drawString(this.mc.fontRenderer, "" + stack.getCount(), (int) (12 - (stack.getCount() > 9 ? 9 : 0)), 9, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
				}
				GlStateManager.popMatrix();
			}
		}
		
		String name = "", title = "";
		double[] p = null;
		int type = 0, range = 5;
		String n = "";
		if (hud.compassData.show) {
			p = new double[] { hud.compassData.pos.getX()-0.5d, hud.compassData.pos.getY()+0.5d, hud.compassData.pos.getZ()+0.5d };
			name = hud.compassData.name;
			title = hud.compassData.title;
			type = hud.compassData.getType();
			if (this.mc.world.provider.getDimension()!=hud.compassData.getDimensionID()) { type = 7; }
			range = hud.compassData.getRange();
			if (hud.compassData.getNPCName().isEmpty()) {
				n = new TextComponentTranslation("entity."+hud.compassData.getNPCName()+".name").getFormattedText();
				n = n.substring(0, n.length()-2);
				if (n.equals("entity."+hud.compassData.getNPCName()+".name")) { n = hud.compassData.getNPCName(); 	}
			}
		} else {
			if (!ClientProxy.playerData.questData.activeQuests.containsKey(hud.questID) || (hud.questID<=0 && ClientProxy.playerData.questData.activeQuests.size()>0)) {
				for (int id : ClientProxy.playerData.questData.activeQuests.keySet()) {
					if (id!=hud.questID && id>0) {
						hud.questID = id;
						break;
					}
				}
			}
			QuestData qData = ClientProxy.playerData.questData.activeQuests.get(hud.questID);
			if (qData!=null) {
				double minD = Double.MAX_VALUE;
				QuestObjective select = null;
				for (IQuestObjective io : qData.quest.questInterface.getObjectives(this.mc.player)) {
					QuestObjective o = (QuestObjective) io;
					if (o.isCompleted()) { continue; }
					if (qData.quest.step!=1) {
						if (o.rangeCompass==0 && select==null) {
							select = o;
						} else if (o.rangeCompass!=0) {
							double d = AdditionalMethods.distanceTo(o.pos.getX()+0.5d, o.pos.getY(), o.pos.getZ()+0.5d, this.mc.player.posX, this.mc.player.posY+this.mc.player.eyeHeight, this.mc.player.posZ);
							if (d <= minD) {
								minD = d;
								select = o;
							}
						}
						continue;
					}
					select = o;
					break;
				}
				if (select!=null) {
					name = qData.quest.getTitle();
					type = select.getType();
					if (!select.getOrientationEntityName().isEmpty()) {
						n = new TextComponentTranslation("entity."+select.getOrientationEntityName()+".name").getFormattedText();
						n = n.substring(0, n.length()-2);
						if (n.equals("entity."+select.getOrientationEntityName()+".name")) { n = select.getOrientationEntityName(); 	}
					}
					if (this.mc.world.provider.getDimension()!=select.dimensionID) { type = 7; }
					if (type!=EnumQuestTask.KILL.ordinal() && type!=EnumQuestTask.AREAKILL.ordinal()) { range = 1; }
					if (select.rangeCompass>0) {
						range = select.rangeCompass;
						EnumQuestTask t = EnumQuestTask.values()[select.getType()];
						p = new double[] { select.pos.getX()-0.5d, select.pos.getY()+0.5d, select.pos.getZ()+0.5d };
						if (t == EnumQuestTask.ITEM) {
							title = new TextComponentTranslation("gui.get").getFormattedText()+": "+select.getItem().getDisplayName() + ": " + select.getProgress() + "/" + select.getMaxProgress();
						}
						else if (t == EnumQuestTask.CRAFT) {
							title = new TextComponentTranslation("gui.get").getFormattedText()+": "+select.getItem().getDisplayName() + ": " + select.getProgress() + "/" + select.getMaxProgress();
						}
						else if (t == EnumQuestTask.DIALOG) {
							title = new TextComponentTranslation("gui.read").getFormattedText()+": ";
							Dialog dialog = DialogController.instance.dialogs.get(select.getTargetID());
							if (dialog != null) { title += new TextComponentTranslation(dialog.title).getFormattedText(); }
							else { title = "Dialog"; }
						}
						else if (t == EnumQuestTask.LOCATION) {
							title = new TextComponentTranslation("gui.found").getFormattedText()+": "+select.getTargetName();
						}
						else if (EnumQuestTask.values()[select.getType()] == EnumQuestTask.MANUAL) {
							title = new TextComponentTranslation("gui.do").getFormattedText()+": "+select.getTargetName();
						}
						if (t == EnumQuestTask.KILL || t == EnumQuestTask.AREAKILL) {
							n = new TextComponentTranslation("entity."+select.getTargetName()+".name").getFormattedText();
							n = n.substring(0, n.length()-2);
							if (n.equals("entity."+select.getTargetName()+".name")) { n = select.getTargetName(); 	}
							title = new TextComponentTranslation("gui.kill").getFormattedText()+": "+n+ ": " + select.getProgress() + "/" + select.getMaxProgress();
						}
					}
				} else if (qData.isCompleted && qData.quest.completion==EnumQuestCompletion.Npc && !qData.quest.completerNpc.isEmpty()){
					p = new double[] { qData.quest.completerPos[0]-0.5d, qData.quest.completerPos[1]+0.5d, qData.quest.completerPos[2]+0.5d };
					type = EnumQuestTask.DIALOG.ordinal();
					if (this.mc.world.provider.getDimension()!=qData.quest.completerPos[3]) { type = 7; }
					else {
						AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(64.0d, 128.0d, 64.0d);
						List<EntityLivingBase> ents = this.mc.world.getEntitiesWithinAABB(EntityNPCInterface.class, bb);
						double d = 65535.0d;
						Vec3i v = new Vec3i(p[0], p[1], p[2]);
						EntityLivingBase et = null;
						for (EntityLivingBase el : ents) {
							if (!el.getName().equals(qData.quest.completerNpc)) { continue; }
							double r = v.distanceSq((Vec3i) el.getPosition());
							if (et == null) { d = r; et = el; }
							else {
								if (r >= d) { continue; }
								d = r;
								et = el;
							}
						}
						if (et!=null) {
							p[0] = et.posX;
							p[1] = et.posY;
							p[2] = et.posZ;
							range = 1;
						}
					}
				}
			}
		}
		if (!n.isEmpty() && p!=null) {
			EntityLivingBase e = null;
			AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(range, 1.5d, range);
			List<EntityLivingBase> ents = this.mc.world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
			if (n.equals("Player")) {
				EntityPlayer pl = this.mc.world.getClosestPlayerToEntity(this.mc.player, 32.0d);
				if (pl!=null && pl.getActivePotionEffect(Potion.getPotionFromResourceLocation("minecraft:invisibility"))==null) {
					e = pl;
					range = 1;
				}
			}
			if (e==null) {
				double d = range * range * range;
				EntityLivingBase et = null;
				Vec3i v = new Vec3i(p[0], p[1], p[2]);
				for (EntityLivingBase el : ents) {
					if (!el.getName().equals(n)) { continue; }
					double r = v.distanceSq((Vec3i) el.getPosition());
					if (et == null) { d = r; et = el; }
					else {
						if (r >= d) { continue; }
						d = r;
						et = el;
					}
				}
				if (et==null) {
					bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(p[0], p[1], p[2]).grow(range, range, range);
					ents = this.mc.world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
					d = range * range * range;
					for (EntityLivingBase el : ents) {
						if (!el.getName().equals(n)) { continue; }
						double r = v.distanceSq((Vec3i) el.getPosition());
						if (et == null) { d = r; et = el; }
						else {
							if (r >= d) { continue; }
							d = r;
							et = el;
						}
					}
				}
				e = et;
				range = 1;
			}
			if (e!=null) {
				p[0] = e.posX;
				p[1] = e.posY;
				p[2] = e.posZ;
			}
		}
		
		if (p!=null && p.length>=3) {
			double[] angles = AdditionalMethods.getAngles3D(this.mc.player.posX, this.mc.player.posY+this.mc.player.eyeHeight, this.mc.player.posZ, p[0], p[1], p[2]);
			float scale = -30.0f * hud.compassData.scale;
			float incline = -45.0f + hud.compassData.incline;
			double[] uvPos = new double[] { this.sw.getScaledWidth_double() * hud.compassData.screenPos[0], this.sw.getScaledHeight_double() * hud.compassData.screenPos[1] };

			GlStateManager.pushMatrix();
			
			if (this.qt<40) {
				
				this.qt++;
			}
			else if (this.qt>0 && p==null) {
				
				this.qt--;
			}
			
			GlStateManager.translate(uvPos[0], uvPos[1], 0.0d);
			
			// Named
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0d, 33.0f, 0.0d);
			int i = 0;
			if (hud.compassData.showQuestName) {
				this.drawCenteredString(this.mc.fontRenderer, name, 0, 0, 0xFFFFFFFF);
				i = 12;
			}
			if (hud.compassData.showTaskProgress) { this.drawCenteredString(this.mc.fontRenderer, title, 0, i, 0xFFFFFFFF); }
			GlStateManager.popMatrix();

			this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.translate(0.0f, -31.42857f * hud.compassData.scale + 30.71429f, 0.0f);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(incline, 1.0f, 0.0f, 0.0f);
			if (hud.compassData.rot!=0.0f)  { GlStateManager.rotate(hud.compassData.rot, 0.0f, 1.0f, 0.0f); }
			GlStateManager.enableDepth();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableLighting();
			RenderHelper.enableStandardItemLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
			
			//ClientGuiEventHandler.compasRes = new ResourceLocation(CustomNpcs.MODID+":models/util/compass2.obj");
			
			// Body
			GlStateManager.pushMatrix();
			GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("body"), null));
			GlStateManager.popMatrix();
			
			// Dial
			GlStateManager.pushMatrix();
			GlStateManager.rotate(-1.0f * this.mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
			GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("dial"), null));
			GlStateManager.popMatrix();
			
			// Arrow_0
			GlStateManager.pushMatrix();
			if (angles!=null && (range==1 || angles[3]>range)) {
				float yaw = this.mc.player.rotationYaw % 360.0f;
				if (yaw<0) { yaw += 360.0f; }
				GlStateManager.rotate(180.0f + yaw - (float) angles[0], 0.0f, 1.0f, 0.0f);
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_0"), null));
			} else {
				double t = System.currentTimeMillis()%4000.0d;
				double f0 = t<2000.0d ? -0.00033d * t + 1.0d : 0.00033 * t - 0.30033d; 
				GlStateManager.scale(f0, f0, f0);
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_3"), null));
			}
			GlStateManager.popMatrix();

			// Arrow_1 upper
			double yP = 0.0d;
			if (p!=null) {
				yP = -0.25d * (this.mc.player.posY - p[1]) / (double) range;
				GlStateManager.pushMatrix();
				if (yP >= -0.25d && yP <= 0.25d) { GlStateManager.translate(0.0d, yP, 0.0d); }
				else {
					if (yP > 0.25d) { GlStateManager.translate(0.0d, 0.275d, 0.0d); }
					else if (yP < -0.25d) { GlStateManager.translate(0.0d, -0.275d, 0.0d); }
					double t = System.currentTimeMillis()%1000.0d;
					double f0 = t<500.0d ? -0.025d + 0.05d * (t % 500.0d)/500.0d : 0.025d - 0.05d * (t % 500.0d)/500.0d; 
					GlStateManager.translate(0.0d, f0, 0.0d);
				}
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_1"), null));
				GlStateManager.popMatrix();
			}
			
			// Arrow_2
			if (p!=null) {
				GlStateManager.pushMatrix();
				if (yP > 0.25d) { GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_21"), null)); }
				else if (yP < -0.25d) { GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_22"), null)); }
				else { GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_20"), null)); }
				GlStateManager.popMatrix();
			}
			
			if (type>=0 && type<=EnumQuestTask.values().length) {
				Map<String, String> m = Maps.<String, String>newHashMap();
				//type = 0;
				m.put("customnpcs:util/task_0", "customnpcs:util/task_"+type);
				GlStateManager.pushMatrix();
				GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("fase"), m));
				GlStateManager.popMatrix();
			}

			GlStateManager.disableRescaleNormal();
			GlStateManager.disableLighting();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableBlend();
			GlStateManager.disableDepth();
			
			GlStateManager.popMatrix();
		}
		
		String rayName = "", rayTitle = "";
		if (this.mc.player!=null && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemNbtBook) {
			double distance = this.mc.gameSettings.getOptionFloatValue(GameSettings.Options.RENDER_DISTANCE) * 16.0d;
			Vec3d vec3d = this.mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = this.mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
			RayTraceResult result = this.mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, true);
			if (result != null) {
				BlockPos blockPos = result.getBlockPos();
				Entity entity = null;
				vec3d3 = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
				List<Entity> list = this.mc.player.world.getEntitiesWithinAABBExcludingEntity(this.mc.player, this.mc.player.getEntityBoundingBox().grow(distance));
				List<Entity> rs = new ArrayList<Entity>();
				for (Entity entity1 : list) {
					if (entity1.canBeCollidedWith() && entity1 != this.mc.player) {
						AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
						RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d3);
						if (raytraceresult1 == null) { continue; }
						rs.add(entity1);
					}
				}
				if (!rs.isEmpty()) { 
					rs.sort((o1, o2) -> {
						double d1 = this.mc.player.getDistance(o1);
						double d2 = this.mc.player.getDistance(o2);
						if (d1 == d2) { return 0; }
						else { return (d1 > d2) ? 1 : -1; }
					});
					entity = rs.get(0);
				}
				ItemStack st = null;
				IBlockState state = null;
				double dist = -1.0d;
				if (entity!=null) {
					dist = Math.round(this.mc.player.getDistance(entity) * 10.0d)/10.0d;
					ResourceLocation res = EntityList.getKey(entity);
					rayName = ((char) 167) + "7 [" + entity.getClass().getSimpleName() + "]" +
							" " + ((char) 167) + "r" + entity.getName() +
							((char) 167) + "2 "+dist;
					rayTitle = (res !=null ? ((char) 167) + "e" + res.toString() : "") +
							((char) 167) + "b [X:" + ((char) 167) + "6" + Math.round(entity.posX * 10.0d)/10.0d +
							((char) 167) + "b, Y:" + ((char) 167) + "6" + Math.round(entity.posY * 10.0d)/10.0d +
							((char) 167) + "b, Z:" + ((char) 167) + "6" + Math.round(entity.posZ * 10.0d)/10.0d +
							((char) 167)+"b]";
				}
				else {
					float f = (float)(this.mc.player.posX - blockPos.getX() + 0.5d);
			        float f1 = (float)(this.mc.player.posY - blockPos.getY() + 0.5d);
			        float f2 = (float)(this.mc.player.posZ - blockPos.getZ() + 0.5d);
			        dist = Math.round(MathHelper.sqrt(f * f + f1 * f1 + f2 * f2) * 10.0d)/10.0d;
			        if (dist>6.0d && !this.mc.player.getHeldItemOffhand().isEmpty()) {
			        	st = this.mc.player.getHeldItemOffhand();
			        	rayName = ((char) 167) + "r" + st.getDisplayName();
			        } else {
						state = this.mc.world.getBlockState(blockPos);
			        	if (dist>6.0d) {
			    			result = this.mc.player.world.rayTraceBlocks(vec3d, vec3d3, true, false, true);
			    			if (result != null) {
			    				IBlockState tempState = this.mc.world.getBlockState(result.getBlockPos());
			    				if (!(tempState.getBlock() instanceof BlockAir)) { state = tempState; }
			    			}
			        	}
						rayName = ((char) 167) + "7ID:" + Block.REGISTRY.getIDForObject(state.getBlock()) + 
							" " + ((char) 167) + "r" + state.getBlock().getLocalizedName() +
							((char) 167) + "2 "+dist;
						rayTitle = ((char) 167) + "7[" + ((char) 167) + "r" + state.getBlock().getClass().getSimpleName() +
							((char) 167) + "7" + "; meta:" + ((char) 167) + "e" + state.getBlock().getMetaFromState(state) + ((char) 167)+"7]" +
							((char) 167) + "a [X:" + ((char) 167) + "6" + blockPos.getX() +
							((char) 167) + "a, Y:" + ((char) 167) + "6" + blockPos.getY() +
							((char) 167) + "a, Z:" + ((char) 167) + "6" + blockPos.getZ() +
							((char) 167)+"a]";
						if (state.getBlock() instanceof ITileEntityProvider) {
							rayTitle += ((char) 167) + "7 [" + ((char) 167)+"3hasTile"+ ((char) 167) + "7]";
						}
			        }
				}
				
				GlStateManager.pushMatrix();
				GlStateManager.translate((hud.getWindowSize()[0] - (double) this.mc.fontRenderer.getStringWidth(rayName)) / 2.0d, hud.getWindowSize()[1] - 65.0d + (st!=null ? 10.0d : 0.0d), 0.0d);
				if (entity!=null) {
					GlStateManager.pushMatrix();
					this.drawNpc(entity, -12, 10, 0.75f, 0, 0);
					GlStateManager.popMatrix();
				}
				else if (state!=null){
					st = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
				}
				if (st!=null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(-18.0f, -4.0f, 0.0f);
					RenderHelper.enableGUIStandardItemLighting();
					RenderItem itemRender = this.mc.getRenderItem();
					itemRender.renderItemAndEffectIntoGUI(st, 0, 0);
					itemRender.renderItemOverlays(this.mc.fontRenderer, st, 0, 0);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
				}
				this.drawString(this.mc.fontRenderer, rayName, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.translate((hud.getWindowSize()[0] - (double) this.mc.fontRenderer.getStringWidth(rayTitle)) / 2.0d, hud.getWindowSize()[1] - 55.0d, 0.0d);
				this.drawString(this.mc.fontRenderer, rayTitle, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();
			}
			
		}
	}

	private void drawNpc(Entity entityIn, int x, int y, float zoomed, int rotation, int vertical) {
		if (!(entityIn instanceof EntityLivingBase)) { return; }
		EntityLivingBase entity = (EntityLivingBase) entityIn;
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) { npc = (EntityNPCInterface) entity; }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, 50.0f);
		float scale = 1.0f;
		if (entity.height > 2.4) {
			scale = 2.0f / entity.height;
		}
		GlStateManager.scale(-30.0f * scale * zoomed, 30.0f * scale * zoomed, 30.0f * scale * zoomed);
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
			npc.ais.orientation = rotation;
		}
		GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
		entity.renderYawOffset = rotation;
		entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + rotation);
		entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		entity.rotationYawHead = entity.rotationYaw;
		this.mc.getRenderManager().playerViewY = 180.0f;
		if (vertical!=0) {
			GlStateManager.translate(0.0f, 1.0f - Math.cos((double) vertical * 3.14d / 180.0d), 0.0f);
			GlStateManager.rotate(vertical, 1.0f, 0.0f, 0.0f);
		}
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		float n = f2;
		entity.renderYawOffset = n;
		entity.prevRenderYawOffset = n;
		float n2 = f3;
		entity.rotationYaw = n2;
		entity.prevRotationYaw = n2;
		float n3 = f4;
		entity.rotationPitch = n3;
		entity.prevRotationPitch = n3;
		float n4 = f5;
		entity.rotationYawHead = n4;
		entity.prevRotationYawHead = n4;
		if (npc != null) {
			npc.ais.orientation = orientation;
		}
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	private int[] getOffset(int type) {
		int[] offsets = new int [] {0, 0};
		switch(type) {
			case 1: { // left down
				offsets[0] = 0;
				offsets[1] = (int) this.sw.getScaledHeight_double();
				break;
			}
			case 2: { // right up
				offsets[0] = (int) this.sw.getScaledWidth_double();
				offsets[1] = 0;
				break;
			}
			case 3: { // right down
				offsets[0] = (int) this.sw.getScaledWidth_double();
				offsets[1] = (int) this.sw.getScaledHeight_double();
				break;
			}
			default: { // left up
				offsets[0] = 0;
				offsets[1] = 0;
			}
		}
		return offsets;
	}
	
	@SubscribeEvent
	public void onDrawScreenEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
		Minecraft mc = event.getGui().mc;
		if (event.getGui() instanceof GuiInventory && CustomNpcs.showMoney) {
			String text = AdditionalMethods.getTextReducedNumber(CustomNpcs.proxy.getPlayerData(mc.player).game.money, true, true, false) + CustomNpcs.charCurrencies;
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			int x=0, y=0;
			try {
				x = ((GuiInventory) mc.currentScreen).getGuiLeft()+122;
				y = ((GuiInventory) mc.currentScreen).getGuiTop() + 61;
			}
			catch (Exception e) { return; }
			GlStateManager.translate(x, y, 0.0f);
			mc.renderEngine.bindTexture(ClientGuiEventHandler.COIN_NPC);
			float s = 16.0f / 250.f;
			GlStateManager.scale(s, s, s);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			mc.fontRenderer.drawString(text, x+15, y+8 / 2, 0x404040, false);
			GlStateManager.popMatrix();
			int xm = event.getMouseX(), ym = event.getMouseY();
			if (xm>x&& ym>y && xm<x+50  && ym<y+12) {
				List<String> hoverText = new ArrayList<String>();
				hoverText.add(new TextComponentTranslation("inventory.hover.currency").getFormattedText());
				hoverText.add("" + CustomNpcs.proxy.getPlayerData(mc.player).game.money);
				event.getGui().drawHoveringText(hoverText, xm, ym);
			}
		}
		else if (event.getGui() instanceof GuiContainerCreative && CustomNpcs.showMoney) {
			int x=0, y=0;
			try {
				x = ((GuiContainerCreative) event.getGui()).getGuiLeft() - 30;
				y = ((GuiContainerCreative) event.getGui()).getGuiTop() + 4;
			}
			catch (Exception e) { return; }
			
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			
			GlStateManager.pushMatrix();
			this.mc.getTextureManager().bindTexture(CREATIVE_TABS);
			GlStateManager.translate(x, y+28, 0.0f);
			GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f);
			int mx = event.getMouseX() - x;
			int my = event.getMouseY() -y;
			if (mx>0 && mx<=32 && my>0 && my<=28) { this.drawTexturedModalRect(0, 2, 28, 32, 28, 32); }
			else { this.drawTexturedModalRect(0, 0, 28, 0, 28, 30); }
			GlStateManager.translate(-28.0f, 0.0f, 0.0f);
			my -= 28;
			if (mx>0 && mx<=32 && my>0 && my<=28) { this.drawTexturedModalRect(0, 2, 28, 32, 28, 32); }
			else { this.drawTexturedModalRect(0, 0, 28, 0, 28, 30); }
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			RenderHelper.enableGUIStandardItemLighting();
			String i = String.valueOf(31L - (System.currentTimeMillis() / 100L) % 32L);
			if (i.length()<2) { i = "0"+i; }
			this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/items/compass_"+i+".png"));
			GlStateManager.translate(x+10, y+6, 0.0f);
			float s = 16.0f / 256.0f;
			GlStateManager.scale(s, s, s);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.translate(0.0f, 28.0f / s, 0.0f);
			this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/items/book_normal.png"));
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
			
		}
	}

	@SubscribeEvent
	public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof GuiContainerCreative) {
			int x=0, y=0;
			try {
				x = ((GuiContainerCreative) event.getGui()).getGuiLeft() - 30;
				y = ((GuiContainerCreative) event.getGui()).getGuiTop() + 4;
			}
			catch (Exception e) { return; }
			event.getButtonList().add(new GuiNpcButton(150, x, y, 32, 28, 0, 128, ClientGuiEventHandler.CREATIVE_TABS));
			event.getButtonList().add(new GuiNpcButton(151, x, y + 28, 32, 28, 0, 128, ClientGuiEventHandler.CREATIVE_TABS));
		}
	}
	
	@SubscribeEvent
	public void onButtonEvent(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() instanceof GuiContainerCreative) {
			switch(event.getButton().id) {
				case 150: {
					this.mc.displayGuiScreen(new GuiCompassSetings(event.getGui()));
					break;
				}
				case 151: {
					this.mc.displayGuiScreen(new GuiQuestLog(this.mc.player));
					break;
				}
			}
		}
	}
	
	/** Any Regions */
	@SubscribeEvent
	public void npcRenderWorldLastEvent(RenderWorldLastEvent event) {
		if (this.mc==null) { this.mc = Minecraft.getMinecraft(); return; }
		if (this.sw==null) { this.sw = new ScaledResolution(this.mc); return; }
		if (this.bData==null) { this.bData = BorderController.getInstance(); return; }
		if (this.mc.player.world==null) { return; }

		//if (!this.mc.player.capabilities.isCreativeMode || !ClientProxy.playerData.game.op) { return; }
		// position
		this.dx = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * (double) event.getPartialTicks();
		this.dy = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * (double) event.getPartialTicks();
		this.dz = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * (double) event.getPartialTicks();
		
		if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBuilder) {
			int id = this.mc.player.getHeldItemMainhand().getTagCompound().getInteger("ID");
			if (!CommonProxy.dataBuilder.containsKey(id)) {
				Client.sendDataDelayCheck(EnumPlayerPacket.GetBuildData, this.mc.player, 1000);
				return;
			}
			BuilderData builder = CommonProxy.dataBuilder.get(id);
			if (builder.type==4) { this.drawZone(builder, null); return; }
			Vec3d vec3d = this.mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = this.mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
			ClientGuiEventHandler.result = this.mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			if (ClientGuiEventHandler.result!=null && ClientGuiEventHandler.result.getBlockPos()!=null) {
				this.drawZone(builder, ClientGuiEventHandler.result.getBlockPos());
			}
		}
		else if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemNpcMovingPath) {
			NBTTagCompound nbt = this.mc.player.getHeldItemMainhand().getTagCompound();
			if (nbt!=null && nbt.hasKey("NPCID", 3)) {
				Entity entity = this.mc.player.world.getEntityByID(nbt.getInteger("NPCID"));
				if (entity instanceof EntityCustomNpc) {
					this.drawNpcMovingPath((EntityCustomNpc) entity);
				}
				else {
					this.listMovingPath = null;
					this.listPath.clear();
				}
			}
		}
		int id = -1;
		// Show rayTrace point
		if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
			if (this.mc.player.getHeldItemMainhand().hasTagCompound() && this.mc.player.getHeldItemMainhand().getTagCompound().hasKey("RegionID", 3)) { id = this.mc.player.getHeldItemMainhand().getTagCompound().getInteger("RegionID"); }
			Vec3d vec3d = this.mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = this.mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
			ClientGuiEventHandler.result = this.mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			Zone3D reg = (Zone3D) BorderController.getInstance().getRegion(id);
			if (reg == null && ClientGuiEventHandler.result!=null && ClientGuiEventHandler.result.getBlockPos()!=null) {
				int x = ClientGuiEventHandler.result.getBlockPos().getX();
				int y = ClientGuiEventHandler.result.getBlockPos().getY();
				int z = ClientGuiEventHandler.result.getBlockPos().getZ();
				try {
					switch(ClientGuiEventHandler.result.sideHit) {
						case UP: { y += 1; break; }
						case NORTH: { z -= 1; break; }
						case SOUTH: {  z += 1; break; }
						case WEST: { x -= 1; break; }
						case EAST: { x += 1; break; }
						default: { y -= 1; break; }
					}
				}
				catch (Exception e) { }
				BlockPos pos = new BlockPos(x, y, z);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX()-this.dx+0.5d, pos.getY()-this.dy, pos.getZ()-this.dz+0.5d);
				GlStateManager.rotate((System.currentTimeMillis()/7) % 360, 0.0f, 1.0f, 0.0f);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.35d, 0.15d, -0.35d, 0.35d, 0.85d, 0.35d)), 1.0f, 0.50f, 1.0f, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
		// Show Regions
		for (Zone3D reg : this.bData.getRegionsInWorld(this.mc.player.world.provider.getDimension())) {
			if (reg==null || reg.dimensionID!=this.mc.player.world.provider.getDimension() || reg.distanceTo(this.mc.player)>250.0d) { continue; }
			if (this.mc.player.capabilities.isCreativeMode) { this.renderRegion(reg, id); }
			else if (reg.showInClient) {
				this.drawRegion(reg, -1);
			}
		}
	}

	/** Regions Edit -> Draw */
	private void renderRegion(Zone3D reg, int editID) {
		if (reg.size()==0) { return; }
		double distMin = Double.MAX_VALUE;
		boolean start = true;
		Point playerPoint = new Point(this.mc.player.getPosition().getX(), this.mc.player.getPosition().getZ());
		Point nearestPoint = null;
		double[] nt = new double[] { 0.0d, 255.0d };
		// Draw Vertex/Bound and get nearest Point
		drawRegion(reg, editID);
		if (reg.getId()!=editID) { return; }
		for (Point p : reg.points.values()) {
			if (start || distMin>p.distance(playerPoint)) {
				start = false;
				distMin = p.distance(playerPoint);
				nearestPoint = p;
				nt[0] = (double) reg.y[0]-0.175d;
				nt[1] = (double) reg.y[1]+1.175d;
			}
		}
		if (ClientGuiEventHandler.result!=null && ClientGuiEventHandler.result.sideHit!=null && ClientGuiEventHandler.result.getBlockPos()!=null) {
			BlockPos p = ClientGuiEventHandler.result.getBlockPos();
			double min = p.getY()<reg.y[0]?(double) p.getY():(double) reg.y[0];
			double max = (p.getY()>reg.y[1]?(double) p.getY():(double) reg.y[1])+1.0d;
			Point pb = new Point(p.getX(), p.getZ());
			double px = p.getX(), x = px+0.5d;
			double py = p.getY(), y = py+0.5d;
			double pz = p.getZ(), z = pz+0.5d;
			switch(ClientGuiEventHandler.result.sideHit) {
				case UP: { y+=0.55d; py += 2.0d; break; }
				case NORTH: { z-=0.55d; pz -= 1.0d; break; }
				case SOUTH: { z+=0.55d; pz += 1.0d; break; }
				case WEST: { x-=0.55d; px -= 1.0d; break; }
				case EAST: { x+=0.55d; px += 1.0d; break; }
				default: { y-=0.55d; py -= 1.0d; break; }
			}
			drawVertex(x, y, z, 1.0f, 1.0f, 0.0f);
			// Bound
			Point[] pns = reg.getClosestPoints(pb, NpcAPI.Instance().getIPos(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ));
			drawAddSegment(pns, pb, min, max, 0.75f, 0.75f, 0.75f, 1.0f);
		}

		if (nearestPoint!=null) { // nearest vertex
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(nearestPoint.x+0.35d, nt[0], nearestPoint.y+0.35d, nearestPoint.x+0.65d, nt[1], nearestPoint.y+0.65d)).offset(-this.dx, -this.dy, -this.dz), 1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	private void drawRegion(Zone3D reg, int editID) {
		if (reg==null || reg.size()==0) { return; }
		float red = (float) (reg.color >> 16 & 255) / 255.0f;
		float green = (float) (reg.color >> 8 & 255) / 255.0f;
		float blue = (float) (reg.color & 255) / 255.0f;
		
		// simple colored
		//float wallAlpha = 0.11f;
		
		// textured
		int xm = reg.getMinX(), xs = reg.getMaxX() - reg.getMinX();
		int zm = reg.getMinZ(), zs = reg.getMaxZ() - zm;
		double size = (double) (xs > zs ? xs : zs) / 4.0D;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		if (reg.size()>1) {
			// Walls
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.depthMask(false);
			GlStateManager.translate(-this.dx, -this.dy, -this.dz);
			
			// simple colored
			//GlStateManager.disableTexture2D();
			//buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			
			// textured
			GlStateManager.color(red, green, blue, 1.0f);
			this.mc.getTextureManager().bindTexture(BORDER[(int) (this.mc.world.getTotalWorldTime() % 16L)]);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); // textured
			for (int pos : reg.points.keySet()) {
				Point p0 = reg.points.get(pos);
				Point p1 = pos==reg.points.size()-1 ? reg.points.get(0) : reg.points.get(pos+1);
				
				/* simple colored:
				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();*/
				
				// textured
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).tex(0.0D, size).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).tex(size, size).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).tex(size, 0.0D).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).tex(0.0D, 0.0D).endVertex();

				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).tex(0.0D, 0.0D).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).tex(size, 0.0D).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).tex(size, size).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).tex(0.0D, size).endVertex();
			}
			GlStateManager.scale(1.0F, 1.0F, 1.0F);
			
			tessellator.draw();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			
			GlStateManager.depthMask(true);
			//GlStateManager.enableTexture2D(); // simple colored
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();

			// Lines
			buffer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-this.dx, -this.dy, -this.dz);
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			double x = 0.0d, z = 0.0d;
			for (Point v : reg.points.values()) {
				 x += (double) v.x;
				 z += (double) v.y;
			}
			if (reg.points.size()>0) {
				x /= (double) reg.points.size();
				z /= (double) reg.points.size();
			}
			x += 0.5d;
			z += 0.5d;
			float alpha = 0.5f;
			for (int pos : reg.points.keySet()) {
				Point p0 = reg.points.get(pos);
				int minY = reg.y[0], maxY = reg.y[1];
				Point p1 = reg.points.get(0);
				if (reg.points.containsKey(pos+1)) { p1 = reg.points.get(pos+1); }
				
				buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p0.x+0.5d, maxY+1.0D, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p1.x+0.5d, maxY+1.0D, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
				
				if (reg.getId() == editID) {
					buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
					buffer.pos(x, minY, z).color(red,green,blue,alpha).endVertex();
					buffer.pos(p0.x+0.5d, 1.0d+maxY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
					buffer.pos(x, 1.0d+maxY, z).color(red,green,blue,alpha).endVertex();
					
					if (maxY-minY>1) {
						for (int i=1; i<=maxY-minY; i++) {
							buffer.pos(p0.x+0.5d, minY+(double)i, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
							buffer.pos(p1.x+0.5d, minY+(double)i, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
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
		
		
		if (reg.size()>2) { // Polygons up and down
			for (int i=0; i<2; i++) {
				double y = i==0?(double) reg.y[1]+0.98d:(double) reg.y[0]+0.02;
				buffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.depthMask(false);
				GlStateManager.translate(-this.dx, -this.dy, -this.dz);
				
				// simple colored
				//GlStateManager.disableTexture2D();
				//buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
				
				// textured
				GlStateManager.color(red, green, blue, 1.0f);
				this.mc.getTextureManager().bindTexture(BORDER[(int) (this.mc.world.getTotalWorldTime() % 16L)]);
				buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
				
				for (Point p : reg.points.values()) {
					// simple colored
					//buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue, wallAlpha).endVertex();
					
					// textured
					double texU = 2.0d * size *  (double) (p.x - xm) / (double) xs;
					double texV = 2.0d * size * (double) (p.y - zm) / (double) zs;
					buffer.pos(p.x+0.5d, y, p.y+0.5d).tex(texU, texV).endVertex();
				}
				for (int pos=reg.points.size()-1; pos>=0; pos--) {
					Point p = reg.points.get(pos);
					// simple colored
					//buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue, wallAlpha).endVertex();

					// textured
					double texU = 2.0d * size *  (double) (p.x - xm) / (double) xs;
					double texV = 2.0d * size * (double) (p.y - zm) / (double) zs;
					buffer.pos(p.x+0.5d, y, p.y+0.5d).tex(texU, texV).endVertex();
				}
				tessellator.draw();
				GlStateManager.depthMask(true);
				//GlStateManager.enableTexture2D(); // simple colored
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
		
		if (reg.getId() == editID) {
			for (int i=0; i<reg.points.size(); i++) {
				Point p0 = reg.points.get(i);
				Point p1 = reg.points.get(i>=reg.points.size()-1?0:(i+1));
				// vertex as * down and up
				if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					drawVertex((double) p0.x+0.5d, (double) reg.y[0], (double) p0.y+0.5d, red, green, blue);
					drawVertex((double) p0.x+0.5d, (double) reg.y[1]+1.0d, (double) p0.y+0.5d, red, green, blue);
				}
				// Bound
				drawSegment(p0, p1, (double) reg.y[0], (double) reg.y[1]+1.0d, 2.0f, red, green, blue, 0.5f);
			}
		}
	}
	
	private void drawSegment(Point p0, Point p1, double minY, double maxY, float width, float red, float green, float blue, float alpha) {
		if (p0==null || p1==null) { return; }
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(width);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-this.dx, -this.dy, -this.dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,alpha).endVertex(); // _
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p0.x+0.5d, maxY, p0.y+0.5d).color(red,green,blue,alpha).endVertex(); // -
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex(); // |
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void drawAddSegment(Point[] pns, Point p1, double minY, double maxY, float red, float green, float blue, float alpha) {
		if (pns==null || pns.length!=2 || p1==null) { return; }
		Point p0 = pns[0], p2 = pns[1];
		if (p0==null || p2==null) { return; }
		float wallAlpha = 0.11f;
		// Walls
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-this.dx, -this.dy, -this.dz);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		
		buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p0.x+0.5d, maxY, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p0.x+0.5d, maxY, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();

		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p2.x+0.5d, maxY, p2.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p2.x+0.5d, minY, p2.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p2.x+0.5d, minY, p2.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p2.x+0.5d, maxY, p2.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		
		// Lines
		buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-this.dx, -this.dy, -this.dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p2.x+0.5d, minY, p2.y+0.5d).color(red,green,blue,alpha).endVertex();
		if (maxY-minY>1) {
			for (double i=1.0d; i<maxY-minY; i++) {
				buffer.pos(p0.x+0.5d, minY+i, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p1.x+0.5d, minY+i, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p1.x+0.5d, minY+i, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
				buffer.pos(p2.x+0.5d, minY+i, p2.y+0.5d).color(red,green,blue,alpha).endVertex();
			}
		}
		buffer.pos(p0.x+0.5d, maxY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p2.x+0.5d, maxY, p2.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p0.x+0.5d, maxY, p0.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p1.x+0.5d, maxY, p1.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p2.x+0.5d, minY, p2.y+0.5d).color(red,green,blue,alpha).endVertex();
		buffer.pos(p2.x+0.5d, maxY, p2.y+0.5d).color(red,green,blue,alpha).endVertex();
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
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-this.dx, -this.dy, -this.dz);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x-sizeS, y-sizeS, z-sizeS).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+sizeS, y+sizeS, z+sizeS).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x-sizeS, y-sizeS, z+sizeS).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+sizeS, y+sizeS, z-sizeS).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x+sizeS, y-sizeS, z+sizeS).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x-sizeS, y+sizeS, z-sizeS).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x+sizeS, y-sizeS, z-sizeS).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x-sizeS, y+sizeS, z+sizeS).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x-sizeS, y, z).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+sizeS, y, z).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x, y-sizeS, z).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x, y+sizeS, z).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x, y, z-sizeS).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x, y, z+sizeS).color(red,green,blue,1.0f).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawZone(BuilderData builder, BlockPos pos) {
		if (builder.type<3) {
			int[] s = new int[] { 0, 0, 0 };
			int[] e = new int[] { 1, 1, 1 };
			float r=1.0f, g=0.0f, b=0.0f;
			if (builder!=null) {
				int[] m = builder.getDirections(this.mc.player);
				for (int j=0; j<3; j++) {
					s[j] = m[j];
					e[j] = m[j+3];
				}
				if (builder.type==1) { r=0.0f; g=1.0f; b=1.0f; }
				else if (builder.type==2) { r=1.0f; g=0.0f; b=1.0f; }
			}
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX()+s[0]-this.dx, pos.getY()+s[1]-this.dy, pos.getZ()+s[2]-this.dz);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, e[0], e[1], e[2])), r, g, b, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
	
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX()-this.dx+0.5d, pos.getY()-this.dy, pos.getZ()-this.dz+0.5d);
			//GlStateManager.rotate((System.currentTimeMillis()/15) % 360, 0.0f, 1.0f, 0.0f);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d, 0.0d, -0.5d, 0.5d, 1.0d, 0.5d)), 1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		else if (builder.type==4) {
			if (!builder.schMap.containsKey(0)) { return;}
			pos = builder.schMap.get(0);
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(5.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos.getX()-this.dx, pos.getY()-this.dy, pos.getZ()-this.dz);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1,1,1)), 1,1,1, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
			if (builder.schMap.containsKey(1)) {
				pos = builder.schMap.get(1);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX()-this.dx, pos.getY()-this.dy, pos.getZ()-this.dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1,1,1)), 0,1,0, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			if (builder.schMap.containsKey(2)) {
				pos = builder.schMap.get(2);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX()-this.dx, pos.getY()-this.dy, pos.getZ()-this.dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, 1,1,1)), 0,0,1, 1.0f);
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
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(3.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(pos.getX()-this.dx, pos.getY()-this.dy, pos.getZ()-this.dz);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(0, 0, 0, aabb.maxX-aabb.minX+1, aabb.maxY-aabb.minY+1, aabb.maxZ-aabb.minZ+1)), 1,0,0, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}
	
	private void drawNpcMovingPath(EntityCustomNpc npc) {
		Client.sendDataDelayCheck(EnumPlayerPacket.MovingPathGet, npc, 5000, npc.getEntityId());
		if (npc.ais.getMovingType()!=2 || npc.ais.getMovingPathSize()<=1) {
			this.listMovingPath = null;
			this.listPath.clear();
			return;
		}
		boolean type = npc.ais.getMovingPathType()==0;
		List<int[]> list = npc.ais.getMovingPath();
		boolean bo = this.listMovingPath!=null && list==this.listMovingPath;
		if (this.listMovingPath==null) { this.listMovingPath = list; }
		else if (!bo && this.mc.world.getTotalWorldTime()%100 == 0) {
			bo = list.size()==this.listMovingPath.size();
			if (bo) {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).length!=this.listMovingPath.get(i).length) {
						bo = false;
						break;
					}
					for (int j = 0; j < list.get(i).length; j++) {
						if (list.get(i)[j]!=this.listMovingPath.get(i)[j]) {
							bo = false;
							break;
						}
					}
					if (!bo) { break; }
				}
			}
		}
		if (!bo || this.listPath.isEmpty()) {
			NBTTagCompound npcNbt = new NBTTagCompound();
			npc.writeToNBTAtomically(npcNbt);
			Entity entity = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
			entity.setUniqueId(UUID.randomUUID());
			if (entity instanceof EntityCustomNpc) {
				this.listPath.clear();
				EntityCustomNpc newNpc = (EntityCustomNpc) entity;
				int[] pos = list.get(0);
				double yo = 0.0d;
				IBlockState state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
				if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
				newNpc.setPosition(pos[0], pos[1]+yo, pos[2]);
				this.listPath.add(new double[] { pos[0] + 0.5d, pos[1] + yo + 0.4d, pos[2] + 0.5d});
				newNpc.display.setVisible(1);
				newNpc.display.setSize(1);
				newNpc.display.setShowName(1);
				this.mc.world.spawnEntity(newNpc);
				PathNavigate nv = newNpc.getNavigator();
				for (int i = 1; i < list.size(); i++) {
					pos = list.get(i);
					nv.clearPath();
					newNpc.motionX = 0.0d;
					newNpc.motionY = 0.0d;
					newNpc.motionZ = 0.0d;
					Path path = nv.getPathToXYZ(pos[0], pos[1], pos[2]);
					if (path == null) {
						this.listPath.add(new double[0]);
						yo = 0.0d;
						state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
						if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
						newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
						continue;
					}
					for (int p = 0; p < path.getCurrentPathLength(); p++) {
						PathPoint pp = path.getPathPointFromIndex(p);
						this.listPath.add(new double[] { pp.x + 0.5d, pp.y + 0.4d, pp.z + 0.5d});
					}
					yo = 0.0d;
					state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
					if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
					newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
				}
				if (type) {
					nv.clearPath();
					pos = list.get(list.size()-1);
					yo = 0.0d;
					state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
					if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
					newNpc.setPosition(pos[0], pos[1] + yo, pos[2]);
					newNpc.motionX = 0.0d;
					newNpc.motionY = 0.0d;
					newNpc.motionZ = 0.0d;
					pos = list.get(0);
					Path path = nv.getPathToXYZ(pos[0], pos[1], pos[2]);
					if (path != null) {
						for (int p = 0; p < path.getCurrentPathLength(); p++) {
							PathPoint pp = path.getPathPointFromIndex(p);
							this.listPath.add(new double[] { pp.x + 0.5d, pp.y + 0.4d, pp.z + 0.5d});
						}
					}
				}
				newNpc.isDead = true;
			}
			this.listMovingPath = list;
		}
		
		double[] pre = null;
		float r, g, b;
		// Can Way
		if (this.listPath.size()>1) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-this.dx, -this.dy, -this.dz);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			r = 0.8f; g = 0.8f; b = 0.8f;
			pre = null;
			for (int i = 0; i < this.listPath.size(); i++) {
				double[] pos = this.listPath.get(i);
				if (pos.length==0) { pre = null; continue; }
				double[] newPre = new double[] { pos[0], pos[1], pos[2] };
				if (pre!=null) {
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
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.translate(-this.dx, -this.dy, -this.dz);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		r = 1.0f; g = 0.0f; b = 0.0f;
		for (int i = 0; i < list.size(); i++) {
			int[] pos = list.get(i);
			double yo = 0.0d;
			IBlockState state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
			if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
			double[] newPre = new double[] { pos[0]+0.5d, pos[1]+0.5d+yo, pos[2]+0.5d };
			if (pre!=null) {
				buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
				buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
			}
			pre = newPre;
			if (type && i == list.size()-1) {
				pos = list.get(0);
				newPre = new double[] { pos[0]+0.5d, pos[1]+0.5d+yo, pos[2]+0.5d };
				buffer.pos(pre[0], pre[1], pre[2]).color(r, g, b, 1.0f).endVertex();
				buffer.pos(newPre[0], newPre[1], newPre[2]).color(r, g, b, 1.0f).endVertex();
			}
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		
		// block poses
		for (int i = 0; i < list.size(); i++) {
			if (i==0) { r = 1.0f; g = 1.0f; b = 1.0f; }
			else { r = 1.0f; g = 1.0f; b = 0.0f; }
			int[] pos = list.get(i);
			double yo = 0.0d;
			IBlockState state = this.mc.world.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
			if (state!=null && state.isFullBlock() || state.isFullCube()) { yo = 1.0d; }
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(pos[0]-this.dx+0.5d, pos[1]-this.dy+0.85d+yo, pos[2]-this.dz+0.5d);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.25d, -0.25d, -0.25d, 0.25d, 0.25d, 0.25d)), r, g, b, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
	
}