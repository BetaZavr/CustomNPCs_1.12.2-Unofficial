package noppes.npcs.client.gui.select;

import java.util.ArrayList;
import java.util.HashMap;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;

import javax.annotation.Nonnull;

public class SubGuiDialogSelection extends SubGuiInterface implements ICustomScrollListener {

	protected final HashMap<String, DialogCategory> categoryData = new HashMap<>();
	protected final HashMap<String, Dialog> dialogData = new HashMap<>();
	protected GuiSelectionListener listener;
	protected GuiCustomScroll scrollCategories;
	protected GuiCustomScroll scrollDialogs;
	public DialogCategory selectedCategory;
	public Dialog selectedDialog;

	public SubGuiDialogSelection(int dialogID, int id) {
		super(id);
		setBackground("menubg.png");
		drawDefaultBackground = false;
		title = "";
		xSize = 366;
		ySize = 226;

		selectedDialog = DialogController.instance.dialogs.get(dialogID);
		if (selectedDialog != null) { selectedCategory = selectedDialog.category; }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() == 2) {
			if (selectedDialog != null) { scrollDoubleClicked(null, null); }
			else { onClosed(); }
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (parent instanceof GuiSelectionListener) { listener = (GuiSelectionListener) parent; }
		addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "dialog.dialogs", guiLeft + 175, guiTop + 4));
		addButton(new GuiNpcButton(2, guiLeft + xSize - 26, guiTop + 4, 20, 20, "X"));
		categoryData.clear();
		for (DialogCategory category : DialogController.instance.categories.values()) { categoryData.put(category.title, category); }
		dialogData.clear();
		if (selectedCategory != null) {
			for (Dialog dialog : selectedCategory.dialogs.values()) { dialogData.put(dialog.title, dialog); }
		}
		if (scrollCategories == null) { scrollCategories = new GuiCustomScroll(this, 0).setSize(170, 200); }
		scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		if (selectedCategory != null) { scrollCategories.setSelected(selectedCategory.title); }
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 14;
		addScroll(scrollCategories);
		if (scrollDialogs == null) { scrollDialogs = new GuiCustomScroll(this, 1).setSize(170, 200); }
		scrollDialogs.setList(new ArrayList<>(dialogData.keySet()));
		if (selectedDialog != null) { scrollDialogs.setSelected(selectedDialog.title); }
		scrollDialogs.guiLeft = guiLeft + 175;
		scrollDialogs.guiTop = guiTop + 14;
		addScroll(scrollDialogs);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			selectedCategory = categoryData.get(scrollCategories.getSelected());
			selectedDialog = null;
			scrollDialogs.setSelect(-1);
		}
		if (scroll.getID() == 1) { selectedDialog = dialogData.get(scrollDialogs.getSelected()); }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (selectedDialog == null) { return; }
		if (listener != null) { listener.selected(selectedDialog.id, selectedDialog.title); }
		onClosed();
	}

}
