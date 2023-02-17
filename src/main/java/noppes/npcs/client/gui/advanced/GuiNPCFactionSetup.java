package noppes.npcs.client.gui.advanced;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCFactionSetup
extends GuiNPCInterface2
implements IScrollData, ICustomScrollListener {
	
	private HashMap<String, Integer> data;
	private GuiCustomScroll scrollFactions;

	public GuiNPCFactionSetup(EntityNPCInterface npc) {
		super(npc);
		this.data = new HashMap<String, Integer>();
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			this.npc.advanced.attackOtherFactions = (button.getValue() == 1);
		}
		if (button.id == 1) {
			this.npc.advanced.defendFaction = (button.getValue() == 1);
		}
		if (button.id == 12) {
			this.setSubGui(new SubGuiNpcFactionOptions(this.npc.advanced.factions));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "faction.attackHostile", this.guiLeft + 4, this.guiTop + 25));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 144, this.guiTop + 20, 40, 20,
				new String[] { "gui.no", "gui.yes" }, (this.npc.advanced.attackOtherFactions ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(1, "faction.defend", this.guiLeft + 4, this.guiTop + 47));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 144, this.guiTop + 42, 40, 20,
				new String[] { "gui.no", "gui.yes" }, (this.npc.advanced.defendFaction ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(12, "faction.ondeath", this.guiLeft + 4, this.guiTop + 69));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 90, this.guiTop + 64, 80, 20, "faction.points"));
		if (this.scrollFactions == null) {
			(this.scrollFactions = new GuiCustomScroll(this, 0)).setSize(180, 200);
		}
		this.scrollFactions.guiLeft = this.guiLeft + 200;
		this.scrollFactions.guiTop = this.guiTop + 4;
		this.addScroll(this.scrollFactions);
		Client.sendData(EnumPacketServer.FactionsGet, new Object[0]);
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && this.scrollFactions != null) {
			this.scrollFactions.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if (guiCustomScroll.id == 0) {
			Client.sendData(EnumPacketServer.FactionSet, this.data.get(this.scrollFactions.getSelected()));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = this.npc.getFaction().name;
		this.data = data;
		this.scrollFactions.setList(list);
		if (name != null) {
			this.setSelected(name);
		}
	}

	@Override
	public void setSelected(String selected) {
		this.scrollFactions.setSelected(selected);
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		// New
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.replace").getFormattedText());
		}
	}
}
