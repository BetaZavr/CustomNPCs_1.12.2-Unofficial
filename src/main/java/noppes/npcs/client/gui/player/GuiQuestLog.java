package noppes.npcs.client.gui.player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.GuiCompassSetings;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.renderer.TempTexture;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.ObfuscationHelper;
import noppes.npcs.util.ValueUtil;

public class GuiQuestLog
extends GuiNPCInterface
implements GuiYesNoCallback, IGuiData {
	
	private static final Map<Integer, ResourceLocation> ql = Maps.<Integer, ResourceLocation>newTreeMap();
	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	public static QuestInfo activeQuest;
	
	static {
		GuiQuestLog.ql.clear();
		for (int i = 0; i < 6; i++) {
			GuiQuestLog.ql.put(i, new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_"+i+".png"));
		}
	}

	boolean isStart, needCreateSheets;
	boolean listType; // false-category; true-quest
	int aType, step, tick, mtick;
	int currentList, currentCat, totalActiveQuest, hoverQuestID, hoverButton;
	float scale;
	String currentCategory;
	
	final Map<Integer, Integer[]> buttons; // 0-main inv; 1-factions; 2-compass; 3-tab quest list; 4-tab quest info; 5-to right; 6-to left
	final Map<String, Map<Integer, Quest>> activeQuests; // { category, [id, quest]}
	final Map<String, Color> categoryColors; // [name, color]
	final Map<Integer, ResourceLocation> categorys;
	final Map<Integer, ResourceLocation> questLists;
	final Map<Integer, ResourceLocation> scrolling;
	private final Random rnd = new Random();
	QuestMiniInfo currentQuestList;

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
				this.currentCat = 0;
				this.currentCategory = "";
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
		this.guiLeft = uC;
		this.guiTop = (int) (sw.getScaledHeight_double() / 2.0d);
		
		this.buttons.put(0, new Integer[] { uC - (int) (125.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 0-main inv
		this.buttons.put(1, new Integer[] { uC - (int) (93.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 1-factions
		if (CustomNpcs.showQuestCompass) {
			this.buttons.put(2, new Integer[] { uC + (int) (100.0f * this.scale), vC - (int) (30.0f * this.scale), (int) (28.0f * this.scale), (int) (30.0f * this.scale) } ); // 2-compass
		}
		if (!this.activeQuests.isEmpty()) {
			if (this.listType) { // Show Quest
				this.buttons.put(3, new Integer[] { uC - (int) (154.0f * this.scale), vC + (int) (11.0f * this.scale), (int) (30.0f * this.scale), (int) (28.0f * this.scale) } ); // 3-tab quest list
				if (GuiQuestLog.activeQuest!=null) {
					if (this.currentList == 0) {
						this.buttons.put(4, new Integer[] { uC + (int) (105.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (53.0f * this.scale), (int) (23.0f * this.scale) } );
					} else {
						this.buttons.put(4, new Integer[] { uC - (int) (151.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (44.0f * this.scale), (int) (23.0f * this.scale) } );
					}
					if (!GuiQuestLog.activeQuest.map.isEmpty()) {
						String key = GuiQuestLog.activeQuest.map.keySet().toArray(new String[1])[0];
						int totalLists = (int) Math.ceil((double) GuiQuestLog.activeQuest.map.get(key).size() / 2.0d);
						if (this.currentList + 1 < totalLists) { // to right
							this.buttons.put(5, new Integer[] { uC + (int) (100.0f * this.scale), vC + (int) (169.0f * this.scale), 23, 13 } );
						}
						if (this.currentList != 0) { // to left
							this.buttons.put(6, new Integer[] { uC - (int) (120.0f * this.scale), vC + (int) (169.0f * this.scale), 23, 13 } );
						}
					}
				}
			} else { // Show Categories
				this.buttons.put(3, new Integer[] { uC - (int) (154.0f * this.scale), vC + (int) (11.0f * this.scale), (int) (57.0f * this.scale), (int) (28.0f * this.scale) } );
				if (GuiQuestLog.activeQuest!=null) {
					this.buttons.put(4, new Integer[] { uC + (int) (125.0f * this.scale), vC + (int) (138.0f * this.scale), (int) (33.0f * this.scale), (int) (23.0f * this.scale) } );
				}
				if (this.totalActiveQuest>10) {
					if ((this.currentList + 1) * 10 < this.totalActiveQuest) { // to right
						this.buttons.put(5, new Integer[] { uC + (int) (100.0f * this.scale), vC + (int) (169.0f * this.scale), 23, 13 } );
					}
					if (this.currentList != 0) { // to left
						this.buttons.put(6, new Integer[] { uC - (int) (120.0f * this.scale), vC + (int) (169.0f * this.scale), 23, 13 } );
					}
				}
			}
			
		}
	}

	private void buttonPress(int id) {
		if (this.aType>-1) { return; }
		switch(id) {
			case 0: {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				this.mc.displayGuiScreen(new GuiInventory(this.mc.player));
				break;
			}
			case 1: {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				this.mc.displayGuiScreen(new GuiFaction());
				break;
			}
			case 2: {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				this.displayGuiScreen(new GuiCompassSetings(this));
				break;
			}
			case 3: { // category select
				if (!this.listType) { return; }
				if (this.questLists.containsKey(this.currentList) && this.questLists.get(this.currentList)!=null) {
					this.scrolling.put(4, this.questLists.get(this.currentList));
				}
				else { this.scrolling.put(4, GuiQuestLog.ql.get(3)); }
				for (int i = 1; i < 4 ; i++) { this.scrolling.put(i, GuiQuestLog.ql.get(3)); }
				this.scrolling.put(0, this.categorys.get(0));
				this.listType = false;
				this.currentList = 0;
				this.aType = 3;
				this.tick = 5;
				this.mtick = 5;
				break;
			}
			case 4: {
				if (this.listType) { return; }
				this.listType = true;
				this.scrolling.put(0, this.categorys.get(this.currentList));
				for (int i = 1; i < 4 ; i++) { this.scrolling.put(i, GuiQuestLog.ql.get(3)); }
				this.scrolling.put(4, this.questLists.get(0));
				this.currentList = 0;
				this.aType = 1;
				this.tick = 5;
				this.mtick = 5;
				break;
			}
			case 5: { // to right
				Map<Integer, ResourceLocation> map = this.listType ? this.questLists : this.categorys;
				this.scrolling.put(0, map.get(this.currentList));
				this.currentList++;
				if (this.currentList >= map.size()) { this.currentList = map.size() - 1; }
				this.scrolling.put(1, map.get(this.currentList));
				this.step = 0;
				this.aType = 1;
				this.tick = 5;
				this.mtick = 5;
				break;
			}
			case 6: { // to left
				Map<Integer, ResourceLocation> map = this.listType ? this.questLists : this.categorys;
				this.scrolling.put(0, map.get(this.currentList));
				this.currentList--;
				if (this.currentList < 0) { this.currentList = 0; }
				this.scrolling.put(1, map.get(this.currentList));
				this.step = 0;
				this.aType = 3;
				this.tick = 5;
				this.mtick = 5;
				break;
			}
			case 7: { // quest select in list
				if (this.listType) { return; }
				switch(this.hoverButton) {
					case -1: // select quest
						for (Map<Integer, Quest> map : activeQuests.values()) {
							for (Quest q : map.values()) {
								if (q.id==this.hoverQuestID) {
									if (GuiQuestLog.activeQuest==null || this.hoverQuestID!=GuiQuestLog.activeQuest.q.id) {
										GuiQuestLog.activeQuest = new QuestInfo(q);
									}
									this.listType = true;
									this.scrolling.put(0, this.categorys.get(this.currentList));
									for (int i = 1; i < 4 ; i++) { this.scrolling.put(i, GuiQuestLog.ql.get(3)); }
									this.scrolling.put(4, this.questLists.get(0));
									if (this.scrolling.get(4)==null) { this.scrolling.put(4, GuiQuestLog.ql.get(3)); }
									this.currentList = 0;
									this.aType = 1;
									this.tick = 5;
									this.mtick = 5;
									break;
								}
							}
						}
						break;
					case 0: // remove active quest
						for (Map<Integer, Quest> map : activeQuests.values()) {
							for (Quest q : map.values()) {
								if (q.id==this.hoverQuestID) {
									GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("drop.quest", new Object[] { new TextComponentTranslation(q.getTitle()).getFormattedText() }).getFormattedText(), new TextComponentTranslation("quest.cancel.info", new Object[0]).getFormattedText(), q.id);
									this.displayGuiScreen((GuiScreen) guiyesno);
									break;
								}
							}
						}
						break;
					case 1: // show quest
						if (ClientProxy.playerData.hud.questID == this.hoverQuestID) { ClientProxy.playerData.hud.questID = -1; }
						else { ClientProxy.playerData.hud.questID = this.hoverQuestID; }
						break;
				}
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) { return; }
		NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestRemoveActive, id);
		PlayerQuestData data = PlayerData.get(this.player).questData;
		if (data!=null) {
			data.activeQuests.remove(id);
			this.initGui();
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
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
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
			this.categorys.clear();
			this.questLists.clear();
			if (!this.activeQuests.isEmpty()) {
				try {
					BufferedImage bufferAdd = ImageIO.read(this.mc.getResourceManager().getResource(GuiQuestLog.ql.get(4)).getInputStream());
					BufferedImage npcCatFrame = new BufferedImage(98, 30, bufferAdd.getType());
					for (int v = 0; v < 30; v++) {
						for (int u = 0; u < 98; u++) {
							int c = bufferAdd.getRGB(u, v + 60);
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
									try { icon = ImageIO.read(this.mc.getResourceManager().getResource(quest.icon).getInputStream()); } catch (Exception e) { }
									if (icon==null) {
										try { icon = ImageIO.read(this.mc.getResourceManager().getResource(new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png")).getInputStream()); } catch (Exception e) { }
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
								quests.clear();
								listCat++;
								pos = 0;
							}
				    	}
					}
					if (GuiQuestLog.activeQuest!=null) {
						this.questLists.put(0, GuiQuestLog.ql.get(3));
						if (GuiQuestLog.activeQuest.q.completion==EnumQuestCompletion.Npc) {
							BufferedImage buffer = ImageIO.read(this.mc.getResourceManager().getResource(GuiQuestLog.ql.get(3)).getInputStream());
							BufferedImage npcQuesFrame = new BufferedImage(56, 43, bufferAdd.getType());
							for (int v = 0; v < 43; v++) {
								for (int u = 0; u < 56; u++) {
									int c = bufferAdd.getRGB(u, v);
									if (c==16777215) { continue; }
									npcQuesFrame.setRGB(u, v, c);
								}
							}
							Graphics g = buffer.getGraphics();
							g.drawImage(npcQuesFrame, 46, 44, 56, 43, null);
							ResourceLocation rl = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/quest_list.png");
							TempTexture texture = new TempTexture(rl, buffer);
							this.mc.getTextureManager().loadTexture(rl, texture);
							Map<ResourceLocation, ITextureObject> mapTextureObjects = ObfuscationHelper.getValue(TextureManager.class, (TextureManager) this.mc.getTextureManager(), Map.class);
							if (!mapTextureObjects.containsKey(rl)) {
								texture.loadTexture(this.mc.getResourceManager());
								mapTextureObjects.put(rl, texture);
							}
							this.questLists.put(0, rl);
						}
						if (!GuiQuestLog.activeQuest.map.isEmpty()) {
							String key = GuiQuestLog.activeQuest.map.keySet().toArray(new String[1])[0];
							int totalLists = (int) Math.ceil((double) GuiQuestLog.activeQuest.map.get(key).size() / 2.0d);
							for (int l = 1; l < totalLists; l++) {
								this.questLists.put(l, GuiQuestLog.ql.get(3));
							}
						}
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
								MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.down", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.75f + 0.25f * this.rnd.nextFloat());
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
					if (this.isStart) { this.drawBase(uC, vC); }
					else if (this.step>0) {
						this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(1));
						this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
					}
					ResourceLocation next = this.scrolling.get(this.step+1);
					ResourceLocation nowl = this.scrolling.get(this.step);
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
					if (tick==mtick) { MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat()); }
					if (tick==0) {
						step++;
						tick = mtick;
						if (step > this.scrolling.size() - 2) {
							step = 0;
							if (this.isStart) {
								aType = -1;
								tick = -1;
								mtick = -1;
								this.initGui();
							} else {
								aType = 2;
								tick = 10;
								mtick = 10;
							}
							this.isStart = true;
							this.scrolling.clear();
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
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
					if (h>0) {
						this.drawTexturedModalRect(-125, 0 - h, 56, 0, 28, h);
						this.drawTexturedModalRect(-93, 0 - h, 56, 0, 28, h);
						if (CustomNpcs.showQuestCompass) {
							this.drawTexturedModalRect(99, 0 - h, 56, 0, 28, h);
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
				case 3: { // sheets to right - 5 ticks
					GlStateManager.pushMatrix();
					GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(0));
					this.drawTexturedModalRect(-127, 0, 0, 37, 256, 175);
					this.drawBase(uC, vC);
					ResourceLocation next = this.scrolling.get(this.step+1);
					ResourceLocation nowl = this.scrolling.get(this.step);
					if (next==null || nowl==null) {
						GlStateManager.popMatrix();
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
						return;
					}
					this.mc.renderEngine.bindTexture(next); // left
					this.drawTexturedModalRect(-127, 0, 0, 37, 127, 175);
					this.mc.renderEngine.bindTexture(nowl); // right
					this.drawTexturedModalRect(0, 0, 127, 37, 129, 175);
					GlStateManager.popMatrix();
					boolean up = (tick > mtick / 2);
					part = (up ? (float) tick - (float) mtick / 2.0f: (float) tick) + pTicks;
					cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 90.0d);
					GlStateManager.pushMatrix();
					double h = up ? 174.0f : 175.0d, p = 0.00390625d;
					double tx = up ? 0.0d : 127.0d, ty = 37.0d;
					double w = (up ? (1 - cos) * 127.0d : cos * 130.0d);
					double uH = up ? 0.0d : (part / (double) mtick * 2.0d) * -30.0d;
					double dH = up ? 0.0d : (part / (double) mtick * 2.0d) * 30.0d;
					double suH = up ? (((double) mtick - part) / (double) mtick * 2.0d) * 30.0d : 0.0d;
					double sdH = up ? (((double) mtick - part) / (double) mtick * 2.0d) * -30.0d : 0.0d;
					GlStateManager.translate(uC/this.scale - (up ? w : 0.0d), vC/this.scale, 0.0f);
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
					if (tick==mtick) { MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat()); }
					if (tick==0) {
						step++;
						tick = mtick;
						if (step > this.scrolling.size() - 2) {
							step = 0;
							mtick = 200;
							aType = -1;
							tick = -1;
							mtick = -1;
							this.initGui();
							this.scrolling.clear();
						}
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
						return;
					}
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
			this.drawBase(uC, vC);
			GlStateManager.popMatrix();
		}
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		this.hoverQuestID = -1;
		this.hoverButton = -1;
		if (this.aType<0 && !this.activeQuests.isEmpty()) {
			if (this.listType) {
				
			} else {
				if (this.currentQuestList == null) { return; }
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				for (int p : this.currentQuestList.quests.keySet()) {
					Quest q = this.currentQuestList.quests.get(p);
					Integer[] ps = this.currentQuestList.poses.get(p);

					int u = (int) ((float) ps[0] - 20.5f* this.scale);
					int v = (int) ((float) ps[1] - 1.5f* this.scale);
					int w = (int) ((float) ps[0] + (float) ps[2] + 4.5f * this.scale);
					int h = (int) ((float) ps[1] + (float) ps[3] + 2.0f * this.scale);
					if (this.isMouseHover(mouseX, mouseY, u, v, w-u, h-v)) {
						this.hoverQuestID = q.id;
						String progress = "";
						if (q.questInterface.isCompleted(this.mc.player)) { progress = new TextComponentTranslation("questlog.completed").getFormattedText().replace(": ", ""); }
						else {
							IPlayer<?> pl = (IPlayer<?>) NpcAPI.Instance().getIEntity(this.mc.player);
							int i = 0;
							for (IQuestObjective task : q.getObjectives(pl)) {
								if (task.isCompleted()) { i++; }
							}
							progress = i + " / " + q.questInterface.tasks.length;
						}
						this.hoverText = new String[] {
								new TextComponentTranslation(q.category.title).getFormattedText(),
								new TextComponentTranslation("gui.quest", ": " + q.getTitle()).getFormattedText(),
								new TextComponentTranslation("gui.progress", ": " + progress).getFormattedText()
						};
						this.drawGradientRect(u, v, w, h, 0x2000FF00, 0x40FF0000);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						if (q.cancelable) {
							u = (int) ((float) ps[0] - 18.0f* this.scale);
							v = (int) ((float) ps[1] + 18.0f* this.scale);
							w = (int) ((float) u + 9.0f * this.scale);
							h = (int) ((float) v + 9.0f * this.scale);
							if (this.isMouseHover(mouseX, mouseY, u, v, w-u, h-v)) {
								this.hoverButton = 0;
								this.hoverText = new String[] { new TextComponentTranslation("drop.quest", q.getName()).getFormattedText() };
							}
						}
						if (CustomNpcs.showQuestCompass && q.hasCompassSettings()) {
							u = (int) ((float) ps[0] - 9.0f* this.scale);
							v = (int) ((float) ps[1] + 18.0f* this.scale);
							w = (int) ((float) u + 9.0f * this.scale);
							h = (int) ((float) v + 9.0f * this.scale);
							if (this.isMouseHover(mouseX, mouseY, u, v, w-u, h-v)) {
								this.hoverButton = 1;
								this.hoverText = new String[] { new TextComponentTranslation("quest.hover.compass."+(ClientProxy.playerData.hud.questID==q.id)).getFormattedText() };
							}
						}
					}
					if (GuiQuestLog.activeQuest!=null && GuiQuestLog.activeQuest.q.id==q.id) {
						this.drawGradientRect(u, v, w, h, 0x20FF0000, 0x400000FF);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					}
					
					GlStateManager.pushMatrix();
					GlStateManager.translate((float) ps[0] - 19.0f * this.scale, (float) ps[1] + 17.0f * this.scale, 0.0f);
					GlStateManager.scale(this.scale, this.scale, 1.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
					if (q.cancelable) {
						this.drawTexturedModalRect(0, 0, 98, 60 + (this.hoverQuestID==q.id && this.hoverButton==0 ? 9 : 0), 9, 9);
					}
					if (CustomNpcs.showQuestCompass && q.hasCompassSettings()) {
						this.drawTexturedModalRect(9, 0, ClientProxy.playerData.hud.questID==q.id ? 107 : 116, this.hoverQuestID==q.id && this.hoverButton==1 ? 69 : 60, 9, 9);
					}
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
					this.drawString(q.getName(), ps[0], ps[1], ps[2], CustomNpcs.questLogColor);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					GlStateManager.popMatrix();
				}
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
		if (this.tick<0) {
			int u0 = (int) ((float) uC - 101.0f * this.scale);
			int u1 = (int) ((float) uC + 7.0f * this.scale);
			int v = (int) ((float) vC + 10.0f * this.scale);
			if (this.listType) {
				int vc = GuiQuestLog.activeQuest.npc!=null ? (int) (43.0f * this.scale) : 0;
				if (GuiQuestLog.activeQuest.q.texture!=null) {
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					float scale = (64.0f / 256.0f);
					GlStateManager.rotate(5.0f, 0.0f, 0.0f, 1.0f);
					GlStateManager.scale(this.scale, this.scale, 1.0f);
					GlStateManager.translate(112 + (uC)/this.scale, vC/this.scale - 16, 100.0f);
					GlStateManager.scale(scale, scale, 1.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(5));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.scale(0.9f, 0.9f, 1.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.activeQuest.q.texture);
					this.drawTexturedModalRect(16, 16, 0, 0, 256, 256);
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
				if (this.currentList==0 && GuiQuestLog.activeQuest.npc!=null) {
					float size = 0.6f / GuiQuestLog.activeQuest.npc.width;
					int vH = (int) (1.9f - 4.6f * Math.pow(this.scale, 3));
					vH = (int) ((float) vH * (-13.5f * size + 14.5f));
					this.drawNpc(GuiQuestLog.activeQuest.npc, (int) (-52.5f * this.scale), vH, size, 30, 15, false);
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					GlStateManager.scale(this.scale, this.scale, 1.0f);
					GlStateManager.translate(uC/this.scale, vC/this.scale, 100.0f);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(3));
					this.drawTexturedModalRect(-127, -1, 0, 37, 64, 16);
					this.drawTexturedModalRect(-63, -1, 64, 37, 64, 14);
					this.drawTexturedModalRect(-127, 15, 0, 53, 52, 29);
					this.drawTexturedModalRect(-33, 13, 94, 53, 34, 31);
					this.drawTexturedModalRect(-127, 44, 0, 82, 128, 130);
					this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
					this.drawTexturedModalRect(-81, 8, 0, 0, 56, 43);
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(0.0f, 0.0f, 101.0f);
				String key = ((int) (98.0f * this.scale))+"_"+((int) (150.0f * this.scale))+"_"+vc;
				if ((GuiQuestLog.activeQuest.map.isEmpty() || !GuiQuestLog.activeQuest.map.containsKey(key)) &&
						(!GuiQuestLog.activeQuest.q.logText.isEmpty() ||
						!GuiQuestLog.activeQuest.q.rewardText.isEmpty() ||
						GuiQuestLog.activeQuest.q.rewardExp>0 ||
						GuiQuestLog.activeQuest.q.completion==EnumQuestCompletion.Npc)) {
					GuiQuestLog.activeQuest.getText((int) (98.0f * this.scale), (int) (150.0f * this.scale), vc, this.player, this.fontRenderer);
					this.initGui();
				}
				Map<Integer, List<String>> texts = GuiQuestLog.activeQuest.getText((int) (98.0f * this.scale), (int) (150.0f * this.scale), vc, this.player, this.fontRenderer);
				if (texts.containsKey(this.currentList * 2)) {
					int d = 0;
					for (String str : texts.get(this.currentList * 2)) {
						this.mc.fontRenderer.drawString(str, u0, v + (this.currentList==0 && GuiQuestLog.activeQuest.npc!=null ? vc : 0) + d * 10, CustomNpcs.questLogColor);
						d++;
					}
					this.mc.fontRenderer.drawString(""+((this.currentList * 2) + 1), (int) ((float) u0 + 5.0f * this.scale), (int) ((float) v + 151.0f * this.scale), CustomNpcs.notEnableColor);
				}
				if (texts.containsKey((this.currentList * 2) + 1)) {
					int d = 0;
					for (String str : texts.get((this.currentList * 2) + 1)) {
						this.mc.fontRenderer.drawString(str, u1, v + d * 10, CustomNpcs.questLogColor);
						d++;
					}
				}
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			if (this.totalActiveQuest>0) {
				this.mc.fontRenderer.drawString(""+((this.currentList * 2) + 1), (int) ((float) u0 + 5.0f * this.scale), (int) ((float) v + 151.0f * this.scale), CustomNpcs.notEnableColor);
				this.mc.fontRenderer.drawString(""+((this.currentList * 2) + 2), (int) ((float) u1 + 93.0f * this.scale), (int) ((float) v + 151.0f * this.scale), CustomNpcs.notEnableColor);
			}
			for (int id : this.buttons.keySet()) {
				Integer[] b = this.buttons.get(id);
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				if (this.isMouseHover(mouseX, mouseY, b[0], b[1], b[2], b[3])) {
					this.hoverButton = id;
				}
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			Integer[] p = this.buttons.get(5);
			if (p!=null) { // to right
				if (this.isMouseHover(mouseX, mouseY, p[0], p[1], 23, 13)) { this.hoverButton = 5; }
				this.mc.renderEngine.bindTexture(GuiQuestLog.bookGuiTextures);
				this.drawTexturedModalRect(p[0], p[1], 1 + (this.hoverButton==5 ? 23 : 0), 192, 23, 13);
			}
			p = this.buttons.get(6);
			if (p!=null) { // to left
				if (this.isMouseHover(mouseX, mouseY, p[0], p[1], 23, 13)) { this.hoverButton = 6; }
				this.mc.renderEngine.bindTexture(GuiQuestLog.bookGuiTextures);
				this.drawTexturedModalRect(p[0], p[1], 1 + (this.hoverButton==6 ? 23 : 0), 205, 23, 13);
			}
		}
		if (this.hoverText!=null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void drawBase(float uC, float vC) {
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
			this.drawString(text, 2, 50, 105.0f * this.scale, CustomNpcs.questLogColor);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate(uC/this.scale, vC/this.scale, 0.0f);
		}
		this.mc.renderEngine.bindTexture(GuiQuestLog.ql.get(4));
		this.drawTexturedModalRect(-125, -30, 56, this.hoverButton==0 ? 30 : 0, 28, 30);
		
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
		this.drawTexturedModalRect(-93, -30, 56, this.hoverButton==1 ? 30 : 0, 28, 30);

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
			this.drawTexturedModalRect(99, -30, 56, this.hoverButton==2 ? 30 : 0, 28, 30);
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
			int w = 31, qw = 33;
			if (this.listType) {
				qw = 53;
				if (this.scrolling.size()>2 && this.aType!=0) {
					float s = 26.0f / (float) (this.scrolling.size()-1);
					float o = 20.0f / (float) (this.scrolling.size()-1);
					int p = (int) ((s * (float) (this.mtick - this.tick) / (float) this.mtick) + (float) this.step * s);
					int d = (int) ((o * (float) (this.mtick - this.tick) / (float) this.mtick) + (float) this.step * o);
					w = this.aType==3 ? 31 + p: 57 - p;
					qw = this.aType==3 ? 53 - d : 33 + d;
				}
				this.drawTexturedModalRect(-154, 10, 137, this.hoverButton==3 ? 27 : 0, w, 27); // cat tab
				if (GuiQuestLog.activeQuest!=null) {
					if (this.currentList == 0) {
						this.drawTexturedModalRect(157 - qw, 138, 137 - qw, 0, qw, 22);
					} else {
						for (int i = 44; i > 0; i--) {
							this.drawTexturedModalRect(i - 152, 138, 137 - i, 0, 1, 22);
						}
					}
				}
			} else {
				if (this.categorys.size()>1) {
					float a = -26.0f / (float) (this.categorys.size()-1), b = 26.0f;
					w = (int) (31.0f + this.currentList * a + b);
				}
				if (this.scrolling.size()>2 && this.aType!=0) {
					float s = 26.0f / (float) (this.scrolling.size()-1);
					float o = 20.0f / (float) (this.scrolling.size()-1);
					int p = (int) ((s * (float) (this.mtick - this.tick) / (float) this.mtick) + (float) this.step * s);
					int d = (int) ((o * (float) (this.mtick - this.tick) / (float) this.mtick) + (float) this.step * o);
					w = this.aType==3 ? 31 + p : 57 - p;
					qw = this.aType==3 ? 53 - d : 33 + d;
				}
				this.drawTexturedModalRect(-154, 10, 137, 0, w, 27); // cat tab
				if (GuiQuestLog.activeQuest!=null) {
					this.drawTexturedModalRect(157 - qw, 138, 137 - qw, 0, qw, 22);
				}
			}
		}
	}

	private void drawString(String text, int x, int y, float size, int color) {
		text = AdditionalMethods.instance.deleteColor(text);
		if (this.mc.fontRenderer.getStringWidth(text) > size) {
			String temp = "";
			int h = 0;
			for (int c = 0; c < text.length(); c++) {
				char ch = text.charAt(c);
				if (this.mc.fontRenderer.getStringWidth(temp + ch)>=size || c==text.length()-1) {
					if (temp.lastIndexOf(" ")!=-1) {
						if (c==text.length()-1) {
							this.mc.fontRenderer.drawString(temp+""+ch, x, y + h * 10, CustomNpcs.questLogColor);
							break;
						} else {
							this.mc.fontRenderer.drawString(temp.substring(0, temp.lastIndexOf(" ")), x, y + h * 10, CustomNpcs.questLogColor);
							temp = temp.substring(temp.lastIndexOf(" ")+1)+""+ch;
						}
					}
					else {
						if (c==text.length()-1) { temp += ch; break; }
						this.mc.fontRenderer.drawString(temp, x, y, CustomNpcs.questLogColor);
						temp = ""+ch;
					}
					h++;
				} else {
					temp += ch;
				}
			}
		}
		else { this.mc.fontRenderer.drawString(text, x, y, CustomNpcs.questLogColor); }
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
		if (this.hoverQuestID>-1) { this.buttonPress(7); }
		else if (this.hoverButton>-1) { this.buttonPress(this.hoverButton); }
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
				if (q.id == GuiQuestLog.activeQuest.q.id) { return; }
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
		
		public final Quest q;
		private EntityNPCInterface npc;
		private Map<String, Map<Integer, List<String>>> map; // [key, data texts]
		
		public QuestInfo(Quest q) {
			this.q = q;
			this.npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), q.world);
			this.map = Maps.<String, Map<Integer, List<String>>>newHashMap();
			if (q.completer!=null) {
				NBTTagCompound compound = new NBTTagCompound();
				q.completer.writeToNBTOptional(compound);
				compound.setUniqueId("UUID", UUID.randomUUID());
				Entity e = EntityList.createEntityFromNBT(compound, q.completer.world);
				if (e instanceof EntityNPCInterface) { this.npc = (EntityNPCInterface) e; }
				else { this.npc.readEntityFromNBT(compound); }
			}
			else { this.q.completer = this.npc; }
			MarkData data = MarkData.get(this.npc);
			if(data!=null) { data.marks.clear(); }
			this.npc.display.setShowName(1);
			this.npc.animation.clear();
		}

		public Map<Integer, List<String>> getText(int width, int height, int first, EntityPlayer player, FontRenderer fontRenderer) { // [listID/2, lable texts]
			String key = width+"_"+height+"_"+first;
			if (this.map.containsKey(key)) { return this.map.get(key); }
			this.map.clear();
			String ent = ""+((char) 10);
			String text = ((char) 167)+"l"+new TextComponentTranslation(this.q.category.title).getFormattedText() + ent;
			if (this.q.completion==EnumQuestCompletion.Npc && this.q.completer!=null) {
				text += new TextComponentTranslation("quest.completewith", ((char) 167)+"l"+this.q.completer.getName()).getFormattedText() + ent;
			}
			IQuestObjective[] allObj = this.q.getObjectives(player);
			if (allObj.length>0) {
				String colorR = ((char) 167) + "c";
				String colorG = ((char) 167) + "a";
				String color;
				int pos = 0;
				text +=  ent + ((char) 167) + "l" + new TextComponentTranslation("quest.objectives."+this.q.step).getFormattedText() + ent;
				for (int i = 0; i < allObj.length; i++) {
					if (this.q.step == 1) { // ones
						if (allObj[i].isCompleted() && i == pos) {
							color = colorG;
							pos++;
						}
						else { color = colorR; }
					}
					else { color = allObj[i].isCompleted() ? colorG : colorR; } // or || all
					text += color + (i + 1) + ((char) 167) + "r-" + allObj[i].getText() + ent;
				}
				text = text.substring(0, text.length()-1);
			}
			text += this.q.getLogText();
			Map<Integer, List<String>> texts = Maps.<Integer, List<String>>newTreeMap();
			List<String> lines = Lists.<String>newArrayList();
			int curentList = 0;
			String line = "";
			text = text.replace("\n", " \n ");
			text = text.replace("\r", " \r ");
			String[] words = text.split(" ");
			String color = ((char) 167) + "r";
			for (String word : words) {
				Label_0236: {
					if (!word.isEmpty()) {
						if (word.length() == 1) {
							char c = word.charAt(0);
							if (c == '\r' || c == '\n') {
								lines.add(color + line);
								color = AdditionalMethods.getLastColor(color, line);
								line = "";
								break Label_0236;
							}
						}
						String newLine;
						if (line.isEmpty()) { newLine = word; } else { newLine = line + " " + word; }
						if (fontRenderer.getStringWidth(newLine) > width) {
							lines.add(color + line);
							color = AdditionalMethods.getLastColor(color, line);
							line = word.trim();
						} else {
							line = newLine;
						}
					}
				}
			}
			if (!line.isEmpty()) {
				lines.add(color + line);
			}
			List<String> list = Lists.<String>newArrayList();
			for (String l : lines) {
				if ((list.size() * 10) > height - (curentList==0 ? first : 0)) {
					texts.put(curentList, list);
					list = Lists.<String>newArrayList();
					curentList++;
				}
				list.add(l);
			}
			if (!list.isEmpty()) { texts.put(curentList, list); }
			this.map.put(key, texts);
			return texts;
		}
		
	}
	
}
