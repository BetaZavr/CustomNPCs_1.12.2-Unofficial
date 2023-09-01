package noppes.npcs.client.gui.player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiCompassSetings;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.renderer.TempTexture;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.ObfuscationHelper;
import noppes.npcs.util.ValueUtil;

public class GuiQuestLog
extends GuiNPCInterface
implements GuiYesNoCallback, IGuiData {
	
	private static final Map<Integer, ResourceLocation> ql = Maps.<Integer, ResourceLocation>newTreeMap();
	private static Quest activeQuest;
	
	static {
		GuiQuestLog.ql.clear();
		for (int i = 0; i < 5; i++) {
			GuiQuestLog.ql.put(i, new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_"+i+".png"));
		}
	}

	boolean isStart, needCreateSheets;
	boolean listType; // false-category; true-quest
	int aType, step, tick, mtick;
	int currentList, totalActiveQuest;
	float scale;
	
	final Map<Integer, Integer[]> buttons; // 0-main inv; 1-factions; 2-compass; 3-tab quest list; 4-tab quest info; 5-to right; 6-to left
	final Map<String, Map<Integer, Quest>> activeQuests; // { category, [id, quest]}
	final Map<String, Color> categoryColors; // [name, color]
	final Map<Integer, ResourceLocation> categorys;
	final Map<Integer, ResourceLocation> questLists;
	final Map<Integer, ResourceLocation> scrolling;
	QuestMiniInfo currentQuestList;
	QuestInfo currentQuest;

	public GuiQuestLog() {
		super();
		this.isStart = false;
		this.tick = 15;
		this.mtick = 15;
		this.aType = 0;
		this.step = 0;
		this.buttons = Maps.<Integer, Integer[]>newTreeMap();
		this.categoryColors = Maps.<String, Color>newTreeMap();
		this.activeQuests = Maps.<String, Map<Integer, Quest>>newTreeMap();
		this.categorys = Maps.<Integer, ResourceLocation>newTreeMap();
		this.questLists = Maps.<Integer, ResourceLocation>newTreeMap();
		this.scrolling = Maps.<Integer, ResourceLocation>newTreeMap();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.activeQuests.clear();
		this.categoryColors.clear();
		this.needCreateSheets = true;
		if (!this.isStart || this.activeQuests.isEmpty()) {
			this.scrolling.clear();
			this.buttons.clear();
			if (!this.isStart) {
				this.currentList = 0;
				this.scrolling.put(0, GuiQuestLog.ql.get(1));
				for (int i = 0; i < 4 ; i++) { this.scrolling.put(i, GuiQuestLog.ql.get(3)); }
			}
		}
		// Quest List
		Vector<Quest> list = PlayerQuestController.getActiveQuests(this.player);
		this.totalActiveQuest = list.size();
		if (!list.isEmpty()) {
	    	for (Quest q : list) {
	    		if (!this.activeQuests.containsKey(q.category.getName())) {
	    			this.activeQuests.put(q.category.getName(), Maps.<Integer, Quest>newTreeMap());
	    			String name = q.category.getName();
	    			int r = 128, g = 32, b = 224;
	    			for (int i = 0; i < name.length(); i++) {
	    				switch(i%3) {
		    				case 0: r += name.charAt(i); break;
		    				case 1: g += name.charAt(i); break;
		    				case 2: b += name.charAt(i); break;
		    			}
	    			}
	    			this.categoryColors.put(q.category.getName(), new Color((r * name.length()) % 256, (g * name.length()) % 256, (b * name.length()) % 256));
	    		}
	    		this.activeQuests.get(q.category.getName()).put( q.id, q);
	    	}
		}
		this.cheakActiveQuest();
		// Buttons Poses
		ScaledResolution sw = new ScaledResolution(this.mc);
		float uS = ((float) sw.getScaledWidth_double() - 20.0f) / 320.0f;
		float vS = ((float) sw.getScaledHeight_double() - 30.0f) / 204.0f;
		this.scale = uS < vS ? uS : vS;
		int uC = (int) (sw.getScaledWidth_double() / 2.0d);
		int vC = (int) ((sw.getScaledHeight_double() - 144.0d * this.scale) / 2.0d);
		
		this.buttons.put(0, new Integer[] { uC - (int) (125.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 0-main inv
		this.buttons.put(1, new Integer[] { uC - (int) (93.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 1-factions
		if (CustomNpcs.showQuestCompass) {
			this.buttons.put(2, new Integer[] { uC + (int) (100.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 2-compass
		}
		if (!this.activeQuests.isEmpty()) {
			if (this.listType) {
				this.buttons.put(3, new Integer[] { uC - (int) (154.0f * this.scale), vC + (int) (11.0f * this.scale), (int) (30.0f * this.scale), (int) (28.0f * this.scale) } ); // 3-tab quest list
				if (GuiQuestLog.activeQuest!=null) {
					if (this.currentList == 0) {
						this.buttons.put(4, new Integer[] { uC + (int) (105.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (53.0f * this.scale), (int) (23.0f * this.scale) } );
					} else {
						this.buttons.put(4, new Integer[] { uC - (int) (151.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (44.0f * this.scale), (int) (23.0f * this.scale) } );
					}
				}
			} else {
				this.buttons.put(3, new Integer[] { uC - (int) (154.0f * this.scale), vC + (int) (11.0f * this.scale), (int) (57.0f * this.scale), (int) (28.0f * this.scale) } );
				if (GuiQuestLog.activeQuest!=null) {
					this.buttons.put(4, new Integer[] { uC + (int) (125.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (20.0f * this.scale), (int) (23.0f * this.scale) } );
				}
			}
		}
	}

	private void buttonPress(int id) {
		if (this.aType>-1) { return; }
		System.out.println("button ID: "+id);
		switch(id) {
			case 0: {
				this.mc.displayGuiScreen(new GuiInventory(this.mc.player));
				break;
			}
			case 1: {
				this.mc.displayGuiScreen(new GuiFaction());
				break;
			}
			case 2: {
				this.displayGuiScreen(new GuiCompassSetings(this));
				break;
			}
			case 3: { // tab quest list
				if (this.listType) {
					if (this.currentList!=0) {
						this.aType = 3;
					}
					return;
				}
				break;
			}
			case 4: { // tab quest info
				
				break;
			}
			case 5: { // to right
				
				break;
			}
			case 6: { // to left
				
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) {
			return;
		}
		if (id == 30) {
			//NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestRemoveActive, this.selectedQuest.id);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float pTicks) {
		this.cheakActiveQuest();
		
		GlStateManager.pushMatrix();
		this.drawGradientRect(0, 0, this.mc.displayWidth, this.mc.displayHeight, 0xAA000000, 0xAA000000);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		ScaledResolution sw = new ScaledResolution(this.mc);
		float uS = ((float) sw.getScaledWidth_double() - 20.0f) / 320.0f;
		float vS = ((float) sw.getScaledHeight_double() - 30.0f) / 204.0f;
		this.scale = uS < vS ? uS : vS;
		GlStateManager.enableBlend();
		GlStateManager.scale(this.scale, this.scale, 1.0f);
		float uN = (float) sw.getScaledWidth_double();
		float vN = (float) sw.getScaledHeight_double();
		float uC = uN / 2.0f;
		float vC = (vN - 144.0f * this.scale) / 2.0f;
		
		if (this.needCreateSheets) {
			this.currentQuestList = null;
			this.currentQuest = null;
			this.categorys.clear();
			this.questLists.clear();
			if (!this.activeQuests.isEmpty()) {
				try {
					BufferedImage bufferAdd = ImageIO.read(this.mc.getResourceManager().getResource(GuiQuestLog.ql.get(4)).getInputStream());
					BufferedImage npcCatFrame = new BufferedImage(98, 30, bufferAdd.getType());
					for (int v = 0; v < 30; v++) {
						for (int u = 0; u < 98; u++) {
							int c = bufferAdd.getRGB(u + 113, v + 34);
							if (c==16777215) { continue; }
							npcCatFrame.setRGB(u, v, c);
						}
					}
					int listCat = 0, pos = 0;
					final Map<Integer, Quest> quests = Maps.newTreeMap();
					int i = 0;
			    	for (Map<Integer, Quest> map : this.activeQuests.values()) {
						for (Quest q : map.values()) {
							quests.put(pos, q); // collect quest
							pos++;
							i++;
							if (pos > 9 || i >= this.totalActiveQuest) {
								BufferedImage buffer = ImageIO.read(this.mc.getResourceManager().getResource(GuiQuestLog.ql.get(3)).getInputStream());
								
								Graphics g = buffer.getGraphics();
								if (this.currentList==listCat) { this.currentQuestList = new QuestMiniInfo(quests); }
								for (int p : quests.keySet()) {
									Quest quest = quests.get(p);
									g.drawImage(this.copyBuffer(npcCatFrame, this.categoryColors.get(quest.category.getName())), p < 5 ? 25 : 134, 48 + (p % 5) * 30, 98, 30, null);
									BufferedImage icon = null;
									try { icon = ImageIO.read(this.mc.getResourceManager().getResource(quest.icon).getInputStream()); }
									catch (Exception e) { e.printStackTrace(); }
									if (icon==null) {
										try { icon = ImageIO.read(this.mc.getResourceManager().getResource(new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png")).getInputStream()); }
										catch (Exception e) { e.printStackTrace(); }
									}
									if (icon!=null) {
										int size = 16;
										double s = (double) size / (double) (icon.getWidth()>icon.getHeight() ? icon.getWidth() : icon.getHeight());
										if (s!=1.0f) {
											AffineTransform at = new AffineTransform();
											at.scale(s, s);
											icon = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(icon, new BufferedImage(size, size, icon.getType()));
										}
										g.drawImage(icon, p < 5 ? 27 : 136, 50 + (p % 5) * 30, size, size, null);
									}
									if (this.currentQuestList!=null) {
										Integer[] ps = this.currentQuestList.poses.get(p);
										ps[0] = (int) uC - (int) ((127.0f - (p < 5 ? 47.0f : 156.0f)) * this.scale);
										ps[1] = (int) vC + (int) ((14.0f + (p % 5) * 30.0f) * this.scale);
										ps[2] = (int) (72.0f * this.scale);
										ps[3] = (int) (26.0f * this.scale);
									}
								}
								g.dispose();
								
								ResourceLocation rl = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/cat_list_"+listCat+".png");
								TempTexture texture = new TempTexture(rl, buffer);
								this.mc.getTextureManager().loadTexture(rl, texture);
								Map<ResourceLocation, ITextureObject> mapTextureObjects = ObfuscationHelper.getValue(TextureManager.class, (TextureManager) this.mc.getTextureManager(), Map.class);
								if (!mapTextureObjects.containsKey(rl)) {
									texture.loadTexture(this.mc.getResourceManager());
									mapTextureObjects.put(rl, texture);
								}
								
								this.categorys.put(listCat, rl);
								if (listCat==0 && !this.isStart) { this.scrolling.put(3, rl); }

							    ImageIO.write(buffer, "png", new File(CustomNpcs.Dir, "cat_list_"+listCat+".png"));
							    
								quests.clear();
								listCat++;
								pos = 0;
							}
				    	}
					}
					if (GuiQuestLog.activeQuest!=null) {
						
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			} else {
				this.categorys.put(0, GuiQuestLog.ql.get(3)); // empty
				this.questLists.put(0, GuiQuestLog.ql.get(3)); // empty
			}
			this.needCreateSheets = false;
		}
		
		if (tick>=0) {
			if (tick==0) { pTicks = 0.0f; }
			float part = (float) tick + pTicks;
			float cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 180.0d);
			//System.out.println("UV: ["+uN+", "+vN+"]; pos: ["+uC+", "+vC+"]");
			switch(aType) {
				case 0: { // Open book
					switch(step) {
						case 0: { // 15 ticks
							this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(2));
							GlStateManager.pushMatrix();
							GlStateManager.translate((uC + (1.0f - cos) * (uN - uC))/this.scale, (vC + (1.0f - cos) * (vN - vC))/this.scale, 0.0f);
							this.drawTexturedModalRect(0, 0, 63, 41, 130, 174);
							GlStateManager.popMatrix();
							if (tick==0) {
								step = 1;
								tick = 20;
								mtick = 20;
								GlStateManager.disableBlend();
								GlStateManager.popMatrix();
								return;
							}
							break;
						}
						case 1: { // 20 ticks
							GlStateManager.pushMatrix();
							GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
							this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(0));
							this.drawTexturedModalRect(0, 0, 127, 37, 129, 175);
							GlStateManager.popMatrix();
							boolean up = tick >= mtick / 2;
							part = (up ? (float) tick - (float) mtick / 2.0f: (float) tick) + pTicks;
							cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 90.0d);
							GlStateManager.pushMatrix();
							double h = up ? 174.0f : 175.0d, tx = up ? 63.0d : 0.0d, ty = up ? 41.0d : 37.0d, p = 0.00390625d;
							double w = (up ? (1 - cos) * 130.0d : cos * 127.0d);
							double uH = up ? (1 - part / (double) mtick / 2.0d) * -30.0d : 0.0d;
							double dH = up ? (1 - part / (double) mtick / 2.0d) * 30.0d : 0.0d;
							double suH = up ? 0.0d : (part / (double) mtick * 2.0d) * 30.0d;
							double sdH = up ? 0.0d : (part / (double) mtick * 2.0d) * -30.0d;
							GlStateManager.translate(uC/this.scale - (up ? 0.0d : w), vC/this.scale, 0.0f);
							Tessellator tessellator = Tessellator.getInstance();
							BufferBuilder buffer = tessellator.getBuffer();
							buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
							buffer.pos(0.0d, h + suH, this.zLevel).tex(tx * p, (ty +h) * p).endVertex();
							buffer.pos(w, h + dH, this.zLevel).tex((tx + (up ? 130.0d : 127.0d)) * p, (ty + h) * p).endVertex();
							buffer.pos(w, uH, this.zLevel).tex((tx + (up ? 130.0d : 127.0d)) * p, ty * p).endVertex();
							buffer.pos(0.0d, sdH, this.zLevel).tex(tx * p, ty * p).endVertex();
							if (up) { this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(2)); }
							tessellator.draw();
							GlStateManager.popMatrix();
							if (tick==0) {
								step = 0;
								if (this.activeQuests.isEmpty()) {
									aType = 2;
									tick = 10;
									mtick = 10;
								} else {
									aType = 1;
									step = 0;
									tick = 8;
									mtick = 8;
									listType = false;
								}
								GlStateManager.disableBlend();
								GlStateManager.popMatrix();
								return;
							}
							break;
						}
					}
					break;
				}
				case 1: { // sheets to left - 5 ticks
					GlStateManager.pushMatrix();
					GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(0));
					this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
					if (this.isStart) {
						this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(1));
						this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
					}
					
					ResourceLocation next = this.scrolling.get(step+1);
					ResourceLocation nowl = this.scrolling.get(step);
					if (next==null || nowl==null) {
						GlStateManager.popMatrix();
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
						return;
					}
					if (this.isStart) { // Left
						this.mc.renderEngine.bindTexture(nowl);
						this.drawTexturedModalRect(-127, 0, 0, 37, 127, 175);
					}
					this.mc.renderEngine.bindTexture(next); // right
					this.drawTexturedModalRect(0, 0, 127, 37, 129, 175);
					GlStateManager.popMatrix();

					boolean up = (tick >= mtick / 2);
					part = (up ? (float) tick - (float) mtick / 2.0f: (float) tick) + pTicks;
					
					cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 90.0d);
					GlStateManager.pushMatrix();
					double h = up ? 174.0f : 175.0d, p = 0.00390625d;
					double tx = up ? 127.0d : 0.0d, ty = 37.0d;
					double w = (up ? (1 - cos) * 130.0d : cos * 127.0d);
					double uH = up ? (1 - part / (double) mtick / 2.0d) * -30.0d : 0.0d;
					double dH = up ? (1 - part / (double) mtick / 2.0d) * 30.0d : 0.0d;
					double suH = up ? 0.0d : (part / (double) mtick * 2.0d) * 30.0d;
					double sdH = up ? 0.0d : (part / (double) mtick * 2.0d) * -30.0d;
					//System.out.println("tick: "+tick+"; part: "+(part / (double) mtick / 2.0d)+"; suH: "+suH);
					GlStateManager.translate(uC/this.scale - (up ? 0.0d : w), vC/this.scale, 0.0f);
					
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder buffer = tessellator.getBuffer();
					buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
					
					buffer.pos(0.0d, h + suH, this.zLevel).tex(tx * p, (ty + h) * p).endVertex();
					buffer.pos(w, h + dH, this.zLevel).tex((tx + (up ? 130.0d : 127.0d)) * p, (ty + h) * p).endVertex();
					buffer.pos(w, uH, this.zLevel).tex((tx + (up ? 130.0d : 127.0d)) * p, ty * p).endVertex();
					buffer.pos(0.0d, sdH, this.zLevel).tex(tx * p, ty * p).endVertex();

					this.mc.renderEngine.bindTexture(up ? nowl : next);
					
					tessellator.draw();
					
					GlStateManager.popMatrix();
					
					if (tick==0) {
						step++;
						tick = mtick;
						this.isStart = true;
						if (step > this.scrolling.size() - 2) {
							step = 0;
							aType = 2;
							tick = 10;
							mtick = 10;
						}
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
						return;
					}
					break;
				}
				case 2: { // tabs - 10 ticks
					GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(0));
					this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
					if (!this.activeQuests.isEmpty()) {
						this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(1));
						this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
						ResourceLocation rl = (this.listType ? this.questLists : this.categorys).get(this.currentList);
						if (rl!=null) {
							this.mc.renderEngine.bindTexture(rl);
							this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
						}
					}
					
					int h = (int) (30.0f * cos);
					//System.out.println("tick: "+tick+"; h: "+h);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
					if (h>0) {
						this.drawTexturedModalRect(-125, 0 - h, 113, 0, 28, h);
						this.drawTexturedModalRect(-93, 0 - h, 113, 0, 28, h);
						if (CustomNpcs.showQuestCompass) {
							this.drawTexturedModalRect(99, 0 - h, 113, 0, 28, h);
						}
					}
					if (!this.activeQuests.isEmpty()) {
						if (this.listType) {
							int w = (int) (30.0f * cos);
							this.drawTexturedModalRect(-124 - w, 10, 4, 96, w, 28);
							if (this.currentList == 0) {
								this.drawTexturedModalRect(105, 138, 67, 98, 53, 22);
							} else {
								for (int i = 44; i > 0; i--) {
									this.drawTexturedModalRect(i - 25, 138, 120 - i, 98, 1, 28);
								}
							}
						} else {
							this.drawTexturedModalRect(-154, 10, 4, 96, this.currentList == 0 ? 57 : 45, 28);
							if (GuiQuestLog.activeQuest!=null) {
								int w = (int) ((this.listType ? 53.0f : 20.0f) * cos);
								this.drawTexturedModalRect(124, 138, 120 - w, 98, w, 22);
							}
						}
					}
					
					if (tick==0) { this.isStart = true; }
					break;
				}
			}
			tick--;
		} else {
			this.aType = -1;
			GlStateManager.pushMatrix();
			GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
			this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(0));
			this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
			if (!this.activeQuests.isEmpty()) {
				this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(1));
				this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
				ResourceLocation rl = (this.listType ? this.questLists : this.categorys).get(this.currentList);
				if (rl!=null) {
					this.mc.renderEngine.bindTexture(rl);
					this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
				}
			} else {
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(1.0001f / this.scale, 1.0001f / this.scale, 1.0f);
				GlStateManager.translate(4 + uC, vC, 0.0f);
				String text = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("quest.noquests").getFormattedText());
				float s = 105.0f * this.scale;
				if (this.mc.fontRenderer.getStringWidth(text) > s) {
					String temp = "";
					int h = 0;
					for (int c = 0; c < text.length(); c++) {
						char ch = text.charAt(c);
						if (this.mc.fontRenderer.getStringWidth(temp + ch)>=s || c==text.length()-1) {
							if (temp.lastIndexOf(" ")!=-1) {
								this.mc.fontRenderer.drawString(temp.substring(0, temp.lastIndexOf(" ")), 2, 50 + h * 10, CustomNpcs.questLogColor);
								temp = temp.substring(temp.lastIndexOf(" ")+1)+""+ch;
								if (c==text.length()-1) {
									h++;
									this.mc.fontRenderer.drawString(temp, 2, 50 + h * 10, CustomNpcs.questLogColor);
									break;
								}
							}
							else {
								if (c==text.length()-1) { temp += ch; break; }
								this.mc.fontRenderer.drawString(temp, 2, 50 + h * 10, CustomNpcs.questLogColor);
								temp = ""+ch;
							}
							h++;
						} else {
							temp += ch;
						}
					}
				}
				else { this.mc.fontRenderer.drawString(text, 2, 50, CustomNpcs.questLogColor); }
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
			}
			this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
			this.drawTexturedModalRect(-125, -30, 113, 0, 28, 30);
			
			ItemStack stack = new ItemStack(Items.BANNER, 1, 1);
			RenderHelper.enableGUIStandardItemLighting();
			this.zLevel = 100.0f;
			this.itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			this.itemRender.renderItemAndEffectIntoGUI(stack, -88, -22);
			this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, (String) null);
			GlStateManager.disableLighting();
			this.itemRender.zLevel = 0.0f;
			this.zLevel = 0.0f;
			RenderHelper.disableStandardItemLighting();

			this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
			this.drawTexturedModalRect(-93, -30, 113, 0, 28, 30);

			stack = new ItemStack(Blocks.CRAFTING_TABLE);
			RenderHelper.enableGUIStandardItemLighting();
			this.zLevel = 100.0f;
			this.itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			this.itemRender.renderItemAndEffectIntoGUI(stack, -119, -22);
			this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, (String) null);
			GlStateManager.disableLighting();
			this.itemRender.zLevel = 0.0f;
			this.zLevel = 0.0f;
			RenderHelper.disableStandardItemLighting();

			this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
			if (CustomNpcs.showQuestCompass) {
				this.drawTexturedModalRect(99, -30, 113, 0, 28, 30);
				stack = new ItemStack(Items.COMPASS);
				RenderHelper.enableGUIStandardItemLighting();
				this.zLevel = 100.0f;
				this.itemRender.zLevel = 100.0f;
				GlStateManager.enableLighting();
				GlStateManager.enableRescaleNormal();
				this.itemRender.renderItemAndEffectIntoGUI(stack, 105, -22);
				this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, (String) null);
				GlStateManager.disableLighting();
				this.itemRender.zLevel = 0.0f;
				this.zLevel = 0.0f;
				RenderHelper.disableStandardItemLighting();

				this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
			}
			if (!this.activeQuests.isEmpty()) {
				if (this.listType) {
					this.drawTexturedModalRect(-154, 10, 4, 96, 30, 28); // here
					if (GuiQuestLog.activeQuest!=null) {
						if (this.currentList == 0) {
							this.drawTexturedModalRect(105, 138, 67, 98, 53, 22);
						} else {
							for (int i = 44; i > 0; i--) {
								this.drawTexturedModalRect(i - 152, 138, 120 - i, 98, 1, 28);
							}
						}
					}
				} else {
					this.drawTexturedModalRect(-154, 10, 4, 96, this.currentList == 0 ? 57 : 45, 28);
					if (GuiQuestLog.activeQuest!=null) {
						this.drawTexturedModalRect(124, 138, 100, 98, 20, 22);
					}
				}
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		
		if (this.aType<0 && !this.activeQuests.isEmpty()) {
			if (this.listType) {
				
			} else {
				if (this.currentQuestList == null) { return; }
				GlStateManager.pushMatrix();
				for (int p : this.currentQuestList.quests.keySet()) {
					Quest q = this.currentQuestList.quests.get(p);
					Integer[] ps = this.currentQuestList.poses.get(p);
					
					GlStateManager.pushMatrix();
					this.fontRenderer.drawString(q.getName(), ps[0], ps[1], CustomNpcs.questLogColor);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					GlStateManager.popMatrix();
					//System.out.println("p["+p+"]: "+q.getName()+"; UV: ["+ps[0]+", "+ps[1]+"]");
				}
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.buttons.isEmpty()) { return; }
		for (int id : this.buttons.keySet()) {
			Integer[] s = this.buttons.get(id);
			if (s==null || s.length<4) { continue; }
			if (this.isMouseHover(mouseX, mouseY, s[0], s[1], s[2], s[3])) {
				this.buttonPress(id);
				break;
			}
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.initGui();
	}

	private void cheakActiveQuest() {
		if (GuiQuestLog.activeQuest==null) { return; }
		for (Map<Integer, Quest> map : this.activeQuests.values()) {
			for (Quest q : map.values()) {
				if (q.id == GuiQuestLog.activeQuest.id) { return; }
			}
		}
		GuiQuestLog.activeQuest = null;
	}

	private BufferedImage copyBuffer(BufferedImage parent, Color color) {
		BufferedImage buffer = new BufferedImage(parent.getWidth(), parent.getHeight(), parent.getType());
		for (int v = 0; v < parent.getHeight(); v++) {
			for (int u = 0; u < parent.getWidth(); u++) {
				int num = parent.getRGB(u, v);
				if (num==0) { continue; }
				if (color!=null) {
					Color c = new Color(num);
					c = new Color(ValueUtil.correctInt((c.getRed() + color.getRed()) / 2, 0, 255),
							ValueUtil.correctInt((c.getGreen() + color.getGreen()) / 2, 0, 255),
							ValueUtil.correctInt((c.getBlue() + color.getBlue()) / 2, 0, 255),
							c.getAlpha());
					num = c.getRGB();
				}
				buffer.setRGB(u, v, num);
			}
		}
		return buffer;
	}
	
	private class QuestMiniInfo {
		
		public Map<Integer, Quest> quests;
		public Map<Integer, Integer[]> poses;
		
		public QuestMiniInfo(Map<Integer, Quest> quests) {
			this.quests = Maps.newTreeMap();
			this.quests.putAll(quests);
			this.poses = Maps.<Integer, Integer[]>newTreeMap();
			for (int i = 0; i < quests.size(); i++) { this.poses.put(i, new Integer[4]); }
		}
		
	}
	
	private class QuestInfo {
		
		public Quest q;
		
		public QuestInfo(Quest q) {
			this.q = q;
		}
		
	}
	
}
