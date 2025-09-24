package noppes.npcs.client.gui.availability;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CommonProxy;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Availability;

import javax.annotation.Nonnull;

public class SubGuiNpcAvailability extends SubGuiInterface implements ISliderListener, ITextfieldListener {

	protected final GuiScreen parent;
	public final Availability availability;

	public SubGuiNpcAvailability(Availability availabilityIn, GuiScreen gui) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 217;

		parent = gui;
		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: setSubGui(new SubGuiNpcAvailabilityDialog(availability)); break;
			case 1: setSubGui(new SubGuiNpcAvailabilityQuest(availability)); break;
			case 2: setSubGui(new SubGuiNpcAvailabilityFaction(availability)); break;
			case 3: setSubGui(new SubGuiNpcAvailabilityScoreboard(availability)); break;
			case 4: {
				availability.healthType = button.getValue();
				if (getSlider(5) != null) { getSlider(5).setIsVisible(availability.healthType != 0); }
				break;
			}
			case 6: setSubGui(new SubGuiNpcAvailabilityNames(availability)); break;
			case 7: setSubGui(new SubGuiNpcAvailabilityStoredData(availability)); break;
			case 8: {
				SubGuiNpcAvailabilityItemStacks.parent = parent;
				SubGuiNpcAvailabilityItemStacks.setting = this;
				CommonProxy.availabilityStacks.put(player, availability);
				NBTTagCompound compound = new NBTTagCompound();
				availability.save(compound);
				Client.sendData(EnumPacketServer.AvailabilityStacks, compound);
				NoppesUtil.requestOpenGUI(EnumGuiType.AvailabilityStack);
				break;
			} // ItemStacks
			case 9: setSubGui(new SubGuiNpcAvailabilityRegions(availability)); break; // ItemStacks
			case 50: {
				if (button.getValue() == 0) {
					availability.daytime[0] = 0;
					availability.daytime[1] = 0;
					getTextField(52).setText("0");
					getTextField(53).setText("0");
				} else {
					switch (EnumDayTime.values()[button.getValue() - 1]) {
						case Always: {
							availability.daytime[0] = 0;
							availability.daytime[1] = 0;
							break;
						}
						case Night: {
							availability.daytime[0] = 18;
							availability.daytime[1] = 6;
							break;
						}
						case Day: {
							availability.daytime[0] = 6;
							availability.daytime[1] = 18;
							break;
						}
					}
				}
				getTextField(52).setText("" + availability.daytime[0]);
				getTextField(53).setText("" + availability.daytime[1]);
				break;
			} // daytime
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		addLabel(new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4)
				.setCenter(xSize));
		// colloquium 1
		int x = guiLeft + 6;
		int y = guiTop + 14;
		int h = 18;
		addButton(new GuiNpcButton(0, x, y, 120, h, "availability.selectdialog")
				.setHoverText("availability.hover.selectdialog"));
		addButton(new GuiNpcButton(1, x, y += h + 2, 120, h, "availability.selectquest")
				.setHoverText("availability.hover.selectquest"));
		addButton(new GuiNpcButton(2, x, y += h + 2, 120, h, "availability.selectfaction")
				.setHoverText("availability.hover.selectfaction"));
		addButton(new GuiNpcButton(8, x, y + h + 2, 120, h, "availability.stack")
				.setHoverText("availability.hover.stack"));
		// colloquium 2
		x += 124;
		y = guiTop + 14;
		addButton(new GuiNpcButton(3, x, y, 120, h, "availability.selectscoreboard")
				.setHoverText("availability.hover.selectscoreboard"));
		addButton(new GuiNpcButton(6, x, y += h + 2, 120, h, "availability.selectnames")
				.setHoverText("availability.hover.selectnames"));
		addButton(new GuiNpcButton(7, x, y += h + 2, 120, h, "availability.storeddata")
				.setHoverText("availability.hover.storeddata"));
		addButton(new GuiNpcButton(9, x, y + h + 2, 120, h, "availability.region")
				.setHoverText("availability.hover.region"));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192, 98, h, "gui.done")
				.setHoverText("hover.back"));
		// day type
		addLabel(new GuiNpcLabel(50, "availability.daytime", guiLeft + 4, guiTop + 131));
		addButton(new GuiNpcButton(50, guiLeft + 70, guiTop + 126, 70, h, new String[] { "availability.own", "availability.always", "availability.night", "availability.day" }, availability.daytime[0] == availability.daytime[1] ? 1 : availability.daytime[0] == 18 && availability.daytime[1] == 6 ? 2 : availability.daytime[0] == 6 && availability.daytime[1] == 18 ? 3 : 1)
				.setHoverText("availability.hover.daytime.0"));
		// min player level
		addLabel(new GuiNpcLabel(51, "availability.minlevel", guiLeft + 4, guiTop + 153));
		addTextField(new GuiNpcTextField(51, this, guiLeft + 70, guiTop + 149, 70, h - 2, availability.minPlayerLevel + "")
				.setMinMaxDefault(0, Integer.MAX_VALUE, 0)
				.setHoverText("availability.hover.level"));
		// start day time
		addTextField(new GuiNpcTextField(52, this, guiLeft + 145, guiTop + 127, 40, h - 2, availability.daytime[0] + "")
				.setMinMaxDefault(0, 23, availability.daytime[0])
				.setHoverText("availability.hover.daytime.1"));
		// next day time
		addTextField(new GuiNpcTextField(53, this, guiLeft + 190, guiTop + 127, 40, h - 2, availability.daytime[1] + "")
				.setMinMaxDefault(0, 23, availability.daytime[1])
				.setHoverText("availability.hover.daytime.2"));
		// health
		addLabel(new GuiNpcLabel(52, "availability.health", guiLeft + 4, guiTop + 175));
		addButton(new GuiNpcButton(4, guiLeft + 70, guiTop + 170, 70, h, new String[] { "availability.always", "availability.bigger", "availability.smaller" }, availability.healthType)
				.setHoverText("availability.hover.health.type"));
		addSlider(new GuiNpcSlider(this, 5, guiLeft + 145, guiTop + 170, 106, 20, availability.health / 100.0f)
				.setHoverText("availability.hover.health")
				.setIsVisible(availability.healthType != 0));
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		availability.health = (int) (slider.sliderValue * 100.0f);
		slider.setString(availability.health + "%");
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void save() {
		availability.minPlayerLevel = getTextField(51).getInteger();
		availability.daytime[0] = getTextField(52).getInteger();
		availability.daytime[1] = getTextField(53).getInteger();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 51: availability.minPlayerLevel = textfield.getInteger(); break;
			case 52: availability.daytime[0] = textfield.getInteger(); break;
			case 53: availability.daytime[1] = textfield.getInteger(); break;
		}
	}

}
