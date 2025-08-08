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

public class SubGuiNpcAvailability
extends SubGuiInterface
implements ISliderListener, ITextfieldListener {

	public final Availability availability;
	public final GuiScreen parent;

	public SubGuiNpcAvailability(Availability availabilityIn, GuiScreen gui) {
		super();
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		parent = gui;
		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				setSubGui(new SubGuiNpcAvailabilityDialog(availability));
				break;
			}
			case 1: {
				setSubGui(new SubGuiNpcAvailabilityQuest(availability));
				break;
			}
			case 2: {
				setSubGui(new SubGuiNpcAvailabilityFaction(availability));
				break;
			}
			case 3: {
				setSubGui(new SubGuiNpcAvailabilityScoreboard(availability));
				break;
			}
			case 4: {
				availability.healthType = button.getValue();
				if (getSlider(5) != null) {
					getSlider(5).setIsVisible(availability.healthType != 0);
				}
				break;
			}
			case 6: {
				setSubGui(new SubGuiNpcAvailabilityNames(availability));
				break;
			}
			case 7: {
				setSubGui(new SubGuiNpcAvailabilityStoredData(availability));
				break;
			}
			case 8: { // ItemStacks
				SubGuiNpcAvailabilityItemStacks.parent = parent;
				SubGuiNpcAvailabilityItemStacks.setting = this;
				CommonProxy.availabilityStacks.put(player, availability);

				NBTTagCompound compound = new NBTTagCompound();
				availability.save(compound);
				Client.sendData(EnumPacketServer.AvailabilityStacks, compound);

				NoppesUtil.requestOpenGUI(EnumGuiType.AvailabilityStack);
				break;
			}
			case 9: { // ItemStacks
				setSubGui(new SubGuiNpcAvailabilityRegions(availability));
				break;
			}
			case 50: {
				if (button.getValue() == 0) {
					getTextField(52).setFullText("" + availability.daytime[0]);
					getTextField(53).setFullText("" + availability.daytime[1]);
				} else {
					switch (EnumDayTime.values()[button.getValue() - 1]) {
						case Always: {
							getTextField(52).setFullText("0");
							getTextField(53).setFullText("0");
							break;
						}
						case Night: {
							getTextField(52).setFullText("18");
							getTextField(53).setFullText("6");
							break;
						}
						case Day: {
							getTextField(52).setFullText("6");
							getTextField(53).setFullText("18");
							break;
						}
					}
				}
				availability.daytime[0] = getTextField(52).getInteger();
				availability.daytime[1] = getTextField(53).getInteger();
				break;
			}
			case 66: {
				close();
				break;
			}
			default: {

			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		GuiNpcLabel label = new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4);
		label.setCenter(xSize);
		addLabel(label);
		// colloquium 1
		int x = guiLeft + 6;
		int y = guiTop + 14;
		int h = 18;
		GuiNpcButton button = new GuiNpcButton(0, x, y, 120, h, "availability.selectdialog");
		button.setHoverText("availability.hover.selectdialog");
		addButton(button);
		button = new GuiNpcButton(1, x, y += h + 2, 120, h, "availability.selectquest");
		button.setHoverText("availability.hover.selectquest");
		addButton(button);
		button = new GuiNpcButton(2, x, y += h + 2, 120, h, "availability.selectfaction");
		button.setHoverText("availability.hover.selectfaction");
		addButton(button);
		button = new GuiNpcButton(8, x, y + h + 2, 120, h, "availability.stack");
		button.setHoverText("availability.hover.stack");
		addButton(button);
		// colloquium 2
		x += 124;
		y = guiTop + 14;
		button = new GuiNpcButton(3, x, y, 120, h, "availability.selectscoreboard");
		button.setHoverText("availability.hover.selectscoreboard");
		addButton(button);
		button = new GuiNpcButton(6, x, y += h + 2, 120, h, "availability.selectnames");
		button.setHoverText("availability.hover.selectnames");
		addButton(button);
		button = new GuiNpcButton(7, x, y += h + 2, 120, h, "availability.storeddata");
		button.setHoverText("availability.hover.storeddata");
		addButton(button);
		button = new GuiNpcButton(9, x, y + h + 2, 120, h, "availability.region");
		button.setHoverText("availability.hover.region");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, guiLeft + 82, guiTop + 192, 98, h, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
		// day type
		addLabel(new GuiNpcLabel(50, "availability.daytime", guiLeft + 4, guiTop + 131));
		button = new GuiNpcButton(50, guiLeft + 70, guiTop + 126, 70, h, new String[] { "availability.own", "availability.always", "availability.night", "availability.day" }, availability.daytime[0] == availability.daytime[1] ? 1 : availability.daytime[0] == 18 && availability.daytime[1] == 6 ? 2 : availability.daytime[0] == 6 && availability.daytime[1] == 18 ? 3 : 1);
		button.setHoverText("availability.hover.daytime.0");
		addButton(button);
		// min player level
		addLabel(new GuiNpcLabel(51, "availability.minlevel", guiLeft + 4, guiTop + 153));
		GuiNpcTextField textField = new GuiNpcTextField(51, this, fontRenderer, guiLeft + 70, guiTop + 149, 70, h - 2, availability.minPlayerLevel + "");
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, 0);
		textField.setHoverText("availability.hover.level");
		addTextField(textField);
		// start day time
		textField = new GuiNpcTextField(52, this, fontRenderer, guiLeft + 145, guiTop + 127, 40, h - 2, availability.daytime[0] + "");
		textField.setMinMaxDefault(0, 23, availability.daytime[0]);
		textField.setHoverText("availability.hover.daytime.1");
		addTextField(textField);
		// next day time
		textField = new GuiNpcTextField(53, this, fontRenderer, guiLeft + 190, guiTop + 127, 40, h - 2, availability.daytime[1] + "");
		textField.setMinMaxDefault(0, 23, availability.daytime[1]);
		textField.setHoverText("availability.hover.daytime.2");
		addTextField(textField);
		// health
		addLabel(new GuiNpcLabel(52, "availability.health", guiLeft + 4, guiTop + 175));
		button = new GuiNpcButton(4, guiLeft + 70, guiTop + 170, 70, h, new String[] { "availability.always", "availability.bigger", "availability.smaller" }, availability.healthType);
		button.setHoverText("availability.hover.health.type");
		addButton(button);
		GuiNpcSlider slider = new GuiNpcSlider(this, 5, guiLeft + 145, guiTop + 170, availability.health / 100.0f);
		slider.width = 106;
		slider.visible = availability.healthType != 0;
		slider.setHoverText("availability.hover.health");
		addSlider(slider);
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		availability.health = (int) (slider.getSliderValue() * 100.0f);
		slider.setString(availability.health + "%");
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) { }

	@Override
	public void mouseReleased(IGuiNpcSlider slider) { }

	@Override
	public void save() {
		availability.minPlayerLevel = getTextField(51).getInteger();
		availability.daytime[0] = getTextField(52).getInteger();
		availability.daytime[1] = getTextField(53).getInteger();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 51: availability.minPlayerLevel = textfield.getInteger(); break;
			case 52: availability.daytime[0] = textfield.getInteger(); break;
			case 53: availability.daytime[1] = textfield.getInteger(); break;
		}
	}

}
