package noppes.npcs.client.gui.drop;

import java.util.*;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCDropSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DropNbtSet;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.entity.data.EnchantSet;

import javax.annotation.Nonnull;

public class SubGuiDropEdit extends GuiContainerNPCInterface
		implements ICustomScrollListener, ITextfieldListener {

	public static GuiContainer parent;
	public static BlockPos parentData;
	public static EnumGuiType parentContainer;

	protected final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
	protected final ContainerNPCDropSetup menu;

	protected Map<String, AttributeSet> attributesData;
	protected Map<String, EnchantSet> enchantData;
	protected Map<String, DropNbtSet> tagsData;
	protected AttributeSet attribute;
	protected EnchantSet enchant;
	protected DropNbtSet tag;
	protected GuiCustomScroll scrollAttributes = null;
	protected GuiCustomScroll scrollEnchants = null;
	protected GuiCustomScroll scrollTags = null;
	protected DropSet drop;
	protected int[] amount;
	protected int reset = 0;

	public SubGuiDropEdit(EntityNPCInterface npc, ContainerNPCDropSetup container) {
		super(npc, container);
		setBackground("npcdrop.png");
		drawDefaultBackground = false;
		closeOnEsc = true;
		xSize = 421;
		ySize = 217;
		menu = container;

		drop = menu.inventoryDS;
		if (drop != null) { amount = new int[] { drop.getMinAmount(), drop.getMaxAmount() }; }
	}

	@Override
	public void drawDefaultBackground() {
		RenderHelper.disableStandardItemLighting();
		// Background
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		GlStateManager.scale(bgScale, bgScale, bgScale);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, 252, ySize);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(252, 0, 256 - xSize + 252, 0, xSize - 252, ySize);
		GlStateManager.popMatrix();
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		GuiNpcTextField.unfocus();
		switch (button.getID()) {
			case 0: {
				drop.resetTo(drop.item);
				initGui();
				break;
			} // reset drop
			case 1: {
				enchant = (EnchantSet) drop.addEnchant(0);
				setSubGui(new SubGuiDropEnchant(enchant));
				break;
			} // add enchant
			case 2: {
				drop.removeEnchant(enchantData.get(scrollEnchants.getSelected()));
				initGui();
				break;
			} // remove enchant
			case 3: setSubGui(new SubGuiDropEnchant(enchant)); break; // edit enchant
			case 4: {
				attribute = (AttributeSet) drop.addAttribute("");
				setSubGui(new SubGuiDropAttribute(attribute));
				break;
			} // add attribute
			case 5: {
				drop.removeAttribute(attributesData.get(scrollAttributes.getSelected()));
				initGui();
				break;
			} // remove attribute
			case 6: setSubGui(new SubGuiDropAttribute(attribute)); break; // edit attribute
			case 7: {
				tag = (DropNbtSet) drop.addDropNbtSet(0, 100.0d, "", new String[0]);
				setSubGui(new SubGuiDropValueNbt(tag));
				break;
			} // add tag
			case 8: {
				drop.removeDropNbt(tagsData.get(scrollTags.getSelected()));
				initGui();
				break;
			} // remove tag
			case 9: setSubGui(new SubGuiDropValueNbt(tag)); break; // edit tag
			case 10: drop.setLootMode(button.getValue()); break; // loot mode
			case 11: drop.setTiedToLevel(button.getValue() == 1); break; // tied mode
			case 12: setSubGui(new SubGuiNpcAvailability(drop.availability, this)); break; // availability
		}
	}

	@Override
	public void onClosed() {
		GuiNpcTextField.unfocus();
		save();
		if (parent != null) { displayGuiScreen(parent); }
		else if (parentData != null && parentContainer != null) { NoppesUtil.requestOpenGUI(parentContainer, parentData.getX(), parentData.getY(), parentData.getZ()); }
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (drop == null || (parent == null && (parentData == null || parentContainer == null))) {
			String message = "";
			if (drop == null) { message = "drop; "; }
			if (parent == null) {
				message += "parent";
				if (parentData == null || parentContainer == null) { message += " and data or container"; }
				message += "; ";
			}
			LogWriter.pathInfo("Not set " + message + " to GUI", -1);
			onClosed();
			return;
		}
		if (reset > 0) {
			reset--;
			if (reset == 0) { initGui(); }
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
        if (getButton(0) != null) {
			ItemStack stack = inventorySlots.getSlot(0).getStack();
			GuiNpcButton button = getButton(0);
			if (button.enabled && stack.isEmpty()) { drop.item = stack; }
			else if (!button.enabled && !stack.isEmpty()) { drop.item = stack; }
		}
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		if (hasSubGui()) { return; }
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
        reset = 1;
    }

	@Override
	public void initGui() {
		super.initGui();
		if (drop.item != menu.getSlot(0).getStack()) { drop.item = menu.getSlot(0).getStack(); }
		int lId = 0;
		// slot
		addLabel(new GuiNpcLabel(lId++, "drop.slot", guiLeft + 171, guiTop + 139)
				.setHoverText("drop.hover.slot"));
		int x = guiLeft + 225;
		int y = guiTop + 135;
		// chance
		addLabel(new GuiNpcLabel(lId++, "drop.chance", x, y + 2, 48, 12)
				.setIsVisible(!drop.item.isEmpty()));
		addTextField(new GuiNpcTextField(0, this, x + 50, y, 50, 16, String.valueOf(drop.getChance()))
				.setMinMaxDoubleDefault(0.0001d, 100.0d, drop.getChance())
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText("drop.hover.chance"));
		// damage
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (drop.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		addLabel(new GuiNpcLabel(lId++, "drop.break", x, (y += 18) + 2)
				.setIsVisible(!drop.item.isEmpty()));
		addTextField(new GuiNpcTextField(3, this, x + 50, y, 50, 16, String.valueOf(drop.getDamage()))
				.setMinMaxDoubleDefault(0.0d, 1.0d, drop.getDamage())
				.setIsVisible(!drop.item.isEmpty())
				.setIsEnable(drop.item.getMaxDamage() != 0)
				.setHoverText("drop.hover.break", tied));
		// amount
		boolean needReAmount = false;
		amount = drop.amount;
		if (drop.getMinAmount() > drop.item.getMaxStackSize()) {
			amount[0] = drop.item.getMaxStackSize();
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
		if (needReAmount) { drop.setAmount(amount[0], amount[1]); }
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("drop.amount").appendText(":"), x, y += 16, 48, 12)
				.setIsVisible(!drop.item.isEmpty()));
		addLabel(new GuiNpcLabel(lId++, "gui.min", x, (y += 10) + 2, 48, 12)
				.setIsVisible(!drop.item.isEmpty()));
		addTextField(new GuiNpcTextField(1, this, x + 50, y, 50, 16, "" + amount[0])
				.setMinMaxDefault(1, drop.item.getMaxStackSize(), drop.item.getCount())
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText("drop.hover.amount", tied));
		addLabel(new GuiNpcLabel(lId++, "gui.max", x, (y += 18) + 2, 48, 12)
				.setIsVisible(!drop.item.isEmpty()));
		addTextField(new GuiNpcTextField(2, this, x + 50, y, 50, 16, "" + amount[1])
				.setMinMaxDefault(1, drop.item.getMaxStackSize(), drop.item.getCount())
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText("drop.hover.amount", tied));
		getTextField(2).setVisible(!drop.item.isEmpty());
		// reset
		addButton(new GuiNpcButton( 0, guiLeft + 171, guiTop + 169, 48, 20, "remote.reset")
				.setIsEnable(!drop.item.isEmpty())
				.setHoverText("drop.hover.reset"));
		// Enchants:
		// List
		addLabel(new GuiNpcLabel(lId++, "drop.enchants", guiLeft + 4, guiTop + 5, 133, 12)
				.setHoverText("drop.hover.enchants"));
		Map<String, EnchantSet> newEnchData = new HashMap<>();
		for (IEnchantSet ies : drop.getEnchantSets()) { newEnchData.put(((EnchantSet) ies).getKey(), (EnchantSet) ies); }
		enchantData = newEnchData;
		if (scrollEnchants == null) { scrollEnchants = new GuiCustomScroll(this, 0).setSize(133, 93); }
		scrollEnchants.setList(new ArrayList<>(enchantData.keySet()));
		scrollEnchants.guiLeft = guiLeft + 4;
		scrollEnchants.guiTop = guiTop + 16;
		if (enchant != null) { scrollEnchants.setSelected(enchant.getKey()); }
		addScroll(scrollEnchants);
		// enchant add
		addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && enchantData.size() <= 16)
				.setHoverText("drop.hover.enchant.add"));
		// enchant del
		addButton(new GuiNpcButton(2, guiLeft + 4 + 45, guiTop + 112, 43, 20, "gui.remove", scrollEnchants.getSelected() != null)
				.setHoverText("drop.hover.enchant.del"));
		// enchant edit
		addButton(new GuiNpcButton(3, guiLeft + 4 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollEnchants.getSelected() != null)
				.setHoverText("drop.hover.enchant.edit"));
		// Attributes:
		// List
		addLabel(new GuiNpcLabel(lId++, "drop.attributes", guiLeft + 143, guiTop + 5)
				.setHoverText("drop.hover.attributes"));
		Map<String, AttributeSet> newAttrData = new HashMap<>();
		for (IAttributeSet ias : drop.getAttributeSets()) { newAttrData.put(((AttributeSet) ias).getKey(), ((AttributeSet) ias)); }
		attributesData = newAttrData;
		if (scrollAttributes == null) { scrollAttributes = new GuiCustomScroll(this, 1).setSize(133, 93); }
		scrollAttributes.setList(new ArrayList<>(attributesData.keySet()));
		scrollAttributes.guiLeft = guiLeft + 143;
		scrollAttributes.guiTop = guiTop + 16;
		if (attribute != null) { scrollAttributes.setSelected(attribute.getKey()); }
		addScroll(scrollAttributes);
		// attribute add
		addButton(new GuiNpcButton(4, guiLeft + 143, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && attributesData.size() <= 16)
				.setHoverText("drop.hover.attribute.add"));
		// attribute del
		addButton(new GuiNpcButton(5, guiLeft + 143 + 45, guiTop + 112, 44, 20, "gui.remove", scrollAttributes.getSelected() != null)
				.setHoverText("drop.hover.attribute.del"));
		// attribute edit
		addButton(new GuiNpcButton(6, guiLeft + 143 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollAttributes.getSelected() != null)
				.setHoverText("drop.hover.attribute.edit"));
		// Tags:
		// List
		addLabel(new GuiNpcLabel(lId, "drop.tags", guiLeft + 283, guiTop + 5)
				.setHoverText("drop.hover.tags"));
		Map<String, DropNbtSet> newTagsData = new HashMap<>();
		for (IDropNbtSet dns : drop.getDropNbtSets()) { newTagsData.put(((DropNbtSet) dns).getKey(), (DropNbtSet) dns); }
		tagsData = newTagsData;
		if (scrollTags == null) { scrollTags = new GuiCustomScroll(this, 2).setSize(133, 93); }
		scrollTags.setList(new ArrayList<>(tagsData.keySet()));
		scrollTags.guiLeft = guiLeft + 283;
		scrollTags.guiTop = guiTop + 16;
		if (tag != null) { scrollTags.setSelected(tag.getKey()); }
		addScroll(scrollTags);
		// tag add
		addButton(new GuiNpcButton(7, guiLeft + 283, guiTop + 112, 43, 20, "gui.add", !drop.item.isEmpty() && tagsData.size() <= 24)
				.setHoverText("drop.hover.tag.add"));
		// tag del
		addButton(new GuiNpcButton(8, guiLeft + 283 + 45, guiTop + 112, 43, 20, "gui.remove", scrollTags.getSelected() != null)
				.setHoverText("drop.hover.tag.del"));
		// tag edit
		addButton(new GuiNpcButton(9, guiLeft + 283 + 91, guiTop + 112, 43, 20, "selectServer.edit", scrollTags.getSelected() != null)
				.setHoverText("drop.hover.tag.edit"));
		x = guiLeft + 329;
		y = guiTop + 146;
		// availability
		addButton(new GuiNpcButton(12, x, y, 87, 20, "availability.available")
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText("availability.hover"));
		// lootMode
		addButton(new GuiNpcButton(10, x, y += 23, 87, 20, drop.lootMode,
				"stats.normal", "inv.auto", "inv.inventory")
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText("drop.hover.mode"));
		// tied level
		int t = (int) (3.0f + 9.0f * 17.0f / (float) CustomNpcs.MaxLv);
		addButton(new GuiNpcButton(11, x, y + 23, 87, 20, drop.tiedToLevel ? 1 : 0,
				"drop.type.random", "drop.type.level")
				.setIsVisible(!drop.item.isEmpty())
				.setHoverText(new TextComponentTranslation("drop.hover.tied", TextFormatting.RED + "" + CustomNpcs.MaxLv, CustomNpcs.MaxLv, TextFormatting.YELLOW + "" + t)));
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (subgui == null && isMouseHover(mouseX, mouseY, guiLeft, guiTop - 20, width, 20)) { // check error inventory gui
			onClosed();
			return true;
		}
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

	@Override
	public void save() {
		if (drop.pos == -1) {
			if (drop.item.isEmpty()) { return; }
			if (drop.getMinAmount() == 1 && drop.getMinAmount() == 1) { drop.setAmount(drop.item.getCount(), drop.item.getCount()); }
		}
		drop.item.setCount(1);
		if (menu.dataType == 0 ) { Client.sendData(EnumPacketServer.MainmenuInvDropSave, menu.dropType, menu.groupId, drop.pos, drop.save()); }
		else if (menu.dataType == 1) { Client.sendData(EnumPacketServer.MarcetDropSave, menu.marcetID, menu.dealID, drop.save()); }
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) { return; }
		GuiNpcTextField.unfocus();
		switch (scroll.getID()) {
			case 0: enchant = enchantData.get(scroll.getSelected()); break; // scrollEnchants
			case 1: attribute = attributesData.get(scroll.getSelected()); break; // scrollAttributes
			case 2: tag = tagsData.get(scroll.getSelected()); break; // scrollTags
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: setSubGui(new SubGuiDropEnchant(enchant)); break; // scrollEnchants
			case 1: setSubGui(new SubGuiDropAttribute(attribute)); break; // scrollAttributes
			case 2: setSubGui(new SubGuiDropValueNbt(tag)); break; // scrollTags
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
			if (gui.attribute.getAttribute().isEmpty()) { drop.removeAttribute(attribute); }
			else { attribute.load(gui.attribute.getNBT()); }
		}
		else if (subgui instanceof SubGuiDropValueNbt) {
			SubGuiDropValueNbt gui = (SubGuiDropValueNbt) subgui;
			if (gui.tag.getPath().isEmpty() || gui.tag.getValues().length == 0) { drop.removeDropNbt(tag); }
			else { tag.load(gui.tag.getNBT()); }
		}
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 0: drop.setChance(textField.getDouble()); break; // common chance
			case 1: {
				amount[0] = textField.getInteger();
				drop.setAmount(amount[0], amount[1]);
				break;
			} // amount min
			case 2: {
				amount[1] = textField.getInteger();
				drop.setAmount(amount[0], amount[1]);
				break;
			} // amount max
			case 3: drop.setDamage((float) textField.getDouble()); break; // break item
		}
	}

}
