package noppes.npcs.client.gui.player;

import java.awt.Color;
import java.util.*;

import net.minecraft.client.renderer.RenderHelper;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
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
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.QuestEvent.QuestExtraButtonEvent;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.constants.EnumRewardType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerCompassHUDData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;

public class GuiLog
extends GuiNPCInterface
implements GuiYesNoCallback, IGuiData, ISliderListener, ITextfieldListener {

	public static class QuestInfo {

		public final QuestData qData;
		protected EntityNPCInterface npc;
		protected final Map<Integer, List<String>> map = new TreeMap<>(); // [key, data texts]
		public final List<ItemStack> stacks = new ArrayList<>();
		public final Map<Integer, Entity> entitys = new TreeMap<>();
		protected final World world;

		protected boolean newInstance = true;

		public QuestInfo(QuestData qd, World world) {
			this.world = world;
			qData = qd;
			if (qd.quest.completer != null) {
				NBTTagCompound compound = new NBTTagCompound();
				qd.quest.completer.writeToNBTOptional(compound);
				compound.setUniqueId("UUID", UUID.randomUUID());
				Entity e = EntityList.createEntityFromNBT(compound, world);
				if (e instanceof EntityNPCInterface) {
					npc = (EntityNPCInterface) e;
				} else {
					npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
                    if (npc != null) { npc.readEntityFromNBT(compound); }
				}
			} else {
				npc = (EntityNPCInterface) EntityList
						.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
				qd.quest.completer = npc;
				if (npc != null) {
					qd.quest.completerPos[0] = (int) npc.posX;
					qd.quest.completerPos[1] = (int) (npc.posY + 0.5d);
					qd.quest.completerPos[2] = (int) npc.posZ;
					qd.quest.completerPos[3] = npc.world.provider.getDimension();
				}
			}
			npc = Util.instance.copyToGUI(npc, world, false);
		}

		public Map<Integer, List<String>> getText(int first, EntityPlayer player, FontRenderer fontRenderer) {
			if (!newInstance && !map.isEmpty()) { return map; }
			map.clear();
			stacks.clear();
			entitys.clear();
			String ent = "" + ((char) 10);
			StringBuilder text = new StringBuilder(((char) 167) + "l" + new TextComponentTranslation(qData.quest.title).getFormattedText() + ent);
			if (qData.quest.completion == EnumQuestCompletion.Npc && qData.quest.completer != null) {
				text.append(new TextComponentTranslation("quest.completeness", ((char) 167) + "l" + qData.quest.completer.getName()).getFormattedText()).append(ent);
			}
			IQuestObjective[] allObj = qData.quest.getObjectives(player);
			if (allObj.length > 0) {
				text.append(ent).append((char) 167).append("l").append(new TextComponentTranslation("quest.objectives." + qData.quest.step).getFormattedText()).append(ent);
				for (int i = 0; i < allObj.length; i++) {
					text.append((i + 1)).append("-");
					if (((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.ITEM
							|| ((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.CRAFT) {
						stacks.add(((QuestObjective) allObj[i]).getItemStack());
						text.append(" " + ((char) 0xffff) + " ");
					}
					else if (((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.KILL
							|| ((QuestObjective) allObj[i]).getEnumType() == EnumQuestTask.AREAKILL) {
						text.append(" " + ((char) 0xfffe) + " ");
						if (allObj[i].isNotShowLogEntity()) {
							entitys.put(entitys.size(), null);
						} else {
							String target = allObj[i].getTargetName();
							Entity e = EntityList.createEntityByIDFromName(new ResourceLocation(target), world);
							if (e == null) {
								IPos pos = allObj[i].getCompassPos();
								if (pos.getY() >= 0 && (pos.getX() != 0 || pos.getZ() != 0)
										&& world.provider.getDimension() == allObj[i].getCompassDimension()) {
									int r = allObj[i].getCompassRange();
									List<Entity> list = new ArrayList<>();
									try {
										list = world.getEntitiesWithinAABB(Entity.class,
												new AxisAlignedBB(pos.getX() - r, pos.getY() - r, pos.getZ() - r,
														pos.getX() + r, pos.getY() + r, pos.getZ() + r));
									}
									catch (Exception ignored) { }
									for (Entity en : list) {
										if (en.getName().equals(target)) {
											NBTTagCompound compound = new NBTTagCompound();
											en.writeToNBTAtomically(compound);
											Entity entity = EntityList.createEntityFromNBT(compound, world);
											if (entity == null) {
												e = en;
											} else {
												e = entity;
												if (e instanceof EntityNPCInterface) {
													e = Util.instance.copyToGUI((EntityNPCInterface) e, world,
															false);
												}
											}
											break;
										}
									}
								}
							}
							entitys.put(entitys.size(), e);
						}
					}
					text.append(allObj[i].getText()).append(ent);
				}
				text = new StringBuilder(text.substring(0, text.length() - 1));
			}
			text.append(qData.quest.getLogText());
			List<String> lines = new ArrayList<>();
			int currentList = 0;
			String line = "";
			text = new StringBuilder(text.toString().replace("\n", " \n "));
			text = new StringBuilder(text.toString().replace("\r", " \r "));
			String[] words = text.toString().split(" ");
			String color = ((char) 167) + "r";
			float width = 98.0f * GuiLog.scaleW;
			for (String word : words) {
				Label_0236: {
					if (!word.isEmpty()) {
						if (word.length() == 1) {
							char c = word.charAt(0);
							if (c == '\r' || c == '\n') {
								lines.add(color + line);
								color = Util.instance.getLastColor(color, line);
								line = "";
								break Label_0236;
							}
						}
						String newLine;
						if (line.isEmpty()) {
							newLine = word;
						} else {
							newLine = line + " " + word;
						}
						if (fontRenderer.getStringWidth(newLine) > width) {
							lines.add(color + line);
							color = Util.instance.getLastColor(color, line);
							line = word.trim();
						} else {
							line = newLine;
						}
					}
				}
			}
			if (!line.isEmpty()) { lines.add(color + line); }
			List<String> list = new ArrayList<>();
			float height = (3.57143f * GuiLog.scaleH + 116.42857f) * GuiLog.scaleH; // 1.0 - 120; 2.4 - 125
			for (String l : lines) {
				if ((list.size() * 10) > height - (currentList == 0 ? first : 0)) {
					map.put(currentList, list);
					list = new ArrayList<>();
					currentList++;
				}
				list.add(l);
			}
			if (!list.isEmpty()) { map.put(currentList, list); }
			newInstance = false;

			List<ItemStack> rewarList = new ArrayList<>();
			for (int i = 0; i < qData.quest.rewardItems.getSizeInventory(); i++) {
				ItemStack stack = qData.quest.rewardItems.getStackInSlot(i);
				if (stack.isEmpty()) { continue; }
				boolean has = false;
				if (qData.quest.rewardType == EnumRewardType.ALL) {
					for (ItemStack it : rewarList) {
						if (stack.isItemEqual(it) && ItemStack.areItemStackShareTagsEqual(stack, it)) {
							has = true;
							break;
						}
					}
				}
				if (!has) { rewarList.add(stack); }
			}
			if (!rewarList.isEmpty()) { stacks.addAll(rewarList); }
			return map;
		}

		public void reset() {
			newInstance = true;
		}

	}
	protected static final Map<Integer, ResourceLocation> ql = new TreeMap<>();
	protected static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	protected static final ResourceLocation killIcon = new ResourceLocation("textures/entity/skeleton/skeleton.png");

	public static float scaleW, scaleH;

	static {
		GuiLog.ql.clear();
		for (int i = 0; i < 6; i++) {
			GuiLog.ql.put(i, new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_" + i + ".png"));
		}
	}
	public static QuestInfo activeQuest;
	public static boolean preDrawEntity(String modelName) {
		boolean canUpdate = true;
        switch (modelName) {
            case "customnpcs:npcslime":
            case "minecraft:shulker":
                GlStateManager.translate(-2.0f, -15.0f * scaleH, 0.0f);
                break;
            case "minecraft:magma_cube":
            case "minecraft:silverfish":
            case "minecraft:slime":
                GlStateManager.translate(-2.0f, -21.0f * scaleH, 0.0f);
                break;
            case "minecraft:zombie":
                GlStateManager.translate(3.0f, 9.0f * scaleH, 0.0f);
                break;
            case "minecraft:vex":
                GlStateManager.translate(-3.0f, -15.0f * scaleH, 0.0f);
                break;
            case "minecraft:endermite":
                GlStateManager.translate(-1.0f, -25.0f * scaleH, 0.0f);
                break;
            case "minecraft:enderman":
                GlStateManager.translate(0.0f, 30.0f * scaleH, 0.0f);
                break;
            case "minecraft:cave_spider":
                GlStateManager.translate(-2.0f, -18.0f * scaleH, 0.0f);
                break;
            case "minecraft:chicken":
            case "minecraft:wolf":
            case "minecraft:ocelot":
            case "minecraft:spider":
                GlStateManager.translate(0.0f, -15.0f * scaleH, 0.0f);
                break;
            case "minecraft:squid":
                GlStateManager.translate(0.0f, -5.0f * scaleH, 0.0f);
                break;
            case "minecraft:guardian":
                GlStateManager.translate(4.0f, -18.5f * scaleH, 0.0f);
                canUpdate = false;
                break;
            case "minecraft:parrot":
            case "minecraft:rabbit":
            case "minecraft:bat":
                GlStateManager.translate(0.0f, -19.0f * scaleH, 0.0f);
                break;
            case "minecraft:horse":
            case "minecraft:illusion_illager":
            case "minecraft:villager":
            case "minecraft:snowman":
            case "minecraft:vindication_illager":
            case "minecraft:zombie_horse":
            case "minecraft:zombie_villager":
            case "minecraft:stray":
            case "minecraft:skeleton":
            case "minecraft:witch":
            case "minecraft:skeleton_horse":
            case "minecraft:mule":
            case "minecraft:evocation_illager":
            case "minecraft:zombie_pigman":
                GlStateManager.translate(0.0f, 5.0f * scaleH, 0.0f);
                break;
            case "minecraft:ender_dragon":
                GlStateManager.translate(35.0f, -32.0f * scaleH, 0.0f);
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                break;
            case "minecraft:elder_guardian":
                GlStateManager.translate(1.5f, -15.0f * scaleH, 0.0f);
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                canUpdate = false;
                break;
            case "minecraft:giant":
                GlStateManager.translate(0.0f, 15.0f * scaleH, 0.0f);
                GlStateManager.scale(0.1875f, 0.1875f, 0.1875f);
                canUpdate = false;
                break;
            case "customnpcs:npcdragon":
                GlStateManager.translate(22.0f, -16.0f * scaleH, 0.0f);
                canUpdate = false;
                break;
            case "customnpcs:npcpony":
                GlStateManager.translate(-5.0f, 2.0f * scaleH, 0.0f);
                break;
            case "customnpcs:npccrystal":
                GlStateManager.translate(0.0f, 3.0f * scaleH, 0.0f);
                break;
            case "minecraft:wither_skeleton":
            case "minecraft:villager_golem":
            case "minecraft:customnpcs.npcgolem":
                GlStateManager.translate(0.0f, 18.0f * scaleH, 0.0f);
                break;
            case "minecraft:polar_bear":
                GlStateManager.translate(-1.0f, -12.0f * scaleH, 0.0f);
                GlStateManager.scale(0.75f, 0.75f, 0.75f);
                break;
            case "minecraft:husk":
            case "minecraft:llama":
                GlStateManager.translate(0.0f, 12.0f * scaleH, 0.0f);
                break;
            case "minecraft:pig":
                GlStateManager.translate(0.0f, -12.0f * scaleH, 0.0f);
                break;
            case "minecraft:wither":
                GlStateManager.translate(-3.0f, 3.0f * scaleH, 0.0f);
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                break;
            case "minecraft:ghast":
                GlStateManager.translate(-2.0f, -21.0f * scaleH, 0.0f);
                GlStateManager.scale(0.2f, 0.2f, 0.2f);
                break;
            case "minecraft:customnpcs.customnpcalex":
                GlStateManager.translate(-1.0f, 0.0f, 0.0f);
                break;
            default:
                GlStateManager.translate(0.0f, -8.0f * scaleH, 0.0f);
                break;
        }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(210.0f, 0.0f, 1.0f, 0.0f);
		return canUpdate;
	}
	/*
	 * 0-tab inv; 1-tab factions; 2-tab quests; 3-tab compass 4-page right; 5-page
	 * left 6-quest; 7/14-tab categories 16-pre cat list; 17-next cat list 20/28-cat
	 * list 30 - extended button 31 - compass look 32 - cancel quest
	 */
	protected int hoverButton;
	protected int hoverQuestId;
	protected int catRow;
	protected int catSelect;
	protected int page;
	protected int step;
	protected int tick;
	protected int milliTick;
	protected int temp;
	protected int guiLLeft;
	protected int guiLRight;
	protected int guiLTop;
	protected int guiTopLog;
	protected int guiCenter;
	public int type; // -1-inv; 0-faction; 1-quests; 2-compass

	protected boolean toPrePage = true;
	protected final Random rnd = new Random();
	protected ScaledResolution sw;
	protected final Map<String, Map<Integer, QuestData>> quests = new TreeMap<>(); // {
	// category																							// quest]}
	protected final Map<String, Color> categories = new TreeMap<>(); // [name, color]
	protected final List<Faction> playerFactions = new ArrayList<>();

	protected final PlayerCompassHUDData compassData;

	protected PlayerData playerData;

	public GuiLog(int t) {
		super();
		type = t;
		temp = 0;
		tick = 15;
		milliTick = 15;
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
		compassData = new PlayerCompassHUDData();
		compassData.load(CustomNpcs.proxy.getPlayerData(player).hud.compassData.getNbt());
		activeQuest = null;
		if (t == 1) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FactionsGet);
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (type != 2) {
			return;
		}
		switch (button.getID()) {
			case 0: {
				compassData.showQuestName = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 1: {
				compassData.showTaskProgress = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
		}
	}

	public void buttonPress(int id) {
		if (type == 0 && id > 6 && id < 15) {
			int catList = catRow * 8 + id - 7;
			if (catSelect == catList && page != 0) {
				step = 11;
				tick = 10;
				milliTick = 10;
				page = 0;
			}
			if (catSelect != catList || activeQuest != null) {
				step = catSelect > catList || activeQuest != null ? 11 : 10;
				tick = 11;
				milliTick = 10;
				catSelect = catList;
				page = 0;
				activeQuest = null;
			}
			return;
		} // quest category rows
		switch (id) {
			case 0: {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				milliTick = 15;
				step = type + 7;
				type = -1;
				break;
			} // inventory
			case 1: {
				if (type == 1) {
					return;
				}
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				milliTick = 15;
				toPrePage = false;
				step = type + 7;

				page = 0;
				type = 1;
				NoppesUtilPlayer.sendData(EnumPlayerPacket.FactionsGet);
				initGui();
				break;
			} // factions
			case 2: {
				if (type == 0) {
					return;
				}
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				milliTick = 15;
				toPrePage = type == 1;
				step = type + 7;

				catRow = 0;
				catSelect = 0;
				page = 0;
				activeQuest = null;
				type = 0;
				initGui();
				break;
			} // quests
			case 3: {
				if (type == 2 || !CustomNpcs.ShowQuestCompass) {
					return;
				}
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				tick = 15;
				milliTick = 15;
				toPrePage = true;
				step = type + 7;

				page = 0;
				type = 2;
				initGui();
				break;
			} // compass
			case 4: {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				page++;
				step = 10;
				tick = 10;
				milliTick = 10;
				break;
			} // page right
			case 5: {
				page--;
				step = 11;
				tick = 10;
				milliTick = 10;
				break;
			} // page left
			case 6: {
				if (hoverQuestId < 1) {
					return;
				}
				String catName = "";
				int i = 0;
				for (String key : categories.keySet()) {
					if (i == catSelect) {
						catName = key;
						break;
					}
					i++;
				}
				if (catName.isEmpty() || !quests.containsKey(catName) || !quests.get(catName).containsKey(hoverQuestId)) {
					return;
				}
				activeQuest = new QuestInfo(quests.get(catName).get(hoverQuestId), this.mc.world);
				step = 10;
				tick = 10;
				milliTick = 10;
				break;
			} // quest select
			case 16: {
				if (type != 0) {
					return;
				}
				MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
						(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
						0.8f + 0.4f * this.rnd.nextFloat());
				catRow--;
				break;
			} // pre cat list
			case 17: {
				if (type != 0) {
					return;
				}
				MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
						(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
						0.8f + 0.4f * this.rnd.nextFloat());
				catRow++;
				break;
			} // next cat list
			case 30: {
				if (hoverQuestId <= 0) {
					return;
				}
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.QUEST_LOG_BUTTON, new QuestExtraButtonEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player), QuestController.instance.get(hoverQuestId)));
				NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestExtraButton, hoverQuestId);
				break;
			} // extended button
			case 31: {
				if (hoverQuestId <= 0) {
					return;
				}
				if (ClientProxy.playerData.hud.questID == hoverQuestId) {
					ClientProxy.playerData.hud.questID = -1;
				} else {
					ClientProxy.playerData.hud.questID = hoverQuestId;
				}
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				break;
			} // compass look
			case 32: {
				if (hoverQuestId <= 0) {
					return;
				}
				for (Map<Integer, QuestData> map : quests.values()) {
					for (QuestData qd : map.values()) {
						if (qd.quest.id == hoverQuestId) {
							GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("drop.quest", new TextComponentTranslation(qd.quest.getTitle()).getFormattedText()).getFormattedText(), new TextComponentTranslation("quest.cancel.info").getFormattedText(), hoverQuestId);
							displayGuiScreen(guiyesno);
							break;
						}
					}
				}
				break;
			} // cancel quest
		}
	}

	public void close() {
		PlayerCompassHUDData compassD = CustomNpcs.proxy.getPlayerData(player).hud.compassData;
		NBTTagCompound compound = compassData.getNbt();
		compassD.load(compound);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.SaveCompassData, compound);
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) {
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestRemoveActive, id);
		PlayerQuestData data = CustomNpcs.proxy.getPlayerData(player).questData;
		if (data != null) {
			data.activeQuests.remove(id);
			initGui();
		}
	}

	protected void drawBox(int mouseX, int mouseY) {
		hoverButton = -1;
		hoverQuestId = 0;
		// tabs
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 10, guiTop, 0.0f);
		boolean offset = false;
		for (int i = 0; i < (CustomNpcs.ShowQuestCompass ? 4 : 3); i++) {
			boolean hover;
			switch (i) {
				case 1: {
					if (offset) {
						GlStateManager.translate(0.0f, 0.0f, -1.0f);
					}
					offset = (type == 1);
					GlStateManager.translate(33.0f, 0.0f, offset ? 1.0f : 0.0f);
					hover = this.isMouseHover(mouseX, mouseY, guiLeft + 43, guiTop, 28, 30);
					break;
				}
				case 2: {
					if (offset) {
						GlStateManager.translate(0.0f, 0.0f, -1.0f);
					}
					offset = (type == 0);
					GlStateManager.translate(33.0f, 0.0f, offset ? 1.0f : 0.0f);
					hover = this.isMouseHover(mouseX, mouseY, guiLeft + 76, guiTop, 28, 30);
					break;
				}
				case 3: {
					if (offset) {
						GlStateManager.translate(0.0f, 0.0f, -1.0f);
					}
					offset = (type == 2);
					GlStateManager.translate(-114.0f + 256.0f * scaleW, 0.0f, offset ? 1.0f : 0.0f);
					hover = this.isMouseHover(mouseX, mouseY, (int) (guiLeft - 38.0f + 256.0f * scaleW), guiTop, 28, 30);
					break;
				}
				default: {
					hover = this.isMouseHover(mouseX, mouseY, guiLeft + 10, guiTop, 28, 30);
					break;
				}
			}
			if (hover) {
				hoverButton = i;
			}
			this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
			this.drawTexturedModalRect(0, 0, 0, hover || offset ? 30 : 60, 28, 30);

			GlStateManager.pushMatrix();
			GlStateManager.translate(6.0f, 8.0f, 0.0f);
			zLevel = 100.0f;
			itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			ItemStack stack;
			switch (i) {
			case 1: {
				stack = new ItemStack(Items.BANNER, 1, 1);
				break;
			}
			case 2: {
				stack = new ItemStack(Items.BOOK);
				break;
			}
			case 3: {
				stack = new ItemStack(Items.COMPASS);
				break;
			}
			default:
				stack = new ItemStack(Blocks.CRAFTING_TABLE);
				break;
			}
			RenderHelper.enableGUIStandardItemLighting();
			itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
			itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, stack, 6, 8, null);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			itemRender.zLevel = 0.0f;
			zLevel = 0.0f;
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();

		// place
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
		GlStateManager.scale(scaleW, scaleH, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
		this.mc.getTextureManager().bindTexture(GuiLog.ql.get(1));
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
		GlStateManager.popMatrix();

		if (step == -1 && (type == 0 || type == 1)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLLeft + 2.0f * scaleH, guiLTop + 152.5f * scaleH, 0.0f);
			fontRenderer.drawString("" + (page * 2 + 1), 0, 0, CustomNpcs.NotEnableColor.getRGB());
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			String p = "" + (page * 2 + 2);
			GlStateManager.translate(guiLLeft - fontRenderer.getStringWidth(p) + 205.0f * scaleW, guiLTop + 153.0f * scaleH, 0.0f);
			fontRenderer.drawString(p, 0, 0, CustomNpcs.NotEnableColor.getRGB());
			GlStateManager.popMatrix();
		}
		if (step >= 0 && step < 10) {
			return;
		}
		if (type == 0) {
			this.drawQuestLog(mouseX, mouseY);
		} else if (type == 1) {
			this.drawFaction(mouseX, mouseY);
		} else if (type == 2) {
			this.drawCompass();
		}
	}

	protected void drawCompass() {
		if (!CustomNpcs.ShowQuestCompass || step != -1) {
			return;
		}

		fontRenderer.drawString(new TextComponentTranslation("quest.screen.pos").getFormattedText(), (int) (guiLLeft - 3.0f * scaleW), guiLTop, CustomNpcs.QuestLogColor.getRGB());

		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLLeft - 3.0f * scaleW, guiLTop + 10, 0);
		GlStateManager.scale(0.5f * scaleW, 0.5f * scaleH, 0.5f);
		Gui.drawRect(-1, -1, 207, 139, 0xFF808080);
		Gui.drawRect(0, 0, 206, 138, 0xFFF0F0F0);
		Gui.drawRect(58, 113, 149, 139, 0xFF808080);
		Gui.drawRect(59, 114, 148, 138, 0xFFA0A0A0);
		GlStateManager.translate(this.compassData.screenPos[0] * 206.0d, this.compassData.screenPos[1] * 138.0d, 0.0d);
		Gui.drawRect(-3, -1, 4, 3, 0xFF0000FF);
		Gui.drawRect(-3, 3, 4, 5, 0xFFFF00FF);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLLeft - 3.0f * scaleW, guiLTop + 84.0f * scaleH, 0);
		this.drawString(fontRenderer, " ", 0, 0, 0xFFFFFFFF);
		fontRenderer.drawString("U:", 0, 0, CustomNpcs.QuestLogColor.getRGB());
		fontRenderer.drawString("V:", (int) (54.0f * scaleW), 0, CustomNpcs.QuestLogColor.getRGB());
		fontRenderer.drawString("S:", 0, (int) (18.0f * scaleH), CustomNpcs.QuestLogColor.getRGB());
		fontRenderer.drawString("T:", 0, (int) (34.0f * scaleH), CustomNpcs.QuestLogColor.getRGB());
		fontRenderer.drawString("R:", 0, (int) (50.0f * scaleH), CustomNpcs.QuestLogColor.getRGB());
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLRight + (int) (3.0f * scaleW), guiLTop + (int) (91.0f * scaleH), 0);
		int i = 0;
		if (this.compassData.showQuestName) {
			String text = new TextComponentTranslation("quest.setts.q.name").getFormattedText();
			int w = (int) (49.0f * scaleW) - fontRenderer.getStringWidth(text) / 2;
			fontRenderer.drawString(text, w, 0, CustomNpcs.QuestLogColor.getRGB());
			i = 12;
		}
		if (this.compassData.showTaskProgress) {
			String text = new TextComponentTranslation("quest.setts.q.tasks").getFormattedText();
			int w = (int) (49.0f * scaleW) - fontRenderer.getStringWidth(text) / 2;
			fontRenderer.drawString(text, w, i, CustomNpcs.QuestLogColor.getRGB());
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLRight + (int) (52.0f * scaleW), guiLTop + (int) (57.0f * scaleH), 50.0f);
		float scale = -30.0f * this.compassData.scale;
		float incline = -45.0f + this.compassData.incline;

		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(0.0f, (-32.85714f * this.compassData.scale + 32.42857f) * scaleH, 0.0f);
		GlStateManager.scale(scale * scaleW, scale * scaleH, scale);
		GlStateManager.rotate(incline, 1.0f, 0.0f, 0.0f);
		if (this.compassData.rot != 0.0f) {
			GlStateManager.rotate(this.compassData.rot, 0.0f, 1.0f, 0.0f);
		}
		GlStateManager.enableDepth();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);

		// Body
		RenderHelper.enableStandardItemLighting();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, new ArrayList<>(Arrays.asList("body", "dial", "arrow_1", "arrow_20", "fase")), null));
		GlStateManager.rotate((System.currentTimeMillis() % 3500L) / (3500.0f / 360.0f), 0.0f, 1.0f, 0.0f);
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Collections.singletonList("arrow_0"), null));
		GlStateManager.popMatrix();
	}

	protected void drawFaction(int mouseX, int mouseY) {
		if (step != -1) {
			return;
		}
		if (playerFactions.isEmpty()) {
			String noFaction = new TextComponentTranslation("faction.nostanding").getFormattedText();
			fontRenderer.drawSplitString(noFaction, guiLeft + 24, guiTop + 36, 98, CustomNpcs.QuestLogColor.getRGB());
			return;
		}
		if (playerFactions.size() > 16) {
			if (page > 0) { // left
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLLeft - 5.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLLeft - 5.0f * scaleW), (int) (guiLTop + 160.0f * scaleH), 18, 10)) {
					hoverButton = 5;
				} // pre cat list;
				this.mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hoverButton == 5 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if (Math.floor(playerFactions.size() / 16.d) > page) { // right
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 230.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLeft + 230.0f * scaleW), (int) (guiLTop + 160.0f * scaleH), 18, 10)) {
					hoverButton = 4;
				} // next cat list;
				this.mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hoverButton == 4 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
		}
		int i = 0, p = 0;
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLLeft, guiLTop, 0.0f);
		for (Faction f : playerFactions) {
			if (f.hideFaction && !player.capabilities.isCreativeMode) {
				continue;
			}
			if (p < page * 10) {
				p++;
				continue;
			}
			if (i == 8) {
				GlStateManager.translate(105.0f * scaleW, -7.0f * 19.0f * scaleH, 0.0f);
			} else if (i % 8 != 0) {
				GlStateManager.translate(0.0f, 19.0f * scaleH, 0.0f);
			}
			if (f.hideFaction) {
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				this.drawGradientRect(1, 1, 90, 12, 0x20FF0000, 0x80FF0000);
				GlStateManager.popMatrix();
			}
			this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
			Color c = new Color(f.color);
			GlStateManager.color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);

			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			this.drawTexturedModalRect(0, 0, 158, 74, 98, 16);
			GlStateManager.popMatrix();

			float w;
			Color h;
			int points = playerData.factionData.getFactionPoints(player, f.id), nextPoint = 0, t = 0;
			if (f.isNeutralToPlayer(player)) {
				t = 1;
				h = new Color(0xF2DD00);
				w = (float) (f.friendlyPoints - points) / (float) (f.friendlyPoints - f.neutralPoints);
				nextPoint = f.friendlyPoints;
			} else if (f.isFriendlyToPlayer(player)) {
				t = 2;
				h = new Color(0x00DD00);
				w = (float) (f.friendlyPoints * 2 - points) / (float) f.friendlyPoints;
			} else {
				h = new Color(0xDD0000);
				w = (float) (f.neutralPoints - points) / (float) f.neutralPoints;
				nextPoint = f.neutralPoints;
			}

			if (w < 0.0f) {
				w = 0.0f;
			} else if (w > 1.0f) {
				w = 1.0f;
			}
			int em = (int) (89.0f * w), ew = 89 - em;
			if (em > 0) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 0.65f);
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				this.drawTexturedModalRect(90 - em, 12, 256 - em, 71, em, 3);
				GlStateManager.popMatrix();
			}
			if (ew > 0) {
				GlStateManager.color(h.getRed() / 255.0f, h.getGreen() / 255.0f, h.getBlue() / 255.0f, 0.65f);
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				this.drawTexturedModalRect(1, 12, 167, 71, ew, 3);
				GlStateManager.popMatrix();
			}

			StringBuilder name = new StringBuilder();
            String qName = f.getName();
            if (this.fontRenderer.getStringWidth(qName) < 87.0f * scaleW) {
				name = new StringBuilder(qName);
			} else {
				for (int j = 0; j < qName.length(); j++) {
					if (this.fontRenderer.getStringWidth(name.toString() + qName.charAt(j) + "...") >= 87.0f * scaleW) {
						break;
					}
					name.append(qName.charAt(j));
				}
				name.append("...");
			}
			this.fontRenderer.drawString(name.toString(), 3 * scaleW, 2 * scaleH, CustomNpcs.QuestLogColor.getRGB(), false);

			if (isMouseHover(mouseX, mouseY, (int) (guiLLeft + (i > 4 ? 105.0f : 0) * scaleW), (int) (guiLTop + (i % 8) * 19.0f * scaleH), (int) (98.0f * scaleW), (int) (16.0f * scaleH))) {
				List<String> hover = new ArrayList<>();
				// GM
				if (f.hideFaction) {
					hover.add(new TextComponentTranslation("faction.hover.hidden").getFormattedText());
				}
				// name
				if (player.capabilities.isCreativeMode) {
					hover.add(((char) 167) + "7ID:" + f.id + "; "
							+ new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: "
							+ ((char) 167) + "r" + f.getName());
				} else {
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText()
							+ ((char) 167) + "7: " + ((char) 167) + "r" + f.getName());
				}
				// attitude
				hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.attitude").getFormattedText()
						+ ((char) 167) + "7: " + ((char) 167)
						+ (t == 0 ? "4" + new TextComponentTranslation("faction.unfriendly").getFormattedText()
								: t == 2 ? "2" + new TextComponentTranslation("faction.friendly").getFormattedText()
										: "6" + new TextComponentTranslation("faction.neutral").getFormattedText()));
				// points
				hover.add(((char) 167) + "7" + new TextComponentTranslation("faction.points").getFormattedText()
						+ ((char) 167) + "7: " + ((char) 167) + "r" + points + (nextPoint != 0 ? "/" + nextPoint : ""));
				if (!f.description.isEmpty()) {
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.description").getFormattedText());
					hover.add(new TextComponentTranslation(f.description).getFormattedText());
				}
				setHoverText(hover);
			}
			this.mc.getTextureManager().bindTexture(f.flag);
            mc.getTextureManager().getTexture(f.flag);
            GlStateManager.pushMatrix();
            GlStateManager.translate(90.0f * scaleW, scaleH, 0.0f);
            GlStateManager.scale(0.175f, 0.11f, 1.0f);
            GlStateManager.scale(scaleW, scaleH, 1.0f);
            GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
            this.drawTexturedModalRect(0, 0, 4, 4, 40, 128);
            GlStateManager.popMatrix();
            i++;
			p++;
			if (i == 16) {
				break;
			}
		}
		GlStateManager.popMatrix();
	}

	protected void drawNpc(EntityNPCInterface npc) {
		if (npc == null) {
			return;
		}
		GlStateManager.translate(guiLLeft + 49.0f * scaleW, guiLTop + 67.0f * scaleH, 0.0f); // 49, 67
		String modelName = "";
		if (npc.display.getModel() != null) {
			modelName = npc.display.getModel();
		}
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		GlStateManager.scale(scaleW, scaleH, 1.0f);
		npc.ticksExisted = 100;
		if (canUpdate) {
			npc.onUpdate();
		}
		this.mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	protected void drawQuestLog(int mouseX, int mouseY) {
		if (categories.isEmpty()) {
			String noFaction = new TextComponentTranslation("quest.noquests").getFormattedText();
			fontRenderer.drawSplitString(noFaction, guiLLeft, guiLTop, (int) (98.0f * scaleW), CustomNpcs.QuestLogColor.getRGB());
			return;
		}
		List<String> hover = new ArrayList<>();
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTopLog + 23.5f * scaleH, 0.0f);
		this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
		if (catRow > 0) { // pre Cats
			if (isMouseHover(mouseX, mouseY, guiLeft - (int) (17.0f * scaleW), (int) (guiTopLog + 7.5f * scaleH), (int) (18.0f * scaleW), (int) (16.0f * scaleH))) {
				hoverButton = 16;
			} // pre cat list;
			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(-17, -16, 111, hoverButton == 16 ? 30 : 46, 18, 16);
			GlStateManager.popMatrix();
		}
		if (categories.size() - (catRow + 1) * 8 > 0) { // next Cats
			if (isMouseHover(mouseX, mouseY, guiLeft - (int) (17.0f * scaleW), (int) (guiTopLog + 151.5f * scaleH), (int) (18.0f * scaleW), (int) (16.0f * scaleH))) {
				hoverButton = 17;
			} // next cat list;
			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(-17, 128, 129, hoverButton == 17 ? 30 : 46, 18, 16);
			GlStateManager.popMatrix();
		}
		int i = 0, p = 0, st = catRow * 8;
		String selectCat = "";
		for (String fullCatName : categories.keySet()) {
			String catName = fullCatName;
			if (this.fontRenderer.getStringWidth(catName) > 80) {
				StringBuilder tempCatName = new StringBuilder();
				for (int g = 0; g < catName.length(); g++) {
					char c = catName.charAt(g);
					if (this.fontRenderer.getStringWidth(tempCatName.toString() + c + "...") > 86) {
						break;
					}
					tempCatName.append(c);
				}
				catName = tempCatName + "...";
			}
			if (p < st) {
				if (catSelect == p && step < 0) {
					selectCat = catName;
				}
				p++;
				continue;
			}
			int catW = this.fontRenderer.getStringWidth(catName) + 10 + i;
			this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
			if (isMouseHover(mouseX, mouseY, guiLeft + (int) ((5 - catW) * scaleW), (int) (guiTopLog + (23.5f + i * 16.0f) * scaleH), (int) (catW * scaleH), (int) (16.0f * scaleH))) {
				hoverButton = 7 + i;
			} // 7/15-tab categories;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			this.drawTexturedModalRect(4 - (int) (catW / scaleW) + i, i * 16, 0, 90 + (catSelect == p || hoverButton == 7 + i ? 0 : 16), (int) (catW / scaleW), 16);
			GlStateManager.popMatrix();
			if (catSelect == p && step < 0) {
				selectCat = catName;
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				this.drawTexturedModalRect(3 + i, i * 16, 234 + i, 90, 22 - i, 16);
				GlStateManager.popMatrix();
			}
			StringBuilder name = new StringBuilder();
			for (int j = 0; j < catName.length(); j++) {
				if (this.fontRenderer.getStringWidth(name.toString() + catName.charAt(j)) > catW - 5) {
					break;
				}
				name.append(catName.charAt(j));
			}
			this.fontRenderer.drawString(name.toString(), 4 - catW + 10 + i, (16.0f * scaleH - 10.0f) / 2.0f + (i * 16.0f) * scaleH, CustomNpcs.QuestLogColor.getRGB(), false);
			i++;
			p++;
			if (i >= 8) {
				break;
			}
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();

		if (step != -1) {
			return;
		}
		if (activeQuest != null) {
			int first = 0;
			// NPC
			if (activeQuest.qData.quest.completion == EnumQuestCompletion.Npc && activeQuest.npc != null) {
				if (page == 0) {
					GlStateManager.pushMatrix();
					GL11.glEnable(GL11.GL_SCISSOR_TEST);
					int c = sw.getScaledWidth() < this.mc.displayWidth
							? (int) Math.round((double) this.mc.displayWidth / (double) sw.getScaledWidth())
							: 1;
					GL11.glScissor(((int) (guiLLeft + 22.0f * scaleW) * c), (int) (guiLTop + (12.0f * scaleH + 81.0f) * scaleH) * c, (int) (54.0f * scaleW) * c, (int) (38.0f * scaleH) * c);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawNpc(activeQuest.npc);
					GL11.glDisable(GL11.GL_SCISSOR_TEST);
					GlStateManager.popMatrix();

					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLLeft + 16.5f * scaleW, guiLTop - 4.0f * scaleH, 500.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					GlStateManager.enableBlend();
					GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 193, 0, 63, 52);
					GlStateManager.popMatrix();
				}
				first = (int) (44.0f * scaleH);
			}
			// Text
			ItemStack[] stacks = activeQuest.stacks.toArray(new ItemStack[0]);
			int j = 0, k = 0;
			for (int l = 0; l < 2; l++) {
				List<String> list = activeQuest.getText(first, player, fontRenderer).get(page + l);
				if (list == null) {
					continue;
				} // empty right page
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLLeft, guiLTop, 501.0d);
				int h = 0;
				for (String line : list) {
					if (line.contains(" " + ((char) 0xffff) + " ") || line.contains(((char) 0xffff) + " ")) {
						if (j < stacks.length) {
							int pos = fontRenderer.getStringWidth(line.substring(0, line.indexOf("" + ((char) 0xffff)) - 1));
							ItemStack stack = stacks[j];
							float x = pos + (l == 1 ? 105.0f : 0.0f) * scaleW;
							float y = (page == 0 && l == 0 ? first : 0.0f) + h * 12.0f;
							if (isMouseHover(mouseX, mouseY, guiLLeft + (int) x, guiLTop + (int) y, 10, 10)) {
								hover = stack.getTooltip(player, player.capabilities.isCreativeMode ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
								setHoverText(hover);
							}
							GlStateManager.pushMatrix();
							GlStateManager.translate(x - 4.0f, y - 5.5f, 0.0f);
							GlStateManager.scale(0.65f, 0.65f, 0.65f);
							GlStateManager.translate(6.0f, 8.0f, 0.0f);
							zLevel = 100.0f;
							itemRender.zLevel = 100.0f;
							GlStateManager.enableLighting();
							GlStateManager.enableRescaleNormal();
							RenderHelper.enableGUIStandardItemLighting();
							itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
							RenderHelper.disableStandardItemLighting();
							GlStateManager.disableLighting();
							itemRender.zLevel = 0.0f;
							zLevel = 0.0f;
							GlStateManager.popMatrix();
							j++;
						}
						line = line.replace("" + ((char) 0xffff), " " + (!line.contains(" " + ((char) 0xffff) + " ") ? " " : ""));
					}
					if (line.indexOf(((char) 0xfffe)) != -1) {
						if (activeQuest.entitys.containsKey(k)) {
							int pos = fontRenderer.getStringWidth(line.substring(0, line.indexOf("" + ((char) 0xfffe)) - 1));
							float x = pos + (l == 1 ? 105.0f : 0.0f) * scaleW;
							float y = (page == 0 && l == 0 ? first : 0.0f) + h * 12.0f;
							if (isMouseHover(mouseX, mouseY, guiLLeft + (int) x, guiLTop + (int) y, 10, 10)) {
								if (!hoverMob(mouseX, mouseY, activeQuest.entitys.get(k))) {
									setHoverText(new TextComponentTranslation("quest.hover.err.log.entity")
											.getFormattedText());
								}
							}
							GlStateManager.pushMatrix();
							GlStateManager.enableAlpha();
							GlStateManager.enableBlend();
							GlStateManager.translate(x + 0.5f, y, 0.0f);
							GlStateManager.scale(0.3f, 0.15f, 1.0f);
							GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
							this.mc.getTextureManager().bindTexture(killIcon);
							this.drawTexturedModalRect(0, 0, 32, 64, 32, 64);
							GlStateManager.popMatrix();
						}
						line = line.replace("" + ((char) 0xfffe), " ");
						k++;
					}
					this.fontRenderer.drawString(line, (l == 1 ? 105.0f : 0.0f) * scaleW, (page == 0 && l == 0 ? first : 0) + h * 12, CustomNpcs.QuestLogColor.getRGB(), false);
					h++;
				}
				GlStateManager.popMatrix();
			}

			if (page > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLLeft - 5.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLLeft - 5.0f * scaleW), (int) (guiLTop + 160.0f * scaleH), 18, 10)) { hoverButton = 5; } // pre cat list;
				mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(0, 0, hoverButton == 5 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if (page * 2 < activeQuest.map.size()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 230.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLeft + 230.0f * scaleW), (int) (guiLTop + 160.0f * scaleH), 18, 10)) { hoverButton = 4; } // next cat list;
				mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(0, 0, hoverButton == 4 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
		} else if (quests.containsKey(selectCat)) {
			if (page > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLLeft - 5.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLLeft - 5.0f * scaleW), (int) (guiLTop + 160.0f * scaleH),
						18, 10)) {
					hoverButton = 5;
				} // pre cat list;
				this.mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hoverButton == 5 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if (Math.floor(quests.size() / 10.d) > page) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 230.0f * scaleW, guiLTop + 160.0f * scaleH, 0.0f);
				if (isMouseHover(mouseX, mouseY, (int) (guiLeft + 230.0f * scaleW), (int) (guiLTop + 160.0f * scaleH),
						18, 10)) {
					hoverButton = 4;
				} // next cat list;
				this.mc.getTextureManager().bindTexture(GuiLog.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hoverButton == 4 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
			i = 0;
			p = 0;
            Color color = categories.get(selectCat);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLLeft, guiLTop - 1.5f * scaleH, 0.0f);
			for (int id : quests.get(selectCat).keySet()) {
				if (p < page * 10) {
					p++;
					continue;
				}
				if (i == 5) {
					GlStateManager.translate(105.0f * scaleW, -124.0f * scaleH, 0.0f);
				} else if (i % 5 != 0) {
					GlStateManager.translate(0.0f, 31.0f * scaleH, 0.0f);
				}

				GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
						1.0f);
				this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 98, 30);
				GlStateManager.popMatrix();
				QuestData qd = quests.get(selectCat).get(id);
				Quest quest = qd.quest;

				GlStateManager.pushMatrix();
				GlStateManager.translate(3.0f * scaleW, 3.0f * scaleH, 0.0f);
				this.mc.getTextureManager().bindTexture(quest.icon);
				GlStateManager.scale(0.09375f, 0.09375f, 1.0f);
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
				GlStateManager.popMatrix();

				int qxPos = (int) (guiLLeft + (i > 4 ? 105 : 0) * scaleW);
				int qyPos = (int) (guiLTop + (-1.5f + (i % 5) * 31.0f) * scaleH);

				boolean hasExtraButton = quest.extraButton != 0 || player.capabilities.isCreativeMode;
				if (hasExtraButton) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(87.0f * scaleW, 19.0f * scaleH, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					int xo = quest.extraButton == 0 ? 9 : quest.extraButton * 9;
					if (quest.extraButton == 0 && player.capabilities.isCreativeMode) {
						this.drawGradientRect(1, 1, 8, 8, 0x20FF0000, 0x80FF0000);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						xo = (int) ((System.currentTimeMillis() % 5000) / 1000) * 9 + 9;
					}
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					if (isMouseHover(mouseX, mouseY, qxPos + (int) (87.0f * scaleW), qyPos + (int) (19.0f * scaleH),
							(int) (9.0f * scaleW), (int) (9.0f * scaleH))) {
						hoverButton = 30;
						hoverQuestId = id;
					}
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(0, 0, 116 + xo, hoverButton == 30 && hoverQuestId == id ? 9 : 0, 9, 9);
					GlStateManager.popMatrix();
				}

				boolean hasCompassButton = quest.hasCompassSettings()
						&& (CustomNpcs.ShowQuestCompass || player.capabilities.isCreativeMode);
				if (hasCompassButton) {
					GlStateManager.pushMatrix();
					GlStateManager.translate((87.0f - (hasExtraButton ? 9.0f : 0.0f)) * scaleW, 19.0f * scaleH, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					if (!CustomNpcs.ShowQuestCompass && player.capabilities.isCreativeMode) {
						this.drawGradientRect(1, 1, 8, 8, 0x20FF0000, 0x80FF0000);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					}
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					if (isMouseHover(mouseX, mouseY, qxPos + (int) ((87.0f - (hasExtraButton ? 9.0f : 0.0f)) * scaleW),
							qyPos + (int) (19.0f * scaleH), (int) (9.0f * scaleW), (int) (9.0f * scaleH))) {
						hoverButton = 31;
						hoverQuestId = id;
					}
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(0, 0, 107 + (playerData.hud.questID == quest.id ? 0 : 9),
							hoverButton == 31 && hoverQuestId == id ? 9 : 0, 9, 9);
					GlStateManager.popMatrix();
				}

				boolean hasCancelableButton = quest.cancelable || player.capabilities.isCreativeMode;
				if (hasCancelableButton) {
					GlStateManager.pushMatrix();
					final float v = 87.0f - (hasExtraButton ? 9.0f : 0.0f) - (hasCompassButton ? 9.0f : 0.0f);
					GlStateManager.translate(
							v * scaleW,
							19.0f * scaleH, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					if (!quest.cancelable && player.capabilities.isCreativeMode) {
						this.drawGradientRect(1, 1, 8, 8, 0x20FF0000, 0x80FF0000);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					}
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					if (isMouseHover(mouseX, mouseY,
							qxPos + (int) (v
									* scaleW),
							qyPos + (int) (19.0f * scaleH), (int) (9.0f * scaleW), (int) (9.0f * scaleH))) {
						hoverButton = 32;
						hoverQuestId = id;
					}
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(0, 0, 98, hoverButton == 32 && hoverQuestId == id ? 9 : 0, 9, 9);
					GlStateManager.popMatrix();
				}

				GlStateManager.pushMatrix();
				GlStateManager.translate(29.0f * scaleW, 3.0f * scaleH, 0.0f);
				StringBuilder name = new StringBuilder();
                String qName = quest.getTitle();
                if (this.fontRenderer.getStringWidth(qName) < 67.0f * scaleW) {
					name = new StringBuilder(qName);
				} else {
					for (int j = 0; j < qName.length(); j++) {
						if (this.fontRenderer.getStringWidth(name.toString() + qName.charAt(j) + "...") >= 67.0f * scaleW) {
							break;
						}
						name.append(qName.charAt(j));
					}
					name.append("...");
				}
				this.fontRenderer.drawString(name.toString(), 0, 0, CustomNpcs.QuestLogColor.getRGB(), false);
				IQuestObjective[] objs = quest.getObjectives(player);
				int j = 0;
				for (IQuestObjective iqo : objs) {
					if (iqo.isCompleted()) {
						j++;
					}
				}
				String progress = j + " / " + objs.length;
				this.fontRenderer.drawString(progress, 0, 10, CustomNpcs.QuestLogColor.getRGB(), false);

				if (hoverButton > 29 && hoverQuestId == id) {
					if (hoverButton == 30) {
						hover.add(new TextComponentTranslation(
								quest.extraButtonText.isEmpty() ? "quest.hover.extra.button" : quest.extraButtonText)
										.getFormattedText());
						if (quest.extraButton == 0 && player.capabilities.isCreativeMode) {
							hover.add(new TextComponentTranslation("quest.hover.gm.info").getFormattedText());
						}
					} else if (hoverButton == 31) {
						hover.add(new TextComponentTranslation(
								"quest.hover.compass." + (playerData.hud.questID == quest.id)).getFormattedText());
						if (!CustomNpcs.ShowQuestCompass && player.capabilities.isCreativeMode) {
							hover.add(new TextComponentTranslation("quest.hover.gm.info").getFormattedText());
						}
					} else if (hoverButton == 32) {
						hover.add(new TextComponentTranslation("drop.quest", quest.getName()).getFormattedText());
						if (!quest.cancelable && player.capabilities.isCreativeMode) {
							hover.add(new TextComponentTranslation("quest.hover.gm.info").getFormattedText());
						}
					}
				} else if (isMouseHover(mouseX, mouseY, qxPos, qyPos, (int) (98.0f * scaleW), (int) (30.0f * scaleH))) {
					hoverButton = 6;
					hoverQuestId = id;
					hover.add(((char) 167) + "7" + new TextComponentTranslation("drop.category").getFormattedText()
							+ ((char) 167) + "7: " + ((char) 167) + "r" + selectCat);
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText()
							+ ((char) 167) + "7: " + ((char) 167) + "r" + qName);
					hover.add(((char) 167) + "7" + new TextComponentTranslation("gui.progress").getFormattedText()
							+ ((char) 167) + "7: " + ((char) 167) + (j >= objs.length ? "a" : "c") + progress);
					if (quest.completion == EnumQuestCompletion.Npc && quest.completer != null) {
						hover.add(new TextComponentTranslation("quest.completewith", quest.completer.getName()).getFormattedText());
					}
				}
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.popMatrix();
				i++;
				p++;
				if (i == 10) {
					break;
				}
			}
			GlStateManager.popMatrix();
			if (!hover.isEmpty()) {
				setHoverText(hover);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		playerData = CustomNpcs.proxy.getPlayerData(player);
		// Back
		GlStateManager.pushMatrix();
		drawGradientRect(0, 0, mc.displayWidth, mc.displayHeight, 0xAA000000, 0xAA000000);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();

		// Animations
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (tick >= 0) {
			if (tick == 0) {
				partialTicks = 0.0f;
			}
			float part = (float) tick + partialTicks;
			float cos = (float) Math.cos(90.0d * part / (double) milliTick * Math.PI / 180.0d);
			if (cos < 0.0f) {
				cos = 0.0f;
			} else if (cos > 1.0f) {
				cos = 1.0f;
			}
			switch (step) {
				case 0: {
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(2));
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter + (1.0f - cos) * (guiCenter + 50.0f),
							guiTopLog + (1.0f - cos) * 250.0f, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 1;
						tick = 21;
						milliTick = 20;
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.down",
								(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
								0.75f + 0.25f * this.rnd.nextFloat());
						GlStateManager.disableBlend();
					}
					break;
				} // start open
				case 1: {
					// right
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					GlStateManager.popMatrix();
					// left
					boolean up = tick >= milliTick / 2;
					GlStateManager.pushMatrix();
					if (up) {
						part = (float) (tick - (milliTick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(2));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					} else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 2;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // open
				case 2: {
					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					if (temp > 0) {
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(1));
						this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					}
					GlStateManager.popMatrix();

					// left
					boolean up = tick >= milliTick / 2;
					GlStateManager.pushMatrix();
					if (up) {
						part = (float) (tick - (milliTick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					} else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();

					if (tick == milliTick) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
								(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
								0.8f + 0.4f * this.rnd.nextFloat());
					}
					if (tick == 0) {
						if (temp < 3) {
							temp++;
							step = 2;
							tick = 11;
							milliTick = 10;
						} else {
							temp = 0;
							step = 3;
							tick = 21;
							milliTick = 20;
						}
						GlStateManager.disableBlend();
					}
					break;
				} // open lists
				case 3: {
					// Tabs
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft + 10, guiTop + (1.0f - cos) * 28.0f, 0.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f, 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f, 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					if (CustomNpcs.ShowQuestCompass) {
						GlStateManager.translate(-114.0f + 256.0f * scaleW, 0.0f, 0.0f);
						this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					}
					GlStateManager.popMatrix();
					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(1));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					GlStateManager.popMatrix();

					if (tick == 0) {
						step = type + 4;
						tick = 21;
						milliTick = 20;
						GlStateManager.disableBlend();
					}
					break;
				} // tab open
				case 4: {
					drawBox(mouseX, mouseY);
					if (!categories.isEmpty()) {
						GlStateManager.pushMatrix();
						GlStateManager.translate(guiLeft, guiTopLog + 23.5f * scaleH, 0.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
						int i = 0, p = 0;
						for (String catName : categories.keySet()) {
							int catW = (int) ((this.fontRenderer.getStringWidth(catName) + 10 + i) * cos);
							this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
							if (isMouseHover(mouseX, mouseY, guiLeft + (int) ((5 - catW) * scaleW),
									(int) (guiTopLog + (23.5f + i * 16.0f) * scaleH), (int) (catW * scaleH),
									(int) (16.0f * scaleH))) {
								hoverButton = 7 + i;
							} // 7/15-tab categories;
							GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
							GlStateManager.pushMatrix();
							GlStateManager.scale(scaleW, scaleH, 1.0f);
							this.drawTexturedModalRect(4 - (int) (catW / scaleW) + i, i * 16, 0,
									90 + (catSelect == p || hoverButton == 7 + i ? 0 : 16), (int) (catW / scaleW), 16);
							GlStateManager.popMatrix();
							StringBuilder name = new StringBuilder();
							for (int j = 0; j < catName.length(); j++) {
								if (this.fontRenderer.getStringWidth(name.toString() + catName.charAt(j)) > catW - 5) {
									break;
								}
								name.append(catName.charAt(j));
							}
							this.fontRenderer.drawString(name.toString(), 4 - catW + 10 + i,
									(16.0f * scaleH - 10.0f) / 2.0f + (i * 16) * scaleH, CustomNpcs.QuestLogColor.getRGB(),
									false);
							i++;
							p++;
							if (i >= 8) {
								break;
							}
						}
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						GlStateManager.popMatrix();
					} else {
						tick = 0;
					}
					if (tick == 0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // quest tab open
				case 5: {
					drawBox(mouseX, mouseY);
					if (tick == 0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // faction open
				case 6: {
					drawBox(mouseX, mouseY);
					if (tick == 0) {
						step = toPrePage ? 10 : 11;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // compass open
				case 7: {
					drawBox(mouseX, mouseY);
					if (!categories.isEmpty()) {
						temp = 1;
						GlStateManager.pushMatrix();
						GlStateManager.translate(guiLeft, guiTopLog + 7.5f, 0.0f);
						GlStateManager.translate(0.0f, 16.0f, 0.0f);
						int i = 0, p = 0, st = catRow * 8;
						for (String catName : categories.keySet()) {
							if (p < st) {
								p++;
								continue;
							}
							int catW = (int) ((this.fontRenderer.getStringWidth(catName) + 10) * (1.0f - cos));
							this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
							this.drawTexturedModalRect(5 - catW, i * 16, 0, 90 + (catSelect == p ? 0 : 16), catW, 16);
							StringBuilder name = new StringBuilder();
							for (int j = 0; j < catName.length(); j++) {
								if (this.fontRenderer.getStringWidth(name.toString() + catName.charAt(j)) > catW - 5) {
									break;
								}
								name.append(catName.charAt(j));
							}
							this.fontRenderer.drawString(name.toString(), 10 - catW, 3 + i * 16, CustomNpcs.QuestLogColor.getRGB(),
									false);
							GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
							i++;
							if (i >= 8) {
								break;
							}
						}
						GlStateManager.popMatrix();
					} else {
						tick = 0;
						temp = 0;
					}

					if (tick == 0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
						} else {
							step = type + 4;
						}
						tick = 21;
						milliTick = 20;
					}
					break;
				} // quest tab close
				case 8: {
					drawBox(mouseX, mouseY);

					if (tick == 0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
						} else {
							step = type + 4;
						}
						tick = 21;
						milliTick = 20;
					}
					break;
				} // faction close
				case 9: {
					drawBox(mouseX, mouseY);

					if (tick == 0) {
						GlStateManager.disableBlend();
						if (type < 0) {
							step = 12;
						} else {
							step = type + 4;
						}
						tick = 21;
						milliTick = 20;
					}
					break;
				} // compass close
				case 10: {
					drawBox(mouseX, mouseY);
					boolean up = tick >= milliTick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					if (up) {
						part = (float) (tick - (milliTick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					} else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter - cos * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();

					if (tick == milliTick) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
								(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
								0.8f + 0.4f * this.rnd.nextFloat());
					}
					if (tick == 0) {
						step = -1;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // next page
				case 11: {
					drawBox(mouseX, mouseY);
					boolean up = tick >= milliTick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					if (up) {
						part = (float) (tick - (milliTick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter - (1.0f - cos) * width / 2.0f, guiTopLog, 0.0f);
						GlStateManager.scale((1.0f - cos), 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					} else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(guiCenter, guiTopLog, 0.0f);
						GlStateManager.scale(cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(3));
						this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					}
					GlStateManager.popMatrix();

					if (tick == milliTick) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
								(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
								0.8f + 0.4f * this.rnd.nextFloat());
					}
					if (tick == 0) {
						step = -1;
						tick = 11;
						milliTick = 10;
						GlStateManager.disableBlend();
					}
					break;
				} // pre page
				case 12: {
					// Tabs
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft + 10, guiTop + cos * 28.0f, 0.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(4));
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f, 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					GlStateManager.translate(33.0f, 0.0f, 0.0f);
					this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					if (CustomNpcs.ShowQuestCompass) {
						GlStateManager.translate(-114.0f + 256.0f * scaleW, 0.0f, 0.0f);
						this.drawTexturedModalRect(0, 0, 0, 30, 28, 30);
					}
					GlStateManager.popMatrix();

					// place
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTopLog, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(1));
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 175);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 13;
						tick = 21;
						milliTick = 20;
						GlStateManager.disableBlend();
					}
					break;
				} // close tabs
				case 13: {
					// left
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f * cos, guiTopLog, 0.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.drawTexturedModalRect(0, 0, 128, 0, 128, 175);
					GlStateManager.popMatrix();

					// right
					boolean up = tick >= milliTick / 2;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f * cos, guiTopLog, 0.0f);
					if (up) {
						part = (float) (tick - (milliTick / 2)) + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.translate(-128.0f * (1.0d - cos), 0.0f, 0.0f);
						GlStateManager.scale(1.0f - cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(0));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
						if (temp > 0) {
							this.mc.getTextureManager().bindTexture(GuiLog.ql.get(1));
							this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
						}
					} else {
						part = (float) tick + partialTicks;
						cos = (float) Math.cos(90.0d * part / ((double) milliTick / 2.0d) * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						GlStateManager.scale(cos, 1.0f, 1.0f);
						GlStateManager.scale(scaleW, scaleH, 1.0f);
						this.mc.getTextureManager().bindTexture(GuiLog.ql.get(2));
						this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					}
					GlStateManager.popMatrix();

					if (tick == 0) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.down",
								(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
								0.75f + 0.25f * this.rnd.nextFloat());
						step = 14;
						tick = 21;
						milliTick = 20;
						GlStateManager.disableBlend();
					}
					break;
				} // close book
				case 14: {
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiCenter - 64.0f + cos * (guiCenter + 50.0f), guiTopLog + cos * 250.0f, 0.0f);
					GlStateManager.scale(scaleW, scaleH, 1.0f);
					this.mc.getTextureManager().bindTexture(GuiLog.ql.get(2));
					this.drawTexturedModalRect(0, 0, 0, 0, 128, 175);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 14;
						tick = 101;
						milliTick = 100;
						save();
						if (type == -1) {
							mc.displayGuiScreen(new GuiInventory(player));
						} else {
							displayGuiScreen(null);
							mc.setIngameFocus();
						}
						GlStateManager.disableBlend();
					}
					break;
				} // close
			}
			tick--;
			if (step != -1) {
				GlStateManager.popMatrix();
				return;
			}
		}
		drawBox(mouseX, mouseY);
		GlStateManager.popMatrix();

		if (tick < 0 && step == -1) {
			GlStateManager.pushMatrix();
			super.drawScreen(mouseX, mouseY, partialTicks);
			GlStateManager.popMatrix();
		}
		if (type == 2) {
			if (this.getTextField(0) != null && this.getTextField(0).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.upos");
			} else if (this.getTextField(1) != null && this.getTextField(1).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.vpos");
			} else if (this.getButton(0) != null && this.getButton(0).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.showname");
			} else if (this.getButton(1) != null && this.getButton(1).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.showtask");
			} else if (this.getSlider(0) != null && this.getSlider(0).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.scale");
			} else if (this.getSlider(1) != null && this.getSlider(1).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.incline");
			} else if (this.getSlider(2) != null && this.getSlider(2).isHovered()) {
				this.setHoverText("quest.hover.compass.edit.rotation");
			}
		}
		drawHoverText(null);
	}

	protected boolean hoverMob(int mouseX, int mouseY, Entity entity) {
		if (entity == null) {
			return false;
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate((guiLeft + 22) * -1, guiTopLog * -1, 300.0d);
		GlStateManager.translate(mouseX, mouseY, 0.0f);

		if (mouseY > sw.getScaledHeight_double() / 2.0d) {
			GlStateManager.translate(0.0f, -15.0f, 0.0f);
		} else {
			GlStateManager.translate(0.0f, 45.0f, 0.0f);
		}

		String modelName = "";
		if (entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).display.getModel() != null) {
			modelName = ((EntityNPCInterface) entity).display.getModel();
		} else {
			ResourceLocation location = EntityList.getKey(entity);
			if (location != null) {
				modelName = location.toString();
			}
		}
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.rotate((mc.world.getTotalWorldTime() % 360) * 5.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		entity.ticksExisted = 1;
		if (canUpdate) {
			entity.onUpdate();
		}
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.popMatrix();
		return true;
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(this.mc);
		scaleW = ((float) sw.getScaledWidth_double() - 160.0f) / 256.0f;
		scaleH = ((float) sw.getScaledHeight_double() - 78.0f) / 175.0f;
		guiCenter = (int) Math.ceil(sw.getScaledWidth_double() / 2.0d + 15.0d * scaleW);
		width = (int) (256.0f * scaleW);
		height = (int) (203.0f * scaleH);
		guiLeft = guiCenter - (int) (128.0f * scaleW);
		guiTop = 25;
		guiTopLog = guiTop + 28;
		guiLLeft = guiLeft + (int) (26.0f * scaleW);
		guiLRight = guiCenter + (int) (2.0f * scaleW);
		guiLTop = guiTopLog + (int) (8.0f * scaleH);

		// Quests
		if (type == 0) {
			quests.clear();
			categories.clear();
			Collection<QuestData> list = CustomNpcs.proxy.getPlayerData(player).questData.activeQuests.values();
			// Quest List
			if (!list.isEmpty()) {
				for (QuestData qd : list) {
					Quest quest = qd.quest;
					String catName = quest.category.getName();
					if (!categories.containsKey(catName)) {
						int r = 128, g = 32, b = 224;
						for (int i = 0; i < catName.length(); i++) {
							switch (i % 3) {
							case 0:
								r += catName.charAt(i);
								break;
							case 1:
								g += catName.charAt(i);
								break;
							case 2:
								b += catName.charAt(i);
								break;
							}
						}
						this.categories.put(catName, new Color((r * catName.length()) % 256,
								(g * catName.length()) % 256, (b * catName.length()) % 256));
					}
					if (!quests.containsKey(catName)) {
						quests.put(catName, new TreeMap<>());
					}
					quests.get(catName).put(quest.id, qd);
				}
			}
			if (activeQuest != null) {
				activeQuest.reset();
			}
			while (catSelect >= categories.size()) {
				catSelect--;
			}
		}
		// Factions
		else if (type == 2) {
			int x0 = guiLLeft + 8;
			int x1 = guiLRight + (int) (3.0f * scaleW);
			int y = (int) (guiLTop + 82.0f * scaleH);
			// Screen Pos
			this.addTextField(new GuiNpcTextField(0, this, fontRenderer, x0, y, (int) (40.0f * scaleW),
					(int) (10.0f * scaleH), "" + this.compassData.screenPos[0]));
			this.getTextField(0).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[0]);

			this.addTextField(new GuiNpcTextField(1, this, fontRenderer, x0 + (int) (54.0f * scaleW), y,
					(int) (40.0f * scaleW), (int) (10.0f * scaleH), "" + this.compassData.screenPos[1]));
			this.getTextField(1).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[1]);

			// Scale
			x0 -= 1;
			float v = this.compassData.scale - 0.5f;
			this.addSlider(new GuiNpcSlider(this, 0, x0, y += (int) (17.0f * scaleH), (int) (96.0f * scaleW),
					(int) (12.0f * scaleH), v));
			this.getSlider(0).setString(("" + this.compassData.scale).replace(".", ","));

			// Incline
			v = this.compassData.incline * -0.022222f + 0.5f;
			this.addSlider(new GuiNpcSlider(this, 1, x0, y += (int) (16.0f * scaleH), (int) (96.0f * scaleW),
					(int) (12.0f * scaleH), v));
			this.getSlider(1).setString(("" + (45.0f + this.compassData.incline * -1.0f)).replace(".", ","));
			addButton(new GuiNpcCheckBox(0, x1, y - (int) scaleH, (int) (100.0f * scaleW), (int) (12.0f * scaleH), "quest.screen.show.quest", null, compassData.showQuestName));
			getButton(0).setTextColor(CustomNpcs.QuestLogColor.getRGB());

			// Rotation
			v = this.compassData.rot * 0.016667f + 0.5f;
			this.addSlider(new GuiNpcSlider(this, 2, x0, y += (int) (16.0f * scaleH), (int) (96.0f * scaleW),
					(int) (12.0f * scaleH), v));
			this.getSlider(2).setString(("" + this.compassData.rot).replace(".", ","));
			addButton(new GuiNpcCheckBox(1, x1, y - (int) scaleH, (int) (100.0f * scaleW), (int) (12.0f * scaleH), "quest.screen.show.task", null, compassData.showTaskProgress));
			getButton(1).setTextColor(CustomNpcs.QuestLogColor.getRGB());
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (step >= 0) {
			return;
		}
		if (i == 1 || i == mc.gameSettings.keyBindInventory.getKeyCode()) {
			tick = 15;
			milliTick = 15;
			step = type + 7;
			type = i == 1 ? -2 : -1;
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		mouseX = this.mouseX;
		mouseY = this.mouseY;
		if (step >= 0) {
			return;
		}
		if (type == 2) {
			super.mouseClicked(mouseX, mouseY, mouseBottom);
			if (mouseX >= (int) (guiLLeft - 3.0f * scaleW) && mouseX <= (int) (guiLLeft + 100.0f * scaleW)
					&& mouseY >= guiLTop + 10 && mouseY <= (int) (guiLTop + 10 + (69.0f * scaleH))) {
				double x = (mouseX - (int) (guiLLeft - 3.0f * scaleW)) / scaleW;
				double y = (mouseY - (guiLTop + 10)) / scaleH;
				this.compassData.screenPos[0] = Math.round(x / 103.0d * 1000.0d) / 1000.0d;
				this.compassData.screenPos[1] = Math.round(y / 69.0d * 1000.0d) / 1000.0d;
				initGui();
			}
		}
		this.buttonPress(hoverButton);
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		if (type != 2) {
			return;
		}
		switch (slider.getID()) {
			case 0: {
				this.compassData.scale = Math.round((slider.getSliderValue() + 0.5f) * 100.0f) / 100.0f;
				slider.setString(("" + this.compassData.scale).replace(".", ","));
				break;
			}
			case 1: {
				this.compassData.incline = Math.round((-45.0f * slider.getSliderValue() + 22.5f) * 100.0f) / 100.0f;
				slider.setString(("" + (45.0f + this.compassData.incline * -1.0f)).replace(".", ","));
				break;
			}
			case 2: {
				this.compassData.rot = Math.round((60.0f * slider.getSliderValue() - 30.0f) * 100.0f) / 100.0f;
				slider.setString(("" + this.compassData.rot).replace(".", ","));
				break;
			}
		}
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(IGuiNpcSlider slider) {
	}

	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.SaveCompassData, this.compassData.getNbt());
	}

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
					if (faction2.id == id) {
						faction2.defaultPoints = points;
					}
				}
			}
		}
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (type != 2) {
			return;
		}
		switch (textField.getID()) {
			case 0: {
				this.compassData.screenPos[0] = Math.round(textField.getDouble() * 100.0d) / 100.0d;
				break;
			}
			case 1: {
				this.compassData.screenPos[1] = Math.round(textField.getDouble() * 100.0d) / 100.0d;
				break;
			}
		}
	}

}
