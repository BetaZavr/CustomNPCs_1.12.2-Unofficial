package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCMarks
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener {
	
	private MarkData data;
	private MarkData dataDisplay;
	private String[] marks;
	private EntityNPCInterface npcDisplay;
	// New
	private GuiCustomScroll scroll;
	private MarkData.Mark selectedMark;
	private String selMark;

	public GuiNPCMarks(EntityNPCInterface npc) {
		super(npc);
		this.marks = new String[] { "gui.none", "mark.question", "mark.exclamation", "mark.pointer", "mark.skull",
				"mark.cross", "mark.star" };
		this.data = MarkData.get(npc);
		this.npcDisplay = new EntityCustomNpc(npc.world);
		NBTTagCompound nbtData = new NBTTagCompound();
		npc.writeEntityToNBT(nbtData);
		this.npcDisplay.readEntityFromNBT(nbtData);
		this.npcDisplay.display.setShowName(1);
		this.dataDisplay = MarkData.get(this.npcDisplay);
		this.selMark = "";
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> ds = new ArrayList<String>();
		List<Integer> colors = new ArrayList<Integer>();
		int i = 0;
		for (Mark mark : this.data.marks) {
			String name = i + ": " + new TextComponentTranslation(this.marks[mark.type]).getFormattedText();
			ds.add(name);
			colors.add(mark.color);
			if (!this.selMark.isEmpty() && this.selMark.equals(name)) {
				this.selectedMark = mark;
			}
			i++;
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(130, 174);
		}
		this.scroll.setListNotSorted(ds);
		this.scroll.guiLeft = this.guiLeft + 5;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setColors(colors);
		if (this.selectedMark != null && !this.selMark.isEmpty()) {
			this.scroll.setSelected(this.selMark);
		}
		this.addScroll(this.scroll);
		if (this.selectedMark == null) {
			this.selectedMark = (Mark) this.data.getNewMark();
		}
		this.addButton(new GuiButtonBiDirectional(0, this.guiLeft + 140, this.guiTop + 14, 120, 20, this.marks,
				this.selectedMark.getType()));
		String color;
		for (color = Integer.toHexString(this.selectedMark.getColor()); color.length() < 6; color = "0" + color) {
		}
		this.addButton(new GuiNpcButton(1, this.guiLeft + 140, this.guiTop + 36, 120, 20, color));
		this.getButton(1).setTextColor(this.selectedMark.getColor());
		this.addButton(new GuiNpcButton(2, this.guiLeft + 140, this.guiTop + 58, 120, 20, "availability.options"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 5, this.guiTop + this.ySize - 9, 64, 20, "gui.add"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 71, this.guiTop + this.ySize - 9, 64, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 140, this.guiTop + 80, 120, 20, new String[] { "movement.rotation", "ai.standing" }, this.selectedMark.rotate ? 0 : 1));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 140, this.guiTop + 102, 120, 20, new String[] { "3D", "2D" }, this.selectedMark.is3d ? 0 : 1));

		this.getButton(3).enabled = this.selectedMark.type > 0;
		this.getButton(4).enabled = this.scroll.selected >= 0;

		this.dataDisplay.marks.clear();
		MarkData.Mark mark = (Mark) this.dataDisplay.addMark(this.selectedMark.type);
		mark.setColor(this.selectedMark.color);
		mark.setRotate(this.selectedMark.rotate);
		mark.set3D(this.selectedMark.is3d);
		mark.availability = new Availability();
		this.addLabel(new GuiNpcLabel(5, new TextComponentTranslation("advanced.marks").getFormattedText()+":", this.guiLeft + 5, this.guiTop + 4));
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		if (this.selectedMark == null) { return; }
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: {
				this.selectedMark.type = ((GuiNpcButton) button).getValue();
				this.initGui();
				break;
			}
			case 1: {
				this.setSubGui(new SubGuiColorSelector(this.selectedMark.color));
				break;
			}
			case 2: {
				this.setSubGui(new SubGuiNpcAvailability(this.selectedMark.availability));
				break;
			}
			case 3: {
				Mark newark = (Mark) this.data.addMark(this.selectedMark.type);
				newark.color = this.selectedMark.color;
				newark.rotate = this.selectedMark.rotate;
				newark.availability.readFromNBT(this.selectedMark.availability.writeToNBT(new NBTTagCompound()));
				this.selectedMark = newark;
				this.initGui();
				break;
			}
			case 4: {
				if (this.scroll.selected < 0) { return; }
				this.data.marks.remove(this.selectedMark);
				this.scroll.selected = -1;
				this.selMark = "";
				this.selectedMark = null;
				this.initGui();
				break;
			}
			case 5: {
				this.selectedMark.rotate = ((GuiNpcButton) button).getValue() == 0;
				this.initGui();
				break;
			}
			case 6: {
				this.selectedMark.is3d = ((GuiNpcButton) button).getValue() == 0;
				MarkRenderer.needReload = true;
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawNpc(this.npcDisplay, 350, 150, 1.0f, 0, 0, true);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(this.guiLeft + 319, this.guiTop + 30, this.guiLeft + 380, this.guiTop + 165, 0xFF808080);
		Gui.drawRect(this.guiLeft + 320, this.guiTop + 31, this.guiLeft + 379, this.guiTop + 164, 0xFF000000);
		GlStateManager.popMatrix();
		super.drawScreen(i, j, f);
		// New
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mark.hover.type").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("color.hover").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mark.hover.add").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mark.hover.del").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mark.hover.rotate").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mark.hover.is3d").getFormattedText());
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedMarkData, this.data.getNBT());
	}

	// New
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.selMark.equals(scroll.getSelected())) {
			return;
		}
		this.selMark = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector) {
			if (this.selectedMark == null) {
				return;
			}
			this.selectedMark.color = ((SubGuiColorSelector) subgui).color;
			this.initGui();
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

}
