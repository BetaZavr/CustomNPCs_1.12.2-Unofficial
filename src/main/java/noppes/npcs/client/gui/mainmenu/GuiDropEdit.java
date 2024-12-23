package noppes.npcs.client.gui.mainmenu;

import java.io.IOException;
import java.util.*;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.drop.SubGuiDropAttribute;
import noppes.npcs.client.gui.drop.SubGuiDropEnchant;
import noppes.npcs.client.gui.drop.SubGuiDropValueNbt;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCDropSetup;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DropNbtSet;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.entity.data.EnchantSet;

import javax.annotation.Nonnull;

public class GuiDropEdit
extends GuiContainerNPCInterface2
implements ICustomScrollListener, ISubGuiListener, ITextfieldListener {

	private final GuiContainer parent;
	public DropSet drop;
	private Map<String, AttributeSet> attributesData;
	private Map<String, EnchantSet> enchantData;
	private Map<String, DropNbtSet> tagsData;
	private AttributeSet attribute;
	private EnchantSet enchant;
	private DropNbtSet tag;
	public int dropType, groupId, slot;
	private int reset;
	private int[] amount;
	private GuiCustomScroll scrollAttributes;
	private GuiCustomScroll scrollEnchants;
	private GuiCustomScroll scrollTags;

	public GuiDropEdit(EntityNPCInterface npc, ContainerNPCDropSetup cont, GuiContainer gui, int dropType, int groupId, int pos) {
		super(npc, cont);
		ySize = 200;
		closeOnEsc = true;
		setBackground("npcdrop.png");

		parent = gui;
		this.dropType = dropType;
		this.groupId = groupId;
		slot = pos;
		drop = cont.inventoryDS;
		scrollEnchants = null;
		scrollAttributes = null;
		scrollTags = null;
		reset = 0;
		amount = new int[] { drop.getMinAmount(), drop.getMaxAmount() };
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // reset drop
				drop.resetTo(drop.item);
				initGui();
				break;
			}
			case 1: { // add enchant
				enchant = (EnchantSet) drop.addEnchant(0);
				setSubGui(new SubGuiDropEnchant(enchant));
				break;
			}
			case 2: { // remove enchant
				drop.removeEnchant(enchantData.get(scrollEnchants.getSelected()));
				initGui();
				break;
			}
			case 3: { // edit enchant
				setSubGui(new SubGuiDropEnchant(enchant));
				break;
			}
			case 4: { // add attribute
				attribute = (AttributeSet) drop.addAttribute("");
				setSubGui(new SubGuiDropAttribute(attribute));
				break;
			}
			case 5: { // remove attribute
				drop.removeAttribute(attributesData.get(scrollAttributes.getSelected()));
				initGui();
				break;
			}
			case 6: { // edit attribute
				setSubGui(new SubGuiDropAttribute(attribute));
				break;
			}
			case 7: { // add tag
				tag = (DropNbtSet) drop.addDropNbtSet(0, 100.0d, "", new String[0]);
				setSubGui(new SubGuiDropValueNbt(tag));
				break;
			}
			case 8: { // remove tag
				drop.removeDropNbt(tagsData.get(scrollTags.getSelected()));
				initGui();
				break;
			}
			case 9: { // edit tag
				setSubGui(new SubGuiDropValueNbt(tag));
				break;
			}
			case 10: { // loot mode
				drop.setLootMode(button.getValue());
				break;
			}
			case 11: { // tied mode
				drop.setTiedToLevel(button.getValue() == 1);
				break;
			}
			case 12: { // quest select
				setSubGui(new GuiQuestSelection(drop.questId));
				break;
			}
		}
	}

	@Override
	public void close() {
		GuiNpcTextField.unfocus();
		if (parent != null) {
			save();
			displayGuiScreen(parent);
		} else {
			super.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (reset > 0) {
			reset--;
			if (reset == 0) {
				initGui();
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui != null) { return; }
        inventorySlots.getSlot(0);
        if (getButton(0) != null) {
			ItemStack stack = inventorySlots.getSlot(0).getStack();
			GuiNpcButton button = getButton(0);
			if (button.enabled && stack.isEmpty()) {
				drop.item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
			} else if (!button.enabled && !stack.isEmpty()) {
				drop.item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
			}

		}
		if (!CustomNpcs.ShowDescriptions) { return; }
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (drop.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		if (isMouseHover(mouseX, mouseY, guiLeft + 368, guiTop + 138, 46, 24)) {
			drawHoverText("drop.hover.amount", tied);
		} else if (isMouseHover(mouseX, mouseY, guiLeft + 270, guiTop + 171, 46, 16)) {
			drawHoverText("drop.hover.break", tied);
		} else if (getTextField(4) != null && getTextField(4).isMouseOver()) {
			Quest quest = QuestController.instance.quests.get(getTextField(4).getInteger());
			if (quest != null) { drawHoverText("drop.hover.quest", quest.getName()); }
			else { drawHoverText("drop.hover.any.quest"); }
		}
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
        reset = 3;
    }

	@Override
	public void initGui() {
		super.initGui();
		if (parent == null) {
			close();
			return;
		}
		int anyIDs = 0;
		// slot
		GuiNpcLabel label = new GuiNpcLabel(anyIDs++, "drop.slot", guiLeft + 171, guiTop + 139);
		label.setHoverText("drop.hover.slot");
		addLabel(label);
		// chance
		addLabel(new GuiNpcLabel(anyIDs++, "drop.chance", guiLeft + 229, guiTop + 145));
		GuiNpcTextField chance = new GuiNpcTextField(0, this, guiLeft + 268, guiTop + 140, 50, 20, String.valueOf(drop.getChance()));
		chance.setMinMaxDoubleDefault(0.0001d, 100.0d, drop.getChance());
		chance.setEnabled(!drop.item.isEmpty());
		chance.setHoverText("drop.hover.chance");
		addTextField(chance);
		// amount
		boolean needReAmount = false;
		amount = drop.amount;
		if (drop.getMinAmount() > drop.item.getMaxStackSize()) {
			amount[0] = drop.item.getMaxItemDamage();
			needReAmount = true;
		} else if (drop.getMinAmount() <= 0) {
			amount[0] = 1;
			needReAmount = true;
		}
		if (drop.getMaxAmount() > drop.item.getMaxStackSize()) {
			amount[1] = drop.item.getMaxStackSize();
			needReAmount = true;
		} else if (drop.getMaxAmount() <= 0) {
			amount[1] = 1;
			needReAmount = true;
		}
		if (needReAmount) {
			drop.setAmount(amount[0], amount[1]);
		}
		addLabel(new GuiNpcLabel(anyIDs++, "drop.amount", guiLeft + 329, guiTop + 145));
		GuiNpcTextField countMin = new GuiNpcTextField(1, this, guiLeft + 366, guiTop + 136, 50, 14, "" + amount[0]);
		countMin.setMinMaxDefault(1, drop.item.getMaxStackSize(), drop.item.getStackSize());
		countMin.setEnabled(!drop.item.isEmpty());
		addTextField(countMin);
		GuiNpcTextField countMax = new GuiNpcTextField(2, this, guiLeft + 366, guiTop + 150, 50, 14, "" + amount[1]);
		countMax.setMinMaxDefault(1, drop.item.getMaxStackSize(), drop.item.getStackSize());
		countMax.setEnabled(!drop.item.isEmpty());
		addTextField(countMax);
		// damage
		addLabel(new GuiNpcLabel(anyIDs++, "drop.break", guiLeft + 229, guiTop + 174));
		GuiNpcTextField damage = new GuiNpcTextField(3, this, guiLeft + 268, guiTop + 169, 50, 20, String.valueOf(drop.getDamage()));
		damage.setMinMaxDoubleDefault(0.0d, 1.0d, drop.getDamage());
		damage.setEnabled(drop.item.getMaxItemDamage() != 0);
		addTextField(damage);
		// reset
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 171, guiTop + 169, 48, 20, "remote.reset", !drop.item.isEmpty());
		button.setHoverText("drop.hover.reset");
		addButton(button);
		// Enchants:
		// List
		label = new GuiNpcLabel(anyIDs++, "drop.enchants", guiLeft + 4, guiTop + 5);
		label.setHoverText("drop.hover.enchants");
		addLabel(label);
		Map<String, EnchantSet> newEnchData = new HashMap<>();
		for (IEnchantSet ies : drop.getEnchantSets()) {
			newEnchData.put(((EnchantSet) ies).getKey(), (EnchantSet) ies);
		}
		enchantData = newEnchData;
		if (scrollEnchants == null) {
			(scrollEnchants = new GuiCustomScroll(this, 0)).setSize(133, 93);
		}
		scrollEnchants.setList(new ArrayList<>(enchantData.keySet()));
		scrollEnchants.guiLeft = guiLeft + 4;
		scrollEnchants.guiTop = guiTop + 16;
		if (enchant != null) {
			scrollEnchants.setSelected(enchant.getKey());
		}
		addScroll(scrollEnchants);
		// enchant add
		button = new GuiNpcButton(1, guiLeft + 4, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && enchantData.size() <= 16);
		button.setHoverText("drop.hover.enchant.add");
		addButton(button);
		// enchant del
		button = new GuiNpcButton(2, guiLeft + 4 + 45, guiTop + 112, 43, 20, "gui.remove", scrollEnchants.getSelected() != null);
		button.setHoverText("drop.hover.enchant.del");
		addButton(button);
		// enchant edit
		button = new GuiNpcButton(3, guiLeft + 4 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollEnchants.getSelected() != null);
		button.setHoverText("drop.hover.enchant.edit");
		addButton(button);
		// Attributes:
		// List
		label = new GuiNpcLabel(anyIDs++, "drop.attributes", guiLeft + 143, guiTop + 5);
		label.setHoverText("drop.hover.attributes");
		addLabel(label);
		Map<String, AttributeSet> newAttrData = new HashMap<>();
		for (IAttributeSet ias : drop.getAttributeSets()) {
			newAttrData.put(((AttributeSet) ias).getKey(), ((AttributeSet) ias));
		}
		attributesData = newAttrData;
		if (scrollAttributes == null) {
			(scrollAttributes = new GuiCustomScroll(this, 1)).setSize(133, 93);
		}
		scrollAttributes.setList(new ArrayList<>(attributesData.keySet()));
		scrollAttributes.guiLeft = guiLeft + 143;
		scrollAttributes.guiTop = guiTop + 16;
		if (attribute != null) {
			scrollAttributes.setSelected(attribute.getKey());
		}
		addScroll(scrollAttributes);
		// attribute add
		button = new GuiNpcButton(4, guiLeft + 143, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && attributesData.size() <= 16);
		button.setHoverText("drop.hover.attribute.add");
		addButton(button);
		// attribute del
		button = new GuiNpcButton(5, guiLeft + 143 + 45, guiTop + 112, 44, 20, "gui.remove", scrollAttributes.getSelected() != null);
		button.setHoverText("drop.hover.attribute.del");
		addButton(button);
		// attribute edit
		button = new GuiNpcButton(6, guiLeft + 143 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollAttributes.getSelected() != null);
		button.setHoverText("drop.hover.attribute.edit");
		addButton(button);
		// Tags:
		// List
		label = new GuiNpcLabel(anyIDs++, "drop.tags", guiLeft + 283, guiTop + 5);
		label.setHoverText("drop.hover.tags");
		addLabel(label);
		Map<String, DropNbtSet> newTagsData = new HashMap<>();
		for (IDropNbtSet dns : drop.getDropNbtSets()) {
			newTagsData.put(((DropNbtSet) dns).getKey(), (DropNbtSet) dns);
		}
		tagsData = newTagsData;
		if (scrollTags == null) {
			(scrollTags = new GuiCustomScroll(this, 2)).setSize(133, 93);
		}
		scrollTags.setList(new ArrayList<>(tagsData.keySet()));
		scrollTags.guiLeft = guiLeft + 283;
		scrollTags.guiTop = guiTop + 16;
		if (tag != null) {
			scrollTags.setSelected(tag.getKey());
		}
		addScroll(scrollTags);
		// tag add
		button = new GuiNpcButton(7, guiLeft + 283, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && tagsData.size() <= 24);
		button.setHoverText("drop.hover.tag.add");
		addButton(button);
		// tag del
		button = new GuiNpcButton(8, guiLeft + 283 + 45, guiTop + 112, 43, 20, "gui.remove", scrollTags.getSelected() != null);
		button.setHoverText("drop.hover.tag.del");
		addButton(button);
		// tag edit
		button = new GuiNpcButton(9, guiLeft + 283 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollTags.getSelected() != null);
		button.setHoverText("drop.hover.tag.edit");
		addButton(button);
		// lootMode
		button = new GuiNpcButton(10, guiLeft + 329, guiTop + 169, 87, 20, new String[] { "stats.normal", "inv.auto", "inv.inventory" }, drop.lootMode);
		button.setEnabled(!drop.item.isEmpty());
		button.setHoverText("drop.hover.mode");
		addButton(button);
		// tied level
		button = new GuiNpcButton(11, guiLeft + 329, guiTop + 192, 87, 20, new String[] { "drop.type.random", "drop.type.level" }, drop.tiedToLevel ? 1 : 0);
		button.setEnabled(!drop.item.isEmpty());
		button.setHoverText("drop.hover.tied");
		addButton(button);
		// Quest ID
		addLabel(new GuiNpcLabel(anyIDs, "global.quests", guiLeft + 229, guiTop + 197));
        int qid = Math.max(drop.questId, 0);
		GuiNpcTextField textField = new GuiNpcTextField(4, this, guiLeft + 268, guiTop + 192, 50, 20, "" + qid);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, qid);
		addTextField(textField);
		addButton(new GuiNpcButton(12, guiLeft + 171, guiTop + 191, 48, 20, "availability.select", !drop.item.isEmpty()));
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		// check error inventory gui
		if (isMouseHover(i, j, guiLeft, guiTop - 20, width, 20)) {
			close();
		} else {
			super.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (slot == -1) {
			if (drop.item.isEmpty()) {
				return;
			}
			if (drop.getMinAmount() == 1 && drop.getMinAmount() == 1) {
				drop.setAmount(drop.item.getStackSize(), drop.item.getStackSize());
			}
		}
		drop.item.setStackSize(1);
		Client.sendData(EnumPacketServer.MainmenuInvDropSave, dropType, groupId, slot, drop.getNBT());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) { return; }
		switch (scroll.id) {
			case 0: { // scrollEnchants
				enchant = enchantData.get(scroll.getSelected());
				break;
			}
			case 1: { // scrollAttributes
				attribute = attributesData.get(scroll.getSelected());
				break;
			}
			case 2: { // scrollTags
				tag = tagsData.get(scroll.getSelected());
				break;
			}
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		switch (scroll.id) {
			case 0: { // scrollEnchants
				setSubGui(new SubGuiDropEnchant(enchant));
				break;
			}
			case 1: { // scrollAttributes
				setSubGui(new SubGuiDropAttribute(attribute));
				break;
			}
			case 2: { // scrollTags
				setSubGui(new SubGuiDropValueNbt(tag));
				break;
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiDropEnchant) {
			SubGuiDropEnchant gui = (SubGuiDropEnchant) subgui;
			enchant.load(gui.enchant.getNBT());
		}
		else if (subgui instanceof SubGuiDropAttribute) {
			SubGuiDropAttribute gui = (SubGuiDropAttribute) subgui;
			if (gui.attribute.getAttribute().isEmpty()) {
				drop.removeAttribute(attribute);
			} else {
				attribute.load(gui.attribute.getNBT());
			}
		}
		else if (subgui instanceof SubGuiDropValueNbt) {
			SubGuiDropValueNbt gui = (SubGuiDropValueNbt) subgui;
			if (gui.tag.getPath().isEmpty() || gui.tag.getValues().length == 0) {
				drop.removeDropNbt(tag);
			} else {
				tag.load(gui.tag.getNBT());
			}
		} else if (subgui instanceof GuiQuestSelection) {
			if (((GuiQuestSelection) subgui).selectedQuest != null) {
				drop.questId = ((GuiQuestSelection) subgui).selectedQuest.id;
			}
		}
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
			case 0: { // common chance
				drop.setChance(textField.getDouble());
				break;
			}
			case 1: { // amount min
				amount[0] = textField.getInteger();
				drop.setAmount(amount[0], amount[1]);
				break;
			}
			case 2: { // amount max
				amount[1] = textField.getInteger();
				drop.setAmount(amount[0], amount[1]);
				break;
			}
			case 3: { // break item
				drop.setDamage((float) textField.getDouble());
				break;
			}
			case 4: { // quest set
				drop.questId = textField.getInteger();
				break;
			}
		}
	}

}
