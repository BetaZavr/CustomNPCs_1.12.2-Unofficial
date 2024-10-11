package noppes.npcs.client.gui.player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import noppes.npcs.LogWriter;
import noppes.npcs.mixin.client.resources.ILanguageManagerMixin;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
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

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose {

	static class DialogTexture {

		public ResourceLocation res;
		public int left, uS, vS, height;
		public TextBlockClient line;

		private DialogTexture(ResourceLocation r, int[] uv, TextBlockClient tb) {
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
		icons = Maps.newLinkedHashMap();
		icons.put(1, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/important.png"));
		icons.put(2, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/question.png"));
		icons.put(3, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/circle.png"));
		icons.put(4, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/joke.png"));
		icons.put(5, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/triangle.png"));
		icons.put(6, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/square.png"));
		icons.put(7, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/hexagon.png"));
		icons.put(8, new ResourceLocation(CustomNpcs.MODID, "textures/gui/dialog_option_icons/dice.png"));
	}
	private Dialog dialog; // current dialog

	private boolean isGrabbed; // used for answer wheel
	private final ResourceLocation wheel = new ResourceLocation(CustomNpcs.MODID, "textures/gui/wheel.png");
	private final Map<Integer, List<String>> options; // [slotID, text]
	// dialog place
	private int lineStart, lineTotal, lineVisibleSize;
	private int[] scrollD;
	// option place
	private int selected, selectedStart, selectedSize, selectedVisibleSize;
	private final Map<Integer, Integer> selectedTotal;
	private int[] scrollO;
	// wheel option
	private int wheelList, selectedX, selectedY, selectedWheel;
    // textures
	private long waitToAnswer;
	private final Map<Integer, DialogTexture> textures = Maps.newHashMap();
	// Display
	private final List<TextBlockClient> lines; // Dialog Logs
	private final EntityNPCInterface dialogNpc;
	private int w;
    private int h;
    private int tf;
    private int dialogHeight;
    private int dialogWidth;
    private int startLine;
	private long startTime;
	private float corr = 1.0f;

	private boolean showOptions, newDialogSet;

	public GuiDialogInteract(EntityNPCInterface npc, Dialog dialog) {
		super(npc);
		dialogNpc = Util.instance.copyToGUI(npc, mc.world, false);
		selected = 0;
		selectedStart = 0;
		selectedX = 0;
		selectedY = 0;
		wheelList = 0;
		selectedWheel = 0;
		lines = Lists.newArrayList();
		options = Maps.newTreeMap();
		lineStart = 0;
		lineTotal = 0;
		isGrabbed = false;
		ySize = 238;
		selectedSize = 0;
		selectedTotal = Maps.newHashMap();
		this.dialog = dialog;
		scrollD = null;
		scrollO = null;
		initGui();
		appendDialog(dialog);
	}

	public void appendDialog(Dialog d) { // 62: NoppesUtil.openDialog();
		this.closeOnEsc = !d.disableEsc;
		this.newDialogSet = true;
		Dialog oldDialog = this.dialog;
		this.dialog = d.copy();
		this.options.clear();
		selected = 0;
		selectedStart = 0;
		// Old Sound
		MusicController.Instance.stopSound(null, SoundCategory.VOICE);
		if (d.sound != null && !d.sound.isEmpty()) {
			BlockPos pos = dialogNpc.getPosition();
			if (oldDialog != null && !oldDialog.equals(d) && oldDialog.sound != null && !oldDialog.sound.isEmpty()
					&& MusicController.Instance.isPlaying(oldDialog.sound)) {
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
		this.setStartLine();
		StringBuilder dText = new StringBuilder(d.text);
		while (dText.toString().contains("<br>")) {
			dText = new StringBuilder(dText.toString().replace("<br>", "" + ((char) 10)));
		}
		while (dText.toString().contains("\\n")) {
			dText = new StringBuilder(dText.toString().replace("\\n", "" + ((char) 10)));
		}
		ResourceLocation txtr = null;
		int[] txtrSize = null;
		if (!d.texture.isEmpty()) {
			txtr = new ResourceLocation(d.texture);
			this.mc.getTextureManager().bindTexture(txtr);
			try {
				IResource res = this.mc.getResourceManager().getResource(new ResourceLocation(d.texture));
				BufferedImage buffer = ImageIO.read(res.getInputStream());
				txtrSize = new int[] { buffer.getWidth(), buffer.getHeight(),
						(int) Math.ceil((double) buffer.getHeight() / (double) this.tf / 2.0d) };
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}
		if (txtrSize != null) {
			for (int i = 0; i < txtrSize[2]; i++) {
				dText.append("" + ((char) 10));
			}
		}
		this.lines.add(new TextBlockClient(this.dialogNpc, dText.toString(), this.dialogWidth - (int) (13.0f / this.corr), 0xE0E0E0, dialogNpc, player, npc));
		if (!d.showFits) {
			this.setStartLine();
		}
		// Dialog options
		this.resetOptions();
		this.grabMouse(d.showWheel);
		this.waitToAnswer = this.dialog != null ? System.currentTimeMillis() + this.dialog.delay * 50L : 0L;
		lineTotal = 0;
		for (TextBlockClient textBlock : this.lines) {
			lineTotal += 1 + textBlock.lines.size();
		}
		selectedTotal.clear();
		int i = 0;
		selectedSize = 0;
		for (int id : this.options.keySet()) {
			selectedTotal.put(i, this.options.get(id).size());
			selectedSize += this.options.get(id).size();
			i++;
		}
		lineVisibleSize = this.dialogHeight / this.tf;
		selectedVisibleSize = Math.round((float) (this.h - dialogHeight - 2) / (float) this.tf);
		selected = 0;
		selectedStart = 0;
		if (lineStart < 0) {
			lineStart = 0;
		}
		// Dialog texture
		if (txtr != null && txtrSize != null && !this.textures.containsKey(lineTotal - 1 - txtrSize[2])) {
			this.textures.put(lineTotal - 1 - txtrSize[2],
                    new DialogTexture(txtr, txtrSize, this.lines.get(this.lines.size() - 1)));
		}
	}

	private void checkSelected() {
		int selSize = 0, pos = 1;
		for (int i = selectedTotal.size() - 1; i > 0; i--) {
			selSize += selectedTotal.get(i);
			if (selSize >= selectedVisibleSize) {
				break;
			}
			pos++;
		}
		if (selectedStart <= 0) {
			selectedStart = 0;
			if (scrollO != null) {
				scrollO[8] = selectedStart;
				scrollO[7] = mouseY;
			}
		} else if (selectedStart >= selectedTotal.size() - pos) {
			selectedStart = selectedTotal.size() - pos;
			if (scrollO != null) {
				scrollO[8] = selectedStart;
				scrollO[7] = mouseY;
			}
		}
	}

	@Override
	public void close() {
		this.grabMouse(false);
		if (this.dialog.sound != null && !this.dialog.sound.isEmpty() && this.dialog.stopSound) {
			if (MusicController.Instance.isPlaying(this.dialog.sound)) {
				MusicController.Instance.stopSound(this.dialog.sound, SoundCategory.VOICE);
			}
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
		super.close();
	}

	private void drawLinedOptions() {
		int i = 0, optPos = 0, endW = this.w - 3;
		if (selectedSize > selectedVisibleSize) {
			endW -= 13;
		}
		for (int id : this.options.keySet()) {
			if (optPos < selectedStart) {
				optPos++;
				continue;
			}
			DialogOption option = this.dialog.options.get(id);
			List<String> lines = this.options.get(id);
			int j = 0;
			for (String sct : lines) {
				if (j == 0) {
					this.drawString(this.fontRenderer, optPos == this.selected ? "->" : " *",
							this.guiLeft + 1 + (icons.containsKey(option.iconId) ? 0 : 10),
							this.dialogHeight + i * this.tf, 0xFFFFFFFF);
					if (i != 0 && optPos - 1 != this.selected) {
						this.drawHorizontalLine(this.guiLeft + 2, endW, this.dialogHeight + i * this.tf, 0xFF404040);
					}
					if (this.selected == optPos) {
						this.drawHorizontalLine(this.guiLeft + 28, endW - 1, this.dialogHeight + i * this.tf,
								0xFF80F080);
					}
					if (icons.containsKey(option.iconId)) {
						this.mc.getTextureManager().bindTexture(icons.get(option.iconId));
						GlStateManager.pushMatrix();
						GlStateManager.translate(this.guiLeft + 11.5f, this.dialogHeight + i * this.tf + 1.0f, 0.0f);
						GlStateManager.color((float) (option.optionColor >> 16 & 255) / 255.0F,
								(float) (option.optionColor >> 8 & 255) / 255.0F,
								(float) (option.optionColor & 255) / 255.0F, 1.0F);
						float s = 12.0f / 256.0f;
						GlStateManager.scale(s, s, s);
						this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
						GlStateManager.popMatrix();
					}
				}
				if (this.selected == optPos) {
					this.drawGradientRect(this.guiLeft + 25, this.dialogHeight + i * this.tf + (j == 0 ? 1 : 0),
							endW - 1, this.dialogHeight + (i + 1) * this.tf, 0xFF202020, 0xFF202020);
				}
				this.drawString(this.fontRenderer, sct, this.guiLeft + 25, this.dialogHeight + i * this.tf,
						option.optionColor);
				i++;
				j++;
				if (j == lines.size()) {
					this.drawHorizontalLine(this.guiLeft + 2, endW, this.dialogHeight + i * this.tf, 0xFF404040);
					if (this.selected == optPos) {
						this.drawHorizontalLine(this.guiLeft + 28, endW - 2, this.dialogHeight + i * this.tf, 0xFF80F080);
					}
				}
			}
			optPos++;
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Back
		this.drawGradientRect(0, 0, this.width, this.height, 0xCC000000, 0xCC000000);
		this.drawVerticalLine(this.guiLeft - 3, 1, this.h - 2, 0x40FFFFFF);
		this.drawVerticalLine(this.guiLeft - 1, 1, this.dialogHeight - 2, 0x40FFFFFF);
		this.drawHorizontalLine(this.guiLeft, this.w - 3, this.dialogHeight - 3, 0x40FFFFFF);
		this.drawVerticalLine(this.guiLeft - 1, this.dialogHeight - 2, this.h - 2, 0x40FFFFFF);
		this.drawHorizontalLine(this.guiLeft, this.w - 3, this.dialogHeight - 1, 0x40FFFFFF);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		// NPC
		if (!this.dialog.hideNPC && this.dialogNpc != null) {
			this.drawNpc(this.dialogNpc, this.guiLeft / -2, this.h - 10, 2.0f, -40, 15, dialog.showWheel ? 0 : 2);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.5f, 100.065f);
		int time = 1 + (int) ((System.currentTimeMillis() - this.startTime) / (1000L / CustomNpcs.DialogShowFitsSpeed));
		int l = 0;
		showOptions = true;
		for (TextBlockClient textBlock : Lists.newArrayList(this.lines)) {
			int size = ClientProxy.Font.width(textBlock.getName() + ": ");
			if (l >= this.lineStart) {
				this.drawString(textBlock.getName() + ": ", 0, textBlock.color, l);
			}
			for (ITextComponent line : textBlock.lines) {
				if (newDialogSet && l >= lineStart + lineVisibleSize) {
					lineStart = l - lineVisibleSize + 1;
				}
				if (l < this.lineStart) {
					++l;
					continue;
				}
				if (this.dialog.showFits && this.startLine == l) {
					String text = line.getFormattedText();
					while (text.endsWith(((char) 167) + "r")) {
						text = text.substring(0, text.length() - 2);
					}
					while (text.startsWith(((char) 167) + "r")) {
						text = text.substring(2);
					}
					if (text.length() >= time) {
						this.drawString(text.substring(0, time), size, textBlock.color, l);
						if (text.length() == time) {
							this.startLine++;
							this.startTime = System.currentTimeMillis();
						}
						showOptions = false;
						break;
					}
				}
				this.drawString(line.getFormattedText(), size, textBlock.color, l);
				++l;
			}
			++l;
		}
		GlStateManager.popMatrix();

		if (!this.textures.isEmpty()) {
			for (int linePos : this.textures.keySet()) {
				DialogTexture dt = this.textures.get(linePos);
				if (dt.left < this.guiLeft) {
					continue;
				}
				int tys = this.tf * (linePos - lineStart);
				int tye = tys + dt.vS / 2;
				if (tye >= 0 && tys <= this.dialogHeight - 4) {
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
					this.mc.getTextureManager().bindTexture(dt.res);
					GlStateManager.translate(dt.left, pH, 0.0f);
					float sc = (Math.max(dt.uS, dt.vS)) / 256.0f;
					GlStateManager.scale(sc / 2.0f, sc / 2.0f, 1.0f);
					if (pH + (e * sc) / 2 > this.dialogHeight - 4) {
						e = (int) ((float) ((this.dialogHeight - 4 - pH) * 2) / sc);
					}
					this.drawTexturedModalRect(0, v, 0, s, 256, e);
					GlStateManager.popMatrix();
				}
			}
		}

		if (!this.options.isEmpty()) {
			if (this.waitToAnswer > System.currentTimeMillis()) {
				int offset = this.dialogHeight;
				this.drawString(this.fontRenderer,
						((char) 167) + "e" + new TextComponentTranslation("gui.wait",
								((char) 167) + "e: " + ((char) 167) + "f" + Util.instance.ticksToElapsedTime(
										(this.waitToAnswer - System.currentTimeMillis()) / 50L, false, false, false))
												.getFormattedText(),
						this.guiLeft + 25, this.dialogHeight, 0xFFFFFF);
				showOptions = false;
			}
			if (showOptions) {
				if (!dialog.showWheel) {
					this.drawLinedOptions();
				} else {
					this.drawWheel();
				}
			}
		}
		if (showOptions) {
			if (newDialogSet && lineTotal == l) {
				newDialogSet = false;
			}
			if (lineTotal > lineVisibleSize) {
				int x = this.w - 10, y = 1, hd = dialogHeight - 5;
				float ms = (float) lineVisibleSize / (float) lineTotal;
				int sHeight = (int) (ms * (float) hd);
				y += (int) ((float) (hd - sHeight) * (float) lineStart / (float) (lineTotal - lineVisibleSize));
				if (scrollD == null) {
					scrollD = new int[9];
				}
				scrollD[0] = x;
				scrollD[1] = y;
				scrollD[2] = x + 7;
				scrollD[3] = Math.min((y + sHeight), dialogHeight - 4);
				scrollD[4] = 1;
				scrollD[5] = hd;
				scrollD[6] = (int) (((float) hd - (float) sHeight) / (float) (lineTotal - lineVisibleSize));
				this.drawGradientRect(scrollD[0], scrollD[1], scrollD[2], scrollD[3], 0x90FFFFFF, 0x90FFFFFF);
				this.drawVerticalLine(this.w - 13, 0, dialogHeight - 4, 0x40FFFFFF);
			} else {
				scrollD = null;
			}
			if (selectedSize > selectedVisibleSize && !dialog.showWheel) {
				int x = this.w - 10, y = dialogHeight + 1, hd = this.h - dialogHeight - 2;
				float ms = (float) selectedVisibleSize / (float) selectedTotal.size();
				int sHeight = (int) (ms * (float) hd);
				y += (int) ((float) (hd - sHeight) * (float) selectedStart
						/ (float) (selectedTotal.size() - selectedVisibleSize));
				if (scrollO == null) {
					scrollO = new int[9];
				}
				scrollO[0] = x;
				scrollO[1] = y;
				scrollO[2] = x + 7;
				scrollO[3] = Math.min((y + sHeight), this.h - 1);
				scrollO[4] = dialogHeight + 1;
				scrollO[5] = hd;
				scrollO[6] = (int) (((float) hd - (float) sHeight)
						/ (float) (selectedTotal.size() - selectedVisibleSize));
				this.drawGradientRect(scrollO[0], scrollO[1], scrollO[2], scrollO[3], 0x90FFFFFF, 0x90FFFFFF);
				this.drawVerticalLine(this.w - 13, dialogHeight, this.h - 1, 0x40FFFFFF);
			} else {
				scrollO = null;
			}
			int drag = Mouse.getDWheel() / 120;
			if (lineTotal > lineVisibleSize && drag != 0
					&& this.isMouseHover(mouseX, mouseY, this.guiLeft, 0, this.w - this.guiLeft, this.dialogHeight)) { // Dialog
																														// text
				lineStart -= drag;
				if (lineStart < 0) {
					lineStart = 0;
				} else if (lineStart > lineTotal - lineVisibleSize) {
					lineStart = lineTotal - lineVisibleSize;
				}
			}
			if (this.isMouseHover(mouseX, mouseY, this.guiLeft, this.dialogHeight, this.w - this.guiLeft,
					this.h - this.dialogHeight)) { // options text
				if (selectedSize > selectedVisibleSize && drag != 0) {
					selectedStart -= drag;
					checkSelected();
				}
				int y = (int) Math.floor(((double) mouseY - (double) this.dialogHeight) / (double) this.tf);
				int i = 0, optPos = 0;
				this.selected = -1;
				for (List<String> list : this.options.values()) {
					if (optPos < selectedStart) {
						optPos++;
						continue;
					}
					for (String opt : list) {
						if (i == y) {
							this.selected = optPos;
							break;
						}
						i++;
					}
					if (this.selected != -1) {
						break;
					}
					optPos++;
				}
				if (this.selected == -1) {
					this.selected = this.options.size() - 1;
				}
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawString(@Nonnull FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color) {
		ClientProxy.Font.drawString(text, x, y, color);
	}

	private void drawString(String text, int left, int color, int linePos) {
		int height = (linePos - this.lineStart) * this.tf;
		int line = this.guiTop + this.dialogHeight - this.tf / 3;
		if (height > line) {
			return;
		}
		this.drawString(this.fontRenderer, text, this.guiLeft + 1 + left, this.guiTop + height, color);
		if (this.textures.containsKey(linePos)) {
			this.textures.get(linePos).left = this.guiLeft + 1 + left;
		}
	}

	private void drawWheel() {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		int center = this.dialogWidth / 2;
		GlStateManager.translate(this.guiLeft + center, this.guiTop + this.dialogHeight + 14, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		// background
		this.mc.getTextureManager().bindTexture(this.wheel);
		this.drawTexturedModalRect(0, 0, 0, 0, 63, 40);

		// select pos
		int mdx = Mouse.getDX(), mdy = Mouse.getDY();
		int maxLists = (int) Math.ceil(this.options.size() / 6.0d) - 1;
		selectedX += mdx;
		selectedY += mdy;
		float a = 118.0f, b = 65.0f;
		double inB = Math.pow(selectedX, 2.0d) / Math.pow(a, 2.0d) + Math.pow(selectedY, 2.0d) / Math.pow(b, 2.0d);
		if (inB > 1.0d) {
			selectedX -= mdx;
			selectedY -= mdy;
		}
		selectedWheel = 0;
		if (this.options.size() > 6) { // options size > 6
			this.drawTexturedModalRect(17, 7, 63, 0, 30, 18);
			if (wheelList == 0) {
				this.drawTexturedModalRect(17, 7, 93, 0, 15, 18);
			} else if (wheelList >= maxLists) {
				this.drawTexturedModalRect(32, 7, 108, 0, 15, 18);
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
				this.selected = wheelList * 6;
			} else if (rot > 85.0d && rot <= 122.0d) {
				this.selected = 1 + wheelList * 6;
			} else if (rot > 0.0d && rot <= 85.0d) {
				this.selected = 2 + wheelList * 6;
			} else if (rot > 275.0d && rot <= 360.0d) {
				this.selected = 3 + wheelList * 6;
			} else if (rot > 238.0d && rot <= 275.0d) {
				this.selected = 4 + wheelList * 6;
			} else {
				this.selected = 5 + wheelList * 6;
			}
		} else {
			if (wheelList == 0 && selectedWheel == 1) {
				selectedWheel = 0;
			} else if (selectedWheel == 2 && wheelList >= maxLists) {
				selectedWheel = 0;
			}
			if (selectedWheel != 0) {
				this.selected = -1;
				int wu = 123 + (selectedWheel == 1 ? 0 : 15);
				this.drawTexturedModalRect(17 + (selectedWheel == 1 ? 0 : 15), 7, wu, 0, 15, 18);
			}
		}

		// draw select
		if (wheelList >= maxLists) {
			for (int slot = selected < 0 ? this.options.size() - this.options.size() % 6
					: selected - selected % 6, j = 0; j < 6; slot++, j++) {
				if (slot >= this.options.size()) {
					int u = 63 * (slot % 3);
					int v = 40 * (int) (3.0d + Math.floor((double) slot % 6 / 3.0d));
					this.drawTexturedModalRect(0, 0, u, v, 63, 40);
					if (slot == selected) {
						selected = -1;
					}
				}
			}
		}
		if (selected >= 0) {
			this.drawTexturedModalRect(0, 0, 63 * (this.selected % 3),
					40 * (int) (1.0d + Math.floor((double) this.selected % 6 / 3.0d)), 63, 40);
		}
		// cursor
		this.drawTexturedModalRect(30 + selectedX / 4, 17 - selectedY / 4, 63, 18, 3, 3);
		// text
		for (int slot = wheelList * 6, j = 0; j < 6; slot++, j++) {
			DialogOption option = this.dialog.options.get(slot);
			if (option == null || option.optionType == OptionType.DISABLED || option.title.isEmpty()) {
				continue;
			}
			Dialog d = option.getDialog(this.player);
			if (d != null && !d.availability.isAvailable(this.player)) {
				continue;
			}

			int color = option.optionColor;
			if (slot == this.selected) {
				color = 0x838FD8;
			}

			StringBuilder text = new StringBuilder(option.title);
			if (this.fontRenderer.getStringWidth(text.toString()) * this.corr > center - 5) {
				text = new StringBuilder();
				for (int c = 0; c < option.title.length(); c++) {
					char ch = option.title.charAt(c);
					if (this.fontRenderer.getStringWidth(text.toString() + ch) * this.corr > center - 5) {
						text.append("...");
						break;
					}
					text.append(ch);
				}
			}
			int height = this.getFontHeight(text.toString());
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
			this.drawString(this.fontRenderer, text.toString(), u, v, color);
		}
		GlStateManager.popMatrix();
	}

	private int getFontHeight(String str) {
		int h = ClientProxy.Font.height(str);
		return h <= 1 ? 13 : h;
	}

	public int getSelected() {
		int i = 0, last = -1;
		for (int id : this.options.keySet()) {
			if (i == this.selected || this.selected < 0) {
				return id;
			}
			i++;
			last = id;
		}
		return last;
	}

	public void grabMouse(boolean grab) {
		if (grab && !this.isGrabbed) {
			Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
			this.isGrabbed = true;
		} else if (!grab && this.isGrabbed) {
			Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
			this.isGrabbed = false;
		}
	}

	private void handleDialogSelection() {
		int optionId = this.getSelected();
		if (!this.options.containsKey(optionId)) {
			if (this.options.isEmpty() && this.closeOnEsc) {
				this.close();
			}
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, optionId);
		if (this.dialog == null || this.dialog.notHasOtherOptions() || this.options.isEmpty()) {
			if (this.closeOnEsc) {
				this.close();
			}
			return;
		}
		DialogOption option = this.dialog.options.get(optionId);
		if (option == null || option.optionType != OptionType.DIALOG_OPTION) {
			if (this.closeOnEsc) {
				this.close();
			}
			return;
		}
		this.lines.add(new TextBlockClient(this.player.getDisplayNameString(), option.title, this.dialogWidth,
				option.optionColor, dialogNpc, this.player, dialogNpc));
		NoppesUtil.clickSound();
	}

	@Override
	public void initGui() {
		super.initGui();

		ScaledResolution sw = new ScaledResolution(this.mc);
		if (this.dialogNpc != null) {
			boolean addDots = false;
			String name = this.dialogNpc.getName();
			int wName = (int) (sw.getScaledWidth_double() * 0.1525d);
			while (this.fontRenderer.getStringWidth(name) > wName) {
				name = name.substring(0, name.length() - 2);
				addDots = true;
			}
			if (addDots) {
				this.dialogNpc.display.setName(name + "...");
			}
			addDots = false;
			String title = this.dialogNpc.display.getTitle();
			while (this.fontRenderer.getStringWidth(title) > wName) {
				title = title.substring(0, title.length() - 2);
				addDots = true;
			}
			if (addDots) {
				this.dialogNpc.display.setTitle(title + "...");
			}
		}

		this.isGrabbed = false;
		this.grabMouse(this.dialog.showWheel);
		this.guiLeft = 112;
		this.guiTop = 0;
		this.tf = this.getFontHeight(null);
		this.w = (int) Math.ceil(sw.getScaledWidth_double());
		this.h = (int) Math.ceil(sw.getScaledHeight_double());
        int optionHeight = this.dialog.showWheel ? 60 : (int) (Math.ceil(sw.getScaledHeight_double()) / 3.0d);
		this.dialogHeight = this.h - optionHeight;
		this.corr = ((ILanguageManagerMixin) Minecraft.getMinecraft().getLanguageManager()).npcs$getCurrentLanguage().equals("ru_ru") ? 1.14583f : 1.0f;
		this.dialogWidth = (int) ((float) (this.w - this.guiLeft - 43) / this.corr);
		if (!this.lines.isEmpty()) {
			int max = this.dialogWidth - (int) (13.0f / this.corr);
			for (TextBlockClient textBlock : this.lines) {
				textBlock.lines.clear();
				textBlock.resetWidth(max, false);
			}
		}
		if (!this.options.isEmpty()) {
			this.resetOptions();
		}
		lineTotal = 0;
		for (TextBlockClient textBlock : this.lines) {
			lineTotal += 1 + textBlock.lines.size();
		}
		selectedTotal.clear();
		int i = 0;
		selectedSize = 0;
		for (int id : this.options.keySet()) {
			selectedTotal.put(i, this.options.get(id).size());
			selectedSize += this.options.get(id).size();
			i++;
		}
		lineVisibleSize = this.dialogHeight / this.tf;
		selectedVisibleSize = Math.round((float) (this.h - dialogHeight - 2) / (float) this.tf);
		selected = 0;
		selectedStart = 0;
		lineStart = lineTotal - lineVisibleSize;
		if (lineStart < 0) {
			lineStart = 0;
		}
		// Dialog texture
		if (!this.textures.isEmpty()) {
			Map<Integer, DialogTexture> newTxts = Maps.newHashMap();
			for (int pos : this.textures.keySet()) {
				DialogTexture dt = this.textures.get(pos);
				int lt = 0;
				boolean found = false;
				for (TextBlockClient textBlock : this.lines) {
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
			this.textures.clear();
			this.textures.putAll(newTxts);
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (showOptions) {
			if (i == this.mc.gameSettings.keyBindForward.getKeyCode() || i == 200) {
				--selected;
				--selectedStart;
				if (selected < 0) {
					selected = 0;
				}
				checkSelected();
			}
			if (i == this.mc.gameSettings.keyBindBack.getKeyCode() || i == 208) {
				++this.selected;
				++selectedStart;
				if (selected >= this.options.size()) {
					selected = this.options.size() - 1;
				}
				checkSelected();
			}
			if (i == 28) {
				this.handleDialogSelection();
			}
		}
		if (this.closeOnEsc && (i == 1 || this.isInventoryKey(i))) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, -1);
			this.close();
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
			if (this.isMouseHover(mouseX, mouseY, scrollD[0], scrollD[4], scrollD[2] - scrollD[0], scrollD[5])) {
				if (this.isMouseHover(mouseX, mouseY, scrollD[0], scrollD[1], scrollD[2] - scrollD[0], scrollD[3] - scrollD[1])) {
					scrollD[7] = mouseY;
					scrollD[8] = lineStart;
				}
				return;
			}
		}
		if (scrollO != null) {
			scrollO[7] = -1;
			if (this.isMouseHover(mouseX, mouseY, scrollO[0], scrollO[4], scrollO[2] - scrollO[0], scrollO[5])) {
				if (this.isMouseHover(mouseX, mouseY, scrollO[0], scrollO[1], scrollO[2] - scrollO[0], scrollO[3] - scrollO[1])) {
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
					int max = (int) Math.ceil(this.options.size() / 6.0d);
					if (wheelList >= max) {
						wheelList = max - 1;
					}
				}
				this.mc.getSoundHandler()
						.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				return;
			} else if (selected < 0) {
				return;
			}
		} else if (!this.isMouseHover(mouseX, mouseY, this.guiLeft, this.dialogHeight, this.w - this.guiLeft,
				this.h - this.dialogHeight)) {
			return;
		}
		if (mouseBottom == 0) {
			this.handleDialogSelection();
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (scrollD != null && scrollD[7] > -1) {
			int offsetline = (int) (((float) scrollD[7] - (float) mouseY) / (float) scrollD[6]);
			if (offsetline == 0) {
				return;
			}
			lineStart = scrollD[8] - offsetline;
			if (lineStart < 0) {
				lineStart = 0;
				scrollD[8] = lineStart;
				scrollD[7] = mouseY;
			} else if (lineStart > lineTotal - lineVisibleSize) {
				lineStart = lineTotal - lineVisibleSize;
				scrollD[8] = lineStart;
				scrollD[7] = mouseY;
			}
		}
		if (scrollO != null && scrollO[7] > -1) {
			int offsetline = (int) (((float) scrollO[7] - (float) mouseY) / (float) scrollO[6]);
			if (offsetline == 0) {
				return;
			}
			if (offsetline == 0.0) {
				return;
			}
			selectedStart = scrollO[8] - offsetline;
			checkSelected();
		}
	}

	private void resetOptions() {
		int max = this.dialogWidth - (int) (14.0f / this.corr);
		this.options.clear();
		for (int slot : this.dialog.options.keySet()) {
			DialogOption option = this.dialog.options.get(slot);
			if (option == null || option.optionType == OptionType.DISABLED || !option.isAvailable(this.player)) {
				continue;
			}
			String optionText = NoppesStringUtils.formatText(option.title, this.player, dialogNpc);
			List<String> lines = Lists.newArrayList();
			if (this.fontRenderer.getStringWidth(optionText) * this.corr > max) {
				StringBuilder total = new StringBuilder();
				for (String sct : optionText.split(" ")) {
					if (this.fontRenderer.getStringWidth(total + " " + sct) * this.corr > max) {
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
			this.options.put(slot, lines);
		}
		if (!this.closeOnEsc && this.options.isEmpty()) { this.closeOnEsc = true; }
	}

	@Override
	public void save() {
	}

	@Override
	public void setClose(int id, NBTTagCompound nbt) {
		this.grabMouse(false);
	}

	@SuppressWarnings("unused")
	private void setStartLine() {
		startLine = 0;
		for (TextBlockClient textBlock : this.lines) {
			startLine++;
			for (ITextComponent line : textBlock.lines) {
				startLine++;
			}
		}
	}
}
