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
import noppes.npcs.util.Util;

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

	protected int colour;
	protected String[] hoverText;
	protected GuiCustom parent;
	protected float scale;
	protected final long start;
	protected final long now;
	protected final long end;
	protected final boolean reverse;
	protected final FontRenderer fontRenderer;
	protected int offsetType;
	protected final int textColor;
	protected final int[] offsets;

	public CustomGuiTimer(long startIn, long endIn, boolean reverseIn, int id, int x, int y, int width, int height, int colourIn) {
		super(Minecraft.getMinecraft().fontRenderer, id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, colourIn);
		fontRenderer = Minecraft.getMinecraft().fontRenderer;
		textColor = colour;
		offsetType = 0;
		scale = 1.0f;
		colour = colourIn;
		start = startIn;
		end = endIn;
		now = System.currentTimeMillis();
		reverse = reverseIn;
		offsets = new int[] { 0, 0 };
	}

	public int getId() { return id; }

	@Override
	public int[] getPosXY() { return new int[] { x, y }; }

	public String getText() {
		long time = System.currentTimeMillis() - now;
		time /= 50L;
		if (reverse) { time = start - time; }
		if (time < 0 || (!reverse && time > end)) { NoppesUtilPlayer.sendDataCheckDelay(EnumPlayerPacket.HudTimerEnd, this, 250, id, offsetType); }
		if (reverse) { time += 20; }
		return Util.instance.ticksToElapsedTime(time, false, false, false);
	}

	@Override
	public void offSet(int offsetTypeIn, double[] windowSize) {
		offsetType = offsetTypeIn;
		switch (offsetTypeIn) {
			case 1: { // left down
				offsets[0] = 0;
				offsets[1] = (int) windowSize[1];
				break;
			}
			case 2: { // right up
				offsets[0] = (int) windowSize[0];
				offsets[1] = 0;
				break;
			}
			case 3: { // right down
				offsets[0] = (int) windowSize[0];
				offsets[1] = (int) windowSize[1];
				break;
			}
			default: { // left up
				offsets[0] = 0;
				offsets[1] = 0;
			}
		}
	}

	@Override
	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		if (!visible) { return; }
		int xIn = offsets[0] == 0 ? x : offsets[0] - x - width;
		int yIn = offsets[1] == 0 ? y : offsets[1] - y - height;
		boolean hovered = mouseX >= xIn && mouseY >= yIn && mouseX < xIn + width && mouseY < yIn + height;
		GlStateManager.pushMatrix();
		GlStateManager.translate(xIn, yIn, Math.min(id, 1000));
		GlStateManager.scale(scale, scale, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		drawString(fontRenderer, getText(), 0, 0, textColor);
		if (hovered && hoverText != null && hoverText.length > 0) { parent.hoverText = hoverText; }
		GlStateManager.popMatrix();

	}

	@Override
	public void setParent(GuiCustom parentIn) { parent = parentIn; }

	@Override
	public void setPosXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public void setScale(float scaleIn) { scale = scaleIn; }

	@Override
	public ICustomGuiComponent toComponent() {
		CustomGuiTimerWrapper component = new CustomGuiTimerWrapper(id, start, end, x, y, width, height, colour);
		component.setHoverText(hoverText);
		return component;
	}

}
