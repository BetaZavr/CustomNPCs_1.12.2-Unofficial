package noppes.npcs.client.gui.global;

import java.util.HashMap;

import com.google.common.collect.Lists;

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
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

// Changed
public class GuiNPCManageDialogs
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {
	
	public static GuiScreen Instance;
	private HashMap<String, DialogCategory> categoryData;
	char chr = Character.toChars(0x00A7)[0];
	private HashMap<String, Dialog> dialogData;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollDialogs;
	private String selectedCategory = "";
	private String selectedDialog = "";

	public GuiNPCManageDialogs(EntityNPCInterface npc) {
		super(npc);
		this.categoryData = new HashMap<String, DialogCategory>();
		this.dialogData = new HashMap<String, Dialog>();
		GuiNPCManageDialogs.Instance = this;
		Client.sendData(EnumPacketServer.DialogCategoryGet);
	}

	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch(button.id) {
			case 1: {
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: {
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.categoryData.get(this.selectedCategory).title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 3: {
				this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
				break;
			}
			case 11: {
				this.setSubGui(new SubGuiEditText(11, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: {
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.dialogData.get(this.selectedDialog).title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 13: {
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
	public void initGui() {
		super.initGui();
		HashMap<String, DialogCategory> categoryData = new HashMap<String, DialogCategory>();
		HashMap<String, Dialog> dialogData = new HashMap<String, Dialog>();
		for (DialogCategory category : DialogController.instance.categories.values()) {
			categoryData.put(category.title, category);
		}
		this.categoryData = categoryData;
		if (this.selectedCategory.isEmpty() && categoryData.size() > 0) {
			for (String key : categoryData.keySet()) {
				this.selectedCategory = key;
				break;
			}
		}
		if (!this.selectedCategory.isEmpty()) {
			if (this.categoryData.containsKey(this.selectedCategory)) {
				for (Dialog dialog : this.categoryData.get(this.selectedCategory).dialogs.values()) {
					boolean b = !dialog.text.isEmpty();
					dialogData.put( chr + "7ID:" + dialog.id + "-\"" + chr + "r" + dialog.title + chr
							+ "7\"" + chr + (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText())
							+ chr + (b ? "2)" : "c)"), dialog);
				}
			} else {
				this.selectedCategory = "";
				this.selectedDialog = "";
			}
		}
		this.dialogData = dialogData;
		if (this.selectedDialog.isEmpty() && dialogData.size() > 0) {
			for (String key : dialogData.keySet()) {
				this.selectedDialog = key;
				break;
			}
		}

		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 8, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "dialog.dialogs", this.guiLeft + 175, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(3, "dialog.dialogs", this.guiLeft + 356, this.guiTop + 8));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 356, this.guiTop + 18, 58, 20, "selectServer.edit",
				!this.selectedDialog.isEmpty()));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 356, this.guiTop + 41, 58, 20, "gui.remove",
				!this.selectedDialog.isEmpty()));
		this.addButton(new GuiNpcButton(11, this.guiLeft + 356, this.guiTop + 64, 58, 20, "gui.add",
				!this.selectedCategory.isEmpty()));
		this.addLabel(new GuiNpcLabel(2, "gui.categories", this.guiLeft + 356, this.guiTop + 110));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 356, this.guiTop + 120, 58, 20, "selectServer.edit",
				!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 356, this.guiTop + 143, 58, 20, "gui.remove",
				!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 356, this.guiTop + 166, 58, 20, "gui.add"));

		if (this.scrollCategories == null) {
			(this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, 200);
		}
		this.scrollCategories.setList(Lists.newArrayList(categoryData.keySet()));
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		if (!this.selectedCategory.isEmpty()) {
			this.scrollCategories.setSelected(this.selectedCategory);
		}
		this.addScroll(this.scrollCategories);

		if (this.scrollDialogs == null) {
			(this.scrollDialogs = new GuiCustomScroll(this, 1)).setSize(170, 200);
		}
		this.scrollDialogs.setList(Lists.newArrayList(dialogData.keySet()));
		this.scrollDialogs.guiLeft = this.guiLeft + 175;
		this.scrollDialogs.guiTop = this.guiTop + 14;
		if (!this.selectedDialog.isEmpty()) {
			this.scrollDialogs.setSelected(this.selectedDialog);
		}
		this.addScroll(this.scrollDialogs);
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
			Client.sendData(EnumPacketServer.DialogSave, this.categoryData.get(this.selectedCategory).id,
					dialog.writeToNBT(new NBTTagCompound()));
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
		}
		super.keyTyped(c, i);
	}
	
}
