package noppes.npcs.client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.gui.GuiCompassSetings;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
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
	public static final ResourceLocation RESOURCE_COMPASS = new ResourceLocation(CustomNpcs.MODID+":models/util/compass.obj");
	
	private Minecraft mc;
	private ScaledResolution sw;
	private BorderController bData;
	private double dx, dy, dz;
	private int qt=0;
	
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
				if (n.equals("entity."+hud.compassData.getNPCName()+".name")) { n = hud.compassData.getNPCName(); 	}
				else { n = n.substring(0, n.length()-2); }
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
			if (qData==null) { return; }
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
				if (select.getOrientationEntityName().isEmpty()) {
					n = new TextComponentTranslation("entity."+select.getOrientationEntityName()+".name").getFormattedText();
					if (n.equals("entity."+select.getOrientationEntityName()+".name")) { n = select.getOrientationEntityName(); 	}
					else { n = n.substring(0, n.length()-2); }
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
						String entityName = new TextComponentTranslation("entity."+select.getTargetName()+".name").getFormattedText();
						if (entityName.equals("entity."+select.getTargetName()+".name")) { entityName = select.getTargetName(); 	}
						else { entityName = entityName.substring(0, entityName.length()-2); }
						n = entityName;
						title = new TextComponentTranslation("gui.kill").getFormattedText()+": "+entityName+ ": " + select.getProgress() + "/" + select.getMaxProgress();
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
			}
			if (e!=null) {
				p[0] = e.posX;
				p[1] = e.posY;
				p[2] = e.posZ;
			}
		}
		
		double[] angles = null;
		if (p==null || p.length!=3) { return; }
		angles = AdditionalMethods.getAngles3D(this.mc.player.posX, this.mc.player.posY+this.mc.player.eyeHeight, this.mc.player.posZ, p[0], p[1], p[2]);
		
		float scale = -30.0f * hud.compassData.scale;
		float incline = -45.0f + hud.compassData.incline;
		
		//System.out.println("screenPos: ["+hud.compassData.screenPos[0]+", "+hud.compassData.screenPos[1]+"]");
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
		int id = -1;
		// Show rayTrace point
		if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
			if (this.mc.player.getHeldItemMainhand().hasTagCompound() && this.mc.player.getHeldItemMainhand().getTagCompound().hasKey("RegionID", 3)) { id = this.mc.player.getHeldItemMainhand().getTagCompound().getInteger("RegionID"); }
			Vec3d vec3d = this.mc.player.getPositionEyes(1.0f);
			Vec3d vec3d2 = this.mc.player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
			ClientGuiEventHandler.result = this.mc.player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			Zone3D reg = BorderController.getInstance().getRegion(id);
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
		if (!this.mc.player.capabilities.isCreativeMode || !ClientProxy.playerData.game.op) { return; }
		// Show All Regions
		for (Zone3D reg : this.bData.getRegionsInWorld(this.mc.player.world.provider.getDimension())) {
			if (reg==null || reg.distanceTo(this.mc.player)>250.0d) { continue; }
			this.renderRegion(reg, id);
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
		float r=1.0f, g=1.0f, b=1.0f;
		// Draw Vertex/Bound and get nearest Point
		r = (float) (reg.color >> 16 & 255) / 255.0f;
		g = (float) (reg.color >> 8 & 255) / 255.0f;
		b = (float) (reg.color & 255) / 255.0f;
		drawRegion(reg, editID, r, g, b);
		if (reg.id!=editID) { return; }
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
			Point[] pns = reg.getClosestPoints(pb, this.mc.player.getPosition());
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

	private void drawRegion(Zone3D reg, int editID, float red, float green, float blue) {
		if (reg==null || reg.size()==0) { return; }
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		float wallAlpha = 0.11f;
		if (reg.size()>1) {
			// Walls
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(-this.dx, -this.dy, -this.dz);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (int pos : reg.points.keySet()) {
				if (pos==reg.points.size()-1) { break; }
				Point p0 = reg.points.get(pos);
				Point p1 = reg.points.get(pos+1);
				
				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
		
				buffer.pos(p1.x+0.5d, (double) reg.y[0], p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p1.x+0.5d, (double) reg.y[1]+1.0d, p1.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[1]+1.0d, p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				buffer.pos(p0.x+0.5d, (double) reg.y[0], p0.y+0.5d).color(red,green,blue,wallAlpha).endVertex();
				
			}
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
			
			if (reg.id == editID) {
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
				double[] c = reg.getExactCenter();
				c[0] += 0.5d;
				c[2] += 0.5d;
				for (int pos : reg.points.keySet()) {
					Point p0 = reg.points.get(pos);
					int minY = reg.y[0], maxY = reg.y[1];
					Point p1 = reg.points.get(0);
					if (reg.points.containsKey(pos+1)) { p1 = reg.points.get(pos+1); }
					buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,1.0f).endVertex();
					buffer.pos(c[0], minY, c[2]).color(red,green,blue,1.0f).endVertex();
					buffer.pos(p0.x+0.5d, 1.0d+maxY, p0.y+0.5d).color(red,green,blue,1.0f).endVertex();
					buffer.pos(c[0], 1.0d+maxY, c[2]).color(red,green,blue,1.0f).endVertex();
					
					buffer.pos(p0.x+0.5d, minY, p0.y+0.5d).color(red,green,blue,1.0f).endVertex();
					buffer.pos(p1.x+0.5d, minY, p1.y+0.5d).color(red,green,blue,1.0f).endVertex();
					
					if (maxY-minY>1) {
						for (int i=1; i<=maxY-minY; i++) {
							buffer.pos(p0.x+0.5d, minY+(double)i, p0.y+0.5d).color(red,green,blue,1.0f).endVertex();
							buffer.pos(p1.x+0.5d, minY+(double)i, p1.y+0.5d).color(red,green,blue,1.0f).endVertex();
						}
					}
				}
				tessellator.draw();
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
		
		if (reg.size()>2) { // Polygons up and down
			for (int i=0; i<2; i++) {
				double y = i==0?(double) reg.y[1]+0.98d:(double) reg.y[0]+0.02;
				buffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(-this.dx, -this.dy, -this.dz);
				buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
				for (Point p : reg.points.values()) {
					buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue, wallAlpha).endVertex();
				}
				for (int pos=reg.points.size()-1; pos>=0; pos--) {
					Point p = reg.points.get(pos);
					buffer.pos(p.x+0.5d, y, p.y+0.5d).color(red,green,blue, wallAlpha).endVertex();
				}
				tessellator.draw();
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
		
		for (int i=0; i<reg.points.size(); i++) {
			Point p0 = reg.points.get(i);
			Point p1 = reg.points.get(i>=reg.points.size()-1?0:(i+1));
			// vertex as * down and up
			if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
				drawVertex((double) p0.x+0.5d, (double) reg.y[0], (double) p0.y+0.5d, 0.0f, 1.0f, 0.0f);
				drawVertex((double) p0.x+0.5d, (double) reg.y[1]+1.0d, (double) p0.y+0.5d, 0.0f, 1.0f, 0.0f);
			}
			// Bound
			drawSegment(p0, p1, (double) reg.y[0], (double) reg.y[1]+1.0d, 2.0f, red, green, blue, 0.5f);
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
		buffer.pos(x-0.05d, y-0.05d, z-0.05d).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+0.05d, y+0.05d, z+0.05d).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x-0.05d, y-0.05d, z+0.05d).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+0.05d, y+0.05d, z-0.05d).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x+0.05d, y-0.05d, z+0.05d).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x-0.05d, y+0.05d, z-0.05d).color(red,green,blue,1.0f).endVertex();
		
		buffer.pos(x+0.05d, y-0.05d, z-0.05d).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x-0.05d, y+0.05d, z+0.05d).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x-0.065d, y, z).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x+0.065d, y, z).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x, y-0.065d, z).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x, y+0.065d, z).color(red,green,blue,1.0f).endVertex();

		buffer.pos(x, y, z-0.065d).color(red,green,blue,1.0f).endVertex();
		buffer.pos(x, y, z+0.065d).color(red,green,blue,1.0f).endVertex();
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

}
