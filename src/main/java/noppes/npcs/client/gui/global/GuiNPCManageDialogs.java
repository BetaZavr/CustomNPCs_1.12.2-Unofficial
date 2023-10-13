package noppes.npcs.client.gui.global;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

// Changed
public class GuiNPCManageDialogs
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {
	
	public static GuiScreen Instance;
	private final TreeMap<String, DialogCategory> categoryData;
	private final TreeMap<String, Dialog> dialogData;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollDialogs;
	private String selectedCategory = "";
	private String selectedDialog = "";
	private Dialog copyDialog = null;
	char chr = Character.toChars(0x00A7)[0];

	public GuiNPCManageDialogs(EntityNPCInterface npc) {
		super(npc);
		this.categoryData = Maps.<String, DialogCategory>newTreeMap();
		this.dialogData = Maps.<String, Dialog>newTreeMap();
		GuiNPCManageDialogs.Instance = this;
		Client.sendData(EnumPacketServer.DialogCategoryGet);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.categoryData.clear();
		this.dialogData.clear();
		String[][] ht = null;
		DialogController dData = DialogController.instance;
		// categorys
		for (DialogCategory category : dData.categories.values()) {
			this.categoryData.put(category.title, category);
			if (this.selectedCategory.isEmpty()) { this.selectedCategory = category.title; }
		}
		// dialogs
		if (!this.selectedCategory.isEmpty()) {
			if (this.categoryData.containsKey(this.selectedCategory)) {
				for (Dialog dialog : this.categoryData.get(this.selectedCategory).dialogs.values()) {
					boolean b = !dialog.text.isEmpty();
					String key = chr + "7ID:" + dialog.id + "-\"" + chr + "r" + dialog.title + chr + "7\"" + chr + (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText()) + chr + (b ? "2)" : "c)");
					this.dialogData.put(key, dialog);
					if (this.selectedDialog.isEmpty()) { this.selectedDialog = key; }
				}
				// Hover Text:
				if (!this.dialogData.isEmpty()) {
					int pos = 0;
					ht = new String[this.dialogData.size()][];
					Map<String, Integer> nextDialogIDs = Maps.<String, Integer>newTreeMap();
					for (Dialog dialog : this.dialogData.values()) {
						List<String> h = Lists.newArrayList(), activationDialogs = Lists.newArrayList(), nextDialogs = Lists.newArrayList();
						for (DialogOption option : dialog.options.values()) {
							if (option.optionType!=1 ||  option.dialogs.isEmpty()) { continue; }
							int i = 0;
							for (OptionDialogID od : option.dialogs) {
								nextDialogIDs.put(option.slot + "." + i, od.dialogId);
								i++;
							}
						}
						try {
							Set<Integer> dSet = dData.dialogs.keySet();
							for (int dialogId : dSet) {
								if (!dData.hasDialog(dialogId)) { continue; }
								Dialog d = (Dialog) dData.get(dialogId);
								for (DialogOption option : d.options.values()) {
									if (option.optionType!=1 ||  option.dialogs.isEmpty()) { continue; }
									int i = 0;
									for (OptionDialogID od : option.dialogs) {
										if (od.dialogId!=dialog.id) { continue; }
										activationDialogs.add(chr + "7ID:" + d.id + chr + "8 " + (new TextComponentTranslation("gui.answer").getFormattedText()) + chr + "8: " + chr + "7" + option.slot + "." + i + chr + "8; " + d.category.getName() + "/" + chr + "r" + d.getName());
										i++;
									}
								}
								if (nextDialogIDs.containsValue(d.id)) {
									for (String k : nextDialogIDs.keySet()) {
										if (nextDialogIDs.get(k)!=d.id) { continue; }
										nextDialogs.add(chr + "8" + (new TextComponentTranslation("gui.answer").getFormattedText()) + chr + "8: " + chr + "7" + k + chr + "7 ID:" + d.id + chr + "8; " + d.category.getName() + "/" + chr + "r" + d.getName());
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (!activationDialogs.isEmpty()) {
							h.add(new TextComponentTranslation("dialog.hover.act.1").getFormattedText());
							h.addAll(activationDialogs);
						}
						else { h.add(new TextComponentTranslation("dialog.hover.act.0").getFormattedText()); }
						if (!nextDialogs.isEmpty()) {
							h.add(new TextComponentTranslation("dialog.hover.next.1").getFormattedText());
							h.addAll(nextDialogs);
						}
						else { h.add(new TextComponentTranslation("dialog.hover.next.0").getFormattedText()); }
						ht[pos] = h.toArray(new String[h.size()]);
						pos ++;
					}
				}
			} else {
				this.selectedCategory = "";
				this.selectedDialog = "";
			}
		}
		// scroll info
		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 8, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "dialog.dialogs", this.guiLeft + 175, this.guiTop + 4));
		// dialog buttons
		int x = this.guiLeft + 350, y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(3, "dialog.dialogs", x + 2, y));
		this.addButton(new GuiNpcButton(13, x, y += 10, 64, 15, "selectServer.edit", !this.selectedDialog.isEmpty()));
		this.addButton(new GuiNpcButton(12, x, y += 17, 64, 15, "gui.remove", !this.selectedDialog.isEmpty()));
		this.addButton(new GuiNpcButton(11, x, y += 17, 64, 15, "gui.add", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(10, x, y += 21, 64, 15, "gui.copy", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(9, x, y += 17, 64, 15, "gui.paste", this.copyDialog!=null));
		// category buttons
		y = this.guiTop + 130;
		this.addLabel(new GuiNpcLabel(2, "gui.categories", x + 2, y));
		this.addButton(new GuiNpcButton(3, x, y += 10, 64, 15, "selectServer.edit", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(2, x, y += 17, 64, 15, "gui.remove",!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(1, x, y += 17, 64, 15, "gui.add"));

		if (this.scrollCategories == null) { (this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, 200); }
		this.scrollCategories.setList(Lists.newArrayList(this.categoryData.keySet()));
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		if (!this.selectedCategory.isEmpty()) { this.scrollCategories.setSelected(this.selectedCategory); }
		this.addScroll(this.scrollCategories);

		if (this.scrollDialogs == null) { (this.scrollDialogs = new GuiCustomScroll(this, 1)).setSize(170, 200); }
		this.scrollDialogs.setList(Lists.newArrayList(this.dialogData.keySet()));
		this.scrollDialogs.guiLeft = this.guiLeft + 175;
		this.scrollDialogs.guiTop = this.guiTop + 14;
		if (ht!=null) { this.scrollDialogs.hoversTexts = ht; }
		if (!this.selectedDialog.isEmpty()) { this.scrollDialogs.setSelected(this.selectedDialog); }
		this.addScroll(this.scrollDialogs);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui !=null || !CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.edit").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.del").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.add").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.dialog.paste." + (this.copyDialog!=null), (this.copyDialog!=null ? this.copyDialog.getKey() : "")).getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.dialog.copy", this.selectedDialog).getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.dialog.add", this.selectedCategory).getFormattedText());
		} else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.dialog.del", this.selectedDialog).getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.dialog.edit", this.selectedDialog).getFormattedText());
		}
	}

	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch(button.id) {
			case 1: { // new cat
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: { // del cat
				if (!this.categoryData.containsKey(this.selectedCategory)) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.categoryData.get(this.selectedCategory).title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 3: {
				if (!this.dialogData.containsKey(this.selectedDialog)) { return; }
				this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
				break;
			}
			case 9: { // paste
				if (this.copyDialog==null || !this.categoryData.containsKey(this.selectedCategory)) { return; }
				Dialog dialog = this.copyDialog.copy(null);
				dialog.id = -1;
				dialog.category = this.categoryData.get(this.selectedCategory);
				while (DialogController.instance.containsDialogName(dialog.category, dialog)) { dialog.title += "_"; }
				this.selectedDialog = dialog.title;
				Client.sendData(EnumPacketServer.DialogSave, this.categoryData.get(this.selectedCategory).id, dialog.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 10: { // copy
				if (!this.dialogData.containsKey(this.selectedDialog)) { return; }
				this.copyDialog = this.dialogData.get(this.selectedDialog);
				this.initGui();
				break;
			}
			case 11: {
				this.setSubGui(new SubGuiEditText(11, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: { // del dialog
				if (!this.dialogData.containsKey(this.selectedDialog)) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.dialogData.get(this.selectedDialog).getKey(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 13: {
				if (!this.dialogData.containsKey(this.selectedDialog)) { return; }
				this.setSubGui(new GuiDialogEdit(this.dialogData.get(this.selectedDialog)));
				break;
			}
		}
	}

	public void close() {
		super.close();
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) {
			return;
		}
		if (id == 2) {
			Client.sendData(EnumPacketServer.DialogCategoryRemove, this.categoryData.get(this.selectedCategory).id);
			this.selectedCategory = "";
			this.selectedDialog = "";
		}
		if (id == 12) {
			Client.sendData(EnumPacketServer.DialogRemove, this.dialogData.get(this.selectedDialog).id);
			this.selectedDialog = "";
		}
	}

	@Override
	public void save() { GuiNpcTextField.unfocus(); }

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if (guiCustomScroll.getSelected() == null) {
			return;
		}
		if (guiCustomScroll.id == 0) {
			if (this.selectedCategory.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedCategory = guiCustomScroll.getSelected();
			this.selectedDialog = "";
			this.scrollDialogs.selected = -1;
		}
		if (guiCustomScroll.id == 1) {
			if (this.selectedDialog.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedDialog = guiCustomScroll.getSelected();
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (!this.selectedDialog.isEmpty() && scroll.id == 1) {
			this.setSubGui(new GuiDialogEdit(this.dialogData.get(this.selectedDialog)));
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		if (subgui.id == 1) {
			DialogCategory category = new DialogCategory();
			category.title = ((SubGuiEditText) subgui).text[0];
			while (DialogController.instance.containsCategoryName(category)) {
				StringBuilder sb = new StringBuilder();
				DialogCategory dialogCategory = category;
				dialogCategory.title = sb.append(dialogCategory.title).append("_").toString();
			}
			Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
		}
		if (subgui.id == 3) {
			if (((SubGuiEditText) subgui).text[0].isEmpty()) {
				return;
			}
			DialogCategory category = this.categoryData.get(this.selectedCategory);
			category.title = ((SubGuiEditText) subgui).text[0];
			while (DialogController.instance.containsCategoryName(category)) {
				category.title += "_";
			}
			this.selectedCategory = category.title;
			Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui.id == 11) {
			if (((SubGuiEditText) subgui).text[0].isEmpty()) {
				return;
			}
			Dialog dialog = new Dialog(this.categoryData.get(this.selectedCategory));
			dialog.title = ((SubGuiEditText) subgui).text[0];
			while (DialogController.instance.containsDialogName(this.categoryData.get(this.selectedCategory), dialog)) {
				dialog.title += "_";
			}
			this.selectedDialog = dialog.title;
			Client.sendData(EnumPacketServer.DialogSave, this.categoryData.get(this.selectedCategory).id, dialog.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui instanceof GuiDialogEdit) {
			this.initGui();
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}
	
}
