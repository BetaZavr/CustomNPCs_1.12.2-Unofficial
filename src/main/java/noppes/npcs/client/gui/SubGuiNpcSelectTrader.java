package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Marcet;

public class SubGuiNpcSelectTrader
extends SubGuiInterface
implements IGuiData, ICustomScrollListener {

	public int id;
	private final Map<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scrollMarkets;
	private String select = "";

	public SubGuiNpcSelectTrader(int i) {
		setBackground("menubg.png");
		xSize = 190;
		ySize = 217;
		closeOnEsc = true;

		id = i;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
        if (button.getId() == 66) {
            close();
        }
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (background != null) { // add right
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			mc.getTextureManager().bindTexture(background);
			drawTexturedModalRect(xSize, 0, 252, 0, 4, ySize);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> list = new ArrayList<>();
		data.clear();
		for (Marcet m : MarcetController.getInstance().markets.values()) {
			if (!m.isValid()) {
				continue;
			}
			String name = m.getSettingName();
			list.add(name);
			data.put(name, m.getId());
			if (id == m.getId()) {
				select = name;
			}
		}
		if (scrollMarkets == null) {
			(scrollMarkets = new GuiCustomScroll(this, 0)).setSize(170, 157);
		}
		int x = guiLeft + 12, y = guiTop + 14;
		scrollMarkets.guiLeft = x;
		scrollMarkets.guiTop = y;
		scrollMarkets.setList(list);
		if (data.containsValue(id) && !select.isEmpty()) {
			scrollMarkets.setSelected(select);
		}
		GuiNpcLabel label = new GuiNpcLabel(0, "market.select", x + 2, y - 10);
		label.setHoverText("market.hover.role.list");
		addLabel(label);
		addScroll(scrollMarkets);

		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 50, guiTop + 190, 90, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getSelected().equals(select) || !data.containsKey(scroll.getSelected())) {
			return;
		}
		select = scroll.getSelected();
		id = data.get(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		close();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		initGui();
	}

}
