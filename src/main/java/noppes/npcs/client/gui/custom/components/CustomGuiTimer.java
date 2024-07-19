package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTimerWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.util.AdditionalMethods;

public class CustomGuiTimer extends GuiLabel implements IGuiComponent {

	public static CustomGuiTimer fromComponent(CustomGuiTimerWrapper component) {
		CustomGuiTimer timer = new CustomGuiTimer(component.start, component.end, component.reverse, component.getId(),
				component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(),
				component.getColor());
		timer.setScale(component.getScale());
		if (component.hasHoverText()) {
			timer.hoverText = component.getHoverText();
		}
		return timer;
	}

	int colour;
	String[] hoverText;
	GuiCustom parent;
	float scale;
	private final long start;
    private final long now;
    private final long end;
	private final boolean reverse;
	private final FontRenderer fontRenderer;
	private int offsetType;
	private final int textColor;
	private final int[] offsets;

	public CustomGuiTimer(long start, long end, boolean reverse, int id, int x, int y, int width, int height,
			int colour) {
		super(Minecraft.getMinecraft().fontRenderer, id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height,
				colour);
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
		this.textColor = colour;
		this.offsetType = 0;
		this.scale = 1.0f;
		this.colour = colour;
		this.start = start;
		this.end = end;
		this.now = System.currentTimeMillis();
		this.reverse = reverse;
		this.offsets = new int[] { 0, 0 };
	}

	public int getId() {
		return this.id;
	}

	@Override
	public int[] getPosXY() {
		return new int[] { this.x, this.y };
	}

	public String getText() {
		long time = System.currentTimeMillis() - this.now;
		time /= 50L;
		if (this.reverse) {
			time = this.start - time;
		}
		if (time < 0 || (!this.reverse && time > this.end)) {
			NoppesUtilPlayer.sendDataCheckDelay(EnumPlayerPacket.HudTimerEnd, this, 250, this.id, this.offsetType);
		}
		if (this.reverse) {
			time += 20;
		}
		return AdditionalMethods.ticksToElapsedTime(time, false, false, false);
	}

	@Override
	public void offSet(int offsetType, double[] windowSize) {
		this.offsetType = offsetType;
		switch (offsetType) {
		case 1: { // left down
			this.offsets[0] = 0;
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		case 2: { // right up
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = 0;
			break;
		}
		case 3: { // right down
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		default: { // left up
			this.offsets[0] = 0;
			this.offsets[1] = 0;
		}
		}
	}

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		if (!this.visible) {
			return;
		}
		int x = this.offsets[0] == 0 ? this.x : this.offsets[0] - this.x - this.width;
		int y = this.offsets[1] == 0 ? this.y : this.offsets[1] - this.y - this.height;
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, Math.min(this.id, 1000));
		GlStateManager.scale(this.scale, this.scale, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		this.drawString(this.fontRenderer, this.getText(), 0, 0, this.textColor);
		if (hovered && this.hoverText != null && this.hoverText.length > 0) {
			this.parent.hoverText = this.hoverText;
		}
		GlStateManager.popMatrix();

	}

	@Override
	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	@Override
	public void setPosXY(int newX, int newY) {
		this.x = newX;
		this.y = newY;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiTimerWrapper component = new CustomGuiTimerWrapper(this.id, this.start, this.end, this.x, this.y,
				this.width, this.height, this.colour);
		component.setHoverText(this.hoverText);
		return component;
	}

}
