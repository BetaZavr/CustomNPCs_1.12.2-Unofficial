package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ITopButtonListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.AdditionalMethods;

public class GuiTransportSelection
extends GuiNPCInterface
implements ITopButtonListener, IScrollData, ICustomScrollListener {
	
	private boolean canTransport;
	protected int bxSize, bySize;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private GuiCustomScroll scroll;
	private final Map<String, Integer> data;
	private TransportLocation locSel;
	private Map<ItemStack, Boolean> barterItems;

	public GuiTransportSelection(EntityNPCInterface npc) {
		super(npc);
		this.xSize = 176;
		this.drawDefaultBackground = false;
		this.title = "";
		this.data = Maps.<String, Integer>newTreeMap();
		this.locSel = null;
		this.canTransport = true;
		this.bxSize = 0;
		this.bySize = 0;
		Client.sendDataDelayCheck(EnumPlayerPacket.TransportCategoriesGet, this, 0);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0 && this.locSel!=null) {
			this.close();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Transport, this.locSel.id);
		}
	}

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 222);
		this.barterItems = null;
		if (this.locSel==null) { return; }

		if (this.bxSize>0) {
			int w = this.bxSize + 13;
			int h = this.bySize + 18;
			int x = this.guiLeft + 176;
			int y = this.guiTop + 14;
			this.mc.renderEngine.bindTexture(this.resource);
			this.drawTexturedModalRect(x, y, 176 - w, 0, w, h);
			this.drawTexturedModalRect(x, y + h, 176 - w, 218, w, 4);
			x += 5;
			y += 4;
			if (!this.locSel.inventory.isEmpty()) {
				this.fontRenderer.drawString(new TextComponentTranslation("gui.market.barter").getFormattedText(), x, y, CustomNpcs.lableColor, false);
			}
			if (this.locSel.money>0L) {
				y += 32;
				this.fontRenderer.drawString(AdditionalMethods.getTextReducedNumber(this.locSel.money, true, true, false)+" "+CustomNpcs.charCurrencies.charAt(0), x, y, CustomNpcs.lableColor, false);
			}
		}
		// Items
		this.bxSize = 0;
		this.bySize = 0;
		if (!this.locSel.inventory.isEmpty()) {
			GlStateManager.pushMatrix();
			this.mc.renderEngine.bindTexture(this.slot);
			this.barterItems = AdditionalMethods.getInventoryItemCount(this.player, this.locSel.inventory);
			int slot = 0;
			this.canTransport = true;
			for (ItemStack stack : this.barterItems.keySet()) {
				int u = this.guiLeft + this.xSize + 5 + (slot % 3) * 18;
				int v = this.guiTop + 18 + (slot / 3) * 18;
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(u, v, 0, 0, 18, 18);
				if (this.canTransport) { this.canTransport = this.barterItems.get(stack); }
				if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
					Gui.drawRect(u + 1, v + 1, u + 17, v + 17, this.barterItems.get(stack) ? 0x8000FF00 : this.player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000);
				}
				slot++;
			}
			float a = (float) slot / 3.0f;
			this.bxSize = (a >= 1.0f ? 3 : a >= 2.0f / 3.0f ? 2 : 1) * 18;
			this.bySize = (int) (Math.ceil(a) * 18.0d);
			GlStateManager.popMatrix();
		}
		if (this.locSel.money > 0) {
			this.bySize += 14;
		}
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		if (this.locSel!=null) {
			if (!this.locSel.inventory.isEmpty()) {
				int slot = 0;
				for (ItemStack stack : this.barterItems.keySet()) {
					int u = this.guiLeft + this.xSize + 5 + (slot % 3) * 18;
					int v = this.guiTop + 18 + (slot / 3) * 18;
					GlStateManager.pushMatrix();
					GlStateManager.translate(u, v, 50.0f);
					RenderHelper.enableGUIStandardItemLighting();
					this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					this.drawString(this.mc.fontRenderer, "" + stack.getCount(), (13 - (stack.getCount() > 9 ? 6 : 0)), 10, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
					if (isMouseHover(i, j, u, v, 18, 18)) {
						List<String> list = new ArrayList<String>();
						//list.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						list.addAll(stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
						this.hoverText = list.toArray(new String[list.size()]);
					}
					
					slot++;
				}
			}
		}
		if (this.getButton(0)!=null) {
			GuiNpcButton button = this.getButton(0);
			button.setEnabled(this.canTransport && this.locSel!=null);
			if (!button.enabled && button.isMouseOver()) {
				if (this.locSel==null) {
					this.setHoverText(new TextComponentTranslation("transporter.hover.not.select").getFormattedText());
				} else if (this.locSel.money > ClientProxy.playerData.game.getMoney()) {
					this.setHoverText(new TextComponentTranslation("transporter.hover.not.money").getFormattedText());
				} else {
					this.setHoverText(new TextComponentTranslation("transporter.hover.not.item").getFormattedText());
				}
			}
		}
		super.drawScreen(i, j, f);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - 222) / 2;
		String title = "";
		TransportController tData = TransportController.getInstance();
		if (this.npc!=null && this.npc.advanced.roleInterface instanceof RoleTransporter) {
			int id = ((RoleTransporter) this.npc.advanced.roleInterface).transportId;
			TransportLocation loc = tData.getTransport(id);
			if (loc!=null) {
				title = new TextComponentTranslation(loc.category.title).getFormattedText()+": "+new TextComponentTranslation(loc.name).getFormattedText();
			}
		}
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + (this.xSize - this.fontRenderer.getStringWidth(title)) / 2, this.guiTop + 10));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 10, this.guiTop + 192, 156, 20, new TextComponentTranslation("transporter.travel").getFormattedText()));
		if (this.scroll == null) { this.scroll = new GuiCustomScroll(this, 0); }
		this.scroll.setSize(156, 165);
		this.scroll.guiLeft = this.guiLeft + 10;
		this.scroll.guiTop = this.guiTop + 20;
		this.addScroll(this.scroll);
		
		List<String> list = Lists.newArrayList(this.data.keySet());
		this.scroll.setList(list);
		if (!this.data.isEmpty()) {
			List<String> suffixs = Lists.<String>newArrayList();
			List<Integer> colors = Lists.<Integer>newArrayList();
			for (String name : list) {
				int color = 0xFF00FF00;
				String sfx = "";
				TransportLocation loc = tData.getTransport(this.data.get(name));
				if (loc!=null) {
					if (loc.money>0 || !loc.inventory.isEmpty()) {
						if (loc.money>0) {
							sfx = AdditionalMethods.getTextReducedNumber(loc.money, true, true, false)+" "+CustomNpcs.charCurrencies.charAt(0);
							if (loc.money>0 && loc.money>ClientProxy.playerData.game.getMoney()) { color = 0xFFFF0000; }
						}
					}
					if (!loc.inventory.isEmpty() && color != 0xFFFF0000) {
						sfx += ((char) 167)+"7 ["+((char) 167)+"6I"+((char) 167)+"7]";
						Map<ItemStack, Boolean> items = AdditionalMethods.getInventoryItemCount(this.player, loc.inventory);
						for (ItemStack s : items.keySet()) {
							if (!items.get(s)) {
								color = 0xFFFF0000;
								break;
							}
						}
					}
				}
				suffixs.add(sfx);
				colors.add(color);
			}
			this.scroll.setColors(colors);
			this.scroll.setSuffixs(suffixs);
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

	@Override
	public void save() {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		for (String key : data.keySet()) {
			String name = new TextComponentTranslation(key).getFormattedText();
			this.data.put(name, data.get(key));
		}
		this.initGui();
	}

	@Override
	public void setSelected(String selected) { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.data.containsKey(scroll.getSelected())) {
			this.locSel = TransportController.getInstance().getTransport(this.data.get(scroll.getSelected()));
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (this.locSel!=null) {
			this.close();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Transport, this.locSel.id);
		}
	}
	
}
