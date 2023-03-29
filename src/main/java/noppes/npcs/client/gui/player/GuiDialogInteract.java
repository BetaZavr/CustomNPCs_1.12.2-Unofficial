package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose {
	private Dialog dialog;
	private int dialogHeight;
	private ResourceLocation indicator;
	private boolean isGrabbed;
	private List<TextBlockClient> lines;
	private List<Integer> options;
	private int rowStart;
	private int rowTotal;
	private int selected;
	private int selectedX;
	private int selectedY;
	private ResourceLocation wheel;
	private ResourceLocation[] wheelparts;
	private long wait;

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
		this.indicator = this.getResource("indicator.png");
		this.wheelparts = new ResourceLocation[] { this.getResource("wheel1.png"), this.getResource("wheel2.png"), this.getResource("wheel3.png"), this.getResource("wheel4.png"), this.getResource("wheel5.png"), this.getResource("wheel6.png") };
		this.wait = this.dialog!=null ? System.currentTimeMillis() + this.dialog.delay * 50L : 0L;
	}

	public void appendDialog(Dialog dialog) {
		this.closeOnEsc = !dialog.disableEsc;
		this.dialog = dialog;
		this.options = new ArrayList<Integer>();
		CustomNpcs.stopPreviousSound(dialog.sound); // New
		if (dialog.sound != null && !dialog.sound.isEmpty()) {
			MusicController.Instance.stopMusic();
			BlockPos pos = this.npc.getPosition();
			MusicController.Instance.playSound(SoundCategory.VOICE, dialog.sound, pos.getX(), pos.getY(), pos.getZ(),
					1.0f, 1.0f);
		}
		this.lines
				.add(new TextBlockClient(this.npc, dialog.text, 280, 14737632, new Object[] { this.player, this.npc }));
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

	private void calculateRowHeight() {
		if (this.dialog.showWheel) {
			this.dialogHeight = this.ySize - 58;
		} else {
			this.dialogHeight = this.ySize - 3 * ClientProxy.Font.height(null) - 4;
			if (this.dialog.options.size() > 3) {
				this.dialogHeight -= (this.dialog.options.size() - 3) * ClientProxy.Font.height(null);
			}
		}
		this.rowTotal = 0;
		for (TextBlockClient block : this.lines) {
			this.rowTotal += block.lines.size() + 1;
		}
		int max = this.dialogHeight / ClientProxy.Font.height(null);
		this.rowStart = this.rowTotal - max;
		if (this.rowStart < 0) {
			this.rowStart = 0;
		}
	}

	private void closed() {
		this.grabMouse(false);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, 0);
	}

	private void drawLinedOptions(int j) {
		this.drawHorizontalLine(this.guiLeft - 45, this.guiLeft + this.xSize + 120, this.guiTop + this.dialogHeight - ClientProxy.Font.height(null) / 3, -1);
		int offset = this.dialogHeight;
		if (j >= this.guiTop + offset) {
			int selected = (j - (this.guiTop + offset)) / ClientProxy.Font.height(null);
			if (selected < this.options.size()) {
				this.selected = selected;
			}
		}
		if (this.selected >= this.options.size()) {
			this.selected = 0;
		}
		if (this.selected < 0) {
			this.selected = 0;
		}
		for (int k = 0; k < this.options.size(); ++k) {
			int id = this.options.get(k);
			DialogOption option = this.dialog.options.get(id);
			int y = this.guiTop + offset + k * ClientProxy.Font.height(null);
			if (this.selected == k) {
				this.drawString(this.fontRenderer, ">", this.guiLeft - 60, y, 14737632);
			}
			this.drawString(this.fontRenderer, NoppesStringUtils.formatText(option.title, this.player, this.npc), this.guiLeft - 30, y, option.optionColor);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawGradientRect(0, 0, this.width, this.height, -587202560, -587202560);
		if (!this.dialog.hideNPC) {
			int l = -70;
			int i2 = this.ySize;
			this.drawNpc(this.npc, l, i2, 1.4f, 0, false);
		}
		super.drawScreen(i, j, f);
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
		if (!this.options.isEmpty()) {
			if (this.wait>System.currentTimeMillis()) {
				this.drawHorizontalLine(this.guiLeft - 45, this.guiLeft + this.xSize + 120, this.guiTop + this.dialogHeight - ClientProxy.Font.height(null) / 3, -1);
				int offset = this.dialogHeight;
				this.drawString(this.fontRenderer, ((char) 167)+"e"+new TextComponentTranslation("gui.wait", ((char) 167)+"e: "+((char) 167)+"f"+AdditionalMethods.ticksToElapsedTime((this.wait - System.currentTimeMillis())/50L, false, false, false)).getFormattedText(), this.guiLeft - 30, this.guiTop + offset, 0xFFFFFF);
			}
			else if (!this.dialog.showWheel) {
				this.drawLinedOptions(j);
			} else {
				this.drawWheel();
			}
		}
		GlStateManager.popMatrix();
	}

	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) { ClientProxy.Font.drawString(text, x, y, color); }

	private void drawString(String text, int left, int color, int count) {
		int height = count - this.rowStart;
		this.drawString(this.fontRenderer, text, this.guiLeft + left, this.guiTop + height * ClientProxy.Font.height(null), color);
	}

	private void drawWheel() {
		int yoffset = this.guiTop + this.dialogHeight + 14;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
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
		this.mc.renderEngine.bindTexture(this.wheelparts[this.selected]);
		this.drawTexturedModalRect(this.width / 2 - 31, yoffset, 0, 0, 85, 55);
		for (int slot : this.dialog.options.keySet()) {
			DialogOption option = this.dialog.options.get(slot);
			if (option != null && option.optionType != 2) {
				if (option.hasDialog() && !option.getDialog().availability.isAvailable((EntityPlayer) this.player)) {
					continue;
				}
				int color = option.optionColor;
				if (slot == this.selected) {
					color = 8622040;
				}
				int height = ClientProxy.Font.height(option.title);
				if (slot == 0) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 13, yoffset - height, color);
				}
				if (slot == 1) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 33, yoffset - height / 2 + 14,
							color);
				}
				if (slot == 2) {
					this.drawString(this.fontRenderer, option.title, this.width / 2 + 27, yoffset + 27, color);
				}
				if (slot == 3) {
					this.drawString(this.fontRenderer, option.title,
							this.width / 2 - 13 - ClientProxy.Font.width(option.title), yoffset - height, color);
				}
				if (slot == 4) {
					this.drawString(this.fontRenderer, option.title,
							this.width / 2 - 33 - ClientProxy.Font.width(option.title), yoffset - height / 2 + 14,
							color);
				}
				if (slot != 5) {
					continue;
				}
				this.drawString(this.fontRenderer, option.title,
						this.width / 2 - 27 - ClientProxy.Font.width(option.title), yoffset + 27, color);
			}
		}
		this.mc.renderEngine.bindTexture(this.indicator);
		this.drawTexturedModalRect(this.width / 2 + this.selectedX / 4 - 2, yoffset + 16 - this.selectedY / 6, 0, 0, 8,
				8);
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
		this.lines.add(new TextBlockClient(this.player.getDisplayNameString(), option.title, 280, option.optionColor,
				new Object[] { this.player, this.npc }));
		this.calculateRowHeight();
		NoppesUtil.clickSound();
	}

	@Override
	public void initGui() {
		super.initGui();
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
