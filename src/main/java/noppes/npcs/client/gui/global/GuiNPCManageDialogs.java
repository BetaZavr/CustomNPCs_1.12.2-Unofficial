package noppes.npcs.client.gui.global;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class GuiNPCManageDialogs
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {

	public static GuiScreen Instance;
	private static boolean isName = true;
	private final Map<String, DialogCategory> categoryData = new TreeMap<>();
	private final Map<String, Dialog> dialogData = new LinkedHashMap<>();
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollDialogs;
	private String selectedCategory = "";
	private String selectedDialog = "";
	private Dialog copyDialog = null;
	String chr = "" + ((char) 167);

	public GuiNPCManageDialogs(EntityNPCInterface npc) {
		super(npc);
		GuiNPCManageDialogs.Instance = this;
		Client.sendData(EnumPacketServer.DialogCategoryGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: { // create cat
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: { // del cat
				if (!categoryData.containsKey(selectedCategory)) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						categoryData.get(selectedCategory).title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				displayGuiScreen(guiyesno);
				break;
			}
			case 3: {
				if (!dialogData.containsKey(selectedDialog)) {
					return;
				}
				setSubGui(new SubGuiEditText(3, categoryData.get(selectedCategory).title));
				break;
			}
			case 9: { // paste
				if (copyDialog == null || !categoryData.containsKey(selectedCategory)) {
					return;
				}
				Dialog dialog = copyDialog.copy(null);
				dialog.id = -1;
				dialog.category = categoryData.get(selectedCategory);

				StringBuilder t = new StringBuilder(dialog.title);
				boolean has = true;
				while (has) {
					has = false;
					for (Dialog dia : dialog.category.dialogs.values()) {
						if (dia.id != dialog.id && dia.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				dialog.title = t.toString();

				selectedDialog = dialog.title;
				Client.sendData(EnumPacketServer.DialogSave, categoryData.get(selectedCategory).id, dialog.writeToNBT(new NBTTagCompound()));
				initGui();
				break;
			}
			case 10: { // copy
				if (!dialogData.containsKey(selectedDialog)) {
					return;
				}
				copyDialog = dialogData.get(selectedDialog);
				initGui();
				break;
			}
			case 11: {
				setSubGui(new SubGuiEditText(11, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: { // del dialog
				if (!dialogData.containsKey(selectedDialog)) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, dialogData.get(selectedDialog).getKey(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				displayGuiScreen(guiyesno);
				break;
			}
			case 13: {
				if (!dialogData.containsKey(selectedDialog)) {
					return;
				}
				setSubGui(new GuiDialogEdit(dialogData.get(selectedDialog), this));
				break;
			}
			case 14: {
				GuiNPCManageDialogs.isName = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("hover.sort", new TextComponentTranslation("dialog.dialogs").getFormattedText(), ((GuiNpcCheckBox) button).getText());
				break;
			}
		}
	}

	public void close() {
		super.close();
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) {
			return;
		}
		if (id == 2) {
			Client.sendData(EnumPacketServer.DialogCategoryRemove, categoryData.get(selectedCategory).id);
			selectedCategory = "";
			selectedDialog = "";
		}
		if (id == 12) {
			Client.sendData(EnumPacketServer.DialogRemove, dialogData.get(selectedDialog).id);
			selectedDialog = "";
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (hasSubGui()) { return; }
		drawHorizontalLine(guiLeft + 348, guiLeft + 414, guiTop + 128, 0x80000000);
	}

	@Override
	public void initGui() {
		super.initGui();
		categoryData.clear();
		dialogData.clear();
		DialogController dData = DialogController.instance;
		// category's
		for (DialogCategory category : dData.categories.values()) {
			categoryData.put(category.title, category);
			if (selectedCategory.isEmpty()) {
				selectedCategory = category.title;
			}
		}
		// dialogs
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		if (!selectedCategory.isEmpty()) {
			if (categoryData.containsKey(selectedCategory)) {
				Map<String, Dialog> map = new TreeMap<>();
				for (Dialog dialog : categoryData.get(selectedCategory).dialogs.values()) {
					boolean b = !dialog.text.isEmpty();
					String key = chr + "7ID:" + dialog.id + "-\"" + chr + "r" + new TextComponentTranslation(dialog.title).getFormattedText() + chr + "7\"" + chr + (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText()) + chr + (b ? "2)" : "c)");
					map.put(key, dialog);
				}
				List<Entry<String, Dialog>> list = getEntryList(map);
				for (Entry<String, Dialog> entry : list) {
					dialogData.put(entry.getKey(), entry.getValue());
					if (selectedDialog.isEmpty()) {
						selectedDialog = entry.getKey();
					}
				}
				// Hover Text:
				if (!dialogData.isEmpty()) {
					int pos = 0;
					Map<String, Integer> nextDialogIDs = new TreeMap<>();
					for (Dialog dialog : dialogData.values()) {
						List<String> h = new ArrayList<>();
						List<String> activationDialogs = new ArrayList<>();
						List<String> nextDialogs = new ArrayList<>();
						h.add(new TextComponentTranslation(dialog.title).getFormattedText() + ":");
						for (DialogOption option : dialog.options.values()) {
							if (option.optionType != OptionType.DIALOG_OPTION || option.dialogs.isEmpty()) {
								continue;
							}
							int i = 0;
							for (OptionDialogID od : option.dialogs) {
								nextDialogIDs.put(option.slot + "." + i, od.dialogId);
								i++;
							}
						}
						try {
							Set<Integer> dSet = dData.dialogs.keySet();
							for (int dialogId : dSet) {
								if (!dData.hasDialog(dialogId)) {
									continue;
								}
								Dialog d = (Dialog) dData.get(dialogId);
								for (DialogOption option : d.options.values()) {
									if (option.optionType != OptionType.DIALOG_OPTION || option.dialogs.isEmpty()) {
										continue;
									}
									int i = 0;
									for (OptionDialogID od : option.dialogs) {
										if (od.dialogId != dialog.id) {
											continue;
										}
										activationDialogs.add(chr + "7ID:" + d.id + chr + "8 " + (new TextComponentTranslation("gui.answer").getFormattedText()) + chr + "8: " + chr + "7" + option.slot + "." + i + chr + "8; " + d.category.getName() + "/" + chr + "r" + d.getName());
										i++;
									}
								}
								if (nextDialogIDs.containsValue(d.id)) {
									for (String k : nextDialogIDs.keySet()) {
										if (nextDialogIDs.get(k) != d.id) {
											continue;
										}
										nextDialogs.add(chr + "8" + (new TextComponentTranslation("gui.answer").getFormattedText()) + chr + "8: " + chr + "7" + k + chr + "7 ID:" + d.id + chr + "8; " + d.category.getName() + "/" + chr + "r" + d.getName());
									}
								}
							}
						} catch (Exception e) { LogWriter.error("Error:", e); }
						if (!activationDialogs.isEmpty()) {
							h.add(new TextComponentTranslation("dialog.hover.act.1").getFormattedText());
							h.addAll(activationDialogs);
						} else {
							h.add(new TextComponentTranslation("dialog.hover.act.0").getFormattedText());
						}
						if (!nextDialogs.isEmpty()) {
							h.add(new TextComponentTranslation("dialog.hover.next.1").getFormattedText());
							h.addAll(nextDialogs);
						} else {
							h.add(new TextComponentTranslation("dialog.hover.next.0").getFormattedText());
						}
						hts.put(pos, h);
						pos++;
					}
				}
			} else {
				selectedCategory = "";
				selectedDialog = "";
			}
		}
		// scroll info
		addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "dialog.dialogs", guiLeft + 180, guiTop + 4));
		// dialog buttons
		int x = guiLeft + 350, y = guiTop + 8;
		addLabel(new GuiNpcLabel(3, "dialog.dialogs", x + 2, y));
		GuiNpcButton button = new GuiNpcButton(13, x, y += 10, 64, 15, "selectServer.edit", !selectedDialog.isEmpty());
		button.setHoverText("manager.hover.dialog.edit", selectedDialog);
		addButton(button);
		button = new GuiNpcButton(12, x, y += 17, 64, 15, "gui.remove", !selectedDialog.isEmpty());
		button.setHoverText("manager.hover.dialog.del", selectedDialog);
		addButton(button);
		button = new GuiNpcButton(11, x, y += 17, 64, 15, "gui.add", !selectedCategory.isEmpty());
		button.setHoverText("manager.hover.dialog.add", selectedCategory);
		addButton(button);
		button = new GuiNpcButton(10, x, y += 21, 64, 15, "gui.copy", !selectedCategory.isEmpty());
		button.setHoverText("manager.hover.dialog.copy", selectedDialog);
		addButton(button);
		button = new GuiNpcButton(9, x, y += 17, 64, 15, "gui.paste", copyDialog != null);
		button.setHoverText("manager.hover.dialog.paste." + (copyDialog != null), (copyDialog != null ? copyDialog.getKey() : ""));
		addButton(button);
		button = new GuiNpcCheckBox(14, x, y + 17, 64, 14, "gui.name", "ID", GuiNPCManageDialogs.isName);
		button.setHoverText("hover.sort", new TextComponentTranslation("dialog.dialogs").getFormattedText(), ((GuiNpcCheckBox) button).getText());
		addButton(button);
		// category buttons
		y = guiTop + 130;
		addLabel(new GuiNpcLabel(2, "gui.categories", x + 2, y));
		// edit
		button = new GuiNpcButton(3, x, y += 10, 64, 15, "selectServer.edit", !selectedCategory.isEmpty());
		button.setHoverText("manager.hover.category.edit");
		addButton(button);
		// del
		button = new GuiNpcButton(2, x, y += 17, 64, 15, "gui.remove", !selectedCategory.isEmpty());
		button.setHoverText("manager.hover.category.del");
		addButton(button);
		// add
		button = new GuiNpcButton(1, x, y + 17, 64, 15, "gui.add");
		button.setHoverText("manager.hover.category.add");
		addButton(button);

		if (scrollCategories == null) {
			(scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, ySize - 3);
		}
		scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 15;
		if (!selectedCategory.isEmpty()) {
			scrollCategories.setSelected(selectedCategory);
		}
		addScroll(scrollCategories);

		if (scrollDialogs == null) {
			(scrollDialogs = new GuiCustomScroll(this, 1)).setSize(170, ySize - 3);
		}
		scrollDialogs.setListNotSorted(new ArrayList<>(dialogData.keySet()));
		scrollDialogs.guiLeft = guiLeft + 176;
		scrollDialogs.guiTop = guiTop + 15;
		if (!selectedDialog.isEmpty()) {
			scrollDialogs.setSelected(selectedDialog);
		}
		scrollDialogs.setHoverTexts(hts);
		addScroll(scrollDialogs);
	}

	private static List<Entry<String, Dialog>> getEntryList(Map<String, Dialog> map) {
		List<Entry<String, Dialog>> list = new ArrayList<>(map.entrySet());
		list.sort((d_0, d_1) -> {
			if (GuiNPCManageDialogs.isName) {
				String n_0 = Util.instance.deleteColor(new TextComponentTranslation(d_0.getValue().title).getFormattedText() + "_" + d_0.getValue().id).toLowerCase();
				String n_1 = Util.instance.deleteColor(new TextComponentTranslation(d_1.getValue().title).getFormattedText() + "_" + d_1.getValue().id).toLowerCase();
				return n_0.compareTo(n_1);
			} else {
				return Integer.compare(d_0.getValue().id, d_1.getValue().id);
			}
		});
		return list;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		if (scroll.getID() == 0) {
			if (selectedCategory.equals(scroll.getSelected())) {
				return;
			}
			selectedCategory = scroll.getSelected();
			selectedDialog = "";
			scroll.setSelect(-1);
		}
		if (scroll.getID() == 1) {
			if (selectedDialog.equals(scroll.getSelected())) {
				return;
			}
			selectedDialog = scroll.getSelected();
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (!selectedDialog.isEmpty() && scroll.getID() == 1) {
			setSubGui(new GuiDialogEdit(dialogData.get(selectedDialog), this));
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && !((SubGuiEditText) subgui).cancelled) {
			if (subgui.getId() == 1) {
				DialogCategory category = new DialogCategory();
				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (DialogCategory cat : DialogController.instance.categories.values()) {
						if (category.id != cat.id && cat.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				category.title = t.toString();
				Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
			}
			if (subgui.getId() == 3) {
				if (((SubGuiEditText) subgui).text[0].isEmpty() || !categoryData.containsKey(selectedCategory)) {
					return;
				}
				DialogCategory category = categoryData.get(selectedCategory).copy();
				if (category.title.equals(((SubGuiEditText) subgui).text[0])) {
					return;
				}
				category.title = ((SubGuiEditText) subgui).text[0];

				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (DialogCategory cat : DialogController.instance.categories.values()) {
						if (category.id != cat.id && cat.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				category.title = t.toString();
				selectedCategory = category.title;
				Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
				initGui();
			}
			if (subgui.getId() == 11) {
				if (((SubGuiEditText) subgui).text[0].isEmpty()) {
					return;
				}
				Dialog dialog = new Dialog(categoryData.get(selectedCategory));

				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (Dialog dia : dialog.category.dialogs.values()) {
						if (dia.id != dialog.id && dia.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				dialog.title = t.toString();
				selectedDialog = dialog.title;
				Client.sendData(EnumPacketServer.DialogSave, categoryData.get(selectedCategory).id, dialog.writeToNBT(new NBTTagCompound()));
				initGui();
			}
		}
		if (subgui instanceof GuiDialogEdit) {
			initGui();
		}
	}

}
