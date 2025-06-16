package noppes.npcs.client.gui.mainmenu;

import java.awt.*;
import java.util.*;
import java.util.List;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.util.Util;

public class GuiNPCInv
extends GuiContainerNPCInterface2
implements ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback, ITextfieldListener {

	private final ContainerNPCInv container;
	private final Map<String, DropSet> dropsData = new HashMap<>();
	private final DataInventory inventory;
	private DropsTemplate temp;
	private int groupId;
	private GuiCustomScroll scrollTemplate;
	private GuiCustomScroll scrollDrops;
	private final EntityNPCInterface displayNpc;

	public GuiNPCInv(EntityNPCInterface npc, ContainerNPCInv cont) {
		super(npc, cont, 3);
		setBackground("npcinv.png");
		container = cont;
		ySize = 200;

		displayNpc = Util.instance.copyToGUI(npc, mc.world, false);
		groupId = 0;
		inventory = npc.inventory;
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: { // lootMode
				this.inventory.dropType = button.getValue();
				this.initGui();
				break;
			}
			case 1: { // add Drop in NPC
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, inventory.dropType, groupId, -1);
				break;
			}
			case 2: { // edit Drop in NPC
				if (this.scrollDrops.getSelect() == -1) {
					return;
				}
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.inventory.dropType, this.groupId, this.scrollDrops.getSelect());
				break;
			}
			case 3: { // remove Drop in NPC
				if (this.scrollDrops.getSelect() == -1) { return; }
				NBTTagCompound compound = new NBTTagCompound();
				compound.setTag("Item", ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
				Client.sendData(EnumPacketServer.MainmenuInvDropSave, inventory.dropType, groupId, scrollDrops.getSelect(), compound);
				break;
			}
			case 4: { // create template
				this.setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 5: { // copy template
				if (scrollTemplate == null || !scrollTemplate.hasSelected() || this.temp == null) { return; }
				String name = scrollTemplate.getSelected();
				DropsTemplate dt = DropsTemplate.from(this.temp);
				DropController dData = DropController.getInstance();
				while (dData.templates.containsKey(name)) { name += "_"; }
				this.inventory.saveDropsName = name;
				dData.templates.put(this.inventory.saveDropsName, dt);
				this.temp = dt;
				this.initGui();
				break;
			}
			case 6: { // edit template name
				this.setSubGui(new SubGuiEditText(2, this.inventory.saveDropsName));
				break;
			}
			case 7: { // del template
				if (scrollTemplate == null || !scrollTemplate.hasSelected()) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, scrollTemplate.getSelected(), new TextComponentTranslation("gui.clearMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
			}
			case 8: { // group ID
				this.groupId = button.getValue();
				this.initGui();
				break;
			}
			case 9: { // add Drop
				groupId = temp.groups.size();
				temp.groups.put(groupId, new TreeMap<>());
				initGui();
				break;
			}
			case 10: { // lootMode
				this.inventory.lootMode = (button.getValue() == 1);
				break;
			}
			case 11: { // copy Group
				if (this.temp == null) { return; }
				Map<Integer, DropSet> parent = this.temp.groups.get(this.groupId);
				Map<Integer, DropSet> newGroup = new TreeMap<>();
				for (int id : parent.keySet()) {
					DropSet ds = new DropSet(this.inventory);
					ds.load(parent.get(id).getNBT());
					newGroup.put(id, ds);
				}
				this.groupId = this.temp.groups.size();
				this.temp.groups.put(this.groupId, newGroup);
				this.initGui();
				break;
			}
			case 12: { // del Group
				if (this.temp == null || this.temp.groups.isEmpty() || this.groupId <= 0) { return; }
				if (this.temp.groups.get(this.groupId).isEmpty()) {
					this.temp.removeGroup(this.groupId);
					this.groupId--;
					this.initGui();
				} else {
					GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.group").getFormattedText() + " ID: " + this.groupId, new TextComponentTranslation("gui.clearMessage").getFormattedText(), 1);
					displayGuiScreen(guiyesno);
				}
				break;
			}
		}
	}

	private void saveTemplate() {
		if (inventory.dropType == 1 && inventory.saveDropsName != null && !inventory.saveDropsName.isEmpty()) {
			DropController.getInstance().sendToServer(inventory.saveDropsName);
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		this.displayGuiScreen(this);
		if (!result) { return; }
		if (id == 0) { // del template
			if (scrollTemplate == null || !scrollTemplate.hasSelected()) { return; }
			DropController.getInstance().templates.remove(scrollTemplate.getSelected());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 2, scrollTemplate.getSelected());
			this.initGui();
		}
		else if (id == 1) { // del group
			if (this.temp == null || this.temp.groups.isEmpty()) { return; }
			this.temp.removeGroup(this.groupId);
			this.groupId--;
			this.initGui();
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int id = 4; id <= 8; ++id) {
			Slot slot = this.container.getSlot(id);
			this.mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			if (id > 6 && ArmourersWorkshopClientApi.getSkinRenderHandler() != null) {
				this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 0, 0, 18, 18);
				if (!slot.getHasStack()) {
					this.mc.getTextureManager().bindTexture(id == 7 ? ArmourersWorkshopUtil.getInstance().slotOutfit : ArmourersWorkshopUtil.getInstance().slotWings);
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, 0.0f);
					GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
					this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
				}
			} else if (slot.getHasStack()) {
				this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		int color = new Color(0xFF606060).getRGB();
		if (this.inventory.dropType != 0) {
			this.drawVerticalLine(this.guiLeft + 274, this.guiTop + 26, this.guiTop + this.ySize + 12, color);
		}
		if (this.inventory.dropType == 1) {
			this.drawVerticalLine(this.guiLeft + 327, this.guiTop + 162, this.guiTop + this.ySize + 12, color);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (int i = 0; i < 4; i++) { displayNpc.inventory.setArmor(i, npc.inventory.getArmor(i)); }
		displayNpc.inventory.setRightHand(npc.inventory.getRightHand());
		displayNpc.inventory.setProjectile(npc.inventory.getProjectile());
		displayNpc.inventory.setLeftHand(npc.inventory.getLeftHand());
		displayNpc.ticksExisted = npc.ticksExisted;
		drawNpc(displayNpc, 50, 84, 1.0f, 0, 0, 1);
	}

	@Override
	public void initGui() {
		super.initGui();
		// min xp
		addLabel(new GuiNpcLabel(0, "inv.minExp", this.guiLeft + 108, this.guiTop + 18));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 108, this.guiTop + 29, 60, 20, this.inventory.getExpMin() + "");
		textField.setMinMaxDefault(0, Short.MAX_VALUE, 0);
		textField.setHoverText("inv.hover.drops.minxp");
		addTextField(textField);
		// max xp
		addLabel(new GuiNpcLabel(1, "inv.maxExp", this.guiLeft + 108, this.guiTop + 52));
		textField = new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 108, this.guiTop + 63, 60, 20, this.inventory.getExpMax() + "");
		textField.setMinMaxDefault(0, Short.MAX_VALUE, 0);
		textField.setHoverText("inv.hover.drops.maxxp");
		addTextField(textField);
		// xp loot mode
		GuiNpcButton button = new GuiNpcButton(10, this.guiLeft + 107, this.guiTop + 88, 62, 20, new String[] { "stats.normal", "inv.auto" }, this.inventory.lootMode ? 1 : 0);
		button.setHoverText("inv.hover.auto.xp");
		addButton(button);
		// drop type
		addLabel(new GuiNpcLabel(2, "inv.npcInventory", this.guiLeft + 191, this.guiTop + 5));
		addLabel(new GuiNpcLabel(3, "inv.inventory", this.guiLeft + 8, this.guiTop + 101));
		button = new GuiNpcButton(0, this.guiLeft + 175, this.guiTop + 4, 120, 20, new String[] { "inv.use.drops.0", "inv.use.drops.1", "inv.use.drops.2" }, this.inventory.dropType);
		button.setHoverText("inv.hover.drops.type");
		addButton(button);
		// max amount
		textField = new GuiNpcTextField(2, this, this.guiLeft + 300, this.guiTop + 4, 60, 20, "" + this.inventory.limitation);
		textField.setMinMaxDefault(0, 128, this.inventory.limitation);
		textField.setHoverText("inv.hover.drops.amount");
		addTextField(textField);
		// data
		dropsData.clear();
		temp = null;
		String dropName = "";
		if (scrollDrops != null && scrollDrops.getSelected() != null && dropsData.get(scrollDrops.getSelected()) != null) { dropName = dropsData.get(scrollDrops.getSelected()).getItem().getDisplayName(); }
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		List<ItemStack> stacks = new ArrayList<>();
		LogWriter.debug("Inventory drop type: " + inventory.dropType);
		if (this.inventory.dropType == 0) {
			if (inventory.getDrops().length > 0) {
				int i = 0;
				for (ICustomDrop ids : inventory.getDrops()) {
					DropSet ds = (DropSet) ids;
					dropsData.put(ds.getKey(), ds);
					hts.put(i++, ds.getHover(player));
					stacks.add(ds.item.getMCItemStack());
				}
			}
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }
			scrollDrops.setSize(238, 157);
			scrollDrops.setList(new ArrayList<>(dropsData.keySet()));
			scrollDrops.guiLeft = this.guiLeft + 175;
			scrollDrops.guiTop = this.guiTop + 38;
			scrollDrops.setHoverTexts(hts);
			scrollDrops.setStacks(stacks);
			addScroll(this.scrollDrops);
			this.addLabel(new GuiNpcLabel(4, "inv.drops", this.guiLeft + 176, this.guiTop + 27));
			button = new GuiNpcButton(1, this.guiLeft + 175, this.guiTop + 197, 60, 15, "gui.add", this.dropsData.size() < CustomNpcs.MaxItemInDropsNPC);
			button.setHoverText("inv.hover.add.drop", "" + CustomNpcs.MaxItemInDropsNPC);
			addButton(button);
			button = new GuiNpcButton(2, this.guiLeft + 240, this.guiTop + 197, 60, 15, "selectServer.edit", this.scrollDrops.getSelect() >= 0);
			button.setHoverText("inv.hover.edit.drop", dropName);
			addButton(button);
			button = new GuiNpcButton(3, this.guiLeft + 305, this.guiTop + 197, 60, 15, "gui.remove", this.scrollDrops.getSelect() >= 0);
			button.setHoverText("inv.hover.del.drop", dropName);
			addButton(button);
		}
		else if (this.inventory.dropType == 1) {
			this.addLabel(new GuiNpcLabel(4, "gui.templates", this.guiLeft + 176, this.guiTop + 27));
			if (scrollTemplate == null) { scrollTemplate = new GuiCustomScroll(this, 0); }
			scrollTemplate.setSize(98, 140);
			scrollTemplate.setList(new ArrayList<>(DropController.getInstance().templates.keySet()));
			scrollTemplate.guiLeft = this.guiLeft + 175;
			scrollTemplate.guiTop = this.guiTop + 38;
			this.addScroll(scrollTemplate);
			if (DropController.getInstance().templates.containsKey(inventory.saveDropsName)) {
				temp = DropController.getInstance().templates.get(inventory.saveDropsName);
				scrollTemplate.setSelected(inventory.saveDropsName);
				if (temp.groups.containsKey(groupId)) {
					int i = 0;
					for (DropSet ds : temp.groups.get(groupId).values()) {
						dropsData.put(ds.getKey(), ds);
						hts.put(i++, ds.getHover(player));
						stacks.add(ds.item.getMCItemStack());
					}
				}
			}
			else { this.groupId = 0; }
			button = new GuiNpcButton(4, this.guiLeft + 175, this.guiTop + 180, 48, 15, "gui.add", true);
			button.setHoverText("inv.hover.new.template");
			addButton(button);
			button = new GuiNpcButton(5, this.guiLeft + 175, this.guiTop + 197, 48, 15, "gui.copy", !inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"));
			button.setHoverText("inv.hover.copy.template");
			addButton(button);
			button = new GuiNpcButton(6, this.guiLeft + 225, this.guiTop + 180, 48, 15, "selectServer.edit", !inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"));
			button.setHoverText("inv.hover.rename.template");
			addButton(button);
			button = new GuiNpcButton(7, this.guiLeft + 225, this.guiTop + 197, 48, 15, "gui.remove", !this.inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"));
			button.setHoverText("inv.hover.del.template");
			addButton(button);
			this.addLabel(new GuiNpcLabel(5, "gui.groups", this.guiLeft + 277, this.guiTop + 30));
			List<String> l = new ArrayList<>();
			int g = 1;
			if (this.temp != null && !this.temp.groups.isEmpty()) { g = this.temp.groups.size(); }
			for (int i = 0; i < g; i++) { l.add((i + 1)+ " / " + g); }
			button = new GuiButtonBiDirectional(8, this.guiLeft + 346, this.guiTop + 27, 70, 15, l.toArray(new String[0]), this.groupId);
			button.setHoverText("inv.hover.group.id");
			addButton(button);
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }
			scrollDrops.setSize(140, 117);
			scrollDrops.setList(new ArrayList<>(dropsData.keySet()));
			scrollDrops.guiLeft = guiLeft + 276;
			scrollDrops.guiTop = guiTop + 44;
			scrollDrops.setHoverTexts(hts);
			scrollDrops.setStacks(stacks);
			addScroll(scrollDrops);
			// Stacks
			button = new GuiNpcButton(1, this.guiLeft + 330, this.guiTop + 163, 48, 15, "gui.add", !inventory.saveDropsName.isEmpty());
			button.setHoverText("inv.hover.add.drop", "9000");
			addButton(button);
			button = new GuiNpcButton(2, this.guiLeft + 330, this.guiTop + 180, 48, 15, "selectServer.edit", scrollDrops.getSelected() != null);
			button.setHoverText("inv.hover.edit.drop", dropName);
			addButton(button);
			button = new GuiNpcButton(3, this.guiLeft + 330, this.guiTop + 197, 48, 15, "gui.remove", scrollDrops.hasSelected() && !(inventory.saveDropsName.equals("default") && scrollDrops.getSelect() < 4));
			button.setHoverText("inv.hover.del.drop", dropName);
			addButton(button);
			// Groups
			button = new GuiNpcButton(9, this.guiLeft + 277, this.guiTop + 163, 48, 15, "gui.add", true);
			button.setHoverText("inv.hover.add.group");
			addButton(button);
			button = new GuiNpcButton(11, this.guiLeft + 277, this.guiTop + 180, 48, 15, "gui.copy", temp != null && this.temp.groups.containsKey(this.groupId));
			button.setHoverText("inv.hover.copy.group");
			addButton(button);
			button = new GuiNpcButton(12, this.guiLeft + 277, this.guiTop + 197, 48, 15, "gui.remove", temp != null && temp.groups.containsKey(groupId) && groupId > 0);
			button.setHoverText("inv.hover.del.group");
			addButton(button);
		}
		else {
			this.addLabel(new GuiNpcLabel(4, "gui.templates", this.guiLeft + 176, this.guiTop + 27));
			this.addLabel(new GuiNpcLabel(5, "inv.drops", this.guiLeft + 277, this.guiTop + 27));
			if (inventory.getDrops().length > 0) {
				int i = 0;
				for (ICustomDrop ids : inventory.getDrops()) {
					DropSet ds = (DropSet) ids;
					dropsData.put(ds.getKey(), ds);
					hts.put(i++, ds.getHover(player));
					stacks.add(ds.item.getMCItemStack());
				}
			}
			if (this.scrollTemplate == null) { this.scrollTemplate = new GuiCustomScroll(this, 0); }
			this.scrollTemplate.setSize(98, 174);
			this.scrollTemplate.setList(new ArrayList<>(DropController.getInstance().templates.keySet()));
			this.scrollTemplate.guiLeft = this.guiLeft + 175;
			this.scrollTemplate.guiTop = this.guiTop + 38;
			this.addScroll(this.scrollTemplate);
			if (DropController.getInstance().templates.containsKey(this.inventory.saveDropsName)) {
				this.temp = DropController.getInstance().templates.get(this.inventory.saveDropsName);
				this.scrollTemplate.setSelected(this.inventory.saveDropsName);
			}
			else { this.groupId = 0; }
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }

			scrollDrops.setSize(140, 157);
			scrollDrops.setList(new ArrayList<>(dropsData.keySet()));
			scrollDrops.guiLeft = this.guiLeft + 276;
			scrollDrops.guiTop = this.guiTop + 38;
			scrollDrops.setHoverTexts(hts);
			scrollDrops.setStacks(stacks);
			addScroll(scrollDrops);

			button = new GuiNpcButton(1, this.guiLeft + 277, this.guiTop + 197, 45, 15, "gui.add", this.dropsData.size() < CustomNpcs.MaxItemInDropsNPC);
			button.setHoverText("inv.hover.add.drop", "" + CustomNpcs.MaxItemInDropsNPC);
			addButton(button);
			button = new GuiNpcButton(2, this.guiLeft + 324, this.guiTop + 197, 45, 15, "selectServer.edit", this.scrollDrops.getSelect() >= 0);
			button.setHoverText("inv.hover.edit.drop", dropName);
			addButton(button);
			button = new GuiNpcButton(3, this.guiLeft + 371, this.guiTop + 197, 45, 15, "gui.remove", this.scrollDrops.getSelect() >= 0);
			button.setHoverText("inv.hover.del.drop", dropName);
			addButton(button);
		}
	}
	
	@Override
	public void save() {
		saveTemplate();
		Client.sendData(EnumPacketServer.MainmenuInvSave, inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getSelected() == null) { return; }
		if (scroll.getID() == 0) {
			saveTemplate();
			if (scroll.getSelected().equals(inventory.saveDropsName)) {
				inventory.saveDropsName = "";
				scroll.setSelected(null);
			}
			else { inventory.saveDropsName = scroll.getSelected(); }
			Client.sendData(EnumPacketServer.MainmenuInvSave, inventory.writeEntityToNBT(new NBTTagCompound()));
			if (inventory.dropType == 1) { groupId = 0; }
			initGui();
		}
		if (scroll.getID() == 1) {
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (scroll.getID() == 1) {
			if (dropsData.get(scrollDrops.getSelected()) != null) {
				this.saveTemplate();
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.inventory.dropType, this.groupId, this.scrollDrops.getSelect());
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.inventory.readEntityFromNBT(compound);
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		DropController dData = DropController.getInstance();
        String name = ((SubGuiEditText) subgui).text[0];
		if (subgui.getId() == 1) { // create template
			while (dData.templates.containsKey(name)) { name += "_"; }
			this.inventory.saveDropsName = name;
			dData.templates.put(this.inventory.saveDropsName, new DropsTemplate());
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", name);
			nbtTemplate.setTag("Groups", dData.templates.get(this.inventory.saveDropsName).getNBT());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
		}
		else if (subgui.getId() == 2) { // rename template
			if (name == null || name.equals(this.inventory.saveDropsName) || dData.templates.containsKey(name) || !dData.templates.containsKey(this.inventory.saveDropsName)) { return; }
			dData.templates.put(name, dData.templates.get(this.inventory.saveDropsName));
			dData.templates.remove(this.inventory.saveDropsName);
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 2, this.inventory.saveDropsName);
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", name);
			nbtTemplate.setTag("Groups", dData.templates.get(name).getNBT());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
		}
		this.initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		switch(textField.getID()) {
			case 0: {
				this.inventory.setExp(textField.getInteger(), this.getTextField(1).getInteger());
				break;
			}
			case 1: {
				this.inventory.setExp(this.getTextField(0).getInteger(), textField.getInteger());
				break;
			}
			case 2: {
				this.inventory.limitation = this.getTextField(2).getInteger();
				break;
			}
		}
	}

}
