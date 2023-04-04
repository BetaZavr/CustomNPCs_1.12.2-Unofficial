package noppes.npcs.client.gui.mainmenu;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DropSet;

public class GuiNPCInv extends GuiContainerNPCInterface2
		implements ICustomScrollListener, /* ISliderListener, */ IGuiData // Changed and New
{
	// private HashMap<Integer, Integer> chances; Changed
	private ContainerNPCInv container;
	private Map<String, DropSet> dropsData = new HashMap<String, DropSet>();
	private DataInventory inventory;
	// New
	private GuiCustomScroll scrollDrops;
	private ResourceLocation slot;

	public GuiNPCInv(EntityNPCInterface npc, ContainerNPCInv container) {
		super(npc, container, 3);
		// this.chances = new HashMap<Integer, Integer>(); Change
		this.inventory = this.npc.inventory;
		this.setBackground("npcinv.png");
		this.container = container;
		this.ySize = 200;
		this.slot = this.getResource("slot.png");
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 10: { // lootMode
			this.inventory.lootMode = (button.getValue() == 1);
			break;
		}
		case 13: { // edit
			if (this.scrollDrops.selected == -1) { return; }
			NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.scrollDrops.selected, 0, 0);
			break;
		}
		case 14: { // remove
			if (this.scrollDrops.selected == -1) { return; }
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("Item", ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
			Client.sendData(EnumPacketServer.MainmenuInvDropSave, this.scrollDrops.selected, compound);
			break;
		}
		case 15: { // add Drop
			NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, -1, 0, 0);
			break;
		}
		case 16: { // max Amount Srop
			this.inventory.setMaxAmount(button.getValue());
			break;
		}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.slot);
		for (int id = 4; id <= 6; ++id) {
			Slot slot = this.container.getSlot(id);
			if (slot.getHasStack()) {
				this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		int showname = this.npc.display.getShowName();
		this.npc.display.setShowName(1);
		this.drawNpc(50, 84);
		this.npc.display.setShowName(showname);
		super.drawScreen(i, j, f);
		// New
		if (!CustomNpcs.showDescriptions) { return; }
		String dropName = "";
		if (this.scrollDrops!=null && this.scrollDrops.selected>=0 && this.dropsData.get(this.scrollDrops.getSelected())!=null) {
			dropName = this.npc.inventory.drops.get(this.scrollDrops.selected).getItem().getDisplayName();
		}
		if (isMouseHover(i, j, this.guiLeft + 90, this.guiTop + 90, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("inv.hover.auto.xp").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 358, this.guiTop + 20, 54, 16)) {
			this.setHoverText(
					new TextComponentTranslation("inv.hover.edit.drop", new Object[] { dropName }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 358, this.guiTop + 43, 54, 16)) {
			this.setHoverText(
					new TextComponentTranslation("inv.hover.del.drop", new Object[] { dropName }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 358, this.guiTop + 66, 54, 16)) {
			this.setHoverText(new TextComponentTranslation("inv.hover.add.drop").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 177, this.guiTop + 5, 65, 10)) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.info").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 358, this.guiTop + 89, 54, 16)) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.amount").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "inv.minExp", this.guiLeft + 118, this.guiTop + 18));
		this.addTextField(new GuiNpcTextField(0, (GuiScreen) this, this.fontRenderer, this.guiLeft + 108,
				this.guiTop + 29, 60, 20, this.inventory.getExpMin() + ""));
		this.getTextField(0).numbersOnly = true;
		this.getTextField(0).setMinMaxDefault(0, 32767, 0);
		this.addLabel(new GuiNpcLabel(1, "inv.maxExp", this.guiLeft + 118, this.guiTop + 52));
		this.addTextField(new GuiNpcTextField(1, (GuiScreen) this, this.fontRenderer, this.guiLeft + 108,
				this.guiTop + 63, 60, 20, this.inventory.getExpMax() + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(0, 32767, 0);
		this.addButton(new GuiNpcButton(10, this.guiLeft + 88, this.guiTop + 88, 80, 20,
				new String[] { "stats.normal", "inv.auto" }, this.inventory.lootMode ? 1 : 0)); // Changed
		this.addLabel(new GuiNpcLabel(2, "inv.npcInventory", this.guiLeft + 191, this.guiTop + 5));
		this.addLabel(new GuiNpcLabel(3, "inv.inventory", this.guiLeft + 8, this.guiTop + 101));
		// New
		Map<String, DropSet> newData = new HashMap<String, DropSet>();
		for (ICustomDrop ids : this.inventory.getDrops()) {
			DropSet ds = (DropSet) ids;
			newData.put(ds.getKey(), ds);
		}
		this.dropsData = newData;
		if (this.scrollDrops == null) {
			(this.scrollDrops = new GuiCustomScroll(this, 11)).setSize(175, 198);
		}
		this.scrollDrops.setList(Lists.newArrayList(this.dropsData.keySet()));
		this.scrollDrops.guiLeft = this.guiLeft + 175;
		this.scrollDrops.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollDrops);

		this.addLabel(new GuiNpcLabel(12, "inv.drops", this.guiLeft + 356, this.guiTop + 4));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 356, this.guiTop + 18, 58, 20, "selectServer.edit", this.scrollDrops.selected>=0));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 356, this.guiTop + 41, 58, 20, "gui.remove", this.scrollDrops.selected>=0));
		this.addButton(new GuiNpcButton(15, this.guiLeft + 356, this.guiTop + 64, 58, 20, "gui.add", this.dropsData.size() < 32));

		String[] maxCount = new String[this.dropsData.size()];
		if (this.dropsData.size() > 0) {
			maxCount[0] = "attribute.slot.0";
			for (int i = 1; i < this.dropsData.size(); i++) {
				maxCount[i] = "" + i;
			}
		} else {
			maxCount = new String[] { "attribute.slot.0" };
		}
		int max = this.inventory.getMaxAmount();
		if (max >= this.dropsData.size()) {
			if (this.dropsData.size() == 0) {
				max = 0;
			} else {
				max = this.dropsData.size() - 1;
			}
			this.inventory.setMaxAmount(max);
		}
		this.addButton(new GuiButtonBiDirectional(16, this.guiLeft + 356, this.guiTop + 87, 58, 20, maxCount, max));
		this.getButton(16).setEnabled(this.dropsData.size() > 1);
	}

	@Override
	public void save() {
		System.out.println("start size: "+this.npc.inventory.drops.size());
		this.inventory.setExp(this.getTextField(0).getInteger(), this.getTextField(1).getInteger());
		Client.sendData(EnumPacketServer.MainmenuInvSave, this.inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		if (scroll.id == 11) {
			//this.dropsData.get(this.scrollDrops.getSelected());
		}
		this.initGui();
	}

	/*
	 * Chanced
	 * 
	 * @Override public void mouseDragged(GuiNpcSlider guiNpcSlider) {
	 * guiNpcSlider.displayString = I18n.translateToLocal("inv.dropChance") + ": " +
	 * (int)(guiNpcSlider.sliderValue * 100.0f) + "%"; }
	 * 
	 * @Override public void mousePressed(GuiNpcSlider guiNpcSlider) { }
	 * 
	 * @Override public void mouseReleased(GuiNpcSlider guiNpcSlider) {
	 * this.chances.put(guiNpcSlider.id, (int)(guiNpcSlider.sliderValue * 100.0f));
	 * }
	 */

	// New

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (scroll.id == 11) {
			if (this.dropsData.get(this.scrollDrops.getSelected()) != null) {
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.scrollDrops.selected, 0, 0);
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.inventory.readEntityFromNBT(compound);
		this.initGui();
	}

	@Override
	public void updateScreen() {
		if (this.scrollDrops != null) {
			this.scrollDrops.setList(Lists.newArrayList(this.dropsData.keySet()));
		}
		if (this.getButton(13) != null) {
			this.getButton(13).enabled = this.scrollDrops != null && this.scrollDrops.getList().size() > 0 && this.scrollDrops.selected>=0;
		}
		if (this.getButton(14) != null) {
			this.getButton(14).enabled = this.scrollDrops != null && this.scrollDrops.getList().size() > 0 && this.scrollDrops.selected>=0;
		}
		if (this.getButton(15) != null) {
			boolean b = true;
			for (ICustomDrop ids : this.inventory.getDrops()) {
				if (((DropSet) ids).item.isEmpty()) {
					b = false;
					break;
				}
			}
			this.getButton(15).enabled = b && this.dropsData.size() < 32;
		}
	}
}
