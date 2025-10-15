package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTransportSetup;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiNPCManageTransporters extends GuiContainerNPCInterface2
		implements IGuiData, ICustomScrollListener, ITextfieldListener {

	protected final ContainerNPCTransportSetup container;
	protected final Map<String, Integer> dataCat = new TreeMap<>();
	protected final Map<String, Integer> dataLoc = new TreeMap<>();
	protected GuiCustomScroll categories, locations;
	protected String catSel = "";
	protected String locSel = "";
	protected boolean wait = true;

	public GuiNPCManageTransporters(EntityNPCInterface npc, ContainerNPCTransportSetup containerIn) {
		super(npc, containerIn);
		setBackground("tradersetup.png");
		closeOnEsc = true;
		ySize = 200;
		parentGui = EnumGuiType.MainMenuGlobal;

		container = containerIn;
		Client.sendData(EnumPacketServer.TransportCategoriesGet);
		if (TransportController.getInstance().categories.containsKey(container.catId)) {
			TransportCategory category = TransportController.getInstance().categories.get(container.catId);
			catSel = ((char) 167) + "7ID: " + container.catId + " \"" + ((char) 167) + "r" + (new TextComponentTranslation(category.title).getFormattedText()) + ((char) 167) + "7\"";
			dataCat.put(catSel, container.catId);
		}
		if (container.location.id > -1) {
			locSel = ((char) 167) + "7ID: " + container.location.id + " \"" + ((char) 167) + "r" + (new TextComponentTranslation(container.location.name).getFormattedText()) + ((char) 167) + "7\"";
		}
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		TransportCategory cat = null;
		TransportLocation loc = container.location;
		if (!catSel.isEmpty()) { cat = TransportController.getInstance().categories.get(dataCat.get(catSel)); }
		switch (button.getID()) {
			case 0: {
				setSubGui(new SubGuiEditText(0, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			} // add cat
			case 1: { // del cat
				if (cat == null) {
					return;
				}
				Client.sendData(EnumPacketServer.TransportCategoryRemove, cat.id);
				break;
			}
			case 2: { // tp
				transfer(loc);
				break;
			}
			case 3: {
				if (loc == null) {
					return;
				}
				loc.type = button.getValue();
				break;
			}
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		if (!locSel.isEmpty()) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			for (int slotId = 0; slotId < 10; ++slotId) {
				int x = guiLeft + container.getSlot(slotId).xPos;
				int y = guiTop + container.getSlot(slotId).yPos;
				mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!wait && !catSel.isEmpty()) {
			drawHorizontalLine(guiLeft + 212, guiLeft + xSize - 3, guiTop + 178, 0x80000000);
			drawVerticalLine(guiLeft + 211, guiTop + 4, guiTop + ySize + 12, 0x80000000);
			if (!locSel.isEmpty()) {
				drawVerticalLine(guiLeft + 271, guiTop + 4, guiTop + 111, 0x80000000);
				drawHorizontalLine(guiLeft + 212, guiLeft + 270, guiTop + 110, 0x80000000);
			}
			drawVerticalLine(guiLeft + 418, guiTop + 4, guiTop + ySize + 12, 0x80000000);
		}
	}

	@Override
	public void initGui() {
		if (wait) { super.initGui(); return; }
		super.initGui();
		if (categories == null) { categories = new GuiCustomScroll(this, 0).setSize(100, 96); }
		int x = guiLeft + 5;
		int y = guiTop + 14;
		categories.guiLeft = x;
		categories.guiTop = y;
		addScroll(categories.setUnsortedList(new ArrayList<>(dataCat.keySet())));
		if (!catSel.isEmpty()) { categories.setSelected(catSel); }
		addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 5, y - 10));
		y += categories.height + 2;
		GuiNpcButton button = new GuiNpcButton(0, x, y, 49, 20, "gui.add");
		button.setHoverText("manager.hover.transport.add");
		addButton(button);
		button = new GuiNpcButton(1, x + 52, y, 49, 20, "gui.remove");
		button.setIsEnable(!catSel.isEmpty());
		button.setHoverText("manager.hover.transport.del", "\"" + catSel + "\"");
		addButton(button);

		if (locations == null) { locations = new GuiCustomScroll(this, 1).setSize(100, 96); }
		x += 102;
		y = guiTop + 14;
		locations.guiLeft = x;
		locations.guiTop = y;
		addScroll(locations.setUnsortedList(new ArrayList<>(dataLoc.keySet())));
		if (!locSel.isEmpty()) { locations.setSelected(locSel); }
		addLabel(new GuiNpcLabel(1, "gui.location", guiLeft + 113, y - 10));
		y += locations.height + 2;
		button = new GuiNpcButton(2, x, y, 100, 20, "transporter.travel");
		button.setIsEnable(!locSel.isEmpty());
		button.setHoverText("hover.teleport");
		addButton(button);

		if (catSel.isEmpty()) { return; }
		TransportCategory cat = TransportController.getInstance().categories.get(dataCat.get(catSel));
		if (cat == null) { return; }
		y = guiTop + 191;
		addLabel(new GuiNpcLabel(2, "parameter.ikeysetting.catname", guiLeft + 216, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, guiLeft + 214, y, 132, 20, cat.title);
		textField.setHoverText("manager.hover.transport.cat.name");
		addTextField(textField);

		if (locSel.isEmpty()) {
			return;
		}
		x = guiLeft + 214;
		y = guiTop + 8;
		addLabel(new GuiNpcLabel(3, "market.barter", x + 2, y));
		y += 80;
		addLabel(new GuiNpcLabel(4, "market.currency", x + 2, y - 10));
		textField = new GuiNpcTextField(1, this, x, y, 50, 20, "" + container.location.money);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, (int) container.location.money);
		textField.setHoverText("manager.hover.transport.money");
		addTextField(textField);

		y += 34;
		addLabel(new GuiNpcLabel(5, "parameter.ikeysetting.name", x + 2, y - 10));
		textField = new GuiNpcTextField(2, this, x, y, 202, 20, container.location.name);
		textField.setHoverText("manager.hover.transport.loc.name");
		addTextField(textField);
		y += 34;
		addLabel(new GuiNpcLabel(6, "UUID NPC", x + 2, y - 11));
		textField = new GuiNpcTextField(3, this, x, y, 202, 20, container.location.npc == null ? "" : container.location.npc.toString());
		textField.setHoverText("parameter.entity.uuid");
		addTextField(textField);

		x += 60;
		y = guiTop + 20;
		addLabel(new GuiNpcLabel(7, "parameter.world", x + 2, y - 11));
		addLabel(new GuiNpcLabel(8, "ID:", x + 2, y + 6));
		textField = new GuiNpcTextField(4, this, x + 15, y, 42, 20, "" + container.location.dimension);
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, container.location.dimension);
		textField.setHoverText("parameter.dimension.id");
		addTextField(textField);

		y += 34;
		addLabel(new GuiNpcLabel(9, "parameter.position", x + 2, y - 11));
		String idt = "ID:" + container.location.id;
		addLabel(new GuiNpcLabel(10, idt, guiLeft + xSize - fontRenderer.getStringWidth(idt) - 4,
				guiTop + 8));
		for (int i = 0; i < 3; i++) {
			int v = i == 0 ? container.location.pos.getX()
					: i == 1 ? container.location.pos.getY() : container.location.pos.getZ();
			textField = new GuiNpcTextField(5 + i, this, x + 1 + i * 48, y, 44, 20, "" + v);
			textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, v);
			textField.setHoverText("parameter.pos" + (i == 0 ? "x" : i == 1 ? "y" : "z"));
			addTextField(textField);
		}
		y += 34;
		addLabel(new GuiNpcLabel(11, "gui.type", x + 2, y - 11));
		button = new GuiNpcButton(3, x, y, 137, 20, new String[] { "transporter.discovered", "transporter.start", "transporter.interaction" }, container.location.type);
		button.setHoverText("manager.hover.transport.type");
		addButton(button);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (catSel.isEmpty() || getTextField(0) == null) { return; }
		TransportCategory cat = TransportController.getInstance().categories.get(dataCat.get(catSel));
		if (cat == null) { return; }
		cat.title = getTextField(0).getText();
		if (locSel.isEmpty() || getTextField(1) == null) {
			NBTTagCompound compound = new NBTTagCompound();
			cat.writeNBT(compound);
			Client.sendData(EnumPacketServer.TransportCategorySave, compound);
			return;
		}
		container.location.money = getTextField(1).getInteger();
		container.location.name = getTextField(2).getText();
		container.location.dimension = getTextField(4).getInteger();
		container.location.pos = new BlockPos(getTextField(5).getInteger(), getTextField(6).getInteger(), getTextField(7).getInteger());
		try { container.location.npc = UUID.fromString(getTextField(3).getText()); } catch (Exception e) { LogWriter.error(e); }
		container.location.type = getButton(3).getValue();
		Client.sendData(EnumPacketServer.TransportCategorySave, container.saveTransport(cat));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: {
				if (catSel.equals(scroll.getSelected()) || !dataCat.containsKey(scroll.getSelected())) { return; }
				save();
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, container.location.id, dataCat.get(scroll.getSelected()), 0);
				wait = true;
				initGui();
				break;
			}
			case 1: {
				if (locSel.equals(scroll.getSelected()) || !dataLoc.containsKey(scroll.getSelected()) || !dataCat.containsKey(catSel)) { return; }
				save();
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, dataLoc.get(scroll.getSelected()), dataCat.get(catSel), 0);
				wait = true;
				initGui();
				break;
			}
		}
		setGuiData(null);
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (scroll.getID() == 1 && scroll.hasSelected()) { transfer(container.location); }
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		int selCatId = -1;
		if (!catSel.isEmpty()) { selCatId = dataCat.get(catSel); }
		dataCat.clear();
		dataLoc.clear();
		for (int catId : TransportController.getInstance().categories.keySet()) {
			TransportCategory cat = TransportController.getInstance().categories.get(catId);
			String catKey = ((char) 167) + "7ID: " + catId + " \"" + ((char) 167) + "r"
					+ (new TextComponentTranslation(cat.title).getFormattedText()) + ((char) 167) + "7\"";
			dataCat.put(catKey, catId);
			if (catId == selCatId) {
				for (int locId : cat.locations.keySet()) {
					String locKey = ((char) 167) + "7ID: " + locId + " \"" + ((char) 167) + "r"
							+ (new TextComponentTranslation(cat.locations.get(locId).name).getFormattedText())
							+ ((char) 167) + "7\"";
					dataLoc.put(locKey, locId);
				}
			}
		}
		if (!catSel.isEmpty() && !dataCat.containsKey(catSel)) { catSel = ""; }
		if (!locSel.isEmpty() && !dataLoc.containsKey(locSel)) { locSel = ""; }
		wait = false;
		initGui();
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).text[0].isEmpty()) { return; }
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("CategoryId", -1);
		compound.setString("CategoryTitle", ((SubGuiEditText) subgui).text[0]);
		Client.sendData(EnumPacketServer.TransportCategorySave, compound);
	}
	private void transfer(TransportLocation loc) {
		if (loc == null) { return; }
		try { Client.sendData(EnumPacketServer.TeleportTo, loc.dimension, loc.pos.getX(), loc.pos.getY(), loc.pos.getZ()); } catch (Exception e) { LogWriter.error(e); }
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		TransportCategory cat = null;
		TransportLocation loc = container.location;
		if (!catSel.isEmpty()) { cat = TransportController.getInstance().categories.get(dataCat.get(catSel)); }
		switch (textField.getID()) {
			case 0: {
				if (textField.getText().isEmpty() || cat == null) { return; }
				cat.title = textField.getText();
				break;
			} // cat name
			case 1: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				loc.money = textField.getInteger();
				break;
			} // money
			case 2: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				loc.name = textField.getText();
				break;
			} // loc name
			case 3: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				try { loc.npc = UUID.fromString(textField.getText()); } catch (Exception e) { textField.setText(loc.npc == null ? "" : loc.npc.toString()); }
				break;
			} // npc uuid
			case 4: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				int dimId = textField.getInteger();
				if (!TransportController.getInstance().worldIDs.contains(dimId)) {
					textField.setText("" + loc.dimension);
					return;
				}
				loc.dimension = dimId;
				break;
			} // dim ID
			case 5: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				int y = loc.pos.getY();
				int z = loc.pos.getZ();
				loc.pos = new BlockPos(textField.getInteger(), y, z);
				break;
			} // X
			case 6: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				int x = loc.pos.getX();
				int z = loc.pos.getZ();
				loc.pos = new BlockPos(x, textField.getInteger(), z);
				break;
			} // Y
			case 7: {
				if (textField.getText().isEmpty() || loc == null) { return; }
				int x = loc.pos.getX();
				int y = loc.pos.getY();
				loc.pos = new BlockPos(x, y, textField.getInteger());
				break;
			} // Z
		}
	}

}
