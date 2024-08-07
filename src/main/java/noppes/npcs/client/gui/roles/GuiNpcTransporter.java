package noppes.npcs.client.gui.roles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
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

public class GuiNpcTransporter extends GuiNPCInterface2 implements IScrollData, IGuiData {

	private final HashMap<String, Integer> data;
	public TransportLocation location;
	private GuiCustomScroll scroll;

	public GuiNpcTransporter(EntityNPCInterface npc) {
		super(npc);
		this.location = new TransportLocation();
		this.data = new HashMap<>();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.location.type = button.getValue();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.scroll != null) {
			if (this.getTextField(0) != null) {
				this.getTextField(0).setVisible(this.scroll.hasSelected());
			}
			if (this.getButton(0) != null) {
				this.getButton(0).setVisible(this.scroll.hasSelected());
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.type")
					.appendSibling(new TextComponentTranslation("manager.hover.transport.addinfo")).getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.loc.name").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
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
		this.addButton(new GuiNpcButton(0, x, y + 24,
				new String[] { "transporter.discovered", "transporter.start", "transporter.interaction" },
				this.location.type));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.TransportCategoriesGet, -1);
		Client.sendData(EnumPacketServer.TransportGetLocation);
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}

	@Override
	public void save() {
		if (!this.scroll.hasSelected()) {
			return;
		}
		String name = this.getTextField(0).getText();
		if (!name.isEmpty()) {
			this.location.name = name;
		}
		this.location.pos = new BlockPos(this.player);
		this.location.dimension = this.player.dimension;
		try {
			this.location.npc = this.npc.getUniqueID();
		} catch (Exception e) { LogWriter.error("Error:", e); }
		int cat = this.data.get(this.scroll.getSelected());
		Client.sendData(EnumPacketServer.TransportSave, cat, this.location.writeNBT());
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		this.data.putAll(data);
		Collections.sort(list);
		this.scroll.setListNotSorted(list);
		this.scroll.hoversTexts = new String[list.size()][];
		int i = 0;
		ITextComponent l = new TextComponentTranslation("gui.localization");
		ITextComponent p = new TextComponentTranslation("gui.points");
		l.getStyle().setColor(TextFormatting.GRAY);
		p.getStyle().setColor(TextFormatting.GRAY);
		for (String str : list) {
			StringBuilder hover = new StringBuilder();
			TransportCategory cat = TransportController.getInstance().categories.get(data.get(str));
			if (cat != null && !cat.locations.isEmpty()) {
				for (int id : cat.locations.keySet()) {
					if (hover.length() > 0) {
						hover.append(";<br>");
					}
					hover.append(((char) 167) + "7ID: " + ((char) 167) + "r").append(id).append((char) 167).append("7 \"").append((char) 167).append("r").append(new TextComponentTranslation(cat.locations.get(id).name).getFormattedText()).append((char) 167).append("7\"");
				}
				hover.insert(0, p.getFormattedText() + "<br>");
			}
			this.scroll.hoversTexts[i] = hover.toString().split("<br>");
			i++;
		}

	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.getKeySet().isEmpty()) {
			return;
		}
		TransportLocation loc = new TransportLocation();
		loc.readNBT(compound);
		this.location = loc;
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
		this.scroll.setSelected(selected);
	}
}
