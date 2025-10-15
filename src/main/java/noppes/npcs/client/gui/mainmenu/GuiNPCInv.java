package noppes.npcs.client.gui.mainmenu;

import java.awt.*;
import java.util.*;
import java.util.List;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.drop.SubGuiDropEdit;
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

import javax.annotation.Nonnull;

public class GuiNPCInv extends GuiContainerNPCInterface2
		implements ICustomScrollListener, IGuiData, GuiYesNoCallback, ITextfieldListener {

	protected final ContainerNPCInv container;
	protected final Map<String, DropSet> dropsData = new HashMap<>();
	protected final EntityNPCInterface displayNpc;
	protected final DataInventory inventory;
	protected GuiCustomScroll scrollTemplate;
	protected GuiCustomScroll scrollDrops;
	protected DropsTemplate temp;
	protected int groupId = 0;

	public GuiNPCInv(EntityNPCInterface npc, ContainerNPCInv cont) {
		super(npc, cont, 3);
		setBackground("npcinv.png");
		container = cont;
		ySize = 200;

		displayNpc = Util.instance.copyToGUI(npc, mc.world, false);
		inventory = npc.inventory;
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				inventory.dropType = button.getValue();
				initGui();
				break;
			} // lootMode
			case 1: {
				SubGuiDropEdit.parent = null;
				SubGuiDropEdit.parentContainer = EnumGuiType.MainMenuInv;
				SubGuiDropEdit.parentData = BlockPos.ORIGIN;
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("InventoryType", 0);
				compound.setInteger("DropType", inventory.dropType);
				compound.setInteger("GroupId", groupId);
				compound.setInteger("Pos", -1);
				compound.setInteger("EntityId", npc.getEntityId());
				NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
				break;
			} // add Drop in NPC
			case 2: {
				if (!scrollDrops.hasSelected()) { return; }
				SubGuiDropEdit.parent = null;
				SubGuiDropEdit.parentContainer = EnumGuiType.MainMenuInv;
				SubGuiDropEdit.parentData = BlockPos.ORIGIN;
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("InventoryType", 0);
				compound.setInteger("DropType", inventory.dropType);
				compound.setInteger("GroupId", groupId);
				compound.setInteger("Pos", scrollDrops.getSelect());
				compound.setInteger("EntityId", npc.getEntityId());
				NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
				break;
			} // edit Drop in NPC
			case 3: {
				if (!scrollDrops.hasSelected()) { return; }
				NBTTagCompound compound = new NBTTagCompound();
				compound.setTag("Item", ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
				Client.sendData(EnumPacketServer.MainmenuInvDropSave, inventory.dropType, groupId, scrollDrops.getSelect(), compound);
				break;
			} // remove Drop in NPC
			case 4: {
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			} // create template
			case 5: {
				if (scrollTemplate == null || !scrollTemplate.hasSelected() || temp == null) { return; }
				String name = scrollTemplate.getSelected();
				DropsTemplate dt = DropsTemplate.from(temp);
				DropController dData = DropController.getInstance();
				while (dData.templates.containsKey(name)) { name += "_"; }
				inventory.saveDropsName = name;
				dData.templates.put(inventory.saveDropsName, dt);
				temp = dt;
				initGui();
				break;
			} // copy template
			case 6: {
				setSubGui(new SubGuiEditText(2, inventory.saveDropsName));
				break;
			} // edit template name
			case 7: {
				if (scrollTemplate == null || !scrollTemplate.hasSelected()) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, scrollTemplate.getSelected(), new TextComponentTranslation("gui.clearMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
			} // del template
			case 8: {
				groupId = button.getValue();
				initGui();
				break;
			} // group ID
			case 9: {
				groupId = temp.groups.size();
				temp.groups.put(groupId, new TreeMap<>());
				initGui();
				break;
			} // add Drop
			case 10: {
				inventory.lootMode = (button.getValue() == 1);
				break;
			} // lootMode
			case 11: {
				if (temp == null) { return; }
				Map<Integer, DropSet> parent = temp.groups.get(groupId);
				Map<Integer, DropSet> newGroup = new TreeMap<>();
				for (int id : parent.keySet()) {
					DropSet ds = new DropSet(inventory, null);
					ds.load(parent.get(id).save());
					newGroup.put(id, ds);
				}
				groupId = temp.groups.size();
				temp.groups.put(groupId, newGroup);
				initGui();
				break;
			} // copy Group
			case 12: {
				if (temp == null || temp.groups.isEmpty() || groupId <= 0) { return; }
				if (temp.groups.get(groupId).isEmpty()) {
					temp.removeGroup(groupId);
					groupId--;
					initGui();
				} else {
					GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.group").getFormattedText() + " ID: " + groupId, new TextComponentTranslation("gui.clearMessage").getFormattedText(), 1);
					displayGuiScreen(guiyesno);
				}
				break;
			} // del Group
		}
	}

	private void saveTemplate() {
		if (inventory.dropType == 1 && inventory.saveDropsName != null && !inventory.saveDropsName.isEmpty()) {
			DropController.getInstance().sendToServer(inventory.saveDropsName);
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		displayGuiScreen(this);
		if (!result) { return; }
		if (id == 0) {
			if (scrollTemplate == null || !scrollTemplate.hasSelected()) { return; }
			DropController.getInstance().templates.remove(scrollTemplate.getSelected());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 2, scrollTemplate.getSelected());
			initGui();
		} // del template
		else if (id == 1) {
			if (temp == null || temp.groups.isEmpty()) { return; }
			temp.removeGroup(groupId);
			groupId--;
			initGui();
		} // del group
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int id = 4; id <= 8; ++id) {
			Slot slot = container.getSlot(id);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			if (id > 6 && ArmourersWorkshopClientApi.getSkinRenderHandler() != null) {
				drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 0, 0, 18, 18);
				if (!slot.getHasStack()) {
					mc.getTextureManager().bindTexture(id == 7 ? ArmourersWorkshopUtil.getInstance().slotOutfit : ArmourersWorkshopUtil.getInstance().slotWings);
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft + slot.xPos, guiTop + slot.yPos, 0.0f);
					GlStateManager.scale(0.0625f, 0.0625f, 0.0625f);
					drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
				}
			}
			else if (slot.getHasStack()) { drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 0, 0, 18, 18); }
		}
		int color = new Color(0xFF606060).getRGB();
		if (inventory.dropType != 0) { drawVerticalLine(guiLeft + 274, guiTop + 26, guiTop + ySize + 12, color); }
		if (inventory.dropType == 1) { drawVerticalLine(guiLeft + 327, guiTop + 162, guiTop + ySize + 12, color); }

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
		addLabel(new GuiNpcLabel(0, "inv.minExp",  guiLeft + 108, guiTop + 18, 66, 12));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 108, guiTop + 29, 60, 20, inventory.getExpMin() + "")
				.setMinMaxDefault(0, Short.MAX_VALUE, 0)
				.setHoverText("inv.hover.drops.minxp"));
		// max xp
		addLabel(new GuiNpcLabel(1, "inv.maxExp", guiLeft + 108, guiTop + 52, 66, 12));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 108, guiTop + 63, 60, 20, inventory.getExpMax() + "")
				.setMinMaxDefault(0, Short.MAX_VALUE, 0)
				.setHoverText("inv.hover.drops.maxxp"));
		// xp loot mode
		addButton(new GuiNpcButton(10, guiLeft + 107, guiTop + 88, 62, 20, new String[] { "stats.normal", "inv.auto" }, inventory.lootMode ? 1 : 0)
				.setHoverText("inv.hover.auto.xp"));
		// drop type
		addLabel(new GuiNpcLabel(2, "inv.npcInventory", guiLeft + 176, guiTop + 5, 82, 12));
		addLabel(new GuiNpcLabel(3, "inv.inventory", guiLeft + 8, guiTop + 101, 166, 12));
		addButton(new GuiNpcButton(0, guiLeft + 260, guiTop + 4, 120, 20, new String[] { "inv.use.drops.0", "inv.use.drops.1", "inv.use.drops.2" }, inventory.dropType)
				.setHoverText("inv.hover.drops.type"));
		// max amount
		addTextField(new GuiNpcTextField(2, this, guiLeft + 364, guiTop + 5, 48, 18, "" + inventory.limitation)
				.setMinMaxDefault(0, 128, inventory.limitation)
				.setHoverText("inv.hover.drops.amount"));
		// data
		dropsData.clear();
		temp = null;
		String dropName = "";
		if (scrollDrops != null && scrollDrops.getSelected() != null && dropsData.get(scrollDrops.getSelected()) != null) { dropName = dropsData.get(scrollDrops.getSelected()).getItem().getDisplayName(); }
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		List<ItemStack> stacks = new ArrayList<>();
		if (inventory.dropType == 0) {
			if (inventory.getDrops().length > 0) {
				int i = 0;
				for (ICustomDrop ids : inventory.getDrops()) {
					DropSet ds = (DropSet) ids;
					dropsData.put(ds.getKey(), ds);
					hts.put(i++, ds.getHover(player));
					stacks.add(ds.item);
				}
			}
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }
			scrollDrops.guiLeft = guiLeft + 175;
			scrollDrops.guiTop = guiTop + 38;
			addScroll(scrollDrops.setSize(238, 157)
					.setUnsortedList(new ArrayList<>(dropsData.keySet()))
					.setHoverTexts(hts)
					.setStacks(stacks));
			addLabel(new GuiNpcLabel(4, "inv.drops", guiLeft + 176, guiTop + 27, 236, 12));
			addButton(new GuiNpcButton(1, guiLeft + 175, guiTop + 197, 60, 15, "gui.add", dropsData.size() < CustomNpcs.MaxItemInDropsNPC)
					.setHoverText("inv.hover.add.drop", "" + CustomNpcs.MaxItemInDropsNPC));
			addButton(new GuiNpcButton(2, guiLeft + 240, guiTop + 197, 60, 15, "selectServer.edit", scrollDrops.hasSelected())
					.setHoverText("inv.hover.edit.drop", dropName));
			addButton(new GuiNpcButton(3, guiLeft + 305, guiTop + 197, 60, 15, "gui.remove", scrollDrops.hasSelected())
					.setHoverText("inv.hover.del.drop", dropName));
		}
		else if (inventory.dropType == 1) {
			addLabel(new GuiNpcLabel(4, "gui.templates", guiLeft + 176, guiTop + 27, 96, 12));
			if (scrollTemplate == null) { scrollTemplate = new GuiCustomScroll(this, 0); }
			scrollTemplate.guiLeft = guiLeft + 175;
			scrollTemplate.guiTop = guiTop + 38;
			addScroll(scrollTemplate.setSize(98, 140)
					.setList(new ArrayList<>(DropController.getInstance().templates.keySet())));
			if (DropController.getInstance().templates.containsKey(inventory.saveDropsName)) {
				temp = DropController.getInstance().templates.get(inventory.saveDropsName);
				scrollTemplate.setSelected(inventory.saveDropsName);
				if (temp.groups.containsKey(groupId)) {
					int i = 0;
					for (DropSet ds : temp.groups.get(groupId).values()) {
						dropsData.put(ds.getKey(), ds);
						hts.put(i++, ds.getHover(player));
						stacks.add(ds.item);
					}
				}
			}
			else { groupId = 0; }
			addButton(new GuiNpcButton(4, guiLeft + 175, guiTop + 180, 48, 15, "gui.add")
					.setHoverText("inv.hover.new.template"));
			addButton(new GuiNpcButton(5, guiLeft + 175, guiTop + 197, 48, 15, "gui.copy", !inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"))
					.setHoverText("inv.hover.copy.template"));
			addButton(new GuiNpcButton(6, guiLeft + 225, guiTop + 180, 48, 15, "selectServer.edit", !inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"))
					.setHoverText("inv.hover.rename.template"));
			addButton(new GuiNpcButton(7, guiLeft + 225, guiTop + 197, 48, 15, "gui.remove", !inventory.saveDropsName.isEmpty() && !inventory.saveDropsName.equals("default"))
					.setHoverText("inv.hover.del.template"));
			addLabel(new GuiNpcLabel(5, "gui.groups", guiLeft + 277, guiTop + 30, 67, 12));
			List<String> l = new ArrayList<>();
			int g = 1;
			if (temp != null && !temp.groups.isEmpty()) { g = temp.groups.size(); }
			for (int i = 0; i < g; i++) { l.add((i + 1)+ " / " + g); }
			addButton(new GuiButtonBiDirectional(8, guiLeft + 346, guiTop + 27, 70, 15, l.toArray(new String[0]), groupId)
					.setHoverText("inv.hover.group.id"));
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }
			scrollDrops.guiLeft = guiLeft + 276;
			scrollDrops.guiTop = guiTop + 44;
			addScroll(scrollDrops.setSize(140, 117)
					.setUnsortedList(new ArrayList<>(dropsData.keySet()))
					.setHoverTexts(hts)
					.setStacks(stacks));
			// Stacks
			addButton(new GuiNpcButton(1, guiLeft + 330, guiTop + 163, 48, 15, "gui.add", !inventory.saveDropsName.isEmpty())
					.setHoverText("inv.hover.add.drop", "9000"));
			addButton(new GuiNpcButton(2, guiLeft + 330, guiTop + 180, 48, 15, "selectServer.edit", scrollDrops.hasSelected())
					.setHoverText("inv.hover.edit.drop", dropName));
			addButton(new GuiNpcButton(3, guiLeft + 330, guiTop + 197, 48, 15, "gui.remove", scrollDrops.hasSelected() && !(inventory.saveDropsName.equals("default") && scrollDrops.getSelect() < 4))
					.setHoverText("inv.hover.del.drop", dropName));
			// Groups
			addButton(new GuiNpcButton(9, guiLeft + 277, guiTop + 163, 48, 15, "gui.add")
					.setHoverText("inv.hover.add.group"));
			addButton(new GuiNpcButton(11, guiLeft + 277, guiTop + 180, 48, 15, "gui.copy", temp != null && temp.groups.containsKey(groupId))
					.setHoverText("inv.hover.copy.group"));
			addButton(new GuiNpcButton(12, guiLeft + 277, guiTop + 197, 48, 15, "gui.remove", temp != null && temp.groups.containsKey(groupId) && groupId > 0)
					.setHoverText("inv.hover.del.group"));
		}
		else {
			addLabel(new GuiNpcLabel(4, "gui.templates", guiLeft + 176, guiTop + 27, 96, 12));
			addLabel(new GuiNpcLabel(5, "inv.drops", guiLeft + 277, guiTop + 27, 138, 12));
			if (inventory.getDrops().length > 0) {
				int i = 0;
				for (ICustomDrop ids : inventory.getDrops()) {
					DropSet ds = (DropSet) ids;
					dropsData.put(ds.getKey(), ds);
					hts.put(i++, ds.getHover(player));
					stacks.add(ds.item);
				}
			}
			if (scrollTemplate == null) { scrollTemplate = new GuiCustomScroll(this, 0); }
			scrollTemplate.guiLeft = guiLeft + 175;
			scrollTemplate.guiTop = guiTop + 38;
			addScroll(scrollTemplate.setSize(98, 174)
					.setList(new ArrayList<>(DropController.getInstance().templates.keySet())));
			if (DropController.getInstance().templates.containsKey(inventory.saveDropsName)) {
				temp = DropController.getInstance().templates.get(inventory.saveDropsName);
				scrollTemplate.setSelected(inventory.saveDropsName);
			}
			else { groupId = 0; }
			if (scrollDrops == null) { scrollDrops = new GuiCustomScroll(this, 1); }
			scrollDrops.guiLeft = guiLeft + 276;
			scrollDrops.guiTop = guiTop + 38;
			addScroll(scrollDrops.setSize(140, 157)
					.setUnsortedList(new ArrayList<>(dropsData.keySet()))
					.setHoverTexts(hts)
					.setStacks(stacks));

			addButton(new GuiNpcButton(1, guiLeft + 277, guiTop + 197, 45, 15, "gui.add", dropsData.size() < CustomNpcs.MaxItemInDropsNPC)
					.setHoverText("inv.hover.add.drop", "" + CustomNpcs.MaxItemInDropsNPC));
			addButton(new GuiNpcButton(2, guiLeft + 324, guiTop + 197, 45, 15, "selectServer.edit", scrollDrops.hasSelected())
					.setHoverText("inv.hover.edit.drop", dropName));
			addButton(new GuiNpcButton(3, guiLeft + 371, guiTop + 197, 45, 15, "gui.remove", scrollDrops.hasSelected())
					.setHoverText("inv.hover.del.drop", dropName));
		}
	}
	
	@Override
	public void save() {
		saveTemplate();
		Client.sendData(EnumPacketServer.MainmenuInvSave, inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.hasSelected()) {
			if (scroll.getID() == 0) {
				saveTemplate();
				if (scroll.getSelected().equals(inventory.saveDropsName)) {
					inventory.saveDropsName = "";
					scroll.setSelected(null);
				}
				else { inventory.saveDropsName = scroll.getSelected(); }
				Client.sendData(EnumPacketServer.MainmenuInvSave, inventory.writeEntityToNBT(new NBTTagCompound()));
				if (inventory.dropType == 1) { groupId = 0; }
			}
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (scroll.getID() == 1) {
			if (dropsData.get(scrollDrops.getSelected()) != null) {
				saveTemplate();
				SubGuiDropEdit.parent = null;
				SubGuiDropEdit.parentContainer = EnumGuiType.MainMenuInv;
				SubGuiDropEdit.parentData = BlockPos.ORIGIN;
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("InventoryType", 0);
				compound.setInteger("DropType", inventory.dropType);
				compound.setInteger("GroupId", groupId);
				compound.setInteger("Pos", scrollDrops.getSelect());
				compound.setInteger("EntityId", npc.getEntityId());
				NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NpcInv", 9)) {
			inventory.readEntityFromNBT(compound);
			initGui();
		}
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiEditText && !((SubGuiEditText) subgui).cancelled) {
			DropController dData = DropController.getInstance();
			String name = ((SubGuiEditText) subgui).text[0];
			if (((SubGuiEditText) subgui).getId() == 1) {
				while (dData.templates.containsKey(name)) { name += "_"; }
				inventory.saveDropsName = name;
				dData.templates.put(inventory.saveDropsName, new DropsTemplate());
				NBTTagCompound nbtTemplate = new NBTTagCompound();
				nbtTemplate.setString("Name", name);
				nbtTemplate.setTag("Groups", dData.templates.get(inventory.saveDropsName).getNBT());
				Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
			} // create template
			else if (((SubGuiEditText) subgui).getId() == 2) {
				if (name == null || name.equals(inventory.saveDropsName) || dData.templates.containsKey(name) || !dData.templates.containsKey(inventory.saveDropsName)) { return; }
				dData.templates.put(name, dData.templates.get(inventory.saveDropsName));
				dData.templates.remove(inventory.saveDropsName);
				Client.sendDirectData(EnumPacketServer.DropTemplateSave, 2, inventory.saveDropsName);
				NBTTagCompound nbtTemplate = new NBTTagCompound();
				nbtTemplate.setString("Name", name);
				nbtTemplate.setTag("Groups", dData.templates.get(name).getNBT());
				Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
			} // rename template
			initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getID()) {
			case 0: inventory.setExp(textField.getInteger(), getTextField(1).getInteger()); break;
			case 1: inventory.setExp(getTextField(0).getInteger(), textField.getInteger()); break;
			case 2: inventory.limitation = getTextField(2).getInteger(); break;
		}
	}

}
