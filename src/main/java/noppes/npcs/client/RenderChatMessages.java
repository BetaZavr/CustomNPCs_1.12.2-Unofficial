package noppes.npcs.client;

import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.IChatMessages;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderChatMessages
implements IChatMessages {
	
	private int boxLength;
	private String lastMessage;
	private long lastMessageTime;
	private Map<Long, TextBlockClient> messages;
	private float scale;

	public RenderChatMessages() {
		this.messages = new TreeMap<Long, TextBlockClient>();
		this.boxLength = 46;
		this.scale = 0.5f;
		this.lastMessage = "";
		this.lastMessageTime = 0L;
	}

	@Override
	public void addMessage(String message, EntityNPCInterface npc) {
		if (!CustomNpcs.EnableChatBubbles) {
			return;
		}
		long time = System.currentTimeMillis();
		if (message.equals(this.lastMessage) && this.lastMessageTime + 5000L > time) {
			return;
		}
		Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>(this.messages);
		messages.put(time, new TextBlockClient(message, this.boxLength * 4, true, new Object[] { Minecraft.getMinecraft().player, npc }));
		if (messages.size() > 3) { messages.remove(messages.keySet().iterator().next()); }
		this.messages = messages;
		this.lastMessage = message;
		this.lastMessageTime = time;
	}

	private void drawRect(int par0, int par1, int par2, int par3, int par4, double par5) {
		if (par0 < par2) {
			int j1 = par0;
			par0 = par2;
			par2 = j1;
		}
		if (par1 < par3) {
			int j1 = par1;
			par1 = par3;
			par3 = j1;
		}
		float f = (par4 >> 24 & 0xFF) / 255.0f;
		float f2 = (par4 >> 16 & 0xFF) / 255.0f;
		float f3 = (par4 >> 8 & 0xFF) / 255.0f;
		float f4 = (par4 & 0xFF) / 255.0f;
		BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
		GlStateManager.color(f2, f3, f4, f);
		tessellator.begin(7, DefaultVertexFormats.POSITION);
		tessellator.pos(par0, par3, par5).endVertex();
		tessellator.pos(par2, par3, par5).endVertex();
		tessellator.pos(par2, par1, par5).endVertex();
		tessellator.pos(par0, par1, par5).endVertex();
		Tessellator.getInstance().draw();
	}

	private Map<Long, TextBlockClient> getMessages() {
		Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>();
		long time = System.currentTimeMillis();
		for (Map.Entry<Long, TextBlockClient> entry : this.messages.entrySet()) {
			if (time > entry.getKey() + 10000L) {
				continue;
			}
			messages.put(entry.getKey(), entry.getValue());
		}
		return this.messages = messages;
	}

	private void render(double par3, double par5, double par7, float textscale, boolean depth) {
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		float var13 = 1.6f;
		float var14 = 0.016666668f * var13;
		GlStateManager.pushMatrix();
		int size = 0;
		for (TextBlockClient block : this.messages.values()) {
			size += block.lines.size();
		}
		Minecraft mc = Minecraft.getMinecraft();
		int textYSize = (int) (size * font.FONT_HEIGHT * this.scale);
		GlStateManager.translate(par3 + 0.0f, par5 + textYSize * textscale * var14, par7);
		GlStateManager.scale(textscale, textscale, textscale);
		GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(-var14, -var14, var14);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.depthMask(true);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		if (depth) {
			GlStateManager.enableDepth();
		} else {
			GlStateManager.disableDepth();
		}
		int black = depth ? -16777216 : 1426063360;
		int white = depth ? -1140850689 : 1157627903;
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.enableCull();
		this.drawRect(-this.boxLength - 2, -2, this.boxLength + 2, textYSize + 1, white, 0.11);
		this.drawRect(-this.boxLength - 1, -3, this.boxLength + 1, -2, black, 0.1);
		this.drawRect(-this.boxLength - 1, textYSize + 2, -1, textYSize + 1, black, 0.1);
		this.drawRect(3, textYSize + 2, this.boxLength + 1, textYSize + 1, black, 0.1);
		this.drawRect(-this.boxLength - 3, -1, -this.boxLength - 2, textYSize, black, 0.1);
		this.drawRect(this.boxLength + 3, -1, this.boxLength + 2, textYSize, black, 0.1);
		this.drawRect(-this.boxLength - 2, -2, -this.boxLength - 1, -1, black, 0.1);
		this.drawRect(this.boxLength + 2, -2, this.boxLength + 1, -1, black, 0.1);
		this.drawRect(-this.boxLength - 2, textYSize + 1, -this.boxLength - 1, textYSize, black, 0.1);
		this.drawRect(this.boxLength + 2, textYSize + 1, this.boxLength + 1, textYSize, black, 0.1);
		this.drawRect(0, textYSize + 1, 3, textYSize + 4, white, 0.11);
		this.drawRect(-1, textYSize + 4, 1, textYSize + 5, white, 0.11);
		this.drawRect(-1, textYSize + 1, 0, textYSize + 4, black, 0.1);
		this.drawRect(3, textYSize + 1, 4, textYSize + 3, black, 0.1);
		this.drawRect(2, textYSize + 3, 3, textYSize + 4, black, 0.1);
		this.drawRect(1, textYSize + 4, 2, textYSize + 5, black, 0.1);
		this.drawRect(-2, textYSize + 4, -1, textYSize + 5, black, 0.1);
		this.drawRect(-2, textYSize + 5, 1, textYSize + 6, black, 0.1);
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.scale(this.scale, this.scale, this.scale);
		int index = 0;
		for (TextBlockClient block2 : this.messages.values()) {
			for (ITextComponent chat : block2.lines) {
				String message = chat.getFormattedText();
				font.drawString(message, -font.getStringWidth(message) / 2, index * font.FONT_HEIGHT, black);
				++index;
			}
		}
		GlStateManager.disableCull();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderMessages(double par3, double par5, double par7, float textscale, boolean inRange) {
		Map<Long, TextBlockClient> messages = this.getMessages();
		if (messages.isEmpty()) { return; }
		if (inRange) { this.render(par3, par5, par7, textscale, false); }
		this.render(par3, par5, par7, textscale, true);
	}
}
