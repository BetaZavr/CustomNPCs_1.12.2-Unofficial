package noppes.npcs.client.gui.player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import noppes.npcs.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerSkinController;
import noppes.npcs.controllers.data.DialogGuiSettings;
import noppes.npcs.entity.EntityCustomNpc;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;
import org.lwjgl.opengl.GL11;

public class GuiDialogInteract
extends GuiNPCInterface
implements IGuiClose {

	protected Dialog dialog;
	protected int selected = 0;
	protected final List<TextBlockClient> lines = new ArrayList<>();
	protected int dialogHeight = 180;
	protected final ResourceLocation wheel = new ResourceLocation(CustomNpcs.MODID, "textures/gui/wheel.png");
	protected boolean isGrabbed = false;

	// New from Unofficial (BetaZavr)
	public static class DialogTexture {

		public ResourceLocation res;
		public int left, uS, vS, height;
		public TextBlockClient line;

		protected DialogTexture(ResourceLocation r, int[] uv, TextBlockClient tb) {
			res = r;
			left = 0;
			uS = uv[0];
			vS = uv[1];
			height = uv[2];
			line = tb;
		}

	}
	public static Map<Integer, ResourceLocation> icons;
	static {
		icons = new LinkedHashMap<>();
		icons.put(1, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/important.png"));
		icons.put(2, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/question.png"));
		icons.put(3, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/circle.png"));
		icons.put(4, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/joke.png"));
		icons.put(5, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/triangle.png"));
		icons.put(6, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/square.png"));
		icons.put(7, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/hexagon.png"));
		icons.put(8, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/dice.png"));
	}
	protected final ResourceLocation npcSkin;
	protected final ResourceLocation playerSkin;
	protected final Map<Integer, List<String>> options = new TreeMap<>(); // [slotID, text]
	// dialog place
	protected int lineStart = 0;
	protected int lineTotal = 0;
	protected int lineVisibleSize;
	protected int[] scrollD = null;
	// option place
	protected int selectedStart = 0;
	protected int selectedSize = 0;
	protected int selectedVisibleSize;
	protected final Map<Integer, Integer> selectedTotal = new HashMap<>();
	protected int[] scrollO = null;
	// wheel option
	protected int wheelList = 0;
	protected int selectedX = 0;
	protected int selectedY = 0;
	protected int selectedWheel = 0;
    // textures
	protected long waitToAnswer;
	protected final Map<Integer, DialogTexture> textures = new HashMap<>();
	// Display
	protected final EntityNPCInterface dialogNpc;
	protected int fontHeight;
	protected int startLine;
	protected long startTime;
	protected final float corr;
	private final DialogGuiSettings guiSettings;
	ScaledResolution sw = new ScaledResolution(mc);

	protected boolean showOptions;
	protected boolean newDialogSet;

	public GuiDialogInteract(EntityNPCInterface npc, Dialog dialogIn) {
		super(npc);
		drawDefaultBackground = false;
		ySize = 238;

		dialogNpc = Util.instance.copyToGUI(npc, mc.world, false);
		dialogNpc.display.setVisible(0);
		dialog = dialogIn;

		guiSettings = DialogController.instance.getGuiSettings();
		corr = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().equals("ru_ru") ? 1.14725f : 1.0f;

		playerSkin = PlayerSkinController.getInstance().playerTextures.get(player.getUniqueID()).get(MinecraftProfileTexture.Type.SKIN);

		if (npc.display.skinType == 0) { npcSkin = new ResourceLocation(npc.display.getSkinTexture()); }
		else {
			ResourceLocation skin = DefaultPlayerSkin.getDefaultSkinLegacy();
			if (npc.display.skinType == 1) {
				if (npc.display.playerProfile == null) { npc.display.loadProfile(); }
				if (npc.display.playerProfile != null) {
					PlayerSkinController pData = PlayerSkinController.getInstance();
					Map<MinecraftProfileTexture.Type, ResourceLocation> map = pData.getData(npc.display.playerProfile.getId());
					if (map != null && map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
						skin = map.get(MinecraftProfileTexture.Type.SKIN);
					}
					else {
						Minecraft minecraft = Minecraft.getMinecraft();
						Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> mapMC = minecraft.getSkinManager().loadSkinFromCache(npc.display.playerProfile);
						if (mapMC.containsKey(MinecraftProfileTexture.Type.SKIN)) {
							skin = minecraft.getSkinManager().loadSkin(mapMC.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
						}
					}
				}
			}
			else if (npc.display.skinType == 2) {
				try {
					boolean fixSkin = npc instanceof EntityCustomNpc && ((EntityCustomNpc)npc).modelData.getEntity(npc) == null;
					skin = new ResourceLocation(CustomNpcs.MODID, "skins/" + (npc.display.getSkinUrl() + fixSkin).hashCode() + (fixSkin ? "" : "32"));
				}
				catch (Exception e) { LogWriter.error(e); }
			}
			npcSkin = skin;
		}

        initGui();
		appendDialog(dialogIn);
	}

	public void appendDialog(Dialog d) {
		closeOnEsc = !d.disableEsc;
		newDialogSet = true;
		Dialog oldDialog = dialog;
		dialog = d.copy();
		options.clear();
		selected = 0;
		selectedStart = 0;
		// Old Sound
		MusicController.Instance.stopSound(null, SoundCategory.VOICE);
		if (d.sound != null && !d.sound.isEmpty()) {
			BlockPos pos = dialogNpc.getPosition();
			if (oldDialog != null && !oldDialog.equals(d) && oldDialog.sound != null && !oldDialog.sound.isEmpty() && MusicController.Instance.isPlaying(oldDialog.sound)) {
				MusicController.Instance.stopSound(oldDialog.sound, SoundCategory.VOICE);
			}
			boolean isPlay = MusicController.Instance.isPlaying(d.sound);
			if (isPlay) {
				MusicController.Instance.stopSound(d.sound, SoundCategory.VOICE);
			}
			CustomNPCsScheduler.runTack(() -> MusicController.Instance.playSound(SoundCategory.VOICE, d.sound, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f) , 50);
		}
		// Dialog texts
		startTime = System.currentTimeMillis();
		setStartLine();
		StringBuilder dText = new StringBuilder(new TextComponentTranslation(d.text).getFormattedText());
		while (dText.toString().contains("<br>")) { dText = new StringBuilder(dText.toString().replace("<br>", "" + ((char) 10))); }
		while (dText.toString().contains("\\n")) { dText = new StringBuilder(dText.toString().replace("\\n", "" + ((char) 10))); }
		ResourceLocation txtr = null;
		int[] txtrSize = null;
		if (!d.texture.isEmpty()) {
			txtr = new ResourceLocation(d.texture);
			mc.getTextureManager().bindTexture(txtr);
			try {
				IResource res = mc.getResourceManager().getResource(new ResourceLocation(d.texture));
				BufferedImage buffer = ImageIO.read(res.getInputStream());
				txtrSize = new int[] { buffer.getWidth(), buffer.getHeight(),
						(int) Math.ceil((double) buffer.getHeight() / (double) fontHeight / 2.0d) };
			} catch (IOException e) { LogWriter.error(e); }
		}
		if (txtrSize != null) {
			for (int i = 0; i < txtrSize[2]; i++) {
				dText.append("" + ((char) 10));
			}
		}

		int textWidth = guiSettings.dialogWidth - (int) (13.0f * corr);
		lines.add(new TextBlockClient(dialogNpc, dText.toString(), textWidth, 0xE0E0E0, dialogNpc, player, npc));
		if (!d.showFits) { setStartLine(); }
		// Dialog options
		resetOptions();
		grabMouse(d.showWheel);
		waitToAnswer = dialog != null ? System.currentTimeMillis() + dialog.delay * 50L : 0L;
		lineTotal = 0;
		for (TextBlockClient textBlock : lines) { lineTotal += 1 + textBlock.lines.size(); }
		selectedTotal.clear();
		int i = 0;
		selectedSize = 0;
		for (int id : options.keySet()) {
			selectedTotal.put(i, options.get(id).size());
			selectedSize += options.get(id).size();
			i++;
		}
		lineVisibleSize = dialogHeight / fontHeight;
		selectedVisibleSize = Math.round((float) (height - dialogHeight - 2) / (float) fontHeight);
		selected = 0;
		selectedStart = 0;
		if (lineStart < 0) {
			lineStart = 0;
		}
		// Dialog texture
		if (txtr != null && txtrSize != null && !textures.containsKey(lineTotal - 1 - txtrSize[2])) { textures.put(lineTotal - 1 - txtrSize[2], new DialogTexture(txtr, txtrSize, lines.get(lines.size() - 1))); }
		initGui();
	}

	protected void checkSelected() {
		int selSize = 0, pos = 1;
		for (int i = selectedTotal.size() - 1; i > 0; i--) {
			selSize += selectedTotal.get(i);
			if (selSize >= selectedVisibleSize) { break; }
			pos++;
		}
		if (selectedStart <= 0) {
			selectedStart = 0;
			if (scrollO != null) {
				scrollO[8] = selectedStart;
				scrollO[7] = mouseY;
			}
		}
		else if (selectedStart >= selectedTotal.size() - pos) {
			selectedStart = selectedTotal.size() - pos;
			if (scrollO != null) {
				scrollO[8] = selectedStart;
				scrollO[7] = mouseY;
			}
		}
	}

	@Override
	public void close() {
		grabMouse(false);
		if (dialog.sound != null && !dialog.sound.isEmpty() && dialog.stopSound) {
			if (MusicController.Instance.isPlaying(dialog.sound)) {
				MusicController.Instance.stopSound(dialog.sound, SoundCategory.VOICE);
			}
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
		super.close();
	}

	@Override
	public void save() {
		ClientProxy.playerData.dialogData.addLogs(lines, npcSkin.toString());
		NBTTagCompound compound = new NBTTagCompound();
		ClientProxy.playerData.dialogData.saveNBTData(compound);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.PlayerDialogData, compound);
	}

	protected void drawLinedOptions() {
		int i = 0;
		int optPos = 0;
		int endW = guiSettings.guiRight - 1;
		int left = guiLeft + 24;
		int addX = 0;
		// scroll bar
		if (selectedSize > selectedVisibleSize && !dialog.showWheel) { left += 13; }
		if (selectedSize > selectedVisibleSize) {
			endW -= 13;
			if (!dialog.showWheel) { left += 13; }
			if (guiSettings.getType() == 2) { addX = 13; }
			else { left -= 12; }
		}
		for (int id : options.keySet()) {
			if (optPos < selectedStart) {
				optPos++;
				continue;
			}
			DialogOption option = dialog.options.get(id);
			List<String> lines = options.get(id);
			int j = 0;
			for (String sct : lines) {
				if (j == 0) {
					drawString(fontRenderer, optPos == selected ? "->" : " *", guiLeft + 1 + (icons.containsKey(option.iconId) ? 0 : 10) + addX, dialogHeight + i * fontHeight, guiSettings.pointerColor);
					if (i != 0 && optPos - 1 != selected) {
						drawHorizontalLine(guiLeft + 2 + addX, endW + addX, dialogHeight + i * fontHeight, guiSettings.scrollLineColor);
					}
					if (selected == optPos) {
						drawHorizontalLine(left, endW + addX, dialogHeight + i * fontHeight, guiSettings.hoverLineColor);
					}
					if (icons.containsKey(option.iconId)) {
						mc.getTextureManager().bindTexture(icons.get(option.iconId));
						GlStateManager.pushMatrix();
						GlStateManager.translate(guiLeft + 11.5f + addX, dialogHeight + i * fontHeight + 1.0f, 0.0f);
						GlStateManager.color((float) (option.optionColor >> 16 & 255) / 255.0F, (float) (option.optionColor >> 8 & 255) / 255.0F, (float) (option.optionColor & 255) / 255.0F, 1.0F);
						float s = 12.0f / 256.0f;
						GlStateManager.scale(s, s, s);
						drawTexturedModalRect(0, 0, 0, 0, 256, 256);
						GlStateManager.popMatrix();
					}
				}
				// select
				if (selected == optPos) {
					fill(left, dialogHeight + i * fontHeight + (j == 0 ? 1 : 0), endW + addX, dialogHeight + (i + 1) * fontHeight, zLevel, guiSettings.selectOptionLeftColor, guiSettings.selectOptionRightColor);
				}
				// option
				drawString(fontRenderer, sct, left, dialogHeight + i * fontHeight, option.optionColor);
				i++;
				j++;
				if (j == lines.size()) {
					drawHorizontalLine(guiLeft + 2 + addX, endW + addX, dialogHeight + i * fontHeight, guiSettings.scrollLineColor);
					if (selected == optPos) { drawHorizontalLine(left, endW + addX, dialogHeight + i * fontHeight, guiSettings.hoverLineColor); }
				}
			}
			optPos++;
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// background
		int vType = guiSettings.getShowVerticalLines();
		int hType = guiSettings.getShowVerticalLines();
		int blurringLine = (int) (guiSettings.dialogWidth * guiSettings.getBlurringLine());
		if (guiSettings.backTexture != null) {
			mc.getTextureManager().bindTexture(guiSettings.backTexture);
			GlStateManager.pushMatrix();
			GlStateManager.scale(width / 256.0f, height / 256.0f, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
		}
		if (guiSettings.windowTexture != null) {
			mc.getTextureManager().bindTexture(guiSettings.windowTexture);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiSettings.guiLeft, 0.0f, 0.0f);
			GlStateManager.scale((float) guiSettings.dialogWidth / 256.0f, height / 256.0f, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
		}
		switch (guiSettings.getType()) {
			case 0: {
				// dialogs
				int left = guiLeft + blurringLine;
				if (blurringLine > 0) { fill(guiLeft, 0, left, height, zLevel, guiSettings.backBorderColor, guiSettings.backWindowColor); }
				fill(left, 0, width, height, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
				// border
				fill(0, 0, guiLeft, height, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
				// lines
				if (vType != 0) {
					int lX = guiLeft - 1;
					if (hType == 2) {
						drawVerticalLine(lX, 0, dialogHeight - 2, guiSettings.linesColor);
						drawVerticalLine(lX, dialogHeight - 2, height - 2, guiSettings.linesColor);
						lX = guiLeft - 3;
					}
					else if (hType == 1) {
						drawVerticalLine(lX, 0, height - 2, guiSettings.linesColor);
						lX = guiLeft - 3;
					}
					if (vType == 2) { drawVerticalLine(lX, 1, height - 2, guiSettings.linesColor); }
				}
				if (hType == 2) {
					drawHorizontalLine(guiLeft, width - 3, dialogHeight - 3, guiSettings.linesColor);
					drawHorizontalLine(guiLeft, width - 3, dialogHeight - 1, guiSettings.linesColor);
				}
				else if (hType == 1) { drawHorizontalLine(guiLeft, width - 3, dialogHeight - 2, guiSettings.linesColor); }
				break;
			} // right
			case 1: {
				// dialogs
				int left = guiLeft + blurringLine;
				int right = guiSettings.guiRight - blurringLine;
				if (blurringLine > 0) { fill(guiLeft, 0, left, height, zLevel, guiSettings.backBorderColor, guiSettings.backWindowColor); }
				fill(left, 0, right, height, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
				if (blurringLine > 0) { fill(right, 0, guiSettings.guiRight, height, zLevel, guiSettings.backWindowColor, guiSettings.backBorderColor); }
				// border
				fill(0, 0, guiLeft, height, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
				fill(guiSettings.guiRight, 0, width, height, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
				// lines
				if (vType != 0) {
					int lX = guiSettings.guiLeft - 1;
					int rX = guiSettings.guiRight + 1;
					if (hType == 2) {
						// left
						drawVerticalLine(lX, 0, dialogHeight - 2, guiSettings.linesColor);
						drawVerticalLine(lX, dialogHeight - 2, height - 2, guiSettings.linesColor);
						// right
						drawVerticalLine(rX, 0, dialogHeight - 2, guiSettings.linesColor);
						drawVerticalLine(rX, dialogHeight - 2, height - 2, guiSettings.linesColor);
						lX = guiLeft - 3;
						rX += 2;
					}
					else if (hType == 1) {
						// left
						drawVerticalLine(lX, 0, height - 2, guiSettings.linesColor);
						// right
						drawVerticalLine(rX, 0, height - 2, guiSettings.linesColor);
						lX = guiLeft - 3;
						rX += 2;
					}
					if (vType == 2) {
						// left
						drawVerticalLine(lX, 0, height - 2, guiSettings.linesColor);
						// right
						drawVerticalLine(rX, 0, height - 2, guiSettings.linesColor);
					}
				}
				if (hType == 2) {
					drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 3, guiSettings.linesColor);
					drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 1, guiSettings.linesColor);
				}
				else if (hType == 1) { drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 2, guiSettings.linesColor); }
				break;
			} // center
			case 2: {
				// dialogs
				int right = guiSettings.guiRight - blurringLine;
				fill(0, 0, right, height, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
				if (blurringLine > 0) { fill(right, 0, guiSettings.dialogWidth, height, zLevel, guiSettings.backWindowColor, guiSettings.backBorderColor); }
				// border
				fill(guiSettings.dialogWidth, 0, width, height, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
				// lines
				if (vType != 0) {
					int rX = guiSettings.guiRight + 1;
					if (hType == 2) {
						drawVerticalLine(rX, 0, dialogHeight - 2, guiSettings.linesColor);
						drawVerticalLine(rX, dialogHeight - 2, height - 2, guiSettings.linesColor);
						rX += 2;
					}
					else if (hType == 1) {
						drawVerticalLine(rX, 0, height - 2, guiSettings.linesColor);
						rX += 2;
					}
					if (vType == 2) {
						drawVerticalLine(rX, 0, height - 2, guiSettings.linesColor);
					}
				}
				if (hType == 2) {
					drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 3, guiSettings.linesColor);
					drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 1, guiSettings.linesColor);
				}
				else if (hType == 1) { drawHorizontalLine(guiLeft, guiSettings.guiRight, dialogHeight - 2, guiSettings.linesColor); }
				break;
			} // left
		}
		// NPC
		if (guiSettings.showNPC && !dialog.hideNPC && dialogNpc != null) {
			drawNpc(dialogNpc, guiSettings.npcPosX, guiSettings.npcPosY, guiSettings.getNpcScale(),
					guiSettings.getType() == 2 || (guiSettings.getType() == 1 && !guiSettings.npcInLeft) ? 40 : -40,
					15, dialog.showWheel ? 0 : 2);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (guiSettings.backTexture != null) {
			mc.getTextureManager().bindTexture(guiSettings.backTexture);
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.66796875f, 0.9375f, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
		}
		if (guiSettings.windowTexture != null) {
			mc.getTextureManager().bindTexture(guiSettings.windowTexture);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiSettings.guiLeft, 0.0f, 0.0f);
			GlStateManager.scale(guiSettings.dialogWidth / 427.0f * 1.66796875f, 0.9375f, 1.0f);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
		}
		// Dialog text lines
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.5f, 100.065f);
		int time = 1 + (int) ((System.currentTimeMillis() - startTime) / (1000L / CustomNpcs.DialogShowFitsSpeed));
		int l = 0;
		showOptions = true;
		GlStateManager.pushMatrix();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		int i = mc.displayHeight;
		double d4 = sw.getScaledWidth() < mc.displayWidth  ? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth())  : 1;
		double d5 = (double) guiLeft * d4;
		double d6 = (double) i - (double) (dialogHeight - 3) * d4;
		double d7 = (double) (guiSettings.dialogWidth + 1) * d4;
		double d8 = (double) (dialogHeight - 3) * d4;
		GL11.glScissor((int)d5, (int)d6, Math.max(0, (int)d7), Math.max(0, (int)d8));
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (TextBlockClient textBlock : new ArrayList<>(lines)) {
			int left = ClientProxy.Font.width(textBlock.getName() + ": ");
			if (l >= lineStart) { drawString(textBlock.getName() + ": ", 0, textBlock.color, l); }
			for (ITextComponent line : textBlock.lines) {
				if (newDialogSet && l >= lineStart + lineVisibleSize) { lineStart = l - lineVisibleSize + 1; }
				if (l < lineStart) { ++l; continue; }
				if (dialog.showFits && startLine == l) {
					String text = line.getFormattedText();
					while (text.endsWith(((char) 167) + "r")) { text = text.substring(0, text.length() - 2); }
					while (text.startsWith(((char) 167) + "r")) { text = text.substring(2); }
					if (text.length() >= time) {
						drawString(text.substring(0, time), left, textBlock.color, l);
						if (text.length() == time) {
							startLine++;
							startTime = System.currentTimeMillis();
						}
						showOptions = false;
						break;
					}
				}
				drawString(line.getFormattedText(), left, textBlock.color, l);
				++l;
			}
			++l;
		}
		// next any
		GlStateManager.popMatrix();
		if (!textures.isEmpty()) {
			for (int linePos : textures.keySet()) {
				DialogTexture dt = textures.get(linePos);
				if (dt.left < guiLeft) {
					continue;
				}
				int tys = fontHeight * (linePos - lineStart);
				int tye = tys + dt.vS / 2;
				if (tye >= 0 && tys <= dialogHeight - 4) {
					int s = (tys < 0 ? -1 * tys : 0) * 2;
					int e = 256 - s;
					int pH = Math.max(tys, 0);
					int v = 0;
					if (pH == 0) {
						v = 1;
						pH = 1;
						s += 1;
					}
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					GlStateManager.enableBlend();
					mc.getTextureManager().bindTexture(dt.res);
					GlStateManager.translate(dt.left, pH, 0.0f);
					float sc = (Math.max(dt.uS, dt.vS)) / 256.0f;
					GlStateManager.scale(sc / 2.0f, sc / 2.0f, 1.0f);
					if (pH + (e * sc) / 2 > dialogHeight - 4) {
						e = (int) ((float) ((dialogHeight - 4 - pH) * 2) / sc);
					}
					drawTexturedModalRect(0, v, 0, s, 256, e);
					GlStateManager.popMatrix();
				}
			}
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GlStateManager.popMatrix();
		if (!options.isEmpty()) {
			if (waitToAnswer > System.currentTimeMillis()) {
				int offset = dialogHeight;
				drawString(fontRenderer, ((char) 167) + "e" + new TextComponentTranslation("gui.wait", ((char) 167) + "e: " + ((char) 167) + "f" + Util.instance.ticksToElapsedTime((waitToAnswer - System.currentTimeMillis()) / 50L, false, false, false)).getFormattedText(), guiLeft + 25, dialogHeight, 0xFFFFFF);
				showOptions = false;
			}
			if (showOptions) {
				if (!dialog.showWheel) { drawLinedOptions(); }
				else { drawWheel(); }
			}
		}
		if (showOptions) {
			if (newDialogSet && lineTotal == l) { newDialogSet = false; }
			// Dialog Slider
			if (lineTotal > lineVisibleSize) {
				int x = guiSettings.guiRight - 10;
				int r = guiSettings.guiRight - 13;
				if (guiSettings.getType() == 2) { x = 1; r = 12; }
				int y = 1, hd = dialogHeight - 5;
				float ms = (float) lineVisibleSize / (float) lineTotal;
				int sHeight = (int) (ms * (float) hd);
				y += (int) ((float) (hd - sHeight) * (float) lineStart / (float) (lineTotal - lineVisibleSize));
				if (scrollD == null) { scrollD = new int[9]; }
				scrollD[0] = x;
				scrollD[1] = y;
				scrollD[2] = x + 9;
				scrollD[3] = Math.min((y + sHeight), dialogHeight - 4);
				scrollD[4] = 1;
				scrollD[5] = hd;
				scrollD[6] = (int) (((float) hd - (float) sHeight) / (float) (lineTotal - lineVisibleSize));
				fill(scrollD[0], scrollD[1], scrollD[2], scrollD[3], zLevel, guiSettings.sliderColor, guiSettings.sliderColor);
				drawVerticalLine(r, 0, dialogHeight - 4, guiSettings.linesColor);
			}
			else { scrollD = null; }
			// Option Slider
			if (selectedSize > selectedVisibleSize && !dialog.showWheel) {
				int x = guiSettings.guiRight - 10;
				int r = guiSettings.guiRight - 13;
				if (guiSettings.getType() == 2) { x = 1; r = 12; }
				int y = dialogHeight + 1;
				int hd = height - dialogHeight - 2;
				float ms = (float) selectedVisibleSize / (float) selectedTotal.size();
				int sHeight = (int) (ms * (float) hd);
				y += (int) ((float) (hd - sHeight) * (float) selectedStart / (float) (selectedTotal.size() - selectedVisibleSize));
				if (scrollO == null) { scrollO = new int[9]; }
				scrollO[0] = x;
				scrollO[1] = y;
				scrollO[2] = x + 7;
				scrollO[3] = Math.min((y + sHeight), height - 1);
				scrollO[4] = dialogHeight + 1;
				scrollO[5] = hd;
				scrollO[6] = (int) (((float) hd - (float) sHeight) / (float) (selectedTotal.size() - selectedVisibleSize));
				fill(scrollO[0], scrollO[1], scrollO[2], scrollO[3], zLevel, guiSettings.sliderColor, guiSettings.sliderColor);
				drawVerticalLine(r, dialogHeight, height - 1, guiSettings.linesColor);
			}
			else { scrollO = null; }
			// Dialog
			int drag = Mouse.getDWheel() / 120;
			if (lineTotal > lineVisibleSize && drag != 0 && isMouseHover(mouseX, mouseY, guiLeft, 0, guiSettings.dialogWidth, dialogHeight)) {
				lineStart -= drag;
				if (lineStart < 0) { lineStart = 0; }
				else if (lineStart > lineTotal - lineVisibleSize) { lineStart = lineTotal - lineVisibleSize; }
			}
			// cursor select option
			if (isMouseHover(mouseX, mouseY, guiLeft, dialogHeight, guiSettings.dialogWidth, height - dialogHeight)) { // options text
				if (selectedSize > selectedVisibleSize && drag != 0) {
					selectedStart -= drag;
					checkSelected();
				}
				int y = (int) Math.floor(((double) mouseY - (double) dialogHeight) / (double) fontHeight);
				i = 0;
				int optPos = 0;
				selected = -1;
				for (List<String> list : options.values()) {
					if (optPos < selectedStart) {
						optPos++;
						continue;
					}
					for (String opt : list) {
						if (i == y) {
							selected = optPos;
							break;
						}
						i++;
					}
					if (selected != -1) { break; }
					optPos++;
				}
				if (selected == -1) { selected = options.size() - 1; }
			}
		}
	}

	@Override
	public void drawString(@Nonnull FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color) {
		ClientProxy.Font.drawString(text, x, y, color);
	}

	protected void drawString(String text, int left, int color, int linePos) {
		int height = (linePos - lineStart) * fontHeight;
		int line = guiTop + dialogHeight - fontHeight / 3;
		if (height > line) { return; }
		int x = guiLeft + 1 + left;
		if (guiSettings.getType() == 2 && lineTotal > lineVisibleSize) { x += 13; }
		drawString(fontRenderer, text, x, guiTop + height, color);
		if (textures.containsKey(linePos)) {
			textures.get(linePos).left = guiLeft + 1 + left;
		}
	}

	protected void drawWheel() {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		int center = guiSettings.dialogWidth / 2;
		GlStateManager.translate(guiLeft + center, guiTop + dialogHeight + 14, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		// background
		mc.getTextureManager().bindTexture(wheel);
		drawTexturedModalRect(0, 0, 0, 0, 63, 40);

		// select pos
		int mdx = Mouse.getDX(), mdy = Mouse.getDY();
		int maxLists = (int) Math.ceil(options.size() / 6.0d) - 1;
		selectedX += mdx;
		selectedY += mdy;
		float a = 118.0f, b = 65.0f;
		double inB = Math.pow(selectedX, 2.0d) / Math.pow(a, 2.0d) + Math.pow(selectedY, 2.0d) / Math.pow(b, 2.0d);
		if (inB > 1.0d) {
			selectedX -= mdx;
			selectedY -= mdy;
		}
		selectedWheel = 0;
		if (options.size() > 6) { // options size > 6
			drawTexturedModalRect(17, 7, 63, 0, 30, 18);
			if (wheelList == 0) {
				drawTexturedModalRect(17, 7, 93, 0, 15, 18);
			} else if (wheelList >= maxLists) {
				drawTexturedModalRect(32, 7, 108, 0, 15, 18);
			}
			a = 55.0f;
			b = 34.0f;
			inB = Math.pow(selectedX, 2.0d) / Math.pow(a, 2.0d) + Math.pow(selectedY - 10, 2.0d) / Math.pow(b, 2.0d);
			if (inB < 1.0d) {
				selected = -1;
				selectedWheel = selectedX > 0 ? 2 : 1;
			}
		}

		// draw select wheel
		if (selectedWheel == 0) {
			double xVal = -selectedX, yVal = -selectedY;
            double rad = 180.0d / Math.PI;
            double rot;
			if (xVal == 0.0d) {
				rot = selectedY > 0 ? 180.0d : 0.0d;
			} else {
				final double v = Math.atan(yVal / xVal) * rad;
				if (xVal <= 0.0d) {
					rot = 90.0d + v;
				} else {
					rot = 270.0d + v;
				}
			}
			rot %= 360.0d;
			if (rot < 0.0d) {
				rot += 360.0d;
			}
			if (rot > 122.0d && rot <= 180.0d) {
				selected = wheelList * 6;
			} else if (rot > 85.0d && rot <= 122.0d) {
				selected = 1 + wheelList * 6;
			} else if (rot > 0.0d && rot <= 85.0d) {
				selected = 2 + wheelList * 6;
			} else if (rot > 275.0d && rot <= 360.0d) {
				selected = 3 + wheelList * 6;
			} else if (rot > 238.0d && rot <= 275.0d) {
				selected = 4 + wheelList * 6;
			} else {
				selected = 5 + wheelList * 6;
			}
		} else {
			if (wheelList == 0 && selectedWheel == 1) {
				selectedWheel = 0;
			} else if (selectedWheel == 2 && wheelList >= maxLists) {
				selectedWheel = 0;
			}
			if (selectedWheel != 0) {
				selected = -1;
				int wu = 123 + (selectedWheel == 1 ? 0 : 15);
				drawTexturedModalRect(17 + (selectedWheel == 1 ? 0 : 15), 7, wu, 0, 15, 18);
			}
		}

		// draw select
		if (wheelList >= maxLists) {
			for (int slot = selected < 0 ? options.size() - options.size() % 6
					: selected - selected % 6, j = 0; j < 6; slot++, j++) {
				if (slot >= options.size()) {
					int u = 63 * (slot % 3);
					int v = 40 * (int) (3.0d + Math.floor((double) slot % 6 / 3.0d));
					drawTexturedModalRect(0, 0, u, v, 63, 40);
					if (slot == selected) {
						selected = -1;
					}
				}
			}
		}
		if (selected >= 0) {
			drawTexturedModalRect(0, 0, 63 * (selected % 3),
					40 * (int) (1.0d + Math.floor((double) selected % 6 / 3.0d)), 63, 40);
		}
		// cursor
		drawTexturedModalRect(30 + selectedX / 4, 17 - selectedY / 4, 63, 18, 3, 3);
		// text
		for (int slot = wheelList * 6, j = 0; j < 6; slot++, j++) {
			DialogOption option = dialog.options.get(slot);
			if (option == null || option.optionType == OptionType.DISABLED || option.title.isEmpty()) {
				continue;
			}
			Dialog d = option.getDialog(player);
			if (d != null && !d.availability.isAvailable(player)) {
				continue;
			}

			int color = option.optionColor;
			if (slot == selected) {
				color = 0x838FD8;
			}

			String wTitle = new TextComponentTranslation(option.title).getFormattedText();
			StringBuilder text = new StringBuilder(wTitle);
			if (fontRenderer.getStringWidth(text.toString()) * corr > center - 5) {
				text = new StringBuilder();
				for (int c = 0; c < wTitle.length(); c++) {
					char ch = wTitle.charAt(c);
					if (fontRenderer.getStringWidth(text.toString() + ch) * corr > center - 5) {
						text.append("...");
						break;
					}
					text.append(ch);
				}
			}
			int height = getFontHeight(text.toString());
			int u = 0, v = 0;
			switch (slot % 6) {
			case 0:
				u = 62;
				v = 1 - height;
				break;
			case 1:
				u = 68;
				v = 13 - height / 2;
				break;
			case 2:
				u = 62;
				v = 27;
				break;
			case 3:
				u = -ClientProxy.Font.width(text.toString());
				v = 27;
				break;
			case 4:
				u = -5 - ClientProxy.Font.width(text.toString());
				v = 13 - height / 2;
				break;
			case 5:
				u = -ClientProxy.Font.width(text.toString());
				v = 1 - height;
				break;
			}
			drawString(fontRenderer, text.toString(), u, v, color);
		}
		GlStateManager.popMatrix();
	}

	protected int getFontHeight(String str) {
		int h = ClientProxy.Font.height(str);
		return h <= 1 ? 13 : h;
	}

	public int getSelected() {
		int i = 0, last = -1;
		for (int id : options.keySet()) {
			if (i == selected || selected < 0) {
				return id;
			}
			i++;
			last = id;
		}
		return last;
	}

	public void grabMouse(boolean grab) {
		if (grab && !isGrabbed) {
			Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
			isGrabbed = true;
		} else if (!grab && isGrabbed) {
			Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
			isGrabbed = false;
		}
	}

	protected void handleDialogSelection() {
		int optionId = getSelected();
		if (!options.containsKey(optionId)) {
			if (options.isEmpty() && closeOnEsc) { close(); }
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, optionId);
		if (dialog == null || dialog.notHasOtherOptions() || options.isEmpty()) {
			if (closeOnEsc) { close(); }
			return;
		}
		DialogOption option = dialog.options.get(optionId);
		if (option == null || !(option.optionType == OptionType.DIALOG_OPTION || option.optionType == OptionType.ROLE_OPTION)) {
			if (closeOnEsc) { close(); }
			return;
		}
		lines.add(new TextBlockClient(player.getDisplayNameString(), option.title, guiSettings.dialogWidth, option.optionColor, dialogNpc, player, dialogNpc));
		NoppesUtil.clickSound();
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(mc);
		guiSettings.init(sw.getScaledWidth_double(), sw.getScaledHeight_double());
		// display NPC name
		if (dialogNpc != null && guiSettings.showNPC) {
			boolean addDots = false;
			String name = npc.getName();
			while (fontRenderer.getStringWidth(name) > guiSettings.npcWidth && name.length() > 2) {
				name = name.substring(0, name.length() - 2);
				addDots = true;
			}
			if (addDots) { name += "..."; }
			dialogNpc.display.setName(name);
			addDots = false;
			String title = dialogNpc.display.getTitle();
			while (fontRenderer.getStringWidth(title) > guiSettings.npcWidth && title.length() > 2) {
				title = title.substring(0, title.length() - 2);
				addDots = true;
			}
			if (addDots) { dialogNpc.display.setTitle(title + "..."); }
		}

		isGrabbed = false;
		grabMouse(dialog.showWheel);
		guiLeft = guiSettings.guiLeft;
		guiTop = 0;
		fontHeight = getFontHeight(null);
		width = (int) Math.ceil(sw.getScaledWidth_double());
		height = (int) Math.ceil(sw.getScaledHeight_double());
        int optionHeight = dialog.showWheel ? 60 : guiSettings.optionHeight;
		dialogHeight = height - optionHeight;
		if (!lines.isEmpty()) {
			int max = guiSettings.dialogWidth - (int) (2.0f + fontRenderer.getStringWidth((dialogNpc != null ? dialogNpc.getName() : npc.getName()) + ": ") / corr);
			if (lineTotal > lineVisibleSize) { max -= (int) (18.0f / corr); }
			for (TextBlockClient textBlock : lines) {
				textBlock.lines.clear();
				textBlock.resetWidth(max, false);
			}
		}
		if (!options.isEmpty()) { resetOptions(); }
		lineTotal = 0;
		for (TextBlockClient textBlock : lines) { lineTotal += 1 + textBlock.lines.size(); }
		selectedTotal.clear();
		int i = 0;
		selectedSize = 0;
		for (int id : options.keySet()) {
			selectedTotal.put(i, options.get(id).size());
			selectedSize += options.get(id).size();
			i++;
		}
		lineVisibleSize = dialogHeight / fontHeight;
		selectedVisibleSize = Math.round((float) (height - dialogHeight - 2) / (float) fontHeight);
		selected = 0;
		selectedStart = 0;
		lineStart = lineTotal - lineVisibleSize;
		if (lineStart < 0) { lineStart = 0; }
		// Dialog texture
		if (!textures.isEmpty()) {
			Map<Integer, DialogTexture> newTxts = new HashMap<>();
			for (int pos : textures.keySet()) {
				DialogTexture dt = textures.get(pos);
				int lt = 0;
				boolean found = false;
				for (TextBlockClient textBlock : lines) {
					lt += 1 + textBlock.lines.size();
					if (textBlock.equals(dt.line)) {
						newTxts.put(lt - 1 - dt.height, dt);
						found = true;
						break;
					}
				}
				if (!found) {
					newTxts.put(pos, dt);
				}
			}
			textures.clear();
			textures.putAll(newTxts);
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (showOptions) {
			if (i == mc.gameSettings.keyBindForward.getKeyCode() || i == 200) {
				--selected;
				--selectedStart;
				if (selected < 0) {
					selected = 0;
				}
				checkSelected();
			}
			if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == 208) {
				++selected;
				++selectedStart;
				if (selected >= options.size()) {
					selected = options.size() - 1;
				}
				checkSelected();
			}
			if (i == 28) {
				handleDialogSelection();
			}
		}
		if (closeOnEsc && (i == 1 || isInventoryKey(i))) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, -1);
			close();
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (!showOptions) {
			if (newDialogSet) {
				dialog.showFits = false;
				lineStart = lineTotal - lineVisibleSize;
				if (lineStart < 0) {
					lineStart = 0;
				} else if (lineStart > lineTotal - lineVisibleSize) {
					lineStart = lineTotal - lineVisibleSize;
				}
			}
			return;
		}
		if (scrollD != null) {
			scrollD[7] = -1;
			if (isMouseHover(mouseX, mouseY, scrollD[0], scrollD[4], scrollD[2] - scrollD[0], scrollD[5])) {
				if (isMouseHover(mouseX, mouseY, scrollD[0], scrollD[1], scrollD[2] - scrollD[0], scrollD[3] - scrollD[1])) {
					scrollD[7] = mouseY;
					scrollD[8] = lineStart;
				}
				return;
			}
		}
		if (scrollO != null) {
			scrollO[7] = -1;
			if (isMouseHover(mouseX, mouseY, scrollO[0], scrollO[4], scrollO[2] - scrollO[0], scrollO[5])) {
				if (isMouseHover(mouseX, mouseY, scrollO[0], scrollO[1], scrollO[2] - scrollO[0], scrollO[3] - scrollO[1])) {
					scrollO[7] = mouseY;
					scrollO[8] = selectedStart;
				}
				return;
			}
		}
		if (dialog.showWheel) {
			if (selectedWheel != 0) {
				if (selectedWheel == 1) {
					wheelList--;
					if (wheelList < 0) {
						wheelList = 0;
					}
				} else {
					wheelList++;
					int max = (int) Math.ceil(options.size() / 6.0d);
					if (wheelList >= max) {
						wheelList = max - 1;
					}
				}
				mc.getSoundHandler()
						.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				return;
			}
			else if (selected < 0) { return; }
		}
		else if (!isMouseHover(mouseX, mouseY, guiLeft, dialogHeight, width - guiLeft, height - dialogHeight)) { return; }
		if (mouseBottom == 0) { handleDialogSelection(); }
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		LogWriter.debug("TEST: ");
		if (scrollD != null && scrollD[7] > -1) {
			int offsetLine = (int) (((float) scrollD[7] - (float) mouseY) / (float) scrollD[6]);
			if (offsetLine == 0) { return; }
			lineStart = scrollD[8] - offsetLine;
			if (lineStart < 0) {
				lineStart = 0;
				scrollD[8] = lineStart;
				scrollD[7] = mouseY;
			}
			else if (lineStart > lineTotal - lineVisibleSize) {
				lineStart = lineTotal - lineVisibleSize;
				scrollD[8] = lineStart;
				scrollD[7] = mouseY;
			}
		}
		if (scrollO != null && scrollO[7] > -1) {
			int offsetLine = (int) (((float) scrollO[7] - (float) mouseY) / (float) scrollO[6]);
			if (offsetLine == 0) { return; }
			selectedStart = scrollO[8] - offsetLine;
			checkSelected();
		}
	}

	protected void resetOptions() {
		int max = guiSettings.dialogWidth - (int) (46.0f / corr);
		if (selectedSize > selectedVisibleSize) { max -= (int) (18.0f / corr); }
		options.clear();
		for (int slot : dialog.options.keySet()) {
			DialogOption option = dialog.options.get(slot);
			if (option == null || option.optionType == OptionType.DISABLED || !option.isAvailable(player)) {
				continue;
			}
			String optionText = NoppesStringUtils.formatText(option.title, player, dialogNpc);
			List<String> lines = new ArrayList<>();
			if (fontRenderer.getStringWidth(optionText) * corr > max) {
				StringBuilder total = new StringBuilder();
				for (String sct : optionText.split(" ")) {
					if (fontRenderer.getStringWidth(total + " " + sct) * corr > max) {
						lines.add(total.toString());
						total = new StringBuilder(sct);
					} else {
						if (total.length() > 0) {
							total.append(" ");
						}
						total.append(sct);
					}
				}
				if (total.length() > 0) {
					lines.add(total.toString());
				}
			} else {
				lines.add(optionText);
			}
			options.put(slot, lines);
		}
		if (!closeOnEsc && options.isEmpty()) { closeOnEsc = true; }
	}

    @Override
	public void setClose(NBTTagCompound nbt) { grabMouse(false); }

	@SuppressWarnings("unused")
	protected void setStartLine() {
		startLine = 0;
		for (TextBlockClient textBlock : lines) {
			startLine++;
			for (ITextComponent line : textBlock.lines) {
				startLine++;
			}
		}
	}

}
