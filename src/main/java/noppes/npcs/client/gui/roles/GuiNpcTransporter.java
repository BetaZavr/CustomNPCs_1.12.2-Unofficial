package noppes.npcs.client.gui.roles;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNpcTransporter extends GuiNPCInterface2 implements ICustomScrollListener, IScrollData, IGuiData {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	public TransportLocation location = new TransportLocation();

	public GuiNpcTransporter(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && button.getID() == 0) { location.type = button.getValue();}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (scroll != null) {
			if (getTextField(0) != null) { getTextField(0).setIsVisible(scroll.hasSelected()); }
			if (getButton(0) != null) { getButton(0).setIsVisible(scroll.hasSelected()); }
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
        if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(143, 196); }
		int x = guiLeft + 6, y = guiTop + 16;
		scroll.guiLeft = x;
		scroll.guiTop = y;
		addScroll(scroll);
		addLabel(new GuiNpcLabel(0, "gui.categories", x + 2, y - 11));
		x += 147;
		addLabel(new GuiNpcLabel(1, "gui.name", x, y - 11));
		addTextField(new GuiNpcTextField(0, this, x, y, 140, 20, location.name)
				.setHoverText("manager.hover.transport.loc.name"));
		addButton(new GuiNpcButton(0, x, y + 24, new String[] { "transporter.discovered", "transporter.start", "transporter.interaction" }, location.type)
				.setHoverText(new TextComponentTranslation("manager.hover.transport.type")
						.appendSibling(new TextComponentTranslation("manager.hover.transport.addinfo")).getFormattedText()));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.TransportCategoriesGet, -1);
		Client.sendData(EnumPacketServer.TransportGetLocation);
	}

	@Override
	public void save() {
		if (!scroll.hasSelected()) { return; }
		String name = getTextField(0).getText();
		if (!name.isEmpty()) { location.name = name; }
		location.pos = new BlockPos(player);
		location.dimension = player.dimension;
		try { location.npc = npc.getUniqueID(); }
		catch (Exception e) { LogWriter.error(e); }
		int cat = data.get(scroll.getSelected());
		Client.sendData(EnumPacketServer.TransportSave, cat, location.writeNBT());
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		data.putAll(dataMap);
		Collections.sort(dataList);
		scroll.setUnsortedList(dataList);
		ITextComponent l = new TextComponentTranslation("gui.localization");
		ITextComponent p = new TextComponentTranslation("gui.points");
		l.getStyle().setColor(TextFormatting.GRAY);
		p.getStyle().setColor(TextFormatting.GRAY);
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 0;
		for (String str : dataList) {
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
			hts.put(i++, Arrays.asList(hover.toString().split("<br>")));
		}
		scroll.setHoverTexts(hts);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.getKeySet().isEmpty()) { return; }
		TransportLocation loc = new TransportLocation();
		loc.readNBT(compound);
		location = loc;
		initGui();
	}

	@Override
	public void setSelected(String selected) { scroll.setSelected(selected); }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
