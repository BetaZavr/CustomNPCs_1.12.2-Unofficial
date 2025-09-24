package noppes.npcs.client.gui.roles;

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

import javax.annotation.Nonnull;

public class SubGuiNpcSelectTrader extends SubGuiInterface implements IGuiData, ICustomScrollListener {

	protected final Map<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scrollMarkets;
	protected String select = "";

	public SubGuiNpcSelectTrader(int id) {
		super(id);
		setBackground("menubg.png");
		xSize = 190;
		ySize = 217;
		closeOnEsc = true;

		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
        if (button.getID() == 66) { onClosed(); }
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
			if (!m.isValid()) { continue; }
			String name = m.getSettingName();
			list.add(name);
			data.put(name, m.getId());
			if (id == m.getId()) { select = name; }
		}
		if (scrollMarkets == null) { scrollMarkets = new GuiCustomScroll(this, 0).setSize(170, 157); }
		int x = guiLeft + 12, y = guiTop + 14;
		scrollMarkets.guiLeft = x;
		scrollMarkets.guiTop = y;
		scrollMarkets.setList(list);
		if (data.containsValue(id) && !select.isEmpty()) { scrollMarkets.setSelected(select); }
		addScroll(scrollMarkets);
		addLabel(new GuiNpcLabel(0, "market.select", x + 2, y - 10).setHoverText("market.hover.role.list"));
		addButton(new GuiNpcButton(66, guiLeft + 50, guiTop + 190, 90, 20, "gui.done").setHoverText("hover.back"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getSelected().equals(select) || !data.containsKey(scroll.getSelected())) { return; }
		select = scroll.getSelected();
		id = data.get(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { onClosed(); }

	@Override
	public void setGuiData(NBTTagCompound compound) { initGui(); }

}
