package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiMarketExtraEdit
extends SubGuiInterface
implements ITextfieldListener {

	public Marcet marcet;
	public int type;

	public SubGuiMarketExtraEdit(int id, EntityNPCInterface npc, Marcet marcet) {
		super();
		this.id = id;
		this.npc = npc;
		this.marcet = marcet;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.type = 0;
	}

	@Override
	public void initGui() {
		super.initGui();
		String[] values = new String[this.marcet.markup.size()];
		int i = 0;
		for (int id : this.marcet.markup.keySet()) {
			values[i] = (new TextComponentTranslation("bank.slot")).getFormattedText() + " " + id;
			i++;
		}
		this.addLabel(new GuiNpcLabel(0, "gui.type", this.guiLeft + 6, this.guiTop + 4, 60));
		this.addLabel(new GuiNpcLabel(1, "market.extra.markup.buy", this.guiLeft + 74, this.guiTop + 4, 80));
		this.addLabel(new GuiNpcLabel(2, "market.extra.markup.sell", this.guiLeft + 164, this.guiTop + 4, 80));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 14, 60, 20, values, this.type));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 4, this.guiTop + this.ySize - 24, 60, 20, "gui.done"));
		this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 72, this.guiTop + 14, 80, 20, ""));
		this.addTextField(new GuiNpcTextField(1, this, this.guiLeft + 162, this.guiTop + 14, 80, 20, ""));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch(button.id) {
			case 66: { // exit
				this.close();
				break;
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.extra.slot").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getId()) {
			case 0: {
				
				this.initGui();
				break;
			}
		}
	}
	
}
