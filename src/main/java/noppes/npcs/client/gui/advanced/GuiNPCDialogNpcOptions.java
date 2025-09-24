package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiDialogSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCDialogNpcOptions extends GuiNPCInterface2
		implements GuiSelectionListener, IGuiData, ICustomScrollListener {

	protected final HashMap<Integer, NBTTagCompound> data = new HashMap<>(); // slotID, dialogData
	protected GuiCustomScroll scroll;
	protected int selectedSlot = -1;
	protected int error = 0;

	public GuiNPCDialogNpcOptions(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		drawDefaultBackground = true;
		Client.sendData(EnumPacketServer.DialogNpcGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				selectedSlot = -1;
				setSubGui(new SubGuiDialogSelection(-1, 0));
				break;
			} // add
			case 2: {
				data.clear();
				Client.sendData(EnumPacketServer.DialogNpcRemove, selectedSlot);
				selectedSlot = -1;
				initGui();
				break;
			} // del
			case 3: {
				if (!data.containsKey(selectedSlot)) { return; }
				setSubGui(new SubGuiDialogSelection(data.get(selectedSlot).getInteger("Id"), 0));
				break;
			} // change
			case 4: {
				if (selectedSlot < 1) { return; }
				Client.sendData(EnumPacketServer.DialogNpcMove, selectedSlot, true);
				selectedSlot--;
				initGui();
				break;
			} // up
			case 5: {
				if (selectedSlot >= data.size()) { return; }
				Client.sendData(EnumPacketServer.DialogNpcMove, selectedSlot, false);
				selectedSlot++;
				initGui();
				break;
			} // down
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (error > 0) {
			if (scroll != null) {
				scroll.colorBackS = 0xC0A00000;
				scroll.colorBackE = 0xC0A00000;
			}
			error--;
			if (error <= 0 && scroll != null) {
				scroll.colorBackS = 0xC0101010;
				scroll.colorBackE = 0xC0101010;
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> dialogs = new ArrayList<>();
		for (int slot : data.keySet()) {
			NBTTagCompound nbt = data.get(slot);
			String str = (slot + 1) + "; " + ((char) 167) + "7" + "ID:" + nbt.getInteger("Id") + " - ";
			str += ((char) 167) + "8" + nbt.getString("Category") + "/";
			str += ((char) 167) + "r" + nbt.getString("Title");
			dialogs.add(str);
		}
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(210, 196); }
		scroll.setUnsortedList(dialogs);
		scroll.guiLeft = guiLeft + 5;
		scroll.guiTop = guiTop + 14;
		if (selectedSlot >= 0 && data.containsKey(selectedSlot)) { scroll.setSelect(selectedSlot); }
		else {
			selectedSlot = -1;
			scroll.setSelect(-1);
		}
		addScroll(scroll);
		// add
		addButton(new GuiNpcButton(1, guiLeft + 220, guiTop + 14, 64, 20, "gui.add")
				.setHoverText("dialog.hover.add"));
		// del
		addButton(new GuiNpcButton(2, guiLeft + 220, guiTop + 36, 64, 20, "gui.remove")
				.setIsEnable(selectedSlot >= 0)
				.setHoverText("dialog.hover.del"));
		// edit
		addButton(new GuiNpcButton(3, guiLeft + 220, guiTop + 58, 64, 20, "advanced.editingmode")
				.setIsEnable(selectedSlot >= 0)
				.setHoverText("dialog.hover.change"));
		// up pos
		addButton(new GuiNpcButton(4, guiLeft + 220, guiTop + 102, 64, 20, "type.up")
				.setIsEnable(selectedSlot >= 0 && selectedSlot >= 1)
				.setHoverText("dialog.hover.up"));
		// down pos
		addButton(new GuiNpcButton(5, guiLeft + 220, guiTop + 124, 64, 20, "type.down")
				.setIsEnable(selectedSlot >= 0 && selectedSlot < (data.size() - 1))
				.setHoverText("dialog.hover.down"));
		// help
		addLabel(new GuiNpcLabel(6, new TextComponentTranslation("type.help").getFormattedText(), guiLeft + 230, guiTop + 150)
				.setBackColor(0x40FF0000)
				.setBorderColor(0x80808080)
				.setColor(0xFF000000)
				.setHoverText("dialog.hover.info"));
		addLabel(new GuiNpcLabel(7, new TextComponentTranslation("dialog.dialogs").getFormattedText() + ":", guiLeft + 5, guiTop + 4));
	}

	// New from Unofficial BetaZavr
	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		selectedSlot = scroll.getSelect();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (!data.containsKey(selectedSlot)) { return; }
		setSubGui(new SubGuiDialogSelection(data.get(selectedSlot).getInteger("Id"), 0));
	}

	@Override
	public void selected(int id, String name) {
		if (selectedSlot < 0) { selectedSlot = data.size(); }
		for (int slot : data.keySet()) {
			if (selectedSlot == slot) { continue; }
			if (data.get(slot).getInteger("Id") == id) {
				error = 60;
				ITextComponent end = new TextComponentTranslation("trader.busy");
				end.getStyle().setColor(TextFormatting.RED);
				player.sendMessage(CustomNpcs.prefix.appendSibling(new TextComponentTranslation("dialog.dialog")).appendSibling(new TextComponentString(((char) 167) + "7 ID:" + id + ((char) 167) + "r \"" + name + "\"" + ((char) 167) + "c - ")).appendSibling(end));
				return;
			}
		}
		Client.sendData(EnumPacketServer.DialogNpcSet, selectedSlot, id);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Slot", 3)) { data.put(compound.getInteger("Slot"), compound); }
		initGui();
	}

}
