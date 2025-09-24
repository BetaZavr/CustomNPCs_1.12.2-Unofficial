package noppes.npcs.client.gui.player;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.Util;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiTransportSelection extends GuiNPCInterface implements IScrollData, ICustomScrollListener {

	protected final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	protected GuiCustomScroll scroll;
	protected final Map<String, Integer> data = new TreeMap<>();
	protected TransportLocation locSel;
	protected Map<ItemStack, Boolean> barterItems;
	protected boolean canTransport = true;
	protected int bxSize = 0;
	protected int bySize = 0;

	public GuiTransportSelection(EntityNPCInterface npc) {
		super(npc);
		drawDefaultBackground = false;
		xSize = 176;
		title = "";

		NoppesUtilPlayer.sendData(EnumPlayerPacket.TransportCategoriesGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && button.getID() == 0 && locSel != null) {
			onClosed();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Transport, locSel.id);
		}
	}

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 222);
		barterItems = null;
		if (locSel == null) { return; }
		if (bxSize > 0) {
			int w = bxSize + 13;
			int h = bySize + 18;
			int x = guiLeft + 176;
			int y = guiTop + 14;
			mc.getTextureManager().bindTexture(resource);
			drawTexturedModalRect(x, y, 176 - w, 0, w, h);
			drawTexturedModalRect(x, y + h, 176 - w, 218, w, 4);
			x += 5;
			y += 4;
			if (!locSel.inventory.isEmpty()) { fontRenderer.drawString(new TextComponentTranslation("market.barter").getFormattedText(), x, y, CustomNpcs.LableColor.getRGB(), false); }
			if (locSel.money > 0L) {
				y += 32;
				fontRenderer.drawString(Util.instance.getTextReducedNumber(locSel.money, true, true, false) + " " + CustomNpcs.displayCurrencies, x, y, CustomNpcs.LableColor.getRGB(), false);
			}
		}
		// Items
		bxSize = 0;
		bySize = 0;
		if (!locSel.inventory.isEmpty()) {
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			barterItems = Util.instance.getInventoryItemCount(player, locSel.inventory);
			int slot = 0;
			canTransport = true;
			for (ItemStack stack : barterItems.keySet()) {
				int u = guiLeft + xSize + 5 + (slot % 3) * 18;
				int v = guiTop + 30 + (slot / 3) * 18;
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(u, v, 0, 0, 18, 18);
				if (canTransport) { canTransport = barterItems.get(stack); }
				if (getButton(0) != null && getButton(0).isHovered()) {
					Gui.drawRect(u + 1, v + 1, u + 17, v + 17, barterItems.get(stack) ? 0x8000FF00 : player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000);
				}
				slot++;
			}
			float a = (float) slot / 3.0f;
			bxSize = (a >= 1.0f ? 3 : a >= 2.0f / 3.0f ? 2 : 1) * 18;
			bySize = (int) (Math.ceil(a) * 18.0d);
			GlStateManager.popMatrix();
		}
		if (locSel.money > 0) { bySize += 14; }
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (locSel != null) {
			if (!locSel.inventory.isEmpty()) {
				int slot = 0;
				for (ItemStack stack : barterItems.keySet()) {
					int u = guiLeft + xSize + 5 + (slot % 3) * 18;
					int v = guiTop + 31 + (slot / 3) * 18;
					GlStateManager.pushMatrix();
					GlStateManager.translate(u, v, 50.0f);
					RenderHelper.enableGUIStandardItemLighting();
					mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					drawString(mc.fontRenderer, "" + stack.getCount(), (13 - (stack.getCount() > 9 ? 6 : 0)), 9, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
					if (isMouseHover(mouseX, mouseY, u, v, 18, 18)) {
                        putHoverText(new ArrayList<>(stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL)));
					}
					slot++;
				}
			}
		}
		if (getButton(0) != null) {
			GuiNpcButton button = getButton(0);
			button.setIsEnable(canTransport && locSel != null);
			if (!button.enabled && button.isHovered()) {
				if (locSel == null) { putHoverText(new TextComponentTranslation("transporter.hover.not.select").getFormattedText()); }
				else if (locSel.money > ClientProxy.playerData.game.getMoney()) { putHoverText(new TextComponentTranslation("transporter.hover.not.money").getFormattedText()); }
				else { putHoverText(new TextComponentTranslation("transporter.hover.not.item").getFormattedText()); }
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - 222) / 2;
		String title = "";
		TransportController tData = TransportController.getInstance();
		if (npc != null && npc.advanced.roleInterface instanceof RoleTransporter) {
			int id = ((RoleTransporter) npc.advanced.roleInterface).transportId;
			TransportLocation loc = tData.getTransport(id);
			if (loc != null) {
				title = new TextComponentTranslation(loc.category.title).getFormattedText() + ": " + new TextComponentTranslation(loc.name).getFormattedText();
			}
		}
		addLabel(new GuiNpcLabel(0, title, guiLeft + (xSize - fontRenderer.getStringWidth(title)) / 2, guiTop + 10));
		addButton(new GuiNpcButton(0, guiLeft + 10, guiTop + 192, 156, 20, new TextComponentTranslation("transporter.travel").getFormattedText()));
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0); }
		scroll.setSize(156, 165);
		scroll.guiLeft = guiLeft + 10;
		scroll.guiTop = guiTop + 20;
		addScroll(scroll);

		List<String> list = new ArrayList<>(data.keySet());
		scroll.setList(list);
		if (!data.isEmpty()) {
			List<String> suffixes = new ArrayList<>();
			List<Integer> colors = new ArrayList<>();
			for (String name : list) {
				int color = new Color(0xFF00FF00).getRGB();
				int redC = new Color(0xFFFF0000).getRGB();
				String sfx = "";
				TransportLocation loc = tData.getTransport(data.get(name));
				if (loc != null) {
					if (loc.money > 0 || !loc.inventory.isEmpty()) {
						if (loc.money > 0) {
							sfx = Util.instance.getTextReducedNumber(loc.money, true, true, false) + " "
									+ CustomNpcs.displayCurrencies;
							if (loc.money > 0 && loc.money > ClientProxy.playerData.game.getMoney()) { color = redC; }
						}
					}
					if (!loc.inventory.isEmpty() && color != redC) {
						sfx += ((char) 167) + "7 [" + ((char) 167) + "6I" + ((char) 167) + "7]";
						Map<ItemStack, Boolean> items = Util.instance.getInventoryItemCount(player, loc.inventory);
						for (ItemStack s : items.keySet()) {
							if (!items.get(s)) { color = redC; break; }
						}
					}
				}
				suffixes.add(sfx);
				colors.add(color);
			}
			scroll.setColors(colors);
			scroll.setSuffixes(suffixes);
		}
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui != null) { return subgui.keyCnpcsPressed(typedChar, keyCode); }
		if (keyCode == Keyboard.KEY_ESCAPE || isInventoryKey(keyCode)) {
			onClosed();
			return true;
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (data.containsKey(scroll.getSelected())) {
			locSel = TransportController.getInstance().getTransport(data.get(scroll.getSelected()));
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (locSel != null) {
			onClosed();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Transport, locSel.id);
		}
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		for (String key : dataMap.keySet()) {
			String name = new TextComponentTranslation(key).getFormattedText();
			data.put(name, dataMap.get(key));
		}
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

}
