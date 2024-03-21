package noppes.npcs.client.gui.player;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerCompassHUDData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.AdditionalMethods;

public class GuiLog
extends GuiNPCInterface
implements GuiYesNoCallback, IGuiData, ISliderListener, ITextfieldListener {
	
	private static final Map<Integer, ResourceLocation> ql = Maps.<Integer, ResourceLocation>newTreeMap();
	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	private static ResourceLocation killIcon = new ResourceLocation("textures/entity/skeleton/skeleton.png");
	
	static {
		GuiLog.ql.clear();
		for (int i = 0; i < 6; i++) {
			GuiLog.ql.put(i, new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_"+i+".png"));
		}
	}
	
	private int hoverButton, hoverQuestId, catRow, catSelect, page;
	private int type; // -1-inv; 0-faction; 1-quests; 2-compass
	private int step, tick, mtick, temp, guiTopLog, guiCenter;
	private boolean toPrePage = true;
	private final Random rnd = new Random();
	private ScaledResolution sw;
	
	/* 0-tab inv; 1-tab factions; 2-tab quests; 3-tab compass
	 * 4-page right; 5-page left
	 * 6-quest; 7/14-tab categories
	 * 16-pre cat list; 17-next cat list
	 * 20/28-cat list
	 * [] = [x, y, w, h]
	 */
	private final Map<Integer, int[]> buttons = Maps.<Integer, int[]>newTreeMap();
	private final Map<String, Map<Integer, QuestData>> quests = Maps.<String, Map<Integer, QuestData>>newTreeMap(); // { category, [id, quest]}
	private final Map<String, Color> categories = Maps.<String, Color>newTreeMap(); // [name, color]
	private final List<Faction> playerFactions = Lists.<Faction>newArrayList();
	private final PlayerCompassHUDData compassData;
	public static QuestInfo activeQuest;

	public GuiLog(int t) {
		super();
		type = t;
		temp = 0;
		tick = 15;
		mtick = 15;
		step = 0;
		closeOnEsc = true;
		
		xSize = 0;
		ySize = 0;
		width = 0;
		height = 0;
		hoverButton = -1;
		hoverQuestId = 0;
		catRow = 0;
		catSelect = 0;
		page = 0;
		sw = new ScaledResolution(this.mc);
		compassData = PlayerData.get(player).hud.compassData;
		activeQuest = null;
		if (t == 1) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FactionsGet);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(this.mc);
		guiCenter = (int) Math.ceil(sw.getScaledWidth_double() / 2.0d);
		double centerH = Math.ceil(sw.getScaledHeight_double() / 2.0d);
		width = 256;
		height = 203;
		guiLeft = guiCenter - 128;
		guiTop = (int) (centerH - 101.5d);
		guiTopLog = guiTop + 28;
		// Buttons
		// Tabs
		buttons.clear();
		buttons.put(0, new int[] { guiLeft + 10, guiTop, 28, 28 }); // tab inv
		buttons.put(1, new int[] { guiLeft + 43, guiTop, 28, type == 1 ? 30 : 28 }); // tab factions
		buttons.put(2, new int[] { guiLeft + 76, guiTop, 28, type == 0 ? 30 : 28 }); // tab quests
		buttons.put(3, new int[] { guiLeft + 218, guiTop, 28, type == 2 ? 30 : 28 }); // tab compass
		
		// Quests
		if (type==0) {
			quests.clear();
			categories.clear();
			Collection<QuestData> list = PlayerData.get(player).questData.activeQuests.values();
			// Quest List
			if (!list.isEmpty()) {
		    	for (QuestData qd : list) {
		    		Quest quest = qd.quest;
		    		String catName = quest.category.getName();
		    		if (!categories.containsKey(catName)) {
		    			int r = 128, g = 32, b = 224;
		    			for (int i = 0; i < catName.length(); i++) {
		    				switch(i%3) {
			    				case 0: r += catName.charAt(i); break;
			    				case 1: g += catName.charAt(i); break;
			    				case 2: b += catName.charAt(i); break;
			    			}
		    			}
		    			this.categories.put(catName, new Color((r * catName.length()) % 256, (g * catName.length()) % 256, (b * catName.length()) % 256));
		    		}
		    		if (!quests.containsKey(catName)) { quests.put(catName, Maps.<Integer, QuestData>newTreeMap()); }
		    		quests.get(catName).put(quest.id, qd);
		    	}
			}
			if (activeQuest != null) { activeQuest.reset(); }
		}
		if (type==2) {
			int x0 = guiLeft + 32;
			int x1 = guiCenter + 5;
			int y = guiTopLog + 92;
			//Screen Pos
			this.addTextField(new GuiNpcTextField(0, this, fontRenderer, x0, y, 40, 10, ""+this.compassData.screenPos[0]));
			this.getTextField(0).setDoubleNumbersOnly();
			this.getTextField(0).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[0]);
			
			this.addTextField(new GuiNpcTextField(1, this, fontRenderer, x0 + 54, y, 40, 10, ""+this.compassData.screenPos[1]));
			this.getTextField(1).setDoubleNumbersOnly();
			this.getTextField(1).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[1]);
			
			// Scale
			x0 -= 1;
			float v = this.compassData.scale - 0.5f;
			this.addSlider(new GuiNpcSlider(this, 0, x0, y += 17, 96, 12, v));
			this.getSlider(0).setString((""+this.compassData.scale).replace(".", ","));

			this.addButton(new GuiNpcCheckBox(0, x1, y - 1, 100, 12, "quest.screen.show.quest"));
			((GuiNpcCheckBox) this.getButton(0)).setSelected(this.compassData.showQuestName);
			
			// Incline
			v = this.compassData.incline * -0.022222f + 0.5f;
			this.addSlider(new GuiNpcSlider(this, 1, x0, y += 16, 96, 12, v));
			this.getSlider(1).setString((""+(45.0f + this.compassData.incline*-1.0f)).replace(".", ","));

			this.addButton(new GuiNpcCheckBox(1, x1, y - 1, 100, 12, "quest.screen.show.task"));
			((GuiNpcCheckBox) this.getButton(1)).setSelected(this.compassData.showTaskProgress);
			
			// Rotation
			v = this.compassData.rot * 0.016667f + 0.5f;
			this.addSlider(new GuiNpcSlider(this, 2, x0, y += 16, 96, 12, v));
			this.getSlider(2).setString((""+this.compassData.rot).replace(".", ","));
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (type != 2) { return; }
		switch(button.id) {
			case 0: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				this.compassData.showQuestName = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 1: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				this.compassData.showTaskProgress = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
		}
	}
	
	public void buttonPress(int id) {
		if (type == 0 && id > 6 && id < 15) { // quest category rows
			int catList = catRow * 8 + id - 7;
			if (catSelect == catList && page != 0) {
				step = 11;
				tick = 10;
				mtick = 10;
				page = 0;
			}
			if (catSelect != catList || activeQuest != null) {
				step = catSelect > catList || activeQuest != null ? 11 : 10;
				tick = 11;
				mtick = 10;
				catSelect = catList;
				page = 0;
				activeQuest = null;
			}
			return;
		}
		switch(id) {
			case 0: { // inventory
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				mtick = 15;
				step = type + 7;
				type = -1;
				break;
			}
			case 1: { // factions
				if (type == 1) { return; }
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				mtick = 15;
				toPrePage = false;
				step = type + 7;

				page = 0;
				type = 1;
				NoppesUtilPlayer.sendData(EnumPlayerPacket.FactionsGet);
				initGui();
				break;
			}
			case 2: { // quests
				if (type == 0) { return; }
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				mtick = 15;
				toPrePage = type == 1;
				step = type + 7;
				
				catRow = 0;
				catSelect = 0;
				page = 0;
				activeQuest = null;
				type = 0;
				initGui();
				break;
			}
			case 3: { // compass
				if (type == 2 || !CustomNpcs.showQuestCompass) { return; }
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				mtick = 15;
				toPrePage = true;
				step = type + 7;

				page = 0;
				type = 2;
				initGui();
				break;
			}
			case 4: { // page right
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				if (type == 0) {
					if (activeQuest != null) {
						
					} else {
						page++;
						step = 10;
						tick = 10;
						mtick = 10;
					}
				}
				break;
			}
			case 5: { // page left
				if (type == 0) {
					if (activeQuest != null) {
						
					} else {
						page--;
						step = 11;
						tick = 10;
						mtick = 10;
					}
				}
				break;
			}
			case 6: { // quest select
				if (hoverQuestId < 1) { return; }
				String catName = "";
				int i = 0;
				for (String key : categories.keySet()) {
					if (i == catSelect) {
						catName = key;
						break;
					}
					i++;
				}
				if (catName.isEmpty() || !quests.containsKey(catName) || !quests.get(catName).containsKey(hoverQuestId)) { return; }
				activeQuest = new QuestInfo(quests.get(catName).get(hoverQuestId), this.mc.world);
				step = 10;
				tick = 10;
				mtick = 10;
				break;
			}
			case 16: { // pre cat list
				if (type != 0) { return; }
				MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat());
				catRow--;
				break;
			}
			case 17: { // next cat list
				if (type != 0) { return; }
				MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat());
				catRow++;
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) player, this);
		if (!result) { return; }
		NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestRemoveActive, id);
		PlayerQuestData data = PlayerData.get(player).questData;
		if (data!=null) {
			data.activeQuests.remove(id);
			initGui();
		}
	}

	@Override
	public boolean doesGuiPauseGame() { return false; }

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Back
		GlStateManager.pushMatrix();
		drawGradientRect(0, 0, mc.displayWidth, mc.displayHeight, 0xAA000000, 0xAA000000);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
		
		// Animations
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (tick>=0) {
			if (tick==0) { partialTicks = 0.0f; }
			float part = (float) tick + partialTicks;
			float cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 180.0d);
			if (cos < 0.0f) { cos = 0.0f; }
			else if (cos > 1.0f) { cos = 1.0f; }
			switch(step) {
				case 0: { // start open
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(2));
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter + (1.0f - cos) * (guiCenter + 50.0f), guiTopLog + (1.0f - cos) * 250.0f, 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					GlStateManager.popMatrix();
					if (tick==0) {
						step = 1;
						tick = 21;
						mtick = 20;
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.down", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.75f + 0.25f * this.rnd.nextFloat());
						GlStateManager.disableBlend();
					}
					break;
				}
				case 1: { // open
					// right
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					GlStateManager.popMatrix();
					// left
					boolean up = tick >= mtick / 2;
					GlStateManager.pushMatrix();
					if (up) {
						part = (float) (tick - (mtick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(2));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					if (tick==0) {
						step = 2;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 2: { // open lists
					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					if (temp > 0) {
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(1));
						this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					}
					GlStateManager.popMatrix();
					
					// left
					boolean up = tick >= mtick / 2;
					GlStateManager.pushMatrix();
					if (up) {
						part = (float) (tick - (mtick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					
					if (tick==mtick) { MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat()); }
					if (tick==0) {
						if (temp < 3) {
							temp++;
							step = 2;
							tick = 11;
							mtick = 10;
						} else {
							temp = 0;
							step = 3;
							tick = 21;
							mtick = 20;
						}
						GlStateManager.disableBlend();
					}
					break;
				}
				case 3: { // tab open
					// Tabs
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft + 10, guiTop + (1.0f - cos) * 28.0f , 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f , 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f , 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					if (CustomNpcs.showQuestCompass) {
						GlStateManager.translate(142.0f, 0.0f , 0.0f);
						this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					}
					GlStateManager.popMatrix();
					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(1));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					GlStateManager.popMatrix();
					
					if (tick==0) {
						step = type + 4;
						tick = 21;
						mtick = 20;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 4: { // quest tab open
					drawBox(mouseX, mouseY, partialTicks);
					if (!categories.isEmpty()) {
						GlStateManager.pushMatrix();
						GlStateManager.translate(guiLeft, guiTopLog + 7.5f, 0.0f);
						GlStateManager.translate(0.0f, 16.0f, 0.0f);
						int i = 0, p = 0, st = catRow * 8;
						for (String catName : categories.keySet()) {
							if (p < st) { p++; continue; }
							int catW = (int) ((this.fontRenderer.getStringWidth(catName) + 10) * cos);
							this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
							this.drawTexturedModalRect(5 - catW, i * 16, 0, 90 + (catSelect == p ? 0 : 16), catW, 16);
							String name = "";
							for (int j = 0; j < catName.length(); j++) {
								if (this.fontRenderer.getStringWidth(name + catName.charAt(j)) > catW - 5) { break; }
								name += catName.charAt(j);
							}
							this.fontRenderer.drawString(name, 10 - catW, 3 + i * 16, CustomNpcs.questLogColor, false);
							GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
							i++;
							if (i >= 8) { break; }
						}
						GlStateManager.popMatrix();
					}
					else { tick = 0; }
					if (tick==0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 5: { // faction open
					drawBox(mouseX, mouseY, partialTicks);
					if (tick==0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 6: { // compass open
					drawBox(mouseX, mouseY, partialTicks);
					if (tick==0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 7: { // quest tab close
					drawBox(mouseX, mouseY, partialTicks);
					if (!categories.isEmpty()) {
						temp = 1;
						GlStateManager.pushMatrix();
						GlStateManager.translate(guiLeft, guiTopLog + 7.5f, 0.0f);
						GlStateManager.translate(0.0f, 16.0f, 0.0f);
						int i = 0, p = 0, st = catRow * 8;
						for (String catName : categories.keySet()) {
							if (p < st) { p++; continue; }
							int catW = (int) ((this.fontRenderer.getStringWidth(catName) + 10) * (1.0f - cos));
							this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
							this.drawTexturedModalRect(5 - catW, i * 16, 0, 90 + (catSelect == p ? 0 : 16), catW, 16);
							String name = "";
							for (int j = 0; j < catName.length(); j++) {
								if (this.fontRenderer.getStringWidth(name + catName.charAt(j)) > catW - 5) { break; }
								name += catName.charAt(j);
							}
							this.fontRenderer.drawString(name, 10 - catW, 3 + i * 16, CustomNpcs.questLogColor, false);
							GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
							i++;
							if (i >= 8) { break; }
						}
						GlStateManager.popMatrix();
					}
					else {
						tick = 0;
						temp = 0;
					}
					
					if (tick==0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
							tick = 21;
							mtick = 20;
						} else {
							step = type + 4;
							tick = 21;
							mtick = 20;
						}
					}
					break;
				}
				case 8: { // faction close
					drawBox(mouseX, mouseY, partialTicks);

					if (tick==0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
							tick = 21;
							mtick = 20;
						} else {
							step = type + 4;
							tick = 21;
							mtick = 20;
						}
					}
					break;
				}
				case 9: { // compass close
					drawBox(mouseX, mouseY, partialTicks);

					if (tick==0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
							tick = 21;
							mtick = 20;
						} else {
							step = type + 4;
							tick = 21;
							mtick = 20;
						}
					}
					break;
				}
				case 10: { // next page
					drawBox(mouseX, mouseY, partialTicks);
					boolean up = tick >= mtick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					if (up) {
						part = (float) (tick - (mtick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					}
					else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					
					if (tick==mtick) { MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat()); }
					if (tick==0) {
						step = -1;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 11: { // pre page
					drawBox(mouseX, mouseY, partialTicks);
					boolean up = tick >= mtick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					if (up) {
						part = (float) (tick - (mtick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter - (1.0f - cos) * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale((1.0f - cos), 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					
					if (tick==mtick) { MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.sheet", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.8f + 0.4f * this.rnd.nextFloat()); }
					if (tick==0) {
						step = -1;
						tick = 11;
						mtick = 10;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 12: { // close tabs
					// Tabs
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft + 10, guiTop + cos * 28.0f , 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f , 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f , 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					if (CustomNpcs.showQuestCompass) {
						GlStateManager.translate(142.0f, 0.0f , 0.0f);
						this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					}
					GlStateManager.popMatrix();
					
					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(1));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					GlStateManager.popMatrix();
					if (tick==0) {
						step = 13;
						tick = 21;
						mtick = 20;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 13: { // close book
					// left
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f * cos, guiTopLog, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					GlStateManager.popMatrix();
					
					// right
					boolean up = tick >= mtick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f * cos, guiTopLog, 0.0f);
					if (up) {
						part = (float) (tick - (mtick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.translate(-128.0f * (1.0d - cos), 0.0f, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
						if (temp > 0) {
							this.mc.renderEngine.bindTexture(GuiLog.ql.get(1));
							this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
						}
					}
					else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) mtick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) { cos = 0.0f; }
						else if (cos > 1.0f) { cos = 1.0f; }
						GlStateManager.scale(cos, 1.0f, 1.0f);
						this.mc.renderEngine.bindTexture(GuiLog.ql.get(2));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					
					if (tick==0) {
						step = 14;
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID+":book.down", (float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f, 0.75f + 0.25f * this.rnd.nextFloat());
						tick = 21;
						mtick = 20;
						GlStateManager.disableBlend();
					}
					break;
				}
				case 14: { // close
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f + cos * (guiCenter + 50.0f), guiTopLog + cos * 250.0f, 0.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(2));
					this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					GlStateManager.popMatrix();
					if (tick==0) {
						save();
						if (type == -1) { mc.displayGuiScreen(new GuiInventory(player)); }
						else {
							mc.displayGuiScreen(null);
							mc.setIngameFocus();
						}
						GlStateManager.disableBlend();
					}
					break;
				}
			}
			tick--;
			if (step != -1) {
				GlStateManager.popMatrix();
				return;
			}
		}
		drawBox(mouseX, mouseY, partialTicks);
		GlStateManager.popMatrix();
		
		if (tick < 0 && step == -1) {
			GlStateManager.pushMatrix();
			RenderHelper.disableStandardItemLighting();
			super.drawScreen(mouseX, mouseY, partialTicks);
			GlStateManager.popMatrix();
		}
		if (type == 2) {
			if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.upos").getFormattedText());
			} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.vpos").getFormattedText());
			} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.showname").getFormattedText());
			} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.showtask").getFormattedText());
			} else if (this.getSlider(0) != null && this.getSlider(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.scale").getFormattedText());
			} else if (this.getSlider(1) != null && this.getSlider(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.incline").getFormattedText());
			} else if (this.getSlider(2) != null && this.getSlider(2).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.edit.rotation").getFormattedText());
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void drawBox(int mouseX, int mouseY, float partialTicks) {
		hoverButton = -1;
		hoverQuestId = 0;
		for (Integer id : buttons.keySet()) {
			int[] bd = buttons.get(id);
			if (isMouseHover(mouseX, mouseY, bd[0], bd[1], bd[2], bd[3])) {
				hoverButton = id;
				break;
			}
		}
		// tabs
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 10, guiTop, 0.0f);
		boolean offset = false;
		for (int i = 0; i < (CustomNpcs.showQuestCompass ? 4 : 3); i++) {
			boolean hover = (hoverButton == i);
			switch(i) {
				case 1: {
					if (offset) { GlStateManager.translate(0.0f, 0.0f, -1.0f); }
					offset = (type == 1);
					GlStateManager.translate(33.0f, 0.0f, offset ? 1.0f : 0.0f);
					break;
				}
				case 2: {
					if (offset) { GlStateManager.translate(0.0f, 0.0f, -1.0f); }
					offset = (type == 0);
					GlStateManager.translate(33.0f, 0.0f, offset ? 1.0f : 0.0f);
					break;
				}
				case 3: {
					if (offset) { GlStateManager.translate(0.0f, 0.0f, -1.0f); }
					offset = (type == 2);
					GlStateManager.translate(142.0f, 0.0f, offset ? 1.0f : 0.0f);
					break;
				}
				default: break;
			}
			this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
			this.drawTexturedModalRect(0, 0, 0, hover || offset ? 30 : 60, 28, 30);
			
			GlStateManager.pushMatrix();
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.translate(6.0f, 8.0f, 0.0f);
			zLevel = 100.0f;
			itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			ItemStack stack;
			switch(i) {
				case 1:{
					stack = new ItemStack(Items.BANNER, 1, 1);
					break;
				}
				case 2:{
					stack = new ItemStack(Items.BOOK);
					break;
				}
				case 3:{
					stack = new ItemStack(Items.COMPASS);
					break;
				}
				default: stack = new ItemStack(Blocks.CRAFTING_TABLE); break;
			}
			this.itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
			this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, (String) null);
			GlStateManager.disableLighting();
			itemRender.zLevel = 0.0f;
			zLevel = 0.0f;
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
		
		// place
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
		this.mc.renderEngine.bindTexture(GuiLog.ql.get(0));
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
		this.mc.renderEngine.bindTexture(GuiLog.ql.get(1));
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
		GlStateManager.popMatrix();
		
		if (step == -1 && (type == 0 || type == 1)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 28, guiTopLog + 163, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			fontRenderer.drawString(""+(page * 2 + 1), 0, 0, CustomNpcs.notEnableColor);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 228, guiTopLog + 163, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			fontRenderer.drawString(""+(page * 2 + 2), 0, 0, CustomNpcs.notEnableColor);
			GlStateManager.popMatrix();
		}
		if (step >= 0 && step < 10) { return; }
		if (type == 0) { this.drawQuestLog(mouseX, mouseY, partialTicks); }
		else if (type == 1) { this.drawFaction(mouseX, mouseY, partialTicks); }
		else if (type == 2) { this.drawCommpass(mouseX, mouseY, partialTicks); }
	}

	private void drawQuestLog(int mouseX, int mouseY, float partialTicks) {
		if (categories.isEmpty()) {
			String noFaction = new TextComponentTranslation("quest.noquests").getFormattedText();
			fontRenderer.drawSplitString(noFaction, guiLeft + 24, guiTop + 36, 98, CustomNpcs.questLogColor);
			return;
		}
		List<String> hover = Lists.<String>newArrayList();
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTopLog + 23.5f, 0.0f);
		this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
		if (catRow > 0) { // pre Cats
			if (isMouseHover(mouseX, mouseY, guiLeft -17, (int) (guiTopLog + 7.5f), 18, 16)) { hoverButton = 16; } // pre cat list; 
			this.drawTexturedModalRect(-17, -16, 111, hoverButton == 16 ? 30 : 46, 18, 16);
		}
		if (categories.size() - (catRow + 1) * 8 > 0) { // next Cats
			if (isMouseHover(mouseX, mouseY, guiLeft -17, (int) (guiTopLog + 151.5f), 18, 16)) { hoverButton = 17; } // next cat list; 
			this.drawTexturedModalRect(-17, 128, 129, hoverButton == 17 ? 30 : 46, 18, 16);
		}
		int i = 0, p = 0, st = catRow * 8;
		String selectCat = "";
		for (String catName : categories.keySet()) {
			if (p < st) {
				if (catSelect == p && step < 0) { selectCat = catName; }
				p++;
				continue;
			}
			int catW = (int) (this.fontRenderer.getStringWidth(catName) + 10);
			this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
			if (isMouseHover(mouseX, mouseY, guiLeft + 5 - catW, (int) (guiTopLog + 23.5f + i * 16.0f), catW, 16)) { hoverButton = 7 + i; } // 7/15-tab categories; 
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(4 - catW + i, i * 16, 0, 90 + (catSelect == p || hoverButton == 7 + i ? 0 : 16), catW, 16);
			if (catSelect == p && step < 0) {
				selectCat = catName;
				this.drawTexturedModalRect(3 + i, i * 16, 234 + i, 90, 22 - i, 16);
			}
			String name = "";
			for (int j = 0; j < catName.length(); j++) {
				if (this.fontRenderer.getStringWidth(name + catName.charAt(j)) > catW - 5) { break; }
				name += catName.charAt(j);
			}
			this.fontRenderer.drawString(name, 10 - catW, 3 + i * 16, CustomNpcs.questLogColor, false);
			i++;
			p++;
			if (i >= 8) { break; }
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
		
		if (step != -1) { return; }
		if (activeQuest != null) {
			int first = 0;
			if (activeQuest.qData.quest.completion==EnumQuestCompletion.Npc && activeQuest.npc != null) {
				if (page == 0) {
					GlStateManager.pushMatrix();
					GL11.glEnable(GL11.GL_SCISSOR_TEST);
					int c = sw.getScaledWidth() < this.mc.displayWidth ? (int) Math.round((double) this.mc.displayWidth / (double) sw.getScaledWidth()) : 1;
					GL11.glScissor((guiCenter - 84) * c, (guiTopLog + 90) * c, (56) * c, (44) * c);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawNpc(activeQuest.npc);
					GL11.glDisable(GL11.GL_SCISSOR_TEST);
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 89, guiTopLog + 9, 500.0f);
					GlStateManager.enableBlend();
					GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
					this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 193, 0, 63, 52);
					GlStateManager.popMatrix();
				}
				first = 60;
			}
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 22, guiTopLog, 1.0d);
			ItemStack[] stacks = activeQuest.stacks.toArray(new ItemStack[activeQuest.stacks.size()]);
			int j = 0, k = 0;
			for (int l = 0; l < 2; l++) {
				List<String> list = activeQuest.getText(first, player, fontRenderer).get(page + l);
				if (list == null) { continue; } // empty right page
				int h = 0;
				for (String line : list) {
					if (line.indexOf(" " + ((char) 0xffff)+ " ") !=-1) {
						if (j < stacks.length) {
							int pos = fontRenderer.getStringWidth(line.substring(0, line.indexOf(""+((char) 0xffff)) - 1));
							ItemStack stack = stacks[j];
							float x = pos + (l == 1 ? 128.0f : -3.0f);
							float y = (page==0 && l==0 ? first : 0.0f) + h * 12.0f - 7.5f;
							if (isMouseHover(mouseX, mouseY, guiLeft + 27 + (int) x, guiTopLog + 7 + (int) y, 10, 10)) {
								hover = stack.getTooltip(player, player.capabilities.isCreativeMode ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
								this.hoverText = hover.toArray(new String[hover.size()]);
							}
							GlStateManager.pushMatrix();
							GlStateManager.translate(x, y, 0.0f);
							GlStateManager.scale(0.65f, 0.65f, 0.65f);
							RenderHelper.enableGUIStandardItemLighting();
							GlStateManager.translate(6.0f, 8.0f, 0.0f);
							zLevel = 100.0f;
							itemRender.zLevel = 100.0f;
							GlStateManager.enableLighting();
							GlStateManager.enableRescaleNormal();
							this.itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
							this.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, (String) null);
							GlStateManager.disableLighting();
							itemRender.zLevel = 0.0f;
							zLevel = 0.0f;
							RenderHelper.disableStandardItemLighting();
							GlStateManager.popMatrix();
							j++;
						}
						line = line.replace(""+((char) 0xffff), " ");
					}
					if (line.indexOf(((char) 0xfffe)) !=-1) {
						if (activeQuest.entitys.containsKey(k)) {
							int pos = fontRenderer.getStringWidth(line.substring(0, line.indexOf(""+((char) 0xfffe)) - 1));
							float x = pos + (l == 1 ? 128.0f : -3.0f);
							float y = (page==0 && l==0 ? first : 0.0f) + h * 12.0f - 7.5f;
							if (isMouseHover(mouseX, mouseY, guiLeft + 26 + (int) x, guiTopLog + 7 + (int) y, 10, 10)) {
								if (!hoverMob(mouseX, mouseY, activeQuest.entitys.get(k))) {
									setHoverText(new TextComponentTranslation("quest.hover.err.log.entity").getFormattedText());
								}
							}
							GlStateManager.pushMatrix();
							GlStateManager.enableAlpha();
							GlStateManager.enableBlend();
							GlStateManager.translate(x + 4, y + 6.5f, 0.0f);
							GlStateManager.scale(0.3f, 0.15f, 1.0f);
							GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
							this.mc.renderEngine.bindTexture(killIcon);
							this.drawTexturedModalRect(0, 0, 32, 64, 32, 64);
							GlStateManager.popMatrix();
						}
						line = line.replace(""+((char) 0xfffe), " ");
						k++;
					}
					this.fontRenderer.drawString(line, l == 1 ? 128 : 0, (page==0 && l==0 ? first : 0) + h * 12, CustomNpcs.questLogColor, false);
					h++;
				}
			}
			GlStateManager.popMatrix();
		}
		else if (quests.containsKey(selectCat)) {
			if (page > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 12, guiTopLog + 168, 0.0f);
				if (isMouseHover(mouseX, mouseY, guiLeft + 12, guiTopLog + 168, 18, 10)) { hoverButton = 5; } // pre cat list; 
				this.mc.renderEngine.bindTexture(GuiLog.bookGuiTextures);
				this.drawTexturedModalRect(0, 0, hoverButton == 5 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if (Math.floor(quests.size() / 10.d) > page) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 230, guiTopLog + 168, 0.0f);
				if (isMouseHover(mouseX, mouseY, guiLeft + 230, guiTopLog + 168, 18, 10)) { hoverButton = 4; } // next cat list; 
				this.mc.renderEngine.bindTexture(GuiLog.bookGuiTextures);
				this.drawTexturedModalRect(0, 0, hoverButton == 4 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
			i = 0; p = 0; st = page * 10;
			Color color = categories.get(selectCat);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 26, guiTopLog + 6.0f, 0.0f);
			for (int id : quests.get(selectCat).keySet()) {
				if (p < page * 10) { p++; continue; }
				if (i == 5) {
					GlStateManager.translate(105.0f, -124.0f, 0.0f);
				} else if (i % 5 != 0) {
					GlStateManager.translate(0.0f, 31.0f, 0.0f);
				}
				GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
				this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
				this.drawTexturedModalRect(0, 0, 0, 0, 98, 30);
				QuestData qd = quests.get(selectCat).get(id);
				Quest quest = qd.quest;
				if (isMouseHover(mouseX, mouseY, guiLeft + 26 + (i > 4 ? 105 : 0), guiTopLog + 6 + (i % 5) * 31, 98, 30)) {
					hoverButton = 6;
					hoverQuestId = id;
				}
				GlStateManager.pushMatrix();
				GlStateManager.translate(3.0f, 3.0f, 0.0f);
				this.mc.renderEngine.bindTexture(quest.icon);
				GlStateManager.scale(0.09375f, 0.09375f, 1.0f);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(29.0f, 3.0f, 0.0f);
				String name = "", qName = quest.getTitle();
				if (this.fontRenderer.getStringWidth(qName) < 67) { name = qName; }
				else {
					for (int j = 0; j < qName.length(); j++) {
						if (this.fontRenderer.getStringWidth(name + qName.charAt(j) + "...") >= 67) { break; }
						name += qName.charAt(j);
					}
					name += "...";
				}
				this.fontRenderer.drawString(name, 0, 0, CustomNpcs.questLogColor, false);
				IQuestObjective[] objs = quest.getObjectives(player);
				int j = 0;
				for (IQuestObjective iqo : objs) {
					if (iqo.isCompleted()) { j++; }
				}
				String progress = j + " / " +  objs.length;
				this.fontRenderer.drawString(progress, 0, 10, CustomNpcs.questLogColor, false);
				if (hoverQuestId == id) {
					hover.add(((char) 167) + "7" + new TextComponentTranslation("drop.category").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + selectCat);
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + qName);
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.progress").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + (j >= objs.length ? "a" : "c") + progress);
					if (quest.completion == EnumQuestCompletion.Npc && quest.completer != null) {
						hover.add(new TextComponentTranslation("quest.completewith", quest.completer.getName()).getFormattedText());
					}
				}
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.popMatrix();
				i++;
				p++;
				if (i == 10) { break; }
			}
			GlStateManager.popMatrix();
			if (!hover.isEmpty()) { this.hoverText = hover.toArray(new String[hover.size()]); }
		}
	}

	private void drawCommpass(int mouseX, int mouseY, float partialTicks) {
		if (!CustomNpcs.showQuestCompass || step != -1) { return; }
		
		fontRenderer.drawString(new TextComponentTranslation("quest.screen.pos").getFormattedText(), guiLeft + 22, guiTopLog + 10, CustomNpcs.questLogColor);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 22, guiTopLog + 20, 0);
		GlStateManager.scale(0.5f, 0.5f, 0.5f);
		Gui.drawRect(-1, -1, 207, 139, 0xFF808080);
		Gui.drawRect(0, 0, 206, 138, 0xFFF0F0F0);
		Gui.drawRect(58, 113, 149, 139, 0xFF808080);
		Gui.drawRect(59, 114, 148, 138, 0xFFA0A0A0);
		GlStateManager.translate(this.compassData.screenPos[0] * 240.0d, this.compassData.screenPos[1] * 160.0d, 0.0d);
		Gui.drawRect(-3, -1, 4, 3, 0xFF0000FF);
		Gui.drawRect(-3, 3, 4, 5, 0xFFFF00FF);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 22, guiTopLog + 94, 0);
		this.drawString(fontRenderer, " ", 0, 0, 0xFFFFFFFF);
		fontRenderer.drawString("U:", 0, 0, CustomNpcs.questLogColor);
		fontRenderer.drawString("V:", 54, 0, CustomNpcs.questLogColor);
		fontRenderer.drawString("S:", 0, 18, CustomNpcs.questLogColor);
		fontRenderer.drawString("T:", 0, 34, CustomNpcs.questLogColor);
		fontRenderer.drawString("R:", 0, 50, CustomNpcs.questLogColor);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiCenter + 5, guiTopLog + 10, 0);
		int i = 0;
		if (this.compassData.showQuestName) { fontRenderer.drawString(new TextComponentTranslation("quest.setts.q.name").getFormattedText(), 0, 0, CustomNpcs.questLogColor); i = 12; }
		if (this.compassData.showTaskProgress) { fontRenderer.drawString(new TextComponentTranslation("quest.setts.q.tasks").getFormattedText(), 0, i, CustomNpcs.questLogColor); }
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiCenter + 54, guiTopLog + 10, 50.0f);
		float scale = -30.0f * this.compassData.scale;
		float incline = -45.0f + this.compassData.incline;

		this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(0.0f, -32.85714f * this.compassData.scale + 32.42857f, 0.0f);
		GlStateManager.scale(scale , scale, scale);
		GlStateManager.rotate(incline, 1.0f, 0.0f, 0.0f);
		if (this.compassData.rot!=0.0f)  { GlStateManager.rotate(this.compassData.rot, 0.0f, 1.0f, 0.0f); }
		GlStateManager.enableDepth();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		RenderHelper.enableStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
		
		// Body
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("body", "dial", "arrow_1", "arrow_20", "fase"), null));
		GlStateManager.rotate((System.currentTimeMillis()%3500L) / (3500.0f / 360.0f), 0.0f, 1.0f, 0.0f);
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_0"), null));
		GlStateManager.popMatrix();
		
	}

	private void drawFaction(int mouseX, int mouseY, float partialTicks) {
		if (step != -1) { return; }
		if (playerFactions.isEmpty()) {
			String noFaction = new TextComponentTranslation("faction.nostanding").getFormattedText();
			fontRenderer.drawSplitString(noFaction, guiLeft + 24, guiTop + 36, 98, CustomNpcs.questLogColor);
			return;
		}
		int i = 0, p = 0;
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 26, guiTopLog + 8.0f, 0.0f);
		PlayerFactionData data = PlayerData.get(player).factionData;
		for (Faction f : playerFactions) {
			if (f.hideFaction && !player.capabilities.isCreativeMode) { continue; }
			if (p < page * 10) { p++; continue; }
			if (i == 8) { GlStateManager.translate(105.0f, -7.0f * 19.0f, 0.0f); }
			else if (i % 8 != 0) { GlStateManager.translate(0.0f, 19.0f, 0.0f); }
			if (f.hideFaction) { this.drawGradientRect(1, 1, 90, 12, 0x20FF0000, 0x80FF0000); }
			this.mc.renderEngine.bindTexture(GuiLog.ql.get(4));
			Color c = new Color(f.color);
			GlStateManager.color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
			this.drawTexturedModalRect(0, 0, 158, 74, 98, 16);
			
			float w = 1.0f;
			Color h;
			int points = data.getFactionPoints(player, f.id), nextp = 0, t = 0;
			if (f.isNeutralToPlayer(player)) {
				t = 1;
				h = new Color(0xF2DD00);
				w = (float) (f.friendlyPoints - points) / (float) (f.friendlyPoints - f.neutralPoints);
				nextp = f.friendlyPoints;
			}
			else if (f.isFriendlyToPlayer(player)) {
				t = 2;
				h = new Color(0x00DD00);
				w = (float) (f.friendlyPoints * 2  - points) / (float) f.friendlyPoints;
			}
			else {
				h = new Color(0xDD0000);
				w = (float) (f.neutralPoints - points) / (float) f.neutralPoints;
				nextp = f.neutralPoints;
			}
			
			if (w < 0.0f) { w = 0.0f; } else if (w > 1.0f) { w = 1.0f; }
			int em = (int) (89.0f * w), ew = 89 - em;
			if (em > 0) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 0.65f);
				this.drawTexturedModalRect(90 - em, 12, 256 - em, 71, em, 3);
			}
			if (ew > 0) {
				GlStateManager.color(h.getRed() / 255.0f, h.getGreen() / 255.0f, h.getBlue() / 255.0f, 0.65f);
				this.drawTexturedModalRect(1, 12, 167, 71, ew, 3);
			}
			
			String name = "", qName = f.getName();
			if (this.fontRenderer.getStringWidth(qName) < 87) { name = qName; }
			else {
				for (int j = 0; j < qName.length(); j++) {
					if (this.fontRenderer.getStringWidth(name + qName.charAt(j) + "...") >= 87) { break; }
					name += qName.charAt(j);
				}
				name += "...";
			}
			this.fontRenderer.drawString(name, 3, 2, CustomNpcs.questLogColor, false);
			
			if (isMouseHover(mouseX, mouseY, guiLeft + 26 + (i > 4 ? 105 : 0), guiTopLog + 8 + (i % 8) * 19, 98, 16)) {
				List<String> hover = Lists.<String>newArrayList();
				// GM
				if (f.hideFaction) { hover.add(new TextComponentTranslation("faction.hover.hidden").getFormattedText()); }
				// name
				if (player.capabilities.isCreativeMode) { hover.add(((char) 167) + "7ID:"+f.id + "; " + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + f.getName()); }
				else { hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + f.getName()); }
				// attitude
				hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.attitude").getFormattedText() + ((char) 167) + "7: " + ((char) 167) +
						(t==0 ? "4" + new TextComponentTranslation("faction.unfriendly").getFormattedText() :
						t==2 ? "2" + new TextComponentTranslation("faction.friendly").getFormattedText() :
						"6" + new TextComponentTranslation("faction.neutral").getFormattedText()));
				// points
				hover.add(((char) 167) + "7" + new TextComponentTranslation("faction.points").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + points + (nextp != 0 ? "/" + nextp : ""));
				if (!f.description.isEmpty()) {
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.description").getFormattedText());
					hover.add(new TextComponentTranslation(f.description).getFormattedText());
				}
				
				this.hoverText = hover.toArray(new String[hover.size()]);
			}
			this.mc.renderEngine.bindTexture(f.flag);
			if (mc.renderEngine.getTexture(f.flag) != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(90.0f, 1.0f, 0.0f);
				GlStateManager.scale(0.175f, 0.11f, 1.0f);
				GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 4, 4, 40, 128);
				GlStateManager.popMatrix();
			}
			i++;
			p++;
			if (i == 16) { break; }
		}
		GlStateManager.popMatrix();
	}
	
	private boolean hoverMob(int mouseX, int mouseY, Entity entity) {
		if (entity == null) { return false; }
		GlStateManager.pushMatrix();
		GlStateManager.translate((guiLeft + 22) * -1, guiTopLog * -1, 300.0d);
		GlStateManager.translate(mouseX, mouseY, 0.0f);
		
		if (mouseY > sw.getScaledHeight_double() / 2.0d) { GlStateManager.translate(0.0f, -15.0f, 0.0f); }
		else { GlStateManager.translate(0.0f, 45.0f, 0.0f); }
		
		String modelName = "";
		if (entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).display.getModel() != null) { modelName = ((EntityNPCInterface) entity).display.getModel(); }
		else {
			ResourceLocation location = EntityList.getKey(entity);
			if (location != null) { modelName = location.toString(); }
		}
		boolean canUpdate = this.preDrawEntity(modelName);
		GlStateManager.rotate((mc.world.getTotalWorldTime() % 360) * 5.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		entity.ticksExisted = 1;
		if (canUpdate) { entity.onUpdate(); }
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.popMatrix();
		return true;
	}

	private void drawNpc(EntityNPCInterface npc) {
		if (npc == null) { return; }
		GlStateManager.translate((sw.getScaledWidth_double() - 110.0d) / 2.0d, (sw.getScaledHeight_double() - 20.0d) / 2.0d, 0.0d);
		String modelName = "";
		if (npc.display.getModel() != null) { modelName = npc.display.getModel(); }
		boolean canUpdate = this.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		npc.ticksExisted = 100;
		if (canUpdate) { npc.onUpdate(); }
		this.mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	private boolean preDrawEntity(String modelName) {
		boolean canUpdate = true;
		if (modelName.equals("customnpcs:npcslime")) { GlStateManager.translate(-2.0f, -15.0f, 0.0f); }
		else if (modelName.equals("minecraft:magma_cube") ||
				modelName.equals("minecraft:silverfish") ||
				modelName.equals("minecraft:slime")) { GlStateManager.translate(-2.0f, -21.0f, 0.0f); }
		else if (modelName.equals("minecraft:zombie")) { GlStateManager.translate(3.0f, 9.0f, 0.0f); }
		else if (modelName.equals("minecraft:vex")) { GlStateManager.translate(-3.0f, -15.0f, 0.0f); }
		else if (modelName.equals("minecraft:endermite")) { GlStateManager.translate(-1.0f, -25.0f, 0.0f); }
		else if (modelName.equals("minecraft:enderman")) { GlStateManager.translate(0.0f, 30.0f, 0.0f); }
		else if (modelName.equals("minecraft:cave_spider")) { GlStateManager.translate(-2.0f, -18.0f, 0.0f); }
		else if (modelName.equals("minecraft:chicken") ||
				modelName.equals("minecraft:wolf") ||
				modelName.equals("minecraft:ocelot") ||
				modelName.equals("minecraft:spider")) { GlStateManager.translate(0.0f, -15.0f, 0.0f); }
		else if (modelName.equals("minecraft:shulker")) { GlStateManager.translate(-2.0f, -15.0f, 0.0f); }
		else if (modelName.equals("minecraft:squid")) { GlStateManager.translate(0.0f, -5.0f, 0.0f); }
		else if (modelName.equals("minecraft:guardian")) {
			GlStateManager.translate(4.0f, -18.5f, 0.0f);
			canUpdate = false;
		}
		else if (modelName.equals("minecraft:parrot") ||
				modelName.equals("minecraft:rabbit") ||
				modelName.equals("minecraft:bat")) { GlStateManager.translate(0.0f, -19.0f, 0.0f); }
		else if (modelName.equals("minecraft:horse") ||
				modelName.equals("minecraft:illusion_illager") ||
				modelName.equals("minecraft:villager") ||
				modelName.equals("minecraft:snowman") ||
				modelName.equals("minecraft:vindication_illager") ||
				modelName.equals("minecraft:zombie_horse") ||
				modelName.equals("minecraft:zombie_villager") ||
				modelName.equals("minecraft:stray") ||
				modelName.equals("minecraft:skeleton") ||
				modelName.equals("minecraft:witch") ||
				modelName.equals("minecraft:skeleton_horse") ||
				modelName.equals("minecraft:mule") ||
				modelName.equals("minecraft:evocation_illager") ||
				modelName.equals("minecraft:zombie_pigman"))
		{ GlStateManager.translate(0.0f, 5.0f, 0.0f); }
		else if (modelName.equals("minecraft:ender_dragon")) {
			GlStateManager.translate(35.0f, -32.0f, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
		}
		else if (modelName.equals("minecraft:elder_guardian")) {
			GlStateManager.translate(1.5f, -15.0f, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			canUpdate = false;
		}
		else if (modelName.equals("minecraft:giant")) {
			GlStateManager.translate(0.0f, 15.0f, 0.0f);
			GlStateManager.scale(0.1875f, 0.1875f, 0.1875f);
			canUpdate = false;
		}
		else if (modelName.equals("customnpcs:npcdragon")) {
			GlStateManager.translate(22.0f, -16.0f, 0.0f);
			canUpdate = false;
		}
		else if (modelName.equals("customnpcs:npcpony")) { GlStateManager.translate(-5.0f, 2.0f, 0.0f); }
		else if (modelName.equals("customnpcs:npccrystal")) { GlStateManager.translate(0.0f, 3.0f, 0.0f); }
		else if (modelName.equals("minecraft:wither_skeleton") ||
				modelName.equals("minecraft:villager_golem") ||
				modelName.equals("minecraft:customnpcs.npcgolem")) { GlStateManager.translate(0.0f, 18.0f, 0.0f); }
		else if (modelName.equals("minecraft:polar_bear")) {
			GlStateManager.translate(-1.0f, -12.0f, 0.0f);
			GlStateManager.scale(0.75f, 0.75f, 0.75f);
		}
		else if (modelName.equals("minecraft:husk") ||
				modelName.equals("minecraft:llama")) { GlStateManager.translate(0.0f, 12.0f, 0.0f); }
		else if (modelName.equals("minecraft:pig")) { GlStateManager.translate(0.0f, -12.0f, 0.0f); }
		else if (modelName.equals("minecraft:sheep")) { GlStateManager.translate(0.0f, -8.0f, 0.0f); }
		else if (modelName.equals("minecraft:wither")) {
			GlStateManager.translate(-3.0f, 3.0f, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
		}
		else if (modelName.equals("minecraft:ghast")) {
			GlStateManager.translate(-2.0f, -21.0f, 0.0f);
			GlStateManager.scale(0.2f, 0.2f, 0.2f);
		}
		else if (modelName.equals("minecraft:customnpcs.customnpcalex")) { GlStateManager.translate(-1.0f, 0.0f, 0.0f); }
		else { GlStateManager.translate(0.0f, 8.0f, 0.0f); }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(210.0f, 0.0f, 1.0f, 0.0f);
		return canUpdate;
	}

	public void close() { }
	
	@Override
	public void keyTyped(char c, int i) {
		if (step >= 0) { return; }
		if (i == 1 || i == mc.gameSettings.keyBindInventory.getKeyCode()) { 
			tick = 15;
			mtick = 15;
			step = type + 7;
			type = i == 1 ? -2 : -1;
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (step >= 0) { return; }
		if (type == 2) {
			if (mouseX>=this.guiLeft+5 && mouseX<=this.guiLeft+125 && mouseY>=this.guiTop+14 && mouseY<=this.guiTop+94) {
				this.compassData.screenPos[0] = Math.round((double) (mouseX - this.guiLeft - 5) * 8.33333d)/1000.0d;
				this.compassData.screenPos[1] = Math.round((double) (mouseY - this.guiTop - 14) * 12.5d)/1000.0d;
				this.initGui();
			}
		}
		else { this.buttonPress(hoverButton); }
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		switch(slider.id) {
			case 0: {
				this.compassData.scale = Math.round((slider.sliderValue + 0.5f)*100.0f)/100.0f;
				slider.setString((""+this.compassData.scale).replace(".", ","));
				break;
			}
			case 1: {
				this.compassData.incline = Math.round((-45.0f * slider.sliderValue + 22.5f)*100.0f)/100.0f;
				slider.setString((""+(45.0f + this.compassData.incline*-1.0f)).replace(".", ","));
				break;
			}
			case 2: {
				this.compassData.rot = Math.round((60.0f * slider.sliderValue - 30.0f)*100.0f)/100.0f;
				slider.setString((""+this.compassData.rot).replace(".", ","));
				break;
			}
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (type != 2) { return; }
		switch(textField.getId()) {
			case 0: {
				this.compassData.screenPos[0] = Math.round(textField.getDouble()*100.0d)/100.0d;
				break;
			}
			case 1: {
				this.compassData.screenPos[1] = Math.round(textField.getDouble()*100.0d)/100.0d;
				break;
			}
		}
	}

	@Override
	public void save() { Client.sendDataDelayCheck(EnumPlayerPacket.SaveCompassData, 0, 0, this.compassData.getNbt()); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("FactionList", 9)) {
			playerFactions.clear();
			NBTTagList list = compound.getTagList("FactionList", 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				Faction faction = new Faction();
				faction.readNBT(list.getCompoundTagAt(i));
				playerFactions.add(faction);
			}
			PlayerFactionData data = new PlayerFactionData();
			data.loadNBTData(compound);
			for (int id : data.factionData.keySet()) {
				int points = data.factionData.get(id);
				for (Faction faction2 : this.playerFactions) {
					if (faction2.id == id) { faction2.defaultPoints = points; }
				}
			}
		}
		initGui();
	}
	
	public class QuestInfo {
		
		public final QuestData qData;
		private EntityNPCInterface npc;
		private final Map<Integer, List<String>> map = Maps.<Integer, List<String>>newTreeMap(); // [key, data texts]
		public final List<ItemStack> stacks = Lists.<ItemStack>newArrayList();
		public final Map<Integer, Entity> entitys = Maps.<Integer, Entity>newTreeMap();
		private final World world;
		
		private boolean newInstance = true;

		public QuestInfo(QuestData qd, World world) {
			this.world = world;
			qData = qd;
			if (qd.quest.completer != null) {
				NBTTagCompound compound = new NBTTagCompound();
				qd.quest.completer.writeToNBTOptional(compound);
				compound.setUniqueId("UUID", UUID.randomUUID());
				Entity e = EntityList.createEntityFromNBT(compound, world);
				if (e instanceof EntityNPCInterface) { npc = (EntityNPCInterface) e; }
				else {
					npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
					npc.readEntityFromNBT(compound);
				}
			}
			else {
				npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
				qd.quest.completer = npc;
				qd.quest.completerPos[0] = (int) npc.posX;
				qd.quest.completerPos[1] = (int) (npc.posY + 0.5d);
				qd.quest.completerPos[2] = (int) npc.posZ;
				qd.quest.completerPos[3] = npc.world.provider.getDimension();
			}
			npc = AdditionalMethods.setToGUI(npc);
		}
		
		public void reset() { newInstance = true; }
		
		public Map<Integer, List<String>> getText(int first, EntityPlayer player, FontRenderer fontRenderer) { // [listID/2, lable texts]
			if (!newInstance && !map.isEmpty()) { return map; }
			map.clear();
			stacks.clear();
			entitys.clear();
			String ent = ""+((char) 10);
			String text = ((char) 167)+"l"+new TextComponentTranslation(qData.quest.title).getFormattedText() + ent;
			if (qData.quest.completion==EnumQuestCompletion.Npc && qData.quest.completer!=null) {
				text += new TextComponentTranslation("quest.completewith", ((char) 167)+"l"+qData.quest.completer.getName()).getFormattedText() + ent;
			}
			IQuestObjective[] allObj = qData.quest.getObjectives(player);
			if (allObj.length>0) {
				text +=  ent + ((char) 167) + "l" + new TextComponentTranslation("quest.objectives."+qData.quest.step).getFormattedText() + ent;
				for (int i = 0; i < allObj.length; i++) {
					text += (i + 1) + "-";
					if (((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.ITEM || ((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.CRAFT) {
						stacks.add(((QuestObjective) allObj[i]).getItemStack());
						text += " " + ((char) 0xffff)+ " ";
					}
					if (((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.KILL || ((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.AREAKILL) {
						text += " " + ((char) 0xfffe)+ " ";
						if (allObj[i].isNotShowLogEntity()) { entitys.put(entitys.size(), null); }
						else {
							String target = ((QuestObjective) allObj[i]).getTargetName();
							Entity e = EntityList.createEntityByIDFromName(new ResourceLocation(target), world);
							if (e == null) {
								IPos pos = allObj[i].getCompassPos();
								if (pos.getY() >=0 && (pos.getX() != 0 || pos.getZ() != 0) && world.provider.getDimension() == allObj[i].getCompassDimension()) {
									int r = allObj[i].getCompassRange();
									List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() - r, pos.getY() - r, pos.getZ() - r, pos.getX() + r, pos.getY() + r, pos.getZ() + r));
									for (Entity en : list) {
										if (en.getName().equals(target)) {
											NBTTagCompound compound = new NBTTagCompound();
											en.writeToNBTAtomically(compound);
											Entity enti = EntityList.createEntityFromNBT(compound, world);
											if (enti == null) { e = en; } else {
												e = enti;
												if (e instanceof EntityNPCInterface) { e = AdditionalMethods.setToGUI((EntityNPCInterface) e); }
											}
											break;
										}
									}
								}
							}
							entitys.put(entitys.size(), e);
						}
					}
					text += allObj[i].getText() + ent;
				}
				text = text.substring(0, text.length()-1);
			}
			text += qData.quest.getLogText();
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
						if (fontRenderer.getStringWidth(newLine) > 98) {
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
					map.put(curentList, list);
					list = Lists.<String>newArrayList();
					curentList++;
				}
				list.add(l);
			}
			if (!list.isEmpty()) { map.put(curentList, list); }
			newInstance = false;
			return map;
		}
		
	}
	
}
