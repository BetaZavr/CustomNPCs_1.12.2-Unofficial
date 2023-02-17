package noppes.npcs.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.AdditionalMethods;

@SideOnly(Side.CLIENT)
public class ClientGuiEventHandler
extends Gui
{
	
	protected ResourceLocation coinNpc = new ResourceLocation(CustomNpcs.MODID, "textures/items/coin_gold.png");

	@SubscribeEvent
	public void onDrawScreenEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
		Minecraft mc = event.getGui().mc;
		if (!(event.getGui() instanceof GuiInventory) || !CustomNpcs.showMoney) { return; }
		String text = AdditionalMethods.getTextReducedNumber(CustomNpcs.proxy.getPlayerData(mc.player).game.money, true, true, false) + CustomNpcs.charCurrencies;
		GlStateManager.pushMatrix();
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		int x = ((GuiInventory) mc.currentScreen).getGuiLeft()+122;
		int y = ((GuiInventory) mc.currentScreen).getGuiTop() + 61;
		GlStateManager.translate(x, y, 0.0f);
		mc.renderEngine.bindTexture(this.coinNpc);
		float s = 16.0f / 250.f;
		GlStateManager.scale(s, s, s);
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		mc.fontRenderer.drawString(text, x+15, y+8 / 2, 0x404040, false);
		GlStateManager.popMatrix();
		int xm = event.getMouseX(), ym = event.getMouseY();
		if (xm>x&& ym>y && xm<x+50  && ym<y+12) {
			List<String> hoverText = new ArrayList<String>();
			hoverText.add(new TextComponentTranslation("inventory.hover.currency").getFormattedText());
			hoverText.add("" + CustomNpcs.proxy.getPlayerData(mc.player).game.money);
			event.getGui().drawHoveringText(hoverText, xm, ym);
		}
	}
	
}
