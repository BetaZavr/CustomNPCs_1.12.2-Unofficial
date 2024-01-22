package noppes.npcs.client.gui.player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
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
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiDialogInteract
extends GuiNPCInterface
implements IGuiClose {
	
	private Dialog dialog;
	private int dialogHeight;
	private boolean isGrabbed;
	private List<TextBlockClient> lines;
	private List<Integer> options;
	private int rowStart, rowTotal, selected, selectedX, selectedY;
	private ResourceLocation wheel;
	private long wait;
	private ScaledResolution sw;
	private Map<Integer, ResourceLocation> textures = Maps.<Integer, ResourceLocation>newHashMap();
	private Map<Integer, Integer[]> texturesSize = Maps.<Integer, Integer[]>newHashMap();

	public GuiDialogInteract(EntityNPCInterface npc, Dialog dialog) {
		super(npc);
		this.selected = 0;
		this.lines = new ArrayList<TextBlockClient>();
		this.options = new ArrayList<Integer>();
		this.rowStart = 0;
		this.rowTotal = 0;
		this.dialogHeight = 180;
		this.isGrabbed = false;
		this.selectedX = 0;
		this.selectedY = 0;
		this.appendDialog(this.dialog = dialog);
		this.ySize = 238;
		this.wheel = this.getResource("wheel.png");
		this.wait = this.dialog!=null ? System.currentTimeMillis() + this.dialog.delay * 50L : 0L;
	}

	public void appendDialog(Dialog dialog) {
		this.closeOnEsc = !dialog.disableEsc;
		Dialog oldDialog = this.dialog;
		this.dialog = dialog;
		this.options = new ArrayList<Integer>();
		MusicController.Instance.stopSound(null, SoundCategory.VOICE);
		if (dialog.sound != null && !dialog.sound.isEmpty()) {
			BlockPos pos = this.npc.getPosition();
			if (oldDialog != null && !oldDialog.equals(dialog) && oldDialog.sound != null && !oldDialog.sound.isEmpty() && MusicController.Instance.isPlaying(oldDialog.sound)) {
				MusicController.Instance.stopSound(oldDialog.sound, SoundCategory.VOICE);
			}
			boolean isPlay = MusicController.Instance.isPlaying(dialog.sound);
			if (isPlay) { MusicController.Instance.stopSound(dialog.sound, SoundCategory.VOICE); }
			CustomNPCsScheduler.runTack(() -> {
				MusicController.Instance.playSound(SoundCategory.VOICE, dialog.sound, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
			}, 50);
		}
		String dText = dialog.text;
		int h = 0;
		ResourceLocation txtr = null;
		Integer[] txtrSize = null;
		if (!dialog.texture.isEmpty()) {
			txtr = new ResourceLocation(dialog.texture);
			this.mc.getTextureManager().bindTexture(txtr);
			try {
				IResource res = this.mc.getResourceManager().getResource(new ResourceLocation(dialog.texture));
				BufferedImage buffer = ImageIO.read(res.getInputStream());
				txtrSize = new Integer[] { buffer.getWidth(), buffer.getHeight() };
				h = buffer.getHeight() / this.getFontHeight(null) / 2;
			}
			catch (IOException e) {}
		}
		for (int i=0; i<h; i++) { dText += ""+((char) 10); }
		this.lines.add(new TextBlockClient(this.npc, dText, 280, 0xE0E0E0, this.npc, new Object[] { this.player, this.npc }));
		if (h>0 && txtr!=null && txtrSize!=null) {
			int c = 0, s = 0;
			for (TextBlockClient t : this.lines) {
				c += t.lines.size();
				if (s==this.lines.size()-1) {
					c -= h;
					this.textures.put(c, txtr);
					this.texturesSize.put(c, txtrSize);
					break;
				}
				s++;
			}
		}
		for (int slot : dialog.options.keySet()) {
			DialogOption option = dialog.options.get(slot);
			if (option != null) {
				if (!option.isAvailable((EntityPlayer) this.player)) {
					continue;
				}
				this.options.add(slot);
			}
		}
		this.calculateRowHeight();
		this.grabMouse(dialog.showWheel);
	}

	private int getFontHeight(String str) {
		int h = ClientProxy.Font.height(str);
		return h<=1 ? 13 : h;
	}

	private void calculateRowHeight() {
		int fh = this.getFontHeight(null);
		if (this.dialog.showWheel) {
			this.dialogHeight = this.ySize - 58;
		} else {
			int line = 0;
			if (this.sw == null) {
				this.sw = new ScaledResolution(this.mc);
			}
			int w = this.sw.getScaledWidth() - this.guiLeft - 50;
			for (DialogOption option : this.dialog.options.values()) {
				if (option.optionType == 2) { continue;}
				String text = NoppesStringUtils.formatText(option.title, this.player, this.npc);
				if (this.fontRenderer.getStringWidth(text) > w) {
					List<String> lines = Lists.newArrayList();
					String total = "";
					for (String sct : text.split(" ")) {
						if (this.fontRenderer.getStringWidth(total + " " + sct) > w) {
							lines.add(total);
							total = sct;
						}
						else {
							if (!total.isEmpty()) { total += " "; }
							total += sct;
						}
					}
					if (!total.isEmpty()) { lines.add(total); }
					line += lines.size();
				} else {
					line++;
				}
			}
			if (line < 3) { line = 3; } // min
			this.dialogHeight = this.ySize - line * fh - 4;
		}
		this.rowTotal = 0;
		for (TextBlockClient block : this.lines) {
			this.rowTotal += block.lines.size() + 1;
		}
		int max = this.dialogHeight / fh;
		this.rowStart = this.rowTotal - max;
		if (this.rowStart < 0) {
			this.rowStart = 0;
		}
	}

	private void closed() {
		this.grabMouse(false);
		if (this.dialog.sound != null && !this.dialog.sound.isEmpty() && this.dialog.stopSound) {
			if (MusicController.Instance.isPlaying(this.dialog.sound)) { MusicController.Instance.stopSound(this.dialog.sound, SoundCategory.VOICE); }
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
	}

	private void drawLinedOptions(int mouseY) {
		int fh = this.getFontHeight(null);
		this.drawHorizontalLine(this.guiLeft - 45, this.guiLeft + this.xSize + 120, this.guiTop + this.dialogHeight - fh / 3, -1);
		int offset = this.dialogHeight;
		if (this.selected >= this.options.size()) {
			this.selected = 0;
		}
		if (this.selected < 0) {
			this.selected = 0;
		}
		int addLine = 0;
		int sel = this.fontRenderer.getStringWidth("-->") + 4;
		int var = this.fontRenderer.getStringWidth("*") + 4;
		for (int k = 0; k < this.options.size(); ++k) {
			int id = this.options.get(k);
			DialogOption option = this.dialog.options.get(id);
			int y = this.guiTop + offset + (k + addLine) * fh;
			String text = NoppesStringUtils.formatText(option.title, this.player, this.npc);
			if (this.fontRenderer.getStringWidth(text) > this.sw.getScaledWidth() - this.guiLeft - 50) {
				int w = this.sw.getScaledWidth() - this.guiLeft - 50;
				List<String> lines = Lists.newArrayList();
				String total = "";
				for (String sct : text.split(" ")) {
					if (this.fontRenderer.getStringWidth(total + " " + sct) > w) {
						lines.add(total);
						total = sct;
					}
					else {
						if (!total.isEmpty()) { total += " "; }
						total += sct;
					}
				}
				if (!total.isEmpty()) { lines.add(total); }
				addLine += lines.size() - 1;
				int i = 0;
				for (String sct : lines) {
					this.drawString(this.fontRenderer, sct, this.guiLeft - 30, y + i * fh, option.optionColor);
					i++;
				}
				if (mouseY >= y && mouseY <= y + lines.size() * fh) {
					int selected = k;
					if (selected < this.options.size()) {
						this.selected = selected;
					}
				}
				if (this.selected == k) {
					this.drawString(this.fontRenderer, "-->", this.guiLeft - 30 - sel, y, 14737632);
				} else {
					this.drawString(this.fontRenderer, "*", this.guiLeft - 30 - var, y, 14737632);
				}
			}
			else {
				this.drawString(this.fontRenderer, text, this.guiLeft - 30, y, option.optionColor);
				if (mouseY >= y && mouseY <= y + fh) {
					int selected = k;
					if (selected < this.options.size()) {
						this.selected = selected;
					}
				}
				if (this.selected == k) {
					this.drawString(this.fontRenderer, "-->", this.guiLeft - 30 - sel, y, 14737632);
				} else {
					this.drawString(this.fontRenderer, "*", this.guiLeft - 30 - var, y, 14737632);
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawGradientRect(0, 0, this.width, this.height, -587202560, -587202560);
		if (!this.dialog.hideNPC) {
			int l = -70;
			int i2 = this.ySize;
			this.drawNpc(this.npc, l, i2, 1.4f, 0, 0, false);
		}
		super.drawScreen(mouseX, mouseY, f);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.5f, 100.065f);
		int count = 0;
		for (TextBlockClient block : new ArrayList<TextBlockClient>(this.lines)) {
			int size = ClientProxy.Font.width(block.getName() + ": ");
			this.drawString(block.getName() + ": ", -4 - size, block.color, count);
			for (ITextComponent line : block.lines) {
				this.drawString(line.getFormattedText(), 0, block.color, count);
				++count;
			}
			++count;
		}
		int maxRows = this.dialogHeight / this.getFontHeight(null);
		if (!this.options.isEmpty()) {
			if (this.wait>System.currentTimeMillis()) {
				this.drawHorizontalLine(this.guiLeft - 45, this.guiLeft + this.xSize + 120, this.guiTop + this.dialogHeight - this.getFontHeight(null) / 3, -1);
				int offset = this.dialogHeight;
				this.drawString(this.fontRenderer, ((char) 167)+"e"+new TextComponentTranslation("gui.wait", ((char) 167)+"e: "+((char) 167)+"f"+AdditionalMethods.ticksToElapsedTime((this.wait - System.currentTimeMillis())/50L, false, false, false)).getFormattedText(), this.guiLeft - 30, this.guiTop + offset, 0xFFFFFF);
			}
			else if (!this.dialog.showWheel) {
				this.drawLinedOptions(mouseY);
			} else {
				this.drawWheel();
			}
		}
		if (this.rowTotal > maxRows) {
			int x = (int) this.sw.getScaledWidth_double() - 10;
			int y = this.guiTop + 9;
			int sHeight = (int) ((float) maxRows / (float) this.rowTotal * (float) this.dialogHeight);
			y += this.rowStart * this.getFontHeight(null) / 2;
			this.drawGradientRect(x, y, x+7, y+sHeight, 0xFFFFFFFF, 0xFFFFFFFF);
		}
		GlStateManager.popMatrix();
		int drag = Mouse.getDWheel() / 120;
		if (drag!=0) {
			this.rowStart -= drag;
			if (this.rowStart>this.rowTotal-2) { this.rowStart = this.rowTotal-2; }
			if (this.rowStart<0) { this.rowStart = 0; }
			if (this.rowTotal - this.rowStart < maxRows) { this.rowStart = this.rowTotal - maxRows; }
		}
	}

	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) { ClientProxy.Font.drawString(text, x, y, color); }

	private void drawString(String text, int left, int color, int count) {
		int height = (count - this.rowStart) * this.getFontHeight(null);
		int line = this.guiTop + this.dialogHeight - this.getFontHeight(null) / 3;
		if (height+12 > line) { return; }
		this.drawString(this.fontRenderer, text, this.guiLeft + left, this.guiTop + height, color);
		if (this.textures.containsKey(count) && this.texturesSize.containsKey(count)) {
			Integer[] size = this.texturesSize.get(count);
			if (height+size[1]/2 > line) { return; }
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableBlend();
			this.mc.renderEngine.bindTexture(this.textures.get(count));
			GlStateManager.translate(this.guiLeft + left, this.guiTop + height, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			this.drawTexturedModalRect(0, 0, 0, 0, size[0], size[1]);
			GlStateManager.popMatrix();
		}
	}

	private void drawWheel() {
		int yoffset = this.guiTop + this.dialogHeight + 14;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		// base
		this.mc.renderEngine.bindTexture(this.wheel);
		this.drawTexturedModalRect(this.width / 2 - 31, yoffset, 0, 0, 63, 40);
		this.selectedX += Mouse.getDX();
		this.selectedY += Mouse.getDY();
		int limit = 80;
		if (this.selectedX > limit) {
			this.selectedX = limit;
		}
		if (this.selectedX < -limit) {
			this.selectedX = -limit;
		}
		if (this.selectedY > limit) {
			this.selectedY = limit;
		}
		if (this.selectedY < -limit) {
			this.selectedY = -limit;
		}
		this.selected = 1;
		if (this.selectedY < -20) {
			++this.selected;
		}
		if (this.selectedY > 54) {
			--this.selected;
		}
		if (this.selectedX < 0) {
			this.selected += 3;
		}
		// more
		this.mc.renderEngine.bindTexture(this.wheel);
		this.drawTexturedModalRect(this.width / 2 - 31, yoffset, 0, 40, 63, 40);
		// select
		this.mc.renderEngine.bindTexture(this.wheel);
		int u = 63 + 63 * (this.selected % 3);
		int v = (int) (40.0d * Math.floor((double) this.selected / 3.0d));
		this.drawTexturedModalRect(this.width / 2 - 31, yoffset, u, v, 63, 40);
		for (int slot : this.dialog.options.keySet()) {
			DialogOption option = this.dialog.options.get(slot);
			if (option != null && option.optionType != 2) {
				Dialog d = option.getDialog((EntityPlayer) this.player);
				if (d != null && !d.availability.isAvailable((EntityPlayer) this.player)) {
					continue;
				}
				int color = option.optionColor;
				if (slot == this.selected) {
					color = 8622040;
				}
				int height = this.getFontHeight(option.title);
				if (slot == 0) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 13, yoffset - height, color);
				}
				if (slot == 1) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 33, yoffset - height / 2 + 14, color);
				}
				if (slot == 2) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 27, yoffset + 27, color);
				}
				if (slot == 3) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 - 13 - ClientProxy.Font.width(option.title), yoffset - height, color);
				}
				if (slot == 4) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 - 33 - ClientProxy.Font.width(option.title), yoffset - height / 2 + 14, color);
				}
				if (slot != 5) {
					continue;
				}
				this.drawString(this.fontRenderer, option.title, this.width / 2 - 27 - ClientProxy.Font.width(option.title), yoffset + 27, color);
			}
		}
		// indicator
		this.mc.renderEngine.bindTexture(this.wheel);
		this.drawTexturedModalRect(this.width / 2 + this.selectedX / 4 - 2, yoffset + 16 - this.selectedY / 6, 63, 80, 8, 8);
	}

	public int getSelected() {
		if (this.selected <= 0) {
			return 0;
		}
		if (this.selected < this.options.size()) {
			return this.selected;
		}
		return this.options.size() - 1;
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
		int optionId = -1;
		if (this.dialog.showWheel) {
			optionId = this.selected;
		} else if (!this.options.isEmpty()) {
			optionId = this.options.get(this.selected);
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, optionId);
		if (this.dialog == null || !this.dialog.hasOtherOptions() || this.options.isEmpty()) {
			if (this.closeOnEsc) {
				this.closed();
				this.close();
			}
			return;
		}
		DialogOption option = this.dialog.options.get(optionId);
		if (option == null || option.optionType != 1) {
			if (this.closeOnEsc) {
				this.closed();
				this.close();
			}
			return;
		}
		this.lines.add(new TextBlockClient(this.player.getDisplayNameString(), option.title, 280, option.optionColor, this.npc, new Object[] { this.player, this.npc }));
		this.calculateRowHeight();
		NoppesUtil.clickSound();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.sw = new ScaledResolution(this.mc);
		this.isGrabbed = false;
		this.grabMouse(this.dialog.showWheel);
		this.guiTop = this.height - this.ySize;
		this.calculateRowHeight();
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i!=1 && this.wait>System.currentTimeMillis()) { return; }
		if (i == this.mc.gameSettings.keyBindForward.getKeyCode() || i == 200) {
			--this.selected;
		}
		if (i == this.mc.gameSettings.keyBindBack.getKeyCode() || i == 208) {
			++this.selected;
		}
		if (i == 28) {
			this.handleDialogSelection();
		}
		if (this.closeOnEsc && (i == 1 || this.isInventoryKey(i))) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, -1);
			this.closed();
			this.close();
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		if (this.wait>System.currentTimeMillis()) { return; }
		if (((this.selected == -1 && this.options.isEmpty()) || this.selected >= 0) && k == 0) {
			this.handleDialogSelection();
		}
	}

	@Override
	public void save() { }

	@Override
	public void setClose(int i, NBTTagCompound data) { this.grabMouse(false); }
	
}
