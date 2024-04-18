package noppes.npcs.client.gui.mainmenu;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
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
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.drop.SubGuiDropAttribute;
import noppes.npcs.client.gui.drop.SubGuiDropEnchant;
import noppes.npcs.client.gui.drop.SubGuiDropValueNbt;
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
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DropNbtSet;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.entity.data.EnchantSet;

public class GuiDropEdit
extends GuiContainerNPCInterface2
implements ICustomScrollListener, ISubGuiListener, ITextfieldListener {

	private GuiContainer parent;
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
		this.parent = gui;
		this.dropType = dropType;
		this.groupId = groupId;
		this.slot = pos;
		this.drop = cont.inventoryDS;
		this.setBackground("npcdrop.png");
		this.ySize = 200;
		this.closeOnEsc = true;
		this.scrollEnchants = null;
		this.scrollAttributes = null;
		this.scrollTags = null;
		this.reset = 0;
		this.amount = new int[] { this.drop.getMinAmount(), this.drop.getMaxAmount() };
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.parent == null) {
			this.close();
			return;
		}
		int anyIDs = 0;
		// slot
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.slot", this.guiLeft + 171, this.guiTop + 139));
		// chance
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.chance", this.guiLeft + 229, this.guiTop + 145));
		GuiNpcTextField chance = new GuiNpcTextField(0, (GuiScreen) this, this.guiLeft + 268, this.guiTop + 140, 50, 20,
				String.valueOf(this.drop.getChance()));
		chance.setDoubleNumbersOnly().setMinMaxDoubleDefault(0.0001d, 100.0d, this.drop.getChance());
		chance.setEnabled(!this.drop.item.isEmpty());
		this.addTextField(chance);
		// amount
		boolean needReAmount = false;
		this.amount = this.drop.amount;
		if (this.drop.getMinAmount() > this.drop.item.getMaxStackSize()) {
			this.amount[0] = this.drop.item.getMaxItemDamage();
			needReAmount = true;
		} else if (this.drop.getMinAmount() <= 0) {
			this.amount[0] = 1;
			needReAmount = true;
		}
		if (this.drop.getMaxAmount() > this.drop.item.getMaxStackSize()) {
			this.amount[1] = this.drop.item.getMaxStackSize();
			needReAmount = true;
		} else if (this.drop.getMaxAmount() <= 0) {
			this.amount[1] = 1;
			needReAmount = true;
		}
		if (needReAmount) {
			this.drop.setAmount(this.amount[0], this.amount[1]);
		}
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.amount", this.guiLeft + 329, this.guiTop + 145));
		GuiNpcTextField countMin = new GuiNpcTextField(1, (GuiScreen) this, this.guiLeft + 366, this.guiTop + 136, 50, 14, "" + this.amount[0]);
		countMin.setNumbersOnly().setMinMaxDefault(1, this.drop.item.getMaxStackSize(), this.drop.item.getStackSize());
		countMin.setEnabled(!this.drop.item.isEmpty());
		this.addTextField(countMin);
		GuiNpcTextField countMax = new GuiNpcTextField(2, (GuiScreen) this, this.guiLeft + 366, this.guiTop + 150, 50, 14, "" + this.amount[1]);
		countMax.setNumbersOnly().setMinMaxDefault(1, this.drop.item.getMaxStackSize(), this.drop.item.getStackSize());
		countMax.setEnabled(!this.drop.item.isEmpty());
		this.addTextField(countMax);
		// damage
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.break", this.guiLeft + 229, this.guiTop + 174));
		GuiNpcTextField damage = new GuiNpcTextField(3, (GuiScreen) this, this.guiLeft + 268, this.guiTop + 169, 50, 20,
				String.valueOf(this.drop.getDamage()));
		damage.setDoubleNumbersOnly().setMinMaxDoubleDefault(0.0d, 1.0d, (double) this.drop.getDamage());
		damage.setEnabled(this.drop.item.getMaxItemDamage() != 0);
		this.addTextField(damage);
		// reset
		this.addButton(new GuiNpcButton(0, this.guiLeft + 171, this.guiTop + 169, 48, 20, "remote.reset", !this.drop.item.isEmpty()));
		// Enchants:
		// List
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.enchants", this.guiLeft + 4, this.guiTop + 5));
		Map<String, EnchantSet> newEnchData = new HashMap<String, EnchantSet>();
		for (IEnchantSet ies : this.drop.getEnchantSets()) {
			newEnchData.put(((EnchantSet) ies).getKey(), (EnchantSet) ies);
		}
		this.enchantData = newEnchData;
		if (this.scrollEnchants == null) {
			(this.scrollEnchants = new GuiCustomScroll(this, 0)).setSize(133, 93);
		}
		this.scrollEnchants.setList(Lists.newArrayList(this.enchantData.keySet()));
		this.scrollEnchants.guiLeft = this.guiLeft + 4;
		this.scrollEnchants.guiTop = this.guiTop + 16;
		if (this.enchant != null) {
			this.scrollEnchants.setSelected(this.enchant.getKey());
		}
		this.addScroll(this.scrollEnchants);
		// Buttons
		this.addButton(new GuiNpcButton(1, this.guiLeft + 4, this.guiTop + 112, 43, 20, "gui.add",
				!this.drop.item.isEmpty() && this.enchantData.size() <= 16));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 4 + 45, this.guiTop + 112, 43, 20, "gui.remove",
				this.scrollEnchants.getSelected() != null));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 4 + 91, this.guiTop + 112, 43, 20, "selectServer.edit",
				this.scrollEnchants.getSelected() != null));
		// Attributes:
		// List
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.attributes", this.guiLeft + 143, this.guiTop + 5));
		Map<String, AttributeSet> newAttrData = new HashMap<String, AttributeSet>();
		for (IAttributeSet ias : this.drop.getAttributeSets()) {
			newAttrData.put(((AttributeSet) ias).getKey(), ((AttributeSet) ias));
		}
		this.attributesData = newAttrData;
		if (this.scrollAttributes == null) {
			(this.scrollAttributes = new GuiCustomScroll(this, 1)).setSize(133, 93);
		}
		this.scrollAttributes.setList(Lists.newArrayList(this.attributesData.keySet()));
		this.scrollAttributes.guiLeft = this.guiLeft + 143;
		this.scrollAttributes.guiTop = this.guiTop + 16;
		if (this.attribute != null) {
			this.scrollAttributes.setSelected(this.attribute.getKey());
		}
		this.addScroll(this.scrollAttributes);
		// Buttons
		this.addButton(new GuiNpcButton(4, this.guiLeft + 143, this.guiTop + 112, 43, 20, "gui.add",
				!this.drop.item.isEmpty() && this.attributesData.size() <= 16));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 143 + 45, this.guiTop + 112, 44, 20, "gui.remove",
				this.scrollAttributes.getSelected() != null));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 143 + 91, this.guiTop + 112, 43, 20, "selectServer.edit",
				this.scrollAttributes.getSelected() != null));
		// Tags:
		// List
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.tags", this.guiLeft + 283, this.guiTop + 5));
		Map<String, DropNbtSet> newTagsData = new HashMap<String, DropNbtSet>();
		for (IDropNbtSet dns : this.drop.getDropNbtSets()) {
			newTagsData.put(((DropNbtSet) dns).getKey(), (DropNbtSet) dns);
		}
		this.tagsData = newTagsData;
		if (this.scrollTags == null) {
			(this.scrollTags = new GuiCustomScroll(this, 2)).setSize(133, 93);
		}
		this.scrollTags.setList(Lists.newArrayList(this.tagsData.keySet()));
		this.scrollTags.guiLeft = this.guiLeft + 283;
		this.scrollTags.guiTop = this.guiTop + 16;
		if (this.tag != null) {
			this.scrollTags.setSelected(this.tag.getKey());
		}
		this.addScroll(this.scrollTags);
		// Buttons
		this.addButton(new GuiNpcButton(7, this.guiLeft + 283, this.guiTop + 112, 43, 20, "gui.add",
				!this.drop.item.isEmpty() && this.tagsData.size() <= 24));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 283 + 45, this.guiTop + 112, 43, 20, "gui.remove",
				this.scrollTags.getSelected() != null));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 283 + 91, this.guiTop + 112, 43, 20, "selectServer.edit",
				this.scrollTags.getSelected() != null));
		// lootMode
		int lm = 0;
		if (this.drop.lootMode) {
			lm = 1;
		}
		this.addButton(new GuiNpcButton(10, this.guiLeft + 329, this.guiTop + 169, 87, 20,
				new String[] { "stats.normal", "inv.auto" }, lm));
		this.getButton(10).setEnabled(!this.drop.item.isEmpty());
		// tiedToLevel
		int ttl = 0;
		if (this.drop.tiedToLevel) {
			ttl = 1;
		}
		this.addButton(new GuiNpcButton(11, this.guiLeft + 329, this.guiTop + 192, 87, 20,
				new String[] { "drop.type.random", "drop.type.level" }, ttl));
		this.getButton(11).setEnabled(!this.drop.item.isEmpty());
		// Quest ID
		this.addLabel(new GuiNpcLabel(anyIDs++, "global.quests", this.guiLeft + 229, this.guiTop + 197));
		int qid = 0;
		if (this.drop.questId >= 0) {
			qid = this.drop.questId;
		}
		GuiNpcTextField questId = new GuiNpcTextField(4, this, this.guiLeft + 268, this.guiTop + 192, 50, 20, "" + qid);
		questId.setNumbersOnly().setMinMaxDefault(0, Integer.MAX_VALUE, qid);
		this.addTextField(questId);
		// if (this.drop!=null) { return; }
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // reset drop
				this.drop.resetTo(this.drop.item);
				this.initGui();
				break;
			}
			case 1: { // add enchant
				this.enchant = (EnchantSet) this.drop.addEnchant(0);
				this.setSubGui(new SubGuiDropEnchant(this.enchant));
				break;
			}
			case 2: { // remove enchant
				this.drop.removeEnchant(this.enchantData.get(this.scrollEnchants.getSelected()));
				this.initGui();
				break;
			}
			case 3: { // edit enchant
				this.setSubGui(new SubGuiDropEnchant(this.enchant));
				break;
			}
			case 4: { // add attribute
				this.attribute = (AttributeSet) this.drop.addAttribute("");
				this.setSubGui(new SubGuiDropAttribute(this.attribute));
				break;
			}
			case 5: { // remove attribute
				this.drop.removeAttribute(this.attributesData.get(this.scrollAttributes.getSelected()));
				this.initGui();
				break;
			}
			case 6: { // edit attribute
				this.setSubGui(new SubGuiDropAttribute(this.attribute));
				break;
			}
			case 7: { // add tag
				this.tag = (DropNbtSet) this.drop.addDropNbtSet(0, 100.0d, new String(), new String[0]);
				this.setSubGui(new SubGuiDropValueNbt(this.tag));
				break;
			}
			case 8: { // remove tag
				this.drop.removeDropNbt(this.tagsData.get(this.scrollTags.getSelected()));
				this.initGui();
				break;
			}
			case 9: { // edit tag
				this.setSubGui(new SubGuiDropValueNbt(this.tag));
				break;
			}
			case 10: { // loot mode
				this.drop.setLootMode(button.getValue() == 1);
				break;
			}
			case 11: { // tied mode
				this.drop.setTiedToLevel(button.getValue() == 1);
				break;
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.reset > 0) {
			this.reset--;
			if (this.reset == 0) {
				this.initGui();
			}
		}
		if (this.subgui != null) { return; }
		if (this.inventorySlots.getSlot(0)!=null && this.getButton(0)!=null) {
			ItemStack stack = this.inventorySlots.getSlot(0).getStack();
			GuiNpcButton button = this.getButton(0);
			if (button.enabled && stack.isEmpty()) {
				this.drop.item = NpcAPI.Instance().getIItemStack(stack);
			}
			else if (!button.enabled && !stack.isEmpty()) {
				this.drop.item = NpcAPI.Instance().getIItemStack(stack);
			}
			
		}
		if (!CustomNpcs.ShowDescriptions) { return; }
		String tied = new TextComponentTranslation("drop.tied.random", new Object[0]).getFormattedText();
		if (this.drop.tiedToLevel) {
			tied = new TextComponentTranslation("drop.tied.level", new Object[0]).getFormattedText();
		}
		if (isMouseHover(i, j, this.guiLeft + 171, this.guiTop + 139, 28, 10)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.slot", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 5, 133, 10)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.enchants", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 143, this.guiTop + 5, 134, 10)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.attributes", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 283, this.guiTop + 5, 133, 10)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tags", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 173, this.guiTop + 171, 44, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.reset", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 270, this.guiTop + 142, 46, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.chance", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 368, this.guiTop + 138, 46, 24)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.amount", new Object[] { tied }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 270, this.guiTop + 171, 46, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.break", new Object[] { tied }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 114, 39, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.enchant.add", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6 + 45, this.guiTop + 114, 39, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.enchant.del", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6 + 91, this.guiTop + 114, 39, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.enchant.edit", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 145, this.guiTop + 114, 39, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.attribute.add", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 145 + 45, this.guiTop + 114, 40, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.attribute.del", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 145 + 91, this.guiTop + 114, 39, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.attribute.edit", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 285, this.guiTop + 114, 39, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.add", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 285 + 45, this.guiTop + 114, 39, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.del", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 285 + 91, this.guiTop + 114, 39, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.edit", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 331, this.guiTop + 171, 83, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.mode", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 331, this.guiTop + 194, 83, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tied",
					new Object[] { ((char) 167) + "b" + CustomNpcs.MaxLv, ((char) 167) + "7" + CustomNpcs.MaxLv,
							"" + ((int) (3 + (12 - 3) * 17 / CustomNpcs.MaxLv)) }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 270, this.guiTop + 194, 46, 16)) {
			String qname = "";
			GuiNpcButton button = this.getButton(14);
			if (button != null && button.getValue() > 0) {
				int id = Integer.valueOf(button.getVariants()[button.getValue()]);
				for (IQuestCategory cat : NpcAPI.Instance().getQuests().categories()) {
					for (IQuest q : cat.quests()) {
						if (q.getId() == id) {
							this.setHoverText(
									new TextComponentTranslation("drop.hover.quest", new Object[] { q.getName() })
											.getFormattedText());
							break;
						}
					}
					if (qname.length() > 0) {
						break;
					}
				}
			} else {
				this.setHoverText(new TextComponentTranslation("drop.hover.any.quest", new Object[0]).getFormattedText());
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		// cheak error inventory gui
		if (this.isMouseHover(i, j, this.guiLeft, this.guiTop - 20, this.width, 20)) {
			close();
		} else {
			super.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (this.slot==-1) {
			if (this.drop.item.isEmpty()) { return; }
			if (this.drop.getMinAmount() == 1 && this.drop.getMinAmount() == 1) {
				this.drop.setAmount(this.drop.item.getStackSize(), this.drop.item.getStackSize());
			}
		}
		this.drop.item.setStackSize(1);
		Client.sendData(EnumPacketServer.MainmenuInvDropSave, this.dropType, this.groupId, this.slot, this.drop.getNBT());
	}

	@Override
	public void close() {
		GuiNpcTextField.unfocus();
		if (this.parent != null) {
			this.save();
			this.displayGuiScreen(this.parent);
		} else {
			super.close();
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		switch (scroll.id) {
		case 0: { // scrollEnchants
			this.enchant = this.enchantData.get(scroll.getSelected());
			break;
		}
		case 1: { // scrollAttributes
			this.attribute = this.attributesData.get(scroll.getSelected());
			break;
		}
		case 2: { // scrollTags
			this.tag = this.tagsData.get(scroll.getSelected());
			break;
		}
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		switch (scroll.id) {
		case 0: { // scrollEnchants
			this.setSubGui(new SubGuiDropEnchant(this.enchant));
			break;
		}
		case 1: { // scrollAttributes
			this.setSubGui(new SubGuiDropAttribute(this.attribute));
			break;
		}
		case 2: { // scrollTags
			this.setSubGui(new SubGuiDropValueNbt(this.tag));
			break;
		}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiDropEnchant) {
			SubGuiDropEnchant gui = (SubGuiDropEnchant) subgui;
			this.enchant.load(gui.enchant.getNBT());
		} else if (subgui instanceof SubGuiDropAttribute) {
			SubGuiDropAttribute gui = (SubGuiDropAttribute) subgui;
			if (gui.attribute.getAttribute().length() == 0) {
				this.drop.removeAttribute(this.attribute);
			} else {
				this.attribute.load(gui.attribute.getNBT());
			}
		} else if (subgui instanceof SubGuiDropValueNbt) {
			SubGuiDropValueNbt gui = (SubGuiDropValueNbt) subgui;
			if (gui.tag.getPath().length() == 0 || gui.tag.getValues().length == 0) {
				this.drop.removeDropNbt(this.tag);
			} else {
				this.tag.load(gui.tag.getNBT());
			}
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
			case 0: { // common chance
				this.drop.setChance(textField.getDouble());
				break;
			}
			case 1: { // amount min
				this.amount[0] = textField.getInteger();
				this.drop.setAmount(this.amount[0], this.amount[1]);
				break;
			}
			case 2: { // amount max
				this.amount[1] = textField.getInteger();
				this.drop.setAmount(this.amount[0], this.amount[1]);
				break;
			}
			case 3: { // break item
				this.drop.setDamage((float) textField.getDouble());
				break;
			}
			case 4: { // quest set
				int qid = textField.getInteger();
				if (qid == 0) {
					this.drop.questId = 0;
				} else {
					this.drop.questId = qid;
				}
				break;
			}
		}
	}
	
	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
        if (slotIn != null) { this.reset = 5; }
    }
	
}
