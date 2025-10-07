package noppes.npcs.client.gui.player;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

// Changed from Unofficial (BetaZavr)
@SideOnly(Side.CLIENT)
public class GuiNPCTrader extends GuiContainerNPCInterface
		implements IGuiData, ITextfieldListener, ITextChangeListener {

	public static final ResourceLocation BUTTONS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trade/buttons.png");
	public static final ResourceLocation HOVERS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trade/hovers.png");
	public static final ResourceLocation INV = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trade/player_inventory.png");
	public static final ResourceLocation SCROLL = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trade/scroll.png");
	public static final ResourceLocation ICONS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trade/sections.png");
	public static Marcet marcet;
	protected static String search = "";
	protected static boolean isIdSort = true;
	protected static int section = -1;
	protected static final Comparator<Deal> comparator = (t1, t2) -> {
		if (isIdSort) {
			Map<Integer, Integer> indexMap = new HashMap<>();
			int i = 0;
			for (IDeal iDeal : GuiNPCTrader.marcet.getDeals(GuiNPCTrader.section)) { indexMap.put(iDeal.getId(), i++); }
			return Integer.compare(indexMap.getOrDefault(t1.getId(), Integer.MAX_VALUE), indexMap.getOrDefault(t2.getId(), Integer.MAX_VALUE));
		}
		else { return t1.getName().compareToIgnoreCase(t2.getName()); }
	};

	protected final Map<Integer, Deal> data = new LinkedHashMap<>();
	protected DealMarkup selectDealData;

	protected List<Integer> canBuy = new ArrayList<>();
	protected List<Integer> canSell = new ArrayList<>();
	protected int count = 1;

	// buttons
	public Map<Integer, TradeButton> tButtons = new ConcurrentHashMap<>();
	// display
	protected boolean wait = false;
	protected int invPosX;
	protected int invPosY;
	// scroll
	protected boolean isScrolled;
	protected int scrollWidth;
	protected int scrollHMax;
	protected int scrollBMax;
	protected int scrollHeight;
	protected int scrollBHeight;
	protected int scrollY;
	protected int scrollMaxY;
	// hovers
	protected List<String> hovers = new ArrayList<>();
	protected boolean isHovered;
	protected int hoverHeightMax;
	protected int hoverHMax;
	protected int hoverBMax;
	protected int hoverHeight;
	protected int hoverBHeight;
	protected int hoverY;
	protected int hoverMaxY;
	// model rotate
	protected Map<String, String> materialTextures = new HashMap<>();
	protected float rotateX = 0.0f;
	protected float rotateZ = 0.0f;

	// Tabs
	protected int ceilHeight = 0;
	protected int ceilList = -1;

	public GuiNPCTrader(ContainerNPCTrader container) {
		super(NoppesUtil.getLastNpc(), container);
		drawDefaultBackground = false;
		closeOnEsc = true;
		hoverIsGame = true;
		title = "";

		ScaledResolution sw = new ScaledResolution(mc);
		xSize = (int) sw.getScaledWidth_double();
		ySize = (int) sw.getScaledHeight_double();
		marcet = container.marcet;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button instanceof TradeButtonBiDirectional) {
			count = button.getValue() + 1;
			initGui();
			return;
		}
		if (button instanceof SectionButton) {
			int s = button.id - 20 + ceilList * ceilHeight;
			if (button.id >= 20 && s != section) {
				section = s;
				selectDealData = null;
				scrollY = 0;
				hoverY = 0;
				rotateX = 0.0f;
				rotateZ = 0.0f;
				count = 1;
				initGui();
			}
			return;
		}
		if (button instanceof TradeButton) {
			TradeButton tradeB = (TradeButton) button;
			if ((tradeB.deal.getAmount() > 0 || player.isCreative()) && (selectDealData == null || selectDealData.deal.getId() != tradeB.deal.getId())) {
				selectDealData = tradeB.dm;
				hoverY = 0;
				rotateX = 0.0f;
				rotateZ = 0.0f;
				count = 1;
				initGui();
			}
			return;
		} // select deal
		switch (button.id) {
			case -1: NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketBuy, marcet.getId(), selectDealData.deal.getId(), npc == null ? -1 : npc.getEntityId(), count); break; // buy
			case 1: NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketSell, marcet.getId(), selectDealData.deal.getId(), npc == null ? -1 : npc.getEntityId(), count); break; // Sell
			case 2: NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketReset, marcet.getId()); break; // Reset
			case 3: {
				if (ceilList <= 0) { return; }
				ceilList--;
				initGui();
				return;
			} // up
			case 4: {
				if (ceilList >= Math.floor((double) marcet.sections.size() / 5.0d)) { return; }
				ceilList++;
				initGui();
				return;
			} // down
			case 11: {
				isIdSort = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				return;
			} // sort type
		}
		wait = true;
		initGui();
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

		GlStateManager.enableBlend();
		GlStateManager.translate(0.0f, 0.0f, -1.0f);
		int w;
		int h = (250 - scrollHeight) / 2;
		String text;
		// update / money pos
		mc.getTextureManager().bindTexture(INV);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(invPosX, 0, 0, 142, 178, 24);
		// player inventory:
		drawTexturedModalRect(invPosX, invPosY, 0, 0, 178, 118);
		// Scroll
		int s = 0;
		int v;
		mc.getTextureManager().bindTexture(SCROLL);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int i = 0; i < scrollHMax; i++) {
			if (i == 0) { v = 0; } else { v = 3 + h; }
			drawTexturedModalRect(0, i * scrollHeight, 0, v, scrollWidth, scrollHeight); // left
			drawTexturedModalRect(scrollWidth, i * scrollHeight, 214 - scrollWidth, v, scrollWidth, scrollHeight); // right
			s += scrollHeight;
		}
		// end
		h = ySize - s;
		drawTexturedModalRect(0, ySize - h, 0, 256 - h, scrollWidth, h); // left
		drawTexturedModalRect(scrollWidth, ySize - h, 214 - scrollWidth, 256 - h, scrollWidth, h); // right
		// bar
		if (scrollMaxY > 0) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(scrollWidth * 2 - 14, 4.0f, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 1.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(0, 0, 236, 0, 20, 20);
			drawTexturedModalRect(0, (ySize - 61) * 2, 236, 236, 20, 20);
			GlStateManager.scale(1.0f, 2.0f, 1.0f);
			s = 10;
			h = (216 - scrollBHeight) / 2;
			for (int i = 0; i < scrollBMax; i++) {
				if (i == 0) { v = 20; } else { v = h; }
				drawTexturedModalRect(0, 10 + i * scrollBHeight, 236, v, 20, scrollBHeight); // bar
				s += scrollBHeight;
			}
			h = 28;
			drawTexturedModalRect(0, s, 236, 236 - scrollHeight, 20, scrollHeight - h); // bar
			GlStateManager.popMatrix();
		}
		mc.getTextureManager().bindTexture(BUTTONS);
		// place of sale
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(4, ySize - 46, 0, 214, scrollWidth - 4, 42);
		drawTexturedModalRect(scrollWidth, ySize - 46, 260 - scrollWidth, 214, scrollWidth - 4, 42);
		// hover deal
		if (selectDealData != null && selectDealData.deal != null) {
			int x = xSize - 155;
			int y = 24;
			mc.getTextureManager().bindTexture(HOVERS);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(x, y, 0, 0, 132, 50);
			if (hoverHeightMax >= 24) {
				y += 50;
				s = 0;
				for (int i = 0; i < hoverHMax; i++) {
					if (i == 0) { v = 50; } else { v = 53; }
					drawTexturedModalRect(x, y + i * hoverHeight, 0, v, 132, hoverHeight);
					s += hoverHeight;
				}
				// end
				h = hoverHeightMax - s;
				drawTexturedModalRect(x, invPosY - 24 - h, 0, 256 - h, 132, h);
				// bar
				if (hoverMaxY > 0) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(x + 120.0f, y + 2, 0.0f);
					GlStateManager.scale(0.5f, 0.5f, 1.0f);
					mc.getTextureManager().bindTexture(SCROLL);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					drawTexturedModalRect(0, 0, 236, 0, 20, 20);
					drawTexturedModalRect(0, (hoverHeightMax - 14) * 2, 236, 236, 20, 20);
					if (hoverHeightMax > 24) {
						GlStateManager.scale(1.0f, 2.0f, 1.0f);
						s = 10;
						h = (216 - hoverBHeight) / 2;
						for (int i = 0; i < hoverBMax; i++) {
							if (i == 0) { v = 20; } else { v = h; }
							drawTexturedModalRect(0, 10 + i * hoverBHeight, 236, v, 20, hoverBHeight); // bar
							s += hoverBHeight;
						}
						h = hoverHeightMax % 2 != 0 ? 9 : 10;
						drawTexturedModalRect(0, s, 236, 236 - hoverHeight, 20, hoverHeight - h); // bar
					}
					GlStateManager.popMatrix();
				}
			}
		}
		// Market level
		if (marcet.showXP) {
			mc.getTextureManager().bindTexture(INV);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(xSize - 100, invPosY - 24, 0, 118, 100, 24);
			MarkupData md = ClientProxy.playerData.game.getMarkupData(marcet.getId());
			MarkupData mm = marcet.markup.get(md.level);
			if (md.xp > 0) {
				double mXP = mm.xp;
				if (md.xp >= mXP) { s = 96; }
				else { s = (int) (96.0d * md.xp / mXP); }
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(xSize - 2 - s, ySize - 142, 198 - s, 118, s, 24);
			}
			String lv = "enchantment.level." + (md.level + 1);
			if (!new TextComponentTranslation(lv).getFormattedText().equals(lv)) { lv = new TextComponentTranslation(lv).getFormattedText(); }
			else { lv = "" + (md.level + 1); }
			drawString(fontRenderer, lv, xSize - 6 - fontRenderer.getStringWidth(lv), ySize - 131, CustomNpcs.MainColor.getRGB());
			if (isMouseHover(mouseX, mouseY, xSize - 100, ySize - 142, 100, 24)) {
				putHoverText("market.hover.you.level", "" + (md.level + 1),
						"" + Math.min(md.xp, mm.xp), "" + mm.xp,
						(mm.buy <= 0.0f ? TextFormatting.GREEN: TextFormatting.RED) + "" + (int) (mm.buy * 100.0f),
						(mm.sell <= 0.0f ? TextFormatting.RED: TextFormatting.GREEN) + "" + (int) (mm.sell * 100.0f));
			} // hover market xp
		}

		// name
		if (marcet.getName().isEmpty()) { text = new TextComponentTranslation("role.trader").getFormattedText(); }
		else { text = new TextComponentTranslation(marcet.getName()).getFormattedText(); }
		w = ClientProxy.Font.width(text) / 2;
		ClientProxy.Font.drawString(text, scrollWidth - w + 10, 2, CustomNpcs.MainColor.getRGB());
		// update
		if (marcet.updateTime > 0) {
			TextFormatting color = TextFormatting.RESET;
			if (marcet.nextTime <= 60000 && marcet.nextTime % 1000 < 500) { color = TextFormatting.GOLD; }
			else if (marcet.nextTime <= 10000) { color = marcet.nextTime % 1000 < 500 ? TextFormatting.GOLD : TextFormatting.RED; }
			text = new TextComponentTranslation("market.uptime", color + Util.instance.ticksToElapsedTime(marcet.nextTime / 50, false, false, false)).getFormattedText();
			w = ClientProxy.Font.width(text);
			ClientProxy.Font.drawString(text, invPosX + 3, 2, CustomNpcs.MainColor.getRGB());
			if (marcet.nextTime <= 0) { NoppesUtilPlayer.sendDataCheckDelay(EnumPlayerPacket.MarketTime, this, 2500, marcet.getId()); }
			if (isMouseHover(mouseX, mouseY, invPosX, 0, w, 24)) {
				putHoverText("market.hover.update");
			} // hover update time
		}
		// marcet money
		text = Util.instance.getTextReducedNumber(marcet.money, true, true, false) + CustomNpcs.displayCurrencies;
		w = ClientProxy.Font.width(text);
		ClientProxy.Font.drawString(text, xSize - w - 15, 2, CustomNpcs.MainColor.getRGB());
		if (isMouseHover(mouseX, mouseY, xSize - w - 14, 0, w, 24)) {
			putHoverText("market.hover.currency.1", marcet.money, CustomNpcs.displayCurrencies);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(xSize - 15, 0, 1.0f);
		GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
		mc.getTextureManager().bindTexture(GuiNPCInterface.MONEY);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 0, 256, 256);
		GlStateManager.popMatrix();
		// money
		int x = invPosX + 4;
		int y = invPosY + 9;
		text = new TextComponentTranslation("questlog.rewardmoney", ClientProxy.playerData.game.getTextMoney(), CustomNpcs.displayCurrencies).getFormattedText();
		ClientProxy.Font.drawString(text, x, y, CustomNpcs.MainColor.getRGB());
		w = ClientProxy.Font.width(text);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + w, y - 2.0f, 1.0f);
		GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 0, 256, 256);
		GlStateManager.popMatrix();
		if (isMouseHover(mouseX, mouseY, x, y, w + 16, 16)) {
			putHoverText(new TextComponentTranslation("inventory.hover.currency").appendText(" " + ClientProxy.playerData.game.getMoney()).getFormattedText());
		} // hover money
		// donat
		text = new TextComponentTranslation("questlog.rewarddonat", ClientProxy.playerData.game.getTextDonat(), CustomNpcs.displayDonation).getFormattedText();
		w = ClientProxy.Font.width(text);
		x = xSize - 18 - w;
		ClientProxy.Font.drawString(text, x, y, CustomNpcs.MainColor.getRGB());
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + w, y - 2.0f, 1.0f);
		GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
		mc.getTextureManager().bindTexture(GuiNPCInterface.DONAT);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 0, 256, 256);
		GlStateManager.popMatrix();
		if (isMouseHover(mouseX, mouseY, x, y, w + 16, 16)) {
			putHoverText(new TextComponentTranslation("inventory.hover.donat").appendText(" " + ClientProxy.playerData.game.getDonat()).getFormattedText());
		} // hover donat
		// search icon
		GlStateManager.pushMatrix();
		GlStateManager.translate(5.0f, ySize - 25.0f, 0.0f);
		GlStateManager.scale(0.833333f, 0.833333f, 0.833333f);
		mc.getTextureManager().bindTexture(ICONS);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 216, 24, 24);
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (marcet == null) { onClosed(); return; }
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.enableBlend();
		if (!hovers.isEmpty()) {
			int i = 0;
			int x = xSize - 152;
			int y;
			GlStateManager.pushMatrix();

			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = xSize < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / (double) xSize) : 1;
			GL11.glScissor(x * c, (ySize - 72 - hoverHeightMax) * c, 127 * c, (hoverHeightMax - 4) * c);
			for (String hover : hovers) {
				y = 77 + hoverY + i * (fontRenderer.FONT_HEIGHT + 1);
				if (y >= 77 - fontRenderer.FONT_HEIGHT && y < 71 + hoverHeightMax) {
					drawString(fontRenderer, hover, x, y, CustomNpcs.MainColor.getRGB());
				}
				if (y >= 71 + hoverHeightMax) { break; }
				i++;
			}
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
		}
		if (scrollMaxY != 0) {
			float f0 = (float) -scrollY / (float) scrollMaxY * (float) (ySize - 81);
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.translate(scrollWidth * 2.0f - 14.5f, 4.0f + f0, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			mc.getTextureManager().bindTexture(SCROLL);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(0, 0, 214, 0, 22, 60);
			GlStateManager.popMatrix();
		}
		if (hoverMaxY != 0) {
			//isHovered = isMouseHover(mouseX, mouseY, xSize - 35, 76, 10, hoverHeightMax - 4);
			float f0 = (float) -hoverY / (float) hoverMaxY * (float) (hoverHeightMax - 20);
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.translate(xSize - 35.0f, 76.0f + f0, 0.0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			mc.getTextureManager().bindTexture(SCROLL);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(0, 0, 214, 0, 22, 16);
			drawTexturedModalRect(0, 16, 214, 44, 22, 16);
			GlStateManager.popMatrix();
		}
		if (selectDealData != null && selectDealData.deal != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(xSize - 89.0f, 49.0f, 50.0f);
			if (rotateX == 0 && rotateZ == 0 && mc.world != null) {
				GlStateManager.rotate(-30.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate((float) (System.currentTimeMillis() % 36000) / -20.0f, 0.0f, 1.0f, 0.0f);
			}
			else {
				GlStateManager.rotate(-30.0f + rotateX, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate(rotateZ, 0.0f, 0.0f, 1.0f);
			}
			if (selectDealData.deal.isCase()) {
				GlStateManager.translate(-16.0f, -8.0f, 0.0f);
				GlStateManager.scale(32.0f, -32.0f, 32.0f);
				GlStateManager.callList(ModelBuffer.getDisplayList(selectDealData.deal.getCaseObjModel(), null, materialTextures));
			}
			else {
				if (!selectDealData.deal.getProduct().isEmpty()) {
					ItemStack stack = selectDealData.deal.getProduct().getMCItemStack();
					GlStateManager.scale(32.0f, -32.0f, 32.0f);
					mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
				}
			}
			GlStateManager.popMatrix();
		}
		if (Mouse.hasWheel()) {
			if (isMouseHover(mouseX, mouseY, 3, 3, scrollWidth * 2 - 6, ySize - 50)) {
				scrollY = ValueUtil.correctInt(scrollY + (int) (Mouse.getDWheel() / 120.0d * 28.0d), -scrollMaxY, 0);
			}
			else if (isMouseHover(mouseX, mouseY, xSize - 153, 76, 128, hoverHeightMax - 4)) {
				hoverY = ValueUtil.correctInt(hoverY + (int) (Mouse.getDWheel() / 120.0d * ((double) fontRenderer.FONT_HEIGHT + 1.0d)), -hoverMaxY, 0);
			}
		}
	}

	@Override
	public void initGui() {
		ScaledResolution sw = new ScaledResolution(mc);
		// window
		if (xSize != (int) sw.getScaledWidth_double() || ySize != (int) sw.getScaledHeight_double()) {
			scrollY = 0;
			hoverY = 0;
		}
		xSize = (int) sw.getScaledWidth_double();
		ySize = (int) sw.getScaledHeight_double();
		((ContainerNPCTrader) inventorySlots).reset(xSize, ySize);
		boolean focus = getTextField(0) != null && getTextField(0).isFocused();
		super.initGui();
		invPosX = xSize - 178;
		invPosY = ySize - 118;
		scrollWidth = ValueUtil.correctInt(214, 0, xSize - 202) / 2;
		if (ySize <= 512) {
			scrollHMax = 1;
			scrollBMax = 1;
			scrollHeight = ySize / 2;
			scrollBHeight = (ySize - 85) / 2;
		}
		else {
			scrollHMax = (int) Math.ceil(((float) ySize - 10.0f) / 216.0f);
			scrollHeight = (int) Math.ceil(((float) ySize - 6.0f) / (float) scrollHMax);
			scrollHMax--;
			scrollBMax = (int) Math.ceil(((float) ySize - 85.0f) / 200.0f);
			scrollBHeight = (int) Math.ceil(((float) ySize - 85) / (float) scrollHMax);
			scrollBMax--;
		}
		hoverHeightMax = invPosY - 98;
		if (hoverHeightMax <= 206) {
			hoverHMax = 1;
			hoverBMax = 1;
			hoverHeight = (hoverHeightMax - 4) / 2;
			hoverBHeight = (hoverHeightMax - 24) / 2;
		}
		else {
			hoverHMax = (int) Math.ceil(((float) hoverHeightMax - 4.0f) / 216.0f);
			hoverHeight = (int) Math.ceil(((float) hoverHeightMax - 4.0f) / (float) hoverHMax);
			hoverHMax--;
			hoverBMax = (int) Math.ceil(((float) hoverHeightMax - 24.0f) / 200.0f);
			hoverBHeight = (int) Math.ceil(((float) hoverHeightMax - 24.0f) / (float) hoverHMax);
			hoverBMax--;
		}
		ceilHeight = (int) Math.floor(((float) ySize - 36.0f) / 24.0f);
		// gm buttons
		addButton(new GuiNpcButton(2, invPosX, invPosY - 22, 76, 20, "remote.reset")
				.setIsVisible(player.isCreative())
				.setHoverText("market.hover.reset"));
		// section tabs
		SectionButton tab;
		if (ceilList < 0) {
			ceilList = 0;
			section = 0;
		}
		if (marcet.sections.size() > 1) {
			int offsetY = 4;
			if (marcet.sections.size() > ceilHeight) {
				if (ceilList > 0 && section != ceilList) {
					tab = new SectionButton(this, 3, null, scrollWidth * 2 + 3, ySize - 16);
					addButton(tab);
				} // down | next
				if (ceilList < Math.floor((double) marcet.sections.size() / (double) ceilHeight)) {
					tab = new SectionButton(this, 4, null, scrollWidth * 2 + 3, 7);
					addButton(tab);
				} // up | back
				offsetY += 14;
			}
			int id;
			for (int i = 0; i < ceilHeight && (i + ceilList * ceilHeight) < marcet.sections.size(); i++) {
				id = i + ceilList * ceilHeight;
				tab = new SectionButton(this, 20 + i, marcet.sections.get(id), scrollWidth * 2 + 9, offsetY + i * 24);
				ITextComponent temp = new TextComponentTranslation("market.hover.section");
				temp.getStyle().setColor(TextFormatting.GRAY);
				tab.setHoverText(temp.getFormattedText() + "<br>" + marcet.sections.get(id).getName());
				if (i + ceilList * ceilHeight == section) { tab.active = true; }
				addButton(tab);
			}
		}
		// section deals
		int level = ClientProxy.playerData.game.getMarcetLevel(marcet.getId());
		List<Deal> dealInTrade = new ArrayList<>();
		List<Deal> caseInTrade = new ArrayList<>();
		List<Deal> dealNotTrade = new ArrayList<>();
		List<Deal> caseNotTrade = new ArrayList<>();
		MarcetController mData = MarcetController.getInstance();
		MarcetSection ms = marcet.sections.get(section);
		String s = search.toLowerCase();
		if (ms != null && !ms.deals.isEmpty()) {
			for (Deal deal : ms.deals) {
				if (!s.isEmpty() && !deal.getName().toLowerCase().contains(s)) { continue; }
				if (deal.getMaxCount() != 0 && deal.getAmount() == 0) {
					if (deal.isCase()) { caseNotTrade.add(deal); }
					else { dealNotTrade.add(deal); }
				}
				else {
					if (deal.isCase()) { caseInTrade.add(deal); }
					else { dealInTrade.add(deal); }
				}
			}
		}
		dealInTrade.sort(comparator);
		caseInTrade.sort(comparator);
		dealNotTrade.sort(comparator);
		caseNotTrade.sort(comparator);
		data.clear();
		for (Deal deal : caseInTrade) { data.put(deal.getId(), deal); }
		for (Deal deal : caseNotTrade) { data.put(deal.getId(), deal); }
		for (Deal deal : dealInTrade) { data.put(deal.getId(), deal); }
		for (Deal deal : dealNotTrade) { data.put(deal.getId(), deal); }
		if (data.isEmpty()) { scrollMaxY = 0; }
		else { scrollMaxY = ValueUtil.correctInt(data.size() * 28 - ySize + 62, 0, Integer.MAX_VALUE); }
		int i = 0;
		for (Deal deal : data.values()) {
			TradeButton button = new TradeButton(this, deal, level, 5, 15 + i * 28, (scrollWidth * 2) - (scrollMaxY == 0 ? 9 : 22), dealInTrade.contains(deal) || caseInTrade.contains(deal));
			tButtons.put(button.id, button);
			addButton(button);
			i++;
			if ((selectDealData == null || selectDealData.deal == null) && (player.isCreative() || deal.getMaxCount() > 0 && deal.getAmount() > 0)) {
				selectDealData = mData.getBuyData(marcet, deal, level, count);
			}
		}
		if (selectDealData != null && selectDealData.deal != null) {
			selectDealData = mData.getBuyData(marcet, selectDealData.deal, level, count);
			boolean found = false;
			for (Deal deal : data.values()) {
				if (deal.getId() == selectDealData.deal.getId()) {
					found = true;
					break;
				}
			}
			if (found) { selectDealData.check(player.inventory.mainInventory); }
			else {
				selectDealData = null;
				scrollY = 0;
				hoverY = 0;
				rotateX = 0.0f;
				rotateZ = 0.0f;
			}
		}
		if (selectDealData != null && selectDealData.deal != null) {
			hovers.clear();
			List<String> temp = new ArrayList<>();
			if (selectDealData.deal.isCase()) {
				materialTextures.put("minecraft:entity/chest/christmas", selectDealData.deal.getCaseTexture().toString());
				if (selectDealData.deal.showInCase() || player.isCreative())
				{ selectDealData.deal.putHoverCaseItems(temp, player.isCreative() ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL); }
			} else if (!selectDealData.deal.getProduct().isEmpty()) {
				temp.addAll(selectDealData.deal.getProduct().getMCItemStack().getTooltip(player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
			}
			if (!temp.isEmpty()) {
				String lastColor;
				int w = 116;
				for (String line : temp) {
					if (fontRenderer.getStringWidth(line) < w) { hovers.add(line); }
					else {
						lastColor = "";
						StringBuilder l = new StringBuilder();
						for (int j = 0; j < line.length(); j++) {
							char c = line.charAt(j);
							try {
								if ((int) c == 167) {
									lastColor = c + "" + line.charAt(j + 1);
								}
							}
							catch (Exception ignored) { }
							if (fontRenderer.getStringWidth(l.toString() + c) > w) {
								hovers.add(l.toString());
								l = new StringBuilder(lastColor + c);
								lastColor = "";
							}
							else { l.append(c); }
						}
						if (!l.toString().isEmpty()) { hovers.add(l.toString()); }
					}
				}
			}
			if (hovers.isEmpty()) { hoverMaxY = 0; }
			else { hoverMaxY = ValueUtil.correctInt(hovers.size() * (fontRenderer.FONT_HEIGHT + 1) - hoverHeightMax + 4, 0, Integer.MAX_VALUE); }
		}
		// buy
		int x = scrollWidth;
		int y = ySize - 45;
		boolean enableBuy = selectDealData != null && selectDealData.deal != null && selectDealData.deal.getType() != 1;
		GuiNpcButton buyButton;
		addButton(buyButton = new GuiNpcButton(-1, x, y, scrollWidth - 5, 20,
				"   " + new TextComponentTranslation("gui.buy").getFormattedText())
				.setTexture(BUTTONS)
				.setUV(0, 144, 128, 20)
				.setIsEnable(enableBuy));
		buyButton.simple(true);
		canBuy.clear();
		if (enableBuy) {
			if (wait || selectDealData.deal.getType() == 1) { canBuy.add(1); }
			if (selectDealData.deal.getAmount() <= 0 && selectDealData.deal.getMaxCount() <= 0) { canBuy.add(6); }
			if (!selectDealData.deal.availability.isAvailable(player)) { canBuy.add(2); }
			if (selectDealData.buyMoney > 0 && ClientProxy.playerData.game.getMoney() < selectDealData.buyMoney) { canBuy.add(3); }
			if (selectDealData.buyDonat > 0 && ClientProxy.playerData.game.getDonat() < selectDealData.buyDonat) { canBuy.add(7); }
			if (!Util.instance.canRemoveItems(player.inventory.mainInventory, selectDealData.buyItems, selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canBuy.add(4); }
			if (!selectDealData.deal.isCase() && !Util.instance.canAddItemAfterRemoveItems(player.inventory.mainInventory, selectDealData.main, selectDealData.buyItems, selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canBuy.add(5); }
			Map<ItemStack, Integer> mainItem = new LinkedHashMap<>();
			mainItem.put(selectDealData.main, selectDealData.count);
			if (marcet.isLimited && !selectDealData.deal.isCase() && !Util.instance.canRemoveItems(marcet.inventory, mainItem, selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canBuy.add(8); }
			if (buyButton.enabled) {
				if (!player.isCreative()) { buyButton.setIsEnable(canBuy.isEmpty()); }
				if (!canBuy.isEmpty()) { buyButton.setLayerColor(0xFF800000); }
			}
		}
		// sell
		boolean enableSell = selectDealData != null && selectDealData.deal != null  && selectDealData.deal.getType() != 0;
		GuiNpcButton sellButton;
		addButton(sellButton = new GuiNpcButton(1, x, y + 20, scrollWidth - 5, 20,
				new TextComponentTranslation("gui.sell").getFormattedText() + "   ")
				.setTexture(BUTTONS)
				.setUV(128, 144, 128, 20)
				.setIsEnable(enableSell));
		sellButton.simple(true);
		canSell.clear();
		if (enableSell) {
			if (wait) { canSell.add(1); }
			if (!selectDealData.deal.availability.isAvailable(player)) { canSell.add(2); }
			Map<ItemStack, Integer> mainItem = new HashMap<>();
			mainItem.put(selectDealData.main, selectDealData.count);
			if (!selectDealData.main.isEmpty()  && !Util.instance.canRemoveItems(player.inventory.mainInventory, mainItem,  selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canSell.add(3); }
			if (marcet.isLimited) {
				if (selectDealData.sellMoney > marcet.money) { canSell.add(4); }
				if (!selectDealData.sellItems.isEmpty() && !Util.instance.canRemoveItems(marcet.inventory,  selectDealData.sellItems, selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canSell.add(5); }
			}
			if (selectDealData.deal.getMaxCount() == 0 && selectDealData.deal.getAmount() <= 0 && selectDealData.deal.getType() != 1) { canSell.add(6); }
			if (sellButton.enabled) {
				if (!player.isCreative()) { sellButton.setIsEnable(canSell.isEmpty()); }
				if (!canSell.isEmpty()) { sellButton.setLayerColor(0xFF800000); }
			}
		}
		// prise buttons
		if (selectDealData != null && selectDealData.deal != null) {
			List<String> hoverBuy = new ArrayList<>();
			List<String> hoverSell = new ArrayList<>();
			if (selectDealData.deal.getAmount() > 0 || (mc.player != null && mc.player.isCreative())) {
				if (!canBuy.isEmpty()) {
					for (int id : canBuy) { hoverBuy.add(new TextComponentTranslation("market.hover.notbuy." + id).getFormattedText()); }
				}
				if (!canSell.isEmpty()) {
					for (int id : canSell) { hoverSell.add(new TextComponentTranslation("market.hover.notsell." + id).getFormattedText()); }
				}
				if (!canBuy.isEmpty() || selectDealData.deal.getAmount() <= 0) { hoverBuy.add(new TextComponentTranslation("gui.allowed").getFormattedText()); }
				if (!canSell.isEmpty()) { hoverSell.add(new TextComponentTranslation("gui.allowed").getFormattedText()); }
				// buy hover info
				if (!selectDealData.buyItems.isEmpty()) {
					hoverBuy.add(new TextComponentTranslation("market.hover.item.buy").getFormattedText());
					for (ItemStack curr : selectDealData.buyItems.keySet()) {
						hoverBuy.add(curr.getDisplayName() + TextFormatting.GRAY + " x" + TextFormatting.GOLD + selectDealData.buyItems.get(curr) + " ");
					}
				}
				if (selectDealData.buyMoney > 0) { hoverBuy.add(new TextComponentTranslation("market.hover.currency.buy", selectDealData.buyMoney, CustomNpcs.displayCurrencies).getFormattedText()); }
				if (selectDealData.buyDonat > 0) { hoverBuy.add(new TextComponentTranslation("market.hover.donat.buy", selectDealData.buyDonat, CustomNpcs.displayDonation).getFormattedText()); }
				// sell hover info
				if (!selectDealData.sellItems.isEmpty()) {
					hoverSell.add(new TextComponentTranslation("market.hover.item.sell").getFormattedText());
					for (ItemStack curr : selectDealData.sellItems.keySet()) {
						hoverSell.add(curr.getDisplayName() + TextFormatting.GRAY +" x" + TextFormatting.GOLD + selectDealData.sellItems.get(curr) + " ");
					}
				}
				if (selectDealData.sellMoney > 0) { hoverSell.add(new TextComponentTranslation("market.hover.currency.sell", selectDealData.sellMoney, CustomNpcs.displayCurrencies).getFormattedText()); }
			}
			if (selectDealData.deal.getType() != 1) { buyButton.setHoverText(hoverBuy); }
			if (selectDealData.deal.getType() != 0) { sellButton.setHoverText(hoverSell); }
		}
		addButton(new TradeButtonBiDirectional(this, 6, y, scrollWidth - 5));
		addButton(new GuiNpcCheckBox(11, 3, 3, 26, 12, "type.id", "N", isIdSort)
				.setHoverText("hover.sort", new TextComponentTranslation("market.deals").getFormattedText(), new TextComponentTranslation(isIdSort ? "type.id" : "gui.name").getFormattedText()));
		addTextField(new MarcetTextField(this, 28, ySize - 19, scrollWidth - 31)
				.setHoverText("market.hover.is.search"));
		getTextField(0).setFocused(focus);
		// hover item / case
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (!hasSubGui()) {
			if (keyCode == Keyboard.KEY_UP || keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
				if (!hovers.isEmpty() && isMouseHover(mouseX, mouseY, xSize - 153, 76, 128, hoverHeightMax - 4)) {
					hoverY = ValueUtil.correctInt(hoverY + fontRenderer.FONT_HEIGHT + 1, -hoverMaxY, 0);
					return true;
				} else {
					scrollY = ValueUtil.correctInt(scrollY + 28, -scrollMaxY, 0);
				}
			} else if (keyCode == Keyboard.KEY_DOWN || keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
				if (!hovers.isEmpty() && isMouseHover(mouseX, mouseY, xSize - 153, 76, 128, hoverHeightMax - 4)) {
					hoverY = ValueUtil.correctInt(hoverY - fontRenderer.FONT_HEIGHT - 1, -hoverMaxY, 0);
					return true;
				} else {
					scrollY = ValueUtil.correctInt(scrollY - 28, -scrollMaxY, 0);
				}
			}
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			isScrolled = isMouseHover(mouseX, mouseY, scrollWidth * 2 - 14, 4, 10, ySize - 51);
			if (isScrolled) {
				double yPos = ValueUtil.correctDouble(mouseY, 20.0d, ySize - 71.0d) - 20.0d;
				scrollY = ValueUtil.correctInt((int) (yPos / (ySize - 91.0d) * -scrollMaxY), -scrollMaxY, 0);
			}
			isHovered = isMouseHover(mouseX, mouseY, xSize - 35, 76, 10, hoverHeightMax - 4);
			if (isHovered) {
				double yPos = ValueUtil.correctDouble(mouseY, 86.0d, 61.0d + hoverHeightMax) - 86.0d;
				hoverY = ValueUtil.correctInt((int) (yPos / (hoverHeightMax - 26.0d) * -hoverMaxY), -hoverMaxY, 0);
			}
		}
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (Mouse.isButtonDown(0)) {
			if (isScrolled) {
				double yPos = ValueUtil.correctDouble(mouseY, 20.0d, ySize - 71.0d) - 20.0d;
				scrollY = ValueUtil.correctInt((int) (yPos / (ySize - 91.0d) * -scrollMaxY), -scrollMaxY, 0);
			} else if (isHovered) {
				double yPos = ValueUtil.correctDouble(mouseY, 86.0d, 61.0d + hoverHeightMax) - 86.0d;
				hoverY = ValueUtil.correctInt((int) (yPos / (hoverHeightMax - 26.0d) * -hoverMaxY), -hoverMaxY, 0);
			}
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void save() {
		if (marcet != null) { NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderLivePlayer, marcet.getId()); }
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		wait = false;
		marcet = MarcetController.getInstance().getMarcet(marcet.getId());
		((ContainerNPCTrader) inventorySlots).marcet = marcet;
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) { }

	@Override
	public void textUpdate(String text) {
		search = text;
		initGui();
	}

	@SideOnly(Side.CLIENT)
	public static class TradeButton extends GuiNpcButton {

		protected static final Random rnd = new Random();
		protected final Minecraft mc = Minecraft.getMinecraft();
		protected final Deal deal;
		protected final DealMarkup dm;
		protected final boolean inTrade;
		protected final GuiNPCTrader listener;
		// case
		protected ResourceLocation objCase;
		protected Map<String, String> materialTextures = new HashMap<>();
		protected boolean type;
		protected boolean start;
		protected int rncd;
		// hovers
		protected final List<String> hoverMain = new ArrayList<>();
		protected final List<String> hoverPrise = new ArrayList<>();

		public TradeButton(GuiNPCTrader gui, Deal dealIn, int level, int x, int y, int w, boolean inTradeIn) {
			super(dealIn.getId(), x, y, w, 28, dealIn.getName());
			texture = BUTTONS;
			deal = dealIn;
			txrW = 256;
			txrH = 28;

			listener = gui;
			dm = MarcetController.getInstance().getBuyData(marcet, deal, level, gui.count);
			if (!deal.isCase()) { currentStack = dm.main; }
			inTrade = inTradeIn;
			ITextComponent temp;
			// product info
			if (deal.isCase()) {
				hoverMain.add(new TextComponentTranslation("market.hover.case").getFormattedText());
				hoverMain.add(new TextComponentTranslation("market.deal.case.count", deal.getCaseCount()).getFormattedText());
				if (!deal.showInCase()) {
					temp = new TextComponentTranslation("market.case.show.false");
					temp.getStyle().setColor(TextFormatting.RED);
					hoverMain.add(temp.getFormattedText());
				}
				if (deal.showInCase() ||
						(mc.player != null && mc.player.isCreative()))
				{ deal.putHoverCaseItems(hoverMain, TooltipFlags.NORMAL); }
			}
			else {
				hoverMain.add(new TextComponentTranslation("market.hover.product").getFormattedText());
				hoverMain.add(dm.main.getDisplayName() + TextFormatting.GRAY + " x" + TextFormatting.GOLD + dm.count + " " +
						new TextComponentTranslation("market.hover.item." + (deal.getMaxCount() > 0 ? deal.getAmount() == 0 ? "not" : "amount" : "infinitely"), "" + deal.getAmount()).getFormattedText());
			}
			if (deal.getAmount() > 0 || (mc.player != null && mc.player.isCreative())) {
				if (deal.getAmount() <= 0) { hoverPrise.add(new TextComponentTranslation("gui.allowed").getFormattedText()); }
				// buy hover info
				if (!dm.buyItems.isEmpty()) {
					hoverPrise.add(new TextComponentTranslation("market.hover.item.buy").getFormattedText());
					for (ItemStack curr : dm.buyItems.keySet()) {
						hoverPrise.add(curr.getDisplayName() + TextFormatting.GRAY + " x" + TextFormatting.GOLD + dm.buyItems.get(curr) + " ");
					}
				}
				if (dm.buyMoney > 0) { hoverPrise.add(new TextComponentTranslation("market.hover.currency.buy", dm.buyMoney, CustomNpcs.displayCurrencies).getFormattedText()); }
				if (dm.buyDonat > 0) { hoverPrise.add(new TextComponentTranslation("market.hover.donat.buy", dm.buyDonat, CustomNpcs.displayDonation).getFormattedText()); }
				// sell hover info
				if (!dm.sellItems.isEmpty()) {
					hoverPrise.add(new TextComponentTranslation("market.hover.item.sell").getFormattedText());
					for (ItemStack curr : dm.sellItems.keySet()) {
						hoverPrise.add(curr.getDisplayName() + TextFormatting.GRAY + " x" + TextFormatting.GOLD + dm.sellItems.get(curr) + " ");
					}
				}
				if (dm.sellMoney > 0) { hoverPrise.add(new TextComponentTranslation("market.hover.currency.sell", dm.sellMoney, CustomNpcs.displayCurrencies).getFormattedText()); }
			}
			// case model
			rncd = rnd.nextInt(10000);
			objCase = deal.getCaseObjModel();
			if (objCase != null) {
				try {
					mc.getResourceManager().getResource(objCase);
                    objCase = Deal.defaultCaseOBJ;
                }
				catch (Exception e) { objCase = null; }
			}
			materialTextures.put("minecraft:entity/chest/christmas", deal.getCaseTexture().toString());
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (!visible) { return; }
			GlStateManager.enableBlend();
			int posY = y + listener.scrollY;
			hovered = mouseY > 14 && mouseY < listener.ySize - 47 && mouseX >= x && mouseY >= posY && mouseX < x + width && mouseY < posY + height;
			if (hovered) {
				hoverText.clear();
				hoverText.addAll(hoverMain);
			}
			int posX = x;
			if (posY + height < 15 || posY > listener.ySize - 48) { return; }
			posY = y;

			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = listener.xSize < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / (double) listener.xSize) : 1;
			GL11.glScissor(x * c, 48 * c, (width + 1) * c, (listener.ySize - 62) * c);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, listener.scrollY, 1.0f);

			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			boolean isPrefabricated = txrW == 0;
			float scaleH = height / (float) txrH;
			float scaleW = isPrefabricated ? scaleH : width / (float) txrW;
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			GlStateManager.translate(posX / scaleW, posY / scaleH, 0.0f);
			mc.getTextureManager().bindTexture(texture);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(0, 0, txrX, txrY + getState(inTrade) * txrH, txrW, txrH);
			GlStateManager.popMatrix();

			// rarity color
			if (deal.getRarityColor() != 0) { drawGradientRect(posX + 2, posY + 2, posX + width - 2, posY + height - 2, 0x0, deal.getRarityColor() | 0x80000000); }
			// case obj model
			if (deal.isCase() && objCase != null) {
				GlStateManager.pushMatrix();
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.translate(posX + 16.0f, posY + 8.5f, 16.0f);
				if ((System.currentTimeMillis() + rncd) % 10000 < 2000 || hovered && isMouseHover(mouseX, mouseY, posX + 1, posY + listener.scrollY + 2, 32, 22)) {
					float i = (float) ((System.currentTimeMillis() + rncd) % 2000);
					if (!start) {
						GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
						GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
						GlStateManager.scale(16.0f, -16.0f, 16.0f);
						GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
						if (i >= 1980) { start = true; }
					}
					else {
						if (i <= 20) { type = rnd.nextFloat() < 0.5f; }
						float rot;
						if (type) {
							if (i < 600) { rot = -0.033333f * i; }
							else if (i < 1700) { rot = 0.027273f * i - 36.363636f; }
							else { rot = - 0.033333f * i + 66.666666f; }
							GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
							GlStateManager.rotate(-75.0f + rot, 0.0f, 1.0f, 0.0f);
							GlStateManager.scale(16.0f, -16.0f, 16.0f);
							GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
						}
						else {
							GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
							GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
							GlStateManager.scale(16.0f, -16.0f, 16.0f);
							GlStateManager.callList(ModelBuffer.getDisplayList(objCase, Collections.singletonList("body"), materialTextures));
							if (i < 1500) { rot = 0.016667f * i; }
							else if (i < 1900) { rot = 25.0f; }
							else { rot = - 0.25f * i + 500.0f; }
							GlStateManager.pushMatrix();
							GlStateManager.rotate(rot, 0.0f, 0.0f, 1.0f);
							GlStateManager.callList(ModelBuffer.getDisplayList(objCase, Collections.singletonList("top"), materialTextures));
							GlStateManager.popMatrix();
						}
					}
				}
				else {
					GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
					GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
					GlStateManager.scale(16.0f, -16.0f, 16.0f);
					GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
				}
				GlStateManager.popMatrix();
			}
			if (currentStack != null && !currentStack.isEmpty()) {
				mc.getTextureManager().bindTexture(GuiNPCTrader.ICONS);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect( posX + 6, posY + 2, 0, getState(true) * 24, 24, 24);
				if (!inTrade) { GlStateManager.color(0.4F, 0.4F, 0.4F, 1.0F); }
				mc.getRenderItem().renderItemAndEffectIntoGUI(currentStack, posX + 10, posY + 6);
				mc.getRenderItem().renderItemOverlays(mc.fontRenderer, currentStack, posX + 10, posY + 6);
				if (!inTrade) { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
				if (hovered && isMouseHover(mouseX, mouseY, posX + 6, posY + listener.scrollY + 3, 22, 22)) {
					hoverText.clear();
					hoverText.addAll(currentStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				}
			}
			// money and barter
			int mw = 0;
			if (deal.getAmount() > 0 || (mc.player != null && mc.player.isCreative())) {
				// money
				ITextComponent money = new TextComponentString("");
				if (dm.sellMoney > 0) {
					money.appendText(TextFormatting.YELLOW + "↑" + TextFormatting.RESET + Util.instance.getTextReducedNumber(dm.sellMoney, true, true, false));
				}
				if (dm.buyMoney > 0) {
					if (!money.getFormattedText().isEmpty()) { money.appendText(" "); }
					money.appendText(TextFormatting.GREEN + "↓" + TextFormatting.RESET + Util.instance.getTextReducedNumber(dm.buyMoney, true, true, true));
				}
				ITextComponent donat = new TextComponentString("");
				if (dm.buyDonat > 0) {
					if (!donat.getFormattedText().isEmpty()) { donat.appendText(" "); }
					donat.appendText(TextFormatting.BLUE + "↓" + TextFormatting.RESET + Util.instance.getTextReducedNumber(dm.buyDonat, true, true, false));
				}
				int mt = 0;
				boolean hasM = !money.getFormattedText().isEmpty();
				boolean hasD = !donat.getFormattedText().isEmpty();
				if (hasM && hasD) {
					posX = x + width - 14;
					posY = y + 3;
					mt = 1;
					mw = mc.fontRenderer.getStringWidth(money.getFormattedText());
					if (System.currentTimeMillis() % 4000 < 2000) { mt = 2; mw = mc.fontRenderer.getStringWidth(donat.getFormattedText()); }
				}
				else if (hasM || hasD) {
					posX = x + width - 14;
					posY = y + 3;
					if (hasM) { mt = 1; mw = mc.fontRenderer.getStringWidth(money.getFormattedText()); }
					if (hasD) { mt = 2; mw = mc.fontRenderer.getStringWidth(donat.getFormattedText()); }
				}
				// draw prise info
				if (mt != 0) {
					posX -= mw;
					mc.fontRenderer.drawString((mt == 1 ? money : donat).getFormattedText(), posX, posY, CustomNpcs.MainColor.getRGB() | 0xFF000000, false);
					GlStateManager.pushMatrix();
					GlStateManager.enableAlpha();
					GlStateManager.enableBlend();
					GlStateManager.translate(posX + mw - 2, posY - 4, 0.0f);
					GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
					mc.getTextureManager().bindTexture(mt == 1 ? GuiNPCInterface.MONEY : GuiNPCInterface.DONAT);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
					if (hovered && isMouseHover(mouseX, mouseY, posX, posY + listener.scrollY, mw + 14, 10)) {
						hoverText.clear();
						hoverText.addAll(hoverPrise);
					}
				}
				// barter
				if (!dm.buyItems.isEmpty()) {
					float sc = 1.0f;
					int size = dm.buyItems.size();
					mw = size * 16;
					if (width - 34 < mw) { sc = (width - 34.0f) / (float) mw; }
					float s = 0.666666f * sc;
					// slots
					GlStateManager.pushMatrix();
					GlStateManager.enableAlpha();
					GlStateManager.enableBlend();
					GlStateManager.translate(x + width - 2 - mw * sc, y + height - 2.0f - 16.0f * sc, 0.0f);
					GlStateManager.scale(s, s, s);
					mc.getTextureManager().bindTexture(GuiNPCTrader.ICONS);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					for (int i = 0; i < size; i++) { drawTexturedModalRect(i * 24, 0, 0, 0, 24, 24); }
					GlStateManager.popMatrix();
					s = 0.875f * sc;
					// stacks
					GlStateManager.pushMatrix();
					posX = (int) (x + width - 1 - mw * sc);
					posY = (int) (y + height - 1.0f - 16.0f * sc);
					GlStateManager.translate(posX, posY, 0.0f);
					GlStateManager.scale(s, s, s);
					int i = 0;
					for (ItemStack stack : dm.buyItems.keySet()) {
						mc.getRenderItem().renderItemAndEffectIntoGUI(stack, i * 18, 0);
						GlStateManager.pushMatrix();
						String sCount = Util.instance.getTextReducedNumber(dm.buyItems.get(stack), true, true, false);
						GlStateManager.translate(i * 18 + 17, 16.0F, 200.0F);
						GlStateManager.scale(0.75f, 0.75f, 0.75f);
						GlStateManager.disableLighting();
						GlStateManager.disableDepth();
						GlStateManager.disableBlend();
						mc.fontRenderer.drawStringWithShadow(sCount, 24.0f - (float) mc.fontRenderer.getStringWidth(sCount), -6.0f, 0xFFFFFF);
						GlStateManager.enableLighting();
						GlStateManager.enableDepth();
						GlStateManager.enableBlend();
						GlStateManager.popMatrix();

						if (hovered && isMouseHover(mouseX, mouseY, posX + (i * 18) * s, posY + listener.scrollY, 16.0f * s, 16.0f * s)) {
							hoverText.clear();
							hoverText.addAll(stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
						}
						i++;
					}
					GlStateManager.popMatrix();
				}
			}
			// name
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 36.0f, y + 2.0f + listener.scrollY, 1.0f);
			GuiNpcButton.renderString(mc.fontRenderer, getDisplayString(), 0, 0, width - 39 - mw, 10, CustomNpcs.MainColor.getRGB() | 0xFF000000, false, false);
			GlStateManager.popMatrix();

			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		public int getState(boolean tradeIn) {
			if (!tradeIn) { return 2; }
			if (!listener.hasSubGui()) {
				try {
					if (listener.selectDealData.deal.equals(deal)) { return 1; }
				}
				catch (Exception ignored) { }
				if (hovered && !listener.hasSubGui()) {
					return Mouse.isButtonDown(0) ? 2 : 1;
				}
			}
			return 0;
		}

		public boolean isMouseHover(double mX, double mY, double px, double py, double pWidth, double pHeight) {
			return mX >= px && mY >= py && mX < (px + pWidth) && mY < (py + pHeight);
		}

	}

	@SideOnly(Side.CLIENT)
	public class SectionButton extends GuiMenuSideButton {

		protected final GuiNPCTrader listener;

		public SectionButton(GuiNPCTrader gui, int id, MarcetSection sectionIn, int x, int y) {
			super(id, x, y, "");
			width = sectionIn == null ? 16 : 24;
			height = sectionIn == null ? 9 : 24;
			listener = gui;
			texture = GuiNPCTrader.ICONS;
			if (sectionIn != null) {
				txrX = (sectionIn.getIcon() % 10) * 24;
				txrY = (int) Math.floor((float) sectionIn.getIcon() / 10.0f) * 72;
			} else {
				txrX = 240;
				txrY = id == 3 ? 27 : 0;
			}
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (!visible) { return; }
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			int state = 0;
			boolean lbm = Mouse.isButtonDown(0);
			if (hovered && !listener.hasSubGui()) { state = (lbm ? 2 : 1) * height; }
			else if (active) { state = height; }
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(texture);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			drawTexturedModalRect(x, y, txrX, txrY + state, width, height);
			if (hovered && !hoverText.isEmpty()) {
				if (listener != null) { listener.putHoverText(hoverText); }
				else { drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY); }
			}
			GlStateManager.popMatrix();
		}

	}

	@SideOnly(Side.CLIENT)
	public class TradeButtonBiDirectional extends GuiButtonBiDirectional {

		protected final GuiNPCTrader listener;

		public TradeButtonBiDirectional(GuiNPCTrader gui, int x, int y, int w) {
			super(0, x, y, w, 20, new String[1], 0);
			texture = GuiNPCTrader.BUTTONS;
			txrY = 84;
			txrW = 256;
			txrH = 20;
			listener = gui;
			display = new String[64];
			for (int i = 0; i < 64; i++) { display[i] = "" + (i + 1); }
			displayValue = gui.count - 1;
			if (displayValue < display.length) { setDisplayText(display[displayValue]); }
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (!visible) { return; }
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			hoverL = mouseX >= x && mouseY >= y && mouseX < x + 20 && mouseY < y + height;
			hoverR = !hoverL && mouseX >= x + width - 19 && mouseY >= y && mouseX < x + width && mouseY < y + height;

			boolean lmb = Mouse.isButtonDown(0);
			int stateL = !enabled ? 40 : hoverL ? (display.length > 1 ? lmb ? 40 : 20 : 0) : 0;
			int stateR = !enabled ? 40 : hoverR ? (display.length > 1 ? lmb ? 40 : 20 : 0) : 0;
			int state = !enabled ? 40 : hovered && display.length > 1 ? 20 : 0;
			int wl = (width - 38) / 2;
			int wr = width - 39 - wl;

			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.translate(x, y, 1.0f);
			mc.getTextureManager().bindTexture(GuiNPCTrader.BUTTONS);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(0, 0, 0, txrY + stateL, 19, 20);
			drawTexturedModalRect(width - 20, 0, 256 - 19, txrY + stateR, 19, 20);
			drawTexturedModalRect(19, 0, 19, txrY + state, wl, 20);
			drawTexturedModalRect(19 + wl, 0, 236 - wr, txrY + state,  wr, 20);
			GlStateManager.popMatrix();

			String text = "";
			float maxWidth = (width - 36);
			if (checkWidth && mc.fontRenderer.getStringWidth(displayString) > maxWidth) {
				for (int h = 0; h < displayString.length(); ++h) {
					text += displayString.charAt(h);
					if (mc.fontRenderer.getStringWidth(text) > maxWidth) { break; }
				}
				text += "...";
			}
			else { text = displayString; }
			if (hovered && enabled) { text = TextFormatting.UNDERLINE + text; }
			int c = color;
			if (packedFGColour != 0) { c = packedFGColour; }
			else if (!enabled) { c = CustomNpcs.NotEnableColor.getRGB(); }
			else if (hovered) { c = CustomNpcs.HoverColor.getRGB(); }

			renderString(mc.fontRenderer, text, x + 11, y, x + width - 11, y + height, c, showShadow, true);

			if (hovered && !hoverText.isEmpty()) {
				if (listener != null) { listener.putHoverText(hoverText); }
				else { drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY); }
			}
		}

	}

	@SideOnly(Side.CLIENT)
	public static class MarcetTextField extends GuiNpcTextField {

		public MarcetTextField(GuiNPCTrader gui, int x, int y, int widthIn) {
			super(0, gui, x, y, widthIn, 18, GuiNPCTrader.search);
			setEnableBackgroundDrawing(false);
		}

		@Override
		public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
			if (!enabled || !getVisible()) { return; }
			setTextColor(isFocused() ? 0xFFE0E0E0 : 0xFF707070);
			int posX = x - 3;
			int posY = y - 6;
			int w = width + 6;
			hovered = mouseX >= posX && mouseY >= posY && mouseX < posX + w && mouseY < posY + height + 2;

			int w0 = w / 2;
			int w1 = w - w0;
			int state = isFocused() || !hovered ? 56 : 0;
			Minecraft mc = Minecraft.getMinecraft();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(GuiNPCTrader.BUTTONS);
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			drawTexturedModalRect(posX, posY, 0, state, w0, 10); // left1
			drawTexturedModalRect(posX, posY + 10, 0, state + 18, w0, 10); // left down
			drawTexturedModalRect(posX + w0, posY, 256 - w1, state, w1, 10); // right up
			drawTexturedModalRect(posX + w0, posY + 10, 256 - w1, state + 18, w1, 10); // right down
			super.drawTextBox();
			GlStateManager.popMatrix();
			if (hovered && !hoverText.isEmpty() && listener instanceof GuiNPCTrader) { ((GuiNPCTrader) listener).putHoverText(hoverText); }
		}

	}

}
