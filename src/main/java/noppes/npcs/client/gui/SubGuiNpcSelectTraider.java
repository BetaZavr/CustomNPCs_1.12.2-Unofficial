package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Marcet;

public class SubGuiNpcSelectTraider extends SubGuiInterface implements IGuiData, ICustomScrollListener {
	
	public int id;
	private Map<String, Integer> data;
	private GuiCustomScroll scrollMarcets;
	private String select;

	public SubGuiNpcSelectTraider(int id) {
		this.id = id;
		this.setBackground("menubg.png");
		this.xSize = 190;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.data = new HashMap<String, Integer>();
		this.select = "";
		Client.sendData(EnumPacketServer.TraderMarketGet, new Object[0]);
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> list = new ArrayList<String>();
		this.data.clear();
		for (Marcet m : MarcetController.getInstance().marcets.values()) {
			if (!m.isValid()) { continue; }
			String name = m.getSettingName();
			list.add(name);
			this.data.put(name, m.getId());
			if (this.id == m.getId()) { this.select = name; }
		}
		if (this.scrollMarcets == null) { (this.scrollMarcets = new GuiCustomScroll(this, 0)).setSize(170, 157); }
		int x = this.guiLeft + 12, y = this.guiTop + 14;
		this.scrollMarcets.guiLeft = x;
		this.scrollMarcets.guiTop = y;
		this.scrollMarcets.setList(list);
		if (this.data.containsValue(this.id) && !this.select.isEmpty()) {
			this.scrollMarcets.setSelected(this.select);
		}
		this.addLabel(new GuiNpcLabel(0, "market.select", x + 2, y - 10));
		this.addScroll(this.scrollMarcets);
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 50, this.guiTop + 190, 90, 20, "gui.done"));
	}


	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.background != null && this.mc.renderEngine != null) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			this.mc.renderEngine.bindTexture(this.background);
			this.drawTexturedModalRect(this.xSize, 0, 252, 0, 4, this.ySize);
			GlStateManager.popMatrix();
		}
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions || this.subgui != null) {
			return;
		}
		if (this.getLabel(0)!=null && this.getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.role.list").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.role.own").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (scroll.getSelected().equals(this.select) || !this.data.containsKey(scroll.getSelected())) {
			return;
		}
		this.select = scroll.getSelected();
		this.id = this.data.get(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.close();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.initGui();
	}

}
