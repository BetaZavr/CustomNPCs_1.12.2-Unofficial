package noppes.npcs.client.gui.roles;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcTransporter
extends GuiNPCInterface2
implements IScrollData, IGuiData {
	
	private HashMap<String, Integer> data;
	public TransportLocation location;
	private GuiCustomScroll scroll;

	public GuiNpcTransporter(EntityNPCInterface npc) {
		super(npc);
		this.location = new TransportLocation();
		this.data = new HashMap<String, Integer>();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.location.type = button.getValue();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		Vector<String> list = new Vector<String>();
		list.addAll(this.data.keySet());
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(143, 196);
		}
		int x = this.guiLeft + 6, y = this.guiTop + 16;
		this.scroll.guiLeft = x;
		this.scroll.guiTop = y;
		this.addScroll(this.scroll);
		this.addLabel(new GuiNpcLabel(0, "gui.categories", x + 2, y - 11));
		x += 147;
		this.addLabel(new GuiNpcLabel(1, "gui.name", x, y - 11));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x, y, 140, 20, this.location.name));
		this.addButton(new GuiNpcButton(0, x, (y += 24), new String[] { "transporter.discovered", "transporter.start", "transporter.interaction" }, this.location.type));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.TransportCategoriesGet, -1);
		Client.sendData(EnumPacketServer.TransportGetLocation, new Object[0]);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.scroll!=null) {
			if (this.getTextField(0)!=null) { this.getTextField(0).setVisible(this.scroll.hasSelected()); }
			if (this.getButton(0)!=null) { this.getButton(0).setVisible(this.scroll.hasSelected()); }
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.type").appendSibling(new TextComponentTranslation("manager.hover.transport.addinfo")).getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.loc.name").getFormattedText());
		}
	}

	@Override
	public void save() {
		if (!this.scroll.hasSelected()) { return; }
		String name = this.getTextField(0).getText();
		if (!name.isEmpty()) { this.location.name = name; }
		this.location.pos = new BlockPos(this.player);
		this.location.dimension = this.player.dimension;
		try { this.location.npc = this.npc.getUniqueID(); } catch (Exception e) { }
		int cat = this.data.get(this.scroll.getSelected());
		Client.sendData(EnumPacketServer.TransportSave, cat, this.location.writeNBT());
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = data;
		Collections.sort(list);
		this.scroll.setListNotSorted(list);
		this.scroll.hoversTexts = new String[list.size()][];
		int i = 0;
		ITextComponent l = new TextComponentTranslation("gui.localization");
		ITextComponent p = new TextComponentTranslation("gui.points");
		l.getStyle().setColor(TextFormatting.GRAY);
		p.getStyle().setColor(TextFormatting.GRAY);
		for (String str : list) {
			String hover = "";
			TransportCategory cat = TransportController.getInstance().categories.get(data.get(str));
			if (cat!=null && !cat.locations.isEmpty()) {
				for (int id : cat.locations.keySet()) {
					if (!hover.isEmpty()) { hover += ";<br>"; }
					hover += ((char) 167) + "7ID: " + ((char) 167) + "r" + id + ((char) 167) + "7 \"" + ((char) 167) + "r" + new TextComponentTranslation(cat.locations.get(id).name).getFormattedText() + ((char) 167) + "7\"";
				}
				hover = p.getFormattedText() + "<br>" + hover;
			}
			this.scroll.hoversTexts[i] = hover.split("<br>");
			i++;
		}
		
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.getKeySet().isEmpty()) { return; }
		TransportLocation loc = new TransportLocation();
		loc.readNBT(compound);
		this.location = loc;
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
		this.scroll.setSelected(selected);
	}
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}
}
